# Every way on waits for the group (0.4.400)

The owner: "room crossings don't allow time for other players to join?"

## What was wrong

Since 0.4.146, a door into a story has been held for the group (`GroupCrossings`). Whoever follows through it within half a minute goes into the same scene, unless they have finished it or their own story there went another way.

Doors onto the ordinary ways on were never held: halls, trials and rooms to rest in. Each reader's door was dealt from their own route through the House: the doors on their way back, and their own depth. Two friends who had come by different ways, or one a few crossings deeper than the other, could stand at the same door. One went through, and the other followed a moment later and came out somewhere else.

## What changes

Every dealt door is now held behind the first reader through it for half a minute (`GroupCrossings.WINDOW`, 600 ticks):

- **A way on is anyone's to follow into.** A gray room that stands once for everyone (`GroupCrossings.wayOn`) is a hall, a trial or a room to rest in. Whoever follows through the same door in that time comes out in the same room, whatever their own way would have dealt.
- **Stories keep their own rules.** A friend who has finished the story, or whose story there went another way, still keeps their own door and is told why. A one-at-a-time scene still splits the group.
- **The same cue as a story door.** While the door is held, a friend near it sees the motes at it and is told who went through, and that the way is still open behind them.
- **Folds already worked this way.** Since 0.4.394, a fold took the friends behind the first reader back with them.
- **After the half minute.** The door goes back to being each reader's own.

The quiet rooms are not affected: each reader's quiet rooms are their own.

## A door turned elsewhere opens onto a hall that stands

When a door cannot open onto what it was dealt, it is turned to an ordinary way on (`LabyrinthDealer.openWayOn`). This happens when another reader is in a one-at-a-time scene, or when a story is gone for this reader. The way on was picked from every hall the reader could be dealt, including halls not built yet. One still to be built holds its door shut, so the reader was left at a door that stuck. Since the four new halls of 0.4.391, that happened more often. Now a way on that is already standing is picked whenever there is one.

## Tests

- **New case `CoopTests.aFriendWhoFollowsThroughAHallDoorComesOutInTheSameHall`:**
  - the first through a hall door keeps the hall dealt to them;
  - a friend at the door is told the way is open;
  - following through the same door, the friend comes out in the same hall, not the one their own way dealt, and is told of no split;
  - once the door is let go, a reader's door is their own again.
- `CoopTests.aPrivateSceneIsOneReadersAtATimeAndWaitsForTheNext` again finds the next reader in a standing hall when the study is taken.
- The house-read job (house read, tailoring and co-op) now counts 46 cases.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
