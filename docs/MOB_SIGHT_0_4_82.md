# Moving mobs across the visible landscape (0.4.82)

Built on the latest public Three Fingers update, `d4fd771` (verified executable source `8762577`, run 38174200201: all twelve jobs, 537 native cases and 88 architecture views passed). Review found no release blocker in the cottage's personal wish progression, finite rewards, original custody or owner-bound cast and sound. Its prior updates remain intact.

The reader can see moving mobs across the terrain delivered to them, including diagonal view corners. This applies both to outdoor vignette landscapes and to real exterior mobs seen from inside the mundane manor.

- The native server entity-distance gate follows each reader's effective chunk view for outdoor mobs and scenery projections. Native sent-chunk and broadcast-permission checks still decide which entities are delivered. No entity type, AI follow range or attack range changes.
- The registered client renderer uses the effective terrain horizon instead of the smaller model-size/entity-distance gate. Native frustum culling, depth, fog and private owner checks remain. The minimum entity-distance slider no longer makes a visible outdoor character disappear while its landscape remains.
- The manor's exterior mirror discovers sources across the union of its domestic readers' views, rather than a fixed 40-block band. Every visible projection follows native position, body/head turns and stride each tick; appearance/NBT refresh still runs once a second. Source identities and gameplay remain authoritative in the Overworld.
- Source chunk requests come from each domestic reader's native chunk-tracking view. Additions are asynchronous and limited to sixteen chunks per pass for each ticket class, nearest first. Source simulation follows the server's simulation distance; seeing terrain farther away does not increase the configured simulation radius. Matching target chunks and entity sections must already be ready before a projection registers.
- Leaving or reducing the view releases unneeded source/motion tickets. The last domestic reader leaving removes exterior projections and releases all their tickets, preserving the real sources. Crash-saved orphan scenery is scrubbed when a larger view exposes it. Impossible rooms, original containers and saved routes are not copied or rebuilt.

Layout remains 42, protocol 54, slots 0–94. No art regeneration, saved-world rebuild or new Witness source: 44 eligible sources / 33 personal resolutions / at least two kinds / three endings.

## Verification

The expanded actual-client expedition adds three phases after the complete Goatman night:

1. Both clients receive one moving native wolf in a diagonal visible chunk beyond the previous 80-block server cutoff. Their registered renderer shows it beyond the original model-distance cutoff, still rejects an out-of-frustum wolf and a wolf beyond the effective horizon, and reveals only each client's own distant private actor.
2. Both clients enter the mundane manor and receive one shared moving projection of a real wolf beyond the former exterior-copy band. Native positions and identity are compared; the source and projection remain distinct and the projection has no gameplay authority.
3. Both leave. The production mirror removes the projected scenery and the real source survives.

Four actual world screenshots and both clients' measurements are mandatory in `tools/run_live_expedition.py`, alongside every earlier screenshot and both reconnects. The complete 537-case native suite, all focused jobs, writing, texture, package, architecture and client proofs remain mandatory. Candidate validation pending; successful checks from 0.4.81 do not validate the changed source.
