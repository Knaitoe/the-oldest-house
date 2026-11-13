# The Pell house 15: the journal's false entries (0.4.537)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its first
visit whole, and its second, a week in, has the afternoon and June's swim, the knife, June's hand and the tapes
(`docs/PELL_HOUSE_TAPES_0_4_536.md`). This is its fifteenth slice, the journal's false entries, from the proposal's "2. A week in":

> **The journal:** the first false entries appear in the older sibling's journal (below).

and from its section "Being framed":

> **The journal, in the reader's own hand.**
> - **When:** after each framed incident, and once after each night in the garage.
> - **What:** a short entry appears in the older sibling's real House journal (`docs/JOURNAL_DESIGN.md`), in their own handwriting and
>   their own ink, as if they wrote it. [...]
> - **How it differs from the co-author.** This is not the other hand [...], which writes in its own lettering. This is the reader's
>   lettering, which is the horror.

with the owner's decision at its head: "yes, carry the gaslighting forward." The freezer comes after. The House still does not deal the
house.

## When an entry is due

| After | Whose journal | The entry |
|---|---|---|
| The knife | the reader June found in the kitchen, when they go on Gail's file for it | "I took the knife out of the block." |
| June's hand | the reader she says slammed the laundry door on it, when they go on Gail's file for it | "I slammed the door on her hand. Nobody saw." |
| A night in the garage, a week in | each reader who lay down on the camp bed that night, when June calls breakfast | "I told Tilly nobody wants her here." |

- **Each entry once, ever,** for each reader. Put on the file for the same thing again, nothing more falls due.
- **The first visit's night gives none.** The proposal's first false entries are a week in, after the first framing.
- **The last visit's night** has its line ready ("I scuffed the line by the drive on purpose, to see what she'd do."), for when the last
  night has a morning; it is not reached yet.

## Where it goes

- **In the reader's own journal, in their own lettering and graphite:** the font and ink their own writing is drawn in, never the other
  hand's.
- **On their last page begun in the House,** on the rows after their own writing, when there is room for it there; otherwise on the
  blank page after their last, which it begins, in the House.
- **Never on their words, never on a page begun outside the House, never on a page they have open.** An entry waits while their journal
  lies open at its page, and while they have no journal yet; it goes in the first moment it can while they are at the house (looked for
  once a second). Two due at once go in one after the other.
- **Nothing tells the reader it happened.** No line, no sound.

## What stays

- **For good, whichever way it ends.** An entry is not the reader's writing, so nothing they do to their writing takes it off the page;
  written on below, the page keeps it after their words.
- **Torn out,** the page carries it. Where a torn false page turns up again (in Gail's folder on the last visit; pinned to a wall in a
  later hall) comes later, as do the entries after a failure.

## More than one reader

- Each reader's entries are their own, from what was put on them: another reader's journal has none of them. Two readers who both slept
  out in the garage each get that night's entry, each in their own journal and their own hand.
- A spectator gets none.

## What each reader keeps

- the entries due and not yet written ("FalseDue", in order) and those written ("FalseWritten");
- which visits' nights they slept in the garage ("GarageNights", one bit to a visit).

## How it is made

- **`journal.OwnHand`** writes a line in a reader's own hand: `write(p, text)` returns the page, or 0 when it cannot go in now or at all.
  Like the other hand it only adds. The line is a page note of the new mark **`JournalPage.Mark.OWN`** (saved as `own`), text only, the
  server's: the reader's edits keep it and a torn page carries it. `OwnHand.lines` lists them for tests.
- **`client.JournalPageRenderer`** draws an `own` note in the reader's font and ink on the rows after their writing (after any earlier
  ones), in the writing's own column.
- **`labyrinth.PellEntries`**: `due` from `PellKnife` (both places the knife puts a reader on the file), `PellHand.mark`, and
  `PellMorning`'s breakfast call (the nights from `PellMorning.lieDown`); `reader` from `PellHouse.reader`, every `TRY` (20) server
  ticks. `PellTexts.falseEntry` holds the lines.
- The playtest log records `journal_own_hand` with the page.

## Decided here (any of it can move)

- The first false entries come a week in, as the proposal has them; the first visit's night in the garage gives none.
- A night in the garage is a night lain down on the camp bed; its entry falls due when June calls breakfast.
- An entry goes on the last page begun in the House when there is room below the reader's writing, otherwise on the next blank page.
- One entry at a time, looked for once a second.
- A week in's night entry is the proposal's third example; the last night's is new.

## Checked

- **One new case,** `whatJuneSaysTheyDidIsInTheirJournalInTheirOwnHand`: two readers a week in, each carrying their own journal, one
  having written a page of it in the House. June says that one slammed the laundry door on her hand: the entry goes in on that page,
  after their words and in their own hand, their words untouched, and the other's journal has none of it; said again, nothing more falls
  due. The other, found in the kitchen after the knife with their journal open at its first spread, waits; shut, it goes in on their
  blank first page, begun in the House. Written on below, the first reader's page keeps it. Nothing tells either of them.
- **Two cases extended:** the first morning (each keeps that they slept out there on the first visit, and no entry is due for that
  night); the morning after the knife (a week in, each has that night's entry due, after the knife's for the one June found, and with no
  journal none is written).
- **Pell 25/25 locally** (the twenty-four earlier cases and this one), the architecture namespace and the quick checks.
- **The drawing is client-side;** it compiles, and CI's client jobs load it.
