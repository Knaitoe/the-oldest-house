# Design outline for The Oldest House

This file records the current design decisions so implementation does not outrun the concept.

## Central rule

There is one anomalous structure: The Oldest House.

Its overworld exterior is a fixed authored design. The facade does not grow, morph, or advertise supernatural behavior. Impossible space is confined to what lies behind it.

The technical implementation should be boring and dependable even when the player-facing result is impossible:

- Java 21.
- NeoForge 1.21.1.
- No core mixins unless a later mechanic proves genuinely impossible without one.
- Persistent global The Oldest House state in Overworld `SavedData`.
- Modular interior cells and a persistent topology graph rather than one enormous contiguous structure.
- Generate/instantiate only what players can currently reach or interact with.

## Appearance

The world begins normally. The Oldest House does not exist at world creation.

The spawn trigger is **settlement residency**, not generic exploration and not a cursed-item quest.

A player establishes the candidate settlement by repeatedly sleeping through nights in the same home area. The initial implementation counts five successful overnight sleeps whose wake positions stay within 32 horizontal blocks of the same settlement anchor. Moving outside that area begins a new candidate settlement count.

Reaching the threshold only makes the world **eligible**. It does not spawn The Oldest House immediately.

Eligibility initializes a hidden appearance chance at **5%**. The same morning that establishes eligibility cannot spawn The Oldest House.

Beginning on the following morning, the mod makes one hidden appearance roll per Minecraft day using the current chance. If The Oldest House does not appear, the chance for the next eligible morning performs a random walk:

- 50% chance to increase by 2-5 percentage points,
- 50% chance to decrease by 1-2 percentage points,
- minimum 5%,
- maximum 100%.

Because upward movements are larger than downward movements, the chance has a gentle long-term upward drift while remaining capable of falling from one night to the next. The spawn should therefore feel increasingly plausible without becoming a visible or deterministic countdown.

If a roll succeeds, the game searches for reasonably flat, empty terrain near the established settlement and places The Oldest House there. If no safe site is available, nothing is destroyed; the chance still changes for the next eligible night and the system tries again later.

No advancement, toast, chat line, sound sting, or other notification reveals eligibility, probability changes, or the successful appearance.

One morning, The Oldest House simply exists on ground that was previously empty.

The exterior remains permanently measurable and trustworthy.

## Perceived age

After The Oldest House appears, the mod tracks how long it has perceived the structure as having existed in the world.

The initial implementation advances this value once per successful Minecraft morning while The Oldest House is present. It is persisted separately from ordinary world time so development commands can fast-forward or set it directly.

Future architectural and narrative stages should key off perceived age rather than requiring testers or players to wait an exact amount of real time. Development command `/oldesthouse advance [days]` exists specifically to exercise these stages.

The current transition prototype reveals the first impossible door at age 3. That threshold is deliberately provisional and isolated in `HouseStageManager` so later pacing work does not require architectural changes.

## Domestic phase

The Oldest House is useful before it is scary:

- furnished,
- lit,
- safe from ordinary hostile spawning,
- suitable for storage and sleeping,
- player-modified domestic rooms become protected anchors.

The player should have time to adopt it as property.

Later, a door appears in an interior wall where no door existed before. The exterior has not changed. Space beyond the door cannot fit inside the measured shell.

## First impossible doorway and hidden transition

The stable domestic floor remains physically in the Overworld so its windows, storage, furniture, pets, and exterior measurements are ordinary Minecraft reality.

The first impossible doorway appears in an interior partition at the end of the rear hall. Two blocks of matching corridor exist behind the doorway inside the normal footprint. The cross-dimension transition occurs only after the player steps into that buffer, giving the architecture a chance to conceal the transfer.

The first cross-dimension prototype worked functionally but exposed Minecraft's dimension handoff too clearly. The active prototype therefore uses a same-dimension transition cell: it keeps the player's X/Z position and changes only Y, placing a matching corridor high above the physical structure in the same loaded chunk column. A reverse threshold returns the player to the domestic hall.

The first prototype corridor is intentionally simple and long. Its purpose is to test the hidden transition and spatial contradiction before the modular graph system is layered on top.

The cross-dimension transition did prove visibly disruptive in playtesting. The same-dimension cell is now the active experiment. Its purpose is to isolate seam quality from the separate questions of F3 behavior, map behavior, and long-term cell placement.

## Interior

The impossible interior lives in a controlled interior dimension of The Oldest House.

It is represented internally as a graph of modular rooms/corridors/stair modules. Connections can move players between distant physical cells while preserving the illusion of continuous architecture.

The Oldest House grows a little each night by changing/unlocking graph state. New physical cells are generated only when approached.

The main staircase goes deeper on later visits.

## Navigation horror

The mod should attack ordinary Minecraft navigation habits rather than depend on cinematic effects.

### Torches

Players often mark one wall with torches to find their way home. The Oldest House records player-placed torches and, rarely while unobserved, can:

- move one,
- add one,
- alter spacing.

This must remain rare enough that the player can doubt their own memory.

### Coordinates

Do not falsify F3 in the initial design.

F3 tells the truth. Modular transitions make the player's experienced travel disagree with the coordinates: forty visible blocks may correspond to eleven actual X blocks, or several flights of stairs may barely change Y.

### Maps

Maps should work. Their honesty is the problem.

Because interior cells occupy impossible/disconnected real coordinates, an interior map can naturally show a footprint larger than the exterior or disconnected architecture. Old and new maps may disagree after topology changes.

### Measurement

A custom measuring tool can return slightly different plausible readings for the same span.

Earlier explorer notes tell players to count steps, producing another source of measurement that may disagree.

## Sound and population

The House is nearly silent and mostly empty.

Do not fill it with conventional hostile mobs.

Occasional distant growls foreshadow the Minotaur for a long time before there is any physical entity to fight.

## Explorer notes

Earlier explorers leave behind notes, measurements, journals, supplies, and contradictory observations.

They serve three jobs:

1. tell a story worth completing,
2. teach navigation behavior such as counting steps,
3. provide evidence that the player's experiences are not unique.

## Rewards

There must be useful rewards found nowhere else, improving with depth.

The Oldest House should not become a conventional loot dungeon. Unique tools and artifacts should ideally relate to understanding, navigating, or surviving the House.

## Death

Death inside creates a strong return loop.

Ordinary dropped gear should be preserved rather than simply despawn. The room containing the items remains, but the topology may rearrange before the player returns.

The Oldest House changes the route to the gear, not the gear itself.

Compatibility with normal Minecraft death rules and common grave/keep-inventory behavior is preferred over replacing the death system wholesale.

## Dog

The player eventually finds a lost dog inside.

It becomes a genuine companion and useful ally, with enough time for ordinary Minecraft attachment to develop. Possible House-specific behaviors include reacting to bad doors, growls, topology changes, or lost belongings.

## Mother of Lost Things

A rare humanoid presence inspired by the woman who calls herself a mother to lost things.

She should not be a normal roaming hostile mob.

Her idea of caretaking is cruel. She becomes interested in the lost dog. Eventually the House separates the player from the dog; recovering it leads to an encounter with the Mother, who kills it with abrupt, casual violence.

The current practical staging idea is a fall from a great height using ordinary Minecraft movement/physics rather than gore or elaborate animation.

Text dialogue is straightforward. Branching dialogue remains optional until choices have meaningful consequences.

## Minotaur

The Minotaur is effectively endgame/conclusion, not a routine mob.

For most of the mod it exists as sound, testimony, and abstract The Oldest House state. A physical entity should only be instantiated when a local encounter requires one.

The final resolution is intentionally undecided. Do not reduce it prematurely to a standard boss health bar.
