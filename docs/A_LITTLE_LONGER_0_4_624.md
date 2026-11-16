# A Little Longer 0.4.624: chapter 2, the kettle

The fourth slice of the plan (docs/A_LITTLE_LONGER_PLAN.md). Built from the proposal's chapter 2 (docs/A_LITTLE_LONGER_PROPOSAL.md):
"I'm making tea." / "Is that part of the repair?" / "No." / a pause / "You could stay for it.", the cups that were never a pair, the
comfortable chair, the toast, and questions about now. Code: `labyrinth/LongerKettle.java`, ticked and talked to after the bench
(`LongerRowan`), with the cups and the toaster answering through `Longer.click`.

## What happens

- **The offer.** Once the bench is put away, a hand on Rowan, or a reader by the chair for five seconds, opens the panel: "I'm making
  tea." The reader asks whether that is part of the repair ("No.") or says nothing. Then three seconds with nothing to choose, then "You
  could stay for it.": stay, one cup, or go on. Going on is let be; the next hand on Rowan offers once more ("Kettle's still there."),
  and after a second no it is not offered again.
- **Upstairs.** Rowan gets out of the chair and walks the aisle, across to the back stair and up its six treads, the height eased tread
  by tread (`LongerRowan.step` now eases height along the way), along the flat to the kettle. The kettle goes on (a new sound: from cold
  to the boil, the click), steam at its spout. While it boils Rowan walks to the table, takes up the two cups, and carries them back.
  At the click Rowan pours (a new sound) and carries them to the table; the cracked one is theirs, the whole one is left.
- **The cup.** A hand on the cups once poured is a real cup of tea (a new item, drunk, not eaten).
- **The good chair.** Rowan sits in the armchair with their cup, drinking now and then, and is not getting up. A reader already sat in
  the armchair keeps it, and Rowan takes the kitchen chair. The first hand on Rowan in the armchair: "This is the good chair. I know.
  I'm not getting up."
- **Where the reader is.** Seated, Rowan's first words are about where the reader is: in the kitchen chair by the window, standing, or
  at the toaster.
- **The toast.** A hand on the toaster pushes it down; Rowan says the heat is manageable; the left slice scorches (smoke) and the toaster
  pops (a new sound, its pop timed to the slices coming up). A second hand before it scorches pops it in time: two good slices,
  "Show-off." Otherwise one comes up burnt, and the reader teases, gives Rowan the good slice, or takes the burnt one to be brave (the
  proposal's exchange word for word). The slices are real items, toast and burnt toast. Left alone for thirty seconds, the toaster is
  forgotten and the questions begin.
- **Now.** Three questions from Rowan about the present, each with three answers, each answered by Rowan as well. Nothing about
  anybody's past. Kept: `Music0624`, `Plan0624`, `First0624`, `Toast0624`, `Stay0624`.
- **After.** Rowan stays in their chair upstairs with the cup. Leaving part way puts the chapter back to before the offer, Rowan back at
  the bench and the kitchen as it was; what was chosen is kept. The radio (chapter 3) is the next slice.

## New lines, flagged for the owner

From the proposal, word for word: "I'm making tea.", "Is that part of the repair?" (the reader's), "No.", "You could stay for it.",
"You don't have to be polite.", "I'm being brave." (the reader's), "That's worse."

New (Rowan):
- "All right. It's the same door you came in by." (going on)
- "Kettle's still there." (the offer once more)
- "Kettle's on. Sit down somewhere." (a hand on Rowan while they make it)
- "The one without the crack's yours." (setting the cups down)
- "This is the good chair. I know. I'm not getting up."
- "You found the good one. That's fine. That's completely fine." (the reader already in the armchair)
- "Is it cold by the window? Pull the chair in. It's warmer this side." (the reader in the kitchen chair)
- "You can sit. That chair doesn't bite. This one does, a bit." (standing)
- "Two slices. The left side burns, so watch it." (at the toaster)
- "Push the toaster down, would you? It's the only thing in this flat I can cook."
- "It's fine. The heat's perfectly manageable."
- "Show-off." (popped in time)
- "It was until it wasn't." (teased)
- "Then you've got the burnt one. I won't stop you." (the good slice given to Rowan)
- "Drink it while it's hot. It won't be, for long." (a hand on Rowan before the cup is taken)
- "Music, or quiet? I never know which people want." / "Music. Quiet in here and I start listening for things."
- "Do you like having a plan?" / "I have a list. I don't do what's on it."
- "First thing you'd do, out there?" / "Find out what day it is."
- "Sit as long as you like. I'm not going anywhere. I've checked." (after the chapter)

New (the reader's replies): "Say nothing", "I'd like that.", "One cup.", "I should keep going.", "Perfectly manageable.", "Give them the
good slice", "I'll have the burnt one.", and the answers "Music." / "Quiet." / "Whatever you'd have on.", "Yes." / "Not really." /
"Depends whose.", "Eat something that isn't toast." / "Sleep." / "I don't know yet."

New (cues): "You take the cup without the crack.", "The lever goes down on two slices. The elements tick.", "The toaster throws up two
slices. The left one is black down one side."; the items "Tea", "Toast", "Burnt Toast".

## Art and sound

- Items drawn by `tools/draw_longer_art.py` (16 pixels, covered by `--check`): the whole cup with tea and a wisp of steam, a slice of
  toast, a burnt slice.
- Sounds by `tools/generate_longer_audio.py` (synthesized, mono, subtitled): the kettle from cold to the boil and its click (7.5 s), tea
  poured into a cup rising in pitch as it fills, the toaster's timer ticking and the spring (the pop at 2.0 s).
- The radio's tune for the next chapter, drawn now in the same tool: an original eight-bar melody in three-time, piano over a
  walking bass and brushes, narrowed to a valve set's speaker, whole and turned down (`longer.tune`, `longer.tune_low`). Nothing plays it
  in this slice.
- Rowan's new gestures (`client.LiteraryRenderers`): a hand on the kettle; carrying the cups at the waist; pouring, the kettle lifted and
  tipped; sat with the cup, raised to drink every seven seconds with the head tipped back to it. Review frames of both casts at the
  kettle, carrying, pouring (lifted and tipped), in the good chair (holding, drinking, cup and toast) and in the kitchen chair were
  rendered and looked at.

## Cases

`LongerTests`: the offer, the pause, going on and the second offer, one cup, the walk up the stair through nothing solid, the kettle and
the pour heard, the cups taken up and set down, the good chair, the cup taken, the chair noticed; the toast burnt and the brave exchange
in order, the slices given, the three questions and Rowan's answers, the chapter done and Rowan staying; the good chair taken first,
Rowan in the kitchen chair, the toast rescued. The bench case now expects the tea offer after its chapter.
