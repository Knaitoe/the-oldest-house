# A Place for You: the broken promise and the confession (0.4.693)

The ninth slice (the second part of item 7 of `docs/A_PLACE_FOR_YOU_PLAN.md`): two more consequence branches, each following from
what the reader said to Mara at the sink.

Code: `PlaceForYouPromise` (new), hooks in `PlaceForYou` (where people stand, the evening's clock, talking, the confrontation at the
table, how Mara decides, the spoon) and the account in `PlaceForYouTexts`. No new art: Mara is the same Mara, in a doorway the house
already has.

## What happens

### The broken promise

- **When.** At the sink the reader said "I'll sit with you tonight." while the back passage (0.4.690) was already under way, and then
  went back to the back door with Kit.
- **Missed.** Once the reader has been at the back of the hall, out of the kitchen, for eight seconds, Mara comes out into the
  hall, below the foot of the stair, and stands there looking down it at them (cue). She says "Oh." once, and after five seconds goes back in.
- **Said back.** When the bag is out and the reader is back in the kitchen near her, she says the promise back to them word for
  word, and where she found them. Three answers:
  - "I said tonight. It isn't over yet." She says it isn't, and that she'll hold them to the rest of it.
  - "I'm sorry. I said it, and then I went." She says they did, and that saying so doesn't undo it.
  - "Getting Kit out mattered more." She says she expects it did, and not to promise her things, then.
- **Before then.** Spoken to while the bag is still inside, she says "Go on. Finish it."
- **Afterwards.** Taking the spoon from her no longer counts as the promise kept. When she decides, after the house is opened, she
  asks differently: "You said tonight. It's still tonight. Will you sit with me a minute?" after the first answer, "I'll ask again
  anyway. Will you sit with me a minute? Before I decide." after the second; after the third she doesn't ask, and decides on her own to
  stay.

### The confession used

- **When.** At the table, for a reader who has heard about Theo (at the sink or upstairs), a fifth answer: "Mara, tell them about
  Theo. Or I will."
- **Told.** Mara says nothing. The reader tells them (the panel says what, from what Mara told them). Kit asks if it's true; Mara says
  yes; Ren says she told the reader that on her own, and they didn't have to do it like this. Two answers: it was the only thing that
  would work (Ren: working isn't the same as it being all right), or Ren is right (Ren: say that to her).
- **The house opens.** Mara says to open the door, she won't stop anyone, and not to sit with her after. Later, asked to decide, she
  stays: "I'll stay. I'd rather be on my own for it."

## With more than one reader

Each reader's evening is a copy of its own. Mara in the doorway, what she says back, and the confession at the table are that
reader's alone; nothing is shared.

## New lines

Every line below is new in this release.

**Cues**

- "Mara is in the hall, looking down it at you." *(new, flagged)*

**Narration in the panel**

- "You tell them: Theo, the last ordinary evening, and how she keeps the evening from getting to the part where he isn't here." /
  "You tell them what she told you at the sink: Theo, who used to dry, and the empty kitchen she won't talk in again." *(new, flagged)*

**The reader**

- "I said tonight. It isn't over yet." / "I'm sorry. I said it, and then I went." / "Getting Kit out mattered more." *(new, flagged)*
- "Mara, tell them about Theo. Or I will." *(new, flagged)*
- "It was the only thing that would work." / "You're right. I shouldn't have." *(new, flagged)*

**Mara**

- "Oh." (in the doorway) *(new, flagged)*
- "\"I'll sit with you tonight.\" That's what you said." / "I came to find you, and you were at the back door with Kit." *(new,
  flagged)*
- "No. It isn't." / "Then I'll hold you to the rest of it." *(new, flagged)*
- "You did." / "Thank you for saying so. It doesn't undo it." *(new, flagged)*
- "Yes. I expect it did." / "Then don't promise me things." *(new, flagged)*
- "Go on. Finish it." *(new, flagged)*
- "You said tonight. It's still tonight. Will you sit with me a minute?" / "I'll ask again anyway. Will you sit with me a minute? Before
  I decide." / "I'll decide this one on my own. It's still my kitchen." *(new, flagged)*
- "..." / "Yes." (the confession) *(new, flagged)*
- "Open the door, then. I won't stop anyone." / "Don't sit with me after." *(new, flagged)*
- "I'll stay. I'd rather be on my own for it." *(new, flagged)*

**Kit and Ren**

- Kit: "Is that true?" *(new, flagged)*
- Ren: "She told you that on her own. You didn't have to do it like this." / "It worked. That isn't the same as it being all right." /
  "Say that to her, not to me." *(new, flagged)*

**The account**

- After the back passage: "Mara came to find me, and I was at the back door with Kit." then one of "I said I'd said tonight, and it
  wasn't over yet. She said she'd hold me to the rest of it." / "I said I was sorry. She said it didn't undo it." / "I said getting Kit
  out had mattered more. She said not to promise her things, then." *(new, flagged)*
- The promise sentence at the sink drops its "I did." / "I didn't." when the promise was broken; the paragraph above says what
  happened instead. *(changed, flagged)*
- In place of the open confrontation's sentence: "At the table I told Mara to tell them about Theo, or I would, and then I told them
  myself. Kit asked if it was true, and she said yes. Ren said she had told me that on her own, and I hadn't had to do it like that."
  then "I said it was the only thing that would work. Ren said working wasn't the same as it being all right." / "I said Ren was right.
  Ren said to say that to her." then "Mara said to open the door, and that she wouldn't stop anyone. She asked me not to sit with her
  after." *(new, flagged)*
- How Mara ends: "Mara stayed. She'd rather be on her own for it, she said." / "Mara decided that one on her own, and stayed. It was
  still her kitchen, she said." *(new, flagged)*

## Cases

- `PlaceForYouTests#theBrokenPromise` (new): the quiet agreed with Kit; the key offered and taken; the promise at the sink, the back way
  still to do; at the back door, Mara comes looking, stands in the hall where the reader can see her, says "Oh." and goes back in; the door, the gate, the
  bag out; in the kitchen she says it back; three answers; the apology; the spoon not counted as the promise kept; how she asks again;
  the account; she doesn't ask after "Kit mattered more"; the "tonight" answer's ask.
- `PlaceForYouTests#theConfessionUsed` (new): the sink heard; the spoon; five answers at the table; the confession, Kit, Mara, Ren and
  two answers; the house opened; she decides to stay on her own; the account's confession and her end, and not the open telling's
  sentence.

## Also in this release: a Fallow customer finishes answering first

Run 1718's two-reader conduct check failed on Fallow: Otis, spoken to while buying, turned to the machine and then the counter while
his answer was still up, so his head swung away from the reader he was answering. A customer a reader is talking to now finishes
answering before going on with the purchase (`Fallow.customers()`), except when the strike's tone has begun. New case
`FallowTests#aCustomerFinishesAnsweringFirst`: a reader speaks to Otis while he buys, and his head must never swing more than 40
degrees away from them in one tick while he is addressed or saying something. With the fix taken out, it fails ("head -20.7 from
430.3, the reader at 70.3"); with it, it passes.

## Results

Locally: the sixteen `PlaceForYouTests` cases and the cast and together conduct checks for A Place for You, 18 of 18; after Mara was
moved into the hall (below), the two new cases and both conduct checks again, 4 of 4. The thirteen `FallowTests` cases and the cast
and together conduct checks for Fallow, 15 of 15; the new Fallow case fails without the fix.

Eye-level frames from the back of the hall, where the reader stands with Kit. The first frames had Mara in the kitchen archway (3.5,
-14.5): from the back door the archway is behind the hall's east wall and the stair, so a reader there could not see her at all. She
now comes out into the hall below the foot of the stair (0, -15.6), facing down it, and the frames show her face-on from the back
door, from halfway down and close to. The case now also checks that the reader can see her from where they stand.
