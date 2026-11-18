# Twelve hallway variants

Two ordinary routes and one physical hazard for each part of the walk. The new places are appended; existing rooms are not rebuilt.

| Section | Ordinary routes | Hazard |
| --- | --- | --- |
| Early, before stone | **Parallel Passage:** adjoining corridors divided by glazing, with two connecting doors. One way on is halfway down the first lane; the other requires doubling back in the lane beyond the glass. **Back Stair:** a half-step stair folds above the entrance passage. The upper landing branches west, or continues below the far end of a second flight to an east door. | **Falling Plaster:** three bowed ceiling spans between sound lintels. Approaching a span starts two seconds of dust and visible cracking. It falls once, leaving loose chips over a sound floor. Side recesses shelter a reader. An exposed reader loses at most one heart; the fall itself cannot take their last half-heart. |
| Mid, stone | **Crossed Galleries:** an upper bridge looks into a lower gallery. The side stair descends to the lower way on; the upper passage also continues. **Three Vaults:** three broad chambers, their vaults alternating direction, joined through narrow openings on alternating sides. A door branches from the middle chamber; another stands beyond the last. | **Steam Walk:** three piped bays, each with a masonry recess. Rattling and a thin plume precede a full jet across the walking lane. The jets use one saved, staggered room cycle. The recesses stay outside the damaging volume. |
| Yard | **Behind the Garages:** a lane between garage backs, under a roof with an opening to the sky, then out onto an apron. A side gate and the drive both continue. **Orchard Rows:** two low paths between tree rows meet at a gate beside an equipment shelter. A lateral path reaches a second door. The shelter's barrel opens. | **Downed Line:** a fallen cable crosses a wet service lane. The pole's work light dims, with humming and sparks before the live interval. Readers can wait for the short crossing or take a longer, raised dry bank, with half steps at both ends. |
| Deep | **Dividing Wall:** a tall, bare chamber split by a massive wall. Narrow passages on either side rejoin behind it, where the onward door is hidden. A shallow recess resembles a continuation. **Low Lintels:** a level floor through high chambers, with low openings alternating from one side to the other. Each fits a standing player. | **Opening Seams:** three floor joints grind and widen before opening. Sound banks separate them. Each opening has a lower catch ledge and half steps back to the preceding bank. A closing joint waits until its floor volume is clear of players, pets and other entities. |

## Placement and pacing

- Parallel Passage and Back Stair enter the domestic pool. Falling Plaster is eligible only at depths 6–8.
- Crossed Galleries and Three Vaults enter at depth 9; Steam Walk at 10. They leave the pool when only the deep halls remain, at 21.
- The three yard additions are eligible from depth 6. They participate in `YardStretch`, so they follow the back-door entry and the existing maximum stretch of three yard places.
- The deep additions enter at depth 16. Their architecture uses the existing smooth dark surfaces, with no installed lights or furnishings.
- Ordinary variants have greater individual weight than their section's new hazard. All four hazards use the existing physical-trial chance and spacing. They earn their own trial mark when crossed by a way on, and contribute no Witness story credit.

## Hazard state

Steam, wire and seams use a 240-tick occupied cycle per room. A bay warns for 40 ticks before its 40-tick active interval. The three bays are staggered by 60 ticks, each beginning with a safe approach. With no survival reader in the room, its clock pauses. SavedData retains clocks, fallen spans, floor stages and damage cooldowns across a reload.

Steam and wire deal two health points per successful hit, with at least 20 ticks between hits to the same player. All readers see the same world blocks and cycle. The seam restores only its missing floor, after checking the occupied volume. Recovery does not require an item, a second player, a teleport or a forced movement effect.

## Existing assets

Domestic plaster, cracked plaster, trim and worn boards; the atrium's steps and balustrades; ashlar and worn flagstones; smooth dark walls and floors; tank pipes and outlets; yard siding, gravel, bulbs and tree materials; the power system's cable and work light. Small generated models provide the bowed plaster, loose chips, floor joint, dark half step and fallen cable, all using those existing textures. No new texture or sound files are added.

## Checks

`HallwayVariantsTests` walks a native survival player through every promised way on, including both routes around the dividing wall, both orchard rows, the dry bank and each seam's recovery stair. Separate cases cover warning intervals, exposed and sheltered players, damage cooldowns, nonfatal plaster, once-only falls, occupied seam closure, empty-room clocks and SavedData reload. The final focused run also includes the existing slot-overlap, interior-height, dealer-pacing, yard-routing and authored-room checks.

`ArchitectureTests` exports every new room. `tools/draw_hallway_variants.py --check` is included in the quick job, and the new models use the shared flicker settlement code.

Local result: compilation and packaging passed; all 25 required cases in the focused run passed, including the 19 new cases. Texture decoding, particle references, font and comfort checks, model generation and the full model flicker check passed.

Full workflow and client rendering remain pending verification gates for a merge.

## Merged (0.4.712)

Brought into `feature/vignette-environment-audit-0.4.125` on 18 November (`docs/INTEGRATION_0_4_712.md`). Slot 246 had gone to
*The list* in the meantime, so the twelve stand at slots 247 to 258 in the same order; nothing else in them changed.
