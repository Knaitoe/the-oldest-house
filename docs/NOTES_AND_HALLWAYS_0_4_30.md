# Notes and hallways — 0.4.30

Layout 29 / protocol 28. This repair release preserves the seven integrated Claude commits, all 0.4.29 architecture and outdoor migrations, finite native originals, personal evidence, and the seventeen-source Witness pool (thirteen personal resolutions across two kinds; three endings).

## Implemented

- Straight and bent corridors have two end doors. Cross halls keep their actual branches. Floors, height, foyers and doglegs vary with ordinary plaster, stone and timber. A loop requires twelve crossings and eight intervening visits. Retired old doors first remap saved returns; domestic fragments and block entities remain. Living residents, including pets left on Stay, delay the migration along with visiting explorers.
- The child's room stays closed before visit three; an explorer already inside can leave. The ordinary door resumes native use once relevant.
- Chalk and string work outside as well as inside. Owned chalk positions include dimension identity, and string resets its segment when crossing worlds.
- The Whale sends its finite, personal letters to empty Overworld chests. Full chests and interior caches are left alone. Its outgoing-mail plaque has a timber post and floor support.
- Stair papers have distinct writer fonts, varied placements, concrete letters and no repeated “STAIR PAGE,” “LIMEN,” “second hand” or unrelated Holloway summaries. Already taken original books retain their saved components and reader snapshots.
- Holloway has native lethal damage, visible arrows and injury feedback, and varied moving dialogue. Actual death is permanent; only the killer remembers it. Death does not grant the earned shield or Witness. Personal ordered arenas and service-latch escape still resolve the source.
- The caver's door connects physically through the old stone gap. Tom's actual actor and campfire move beside the stair entrance with a continuous floor. Old exposed arena and side-maze boundaries close once, preserving deliberate passages, collapsed escape and finite caches.
- Ambient livestock no longer join the interior labyrinth. Authored actors, persistent intended animals and owned pets keep their identities.
- Player-facing entity translations replace raw registry names. The caged child has an original material-based skin and keeps the creature's UUID, hidden transformation and peaceful ending.

## Additional wall notes

The five screenshots in the added wall document are covered by targeted repairs:

- Match the impossible entry hallway's yellow patch to its existing pale plaster, preserving changed blocks and cupboard contents.
- Give the red-looking bedroom alcove a neutral finish. Recent ordinary corridors receive less weight; the recurring home copy has six intervening visits, with deliberate companion scent preserved. These intervals are personal and survive reload.
- Back the plain's entire walkable sand sheet with sandstone before restoring missing floor tiles. The old floor was suspended over outside air and could cascade into thousands of native falling blocks, leaving floating furniture and producing severe lag. Remove residual sand debris only in that authored footprint. Keep the actual lectern, barrel, photograph progression and player edits.
- Open the Goatman return door's original painted z=0 wall onto its trail. New scenes have a real approach; saved ones keep their trailer, actors, original inventory and vigil.
- Cap the west side-maze connector one block beyond its last walking row, at z=38. Its old corridor cut through z=37, which was also the outer wall. Keep the passage open and close the actual sky-facing gap; preserve collapse geometry.

These repairs have their own once-only checkpoints and wait for visitors. Native chunk tickets load old scene data between ticks before migration. They do not repeat the earlier paper/actor repair or restock stories. Five new regressions exercise actual sand ticks (with an unsupported falling control), real player collision at both entrances, finite native inventories and saved personal route memory.

## Minotaur investigation

The supplied playtest logs show death at 20:55:15 and a 2,099 ms / 41-tick server warning at 20:55:17. They contain no combat exception or profiler identifying the freeze. Two startup PNG chunk errors (the great-room rug and wardrobe side) are fixed. Every shipped texture now receives strict chunk CRC validation and full pixel decoding.

Attack motion no longer repeats collision-size reconciliation; only the actual child/adult change refreshes dimensions. Stair route positions and native model parts are cached. Per-player finale reads no longer copy every other explorer's state. The model UV dimensions now match its actual 256×256 material atlas. A creature tick exceeding 100 ms logs phase, position and owner with rate limiting.

Native tests exercise the actual transformed entity, survival player, shield collision, original weapon and living wounded state; an animated client proof renders four attack poses across 64 frames. Their measured evidence belongs in the build artifacts. These are regression checks, not a reproduction on the user's hardware, and do not establish one proven cause for the reported freeze.

## Verification

[Native Java 21 / NeoForge build](https://github.com/Knaitoe/the-oldest-house/actions/runs/37093414011) passed all **277 required GameTests**, strict CRC/decode checks for all **146 PNG textures**, package and font checks, native font/NPC/cast/home/Minotaur/inventory rendering, twenty-two architecture views and three outside-sky elevations. The physics regression directly invokes Minecraft's native sand block tick on every repaired floor tile and an explicitly unsupported control. The custody regression compares all five live original stacks, including carried-time ownership components, immediately before the actual taking.

Validated source: `d2969adebee3f66567a040d0b53510c20b1d86a3`. Runtime JAR: `the_oldest_house-0.4.30.jar`. SHA-256: `35108b23fb5cae182c0995a142af017397119755929148bc7102371ef55cee13`.

All twenty custom inventory items have separately generated transparent artwork, native 32×32 texture references and [asset provenance](../art/ASSETS_0_4_30.md). Native inventory rendering is included in the client proof. On the CI machine, 69 warmed native combat ticks averaged 0.0383 ms (maximum 0.2327 ms); 64 rendered frames averaged 1.0267 ms of CPU submission (maximum 1.4979 ms). These isolated measurements do not reproduce an integrated world stall or measure the user's hardware. The Minotaur's native combat/render checks passed; the user's reported freeze was not reproduced and still requires a fresh playtest. Timing diagnostics are included in the build artifacts and future expensive creature ticks log their phase.
