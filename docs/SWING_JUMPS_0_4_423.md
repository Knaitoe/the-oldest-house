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

## 0.4.443: a push forward

The owner, November 10: "The swing should offer some forward momentum from jumps too".

A jump now also pushes off forward, the way the sitter faces. Every jump gains a fifth of a block a tick along the swing's forward
(`HouseReadSwing.PUSH`), on top of the seat's carried speed and the hop.

- **Going forward through the bottom of the arc,** a jump lands about three and a half blocks ahead. It was a little over two.
- **At either end of the arc,** where the seat stops, a jump hops forward about a block and a half. It used to drop almost straight
  down.
- **Going back,** the push takes most of the way back off: under a block from the bottom of the arc, where it was over two.

The fall is no higher than before, so it still cannot hurt.

### The empty swing swings on

Before, the still seat and its ropes came back the moment a reader let go, at the swing's rest place. A hop forward from the back
of the arc now crosses that place, and the reader would have come down standing on the seat. So after a jump:

- **The empty seat keeps swinging.** It goes on until the reader has had time to land (12 ticks) and the seat next passes the bottom
  of its arc. That is at most about two seconds.
- **The still seat comes back there,** where the moving seat and the still one are in the same place, so nothing jumps on screen.
- **Nobody can sit on it** until it has come back.
- **Getting off with sneak** is unchanged: the still swing is put back at once.
- **If the swing is left over a reload** while it is swinging empty, it is put back at once, as before.

This is in `SeatEntity` (`CLEAR`, `GIVE_UP`). Nothing new is saved or sent.

### The Hollis yard's swing faces the path

The Hollis swing had been hung facing the yard's side fence, under three blocks from its crossbar. Without the push a jump fell
short of the chain-link; with it, a jump would have reached the fence. The swing now faces the other way, toward the path, with six
clear blocks of yard in front of it and the side fence behind it. A jump going back falls short of that fence by more than before.

The porch's swing already faces open grass.

### Tests

- **`HouseReadTests.porchWhistleVoiceAndAllQuietWaysOnRemainPersonalAndTheRailWritesUnseen`:**
  - where the seat stops, a jump still hops forward at the push's speed, with the hop up;
  - going back at full speed, the push leaves less than the seat's own carried speed, and never sends the reader forward;
  - going forward through the bottom of the arc, the reader leaves faster than the seat's carried speed alone;
  - three ticks after the jump the empty seat is still swinging and the still seat is not back;
  - once it has come back through the bottom of its arc the still swing and its ropes are back, and the moving seat is gone.
- **`BorrascaTests.theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits`:**
  - a jump going forward throws the child south, toward the path;
  - the yard in front of the swing is clear to land in for six blocks;
  - the side fence is behind the swing.

Protocol 177 and layout are unchanged. The turned swing is in new builds only.

