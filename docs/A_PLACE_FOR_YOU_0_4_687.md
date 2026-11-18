# A Place for You: keep a place (0.4.687)

The third slice (item 2 of `docs/A_PLACE_FOR_YOU_PLAN.md`), the proposal's E2:

> The reader hangs their picture back up and accepts the recurring evening. Mara is relieved. The meal is warm. A familiar person
> may sit beside the reader. The first repetitions are comforting. Then the scene shows the cost: a new question gets an old answer;
> an affectionate gesture resumes at exactly the same point; the prepared routine is waiting untouched again. Kit is not restored if
> they already left. A peer who refused to stay is not silently seated at the table. The reader cannot consent on everybody's
> behalf. The player can still physically leave the vignette.

Code: `PlaceForYouKeep` (new), hooks in `PlaceForYou` (the chapter `KEEP`, its marks, the fridge, talk), and the account in
`PlaceForYouTexts`. No new art: the picture on the fridge, the table, the chairs and the routine are slice 1's.

## What happens

- **The picture back up.** While the house is being opened, once the reader's picture is down off the fridge, a hand on the fridge
  again asks: "Put your picture back up." / "Leave it down." Put back, the evening is kept. If somebody was still knocking, the
  knocking stops. If the reader's chair had gone from the table, Mara pulls it back out. Mara says, above her head, that's where it
  goes, and the supper is still warm; if Ren had said what letting the past in would cost, Ren says they won't say it again.
- **What the house puts back out of sight.** The household's pictures as they were; the supper on the table; the hall chair back in
  the hall and the front door shut. Each only while the reader is not looking at it.
- **Sitting down.** When the reader sits in their own chair, somebody familiar sits beside them: the first of the household the
  reader sat with, if they are staying; otherwise whoever helped; otherwise Mara, Ren, Alex. Never Kit. Nobody who has said they are
  going. Alex already sits beside the reader's place; anyone else takes Kit's chair, or Alex's if Alex has gone.
- **The first repetitions.** Mara: "Eat. There's more." Then the familiar gesture, by who it is.
- **The cost, shown.** Mara is there to be asked something new: what they are doing tomorrow, where Kit is tonight (if Kit has gone),
  who was at the door (if somebody knocked). Whatever is asked, she answers with what she said when the reader came in. A while
  after, the gesture comes again, the same way, with the same words. Then, out of sight, the reader's things are laid out again in
  the bathroom, untouched; a reader who goes and looks sees it.
- **The end.** Mara: "Same time tomorrow." The evening is kept (`kept_a_place`), and the account waits in the mailbox as before.
  Nothing locks: the reader can walk out of the story at any point.

## With more than one reader

Each reader's evening is a copy of its own; the kept evening is that reader's alone. Nothing new is shared.

## New lines

Every line below is new in this release.

**Cues**

- "The fridge door, bare where your picture was." *(new, flagged)*
- "You put your picture back up on the fridge." and, if somebody was knocking, "The knocking stops." *(new, flagged)*
- "Mara pulls your chair back out from the table." *(new, flagged)*
- Mara's gesture: "Mara rests her hand on your arm while she talks." *(new, flagged)*
- Ren's: "Ren knocks your elbow with theirs." *(new, flagged)*
- Alex's: "Alex tips the last of the jug into your glass before their own." *(new, flagged)*
- "Laid out again, untouched. Ready for the same moment." *(new, flagged)*
- The ending: "Your chair is pulled out from the table. The supper is still warm." *(new, flagged)*

**The reader's replies**

- "Put your picture back up." / "Leave it down." *(new, flagged)*
- To Mara: "What are we doing tomorrow?" / "Where's Kit tonight?" / "Who was at the door?" *(new, flagged)*

**The household**

- Mara: "There. That's where it goes. Sit down, it's still warm." *(new, flagged)*
- Ren, if they had said the cost: "All right. I won't say it again." *(new, flagged)*
- Mara: "Eat. There's more." *(new, flagged)*
- Mara with the gesture: "I'm glad it's you at this table." *(new, flagged)*
- Ren with the gesture: "Told you she'd make too much." *(new, flagged)*
- Alex with the gesture: "Drink that before she fills it again." *(new, flagged)*
- Mara's answer to the new question: her own welcome line, repeated word for word (slice 1's).
- Mara: "Same time tomorrow." *(new, flagged)*
- Spoken to in the kept evening: Kit, "I'm still going."; Mara, "Sit down. It's still warm." then "Eat something."; Alex or Ren,
  "Sit with us." then "Mm?" *(new, flagged except "Eat something." and "Mm?", which are slice 1's)*

**The account** (for `kept_a_place`, after the confrontation and the door)

- "I had taken my picture down off the fridge. I put it back up." *(new, flagged)*
- "The knocking stopped." *(new, flagged)*
- "Mara said that was where it went, and the supper was still warm." *(new, flagged)*
- "[Name] sat down next to me." then, by who: "She rested her hand on my arm while she talked, and said she was glad it was me at
  the table." / "Ren knocked my elbow with theirs and said they'd told me she would make too much." / "Alex tipped the last of the
  jug into my glass before their own." *(new, flagged)*
- "I asked Mara what we were doing tomorrow." (or "where Kit was tonight", "who had been at the door") "She said, \"...\" It was
  what she had said when I came in." *(new, flagged)*
- "A while later [Name] did it again, the same way, with the same words." *(new, flagged)*
- "In the bathroom my [routine] was laid out again, untouched." *(new, flagged)*
- "Nobody set a place for Kit." / "Alex's chair stayed empty." *(new, flagged)*
- "The hall chair was back in the hall, and the front door was shut." *(new, flagged)*
- "When I left, my chair was still pulled out from the table." *(new, flagged)*

## Cases

`PlaceForYouTests#keepingAPlace` (new): the picture down and put back up, the chapter kept, the reader's chair at the table; the
reader sits and Alex, the first they sat with, is beside them; the gesture; a new question and the old answer, word for word; the
gesture again; `kept_a_place`; Kit not sat back down; the reader's things laid out again in the bathroom; and the account's
sentences, without the opened house's.

## Results

Locally: the five `PlaceForYouTests` cases with A Place for You's cast and two-reader conduct cases, 7 of 7. Eye-level frames
of the kept table and the routine laid out again. The release chain on this commit is in the commit's message.
