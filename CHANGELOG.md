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

- Emote bindings now follow a recorded emote: tick "Link an emote" in the bindings menu, save,
  then perform the emote once (Geyser 2.11+ no longer exposes emote wheel slots).
- Translations bundled in the jar for strings added by the fork (English, French).
- Players' default cooldown display follows Geyser's `cooldown-type`.
- Config option `bedrock-command-delay-ms` (config version 6, migrated automatically).
- Debug traces (`debug-mode: true`) for player creation, forms, inventory shortcuts and emotes.

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

### Security
- Skin saving no longer uses client-chosen skin/cape ids as paths (a modified client could write
  files outside the plugin folder) and rejects oversized or truncated images.

### Fixed
- `/geyserextras` typed in chat by a Bedrock player showed nothing (forms arriving while the chat is
  open are discarded); commands now run after `bedrock-command-delay-ms` (default 500).
- Packets for a player GeyserExtras does not track yet are handled by Geyser instead of failing
  (`Could not translate packet`, upstream #36, #54); player maps exist before any packet (#29, #76).
- Config version mismatch: configs were rewritten on every start or kept removed options.
- Disabling the plugin never throws, even after a failed start (upstream #47).
- A null dimension no longer throws on every tick (upstream #67); a failing player tick is logged
  once and no longer stops other players' ticks.
- Disconnected players stop ticking on Velocity, Bungee and the extension (tasks were never
  cancelled: likely cause of the proxy memory leak in upstream #32).
- Velocity: a player kicked from a backend server no longer reconnects every Bedrock player.
- GeyserExtras no longer deletes packs from Geyser's packs folder at startup; it only warns.
- Emote chat to Java players works again on Geyser 2.11+ (Geyser no longer forwards the emote list).
- Cooldown preferences saved before Geyser 2.11 (`TITLE`, `ACTIONBAR`) are migrated instead of
  becoming null.
- Skin saving no longer decodes skin data twice (would fail on join with Geyser 2.11+).
- Opening the inventory while riding a nautilus (upstream #62).
- Touch players no longer see their arm swing twice on a missed attack.
- A binding or optional pack chosen by a player without a preferences file applied to every such
  player and was never saved (preferences shared their collections with the defaults).
- The block display workaround crashed GeyserExtras' startup on Geyser 2.12 (entity registry frozen).
- Inventory double-tap detection was random (timestamp stored in a float).
- The build no longer produces an empty `GeyserExtras-CPMC-<version>.jar` at the project root.
- The update checker no longer compares the fork with the archived upstream Modrinth project,
  which would always have reported a bogus update. It is skipped until the fork has releases.

### Known issues
- Bugs listed in `docs/AUDIT.md` (startup order, network-dependent startup, mannequin capes and
  anti-cheats, reconnect on kick, …) are scheduled for the reliability phase.
