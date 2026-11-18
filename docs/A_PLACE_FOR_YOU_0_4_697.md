# A Place for You: the direction of care (0.4.697)

The thirteenth slice, item 9 of `docs/A_PLACE_FOR_YOU_PLAN.md` (the personhood variants), for the waiting room's read (AGE). Until now
the read changed one line of Mara's welcome. Now the household acts on what it assumes about the reader through the evening, the reader
can answer it, and the answer comes back when everyone sits down, at the table, in the account and in the Witness line. The proposal
asks for "substantial scenes, not interchangeable adjectives"; each read is its own exchange with its own person.

Code: `PlaceForYouAge` (new); hooks in `PlaceForYou` (the evening's clock, talking, the confrontation), the account in
`PlaceForYouTexts` and the Witness line in `WitnessAccount`. No new art.

## What happens

The assumption is said once, over the speaker's head, between supper and dinner while the reader is in the kitchen or the sitting room
and within ten blocks of them. The exchange opens the next time the reader talks to that person with nothing else to say to them: it
takes the place of their idle word ("Eat something.", "Not now.", "Sit down."), never of a conversation that moves the evening on. It is
offered once. Leaving it unanswered lets the assumption stand.

- **Come back after a life elsewhere (DIGITAL).** Mara has kept the reader's room, and offers them a reason never to go back to the life
  they have now. The reader can say they have a place of their own, that they might stay a while, or ask her to leave the room as it is.
- **The one who manages everyone (TRANSITIONAL).** Ren and Alex want the reader to be the one who tells Mara that Kit is going. The
  reader can tell the two of them to do it together, say they will, or ask why it is always them.
- **Someone to be looked after (ANALOG).** Mara has put the reader nearest the stove and will cut up their supper. The reader can say
  they can manage, thank her, or ask to choose where they sit.
- **Read, but not settled: a guest.** Alex says the reader will be one of them by pudding, the way he was. The reader can say they are
  only there for supper, that they'd like that, or ask how long he has been there.
- **The House was asked not to read the reader.** Nothing happens.

Once the spoon is out of Mara's hand and everyone sits, one of them says something that keeps the assumption, or the reader's answer if
they turned it down or chose for themselves. At the confrontation, straight after "Sit down, all of you. It's getting cold.", Mara uses
the assumption as a lever, or turns the reader's own answer back on them. Neither changes the table's answers or what the reader can do.

The account gives the read its own paragraph after supper: what was assumed, what the reader said, and the two lines word for word if
they were said. The Witness line adds a sentence for what the household assumed and, if the reader turned it down, the answer.

## With more than one reader

Each reader's evening is a copy of its own, and so is the read: their own assumption, said over their copy's heads, their own exchange
and their own answer. Two readers with different reads sit down to different evenings.

## New lines

Every line below is new in this release.

**Over their heads, between supper and dinner** *(new, flagged)*

- Mara (DIGITAL): "Your coat's on the back of your door, where you left it."
- Ren (TRANSITIONAL): "When you've a minute. Alex and I need you to settle something."
- Mara (ANALOG): "Don't get up. I'll bring it to you."
- Alex (guest): "First night's the strangest. After that you're one of us."

**The exchanges** *(new, flagged)*

- DIGITAL. Mara: "You don't have to go back to that place of yours, you know. Whatever it is. Your room's here."
  - "I have a place of my own now." / Mara: "Yes. Of course you do. I won't say it again."
  - "I might stay a while." / Mara: "A while. Good. That's all I meant."
  - "Leave my room as it is." / Mara: "I always have."
- TRANSITIONAL. Ren: "Kit's going, and somebody has to tell Mara. Alex says it should be you. You always know how to put things." /
  Alex: "Ren says so too. We agree on that much."
  - "You two tell her. Together." / Ren: "...All right. Together." / Alex: "Together, then."
  - "I'll tell her." / Ren: "Thank you. I knew you would."
  - "Why is it always me?" / Ren: "Because it always has been." / Alex: "That isn't an answer, Ren."
- ANALOG. Mara: "I've put you nearest the stove, it's warmest. And I'll cut your supper up for you. It's easier."
  - "I can manage, thank you." / Mara: "Of course you can. I forget."
  - "Thank you." / Mara: "There. Isn't that better."
  - "Let me choose where I sit." / Mara: "...Choose, then."
- Guest. Alex: "You'll be one of us by pudding. That's how it goes here. It went that way for me."
  - "I'm only here for supper." / Alex: "Course. Everyone is, the first time."
  - "I'd like that." / Alex: "See? Easy."
  - "How long have you been here?" / Alex: "Since a supper. I couldn't tell you which."

**When everyone sits down** *(new, flagged)*

- DIGITAL, Mara: "This place of yours. Is it warm? Tell me it's warm." (they said they have a place) / "I've put clean sheets on your
  bed. For tonight, and after." (otherwise)
- TRANSITIONAL: Ren, "We told her, Alex and me. She took it better than she would have from you." (they gave it back to the two of
  them) / Alex, "Go on. Everyone's waiting for you to say it." (otherwise)
- ANALOG, Mara: "Help yourself. You said you could manage." / "Sit where you like. You wanted to choose." / "I've cut it up for you.
  Eat." (otherwise)
- Guest: Alex, "Only supper, you said. I'll hold you to it." (only there for supper) / Mara, "There. You're not a guest any more."
  (otherwise)

**At the table, Mara** *(new, flagged)*

- DIGITAL: "You've a place of your own. I know. I'm asking anyway." / "You came home. Don't make me watch you leave it twice."
- TRANSITIONAL: "Ren and Alex told me about Kit. I know you put them up to it." / "Go on. You're the one who decides things here.
  Decide this."
- ANALOG: "You can manage. You told me. Then manage this." / "You wanted to choose. Then choose." / "Don't you worry about any of
  this. We'll decide it. You sit."
- Guest: "You said only supper. It's after supper." / "You're one of us now. Alex said so."

**The account** *(new, flagged)*: one opening sentence per read ("Mara had kept my coat on the back of a door she called mine, and told
me I didn't have to go back to the place I lived now." / "Ren and Alex wanted me to be the one to tell Mara that Kit was going,
because I always knew how to put things." / "Mara put me nearest the stove and said she would cut my supper up for me." / "Alex said I
would be one of them by pudding, the way he had been."), one sentence per answer, including not answering, then "Later, at the table,
<who> said, "…"" and "When it came to deciding, Mara said, "…"" with the lines as they were said.

**The Witness line** *(new, flagged)*, after the story's own line: "They had kept my room for me." / "They had kept my room, and I told
them I had a place of my own." / "They wanted me to be the one who told her." / "They wanted me to be the one who told her, and I gave
it back to them." / "They meant to look after me." / "They meant to look after me, and I told them I could manage." / "They meant to
look after me, and I chose where I sat." / "Someone there said I'd be one of them by pudding." / "Someone there said I'd be one of them
by pudding. I said I was only there for supper."

## Cases

- `PlaceForYouTests#theDirectionOfCare` (new): read as one to be looked after (ANALOG) and nothing said of it yet; in the kitchen
  between supper and dinner Mara says it over her head; the next word with her is the exchange, with three answers; asking to choose
  where they sit; not asked again, her idle word instead; when everyone sits, she keeps the answer; at the table the lever comes
  straight after her opener, and the table's own answers are all still offered; the account's paragraph word for word and in the
  account; the Witness line. The other reads and a reader the House was asked not to read, from the record.

## Results

Locally: `theDirectionOfCare` 1 of 1; with it `aReaderTheHouseKnowsTakesTheRoad` (an ANALOG reader through the whole evening),
`aGuestOpensTheHouse`, `somebodyAtTheDoor`, `theBondOffered`, `theConfessionUsed` and A Place for You's cast and together conduct
checks, 8 of 8.
