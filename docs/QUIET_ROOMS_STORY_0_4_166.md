# The quiet rooms as a story, with locked doors — 0.4.166

The owner, about the five personality rooms:

> "The personality vignettes also shouldn't have exits; the house needs those bits of information"
>
> "The doors can also just be locked ... Until the vignettes are completed."
>
> "Make a story about the player. A line should trigger at the beginning and end of each personality vignette. I'm unsure the best way to handle this, it requires consideration. But the vignettes should feel harsher than simply popping in and out of a room."
>
> "the story should sort of 'welcome' the player into the house, end with the warning that this house has ghosts in it"

This version does three things:

1. It locks each room's doors until the House has what the room is for.
2. It frames the five rooms as one story the House tells about the reader.
3. It changes how a reader enters and leaves a room: a hard cut to black with a line, never a stroll through a door.

It also repairs the failures of run 1330 (below).

## The story

The House talks to the reader in the second person, as a host who has been expecting them. Each room is a moment of the reader's life as the House imagines it. Each room opens with the House setting the scene, and closes with the House taking what it saw.

- **The first room** opens with the welcome, before the room's own line:
  > Come in. Shut the door behind you; it sticks. Every house keeps a story about the people who live in it, and you've only just arrived, so I'll have to learn yours.
- **The last room** closes on the warning, after its own line:
  > You should know, before you go any further:
  >
  > **this house has ghosts in it.**

| Room | Opening | Closing |
| --- | --- | --- |
| Waiting room | You grew up the way everyone does, mostly by waiting. A clock, a row of chairs, and a door that would open when it was ready. | They called your name. They always do, in the end. |
| Bathroom | Some nights you came home late and worn through, and your hands still knew what to do before bed. | The light went out. You slept the way people sleep in a house that is listening. |
| Coatroom | Once you came in out of the rain, soaked to the skin, into a hall where nobody was looking, and dressed yourself. | That's how you'll look here. The glass will remember. |
| Shelter | There was a summer storm, and a roof over the water, and two people you hadn't found the nerve to talk to. | The song ended. Somebody always goes home before the rain stops. |
| Porch | And at the end of every day, somebody called you in for supper. | Welcome home. I've set a place for you at the table. |

The welcome's "it sticks" is the first hint that the doors are locked. The porch is the last room in the order, so its closing "Welcome home" leads straight into the warning. If the order changes (the kitchen-table room is to come first), the warning still follows whichever room is last.

The lines follow the House's rules:

- They never name what the House read.
- They never gender the reader, or name a colour or an age.
- A test checks every line for gendered, colour and age words.

## How a room begins and ends

- **Entering.** The door's fade lands on black, not on the room. The House's line comes up page by page in the dark, and only then is the room there, all at once. Before the first room the welcome comes first.
- **Leaving.** The room is cut off: the screen goes black at once, with no fade, and the room's own sound is heard in the dark:
  - the office door closing;
  - the bathroom's light switch;
  - the front door;
  - a low note from the jukebox;
  - the screen door slapping shut.

  The closing line follows, then the warning after the last room. The reader is taken on while the card holds, so the next hall is simply there when it lifts.
- **The card itself.**
  - It holds every reader in place.
  - Escape doesn't close it.
  - Nothing in the room moves on while it is up. The waiting room's forty seconds start after the opening card, not during it.
  - Each page holds for a beat plus the time to read it, at most ten seconds.
- **Private.** The cards, like the rooms, are each reader's own.

## Locked until complete

Both of a room's doors, the way on and the way back, stay locked until the House has its answer. Trying one gives "The door won't open." and a locked rattle. Once the room is complete, either door opens, and either way plays the closing line.

| Room | Complete when | Until then |
| --- | --- | --- |
| Waiting room | the receptionist has called the reader: forty seconds have passed and the reader has picked something up to pass the time | at the forty seconds, "It won't be much longer. Make yourself comfortable."; thirty seconds later, "Help yourself to a magazine while you wait." A first pick-up after the forty seconds gets three seconds of its own before the door clicks, so the call still reads what they are doing. |
| Bathroom | a routine item has been used at the mirror | the light switch only works the light. It no longer opens the bedroom, and a routine used with the light off is read like any other. |
| Coatroom | the reader has taken a coat or boots, or turned or covered the glass first | the curtain on the way on is shut and stops a body; it parts, with a rustle, the moment the room has its answer |
| Shelter | the song has played through and the reader has gone to someone, stayed between them, or walked out to the lantern | if the song ends with none of these, the jukebox plays it again |
| Porch | the whistle has been answered: in through the screen door, under the porch, down the road, or watching at the lit window | a whistle nobody answers is blown again five seconds after its window closes, and the supper voice calls again |

These replace the room rule "A reader can always go back the way they came" in `docs/HOUSE_READ_DESIGN.md`. If the operator turns a trait off, the rooms still complete: completion follows what the reader did, not whether the trait was read.

## The coats move with the legs

> "try to better anticipate player movement for the legs of the clothes if you can, so the clothes move with the legs?"

The wool and trench coats' skirt was one box fixed to the body, so the legs walked through it. It is now two halves, each hung from its own leg's hip, in the same place and size, so the skirt swings with each stride. Wherever the coat is worn the halves move with the legs: on the reader, in the coatroom glass and on the House's version of the reader.

## Run 1330 (0.4.164): what failed and the repairs

| Job | Failure | Cause | Repair |
| --- | --- | --- | --- |
| Goatman, build, live-multiplayer | the finished camper had no raised sills, iron-bar frames or porch rail; the build's exterior checkpoint and both clients' camper entry failed with it | 0.4.164's stall fix made the exterior pass skip any scene not fully loaded, including when a builder or fixture called it directly | only the once-a-second check waits for loaded chunks; a builder holding the scene decorates it at once |
| Literary | the Sheriff's escort back to the tape sometimes never ended | the escort ended only once the reader was within half a block of the Sheriff's own spot, and the Sheriff stands on it; pressed against him, the reader sometimes stopped short | the reader is let go a block short, at the near side of the tape |
| Literary | two Borrasca cases timed out waiting for their chunks | load in the full run; both pass alone | none needed |
| Literary | the Last Stop men did not take the returning reader | not reproduced: it passed on a second local run | its failure now reports each man's place and leg, the reader and the side door, for the next time |
| Exploration | the cold store's frost was measured too low | its timer started before the room's sections were loaded | it waits for them, then measures |
| Live-multiplayer | the uncanny tunnel attempt failed on one client | not reproduced | watched in the next run |

The local selection of native cases also accepts class names with digits (`Vignette0492Tests`).

## Tests and proofs

- **New:** the House's story has a line for every room, starts with "Come in", ends with the warning, never genders the reader or names a colour or an age, and holds each page between 2 and 10 seconds.
- **Coatroom:**
  - the door back is locked and the curtain stops a body until the reader dresses;
  - then the curtain parts;
  - leaving shows the closing line, with its sound, before the reader goes.
- **Waiting room, two readers:**
  - the one who picks up the phone is called after the forty seconds;
  - the one who does nothing hears the receptionist, and both their doors stay locked;
  - once they pick up the paper they are called three seconds later and taken on.
- **Bathroom:** after the routine the room is cut off with its switch and the closing line.
- **Bathroom, light first:**
  - the switch alone no longer opens the bedroom, and the door back is locked in the dark;
  - a routine used at the glass with the light off is read and completes the room.
- **Porch:** the way back is locked until the whistle is answered.
- **Shelter:**
  - its doors are locked before the song;
  - staying out by the lantern through the song completes it;
  - as the last room it ends on the warning, drawn as a title.
- **Entrance:**
  - the first quiet room, reached through the junction's real door, opens with the welcome and its own line;
  - each room's doors are locked before its answer.
- **Client proof:** three native pages, `build/house-read-lines/`:
  - the welcome;
  - the bathroom's closing line;
  - the warning.

  CI requires them.
- **Locally:** the house-read, tailoring and co-op suites pass (37 cases), along with the run-1330 cases above.

Protocol 92: one new client packet, the House's line. No layout or Witness change: 54 / 41 / two kinds / three endings.
