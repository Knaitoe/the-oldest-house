# Not Yet 1: the ward (0.4.663)

The first part of *Not Yet*, the third vignette in `docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`. The owner approved the proposal on 17 November ("They read well ... begin work on implementing the vignettes"), and it is built to the proposal's recommended decisions.

This part is the place alone. A passage of the machine's own casing runs over a hatch. Past it is a ward of cots under lamps, with a painted window and a counter. Off the ward is the mending room, with its table under a ring of arms. Past the ward is the long room, where three people the machine has mended too often lie in their cots with their eyes open. The machine is the Kept's, deeper in.

Nothing happens yet. The hatch stays shut, the screens stay dark, the counter stands at nought, and the far door stays shut. The House does not deal the place until part 3, when a reader can lie down.

## The parts

The proposal's build is four parts:

1. **The ward** (this release, 0.4.663): the place and the three people.
2. **Refused** (0.4.664): the fall through the hatch, death refused, the mending on the table, NOT YET on the screens, waking on the reader's own cot, the seams, and the counter.
3. **Lie down** (0.4.665): the invitation, the trial and its five provocations, and the far door.
4. **After** (0.4.666): the cot made, the account, the Witness story, the stair's company and the shared accounts.

## The place

All positions are relative to the House's door, which is at z 0 facing north.

- **The passage** (x -2 to 2, z 0 to -8, three high). The machine's casing, grating underfoot, cable overhead, two strip lamps, and cable runs down both walls.
  - Nine of its floor plates (x -1 to 1, z -4 to -6) are a hatch, shut.
  - Under the hatch is a shaft ten blocks deep in the casing, with grating at its foot and a little rime on its walls.
  - A doorway three wide leads into the ward.
- **The ward** (x -9 to 9, z -10 to -44, seven high). It has its own tiled floor, a darker gloss dado to waist height, and painted wall above. The ceiling is the machine's cable.
  - **Cots.** Fifteen in two rows: eight against the east wall and seven against the west, where a gap is left opposite the mending room's doorway. Each is a frame of steel tube with a thin ticked mattress, a pillow and a blanket. The head stands against the wall with a drawer under it. A screen hangs on the wall over each head, dark, and a lamp on a jointed arm over that, off.
  - **Curtains.** These hang on rails of cable between the cots, bunched at the wall.
  - **Lights and air.** Strip lamps run down the middle, and there are vents low in the long walls.
  - **The chart** is on the wall by the door.
- **The window.** It runs down the east wall, three rows high, from z -14 to -38. It shows the machine's copy of outdoors: flat bands of sky, the one cloud it knows three times over, and a sun, two by two, that does not move.
- **The counter.** Seven windows of turning digits stand over the arch into the long room. Each digit is drawn by each reader's own game, and stands at nought in this part.
- **The mending room** (x -19 to -12, z -23 to -33, three high), through a doorway three wide in the ward's west wall.
  - The mending table, steel on a pedestal with straps, stands under a ring of six arms folded up under the ceiling. Their fingers are many and fine.
  - Cabinets and cable stand along its west wall.
- **The long room** (x -9 to 9, z -46 to -62, six high), through an arch five wide.
  - The same tile and walls, lower. Three cots to a side.
  - Three of the cots are taken: west at -49, east at -54, west at -59. Their lamps are on, their screens show noise, and their curtains are drawn beside them.
  - Vents are rimed over.
- **The far door.** A steel leaf sliding in its frame in the long room's end wall, shut. Past it is a small room in the casing with the way on.

Twenty-one beds in all, and three kept. The chart's count is the ward's own (the test checks one against the other).

## The three

The three are on stranger phases 240 to 242. Each lies on their back in their cot with their eyes open, the head on the pillow at the wall and the hands at the sides. They breathe, and nothing else of them moves. Each carries the seams of their mending: a raised ridge with a lighter lip and darker stitches across it, one every third pixel.

- **Ines (240).** A seam across her brow and another down her cheek; a lengthwise seam down her left forearm; a ring round each wrist.
- **Bram (241).** A seam up from the hairline at the crown and one across his cheek; a ring round each wrist and another round each arm below the elbow; stubble.
- **Odile (242).** A seam along the hairline, sewn more than once, and one down her temple; a ring round each wrist. She is the eldest.

All three wear the ward's gown, and none is named in the place yet (part 2 gives them their words). Each is painted by `tools/paint_character_skins.py` (`literary_notyet_ines`, `_bram`, `_odile`) on the cast's grid. The art check holds them to the catalog.

They lie in cots, which are beds to the game. The game's rule that gets up a sleeper whose bed has gone is not theirs: a `CanContinueSleepingEvent` listener keeps them lying, as the King's does. They are laid out on the mattress by the renderer (`LiteraryRenderers.lieOnNotYet`) from the cot's own facing, whatever way the body turns. They are drawn with no eyelids, so their eyes stay open. Each is made in their cot only if their body is not already there, and nothing is made while the long room's entities are unloaded, so there is never more than one of each.

## Single and multiplayer

The ward is a recurring place. In this part every reader sees the same three in the same cots. A hand on anything answers that reader alone, in a grey line over the hotbar, and changes nothing.

From part 2 the per-reader things begin: a reader's own cot, their own counter, their own seams, the names the machine calls. The proposal's multiplayer and single-player paths (the names called are other readers' in a group and lost companions' when alone; the machine's lie, "They got up") are part 3's.

## Made as

- **The place.**
  - `LabyrinthPlace.NOT_YET` ("not_yet", recurring, slot 243), bounds (-20,-12,-67) to (12,8,0), and `LabyrinthBuilder.VERSION` 141.
  - `NotYetSite`: the plan as constants, the build, and where a body is.
  - `NotYetRegistry`: three materials and the fitting block.
  - `NotYetBlock`: fourteen kinds of fitting, with stages. Cot and cot foot; curtain (rail, bunched, drawn); lamp off and on; screen (dark, the two words, the door open, someone in it, noise); counter; table and table foot; arm (folded, let down, working); hatch shut and open; vent still and rimed; window (sky, three parts of a cloud, four quarters of a sun); door (shut and open, lower and upper); chart.
  - `NotYet`: the three, a hand on anything, never dealt.
  - `NotYetTexts`.
- **Art.** `tools/draw_not_yet_art.py` draws 50 textures, models and states at sixteen pixels, with `--check`. The machine's own casing, grating, cable ceiling, strip lamps, cabinets and cable runs are the Kept's.
- **Sound.** `tools/generate_not_yet_audio.py` makes the ward's hum and one turn of the counter, from noise, both subtitled. The door bleed (`bleed.not_yet`, `tools/generate_door_bleed_audio.py`) is that hum, with the counter turning twice through a door.
- **The House's tables.**
  - The literary places.
  - The source (the chart) and the ending.
  - The miniature.
  - The staircase name ("the machine's ward").
  - A shared-account line.
  - The way on (a service door).
  - The door bleed (a steady pale light).
  - The deal gate (never, until part 3).

## Checked

`NotYetTests.theWardTheMendingRoomAndTheLongRoom` (the new `not_yet` namespace and job):

- **Materials:** the passage's casing, grating and cable; the hatch's nine plates, shut, over the shaft and its grating; the ward's tile, dado, wall and ceiling.
- **Fittings:**
  - all twenty-one cots, each with its foot, screen and lamp;
  - the counter at nought;
  - the whole painted window and its sun;
  - the table and its six arms, folded;
  - the long room's screens, lamps and curtains, by which cots are taken;
  - the far door, shut;
  - the chart.
- **Walking:** from the House's door over the shut hatch, the ward end to end, the mending room through its doorway, and the long room through the arch. Nothing past the far door and nothing down the shaft.
- **The three:** they lie in their cots, one of each however often they are looked for. Each sleeps on their cot's head. With a reader beside them for two seconds, they lie on unmoved.
- **Lines and paper:** every thing answers a hand with its own line, and so do the three. The chart counts twenty-one beds and three kept, matching the ward as built. The chart says nothing of what anybody is. The account's title fits a book.
- **Not dealt.**

Also:

- `ArchitectureTests`: the ward lays no House rug (its own tile, the machine's grating, the chart), and its source surface is the chart. Architecture 6/6.
- `LiteraryTests`: a place still being built is never dealt and is no source. The literary suite passes.
- The tool checks: art, audio, door bleed, the painter and the art catalog.

## New lines (for the owner)

All in `NotYetTexts`, all new:

- Hint: "There is a chart on the wall by the door, in the machine's hand."
- Cot: "A cot made up tight. The sheet is turned back exactly the width of a hand."
- Cot foot: "The foot of the cot. The blanket is tucked in so hard it would take both hands to pull it loose."
- Curtain: "A curtain on a rail of cable. It is heavier than cloth and does not quite hang straight."
- Lamp: "A lamp on a jointed arm, bent down over the pillow as if to look." / on: "A lamp on a jointed arm. It is on, and bent down close over the pillow."
- Screen: "A screen at the head of the cot. It is dark, but something behind it hums."
- Counter: "A wall of turning digits, seven to a row. Every one of them stands at nought."
- Table: "A table under a ring of arms. The top is warm, and worn into a hollow the length of a body."
- Arm: "An arm folded up under the ceiling. Its fingers are many and fine, like a watchmaker's tools."
- Hatch: "A plate in the floor, set a little apart from the plates round it. It rings hollow."
- Vent: "A grille low in the wall. Cold comes through it, and a smell of nothing at all."
- Window: "A long window onto a field under a sun. The sun has not moved. The clouds are painted on."
- Far door: "The far door. It has no handle on this side, only a seam down the middle." / open (unused until part 3): "The far door stands open."
- Ines: "She lies on her back with her eyes open, looking at the ceiling. A seam runs across her brow. She breathes, and nothing else of her moves."
- Bram: "He lies the same way, his hands at his sides. There is a seam round each wrist, and another round each arm below the elbow."
- Odile: "Her eyes are open and follow nothing. The seam along her hairline has been sewn more than once."
- The first paper, "The ward's chart", by the machine, in its capitals:
  - "WARD. / BEDS: TWENTY-ONE. / KEPT: THREE. / GOT UP: ALL THE REST."
  - "THIS IS WHERE I MEND WHAT I BREAK. / I BREAK A GREAT DEAL. I HAVE BECOME VERY GOOD AT MENDING."
  - "NOBODY DIES HERE. I HAVE SEEN TO IT."
  - "BED ONE IS MADE. / I HAVE PUT A NAME ON IT IN PENCIL. I WILL WRITE IT IN INK WHEN YOU LIE DOWN."
- The account (unused until part 4): "There was a ward deep in the machine, with cots in rows under lamps, and a window onto a field where the sun did not move. A wall of digits stood at nought. Beyond it, in a long room, three people lay with their eyes open."
- Miniature: "A row of cots no longer than a matchstick under lamps bent close as if to look, a window painted with a field, and at the end of the room a table under a ring of arms as fine as hairs."
- Shared account: "%s was in the ward with me, in another cot."
- Sound subtitles: "The ward hums"; "A counter turns over"; door bleed: "A ward hums and a counter turns over, behind a door".

Gender appears only in the three people's own lines (she, he, her), as the place shows them. The chart, which a reader reads, says nothing of what anybody is.
