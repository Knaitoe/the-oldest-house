# Polybius: a cabinet of your own (0.4.413)

Finishing Polybius now leaves the reader something to take home. A reader who walked away from the black cabinet and read the men's
clipboard finds a key taped under its clip. With it they can make an arcade cabinet of their own at a crafting table. The cabinet
plays the arcade's three working games wherever they put it.

## The key

- **When:** given with the account (reading the clipboard), to that reader only, once. The form's last line says where it was:
  "Taped under the clip, a small round key on a ring. Its paper tag says FREE PLAY."
- **What it is:** the Service Key. It is marked for its reader like every keepsake, and its lore line says what it does.
- **The recipe:** the cabinet's recipe goes into the reader's recipe book with the key.

## Making a cabinet

At a crafting table:

```
 G        G  glass pane
PKP       K  the service key
PRP       P  any planks
          R  redstone
```

- The key is not used up. It comes back to the grid as it went in, still marked as its reader's.
- **Only its reader.** A grid holding someone else's key, or a key nobody was given, shows no result, so nothing can be made with
  it.
- Each cabinet takes one craft, so a reader can make as many as they like.

## The cabinet

- **Size and look:** two blocks tall, on the arcade's own cabinet models: the control panel below, the screen and marquee above.
  The screen glows.
- **Placing it:** it needs a free block above, faces whoever put it down, and goes down anywhere outside the House. The House's
  building rule still applies inside.
- **Playing it:** using it opens its game on the reader's screen, the same game the arcade's cabinet plays: Mothlight, Undertow or
  Signal. The black cabinet's game is never one of them.
  - One reader plays it at a time.
  - A game ends the same way as in the arcade. The server plays it again from the session's seed with the reader's keys, and that
    score is the one that stands.
  - Walking more than six blocks away closes the game.
- **Its own table:** each cabinet keeps a table of ten for each game, saved with the world. Anyone who plays it can go on it, not
  only the reader who made it. Its scores do not go on the arcade's tables, and it pays no tickets: the tickets are the arcade's.
- **Changing the board:** crouching and using it changes it to the next game, panel and screen together, and keeps its tables. The
  board cannot be changed while someone is playing.
- **Breaking it:** an ordinary block. It breaks by hand or with an axe, from either half, and drops one cabinet. Broken in
  creative, it drops nothing.

## Made

- `ServiceKeyItem`: the key, its crafting remainder and its owner check.
- `CraftingKeyMixin`: empties the crafting result when the grid holds a key that is not the crafter's.
- `HomeCabinetBlock` and `HomeCabinetBlockEntity`: the two halves, the board, playing, and the tables.
- `Polybius`: sessions can belong to a home cabinet (`playHome`, `submitHome`). Such a session closes by distance rather than by
  leaving the arcade, and never blocks the arcade's own cabinets.
- `PolybiusScores`: the table's reading, writing and ranking are shared with the home cabinet.
- `tools/draw_polybius_art.py` draws the key and the cabinet's item picture, and writes the cabinet's block states on the existing
  cabinet models.
- The recipe and the cabinet's loot table are hand-written data files.
- Protocol 168 is unchanged; the screen and its payloads are the arcade's. New builds only.

## Tests

One new case in `PolybiusTests` (literary namespace, its own batch):

- reading the clipboard gives the key, marked for that reader, and the recipe;
- the key makes a cabinet in its reader's grid and nothing in another's, and comes back;
- the cabinet stands two blocks tall, facing its placer, with its table in the panel;
- it plays for one reader at a time, and its board cannot be changed under them;
- a game stands on the cabinet's own table, not the arcade's, for no tickets;
- crouching changes the board on both halves and keeps the tables;
- breaking it from the screen leaves one cabinet.

## What stays

- **Witness:** 85 sources / 64 resolutions / two kinds / three endings.
- **Layout:** 134.
