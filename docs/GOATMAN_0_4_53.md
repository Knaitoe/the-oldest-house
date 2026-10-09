# Anansi's Goatman: the night (0.4.53)

Owner-requested rework of the Goatman (slot 31), after a playtest and a reading of the original story. Status: implementation complete; exact-head verification pending.

The vignette was a test of restraint: survive a minute of hammering by not opening the door. It is now a puzzle built from the story's own pieces:
- the count;
- a knock that is real and a knock that is not;
- signs that announce it;
- a gap it gets in through.

Nothing explains the rules. A player who watches and counts can work it out; one who does not, finds out by what follows them home.

## Playtest repairs

- **Words at the door only at night.** The door overlay used to show "Let me in" from the first seconds on the trail, because it ran off a clock that ticks in every phase. It now shows only words actually said at the door: the cousin outside at dusk, or what knocks at night.
- **Real seats.** Every chair and fire seat has its back behind its sitter. Each cousin sits on an actual seat, centred on it, at the seat's height. The supper spots used to sit a block beside their chairs, and the bent pose sank into the stair. The table is a long slab top a hand above the chairs.
- **Real walking.** Cousins walk authored routes through the yard, up the porch step and in at the door, along the aisles, to their seats and bunks. They never pass through the fire seats, the porch rails or the table. The old idle loop (a constant slide around each spot) is gone. Instead they turn their heads to someone now and then, and the STILL tell does not.
- **Nothing buried.** No activity spot sits in a prop. The "firelight" tell's spot was inside a feed sack, and one fire spot was inside a log seat.
- **Light.** The dimension has no ambient light, and the trailer had two cold sea lanterns down its middle, so the bunks and kitchenette were at light 0–3. Now there are warm lamps:
  - two pendants over the table;
  - a lamp on a shelf beside each pair of bunks;
  - a hanging lamp over the kitchenette, one in the bathroom and one inside the door;
  - a porch light outside.

  Everywhere a cousin can be is lit to 8 or more, so the tells are readable. The global darkness floor (an owner decision) is unchanged.

## The camp

- **The trail.** Two hollows open off it. In each, privately for each latecomer, something stands with its back to the path: the first a little way into the woods, the second on the other side and closer. Each is gone once the latecomer is past it. This is the Goatman's own form (below).
- **The fire.** It has four seats, turned in to it.
- **The generator shed.** Across the yard, with its blast-furnace generator and a gas can.
- **The trailer.**
  - the kitchenette, with a pan of brats on a griddle on the stove;
  - the long supper table;
  - bunks in both bays;
  - a small bathroom behind a partition in the north-east corner. Its awning window starts propped open.

## The evening (gathering clock, ticks)

| Tick | What happens |
| --- | --- |
| 200 | A cousin: "Somebody shut the bathroom window. Bugs are getting in." |
| 360 | One real cousin (the **runner**, never the one that doesn't belong) says the generator is out of gas and walks off down the trail. |
| 600 | Supper. The pan holds one brat for every child who should be there: four real cousins and every enrolled player. Each cousin who sits takes one, and so does the one that doesn't belong. Each player takes one from the stove. |
| 1000 | A cousin: "Who had two? There was one for everybody." |
| 1100 | The one that doesn't belong gets up, goes out and stands by the fire with its back to the trailer. Watched, it laughs without a sound. |
| 1300 | The runner comes back up the trail calling "Wait up! Don't lock it!". If the door is open he walks in. If it is shut he knocks, in an ordinary voice, until someone opens it. If the pan is empty when he sits: "Y'all ate mine?" |
| 1500 | **The quiet.** The crickets and the woods stop, the porch light goes out, copper motes fill the camp and the fire gutters. If the runner is still outside, his knocking stops mid-word and he is gone. The thing by the fire starts for the door, moving only while no child is looking at it. If it reaches an open door, it walks in and sits down among them. |
| 1800 | Night. A cousin locks the door ("Lock it."). Anyone outside finds themselves inside. |

## The night (vigil clock, 1,600 ticks)

- **The knocking.** If it is outside, it knocks and claws at the door from tick 60 to tick 1150. It uses the runner's words without his voice: "Let me. In." "in" "stop playing" "It's. me."
- **The window.** From tick 700, a propped-open bathroom window lets it in. It comes out of the bathroom and lies down on the floor among them. A shut window gets a scrape at the glass, and it stays outside.
- **The cousins.** At tick 720 a cousin says "Maybe we ought to check." and goes to stand at the door. At tick 1000 the cousins go to their bunks; if it is inside, it lies on the floor.
- **The scream.** At tick 1250, if it is still outside, something screams far off in the woods.
- **Opening the door at night** takes the opener, alive and with everything they carry, out of the trailer to the manor. It goes with them (the haunting). The door shuts again behind them, and peers continue.

## Dawn

The night is **right** only if:
- the runner was let in before the quiet;
- it was kept out, by the door and the window.

Every enrolled child who sat out the whole night then resolves the story (outcome `counted_right`) and takes home the **tally counter**, once ever. Something stands at the treeline with its back to the camp for them. If the night was not right, it walks home with every child there.

## The fail state: it comes home with you

No death and nothing taken. It is private to the reader, it never attacks, and it lasts until the reader gets a night right:
- **It eats with them.** One piece of food is gone from the pack each in-game day: "Someone has been at your food."
- **It is glimpsed.** Every three to seven minutes it is seen ten to fourteen blocks off, well to one side of where they look, in its own form. It is gone when they look straight at it.
- **It tries their door.** On some Overworld nights, by a closed wooden door, there is clawing at the door and "Let me in. Stop playing."

The dealer weights the trailer twenty-four times for a haunted reader, so the way to be rid of it stays near. On a later night done right, it walks out of the camp with them in the cousin's shape it wore (the runner's, if it took him). It falls to the back, looks at them, and goes into the trees, and the haunting is over.

Failures from before 0.4.53 still wake once in the manor, as they were promised. A death in the scene from any other cause only ends that child's evening.

## The tally counter

Nobody saw the Goatman; arithmetic caught it. The counter works only for the reader it was left for.
- **Inside the House.** Clicking it counts every living thing really within sixteen blocks and makes anything hiding or pretending glow for five seconds: the invisible, a cousin with tells, the thing itself. It never reveals another player. It also clicks on its own when something hostile comes up behind its keeper (eight blocks, once each ten seconds).
- **Outside.** Clicking it counts the hostile things within thirty-two blocks. While its keeper sleeps, anything hostile within ten blocks wakes them with a click and is shown to them.

## The Goatman itself

A man with the head of a goat:
- a gaunt grey body with ribs and spine;
- arms too long, ending in dark hands with black nails;
- furred legs ending in hooves;
- a goat's skull with muzzle and beard, drooping ears and swept-back horns;
- amber eyes with a flat black pupil, on an emissive layer so they catch the dark.

It is 2.2 blocks tall, private to its viewer, never saved and never hurt. It walks with a hitch, its head sits crooked, and it laughs without a sound.

## Assets and code

- **Assets.** `tools/generate_goatman_night_assets.py` generates:
  - the counter and brat item textures and models;
  - three copper mote particles;
  - the Goatman's 64×64 skin (exact box UVs of `GoatmanFigureModel`) and its eyes layer;
  - four original synthesized, subtitled cues: the counter's click, clawing at the door, crickets (a seamless six-second loop), and something screaming in the woods.
- **Code.**
  - `GoatmanVignette`: the evening and the night;
  - `GoatmanWoods`: the camp;
  - `GoatmanHaunt`: the fail state;
  - `TallyCounterItem`;
  - `GoatmanFigure` with `GoatmanFigureModel` and `GoatmanFigureRenderer`;
  - `GoatmanParticles`;
  - `GoatmanVisualProof`: the native client proof.

## Saved worlds and protocol

- **Layout 38 / protocol 36.** Worlds at layouts 18 to 37 carve the woods, camp and trailer again, last, only once nobody is in them or can see them and they are loaded, in bounded slices. The trailer's own furnishing, shell and exterior passes and the polish and composition then dress the new carve as they would a fresh one.
- **What is kept.** The old evening is forgotten. Every child's own record is kept: Witness evidence, failures, haunting.
- **Counts.** Forty-three sources / thirty-three resolutions / two kinds / three endings are unchanged.

## Verification

Require:
- the complete suite, including the nine Goatman cases: the trail and its hollows, a night done right with an old haunting cured, the door opened at night, the window left open, the runner left outside, the counter, resume, and the old-layout append;
- the old-save upgrade's re-carve assertions;
- the native Goatman proof;
- all architecture views.
