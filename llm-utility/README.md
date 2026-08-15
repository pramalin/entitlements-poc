# Entitlements POC — LLM Utility (Step 4)

**Stack:** Java 25 · Spring Boot 4.1.0 · Spring AI 2.0.0 (OpenAI-compatible client, pointed at a local vLLM server by default) · plain JDBC (no JPA)

## What this is

This is the whole point of the original proposal: **descriptions are generated offline,
once, and stored** - not fetched from an LLM every time someone hovers over an access
title in the UI. This module is a standalone batch job, not a service:

- No web starter, no port, no `restart` policy.
- Reads every row in `entitlements` that doesn't yet have a matching row in
  `entitlement_descriptions`.
- Asks the model (via Spring AI's `ChatClient`) to write a plain-English description,
  plus an optional short risk note for anything meaningfully privileged (production
  write access, financial approval authority, admin/sudo, bulk export,
  segregation-of-duties conflicts like Diego's wire-initiate + wire-approve).
- Writes the result to `entitlement_descriptions` and exits.
- Safe to re-run: it only ever processes entitlements still missing a description, so
  adding new entitlements later and re-running the utility just fills in the gaps.

## Which model

Defaults to a **local vLLM server** at `http://192.168.1.251:8000`, serving
`qwen/qwen3.5` - Spring AI's OpenAI-compatible client works against any server that
speaks the OpenAI API shape (vLLM, Ollama, LM Studio, or the real OpenAI/cloud APIs),
so switching later is an environment variable change, not a code change:

| Variable          | Default                        | What it controls                          |
|--------------------|--------------------------------|--------------------------------------------|
| `OPENAI_BASE_URL`  | `http://192.168.1.251:8000`    | The server to talk to (no `/v1` suffix - Spring AI adds that itself) |
| `OPENAI_API_KEY`   | `not-needed`                   | Local servers usually don't check this; set a real key to use a cloud provider |
| `OPENAI_MODEL`     | `qwen/qwen3.5`                 | Must match a model id the server actually serves - check `GET <base-url>/v1/models` |

## Running it

**This is a fully separate Docker Compose project from the app stack** - it has its
own `compose.yaml` right here in this directory, not a service inside the root
`compose.yaml`. That's deliberate: this is offline AI tooling used to prepare data,
not something that would ever ship as part of the running application, and keeping it
isolated leaves room to add MCP servers or other AI tooling here later without
touching the app stack.

**Prerequisites:**
1. The app stack must already be running (from the project root): `docker compose up -d`
   - This utility reaches Postgres via the host-published port, not a shared Docker
     network, since it's a separate Compose project.
2. Your local vLLM server needs to be reachable from wherever Docker runs this
   container - if it's on your LAN (like the default `192.168.1.251` above), that
   should just work; no `.env` file is required for the local-server defaults.

Run it:
```bash
docker compose run --rm llm-utility
```
(`--rm` cleans up the exited container automatically, since it's not meant to stick
around like a service.)

**Switching to a cloud provider later:** copy `.env.example` to `.env` and uncomment/
fill in `OPENAI_BASE_URL`, `OPENAI_API_KEY`, and `OPENAI_MODEL` for whichever provider
you're using - no other changes needed.

You'll see per-entitlement progress in the console, e.g.:
```
Generating descriptions for 25 entitlement(s) using qwen/qwen3.5...
  [ok]     SAP_CO_COST_CTR_MAINT         Lets you create and edit cost centers used for...
  [ok]     SAP_TREASURY_WIRE_INIT        Starts an outbound wire transfer for approval...
           ⚠ Combined with wire-approve, bypasses maker-checker control
  ...
Done. 25 succeeded, 0 failed out of 25.
```

Then refresh the frontend (`http://localhost:5173`) - hovering over access titles now
shows real descriptions instead of "No description generated yet."

## Running without Docker
```bash
cd llm-utility
export DB_HOST=localhost   # if Postgres isn't in a container
mvn spring-boot:run
```
(No `OPENAI_*` exports needed for the local vLLM defaults; set them if pointing
somewhere else.)

## Notes
- `spring.main.web-application-type: none` - explicit, though Spring Boot would infer
  this anyway from the missing web starter.
- `SpringApplication.exit(context)` + `System.exit(...)` in `main()` - this is what
  makes `docker compose run` actually return control to your shell instead of hanging.
  A plain `CommandLineRunner` finishing isn't enough on its own to guarantee JVM exit.
- The prompt explicitly tells the model not to invent risk notes for routine access -
  worth checking the actual output once you run it; if everything comes back flagged,
  the prompt needs tightening.
- **Not yet verified end-to-end in this environment** - Spring AI's Maven artifacts
  need the same Maven Central access the backend needed, which isn't available in the
  sandbox this was written in. The dependency coordinates and Spring Boot 4
  compatibility were checked against Spring's official 2.0.0 GA announcement rather
  than guessed, but `mvn package` here is the real test, same as it was for `backend`.
