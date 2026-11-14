#!/usr/bin/env bash
# Builds the API with the JDK only (no Maven, no downloads): out/astra-twin-api.jar
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out/classes && mkdir -p out/classes
javac --release 21 -Xlint:all -Werror -d out/classes $(find src/main/java -name '*.java')
cp -r src/main/resources/. out/classes/
jar --create --file out/astra-twin-api.jar --main-class astratwin.App -C out/classes .
echo "built $(pwd)/out/astra-twin-api.jar"
