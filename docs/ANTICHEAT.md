# Points de friction avec un anti-cheat et les plugins PvP

> Contexte CrossPlayMC-SMP : Purpur 1.21.11, Geyser-Spigot 2.12.0 + ViaVersion, Floodgate, PacketEvents.
> Anti-cheat : *non communiqué*. L'analyse vaut pour les anti-cheats courants (Grim, Vulcan, Matrix,
> Spartan, Themis) ; les remarques « PacketEvents » concernent surtout ceux qui s'appuient dessus (Grim).
> Ce document décrit le code de `port/geyser-2.12`. Les solutions marquées « Phase 3 » ne sont **pas** implémentées.

Rappel d'architecture : GeyserExtras agit à deux endroits.
1. **Côté Geyser** (joueurs Bedrock) : il remplace des translators Geyser. Ce qu'il envoie au serveur
   passe ensuite par ViaVersion puis Purpur, exactement comme un paquet Geyser normal. L'anti-cheat
   voit un joueur Java ordinaire (celui que Geyser simule).
2. **Côté PacketEvents** (joueurs Java) : il modifie les paquets des **joueurs Java** (capes par
   mannequins). C'est là que se trouvent les risques sérieux.

| # | Fonctionnalité | Ce que voit l'anti-cheat | Risque | Solution proposée |
|---|---|---|---|---|
| 1 | **Capes par mannequins** (`enableBedrockCapesOnJavaWorkaround`, **activé par défaut**) | Le serveur envoie au joueur Java des entités « mannequin » avec `sendPacketSilently` (aucun autre listener PacketEvents ne les voit). Quand le Java frappe un mannequin, GE réécrit l'ID de la cible vers le joueur Bedrock, avec la priorité `HIGHEST`, donc **après** la plupart des listeners. | 🔴 Selon l'anti-cheat, l'attaque visant une entité inconnue est soit annulée (le Java ne peut plus toucher le Bedrock), soit exemptée des contrôles de reach/hitbox (un tricheur Java n'est plus détecté contre les Bedrock). | **Immédiat : `enableBedrockCapesOnJavaWorkaround: false` sur le SMP.** Phase 3 : réécrire l'ID au plus tôt (priorité `LOWEST`) pour que l'anti-cheat voie la vraie cible, ne réécrire que les IDs de mannequins connus (aujourd'hui `Math.abs` sur tout `INTERACT_ENTITY`), option pour couper la réécriture seule. Tester ensuite avec l'anti-cheat en place. |
| 2 | **Initialisation de PacketEvents par GE** | Dans `onLoad`, GE appelle `PacketEvents.setAPI(...)`, puis `load()` et `init()` sur une **nouvelle** instance, alors que le plugin PacketEvents installé a déjà initialisé la sienne. | 🟠❓ Risque de double injection dans le pipeline réseau ou de remplacement de l'instance utilisée par les autres plugins (dont un anti-cheat qui utilise le PacketEvents installé). Je n'ai pas encore pu le vérifier en jeu. | Phase 3 : si le plugin PacketEvents est présent, réutiliser son API (`PacketEvents.getAPI()`) et seulement enregistrer le listener, sans `setAPI`/`init`/`terminate`. À tester en priorité : présence d'erreurs PacketEvents au démarrage, et flags anormaux sur les joueurs **Java**. |
| 3 | Swap offhand rebindé (Emote, Pick Block, Sneak+Drop, Sneak+Inventaire, Inventaire) | `ServerboundPlayerAction SWAP_ITEM_WITH_OFFHAND`, le même paquet que la touche F. | 🟡 Pas de limite de fréquence (Java non plus). Un contrôle « action avec inventaire ouvert » peut flagger si l'action part pendant qu'un formulaire ou l'inventaire est ouvert. | Phase 3 : délai minimal configurable entre deux swaps (désactivé par défaut = comportement Java), et ne pas exécuter l'action si un inventaire est ouvert. |
| 4 | Sneak+Drop rebindé | Le drop est déjà traduit par Geyser, **puis** GE exécute l'action remappée et resynchronise l'inventaire dans le même tick. | 🟡❓ Peut ressembler à une manipulation d'inventaire (drop + swap dans le même tick). | Test en jeu (voir checklist). Phase 3 : annuler le drop quand la combinaison est remappée. |
| 5 | **Placement « Java only »** (Spigot, `enableJavaOnlyBlockPlacement`, désactivé par défaut) | Annule le placement, **téléporte le joueur sur sa propre position**, puis réapplique sa vélocité 2 ticks plus tard. | 🟠 La téléportation et le changement de vélocité peuvent provoquer des setbacks ou des flags de vol/vélocité, et entrer en conflit avec le setback de l'anti-cheat. | Laisser désactivé. Phase 3 : remplacer la téléportation par un simple renvoi du bloc et de l'inventaire. |
| 6 | Contournement des blocs fantômes (Spigot, désactivé par défaut) | `sendBlockChange` sur la ligne de vue tous les 2 ticks, envoyé au client uniquement. | 🟢 Pas d'interaction avec l'anti-cheat ; coût CPU et réseau. | Laisser désactivé sauf besoin. |
| 7 | **Reconnexion auto après un kick** (`autoReconnect`, activé par défaut) | Quand le serveur kick un Bedrock (anti-cheat, combat-log, staff), GE le **transfère** aussitôt vers le même serveur. | 🟠 Le kick perd son effet (le joueur revient en quelques secondes) ; peut contourner un « combat-log » ou une sanction temporaire basée sur le kick. Comportement réel à confirmer en jeu. | Phase 3 : nouvelle option `autoReconnectOnKick` (par défaut `false`, migration automatique), la reconnexion restant active pour les reloads et arrêts. |
| 8 | Sons de combat, son du bouclier, brouillard du Nether, paper doll, FPS | Rien : tout reste côté client Bedrock. | 🟢 | — |
| 9 | Double-tap inventaire | Aucun paquet de plus ; l'inventaire s'ouvre ~220 ms plus tard. | 🟢 (désavantage léger pour le Bedrock) | Phase 3 : corriger la détection du double-tap (horodatage stocké en `float`). |
| 10 | Floodgate | Les formulaires passent par l'API Floodgate. Le menu GE n'apparaît pas dans les Paramètres Bedrock. | 🟢 | — |
| 11 | ViaVersion | GE n'interagit pas avec ViaVersion ; il ne voit que des paquets Java 26.3 côté Geyser. | 🟢 | Les tests de combat doivent passer par la vraie chaîne Geyser → ViaVersion → Purpur. |
| 12 | Version de PacketEvents | GE est compilé contre PacketEvents **2.10.0**. Une version installée très différente peut provoquer des `NoSuchMethodError` dans le code des mannequins. | 🟡❓ | Me donner la version installée ; aligner la dépendance de compilation. |

## Recommandation de config pour le SMP en attendant la Phase 3

```yaml
enable-bedrock-capes-on-java-workaround: false   # points 1 et 2
auto-reconnect: true                              # utile pour les reloads, mais voir point 7
disable-paper-doll: true                          # équité : la paper doll montre l'armure sans ouvrir l'inventaire
```

Les noms exacts des clés sont ceux de `plugins/GeyserExtras/config.yml` sur ton serveur (le fork n'en a changé aucune).
