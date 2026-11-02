# Finding your place in the journal (0.4.268)

The fourth of the owner's quality-of-life notes of 2 November (docs/OWNER_QOL_NOTES_2026_11_02.md):

> Journal bookmarks and search. It already remembers the open spread, but finding an older entry means paging through the notebook. A
> ribbon bookmark, page-number jump, and search through the player's own writing would become increasingly valuable across a long
> playthrough.

Code: `client/JournalScreen` (the second row), `journal/Journals` (the ribbon kept with the book) and
`network/JournalRibbonPayload`.

## The second row

Under the journal's page arrows and the "What was said" ribbon is a second row:
- **Ribbon here** lays the ribbon in the open spread. On a spread that already has it, the button reads **Ribbon out**.
- **To ribbon** turns straight to the ribbon's spread, wherever it is.
- **Find in your writing** is a field. Type a word and press Enter: the journal turns to the next page after the open spread whose
  writing holds it, wrapping round from the last page to the first, and the words are picked out on the page. Enter again finds the
  next. When nothing matches, the field's text turns a dull red until it is changed.
- **Page** is a field for a page number. Type it and press Enter to turn to that page's spread.

What the search covers:
- **Searched.** Only the reader's own writing, the words they typed onto the page.
- **Not searched.** The House's turned and boxed lines, the other hand's ink, captions and rubbings. Torn-out pages are skipped.

Escape takes the keys back from a field without closing the journal. Clicking a page goes back to writing it.

## The ribbon

The ribbon is a strip of dark cloth:
- **On the open spread.** It lies down the page it marks and out past the foot of the page.
- **Elsewhere in the book.** Only its end shows at the spine, below the pages.

The server keeps the ribbon with the reader's journal (`Ribbon`), so it is still there on the next opening and after the server stops.
A new copy of a lost journal keeps it too, as it keeps every page. Only a reader carrying their own journal can lay it or take it out.

## Room for it

The pages now sit a little higher, so both rows fit under them on the smallest screen. "What was said" uses the same first row.

## Tests

- `JournalTests.theRibbonStaysInItsPageForTheReaderCarryingTheirJournal` (batch journal_ribbon) checks that the ribbon:
  - is not laid by a reader who does not carry their journal;
  - lies in the page it was laid in and goes to the screen with the pages;
  - is held within the book;
  - survives a save;
  - comes out again;
  - is never laid in someone else's journal.
- The native client proof adds `native-journal-find.png`. It opens the sample journal with the ribbon in page 5, the second row
  showing and a search for "bell". It requires the spread to have turned to page 4 and the ribbon's cloth to show. The build job
  requires the image.
