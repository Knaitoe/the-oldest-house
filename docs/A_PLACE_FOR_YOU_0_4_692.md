# A Place for You: the letter and the porch (0.4.692)

The eighth slice (the first part of item 7 of `docs/A_PLACE_FOR_YOU_PLAN.md`): the first two consequence branches, each following from
what the reader did in the back passage (0.4.690).

Code: `PlaceForYouLetter` (new), hooks in `PlaceForYou` (where people stand, the sink chapter waiting, the evening's clock, Kit going,
talking, the right-clicks, Mara's door, how people walk), `PlaceForYouSite` (the way through the ground floor), `PlaceForYouBack`
(Kit's way out), one carried thing (`LETTER`), the art in `tools/draw_place_for_you_art.py` and the account in `PlaceForYouTexts`.

## What happens

### The letter

- **When.** The reader agreed to keep the back way quiet, then told Mara before the bag was out (`BrokeQuiet`), and the bag is now
  outside, all while Mara is at the sink.
- **Kit.** Back from the gate, Kit waits by the back door and calls the reader over now and then ("Back here. Please."). Close enough,
  Kit says the letter that was wrapped round the bag's clasp is gone, that Kit heard Mara on the stairs while the bag was by the door,
  and that the address was on it.
- **Three answers.** Get it back; she needed to hear it from somebody; go without it and ask when you get there.
- **Fetched.** Mara's door stands open. Her top drawer holds the letter, under the work shirt the reader saw there if they have been
  in her room with her. Carried down and put in Kit's hand, Kit holds it open, says it is the one and will be kept closer this time, and
  puts it away. Mara's
  door shuts again once the reader is out of her room and not looking at it.
- **Supper waits** while Kit has something to say about the letter or the reader is fetching it.
- **Left.** If Kit goes before the letter is settled, it stays in the drawer.
- **What Kit says going.** "I'll write. I've got the address on me now." if the letter came back; otherwise the old line.

### The porch

- **When.** The reader told Mara openly, with Kit there (`ToldOpen`), and the confrontation was not Kit's alone.
- **Kit about to go.** Mara has the bag. She carries it out onto the porch and stands beside the door, leaving the path clear; the front
  door and the gate stand open, and Kit waits at the gate with the road behind. The porch begins once both are there and the reader is
  near.
- **On the porch.** Mara says she brought it round from the back before it got damp; Kit says they will take it from here. Then what
  she does follows what the reader did with her earlier:
  - **Heard her out** (her room told, or her promise answered with what Kit wants): she goes down the path and puts it in Kit's hands.
  - **Promised company:** she asks the reader to tell Kit to wait till morning. Told so, Kit says no and goes without it, and Mara
    takes the bag back in to Kit's chair. Told to give it over, she reminds the reader of the promise and gives it.
  - **Otherwise:** she stands on the porch holding it and says she can't go past it. Kit comes up the path and takes it, or the
    reader carries it down.
- Kit goes by the front gate with the bag, whichever way the back passage opened.

## Walking through the house

Found while taking the porch frames: anyone in the house who walked while the reader could see them went in a straight line, so Mara
stuck in the front doorway and Kit against the kitchen table, and the same held for Kit, Mara, Alex and Ren going out to the yard in
the open house. Now whoever walks goes by the gaps: up from a chair at the table's front row into the strip of floor in front of it,
through the kitchen or sitting room archway, inside the front door, out onto the porch. While the reader can't see them they skip
ahead through the rooms the reader can't see into, and only the part in view is walked. Teleporting while the reader sees neither them
nor where they are going is unchanged.

## With more than one reader

Each reader's evening is a copy of its own: Kit, the letter, Mara's drawer and door, the porch and the bag are that reader's own, and
so is the record of what was said and done. A reader who neither broke the quiet nor told Mara openly sees neither.

## Art

Drawn at sixteen pixels by `tools/draw_place_for_you_art.py`:

- Kit's letter, carried (`letter`, `pfy_carried` 10): lined paper folded in four, its creases crossing, Kit's name pencilled on the
  outside fold, a corner worn soft, a bend and a rust mark where it was wrapped round the clasp.

## New lines

Every line below is new in this release.

**Cues**

- "Upstairs, Mara's door is open." *(new, flagged)*
- "Mara's room." (her door, while the letter is being fetched; the same words as 0.4.689's) *(flagged)*
- "In the top drawer, under the work shirt, Kit's letter, folded in four. You shut the drawer." / "In the top drawer, under a folded
  shirt, a letter folded in four with Kit's name on it. You shut the drawer." *(new, flagged)*
- "Mara has Kit's bag. She takes it out onto the porch." *(new, flagged)*

**Narration in the panel**

- "Mara goes down the path and puts the bag in Kit's hands." *(new, flagged)*
- "Mara holds the bag out, and Kit comes up the path and takes it." *(new, flagged)*
- "Kit comes up the path and takes the bag. Mara lets go of it." *(new, flagged)*
- "You take the bag from Mara and carry it down the path to Kit." *(new, flagged)*

**The reader**

- "I'll get it back." / "She needed to hear it from somebody." / "Go without it. Ask when you get there." *(new, flagged)*
- "Go on." (the letter returned) *(flagged)*
- "Thank you, Mara." / "Kit, write to her." / "Say nothing." *(new, flagged)*
- "Kit, wait till morning." / "Give Kit the bag, Mara." *(new, flagged)*
- "Kit, come and take it." / "Carry the bag down to Kit." *(new, flagged)*

**Kit**

- "Back here. Please." *(new, flagged)*
- "The letter's gone. I wrapped it round the clasp, and it isn't there." / "I heard her on the stairs while the bag was by the door." /
  "The address was on it. The room above the workshop." *(new, flagged)*
- "Her top drawer. That's where she puts things." / "Her top drawer." *(new, flagged)*
- "Maybe she did. It was still mine to say." / "I'll ask at the workshop. They'll know my name or they won't." *(new, flagged)*
- "I was always going to have to ask." *(new, flagged)*
- "That's it. That's the one." / "I'll keep it on me this time." *(new, flagged)*
- "I'll take it from here." / "I'll try." / "No." / "Keep it, then." *(new, flagged)*
- Going: "I'll write. I've got the address on me now." / "I'll manage." *(new, flagged)*

**Mara**

- "I brought it round from the back. It was going to get damp." *(new, flagged)*
- "Here." / "Go on, then. Before I'm sensible." / "Don't. Go on." *(new, flagged)*
- "Tell Kit to wait till morning. Kit will listen to you." / "You said you'd sit with me." / "...Here." *(new, flagged)*
- "I can't go past the porch. I've tried." *(new, flagged)*

**The account**

- After the back passage: "When the bag was out, Kit's letter was gone from the clasp. Mara had put it in her top drawer. I went up and
  got it back, and gave it to Kit." / "When the bag was out, Kit's letter was gone from the clasp; Mara had taken it upstairs. I said
  she had needed to hear it from somebody. Kit said it was still theirs to say." / "...I told Kit to go without it and ask when they
  got there." / "When the bag was out, Kit's letter was gone from the clasp. Mara had taken it upstairs, and it was still in her drawer
  when Kit went." *(new, flagged)*
- In place of "Kit went out through the gate with the bag.": "When Kit went, Mara carried the bag out onto the porch, and Kit waited at
  the open gate." then one of "She went down the path and put it in Kit's hands." / "She asked me to tell Kit to wait till morning, and
  I did. Kit said no, and went without the bag." / "She asked me to tell Kit to wait till morning. I told her to give Kit the bag, and
  she did." / "She said she couldn't go past the porch. I carried the bag down the path to Kit." / "She said she couldn't go past the
  porch. Kit came up the path and took it from her." *(new, flagged)*

**Changed**

- Alex is the man (`docs/A_PLACE_FOR_YOU_0_4_257.md`); three account lines had him as "they": "I walked with him as far as the road."
  / "He asked if I wanted to walk a while. I said no, and he said that was a whole answer." / "Alex tipped the last of the jug into my
  glass before his own." *(changed, flagged)*

## Cases

- `PlaceForYouTests#theLetterTakenUpstairs` (new): the quiet agreed and broken with the key; the bag out; the letter gone and supper
  waiting; Kit's words and three answers; her door open and left open when clicked; the letter from her top drawer before supper; her
  door shut once the reader is downstairs; the letter back in Kit's hand; settled; the account.
- `PlaceForYouTests#thePorchWithTheBag` (new): told openly; on to the house opened; Mara takes the bag out and the front door stands
  open, Kit not yet gone; the reader in the yard facing the house, so Mara and Kit walk out in view; Kit at the open gate; on the porch, the threshold and two answers; carried down; the bag with Kit, Kit going by the front; the
  account in place of the plain sentence; the other two stances' sentences.

## Results

Locally: the fourteen `PlaceForYouTests` cases and the cast and together conduct checks for A Place for You, 16 of 16. Eye-level frames
of the porch from the yard, from the hall, of Kit at the gate and of Mara by the door. The first frames showed Mara stopped in the
front doorway and Kit against the kitchen table, both walking in straight lines; with the walk through the gaps (above), Mara stands
on the porch beside the door, under the porch light, and Kit waits at the open gate facing the house. Kit's letter was checked at
sixteen times its size: the first drawing had a black corner where the worn corner should be, the name read as a blot and the rule as
a bar; all three were redrawn.
