# The quiet rooms after the owner's playtest — 0.4.161

The owner played the five quiet rooms and sent notes (October 29). This version answers every note. Each room is rebuilt in its own native materials, the reflection is a true mirror, and a test audits every room for the kind of fault the owner found.

## The owner's notes, and what changed

### Everywhere

> "Calendar placement is fucked up. It's instances like that I need you to check for." / "No custom textures. Blocks not connecting."

- **The cause of the floating decals.** Several wall fittings (the calendar, the light switch, the postcard) were modelled a block's depth away from the wall they hang on. Every wall fitting is now modelled against the back of its own cell and faces out of the wall.
- **The cause of the unjoined blocks.** The rooms are built without neighbour updates, which leaves fences, panes, stairs and gates in their unjoined shapes. A pass after each build now gives every block in the room its joined shape, one layer at a time.
- **No plain vanilla building blocks.** The rooms use 32 new native blocks drawn from 33 new 16-pixel textures (`tools/house_read_materials_art.py`):
  - carpet, ceiling tile, ceiling, counter;
  - asphalt and its painted line, the lot's brick wall, low cloud;
  - porch boards, shingles, trim, floorboards, wainscot, a night backdrop;
  - posts, dock posts, rails, pickets, dock rails, a gate;
  - window panes and a diamond lattice;
  - a panelled door, an office door and a boathouse door;
  - porch steps, shingle stairs, dock steps and rope.
  - Their blockstates follow vanilla's own fence, pane, stair, gate, slab and door states.
- **The rooms' own furniture.** 23 new fitted pieces (`tools/house_read_rooms160_art.py`):
  - tables, a placard, a wall clock, an outlet, ceiling panels;
  - a vanity, a two-part tub, a toilet, a towel bar, a vanity light, bath mats;
  - an empty hanger, a scarf and hat hook, a bench, pendants, a runner;
  - a porch sconce, a kitchen chair, a lamp, deck lanterns, a side table.
- **The audit.** `HouseReadAudit` walks every block of each room as it is built, in all five room tests, and fails on:
  - a wall fitting that is not flush with a solid face behind it;
  - a hanging light without a solid face above it;
  - any other fitting that floats or stands on nothing;
  - a block whose shape is not joined to its neighbours;
  - a plain vanilla block outside a short list of natural ground (grass, dirt, gravel, the lake's water, trees, a bed).

  It found one more fault: the shelter's dock lantern hung from nothing. It now hangs from an arm on the dock's corner post, out over the water.

### The waiting room

> "Custom textures for the table and sign. Ensure player can't drink from the empty cone of water. … Phone disappears after use. Or buries itself. The lever should receive a custom texture. Custom textures for the magazines relating to their contents. Space things out more, make the room less cluttered. Bad clock lol. … The waiting time should be shortened to something like 15 seconds … Add text and remove the player from the room - something like 'sorry for keeping you waiting'."

- **The layout.**
  - A reception partition: a counter, frosted glass and the office door, with the desk, lamp and calendar behind it.
  - A low table on the left with the glasses, the paper and the magazine, one to a place, and two armchairs facing it from the wall.
  - Ahead, a panelled column with the television cart parked against it, facing the armchairs, and the water cooler on its far side.
  - On the right, the phone chair by the window, the phone on a side table charging at its outlet, and one more chair.
  - Ceiling panels light the room; a rainy car park lies beyond the windows.
- **The table and the sign** have their own textures: a wood tabletop, and a printed reception notice on a stand. The notice and the bell stand on a ledge in front of the glass.
- **The clock** is a plain contemporary wall clock above the ledge.
- **An empty cone can't be drunk.** It reads "The cone is empty." and has no drinking animation.
- **The phone no longer vanishes.** Everything held in the hand is now a flat item sprite, not a block model; the phone was a block model that sank out of view in the hand. Taking the phone leaves its coiled charging cable on the side table.
- **The lever** is gone. The phone charges at a wall outlet instead.
- **The magazines' covers match their contents.** The paper has its own newsprint.
- **The wait is 15 seconds.** Then the office door unlocks, "“Sorry for keeping you waiting.”" appears in grey italics, and the House takes the reader on as they go through, or two seconds later if they don't.

### The bathroom

> "This is another room that looks awful. Poorly structured. Floating wall decal. The comb should be able to be picked up, or at least conclude the vignette. The player shouldn't be able to pick up/play with every comb."

- **The layout.**
  - Tile to shoulder height, wallpaper above.
  - A vanity across the back wall: the sink in the middle under its mirror and a bar light, the four routines on the counter two either side, and the postcard tucked at the mirror's edge.
  - The tub under a frosted window; the toilet and towel bar opposite; mats; a radiator by the door; a pendant.
  - The bedroom beyond its door: a turned-down bed, a nightstand and its lamp.
- **One routine item at a time.**
  - The reader picks one up from the counter and it leaves its place.
  - Picking another puts the first back.
  - Using the one in hand at the mirror or the sink is the routine. It settles the read, and then nothing else on the counter can be played with.
  - A second and a half later the light goes out by itself, and three seconds after that the House takes the reader on to bed.
  - The switch still works as before: turning the light off without touching anything opens the bedroom too.
- **On leaving,** the routine item goes back to its place on the counter, and the reader's own item goes back into their hand.
- **The account** says which item was reached for first and what was used at the mirror.

### The coatroom

> "Once again, there is too much open space. The clothes all look overly similar too. Random generic clutter rather than tailored decal. Mirror in closet doesn't provide accurate reflection."

- **Smaller.** A front hall seven blocks wide.
- **Tailored to a front hall.**
  - The west wall: an empty hanger, the three coats on hooks with their boots below, and a scarf and hat.
  - The east wall: a bench under the window and a radiator.
  - By the screen door: the umbrella stand.
  - The back wall: the hall glass with its towel bar, and the curtained way on.
  - A runner from door to curtain, and a pendant.
- **Three different coats**, hanging and worn:
  - a long charcoal wool overcoat with notched lapels and flap pockets;
  - a tan double-breasted trench with a belt, epaulettes and a storm flap;
  - a short olive barn jacket with a brown corduroy collar, snaps and patch pockets.
  - The boots differ too: tall brown lace-ups, black heeled ankle boots and tan work boots.
  - The worn layers have their own fitted textures, and the coat mesh gives every box its own region of the sheet; body and sleeves used to share pixels.
- **A true plane mirror** (`HouseReadReflection`).
  - Each frame, the room in front of the nearest glass in view is reflected through the glass's plane.
  - So are the people standing in front of it, at full size, with their clothes, held items and pose.
  - It is drawn into an offscreen picture with the ordinary camera, and the glass shows that picture pixel for pixel through a small shader (`house_read_mirror`).
  - The reader is still the House's version of themselves in it: the glasses an older read gives them, or no face where the glass did not agree.
  - The room is re-captured every half second, so a coat taken from its hook is missing from the reflection too.
  - The bathroom's mirror works the same way. A turned or towelled glass shows no reflection.

### The porch

> "No custom textures. Blocks not connecting."

- **New native materials** throughout:
  - porch boards, posts, rails and a lattice skirt under the porch;
  - clapboard and a brick foundation;
  - a gabled shingle roof with a ridge, and windows;
  - a panelled front door, a picket fence and gate, and a swing on painted posts and rope.
- **Every rail, picket, pane and gate joins its neighbours.**
- **The kitchen through the window:** a table with two chairs, the plate kept warm, a pendant and a lamp. The kitchen light and the sconce go out when supper ends, as before.

## Saved worlds

The quiet-room layout is now 161. A room a reader has had built but never entered is rebuilt in the new design the next time it is prepared. Every room's build first clears its whole area, so nothing of an older, larger room is left standing outside the new walls. Rooms a reader has already entered are left as they are.

## Tests and proofs

- **The audit** runs in each of the five room tests.
- **A new bathroom case** walks one item at a time: pick up, swap, use at the mirror, the light going out, the House taking the reader on, and everything back in its place.
- **Updated cases.**
  - The waiting room: the 15-second call and leaving.
  - The bathroom accounts, which now say what was used.
  - The coatroom's bench and hooks.
- **The connected-client tailoring proof** gains a fifth phase: both clients stand in a coatroom facing the glass, the glass must be drawing the reflection, and each client captures its window (`A-mirror.png`, `B-mirror.png`).
- **The native art proof** has nine object pages (20 images in all):
  - the waiting room, the bathroom, the shelter, the coatroom and the porch;
  - every new material, with fences and panes joined;
  - every held item as a flat sprite.
- **Counts.** The quiet-room job still runs 31 cases; the new bathroom case replaces the old routine case. The writing job is unchanged.

No layout, protocol or Witness change outside the quiet rooms: 54 / 41 / two kinds / three endings.
