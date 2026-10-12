# October 12 additional playtest notes

The additional `This%20kid%20begins%20in%20the%20doorway.docx` was read in full, including all nine rendered pages and its screenshots. These requests supplement the [first notes](PLAYTEST_NOTES_0_4_88.md). The unfinished Holy Rabbit shelter sentence was clarified: the owner selected a shelter beneath a tree.

| Request | Status in this source batch |
| --- | --- |
| Mirror mobs remain stuck | Fixed the native empty-dimension entity timeout while actual observers remain; native regression passes beyond fifteen seconds |
| Holy Rabbit father and son beneath a tree | Confirmed; camp dressing remains pending |
| Hotel NPC texture alignment | Corrected six-skin set from 0.4.87 is retained; current cast screenshot has complete faces; compare against an updated in-game build |
| Room 217 will not open | Added the reader's displayed 217 as an actual entrance into the hotel; other private plaque numbers remain locked |
| Floating props and stacked boxes in the child's room | Pending geometry/support repair |
| Ghost Set source should look like a call sheet | Pending custom surface art; preserve the finite original book |
| Marjorie's text should rumble and roll | Pending |
| Different meals across visits, ending with spaghetti | Pending |
| Day six frozen jump, head turn toward camera, then forced departure | Pending; keep each reader's filming day private |
| Improve all Ghost Set art, animation, house construction and interactions | Pending |
| Plains: unique note storage and daytime arrival | Still pending from the first notes |
| Fan: visible blade spin, flickering light and repair | Pending |
| Fan toolbox: actual lightbulb, not planks | Pending |
| Five seconds on the fan: knock off, return outside and complete sequence | Pending |
| Floor tape must trace the deer silhouette | Pending |
| Club: brighter red, blue, green and rare strobe | Integrated: six lower hanging fixtures, brighter steady strips, native full-bright panel faces and three brief flashes every 130 seconds |
| Harrigan wake from the first notes | Integrated: same original corpse moves behind the closed funeral door before it opens; three original guests can be addressed |
| Goatman plates after departure from the first notes | Integrated: last actual enrolled departure clears native serving displays and stale flags; peers and carried food retain their originals |
| Simpler chalk and spool artwork | Still pending |
| Institute: yellow ceiling lights only | Still pending |
| Courtyard fountain texture and proper water spout | Still pending |
| Zampanò's wall covered in his writings | Still pending |

Room 217 uses the authoritative per-player loop count to resolve the actual number on that door. The same shared door remains a locked resident room for a reader seeing 201. Clicking 217 enters the existing furnished hotel, with its unchanged personal tab, rest, belongings, photograph and closing account. No shared door registration is rewritten. The ordinary upstairs bedroom door still uses native opening; the service door retains its actual key.

Source worlds with observers across the manor boundary reset their native empty-time counter. Interior source leases also reach entity-ticking ticket level. This permits the original mobs to keep ticking; projections remain invulnerable, non-interactive, without AI and without ownership authority. Leases and projections release after the last observer leaves.

The wake preserves Harrigan's body UUID through life, death in the study and the casket. Its three quiet guests each have a distinct painted face and closed formal clothing. The first two art passes enlarged their UV islands and were rejected; the final pass used only the original Harrigan layout as its edit target. The native player mesh and actual armor-stand poses render these guests. Their own saved UUIDs are never replaced if missing or unloaded. Opening the wake alone does not grant an ending: the original phone's kept/buried choice and final reading remain authoritative. [Artwork and exact prompts](HARRIGAN_WAKE_ART_0_4_88.md).

Club fixtures now use separate red, blue and green states. The lower pendants have visible chain support and light the dance floor at native level 15. Neon panel faces use NeoForge's actual `neoforge_data` light fields. Two strobe housings produce only three 100 ms flashes, separated by 400 ms, once every 130 seconds; the steady coloured light remains on. Silence stops the flashes. Existing club fixtures migrate once while vacant; player edits, finite glasses, music setting and original actors are retained.

Mod 0.4.88 / protocol 62 / layout 43. The native suite now declares 571 cases; all 717 PNGs decode and all 144 club states resolve. All six focused new/updated native cases pass locally in 36 seconds: wake, reader-specific 217, mirror idle timeout, Goatman last departure, finite club glass/seats and reversible shared dance/despair. Full exact-source CI and native GPU inspection remain pending. Sources remain 44, required resolutions 33 across two kinds, with three endings. Continue source batches after quick checks without waiting for full CI.

The preceding source 552772 passed nine of twelve workflow jobs. Two old server assertions still expected the pre-nightclub recurring pool and slot total; the client architecture assertion still expected 90 rather than the actual 91 views. Their exact expectations are corrected here. The actual multiplayer run stopped at its server watchdog in phase six and remains unverified; it is not counted as passing.
