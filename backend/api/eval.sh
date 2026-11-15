#!/usr/bin/env bash
# Evaluates the support companion against the local model (needs Ollama: `ollama pull phi3.5`).
# Writes eval/REPORT.md; exit code 1 if any case fails. Options: -Dllm.url=… -Dllm.model=…
set -euo pipefail
cd "$(dirname "$0")"
./build.sh > /dev/null
rm -rf out/test-classes && mkdir -p out/test-classes
javac --release 21 -Xlint:all -Werror -cp out/classes -d out/test-classes $(find src/test/java -name '*.java')
java "$@" -cp out/classes:out/test-classes astratwin.SupportEval eval/support_cases.json eval/REPORT.md
