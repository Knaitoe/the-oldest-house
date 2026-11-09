# Notes from the November 9 playtest (0.4.403)

The owner, after 0.4.402: "more notes from tonight's playtest" ("Too low for players to climb back up.docx"). Each note below is quoted from the document.

## The manor

- **"Make front house door indestructible – a player who was locked outside the house decided to try to break it down lol."**
  - The manor's two outside doors, front and back, are now protected on both sides of the mirror, as the impossible hallway is. `HouseMirrorSyncEvents.isExteriorDoor` covers either half of either door.
  - Mining one makes no progress (`onBreakSpeed`), as with bedrock.
  - Breaking events, explosions and pistons are refused (`protectsImpossibleStructure`).
  - Creatures that break blocks leave them standing (`onMobDestroy`).
  - Opening and shutting them is unchanged.

## The Last Swimmer

- **"Sign doesn't read well."** and **"More poor paint."**
  - The pool's signs were redrawn by `tools/draw_swimmer_art.py`:
    - The NO RUNNING sign is a broad N and O over a running figure.
    - LAST SWIM is two centred lines, with narrow I and L glyphs so both words fit.
    - The EXIT sign is a centred word between two dark rules.
  - Their faces now fill the block, so no letter runs off the edge.
- **"Block of water on ladder."** The pool ladder is no longer waterlogged (`SwimmerBlock.wet`). Only the lane rope and the cover take water.
- **"No dialogue or interaction?"** The pool attendant answers a right-click (`LastSwimmer.talk`). What they say depends on the night:
  - Before the first haul, they point the reader to the strap at the shallow end.
  - During the hauls, there are five rotating lines about the work and the swimmer.
  - Once the pool is closed, they get the lights.
  - While someone is held, they say nothing and tip their head at the door.

## Hazards and trials

- **"Too low for players to climb back up."** In the Still Light and the Soft Floor, a reader who fell through the boards could only get back up by the steps beside the entry. Each room now has:
  - two more ways up, against the side walls partway down the room. Each is the same two steps under its own hole in the floor;
  - a faint glow (light 6) at the foot of every way up, so a reader in the dark under the boards can find them.
- **"Glass barrier in reflection does not seal. Players can walk behind it."** The mirror hall's glass stopped a block short of each end wall and of the ceiling, and readers walked round its ends. It now runs wall to wall and floor to ceiling (`HazardMaps.MIRROR_X`).

## The yard

- **"Players can get stuck on other side of fence."** In the Windbreak, the field ran a block past the wire. A gust or the sheet could throw a reader over the wire into a strip they could not climb back out of. The hedge now stands right behind the wire (`YardWind`).
- **"Lake witch caught on boat."** The lake witch never rides anything:
  - she refuses to mount (`canRide` and `startRiding`);
  - if anything has seated her, she steps off on her next tick.

## Truant

- **"Truant drops players into a void with his 'help'."** The dip in the floor on his "this way, the other one drops" branch ran right up to the door. A reader crossing from a block below its sill came out in the next hall half in its floor.
  - The dip now ends a block short of the door, at a step back up.
  - The door is reached from the hall's own floor level.

## Stories

- **Usher: "She's not in the coffin. She's moving. Right-clicking reads that the coffin lid was left open."** The coffin is shared between readers.
  - **What went wrong:** a friend on their storm night had walked her out to the wing, and a reader on an earlier visit found her there.
  - **Where she is now:** she stands outside only while someone in that house is on their storm night (`stormHere`). With nobody on it, she is back in her coffin, lying still where she was laid.
  - **The lid message:** a reader whose lid was settled by someone before them is told so:
    - "Someone closed the lid before you came. Return and listen."
    - "Someone left the lid open before you came. Return to the wing."
- **Winchester: "Missed mirror opportunity here."** The dining room buffet's glass is a true plane mirror, as the hall stand's has been since 0.4.223.
  - It runs across both halves of the buffet, from the top of the cupboard to the frame's head.
  - Without its left half beside it, it stops at its own frame.

## Not yet addressed

- **"Goatman vignette doesn't work with multiplayer as intended."** The note does not say what went wrong; the owner has been asked.
- **Two screenshots without a note:** a stone room with a coated figure, and a leaf-walled corridor with "im in a hospital" in chat. The owner has been asked what each shows.

## Tests

- **Still Light and Soft Floor cases** check:
  - both new ways up: their holes, both steps and the upper tread a reader stands on;
  - the glow at their feet.
- **Last Swimmer haul case:** after nine hauls, the attendant gives two different lines to two right-clicks.
- **Hall fixtures:** CI run 1488 failed `CoopTests.aPrivateSceneIsOneReadersAtATimeAndWaitsForTheNext`. A re-dealt door opened onto the bedroom hall, which the fixture had never built.
  - Since 0.4.390 the laundry, bedroom and atrium halls have been ordinary ways on. The shared fixture list `EntrancePlaytestTests.HALLS` now includes them.
  - Every fixture that uses the list builds them with `EntrancePlaytestTests.buildHall`. That covers both private-scene co-op cases and the two junction cases in `EntrancePlaytestTests`.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
- **New builds only.** Rooms already built keep their old geometry.
