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
  both players leave. The focused seam namespace now declares three cases.

Layout 32 / protocol 32 are unchanged.
