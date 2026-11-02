# Journal autosave (0.4.265)

The first of the owner's quality-of-life notes of 2 November (docs/OWNER_QOL_NOTES_2026_11_02.md):

> Autosave journal writing. Currently, edits are sent when turning a page or closing the journal. Save periodically while typing to
> reduce lost writing after a disconnect or crash.

## What changes

The journal still sends its pages when one is turned and when it is closed. Besides that, writing not yet sent now goes back to the
server:
- a second after the hand stops (20 ticks without a keystroke on either open page);
- at least every five seconds while typing goes on (100 ticks).

A dropped connection or a crash now loses a few words at most. A page turn's own send starts the wait over.

`journal/WritingClock` decides when; `JournalScreen.tick` asks it each tick with what each open page holds and what was last sent.

## Nothing else changes

- **The rate limit.** The server allows eight edits at once, refilled one every five ticks (`Journals.TOKENS`, `REFILL_TICKS`). The
  clock sends at most two pages a second, so it stays inside that.
- **The open screen.** A kept edit sends nothing back to it, so autosaving never moves the cursor or rebuilds the page.
- **Quotes and grounding** see the writing in smaller steps, with the same rules:
  - a line the reader stops halfway through is quoted only while it stays on the page;
  - a new line of twenty-four letters settles the static when it is first sent, as before.

## Tests

`JournalTests.writingIsSentASecondAfterTheHandStopsAndEveryFiveSecondsWhileItGoesOn` (batch journal_autosave) drives the clock:
- nothing unsent means nothing sent;
- steady typing is sent at five seconds, not before;
- a pause sends once, a second later;
- the other page counts too;
- a page turn's send starts the wait over.

It passes locally with the journal edit case.
