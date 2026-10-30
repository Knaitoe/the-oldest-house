# Writing as grounding: 0.4.199

This is step 6 of [JOURNAL_DESIGN.md](JOURNAL_DESIGN.md): writing as grounding. It is the owner's idea 4.

## The static

When one of the House's worst rooms ends for a reader, a lingering static stays on them (`journal/Grounding`). The rooms are the same list the other hand answers (`CoAuthor.DREAD`), and the static starts when Witness records that reader's ending. It has three parts:

- **a faint ringing in the ears:** an original synthesized loop, `journal.static`, subtitled "Ringing in the ears";
- **the view not quite steady:** a slow, small drift of the camera, never a shake;
- **the edges of sight a little dark,** the way vanilla darkens them at night, only more.

All of it is scaled by Minecraft's screen-effects setting: the sound, the drift and the dark. At zero there is nothing to see or hear (`Grounding.strength`).

## It fades by itself

- **It never blocks anything:** no movement, use or menu.
- **It fades over about five minutes of play,** a second for every second the reader is in the world. Time away does not count, and what is left is there when they come back.

## Writing settles it

One full line in the reader's journal settles the static within three seconds, under the graphite's sound. The line must:
- be at least twenty-four letters long;
- be new, not already on the page;
- be written in an ordinary hallway of the House.

An ordinary hallway means:
- a hall from the House's ordinary set: the junction, the gray and plain halls, the stone halls;
- not a story room;
- no hunter within twenty-four blocks: no monster the House has let in, and nothing hunting the reader.

Writing anywhere else, or a shorter line, does nothing. Nothing explains this: there is no bar and no hint. The sound and the quiet that follows are the whole cue.

## What it is not

The static counts toward nothing, and the journal's words are kept as written. Witness stays 55 / 42 / two kinds / three endings.

## Build

- **Code.** New: `journal/Grounding`, `client/GroundingClient` and `network/GroundingPayload` (server to client: the static's level, and whether it was just settled). It is called from `WitnessAccount` when a story resolves and from `Journals` when a page is kept. The state lives in `LabyrinthData`, under `grounding_0199`.
- **Protocol 106.**
- **Sound.** `tools/generate_journal_audio.py` writes `sounds/journal/static_ring.ogg`: a 6.4 kHz tone with slow wavers over a thin hiss, a seamless six-second loop.

## Tests

`JournalTests.aFullLineWrittenInAQuietHallSettlesTheStaticTheWorstRoomsLeave` checks:
- the screen-effects scale, with zero showing nothing;
- a gentler story leaves nothing, and one of the worst leaves the static full;
- a full line in a story room does not settle it;
- a short line in a quiet hall does not settle it;
- a full line with a hunter the House let in nearby does not settle it;
- a line already on the page is not new;
- a new full line in a quiet hall settles it;
- left alone, it fades by itself;
- nothing is counted, and the words are kept.
