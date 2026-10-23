# Suivi des issues du dépôt original (mission 2, Phase B)

> Mis à jour le 2026-10-23. Le classement complet des 48 issues est dans `docs/AUDIT.md` (§4) ; ce fichier suit l'avancement.
> Statuts : ✅ corrigé · 🟰 réglé par le portage (preuve indiquée) · 📝 expliqué, pas de bug chez nous · ⏳ à faire.

## Priorité 1 : compatibilité et crashs
| Issue | Statut | Preuve / commit |
|---|---|---|
| #76 (et #29) Standalone : `GE.connections is null` | ✅ | Les tables de joueurs sont créées avant le moindre paquet (`7e4821b`), et les injecteurs se rabattent sur Geyser quand le joueur est inconnu (`ab7bcd6`). |
| #74 / #70 `NoSuchFieldError CooldownType.TITLE` (Velocity) | 🟰 | Compilé contre Geyser 2.12.0. Analyse du bytecode des 4 jars : aucune référence à `TITLE` ni `ACTIONBAR`, seulement `CROSSHAIR`, `HOTBAR`, `DISABLED` et `VALUES`, toutes présentes dans `CooldownUtils$CooldownType` de Geyser 2.12.0. Préférences déjà enregistrées : migrées (`c536972`). |
| #69 `NoSuchMethodError getGeyserId` | 🟰 | `geyserId()` (portage `0cde60c`). |
| #69 `LinkageError` sur la tab list | ⏳ | Priorité 3 : à reproduire avec notre jar. Le jar Spigot pointe vers la copie d'Adventure embarquée dans Geyser (vérifié). |
| #68 NPE à la connexion pendant l'initialisation | 📝 | Bug de **Geyser** : Geyser-Spigot ouvre son port UDP (`GeyserImpl.start()`) avant de créer son `WorldManager` (`GeyserSpigotPlugin.onGeyserEnable`, commit `2565dc1c`). GeyserExtras élargissait la fenêtre parce qu'il télécharge des fichiers dans `GeyserPostInitializeEvent`, qui s'exécute à l'intérieur de `start()`. Ce n'est plus le cas une fois le cache rempli (0 s dans tes logs). Solution définitive côté fork : embarquer langues et pack dans le jar (audit B6). À signaler à GeyserMC. |
| #67 `getDimensionType()` null | ✅ | Garde dans `ExtrasPlayer.tick` + `safeTick()` qui n'écrit qu'une erreur par joueur (`339fff2`). |
| #47 crash à l'arrêt (core jamais initialisé) | ✅ | `onDisable` passe par `GeyserExtras.runSafely` sur toutes les plateformes, test `RunSafelyTest` (`1a3092f`). |

## Priorité 2 : PvP
| Issue | Statut | Explication |
|---|---|---|
| #65 « 9996.794 Attack Speed » dans l'infobulle | 📝 | La capture montre la valeur **absolue** de la vitesse d'attaque, calculée à partir de la valeur de base du joueur. Un plugin qui supprime le cooldown (genre OldCombatMechanics) porte cette base à ~10 000 ; le client Java affiche alors la même chose. Ce n'est pas un bug de GeyserExtras, et l'issue date d'avant le retrait du cooldown maison (`66094e0`, février 2026). Geyser masque d'ailleurs son indicateur quand la vitesse d'attaque dépasse 20. Sur ton SMP, AttackThrough et MaceCooldown ne modifient pas cette valeur, à confirmer en jeu. |
| #58 un « X » à la place de l'indicateur de dégâts | 📝 | La capture montre un **carré de caractère manquant** sous le viseur. C'est l'ancien indicateur de cooldown de GeyserExtras, qui utilisait les caractères spéciaux de `font/glyph_F8.png` du GeyserExtrasPack ; quand le pack n'était pas chargé, on voyait ce carré. Cet indicateur a été retiré en février 2026 : c'est Geyser qui l'affiche, en interface JSON avec son GeyserIntegratedPack. **Reste** : le GeyserExtrasPack déplace toujours le sous-titre sous le viseur et change la taille des titres (`ui/hud_screen.json`) pour **tous** les titres du serveur. À rendre optionnel quand on reprendra le pack. |

## Reprises des autres forks (`docs/FORKS-COMPARISON.md`)
| Correctif | Statut |
|---|---|
| Délai des commandes Bedrock (paper0319) | ✅ `1f994a0` |
| Gardes null (OneCraftES) | ✅ `ab7bcd6` |
| Cooldown par défaut = config Geyser (AnarchadiaMC) | ✅ `d035458` |
| Ne plus supprimer les packs de Geyser (h4-hub) | ✅ `0d2e2a0` |
| Réflexion pour la tab list (AnarchadiaMC) | ⏳ seulement si #69 se reproduit |
| Alias configurables (#35, à la place de Parthodys) | ✅ |

## Priorité 3 : liste des joueurs et tablist
| Issue | Statut | Preuve / commit |
|---|---|---|
| #53 / #43 erreur à l'ouverture de la player list | ✅ (cause probable) | Les logs mclo.gs ont expiré (404). Dans le code, une entrée créée sans `ADD_PLAYER` n'a ni profil ni nom affiché, et `getPlayerListName` plantait dessus. Nom de secours, et entrées créées seulement sur `ADD_PLAYER` ; test `TabListDataTest` (`8184cf3`). |
| #54 `NoSuchMethodError PlayerListEntry.getDisplayName()` | ✅ | Le log (pastebin) montre **les jars Spigot et Extension installés ensemble** : le jar extension, compilé avec Adventure non relocalisé, tourne dans Geyser-Spigot, qui relocalise Adventure. Désormais un seul message explique quel jar utiliser, et la liste des joueurs se désactive au lieu de produire une erreur à chaque paquet (`8184cf3`). |
| #56 `ClientboundTabListPacket` | ✅ | Même famille de `LinkageError`, même traitement (`8184cf3`). Le log joint ne contient pas l'erreur GeyserExtras. |
| Tablist vidée au changement de serveur (proxy) | ✅ (à confirmer en jeu) | `ClientboundPlayerInfoRemovePacket` n'était pas géré : les joueurs partis restaient affichés, et la liste dépendait d'états incohérents. Les proxys vident et reremplissent la liste avec ces paquets, désormais suivis comme le fait le client Java (`8184cf3`). |

## Priorité 4 : divers
| Issue | Statut | Preuve / commit |
|---|---|---|
| #62 nautile et inventaire | ✅ | `2e1c42c` |
| #57 le menu Extras refuse de s'ouvrir | ✅ | Plantage de `GELocale` sans fichier `en_US` corrigé (`dc35986`), test `GELocaleTest` (`ca83b15`). |
| #61 Standalone oublie les packs après un redémarrage | ✅ (cause probable) | Le log mclo.gs a expiré. Les préférences, packs compris, étaient écrites par un thread créé pour chaque sauvegarde, qui **vidait le fichier avant d'écrire** : un arrêt entre les deux laissait un fichier vide. Écriture atomique et ordonnée, attente de 5 s à l'arrêt ; tests `PreferencesIOTest` (`cdd6b95`). |
| #60 packs `.zip` | ✅ | `.zip` et `.mcpack` acceptés, un pack cassé est ignoré avec un avertissement ; test `PackLoaderTest`. |
| Réglages absents du menu Paramètres avec Floodgate | ✅ (à confirmer en jeu) | Tentative par la voie Geyser ; à la première `LinkageError`, un message unique puis retour au toast (`0cb3177`). |
| #35 alias configurables | ✅ | Option `disabled-command-aliases`. `/playerlist` réparé au passage. |

## Trouvé en route (absent des issues)
| Problème | Statut |
|---|---|
| Sauvegarde des skins : écriture hors du dossier, saturation mémoire | ✅ `86bba79` |
| Velocity : un kick reconnectait tous les joueurs Bedrock | ✅ `16dce7a` |
| Tâches de tick jamais arrêtées après déconnexion (Velocity, Bungee, extension) : cause probable de #32 | ✅ `339fff2` |
| Préférences partagées entre joueurs sans fichier | ✅ `27a6db6` |
| Version de config incohérente | ✅ `9b9e769` |
