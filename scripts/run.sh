#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [ ! -d out ] || ! find out -name 'Main.class' -print -quit | grep -q .; then
  bash scripts/build.sh
fi
java -cp out Main
