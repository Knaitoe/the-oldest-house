# The House's read: home (0.4.371)

The first of the read's leftovers (`docs/HOUSE_READ_0_4_159.md`, "Not built yet"): settled `DANGER` authority figures who linger instead of chase, and the mood a tentative `HOME` gives domestic rooms. The rules are the owner's, from October 27 (`docs/HOUSE_READ_DESIGN.md`, "Authority figures under `DANGER`"):

> *"For someone who grew up in a `DANGER` house, the terror isn't the chase; it's listening to the hallway through a closed door."*

- **Witness:** unchanged, 81 / 61 / two kinds / three endings.
- **Layout:** 122. **Protocol:** 163, unchanged.
- **Three new sounds,** all subtitled and original: a floorboard in the hall, a slow step, and a door handle turned and let go (`tools/generate_home_audio.py`).

## What reads what

- **A settled `DANGER`:** the authority figures in that reader's rooms linger (below).
- **A tentative or settled `HAVEN` or `DANGER`:** domestic rooms get a mood, one line and nothing more.
- **`RUNAWAY`, no read, an uncertain read, or the trait switched off:** nothing changes.

Whatever the read, nobody moves faster or hits harder, nothing is shut, and every house keeps its second way out. All of it goes to the one reader: the sounds and lines are sent to them alone, so a friend standing beside them hears an ordinary evening.

## Somebody in the hall

`HomeRead.linger` is the one shape all three houses use. Somebody comes along the hall a step at a time. The board takes their weight outside the door, and they stay there. Then they try the handle and let it go, and later they go back the way they came. It takes about eighteen seconds. A reader who leaves the room leaves the hall behind them.

- **The child's room:** the first and third night lines are not words for this reader.
  - After the first window goes, footsteps in the hall stop outside the door, and the handle is tried and let go.
  - After the first door goes, the footsteps come back and stop where the door was. Nobody says anything.
  - The other four lines, the sealing and the crawl under the bed are as they were for everyone.
- **The Ghosts set:** once each filming day, between takes and out of sight of John, a heavy tread stops outside the door nearest the reader, and the handle is tried. The family's performances, the takes and the banister are untouched.
- **Three Fingers:** a reader alone in their thread finds Walter standing in the gap between the kitchen and the parlour as they come in. That happens before the sergeant's tale, or during the mourning. He says nothing. When they come near, or after eight seconds, he walks back to his chair.
  - It happens once an evening.
  - Never during the news or the knocking, which belong to the paw.
  - With anyone else in the thread he stays in his chair, so nobody's evening carries another reader's read.
  - The front door is the way out, as it always is.

## Moods

- **Goatman's supper,** early in the evening, to each reader by their own read:
  - **`HAVEN`:** *"Somebody has a radio on low in the cab. A cousin is singing the half of it they know."*
  - **`DANGER`:** *"The screen door bangs, and two of the cousins look up at the same time before they carry on."*

  The cousins' shared speech is unchanged.
- **The household rooms** off the great staircase get one line four seconds after their first line. The warm and the watchful lines are:

  | Room | Warm | Watchful |
  |---|---|---|
  | Sunday lunch | whoever is in the kitchen is singing under their breath | a pan goes down hard, then quiet |
  | Fresh sheets | a radio downstairs, somebody laughing at it | a door shuts hard downstairs; nobody comes up |
  | Mending day | a cup of tea by the lamp, too hot to drink | the television next door turned up loud |
  | First light | a blanket over the sleeper | their shoes still on, a glass on its side |
  | The long way home | Mara humming to the radio | Mara with both hands on the wheel and the radio off |
  | Washing day | something baking next door | a voice raised in the house behind, then stopping |
  | The lay-by | the crusts cut off yours | nobody has said anything since the last town |
  | The last landing | a cup left out for you, upside down | their boots lined up against the wall, exactly |

## Speech is never chat

The child's room's parents spoke in chat. Their lines now come on the action bar, like every other voice through a wall.

## Tests

One new case in the tailoring namespace, `aSettledDangerHallLingersForThatReaderAloneAndMoodsFollowTheHomeRead`. It covers:
- only a settled `DANGER` lingers;
- tentative and settled `HAVEN` and `DANGER` give moods, and `RUNAWAY` gives none;
- every household room has a warm and a watchful line, and neither genders the reader;
- the child's room changes its first and third lines only;
- a linger sends five steps, the board, the handle and four steps away to that reader, and none of it to a friend beside them.

The house-read job now counts 43 cases.
