# Second Person, spring: the let (0.4.633)

The proposal's fourth season (docs/SECOND_PERSON_PROPOSAL.md, section 4, ledger rows 19 to 23), between the winter and the last
morning. New builds only. No layout, protocol or Witness change: 90 sources / 68 required / two kinds / three endings.

## What happens

The winter's turn in the airing cupboard now opens the spring ("While you were in the cupboard, the rest of the winter went on
without you. Then the tenancy came up."). The House's pages, written in the reader's name while they were in the cupboard, are
written at this turn as before. The street tree is in blossom.

1. **The agent** (`SecondPersonSpring.S_AGENT`). A knock; the resident opens the flat door; the letting agent comes up from the stair
   top with a clipboard and is walked round: the hall, the front window, the kitchen sink, the bathroom door, with a word at each.
   A reader who stands within reach of the agent for two seconds makes the air go cold round them (snowflakes, as for the cast
   already); the agent hurries, and the rent stays where it was (`Rent` = `same`). Otherwise it goes up (`up`). The agent leaves by
   the landing and the door shuts behind them before they go. Ledger 19.
2. **Deciding** (`S_DECIDE`). The resident sits at the kitchen table with the letter and thinks aloud, by the House's read of home
   (HAVEN, DANGER, RUNAWAY) or by Sam, if Sam stayed. While they sit, a reader can lay one of their things by the flat door
   (`LaidGo`) or put one where it lives (`LaidStay`); they notice either. Each counts once in `SecondPersonStory.stays`, as do the
   rent held (stay) and raised (go). What they decide is spoken, and saved in `Decision` and `Going`. Ledgers 20 and 21. The
   decision is never the reader's: the laid things are two weights among the year's many.
3. **The form on the mat** (`S_POST`). The post brings an application for the flat, "for when the current tenant leaves", in the
   reader's handwriting, with the reader's name. The reader can read it all, turn it face down (the block shows its blank back), or
   leave it. Read: the resident reads it too, says it is them the form means, and asks the room "Is this you?"; a word written on
   the steamed mirror or the street window in the next thirty seconds is read at once and kept (`SpringAnswer`), and the resident
   answers it. Face down: they pick it up with the post and never turn it over. Left: they read it and say somebody is keen. Either
   way it goes in the hall table drawer. Ledgers 22 and 23. Signing it (ledger 24, E2) is the next slice.
4. **The last of the spring** (`SPRING_DONE`). The cupboard turns the season again, into the last morning. The decision made in the
   spring stands; a year that never reached the deciding is read the same way off the record, as before.

The book on the shelf has a page for each step. The account gains one spring paragraph after the winter's, from the record only.

## Art and the cast

- The letting agent: `literary_flat_agent` (stranger phase 77, `SecondPerson.AGENT`), painted by tools/paint_character_skins.py in
  an off-the-peg suit with an open collar. Seen in review frames from the front, side and three-quarter.
- The clipboard: a new carried model (custom model data 23, `SecondPersonRegistry.FORM_MODEL`): hardboard, the filled-in form and a
  steel clip, held upright at the hand with the form facing out (its own third-person display).
- The form on the mat: `sp_fitting` kind `application`, stage 0 face up (a printed band, ruled boxes, answers in ink, an empty
  signature line) and stage 1 face down (the blank back with the boxes faintly through). Drawn by tools/draw_second_person_art.py
  (`form`, `form_back`, `hardboard`). No collision.
- The street tree: cherry leaves for the spring (season 4 in `SecondPersonSite.season`).

## New lines (all new in this slice)

The agent: "Morning. I won't keep you. Just a look round for the landlord." / "Hall's fine." / "Light's good at the front. They
always say that about the front." / "Tap doesn't drip? Good." / "And the bathroom. That's the lot." / "Is it me, or is it cold in
here?" / "Sorry, was that you? No." / "Look, I'll put it down as damp. Same as last year, then." / "It'll go up in the summer.
Everything does. I'll send it over."

The resident: "It does that." / "It's an old building." / "Thanks. End of the week, then." / "Sam's got a spare room. Sam's got a
garden." / "I could go home. They'd have the back room ready by Friday." / "Somewhere nobody has the address. Somewhere new,
again." / "I could write back. I could just write back." / "Twelve months. Same again." / "Or I stay." / "Is that a hint?" /
"...You think so?" / "I'm staying. I'll ring them in the morning." / "I'm going to Sam's." / "I'm going home." / "I'm going. I won't
say where. Not even here." / "I'm going to write back. And then I'm going." / "I'm going. I don't know where yet." / "\"For when the
current tenant leaves.\" That's me." / "Is this you?" / "...Somebody's idea of a joke." / "Somebody's keen." / "Then you were here
first." / "Then who keeps writing it?" / "Stay, then." / "Go where? You live here." / "Hello." / "What for?"

The form (panel): "An application for the tenancy of the flat above the launderette." / "For when the current tenant leaves." /
"Name: (the reader's name)." / "It is in your handwriting." / "Current address: this one." / "How long at current address: longer
than the tenant." / "Reason for applying: I was here first." / "At the foot there is a line to sign. It is empty." / "Your name, in
your hand." / "Face down." Replies: "Read it all." / "Turn it face down." / "Leave it."

Cues: "Somebody knocks at the flat door." / "The post comes through the door." / "They pick it up with the rest of the post, and
never turn it over." / "They put the form in the hall table drawer." / "The flat goes quiet."

The book: "While you were in the cupboard, the rest of the winter went on without you. Then the tenancy came up." / "A letting
agent is coming to look round." / "Stand close to the agent, where the air goes cold." / "Or keep out of their way." / "They have
the letter at the kitchen table. They have until the end of the week to say." / "Lay out what they would need to go: their coat by
the door." / "Or what they would need to stay: their things put where they live." / "Or leave them to it." / "The post has come.
There is a form on the mat." / "Read it." / "Turn it face down." / "Leave it where it is." / "They have read the form, and asked
the room a question." / "Breathe on the glass. It holds one word." / "Or say nothing." / "The last of the spring." / "While you were
in the cupboard, the end of the week came, and the letter about the tenancy was answered."

Pages turned: "You stood close to the agent, and the rent stayed where it was." / "You let the agent go round, and the rent went
up." / "You laid (a thing) by the door, as if for going." / "You put (a thing) where it lives, as if for staying." / "You read the
form on the mat. It was in your handwriting." / "You turned the form face down." / "You left the form on the mat, and they read it
and said somebody was keen." / "They read the form with your name on it, and asked the room if it was you." / "They asked if it was
you. You wrote (word) on the glass." / "You let the rest of the spring go by in the cupboard."

The account: "In the spring the tenancy came up. A letting agent came round with a clipboard" then ". You stood close enough for
the cold to be felt, and they hurried, and the rent stayed where it was." or ", and the rent went up."; "While they decided, you
laid their things by the door." or "While they decided, you put their things where they lived."; "Then a form for the flat came
through the door, for when the current tenant leaves, in your handwriting, with your name on it. You read it. They read it too,
and asked the room if it was you." and "You wrote (word) on the glass, and they kept the word."; or "...You turned it face down,
and they never turned it over."; or "...You left it on the mat. They read it and said somebody was keen."

## Tests

- New: `SecondPersonTests.springTheLetTheAgentAndTheForm`: the turn into spring and its page, the blossom, the agent let in with
  the clipboard in hand, the cold and the rent held, the agent gone, the coat laid by the door, the decision and the post, the
  reader's name on the form, the form read, the question asked and the book's page for it, YES on the street window kept, the
  account's spring paragraph, and the last morning keeping the spring's decision with no form left on the mat.
- The three year-long cases now go winter, spring (to its end), then the last morning.
