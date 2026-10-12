# Lake house: integration note

The house at the bottom of the lake is being built on its own branch, `claude/funny-edison-dmjvs1`, cut from `feat/playtest-0.4.64` at b662b89. The design is docs/HOUSE_AT_THE_BOTTOM_OF_A_LAKE_DESIGN.md. This note says what the branch adds and exactly where it touches shared files, so it can be merged onto the main line once the owner's current work is in.

## How it is kept separate

- Almost everything is in new files.
- Each shared file gets one line or one `case`, beside the matching Three Fingers line, so a merge conflict is easy to read.
- The branch does not touch AGENTS.md, README, the release checklist, gradle.properties (mod version) or the network protocol. Those are set once, at integration.

## Phase 1, on the branch now

- **The place.** Slot 95, outdoor, multi-visit, at a fixed base height of y 40 so the deep lake and the house's basement fit.
- **The build** (`LakeHouse.build`):
  - the boathouse, with the rental log on a lectern and the account on a shelf;
  - the dock and the clear lake;
  - the culvert under the service drive: two wide, one block of air over one of water, so a seated reader in a canoe just fits;
  - the third lake, about 26 blocks deep;
  - the two-storey brick house on its bed: brick walls, waterlogged windows, an open front doorway, the stone step and its handrails, the garden, a slate roof two blocks under the surface, the chimney;
  - the raft tied over the chimney;
  - woods all round, and banks that rise at every edge.

  Lake water stands against solid ground on every side, so nothing drains. The scenery passes (polish, composition, review, playtest repairs) leave the scene alone.
- **The canoe** (`LakeHouseCanoe`, entity `lake_house_canoe`):
  - A vanilla boat underneath, so it floats, paddles and seats two the usual way.
  - Drawn by its own model and renderer (`LakeHouseCanoeModel`, `LakeHouseCanoeRenderer`): long and narrow, tapered ends, upswept stems, two seats and a yoke, single-blade paddles, and the boat's water mask.
  - Each reader is lent their own, waiting by the dock. It cannot be broken and drops nothing. It is put away when its reader has left the scene and nobody is in it. Anyone may ride in it; a literary actor (Tess, later) only sits behind a reader.
  - Its paint has three states, fresh, chipped and scraped, drawn by tools/draw_lake_house_art.py into `textures/entity/lake_house_canoe*.png`. Nothing chips it yet; the culvert will in a later phase.
- **Texts:**
  - The source is a canoe rental log, in original words.
  - There are three endings, each with its own account (`LakeHouse.account`).
- **Witness:** one new personal source, `lake_house`, "The house in the third lake", of the connection kind. That makes 45 sources and 34 required.
- **Saved worlds:** layout 43 carves the scene once, through the usual append guard.
- **Tests:** `LakeHouseTests`, in its own namespace `the_oldest_house_lake_house`. The complete native suite counts it, and no focused job's counter changes.
  - The culvert carries water from lake to lake, and a canoe with its reader fits along its whole length.
  - No lake water stands against open space.
  - The house, roof and raft stand where they should.
  - Two readers get two canoes, apart. A canoe cannot be broken, wears to three states, carries two, and is put away when its reader leaves.
  - The rental log, then an account, resolves the reader's own story.

## Shared files touched

| File | Change |
|---|---|
| `TheOldestHouse.java` | `LakeHouseRegistry.register(modEventBus);` after `LiteraryRegistry` |
| `labyrinth/LabyrinthPlace.java` | `LAKE_HOUSE("lake_house",Kind.MULTI_VISIT,95,…)` after `THREE_FINGERS` |
| `labyrinth/LabyrinthPlaces.java` | `baseAtSpacing`: `place == LAKE_HOUSE ? LakeHouse.BASE_Y : …` |
| `labyrinth/LabyrinthBuilder.java` | `VERSION = 43`; append guard `builtVersion() < LakeHouse.LAYOUT && place == LAKE_HOUSE` |
| `labyrinth/LiteraryRooms.java` | `isLiterary`, `outside`, `source`, `ending`, the build `case`, and spruce `wall`/`floor` |
| `labyrinth/LiteraryTexts.java` | the rental log; `case LAKE_HOUSE->LakeHouse.account(own)` |
| `labyrinth/LiteraryVignettes.java` | arrive, click, talk and the reader tick, one line each |
| `labyrinth/WitnessAccount.java` | `LAKE_HOUSE` story after `THREE_FINGERS` |
| `labyrinth/ScenePolish.java`, `SceneReview.java`, `ScenePlaytestRepairs.java` | excluded like `THREE_FINGERS` |
| `labyrinth/SceneClock.java` | `case LAKE_HOUSE->4` (a summer noon) |
| `lang/en_us.json` | `entity.the_oldest_house.lake_house_canoe` |
| `gametest/ArchitectureTests.java` | lectern source for `LAKE_HOUSE`; a `lake_house_cutaway` view through both storeys |
| `client/SceneVisualProof.java` | 88 → 90 architecture views (the scene's own view and the cutaway) |
| `gametest/HazardVarietyTests.java` | 96 slots, the highest 95 |
| Eleven tests with Witness counts | `Story.values().length==45`, `REQUIRED==34` (Classics, Literary, Goatman, HouseNpc, Hotel, NovelCorrespondence, ExplorationPass, Correspondence, Witness, Stacey, Marginalia) |

## At integration

1. Merge the owner's finished main line into this branch, or this branch onto it, and resolve the one-line hooks.
2. If the main line has moved past layout 42, set `LakeHouse.LAYOUT` and `LabyrinthBuilder.VERSION` to its next number. The append guard reads `LakeHouse.LAYOUT`, so that is the only number to change.
3. Bump the network protocol (`HouseNetwork`, `registrar("56")`) and the mod version: this branch registers a new entity type.
4. If the main line has added Witness sources, slots or architecture views of its own, recount:
   - the 45/34 assertions;
   - the slot test;
   - `SceneVisualProof`'s view count.
5. Add the AGENTS.md paragraph, the release-checklist items (docs/VIGNETTE_RELEASE_CHECKLIST.md) and a canon document for the shipped version.

## Phases still to come

These follow the design document.

- **Breath:**
  - the vanilla air bar, with the reader's own aids not working;
  - the lent gear: free dive, then the helmet on the raft compressor's hose, then chest-slot tanks;
  - the choke and blackout onto the raft.
- **Inside the house:**
  - the rooms, and mining as asking how;
  - dropped items sinking, the dry chairs, the dresses and the painting.
- **Tess:**
  - solo only, and gone when a second reader arrives;
  - the visits and the footprints at home.
- **The finale and endings:**
  - the three endings;
  - the artifacts: the flashlight, the house light, the gold helmet;
  - the empty-lake copy.
