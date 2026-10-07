# Finale route preparation — 0.4.52

The operator shortcuts now prepare the complete physical route before moving the selected player or starting an ending clock. The native staircase builder still authors the whole ascent and chamber asynchronously. Preparation holds native chunk/entity tickets through the entire stair column footprint, cell, chute and lower escape hallway. The existing lower floor plan is shared with operator preparation, so a ready cell from an older save cannot leave the later hallway unsupported. Only absent supports are placed. A living peer standing inside an absent support makes the shortcut wait until that space is clear.

The physical dogleg, water landing, finite cache, jump/crouch obstacles and final native door are dressed before arrival. Saved dressing checkpoints retain original containers and depleted inventories. Normal finale progression remains intact; the change is called only by explicit operator shortcuts. Layout 35, protocol 34, forty-three Witness sources, thirty-three resolutions across two story kinds, three endings.

## Testing

Use a world copy. `/oldesthouse test ending escape` or its `collapse` alias starts the real wounded creature and collapse. Take the west breach, fall into the water, jump the rubble, crouch beneath the lintel, follow the complete hallway and pull the final handle four times. Completing this ending removes the shared House and evacuates its residents. `/oldesthouse test ending witness` starts peaceful release and prepares the real staircase for the journey home. `minotaur` and `boy` inspect the same closed native cell without committing an ending. `defeat` invokes native permanent defeat. Cancellation, logout/world-change checks and offline-owner exclusion remain.

The build also includes the [Ted the Caver repair](CAVER_INTERACTIONS_0_4_51.md): native floor/wall torch placement and recovery, native pickaxe mining of the actual cracked aperture, confirmation only after uncanceled removal, and the retained timed right-click excavation option.

## Required validation

400 declared native tests, 29 focused multiplayer cases, 9 cave cases, all existing gameplay suites, native package/writing/client proofs and the established two-client reconnect expedition. The new command traversal begins with the later hallway absent, preserves a depleted original cache and the actual prisoner, follows native movement through the fall, all obstacles and four timed pulls, and verifies complete owner/peer evacuation. A separate native peer case checks safe waiting and resumed preparation without shared or personal progress leakage. Exact-head validation pending.
