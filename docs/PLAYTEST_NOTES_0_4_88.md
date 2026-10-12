# October 12 playtest notes — first checkpoint (0.4.88)

This checkpoint implements the first part of the uploaded `This kid begins in the doorway(2).docx`, including the later instruction that the Father be Native American. The other notes remain open. It is not a verified release.

| Note | Current state |
| --- | --- |
| Cottage speech changes and overlapping bubbles | Implemented; actual private-speaker and expiry test passes |
| Chalk inside protected House and string interaction | Implemented; actual Adventure-mode click and unchanged structural protection test passes |
| Holy Rabbit Father, hunger, cold, limp and reveal | Implemented; three native cases pass, including two actual nights and the reachable ending |
| Custom Father and rabbit art | Integrated; native client render checks pending |
| Harrigan's wake after finding him in the chair | Pending |
| Simpler chalk and spool art | Pending |
| Endless dark nightclub with custom dancers | Pending |
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

Mod 0.4.88 / protocol 59 / layout 42. Protocol 59 keeps the new injury and stationary-rabbit client rendering paired with this server. No new source or slot is added in this checkpoint: 44 eligible sources, 33 required resolutions across two kinds, three endings. Require all twelve exact-source jobs, the complete 565-case native suite, 693 PNG decodes, 90 architecture views, both Holy Rabbit views, all four Stanley views and every earlier native/client/socket proof before calling a release verified. The full hotel candidate remains tracked separately at source `6e63f3f439c415b6096df5856ab785707d54388e`, run `38270670659`.

Artwork provenance and prompt directions: [HOLY_RABBIT_ART_0_4_88.md](HOLY_RABBIT_ART_0_4_88.md).
