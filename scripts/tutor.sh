#!/bin/bash
# The tutor's harness on the Mac (docs/TUTOR.md, "Harness"): sign-in, API probes, reading and explanation runs.
# Build it once: ./gradlew :shared:tutorHarness (through the machine's shared lock, docs/SYSTEM.md)
# Then: scripts/tutor.sh login | stav | modely | odhlas | sonda | citanie | vysvetlenie | data
set -e
here="$(cd "$(dirname "$0")/.." && pwd -P)"
lib="${TUTOR_LIB:-$here/shared/build/tutor-harness/lib}"
[ -d "$lib" ] || { echo "Najprv: ./gradlew :shared:tutorHarness" >&2; exit 2; }
java="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21}/bin/java"
# no paid API keys in the harness's environment: it runs only on the ChatGPT sign-in
exec env -u OPENAI_API_KEY -u ANTHROPIC_API_KEY -u GOOGLE_API_KEY -u OPENROUTER_API_KEY \
  "$java" -Djava.awt.headless=true -Xmx3g -Dtutor.repo="$here" -cp "$lib/*" dev.hamper.tabula.tutor.harness.CliKt "$@"
