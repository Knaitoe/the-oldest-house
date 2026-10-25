# Reworks, 0.4.121

The fifth batch of the owner's notes: ten reworks, each planned and approved before it was built ("Batch 5 plans sound
great. Run with them."). They are built in four parts. The owner's later list of fixes is built alongside them. Nothing here changes the Witness pool: 52 sources / 39
resolutions / two kinds / three endings.

## Part one: Miniatures, Confession, Karen's room, the phone in the canoe

### Miniatures

- **Cards.** A touch on a model stand (the three at (-8|0|8, 1, -18)) shows that model's card in the chat, in grey
  italics. It names the reader and the room (`StaircaseProse.place`, or `UncannyTexts.title`), then says only what the
  House recorded about them there, in the third person and with no pronoun guessed:
  - how often they came and how long they stayed the last time (literary rooms' `Visit` and `Present`);
  - how many rooms back it was (`LabyrinthData.recentVisit`);
  - whether they read what was left out (`Read_Source`);
  - what they did, from their own recorded outcome (`Outcome`, or the Witness record for other rooms), through a table of
    third-person phrases (`MiniatureNotes.PHRASES`), with unlisted outcomes read as their own words;
  - how it ended: the last account read, left where it lay, or left before it was finished.
  A model of a room still ahead says the reader has not been there. The room crossed alone (the model with the second
  figure) adds that no one came in with them. Reading a card sets a bit in `CardsRead`; it grants nothing.
- **The mother.** After twenty seconds in the room (`MOTHER_AFTER`, the visit's own ticks), the server names the room's
  four upper corners to that reader's client, inside the models payload. The client (`MiniatureMotherClient`) may then
  put her in one corner whose whole body already sits at the edge of the screen (74–97% of the half-width or height) with
  a clear line to it: face down on the ceiling, gown hanging, arms reaching past her head, face lifted to the room, lit by
  the corner's own light. A faint scuttle plays. She is gone the moment her head or middle comes inside 58% of the screen,
  or after a second and a half, and waits five to twelve minutes, longer each time, before the next. She is drawn only on
  that client; no entity exists.
- **Her skin** is painted on the shared character grid (`tools/paint_character_skins.py`, `literary_miniature_mother`,
  mark `possessed`: sunken sockets with the whites showing, a bloodless open mouth, lank strands over the face, bare feet
  under a slept-in nightgown). It is reviewed on the gowned cast model (catalog role `LADY`). The native literary-effects
  proof draws her ceiling pose from below.

### Confession

- After each chapter the listener dictates, he asks the person holding the pen one question (`ConfessionAnswers.QUESTIONS`,
  one per chapter), on his name tag and in the reader's chat.
- The reader answers by typing in chat. The line never reaches public chat: the event is cancelled, the reader alone sees
  their own words ("You, quietly: …") and the fragment he repeats while he writes it down (the answer's longest word and
  the two after it). Anything said to him before he asks, during a chapter, is held back the same way, and he says not
  yet. Between chapters, ordinary chat is the reader's own again.
- Thirty seconds of silence before him (counted only while the reader stands in his sight) goes down as
  "(did not answer)".
- The journal takes each question and the reply. The sealed journal is the reader's earlier writing, then his eight
  questions with their answers, one to a page, then the count and the signature. The last account ends on their last
  answer, or on the line saying they gave none. The resolution and its gates are unchanged.

### Karen's room

- The reader's own client projects onto the white wall: their newest F2 screenshots, read from this computer's
  `screenshots` folder, and, among them, small pictures of their screen that the client took now and then without saying
  so (every five to ten minutes of play with no screen open, 160 by 90, the newest 24 kept in
  `the_oldest_house/karen` in the game folder). Up to ten of each, newest first, interleaved; each is cut to 16:9 from its
  middle, scaled to 256 by 144 on a background thread, and shown for six seconds with a projector click. The House's own
  photographs take a turn after every third picture; the projector's button moves the slides on.
- Nothing about these pictures leaves the computer: no image, file name or count is sent to the server. The server only
  names the wall, and now does so even before any photograph is ready.
- Client settings (`the_oldest_house-client.toml`): `projectScreenshots` and `secretSnapshots`, both on by default.
- The native literary-effects proof checks the reading order, the cut and the refusal of non-pictures in a scratch folder.

### The phone in the canoe

- **Filming** shows the phone's viewfinder: blinking REC, a running timecode, frame corners, battery, ISO and quality
  marks and a night grain. Four waterlogged light blocks put moonlight on the water over the drowned steeple.
- **The fall.** As the phone goes over the side the picture tumbles for 24 ticks, with a splash and the sound of going
  under. It then lies looking up through the water for the rest of the seven seconds: a teal dark, a moon broken into
  drifting bands, the odd bubble rising past the lens, the muffled lake, and a viewfinder that says the lens is wet.
- **Coming to.** The fade to black beats like a heart (four beats, slowing). The reader comes to on the jetty: the dark
  lifts over four and a half seconds, they gasp out of the water, drip for twelve seconds, and are told they are soaked
  through. The server marks the finish by sending the end tick with phase 0.
- **Playback.** Recovered from the cave canoe, the phone plays its clip back on its cracked screen
  (`PhonePlaybackScreen`). Each stored frame is drawn for two and a half seconds as a small, grainy night picture of what
  the lens faced, with a crossfade and the frame's own time and description: the sky with the moon, the drowned steeple
  and its belfry, the canoe's boards and ribs, the dark bank, the turn as it fell, and the moon from under the water,
  which it holds with "END OF CLIP". The pictures are drawn from the frame's description, not read from the world.
  Crouching opens the frame record as a book, as before.
- The six pictures were rendered locally from a port of the painter and revised before they were built: the steeple
  became a tower and belfry instead of a tree shape, the boards lost brick-like joints, the bank gained a sky glow
  behind its trees, and the underwater moon grew.
- The native phone proof chain adds three held playback frames and the viewfinder over the water.

## Part two: Poltergeist

- **The house.** The model home is rebuilt whole as a Cuesta Verde show home of the early 1980s: wood paneling and shag in
  the living room, linoleum and harvest-gold paper in the kitchen, a pink child's room. Every fitting is one `tract_house`
  block (26 kinds) with five materials drawn in code by `tools/draw_tract_house.py`. No polish, shell or dressing pass
  touches it. Its windows look onto the night yard, or onto a black backing.
- **Visit one.** The child's door is locked and the six kitchen chairs stack while nobody watches.
- **Visit two.** Nothing stacks. The set shows static; three seconds of watching it opens the child's door behind the
  watcher.
- **Visit three.** Nothing stacks. Whoever goes into the child's room sets the tree off: the glass breaks in and a jointed
  limb of block displays reaches toward them.
- The tree outside stands one stage closer each visit. The resolution is unchanged.

## Part two: the Wheel

- **The rooms.** Behind the front door the numbered rooms are the stages of a daughter's life, each built whole by
  `WheelHouse` from one `life_stage` block (60 kinds) and fourteen materials drawn in code by `tools/draw_wheel_house.py`:
  1. her nursery: duckling paper, a crib with a turning-shapes mobile, drawers with a lamp and the baby book, alphabet
     blocks, the child's chair;
  2. her childhood room: buttercup paper, a bed with a star headboard, crayons, a drawing on the wall, a moon night light,
     glow stars on a painted ceiling;
  3. her teenage room: violet walls, an iron bed, the diary forced open, a lava lamp, a stereo, two posters, KEEP OUT;
  4. her first flat: bare boards, a mattress on the floor, boxes (KITCHEN crossed out), a hot plate, a bare bulb, Mother's
     letter;
  5. her own daughter's nursery: the same paper, an ordinary crib she leans over, the chair at its own size;
  6. the hospital room where her mother lay: ward tile, the bed and its chart, the drip, the monitor, carnations, a
     visitor's chair.
  The first two are built at twice the size and the reader is a child in them. The other rooms are ordinary size, and
  Mother's room has her bed, wardrobe, lamp, armchair and the chair once more. No polish, craft, review or dressing pass
  touches the house.
- **The doors.** Every room has two doors, each with a brass plaque naming the room it opens on: back the way she came,
  on to the next. Rooms are reached only by their doors. Going forward into the first two rooms makes the reader a child;
  going back, they keep their own height, and the child's chair there is drawn at its ordinary size on their screen alone
  ("When I went back through two, the chair in one was smaller"). The front door takes them to the nursery.
- **The wheel.** The hospital room's door says 1 and goes round to the nursery. A reader who has gone back through a door
  and seen all six rooms finds that it opens on Mother's room instead, farther in than the numbers suggest. There they are
  small again. Once they have seen her she says they may leave, and her door, the one with no number, lets them out to the
  hall at their own height, ready to read the account. The resolution is unchanged.
- **Texts.** Each room keeps one short text, shown in the chat when touched: the baby book, the drawing, the diary, the
  letter, her own baby book and the chart.
- The old generic Wheel could not be finished on foot: its cross walls had doors only from one side. The new one can.

## Fixes from the owner's notes ("Need to fix this")

### Fridge corridor (and the other hazard halls)

- The cold store's heat lamps and meat hooks hung a block below the ceiling, and its two condensing units floated off the
  floor and the end wall. They now sit directly under the ceiling (`HazardMaps.underCeiling`), the units in the corner of
  the ceiling and the end wall, either side of the latch door.
- The same goes for the loose lamps in the listing, quiet, weighing, nested, spiral, threshold, duplicate and gravity
  halls. The counting stair's lamps go onto its west wall. The quiet hall's sweep darkens its lamps at the new height.

### Goatman

- The night begins with his voice. Nothing is struck before the first demand; he waits silent on the step and only then
  goes round the trailer. Every demand is spoken first and its blows follow it.
- Much less knocking:
  - two laps of the trailer instead of three, six surfaces a lap instead of eight;
  - three front-door blows instead of six, and about half the blows per demand;
  - one knock every twenty seconds after the demands, instead of three every four;
  - one private lap a night instead of three;
  - the real cousin at the door says his line, then knocks twice every five seconds.
- He crawls only through the bathroom sash. Once inside he stands up and walks out among them as a cousin. While he comes
  in, and for fifteen seconds after he lies down, the cousins in the back half draw away toward the door.
- A body caught on a tread lip steps up it physically; walls stay walls.
- Dialogue is checked against the moment:
  - nobody asks why it has gone quiet while something is knocking (the line waits up to thirty seconds for real quiet);
  - nobody asks for theirs to stay on an empty griddle;
  - lines about his voice, who went out or what went past wait until those things have happened.

### Monkey's paw

- Only the three fingers are wishes: the thumb no longer sweeps across the palm as a finger curls. The window-entry proof
  checks that it stays put.
- Three new first wishes, each taken the worst way, each with its own words from Will, the man from the mill and the
  account:
  - a full larder (the men at the mill take up a collection for the funeral tea);
  - Will kept safe (he is out of all harm now; his watch stopped at ten past four);
  - a fire that never goes out (a fire at the mill that nothing would put out; a coal that does not cool).

### Yacht and slasher

- The arrival copies the last room's doorway only as high as the guest cabin. The deckhouse's forward face above it
  (with two dark windows) and its fill over the arrival corridor are the yacht's own, so the boat no longer shows a patch
  of the previous room from the deck or the water.
- A blow that lands throws a spray of blood off the struck side in the swing's direction, with gouts falling and a wet
  sound.
- Both slashers strike every 75 ticks instead of 90.
- Portholes break by hand in under a second (hardness 0.6, no tool).

### The drag and the pile

- The reader's view rides a hand above the ground while they are dragged (`ElkDrag.EYE`), looking down their own body at
  him, not at standing height.
- One drag a crossing: a second killing blow on the same crossing is a real death (`ElkDraggedThisCrossing`, cleared on
  arrival).
- He keeps away for as long as they are under the pile (`BURIED_GRACE`, five minutes at most), then for thirty seconds
  once they are out (`ESCAPE_GRACE`): he walks out of the cave and the noise of the carcasses does not bring him back.
- Two blows tear a carcass loose (three before), and the hint comes after three seconds. If a minute passes and no way
  through is open, the pile slumps open on one side by itself (`SETTLE`), so no one is left in it for good.

### Child's room

- The bed stands on short legs at floor level, the quilt over its long sides. Crouching at its foot or either side gets a
  child-sized reader down to crawl under it; the gap fits only someone lying flat.
- The doll on the toy chest is gone; the doll on the shelf is the one the room takes for its mobile.
- The bedtime card is small on the desk, so a hand on the desk now opens it. Opening the card starts the room's clock
  (reading to the last page is no longer needed for that); the account still asks for the card read.
- Dinnerbone comes back, unseen, if the figure was lost with the room.
- The server log notes when the room starts closing and each exit it seals.

## Tests

- `miniatureCardsTellTheReaderWhatTheyDidInEachRoom`: the card's wording from recorded facts, an unfinished room, a room
  still ahead, the stands, the mother's twenty seconds and the four corners in open air under the ceiling.
- `theActualCarriedQuillResumesAcrossVisitsAndTheSealedJournalIsReadPersonally` now answers each question with a real
  chat event (cancelled from public chat), leaves the fourth in silence, checks ordinary chat between chapters and the
  sealed journal's pages.
- `theListenerRepeatsAFragmentOfTheReadersOwnWords`: the fragment, one question per chapter, an unanswered journal.
- The recovered phone case: a plain use plays back without writing the record; crouching writes and opens it.
- `elkKillingBlowDragsTheReaderIntoThePileAndTheyTearTheirWayOut` now also checks the camera rides at ground height,
  the long grace under the pile and the head start once out, and that a second killing blow on the same crossing kills.
- `theWheelGoesRoundUntilTheReaderGoesBackThenOpensOnMotherAndLetsThemOutGrown`: the front door, six rooms forward with the
  child's height only in the first two, the wrap to the nursery, going back through two at full height (the room keeps its
  big chair), round again to Mother, her door shut until she is seen, and out to the hall grown.
- The rewritten `ModelHomeTests` cover the three visits, the locked door, the stacking chairs, the set and the limb.
- The Goatman, child's room and paw cases follow the new counts, gates and menus.
