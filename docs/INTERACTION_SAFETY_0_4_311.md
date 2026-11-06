# Distance and physical interaction (0.4.311)

The owner asked for the outdoor monster-range correction, a review of the Google Docs notes, and improvements focused on immersion and interactivity.

## The count outside

The counter's broad search previously counted the corners of a box. A living hostile mob thirty blocks east and thirty south was reported despite being more than thirty-two blocks away. The search now applies the actual three-dimensional distance from the keeper, including the exact boundary. The sleeping watch uses the same helper at its existing smaller range. Ownership and the count inside the House retain their existing rules.

`CounterRangeTests` checks the diagonal, horizontal and vertical boundaries, dead and non-hostile exclusions, the real action-bar count, and the sleeping watch's smaller radius.

## A hand on the cable gear

The [new puzzle and hazard proposals](https://docs.google.com/document/d/1FKRfWE2eHIODlXxNXj0GzjufKllDUhFgMJlnB7nPb3Y/edit) ask for reversible mechanisms, visible physical consequences, and no crushing or soft locks. The early Tension Room already exists; this pass finishes its handling of living occupants:

- A counterweight cannot rise or fall through a reader or animal. An obstructed turn leaves the weight, wheel, drum, pin and cleat unchanged.
- A blocked cleat release holds the cable instead of dropping the weight into an occupant.
- Before the platform rises, its entire path and each rider's destination must be clear. A solid obstruction is preserved rather than overwritten.
- The platform lifts the actual living riders by its own rise, retaining their identities and view. It does not replace an animal or move an observer.
- Once caught at the landing, the platform remains up when the cable is paid out. The shared settled state persists and grants no Witness credit.
- Action-bar feedback explains a blocked mechanism. The completion line describes the platform carrying its occupants, and is not immediately overwritten by ordinary wheel feedback.

The existing Tension Room mechanical test is preserved. `TensionSafetyTests` adds a native hand-operated sequence covering a blocked shaft, paying out again, an animal beneath a held weight, a solid obstruction, rider headroom, a reader and pig riding the lift, saved-data reload and the permanent catch.

## Notes reviewed and scope

The [design document](https://docs.google.com/document/d/1xicuvW7FgAGwGhxivuJrQ_AHKJNEDvSXV-5jccuUFeU/edit) and [vignette review](https://docs.google.com/document/d/12hYEBss_5OGtYVULKQAjWX328JOrmPOyk19kO6YbYFA/edit) contain older baseline descriptions alongside later proposals. Existing current mechanics take precedence over those older descriptions. In particular, the picture/fridge personalization and its skin/hair checks are already present in the current development source; this pass does not reimplement them.

The new pipe, clockwork and cold-pantry rooms and the deep Tension rooms remain separate work. They need their full physical mechanisms, assets and exploration checks; none is represented here by a renamed existing room. No new story, layout, protocol, migration or Mother-of-Strays behavior is introduced.

## Validation

- Local journey-measurement unit tests: 17 passed.
- Existing Tension fitting and original-audio generators: check mode passed.
- All 3,414 shipped PNGs decoded with valid chunk checksums.
- Whitespace checks passed.
- The quick CI gate retains its existing native smoke and additionally runs the original Tension Room case and both new regression cases before the larger suites.
- Compilation, packaged-JAR validation and native execution are pending on this exact commit. This is a development candidate, not a verified release or a claimed client playthrough.

### First CI result and fixture correction

Run [42159474934](https://github.com/Knaitoe/the-oldest-house/actions/runs/42159474934) compiled and packaged the candidate, passed the asset and journey gates and existing native smoke, and passed the original Tension Room mechanical case. The new count's geometric assertions passed, but its immediate message assertion failed. The animal obstruction fixture used an ambient animal, which the House correctly rejects. The follow-up makes the intended animal occupants persistent (as with existing animal fixtures), asserts their actual admission and waits for each to appear in the spatial entity query before operating the mechanism. It keeps every range, obstruction, unchanged-state, rider and persistence assertion.

The fixture follow-up's compile in [42160149076](https://github.com/Knaitoe/the-oldest-house/actions/runs/42160149076) caught two version-specific API errors: the action-bar packet exposes its record component as `text()`, and the spatial box uses six coordinates rather than two block positions. Both are corrected in the next rerun; gameplay code is unchanged by these fixture follow-ups.

Run [42160546255](https://github.com/Knaitoe/the-oldest-house/actions/runs/42160546255) then passed both the original Tension Room and the complete native safety sequence. Only the counter's immediate message assertion failed. Its final fixture now waits for the exact expected text, collecting both valid native action-bar forms (a dedicated action-bar packet or a system packet with its overlay flag), with packet/message diagnostics on timeout. It still requires the displayed count, not merely the helper result. Exact-commit rerun remains pending.
