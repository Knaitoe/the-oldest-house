# The Pell house, the owner's playtest notes, continued (0.4.567)

The second batch of the owner's notes from playing the Pell house on 13 November (the first: `docs/PELL_HOUSE_NOTES_0_4_566.md`).
New builds only; no layout or Witness change. Protocol 189: the cast's people now carry who has hold of them (below).

## The shed

The owner: "is the player supposed to gain access to the shed at a specific time? as is, they can walk in any time june has it
opened and aren't forced to leave despite june's prompts".

When it is meant to be open:

- **The first visit.** It is padlocked. At night June opens it twice: at eleven, to take the tin marked LINE and its brush on her
  way to the strip behind it, and after painting, to put them back. It is hers then, not the readers'.
- **A week in.** By day, until June goes out on her rounds at eleven, its padlock hangs open and anybody may go in: the tapes are on
  its back wall and the set on its bench (`PellTapes.unpadlocked`). From eleven it is hers again, as on the first night.
- **The last night.** June does not make her rounds, and the shed stays as the day left it.

Until now, whoever went in after June at night could stay there: she asked them out every twenty seconds and waited for ever, and
the night waited with her. Now she asks once ("Out of there, love. I'm locking up."), and five seconds after that, if they are still in
it or in its door, she goes in for them. If she has the tin in her hand (on her way out to the line, not when she has put it back),
she first sets it down on the ground by the door to free a hand. She goes to the nearest of them and takes them
by the arm ("Come on, love. Out."; the reader is told "June takes you by the arm and walks you out."). She walks them out a step ahead
of them, round to the shed's front, where they stand clear of its door, and lets go ("She lets go of your arm."). Then she shuts and
padlocks it, takes the tin up again if she set it down, and goes on.

- She walks out more than one reader one at a time, nearest first.
- She never locks anybody in. If somebody is in it shut, she opens it to fetch them.
- Nobody is hurt.
- A reader who walks back in before she has the padlock on is walked out again.

(`PellNight.shutShed`; her pose with her hand back to theirs is `PellPeople.SHOWING`.)

The house's own doors at lock-up, and the back door on her rounds, still only ask and wait.

## The scrubbing brush

The owner: "does the brush work? or is it only intended to work on specific instances? i just want to make sure the house's
indestructible rule isn't blocking effect".

It works anywhere on the line, on any night, and the House's rule against breaking does not touch it: scrubbing is the brush's own
use, not a block broken.

- **How to use it.** Take the bucket from the laundry and hold use with its brush on a cell of the line within reach.
- **How long it takes.** On the thin stretch behind the shed each half of the paint takes two seconds. Anywhere else, where the line
  is a hand wide, each half takes five.
- **The gap.** Two cells side by side scrubbed out make a gap.
- **Repainting.** On the visits before the last, June paints every scrubbed cell over by first light. On the last night the line
  stays as the readers leave it.

What made it seem not to work:

- Nothing showed until a whole half came away: five seconds wherever the line is a hand wide.
- The work was lost as soon as the aim left the cell, even for a moment.

Now the paint goes a quarter at a time. There are two new stages, a quarter and three quarters scrubbed away, drawn for each surface
the line lies on (`tools/draw_pell_line.py`; `PellLine.QUARTER_SCRUBBED`, `MOSTLY_SCRUBBED`). The work is seen going and stays done
in the cell itself. An aim that slips loses only the quarter under way, and only after half a second away (`PellScrub.SLIP`). The
first quarter a reader takes up, the House says so: "The paint comes up under the brush." The time to scrub a cell out is unchanged.

## Taking hold of Callum's wrist

The owner: "there should be an actual animation for grabbing his wrist". Until now the reader was told "You take hold of his
wrist." and nothing of theirs moved.

While Callum struggles for the knife, the reader who has hold of him has both hands on his (`client/PellWristClient`):

- **In their own view.** Both hands are out in front of them and in toward the middle: the right closed round his wrist, the left on
  his forearm just past it. Both are pulled toward the reader and back with each of his jerks and turned with his twisting.
- **Seen by anybody else, or from behind.** Both arms are out to him at the height of his hands, turned to wherever he stands, and
  jerking with him.

His jerks are his own pose's, on his clock, so the two keep together. His fist coming throws the hold off and their hands go back
to what they were. A reader without one arm holds on with the other.

The server records who has hold of him (`LiteraryActor.holder`, a synced field on the cast's people, set by `PellKnife` and cleared
at the lunge, the cut and a restart). That is why the protocol moves to 189: an older client would not know the field.

## Answered, without a change

- **Callum's plate** is laid and stays empty at dinner on purpose (as 0.4.566 says).

## Still to come

The rest of the notes, in the next releases:

- Tilly's hair growing.
- The sleepers' pose.
- Faces.
- What the people and the readers do through the day.
- The interior.
- The flower bed.

## Tests

The Pell house namespace, 53 cases:

- **New case.** `PellHouseTests.aReaderWhoStaysInTheShedIsWalkedOutBeforeItIsPadlocked`. A reader goes into the shed after June on
  her rounds and stays. June asks them out, then comes for them no sooner than she said, with the tin set down by the door. She
  takes their arm, walks them out clear of the door and lets go. She padlocks the shed with nobody in it, takes the tin up and goes on
  to the line, and the shed does not open to a hand.
- **The scrubbing cases.** Both step through the quarters. The case on the wide line also takes the brush off a cell after a
  quarter, works another cell for a moment, and finds the quarter still done when it comes back. The case on the thin stretch hears
  the House's line once.
- **The knife case.** It finds the hold recorded while they struggle and thrown off at the lunge.

The first run of the thin-stretch case failed: it read the House's line on the tick the quarter came up, and the test player's
messages arrive a tick later, as the file's other checks allow for. The case now waits a tick.
