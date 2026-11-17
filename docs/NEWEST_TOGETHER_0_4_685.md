# The five newest, with more than one reader (0.4.685)

The multiplayer audit (`docs/MULTIPLAYER_AUDIT.md`) had stopped at 88 stories. Six have come into the Witness pool since:
the lodging house, A Little Longer, the sleeping King, Not Yet, Last Seen and Fallow. This release brings the five that had no
conduct cases under the same checks as the rest, and corrects how two of them hold readers.

## How each holds readers

| Story | How more than one reader is held |
|---|---|
| A little longer | A copy of their own for each reader. |
| A sort of thing (the sleeping King) | Up to four in one country; the fifth reader goes to another copy. |
| Not yet | One ward for everyone; each reader who stays down has their own cot and their own drawer. |
| Last seen | One park for everyone; each reader's folder and night are their own. |
| Fallow | One station for everyone; each reader's chore, shadow, answer and account are their own. |

## What was found by reading

- **A Little Longer had no copies.** Its plan says "One private story instance per reader (the scene-copy system), never shared:
  'A second player's jokes or decisions should not speak for the reader's romantic interest.'" Its audit entries and its together
  line ("in another copy of it") said the same. It was never put in `SceneInstances`' list of scenes with copies, so two readers
  went into one shop with one Rowan. Everything in the shop was already kept by where it stands (`LongerRowan.SHOPS`, each chapter's
  work by base, each reader's record by UUID), so registering it is the whole fix: the second reader through the door now goes to a
  shop of their own.
- **The sleeping King had no copies either.** Its proposal says "One dream to a party, by the scene copies (`SceneInstances`): its own
  King, sleep and country", and its audit entries speak of "each copy of the country". It was in neither list, so every reader
  shared one country however many came. It is a party now, four to a country (`King.PARTY`): the three files of the board and the
  King's sleep are written for a few readers, and the fifth goes to another copy. The breath, the battle, the feast and the
  aftermath were already kept by base.
- **The King's together line.** It read "%s {was} in the drawing-room too, in another copy of it.", which was wrong for a party:
  whoever else was there was on the same board. It is now:

  > %s {was} on the board with me while he slept. *(new, flagged)*

## The cases

`CastConductTests` gains five cases (`aLittleLonger`, `theSleepingKing`, `notYet`, `lastSeen`, `fallow`), each a round of that
story's people with one reader, held to the same checks as every other cast: no jumps or steps, nobody inside a block or off the
ground, a line said again only out of earshot, turning to the reader spoken to, one body each. `TogetherConductTests` gains the same
five with two readers, the second coming through the door the House gives them; for A Little Longer that door now opens on a shop of
their own, and for the King on the same country as the first.

`KingTests#walkingOffLeavesHimHisSleep` and `LongerTests`' first case now also check the registration.

## What the King's cases found

The new cases on the sleeping King failed before they passed, and each failure was a fault in the country, not in the check:

- **The Cat stood on nothing.** Its place in the tree was a block and a half out from the canopy, in the air. It now sits on a
  bough at the canopy's west edge (`KingSite.CAT_BOUGH`), with the two blocks above cleared, facing the court.
- **The Cat jumped.** Going from its tree to over the court and back, it moved twenty blocks in a tick, in sight. It goes the way
  the Cat goes now: it fades where it is in a breath of smoke, moves while it cannot be seen, and is seen again a moment later
  (`KingCourt.FADE`, ten ticks each way).
- **The court vanished in sight.** "A pack of cards" took the King, the Queen and the Knave out of the world on the tick the cards
  came down. They go in the puff as the cards land, unseen, and are cleared away five ticks later.
- **The country talked to nobody, and said it again.** Its people spoke over their heads with no reader in earshot, and repeated a
  line still hanging there. Everything said aloud in the country now goes through `King.aloud`: said only if a reader is within 24
  blocks, and not again for ten seconds (`LiteraryActor.saidWithin`).
- **The Queen held a hand she no longer had.** If the reader she was running beside was put somewhere else, she ran on beside
  nobody. She lets go of a reader more than six blocks off.

`CastConduct` counts a body that cannot be seen (invisible) as out of sight for its jump and vanish checks, as it already did for
bodies out of every reader's view.

## Results

Locally on the release commit's tree: the ten cast and two-reader cases with the King's and Longer's own registration cases, 12 of
12; then all fourteen King cases with both King conduct cases, 16 of 16. The release chain (build, package, quick checks, writing,
the literary, architecture and Borrasca namespaces, the newest stories' namespaces, the Witness cases and the client proof) is in
the commit's message.
