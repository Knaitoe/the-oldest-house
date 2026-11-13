# The Pell house 21: after a failure (0.4.543)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its endings
and, since its twentieth slice, its aftermath in a later hall (`docs/PELL_HOUSE_AFTER_0_4_542.md`). This is its twenty-first: what follows
a reader through the House after the house has failed them. From the proposal's "Failure: Tilly answers":

> - **For a character Tilly** (solo, or co-op with a neighbour): [...] later in the House, now and then, there's a cane tapping behind a
>   door that will not open, and a voice that answers to a name that isn't hers.

and from "Being framed":

> - **Carried forward,** by the owner's decision:
>   - the entries made during the visits stay in the journal for good, whichever way it ends;
>   - after a failure, new false entries keep coming, rarely (at most one a House day), each a twisted line about a real thing the reader
>     did in a later vignette, in their own hand;
>   - after a success they stop, but the old ones stay.

The torn false page that turns up again (in Gail's folder on the last visit; pinned to a wall in a later hall) is the next slice.

## The cane behind a door that will not open

- **Whose:** a reader whose morning came after Tilly answered, and nobody else. Nobody else hears it, even standing beside them.
- **Where:** in an ordinary hall of the House, behind a resident's door: one the House keeps locked ("The door is locked. Someone lives
  here."). So it is a door that will not open, and never one the reader needs to get on.
- **When:** now and then. Each second the reader is in an ordinary hall, one chance in forty that it begins behind a resident's door
  between three and ten blocks away; and once a House day at most.
- **What:**
  - "Behind one of the doors, a cane is tapping." The cane taps its way along the far side of the door, heard from there.
  - When the reader comes to the door, or after ten seconds if they do not, a woman calls "Nessa?", and a child's voice answers her with
    the words Tilly answered her with in the pool: "Yes, Mum."
  - Then the cane goes away from the door, further in and fainter, and there is nothing.
- **Trying the door** stops it at once: the door stays locked ("The door is locked. Someone lives here."), and the tapping stops.
- **Knocking** stops it too, and nobody knocks back (a resident otherwise knocks back twice).
- It stops if the reader goes more than twenty-four blocks away, or the door is opened some other way.

## The entries go on

- **Anywhere in the House:** an entry still due when the reader has left the house goes in wherever they are in the House, as it would
  have at the house: in their own hand, never on their words, never on a page they have open, never on a page begun outside the House.
- **The last night's entry:** a reader who slept in the garage the last night, and whose morning came after Tilly answered, has that
  night's entry due ("I scuffed the line by the drive on purpose, to see what she'd do."). After a success nothing new is due.
- **Later stories:** after Tilly answered, each story the reader resolves later leaves a line due: what they did there, told as if they
  had done it worse, in their own hand. One goes in a House day at most; if more than one story is resolved before it can, the latest is
  the one that goes in. After a success nothing new comes, and the old entries stay.
- **The lines** are one for each Witness story but the house's own (87): the thing the reader did there (its Witness line), twisted. For
  example, after the trailer: "I knew whose voice it was at the door. I kept it shut anyway."; after the buzzer: "I pressed the buzzer.";
  after the dare on the ice: "I dared her out onto the ice." A story added later needs its line too (`PellTexts.forged`; the case below
  checks every story has one).
- **Nothing tells the reader it happened.**

## What each reader keeps

- `CaneDay`: the House day the cane last came.
- `ForgeNext`: the latest later story whose line is due; `ForgedDay`: the House day a line last went in; `Forged`: the stories whose
  lines have gone in, in order.

## How it is made

- **`labyrinth.PellAfterlife`** (new): the cane (`due`, `inHall`, `door`, `begin`, its steps, `tried`); and the tick that runs, once a
  second, the entries away from the house for every reader in the House (`PellEntries.anywhere`).
- **`PellEntries`:** `failed` (Tilly answered; the older sibling taken away by Gail waits on the owner's open question 2), `resolved`
  (from `WitnessAccount.resolve`, for every story but the house's own), `anywhere`, `forged` (for tests).
- **`PellDawn`:** after Tilly answered, the last night's entry is due for a reader who slept in the garage.
- **`LabyrinthDoors.use`** and **`DoorSounds.knock`:** a hand on or a knock at a resident's door where the cane is, for that reader, stops
  it; the knock gets no answer.
- **`PellTexts`:** `forged` (the 87 lines); `CANE_BEGINS`, `CANE_CALL`, `CANE_ANSWER` (built from `TILLY_ANSWERS`).
- **Sound:** `pell.cane`, three takes of a rubber cane tip on a floor, heard through a door (`tools/generate_pell_audio.py`; subtitle
  "A cane, tapping").

## Decided here (any of it can move)

- The cane is behind a resident's locked door, so the door "will not open" without ever being in the reader's way.
- One chance in forty a second in an ordinary hall, once a House day; ten seconds of tapping before the call if the reader stays away.
- The voice answers with "Yes, Mum.", Tilly's answer in the pool.
- A knock is answered by the tapping stopping, and nothing else.
- Only the latest later story waits for the next House day; earlier ones not yet written are passed over.
- A reader who was part of a shared story some other way than acting (a witness, say) gets the same line: the house puts the act in
  their hand all the same.
- Every one of the 87 lines (`PellTexts.forged`); the owner's text editor (#538) is where they can be changed.
- After a success, no entry is due for the last night in the garage.

## Not yet

- The torn false page that turns up again: in Gail's folder at first light on the last visit, and pinned to a wall in a later hall.
- Gail's third mark and the older sibling taken away (open question 2), which would also count as a failure here.
- A reader playing Tilly (co-op), and the kids next door.

## Checked

- `PellHouseTests.afterTillyAnsweredWhatTheyDoLaterGoesIntoTheirJournalTwisted`: two readers whose endings have landed, each with a
  journal, Tilly answered for one and out with the other. Each resolves a later story (the trailer). Into the first one's journal, in
  their own hand, goes the trailer's line; the other's stays empty. A second story the same House day waits; the next House day it goes
  in after the first. Every Witness story but the house's own has a line.
- `PellHouseTests.afterTillyAnsweredACaneTapsBehindADoorThatWillNotOpen`: two readers a few steps from a resident's locked door. The cane
  is due for the reader Tilly answered for, not the other; the door near them is the one found. The cane taps behind the door, for that
  reader alone, and they are told so; at the door, "Nessa?", then "Yes, Mum."; then it is over, and not due again that House day. The
  next day, trying the door stops it and the door stays shut; knocking stops it and nobody knocks back.
- The first-light case after Tilly answered: the reader who slept in the garage that night has the last night's entry due, and the other
  has none.
