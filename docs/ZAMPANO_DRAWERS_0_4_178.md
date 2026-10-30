# Zampanò's drawers — 0.4.178

Step 2 of [JOURNAL_DESIGN.md](JOURNAL_DESIGN.md): Zampanò's courtyard on the journal's page renderer.

> "You might be able to use the same journal system to help in zampano's courtyard. It should allow for the sort of spiraling and cascading text featured in the actual house of leaves book. The ergodic style?"

## The cabinets

- **Where they stand.** Three filing cabinets stand in the courtyard archive, against the north wall under Zampanò's scribbles, at x -1, 0 and 1, z -38. They face the iron door, so a reader coming in walks straight up the aisle to them.
- **The block.** `zampano_cabinet` (`ZampanoCabinetBlock`) has three properties:
  - `facing`;
  - `cabinet` (0–2, left to right);
  - `drawer` (0 when shut, 1–3 for the open drawer, top to foot).

  That is 48 states. It cannot be broken and drops nothing.
- **Textures.** Worn walnut and a carcass with dark gaps between the drawers. Each drawer front has a brass card holder and a brass cup pull.
- **An open drawer** stands six pixels proud, and the box behind its front shows the folders on edge with their tabs.
- **The art** is drawn by `tools/draw_zampano_cabinet_art.py`: five 16-pixel block textures, the leaf icon, four models and the blockstate. CI runs it with `--check`.

## Opening a drawer

- **Which drawer.** A hand on a drawer front (the front face only) opens the drawer under it: the top, middle or foot third of the front.
- **What is shared.** The drawer slides out (a pick-up click and a page turn), and its folder opens on that reader's screen. Which drawer shows open is the one thing readers share. If two readers have folders from the same cabinet open, the cabinet shows the newest.
- **Letting go.** A reader lets go of a folder when they:
  - close the screen;
  - walk more than six blocks away;
  - die;
  - change world;
  - log out.

  The drawer goes back in, with an insert click, once nobody holds a folder from it.
- **The server's record** of who holds which folder (`ZampanoDrawers`) is not saved. It is swept every ten ticks.

## The folders and their leaves

- **Nine folders** (`ZampanoLeaves`), one per drawer, each holding two leaves. Drawer d of cabinet c is folder 3c+d, and holds leaves 2f and 2f+1.
- **The writing.** The leaves are written for the courtyard in Zampanò's manner. Nothing is quoted from the book.
  - Measurements of the hall.
  - Echoes that come back late, with a note on a book that does not exist.
  - The plan of the hall as the readers entered it.
  - The stair counted aloud.
  - The readers.
  - Notes on the notes.
  - The growl.
  - Doors.
  - Last leaves.
- **His readers.** The seven historical readers who read to him (Beatrice, Leonie, Pauline, Ruth, Anne, Esther, Helen) appear in them.
- **The shapes.** Every leaf is drawn by the journal's own renderer, in Zampanò's font. Between them they use every shape the journal has:
  - lines at any angle, upside down or up the margin;
  - echoes shrinking across the page;
  - a growl growing to fill it;
  - a spiral that turns and does not come back;
  - cascades falling down the page like a stair;
  - two columns side by side;
  - boxed notes that cite each other;
  - the plan of the hall in a box, and the same box mirrored on the facing leaf;
  - the seven names around the page like a clock;
  - "which way" radiating in eight directions.
- **Colour.** Three Minotaur passages are struck through, as his are, and show red. "House" is blue on the pages that name it.
- **Limits.** Every leaf stays within a journal page's limits: 420 characters of flowing text, eight placed runs of 96 characters each.

## The screen

`ZampanoScreen` shows the folder lying open with its two leaves side by side. Under each leaf:

- **Lift out** holds the leaf up alone. Then:
  - **Turn** turns it a quarter at a time, so text set upside down or sideways can be read.
  - **Turn over** shows it from the back: mirrored, the paper shaded and the ink fainter, as writing is through paper. That is how the mirrored box reads.
  - **Into the folder** puts it back.
- **Take it** pulls the reader's own copy of the leaf.

## Pulling a leaf

- **Once per reader.** A reader pulls each leaf once (`ZampanoDrawers.pull`).
  - The copy goes into their pack, or drops at their feet if the pack is full.
  - From then on their folder shows a paler patch where the leaf lay.
  - Everyone else's folder still holds it.
- **Saved.** Pulls are kept in the reader's own courtyard record (`ZampanoLeaves0178`, a mask of 18 leaves) and survive a save and reload.
- **The item.** `zampano_leaf` (`ZampanoLeafItem`, "Loose leaf: <title>") carries only its leaf number. Using it lifts it to read, turn and turn over.
- **What a leaf is not.** Leaves are not a story. They confer no Witness credit, and nothing depends on holding one.
- **The archive's finite originals are untouched.** The seven readers' files (21 drawers), the chest's book and the survey on its lectern are not touched by opening drawers or pulling leaves.

## New and saved courtyards

- **New courtyards.** Cabinets stand as the courtyard is built (`NovelRooms.courtyard`), and the courtyard is marked done (`zampano_cabinets0178`).
- **Saved courtyards** get them once, from the archive's existing tick, after the readers' files repair. This happens only when:
  - the cells' chunks and entity sections are loaded;
  - no camera is within 24 blocks;
  - nothing living is in the way;
  - the three cells and the floor in front of them are bare, with ground beneath.

  A reader's own building there is kept, and the courtyard is marked done either way.
- **Scene dressing** keeps two blocks clear around the cabinets, as for any House prop.

## Renderer changes

`JournalPageRenderer` now:

- draws every shape in a given hand and ink (`Style`), so Zampanò's pages use his font and the journal keeps the default;
- strikes runs through;
- counts nested culling, so a mirrored run inside a page turned over draws correctly;
- has `leaf(...)`, which draws one of his leaves: paper aged by the leaf's own wear, his title faint at the head, his page number.

## Network (protocol 100)

- `ZampanoFolderPayload` (S2C: folder or -1, held leaf, the reader's pulled mask).
- `ZampanoDrawerActionPayload` (C2S: pull a leaf, or -1 to let go).

Payloads go through `HousePackets.send`, which skips connections without the channel; the journal's open payload now does too.

## Tests and proofs

- **`NovelTests.zampanosDrawersOpenOntoTheirFoldersAndShutWhenLetGo`:**
  - three shut cabinets in a new courtyard, recorded;
  - only the fronts open;
  - each drawer opens its folder;
  - a second reader's drawer shows, then the first again when they let go;
  - walking off and closing shut it;
  - a saved courtyard waits for readers to leave, then gets its cabinets once;
  - an edited floor is left as it is.
- **`NovelTests.eachReaderPullsTheirOwnLeavesAndTheArchiveKeepsItsOriginals`:**
  - nothing from a shut drawer, nor from another folder;
  - one pull per leaf, with a copy titled by its leaf;
  - another reader's pulls are their own;
  - a closed folder gives nothing;
  - pulls survive a reload;
  - every finite original in the archive is unchanged;
  - no Witness credit;
  - all 18 leaves fit a journal page;
  - between them the leaves use every shape, mirroring and striking, and name the house and the Minotaur.
- **The build job's native client proof**, after the journal's three views, renders `native-courtyard-0..8.png` (the nine folders, the second with leaf 4 taken) and `native-courtyard-9..11.png` (a leaf turned over, one upside down, one on its side). It checks:
  - Zampanò's ink on every view;
  - the struck Minotaur red on the third folder;
  - the house blue on the eighth folder.
- **CI also checks** the five cabinet textures, the 48-state blockstate and its models, the leaf icon, the new classes and `draw_zampano_cabinet_art.py --check`.

No layout or Witness change: layout 51; Witness 55 / 42 / two kinds / three endings.
