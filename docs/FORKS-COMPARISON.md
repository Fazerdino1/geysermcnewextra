# Comparaison avec les autres forks de GeyserExtras

> Date : 2026-10-23 · Notre base : branche `port/geyser-2.12` (GeyserMCNewExtra `2.1.0-alpha.1`, Geyser 2.12.0).
> Méthode : API Modrinth, puis API GitHub `compare` de chacun des 34 forks de `GeyserExtras/GeyserExtras`
> par rapport à `master` (`d49ad51`), lecture des patchs des commits en avance.
> Toutes les sources lues ici sont sous licence **MIT**, comme l'original. On peut donc reprendre du code
> en conservant la mention de copyright et en créditant l'auteur dans le commit.

## 1. Le fork « geyser-extras-fork » sur Modrinth

**Introuvable.** `https://api.modrinth.com/v2/project/geyser-extras-fork` répond **404**, tout comme les
variantes `geyserextras-fork`, `geyser-extras` et `geyserextrasfork`. Les recherches Modrinth « geyser extras »,
« geyserextras fork » et « geyser fork » ne renvoient que :

| Slug Modrinth | Auteur | Source | Remarque |
|---|---|---|---|
| `geyserextras` | LetsGoAway | dépôt original | « [Discontinued] » |
| `geyserextras_cn` | ning-g-mo | — | traduction chinoise de la v1.1.4 (2024) |
| `geyserplus` | ning-g-mo | `leemwood/GeyserPlus` | même code que GeyserPlus v2.1.0 |

Le projet a donc soit été supprimé, soit renommé, soit l'adresse est erronée. **Je n'ai trouvé aucun fork,
sur Modrinth ou GitHub, qui revendique ou contienne un correctif de sécurité.** Si tu as un autre lien ou le nom
de l'auteur, je reprends l'analyse. Le § 4 liste les failles que j'ai trouvées moi-même.

## 2. Forks GitHub qui ont du code en plus de l'original

Sur les 34 forks : 14 n'ont aucun commit en avance ; 4 ne contiennent que des workflows de CI ; 7 sont des forks de la v1.x (137 commits de retard, code sans rapport avec la 2.x : traductions chinoises, petits correctifs 1.1.4) ; 1 n'a pas pu être comparé (`rakahago`, historique sans ancêtre commun). Ceux qui comptent :

| Fork | Commits en avance | Contenu | Dernière activité |
|---|---|---|---|
| `leemwood/GeyserPlus` | 3 | portage Geyser 2.11.1, renommage | 2026-08-09 |
| `OneCraftES/GeyserExtrasPlus` | 9 | build Fabric/NeoForge, Geyser 26.2, **gardes null dans tous les injecteurs** | 2026-07-30 |
| `paper0319/GeyserExtras` | 3 | portage 26.1.2, **correctif `/geyserextras` qui n'affiche rien**, workflow de release | 2026-07-03 |
| `AnarchadiaMC/GeyserExtras` | 5 | réflexion contre les `LinkageError` Adventure, cooldown par défaut, compat de leur fork de Geyser | 2026-06-17 |
| `mas6y6/GeyserExtras` (PR #75) | 6 | réécriture du build, port 26.1 (même base que OneCraftES) | 2026-05-22 |
| `Parthodys/GeyserExtras` | 1 | suppression de l'alias `/l` | 2026-01-18 |
| `h4-hub/GeyserExtras` | 4 | ne supprime plus de fichiers dans le dossier `packs` de Geyser | 2025-11-15 |
| `dronzer-tb/GeyserExtras-CFW` (archivé) | 1 | API de boutons personnalisés dans le menu via plugin messages | 2026-01-02 |
| `mclk623`, `PythonChicken123`, `wlm1455925277`, `THEMPGUYAlt` | 2 à 12 | workflows GitHub Actions uniquement | — |

## 3. Correctifs, un par un

Statut : ✅ déjà dans notre fork · 🟢 à reprendre · 🟡 à reprendre en l'adaptant · 🔵 idée reprise, code différent · ⛔ rejeté · ➖ sans objet pour nous.

| # | Fork / commit | Changement | Chez nous | Décision |
|---|---|---|---|---|
| 1 | GeyserPlus `23a2999` | Adaptation à l'API Geyser 2.11 | ✅ commit `0cde60c` (crédité), avec 2 régressions corrigées ensuite : double décodage du skin (`3c5d6cf`) et emote chat cassé (`6f7bf3d`) | — |
| 2 | **paper0319 `35d1877`** | Exécute les commandes des joueurs Bedrock **500 ms plus tard**, parce qu'un formulaire reçu pendant que le chat est encore ouvert est **jeté sans message par le client**. Patch Velocity uniquement. | ❌ Absent. C'est très probablement pourquoi `/geyserextras` tapé au chat n'a rien affiché sur ton serveur (log du 23/10, 08:00:16), alors que le double-tap sur inventaire, lui, marche. | 🟡 **Reprendre** : le même délai dans `SpigotCommandHandler` (ta plateforme), dans `BedrockCommandRequestInjector` (extension) et pour Bungee. Garder l'envoi des formulaires par Floodgate (le patch rappelle qu'envoyer par `GeyserSession.sendForm` provoque une `LinkageError` quand Floodgate est présent). Rendre le délai configurable. |
| 3 | paper0319 `a3c27b1` | Workflow GitHub qui publie les jars à chaque tag | ❌ (prévu en Phase 4 de la mission 1) | 🔵 Je l'écrirai pour notre fork, sans copier. |
| 4 | **OneCraftES `7114bb5`** | `if (player == null)` dans les 14 injecteurs | ❌ (bug B4 de l'audit, issues #29, #36, #76) | 🟡 **Reprendre en corrigeant** : dans `BedrockInteractInjector`, le patch fait `return` pour `OPEN_INVENTORY` quand le joueur est inconnu, donc **l'inventaire ne s'ouvre plus**. Il faut toujours appeler `super.translate()`. Même chose pour la commande Bedrock : si le joueur est inconnu, il faut rendre la main à Geyser au lieu d'avaler la commande. |
| 5 | OneCraftES / mas6y6 (build) | Gradle réécrit, Fabric/NeoForge, Java 25 | ➖ | ⛔ Hors périmètre (ton serveur est sur Purpur). |
| 6 | **AnarchadiaMC `2a5c60a`** | Lit l'en-tête de la tab list et les server links **par réflexion** pour éviter la `LinkageError` Adventure (#69, #56) | ❓ Notre jar Spigot référence bien la copie d'Adventure de Geyser (vérifié : 10 classes concernées, aucune classe Adventure embarquée). La `LinkageError` peut toutefois revenir si un autre plugin embarque Adventure sous le même nom. | 🟡 À traiter en **Phase B, priorité 3** : d'abord reproduire avec notre jar. Ne garder la réflexion qu'en secours, avec un message clair si elle échoue (le patch fait `e.printStackTrace()` à chaque paquet). |
| 7 | AnarchadiaMC `1e7ea32` | Réflexion pour `getGeyserId`/`geyserId` | ✅ sans objet : on compile contre l'API 2.12 (`geyserId()`) | ⛔ |
| 8 | AnarchadiaMC `98bed88` | Cooldown par défaut = celui configuré dans Geyser, valeur null remplacée | 🔵 Partiellement : notre `CooldownTypeAdapter` (`c536972`) convertit `TITLE`/`ACTIONBAR` et les valeurs null, mais se replie sur `CROSSHAIR`. | 🟢 Reprendre l'idée : se replier sur le `cooldown-type` de la config Geyser plutôt que sur `CROSSHAIR`. |
| 9 | AnarchadiaMC `4d08f86` | `EmoteListPacket` pour leur fork de Geyser | ✅ sans objet : Geyser ignore ce paquet. On l'a remplacé par les liens d'emotes (`83dc8a2`). | ⛔ |
| 10 | Parthodys `9f9d2b2` | Supprime les alias `/l`, `/tab`, `/tablist`, `/listplayers` | ❌ | ⛔ Tel quel, ça casse la compatibilité (règle n°3). 🟢 À la place : **alias configurables** (issue #35), en gardant les alias actuels par défaut. |
| 12 | **h4-hub `a352538`** | Retire le code qui **supprime au démarrage** `GeyserExtrasPack.mcpack` et `GeyserOptionalPack.mcpack` du dossier `packs` de Geyser | ❌ Notre code supprime toujours ces deux fichiers à chaque démarrage (`PackCacheUtils.initialize`). Un admin qui aurait volontairement mis le GeyserOptionalPack dans Geyser le perd sans prévenir. | 🟢 **Reprendre l'idée** : ne plus rien supprimer chez Geyser, seulement avertir dans la console que le pack fait doublon. |
| 11 | dronzer-tb `18affd7` | Le serveur backend ajoute des boutons au menu `/ge` via un plugin message | ❌ | ⛔ Pas maintenant : c'est une API pensée pour un proxy, inutile sur Purpur. Une source externe pourrait aussi injecter des boutons, à verrouiller si on la reprend. L'idée de boutons configurables rejoint la Phase E (branding du menu). |

## 4. Sécurité : ce qu'aucun fork ne corrige

Les forks n'apportent aucun correctif de sécurité. En relisant le code pour cette comparaison, j'ai trouvé :

| Gravité | Problème | Détail | Correctif proposé |
|---|---|---|---|
| 🔴 (seulement si `enable-skin-saving: true`, désactivé par défaut) | **Écriture de fichiers hors du dossier du plugin (path traversal)** | `SkinSaver` nomme les dossiers avec `SkinId` et `CapeId`, deux chaînes **choisies par le client Bedrock** et non filtrées par Geyser. Avec `../`, un client modifié crée des dossiers et y écrit `skin.png`, `skin.geometry.json` (contenu JSON qu'il contrôle), etc., partout où le serveur a le droit d'écrire. | Phase B : n'accepter que `[A-Za-z0-9._-]`, sinon un hash, et vérifier que le chemin final reste dans `skins/` ou `capes/`. **D'ici là, ne pas activer `enable-skin-saving`.** |
| 🟠 (même condition) | **Déni de service mémoire** | La taille de l'image (`SkinImageWidth`/`Height`) est fournie par le client. `new BufferedImage(width, height)` peut épuiser la mémoire, et un tableau de pixels trop court provoque une exception. | Plafonner à 512×512 et vérifier que `pixels.length == width × height × 4`. |
| 🟡 | Contenu distant non vérifié | Langues, crédits et `GeyserExtrasPack` sont téléchargés depuis GitHub sans contrôle d'intégrité. Le dépôt du pack est archivé. Les textes téléchargés sont affichés tels quels, codes couleur `§` compris. | Embarquer ces fichiers dans le jar (déjà commencé pour les nouveaux textes, `dc35986`). Sinon, vérifier un hash. |
| 🟡 (seulement si les mannequins sont activés) | Réécriture d'ID d'attaque | `Math.abs(entityId)` sur **tout** paquet d'attaque des joueurs Java. Le serveur revérifie la portée sur la vraie cible, donc pas de portée gagnée, mais n'importe quel ID négatif est réécrit. | Ne réécrire que les IDs des mannequins que GeyserExtras a lui-même créés (lié au point 1 de `ANTICHEAT.md`). |

## 5. Ce qu'on reprend : récapitulatif

À faire dans la Phase B, chaque fois sur une branche `fix/...` avec crédit dans le commit :
1. Délai avant d'exécuter les commandes Bedrock (d'après **paper0319**), adapté à Spigot, Bungee et l'extension, et configurable.
2. Gardes null dans les injecteurs (d'après **OneCraftES / Undecimer**), en corrigeant le cas inventaire et le cas commande.
3. Cooldown par défaut pris dans la config Geyser (idée d'**AnarchadiaMC**).
4. Réflexion pour la tab list, en secours seulement, si la `LinkageError` se reproduit (d'après **AnarchadiaMC**).
5. Alias de commandes configurables (issue #35, à la place du commit de **Parthodys**).
7. Ne plus supprimer de fichiers dans le dossier `packs` de Geyser (idée de **h4-hub**), seulement avertir.
6. Corrections de sécurité de `SkinSaver` (trouvées par nous).

Licences : MIT pour tous, vérifiée par l'API GitHub (`license.spdx_id = MIT`) pour OneCraftES, paper0319,
AnarchadiaMC, Parthodys, dronzer-tb et h4-hub. Le crédit se fera dans le message de commit (`Based on <fork>@<sha> by <auteur>`)
et dans la section Crédits du README.
