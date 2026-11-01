# One property for rooms built whole (0.4.226)

The owner, 1 November: "Consolidate protection for authored rooms. A single room property should exclude them from generic
furnishing and repair passes. Maintaining several separate exclusion lists makes each new room another opportunity for accidental
alterations."

## What it was

Every generic pass kept its own list of rooms to leave alone, and the lists had drifted apart:

| Pass | Kept its own list of |
|---|---|
| `ScenePolish.applies` (and `SceneDressing` under it) | 37 named rooms and 8 family checks |
| `SceneReview.applies` / `SceneHuntReview.applies` | 32 named rooms and 4 family checks, then an inclusion list |
| `ScenePlaytestRepairs.applies` | 28 named rooms and 4 family checks |
| `VignetteArchitecture.applies` (and `SceneShells`, `SceneExteriors` under it) | 9 named rooms |
| `SceneShells.shapes` | 2 named rooms |

A room missing from one list was changed by that pass. The silo and the back seat were missing from the polish's list (it lit and
bordered them), the playtest repairs' sweep reached the Ghosts set, the child's room, the nightclub, the three lake nights and
Borrasca, and the Last Stop's bins were swapped in by a refit that lived outside its builder. `AGENTS.md` had noted that one flag
would be better.

## What it is now

`LabyrinthPlace.Made`, declared on the room itself, next to its slot and box:

- `Made.WHOLE`: built whole by its own builder (walls, floors, furniture, lights, grounds, papers). No generic pass touches it.
- `Made.DRESSED`: built, then dressed by the generic passes. The older halls, hazards and scenes that rely on them for their lights
  and furniture (61 rooms) say this.

A room that says nothing is **built whole**. So a new room is protected unless it asks for the passes, and forgetting to ask shows
up as an unlit, unfurnished room in its own testing, not as an alteration nobody notices.

`LabyrinthPlace.authored()` is true for `Made.WHOLE`. Every generic pass asks it, and nothing else, before touching a room:

- `ScenePolish.applies`: `!authored()`, a room box and a slot.
- `SceneCraft.applies`: `!authored()`, and its own list of rooms it composes (all dressed).
- `SceneReview.applies` and `SceneHuntReview.applies`: `!authored()` and literary. The elk carcasses keep their one exception (their
  reading place is on the yacht), which is about what the pass does, not about protecting the room.
- `ScenePlaytestRepairs.applies`: `!authored()`, and a vignette, an outdoor scene or the flooded passage.
- `VignetteArchitecture.applies`: `!authored()`, not literary, and a vignette or the explorers' camp.
- `SceneShells.apply`/`shapes`, `SceneDressing.apply` and `SceneExteriors` refuse a room built whole themselves too, in case they
  are ever called from somewhere new.

The Last Stop's steel bins are built by `ElkLot` itself now; `PlaytestScenery130` names only dressed rooms.

## What changed in play

Only rooms that a pass reached by accident:
- The silo and the back seat are no longer polished (no added lanterns, wall fittings or wooded border).
- The playtest repairs' sweep no longer walks the rooms built whole. Their builders already shape panes and fences
  (`BuildBlocks`), lay their own arrival ground and skin the arrival copy, so nothing they relied on is lost.
- The model home and the copied Red Room no longer go through the vignette architecture pass (it did nothing to either).
- The barn well is no longer handed to the scene review (which had nothing for it).

## Tests

`ArchitectureTests.everyGenericPassLeavesRoomsBuiltWholeAlone`: every room built whole is refused by every generic pass; every
dressed room is polished; each room the old lists named, and every family built whole (uncanny, dare, Feet Up, puzzle, added
hazard, dark hall, dark-hall horror, quiet room), is built whole; the named refits (`VignetteInteriors`, `PlaytestScenery130`)
name only dressed rooms; 61 rooms are dressed. The architecture pass's own count of generically dressed scenes is 16 (the model
home is no longer among them).
