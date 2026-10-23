# Changelog

All notable changes to GeyserExtras CPMC are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Versions continue from the upstream GeyserExtras `2.0.0-BETA-11` (last upstream release).

## [Unreleased]

### Added
- Phase 0 audit of the codebase, open upstream issues and Geyser API changes (`docs/AUDIT.md`).
- Gradle downloads the required JDK automatically (foojay toolchain resolver).
- Build check that fails when the code version and the Gradle version differ.
- `CHANGELOG.md`.

### Changed
- Project renamed to **GeyserExtras CPMC** (display name, authors, descriptions, README).
  The id `geyserextras`, plugin name `GeyserExtras`, commands and data folder are unchanged.
- Jars renamed to `GeyserExtras-CPMC*.jar`.
- Single version number `2.1.0-alpha.1` everywhere (plugin descriptors used to say `2.0.0`
  while the code said `2.0.0-BETA-11`).

### Fixed
- The update checker no longer compares the fork with the archived upstream Modrinth project,
  which would always have reported a bogus update. It is skipped until the fork has releases.

### Known issues
- The code does not compile yet against any published Geyser version: the last upstream commit
  already uses the Geyser 2.11 API while its dependencies were still on 2.9. The port to the
  latest Geyser is the next step.
