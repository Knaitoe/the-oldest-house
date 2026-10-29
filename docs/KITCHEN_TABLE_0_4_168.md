# The kitchen table, the first quiet room — 0.4.168

The owner:

> "The skin-color selection should be the first vignette the player encounters"

This builds the self-portrait room from `docs/HOUSE_READ_DESIGN.md` (section 6, "The self-portrait") and puts it first. The House reads the reader's skin tone from the colour they choose for their own face, never from anything else, and uses it only for the House's body.

## The room

A family kitchen at about four in the afternoon, after school:

- low gold light through the window over the sink, onto a yard with a fence;
- the radio turned down low, the fridge humming, the kitchen clock ticking;
- a table with four chairs and a rug round it.

On the table:

- **The drawing**, in front of the chair nearest the door: a figure in light pencil, with four parts to colour (face, hands, hair, clothes) and nothing else drawn in but the eyes and mouth.
- **The tin of crayons**, to its left, in two halves:
  - along the back, the **twelve skin crayons**, from very light to very deep, in the design's colours;
  - along the front, the **eight others**: black, dark brown, light brown, yellow, orange-red, grey, blue and green.
- **A plate**, to its right, with a sandwich cut into triangles.

Around the room:

- **The fridge**, with old drawings on its lower door (a house, a dog, a sun with a face, a rainbow, none of them people), and room on the upper door for one more.
- **The way on** is the hall door past the fridge; **the way back** is the door the reader came in by.

Equal places:

- The skin crayons' places along the tin are shuffled for each reader, so the nearest or leftmost is never a default.
- No crayon is called "flesh" or "skin", and none is the normal one. A held crayon is just "Crayon".

## Drawing yourself

1. **Taking a crayon.** Using the tin takes the crayon at that place into the reader's hand, in its own colour. It is lent, like the waiting room's paper, and the reader's own item goes back to their hand when it is returned.
   - Taking another puts the first back.
   - Taking the same one again just puts it back.
   - The crayon in hand is missing from the tin.
2. **Colouring.** Using the drawing with a crayon colours the part under it with crayon strokes.
   - A near miss still colours the nearest part.
   - A part can be coloured again.
3. **Putting it up.** Using the fridge once the drawing has any colour on it:
   - the picture goes up on the fridge door under a magnet and leaves the table;
   - the crayon goes back into the tin;
   - the hall door opens.

   Before then the fridge just shows its drawings ("There's room on the door for one more.").

The doors stay locked until the picture is up, as in the other quiet rooms. A reader who does nothing hears a voice from the next room:

- after thirty seconds, "Go on, finish your picture. You can put it up on the fridge when you're done.";
- thirty seconds after they start colouring, if it still isn't up: "Put it up on the fridge when you're done, so everybody can see."

Once it is up, the House takes the reader on as they go through the hall door, or after five seconds.

Inside the tin's lid is the design's placeholder text ("Every tin of crayons used to come with one stick called flesh..."), for the owner to replace or rewrite.

## What the House reads

A new trait, `TONE`. Its value is the crayon's colour as drawn, never a name.

| Signal | Effect |
| --- | --- |
| The first face in a colour a person could be: any skin crayon, or either brown | Sets `TONE` to that colour, tentatively |
| The face coloured again in a different crayon | Disagrees: the read is uncertain until the next signal |
| The hands in the face's crayon | Settles it |
| The hands in another person's colour | Disagrees |
| A face in blue, green, black, yellow, orange-red or grey | Is play, and reads nothing |

The hair's last colour is also kept, if it is a colour hair can be (black, the browns, yellow, orange-red or grey).

Neither is inferred from anything else, and neither feeds anything but the House's body.

## Where it goes

- **The House's body** (0.4.163) wears the tone and hair colour the reader drew. Until now it always used the account's default skin. A reader who skips the read or colours their face green keeps that default.
- **The account's opening** now begins with the kitchen. It recites the act, never a colour:
  - "drew yourself at the kitchen table, face and hands alike, and put the picture up on the fridge";
  - "drew yourself at the kitchen table and put the picture up on the fridge".
- **The House of Leaves**: *II. The hands* takes "You drew yourself once at a kitchen table, and pressed hard on the hands." when the bathroom and the coatroom gave it nothing.

## The House's story

The kitchen is now the first room of the story, so the welcome comes before its line:

| | |
| --- | --- |
| Opening | You were small once. It was four o'clock, the kitchen was warm, and somebody had left the crayons out on the table. |
| Closing | There you are. Now I'll know you when I see you. |
| Cut | the fridge door shutting |

The waiting room's "You grew up the way everyone does, mostly by waiting" now follows it. Neither line genders the reader or names a colour or an age; the story test checks every line.

## Order and older readers

- **The order** is now:
  1. the kitchen table;
  2. the waiting room;
  3. the bathroom;
  4. the coatroom;
  5. the shelter;
  6. the porch.

  Saved names, offsets and slots keep their places. The kitchen is appended at slot 112 and its own offset; only the order the rooms are met changes.
- **Readers who entered the other five before 0.4.168** meet the kitchen as their next quiet room, as the design proposed. For them:
  - there is no second welcome or warning;
  - their depth is not reset when they finish it.

## Also in this version

- **The bathroom's new tools in the House of Leaves.** *II. The hands* only knew the first twelve tools, so the four added in 0.4.167 (bonnet, edge brush, rollers, wave brush) left the leaf empty. It now names all sixteen.
- **The account's room numbering.** It follows the order the rooms are met. The second sentence still begins at the hall.

## Tests and proofs

- **New kitchen case:**
  - the built room passes the quiet-room audit;
  - it is the first room a new reader meets;
  - the tin holds every skin crayon once along the backs and the eight others along the fronts, in an order that differs between readers;
  - the doors are locked;
  - a crayon comes into the hand in its colour, leaves the tin, and goes back when picked again;
  - the face sets the tone tentatively, a second face disagrees, and the hands settle it;
  - green hair is ignored and black hair kept;
  - the House's body wears both;
  - the picture goes up on the fridge and the door opens;
  - the account and the book recite the act;
  - the room is cut off with the fridge door and its closing line, and no crayon leaves.
- **Updated cases:**
  - the entrance chain now meets the kitchen first, then the waiting room, then the bathroom;
  - the address test counts the new slot;
  - the account's sentence split uses the new numbering.
- **Client proof:** two native pages in `build/house-read-kitchen/`, required by CI:
  - the objects through the production renderer: both tin halves with one reader's crayons and one taken, the drawing blank and coloured, the picture on the fridge door, and the radio on and off;
  - the drawing in each of the twelve skin crayons, beside the crayons as they are held.

  The room's own cutaway joins the five others (six exports, 23 art pages).
- **Locally:** the house-read, tailoring and co-op suites (38 cases).

## Versions

- Protocol 94: the new fittings, the crayon and the afternoon sky mean a client and server must both be 0.4.168. The kitchen's colours travel in its fittings' own block-entity data.
- No layout or Witness change: 54 / 41 / two kinds / three endings.
