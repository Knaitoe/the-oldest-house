# Bike night — a proposal (November 10)

The owner's notes for a bike vignette, recorded as given and then worked into a design for review. Its first slice, the bikes, is built (0.4.463), the park they stand in (0.4.464), the playground's swings and sand balls (0.4.465), its slide (0.4.466), its monkey bars (0.4.467), its seesaw (0.4.468), and its merry-go-round (0.4.469); the rest is not. It comes after Borrasca, using the bikes Borrasca already has (`docs/BORRASCA_BIKES_0_4_435.md`).

## The owner's notes

> - Players can select from several bikes. These bikes have slightly different stats - or really, each has a unique perk. better maneuverability, better acceleration, higher top speed, better jumping, headlights, a card in the wheel that allows a temporary 'turbo' boost.
> - The vignette are dorks at night. Kids on a playground maybe? Lots of fun potential for unique interaction here - a seesaw, slides, swings, monkey bars, etc
> - After a beat, bullies approach. The players can throw balls of sand at the bullies. Bullies eventually pursue the players;
> - The pursuit leads around a detailed 80s small town at night. Past laundromats, fast food, groceries, a theater and school. Cars pass by, passerby's honk yell and cheer from the street and sidewalks. The bullies pursue yelling jeers.
> - Eventually the player or players have to cut through a graveyard - here, they have new obstacles to avoid - headstones and other memorials. The bullies can lag behind as mist creeps up. This could lead to a different sort of chase. Maybe find another mechanic to keep this mechanically unique - the city starts to melt away and the players have to platform - jump and swerve - their bikes through a realm of ghouls and goblins jumping out. That could utilize a bike-jump feature I was hoping to include, could instance platforms for players to jump to and from taking them high and low - as the bullies fall behind they could be chased by several Halloween specters, witches, werewolves, etc. its resource intensive but could be super flavorful. Eventually they exit from the other side of the graveyard

November 11, on seeing the three as first painted and the town as first built:

> - give the bullies more contemporary clothing please. skull with fire eyes on a t-shirt, hoodie, etc. the neighborhood looks good but would look better with less even streets, some suburb shortcuts, etc
> - clutter and decor in alleys would benefit it too. space between buildings, dumpsters, trash

(The clothes: 0.4.480, `docs/BIKE_NIGHT_CLOTHES_0_4_480.md`. The streets, the shortcuts and the alleys: 0.4.481, `docs/BIKE_NIGHT_TOWN_0_4_481.md`.)

November 11, while the town was being rebuilt:

> - do you have a path to the cemetery planned?

(Yes: every way north comes out on Church Lane, which touches the cemetery's railings at its gate; the straight way is the cut, a footpath from Elm Street, named CEMETERY at its mouth. The same note.)

Everything below is Claude's working of those notes. Names, places, lines and creatures are original, and nothing recreates a particular film or game.

## The shape

**The night.** Halloween night in **Calder Falls**, a small town in the 1980s. The readers are the kids who didn't go trick-or-treating: the dorks, out on their bikes at the park after dark.

**Three acts**, each played differently:
1. **The playground.** Free play, with every piece of equipment a toy that does something.
2. **The town.** A chase on bikes through lit streets, among traffic and people.
3. **The graveyard, and what it turns into.** Obstacles in the mist, then the town melts away into a course of floating ground, jumped and swerved through with monsters after them.

It ends at the graveyard's far gate.

**Who.**
- **The readers** are at child height, as on Borrasca's childhood visits.
- **The ride:** one to six readers, one bike each.
- **Alone,** a reader has one friend along, **Gus**, a character on his own bike, who talks, rides the seesaw with them and keeps up in the chase.
- **More than six readers** get a town of their own (the scene-copy system).

## The bikes

Six bikes lean in the rack at the park gate. Each reader takes one, and each bike is one reader's for the night.
- **One of each.** Six bikes, six readers. In a group the choice is a real one, because each perk opens something on the course that the others can't (see **The Hollow**).
- **The controls stay.** All six ride on the existing bike's controls: the strafe keys are the pedals, pedalling is balancing, the back key brakes and sneaking gets off.
- **One perk each.** Each bike is otherwise ordinary and has one unique perk:

| Bike | The look | The perk |
| --- | --- | --- |
| **The Wasp** | a low BMX with small wheels and fat tyres | **Handling.** It turns much tighter and leans less in a turn, and a glance along a wall keeps nearly all its way. |
| **The Hare** | a light racer with a thumb-shifter | **Acceleration.** Each push drives it on harder, and it comes up to speed in half the time. |
| **The Comet** | a ten-speed with drop bars and thin tyres | **Top speed.** It goes much faster flat out, but it is slow to turn and slow to stop. |
| **The Hopper** | a BMX with stunt pegs on both axles | **Jumping.** A higher, longer hop, and it lands soft. The pegs carry a second rider (see **Riding double**). |
| **The Lamp** | a paper-route bike with a front basket and a dynamo lamp | **The headlight.** A real moving light, brighter the faster it goes, as a dynamo is. It shows the path in the mist and the ground in the dark. |
| **The Rattler** | a banana-seat bike with a card clothes-pegged into the spokes | **The turbo.** Holding the sprint key flaps the card into a rattle and gives a short burst. It recharges while the bike is ridden steadily. |

- **The jump** is the bike-jump feature the owner wants, on every bike:
  - **The pop.** Holding jump crouches the rider over the bars, and letting go pops the bike up. A longer hold pops higher, up to a limit; the Hopper's limit is higher.
  - **Ramps** throw a moving bike in the air by its own speed, as the swing does (`docs/SWING_JUMPS_0_4_423.md`).
  - **Landings.** Landing on both wheels keeps the way. Landing crooked tips the lean hard, so the reader must push out of it.
- **Riding double.** The Hopper's pegs take a second reader standing on them. A reader who has lost their bike can be carried on someone else's pegs. That makes losing a bike in the chase a co-op rescue rather than the end.

## Act one: the playground

A park with a sandy playground under two lamps, one of which flickers. Every piece of equipment does something real:

- **Seesaw.** It takes two: two readers, or one and Gus. Each pushing down at the right moment bounces the other higher. A big bounce throws the one on top off with its speed, like a swing jump.
- **Swings.** The existing swing, with the existing jump off it, out into the sand. Every jump carries forward (0.4.443, `docs/SWING_JUMPS_0_4_423.md`).
  - **A push.** Added by the owner on 11 November ("please ensure players can push other players on the swing set"): somebody on the sand pushes the one on a swing, and has to time it (0.4.474, `docs/SWING_PUSH_0_4_474.md`).
  - **Over the bar.** Added by the owner the same day ("it would be cute if there was a way for the player to travel all the way around the swing. it should be slightly difficult to manage"): hidden behind a rumour scratched into the frame, and done by kicking exactly as the swing turns, five ends in a row (0.4.475, `docs/BIKE_NIGHT_OVER_THE_BAR_0_4_475.md`).
- **Slide.** A long metal slide. A reader goes down it seated and shoots off the end with speed. Riding down it on the Wasp is possible, and Gus is impressed.
- **Monkey bars.** A new verb: hanging and swinging hand over hand.
  - **The verb.** A jump at the bars grabs one. The strafe keys swing the body, and each swing at the top of its arc reaches the next bar. Letting go drops.
  - **The rhythm.** Too early misses the bar, and the reader drops into the sand. It is a timing game, like the pedals.
- **Merry-go-round.** Readers spin it by running alongside, then jump on. Anyone letting go flies off with its speed. With several readers on it, the one pushing sets how fast everyone goes.
- **Spring rider.** A horse on a spring that rocks with the strafe keys. It is pure play.
- **Climbing dome.** Climbable on every bar, with a view over the park.
- **Sandpit.** The source of the sand balls: using an empty hand on the sand packs one, and readers can carry a few. Thrown like Winter Lake's snowballs (`docs/WINTER_LAKE_0_4_104.md`), they burst into a puff.
- **Also there:** a drinking fountain that works, a payphone that only takes coins, and a bench with a carved heart and two sets of initials.
- **The tone.** Gus talks the whole time about nothing. The readers are left alone long enough to play.

### The bullies arrive

- **When.** After a few minutes of play (an occupied clock, as at the Last Stop), headlights sweep the fence. Three older kids ride up on BMXs and circle the playground.
  - **Their names:** **Tully**, who leads; **Ray-Ray**; and **Duff**.
  - **Their talk** is jeers, in captions over their heads and never in chat. They are PG and pointed, never slurs: "Look, it's the science club." "Nice bike. Is it your mom's?" "Swing higher, dork."
- **What they do.**
  - They take something: Gus's cap, or a reader's own, which comes back at the end.
  - They kick sand, and one of them stands on the seesaw.
- **Sand balls.**
  - **A hit** makes a bully cough and wipe his eyes for a moment. He stops where he is.
  - **It buys time,** but each hit makes them angrier.
  - **The trigger.** A few hits, or enough time, and Tully says "Get them." From then on the chase is on, and the readers run for the rack.

## Act two: the town

The chase runs through Calder Falls, built whole at night and dressed for Halloween:
- pumpkins on porches;
- paper skeletons in shop windows;
- toilet paper in a tree outside the school;
- trick-or-treaters in costume on the sidewalks with pillowcases;
- reusing Costume Night's painted costume skins where they fit.

**The route.** A loop through the middle of town, with side ways and alleys. The readers can take any way; the bullies know them all.

- **The laundromat.**
  - Lit, with dryers tumbling.
  - A woman is folding alone at the counter, and a television on a bracket plays a late horror film.
  - Its back alley has a loading dock to jump off.
- **The burger drive-in.**
  - Neon, with an arcade cabinet in the window. It is Polybius's cabinet if it fits the decade.
  - Teenagers sit on the hoods of their cars in the lot. They cheer the readers and boo the bullies.
  - The drive-thru lane is a way through.
- **The grocery.**
  - Closing up: the lights go off aisle by aisle.
  - A stock boy is wheeling a long chain of carts across the lot. They are a moving obstacle, and riding through the gap in the chain is a shortcut.
- **The theater.**
  - A marquee in changeable letters with an original late double feature.
  - A line of costumed teenagers waits outside. As the readers pass, the line cheers and parts for them, then closes on the bullies.
- **The school.**
  - Dark, with a chain-link fence. A hole in the fence is a reader-sized shortcut across the football field and under the bleachers.
  - Riders leave tyre tracks in the dew on the field, and the bleachers' shadows are dark.
- **The rest of the street:**
  - a bank clock that shows the time and temperature;
  - a VHS rental (the work-print shop's shopfront, if it fits);
  - payphones;
  - a barber pole;
  - a closed hardware store with rakes in the window.

**Traffic.**
- **The cars:** a new, simple actor, a car driving its lane with its headlights on.
- **Who drives them:** a station wagon with wood panels, a pickup, a long sedan, a muscle car with its windows down.
- **Honks:** they honk at a rider in their lane and slow down. They never hit anyone, but a rider who swerves into one comes off.
- **Voices:** people call from windows and sidewalks, in captions: "Get off the road!", "Go, kid, go!", "Leave 'em alone, Tully!" One car slows beside the bullies and yells at them, which buys the readers a few seconds.

**The bullies.**
- **How they ride:** on the same bike physics, simpler.
- **How they hunt:** they split up. One stays on the readers' tail, and the others take the town's back ways to head them off at a corner.
- **Jeers:** they keep up a running stream of them.
- **Caught:** a bully within arm's reach of a rider for two seconds has caught them (see **Endings**).
- **The way out:** the chase steers the readers toward the edge of town, where the only way the bullies aren't blocking is the graveyard gate.

## Act three: the graveyard, and the Hollow

**The graveyard.** It sits on a rise at the edge of town, inside an old iron fence.
- **The paths** are gravel.
- **The obstacles:** headstones at every lean and height, and taller memorials:
  - an angel;
  - an obelisk;
  - a family mausoleum with iron doors;
  - a fresh open grave with a mound and a tarp, which is a hazard to jump.
- **Riding it:** riders weave and hop between the stones. A stone is a wall: a glance off it is fine, a head-on hit puts the rider over the bars.
- **The mist.**
  - It rises off the grass and thickens with each row.
  - The Lamp's headlight cuts it. Without it, the stones come up close and sudden.
- **The bullies:**
  - they come through the gate, slower;
  - one stops at the fence ("I'm not going in there");
  - the other two fall back in the mist, and then their jeers stop altogether.
- **The stones tell a story.**
  - One row of small headstones near the back all carries the same autumn date, years before, with names and nothing else.
  - The town has a story about the kids who cut through the graveyard on Halloween. Gus tells it badly on the ride, earlier, as a joke. It is never told properly.

**The town melts away.** Past the last row, the lights of Calder Falls go out behind the riders, one street at a time. Then the ground ahead falls away. They are riding into **the Hollow**:
- the same night and the same moon;
- a course of floating ground, high and low, over nothing.

**The Hollow** is the owner's platforming realm.
- **The ground:**
  - islands of graveyard turf with their headstones still in them;
  - crooked fences;
  - a bridge of coffin lids;
  - a pumpkin vine the size of a road, curling up and down;
  - stairs of tilted stones.
- **The riding:** riders jump and swerve from piece to piece. Missing a jump drops them onto the last island they stood on (a checkpoint), with a scare.
- **Ghouls and goblins** jump out:
  - from behind stones, from under coffin lids and from inside the vine;
  - they are pop-up obstacles in the rider's path, swerved round or hopped over;
  - they never touch anyone, only startle and block.
- **Specters chase:**
  - **witches** swoop low on brooms across the path, to be ducked or dodged;
  - **a werewolf** runs alongside on a parallel island and leaps across at gaps;
  - **ghosts** drift through the course and through the riders, chilling their view;
  - **a headless rider on a headless horse** comes up behind on the last stretch.
  - Being caught by any of them is the Hollow's failure (see **Endings**).
- **Routes, high and low, opened by the bikes:**
  - **high:** a gap only the Hopper's jump clears, or the Rattler with its turbo;
  - **long:** a run only the Comet's top speed carries;
  - **dark:** only the Lamp shows where the ground is;
  - **tight:** a twisting path along the vine only the Wasp turns sharply enough for;
  - **steep:** a climb only the Hare gets going up in time.
- **Every bike has a way through,** and every bike has a way it can't take. In a group the riders split and regroup, so the perks matter. The Lamp's light helps whoever rides near it, and the Hopper can carry a rider whose bike was lost.
- **Instanced or shared?** The owner suggests instancing the platforms per rider.
  - **For co-op,** Claude suggests one shared course built in the slot: riders can see each other, carry each other and split up.
  - **The specters** would be private to the rider each one chases, as the House's other private actors are, which keeps the entity count down.
  - The course is real blocks, built once.
- **The way out.** The Hollow folds back down into the graveyard's far side. The riders come out through the back gate onto a quiet lane above the town, with the lights of Calder Falls on again below.

## Endings

The owner has asked for permanent failures (as for the Pell house). These follow that unless the owner says otherwise for a lighter vignette.

### Success: out the back gate

- **The condition:** the reader rides out of the far gate.
- **What follows:**
  - Gus is there if he made it (in co-op, the other riders who made it);
  - the taken cap is hanging on the gate;
  - the bullies' three bikes lie abandoned by the front gate, and the bullies are nowhere.
- **The account** (survival kind) remembers:
  - the bike;
  - the best thing they did on the playground;
  - how many sand balls hit;
  - which streets they took;
  - whether they carried anyone;
  - which way they went through the Hollow, high or low.
- **The keepsake:** Claude suggests the bike itself, owner-bound, rideable in the Overworld and the House with its perk. A smaller alternative is the Rattler's spoke card. The owner's call.

### Failure: caught in town

- **The moment:** a bully catches the reader. He takes the bike, the rider is shoved into the leaves, and the bullies ride off laughing with it.
- **In co-op,** the reader is not out yet: the Hopper can carry them on its pegs. Only a reader nobody carries is out.
- **A reader who is out** walks home. The night is over for them, and the vignette is never dealt to them again.
- **The bike** turns up later, in a later hall, chained to a school fence with its front wheel bent.

### Failure: caught in the Hollow

- **The moment:** a specter catches the reader. The screen goes to mist.
- **Afterwards:** they come round at dawn outside the graveyard's front gate, alive, the bike gone.
- **What stays:**
  - the vignette is never dealt to them again;
  - in that town, in every copy any reader later visits, there is a new small headstone at the end of the row with the old date. It carries this reader's name and the date of their night, and their bike leans against it, rusting.
- **For the others:** in co-op the others ride on and see it happen.

## The town's detail

The owner asked for "a detailed 80s small town". The same rule as the Pell house applies:
- **No vanilla stand-ins:** every storefront, sign and street fixture is its own texture, drawn in code at native 16-pixel scale with eight-pixel faces.
- **Readable signs:** they read at riding height.
- **Review:** an eye-level render review covers every street.

- **Signs and fronts:**
  - **The laundromat:** "WASH-O-MAT", with coin-op prices in the window.
  - **The drive-in:** "BIG TOP BURGERS", in neon that buzzes and flickers one letter.
  - **The grocery:** "CALDER FOOD FAIR", with weekly specials hand-lettered on butcher paper.
  - **The theater:** the marquee's changeable letters, with an original double feature that changes on each visit.
  - **The school:** "CALDER JR. HIGH", in cast letters over the door, with the trophy case lit.
  - **The bank clock:** shows the real time of the night and a temperature.
- **Lights.** Neon and lit windows are emissive textures that light their own glow. Streetlamps light the main street, and the alleys are dark.
- **Halloween:** pumpkins carved in many faces (a sheet of them), paper decorations, costumes.
- **The graveyard:**
  - headstones in many shapes, some leaning, with carved names and dates (original, period-plausible), lichen and moss;
  - the angel and obelisk models;
  - the mausoleum with its iron doors;
  - the iron fence with spear tops, which must be connected properly, unlike the Corner's fence;
  - the open grave's mound and tarp.
- **The Hollow:**
  - floating turf undersides with roots hanging;
  - the coffin-lid bridge;
  - the giant vine and its leaves;
  - stone stairs;
  - creature skins for the ghouls, goblins, witches, werewolf, ghosts and the headless rider, painted by the shared painter.
- **Sound:**
  - the town: distant music from a car radio, the dryers, the neon's buzz, the theater's laughter;
  - traffic: car horns, each car its own;
  - the bikes: the Rattler's card (louder in its turbo), the dynamo's whine, tyres on concrete, gravel and grass;
  - people: the bullies' jeers, called;
  - the graveyard: mist and the iron gate;
  - the Hollow: the witches' cackle, the werewolf's howl, the ghosts' wind, the headless rider's hooves.
  - All original and subtitled.

## Fit with the House

- **Witness:** one new survival source. Success resolves it, and neither failure does. The quota stays derived from the pool.
- **Multiplayer:**
  - up to six readers a town, one bike each;
  - the town, the bullies and the course are shared;
  - the specters are private to the rider they chase;
  - accounts are each reader's own, with co-op lines for who rode with whom and who carried whom.
- **New builds only.**
- **Writing rules:**
  - the House's writing never genders the reader, never names a colour or an age, never tells the reader how they feel;
  - speech is never chat; the bullies' jeers and the street's calls are captions.
- **Comfort:** the cut to mist, the ghosts' chill and every shake scale with `Comfort.effects()`, with no floor.

## Content lines

- **The bullies:**
  - they are kids, and their threat is humiliation, never injury;
  - they shove, take and jeer;
  - their language is PG, with no slurs.
- **Sand balls** sting and blind for a moment, and do nothing else.
- **The monsters** are Halloween-scary, not gory. Caught is a cut to mist, never a wound.
- **Falls** from bikes cost a little, as the existing bike's do, never below two hearts.

## What it would take

It is large. It has three acts, a town, and a course in the air. In slices:
1. **The bikes** (built, 0.4.463, `docs/BIKE_NIGHT_BIKES_0_4_463.md`; whose bike is whose comes with the park):
   - the six bikes and their perks, on the existing bike;
   - the jump and landings;
   - riding double;
   - the rack and the choice.
2. **The playground:** the seesaw, the slide, the monkey bars (new verb), the merry-go-round, the spring rider, the dome, the sandpit and sand balls. Gus. (The park itself is built, 0.4.464, `docs/BIKE_NIGHT_PARK_0_4_464.md`: the place, its railings, gate, lamps, rack and sand, and whose bike is whose. The swings and the sand balls are built, 0.4.465, `docs/BIKE_NIGHT_SWINGS_0_4_465.md`, the slide, 0.4.466, `docs/BIKE_NIGHT_SLIDE_0_4_466.md`, the monkey bars, 0.4.467, `docs/BIKE_NIGHT_BARS_0_4_467.md`, the seesaw, 0.4.468, `docs/BIKE_NIGHT_SEESAW_0_4_468.md`, and the merry-go-round, 0.4.469, `docs/BIKE_NIGHT_ROUNDABOUT_0_4_469.md`. The monkey bars and the seesaw were first built short of what is written above, and were put right in 0.4.470, `docs/BIKE_NIGHT_RHYTHM_0_4_470.md`. The spring rider, the climbing dome, the drinking fountain, the payphone and the carved bench are built, 0.4.471, `docs/BIKE_NIGHT_SMALL_THINGS_0_4_471.md`. Gus at the playground is built, 0.4.476, `docs/BIKE_NIGHT_GUS_0_4_476.md`; his bike and his riding come with the chase. Riding the slide on a bike is noticed, 0.4.477, `docs/BIKE_NIGHT_SLIDE_BIKE_0_4_477.md`; a hill takes and gives a bike its speed, 0.4.478, `docs/BIKE_HILLS_0_4_478.md`.)
3. **The bullies** at the playground, and the start of the chase. (Built, 0.4.479, `docs/BIKE_NIGHT_BULLIES_0_4_479.md`: the three, their bikes, the gate, the cap, the seesaw, sand balls and "Get them." Being caught comes with the town.)
4. **The town:** the streets, buildings and dressing (built, 0.4.481, `docs/BIKE_NIGHT_TOWN_0_4_481.md`); the traffic actor; the passers-by; the bullies' hunt through the back ways.
5. **The graveyard:** the stones, the mist, the bullies falling back.
6. **The Hollow:** the course, its routes, the pop-up ghouls and goblins, the specters.
7. **The endings,** the account, the keepsake, the failures' headstone and chained bike, and the aftermath.

## Open, for the owner

1. **Halloween:** is it Halloween night, as assumed here?
2. **Controls:** keep the existing pedal-to-balance controls for the chase, or give these bikes a simpler ride, holding forward to pedal, so the chase is about the route?
3. **The keepsake:** the bike itself, rideable outside the vignette, or the spoke card?
4. **Permanent failures:** both permanent, as written, or is this a lighter vignette where being caught in town lets the reader walk back and try again?
5. **The Hollow:** one shared course as suggested, or a course instanced per rider as in the owner's notes?
6. **Order:** where does it go in the queue, before or after the Pell house?
