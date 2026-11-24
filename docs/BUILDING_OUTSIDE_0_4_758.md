# Building the stories outside: keepsakes and block sets (0.4.758)

The owner chose two of the three options (24 November): each story gives its **signature piece** as a keepsake, and finishing a
story **unlocks that story's block set** so a reader can build it in their own world. Nothing places a whole building for them.

## What a reader gets

When a reader finishes a story (the moment `WitnessAccount.resolved` records it, as actor or witness):

1. **The keepsake.** One block of the story's signature piece goes into their inventory (or drops at their feet if it is full),
   once per story per reader. It carries the story's name and one line in the story's own words. It places like any block, in
   any world. It is not bound to its reader: once set down it is furniture.
2. **The block set.** Every piece of the story is added to their recipe book in one go, with one grey chat line: "The pieces of
   “The back bedroom” can be cut at a stonecutter now." The pieces are made at a **stonecutter**, one from one, out of what each
   piece is made of, read from the sound of the block it was made like: wood from any planks, cloth from any wool, glass from
   glass, metal from an iron ingot, earth from dirt, and everything else (stone, brick, plaster, concrete) from cobblestone,
   blackstone or cobbled deepslate.

Why the stonecutter:
- Many pieces are cut from the same vanilla block; on a crafting table one input can only make one thing, so thousands of pieces
  would collide. The stonecutter already shows every piece a block can become and lets the reader choose.
- The stonecutter in vanilla shows its recipes to everyone. Here it shows a reader only the story pieces they have unlocked; others
  see the ordinary stonecutter. Server and client both filter, so nobody can cut what they have not earned.

## What is in a story's set

- Its **materials**: walls, floors, ceilings, trims, stairs, slabs (e.g. `cold_room_boards`, `cold_room_paper`, `cold_room_treads`).
- Its **fittings**: every kind of its furniture block, each as its own piece in its default stage (e.g. the cold room's bed head,
  bed foot, kitchen table, stove, crucifix, radiator).
- **Not** in it: blocks that make the House work rather than the room (doors back into the halls, accounts and sources while
  still unread, light-only and invisible blocks, triggers), and stage variants that only the story's own events reach (a door
  already broken, a page already burned). Pieces of a bigger thing (a bed's two halves, the plane's parts) are kept: builders place
  them like a vanilla bed or door, one half at a time.
- Blocks shared by many rooms (the halls, the domestic set) belong to no story and are not unlocked.

## Outside the House

A story piece set down outside the House is furniture. It keeps its look, collision and light; it does not run its story (no
pages turn, no one comes up the stairs). Using it does nothing (a held block is still set against it). It breaks like an ordinary
block, by hand, and drops itself in the kind it was; a block the House built stays as hard as the House. A piece cannot be set
down inside the House ("It does not belong in here.").

How: the server keeps, per level, where readers set pieces down (`PlacedPieces`); only those break or refuse use. The client lets
any piece outside the House's dimensions break, and the server puts back whatever it refuses.

## The signature piece, per story

Chosen by hand: the one object a reader would carry out of the room. Built so far (the rest follow in the same spirit, one line
each here as they are built):

| Story | Keepsake |
|---|---|
| The back bedroom (cold room) | the crucifix: "Off the nail over the bed in the back room. It hangs the right way up." |
| The night shift (waxworks) | a figure's plinth: "The stand under the twenty-fourth figure. Nobody painted a plaque for it." |

## Multiplayer

- Each reader's keepsake and set are their own: finishing together gives each of them one keepsake and their own recipes.
- A reader who joins a story partway or watches its end (`resolveAs`) gets both too.
- Spectators get nothing.
- A reader who loses the keepsake can cut another at a stonecutter, since the piece is in their set.

## Building it (slices)

1. **The engine (0.4.758, built)**: `StoryPieces` reads `the_oldest_house/story_pieces.json` (story, block prefixes, skips,
   keepsake, line); every matching block with no item of its own gets a `PieceItem`, each kind of a fitting is its own piece
   (carried as the stack's block state), and the stonecutter recipes are written as recipes load (`RecipeManagerPiecesMixin`),
   so no recipe files are kept. `StonecutterPiecesMixin` filters the list on both sides by the reader's recipe book;
   `PieceBreakMixin` and `PlacedPieces` make set-down pieces ordinary furniture. Item models all point at
   `item/story_piece`, drawn by `client.PieceClient` as the block the stack places; `tools/write_story_piece_models.py` writes
   them (`--check` in the quick gate). Names come from the kind (`kitchen_table` is "Kitchen table") unless the language file has
   `piece.the_oldest_house.<block>.<kind>`. Five cases in `StoryPiecesTests` (namespace `_pieces`).
2. **The manifest for every story**: prefix-to-story table, exclusions, the keepsake for each of the 97 stories.
3. **Review**: eye-level frames of each set's pieces in an Overworld test plot; fix pieces that look wrong as items or outside.

## Defaults the owner may want to change

(All built as listed; any can be changed.)

- Stonecutter rather than crafting table.
- One piece from one vanilla block (cheap); could instead cost more for big fittings.
- Not owner-bound.
- Unlock on finishing as either actor or witness.
