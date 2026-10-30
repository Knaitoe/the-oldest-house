# The Last Stop and the elk woman — 0.4.187

The owner: "The last stop is inspired by Stephen Graham Jones's The Only Good Indians. The elk entity in that story punished its protagonist for harming animals. That might be better? Consider how to work that out." This is the success-and-failure audit's second build (docs/SUCCESS_AND_FAILURE_AUDIT.md).

## The men read the reader's record

Every fourth thing the men say outside reads the reader's own record back to them, from Minecraft's statistics of animals killed (`ElkFollows.accusation`).
- **A long record:** the two kinds most killed, in words, with the reader's name between: "Forty cows, …. Thirty pigs."
- **One kind only:** "One cow, …. We counted."
- **No kills at all:** the accusation falters: "Never even shot a deer, …? Then why'd it look at you?"

## Striking her is the failure

Striking the elk already wounded her and changed the men's words. It now also marks the night (`StruckVisit0187`).
- **What happens:** the herd or the men take the reader as they take anyone, and they wake on the porch. But the night resolves nothing.
  - "The noticeboard is bare. Nobody wrote about you tonight."
  - There is no clipping and no Witness credit.
  - The capture does not come again that visit.
- **She follows them home** (`ElkFollows`): "Out past the fence something stands and watches you go. It will follow you home."
- **Never in the House.** What follows plays only in the reader's own world, and it never wounds them.

## What she answers while she follows

- **Waste.** The meat of any animal they kill turns to rotten flesh as it drops, and its leather or hide is gone. "It spoiled as it fell." The men in the novel left meat on the hill.
- **In kind.** Each animal they kill is counted. Each morning while the count stands, one of their penned animals near where they sleep is gone, and the nearest gate is left open: "A gate stands open this morning, and one of your animals is gone. The prints by the gate are not a cow's."
  - **Limits:** one a morning, and never more than they have killed.
  - **Never taken:** a named, leashed or tamed animal, or one outside a pen (no fence or gate within four blocks).
  - **This is the harsh option the audit offered,** and it can be cut.
- **The calf.** Killing a young animal costs them that night, until the next dawn: "That one was young. She will come for your night."
  - **No sleep:** they cannot sleep. "You can't sleep. Something is standing out in the field, waiting."
  - **The field:** out on open ground in their world at night, walking more than 24 blocks from where the night found them brings them back. Faint eyes show in the dark on either side. "The field again."
  - **This replaces the audit's "wake in the looping field."** It keeps the field's loop without pulling a sleeping reader across worlds.

## How it ends

On a later night at the Last Stop, the reader reaches an ending without raising a hand to her, and so lets her pass.
- **Killed nothing since their last night there:** "She passes you in the dark and does not look back. She does not follow you now." The night resolves as any night does.
- **Killed since:** the night still resolves, but "She passes you in the dark, and looks back at you. There is more blood on you than there was." The count starts again from that night, so a next visit with nothing killed ends it.

This is how the novel's cycle ends: Denorah faces her and does not shoot.

## The win's trade

Reading the clipping after either ending:
- **Gives** the cap from the broken post, the cap that appears in the field. It is a dyed cap the reader can wear.
- **Takes** the tab: one emerald a drink from their pack, as far as it goes. "The tab came due."
  - If they carry no emeralds: "Marla's tab is still open. She has your name."

## Tests

- **`LiteraryTests.strikingTheElkFailsTheNightAndSheFollowsUntilALaterNightLetsHerPass`.** It checks:
  - the struck night resolves nothing, and she follows;
  - a later night resolves and lets the reader go;
  - the cap is given, and two drinks take two of five emeralds.
- **`LiteraryTests.whileSheFollowsSheAnswersWhatTheReaderDoesToAnimals`.** In the reader's world, it checks:
  - a killed cow drops rotten flesh and no beef or leather;
  - the men's line counts it;
  - a calf makes the reader restless;
  - in the morning the penned cow is gone and the gate open, and the named sheep stays.
- **`theFieldRepeatsUnseen…`.** The peer now strikes the elk there, so the reader still resolves.

All eleven Last Stop cases pass locally.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
