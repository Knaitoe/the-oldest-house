# The way out: 0.4.199

The owner: "How would you feel about making a simulacrum of the overworld as a vignette? Slightly alter Minecraft's vanilla music, have things be slightly off - then occasional blips of static fuzzing over the screen, and finally text that reads 'you really thought you were out?' <- the players journal could be used to impart that text too. Player death should also trigger an exit from the vignette."

## What it is

A door like any other opens onto daylight. The reader steps out of a small shed into an ordinary-looking world:
- grass, flowers, oak and birch;
- a pond with sugar cane;
- cows, sheep, pigs and chickens;
- a dirt path to a cottage on the far side of the meadow;
- vanilla music playing.

It looks as if the House let them go. It has not.

Nothing in it is a monster, and it counts toward nothing: it is not a Witness source. It is dealt once to each reader, once they have been in the House long enough to want out, and to one reader at a time.

## Slightly off, then wrong

- **The music** is the game's own, a little flat and wavering, like a worn tape. It plays from the reader's own client, following the Music slider; the game's usual music is held back while they are there.
- **The sun** does not move.
- **The blocks** will not break, as everywhere in the House.
- **After the first half-minute:**
  - the animals stop where they are, and whichever of them the reader is not looking at turns to face them;
  - the reader's footsteps start to come back a moment late, for them alone.
- **Static** blips over the screen, for a few frames at first, with a crackle. The blips come more often the longer the reader stays.

## The end

The end comes when any of these happens:
- the reader reaches the cottage door (the way home);
- the reader has been there long enough;
- the reader dies there.

What happens then:
- The static thickens until it covers everything, and the music drops away.
- The screen goes black and holds one line: *you really thought you were out?*
- **The journal:** the same line is written on the next blank page of the reader's journal, in the other hand, if they carry one.
- The reader wakes back in the hall they came from, as if they had never left it.

## Death is a way out

Dying there is not a death. The death is cancelled, the reader is healed, and the end begins at once. Nothing is dropped and nothing goes to the Mother.

## Who gets it

- Dealt to a reader once they have eight Witness resolutions behind them, and never again once they have seen it through.
- A reader who goes back out through the shed door before the end has not had it: it may be dealt to them again, from the start.
- One reader at a time.
- It is not a Witness source: Witness stays 55 / 42 / two kinds / three endings.

## Build

- **Code.**
  - New: `labyrinth/WayOut` (dealing, the reader's clock, the watching animals, the late footsteps, the static timings, the end, a death there) and `labyrinth/WayOutRooms` (the meadow, shed, pond, trees, path, cottage and animals, all the game's own blocks).
  - New: `network/WayOutPayload` (arriving, a blip and its length, the end, the line, leaving) and `client/WayOutClient` (the detuned music, the static overlay and crackle, the line on black).
  - `Journals.houseLine` writes the line on the page after the reader's last.
- **The place.** Slot 126 (`way_out`), outside, layout 56 (saved worlds build it once), protocol 108.
  - Dealt at the strange depths, behind any ordinary door.
  - Its way on is the cottage's front door.
  - It keeps a fine morning that never moves (scene clock 12).
- **Its timings:** calm for thirty seconds; static from forty-five; the end on its own after three minutes.
- **The static** follows the screen-effects setting, never fading below a faint trace. The line always shows.
- **Art.** No new textures or sounds: the meadow is the game's own, and the crackle is the journal's static.

## Tests

`WayOutTests.theWayOutLooksLikeOutsideTurnsWrongAndEndsWithTheLineBackInTheHall` checks:
- the meadow, its door in and its cottage door;
- the animals, and the still morning;
- dealing only after eight resolutions;
- calm at first;
- the animals stopping and every unseen one turning to face the reader;
- a late step;
- the static beginning;
- a death cancelled with everything kept, ending in the hall with the line in the journal;
- never dealt again, and counting toward nothing;
- the cottage door ending it for a second reader with no journal and no line;
- the animals grazing again;
- its architecture view.
