# The companion key and the wheel's current order (0.4.269)

The fifth of the owner's quality-of-life notes of 2 November (docs/OWNER_QOL_NOTES_2026_11_02.md):

> Companion commands while holding a light. Currently, opening the wheel requires an empty hand or the compass shortcut. A rebindable
> nearby-companion key would remove that inventory shuffle. The wheel should also highlight the current order, so players can
> immediately distinguish "staying" from "failing to follow."

Code: `client/CompanionKeyClient` (the key), `client/CompanionWheel` (the wheel), `opening/CompanionOrders` (`nearest`,
`openNearest`, `menu`) and `network/CompanionNearbyPayload`. `CompanionMenuPayload` now also carries the order.

## The key

**Companion orders** is a key under Controls, in the mod's own category, set to G by default and rebindable there. Pressed in the world,
whatever is in the reader's hands (a torch, a lantern, a book), it opens the wheel for one of their own animals close by.

The server chooses which animal:
- **Who counts.** Only the reader's own cat or dog that could be given an order anyway: alive, in the same place and within six
  blocks.
- **Which one.** Of those, the one most nearly in front of the reader, then the nearest. An animal a step behind loses to one a little
  further off in front.
- **Never.** Another reader's animal, one of the reader's own further off, or anything for someone only watching.

If none is close enough, the House says so as it does for a wheel order out of reach: "Stand close to your own cat or dog to give it an
order." Hillary's introduction is kept: a dog still waiting to be properly met is patted instead, as with an empty hand.

Right-clicking an animal with an empty hand, or with a compass for "find the way out", works as before.

## The current order

The wheel now shows which order the animal is under:
- **On the wheel.** That option has its own darker ground and a thin pale gold edge, under the usual hover.
- **Under the name.** A line in the same gold: "Following you", "Staying here", "Tracking deeper" or "Finding the way out".

An animal standing still while the wheel says "Following you" is failing to follow; one that says "Staying here" was told to stay. An
animal never given an order is following.

## Tests

- `CompanionTests.theCompanionKeyFindsTheReadersOwnAnimalAndTheWheelKnowsItsOrder` (batch companions), with a torch in the reader's
  hand, checks:
  - nothing is found with no animal close;
  - with one of the reader's dogs in front, a nearer one behind, another reader's dog beside them and one of theirs out of reach,
    the key means the one in front, and turned round, the one behind;
  - it never means another reader's dog or one out of reach;
  - a cat counts too;
  - the wheel says "following" for an animal never given an order and "staying" once it is told to stay;
  - the key opens the wheel.
- The native client proof adds `native-companion-wheel.png`, the wheel with "Stay" as the current order, and requires the gold
  edge. The build job requires the image.
