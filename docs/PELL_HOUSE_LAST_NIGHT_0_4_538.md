# The Pell house 16: the last night begins (0.4.538)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its first
visit whole, and its second, a week in, has the afternoon and June's swim, the knife, June's hand, the tapes and the journal's false
entries (`docs/PELL_HOUSE_ENTRIES_0_4_537.md`). This is its sixteenth slice: the third visit begins, and the line can be broken. From
the proposal's "3. The last night":

> - **The scene:**
>   - the calendar's circle is today;
>   - the pool is full and lit;
>   - the line is fresh and wet in places;
>   - every knife slot is empty;
>   - Callum is on the line at its thinnest point, behind the shed.

and from "The last night, in play":

> - **Breaking the line.**
>   - **The tools:** the scrubbing brush and the bucket from the laundry.
>   - **The work:** holding use on a cell of the line scrubs it out over a few seconds. The paint fades in stages, on its own texture
>     for each surface.
>   - **The gap:** two cells side by side make a gap wide enough to pass Tilly through.
>   - **June repaints** on her rounds on the earlier visits, so a gap opened then is gone by morning. On the last night she is in the
>     water, and the line stays as the readers leave it.
> - **Callum on the line.** He stands where it is thinnest and does not move for pushing.
>   - **What moves him:** food. A plate from the fridge, anything, offered to him. He takes it, sits down on the grass inside the line
>     and eats, and the line behind him is free to scrub.
>   - **When the line breaks:** he looks up and says the only thing he says in the whole vignette: "My name's Callum."

Midnight in the pool and getting Tilly out come next, then the freezer, then the endings. The House still does not deal the house.

## The third visit

- **Who has it.** A reader who has had the first two visits has the third. The house plays the earliest visit any reader at it has
  not had, as before, so a reader who has had neither still has the first.
- **It begins in the evening,** without Gail: June beside her chair, the television about to go on; Tilly in Nessa's room; Callum out
  on the line in the middle of the thin stretch behind the shed, facing the house.
- **The house as the last night has it:**
  - the pool full, lit from under the water at both ends of its long sides, and its lane rope gone;
  - the line gone over again all the way round, wet in places;
  - the knife block gone from the bench (a clean square where it stood) and put away in the cupboard above it, every slot empty;
  - a space in Callum's drawer where the knife lay (the drawer's third stage is now drawn: the felt pressed down along the space);
  - the shed's calendar: the circled night is tonight.
- **The evening holds** until a reader first comes in. June, from her chair: "That you, love? Early night tonight, all of us." A reader
  too far off to hear her is told she called it. Then the evening goes as it does: the television, Tilly's hair brushed at bedtime,
  everyone sent out and the house locked up. Callum stays where he is.
- **The night** goes on, locked up, to midnight, and waits there. June does not go out on her rounds, so the line stays as the readers
  leave it. What happens at midnight comes next.

## The bucket and the brush

- **In the laundry** the bucket stands with water in it and the scrubbing brush across it, its bristles worn to one side. A hand on it
  takes it ("You take the bucket and its brush."), on any visit; with no room to carry it, it stays. There is one to a house.
- **In hand** it is the scrubbing brush (an item of its own, drawn by `tools/draw_pell_people.py`). Held to a cell of the line (the game's
  own brushing, as with the archaeologist's brush), it works at the paint: half away, then out. Each stage takes two seconds on the thin
  stretch behind the shed, five anywhere else, where the line is a hand wide. Taken off the cell, or onto another, the work on it starts
  again. A cell scrubbed out is done with: the brush comes away from it. Each stroke is heard (a new sound, `pell.scrub`) and splashes.
- **The gap.** A cell scrubbed out beside another scrubbed out is a gap: the line is broken.
- **A bucket nobody has.** If no reader at the house has it (carried off out of the house, or lost) and it is not lying in the lot, it
  is back in its place within a second. The visit beginning again puts it back too.
- **On the visits before the last,** June has painted over every scrubbed cell by first light.

## Callum on the line

- **He keeps the thin stretch.** While he is out on the line and has not eaten, a reader holding the brush to any cell of the thin
  stretch brings him onto that cell, and he stands there: nothing comes off ("Callum steps onto the line where the brush is, and stands
  there.", once to each reader). The rest of the line is not his.
- **A hand on him** on the line: he does not move ("Callum does not move. His bare feet are on the paint.", and so on).
- **Food moves him.** On the last night, a plate from the fridge (a hand on the fridge gives each reader one plate of dinner under cling
  film, once) or anything else to eat, held out to him: he takes it ("Callum takes it. He sits down on the grass inside the line, and
  eats."), goes a step inside the line, sits down on the grass and eats it with his head down, and the stretch is free. A hand on him
  then: he eats. More food: he already has what he was given. When he has eaten it he sits on, his hands in his lap.
- **On the visits before the last** he looks at food held out to him and does not take it.
- **When the line breaks,** anywhere, he looks up and says: "My name's Callum." Once. Every reader near enough hears it.

## Poses

- **Eating** (`PellPeople.EATING`): sitting on the grass, his legs out in front of him, what he was given in his left hand in his lap,
  his right hand going to his mouth and back, his head bowed to it.
- **Sitting** (`PellPeople.SITTING`): the same, both hands in his lap. His head keeps its own pitch, so he looks up when he says his
  name.

## What each reader keeps

- `TookBucket`, `TookPlate`, `FedCallum`, `CallumStood`;
- `Scrubbed` (how many stages of paint they have scrubbed off) and `ScrubbedOut` (that they scrubbed a cell out);
- `HeardCallum`.

## How it is made

- **`labyrinth.PellLastNight`**: the last night's beginning (`begin`, from `PellVisit.restart`), June's welcome, Callum on the line
  (`callum`, every tick; `stands`; `feed`, from `PellPeople.talk`; `broken`), the pool's light (waterlogged light blocks in the water,
  `poolLight`), and `lockedUp`, a test fixture.
- **`labyrinth.PellScrub`**: the bucket (`take`, `keep`, `back`), the brush's work (`scrubbing`, from `PellScrubItem.onUseTick`), the gap
  (`gapAt`, `broken`), the dawn repaint (`repainted`, from `PellMorning`'s first light) and the fridge (`fridge`).
- **`labyrinth.PellScrubItem`**: the brush in hand, `UseAnim.BRUSH`.
- **`PellWeek.LAST`** is 3. `PellWeek.dress` dresses the third visit; `PellVisit.clock` holds the evening until the welcome and takes the
  night to midnight; `PellNight` skips June's rounds and Callum's night on the last night.
- `PellHouse`: a hand on the line with the brush in hand goes to the brush; the bucket and the fridge answer.
- Client: `LiteraryRenderers.pell` draws `EATING` and `SITTING`.
- Art and sound: `pell_scrub` (`tools/draw_pell_people.py`), `cutlery_2` (`tools/draw_pell_things.py`), `pell.scrub`
  (`tools/generate_pell_audio.py`).

## Decided here (any of it can move)

- The third visit begins at dusk, at the television, and holds until a reader comes in.
- The last night has no rounds: the night goes to midnight and waits there for what comes next.
- The bucket comes with its water; there is one to a house; it goes back when nobody at the house has it.
- Two seconds a stage on the thin stretch, five elsewhere.
- Callum keeps the whole thin stretch by stepping onto whichever cell is being scrubbed; the rest of the line is open.
- Anything to eat moves him, and only on the last night; the fridge has one plate for each reader, on the last night only.
- The earlier visits' scrubbing is painted over by first light.
- The pool is lit by light in the water; a lamp fitting can come with the art pass.

## Checked

- `PellHouseTests.theLastNightBeginsWithThePoolLitTheKnivesPutAwayAndCallumOnTheLine`: two readers who have had both visits; the house as
  above; the evening holding, June's welcome and the far reader told; the fridge's plates; the bucket taken, kept while held and back
  when nobody has it; locked up, the night waiting at midnight with June in her room and Callum on the line.
- `PellHouseTests.callumKeepsTheThinStretchUntilHeIsFedAndWhenTheLineBreaksHeSaysHisName`: two readers; a hand on Callum; the brush
  bringing him onto the cell and nothing coming off; the plate held out, taken, eaten; the hand and more food while he eats; two thin
  cells scrubbed out in stages; the brush coming away from a scrubbed-out cell; his name, heard by both; the scrubbing kept; his eating
  done and sitting on.
- `PellHouseTests.beforeTheLastNightCallumWillNotEatAndJuneHasPaintedOverTheScrubbingByFirstLight`: a week in, food refused; a thick cell
  scrubbed out and the next half away at five seconds a stage; asleep in the garage, both painted over by first light.
- The second visit's call case now ends with the house playing the last night for two readers who have had both visits, then the first
  visit, undone (no light, the cupboard shut, Callum not sitting out), for a reader who has had neither.
