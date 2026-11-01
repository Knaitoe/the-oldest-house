# The replacement (0.4.218)

The owner, 31 October: "vignette that only triggers for players with a companion - the player is on a neighborhood street. all homes
are boarded minus one. an npc replaces their companion for that vignette. follows around "meowing" or "barking." the player can't
shake them lose. the npc has a hoard of dead cats and dogs in its messy, uncared for home."

This is the build.

**Slot 145. Literary, outside, multi-visit. Witness: 69 sources / 52 required / two kinds / three endings.** The new story is
`replacement` ("The replacement", understanding).

Layout 71. No protocol change from 0.4.217. New builds only.

## Who it is dealt to

The street is dealt only to a reader who has a companion following them: a tamed cat or dog of their own, not told to stay.
- Every two seconds, each reader inside the House has this noted in their own record (`Companion218`).
- The dealer reads that note (`Replacement.hasCompanion`).

## The street

`ReplacementSite` builds it whole, under the outside sky at midnight (scene clock 9). The polish passes skip it.
- **The way in:** the reader comes out of the front door of a house at the south end, boarded from the outside like every other.
- **The road:** it runs north between sidewalks.
- **The houses:** three either side, siding in three faded paints, every window and door boarded over with new nails in old wood.
- **The posters (the source):** a telephone pole by the first house, papered with LOST posters for years of animals, every one ticked
  in the corner. The newest is in the reader's own handwriting, for their own companion, by name.
- **Lamps:** two street lamps still lit; the last one dead.
- **The open house:** at the end, past a turning circle. Its door stands open and its windows have gone brown.
- **The edge:** a hedge too high to climb closes the street in.

**Inside the open house:**
- **The front room:** a stained sofa with a dip the shape of someone who sleeps curled up, newspapers, bin bags, bowls everywhere,
  a litter tray, a blanket over a shape, a cat on the cushion that does not move, and along the wall a row of collars on hooks, every
  one with a tag.
- **The kitchen:** tins, bowls, bags, and a dog curled in the corner, not breathing. Its back door is the way on.
- **The back bedroom:** blankets over shapes, a cat by the wall, and in the far corner a crate, shut.

Nothing is graphic: the animals lie curled as if asleep, and the blankets are not lifted.

One fitting block, `replacement_fitting` (17 kinds), and six materials, drawn by `tools/draw_replacement_art.py`. Its noises are
vanilla's.

## The replacement

**The swap.** The first moment the reader isn't looking at their companion, it is gone, and something is at their heels in its
place (`Replacement`):
- a person on all fours, in a stained cardigan (a new skin, stranger phase 43);
- making the companion's noises in a person's mouth: "Mrrow.", "Woof.", "It purrs. It isn't a purr. It's someone saying the word
  under their breath.";
- the noises are pitched-down vanilla cat and wolf sounds, private to the reader, with a line on the action bar.

**It can't be shaken off:**
- it keeps two blocks behind the reader;
- left more than nine blocks behind, and out of sight, it is behind them again;
- when the reader crouches it lies down and rolls half over, "the way yours does";
- touched, it pushes its head up under the reader's hand.

It is the reader's own: no one else sees it.

**The companion.** It is safe the whole time:
- it sits shut in the crate in the back bedroom, and cannot be hurt while it is held;
- now and then it can be heard from the street.

**The account (success).** The reader opens the crate:
- the companion comes out and follows them again;
- behind them, the replacement stands up for the first time and says the companion's name in its own voice, as if trying it out;
- then it goes and lies down on the blanket in the front room with the others.

Out on the street again, with the companion within sixteen blocks, the reader is ready (`took_it_home`). There's a page in the
mailbox of the house they came out of.

**Leaving without it (failure).** If the reader leaves the street with their companion still in the crate:
- the companion is sent home to the manor's front hall, and waits there, sitting, still theirs;
- "Somewhere behind you, something meows in your companion's voice. Yours has gone home ahead of you. It will be waiting.";
- no account that visit.

They may come back another time. A companion still in the crate from an earlier night stays held until it is let out.

## The account

The account is a page torn from a ledger in the open house, in a careful hand that gets less careful down the page:
- a column of names: Biscuit, Pepper, Rufus, Mouse, Duchess, Bear, Smudge, each with "stayed";
- the last line, in a different pen, gone over twice: the companion's name, "went home".

Then the reader's own words:
- not seeing it go;
- the thing at their heels that knew how the companion lies down;
- the house of bowls and blankets and collars;
- the crate;
- the thing standing up and saying the name;
- "the last line in its book has [the name] on it, and that it says went home".

## Literature

- **Witness line (understanding):** "It wasn't mine. Something wore my companion's noises at my heels down a street of boarded houses,
  and I found mine shut in a crate in the one house that was open, among the ones that weren't so lucky, and I brought mine home."
- **House of Leaves place:** "the street where every house was boarded but one". The account's title is "The account: the street".
- **Co-op lines:** "on the street with me. Neither of us said anything about what was following the other." / "still on the street
  when I came out of the open house with mine."
- **Miniature:** matchbox houses with their windows taped over, all but one, with a collar no bigger than a ring on its door.
- **Advancement:** `account/replacement`, shown with a lead.

## Multiplayer

- Each reader's companion, crate, replacement and record are their own.
- Two readers with companions each have something at their heels that the other cannot see.
- The street and the house are shared.

## Tests

`ReplacementTests` (literary namespace), four cases:
- **The deal and the build:**
  - dealt only with a companion following;
  - the posters, the mailbox, the crate and its pen;
  - the boarded houses;
  - the clear way down the street;
  - the posters' text and the Witness pool.
- **The swap:**
  - a tamed cat named Pip, behind the reader, is taken to the crate (sitting, unhurtable);
  - the street records what it took;
  - something is at the reader's heels, and still is after the reader is moved.
- **The crate:** opening it gives Pip back, following; the thing stands up; out on the street with Pip, the reader is ready and the
  mailbox has its page; the ledger names Pip, and the account never genders the reader.
- **Leaving:** with Pip in the crate, the reader leaves; Pip goes home to the manor's hall, sitting, still the reader's, and nothing is
  held any longer.

The architecture case checks the street's asphalt, the poster on the pole and the crate, and exports its view.
