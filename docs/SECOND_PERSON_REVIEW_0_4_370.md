# Second Person, the review (0.4.370)

The owner asked for Second Person (`docs/SECOND_PERSON_0_4_274.md`, `docs/SECOND_PERSON_0_4_369.md`) to be tested for three things: AI-isms in the writing, prompts that say the same thing twice, and animations that are wrong. This release fixes all three.

- **Witness:** unchanged, 81 / 61 / two kinds / three endings.
- **Layout:** 122. **Protocol:** 163, for the five new gestures. No new fittings; one new model state (the phone's empty cradle) and one carried model (the receiver).

## The writing

Lines that explained themselves, padded a beat, or sounded like a narrator taking a bow:

- **Removed:**
  - the armchair's *"Nothing happens. That is its own answer."*;
  - *"To you."* after Iris speaks past them into the room, and *"To nobody."*;
  - the autumn's opening radio cue (the radio is in the season's page already);
  - the first winter night's book page, *"Night. They are asleep. Or they were."*, is now *"Night."* like the others.
- **Rewritten:**
  - the resident at the knitting: *"I don't know how to do this. I'm doing it anyway."* is now *"I've dropped one. Where's it gone."*;
  - the resident on Iris's absence: *"It's so quiet without her through the wall."* is now *"She had the radio on every night. You could hear it through the wall."*;
  - House page 80: *"They thanked the kitchen."* is now *"They stopped asking who had."*;
  - the account's armchair night: *"nothing happened, which was its own answer"* is now *"in the dark, and they never knew"*;
  - the account's sleeve: *"finishing it, badly, and finished it"* is now *"with it, dropping stitches, until it was done"*;
  - the sleepwalk in the account: the hall chair and the street window no longer both stand *"as if waiting"*. They use what the resident said that night: *"asked if it was them next"*, *"said it would stop in a minute"*.
- **Trimmed:**
  - *"a long time"* where it was stacked: the glass, the care mornings, the photograph and its account line;
  - *"A short grey day."* to *"A short day."* (the House names no colour);
  - the summer's *"The bed has an underneath. The cupboard has a dark."* to the plain *"under the bed, or in the airing cupboard"*;
  - the night drawing cue to *"On the kitchen table, a face drawn in pencil in the dark. The one you drew."*
- **The rota** is on the kitchen wall, where the flat puts it, not the fridge.

## The prompts

- **The hint** after the opening now says only what the opening does not: *"Right-click something of theirs to pick it up, and a shelf, a table or the floor to set it down."* The old hint repeated the opening's *"Move something"* and the cupboard.
- **The arrival:** *"Somebody's boxes. Somebody is coming up the stairs."* is now *"Someone is coming up the stairs."*, with a footstep on the stair. The boxes are in the room.
- **The end of autumn and winter** both said where the cupboard was again; each now says *"The flat goes quiet."* The end of summer, the first time it matters, still says the cupboard is warm.
- **Season turns:** the action bar carries the first sentence of the new season's page (`SecondPersonStory.first`). The book keeps the whole page.
- **The glass prompts** say where to stand: *"Stand behind them at the basin, where the mirror would show you."*; *"Stand behind their chair, where the dark screen would show you."*
- **The ticket away:** *"The door shuts behind them. The flat is empty but for you."* replaces a line that told the reader nothing would move.
- **The last morning:** *"The front door is on the latch."* comes after the resident has spoken (six seconds in), not over them.

## The animations

`SecondPersonPoseProof` (run with `-PsecondPersonPoseSmoke`) draws the resident in all fourteen gestures. It puts them on the furniture the story uses, at the heights the server gives, with what the server puts in their hand. It saves a front view and a side view to `build/font-smoke` and stops.

What it showed, and what changed:

- **Sitting:**
  - the cane chairs (the resident's, the guest's, the hall chair) are 0.1 higher than the armchair and sofa, so the resident is raised to sit on them, not in them;
  - the night reaction at the bedroom door was a perch in mid-air; it is now a seat on the bed's edge (`bedEdge`, 0.12 up).
- **New gestures:**
  - 8, looking down at the phone in their hand (the picture);
  - 9, walking with one bag (the ticket away, the latch, out);
  - 10, knitting in the armchair once the sleeve is found;
  - 11, walking asleep: slow, arms a little forward, head down, a short stride;
  - 12, a hand out to the latch.
- **Carrying the last box:** the moving box is in their arms now, drawn between the hands (`Held`).
- **In their hand,** set by the server (`SecondPerson.props`): the receiver on the landline, their phone for the camera and the picture, a bag, Iris's sleeve while knitting. The phone's cradle is empty while the receiver is out, and the sleeve leaves the chair arm while they knit.
- **The landline:** the arm crossed the face, with the receiver above the head. The arm is now raised beside the head. The receiver's in-hand model is solved for that pose, so it runs from the hand at the temple down the side of the face to the mouth.
- **Hands:** a swing where they take, set down, post, hang, knock, open and lift things. Before this the resident reached for nothing.

The proof caught one fault of its own: a bare model starts young, at half size, unless something clears it every frame. The renderer does that and the proof did not, so the proof now clears it too.

## Tests

The four Second Person cases are unchanged except for the sleeve's account line. The pose proof is a manual client check, like the other `-P...Smoke` proofs.
