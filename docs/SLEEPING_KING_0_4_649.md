# A sort of thing 9: afterwards (0.4.649)

The ninth and last part of the plan (docs/SLEEPING_KING_PLAN.md), from the proposal's "Afterwards" (docs/SLEEPING_KING_PROPOSAL.md)
and its tenth decision: the crown as the keepsake, the country come back to, the Witness line by ending and the reader's own leaf. With
this part every row of the plan is built. No Witness count, layout or protocol change: the story has been one Witness source since part
3, and this part changes only which line that source writes.

## The country, come back to

The King's country joins the aftermath rooms (`Aftermath.PLACES`). It stands as what the dream's ending left (`KingAfter`):

- **If he woke** (`KingAfter.WOKE`): all of it ruled, to the top of his tree, and silent. The breath has stopped, so nothing moves at the
  edge of the filled land and nothing stirs. Nobody is under the tree. His night-cap hangs under the lowest bough on the side he lay,
  by its band (a new `NIGHTCAP` fitting).
- **If he slept** (`KingAfter.SLEPT`): all of it filled to its edges, as the last song leaves it, and the breath going on. He is asleep
  under his tree in his night-cap, and nobody else of the dream is there. The feast on the eighth square is filled in, with nobody at
  it.

In both, nothing of the dream goes on: the brothers, the Queen, the Guard, the Caterpillar, the court, the Knight and the guests are all
gone, nothing counts a reader or stirs him, and a reader cannot be crowned or put in check. On the country's land a reader is a line,
from one end of it to the other; in the drawing-room they are a thing (`KingBreath`, which knows the house's floor from the country's
by what is under the reader's feet).

**Whose ending decides it.** It is decided for a dream, not for each reader: by everybody in that copy of the country at once. It is
the country after he woke if any of them woke him, stood there until he woke, was there when he woke, or left him his sleep and heard
that he woke after they had gone. Otherwise it is the country he sleeps on in. A reader coming into it who has not had an ending makes
it a fresh dream again, for everybody there, with all its people back where a fresh dream has them; when they have gone it is as it was.
A waking that has just ended and that a reader's own tick has not yet written down counts already (`KingSleep.comingTo`), so the
country never flickers back to a dream at the moment of the waking.

**Dealt back.** As every aftermath room is: to a reader who has read their account, at most twice, never on the visit their ending
landed on (`KingEndedVisit`), and each return counted once, on a later visit. Two seconds into a counted return the reader is told what
is there, once. A reader whose ending landed and who has not read the account finds the same country and the account still on the
armchair; that visit is not a return.

## The keepsake: the crown

A crown is real in the King's country while his dream goes on. Anywhere else (the House, the Overworld, the country come back to) it
is only drawn: an outline of the band, its eight points and its three stones, with nothing in it (`KingKeepsake`, the item's
`the_oldest_house:drawn` property and its `king_crown_drawn` model).

- **It fills in while its wearer is watched**, by another reader or by one of the House's people. Looking is as it is in the country:
  a head turned to within twenty-five degrees of the wearer with nothing between them. A reader looks from up to forty-eight blocks,
  one of the House's people from up to thirty-two. The House's people are whatever the House has made that is alive, awake and to be
  seen by this reader: not somebody asleep, not somebody only another reader can see, and not anything that came from outside the
  House. A look that has just left holds the crown for a second, so it does not flicker at the edge of a look.
- **Its wearer hears it fill in** (`king.crown_fills`, theirs alone, at most every three seconds) and is told once what each of its
  two looks means.
- **Only a reader's own crown, worn,** fills for being looked at. One carried, or another reader's on a head, is drawn outside the
  country and filled within it. Which it is, is kept on the crown, so everybody who can see it sees the same crown.
- A drawn crown is still its wearer's, but it gives nobody the Queen's lines.

## The account, the Witness line and the leaf

- **The account** is unchanged from the earlier parts: "The account: the ruled country", with the parts of the country the reader
  went through and the way they left the court.
- **The Witness line** now follows the reader's own ending (`KingTexts.witness`). The two lines the proposal gives are used, with one
  word changed (below), and a third is new, for a reader who was there when he woke.
- **The reader's own book.** A new fact for the leaves, `ruled` (`StaircaseProse`, `StaircaseAccount`): written once the reader has had
  an ending, with the King left awake or asleep. It has a ledger line, three phrasings and a margin note for the memory page. The first
  phrasing is the proposal's line for the leaf, turned to the reader as the leaves speak.

## With more than one reader

The country come back to is one per copy of the country, and decided by everybody in it, as above: one reader without an ending makes
it a dream again for everybody, and a waking wins over a sleep. Whether a reader is dealt it back, and each return counted, is their own
record. Each crown is its wearer's, and fills for any reader's look but its wearer's own; a crown in the country fills or empties for
everybody who sees it at once.

## Art and sound

- `tools/draw_king_art.py`: the night-cap off his head, in the cloth he is painted in (`nightcap`, its turned-up band and its tassel),
  hung by its band from a ruled twig: the band, three boxes of wool sagging each a little narrower and to one side, and the tassel at
  the tip. The drawn crown is the filled crown's every edge as a bar 0.4 of a pixel wide in one pencil grey (`item/king/crown_pencil`),
  the stones as small squares. 99 textures in all now.
- The same tool now writes an explicit texture square for every face of an element that reaches past its block. Without one the game
  takes the face's texture from where the face lies, which can be off the edge of its texture and into whatever is next to it in the
  atlas. Twelve of the King's block models and the crown had such faces (the armchair, the carriage seat, the glass table and its
  bottle, the feast chair, the candles, the mutton and the pudding, drawn and filled, and the crown's points). None showed the fault in the frames, but it was
  there to show at any time the atlas was laid out differently. The rest of the mod is to be swept the same way.
- `tools/generate_king_audio.py`: `king.crown_fills` (a breath of pencil being shaded in, and the band's small clink). Thirteen cues.

## Checked

- `KingTests.theCountryComeBackToAfterHeWoke`: a fresh dream first, with him asleep and his people there; a reader alone in it with
  their ending makes it the country after he woke (all ruled, his tree to its top, nobody under it, none of the dream's people, his
  night-cap on a bough, the breath stopped); the reader is a thing in the house and a line on the first square and beside where he lay;
  a later visit counts one return and says what is there; a hand on the night-cap says what it is; the Witness line follows each of the
  four endings; the reader's own book tells that he was woken; a reader with no ending makes it a dream again (him under his tree, the
  night-cap on his head, the brothers back), and when they go it is as it was, with still the one return.
- `KingTests.theCountryHeSleepsOnIn`: a reader alone in it having left him his sleep makes it the country he sleeps on in (all filled
  and breathing, the eighth rank and the feast filled, him asleep in his night-cap, nobody at the feast, a line beside him on the
  in-breath who does not stir him); one return counted; the book tells he was left asleep; if he woke after they had gone it is the
  ruled country.
- `KingTests.theCrownOutOfHisCountry`: filled in his country, worn or carried; drawn out of it, worn or carried, and still its wearer's;
  told so once; in the country come back to, drawn with nobody looking; another reader's look fills it, and its wearer hears it; a look
  that has just left holds it a second; somebody asleep is not looking, nor is somebody only another reader can see, but somebody who
  is this reader's own to see is; another reader's crown on a head fills for nobody's look.
- `KingTests.theBrothersTheRattleAndHisName` now expects the country after he woke where it expected a fresh dream: both its readers
  have had their endings by then.
- The earlier ten cases still pass: 14 King cases in all.
- The book check, at the client's own fonts: the third phrasing of the leaf, as first written ("... When you left it the King
  was awake."), ran to fifteen lines on a fifth-leaf page with the widest fact the check writes. It was shortened to the line
  below; every leaf page fits in fourteen lines.
- Look review, 38 frames from a headless client, 14 of them new: the drawn crown worn from five sides and as an item beside the filled
  one, large and at the size a pack shows it; the night-cap on its bough, close, and under the tree; and the room's tall pieces again
  from two sides after their faces were given their own squares. What the frames found:
  - The drawn crown reads as an outline of the filled one. Through it the wearer's hair shows, as it should.
  - The night-cap hangs by its band under the bough, in the cloth he is painted in, its tassel straight down. In the ruled country it
    is the one thing still filled in, which is as it should be: it is what he left.
  - The two wide views of the night-cap were drawn with the ground between the camera and the tree. The review's own framing was at
    fault, not the mod: they were drawn again without the ground under a camera that looks up.
  - The tall pieces show their own faces from both sides; nothing samples from beside them.

## Fixed after run 1683

Run 1683 (0.4.648) failed the drawing-room case in the full suite: the King was found standing under his tree. He sleeps on the
ground there, not in a bed, and the game gets up any sleeper with no bed under him on that sleeper's own tick; the dream laid him
down again after, so he was stood up and laid down again every tick that his own tick ran, which is whenever a reader is near
enough to his tree (in that run, the chunk was kept ticking by the suite). Only the dream wakes him now (`KingSleep.keepSleeping`).
The drawing-room case now puts a second reader by his tree and checks that he sleeps on: it failed before the change, as run 1683
did, and passes after it (14 King cases).

## New lines (for the owner)

The Witness line, by the reader's own ending:
- Said his whole name, or stood until he woke: "I woke the King, and came to in the drawing-room with his country gone." (The
  proposal's line, with "hall" made "drawing-room": a reader comes to in the drawing-room, by the fire, not in the House's hall.)
- There when he woke (new): "I was in the King's country when he woke, and came to in the drawing-room with it gone."
- Left him his sleep: "I left the King asleep and walked out of his country as a line." (The proposal's line.)

Cues:
- The country after he woke, on a counted return: "The country is all lines, and nothing in it is breathing. His night-cap hangs on a
  bough. Nobody is under the tree."
- The country he sleeps on in, on a counted return: "The country is filled in to its edges, and he is breathing under his tree. Out on
  it, you are a line."
- A hand on the night-cap: "His night-cap, hung on a bough by its band. The tassel hangs straight down."
- The crown, the first time it is only drawn: "Out of his country the crown is only drawn: an outline, with nothing in it."
- The crown, the first time it fills in for a look: "The crown has its weight back. Something is looking at you."

Subtitle: "A crown fills in".

The reader's own book (the `ruled` fact):
- Ledger: "The King: awake" or "The King: asleep".
- "In a country ruled into squares you were a thing while you kept its rules, and a line when you did not." (The proposal's leaf, turned
  to "you".)
- "An old man was dreaming a country in squares. On it you were as much of a thing as his dream made of you."
- "You crossed a country of squares, a thing on some and a line on others, and left the King awake." (or "asleep")
- Memory page: "You crossed the King's country. The writer has drawn you twice in the margin, once filled in and once in outline."

## Not covered

- Whether a crown is watched is decided on the server from where heads are turned. The look of one of the House's people cannot tell
  everything that lies still from everything that is awake: only the sleeping, the invisible, those faded right out and those who are
  another reader's own are left out.
- The crown filling in on a client is seen in the review frames (the model, both ways) and checked on the server (its state), not by a
  two-client proof.
- A reader who was only there when he woke, and was never crowned, has no keepsake; a crown that is lost is not given again.
- A crown left in a chest or on the ground keeps the look it had until somebody carries it.
