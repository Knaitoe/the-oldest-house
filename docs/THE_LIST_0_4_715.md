# The list, part 4c-2: the car door (0.4.715)

The second of the drive-in's set pieces (`docs/THE_LIST_PLAN.md`), after the fryer (`docs/THE_LIST_0_4_714.md`). Like the others it is
begun by the slasher's hand on the thing it is done with, and by hand until a night uses it.

## The car

A car stands in one of the Caprock's stalls, nose in under the canopy between the second and third posts from the north: the blue
sedan, its lights off, nobody in it. It is there while anyone is in town. Its driver's door is on the north side, toward the next stall.

A hand on the driver's door opens it, for anyone who is not a spectator, with or without a hunt: the latch lets go and the door swings
out wide on its hinges, and the car's inside shows through it: the floor, the bench seat and its back, the wheel, the dash and the far
side's lining. Another hand slams it shut. A hand anywhere else on the car is a look at it.

1. **The door, held.** The slasher with hold of one of the five can open the door with the other hand; it does not let them go.
2. **The head in the door.** The door open, the slasher with hold of one of the five within reach of it puts a hand on it. They are
   forced down on their knees beside the car, facing it, and their head is pushed in at the open door, at its back edge. Their own
   words, a scream; what they had in hand they let fall. Anyone on the list near enough runs, as in 4a.
3. **Slammed.** Each hand (or blow) of the slasher's on the door, or on them, slams it: the door is pulled back wide and swung shut, and
   a moment later it lands on the head and stops short of shut with a crack of steel, bounces, and comes to rest against them. A hand
   straight after does nothing; the next slam waits until the door has come to rest and a little over half a second has passed.
   - The first: they jerk, and blood drops onto the lot under the door.
   - The second: something gives against the frame; blood sprays down the car's side and onto the slasher, and the back of the head is
     marked.
   - The third: they go still, slumped on the knees against the car with the door shut on the head. The slasher is told; anyone in
     town within forty blocks who is not the slasher is told from which way a scream stopped. The kill is kept in the slasher's record,
     even if they have logged off since. A pool spreads under the head.
4. **Fighting it.** Before the second slam, the one in the door fights to pull the head out as they fight a grip (4a-2): longer while
   the slasher looks at them, twice as long after the first slam. Free, they pull the head out (the door pushed wide as they go) and
   run. After the second slam they cannot. The slasher going from the town lets them up the same way.
5. **After.** The door rests against them. A hand on it will not shut it; a hand on them is told they do not move.

## Found

As in 4a-3: the rest of the five who see them say so (Shannon's own line says where), and calm ones run; a reader who is not the
slasher, near and looking, is told once a hunt that they are kneeling at a car with the door shut on the head.

## Single and multiplayer

- **Alone**, the reader catches one of the five, brings them to the car, opens its door and slams it on them.
- **With others**, the door is anyone's until one of the five is in it: another reader may open or shut it. With one of the five in
  it, another reader's hand on the door or on them finds the head caught; nobody else can pull them out (what Amber's side can do about
  that is part 8's). Only the slasher holding them slams it, and the kill is theirs. The door's swing is the same for everyone who can
  see the car.
- **Spectators** touch nothing, open nothing and are told nothing.

## Retreat

As with one held (4a-2): while the slasher holds one of the five at the door, the House's door is shut to them and they cannot walk away
from the car. Once the one in the door has gone still, the hold is let go: the slasher walks, and the door is open to them again.

## Ended

The hunt ended (or begun again), everyone goes back to their place, unmarked, and the car's door is shut.

## Made as

- **`TheListCar`** (new): the car in its stall (`AT`, nose east, `YAW` -90), told where it stands every town tick and never saved; its
  door's angle out from shut (`OPEN` 70, `HIT` 34, where it stops on a head, a head between its back edge and the pillar holding it a third open), swung through legs by the town's tick and sent to everyone
  with the car; the hinge and back edge in the car's own space; `REACH` 2.6 and `HAND` 1.4; where one forced down at the door kneels
  (`kneel`, facing south into the car, just inside the opening's back edge) and where the head is (`head`), and where blood off it lands
  (`under`, on the lot under the door). `hand` (a hand or a blow on the car: near the driver's door the
  hunt's first, otherwise opened or slammed; elsewhere a look), `slam` (pulled back wide, slammed down to `HIT`, landing, bouncing and
  coming to rest), `reset`.
- **`TownCar`**: a synced door that opens (`working`) and its angle (`door`), drawn between ticks on the client (`doorAt`); only a car
  with a working door is pickable, and a hand or a blow on it goes to `TheListCar.hand` (nothing is hurt).
- **`TheListHunt`**: the state `CAR`; gestures `DOORED` (16) and `SLAMMED` (17); `CRUSH` (16, the marks bit); `SLAM_GAP` 12;
  `carDoor(player, base)` from a hand on the door, `doored`, `atDoor`, `slam`, `slammed` (where the door lands), `dooring` (each tick),
  `slams` (for tests); a blow on one in the door slams it, a hand on them from anyone else finds the head caught. Torn loose from the
  door, it is pushed wide. One dead at the door is found by the back, a little up from the ground.
- **Client**: the two poses (`LiteraryRenderers.CastModel.hunted`): on the knees bent toward the car, the head in at the door and
  turning, the hands flat on the car's side either side of it pushing back; still, slumped on the knees against the car, one arm hanging
  down its side and the other fallen in over the sill, the head down and turned. The crush (`TheListMarks`, `crush.png`). The car
  (`TownCarRenderer`): with its door open the blue sedan is drawn with the door cut out of it, and the door turned about its hinge.
- **Sounds**: `the_list.car_door` (a heavy door slammed: the latch, the thud of steel, the body ringing, the glass rattling, the car
  rocking on its springs) and `the_list.car_open` (the latch let go, the hinges' creak), subtitled.
- **Art**: `tools/draw_calder_cars.py`: the blue sedan again with its driver's door cut out of its left side and its inside showing
  through (the floor, the bench seat and its back, the wheel on its column, the dash, the far side's lining, the inside of the
  windscreen); the door as its own model (its panel in the car's paint with the belt's strip and the sill's band, its window in its
  frame, the handle, the lining and armrest inside); two new pictures, the door's lining and the seat's vinyl.
  `tools/draw_the_list_art.py`: `crush()` over the five's skins: a dented band across the back of the head, split and dark round it,
  the hair over it matted, runs down to the nape and onto the collar behind, the back edges of the head's sides caught, and blood from the
  nose and mouth.

## Also

- **The line's spray** (4b-2) lands on the first fresh open cell along its head's arc, out from where it was thrown, so each spray
  leaves new blood. The line case could miss it when the one cell it tried was already red or not open ground.

## Tests

`TheListTests.theCarDoor` (new, batch `the_list_car`): the sedan in its stall, lights off, its door shut. Another reader opens its door,
told so and hearing the latch; it stands open, and they slam it shut, heard; on the car's far side a hand on it is a look. Deek taken
from behind on the home bench and brought to the car, held by its door. Shut, a hand on it opens it and he is still held; open, a hand
on it forces him down at it: the slasher's cue, his words, on the knees by the door facing in, still held with no retreat and no walk,
the other reader hearing him. A hand from the other reader on him, and on the door: the head is caught, the door left where it is. The
slasher's hand: the door on its way down, landing a moment later, the first slam's cue, stopped short of shut, heard by the other
reader, blood under the door; a hand straight after does nothing, the door at rest against him. The second, after a wait: something
gives, the back of the head marked, blood on the slasher, still there. The third: still, the slasher told, the other reader told a
scream stopped, the kill kept by the slasher, the hold let go, the door resting against him where he knelt; a hand on the door will not
shut it; a hand on him after and he does not move. The other reader, looking, finds him at the car. Ended: the door shut, Deek back and
unmarked.

## Seen in the frames

Eye-level frames of the stall with the door shut, open, Deek in it, after the first slam and dead: from the next stall north, close
over the driver's side, from behind the car's tail, from in front of its nose and from across the lot.

- **The car** reads in its stall under the canopy, the blue sedan with its lights off. Open, the door swings out from its front hinge,
  its lining toward the lot, and through the opening the bench seat, the wheel and the far side's lining show.
- **In the door**, Deek is down on his knees beside the car at the back of the opening, bent in toward it, the head in at the door
  against the pillar, the hands on the car's side.
- **Found and fixed**: in the first frames he knelt further along the opening and the door came down to sixteen degrees, nearly shut,
  through the middle of his head. He now kneels at the opening's back edge, the head between the door and the pillar, and the door
  stops on it a third open (thirty-four degrees). From outside, the door's frame then lies across the head and the back of the head
  shows past its edge. Each slam's blood also landed a cell further out than the last (the search for open ground stepped past cells
  already bloodied), so the pool lay a block and a half from the car; it now lands under the door every time and thickens there.
- Particles (the blood off the head) are not drawn by the review tool; the case checks the stain under the door.

## Every new line

Speech, over the speaker's head as their name, forced down at the door:

- Deek: "Not the door. Come on, not the door."
- Wes: "Get my head out! Get my head out of there!"
- Mel: "Please. You'll break my neck."
- Lesley: "Whose car is this? Somebody's coming back for it!"
- Shannon: "Jenna! Jenna, I'm at the cars!"

Shannon, coming across one of the others at the car (the others keep their lines from 4a-3): "That's %s. That's %s at the car."

Grey cues on the action bar, to the slasher:

- "You force %s down on the knees at the car and push the head in at the open door."
- "You slam the door. It stops on the head and swings back."
- "Again. Something gives against the frame."
- "Again. %s goes still."

To anyone else whose hand is on one in the door, or on the door: "%s's head is caught in the car door."

To anyone whose hand is on the door with one dead in it: "The door won't shut. %s is in the way."

To whoever opens or shuts the door: "The door swings open. Nobody's in the car." and "The door slams shut."

To a hand anywhere else on the car: "A car in its stall, its lights off. Nobody's in it."

To a reader who is not the slasher, coming across them: "%s kneels at a car in the drive-in, the door shut on the head."

Subtitles: "A car door slams", "A car door opens".
