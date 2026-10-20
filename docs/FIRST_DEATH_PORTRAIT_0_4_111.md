# One face in the family

Development candidate 0.4.111 implements the owner's first-House-death portrait and explicit **one portrait per player** constraint. Keep the quiet home encounters, crouched Plains figure, four-visit Borrasca and all prior Claude, Harrigan and hospital work.

## The change

The first genuine death in a House dimension records that player's UUID and native skin profile. On the next safe loaded opportunity, one existing family portrait in the main manor shows a painted bust of that player. No announcement or apparition. Actual cameras, including spectators and exterior observers, protect the change.

The vanilla painting remains: same UUID, anchor, variant, backing, frame and collision. Only its front receives a runtime texture. Saved and natively tracked entity data carries the skin snapshot and signed texture property, so late tracking and reconnect do not need the pictured player online. Other readers see the same portrait. Ordinary paintings retain their normal renderer.

The painter uses the originals' **16×32** scale and ground colour, skin face/clothing/hat/jacket pixels, restrained warm colours, left-side light and lower/right shadow. No photographic screenshot, AI service or arbitrary external image URL. All 1,000 existing resource PNGs stay byte-identical.

## Only one

The saved per-UUID first-death record is authoritative through later deaths, respawn, rename, reconnect and world reload. Players claim different available originals; normal painting shifts skip claims. Removing a portrait does not create a replacement or take another frame.

Five authored family portraits are eligible. If every remaining original is claimed, missing, edited away, watched or unloaded, the request stays pending; never overwrite another person's portrait or fabricate a new frame. Originals are marked during **new manor construction**; no migration/reconstruction of earlier generated manors.

A death event alone is insufficient: require the native death-stat increase after the complete cancellable event. Late cancellations/rescues, spectators, synthetic event-only calls and ordinary Overworld/Nether/End deaths do not spend this first death. Inventories, Mother custody, finale outcomes and Witness are unchanged.

## Test commands

Permission level 2. `/oldesthouse` remains an alias of `/ghosthouse`.

| Command | Effect |
| --- | --- |
| `/ghosthouse test home portrait preview` | Queue one reversible painted skin on an available original. Run in the main manor. Repeated previews are idempotent. |
| `/ghosthouse test home portrait status` | Show first-death/preview/pending flags, original UUID and saved position. |
| `/ghosthouse test home portrait reset` | Remove only your test preview from its same loaded original. Cannot reset a genuine death or recreate a missing picture. |

Look away from the portraits after staging, then inspect the reported position. To exercise the genuine path, use a disposable world/inventory, die inside the House and return; a second death must keep the same portrait UUID/position.

## Verification

- Five native regressions: actual death/stat/respawn, repeated deaths, rename/reconnect, SavedData and painting-NBT reload, native tracking, late cancellation, ordinary-world exclusion, separate owners, removed originals, spectator/exterior cameras and actual preview commands. **692 declared cases / 95 multiplayer**.
- Native art comparison: both actual family paintings, standard/slim player skins, normal/dim oblique light, async skin-manager resolution, GPU readback, exact generated-pixel comparison, hat/jacket, legacy 64×32 skin and texture cleanup. Direct check: `gradle runClient -PdeathPortraitSmoke`.
- Preserve all earlier tests, assets, 99 architecture views and both actual client/reconnect proofs. All twelve exact-source jobs remain required. Staged art is not a manual ordinary death/return playthrough.
- **Protocol 83** needs matching clients/server. Labyrinth layout 45 / manor layout 2 remain. No Witness source: **46 sources / 35 resolutions / two kinds / three endings**.
- Local whitespace, resource JSON and unchanged-character-art checks pass. Candidate compilation/native/GPU checks are pending.

The preceding home encounters at `0b5104e9` passed all twelve [source jobs](https://github.com/Knaitoe/the-oldest-house/actions/runs/39436551071) and all twelve [PR jobs](https://github.com/Knaitoe/the-oldest-house/actions/runs/39436559047), including all 90 focused multiplayer cases. Those results verify 0.4.110, not this added portrait code.
