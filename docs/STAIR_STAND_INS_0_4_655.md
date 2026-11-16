# Someone on the stair: the stories' own creatures (0.4.655)

0.4.654 gave five stories someone on the great staircase, in the cast's own bodies. Fourteen still had nobody, because their
people are some other kind of body than the cast's. This release gives nine of them someone: eight creatures, each drawn on the
stair by its own story's renderer (one elk stands for two stories). The last five, drawn as blocks or in a mirror and not as a
body, come next. No Witness, layout or protocol change.

## Who

| Story | Figure | Drawn as |
|---|---|---|
| Hide and clap | the girl from the wardrobe | the girl her own story draws, standing still |
| Drowned Town | Stacey Graves, as she hunts | the lake witch low on her hands and feet, stalking |
| Preserved cave | the preacher | the congregation's preacher, on his feet |
| Shallows | the girl as she was before the lake | the Shallows' girl, standing |
| Goatman | the cousin who was one too many, eyes lit | a cousin in the face it wore in the reader's own night |
| Séance | the mother from the table | the séance's mother, standing as at the table |
| Hotel | the bartender | the Stanley's bartender |
| Last Stop, and the fan room | the elk, standing and looking | the elk, its eyes lit as the herd's are in the field |

As with every figure since 0.4.596, each reader's people are their own: nobody else sees them, none of them speaks or can be
touched, they move only while unseen, and nothing is saved of them. They stand only for a reader who heard their story to the
end.

## How a creature stands on the stair

- **One of the cast's bodies, standing in.** The body set down on the stair is still one of the cast's (`LiteraryActor`), in a
  role of its own, the stand-in (`LiteraryActor.STAND_IN`, `StairStandIn`). It is set down, kept for one reader, looked for and
  moved exactly as every other figure there is. Its phase says which creature it stands in for, and how that one is dressed.
- **The creature's size.** The body is as broad and as tall as the creature's own story makes it, with its eyes where the
  creature's are: Stacey low, the cousin a child's height, the elk broad, the girl from the wardrobe small. The looks that keep a
  figure still are aimed at that body, so they aim at the creature and not at a person's height of air above it. A figure not yet
  set down is measured the same way (`StairCompany.Figure.height`).
- **Drawn by its own story's renderer.** The reader's game makes the creature the first time the figure is drawn, as its own
  story makes it, and never adds it to the world: nothing ticks it, hears it or bumps into it, and it goes when the figure goes.
  Each time the figure is drawn, the creature is drawn in its place and turned as it is turned, by the renderer its own story
  uses (`client.StairStandIns`). On the way down, its shape is drawn pale and dim as every other figure's is.
- **Lit as its own renderer lights it.** The girl from the wardrobe gives her own light, as she did in that room, so she shows
  in the stair's dark; it lights nothing round her. The rest are lit by whatever light is where they stand. The cousin is the one exception. In the trailer's window its renderer lights it at least
  as much as the trailer's spill would, and on the stair there is no trailer, so it stands in the stair's own dark with only its
  eyes lit (`GoatmanChildRenderer`, while `StairStandIns.drawing`).

## Each creature

### The girl from the wardrobe

- Her own story draws her walking in place, her legs swinging. On the stair her renderer is given no passing time, so she stands
  still.

### Stacey Graves, as she hunts

- Low on her hands and feet, stalking, as she hunts the town: the hunting body, not the girl. The small hitches of her head go
  on.

### The preacher

- The congregation's preacher, on his feet. His renderer eases him between sitting and standing as his body ticks, and nothing
  ticks a creature on the stair, so it is set to his pose at once (`LakeCongregantEntity.settle`).

### The girl as she was before the lake

- The Shallows has her lying, carried, thrown, broken, rising and running on all fours, never standing. On the stair she
  stands, in her own body and the skin the Shallows gives her, as nobody at the lake ever saw her: a phase never used there
  (`Shallows.STOOD`), which her body draws as the girl on her feet.

### The cousin who was one too many, eyes lit

- **Dressed for each reader.** From 0.4.655 the Goatman's dawn keeps, in the record of each reader who counted right and sat
  the whole night, the face the one too many wore that night (`GoatmanVignette.EXTRA_FACE`). The figure wears that face, so two
  readers who sat different nights see different faces on the same stair.
- A reader who finished before 0.4.655 has none kept, and is shown one of the eight faces the cousins are dealt, the same one
  every time for that reader. Nothing is written by asking.
- Out of doors, as it is at the window, so its eyes are lit; a child's size, as the cousins are (`GoatmanChild.SIZE`).

### The mother from the table

- Standing as the family stands at the table (0.4.117), her hands where the table would be. There is no table on the stair.

### The bartender

- The Stanley's bartender, at rest behind nothing.

### The elk, standing and looking

- One elk for the Last Stop and the fan room, as one man with a blade stands for Camp Blood and the carcasses.
- The reader's own, as the herd in the field beyond the Last Stop is, so its eyes are lit as theirs are.

## Not on the stair yet

Five stories, whose people are drawn as blocks or in a mirror and not as a body. They come next.

| Story | Figure on the list |
|---|---|
| Backseat | the one from the back seat |
| Third Floor Girls' | the woman from the mirror |
| Work print | the figure in sacking |
| Left-Right Game | Marjorie from the salt |
| Dead Air | the Skin-Taker |

## Checked

- `StaircaseAccessTests.theStoriesOwnCreaturesStandOnTheStairAtTheirOwnSize` (new): each of the eight creatures stands for one
  row of the list, and those rows are the nine stories'; only the five drawn as blocks or in a mirror are still waiting; the
  cousin is shown the face kept for its reader, and a reader with none kept is shown one of the eight faces, the same each time,
  with nothing written; each creature's size is its own story's; set down, each is that size, its reader's alone, and nobody to
  push, hurt or talk to.
- `GoatmanTests.aNightDoneRightCountsLetsTheCousinInKeepsItOutAndEndsAnOldHaunting`: each child who counted right keeps the face
  the one too many wore.
- `StaircaseAccessTests.everyFigureKeepsItsPlaceWhileTheServerRuns`: all sixty-one figures now (fifty-three before). In
  `aReadersPeopleStandAtTheirFiresAndComeAfterThemUnseen` a reader with every story heard has all sixty-one come after them, no
  two in one place. The case that hears five stories and finds three people hears the Mother of Strays where it heard Hide and
  clap, which has someone now.
- The fifteen staircase cases and the Goatman cases; build, package, branding and quick checks; architecture, writing, cast and
  the client proof.
- Review frames of each creature drawn through the cast's own renderer, from the front, three-quarters and the side, and in a
  row standing in a world.

## Not covered

- The client proof CI runs draws no world, and a creature can only be made in one, so CI draws none of these. The review frames
  were made locally, in a world.
- The frames show each creature lit and by day: not on the stair in the dark, and not as a shape on the way down.
- Nobody has walked the stair with these figures.

## New lines

None.
