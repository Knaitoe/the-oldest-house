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
| 80 | The last crossing (last crossing) | Up to six in the crew; the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
| 81 | The weekend at Pinehook (pinehook) | Up to six in the party; the next reader goes to another copy. | `TogetherConductTests` (a talking scene) |
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

## Decided (any of it can move)

- The hotel stays one building, made for any number of guests (0.4.563).
- The Sphinx's halls are the House's own, and stay shared.
