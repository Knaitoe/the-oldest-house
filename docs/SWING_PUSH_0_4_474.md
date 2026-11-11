# A push on the swings (0.4.474)

The owner, 11 November:

> also, please ensure players can push other players on the swing set

## What was there

A reader on one of the park's swings could take it higher by leaning with it (0.4.465), and that was all anybody could do for
it. A hand on somebody who was on a swing did nothing, on any swing in the game.

## The push

A hand on somebody who is on a swing pushes them. (The hand is the use key, on the rider. A ridden swing's own seat is not
there to be touched: the rider is.)

- **Where to stand.** On your feet, behind the swing or in front of it. From beside it nothing is pushed, and the one trying
  is told where to stand.
- **When.** A swing is pushed as it leaves the hands' end of its arc, or as it stops there (hands may meet it a fifth of a
  second before the top). That widens the arc.
- **Too soon.** A push made while the swing is still coming at the hands takes some of the arc off it, and the one pushing is
  told it was too soon. So holding the key down does not do the timing for anybody.
- **Out of reach.** Out past the middle from the hands a push does nothing, and they are told to wait for it to come back.
- **From still.** A swing hanging all but still is started, from the bottom of its arc, away from the hands: forward from
  behind, backward from in front.
- **One push a pass.** The same hands push again only after a second, which is longer than a swing takes to leave them. A
  second reader at the other end can push as well: one behind and one in front keep a swing going from both ends.
- **What each is told.** The one pushing is told once how ("Push as the swing starts away from you. Too soon, and you take
  the swing off it."). The one pushed is told who, once for each pair of hands ("… gives the swing a push."). Both in the
  grey of every other hint, over the hotbar; nothing in chat.
- **Who does not push.** Somebody sitting on a seat of their own; a spectator; and a hand with a ball of the park's sand in
  it, which throws.

## The park's swings

The four swings of Calder Park are pushed as high as they are pumped: each well-timed push adds to the arc, up to the same
widest arc a rider reaches by leaning. The rider's leaning and the pushing add together, so a pushed rider who also leans gets
there sooner. Left alone a high swing still eases back to its gentle arc, so it has to be kept up.

The chains are heard on each push. Both readers' records keep it, and both accounts say so: "I pushed … on the swings."; "I
sat on one of the swings, and … pushed me."; and one who was pushed and then let go, "I got one of the swings as high as it
would go, with … pushing, and let go of it, and came down … paces out in the sand."

## The rope swings

Any swing a reader can sit on can be pushed. The porch's kind hangs on ropes (Drisking's yard swing is one), and has always
had one gentle arc that it comes up to by itself. A push starts it at once, and takes it a little wider than that arc: at
most two fifths wider. It is never pushed wider than there is room for: before the arc is widened the seat and the one on it
are tried at both ends of the wider arc, and if anything stands there the arc stops short of it. (Drisking's yard has its
side fence three blocks behind its swing; the widest arc clears it.) Left alone, the push eases off and the swing is back on
the arc it has always had. With nobody pushing, nothing about a rope swing has changed.

## Where it lives

- `house/SeatEntity.push(hands, side)` is the rule: the timing, the wait between pushes, what a push adds or takes, and the
  room a rope swing has. It answers `SENT`, `CHECKED`, `FAR`, or nothing when those hands pushed too lately.
- `labyrinth/HouseReadSwing.hand` is the hand on the rider: who may push, from where, and what each is told. Its numbers
  (`PUSHED`, `CHECKED`, `STILL`, `BESIDE`, `ROPE_HIGH`, `MEET`, `PUSH_WAIT`) and lines are there.
- `labyrinth/BikeNight.swingPushed` keeps the park's records (`SwingPushes`, `SwingPushed`, `SwingPushedBy`).

## Tests

`BikeNightTests`, two more cases (28 in the namespace):

- **A reader on a swing is pushed by another on the sand who times it.** A hand on somebody standing is nothing; from beside
  the swing nothing is pushed; a ball of sand in the hand is not a push; from behind, a still swing starts forward from the
  bottom of its arc; the same hands do not push again at once; out past the middle a push does nothing; a push while it is
  still coming takes exactly what it should off the arc; a push as it stops at the top sends it on; pushed each time it
  starts away, the swing reaches its widest and goes no wider; nobody pushes from a seat of their own; from in front the push
  comes as the swing starts back; a still swing pushed from in front starts backward; what each is told; both records; both
  accounts.
- **A rope swing is pushed a little wider and no further than there is room.** A rope swing hung in the air over the park for
  the case: a push from behind starts it; left alone the push eases off and the swing is exactly on the arc it has always
  had; two timed pushes take it to the widest a rope swing goes; with a wall put where that arc would reach, the next push
  takes it only as wide as there is room for.

## What I have not seen

- **Any of it in the game.** In particular whether a child-sized reader finds the rider under the crosshair easily. The
  reasoning is that the push belongs at the top of the arc nearest the hands, where the rider is all but still and right in
  front of the one pushing, but I have not stood there.
- **Two real clients.** The cases use the fixture's readers, and post the game's own event for a hand on another player.
- The pushing arm's swing as the others see it.

## Numbers

No layout, protocol or Witness change. One more piece of a seat's synced data (how far a rope swing has been pushed), so a
client and a server of different builds will not agree about a pushed rope swing; they already must match. New builds only.
