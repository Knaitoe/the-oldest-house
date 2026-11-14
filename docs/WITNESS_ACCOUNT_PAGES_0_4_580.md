# The account's pages, the owner's playtest note (0.4.580)

One of the owner's notes from playing the stories built after Borrasca, on the book a reader carries of the rooms they have
heard to the end (the Witness account). New builds only. No protocol, layout or Witness change, and no line changed.

The note, as it was recorded when the owner gave it:

- The witness book cuts accounts off: each story is forced onto one page.

## What was there

- **One page to a room.** Every room a reader had heard to the end had one page in the account: the room's title, an empty
  line, and what the reader says of it.
- **A page holds fourteen lines.** A page of a book in the game is 114 pixels wide and fourteen lines high. The game wraps
  the text to that width, draws fourteen lines and draws nothing after them. There is no mark where it stops, so an account
  longer than its page read as an account that ended in the middle of a sentence.
- **Two rooms ran past their page for every reader.** Measured with the game's own letter widths:
  - *The last swimmer* is seventeen lines. The page stopped at "Then something walked across the cover toward me from" and
    lost "underneath, and the attendant held the fire door, and I went."
  - *The small passage* is sixteen lines, with the sentence every reader who came back through the squeeze has after it.
    The page stopped at "Under water I can hold my" and lost "breath longer than I used to."
- **The rest fit only while nothing was added.** Of the eighty-eight rooms, six fill all fourteen lines of their page,
  thirteen have one line to spare, and twenty-six have two or three. And things are added after a room's own account, from
  the reader's own record:
  - what happened afterwards (a trade and what followed it, what the manor kept);
  - who went ahead and who was still in it, where the room was shared;
  - for some rooms a different account for a different ending, and for a reader who watched, joined partway or came after,
    the account written for that.

  Each of those is a sentence or more, and a sentence is two or three lines. The Pell house's account is thirteen lines,
  and a reader who was there with someone has a sentence after it that says who. With that sentence it is fifteen, and the
  page lost the end of it ("Close with me.").
- **The check did not look.** One check draws every book page with the game's fonts and fails if a page needs more than
  fourteen lines. For the account it drew the page that counts the rooms, the page about other endings and the unfinished
  entries. It left the rooms' own pages out.

## What changed

**A room's account runs over as many pages as it needs.** Its title stands over the first of them, and the pages after carry
on without a heading, as the next page of a book does.

- **A page ends where a sentence ends.** A page is filled with whole sentences; the first one that would not fit begins the
  next page. No sentence is broken over two pages unless it is longer than a page by itself, and no room has one that long.
  A sentence that long would be broken between two words.
- **Speech stays with who said it.** A question mark inside quotation marks does not end the sentence when what follows is
  in lower case, and neither does a row of full stops: no page ends between "Is that all?" and "she said."
- **No stranded last line.** A last page of a line or two takes the sentence before it from the page before, and the one
  before that, until it is more than two lines or the page before is down to one sentence.
- **The lines a writer put in are kept.** A line's end, and an empty line between two paragraphs, stay where both sides of
  them are on one page.
- **The measure is the game's.** The pages are measured with the widths the correspondence already carries for each hand.
  For the hand the account is written in they are the game's own: all 103 of its letters agree with the font. A letter the
  hand does not have is counted at least as wide as the game draws it, so a page is never longer than it is counted.

With every room ended and nothing else on a reader's record, eighty-six rooms are still one page and two are two: *The last
swimmer* breaks after "because the card said to." and *The small passage* carries its last sentence over. A reader with
trades, company and aftermaths on their record has more.

**In the code.** A new class, `BookPages` (`house`): `lines(style, text)` counts the lines the game wraps a page's text to,
`fits` says whether a page is shown whole, `sentences` cuts a paragraph into its sentences, and `split(style, head, text)`
returns the pages. A sentence ends at a full stop, a question mark or an exclamation mark, with any closing quotation mark
or bracket after it, and then a space; a full stop after Mr, Mrs, Ms, Dr, St, No or a single capital letter ends none, and
neither does anything that is followed in lower case. Each hand's letter widths are looked up once and kept, because a
reader's account is paged again whenever their record changes.
`WitnessAccount.book` passes each room's title and account to `split`. `CorrespondenceTexts.advance` (a letter's width in a
hand) is open to the package for it. Nothing else in the account changed: the page that counts the rooms, the trial marks,
the unfinished entries and the play are as they were.

## Checks

- **A new case,** `WitnessTests.everyRoomOfTheAccountIsOnPagesTheGameShowsWhole`: the last swimmer's account is longer than
  a page on one page; with every room ended, every page of the account fits, each room's title stands over exactly one page,
  and the last swimmer reads whole across its pages, word for word. Then the splitter by itself, on texts of one to sixty
  sentences: the head is on the first page only; every page fits and begins and ends with a sentence; put back together the
  pages are the text, with nothing lost and nothing twice; and no last page is a stranded line or two. A sentence of 260
  words is broken between words and none is lost. "Mr." and an initial end no sentence, a closing quotation mark stays
  with its sentence, and speech stays with who said it. A line's end and an empty line are kept. A head with nothing under
  it is one page, and nothing is none.
- **The game's own fonts.** The whole account, with every room ended, is now among the pages the font check draws. Run
  here, it drew 4,016 book pages and every one fit; all thirty-five of the client's checks passed.
- **The nineteen cases that read the account** (that one, three more of the Witness suite's, and fifteen in the co-op,
  lake house, Pell house, exploration, keepsake, staircase, House-read and literary suites), and the writing namespace's
  six.
- The quick checks, and the declared case count (1,567).

## Caught while building

- **The last page was first topped up once.** A last page of one short sentence took one more sentence from the page before
  and was still two lines. The new case found it at eight sentences ("The door was shut. Nobody answered!" alone on a page).
  It is topped up until it is more than two lines.
- **Speech could be parted from its speaker.** The first rule ended a sentence at any question mark, so a page could end on
  a quoted question and the next begin "she said." No room's account has such a break now, but what is added after an
  account can be anything, so the rule was made right.
- **The widths were looked up a letter at a time** in the table the correspondence reads, for every sentence tried on every
  page. An account is paged again each time its reader arrives somewhere or ends a room, so the widths are kept in a small
  table for each hand.

## For the owner

Nothing here is new story, and no line was written or changed. Four things are choices of mine:

- **A page ends at a sentence's end,** so some pages are a few lines short of full. Filling every page to its last line
  would break sentences across pages.
- **The pages after the first have no heading** and no mark that they carry on. They read as the next page of a book. If a
  room's title should stand over each of its pages, or a small mark at the foot, it is a line in one place.
- **Two rooms are two pages for everyone now** (the last swimmer, the small passage), and the fullest rooms become two as
  soon as anything is added after them. The other way is to shorten those accounts to a page, which is a change to lines
  and so is yours.
- **Only the account's rooms are paged this way.** Each story's own account, the letters and the notes have their own
  paging and were already drawn by the font check at their longest. If any other book reads as cut off, say which.

## Still to come

The rest of the notes from the same playtest: the last swimmer's pool, and the yard's stock stairs, wall light, long table
and floating lamp.
