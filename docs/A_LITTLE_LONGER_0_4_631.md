# A Little Longer 0.4.631: leaving early and coming back

The eleventh slice of the plan (docs/A_LITTLE_LONGER_PLAN.md): "A shorter evening with nothing invented; reunion on return; nothing
undone by a restart." From the proposal's "Departure, saving, and a shorter visit": "An explicit early departure can produce a shorter
encounter. Rowan can ask once whether the reader would like company. If the reader agrees to continue together early, compress the later
practical preparations and stage the same severance without fabricating an intimacy that never developed... A player returning in another
session can experience a genuine reunion." Code: `labyrinth/LongerEarly.java`.

## What happens

- **Going early.** Once the work at the bench is done, and before the door moves (chapter 5), a reader who has been in the shop or the
  flat and goes out into the House's corridor is asked once by Rowan whether they want company. Three replies:
  - "Come with me." Rowan goes for their coat: on foot up the back stair if a reader can see them at the bench, otherwise they are at
    the hook in the coat when next seen. Then the same cut as anybody's (LongerCut): the toaster, the wrong edge, "Wait.", the passage,
    the room afterward. Nothing between is played or invented: no roof, no list, no tomorrow.
  - "I'll come back." or "No. Goodnight." The reader goes and the evening waits where it was.
  Rowan asks only once per reader, whatever the answer.
- **Coming back.** A reader who comes in again before the cut is met by Rowan once, the first time Rowan can see them within six
  blocks, with a line for how far the evening had got (after the bench; after the tea; after the roof). Each chapter already starts
  again from its own beginning when a reader arrives (its own arrive), so nothing half done is carried over.
- **Nothing undone by a restart.** Rowan's runtime state is rebuilt from the reader's record on arrival: the same body is found again,
  not made a second time, in the same cast. After the cut the room afterward is rebuilt and Rowan is never made.
- **The account** says only what happened: "I went out for a while and came back." after the chapter where the reader first came back,
  and "I said I was going, and Rowan said they would come with me." before the cut. Two corrections that leaving early made visible:
  the kettle paragraph appears only if the tea was had, and "The radio stopped halfway through the tune." only if the radio was on when
  the cut came (the cue, too).
- **Kept**: `Early0631` (ASKED, WITH, BACK, ALONE), `Returns0631`, `BackAt0631` (how many chapters were behind the reader the first
  time they came back), `Visits0631`, and `Stopped0631` on the cut.

## New lines, flagged for the owner

Rowan:
- "You're going? Do you want company?"
- "Give me a minute. I'll get my coat and pull the toaster's plug out."
- "I'll be here."
- "Goodnight, then."
- "You came back. I was about to stop for a cup of tea." (after the bench)
- "There you are. I thought you'd gone for good." (after the tea)
- "You came back. I didn't know if you would." (after the roof)

Replies: "Come with me.", "I'll come back.", "No. Goodnight."

Account sentences: "I went out for a while and came back." and "I said I was going, and Rowan said they would come with me."

## Tests

`LongerTests.leavingEarlyComingBackAndTheSameCut`: after the tea the reader leaves and the server's memory is cleared; on coming back
the same Rowan (same id, same cast) is there and the tea is still had; Rowan greets them back with the after-the-tea line, kept with
where it was; going out by the corridor they are asked once, with all three replies; "Come with me" puts Rowan in the coat at the hook,
and the same cut follows; the account has the coming back, the going together and the wall, and no roof, no list and no radio stopping;
it ends with the kettle; the outcome is left_before_tomorrow; after a second clear the room afterward is kept and Rowan is not made. The
longer namespace passes 21 of 21.

## Not covered

- Rowan's walk up the stair when seen at the bench has no case of its own (the case's reader is in the corridor and cannot see the
  flat, so Rowan is placed at the hook unseen).
- "I'll come back" and "Goodnight" have no case beyond being offered.
- The House's read of the reader (personhood) is slice 12.
