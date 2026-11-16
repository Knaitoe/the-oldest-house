# Second Person, E4: two cups (0.4.636)

The proposal's fourth ending (docs/SECOND_PERSON_PROPOSAL.md, section 5, "Let yourself be known"). New builds only. No layout,
protocol or Witness change: 90 sources / 68 required / two kinds / three endings. The ending resolves Second Person like E1 to E3,
with its own outcome (`two_cups`).

## What happens

1. **Known gently.** At the cupboard at the end of the spring (never once the form is signed), the year is read off the reader's own
   record: `SecondPersonStory.gentle` at six or more and three or more over `fright`, and at least once the reader let themselves be
   met (stood still or put a kind hand on them when seen, wrote a word when seen, answered the spring glass, or HELLO, STAY or SORRY
   read on the glass). Then the year turns into its last evening, not its last morning: `SecondPersonCups.TWO_CUPS` (6, compared only
   by equality). The decision is still taken from the year as before; it is not shown until the end.
2. **Two cups** (sub 0). Evening, the lamp on, the flat door shut. On the kitchen table, where the cloth lies, a new `sp_fitting`
   kind `CUPS`: two cups, theirs by the resident's chair, the reader's by the guest chair. They are sitting at the table and wait for
   the reader to come to the guest chair (or half a minute).
3. **They talk** (sub 1). One line every five seconds, overhead as always, from the year: Iris (the sleeve, the knock or the flat
   never empty), the phone (rung home or rung back), Sam (comes at the weekends or asked after the reader), the face in the glass (how
   the reader was seen).
4. **The question** (sub 2). "Will you be here when I wake up?" The glass holds one word; whatever the reader writes is their answer.
   Saying nothing is allowed (45 seconds).
5. **The end** (sub 3). Their reply to the word, then "You can stay." or "You can come.", from the decision. They finish their cup
   (the cup's top turns to a stain, a drink sound). The last line: "They will get older here. You won't." The flat door opens, the
   book is on the bench and the account is ready.

The account keeps the whole year and replaces the last morning with the evening's own paragraph.

## Art

`tools/draw_second_person_art.py`: three textures (`mug`, a 4x5 side with rim, blue band and foot; `mug_full` and `mug_empty`, 4x4
tops with tea or a stain) and two models (`cups_0`, `cups_1`), each two 4x5x4 cups with three-part handles, the pixel scale matching
the face sizes. Reviewed on the formica table from the room, from the reader's chair and from above, full and after.

## New lines (all new in this slice)

Cues: "The last evening of the year. On the kitchen table there are two cups." / "Breathe on the glass. It holds one word." /
"They will get older here. You won't." / "On the bench at the bus stop, where nobody waits, there is a book."

The resident: "I made two. I don't know why I made two." / "Sit down. If you sit." / "I finished Iris's sleeve. It's the wrong
length. I don't care." / "Iris knew. She said I had company. I thought she meant a cat." / "Iris used to say this flat was never
empty." / "I rang home on Sunday. I said I was all right. I think I meant it." / "I rang back, in the end. We talked for an hour
about nothing." / "Sam comes at the weekends now. Sam always comes." / "Sam asked after you. I said you were fine. I don't know why I
said that." / "You stood still for me, that night in the glass. I've thought about it a lot." / "That night you put your hand on me.
It was cold. It was kind." / "You wrote on the glass, that night. I kept it." / "I think I've seen you in the glass. Or I've wanted
to." / "Will you be here when I wake up?" / "Good." / "No. No, I know." / "Hello." / "All right." / "Don't be." / "...All right." /
"That's all right. You don't have to." / "You can stay." / "You can come."

The book's pages: "1. Sit with them." / "They are talking to you." / "1. Listen." / "They have asked you one thing." / "2. Or say
nothing." / "They have finished their cup." / "1. Go out by the front door and down to the street."

Pages turned: "You went into the cupboard at the end of the spring, and came out into the last evening." / "They asked if you would
be here when they woke up. You wrote (word) on the glass." / "They set out two cups, and talked to you through the last evening."

The account: "On the last evening of the year they set out two cups, and talked to you across the kitchen table: about Iris, about
the phone, about Sam, about the face in the glass. They asked if you would be here when they woke up." / "You wrote (word) on the
glass." (or "You did not answer, and they said that was all right.") / "They said, "You can stay."" (or "You can come.") / "They
will get older here. You won't."

## Tests

`SecondPersonTests.twoCups` (batch `second_cups`): a gentle autumn and a seen-still winter, then the spring's cupboard; the evening,
the cups, the reader at the table, YES on the glass, the outcome, the cups drunk and the account; no last morning in it.
`aGentleWinterAndTheHousesKindPages` now follows the gentle, seen year into the two cups (STAY written). The Second Person namespace
is 8/8.
