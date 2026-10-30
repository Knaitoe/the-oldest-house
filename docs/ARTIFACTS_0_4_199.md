# Twenty artifacts that do something: 0.4.199

This is build 7 of [SUCCESS_AND_FAILURE_AUDIT.md](SUCCESS_AND_FAILURE_AUDIT.md), "Every artifact does something". The owner: "most of the items you provided the player with don't really have an effect on the gameplay, either inside or outside of the house."

The keepsakes the rooms already gave were given effects in 0.4.192 and 0.4.198. Twenty rooms still gave nothing to carry out. Each now gives one artifact, and each artifact does one small thing from its room's story.

## How they are given

- **When.** When the room's ending is recorded (`WitnessAccount`), as the reader's own (`Artifacts.ended`), once ever per reader.
- **Which endings.** Most rooms record an ending only when the reader came through, and the experiences only when they were lived through. Four rooms choose:
  - the séance's record only for driving the family out;
  - the hotel's key tag only for settling the account or signing;
  - the family's photograph only for leaving when they asked;
  - nothing for a failed uncanny room (the buzzer, the basement, the diner).
- **Rules shared with the keepsakes:**
  - each works only for its own reader; a copy in anyone else's hands does nothing;
  - each is offerable to the Mother;
  - none adds Witness credit;
  - each carries a line under its name saying what it does, in the room's words.
- **"Their world"** means outside the House. An effect that is only for their world never touches the House's own dark, hunters or rooms.
- **The game's own creatures only.** The sitting record and the vial act only on the game's own monsters, and the candle shows only the game's own creatures or anything under an invisibility effect. The House's creatures are its own.

## The twenty

| Room | Artifact | What it does | Where |
|---|---|---|---|
| The wet boards (Bly) | Flora's doll | Carried: the Lady passes them by (`BlyFollows`), and phantoms never come for them. | Both |
| The passing boots (elk carcasses) | A hunter's tag | Crouched: monsters notice them at half the distance. | Both |
| The thirteenth witch (Costume Night) | Entrant's number card | Carried: witches do not fix on them or hurt them. | Their world |
| The flashes over water (Movie Night) | Ticket stub: *Teeth of Summer* | Carried: a boat they paddle forward is pushed a quarter harder. | Both |
| The dare (Winter Lake) | Bud's note | Carried: ice grips like ordinary ground, and powder snow bears them. | Their world |
| The service road (Camp Blood) | Counsellor's whistle | Blown: their own companions within 64 blocks come to them, unless told to stay; once a minute. | Both |
| Proofrock | A page of a student essay | Carried: the drowned do not fix on them or hurt them, and they swim a third faster. | Their world |
| The unhinged door (buzzer) | The scrawled note | Carried: no zombie breaks a door within 16 blocks of them. | Their world |
| The cellar shift (basement) | The worker's broken blade | A sword, as iron; what it cuts is slowed a moment. | Both |
| The second apron (diner) | Diner receipt | Carried: food fills one more point. | Their world |
| The floorboards (Tell-Tale) | The old man's watch | Carried: their footsteps make no vibration; sculk does not hear them walk. | Both |
| Hide-and-clap | Folded blindfold | In the off hand: blindness and darkness do not take them. | Their world |
| The last clock (Masque) | Invitation to the masque | Carried: poison and hunger do not take them. | Their world |
| The empty chair (séance) | The sitting record | Read aloud: monsters within six blocks are driven back and lose hold of them; once every five minutes. | Both |
| The closing account (Stanley) | Hotel key tag | Waking from a night's sleep in their world: fed in full. | Their world |
| The house they remembered (family copy) | The family's photograph | Carried within 32 blocks of their bed: they mend half a heart every two and a half seconds. | Their world |
| The cover (barn well) | A stone with initials | Carried: they crawl through one-high gaps at twice the pace. | Both |
| The call button (hospital) | Patient's bracelet | Carried: they mend on fourteen of twenty hunger, as a full stomach would. | Their world |
| The third count (third-floor bathroom) | The dare's candle stub | Lit: what is near and unseen shows; it burns twenty minutes in all, only while lit. | Both |
| Yellow dust (silo) | A vial of the dust | Broken open: monsters within five blocks are slowed and weakened for ten seconds; three times. | Both |

## Not in this change

- **The key to Mother's room** (the Wheel). Its effect would change the Mother's ransom, and the owner has asked that the Mother not be changed. It waits for the owner's word, as in 0.4.198.

## Build

- **Code.**
  - New: `labyrinth/Artifacts` (the twenty kinds, giving, effects), `labyrinth/ArtifactRegistry` (the items) and `client/ArtifactVisualProof`.
  - New mixins, run on both sides because the reader's own client moves them: `ArtifactGripMixin` (ice friction), `ArtifactPaddleMixin` (the boat's push) and `ArtifactSnowMixin` (powder snow).
  - `WitnessAccount` hands out the artifact; `Keepsakes.line` gives each its line; `BlyFollows` lets the doll's keeper be.
- **Art.** `tools/draw_artifacts.py` draws the twenty 16×16 icons and their item models. CI runs it in check mode. No existing PNG changes.
- **State.** `artifacts_0199` in the House's saved data remembers which artifacts each reader has been given.
- **Protocol.** No new payloads.

## Tests and proofs

- **`ArtifactTests.eachRoomsEndingGivesItsArtifactOnceAndOnlyTheEndingThatEarnsIt`:**
  - the séance, hotel, family copy and a failed uncanny room give nothing for the wrong ending;
  - every artifact is given once for the right ending, marked as the reader's own, offerable to the Mother, with its line;
  - the silo's comes through the real account;
  - no second copy on a repeated ending, and no Witness credit.
- **`ArtifactTests.theCarriedArtifactsWorkInTheirReadersWorldAndForNobodyElse`:**
  - the witch, the drowned and the phantom;
  - the phantoms' night and the Lady;
  - the tag's half sight;
  - the note's door;
  - the receipt's extra point;
  - the watch's silent step;
  - poison, hunger and blindness;
  - the key tag's breakfast;
  - ice, snow and the boat;
  - swimming and crawling;
  - mending by the photograph and the bracelet.

  For each it checks that a copy carried by someone else does nothing.
- **`ArtifactTests.theUsedArtifactsCallDriveBackShowAndSlow`:**
  - the whistle calls only the keeper's free companion;
  - the record drives back only the near zombie, then rests, and does nothing in another's hand;
  - the blade slows what it cuts;
  - the candle shows an invisible zombie, burns only while lit and burns out;
  - the vial slows and weakens a skeleton but not a wolf, and is spent after three bursts.
- **The client proof** renders all twenty baked models (`native-artifacts.png`). CI requires it.

Witness stays 55 / 42 / two kinds / three endings.
