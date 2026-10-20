# Home encounters — 0.4.110

Faint knocking and returned possessions take place in the main Tudor manor, in its authoritative interior dimension. They do not create separate vignette rooms or require a companion. The missing hallway remains a proposal: an entrance the player has already used could later lead into an ordinary cupboard while the destination remains saved. This change removes no hallway.

## Knocking

After a genuine labyrinth return, once the impossible doorway is revealed, the reader may hear three quiet groups from the parlour/study partition. The middle group is slower. Crouching close to the partition after the signal produces a delayed answer from the study's hall wall. It then falls silent. Leaving, sleeping, opening a menu or switching to spectator pauses its occupied clock. It occurs once per reader unless explicitly replayed.

Native positional sound packets go only to the reader. The `home.knock` sound event reuses the existing three-knuckle recording at low volume, subtitled **Faint knocking**. There is no actor, screen effect, explanatory chat or objective marker. The wall and doors remain ordinary physical blocks.

## Returned possessions

When a survival player deposits possessions into previously vacant slots of the explorer's camp's existing barrel, up to three stacks can become a personal trace. Actual inventory changes must account for those deposits. Existing supplies, merged stacks and simultaneous sessions with another reader do not qualify.

The possessions stay in that cache until the reader returns and spends six occupied seconds in the manor. Each original stack can then appear separately on the upstairs reading-bedroom floor, removed from its original cache slot. These are persistent native items that the owner can pick up. Every original component and stack count remains unchanged. There is no invented equipment, bag or temporary companion.

Delivery holds one temporary source-chunk lease and waits for native entity loading, an unused source menu and supported, vacant, unwatched floor positions. Actual spectator cameras count. Player blocks and items are preserved. A retrieved or changed stack, or a removed cache, yields nothing from its saved description. The return never searches somebody else's inventory, another container or the Mother's custody. Completed floor items are never replenished.

## Operator commands

Requires operator level 2. Enter the main manor first; copied homes and separate scenes do not qualify. `/oldesthouse` is also supported.

| Command | Effect |
| --- | --- |
| `/ghosthouse test home` | Show the controls. |
| `/ghosthouse test home status` | Show this reader's saved state for both events. |
| `/ghosthouse test home knocking` | Replay the knocks. Listen downstairs near the parlour/study partition; the first group starts after four present seconds. Crouch close after the third group. `knocking start` is an alias. |
| `/ghosthouse test home knocking reset` | Stop and suppress the natural sound for this reader; `start` replays it. |
| `/ghosthouse test home belongings` | Stage one actual main-hand item in finite saved test custody. Stay home for six seconds with the upstairs reading bedroom out of view, then inspect its floor. `belongings stage` is an alias. |
| `/ghosthouse test home belongings return` | Skip the return/wait gate for an existing deposit or staged item. Native loading, cameras, support, occupancy and original-item checks still apply. |
| `/ghosthouse test home belongings reset` | Cancel the return. Undelivered test custody is refunded once; make an inventory slot free if necessary. Natural cache contents and already delivered originals stay where they are. |

The fixture never fabricates gear, marks a natural return, completes a story, changes an ending or resets the manor. A full inventory retains pending custody and reports the need for room.

## Verification

Six native regressions exercise real commands and positional packets, private hearing, crouching, paused clocks, original deposits, three preserved stacks, reload, missing-source exclusion, spectator cameras, owner pickup, blocked placement, repeat-safe refunds and contended sessions. They join the existing multiplayer namespace and its declared-count gate. The full corpus is **687 cases**.

These domestic events add no Witness source: **46 sources / 35 required resolutions / two kinds / three endings**. Protocol **82**, labyrinth layout **45** and manor layout **2** remain. All 1,000 PNGs are unchanged. Texture CRC/decode, eight font grids and character-art checks pass locally. Local Java is 17; compilation/native execution require the configured JDK 21 CI host. Exact-source CI and ordinary in-client listening/return remain pending.

Baseline Plains source `7ee7c047e40c594e323956df8dff7869ed5c7e7b` passed all twelve jobs in [run 39416482891](https://github.com/Knaitoe/the-oldest-house/actions/runs/39416482891), including the then-declared 681 cases, native assets/architecture and both connected clients/reconnects. The PR matrix also passed after retrying an older Elk timing failure. Four native crouch views, grounded 0.802-block bounds, projected fallback and both actual camera exposures were inspected. Baseline evidence does not verify 0.4.110.
