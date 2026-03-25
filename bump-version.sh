#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TS_PACKAGE_JSON="$ROOT_DIR/ts/package.json"
JAVA_POM="$ROOT_DIR/java/pom.xml"
DRY_RUN=0

usage() {
  cat <<'EOF'
Usage:
  ./bump-version.sh patch [--dry-run]
  ./bump-version.sh minor [--dry-run]
  ./bump-version.sh major [--dry-run]
  ./bump-version.sh set <version> [--dry-run]

Behavior:
  1. Read the current version from ts/package.json
  2. Compute the next version or use the explicit version
  3. Update ts/package.json and java/pom.xml to the same version
  4. Print the recommended next release and git-tag steps

Notes:
  - The script does not create a git tag automatically.
  - Create the tag only after committing the version bump.
EOF
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Missing required command: %s\n' "$1" >&2
    exit 1
  fi
}

read_current_version() {
  node -e "const fs=require('fs'); const pkg=JSON.parse(fs.readFileSync(process.argv[1],'utf8')); process.stdout.write(pkg.version);" \
    "$TS_PACKAGE_JSON"
}

compute_next_version() {
  local mode="$1"
  local current_version="$2"
  node -e '
    const mode = process.argv[1];
    const current = process.argv[2];
    const match = current.match(/^(\d+)\.(\d+)\.(\d+)$/);
    if (!match) {
      console.error(`Unsupported version format: ${current}`);
      process.exit(1);
    }
    let [major, minor, patch] = match.slice(1).map(Number);
    if (mode === "patch") {
      patch += 1;
    } else if (mode === "minor") {
      minor += 1;
      patch = 0;
    } else if (mode === "major") {
      major += 1;
      minor = 0;
      patch = 0;
    } else {
      console.error(`Unsupported bump mode: ${mode}`);
      process.exit(1);
    }
    process.stdout.write(`${major}.${minor}.${patch}`);
  ' "$mode" "$current_version"
}

validate_version() {
  local version="$1"
  if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    printf 'Unsupported version format: %s\n' "$version" >&2
    exit 1
  fi
}

update_ts_version() {
  local new_version="$1"
  node -e '
    const fs = require("fs");
    const file = process.argv[1];
    const version = process.argv[2];
    const pkg = JSON.parse(fs.readFileSync(file, "utf8"));
    pkg.version = version;
    fs.writeFileSync(file, `${JSON.stringify(pkg, null, 2)}\n`);
  ' "$TS_PACKAGE_JSON" "$new_version"
}

update_java_version() {
  local new_version="$1"
  NEW_VERSION="$new_version" node -e '
    const fs = require("fs");
    const file = process.argv[1];
    const version = process.env.NEW_VERSION;
    const source = fs.readFileSync(file, "utf8");
    const updated = source.replace(
      /(<artifactId>numbering-maker-java<\/artifactId>\s*<version>)([^<]+)(<\/version>)/,
      `$1${version}$3`
    );
    if (updated === source) {
      console.error("Failed to update Java version in pom.xml");
      process.exit(1);
    }
    fs.writeFileSync(file, updated);
  ' "$JAVA_POM"
}

MODE="${1:-}"
EXPLICIT_VERSION=""

if [[ -z "$MODE" ]]; then
  usage >&2
  exit 1
fi

case "$MODE" in
  patch|minor|major)
    shift
    ;;
  set)
    EXPLICIT_VERSION="${2:-}"
    if [[ -z "$EXPLICIT_VERSION" ]]; then
      printf 'Missing version for set mode.\n\n' >&2
      usage >&2
      exit 1
    fi
    shift 2
    ;;
  --help|-h)
    usage
    exit 0
    ;;
  *)
    printf 'Unknown mode: %s\n\n' "$MODE" >&2
    usage >&2
    exit 1
    ;;
esac

while [[ $# -gt 0 ]]; do
  case "$1" in
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

CURRENT_VERSION="$(read_current_version)"
if [[ "$MODE" == "set" ]]; then
  NEXT_VERSION="$EXPLICIT_VERSION"
else
  NEXT_VERSION="$(compute_next_version "$MODE" "$CURRENT_VERSION")"
fi
validate_version "$NEXT_VERSION"

printf '[bump-version] current version: %s\n' "$CURRENT_VERSION"
printf '[bump-version] next version: %s\n' "$NEXT_VERSION"

if [[ "$DRY_RUN" -eq 1 ]]; then
  printf '[bump-version] dry-run only, no files changed\n'
else
  update_ts_version "$NEXT_VERSION"
  update_java_version "$NEXT_VERSION"
  printf '[bump-version] updated %s\n' "$TS_PACKAGE_JSON"
  printf '[bump-version] updated %s\n' "$JAVA_POM"
fi

cat <<EOF

Recommended next steps:
  1. Review the version change
  2. Commit it:
     git add ts/package.json java/pom.xml
     git commit -m "chore: bump version to $NEXT_VERSION"
  3. If this commit is the release commit, create an annotated tag after the commit:
     git tag -a v$NEXT_VERSION -m "Release v$NEXT_VERSION"
  4. Publish:
     ./release.sh
     ./publish-npm.sh

Tag guidance:
  - Create a tag when this version will be published or released.
  - Do not create the tag before the version bump commit exists.
  - If you are only testing locally and not releasing yet, skip the tag for now.
EOF
