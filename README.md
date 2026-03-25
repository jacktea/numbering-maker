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

## Scope

This repository provides pure in-memory numbering libraries only. It does not include DOCX parsing, XML adapters, or `docx4j` integration.
