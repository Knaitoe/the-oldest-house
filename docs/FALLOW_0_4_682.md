# Fallow 4b: the ash outside (0.4.682)

The second half of the fourth part of *Fallow* (`docs/FALLOW_PLAN.md`). Part 4 (0.4.681) was the cellar after the light: the days
below, the lantern, Opal's bean, Otis's question and digging out. This half is what is outside when they come up: the field, the road and
the farmhouse after the light, and the ash over all of it. Part 5 is the road.

Fallow is still not a Witness source.

## What happens

Times are on the afternoon's own clock, after the light (`FallowAsh`).

1. **The field.** Forty-five ticks after the light (`FallowAsh.START`), once anyone out in it has gone, the field burns from the station
   outward, two tiles of sixteen by sixteen a tick: every head of standing wheat goes to burnt stubble or to nothing (about half each, by
   place), and the soil under it goes grey with ash. Only cells where wheat stood change, so the station, the road, the pads and the yard
   are left as the light left them.
2. **The ground opened.** Ten ticks later, in five places clear of the road, the station, the pads and the farmhouse's yard
   (`FallowAsh.CRATERS`), the ground is opened: a bowl three deep at the middle, two and one toward its edge, burnt black at the bottom,
   with a black lip round it. A column with a body in it is left alone.
3. **The poles.** Ten ticks after that the telephone poles go down across the road, away from the light: the wires gone, about two in five
   broken off at their feet with a stump standing, the rest down from the ground. Each lies across the shoulder and the near lane, its top
   at the far end with the crossarm driven into the ground on end and one insulator still on it.
4. **The farmhouse.** Ten ticks after that the farmhouse burns to its stone sill. The chimney stands to ten blocks, its top broken off. The
   front door stands in a charred scrap of its frame (both jambs and the lintel), and it is still the way on: the fire never touches its two
   halves. The floor where it lay is burnt, with beams and heaps of char and ash on it, clear of the way to the door and of the chimney. The
   porch boards are charred. The windbreak burns standing: every leaf gone, each trunk charred and broken off at its own height. The yard
   round the house is burnt black.
5. **The air.** To everyone at the station who is out under the sky, spectators too: the haze gives way to ash over the first three
   seconds after the light, close round by day (whole at 30 blocks) and closer by night (14), dull by day and near black by night; the sky
   goes to one low, flat ceiling of ash, with no sun, moon or stars in it; the light on the ground is as dim as the late evening by day and
   as dark as midnight at night, on the buried days' own clock (dusk over the three seconds before Otis's nightfall, the dark lifting over
   the first three of the morning). Ash comes down round a reader out under the sky once the field has burnt: grey flakes and pale ones,
   the most of it by day. Under a roof (the store, the garage, the cellar) none of this.
6. **Heard.** No more wind in the wheat and no insects after the light. A wind over the ash, low with a whistle at the top of its gusts,
   plays about a reader up in it every eleven seconds; it is never played about a reader below.
7. **Put back.** A fresh afternoon puts all of it back from its plans where it is loaded: the same wheat on the same soil in the same
   places (the field is sown from the place alone, `FallowSite.wheatAt`, never from chance), the opened ground filled, the poles stood up and
   strung again, the farmhouse built whole from its plans with its windbreak grown from the same seed (`FallowSite.FARM_SEED`) and its yard
   in grass, and its front door set back in its wall as it stood. Whatever was not loaded is put back during the next afternoon, before the
   light, a few tiles at a time and only where nobody at the station is within 48 blocks (the haze is whole by then), spectators counted.
8. **Loaded.** Whatever part of the outside is not loaded when its time comes waits until it is.

## Single and multiplayer

- The outside is one: one field burns, one set of poles falls, one farmhouse burns, for everyone. It changes only while someone is at the
  station, as the afternoon's clock does.
- The air is each viewer's own screen of it: everyone at the station out under the sky sees the ash, spectators included, and nobody under
  a roof does. The wind over the ash is heard where a reader up in it is standing.
- Nothing burns into a body: the opened ground leaves a column with a body in it, and nothing is laid where a body stands.
- Spectators change nothing and keep nothing, but count as watchers when the outside is put back.

## On the reader's screen

- Four new materials, drawn at 16 px: **ash** ground (grey, darker flecks and pale ash), **scorched earth** (darker, crazed), **charred
  boards** (near black, split along the grain, a few embers of brown) and the **charred trunk** (bark burnt to scales, its cut end ringed).
  Brightness is held by the art tool: ash above scorched above charred, the trunk below the ash.
- The pole has two new stages: lying along the road (`pole_5`, a shaft five pixels high) and its top (`pole_6`, the crossarm upright and one
  insulator). The lying textures are the pole's own wood turned on its side, with a cut end.
- The farmhouse fitting has four new stages: a burnt beam lying at a slant with a splinter off it (`farm_4`), a heap of ash with three
  charred boards on it (`farm_5`), and the door's charred frame in the door's own plane (a closed door facing south lies along its
  block's north edge, set back from the sill's face): a jamb post four pixels square on the door's side of each block beside the door's
  upper half, standing on the sill (`farm_6`), and the lintel lying on them across the top of the door (`farm_7`). The farm stages face
  north unturned, so the west jamb is set facing north and the east one facing west. The frames showed it wrong four times before it
  was right: a black box of charred blocks round the door; the lintel floating a block over it; the jambs turned a quarter, centred
  and away from it; and, made as deep as the wall, the jambs' inner faces standing out round the door like two charred blocks. (A
  try at the blocks' south edge stood the frame out in front of the door, with a gap; the frames showed the door set back.)
- The ash sky, fog, dim light and falling ash are drawn by the client (`FallowClient`, `HouseOutsideEffects.drawAsh`).

## Made as

- `FallowAsh` (new): the field by tiles, the craters, the poles, the farmhouse and windbreak and yard; `mend` at a fresh afternoon and
  `leftover` before the next light; `done` and `burnt` for tests.
- `FallowStrike`: `FallowAsh.tick` after the light; the State keeps which tiles have burnt and whether the ground, the poles and the
  farmhouse have gone, and before the next light which tiles are known whole again.
- `FallowRuin.mend` puts the outside back first; `Fallow` runs `FallowAsh.leftover` every twenty ticks, and after the light plays the wind
  over the ash instead of the wind in the wheat and the insects.
- `FallowSite`: `wheatAt` (the wheat by place) and `FARM_SEED` (the farmhouse and windbreak by one seed), so what burns is what comes back.
- `FallowBlock`: the pole's `POLE_DOWN` and `POLE_DOWN_TOP`, the farmhouse's `BEAM` and `BURNT`, with their shapes (a lying pole has
  collision; a standing one only at its foot).
- `FallowRegistry`: the four materials and the `fallow.ash_wind` sound.
- Client: `FallowClient` (ash fog, the ash sky's colour, the station's light by day and night, falling ash), `FallowStrikeClient.now`
  (cheaper when stale), `NovelSceneClient` (the station's time on the ground), `HouseOutsideEffects.drawAsh`.
- `tools/draw_fallow_art.py`: the four materials, the lying pole and its top, the beam, the burnt heap and the door's frame. The burnt
  stubble's ash tips are fewer and darker than part 4's first frames had them, which read as a field in flower. `tools/generate_fallow_audio.py`:
  the wind over the ash.
- `FallowTests.theAshOutside` (new): one reader below at the light; the whole field burnt (ash ground, no wheat standing anywhere), the ground
  opened and burnt black at the bottom, every pole down with its crossarm on end and the broken ones' stumps, no wires, the chimney standing
  broken, the walls gone but for the charred frame, the door kept, the windbreak burnt standing, the yard black; then, the afternoon over,
  every bit of it put back as sown and built, the door kept.

## Every new line

Flagged for the owner. Nothing here names a colour or an age or says how the reader feels.

**A hand on them** (`FallowTexts`):
- The door's frame: "The front door's frame, charred through. The door in it was not touched."
- The pole down: "A telephone pole, down across the road. The wires are gone off it."
- Its top: "The top of the pole, down on the road, its crossarm driven into the ground on end. One insulator is still on it."
- A beam: "A beam of the farmhouse, burnt through. Under the ash it is still warm."
- The burnt heap: "What the farmhouse came down to: boards burnt to char, and the ash of the rest."

**Names:** "Ash", "Scorched Earth", "Charred Boards", "Charred Trunk".

**Subtitle:** "Wind blows over the ash".
