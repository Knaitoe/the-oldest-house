# The reader's journal, step one — 0.4.177

Step 1 of [JOURNAL_DESIGN.md](JOURNAL_DESIGN.md): the journal itself and its page renderer, which Zampanò's courtyard will share.

> "I think the journal might be worth developing as a custom sub system. I especially like the idea of a damaged book returning on death. The custom sub system fixes worrying about signing the book too."

## Where it comes from

- **On the doorstep.** When Will's letter is left on the doorstep, a second parcel lies beside it, addressed in the reader's own hand: a blank journal with their name inside the cover.
- **Late joiners.** A reader who missed the letter (a late joiner, or a world from before this version) is given theirs the first time they come in through the House's front door: "A parcel addressed in your own hand was waiting for you: a blank journal. You don't remember sending it."
- **Where it goes.** It goes into the pack, not the hotbar, so it never lands in a hand kept empty for something else.

## The item and the saved data

- **One journal per reader.** Only its owner opens it. To anyone else it says: "It isn't yours. A name is written inside the cover: <name>."
- **The words live in the world's saved data** (`Journals`, `the_oldest_house_journals`). The stack holds only its owner, the name, its issue number and its wear.
  - A copied stack opens the same pages.
  - A lost stack loses nothing.
  - Nothing is ever signed.
- **A lost journal comes back.** If the journal is lost in the world, it comes back to its owner one wear worse, on their next arrival in the House (or within five seconds if they are already there). Lost means any of:
  - left to despawn;
  - burnt, crushed or blown up;
  - fallen out of the world;
  - killed by command.
- **The old copy goes blank:** "The pages are blank. This isn't your journal any more."
- **What does not count as lost.** Picking it up, dropping it, or putting it in a chest.
- **Never the Mother's.** It is never taken by the Mother: a despawning journal is caught before her, and she does not count it among things worth keeping.
- **The tooltip** names the owner and the wear: water rings, dog-eared, swollen pages, a torn corner.
- **The icon** has the same five stages (`tools/draw_journal_art.py`).

## The screen

- **The spread.** Two pages at a time on paper, with rules and a margin line. The first page is the inside of the cover, with the owner's name. Pages 1 to 96 can be written in.
- **Writing.**
  - The reader clicks a page and writes straight onto it, in graphite.
  - Vanilla's own multi-line text engine handles wrapping, the caret, selection and the clipboard.
  - A page holds 15 lines, 420 characters and no formatting codes.
  - A pencil scratches now and then while writing. Pages turn with a page sound.
- **Lift line.** The wrapped line at the caret comes out of the writing and lies a little askew where it was, selected. It can then be:
  - dragged anywhere on its page;
  - turned with the scroll wheel, 15° a notch;
  - nudged with the arrow keys;
  - made smaller or larger;
  - mirrored;
  - changed in shape: a line, a spiral, a cascade, a column or a box;
  - put back into the page (backspace or delete does the same);
  - left where it is (escape).
- **Each page holds up to eight placed lines.** They stay on their own page however they are turned or dragged.
- **Literature colours.** "House" is blue, as in every book. "Minotaur" is red, because the page raises the literature count (0.4.175). In a spiral, both colours hold letter by letter.
- **At each page's foot:** where it was begun (the place's prose name in the House) and the day, faintly, then the page number.
- **Wear shows on the paper:**
  1. water rings;
  2. dog-eared corners;
  3. swollen ripples;
  4. a tide line and a torn corner.

  Text stays legible at every stage.
- **No instructions on screen,** only small buttons under the book: the page turns, "Lift line", and, for a selected line, its shape, mirror, smaller, larger and back into the page.

## Network (protocol 99)

- **`JournalOpenPayload` (S2C):** the whole journal, sent only to its owner when they open it.
- **`JournalEditPayload` (C2S):** one page as the reader left it. It is sent on each page turn and on closing, only for pages that changed, and capped at 32 KB.

The server keeps an edit only if:
- the reader carries their own current journal;
- the page is 1 to 96;
- the writing fits what the screen allows (length, lines, no formatting codes, at most eight placed lines in known shapes, positions on the page);
- their allowance holds (eight pages at once, one more every five ticks).

The words then pass the server's text filter before they are kept. The server stamps where and when a page was begun the first time anything is written on it; it never takes that from the client.

## Tests

- **`JournalTests.theJournalIsItsOwnersAndAnOldCopyGoesBlank`:**
  - one journal, once, by doorstep or first arrival;
  - only its owner opens it;
  - the name is inside the cover;
  - a late joiner is given theirs;
  - a journal that is not lost does not come back, and a lost one does, once, a stage worse;
  - the old copy goes blank.
- **`JournalTests.editsAreKeptOnlyFromTheOwnerAndWithinWhatThePageHolds`:**
  - nothing is kept from a reader not carrying their journal, or from a borrower;
  - only pages 1 to 96;
  - no overlong writing, too many lines, formatting codes, too many placed lines or unknown shapes;
  - the allowance of eight;
  - the kept page after the text filter, with placed lines clamped to the page and keeping their shapes;
  - the begun-at stamp;
  - a save and reload;
  - the screen sent only written pages.
- **`JournalTests.aJournalLostInTheWorldComesBackWorn`:**
  - picked up, it is not lost;
  - despawning, it is lost and the Mother never shelves it, and it comes back water-stained;
  - burnt, it comes back dog-eared;
  - fallen out of the world, it comes back swollen.
- **The native client proof (`JournalVisualProof`, after the embers)** renders three views:
  - the cover and a written page with a turned line;
  - a dog-eared spread with every shape and a selection;
  - the same spread torn and tide-marked.

  The second must show the blue House word and the red Minotaur in its pixels. The build job requires `journal-passed.txt` and `native-journal-0..2.png`.

## Not yet

- Rubbings, the co-author, torn pages, grounding and the post-mortem journal are the next steps, after Zampanò's courtyard (step 2).
- On a death the journal still drops like anything else (step 6 changes that). If it is left to despawn, it comes back worn.

## Versions

- **Mod version:** 0.4.177.
- **Protocol:** 99.
- **Layout:** 51.
- **Witness:** unchanged at 55 sources, 42 required, two kinds, three endings. The journal counts toward nothing.
