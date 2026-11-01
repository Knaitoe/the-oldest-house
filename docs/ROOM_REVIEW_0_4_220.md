# Room review: the Ghosts set, the Winchester, Work print and the Whistlers (0.4.220)

The owner's screenshots on 1 November: a door over a window, the same coat rack twice, Barry looking away from a blank
monitor, a kitchen with no appliances, a bathroom far too large, twin dressers, a jug in a wall, a recycled playmat; in the
Winchester a clock standing out from the wall, a "random small dresser" and vanilla chairs ("i really need you to review the
furniture placement to ensure accuracy to environment"); at the video shop a till facing the wrong way, a camcorder that filled
the screen, shelves that did not read as tapes; in the Whistlers disconnected banisters and a stair that did not seem to reach
its opening; and, everywhere, lines running off the screen. "I really feel like you're not reviewing these."

Each room below was rebuilt from a first-person render at standing height (a local review tool exports a scene's blocks and
cast and draws them from cameras placed where a reader stands), and checked again after the change.

## Everywhere

- **Long lines wrap.** The action bar drew one line however long it was. A line wider than the screen allows is now wrapped
  and stacked upward from where the action bar sits, with the same fade, colour and backdrop (`ActionBarWrapMixin`). Short
  lines are untouched.
- **Rails join.** Builders lay fences, walls and panes with known shapes, so a rail placed before its neighbour never joined
  it; that is where the disconnected banisters kept coming from. `BuildBlocks` now records every fence, wall and pane a build
  lays and, as the build's last step, gives each the connections vanilla would against what stands beside it. Iron bars keep
  the shape they were placed with (pipes and grilles are drawn by hand), and a fence under a carpet or pressure plate is left as
  a table leg.

## The Ghosts set

- The onward door no longer stands over the dining room's window; it is in the back wall of the house, clear of every window.
- The hall has two coat racks with different coats on them, and a runner down it.
- Barry sits facing the monitor, and the monitor shows the feed from the crew's camera (a lit field monitor, not a dark TV).
- The kitchen has its appliances: a range under its hood, a dishwasher, wall cabinets, a coffee maker, a microwave and a
  toaster. The dining room has a sideboard.
- The girls' rooms no longer share furniture: Marjorie has a chest of drawers with her books, a bedside table with her hat and
  a shag rug; Merry has her own chest of drawers, her drawing moved to the wall by the bookcase, her jug on the bookcase instead
  of set into the wall, and a round night-sky rug (a moon, Saturn and stars) drawn for her alone, not the nursery playmat used
  elsewhere. Both rooms' windows moved off the furniture.
- The family bathroom is four by five, with the tub, toilet, vanity, towel bar, hamper and a print on the wall.
- The call sheet (the source) lies on the floor by the crew's folding table in the den.
- The basement holds covered furniture, boxes, shelves and the laundry.
- The family's lines no longer say "nobody will say whose" and the like; it is not the reader's home. New fittings have their
  own lines.

## The Winchester

- **The hall clock** stands against the hall's east wall, and its hands move round its dial of thirteen hours (a block entity
  renderer over the case's face; the painted hands are gone from the dial).
- **Furniture of the house's own**, drawn by `tools/draw_winchester.py`: balloon-back dining chairs in oxblood velvet, a long
  table (two ends, its pedestal in the middle, a lace runner), candelabra, a mirrored buffet, a button-back settee, wing chairs
  in bottle-green velvet, lamp tables with their oil lamps, an upright piano, fern stands, a hall stand with its mirror, a hall
  console, a treadle sewing machine, a dress form, gas sconces, a Persian rug and runners, and a coffered ceiling.
- **Placed for the rooms.** The hall: the clock on the east wall, the ledger on its console against the west wall (it stood on
  a bedside table in the middle of the floor: the "random small dresser"), the hall stand by the front door and a fern across
  from it, a runner, sconces. The parlour is sat round its hearth: a rug before it, the settee facing it with a lamp table at
  its end, a wing chair either side, the piano against the south wall with its chair, a reading corner under the spider-web
  window, ferns in the corners, sconces either side of the chimney breast. The dining room: one long table with ten chairs set
  to it and two candelabra, the buffet against the north wall between sconces (it was nine writing desks in a row, cane chairs,
  and a chest of drawers by itself in the middle of the floor). Runners down the spine and the passage, sconces along the
  spine. The sewing room: her desk and chair, the treadle machine against the west wall with a chair at it, the dress form. The
  last room's chair matches the rest.
- The rooms were cut out of a block of wallpaper, so every ceiling was papered; each room now has a coffered ceiling, laid only
  where the paper still was.

## Work print

- The till's keys and drawer face the clerk and its display faces the customer; a right-click rings "no sale" and opens or
  shuts the drawer.
- The counter's front is plain laminate (a sticker on every panel read as a row of drawer labels); the clerk's side has open
  shelves with returned tapes, the bag of slips and a roll of receipt paper.
- The shelves are rows of face-out boxes on lipped shelves: VHS covers with a title band, an orange sticker and one of six
  kinds of cover art (a face, a figure against a lit ground, a moon over a horizon, a drip from the title, a house with one lit
  window, a knife), and DVD cases with their blue band.
- Raising the camcorder hides the hand and shows a viewfinder (corner marks, the centre cross, a blinking REC and the tape
  counter), a little zoomed.
- A reader carrying a blank cannot take another from the box.

## The Whistlers

- Rail posts stand two high where the next tread is a step up, so each meets the next at the height they share and the rail
  runs on unbroken up the flights (with the shared join above).
- Each landing's lantern hangs from a beam over the landing's outer rail, beside the way, not over the turn in front of the
  reader's face.
- The cab's floor hatch stands open against the south wall, so the stair visibly comes up into the cab through its railed
  opening.

## God of the Pitt

- Brad was a figure built out of blocks, and his hard hat was a block. He is now painted like the rest of the cast
  (`literary_pitt_brad`, by the common painter): a clay-stained orange oversuit, the harness over it, a short beard and a yellow
  caving helmet with the lamp on its front. He sits on his kit box at the belay, one Brad for everyone in the cave, there exactly
  while the box is (gone from the third visit, and while a second reader holds the belay in his place).
- His lamp still carries the draft: it flashes white twice while the draft blows and strobes amber when it is about to die. The
  glow is drawn over the helmet's lamp (a glow mask the painter draws with him), and the light is real, a light block at his head.
- Speaking to him, or to his box, gives the same lines as before.

## Not changed

No layout, protocol or Witness change; no vignette added. New builds only.

## Tests

`GhostsSetTests` checks the call sheet on the floor by the folding table, the field monitor, the range, the second coat rack,
the sideboard, Merry's rug and that no fitting is left in the wall where the jug was. The Winchester case's checks follow the
clock and ledger to their new places. The scenes' existing architecture, Witness and route cases pass unchanged, including the
cases that check pen and yard rails join their neighbours. `PittTests` checks that Brad sits on his box as one unowned actor on the first visit and is gone with it on the third.
