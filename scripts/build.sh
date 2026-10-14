#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf out
mkdir -p out
javac -d out $(find src -name "*.java")
javac -cp out -d out $(find tests -name "*.java")
echo "Build complete. Run: java -cp out Main"
