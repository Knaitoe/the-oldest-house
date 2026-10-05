# Composed scenes — 0.4.46

The literary rooms and outdoor scenes are composed to the standard of the manor. `SceneCraft` runs once per scene, after its polish (checkpoint `scene_craft_0446`).

- New worlds compose each scene as it is built.
- Existing worlds compose one standing scene at a time, only once nobody is in it and its chunks are loaded.
- An explicit rebuild forgets the checkpoint along with the polish.

## What it changes

### Defects the generic passes left

- **Lattice carpets are removed.** The 0.4.42 dressing laid each rug square, then refused every neighbour of a square it had just laid. The result was a checkerboard of lone carpet squares instead of rugs, sometimes on top of ceilings. Those squares are taken up.
- **Scattered ceiling pendants are replaced.** The generic lighting hung a lantern over each dark patch, so the literary rooms read like a warehouse. Those pendants come down; each room keeps the lanterns it was authored with.
- **Clutter stood on ceilings is removed:** furniture, detail and carpet sitting on top of a room's ceiling.
- **The Hill nursery's ending ledger** was authored on the cellar's roof, outside every room, and could not be reached. It now stands on a desk in the cellar the knocks come from (`LiteraryRooms.ending`), and the old roof desk is taken away.
- **The costume-night beach** was a single sheet of sand over the lake. It is given sandstone beneath, so it cannot fall in.

### Interiors

There are thirteen literary rooms. Each gets a composition of its own, built from a common kit:

- **A hearth** where the room has a suitable wall: a chimney breast to the ceiling, a lit grate, a hearthstone, a mantel with candles and a picture over it. The workshop, the Usher hall, the Crimson hall, Bly and the confession room each have one. The elk-fan room's free-standing hearth gains a stack and mantel.
- **Built-in shelving.** Bookcases run along straight walls, stacked on the floor or on the barrels already there, under a slab cornice. The confession room's low shelving rises to full height.
- **Rugs laid into the floor**, with borders, under real seating groups and furniture: runners down halls, rugs before hearths and beside beds.
- **Seating groups** that face something: armchairs drawn up to a fire, a sofa and footstool, a second chair across from an authored one, stools at a kitchen table.
- **Lighting placed where a house would have it:**
  - chandeliers on a room's axis (a four-armed fitting on a short drop, iron in the gothic halls);
  - bracket lamps on every other timber post;
  - standing lamps and candlestands in corners;
  - lamp tables beside beds.

  The previous darkness targets still apply, so dim rooms stay dim.
- **Particular rooms:**
  - Usher's crypt roof, a step above the hall floor, becomes a resurfaced, stepped dais with candlestands.
  - Masque chambers get rugs in their own colours and candlestands, and the last chamber keeps only its red light.
  - The Bly pool gets a kerb, planters and benches, and its small bedrooms get beds.
  - The Hill nursery's cellar has casks along its far wall.
  - Wheel's mirrored bays read as hall, sitting room, study and bedroom.
  - The film set gets a lighting truss, crew chairs, catering and flight cases.

### Outdoors

- **Natural woods replace the planted grid.** The identical spruces planted on a fixed grid, including those standing in the lakes, are taken down. Woods are replanted at irregular spacing, in clumps and clearings, in the species each place would have:
  - spruce, pine, oak and birch, with dead snags in the hunting woods;
  - snow on the crowns in winter scenes.
- **Undergrowth by ground type:** grasses and flowers on grass; ferns, moss and fungus on podzol; drifted snow on snow. Occasional bushes, boulders and fallen trunks appear off the walks.
- **The elk lot's field** is a meadow in drifts instead of a grid of tufts.
- **Lakes:** shorelines are filled into irregular banks, not ruled straight. Reeds and lily pads grow in the shallows; dead stalks stand in the frozen lake's snow.
- **Indian Lake banks:** the checkerboard of single steps is regraded to a smooth rise. The flat wall of leaves behind it is replaced by a close wood with undergrowth.
- **The hotel grounds:** smooth snow drifts replace the stepped terraces, with snowy spruces on them. The novel's hedge animals stand by the maze, and lamps line the approach.
- **Buildings:**
  - The camp cabins each get a porch on posts, a number board, shutters, corner posts and a stone base course.
  - The lodge gets a porch roof and railing, a chair and lamp, shutters, a brick chimney and a woodpile.
  - The end-of-the-world cabin gets porch posts and railings, rocking chairs, a lamp, shutters, a woodpile and a jetty on pilings.
  - The bar gets a door canopy and lamp, painted parking bays, a dumpster and street lights.
  - The camp gets a fire ring with log benches, a picnic table, a gate sign and road lamps.

## Structure

The dressing above furnishes buildings and rooms. These changes rebuild their structure.

- **Roofs.** The generic builders roofed every cabin with a staircase of stair blocks, one step per block, which read as a stepped pyramid.
  - The camp cabins, the lodge, the end-of-the-world cabin and the bar each lose that roof. It is removed by the builder's own formula, so nothing else is touched.
  - Each gets a half-pitch slab roof instead: a block of overhang at the eaves, boarded gables, and a log ridge.
  - The cabins vary between dark oak and spruce roofs and spruce, dark oak and birch gables. Two have stone chimneys and two have lean-to woodsheds.
- **The barn** loses its sawtooth stair roof, and the flat birch ceiling its first box left above that roof, for a dark oak gambrel, steep below and shallow above. It is open to the rafters inside. It has hayloft doors in the gable and a hoist beam with a chain over them; a ceiling block that something hangs from becomes dark oak instead of being removed.
- **Goatman's woods** were a flat wall of leaves with trunks hidden inside. They become a thicket:
  - mixed leaves, with flowering azalea at the trail's edge;
  - an uneven hedge top;
  - trunks showing along the trail;
  - separate trees of dark oak, spruce and birch, with broken crowns.

  The trail itself stays clear, and nothing grows across it.
- **Literary interiors** were boxes with posts. Each room's own wall plane gets:
  - a panelled dado, two blocks high;
  - a rail above the dado;
  - a cornice under ceilings of six blocks or more.

  Posts, doors, glass, block entities and story cells are left as they are.
- **Recessed windows** appear in alternate bays between the posts, with dark glass set a block back like the rooms' own windows. A window is cut only where solid fill lies two blocks deep behind the wall, so none opens onto a passage.
- **The explorers' camp** was a pale box cut into the labyrinth fill. Now it has:
  - hewn rock faces;
  - a floor of trodden earth and gravel;
  - two timber shoring frames carrying the ceiling, with a lamp under each.

## The cast

- **The drowned church's congregation** sat a block above the floor, a row behind its pews. The pews also faced away from the pulpit.
  - The pews now face the preacher; existing churches are turned in place, and keep their water.
  - Each body sits on its pew at floor level.
  - The preacher stands on the floor behind the pulpit.
- **Literary actors placed in a chair now sit in it:** hips on the seat, legs forward, facing the way the chair faces. The confession room's visitor had been standing inside its armchair.
- **The lake witch's hunting body was one dark tone**, so in the dark it read as a box. It now has:
  - pale, bruised skin and blackened claws;
  - wet hair, threaded with weed, hanging over the face, and pinpoint eyes;
  - a torn, sodden dress.

  Her human memory keeps its own skin.
- **Cast proof framing:** the native cast proof no longer cuts off the Mother of Strays' head behind the panel title.

## What it never does

- It only adds blocks into empty space. It replaces only ordinary floor, wall and natural-ground material.
- It never touches:
  - doors or block entities;
  - story volumes, or the literary source and ending papers and their approaches;
  - the copied arrival vestibule;
  - anyone standing in a cell;
  - each story's lanes: actor routes, crawls, sightlines and test positions.
- **Reachability is checked.** Every piece of furniture is undone again if any part of the room a visitor could reach before is no longer reachable.
- **Nothing is refilled or rebuilt:**
  - no original, inventory, actor, story state or Witness evidence is altered;
  - a completed composition never puts back what was later taken away.

## Verification

`ArchitectureTests` checks that:

- every applicable scene records its composition;
- no composed literary room keeps a lone carpet square, and each has a laid rug;
- every literary source and ending can be reached from the entrance;
- each designed hearth exists exactly once, and the nursery's ledger stands in the cellar;
- the camp stands in a mixed wood;
- no tree grows out of a lake;
- the shallows bank rises smoothly;
- the barn has its gambrel ridge and no flat lid;
- a completed composition is not restaged.

The architecture views publish the composed scenes to the build log. Five full-height exterior views show the re-roofed buildings: the camp, the lodge, the end-of-the-world cabin, the bar and the barn. Woods' crowns are left out of these views so they do not hide the buildings. That brings the native client's architecture views to fifty-eight.

Verified source cc0a4f2578463e329efe9e202632df3129629e5a, run 37359541865. It passed:

- all 359 native gameplay tests;
- the multiplayer, staircase, hallway, hotel, literary and seam suites;
- the native client checks, including all fifty-eight architecture views.
