#!/usr/bin/env bash
# Builds if needed and starts the API on http://127.0.0.1:8080 (demo console: /demo/).
set -euo pipefail
cd "$(dirname "$0")"
[ -f out/astra-twin-api.jar ] || ./build.sh
exec java -jar out/astra-twin-api.jar --data ../../data/cache "$@"
