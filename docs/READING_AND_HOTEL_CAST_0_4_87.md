# Call-sheet collection and the hotel cast (0.4.87)

The player reported that collecting the first Head Full of Ghosts call sheet recalled the plains photograph instead. The photograph's independent morning recall could run while a native reading menu was open. A due recall now waits until the reader's menu closes, without advancing its saved recall day. The call sheet is still its exact reader-owned written original and can be collected once; afterward the same photograph returns from its observed storage once. Original contents, map identity, ownership, Mother custody and saved collection records are retained.

The new native regression uses the real Ghosts reading surface and registered event bus, a real stored map and a due morning. It checks the open reader, exact book pickup, finite collection, subsequent photograph recall and native SavedData reload. It failed against the earlier implementation locally and in CI run 38255031694, then passed with the guard. The complete native suite now declares 559 cases.

## Hotel cast

All six hotel skins are painted raster assets, restyled using the existing Last Stop and cottage NPCs as references: human facial detail, hair, worn cloth, tailoring and restrained colours. The porter retains burgundy and gold, the bartender his cream apron, the guest his healed scar, the pianist grey hair, and the dance partners their evening clothes. They use the same normalized 128 × 64 logical UV layout. The entity textures are 1774 × 887 RGBA atlases; native entity UVs use the 2:1 aspect ratio independently of raster resolution.

The coat torso now uses the actual shirt/lapel UV region instead of covering it with the skirt material, while its two coat tails remain separate moving meshes. Elbow, knee and finger joints and the existing greeting, pouring, polishing, playing, listening and dance motions remain native. The material generator preserves the reviewed cast PNGs when room materials are regenerated. Earlier hotel assets remain available.

Four mandatory native GPU views cover the cast, actions, furnishings and a comparison with the existing Russ and Marla models. The comparison renders the registered meshes and textures at the same scale and lighting. The six texture paths are `src/main/resources/assets/the_oldest_house/textures/entity/stanley_{porter,bartender,scarred_guest,pianist,dancer,dancer_dress}.png`. Artwork was made with the built-in ImageGen tool; the prompt set is recorded in [STANLEY_CAST_ART_0_4_87.md](STANLEY_CAST_ART_0_4_87.md).

## Remaining charge fixture

The 0.4.86 full suite completed with one failed charge case. Its test player was in the Overworld during the eighty-tick spawn-grace wait; the actual commitment guard moved that reader into the interior, leaving the test creature in a different dimension. The fixture now uses the real arena/interior, acknowledges the native dimension change and begins the fight only after spawn grace. It registers the same creature in the personal record and retains all three native charges, exact two-heart damage, knockback, shield wear and third-block break assertions.

That existing case also has a focused `_charge` namespace and a matching empty native template, and remains included in the full suite. It passes locally. CI runs it before the full suite without replacing any previous check. The two-client software renderer limit, enabled watchdog and both reconnects remain required.

Mod 0.4.87; protocol 58; layout 42. Still 44 sources, 33 required resolutions across two kinds and three endings. All twelve jobs, all 559 native cases, 691 PNG decodes, 90 architecture views, four Stanley views and every previous native/client/socket proof are required. Exact-source release validation is pending.
