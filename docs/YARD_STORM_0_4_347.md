# The Storm Field (0.4.347)

The last of the yard's encounters from the owner's draft "The House Has a Yard" (4 November). It is a gray passage outside, not a Witness story. Layout 108 appends slot 208. Protocol 142 adds the new fittings' block states (corn and a windsock). Nothing standing is rebuilt.

From the draft:

> *"A storm field can combine those ideas without committing to a full vignette. From a rise, the reader sees a farmhouse, grain elevator, and power line. At eye level in the corn, they lose all three. The ditch is easy to follow until it fills. The exposed field edge gives a bearing, and the wind there drowns out footsteps. Cutting across the rows is quick and leaves a track of bent stalks. A dry equipment shed midway offers a pause. From inside, you can hear someone following the ditch you just abandoned. In co-op, the reader on the rise can signal until the rain takes the view away."*

`YARD_STORM_FIELD`, `YardStorm`. It is a physical hazard, dealt (weight 2) from the deep halls, after the Windbreak.

The slot grid was full at slot 207 (twenty-six columns of eight at common manor heights), so it now allows thirty columns. No earlier slot moves.

## The field

Through the House's back door onto a verge.

- **The rise.** To the west a grassy rise climbs in three shelves, with old stone steps up its east flank a half-step at a time.
  - From the top, a reader's eyes are above the corn. They see the farmhouse at the far end, the grain elevator beyond the corn to the north-east, and the power line along the east edge.
  - A pole on the top carries a windsock and a yard light, with the light's switch at hand height.
- **The corn.** It stands three blocks high in rows running east to west, across the way north.
  - At eye level in an aisle, the stalks hide the farmhouse, the elevator and the power line.
  - Standing corn has no collision but holds a body back. Once a reader has pushed halfway into a row, the stalk goes over the way they were going and the two above it come down.
  - Cutting north across the rows is quick, and it leaves a track of bent stalks that anyone can follow at a walk. The rows either side still stand.
- **The ditch.** It runs the length of the west side, a block and a half below the field.
  - Its water is boot-deep over stone slabs, with tall grass along the banks.
  - A mud step to climb out comes every nine blocks.
  - It is easy to follow.
- **The open edge.** Along the east side, open grass runs between the corn and the wire, under the power line on its poles, straight to the elevator.
- **The shed.** A machine shed stands in a clearing midway. It has plank sides, a roof of barn sheets and an open front. Inside are a bulb, hay, a barrel, a grindstone and an anvil, all out of the weather.
- **The farmyard.** It is gravel before a two-storey farmhouse, which has lit windows, a pitched roof and a chimney.
  - Its back porch, under a bulb, holds the door that is the way on.
  - The elevator stands to the east with its headhouse and a lightning rod.

## The storm

One clock, which runs only while someone is in the field:

1. **Overcast.** For twenty seconds the sky is darkened, the view clear to the farmhouse, and the windsock slack. Thunder sounds far off once.
2. **The rain.** At twenty seconds the rain comes on, and for twenty-five seconds:
   - the windsock lifts and fills, pointing east;
   - the view closes to the middle of the field.
3. **The storm.** At forty-five seconds, and until the field resets:
   - the windsock stands out full, and the view is taken down to a few rows;
   - the ditch fills to its brim, and its current carries whoever is in it back toward the rise, breath kept and unhurt;
   - the water stays in its banks;
   - lightning finds the elevator's rod every fifteen seconds. It is visual only: no fire, no harm.

Every reader in the field sees the same sky, because each client reads the windsock: rain level, thunder, and fog colour and distance. Under the shed's roof the rain falls outside.

**The open edge.** Once the rain has come, the wind on the open edge is loud enough to cover a footstep, and the grass blows across it.

**The shed.** A reader who comes into the shed once the rain has come hears that the rain is loud on the roof sheets.

- Then they hear someone splashing up the ditch from south of the shed to the far end. Footsteps alternate with splashes, and the corn along the ditch rustles as they pass.
- Only that reader hears it, once in each storm, and nothing is there.

**The yard light.** From the top of the rise, the switch swings the yard light out over the corn to where the reader is looking.

- A spot of light lands above the stalks for four seconds. Its beam is drawn for everyone in the field.
- In the rain it reaches only half as far.
- In the storm it reaches the first rows and no further: "The rain has taken the field."

**Reset.** Once the field has been empty and unwatched for ten seconds, it stands as it was: the corn upright, the ditch low, the windsock slack and the clock back to overcast.

Nothing here hurts or is used up.

## Assets

`tools/draw_yard_fittings.py` draws two new fittings:

- **Field corn**, in four stages: the stalk with its long leaves, the stalk with an ear in its husk, the tassel, and the corn pushed flat. The standing stages are crossed planes, like vanilla's crops.
- **A windsock** on a galvanised hoop: slack, lifting, full.

No new sounds: the thunder, rain, splashes and lever are vanilla's, and the footsteps and wind are the yard's own.

## Tests

`YardStormTests` (exploration), with four readers in the field:

1. **The view.** From the rise, the farmhouse, the elevator and a pole of the power line are in sight. From an aisle in the corn, each is lost behind the stalks. The farmhouse's back door is the way on.
2. **The corn.** A reader cuts north across the rows quickly and leaves a track of bent stalks. The rows either side still stand.
3. **The yard light.** Swung from the rise to a point in the corn, it lands where the reader looks.
4. **The rain.** The windsock lifts. A reader in the shed hears footsteps up the ditch, and the reader on the rise does not.
5. **The storm.** The windsock is full and the ditch fills. Its current carries the wader back toward the rise, unhurt and breathing, and the water stays in its banks. The yard light no longer reaches.
6. **The reset.** The corn stands again, the ditch is low and the windsock slack.

The field earns no story credit. It is exported for the native architecture views.
