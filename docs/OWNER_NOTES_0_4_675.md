# The owner's twelve notes (0.4.675)

The owner's notes of 17 November, with twelve screenshots, in their order:

> Custom sign textures
> Doubled dialogue?
> Broken bleachers in bike night
> The player's need guidance towards the cemetery. Considerations must be made for this both on and off their bikes – ie, whether their bike was stolen by the bully npcs or not.
> Shopping carts are disconnected.
> Change dialogue for morning humans. Maybe render them without costumes too? After the scene has shifted to day. Also, have them check whether the player is actually on a bike or not for the even on a bike line.
> A little longer vignette spawns players outside the house.
> basement in ones already inside is unreachable. Stairs are too high.
> Snow floats above house in the black city
> What is that overlay in the tall grass?
> Keep the npcs in the kept from stacking on top of the other
> The unique dialogue formatting for AM is good, however it doesn't fit into the screen

A later note on the first one:

> there are several signs in last crossing that could benefit from custom textures. i was hoping you'd notice that on your own

New in-game lines are marked **(new)**. Lines that were only moved somewhere else are not marked.

## 1. The Last Crossing's signs

### Before

The flood station's four signs were the game's own wooden signs:

- the name board over the office porch;
- the keeper's plate by the office door;
- the barricade where the road goes under the water;
- the warning over the pump room's ramp.

### Now

Each sign is a painted board, made the way Drisking's and Calder Falls' boards are. The art is drawn by `tools/draw_crossing_signs.py` at sixteen pixels to the block, with the lettering in the picture. A large sign is cut into one square per block: `CrossingSigns`, `CrossingSignBlock` and the `crossing_sign` block.

| Sign | Size | Where | What it shows |
|---|---|---|---|
| Name board | 5 × 1 | On the wall over the porch | The station's green deal, ROOK FERRY in large cream letters with FLOOD STATION under it, a wave at each end, rusting nails, and the line of dried silt where the river stood against the wall |
| Keeper's plate | 1 × 1 | At eye height beside the office door | Enamel, KEEPER, a screw at each end, a chipped corner |
| Barricade | 3 × 1 | Free-standing, on the sawhorse's legs | ROAD FLOODED between slanted red and white bands, with dried mud splashed up from the foot and never over the letters; read from both sides |
| Ramp warning | 3 × 1 | Painted on the pump room's wall | RAMP, KEEP CLEAR, the paint lifting at the edges and a drip from the P |

A hand on a sign reads it:

- **(new)** "ROOK FERRY FLOOD STATION, on boards over the porch. A line of dried silt runs across the lower letters."
- **(new)** "KEEPER, on an enamel plate by the door. A corner of it is chipped."
- **(new)** "ROAD FLOODED, on the board across the sawhorse. Mud is caked up its legs."
- **(new)** "RAMP. KEEP CLEAR. Painted on the pump room's wall, coming away from the stone at the edges."

Placing a sign where something already stands throws an error in the builder, so a sign can never quietly replace a block.

The porch lamp had hung from the porch roof right in front of the name board, over its F and its ST. It now hangs over the porch table and Vera's clipboard, beside the board.

The other vignettes are being checked the same way for the game's own signs and plain stand-ins. Pinehook's PINEHOOK sign is the first one found.

CI runs `tools/draw_crossing_signs.py --check` beside the Borrasca and Calder sign checks.

## 2. Doubled dialogue

Each place that drew a person's line twice now draws it once: the caption over the speaker, with their name, for whoever is near enough to hear. The second copy came from one of two sources:

- the action bar repeated the caption;
- a chat or subtitle line said it again.

Changed in:

- The House on the Corner, the Drawing, the Kept, Growing Things, Last Orders, Last Seen, the Supper and Pinehook;
- The Last Crossing, Costume Night, Winter Lake, the Last Stop's night and Movie Night's cast;
- the Shallows (its cue payload and client), the lake house's Tess, the Holy Rabbit and its dialogue;
- the Blue-Light Diner and the other uncanny rooms;
- the test players' captions.

No line's words changed.

## 3. Bike night's bleachers

The bleachers stood on the playground's striped posts at the front and back, with nothing under the raised rows. They are rebuilt:

- Aluminium planks, ribbed along their length.
- At each frame line under every row, a steel leg runs down to a base plate on the ground, with a strut forward to the row in front.
- The way between the holes in the fence (x -18) stays clear under every row.

`calder_town` gained the bleacher's four row blocks (`bleacher_0` to `bleacher_3`) and its texture.

## 4. The way to the cemetery

Once the three boys go for their bikes, a reader in the town who is not getting nearer the cemetery is told which way Calder Hill lies and what to make for next (`BikeNightWay`).

- **Checking.** Every ten seconds the House looks at each reader's distance from the cemetery gate. A reader who has come six blocks nearer since the last check hears nothing.
- **Directions.** The next place is the first one more than five blocks away along the shortest route through the town's own ways (`CalderRoutes`). The side is taken from where the reader is looking.
- **Riding or walking.**
  - **(new)** "The cemetery on Calder Hill is %s. Ride for %s." (on a bike)
  - **(new)** "The cemetery on Calder Hill is %s. Run for %s." (on foot: they left the bike, or the boys took it)
  - **(new)** the side: "ahead", "to your left", "to your right", "behind you".
- **Pillion.** A reader carried on someone's pegs is not told; their rider is.
- **Nobody needs it.** Nobody already at the gate or inside the cemetery is told, and nobody whose night has turned to dawn.

## 5. The shopping carts

The basket stood on nothing over a flat plate, and the handle floated above it. The cart is redrawn as one piece:

- two side tubes run from the castors up the back to the handle;
- the basket rests on posts at its four corners;
- the lower rack is a wire tray between the frame's rails.

## 6. The morning people

When a reader's night has turned to dawn (`BikeNightGraveyard.dawn`), the ten people in costume are drawn in their plain clothes: the trick-or-treaters and the line at the theater. Ten new `literary_calder_*_day` skins, from `tools/paint_character_skins.py`. The client switches skins once the reader is more than halfway to dawn (`BikeNightMistClient.morning`).

They say the morning's lines:

- **(new)** "My mom says we have to come in now." / "I ate all the peanut ones in bed."
- **(new)** "We stayed out all night. Don't tell." / "My mask's still on the porch somewhere."
- **(new)** "They never opened the doors." / "Were you out all night too?"
- **(new)** "Somebody should drive us home." / "Is it Saturday already?"
- **(new)** "Morning. We open at eight." / "Forty-one carts. Every one of them back."

The line's word against cutting:

- to a reader on foot: **(new)** "No cutting."
- to a reader actually on a bike: "No cutting. Not even on a bike."

## 7. A Little Longer, and two more doorways

### The fault

The House's door into a literary scene stands at z 1, so a reader arrives just inside it, at z 0. The builder cuts that cell open unless the scene asks it not to. A Little Longer asked, so its corridor's end wall stayed solid where the reader arrived. The game pushed them out of the wall, which put them outside the house.

### Two more found

A new architecture check walks a body through the first three cells inside every scene's door. It found the same fault in two more scenes:

- **Last Seen:** the office's south wall.
- **Fallow:** the store's back wall, where the shelves already leave a gap for the House's door.

### The fix

- All three scenes now have their doorway cut.
- A Little Longer's runner carries on under it.

### The check

`ArchitectureTests`: for every scene, the cells at x 0, z 0 to -2 must hold a door or have room for a body 0.6 by 1.8 blocks. On the build without the fix it named exactly these three scenes.

## 8. The Ones Already Inside: the cellar stair

### The fault

The back stair's four treads were there, but a body walking down them upright hit its head:

- first on the well's north wall, at the third tread;
- then on the scullery floor, at the fourth.

Only crouching got past. The existing per-tread headroom check couldn't see it, because it looked at each tread on its own.

### The fix

- The well is a block deeper: its north wall moves from z -20 to z -21.
- The floor over the stair is open across three treads instead of two.
- The scullery behind the well loses one row at its south end. Nothing stood there.

### The check

`LodgingHouseTests` walks a 0.6 × 1.8 body down the stair from the well's floor to the cellar floor and back up, stepping as a player does. On the build without the fix it stopped at z -18.75 with its feet on the second tread.

## 9. Snow over the hut in the black city

The hut's roof was a bottom slab, which left half a block of air under the snow on it. The roof is now the slab's double, so the snow lies on the boards.

## 10. The overlay in the tall grass

The far-off telephone poles and their wire were drawn past the client's own render distance. There was no ground drawn there, so they hung in the sky. They now stand inside the render distance, always over drawn ground and in the same fog.

## 11. The Kept's people standing on each other

All four walk their routes as before. When any two come closer than a body's width, they ease apart, and the later one in the order gives way.

## 12. AM's line

The machine's line was drawn as a vanilla title, which doesn't wrap, so a long line ran off both edges of the screen. It now has its own overlay (`KeptVoicePayload`, `KeptVoiceClient`):

- large and centered, as before;
- wrapped to the screen's width;
- drawn smaller where even that will not fit;
- fades in over six ticks and out over twenty, as the title did.
