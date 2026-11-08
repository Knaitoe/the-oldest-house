# Last Weekend at Pinehook, slice 1 (0.4.364)

The owner's multiplayer cabin weekend (`docs/PINEHOOK_PROPOSAL.md`, for two to six readers), built in slices. This first slice is the proposal's own:

> *"The most useful first slice is arrival, one fake attack, an early unmasking branch, and the real attempted attack on Jude. It should already demonstrate that a role can be funny, competent, and wrong in the same evening."*

It covers:

- the cabin, the shed, the lake, the dock and the boathouse;
- the four people;
- the parts offered at arrival and the thing each one hands over;
- the evening as a party;
- the fake attack;
- the early unmasking;
- Nina's attempt on Jude in the shed, with its three outcomes;
- the turn.

After the turn the night holds where it is. The parallel scenes, the puzzles, the crisis, the outcome families and the accounts are the next slices'. The weekend is not dealt yet, and it adds no Witness source until it can be finished. Until then the operator's test door reaches it.

Layout 119 appends slot 221. Protocol 158 adds:

- the cabin's fittings;
- eight carried things;
- five sounds.

Nothing standing is rebuilt.

- **Names:** `PINEHOOK`, `Pinehook`.
- **Kind of place:** a literary place outside, built whole by `PinehookSite`.
- **Box:** x -36..36, y -5..18, z -96..1.
- **Clock:** dusk (scene clock 3).
- **Story id:** `pinehook`.
- **Place name** (in staircase prose): "the cabin at Pinehook".

## The place

- **The House's door** opens out of the back of a woodshed onto the end of a gravel drive. The drive runs north to the cabin. The general store and the lane are behind; Len walks up the lane from the gate.
- **The cabin** (x -8..8, z -20..-34) stands with its porch to the drive. A wall at z -28 divides it.
  - **The porch:** the guest book's stand, which is the source, with Casey's invitation on it. The cooler stands on the porch at the start.
  - **The living room:**
    - the stereo, with a tape deck and Casey's records;
    - the television on a cabinet, with a tape deck under it;
    - the shelf of rented films;
    - the hearth;
    - the front window, where the fake attack knocks;
    - the ending's lectern, on the west wall, unused in this slice.
  - **Behind the dividing wall:**
    - the kitchen, with the stove, the counter and the cooler's place;
    - the bathroom, with the first-aid kit on its wall;
    - the bedroom;
    - the back door, with the breaker box beside it.
  - **The lights:** seven lamps. The lamps, the stove, the television and the stereo all go off together when the power goes.
- **The shed** (x -20..-15, z -38..-43) stands in the pines west of the cabin's back door. Jude's duffel is in it. Behind it lies a canvas bag somebody threw down.
- **The beach** runs along z -46..-49. The lake runs from z -50, five blocks deep.
- **The dock** runs out to z -64, with a T across its far end. Its far end is what you see from the back step.
- **Len's boathouse** stands beside the dock, padlocked, with a sign to keep out.
- **The way on** is a door in a pump house at the west edge of the clearing.

The fittings are one block, `pinehook_fitting` (ten kinds, a facing, four stages), drawn with 34 textures by `tools/draw_pinehook_art.py`.

## The people

All four are actors with their own painted skins:

- **Casey** (phase 117);
- **Jude** (118), masked as the killer (119) and in the coverall without the mask (120);
- **Nina** (121);
- **Len** (122).

They walk real routes through the cabin's doors and round its walls. They speak in the scene, never in chat.

## The parts

Casey greets each reader who comes within six blocks of him on the porch (GREET, or GREET_LATE once the evening has begun) and offers the parts openly. Each part's reply is the reader's own line. The part is a fiction role, recorded as `Part`; nothing here reads it as anything about the reader.

| Part | What Casey hands over |
|---|---|
| The Athlete | Nothing in hand: "Then you're on the cooler." |
| The Skeptic | A camera and a torch. |
| The Charmer | A mixtape, and the music is theirs. |
| The Wildcard | A battered portable player, left in Casey's car last summer. |
| The Horror Fan | Nothing more: they brought the rented film themselves. It is on the shelf if nobody else has it. |
| The Responsible One | A first-aid bag. |
| None of them | "Suit yourself. Everybody's somebody by Sunday." |

- **Repeats:** parts may repeat.
- **Nothing needs a part:**
  - anyone can lift the cooler;
  - the rented film is on the shelf;
  - the first-aid kit hangs in the bathroom;
  - any reader can predict the power, find the elastic, see the prop knife or pull the mask off.
- **When the evening starts:** once every reader present has a part, or 600 ticks after the first one chose.

## The evening

The evening is the party being a party. Everything in it is an opportunity, recorded on the reader's own record. None of it is required.

- **The cooler.**
  - It is heavy: carrying it slows the carrier.
  - Casey shows the way to the kitchen.
  - Set down in the kitchen, it brings Casey's secret, told quietly to whoever carried it: Jude is going to give everybody a scare after dark.
  - The reader can keep it, call it a terrible idea, or ask who Jude is.
- **The music.** The stereo plays the mixtape (if carried), one of Casey's records, or the radio. Len, once he is here, asks for it to be turned down.
- **The film.** The television plays Casey's film, or the rented horror film if someone carries it. Casey complains about the rented one, and Jude says he was in one like it.
- **The burgers.** The stove cooks them in 600 ticks. Then they can be served.
- **The dock.** Walking out to its end shows the lake and the padlocked boathouse.
- **Asking Casey** "Bet you the power goes out tonight." records a prediction that pays off at dusk.
- **Jude:**
  - can be asked what he has been in, or mocked ("Should I know your face?");
  - once the reader knows the plan, can be asked whether the owner knows he is here: "Casey said he told him. Why?"
- **Nina:**
  - a reader can sit with her;
  - before Len comes, she can be asked whether she has been here before;
  - a reader who saw her go quiet can ask her what Len said: "He said the lake's low this year. That's all."
- **Len:**
  - walks up the lane at 1800 ticks. Every reader who is watching Nina as he arrives sees her stop talking halfway through a sentence;
  - his warning is about the boathouse and the dock. A reader can take him seriously or say "Sure, Len."
- **Behind the shed:**
  - the bag holds a length of elastic with a buckle at each end, cut to fit a mask;
  - Jude's duffel holds a rubber knife painted to look like steel, and stage blood.
- **Idle talk:** Casey, Jude and Nina take turns at idle lines every 400 ticks. Nina is silent once Len has come.

### Dusk

- **When Jude leaves.** He says he is going to look at the lake and walks out to the shed. This happens once the cabin has settled (the cooler in the kitchen, something playing, a film chosen, and Len here) and the evening is at least 3200 ticks old. Otherwise it happens at 5600 ticks.
- **The scare.** 400 ticks later:
  - Casey trips the breaker, and the lights, the stereo and the television die.
  - Readers who predicted it are told the lights went out the way they said.
  - Casey goes to the box by the back door, and Nina goes to the bathroom.
  - A reader who followed Jude to the shed watches him put the costume on.

## The fake attack

- **The window.** Jude, masked and in the coverall, appears at the front window 60 ticks into the dark and raps on the glass three times.
- **The back door.** He goes round the cabin to the back door and rattles it.
- **The retreat.** At 340 ticks he retreats to the shed.
- **What readers see:**
  - Every reader who sees him records the mask.
  - A lit torch within ten blocks shows the face is rubber.
  - The camera's flash, on him, records the mask full on.

### The early unmasking

- **Pulling the mask off.** Any reader within reach of the masked figure can pull the mask off. It is Jude.
- **Casey owns up** ("Okay. Okay! Surprise. That's Jude. It was a bit. It was a good bit.") and puts the power back on.
- **An awkward talk.** The evening becomes an awkward talk with Casey instead. A reader can ask:
  - whether he was going to tell them;
  - whether he cut the power for this;
  - or tell him it was actually good.
- **Jude goes to change.** 100 ticks later Jude goes to the shed to get out of the costume.

If nobody unmasks him, the power comes back when Jude reaches the shed, and Casey says it is old wiring.

## The shed

Jude changes in the shed for 600 ticks. Nina leaves the bathroom 160 ticks after he goes in and walks out of the back door along the path to the shed.

- **Somebody is with Jude.** She waits behind the shed. Readers who see her there see someone standing very still between the pines.
  - If Jude is ever alone, she goes in.
  - If he never is, she gives up when he has finished changing, or after 600 ticks. She walks back to the cabin.
  - The outcome is **prevented**.
- **Nobody is with Jude.** She goes in, and the door shuts. A reader watching the door sees someone go in. There is a crash at once, and another 60 ticks later.
  - **Witnessed.** A reader who gets into the shed in the 100 ticks while she is at him stops her and sees who it was: Nina, a knife in her hand. She goes out through the back window.
  - **Wounded.** Otherwise she takes the costume. Jude comes back to the cabin cut, slowly, without it, and the duffel is left open and empty but for the rubber knife.

The interruption is real. If a reader got there, Jude is not wounded behind their back.

## The turn

The turn plays when Jude reaches Casey on the back step, where the far end of the dock is in view.

- **Wounded.**
  - The lines:
    > *Jude: Who else did you hire?*
    > *Casey: Nobody.*
    > *Jude: Then tell me who was in the shed.*
  - As he asks, a figure in the coverall and the rubber mask walks across the far end of the dock, without hurrying. Every reader who looks sees it. A flash at it records the attempt, too far to be sure.
  - The figure is gone with a splash.
  - Later, Nina comes back into the living room: "What's going on? I was in the bathroom."
  - Jude says what happened. Casey says he probably tripped. Jude says he didn't.
  - The first-aid bag or the bathroom kit, used on Jude, dresses the cut.
- **Witnessed.** Jude says it was Nina, with a real knife. Casey says it can't be. Jude asks where she is, then.
- **Prevented.** Jude thanks whoever kept him company; the shed's creepy in the dark. Casey asks if everyone is okay.

Then the night holds. The next slice begins there.

## Multiplayer

- **One shared scene.** There is one run for everyone in the cabin; no reader's choice is the host's.
- **Private conversations.** The secret goes only to whoever carried the cooler in. Each reader's conversations with the cast are their own.
- **The evening's clock** is shared. It runs while anyone is in the cabin's world, whoever that is.
- **Each reader's own record** keeps:
  - the part they played;
  - what they did: carried, played, chose, cooked, served, walked;
  - what they saw: Nina going quiet, the elastic, the prop knife, the mask, the rubber, the unmasking, Nina lurking or going in, the attack, the figure;
  - what they said, and what they were told;
  - the outcome;
  - whether they dressed Jude's cut.
- **The weekend's things** stay in the cabin's world. A reader who leaves the scene leaves them behind.
- **Lights:** lit torches are moving lights, one per reader.
- **Reset:** after 200 ticks with nobody in the cabin, the cast, the fittings and the lights reset.

## The account (provisional)

`PinehookTexts.account` writes a reader's evening from their own record:

- the part they said they'd be;
- what they did;
- whether they kept the secret;
- Nina going quiet, and what she said if asked;
- the elastic;
- the mask, the rubber, or pulling it off;
- the shed's outcome as that reader knew it;
- dressing the cut.

It ends "That was the first night." The last slice writes the whole weekend's account. Nothing is earned yet.

## Art and sound

- `tools/draw_pinehook_art.py` draws the fittings, the eight carried things and their item models.
- `tools/paint_character_skins.py` paints the six skins (`literary_pinehook_*`). The killer's face is a new `slasher` mask: pale rubber with crooked eyeholes. No existing skin changed.
- `tools/generate_pinehook_audio.py` makes five original sounds, with these subtitles:
  - "Knuckles rap on glass";
  - "The power cuts out";
  - "Something crashes in the shed";
  - "A camera flashes";
  - "A tape slots into the deck".
- Both tools have `--check` modes, run by the quick gate.

## Tests

`PinehookTests` (literary namespace) has three cases:

1. **`theWeekendOpensAndThePrankGoesWrong`.** Two readers (the Skeptic and the Athlete) play the whole slice:
   - the cooler and the secret;
   - the music, the film and the prediction;
   - the duffel and the bag;
   - Len, and Nina going quiet;
   - asking Nina, and taking Len seriously;
   - Jude leaving;
   - the scare, and the prediction paying off;
   - the unmasking, and the awkward questions;
   - the shed, ending wounded;
   - the turn, and the figure on the dock;
   - Nina back;
   - the kit on Jude's cut;
   - the account's lines;
   - and that nothing was earned.
2. **`someoneStaysWithJudeAndNinaGivesUp`.** A reader stays in the shed; Nina lurks and gives up (prevented).
3. **`aReaderGetsIntoTheShedInTime`.** A reader gets into the shed while she is at him (witnessed).

## No Witness change

80 / 60 / two kinds / three endings.
