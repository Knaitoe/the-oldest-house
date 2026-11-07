# State Route 9 (0.4.333)

From the owner's design "State Route 9 (the-median)": a one-shot outdoor crossing, Survival in the Witness. Frogger turned into a childhood fear: crossing a four-lane rural highway on foot after dark, judging the gap, getting to the weeds in the middle, and getting back.

`STATE_ROUTE`, slot 201, a literary place outside under the night (scene clock 9, rain, close fog, no music). It is dealt once to each reader, and only to a reader who has crossed any of the House's ordinary yard: the joists hall already lets them hear this road overhead. Layout 102.

## The road

Out of the door a short lane between cedar hedges runs north onto the starting shoulder, which is wet gravel. Along the back of the shoulder stands a tall chain-link fence with a padlocked pedestrian gate in it. The gate is the way on.

The road beyond is laid out like this:

- two eastbound lanes;
- the median, two blocks wide and a step down, with dead weeds in the ditch gravel and bent guardrail posts;
- two westbound lanes;
- the far shoulder, with a galvanized mailbox under the one sodium lamp. Its flag is up and the brass key is inside. The route shield and the mile marker stand beside it.

Behind the far shoulder the corn stands wet and close, up to a face of dark joists. The traffic comes out of the hedges at either end of the road and goes back into them. Waiting on the starting shoulder is always safe.

## Beat 1: judging the gap

Each lane has its own traffic, and a vehicle's lights alone do not tell a reader how far away or how fast it is.

- **Pickup.** Slow. Its diesel chugs ("A distant diesel engine chugs steadily") and its headlights throw short, warm pools on the tar. It clips a reader rather than running them down: two hearts and onto the tar, not sent back.
- **Car.** Quick. It rushes past in the spray.
- **Semi.** A logging rig. Three seconds before it comes out of the hedge, its tires hiss on the wet tar ("High-speed tires hiss on wet asphalt") and a white shine slides down its lane ahead of it. Its air horn sounds as it goes by. Struck squarely by a car or a semi:
  - the screen goes black to a screech, and the reader comes to on the starting shoulder, hurt but never down to less than a heart;
  - an impact that would have killed them sends them back into the House instead. They can be dealt the road again.
- **The wind off a trailer.** A semi that misses still pulls at a reader within a block and a half of its lane: a heart, half a block toward the tires, and a shudder of the view (scaled to the screen-effects setting).

## Beat 2: the median

The first reader to drop into the median brings a convoy: a semi down each lane beside them, timed to reach them together, then a solid run of cars. Crouched in the weeds, the wind goes over them. Standing, it shakes them like washing on a line. Afterwards the lanes beside the median get a fair gap so the second half can be crossed. A later crossing gets its own convoy once the median has been empty a while.

## Beat 3: the crawl and the crowd

A hand on the mailbox brings the flag down and gives the key. The note wrapped round it is the owner's found text, as written; it is the source. Then every vehicle on the road drops to a crawl, for everyone on it: bumper to bumper at ten miles an hour in all four lanes, a car's gap between bumpers, engines ticking in the rain. Bumpers only nudge a reader along.

- **The empty cars.** Every cabin is drawn hollow behind see-through glass, so every car can be seen to be empty.
- **The wagon.** One station wagon with wood along its sides comes along the third lane, every seat full of dark, still people whose heads turn to watch the reader.

Back on the starting shoulder, the key in the padlock sets off every horn on the highway in one chord. Every light on the road goes out, the lamp over the mailbox with them, and the gate stands open.

## Together

There is one road and one lot of traffic for the party. Whoever opens the mailbox brings the crawl for everyone on the road.

A reader who stays on the starting shoulder can:

- lean on the chain-link and call the gap aloud for whoever is out on the road. The call shows over their head, like anything said (0.4.329). It is judged from the road as it really is, looking at the two lanes the runner still has to cross: "Now! Go, go!", "Wait! Truck!", "Wait—car!", "Wait, there's a pickup, let it by." or "Hold on, I can hear a truck coming.";
- hold the gate as far as its chain allows, which is a hand's width.

Everyone on the road when the padlock opens is resolved and given their own account:

| Who | Outcome | Role |
| --- | --- | --- |
| Whoever turned the key | `carried_the_key_back_across_the_9` | actor |
| A reader who crossed | `crossed_the_9_with_them` | actor |
| A reader who stayed on the starting shoulder | `watched_from_the_fence` | witness |

The co-op line is "…on the 9 with me that night."

## Variant accounts

Each account says only what that reader's own record holds:

- whether they crossed for the key, crossed with someone, or watched;
- the gaps they called and the gate they held;
- how often the road put them back, a pickup's knock, the wind off the trucks;
- whether they got down in the median or stood;
- the note round the key;
- the crawl and the empty cars;
- whether they looked into the wagon and whether its heads turned;
- who turned the key.

The House's read changes it:

- **AGE.** The route shield and the mile marker are each sent to a reader as the old painted steel kind or, for a reader read as DIGITAL, the reflective aluminum panel, and the account describes the one they saw.
- **HOME = RUNAWAY.** An unmailed postcard of the county line lies in the mailbox with the key ("Made it this far. The rest is just walking."), and sprinting on the asphalt is a tenth faster.
- **HOME = DANGER.** Crouched in the median weeds, the wagon's heads do not find them; when they stand to run, every head comes round. The account says so.

No colour is named, the reader's gender never comes into it, and neither does an age.

## Cheating and companions

- Block breaking and placing are the House's as everywhere.
- An ender pearl thrown across the lanes is taken out of the air by a trailer's wind and drops in the median.
- The vehicles are drawn by the road's anchor, not mobs, so there is nothing to strike.
- Companions that wander out onto the road are put back in the lane between the hedges behind the fence. Their orders stand.
- The key never leaves the road. A reader who leaves or is struck with it loses it back into the mailbox, and the traffic picks up again.

## Yield

The Amber Reflector Disc, a cracked reflector button off a guardrail post. Carried, it leans the dealing of roads and open air up by half. Survival credit comes at the padlock.

## Assets

- `tools/draw_state_route_art.py`: 49 textures, 42 models and states, and the item names. Wet asphalt and its line rows; the mailbox flag up and down; the shield and mile marker painted and reflective; the sodium lamp lit and dark; chain-link and the gate; the guardrail post with and without its reflector; corn; six paints, the cab, trailer, logs and log ends, the wagon and its wood panel, rain-streaked glass, the wagon's people, two headlight pools and the semi's shine; the key, postcard and reflector.
- `tools/generate_state_route_audio.py`: nine original subtitled mono cues (diesel, hiss, horn, pass, wake, screech, tick, chord, padlock).
- `StateRouteRenderer` draws the traffic; `StateRouteClient` draws the rain, fog, music and shudder.

## Validation

- `StateRouteTests#theRoadTheTrafficAndWhatItDoesToABody` (literary) checks:
  - the road built whole and dealt only after the yard;
  - traffic out of the hedges;
  - a car squarely, landing on the shoulder hurt and alive;
  - a pickup's knock, which does not send the reader back;
  - a semi's wake on the next lane;
  - the convoy called by the first reader into the median, and a crouched reader untouched by a semi.
- `StateRouteTests#theKeyTheCrawlAndTheGateForARunnerAndAWatcher` (literary) checks:
  - a watcher's call heard by the runner, and a semi called;
  - the key out and the road at a crawl for both readers, with the wagon in the third lane and looked into;
  - a companion put back behind the fence;
  - the padlock: dark, the gate open, the key gone, both resolved with their own accounts, no colours named, the reflector for each;
  - the road set back.
- Witness 76 / 57 / 53, two kinds, three endings.
