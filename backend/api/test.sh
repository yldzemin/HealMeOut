#!/usr/bin/env bash
# Compiles and runs the tests with the JDK only. Exit code 1 if any test fails.
set -euo pipefail
cd "$(dirname "$0")"
./build.sh > /dev/null
rm -rf out/test-classes && mkdir -p out/test-classes
javac --release 21 -Xlint:all -Werror -cp out/classes -d out/test-classes $(find src/test/java -name '*.java')
java -ea -cp out/classes:out/test-classes astratwin.TestRunner ../../data/cache
