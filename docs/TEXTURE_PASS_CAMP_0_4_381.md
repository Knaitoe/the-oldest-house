# Texture pass, release 8: Holloway's camp and the cave's chapel (0.4.381)

The plan (docs/TEXTURE_PASS_PLAN.md) lists Holloway's camp and the preserved cave's pews next. Holloway's hut is a dugout of earth,
and its exterior is dressed by the scene pass; the three rooms north of it, where the hunt runs, were laid in vanilla stone brick,
stone, spruce, tuff and deepslate tiles. The drowned congregation's chapel was a cave of natural stone with pews of dark oak stairs
and a causeway of dark oak planks. This release gives the hunt's rooms and the chapel's woodwork their own surfaces. Nothing moves:
every wall, pillar, sight break, doorway, torch, sign, pew and board is where it was, and the hunt and the congregation work as
before.

## What changes

| Place | Was | Becomes |
|---|---|---|
| Holloway's rooms | Stone bricks on the walls of the first and last rooms, the ceilings of the first two, and the sight breaks | Rough ashlar: big blocks in two courses with wide dark joints. Picked by position, about one block in six has a crack down its top course and one moss in its joints |
| | Stone floor in the first room | Irregular flags, three rows of uneven height, laid in four turns |
| | Stripped spruce walls in the room with somebody's stairs | Hewn timber: planks of uneven widths stood upright, the adze's marks across them |
| | Spruce plank floor there | Camp boards, in two turns |
| | Tuff floor in the last room | Packed grit |
| | Deepslate tiles over the last room | Slate laid in staggered courses |
| The cave's chapel | Dark oak stairs for the pews | The chapel's pew: the seat worn smooth and pale where people sat, the ends and back dark boards |
| | Dark oak planks on the causeway | Darker boards, in two turns |

The hut, its dirt and its fire, the bookshelves, the somebody's stairs, the cobbled deepslate pillars, the latch, the cave's own
stone, the pew ends' fences, the walls and lights along the aisle, the gravel recess and the water stay as they are.

Before, the scene pass weathered some of the camp's vanilla stone into cracked and mossy neighbours. The pass weathers only
vanilla blocks, so it leaves the new surfaces alone; the ashlar's three patterns, picked by position, carry that variety instead.

Eight blocks and eleven textures, drawn in code by `tools/draw_camp_chapel_art.py` (CI and the quick gate run it with `--check`).
The pew's block state is written in full, every facing, half and shape.

## How

`HollowayCamp.build` and `IndianLakeArchitecture.cave` lay the new blocks themselves; there is no pass afterwards. `RoomShell`
holds them beside the child's room's and the boiler room's, and `RoomShell.camp(state)` tests the camp's surfaces. Each copies the
collision and sound of the vanilla block it stands in for and cannot be broken.

New builds only. A world that already built these places keeps them as they are.

## Tests

- **The honest reader case** checks, right after the camp is built, that more than 1,000 faces of the hunt's three rooms are the
  camp's own and none is the vanilla stone, tuff, slate or spruce they were laid in.
- **The cave's originals case** checks that all 72 pews and all 27 causeway boards are the chapel's own and no dark oak stair or
  plank is left in the cave.
- The rest of the Holloway cases (the patrol, the two readers and the latch, the late reader's shield) and the cave's sequence case
  pass unchanged.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
