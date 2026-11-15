# The five fires as the nights of the descent (0.4.595)

## The owner's words

15 November: "I need to consider a way for the staircase to keep its immense size and depth for the spectacle, but also to keep the player engaged."

What I proposed once the novel had been read: "The five fires become the nights of the descent, each at a camp; the three cold camps already down there become three of them. Each stretch's leaf sits in that stretch's own episode, so there is something to find between fires." And on the dark: "I now lean to keeping it at the book's twelve blocks all the way down."

The owner: "Sounds good. I agree with and appreciate the res of your suggestions too."

This release is those two things. The reader's own stories' people on the stair are the next release; the list of figures is with the owner.

## What the novel has

The descent takes days. The explorers camp on the stairs for three nights, and what they carry lights only what is next to them.

## What it was

- All five fires stood on the first five landings, within eighty blocks of the door. A reader had lit them all in the first minute or two, and then walked twelve hundred blocks with nothing asked of them.
- Once the fifth burned the stair's dark lifted, and the well showed forty-six blocks for the rest of the way down.
- Three cold campfires stood far below, on landings 9, 40 and 71, and did nothing.

## What it is now

**Five camps, spread down the whole stair.** They stand on corner landings 9, 25, 40, 56 and 71, counted from the door: 166, 446, 709, 989 and 1,251 blocks down. The cell is at 1,280. Three of them are the cold camps that were already there. Each has its hearth on a hearthstone and two bedrolls laid out from it toward the walls, clear of the walk from one flight to the next.

**A reader goes no further than the camp whose fire is cold for them.** This is the rule the first five landings had, moved. Past each camp the stairs vanish into the dark, as they did, and the reader is put back on the stair they last stood on.

**Each camp's leaf lies above it.** The five leaves lie on landings 6, 24, 38, 54 and 68: three, one, two, two and three landings above their camps. They lay on flights, out to one side on a level tread, and settled into a book that passed within a block and a half. With the camps this far apart a leaf missed on a flight would have been a long climb back, so each lies where the two flights meet, and settles into the book of a reader who passes within three blocks. The page-turn that sounds near a reader's own leaf is as it was.

**A leaf can be read where it lies.** A reader who walked up to their leaf had usually bound it already, and the sheet then told them only that. Until its fire has taken it, the sheet opens.

**The dark holds at twelve blocks at every depth.** It no longer lifts with the fifth fire. At a camp whose fire is cold it closes in to about five blocks, as it did at the old hearths, and draws back when the fire is lit. The dark now moves from one distance to the next over a few seconds, both ways. Off the stair it is not drawn: in the chamber at the foot of the stair, which opens as it is walked into, and in the side mazes, which keep their own.

**Without the book, the first landing is as far as a reader gets.** A reader who walked past the shelf used to learn it at the first hearth, nine blocks down. The first camp is now 166 blocks down, so the dark stops them twelve steps past the first landing and says where the book is.

**Without a flame, a reader is told once.** The first hearth also used to be where a reader learned they needed a lighter. Just below the first landing, a reader who has their book and nothing to light a fire with is told so, while Tom is still near. It does not stop them: another reader may be carrying the flame.

## Lines

Changed, because the old ones spoke of flights or of the dark lifting:

| Where | Was | Is |
|---|---|---|
| The shelf, on giving the book | Your House of Leaves. Its five leaves are loose on the stairs below, one on each flight. | Your House of Leaves. Its five leaves are loose on the stairs below, one above each camp. |
| A hearth, with no leaf bound for it | Your House of Leaves has no unburned leaf. This flight's leaf is somewhere in the dark above. | Your House of Leaves has no unburned leaf. This camp's leaf lies on a landing not far above. |
| The fifth fire | The dark gives way. The whole staircase is there. | The last leaf burns. The dark keeps its distance. |
| At the edge of the dark | The stairs vanish into the dark. Bind this flight's leaf, then feed the fire. | The stairs vanish into the dark. Bind this camp's leaf, then feed the fire. |
| A sheet whose leaf has burned | You have already bound your leaf from this flight. | You have already bound this leaf. |

New:

| Where | Line |
|---|---|
| At the edge of the dark below the first landing, with no book | The stairs vanish into the dark. Your House of Leaves is on the camp shelf above. |
| Just below the first landing, once, to a reader with their book and no flame | You carry no flame. The fires below will need one; Tom has a lighter. |

Left alone, and now looser than they were:

- "A leaf burns. The next flight comes out of the dark." It still does: the dark draws back from about five blocks to twelve.
- The book shown on the shelf: "The light does not reach the bottom. It reaches the next fire." The next fire is now hundreds of blocks down.
- The advancement for the fifth fire: "The book is ash, and the stairs are lit."

## What does not change

The stair's height, treads, route and architecture. The shelf, the book, whose leaf burns where, and that only a reader's own story lights their fires. The leaks, the household's doors and their rhythm, the loose sheets, the wall lamps, the growl, the falling dust and masonry, the light let fall and the lean. The way up, which is still shorter than the way down. Witness and the endings.

Nothing saved is migrated: this is for new builds.

## How it is done

- `StaircaseFire.NIGHTS` names the camps' landings; `landings`, `braziers` and `edge` follow it, so the gate, the hearths and the tests that walk them moved together. The stair's corner landings are found once for an origin and kept.
- `StaircaseFire.reach` is the gate: `edge` for a reader with their book, the first landing's for one without.
- `StaircaseLeaves.LANDINGS` names the leaves' landings.
- `FinaleArchitecture` builds each camp with its landing (`camp`).
- The server sends the sight it wants (`StaircaseFire.SIGHT`, or `CLEAR` off the stair); `client.StaircaseDarkness` eases to it and stops drawing once it has opened to the well's own fog.

## Checks

- The stair's case for the leaves, rewritten: where the camps are and how far apart; each built with a cold hearth on a hearthstone and two bedrolls on the landing's floor, off the route; each leaf on its landing, within three landings of its camp, on no landing that holds one of the household's sheets, reachable once the fire before it burns and not before; no leaf or camp in reach of a reader without their book; where the dark is drawn.
- The gate's case, extended: a reader without their book is stopped below the first landing and put back on it; with one fire lit they stand at the second camp, more than four hundred blocks below, and are put back on it from beyond it; a reader with no flame is told once below the first landing and one with a flame is not; five fires open the whole descent; a fall to the cell awards nothing.
- The order case, changed where this release changes what it checked: a bound leaf's sheet opens again and binds nothing more, and a leaf its fire has taken no longer opens.
- An eye-level look at the first camp from five places, drawn from the stair's own plan: the hearth on its hearthstone in the landing's outer corner, a bedroll out toward each wall, the walk between the flights clear. One bedroll could not be told from the floor and was changed.
- The staircase's other cases, a compile and the quick checks.

## Not checked

Nobody has played it. The look at the camp was drawn fully lit, from the plan, with no fog: what a reader sees of it in twelve blocks of dark, a leaf on its landing and the dark easing were not looked at. How long the walk between camps feels is not something a case can say.

## For the owner

- **The first stretch is the shortest, and nothing in it asks for the book until the first camp.** A reader takes the book at the top, finds the first leaf about 114 blocks down and the first camp at 166.
- **A leaf binds itself to a reader who walks past.** I chose that over a leaf that must be looked for, because a missed leaf now costs a climb. If you want it looked for, the number is `StaircaseLeaves.NEAR`.
- **Nothing happens at a camp but the fire.** No sleep, no fade, no line that says a night passed. I held back from writing one; say if you want it.
- **The three lines listed as left alone** are yours to change or keep.
- **The climb home passes three of the five camps by.** A reader who climbs is still carried two turns further each time they complete a turn, and by my reading of that rule the camps on landings 56, 40 and 25 fall inside what is skipped. That matters for the people at the fires, and I will deal with it there: no reader will be carried past a camp on the way home.
- **The wall lamps are still there.** You have not said whether they stay.
- **A camp is a cold fire and two bedrolls.** They are carpet, two cells each, as the House's other camps make them. Say if a camp should have more.
- **The numbers** (which landings, three blocks, how fast the dark moves) are mine.
