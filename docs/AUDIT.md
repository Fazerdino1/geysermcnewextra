# Audit GeyserExtras — Phase 0 (état des lieux)

> Date : 2026-10-23 · Base auditée : `GeyserExtras/GeyserExtras@d49ad51` (dernier commit, 2026-03-14, dépôt archivé)
> Référence : `leemwood/GeyserPlus@ad2b4c2` (v2.1.0, 2026-08-09)
> Aucune modification de code n'a été faite. Rien n'a été compilé : **aucun JDK n'est installé sur la machine** (voir §8).

Légende : ⚔️ = impact PvP · 🔴 critique · 🟠 important · 🟡 mineur · ❓ à vérifier (compilation ou test en jeu)

---

## 1. Architecture

### 1.1 Modules Gradle (`settings.gradle.kts`)

| Module | Rôle | Point d'entrée | Descripteur | Jar produit |
|---|---|---|---|---|
| `core` | Toute la logique (injecteurs de paquets, menus, prefs, cache, packs) | `core/GeyserExtras.java` (constructeur) | — | `GeyserExtras.jar` |
| `spigot` | Plugin Paper/Spigot/Purpur | `spigot/GeyserExtrasSpigot.java` (`JavaPlugin`) | `plugin.yml` (`depend: Geyser-Spigot`, `softdepend: packetevents`, `api-version: 1.16`) | `GeyserExtras-Spigot.jar` |
| `velocity` | Plugin Velocity | `velocity/GeyserExtrasVelocity.java` (`@Plugin`) | annotation processor Velocity | `GeyserExtras-Velocity.jar` |
| `bungee` | Plugin BungeeCord/Waterfall | `bungee/GeyserExtrasBungee.java` | `bungee.yml` (`depends: Geyser-BungeeCord`) | `GeyserExtras-BungeeCord.jar` |
| `extension` | Extension Geyser (dossier `Geyser/extensions`, Standalone ou n'importe quelle plateforme) | `extension/GeyserExtrasExtension.java` (`@Subscribe GeyserPostInitializeEvent`) | `extension.yml` (`api: 2.9.0`) | `GeyserExtras-Extension.jar` |

Chaque module plateforme fait `implementation(project(":core"))` et embarque le core via Shadow. L'interface `dev.letsgoaway.geyserextras.Server` est l'abstraction plateforme (log, dossier, envoi de messages, `TickUtil`, `createPlayer`).

Identifiants techniques actuels (à **conserver** pour la compatibilité, règle n°3) :
- id : `geyserextras` (injecté par `processResources` dans `build.gradle.kts`), nom affiché : `GeyserExtras`
- commandes : `/geyserextras` (alias `/ge`), `/platformlist` (`/pl`, `/platforms`), `/playerlist` (`/l`, `/players`, `/listplayers`, `/tab`, `/tablist`), `/emotechat` (`/muteemotechat`, `/unmuteemotechat`)
- dossier de données : `plugins/GeyserExtras/` (ou `Geyser/extensions/geyserextras/`) — `config.yml`, `preferences/<xuid>.json`, `preferences/default.json`, `preferences/java/`, `cache/`, `optionalpacks/`, `skins/`, `capes/`
- package Java : `dev.letsgoaway.geyserextras.*`

### 1.2 Cycle de vie

1. La plateforme construit `new GeyserExtras(server)` (Spigot : `onEnable` ; extension : `GeyserPostInitializeEvent`).
2. `IsAvailable.preload()` détecte Floodgate / Cloudburst / Adventure / PacketEvents par réflexion.
3. `ConfigLoader.load()` (Configurate YAML, migrations versionnées).
4. `GeyserHandler.register()` remplace ~20 translators internes de Geyser dans `Registries.JAVA_PACKET_TRANSLATORS` / `BEDROCK_PACKET_TRANSLATORS` (extension : immédiatement ; plugin : au `GeyserPostInitializeEvent`).
5. `Cache.initialize()` **télécharge** au démarrage : `credits.txt` et les langues depuis `raw.githubusercontent.com/GeyserExtras/data`, le `GeyserExtrasPack.mcpack` depuis `GeyserExtras/GeyserExtrasPack` (dépôt **archivé**), et interroge l'API GitHub pour les dates de mise à jour.
6. Abonnements aux événements Geyser : `SessionLoadResourcePacksEvent` (crée l'`ExtrasPlayer`), `SessionJoinEvent`, `SessionDisconnectEvent`, `ClientEmoteEvent`, `GeyserPreReloadEvent`, `GeyserShutdownEvent`.
7. Tick : Spigot = tâche synchrone chaque tick → `ExtrasPlayer.tick()` ; Velocity/Bungee/extension = tâche par joueur selon le tickrate.
8. PacketEvents (si présent, Spigot/Velocity/Bungee) : `ProtocolHandler` écoute les paquets **des joueurs Java** (contournement des capes).

### 1.3 Dépendances et versions ciblées

| Élément | Valeur actuelle (upstream) | GeyserPlus | Dernière stable |
|---|---|---|---|
| Gradle wrapper | 8.12 | 8.12 | — |
| Plugins Gradle | lombok 8.6, shadow 8.3.0 | idem | — |
| Toolchain Java | **17** | 21 | Geyser compile en **21** depuis 2.10 |
| `geyser:core` (compileOnly) | `2.9.1-SNAPSHOT` (root/extension) **mais** `2.9.0-SNAPSHOT` (core/spigot/velocity/bungee) — incohérent | `2.11.1-SNAPSHOT` partout | **2.12.0** (build #1269, 2026-10-22) |
| `extension.yml` `api` | 2.9.0 | 2.11.0 | 2.12.0 |
| Floodgate | `core:2.2.5-SNAPSHOT` | idem | idem |
| PacketEvents | 2.10.0 | idem | ❓ à vérifier |
| Configurate | `4.2.0-GeyserMC-SNAPSHOT` (shadé + relocalisé) | idem | Geyser utilise `4.2.0-GeyserMC-20251111…` |
| Jackson | 2.17.0 (shadé + relocalisé) | idem | — |
| Spigot API | 1.16.5 | idem | — |
| Velocity API | 3.4.0-SNAPSHOT | idem | — |
| Minecraft | 1.21.x | Java 26.2 / Bedrock 26.0–26.40 | **Java 26.3 / Bedrock 26.30 → 26.52** |

Problèmes de build constatés à la lecture :
- Le `build.gradle.kts` racine applique `java` + dépendances au projet racine alors qu'il ne contient pas de sources ; les dépôts Maven sont dupliqués (dont `oss.sonatype.org`, en fin de vie) ; dépendances déclarées en double `implementation` + `shadow`.
- Pas de `src/test` : **aucun test** dans le projet.
- `gradle.properties` contient des propriétés Fabric/NeoForge/Architectury inutilisées (vestiges d'un port abandonné).
- Relocation d'Adventure dans `spigot` vers le package shadé de Geyser-Spigot : fragile, c'est l'origine des `LinkageError` (#56, #69, #13).

### 1.4 Mécanisme d'injection (point le plus fragile)

Tout le module `core/injectors/` **étend des classes internes de Geyser** (`org.geysermc.geyser.translator.protocol.*`, pas l'API publique) et se réenregistre à la place des translators d'origine. Conséquences :
- chaque refonte interne de Geyser casse le plugin (c'est ce qui a tué l'upstream : issues #47, #63, #69, #70, #72, #74) ;
- quand un injecteur **n'appelle pas** `super.translate(...)`, toute correction future de Geyser sur ce paquet est perdue (voir §5, B7) ;
- une exception dans un injecteur fait échouer la traduction du paquet entier → le paquet est perdu (« Could not translate packet »), ce qui provoque des désynchronisations.

---

## 2. Fonctionnalités et fichiers

Chemins relatifs à `core/src/main/java/dev/letsgoaway/geyserextras/` sauf mention contraire.

| # | Fonctionnalité | Fichiers principaux | Toggle config | PvP |
|---|---|---|---|---|
| F1 | **Sons de combat Java** (crit, sweep, knockback, weak) remplacés par les sons du GeyserExtrasPack | `core/parity/java/combat/SoundReplacer.java`, `core/injectors/java/JavaSoundInjector.java`, `JavaSoundEntityInjector.java` | `enableJavaCombatSounds` (global) | ⚔️ feedback audio des coups |
| F2 | **Indicateur de cooldown d'attaque** : *supprimé de GeyserExtras* (commits `8bde34d`, `66094e0`, `274d4bb`), désormais géré par Geyser (`gameplay.cooldown-type`) + GeyserIntegratedPack. GE ne garde que le choix par joueur (CROSSHAIR / HOTBAR / DISABLED) | `core/menus/settings/sections/VideoSection.java`, `core/preferences/PreferencesData.java` (`cooldownType`) | Geyser (global) | ⚔️ visuel uniquement, le serveur applique le vrai cooldown |
| F3 | **Rebinding d'actions** : emotes 1-4, Inventaire, Sneak+Inventaire, Pick Block, Sneak+Drop, Paramètres → swap offhand, menu GE, liste joueurs, succès, stats, tooltips, perspective, reconnexion… | `core/preferences/bindings/Action.java`, `Remappable.java`, `core/injectors/bedrock/BedrockInteractInjector.java`, `BedrockBlockPickRequestInjector.java`, `BedrockEntityPickRequestInjector.java`, `input/BedrockBlockInteractions.java`, `core/ExtrasPlayer.java#onEmoteEvent` | ❌ aucun toggle global (seulement via `enableGeyserExtrasMenu`) | ⚔️ **swap offhand** = `session.requestOffhandSwap()` (paquet Java légitime, équivalent touche F) |
| F4 | Double-tap inventaire → menu GE | `BedrockInteractInjector.java` | pref joueur `enableDoubleClickShortcut` | ⚔️ retarde l'ouverture d'inventaire de `doubleClickMS + 20` ms (200 ms par défaut) |
| F5 | Menu GE (`/ge`) et menu Paramètres injecté dans les réglages Bedrock | `core/menus/**`, `core/form/**`, `BedrockServerSettingsRequestInjector.java` | `enableGeyserExtrasMenu` | — |
| F6 | Remakes de menus Java : liste des joueurs (tab + header/footer), server links, succès, statistiques | `core/parity/java/menus/tablist/*`, `serverlinks/*`, `JavaTabListInjector.java`, `JavaPlayerInfoUpdateInjector.java`, `JavaServerLinksInjector.java` | via menu | — |
| F7 | Liste des plateformes | `core/features/platformlist/PlatformList.java` (+ `PlatformListCommand.java` vide, action `PLATFORM_LIST` = TODO) | — | — |
| F8 | Emote chat envoyé aux joueurs Java proches (`/muteemotechat`) | `core/parity/bedrock/EmoteUtils.java`, `ExtrasPlayer#onEmoteEvent`, `Server#sendEmoteChat` (Spigot/Velocity/Bungee), `JavaAddEntityInjector`/`JavaRemoveEntityInjector` (dimensions) | — | 🟡 info de position (100 blocs) |
| F9 | **Packs optionnels** choisis par le joueur (`optionalpacks/*.mcpack`, sous-packs) | `core/parity/java/menus/packs/*`, `core/cache/PackCacheUtils.java#onPackLoadEvent` | — | ⚔️ indirect : un pack choisi par l'admin pourrait donner un avantage visuel |
| F10 | Enregistrement du GeyserExtrasPack (priorité configurable, détection CDN) | `core/cache/PackCacheUtils.java` | `geyserExtrasPackPriority` | requis par F1 |
| F11 | **Sauvegarde des skins** (png, geometry, animation, capes) | `core/features/skinsaver/SkinSaver.java` | `enableSkinSaving` (défaut false) | — |
| F12 | **Capes Bedrock visibles côté Java** via mannequins (PacketEvents, clients ≥ 1.21.9) — **réécrit les paquets d'attaque des joueurs Java** | `core/protocol/handlers/workarounds/MannequinCapeWorkaround.java`, `ProtocolHandler.java`, `JavaPlayer.java`, `CapeLoader.java`, `resources/capes/*.json` | `enableBedrockCapesOnJavaWorkaround` (**défaut true**) | ⚔️🔴 hit registration Java → Bedrock |
| F13 | Block Display via FMBE (expérimental) | `core/parity/java/blockdisplay/*` | `enableBlockDisplayWorkaround` (défaut false) | — |
| F14 | Masquer la paper doll (avantage Bedrock) | `ExtrasPlayer#tick` | `disablePaperDoll` (défaut false) | ⚔️ équité (voit l'armure sans inventaire) |
| F15 | Suppression du brouillard du Nether Bedrock | `ExtrasPlayer#tick` | ❌ **aucun toggle, toujours actif** | ⚔️ visibilité dans le Nether |
| F16 | Son de blocage au bouclier (pour soi) | `JavaEntityEventInjector.java` | ❌ | ⚔️ feedback |
| F17 | Animation de swing sur MISSED_SWING (tactile) | `BedrockPlayerAuthInputInjector.java`, `ExtrasPlayer#swingArm` | ❌ | ⚔️ visuel local uniquement |
| F18 | Compteur FPS (bossbar) | `BedrockDiagnosticsInjector.java`, `ExtrasPlayer` | pref joueur `showFPS` | — |
| F19 | Perspective verrouillée | `core/preferences/Perspectives.java` | pref joueur | 🟡 (Java a F5) |
| F20 | Reconnexion auto au reload/arrêt/kick | `GeyserExtras#autoReconnectAll`, `JavaDisconnectInjector.java`, `ExtrasPlayer#reconnect` | `autoReconnect` (défaut true) | ⚔️🟠 voir B15 |
| F21 | Tickrate (`/tick`) suivi par joueur | `JavaTickingStateInjector.java` | — | — |
| F22 | *Spigot* : placement de blocs « Java only » (anti hold-bridge) | `spigot/SpigotListener.java#onPlayerBlockPlace` | `enableJavaOnlyBlockPlacement` (défaut false, config Spigot) | ⚔️ téléporte le joueur (setback) |
| F23 | *Spigot* : contournement des blocs fantômes | `spigot/SpigotExtrasPlayer.java#tick` | `enableBlockGhostingWorkaround` (défaut false) | 🟡 performance |
| F24 | Vérificateur de mise à jour (Modrinth `geyserextras`) + mise à jour des données/pack | `core/version/PluginVersion.java`, `core/cache/Cache.java` | `checkForUpdates` | — |
| F25 | Traductions (téléchargées depuis `GeyserExtras/data`) | `core/locale/GELocale.java`, `BedrockLocale.java` | `defaultLocale` | — |
| F26 | Section Debug (infos appareil du joueur) | `core/menus/settings/sections/DebugSection.java` | `debugMode` | — |

Stub/inachevé : `hungerSprintCancel()` (vide), `PLATFORM_LIST` (TODO), `PlatformListCommand` (vide), `JavaOpenScreenInjector` (passe-plat), `/ge reload` (commenté dans `GeyserExtrasCommand.java`), `restoreCorrectBlock` (code mort dans `BedrockInventoryTransactionInjector`).

---

## 3. Fonctionnalités de combat : analyse d'équité

| Fonctionnalité | Avantage injuste possible ? | Constat |
|---|---|---|
| F1 Sons de combat | Non : donne au Bedrock le même feedback audio que le Java. | Implémentation divergente de Geyser (B7). Toggle global uniquement. |
| F2 Cooldown | Non : indicateur visuel, le serveur Java calcule les dégâts. Désactiver l'indicateur ne pénalise que le joueur lui-même. | Code de cooldown retiré : rien à fiabiliser côté GE, sauf le menu (VideoSection) qui dépend de l'API Geyser. |
| F3 Swap offhand rebindé | Non en soi : paquet `SWAP_HANDS` identique à la touche F Java, validé par le serveur. | Pas de limite de fréquence (Java non plus). Les emotes déclenchant le swap dépendent de `EmoteListPacket`, dont la traduction a disparu de Geyser 2.11 ❓ (B3). |
| F4 Double-tap | Désavantage pour le Bedrock : +220 ms d'ouverture d'inventaire, et détection cassée (B2). | |
| F12 Mannequins (capes) | **Oui, risque réel** : les attaques Java visant un mannequin sont réécrites vers le joueur Bedrock. Les mannequins sont envoyés avec `sendPacketSilently` (invisibles aux autres listeners) et GE écoute en priorité `HIGHEST` : un anti-cheat PacketEvents (Grim…) voit une attaque sur une entité inconnue → selon l'anti-cheat, soit l'attaque est annulée (le Java ne peut plus toucher le Bedrock), soit les contrôles de reach/hitbox sont sautés (un Java qui triche n'est plus détecté contre les Bedrock). | **À désactiver sur le SMP tant que ce n'est pas corrigé/testé.** |
| F14 Paper doll | Avantage Bedrock connu ; l'option existe mais est désactivée par défaut. | Recommandation : `true` sur le SMP. |
| F15 Brouillard Nether | Rétablit la parité (le brouillard Bedrock est bien plus dense que celui de Java). | Aucun toggle → contraire à la règle n°7. |
| F20 Reconnexion sur kick | Un joueur kické (anti-cheat, combat-log) est immédiatement renvoyé vers le serveur. | B15. |
| F22 Placement Java-only | Rétablit la parité (anti scaffold Bedrock). | Téléporte le joueur → conflit possible avec les setbacks d'anti-cheat. |

---

## 4. Issues ouvertes du dépôt original (48 issues + 1 PR)

Classement : **Bug** = bug réel encore présent dans le code actuel (ou très probable) · **Bug-API** = casse due à un changement d'API Geyser (résolu par le portage) · **Feature** · **Obsolète**.

### 4.1 Bugs réels (16)

| # | Titre (résumé) | Cause identifiée dans le code | Gravité |
|---|---|---|---|
| #76, #29 | NPE `GE.connections is null` → désync totale | En mode extension, `GeyserHandler.register()` (`GeyserExtras.java:68`) est appelé **avant** `connections = new ConcurrentHashMap<>()` (l.96), et entre les deux il y a les téléchargements réseau. Tout paquet reçu pendant ce temps plante. | 🔴⚔️ |
| #68 | NPE à la connexion pendant l'initialisation | En partie Geyser (`WorldManager` null), mais la fenêtre est élargie par les téléchargements bloquants de GE au `PostInitialize`. | 🟠 |
| #67 | NPE `getDimensionType()` spammé (écran code de conduite) | `ExtrasPlayer#tick` (l.196) ne vérifie pas que la dimension est connue. | 🟠 |
| #57 | NPE `GELocale.translate` → menu inaccessible | Si les langues n'ont pas été téléchargées, `LANGUAGES_MAP.get("en_US")` est null. Pas de langue embarquée dans le jar. | 🟠 |
| #36 | « Could not translate packet InteractPacket » | Très probablement `ExtrasPlayer.get(session)` null (aucun null-check dans les injecteurs) → le paquet d'interaction est perdu. | 🔴⚔️ |
| #54 | « Could not translate ClientboundPlayerInfoUpdatePacket » | Même famille (null ou Adventure). Logs externes à relire. ❓ | 🟠 |
| #56 | « Could not translate ClientboundTabListPacket » (conflit avec TAB) | Relocation Adventure fragile (`spigot/build.gradle.kts`). | 🟡 |
| #43, #53 | Erreur à l'ouverture de la liste des joueurs | ❓ logs externes (mclo.gs) à relire ; probablement `PlayerListMenu`/`TabListData`. | 🟡 |
| #62 | Monter un zombie nautilus bloque l'inventaire jusqu'à reconnexion | `Action.OPEN_INVENTORY` recopie une ancienne version du code Geyser (ne gère que chevaux et chest boats) ; Geyser a depuis évolué. | 🟠 |
| #61 | Standalone oublie les packs choisis après redémarrage | `onSessionRemove` vide les packs quand `authData`/`clientData` sont null + sauvegardes asynchrones non garanties à l'arrêt (B11). | 🟡 |
| #55 | PacketEvents détecté mais « ne tournera pas » | Logique `IsAvailable`/messages contradictoires selon la plateforme (extension = pas de PacketEvents). | 🟡 |
| #32 | Fuite mémoire (proxy) | Tentative de correctif `b5a6d4b` ; suspects restants : `ProtocolHandler.connections` (HashMap statique), `playerDimensionsMap`, tâches Velocity par joueur, `new Thread` à chaque sauvegarde. ❓ | 🟠 |
| #34 | Bungee : `LinkageError` sur `sendForm` | Conflit de classloaders Cumulus/Floodgate. Plateforme secondaire. | 🟡 |
| #22 | Holoprint ne s'affiche plus avec GE | Probable conflit de fichiers du GeyserExtrasPack (priorité 100 par défaut). Priorité configurable depuis `7bc8940` → partiellement résolu. ❓ | 🟡 |

### 4.2 Bugs liés à l'API Geyser — résolus par le portage (11)

#47 (`platformType`), #63 et #69 (`getGeyserId`, Adventure), #70, #72, #74 (`CooldownType.TITLE`), #44, #46, #52 (Geyser plus récent), #73 (« mettre à jour vers 26.1.2 »), #65 et #58 (indicateur de cooldown/dégâts : géré désormais par Geyser + GeyserIntegratedPack, plus par GE).

### 4.3 Demandes de fonctionnalité (10)

| # | Demande | Avis |
|---|---|---|
| #35 | Alias de commandes configurables (`/l` entre en conflit) | Utile, facile. |
| #60 | Accepter les packs `.zip` | Facile. |
| #20 | Ping dans la bossbar | Facile (le ping moyen est déjà calculé). |
| #16 | Menu `/ge` sur proxy | Moyen. |
| #33 | Item displays / cadres invisibles via FMBE | Gros, expérimental. |
| #18, #38 | Support Folia | Gros (scheduler régional). Hors priorité si tu es sur Paper/Purpur. |
| #6 | Port Fabric | Hors périmètre. |
| #37 | Port NeoForge | Hors périmètre. |

### 4.4 Obsolètes (11)

#2, #3, #5 (étiquetés « fixed in 2.0 »), #10 (traduction chinoise de la 1.1.4), #13 (`PlayerListEntry.getDisplayName`, 2024), #14 (`api-version 1.12`, `plugin.yml` est maintenant en 1.16), #23 (cache, ancienne beta, ❓), #24 (NPE `getJavaUUID`, code réécrit avec null-check), #25 (config null avant le cache, ordre corrigé depuis), #42 (Velocity, corrigé par `bc7d458`), #59 (renvoie à une issue Geyser, sans détail exploitable).

Total : 16 + 11 + 10 + 11 = 48.

### 4.5 PR ouverte

**#75 « 26.1 Port »** (mas6y6, 41 fichiers, jamais fusionnée) : vise Geyser 2.10 / MC 26.1. L'auteur a supprimé `BedrockInteractInjector` (« la fonction n'existait plus ») et constaté la disparition d'`EmoteListPacket` côté traduction (« rendu côté serveur par Bedrock »). Moins à jour que GeyserPlus ; à consulter seulement en complément.

---

## 5. Bugs supplémentaires trouvés à la lecture du code (absents des issues)

| ID | Problème | Emplacement | Gravité |
|---|---|---|---|
| B1 | Migration de config incohérente : `LATEST_VERSION = 4` mais une transformation vers la version **5** existe. Une vieille config migre en v5, puis est réécrite à chaque démarrage (5 ≠ 4) ; `enable-custom-cooldown` n'est jamais retiré des configs déjà en v4. | `core/config/ConfigLoader.java:21,32` | 🟠 |
| B2 | Le double-tap inventaire stocke `System.currentTimeMillis()` dans un **`float`** : précision ≈ 131 secondes à l'époque actuelle → détection du double-tap aléatoire. | `core/ExtrasPlayer.java:84` | 🟠⚔️ |
| B3 | Emotes rebindées : l'index dans `emotesList` sert d'index dans `Remappable.values()` → index ≥ 4 mappe sur SNEAK_INVENTORY/OPEN_INVENTORY/…, index ≥ 9 → `ArrayIndexOutOfBoundsException`. `emotesList.addAll` à chaque `EmoteListPacket` → doublons. Et sous Geyser 2.11+, le paquet n'est peut-être plus envoyé ❓. | `ExtrasPlayer.java:176,184`, `BedrockEmoteListInjector.java:14` | 🟠⚔️ |
| B4 | `ExtrasPlayer.get(session)` peut renvoyer null (avant `SessionLoadResourcePacksEvent`, après déconnexion) ; presque aucun injecteur ne le vérifie → paquet perdu + log d'erreur. Plusieurs injecteurs lèvent l'exception **avant** `super.translate()` (Interact, BlockPick, EntityPick, ServerSettings). | `core/injectors/**` | 🔴⚔️ |
| B5 | Initialisation dans le mauvais ordre (cf. #76) : les translators sont actifs avant que `connections` existe. De plus, le `return` anticipé (Cloudburst absent) laisse `GE` assigné avec `connections` et `config` à null : toute commande de la plateforme planterait ensuite. | `core/GeyserExtras.java:48-96` | 🔴 |
| B6 | Démarrage dépendant du réseau : `Cache.checkData()`, `downloadCredits()`, `HTTP.*`, `PackCacheUtils.initialize()` lèvent `RuntimeException` si GitHub est injoignable ou limite le débit (60 req/h sans jeton) → toute l'initialisation échoue. Le dépôt `GeyserExtras/GeyserExtrasPack` est **archivé** ; `GeyserExtras/data` ne l'est pas encore mais appartient à une organisation inactive. | `core/cache/*.java`, `core/locale/GELocale.java:40` | 🔴 |
| B7 | Les injecteurs de sons **n'appellent jamais** `super.translate()` : ils réimplémentent Geyser. Vérifié sur le code de Geyser 2.12.0 : Geyser utilise maintenant `entity.bedrockPosition()` (GE : `getPosition()`, position Java) et ajoute les particules de pinceau (#6620) — perdues avec GE. | `JavaSoundInjector.java`, `JavaSoundEntityInjector.java` | 🟠⚔️ |
| B8 | Mannequins : risque anti-cheat/équité (§3) ; `Math.abs(entityId)` sur tout paquet `INTERACT_ENTITY` ; `ProtocolHandler.connections` est une `HashMap` statique modifiée depuis plusieurs threads Netty. | `MannequinCapeWorkaround.java:422`, `ProtocolHandler.java` | 🔴⚔️ |
| B9 | Spigot : `SpigotExtrasPlayer.player = Bukkit.getPlayer(...)` au `SessionJoinEvent` (thread Geyser) peut être null → NPE à chaque tick si le contournement des blocs fantômes est activé. | `spigot/SpigotExtrasPlayer.java` | 🟡 |
| B10 | Aucun `catch` autour de `ExtrasPlayer.tick()` : une exception dans le tick d'un joueur casse la boucle Spigot pour **tous** les joueurs de ce tick. | `GeyserExtras#serverTick` | 🟠 |
| B11 | Sauvegarde des préférences : un `new Thread` par sauvegarde, écritures concurrentes sur le même fichier, `FileWriter` ouvert (donc fichier tronqué) avant de décider de le supprimer, écriture non atomique → fichiers corrompus possibles en cas d'arrêt. | `PreferencesData.java:153`, `JavaPreferencesData.java:79` | 🟠 |
| B12 | 18 `catch` silencieux (`ignored`) dont l'enregistrement du pack et des packs optionnels : un pack cassé est ignoré sans message. | `PackCacheUtils.java:141,156`, `Cache.java`, `ReflectionAPI.java` | 🟡 |
| B13 | Plusieurs fonctionnalités qui touchent aux paquets n'ont aucun interrupteur : F15 (brouillard), F16 (son bouclier), F17 (swing), F3 (rebinding, global), F20 sur kick. Contraire à la règle n°7. | — | 🟠⚔️ |
| B14 | Le vérificateur de mise à jour interroge le projet Modrinth `geyserextras` d'origine et affiche « latest version » même quand la requête a échoué. | `PluginVersion.java` | 🟡 |
| B15 | `JavaDisconnectInjector` : sur **n'importe quel** kick du serveur (anti-cheat, combat-log, whitelist), si `autoReconnect` est actif, GE transfère le joueur vers le même serveur. Commentaire d'origine : « Doesnt work lmao ». ❓ comportement réel à tester. | `JavaDisconnectInjector.java:22` | 🟠⚔️ |
| B16 | La config n'est jamais rechargée (`/ge reload` commenté) : impossible de couper une fonctionnalité en urgence sans redémarrer. | `GeyserExtrasCommand.java` | 🟠 |
| B17 | `Action.OPEN_INVENTORY` duplique un ancien code Geyser (cf. #62). | `Action.java` | 🟡 |

---

## 6. GeyserPlus : ce qui a changé et ce qu'il faut en retenir

GeyserPlus = upstream `d49ad51` + **3 commits** (tous le 2026-08-09) :
1. `23a2999` *Port to Geyser 2.11.1 / MC 26.x* (19 fichiers)
2. `4e940c6` *Rename project to GeyserPlus, bump to v2.1.0* (garde l'id `geyserextras`)
3. `ad2b4c2` README

Une release `v2.1.0` (5 jars) existe ; 5 étoiles, 0 issue ; aucun test, aucune CI. Le fichier `AGENTS.md` (en chinois) contient des notes utiles sur l'API.

Changements d'API 2.9 → 2.11.1 relevés dans `23a2999` (à reproduire en les comprenant, pas en les copiant) :

| Changement Geyser | Adaptation GeyserPlus | Mon avis |
|---|---|---|
| Toolchain Geyser en Java 21 depuis 2.10 | toolchain 17 → 21 | Nécessaire. |
| Dépôt `repo.opencollab.dev/maven-snapshots/` requis | ajouté (mal indenté dans `subprojects`) | OK. |
| `gameplay().showCooldown()` → `cooldownType()` ; enum `CooldownType` = `CROSSHAIR/HOTBAR/DISABLED` (plus de `TITLE`/`ACTIONBAR`) | `VideoSection` | OK, mais les prefs joueurs déjà sauvegardées avec `"TITLE"` ou `"ACTIONBAR"` ne se désérialiseront plus (Gson → null) ❓ : il faut une migration. |
| `BedrockEmoteListTranslator` supprimé de Geyser | `BedrockEmoteListInjector` étend `PacketTranslator<EmoteListPacket>` sans `super` | ❓ Si le client n'envoie plus ce paquet, les bindings d'emotes ne marchent plus du tout. À tester en jeu. |
| MCPL : `ClientCommand.STATS` → `REQUEST_STATS` | `Action.java` | OK. |
| Refonte des entités : `EntitySpawnContext`, `EntityTypeBase`/`VanillaEntityType`, `GeyserEntityDataManager`, `Block.asItem()`, `newItemStack(session)` | `blockdisplay/*` | Fonctionnalité expérimentale, désactivée par défaut. |
| `Entity.getGeyserId()` → `geyserId()` | `ExtrasPlayer`, `BedrockInteractInjector` | OK. |
| `BedrockClientData` renvoie `byte[]` | `SkinSaver` : `Base64.decode(byte[])` | ❓ Il faut vérifier si ces `byte[]` sont encore du base64 ou déjà décodés. |
| `getPlayFabId()` déplacé sur la session | `DebugSection` | OK. |
| `getScale()` en conflit avec une méthode de `Entity` | renommé `getDisplayScale()` | OK. |

Ce que GeyserPlus **n'a pas** fait : aucun des bugs des §4.1/§5 n'est corrigé ; le pack et les données viennent toujours des dépôts GeyserExtras ; les sons contournent toujours Geyser ; mannequins activés par défaut.

**Base recommandée pour le fork** : repartir de l'upstream `d49ad51` (historique propre, auteur original), refaire le portage commit par commit en s'appuyant sur `23a2999`, avec crédit `Co-authored`/mention GeyserPlus dans les commits concernés.

---

## 7. Geyser : dernière version stable et changements depuis 2.11.1

Geyser ne publie **ni tags git ni releases GitHub** : les versions sont sur `https://download.geysermc.org/v2/projects/geyser/versions`. Pour « lire le code au tag correspondant », on utilise donc le commit indiqué par chaque build.

- **Dernière stable : Geyser 2.12.0, build #1269 (2026-10-22, commit `2565dc1c`)**, canal `default`.
- Supporte **Java 26.3** et **Bedrock 26.30 → 26.52**. Les versions Java plus anciennes nécessitent ViaVersion sur le serveur.
- Compilation Geyser : Java 21 (target) ; dépendances : MCProtocolLib `26.3`, Cloudburst Protocol `3.0.0.Beta14`, **Adventure 5.2.0** (saut de version majeure par rapport à 4.x : à surveiller pour les relocations Spigot), Cumulus 1.1.2.

Chronologie :

| Version | Builds | Points notables pour GE |
|---|---|---|
| 2.11.1 | #1206–#1226 (août 2026) | Bedrock 26.40/26.44 ; « Optimized message translation » (#6562, touche `MessageTranslator` utilisé par la tab list) ; particules de pinceau ajoutées dans `JavaSoundTranslator` (#6620) |
| 2.11.2 | #1227–#1235 | Bedrock 26.45 ; mise à jour lib réseau et MCPL ; message plugin de skin après le spawn (#6327) |
| 2.11.3 | #1237–#1254 | Cloudburst Protocol mis à jour (#6711), Bedrock 26.50/26.52 ; nouvelles options réseau (NetherNet), **config Geyser v10** |
| 2.12.0 | #1255–#1269 (14→22 oct.) | **Java 26.3** ; nouveau MCPL ; **nouveau `ClientboundSwingAnimationPacket` traduit** (`JavaSwingAnimationTranslator`, à croiser avec F17) ; refonte des registres Java (`RegistryCache`/`TagCache` remplacés par `JavaRegistryProvider`, `Registries.JAVA_ITEMS`/`JAVA_ITEM_IDENTIFIERS`/`POTION_MIXES` supprimés) ; `GeyserSession.sendToast()` ajouté à l'API (#6678, peut remplacer notre `ToastRequestPacket` manuel) ; `setFishingRodCast` → `setFishingHook` ; nouveau `GeyserIntegratedPack.mcpack` (#6815) ; `Item#javaIdentifier` pour les extensions |

Vérifié sur le code de Geyser (`860fc710` = 2.11.1 final → `2565dc1c` = 2.12.0) :
- Les translators étendus par GE existent toujours (BlockPick, EntityPick, CommandRequest, ServerSettings, Interact, AddEntity, RemoveEntities, PlayerInfoUpdate, EntityEvent, OpenScreen, Sound, SoundEntity, TickingState, PlayerAuthInput).
- Aucun usage dans le code GE/GeyserPlus des API supprimées en 2.12 (`getRegistryCache`, `getTagCache`, `JAVA_ITEMS`…), à confirmer par compilation (le code blockdisplay pourrait en dépendre indirectement).
- `joinAddress()`/`joinPort()` gardent leur signature (implémentation changée).
- Geyser n'a **pas** de translator pour `ClientboundDisconnectPacket` : celui de GE (B15) n'écrase rien, il ajoute un comportement.

---

## 8. Points bloquants et décisions nécessaires

1. **Setup confirmé : Paper/Purpur + Geyser-Spigot + Floodgate en plugins** → module prioritaire : `spigot` (le core est embarqué dedans). Conséquences directes :
   - Floodgate présent : le menu GE n'apparaît pas dans les Paramètres Bedrock (`BedrockServerSettingsRequestInjector`, contournement par toast + double-tap) et les formulaires passent par l'API Floodgate (`ExtrasPlayer#sendForm`).
   - PacketEvents éventuellement présent → F12 (mannequins) actif par défaut.
   - F22/F23 (options propres à Spigot) disponibles.
   - Versions exactes de Minecraft et de Geyser : *en attente*. Si le serveur n'est pas en Java 26.3, la cible du portage sera la version de Geyser réellement installée, pas forcément 2.12.0.
2. **Aucun JDK sur la machine** (`java` introuvable). Pour `./gradlew build`, il faut un JDK 21 (par ex. `sudo apt install openjdk-21-jdk`), ou m'autoriser à configurer le téléchargement automatique d'un JDK par Gradle (plugin `foojay-resolver`).
3. **Dépendance réseau aux dépôts GeyserExtras** (B6) : je recommande d'embarquer dans le jar les langues, les crédits et le GeyserExtrasPack (licence à vérifier pour le pack), et de ne plus rien télécharger au démarrage.
4. **Mannequins (F12)** : je recommande `enableBedrockCapesOnJavaWorkaround: false` sur le SMP dès maintenant, quelle que soit la suite.
5. Anti-cheat utilisé sur le SMP (Grim, Vulcan, Matrix… ?) et plugins PvP : nécessaires pour la Phase 2.

---

## 9. Priorités proposées pour les phases suivantes (aperçu)

1. Portage 2.12.0 (Phase 2) + interrupteurs pour toutes les fonctionnalités réseau/combat (règle 7) + rechargement à chaud (B16), puisqu'ils conditionnent la sécurité en production.
2. Corrections combat/stabilité : B4/B5/#76 (null-checks, ordre d'init), B6 (démarrage hors-ligne), B8/F12, B7 (sons via `super`/Geyser), B15, B2, B3, B10.
3. Puis B1 (migration de config), B11 (sauvegardes atomiques), #57, #67, #62, B12.
