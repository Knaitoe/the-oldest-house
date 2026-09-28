# The opening sequence

Everything that happens before a player first enters the House: Navidson's letter, Hillary, and the door. Each player runs their own timeline. The code lives in `io.github.knaitoe.theoldesthouse.opening`.

## Stages

`NONE → ELIGIBLE → LETTER_DELIVERED → DOOR_PLACED → ENTERED`, stored per player in the `the_oldest_house:opening` data attachment (copied on death), together with `nightsSlept`, per-door use counts near the respawn point, `firstJoinDay`, `letterDay`, the entrance door position and Hillary's UUID.

World-level `OpeningWorldData` (`the_oldest_house_opening.dat`) holds every entrance door with its owner and the exact block states and block-entity data it replaced (`OpeningSequence.removeEntranceDoor` restores them; this is the hook for the House's eventual collapse), plus any Hillary currently on her way out of the House.

## Timing

- A **completed sleep** is a wake-up in the Overworld's post-night morning window, counted once per day.
- A player becomes **eligible** when their respawn point is a bed in the Overworld, `nightsSlept >= minNightsSlept` (2) and `minDaysSinceJoin` (3) days have passed since they first joined.
- A **morning** is Overworld day time in `[0, 1000)`. It is handled once per player per day, and only while the player is in the Overworld with their bed's area loaded. A player who is away, in another dimension or offline gets the step on the first morning they are home.
- The letter comes on the first morning after the player became eligible; Hillary and the door on the first morning after the letter.

## Morning 1: the letter

The doorstep is the block in front of the most-used door within `doorstepSearchRadius` (32) of the bed, on the side that can see the sky (a heightmap test, so fresh roofs count immediately), or else the side away from the bed. Fallbacks: the door nearest the bed, then the floor beside the bed.

The book `Howdy, Neighbor` by `Will Navidson` uses the font `the_oldest_house:navidson`. The mod ships that font as a reference to Minecraft's default, so it is always safe; a resource pack can replace `assets/the_oldest_house/font/navidson.json` with a handwriting face. The spec's page 2 wraps to 16 lines in the default font (a book page holds 14), so it is split after "We measured twice." and the book has six pages. `OpeningTests.letterPagesFit` checks the wrapping with the default glyph widths.

The snapshot is a locked filled map whose colours are pre-baked in `data/the_oldest_house/snapshot/porch.bin` (128 × 128 map colour ids; datapacks may replace it). `tools/make_opening_assets.py` draws the art, bakes it to the map palette and writes a preview to `tools/snapshot_preview.png`.

Both items are `the_oldest_house:delivered_item` entities: ordinary item entities that never despawn, can only be picked up by the recipient, and are not pushed by water.

**The House moves in next door.** The letter says the Navidsons just moved in, so the letter's morning also spawns The Oldest House near the recipient's bed if it does not exist yet (the same site search as the natural spawn). If no site is found, the door retries when it is used.

## Morning 2: Hillary and the door

Hillary is an ashen wolf named `Hillary` (name not always shown), persistent, untamed and tagged with the `the_oldest_house:hillary` attachment (recipient and home doorstep). Until tamed she is restricted to 6 blocks around the doorstep and walked back if she strays. The first bone from her recipient always tames her; anyone else's bone is refused with a growl.

The door goes in a wall within `doorSearchRadius` (12) of the bed:

- two stacked full, solid, opaque ordinary blocks (no block entity, redstone component, door, glass, bed or unbreakable block), not placed by the player in the last minute;
- air two blocks high in front, with a sturdy floor, on a side the player can walk to from the bed (a walk search with steps and short drops);
- at least 4 blocks from any other player's door and never on the same wall, and not inside the House's own footprint.

Candidates are scored to prefer walls of the player's house over hillsides: indoor faces, walls flanked by more wall, and walls backed by solid blocks (a door that leads nowhere). The door is placed only where the player cannot see it; if every candidate is in view, placement waits until they look away. With no wall for `freestandingDoorAfterFailedNights` (3) nights, a freestanding door with two stripped dark oak posts and a lintel is placed on open ground within 8 blocks of the bed.

`the_oldest_house:entrance_door` is a dark wooden door: unbreakable in survival, not movable by pistons, immune to withers and the dragon, deaf to redstone and wind charges, and it does not fall if its floor is dug out. It uses the iron block-set type so villagers and zombies do not treat it as a wooden door.

## The door and first entry

The owner's use hands off to the existing transition: the server sends DOOR context, the client plays the door passage screen, and on acknowledgement the player arrives in the House dimension just inside the front door, facing down the hall (`HouseTransitionEvents.beginEntranceTransition`). Anyone else hears a locked rattle. The first entry into the House by any route sets the stage to `ENTERED`.

Leaving the House works as before: the ordinary front door leads out to the Overworld at the House itself, next door to the player's home.

## The House won't keep Hillary

If Hillary is tamed by the player, standing and within 12 blocks when they go through the door, she goes with them: she appears just ahead in the hall and runs towards the threshold. Once she is out of the player's view (after at least half a second), or after two seconds, her entity is saved, removed from the House dimension and set down, sitting, on her doorstep in the Overworld. The doorstep chunk is loaded with a short ticket for the purpose. While she is between dimensions her saved data is kept in `OpeningWorldData`, and a Hillary found in the House after a restart is sent home the same way.
