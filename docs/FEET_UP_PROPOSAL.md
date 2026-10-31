# Feet Up: a parkour vignette (proposal)

*Design only. Nothing here is built. The owner asked for a vignette focused on parkour / "obby" gameplay (October 31, 2026).*

## The idea in one paragraph

A family house on an ordinary evening, lamps lit, adults talking behind the kitchen door. On the hall table, in a child's hand that pressed too hard, the rules of the game the children played every night: **the floor is lava, all of it, stairs are floor; you rest at bases; if you touch you go back to your base and it counts; three and you're out; if they start shouting, don't go down, go up; the attic is safe, pull the ladder up after.** The reader plays the game. The House keeps the children's rules true. The route runs up through the whole house, furniture to furniture, and the higher the reader climbs the later it gets: the ground floor is the evening of the game, the stairs are the night it went wrong, the upstairs floor is already gone, and the attic is where the children went. The obby's grammar is all there, re-dressed as a child's game that stopped being one: checkpoints are the children's bases, lives are the count, respawn is "back to base", moving platforms are the ceiling fan and the dumbwaiter, disappearing platforms are the boards that give way upstairs, the kill brick is the hearth, the leaderboard is a chalk tally on an attic rafter.

Title: **Feet Up** (the Tell-Tale floorboards already hold the Witness title "The floor"). Place id `feet_up`. Witness title "Feet up". Account "The account: feet up". A new Witness source of the survival kind: 56 eligible / 42 required (75% of 56 is exactly 42, so the quota does not move) / two kinds / three endings.

## Why a house and a child's game, not a course

The House already has a parkour hallway (the crumbled hall, 0.4.199) and physical hazard rooms. A dedicated obby needs a reason to exist in this world that is not "a minigame appeared". The floor-is-lava game gives every obby convention a domestic, slightly wrong meaning, and it lets the House do the two things it does best: change things only when nobody is looking, and make the reader complicit in a rule. It also fits the owner's standing theme of *different times through the same space*, vertically, inside one run.

## The space

One building in the interior dimension, built whole (excluded from polish, composition and the review passes, like the cottage, the lake house and the dare). Footprint about 23 by 27, three levels and an attic, all rooms with generous ceilings so that jumping from furniture never hits the ceiling: ground floor five high, first floor four high, attic a pitched roof to a ridge at eight.

Entry: the front door, south, the ordinary return door. Onward: the attic's far gable, a small door onto the ordinary halls (every vignette's forward exit). The onward door is reachable only by finishing the climb; before that the reader can always walk back out of the front door (ordinary unfinished visits allow retreat).

### Ground floor: the evening

Lamps lit, a fire in the front room, a wireless somewhere, cutlery sounds and two adult voices behind the shut kitchen door, never words. Furniture is the House's own household block set (sofa, armchairs, pouffe, coffee table, bookcases, sideboard, piano, dining table and chairs, dresser, counters, stove, fridge, pot rack, dumbwaiter).

1. **The hall** (teaching). Bench by the door, hall table with the rules page under a jar of chalk, coat stand, the newel post. Base one: the big chair just inside the front room, chalked BASE on its arm. Gaps of one and two blocks, one step up. The chalk arrows are the tutorial.
2. **The front room** (static platforms, the kill brick, the unseen help). Sofa, pouffe, coffee table, armchairs, bookcase shelves climbing to the mantelpiece, across the fireplace, down to the piano. The fire is real: the hearth before it is the one place the game was always serious. The two armchairs stand too far from the sofa to jump; the chalk on the wall says DON'T LOOK AT THE CHAIRS. THEY COME IF YOU DON'T LOOK. When no camera in the room can see a chair it slides one block towards the route, to an authored near position, and stops. Who pushes them is never shown. Base two: the sideboard.
3. **The dining room** (the sweeper). The long table is the obvious road, and over it turns a ceiling fan (the elk fan's renderer, a new state) at chest height to anyone standing on the table. Its sweep is a timed obstacle: crouch under the blade on the beat or be knocked off the table onto the floor, which counts. The grandfather clock in the corner keeps the beat visibly (the animated clock arms). The chairs here are pushed in, no help.
4. **The kitchen** (precision and the lift). Counters, the stove (a lit ring hurts like the hearth), the fridge top, then the pot rack: five pots hanging on chains, each a half-block platform, the only precise footing on the ground floor. Past the rack, the dumbwaiter: a one-block box on a rope (the service lift's mechanism) that rises to the landing when its rope is pulled. The kitchen door is the one the voices are behind; the reader passes within a block of it with light under it. Base three: the landing's window seat, where the dumbwaiter opens.

### The stairs and landing: the night

The main stair is floor. The way up is the banister: a rail a quarter of a block wide from the landing to the gallery, with two gaps where balusters are missing. Chalk: SNEAK ON THE RAIL. Sneaking makes the rail safe and slow; the gaps are one-block jumps from a standstill. A longer, easier line runs along the hall bookcases and the radiator tops for anyone who cannot face the rail. The lamps are fewer up here. The voices below have stopped being conversation.

### First floor: after

5. **The upstairs hall and the children's room** (disappearing platforms and the bounce). Up here the boards are going. A loose board holds for half a second under a reader and then drops, a real falling block, into the room beneath, where it lies on the dining table or the kitchen floor as debris until nobody is looking, when the House puts it back where it was. The reader hops board to board and keeps moving; a fall lands downstairs, in the evening again, on the floor, and counts. The children's bed is a mattress that bounces (a block that bounces like a bed should have): the way up to the wardrobe top. Base four: the bed. Base five: the top of the wardrobe.
6. **The hatch** (the climax). The attic hatch in the hall ceiling, its ladder pulled up from above. From the wardrobe top the hatch frame is one up and two across, over two boards that go the moment they are stood on: a timing jump, not a sprint jump. A skilled reader can take it in one leap from the chest of drawers instead (one up and three across, sprinting), which is the single optional sprint in the room. Right-clicking the hatch from below opens it, as the Hill nursery's hatch opens only from below. Chalk by the frame, in the smallest hand: WAIT FOR ME.

### The attic

Blankets, a lantern with no oil, the ladder pulled up, cobwebs that catch anything that falls through the hatch gently. On a rafter the tally: two names with marks against them, a third column with no name and nothing in it. The account is a notebook under the lantern. Reading it resolves the vignette and adds the reader's own column to the rafter. The onward door is at the far gable.

## Rules and mechanics

**The count (lives and respawn).** A reader's feet on any floor cell (floor, stair tread, hearthstone, the ground-floor debris) while the game is on: a short beat, a soft tap, a grey italic *One.*, and they are put back to their last base. Three is *Out.*: the kitchen door opens, light comes up the hall, footsteps come to the stairs, the lamps go down, and the reader is at the front door again with their count at nought. Nothing is lost but the attempt. Each attempt the House is a little darker and the chalk a little more (the children are on your side); the route never gets harder. Readers who leave and come back start a new attempt.

**Bases (checkpoints).** Reaching a base chalked BASE sets the reader's own put-back point. Bases are personal; two readers can hold different ones.

**The unseen help.** Downstairs, a piece of helping furniture moves only when no camera in the level, spectators included, can see it, using the same watcher test the Stooping Giant's steps and the Boarded House's Propped use. It moves one block per unseen tick-window towards its near position and never past it. It goes back to its far position when the attempt resets. Upstairs the same rule works against the reader: fallen boards return only unseen.

**The boards.** A loose board that has carried a reader for ten ticks drops as a falling block into the room below and lies where it lands (never hurting anyone). It is restored when nobody can see its gap, and the debris removed when nobody can see the debris. In practice a reader who falls and is put back to base finds the boards whole by the time they are up again, unless a second reader is still watching them.

**The sweeper.** The fan turns on a fixed beat (one sweep every three seconds). A standing reader on the table in the blade's arc on the beat is knocked towards the nearer floor edge. Crouching clears it. The clock's arms are the tell.

**The lift.** The dumbwaiter rises when its rope is pulled and comes back down two seconds after it has emptied. It carries one reader at a time (its box is one block).

**The dark.** Every ninety occupied seconds the voices below peak in a shout; the lamps go out for two seconds. Nothing changes in the dark, the fan keeps its beat, but a reader mid-route cannot see. Readers on a base are untroubled. This is the room's only clock, and it is pressure, not a limit: there is no timer on an attempt.

**The hearth and the stove.** Fire is fire. Burning readers tend to jump off the mantelpiece onto the floor, which counts. A death is possible but unlikely and follows the House's ordinary rules.

**What a clean run is.** Reaching the attic with no touch. The rafter records the fewest touches and whether the reader ever came up clean. The vignette resolves on the first finish, clean or not; the room stays playable as a passage afterwards (a finished vignette's door is a plain passage), and later runs keep writing the tally.

## Difficulty and reach

- **No sprint is required** anywhere on the required line. Hunger exists in the House and a hungry reader cannot sprint; the course's difficulty comes from precision (the rail, the pots), timing (the fan, the boards, the dark) and height, not distance. The one sprint jump is a shortcut.
- Required jumps: gaps of one or two, steps of one, one-up-two-across at most. The hatch is a two-board timing hop.
- The rail is the hardest precision passage and has an easier, longer alternative. The pot rack has none; it is five half-blocks in a line with nothing to fall onto but the floor (which only counts).
- Three touches is the children's rule. If playtesting says it is cruel, five is a one-line change; the rules page would say five.
- An option for later: readers at child height for the game (the crayon vignette's scale), which makes every piece of furniture a real climb and lets a reader stand under the table. Not in the first build.

## Co-op

The house is shared; the count and the bases are personal. The helping furniture needs every camera off it, so a second reader who stands and watches freezes the chairs, and a reader who looks away for a friend is doing something for them. The boards are shared, so two readers on the upstairs hall are in each other's way in the obvious way. The chalk's second person reads as plural without changing a word. Together lines in the account note who went up first and whether anyone was still counting when the hatch shut.

## Literature

All House writing: never genders the reader, never names a colour or an age.

**The source**, the rules page on the hall table, in the child hand:

> THE RULES
> 1. The floor is lava. All of it. Stairs are floor.
> 2. Bases: the big chair. The sideboard. The window seat on the landing. Our bed. The top of the wardrobe. You can rest on a base.
> 3. If you touch, you go back to your base and it counts. Three and you're out.
> 4. If they start shouting, don't go down. Go up.
> 5. The attic is safe. Pull the ladder up after.

**Chalk**, grey italics the first time each is in view: BASE. JUMP FROM THE ARM NOT THE SEAT. DON'T LOOK AT THE CHAIRS. THEY COME IF YOU DON'T LOOK. SNEAK ON THE RAIL. THE BOARDS GO. KEEP GOING. WAIT FOR ME.

**The count**: *One.* *Two.* *Out.*

**The account** (first person, assembled from the run's facts: touches, where they fell, which line they took at the gallery, clean or not, who else was there), a draft of its middle:

> The rules were on the hall table in a hand that pressed too hard, and the floor was lava, all of it. I went up the big chair's arm as the chalk said and along the shelves over the fire, and when I looked back from the mantelpiece the second chair had come three feet closer to the sofa than I had left it. The fan over the dining table kept the clock's time and I kept under it. I touched twice: once off the fourth pot, which turned, and once when the boards went under me in the upstairs hall and I came down on the dining table among the plates, and both times the count was kept by someone who whispered it. By the time I was on the wardrobe the voices below had stopped being words. The ladder was up. I jumped, and the hatch gave. There were blankets, a lantern with nothing in it, and a tally on the rafter: Ada, Wil, and a third column with no name and nothing in it. I added my own. Then I pulled the ladder up after me, the way the rules said.

**Witness line**: kept off the floor all the way to the attic (outcome `reached_the_attic`). **Journal line** on finishing. **House of Leaves fact**: the number of times the floor had you. **Afterwards line**: *Some nights you still hear it, a count kept softly, that never gets to three.*

**Keepsake** (one of the artifacts with real effects): a stub of the children's chalk, finite and owner-bound, taken from the attic jar. While it is carried, a fall onto any floor inside the House of six blocks or less costs nothing. The children's rule, kept.

## Technical plan

- `LabyrinthPlace.FEET_UP("feet_up", Kind.ONE_SHOT, next free slot, BoundingBox(-11,-1,-27,11,14,0), doors: entry (0,0,1) south RETURN; the onward door installed at the attic's north gable)`. Interior dimension. Required depth 6 (a domestic room belongs with the domestic halls). New builds only; no migration.
- `FeetUpHouse.build(l,b)`: the whole house, furniture from `HouseholdFurnitureBlock`, one new `feet_up` prop block with kinds LOOSE_BOARD, MATTRESS, POT, POT_CHAIN, HATCH_FRAME, TALLY_RAFTER, CHALK_JAR, RULES_PAGE (the source), NOTEBOOK (the account); the fan as a new `ElkFan` renderer state; the dumbwaiter on the puzzle rooms' lift base; the hearth a native fire on its own ash block; the hatch a trapdoor that opens only from below. Excluded from `ScenePolish.applies`, `SceneCraft`, `SceneReview`, `SceneHuntReview`, `VignetteArchitecture`, like the other whole-built scenes.
- `FeetUp` (server tick, occupied clock): personal state in `LiteraryVignettes.personal` (Base, Touches, Attempts, BestTouches, Clean, Finished); shared state (help positions, boards, fan phase, argument clock). Floor contact from the reader's actual supporting block each tick; put-back after a 15-tick beat; three → the out sequence (kitchen door, light, footsteps, lamps, front door) and reset. Unseen movement through the existing watcher test over every player camera and spectator in the level. Boards as `FallingBlockEntity` with `hurtEntities` false.
- The read: one structured signal, which line the reader took at the gallery (the rail or the long way) and whether they finished clean. Account, Witness, journal, leaves and afterwards lines through the existing text classes; a `WitnessAccount.Story` entry of the survival kind.
- Art: `tools/draw_feet_up_art.py`, 16-pixel materials for the prop kinds, the pots, the board faces, the tally rafter and the rules page; the child hand font already exists for the page and the chalk.
- Audio: `tools/generate_feet_up_audio.py`: the muffled two-voice argument (two low-passed layers, no words), the shout, board crack, board fall, the fan's whir, the hatch, chalk, the count's tap; all subtitled.
- Tests, namespace `the_oldest_house_feet_up`: (1) the platform graph: every base is reachable from the previous by edges of at most two across or one up and two across, with headroom, measured from the built collision shapes, and no required edge needs a sprint; (2) a driven walker with ordinary input, gravity and collision crosses the required line to the attic; (3) a floor touch counts and puts the reader back to their base; (4) three touches run the out sequence and reset the attempt, with the helping furniture back at its far positions; (5) a chair moves only while no camera sees it and freezes under a spectator; (6) a board drops after ten ticks, lies below, and returns only unseen; (7) two readers: personal counts and bases, shared boards; (8) the account resolves once, the tally keeps writing, Witness is 56 / 42 / two kinds / three endings. Architecture views `feet_up`, `feet_up_upstairs`, `feet_up_attic` (the derived view check will require the first by name).
- Docs: `docs/FEET_UP_0_4_200.md`, the README's release note, the release checklist's counts.
- Size: comparable to the dare: one builder, one controller, one block, two generators, eight cases, three views.

## Questions for the owner

1. A Witness source of the survival kind (this proposal), or a trial room on the puzzle rooms' base, gating the way on with no story? The story is what makes it the House's; the trial version would be cheaper and could sit deeper.
2. Three touches, or five?
3. The dark every ninety seconds: keep, lengthen, or drop?
4. The second time through the same space: the same house later, with no floors at all, the furniture hanging where it stood over the dark, as a deep sibling (the way hazard rooms have siblings) or a second visit. Worth planning for in the builder (a `stage` flag) even if it is not built first?
5. The keepsake's effect (falls of six blocks forgiven while the chalk is carried): right size, or too useful on the great staircase?
6. How much the attic says. The draft keeps it to a tally with an empty third column and the smallest hand's WAIT FOR ME; nothing is explained.
