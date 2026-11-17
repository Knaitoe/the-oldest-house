# Fallow 5b: the steps (0.4.684)

The second half of the fifth and last part of *Fallow* (`docs/FALLOW_PLAN.md`). Part 5a (`docs/FALLOW_0_4_683.md`) brought the man down
the road and stood him at the cellar doors. This half is what he asks there, what each reader tells him, whether anyone stands on the
steps, and the account in the keeper's ledger. It is built to the proposal's own ending (`docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`,
section 2, "The road").

*Fallow* is now a Witness source, of the survival kind. The pool is 94. In the same release the owner capped the quota: it is 75% of the pool,
rounded up, but never more than 33 (`WitnessAccount.CAP`), the bar approved on October 7 as the hardest ending. So the quota is 33,
across two kinds; there are still three endings.

## What happens

1. **What he asks.** Stood at the cellar doors, the man turns to each reader who comes within six blocks and has him in view, or who speaks
   to him, and asks after the girl, in the conversation panel and over his head: "Folks ought to stick together. Where's the little one?"
   Then he offers them all a ride in the big man's rig. The first time he asks anyone, the rig's engine turns over out on the forecourt,
   and idles there, its two stacks smoking, until he is gone; nothing in it should run. Spectators are not asked.
2. **Each reader's answer.** A lie ("There's nobody down there."), the truth ("She's down the steps.") or nothing. He says "Is that
   right." to the lie and "Obliged." to the truth, and to nothing he looks past them at the doors. Each answer is that reader's own.
3. **The steps.** When everyone near him has answered, or twenty seconds after he first asked, he goes to the head of the cellar steps.
   - **Someone on the steps** (any living reader, not a spectator, on the treads or at their head, between the doors): he smiles at them, says "Have it your
     way. I'll find her anyway.", and walks back round the doors and up the road the way he came, as far as the far pad's track. She stays
     below. Whoever stood there is told he smiled at them; it is kept that they did.
   - **Nobody on the steps:** he goes down them. The first moment nobody sees her or where she goes (spectators counted), the girl is gone
     out of the cellar: she is sitting out in the field past the yard's west fence, and the ground comes up round her there, ring on ring,
     the shoots through the ash and the young wheat behind them, as it did round her bean. Whoever took her down (Faye, or Otis) says she
     was right there. He comes up, goes round the north end of the trailer, and stands at the yard's fence looking out at her.
4. **Gone.** Walking off, he is gone the first moment nobody sees him once he is past the road (or at the end of his way). At the fence, he
   stands a while (ten seconds) and is gone the first moment nobody sees him. Spectators count as watchers here too.
5. **Ready.** Every reader still at the station when he has gone is ready for the account, and told the road is empty, or that nobody is at
   the fence now. A reader who left before he was gone is not. The outcome is the reader's own: kept her on the steps; told him where she
   was; stayed until he had gone; let him look.
6. **The ledger.** The account is in the keeper's ledger, under the counter by the till, its edges singed: from the light on, the till end
   of the counter shows it on the shelf beneath, scorched. Reading Harlan's note and then the ledger resolves the source (survival).
7. **The girl in the field.** Spoken to, she says "It came up for me."

## The account

`FallowTexts.account`, in the reader's own voice, from what they did and nothing they did not:

1. The station; the keeper's chore (the till, or the girl and her bean); the man who bought a soda and asked the time, and whether they told
   him.
2. The tone and the ground opening; whom they brought to the cellar by the hand; whether they held the doors; under the ground at the light
   (the days by the lantern, digging out) or out in it (waking at the House's door, going back to a store with no roof).
3. Their shadow, and others'; the girl's bean and the ground coming up; the man down the road, and each face they saw him wear, by name,
   and their own.
4. What they told him at the cellar doors, and what came of it: on the steps, someone else on the steps, or nobody, and the girl out past
   the fence.
5. The last line: "She was still down the steps when I left. He had said he would find her." or "When I looked again he was gone from the
   fence. She was still out there, and it was still coming up."

## Single and multiplayer

- **Shared:** the man, his walk, the steps, the rig, where the girl ends the afternoon.
- **Each reader's own:** being asked, their answer, whether they stood on the steps, readiness, outcome and the account.
- **Alone:** one reader decides it alone: stand on the steps, or don't.
- **Together:** one reader on the steps keeps her for everyone; another can tell him the truth and still be kept from her by the body on the
  steps. The together line says who else was at the station.
- **Spectators** are asked nothing and earn nothing, but count as watchers: he is not gone while one has him in view.

## The House's read

- **SELF**: nothing he says names the reader.
- **TONE** and **HAIR**: the last face he wore was theirs (5a); the account says so.

## Made as

- **`FallowSteps`** (new): the asking, the answers, the steps, walking off or going down, the girl out in the field, the fence, gone,
  readiness, the rig idling, and a word with the girl.
- **`FallowRoad`**: the green grows round `greenAt` (the bean, then wherever she sits); the man's taker is kept; his talk goes to
  `FallowSteps.talk`; `comeNow` for tests.
- **`FallowStrike.State`**: `steps`, `stepsAt`, `asked`, `answered`, `kept`, `inField` (the girl out past the fence; `out` already holds the
  readers who came up from below), `rig`, `taker`, `greenAt`. At the light, each reader's `RoadAnswer`, `OnSteps` and the
  rest are cleared, so what they told him and where they stood belong to that one afternoon.
- **`FallowTexts`**: the lines below; the account.
- **`FallowRuin`**: the till end of the counter scorched, the ledger beneath (counter stage 4).
- **Witness:** `WitnessAccount.Story.FALLOW` ("Fallow", survival); pool 94 / 33 / 70 (the quota capped at 33, `WitnessAccount.requiredForPoolSize`); the account advancement; the together line; the
  stair's figure (the man who came down the road); the House's forged line in the Pell house's journal.
- **Sound:** `fallow.rig` (a cold diesel turning over and idling: knocks at about eleven a second over a low drone, with a rattle), by
  `tools/generate_fallow_audio.py`, subtitled "A diesel engine idles".
- **Art:** the scorched till end and the singed ledger, by `tools/draw_fallow_art.py`: `formica_scorched` (the laminate blistered and
  browned in a fan from the window side, widest at the front edge and fading out before the block's sides so it never stops at a
  seam, the front edge black where the fan is deepest) and `counter_back_ledger` (the staff's side: the sacks browned at their tops,
  the cigar box gone dark, and on the right cubby's floor, in place of the receipt books, the keeper's ledger lying flat and seen
  edge-on: cloth covers above and below a ruled block of pages, its spine to the left and its far corner singed). Both were checked in
  close frames beside the plain till end from the front, the back, above and in low light. Counter stage 4 is the register on top of those.
- **Seen at eye level:** him at the head of the steps from the yard and from the treads below; her out in the field with the ground
  come up round her, close to and from the field's far side with him at the fence. From the yard the trailer stands between the steps
  and the fence, which is why nobody in the yard sees her go.
- **Tests** (`FallowTests`): `theStepsKeepHer` (two readers, a lie from the steps and the truth beside him, a spectator who keeps him
  in view up the road and earns nothing, both accounts, the source resolved); `theFieldTakesHer` (one reader says nothing, nobody on
  the steps, the girl out in the field and the ground coming up round her, him at the fence until nobody sees him); `theRoad` now ends
  with him asking after her and the reader saying nothing.

## Every new line

Flagged for the owner. Nothing here names a colour or an age or says how the reader feels.

**Said (over heads and in the panel):**
- The man: "Folks ought to stick together. Where's the little one?"; "That big fella's rig will run. I'll carry the lot of you to town, the
  little one too."; to a lie, "Is that right."; to the truth, "Obliged."; to someone on the steps, "Have it your way. I'll find her anyway."
- Faye, when he finds nobody below: "She was right here. She was right here by me." Otis: "She went right past me. I never saw her go."
- Opal, in the field: "It came up for me."

**Replies:** "There's nobody down there." / "She's down the steps." / "Say nothing."

**Cues:**
- "Out on the forecourt, the rig's engine turns over and catches."
- "He looks at you on the steps, and smiles." / "He turns from the steps and walks back toward the road."
- "He goes down the cellar steps." / "He stands at the yard's fence, looking out at the girl in the field."
- "The road is empty as far as the ash lets you see." / "Nobody is at the fence now."

**The account:** every sentence in `FallowTexts.account`, listed above.

**Witness:** "A light came over the wheat and brought down a gas station. Afterward a man walked down the road wearing our faces and
asked where the girl was."

**Advancement:** "A light came over the wheat and brought down a gas station."

**Together:** "%s {was} at the station with me when he came down the road."

**The House's forged line:** "I told the man on the road where the girl was."
