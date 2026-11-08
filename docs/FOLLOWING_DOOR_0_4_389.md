# The door that follows (0.4.389)

Trial 4 of [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md) (tier 2), which the owner asked for: "The far door moves to whichever wall you aren't looking at. Reach it by backing toward it, or by watching it in a mirror."

## The room

`FOLLOWING_DOOR`, slot 227, a gray room (`TrialRooms`).

- **The room** is eleven by eleven and four high: striped wallpaper, a basketweave parquet, a plaster ceiling and five caged lamps.
- **Nine doors.** Three are set in each of the west, north and east walls, each in a dark wooden frame. All are painted shut but one, which stands open.
- **The passage.** Behind the three walls runs a narrow passage of bare lath and plaster. It leads from every door to the far door, at the back of the north side.
- **Two tall mirrors** hang on the entry wall, either side of the door in, facing into the room. They are true plane mirrors, the same glass as the quiet rooms' and the Winchester hall stand's (`TrialMirrorBlock`, `client.HouseReadReflection`).

The registered far door never moves. What moves is the way into the passage. The design's "the far door moves" is kept as the room's look: nine doors in the walls, and the open one is never where anyone is looking.

## The rules

- **Never seen.** The open door is always one that nobody in the room can see.
  - "Seeing" a door means the room just in front of it is within 75 degrees of where someone is looking, with nothing between. That is wider than any field of view, so the door moves before it reaches the edge of the screen.
  - Spectators in the room count.
  - Anyone already through into the passage does not.
- **Following.** When someone is about to see the open door, it shuts with a click. Another opens out of everyone's sight with a creak: the one most behind whoever is looking. The sounds say where.
- **Held open.** A door with someone standing in it is never shut, whoever looks.
- **All seen.** If every door can be seen, none is open until one can't be.
- **Getting through.** Find the open door by ear, turn your back on it, and walk backwards into it. Or watch it in a mirror while you back towards it. Looking at your feet doesn't help: close to it, the foot of the doorway is in view.
- **Words.** After a reader has watched six doors shut, one grey line: *It is always the one behind you.*
- **Coming back.** Coming back to the room after everyone has left, all the doors are shut, and one opens behind whoever is there.
- **The mark.** Coming through by the far door is a trial and gives its mark (`TrialMarks`, "the door that would not be looked at").
- **Depth.** It is dealt from depth ten, with the other second-tier hazards.

### Together

The doors answer to everyone's eyes at once, so friends get in each other's way.

- **Getting in the way.** A friend facing you sees the wall behind you, so the door can't be there.
- **Helping.**
  - Friends who stand back to back leave it only the sides.
  - Friends who all face the same way leave it the wall behind them all.
  - One can stand in the doorway and hold it open for the rest.
- **The last one.** Once a reader is through into the passage, the doors stop answering to them, so the last one in the room is left to do it alone.

## Art

`tools/draw_trial_art.py` draws nine more textures (32 in all):

- striped wallpaper with a printed sprig;
- basketweave parquet;
- a plaster ceiling with a hairline crack;
- the dark door frames;
- lath and plaster;
- the doors painted shut: the lower half with its knob, and the upper;
- the mirror's dull gilt frame and its glass.

The materials are `trial_follow_wall`, `trial_follow_floor`, `trial_follow_ceiling`, `trial_follow_trim` and `trial_follow_back`. The doors join `trial_fitting` as `follow_panel` (stage 0 the lower half, stage 1 the upper). The mirror is `trial_mirror`: two blocks, with a facing, and the lower block holds the glass. The lamps are the still light's caged lamps, lit. No vanilla building block is used in the room.

## Tests

New case: `HazardVarietyTests.theDoorThatFollowsIsNeverWhereAnyoneIsLooking`.

- **The plan.**
  - Every door has its frame, room in front of it and the passage behind it.
  - The passage is walkable all the way round, and the far door is off it.
  - Both mirrors face into the room, each with its glass.
  - The room is lit.
  - It is a second-tier physical trial with its own mark.
- **Never seen.** With a reader in the middle of the room, exactly one door is open, and it is not one they can see.
- **Following.** Turning to it, it is shut and another opens that they can't see, and again.
- **Held open.** With the reader standing in the doorway, a friend looking straight at it does not shut it.
- **The passage.** The passage is not the room. Once the reader is through, the friend's look shuts the door, and another opens out of the friend's sight.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 128. One appended slot (227). New builds only.
