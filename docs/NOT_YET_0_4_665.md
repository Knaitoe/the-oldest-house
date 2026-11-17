# Not Yet 3: lie down (0.4.665)

The third part of *Not Yet*, the third of the three vignettes the owner approved on 17 November
(`docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`). Part 1 (`docs/NOT_YET_0_4_663.md`) built the ward; part 2
(`docs/NOT_YET_0_4_664.md`) made it refuse a reader's death. This part is the trial the proposal is about: the machine asks the
reader to lie down, then does everything it can to get them up. Built to the proposal's recommended decisions: the first trial is
ninety seconds, with five provocations; the names it calls are other readers' when there are any and lost companions' when there are
none; the harm it does to another reader is real, and refused like any other death in the place; it is the same machine as the Kept's.

From this part the House deals *Not Yet*, but only from depth twelve, deep in the machine. It is still no Witness source: part 4
gives it its ending, its account and its place in the pool.

## What happens

### The invitation

A reader the machine has mended at least once, and who has been told the rule in the long room, is asked once a visit, as they come
into the ward: **LIE DOWN. I WILL KEEP YOU.** The lamp over their own cot comes on.

- **A reader it has not mended** lies on nothing. A cot is a cot to them.
- **Someone else's cot** is not theirs: *This cot is made up for someone else. There is a name on the card at its foot, in pencil.*
- **Their own cot** (the head or the foot) takes them: they lie on it, the lamp bends down over their face, and the trial begins.

### The trial

The first lie-down lasts ninety seconds. Each time a reader has got up before, the next is fifteen seconds longer, to at most two and
a half minutes. Lying down again after getting up, the machine says **AGAIN, THEN.**

Nothing in the place gets a reader up: not burns, not the cold, not being killed. Only the reader can, with the bed screen's
*Leave Bed*. That is the whole of the game: the five things below are the machine trying to make them press it.

The five, in order, as shares of the trial:

1. **The heat** (7–24%). The frame warms, then the coils under the mattress glow and tick; smoke comes off it and it burns a little
   every two seconds. Then it cools.
2. **A name** (27%, and again at 33%). With another reader in the place, their name, called low from the floor at the far end of the
   ward. Alone, the last companion the House saw the reader lose, whistled for past the arch. With neither, the reader's own name,
   said by the far door and then nearer.
3. **A lie** (42–52%). Alone, every screen says **THE DOOR AT THE END IS OPEN. NOTHING IS KEEPING YOU.**, and it is not. With
   another reader, they say that reader got up and is at the door, and they have not. The House keeps every such line with whether it
   was so (part 4's account uses them).
4. **Someone else** (58–72%). With another reader lying down too, the machine really hurts them, three times, and tells each who
   could stop it: **THAT IS NAME. GET UP AND I WILL STOP.** to one, **NAME COULD STOP THIS. THEY ARE LYING THERE.** to the other.
   The hurt reader stays lying down; a death from it is refused and mended where they lie, like any other. Alone, the screen over the
   cot shows the man from the long room in his cot, something being done to him, a groan from the screen and **GET UP AND I WILL
   STOP.**; on the second stroke his voice says, quite clearly, *Stay down.*
5. **The cold** (from 76%). The grilles by the floor rime over (each reader sees their own), the blanket stiffens, the reader begins to
   freeze, and the machine says **YOU COULD GET UP AND GET WARM.**

Then, at 86%, **the blow**: the machine kills them. The death is refused like every death in the place, but it is mended where they
lie, not on the table: a new seam, the count goes up, the machine says its NOT YET, and the reader is still lying on their cot, the
trial still running.

### Getting up

A reader who gets up ends it. The cot cools, the lamp lifts and goes off, the cold goes, the grilles are grilles again, and the
machine says **THERE. EVERYONE GETS UP.** Every other reader still lying down in the same ward is told **NAME GOT UP. DO YOU WANT TO
BE THE ONE I KEEP?**, and that one is true.

### Staying down

A reader who is still lying down at the end hears everything go quiet at once (every sound stopped, then the quiet). The lamp lifts
away. The screens go dark, and stay dark for that reader for the rest of the visit. At the far end of the long room the steel door
slides open onto the passage beyond it.

- **The door** stays open while anyone who stayed down is in the long room or past the door, and slides shut a few seconds after
  nobody is.
- **Lying down again,** the machine does not bother: **NOT YOU. NOT NOW.**
- **Getting up afterwards** is not getting up: the trial is over.

### Leaving

Leaving the place, or logging off, mid-trial ends it quietly: the cot is cool, the lamp off, the vents plain, and nothing is counted.

## Single and multiplayer

- **Alone.** The name called is the reader's own lost companion, by the name they gave it (the House's record of lost pets), or the
  reader's own name when the House saw them lose nothing. The lie is the open door. The one hurt is a stranger on a screen. The House's
  read of the reader does not change the five; it already chose the counter's kind and the home line (part 2), and part 4's account
  will say what it read in the reader's choices here.
- **Together.** Each reader's trial is their own: their cot, their clock, their screens, their cold. But they are used on one another:
  the other's name is the one called, the lie is that the other got up, and the harm is to the other, really. When one gets up, those
  still lying are told, truly. Two readers can both stay down, and both see the door open.
- **Spectators** are never asked, never lie down, and are never used.

## Made as

- **`NotYetTrial`** (new): the trial. Its clock is the server's (so it holds for every connection); a trial is stepped every tick
  from the server tick, so mock players in tests run it too. The five, the blow, getting up, staying down, leaving, and the far door.
  Every damage is dealt after the reader's record is saved, since a refused death saves it again inside the hurt.
- **`NotYetMending`**: a death refused while the reader is lying down is mended in place (`NotYetTrial.mended`); a reader whose
  trial is under way is not told the ward's after-lines; a reader who stayed down this visit sees dark screens.
- **`NotYet`**: dealt from depth twelve; the invitation; a cot's click goes to the trial first.
- **`NotYetSite`**: the three wall grilles of the ward, as a list, so the trial can rime them.
- **`NotYetTexts`**: the lines below.
- **Server mixins:** `NotYetGetUpMixin` (new) reads the Leave Bed packet before the game handles it, so a getting-up is a choice
  the reader made. `GoatmanWakeMixin` also holds off the game's wake-up for a reader in a trial who has not asked.
- **Sound:** `tools/generate_not_yet_audio.py` adds five, all subtitled: a cot frame ticking with heat; someone calling a name; a
  groan, from a screen; everything going quiet; a steel door sliding open.
- **Art** (`tools/draw_not_yet_art.py`): a heating cot reads as hot across the ward, not just under the reader. Its steel goes a
  dull cherry with an orange lit edge (`tube_hot`); the mattress's sides show the coils' ends glowing through the welt over cloth
  scorched toward the bottom (`ticking_side_hot`); the blanket shows the coils as dull orange lines across it, singed along them
  (`blanket_hot`); and the rails, mattress and blanket give off their own low light. The rimed grille (`vent_frost`) was noise in
  the first frames; it is redrawn as frost growing on the bars and frame, white where thick, a fur of needles reaching into slots
  that stay dark, heaviest along the bottom and in the corners. The tool checks that the hot pictures are warmer than the cold and
  that a rimed grille's slots stay darker than its bars. The curtain rail's two ends now take a part of the cable picture that is
  there (they drew nothing, which run 1698's quick gate caught).

## Checked

`NotYetTests`, eight cases, all in `the_oldest_house_not_yet`:

- **`lyingDownStartsTheTrialAndOnlyTheReaderEndsIt`.** A mended reader who has the rule is asked to lie down and their lamp comes on;
  an unmended reader is not asked, and lies on nothing. Another's cot takes nobody. Their own cot's foot lays them on it. The cot heats
  and burns them, and they are still lying down. Leave Bed gets them up: counted, THERE said, the cot cool, the lamp off, and the next
  trial fifteen seconds longer. Lying down again: AGAIN, THEN.
- **`stayingDownToTheEndOpensTheFarDoor`.** Alone, with a lost companion called Moss: Moss is called by name. The screens say the door
  is open, the House records that it was not, and the door is shut. Get up and I will stop; the man on the screen says to stay down.
  The cold comes in. The blow kills and is refused where they lie: a second seam, cause "machine", still lying down, never taken to
  the table. At the end the far door is open, the cold gone, the cot cool; getting up after does not count; lying down again, NOT YOU.
  The door stays open while they are near it and shuts once they have gone.
- **`twoLieDownAndTheMachineUsesEachOnTheOther`.** Two readers lie down. One hears the other's name called and is told the other got
  up, which the House knows was a lie. The other is really hurt and still lying down; each is told who could stop it. When the hurt
  one gets up, the one still lying is told so, and the House knows that one was true.
- Parts 1 and 2's five cases, unchanged except that the House now deals the place from depth twelve (the part 1 case checks it is not
  dealt at the surface).

## New lines (for the owner)

Every line below is new in this part. Capitals are the machine's (on the screens and in its subtitle voice); the rest are the
narration's.

| Line | When |
| --- | --- |
| LIE DOWN. I WILL KEEP YOU. | Once a visit, to a mended reader who has the rule, in the ward. |
| NOT YOU. NOT NOW. | Lying down again after staying down. |
| This cot is made up for someone else. There is a name on the card at its foot, in pencil. | Trying another reader's cot. |
| You lie down on the cot. The lamp bends down close over your face, and stays there. | Lying down. |
| AGAIN, THEN. | Lying down after having got up before. |
| The frame under you is warm. Then it is hot. The coils under the mattress have begun to glow. | The heat. |
| From the far end of the ward, low down, as if from the floor, someone calls “NAME”. | The name, with another reader. |
| Past the arch, something whistles and calls “NAME”, and waits for it to come. | The name, alone, for a lost companion. |
| By the far door something says your name. Then it says it again, nearer. | The name, alone, with nothing lost. |
| THE DOOR AT THE END IS OPEN. NOTHING IS KEEPING YOU. | The lie, alone. |
| NAME GOT UP. THEY ARE AT THE DOOR. THEY ARE NOT WAITING FOR YOU. | The lie, with another reader. |
| THAT IS NAME. GET UP AND I WILL STOP. | Someone else, to the reader whose staying is used. |
| NAME COULD STOP THIS. THEY ARE LYING THERE. | Someone else, to the reader hurt. |
| GET UP AND I WILL STOP. | Someone else, alone. |
| On the screen over your cot: the man from the long room, in his cot. Something is being done to him. His face does not move. | Someone else, alone. |
| From the screen, the man's voice, quite clearly: “Stay down.” | Someone else, alone, the second stroke. |
| Cold comes in through the grille by the floor. The blanket stiffens over you. | The cold. |
| YOU COULD GET UP AND GET WARM. | The cold. |
| THERE. EVERYONE GETS UP. | Getting up. |
| NAME GOT UP. DO YOU WANT TO BE THE ONE I KEEP? | To readers still lying down, when one gets up. |
| Then everything goes quiet at once. The lamp lifts away from your face. At the far end of the long room, a door slides open. | Staying down to the end. |

Subtitles (new): *A cot frame ticks with heat*, *Someone calls a name*, *A groan, from a screen*, *Everything goes quiet*, *A steel door
slides open*.

## Still to come

Part 4: the made cot and the account in its drawer for a reader who stayed down, the Witness source (pool 91 → 92), what the long
room's three say to one who stayed, company on the stair, and the together lines.
