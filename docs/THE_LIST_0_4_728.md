# The list, part 7c: the dawn bus (0.4.728)

The first of night four's other endings (`docs/THE_LIST_PROPOSAL.md`, Endings: "The bus. The reader is on the dawn bus with the mask
in the bag."). No protocol, layout or Witness change.

## Who it is for

Only the one who went in, and only if the House has read them as running (HOME `RUNAWAY`). Nobody else is told or can board, whatever
the House has read in them.

## The night

A little over two minutes of the hunt before first light (`DUE`, the town's clock at `LIGHT` - 900), they are told once, on the action
bar: "The first bus out stops at the feed store at first light."

At first light, if they are standing at the bench outside the feed store (within three and a half blocks), on their feet and not
stopped by Stace, they are on it: "The bus comes in off the highway with its lights on. You get on with the sack in your bag." The
screen goes black and they are set down in their own room by the tape deck, where the account is. Their outcome is `took_the_bus`.
Anywhere else at first light the night ends as before.

## The account

For one who took it: "When it got light the last time I was on the bench outside the feed store, and I got on the first bus out with
the sack folded in my bag. I never went back for the rest of my things."

## Single and multiplayer

- **Alone**, it is one more way through the last night with no kill needed.
- **With others**, only the one who went in is told and boards. A reader on Amber's side read as running, at the same bench, is not
  on it and ends `was_in_town_at_the_end`. **Spectators** keep nothing.

## Not built yet

The bus itself is not drawn: its coming is a cue and a fade. A drawn bus pulling in at the curb is left for a later part. Everyone gone
(every name out of town alive) waits on Amber's side (part 8).

## Made as

- **`TheListBus`** (new): `DUE`, `REACH`, `BENCH`, `ROOM`; `runaway`, `atBench`, `boards`, `tick`, `board`, `told`, `reset`. Who has
  been told is kept per town, never saved.
- **`TheListNights`**: the bus's tick on night four; at first light, the one who went in boards if they may (own `Bus`); `outcome`
  gives `took_the_bus`; for checks `clockTo`. **`TheListTexts`**: `BUS_DUE`, `BUS_TAKEN`, the account's line.

## Tests

`TheListTests.theDawnBus` (new, batch `the_list_bus`): three readers and a spectator; the one who went in and one other read as
running. Late in night four only the one who went in is told. At first light, all at the bench, only they board, are set down in their
room and get `took_the_bus` and the account's line; the other running reader stays at the bench with `was_in_town_at_the_end`; the
third ends as before; the spectator keeps nothing; who was told is forgotten.

## Every new line

Grey cues on the action bar, to the one who went in: "The first bus out stops at the feed store at first light."; "The bus comes in
off the highway with its lights on. You get on with the sack in your bag."

The account: the line above.
