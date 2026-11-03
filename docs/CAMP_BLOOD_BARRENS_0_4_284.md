# Camp Blood, in the Pine Barrens (0.4.284)

The owner: "Consider actual regional landscaping and architecture." Camp Blood is a New Jersey summer camp, set here in the Pine
Barrens: a pitch-pine forest over a dense layer of scrub oak and huckleberry, on sand with almost no stone in it, crossed by white sand
roads. The camp already had the right plan: six cabins down a service road, the mess hall, the fire ring. But its woods were mixed
spruce, oak and birch on podzol, its road was gravel, and every reader looking out saw a wall of spruce leaves and a stand of spire
spruces. That reads as northern taiga.

This pass gives the camp the look of the Barrens. No cabin, door, bed, paper, actor, cover position, hunt route or story position moves.
New builds only.

## The woods (SceneCraft, Camp Blood only)

- **Pitch pine** is most of the wood:
  - a bare trunk under an open, flat-topped crown, not a spire;
  - two or three crooked branch arms below the crown, each ending in a tuft of needles;
  - now and then a sprout of needles on the trunk itself, as pitch pine puts out after a fire.

  Arms and sprouts start four blocks up, over a reader's head.
- **Scrub oak** grows as low thickets on a stub of trunk, never above reach.
- **Oak and birch** stay in the mix as smaller trees. Gray birch is common on the Barrens' disturbed ground.
- The trees are set a little closer together than before.
- The cabins' clearings, the road and the camp ground are kept exactly as they were.

## The floor

- The podzol becomes litter over sand:
  - podzol where the needle and leaf litter is deep;
  - coarse sandy dirt where it is thin;
  - patches of bare white sand out in the woods, with pines growing out of them.
- Ground cover is bracken, sparse grass, lichen and moss, a dead bush on bare sand, and low huckleberry and scrub out in the woods.
- There are **no boulders**: the Barrens are sand, with no stone to turn up. The occasional fallen trunk stays.

## The road and the camp

- **The service road** is a white sand road. Its two ruts stay bare, and a little grass grows on its crown.
- **The cabins** stand on a concrete-block course instead of fieldstone. The two chimneys are brick.
- **The fire ring** is a ring of brown ironstone. That is the Barrens' own stone, the bog-iron rock of the old furnace towns.
- Everything else is unchanged: the porches, the signs, the lamps, the woodsheds, the roofs, the mess hall and the beds.

## The edge (ScenePolish, Camp Blood only)

The wooded border beyond the room was spruce for every scene. At Camp Blood it is now:

- pitch pines over a scrub-oak layer;
- at the far edge, a thicket that still cannot be seen through, with scrub oak to head height, pine needles above it and a ragged top.

## Rules kept

- The hunt is unchanged: the slasher still walks through any leaves and the cover nodes are the same.
- Every cabin, bed and story position is where it was.
- No Witness, layout, protocol or story change: 74 / 56 / two kinds / three endings.

## Tests

These pass locally:

- the camp's slasher, bed and session cases;
- the hunter-cover case;
- the architecture pass (`nativeEverySceneHasSupportedDetailAndPreservesOriginalsOnUpgrade`, which checks that the camp stands in a mixed
  wood of oak, birch and pine trunks and that nothing grows through a cabin);
- `everyGenericPassLeavesRoomsBuiltWholeAlone`.

The camp was rendered whole and from its west edge, and checked.
