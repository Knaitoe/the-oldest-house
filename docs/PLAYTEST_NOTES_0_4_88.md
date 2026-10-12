# October 12 playtest notes — nightclub checkpoint (0.4.88)

This checkpoint implements the first part of the uploaded `This kid begins in the doorway(2).docx`, the Native American Father instruction and the endless nightclub. The other notes remain open. Source batches continue after quick compile and focused checks; full checks run in the background at the owner's request. It is not a verified release.

| Note | Current state |
| --- | --- |
| Cottage speech changes and overlapping bubbles | Implemented; actual private-speaker and expiry test passes |
| Chalk inside protected House and string interaction | Implemented; actual Adventure-mode click and unchanged structural protection test passes |
| Holy Rabbit Father, hunger, cold, limp and reveal | Implemented; three native cases pass, including two actual nights and the reachable ending |
| Custom Father and rabbit art | Integrated; native client render checks pending |
| Harrigan's wake after finding him in the chair | Pending |
| Simpler chalk and spool art | Pending |
| Endless dark nightclub with custom dancers | Implemented; two native gameplay cases pass; custom pixels, painted club attire and reversible despair are integrated; native GPU inspection pending |
| Goatman plates after departure | Pending |
| Institute's yellow ceiling lighting only | Pending |
| Courtyard fountain material and spout | Pending |
| Zampanò's writing wall | Pending |
| Plains note storage and daytime arrival | Pending |

The cottage keeps one current native speech bubble per reader's own household. A new speaker clears the previous speaker; expired speech removes the native custom name, including its hover label. Other readers' cast and bubbles retain their identities and words. Ordinary speech does not replace actors.

The native Adventure interaction now reaches the registered chalk and string tools before vanilla's generic placement restriction. Chalk can mark the protected original wall without granting permission to break or replace it. Door chalk retains its existing floor-mark behavior. Durability, actual hand, native reach and deliberate string toggling remain authoritative.

The Holy Rabbit Father has one consistent Native American appearance across the healthy, bandaged and injured painted skins. He wears ordinary winter clothing. No tribe, ceremonial dress or author likeness is assumed. His original private actor walks a collision-respecting route out of the shelter and back with a guarded left leg, covering less ground on the second night. The third stage has an actual collapsed body and cannot take a stride. A fresh trail requires the reader to see that injury, eat two owned original meals and see the same living rabbit on at least two mornings; the source and final account still require their actual personal readings for Witness credit.

Hunger stays at one while committed, including immediately after native meal consumption. Medicines and milk retain their existing use. The cold uses Minecraft's actual freezing overlay, milder within the shelter, with a lease cleared on departure, death, logout and fixture/server cleanup. A night waits until its finite meal has actually been eaten; a late return can no longer invalidate the meal in the same tick it is issued. Meals preserve their reader, night, original token and once-only count.

The existing native rabbit retains its UUID and any player-given name. It has one stationary location and cannot be shoved by native body collisions. A synced name-style marker selects only this reader's custom fur and fitted eye/nose/ear details; normal rabbits retain their vanilla art. An old circulating rabbit moves to the fixed location once, only when neither position is watched. No missing saved rabbit is silently replaced.

The Father's narrow entrance has physical headroom. Saved shelter repair clears only the expected original spruce leaves in the bounded entrance after native chunk/entity loading and camera guards. It retains the tree, carving, floor, block entities and edits. Creating safe headroom around the intended stationary Father is permitted; other living occupants block the change. Earned older trails, Ready/Completed accounts and original books remain earned and unchanged.

All six new native cases pass locally, including camera-gated saved-shelter preservation. The existing 41 literary cases also ran; their meal regression caught and verified the consumption-order fix. The new complete-night case separately caught and verified the late-meal clock fix. Every one of the 693 shipped PNGs decodes with valid chunk CRCs, and the existing eight font grids pass. Two mandatory native client views render the three Father skins and motions plus the actual rabbit mesh and facial details. GPU inspection is pending.

The recurring nightclub appends physical slot 95 at layout 43. Its dark room has an eighteen-person dance floor, a raised DJ booth, eight speaker cabinets, a native exit sign, bar counters, finite glasses, stools and upholstered benches. Custom wall, floor and speaker textures use Minecraft-like coarse square pixels and subdued palettes. Eighteen painted NPC atlases retain the mod's established style and contemporary club clothing. The original 120-BPM loop has no sampled or licensed recording. Eight distinct dancer designs and a separate DJ retain different faces, hair and club clothing. Split elbows, wrists and knees support shuffle, raised-hands, swaying and mixer gestures.

The mixer cycles normal, soft and silent music. Silence gradually morphs the same saved actors into restrained, hollow facial expressions, a hunched torso, trembling head and dropped arms; feet keep moving. Restarting music resets their synced mood immediately and restores passive or gleeful dancing. This is a shared physical mixer, not a client's volume setting. Joining, reload and revisiting retain the same actor UUIDs and mixer setting. Saved missing originals are never silently replaced.

Decor has actual native actions. Empty-handed clicks take one existing glass from a counter; native item use drinks it and leaves an empty glass; placing it on an empty bar top consumes the held glass and restores an empty physical prop. Another guest cannot duplicate the removed glass, and revisiting does not refill it. The native Adventure allowance is restricted to returning that item to a club bar. Benches and stools mount actual seat entities and permit dismounting. Touching a playing speaker produces a positional bass pulse; a muted speaker stays still. The ordinary return door has proper support and remains usable. There is no completion timer, forced exit, scene damage clock or earned resolution for staying; the room can be revisited indefinitely. This recurring room is intentionally outside the Witness source pool.

Both new focused native cases pass: actual Adventure pickup/drinking/empty return and both seats, and shared music/continued physical dance/despair/restart with original UUIDs, reload, revisit and an open return door. The drinking fixture advances native player-use ticks because mock connections do not dispatch movement packets. All 714 PNGs pass decode and CRC checks; all 132 oriented prop states resolve to native models and existing materials. Four native client views cover cast comparison, motion, despair/return and furnishings. The new GPU views and complete exact-source CI remain pending.

Mod 0.4.88 / protocol 60 / layout 43. Protocol 60 pairs the added registry, glass models, score and synced despair presentation with this server. The first Father checkpoint used protocol 59 / layout 42. The nightclub appends one slot and no source: still 44 eligible sources, 33 required resolutions across two kinds, three endings. Require all twelve exact-source jobs, the complete 567-case native suite, 714 PNG decodes, 91 architecture views, all four nightclub views, both Holy Rabbit views, all four Stanley views and every earlier native/client/socket proof before calling a release verified; do not wait for the full run to keep patching and publishing source batches. The preceding hotel source `6e63f3f439c415b6096df5856ab785707d54388e`, run `38270670659`, passed all twelve jobs, including both actual clients and reconnects; its four captured views match the inspected hotel views byte for byte.

Artwork provenance and prompt directions: [HOLY_RABBIT_ART_0_4_88.md](HOLY_RABBIT_ART_0_4_88.md).

Nightclub artwork and native models: [NIGHTCLUB_0_4_88.md](NIGHTCLUB_0_4_88.md).

The preceding Father checkpoint `ec82d52` completed eleven of twelve CI jobs, including both actual clients and reconnects. Its build job stopped on the old approved hash for `literary_father.png`, before the complete default gameplay corpus ran. This source updates both authorized Father artwork records to the requested Native American Father, retaining the old values as provenance and keeping all other approved hash/alpha checks strict. Full verification remains pending.
