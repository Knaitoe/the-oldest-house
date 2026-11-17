# Heard apart (0.4.661)

The owner's second suggestion of 16 November: "Give co-op players different things to swear they heard. Two people stop walking in the
same hall. One hears their partner's footsteps carry on around the corner. Let them compare stories while they're still standing
there." This is the half for readers together. The half for a reader alone (a feed of the hall just left, with footsteps still going) is
its own release.

Nothing is written, said or shown. What each reader hears is ordinary: footsteps, a door. Only comparing tells either of them that the
other did not hear it. No Witness, layout or protocol change. New builds only.

## Now

- **Where.** The ordinary halls: the domestic halls and the stone halls. Not the explorers' camp, not a story, not the staircase.
- **When.** Two readers in the same hall have both stood still for three seconds, between two and fourteen blocks apart. Both are six
  crossings or more into the House, where its strangeness starts. Neither has been one of such a pair in the last ten minutes of the
  world's time. About one such stop in two, and only once for a stop: moving off and stopping again is a new stop.
- **What one hears.** One of them hears the other's footsteps go on: ten steps at a walking pace, a moment after, starting a stride on
  from where the other stands, down the hall away from them, turning the corner where the hall turns and carrying on round it. Each step
  sounds like the floor it falls on, as loud as a reader's own at first and a third of that by the last. The steps keep to floor a body
  could walk on, never pass through a wall, and never come back towards the one who hears them; where nothing is open they stop.
- **What the other hears.** While those footsteps are going, the other hears a door shut softly somewhere behind the first, the way they
  were both facing from.
- **Which is which.** Either may be the one who hears the footsteps. If one of them has nowhere for the footsteps to go (a wall a step
  behind the other), the other is.
- **Nobody else.** Each sound is sent to its own reader alone. A third reader in the same hall hears neither; spectators are never one of
  a pair and hear nothing of it. A reader who leaves the House or logs off hears nothing more of what was still to come.

## With others

This is only for readers together. Each reader's sounds are their own and are never played in the world, so nobody standing nearby can
confirm either story. Each reader keeps, on their own record, when they were last one of a pair.

## Checked

- `ExplorationPassTests.twoReadersStoppedInAHallSwearToDifferentThings`, in a built hall three wide that turns at its end:
  - The ordinary halls, domestic and stone, count; the camp, a story and outside the House do not.
  - A reader at the House's first doors is never one of a pair; six crossings in and three seconds still, a reader may be; not before the
    three seconds; never a spectator.
  - Two readers shoulder to shoulder hear nothing; moving starts a reader's stillness again.
  - Four steps apart, both still: the reader with hall behind the other hears exactly ten footsteps and nothing else, each on floor a body
    could stand on, each further from them, fading, eight ticks apart, the first a stride on from the other; they turn the corner and carry
    on round it and never pass through its wall.
  - The other hears exactly one door, behind the first, while the footsteps are going.
  - A third reader and a spectator in the hall hear neither; both of the pair keep when it was.
  - The same stop, and a new stop within ten minutes, hear nothing more; ten minutes on, the next stop together can.
  - With only a wall behind, the footsteps have nowhere to go.
  - What is still to come is dropped for a reader who goes, and the rest is sent.

## Not covered

- What either reader hears is checked by the sounds planned and sent, not by a client listening to them.
- The rule that picks a pair (by hall, every half second) runs from the server's own tick and is not driven by a case; the case drives
  the same steps it takes directly, with times of its own.

## New lines

None.
