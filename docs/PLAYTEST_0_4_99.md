# Player checks — 0.4.99

The mod is now **This House Has Ghosts in It**. See [the rename record](RENAME_0_4_99.md)
for the archive and command changes, the completed 0.4.98 Goatman review, and
preserved native world identities.

## Watched lake witch

The missing-cover branch previously planned directly to the target player while
claiming to withdraw. In addition, critical health, a failed-cover fallback and
post-hide emergence could start a full rush under the readers' gaze. The owner
explicitly changed that behavior on October 16: a watched witch follows her
physical escape, and may only give an incidental hand swipe when that actual
swept route passes within one block of a reader.

Both town and literary hunts now respect an eligible reader's gaze. If real
unseen cover is unavailable, a bounded native search chooses a reachable point
farther from the target. It keeps that escape route instead of periodically
replanning toward the player. If no farther point exists, she holds rather than
calling the reader a hiding place. The failed-cover clock cannot force a watched
pursuit; looking away still permits the existing short rush. The ordinary
one-to-three-second occupied hiding interval and its reload/offline semantics
remain. A retreat may naturally pass a reader while reaching actual cover; the
native movement segment, clear sight, refuge rules, one-block radius and existing
single-actor cooldown determine whether that passing hand swipe is possible.

Four added native cases cover both sites' sustained watched, low-health and
saved-emergence retreat, plus a forced physical aisle passing a reader 0.9 or
1.1 blocks away. Existing no-cover/emergence cases now deliberately look away
before expecting a renewed pursuit, as required by the owner's new instruction;
their original identity, native movement, timing, hiding, offline/reload, strike
and peer-credit checks remain.

## Hide and Clap cloth

The old head equipment was the flat inventory sprite, with no fitted worn mesh.
A native layer on both standard and slim PlayerRenderers wraps the actual head:
an eye band, side/back cloth, a rear knot and short ties. It uses Minecraft's
black wool texture and follows the parent's real head transform. The original
inventory icon stays byte-identical; only its HEAD display is hidden to prevent
a second floating sprite over the fitted cloth.

Successful wardrobe opening clears the active game before releasing equipment.
Both the live game and its saved Session are detached before inventory or drop
callbacks: otherwise a full-inventory toss can restore the just-finished turn
and return the original cloth again. Completed rounds cannot rebind equipment.
All temporary owner-bound wrappers are removed from armor, inventory, menu slots
and cursor, the worn head slot is explicitly cleared, and the exact original
cloth returns to inventory or one native drop when full. The original is consumed
from the release record once; both inventory menus update and the completed
client lock/cue state clears. Other equipment and peer items remain exact. The
failure reveal/death handling remains authoritative.

Two new native cases actually count, follow the saved clap positions, create the
real wardrobe and open it through the production interaction. They check normal
and full inventories, exact custom components, another player's failed opening,
head/cursor cleanup, peer equipment, one returned original, repeated clicks,
SavedData reload and the login/player-tick paths without rebinding. Native GPU
views add both player head types from front/side/back and crouched head turn,
then empty equipment; the same production cloth mesh/transform and native skin
renderer registrations are checked. These staged fixtures do not certify an
uninterrupted ordinary survival playthrough.

## Validation

The declared native corpus is 648 cases (642 previous, four witch cases and two
cloth success cases). All twelve exact-source jobs, all earlier GPU and 97 native
architecture views, both original socket clients and reconnects, the new worn/
removed head captures, renamed JAR metadata and twelve decoded positional contacts
remain required. Exact-source validation is pending for this combined candidate.
All original PNG bytes, finite inventories, native identities, personal outcomes,
46 sources / 35 required / two kinds / three endings, protocol 72 and layout 45
remain. Never substitute the previous green 0.4.98 result for this new source.
