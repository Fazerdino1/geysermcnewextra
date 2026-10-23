# Checklist de test en jeu — portage Geyser 2.12.0

> Build : branche `port/geyser-2.12`, jar `spigot/build/libs/GeyserMCNewExtra-Spigot.jar` (`2.1.0-alpha.1`).
> À faire sur un **serveur de test** qui reproduit le SMP : Purpur 1.21.11, Geyser-Spigot 2.12.0,
> ViaVersion, Floodgate, PacketEvents, ton anti-cheat et tes plugins PvP.
> Il faut au moins **un compte Bedrock** et **un compte Java**, idéalement un Bedrock tactile (mobile) en plus.

Pour chaque ligne : ✅ OK, ❌ KO (avec l'extrait de log), ➖ pas testé.

## 0. Préparation
- [ ] Copier `plugins/GeyserMCNewExtra/` du SMP vers le serveur de test (pour vérifier la reprise des vraies configs et préférences).
- [ ] Noter la version de PacketEvents installée : `/version packetevents` → `______`
- [ ] Supprimer l'ancien jar du plugin (ancien nom), mettre `GeyserMCNewExtra-Spigot.jar`. Au premier démarrage, l'ancien dossier de données est recopié dans `plugins/GeyserMCNewExtra/`.
- [ ] Premier passage avec `enable-bedrock-capes-on-java-workaround: false` (voir [ANTICHEAT.md](ANTICHEAT.md)).

## 1. Démarrage
| # | Test | Attendu |
|---|---|---|
| 1.1 | Console au démarrage | Bannière `GeyserMCNewExtra`, `Version: 2.1.0-alpha.1`, `Done!`, aucune exception GeyserMCNewExtra |
| 1.2 | Message de mise à jour | `Update check skipped: GeyserMCNewExtra has no release channel yet.` |
| 1.3 | `config.yml` avant/après (diff) | Mêmes clés et valeurs ; seul le champ `version` peut changer |
| 1.4 | Erreurs PacketEvents au démarrage (point 2 de ANTICHEAT.md) | Aucune erreur ni double injection signalée |
| 1.5 | Connexion Bedrock **juste après** « Geyser started on UDP port » (issues #68/#76) | Noter s'il y a une NPE GeyserMCNewExtra (connue, à corriger en Phase 3) |

## 1bis. Langues
| # | Test | Attendu |
|---|---|---|
| 1b.1 | Premier démarrage | Dossier `plugins/GeyserMCNewExtra/lang/` créé avec `fr_FR.yml`, `en_US.yml`… ; messages de console en français |
| 1b.2 | Client Bedrock en français, puis en anglais (réglage de langue du jeu) | Menus et messages GeyserMCNewExtra dans la langue du client |
| 1b.3 | Modifier un texte dans `fr_FR.yml`, redémarrer | Le texte modifié s'affiche, la modification est conservée |
| 1b.4 | Supprimer une ligne de `fr_FR.yml`, redémarrer | Console : « 1 textes manquants ajoutés… », la ligne revient en fin de fichier |

## 2. Connexion et menus (Bedrock)
| # | Test | Attendu |
|---|---|---|
| 2.1 | Rejoindre | Toast de bienvenue, aucune erreur console |
| 2.2 | Préférences d'un joueur existant | Ses réglages GE sont conservés (bindings, double-tap, packs choisis) |
| 2.3 | `/ge`, puis double-tap sur l'inventaire | Le menu GE s'ouvre (via Floodgate) |
| 2.4 | Un seul tap sur l'inventaire | L'inventaire s'ouvre (avec ~0,2 s de délai). Noter si le double-tap est capricieux (bug connu) |
| 2.5 | Paramètres Bedrock | Toast « double-tap inventory » (limite Floodgate, normal) |
| 2.6 | `/l` (liste joueurs), server links, succès, statistiques | Les formulaires s'ouvrent et se remplissent |
| 2.7 | Menu Vidéo → indicateur de cooldown : crosshair, hotbar, désactivé | Chaque choix est appliqué et survit à une reconnexion |
| 2.8 | Joueur dont `preferences/<xuid>.json` contient `"cooldownType":"TITLE"` (ancien format, à créer à la main si besoin) | Charge sans erreur, indicateur en mode crosshair |

## 3. Combat ⚔️ (le plus important)
Toujours avec l'anti-cheat actif ; surveiller ses logs ou alertes pendant toute la section.

| # | Test | Attendu |
|---|---|---|
| 3.1 | Java frappe Bedrock (coups normaux, critiques en sautant, sweep à l'épée, knockback en sprint) | Les coups passent, mêmes dégâts et knockback que Java contre Java |
| 3.2 | Bedrock frappe Java (mêmes types de coups) | Les coups passent, aucune alerte anti-cheat sur le Bedrock |
| 3.3 | Sons entendus **par le Bedrock** pour 3.1/3.2 | Sons Java crit / sweep / knockback / coup faible |
| 3.4 | `enable-java-combat-sounds: false`, puis redémarrer | Sons Bedrock d'origine (Geyser) |
| 3.5 | Bedrock bloque au bouclier | Son du bouclier **une seule fois** (noter s'il est doublé) |
| 3.6 | Bedrock **tactile** : frapper dans le vide | Animation de bras **une seule fois** (doublée avant ce portage) |
| 3.7 | Indicateur de cooldown pendant les combats 3.2 | Cohérent avec le cooldown réel (issues #58/#65 : noter toute anomalie) |
| 3.8 | Lier « Pick Block » ou « Sneak + Inventaire » à **Swap offhand**, l'utiliser en plein combat | Swap instantané, aucune alerte anti-cheat |
| 3.9 | Lier « Sneak + Drop » à Swap offhand | Noter : l'objet est-il **aussi** jeté ? alerte anti-cheat ? |
| 3.10 | Combat dans le Nether | Pas de brouillard Bedrock épais (comportement voulu) |
| 3.11 | `disable-paper-doll: true` | La paper doll disparaît chez le Bedrock |

### 3bis. Capes par mannequins (seulement si tu veux évaluer cette option)
Activer `enable-bedrock-capes-on-java-workaround: true`, redémarrer, un Bedrock avec une cape.
| # | Test | Attendu |
|---|---|---|
| 3b.1 | Le Java voit la cape du Bedrock | Oui (client Java ≥ 1.21.9) |
| 3b.2 | Java frappe le Bedrock 30 fois, à différentes distances | **Tous** les coups comptent ; alertes anti-cheat : noter lesquelles |
| 3b.3 | Java avec reach augmentée (client de test, **uniquement sur le serveur de test**) | L'anti-cheat doit le détecter comme contre un Java. S'il ne le détecte pas → garder l'option désactivée |

## 4. Emotes
| # | Test | Attendu |
|---|---|---|
| 4.1 | Bedrock fait une emote près d'un Java | Message d'emote dans le chat du Java (cassé dans GeyserPlus, rétabli ici) |
| 4.2 | Java tape `/muteemotechat` puis le Bedrock refait une emote | Plus de message |
| 4.3 | Menu GE → Paramètres → Raccourcis : Emote 1 = Swap offhand, cocher « Lier une emote à Emote 1 », valider, fermer les menus, faire une emote | Message « Emote 1 est maintenant liée à l'emote … » ; cette emote n'envoie pas d'emote chat |
| 4.4 | Refaire cette emote (avec un objet dans chaque main) | Les mains s'échangent |
| 4.5 | Faire une autre emote, non liée | Emote chat normal pour les Java proches, pas de swap |
| 4.6 | Se déconnecter / reconnecter, refaire l'emote liée | Le lien est conservé (swap) |
| 4.7 | Un second joueur Bedrock **sans** réglages personnels ouvre ses raccourcis | Il ne voit **pas** les réglages du premier joueur (bug corrigé) |

## 5. Divers
| # | Test | Attendu |
|---|---|---|
| 5.1 | Monter un nautilus sellé, appuyer sur inventaire (simple tap, puis double-tap) | L'inventaire du nautilus s'ouvre (#62) |
| 5.2 | Descendre du nautilus, ouvrir l'inventaire | Fonctionne sans reconnexion |
| 5.3 | Utiliser un pinceau sur du sable suspect, observé par un autre Bedrock | Particules visibles (fonction Geyser, auparavant bloquée par GE) |
| 5.4 | Packs optionnels : en activer un dans le menu, se reconnecter | Le pack est chargé ; le pack de ressources « extras » aussi |
| 5.5 | `/kick <bedrock>` | Noter : le joueur est-il automatiquement renvoyé sur le serveur ? (point 7 d'ANTICHEAT.md) |
| 5.6 | `/stop` avec un Bedrock connecté | Le Bedrock est transféré (reconnexion auto) puis rejoint au redémarrage |
| 5.7 | Si `enable-skin-saving: true` | Dossier `skins/<id>/` avec `skin.png` lisible et fichiers geometry ; aucune exception à la connexion |
| 5.8 | 15 minutes de jeu avec 2+ Bedrock, puis `/spark profiler` ou les timings | GeyserMCNewExtra ne ressort pas dans le profil |

## 6. Modules compagnons
Activer chaque module dans `spigot.yml` (ou `config.yml` pour les menus de recettes), redémarrer.
Faire chaque test avec un joueur **Bedrock** et un joueur **Java**.

| # | Test | Attendu |
|---|---|---|
| 6.1 | `armor-stands.enabled: true`. Poser un porte-armure | Il a des bras (Java et Bedrock) |
| 6.2 | Accroupi, main vide puis objet en main, utiliser le porte-armure plusieurs fois | La pose change à chaque fois (13 poses, retour à la première) ; aucun objet n'est échangé |
| 6.3 | Même chose sans être accroupi | Comportement normal (échange d'équipement) |
| 6.4 | Dans une zone protégée (claim, région) où le joueur n'a pas les droits | Rien ne change |
| 6.5 | Porte-armure invisible (hologramme d'un autre plugin) | Rien ne change |
| 6.6 | `worlds: [<un autre monde>]` | Plus de bras ni de poses dans ce monde-ci |
| 6.7 | `maps.enabled: true`. Créer une carte, la tenir en main | `X : …, Y : …, Z : …` en haut de la carte, qui suit le joueur ; texte en anglais pour un client en anglais |
| 6.8 | Poser la carte dans un cadre | Les coordonnées ne s'affichent pas sur la carte du cadre (sauf si on tient la même carte) |
| 6.9 | `locator-maps: true`, deux joueurs dans le monde de la carte | L'autre joueur apparaît (flèche bleue, rouge dans le Nether) avec son nom ; un joueur en spectateur ou vanish n'apparaît pas |
| 6.10 | `reduced-debug-info: true`, redémarrer, puis `false` et redémarrer | La règle `reducedDebugInfo` passe à `true` puis revient à `false` (messages dans la console) ; une règle déjà à `true` avant n'est jamais remise à `false` |
| 6.11 | `collisions.enabled: true`, redémarrer | Console : « bamboo n'a plus de collision… » et pareil pour pointed_dripstone ; avec Hurricane installé, un avertissement et rien n'est modifié |
| 6.12 | Bedrock : marcher dans un champ de bambous et contre des stalactites | Plus de retours en arrière |
| 6.13 | Poser un bambou ou une stalactite à l'endroit où l'on se tient | Refusé |
| 6.14 | `recipe-menus.enabled: true`. Bedrock : ouvrir une enclume | Un coffre : objet gauche, objet droite, résultat ; l'objet central indique le coût |
| 6.15 | Réparer / combiner deux objets, prendre le résultat | Résultat identique à Java, niveaux retirés, objets d'entrée consommés |
| 6.16 | Recette ou enchantement personnalisé d'un plugin (ou datapack) dans l'enclume et la table de forge | Le résultat s'affiche et se récupère |
| 6.17 | Cliquer sur une vitre grise ou l'objet d'information, essayer de les déplacer | Rien ne bouge, le menu se remet en place |
| 6.18 | Coût ≥ 40 en survie | « Trop cher ! » ; le résultat ne se récupère pas |
| 6.19 | Ouvrir l'enclume accroupi, mains vides | L'enclume Bedrock normale s'ouvre ; renommer fonctionne |
| 6.20 | Table de forge : modèle + armure en diamant + lingot de netherite | Armure en netherite, enchantements conservés |
| 6.21 | Fermer le menu avec des objets dedans, puis vérifier l'inventaire Java et Bedrock | Les objets reviennent dans l'inventaire, **aucune duplication** |
| 6.22 | Menu d'enclume ouvert par un plugin (saisie de texte) | Non modifié (pas de bloc d'enclume) — à confirmer |

## Retour
Envoie-moi : le tableau rempli, le `latest.log` du serveur de test, le nom et la version de ton anti-cheat, la version de PacketEvents.
