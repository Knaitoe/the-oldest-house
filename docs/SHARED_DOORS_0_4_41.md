# Shared doors and breaches (0.4.41)

Playtest report: doors had to be opened several times because they shut before
the player crossed; the House misbehaved when players shared a hallway; some
players were still reported as teleported outside.

## Labyrinth entry doors

- The entry door used to be shut on **every tick** a player stood 2.5 blocks or
  more inside the room. A player turning back could open it from reach and have
  it slam on the next tick, and a second player inside kept shutting it on
  anyone following. It now shuts **once per crossing**. The crossing resets when
  the player returns to the door cell.
- It is never shut, on arrival, on the shut-behind or on a walk-back, while
  another (non-spectator) player is within three blocks of it. The shut-behind
  waits and shuts once the doorway is clear.
- Arriving copies the source vestibule behind the entry door. The copy now skips
  every cell that another player's body (inflated by 0.75 horizontally and 0.5
  vertically) touches, so no one is walled in or loses the floor under them.

## Front and back doors

- When a player finishes passing an exterior door, it no longer shuts if another
  player is within the door grace (three blocks) on either side. The existing
  forty-tick idle closer shuts it once nobody is within two blocks.
- Walking out of an open exterior door, the crossing is classified DOOR (using
  the nearest door within 4.5 blocks) instead of BREACH.

## From the playtest log (0.4.40 server, four players)

- **Clicking a front door from full reach was undone.** Bug8735 crossed in from
  4.1 blocks outside and Proud_Badger crossed out from 4.45 blocks inside. Each
  arrived at the same spot on the other side, which was outside the old
  three-block door grace. Within 50 ms each was sent back as a BREACH (log
  tokens 13 to 14 and 20 to 21). This was the "door has to be opened several
  times" report.
  - The grace is now 5.25 blocks: the native 4.5-block interaction reach plus
    half a door.
  - A player who crossed by the handle also stays valid within six blocks until
    they pass the door or walk off.
- **Hallway breaches with several players.** Every one of tokens 9, 12, 18 and
  19 landed in the impossible hallway at manor-relative z≈77 and x≈12.9–14.0.
  That is inside the hallway's west wall.
  - These were walk-backs from the junction vestibule.
  - The vestibule is shared, and another explorer's arrival had rebuilt it as
    their own, wider corridor.
  - Six blocks back and two or three to the side mapped into the three-wide
    hallway's wall.
  - Returns to the hallway now keep the step back but clamp the sideways offset
    inside its walls.
  - Any return whose matched spot would collide, or leave the hallway, lands
    just in front of the player's own door, with a log line.

## Breaches

- A BREACH now needs three consecutive ticks outside every valid House volume,
  so a one-tick position during a same-level shift or a geometry change does not
  eject anyone.
- Every breach logs `The Oldest House breach for <name>`. The line gives the
  manor-relative position, each validity check's result (domestic, near door,
  deepened hall, stack, finale, hallway revealed, hallway), the labyrinth place
  and any players within sixteen blocks. A future report can then name the
  volume that rejected the player.

## Verification

- `SharedDoorTests` uses two native players at a real junction entry door
  built by the tiered carve. It covers shut once, reopen from reach, the open
  doorway while another explorer stands in it, shutting after the doorway
  clears, and a vestibule copy that leaves an occupant's cells.
- `SeamDoorTests.aDoorStaysOpenForTheNextPlayerAndShutsOnceNobodyIsAtIt` covers
  the front door: it stays open for a follower and the idle closer shuts it once
  both players leave.
- `SeamDoorTests.aDoorClickedFromFullReachIsNotUndoneOnArrival` reproduces the
  4.45-block exit from the log. The focused seam namespace now declares four
  cases.
- `SharedDoorTests` also reproduces the logged walk-back: six and a half blocks
  back and 2.6 to the side in the junction vestibule must land inside the
  hallway.

Layout 32 / protocol 32 are unchanged.
