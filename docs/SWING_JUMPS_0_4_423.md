# Jumping off swings (0.4.423)

The owner, November 9: "players should be able to jump from swings. The game should give them an appropriate launch based on their
velocity. This should occur in the household-kitchen vignette too".

## The jump

Pressing jump while sitting on a swing lets go of it. The reader leaves from where the seat is at that moment, not from a spot
beside the swing. They fly on with the seat's own speed along the arc, a little more than it, as if they pushed off with their
legs, and with a small hop up.

- **Going forward through the bottom of the arc,** where the seat is fastest, a jump throws them out ahead, about two to three blocks
  at the swing's widest.
- **At either end of the arc,** where the seat stops for a moment, they mostly drop.
- **Going back,** they go back.

At its widest the seat moves about a fifth of a block a tick; the reader keeps 1.6 times that, plus a hop of 0.3 up
(`HouseReadSwing.CARRY` and `HOP`). That is too low for a fall to hurt.

The swing's motion still comes only from the time the reader sat down, so every client draws it in the same place. The client
sends only "jumped" (`SwingJumpPayload`). The server works out where the seat is and how fast it is going, then dismounts the
reader there and sets their velocity.

## Which swings

- **The quiet rooms' porch.** The rope swing in the yard of the house with the kitchen and the supper table, which is the swing
  meant by "the household-kitchen vignette". It swings toward the house, so a jump forward lands toward the porch.
- **Borrasca's Hollis yard.** Its swing was two chains and a slab, which could not be sat on. It now uses the porch swing's seat and
  ropes on its own posts and crossbar, so it can be sat on, swings and can be jumped from. It hangs across the yard. A jump forward
  lands on the grass toward the path, not in the chain-link.

A swing can now hang either way round. The seat block's facing says which way its sitter faces, and the arc, the drawn seat and
the jump follow it.

## Tests

- **`HouseReadTests.porchWhistleVoiceAndAllQuietWaysOnRemainPersonalAndTheRailWritesUnseen`** now jumps instead of getting off:
  - at full reach, going forward through the bottom of the arc, the reader leaves north (ahead), faster than the seat, with the hop;
  - they leave from the seat;
  - their own client is sent their new speed;
  - the still swing and its ropes come back.
- **`BorrascaTests.theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits`** now checks the Hollis yard's swing:
  - it has a seat and ropes under the crossbar;
  - a child can sit on it;
  - a jump going forward throws the child north across the yard.

## What stays

- **Witness:** 86 / 65 / 63.
- **Protocol:** 171 (one new payload).
- **Layout:** 135.
- **Content note:** unchanged.
- **New builds only.**
