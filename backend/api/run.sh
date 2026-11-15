#!/usr/bin/env bash
# Builds if needed and starts the API on http://127.0.0.1:8080 (demo console: /demo/).
# If the frontend has been built (frontend/dist), it is served at / as well.
set -euo pipefail
cd "$(dirname "$0")"
[ -f out/astra-twin-api.jar ] || ./build.sh
ui=()
[ -f ../../frontend/dist/index.html ] && ui=(--ui ../../frontend/dist)
exec java -jar out/astra-twin-api.jar --data ../../data/cache "${ui[@]}" "$@"
