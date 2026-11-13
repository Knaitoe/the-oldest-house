# A copy for the seventh: the party stories (0.4.551)

The owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full".

The system is the House's scene copies (`SceneInstances`): a second whole copy of a scene, a million blocks west of the first,
built by the scene's own builder, with a story of its own. The private scenes have had them since 0.4.154, Bike night since
0.4.513 (six to a town) and the Pell house since 0.4.547 (two to a house). This release gives them to the other stories written
for a party, and makes "full" mean the same thing for all of them.

## Full

Each party story has a number of places (`SceneInstances.PARTIES`):

| Story | Places | Why |
|---|---|---|
| Bike night | 6 | six bikes (0.4.513, unchanged) |
| The Last Crossing | 6 | the proposal's crew of four to six on Vera's roll |
| Pinehook | 6 | the proposal's two to six |
| The Ones Already Inside | 6 | the plan: six sockets, six guest rooms; "a seventh reader gets a house of their own" |

A copy's places are held by the readers in it now and by the readers its story keeps a place for, though they have stepped out to the
House (`SceneInstances.held`): The Last Crossing's crew on the roll, Pinehook's roll. A copy whose places are all held is full.

- A reader goes back into the copy they keep while it has a place, and otherwise into the first that stands and has one, so friends
  go into the same one, as on Bike night.
- One of the crew who steps out and comes back finds their place still theirs: a newcomer cannot take it while they are out.
- A latecomer still goes into a copy with a place left, and comes in partway, as each story has always let them ("I came in after
  the roll had been called."). Only a full copy sends somebody on.
- The seventh goes into the next copy: a second station, a second weekend, a second lodging house, each built whole as the first and
  with nothing begun in it. Its door waits while it is made ready, as any door onto a copy being made does.
- The next copy is made ready ahead, when a copy is down to its last place and no other has room, so the seventh seldom waits.
- The server's `sceneCopies` setting is the most copies there can be, as for every scene with copies.

Each story already kept its state by the copy it was in (its runs keyed by where the scene stands; Witness and the accounts per
copy), so nothing in the stories themselves changes.

## The cases

- `LastCrossingTests.aSeventhReaderIsGivenACrossingOfTheirOwn` (literary): a crew of four signs and the day begins. A latecomer goes
  into the first crossing (four places held) and comes in partway; the next crossing is made ready ahead; another latecomer takes the
  last place. A seventh waits. One of the crew steps out: the crossing is still full, their place is held, and it is theirs when they
  come back. The second crossing stands a million blocks west; the seventh goes in by its own door; nothing has begun there; the first
  crossing's clipboard has its roll on it and the second's has not.
- `PinehookTests.aSeventhReaderIsGivenAWeekendOfTheirOwn` (pinehook): one reader in the cabin and five more on the roll who have
  stepped out: six places held. A seventh waits; one of the six goes back into theirs; the second cabin stands, the seventh goes into
  it by its own door, and nobody's weekend has begun there.
- `LodgingHouseTests.aSeventhReaderIsGivenAHouseOfTheirOwn` (lodging): six readers in the house; a seventh waits; one of the six goes
  back into theirs; the second house stands a million blocks west, the seventh goes into it by its own door, and it has what the first
  has where the first has it (the terms on the hall table, the hall stand, Daniel's frame and door, two guest rooms' beds, the settee,
  the range, the key on its hook, the ratchet, the dead post, the way on).

## Decided here (any of it can move)

- Full counts places, not the story's progress: a latecomer joins partway while there is room, as the stories were written.
- A place on a roll is held while its reader is out, for as long as that story keeps its run.
- Friends are not kept together across copies (as on Bike night): seven who come to the door together are six and one. The Ones
  Already Inside's own rule, "a party is never split", is its slice 18's.

## Not yet

The other stories more than one reader plays, one at a time, each with a case where one reader too many arrives:

- **The Goatman's trailer.** Its night is one run for the whole world, so a copy cannot have a night of its own yet; it needs its run
  kept by copy first. Then eight places (the cousins' beds), and the late cousins' rule (nobody new after supper) as its own.
- **The Ghosts Set** crew and **the Shallows**: their party sizes, and whether their state is per copy.
- **The shared scenes with no number yet** (Camp Blood, the lake nights, the hotel, the séance, Last orders, Polybius, the Sphinx,
  the yard's stories): each needs a number of places decided, or is left shared on purpose.

## Numbers

No layout change. No Witness change. New builds only.
