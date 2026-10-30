# The House remembers a room — 0.4.190

This is the success-and-failure audit's fifth build (docs/SUCCESS_AND_FAILURE_AUDIT.md). Twenty-four rooms are experiences: a reader goes through them, and they are neither won nor lost. Twelve already give an artifact of their own. This build gives eight of the others what the audit proposed for them: a subtle change that plays only in the House once the room is resolved.

Each one:
- echoes its own room;
- is seen or heard by that reader alone;
- plays only in the manor, never in the reader's own world;
- never blocks a way or changes anything real.

## How it is private (`HouseRemembers`)

Nothing in the manor is changed for anyone.
- **Blocks** the reader sees differently are block updates sent to their client only. The world keeps its real blocks, and nothing is saved.
- **Figures** (the toy, the pew, the small figure) exist only in packets sent to that reader. They are never added to the world, have no collision and are taken back when the reader leaves.
- **Sounds** are positional sounds addressed to that reader.
- **The backseat's stowaway** is drawn by that reader's client alone (`HouseRemembersClient`), using the car's own body. The client is told through the existing literary view payload, so there is no protocol change.

Leaving the manor withdraws every view at once: real blocks are re-sent and packet figures removed. A client that changes level has already dropped them.

Each echo starts the House day the reader's resolution of its room is first seen in the manor (`Since_<story>` on the reader's `house_remembers_0190` record). Readers who resolved these rooms before this build start from that day.

## The eight

| Room | In the House |
|---|---|
| **The pattern (wallpaper)** | The great room's west wall takes the yellow paper, two strips a House day from the front, until the wall is covered (seven days). Only plain solid wall is papered, never the windows. Now and then a figure shows in the paper at the edge of sight, 45 to 70 degrees off where the reader looks. It is gone when they look at it (within 35 degrees), or after a second and a half. |
| **The window (model home)** | An oak stands outside the great room's front window, three blocks out, trunk and crown. It is set only into empty air. It is not there from outside. |
| **The upper ledger (Winchester)** | A spruce door stands against the study's west wall, between its windows, flush to the plaster. It is set only where the wall is plain and the floor is sound. Opening it swings it aside onto the wall behind it: "It opens onto plaster." It shuts itself three seconds later. Using it never reaches the real world: the click is taken, so nothing is placed there. |
| **The disappearing exits (child's room)** | The teddy from the ceiling sits on the low half of a tread in the stair tower. On the first day it is on the lowest tread, and it is one step higher each House day, until it reaches the top. |
| **The congregation (preserved cave)** | Walking into the hall from another room: a single dark oak pew stands ahead or against the far wall, its seat turned to face the way the reader came. It is gone when they leave the hall. It comes the first day after the cave, then on one hall entry in three, a day at most. |
| **The little rooms (miniatures)** | Where the reader stands still in the manor is kept. On a later House day, the place they last stood on an earlier day becomes the figure's. The figure is a small carved stand, about knee high, wearing the reader's own head and facing the way they faced. |
| **The returned knocks (Hill nursery)** | Now and then, after the reader has been in the manor a while, two to four blows come on the plain wall nearest them at head height, within four blocks. Then the wall listens for fifteen seconds; the first time it says so: "The pounding stops, as if it is listening." Knocking back the same count on that wall (a left click on it or a block beside it) is answered knock for knock, softly. The wall is then quiet until the next House day. A wrong count, or none, lets it try again later, up to three times a day. |
| **Twenty seconds (backseat)** | At night, if the reader stands still for two seconds before a manor window and looks into it, the stowaway from the car sits up behind their shoulder in the black glass. It is placed beyond the pane where the point behind them would be reflected, faintly lit, as it rose in the car's mirror. It is gone when they turn from the glass or step away, or after five seconds. It comes once a House day at most. |

## Tests

New: `HomeIntrusionTests.theManorRemembersEachExperienceOnlyForItsReaderAndChangesNothingReal`, run with the manor fixture and a peer in the same room. It checks:
- the first day's two strips of paper, then four the next day, around the window glass;
- the oak outside the front window;
- the shut door against the study wall, which opens onto plaster and shuts itself;
- the teddy on the lowest tread, then a step higher;
- the pew facing the way the reader came, and gone when they leave the hall;
- the small figure wearing the reader's head where they stood the day before;
- three private blows on the nearest wall, answered three times when knocked back three times, then quiet;
- the stowaway placed beyond the glass at the reflected point behind the reader, then taken back when they turn.

It also checks that:
- every view reaches the reader as block updates and packet figures;
- the peer is sent none of them;
- the real blocks are unchanged;
- leaving the manor withdraws everything;
- the reader's account is untouched.

It passes locally, with the existing manor knocking case.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No new resolution.** None of these is a Witness source.
- **No PNG changes.** Every echo reuses its room's existing block, model or sound.
