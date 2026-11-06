# A hand on the rail (0.4.312)

The owner's focus remains immersion and interactivity. A rail is a physical point of support, not a timed immunity carried away from it.

## Play

In the Listing Hall and Turning Gravity, a grip now remembers the exact rail used. It holds only while that block remains a rail and the reader remains within the existing three-block reach. Stepping away, losing the rail or letting the existing grip lease expire releases it. Returning requires taking hold again. Crouching remains its own way to hold footing. Grip state is personal; another reader still feels the room's pull.

A fresh grip produces a quiet metal contact cue and the action-bar line, “You take hold of the rail.” Repeated Use refreshes the existing lease without repeating the feedback. The room's original listing, gravity, camera comfort, harmless recovery and multiplayer clocks remain.

These are gray navigation rooms: no account, Witness source, keepsake, protocol or layout change. No existing world is rebuilt or migrated. The Mother of Strays is untouched.

## What the preceding broad run found

[Run 42161159782](https://github.com/Knaitoe/the-oldest-house/actions/runs/42161159782), source `cc8f2bd`, passed the quick gate, both Tension cases, the radial counter, the exploration suite and the other focused gameplay/client jobs except two verification failures:

- **Goatman:** all 58 native cases passed, but the job's hand-maintained file list expected 57. The new counter class was absent from that list. The gate now derives its exact expected count from every Java holder in the namespaces it actually runs, including both literal and `MOD_ID` namespace forms. It still requires every declared case.
- **Full corpus:** 1,085 native tests ran; the sole failure was the existing Listing Hall item-drift assertion. Its ItemEntity retained the constructor's random toss velocity, which could oppose the hall's pull during the measurement. The case now starts the actual item at rest, retaining real entity motion, the same occupied-clock phase, drift distance, crouch and rail assertions. Its isolated fixture also marks its dummy manor initialized so unrelated terrain copying does not compete with the hazard it builds itself.

## Checks

Two additional native hand-interaction cases cover both rooms: a held rail secures only its reader, walking out of reach releases it, returning permits a new grip, removing the actual rail releases it, and crouching still holds. The quick native preflight now runs these with the original listing drift and the three 0.4.311 regression cases, requiring six passes. The full namespaces retain their independent complete runs.

Local source/whitespace and count checks precede the push. Compilation, native execution and full exact-commit CI remain pending. This candidate is not a claimed client playthrough or a verified release.
