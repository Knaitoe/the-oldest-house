# More halls, more trials, a harder road down, and different times through the same space

Status: **proposal for the owner's review (October 29)**. Nothing here is built.

The owner (October 29): "can you also design some more hallway variations and trial rooms? I've found it too easy to get to the staircase. … I'd love to play with the mechanics of different people being able to traverse the same amount of space in different times. I don't know how that could be implemented."

Each proposal is numbered so the owner can pick by number.

## Why the staircase is easy to reach today

- **Depth is just doors.** A reader's depth is the number of forward crossings on their return stack. Any door counts: an ordinary hall, a story or a trial.
- **The staircase's gate.**
  - It needs depth 20.
  - It needs `FinaleProgress.eligible`: depth 12 and two stories visited (visited, not resolved).
  - After that, 18% of fresh deals put it behind one door.
- **Nothing has to be overcome.**
  - Trials (28%, 42% or 54% by depth) and anomalies are only ever one door among several. A reader can always take another.
  - Nothing is lost by walking away from a trial, and no trial has to be finished.
- **The result:** twenty crossings at twenty to forty seconds each is ten to fifteen minutes of walking through halls.

## A harder road: proposals

**R1. Seals.** The staircase needs three marks as well as depth.
- A mark is earned by finishing a trial room through its far door, once per kind of trial; walking back out the way you came earns nothing.
- A mark shows as a chalk sigil on the reader's map and a line in their account.
- The staircase is dealt only after three marks, four personally resolved stories (not visits) and depth 24.

**R2. Trials stand in the way.**
- From depth 14 on, a hall's onward doors are at most one ordinary hall; the others lead to trials or stories.
- From depth 18 on, about one hall in three has no ordinary way on, only a trial.
- A reader can still go back, but going back costs depth.

**R3. Folds.**
- From depth 12, about one ordinary door in seven is a fold: it opens onto a hall the reader has already passed, two to four crossings back, and their depth drops to match.
- Folds are never dealt twice in a row, never right after a story, and never in a group crossing that would split friends.
- The House is visibly undoing progress, which fits the book, but it can frustrate. **R3 is optional.**

**R4. A slower first stretch.** Domestic halls end at depth 8 rather than 6, and stone halls begin at 9. Stories are unchanged; this only adds walking.

Recommended: **R1 and R2**, with R3 left for a playtest. R1 alone already turns "walk twenty doors" into "survive three trials and finish four stories".

## New hallway variations (ordinary, no hazard)

These join the ordinary pool. Each has two or three onward doors and its own materials (see "Assets" below).

Domestic, before depth 8:
1. **The laundry corridor.** Sheets hang on lines across a long hall. Doors are hidden behind some of them, and parting a sheet makes it sway behind you.
2. **The long bedroom hall.** A corridor of identical closed bedroom doors. Two open; the rest are painted onto the wall and say so when tried.
3. **The landing.** Half-flights up and down between two short corridors on two levels, with doors on both.
4. **The service passage.** Narrow and back-of-house, lined with pipes and a fuse board. A dumbwaiter hatch is a second, crouched way through.
5. **The atrium.** A tall square room with a balcony ring. One door is on the floor and one on the balcony, reached by a stair around the walls.

Stone, from depth 9:

6. **The cistern walk.** A stone walkway over still water, with pillars and a dripping vault. Nothing in the water, ever.
7. **The niche hall.** A long hall of empty wall niches, each just big enough to stand in.
8. **The collapsed gallery.** A hall half-filled with fallen masonry. Climbing over it is ordinary parkour: one-block steps, no falls.
9. **The bridge.** A narrow railed bridge across a dark room, with doors at both ends and one halfway along, on a side platform.
10. **The library stacks.** Narrow aisles between tall shelves, forming a small maze with two exits, and books on the shelves that can't be taken.

## New trial rooms

A trial has a real physical demand. Failing never kills: it puts the reader back at the room's entry, or costs a fold (R3) if that is chosen. Each gives one mark (R1) the first time its far door is reached.

Tier 1 (depth 6 or more):
1. **The still light.** A dark corridor in which the lamps come on only while you stand still and go out when you move. Cross it from memory, a stretch at a time.
2. **The soft floor.** Floor plates sink while someone stands on them. Stop for more than two seconds and the plate drops you to the corridor below, which leads back to the start. Keep moving.
3. **The listening room.** Footsteps, jumps and blows fill a meter, shown as dust shaking from the ceiling. A full meter rings a bell and the room puts you back at the entry. Crouch-walk.

Tier 2 (depth 10 or more):

4. **The door that follows.** The far door moves to whichever wall you aren't looking at. Reach it by backing toward it, or by watching it in a mirror.
5. **The narrowing hall.** The walls close in over sixty seconds, until only a crawl gap low on one side remains. Find the gap before the walls meet; if they do, they push you back out.
6. **The rising stair.** A flooded stairwell whose water rises one step every three seconds. Climb faster than it, and dive for the two air pockets if you fall behind.

Tier 3 (depth 14 or more):

7. **The clock stair.** The treads fold flat except on the tick of a large clock, so the climb has to be made in time with it.
8. **The mirror corridor.** The true path is the one in the mirror. The corridor you stand in has walls where its reflection has openings.
9. **The two levers.** Two levers too far apart for one reader to hold. Friends pull them together; a reader alone finds a weight to hang on one, at the cost of carrying it there.

The tenth is the time trial below.

## Different times through the same space

Minecraft runs one clock for everyone: the server ticks every player at the same rate. A player can't literally live faster than another. What the House can control is close enough:
- how far each step carries each reader;
- which copy of a room each reader is in;
- how fast each reader moves;
- what each reader's own screen and ears show them.

Every technique below is built from pieces this mod already has.

### The tools

**T1. Private stretches.**
- A room whose two ends are shared and whose middle is a private copy for each reader.
- Readers go in together, each walks their own middle, and they come out of the same far door at different times.
- The quiet rooms and scene copies already give each reader their own building. A seamless threshold, like the impossible hallway's, makes the hand-off invisible.

**T2. Folding.**
- The middle of a hall repeats: past a marker, a reader is moved back by one repeat, keeping their facing and momentum.
- The House decides, reader by reader, how many repeats each one walks.
- The long hallway already does exactly this (`LabyrinthLoops`). Folds happen only out of every other camera's view, which the House already checks.

**T3. Pace.**
- Each reader in a shared room gets their own movement speed, through the owned, temporary speed modifiers the hazard halls already use.
- Two friends walking side by side drift apart, one slowed and one quickened.

**T4. Felt time.** Each reader's client is told how fast their time runs:
- a slowed reader hears the room's sounds lower and longer, and sees dust and water drift slowly;
- a quickened reader hears them high and short;
- the client already shows each room its own time of day, so one reader can see dusk in a room where another sees noon.

**T5. Echoes.**
- The House records a reader's path through a room and later plays it back to someone else as a faint figure, at the pace it was walked.
- You cross the hall alongside a friend who walked it an hour ago.

### What decides each reader's time

These are rules, and the owner may want only one. Every rule uses something the reader does, never who the reader is: no rule reads presentation, attraction or skin tone.

- **P1. Hurry is punished.** Sprinting or jumping folds you back one repeat; walking crosses in fixed time. Zeno's corridor: whoever runs arrives last.
- **P2. What you carry.** The more full stacks a reader carries, the longer their stretch. Emptying your pockets into the room's shelves shortens it, and the things wait for you on the way back.
- **P3. Company.** Alone, the hall is short. Together, each reader's stretch grows with the distance between them, so the House separates people who walk apart and rewards those who hold together.
- **P4. What you took.** Readers who looted or broke things in the House walk longer; readers who came back for a lost companion, or let a peer go first, walk shorter.
- **P5. The porch's read.** A reader the porch settled as one who takes the road finds roads short and rooms long. One who listens to hallways (`DANGER`) finds a hall lengthens while it is watched. This is the only rule that uses the read, and only `HOME`, which is behaviour.

### First builds proposed

**Trial 10: the hall of many lengths.**
- A forty-block hall with shared ends and a private, folded middle (T1 and T2), paced by P1.
- In company, readers enter together and come out apart. The ones who arrive first hear the others' footsteps in the walls, and the last always arrives walking.
- It counts as a trial (R1) for whoever reaches the far door.

**Hall variation 11: the staggered stair.**
- A stairwell whose flight count is private: the counting stair already keeps counts per reader.
- Each reader climbs the number of flights the House chooses for them (P2 or P4), and everyone comes out on the same landing.

Both reuse code that already exists. The hall of many lengths is about a third new work; the staggered stair is mostly assets.

## Assets

Every new room gets its own native 16-pixel materials and fitted models, with no plain vanilla building blocks. The quiet-room review found that blocks placed without neighbour updates never connect. It also found decals drawn on the wrong side of their cell. Every new room gets the same audit the quiet rooms will:
- every wall fitting backed by a solid face;
- every object resting on a surface;
- every fence and pane connected;
- no unlisted vanilla block.

## Questions for the owner

1. Which road changes: R1, R2, R3 or R4?
2. Which of the ten halls, and which of the nine trials? Five halls and four trials would be a good first batch.
3. Which pace rule or rules (P1 to P5) for the time trial, and should P5 (the porch's read) be used at all?
4. Should the staircase's other gate (`eligible`: depth 12 and two stories visited) become "four stories resolved", as R1 proposes?
