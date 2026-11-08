# Texture pass, release 2: the stone halls (0.4.374)

The plan (docs/TEXTURE_PASS_PLAN.md) found the stone halls, the ordinary ways on from the seventh forward crossing, entirely
vanilla: stone bricks, polished andesite and diorite, chiselled bricks. Release 1 (docs/TEXTURE_PASS_HALLS_0_4_219.md) gave the
domestic halls their own plaster and boards. This release does the same for the stone.

## What changes

All six stone halls (gallery, crossing, descent, arcade, bend and landing) and the grit gallery now wear their own stone:

| Was | Becomes |
|---|---|
| Stone bricks in walls and vaults | Dressed ashlar: two courses a block with staggered joints, each stone its own tone, lit along its top and shadowed at its foot, the face finely tooled with a pit or two. About one stone in four is weathered: darker, with rain marks run down from the joints |
| Cracked stone bricks, and about one plain stone in twenty | Cracked ashlar, in six different cracks |
| Mossy stone bricks | Damp ashlar: the same stone, its lower half darker and a little green under a wavering tide line |
| Stone bricks or cracked or mossy bricks at the foot of a wall, beside a floor someone stands on | A damp course: the last of the ashlar, a dark slate course laid in the joint, and a plinth stone below it, darkened where the damp stops |
| Polished andesite, or the older halls' brick path, as a floor | Flagstones, four stones to a block, laid in four turns so the joints do not repeat. Down the middle of the hall they are worn: paler towards the middle of each stone, where feet have polished it |
| Chiselled stone bricks (the plinths and courses) | A carved band: a moulding top and foot, and between them a running chevron cut into the stone |

### The cracks

The owner asked for variance in the cracked stone. A cracked ashlar block shows one of six cracks, chosen by where the block
stands, so two cracked blocks side by side rarely match:
- down one of the lower stones;
- along the upper stone;
- through the joint between the courses, with the corner chipped where it crossed;
- a crack that forks in two;
- a spalled chip gone out of the face, with a short crack running from it;
- a faint hairline across both courses.

Each crack is a dark split with the edge it broke from catching the light, so it reads as a crack in the stone and not a line painted
on it, and each is drawn over its own stone, so the tones differ too. The six live in one block state and the game picks one by
position, as it does for vanilla grass.

The halls laid with cracked courses (gallery, crossing, descent) keep them where they were. The finish also cracks about one plain
stone in twenty in every hall, chosen by its place in the hall, so the arcade, bend and landing, which were laid with none, show
cracks too. Cracked stone stays well under a fifth of a hall's ashlar.

Eight blocks, thirteen textures, all drawn in code by `tools/draw_stone_hall_art.py` (CI and the quick gate run it with `--check`).

## How

`StoneFinish` is the last thing done to a stone hall as it is built (`StoneHalls.build` after its doors; `TraceRooms.buildStoneDust`
after its doors and before the grit settles). Like `HallFinish`, it swaps a surface only from the vanilla block it was laid in, one
for one. Each new block copies the collision and sound of the block it replaces, and is as unbreakable as the halls already were.
Nothing moves: no shape, door, lamp, rib, stair, story volume or gameplay coordinate changes.

- **Floors first.** A block of polished andesite or brick path is a floor when no wall stands on it (a table, a chair or a fallen
  rib may) and the hall's own roof is somewhere above it, looked for no higher than the top of the hall. The top of an outer wall,
  with nothing of the hall over it, is never a floor.
- **Walls next.** A wall's foot is judged against the finished floor beside it, so the damp course follows the floors exactly.
- **What stays.** The dry fountain's diorite rings, the stair and slab ribs and the parapets.

Every read and write goes through `BuildBlocks`, so a paced build is the same as a direct one, block for block. Every choice the
finish makes comes from coordinates relative to the hall. (Which courses are cracked or mossy, the hall's builder had already
chosen by world position; that is unchanged.)

The hall's stone sound, its dust and its place in the dealing are unchanged.

New builds only. A world that already carved its stone halls keeps them as they are.

## Not in this release

- The quiet and threshold halls (last sconce, watched threshold, mirror hall, blind stretch, quiet hall): release 2b. Their
  hazards read some of their own floor and wall blocks (the watched threshold's moving door looks for its terracotta; the lamps and
  mirrors hang from particular blocks), so they need their own finish, not this one.
- The great staircase: release 3.
- The halls' working doors stay vanilla.

## Tests

- **`StoneHallTests.stoneHallsWearTheirOwnStone`** (new) builds all six stone halls and counts, for each:
  - more than 100 ashlar faces, more than 5 of them cracked and never a fifth;
  - more than 20 flagstones, at least one worn;
  - more than 10 wall feet, as a damp course or a carved band;
  - no vanilla stone bricks of any kind left in the hall;
  - every way on still walked to.
- **`TraceRoomTests`** (the grit gallery case) now expects flagstones underfoot and the hall's own masonry on its walls.
- **`ExplorationPassTests`**: the paced construction case still finds the stone arcade the same, paced or direct, block for block.
- The client check renders every architecture view as before.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
