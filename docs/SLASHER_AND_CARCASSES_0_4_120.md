# The elk carcasses: the way in, the crew's machines, the killer at work, the shove and the drag, 0.4.120

The fourth batch of the owner's October 24 notes, with the textures rendered and reviewed as they were drawn. No layout,
protocol or Witness change: 52 sources / 39 resolutions / two kinds / three endings. The resolution is unchanged: hold
still under the hides, watch his boots go by, leave by the crew's gate.

## The way into the cave

The mouth's level pad stood two blocks above the ground in front of it, so a reader could not jump in and had to climb
the rock round it (and cannot build or break). The pad now eases down to the wood a block at a time (its blend widened
from two blocks to four and a half), and the trees keep back from that ramp. The trail walks up to the mouth with no
step higher than one block and two blocks of headroom; the mouth is level with the ground in front of it and open three
high. ArchitectureTests walks the trail's own line to the mouth and checks every step.

## The crew's machines

The skid steer (concrete and terracotta) and the flatbed (concrete, planks and fences) are replaced by one
`site_machine` block: each cell of either machine is one kind (61 in all), turned by `facing`.
`tools/draw_site_machines.py` authors each machine whole, in pixels, facing north, and cuts it into one native model per
cell, so paint, steel and planks run on unbroken from cell to cell; fitted faces (wheels, the cab's glass and doors, the
grille, the tail lamps, the headache rack's mesh) come from one decal sheet per machine at one texel per pixel. Every
cell's box is the box round its own pieces; `SiteMachineBlock` carries the same boxes and the tool's `--check` (in CI)
confirms them.

- **Skid steer**, bucket down with a load of mud in it: worn yellow paint with mud thrown up the sides, black chassis,
  four lugged tyres on pressed rims, lift arms down to the bucket, a cage cab with a glass front door, an engine door
  with louvres and tail lamps, an exhaust stack.
- **Flatbed**, nose to the road and tail to the trail: municipal orange cab with a grille and square lamps, windshield
  and wipers, mirrors, an amber beacon, a round department seal on the doors (no name), an expanded-metal headache rack,
  a weathered plank deck with stake pockets and galvanised stakes, a fuel tank and toolbox, dual rear wheels and mud
  flaps. Two carcasses still lie on the deck where it is stained, with smears running off the back toward the drag
  marks.

Texture review: rendered from four sides with a z-buffered model preview. The first pass's orange paint had pale
scratches that read as snow at a distance (now one dark primer scratch a tile), the door's crest read as a stray
sticker (now a small round seal), the chrome bumper was too stark (now galvanised), and the skid steer's cage bars
vanished into its dark glass (now mid-grey).

## The killer

- **How he moves.** He carries himself low and heavy and makes no sound of his own. Walking, long planted steps with
  the axe hanging low and the head turning; running, a lunge with the axe raised back and the free hand reaching;
  standing, a slow breathing and now and then a sharp tilt of the head.
- **The blow.** The axe goes up in both hands for half a second, then comes down in a two-handed chop. Blows stay a
  saved 90 ticks apart; the falling blow reaches a little further than he had to be to start it.
- **Pushing him off.** A reader's blow cannot hurt him, but it shoves him: a real knockback away from them, a stagger
  that stops a raised axe, and a moment before he can swing again. He can be rocked like this once every two and a half
  seconds; between times a blow only moves him a little. He knows where they are afterwards.
- **His work.** When no one is in his sight he goes back to the dead between legs of his round: he stands over a
  carcass and hacks at it (behind the flatbed, and at two he left half-dressed beside the trail), or takes one by a hind
  leg and hauls it down the trail into the cave. Bent over his work he sees only what is close and in front of him. He
  starts a haul only while nobody can see him take it up. The hauled carcass is drawn behind his hand; the shared
  carcasses never move. The chops, the wet sounds and the blood are the reader's alone.

## The drag

His killing blow does not kill, and nothing records a death. The reader's sight goes in pulses; they come
to on their back, dragged by the ankle across the mouth's pad and down the passage into the cave, the ground jolting
under them on every heave and their sight lapsing; his back and boots are above them and now and then he looks back
over his shoulder. It goes dark again, and they wake inside the pile in the hollow, with three hearts, facing the gap.

The gap and the cell beyond it are packed with carcasses only that reader sees (sent to their client; the shared pile is
not touched). Each takes a few blows to tear loose (one, if the reader's own tool breaks it out first); a way through is
open once one side is clear. He walks away out of the cave and hunts no one for ten seconds; after that the noise of the
blows carries and he is hunting them again. The drag can happen again.

The reader's body waits in the pile from the first moment; what they see is a camera of their own carried along the
drag, with their own killer pulling ahead of it, and nothing reaches them while it runs. The drag's clock is kept in
memory; a save or a departure in the middle of it wakes the reader in the pile. No death is recorded.

## Tests

- ArchitectureTests: the walk up the trail into the mouth; every cell of both machines; the carcasses on the deck and
  beside the trail; standing room at every place he works.
- `literary_elk_shove`: a real blow rocks him back, stops the raised axe and does him no harm; a second straight after
  only nudges him; he comes on again.
- `literary_elk_drag`: the killing blow starts the drag; he walks the drag's line ahead of the reader's own camera;
  nothing reaches them; they wake in the pile with the gap packed; blows and a broken-out carcass open a way; out of the
  pile, the private carcasses go.
- `literary_elk_work`: with no one in sight he hacks at a carcass, then hauls another; the shared carcasses stay.
- The client proof draws all ten of his phases, the hauled carcass included.
