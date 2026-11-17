# The multiplayer audit

The owner, 13 November: "confirm intended and functional multiplayer compatibility for each", after "All multiplayer vignettes should
be hooked through that system".

Every story in the Witness pool (88), how it holds more than one reader, and the case that takes two readers into it. Kept current:
a story that changes how it holds readers changes its row here.

## How the stories hold readers

- **One room for everyone.** The scene is built once. Each reader's part is their own (what they have read, seen and done, their
  account, their own people where the story gives them any), and the people a reader meets are the ones the game shows them.
- **Written for a party.** The scene holds a set number at once, each in a place the story gives them (0.4.558's role spots). When it
  is full, the next reader through the door goes to another copy, built whole a million blocks west by the scene's own builder, and
  their door waits while it is made ready (`SceneInstances`).
- **One at a time.** The story is one reader's. The next reader through the door goes to a copy of their own.
- **A copy each.** Every reader has their own copy.
- **A place of their own.** The scene is built for each reader where they are (the backseat's road, the silo, Drisking, the family
  copy of their own home).

## How a story is proved

- **`TogetherSceneTests`** (namespace `together`, 66 cases, 0.4.565): the scene is built as the House builds it, and two readers come
  in through the House's door, the second as the door decides (into the first's scene or a copy of their own), with every scene's own
  welcome run for each (`LabyrinthDoors.scenesArrived`). The second must be where the story says; where there is a paper, each reads
  their own and one reader's reading is never the other's; then both go round the scene's people for a quarter of a minute, and the
  people are held to the cast checks for each reader (`CastConduct`).
- **`TogetherConductTests`** (namespace `together`, 18 cases, 0.4.564): the same for the talking scenes, with both readers walking the
  conversation panel's branches.
- **The copy cases** (`LiteraryTests`, and each story's own): a party or a one-at-a-time story full to the next reader, who goes to a
  copy that matches the first block for block, with its own people and clock.

| # | Story | How more than one reader is held | Proved by |
|---|---|---|---|
| 1 | The floor (floorboards) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#floorboards` |
| 2 | The wardrobe (hide and clap) | One reader at a time: the next has a copy of their own. | `TogetherSceneTests#hideAndClap` |
| 3 | The study (harrigan) | One reader at a time: the next has a copy of their own. | `TogetherConductTests` (a talking scene) |
| 4 | The window (model home) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#modelHome` |
| 5 | The lake (drowned town) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#drownedTown` |
| 6 | The congregation (preserved cave) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#preservedCave` |
| 7 | The boys (shallows) | Up to six at the bank: one takes her wrists, the next her feet, the rest are the boys; the next reader goes to another copy. | `TogetherSceneTests#shallows` |
| 8 | The recording (phone canoe) | One reader at a time: the next has a copy of their own. | `TogetherSceneTests#phoneCanoe` |
| 9 | The trailer (goatman) | Up to sixteen in one trailer, readers in the cousins' places; the next reader goes to another copy. | `TogetherSceneTests#goatman` |
| 10 | The small passage (ted caver) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#tedCaver` |
| 11 | The readers (zampano courtyard) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#zampano` |
| 12 | Return to sender (whale) | One reader at a time: the next has a copy of their own. | `TogetherSceneTests#whale` |
| 13 | The cover (barn well) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#barnWell` |
| 14 | The distant frame (plain) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#plain` |
| 15 | The call button (hospital) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#hospital` |
| 16 | The counted lights (holloway camp) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#holloway` |
| 17 | The keeper (mother den) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#mother` |
| 18 | The empty chair (seance) | Up to four at the table; the next reader goes to another copy. | `TogetherSceneTests#seance` |
| 19 | The pattern (wallpaper nursery) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#wallpaper` |
| 20 | The closing account (hotel) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#hotel` |
| 21 | The returned knocks (hill nursery) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#hillNursery` |
| 22 | The little rooms (miniatures) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#miniatures` |
| 23 | The last clock (masque) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#masque` |
| 24 | The coffin lid (usher) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#usher` |
| 25 | The upper ledger (winchester) | A copy of their own for each reader. | `TogetherSceneTests#winchester` |
| 26 | The disappearing exits (child room) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#childRoom` |
| 27 | The recorded voices (crimson hall) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#crimsonHall` |
| 28 | The wet boards (bly route) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#bly` |
| 29 | The looping field (elk lot) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#elkLot` |
| 30 | The broken light (elk fan) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#elkFan` |
| 31 | The hall at 2:49 (mapping interior) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#mapping` |
| 32 | The food in the hollow (holy rabbit) | One reader at a time: the next has a copy of their own. | `TogetherSceneTests#holyRabbit` |
| 33 | The listener's signature (confession) | One reader at a time: the next has a copy of their own. | `TogetherConductTests` (a talking scene) |
| 34 | The passing boots (elk carcasses) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#elkCarcasses` |
| 35 | The thirteenth witch (costume night) | Up to six on the beach; the next reader goes to another copy. | `TogetherSceneTests#costumeNight` |
| 36 | The flashes over water (movie night) | Up to four, one to each canoe; the next reader goes to another copy. | `TogetherSceneTests#movieNight` |
| 37 | The dare (winter lake) | Up to four, one to each boy at the fire; the next reader goes to another copy. | `TogetherSceneTests#winterLake` |
| 38 | The service road (camp blood) | Up to six, a counselor to each cabin; the next reader goes to another copy. | `TogetherSceneTests#campBlood` |
| 39 | The eight loose pages (devils rock) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#devilsRock` |
| 40 | The numbered doors (wheel) | One reader at a time: the next has a copy of their own. | `TogetherSceneTests#wheel` |
| 41 | The tape and the cut (ghosts set) | A lead's take, with their second cameras (four cameras in all); the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
| 42 | The visitors at dusk (end world cabin) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#endWorldCabin` |
| 43 | The house they remembered (family copy) | Each reader's own place, built for them. | each reader's own copy of their home (ArtifactTests and the family copy's own cases) |
| 44 | The last finger (three fingers) | One room for everyone; what each reader does and reads is their own. | `TogetherConductTests` (a talking scene) |
| 45 | The house in the third lake (lake house) | Two to a lake house: the second goes down in Tess's place; the next pair have another. | `TogetherSceneTests#lakeHouse` |
| 46 | The name beside mine (borrasca) | Each reader's own place, built for them. | `BorrascaTests` (two readers walk the trail and shelter before the taking) and the live two-client `BorrascaSocketProof` |
| 47 | The painted sun (painted interior) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#paintedInterior` |
| 48 | The second apron (second waitress) | One reader at a time: the next has a copy of their own. | `TogetherSceneTests#secondWaitress` |
| 49 | Under the green lights (tunnel periphery) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#tunnelPeriphery` |
| 50 | The empty mattress (missing sleep) | One reader at a time: the next has a copy of their own. | `TogetherSceneTests#missingSleep` |
| 51 | The cellar shift (layered man) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#layeredMan` |
| 52 | The unhinged door (motion light) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#motionLight` |
| 53 | Twenty seconds (backseat) | Each reader's own place, built for them. | `BackseatCarTests` and the live two-client `BackseatSocketProof` |
| 54 | Yellow dust (silo) | Each reader's own place, built for them. | `SiloTests` (only the readers who went in come back) |
| 55 | The third count (dare in the dark) | One reader at a time: the next has a copy of their own. | `TogetherSceneTests#dare` |
| 56 | Feet up (feet up) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#feetUp` |
| 57 | The north tower (whistlers) | A copy of their own for each reader. | `TogetherSceneTests#whistlers` |
| 58 | Work print (work print) | A copy of their own for each reader. | `TogetherSceneTests#workPrint` |
| 59 | The yellow balloons (polaroid trail) | A copy of their own for each reader. | `TogetherSceneTests#polaroidTrail` |
| 60 | The left-right game (left right) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#leftRight` |
| 61 | Dead air (dead air) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#deadAir` |
| 62 | The closing hall (closing hall) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#closingHall` |
| 63 | The storeroom (storeroom) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#storeroom` |
| 64 | The stillness (stillness) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#stillness` |
| 65 | The line (line) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#line` |
| 66 | Your own light (own light) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#ownLight` |
| 67 | Polybius (polybius) | Up to four, one to each seat at the cabinet; the next reader goes to another copy. | `TogetherSceneTests#polybius` |
| 68 | The replacement (replacement) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#replacement` |
| 69 | After the people lights (people lights) | A copy of their own for each reader. | `TogetherConductTests` (a talking scene) |
| 70 | Second person (second person) | A copy of their own for each reader. | `TogetherConductTests` (a talking scene) |
| 71 | A place for you (place for you) | A copy of their own for each reader. | `TogetherConductTests` (a talking scene) |
| 72 | Vacant possession (vacant possession) | A copy of their own for each reader. | `TogetherSceneTests#vacantPossession` |
| 73 | The last swimmer (last swimmer) | A copy of their own for each reader. | `TogetherSceneTests#lastSwimmer` |
| 74 | A deer in the road (deer road) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#deerRoad` |
| 75 | The median (state route) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#stateRoute` |
| 76 | The household (staircase) | Each reader's own, on the staircase. | `StaircaseLeakTests` |
| 77 | Growing things (growing things) | Up to four; the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
| 78 | The tall grass (tall grass) | Up to six; the next reader goes to another copy. | `TogetherSceneTests#tallGrass` |
| 79 | The supper (marsh supper) | Up to five at the table; the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
| 80 | The last crossing (last crossing) | One to six in the crew (0.4.598: a reader on their own crews it short-handed, with Vera); the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
| 81 | The weekend at Pinehook (pinehook) | One to six in the party (0.4.598: dealt to a reader on their own); the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
| 82 | The drawing (the drawing) | One room for everyone; what each reader does and reads is their own. | `TogetherConductTests` (a talking scene) |
| 83 | The house on the corner (corner house) | One room for everyone; what each reader does and reads is their own. | `TogetherConductTests` (a talking scene) |
| 84 | The city on the ice (black city) | One room for everyone; what each reader does and reads is their own. | `TogetherSceneTests#blackCity` |
| 85 | The kept (kept) | One room for everyone; what each reader does and reads is their own. | `TogetherConductTests` (a talking scene) |
| 86 | Last orders (last orders) | Up to five in the bar; the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
| 87 | Bike night (bike night) | Up to six, one to each bike; the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
| 88 | The Pell house (pell house) | Two to a house; the next pair have another. | `TogetherConductTests` (a talking scene) |

## What the audit found (0.4.565)

The first run of the 66 two-reader scene cases: 57 passed. Eight faults were in the game, and are fixed:

- **The cabin at the end of the world.** The four visitors spoke in chat, with only their own names over their heads, the name said
  again at each of their lines; the journal kept the name as what they had said. Their lines are captions now, with the name before
  them, and the journal keeps the words.
- **Whom a person turns to.** A person picked the nearest reader to turn to from every player about, including readers who could not
  see them: one reader's own visitors turned to another reader standing nearer, and away from their own. Now only readers the person
  is there for.
- **The closing hall.** A child lifted onto a reader's back arrived there at once from up to three blocks away; they climb up over a
  few ticks now. A child by the hand stood behind wherever the reader looked, and swung round whenever the reader looked about; they
  keep to the reader's body now.
- **Movie Night's canoe.** A teenager in the canoe, spoken to from behind, was held facing the bow. Someone in a boat is seated now,
  and turns their head as anyone seated does.
- **Second Person.** The resident could say "..." twice in three seconds while a reader stood in the way; once in twenty seconds now.
- **Movie Night's lines to the film.** They were said on their frame whoever was near: Lynn called to Bobby with both readers thirty
  blocks off. With nobody in earshot they go unsaid now.
- **Last orders.** Two readers along the bar each had "Go on. Steadies the hand." a second apart; a glass pushed while the last offer
  still hangs in the air gets a word of its own.

The other faults were the checks, written for one reader and a reader on foot, and are now written for what readers see: a part a
scene has several of (the Masque's dancers, the two parents, the painted family), a person who climbs out of something on purpose (the
woman on the deer road), somebody already there when a scene moves a reader (the Masque's red figure), a follower hurrying after a
test's reader who has just been put down beside someone or has turned round, the swimmer in her lane, a child carried on a back, the
Lady gliding out over Bly's pond, somebody who has said something new looking where that goes, and Camp Blood's man killing a reader
who walks up to him.

## Since the audit

What has changed in how readers are held, release by release, since 0.4.565:

- **0.4.582, the Pell house.** What its people do while the house waits, June's jobs, Tilly's question and the book were built for more than one reader and had cases with one; they now have cases with two readers in a house, a reader as Tilly, and a second house, each house keeping its own record (docs/PELL_HOUSE_NOTES_0_4_582.md).
- **0.4.596, the great staircase.** The people of a reader's own finished stories stand on the stair for that reader alone: another reader's game is never told they are there (docs/STAIR_COMPANY_0_4_596.md).
- **0.4.598, a reader on their own.** The Last Crossing and Pinehook, written for a crew, can be played by one (docs/ON_YOUR_OWN_0_4_598.md). Rows 80 and 81 above.
- **0.4.599, a failed story's door.** The door a reader took into a story that failed them is theirs: it opens onto a hall for them, and another reader's door to the same story is as it was (docs/TWO_NOTES_0_4_599.md).
- **0.4.600, the people who are in two places.** Johnny and Holloway visit the familiar kitchen (not a story, and not in the table above) and each also has a place of his own. Each is now in one place at a time: he does not come to the kitchen while a reader is with him at home, and gets up from the table if one finds him there (docs/CAST_MANNERS_0_4_600.md). No case has one reader in each place; the rule is two lines.
- **0.4.601, a ball and an arm in the Pell house.** Each pair's house has its own ball and its own Tilly. In one house two readers share the ball: Callum answers one throw at a time (a second reader's throw while he has the ball goes by), throws back to whoever threw it, and each reader's catches are their own. One reader has Tilly's arm at a time; another's hand on her is told so; each reader's walk, and what she was shown on it, is their own. With a reader as Tilly there is no arm to take (docs/PELL_HOUSE_NOTES_0_4_601.md). The arm's case has two readers; no case has two readers at catch.
- **0.4.602, the lodging house's people.** The seven are the house's: one of each for everybody in it, and each party's house has its own. What a reader is asked and answers is their own: Rae asks each reader for themselves, a promise binds only the one who made it, and Daniel has his name for a reader only once he has given it to them. A line is over the speaker's head for everybody near and in the asking reader's panel; a line said aloud is not said aloud again for twelve seconds, so a second reader who asks the same straight after has it in their panel only. Dan's hand, held out, is seen by everybody. The people's own case has two readers. The two-reader sweep does not have the lodging house, which the House does not deal yet.

- **0.4.604, the rooms and the bodies.** The chapter is the house's, one for everybody in it: Rae goes when every reader in the house has answered her, or after a minute, and Vera's lines are for all of them. Each reader is given a room of their own and a body of their own in its bed, which only that reader's standing beside it wakes and only that reader is answered at; everybody can see everybody's. The House's hair on a body is drawn on its own reader's screen only, as the House keeps it. A reader who comes in after the rooms are given out has one given them. Six rooms; a seventh reader has none until the plan's second house. The new case has two readers (docs/ONES_INSIDE_ROOMS_0_4_604.md).
- **0.4.605, Daniel through the wall.** Each reader reaches Daniel for themselves (in his room, at the hatch, at the door), and the exchange is their own as before. His hammering is the house's: one for everybody, set off by Dan's voice whoever Dan was speaking to, heard by every reader near enough, and once a minute however many readers there are. The hatch and the bolt are shared, as every door in the house is. The case has two readers (docs/ONES_INSIDE_DANIEL_0_4_605.md).
- **0.4.606, the injury.** The incident is the house's: one failed body, one glass, one cut, for everybody in it. It goes for the nearest reader downstairs, whoever that is; any reader's blows count toward keeping it off, and a grip is broken by two blows from anybody else or three from the one held. What each reader saw (Dan's cut, Daniel's, the pin) and did (struck it, kept it off, was held, was taken) is their own record. Nothing it does hurts a reader.
- **0.4.607, the two accounts.** Each account is each reader's own, once, in their own panel; what replies a reader has follows what that reader has heard and seen. The weapon is one reader's item, given from that reader's hand; Daniel holding it, and Dan having asked to be shown, are the house's, for everybody in it.
- **0.4.608, the key.** Where the key is, Rae's errand, Daniel's freedom and his walk are the house's, for everybody in it. Seeing Rae with it, hearing her confession, finding it and using it are each reader's own record; her turns check that the key is still hers before they act, so two readers asking at once cannot both send it somewhere. Custody when a holder leaves is slice 17's.
- **0.4.609, Vera's offer.** The offer, the cost and each answer are each reader's own, in their own panel; one reader's agreement binds nobody else. Vera holding the key is the house's.
- **0.4.610, the cellar.** The wedge and the body's stages are the house's, the same for everyone; who kicked, who saw it come apart and who knows the third way are each reader's own, by sight, not by being in the house.
- **0.4.611, the two men.** The meeting, what has been said aloud and the attempt are the house's; each telling is once for the house, each promise only its own reader's, and who brought Dan or stopped the attempt is each reader's own record.
- **0.4.612, taken.** The disturbance, who is in the wall, the latch and Vera's activity are the house's; who was taken, knocked, lifted or worked the latch, saw a body led, steadied or refused a body, and promised Vera are each reader's own. Only a body's own original can answer for it; another reader's hand on Vera gets nothing.
- **0.4.613, Daniel returned.** Dan's walk, who holds him, the panic and the frame are the house's; who put it to him, held him, stopped him, saw him go in and heard the dog's name are each reader's own. Only readers actually within reach count as holders; a reader elsewhere adds nothing.
- **0.4.614, Dan completed.** Vera's call, Daniel's walk and hold, the room and its end are the house's; who was called, took his shoulders, let go, saw it and heard the voice under Dan's are each reader's own. Only holders within reach count, and each holds by their own reply; a steadied body counts only because its original gave it.
- **0.4.615, the house severed.** The two counts, the bodies and the bench are the house's; who worked which, felt it go, struck the body and saw the bench are each reader's own. The partner bonus needs a second reader at the other place: one reader working both is slow, not refused. Only readers who know the third way can work either.
- **0.4.632, A Little Longer: personhood.** Each reader's lines come from their own read, in their own copy.
- **0.4.633, Second Person: spring.** The year is each reader's own copy of the street (SceneInstances); the agent, the deciding, the form and the answer are in that reader's record, and the agent is staged for that reader only.
- **0.4.634, Second Person: E2.** The signed last day is in that reader's own copy and record: Sam, the call, the reflection and the latch are staged and saved for that reader only.
- **0.4.635, Second Person: E3.** Frightened out is read from that reader's own record; the night they leave is staged in that reader's copy only.
- **0.4.636, Second Person: E4.** Known gently is read from that reader's own record; the cups, the talk and the question are staged in that reader's copy only, and the answer is that reader's word.
- **0.4.637, Second Person: aftermath.** Whether a visit is the aftermath is read from that reader's own record; the flat is that reader's own copy, so the aftermath is staged for them alone, and the dust is sent to them only.
- **0.4.641, A sort of thing: the place.** Not dealt yet. The King is one body per copy of the place (found before he is made, so never twice); the glass moves only the reader who touches it. Whether the country is shared or a copy each is part 3's decision.
- **0.4.645, A sort of thing: the mushroom and the drawn bridge.** The Caterpillar is one per copy of the country and talks to each reader on their own panel, from that reader's own state; the mushroom gives to everybody, as often as they come; whether a reader went through the drawn bridge or crossed it is their own record, written from their own tick.
- **0.4.647, A sort of thing: the forest, the Knight and the song.** The Knight, his falls, the turn and the song's settling are the dream's, one per copy of the country: any reader looking keeps him filled, two readers can keep him by turns, and only readers within twenty-four blocks when he asks are asked. Seeing him fall, getting him up, hearing and singing the song, the cake, the forest peg and the turn's outcome are each reader's own record, written from their own tick. A reader alone needs nobody else: he is the one who looks.
- **0.4.652, the staircase by judgement.** The lamps and newels are the same for every reader. Each reader's lean is their own view; other readers see them standing at the rail, and a light they let go is watched by anyone near, as before. Tom's line from the well is said once to each reader, kept in their own record.
- **0.4.653, the Minotaur half again.** Every reader's creature is the same size, and the gate, doorways, causeway and the opening in the stair's rail are the same for every reader. Each reader's fight, walk out and roaming creature stay their own, as before; a roaming one on the stair is broader, and leaves more than six blocks of the stair to pass it by.
- **0.4.654, someone on the stair: the first five.** Each reader's people stay their own, set down for that reader alone and seen by nobody else, as since 0.4.596. A figure dressed by its story is dressed from that reader's own record: two readers whose lodging house ended differently see Dan and Daniel on the same stair, and each sees the Rowan they were given. Choosing reads the reader's Witness record and a copy of their part of the story, and writes nothing.
- **0.4.655, someone on the stair: the stories' own creatures.** Each reader's people stay their own, as since 0.4.596. The creature a figure stands in for is made in that reader's own game and never added to the world, so no one else sees, hears or bumps into it, and nothing ticks it. The cousin is dressed from that reader's own Goatman record, so two readers who sat different nights see different faces on the same stair; choosing reads the record and writes nothing. The elk is made as that reader's own, so its eyes are lit for its reader.
- **0.4.656, quick fixes.** A reader past either of the King's side hedges is put back, or leaves him his sleep, by their own record, as past the far hedge; another reader's walk does nothing for them. The models, the elk's eyes and the girl's sheet are the same for every reader, and nothing is kept per reader or sent between them.
- **0.4.657, the staircase's stone.** Textures, models and block states only: the block state picks each stone by its place, so every reader sees the same stone in the same place, and nothing is kept per reader or sent between them.
- **0.4.658, where you turned back.** The cave is one for everyone, so the House's torch is a real torch that anyone there sees, and it is set only while nobody there can see the place. How far each reader came, and where they turned back, is their own record; another reader walking out does nothing for them.
- **0.4.659, the second chair.** The kitchen is one for everyone, so its table is one table: whichever reader drew a chair up last, the place is laid for that chair and every reader sees the same table. It is laid and cleared only while nobody at all is in the kitchen or can see the table. Which chair each reader drew up is their own record; another reader pulling a chair does not undo it, but the latest chair is the one laid for.
- **0.4.660, the foot of the stair.** Each reader's own: the score dying away is drawn by each reader's game, the descent's growls leave out each reader who is at the foot, and the growl from the cell is sent to one reader alone, after their own five seconds there, once ever; another reader standing there waits out their own five seconds and hears their own. The lanterns at the gate are the same for everyone.
- **0.4.661, heard apart.** For readers together only. A pair is two living readers in one hall; each is sent their own sound alone (footsteps to one, a door to the other) and nothing is played in the world, so a third reader there, or a spectator, hears neither. Spectators are never one of a pair. When each was last one of a pair is their own record; a reader who leaves hears nothing more of what was still to come.
- **0.4.651, faces that draw nothing.** Models and pictures only: every reader sees the same faces, and nothing is kept per reader or sent between them.
- **0.4.650, every face on its own picture.** Models only: every reader sees the same faces, and nothing is kept per reader or sent between them.
- **0.4.649, A sort of thing: afterwards.** The country come back to is one per copy of the country and decided by everybody in it: it stands as the country after he woke or the one he sleeps on in only when every reader there has had an ending, a waking wins over a sleep, and one reader with no ending makes it a fresh dream again for everybody, with its people back. Whether it is dealt back to a reader, and each return counted, is that reader's own record. Each crown is its wearer's: it fills for any other reader's look (or one of the House's people's that this reader can see), its sound and cues go to the wearer alone, and its state is kept on the stack so every viewer sees the same crown.
- **0.4.648, A sort of thing: the feast, the crown and check.** The feast and what has been introduced are the dream's, one per copy of the country: what one reader introduces is filled in for everybody there, and a guest looks at the nearest reader. Being crowned, the crown itself (bound to its wearer; another reader's crown on a head is only a crown), the lines (drawn only for the wearer), check, the crown coming off, the last song and the ending are each reader's own, written from their own tick. Two readers can both be crowned and both be in check; if the stirs wake him, each one in check stood there until he woke and everybody else was there when he woke. A reader alone has the guests to look at them on the peg. The dream advances on every server tick, whoever's tick comes first (KingBreath.tickAll).
- **0.4.646, A sort of thing: the court of cards and the Cat.** Being named, held, sentenced and which way a reader left are each reader's own record, written from their own tick. The Cat over the court and the pack's fall are the dream's, shared by everybody in that copy of the country: one reader calling the Cat frees every held reader while it is there, and one reader's saying what the court is brings the pack down for all, later readers included. A reader alone has the Cat for the court to look at instead.
- **0.4.644, A sort of thing: the hill, the table and the railway.** The Queen and the Guard are one each per copy of the country and take one reader at a time; the others wait their turn. Her run, each peg counted, the table's bottle and cake and the ride are each reader's own record, written from that reader's own tick. The bottle and cake are tagged to their taker and leave with them.
- **0.4.643, A sort of thing: the brothers and his sleep.** His sleep, the battle and the rattle are one per copy of the country, so every reader there hears the same stirs and sees the same land; what the brothers said, the rattle lifted, the battle seen and the ending are each reader's own record, and the waking's end is settled from each reader's own tick so no reader's tick saves over another's. Whoever says his name is the namer; everybody else in the country at the waking is there_when_he_woke; a too-soon waking fails everybody in the country and nobody outside it.
- **0.4.642, A sort of thing: his breath.** The breath is one clock per copy of the country, counted once a tick however many readers are there. Each reader's state is their own, kept server-side by UUID and sent to every reader in the country so each sees the others flat or in outline; hedges and the verge ask only the colliding reader's own state. Being looked at needs another reader in the same copy who is at least a sort of thing.
- **0.4.640, Second Person: who comes for supper.** The guest comes from that reader's own snapshot, in their own copy of the flat.
- **0.4.639, Second Person: why they are here.** Read from that reader's own snapshot; the chair is on their own copy's landing only.
- **0.4.638, Second Person: the other homes.** The voices come from that reader's own snapshot of home; each reader's flat is their own copy, so one reader's caller is never heard in another's.
- **0.4.631, A Little Longer: leaving early and coming back.** The question, the reunion and the early going are each reader's own, in their own copy, from their own record.
- **0.4.630, A Little Longer: the room afterward.** Every reader's evening is their own copy, so the room afterward, its radio, the cups and the list are each reader's alone; the account, its outcome and the resolution come only from that reader's record.
- **0.4.621, A Little Longer: the place.** Not dealt yet. The plan makes every reader's evening their own copy (one private instance each); the rain and the quiet are client-side, each reader's own.
- **0.4.620, aftermath.** The ended house is the party's and only its members come back to it; each reader's return and its count are their own. A finished reader walking through it changes nothing for anyone still in the story there.
- **0.4.619, personhood fragments.** Every fragment is the reader's own, from their own snapshot, delivered only to them; nothing in the shared house changes, so a party never sees one reader's read. Nothing a mechanism needs depends on it.
- **0.4.618, accounts and the Witness.** The end and who was in the house before it are the house's; each account, its outcome and its resolution are the reader's own, written only from that reader's record. A reader who came in after the end has no account, and an ended house takes nobody new.
- **0.4.617, party changes.** The key's place and the list of who has come in are the house's; whether a reader came in late and how is their own. The key follows its holder only while that holder is in the house with it; leaving, logging off, dying or dropping it puts it back where it lives, so no reader can carry it out or keep a copy. The first reader in is never late.
- **0.4.616, neither Daniel.** The blows, the deaths, Vera's table and Rae's chair are the house's; who struck, saw them die and where, saw the places, told Rae, stopped Vera and broke the support are each reader's own. Only blows from readers who know count, so one who does not cannot be drawn into it.
## Decided (any of it can move)

- The hotel stays one building, made for any number of guests (0.4.563).
- The Sphinx's halls are the House's own, and stay shared.
