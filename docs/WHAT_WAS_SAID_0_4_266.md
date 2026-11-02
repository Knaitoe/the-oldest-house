# What was said: conversations kept in the journal (0.4.266)

The second of the owner's quality-of-life notes of 2 November (docs/OWNER_QOL_NOTES_2026_11_02.md):

> Persistent conversation history. The new conversation panel is a useful foundation, but its history is temporary, capped at 48 lines,
> and discarded on logout. Let players revisit conversations they actually heard, especially their own promises and answers. Keep this
> separate from their handwritten journal.

And then, while it was being built:

> i'm okay with the conversation history being integrated into the player's journal

## What is kept

`labyrinth/TalkHistory` keeps every exchange the conversation panel showed a reader:
- who it was with;
- the day, counted as the journal's pages count it;
- where, by the House's name for the place, when it has one;
- every line, with the reader's own replies among them.

It is saved with the House's other records, so it survives the panel closing, logging out and the server stopping.

The limits:
- **Lines.** The panel still shows only its last 48 lines. The record keeps up to 120 lines of each exchange, the latest if it runs
  longer.
- **Exchanges.** It keeps the last 48 exchanges, letting the oldest go first.
- **What counts.** Only what reached the panel is kept: a line a scene set down and never showed is not.
- **Watchers.** Someone only watching keeps nothing.

Each exchange is kept under its own number, updated each time the panel is brought up to date, so a long conversation is one entry.

## In the journal

At the foot of the open journal, between its arrows, a ribbon reads **What was said**. It turns to a section behind the reader's own
pages:
- every kept conversation, newest first;
- each under a faint heading of who, the day and where;
- each line set down as said, the reader's own replies set in, in their own blue-black ink.

It is printed small in plain type, not in the reader's hand. It takes none of their pages, and it cannot be written on or torn. The
arrows and the wheel turn its pages. **My pages** turns back, to the spread that was open, with anything being written still there
(it was sent before the ribbon turned).

The history is sent only when the ribbon is first turned to, and only to a reader who carries their journal, so opening the journal
stays as quick as it was.

## Network

Two payloads, so the protocol is 122:
- `TalkHistoryRequestPayload`, from the journal;
- `TalkHistoryPayload`, the reader's own kept conversations.

## Tests

- `JournalTests.whatWasSaidIsKeptForTheReadersJournalWithTheirOwnReplies` (batch talk_history) checks:
  - an exchange is kept with who and the day, line for line, with the reader's answer;
  - it is still there after the panel closes and the reader goes;
  - a hundred lines are kept where the panel shows 48;
  - the same exchange keeps its latest 120;
  - a line never shown is not kept;
  - sixty exchanges leave the latest 48;
  - a spectator keeps nothing.
- Truant's and A Place for You's conversation cases pass with the history recording.
- The journal's native proof gains a last view, `native-journal-heard.png`: the section open on two kept conversations, which must
  show printed ink and the reader's replies in their own ink (the build job checks the file).
