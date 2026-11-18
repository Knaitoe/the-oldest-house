# Taken at the end of the hall; one thing kept for the cell (0.4.704)

The fifth of the owner's notes of 18 November (the other four are 0.4.703, `docs/OWNER_NOTES_0_4_703.md`):

> Finale needs revamped slightly; with player items being taken upon entering house (and, items should only be lost on entering the
> impossible hallway. Not the house itself.) the player needs a way to secure their 'favorite' item to slay the Minotaur with

## Where things are taken

Since 0.4.255 the House took a reader's own things at the front door and put them in the chest of drawers in the front hall. Now:

- **The front door takes nothing.** In the manor (the hall, its rooms and upstairs) a reader keeps whatever they carry. Coming in, they
  are told that the House will take it at the end of the hall, and that the hall stand keeps one thing.
- **The end of the hall takes it.** Going past the end of the hall into the impossible hallway, a reader's own things go into the
  drawers as before. Coming back into the manor gives nothing back; the drawer and its "Retrieve belongings" do, as before.
- What the House lets through is unchanged: its own things, anything with its marks, food and drink, lights.
- Something dug or picked up outside is the reader's own as soon as they come in with it, and is taken at the end of the hall like the rest.

## The hall stand

A hall stand stands just inside the front door, in the corner beside the chest of drawers, facing into the hall. It is drawn at
sixteen pixels:

- two posts under a crest;
- a panel with a small mirror and two brass hooks;
- below them an umbrella well, with a front rail and a zinc tray.

A reader uses it with their main hand:

- **Leave a thing.** Whatever is in their hand goes on the stand, one of it. The stand keeps one thing.
- **Swap.** With something already on the stand, the new thing is left and the old one comes back to the hand.
- **Take it back.** An empty hand takes back the thing on the stand.
- **Arrows.** With a bow or crossbow on the stand, arrows go in with it, all of them, and come back with it.

The stand is each reader's own:

- what is on it is kept in their record;
- only that reader sees their thing standing in the well (`HallStandDisplay`), and nobody can take another reader's;
- what is on the stand is never taken at the end of the hall;
- spectators see nothing on it.

How a thing stands in the well (`HallStandDisplay.look`):

- a long thing (a sword, a tool, a bow, a crossbow, a trident, a mace, a rod) stands on its point, its handle above the rail, flat to the
  wall so it is seen whole from the hall;
- a shield stands upright in the tray;
- a block, or anything small, stands on the tray's floor.

## At the cell

- **Handed over.** When the reader's last fight begins, the thing on the stand is put in their hand, and anything they held goes into
  their pockets. Arrows go into their pockets too. They are told so.
- **It is what the Minotaur answers to.** Whatever it is, the thing becomes the reader's original, and their favorite: it counts one use
  more than the thing they had used most (`WeaponHistory.keepsake`). A sword, a spade or a fishing rod: what the reader chose to keep is
  what can wound him.
- **The witness knows.** Asked about a weapon at the cell, the witness names the thing on the stand.
- **The third ending.** A reader whose thing is on the stand has already laid it down, a long way above; they do not lay their favorite
  beside the cell. The thing stays on the stand.
- A reader with nothing on the stand plays as before: their most used weapon is the one he answers to.

## Single and multiplayer

- **Alone**, the reader keeps their things through the manor and chooses one for the cell.
- **With others**, each reader's stand is their own: two readers at the stand each see their own thing in it, take back only their own,
  and are handed only their own at the cell. The drawers are each reader's own, as before.
- **Spectators** earn nothing: they see nothing on the stand and are handed nothing.

## Made as

- **`HouseKeeping`**
  - Where a reader is (outside, the manor, beyond the hall). Coming in from outside counts their own things (`enter`); crossing beyond
    takes them (`cross`).
  - `STAND`; `Keep.kept` and `Keep.quiver`, saved as `Kept` and `Quiver`.
  - The stand's use (`stand`), `handOver` and `giveBack`. While the reader is in the manor, their display is put back every second.
- **`HallStandDisplay`** (new): an item display sent to its reader alone and never saved, standing in the stand's well; `look` and `well`.
- **`HouseSitting.STAND_DISPLAY`**, drawn by the game's own item display renderer (`SeatClientEvents`).
- **`HouseholdFurnitureBlock.Kind.HALL_STAND`** with its shape; `HouseInteriors.hall` stands it in the hall.
- **`tools/draw_household_furniture.py`** draws three new pictures (`stand_zinc`, `stand_glass`, `stand_brass`), the
  `furniture_hall_stand` model and its four facings; the build job now expects sixty furniture variants.
- **`WeaponHistory.keepsake`**; `FinaleController.start` hands the thing over, and the witness says `KEPT_WORDS`; `WitnessEnding.begin`
  counts a thing on the stand as laid down.

## Tests

- **`HouseKeepingTests`**
  - The first case now checks that nothing is taken in the manor and that everything is taken past the hall. Dirt dug outside is the
    reader's own in the manor and goes into the drawer past the hall.
  - The retrieving case goes into the manor, past the hall and back.
  - `theHallStandKeepsOneThingForTheCellDoor` (new), with two readers:
    - a sword left on the stand, the other reader's stand still empty;
    - taken back and left again; a bow swapped for it, and sixteen arrows put in with the bow;
    - past the hall only the sword goes to the drawer; the stand and its arrows survive saving and loading;
    - at the cell the bow is in the hand and the arrows in the pockets; it is the reader's favorite and wounds; nothing a second time;
    - a spade is made an original too;
    - the display is sent to its reader alone and never saved;
    - how things stand, through the game's own turns: a sword on its point, a pixel above the tray's floor, its face to the hall; a block
      on the tray's floor, a face to the hall.
- **`HouseStructureTests.houseStructure`** now also checks that the drawers and the stand are still in the hall once the whole house is
  built, facing into it (`hallKeepsItsFurniture`).
- Run with them: the two Witness ending cases and the staircase sealing case.

## Found and fixed in the making

- **The sword would have lain across the stand.** The game turns every item display half round before drawing it, and the item
  frame's way of holding a flat thing turns it back. The first version allowed for neither, so a sword would have lain across the stand.
  This was found by reading the game's renderer before any frame was drawn. The display now holds flat things the frame's way. The test
  works through both turns, and the frames were drawn the same way.
- **A block left on the stand** would have hung a pixel and a half above the tray's floor. It now stands on it.
- **The stand was never in the house.** It was first set further along the west wall, in front of the great room's double doorway. The
  builder clears the cells in front of every doorway last, so every new house was built without it. The keeping test sets the stand
  itself, so it passed anyway. The first frame of the built hall found the gap. The stand now stands by the front door, clear of every
  doorway, and the structure test checks that it is there.

## Every new line

All are grey cues on the action bar except the witness's, which is said aloud at the cell.

- Coming in: "What you brought in goes no further than the end of the hall. The hall stand keeps one thing for you."
- Past the hall (reworded from 0.4.255): "Not past the hall: the House has put your own things away in the chest of drawers by the front door."
- The stand:
  - "The hall stand is empty. One thing left on it goes with you to the last door."
  - "You leave the %s on the hall stand. It will be in your hand at the last door."
  - "You take back the %s and leave the %s on the hall stand."
  - "You take the %s back off the hall stand."
  - "You put the arrows in with the %s."
- At the cell: "The %s from the hall stand is in your hand."
- The witness: "The %s you left on the hall stand will be in your hand when the cell opens."

`%s` is the thing's own name if the reader named it, and otherwise the game's name for it.
