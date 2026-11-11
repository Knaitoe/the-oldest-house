# Borrasca: the front of Drisking General (0.4.451)

The owner's notes this answers (docs/BORRASCA_PLAYTEST_2026_11_10.md):

- 21, of the two LOST posters by the store's door: "try to add the faces of the actual children, especially if they're npcs from
  the vignette"
- 22, of the store's noticeboard: "Cluttered"
- 43, of the whole front of the store: "Have this whole front of house filled with lost papers after vignette has ended – after
  the orrery has either been fed or stopped."

## How the notes were read

- **21** is plain. The poster was one picture for every child: the word, and a grey outline of a head. Its L was also cut off at
  the picture's edge, so it read "_OST".
- **22** is one word under a picture of the board, which was four copies of one small tile, each with the same three papers. It
  is read as "make it a cluttered board". If it was meant the other way, that the front is too busy, the board is the thing to
  thin: it is one picture for each stage in the tool.
- **43**: the machine is fed when Dana goes into it, and stopped when the breaker is thrown. Either is the ending for this.

## What is on the front now

Each reader's Drisking is their own, so the front follows that reader's own visits and nobody else's.

### The sheets by the door

- **From the second visit**, north of the door: LOST, and Patrick's photograph. It is his own face, read off the skin the game
  draws him with, two of its pixels to one: his hair, his skin, his eyes, his mouth.
- **From the third visit**, south of the door: Kendra's, made the same way.
- **On the last night** both have gone yellow, and a corner is off Patrick's.

A hand on either still opens its pages, as before.

### The noticeboard

One board, two blocks by two, in a dark frame, under what the town pins to it. What is on it is what its pages say on that visit:

- **In childhood**: the church supper, the lost dog with its strip of numbers to tear off, the curfew notice with the Sheriff's
  star, Prescott's night work, Dana's sheet sun-faded with a bake sale flyer pinned over half of it, and behind the curfew notice
  the corner of a much older sheet, brown, with a school photograph on it. Dana's photograph is her own face.
- **Years on**: the supper again, a bicycle for sale, Prescott's contract renewed with a stamp on it, and Kelly's sheet, pale,
  CLOSED across the bottom in marker. Her photograph is her own face as a child. Dana's is most of the way under newer paper.
- **The last night**: the supper struck through, a storm forecast, the night shift, and Kayla's new white sheet over Kelly's old
  one, with the pin holes of sheets nobody kept in the cork beside them.

### After the ending

Once the machine has been fed or stopped, the next time the reader is in town the whole front is papered over: a LOST sheet on
every board of the wall from the deck to the porch roof and from one end of the porch to the other, on the glass of both windows,
and over the noticeboard, frame and all. Forty-two sheets and the board. The doorway is clear, and the store's tin sign and its
bench are left as they are.

- Each square is one sheet over the corners and edges of older ones, so no wall shows between them. Fourteen different squares
  are dealt along the front so that none is beside itself.
- Patrick's and Kendra's are where their sheets were, yellowed, under the rest, and still open their pages.
- Dana's, Kelly's and Kayla's are among them, with their own faces (Kayla is not one of the game's people; hers is drawn).
- The rest have no name left: brown paper and school photographs, one bleached out, one gone over in pencil, one all staple
  holes, one with its photograph cut out with a blade, one torn off below the word.
- On the windows the sheets lie on the glass, not in front of it. From inside the store their backs show through the panes.
- A hand on a sheet reads it in a line. The board opens as new pages of its own.

## What a hand reads

| Sheet | Line |
|---|---|
| Patrick's | LOST. Patrick Maddox. Last seen at Marker Two. |
| Kendra's | LOST. Kendra Bell. Last seen on the ridge road. |
| Kelly's | LOST. Kelly Garrity. CLOSED is written across the bottom in marker. |
| Dana's | LOST. Dana Pruitt. The sun has taken most of it. |
| Kayla's | LOST. Kayla. The paper is new. |
| a school photograph | LOST. A school photograph. The name has worn away. |
| a boy | LOST. A boy squinting at whoever held the camera. The corner with his name on it is gone. |
| gone over in pencil | LOST. Someone went over the name in pencil when the ink faded. The pencil has faded too. |
| staple holes | LOST. The corners are all staple holes. It has been taken down and put back up more than once. |
| bleached | LOST. The photograph has bleached into the paper. The telephone number under it has four figures. |
| cut | LOST, and a name. The photograph has been cut out with a blade. |
| torn | The top of a sheet: LOST, and two staples. |
| two | Two sheets on the same staples. LOST, twice. |
| corners | Corners. There are more sheets under these than show. |

The covered board's pages:

> DRISKING
>
> Every sheet on the board says LOST. They are pinned across the frame and across each other.

> The pins ran out. The newest are tucked under the corners of the ones before.

> On top, Kayla.
> Under her, Kelly.
> Under Kelly, Dana.
>
> Under Dana the paper is brown, and the photographs are school photographs.

## How it is made

- `borrasca_paper` (`BorrascaPaperBlock`): a face with no thickness, a hair off the boards behind it. Its `piece` is which square
  of which picture; `glass` is a sheet on a window, which lies on the pane in the block behind it, half a block further back than
  a wall's boards, and has a back to be seen through the glass.
- `BorrascaPapers`: the table of pictures (`Sheet`), the five stages of the front, and what is hung at each. A stage is hung
  once, when the reader is in town and the front is loaded, and is never taken down. `BorrascaStory.frontStage` says which stage
  a reader is at; the saved `TownPostersVisit` is the stage last hung.
- A sheet is hung in a cell of the front only where the cell is empty and boards or a pane stand behind it, so nothing is hung
  on the door, over the tin sign or through the bench.
- `tools/draw_borrasca_papers.py` draws the 36 pictures, sixteen pixels to the block, and writes the 48 models and the block
  state. It reads the four faces off `textures/entity/borrasca_eli.png`, `borrasca_wren.png`, `borrasca_kimber.png` and
  `borrasca_dana.png`, so a face repainted there is repainted on its sheet the next time the tool is run. Its `--check` runs in
  the quick gate and compares its table with `BorrascaPapers.Sheet`.
- No layout, protocol or Witness change. New builds only.

## Tests and proof

- `BorrascaTests.townChildrenBecomeReadableMissingPosters` now runs all four visits and the ending. It checks the board's four
  quarters on each visit; that Patrick's sheet hangs only once he is gone and Kendra's only once she is; both yellowed on the last
  night with nothing else on the front; that the front is papered once the machine has been fed or stopped, whichever, and not
  before; then, cell by cell, that a sheet hangs on every board and pane of the front, each held by what is behind it, none beside
  itself, the board covered, the doorway clear; that a hand on a sheet reads it without opening pages; and that the covered board
  and the two sheets by the door still open theirs.
- The Borrasca client proof has one more view (22): the sheets, the four boards, the papered front as the game deals it, and two
  window sheets from behind the glass, each block drawn by the game.

## Not done here

- The same old poster, with its L cut off, still hangs in Proofrock and at Indian Lake High (`town_fixture`'s poster). Those are
  other towns with no faces of their own to give it; it is on the list for after Borrasca.
