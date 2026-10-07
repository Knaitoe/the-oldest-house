# Cave and barn supports — 0.4.53

The cave marked stone keeps its actual interaction coordinate and joins a low natural outcrop back to the wall. The calcite crust meets the existing stone ceiling, with its native dripstone pointing downward. Existing placed torches remain. Barn fences recompute their native connections to adjacent fences, gates and walls; open/powered gate state and waterlogging remain.

New construction and existing scenes use the same bounded helpers. Existing worlds receive a separate saved checkpoint once the small areas are loaded, actual entity sections are ready and nearby cameras are absent. Any new collision intersecting a living resident waits. Repairs do not rebuild scenes, refill supplies, replace actors/companions or write personal story progress. A saved repair does not repeat after reload or restore later player edits. Explicit scene rebuilds forget this checkpoint.

The build retains the [Ted interaction repair](CAVER_INTERACTIONS_0_4_51.md) and [full operator finale escape route](FINALE_ROUTE_0_4_52.md). Layout 35, protocol 34, forty-three sources, thirty-three resolutions across two kinds and three endings remain.

## Validation

402 native cases, 31 focused multiplayer cases, 9 cave cases and all established gameplay, native writing/client/package and two-client reconnect proofs are required. Two new native cases reproduce the unsupported cave primitives and disconnected rails; they check actual collision, waiting for an owned sitting cat or sheep, retained original torches/container contents, shared and personal records, open-gate passage and saved repair behavior after reload. Exact-head validation pending.
