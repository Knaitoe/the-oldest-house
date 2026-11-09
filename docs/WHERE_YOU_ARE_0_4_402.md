# The House goes by where you are (0.4.402)

The owner, after 0.4.401: "I mean that the game tracks where it expects players to be and not where they actually are. this is especially apparent when teleporting to/from friends".

## What was wrong

The House knows where a reader is by the doors they came through. That record holds three things:

- the way back, a stack of the doors crossed;
- what lies behind each room's doors on that route;
- the depth, which is the number of crossings on the way back.

Only the House's own crossings changed this record: going through a door, going back, a fold.

A command that moved a reader (`/tp` to a friend, or away from one), or a respawn, moved the body and left the record where it was. Afterwards:

- **The doors** of the room the reader stood in led where the reader's old route dealt them, not where they led for the friend standing beside them.
- **The way back out** took the reader to the last door they had come through somewhere else. A reader who teleported to a friend and walked back out of the room was taken back to their own old hall.
- **The depth** was the old depth. So the stories, stone halls and trials on offer were the old route's, not the friend's.
- **After a respawn in the manor**, walking into the House again carried on from deep inside the old route. The first hall's way back led to a door far down that route.

## What changes (`TeleportAnchor`)

A reader moved by a command (`EntityTeleportEvent.TeleportCommand`), or respawned, takes up where they actually are once they have landed:

- **With a friend in the same room**, they take up the friend's way through the House:
  - their way back becomes the friend's (`LabyrinthData.adoptReturns`), so their depth is the friend's;
  - each room on it is known by the same key (`LabyrinthData.routeKey`);
  - each room's doors lead where they led for the friend.

  Walking back out goes back the way the friend came, room by room. The doors of the room lead where the friend's do.
- **Alone in a room**, the spot they were moved from becomes the way back. Walking back out, the lights go out and they are returned there. The room's doors are dealt from there.
- **In the manor, or out of the House**, there is no way back to keep. The next time they go in, they start from the first hall. A reader who dies inside the House and wakes in the manor starts again from the first hall the same way.
- **A move within the same room** changes nothing.
- **The quiet rooms** keep their own rules, and spectators are unaffected.

Moves the House makes itself (doors, going back, folds, scenes) are unchanged.

## Tests

New co-op case `CoopTests.aReaderTeleportedToAFriendTakesUpTheFriendsWayAndAloneStartsFromWhereTheyWere`:

- **Teleported to a friend:** the friend's way back, the friend's route key and depth, the door here leading where the friend's does, and the room before known as the friend found it.
- **A move within the room:** changes nothing.
- **Alone in another hall:** the way back is the spot they were moved from, and the room's door is dealt.
- **Into the manor:** the way back is cleared.
- **A real `/tp`** by the server's own command system re-anchors the reader to the friend's way back.

The house-read job (house read, tailoring and co-op) now counts 47 cases.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
