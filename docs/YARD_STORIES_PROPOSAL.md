# The yard's stories: a proposal

The owner's draft "The House Has a Yard" (4 November) closes its yard with four stories that branch off it ("Where the vignettes branch off"):

- The Supper;
- In the Tall Grass;
- Growing Things;
- The Last Crossing.

For each, the draft gives the yard's contribution (how a reader comes to it) and the handoff (what the story then does). It also gives one rule for all four:

> *"Give each story its own door, gate, or clear decision to go farther. A player can see farmhouse light across the corn on an ordinary walk and still choose a different way on."*

This proposal builds each story with its own approach, as one piece of work, because an approach with no story behind it is a dead end. The owner said to start running with it, so these are defaults to edit. The order of building is The Supper, In the Tall Grass, Growing Things, then The Last Crossing. The Last Crossing has its own design, "The Last Crossing" in the owner's Drive, and is built from it.

## What all four share

- **Built whole, outside.** Each is a literary place in the outside dimension, under its own scene clock and weather, built by its own builder from the yard's materials, as State Route 9 and A Deer in the Road are.
- **The approach is part of the place.** The yard's contribution is the first stretch of the place: a corn maze, a road shoulder, a back fence or a farm road. A reader can turn back on it.
- **The door is a clear decision.** Going farther is always one visible step: a porch door, the first row of grass, a garden gate, a ferry landing. Until the reader takes it, the House's door behind them still leads back, and leaving costs nothing.
- **Dealt after the ordinary yard.** As with State Route 9, each story is dealt only to a reader who has crossed the House's ordinary yard, once per reader, and never from the shared cache.
- **Witness stays derived.** Each story that has a personal resolution adds one source to the pool, and the quota follows from `WitnessAccount.Story`. Different choices within one story count once.
- **Literature travels with the mechanics.** Each story has a first paper (its source, outlined until it is read) and a written account at its ending.
  - All in-game text is original. The published stories the draft links to inspire the shape and are named in the docs, never quoted.
  - The House's writing never genders the reader and never names a colour or an age.
  - Speech is never chat. Characters speak on displays and in the action bar. Readers speak through the conversation panel, and what they say shows over their head (0.4.329).
- **Every way of being there has its own account.** Each reader's account is built only from their own record: what they did, saw and said, who was with them, and how they left. When one reader's act changes the scene for everyone, the others there get a witness account, and readers who came in partway get theirs (`docs/COOP_ACCOUNTS_0_4_162.md`). The co-op line is added in `CoopAccounts`.
- **The House's read shades it.** Where it fits, AGE, HOME and the other traits change a detail the reader sees and their account follows it, as on State Route 9.
- **Companions.** A reader's companion waits at the approach (the door, the shoulder or the gate) with its orders standing. It is never harmed by the story.
- **New builds only.** A new layout version appends the slots; nothing standing is rebuilt.

---

## 1. The Supper

From the draft:

> *"The corn maze opens into a farmhouse clearing. You see the upstairs windows long before you find the porch. A fallen tree blocks the only drive, and you squeeze around it on foot. 'Wipe your boots' is the first thing you hear inside."*
>
> *"Give the first meal time to feel ordinary. A guest keeps checking the porch light for a ride. When their chair is gone and the host says the ride came, the drive gives the reader reason to doubt it. A brief view into the kitchen gives them proof before the meat is served. If they say the guest's name, one diner admits they heard them calling from the kitchen. Another says they thought the voice came from the corn. The walk in gives the reader something they can bring back to the table."*

It is the owner's own story. Its Witness kind is **understanding**: what the reader brings back to the table.

### The approach

- **The corn maze.** It begins at the House's door, at dusk, in the storm field's corn: three high, slow to push through, and bent where a reader has been.
  - Its paths are cut and trodden.
  - The farmhouse's upstairs windows are lit above the tassels from the first turn, so the reader always has a bearing. They are never close.
- **The clearing.** The maze opens onto a mown clearing round the farmhouse: a yard light on a pole, a pump, a dinner bell on a post.
- **The drive.** The only drive runs out of the clearing to a county road.
  - A fallen oak lies across it, its root plate standing up out of the ditch on one side and its crown in the corn on the other.
  - A reader squeezes round on foot between the roots and the ditch. Nothing on wheels could pass.
  - Its trunk carries the marks of an old storm (moss, a split), not a fresh cut.
- **The porch.** It is the clear decision. A doormat lies at the top of the steps.
  - Stepping onto the porch, the reader hears from inside: "Wipe your boots."
  - The porch light is on. A reader who turns back here simply leaves.

### The supper

**The household.** The household is original, cast phases to be assigned:

- **Ida Marsh,** the host: she sets the table, talks, and will not be hurried.
- **Royce Marsh,** her husband: quiet, carves.
- **Del,** their grown son: friendly, says too much.
- **Nana Marsh:** hard of hearing, sharp.

**The guest.** Corinne is a stranger whose car "quit on the county road" an hour ago. She keeps getting up to look out at the porch light, because someone is coming for her.

**The first meal is ordinary.** Readers take a chair: one each, up to four, and the table grows a leaf for more.

- Bread, a soup, and a jug passed hand to hand.
- Ida asks the readers where they have come from and listens.
- Corinne checks the window twice, says who is coming for her, and says it will not be long.
- Nothing is wrong yet, and nothing hurries it.
- A reader who leaves now leaves. Nobody stops them.

**The chair.** Between courses Corinne goes out to look at the porch light again. The household clears the soup.

- When the readers come back to the table, her chair is gone: not pushed in, but taken away, and the places closed up.
- Ida says, "Her ride came."
- Everyone at the table hears it.
- A reader who saw the fallen tree on the way in has reason to doubt it. Their account says they had seen the drive.

**The kitchen.** The kitchen door swings, and there is one brief look through it:

- the gap as Del comes through with plates;
- the serving hatch if a reader goes to it;
- or the window from the porch, if a reader goes out.

The proof is what is on the side by the butcher's block: Corinne's car keys on their fob, and her coat over a chair. It is never more graphic than that. A reader who has seen it knows before the meat is served.

**Her name.** At the table, a reader may say "Where's Corinne?" or "Corinne's still here" through the conversation panel.

- Del admits he heard her calling, from the kitchen.
- Nana says she thought the voice came from the corn.
- Ida says the meat is ready.

**The meat.** Royce brings in a roast. Each reader then does one of three things, and their account follows:

| What they do | Outcome | Kind |
| --- | --- | --- |
| Eat what was served (take the plate and eat) | `ate_at_the_marsh_table` | understanding |
| Get up and leave with the proof unspoken | `left_before_the_meat` | understanding |
| Lay the keys on the table in front of Ida (taken from the kitchen) | `put_her_keys_on_the_table` | understanding |

All three resolve one source, `the_supper` (built as `marsh_supper`, the place's id).

- A reader who eats wakes in the House with the taste, and their account says so plainly, without detail.
- A reader who lays the keys down ends the meal for everyone at the table.
  - The household goes still, and the porch light goes out.
  - The others at the table get witness accounts.
- A reader who walks out goes back through the maze in the dark. The farmhouse windows go out one by one behind them, and the House's door is where they left it.

### Together

- **One supper for the party.** There is one table, one Corinne and one meal.
- **The chair is gone for everyone.** One reader in the kitchen can be covered by the others keeping Ida talking: while a reader at the table is talking with Ida, she does not look up at the kitchen door.
- **The keys can travel.** A reader in the kitchen can pass the keys to one at the table, and either may lay them down.
- **Accounts, each from that reader's own record:**
  - whether they saw the tree;
  - whether they saw the kitchen, and how;
  - whether they said her name and who answered;
  - what they did when the meat came;
  - who laid the keys down, if anyone;
  - who was at the table with them.

  The roles are:
  - actor: the reader who laid the keys, ate, or left;
  - witness: a reader at the table when someone else laid the keys;
  - partway: a reader who came in after the chair was gone.

**The House's read.**

- **HOME = HAVEN.** Ida keeps a place set for the reader on later supper visits, and the account notices it.
- **HOME = DANGER.** Royce stands behind the reader's chair while carving, and the account says where he stood.
- **AGE.** The jug is milk or tea; the account says which the reader was poured. No age is named.

**Yield.** The Marsh dinner bell's clapper. Carried, it makes the House's doors to outside places a little more likely, and its sound comes from the corn.

**Assets.**

- A supper art tool: the farmhouse's materials; the doormat; the table leaf; the jug; the bread board; the roast under its cover; the keys and fob; the coat.
- The corn reuses the yard's stalks.
- Five skins painted by the character painter (Ida, Royce, Del, Nana, Corinne).
- Original subtitled sounds for the bell, the door swing and the wipe-your-boots line.

**Tests (literary).**

1. **One reader.** The maze to the porch, with the tree seen. The ordinary meal. The chair gone and "her ride came". The kitchen through the hatch. Her name and both answers. Each of the three endings, with its account.
2. **Two readers.** One keeps Ida talking while the other sees the kitchen and takes the keys. The keys are passed and laid down. Witness and actor accounts are written and the porch light goes out. A partway reader gets their own account.

---

## 2. In the Tall Grass

From the draft:

> *"A separate roadside threshold begins on the shoulder, where a boy sounds close enough to help. Let the player see the road from the first few rows. Once they commit, the road recedes even though the boy's voice stays close. In co-op, one reader can call from the shoulder. The first wrong bearing comes when the other reader hears that call ahead. At the end, another car pulls over and the player has a chance to shout something to the person getting out."*

The draft links Stephen King and Joe Hill's novella. Its shape is honoured here: a voice in roadside grass, a field that moves people, and the next car. Nothing is taken from its text, names or particulars. The Witness kind is **survival**.

### The approach

- **The shoulder.** A two-lane county road in the late afternoon. The reader's way out of the House is a car pulled onto the shoulder, its hazards ticking and a door open.
- **The grass.** Across the ditch, grass stands higher than a head, all the way to a line of telegraph poles a long way off. A boy's voice calls out of it: "Hello? Is someone there? I can't find the road."
- **The first rows.** These are the clear decision.
  - From the first few rows the road shows behind through the stems, with the car's hazards ticking.
  - A reader can step back out.
  - Once they are past the old fence line, a few rotten posts in the grass, they have committed.

### The field

- **The road recedes.** Past the fence line, the field moves its readers. Every few seconds out of sight of each other, a reader is set somewhere else in the grass, the same distance from the boy's voice and farther from the road. The grass is built as one field that the House rearranges around each reader.
- **The voice stays close.** It is always a little ahead.
- **The landmarks lie.** The road sounds from the wrong side. The telegraph poles are seen the same distance away from everywhere.
- **What can be trusted.** Trampled stems spring back slowly, so a reader's own track is seen for a while. A reader standing still and silent hears the car's hazards.

### Together

- **Calling from the shoulder.** One reader can stay on the shoulder and call into the grass through the panel ("Over here!", "Follow my voice!", "Can you see the car?").
- **The first wrong bearing.** The first call from the shoulder is heard by a reader in the grass from ahead of them, not behind. It is the first wrong bearing, as the draft asks.
- **After that.** Calls from the shoulder come from the true side only while the caller is standing still by the car.
- **Two readers in the grass** hear each other from wrong directions, and can find each other only by the tracks.

### The end

After the reader has followed the voice long enough, the field lets them come out at its edge, and another car pulls over on the road. Its driver gets out, looking at the grass, and calls, "Hello? Did somebody shout?"

The reader has one chance to shout something through the panel:

| What they shout | Outcome |
| --- | --- |
| "Stay by your car! Don't come in!" | `warned_them_off_the_grass` (the driver hesitates, gets back in and drives on) |
| "Help! In here!" | `called_them_into_the_grass` (the driver steps down off the shoulder toward the grass as the reader is let go) |
| Nothing | `kept_quiet_at_the_road` (the driver listens, hears the boy instead, and starts toward the grass) |

All three resolve one source, `tall_grass`. The reader is let out onto the road, with the House's door in the car's open door.

- **Who shouts.** In co-op, every reader in the grass at the end can shout, and the driver does what the first shout says.
- **Accounts.** Each account has who called, who shouted and what, and which way the driver went.
- **The boy.** He is never seen. His cap, small and wet, lies on the trampled place at the heart of the field, and a reader who picks it up has it in their account.

**The House's read.**

- **HOME = RUNAWAY.** The car on the shoulder is packed for leaving, and the account says the reader had somewhere to be.
- **AGE.** The car's radio plays the tape or the phone station; the account follows. No age is named.

**Yield.** The boy's cap. Carried, it lets the reader hear true bearings in the next field place they enter.

**Tests (literary).**

1. **Solo.** The road seen from the first rows. Committing past the fence line. The road receding while the voice holds close. The track springing back. The end, with each of the three shouts and its account.
2. **Two readers, one on the shoulder.** The first call heard ahead in the grass. Later calls true. Both resolve with their own accounts.

---

## 3. Growing Things

From the draft:

> *"A back fence opens onto a family yard. Familiar chores come first. Bring in washing and help move the kitchen table when the window frame bows. The action moves inside with Merry and Marjorie. Their father goes out to clear the back steps. By supper, the plants have lifted the kitchen floor enough that dinner has to be carried upstairs. Merry asks whether to bring his plate. Let the player answer by what they carry. The sisters give the vignette a connection to the Ghosts Set."*

The draft links Paul Tremblay on his story and the sisters. Merry and Marjorie are the Ghosts Set's Barrett sisters, already in the House with their own painted skins and voices. This story is set before the Ghosts Set's filming, and their father is John, as he is there. The Witness kind is **understanding**.

### The approach

- **The back fence.** A board fence with a garden gate, its latch held by a loop of twine. It is the clear decision: through it is the Barretts' yard. Behind the reader is the House's door in an alley.
- **The yard.** A family yard on an ordinary day: a washing line with sheets on it, a swing, a garden bed.
  - Pale shoots stand in the lawn where nothing was planted, more of them toward the house.
  - The back steps are greened over.

### The day

1. **The chores.**
   - **The washing.** Merry asks the reader to help bring in the washing. Sheets come off the line by hand, using the yard's sheet fitting. Shoots are already up round the line's posts.
   - **The table.** Inside, the kitchen window's frame is bowing, with shoots pushing at the sill. John asks for help moving the table away from it. It is a carry for two: two hands on one end, and it only moves if both ends are held. A reader alone gets John on the other end.
2. **Inside.** Marjorie is quiet at the window, watching the yard. Merry talks.
3. **The back steps.** In the afternoon John goes out to clear the back steps with a spade. The sisters watch him from the window.
   - He does not come back in.
   - His spade is later seen standing in the green where the steps were.
   - Nobody says what happened. Marjorie says he is "out with them".
4. **The floor.** Over the afternoon, which runs on its own occupied clock, the kitchen floor lifts. Its boards tent up in stages, and shoots come up through the gaps. By supper the floor cannot hold the table.
5. **Supper upstairs.** Dinner has to be carried up the stairs to the bedroom the sisters share: plates, the pot, bread.
   - Merry asks, "Should we bring Dad's plate?"
   - The reader answers by what they carry: his plate (it has his name scratched on its rim, as the family's plates do), or not.

### Endings

All three resolve one source, `growing_things`:

| What is carried up | Outcome |
| --- | --- |
| His plate, set at the place by the window | `carried_his_plate_upstairs` (the sisters sit with it; Marjorie says he will come up for it) |
| Everything but his plate | `left_his_plate_downstairs` (Merry looks back down the stairs at the green, and says nothing more) |
| Nothing: the reader leaves before supper is up | `went_back_through_the_fence` |

### Together

- **Everyone carries.** Every reader can carry. What reaches the upstairs table is what the party brought up, and his plate counts if anyone carried it.
- **The table carry.** The table needs two pairs of hands, which readers can be.
- **Accounts:**
  - what that reader carried;
  - who carried his plate, if anyone;
  - who helped with the table;
  - whether they saw the spade.

**The Ghosts Set connection.**

- A reader who has done Growing Things finds, on the Ghosts Set's fridge, a child's drawing of a green yard with a figure in it.
- Merry in the Ghosts Set looks at that reader once, as if she remembers them.
- Both are private to that reader.

**Yield.** A seed head from the yard, dry. Carried, it makes overgrown places a little more likely; planted, it never grows.

**Assets.**

- A growing-things art tool: shoots in four heights; the green on the steps; the tenting floor in three stages; the bowing frame; the plates with names.
- The sisters and John use their existing Ghosts Set skins at their ages there, so no new skin is needed.

**Tests (literary).**

1. **Solo.** The washing. The table with John. The steps and the spade. The floor lifting on its clock. Supper carried up, with and without his plate. The leaving ending.
2. **Two readers.** The table carried by both. His plate carried by one. The accounts for both.
3. **The Ghosts Set.** The drawing appears, privately, for a reader who finished.

---

## 4. The Last Crossing

From the draft:

> *"A farm road drops into floodwater. A power line carries the eye toward a station, a cable house, and a lamp on the far bank. Treat the flood station as its own crew crossing. Arrival through a readable yard route gives the party a place to gather before entering the three-day vignette."*

The owner's own design for it, "The Last Crossing" (a multiplayer vignette for four to six readers, three days and three nights, about thirty-three minutes), was found in the owner's Drive on 8 November. It is built from that design, not from the draft above. In brief:

- **The station and its keeper.** A House door opens onto a farm road dropping into floodwater. The crew follows the power line to a flood station: the keeper's office, the pump room and intake, the cable house across a narrow spillway, the lower service branches, and the ferry ramp and sluice. A lamp burns on the far bank. Vera, the keeper, takes the roll on a clipboard and still calls Ellis, the ferry hand who went under on a previous crossing.
- **Three days and nights.** A seven-minute day of repair, talk and testing, then a four-minute night of work under attack. Day One: jobs (intake worker, gauge reader, cable lookout, landing hand, and a fifth and sixth when present) and the two-knock signal through the speaking pipe. Night One: the authored first infection at the intake, and the rescue. Day Two: the winch puzzle and the first day someone can lie. Night Two: the first transformation at sunset, the pull under attack, a possible second infection in a crew of six. Day Three: launch the ferry or destroy the source. Night Three: the final rise and the crossing.
- **The handprint plate.** A clean palm fogs the near side; an infection grows a second print on the far side. It can be carried and set down, and near it a transformed reader chokes.
- **Transformed readers.** Water breathing, claws, the Call, garbled speech after dark, and a throat that closes while holding a flooded route shut.
- **Endings.** The ferry crosses, the station is cleared, or the source takes the crossing. The broken nameplate is shared out on a crew victory; the spare goes to a nominated reader; the Other Breath is the traitor's permanent bargain.

The draft's approach above becomes the station's own: the farm road into the flood, the power line as the bearing, and the station porch, where Vera takes the roll, as the gathering place and clear decision. The cable ferry is the climax, not the approach. It is dealt only to a crew of four to six readers who cross together (a default to edit).

---

## Build order and slices

1. **The Supper,** in two slices. *Built whole in 0.4.348 (`docs/MARSH_SUPPER_0_4_348.md`), both slices in one release; its story id is `marsh_supper`, the place's id.*
   1. The place, the maze, the drive and tree, the porch, the house and kitchen, the household and the guest, the ordinary meal, the chair and the kitchen view.
   2. Her name, the meat, the three endings, the keys, together, the accounts, the yield, and the tests.
2. **In the Tall Grass,** in two slices: the shoulder and the field's movement; the calls, the end, and the accounts. *Built whole in 0.4.349 (`docs/TALL_GRASS_0_4_349.md`), both slices in one release; the road runs east and west with the wagon on the shoulder and the grass beyond the ditch, the House's door in the brush across the road.*
3. **Growing Things,** in two slices: the yard and the chores; the clock, supper upstairs, the Ghosts Set drawing, and the accounts. *Built whole in 0.4.350 (`docs/GROWING_THINGS_0_4_350.md`), both slices in one release; the yard behind a garage alley, the house in the Ghosts Set's materials, and the drawing sent to the reader who finished alone.*
4. **The Last Crossing,** from the owner's design, in four slices, each with its own version, documentation and tests:
   1. The road, the station and its five places, Vera and the roll, the crew's reservation, the day clock, Day One's jobs, the speaking pipe and the handprint plate. *Built in 0.4.351 (`docs/LAST_CROSSING_0_4_351.md`); reached by the operator's test door until slice 4 deals it and adds its Witness source.*
   2. Night One: the surge, the authored infection and the rescue, the source's mobs, the infected reader's private symptoms, and the plate's reading. *Built in 0.4.352 (`docs/LAST_CROSSING_0_4_352.md`): the source's mobs are the Undertow; the cable house's door bar, the isolation valve and the first rise come with it.*
   3. Day Two and Night Two: the winch puzzle, branches and bypasses and the evidence they leave, the first transformation (water breathing, claws, the Call, garbled speech, the plate's ward, the choking gate), and the second slot. *Day Two built in 0.4.353 (`docs/LAST_CROSSING_0_4_353.md`): the winch, the two branches and their evidence, the Call by daylight; Night Two built in 0.4.354 (`docs/LAST_CROSSING_0_4_354.md`): the turn and the roll, the pull, the branches at night, water breathing and passages, claws, the night Call, garbled speech in chat as well as over the reader's head (owner, November 8), the choking grate and the plate, the grapple and the second slot.*
   4. Day Three and Night Three: the two plans, the launch and the gallery, the master bypass and the Other Breath, the three endings, roll calls, the nameplate and spare, each reader's account, and the Witness source.

Each slice ships with its documentation and tests and its own version. A story's Witness source is added only in the slice that makes it fully playable, so the quota never counts a story that cannot be finished.
