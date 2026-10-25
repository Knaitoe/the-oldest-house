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
- **Flavour.** Before the card, the touch gives one dark-grey line describing the model itself, the way a miniature is
  made (`MiniatureNotes.flavour`): every story room's model has its own line ("The hides are scraps of real leather. One
  has been lifted at the corner."), and a hall or other room uses one of six general lines. The
  lines name things, never a person, so no pronoun is guessed.
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
- **Fix (after Part six):** the nursery's big mobile hung its shapes below what a block model may reach, so the model never
  loaded; its threads are shorter and it now draws.

## Part three: Camp Blood

- **Its history.** Camp Wanaki's third session was closed the morning after a camper went missing at lights out, and
  nobody told the director, R. Hale. He keeps its day still. The camp's papers tell it, and J.'s essay is still the source:
  - the daily schedule, on a stand by the flagpole and on every cabin's noticeboard;
  - three counselors' journals (Dana, Ray, Pat), each on a crate in its cabin;
  - a letter home in every footlocker, a different camper in each cabin;
  - the nurse's log in the first-aid cabinets, dated on past the last entry;
  - the director's unsigned incident report on the mess-hall table, which the radio beside it also gives.
- **The schedule clock** (`CampSession`). One shared clock runs while anyone is in the camp and goes through the six
  periods of the posted schedule, 45 seconds each: reveille at the flagpole, breakfast at the mess hall, cabin inspection
  on the west row, crafts on the east row, campfire at the fire circle, and lights out with rounds to the road. At each
  period the mess-hall bell rings and the PA reads the next line to everyone in the camp. His walk follows the period, so a
  reader who has read the schedule knows where he will be. The session never ends; the day goes round.
- **The bell.** Ringing the bell outside the mess hall calls him there for 30 seconds, off the schedule, dropping whatever
  he was following. It works once for each reader; after that it is only a bell.
- **Hiding.** Under a bunk he never sees a reader, but the boards tell on anyone moving within six blocks, and found that
  way he can reach in. His boots going past are heard close by, with a reminder to keep still.
- **The mess hall** stands west of the path between the middle and last cabins: two benches either side of a long table,
  the radio, the report and a flashlight. A PA horn on a pole points down the camp, and a flag flies at the gate.
- **Pieces:** one `camp_session` block (nine kinds: radio, PA horn, flashlight, mess table, bench, clipboard, schedule
  stand, journal crate, carved plank) and twelve textures drawn in code by `tools/draw_camp_session.py`. Every cabin wall
  gains two planks with initials cut into them.
- **The ending** is still the service road past the last cabins. It now opens to a reader who was chased, who read the
  schedule, or who rang the bell.

## Part four: the Winchester house

- **Always unfinished.** The switchbacks (sixteen long flights with half-block turns), the upper ledger and its resolution
  are unchanged; `WinchesterHouse` adds the house that was never done:
  - a stair of thirteen steps up the west wall of the entrance bay, closed string and balusters, whose last two steps go
    on into a bulkhead under the ceiling (the ATTIC sign is on it);
  - a door onto the west wall with a brass 13 beside it, and a door high in the east wall, on a sill, onto nothing; the
    door off the upper ledger still opens onto the drop into the hay;
  - leaded windows in both side walls that look onto more wallpaper, and a window in the floor of the upper ledger over
    the carpenters' sawhorse, lumber and keg of nails below.
- **The séance room.** Under the ledger, behind the last partition's east door (the one the hay drop leads to): blue
  damask on every wall, a table with a hand bell, one chair, thirteen robes on their hooks (five on each long wall, three
  at the end) and a dim light.
- **Thirteen.** The steps, the robes, the brass 13s, and the hall clock in the entrance bay, whose dial has thirteen hours:
  ten seconds into a visit's thirtieth it strikes thirteen, for each reader on their own.
- **The carpenters** are never seen. Each reader hears them, privately, behind the nearer side wall a few blocks along:
  a hammer driving a nail, or a saw, every five to eleven seconds.
- **Between visits they rebuild.** The four side-passage doors in the middle partitions keep their doors, but two of them
  open onto wallpaper; when a reader comes in to a house nobody else is in, and no camera can see those doors or the
  carpenters' work, the other two are walled instead and the sawhorse and lumber have moved across the house.
- **Materials:** damask wallpaper (oxblood and old gold) on the walls and partitions, basket-weave oak parquet, blue
  damask, leaded glass; and one `winchester_fitting` block (eight kinds: séance table, séance chair, clock and clock top,
  sawhorse, lumber, nail keg, the 13 plaque). Fifteen textures and the models are drawn in code by
  `tools/draw_winchester.py`; three subtitled sounds (hammer, saw, clock) by `tools/generate_winchester_audio.py`.
- No polish, craft, review, dressing or playtest pass touches the house; the rail ledges the polish used to add are built
  with it.

## Part five: the Barretts' house (A Head Full of Ghosts)

- **No more soundstage.** The film crew are in the family's actual home. The filming rooms stay exactly where the seven
  days need them (the dining table, the living room, the open kitchen, Marjorie's and Merry's rooms upstairs, the basement
  under the kitchen, the den), so every take, mark, framing rule and route is unchanged. `BarrettHome` closes the cutaway
  and turns the soundstage that stood round the house into the rest of it:
  - the front door opens on a hall with the stairs to one side: a console with the mail, coats and backpacks on hooks,
    a shoe rack, the girls' school portraits, a runner, a basket of laundry at the foot of the stairs;
  - the garage, through a doorway off the hall: a concrete slab, drywall, the roll-up door, the family's shelves and
    Christmas boxes, and the crew's base (craft services, director's chairs, apple boxes, sandbags, their cases, and the
    call sheets on a folding table: the source);
  - the den as the crew's video village: the family's sofa, television and shelves, the monitor cart, a softbox, the
    producer's chairs, the private-interview corner;
  - behind the stairs the laundry (washer, dryer, baskets) and the pantry off the kitchen (shelves, boxes, the water
    heater); the kitchen gains its fridge (magnets, a crayon drawing, the lunch menu) and a back counter, the living room
    its television;
  - upstairs John and Sarah's room (a double bed, nightstands, a dresser, laundry), the family bathroom, John's office
    (the classifieds with jobs circled, a laptop on a job site, filing boxes), the girls' bathroom, and the spare room
    where the crew keep their gear; the girls' rooms gain a desk, a poster each and clothes on the floor.
- **Furnished, not bare.** The big ground-floor rooms are furnished along their walls so the middle stays open to a
  moving camera: the hall's front room has the good sofa under the window with an end table and a fern, radiators under
  the side window, shelves and a sideboard under the school portraits; the living room a sofa along its wall, an end
  table with library books, two bookshelves and a plant by the door; the kitchen a table with two chairs; the east half
  of the open plan a sideboard with photographs by the dining table, the family computer at a desk, and a sitting corner
  under the back window (a sofa, end table, armchair, footstool, radiator and plants). Nothing stands on a route the
  cast walks, on Marjorie's crawl from the table, at the den door or in the gap through to the den.
- **A house, not a set.** Every wall is painted; there are two whole storeys with a plastered ceiling under the floor
  above; windows on every outside wall have their blinds drawn; ceiling lights in the new rooms; the crew's taped cables
  run from the garage through the hall, under the living-room door, into the den and up the stairs to Marjorie's door,
  and their softboxes stand by the dining table and in her room.
- **Routes kept clear.** The crew's exit, the bedroom pickup up the stairs and Merry's last walk out of the front door are
  walked by the new native test; cables and wall pictures have no collision.
- **Pieces:** one `barrett_home` block (46 kinds: paint, concrete, drywall, windows, the garage door, the storey block,
  ceiling lights, the family's things, bathroom fixtures and the crew's gear) and 74 textures drawn in code by
  `tools/draw_barrett_home.py`. The double bed and the tubs draw past their own block, and `BED_FILL` gives the cells they
  cover their body.

## Part six: Borrasca

- **Four visits kept.** The promise, the taking, the denial and the last night run as before, on the same marks:
  Kimber's walk and carving, the ladder, the taking, the gully, the Sheriff's blockade, the gate, the register, the pack,
  the breaker, the service pipe and the way back down. `BorrascaTown` builds the buildings round them, and a native test
  walks the station aisle, the forks round the Triple Tree and the depot aisle.
- **The town edge.** The reader now steps out of the House into County Trails' station, a board-and-batten lean-to
  against the House's wall: the trail notice on its lectern, the account on its desk, a stove and woodpile, a bench under
  the window, pamphlets, the lost and found, and a trail map on the wall. Out of its doorway the trailhead board stands by
  the path. Round the gravel lot, where the county pickup and Prescott's truck (the Last Stop's pickups, tan and dark
  green) are nosed in, stand Drisking General (a false front, a porch with the town's noticeboard, counter and till, stock
  behind it, two aisles, the cooler) and the Sheriff's office (brick, a counter with a swing gate, two desks and the radio,
  the files, the county map and a holding cell). A rail fence and hedge close the town's back edge, with pines behind the
  buildings.
- **The fort and the Triple Tree.** The three trunks now grow from one burl, and the trail forks round it. The fort in
  their crotch has walls with a window each side, a doorway beside the ladder's hatch, a covered landing with a rail and a
  gabled roof; the trunks run up through the floor, the back wall and the roof and crown together above it. Inside: the
  chair, a barrel, a shelf, a rug, a candle, a lantern under the ridge and Kimber's notes pinned up on the back wall. On
  the adult visits the walls age with the floor.
- **The ridge depot.** The old stone room becomes a steel depot with a copper gable roof, windows and a front lever by the
  office door: an office (filing drawers, two desks, the route board, the receiving counter, a water cooler and a bench), a
  sorting room (tagged property drawers, the long sorting tables with bundled effects on them, the register and the pack on
  their desks, shelved effects behind a wire cage) and a back room (lockers, a bench, a table and the coffee), along one
  aisle from the front door to the service exit. The machine stands in its own steel shed with an open bay on the yard and
  the lamp; the belt runs through both ends into a spoil bin. A gate hut stands inside the wire, and the yard has drums,
  pallets, a fuel tank and lamp posts, all off the line from the taking to the lamp. The way back down now starts west of
  the depot instead of under its corner.
- **The papers.** The trail notice and a trail map are rewritten as County Trails' notices. The store's noticeboard is the
  town's own paper and changes between visits: a church supper, a lost dog, the curfew, Prescott's night work and an older
  girl's sun-faded poster in childhood; Kimber's own poster, gone soft, with CLOSED written across it, years later; and on
  the last night a new poster for Mara over what is left of hers. Kimber's notes in her own hand give the fort's rules,
  what the children know of the Shiny Gentleman, the word the big kids use for the top of the ridge, the sound and the
  trucks after it, and what she keeps there; on the adult visits the damp has taken words out of them. The property
  receipt is rewritten: collected at Marker 3 at 6:40, released by the Sheriff's office, carried by Prescott, release to
  family none, and the next tag. Each visit's board and the aged notes are their own originals; reading them confers
  nothing.
- **Pieces.** The cast wear their own 64x64 skins on the standard humanoid mesh (`tools/draw_borrasca_cast.py`): Kimber,
  Kyle, the Sheriff, Prescott and the plant's coveralls. The town's fittings (siding, steel, rust, paper, the noticeboard,
  filing drawers, the belt, truck paint and the machine frame) get 13 block textures of their own in
  `textures/block/borrasca/`, and the sixteen trail tiles are redrawn, all in `tools/generate_borrasca_trail_assets.py`.

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

- `miniatureCardsTellTheReaderWhatTheyDidInEachRoom`: every model's flavour line (its own for each story room, none with a
  pronoun), the card's wording from recorded facts, an unfinished room, a room
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
- `theLastSessionKeepsItsScheduleAndTheMessHallBellCallsHimOnce`: the session clock runs, the schedule names its places, the
  walk follows the period, and the bell calls him once for each reader.
- `theWinchesterIsNeverFinishedAndItsCarpentersRebuildOnlyAnEmptyUnwatchedHouse`: the wallpaper, parquet and leaded glass,
  the thirteen steps into the ceiling, the doors onto the wall and onto nothing, the séance room's table, chair and
  thirteen robes, the clock; nothing is rebuilt while a second reader is inside; an empty, unwatched house swaps which
  side doors open onto wall and moves the sawhorse; the clock strikes thirteen once and the carpenters are heard.
- `theBarrettsHouseIsAHomeWithTheCrewInItAndEveryFilmingRouteStaysClear`: the closed front, two storeys, the garage, the
  call sheet on the crew's table, the fridge, mail and washer, the village cart and cables, sixty-odd windows, every
  bed and tub's body, and a body clear at every mark and between marks of every route the cast walks.
- `theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits`: the station roof, notice and aisle, the store
  and office, the fort's walls, doorway, trunk and roof, the forks round the burl, the depot aisle and its doors, drawers
  and shed; the trail map, the childhood board and Kimber's notes, then the later board and the aged notes, then the last
  night's board, each its own original and none of them credit.
- The rewritten `ModelHomeTests` cover the three visits, the locked door, the stacking chairs, the set and the limb.
- The Goatman, child's room and paw cases follow the new counts, gates and menus.
