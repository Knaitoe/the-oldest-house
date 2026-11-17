# Fallow 1: the station (0.4.672)

This is the first part of *Fallow*, the second of the three vignettes in `docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`. The owner approved the proposal on 17 November ("They read well ... begin work on implementing the vignettes"). It is built to the proposal's recommended decisions: a four-minute clock; a reader outside at the light faints the House's faint and is not killed, and their shadow stays on the wall; the girl's gift is shown; no refuge but the cellar at first. The plan for all five parts is `docs/FALLOW_PLAN.md`.

This part is the place alone: a gas station and grocery on a two-lane road through wheat, late on a dry summer afternoon. Five people are there. Nothing happens yet. The House does not deal the place until part 2, when there is something to do, and it is not a Witness source until part 5.

## The parts

1. **The station** (this release, 0.4.672): the place, the five, a hand on anything, art, sound, the haze and the door bleed.
2. **The afternoon**: minding the store, the girl's plant, the stranger's soda and his walk into the wheat. Dealt from here.
3. **The four minutes**: the tone, the blank card, the lids, the clock, getting people down, and the light.
4. **Buried**: the lantern's oil, the days, digging out.
5. **The road**: the girl's gift, the shadows, the stranger's offer, the account and the Witness source.

## The place

It is outdoors, under the outside sky, in the late afternoon (`SceneClock` mode 16, held, the same hour as *In the Tall Grass*). Every position is relative to the House's door: z 0, facing north. People stand at y 0 on ground at y -1. The scene runs from x -56 to 56 and from z 0 to -136.

- **The store** (x -8 to 8, z 0 to -12, four high) is painted clapboard on a block plinth under a flat tar-and-gravel roof, with the station's name on a board along the front of the roof. The House's door is its back door, so a reader comes in behind the counter.
  - **The counter** runs across the room at z -4, so the House's door opens onto clear floor behind it. The register is on it, with a rack of candy bars toward the front, and Harlan's note propped against the register: the first paper. Its east end is a flap, lifted, so a reader coming in behind the counter goes round Harlan's stool and out by it.
  - **Behind the counter**, Harlan's stool, the back shelves either side of the House's door (cabinets below, cartons, oil and fan belts above, the radio on one), and the television on its bracket in the corner.
  - **The floor**: coolers with glass doors along the west wall under one lit panel (ICE over one door and COLD over the next), a run of shelving from front to back so the register is in sight from the front door, the road maps by the front door, the coffee machine on its stand by the east wall, and four strip lights.
  - **The front**: plate windows either side of the front door, which opens onto the forecourt, and two windows in the west wall. A plain door in the east wall goes into the garage.
- **The garage** (x 9 to 18) is corrugated iron on the store's east side, its front open where the door is rolled up. The car is up on the lift, a little over a reader's head, its two posts and their crossbar under it. Along the east wall are the bench, the pegboard with two outlines empty, a stack of tires and a drum of rags.
- **The forecourt** is concrete from the store to the road's shoulder:
  - the island with its two pumps, each with its dials on top and a bell;
  - the canopy over them on four columns, lights let into its underside, the station's name on its edge, and pencil marks up one column;
  - the diesel pump on its own island by the rig;
  - the sign by the road: the price board, and the name at the top of the pole.
- **The rig** stands by the diesel pump: a long-nosed cab with its sleeper, and its trailer behind, a box on two axles standing on its legs.
- **The yard** (x -27 to -9, z 0 to -19) is trodden earth inside a wire fence with a gap in it toward the forecourt.
  - The keeper's trailer sits on cinder blocks, with skirting round its foot, a propane tank chained to its side, a hitch propped on a block, and a door up a step of blocks. The door is locked.
  - A clothesline runs between two posts with two work shirts and a dish towel on it, and a burn barrel stands by the store's wall.
  - The cellar's doors are two flat leaves on a low concrete curb.
- **The cellar**: under the doors, three stone steps go down from a block below the yard to the cellar's floor, through a gap in its south wall. It has fieldstone walls, a packed-earth floor and a roof timbered from under. Along the west wall are shelves of dated jars, tins, candles and matches and a first-aid kit. Along the east is a cot, its blanket folded. A crock of water stands in the far corner, and a lantern hangs lit from the middle of the roof.
- **The road** runs from one edge to the other: gravel shoulders, two lanes of asphalt and a dashed line between them. Telephone poles stand along its north side every sixteen blocks, their crossarms carrying wires that sag between them.
- **The farmhouse** across the road is two storeys of painted boards on a stone sill, under a shingled gable with a brick chimney. Every window is empty. Its porch runs along the front with posts and a rail and a can of sand full of cigarette ends. A windbreak of trees stands between it and the road, with a gap for the gravel drive. The mailbox at the end of the drive has its name painted over. Its front door is the way on.
- **The pads.** Two fenced squares of gravel lie out in the wheat, each thirteen across: chain-link with three strands of wire on arms along its top, a sign on it, a chained gate on the track side, a light on a pole lit in daylight, a low steel box with a bolted hatch, and in the middle a flat concrete lid with seamed edges. A dirt track runs from the road to each gate.
- **The wheat** is everywhere else, ripe and to the waist, a bowed head here and there. It has no body: a reader walks through it. A dry, warm haze, the colour of the dust off the road, closes in from twelve blocks and is whole by forty-six (`FallowClient`), so no edge of the field is ever seen. There is no haze under a roof.

## The five

They are on stranger phases 263 to 267, painted on the cast's grid by `tools/paint_character_skins.py` (`literary_fallow_*`). The stranger is painted now too (phase 268) and comes in part 2.

- **Harlan**, who keeps the station, is on his stool behind the register (`ActorSeat`, eleven pixels up). He wears a work shirt with his name on a patch over the pocket and a cloth cap. He is grey at the temples, with a day's stubble.
- **Faye**, travelling, is at the coffee machine. She wears a summer blouse and slacks, her hair tied back.
- **Opal**, her daughter, is on the step out front, at a child's height. Her cotton dress comes to her knee, with bare shins and short socks. Her hair is long.
- **Boyd**, Faye's man, is in the garage by the car, in an undershirt and jeans, his hair combed back.
- **Otis**, the long-haul driver, is at his rig: a big man in a flannel shirt with the sleeves rolled, a ball cap and a short beard.

Each has two lines: their first, and a second for every time after. A line is spoken over their head and kept in the conversation history, never in chat. A reader's count is their own (`Heard0` to `Heard4` on that reader's record).

Each is made only if their body is not already at their place, and nothing is made while their place's entities are unloaded, so there is never more than one of each. The car is placed the same way, once.

## Single and multiplayer

The place is recurring and shared: every reader sees the same station, the same five at their places and the same car on the lift.

- A hand on anything answers that reader alone, in a grey line over the hotbar, and changes nothing. The cellar's doors are the exception: they swing back for anyone, all six lengths together, and stay as they are left. The pump's bell rings for everyone near.
- Each reader's talk counts are their own, so a second reader hears Harlan's first line, not his second.
- The afternoon's sounds run on one clock while anyone is there and stop when the last reader has been gone 100 ticks. They are the wind over the wheat, the insects in it (outside only) and the radio on the back shelf (while someone is in the store).
- Spectators are not readers. They open nothing, count nothing and hear nothing counted.

The shared clock of the afternoon and the four minutes begins in parts 2 and 3. From there, where each reader is when the light comes decides their own path.

## Made as

- **The place.**
  - `LabyrinthPlace.FALLOW` ("fallow", recurring, slot 245), bounds (-56,-6,-136) to (56,16,0).
  - `LabyrinthBuilder.VERSION` 143.
  - `FallowSite`: the plan as constants, the build, and where a body is (in the store, behind the counter, in the garage, in the cellar, on the forecourt, in the scene).
  - `FallowRegistry`: twenty-six materials, the farmhouse's shingle stairs and the cellar's stone steps, the fitting block, and four sounds.
  - `FallowBlock`: twenty-five kinds of fitting with stages. Every one is drawn whole, and its collision is only what stands in the way. The wheat, the lights, the high signs, the wires, the note, the lid's middle and the washing have none. The lifted flap, the open cellar doors' curbs and the shut doors' leaves are no more than they look.
  - `Fallow`: the five, the car, a hand on anything, the cellar's doors, the afternoon's sounds, never dealt.
  - `FallowTexts`: every line.
  - `FallowClient`: the haze.
  - The way on: the farmhouse's front door (`OnwardPlacements`).
- **Art.** `tools/draw_fallow_art.py` draws 152 textures and 172 models and their states at sixteen pixels, with `--check`. The store's name and the price board are lettered in three small pixel fonts.
- **Sound.** `tools/generate_fallow_audio.py` makes four sounds from noise and tone, every one subtitled:
  - the wind over the wheat;
  - the insects in it;
  - a radio playing a ball game;
  - the pump's bell.
  
  The door bleed (`tools/draw_door_bleeds.py`, `tools/generate_door_bleed_audio.py`) is wheat heads drifting across a doorway in warm dust, and the wind.
- **Tests.** `FallowTests` (namespace `the_oldest_house_fallow`, CI job `fallow-gameplay`), four cases:
  - The place, every part where it should be. A body walks from the House's door round the counter by its flap, out of the front door, across the forecourt and the road, up the drive to the farmhouse porch, and round to the yard by the gap in its fence. It cannot go into the locked trailer, down into the shut cellar, inside a pad's fence, or out of a shut door. It is late afternoon, it is not dealt, the title fits, and the note says what it should.
  - The five at their places, one each; Harlan on his stool; Opal at a child's height; the car on the lift once. Two readers each hear everyone's first line for themselves, then the second, under their own names.
  - A hand on anything answers with its own line. Sixty-odd things are touched from where a reader can reach them. The trailer's door is locked and stays shut.
  - The cellar's doors: a spectator's hand opens nothing; a reader's throws all six lengths back; a body walks down the three steps into the cellar with room over its head; a second hand shuts them and the way down is covered.
- **Architecture.** The note is the source fitting, and the station lays no rug, only its linoleum and the forecourt's concrete.

## Found and fixed in the making

- The counter ran the width of the store, so a reader coming in by the House's door was shut behind it. Its east end is a lifted flap, and Harlan's stool sits where a reader goes round it.
- Harlan stood beside his stool. The stool is now his place.
- The way on was built twice, once by the site and once by `OnwardPlacements`. It is now built only by `OnwardPlacements`.
- The cellar's doors were to lie at a slope, which the game's model rotations cannot draw at that angle. They lie flat on a curb, as many do.
- A canopy column stopped short of the deck. Its top piece now meets it.
- The open doors' leaves had a thin slab of collision across the well, which a reader walked into on the steps. Only the curbs stand in the way now.
- The last cellar step was a block above the cellar's floor, under the cellar's roof, so a reader on it could not get into the room. Every step is a block lower; the last is on the floor.
- Opal's dress ran to her ankles and read as pyjamas. It comes to her knee now, with bare shins and short socks.
- The counter stood three blocks in from the House's door, in the cell every scene keeps clear for a body coming in. It stands a row further in.
- Seen from eye height, the shelving ran across the store between the front door and the register, so nobody coming in could see the till. It runs from front to back now, and the place case checks the register is in sight from the front door.
- The cooler's lit sign said ICE COLD over every door. It is one panel now, ICE over one door and COLD over the next, plain over the rest.
- The pump's dials read as a row of keys. They are two rows of drums, each showing a nought, pale on the dark window, five pixels high.
- The rig's hood and the sign's top board were drawn as missing: the flicker cure had laid a face a hair outside the edge of what the game will load, where two faces met on that edge. The fender and the trim are moved in, and the flicker gate now fails any model with a piece past that edge.
- The tops of the pads' upper wire strands and of the cellar's tins took clear rows of their pictures and drew nothing. They take the wire's row and the tins' tops.

## Every new line

Every line below is new in this release. None names a colour or an age, and none says how a reader feels. No line has a gendered word for the reader.

**What things say to a hand** (`FallowTexts.thing`), shown over the hotbar:

- **The store.**
  - Counter: "The counter. Its top is worn through to the board where people set things down to pay."
  - Register: "The register. One of its keys sticks halfway down. The drawer is shut."
  - Flap: "The end of the counter, hinged so it lifts. It stands up, open, for whoever works behind it."
  - Candy rack: "A rack of candy bars and gum at the front of the counter, where a hand waiting to pay will find it."
  - Stool: "Harlan's stool behind the counter. The seat is split and mended with tape."
  - The back shelves:
    - Cabinet: "A cabinet under the back shelves. Its doors are shut."
    - Shelves: "Cartons of cigarettes behind a little rail, cans of motor oil, fan belts in their paper sleeves."
    - Radio: "A radio on the shelf, playing low. A ball game, a long way off, and the crowd coming and going under it."
  - Television: "A television on a bracket in the corner, the sound turned down. A weather map, and a man pointing at it." Its card: "The television shows a card with nothing on it, and a tone." Off: "The television is off." (The card and off are for part 3.)
  - The coolers:
    - Cooler: "A cooler with glass doors. Milk, bottles of pop, sandwiches in wax paper. The glass is beaded on the inside."
    - Its sign: "The cooler's lit sign: ICE COLD."
    - The rest of its panel: "The lit panel along the tops of the cooler's doors."
  - The shelving:
    - "Canned goods: beans, peaches, soup, potted meat."
    - "Boxes of crackers, cereal, matches. Three loaves of bread."
    - "Quarts of oil, wiper blades, a funnel, spark plugs on a card."
    - Its top: "The top of the shelving. Somebody has left a pair of work gloves on it."
  - The maps:
    - Rack: "A rack of road maps, one for each state round about. The nearest one has been unfolded and put back wrong."
    - Header: "The rack's header: ROAD MAPS."
  - Coffee: "A coffee machine on a stand, the pot half full. Paper cups, sugar, and a can with a slot cut in its lid."
  - Window: "The store's front window. Through it, the pumps, the road, and the wheat beyond the road, to the edge of sight." Its top: "The top of the front window. A fly walks on the inside of the glass."
  - Light: "A strip light. It buzzes, very faintly."
  - Name board: "The station's name, painted along the edge of the roof in letters a foot high."
- **The forecourt.**
  - Pump: "A gas pump. Its bell rings as the nozzle comes off its hook."
  - Dials: "The pump's dials, gallons and price, on little drums. They read nothing."
  - Diesel pump: "The diesel pump, set apart for the trucks. Its bell rings as the nozzle comes off its hook."
  - Its dials: "The diesel pump's dials. They still show what the last truck took."
  - Island: "The island's curb, painted where the cars pull in close."
  - Its end: "The rounded end of the island, scraped where tires have run against it."
  - The canopy:
    - Canopy: "The canopy over the pumps. Swallows have built under it."
    - Light: "A light let into the canopy, lit, though the sun is out."
    - Edge: "The canopy's edge, with the station's name along it."
    - Column: "A column of the canopy. Pencil marks run up one side, a line and a date beside each."
    - Foot: "The column's foot, set in concrete."
  - The sign:
    - Pole: "The pole of the station's sign."
    - Prices: "The price board: REGULAR, ETHYL, DIESEL, and the prices in slotted numbers."
    - Name: "HARLAN'S, on the board at the top of the pole, and under it, GROCERIES."
- **The road and the field.**
  - Pole: "A telephone pole, its wood split with weather."
  - Crossarm: "The top of a telephone pole: a crossarm, glass insulators, the wires."
  - Wires: "The wires, slack between the poles, running along the road out of sight both ways."
  - Wheat: "Wheat, ripe, to the waist. It runs to the edge of sight every way but the road."
  - Bowed: "Wheat with its heads bowed over, heavy."
  - Stubble: "Stubble, where the wheat has been cut." Burnt: "Stubble, burnt. The ash comes off on the hand." (Both for part 4.)
- **The pads.**
  - Fence: "Chain-link, with three strands of wire on arms along the top. A sign on it: PROPERTY OF THE U.S. GOVERNMENT. NO TRESPASSING."
  - Post: "A fence post, set in concrete. The wire on it is new."
  - Gate: "The gate in the fence. It is chained and padlocked."
  - Light pole: "A light pole inside the fence."
  - Light: "A floodlight, lit, in daylight."
  - The lid:
    - Edge: "A seamed edge of concrete, level with the gravel."
    - Lid: "A wide slab of concrete, flat in the gravel."
  - Box: "A low steel box with a hatch in it, bolted shut."
- **The garage.**
  - Post: "A post of the car lift." Its top: "The top of a post of the lift."
  - Bar: "The lift's bar, under the car."
  - Bench: "The workbench. Tools lie on a rag in the order they were used."
  - Pegboard: "A pegboard over the bench, each tool's outline drawn on it. Two outlines are empty."
  - Tires: "A stack of used tires."
  - Drum: "An oil drum with its lid cut away, half full of rags."
- **The yard.**
  - Trailer window: "A window of the trailer. A curtain is drawn across it."
  - Hitch: "The trailer's hitch, propped on a block. It has not been hitched to anything in a long time."
  - Tank: "A propane tank, chained to the trailer's side."
  - Steps: "Cinder blocks, stacked for a step."
  - Trailer door: "The trailer's door. It is locked."
  - The clothesline:
    - Post: "A clothesline post."
    - Line: "The clothesline. Two work shirts and a dish towel, pinned, stirring."
  - Barrel: "A burn barrel, scorched through in places. Ash in the bottom."
  - Fence: "The yard's wire fence, sagging between its posts."
- **The cellar.**
  - Doors opened: "The cellar doors swing back on their hinges. Steps go down into the dark."
  - Doors shut: "The cellar doors come down and lie shut."
  - Jars: "A shelf of jars: peaches, beans, tomatoes, each dated on the lid."
  - Tins: "A shelf of tins, a box of candles, matches in a jar, a first-aid kit."
  - Cot: "A cot, a blanket folded at its foot."
  - Crock: "A crock of water, a dipper hung on its side."
  - Lantern, lit: "A lantern on its hook, lit." Unlit: "A lantern on its hook, unlit." (Unlit is for part 4.)
- **The farmhouse.**
  - Window: "A window of the farmhouse across the road. There is nobody at it."
  - Porch post: "A post of the farmhouse porch."
  - Rail: "The porch rail. A coffee can of sand stands on it, full of cigarette ends."
  - Mailbox: "A mailbox on a post at the end of the drive. Its flag is down. The name on it has been painted over."
- **The rig.**
  - Hood: "The rig's hood. The engine under it ticks as it cools."
  - Cab: "The rig's cab. A thermos on the dash, and a clipboard of papers."
  - Sleeper: "The sleeper behind the cab. A curtain is drawn across its window."
  - Roof: "The roof of the rig's cab."
  - Wheels: "The trailer's wheels. Dust is caked on the hubs."
  - Legs: "The trailer's landing legs, cranked up."
  - Doors: "The trailer's back doors, sealed with a wire tag."
  - Side: "The side of the trailer, dusty enough to write in. Nobody has."

**The five**, first line and then every time after:

- Harlan: "Pumps are on the honor when I'm under a car. Pay at the register when I come out." / "Been a dry summer. Wheat came in early, though."
- Faye: "It started making a noise past the last town and quit right in front of this place. He says it's the fuel pump." / "We were supposed to be at my sister's by supper."
- Opal: "I'm growing a bean. It's in a cup. It's in the car, because Mama says it'll spill." / "You can see a long way here. There's nothing in the way."
- Boyd: "He said an hour. It's been an hour." / "Don't touch the car."
- Otis: "Hauling seed corn north. I stop here every time I come through. Best coffee on this road, which isn't saying much." / "Those fenced squares out in the wheat, with the lights? Government. Don't go poking round them."

**The first paper**, "The note by the register", by Harlan, by hand:

- "Under the car or out back. Ring the bell on the pump and I'll come."
- "Pay for gas here when I come in. Coffee is ten cents, help yourself. Milk is in the cooler."
- "The cellar out back is for weather. If the weather turns, go down and pull the doors to. Don't wait on me."
- "Phone is for emergencies. Road maps are free if you buy gas."

**The hint** (`LiteraryTexts.hint`, given once in grey after the paper): "There is a note by the register."

**The account** (unused until part 5, when it is rewritten from the reader's own record): "There was a gas station on a two-lane road, a store and a garage under one roof, in wheat that ran to the edge of sight. A car was up on the lift. Across the road stood a farmhouse, and out in the wheat were two fenced squares with lights in them."

**Elsewhere in the House:**

- Account title (`LiteraryTexts`): "the station".
- Staircase name (`StaircaseProse`): "the station in the wheat".
- Miniature (`MiniatureNotes`): "A gas station no bigger than a matchbox beside a strip of painted road, two pumps the size of thimbles under a card roof, and stiff brush bristles for wheat all the way to the board's edge."
- Shared account (`CoopAccounts`): "%s {was} at the station with me."
- Sound subtitles: "Wind goes over the wheat"; "Insects buzz in the wheat"; "A radio plays a ball game"; "The pump's bell rings"; door bleed: "Wind goes over wheat behind a door".
- Block names:
  - Station Fitting, Shingle Stairs, Cellar Steps.
  - Painted Clapboard, Painted Block, Store Linoleum, Beadboard Ceiling, Tar and Gravel.
  - Forecourt Concrete, Highway Asphalt, Center Line, Gravel Shoulder.
  - Corrugated Iron, Garage Floor.
  - Trailer Siding, Trailer Roof, Trailer Skirting, Trodden Yard.
  - Field Soil, Dirt Track, Pad Gravel.
  - Fieldstone, Cellar Floor, Cellar Roof.
  - Farmhouse Siding, Asphalt Shingles, Porch Boards, Chimney Brick, Trailer Box.

Opal's "Mama" is what she calls her mother; it says nothing of the reader. The pads' sign is the government's own wording, as it would be stencilled on a fence.
