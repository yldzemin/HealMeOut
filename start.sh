#!/usr/bin/env bash
# One command to run ASTRA-TWIN from the repository (macOS, Linux, Git Bash on Windows).
# Builds what is missing or out of date, then starts the app on http://127.0.0.1:8080/.
# Stop with Ctrl+C. Options pass through to the server, e.g.  ./start.sh --port 8081
set -euo pipefail
cd "$(dirname "$0")"

need() {
  command -v "$1" >/dev/null 2>&1 || { echo "Missing: $1. $2"; exit 1; }
}
JAVA_URL="https://adoptium.net/temurin/releases/?version=21"
need java "Install the Java JDK 21 or newer: $JAVA_URL"
need javac "Install the Java JDK (not only the JRE), 21 or newer: $JAVA_URL"
need node "Install Node.js 22 LTS or newer: https://nodejs.org"
need npm "Install Node.js 22 LTS or newer (npm comes with it): https://nodejs.org"
major=$(java -version 2>&1 | head -1 | sed -E 's/.*version "([0-9]+).*/\1/')
if ! [ "$major" -ge 21 ] 2>/dev/null; then
  echo "Java $major found; ASTRA-TWIN needs Java 21 or newer: $JAVA_URL"
  exit 1
fi

# Screens: install the libraries when they are missing or changed; rebuild when the frontend changed.
if [ ! -f frontend/node_modules/.package-lock.json ] || [ frontend/package-lock.json -nt frontend/node_modules/.package-lock.json ]; then
  echo "== Installing the screen libraries (first run: a few minutes) =="
  (cd frontend && npm ci --no-audit --no-fund)
fi
if [ ! -f frontend/dist/index.html ] || [ -n "$(find frontend/src frontend/public frontend/index.html frontend/package.json \
    -newer frontend/dist/index.html -print -quit)" ]; then
  echo "== Building the screens =="
  (cd frontend && npm run build)
fi

# Server: always rebuilt (a few seconds), so it never runs old code.
echo "== Building the server =="
backend/api/build.sh

echo
echo "== Starting. Open the address on the 'Frontend:' line below (normally http://127.0.0.1:8080/). Stop with Ctrl+C. =="
exec backend/api/run.sh "$@"
