# Design proposals: the next series

Status: design for the owner's review. Section 1 (hazards) was implemented in 0.4.80 and section 3 (the paw) in 0.4.81; the rest is design only. Each section ends with the decisions it needs.

This series covers:

- more varied hazard maps;
- one of *The Shadow over Innsmouth* or *At the Mountains of Madness*;
- *The Monkey's Paw*;
- *The Lottery*;
- *I Have No Mouth, and I Must Scream*;
- a story from Mariana Enríquez.

## House rules that apply to every proposal

Everything below is designed to the rules already in `AGENTS.md`:

- **Multiplayer and honesty.** Design for multiplayer first. Progress is personal and honest: watching a peer earns nothing, and spectators earn nothing.
- **Retreat.** Nobody is stranded. An unfinished visit can always be walked out of until a reader commits to a beat themselves.
- **Supplies.** Supplies are finite and never restock.
- **Mystery.** The House foreshadows and does not explain.
- **Sound.** Every custom sound has a subtitle.
- **Saved worlds.** Changes to a saved world happen once, and only when the area is loaded, unwatched and empty.
- **Textures.** Environment textures are drawn in code as vanilla-style pixel art. Character skins and hand-held artifacts start as placeholder files the owner replaces; the generator never overwrites them.

**Rights.** *The Monkey's Paw* (1902) is in the public domain. Lovecraft's two novellas were published in 1936, and their US status depends on renewals, so treat them as still protected until 2032. *The Lottery*, Ellison's story and Enríquez's stories are in copyright. Every vignette here therefore takes only the shape of its source: original names, original text, no quotations, no titles shown in the game.

**Witness.** Each vignette below is one new source with one personal resolution, however many choices it offers. If all five vignettes ship (one of the two Lovecraft pieces, plus the Paw, the Lottery, the machine and the Enríquez house), the pool goes from 43 to 48 sources. The quota, 75% rounded up, goes from 33 to 36, still across at least two kinds. Hazard maps are not sources.

---

## 1. More varied hazard maps

### What there is now

There are eight gray hazard rooms, each one fixed layout in one slot:

- flooded passage;
- fractured walkway;
- compression passage;
- false distance;
- light sink;
- moving threshold;
- duplicate passage;
- gravity drift.

They come in at route depth 6, 10 and 14. Each costs something: breath, health, light, time, orientation or confidence, but never a life outright. The problem is repetition: by the third flooded passage a reader knows it by heart.

### Proposal A: a second layout for every existing hazard

Each hazard gets a sibling map in its own new slot, with the same rules and a different shape. The dealer picks between siblings by route, so a shared route still shares a hall.

| Hazard | Today | Sibling layout |
|---|---|---|
| Flooded passage | A straight channel | A bent cistern that steps down and up again, with air pockets under three ceiling vaults; you swim from pocket to pocket |
| Fractured walkway | A straight broken span | A spiral walkway round a pit, broken in three places; the gaps are on the outside of the turn, so the pull is outward |
| Compression passage | Straight walls closing | An L-bend whose walls close from the corner outward, so the safe end is the one you were walking away from |
| False distance | A corridor that laps you back | A corridor that stretches: the far door recedes as you walk at it and comes closer as you walk backwards. Walking backwards works, and nothing says so |
| Light sink | A room that eats light | One wall sconce survives anywhere in the room, and it moves to whichever light is warmest. Leave your torch in the right place and walk out by its light |
| Moving threshold | The door cycles among three places | The door moves only while nobody is looking at it (cameras and spectators count, as in the child's room). Two readers watching different walls pin it |
| Duplicate passage | Three identical benches | Three identical rooms in a row, one missing a detail (a clock, a plant, a picture). Only the door in the room missing something is real; the others lead back to the start |
| Gravity drift | A pull toward one trench | The pull turns a quarter every twenty occupied seconds. The ledges and ladders are laid so a reader can always reach shelter before the next turn |

### Proposal B: eight new hazards

Each costs something the current set doesn't touch, so a route's hazards feel different from one another, not only harder.

1. **The cold store** (tier 1; costs warmth).
   - **Layout:** a meat locker forty blocks long: hooks, frosted crates, a compressor at the far end. It uses Minecraft's own freezing (the powder snow shiver, slowing and frosting over).
   - **Heat:** heat lamps every eight blocks thaw whoever stands under them, and two readers standing together freeze at half the rate.
   - **The door:** the far door opens on the compressor's cycle: it shudders, falls silent, and the latch lifts for ten seconds.
   - **Limits:** freezing damage stops at three hearts and never kills.
2. **The listing hall** (tier 1; costs footing).
   - **Layout:** a ship's corridor that lists one way and then the other on a slow clock.
   - **Sliding:** while it lists, everyone slides toward the low side unless crouching or holding a rail, and dropped items slide all the way. The low side is a shallow bilge, wet and harmless.
   - **Crossing:** you cross on the level moments, or crouch along the rails.
3. **The quiet hall** (tier 1; costs noise).
   - **Layout:** a long gallery with real sculk sensors under the boards. Running, jumping, opening and dropping things all register on a shared meter.
   - **When it fills:** the lights go and something long walks the length of the hall in the dark. It touches no one, but every reader it passes is sent back to the entrance.
   - **Multiplayer:** one noisy reader resets everyone, so it's a cooperative room.
4. **The unwatched maze** (tier 2; costs orientation).
   - **Layout:** a 6×6 maze whose wall panels slide on a shared clock, but only where no camera can see them.
   - **Holding it still:** alone, you hold only what you look at. With two readers back to back, most of it holds.
   - **Chalk:** chalk marks (the existing chalk) stay on the panels, so a moved wall gives itself away.
   - **Safety:** a wall never moves in a way that cuts any reader off from the exit; every arrangement keeps a path open.
5. **The mirror hall** (tier 2; costs confidence).
   - **Your reflection:** one wall is a mirror. Your reflection (a private figure wearing your own skin, the way the doubles already work) copies you a beat late, then two beats.
   - **The doors:** at the end are three doors. In the mirror, your reflection stands still in front of one of them; that one is real, and the others loop.
   - **Multiplayer:** each reader sees only their own reflection.
6. **Heavy and light** (tier 2; costs movement).
   - **Layout:** two rooms joined by a door, using Minecraft's gravity, jump and fall-damage attributes.
   - **The heavy room:** you can barely jump, ladders are slow, and the route goes along the low ledges.
   - **The light room:** long floating jumps over a gap, with a down-draught over the gap's middle that pulls a careless jump into a net.
   - **Safety:** both rooms reset your attributes on leaving, on death and on logout.
7. **The nested rooms** (tier 3; costs time and nerve).
   - **Layout:** the same parlour four times, each through a smaller door. The reader shrinks with each room (the scale the children use, down to about a third). The furniture doesn't, so the last door is a gap behind the skirting board.
   - **Leaving:** leaving restores the reader's scale over a few seconds, and so do death and logout.
8. **The counting stair** (tier 3; costs time and hunger).
   - **Layout:** a stairwell whose landing plaques all read 4.
   - **The way out:** the House counts each reader's real landings privately. The seventh landing's door, counted from where they entered, is the way out (SEVEN again). Any other landing door returns them to the top.
   - **No clue:** nothing on screen counts for you.

### Pacing

The existing presets (hazards at 28/42/54% with two visits' spacing) stay as they are; more kinds of hazard should not mean more hazards. The new hazards join the tier pools:

- **depth 6:** cold store, listing hall, quiet hall;
- **depth 10:** unwatched maze, mirror hall, heavy and light;
- **depth 14:** nested rooms, counting stair.

The eight siblings join their originals' tiers.

### Technical shape

- **Slots:** sixteen new gray slots appended after 77, each with a layout bump (41). They are carved asynchronously in slices like the 0.4.48 halls.
- **Saved worlds:** discovered links and saved maps stay exact.
- **Assets:** all new art is environment art drawn in code: frosted crates, ship's plating, sculk boards, maze panels, mirror glass, landing plaques.

### Decisions

- Proposal A, B or both? Recommendation: both, with B first (it adds the most variety per slot).
- Is the attribute-based pair (heavy and light, nested rooms) in scope, given it changes a player's real attributes for a while?

---

## 2. Lovecraft: Innsmouth or the Mountains

Both are designed below. **Recommendation: the Mountains now; Innsmouth later or not at all.**

- **Against Innsmouth now:** it overlaps heavily with what the House already has. Proofrock is a large town built for a chase, the drowned congregation sits under the lake, and the Shallows are a sea of faces.
- **For the Mountains:** they bring a new biome (polar ice and a black city), a new way to understand a place (reading a history in murals), and a different kind of chase (a tunnel that fills).

### 2a. *At the Mountains of Madness*: "The Black City"

**Kind:** understanding. **Shape:** outdoor, multi-visit, about 128 × 192.

**The place, in five stages:**

1. **The camp.**
   - **Setting:** wind and blowing snow (the hotel blizzard tech). Torn tents, a plane with its fabric ripped, a dog line with empty harnesses, a geology tent full of cores, and a dissecting table under a tarp with the straps cut.
   - **The source:** the radio log in the radio tent, the expedition's last messages in their operator's hand.
2. **The pass.**
   - **The climb:** ropes and cut steps between peaks too regular to be natural.
   - **The wind:** it pushes as the cave's breath does. Sheltered cells stop the freezing, and exposed ridges freeze you slowly, never fatally.
3. **The city.**
   - **The stone:** black stone terraces carved in fives, on a scale that makes the reader small, with a great ramp spiralling down into the middle.
   - **The murals:** twelve carved panels along the ramp tell a history in original pixel art, with no words: arrival from the sky, building, making servants, the servants rising, the cold, retreat to a sea under the city.
   - **Reading:** a reader reads a panel by standing near it and looking at it for three seconds. The panels must be read in order, and the reading is personal.
4. **The tunnel.**
   - **Below the city:** warm air, blind pale birds (sounds and footprints, no bodies) and a piping call in the dark (original, subtitled "Something pipes in the dark").
   - **The mass:** then a black, iridescent mass fills the tunnel from wall to wall, eyes forming and closing in it, pushing the air ahead of it.
   - **The chase:** you run back up the ramp as the lights die behind you. It never catches a reader who keeps moving.
   - **If caught:** a reader who stops is caught in black and wakes at the camp, unharmed, with every mural read still read (as the caver's blackout works).
5. **The pass at dawn.** On the way out, a reader can look back at the far peaks for three seconds. What they see is not explained: the mist moves wrongly. That look is the last beat.

**Resolution:** all twelve murals read in order, the tunnel survived, and the look back from the pass. The account (*the_black_city*) waits in the radio tent, written in the reader's name.

**Multiplayer:**

- **Shared:** the wind, the cold and the chase. The mass follows the slowest reader but never touches anyone.
- **Personal:** the murals, the blackout and the look back.

**Assets:** black basalt, carved panels, ice, snow drift and tent canvas, all drawn in code; twelve mural panels; the mass as a custom entity whose texture is a placeholder for the owner. Sounds: wind, the piping, the mass's wet push, and the plane's fabric flapping, all synthesized and subtitled.

**Decisions:**

- Is the mass's design (an entity texture) the owner's?
- Should the look back show anything at all, or only mist?

### 2b. *The Shadow over Innsmouth*: "The Harbour Town"

**Kind:** survival. **Shape:** outdoor, one-shot, about 128 × 128.

**The place:**

- **The town:** a dying fishing town reached by one bus at dusk. Harbour, a smoking refinery, a reef offshore where shapes break the water at night, and a lodge with pillars that used to be a church. Gambrel-roofed houses, shuttered (the roof tech exists), and a grocery whose young clerk is an outsider and warns you.

**How it plays:**

1. **The tale.**
   - **The teller:** an old drunk on the wharf tells the town's history in exchange for a bottle, as a multi-page book in his voice, original text.
   - **The bottle:** any bottle will do, including one carried out on the Last Stop's tab.
2. **The night.**
   - **Stranded:** the bus breaks down, and the reader takes a hotel room.
   - **The door:** footsteps in the corridor, then the doorknob is tried.
   - **The escape:** through connecting doors, bolting each one behind you, with bolts that really hold for a few seconds. Then out of a window onto the roofs.
3. **The streets.**
   - **The search:** searchers with lanterns. Their light is what sees, so crouching in shadow works.
   - **Hiding:** a fish shed to hide in.
4. **The railway cut.**
   - **The procession:** a shuffling procession crosses the road above.
   - **Staying hidden:** lie flat (the crawl pose) and don't look. Looking too long blurs and darkens the view until the reader faints.
   - **Fainting:** a reader who faints wakes at the far end of the line, escaped but having seen.

**Resolution:** out along the railway line, seen or unseen; one source (*the_harbour_town*). Afterward, as foreshadowing with no mechanic, the reader's reflection in the House's windows sometimes shows faint lines at the neck. It is private and comes back now and then.

**Overlap:** Proofrock, the drowned congregation and the Shallows already cover much of this. If it is built, it should stay at night and in the hotel and streets, with no lake.

---

## 3. *The Monkey's Paw*: "Three Fingers"

**Kind:** release. **Shape:** an interior cottage, multi-visit, small (about 25 × 30). The story is in the public domain, but the text and names are still original.

**The place:** a cottage parlour on a wild night. Fire, a chessboard mid-game (the chess table exists), wallpaper and a mantel. The father, the mother and a grown son who works at the mill. Visit one brings a visitor: an old soldier back from abroad, who tells stories, shows a withered paw, and throws it into the fire.

**How it plays:**

1. **Taking the paw.** A reader can pull the paw from the fire, costing half a heart for the burn. It is a personal, finite item with three fingers, one per reader, never restocked. The item's art is a placeholder for the owner.
2. **The first wish.**
   - **The choice:** right-clicking the paw offers wishes phrased from the reader's own real circumstances. For example: to find the way out; for enough money; or, only if the House's records show it, to have back a companion that died.
   - **Riches:** the wish comes true in the House's way. A wish for money brings a real, finite pouch the next visit, delivered by a stranger who says there was an accident at the mill. The son's chair is empty from then on.
   - **A dead companion:** a wish for a dead companion brings only its collar and name tag to the step at dawn. The House's rules don't change: nothing is raised.
3. **The second wish.**
   - **The plea:** the mother asks the reader to wish her son back. They may refuse.
   - **The knocking:** if they wish, that night the knocking starts at the cottage door, heard by the wisher alone; peers hear only wind. It slow at first, then hammering.
   - **The plea to open:** the mother begs them to open it.
4. **The third wish, or the door.**
   - **Wishing:** the reader can make the last wish while the knocking goes on, and it stops.
   - **Opening:** or they can open the door, onto an empty road and a lamp flickering.
   - **The paw:** either way, its last finger falls.

**Resolution:** either the last wish made before the door opened, or the door opened onto the empty road. One source (*three_fingers*), different account text for each.

**Multiplayer:** the family is private to each reader (as the cabin's strangers are), so one reader's wish never empties another reader's chair. The knocking is private; only the fire and the storm are shared.

**Decisions:**

- May a wish draw on a reader's real losses (a dead companion's name, from the hotel's records)? It is the strongest version, and the most personal.
- Should the money be a real reward, or only the knowledge of what it cost?

**Status:** implemented in 0.4.81 ([the cottage record](THREE_FINGERS_0_4_81.md)). The owner chose real losses (every wish still corrupted) and a small real purse. A third wish, the storm to stop, and a third ending, the paw given back to the fire, were added. The wind is private like the knocking, so one reader's wish never stills another's storm.

---

## 4. *The Lottery*: "The Drawing"

**Kind:** understanding. **Shape:** an outdoor village square, recurring, about 96 × 96. Copyrighted, so only the shape is used: no names, no text, and not the story's date.

**The place:** a bright summer morning, the only daylight vignette apart from the plain. Post office, bank, grocery, a square with grass between the stones. A black box on a three-legged stool, chipped and splintered. Families arriving in ones and twos. Children filling their pockets with smooth stones and piling them by the wall.

**How it plays:**

1. **The morning.**
   - **The people:** the reader can talk to the villagers. They are friendly and ordinary, talking about tractors, taxes and rain.
   - **The woman:** one woman arrives late, flustered, laughing, and gives the reader a lemonade.
   - **The skins:** the villagers wear placeholder skins; the existing strangers can stand in at first.
2. **The draw.**
   - **The heads of household:** the official calls each household head by name from a list. This year the list includes the reader, by their real name, as a new family.
   - **The slip:** the reader draws a folded slip, private and blank.
   - **Waiting:** nobody opens theirs until everyone has drawn.
3. **The family draw.**
   - **Who it marks:** the marked household is always the late woman's, never a real player.
   - **Her slip:** she draws the spot.
   - **The stone:** the crowd turns and a child presses a stone into the reader's hand, a real item.
4. **The choice.**
   - **What a reader can do:** throw it, drop it, or keep it and walk out through the crowd, which parts silently. Nobody is ever stopped from leaving.
   - **The throw:** the scene cuts before anything is seen.

**Recurring:** the drawing happens on every visit. Each year the village is a little emptier, the box a little shabbier, and the late woman's place in the crowd taken by someone the reader met the year before.

**Resolution:** standing the day to its end, whatever was done with the stone; one source (*the_drawing*). The account names what the reader did with the stone and is honest about it. Witness credit doesn't depend on the choice, so no choice is the "right" one.

**Multiplayer:** each reader draws their own slip. Everyone sees the same marked household. Stones and choices are personal: peers see each other's choice but take nothing from it.

**Decision:** should a real player ever be the one marked, when two or more readers are present? It is the strongest version, but it turns the crowd on a person. **Recommendation: never.**

---

## 5. *I Have No Mouth, and I Must Scream*: "The Kept"

**Kind:** release. **Shape:** interior, multi-visit, large (about 64 × 96 with folds). Copyrighted, and the estate is protective, so: no names, no monologue, and not the title phrase anywhere in the game.

**The place:** a machine the size of a country. Corridors of humming cabinets and cable, cathode screens that wake as a reader passes, rooms that are landscapes a mind has imitated badly (an ice cave, a jungle made of wire, a dining room with a hundred empty chairs). The machine speaks in its own new hand, in text on the screens and as captions.

**How it plays:**

1. **The captives.**
   - **Who they are:** four captives who have been kept here longer than a lifetime, starving and bickering.
   - **The promise:** the machine has promised them food at the end of a long walk. They walk with the reader.
2. **The walk.**
   - **The rooms:** each room is a trial built from the reader's own record, using the House's true facts (the way the staircase prose already does).
   - **What it says:** the machine says, truthfully, how many times the reader has died, which companions they lost and which vignettes they walked out of. It never invents anything.
   - **The ice cave:** costs warmth.
   - **The wire jungle:** a crawl.
   - **The dining hall:** the chairs face the reader.
3. **The food.** At the end is a finite crate of cans, with no way to open them; right-clicking says so. The captives break.
4. **Release.**
   - **Asking:** one by one the captives ask the reader to let them go.
   - **Releasing:** at each one's cell, the reader closes their eyes and opens the door. The captive walks out into white and is gone. It is a release, not a killing.
5. **The punishment.** The machine notices the reader. What it does is the decision below.

**Resolution:** all four released (*the_kept*). A reader who refuses can leave with no credit, and come back.

**Multiplayer:** the captives are shared, but each release is the releasing reader's own, and the punishment is personal. Possibly, in multiplayer, the captives wear the readers' own skins: the machine keeps copies of you. In single player they wear placeholder skins.

**Decision: the punishment.** The source ends with the narrator remade without a mouth.

- **(a) Temporary:** while in the machine, the reader's face renders blank and their chat inside the vignette shows as "…". Both end on leaving.
- **(b) Persistent, like the cabin's lost arm:** the face renders blank everywhere for others and for the reader in the mirror, until something in the House restores it (a later bargain).
- **(c) Persistent face, with chat untouched anywhere.**

**Recommendation: (c)**, as body changes already work in the House: permanent, visible, and leaving how people talk to each other alone.

---

## 6. Mariana Enríquez

**Recommendation: "Adela's House"** (in *Things We Lost in the Fire*). Children dare each other into an empty house in their street; inside are shelves of jars, teeth and fingernails; the girl who loved the house walks into one last room and is never seen again. It is a house that takes people, which is The Oldest House's own subject. The girl in the story has one arm, which meets the cabin bargain's lost arm head on (see below).

The other stories considered:

- **"The Dirty Kid":** a street child and a woman's guilt. Powerful, but its horror is a murdered child, and the House would only be borrowing it.
- **"Under the Black Water":** a drowned boy returns from a poisoned river. It overlaps the House's drowned scenes.
- **"The Inn":** a haunted former police station. It rests on the dictatorship's torture, which needs care the House's form may not give it.
- **"The Neighbor's Courtyard":** a chained child glimpsed next door. Too close to real abuse to make playable.

### "The House on the Corner"

**Kind:** memory. **Shape:** outdoor street plus interior, multi-visit, about 64 × 96. Copyrighted, so only the shape is used, with original names.

**The place:**

- **The street:** a city street at siesta: low houses with balconies, jacaranda petals in the gutters, a kiosk, a dog asleep.
- **The house:** on the corner, an empty house with a tall gate and a front garden kept by nobody. It has never been for sale and never been lived in.
- **The children:** three kids on the kerb. One of them, a girl with one arm who tells a different story about the arm every time, loves the house.

**How it plays, visit by visit:**

1. **The dare.**
   - **The front room:** the kids take the reader through the gate. The front room is clean and lit, though the power has been off for years.
   - **The shelves:** along one wall are shelves of glass jars: teeth in one, fingernails in another, a third with something the reader can't make out. The jars are environment art drawn in code.
2. **The corridor.**
   - **Too long:** the corridor is longer than the house could hold.
   - **Her hand:** the girl wants to go further and holds out her hand. A reader who takes it (by interacting) walks with her, as a gentle leash; letting go is always possible.
   - **The others:** the other two kids want to go home.
3. **The last room.**
   - **The door:** she walks into the last room and its door shuts. A reader can open it at once: the room is empty, and smaller than its door.
   - **The garden:** outside, the garden has grown over the path.
4. **Afterward, on later visits.**
   - **The street:** the street at night, the house's windows lit.
   - **The brother:** the girl's brother, now older, sits on the kerb and tells the reader what he has been hearing.
   - **The jars:** the jars on the shelf are one more each time.

**Resolution:** one source (*the_house_on_the_corner*), reached either way:

- by holding her hand to the last room and then opening its door;
- or by letting go and waiting outside until the door opens onto the street.

The account is the reader's own memory of her, in her stories about the arm.

**The cabin's arm:** if a reader gave an arm in the cabin bargain, the girl notices ("We match") and tells them the true story of hers, the only time she does. This uses only the House's real record, and it is foreshadowing, not explanation.

**Multiplayer:** the street and house are shared. The girl walks with one reader at a time, the one holding her hand; the others see her go in. Each reader's last room is their own.

**Decisions:**

- Is "Adela's House" the right Enríquez story?
- How close to the jars and their contents do we go?

---

## Scope and a possible order

| Piece | Size | New art | New sounds |
|---|---|---|---|
| Hazards B (eight new) | M | Environment only | About 6 |
| Hazards A (eight siblings) | M | Mostly existing | About 2 |
| Three Fingers | M | Cottage plus paw (owner) | About 5 |
| The House on the Corner | M | Street, house, jars; kids (owner) | About 6 |
| The Drawing | M | Village, box; villagers (owner) | About 5 |
| The Black City | L | Ice, basalt, murals; the mass (owner) | About 6 |
| The Kept | L | Machine halls; captives (owner) | About 6 |
| The Harbour Town (if chosen) | L | Town, lodge; townsfolk (owner) | About 7 |

A possible order, smallest risk first:

1. Hazards B and Three Fingers.
2. The House on the Corner and The Drawing.
3. The Black City.
4. The Kept.

Each release updates the Witness counts (48 sources / 36 resolutions once all five vignettes ship), the checklist, `AGENTS.md` and native coverage, as every vignette release does.

## Decisions in one place

1. Innsmouth or the Mountains? (Recommendation: the Mountains.)
2. Which Enríquez story? (Recommendation: "Adela's House".)
3. Paw: may wishes use a reader's real losses?
4. Drawing: is a real player ever the one marked? (Recommendation: never.)
5. Kept: the punishment, temporary or persistent, and does it touch chat? (Recommendation: a persistent blank face, chat untouched.)
6. Hazards: the same frequency with more variety (recommended), or more often? Is the attribute-changing pair in scope?
