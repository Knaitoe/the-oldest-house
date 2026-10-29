# The coatroom's wardrobe — 0.4.169

The owner:

> "a drastically wider variety of clothes, each guessing something about the player's personality. maybe add a brief history to each piece of cloth - belonged to a circus performer. a man died in this. passed down from your brother. etc etc."

The coatroom had three coats and three pairs of boots. It now has twelve coats and six pairs. Each garment has:

- a past, shown under its name when it is held;
- a guess about the reader, which the House says the first time the reader puts it on.

## The garments

The first three coats and the first three pairs of boots are the originals, with their ids, names and art unchanged. Every garment belongs to one of the three sets the House already reads presentation from: the wool overcoat's, the trench's and the barn jacket's.

| Garment | Set | Cut | Its past | The House's guess |
| --- | --- | --- | --- | --- |
| Wool overcoat | wool | long | Bought for a funeral and worn to every one after. | You dress for the worst day and call it being ready. |
| Trench coat | trench | belted | Left on a late train and never claimed. | You plan your way out before anyone asks you to stay. |
| Barn jacket | barn | short | Hung by the back door of a farmhouse for forty winters. | You would rather be useful than looked at. |
| Ringmaster's tailcoat | wool | long | Belonged to a circus performer. The lining is darned in a dozen threads. | You like to be looked at, as long as it's on purpose. |
| Pea coat | wool | short | A deckhand's. The pockets still smell of rope. | You keep your hands busy so nobody asks what they'd rather be doing. |
| Chore coat | wool | short | Passed down from your brother, who never came back for it. | You take the hand-me-down and say it fits. |
| Swing coat | trench | long | Worn to a wedding the bride never came to. | You dress for the party in case there is one. |
| Coat with the soft collar | trench | belted | A man died in this. The family kept the coat. | You would take a stranger's warmth before you'd go cold. |
| Rain mac | trench | belted | From a school's lost-and-found box, the name label cut out. | You were the one still waiting when the others had been picked up. |
| Patched jacket | barn | short | Every patch is a town its owner swore never to go back to. | You collect places so you never have to stay in one. |
| Duffel coat | barn | long | A schoolteacher's. There is chalk in the seams. | You like rules you can explain. |
| Long cardigan | barn | long | Knitted by someone who ran out of yarn and finished it in another. | You keep a thing unfinished rather than throw it away. |
| Heavy leather boots | wool | boots | Resoled four times. The last cobbler wrote his name inside. | You mend things instead of replacing them. |
| Ankle boots | trench | boots | Bought for dancing and kept for interviews. | You know which nights matter. |
| Work boots | barn | boots | Steel in the toes, and somebody else's initials on the tongue. | You would carry another name if it got you through the door. |
| Riding boots | wool | boots | Won in a card game, by the scuff on the heel. | You would bet on yourself, quietly. |
| Galoshes | trench | boots | Two sizes too big, stuffed with newspaper at the toes. | You grow into things. |
| Plimsolls | barn | boots | Drawn on in ballpoint: a phone number, and a heart. | You hold on to the ones who wrote on you. |

A garment's past may be anyone's: a man, a brother, a bride. The guesses are about the reader only, and none of them genders the reader or names a colour or an age. The new case checks every past and guess for that.

## The hall

- **Coats.** An empty hanger hangs by the door. Then:
  - eight coats on hooks down the west wall;
  - four on hooks at the far end of the east wall, past the bench.
- **Boots.** The six pairs stand on the mat under the west hooks.
- **Order.** Coats and boots are each placed in an order of the reader's own, so no garment is always nearest the door.
- **Removed.** The scarf and hat on their hook, to make room.

Everything else stays where it was: the bench, the radiator and window, the hall glass and its towel, the curtain and the umbrella stand.

## Wearing them

Using a coat or boots puts them on, lent, as before:

- the garment leaves its hook or place, and the one it replaces goes back;
- the first time each garment goes on, the House's guess appears in grey italic;
- leaving the room puts everything back, and the reader's own gear returns to its slots.

**On the reader.** The fitted coat mesh follows the coat's cut:

- a short jacket has a collar and no skirt;
- a long coat has a skirt, in two halves that swing with the legs (0.4.166);
- a belted coat has its skirt and a belt.

**Telling them apart.** Each garment has its own worn texture and its own model on the hook:

- the ringmaster's cutaway front and tails, with gold frogging;
- the swing coat's flared skirt;
- the cardigan's second yarn below the hip;
- the duffel's horn toggles;
- the soft collar's fleece;
- the plimsolls' ballpoint;
- the galoshes' newspaper.

## What the House reads

Nothing about the read changes. A garment counts as its set:

- the first garment taken sets `SELF` tentatively;
- a coat and boots from the wool and trench sets worn together read `ANDROGYNOUS`;
- so does anything from the barn jacket's set;
- the glass works as before.

The guess is the House talking to the reader, not a new signal. Nothing is inferred from which coat in a set was taken.

The coatroom's record now keeps the garment as well as its set:

- `CoatId` and `BootsId` sit beside `Coat` and `Boots`;
- an older record keeps only the set's name, which is that set's first garment.

## Where it goes

- **The account** names the garment: "took the ringmaster's tailcoat off the peg in the hall and the galoshes from the mat". The boots now come from the mat whatever their set, since they no longer stand under their coats. Older records read as before.
- **The House of Leaves** (*II. The hands*) names it: "in the cold hall, to the coat with the soft collar".
- **The House's version of the reader** wears the coat they took, in glass and copies.

## Tests and proofs

- **New wardrobe case:**
  - the built hall passes the quiet-room audit;
  - every garment is in each of two readers' wardrobes once, in different orders, and the hanger by the door stays empty;
  - every garment has a past, a guess and a set, with no word reading gender, colour or age;
  - the tailcoat comes off its hook onto the reader, lent, with its past in its tooltip and the House's guess said;
  - it reads by its set and is kept by itself;
  - a second coat swaps back the first, and each is guessed once;
  - the boots are kept;
  - the account and the book name the garments, and an older record keeps its words;
  - leaving puts everything back.
- **Updated cases:**
  - the coatroom retreat takes its boots from the mat;
  - the glass case finds a mixed outfit by set across the wardrobe;
  - the book-width specimens include the longest garment names.
- **Client proof:** three native pages in `build/house-read-wardrobe/`, required by CI:
  - all eighteen garments as they hang and stand;
  - the twelve coats with boots on standard bodies from the front;
  - the same on slim bodies from the back, mid-stride.
- **Locally:** the house-read, tailoring and co-op suites (39 cases).

## Also in this version (run 1334)

- **The kitchen's slot.** 0.4.168 appended the kitchen at slot 112. With a manor at height 80, a column holds eight slots, so slot 112 fell in a fifteenth column, one past the limit. Its base was null, and every case that appends or upgrades a layout at that height failed on it: the hotel's slot check, the old-layout cave, the layout-33 halls, the version-17 upgrade and the scene-detail upgrade. The limit is now fifteen columns. No earlier slot moves.
- **Mapping's brothers.** The case moved the reader on at fixed ticks (10, 20 and 40). At the end of a loaded literary run the brother had not arrived by tick 40. Each step now waits for the one before it.

## Versions

- Protocol 95: twelve new fittings and garments mean a client and server must both be 0.4.169.
- No layout or Witness change: 54 / 41 / two kinds / three endings.
