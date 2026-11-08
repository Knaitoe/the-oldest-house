# The Last Crossing, slice 1 (0.4.351)

The fourth of the yard's stories, built from the owner's design "The Last Crossing" (a multiplayer vignette for four to six readers, three days and three nights, about thirty-three minutes), in the four slices set out in `docs/YARD_STORIES_PROPOSAL.md`. This first slice covers:

- the road;
- the station and its five places;
- Vera and the roll;
- the crew's reservation;
- the day clock;
- Day One's jobs;
- the speaking pipes;
- the handprint plate.

It is not dealt yet, and it adds no Witness source until the crossing can be finished in slice 4. Until then it is reached by the operator's test door.

Layout 112 appends slot 212. Protocol 146 adds:

- the station's fittings;
- the plate, the mooring line, the station lamp and the wrench as carried things;
- four sounds;
- the station's clock packet.

Nothing standing is rebuilt.

From the design:

> *"The crew arrives at a flood station where a keeper is trying to get the last cable ferry across. On the first day she takes everyone's name."*

> *"Vera hands over the clipboard and puts the kettle on. Let readers walk the porch, see the stranded ferry, and ring the small bell beside the loading ramp. She takes everyone's name out loud, calls Ellis after the last reader, then waits."*

- **Names:** `LAST_CROSSING`, `LastCrossing`.
- **Kind of place:** a literary place outside, built whole by `LastCrossingSite`.
- **Box:** x -24..60, y -13..14, z -112..1. Its ground stands 14 blocks up its slot, as Drowned Town's does, so the intake, the sluice pit and the riverbed are inside it.
- **Clock:** it runs on its own (scene clock 17, the hour sent by the station).
- **Story id:** `last_crossing`.

## The road and the station

- **The House's door** opens in the back of a machine shed onto a farm road. The road runs north and drops into floodwater. A sawhorse stands across it at the water's edge with its sign, ROAD FLOODED, and the road's fences stand out of the water where the road goes under.
- **The levee** runs up the east side of the road. Its power line runs on poles from beside the House's door, along the levee, over the water at the mound's edge, and across the apron to the pump room. It carries the eye north-east to the station.
- **The station** stands on a mound at the river's edge, on a concrete apron with walled edges. Its river wall drops to the riverbed.
- **The keeper's office**, with its porch facing the levee, is the gathering place and the clear decision.
  - On the porch: the roll on a clipboard on the porch table, a bench against the wall, and a board that reads ROOK FERRY / FLOOD STATION / KEEPER.
  - Inside:
    - the work slate on the north wall, which is the source;
    - the handprint plate in its brass bracket;
    - a desk;
    - the stove with the kettle;
    - the station lamp on its hook;
    - a shelf of binders.
- **The pump room** has two floors joined by a stair along its west wall:
  - **The gauge room above** has the pressure gauge and the brake lever. An inspection landing (a railed opening in the floor) looks down on the intake. There is a speaking pipe's upper mouth, and an empty bracket for the plate.
  - **The intake below** has:
    - the intake pipe coming in through the west wall, with the wrench lying on it;
    - the bleed screw;
    - three numbered valves;
    - the pump;
    - the service grate with its broken corner and black water under it;
    - the speaking pipe's lower mouth;
    - a shut grate into the service branches.
- **The ferry ramp and sluice.**
  - The landing is a cut in the river wall where *Cordelia*, a flat cable ferry, sits low against its sill with a block of water under her. Her wheelhouse carries her cast brass nameplate in two halves.
  - The loose mooring line lies on the landing, the signal bell stands beside the ramp, and the depth post is marked in feet.
  - The sluice pit is beside the ramp. A ladder goes down to its wheel, and the locking pin is on a deck plate above. A second speaking pipe joins the two.
- **The spillway** is narrow and full to the brim. It runs from a grated culvert to the river, with a footbridge across it.
- **The cable house** stands across the spillway. It holds:
  - the winch drum and its brake;
  - four rollers along the cable bed, one of them slewed off its cradle;
  - the fairlead in the river wall, where the cable has jumped it and kinked;
  - the inspection hatch over a passage flooded to the roof under the rollers;
  - a dry ledge along the west wall;
  - an empty bracket.

  The cable runs out over the river to its anchor on the far bank. The ferry's traveller line runs out to the cable.
- **The lower service branches** are dry and their grates are shut. They join the intake to the sluice pit, and the pit, under the ramp and the spillway, to the cable house's flooded passage. Nothing opens them yet.
- **The far bank** has a landing, a lamp burning on a post, the cable's anchor, a shelter whose door is the way on, and a line of trees.

Banks at the west and east edges hold the water in. Nobody swims the flood: a reader in the river is taken by the current along the river wall and put back on the landing.

## Vera and the roll

Vera is the keeper. She is one shared actor on stranger phase 116, after the Tall Grass driver's, with her own painted skin: a work coat over a cardigan, and hair cut short and gone grey.

1. **Arrival.** She greets each reader who comes up to her porch once: "You'll be the crew, then. Sign the roll and I'll put the kettle on."
2. **Signing.** The roll on its clipboard opens a panel with "Sign the roll." and "Not yet."
   - Signing writes the reader's name under the others. This commits them and reserves their place in the crew for the whole crossing.
   - The sheet takes six names.
   - Until a reader signs, leaving costs nothing.
3. **The roll.** When everyone at the station has signed, or thirty seconds after the first name, Vera takes the roll aloud: "Right. Roll."
   - She calls each name in signing order. The reader it belongs to is offered "Here.", said aloud over their head.
   - She waits for an answer. Unanswered, she calls the name a second time and waits again. A name that is never answered is kept as unanswered.
   - After the last of them she calls "Ellis.", waits, and nobody answers. Everyone there hears it.
4. **The plate and the kettle.** Then she says "Before you start. Come and look at this." and walks into the office to the plate.
   - She lays her palm on it. The near side fogs, and she says what a clean hand and a second print mean.
   - She puts the kettle on (it steams for the rest of the day) and goes back to her porch.
   - A reader who saw her palm on the plate remembers it.
5. **Talking to her during the day.** Right-clicking Vera opens a panel:
   - "What needs doing?" She lists the jobs, the fifth's and sixth's too when the crew is that big.
   - "Who's Ellis?" She says: "Ellis works the ferry. Get the intake going first."
   - "Should someone go and look for him?" She says: "Get the intake going first. Then we'll see."
   - "Hand her the line.", if the reader has it.
   - "The cable's jumped the fairlead.", if they have seen the jam. This reports it, and she answers: "Then that's tomorrow's job, and a pull to free it."
6. **Late arrivals.** A reader who comes once the roll has been taken is partway, and may still sign while there is room; Vera calls their name straight away. A reader on the roll who leaves and comes back while the crossing is live finds their place kept: "There you are."

## The day

Day One runs seven minutes on the station's own clock while anyone is there, from the end of the roll to dusk. The sky goes from morning to evening with it. A day is 8400 ticks and a night 4800, though the nights are the next slices.

When the day's work is done (the intake primed, the roller seated, the jam reported and the line returned), or two thirds of the way to dusk, Vera sends them round once: "Now walk it once, all of it, before the light goes. Office, pump room, the intake below it, the cable house, the ramp." Each reader on the roll who visits all five remembers it.

At dusk Vera says "That's the light going. Get to your places." If the intake still is not primed, she calls the valve down the pipe herself. Everyone on the roll who is there has worked Day One. The station then holds at dusk: Night One is slice 2.

## Day One's jobs

The jobs are stations anyone can walk up to, trade or help with. A reader's record keeps the jobs they worked, in order.

| Job | What the reader does | What they learn first |
| --- | --- | --- |
| **Intake worker** | Takes the wrench off the pipe and opens the bleed screw, which sets the needle in one of three bands. Turns the valve the gauge reader calls. Whoever opens the screw holds the job and can hand it to anyone near them before dark. | With the wrench on the grate (or the wrench dropped by it), something taps back under the broken corner. |
| **Gauge reader** | Reads the needle and calls its band down the pipe ("Three!"), heard at the lower mouth. Holds the brake open above until the intake settles (five seconds within thirty of the valve turning). Then the pump takes the load and the needle settles. | Once primed, the needle dips every thirty seconds though the pump's note never changes. A reader by the gauge sees it. |
| **Cable lookout** | Heaves the loose roller back onto its cradle, tries the inspection hatch (it lifts an inch on black water), and finds the cable jumped from the fairlead. Reports the jam to Vera. | The fairlead's underside is open to the water. The dry ledge is somewhere to stand guard during a pull. |
| **Landing hand** | Hauls the loose line in and returns it to Vera, who coils it on her porch. Checks the sluice wheel below (it holds shut while a hand stays on it) and the locking pin above (it seats only while the wheel is held). | The two knocks rehearsed through the speaking pipe. |
| **Fifth reader** | Carries the station lamp from its hook down to the intake and back up to the gauge room. | Both sides of the intake; the service grate seen through the landing from above. |
| **Sixth reader** | Walks the spillway with the lookout, tries the bell, and takes the coiled line off Vera's porch to make it fast at the head of the ramp. | The way round the cable house. |

A wrong valve sours the pump's note. A turned valve creeps back if nobody takes the load in time, and letting the brake go too soon drops the needle. A single reader can do both sides of the intake by running between them.

**The two knocks.** Once someone has tried the wheel and someone the pin, Vera walks down to the ramp: "Let's try it. One of you down on the wheel, one up here on the pin. Two knocks, and the pin goes in." Then:

- The reader on the wheel holds it and knocks twice on the speaking pipe.
- The reader above hears "Two knocks in the pipe." and seats the pin.

Both remember the rehearsal and who was on the other end. A pin seated without the knocks is only that. In practice the pin is knocked back out after five seconds, ready for the night.

**Handing things over.** Right-clicking another of the crew with the lamp, the wrench or the line in hand hands it over, and both remember it.

## The speaking pipes

There are two pipes:

- one between the gauge room and the intake;
- one between the deck above the sluice pit and the pit's floor.

A knock on either mouth is heard at both. Readers by the other mouth are told "A knock in the pipe.", and two in quick succession are "Two knocks in the pipe." Calls from the gauge come down the pipe as words, heard by whoever is near its lower mouth. Speech after dark, the knocks as signals, and the bell are later slices.

## The handprint plate

The handprint plate is thick glass in a brass frame with a palm etched on its near side. It hangs in its bracket in the office. Right-clicking it with empty hands offers:

- **"Lay your palm on it."** A clean palm fogs the near side for two seconds, where anyone present can see it. Everyone who can see it is told whose palm it was, and remembers. All readers arrive clean. The second print on the far side is drawn and wired for slice 2's infection.
- **"Lift it out of its bracket."** It takes both hands: it is refused with anything in either hand.

While it is carried:

- Letting it out of the hand sets it down where the carrier stands.
- Nothing can be held in the other hand.
- The carrier cannot fight.
- Throwing it sets it down instead.

Its carrier dying, leaving, or disconnecting sets it down or sends it home to the office.

It can be set in any empty bracket (the office, the gauge room, the cable house) or stood on its foot on any floor at the station. Each reader remembers where they set it. The ward that chokes a transformed reader is slice 3.

## What each reader's record keeps

Everything a reader did, learned, saw and was handed is kept in that reader's record. Slice 4's accounts are written from it:

- whether they signed, their place on the roll, whether they answered, and that they heard Ellis called;
- whether they asked after him;
- the jobs they worked, in order;
- the intake: the screw, the gauge read, the valve turned (and a wrong one), whether they were there when it primed, the dip, the broken corner and the tap back, and the intake handed over or taken;
- the cable house: the roller, the hatch, the jam seen and reported, the dry ledge;
- the landing: the line brought in, returned or staged, the wheel and the pin, and the rehearsal and who was on the other end;
- the knocks heard;
- the lamp carried up from the intake, the spillway walked, the bell;
- their own readings at the plate, and whose readings they saw;
- the plate carried and where it was set;
- whatever they handed or were handed, and by whom;
- the route walked;
- Day One worked;
- whether they came partway, or came back.

A station that has started over clears the last crossing's run from each reader's record when they come.

## Reset

Once the station has been empty for ten seconds, it stands as it was in the morning:

- the roll blank;
- the plate in the office;
- the lamp on its hook and the wrench on the pipe;
- the line loose on the ramp;
- the roller off its cradle and the cable jammed;
- the screw shut, the gauge at rest and the pump idle;
- the wheel and the pin free;
- the office door open;
- Vera on her porch.

The crew is cleared, so a new crew starts a new crossing.

## Assets

- `tools/draw_last_crossing_art.py` draws the `crossing_fitting` block, 25 kinds:
  - the porch table, the clipboard (blank and signed) and the work slate;
  - the plate in its bracket and standing, in four readings, and the empty bracket;
  - the stove, the gauge in four needle positions, and the brake (set and held);
  - the three numbered valves and the red bypass, the pipe run, and the bleed screw;
  - the grate (whole and broken), the pump (idle and running), and a speaking-pipe mouth;
  - the roller (seated and unseated), the fairlead (jammed and clear), the drum and the hatch;
  - the sluice wheel (free, held and pinned) and the pin (up and seated);
  - the line (loose, coiled and staged), the lamp hook (empty and lit) and the depth post;
  - the nameplate's two halves, CORD and ELIA;
  - the plate, the line, the lamp and the wrench as items.
- `tools/generate_last_crossing_audio.py` writes four original subtitled sounds:
  - a knock in the speaking pipe;
  - the pump taking the load;
  - something tapping back under the grate;
  - the bleed screw letting go.
- Vera's skin is painted by `tools/paint_character_skins.py` and catalogued in `character_art.json`.

## What the later slices add

As set out in the proposal, the later slices add:

- **Slice 2:** Night One.
- **Slice 3:** Day Two and Night Two.
- **Slice 4:** Day Three and Night Three.

Slice 4 also brings the Witness source, which adds one source and resolves three endings, and the dealing. The default is a crew of four to six readers who cross together.

## Tests

`LastCrossingTests` (literary), each in its own batch:

1. **`aCrewSignsTheRoll`.**
   - The station built whole and not dealt.
   - The water where it belongs and nowhere it doesn't.
   - A crew of four signs; the roll and Ellis; the plate shown and the kettle on.
   - The intake primed by two hands, after a wrong valve. The dip, and the tap under the grate.
   - The cable house: the roller seated, the jam reported, and the dry ledge.
   - The landing: the line brought in and returned, and the pin seated at the second knock.
   - The plate touched with another watching, carried, set in another bracket and on a floor, and put down when it is let out of the hand.
   - Round the route, dusk, and the station set back.
2. **`sixHandsAndTheRest`.**
   - Six sign, and one name is never answered.
   - A late reader finds the sheet full.
   - One reader works both sides of the intake, then hands it over.
   - The lamp is carried up from the intake and passed hand to hand.
   - The spillway is walked with the lookout, the bell is tried, and the line is staged.
   - A reader leaves and comes back to their place on the roll.
   - The current puts a swimmer back on the landing.
   - Dusk, with the intake unprimed.
