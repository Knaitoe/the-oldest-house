# A Little Longer 0.4.626: chapter 4, nothing useful for a while

The sixth slice of the plan (docs/A_LITTLE_LONGER_PLAN.md): the roof, shared silence, the admission, interest asked and answered, Rowan's
hand, Rowan's ask and the kiss, or friendship in kind. Built from the proposal's chapter 4 (docs/A_LITTLE_LONGER_PROPOSAL.md). Code:
`labyrinth/LongerRoof.java`, which takes Rowan over once they are on the roof bench (`LongerRadio`, stage ROOF).

## What happens

- **The sky.** A reader who comes out of the stair house within five blocks of the bench sees Rowan point up between the leaning
  buildings: "There. Between those two. That's the sky." and "I'm not sure it's a real one. I like looking at it anyway."
- **Free time.** The panel stays open. The reader can ask whether it is real, sit on the bench beside Rowan (Rowan moves their cup to make
  room; sitting is needed for the hand and the kiss), say nothing for a while (ten quiet seconds, then "This is nice. Saying nothing."), or
  go back in. Going back in is always offered.
- **The admission.** Once the reader has sat or been quiet, once: "I thought I'd be braver if something like this happened." / "You got
  the lights back on." / "That was wiring. I'm good at wiring." The reader says it matters, says they are frightened too, teases, or sits
  with it. Then Rowan remembers the one thing the reader did in the fuse-board chapter (`Act0623`): held the panel, picked up the screw,
  moved the lamp, or waited at the door.
- **Interest.** After the admission: "I'm glad I stopped." (Rowan: "I'm glad you stayed."); "Was there really something you wanted to show
  me?" for a reader Rowan invited plainly or with the flirt ("Yes." / "The sky?" / "At first."); "Can I sit closer?" when sitting; "Can I
  kiss you?" when sitting, once the reader has been glad, come closer or taken the hand; or "I'd like us to be friends.", after which no
  romance is offered on the roof at all.
- **Rowan's hand.** Six seconds after the reader was glad or came closer, sitting, Rowan holds out a hand: "Here." Take it, or "Not yet."
  ("All right.", the hand goes back to the cup and is not offered again).
- **Rowan's ask.** Eight seconds after the hand is taken: "Can I..." is broken off by the nervous joke ("There's a pigeon. Sorry. There
  isn't a pigeon."), Rowan looks away and back, and asks: "Can I kiss you?" Yes, or "Not tonight." ("That's all right. I'll still be
  here.").
- **The kiss.** Both seated, Rowan leans in; the panel shows only the cue "Rowan leans in. The rain keeps on." A reader who asks while not
  beside Rowan is told "You'll have to come over here for that." Who asked is kept.
- **Back in.** "Come on. It's getting into my collar." Rowan walks down the roof stair tread by tread to the flat and stands at the
  worktop; "Warmer in here. Just about." Chapter 5 (the return indoors) is the next slice.
- **Kept**: `Real0626`, `Sat0626`, `Silence0626`, `Admit0626`, `Meet0626`, `Recalled0626`, `Glad0626`, `Show0626`, `Closer0626`,
  `Hand0626`, `Kiss0626` (ROWAN or READER), `Friend0626`. Leaving part way keeps what was said; Rowan stays on the bench until the reader
  takes them back in.

## New lines, flagged for the owner

From the proposal, word for word: "I thought I'd be braver if something like this happened.", "You got the lights back on." (the
reader's), "That was wiring. I'm good at wiring.", "Was there really something you wanted to show me?" (the reader's), "Yes.", "The
sky?" (the reader's), "At first."

New (Rowan):
- "There. Between those two. That's the sky." / "I'm not sure it's a real one. I like looking at it anyway."
- "Real enough to get rain out of."
- "This is nice. Saying nothing."
- "Maybe." / "Good. I mean, not good. Good that it's not just me." / "Two things. I'm good at two things."
- "You held the panel. Most people would have watched." / "You picked that screw up off the floor. I'd have been on my knees all night." /
  "You moved the lamp before I asked. I noticed that." / "You waited at the door before you came in. People don't, usually."
- "I'm glad you stayed." / "You can." / "Here." / "All right."
- "Can I..." / "There's a pigeon. Sorry. There isn't a pigeon." / "Can I kiss you?" / "That's all right. I'll still be here." / "Yes."
- "You'll have to come over here for that."
- "Then I've got a friend on a roof. That's more than I had this morning."
- "Come on. It's getting into my collar."

New (the reader's): "Is it real?", "Sit down beside them", "Say nothing for a while", "It matters, though.", "I'm frightened too.",
"You're good at a lot of wiring.", "Sit with it", "I'm glad I stopped.", "Can I sit closer?", "Take their hand", "Not yet.", "Yes.", "Not
tonight.", "Can I kiss you?", "I'd like us to be friends.", "Go back in".

New cues: "Rowan moves their cup to make room.", "You would have to be nearer the bench.", "Rowan breathes out, and leaves it there.",
"Their hand is warm from the cup.", "Rowan leans in. The rain keeps on.", "Warmer in here. Just about."

## Art

Three new poses in `client.LiteraryRenderers`, all seated:
- gesture 13, pointing: the right arm raised up and out to the side, clear of the face, the head tipped back after it;
- gesture 14, a hand held out: the left arm forward and out toward the reader on the bench beside, held with a slight steady drift, the
  other hand back in the lap;
- gesture 15, leaning in: body and head tipped toward the bench beside, head a little down, the near arm forward, the other hand in the lap.

Review frames of both casts on two benches (front and from beside) were rendered and looked at. The first pointing pose crossed the face,
and the free arm jutted forward in the other two; both were corrected and rendered again.

## Tests

`LongerTests.onTheRoofTheSkyTheAdmissionTheHandAndRowansAsk` (the whole romantic route: sky, sitting, admission, remembered act, glad,
hand, the joke and Rowan's ask, the kiss, back in) and `LongerTests.aQuietRoofAndFriendshipAnsweredInKind` (silence, admission, friendship,
no romance offered after, back in). The longer namespace passes 13 of 13.
