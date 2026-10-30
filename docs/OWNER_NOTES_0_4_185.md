# Owner's notes, October 30 — 0.4.185

Four small changes from the owner's notes and screenshots.

## The journal: the reader only writes

The owner, on a screenshot of the journal's buttons ("A line", "Mirror", smaller and larger, "Back into the page"): "the player shouldn't have fancy formatting options. those should only exist by the game."

- **The screen.** The journal now has only the page turns. There is no "Lift line" and nothing for a selected line: no shape, mirror, size, turning, dragging or putting back. The reader writes straight onto the paper, as before.
- **The server.**
  - A page sent from the screen carries only its writing (`JournalPage.fromReader`), and only the writing passes the text filter and is kept (`Journals.edit`, `keep`).
  - Any shaped lines already on a page stay exactly as they were, so a modified client cannot set shaped lines either.
- **The House keeps its shapes.** Turned, mirrored, spiral, cascading, column and boxed lines are the House's to set. Zampanò's leaves keep all of them, on the same renderer, and any shaped lines already on a page are still drawn where they lie.
- **Tests.**
  - `JournalTests.editsAreKeptOnlyFromTheOwnerAndWithinWhatThePageHolds` now sends a page with shaped lines and checks that only its writing is kept.
  - The journal proof's sample pages still show every shape the House can set.

## The kitchen table: a child's height

The owner: "ensure players are child height too for the crayon vignette."

- **The scale.** In the kitchen quiet room ("You were small once … somebody had left the crayons out on the table") the reader is scaled to 0.7.
- **Why 0.7.** At that scale the eyes are just over the table top, where the drawing lies, and the fridge door is above them.
  - The child scale the literary rooms use (0.54) would put the eyes below the table top, and the reader could not see the drawing.
- **How it is applied** (`HouseReadKitchen.small`, from `HouseReadSites.reader` each tick):
  - It is a transient attribute modifier, so it is never saved.
  - It is applied only while the reader is in the kitchen, alive and not a spectator.
  - It is removed the moment they leave.
- **Test.** `HouseReadTests.theKitchenTableReadsOnlyTheFaceTheReaderDrawsAndOpensOnceThePictureIsUp` checks:
  - the 0.7 scale in the room;
  - an eye height between one block and 1.2;
  - the reader's own height again once they are out.

## The wall clocks: moving hands

The owner, on a screenshot of a quiet-room wall clock: "would be cool if you could animate clock arms."

- **The hands.** The painted face stays the block's model. The fitting's renderer draws three hands over it (`client/ClockHands`):
  - a short, broad hour hand;
  - a long minute hand;
  - a fine red second hand that ticks once a second.

  They are drawn in pixel widths to match the twelve-pixel face, and they turn with the block's facing.
- **The kitchen clock** says "a little after four": 4:05 the first time it is seen, then running. The kitchen tags its clock when it is built (`Clock: after_four` on the fitting). Its tick sound was already there.
- **The waiting room's office clock** ("a few minutes slow") shows the real hour, four minutes behind.
- **Proof.** The kitchen client proof (`HouseReadKitchenProof`, house-read smoke) adds both clocks to its objects page, drawn through the same renderer.

## The advancements: nothing hidden

The owner saw only the House's tab on a new world and read it as hiding Minecraft's. Nothing in the mod filters vanilla advancements.
- **Vanilla's rule.** Minecraft shows a tab only once something in it is earned.
- **The House's tab** is earned on joining (0.4.171).
- **Minecraft's own tab** comes with the first crafting table.

So a new world shows the House's tab alone, with no tab strip. `AdvancementTests.theHousesTabNeverHidesMinecraftsWhichJoinsItWithTheFirstCraftingTable` checks this with a real player.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
