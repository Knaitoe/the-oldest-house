# Borrasca: the children's bikes (0.4.431)

Continuing the town's detailing (the owner: "i hope you're still working on detailing the architecture"). The children's talk has
always been full of bikes: Patrick says "If my bike's still at the store, I'm up by the second marker", Kendra tells the reader to
watch out for it, his poster says it was found behind the store, and later she says she walked it back to his mother. Until now
there was no bike anywhere.

- A new shop fitting, `BorrascaShopBlock` `BIKE`, a child's bicycle built from bars a pixel or two thick: two spoked wheels, the
  frame, the seat, the bars and grips, the pedals and a kickstand. Stage 0 stands on its kickstand; stage 1 is the same bike dropped
  on its side, its near grip and pedal down in the grass. It runs past its block at both ends, and nothing bumps into it.
- **First visit.** Patrick's bike lies on its side outside the general store, just off the porch, where anybody could trip on it.
  Kendra's stands on its kickstand by where she waits.
- **Second visit.** Patrick's is gone: Kendra has walked it home to his mother. Hers still stands where she waits.
- **Grown visits.** Neither is in town.
- Each has a line when looked at: Patrick's has a baseball card pinned to the fork so the spokes clatter; Kendra's chain shines with
  fresh oil (Dana taught her how to put it back on).

`BorrascaStory.townBike` sets them once per visit (`TownBikeVisit`), like the missing posters. The posters' test now checks both
bikes on every visit.

## Numbers

No protocol, Witness or layout change: protocol 173, Witness 86/65/63, layout 135. New builds only.
