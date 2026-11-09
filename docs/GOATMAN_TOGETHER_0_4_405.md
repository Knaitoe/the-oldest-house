# The trailer for several readers (0.4.405)

The owner, in the November 9 playtest notes:

> "Goatman vignette doesn't work with multiplayer as intended."

The note does not say what went wrong. Asked again, the owner said to find and fix the failures. These are the ones found by following two readers through the trailer's door and its evening.

## What was wrong

- **Friends who had each been to the trailer before could never go back together.** Every shared story keeps threads: the readers who started it together carry on the same story. The trailer kept them too, although its evening begins afresh whenever nobody is in it. Two friends who had each sat an evening there alone were in two different threads.
  - The second through the door was told their story "has already parted from" their friend's, and went elsewhere.
  - The first through, with that friend beside them at the door, was dealt some other story the two shared, or the group split.
- **A friend who followed after supper had started found "The door is locked."** Since 0.4.401, a door leads where the first reader through went for as long as they are beyond it. A friend who came through later still had the door dealt to the trailer. The trailer then refused them, because supper had started. They saw only that the door was locked, though their friend had just gone through it.
  - The door also told them, as they came up to it, that the way was still open behind their friend.
- **A friend whose evening had ended walked back into the others'.** A reader who died at the trailer (a fall, a command) and came back through its door was let into the evening the others were still having. The evening then ignored them: no child's size, no seat, no night.
- **A child who stepped out before dark went uncounted.** A reader who left through the door during the evening and came back before night fell sat the whole night with the others. Morning gave them nothing: no count, no tally counter and no haunting, without a word.

## What changes

### Going back together

- The trailer keeps no threads. An evening is over when its morning comes or its readers leave, so an evening sat apart never keeps friends apart afterwards.
- Anyone the trailer can still be dealt to (not yet counted right there, not waiting to wake in the manor) can follow a friend in.

### Too late to follow

The evening still waits out a supper already under way, as it always has. One more child at the table once the plates are down would change the count everyone is trying to keep.

- **What the friend is told.** At the door, a friend who comes too late is told why, by name:
  - "<name> went through to the trailer. Supper has already started there. It is too late to join them. This door leads you elsewhere."
  - "<name> went through to the trailer. It is already dark there, and its door is locked for the night. This door leads you elsewhere."
  - "<name> went through to the trailer. It is morning there, and the others are walking out. This door leads you elsewhere."
- **Where the door leads.** The held door no longer takes them after their friend. It leads somewhere else, as it would for any friend who cannot follow. No white motes show at it, and nobody tells them the way is open.
- **Walking up on their own.** A reader whose own door was dealt the trailer while others are past supper there gets the same line without a name. They go through to an ordinary way on instead of finding the door locked.

### Coming back

- **A friend whose evening ended** stays out of the others' evening until it is over: "The evening at the trailer has gone on without you. This door leads you elsewhere." Once nobody is left in it, the next evening is theirs as before.
- **A child who stepped out and is back before dark** rejoins the same evening, and the night counts for them like anyone else's. A child who left during the night still cannot come back into it.

## Under the hood

- `SharedVignettes.afresh(place)`: shared scenes that begin afresh, the Goatman alone so far.
  - `VignetteThreads.arrived` begins and joins no thread for them.
  - `GroupCrossings.canJoin` and `why` ignore any thread an earlier build kept there.
- `GoatmanVignette.underway(p[, friend])`: why an evening others are in will not take this reader now (supper started, dark, morning, full, or their own evening ended), or null.
  - `GroupCrossings.follows(ServerPlayer, Crossing)` adds it to the follow rule; `before`, `near`, `cue` and `ledThrough` use it, and `why(ServerPlayer, Crossing)` gives its line.
  - `LabyrinthDoors.use` shows it and sends the reader through `elsewhere` in place of `locked`.
- `GoatmanVignette.canEnter` refuses a Failed member while others are there, and `enter` will not put one back into the evening.
- `GoatmanVignette.enter` clears `Interrupted` for a member who comes back before `VIGIL`.

## Tests

New Goatman case `GoatmanTests.friendsWhoEachSatAnEveningAloneGoBackTogetherAndALateFriendIsToldWhy`. It checks:

- that two readers with separate earlier threads can follow each other, and an arrival begins no thread;
- that a friend follows while the evening is on the trail;
- that once supper has started the held door no longer takes a newcomer, and the line names the friend and the supper;
- the line after dark, and that the reader in the evening is not turned away from it;
- that a reader whose evening ended is neither let back in nor quietly re-enrolled, and is told so;
- that a child back before dark rejoins the same evening with the night ahead counted for them.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
- **New builds only.**
