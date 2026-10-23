# GeyserMCNewExtra

Java Edition features and handy extras for **Bedrock Edition players** on Java Edition servers running
[GeyserMC](https://geysermc.org/). Built and maintained for the **CrossPlayMC** network (Java & Bedrock),
including the public PvP server **CrossPlayMC-SMP**, with a focus on stability and fair PvP between
both editions.

Source code and issues: **https://github.com/Fazerdino1/geysermcnewextra**

> **Status:** `2.1.0-alpha.1`, in development; no build is published yet.
> See [CHANGELOG.md](CHANGELOG.md).

## Supported platforms

Requires **Geyser 2.12.0** (Minecraft Java 26.3 protocol; older servers through ViaVersion) and Java 21.

| Jar | Platform | Notes |
|---|---|---|
| `GeyserMCNewExtra-Spigot.jar` | Paper, Purpur, Spigot with Geyser-Spigot | Primary target (CrossPlayMC runs Purpur + Geyser-Spigot + Floodgate) |
| `GeyserMCNewExtra-Velocity.jar` | Velocity with Geyser-Velocity | Builds, less tested |
| `GeyserMCNewExtra-BungeeCord.jar` | BungeeCord / Waterfall with Geyser-BungeeCord | Builds, less tested |
| `GeyserMCNewExtra-Extension.jar` | `Geyser/extensions` folder (incl. Geyser Standalone) | Builds, less tested. Do **not** use it together with a plugin jar |

[PacketEvents](https://modrinth.com/plugin/packetevents) is optional; some features need it.

## Installing

1. Put the jar for your platform in `plugins/` (or `Geyser/extensions/` for the extension).
2. Start the server once. Settings live in `plugins/GeyserMCNewExtra/config.yml`.

**Coming from GeyserExtras?** Remove its jar. On the first start, GeyserMCNewExtra copies the old
`plugins/GeyserExtras/` folder (config, player preferences, optional packs) into
`plugins/GeyserMCNewExtra/` and renames the two options that carried the old name. The old folder is
kept as a backup and can be deleted afterwards. The main command is now `/geysermcnewextra`
(`/ge` still works).

## Commands

| Command | Aliases | What it does |
|---|---|---|
| `/geysermcnewextra` | `/ge` | Opens the menu (Bedrock players) |
| `/playerlist` | `/l`, `/players`, `/listplayers`, `/tab`, `/tablist` | Java tab list as a Bedrock form |
| `/emotechat` | `/muteemotechat`, `/unmuteemotechat` | Java players: toggle Bedrock emote chat |

Aliases can be turned off with `disabled-command-aliases` (e.g. to free `/l` for a local chat plugin).

## Features

### Combat
- Java Edition combat sounds (critical, sweep, knockback and weak attacks), can be turned off
- The Java-style attack cooldown indicator is provided by Geyser (`cooldown-type` in Geyser's config);
  each player can pick crosshair, hotbar or off, defaulting to the server's setting
### Parity
- Emote chat sent to nearby Java players (muted with `/muteemotechat`)
- Bedrock forms for Java-only menus: player list with tab header/footer, server links,
  advancements, statistics
- Experimental Block Display support using FMBE
- Experimental Bedrock capes shown to Java players (requires PacketEvents; see
  [docs/ANTICHEAT.md](docs/ANTICHEAT.md) before enabling it on a PvP server)
### Utility
- Rebind some Bedrock actions to Java actions (e.g. swap offhand), including **emote shortcuts**:
  link an emote to an action once from the bindings menu
- Optional resource packs (`.mcpack` or `.zip`) players can toggle from `optionalpacks/`
- Save player skin, geometry and animation data to `skins/` (off by default)
- Menu opened by double tapping the inventory button or typing `/ge`
- `debug-mode: true` traces players, forms and shortcuts in the console

## Languages

Every text comes from `plugins/GeyserMCNewExtra/lang/<locale>.yml`: English, French and 10 more
languages are included. Bedrock players see their own client language when a file exists for it;
otherwise, and for the console, `default-locale` is used (French by default). Edit any text in
these files, or add a language by adding a file; texts added by a new version are appended to your
files automatically.

## Documentation

- [docs/TEST-CHECKLIST.md](docs/TEST-CHECKLIST.md): in-game test checklist
- [docs/ANTICHEAT.md](docs/ANTICHEAT.md): anti-cheat friction points and recommended settings
- [docs/ISSUES-STATUS.md](docs/ISSUES-STATUS.md): status of the issues inherited from the original project
- [docs/AUDIT.md](docs/AUDIT.md) and [docs/FORKS-COMPARISON.md](docs/FORKS-COMPARISON.md): code audit and fork survey

## Building

Requires a JVM to launch Gradle; the JDK used for compilation is downloaded automatically.

```bash
./gradlew build
```

Jars are written to `<module>/build/libs/`.

## Credits

GeyserMCNewExtra is based on **GeyserExtras by LetsGoAway** (MIT) and on fixes from several community
forks. The full list of authors and contributors is in **[CREDITS.md](CREDITS.md)**.

## License

MIT, see [LICENSE](LICENSE). The original copyright notice (Copyright (c) 2024 LetsGoAway) is kept as
required by the license.
