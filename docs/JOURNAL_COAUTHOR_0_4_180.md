# The other hand — 0.4.180

Step 4 of [JOURNAL_DESIGN.md](JOURNAL_DESIGN.md): the uninvited co-author. This is the owner's idea 3.

## What happens

Now and then, something writes in the reader's journal while they are not looking. Nothing tells them. They find it the next time they open the book.

- **When it may write** (`journal/CoAuthor.java`):
  - **waking from a night's sleep**: the morning woke them. Leaving the bed early or being shaken awake does not count.
  - **coming round from a blackout**: the caver's choke, the lake house dive, the elk drag and the Last Stop's field.
  - **the end of one of the worst rooms**: the reader's own ending of one of 23 listed stories (`CoAuthor.DREAD`), as Witness records it. The list includes the trailer, the cave, the well, the hunt, Crimson Hall, the carcasses, the camp, the winter lake, the costume night, Proofrock, Borrasca, the child's room, the Hill nursery, hide-and-clap, the six uncanny rooms, the backseat, the silo and the dare.
- **How often.** One time in six, and never more than once in a House day per reader. The day is saved with the journal.
- **Where it writes.**
  - Only on pages begun in the House. A page begun outside is never touched, even with a name on it.
  - Never on a page the reader has open.

## What it writes

It writes in one of the owner's three forms, chosen at random among the forms that have somewhere to go.

1. **A blank page.** On the blank page after the last one the reader began in the House: "Nothing to say? I was right behind you the whole time."
2. **A crossed-out name.** A heavy stroke through a name the reader wrote, one of the people they have met.
   - **Who counts as met.** The names come from the rooms the reader has personally been in (`LabyrinthData.visited`), for example Tess, Harrigan, Marla, Hank, Slaney, Marjorie, Kimber, Holloway, Karen, Zampanò, Pelafina, the Goatman.
   - **Left out on purpose.** Names that are also common words (Will, June, Ray, Dot, Bud) are not in the list.
   - **Where in the sentence.** A name opening a sentence may be any word ("Will you…"), so only names inside a sentence are struck.
   - **The word stays** under the stroke.
3. **A direction.** An answer to a line with "left" or "right" in it, whichever contradicts the line: "No. Right." or "No. Left." It is written after the last word of that paragraph, or on the row beneath it when there is no room. One answer per page.

## It only adds

- **The reader's words are never changed** by it.
- **A stroke goes with its word.** If the reader deletes the word, the stroke goes too. Each stroke is tied to that word's place in the writing, counted from the start.
- **Everything else is ink.** A written page and an answer cannot be rubbed out. An answer whose line the reader has deleted stays where the line was.
- **Tearing comes later.** Taking its ink away by tearing out the page arrives with step 5.
- **The reader's screen cannot write in it.** A page sent with notes keeps none of them.
- **Rubbings keep clear of it.** A rubbing is never laid on a page that holds one of its notes; it goes to the next page with room.

## The other hand's lettering

- **Its own hand.** The notes are in a hand of their own: heavier strokes, a pixel and a half wide as a nib lays ink, leaning steeply, with a looped a.
- **How it is drawn.** `tools/draw_journal_art.py` builds it from the same glyph source as the books' eight hands, but distinct from all of them: `textures/font/margin.png` and `font/margin.json`.
- **Checks.** `tools/verify_writing_fonts.py` now checks nine font grids. The script's `--check`, which CI runs, covers the new atlas and definition.
- **No other atlas changes.** None of the other eight atlases is rewritten.
- **Colour.** It is drawn in a dark iron-gall ink, never the reader's graphite.
- **Literature colours still apply.** The House's blue and the Minotaur's red show in the other hand's words too.

## Saved data and network

- **Page notes.** Each page gains `Notes`, the other hand's ink, set only by the server:
  - `JournalPage.Note`: mark (written, struck or direction), text, the word it is tied to and which use of it, and a position;
  - at most eight notes a page;
  - unknown marks are dropped when a page loads.
- **The day.** Each book gains `Inked`: the House day the other hand last wrote.
- **The view payload.** New C2S payload `JournalViewPayload`: the spread the journal lies open at, or -1 once it is shut.
  - It is sent when the screen opens, on each turn of the page and when it closes.
  - It is never saved. A logout clears it.
- **Protocol 101.**

## Tests and proofs

- **`JournalTests.theOtherHandWritesAfterAMorningAndAWorstRoomsEndOnlyOnHousePagesAndOnceADay`:**
  - leaving the bed early writes nothing;
  - a real morning wake (the native wake event) writes on the blank page after the last House page;
  - an Overworld page with a name is never touched;
  - a second try the same day does nothing;
  - ending an ordinary room writes nothing, and ending the trailer's night through `WitnessAccount.resolve` strikes the reader's met name, leaving the word under the stroke;
  - the ink and its day survive a save and reload;
  - left to chance it writes about one time in six (60 to 150 of 600).
- **`JournalTests.theOtherHandNeverWritesOnAnOpenPageOnlyAddsAndItsStrokeGoesWithTheWord`:**
  - a page on the open spread is passed over;
  - a name opening a sentence is not struck, and the same name later in it is;
  - the shut journal's line about going left is answered "No. Right.";
  - with nothing left to answer it writes nothing and the day is not spent;
  - none of the reader's words change;
  - after real edits the stroke goes with its word, the answer stays, and a page sent with notes keeps none.
- **`CaverTests.badAirDrains…` (extended).** The reader's blackout in the crawl leaves the blank-page note in a journal with a House page.
- **The native client proof** adds `native-journal-4.png`: a struck name, an answer under a line and a written blank page in the other hand. It fails if the other hand's ink is missing.

## Run 1346

Run 1346 tested cf4d59a (0.4.179 with run 1345's two fixes). Two live jobs failed on timing; neither touches the journal.

- **live-multiplayer.** The uncanny preflight passed at view distance 3: both readers sprinted the tunnel and every check held. The step then failed its log check. The server had logged "OTHProofA moved too quickly! -1.0,0.0,11.0" in the bedroom.
  - **Why.** Using the bed moves the reader to the wake spot, 11 blocks away, but did not reset the connection's movement check. The basement's own move in the same file does reset it. On a loaded host the client's first move from the wake spot was measured from the bed.
  - **The fix is in the room.** The bed's wake move now clears the reader's motion and resets the check (`UncannyStories`), as the basement does.
- **trailer-client.** The Backseat live proof's second client crashed at its end check: it had taken its first side-window frame, but its slow frames missed the second before the car's clock reached 60.
  - **The fix is in the proof.** Once the first frame is taken early in the acceleration, the second may follow later in the same reveal (`BackseatSocketClient`). The acceleration frame now waits for it. Every check on the frames is kept.

No layout or Witness change: layout 51; Witness 55 / 42 / two kinds / three endings. The journal still counts toward nothing.
