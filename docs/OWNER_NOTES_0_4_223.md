# The owner's notes of 1 November, second batch (0.4.223)

Screenshots and a video from a playthrough: the waiting room's glasses, a band across the eyes in a later mirror, the hall
windows, the trim and the cracks, the Winchester's hall stand, chairs and piano, the manor's family portraits, and Block Party.

## The waiting room's reading glasses

The owner's video: "what is that intended to be/do?" The object on the waiting room's low table is a pair of reading glasses.
Using them puts them on (the House lends them into the head slot and takes them back when the reader leaves the room), and the
House counts it toward its read of the reader's age, toward an older read. Nothing showed it, so they seemed to vanish.

- Putting them on says so: "Someone's reading glasses. Close up, the print sharpens; the rest of the room goes soft." Taking
  them off: "The glasses go back on the table."
- While they are on, their rims sit out of focus at the edges of sight, like a pumpkin's mask (`client/ReadingGlassesView`, the
  picture drawn by `tools/draw_house_read.py`).

## The glasses in the mirror

"Weird glasses overlay? Or carry over from character skin": it is the House's version of the reader, which later mirrors show
in reading glasses after an older read. The frames were near-black and covered both pupils, so they read as a band across the
eyes. They are thin gold wire now: a rim over each lens and a bridge across the brow row, the lenses open over both eyes, and
arms back to the ears (`specs.png`, `HouseReadClothes` glasses with two arm cubes).

## The ordinary halls

- **Windows.** "Can you fix the windows in the hallway?" A window was laid on any wall block eleven along, corners included, so
  its frame wrapped round two faces; and on an alcove's end wall the dark stone laid behind one window took the next window's
  place, a grey block in the row. A window now stands only in a straight run of wall, with the dark behind it out of every
  room's sight, and on an end wall only every other block (`HallVariations.window`).
- **Trim.** "The trim you added isn't applied evenly." The skirting replaced only the wood laid at a wall's foot, so the
  terracotta beside doors stayed bare, and a household room's wall facing the passage was skipped entirely, rail and all. Every
  wall's foot now has its skirting, and a household room's plain wall facing the passage is dressed like the rest of it
  (`HallFinish.dress`).
- **Cracks.** "Wall fissures require more variance." One crack was drawn, edge to edge, so it repeated and was cut off at each
  block's edge. There are five now, each starting and ending inside its own block: a long hairline with a spur, a diagonal from
  a corner that forks, a settlement crack stepping down like brickwork, a star round a chipped point, and two short parallel
  hairlines. Each cracked block picks one by where it stands (`tools/draw_hall_art.py`).

## The Winchester

- **The hall stand's mirror** ("this should utilize the mirror system we designed") is a true plane mirror, the quiet rooms'
  and the bathroom dare's: the hall and whoever stands in front of it, the reader as the House's version of them
  (`WinchesterClockRenderer.mirror`, the hall stand's top now has an entity).
- **The chairs and the settee** ("the chair should be sittable") seat a reader facing the way they face: the dining chairs, the
  wing chairs, the settee and the séance chairs. Nobody sits where one of the house's people already is.
- **The piano** ("the piano should be playable"): each key sounds where the hand falls, two octaves across the piano's two
  halves, the low notes at the left of whoever faces it, and a note rises off the keys (`WinchesterFittingBlock.note`).

## The manor's family portraits

"Improve the paintings here, bring them in line with the character-captured portraits. Character captured portraits should
use their house-overlays."

- **The family.** The five portraits were hand-placed blocks of colour. Each is now a member of the household painted by the
  cast's common painter (a father in his work jacket, a mother in a cardigan with her hair down, their child, the grandfather in
  his suit, and the woman nobody in the family can name, in her dark dress) and then put through the same bust the captured
  portraits use, over its old ground (`tools/draw_manor_portraits.py`, `--check` in the quick job; recorded in
  `art/inventory/manor_portraits_04223.json`, superseding only those five historical hashes).
- **Captured portraits.** A portrait taken in the House keeps the House's version of the reader at the death: the House's body
  (age, presentation, cut, tone, hair, the face drawn at the kitchen table) and what the glass shows them in (coat or outfit,
  reading glasses, an obscured face). The client paints it from the House's body in place of the skin, with the coat or outfit
  over the shoulders and the glasses or veil on the face (`PortraitProfile.look`, `DeathPortraitClient.house`). The bust reads a
  skin at more than the vanilla scale at up to twice the detail, as the family portraits are. Portraits claimed before keep
  their skin alone.

## Block Party

"Rendering is messed up on brawler." Turned to the left, a body was drawn mirrored, which turns its faces inside out, and the
screen culled them: everything walking left was invisible but its shadow. The mirrored drawing is no longer culled. The zombie's
picture has no left arm or leg of its own (the game mirrors the right ones), so those were drawn blank; they are the right
ones now. And a zombie or creeper walking on the diagonal came to stand inside its fighter, hiding them; it now comes up beside
them, a fist's length off on its own side.

The owner's further asks (classes, combos and items; a pizza and soda counter with a sullen teenager) are the next release.

## Not changed

No layout, protocol or Witness change. New builds only.

## Tests

`ExplorationPassTests` checks that every domestic hall's walls have their skirting, that every window stands in a straight run
of wall and that no window's dark back shows. The Winchester case checks the hall stand's mirror entity, that the settee is a
seat, and that the piano's keys run low to high from the left of whoever faces it. The client portrait proof paints a portrait
from the House's version of the reader and checks it differs from the skin's.
