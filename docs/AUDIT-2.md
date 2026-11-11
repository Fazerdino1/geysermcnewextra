# Audit n°2 : état du code après le portage (2026-11-11)

> Base : `main@d83c92a` (2.1.0-alpha.1) + survol de la branche `feat/companion-modules` (7 commits non fusionnés).
> Suite de `docs/AUDIT.md` (audit de l'upstream) et de `docs/ISSUES-STATUS.md`. Ce document ne liste que ce qui
> **reste à faire** ou qui est **nouveau** ; ce qui est déjà corrigé n'est pas répété.
> Aucune modification de code. Build local impossible dans cet environnement (dépôts Maven bloqués par le réseau),
> mais la CI GitHub est verte sur `main` et sur `feat/companion-modules` (tests compris).

Légende : 🔴 critique · 🟠 important · 🟡 mineur · ⚔️ impact PvP/modération · ❓ à confirmer en jeu

---

## 1. Résumé

| Gravité | Nombre | Les plus urgents |
|---|---|---|
| 🔴 | 4 | Reconnexion auto après un kick (C1), protection des blocs contournée (C2), mannequins actifs par défaut (C3), mannequins envoyés aussi aux joueurs Bedrock (C4) |
| 🟠 | 10 | Démarrage dépendant de GitHub (S1), préférences perdues à l'arrêt (S2), `/pl` remplacé (S4)… |
| 🟡 | 15 | Fuites de fichiers, NPE rares, hygiène du build, code mort |

**À faire avant de mettre le jar sur le SMP :** dans `config.yml`, `auto-reconnect: false` et
`enable-bedrock-capes-on-java-workaround: false` ; dans la config Spigot, laisser `enable-java-only-block-placement: false`.
Ces trois réglages neutralisent C1 à C4 en attendant les correctifs.

---

## 2. Critiques 🔴

### C1 ⚔️ La reconnexion automatique annule les kicks (et peut boucler sur un ban)
`auto-reconnect` vaut `true` par défaut et déclenche `session.transfer()` (renvoi vers le même serveur) à **trois** endroits :

| Endroit | Quand |
|---|---|
| `core/.../injectors/java/JavaDisconnectInjector.java:25` | Sur **tout** `ClientboundDisconnectPacket` du serveur Java : kick d'anti-cheat, combat-log, ban, whitelist, serveur plein. Le commentaire d'origine dit « Doesnt work lmao ». |
| `core/.../GeyserMCNewExtra.java:219` (`onSessionRemove`) | Sur **toute** déconnexion de session Geyser, quelle qu'en soit la cause. |
| `velocity/.../VelocityListener.java:26` | Sur `KickedFromServerEvent` : le correctif `16dce7a` ne transfère plus que le joueur kické, mais le transfère quand même. Sur un proxy, cela casse aussi le renvoi vers le lobby de Velocity (le joueur repart vers le proxy au lieu d'aller au lobby). |

Conséquence attendue : un joueur Bedrock kické par l'anti-cheat ou pour combat-log est renvoyé aussitôt sur le serveur ;
un joueur banni est refusé, transféré, refusé, etc. ❓ L'effet exact dépend de l'ordre dans lequel Geyser ferme la
connexion Bedrock (si le paquet de transfert part avant la fermeture). **Test simple :** `/kick <joueur bedrock>` avec
`auto-reconnect: true`.
**Correctif proposé :** ne reconnecter que sur arrêt/reload du serveur (`autoReconnectAll`), jamais sur un kick ni une
déconnexion ; supprimer `JavaDisconnectInjector` et l'appel dans `onSessionRemove`, ou les mettre derrière une option
séparée `reconnect-on-kick: false`.

### C2 ⚔️ Le placement « Java only » peut **annuler** la protection d'un autre plugin
`spigot/.../SpigotListener.java:105-130`, option `enable-java-only-block-placement` (défaut `false`).
Le handler `BlockPlaceEvent` n'a ni `ignoreCancelled = true` ni priorité adaptée, et fait
`ev.setCancelled(!block.equals(hit))` (l.119). Si WorldGuard, un plugin de claims ou de spawn a déjà annulé la pose,
ce code peut **remettre `cancelled` à false** : un joueur Bedrock pose alors des blocs dans une zone protégée.
En plus, quand la pose est annulée (par lui ou par un autre plugin), il téléporte le joueur sur place et fait
`block.setType(Material.AIR)` (l.123), ce qui peut aussi interférer avec les setbacks d'anti-cheat.
**Correctif :** `@EventHandler(priority = HIGH, ignoreCancelled = true)`, ne jamais appeler `setCancelled(false)`
(seulement `setCancelled(true)`), et retirer le `setType(AIR)` (l'annulation suffit à restaurer le bloc).

### C3 ⚔️ Mannequins (capes) toujours actifs par défaut
`GeyserMCNewExtraConfig.java:76` : `enableBedrockCapesOnJavaWorkaround = true`, alors que l'audit n°1 (§3, §8.4)
recommandait de le couper. Rien n'a changé dans `MannequinCapeWorkaround.java` depuis l'upstream :
- les attaques Java sur l'id d'un mannequin (`-id`) sont réécrites vers le joueur Bedrock (l.422-424) **après** les
  autres listeners (priorité `HIGHEST`) : un anti-cheat PacketEvents (Grim…) voit d'abord une attaque sur une entité
  inconnue ;
- ❓ la réécriture ne fait pas `event.markForReEncode(true)`, contrairement aux autres modifications du même fichier :
  si PacketEvents n'applique pas la modification sans cet appel, **les Java ne touchent plus les Bedrock qui ont une
  cape** (ils frappent le mannequin). À tester en PvP réel ;
- `SERVER.log(capeUUID.toString())` (l.159) écrit une ligne dans la console à **chaque** paquet de métadonnées
  d'un joueur Bedrock (spam de logs) ;
- `ProtocolHandler.connections` (`ProtocolHandler.java:16`) est toujours une `HashMap` statique modifiée depuis
  plusieurs threads Netty (corruption possible de la map) ;
- `getPlayerCapeUUID` lit `session.getClientData()` sans vérifier qu'il n'est pas null.
**Correctif minimal :** défaut à `false` + migration de config ; puis `ConcurrentHashMap`, suppression du log,
`markForReEncode(true)`.

### C4 ❓ Les mannequins sont aussi envoyés aux joueurs **Bedrock**
`ProtocolHandler.onUserLogin` crée un `JavaPlayer` pour **chaque** utilisateur PacketEvents. Sur Geyser-Spigot, la
connexion Java d'un joueur Bedrock passe aussi par PacketEvents ; le commentaire « Only handle this when we are sending
packets to java players » n'est pas appliqué dans le code. Un Bedrock recevrait donc le mannequin d'un autre Bedrock,
que Geyser traduit en entité visible (le « corps invisible » repose sur le pack de ressources **Java**). Résultat
probable : un faux joueur superposé aux Bedrock qui portent une cape. **Correctif :** ignorer les utilisateurs pour qui
`GE.geyserApi.isBedrockPlayer(uuid)` est vrai dans `ProtocolHandler.add`.

---

## 3. Importants 🟠

| ID | Problème | Emplacement | Détail / correctif |
|---|---|---|---|
| S1 | **Démarrage dépendant de GitHub** (reste de B6) | `cache/PackCacheUtils.java:45-53, 94` | Le pack vient encore de `GeyserExtras/GeyserExtrasPack` (dépôt **archivé**). Sans réseau au premier démarrage : `RuntimeException`, `Cache.initialize()` s'arrête, donc `EmoteUtils.initialize()` (`GeyserMCNewExtra.java:142-144`) n'est jamais appelé → **NPE à chaque emote** (`emotes` null) et pas de sons de combat (le pack manque). `getPackVersion` utilise `openStream()` **sans timeout** : un GitHub lent bloque le démarrage de Geyser. Embarquer le pack dans le jar règle tout. |
| S2 | Cache du pack corrompu durablement | `PackCacheUtils.java:45-49` | La version est enregistrée **avant** le téléchargement, et le téléchargement écrit directement dans le fichier final. Un téléchargement coupé laisse un `.mcpack` tronqué, considéré à jour aux démarrages suivants. Écrire dans un `.tmp` puis déplacer, et sauver la version après. |
| S3 | **Préférences perdues à l'arrêt quand `auto-reconnect: false`** | `GeyserMCNewExtra.java:263-273` | `PreferencesIO.flush()` n'est appelé que dans `autoReconnectAll`, qui sort tout de suite si la reconnexion est coupée. Le thread d'écriture est un démon : les sauvegardes en attente sont abandonnées. Et si tu appliques la recommandation C1, tu tombes dedans. Appeler `flush` dans `onDisable`/shutdown de chaque plateforme, indépendamment de la reconnexion. |
| S4 | `/pl` et `/platforms` déclarés mais vides | `spigot/src/main/resources/plugin.yml:18-23` | La fonctionnalité n'existe pas (`PLATFORM_LIST` = TODO, absente de `CommandExecutor`), mais la commande est enregistrée et **prend `/pl`** à Bukkit (liste des plugins) : la commande ne fait rien. Elle n'est pas non plus dans la liste de `disabled-command-aliases`. Retirer la commande de `plugin.yml`. |
| S5 | Course entre deux sessions du même joueur | `GeyserMCNewExtra.java:208-251` | `onLoadPacks` remplace l'`ExtrasPlayer` existant sans appeler `onDisconnect()` (tâche de tick et bossbar de l'ancien jamais arrêtées) ; `onSessionRemove` cherche le joueur par **pseudo** et non par session. Si l'ancienne session se ferme après l'arrivée de la nouvelle (reconnexion rapide, transfert C1), c'est le **nouvel** `ExtrasPlayer` qui est supprimé : menus, bindings et liste des joueurs ne marchent plus jusqu'à la reconnexion suivante. Comparer `player.getSession() == connection`. |
| S6 | `/emotechat` peut planter | `commands/EmoteChatCommand.java:25-34` | `getJavaPreferencesData` peut renvoyer null (préférences retirées par un `PlayerKickEvent` **annulé** : `SpigotListener` traite le kick comme un départ même si un plugin l'annule). NPE → la commande ne répond pas. |
| S7 | Brouillard du Nether retiré sans option (B13) | `ExtrasPlayer.java:248-250` | Toujours actif, impossible à couper. Idem son du bouclier (`JavaEntityEventInjector`). Contraire à ta règle « un interrupteur par fonctionnalité réseau/combat ». |
| S8 | Pas de `/ge reload` (B16) | `GeyserMCNewExtraCommand.java` | Toujours commenté : impossible de couper C1/C3 en urgence sans redémarrer. |
| S9 | Raccourci double-tap : +220 ms sur **chaque** ouverture d'inventaire | `BedrockInteractInjector.java` | Activé par défaut pour tout le monde (`enableDoubleClickShortcut = true`). Désavantage PvP pour les Bedrock (inventaire en combat). Proposer `false` par défaut, ou un délai plus court. |
| S10 | Branche `feat/companion-modules` : collisions désactivées **pour tout le serveur** | `spigot/modules/collisions/*` | Le bambou et les stalactites perdent leur collision pour les Java aussi (on traverse le bambou, qui sert de couverture en PvP). Écrit dans des champs `static final` via `sun.misc.Unsafe` : fonctionne sur Java 21, mais Unsafe est en cours de retrait dans les JDK récents. Déjà signalé « exploitable en PvP » dans `COMPANION-MODULES.md` : à garder désactivé par défaut et à documenter clairement. |

---

## 4. Mineurs 🟡

**Bugs**
- `JavaPreferencesData.java:37, 61` : `new FileInputStream(...)` jamais fermé, à chaque connexion d'un joueur Java (fuite de descripteurs). Utiliser `Files.readAllBytes` ou un try-with-resources.
- `EmoteUtils.java:123` : `obj.get("specialmessage").getAsString()` sans test de null ; 1 emote sur 16 qui n'a pas de message spécial → NPE.
- `ExtrasPlayer.java:304` (et les 2 autres `sendForm`) : `FloodgateApi.getPlayer(javaUUID)` peut renvoyer null (UUID Java encore inconnu) → NPE au lieu d'un repli sur `session.sendForm`.
- `CommandExecutor.java:50` : `input.substring(1)` sur une commande vide → exception, paquet de commande perdu. Les commandes sont aussi sensibles à la casse (`/GE` ne marche pas).
- `SpigotExtrasPlayer.player` peut rester null (B9) → NPE dans l'emote chat et le contournement des blocs fantômes (log une seule fois grâce à `safeTick`, mais la fonctionnalité est morte pour ce joueur).
- `playerDimensionsMap` : `HashMap` écrite par le thread réseau et lue par d'autres ; jamais vidée au changement de dimension (petite fuite par session).
- `averagePing` : moyenne de toute la session, et seulement quand la valeur change ; ce n'est pas un ping moyen utile.
- `ExtrasPlayer.tick()` tourne sur le thread principal Spigot et modifie l'état caméra de la session Geyser (thread réseau) : pas protégé.
- Migration `eightToNine` : une config en `en_US` passe **de force** en `fr_FR`, même si l'admin l'avait choisi exprès.
- `PreferencesData.init` écrit `default.json` avec `FileWriter` (non atomique), alors que tout le reste passe par `PreferencesIO`.

**Build et dépôt**
- `README.md:10` dit « no build is published yet », alors que la release `v2.1.0-alpha.1` a été publiée par la CI.
- Dépendances en **SNAPSHOT** (`configurate-yaml:4.2.0-GeyserMC-SNAPSHOT`, `floodgate:core:2.2.5-SNAPSHOT`) : builds non reproductibles, alors que Geyser est épinglé.
- `build.gradle.kts` : dépôts Fabric/Architectury/NeoForge et `oss.sonatype.org` (fermé) encore présents, `codemc` en double, dépendances déclarées dans le projet racine qui n'a pas de sources, chaque dépendance en `implementation` **et** `shadow`. `gradle.properties` : propriétés Fabric/NeoForge et `minecraft_version=1.21.5` inutilisées. Tout cela était déjà dans l'audit n°1 (§1.3).
- `spigot/build.gradle.kts` compile contre `adventure-api:4.18.0` puis relocalise vers la copie embarquée de Geyser-Spigot, qui est en Adventure 5.x pour Geyser 2.12 ❓ (origine des `LinkageError` #54/#56/#69).
- Plugins Gradle anciens (`lombok 8.6`, `shadow 8.3.0`) ; `qodana.yaml` présent mais Qodana ne tourne pas en CI.

**Qualité du code**
- 22 `catch (... ignored)` silencieux, dont l'enregistrement du pack et des packs optionnels (`PackCacheUtils.onPackLoadEvent`) : un pack qui ne se charge pas ne laisse aucune trace.
- Code mort : `restoreCorrectBlock` (`BedrockInventoryTransactionInjector`), `hungerSprintCancel()`, `PlatformListCommand`, `onSessionLogin`/`onPlayerLogin` vides, `JavaOpenScreenInjector` (simple passe-plat sur `main`), bloc commenté dans `SpigotListener`.
- Imports inutilisés (`GeyserHandler` dans la plupart des injecteurs), import en double de `GeyserMCNewExtraConfig` dans `GeyserMCNewExtra.java`.
- État global statique partout (`GE`, `SERVER`, `CORE`, `CommandExecutor.ids`…), ce qui empêche de tester sans serveur.
- Commentaires d'humeur à nettoyer (« Doesnt work lmao », « AAAAAAA… », « you have never seen jank like this before »).
- Les tests couvrent bien la logique pure (config, préférences, SemVer, langues, tab list), mais **rien** ne couvre les injecteurs ni les événements de session, là où se trouvent C1, S5 et S6.

---

## 5. Ce qui est bien fait

- Ordre d'initialisation, gardes null dans les injecteurs, `safeTick`, `runSafely` à l'arrêt : les crashs de l'upstream (#76, #67, #47) sont réglés proprement.
- Écriture atomique et ordonnée des préférences (`PreferencesIO`), migration `CooldownType`, copie des collections par défaut.
- Sons : seuls les 4 sons de combat contournent Geyser, le reste passe par `super`.
- CI (build + tests sur chaque push), release automatique avec vérification tag/version, test qui interdit les textes en dur.
- Documentation très complète (audit, suivi des issues, checklist de test).

---

## 6. Ordre de correction proposé

1. **Config par défaut sûre** (migration v10) : `auto-reconnect` limité à l'arrêt/reload, mannequins à `false`. Corriger C1 et C2 dans la foulée (petits diffs).
2. `flush` des préférences à l'arrêt sur toutes les plateformes (S3) et `/ge reload` (S8).
3. Retirer `/platformlist` de `plugin.yml` (S4), corriger S5 et S6.
4. Embarquer le pack de ressources dans le jar (S1, S2) : plus aucun téléchargement au démarrage.
5. Mannequins : C4, `ConcurrentHashMap`, log, `markForReEncode`, puis test PvP avec ton anti-cheat avant de les réactiver.
6. Nettoyage du build et du code (§4), interrupteurs manquants (S7).
