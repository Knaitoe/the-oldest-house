# In the Tall Grass (0.4.349)

The second of the yard's four stories, from the owner's draft "The House Has a Yard" (4 November), built as `docs/YARD_STORIES_PROPOSAL.md` describes. Both of the proposal's slices ship together, so the story is playable from its first release, and its Witness source is added now. Layout 110 appends slot 210. Protocol 144 adds the grass, the trampled stems, the cap, the pickup and the wagon's hazard lamps. Nothing standing is rebuilt.

From the draft:

> *"A separate roadside threshold begins on the shoulder, where a boy sounds close enough to help. Let the player see the road from the first few rows. Once they commit, the road recedes even though the boy's voice stays close. In co-op, one reader can call from the shoulder. The first wrong bearing comes when the other reader hears that call ahead. At the end, another car pulls over and the player has a chance to shout something to the person getting out."*

The draft links Stephen King and Joe Hill's novella. Its shape is honoured: a voice in roadside grass, a field that moves people, and the next car. Nothing is taken from its text, names or particulars.

`TALL_GRASS`, `TallGrass`. It is a literary place, outside, built whole by `TallGrassSite`, in the late afternoon (scene clock 16). It is dealt once to each reader, and only to a reader who has crossed one of the House's ordinary yard crossings. Its Witness kind is **survival**. The story id is `tall_grass`.

## The road

- **The House's door** stands in the brush along the south side of a two-lane county road. The road runs out of the trees to the east and back into them to the west.
- **The wagon.** Across the road, on the north shoulder, the House's own station wagon is pulled over, nose west. Its hazard lamps blink at the nose and the tail, and the relay ticks. Its driver's door is open and the keys are in it.
- **The notice.** The county's notice is on its post by the wagon's tail. It is the story's source, outlined until it is read:
  - the verge on the north side will not be cut this season;
  - do not leave a vehicle standing on the shoulder;
  - if you hear someone calling in the grass, stay with your vehicle, do not go in after them, call the number below and wait;
  - the number has been torn off.
- **The ditch** runs along the north shoulder, a step down, damp.
- **The grass** begins past the ditch. It is higher than a head and runs a long way north, to a line of telegraph poles.
- **The boy.** Out of the grass a boy calls: "Hello? Is someone there? I can't find the road." Every reader on the road hears him from the same place, a little way in across from the wagon.
- **The way on** is a field gate in the brush on the south side of the road, east of the House's door.

## The first rows

The grass is the clear decision.

- **The first rows are thin.** Stems are gone toward the ditch, so from the first few rows the road shows back through them, with the wagon's hazards ticking. A reader there is told so, and can step back out.
- **The old fence line.** A few rows in, rotten fence posts stand in the grass. A reader who goes past them has gone in.

Until then, the House's door, the wagon and the gate are all there, and leaving costs nothing. The wagon's driver's door offers a way back: getting in takes the reader back to the House.

## The field

Past the fence line, the field moves its readers.

- **The road recedes.** Every seven to eleven seconds, a reader who has gone in is set down somewhere else in the grass, farther from the road. It happens only while nobody outside their own huddle can see them.
  - A reader who heads back toward the road is moved sooner, and never set closer to the road than the fence line.
  - Readers who keep within a few blocks of one another are a huddle, and are moved together, keeping their places.
  - Deep in, the field moves them sideways instead, so its far edge is never reached.
  - The third time, a reader is set down where the heart of the field is in view.
- **The voice stays close.** The boy calls every six to nine seconds from a little ahead of each reader, privately, and says where he is: "Close by, ahead of you." His lines are his own: he can hear them, they're close, he has been walking a long time, don't go back that way.
- **The landmarks lie.**
  - Walking, a reader hears the wagon's hazards from the wrong side.
  - The telegraph poles stand the same distance away from everywhere: they are drawn a fixed way north of the reader's eye, so walking toward them never brings them nearer.
- **What can be trusted.**
  - **A reader's own track.** Grass goes down under a reader standing in it, the two cells above with it. It stays flat for twenty seconds, then rises in two steps over fifteen more, and then stands again. A reader who is moved finds their track gone from behind them; another reader may find it.
  - **The hazards, heard standing still.** A reader who stands still for three seconds hears the hazards from the true side. They are told which way, by where they face: "Standing still, you can hear the wagon's hazards ticking, behind you. The road is that way."
- **The heart of the field** is a ring trampled flat for good, as though something had turned round and round there. A small cap lies in the middle of it, wet through. Each reader may take one; the cap stays for the next.
- **Nobody's name shows** over the grass, so readers in it find each other by their tracks or not at all.

Nothing in the field hurts.

## Together

- **Calling from the shoulder.** A reader by the wagon can call into the grass through the panel: "Over here!", "Follow my voice!" or "Can you see the car?".
  - **The first wrong bearing.** Each reader in the grass hears the first call from the shoulder from ahead of them, not behind.
  - **After that,** a call comes from the true side only while the caller has stood still by the wagon for two seconds. Otherwise it comes from the wrong side.
  - Each call is heard as a shout from that bearing and shown with its direction: "Sam, behind you: 'Over here!'"
- **Readers in the grass** can call out too, with a hand on the grass: "Hello?", "Where are you?" or "I'm over here!". Others in the grass hear it from the wrong side. Those on the road hear it from where it really is.
- **Finding each other.** Two readers in the grass a few stems apart see each other, and each is told so. From then on they are moved as one huddle while they stay together.
- **The co-op line.** "%s {was} on the road with me" (`CoopAccounts`).

## The end

When a reader has followed the voice for eighty seconds and been moved three times, the field lets everyone in it out together. It never keeps anyone longer than three minutes.

1. **Let out.** Each reader in the grass is set down at its edge, a row in from the ditch, facing the road, while nobody else is looking. "The grass thins. Through the stems: the road, close, and the gravel of the shoulder."
2. **The next car.** A pickup comes along the road out of the trees to the east. It is drawn as it really moves: slowing, pulling onto the north shoulder ahead of the wagon, then parked as solid blocks with its headlights on.
3. **The driver** gets out of the far side of the cab and comes round the front to the edge of the shoulder, looking at the grass. "Hello? Did somebody shout?"
4. **The shout.** Every reader at the edge gets the panel: the driver is looking right where they are, and not seeing them. The driver does what the first shout says:

| What they shout | Outcome | The driver |
| --- | --- | --- |
| "Stay by your car! Don't come in!" | `warned_them_off_the_grass` | stands a moment, says "All right," walks back round, gets in, and drives away west |
| "Help! In here!" | `called_them_into_the_grass` | says "Hang on! I'm coming in!", steps down off the shoulder into the grass, and is gone when nobody is looking |
| Nothing (or "Say nothing.", or no shout in fifteen seconds) | `kept_quiet_at_the_road` | listens; the boy calls from the grass, for everyone to hear; the driver says "Hang on, kid! I'm coming!" and goes in after him |

All three resolve one source, `tall_grass`.

- **Roles.** The reader who shouted is the actor, and everyone else on the road is a witness: the others at the edge, and anyone on the shoulder or at the wagon. When nobody shouts, every reader at the edge is an actor in keeping quiet. A reader who came to the road after the end began is partway.
- The reader is then let out onto the road. The wagon's door, or the House's door across the road, takes them back.

A pickup left by a driver who went into the grass stays on the shoulder, its engine running and its door open.

## The House's read

- **HOME = RUNAWAY.** The wagon is packed to the roof in the back, the way you pack when you are not coming back, and the account says the reader had somewhere to be.
- **AGE.** Through the open door, the wagon's tape is playing near the end of its side (ANALOG and TRANSITIONAL readers), or a phone on the dash is playing a station that keeps losing its signal. The account says which. No age is named.

## The accounts

"The account: the tall grass", in the reader's hand, built only from their own record:

- the wagon, its radio, and whether it was packed;
- the boy calling, and whether they went in, or only far enough to see the road back through the stems;
- for a reader who stayed on the road, that they called into the grass and the last thing they called;
- being moved (or moved again and again), and who was moved with them;
- the road from the wrong side, and the hazards heard true when standing still;
- who called from the road, the first call from ahead, and later calls from the right side;
- who they heard in the grass from the wrong side, and who they found;
- the heart of the field, the cap, and whether they took it;
- how they came out, or that they came to the road late;
- the driver, who shouted what, and which way the driver went;
- whether they said nothing, or had not shouted yet.

It ends with the walk back to the wagon, its hazards still going, and the House's door in the brush across the road. Nothing names the reader's gender, a colour or an age.

The Witness line is "I went into the grass by a county road after a boy who said he couldn't find it. The grass moved me whenever nobody could see, until another car pulled over and I had a chance to shout to the driver."

## The yield

**The boy's cap**, taken from the heart of the field. Carried into one of the House's field places (the storm field, the windbreak, the Marsh farm, State Route 9 or the Sphinx's field crossing), it gives a true bearing once in each: a boy's voice calls "This way" from that place's way on.

## Reset

Once the road has been empty for ten seconds, it stands as it was: the grass standing again, the next car gone and the wagon ticking.

## Assets

- `tools/draw_yard_fittings.py` (the yard's own tool) draws:
  - the wagon's nose and tail with their hazard lamps lit amber;
  - roadside grass in thirds: the crowded stems, the crossing blades and the nodding seed heads, as crossed planes like the corn, drawn without the biome's tint;
  - the same grass trampled flat, rising and nearly up;
  - the cap lying on the flattened stems;
  - a pickup in faded paint (nose, body with and without its front wheel, cab, bed with its back wheel, tailgate);
  - the cap's item sprite.

  Every earlier yard texture is unchanged.
- `tools/generate_tall_grass_audio.py` writes five original subtitled sounds:
  - a boy calling from the grass;
  - the hazard relay;
  - someone shouting;
  - a car slowing and pulling onto gravel;
  - a car pulling away.

  The voices are shaped buzz through vowel-like resonances, calling without words; the words are on the screen.
- One skin, `literary_grass_driver`, from `tools/paint_character_skins.py`, catalogued at stranger phase 115.
- `TallGrassClient` draws the telegraph poles and their wires from vanilla's spruce, and hides name tags on the road.

## Witness

One source added: 78 eligible sources, 59 required, across two kinds; three endings.

## Tests

`TallGrassTests` (literary), each in its own batch:

1. **`aReaderGoesInAndTheRoadRecedes`.**
   - The road built whole and dealt only after the yard.
   - The wagon's hazards blinking; the first row thin and the field close; the notice.
   - The radio by the wagon; the road seen from the first rows; gone in past the fence.
   - Moved farther from the road; the track trampled and springing back; the hazards heard true standing still.
   - Set down by the heart; the cap taken.
   - Let out at the edge, the pickup pulled over, the driver calling, the shout to stay by the car, the driver driving on.
   - The account follows the record, and the road is set back.
2. **`togetherTheFirstShoutDecides`.**
   - A caller by the wagon heard first from ahead, then from the true side.
   - A call from the grass heard by the other reader from the wrong side.
   - Two readers found together and moved as a huddle.
   - A reader who comes in partway.
   - The first shout for help: actor, witnesses and partway, each with their own account.
   - The driver gone into the grass.
