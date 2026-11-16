# A Little Longer 0.4.625: chapter 3, the radio isn't a clue

The fifth slice of the plan (docs/A_LITTLE_LONGER_PLAN.md): "The original tune; the disagreement and its repair; the invitation outside
(direct, flirting, quiet)." Built from the proposal's chapter 3 (docs/A_LITTLE_LONGER_PROPOSAL.md). Code: `labyrinth/LongerRadio.java`,
which takes Rowan over from the kettle chapter once the tea is finished (`LongerKettle`), and plays the tune whenever the kitchen radio is
on (`LongerRowan.tick`).

## What happens

- **The tune.** The kitchen radio now plays an original eight-bar tune (made in 0.4.624, `tools/generate_longer_audio.py`) whenever it is
  on, every thirteen seconds with a breath between, and only while someone is in the place. While Rowan is talking the loop is the
  turned-down take (it ducks under dialogue); once it has been agreed quieter, always. The radio's cues now say a tune comes through.
- **Put it on.** Twenty seconds after the tea, or a hand on Rowan, Rowan asks for the radio. A reader who does not put it on within
  twenty more seconds sees Rowan get up, walk to the worktop, switch it on and sit down again.
- **Leave it on?** Leave it on; say it suits them (a flirt, remembered as `Flirt0625`); ask to turn it down; or worry that something could
  hear it.
- **The disagreement.** Turned down or worried, the exchange runs as the proposal has it. Rowan's next answer is sharper than it needs to be
  and they take their cup to the sink and wash it (a new pose). The reader pushes the practical point, asks why it matters, lets it pass
  (eight quiet seconds), or apologises.
- **The repair.** Whichever, Rowan comes back with it: "That came out badly." / "What did?" / "I wanted a minute where it was just a broken
  radio." The reader wanted the same thing, holds that the doors are real, or offers to keep it on quieter; for the last two Rowan turns it
  down and the quiet take plays from then on. Rowan goes back to their chair.
- **The invitation.** Flirting, plain or quiet, from what the reader did this evening: a reader who said it suits them is asked with the
  extra line; one who came for one cup, or said nothing to the kettle, is asked quietly; anyone else plainly. Either answer, Rowan walks
  up the roof stair on their own feet, opens the stair house door and sits on the bench under the awning with their cup. The roof
  (chapter 4) is the next slice.
- **Kept**: `Mood0625`, `Flirt0625`, `Disagreed0625`, `Repair0625`, `Invite0625`; and from the kettle chapter, `Quiet0624` (said nothing
  to "I'm making tea."). Leaving part way puts the chapter back to the tea finished; Rowan is on the roof bench once they have gone up.

## New lines, flagged for the owner

From the proposal, word for word: "We should be listening for the doors." (the reader's), "We have been listening for the doors.", "They
keep changing." (the reader's), "I know.", "That came out badly.", "What did?" (the reader's), "I wanted a minute where it was just a broken
radio.", "When you finish that, come outside with me.", "Why?" (the reader's), "Because I want to show you something.", "And because you
keep finding reasons to stand up."

New (Rowan):
- "Put the radio on, would you? That one works. I fixed it in March."
- "Leave it on?"
- "Good." (left on) / "It does, doesn't it." (it suits them)
- "Then you listen. I'll do the cups." (the sharper answer)
- "Then we both did." (wanted the same) / "I know they are. Quieter, then. Not off." (the doors are real) / "I can live with quieter."
- "Bring your cup. Come and sit outside with me a while." (the quiet invitation, after the proposal's note)
- "I'll be up there." (in a minute)
- "It's up here. Come and look." (a hand on Rowan on the roof bench)

New (the reader's replies): "Leave it on.", "It suits you, having it on.", "Could we turn it down?", "Something could hear that.", "It's not
about the music.", "Why does it matter so much?", "Let it pass", "Sorry. I didn't mean to spoil it.", "I wanted that too.", "The doors are
still real.", "Keep it on. Just quieter.", "Go with them", "In a minute.".

Changed (slice 1 cues for the kitchen radio, now that it plays): on, "The kitchen radio clicks and warms up, and a tune comes through,
clear as anything." (was "...into a hiss that never finds a station."); off, "The tune stops in the middle of itself." (was "The hiss
stops.").

## Art and sound

- The tune and its turned-down take, registered as sounds (`longer.tune`, `longer.tune_low`).
- Rowan at the sink (`client.LiteraryRenderers`, gesture 12): both hands down in the basin, scrubbing round the cup, shoulders hunched to
  it, head down. Review frames of both casts at the sink (two angles) and on the roof bench with the cup (holding and drinking) were
  rendered and looked at.

## Cases

`LongerTests`: the ask, the radio switched on, the tune heard, the four replies, the disagreement word for word and in order, Rowan to the
sink through nothing solid and busy there, letting it pass, the repair word for word, quieter kept and the quiet take heard, back to the
chair, the plain invitation (and not the flirting line), and the walk up the roof stair through the stair house door to the bench; Rowan
putting the radio on themselves, the flirt kept with no disagreement, the flirting invitation's extra line, "In a minute." with Rowan going
anyway, and the quiet invitation for one cup or silence.
