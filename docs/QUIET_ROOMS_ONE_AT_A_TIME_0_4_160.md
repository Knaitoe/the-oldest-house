# The quiet rooms, one at a time — 0.4.160

The owner (October 29): "I don't think the personhood rooms should be fed all at once. It should be - open door in the junction, be fed the first vignette - it closes and the player moves into a hallway. the next vignette they encounter should be another personhood vignette. the weight of a player encountering a vignette should be increased until these are all completed."

## What changed

Since 0.4.143 the quiet rooms ran as a chain: a junction door led into the first room, and each room's way on led straight into the next until all five were done. Now they come one at a time.

- **The first room.** A reader who has entered no quiet room yet meets the first behind a junction onward door, as before.
- **Then a hall.** Every room's way on is the hall its door was dealt; no room leads into another.
- **Then the next room is the next story.**
  - While a reader has begun the quiet rooms and has one left, each fresh arrival can mark one of its dealt doors as the way into the next room.
  - The chance is 60% on an arrival, 80% after one arrival without, and certain after two. That is far more often than stories come.
  - The marked door is still dealt an ordinary hall, which is where the room's way on leads afterwards.
  - No other story is dealt while a room is owed, except when a reader follows a deliberate scent or goes to rescue a captured pet.
- **Entering a room settles every marked door.** The next is dealt afresh, so at least one hall always separates two quiet rooms. The doors are kept in the reader's quiet-room record and survive saving.
- **The order of the rooms** is unchanged, and so is the rule that a room the reader turned back from is not offered again.

Readers who never began the quiet rooms are dealt as before; this includes observers and readers who never came in by the front door. Readers who began them before this version keep going from wherever they are: the room after their current one is dealt like any other.

## Tests

The junction case is rewritten. It now walks:
- the first room from a junction door, ending in the hall that door was dealt;
- the certainty after two arrivals without a room, with exactly one owed door, which is dealt an ordinary hall, and no other story dealt;
- the next room through that door, keeping its source door and dealt hall, which clears every owed door and again ends in its own hall;
- nothing owed and ordinary junction doors once all are entered.

The quiet-room job still runs 31 cases. Locally all 31 pass. The waiting-room case failed once in the full local run and passed alone, as it does in CI; it depends on test order.

No layout, protocol or Witness change: 54 / 41 / two kinds / three endings.
