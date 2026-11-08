# Texture pass: a plan for the owner

The owner, 31 October: "a lot of the map could use improved, custom textures."

This is a plan, not a build. It ranks where custom textures would change the most, from a measurement of every exported scene, and
proposes an order. Nothing in it is built yet.

## How it was measured

The architecture test exports 140 scenes block for block (`build/architecture-proof/*.json`). For each scene this counts the
**visible** blocks, meaning blocks with air or a see-through block beside them. It then works out what share of those are vanilla.

The second measure leaves out natural terrain: grass, dirt, leaves, water, sand, snow, logs and the like. Terrain is meant to look
like Minecraft. What is left is what was built: walls, floors, trim and fittings.

## What it shows

**The scenes built whole are already custom.** About thirty scenes are 0 to 5% vanilla on their built faces, among them:
- the Stanley, the Masque, the nightclub, Usher, the Ghosts set, Feet Up;
- the institute and the hospital, the Whale;
- the dark halls, the work print, Polybius, the dare.

**The halls everyone walks are entirely vanilla.** Every ordinary hall is 100% vanilla on its built faces, and these are the
surfaces a reader sees most, between every vignette.

| Hall | Visible built blocks | What they are |
|---|---:|---|
| Domestic halls (zigzag, vaulted, service landing, pillar, offset, jogged, forked, alcove) | 600–1,500 each | White terracotta walls (about 1,000 a hall), spruce plank floors, stripped spruce trim, grey stained glass |
| Crumbled hall | 690 | Terracotta, stone bricks, cobblestone |
| Stone halls (landing, bend, arcade) | 1,450–1,680 each | Stone bricks, polished andesite and diorite, chiseled bricks |
| Quiet and threshold halls (last sconce, watched threshold, mirror hall, blind stretch, quiet hall) | 800–2,000 each | Smooth stone floors, grey and white terracotta, grey concrete, dark oak |

**The great staircase is the largest single surface.** It is 68 to 97% vanilla:

| Part | Visible built blocks | What they are |
|---|---:|---|
| Upper | 31,700 | Deepslate tiles (20,000), chiseled deepslate, tuff bricks, iron bars |
| Middle | 20,200 | The same |
| Lower | 20,200 | Deepslate tiles, polished blackstone bricks, iron bars |

**Interiors still built mostly from vanilla (50% or more):**

| Scene | Vanilla | Mostly |
|---|---:|---|
| Proofrock (drowned town) | 96% of 18,000 | Smooth stone roads, oak, spruce and dark oak plank houses, grey concrete, bricks |
| Family copy | 100% of 4,400 | Stone bricks (3,300), oak and birch planks |
| Mother of strays | 98% | Stripped spruce, dark oak |
| Puzzle rooms (service lift, turning stair and landing, twin lift) | 98–99% | Stone bricks, spruce and dark oak, bricks |
| Lake house | 100% of the built part | Spruce, grey concrete, stone brick walls |
| Preserved cave | 99% | Deepslate, dark oak pews |
| Holloway's camp | 98% | Stone, stone bricks, stripped spruce |
| Hotel basement | 96% | Smooth stone, bricks, white terracotta |
| Goatman's trailer | 94% | Light grey and white concrete, terracotta, iron |
| Mapping the Interior | 88% | White concrete, spruce, deepslate tiles |
| Cabin at the end of the world | 81–88% | Spruce, dark oak, birch |
| Child's room | 84% | Birch planks, bricks |
| Old cabin | 79% | Birch and oak planks |
| Nurseries and miniatures | 68–77% | Birch planks, terracotta, stone bricks |

Natural stone counts as built in this measure, so Costume Night, Movie Night and the elk scenes only look vanilla-heavy because of
their rock. They are not on the list.

## The plan

Everything here follows the rules the custom scenes already use:
- 16-pixel, vanilla-style textures drawn in code by a `tools/` script, with a `--check` the gate job runs;
- one block family per set, with kinds and variants (as `manor` and `stanley` do);
- the same collision as the block it replaces;
- no gameplay coordinates move;
- new builds only;
- before and after architecture views for each tier.

### Tier 1: the ordinary halls (the most seen; about 24 textures)

- **Domestic halls:** one `hall` block family:
  - plaster in three variants (plain, a hairline crack, a water stain), so a long wall does not repeat;
  - a skirting board and a picture rail;
  - worn floorboards, darker along the centre where people walk;
  - a door casing and a window reveal, replacing the grey glass's bare frame.

  About 12 textures. Every domestic hall changes at once, because they share one builder.
- **Stone halls:** a `hall_stone` set:
  - dressed ashlar in two variants;
  - flagstones worn in the middle;
  - a carved band where chiseled bricks are now;
  - a damp course at the foot of the wall.

  About 8 textures.
- **Quiet and threshold halls:**
  - institutional linoleum in place of smooth stone;
  - painted plaster with a dado line, in place of terracotta.

  About 4 textures, shared with the hospital's palette where it fits.

### Tier 2: the great staircase (about 10 textures and 2 models)

The owner has allowed architecture changes to the staircase, but not to its height.
- worn tread nosings and plain risers;
- well-wall coursing that darkens in three bands with depth (upper, middle, lower), so the descent reads as going down;
- a real iron balustrade (a model with posts and a rail) in place of iron bars.

The block count does not change; only states are swapped. The book-lit darkness and the dark checks stay as they are.

### Tier 3: interiors, in order of how often they are seen

Two or three scenes per release, 4 to 8 textures each:
1. Proofrock: road asphalt and kerbs, clapboard in three paints, shop fronts. It is the largest vanilla surface after the halls. Built in 0.4.377
   (docs/TEXTURE_PASS_PROOFROCK_0_4_377.md).
2. Family copy: left as it is. It is the reader's own house, copied block for block from the Overworld; the stone brick the export
   counted was the test's stand-in house, so there is nothing of the House's own to dress.
3. Mother of strays: left as it is, at the owner's word.
4. The puzzle rooms (one shared set): lift shaft, worn stair and landing. Built in 0.4.378 for the three older rooms (docs/TEXTURE_PASS_PUZZLES_0_4_378.md);
   the newer puzzle rooms already had their own.
5. The lake house. Its rooms already had their own surfaces; the drowned house's brick and foundation, the boathouse, the dock,
   the culvert and the drive were built in 0.4.379 (docs/TEXTURE_PASS_LAKE_HOUSE_0_4_379.md).
6. The two cabins. The old cabin is the reader's own bedside, copied from the Overworld, and is left as it is, like the family copy.
   The cabin at the end of the world is still to do: its walls and roof are shared with the scene composition pass.
7. The child's room. Built in 0.4.380 with the hotel's boiler room (docs/TEXTURE_PASS_ROOMS_0_4_380.md).
8. Holloway's camp. Built in 0.4.381 with the preserved cave's pews (docs/TEXTURE_PASS_CAMP_0_4_381.md).
9. The hotel basement (the boiler room). Built in 0.4.380 with the child's room.
10. Goatman's trailer siding. Built in 0.4.382, outside and in, with its roof and floor (docs/TEXTURE_PASS_TRAILER_0_4_382.md).
11. Mapping the Interior. Built in 0.4.396, its walls, partitions, ceiling, floors, skirt, roof, tunnel and cistern (docs/TEXTURE_PASS_MAPPING_0_4_396.md).
12. The preserved cave's pews. Built in 0.4.381 with Holloway's camp.

### Left as vanilla

- Natural terrain everywhere: grass, dirt, leaves, water, sand, snow and natural stone.
- The House's working doors, where the vanilla door is part of how doors open, close and lead on. Custom door faces can come
  later as their own small pass.

## The order proposed

| Release | What changes | Textures |
|---|---|---:|
| 1 | The domestic halls (built in 0.4.219, docs/TEXTURE_PASS_HALLS_0_4_219.md) | 12 |
| 2 | The stone halls (built in 0.4.374, docs/TEXTURE_PASS_STONE_0_4_374.md), then the quiet and threshold halls (2b, built in 0.4.375, docs/TEXTURE_PASS_PAINT_0_4_375.md) | 12 |
| 3 | The great staircase (well and rails built in 0.4.376, docs/TEXTURE_PASS_STAIRCASE_0_4_376.md) | 10, plus 2 models |
| 4 | Tier 3: Proofrock (built in 0.4.377, docs/TEXTURE_PASS_PROOFROCK_0_4_377.md) | 16 |
| 5 | Tier 3: the puzzle rooms' shells (built in 0.4.378, docs/TEXTURE_PASS_PUZZLES_0_4_378.md) | 11 |
| 6 | Tier 3: the lake house's outside (built in 0.4.379, docs/TEXTURE_PASS_LAKE_HOUSE_0_4_379.md) | 7 |
| 7 | Tier 3: the child's room and the hotel's boiler room (built in 0.4.380, docs/TEXTURE_PASS_ROOMS_0_4_380.md) | 15 |
| 8 | Tier 3: Holloway's camp and the cave's chapel (built in 0.4.381, docs/TEXTURE_PASS_CAMP_0_4_381.md) | 11 |
| 9 | Tier 3: Goatman's trailer (built in 0.4.382, docs/TEXTURE_PASS_TRAILER_0_4_382.md) | 15 |
| 10 | Tier 3: Mapping the Interior (built in 0.4.396, docs/TEXTURE_PASS_MAPPING_0_4_396.md) | 12 |
| 11 | The cabin at the end of the world | 4–8 |

Each release keeps the architecture views, the texture CRC and decode check and the full suite. Any tier can be reordered or dropped.
