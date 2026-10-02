# Shells and outdoor edges · 0.4.28

> Historical 0.4.28 notes. The 110-block haze is superseded by 4,096-block physical spacing in 0.4.29, which also completes the stair, town and exteriors. See [the current release](ARCHITECTURE_0_4_29.md).

Layout 26, protocol 28 (unchanged). Install the same JAR on server and clients; the outside haze is drawn by the client.

0.4.27 furnished the vignettes. This pass gives their rooms the structural reading the manor has: framed walls, cased openings, windows with depth, hearths, and ceilings and floors that are built rather than painted. Outdoors, the land itself now closes each scene. No scenery grants Witness evidence; the pool stays seventeen sources, thirteen required across two kinds, three endings.

## Indoor shells

`SceneShells` runs once per scene, in place, after the room's furnishings exist (checkpoint `scene_shells_0428`, cleared only by an explicit rebuild). It detects each room's actual wall, floor and ceiling materials, then:

| Element | What it does |
| --- | --- |
| Framing | Posts or pilasters at wall ends and at a rhythm along each wall, offset per wall so facing walls never mirror |
| Openings | Doors and archways get jambs and a lintel; partition ends are cased |
| Rails | Dado, bumper or picture rail; the study has green plaster above its panelling |
| Windows | The opening is cut into the wall with a sill; glass is set one block back; behind it is frosted daylight (a light and backing) or a drawn curtain |
| Fireplaces | A brick or stone chimney breast in the wall plane, a cold grate set back into it, a hearth stone in the floor and a mantel shelf |
| Shelves | Bookshelves inset above the dado, with a sill and lintel |
| Ceilings | Beams across the short span, coffered grids, beams dropped below the ceiling in tall rooms, tiled panels in institutions |
| Floors | A darker border along the walls, or chequered tiles |

| Scene | Character |
| --- | --- |
| Floorboards | Dark timber framing and joists, a brick fireplace, red curtains |
| Hide-and-clap | Birch framing and dado rail, yellow curtains |
| Model home | No exposed timber: a quartz rail and frosted daylight in every outer wall |
| Harrigan | Panelling to the rail, green plaster, coffered and dropped beams, a blackstone fireplace, inset shelves, grey curtains |
| Whale | Stone pilasters, a bumper rail, frosted panes, a white panel ceiling with a grid |
| Hospital | White pilasters, grey bumper rail, chequered tiles, a quartz panel ceiling with a grid |
| Karen | Birch framing and beams, curtains kept drawn for the projector |
| Zampano's archive | Spruce framing, rail and beams; nothing pushed into the alley outside |
| Goatman trailer | Its single skin is also its outside, so only a vinyl floor |
| Preserved cave, Holloway, explorer camp | About one exposed stone in six weathers into its neighbours: cracked or mossy bricks, cobbled, tuff, calcite, rooted earth |

The pass never changes a door, a block entity, a block another block or entity hangs on (frames, clocks, signs, item frames, paintings), a story volume (the model-home tree and chair stack, Harrigan's funeral doorway, the trailer bunks, the Mother's stairs, projector cones), the copied vestibule, or any cell people walk through. Windows and fireplaces only open where the space behind the wall is solid fill or empty. The caver's cave keeps its own weathering and restaged stone; the Mother's den keeps its timber.

## Outdoor edges

| Scene | Edge |
| --- | --- |
| Plain | Invisible walls removed. Dunes rise from the walkable core in steps of two blocks or more, so they can be seen but not climbed. A ravine crosses the far end before the figure's ground, which now runs on to well beyond the haze |
| Zampano's courtyard | The invisible box is the brick facades of surrounding blocks of flats: plinth, string courses, pilasters, framed dark windows, cornice and parapet. The archive's flat top gets a stone cornice and parapet |
| Barn and well | The flat leaf curtain and evenly spaced spruces are a mixed wood: spruce, oak, birch and dead snags of different heights and leans, with undergrowth. The well's cube-crowned spruces become real trees |

The outside dimension gets distance haze ending at 110 blocks, so neighbouring scenes never show on a horizon. `OutdoorBounds` returns anyone a scene has seen standing inside it, who then gets past its edge by pearl, climb or fall, to where they last stood. Creative and spectator players are left alone.

## Verification

The architecture GameTest builds every scene and checks: every reshaped scene records its checkpoint; floorboards and Harrigan each have exactly one fireplace; five rooms have recessed windows; the study is framed, beamed and plastered; no barrier remains around the plain or courtyard; no one-block step climbs the plain's dunes; the barn's skyline is broken crowns rather than a curtain; and the bounds guard returns a survival explorer from beyond the dunes and leaves them alone inside. The existing door-approach, prop-support, preserved-inventory and upgrade checks run on the reshaped rooms.
