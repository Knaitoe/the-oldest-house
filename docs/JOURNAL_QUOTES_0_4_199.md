# The House quoting the reader: 0.4.199

This is step 8 of [JOURNAL_DESIGN.md](JOURNAL_DESIGN.md), the last one. The owner: "yes, the house should quote the player in some carefully chosen instances." The owner then asked: "Can you build those functionalities too?"

The House quotes a reader's own words rarely, exactly, and only to that reader. There are three moments.

## 1. In the reader's own House of Leaves

- **A chapter that names a place.** When a chapter tells of a place where the reader wrote in their journal, it quotes one line written there. The quote is a page of its own, straight after that leaf, in the account's hand:

  > In a straight hall you wrote:
  >
  > "it is longer than it was"

- **When no chapter names one.** If no chapter names a place where the reader wrote, the House's own chapter (IV, the door) quotes a line from the first place they wrote in. The House keeps to that place from then on.
- **Limits.** At most one quote a chapter.
- **Pages are not rewritten.** The quote has its own page, so no leaf is rewritten and no saved account changes.
- **The quote is chosen afresh.** Each time the book is brought up to date, the House chooses the quote again from the journal as it is now. A line the reader has since deleted or torn out drops out of the book.
- **Bound and burned leaves.** A quote shows only with its leaf: once that leaf has burned, its quote has gone with it.
- **Code.** `StaircaseStory.quotes` chooses the quotes, and `StaircaseStory.content` lays them in.

## 2. The other hand, once

- **One copied line.** Once ever, on the blank page after the reader's last page begun in the House, the other hand copies out one of their own lines. It copies the line word for word, in its own ink, and adds nothing.
- **When.** This is one more form among the other hand's choices. It follows the same rules as the rest:
  - one time in six;
  - never twice in a House day;
  - never on an open page.
- **Code.** A new mark, `QUOTED`, drawn like the other hand's written notes. `CoAuthor.copyOut` writes it for the tests.

## 3. At an ending

- **What the House writes.** When a reader reaches any of the three endings (escaped, witnessed, or locked out), the House writes its last page in their journal. The page holds only the first line they ever wrote in the House, in the other hand's ink.
  - It goes on the first blank page after their writing.
  - It is written once for each reader.
- **Why the journal.** Every ending book is a written book that can be handed to anyone. The journal is the one book nobody else opens, so the House's last page goes there.
- **Code.** `Quotes.ending` is called from `FinaleController` (escape, the cell's door, a collapse releasing the kept) and from `WitnessEnding`.

## What may be quoted

`journal/Quotes` keeps a record of the lines a reader writes. Each line keeps its page, the place it was written in and its order.

- **A line** is a written line of the page. For a written line too long to copy whole, each of its sentences counts as a line. It must be at least three words and at most 96 characters long.
- **Only:**
  - the reader's own words, exactly as written;
  - from pages begun in the House;
  - lines the server's text filter left untouched.
- **Never:**
  - a line that names another player: anyone on the server now, or any other reader with a journal;
  - a line on a page that has been torn out, even once the page is back in its stub;
  - a line the reader has since deleted or changed. A changed line is a new line from then on.
- **A server whose House read is off** (`read.readReaders`) quotes nobody, anywhere. The read itself still never looks at the journal.

## Who sees it

Only the reader:

- **A torn page** never carries the House's copy of the reader's line, whether it is read, pinned or glanced at. Back in its stub, the copy is there again.
- **The House of Leaves item never holds its quotes.** They are shown only when its own reader opens it: set on the book, sent to that reader's client alone, opened, and taken off again before anything else can be sent. So nothing anyone else's client is sent carries them, wherever the book goes:
  - another reader's inventory;
  - the ground;
  - a lectern;
  - a chest, or any other container.

  A review found the first version kept the quotes on the item and took them off only on the ground, on a lectern or in another inventory. A chest was missed, and another player's client could be sent the quoted lines before the cleanup. Books from before this change lose their stored quotes the next time their reader's book is brought up to date, or when anyone else holds or reads them.
- **The ending's page** is in the reader's journal, which only its owner opens.

## What it is not

It counts toward nothing. Witness stays 55 / 42 / two kinds / three endings.

## Build

- **Code.**
  - New: `journal/Quotes`.
  - The journal's saved data keeps each reader's quotable lines and their order, whether the other hand has copied one out, and the page the ending went on.
  - `JournalPage.Mark.QUOTED` and `JournalPage.unquoted()`.
  - Also changed: `StaircaseStory` (the quotes, shown to the reader when they open their book and never kept on the item; older stacks lose any they kept), `StaircaseProse.named`, `CoAuthor`, `TornPages`, `FinaleController` and `WitnessEnding`.
- **Protocol 107.** The new mark is something the journal screen must know.
- **No art or sound changes.**

## Tests

`JournalTests.theHouseQuotesOnlyTheReadersOwnLinesAndOnlyToThem` checks:
- only lines of three words or more are kept;
- a line the filter changed is never kept;
- a line naming another reader is never quoted;
- each line keeps the place it was written;
- lines come oldest first;
- a torn line is never quoted, even taken back;
- a deleted line is never quoted;
- all of it survives a reload.

It then checks the three moments:
- **The House of Leaves:**
  - the House's chapter quotes the first place the reader wrote in;
  - a chapter that names the nursery quotes the line written there instead;
  - the item itself never keeps a quoted line; only its reader's screen shows one (`StaircaseStory.shown`);
  - opened by its reader, the book is opened from the quoted pages, and the stack is left without them;
  - a copy set in a chest carries none;
  - in another reader's hands, or on the ground, the book quotes nothing.
- **The other hand:**
  - one line copied word for word onto the next blank page, once only;
  - a torn page shows none of it, and it is there again in the stub.
- **The ending:**
  - the first line, on the page after the reader's writing, once;
  - nothing for a reader who wrote nothing.

Last, it checks the read option: with the House's read turned off nobody is quoted and the House of Leaves holds no quote. Turned back on, it quotes again.
