# The text review page

A page for the owner to read every line of text the game shows, to change any of them, and to send the changes to GitHub.
It is a tool beside the mod: nothing in the game changes, and there is no version, protocol, layout or Witness change.

What the owner asked for, as it was said:

- 12 November: "Finally! Creating a ui for me to review and edit all dialogue and text in the game would be cool"
- 14 November: "the text review ui should push changes to github, ideally. or compile changes and then push as a batch."
- 14 November: "please append the text to categories/characters, and enable a search functionality"

## What it is

- **The page** is a private artifact on claude.ai, titled *Text Review*. Only the owner can open it.
- **The tool** is `tools/text_review.py`, with the page's own source beside it (`tools/text_review_page.html`). It reads
  the text out of the sources, builds what the page shows, and puts edits back.

Built from 0.4.583, the page holds 17,369 lines from 461 files: 15,213 strings in the mod's Java, the 1,940 entries of the
language file, and the titles and texts of the 108 letters.

## What is on it

- **Stories.** Every line under the story it belongs to, then under its file. A story is the first word of a class's name
  (`PellTexts` and `PellVisit` are both under *Pell*).
- **People.** Each line that the code gives to somebody is under their name, story by story: 195 people, about 2,500
  lines.
- **Kinds.** Eleven: Dialogue, Lines, Replies, Books and papers, Accounts, Letters, Names, Subtitles, Advancements,
  Interface, Commands and settings.
- **Search.** One box searches every line, and the name of the constant or method a line sits in. It works inside whatever
  story, person or kind is chosen; if nothing there matches, one press searches everything.
- **Each line** shows who it is given to, its kind, and where it is: the file, the line number, and the constant or method.
  A line the code puts together from pieces is shown as one line, with what is put between the pieces shown as a small
  grey chip, and each piece is changed by itself.
- **Changing a line.** Click it, type, keep it. The page warns when a change drops or adds one of the marks the game fills
  in (`%s`, `{0}`, a colour code), or a space at an end that joined the piece to its neighbour. A changed line shows what
  it was underneath.
- **Changes.** A panel lists every change, old over new, with a note to go with them, and one button: Push to GitHub.

## How a line gets its kind

Each line has exactly one kind, decided in this order:

1. **Commands and settings:** in the `command` package or a `…Commands` class, or passed to a command's reply or a config
   comment.
2. **Names:** a name passed before a line to say who speaks (`speak(n, "Ray", …)`), and every block, item and entity name
   in the language file.
3. **Accounts:** in a class named for an account, the Witness or the staircase's prose, or under a member named account.
4. **Books and papers:** a text block; a book's title; anything passed to a call that writes a book or a typed paper;
   anything under a member named for a book, page, letter, note, diary, journal, card, slate, poster or the like; any text
   with a blank line in it.
5. **Replies:** what a reader can answer with (`Talk.reply`, `Talk.act`, a constant that begins `Q_`).
6. **Interface:** anything in the client's own screens, and the language file's interface entries.
7. **Dialogue:** a line somebody is named as saying.
8. **Lines:** everything else a reader is shown in play. Narration, what a hand on a thing is told, and speech the code
   names nobody for are all here.

Subtitles, Advancements and Letters are what the language file and the letters file say they are.

## How a line gets its person

From the code, in three ways, nearest first:

- **A name passed before the line** in a call that says who speaks or who wrote a thing:
  `speak(n, a, "Will Dunning", "…")`, `Map.of("Ray", new String[]{…})`, `book("Title", "Casey", pages)`.
- **A name in the constant's own name:** `JUNE_PIPES` is June's, `GAIL_FOR_JUNE` is Gail's (the first name wins). A word
  counts as a name in a story when the story's prose uses it with a capital in the middle of a sentence and the code names
  at least two things by it.
- **A name in the method or the array** the line sits in.

A line given to somebody by a name passed before it is theirs to say (Dialogue), or theirs as its writer (Books and
papers). A line given by a constant's name is Dialogue unless its own words name that person ("June's room. The door is
shut."), in which case it is about them and stays under Lines.

**What this misses.** Many spoken lines are in arrays that the code reads by number, with no name near them (the film
set's conversations are the largest case). Those are under their story and file, and under Lines, with no person. Some
names are places or firms that wrote a paper (Borrasca's "Ridge Depot"). A few are wrong. The tags are a way to find
things; nothing is changed by them.

## How a change reaches GitHub

A page of this kind cannot reach GitHub by itself: it may not call any other site, and it holds no key. So the push is in
two steps, and the owner makes only the first.

1. **Push to GitHub, on the page.** The changes are written, as one batch, into the page's own store, and the page wakes
   the Claude session that keeps the mod (a scheduled task with no schedule, fired by the button; the first press asks the
   owner to allow it).
2. **The session applies the batch:** `tools/text_review.py --apply` puts each change into the source, the session
   compiles, runs the quick checks and the cases of any story whose lines changed, fixes any test or art tool that still
   holds the old words, commits, and pushes to the branch. It writes the commit back to the page, which shows it in the
   Changes panel with a link, and rebuilds the page so that it shows the lines as they now are.

If the page cannot wake the session (the owner declined, or the view does not offer it), the batch still waits in the
page's store, and the panel says so. Saying "push the text edits" to Claude does the second step. Nothing is lost by
waiting: the changes are kept with the page, not in a browser.

A push from the page cancels a run of the workflow that is in progress, as any push does. The session waits for a run
whose result is wanted before it pushes.

### How an edit finds its line

By the file, the words as they were, and which of the file's strings with those very words it is. All of a file's edits
are placed against the file as it stood and written together. If the file no longer has as many strings with those words
as the page saw, the line has moved on since the page was built: that edit is not applied, and the panel says which and
why. Only files the page shows can be written.

A name in the language file is usually also held by the art tool that draws the thing, which checks the file against
itself. When exactly one string in such a tool has the old words, the edit is made there too; otherwise the places are
listed for the session to change by hand.

## What it does not cover

- **The novel's own texts** (`correspondence/novel_sources.json`). They are quoted and stay as they are.
- **Words painted into pictures:** signs, labels and letterheads drawn by the art tools.
- **The four jukebox records' names.**
- **Strings written for a log, an exception or an annotation,** and the tests' and proofs' own strings.
- **One-word strings** that do not end a sentence ("Stage", "Beat"): nearly all of them are keys, not text.

## Keeping it

- Rebuild: `python3 tools/text_review.py --build OUT --repo <owner/name> --branch <branch> --trigger <task id>`, then
  publish `OUT/text-review.html` with `OUT/texts.js` beside it to the same artifact (find it among the owner's artifacts
  by its title). It declares three things: a store only the owner can write (`db`, rule on the root), who is looking
  (`user`), and one call on the *Claude Code Remote* connector (`fire_trigger`).
- The page's store has two collections. `edits`: one document a changed line, kept as the owner works. `batches`: one a
  push, with `state` (`queued`, `working`, `pushed`, `problem`), the edits, and, once pushed, `commit`, `said` and
  `missed`.
- Apply: save a batch to a file, `python3 tools/text_review.py --apply FILE --report OUT.json`. The report says what was
  done with each edit, which tests say the old words, and which tools hold an old name.
- The page shows the sources as they were when it was built, and says which commit that was. A stale page is safe (an
  edit to a line that has changed is refused), but rebuild it after a batch is applied and whenever lines change much.
- `python3 tools/text_review.py --check` is in the workflow's quick gate. It tests the tool on a small tree of the same
  shape and then reads the real one. A string it could not write back as it reads is left off the page and said in the
  check's last line; it does not fail the build.

## Checks

- **The tool's own check** (above): strings with every kind of escape, text blocks in both layouts, what counts as words,
  what a string is written for; then, on a small tree, thirteen edits of which seven apply, three have moved on, two are
  refused (a test's own file, a path outside the tree) and one is unchanged, with the language file's edit carried into
  the tool that holds the name, and the test that says the old words reported.
- **Every one of the 15,213 strings** on the page is written back exactly as it is read.
- **The page in a browser**, without the game: by story, by person, by kind, search, change, keep, the Changes panel and
  the push, at desktop and phone width, light and dark, with no error and no sideways scroll. Then against a stand-in for
  the page's store and the wake call: two changes kept, one batch written, the wake called once with the task's id, and
  what the session writes back (a commit, a sentence, one edit that had moved on) shown in the panel.
- **The published page's store:** both collections read, and a write below the owner's level refused.

Not checked: the wake from the real page. It needs the owner's own press, and their yes to the question it asks.

## For the owner

- **Lines are yours.** The session puts a change in exactly as it is typed. It changes a test or a tool only so that it
  says your new words.
- **The tags are guesses from the code.** Where a person or a kind is wrong, say so and the rule is changed; where a
  story's speech has no names in the code, it can be given names there.
- **Stories are named by their classes** ("Pell", "Last", "Place"), not by their titles.
- **One push is one commit** on the working branch, not on `main`. `main` moves as it always has, to a commit whose run
  passed every job.
- **The wake goes to one session.** If that session has ended, the batch waits; any later session can apply it when told.
