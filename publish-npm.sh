#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TS_DIR="$ROOT_DIR/ts"
CACHE_DIR="${CACHE_DIR:-$ROOT_DIR/.cache}"
NPM_CACHE_DIR="${NPM_CACHE_DIR:-$CACHE_DIR/npm}"
REGISTRY_URL="https://registry.npmjs.org/"
SKIP_TESTS=0
DRY_RUN=0
SKIP_AUTH_CHECK=0
PACKAGE_TARBALL=""

usage() {
  cat <<'EOF'
Usage: ./publish-npm.sh [options]

Options:
  --skip-tests   Skip `npm test` before publish
  --dry-run      Run `npm publish --dry-run` without uploading
  --skip-auth    Skip `npm whoami` verification before publish
  --tarball <file> Publish a prebuilt `.tgz` tarball instead of publishing from `ts/`
  --help         Show this message

Behavior:
  1. Build and optionally test the TypeScript package, unless `--tarball` is used
  2. Verify npmjs authentication unless `--dry-run` or CI OIDC mode is used
  3. Publish `ts/` or the provided tarball to npmjs with public access
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

publish_target() {
  if [[ -n "$PACKAGE_TARBALL" ]]; then
    printf '%s\n' "$PACKAGE_TARBALL"
  else
    printf '.\n'
  fi
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
    --skip-auth)
      SKIP_AUTH_CHECK=1
      shift
      ;;
    --tarball)
      PACKAGE_TARBALL="$2"
      shift 2
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

if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
  SKIP_AUTH_CHECK=1
fi

if [[ -n "$PACKAGE_TARBALL" && ! -f "$PACKAGE_TARBALL" ]]; then
  printf 'Tarball not found: %s\n' "$PACKAGE_TARBALL" >&2
  exit 1
fi

if [[ -z "$PACKAGE_TARBALL" && "$SKIP_TESTS" -eq 0 ]]; then
  log "running TypeScript tests"
  run_npm test
elif [[ -z "$PACKAGE_TARBALL" ]]; then
  log "skipping tests"
  log "building TypeScript output"
  run_npm run build
else
  log "using prebuilt tarball $PACKAGE_TARBALL"
fi

if [[ "$DRY_RUN" -eq 0 && "$SKIP_AUTH_CHECK" -eq 0 ]]; then
  log "verifying npmjs authentication"
  run_npm whoami --registry "$REGISTRY_URL" >/dev/null
fi

if [[ "$DRY_RUN" -eq 1 ]]; then
  log "running npm publish dry-run"
  if [[ -n "$PACKAGE_TARBALL" ]]; then
    NPM_CONFIG_CACHE="$NPM_CACHE_DIR" npm publish "$PACKAGE_TARBALL" --access public --registry "$REGISTRY_URL" --dry-run
  else
    run_npm publish "$(publish_target)" --access public --registry "$REGISTRY_URL" --dry-run
  fi
else
  log "publishing package to npmjs"
  if [[ -n "$PACKAGE_TARBALL" ]]; then
    NPM_CONFIG_CACHE="$NPM_CACHE_DIR" npm publish "$PACKAGE_TARBALL" --access public --registry "$REGISTRY_URL"
  else
    run_npm publish "$(publish_target)" --access public --registry "$REGISTRY_URL"
  fi
fi
