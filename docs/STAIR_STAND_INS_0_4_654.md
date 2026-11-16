# Someone on the stair: the first five (0.4.654)

0.4.596 set the people of each reader's own stories on the great staircase. Nineteen stories still had nobody there. The
staircase note (docs/STAIRCASE_JUDGEMENT_0_4_652.md) said each of them would get someone from 0.4.654 on. This release gives
five of them someone: the stories whose people are the cast's own bodies but could not yet stand on a stair, because they were
only ever drawn seated or swimming, or because the story gives each reader a different person. The other fourteen come next.
No Witness, layout or protocol change.

## Who

| Story | Figure |
|---|---|
| Harrigan | Mr. Harrigan, dead and on his feet |
| Last Swimmer | the swimmer, out of the water |
| The Ones Already Inside | Daniel, or Dan where he went back into the frame |
| A Little Longer | Rowan, the one the reader was given |
| A sort of thing | one of the brothers, alone and pointing |

As with every figure since 0.4.596, each reader's people are their own: nobody else sees them, none of them speaks or can be
touched, they move only while unseen, and nothing is saved of them. They stand only for a reader who heard their story to the
end.

### Mr. Harrigan, on his feet

- **Before.** He was only ever drawn in his chair, alive or dead, or laid in his casket. He could not stand on a stair.
- **Now.** He stands as the second visit has him: dead, in his suit, drawn with the painting of him dead. His head is bowed a
  little and to one side; his arms hang with the elbows a little bent.
- **His body is a standing man's.** In his chair his body is a seated man's, a third shorter. Standing on the stair with that
  body, the looks that keep a figure still would have aimed below his head, so he could have moved while a reader saw only
  his face over a rail. On his feet he has the body any standing figure has (`LiteraryActor`).
- His own scene never stands him up. Being on his feet is a phase of its own (`HarriganVignette.STANDING_PHASE`), used only
  on the stair, drawn by his own renderer (`HouseHumanRenderers`, variant `STANDING`).

### The swimmer, out of the water

- **Before.** Drawn only swimming, at the pool.
- **Now.** Standing, cap and goggles on, head a little down: as nobody at the pool ever saw them. A phase of its own
  (`LastSwimmer.STOOD`), never used at the pool, drawn with the swimmer's own painting.

### The ones already inside: whichever man the reader's ending left

- The house comes to one of four ends, and two men are in it: Daniel, and Dan, who was made out of him.
- **Returned.** Dan went back into the frame he came out of, and Daniel walked out. Dan stands on the stair: he is the one
  the house took back.
- **Completed.** Daniel was held in a doorway until there was no room for him. Daniel stands on the stair.
- **Severed or neither.** The house let go of both, or both died of the same blows. Daniel stands, the first of the two.
- A reader with no end of it (who cannot meet the figure, since only finished stories have one) would be shown Daniel.

### Rowan: the one the reader was given

- A Little Longer gives each reader one of two Rowans (0.4.630), fixed the first time they meet and kept. The figure is that
  Rowan.
- For a reader with none kept (who also cannot meet the figure), the story's own rule chooses one, on a copy of their part of
  the story, so nothing is written by asking.

### One of the brothers, alone and pointing

- **Before.** The brothers of A sort of thing were drawn only as a pair, each with an arm round the other: linked,
  pointing, dressing, fighting, running.
- **Now.** The first of the two stands here without the other, one arm held out to his side and his head turned the way it
  points, the arm swaying a little. A gesture of its own (`KingBrothers.ALONE`).
- **Why him.** In the story the pair point opposite ways, each with his free arm, whenever the King lies asleep within
  twelve blocks of them. Alone on the stair, one of them still points.

## Dressed for each reader

- A figure's row can now say how it is dressed for one reader, where that reader's own story chose: `StairCompany.Figure`'s
  `dressed`, read through `phaseFor`. A row without it is dressed as its row says, as before.
- Each reader's figure is set down for that reader alone, so two readers whose stories went differently each see their own:
  on the same stair one sees Dan and the other Daniel.
- Choosing reads the reader's Witness record and a copy of their part of the story. Nothing is written.

## Not on the stair yet

Fourteen stories, whose people are some other kind of body than the cast's. They come next, from 0.4.655, each drawn by its own
story's renderer and set down and kept as the cast's bodies are.

| Story | Figure on the list |
|---|---|
| Hide and clap | the girl from the wardrobe |
| Drowned Town | Stacey Graves, as she hunts |
| Preserved cave | the preacher |
| Shallows | the girl as she was before the lake |
| Goatman | the cousin who was one too many, eyes lit |
| Séance | the mother from the table |
| Hotel | the bartender |
| Last Stop, and the fan room | the elk, standing and looking |
| Backseat | the one from the back seat |
| Third Floor Girls' | the woman from the mirror |
| Work print | the figure in sacking |
| Left-Right Game | Marjorie from the salt |
| Dead Air | the Skin-Taker |

## Checked

- `StaircaseAccessTests.aFigureIsDressedAsTheReadersOwnStoryLeftIt` (new): the lodging house's and the radio shop's figures
  are dressed per reader; a reader whose Dan went back into the frame is shown Dan, one whose Daniel was held in the wall is
  shown Daniel, and so is a reader with no end of it; each reader's kept Rowan is the one shown; a reader with none is shown
  one of the two and nothing is written into their record; set down, each body wears its own reader's dress, and two readers
  see different people; and each of the five new figures stands with a standing body's height, its eyes at a standing
  body's.
- `StaircaseAccessTests.everyFigureKeepsItsPlaceWhileTheServerRuns`: all fifty-three figures now (forty-eight before), each
  dressed as for the reader it was set down for, on a wider grid; forty ticks on, each is where it was set down, turned and
  dressed as it was.
- `StaircaseAccessTests.everyStoryIsAccountedForOnTheStair`: every story still in exactly one of the four lists.
- The fourteen staircase cases and the fourteen King cases; build, package, branding and quick checks; architecture, writing
  and the client proof.
- Review frames of each new figure drawn by its own model and painting, from the front, three-quarters and the side, in the
  pose it stands in: Daniel, Dan, both Rowans, each brother alone, the swimmer and Mr. Harrigan. The first frames of Mr.
  Harrigan read, from the side, as if his head sat off his neck; it bows less now. Rowan's face, enlarged, has no second face
  in it: the dark band under the eyes is the stubble.

## Caught while checking

- **The lone brother would have crawled.** A stranger whose gesture is 5 is drawn on all fours, with a body a third shorter,
  unless that stranger's story keeps gestures of its own (`LiteraryActor.crawling`): 5 is the crawl Costume Night gave the
  cast in 0.4.103. Standing alone is the brothers' sixth gesture, so it is 5. The review frames drew the pose straight from
  the brothers' own pose and showed him standing; in the game he would have been on all fours. The new case's height check
  caught it.
- **The Knight in A sort of thing was drawn on all fours while he was watched riding** (since 0.4.647). His gesture is three
  bits of his own (filled in, off his horse, riding), so filled in and riding is 5, the crawl: the crawl's pose was laid over
  his rider's pose, and his body went a third shorter, which also lowered the middle of him that a reader's look aims at.
  Riding unwatched is 4, the plain reach (both arms out), which his own pose then covered. The brothers' run (4) had the plain
  reach's arms mixed into it.
- **Mended for the whole of the King's country.** Its people (the King, the brothers, the Queen, the guard, the Caterpillar,
  the court, the Knight and the feast's guests) all keep gestures of their own (`King.cast`, listed in
  `LiteraryActor.ownGestures`), as Last orders, the Kept, Bike night, the Pell house, the lodging house and A Little Longer
  already did. `KingTests` steps the Knight through all eight of his gestures and each brother through his six: none is the
  crawl or the plain reach, and each keeps a whole body's height.

## Not covered

- The review frames draw each figure alone and lit, not on the stair in the dark: neither as a shape at the edge of the
  book's light on the way down, nor by a camp's fire on the way home.
- The review frames draw each pose by its own pose code, not through the cast renderer's whole pass, which is how the
  brother's crawl hid from them. The crawl and the plain reach are now checked through the same tests the renderer makes
  (`crawling`, `reaching`), not by a frame.
- Nobody has walked the stair with these figures.

## New lines

None.
