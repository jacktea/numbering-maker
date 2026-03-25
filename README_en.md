# numbering-maker

Multi-language numbering generator implementations for Java, Go, and TypeScript.

## Supported Formats

The three implementations align on the same core formats:

- `decimal`
- `upperLetter`
- `lowerLetter`
- `upperRoman`
- `lowerRoman`
- `ordinal`
- `bullet`
- `chineseSimplified`
- `chineseTraditional`

## Repository Layout

- `java/`: Maven-based Java implementation with JUnit 5 tests
- `go/`: Go module with `go test`
- `ts/`: ESM-only TypeScript package compiled by `tsc`

## Core Behavior

- `NumberingMaker` manages `numId`, `multiLevelType`, and `levels`.
- `generateNumbering(level)` returns an empty string when the requested level is out of range.
- When a parent level changes, all following levels are reset to their initial state.
- `getState()` and `setState()` support paging or checkpoint restore.
- `suffix` is kept as level metadata for future adapters; current output is driven by `pattern`.

## Shared Example

All three implementations follow the same progression:

```text
level 0 -> 1.
level 1 -> 1.1.
level 1 -> 1.2.
level 2 -> 1.2.1.
level 2 -> 1.2.2.
```

## Quick Start

### Java

```bash
cd java
mvn test
```

### Go

```bash
cd go
go test ./...
```

### TypeScript

```bash
cd ts
npm test
```

## Version Bump

Use the root script to bump Java and TypeScript versions together:

```bash
./bump-version.sh patch
```

Other forms:

- `./bump-version.sh minor`
- `./bump-version.sh major`
- `./bump-version.sh set 1.2.3`
- `./bump-version.sh patch --dry-run`

Tag policy:

- Create a git tag when the bumped version is actually being released
- Create the tag only after committing the version bump
- If you are only validating locally, skip the tag until the release commit is ready

## Unified Release

Run the root release script to verify all three packages and collect artifacts:

```bash
./release.sh
```

Options:

- `--output <dir>`: write artifacts to a custom directory
- `--skip-tests`: package existing sources without rerunning tests

Generated artifacts:

- `release/java/numbering-maker-java-<version>.jar`
- `release/go/numbering-maker-go-<version>.tar.gz`
- `release/ts/jacktea-numbering-maker-<version>.tgz`
- `release/SHA256SUMS`
- `release/manifest.txt`

## npm Publish

Publish the TypeScript package in `ts/` to npmjs:

```bash
./publish-npm.sh
```

Useful options:

- `--dry-run`: validate the publish payload without uploading
- `--skip-tests`: publish after `npm run build` only

The script publishes to `https://registry.npmjs.org/` with public access and uses a local npm cache under `.cache/npm`.

For CI or prebuilt tarballs, it also supports:

- `--skip-auth`: skip `npm whoami` verification
- `--tarball <file>`: publish a prebuilt `.tgz` file

## GitHub Tag Release

This repository can be released by pushing a tag in the form `vX.Y.Z`, or by manually dispatching the release workflow against an existing tag.

Workflow file:

- `.github/workflows/release.yml`

What the workflow does:

- validates that the pushed tag version matches `ts/package.json` and `java/pom.xml`
- runs the unified build via `./release.sh`
- creates a GitHub Release and uploads:
  - Java jar
  - Go source archive
  - TypeScript npm tarball
  - `SHA256SUMS`
  - `manifest.txt`
- publishes `ts/` to npmjs through npm Trusted Publishing

Trigger modes:

- push trigger: runs automatically when `vX.Y.Z` is pushed
- manual trigger: run `Release` from GitHub Actions UI and provide `release_tag=vX.Y.Z`

One-time setup:

1. Create the GitHub repository and configure `origin`
2. Push the repository content, including `.github/workflows/release.yml`
3. On npmjs, open the package settings for `@jacktea/numbering-maker`
4. Add a Trusted Publisher:
   `GitHub user/org` + `repository` + `workflow filename=release.yml`
5. Use a GitHub-hosted runner
6. After the first successful publish, optionally tighten npm package security to require 2FA and disallow classic tokens

Per-release local steps:

```bash
./bump-version.sh patch
git add ts/package.json java/pom.xml
git commit -m "chore: bump version to X.Y.Z"
git tag -a vX.Y.Z -m "Release vX.Y.Z"
git push origin <branch> --follow-tags
```

Manual rerun flow:

1. Open GitHub Actions
2. Choose the `Release` workflow
3. Click `Run workflow`
4. Enter an existing tag like `v0.1.1`
5. Run it against that tag

Important rules:

- the tag must be created after the version bump commit
- the tag must match the code version exactly
- the workflow is designed for public npm publishing through OIDC, not long-lived `NPM_TOKEN`

## Scope

This repository provides pure in-memory numbering libraries only.
