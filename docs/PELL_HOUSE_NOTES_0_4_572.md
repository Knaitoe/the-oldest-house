# The Pell house, the owner's playtest notes: the kitchen (0.4.572)

From the owner's notes on playing the Pell house on 13 November (the earlier batches: `docs/PELL_HOUSE_NOTES_0_4_566.md` to
`docs/PELL_HOUSE_NOTES_0_4_571.md`). New builds only. No protocol, layout or Witness change.

The owner: "the whole pell house is too uniform in its walls and carpets, and the kitchen isn't spaced believably. all of the
textures could be improved."

That note is being done in three parts. This is the first: the kitchen, and one fault the room frames showed at every outside
door. The walls and floors, room by room, are next (0.4.573), and then the flower bed along the front wall.

## What the room frames showed

Every room of the house was rendered from eye level before anything was changed (eighteen frames indoors, five of the front).

- **The kitchen.** The table stood across the middle of the room, east to west, two chairs to each long side. The two on its
  north side stood backed against the fronts of the benches, in front of the lunchboxes and the knife block, and the floor
  before the fridge was shut in behind them and the table: a body could not walk to the fridge at all.
- **The outside doors.** The house's floor stops at the inner faces of its walls, and the walls stand on the lot's lawn. Under
  the screen door, the back door and the sliding door there was grass, between the path or the paving outside and the carpet or
  the lino inside. The garage's slab stopped the same way: lawn under the roller door and the walk door.
- **Every room** has the same off-white plaster from floor to ceiling and the same dark carpet, with no skirting and no
  cornice. That is 0.4.573.
- **June's room**, which is locked, has nothing in it at all. That is 0.4.573 too.

## The kitchen

The benches keep the back wall: the fridge in the corner, the lunchboxes, the knife block, the sink under the window onto the
pool, the stove, the cupboards over them. The floor before them is clear now from the fridge to the stove, the whole width of
the room.

The table stands in the corner by the hall wall, lengthwise, its end against that wall. Two chairs stand on each side of it,
the far two against the west wall under the chore chart and the calendar:

- **Tilly's place** is the one by the hall door, straight in from the arch.
- **Callum's** is next to hers, the nearer the stove.
- **The two against the west wall** are the far side: the one a reader is given, and the one that is laid and that nobody sits
  at. They are got to round the table's end, by the benches.

June stands at the table's end by the benches to lay it, to serve and to clear, with the stove and the sink behind her.

What goes on in the kitchen is as it was; only where it happens has moved.

- June lays, serves and clears from the table's end, and wipes the table down there in the afternoon.
- Tilly and Callum come in by the arch and sit from the near side. If a reader has taken one of their chairs, they take the
  next free one, round by the benches when it is on the far side, and come away the same way when they get up.
- June's way to the table in the morning turns at the benches.
- The plates a reader takes down from the cupboards are laid from anywhere a hand reaches the table; each goes to the nearest
  bare place, as before.
- The phone, on the east wall by the arch, and the way to it are untouched.

## The sills

- Under the screen door, the back door and both halves of the sliding door there is a concrete sill.
- The garage's slab runs out under its walls, its roller door and its walk door.

## What the frames show now

Thirteen frames from eye level: eight of the kitchen, five of the doors.

- From the arch: the benches from the fridge to the stove with nothing standing in front of them, the window over the sink, and
  the backs of the two near chairs at the left.
- Along the benches from the stove's end: a clear floor to the fridge, and the table in the corner under CALLUM and TILLY on the
  chart.
- From the benches: the table, a chair to each place, the calendar and the chart over the far two.
- Each of the house's outside doors stands on a concrete sill, and the garage's two on its slab.

## Checks

- `PellHouseTests.everyRoomHasItsThingsAndEachAnswersAHand` now expects the table's two halves in the corner, a chair on each
  side facing it, the floor before the benches bare from the fridge to the stove, and, for each of the table's four places, a
  chair at it and its plate over the table. A body walks from the arch to the fridge, the sink, the stove and the table's end,
  and cannot walk through the table.
- The cases that put a hand on the table, on Callum's plate and on what is left on the table afterwards do it where the table
  now is.
- **The Pell house namespace**: 59 cases, as before. Architecture: 6 cases. The cast and two-reader namespaces: 102 cases.

## Still to come

- The walls and the floors, room by room: a finish for each room's walls with skirting and cornice, a floor for each room, the
  surface textures redrawn; and something in June's room.
- The flower bed.
- Each vignette door's own bleed and sound in the halls.
- Put by for later: a ball, and a game of catch with Callum; taking Tilly round the house by the arm.
