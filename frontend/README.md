# ASTRA-TWIN — Frontend

Tauri v2 desktop app with React + TypeScript + Vite, Tailwind CSS 4, shadcn/ui, Motion and Recharts.
Decisions and screens: [`../docs/FRONTEND.md`](../docs/FRONTEND.md).

## Prerequisites

Node.js, Rust (rustup) and the Tauri system packages for your OS: see `docs/FRONTEND.md` §1.1.

## Run

```sh
npm install
npm run tauri dev      # desktop window with hot reload
npm run tauri build    # installer for the current OS (output in src-tauri/target, not committed)
npm run build          # web build only (plan B: served by the Java API on localhost)
```

Data comes from `src/mock/*.json` until the backend is ready (`USE_MOCK` in `src/api/client.ts`).
