# GeyserExtras CPMC

A maintained fork of [GeyserExtras](https://github.com/GeyserExtras/GeyserExtras) for the
**CrossPlayMC** network (Java & Bedrock), including the public PvP server **CrossPlayMC-SMP**.

GeyserExtras adds Java Edition features and handy extras for Bedrock Edition players joining
Java Edition servers through [GeyserMC](https://geysermc.org/). The original project was archived
on 2026-06-19; this fork keeps it working on current Geyser releases, with a focus on stability
and fair PvP between both editions.

> **Status:** `2.1.0-alpha.1`, in development. The port to the latest Geyser is not finished yet,
> so no build is published. See [CHANGELOG.md](CHANGELOG.md) and [docs/AUDIT.md](docs/AUDIT.md).

## Drop-in replacement

The fork keeps every technical identifier of GeyserExtras, so it can replace an existing install
without touching configs or player data:

- plugin / extension id: `geyserextras` (plugin name `GeyserExtras`)
- commands: `/geyserextras` (`/ge`), `/platformlist`, `/playerlist`, `/emotechat`
- data folder: `plugins/GeyserExtras/` (or `Geyser/extensions/geyserextras/`) with the same
  `config.yml`, `preferences/`, `optionalpacks/`, `skins/` layout

Only the jar names change (`GeyserExtras-CPMC-Spigot.jar`, …): remove the old
`GeyserExtras-*.jar` before adding the new one.

## Supported platforms

| Jar | Platform | Notes |
|---|---|---|
| `GeyserExtras-CPMC-Spigot.jar` | Paper, Purpur, Spigot with Geyser-Spigot | Primary target (CrossPlayMC runs Purpur + Geyser-Spigot + Floodgate) |
| `GeyserExtras-CPMC-Velocity.jar` | Velocity with Geyser-Velocity | Builds, less tested |
| `GeyserExtras-CPMC-BungeeCord.jar` | BungeeCord / Waterfall with Geyser-BungeeCord | Builds, less tested |
| `GeyserExtras-CPMC-Extension.jar` | `Geyser/extensions` folder (incl. Geyser Standalone) | Builds, less tested |

[PacketEvents](https://modrinth.com/plugin/packetevents) is optional; some features need it.

## Features

### Combat
- Java Edition combat sounds (critical, sweep, knockback and weak attacks)
- The Java-style attack cooldown indicator is now provided by Geyser itself
  (`cooldown-type` in Geyser's config); each player can still pick crosshair, hotbar or off
### Parity
- Emote chat sent to nearby Java players (can be muted with `/muteemotechat`)
- Bedrock forms for Java-only menus (player list with tab header/footer, server links,
  advancements, statistics)
- Experimental Block Display support using FMBE
- Experimental Bedrock capes shown to Java players (requires PacketEvents)
### Utility
- Rebind some Bedrock actions to Java actions (e.g. swap offhand)
- Optional resource packs players can toggle from `GeyserExtras/optionalpacks`
- Save player skin, geometry and animation data to `GeyserExtras/skins`
- Menu opened by double tapping the inventory button or typing `/ge`

## Building

Requires a JVM to launch Gradle; the JDK used for compilation is downloaded automatically by the
Gradle toolchain resolver.

```bash
./gradlew build
```

Jars are written to `<module>/build/libs/`.

## Credits

- **[LetsGoAway (letsgoawaydev)](https://github.com/letsgoawaydev)**: original author of
  GeyserExtras, and the project's other contributors
  ([GeyserExtras/GeyserExtras](https://github.com/GeyserExtras/GeyserExtras)).
- **[leemwood/GeyserPlus](https://github.com/leemwood/GeyserPlus)**: community fork whose port to
  Geyser 2.11.1 / Minecraft 26.x was used as a reference for the API changes in this fork.
  Commits that build on it say so.
- [GeyserMC](https://github.com/GeyserMC/Geyser), on which everything here depends.

## License

MIT, see [LICENSE](LICENSE). The original copyright notice (Copyright (c) 2024 LetsGoAway) is kept
as required by the license.
