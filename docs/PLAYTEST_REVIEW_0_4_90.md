# October 13 playtest follow-up — 0.4.90

Development candidate on `feat/playtest-notes-0.4.88`. Full exact-source validation remains pending.

| Note | Implementation | Validation |
| --- | --- | --- |
| Earlier Zampanò/fountain scenery batch | Dense wall using the original handwriting, custom stone fountain bowl and animated blue jet/overflow; supported camp bedrolls/headroom, plains field case/noon, ceiling-only institute fixtures, chalk and string meshes | Included from the pushed 0.4.88 scenery source; four native scenery cases retained |
| Tess swimming | Head-first transform, coordinated stroke/flutter kick, loaded native collision routes, entrance staging and physical departure | Thirteen native lake cases, including the original Tess through the door and up the stair |
| Ghosts staging | Actual floor/room gate, idle dialogue, three-dimensional meals, modern stairs/rails, wall closure and east-facing producer monitor | Twenty-one native Ghosts cases; the upper route goes around the actual stair backing and opens its native door |
| Ghosts performances | Prayer, help/struggle, heavy native fall/impact, camera shake, teenager scale, full jaw UV, eyelids and varied final death poses | Native gameplay cases retained; GPU inspection pending |
| Ghosts sequencing | Private camera pitch eases down; original crew walk out before poison; original sisters use the physical stairs | Actual route, departure and take checks retained |
| Father, Son, Holy Rabbit | The original rabbit is named Slaney in fresh runs; once-only dialogue builds belief after the actual returning rabbit and consumed meal, then explains the food only after the witnessed wound; questions change with the player's saved knowledge | Actual two-night case now requires belief before truth; existing finite meals, carving, trail and personal resolution retained |
| Father's final hunt | Same father visibly leaves on two wooden poles; repeated skin, wet trousers and crossed bloody tracks precede rescue | Native retreat must finish before the escape-freezing clock advances |
| Goatman cooking | Saved rotating idle chatter continues while finite franks are prepared; fresh long table uses the existing formica mesh | Native cooking, departure and preservation cases retained |
| Nightclub | Room-wide music, irregular dancer groups, dim native lighting plus coloured beam/pools, DJ key lights, dedicated painted face mapping and custom EXIT lightbox | Native cases retained; GPU inspection pending |
| Harrigan reading | Commentary starts only with the actual lectern reading; an owner-checked short client lease holds the current line on the reading screen | Native reading pause/resume, page ordering, dismissal and reload checks retained; client appearance pending |
| Harrigan originals | Original prop-frame UUIDs survive repeated staging; the actual displayed spare phone transfers its components; an earned legacy blank phone is repaired in place once | Native finite ticket, phone ownership/component and repeated-staging checks extended |
| Harrigan wake | Original corpse remains behind the closed funeral door, with a fitted padded casket; new burgundy carpet/damask above existing wood panelling | Native original-body and phone-choice checks retained; actual casket model/pose included in the required NPC GPU proof |

Painted Tess remains 18, with pale skin, dark blue eyes, light freckles and long straight black hair. Nine client spring joints drift only outdoors underwater and become rigid inside the house or on land. There is no server hair simulation.

Zampanò's sixteen manuscript images are the existing painted handwriting, remapped onto the dense wall rather than newly repainted. Every PNG from the newest pre-integration source `d57b179` remains byte-identical. The two new 256-pixel funeral surface tiles are additive originals generated for this pass: subdued burgundy damask wallpaper and worn burgundy floral carpet, without lettering, figures or logos. Native casket geometry reuses the existing dark oak and pale lining materials. Saved architecture changes wait for loaded sections and actual cameras/bodies and preserve unexpected edits and block entities. Lost or removed original actors, props and books are never replenished.

Mod 0.4.90 / protocol 69 / layout 44. The existing pool remains 45 eligible sources / 34 distinct personal resolutions across two kinds / three endings. 597 declared native cases, 764 PNGs, 148 Ghosts states and 168 nightclub states. All resource JSON parses, all PNGs decode and all 742 pre-existing PNG hashes match.

All 76 selected native cases passed in the recovered follow-up: Ghosts, lake house, October playtest/scenery, nightclub, Harrigan, Goatman and streetlights. The two-night case includes the new belief-to-revelation assertions. Final source compile and JAR packaging passed, as did all eight font grids in both source and packaged JAR, texture decode/CRC, resource JSON, generator syntax and whitespace checks.

The previous `d05d942` checkpoint passed seven of twelve CI jobs. Its two Ghosts stair-route failures and premature final-hunt freeze are corrected here. The broad structure job, trailer client proof and actual multiplayer bed crawl remain unverified. All twelve exact-source jobs, actual GPU views and client/socket/reconnect checks are required before calling this a verified release. A focused pass is not full release verification.
