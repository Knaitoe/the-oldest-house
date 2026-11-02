# The Barretts between takes (0.4.247)

The owner, 2 November:

> Give Ghosts Set more ordinary behavior between scenes. In the earlier days, a parent clears a plate, someone retrieves a forgotten
> mug, Barry checks a cable while talking. Small, purposeful actions would make the family feel less like a cast waiting for the
> player—and give the later disruption more weight. Keep those actions separate from the carefully staged sequences.

Until now, the family and Barry stood or sat exactly where the day put them, saying an idle line now and then, until the camera
rolled. On the first four days they now go about small things of their own while the camera operator finds their way to Barry and
before the day's take has begun. One person goes at a time, a few seconds apart, and each walks there and back through the house's
own collision.

- **Sarah clears a plate.** She gets up from the table, takes the plate on her side of it ("I'll just get these out of the way before
  you start."), carries it to the sink, sets it down and runs the tap, then comes back and sits down where she was. The plate is
  gone from the table for the rest of the day.
- **John goes back for his mug.** "Hang on. Where did I put my coffee?" He walks to the kitchen and finds it by the coffee maker
  ("There it is. Still warm, nearly."). He sits back down with it and, after a moment, puts it down by his plate.
- **Barry checks a cable while he talks.** He gets up from his chair at the monitors, crouches over the cable where it lifts by the
  den, presses it down and talks through the day's work while he does it. He then goes back to his chair. What he says belongs to
  the day:
  - the interviews and the tape coming up at the corner;
  - going straight through supper with no cuts;
  - the run that won't reach the landing;
  - the cable going down the basement stairs with them.

Which errands come on which day:

| Day | Errands, in order |
|---|---|
| 1 (the interviews) | Barry's cable, Sarah's plate |
| 2 (the family dinner) | John's mug, Barry's cable |
| 3 (the upstairs reconstruction) | Barry's cable (the family is upstairs for the take) |
| 4 (the private conversation) | John's mug, Sarah's plate |
| 5 to 7 | none: by then nobody in the house is going about anything |

## Kept apart from the staged sequences

- Errands start only before a day's take: the camera not rolling, the shot clock at zero, the day not done, and the reader somewhere
  in the house.
- When the take is called with someone still out, Barry says "Places, everyone." That person comes straight back to their seat by
  the way they went, and the take does not start until they have sat down. The staged sequences always begin from the places the day
  gave everyone.
- A new day puts everyone back in their places with empty hands and the table as it really is.
- Everything is the reader's own, as the cast already is: their actors, the things in their hands, and the plate gone from the table
  in their view alone (sent to them again every five seconds, so a section loaded afresh agrees).

## Code and art

- `GhostsErrands`:
  - `Errand` (who, seat, route out, the spot's facing and pose, how long) and `day(d)`;
  - `between` (start one when due, step the one under way) and `home` (the take called: hurry back);
  - `busy`;
  - the phases: off the seat at its height, down, along the route, the work, back, up to the seat height, onto the seat, and (for
    John) holding the mug a moment;
  - its state is kept under `Errands0247` in the reader's record and cleared with each new day.
- `GhostsSet.reader`:
  - calls `between` while the reader is waiting;
  - holds the take while `busy`.
- `GhostsSet.arrive` (a new day) and `jumpToDay` reset the errands. `stage` empties everyone's hands.
- Barry's director's chair has its back right behind where he sits, so he comes back onto the seat a hair in front of it.
- `LiteraryRegistry.GHOSTS_MUG` ("John's mug") is drawn by `tools/draw_ghosts_errands.py` (16x16), checked in CI. Sarah carries the
  trailer's supper plate.
- The Ghosts cast renderer gains an item-in-hand layer, so what someone carries is drawn in their hand.

## Tests

`GhostsSetTests#betweenTakesBarryAndSarahGoAboutErrandsAndTheTakeWaitsForThem` (day one):
- errands on days one to four only;
- Barry crouched over the cable on the floor by the den, with no take running, then back in his chair;
- Sarah taking the plate (in her hand, gone from the table) and setting it down at the sink;
- the take called while she walks back: the take waits for her, she sits where the day put her, and then the take rolls.

`GhostsSetTests#johnGoesBackForHisMugAndSitsDownWithIt` (day two): his mug in his hand on the way back, seated still holding it, then
put down.

The two cases and the other Ghosts Set cases pass locally. This is pending CI. No protocol, layout, Witness or source-kind change.
