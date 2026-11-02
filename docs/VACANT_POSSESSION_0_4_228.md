# Vacant Possession (0.4.228)

The owner, 1 November (`docs/PHYSICAL_HOUSE_PROPOSALS_2026_11_01.md`, proposal 2): a moving-out vignette with an older tenant who
fades as the flat empties; at the end the last chair, and carrying it out or putting it back, each with its own departure.

Slot 147, `LabyrinthPlace.VACANT_POSSESSION`, one visit, indoors, built whole (`VacantSite`). A new understanding story: Witness 71
sources / 54 required / two kinds / three endings. Layout 73, protocol 119 (the actor's fade is synced).

## The flat

- **The landing** (x -5 to 2, z -1 to -3): a runner, a bare bulb, a rail over the stair well. The flat's door stands open straight
  ahead in its double wall; the stair goes down on the left.
- **The flat** (x -2 to 7, z -6 to -17): faded sprig wallpaper, worn boards, plaster ceilings. The living room has the list taped up
  by the door (the source), one coat on a hook, the dresser, the rug, the armchair, the table, the lamp, the boxes, a radiator under
  the window, and the last chair by the window, facing it. Through the partition, the bedroom: the bed and the wardrobe. Both rooms'
  windows look east onto the street.
- **Downstairs**: the hall's lino, the mailboxes (the first floor's slot taped over), the front door standing open onto the pavement,
  and the back door at the far end of the hall (the way on).
- **The street**: pavement, kerb and road between the terrace and the houses opposite, two street lamps, the evening over it. The
  van is parked at the kerb with its back to the front door, its rear doors swung back against its sides, its load bay three by
  three, and the removal docket clipped to a board on its floor (the account).
- Drawn by `tools/draw_vacant_art.py` (54 textures; thirteen materials, one fitting block of 20 kinds, and the carried piece's item
  models). The tenant is painted by `tools/paint_character_skins.py` (`literary_vacant_tenant`, stranger phase 53).

## Moving day

The flat belongs to the flat, so readers who come in together empty it together; a reader on their own gets their own copy. It
starts again from the beginning when nobody has been in it for five seconds. Nothing in it can hurt.

1. **The tenant** waits by the window: "Oh, you came. It all goes down to the van, everything on the list. I can't manage the stairs
   with it any more." The chair can wait.
2. **Carrying.** A right-click on a piece lifts it into the reader's arms (an item drawn as the piece itself, in place of whatever
   they were holding, which comes back when the piece is set down). A right-click on any part of the van sets it down in the next
   free place in the load bay: the bed stood on end, the rug rolled, the lamp unplugged. A right-click where it stood puts it back.
   A piece that leaves the reader's arms any other way (dropped, or the reader leaving or dying) goes back where it stood.
3. **What each piece was hiding** is there when it goes: pencil lines up the wall behind the dresser, each with initials and a date;
   new boards under the rug; four worn places where the bed's legs stood; a brighter square of wallpaper where the wardrobe stood.
   Each can be looked at.
4. **The tenant fades.** Each piece set down in the van takes something of them: they are drawn more faintly (`FADE_EACH`, eight in a
   hundred a piece), and their words come shorter and fainter, white, then grey, then dark grey and broken off: "That's one. Mind
   the turn on the stairs." "The fourth stair creaks." "We had everyone round once." "You needn't be careful with that one." "It's
   going quicker than I thought." "Nearly..." "The light's..."
5. **The last chair.** When everything on the list is in the van, they are sitting in the last chair, facing the window (they go to
   it while nobody in the flat is looking). When the reader comes close they stand: "That's the last of it."
6. **The choice.** The chair can now be lifted.
   - **Carried down** and set in the van: the tenant is gone, the flat's lights go out, and from the street the window is dark
     (`carried_out_the_last_chair`).
   - **Put back** where it stood: they sit down in it again ("Thank you."), a little more there than before, and the light stays on
     (`put_the_chair_back`).
7. **The street is the ending.** With the choice made, stepping out onto the street finishes the story: the removal docket in the
   reader's hand, and the Witness line. Everyone in the flat when the choice is made goes down with it.

## The texts

- **The list** (source), in the tenant's hand: dresser, rug (roll it), bed, wardrobe (leave the hangers), table, armchair, boxes,
  lamp, and under a line on its own, chair. On the back: the letter says the flat is to be left with vacant possession; it means
  nobody in it.
- **The removal docket** (account): from the first-floor flat, to (blank), the pieces carried, and either "Premises left with vacant
  possession" or "One chair left in place at the tenant's request", carried by the reader. Then the reader's own page: what they
  found where the pieces stood, how the tenant faded, and which way the chair went.
- **Witness**: "Vacant possession", understanding.

## Tests

`VacantPossessionTests` (literary namespace), two cases sharing the emptying of the flat: built whole, indoors, one visit, an
understanding story with the quota derived; the list and docket placed; every piece where it stands, the boards new under the rug,
the van at the kerb; the list's text. The chair cannot be taken early; the dresser lifted shows its marks and is put back; a piece
taken from the reader's inventory goes back where it stood; every piece set down in the van with the bed's and wardrobe's traces
shown and the tenant fainter with each; the marks and new boards read; the tenant in the last chair, standing as the reader comes
close; the chair lifted. Then carried down (the tenant gone, the window dark, ready but not finished until the street, the docket in
hand) or put back (sitting again, the light on, the docket saying so).

`ArchitectureTests`: the flat keeps its boards, the list by the door, the dresser and the van; its source is the list; it is built
whole.

## 0.4.260: the van turned round, and weight

The owner's notes: the van "needs to be flipped around", placed objects must not "carry beyond the van", and carrying should have
some weight to it, with "a second or third player" able to help.

**The van.** It backs onto the kerb. The load bay is x 10..14, z -9..-5, with side panels at z -5 and -9 and a bulkhead at x 14; the
cab is beyond the bulkhead at x 15, a panel, a row of glass and a panel. The nine places a piece is set down (`VacantSite.SLOTS`) are
x 13, 12 and 11 by z -8, -7 and -6, front to back, so nothing set down reaches the cab or stands out of the bay. The rear doors are
plain panels with a locking rod, swung right round and hooked back against the sides at x 10 (z -4 and z -10), so the pavement at
x 9 stays clear. Four wheels (`VacantBlock.Kind.VAN_WHEEL`) sit outside the sides. Clicking any of it with a piece in hand sets the
piece down.

**Weight.** Each piece has a weight (`VacantPossession.weight`):

| Weight | Pieces | Carried alone |
|---|---|---|
| Heavy | bed, wardrobe | walking at 55%, jumping at 55%, no running, dragged along the floor |
| Medium | dresser, table, armchair | walking at 75%, jumping at 80% |
| Light | rug, boxes, lamp, chair | walking at 90% |

The weight is a set of transient attribute modifiers, recomputed every tick for each reader in the flat and removed from anyone who
is not in it, so a logout, a death or a door takes it away. Another reader in the flat with empty hands right-clicks the one carrying
a heavy or medium piece to take the other end, and a third can take the middle; with two pairs of hands each bears a third of what
one would alone, with three a sixth. Walking more than four blocks apart, picking up something of their own, or a second right-click
lets go, and both are told. Light pieces need nobody's help.

`VacantPossessionTests.aHeavyPieceIsSharedWithASecondPairOfHands`: the bed slows the lifter and lowers their jump; a helper with
bread in hand cannot take hold, with empty hands can; the weight is then a third each; the helper walking to the street lets go; set
down, the bed weighs nothing; the lamp needs nobody's help; and out of the flat the piece goes back with its weight.
