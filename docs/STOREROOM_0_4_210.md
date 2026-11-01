# The storeroom (0.4.210)

The second of the dark halls' horrors (docs/DARK_HALL_HORRORS_PROPOSAL.md), after Johnny's storeroom in the book. The owner,
1 November: these "should be the most extreme yet, either psychologically or physically"; "real deaths are the right call".
Earlier that day the owner had picked the storeroom as one of the House of Leaves sequences to build.

Slot 140, layout 66, no protocol change. One new Witness story: **63 sources / 48 required / two kinds / three endings**.

## Where it is dealt

Only in the dark band, from depth sixteen (`DarkHalls.DARK_DEPTH`): one door among several, with the usual story spacing
(`DarkHalls.horror`). It is a one-shot room; a failed visit can be dealt again.

## The room

Built whole in the dark halls' ash (`StoreroomSite`), with no light but the bulb:

- **The hall:** three wide and twenty long, running north from the way in. Johnny's footnote lies on its floor just inside (the
  account), and the way on is in its east wall.
- **The door:** dark oak at the hall's end, on a spring. A moment after nobody is in it, it bangs shut.
- **The storeroom:** twelve deep and four high, between grey steel shelving down both sides and across the end. One bulb hangs
  on a pull chain halfway down, and there are boxes on the floor.
- **The list:** pinned beside the door, on its inside (the source). A reader facing into the room has their back to it.
- **The three things:**
  - the big bottle of black ink, on the left shelving;
  - the needles in their sterile packs, on the right;
  - the reader's own thing, on the end shelf.

## The list and the reader's own thing

The list is a page torn from a notebook like the reader's, in their own handwriting, which they have never written:

- black ink, the big bottle;
- needles, the sterile ones;
- a thing of their own, "(mine)".

Under the list, pressed so hard the pen has gone through, it says: DON'T TURN ROUND.

The thing is a copy of something of the reader's, chosen once and kept. It carries a tag with the reader's name in their own
hand. It is taken from, in order:

1. the containers in their copied home;
2. their ender chest;
3. what they carry;
4. failing all of those, a key (a tripwire hook).

It is always an everyday thing: never a container (a copied box would copy its contents), nothing enchanted or rare, and no
precious metal or gem. It is a likeness, never a windfall.

## What happens

The room starts running once the door has shut behind a reader who is in the storeroom. While nobody faces the door,
something gathers behind them.

**The dread** fills in thirty seconds, or fifteen with a back to the door. As it rises:

- the bulb's chain swings toward the reader and the bulb flickers;
- breathing comes nearer: "Breathing, behind you, nearer"; then "Breathing, an arm's length behind you";
- the heart pounds;
- the edges of the sight close in.

**Facing the door holds it back** and empties the dread. Once the list has been read, though, every look costs something:

- the bulb dies for a second and comes back with the side shelves a block nearer;
- holding the look costs another every four seconds.

| Looks | The storeroom between the shelves |
|---|---|
| 0 | five wide |
| 1 | three wide |
| 2 | one wide: the shelves press both shoulders |
| 3 | shut |

- **A reader where a shelf now stands** is pushed into what is left.
- **From the second look the pinch:** a heart every three seconds, and the breath spent fast.
- **At the third look the shelves meet,** and anyone between them dies there: a real House death, with the Mother's custody and
  the portrait if it is the first. The damage type is the new `storeroom`, through armour and the grace after joining.

## How it ends

**The dread runs out:**

1. a hand closes on the reader's shoulder;
2. their head is turned slowly toward the door, against the mouse;
3. the bulb goes out the instant they would see;
4. a real blow (three hearts), and they come to on the hall floor outside, with the door shut.

What they lose:

- One stack of their own ordinary things is gone, for good.
- The three things are taken back.
- The thing with their name on it comes back days later, laid on their bed in the manor. A reader who never goes near their bed
  finds it in their pocket a couple of days after that. While it is away, and once it is back, the end shelf holds only a clean
  square in the dust and the tag, which counts as having it. It is never copied twice.

The visit can be tried again.

**Too many looks:** the shelves meet (above).

**All three, and backing out:** with all three things, a reader who backs into the door without turning round finds it opens
behind them. They are put out in the hall, and Johnny's footnote, by the way in, is ready (`did_not_turn_round`).

The Witness line: "I didn't turn round." Kind: survival.

## Co-op

The room, its shelves and its dread are shared; the list, the three things and the outcome are each reader's own.

- Two readers back to back hold the clock between them: with two or more in the storeroom it climbs at half the rate.
- The room shortens whenever either of them looks.
- The pinch closes on both.

## Literature

**The source:** the list, in the reader's own hand (above).

**The account:** "The account: the footnote".
- It opens in Johnny's hand: a footnote about going back to the storeroom for black and knowing, as the spring door shut, that
  the room was not empty, and behind him. "The trick was not to win. The trick was to..."
- The footnote runs off the page, and the next lines are in the reader's own hand, describing the same room:
  - the third thing, with their name on a tag;
  - how many times they were knocked down, and what was gone;
  - how many times they looked back;
  - backing out without turning;
  - "I don't know which of us wrote the tag."

**Elsewhere:**
- the co-op line;
- the miniature;
- the staircase prose ("the storeroom behind the spring door");
- the co-author pool;
- the advancement under survival.

**Limits held:**
- nothing gory is shown;
- nothing in the room is ever seen;
- the reader's gender, colour and age are never named;
- lines are on the action bar, never in chat.

## Art and sound

`tools/draw_horror_art.py` adds the storeroom's pieces to the shared `horror_fitting` block:
- the steel shelving, bare or holding the ink, the needles or a tagged thing;
- the bulb on its chain, out, lit and swung either way;
- the list;
- the boxes;
- the footnote.

`tools/generate_horror_audio.py` adds six subtitled cues:
- breathing an arm's length behind;
- steel shelving dragged nearer;
- a bulb dying;
- a spring door banging shut;
- a heartbeat;
- a blow from behind.

Both are checked in CI.

## Tests

Six `StoreroomTests` in the literary namespace:
- dealt only in the dark band, with the door, the shelves and the list;
- the door springs shut, and every look brings the shelves nearer;
- the dread runs out, and the reader comes to in the hall with something gone;
- the third look brings the shelves together;
- all three things, and backing out without turning;
- two readers hold the clock between them, and either look shrinks it.
