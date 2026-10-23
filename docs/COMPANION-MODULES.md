# Modules compagnons : étude (mission 2, Phase D)

> 2026-10-23. Licences vérifiées sur Modrinth et GitHub **avant toute ligne de code**. Aucun code n'a été écrit ni copié.
> Notre projet est sous **MIT** : on ne peut intégrer que du code sous licence compatible (MIT, Apache, CC0…).
> Sous GPL, le projet entier deviendrait GPL ; sans licence (« tous droits réservés »), on n'a aucun droit sur le code.
> Dans ces deux cas, il faut soit réécrire la fonctionnalité sans regarder le code, soit renoncer.

## Tableau

| Module | Ce qu'il fait | Licence | Peut-on reprendre le code ? | Effort (intégration ou réécriture) | Intérêt pour CrossPlayMC-SMP |
|---|---|---|---|---|---|
| **Bedrock Armorstands** (Spog, Modrinth `armorstand`) | Les armor stands posés ont des bras ; accroupi + clic droit change la pose (13 poses, comme sur Bedrock) | **Tous droits réservés**, pas de code source publié. (Un mod Fabric homonyme d'AtmosphereDev est en CC0, mais c'est un mod Fabric : rien à reprendre pour Paper) | ❌ → **réécriture de zéro** (fonctionnalité simple, sans besoin de regarder leur code) | **Faible** : ~0,5 jour (bras à la pose de l'armor stand, cycle de poses accroupi + clic droit, option par monde) | 🟡 Moyen. **Déjà installé sur ton serveur** (BedrockArmorstands 1.0.0a) : l'intégrer ne ferait que supprimer un plugin. Aucun impact PvP |
| **MapXYZ** (letsgoawaydev, l'auteur d'origine de GeyserExtras) | Coordonnées XYZ dessinées sur les cartes, carte offerte au premier login, zoom 1:8 par défaut, **cartes localisatrices** (les autres joueurs apparaissent sur la carte), option qui masque les coordonnées du F3 | **MIT** | ✅ Oui, avec crédit et mention du copyright | **Moyen** : ~1 à 1,5 jour (port des 7 classes sous notre nom, passage par les fichiers de langue, options) | 🟡 Moyen : utile aux Bedrock et aux Java. ⚠️ **PvP** : les cartes localisatrices révèlent la position des autres joueurs, et masquer le F3 désavantage les joueurs Java. Sur le SMP, ces deux options devraient rester **désactivées** |
| **Geyser Recipe Fix** (AnsonEyre / Sideways-Sky) | Permet aux joueurs Bedrock d'utiliser les recettes **personnalisées** d'enclume et de table de forge (remplace ces menus pour eux, ajoute un pack avec un objet) | **Tous droits réservés** (aucune licence sur GitHub, « All Rights Reserved » sur Modrinth) | ❌ → réécriture de zéro, ou on garde le plugin à part | **Élevé** : ~3 à 5 jours (menus de remplacement, paquets, objet de pack, tests sur chaque type de recette) | ❓ Dépend de toi : utile **seulement si le serveur a des recettes d'enclume ou de forge personnalisées** (CrossPlayMC-HAMMER-PLUS ?). Les recettes vanilla marchent déjà via Geyser |
| **GeyserFloatingPoints** (oryxel1) | Corrige l'imprécision des coordonnées sur Bedrock très loin du centre du monde, en décalant toutes les positions envoyées au client | **GPL-3.0** | ❌ Incompatible avec notre MIT → réécriture de zéro, ou on le garde à part (c'est aussi une extension Geyser : il fonctionne très bien en plugin séparé) | **Très élevé** : 1 à 2 semaines. Il faut réécrire **toutes** les positions de tous les paquets (entités, blocs, chunks, particules, sons…) | 🔴 Faible, et **risqué en PvP** : la moindre position oubliée décale une hitbox ou un projectile. Le problème n'apparaît que vers ±100 000 blocs et plus ; avec ta bordure de monde, probablement jamais |
| **Hurricane** (GeyserMC) : collisions du bambou et des stalactites | Retire la collision côté serveur du bambou et des stalactites (dripstone) pour éviter les retours en arrière des joueurs Bedrock | **MIT** | ✅ Oui | **Faible à moyen** : ~0,5 à 1 jour, mais passe par la réflexion sur le code interne du serveur (NMS), à adapter à chaque version de Minecraft. GeyserMC le maintient déjà | 🔴 À éviter en **PvP** : Hurricane écrit lui-même qu'un client modifié peut **traverser** ces blocs. **Déjà installé sur ton serveur** (Hurricane 2.1) |
| **Hurricane** : direction des montures (canne à carotte / champignon tordu) | Faisait diriger cochons et arpenteurs aux joueurs Bedrock | MIT | — | — | ✅ **Plus nécessaire** : retiré de Hurricane en septembre 2024 (commit `bde84b0`, « get rid of item steerables workaround ») parce que Geyser le gère lui-même. À vérifier en jeu, mais rien à intégrer |

## Ma recommandation

| Priorité | Module | Pourquoi |
|---|---|---|
| 1 | **Bedrock Armorstands** (réécrit) | Peu d'effort, aucun risque PvP, remplace un plugin sans licence par du code à nous. Activable par monde |
| 2 | **MapXYZ** (repris, MIT) | Effort raisonnable, code de l'auteur d'origine sous MIT. Coordonnées sur les cartes activées, cartes localisatrices et masquage du F3 **désactivés par défaut** |
| 3 | Geyser Recipe Fix (réécrit) | Seulement si tu as des recettes d'enclume ou de forge personnalisées : à me confirmer |
| — | GeyserFloatingPoints | Ne pas intégrer : GPL, effort énorme, risque PvP, besoin quasi nul. Garder en plugin séparé si un jour nécessaire |
| — | Hurricane (collisions) | Ne pas intégrer : exploitable sur un serveur PvP. Réfléchir à garder ou non le plugin actuel |

Chaque module intégré serait **désactivé par défaut**, activable dans la config, et aussi par monde pour ceux qui touchent au gameplay.
