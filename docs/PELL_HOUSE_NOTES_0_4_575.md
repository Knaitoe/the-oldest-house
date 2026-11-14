# The Pell house, the owner's playtest notes: June's room (0.4.575)

From the owner's notes on playing the Pell house on 13 November (the earlier batches: `docs/PELL_HOUSE_NOTES_0_4_566.md` to
`docs/PELL_HOUSE_NOTES_0_4_574.md`). New builds only. No protocol, layout or Witness change.

The owner: "the whole pell house is too uniform in its walls and carpets, and the kitchen isn't spaced believably. all of the
textures could be improved."

The kitchen (0.4.572), the walls and floors (0.4.573) and the front beds (0.4.574) were the three parts of that note. This is
what those notes left as still to come: June's room.

## What was there

June's room is the front room at the east end of the hall. It had its own paper and its own carpet since 0.4.573, a window to
the street with its blind down, and nothing in it. June stands in it every night, facing the window, and the room can be seen
from the hall whenever she goes in or out.

Nothing let a reader out of it either. Her door answers a hand from the hall with "June's room. The door is shut." and stays
shut, and it answered a hand from inside the room the same way.

## The room

Four things, all in one dark timber that nothing else in the house is made of.

| Thing | Where | A hand on it |
| --- | --- | --- |
| Her bed | Along the east wall, its head to the street | "June's bed, made. The sheet is turned down a hand's width, the same all the way across." |
| A bedside table | Under the window, by the bed's head | "A lamp, and a wind-up clock. Its alarm hand is at eleven." |
| A chest of drawers | Against the west wall | "A chest of drawers, older than the rest of the furniture. A doily on it, and nothing on the doily." |
| A wardrobe | The corner by the window, two cells high | "June's wardrobe. Its doors are shut." |

- **The bed** has a candlewick spread pulled square, a pillow, a headboard and a board across its foot.
- **The bedside table** has a lamp and a clock with two bells. Eleven is the hour June gets up and goes out with her lantern
  (0.4.530).
- **The chest of drawers** has three drawers with two brass knobs to each.
- **The wardrobe** has a cornice at its head. It does not open.

The middle of the floor, from the door to the window, is bare. June walks in by it and stands in it, as she did before.

A reader's hand on any of the four is kept with the rest of what they have looked at in the house.

## Her door

- **From the hall,** shut: "June's room. The door is shut." It stays shut, as before.
- **From inside the room,** shut: it opens.
- **Open,** from either side: "June's door stands open." A reader does not shut June's door. She does.

## A reader in her room

June shuts her door behind her whenever she goes in or out. She now looks first.

- **Somebody is in the room.** She does not shut it. She says "Out of there, love. That room's mine." and says it again at
  intervals. A reader who is still there five seconds after she first asked is walked out: she takes them by the arm, walks
  them into the hall clear of the door and lets go, as she does from the shed (0.4.567).
- **Somebody is standing in the doorway.** "Mind the door, love." She waits.
- **Nobody.** She shuts it. If she walked somebody out, she goes back in first and shuts it from inside.

This holds wherever she used to shut that door: going to bed, getting up, and coming back from the pool and from the kitchen
at night.

It is a safety net more than a scene. June sends readers out of the house before she locks up, and at the back door, the
sliding door and after the knife she waits until the house is empty. A reader is in her room at night only if they slipped in
behind her, or are in the house as Tilly.

## Considered and not built

- **A robe on a hook behind the door.** The robe is by the pool, and it is hers there.
- **The lamp lit.** June stands in this room in the dark, and a light behind her blind would be the only lit window at the
  front of the house.
- **June sitting on her bed.** She stands at the window. Sitting needs a pose of its own and a reason to give up the window.

## What the frames show now

Eleven frames from eye level.

- **From the doorway:** the bed along the left wall with its head to the window, the bedside table under the window with the
  lamp and the clock on it, the wardrobe in the corner to the right of the window, and the chest of drawers against the right
  wall with its doily. The floor between them is bare from the door to the window.
- **The bedside table close up:** the clock's face, its two hands, the alarm hand, and its two bells; the lamp; the drawer and
  its knob.
- **The west wall:** the wardrobe's two doors and its cornice, and the three drawers.
- **Back to the door from the window,** and **the door from the hall:** her door, shut, with its knob at hand height.
- **From the lawn:** her blind is down. Nothing of the room shows from the street.

The frames are drawn at full brightness and show shapes and pictures, not the game's light. The tops of beds draw darker in
them than their sides, in every bedroom.

## Checks

- **New case** `PellHouseTests.junesRoomHasHerThingsAndItsDoorOpensFromInside`:
  - the four things where the table above has them, each turned the right way, and her window;
  - the middle of the floor clear; each thing has a line of its own and stops a body;
  - from the hall the shut door says it is shut and stays shut;
  - a hand on each thing from inside gets its line, and the hand is kept;
  - a hand on the door from inside opens it; open, a hand from either side says so and leaves it open.
- **New case** `PellHouseTests.aReaderWhoStaysInJunesRoomIsWalkedOutBeforeSheShutsHerDoor`:
  - a reader in her room at night opens the door from inside;
  - June asks, and does not shut it;
  - five seconds on she takes the reader's arm and walks them out, and lets go in the hall clear of the door;
  - she is back in her place with the door shut, the night is where it was, and nobody is shut in.
- **The street and the lot** (`theStreetAndTheLotAreBuiltOfTheirOwnThingsAndEveryDoorAnswersAsItShould`): the hand on June's
  door is put on it from the hall.
- `tools/draw_pell_things.py --check`: 76 kinds, 218 textures, 155 models.
- **The Pell house namespace**: 63 cases. Architecture: 6 cases. The cast and two-reader namespaces: 102 cases.

## What local testing and the frames found

- **The first full run of the Pell cases failed one older case.** It was the house doing the new thing. The street-and-lot
  case put its hand on June's door with a helper that stands its reader two and a half blocks south of whatever it touches.
  For her door that is inside her room, where the door now opens. The case puts that hand on from the hall now.
- **The foot of the bed showed a strip of bedspread under a rail.** The footboard stood on top of the made bed, so the bed's
  end read as timber, cloth, timber. It is now the bed's whole end from the base up, and the made bed stops against it.
- **These notes first said the room could be seen from the street.** The frame from the lawn shows her blind down. The room is
  seen through her door.

## For the owner

Three things here are new story and not only furniture. Say if any should go.

- **The clock's alarm hand at eleven.** It is the one thing in the house that says when June gets up at night.
- **"Out of there, love. That room's mine."** and **"Mind the door, love."**
- **A reader cannot shut June's door.** Opening it from inside is the reader's; shutting it stays hers.

## Still to come

- The furniture's own textures in the other rooms, where the frames show them flat.
- Each vignette door's own bleed and sound in the halls.
- Put by for later: a ball, and a game of catch with Callum; taking Tilly round the house by the arm.
