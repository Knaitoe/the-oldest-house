# The red Minotaur — 0.4.175

The owner:

> "Minotaur should be red, but only in literature - not server wide, like house"

## What changes

- **In books and on lecterns, "Minotaur" is red.** That covers every written book, every lectern page, a writable book being written in, and the House of Leaves' ember excerpts.
  - On paper it is a dark ink red (`HouseText.INK_RED`, `#A3201C`).
  - On a dark page it is a lighter red (`SCREEN_RED`, `#FF6B5E`).
- **Matching works like the blue "house":**
  - the whole word only, in any case;
  - "Minotaur's" counts;
  - "Minotaurs" and "Minotaur2" do not.
- **Everywhere else, nothing changes.** Chat, tooltips, item names, signs, subtitles and the HUD keep "Minotaur" in its ordinary colour.
- **"House" is unchanged.** It stays blue everywhere, as before.

## How

The blue "house" is a display pass in the font: it colours the word wherever text is drawn. The red Minotaur uses the same pass, but only while literature is being drawn:

- `HouseText` keeps a literature count. `BookLiteratureMixin` raises it while `BookViewScreen` and `BookEditScreen` draw. `LecternScreen` is a `BookViewScreen`, so it is covered too.
- The font pass colours "minotaur" red only while that count is up. The HUD and chat draw before the screen, outside it.
- `HouseText.literature(...)` gives the red to literature drawn outside a book screen. Today that is the embers of a burned leaf.
- Nothing is saved or sent. A page's words and styles are untouched, so saved books, signed books and the server are unchanged.
- The journal and Zampanò's courtyard (docs/JOURNAL_DESIGN.md) will raise the same count while they draw their pages.

## Tests

`WritingTests.theMinotaurIsRedOnlyInLiterature` checks five things:
- outside literature only "house" is coloured;
- inside, "Minotaur" is red and "house" blue, with the word boundaries above and the sink's indices kept;
- the lighter red appears against a dark page;
- leaving the page ends literature;
- the embers carry the red themselves.

The writing cases pass locally, and the native client proof loads the new mixin.

## Run 1340

Run 1340 tested 7d5eede (0.4.174). Fourteen of seventeen jobs passed. Three failed:

- **The literary job: the elk yacht journey.**
  - **What happened.** The case looked at three of the dead on fixed 10-tick steps and asserted all three seen at tick 30. The reader's own killer loaded late on the busy runner. The hunt runs only once he is there, so it had ticked only once.
  - **The fix.** The case now waits for the killer and for each sighting in turn, and walks the rest of the journey as waited steps. Every assertion is kept.
- **The build job: the tunnel passage case.**
  - **What happened.** The case read the tunnel woman's body straight after the source, before the scene had placed her. The missing body was a null in the server tick, so it stopped the whole structure suite. The client proof after it then found no page corpus.
  - **The fix.** The case now waits for her body before moving it.
- **Live multiplayer: the Ghosts leap on the condensed house.** This was the part of 0.4.174 that could not be run locally.
  - **What happened.** Both socket readers followed Marjorie out of her room through her one door. In the smaller house the landing is in view the moment a reader is through that door, so A's own leap froze A just outside it, in B's way. When A had gone, B was still pressed against the open door's panel, 0.02 of a block too far east, and never came free.
  - **What was kept.** A walked, filmed and held the whole leap.
  - **The fix.** B now waits on the far side of the landing, out of the doorway and of Marjorie's path, and A walks through the middle of the door's clear half. The take, the leap and every check are unchanged.

## Versions

- **Mod version:** 0.4.175.
- **Protocol:** 98 (unchanged; nothing is sent).
- **Layout:** 51.
- **Witness:** 55 sources, 42 required, two kinds, three endings.
