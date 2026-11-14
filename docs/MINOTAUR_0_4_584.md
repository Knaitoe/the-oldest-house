# The Minotaur rebuilt from many parts (0.4.584)

The owner, 11 November:

> "The minotaur texture should be improved as the Sphinx's was - use multiple blocks for its body to give it more granular
> detail. its size can increase."

New builds only. No line, protocol, layout or Witness change. It is the same creature: one entity with one UUID from the
caged boy to the end, still only wounded and alive in its cell, and all three endings are as they were.

## What was there

- **Thirty-eight boxes in twenty-four parts.** A chest, a waist and a slab of back; a skull with a brow and a snout; an arm
  of three boxes and a leg of three on each side; a horn of three lengths.
- **Its detail was all paint.** The 0.4.199 skin drew the face, the scars, the belt and the loincloth onto those boxes
  (`docs/OWNER_NOTES_0_4_199.md`). A scar on a flat chest is a scar on a board: from the side there was nothing there.
- **It stood 50 pixels to the top of its head** (3.1 blocks), its horns 66.

## What it is now

**186 boxes in 47 parts,** each a whole number of pixels each way, each with its own piece of one skin at one texel to a
model pixel. Nothing is stretched. What was painted is now built, and the paint goes on top of it.

- **The head** is carried low, against the chest and in front of the hump, as a bull carries it: almost no neck shows.
  - A skull, a poll of curled hair with three forelocks hanging off it, a heavy brow over each eye, cheeks, the bridge of
    the nose, a pale snout, and a dark wet nose pad that holds the nostrils.
  - The eyes are the ones the owner asked for in 0.4.199 ("Don't give it red eyes. It's not necessarily a monster." and
    "Try to make them look human in the way a cows eyes can look human"): large and dark, a lashed lid, white at the outer
    corner and a light caught in the brown. They stand a little proud of the face now, at its outer edges, where a bull's
    are.
  - Ears that hang and can flick. Upper teeth under the snout.
  - A jaw of its own with incisors, molars, a tongue, a chin and a beard. When the mouth opens there is a mouth in it.
  - Each horn is a ring at the root and seven lengths, each turned a little from the last: out, then up and forward. They
    are ivory at the root, yellowing, and near black at the point, ringed.
- **The body** is a pelvis, a waist with six muscles of the belly and a line of hair down the middle, the obliques beside
  them, a chest with two breasts (three old scars across them), ribs, lats, the yoke of the shoulders and the collar bones.
  - Over the shoulders is the hump in three masses, with the mane hanging off it in locks down the back and the sides.
  - The rope belt is rope, in four pieces round the hips with a knot at the front and its end hanging. The loincloth is two
    rags, front and back, with folds down them and a hem worn to threads. Each hangs plumb however far the body bends, and
    a thigh coming forward pushes the front one out.
- **Each arm** is a shoulder under a mantle of mane, a biceps and a triceps, an elbow, a forearm that is thick below the
  elbow and slimmer at the wrist, a palm, a thumb, and four fingers of two joints. Every finger and the thumb ends in a
  claw.
  - As in 0.4.199: furred upper arms, bare forearms with a raised vein, dark hands with pale claws.
- **Each leg** is a bull's hind leg: a hip, a thigh with the hamstring behind it, a kneecap, a gaskin and calf that go
  back to the hock, a cannon bone that comes down again with the fetlock's hair round it, a pastern, the two toes of a
  cloven hoof and two dewclaws.
  - As in 0.4.199: shaggy thighs, dark shins, split black hooves.
- **A tail** of three lengths and a tuft.
- **Hair ends.** A lock of the mane, the beard, the fetlocks, the tail's tuft and the rags' hems end in a ragged edge:
  those texels are clear and the model is drawn with them cut out, so no lock ends in a straight line.

### Its size

- **About a fifth taller.** The top of its head is at 59 pixels (3.7 blocks, was 3.1), the hump behind it at 61 (3.8).
  The horn tips are where they were, at 66 (4.1 blocks): the horns sweep forward now instead of straight up.
- **Broader:** 22 pixels across the chest (was 20), 24 across the yoke of the shoulders, 38 across the arms, 43 across
  the hands.
- **It still fits its cell.** The gate is three blocks wide and four high. Standing, only the horn tips are above four
  blocks, and it goes through the gate bent over in a charge, on its knees when wounded, or with its head down.
- **The body the game collides with** is 1.8 wide as before and 3.7 high (was 3.1), with its eyes at 3.1 (was 2.65).
  - The width is unchanged on purpose. The charge hits whatever its body touches, so a wider body would have made the
    charge harder to step out of.
  - The height is below four blocks, so it needs the same headroom as before wherever it walks: the cell, the gate, the
    chamber, the passage to the stair, and the stair.
- Its breath, when it bellows, comes out at the nostrils of the new head. Its shadow is larger.

### How it moves

The states and what they mean are unchanged. The poses were written for a body of three boxes a limb; they now use the
joints that are there.

- **Standing,** it breathes, its tail hangs and swings, and an ear flicks now and then.
- **Walking,** a leg coming forward folds at the hock and lifts the hoof; trailing, it straightens.
- **Before it comes,** it lowers its neck as well as its body, closes both hands, opens its mouth and paws the floor with
  one hoof, the tail lashing.
- **Charging,** it is bent right over, neck down, fists closed and forearms drawn up, the tail out behind it.
- **Staggered,** as before: head down and swaying, mouth open.
- **Wounded,** it is down on its knees and its knuckles with its head still up. Before, the whole figure was sunk nine
  pixels into the floor with its legs swung forward.
- **Loose** and **released,** as before.

## How it is kept

`tools/draw_minotaur.py` is the one place the model is written down, as a tree of parts and boxes with what each is made
of.

- It draws the skin (`textures/entity/minotaur.png`, 256 by 128; 101 pieces, of which the left side takes the right's,
  mirrored).
- It writes the parts into `client/MinotaurModel.java` between two marked lines, so a box and the piece of the picture it
  takes cannot drift apart. What is written by hand in that class is which parts move, and how.
- Each material is six tones, dark to light. A texel's tone follows which way its face looks, how far up the side it is,
  its edges, and a grain of at most one tone. A painter for a box (an eye, the nose, a scar, a hoof's wall) goes on top.
- `python3 tools/draw_minotaur.py --check` is in the workflow's quick gate: it fails if the picture or the parts in the
  class differ from what the tool would write.

## Checks

- **The tool's check** (above).
- **Sixty cases that involve it,** with the taller body: the finale's eleven, the collapse's three, the Witness ending's
  twenty-one (the released creature walks out of its cell, through the passage and up the stair), the sequence repairs'
  five, the notes repairs' ten (the charge, the shield, the wound), and the playtest commands' ten.
- **The game's own renderer,** in the two client proofs: its four attack poses (`native-minotaur.png`) and, in the
  character art sheets, standing, wounded and released.
- **The quick checks,** and a compile.

Not checked: nobody has fought it by hand since the change. The cases cover what the charge does and where the creature
can walk; they do not say how the fight feels against a larger figure.

## Caught while building

- **The first shape stood 70 pixels to its head** and 82 to its horns: too tall for its gate, and from the side its head
  hung a long way out on a long neck. The head now sits against the chest and the horns sweep forward.
- **From the side the chest was a board** twelve pixels deep under a head eighteen deep. It is fourteen, with the breasts
  in front of that.
- **The first hands were a cool grey** and read as gloves. They are a dark brown now, the hide's own colour.
- **A second pass gave it blunt dark nails** in place of claws. 0.4.199 settled on "dark hands with pale claws", so the
  claws are back, and with them the furred upper arms, bare forearms and dark shins that the same notes list.
- **The first kneeling pose stood on its hands** with its legs in the air: the legs were folded without bringing the hips
  down. The hips come down by what the folded legs need, and the forearms bend until the knuckles reach the floor.
- **One case could not be run through the selection namespace:** the charge case has a namespace of its own, and was run
  there.

## For the owner

- **The size is a choice.** It is a fifth taller, not twice. A larger one would not stand in its gate, and the body the
  charge hits would have had to grow with it. Say if it should be bigger still: the cell's gate would be the thing to
  change.
- **The look follows 0.4.199,** part for part. What is new to see is the head carried low against the chest (it stood on
  a neck before), the horns sweeping forward, and the hind legs' shape.
- **The loincloth and belt are now things,** not paint: the rags hang from the belt and stay plumb when it bends.
- **The caged boy** (the next note) and **the great staircase** are separate and not in this.
