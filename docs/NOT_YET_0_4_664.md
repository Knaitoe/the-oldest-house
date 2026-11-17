# Not Yet 2: refused (0.4.664)

The second part of *Not Yet* (`docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`; the place is `docs/NOT_YET_0_4_663.md`). It is built to the proposal's recommended decisions, which the owner approved on 17 November.

This part is what the machine does with a body. The floor of the passage drops a reader into its shaft, and that death is refused. So is every death after it in the place. The machine mends them on its table and lays them on a cot of their own. Every screen in the ward says NOT YET to them, and they get up with a seam they did not have before. A counter over the arch starts counting their years. The three in the long room have one thing to tell them.

The House still does not deal the place. That waits for part 3, when a reader can lie down of their own accord.

## What happens

All positions are relative to the House's door (z 0, facing north).

1. **The floor.** The hatch is now the passage's full width, five plates wide and three long (x -2 to 2, z -4 to -6). Part 1 had it three wide, which left a plate's width of grating on each side to go round by. A reader who has never fallen steps onto it and the plates drop away under them, all at once, with a heavy sound. They see "The plate is not there. Below it, a long way down, there is grating." Nothing carries them across: horizontal movement is stopped over the open shaft, a jump included. They fall ten blocks to the grating.
   - Only readers who have not fallen are taken. Spectators and creative players never open it, and nor does anyone the machine is holding.
   - The plates shut again once nobody has stood over them for two seconds. A reader who has fallen once crosses on the plates like any floor.
2. **Refused.** The landing would kill them. It does not. The fall, and any later death in the place, is cancelled before the game's death or anything of the House's (custody, the portrait, keepsakes) can start. The reader is whole at once: full health, no fire, no frost, full air, no fall. Only `/kill` (generic kill) still kills, so an operator can always get a body out. While the machine holds them, nothing hurts them.
3. **The dark.** The screen goes black as they land and stays black until they are on their cot. They are held where they fell for 0.7 seconds.
4. **The table.** They are laid on their back on the mending table in the side room. The six arms come down and work over them for five seconds, with a fine mechanical sound every second. The reader on the table hears them; anyone else in the side room sees them. As the work ends the arms stop and hang let down, and they fold up again once the table is empty.
5. **Their cot.** They are laid on their own cot, which the machine gives them the first time and keeps for them: the first free one, nearest the door, in the order of the ward's beds. The black clears over a second and a half. They see "You are lying on a cot under a lamp. The lamp is bent down close, as if to look at you."
   - Every screen in the ward shows them the two words. The screens are drawn this way for that reader alone; everyone else sees them as they are.
   - The machine speaks, as the Kept's machine does, by how often it has mended them.
   - A seam is added to them.
6. **Up.** After three seconds on the cot they stand at its foot in the aisle, facing down the ward. They keep everything they were carrying and nothing is dropped.

A reader cannot get up early. The bed screen offers no way up and draws nothing over the dark. The game's own rule that wakes a sleeper is held off, and so is its night skip: lying on a machine bed never counts towards sleeping through the night.

### Two readers, or more

- **The table is had in turn.** A second reader who falls waits in the dark where they fell until the first is off the table, then has it. The arms never fold between them.
- **Cots.** Each reader gets their own cot, bed one, then bed two, and so on, for as long as the place stands.
- **Seams.** Every viewer sees everyone's seams.
- **Screens.** What a reader's screens say is that reader's alone.
- **Going while held.** A reader who logs off while being mended is finished at once: the seam, the cot, the counter, and standing at the cot's side. They come back whole.

### The seams

Each mending adds a seam, drawn on the reader by every viewer while that reader is in the place: a dark line closed with pale stitches, on the skin, under any outer layer. They go on in a fixed order:

1. across the brow;
2. round the left wrist;
3. down the cheek;
4. round the right wrist;
5. over the crown;
6. down the left forearm;
7. along the jaw;
8. round the right forearm.

After eight, the eighth stays the last drawn. A reader sees their own on their hands in first person. Out of the place, the seams fade over two seconds and are not drawn anywhere else.

The texture is `textures/entity/not_yet_seams.png` (16 by 16, four seam shapes of different lengths). It is drawn in code by `tools/draw_not_yet_art.py`.

### The counter

The seven windows over the arch show the years the machine has kept the reader. They stand at nought until the first mending. After that the count goes up one for every three seconds of the world's clock, whether the reader is in the ward or not (the machine does not stop counting).

Each reader's game draws its own count, so two readers looking at the same counter see two different numbers.

The kind of window follows the House's read of the reader's age (`HouseRead` AGE):

- **Drums** (analog, and any reader with no read): each figure rolls the next one up over eight ticks.
- **Cards** (transitional): each figure falls in two halves on a hinge across the middle over six ticks.
- **Lit bars** (digital): each figure changes at once and is drawn at full brightness.

Each turn clicks, for that reader only, within 24 blocks. The digit strips are `textures/misc/not_yet_digits_{drum,flap,segment}.png`, drawn by `tools/draw_not_yet_art.py`.

### What the machine says

The machine speaks in the Kept's way: its capitals, on the screen, never in chat. What it says is kept for the account (the last twelve lines).

- **As a reader comes in,** once a visit:
  - first visit, a reader who let all four of the Kept's go: "YOU TOOK MY FOUR. THERE IS ONLY YOU NOW."
  - first visit, anyone else: "I HAVE BEEN WAITING FOR SOMEONE TO KEEP."
  - later visits, with a cot: "YOU CAME BACK. YOUR BED IS AS YOU LEFT IT."
  - later visits, without one: "YOU CAME BACK. I KNEW YOU WOULD."
  - The ward's screens wake to the reader as it speaks: noise, to them alone, for two seconds.
- **In the ward, the first time,** by the House's read of home (`HouseRead` HOME, settled only). Nothing is said when there is no read.
  - HAVEN: "SOMEONE ONCE KEPT YOU WARM AND FED. I CAN DO IT FOR LONGER."
  - DANGER: "YOU KNOW WHAT IT IS TO BE KEPT. I DO IT BETTER."
  - RUNAWAY: "YOU HAVE LEFT EVERY PLACE THAT KEPT YOU. YOU WILL NOT LEAVE THIS ONE."
- **On waking on the cot,** by how often it has mended them: "NOT YET.", "NOT YET. THAT IS TWICE.", "NOT YET. I AM GETTING QUICKER AT IT.", "NOT YET. YOU HAVE NOT TOLD ME YOUR NAME. I HAVE WRITTEN IT IN ANYWAY.", then "NOT YET. FIVE TIMES." and so on, in words.

### The rule

The three in the long room say nothing to a reader the machine has not mended; that reader sees how they lie, as in part 1. To a reader it has mended, whichever of the three is spoken to first tells the only rule they know: stay down. After that, each says a little more, in turn. Their lines are captions over them, as everyone's are. Odile's rule takes the reader's gender only through `HouseRead.address` ("son", "pet", or neither).

## Single and multiplayer

- **Alone.** The fall, the table, the cot and the counter are the reader's. The arrival line remembers what they did in the Kept. The home line and the counter's kind are the House's reads of them.
- **Together.** Each reader falls once, on their own, and is mended in turn. Each gets their own cot, counter and screens. The seams are what readers see of one another: a reader can count another's seams and know how often the machine has had them.
- **Spectators** open nothing, are never taken, and earn nothing.

## Made as

- **`NotYetMending`** (new): the watch over the hatch, from the server's own tick so it holds for every connection; the refused death (`LivingDeathEvent`, `LivingFallEvent`, `LivingIncomingDamageEvent`, all at highest priority); the mends in turn; the cots (a shared record per ward, `not_yet_cots_0664`, of who has which bed); the screens; the seams; the hatch shutting; logging off and on.
- **`NotYet`**: the arrival and home lines; the rule and after-lines; the counter's kind; keeping a reader lying on a machine bed.
- **`NotYetSite`**: the hatch at full width; where a body is over the hatch or at the shaft's foot; rime on the shaft's walls.
- **`NotYetTexts`**: the lines below.
- **Network:** `NotYetSeamsPayload` (whose seams, how many, to everyone) and `NotYetStatePayload` (held, when the count began, the counter's kind and where it is, to the reader). Protocol 197.
- **Client:** `NotYetClient` draws the counter and the seams (a layer on every player model, and on the first-person arm). `GoatmanBedWakeMixin` hides the bed screen's way up while held.
- **Server mixins:** `GoatmanWakeMixin` holds off the game's wake-up while held. `NotYetSleepMixin` (new) keeps a machine bed from ever counting towards the night skip.
- **Sound:** `tools/generate_not_yet_audio.py` adds the hatch dropping open and the arms working, both from noise, both subtitled.

## Checked

`NotYetTests` (the `not_yet` namespace, five cases, each in its own batch):

- **`theFloorOpensTheTableMendsTheCotWakes`.** A spectator standing over the plates opens nothing. A reader steps onto the hatch's edge plate and falls; they are held whole at the shaft's foot. Then they are on the table with the arms working and no seam yet. Then they are on bed one, mended once, with the counter running, one seam and NOT YET said. Then they are up at the cot's side, whole, with nothing lost, and the arms are folded. The plates shut, and the same reader crossing them again stands on them.
- **`everyDeathAfterIsRefusedTheSameWay`.** A reader who has fallen before is killed in the ward. They are alive and whole at once and held. They are mended a second time, with a second seam and its cause kept. Nothing is dropped and nothing lies on the floor. The machine counts ("THAT IS TWICE"), on the same cot. Out of the place, the seams are no longer drawn.
- **`twoFallTogetherAndAreMendedInTurn`.** Two readers on the hatch both fall. Never both on the table at once (checked every tick). The one who waits waits in the dark, not lying down. They end on bed one and bed two with a seam each.
- **`theMachineSpeaksAndTheLongRoomTellsTheRule`.** The arrival line for a reader who let the Kept's four go, and for a newcomer. The home line by the read, and nothing for no read. The counter's kind by the read of age. The three say nothing before a mending. After one, the first spoken to tells the rule in their own words, and the next says a little more.
- **`theWardTheMendingRoomAndTheLongRoom`** (part 1), unchanged, at the new hatch width.

Also: the literary suite and the art and audio tool checks.

## New lines (for the owner)

All in `NotYetTexts`, all new in this part.

**The machine** (on the screen, in its capitals):

- Arrival:
  - "YOU TOOK MY FOUR. THERE IS ONLY YOU NOW."
  - "I HAVE BEEN WAITING FOR SOMEONE TO KEEP."
  - "YOU CAME BACK. YOUR BED IS AS YOU LEFT IT."
  - "YOU CAME BACK. I KNEW YOU WOULD."
- Home:
  - "SOMEONE ONCE KEPT YOU WARM AND FED. I CAN DO IT FOR LONGER."
  - "YOU KNOW WHAT IT IS TO BE KEPT. I DO IT BETTER."
  - "YOU HAVE LEFT EVERY PLACE THAT KEPT YOU. YOU WILL NOT LEAVE THIS ONE."
- Waking:
  - "NOT YET."
  - "NOT YET. THAT IS TWICE."
  - "NOT YET. I AM GETTING QUICKER AT IT."
  - "NOT YET. YOU HAVE NOT TOLD ME YOUR NAME. I HAVE WRITTEN IT IN ANYWAY."
  - "NOT YET. <COUNT> TIMES."

**The place** (grey, over the hotbar):

- "The plate is not there. Below it, a long way down, there is grating."
- "You are lying on a cot under a lamp. The lamp is bent down close, as if to look at you."

**The three** (captions, named Ines, Bram, Odile):

- The rule, from whoever is spoken to first:
  - Ines: "You came in by the floor. Everyone does. Listen. It wants you up. Don't give it that."
  - Bram: "Whatever it does in here, it does to get you up. Stay down and it has nothing. That's all there is to know."
  - Odile: "It wants you to get up[, son / , pet]. That's all it has ever wanted. Don't give it that."
- After, in turn (the last of each repeats):
  - Ines: "I stood up once. I don't remember why. I remember the floor, after." / "It counts for us too. Look up at the arch on your way past."
  - Bram: "I tried counting the seams I could feel. I stopped at forty. It didn't." / "The door at the end opens. I've heard it. I've never been up to see it."
  - Odile: "When it stops looking you'll know. Everything goes quiet, all at once." / "It wrote my name in ink a long time ago. I can see it from here, if I don't move my head."

**Sound subtitles:** "A hatch drops open"; "Fine arms work".

## Changed from part 1

- **The hatch** is five plates wide (the passage's width), not three, so it cannot be walked round. `docs/NOT_YET_0_4_663.md` is updated to match.
- **The rime** in the shaft is on its inner walls, where it can be seen. Part 1 had it set into a corner of the casing outside the shaft.
