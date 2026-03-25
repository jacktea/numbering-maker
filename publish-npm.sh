#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TS_DIR="$ROOT_DIR/ts"
CACHE_DIR="${CACHE_DIR:-$ROOT_DIR/.cache}"
NPM_CACHE_DIR="${NPM_CACHE_DIR:-$CACHE_DIR/npm}"
REGISTRY_URL="https://registry.npmjs.org/"
SKIP_TESTS=0
DRY_RUN=0

usage() {
  cat <<'EOF'
Usage: ./publish-npm.sh [options]

Options:
  --skip-tests   Skip `npm test` before publish
  --dry-run      Run `npm publish --dry-run` without uploading
  --help         Show this message

Behavior:
  1. Build and optionally test the TypeScript package
  2. Verify npmjs authentication unless --dry-run is used
  3. Publish `ts/` to npmjs with public access
EOF
}

log() {
  printf '[publish-npm] %s\n' "$1"
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Missing required command: %s\n' "$1" >&2
    exit 1
  fi
}

run_npm() {
  (cd "$TS_DIR" && NPM_CONFIG_CACHE="$NPM_CACHE_DIR" npm "$@")
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-tests)
      SKIP_TESTS=1
      shift
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      printf 'Unknown option: %s\n\n' "$1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

require_cmd node
require_cmd npm
mkdir -p "$NPM_CACHE_DIR"

if [[ "$SKIP_TESTS" -eq 0 ]]; then
  log "running TypeScript tests"
  run_npm test
else
  log "skipping tests"
  log "building TypeScript output"
  run_npm run build
fi

if [[ "$DRY_RUN" -eq 0 ]]; then
  log "verifying npmjs authentication"
  run_npm whoami --registry "$REGISTRY_URL" >/dev/null
fi

if [[ "$DRY_RUN" -eq 1 ]]; then
  log "running npm publish dry-run"
  run_npm publish --access public --registry "$REGISTRY_URL" --dry-run
else
  log "publishing package to npmjs"
  run_npm publish --access public --registry "$REGISTRY_URL"
fi
