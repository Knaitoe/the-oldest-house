# Where you turned back (0.4.658)

The owner, with the notes that led to 0.4.656: "Let rooms remember where you turned back. If you leave Ted the Caver after placing a
torch at the mouth of the squeeze, the next visit could show your torch in the same spot and a second one just beyond where you
stopped. I'd try this first. The player knows exactly how far they went, so that one block tells the story."

Ted the Caver first. No Witness, layout or protocol change.

## Now

- **How far a reader came.** The way down is laid out in order (`CaverCave.WAY`): the passage below the ladder, every cell of the
  squeeze, the bowl to the stone, the passage behind it, every cell of the second crawl, and the low chamber. Each step has a little
  give to either side where the way is wide (the approach and the bowl), none in the crawls. While a reader is in the cave the
  furthest step they have stood on is kept in their own record (`Reached`), from visit to visit, until they turn back.
- **Turning back.** A reader who walks out of the cave alive, before they have finished it, has turned back. Dying, logging out and
  watching as a spectator are not turning back, and a reader who has finished it is never answered.
- **Their own torch.** The torches a reader sets stay where they set them, as they always have: nothing in the cave tidies them away.
- **The House's.** At the first step just beyond where they stopped that can hold a torch, the House stands one of its own. Past the
  packed rubble or the stone, that is the first place behind it, out of sight until the reader digs through or the stone rolls. A
  torch a reader set there is passed over; if the House's own already stands there, because the reader turned back at the same place
  before, nothing is added. It is set only when nobody can see the place: no one within twelve blocks, where its light coming up
  would show, and no one with a clear line to it; until then it waits, and is tried again every five seconds and as soon as the
  reader has left.
- **It is nobody's.** Its setter is recorded as the House, so no reader may take it ("That torch isn't yours to take."), and it is
  kept with the cave's other torches across a reload.
- **No word.** Nothing is said about it, in chat, the field notebook or the account. The torch is the whole of it.
- At most eight of the House's torches for any one reader.

## With others

The cave is one for everyone, so the House's torch is a real torch in it and anyone there sees it, and it can only be set while no one
there can see it go in. How far each reader came, and where they turned back, is their own.

## Checked

- `CaverTests.turningBackLeavesTheReadersTorchAndTheHousesJustBeyond`: a reader sets a torch at the mouth of the squeeze, crawls six
  cells in through the real crawl, and walks out while someone else is crawling just beyond; the House waits, and once that reader is
  back at the camp it stands its torch on the next cell, its setter the House: the reader's own torch is still at the mouth, no other
  torch is on the way down, the other reader cannot take it, and it survives a reload. Turning back at the same place again adds
  nothing; a reader who has finished the cave leaves nothing to answer.
- `CaverTests.turningBackAtTheRubbleLeavesTheHousesTorchBehindIt`: a reader who stood at the face of the packed rubble and walked out
  finds the House's torch at the first place behind it, the rubble as they left it and no work on it invented.
- The caver cases, 14/14; build, package, branding and quick checks.

## Not covered

- Only the cave remembers where a reader turned back. The other rooms do not, yet.

## New lines

None.
