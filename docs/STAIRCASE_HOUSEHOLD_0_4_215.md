# The household on the stairs (0.4.215): the long way home, washing day, the lay-by and the last landing

This finishes the household vignettes (docs/STAIRCASE_HOUSEHOLD_PROPOSAL.md). The frame, the rhythm, the door and the first four
rooms are in docs/STAIRCASE_HOUSEHOLD_0_4_214.md. This pass:
- builds the last four rooms;
- gives the household its own Witness story, resolved at the last landing.

**Witness: 67 sources / 51 required / two kinds / three endings.** The new story is `household` ("The household", understanding).

No protocol change. Layout 69. New builds only.

## Which sheets open which rooms

The three new rooms open from sheets that were "only reading" in 0.4.214. Sheets marked * lie in the continuation below the cell,
which readers rarely walk.

| Room | Sheets (stretch) |
|---|---|
| The long way home | On the way home (2), At the lights (6), The turn*, The bend by the school*, The handbrake* |
| Washing day | After the rain (4), The blanket (7), A stubborn stain*, The clothes horse* |
| The lay-by | A longer way (5), The queue*, The petrol station*, The lay-by* |
| The last landing | The landing (8), The wool scarf (8) |

All of them keep to the 0.4.214 rhythm: at most one household thing in a stretch, nothing straight after, nothing in the first
stretch, nothing on the climb home.

## The long way home

**The room:** the passenger seat of Mara's car, driving home in the dark. The bread is between the reader's knees. The street lamps
go past on both sides, from the bonnet to the boot, while the car moves.

The reader sits for the whole drive. What happens, in order:
1. **The radio.** Mara asks for it to be turned down. Turned off, she says what the house by the corner used to have: the pear tree.
2. **The lights.** At a red light the windscreen fogs. Wiped with a sleeve, a clear patch shows a boy teaching his little sister
   to wait for the green man.
3. **The bend by the school.** Leaning into it is turning to look into the bend. The first time Mara says it won't help. At the
   second bend she leans with the reader.
4. **The junction.** Mara asks "Is this our road?":
   - checking the map says "Not this one. The next";
   - saying nothing in time is saying yes too soon: a wrong turn to a field gate, where Mara reverses and stops to look at the sheep.
     Both are fine.
5. **Home.** The engine ticks. The handbrake goes up, and up again to be certain.

**Success:** the handbrake up twice. The wrong turn changes Jonah's note, not the result.

**Failure:** getting out before home. The car drives on with the passenger door open.

**Afterwards:** Jonah writes about it.

## Washing day

**The room:** the back yard after the rain, with the line free, the wet washing in its basket, a blanket folded by the back door
and the clothes horse inside.

**What is done:**
1. **The line.** Four shirts, one at a time from the basket: by the shoulders, by the hem, by the shoulders.
2. **The blanket.** Once the shirts are up, the blanket wants shaking. The reader takes one end at the back door and waits; the
   other end lifts by itself, and it is shaken out together. Crumbs fall into the grass, and the sparrows come down for them.
3. **The rain.** Then rain comes in over the roofs. Everything on the line is brought in, before the rain has been falling for
   about 25 seconds.
4. **The clothes horse.** The last shirt's damp sleeve goes over the clothes horse.

**Success:** the clothes horse used, with all four shirts in.

**Failure:** the rain gets what is still on the line, and it hangs soaked.

**Afterwards:** Sabine writes about it.

## The lay-by

**The room:** a lay-by on a country road, in the drizzle. The car is parked with Bell asleep against the passenger window. There
are sandwiches and two coffees on the bonnet.

**What is done:**
1. **The sandwiches.** Egg and cress, eaten standing up.
2. **The wrapping.** Folded into a small square, the way Mara does.
3. **The tractors.** Tractors go along the road from time to time. Each one watched as it passes is counted; three can be told to
   Bell when he wakes.
4. **Back in.** Then sit in the car again.

**Success:** back in the seat after the sandwich, the wrapping and three tractors, without waking Bell.

**Failure:** the horn. Bell wakes and asks where everyone is, and talks about the river the rest of the way.

**Afterwards:** Mara writes about it.

## The last landing

The household went down the stairs before the reader. Near the bottom it stopped on a landing and made camp, and that is where its
story ends.

**When it opens:** the last two sheets of the descent (The landing, The wool scarf), read in the bottom stretch, leave a plainer
door. They do
this only once all of the following are true:
- the reader is on that descent, in the staircase phase;
- the reader has been in three of the household's rooms, leaks or doors, whatever came of them;
- the household has not already moved on for that reader.

**The room:** the household's camp on a stone landing:
- four cups and the spoons;
- a lamp and the last candle;
- three tins;
- a line of socks with a space in the middle for Bell's shirt;
- Bell's coat spread on the seat by the wall for Ruth;
- the blanket roll;
- the morning list.

**What is done:** put it all back the way they had it:
- count the cups ("Mara counted them the same") and the spoons;
- check the lamp, which lights and then burns low;
- cut the last candle in half (the lamp first, in case);
- read the morning list: water, food, dry socks, the household's names, and at the bottom, in the reader's own handwriting, the
  reader's own name.

**Success:** all five done. The reader dries the last cup against their sleeve. The household moves on, and its story resolves for
that reader (`moved_on`).

**Failure:** a crouching hand keeps a cup, a spoon, a tin or the half candle. Nobody says anything and nobody packs.

**Afterwards:**
- On success the note is signed by the reader: "Before we moved on". It is written in their own words, as if they had been one of
  the household.
- On failure Ruth writes it.

The last landing does not count as one of the household's rooms, and once the household has moved on the landing door is not left
again.

### Why a door and not the landing itself

The proposal put the last landing on a real landing of the staircase. It is built as a pocket room behind a door, like the others,
so that:
- the camp is the reader's own;
- nothing is left on the shared stairs for another reader to walk into.

## Walking out

Walking out of any of the eight rooms before it is done now says what the household does behind the reader: the talk stops, the
bed is left, the car drives on, the washing stays out.

## Literature

- **Witness line:** "I put their things back in order. A household went down the stairs before me, and on a landing near the bottom
  I counted their cups, and read my own name at the end of their list."
- **House of Leaves place:** "the landing where the household left its camp".
- **Advancement:** `account/household`, shown with a flower pot.
- **The household's notes:** each room's note, success or failure, is shown with its sheet whenever that sheet is read again.

## Tests

`StaircaseLeakTests` adds one case: **the long way home, washing day and the lay-by open the last landing**. It plays each of the
three rooms through to success, then:
- the landing's sheet leaves the plainer door;
- the camp is put in order;
- the household has moved on, and the Witness story is resolved.

`WitnessPool` is 67 / 51 / 44: the household is not a place, so the literary count is unchanged.

The dealer cases now finish the passages under the cave (PITT) the way its field journal does, since that place has no Witness
story. This fixes the "finished vignette dealt again" and "scent finds a recurring anchor" failures in run 1380.
