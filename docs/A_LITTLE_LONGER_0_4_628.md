# A Little Longer 0.4.628: chapter 5, tomorrow has two people in it

The eighth slice of the plan (docs/A_LITTLE_LONGER_PLAN.md): "The shifted door; Rowan's coat; the breakfast scrap written by two people;
the present choice." Built from the proposal's chapter 5 (docs/A_LITTLE_LONGER_PROPOSAL.md). Code: `labyrinth/LongerTomorrow.java`, which
takes Rowan over forty seconds after the return indoors (`LongerReturn`, DONE), or at once on a hand on Rowan.

## What happens

- **Something outside has changed.** The shop's back door is suddenly a block east of where it was; the old opening is plaster; a strip
  of pale tile (the bathroom's) runs down the House's corridor to the new door. A door is heard settling. Rowan: "Did you hear that?",
  and walks down the back stair on their own feet to look at it.
- **Investigating together.** With the reader near: "That was a foot to the left this morning." / "And that floor wasn't there." The
  reader may ask whether it has done this before ("Not since I've been watching it."), look at the floor, and says they should go and see:
  "We should." / "I'm not staying here on my own with it doing that. Give me a minute."
- **The coat.** Rowan walks up to the hook at the top of the stairs, reaches for it, and hangs the apron up and puts the coat on: Rowan's
  cast changes to the same person in their coat (new painted skins, the coat's dark wool the hook's own), and the hook is left empty.
  Rowan is not rooted to the shop and not guarding anything that keeps them behind.
- **Tomorrow, one callback.** In the kitchen, the one callback this evening earned, never more than one:
  - a slice burnt at the kettle: the proposal's exchange word for word ("What do you want for breakfast?" / "Something you haven't set on
    fire." / "All right. You're making it.");
  - the whole cup taken: "You already gave me the good cup." / "You can have it tomorrow too.";
  - the radio enjoyed or flirted over: "You can pick what's on next time. Within reason.";
  - otherwise: "We'll want breakfast. Wherever we are."
- **The list written by two people.** Rowan finds a scrap of paper. "What do we need?" The reader names bread, eggs, coffee or oranges;
  Rowan adds their own underneath ("And tea. Proper tea.", or "And tea, for me." after coffee), and hands the reader the scrap: a real item,
  the reader's line and Rowan's under it. It is written now, for the morning; it is not a letter prepared for anything else. It is the
  keepsake of the slices to come.
- **The present choice.** "Ready when you are." Go together ("Good."); say it matters ("I'd like that. Tomorrow, I mean.": in the
  romantic route, "So would I. And the day after."; otherwise "So would I."); joke ("You're carrying the bread." / "I'm carrying
  everything. You've got the list."); or ask to stay companions ("Friends on the road, then?" / "Friends on the road."). No farewell, no
  promise of forever: they expect to leave together.
- **Kept**: `Tomorrow0628` (stage), `Shift0628`, `Coat0628`, `Callback0628` (TOAST, CUP, RADIO, PLAIN), `List0628` (the reader's word),
  `Choice0628` (TOGETHER, MATTERS, JOKE, COMPANIONS). Leaving part way: the door stays moved; at the door Rowan waits there; walking for the
  coat finishes it (the coat on); the kitchen and the choice resume. The cut (chapter 6) is the next slice.

## New lines, flagged for the owner

From the proposal, word for word: "What do you want for breakfast?", "Something you haven't set on fire." (the reader's), "All right.
You're making it.", "What do we need?", "You already gave me the good cup." (the reader's), "You can have it tomorrow too."

New (Rowan):
- "Did you hear that?"
- "That was a foot to the left this morning." / "And that floor wasn't there."
- "Not since I've been watching it."
- "We should." / "I'm not staying here on my own with it doing that. Give me a minute."
- "You can pick what's on next time. Within reason."
- "We'll want breakfast. Wherever we are."
- "And tea. Proper tea." / "And tea, for me." (on the list)
- "Ready when you are."
- "Good." / "So would I. And the day after." (romantic route) / "So would I." / "I'm carrying everything. You've got the list." /
  "Friends on the road."

New (the reader's): "Has it done this before?", "Look at the floor", "We should go and see.", "Bread", "Eggs", "Coffee", "Oranges",
"Let's go together.", "I'd like that. Tomorrow, I mean.", "You're carrying the bread.", "Friends on the road, then?".

New cues: "Pale tiles, clean, laid by nobody. They stop at the door.", "Rowan hangs up the apron and puts their coat on.", "Rowan finds a
scrap of paper and a pencil stub.", "Rowan writes it under yours and hands you the scrap."; the scrap's name, "A scrap of paper".

## Art

- Rowan in their coat, both casts (`literary_longer_rowan_woman_coat`, `literary_longer_rowan_man_coat`, stranger phases 212 and 213),
  painted by `tools/paint_character_skins.py`: the apron gone, a long dark wool coat with lapels and buttons, the work shirt at the collar,
  long sleeves to the wrist. Only these two sheets were written; the painter's other 364 sheets are unchanged.
- Review frames of both casts in the coat (front, back, side, walking, reaching for the hook) were rendered and looked at.

## Tests

`LongerTests.theDoorMovesRowanPutsTheirCoatOnAndTheBreakfastListHasTwoHands` (the door moved and walled, the tile strip, Rowan down to
it and what is different, the coat on and the hook empty, the burnt-toast callback only, coffee and tea for Rowan, the scrap in the
reader's inventory, the romantic answer) and `LongerTests.theGoodCupTomorrowAndFriendsOnTheRoad` (the good-cup callback, bread and tea,
friends on the road, never the romantic line). The longer namespace passes 17 of 17.
