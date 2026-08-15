# Entitlement Access Viewer — Offline LLM Utility

Standalone Spring AI batch utility that enriches entitlement metadata with plain-English descriptions before the runtime application needs it.

## Why this is separate

The central architectural decision in this POC is that an LLM should **not** be called whenever a reviewer opens an entitlement.

Instead, enrichment runs offline and writes reusable reference data to PostgreSQL.

The utility is therefore:

- a batch job, not a web service;
- packaged in a separate Docker Compose project;
- not required for the frontend or backend to run;
- manually triggered when entitlement descriptions need to be generated.

The runtime application simply reads the stored result.

## Stack

- Java 25
- Spring Boot 4.1
- Spring AI 2.0
- Spring AI OpenAI-compatible client
- plain JDBC
- PostgreSQL

## Processing flow

For each entitlement that does not yet have a row in `entitlement_descriptions`, the utility:

1. reads the entitlement and its application metadata;
2. builds a prompt from the cryptic title, type, source system, and raw attributes;
3. asks the configured model for:
   - a concise plain-English description; and
   - an optional risk hint when the entitlement itself is clearly privileged or sensitive;
4. stores the result with the model name and generation timestamp;
5. continues with the next entitlement;
6. exits when processing is complete.

## Important scope: risk hints are entitlement-level

The model sees **one entitlement at a time**.

A generated `riskNote` can therefore highlight characteristics such as:

- production write or delete capability;
- financial approval authority;
- system or domain administration;
- bulk-data export;
- other clearly sensitive privileges visible in that entitlement's own metadata.

The utility does **not** currently inspect a user's complete access set and does not perform deterministic segregation-of-duties analysis.

For example, it can explain why *wire initiation* and *wire approval* are individually sensitive, but detecting that one user holds both is a separate policy-analysis capability and is outside this POC.

## Incremental and restartable

The utility selects only entitlements that are still missing a stored description.

That makes it safe to rerun:

- newly added entitlements are picked up;
- already enriched entitlements are skipped;
- failed items remain eligible for a later retry.

## Running it

### Prerequisite

Start the application database from the repository root:

```bash
docker compose up -d
```

The LLM utility is deliberately a separate Compose project and reaches PostgreSQL through the host-published database port.

### Run the batch

```bash
cd llm-utility
docker compose run --rm llm-utility
```

The container exits when the batch completes.

Then refresh:

```text
http://localhost:5173
```

The UI will display the newly stored descriptions.

## Model configuration

The checked-in POC defaults to a local vLLM server running Qwen3.5.

Configuration is controlled with:

| Variable | Default in this POC | Purpose |
|---|---|---|
| `OPENAI_BASE_URL` | `http://192.168.1.251:8000` | OpenAI-compatible server base URL |
| `OPENAI_API_KEY` | `not-needed` | Credential expected by the client; local vLLM does not require real authentication here |
| `OPENAI_MODEL` | `qwen/qwen3.5` | Model identifier exposed by the server |
| `DB_HOST` | `host.docker.internal` in Compose | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |

For the verified local vLLM configuration used by this POC, set `OPENAI_BASE_URL` to the server root, for example:

```text
http://model-host:8000
```

Do not append `/v1` to the value used by this checked-in configuration.

To override the defaults, copy:

```bash
cp .env.example .env
```

and edit the values.

## Provider boundary

The utility uses Spring AI's OpenAI-compatible client, so the enrichment module can be pointed at another compatible endpoint through configuration.

That provider choice is isolated to this batch utility. The runtime frontend and backend remain unchanged.

## Generated data

Each successful enrichment stores:

- `description`
- optional `risk_note`
- `generated_by_model`
- `generated_at`

in `entitlement_descriptions`.

This gives the POC basic generation provenance while keeping the generated text outside the runtime model path.

## Running without Docker

With PostgreSQL accessible locally:

```bash
cd llm-utility
export DB_HOST=localhost
mvn spring-boot:run
```

Override the `OPENAI_*` variables as needed for the model endpoint you want to use.

## Production considerations

A production implementation would normally add governance around generated reference data, including:

- review and approval before publication;
- edit/override capability;
- prompt and model version tracking;
- regeneration policy;
- quality checks;
- audit history;
- deterministic SoD/policy evaluation as a separate capability.

Those concerns are intentionally outside this proof of concept.
