# Lines over heads go once read (0.4.398)

The owner, of the Masque's red figure saying *The clock is speaking.*: "his dialogue does not disappear."

## The cause

A character's line over their head is shown by `LiteraryActor.say`. It put the line up and never took it down.

Some scenes cleared their own speakers' lines after about five seconds: the elk lot, Costume Night, Movie Night, Winter Lake and Borrasca. The rest, the Masque's red figures among them, kept the last line up for good.

## The change

`say` now puts a line up for long enough to read:

- five seconds for a short line, longer for a long one, up to twelve seconds;
- then the line goes;
- saying it again keeps it up;
- the time left is saved with the character, so a line half-read when the world is saved goes on schedule after a reload.

The scenes that cleared their own lines still do. Nothing else about what characters say, or when, changes.

## Tests

`LiteraryTests.movingWhileTheClockSpeaksPutsTheReaderBackARoom` now checks that the red figure says the clock is speaking, and that the line is gone once it has been read.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.

## Speak Up: one of each at the table, and room before the way on

The owner: "two variants of the same NPC spawned?" and "the table is also too near to the door for comfort".

- **One of each.** The four at the table are saved with the room. When the world was closed and opened again with the reader at the table, a second four were set down over the first. They sat exactly over one another until Dell moved his chair, and then there were two of him. Now the ones already sitting there are taken up again, and any second of one is sent away.
- **Room before the way on.** The table, its six chairs, the sideboard, the jug and the lamp stand two blocks further from the way on. Two clear blocks now lie between Dell's chair and the door. Dell still sits at the head, in line with the way on, and still drags his chair aside for a reader who asks to leave.

## Movie Night: the post with the life ring is on the sand

The owner: "move the wooden post forward one block so it's off the pier. onto the sand and beside the blanket". The post and its life ring stand one block east of where they were, on the sand beside the blanket at the dock's foot.

## A test that no longer depends on its neighbours

`PetsAndClapTests.playerOfferRequiresTheSameClosedRoomIncludingACeiling` failed once in CI (run 1485). Its template is a single block, so it built its small room in space shared with other tests. An opened wall could lead into a pocket another test had built, and the trap still read as closed. The room now stands high in open air and is cleared afterwards.

## Tests

- `SpeakUpTests.theTableAnswersForTheReaderUntilRuthSaysYouCanAnswerAndTheEmptyChairRepeatsIt` forgets the cast the way a reload does, then checks that each reader's table still has exactly one of each.
- `LiteraryTests.fourCanoesRowToTheFarPierAndAnEmptyCanoeReturnsToItsSlot` passes on the rebuilt dock.
