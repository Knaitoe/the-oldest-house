# The household on the stairs (0.4.214): the leak review, the doors and four of the household's rooms

The owner, 1 November: "You should review and revise the existing leak vignettes too." This pass:
- reviews the five leaks (docs/STAIRCASE_LEAKS_DESIGN.md, 0.4.49);
- lays the frame the household vignettes (docs/STAIRCASE_HOUSEHOLD_PROPOSAL.md) are built on;
- builds the first four of them: Sunday lunch, Fresh sheets, Mending day and First light.

The long way home, Washing day, the lay-by and the last landing follow.

No protocol change. Layout 69 (the staircase is built fresh in new worlds only).

## What the review found

1. **Most of the sheets are never on the way down.**
   - The staircase has 49 loose sheets, one at every third corner, about 52 blocks apart.
   - Only 24 lie above the cell, where readers walk: sheets 0 to 22, and the Panther in place of 23.
   - The other 25 lie in the continuation below the cell, which readers rarely walk.
   - So *Before bed* (32), the fifth leak, was almost never found, and neither were the sixteen stair sheets (48 to 63) that end
     the household's story. *Sunday lunch* (23) was never found at all: the Panther took its place.
2. **The leaks had no rhythm.**
   - Four of the five came in the first 700 blocks: sheets 0, 6, 9 and 12.
   - After that, nothing came for the rest of the descent.
3. **The household was anonymous in its own rooms.**
   - The sheets name Ruth, Jonah, Mara and Bell.
   - The rooms said "From the landing", "a voice", "the driver".
4. **Outside the windows there was a box.**
   - The rooms stood in a deepslate box with a moss floor. Through the glass a reader saw tiled stone four blocks away.
5. **Nothing remembered a leak.**
   - A finished room set one flag. The household's story (docs/STAIRCASE_HOUSEHOLD_PROPOSAL.md) needs the leaks to count.

## What changes

### The descent's sheets are chosen (`StaircaseNotes.DESCENT`)

The 23 sheets above the cell now come in a set order (the Panther still takes the last place above the chamber). The descent is
split into eight stretches of 160 blocks, three sheets to a stretch:

| Stretch | Sheets | What they can open |
|---|---|---|
| 1 (top) | A blue sock, Six o'clock, The cupboard | nothing: the shaft's own dark and the first fire come first |
| 2 | The last plate, Breakfast, On the way home | leak: kitchen; doors: First light, (the long way home) |
| 3 | Saturday washing, The good cups, Thursday | leak: laundry; doors: Sunday lunch, Mending day |
| 4 | The passenger seat, The airing cupboard, After the rain | leak: car; doors: Fresh sheets, (washing day) |
| 5 | A small repair, A longer way, For tomorrow | leak: front room; doors: (the lay-by), First light |
| 6 | Before bed, The table, At the lights | leak: late kitchen; doors: Sunday lunch, (the long way home) |
| 7 | Fresh sheets, The hem, The blanket | doors: Fresh sheets, Mending day, (washing day) |

Doors in brackets open once their vignettes are built; until then those sheets are only reading.
| 8 (bottom) | The landing, Our supper | the household's last landing |

- The house sheets come first, then the stair sheets at the bottom: the household went down before the reader.
- Below the cell, the continuation carries the rest in their old order.
- Saved originals keep their words. A sheet already read keeps its text.

### One rhythm for leaks and doors (`StaircaseHousehold`)

Each reader's descent has its own rhythm:
- At most one household thing per stretch: a leak or a door.
- A stretch offers nothing straight after one that did.
- Stretch 1 offers nothing.
- Stretch 8 holds the landing only.
- No doors and no leaks on the homeward climb.

A descent begins each time the reader comes in through the staircase's entry. The record keeps what each stretch held.

A leak is now spent only by the room itself, and counts in the household's tally with a door's vignette.

### The household is named

The leaks' captions now speak as the sheets do:
- **Kitchen:** Ruth's towel and Ruth's place for the plate.
- **Laundry:** Ruth's voice from the landing with the matches.
- **Car:** Mara's voice from the empty driver's seat about the pear tree.
- **Front room:** Jonah's candle.
- **Late kitchen:** Jonah, at the doorway, about the little light.

### A street outside

The rooms now stand in a street at night instead of a box:
- black above and around;
- terraced backs across the yard, with lit and unlit windows and slate roofs;
- houses across the road at the front;
- a street lamp by the pear tree's stump.

The geometry the chores use is unchanged.

### The household door

A door's source sheet, read to its last line in an eligible stretch, leaves a door beside that landing (`StaircaseHousehold`,
`HouseholdDoorBlock`, art by `tools/draw_household_art.py`):
- an ordinary painted door with a brass knob, set into the shaft wall;
- a doorstep and a short floor of household boards out to it from the landing;
- a gap in the landing's rail for it.

The door is its reader's; anyone else is told "It isn't yours".

- Two readers who read sheets at the same landing each own the door. Each goes through into their own room: the one their own
  sheet opens.
- Going through takes the reader into the vignette's pocket room by the leak engine's fade, with the same saved return
  (inventory, health and pose restored, back on the doorstep).
- The door goes back to stone once it has been used and its reader has stepped off the boards, or once nobody it belongs to is
  still on that descent. Every block it changed is restored as it was.
- **How a visit ends:**
  - walking out of the room's door, or running out of time, is a failure;
  - a blow, a death or a disconnect is not a failure, and leaves the door for that descent.
- Afterwards the household writes about the reader, in the voice of whoever was not in the scene. The note is shown after the
  sheet's own words whenever that sheet is read again, without changing the saved original.

## The four rooms

Each room stands in the same night street as the leaks, in the household's own wallpaper. Nobody is seen: what the household does
is done where the reader is not looking, and what it says is on the action bar. Nothing goes into or out of the reader's
inventory.

### Sunday lunch (The good cups, The towel rail, The table, Sunday lunch)

**The room:** a dining room with a hatch through to a dark kitchen, a table of six, and a seventh chair against the wall.

**Laying the table:**
- The cutlery is in the sideboard. Forks go down first.
- Two of the forks don't match. Ruth changes them while the reader looks elsewhere.
- The good cups come out of the cupboard. The cracked one goes to the flowers first.
- The seventh chair is pulled up to the end of the table, with a place and a good cup there for the visitor, who never comes.

**Serving:** once the table is laid, Ruth's bowls come through the hatch while nobody watches it ("Mind, they're hot"). Six bowls
go round; the visitor's place gets none.

**The meal:**
- The reader sits in any place.
- The empty chairs scrape out and come back in.
- Ruth, Jonah, Bell, Mara and Marsh talk on the action bar about salt, the weather, a painted gate and the good cups.
- The bread on the board can be eaten.

**Success:** sit until the chairs push back.

**Failure:** get up while the others are still eating. Next Sunday no place is laid for that reader: there are five places, and
the only chair left is the visitor's.

**Afterwards:** Sabine writes about it.

### Fresh sheets (The airing cupboard, Fresh sheets, The basket, Lost and found)

**The room:** the back bedroom, with the bed stripped, the airing cupboard in the corner and the basket of warm washing.

**Folding:** two fresh sheets are shaken out over the rag rug. A reader who waits sees the other end lift by itself and come to
meet theirs. Folding one alone leaves a crease. The folded sheets go on top in the airing cupboard.

**Making the bed:**
- The older sheets come from underneath and go on the bed. A sock falls out of the fitted sheet; it goes on the basket for its
  partner.
- The cupboard has only plain pillowcases. The striped one turns up on the basket the moment nobody is looking at it.

**Success:** the bed made with the striped pillowcase and the sock home, with no crease.

**Failure:** a crease, which Ruth sees and says nothing about.

**Afterwards:** A. Bell writes about it.

### Mending day (Thursday, The hem, Mending day, The small tin, The window latch)

**The room:** the front room by the lamp, with Ruth's armchair, the biscuit tin of buttons, the blue coat and the sewing box.

**The lamp and the needle:** the lamp is drawn up so close that it shadows the needle ("You're making a shadow"). Moved aside, the
needle threads on the third try.

**The sleeve:**
- Turn the sleeve inside out before stitching it.
- Stitched unturned, it is sewn shut. Ruth unpicks it once and lets the reader try again; a crouching hand turns it with the needle
  threaded.
- Sewn shut a second time, it stays shut.

**The button:** chosen from the tin.

**Success:** the sleeve sewn right side in and the button on. E. Marsh's note names the button.

**Failure:** the sleeve sewn shut twice.

### First light (Breakfast, For tomorrow, The sink, The loaf, The saucepan)

**The room:** the kitchen before dawn, with somebody asleep on the settee under a blanket that rises and falls.

**What is done:**
- The kettle is boiled twice and taken off before it sings.
- The first cup is for warming hands; the second goes on the little table by the sleeper's head.
- One extra potato is peeled, and Bell's plate is wrapped with his name written on the paper.

**Noise:** running, jumping and a singing kettle wake the sleeper.

**Success:** the cup by the sleeper and the plate wrapped and named, without waking anyone.

**Failure:** waking the sleeper, who asks what time it is.

**Afterwards:** Mara writes about it.

## Not yet built

- Party members following a reader through the door, and the chores that take two done by two readers. Each reader has their own
  door and room.
- The household's sheets in the House of Leaves.

## Tests

`StaircaseLeakTests` keep their chores and recovery cases; the leak sheet now lies in the second stretch. Three new cases:
- **The order and the rhythm:**
  - the descent's order, three sheets to a stretch, a leak in each of stretches two to six;
  - stretch one refuses, and a used stretch blocks the next;
  - the bottom stretch and the homeward climb refuse.
- **Sunday lunch:**
  - a door left, refused to a peer and walked through;
  - the lunch laid, served and sat through, with the household's note on the sheet;
  - the peer's own reading makes the door theirs, and they get up;
  - the next Sunday has no place for them;
  - the wall and rail are put back.
- **The other three rooms:** Fresh sheets, Mending day (with the sleeve sewn shut and unpicked) and First light, each played
  through to success.

The live two-client proof's note scene now lies in the second stretch, with both readers' fires opened for them.
