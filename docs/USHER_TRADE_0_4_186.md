# Usher's trade — 0.4.186

The owner, on the success-and-failure audit: "Start running with it. I'll edit as needed." This is the audit's first build (docs/SUCCESS_AND_FAILURE_AUDIT.md). It replaces 0.4.184's haunting with a trade, as at the end-of-world cabin:
- **Winning sometimes takes.** Fleeing as the house falls costs the reader the house.
- **Failing sometimes gives.** Being buried leaves a keepsake.
- **Not every failure haunts.** In Poe nothing follows the narrator out of the house, so nothing follows the reader home.

## Fled (closed lid)

- **Gives:** the piece of the house (the stone), as in 0.4.184.
- **Takes:** the house.
  - **When:** reading the account sets `RoomGone0185` on the reader's own Usher record.
  - **How:** a gone room is never dealt to that reader again (`LiteraryVignettes.gone`, checked in `canDeal`). A door already leading there reroutes, as a cabin-closed room does (`LabyrinthDoors`).
  - **What the reader sees:** "There is no door to that house any more."

## Found her in the wing (open lid)

- **Gives:** the stone.
- **Takes:** nothing. The open lid already cost the storm night.

## Buried (still inside when the house falls)

The failure itself is unchanged: no credit, the tarn gives the reader back on the bank, and Roderick and the books are gone for the rest of the visit.

- **Takes:** the tarn keeps what was in the reader's hands, one stack (`UsherStory.KEPT`).
  - **What it can take:** only an ordinary thing. It never takes anything of the House's, nothing with custom data (originals, bound items) and nothing bound to a reader. With nothing ordinary in hand, it keeps nothing.
  - **What the reader sees:** "The house came down over you. The tarn gave you back, and kept what you had in your hands."
  - **Getting it back:** on a later night they get out over the causeway in time, it lies on the bank: "On the bank by the causeway: what the tarn kept of yours."
- **Gives:** *A leaf of the vault register*, once. It is a page in R.'s hand with the reader's own name among those carried down beneath the house: "A house does not give back what it has once taken in. It only lends."
  - It comes through the reward path, so it is owner-marked like the room's other artifacts and can be offered to the Mother.
  - It comes into the empty hand where the kept thing was.
- **The account** of a reader once buried now begins: "The first time the house fell, I was still inside it. The tarn gave me back, and kept what I had in my hands until I got out." The second half appears only if it kept something.
- **The scratching is gone:** `UsherHaunt` is removed.

## Also in this build

**The Goatman's door case:** run 1349's build job failed on `GoatmanTests.openingTheDoorAtNightTakesTheOpenerAliveAndWholeAndItComesHomeWithThem`.
- **What went wrong:** the case clicked the trailer door a fixed four ticks after setting up the night. On a slow full suite, the click could land before the opener was inside the vigil, and the door simply opened.
- **The fix:** the case now waits until the opener is inside, in the vigil and still keeping it.
- **Clearer failures:** its last assertion is split so each part says what it found.

## Tests

**`LiteraryTests.stayingInsideAsTheHouseFallsFailsAndTheTarnKeepsWhatWasInHandUntilALaterNightIsSurvived`** replaces the 0.4.184 case. It checks:
- what the tarn takes and refuses;
- the lantern kept and the leaf given with the reader's name;
- no credit;
- on a later night out in time, the lantern back;
- the account's first line;
- the stone;
- the house gone for that reader once the account is read.

All seven Usher cases pass locally, and so does the Goatman door case.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
