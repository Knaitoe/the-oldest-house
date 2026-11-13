# The culvert, Tess, and the cast's manners (0.4.560)

Three of the owner's notes from 13 November, one after another:

- "The canoe tunnel pass needs improvement too."
- "also ensure the npc behaves correctly in that vignette, please - tess"
- "You should standardize your tests with Tess and run them against the dialogue heavy, branching vignettes too. Many of those NPCs
  also did not behave properly."

New builds only. No layout, protocol or Witness change.

## The culvert under the drive

The lake house's canoe pass (LakeHouse, LakeHouseCanoe) was a plain concrete tube with a hand-push corridor that began only under the
roof, a canoe that could miss the gap at either mouth, a back bench that could do nothing, and one scrape sound for every stroke.

What it is now:

- **Cast in sections.** The tube's walls and ceiling meet in joints every eight blocks under the drive (`CulvertBlock.JOINT`),
  crusted white where water from the drive comes through. Ceiling joints drip into the channel, now and then, with a drip you hear.
- **Headwalls.** Each mouth has a concrete headwall from x -4 to 5, coped along its top, with dumped stone on the banks either side
  of the channel. The brush that half hides the mouths stays off the headwalls.
- **Painted marks.** Sixteen marks (`CulvertMarkBlock`, `LakeHouse.MARKS`) at the height of a reader sitting in a canoe, on the face
  turned to the water: arrows pointing on toward the third lake, a tally, hands where hands push off, the house under its waterline
  across two blocks, a helmet, a half door, a spiral, bubbles. No names and no words. Drawn by tools/draw_lake_house_exterior_art.py.
- **Hands wherever the walls are in reach.** The paddles give way to the readers' hands wherever the culvert's own concrete is in
  reach on both sides (`LakeHouseCanoe.pushable`), out along the open troughs to either lake as well as under the roof. Only the
  culvert's concrete counts, so no other narrow place takes the paddles away. The prompt says which it is: too low to paddle, or too
  narrow.
- **Into the gap.** Coming up to either mouth with the bow toward it, within eight blocks and three either side, the canoe is eased
  toward the middle of the gap and turned to the channel (`easeIn`), so it finds the two-wide way between the walls rather than the
  end of one. Nothing pulls a canoe that is going past or away.
- **Both benches push.** Whoever sits in the back pushes too, with their own hands and their own timing (`BACK_SIDE`, `BACK_TICKS`).
  Their strokes are counted on the canoe and taken by the game that moves it (`takeBackStrokes`), so the canoe is still steered from
  the front. A one-armed reader pushes with the hand they have, front or back.
- **Sound.** A stroke is a wet palm on concrete, three original takes with the tube's short answer in them (`lake_house.hand_push`,
  tools/generate_culvert_audio.py, subtitled "Hand pushes off concrete"). The hull scrapes only where it touches a wall, at most once
  a second.
- **Ducking.** Tess (or the young man in her place) in the back of the canoe ducks under the roof as the reader does
  (`CanoeRiderPose.duck`), rather than putting her head through it, and sits up after it.

## Tess

Watched against the standard checks below, Tess did most things a person does not: she appeared on the water in front of the reader,
stood on the water two blocks off the dock, swam through the air to get to it, jumped across the lake to climb into the canoe, moved
in five-tick steps, and on the helmet visit left the compressor to follow the reader.

Now (LakeHouseTess, LakeHouseSwimNavigation):

- **She is never made where the reader can see.** She is already on the dock (or on the raft by the compressor on the helmet visit)
  when they come, or comes out of the boathouse behind them. While every way in is in their sight she waits.
- **She waits on the dock's planks**, by the canoes, not on the water.
- **She moves a tick at a time.** The scene still decides where she goes every five ticks; she covers it a fifth at a time, every
  tick, so she walks and swims rather than jumping along in steps.
- **Ground or water, never neither.** She walks where there is ground and swims where there is water. On foot in the boathouse or on
  the dock with somewhere to be out on the water, she goes out through the lake door and down the middle of the boards, and steps down
  into the water only from the end (`landward`). Getting out of the canoe she steps onto what is beside it, or over its side.
- **Into the canoe from beside it.** She goes to the reader's canoe first, along the dock or swimming, and climbs in only from within
  reach of it; never from across the lake.
- **The helmet visit.** She keeps the compressor going and stays on the raft.
- **She turns to her reader** when she is not going anywhere, her head and, if they are behind her and she is standing, herself;
  quickly while she is speaking.

## The cast's manners: the standard checks

`gametest.CastConduct` is one set of checks every person in a scene is held to, on every tick, while a case plays the scene through.
It was written for Tess first and then, at the owner's word, run against the talking scenes.

| Check | What fails it |
|---|---|
| No jumps | moving more than 2.5 blocks in a tick where a reader can see it, or landing beside a reader that way (a move nobody sees, off by itself, is how a scene puts people away, and is allowed) |
| No steps | covering a good way over a second on fewer than eight of its twenty ticks |
| Nothing through a block | standing inside a whole block for three ticks (a chair, bench or bed is furniture to sit in and does not count); a rider's head through what is over them |
| Ground, water or a seat | standing on nothing, standing in deep water, swimming in the air |
| Present and in earshot | a line said by somebody gone, or more than 24 blocks from the nearest reader |
| Not the same line again | the same line from the same person within 200 ticks, unless it is their answer to a reader who has just spoken to them |
| Turned to the reader | a speaker whose head is more than 75 degrees off the reader they spoke to, 20 ticks after the line; a seated person is held to as far as a head turns |
| One body | two of the same person in a scene at once |
| Not made in sight | somebody coming into being where a reader can see them, within 24 blocks |

A scene can name what its own people do on purpose (`swimming`, `floating`, `turnsAway`, `exempt`). Every fault goes to the log; the
case's failure names the kinds and the first fault, since a long failure message cannot be written into the test's book.

`gametest.CastConductTests` (namespace `the_oldest_house_cast`, one batch each) builds a talking scene, brings a reader in through its
own arrival, and has them go round everyone there for half a minute (`TalkBot`): up to each person in turn, to a spot in plain sight of
them, looking at them, speaking to them (nobody twice within five seconds), and answering whatever the conversation panel asks with a
different answer each time, so its branches are walked. Sixteen scenes: A Place for You, Second Person, Speak Up, the Pell house,
Pinehook, the Last Crossing, the Supper, Growing Things, Last orders, the House on the Corner, the Kept, the Drawing, the People
Lights, Bike night, Three Fingers and Harrigan. The Tall Grass's people are voices until the car at its end, the familiar kitchen's
visitors come with time, and Johnny keeps his own hours between his places; their own cases watch them.

The lake house case `tessKeepsTheCastChecksFromTheDockDownToTheHouse` holds Tess to the same checks from the dock, through the canoe
and the raft, down to the house.

## Turned to whoever they are talking to

The checks' first sweep found it in most of the talking scenes: people said their lines facing a table, a wall or nowhere, with the
reader behind them. It is now one behaviour for the whole cast (LiteraryActor, CastAddress), rather than each scene's to remember:

- Whoever says a line turns to the reader they say it to and keeps looking at them while the line is up: the reader who has just spoken
  to them, or else the nearest reader within twelve blocks who can see and hear them.
- A standing person turns their head and their body comes round after it, as anyone's does. A seated one turns only their head, up to
  80 degrees from their seat (`HEAD_TURN`), and stays in the chair; the server now knows who is seated (`ActorSeat`, moved from the
  client's renderer), and the renderer turns a seated head from the chair rather than from where the body last drifted.
- It is the last word on their heads each tick, after every scene has turned its people, so a scene that sets a head every tick does
  not undo it while a line is up.
- Nobody turns who cannot see the reader: Second Person's resident, who only hears them; the dead, the sleeping, the hidden.

## What testing found

- **Four scenes the checks could not build.** Harrigan, Speak Up, the Truant's places and the familiar kitchen are not literary rooms,
  and the first cases threw before a tick was watched. Harrigan and Speak Up are now built and entered through their own builders and
  arrivals; the Truant and the kitchen are left to their own cases (above).
- **Furniture counted as walls.** A seated person is drawn from a little below the seat, and the first runs said everyone in a chair
  stood inside a block. The check now leaves out the lowest third of a body and anything less than a whole block.
- **Heads left on the table.** The first full sweep found the turn fault in most of the scenes (the Pell house, Pinehook, the Supper,
  Growing Things, Last orders, the Kept, the Drawing, the People Lights, Bike night, Three Fingers): lines said to a table, a wall or
  the dark. That is now the one cast-wide behaviour above. A scene that turned a body after the head had been turned left heads more
  than a head's turn from their own bodies; the head is held within a head's turn of the body when it is set at the end of the tick.
- **Made in sight, and jumps into chairs.** The Drawing made its villagers on their spots in front of the reader; Second Person, A Place
  for You and Last orders put people into chairs with a jump of a block or two. The Drawing now makes them only where nobody can see,
  and the other three sit down with a short slide.
- **Tess** did everything listed under her name above in the first lake house case.
- **The test's own reader.** The case's reader is put beside each person at once rather than walking there, which no reader does. The
  first calibrated runs counted a head that had not yet caught up with a reader put somewhere else that tick, and a move from a spot the
  reader had been looking at a moment before. A move is now judged by what the readers can see when it happens; a turn is checked again
  ten ticks later when the reader has just been moved; and the test reader stays to listen once answered, as a reader would. Cases also
  wait until the ground the scene stands on is ticking its people: in one lake house run Tess said her greeting and then had not had a
  single tick of her own twenty ticks later, because the test began before the world had promoted her chunk.
- **A seated head drawn from the wrong place.** Where a scene seats somebody at an angle to their chair, the renderer turned the head
  from the body as if the two were square; it now turns it from the chair.

Local runs: the lake house and cast namespaces together, 36 of 36 (the sixteen cast scenes, both Tess cases, the two culvert cases
and every earlier lake house case), with no fault from the checks in any scene.

The same commit fixes run 1594's live expedition, which timed out at phase 33: since 0.4.558 the second reader into the Goatman trailer
begins at the camp, and the client walking the other reader up the trail stopped them four blocks short of the clearing, so they never
arrived. Only the reader in the cousin's place waits now; the one on the trail walks on into the clearing.
