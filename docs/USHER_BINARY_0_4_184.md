# Usher's success and failure — 0.4.184

The owner, October 30: "Each vignette should have a success or failure state and something to represent that binary. See how the Goatman plays for reference."

In the Goatman's night:
- **Success** is surviving the vigil. It earns an owner-bound artifact that keeps working, the tally counter.
- **Failure** is opening the door to it, or being lost outside. Then it comes home with the reader as a private, harmless haunting, and only a later night got right ends it.

Usher now has the same shape.

## Success: an artifact

There are two ways to succeed at Usher:
- **Closed lid:** get back over the causeway to the bank after the fall (`fled_as_the_house_fell`).
- **Open lid:** find her standing in the wing (`found_her_outside_the_open_coffin`).

**The artifact.** Either way, reading the account gives the reader **a piece of the house**: a chip of the fissured stone, owner-bound like the House's other artifacts (`labyrinth/UsherStoneItem.java`). It comes through the same reward path as the elk tooth, the Wheel's key and the cabin's globes.

**What it does.** Held to the ear, it lets the reader hear as Roderick heard.
- **The answer.** The nearest living thing within 32 blocks that is hiding out of their sight answers with a knock from where it is. Only the reader hears it.
- **The limits.** Nothing answers where nothing hides. It never answers for a player or anyone's own animal.
- **Resting.** It rests a minute between listenings.
- **It changes nothing.** It can be offered to the Mother as a vignette artifact.

**The art.** The sprite is drawn by `tools/draw_usher_art.py`, whose `--check` CI runs.

## Failure: the scratching follows them

**The flight.** On a closed lid the fall starts a flight: a reader has 20 seconds (`UsherStory.FLIGHT`) to get back over the causeway. A reader still inside when it runs out has failed.
- **The tarn.** The house closes over them, and the tarn gives them back on the bank, in the dark for a moment.
- **No credit.** Nothing is resolved and there is no Witness credit.
- **The rest of the visit.** Roderick is gone and the books will not begin again: "Not tonight. The house is still settling over you."
- **The next visit.** The house stands again and he is back in his chair.

**What follows them** (`labyrinth/UsherHaunt.java`), as the Goatman comes home with a reader who let it in:
- **The scratching.** Every three to seven minutes, wherever they are outside the house, the scratching Roderick heard under the lid is under the floor beneath their feet.
- **Louder each day.** It grows a little louder for each House day since the failure.
- **Private and harmless.** Only that reader hears it, and it never harms them.

**How it ends.** Only with a later storm night on which they get out in time. That success clears it, and the account begins: "The first time the house fell, I was still inside it. The tarn gave me back, and the scratching came home with me."

**In co-op.** Each reader in the night has their own flight. Those who make the bank succeed; those who don't are buried, each separately.

**Visits.** The fall and the failure each count as a beat of the story, so the next arrival is a new visit, as choosing the lid always did.

## What stays

- The open lid cannot fail: nothing in it threatens the reader.
- Witness stays 55 / 42 / two kinds / three endings.
- **Layout and protocol unchanged:** layout 51, protocol 101.
- **New builds only.**

## Tests

**`LiteraryTests.stayingInsideAsTheHouseFallsFailsAndTheScratchingFollowsUntilALaterNightIsSurvived`.** The flight is shortened for the test. The reader stays inside as the house falls, and the test checks:
- they are buried and set back on the bank, with no credit;
- Roderick is hidden from them and the books refuse for the rest of the visit;
- in the Overworld, the scratching comes under their feet;
- on a later visit he is back, and the storm night comes again;
- the reading runs again, and getting out in time clears the haunting;
- the account remembers the first time;
- reading the account gives the stone, and held to the ear on the bank something hidden in the house answers.
