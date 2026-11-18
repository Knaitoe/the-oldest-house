# The list, part 4a-1: the hunt (0.4.708)

The first half of part 4a of *The list* (`docs/THE_LIST_PLAN.md`). Part 4a was one row in the plan: the names as people, hearts that
give them away, the staged hold and breaking free, the hay hook and its finish, blood, wounds and stains, bodies left and found. It is
built in two halves:

- **4a-1 (this release):** the five on the list as people. They notice the slasher, run, hide, shut and bolt doors behind them, yell
  for help and fight back once; found again, they cower.
- **4a-2:** the catch, the staged hold and breaking free, the hay hook and its finish, blood, wounds and stains, and bodies left and
  found.

No night uses the hunt yet; nights two to four (parts 5 to 7) will. Until then it is begun by hand, in the town, with
`/ghosthouse test the_list hunt`, which gives the operator the body and sends the five to their places.

## The five as people

Deek and Wes sit on the bench by the field, Mel and Lesley high on the home stands, Shannon on the bus bench on Main Street, as in 1b.

- **Noticing.** One of them notices the slasher by sight or by sound:
  - seen in front of them (within 75 degrees of where they look), with nothing between: within 24 blocks when the slasher stands in
    light, within 12 in the dark;
  - heard from anywhere within 6 blocks.
- **Running.** They get up off their seat with a scream, say their own words and run.
  - Anyone else on the list within 40 blocks hears the scream and runs too. From the bench by the field that is all four there;
    Shannon, on Main Street, hears none of it.
  - They run faster than they walk. The slasher's body only walks; it does not catch a runner on open ground.
  - Each has two places to hide, and a way to each:

| Name | First place | Second place |
| --- | --- | --- |
| Deek | Across the field and through the field house's back door, through the locker room to the far end of the showers. | Back through the inner wall to the far end of the locker room, between the benches and the lockers. |
| Wes | The same door, then into the coaches' office, his back to the desk. | Back out to the locker room, against the inner wall. |
| Mel | Down off the stands and under them at the north end. | Along under the stands to the south end. |
| Lesley | Along the top row into the press box, bolting its door, to the far end past the tables. | Back out of the press box, down to the sidewalk and round under the south end of the stands. |
| Shannon | East along Main Street, round to the drive-in stand's side door, and in. | Back out of the stand and round behind it. |

- **Doors.** A door on their way is opened as they come to it and shut and bolted behind them:
  - not while anyone is standing in it;
  - held for one of the others still coming the same way, for up to six seconds, unless the slasher comes within five blocks, when it
    is slammed;
  - the slasher, within 24 blocks, is told: "Steel slams. A bolt shoots home." for the steel doors, "A door slams. The lock turns."
    for the stand's.
  - Jenna is in the stand. She tells Shannon to get in as she comes to the door, and to get down once it is bolted.
- **Bolted doors.**
  - Another reader's hand on a bolted door is told "Bolted from the other side."
  - The slasher strikes a bolted steel door (the field house's, the press box's) with the hand or a blow. It takes five blows, eight
    ticks or more apart, each with its own cue, and on the fifth the bolt tears out of its keeper and the door swings in. Nobody bolts
    that door again while the hunt lasts.
  - The stand's wooden door is struck as any locked house door in town (part 2): three blows and it gives.
- **Yelling for help.** Three seconds into the run each yells their own words for help. Every reader in town who is not the slasher,
  within 96 blocks, is told from which way: "Somewhere to the northwest, Deek is yelling for help."
- **Hidden.** At the end of their way they crouch, turned the way the place faces.
- **Hearts.** A name who is running, hidden or cornered has a racing heart in the slasher's ears, wherever they are and whether or not
  they can see the body. A hiding place hides them from sight, not from that.
- **Found.** The slasher finds one hiding by coming within 2.4 blocks with nothing between. Struck by the slasher, one at their place
  runs and one hiding is found.
  - The first time, they fight back: their words, a blow, and the body goes down for its five seconds (part 2), knocked back a little,
    with what they did as its cue. Lesley throws dirt through the sack's eyeholes as well, and for three seconds the slasher sees
    nothing. Then they run for their second place.
  - One still running who is caught up with (within 1.6 blocks) fights back the same way, once, and keeps running.
  - Found again after fighting back, they cower: down on their heels, arms over their head, facing the slasher, with their words. They
    do not fight again. What happens next is 4a-2.
  - Anyone whose way runs out, or who is stuck for eight seconds, hides where they are.
- **Ended** (`hunt off`), everyone is stood up and sent back to their place, and the three doors are put back as they were built.

## Single and multiplayer

- **Alone**, the reader has the body and the five run from them.
- **With others**, only a reader with the body is hunted from. The five do not notice anyone else.
  - Everyone near hears the scream. Everyone in town who is not the slasher is told who is yelling for help and from which way; the
    slasher is not.
  - A door is not shut on anyone standing in it, a reader on Amber's side included.
  - A bolted door is bolted to everyone; only the slasher's blows spring it.
  - Each reader's cues are their own.
- **Spectators** are never noticed or told anything, and are never hunted from.

## Retreat

Unchanged. The hunt locks nothing; the plan's lock while the reader has hold of someone comes with the catch in 4a-2.

## Made as

- **`TheListHunt`** (new): the hunt, one to a town.
  - Each of the five is a runner, `CALM`, `RUN`, `HIDE` or `COWER`, with a gesture the client draws (`STILL`, `RUNNING`, `HIDING`,
    `SWINGING`, `COWERING`).
  - `UP` is where each gets up to; `WAYS` holds each one's two ways as steps (walk, open a door, shut and bolt a door, hide), relative to
    the town.
  - Noticing (`SEES_LIT` 24, `SEES_DARK` 12, `HEARS` 6), the scream (`SCREAM_REACH` 40), the yell (`HELP_AFTER` 60 ticks,
    `HELP_REACH` 96), finding (`FOUND` 2.4), being caught up with (`CAUGHT` 1.6), holding a door (`HOLD` 120 ticks) and slamming it
    (`SLAM` 5).
  - Bolted and sprung doors are kept in the hunt, by their lower halves.
  - A runner's ground is kept loaded under them with a region ticket while they run.
  - `start`, `end`, `struck` (a blow on one of them), `status`, `compass`.
- **`LiteraryActor`**: the five walk the same way as Borrasca's cast (`BorrascaCast.walk`), 1.3 times as fast while running; their
  gestures are their own; they take no harm, and a blow from the slasher is `TheListHunt.struck`.
- **`ActorSeat`**: one of the five is seated only while still, so a runner crossing the stands is not drawn sitting.
- **`TheListPeople`**: nobody held by the hunt is sent back to their place; Mel holds the camcorder through the hunt.
- **`TheList`**: the town's tick runs the hunt; a hand on a bolted door strikes it (the slasher) or is told it is bolted.
- **`TheListBody`**: `felled`, the body put down by one of the five, knocked back, with their cue; the race of a hidden name's heart;
  a bolted steel door struck five times (`STEEL_STRIKES`).
- **`TheListCommands`**: `/ghosthouse test the_list hunt` and `hunt off`; the status line names each runner's state.
- **Client** (`LiteraryRenderers.hunted`): running, the body leans forward and the arms swing wider; hiding, sat on the floor with the
  knees up, the arms round them and the head down, breathing (`CastModel.low`); cowering, down on the heels, bent over, the arms up over
  the head.
- **Sound** (`tools/generate_the_list_audio.py`): a scream, synthesized from a rising and falling voice with breath, subtitled
  "Someone screams".

## Tests

`TheListTests.theHunt` (new, batch `the_list_hunt`), with a second reader:

- The compass's words. The body given, the hunt begun: all five sit, calm.
- The slasher comes within hearing of Deek and Wes on their bench: they run, and the scream sends Mel and Lesley; Shannon does not.
  Deek is up, standing, running at the running pace. Each says their own words. The second reader hears the scream.
- The second reader is told, for each of the four, from which way they are yelling for help; the slasher is told nothing of it.
- Each hides at the end of their first way, crouched. The field house's steel door is shut and bolted, both leaves shut; the press
  box's is bolted, and the slasher near it is told and hears the bolt. Mel keeps the camcorder.
- Mel's heart, hidden, races in the slasher's ears.
- The second reader's hand on the bolted door is told it is bolted, and it stays shut.
- The slasher strikes it: three blows and it holds; at the fifth it springs, each blow with its own cue, and the steel door stands open.
- In the showers, Deek is found and fights back: the slasher is down with what Deek did and his words, and he runs for his second place
  while Wes stays hidden. Deek hides again; the slasher gets up; found again, Deek cowers with his words, and does not fight.
- Come upon at the bus bench, Shannon runs to the stand: Jenna lets her in, the stand's door is bolted, and Jenna tells her to get
  down.
- Ended, the five are back at their places, seated and still, and every door is shut, unbolted and of what it was built.

`TheListTests.theTownsPeopleAtTheirPlaces` and `nightOneTheSeedHouse` failed in run 1730 (aac28a42) in the full suite, though the
list's own job passed: the field, the pasture and the gin had been unloaded under them by the cases before. Each now holds the ground
round the places it uses and waits for it to load, and so does `theHunt`.

## Seen in the frames

Thirteen frames at eye level were looked at after this was pushed: the run across the field from three places, each first hiding place,
the field house's bolted door from outside, Deek cowering from the front and the side, and Shannon in the drive-in stand.

- Hidden and cornered, they read as standing. The game's own crouch only bends the back a little, and from the front Mel under the stands
  and Deek in the locker room were at full height. Hidden, they sit on the floor now with the knees drawn up, the arms round them and the
  head down; cornered, they are down on their heels, bent over, with the arms up over the head.
- Wes faced the coaches' desk from beside it and his head went into it. He sits with his back to it now.

## Every new line

Speech, over the speaker's head as their name. Running:

- Deek: "No. We saw that door shut."
- Wes: "It's the sack. Deek, it's the sack!"
- Mel: "Lesley! Get down off the top!"
- Lesley: "Mel, run!"
- Shannon: "I said open it! I told them!"

Yelling for help:

- Deek: "Coach! Somebody get the coach!"
- Wes: "Hold the door! Hold it!"
- Mel: "Somebody call somebody!"
- Lesley: "Up here! Help! Up here!"
- Shannon: "Jenna! Jenna, open up!"

Fighting back:

- Deek: "Get off me!"
- Wes: "It wasn't me! It was Deek's idea!"
- Mel: "Back up. I'll swing it again."
- Lesley: "Don't. Don't!"
- Shannon: "I didn't touch it! I said open it!"

Cowering:

- Deek: "Okay. Okay. What do you want?"
- Wes: "Please. Please."
- Mel: "Here. Take the camera. Take it."
- Lesley: "I wasn't even there. I was by the truck."
- Shannon: "I'm sorry. I'm sorry."

Jenna, in the stand: "Get in. Get in!" / "Get down. Stay down."

Grey cues on the action bar, to the slasher, put down:

- Deek: "Deek comes up off the floor into you. A shoulder, and you are down."
- Wes: "Wes swings wild and catches you. You go down."
- Mel: "The camcorder catches you across the sack. You go down."
- Lesley: "A fistful of dirt through the sack's eyeholes. Something takes your knees."
- Shannon: "Shannon shoves with both hands. The floor comes up."

To the slasher, near a door bolted: "Steel slams. A bolt shoots home." / "A door slams. The lock turns."

To the slasher, striking a bolted steel door, a blow at a time:

1. "The steel door booms. It holds."
2. "It booms again. The frame shifts in the block."
3. "A hinge pin starts out of its knuckle."
4. "The bolt bends in its keeper."
5. "The bolt tears loose, and the door swings in."

To anyone else's hand on a bolted door: "Bolted from the other side."

To every reader in town but the slasher: "Somewhere to the %s, %s is yelling for help." (a compass direction and a name).

Subtitle: "Someone screams".

For operators:

- "The hunt is on: the five are at their places and run when they see you. /ghosthouse test the_list hunt off ends it."
- "The hunt is over; the five are back at their places and the doors put back."
- "Go through the back door into Lamesa first: /ghosthouse test vignette the_list places its door."
- "Spectators and the dead are not given the body."
