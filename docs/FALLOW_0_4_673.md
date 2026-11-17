# Fallow 2: the afternoon (0.4.673)

This is the second part of *Fallow* (`docs/FALLOW_PLAN.md`), from the proposal the owner approved on 17 November. Part 1 (0.4.672,
`docs/FALLOW_0_4_672.md`) built the station and its five people. This part makes the afternoon happen, and the House deals the place
from here. It is still not a Witness source; that comes in part 5.

## What happens

One clock runs for everyone at the station, and only while somebody is there. When the place has stood empty for five seconds
(`Fallow.EMPTY_FORGET`, 100 ticks), the afternoon is over. The next reader in starts it again, with the bean back in the car and the man
not yet on the road.

1. **The chores** (from 100 ticks). Harlan gives each reader near enough to hear a chore, one at a time, said aloud to that reader
   (`LiteraryActor.sayTo`).
   - The chore follows the House's read of the reader's age:
     - A reader read as young keeps Opal company while her mother sees about the car.
     - Anyone else, unread readers included, minds the till.
   - A second reader gets whichever chore the first did not, so that both are seen to. Past two, it is the read again.
   - He says it in the way the House's read of the reader lets him address them (`HouseRead.address`).
2. **Under the car** (from 220 ticks). Harlan waits until everyone near has a chore, or for twelve seconds more, and for his last word to
   be said. Then he gets down off his stool and goes along behind the counter and out by its flap. He opens the garage door and leaves
   it open. Under the car he works with his arms up into it and a wrench in his hand, and the wrench is heard now and then. He and Boyd
   have three lines between them.
3. **The bean** (from 480 ticks, once he is under the car and their word is done):
   - Opal goes round to the garage and asks for her bean, which is in the car.
   - He reaches up into the car for it and hands it down. She takes it in both hands and carries it out front.
   - She kneels on the forecourt by her own step and sets it down in the sun (`FallowBlock.Kind.BEAN`, stage 0, at `Fallow.BEAN_AT`).
   - Whoever stays within four and a half blocks of her keeps her company. That reader's time is kept (`Kept`, in ticks), and she says
     one of four lines to them every seven seconds.
4. **At the register.** Customers come in turn: Faye for milk (from 760 ticks), then Otis for coffee and twenty on the diesel (from 1080).
   - Each walks to the cooler or the coffee machine and takes what they came for, which is seen in their hand. Then they come to the
     register and ask.
   - A reader behind the counter who puts a hand on the register rings them up (`Fallow.ring`). The register's sound plays, the customer
     thanks that reader, and the reader's own `Rang` count goes up.
   - From in front of the counter the register only describes itself, as in part 1. With nobody waiting, the drawer opens on nothing.
   - After ten seconds with nobody ringing them up, they leave the money by the register, and the coins are heard.
5. **The man on the road** (from 1400 ticks, once both have paid). A man nobody knows comes walking along the road out of the haze from
   the west. He is made only where no reader sees him come to be.
   - He takes a bottle of pop from the cooler and pays for it like the others (rung up or not).
   - He goes out to Opal and asks her name. She keeps her eyes on her bean and does not tell him.
   - He turns to the nearest reader and asks the time. Every reader within ten blocks who can see him is asked on their conversation
     panel. They can tell him ("Almost five.", said aloud over the reader's head) or say nothing.
   - One answer is enough: he says what he says and goes. With no answer in eight seconds, he says it does not matter and goes.
   - He walks off up the far pad's track into the wheat. At the end of it, the first moment nobody sees him, he is gone.

## Single and multiplayer

- **Shared.** The station, the clock, where everyone is, the bean, the customers and the man are the same for every reader there.
- **Each reader's own:**
  - their chore (`Chore`, and `Chores` for how many afternoons have given one);
  - what they rang up (`Rang`, and which customers: `Rang0` to `Rang2`);
  - how long they kept Opal company (`Kept`);
  - whether the man asked them the time (`Asked`), and whether they told him (`Answered`) or said nothing (`Silent`);
  - their talk counts, which go on as in part 1, with new counts for Harlan's word under the car (`HeardWork`) and Opal's by her bean
    (`HeardBean`).
- **Alone.** The afternoon plays the same for one reader:
  - Harlan gives them the chore their read gives them.
  - The customers come whether or not anyone is at the till.
  - The bean is set down whether or not anyone is by Opal.
  - The man asks whoever is near enough, or nobody.
- **Two or more.** The chores are split so that one minds the till and one keeps Opal company. When two readers are near the man, each
  is asked, and the first answer counts for the one who gave it. The others' panels are closed.
- **Spectators** are not readers. They are given no chore, ring nothing up, keep nobody company, are asked nothing and count for nothing.
  They are not counted when the man looks to see whether anyone sees him.

## Dealing

`Fallow.dealable` is true from ten crossings down (`Fallow.DEPTH`). That is after *Last Seen*'s eight and before *Not Yet*'s twelve.

## Made as

- **The afternoon.** `Fallow`:
  - The `Site` holds the clock, who has been sent away from their place, the routes being walked, each reader's chore, and how far
    along Harlan, the bean, the customers and the man are.
  - The five are found anywhere in the scene now, not only at their places, and only ever one of each.
  - `ensure` leaves be whoever the afternoon has sent somewhere.
  - Walkers stop to answer a reader who speaks to them; the man does not. A walker caught on something for three seconds goes on to
    their next point the first moment nobody sees, as in *Last Seen*.
  - The front door and the garage door are opened by whoever comes to them shut, and left open.
- **The bean.** `FallowBlock.Kind.BEAN`, stages 0 to 3: two leaves, four, a tangle as high again as the cup, and grown long and thin in the
  dark. Stages 1 to 3 are for parts 3 and 4. It has no collision. Its model is a six-pixel paper cup with a rolled rim, soil a little
  below the rim, and the plant on two crossed sheets, as vanilla's saplings stand.
- **Items** (`FallowRegistry`), held by the people and never by a reader:
  - the bean in its cup (`fallow_bean_cup`, drawn from the block's model and sized for a hand);
  - a quart of milk (`fallow_milk`);
  - a bottle of pop (`fallow_soda`);
  - a cup of coffee (`fallow_coffee`).
  - Harlan's wrench is The Last Crossing's.
- **Art.** `tools/draw_fallow_art.py`:
  - the cup's side (a ring of print and her pencil going downhill across it) and its soil;
  - the plant at four stages;
  - the three items.
  - All at native scale; reviewed enlarged before going in, and the bean at all four stages in eye-level frames on the forecourt.
    - The leaves were first drawn as hearts upside down, which read as clover; they are pointed blades now, shaded along their lower
      edge.
    - The quart of milk first read as a bottle with a neck. It is a carton seen a little from one side now, with its roof sloping back
      to the fin, the gable over its side and a drop in the band of print.
    - Her pencil on the cup was a dark mark, and from a step away the cup seemed to have two dark eyes. It is one faint stroke now,
      running downhill under the rim, and the soil is a shade lighter so the cup's mouth is not a black hole.
- **Sound.** `tools/generate_fallow_audio.py`, subtitled:
  - the register ringing up a sale ("A cash register rings up a sale");
  - a ratchet wrench under the car ("A wrench works under a car");
  - coins set down on the counter ("Coins are set down on a counter").
- **Poses.** `LiteraryRenderers.fallow`:
  - working overhead under the car, the wrench turning;
  - an arm reaching up or out;
  - a cup held in both hands at the chest, head bent to it;
  - kneeling by the bean, as June kneels at the line in the Pell house, the head bowed;
  - the pot tipped over a cup.
- **Tests.** `FallowTests`:
  - The people case now holds the afternoon back, so the five stay at their places. It waits for the car on the lift as well as the
    five, because in the 0.4.672 release chain the car came after them.
  - New: `theChoresAndTheRegister`, with three readers. It covers the age read and the split, the chore said aloud, Harlan off his
    stool and under the car with the garage door open, Faye rung up from behind the counter and not from in front, the drawer on
    nobody, Otis leaving the money, and every new line checked for colour, age and feeling.
  - New: `theBeanAndTheManOnTheRoad`. It covers the bean fetched, handed down, carried and set down, Opal kneeling, keeping her company,
    and the man made unseen out of the haze. He buys his pop and is rung up, Opal does not answer him, and he asks the reader near her
    the time; the reader tells him and the answer is their own. He walks into the wheat and is gone.
  - The Fallow CI job's limit is 30 minutes now.

## Found and fixed in the making

- **The car on the lift, in the 0.4.672 release chain.** The people case found the five and then looked for the car straight away; the
  car is made only once the scene's entities are loaded, on the twenty-tick beat, so it sometimes came a beat after them. The case now
  waits for the car too.
- **Last Seen's forged line, in run 1705.** The Pell house checks that every Witness story has a forged journal line, and *Last Seen*
  became a Witness source in 0.4.671 without one. It has one now ("I kept one of the folders back and never said whose face was in
  it."), flagged in `docs/LAST_SEEN_0_4_671.md`. *Fallow* will need its own in part 5, when it becomes a Witness source.

## Every new line

Every line below is new in this release. None names a colour or an age, and none says how a reader feels. The reader is addressed only
through the House's read (Harlan's chores), never otherwise by a gendered word.

**Harlan's chores** (`FallowTexts.TILL`, `FallowTexts.COMPANY`), said aloud to the reader. The addresses are the House's read: neutral,
then masculine, feminine, androgynous.

- Minding the till:
  - "Do me a favor and mind the till while I'm under that car. Ring up whatever they bring you."
  - "Do me a favor, sir, and mind the till while I'm under that car. Ring up whatever they bring you."
  - "Do me a favor, ma'am, and mind the till while I'm under that car. Ring up whatever they bring you."
  - "Do me a favor, friend, and mind the till while I'm under that car. Ring up whatever they bring you."
- Keeping Opal company:
  - "Keep the little one company while her mama sees about the car? She's been on that step since noon."
  - "Keep the little one company, son, while her mama sees about the car? She's been on that step since noon."
  - "Keep the little one company, honey, while her mama sees about the car? She's been on that step since noon."
  - "Keep the little one company, kid, while her mama sees about the car? She's been on that step since noon."

**Harlan under the car**, to a reader with a chore (`FallowTexts.working`), first then after:

- Till: "Drawer sticks. Give it the heel of your hand." Then: "Gas is on the honor. Just ring up what they take from the store."
- Company: "She's no trouble. Talks about that bean like it was a dog." Then: "Her mama's had a long day. You're doing her a kindness."

**At the car**, overheard (`FallowTexts.AT_THE_CAR`):

- Boyd: "You said an hour an hour ago."
- Harlan: "Fuel pump's off a different year. I'm making it fit."
- Boyd: "Make it fit faster."

**The bean:**

- Opal: "Can I have my bean? It needs the sun."
- Harlan: "Here. Hold it level, now."
- Opal by her bean (`FallowTexts.BEAN_TALK`), in order:
  - "It's a bean. It came up in a cup at school, and I got to bring it."
  - "You can touch the leaf. Gentle."
  - "Boyd says it won't live in a cup. It's living."
  - "When we get to my aunt's I'm going to put it in the ground by her porch."
- A hand on the bean (`FallowTexts.thing`):
  - "A paper cup of dirt with a bean come up in it, two leaves open. OPAL is written on the cup in pencil, the letters going downhill."
  - Later stages:
    - "The bean has put out two more leaves since it was set down."
    - "The bean has grown over the rim of its cup and leans out after the light."
    - "The bean has grown long and thin in the dark, its leaves open wide, a tendril feeling for something to hold."

**At the register** (`FallowTexts.CUSTOMER`): asking, thanking whoever rang them up, and leaving the money.

- Faye: "Just the milk." / "Thank you." / "I'll leave it by the register."
- Otis: "The coffee, and twenty on the diesel." / "Obliged." / "Money's on the counter, Harlan."
- The man: "Just the pop." / "Keep the change." / "That ought to cover it."
- The register, behind the counter: "The keys go down, the bell rings and the drawer jumps out. The money goes in." With nobody waiting:
  "The drawer jumps out on its spring. Nobody is waiting to pay."
- Coins left, to readers in the store: "Coins clink on the counter by the register."

**The man** (he has no name; the conversation panel calls him "The man"):

- Spoken to before he asks the time: "Hot one."
- To Opal: "That's a fine bean. What's your name?" She does not answer. Then: "That's all right. Names keep."
- To a reader: "Got the time?"
- The reader's replies: "Almost five." (said aloud over their head) or "Say nothing." (an act; nobody hears it).
- Told: "Later than I thought." Not told: "Doesn't matter."
