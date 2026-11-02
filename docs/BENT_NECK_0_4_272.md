# The bent-neck woman, waiting at the start — 0.4.272

The owner's note on the tunnel (Stooping Giant):

> "Bent-neck lady needs revamped – She needs to spawn invisible, untouchable, at start of hallway. Not teleport there after player has entered. She needs to give player 3 seconds of grace so as to not immediately, accidentally touch them/end the vignette. She also needs to properly reset after vignette has concluded"

## What was wrong

- **She was carried there.** She was made folded and unseen at the far end of the tunnel. Once the reader had been in eight seconds and
  was twelve blocks in, she was moved to a clear spot four to ten blocks behind them and shown. Whatever the reader saw of it, she arrived
  rather than having been there.
- **No grace.** The catch needed her within reach and eight seconds on the reader's clock, which had always passed by the time she
  appeared. Shown four blocks behind a reader at a red light, she could reach them inside a second.
- **She stayed where it ended.** A caught reader is put back just inside the mouth and stays in the tunnel until they leave; she stood
  wherever she had caught them, visible and still following. Through the last green she was hidden where she stood, and a reader who
  walked back past the last signal made her show again, frozen, since the light reads green for good once through.

## What she does now

- **Her place is the start.** She is made in the first cell inside the tunnel's mouth (`UncannyStories.GIANT_HOME`, (0,0,-2)) when her
  reader first comes in, unseen. Waiting there she has nothing to touch: she cannot be aimed at, struck, hit by a projectile, pushed or
  bumped into, and she pushes nobody (`LiteraryActor.waitingUnseen`, `isPickable`, `isAttackable`, `pushEntities`). The reader walks in
  through where she stands.
- **She comes out where she stands.** The same moment as before (eight seconds on the reader's clock and twelve blocks in), and only
  while she is out of the reader's view or hidden from it, she is shown where she is, between the reader and the way they came. Nothing
  moves her there. The reader's clock at that moment is kept (`TunnelOut272`).
- **Three seconds.** For her first sixty ticks out (`UncannyStories.GRACE`) she stands as she does at a green light, whatever the signal,
  and cannot touch anyone, even a reader who walks straight back into her. After that the old rules: she freezes at green, comes on at
  red, and her touch at red ends it.
- **The signs.** The count of steps starts when she comes out; before that the signs keep the detour warning, and once the reader is
  through or caught they are off.
- **She goes home when it is over.** Caught, through the last green, or the account read: she is put back at her place unseen at once
  and kept there while the reader stays (`UncannyStories.settle`). The change to unseen reaches the reader's client before she is moved,
  so she is never drawn for a moment at her place in the pose she left (`TunnelStride.home`). As before (0.4.240), she also goes back
  whenever her reader leaves the tunnel, and a reader who comes back after time away starts over with her waiting at the start.

## Tests

- `UncannyTests#theTunnelWomanWaitsUnseenAtTheStartAndComesOutWhereSheStands` (replaces
  `theTunnelWomanStartsUnseenAfterTheFirstSignalWithoutReading`): made at the start, unseen, nothing to aim at, strike, push or bump into;
  out in place behind a reader well in; unmoved through her three seconds; then comes on at red.
- `UncannyBranchTests#theBentNeckedWomanGivesThreeSecondsAndIsGoneOnceItEnds`: a reader walking into her inside the grace is not
  caught; the step after, at red, they are, and she is at her place unseen at once and stays there; coming back starts over; through the
  last green she is home at once; walking back the way they came never shows her.
- Changed: the re-entry case expects her home as soon as the attempt is over, and at the start after coming back; the great-stride case
  stages her forty blocks in (her place is now the start, so the stale spawn it checks is still far from where she is staged); the strike
  case checks she cannot be struck while waiting and strikes her out; the lamp-walk case unbinds her from a reader who stands outside,
  since she would otherwise be sent home (it used to pass only because home was past the lamps); 0.4.240's leave-and-return case reads
  her place as the start.

All ten tunnel cases pass locally.
