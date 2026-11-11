# Bike night 1: the bikes (0.4.463)

The first slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). The owner's note:

> Players can select from several bikes. These bikes have slightly different stats - or really, each has a unique perk. better
> maneuverability, better acceleration, higher top speed, better jumping, headlights, a card in the wheel that allows a temporary
> 'turbo' boost.

and, of the Hollow: "That could utilize a bike-jump feature I was hoping to include".

This slice is the six bikes, the hop, ramps and landings, riding double, and the rack. **It is not placed anywhere yet**: the park
they stand in comes with the playground, the next slice. Until then they can only be reached by the cases and the client proof.

## What I took as decided

The proposal left six questions open. The owner has since answered two: Bike night comes before the Pell house, and failures are
permanent ("Yes, we need to aim for permanent failures"). For the rest I have taken these, and the owner may change any of them:

- **It is Halloween night**, as the proposal assumed.
- **The ride is the one Borrasca's bikes have**: the strafe keys are the pedals, and pedalling is balancing.
- **The Hollow is one shared course of real blocks**, with the specters private to the rider each chases.
- **The keepsake is the Rattler's spoke card**, the smaller of the two suggested. The bike itself, rideable outside the vignette, is
  the larger one, and is left for the owner to ask for.

## One ride for every bike

Borrasca's bike (`docs/BORRASCA_BIKES_0_4_435.md`) was one class. Its ride is now `labyrinth.RiddenBike`, which Borrasca's two bikes
and Bike night's six all are. `BorrascaBike` keeps what makes a bike one of Drisking's: whose it is, the block it stands as, and what
the town's children say. Nothing in how Patrick's and Kendra's bikes pedal, balance, brake, turn or fall has changed, with three
exceptions, all of which apply to every bike:

- **The hop**, below. Borrasca's bikes hop too, and the grey line of keys shown the first time now includes it.
- **A push never slows a bike.** Pedalling a bike already going faster than it can be pedalled (downhill) used to cut it back to its
  top speed. It no longer does.
- **In the air nothing tips it.** A bike off the ground keeps the lean it left with; it used to go on tipping as if on the road.

## The hop

- **Holding jump** crouches the rider over the bars. It takes twelve ticks to be right down.
- **Letting go** pops the bike up. A tap is a hop of about half a block. A full hold is a block and a quarter.
- **In the air** nothing drives the bike, and it turns about a third as fast. A push on a pedal turns no crank: it shifts the rider's
  weight that way, a tenth of the way over for each push, so a bike that left the ground leaning can be levelled before it lands.
- **Coming down that just fails to clear a lip**, the front wheel catches it and rolls up, if the lip is no more than six tenths of a
  block above the wheels.

## Ramps

Any ground a bike was climbing when the ground ran out throws it up by its own speed: a plank ramp, or the top of a flight of steps.
The steeper the climb and the faster the bike, the higher. A single kerb is not a ramp (it must have been climbing for two ticks
running), and a ramp that ends in a deck longer than a block throws nothing.

The plank ramp is a new block, `bike_ramp`: plywood on a timber frame.

- **The long ramp** is two blocks, LOW then HIGH, at 22.5 degrees, to a lip a little over thirteen pixels up. At a bike's ordinary
  top speed it throws it a quarter of a block above the lip.
- **The kicker**, STEEP, is one block at 45 degrees. At the same speed it throws the bike about two blocks up.
- Those two angles are the only ones a model's piece can lie at, which is why the long ramp does not rise a whole block.
- **A body** on a ramp stands on eight shallow steps under the sheet, none more than two pixels, and walks up without noticing.
- **A bike** rides the sheet itself, at exactly its slope, where the middle of its wheels is. The game climbs one step in a move and
  a bike crosses several in a tick, so on the steps it stalled and threw its rider. For a bike the steps are not there.
- A bike that meets the side of a ramp, or its open back, well under the sheet is stopped by it as by a wall.

## Landing

- **Level** (the lean within an eighth of the way over) keeps the way on.
- **Crooked** tips the bike much further the way it leaned, more the harder it lands, and takes some of its way off. Off a full
  hop, a bike a third of the way over lands half way over, and one half way over lands four fifths. The rider must push out of it at
  once.
- **Far too hard** (a drop of about seven blocks) puts the rider over the bars, as running into a wall does.
- A small hop, a kerb or a step down is none of these: nothing happens.
- Coming off still costs half a heart, never below two hearts.

## The six bikes

Each rides as any bike does and does one thing the others do not. The numbers are beside an ordinary bike's.

| Bike | What it is | What it does |
| --- | --- | --- |
| **The Wasp** | a low BMX on small wheels with fat tyres, a race plate on its bars | **Handling.** It turns ten degrees a tick (six). A turn leans it less than a third as far. A glance along a wall leaves it at least four fifths of its way (three tenths), and only a wall met nearly square puts its rider off. |
| **The Hare** | a light racer with a thumb-shifter | **Acceleration.** Each push drives it on nearly twice as hard, so three pushes take it to its top speed. A climb costs it a third of what it costs the others. |
| **The Comet** | a ten-speed with drop bars and thin tyres | **Top speed.** Nearly half as fast again flat out. It turns four degrees a tick, and the brake takes half as much off it. |
| **The Hopper** | a BMX with stunt pegs on both axles | **Jumping.** A full hop is over two blocks. A crooked landing tips it a fifth as much. It can come down from about thirteen blocks before its rider comes off. Its pegs carry a second rider. |
| **The Lamp** | a paper-route bike with a basket of papers and a dynamo lamp | **Its light.** A real light thrown two blocks ahead at the height of the bars: as bright as a torch and more at a good speed, dimmer when slower, and dark when the bike stops, as a dynamo is. |
| **The Rattler** | a long-seated bike with a playing card pegged into its spokes | **The burst.** Holding the sprint key, the card takes the bike to half as fast again as its top speed for two seconds. It comes back over fifteen seconds of steady riding, and not while the key is held. |

**Riding double.** A reader on foot who puts a hand on a Hopper with one rider gets on its pegs: standing over the back axle, behind
the one pedalling. They step off with the sneak key and the bike goes on. With two aboard each push and each hop is fifteen parts in
a hundred weaker. If the bike goes over, both come off. The one on the pegs has both hands free.

**The Lamp's light** is a block of light in the air (`bike_beam`), set ahead of the bike as it goes and taken away behind it. It is
never put where anything stands. It looks after itself: once a second it checks that a Lamp is still lighting it, and goes out if
none is, so a server stopped with somebody riding leaves no light hanging in the air.

## A bike nobody is on

A night bike that nobody rides is a block, `night_bike`, on its kickstand or lying on its side where it went over. A hand on it:

- **The first time**, for each bike, looks at it: its name and the one thing it does.
- **Lying down**, stands it up.
- **Standing**, gets on. The keys are shown once in grey, and the Rattler's own key the first time it is the Rattler.

The six looks:

> The Wasp. A low BMX on small wheels with fat tyres, a race plate on its bars. It turns inside anything, and a wall hardly slows it.
>
> The Hare. A light racer with a thumb-shifter. Every push on a pedal counts for two, and a hill is nothing to it.
>
> The Comet. A ten-speed with drop bars and tyres no wider than a thumb. Nothing here is faster flat out, or slower to turn and stop.
>
> The Hopper. A BMX with stunt pegs on both axles. It jumps higher than the rest and comes down easy, and the pegs will carry a friend.
>
> The Lamp. A paper-route bike with a basket and a dynamo lamp. The faster it goes, the brighter it burns.
>
> The Rattler. A long-seated bike with a playing card pegged into its spokes. Let the card take it, and for a moment nothing keeps up.

**The rack** is a new block, `bike_rack`: one place in a pipe rack, two hoops for a front wheel on rails that run on into the places
beside it. Which bike is whose for the night is the park's rule and comes with the park.

A rider who gets off a bike in the air leaves it on the ground under where it was, as far as twenty-four blocks down. (It used to
look only two blocks down, and a bike let go of higher than that was gone.)

## What the rider sees and hears

- **The lean**, as before: the riders lean with the bike, the view tips, and the needle under the crosshair shows it.
- **The crouch.** The one pedalling comes down and forward over the bars while the jump key is held. In first person the view dips
  a little. A line under the needle fills as the hop winds up.
- **The line it is travelling.** A bike noses up as it climbs or leaves the ground and down as it comes back.
- **The Rattler's card.** A small upright bar beside the needle: full, with a red pip, when the card is ready.
- **The Lamp's lens** is drawn lit while the dynamo turns.
- The tip and the dip scale with the reader's distortion setting, with no floor.
- **Sounds**, all original and subtitled, made by `tools/generate_bike_night_audio.py`: a bike's tyres leaving the road and both
  coming back down (every bike, Borrasca's too); the card in the Rattler's spokes, higher and louder the faster it goes, and its roar
  in the burst; the Lamp's dynamo.

## How it is drawn

`tools/draw_bike_night_art.py` draws all of it at sixteen pixels to the block. Each bike is built from bars a pixel or so thick, as
Borrasca's is: a frame, its wheels and the cranks for the ridden bike, and the same bike whole for the block. The Wasp and the
Hopper are on small fat wheels, the Comet on big thin ones, the Rattler on a big back wheel and a small front one. The Hopper has
mag wheels and pale tyres. The Lamp has mudguards, a rack, a sprung saddle, a bell, a basket with three rolled papers in it, the
lamp and the dynamo. The Rattler has the long quilted seat, the tall bar behind it, high bars with streamers, a raked fork, and the
card on its peg. The tool's check reads each bike's hubs, axle, wheel sizes and saddle height from the game's own list
(`NightBike.Make`), so the drawing and the ride cannot drift apart.

**No two faces flicker.** Two faces that lie in one plane, face the same way and overlap are drawn at the same depth; where they
are not the same colour the eye sees one and then the other, in a pattern that crawls as it moves. I saw it on the first proof page
as a speckle on the Rattler's chain guard, which lay in the plane of the chain stay beside it. A still page shows it only by luck,
so `tools/flicker.py` now finds it by rule: every pair of faces in a model that share a plane, overlap and differ. It found the same
fault at every joint of every tyre (each piece overlaps the next, and every other one is the groove's colour), where a crank axle
met its seat tube, where a head tube met a top tube, on the rack (rails through hoops) and on the back of each ramp. All are fixed
the same way, one piece a hair proud of the other or stopped where the other begins, and the tool refuses to draw or pass a model
that has any.

**Borrasca's bike had it worse**, and since 0.4.447: its tyre was exactly as wide as its frame's tubes, so the side of the wheel and
the side of the frame flickered wherever one passes behind the other (the seat tube, the down tube, the stem), parked and ridden.
Its tyre is a hair narrower than its tubes now, its stem a hair proud of its top tube, its crank axle in a shell, and
`tools/draw_borrasca_town_art.py` checks its five bike models the same way. Nothing else about that bike's drawing has changed.

The same rule run over every model in the pack finds candidates in several hundred others. Most will be faces that show the same
picture at the same place and so cannot be told apart (the rule here only knows a flat colour from a picture); telling those from
real ones needs each face's picture compared pixel for pixel, which is a pass of its own and is not done here.

## Tests

`BikeNightTests`, a new namespace (`the_oldest_house_bike_night`) with a job of its own in the workflow (`bike-night-gameplay`).
Each case rides on a road of its own in the test world's sky.

- **Getting on and off.** The first hand looks and stands a lying bike up; the next gets on; the keys are shown once, the hop among
  them; the rider sits on that bike's own saddle; sneaking leaves the same bike on its stand, pointing the way it was going; a first
  hand on a standing bike only looks; the Rattler's key is shown the first time and the others not again; a rider's hand on another
  bike takes nothing.
- **The Hare and the Comet.** Three pushes take the Hare nearly to its top and an ordinary bike a little over half way. Flat out the
  Comet is far faster and well ahead up the road. The brake stops an ordinary bike much sooner.
- **The Wasp.** In the same four ticks it has turned half as far again. A wall met at sixty degrees takes half an ordinary bike's way
  and a fifth of the Wasp's.
- **The hop.** A tap, a full hold, the Hopper's; a push in the air shifts weight and nothing else; level landings keep the way;
  crooked ones tip an ordinary bike hard and the Hopper hardly at all.
- **Ramps.** Off the long ramp at speed a bike goes up well past the lip; off the kicker nearly two blocks; crawling off the end it
  hardly leaves the sheet's own line before it drops. Each part stands, for a body, as high as its sheet.
- **The pegs.** Only a Hopper with one rider can be touched; a second reader gets on behind and above the back axle; pushes are
  weaker with two; the second steps off and the bike goes on; over it goes and both come off, each half a heart down, the bike on
  its side.
- **The Lamp.** Dark standing; a light two blocks ahead at full brightness at speed, and only one; dimmer slower; never in a pane of
  glass in its way; dark stopped; none left when the rider gets off, a stray one included.
- **Let go of in the air.** A bike whose rider gets off nine blocks up stands on the road under where it was, and nowhere else.
- **The Rattler.** The key does nothing standing still; held while riding it takes the bike well past its top and is spent in two
  seconds; held on, nothing more; let go, steady riding brings half of it back in seven and a half seconds; the card and its roar
  are heard.

`BorrascaTests#aChildGetsOnKendrasBikePedalsAndGetsOff` also hops Kendra's bike.

The client proof, `BikeNightVisualProof` (`gradle runClient -PbikeNightSmoke`, required in CI), draws five pages: the six on their
stands; each as its ridden renderer draws it, upright and leaning with its nose up; the six lying; six in a rack with the long ramp
and the kicker; and the rider's needle, the hop winding up and the Rattler's card. Every state of every block and every part of
every ridden bike must have its model, and every bike must put something on its page.

## What I have not seen or heard

- **The sounds.** They are made and registered; nobody has listened to them.
- **The rider.** The crouch over the bars and the second rider on the pegs are placed by rule and tested by position, but I have not
  looked at a body on a bike: the proof draws bikes without riders.
- **Tyres on the road.** A bike still makes the game's own footstep sounds for what it rolls over. The bikes' own rolling sounds,
  by surface, come with the town's roads.
- **A hand.** None of this has been ridden by a person. The numbers are first guesses made to pass their cases, and are the
  owner's to change.
- **The flicker gone.** It is gone by rule: no two differing faces share a plane. I have looked at the pages again and the speckle
  on the Rattler is not there, but a moving eye is what shows it, and I have none.

## Names

`client.BorrascaBikeClient` is now `client.BikeClient` and `client.BorrascaBikeRenderer` is `client.BikeRenderer`: they serve every
bike.

## Numbers

Protocol 182 (one new payload: the jump and sprint keys a rider holds). No layout or Witness change: nothing is placed, and nothing
is a source yet. Witness 86/65/63 and layout 135 unchanged. New builds only.
