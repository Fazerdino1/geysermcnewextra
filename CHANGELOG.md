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
- JUnit 5 tests for the core module.
- `docs/ANTICHEAT.md` (anti-cheat friction points) and `docs/TEST-CHECKLIST.md` (in-game tests).

### Changed
- Project renamed to **GeyserExtras CPMC** (display name, authors, descriptions, README).
  The id `geyserextras`, plugin name `GeyserExtras`, commands and data folder are unchanged.
- Jars renamed to `GeyserExtras-CPMC*.jar`.
- Single version number `2.1.0-alpha.1` everywhere (plugin descriptors used to say `2.0.0`
  while the code said `2.0.0-BETA-11`).
- **Requires Geyser 2.12.0** (pinned to build #1269) and Java 21; Java Edition 26.3 protocol
  (older servers through ViaVersion). Port of the 2.11 API changes based on
  [leemwood/GeyserPlus](https://github.com/leemwood/GeyserPlus).
- Only the four Java combat sounds are replaced; every other sound is translated by Geyser again.

### Fixed
- Emote chat to Java players works again on Geyser 2.11+ (Geyser no longer forwards the emote list).
- Cooldown preferences saved before Geyser 2.11 (`TITLE`, `ACTIONBAR`) are migrated instead of
  becoming null.
- Skin saving no longer decodes skin data twice (would fail on join with Geyser 2.11+).
- Opening the inventory while riding a nautilus (upstream #62).
- Touch players no longer see their arm swing twice on a missed attack.
- The update checker no longer compares the fork with the archived upstream Modrinth project,
  which would always have reported a bogus update. It is skipped until the fork has releases.

### Known issues
- Emote slot bindings (Emote 1–4 → action) cannot work: Geyser 2.11+ discards the client's emote
  list, so the slot of an emote is unknown.
- Bugs listed in `docs/AUDIT.md` (startup order, network-dependent startup, mannequin capes and
  anti-cheats, reconnect on kick, …) are scheduled for the reliability phase.
