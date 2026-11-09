# Last orders: the keepsakes (0.4.417)

The fourth and last part of Last orders (docs/BAR_GAMES_PROPOSAL.md, "Taking them home"). A reader who wins the last frame against
the man in booth four, leaves his roll on the table and walks out of the bar has the account: the morning shift's note under the
till. From 0.4.417 the note has two things with it.

## What the reader finds

The note gains a line on the walked-out page: "I've put the house chalk and a tin of darts under here with this. They're for whoever
walked out. If they come back in, they're to have them."

Reading the note gives, once each:

- **the house chalk**: "The house chalk, worn hollow on one side. At a crafting table, with wool, planks and iron, it makes a table
  of your own."
- **a tin of darts**: "Three darts in a tin, their flights flattened from years in a pocket. At a crafting table, with string, planks
  and a target, it makes a board of your own."

Both are marked for that reader, as every keepsake is. Both recipes go into their recipe book. A reader who took the roll, or lost
the evening, has no account and gets neither.

## At a crafting table

| Object | Made from | Pattern |
|---|---|---|
| Pool Table | the house chalk, three wool, two planks, two iron ingots | wool across the top; planks either side of the chalk; iron at the two lower corners |
| Dartboard | the tin of darts, string, two planks, a target block | string above the target; planks either side; the tin below |

The keepsake comes back to the grid unchanged, still marked, so a reader can make as many tables and boards as they like. Someone
else's chalk or tin, or one nobody was given, makes nothing: the result slot stays empty.

## Out in the world

Both are the blocks from 0.4.414 and 0.4.415 and play the same games:

- **The pool table:** eight-ball for two players, or four as doubles; one reader alone plays both sides for practice.
- **The dartboard:** 301 or round the clock for one to four players. Its three house darts are handed to whoever throws and taken
  back after.

Anyone can play them, not only the reader who made them. They pay nothing, keep their own scores, and break by hand into themselves.

## One rule for keepsakes that unlock a recipe

`KeepsakeKeyItem` is the service key's rule from 0.4.413, made general. A keepsake key is marked with its owner and the room it came
from. It comes back from the crafting grid unchanged, and `CraftingKeyMixin` shows no result while a grid holds one that isn't the
crafter's own. `ServiceKeyItem` now extends it, and the house chalk and the tin of darts are plain `KeepsakeKeyItem`s for
`last_orders`.

## What is new

- **Items:** `house_chalk` and `dart_tin` (BarRegistry). Their pictures are drawn by `tools/draw_bar_art.py`.
- **Recipes:** `pool_table.json` and `dartboard.json`, taught with the keepsakes (`LastOrders.teach`).
- **Tests:** the walked-out case in `LastOrdersTests` adds these checks:
  - the note's line, for the walked-out reader only;
  - both keepsakes, marked as that reader's;
  - nothing for the one who took the roll;
  - both recipes in the walked-out reader's book;
  - the chalk making a table and the tin a board, and the other reader's grid making nothing;
  - both keepsakes back from the grid, still theirs.

  The Polybius key case passes under the shared rule.
- Witness 86 / 65 / 63, layout 135 and protocol 170 are unchanged. New builds only.
