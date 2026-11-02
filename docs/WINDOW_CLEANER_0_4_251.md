# The Window Cleaner (0.4.251)

The owner's second puzzle proposal of 2 November (docs/PUZZLE_PROPOSALS_2026_11_02.md):

> A window cleaner stranded on a hanging platform asks you to operate an emergency winch inside the room. As you lower them, lit
> windows pass outside: a bedroom, a kitchen, a landing. Eventually, one reveals the corridor you entered from, seen from outside and
> below. The cleaner steps off into that corridor. A moment later they come through your door carrying their equipment, thank you, and
> leave. You can watch the entire return. The unease comes from seeing how those places apparently connect.

The release adds one puzzle room in a new family, WINCH: the window cleaner (`WINDOW_CLEANER`, slot 158, early, with no deep room).
The layout version is 80 (new builds only). The room follows `PuzzleRooms`' pacing:
- it is offered only after a story, and rarely;
- nothing counts down, hurts or is used up, and solved stays solved for everyone;
- no Witness credit.

There is no protocol, Witness or source-kind change. Code: `WindowCleaner`, `WinchBlock`.

## The building

Nothing in the room is a trick of the House's doors. The corridor seen in the well is the corridor the reader walked, and the rooms
above it really are stacked there.

**The corridor.** The entry door opens on a short corridor:
- plaster walls, spruce boards and a red runner;
- a lamp on a little shelf, a hanging lamp, a radiator;
- in its outer wall, a casement onto a light well, painted shut, and a small window either side of it.

**The stair.** At the corridor's end a narrow stair climbs four storeys along the well's far side. Its lower flight has a window onto
the well at every step.

**The plant room.** At the top of the stair a door opens into the plant room:
- smooth stone walls;
- a window the width of the room over the well, down to the floor's edge, so that pressed to the glass the reader can see to the
  bottom;
- the emergency winch by the window: a cable drum between two green cast cheeks, a crank, and a red-knobbed brake lever on an iron
  quadrant;
- an iron staff door in the far wall, with no handle on this side.

Behind the staff door is a small lobby, with the room's far door, also iron and also a staff door.

**The well.** Across the well, one above another, are three lit windows:
- a bedroom;
- a kitchen;
- a landing with a stair going up out of sight.

Below them, below the reader, is the corridor's casement. The rooms behind the windows sit over the corridor, with no door to any of
them. A beam across the top of the well carries the cradle's cables, with a lamp at each end.

## The encounter

**Waiting.** The cleaner stands in a cradle at the top of the well, outside the plant room's window, with a bucket and a brush. While
anyone is in the room they face whoever is nearest and call out (their speech shows over their head, as everyone's does):
- "Hello? In there! Can you hear me?"
- "The motor's gone. There's an emergency winch by your window."
- "Take the brake off first, the lever by the drum. Then wind me down. Slowly."

**The brake.** With the brake on, the handle won't turn. Thrown off, the drum gives a little against the cable and holds.

**Lowering.** Each turn of the crank, a click at a time, lets the cradle down a block. There are fifteen turns from the top to the
bottom. As each window comes level with the cleaner's head, they turn to look in:
- the bedroom: "Someone's left a lamp on in there."
- the kitchen: "There's a kettle on in there. Nobody about."
- the landing: "Stairs. I never know where these places keep their stairs."
- a block above the casement: "Bit more. There's a window here I can get in by."
- at the bottom, level with the corridor's floor: "That'll do. I'll come round."

**The return.** The cleaner opens the casement from outside and steps into the corridor. They shut the casement behind them, unless
somebody is down in the well, and say, "This'll be the way you came up." Then they walk the corridor and climb the stair. Through the
corridor's windows and the stair's, the whole way round can be watched from the plant room. They open the plant room's door, come
through it, and shut it behind them.

**The thanks.** In the room they turn to the reader: "Thanks. I'd have been out there all night." After a moment: "I'll let myself
out the back." They unlock the staff door, go through into the lobby, and it shuts behind them.

**Gone.** A moment after the staff door shuts, both staff doors swing open on their own. Nobody is in the lobby, and there are no
footsteps. That is the room solved. Afterwards the cradle hangs empty at the bottom of the well with the brake off. Built again, the
room stays like that.

**The casement.** Once the cleaner has opened it, the casement opens and shuts like any window, so nobody can be shut out in the well.

## Assets

`tools/draw_winch_fittings.py` (checked in CI) draws the `winch_fitting` block, its models facing north and turned by its facing:
- the drum, its crank a quarter turn further round at each of four stages;
- the brake, on (upright) or off (thrown forward);
- the cradle's slatted floor, at the top of its block, so the cleaner stands level with the corridor at the bottom;
- the cradle's guard rail;
- the steel cable;
- the casement, shut or swung open.

The cleaner is a painted skin on the stranger phase 54 (`literary_window_cleaner`): navy work clothes, a cap and stubble. They carry the
bucket and brush in their hands. The winch's sounds are vanilla: a chain and a lever's ratchet for the drum, a ladder's creak for the
cradle, iron and wooden doors.

## Tests

`WindowCleanerTests` (exploration), `theWindowCleanerComesDownTheWellAndRoundThroughTheReadersOwnDoor`:
- both staff doors iron and shut;
- the cradle at the top with the cleaner standing on its slats;
- the drum won't turn with the brake on;
- the brake comes off;
- fifteen turns, each lowering the cradle and the cleaner a block, with the cradle and cables in place passing the kitchen;
- at the bottom the drum turns freely;
- the casement opens, the cleaner steps into the corridor, and the casement shuts behind them;
- up the stair, through the plant room's door (shut behind them) and into the room;
- not solved while they are there;
- solved when they have gone: nobody behind the door or anywhere in the room, and both staff doors open;
- built again, the doors stand open and the empty cradle is at the bottom.
