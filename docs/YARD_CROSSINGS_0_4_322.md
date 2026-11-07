# The yard's exceptional crossings (0.4.322)

Step 4 of the owner's "The House Has a Yard" (4 November): "Author the exceptional crossings. Test the perimeter loop, deep ceiling, and false porch bearing as discrete events. On a return to the loop, check that the wagon and familiar marker agree on where the player has come out. Preserve a visible way onward."

Three gray outdoor connectors, `YardCrossings`: slots 197–199, layout 100, protocol 134 (the new mud fitting's block states). They are passages, not Witness stories: no source, no credit, no harm, nothing to solve. Each is dealt to a reader once, and only after the ordinary yard has taught them what it breaks. Never from the shared hall cache. Nothing standing is rebuilt; new builds only.

## The long drive (the perimeter loop)

`YARD_LONG_DRIVE`, slot 197. The turn-around exactly as the reader learned it, built by its own builder: porch and bulb, the drive, the apron, the dark garage, the wagon idling nose to the trees, the squeeze between its flank and the garage door. Two things are missing: the swollen creek at the end of the road east, and the gate in the back fence.

Either way out (the squeeze and the path through the tree line, or the road east) bends long and shallow through the cedars and comes back in by the other. The whole circuit is about two hundred blocks. Along it: branches knit across the road where it seems to leave the property, the property's old boundary fence running off into the trees each time the road crosses its line, a turnout with a sawn stump, a dead tree with the crow, a streetlight at the end of the long straight, a fallen run of fence on the inside of the far bend, and a set of tire tracks that stop in clear mud. Past the mud the track is grass.

The familiar marker is the wagon itself, its driver's door ajar, and the reader's own prints. Soft mud lies in the squeeze beside the wagon's door, across the mouth of the road and where the tire tracks stop. A reader walking on it presses footprints into it, pointed the way they were going (`YardBlock` kind `PRINT`, stage 0 clear, 1 printed). Coming back in by the other way, the reader is at the same wagon, with prints beside it.

While a reader is round the far side (more than 55 blocks from the middle of the apron), the garage door goes up, and only while no camera, spectators' included, has it within sixty degrees of its gaze. Back on the apron, the reader gets one line: "The wagon again. Its door still ajar." The top of the opening shows over the wagon's roof from the apron; from the squeeze it is right beside you. The way on is the door in the garage's back wall, under a bulb.

Empty and unwatched for ten seconds, the garage door comes down again and the mud smooths over, for the next reader's discovery. The open door and the trodden mud are shared while anyone is there.

Dealt once to a reader who has crossed the turn-around (or the reunion apron), from the story depth, weight 2.

## The porch turn (the false porch bearing)

`YARD_PORCH_TURN`, slot 198. A back porch with its bulb, and a footpath walking away from it between hedges. Looking back down the first straight, the bulb is behind you. The path bends into a long half-turn, radius twelve, under knitted branches with no sky to steer by, and comes out onto a straight with a porch and its bulb ahead. The same things are on the same hand on both straights: a trough on the left, a stack of split logs on the right. The hedge between the straights is whole, so neither porch can be seen from the other's straight.

As the reader comes out of the turn onto the second straight, somebody shuts the back door ahead (the native wooden door sound, from the door). That door, the far porch's back door, is the way on.

Dealt once to a reader who has crossed two of the ordinary yard's crossings, from the story depth, weight 2.

## The joists (the deep ceiling)

`YARD_JOISTS`, slot 199. A muddy ditch between the cedars and a bank of roots: water over the boots in two stretches, soft mud in two. The bank rises two high from the verge, roots above, then a retaining face of dark timbers, and on top of it the state route: asphalt, a dashed centre line, a guard rail, streetlights on the far shoulder. Close enough to hear, out of reach.

Over all of it, where the sky should be, is a subfloor on joists every third block, with copper runs along and across them. A passing car's headlights run north along the route and are thrown up under the joists, catching them high over the route, then leave nothing behind. The traffic is loudest here, close and above; a pipe ticks overhead as the car goes by; sleet whispers against the bank. The stand of cedars ends at a face of the same dark joists, floor to ceiling, with a door in it under a bulb: the way on.

Dealt once, in the deep halls (depth 12 until the dark halls are all there is), to a reader who has crossed any of the ordinary yard.

## Review fixes carried here

- The reunion apron's far gate faced north while the identical turn-around gate faced south. Arrival rotation maps the door's facing to the next entry's, so a reader walking north through it arrived in the next hall facing back the way they came. It now faces south.
- A car's light on the turn-around's crowns is a row of moving light blocks; a server stopped mid-sweep left them in the world. Each new car now clears the row before it starts. The joists' sweep does the same.

## Validation

Four new exploration cases: a reader walks the whole long loop on their own feet, prints the mud, comes back to the wagon to find the garage up, walks through it to its back door, and then sees the door come down and the mud clear after the reader has gone; a reader walks from one porch round the turn to the other, the bulbs and hedge checked by sight rays, the turn's roof by the heightmap, the door heard shutting; a reader walks the ditch to the joist face, the route checked out of reach, and a car's lights checked catching the joists and leaving nothing; and the dealing (once each, only after the ordinary yard, never from the shared cache) with the reunion gate's facing. The turn-around, reunion, culvert and hall-dealing cases still pass.

Local validation: all 125 exploration cases pass, the four new ones included. One earlier full run, made while the asset checks were running alongside it, failed the two familiar-kitchen cases; they passed alone, after the long drive, and in the full rerun, and the base commit passes the same suite. All 79 tool checks from the quick workflow pass, as does compilation. Exact-commit CI pending.
