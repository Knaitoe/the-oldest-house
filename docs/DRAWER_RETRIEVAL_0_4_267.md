# Taking things back from the hall drawers (0.4.267)

The third of the owner's quality-of-life notes of 2 November (docs/OWNER_QOL_NOTES_2026_11_02.md):

> Convenient retrieval from the hall drawers. Add "retrieve belongings," restore equipment to its previous slots where possible, and
> expose overflow through pages or an overflow count. Currently, only 54 stacks are displayed; additional stacks remain hidden until
> reopening after making room.

Code: `house/HouseKeeping` (the drawer, its pages and the retrieval), `client/KeptClient` (the buttons) and
`network/KeptStatePayload`. The rules of what the House takes and when (docs/HALL_DRAWERS_0_4_255.md) are unchanged.

## Where each thing was

When the House takes something at the front door, it now also notes which inventory slot it was in:
- the hotbar and pockets;
- the armour slots;
- the off hand.

The note is kept beside the drawer, never on the item, so the item stays exactly as it was. Things taken from the crafting grid or the
cursor have no slot to go back to. The House remembers the last 256 of these notes, newest first, and they are saved with the drawer.

## Retrieve belongings

Beside the drawer's six rows, on the reader's screen, is a button: **Retrieve belongings**. It takes everything in the drawer back into
the reader's pockets, in three steps:
1. **Its own slot.** Each thing goes back to the slot it was taken from, if that slot is empty or holds the same thing with room left.
   Armour goes back only to its own place: a helmet to the head, and so on.
2. **Free room.** What has no slot to go back to, or finds it taken, goes into the inventory's free room, as a pickup would.
3. **The drawer.** What still will not fit stays in the drawer.

The House says how it went on the action bar:
- "Your things are back where you carried them." when the drawer is emptied;
- "What would not fit is still in the drawer." when some is left;
- "There is no room on you for any of it." when nothing fitted.

Everything taken out this way is loose, as if taken by hand: carried past the hall, it goes back to the drawer as before. A slot gone
back to is forgotten.

## Pages

A drawer holding more than six rows now turns in pages of six rows. Nothing is hidden any more.
- **Arrows.** When there is more than one page, two arrows under the button turn them, with "1 of 3" between them.
- **Count.** Under them the screen says how many stacks the drawer holds: "130 stacks kept".
- **Changes.** Taking things out of a page, or putting them in, changes only that page. The pages before and after are kept as they
  are.

## Who can press

The buttons are vanilla menu-button clicks. The server honours them only from the drawer's own reader, and only while the drawer is
still open and within reach: vanilla checks that before the click reaches the drawer.

## Tests

`HouseKeepingTests` (exploration), `retrievingPutsThingsBackWhereTheyWereCarriedAndTheDrawerTurnsInPages` (batch hall_drawers_back)
checks:
- **Slots noted.** A reader carries in two stacks of cobblestone, a sword in slot 4, an iron helmet, a shield in the off hand and bread.
  All but the bread are taken. The House notes the sword's, the helmet's and the shield's slots, and still has them after a save.
- **Pages.** With sixty more stacks put away, the drawer shows two pages:
  - the second page holds the rest;
  - there is no page past the last;
  - turning back and forth loses nothing.
- **Retrieval.** One of the stone's slots is taken up by an apple first. Retrieve belongings then puts back:
  - the sword in slot 4, the helmet on and the shield in the off hand;
  - one stack of stone where it was and the other where there was room;
  - the named stones into the remaining free room, so the pockets are full and the rest stays in the drawer.

  Everything that came out is loose, the sword's slot note is forgotten, and the drawer shows what is left on one page.

The original drawer case (`theHouseKeepsWhatAReaderCarriesInFromOutsideInTheHallDrawers`) passes unchanged.

The drawer's buttons are not in the native client proof: that proof runs without a world or a player, and a chest screen needs both.
