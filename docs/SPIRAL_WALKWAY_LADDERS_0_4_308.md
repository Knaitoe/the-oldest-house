# The spiral walkway's ladders (0.4.308)

The owner: "Moving the ladders to z −24 gives both sides a working exit at heights 3 and 6, and each ladder will stop at its walk's
height."

## Before

`HazardMaps.spiral` stood six recovery ladders at z −9, −19 and −27 on both pit walls (x ±11). Every one ran from the pit floor up to y 10.
- At z −27 there is no walk at all, so those two ladders led nowhere.
- The rest ran past the walk beside them, onto nothing.

## After

One ladder on each wall at `HazardMaps.SPIRAL_LADDER_Z` (−24), where both walks pass the pit wall. Each ladder's top comes from the route itself (`spiralLadderTop`):
- the east walk passes x 9 at height 3;
- the west walk passes x −9 at height 6.

Each ladder climbs from the pit floor (y −3) to a top rung one block below its walk. A climber's feet rise a block past the last rung, so they come off level with the walk and nowhere else.

## Verification

- **Native layout case** (`HazardVarietyTests.nativeLayoutsHaveVaultsSensorsSupportedRecoveryAndSmallDoors`) checks:
  - an unbroken, supported ladder from the pit floor on each side;
  - nothing at or above the walk's height;
  - a clear, floored step-off onto the walk;
  - a clear foot;
  - the old ladders gone.
- **Played with a real client** on a dedicated server, using keyboard and screen (screenshots kept with this change's playtest notes):
  - The first build, with the top rung *at* the walk's height, brought the climber's feet out at y 4, a block above the east walk. That was corrected to the rule above.
  - After the correction, holding forward on the east ladder from the pit floor stops at local y 3, and stepping back puts the reader on the east walk at y 3.
  - The west ladder stops at local y 6, and stepping off lands on the west walk at y 6.

No layout version change is needed for new builds of the place, but the change applies only when the walkway is next built.
