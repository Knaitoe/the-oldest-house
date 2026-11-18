# A Place for You: the older pictures (0.4.691)

The seventh slice (item 6 of `docs/A_PLACE_FOR_YOU_PLAN.md`), the proposal's N6C, a familiar stranger and what was actually shared:

> Alex or Ren, whichever the reader actually met, takes them to a row of older photographs. One appears to show the player with this
> person before arrival. The NPC explains a shared memory with false confidence, then reveals inconsistencies. If the player confirms it,
> the relationship becomes warmer but more house-claimed. If the player corrects it, the NPC keeps the actual shared moment only. If the
> player removes their image, the NPC loses the false memory in the middle of a sentence and must form a new relationship from the actual
> present. The scene can lead to friendship, romance, rivalry, mutual protection or a respectful parting, each based on later actions.

Code: `PlaceForYouOlder` (new), `PlaceForYouPortrait` and its block entity (new), `PlaceForYouPortraitRenderer` (new, client), a framed
form of the kitchen drawing in `HouseReadKitchenClient`, hooks in `PlaceForYou` (the guide's mark, talking at the sink, the evening's
clock, the right-clicks), `PlaceForYouSite` (the three older pictures, the copied picture and its nail, the box in the back room, the
back room's door), two new kinds of the fitting block (`OLDER`, `PHOTO_BOX`), one carried thing, the art in
`tools/draw_place_for_you_art.py` and the account in `PlaceForYouTexts`.

## What happens

- **Who takes the reader up.** The one the reader actually stood with under the eaves at the shelter: Alex if the reader stood by the
  man or between the two, Ren if by the woman. A reader who stood out at the lantern, or whom the House did not read, is taken up by
  whichever of the two they spoke to first tonight; otherwise Alex.
- **At the sink.** While Mara is at the sink (after her room and the back door have had their turn), the guide asks: "Come and look at
  something with me? It's on the landing." Two answers: show me, or not now. Not now is a whole answer; the guide doesn't ask again.
- **The landing.** Three older photographs hang on the landing wall: Mara's mother at the lake with her shoes on, the house before the
  porch was closed in with a family nobody knows on it, Mara and Theo on a sled. Between the second and the third, in a frame like
  theirs, is the reader's own picture from the kitchen table, as they drew it. It is hung only when the guide takes them up; until then
  the wall has only the three.
- **The claim.** The guide names the old pictures, then the reader's: the two of them at the lake, the summer they went every weekend,
  the reader at the end of the dock. Then: "That isn't right." What actually happened between them, from the shelter's record: the eaves
  in the rain, the walk out to the lantern, or nothing before tonight's supper. "That part happened. I don't know where she got the
  rest."
- **Five answers.** Something of our own; friends and that's all; none of it happened, she put me there; I need some room; what do you
  want tomorrow. Each is answered and kept for the next slice's branches (item 7). Mara's supper call waits until the answer is given.
- **While the picture hangs.** The guide, near the reader on the landing, says the claim over their head now and then ("You always had
  the end of the dock."), and says it again if spoken to.
- **Taking it down.** The picture comes off its nail into the reader's hands and the guide loses the sentence partway through: "You
  always had the end of the dock, with your feet in the—", then "I had something there. It's gone." and a new beginning: "I don't know
  where you like to sit. I'd like to find out." (or, if the reader asked for room, "You can tell me or not."). The guide mentions a box in
  the back room for the pictures nobody hangs, and the back room's door, shut all evening, now stands open.
- **Hung back.** The claim comes back word for word ("Back on its nail."). Down again, the guide loses it again, shorter: "The summer at
  the—" / "No. Gone again."
- **The box.** Laid in the box in the back room, among the old loose prints, the picture stays there. The back room's door shuts again
  once nobody is looking at it.

## With more than one reader

Each reader's evening is a copy of its own: its landing, its copied picture (that reader's own drawing), its box and the guide's answers
are that reader's own. Nothing is shared.

## Art

Drawn at sixteen pixels by `tools/draw_place_for_you_art.py`, except the reader's own picture, which is drawn live from the kitchen
drawing:

- The three older photographs (`older_0`..`older_2`): a print eight pixels by ten in six sepia tones, a white border, a dark frame
  twelve by fourteen worn at the corners, each on its own nail. Mara's mother on a rock at the water's edge, sitting with her shoes on,
  the far shore's trees behind her; the house with its gable and upstairs windows, the porch still open, four people standing on it;
  two children on a sled at the top of a hill, the slope falling away to a fir.
- The reader's picture on the landing (`pfy_portrait`, a block entity drawn by `PlaceForYouPortraitRenderer`): the kitchen drawing on its
  sheet, in the reader's own crayons, a little larger than on the fridge and centred like the others, in a frame of the same dark wood
  with a worn top edge, on a wire from a nail. Taken down, the nail alone.
- The box in the back room (`photo_box`, shut and open): a shoebox in worn cardboard with a pencilled label; the lid tied both ways with
  string; open, loose old prints in the bottom and the lid leant behind it. The reader's picture lies on top once it is in.
- Carried: the reader's picture, frame and page with a pencil figure (`pfy_carried` 9).

## New lines

Every line below is new in this release.

**Cues**

- "{Alex/Ren} goes up ahead of you." *(new, flagged)*
- "Your picture hangs between theirs, in a frame like theirs." (narration in the Talk panel) *(new, flagged)*
- "Your picture is still up between theirs." *(new, flagged)*
- "A woman at a lake, sitting with her shoes on." / "A family on the porch before it was closed in. Nobody you know." / "Two children on
  a sled at the top of a hill." *(new, flagged)*
- "Your picture, in a frame like theirs." / "Not while {Alex/Ren} is showing you." *(new, flagged)*
- "A bare nail where your picture was." / "Back on its nail." *(new, flagged)*
- "The back room." / "A box of photographs nobody has hung." / "Your picture on top of the others." *(new, flagged)*
- "You lay your picture in the box with the others nobody hangs." *(new, flagged)*

**The reader**

- "Show me." / "Not now." *(new, flagged)*
- "Then let's have something of our own." / "I'd like to be your friend. That's all." / "None of that happened. She put me there." / "I
  need some room from all this." / "What do you want tomorrow?" *(new, flagged)*
- "Go on." (after the guide falters) *(new, flagged)*

**Alex or Ren**

- "Come and look at something with me? It's on the landing." / "It'll keep. Everything here does." / "It's at the top of the stairs." /
  "Up here." *(new, flagged)*
- "That's Mara's mother at the lake. She went every summer, Mara says, and sat with her shoes on." *(new, flagged)*
- "That's the house before the porch was closed in. Nobody knows who those people are. Mara says they came with it." *(new, flagged)*
- "Mara and Theo on the hill behind the school. The winter the creek froze." *(new, flagged)*
- "And that's us. At the lake, the summer we went every weekend. You had the end of the dock and wouldn't give it up." *(new, flagged)*
- "That isn't right. We stood under the eaves in the rain, you and me. That happened. I remember the gutter running over." / "That isn't
  right. I watched you walk out to the lantern in the rain. That happened." / "That isn't right. I met you tonight, at the table. That's
  the part that happened." *(new, flagged)*
- "I liked being there with you. That part happened. I don't know where she got the rest." / "I liked sitting across from you. ..."
  *(new, flagged)*
- Answers: "All right. Something of our own. Starting tonight." / "That's plenty, in here." / "I know. I can hear her doing it while I
  talk. I'm sorry." / "Then take it. I'll go down first. I won't ask you up again." *(new, flagged)*
- Tomorrow: Alex, "To wake up somewhere I didn't fall asleep."; Ren, "To walk out the front door and have it be a street." *(new,
  flagged)*
- Over their head while the picture hangs: "You always had the end of the dock." / "The summer at the lake. You remember." / "You used to
  sit with your feet in the water." *(new, flagged)*
- Hung back: "You always had the end of the dock, with your feet in the water." *(new, flagged)*
- The picture down: "You always had the end of the dock, with your feet in the—" / "..." / "I had something there. It's gone." / "I
  don't know where you like to sit. I'd like to find out." (or "...You can tell me or not.") / "There's a box in the back room. The ones
  nobody hangs go in there." *(new, flagged)*
- Down again: "The summer at the—" / "No. Gone again." *(new, flagged)*

**The account** (after the back passage, before supper)

- "{Alex/Ren} took me up to the pictures on the landing: Mara's mother at the lake, the house before the porch was closed in, Mara and
  Theo on a sled. Mine was up there with them, in a frame like theirs." *(new, flagged)*
- "{Alex/Ren} said it was the two of us at the lake, the summer we went every weekend, and then said that wasn't right." *(new, flagged)*
- "What happened was the two of us under the eaves, in the rain." / "What happened was {Alex/Ren} watching me walk out to the lantern." /
  "What happened was supper, that night." *(new, flagged)*
- "I said we should have something of our own." / "I said I'd like to be friends, and that was all." / "I said none of it had happened,
  and that Mara had put me there." / "I said I needed some room from all of it." / "I asked what Alex wanted tomorrow. To wake up
  somewhere he hadn't fallen asleep." / "I asked what Ren wanted tomorrow. To walk out the front door and have it be a street." *(new,
  flagged)*
- "When I took my picture down, {Alex/Ren} lost a sentence halfway through and started a new one." then, if boxed, "I put the picture in
  the box in the back room, with the ones nobody hangs." *(new, flagged)*
- "I took my picture down and put it back up. {Alex/Ren} lost the lake and found it again." / "I left my picture where it was." *(new,
  flagged)*

## Cases

- `PlaceForYouTests#theOlderPictures` (new): the three older pictures on the landing and no copied picture before the guide asks; Alex
  asks at the sink; shown; the copied picture hung; on the landing, the claim, the eaves line and five answers; asked about tomorrow; the
  picture taken down and the falter's lines; the back room's door open; hung back (rehung); down again, the shorter falter; laid in the
  box; the account's sentences.

## Results

Locally: the twelve `PlaceForYouTests` cases and the cast and together conduct checks for A Place for You, 14 of 14. Eye-level frames of
the landing with the pictures and the reader's own, close and from below; the bare nail; the box in the back room, shut
and open with the picture in it. The first frames showed the reader's frame in a different wood and a pixel off centre, the house
reading as a hat, the sled's fir as a cross and the woman at the lake lost in the water; all four were redrawn and checked again.
