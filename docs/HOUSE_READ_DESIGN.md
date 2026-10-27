# The House gets a read on you

Status: design only, not built (October 27). The concept, the five rooms and their texts are the owner's; the texts are quoted exactly. This document adds how each room is built and watched, what the House keeps, how co-op works, how the House shows its version of the reader, and where the read pays off. It also sets the bar for whether the idea is worth building, and a first slice to test that.

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
- **Not stories.** No source, no account of their own, no Witness credit. The pool stays at 52 sources and 39 resolutions across two kinds.
- **The first instinct counts.** What sets a category is the first deliberate act in the room's window. Later poking around is kept as an observation only.
- **Each reader is alone.** Every reader gets their own copy of each room (see "Co-op"), so the House only ever watches one person and nobody's habits are filed under someone else.
- **Private.** Figures are owner-bound actors, sounds are private (`LiteraryVignettes.heard`), and nothing about a reader's categories is shown or said to anyone else.
- **The notes come after.** Each room's text describes the test. Read beforehand, it tips the reader off; read afterwards, it is the House remarking on what it just watched. Each note stays where the owner placed it but can be read only once the room's first signal has been recorded (or its window has closed). Before that it shows its blank side: a postcard's picture, a calendar's month, a folded slip, the song strips, plain wood.
- **Non-horror.** None of the five ends badly. The unsettling part comes later, when other rooms turn out to know.

## The first rooms

These are the first five rooms a reader reaches after the opening (the three days, two sleeps and the letter are unchanged).

- **Order:** the waiting room, the bathroom, the coatroom, the shelter, the porch. Later rooms can use earlier answers: the shelter's strangers can be the reader's age because the waiting room came first.
- **Dealing:** each of a reader's first five fresh arrivals takes them to their next room. Each room's exit opens into an ordinary hall, so the rooms are naturally spaced. Stories still begin at depth 6 as now.
- **Missed rooms:** if a reader somehow reaches story depth with rooms left, the rest are offered at later fresh arrivals, never in place of a story. A reader who goes back never sees a room twice. Content may be missed, as elsewhere.

## Co-op: each reader gets their own copy

A shared room measures the group: friends follow each other, joke, and grab things for each other. More coats on the rack wouldn't fix that. So each reader gets their own copy of each room.

- **Proven pattern.** Borrasca already builds one private site per reader, with its own entry door, in bounded slices (`BorrascaSites`). These five rooms are far smaller, so a copy per reader is cheap. Each room keeps one slot; its copies are indexed by reader.
- **How it plays.** Friends who cross a door together each arrive in their own copy. Every copy's exit leads into the same next hall, where they meet again. Being separated is on theme: it is the first sign that the House wants a word with each of them.
- **No waiting rules.** Whoever finishes first waits in the shared hall; no clock holds anyone.
- **Building ahead.** A copy is built when the reader's previous room is reached, so it is ready by the time they need it. If it isn't ready, the door waits, as Borrasca's does.

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
| Settled | Two signals that agree | ...make larger changes: the partner, the table setting, pronouns, the order of rooms |
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
- C and D are also gender-coded (the claw clip, the pomade). That is kept as a weak cross-check for `SELF` and never sets it.

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
- **A coat is presentation, not identity.** `SELF` is what the House saw, and other rooms use it with care (see "Pronouns and address").

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

The account opens with the House reciting the habits back, from observations only, in the House's voice. For example:

> You read the paper while you waited. You put the silk on before the light went out. You took the trench coat and didn't look in the glass. You stood by the girl until the song ended. You went in when the whistle blew.

A room the reader didn't finish is left out, and if the House couldn't read them at all the opening is today's. It closes with one line chosen by `HOME`.

### Vignettes

Larger changes need a settled category; smaller ones may use a tentative one. Unknown and uncertain are always today's behaviour.

| Trait | Larger change | Smaller changes |
|---|---|---|
| `AGE` | The Wheel starts at the reader's stage of life, its rooms dressed for their era | Harrigan's phone is a handset for `ANALOG` and a screen otherwise; the shelter strangers' age; a landline rings in later halls for `ANALOG` |
| `HAIR` | The child's room bedtime card describes the reader's own routine being done for a child (the wrap, oil in the part, the brush, a wet comb) | Their set's items on the sinks at the Stanley's 217, the Lake House vanity and their Family Copy; the wrap on their double asleep |
| `SELF` | Pronouns and address (see below); reflections and doubles wear their coat | `UNSEEN`: faces obscured in mirror halls and doubles |
| `DRAWN_TO` | The Lake House partner: Tess for `WOMEN` and `BOTH`, a young man in her place for `MEN`, Tess as a friend rather than a date for `NO_ONE`. The Stanley's table for two is set for that partner, and for one for `NO_ONE` | The Masque's private dancers turn to the reader's preference; later, the owner's planned mattress, marriage and passenger-seat rooms |
| `HOME` | Which vignettes come first: `RUNAWAY` makes road and outdoor rooms likelier (`LabyrinthDealer.dealWeight`) | `HAVEN`: domestic rooms open warm before they turn. `DANGER`: authority figures (the Barretts' John, Three Fingers' Walter, the child's room door at night) become what the reader listens for, and those houses give that reader more hiding places and second doors. Goatman's family night opens warm or tense |

The Lake House keeps Tess solo-only as built.

### Pronouns and address

Today the House uses no pronouns for a reader (the miniature cards, accounts and prose use the name or "you").
- With `SELF` settled as `MASC` or `FEM`, accounts, cards, Witness lines and the people who speak about the reader (the Last Stop men, the Stanley's staff, the Ghosts crew) use he or she. `ANDROGYNOUS` uses they.
- `UNSEEN`, tentative, uncertain and unknown readers are never gendered: their name, or "you".
- A later signal can change `SELF`, and address follows.

## Placement

- **Slots.** Five new slots, 105 to 109, are appended (layout 47). Each is built whole by its own builder, with copies per reader indexed in its own state like `BorrascaSites`, and excluded from the generic polish, composition and review passes.
- **Dimensions.** The waiting room, bathroom, coatroom and shelter are in the interior dimension; the porch is outside. The shelter's lake and rain are a set seen from under the roof, not a full lake.
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
- **Co-op:** two readers crossing one door land in two copies and meet in the same hall, each with their own record.
- **The record:** survives save and reload; never reaches another client; is absent from the playtest log.
- **Payoffs:** the House of Leaves, the account and every vignette that reads it behave as today with no record, and differ for two readers with different records.

## The first slice

The first slice tests the bar before the other rooms and the bulk of the writing are committed:

1. **The record** and the per-reader copies.
2. **The coatroom and the porch** (`SELF`, `HOME`) as the first two rooms.
3. **Three payoffs:**
   - the account opening;
   - the House of Leaves's *II. The hands* (the coat) and *IV. The door*, plus the `HOME` endings;
   - one larger vignette change using these two traits: `HOME`'s room order, or `SELF`'s address by the Last Stop men.

Play it with two people who come in together. If comparing books and accounts is interesting, build the rest. If it isn't, stop, and the slice's rooms stay as quiet first rooms.

## Open questions for the owner

1. **Notes:** keep each note where it is (readable only after the first signal), or move them to the far side of each room's door?
2. **Pronouns from a coat:** is a settled `SELF` enough for the House to use he or she, or should it stay with names, or wait for a second room?
3. **Heritage:** `HAIR` describes a routine. Should anything ever be inferred beyond it?
4. **`DANGER`:** how predatory may authority figures become, and only when settled?
5. **The shelter strangers:** names, if they ever need them.
6. **The first slice's larger change:** room order or address?
