# Torn pages: 0.4.199

This is step 5 of [JOURNAL_DESIGN.md](JOURNAL_DESIGN.md): torn pages and pinning. It is the owner's idea 5, the co-op and wall half. The owner asked: "Can you build those functionalities too?"

## Tearing a page out

- **How.** In the open journal, the reader holds the right mouse button on a written page. The page's outer corner lifts further the longer it is held, and after about a second the page comes away. Nothing on the screen explains this.
- **Which pages.** Only a page with writing on it. A blank page does not tear.
- **The stub.** The journal keeps a strip of paper along the gutter, ragged where the page came away. Nothing can be written on a stub, and it does not tear twice. Rubbings and the other hand's ink never land on it.
- **The page.** It becomes an item, "Torn from <name>'s journal", carrying everything on the page:
  - the writing;
  - the House's placed lines;
  - rubbings;
  - the other hand's ink;
  - where and when it was begun.
- **The filter.** The words pass the server's text filter as they are torn, as they did when they were written.
- **Server side.** `TornPages.tear` and `JournalTearPayload` handle tearing. The screen is redrawn from the server's copy of the book.

## Reading, pinning and leaving it

- **Reading.** Anyone holding the page can read it with a right-click. It opens on the journal's own page renderer, with no wear, no page number and one torn edge, and whose journal it came from written above (`TornPageScreen`, `TornPagePayload`).
- **Pinning.** The page pins to a sturdy wall face, or lies flat on a sturdy table top, turned to face the reader who left it (`PinnedPage`).
  - At a glance it shows its first few lines in tiny graphite, under a brass pin.
  - A right-click opens it in full.
  - Two pages are never pinned over each other.
- **Dropping.** Dropped anywhere in the House, a page stays where it fell and never despawns.
- **Falling.** If the wall or table under a pinned page goes, the page falls and lies where it fell.
- **Taking it down.** Nobody but its writer can take a pinned page down.

## Limits

- **The manor.** No limit. A reader can build their own Zampanò wall of pages.
- **The rest of the House.** At most six pinned or dropped pages per reader at a time. A seventh sends the oldest back into its stub, and the writer is told that one of their pages has found its way back. `TornPages.counts` decides where a page counts; it uses the manor's domestic volume.

## Taking a page back

The writer can take back their own page at any time, and it goes back into its stub with every word:
- with a right-click on the stub in their journal, wherever the page is;
- by crouching and right-clicking the pinned page, or hitting it;
- by crouching and using the page in their hand.

Anyone else's copy then goes blank. A blank torn page does not stay in a pocket, and it never comes into the world.

## Nothing is lost

The torn page is only a handle, like the journal: its words stay in the journal's saved data, keyed by the stub and a serial number. So:
- a copied stack is a blank one;
- a page burnt, crushed or expired out in the world goes back into its stub;
- `/ghosthouse journal clearpages <player>` sends every page of a reader back into its stubs, wherever they are. This is the moderation command.

## What it is not

It counts toward nothing: no Witness, no advancement, no reward. Witness stays 55 / 42 / two kinds / three endings.

## Build

- **Code.** New: `journal/TornPages`, `TornPageItem`, `PinnedPage`, `client/TornPageScreen`, `PinnedPageRenderer`, `command/JournalCommands`. The book saves its torn pages and their order.
- **Protocol 105.** New payloads: `JournalTearPayload` (client to server: tear, or take back) and `TornPagePayload` (server to client: open a torn page).
- **Art.** `tools/draw_journal_art.py` adds the torn page's item icon (`textures/item/torn_page.png`) and the pinned page's paper (`textures/entity/pinned_page.png`). No existing PNG changes.

## Tests and proofs

- **`JournalTests.aWrittenPageTearsOutLeavingItsStubAndItsWriterCanTakeItBack`:**
  - only the carrier of their own journal tears from it, and never a blank page;
  - the page carries its words unaltered through the filter;
  - the stub refuses writing and a second tear;
  - another reader reads the page;
  - the writer takes it back from the stub;
  - the other copy goes blank and leaves the pocket;
  - a page torn again is a new copy.
- **`JournalTests.tornPagesPinAndOnlySixAreLeftOutInTheHouseBeyondTheManor`:**
  - six pages pinned on a wall in a hall, their glance and placement checked;
  - no page pinned over another;
  - a seventh sends the oldest home and off the wall;
  - a page lies on a table and sends the next oldest home;
  - a page in the manor counts toward nothing;
  - another reader cannot take a page down, and the writer takes one back;
  - a page whose wall goes falls and stays where it fell;
  - the command sends all six remaining pages home, and every page's words come back.
- **The client journal proof** adds two views: a spread with its left page torn out (`native-journal-5.png`), and a torn page held up to read (`native-journal-6.png`). CI requires both.
