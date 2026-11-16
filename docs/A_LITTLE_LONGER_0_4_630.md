# A Little Longer 0.4.630: chapter 7, the room afterward

The tenth slice of the plan (docs/A_LITTLE_LONGER_PLAN.md), built from the proposal's chapter 7 (docs/A_LITTLE_LONGER_PROPOSAL.md):
"Do not immediately eject the reader or start another scare. Leave enough ordinary room for them to move, look, and act." Code:
`labyrinth/LongerAfter.java`, which begins when the cut (`LongerCut`, 0.4.629) has closed the space.

With this slice the evening can be finished, so A Little Longer becomes a Witness source and the House deals it.

## What happens

- **Nobody else.** Rowan is gone and does not come back. Nothing new comes into the room, and nothing scares the reader.
- **The radio.** About five seconds after the cut, the kitchen radio comes back on by itself, between stations. It hisses and never
  finds the tune again. The reader may switch it off, or on again; the hiss is all it plays.
- **The alcove.** A hand on the brick where the wall went answers with what it is, and nothing is behind it.
- **The chair.** Sitting in the chair Rowan used (the kitchen chair if the evening went to the kitchen, otherwise the armchair) says
  once how it was left.
- **The cups.** A hand on the cups asks which to take: the good cup, Rowan's (the one with the crack), or neither. A cup taken is an
  ordinary cup of tea named for whose it was. A reader takes one cup at most.
- **The list.** A reader holding the breakfast list (0.4.628) may put it down on the table, writing up; otherwise they keep it.
- **The account.** After ten seconds in the room afterward the account is ready at the table. It tells the evening in order from that
  reader's own record (what they did at the bench, the toast and the cup, what was said about the tune, the roof, the ordinary thing,
  the list and what they said about tomorrow), then the cut, the same for everyone. Its last sentence follows what the reader did in
  the room afterward: a cup brought back, the list left on the table, the list kept, or, with no list, that the kettle was still warm.
  Reading it resolves the story.
- **Kept**: `After0630` (ticks in the room afterward), `Cup0630` (GOOD, ROWANS), `RadioOff0630`, `Sat0630`, `Scrap0630` (LEFT),
  `Brick0630`.

## Witness

`WitnessAccount.Story.LITTLE_LONGER` ("A little longer", memory). The outcome kept with it is what the reader said about tomorrow:
`planned_breakfast_together`, `said_tomorrow_mattered`, `joked_about_the_bread`, `friends_on_the_road`, or `left_before_tomorrow` when
they said nothing. The Witness line names "a friend" for a reader who asked to be friends on the roof, "someone" otherwise, and the list
only if there was one. The pool is now **90 sources / 68 required / 66 with a first paper**; two kinds; three endings. The House deals A
Little Longer until the reader has finished it (`Longer.dealable`). It has an advancement (`account/a_little_longer`, icon a cup of
tea). Rowan is left off the staircase (`StairCompany.NOT_YET`): Rowan's body is whichever of two casts the reader met, and the stair's
figures are one body each.

## New lines, flagged for the owner

Cues (grey italic, in the action bar):
- "The kitchen radio comes on by itself, between stations."
- "The hiss stops."
- "The radio warms up and finds only the space between stations."
- "Brick, cold, a hand's depth behind where the wall was."
- "The chair is pulled out the way Rowan left it."
- "You take the good cup. The tea in it has gone cold."
- "You take Rowan's cup, the one with the crack."
- "You leave the cups where they are."
- "You put the list down on the table, the writing up."

Replies: "Take the good cup", "Take Rowan's cup", "Leave them". Item names: "The good cup", "Rowan's cup".

The account (every sentence is new; which appear depends on the reader's record):
- "There was a radio repair shop with a flat over it, and rain on every window. Rowan was at the bench, trying to make a table radio
  hold a station." + one of "I held the back panel for them." / "I found the screw that had gone under the bench." / "I moved the lamp
  so they could see." / "I waited at the door before I went in."
- "Upstairs Rowan put the kettle on and made toast." + one of "I got it out before it burnt." / "It burnt. I said so, and Rowan had the
  burnt slice." / "It burnt. I gave Rowan the good slice." / "It burnt. I had the burnt slice." + "Rowan gave me the cup without the
  crack."
- "The kitchen radio played a tune, and I said to leave it on." / "...and I asked for it to be turned down." / "...and I said something
  might hear it." + "Afterwards I said I had wanted it on too." / "Afterwards I said the doors were still real." / "Afterwards we kept
  it on, quieter."
- "We went up to the roof. Rowan showed me the sky between two buildings." + "I sat beside them." / "We said nothing for a while." / "I
  said I would like us to be friends." / "I said I was glad I had stopped." / "I sat closer." / "Rowan held out a hand and I took it." /
  "Rowan held out a hand and I said not yet." / "I asked if I could kiss them, and they said yes." / "Rowan asked if they could kiss me,
  and I said yes." / "Rowan asked if they could kiss me, and I said not tonight."
- "Back inside, Rowan washed the cups and I dried." / "Back inside, we argued about how bad the toast had been." / "Back inside, we sat
  down for a while."
- "We made a list for breakfast. I put down [the reader's food]. Rowan put "[Rowan's addition]" under it." + "I said we should go
  together." / "I said tomorrow mattered." / "I said Rowan was carrying the bread." / "I said we were friends on the road."
- "Rowan went to unplug the toaster. The radio stopped halfway through the tune. The wall by the toaster went back further than walls
  do, and Rowan went with it, the toaster and the end of the worktop too. Rowan said wait."
- The last paragraph: "I turned the radio off." (if they did), then "We had made a list for breakfast. I brought one of the cups back."
  (the proposal's sentence) / "We had made a list for breakfast. I left it on the table." / "We had made a list for breakfast. I have
  it." / "The kettle was still warm when I went."

Witness line: "I spent an evening in a radio shop with [a friend / someone] who was fixing a set that kept losing its station.[ We had
made a list for breakfast.] Then the wall by the toaster went back, and they went with it."

## Tests

`LongerTests.theRoomAfterwardTheCupTheListAndTheAccount`: the room afterward begins after the cut; the radio comes back between
stations and is switched off; the cups ask which, and Rowan's is taken and carried; the list goes down on the table; the account is
ready after a while and tells the hand, the list, the present choice and the cut, and ends on the cup (with different last sentences
for leaving the list and for keeping it); the Witness line has the list; the story is a memory source, dealt until read, and reading
the account resolves it. The brick and the chair have no case. The first slice's case now checks that the House deals the story to
a reader who has not finished it. The longer namespace passes 20 of 20; the advancement cases and the staircase's "every story is
accounted for" pass with the new source.

## Not covered

- Nobody has played the room afterward in a client; no review frames were taken this slice (nothing new is drawn: the cups, the radio
  and the chair are 0.4.621's models).
- Leaving early, coming back, and an evening that ends before the cut are slice 11. The House's read of the reader (personhood) is
  slice 12.
