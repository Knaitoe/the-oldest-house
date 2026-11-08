# Lines over heads go once read (0.4.398)

The owner, of the Masque's red figure saying *The clock is speaking.*: "his dialogue does not disappear."

## The cause

A character's line over their head is shown by `LiteraryActor.say`. It put the line up and never took it down.

Some scenes cleared their own speakers' lines after about five seconds: the elk lot, Costume Night, Movie Night, Winter Lake and Borrasca. The rest, the Masque's red figures among them, kept the last line up for good.

## The change

`say` now puts a line up for long enough to read:

- five seconds for a short line, longer for a long one, up to twelve seconds;
- then the line goes;
- saying it again keeps it up;
- the time left is saved with the character, so a line half-read when the world is saved goes on schedule after a reload.

The scenes that cleared their own lines still do. Nothing else about what characters say, or when, changes.

## Tests

`LiteraryTests.movingWhileTheClockSpeaksPutsTheReaderBackARoom` now checks that the red figure says the clock is speaking, and that the line is gone once it has been read.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
