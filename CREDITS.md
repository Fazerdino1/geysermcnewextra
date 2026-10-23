# Credits

GeyserMCNewExtra is developed for the **CrossPlayMC** network. It would not exist without the people below.
Everything they wrote is used under the MIT license; the original copyright notice is kept in [LICENSE](LICENSE).

## Original project
- **LetsGoAway** ([letsgoawaydev](https://github.com/letsgoawaydev)): author of GeyserExtras, the plugin
  GeyserMCNewExtra is based on (archived on 2026-06-19). Almost every feature here started as their work;
  most of the original commits are signed **Ethan** / **LetsGoAway**.
- The other contributors of the original project, by commit author: **onebeastchris**, **Nogard**
  (resource pack priority, CDN pack detection), **Ilamparithi Murali** (Velocity support),
  **Novampr** and **Auri** (cooldown removal), **MythicalFlame** (Geyser update).
- The original resource pack (combat sounds, ping and menu glyphs) and the language files are still
  downloaded from the original project's repositories.

## Forks we took code or ideas from
| Who | What | Where it went |
|---|---|---|
| **leemwood** ([GeyserPlus](https://github.com/leemwood/GeyserPlus)) | Port to the Geyser 2.11 API | the port to Geyser 2.12 |
| **paper0319** | Delaying chat commands so their forms are not discarded by the Bedrock client | `bedrock-command-delay-ms` |
| **Undecimer** ([OneCraftES/GeyserExtrasPlus](https://github.com/OneCraftES/GeyserExtrasPlus)) | Null guards in the packet translators | every packet translator |
| **AnarchadiaMC** | Default cooldown display taken from Geyser's config | player preferences |
| **h4-hub** | Not deleting packs from Geyser's packs folder | resource pack setup |

Each of these changes says so in its commit message.

## Projects we build on
- [GeyserMC](https://github.com/GeyserMC/Geyser) and [Floodgate](https://github.com/GeyserMC/Floodgate),
  which make Bedrock players on Java servers possible.
- [PacketEvents](https://github.com/retrooper/packetevents), [Configurate](https://github.com/SpongePowered/Configurate),
  [Cumulus](https://github.com/GeyserMC/Cumulus) and [Jackson](https://github.com/FasterXML/jackson).
- The emote chat messages come from data extracted from Minecraft: Bedrock Edition.

## CrossPlayMC
- **CrossPlayMC** team: maintenance, testing on the CrossPlayMC-SMP server, French translation.
