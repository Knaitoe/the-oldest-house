# The House gets a read on you

Status (October 28): partly built. The concept, the five rooms and their texts are the owner's; the texts are quoted exactly. The owner's answers to the six open questions (October 27) and the owner's placement and co-op direction (October 28) are listed at the end. This document adds how each room is built and watched, what the House keeps, how co-op works, how the House shows its version of the reader, and where the read pays off. It also sets the bar for whether the idea is worth building, and a first slice to test that.

What is built, in 0.4.138 to 0.4.142 (see "Built so far" at the end):

- all five rooms, as a private copy per reader;
- each room records the reader's literal actions;
- six scenes that read a saved category and stay neutral without one.

What is not built:

- turning actions into categories, which the owner deferred, so no reader has a category yet;
- the account, the House of Leaves, mirrors and doubles reading the record.

A review of the built rooms against this document, with a suggested order for what comes next, is in `docs/PERSONALITY_VIGNETTES_REVIEW.md` (October 28).

The October 28 direction changes where the rooms open, how co-op works and what a finished room becomes. Four parts are built: the quiet rooms open from the junction and a finished story is only a passage (0.4.143), every shared scene has a way on into the halls (0.4.144), and the shared foundation of co-op (0.4.146): friends follow each other into a vignette, mixed groups are dealt something none has started or split, threads, and accounts for each way of being there. The monkey's paw is the first scene made shared, with its drafted accounts (0.4.147, `docs/SHARED_PAW_0_4_147.md`), the drowned town's ending the second (0.4.148, `docs/SHARED_DROWNED_TOWN_0_4_148.md`), the child's room has accounts for how each reader was there (0.4.149, `docs/SHARED_CHILD_ROOM_0_4_149.md`), and the Goatman's extra readers take cousins' places (0.4.150, `docs/GOATMAN_CAMPERS_0_4_150.md`). Sections below describe the intended design, and each says where the build differs today.

## The idea

The House knows what Minecraft records about a reader: deaths, pets, stats, screenshots, names. It knows nothing about the person. A prompt or a "choose your character" room would break the illusion. Instead the House learns the way a stranger does when you visit their home: by watching what your hands reach for out of habit, where your eyes linger, and what kind of silence you are comfortable in.

The first five rooms a reader reaches are quiet, ordinary and not horror. Each is a domestic moment with nothing to solve: getting ready for bed, waiting to be called, borrowing a dry coat, sheltering from a storm, being called in for supper. While the reader moves through it, the House notes the habit and files a category. Later rooms, the reader's account and their House of Leaves are all written with what it learned.

## It is only worth building if the read shows

Five pleasant rooms and a database are not worth the work. The idea earns its place only if:

1. **The House of Leaves is written with it.** Every reader's five leaves draw on their own habits (see "Where the read pays off").
2. **The personal account opens with it.** The House recites the reader's habits back to them.
3. **Every trait changes at least one thing a reader will notice** in a normal playthrough, plus smaller touches elsewhere.
4. **Two friends who came in together finish with different books and different accounts**, and comparing them is interesting.

The cost is mostly writing. Every place that reads a category needs its present text (for readers the House couldn't read) plus a variant per category. If that writing can't be funded, the rooms shouldn't be built. The first slice (at the end) tests the bar before the rest is committed.

## Rules for all five rooms

- **No prompts, no wrong answers, no fail states.** Every choice opens the way on, and so does doing nothing. A reader can always go back the way they came.
- **A way forward.** Every room has an exit that leads on into the ordinary halls, not only the door back (owner, October 28).
- **Not stories.** No source, no account of their own, no Witness credit. The pool stays at 54 sources and 41 resolutions across two kinds.
- **The first instinct counts.** What sets a category is the first deliberate act in the room's window. Later poking around is kept as an observation only.
- **Each reader is alone.** Every reader gets their own copy of each room (see "Co-op"), so the House only ever watches one person and nobody's habits are filed under someone else.
- **Private.** Figures are owner-bound actors, sounds are private (`LiteraryVignettes.heard`), and nothing about a reader's categories is shown or said to anyone else.
- **The notes come after, without leaving the room.** Each room's text describes the test, so read beforehand it would tip the reader off. Each note stays in the object the owner placed it in, showing its blank side first:
  - the postcard in the mirror frame shows a picture of a lake;
  - the calendar behind the reception glass shows only its month;
  - the slip in the umbrella stand is folded;
  - the jukebox lid shows only its song strips;
  - the porch rail is plain wood.

  Once the room's first signal is recorded (or its window closes), the note gains its writing the next time the reader isn't looking at it. Looking back, the postcard has handwriting on it. As everywhere in the House, the change is never seen happening.
- **Non-horror.** None of the five ends badly. The unsettling part comes later, when other rooms turn out to know.

## The first rooms

These are the first five rooms a reader reaches after the opening (the three days, two sleeps and the letter are unchanged).

- **Order:** the waiting room, the bathroom, the coatroom, the shelter, the porch. Later rooms can use earlier answers: the shelter's strangers can be the reader's age because the waiting room came first.
- **Where they open (owner, October 28):** the impossible hallway stays a plain approach. The rooms lead out from the first room past its far door, the junction. Each room's forward exit opens into the ordinary halls, so the rooms are naturally spaced and the reader keeps moving forward. Stories still begin at depth 6 as now.
- **Missed rooms:** content may be missed, as elsewhere. A reader who goes back never sees a room twice.

Built in 0.4.143 as a chain: until a reader has entered all five, any of the junction's onward doors leads into their next room in order, each room's way on leads into the next, and the porch's way on arrives in the hall the junction door was dealt. The 0.4.142 hallway doors are walled up unseen in saved worlds (`docs/JUNCTION_CHAIN_0_4_143.md`).

## Co-op: these rooms stay private, other vignettes are shared

The five quiet rooms stay private (owner, October 28). They define the character, so every reader gets their own copy and the House watches one person at a time. Friends who cross a door together each arrive in their own copy and meet again in the hall beyond. Today's copies, 4,096 blocks from the next reader's and built in bounded slices like `BorrascaSites`, already work this way.

The owner's October 28 direction makes the other vignettes shared:

- **Shared by default, with witnesses.** Whoever acts on the key beat leads it, and everyone else in the room witnesses it. Only Backseat (one driver's seat) and the five quiet rooms stay private.
- **Witnesses get credit.** A witness gets their own account and the Witness credit. The account may change subtly, reading as someone who watched rather than the one who acted.
- **Each reader's own record.** A peer's act never counts as the reader's own act, in the House's read or in a scene's choices.
- **A finished room becomes a passage.** Once a reader has completed a vignette, its door no longer replays the scene for them. It is just a door connecting two halls. Built in 0.4.143.
- **Mixed groups.** When a group reaches a door and some of them have already finished what lies behind it, the House deals a vignette none of them has seen. If there is none, the group splits: those who finished pass through, and the others get the scene.
- **Forward exits** lead back into the ordinary halls. Built in 0.4.144: each shared scene has one way on, placed to suit it, dealt only ordinary halls and held by the entry's locks (`docs/ONWARD_DOORS_0_4_144.md`).
- **The owner's notes on particular scenes:**
  - **Goatman** can eventually be shared. Extra players take the place of other campers, so the headcount stays the same as solo. The count, seating, supper and dialogue must treat each player as that cousin. Built in 0.4.150: readers who arrive with the latecomer each take a cousin's place; that cousin is not staged, and the reader has their plate, chair and bunk. The second takes the place of the cousin who goes for gas: they fill the generator in the shed, must be let back in, and their return sends the thing out past them (owner's note). Any reader can fetch the condiments.
  - **The drowned town and lake-witch scenes** should mostly support several players. The drowned town already ran as one story for the world; 0.4.148 drafts its ending for everyone in Proofrock when the roof opens: a witness's account for those there for the essays, a late joiner's for those who came after.
  - **The monkey's paw:** everyone hears the story, and only one reader holds the paw. The finger clench shows as an animation on the paw in that reader's hand, so the others see it happen. Built in 0.4.147.
  - **The child's room** can be shared. It already was one room for everyone in it, and everyone shut in must crawl out for themselves, so it has no witnesses; 0.4.149 writes each reader's account for who was with them, who began it, whether they came in late and whom they followed down or left behind.
- **Built in 0.4.146** (`docs/COOP_FOUNDATION_0_4_146.md`): a door into a shared vignette is held 30 seconds for the group, so friends through the same door arrive in the same scene. A mixed group at a door is dealt a vignette none of them has started, or splits. The backseat, the silo, Borrasca, the copies and the Red Room stay private; the Goatman and hide-and-clap are held back until they are made shared (the Goatman is shared from 0.4.150). Each reader still plays the scene as their own until it is drafted.
- **Still open:**
  - how a shared room is built and reset when friends arrive at different times;
  - who a scene responds to when two readers act at once.

### Multi-visit vignettes: one story per thread

The owner approved this model (October 28). Built in 0.4.146 as `VignetteThreads`: threads, visits, partway joiners, a beat waiting for its holder, and the holder's object going back after three in-game days away. Each scene supplies its own beats and release.

- **A thread.** The readers present on a vignette's first visit share one story, called a thread. Each reader is in at most one thread per vignette.
- **Picking up the story.** Whoever from the thread comes back, alone or together, continues it where it left off. Nobody gets a fresh copy of a story they have already begun.
- **Beats that need one person wait for them.** The monkey's paw's wishes need its holder. A visit without them plays only what doesn't need them: news arriving, the empty chair, the knocking.
- **No idle visits.** If the next beat needs someone who isn't there, the House deals the arriving readers something else.
- **A holder who never returns.** After a while the held object goes back where it came from (the paw to the fire) for whoever is there, so no thread is stuck.
- **Different threads arriving together** follow the mixed-groups rule: deal something none of them has started, or split them.
- **Late joiners.** A reader who has never seen the vignette and arrives with a thread partway through joins it as a witness from that point.
- **Missed endings.** A thread member who was elsewhere when the story ended can visit the aftermath later. They get a viewer's account and the Witness credit.

The paw, for example:

1. Two readers hear the sergeant together, and the first pulls the paw from the fire and becomes its holder.
2. If the second returns alone after the first has wished, they see that wish come home and their account says they watched it.
3. If they return before any wish, the House deals them another vignette instead.

### Accounts for every way of being there

Each shared vignette's account must be properly drafted for every way a reader can have been part of it (owner). Built in 0.4.146: the Witness record keeps each reader's role beside the story, and a scene registered with `SharedVignettes.draft` supplies its line for each role. Only a drafted scene gives a reader who did not act the credit. The variants are:

- **Actor:** the reader did the key act (today's accounts).
- **Witness:** the reader was there throughout and watched another reader act.
- **Partway:** the reader joined the thread partway through.
- **Aftermath:** the reader came back after the ending happened without them.
- **Absent:** the reader saw the consequence of something done while they weren't there.

Each variant is written prose in the account's own voice. It is not the actor's account with the subject swapped, and it reads correctly however many readers were present. Another reader is named by their player name, and every reader is still "you" in their own account, never he or she. Like the House's read, the witness forms are a place for subtle changes, such as what a watcher noticed that the actor could not. This multiplies the writing per vignette, and the accounts are the main cost of making a vignette shared.

## What the House keeps

One record per reader, `HouseRead`, in `LabyrinthData` under its own state key (the Witness record's pattern). It holds two things.

**Observations:** what the reader did, in order, e.g.:
- *read the paper*;
- *turned the light off without touching anything*;
- *took the trench coat and turned the mirror to the wall*;
- *stood by the young woman until the song ended*;
- *went in when the whistle blew*.

Observations are never revised and never shown to anyone except, in the reader's own account and book, the reader.

**Categories:** what the House concludes, one per trait, each with a strength.

| Strength | Meaning | Later rooms may... |
|---|---|---|
| Unknown | No signal, or the reader skipped the room | ...behave exactly as they do today |
| Tentative | One signal | ...make small changes: a prop, a line, a song |
| Settled | Two signals that agree | ...make larger changes: the partner, the table setting, how strangers address the reader, the order of rooms, how authority figures behave |
| Uncertain | Signals disagree | ...behave as if unknown until a later signal breaks the tie |

A later signal that agrees raises a category to settled; one that disagrees makes it uncertain; the next decides. Categories can therefore change, and no misread is permanent.

| Trait | Values | Room |
|---|---|---|
| `AGE` | `ANALOG` (older), `TRANSITIONAL` (middle), `DIGITAL` (younger) | The waiting room |
| `HAIR` | `WRAPPED`, `BRAIDED_AND_OILED`, `BRUSHED_DRY`, `WASHED_AND_BARBERED` | The bathroom |
| `SELF` | `MASC`, `FEM`, `ANDROGYNOUS`, `UNSEEN` | The coatroom |
| `DRAWN_TO` | `MEN`, `WOMEN`, `BOTH`, `NO_ONE` | The shelter |
| `HOME` | `HAVEN`, `DANGER`, `RUNAWAY` | The porch |

**API.** Code reads it through one call, `HouseRead.category(player, Trait)`, which returns the value and strength or nothing. Every caller must handle nothing, and that branch is today's behaviour.

Where the build differs today:

- **Actions.** Each room keeps its reader's literal actions in its own saved visit (`house_read_rooms_04138`), only to run the room and reveal its note.
- **Categories.** The category record exists only on the read side, under `house_read/<reader UUID>`, with `HAIR`, `SELF` and (since 0.4.152) `DRAWN_TO`, each holding a `Value` and a `Strength`.
- **Classifier.** Nothing writes categories yet, by the owner's choice, so every reader reads as unknown.
- **API.** `category` returns only tentative or settled values. Unknown, uncertain, malformed and missing records all read as nothing. `HouseRead.hair` and `HouseRead.address` wrap it for the six consumers.

**Data handling:**
- The record lives only in the world save and is never sent to another reader's client.
- It is excluded from the opt-in playtest log (`PlaytestLog`).
- A server operator can read a save file, and the README says plainly that the House keeps these observations and categories there.

## 1. The waiting room (waiting-chair) — AGE

### The owner's design

A quiet, wood-panelled small-town dentist's or doctor's waiting room on a rainy afternoon. A grandfather clock ticks steadily in the corner. A frosted glass sliding window at the reception desk has a little brass bell beside it and a small wooden sign: *Back in a moment.* The exit door clicks open after a wait. Around the room are three ways to kill thirty seconds:

- **The low table (`ANALOG`):** a stack of dog-eared magazines and a folded local newspaper beside an ashtray and a pair of reading glasses.
- **The corner cart (`TRANSITIONAL`):** a glossy 1990s CRT television on a rolling cart with a VCR underneath, playing muted local weather radar, with a physical channel knob on the front; next to it a water cooler with paper cones.
- **The armrest and the window (`DIGITAL`):** a glowing glass phone on the armrest of the chair closest to the wall outlet, already half-unplugged, and a window looking out at the parking lot.

### Building it

In Minecraft almost everyone paces, because sitting takes a click, and everyone pokes the TV and the bell once because they are new and clickable. The build therefore reads *what the reader picks up to pass the time*, not posture or the bell.

- **The door** opens after 600 ticks whatever the reader does. The bell rings and nobody comes; it doesn't shorten the wait, so ringing it isn't rewarded. Ringing it is kept as an observation.
- **First signal (tentative):** the first time-killer the reader uses:
  - opening the paper or a magazine (a short readable local page) or putting on the reading glasses: `ANALOG`;
  - turning the TV's channel knob or filling a paper cone at the cooler: `TRANSITIONAL`;
  - picking up the phone: `DIGITAL`.
- **The phone is left for the reader.** Its lock screen shows the time and one unread message ("Running late, sit tight"), reusing the Harrigan phone screen. The reader keeps nothing; it goes back on the armrest when they leave.
- **Second signal (settled):** what the reader does with the rest of the wait:
  - `ANALOG`: sitting in a chair, or keeping the paper or glasses, until the door clicks;
  - `TRANSITIONAL`: staying within three blocks of the cart, or flipping through more channels;
  - `DIGITAL`: keeping the phone in hand until the door clicks.

  Sitting and pacing on their own are observations only.
- **Doing nothing** for the whole wait records nothing.

### Text (owner's; on the bottom of the wall calendar behind the reception glass)

> A waiting room is the only place left where time still moves at the speed of wallpaper.
>
> Folks who remember when the afternoon used to be long just find a chair with good springs, fold the paper back to the local page, and let the clock do its own work. Folks who came up with a wire in their pocket don't know what to do with their knees when a door is shut—they’re up and down the rug before the minute hand even clears the twelve, looking for a piece of glass that’ll glow back at them.

## 2. The bathroom before bed (night-sink) — HAIR

### The owner's design

A small, warm bathroom in an older house at eleven at night. Rain taps against the frosted window; a radiator hisses in the corner. The bedroom door unlocks as soon as the reader turns off the light switch by the mirror, so nothing on the counter has to be touched at all. Laid out across the sink and the open medicine cabinet are four nighttime routines, as if the house expects the reader to finish getting ready for bed:

- **Set A (`WRAPPED`):** a folded silk headwrap or bonnet, a jar of curl cream, a wide-tooth detangling pick.
- **Set B (`BRAIDED_AND_OILED`):** a bottle of castor or scalp oil, a rat-tail parting comb, a small dish of hair ties.
- **Set C (`BRUSHED_DRY`):** a boar-bristle paddle brush, a bottle of dry shampoo, a claw clip.
- **Set D (`WASHED_AND_BARBERED`):** a fine-toothed pocket comb, a tin of pomade, a folded cotton washcloth draped over the faucet.

### Building it

- **Equal places.** "First touched" otherwise favours whatever is nearest the door or leftmost. The four sets sit at equal distance from the entrance (two on the sink, two in the cabinet at the same height), and which set sits where is shuffled for each reader. The position touched is kept as an observation.
- **Touching** (using an item) lifts it, as a person picks something up; the item settles back. The reader keeps nothing.
- **First signal (tentative):** the first item touched sets `HAIR` from its set.
- **Second signal (settled):** a second item from the same set. Running the hot tap settles only Set D, whose washcloth hangs over the faucet; for the other sets the tap is an observation.
- **The light switch** opens the bedroom door whatever has been touched. Turning it off having touched nothing records *turned the light off without touching anything*.
- **`HAIR` stops at the routine.** Nothing else is ever inferred from it: no heritage or race label, and no cross-check for any other trait. The objects speak for themselves wherever the routine comes back.

### Text (owner's; on the back of a postcard wedged into the corner of the mirror frame)

> You never really sleep right in a strange bed until you’ve stood over the sink for two minutes with the hot water running.
>
> Even when you’re dead on your feet, your hands still know the order of things before the light goes out. You reach for the silk so the cotton pillowcase doesn't take what’s yours, or you work the oil into the part, or you run the bristles through until your scalp stops humming from the drive. The house doesn't mind waiting in the hall while you get yourself settled.

## 3. The borrowed dryness (hall-coatrack) — SELF

### The owner's design

A quiet front vestibule or mudroom just inside the front door of a warm country house during a freezing downpour. The reader comes in shivering and dripping. A radiator clanks against the wallpaper. Before the velvet curtains into the dry parlour stand a brass coatrack, a wooden bench and a full-length hall mirror. Dry layers are left out by whoever lives here:

- **Left pegs (`MASC`):** a heavy wool overcoat with squared shoulders, a flannel work shirt, heavy leather boots by the mat.
- **Right pegs (`FEM`):** a tailored trench coat belted at the waist, a soft knitted shawl or cardigan that buttons on the left, heeled ankle boots.
- **The bench (`ANDROGYNOUS`):** an oversized, shapeless canvas barn jacket and a heavy knit scarf that buries the jawline. Taking one item from the left pegs and one from the right also reads `ANDROGYNOUS`.

### Building it

- **The cold.** The reader arrives with the vanilla freezing effect (the frost the Holy Rabbit and Winter Lake already use), harmless here, and it lifts once a dry layer is worn.
- **Equal coats.** All three options are equally appealing, and which side holds which is shuffled for each reader, so nobody is just taking the best-looking coat or the nearest peg.
- **Wearing.** Using a garment puts it on, in the chest slot for coats and jackets and the feet slot for boots. Anything the reader was wearing there goes into their pack and comes back when they leave.
- **First signal (tentative):** the first garment taken sets `SELF`. One from each side sets `ANDROGYNOUS`.
- **The mirror** shows the reader their own skin in the garment they took (see "The House's version of the reader"). Second signals:
  - standing in front of it for two seconds or more after dressing settles the coat's category;
  - turning it to the wall (using it pivots it) or draping the dry towel over it, *before* dressing, settles `UNSEEN`;
  - turning it after dressing gives `UNSEEN` tentatively.

  Turning takes a deliberate use, because readers click everything once.
- **The coat stays.** Passing through the curtain hangs the borrowed layers on the parlour hook. Worn out into the House, other readers would see them, and the choice would stop being private.
- **A coat is presentation, not identity.** `SELF` is what the House saw. Only strangers who speak react to it, the way strangers judge by how someone presents; the House's own writing never does (see "Address").

### Text (owner's; tucked into the brass umbrella stand by the door)

> Nobody minds if you borrow a dry coat off the hall tree when the rain comes through the screen. Cloth is just cloth when your teeth are chattering.
>
> Still, it’s a funny thing watching someone dress themselves in an empty foyer. Even with nobody in the room to see them, a person’s hands will hunt through five hangers just to find the seam that sits right on their collarbones. And if the hall glass doesn't agree with what they see when they button it up, they’ll turn the mirror right around to face the wallpaper before they take another step into the house.

## 4. The last slow dance (jukebox-shelter) — DRAWN_TO

### The owner's design

A wooden lakeside pavilion or boat shelter at night during a warm summer rainstorm. String lights hang from wet rafters. A vintage jukebox glows against the back wall, humming with no song selected. Past the roof, rain falls onto dark lake water. Under the dry eaves, on opposite sides of the deck, are two quiet strangers about the reader's age:

- **The west railing (`MEN`):** a young man in a denim jacket with the collar up, leaning his elbows back against the wet wood, watching the rain hit the dock.
- **The east railing (`WOMEN`):** a young woman sitting on the wide rail with her knees drawn up under a dry sweater, tracing a pattern in the condensation on a glass bottle.
- **The dock steps (`NO_ONE`):** a dry lantern by the steps down to a covered rowboat tied to the pier, away from everyone.

The jukebox has one free credit. Using it, or simply crossing the floor, starts a slow, warm instrumental.

### Building it

- **Equal figures.** The two are mirror images across the centre line:
  - the same distance from it and at the same depth;
  - at the same height (both sit on the rail, or both lean on it);
  - in the same light;
  - each moving the same amount (her finger on the bottle, his collar against the wind).
- **Their age** follows the reader's `AGE` when known (tentative is enough): young adults for `DIGITAL`, around forty for `TRANSITIONAL`, older for `ANALOG`. Otherwise they are young adults.
- **They are unnamed in the shelter.** Any text that mentions them calls them *the boy by the rail* and *the girl with the bottle*, or *the man* and *the woman* for `TRANSITIONAL` and `ANALOG` readers. If a later room needs names, they are Tommy and Katie, from the owner's notes on the road and youth. For older readers the same names can grow up (Tom and Kate is a proposal for the owner).
- **The rowboat isn't rideable,** because boats are a magnet for Minecraft players, and boat lovers would read as `NO_ONE`. The dock and lantern are a quiet place to stand.
- **The way on is behind the jukebox,** so leaving doesn't pass either rail or the dock.
- **First signal (tentative),** measured while the music plays:
  - per figure: gaze (the figure's head within 8° of the view centre with a clear line, as `GhostsSet.framed`) plus time within five blocks;
  - the House compares the two, never raw totals:
    - one figure's share of at least 65%: `MEN` or `WOMEN`;
    - both above a floor and neither at 65%: `BOTH`;
    - walking past both onto the dock to the lantern: `NO_ONE`.
- **Second signal (settled):**
  - standing within three blocks of one figure for three seconds: that figure turns their head, gives a small half-smile and shifts to make room on the rail. Staying beside them until the song ends (about 45 seconds) settles `MEN` or `WOMEN`;
  - standing in the middle by the jukebox: both figures walk slowly inward to listen, and stop three blocks short (two strangers closing in reads as a threat in a horror mod). Staying between them settles `BOTH`;
  - staying on the dock with the lantern until the song ends settles `NO_ONE`.
- The figures never speak, follow, or touch.

### Text (owner's; pasted inside the glass lid of the jukebox beside the song strips)

> A summer storm is a great excuse to stand under a roof with somebody you haven't worked up the nerve to talk to yet.
>
> You put a nickel in the slot just so the quiet doesn't get too heavy under the rafters, and then your feet tell on you anyway. Some people drift over to the rail where the boy is watching the water; some go sit by the girl with the cold bottle; some stand right in the middle of the floorboards and hope neither one of them goes home before the rain stops.

## 5. The porch light at supper (screen-door) — HOME

### The owner's design

A dusty backyard at twilight: fireflies in the grass, an old swing set, a gravel driveway out to the road and dark trees. Ahead, the back porch of a two-storey clapboard house; the yellow porch bulb is on. Through the screen door: the clink of forks on plates, the low murmur of the six o'clock news, the rattle of an ice tray. A sharp, carrying supper whistle, then a tired adult voice from the kitchen: "Wash your hands before you sit down, alright? It’s getting cold." The sound is domestic, not monstrous, so hiding is a reaction to the house's mood, not to a jump-scare cue.

### Building it

The room is outdoors, in the outside dimension.

- **The whistle** sounds once the reader has been in the yard ten seconds or reaches the swing set. The voice and kitchen sounds are private and positional. A faint step crosses the kitchen linoleum after the voice, so the carving's heels have something to stand on.
- **The window is 200 ticks:**
  - going up the steps and through the screen door: `HAVEN`;
  - ducking onto the side porch out of view of the door, or under the lattice: `DANGER`;
  - stopping on the lawn to look through the lit living-room window, then *not* going in during the window: `DANGER`. Looking and then going in stays `HAVEN`, because curious readers look through every window;
  - walking down the gravel to the road: `RUNAWAY`;
  - anything else: no category.
- **Second signals:**
  - `HAVEN`: sitting in the pulled-out chair at the warm table;
  - `DANGER`: staying hidden under the lattice until the kitchen light goes off;
  - `RUNAWAY`: walking past the mailbox into the trees without turning back to the house.
- **Every ending is quiet:**
  - *went in:* a kitchen with a plate kept warm and no one at the table; the way on is through the front hall;
  - *hid:* the kitchen light goes off, the porch light clicks off, the screen door slaps once, and the side gate is open;
  - *ran:* the gravel becomes a dirt road through the trees to a door.

### Text (owner's; carved into the wooden rail of the back steps)

> A whistle carries three blocks when the humidity is down.
>
> Some kids hear that sound when the streetlights kick on and their feet are already moving toward the screen door before the echo dies, because they know the plates are warm and the lock is going to turn behind them. Other kids hear the exact same note and their stomachs drop right into their shoes, and they spend the whole walk up the driveway listening to how hard the heels are hitting the kitchen linoleum so they know which door to use.

## The House's version of the reader

The House never draws an "accurate" reader. The reader already made their likeness: their skin. Redrawing it would mean deciding eyes, skin tone and face, and would override the one self-portrait the reader chose.

Instead the House dresses the reader's own skin with what it learned, in the places where it shows its version of them:

- **Where:**
  - the coatroom mirror and later mirrors;
  - doubles: the Family Copy and the Tom double already render the reader's runtime skin;
  - the first-death portrait, which already paints from the skin.
- **What:** worn layers on top of the skin, never painted onto face or skin:
  - the coat or jacket they chose (`SELF`);
  - the silk wrap on their double asleep, or the pick in a back pocket (`HAIR`);
  - reading glasses on the reflection (`ANALOG`).

  The Hide-and-Clap blindfold already fits standard and slim heads as a worn layer, so the technique is proven.
- **`UNSEEN`:** the coat and silhouette show, but the face is obscured, in every mirror, double and portrait.
- **Never the live model.** Third person always shows the reader's own skin, even on their own client. The House's version belongs in glass and copies. When the House gets something wrong, the reader sees the gap between who they are and who the House thinks they are.

Built so far (0.4.150): the coatroom glass is a true reflection of the reader's own skin and borrowed layers, reversed, half their height where their eye would see it, turning and walking with them, and seen only from in front. `UNSEEN`, the reading glasses and later mirrors wait on the classifier (see `docs/PERSONALITY_VIGNETTES_REVIEW.md`).

## Where the read pays off

### The House of Leaves

`StaircaseProse` writes each reader's five leaves only from recorded facts, in one of four narrators' hands. Its leaves are fixed: *I. Outside*, *II. The hands*, *III. What happened*, *IV. The door*, *V. Below*. Each chapter takes one fact from its own groups, and the fifth is an ending in the narrator's voice. The read slots straight in, as new fact kinds and lines:

- ***II. The hands*** takes the reader's own routine: the pick or the oil from the bathroom, and the coat they hunted through hangers for. When neither is known it falls back to today's facts.
- ***III. What happened*** may take the shelter: who they stood beside until the song ended, or that they went out to the lantern alone.
- ***IV. The door*** takes the porch: which door they used, the screen door, the side gate, or the gravel.
- ***V. Below*** — the endings change with `HOME`. The letter writer's ending today says "I nearly wrote come home. I do not know where that is for you." With `HOME` known, the writer does know:
  - `HAVEN`: the plates are kept warm;
  - `DANGER`: which door to use;
  - `RUNAWAY`: the road.

  The other three narrators get one changed sentence each.
- **The voice of the writing** may follow `AGE` (the ledger for `ANALOG`, the letter for `TRANSITIONAL`, the witness or survey for `DIGITAL`) when known, and stays random otherwise.

Every fact the leaves use is an observation, so the book never states a category. It only shows the reader what the House saw them do.

### The personal account

The account opens with the House reciting the habits back, from observations only, in the House's voice. It reads as one breath of prose, the way a person remembers a night, not as five sentences of a printout. For example (the owner's wording):

> You read the local page until the door clicked, and put the silk on before the bathroom light went out. In the hall, you took the trench coat off the peg without looking in the glass, stood by the girl under the eaves until the song finished, and went straight in when the whistle blew.

How it's composed:
- **One clause per room,** chosen from that room's observations, in room order. Each clause carries its own lead-in ("In the hall,"), so it reads correctly wherever it falls.
- **Joining:** the clauses are stitched into one or two sentences with commas and a final "and"; a new sentence begins at the coatroom when there are more than two clauses.
- **Skipped rooms drop out cleanly.** The composer must read correctly for every combination of rooms; the tests check all 31.
- **Wording follows the reader:** *the girl* or *the woman* by `AGE`, and the clause for a reader who turned the mirror says so.
- If the House couldn't read the reader at all, the opening is today's.
- It closes with one line chosen by `HOME`.
- Like all the House's writing, it uses "you" and the reader's name, never he or she.

### Vignettes

Larger changes need a settled category; smaller ones may use a tentative one. Unknown and uncertain are always today's behaviour.

| Trait | Larger change | Smaller changes |
|---|---|---|
| `AGE` | The Wheel starts at the reader's stage of life, its rooms dressed for their era | Harrigan's phone is a handset for `ANALOG` and a screen otherwise; the shelter strangers' age; a landline rings in later halls for `ANALOG` |
| `HAIR` | The child's room bedtime card describes the reader's own routine being done for a child (the wrap, oil in the part, the brush, a wet comb) | Their set's items on the sinks at the Stanley's 217, the Lake House vanity and their Family Copy; the wrap on their double asleep |
| `SELF` | How strangers address the reader in speech (see "Address"); reflections and doubles wear their coat | `UNSEEN`: faces obscured in mirror halls and doubles |
| `DRAWN_TO` | The Lake House partner: Tess for `WOMEN` and `BOTH`, a young man in her place for `MEN`, Tess as a friend rather than a date for `NO_ONE`. The Stanley's table for two is set for that partner, and for one for `NO_ONE` | The Masque's private dancers turn to the reader's preference; later, the owner's planned mattress, marriage and passenger-seat rooms |
| `HOME` | `DANGER` (settled only): authority figures (the Barretts' John, Three Fingers' Walter, whoever is behind the child's room door at night) bring anticipation, not damage. See "Authority figures under `DANGER`". `RUNAWAY`: road and outdoor rooms are likelier to come first (`LabyrinthDealer.dealWeight`) | Mood only, even when tentative: domestic rooms open warm for `HAVEN` and tense for `DANGER`, and Goatman's family night opens warm or tense |

The Lake House partner is built (0.4.152, `docs/LAKE_HOUSE_PARTNER_0_4_152.md`): chosen once, on the reader's first visit, from a settled read, with Theo as the young man's working name. A second reader can play the partner, in pairs, each pair with its own lake (0.4.156, `docs/LAKE_HOUSE_PAIRS_0_4_156.md`).

Built so far (0.4.142), all neutral until a category exists:

- **`HAIR`:**
  - the first saved bedtime card in the child's room gains one remembered routine line (settled only);
  - one owner-only routine object each on room 217's sink and the Lake House upstairs vanity (tentative or settled).
- **`SELF`, settled only, in private spoken lines:**
  - the Last Stop men's address;
  - the Stanley porter, bartender and pianist greeting the caretaker;
  - Barry's crew talking to the camera operator.

  Shared speech bubbles keep their ordinary lines, and `UNSEEN` reads as neutral.
- **`DRAWN_TO`, settled only (0.4.152):** the lake house partner, as in the table above, with the first greeting, the account's opening and the Witness line following them.

The rest of this table, and the targets in `docs/PERSONALITY_TAILORING_TARGETS_0_4_140.md`, are still planned.

### Authority figures under `DANGER`

A tentative `DANGER` could still be a hesitant player, so it changes only the mood. A settled `DANGER` changes how authority figures behave, and only toward anticipation:
- They never move faster, hit harder or chase like arcade monsters.
- They linger: footsteps stop right outside the bedroom door and stay there; a floorboard creaks in the hall before they come in; the handle is tried and let go.
- Those houses always give that reader the second way out: a second door, a crawlspace under the bed, the gap under the lattice.

For someone who grew up in a `DANGER` house, the terror isn't the chase; it's listening to the hallway through a closed door.

### Address

The House's own writing never genders a reader. Accounts, miniature cards, Witness lines and the House of Leaves use the reader's name or "you", whatever `SELF` says. That is more intimate, and a reader who just liked the look of a coat is never misgendered by the House.

Strangers who speak are different. They judge by how someone presents, as strangers do:
- With `SELF` settled, the Last Stop men, the Stanley's staff and the Ghosts crew address the reader by presentation in spoken lines: "Hey, man", "buddy" and "he" for `MASC`; "Miss", "ma'am" and "she" for `FEM`; "friend" and "they" for `ANDROGYNOUS`.
- If a reader wore the wool overcoat because they like flannel and the Last Stop men call them "buddy", that reads as a realistic stranger's misperception.
- `UNSEEN`, tentative, uncertain and unknown readers are never addressed by gender, even in speech.
- A later signal can change `SELF`, and the strangers' address follows.

## Placement

- **Slots.** The rooms take slots 107 to 111:
  - coatroom 107 and porch 108, in layout 49;
  - waiting room 109, bathroom 110 and shelter 111, in layout 50.

  Each is built whole by its own builder and excluded from the generic polish, composition and review passes.
- **Copies.** Every reader has a private copy of each room, indexed in `house_read_rooms_04138`. The rooms stay private under the October 28 direction (see "Co-op").
- **Dimensions.** The waiting room, bathroom, coatroom and shelter are in the interior dimension; the porch is outside. The shelter's lake and rain are a set seen from under the roof, not a full lake.
- **Doors.** Today five doors stand along the impossible hallway (`HouseReadEntrance`, 0.4.142). The owner's direction moves them to the junction past the far door (see "The first rooms").
- New builds only.

## Assets

- **Items and props:**
  - bathroom: the twelve routine items;
  - waiting room: paper, magazines, glasses, phone (reusing the Harrigan screen);
  - coatroom: the garments (worn models fitted to standard and slim arms);
  - shelter: a lantern.

  All drawn in code like the rest of the mod.
- **Blocks:**
  - waiting room: panelling, reception window and bell, CRT cart, water cooler, clock;
  - bathroom: tiles, sink, cabinet, frosted window;
  - coatroom: coatrack, bench, turnable mirror, curtains;
  - shelter: deck, rafters, string lights, jukebox, rail;
  - porch: screen door, porch bulb, swing set, lattice, mailbox.
- **Cast:** the two shelter figures, in three age variants each, painted on the shared character grid.
- **Sounds, all subtitled:**
  - reused: rain and the clock;
  - new: a reception bell, TV hiss, a water-cooler glug, a hot tap, a radiator, a jukebox start, an original slow instrumental, the supper whistle, the kitchen voice, the news murmur, an ice tray, and a step on linoleum.
- **Particles:** fireflies and rain on the shelter's edge.

## Tests

Native cases:
- **Each room:** each first signal gives its category; each second signal settles it; contradicting signals make a category uncertain and a third decides; doing nothing records nothing and the way on still opens.
- **Bathroom:** shuffled positions; the position is recorded.
- **Coatroom:** the cold lifts when dressed; turning the mirror before and after dressing; the coat stays on the hook; worn items come back.
- **Shelter:** scripted looks and walks give each share; the figures stop three blocks short.
- **Co-op:** two readers crossing one door land in their own copies and meet in the same hall, each with their own record. In a shared vignette, a witness gets their own account and credit.
- **Forward exits:** every room's way on leads into the ordinary halls.
- **Finished rooms:** after a reader completes a room, its door takes them between halls without replaying the scene.
- **The record:** survives save and reload; never reaches another client; is absent from the playtest log.
- **Payoffs:** the House of Leaves, the account and every vignette that reads it behave as today with no record, and differ for two readers with different records.
- **The account opening** reads correctly for all 31 combinations of finished rooms.
- **Address:** the House's writing never contains he or she for a reader; spoken strangers use presentation only when `SELF` is settled, and never for `UNSEEN`.
- **Notes** never change while their reader is looking at them.
- **`DANGER`:** a settled authority figure's speed and damage are unchanged from today.

## The first slice

The first slice tests the bar before the other rooms and the bulk of the writing are committed:

1. **The record** and the per-reader copies.
2. **The coatroom and the porch** (`SELF`, `HOME`) as the first two rooms.
3. **Three payoffs:**
   - the account opening;
   - the House of Leaves's *II. The hands* (the coat) and *IV. The door*, plus the `HOME` endings;
   - `SELF`, as the larger vignette change: the Last Stop men's spoken address, and reflections and doubles wearing the coat (the face obscured for `UNSEEN`).

`SELF` is the owner's choice because two friends notice it at once. Weighted room order is invisible in one run, since dealing already feels random. But when one player's reflection wears a wool overcoat and the Last Stop men call them one thing, while the other's reflection wears a trench coat or has no face, the difference is undeniable.

Play it with two people who come in together. If comparing books and accounts is interesting, build the rest. If it isn't, stop, and the slice's rooms stay as quiet first rooms.

As built, the order went the other way. All five rooms came first, and the payoffs began with six read-side consumers, including the Last Stop men's address. The classifier that would give them something to read is not built yet, so the test with two players can't be run yet.

## The owner's decisions (October 27)

1. **Notes** stay inside their objects, blank side first, and gain their writing unseen after the first signal ("Rules for all five rooms").
2. **Address:** the House's writing uses names and "you" only; spoken strangers address by presentation, only when `SELF` is settled ("Address").
3. **Heritage:** `HAIR` stops at the routine; nothing else is ever inferred from it (the bathroom).
4. **`DANGER`:** tentative changes mood only; settled brings lingering and anticipation, never speed or damage, always with a second way out ("Authority figures under `DANGER`").
5. **The shelter strangers** are unnamed there; if later rooms need names, Tommy and Katie (the shelter).
6. **The first slice** uses `SELF`: the Last Stop men's address and the coat in mirrors and doubles ("The first slice").

The account opening is one flowing breath of prose (the owner's wording), not five separate sentences.

Two small proposals remain for the owner: Tom and Kate as the grown-up names for older readers, and the House of Leaves narrator following `AGE`.

## The owner's direction (October 28)

After playtesting 0.4.142, the owner asked for four changes, for these rooms and for the other vignettes too:

1. **The quiet rooms open from the first room after the hallway**, the junction, and not from the hallway itself ("The first rooms").
2. **Every room has an exit that leads forward**, not just back.
3. **Two or more players can go into a vignette together** ("Co-op"). Only Backseat and the five quiet rooms stay private.
4. **A completed room is just a door connecting halls** for that reader.

Follow-ups the same day settled several details, all in "Co-op":

- mixed groups get a vignette none of them has seen, or else split;
- forward exits lead into the ordinary halls;
- the five quiet rooms and Backseat are the only private ones;
- witnesses get their own account and credit, possibly written as a viewer's;
- multi-visit vignettes follow one story per thread, with late joiners, aftermath visits and accounts drafted for each way of being there.
- the Goatman, the drowned town and lake-witch scenes, the monkey's paw and the child's room are named as scenes to share.

Changes 1, 2 and 4 are built (0.4.143 and 0.4.144). Co-op, change 3, has its shared foundation (0.4.146) and its first shared scenes, the paw (0.4.147), the drowned town's ending (0.4.148), the child's room (0.4.149) and the Goatman's campers (0.4.150). Hide-and-clap, with its one blindfold, is the only scene still held back.

## Built so far

| Version | What it added | Notes |
|---|---|---|
| 0.4.138 | Coatroom and porch, private per-reader copies, offered at a reader's first fresh ordinary arrivals | `docs/HOUSE_READ_ROOMS_0_4_138.md` |
| 0.4.139 | Waiting room, bathroom and shelter, completing the five in the owner's order | `docs/HOUSE_READ_ROOMS_0_4_139.md` |
| 0.4.140 | The list of existing moments to tailor first | `docs/PERSONALITY_TAILORING_TARGETS_0_4_140.md` |
| 0.4.142 | Five doors along the impossible hallway instead of dealt arrivals; the House boundary tick recognizes built quiet rooms, so an idle reader is no longer ejected to the Overworld | `docs/ENTRANCE_PLAYTEST_0_4_142.md` |
| 0.4.142 | The six read-side consumers ("Vignettes") | `docs/SIX_PERSONAL_SCENE_TOUCHES_0_4_142.md` |
| 0.4.143 | The quiet rooms chain from the junction; the hallway doors are retired; a finished story's door is a passage | `docs/JUNCTION_CHAIN_0_4_143.md` |
| 0.4.144 | Every shared scene has a way on into the ordinary halls, with five fitted native doors | `docs/ONWARD_DOORS_0_4_144.md` |
| 0.4.146 | Going in together: groups follow into one scene, mixed groups are dealt a scene none has started, threads, roles kept beside each story | `docs/COOP_FOUNDATION_0_4_146.md` |
| 0.4.147 | The monkey's paw shared: one thread's evening, one holder, the finger curling for everyone who can see the hand, four drafted accounts | `docs/SHARED_PAW_0_4_147.md` |
| 0.4.148 | The drowned town's ending shared: everyone in Proofrock when the roof opens keeps a witness's or late joiner's account | `docs/SHARED_DROWNED_TOWN_0_4_148.md` |
| 0.4.149 | The child's room together: each reader's account says who was with them, who began it, whether they came late and whom they followed or left | `docs/SHARED_CHILD_ROOM_0_4_149.md` |
| 0.4.150 | The Goatman's campers: readers who come with the latecomer take cousins' places, so the camp keeps one reader's headcount | `docs/GOATMAN_CAMPERS_0_4_150.md` |
| 0.4.151 | Private scenes, one reader at a time: Harrigan, the confession, the Holy Rabbit, the Wheel house, the Whale institute, the phone in the canoe, the diner and the split-feed bedroom | `docs/PRIVATE_SCENES_0_4_151.md` |
| 0.4.152 | The lake house partner: `DRAWN_TO` chooses Tess, Theo in her place, or Tess as a friend, and the writing follows | `docs/LAKE_HOUSE_PARTNER_0_4_152.md` |
| 0.4.153 | Copies of private scenes: a reader who finds the confession, the Holy Rabbit, the Wheel house, the diner or the split-feed bedroom taken gets a copy of their own, kept until they finish it | `docs/SCENE_COPIES_0_4_153.md` |
| 0.4.154 | Copies of the Whale institute and the phone canoe, each lake with its own canoe | `docs/SCENE_COPIES_0_4_153.md` |
| 0.4.155 | Copies of Harrigan's room, each keeping its own story; every private scene now has copies | `docs/SCENE_COPIES_0_4_153.md` |
| 0.4.156 | The lake house in pairs: a second reader goes down in Tess's place and is seen as her, a third has a lake of their own with Tess, a fourth joins them | `docs/LAKE_HOUSE_PAIRS_0_4_156.md` |
| 0.4.157 | The shallows together: one throw, a second reader at her feet in Lonnie's place with Lonnie counting, the rest on the bank with the boys, a Witness line for each part | `docs/SHALLOWS_TOGETHER_0_4_157.md` |

The rooms record actions only. Categories, the account opening, the House of Leaves and the House's version of the reader are not built.
