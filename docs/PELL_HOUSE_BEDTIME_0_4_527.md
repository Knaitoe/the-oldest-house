# The Pell house 7: bedtime (0.4.527)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its
street and lot, its rooms, its line, its people, its first visit's day and its first evening (`docs/PELL_HOUSE_DINNER_0_4_526.md`).
This is its seventh slice: the first visit's bedtime, from the proposal. Two parts of it are used here: "Tilly ... has her hair brushed
by June every night", and the older sibling "sleeps apart ... the converted garage across the yard". From "The living room": "June
watches one every night". The night itself is the next slice. The House still does not deal the house.

## What happens

- **The tape.** After the washing up June goes through to the living room, turns on the television and sits in her recliner. A
  birthday tape plays: a cake carried in with its candles lit, a girl in a paper crown close to the lens, then laughing, the candles
  blown out, the tape tearing and rolling to the next year. Its sound is the birthday song's last two lines, sung in a kitchen and heard
  through a small speaker, then a cheer, clapping and a child's laugh.
- **Her chair.** If a reader is sitting in the recliner when she gets there, June says "That's my chair, love." once, and stands
  beside it watching until they get up; then she sits.
- **A hand on the television** while the tape plays finds what is on it, a line at a time and in order, for that reader. A hand on
  June while she watches finds her talking about the girl on it: "That's my Nessa." Then: "Every birthday, a crown. She wouldn't have
  a cake without one."
- **Half past eight.** June turns the television off and calls "Bedtime, Tilly." Tilly comes to the edge of Nessa's bed and sits by
  its head; June sits beside her and brushes her hair, the brush heard slowly. "Hold still for me, love." Tilly: "Dad did it in one go.
  It always came out crooked." June: "Well. I'll do it every night now." And when she has brushed long enough: "There. It'll grow long
  here." Tilly lies down to sleep; June says "Night night, love."
- **Callum** goes to his room when bedtime is called and lies down on his mattress in the middle of the painted circle.
- **Out to the garage.** June goes to the sliding door. If a reader is still in the house she says "Bed, love. Out to the garage, and
  I'll lock up behind you." A reader in the house too far off to hear her is told it, in grey, where they are. If nobody is in the
  house she says "Night, love. I'm locking up." She waits until nobody is in the house, asking again every twenty seconds or so ("Go on,
  love. It's late." / "Garage, love. I want to lock up.").
- **Locking up.** With the house empty, and nobody standing in the doorway, June shuts the sliding door and locks it, then walks
  through the laundry to the back door and locks that, then to the front door and locks it. A lock is heard turning at each. If a
  reader comes back in before she has finished, she stops and waits again.
- **Locked.** From outside, a locked door does not open: a hand on it is told the lock is on the inside. From inside, a locked door's
  lock turns and it opens. After locking up nobody is inside to open one.
- **Her room.** June goes to her room and shuts the door, and the hour goes on from half past eight to ten and waits there for the night.

Nobody is hurt and nothing is forced. A reader can stay in the house; June waits for them, and asks again.

## What each reader keeps

Each reader's record now also keeps:

- whether they saw the tape ("SawTape"; and how many of its lines, "Tape");
- whether they saw June brush Tilly's hair ("SawHairBrushed": in the house and within five blocks of June while she brushes);
- whether they were in the house when June sent them out ("SentToGarage");
- whether they were at the house, outside it, when she locked up ("LockedOut").

## How it is made

- **Steps.** After `CLEARED` come `TELLY`, `BEDTIME`, `BRUSHING`, `TUCKED`, `SENDING`, `LOCKING` and `NIGHT`. The hour holds at half
  past eight from `CLEARED` until `NIGHT`, then goes on to `LATE` (ten, 16000) and holds.
- **Seats away from the table** (`PellVisit.Perch`): June's recliner, and the edge of Nessa's bed for Tilly (by its head) and June
  (beside her, turned to her). A body sits in one where a reader is not sitting (`readerIn`); `unperch` stands them up beside it. The
  recliner and the window seat are seats for readers too now (`PellThing.seat`, nine sixteenths high; the kitchen chairs stay ten).
- **Lying down.** `PellVisit.lie` lays one of them along the bed or mattress at a place: the gesture `ASLEEP`, the hand empty.
  `PellThing.lieHeight` gives each bed's top (Nessa's bed nine sixteenths, the camp bed eight, Callum's mattress four) and
  `PellThing.pillow` its head cell. The renderer lays the body down along it with its head on the pillow
  (`LiteraryRenderers.lieOnPell`, which shares its arithmetic with the vanilla-bed `lieInBed`), and draws the sleeping pose (eyes shut,
  a slow breath).
- **Brushing.** The gestures `BRUSH` (June's right hand up at Tilly's head, drawing the brush down and lifting it, her head bent to it)
  and `BRUSHED` (Tilly's head bowed a little and still), applied after the seated pose (`CastModel.pellSitting`). The renderer sits one
  of the house's people on the edge of Nessa's bed at its height, turned the way they are. June holds the hairbrush.
- **Doors on the way.** Walking, one of the people opens the inside door they come to (Nessa's, Callum's, the laundry's, the
  bathroom's); June her own as well.
- **Locks** are the household's (`Household.locked`); `PellHouse.click` keeps a locked door shut to a hand from outside and turns its
  lock to a hand from inside. When the visit begins again every door of the house is shut and none is locked, the television is off,
  nobody is asleep or perched, and Tilly has her cane again.
- **The television's picture** is its lit front, a strip of seven sixteen-pixel pictures the game plays through (`tv_lit.png.mcmeta`).
  It is drawn by `tools/draw_pell_things.py` and its order is `TAPE_ORDER`. The tape's sound starts again every 170 ticks while it plays,
  and is stopped for anyone near when the television goes off.
- **New art.** June's hairbrush, drawn by `tools/draw_pell_people.py`.
- **New sounds** from `tools/generate_pell_audio.py`: `pell.tape` "A home tape plays: a birthday", `pell.brush` "A hairbrush, slowly",
  and `pell.lock` "A lock turns". The birthday song is the traditional tune; its two lines are sung by synthesized voices.

## Found while building it

- **Textures from outside their picture.** Twenty-seven of the house's things had faces that named no texture rectangle on a part
  standing above the block's top or past its side. The game gives such a face the part of the block's square it covers, which there
  falls outside the face's own picture; it then fills it from whatever lies next to it in the atlas. The television's front was one of
  them: the top half of its screen came from another texture. Among the rest were the recliner's back, the standard lamp's shade and
  pole, the knife block's handles, the kitchen chairs' backs, the stove's back panel and the kennel's ridge.
- **The fix.** `tools/draw_pell_things.py` now gives every such face a rectangle inside its own picture: moved by whole pictures, as if
  it tiled, where that fits; else slid to the nearer edge. The check now fails on any face whose rectangle leaves its picture. The
  television's front is given its whole picture, so the screen, its buttons and its bezel are drawn as the texture was made.
- **Children sank into their seats.** A seated body is lowered by the seat's height, and that drop was measured on a full-sized body;
  but the body is drawn at the actor's own scale, so Tilly and Callum sat with their hips well below the seat, at dinner as well as on
  the bed. The drop is now the seat's height over the actor's scale (`LiteraryRenderers`). Full-sized people sit as they did; any other
  child placed in a seat, such as Movie Night's boy in his lawn chair, now sits on it too.

## Decided here

The proposal gives the beats but not their staging. These are mine, and any of them can be moved:

- **June watches a tape every night after dinner** in her recliner, and the tape is a birthday.
- **The brushing is on the edge of Nessa's bed**, June beside Tilly. What they say while she brushes.
- **"It'll grow long here."** June's line is kind, and it is about Nessa's hair, not Tilly's.
- **Callum sleeps in his circle** on the first night. Standing on the line in the dark comes with the night.
- **June locks the three outside doors from inside and keeps the keys**: the sliding door, then the back door, then the front. The
  garage is not locked; it is the older sibling's room.
- **She waits for the house to be empty** before she locks anything, and asks again rather than forcing anyone out.
- **Ten o'clock** is where the evening waits for the night.

## The case

`PellHouseTests.bedtimeLocksTheDoors`, with two readers, checks:

- after the washing up June goes through to the living room;
- a reader in her chair is asked for it, and June stands beside it with the television on until they get up, then sits;
- a hand on the television finds the tape's first line, and the reader has seen it; a hand on June finds her about the girl on it;
- at half past eight the television goes off and June calls Tilly to bed;
- both sit on the edge of Nessa's bed, June with the brush, brushing;
- Tilly lies down asleep with her cane put down; the reader in the room saw her hair brushed and the one in the garage did not; a
  hand on her finds her asleep;
- Callum lies down in his circle;
- June sends the reader in the house out to the garage, and waits while they stay;
- with the house empty she shuts and locks the sliding door, and from outside it does not open to a hand;
- she locks the back door and the front door, and both readers were outside when she did;
- she goes to her room and shuts its door; the hour goes on to ten and waits there;
- left, the house begins again with nothing locked, the television off, nobody asleep and Tilly with her cane, and each reader keeps
  what they saw.

The evening's and the first visit's cases still hold with bedtime after them.

## Not yet

The night: June out with the tin and the brush, seen only from the garage's window, the singing through the wall, Callum on the line
facing in. Then the morning (breakfast, Gail's call), and everything after.

## Numbers

No layout change. No Witness change. New: one item (the hairbrush), three sounds, an animated picture on the television.
