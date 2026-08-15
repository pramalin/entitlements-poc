# Entitlements POC — Frontend

**Stack:** React 19.2.8 · Vite 8.2.1 (verified against npm - actually installed, built,
and dev-server-started in the environment this was generated in, unlike the backend
which couldn't be compiled here due to no Maven Central access)

## Prerequisites
- Node.js (18+ recommended)
- The backend running on `http://localhost:8080` (step 2) - CORS is locked to exactly
  `http://localhost:5173`, which is why `vite.config.js` pins that port with
  `strictPort: true` rather than letting Vite pick a different free port.
- Postgres running (step 1)

## Run it
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`. You should see the user list on the left; clicking a user
loads their access list on the right. Hovering over a cryptic access title shows its
description - which will say *"No description generated yet for this entitlement"* for
every row right now, since `entitlement_descriptions` is still empty until step 4's LLM
utility runs.

## What's here
- `src/App.jsx` - user list + access table, two effects: load users on mount, load a
  user's access whenever the selection changes.
- `src/Tooltip.jsx` - hover tooltip that follows the cursor; falls back to a "no
  description yet" message rather than showing nothing, so it's obvious the feature is
  wired up correctly even before step 4 populates real descriptions.
- `src/api.js` - thin fetch wrapper. `VITE_API_BASE` env var overrides the default
  `http://localhost:8080/api` if you ever need to point it elsewhere.
- `src/index.css` - same restrained navy/steel/teal palette as the original static
  mockup, monospace for the cryptic titles specifically (visually signals "this is a raw
  system identifier, not prose") with a dashed underline as the hover affordance.

## Verified in this environment
Unlike the Spring Boot backend, this sandbox *does* have npm registry access, so this
was actually installed, built (`npm run build` - clean, ~345ms), and dev-server-started
(`npm run dev` - came up on :5173 as expected) before being handed off. Should be lower
risk than step 2 was.
