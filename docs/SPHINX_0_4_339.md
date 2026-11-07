# The Sphinx: the road under the corn (0.4.339)

The Sphinx's fourth crossing, from the owner's draft "The Sphinx" (4 November), "Outside · The road under the corn":

> *"Which path was here before the field?"*

`LabyrinthPlace.SPHINX_FIELD`, slot 204, gray, layout 105. It is built outside, at dusk (scene clock 3), and comes rarely (weight 2) to a reader past the story depth who has already been out in the House's yard. It is dealt per reader, so a reader who has never seen the yard never meets it. The way onward is always open, along the ditch and up a lane, so nobody has to answer her.

## The place

- **The back door.** The House's clapboard back wall stands behind the reader, its bulb lit.
- **The ditch.** A roadside ditch a block deep runs across in front of the door, with mud-brick steps down and up. A culvert's grate is set in its far bank.
- **The clearing.** Up out of the ditch is a clearing of stubble and trodden grass at the edge of the corn. The corn stands two blocks high and a body cannot push through it.
- **The gaps.** Three gaps lead into the corn, each as worn as the others, each with a spruce field gate between two stripped posts. On the posts are a stop pin and a survey socket (docs/SPHINX_0_4_334.md). Loose soil lies across each gap's mouth.
- **The paths.** Two of the paths are newer: they end a short way in at a trodden circle. The third is the old road: it runs on through the corn to a strip of asphalt across the far side of the field, its yellow edge line still painted.
- **The way onward.** At the ditch's east end is a gravel turnout with a surveyor's tin box on a post. A lane runs up the east edge of the field to the side of a shed with a door in it, under a lit bulb.

## The proof

- **Her voice.** Her question comes first from the ditch, as the reader reaches it or the clearing. On her next breath the tassels part well above the reader's head, chaff comes down, and she is heard again from high over the corn. The culvert's grate asks the question again for anyone who missed it.
- **Brushing.** The loose soil at each gap's mouth can be brushed away with a hand:
  - under the soil of the two newer paths, the dirt is packed hard, with footprints in it going in;
  - under the third, there is cracked asphalt and the edge of a painted line, running on under the standing corn on both sides.

  What is brushed bare stays bare for everyone and through a rebuild. Each reader remembers what they found.

## Answering

- **Opening a gate** is the answer. Survey pins are locked exactly as in the vestibule, and one gate moving at a time refuses another. The gates open only this way: a hand on a gate is the House's handler, not vanilla's.
- **The old road.** Its gate swings open. Walking the road through the corn to the strip of asphalt brings the reader round to the lane: the stalks fall aside where the strip meets it, and that way stays open for everyone, rebuilds included.
- **A newer path.** Its gate lifts an inch and catches on its stop, and then:
  - she says "Not that one. That path came after the field.";
  - that gate's stop pin shows;
  - her eye comes up over the corn behind the gap, upright, following whoever is nearest;
  - after seven seconds it sinks again.

## Forcing

Pulling an exposed stop pin is the only thing that brings her. Over three seconds she comes out of the stalks:

1. The tassels shake, moving toward the gap from far back in the field.
2. The sweep of a shoulder rises above the corn, well back and to one side.
3. The rows flatten either side of the path, from the far end toward the gate.
4. Her hand comes down across the track just inside the gate, her forearm lying along the path behind it. The gate is bowed open, and her eye is up over the corn.

Then:

- **Anyone in her reach.** Anyone in the clearing within six blocks of that gap is warned (her breath, chaff on them), then her hand comes across the clearing and sweeps them back into the ditch. Nobody is hurt. Each sweep is recorded.
- **Withdrawal.** Once the clearing has been empty for five seconds she sinks back into the corn: the hand and forearm go, the rows stand again, her shoulder and eye sink, the gate drops shut and the pin goes back in.
- **Walking on past her hand.** The track cracks and tilts under the reader and they are set down past her, on the lane. Behind them the corn closes: that gap is standing corn from then on, its soil asphalt, its pin and socket gone, for everyone and through a rebuild.

## The folded survey

The surveyor's tin at the turnout gives each reader who has heard her question a **Folded Field Survey**, whatever they did after. It holds two sketches of the culvert dated the same day: in one a road runs over it, in the other corn is planted straight across it. A reader who brushed the old road bare has a line of their own on it.

It is marked as that reader's own. The tin gives another only to a reader who no longer carries theirs. The survey is kept for the dream (the next Sphinx release).

## Multiplayer

There is one field for the party:

- the gates, the brushed soil, the open way, her emergence and the closed gaps are shared;
- each reader's answer (by pin or gate), what they brushed, their pulled pin, their sweeps, their walkaway and their survey are their own, under `sphinx_0306` → reader → `field`;
- her question is asked privately to each reader who reaches the ditch or the clearing, and the echo over the corn follows that reader;
- her warnings reach everyone in the field;
- her eye follows whoever is nearest;
- a reader who leaves, or goes offline, with a pin set takes it unanswered.

## Assets

`tools/draw_sphinx_fittings.py` draws four new textures and three new fitting kinds:

- **earth** and **soil** (loose, footprints, asphalt and paint): the soil at each gap's mouth, in three stages;
- **flat corn**: the corn pressed flat under her forearm;
- **tin**: the surveyor's box on its post.

The corn is State Route 9's. Her eye, hand, forearm and shoulder are the fittings drawn for the earlier crossings. Every sound is one already made for her.

## Tests

`SphinxFieldTests#theFieldIsBrushedAnsweredWalkedAndForced` (exploration), with two readers, checks:

- the field as built: gates, soil, pins and sockets; paths between corn a body cannot pass; the old road through to the strip; corn between the strip and the lane; the lane open the whole way; dusk;
- the question from the ditch, not heard on the lane, and the culvert's grate asking it again;
- footprints under one gap's soil and asphalt under another's, each remembered by its reader;
- one survey from the tin for each reader who heard her, with the brushed line for the reader who found the road, and no second while one is carried;
- survey pins locked by a caught gate, each pin its reader's own answer;
- the warning, the stop pin, and her eye coming up and sinking;
- the old road's gate opening and walking it to the strip opening the way to the lane;
- the pin owned by its puller, her shoulder, the flattened rows, her hand across the track and the gate bowed open;
- a sweep into the ditch, with nobody hurt;
- her withdrawal once the clearing is empty;
- a second provocation and walking on past her hand, which sets the reader on the lane and closes the gap in corn;
- the open gate, open way, closed gap and brushed soil all kept through a rebuild, and a lost survey replaced from the tin;
- no Witness credit.
