#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_DIR="$ROOT_DIR/java"
GO_DIR="$ROOT_DIR/go"
TS_DIR="$ROOT_DIR/ts"
CACHE_DIR="${CACHE_DIR:-$ROOT_DIR/.cache}"
MAVEN_REPO_DIR="${MAVEN_REPO_DIR:-$CACHE_DIR/m2}"
GO_CACHE_DIR="${GO_CACHE_DIR:-$CACHE_DIR/go-build}"
RELEASE_DIR="${RELEASE_DIR:-$ROOT_DIR/release}"
SKIP_TESTS=0

usage() {
  cat <<'EOF'
Usage: ./release.sh [options]

Options:
  --output <dir>   Release output directory. Default: ./release
  --skip-tests     Skip Java/Go/TS test execution and package only
  --help           Show this message

Behavior:
  1. Verify Java and TypeScript versions are aligned
  2. Run language-specific tests unless --skip-tests is set
  3. Build Java jar, TypeScript npm tarball, and Go source archive
  4. Copy artifacts into the release directory and generate SHA256 checksums
EOF
}

log() {
  printf '[release] %s\n' "$1"
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Missing required command: %s\n' "$1" >&2
    exit 1
  fi
}

read_java_version() {
  sed -n 's:.*<version>\(.*\)</version>.*:\1:p' "$JAVA_DIR/pom.xml" | head -n 1
}

read_ts_version() {
  node -e "const fs = require('fs'); const pkg = JSON.parse(fs.readFileSync(process.argv[1], 'utf8')); process.stdout.write(pkg.version);" \
    "$TS_DIR/package.json"
}

sha256_file() {
  local file="$1"
  if command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$file"
  else
    openssl dgst -sha256 "$file"
  fi
}

package_go_source() {
  local version="$1"
  local output_dir="$2"
  local archive="$output_dir/numbering-maker-go-$version.tar.gz"
  tar -czf "$archive" -C "$ROOT_DIR" go
  printf '%s\n' "$archive"
}

package_ts() {
  local output_dir="$1"
  local tarball
  tarball="$(cd "$TS_DIR" && NPM_CONFIG_CACHE="$CACHE_DIR/npm" npm pack --pack-destination "$output_dir" --loglevel error | tail -n 1)"
  if [[ -z "$tarball" ]]; then
    printf 'failed to produce TypeScript tarball\n' >&2
    exit 1
  fi
  printf '%s/%s\n' "$output_dir" "$tarball"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --output)
      RELEASE_DIR="$2"
      shift 2
      ;;
    --skip-tests)
      SKIP_TESTS=1
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

require_cmd java
require_cmd jar
require_cmd mvn
require_cmd go
require_cmd node
require_cmd npm
require_cmd tar

mkdir -p "$CACHE_DIR" "$MAVEN_REPO_DIR" "$GO_CACHE_DIR" "$CACHE_DIR/npm"
rm -rf "$RELEASE_DIR"
mkdir -p "$RELEASE_DIR/java" "$RELEASE_DIR/go" "$RELEASE_DIR/ts"

JAVA_VERSION="$(read_java_version)"
TS_VERSION="$(read_ts_version)"

if [[ -z "$JAVA_VERSION" || -z "$TS_VERSION" ]]; then
  printf 'Failed to read package versions.\n' >&2
  exit 1
fi

if [[ "$JAVA_VERSION" != "$TS_VERSION" ]]; then
  printf 'Version mismatch: java=%s ts=%s\n' "$JAVA_VERSION" "$TS_VERSION" >&2
  exit 1
fi

VERSION="$JAVA_VERSION"
log "release version: $VERSION"

if [[ "$SKIP_TESTS" -eq 0 ]]; then
  log "running Java tests"
  (cd "$JAVA_DIR" && mvn -Dmaven.repo.local="$MAVEN_REPO_DIR" test)

  log "running Go tests"
  (cd "$GO_DIR" && GOCACHE="$GO_CACHE_DIR" go test ./...)

  log "running TypeScript tests"
  (cd "$TS_DIR" && npm test)
else
  log "skipping tests"
  log "compiling Java sources"
  (cd "$JAVA_DIR" && mvn -Dmaven.repo.local="$MAVEN_REPO_DIR" test-compile)

  log "building TypeScript output"
  (cd "$TS_DIR" && npm run build)
fi

log "packaging Java jar"
JAVA_ARTIFACT="$RELEASE_DIR/java/numbering-maker-java-$VERSION.jar"
rm -f "$JAVA_ARTIFACT"
(cd "$JAVA_DIR" && jar --create --file "$JAVA_ARTIFACT" -C target/classes .)

log "packaging Go source archive"
GO_ARCHIVE="$(package_go_source "$VERSION" "$RELEASE_DIR/go")"

log "packaging TypeScript tarball"
TS_ARCHIVE="$(package_ts "$RELEASE_DIR/ts")"

CHECKSUM_FILE="$RELEASE_DIR/SHA256SUMS"
: > "$CHECKSUM_FILE"
sha256_file "$JAVA_ARTIFACT" >> "$CHECKSUM_FILE"
sha256_file "$GO_ARCHIVE" >> "$CHECKSUM_FILE"
sha256_file "$TS_ARCHIVE" >> "$CHECKSUM_FILE"

cat > "$RELEASE_DIR/manifest.txt" <<EOF
version=$VERSION
java_artifact=$(basename "$JAVA_ARTIFACT")
go_artifact=$(basename "$GO_ARCHIVE")
ts_artifact=$(basename "$TS_ARCHIVE")
EOF

log "artifacts written to $RELEASE_DIR"
log "checksum file: $CHECKSUM_FILE"
