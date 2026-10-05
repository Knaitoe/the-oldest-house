# Staircase entrance repairs — 0.4.43

The production arrival copy covers x=-7..7 and z=26..42 relative to the staircase base. In 0.4.42 it overwrote the supply lectern at (2, TOP, 32) and most of Tom's camp. The once-only dressing then considered them already complete.

Tom's camp now occupies x=-15..-8, z=19..26, with a supported three-block passage at z=23..25 connecting it to the first tread. Tom stands at (-12, TOP, 22); the House of Leaves supply is at (-10, TOP, 20). Both sit outside the arrival copy. Each confirmed arrival immediately checks the explorer's personal Tom. Existing UUIDs, owners and reader records remain authoritative.

The small `staircase_entrance_0443` migration waits for the entrance to be vacant and its chunks to be present. A player far down the shaft cannot block it. It moves the original lectern with its components, the existing campfire with its lit state and contents, and existing Toms. It never rebuilds the shaft or clears progress. A missing authored display is repaired once; each explorer's five-leaf supply remains governed by the existing finite `StairBookTaken` record.

Each hearth's placement waits only for its own occupied cell. Retrying incomplete dressing does not refill an emptied lectern. Retreat detection now checks the real entry corridor's width, so visiting the former camp beside it cannot pop the explorer's return stack.

Already polished 0.4.42 scenes receive the existing targeted support and door corrections under a separate `scene_polish_repairs_0443` checkpoint. Their general scenery pass does not repeat. Original block entities, collected property and removed scenery remain intact; explicit room rebuilds clear both checkpoints.

Five native regressions exercise production arrival copying, original migration, two real survival players and separate Toms, native walking clearance, occupied hearth retries, actual saved-door retreat and upgrades of already polished scenes. The existing full staircase-plan test also checks the camp connection and the five actual hearth supports. A release requires the entire native gameplay suite, existing focused jobs, client proofs and package checks on the exact release commit.

On an existing world the entrance repair occurs after visitors leave the top entrance area. A world reset is not required.
