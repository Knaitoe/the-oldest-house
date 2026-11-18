# The list 1c: the pasture (0.4.701)

The third part of *The list* (`docs/THE_LIST_PLAN.md`), after 1b (`docs/THE_LIST_0_4_700.md`). It adds the pasture south of town, across the cotton from the reader's back yard: a county road, barbed wire, a stock tank and its windmill, and three pumpjacks on an oil lease. The town's people come in 1d. The place is still not dealt and is not yet a Witness source.

## The place

The scene now runs south to z 140 (was 24). The back yard keeps its own fence at z 22; everything else in 1a and 1b stands as it was.

- **The county road.** Caliche, west off Avenue K with a stop sign at the highway, to the pasture's east fence. It crosses the fence on a cattle guard and goes on inside as the lease road.
- **The pasture** (x -76 to 6, z 34 to 124) is buffalo grass, grazed short and worn to dirt in places, with cow trails out from the tank to the hay ring, the far corner and the south fence. Mesquite, prickly pear, broomweed and tufts stand on the grass; tumbleweeds are caught along the south and west fences.
- **The fence** is barbed wire on cedar posts, a post every five, four strands between. Each corner has a braced post: a rail out to the next post and a wire from its foot back up to the corner. The wire gap in the north fence lies shut between two posts.
- **The cattle guard** is pipe laid over a pit, three blocks across and the road's width long.
- **The stock tank** is galvanized, seven across, with water inside its walls and trodden mud round it.
- **The windmill** stands west of the tank. Its four legs lean in from concrete footings to a platform eleven blocks up, and its pump is at the foot. The head is on top: the wheel turns in front, and the tail stands behind on its boom. The pump's pipe runs along the ground and comes over the tank's rim.
- **The hay ring and the mineral tub.** A hay ring holds what is left of a bale, on ground worn to dirt round it. A mineral block sits in a tub nearby.
- **The pumpjacks.** There are three, each on a caliche pad at the end of a spur off the lease road. Each one has:
  - the wellhead, with the well's numbered plate beside it;
  - the Samson post and the walking beam on it, the horsehead out over the well and the bridle hanging from it to the rod;
  - the cranks and their counterweights, the pitman arms, and the gearbox and motor on their skid behind.
- **The tank battery** is inside the gate: two tanks with ladders and roof hatches and a separator, inside an earth berm with a way in from the lease road.
- **The power line** comes in from the highway's poles along the north fence.
- **The lease's sign** stands at the road, outside the guard.

## Single and multiplayer

Shared and recurring, as in 1a and 1b.

- A hand on anything answers that reader alone.
- The wire gap opens to a hand and stays open for everyone until someone shuts it. The cue is that reader's alone; the chain's clink is heard by anyone near.
- A reader standing cannot pass the wire. A reader crouching ducks between the strands. Nobody passes a post.
- The cattle guard rattles under whoever walks over it, heard by anyone near.
- The machines keep time with the level's own clock. The server, which plays their sounds, and every client, which draws them, work out the same angle from the same tick, so every reader sees the same stroke at the same moment and the knock comes as the rod reaches the bottom.
  - The windmill creaks each time its wheel comes round, within forty blocks of someone outside.
  - Each pumpjack knocks once a stroke, eight strokes a minute, within thirty-six blocks of someone outside.

## Made as

- **Bounds.** `TheListSite.SOUTH` is 140. `YARD_S` (22) keeps the back yard where it was. `LabyrinthPlace.THE_LIST`'s bounds and the client haze follow it, and so do the place's lookup and presence bounds (see below).
- **`TheListSite`.**
  - **Constants:** the county road (`ROAD_N/S`), the pasture (`PAST_W/E/N/S`), the gap (`GAP_X`), the lease road (`LEASE_N/S`), `TANK_AT`, `WINDMILL_AT` and the head's height `WIND_TOP`, the pumpjacks (`JACKS`, `JACK_FACING`) and `BATTERY_TANKS`.
  - **`pasture`** builds it all:
    - `fence` sets one cell of a run; `trail` wears a cow trail;
    - `stockTank` sets the tank's water in an octagon of whole cells (`inTank`), with its walls where they meet the water;
    - `windmill`, `hayRing`, `pumpjack`, `battery` and `powerLine` build the rest.
  - **Helpers:** `inPasture`, `jackMachine` and `windmillHead`.
- **`TheListBlock`.** Five new kinds:
  - PASTURE (posts, wire, the gap shut and open, the guard, the corner post, the mineral tub, the hay ring);
  - STOCK (the tank's wall, corner and seam, the spout, the pipe);
  - WINDMILL (four tower sections, the footing, the tail);
  - PUMPJACK (the wellhead, the Samson post, the gearbox and motor);
  - BATTERY (the tanks, the separator, the lease's sign, the well plates).

  Collision:
  - The wire and the shut gap stand a block and a half high, like the posts. They hold anyone but a crouching player; the open gap holds nobody.
  - Of the windmill, only the pump and the footings are solid. Of the pumpjack, the wellhead, the post and the gearbox are.
- **`TheListMachineBlock`** (new) is the windmill's head and each pumpjack's moving parts, a block with a block entity, drawn by `TheListMachineRenderer` (new):
  - The windmill's wheel turns in front of its head.
  - The pumpjack's beam rocks on its bearing. The cranks turn on the gearbox's shaft, the pitman arms follow the crank pins to the equalizer, and the rod and its carrier rise and fall under the horsehead on the bridle.
- **`TheListMachines`** (new) works out every angle from the level's time:
  - The crank turns once a stroke; each well has its own place in the stroke.
  - The beam's angle is the one that keeps the pitman arms their length, solved a few steps at a time.
  - The rod hangs from the horsehead's arc.
  - The windmill's wheel turns about once in two and a half seconds, faster and slower as the wind goes.
  - The knock and the creak fall on the ticks where the stroke bottoms and the wheel comes round.
- **`TheListRegistry`.** Two materials (buffalo grass, trodden mud), the machine block and its block entity, three sounds.
- **`TheList`.**
  - The gap opens and shuts by hand, all three cells at once.
  - The pasture's sounds: the creak, the knocks and the guard.
  - A hand on either machine gets its own line.
  - The dog behind the back fence keeps to the neighborhood.
- **Art.** `tools/draw_the_list_art.py` now draws 313 textures and 399 models and states. The new ones:
  - the buffalo grass, the mud, cedar, barbed wire and its barbs, the guard's pipe and pit;
  - the tank's galvanized wall and its water stain, salt and old hay;
  - the tower's iron, the sails, the head's case, the vane and the pier;
  - the pumpjack's paint, worn and whole, the weight's cast number, the cable, the polished rod, the wellhead, the motor, the belt guard's mesh and the skid;
  - the battery's steel and roofs, the lease's sign and the three well plates.

  Every model keeps within its bounds and turns only by the angles the game allows. The tower's legs lean by steps, and its braces cross each panel at the steepness nearest its own.
- **Sound.** `tools/generate_the_list_audio.py` adds the windmill's creak, the pumpjack's knock and the cattle guard's rattle, each subtitled.
- **Tests.** `TheListTests` gains a fifth case, `thePastureItsWindmillAndPumpjacks`:
  - **What stands:** the fence, the corner posts and the shut gap; the cattle guard across the road; the tank's water, walls and pipe; the windmill's sections, footings, head (its block entity) and tail; each pumpjack's post, wellhead, gearbox, plate and machine (facing its own way, with its block entity); the battery and the sign.
  - **The machines' math:** at every tick of a stroke the pitman arm keeps its length, the beam rocks and the rod rises and falls. The wheel turns, and each pumpjack knocks once a stroke.
  - **Walking:** a body walks in from the county road over the guard to the lease road, the tank, the hay ring, a pumpjack's pad and the far corner. From inside there is no way out through the wire until the gap is laid open by hand (said to that reader alone), and then out through it; the gap shuts again.
  - **The wire:** it holds a reader standing; a reader crouching passes.
  - **Heard:** the windmill at the tank, a pumpjack by its pad, and the guard under a reader walking over it.

## Found and fixed in the making

Found by the new test before this commit:

- **The west fence's hole.** The west fence had a hole five cells wide across from where the county road comes in on the east. The loop that left the guard's rows open left both sides open. Only the east side is left open now.
- **The windmill's pipe.** The pipe's first cell was missing. The windmill clears the ground round its tower after the tank is built, and that took the pipe with it. The windmill lays the pipe itself now, after its clearing.
- **Hands and presence in the pasture.** A hand on the wire gap went unanswered. The House finds which place a reader is in by each place's bounds, and those ended a few blocks south of a place's door, because every other scene runs north of its door. The list's town runs south as well, through the back yard (since 1a) and now to the pasture. Both the lookup (`LabyrinthPlaces.placeBounds`) and the presence test (`IndianLakeRooms.bounds`) now carry the list to the south edge of its ground; no other place changes.

Seen in contact sheets before this commit:

- The well plates are numbered with the stencil letters, which have no small letters, so each carries its digit alone.
- The lease's sign and the battery's rust were redrawn after the first sheet.

Seen in eye-level frames before this commit:

- The Samson post's back braces stopped short of the legs, their tops in the air. Their feet now stand on the skid eleven pixels behind the legs, so that at the steepest lean the game allows each brace meets its leg.

## Every new line

Everything here is written for this part. Nothing of the book is used.

**A hand on things**
- The gap: "You lift the loop off the post and lay the gap back on the grass." / "You stretch the gap across and drop the loop back over the post."
- The fence and the pasture: "A cedar post, the wire stapled to it. The staples have rusted into the wood." / "Barbed wire, four strands. A tuft of hair is caught on a barb." / "A cattle guard. Pipes over a pit, far enough apart for a hoof to go through." / "The corner post, braced against the pull of the wire." / "A mineral block in a tub, licked into a bowl." / "A hay ring. What is left of the bale inside is trodden flat."
- The tank: "The windmill's pipe comes over the rim. A little runs from it with each stroke." / "The pipe from the windmill, along the ground to the tank." / "The stock tank, full nearly to the rim. Moths float on the water."
- The windmill: "The windmill's pump. The rod rises and drops in it." / "A footing, the leg bolted to it. The nuts are rusted round." / "The tail, holding the wheel into the wind." / "The pump rod, up the middle of the tower to the head." / "The windmill's head, up on its tower. The wheel goes round in front of it."
- The pumpjacks: "The wellhead. It smells of crude, and the polished rod slides in and out of it." / "The Samson post. It shakes with every stroke." / "The gearbox, warm, oil weeping from its seal. Behind it the motor hums." / "The beam's bearing on top of the post. The horsehead rises and falls over the well."
- The battery: "The separator. A gauge on it, the needle steady." / "The lease's sign. Somebody has shot a hole through one corner." / "A plate with the well's number on it." / "A tank on the lease, a hatch on its roof and a ladder up its side."

**Painted on things** (in the textures)
- The lease's sign: "MASSEY", "DAWSON CO".
- The well plates: "1", "2", "3".
- The counterweights: "3".

**Elsewhere**
- Subtitles: "A windmill turns and creaks" / "A pumpjack knocks on its downstroke" / "A cattle guard rattles underfoot".
- Block names: Buffalo Grass, Trodden Mud, Lamesa Machine.
