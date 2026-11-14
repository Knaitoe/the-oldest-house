# What each door lets out (0.4.576)

From the owner's notes on 13 November: "let's also give each vignette's door more unique hallway bleeds/sound effects".

New builds only. Protocol 191: a client is told which room's bleed a door has, and a 0.4.575 client could not read it. No layout
or Witness change.

## What was there

A shut door to a story lets something out into the hall, for the reader it is dealt to: something on the floor at the door, and
a sound every four seconds once they are within six blocks. It stops when the door is opened and when the story is finished.

There were thirteen kinds of it between ninety-four stories.

- **Eighteen stories had a kind chosen for them,** and several shared one. The red room, the model home and Karen's room all
  had the same warm light and the same television. Zampanò's flat had the Mother's fur and her purr.
- **The other seventy-six borrowed one of the thirteen by chance.** The Pell house could leak lake water, and the pool a
  heartbeat.

## What a door lets out now

Every story has its own, in four parts. A room may have fewer than four, and no two rooms look alike at the door or sound alike.

- **On the floor.** One of three things.
  - Its own picture: sixty of them, sixteen pixels to the block, a block wide and one or two blocks out from the door, the
    first row against the door. Somebody standing in the hall facing the door reads it the right way round.
  - Water in the room's own colour: ten rooms. It is the water that was there before, which was one colour for all of them.
  - One of the four strips that were there before, unchanged: the heart's floorboards, the blindfold's cloth, the Mother's
    fur, the hotel's carpet.
- **The light under the door.** Forty-nine rooms have one, in the room's colour, and it behaves as that room's light does:
  a candle, a television, a projector, a fan turning under a lamp, headlights going by. There are twenty-two behaviours
  (below).
- **In the air.** Twenty-four rooms let something drift out at the foot of the door: drips, snow, ash, dust, steam, sand or
  flies. It shows within eight blocks.
- **The sound.** One of its own for every room: eighty-three new ones and the twelve that were there. Where the room has
  sounds of its own, those are what is heard, put behind wood: the top of them gone, a few seconds long. Where it has none,
  the sound was made for it. Every new sound has a subtitle.

A door that leads to a finished story, or to a room that is waiting on something else, still lets nothing out.

## Every door

"As before" marks what is unchanged from 0.4.575. Light colours are named roughly.

| Room | On the floor | Light | In the air | Heard |
| --- | --- | --- | --- | --- |
| `FLOORBOARDS` | Floorboards (as before) | — | — | A heart beats beneath the floor |
| `RED_ROOM` | — | Red, swelling and fading | — | A low note swells behind a door |
| `HIDE_AND_CLAP` | White cloth (as before) | — | — | A clap, muffled |
| `MODEL_HOME` | — | Pale blue, a screen changing shots | — | Television murmurs |
| `HARRIGAN` | — | Amber, steady | — | Phone rings behind a door |
| `MOTHER_DEN` | Grey fur (as before) | — | — | Cat purrs (vanilla) |
| `FLOODED_PASSAGE` | Water (as before) | — | drips | Water flows (vanilla) |
| `DROWNED_TOWN` | Lake water | — | drips | Lake water laps behind a door |
| `PRESERVED_CAVE` | Cave water, pale | — | drips | A voice beneath the water |
| `SHALLOWS` | Reed-bed water | — | drips | Water against the reeds |
| `PHONE_CANOE` | Water (as before) | Pale blue, swelling and fading | drips | Water against a wooden hull |
| `GOATMAN` | Dead leaves and twigs | — | — | Birds in the woods |
| `TED_CAVER` | Rock dust and broken stone | — | dust | A chisel taps rock behind a door |
| `ZAMPANO_COURTYARD` | Handwritten pages | — | — | A pen scratches behind a door |
| `WHALE` | Letters, one still half under the door | — | — | Clocks tick out of step behind a door |
| `BARN_WELL` | Straw | — | — | Slow breathing behind a door |
| `PLAIN` | Blown sand, rippled (two blocks) | — | sand | Dry wind in grass behind a door |
| `HOSPITAL` | — | White, a tube that stutters | — | A monitor beeps behind a door |
| `KAREN_ROOM` | — | Warm white, a projector's flutter | — | A projector runs behind a door |
| `HOLLOWAY_CAMP` | Ash, charcoal and two embers | Orange, firelight | ash | A fire crackles behind a door |
| `SEANCE` | — | Amber, candlelight | — | A chair scrapes, and a low voice, behind a door |
| `WALLPAPER_NURSERY` | A torn strip of wallpaper, curling over (two blocks) | — | — | Paper tears in strips behind a door |
| `HOTEL` | The hotel's carpet (as before) | — | — | Music behind a door |
| `HILL_NURSERY` | Frost, feathering out (two blocks) | — | snow | Something pounds a wall behind a door |
| `MINIATURES` | Sawdust, shavings, and a chair and table a hand high | — | — | A small saw works behind a door |
| `MASQUE` | — | Seven colours in turn | — | Dance music stops for a clock behind a door |
| `USHER` | A crack running out across the floor (two blocks) | — | — | Thunder rolls behind a door |
| `WINCHESTER` | Sawdust, shavings and a bent nail | — | dust | A hammer and a saw behind a door |
| `CHILD_ROOM` | Alphabet blocks, a marble, a crayon | Warm white, steady | — | A music box plays behind a door |
| `CRIMSON_HALL` | Snow, with clay coming up through it (two blocks) | — | snow | A recording plays behind a door |
| `BLY_ROUTE` | Wet footprints, and what ran off her at the door (two blocks) | — | drips | Wet footsteps pass behind a door |
| `ELK_LOT` | — | Pink and blue, two neon tubes | — | Pool balls clack and a truck passes, behind a door |
| `ELK_FAN` | — | Warm white, a fan's blades across it | — | A ceiling fan turns behind a door |
| `MAPPING_INTERIOR` | Beads and a feather | — | — | Bells on a dancer's steps behind a door |
| `HOLY_RABBIT` | Snow with tracks in it (two blocks) | — | snow | Wind over snow behind a door |
| `CONFESSION` | — | Amber, candlelight through a lattice | — | One voice, low and steady, behind a door |
| `ELK_CARCASSES` | Churned mud and hoof prints (two blocks) | — | flies | Flies behind a door |
| `COSTUME_NIGHT` | Sweet wrappers and an entrant's number | A string of bulbs | — | A megaphone squeals and a crowd claps, behind a door |
| `MOVIE_NIGHT` | Spilled popcorn | Pale blue and warm white, a projector's flutter | — | Film music behind a door |
| `WINTER_LAKE` | Ice run in under the door, and snow (two blocks) | — | snow | Ice groans behind a door |
| `CAMP_BLOOD` | Pine needles and a craft lanyard | — | — | Crickets, and a screen door, behind a door |
| `DEVILS_ROCK` | Diary pages, torn off their wire | — | — | Pages turn behind a door |
| `WHEEL` | Brass room numbers and the screws that held them | — | — | Doors open one after another behind a door |
| `GHOSTS_SET` | A tape mark for an actor's feet, and a cable | White, steady | — | A bell, then a clapperboard, behind a door |
| `END_WORLD_CABIN` | A jar lid punched for air, grasshoppers, grass | — | — | Rain on a roof, and knocking, behind a door |
| `FAMILY_COPY` | A doormat, pushed half through | Warm white, steady | — | A handle turns and is let go behind a door |
| `OLD_CABIN` | — | Warm white, keyed in Morse | — | A chair rocks behind a door |
| `THREE_FINGERS` | Mud and a boot print | Amber, firelight | — | A fire spits and something scratches, behind a door |
| `NIGHTCLUB` | — | Pink and blue, pulsing on a beat | — | A bass beat thuds behind a door |
| `LAKE_HOUSE` | Dark lake water | Pale green, light off moving water | drips | A compressor chugs behind a door |
| `BORRASCA` | Gravel and pine twigs | — | — | Metal grinds, far off, behind a door |
| `PAINTED_INTERIOR` | Flakes of paint | — | — | Boards creak under a weight behind a door |
| `SECOND_WAITRESS` | Black frost, feathering out (two blocks) | Pale blue, steady | — | Plates are stacked behind a door |
| `TUNNEL_PERIPHERY` | — | Amber, a tube that stutters | — | Traffic rumbles behind a door |
| `MISSING_SLEEP` | — | Grey, a screen changing shots | — | A monitor clicks from feed to feed behind a door |
| `LAYERED_MAN` | Buttons and a length of thread | — | — | Something wet tears behind a door |
| `MOTION_LIGHT` | — | White, on when somebody is near | — | A buzzer rasps behind a door |
| `BACKSEAT` | Rainwater | Warm white, a band of light going across | drips | An engine idles and wipers drag, behind a door |
| `SILO` | Spilled corn (two blocks) | — | dust | Grain shifts behind a door |
| `DARE_IN_THE_DARK` | Water off a bathroom floor | Amber, candlelight | — | Giggling, and a tap dripping, behind a door |
| `WAY_OUT` | Grass and a daisy | Warm white, steady | — | Birdsong catches and repeats behind a door |
| `FEET_UP` | — | Orange, firelight | — | Someone counts behind a door |
| `WHISTLERS` | Drifted snow and pine needles (two blocks) | — | snow | Something whistles in pines behind a door |
| `WORK_PRINT` | Tape off its spool (two blocks) | Blue, a screen changing shots | — | A tape goes in and a deck plays, behind a door |
| `POLAROID_TRAIL` | Four photographs | White, a flash now and then | — | A flash charges and a shutter clicks, behind a door |
| `LEFT_RIGHT` | A line of salt across the sill, a foot through it | — | — | A CB radio crackles behind a door |
| `DEAD_AIR` | — | A test card's bars | — | A flat tone holds behind a door |
| `PITT` | Dirt, dragged by fingers | — | — | A draft moves through stone behind a door |
| `CLOSING_HALL` | Plaster dust and chips | — | dust | Walls step nearer behind a door |
| `STOREROOM` | Ink, still coming | Warm white, a lamp swinging | — | A bulb buzzes and shelving drags, behind a door |
| `STILLNESS` | — | — | — | Snuffling at a floor behind a door (every 12 seconds) |
| `LINE` | A line running out along the floor, frayed (two blocks) | — | — | A line creaks and snaps taut behind a door |
| `OWN_LIGHT` | — | Warm white, a lamp swinging | — | Two sets of footsteps, in step, behind a door |
| `POLYBIUS` | A strip of prize tickets (two blocks) | Green and pink, on and off | — | Arcade machines chirp behind a door |
| `REPLACEMENT` | Paw prints in mud, fading (two blocks) | — | — | Claws click along behind a door |
| `LAST_SWIMMER` | Pool water | Pale blue, light off moving water | — | Someone swims a length behind a door |
| `VACANT_POSSESSION` | Packing paper, a curl of tape, bubble wrap | — | — | Packing tape rips off a roll behind a door |
| `PEOPLE_LIGHTS` | — | Warm white, four windows going dark in turn | — | Wheels roll over boards behind a door |
| `PLACE_FOR_YOU` | — | Warm white, steady | steam | A kettle comes to the boil behind a door |
| `SECOND_PERSON` | Dryer lint and a sock | — | steam | Dryers tumble behind a door |
| `DEER_ROAD` | Broken lamp glass | Amber, on and off | — | Hazard lights tick behind a door |
| `STATE_ROUTE` | Road grit and a cigarette end | Warm white, a band of light going across | — | Cars rush past behind a door |
| `MARSH_SUPPER` | Corn husks and kernels | Amber, steady | — | A dinner bell rings behind a door |
| `TALL_GRASS` | Grass, reaching out (two blocks) | — | — | Someone calls from tall grass behind a door |
| `GROWING_THINGS` | Shoots, growing out along the floor (two blocks) | — | — | Something rustles in shoots behind a door |
| `LAST_CROSSING` | River water, silted | — | drips | A pump labours and a winch ratchets, behind a door |
| `PINEHOOK` | Pine needles and bottle caps | Warm white, steady | — | Bottles clink and someone laughs, behind a door |
| `THE_DRAWING` | Slips of paper, one of them marked | Warm white, steady | — | A crowd murmurs and paper rustles, behind a door |
| `CORNER_HOUSE` | Dry curled leaves and dust | White, steady | — | Cicadas, and an iron gate, behind a door |
| `BLACK_CITY` | Snow, and the grit the wind takes off the walls (two blocks) | — | snow | Something pipes, far off, behind a door |
| `KEPT` | Punched tape and what was punched out of it | Orange, pulsing on a beat | — | Machines hum and a screen wakes, behind a door |
| `LAST_ORDERS` | Sawdust, a beer mat, a cigarette end | Amber, steady | — | Darts thud into a board behind a door |
| `BIKE_NIGHT` | Leaves and a card off somebody's spokes | Amber, candlelight | — | A card rattles in spokes behind a door |
| `PELL_HOUSE` | June's painted line, a quarter of it scrubbed at | Warm white, steady | — | A hairbrush, slowly, behind a door |
| `ONES_INSIDE` | Plaster, lath and the hair that bound it | Warm white, candlelight | — | Something moves inside a wall, behind a door |

## The lights

Every light that moves is scaled by Distortion Effects in the accessibility options, with no floor. At nothing, each is a
steady light. At full, no light loses more than a sixth of its brightness and gets it back more than three times in a second.
The quick ones are slight: a projector's flutter, ten times a second, is a seventh of the light, and a neon tube's hum, six
times a second, is a sixth. A light is thin over the floor: about a third at the row nearest the door and less further out,
and a little over half for the instant of the Polaroid's flash.

| Behaviour | At full | At nothing | Rooms |
| --- | --- | --- | --- |
| Steady | It does not move | The same | Harrigan, the child's room, the Ghosts set, the family copy, the second waitress, the way out, a place for you, the Supper, Pinehook, the Drawing, the corner house, last orders, the Pell house |
| Swelling and fading | Up and down over six seconds | Held at its middle | The red room, the phone in the canoe |
| Firelight | A fire's unsteadiness, never out | Held at its middle | Holloway's camp, Three Fingers, Feet Up |
| Candlelight | A smaller unsteadiness | Held at its middle | The séance, the dare, bike night, the ones inside |
| A tube that stutters | Full; every seven seconds it stutters for three fifths of a second | Held, a little under full | The hospital, the tunnel |
| A screen changing shots | A new brightness every two fifths of a second | Held at its middle | The model home, the missing sleep, the work print |
| A projector's flutter | A slight flutter ten times a second; with two colours, the tint changes with the shot | Held, the two colours mixed | Karen's room, movie night |
| Pulsing on a beat | A pulse twice a second, falling away; with two colours, one and then the other | Held at its middle, the colours mixed | The nightclub; the Kept, at half the pace |
| On and off | Half a second on and half off; with two colours, one and then the other | Held at its middle, or the colours mixed | A deer in the road, Polybius |
| A flash now and then | One flash every seven seconds, gone in a third of a second | A faint steady light | The Polaroid trail |
| A band of light going across | A band crosses from left to right and is gone | A faint steady light | The backseat; State Route 9, more often |
| A lamp swinging | A bright place moving from side to side every three seconds | The bright place in the middle | The storeroom, your own light |
| A fan's blades across it | Shadows crossing about once a second | Held, a little under full | The elk and the fan |
| Seven colours in turn | The suite's seven rooms, four seconds each | The first room's colour | The masque |
| Four windows going dark in turn | Four lit places go out one at a time, and all come back | All four lit, held | The people lights |
| Keyed in Morse | A word, a unit to three tenths of a second | Held at its middle | The old cabin |
| Candlelight through a lattice | A candle behind a screen: every other square is dim | The lattice, held | The confession |
| Light off moving water | Ripples | Held at its middle | The lake house, the last swimmer |
| A test card's bars | Eight bars of colour; it does not move | The same | Dead air |
| On when somebody is near | It comes on when anybody is within four and a half blocks of the door, and stays on three and a half seconds after they have gone | Held at half | The motion light |
| Two neon tubes | Two colours side by side, humming; the second drops out for an instant every five seconds | Held | The Last Stop |
| A string of bulbs | Four colours across the door, each a little unsteady | Held | Costume night |

## What became of the thirteen

- **The heartbeat, the cloth, the Mother's fur and purr, the hotel's carpet and band** are as they were. The Mother of
  Strays is not changed. The hotel itself now has the carpet and the band; before, only its numbered corridor did, and the
  hotel borrowed a kind by chance.
- **The warm light and the television** were three rooms'. The model home keeps the television, in a screen's own light. The
  red room has a light of its own and a low note. Karen's room has a projector's light and a projector.
- **The telephone** was three rooms'. Harrigan's study keeps it, under a steady lamp. The Whalestoe has its letters and its
  clocks, and the hospital its tube and its monitor.
- **The five waters** keep their sounds and the shape of the water. Each is now its own colour, and the canoe has the light
  of a phone in it.
- **The leaves and the birds** were the Goatman's and the barn's. The Goatman keeps the birds over drawn leaves. The barn has
  straw and slow breathing.
- **The stone** was the cave's and the plain's. The cave has rock dust and a chisel. The plain has blown sand and a dry wind.

## What is unchanged

- **Which doors leak, for whom, and when they stop.** A story's door leaks to the reader it is dealt to. A finished story's
  door, a room waiting on something else, and the heart's room once it is done, let nothing out. An open door lets nothing
  out.
- **A gray door that leaks anyway.** Some ordinary doors borrow a room's bleed, as they borrowed a kind: a hint is a sense of
  some room and never a label on this one. They now borrow one of ninety-five.
- **How far.** Seen from twelve blocks and heard from six, a quarter as loud as an ordinary sound, from just behind the door.
  Hillary behind a door is heard every four seconds, as before.
- **Sound through a door from close to** (0.4.229: a clock, water, music, voices, somebody moving, nothing moving) is a
  separate thing at under two blocks, and is left as it was.

## What it does not do

- **A bleed does not follow what is happening in the room.** It is the room's own sense, the same on every visit until the
  story is done. The sounds from close to the door do follow the room (the séance's go quiet once the family has gone), so at
  such a door the two can disagree.
- **The light is drawn on the floor.** It does not light the hall, the door or anybody standing in it.
- **A picture lies flat on the floor in front of the door,** as the strips always did. Where the floor steps down at a door,
  it will stand in the air.

## Tools and checks

- `tools/draw_door_bleeds.py` draws the sixty pictures. `--check` redraws them and compares, and reads `DoorBleed.java`: a
  drawn bleed there with no picture, or a picture no door lets out, fails.
- `tools/generate_door_bleed_audio.py` makes the eighty-three sounds, their entries in `sounds.json` and their subtitles.
  `--check` looks for each file, entry and subtitle, reads `DoorBleed.java` in the same way, and fails if two rooms are heard
  as the same words.
- Both run in the quick checks at the head of every CI run.
- **Extended case** `MazeTests.everyCurrentVignetteAndTheHotelHaveTheirOwnCue`:
  - every story has a bleed of its own, and lets it out whatever the seed;
  - no two look alike at the door, and no two share a sound;
  - every bleed belongs to a story (or to the flooded passage, which kept its water);
  - every drawn floor and every new sound is in the pack;
  - every sound's interval is a whole number of seconds;
  - the Mother's den has her fur, her purr at the pitch it was, no light and nothing in the air;
  - an ordinary hall has nothing of its own, and a leaking gray door still borrows.
- **Eight other cases** that named the old kinds now name the rooms' own: the personal deals at one door, the round trip of
  what a client is told, the heart's room, the drowned town's door, the preserved cave and the shallows, the canoe, the
  Goatman, and the caver's cave.

## What local testing and the frames found

A local aid drew all ninety-five bleeds through the game's own renderer: from above, the door at the top of each tile, at
five moments, on a dark floor and on a pale one, and at two moments with screen effects at nothing. It is a review aid and is
not in the commit.

- **Every light came out dark in the first frames.** The fault was in the aid, which drew under the inventory's lighting,
  where a face that looks up is in shadow. In a hall the renderer was right. The aid now sets the level's lighting.
- **The pictures were the wrong way round for somebody facing the door.** The Wheel's brass numbers and costume night's
  number card read backwards, and dead air's bars ran in reverse. A picture's left is now the left of somebody standing in
  the hall facing the door, and a light's first column is at their left. The frames after read 4 1 2 7, the card's 7, and
  the bars in a test card's order.
- **The replacement's paw prints did not show on a dark floor.** They are drawn in two tones now, so that one of them shows
  on any floor.
- **Two pairs of rooms had lights too alike to tell apart.** Harrigan's study and the séance were both candlelight in close
  ambers; the red room and Feet Up both swelled, in red and in orange. Harrigan's is a steady lamp now and Feet Up's is
  firelight. The extended case asks that no two look alike, and does not count the sound towards it.
- **Several sounds sat too low to be heard on a small speaker,** most of their weight under a hundred hertz: Borrasca, the
  Kept, the last crossing, the work print, the motion light, the red room, the Hill nursery, Usher and the holy rabbit among
  them. Each was raised, or given something in the middle of the range, and three subtitles changed with what is heard now:
  the last crossing's pump and winch, the Kept's machines and a screen waking, the holy rabbit's wind over snow. The tool now
  measures loudness between 180 and 5,000 hertz, takes out everything under 70, and holds every peak to four fifths: one file
  had peaked at .97 once it was encoded.
- **Two lights went down and came back too fast.** The tube's stutter was in twelfths of a second, and the neon's second tube
  stuttered as it dropped out. The stutter is in fifths of a second now, never down and up more than twice in one, and the
  dropout is one dip.
- **With screen effects at nothing, two different moments gave the same frame,** pixel for pixel, apart from the ten tiles
  with water, where the game's own water moves. At full, twenty-four tiles differ between the same two moments.
- **The nine changed cases pass.** Eight ran together in the selected set (8 of 8). The ninth, the caver's cave, could not
  start there: the selected set has no test area for the cave's namespace, and the server stopped before any case ran. It
  ran in the cave's own namespace instead (12 of 12). The quick checks pass, with the two new tool checks among them, and 1,564
  cases are declared, as before.

## For the owner

Four things here are choices more than repairs. Say if any should go another way.

- **The old cabin's lamp under the door keys a word.** The lamp inside spells the name the visit asks for. From the hall it
  cannot know that name, so it keys `HOME`, over and over. It could key another word, or only blink.
- **Two bleeds say something about their story before the door is opened.** The Drawing's slips of paper have one marked
  slip among them. The family copy's sound is a door handle turned and let go. Both can be made plainer.
- **The motion light answers to anybody.** Its light comes on when any reader is near its door, on each reader's own screen.
- **The eighty-three subtitles are new player-facing text.** They are in the table above. Each says what is heard and that a
  door is in the way, and names no colour, nobody's age and nothing anybody feels.

## Still to come

- The furniture's own textures in the Pell house's other rooms, where the frames show them flat.
- Put by for later: a bleed that follows the room (quiet once the room is empty); a ball, and a game of catch with Callum;
  taking Tilly round the house by the arm.
