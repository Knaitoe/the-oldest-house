# The spiral walkway's climbers (0.4.323)

The owner, November 5: "this room is unpassable, if a player falls. the manipulated gravity is great, but it binds a player to the ladders."

0.4.308 moved the recovery ladders to z -24 and stopped each one level with its walk, so a fallen reader has a real way back up. The walkway's pull (`HazardVariety.spiral`) still acted on anyone above the pit floor, including a reader on a ladder. Every five ticks it pushed the climber outward, into the wall the ladder hangs on. Vanilla climbs anyone pressing into a ladder, so the reader rose whether they meant to or not, and each push reset their sideways movement, so stepping off onto the walk was a fight.

A reader on a ladder, and for half a second after leaving one, is no longer pulled. The walk itself pulls outward exactly as before. No layout, protocol, Witness or story change.

New exploration case `HazardVarietyTests#spiralClimbersAreNotPulledButTheWalkStillPulls`: a reader held on the east recovery ladder for thirty ticks gains no push; the same reader on the walk is pulled outward. Without the exemption it fails with the climber pushed (0.026, 0, 0) into the wall; with it, it passes. Exact-commit CI pending.
