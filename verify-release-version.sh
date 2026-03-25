#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TS_PACKAGE_JSON="$ROOT_DIR/ts/package.json"
JAVA_POM="$ROOT_DIR/java/pom.xml"

usage() {
  cat <<'EOF'
Usage: ./verify-release-version.sh <tag-or-version>

Examples:
  ./verify-release-version.sh v0.1.1
  ./verify-release-version.sh 0.1.1

Behavior:
  - Normalizes `vX.Y.Z` to `X.Y.Z`
  - Reads versions from `ts/package.json` and `java/pom.xml`
  - Fails unless all three versions match exactly
EOF
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Missing required command: %s\n' "$1" >&2
    exit 1
  fi
}

read_ts_version() {
  node -e "const fs=require('fs'); const pkg=JSON.parse(fs.readFileSync(process.argv[1], 'utf8')); process.stdout.write(pkg.version);" \
    "$TS_PACKAGE_JSON"
}

read_java_version() {
  sed -n 's:.*<version>\(.*\)</version>.*:\1:p' "$JAVA_POM" | head -n 1
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" || $# -ne 1 ]]; then
  usage
  if [[ $# -ne 1 ]]; then
    exit 1
  fi
  exit 0
fi

require_cmd node

INPUT_VERSION="$1"
EXPECTED_VERSION="${INPUT_VERSION#v}"

if [[ ! "$EXPECTED_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  printf 'Unsupported tag/version format: %s\n' "$INPUT_VERSION" >&2
  exit 1
fi

TS_VERSION="$(read_ts_version)"
JAVA_VERSION="$(read_java_version)"

if [[ "$TS_VERSION" != "$JAVA_VERSION" ]]; then
  printf 'Version mismatch between ts and java: ts=%s java=%s\n' "$TS_VERSION" "$JAVA_VERSION" >&2
  exit 1
fi

if [[ "$EXPECTED_VERSION" != "$TS_VERSION" ]]; then
  printf 'Tag/version mismatch: expected=%s code=%s\n' "$EXPECTED_VERSION" "$TS_VERSION" >&2
  exit 1
fi

printf 'verified release version %s\n' "$EXPECTED_VERSION"
