# The Pell house 8: the first night (0.4.530)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its
street and lot, its rooms, its line, its people, its first visit's day, its first evening and its bedtime
(`docs/PELL_HOUSE_BEDTIME_0_4_527.md`). This is its eighth slice: the first night, from the proposal's "1. The placement":

> At night: the older sibling sees June out with the tin; Tilly hears singing through the wall; Callum stands on the line in the dark,
> facing in.

Two more parts of the proposal are used here. From the older sibling's part: "Sees June out at night, walking the line with a tin and a
brush. The garage window is the only window that looks onto that stretch." And from Callum's: "At night he stands on the line." The
House still does not deal the house.

## What happens

- **Eleven.** Once the house is locked up and June is in her room, the hour goes on to eleven, three times as fast as it did by day.
- **Out.** At eleven June comes out of her room with a battery lantern and goes to the back door. She unlocks it, goes out, turns, and
  locks it behind her with her key. A lock is heard each time. From outside, a hand on the back door now finds "The back door is locked.
  June has the key with her."
- **The shed.** She unlocks the shed's padlock, goes in, and takes the tin marked LINE and its brush from the bench. She comes out and
  padlocks the shed again. While it is open, a hand on its door finds the padlock hanging open on its hasp.
- **The strip.** She goes up the passage between the shed and the garage to the strip behind the shed, where the line is thinnest. At
  its end by the passage she sets the lantern down on the grass, and it lights the strip. She sets the tin down beside her, opens it,
  and kneels with the brush.
- **The line.** She paints the thin line again, cell by cell, from the passage end westward. The brush is heard in strokes. Each cell
  is left wet. Between cells she gets up, takes the tin in her other hand, moves along, and sets it down beside her again.
- **The paint smears.** A foot in the wet paint drags it, and the reader keeps that they did it. June, still out and near enough to
  hear, says "Oh, love. Mind the paint." once. When she has done the stretch she goes back and paints over any smears.
- **A reader in her way.** If a reader stands on the cell she is about to paint, or where she kneels for it, she says "Feet, love. I'm
  doing that bit." once, and waits.
- **Back in.** She taps the tin's lid back on, takes up the tin and the lantern, and goes back down the passage. She puts the tin back
  on the bench where it always stands and padlocks the shed. She comes in by the back door and locks it behind her, then goes to her
  room and shuts the door. The hour goes on to midnight, and the night waits there for the morning.
- **Talking to June.** Out with the tin, June says "Can't sleep, love? It's only me." to the first reader who comes near her, once a
  night. A hand on her while she is up gets her night lines, in turn:
  - "You should be asleep, love. It's late."
  - "Just going over it. It wears, out here."
  - "Mind the paint. It's still wet."
  - "Biscuit needs it bright. He's used to it."
  - "Go on, back to bed. I won't be long."
- **Doors behind her.** Wherever June shuts a door behind her, she waits until nobody is on the far side of it or in its way, and asks
  anyone who is to come out:
  - a reader who slipped into the house while she was going out: "Out you come, love. Not tonight.";
  - a reader in the shed: "Out of there, love. I'm locking up.";
  - a reader who followed her back in: "Back out you go, love. Bed."
- **Callum.** Once June is out, at the first moment nobody can see where he lies or the place on the line, Callum is standing on the
  line by the drive, short of the garage, barefoot, facing in. He stays there through the night.
  - **A beat late.** He copies the reader nearest him, within twelve blocks, a beat (twelve ticks) late. He crouches when they crouch
    and stands when they stand. While they look at him, he looks where they look, as a reflection would. When they look away, he looks
    at them.
  - **A hand on him** finds, in turn: "Callum is standing on the line, barefoot, facing the house. His hair is wet."; "He doesn't move
    for your hand."; "The grass round his feet is dry."

Nobody is hurt and nothing is forced.

## What each reader keeps

Each reader's record now also keeps:

- whether they saw June out of the house tonight ("SawJuneOut");
- whether they saw her painting ("SawPainting");
- whether they saw either from inside the garage ("SawFromGarage");
- whether they saw Callum on the line ("SawCallumOnLine");
- whether they smeared the wet paint ("Smeared");
- whether they spoke to June while she was up ("SpokeToJuneAtNight");
- whether they put a hand on Callum on the line ("TouchedCallumAtNight").

Seeing is the reader's own sight:
- within forty blocks, and within sixty degrees of where they look;
- with nothing in the way but clear glass, or near enough to touch.

The garage's window is how the strip is seen from inside the garage.

## How it is made

- **`PellNight`** has the night. `PellVisit.evening` hands the house to it once June has locked up (`NIGHT`).
- **The steps** after `NIGHT`: `ROUNDS` (out of her room and out by the back door), `TO_SHED`, `TO_LINE`, `PAINTING`, `PACKING` (back
  to the shed), `HOMEWARD` (in and to her room), and `SMALL_HOURS`, where the night waits.
  - Within a step, `Household.waypoint` counts what she has done so far: unlocked, out, turned to the door.
- **The hour.** At `NIGHT` it goes on at `NIGHT_PACE` (three ticks of the day to a game tick) to `ELEVEN` (17000). From `ROUNDS` it goes
  on at one to `TWELVE` (18000). The client keeps the sky moving between packets at the rate it is sent (`PellClient`).
- **The walk.** June walks the yard, the shed and the passage with the shared footpaths (`SceneFootpaths`), and opens and shuts the
  back door and the shed's door herself (`PellHouse.toggle`). The back door is a lock of the household's (`Household.locked`); the
  shed's padlock is heard but kept nowhere, since only June opens it.
- **Her things.** In her hands they are items:
  - the lantern (`pell_lantern`);
  - the tin (`pell_tin`);
  - the brush (`pell_paintbrush`).

  Set down, they are things (`PellThing`):
  - `LANTERN`, which gives light 12;
  - `TIN` at stage 1, open on the grass.

  The tin on the bench is `TIN` at stage 0 as before, taken away while she has it.
- **The paint** is the line's own stages (`PellLine`): a painted cell is `LAST` (wet), a smeared one `FOOTPRINT`. The thin line's art
  for both was drawn in 0.4.523.
- **Her pose.** The gesture `PAINT`: kneeling up on the grass at the line, bent forward from the hips over it. Her left hand is flat on
  the grass, and her right draws the brush along the line and back. Her body bends from the hips, not the neck, so her head and
  shoulders come forward over the paint. A leg is one piece and cannot fold at the knee, so her shins lie inside the ground where her
  knees rest on it.
- **Callum's pose.** The gesture `CROUCH`: the game's own crouch.
- **Callum's copy.** Each tick the nearest reader's look, pitch and crouch go into a ring of `BEAT`+1. Callum takes the one a beat old:
  - **Look.** How far their look is turned off him. Within a quarter turn, his head turns the other way by as much, from facing them.
  - **Pitch.** Their pitch is his.
  - **Crouch.** Their crouch is his gesture.

  His body stays facing in. His head turns no further than a head turns.
- **Unseen.** Callum is moved to the line only when no camera sees him or the place (`LiteraryVignettes.watched`), and no reader
  stands there.
- **When the visit begins again** (`PellNight.restart`):
  - the thin cells go back to the first visit's paint;
  - the tin is on the bench, and nothing is left on the strip;
  - June's other hand is empty;
  - Callum is in;
  - the shed's door is shut with the house's others (`PellVisit.ALL_DOORS`).
- **New art.** Drawn by `tools/draw_pell_things.py`:
  - the lantern set down, its globe giving its own light so it shows in the dark;
  - the open tin, the paint in it.

  In June's hands, from `tools/draw_pell_people.py`:
  - the brush, drawn flat as the hairbrush is, its tip wet;
  - the lantern and the tin, which hang from her hand by their handles, upright. They are built from the things' own block art: the
    lantern is the one she sets down, its globe lit; the tin is the one from the bench, its lid marked LINE, with its wire handle up
    and a wooden grip on the wire.
- **New sounds** from `tools/generate_pell_audio.py`:
  - `pell.paint`, "A paintbrush, in strokes": a wide brush drawn along the ground and back, wet;
  - `pell.tin`, "A paint tin's lid": levered up with a dull ring, or tapped back down at a lower pitch.

## Found while building it

- **Nothing in June's hands was ever drawn.** The layer that draws what the cast holds drew it only for Tilly and Gail. So June's
  saucepan at dinner (0.4.526) and her hairbrush at bedtime (0.4.527) were in her hand on the server but never shown. Both slices'
  notes say she holds them.
  - The cases checked the item in her hand, and the people's preview draws the held item itself, so neither caught it.
  - The layer now draws whatever any of the house's people holds (`PellPeople.carries`).
- **A thing drawn flat is held level, like a tray.** The first render of June with the lantern and the tin showed them as flat sheets
  held level, edge-on from the front, the side and the back. The game holds a flat picture that way; only a tool (the brushes, the
  saucepan) is turned to be seen from the side. So the lantern and the tin have no picture of their own: each is a small model that
  hangs from the hand, upright, and is seen from every side.
- **Her first painting pose read as lying down.** With a leg in one piece, kneeling back on her heels put her legs straight out behind
  her along the grass and her body on a slant, so from the side she looked to be lying on her front. She now kneels up, bent from the
  hips (above).

## Decided here

The proposal gives the beats but not their staging. These are mine, and any of them can be moved:

- **Eleven** is when June goes out; **midnight** is where the night waits for the morning. The hour goes on three times as fast while
  she is in her room, so the wait in the garage is under a minute.
- **She goes out by the back door** and locks it behind her: the house is never left open while she is out.
- **She paints only the thin stretch behind the shed** on this night, from the passage end westward, and leaves it wet. By morning it
  will be dry (the next slice).
- **A battery lantern**, set down at the end of the strip, so the strip is lit and she is seen from the garage window.
- **The tin moves along with her**, set down beside each cell.
- **Smears are hers to put right** while she is out; after she has gone in, a smear stays until morning.
- **Callum stands on the line by the drive**, short of the garage. On the last night he will be at its thinnest point, behind the
  shed (the proposal).
- **Callum's copy** is of the reader nearest him, a beat late, and mirrored while they look at him.
- **Tilly's singing through the wall** reaches only a reader playing Tilly, and no reader plays her yet. In solo play Tilly will
  mention it in the morning instead (the next slice), unless that moves.

## The case

`PellHouseTests.theFirstNightJuneRepaintsTheLineAndCallumStandsOnIt`, with two readers, one in the garage at its window and one in the
yard, checks:

- locked up, the hour goes on three times as fast, and June stays in her room until eleven;
- at eleven she gets up with her lantern, goes out by the back door, and locks it behind her;
- from outside, a hand on the back door finds it locked, and June has the key;
- the tin is gone from the bench and in her hand, and the shed is shut behind her;
- at the line, the lantern stands by the passage, the tin is open beside her, the brush is in her hand, and she kneels to it;
- the first cell is painted, and wet;
- the reader at the garage window saw her painting, from the garage;
- the reader in the yard, looking the other way, did not see her painting, though she went by them on her way to the shed;
- Callum is out, standing on the line by the drive; nobody saw him come;
- the reader in the yard turns to him and crouches. He does not crouch at once; a beat later he does, and a beat after the reader
  stands, so does he;
- a hand on him finds him standing on the line, and the reader saw him there;
- the reader puts a foot in the wet paint: it smears, they keep that they did, and June says so;
- she paints the smear over before she packs up: every thin cell is wet;
- the tin is back on the bench, the lantern is taken in, the shed is shut and the back door locked;
- June is in her room with its door shut and her hands empty, and Callum is still on the line;
- the hour goes on to midnight;
- left, the house begins again: the line as the first visit has it, the tin on the bench, and Callum back at the end of the hall;
- each reader keeps what they saw and did.

The bedtime case now ends with the hour going on fast while June is in her room, and June staying there until eleven.

## Not yet

- Tilly's singing through the wall, for a reader playing Tilly (the co-op Tilly slice).
- The morning: breakfast, the line dry, Gail's call, Callum in from the line.
- Everything after.

## Numbers

No layout change. No Witness change. New: three items (the lantern, the tin, the brush), one kind of thing (the lantern), one stage of
another (the open tin), two sounds, two gestures.
