# October 13 playtest follow-up — 0.4.90

Development checkpoint. Full exact-source validation is pending.

| Note | Implementation | Validation |
| --- | --- | --- |
| Tess swimming | Head-first transform, coordinated stroke/flutter kick, native collision routes, entrance staging and physical departure | All 13 lake cases pass, including the original Tess swimming through the door and up the stair |
| Ghosts staging | Actual floor/room gate, idle dialogue, three-dimensional meals, modern stair/rail meshes, wall closure and east-facing producer monitor | Compiles; native rerun pending |
| Ghosts performances | Prayer, help/struggle, heavy native fall/impact, camera shake, teenager scale, full jaw UV, eyelids and varied final death poses | Compiles; native and GPU proofs pending |
| Ghosts sequencing | Private pitch eases down; original crew walk out before poison; original sisters climb the actual stairs | 19 Ghosts cases pass; the two stair-route cases exposed an armchair collision, corrected and awaiting rerun |
| Father, Son, Holy Rabbit | Repeated skin and wet trouser clues; crossed bloody tracks; visible original father retreats on two wooden poles; search-light/radio cue | Existing finite meals, carving and original rabbit retained; native tests pending |
| Goatman cooking | Saved rotating idle chatter continues while real finite franks are prepared | Compiles; native regression pending |
| Nightclub | Whole-room loop, authored irregular pairs, dim native lighting plus coloured beams/pools, local DJ lights, dedicated painted face mesh and custom EXIT lightbox | Compiles; native and GPU proofs pending |
| Harrigan | Reading interjections, original props, wake surfaces/casket and legacy phone | In progress |

Painted Tess remains 18, pale-skinned, dark blue eyes, light freckles and long straight black hair. Nine client hair joints drift outside underwater and become rigid inside; there is no server physics simulation. New art is additive: every previous PNG remains byte-identical. Saved architecture repairs wait for loaded sections and actual cameras/bodies, and only alter expected blocks.

Previous checkpoint `0acb23a` passed literary gameplay and nine other CI jobs. Structure and live multiplayer failed; neither a compile nor a focused test pass constitutes a release.
