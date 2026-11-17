# Last Seen 4: the night (0.4.670)

This is the fourth of the five parts of *Last Seen* (`docs/LAST_SEEN_PLAN.md`). It is built to the recommended decisions in `docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`, which the owner approved on 17 November.

Part 3 (`docs/LAST_SEEN_0_4_669.md`) was the second day. Part 4 is the night after it. The gates are chained from outside with the children in the park, men gather on the avenue with flashlights, and a reader can take a child's hand and lead them into the archive. The men come over the railing. At dawn the children in the archive are gone too, and their folders lie open on the long table. It is still not a Witness source; that is part 5.

## The night

A reader who has had the second day (their own `Days` at least two) comes back to the night. With two or more readers, it is the furthest any of them has reached, as before. The night counts only for a reader who has had the second day.

The night starts with:

- only Nora in the office, at her desk; Patricia and Luz have gone home;
- the front door locked ("The front door is locked. Patricia locked it when she went home."). The archive's back door is not;
- the long table bare;
- the city's fence round the playground and its notices on every lamp post already up;
- the eight at their places, facing the avenue: Tomás in his coat, Daiana at the gate, Lautaro at the old gap;
- the park's lamps lit.

The hour runs from nightfall toward the small hours over the night's four minutes (`NIGHT_TIME` 13800 to `LATE_TIME` 21800), then to first light at dawn (`DAWN_TIME` 23300 and on). At night a car goes by on the avenue only now and then, and there are no pigeons.

### The men

- **They come** (from 10 seconds): three men, each made only where nobody sees, at one of the avenue's two ends. Each has a flashlight lit (a subtitled click) and two carry a length of wood. They walk along the avenue to the park's gate and stand on the near pavement, facing the park.
- **The gate.** Once all three are there, the gate is chained and padlocked from the avenue side, with a subtitled chain. Readers outside the office are told; Nora tells a reader in the office to bring them in by the back door. The gate answers a hand: "The park's gate, chained and padlocked."
- **While they wait**, one of them speaks every eighteen seconds, in turn, with the sound of men talking low. Each line is heard by readers within 24 blocks of whoever says it.
- **Their flashlights.** Each is held out ahead at shoulder height and swept. Where its beam lands, the light falls (a light block of level 11 that moves with the beam), with a faint dust along the beam.
- **A hand on one of them**: "He turns his light on you, and then back to the park."

### A child's hand

Before the men come over, a reader with an empty hand can take one of the eight by the hand:

- the child takes it ("Lautaro takes your hand. His is cold, and it holds on.") and follows a step behind and to the left, holding on;
- from then until morning, that reader does not go back out the way they came (`LastSeen.locked`, under `LiteraryVignettes.retreatLocked`). Until a reader takes a hand or keeps the door, the way back is open;
- one child at a time; one already held by someone else is not taken ("Lautaro is holding someone else's hand.");
- a second hand on them, crouching, going more than six blocks ahead or leaving lets go. Let go of out in the park, they go back to their place;
- once the child is in the archive, they let go and go to stand at the shelf where their own folder is, facing it. A hand on them then: "Lautaro stands at his folder, facing it, and does not turn round."

The only way in for them is the archive's back door. The front is locked.

### Keeping the door

A reader inside the archive, just inside the back door (within about two and a half blocks of it), is keeping it. Once the men are over, a reader keeping it is told once: "You put your weight against the back door." Keeping it commits them as taking a hand does: from then until morning they do not go back out the way they came.

### Over the railing

At two and a half minutes into the night, the men come over the railing, with a subtitled sound of climbing:

- the park's lamps all go out together;
- a reader out in the park or on the avenue sees nothing for a moment (`HouseFadePayload`), and when they can see again, none of the eight still in the park is where they were. A child still out there that a reader in the office can see goes the first moment nobody sees;
- a reader leading one of them out there finds their hand empty;
- every reader in the place is counted as having been there when the men came (`NightOver`).

The men walk down the path to the office, a little apart, never so far out as the benches and lamp posts beside it. Two stand at the archive's louvres with their lights on them; one tries the back door, with a subtitled rattle:

- **Kept:** it holds. The keeper hears "The handle turns under your hand. You hold it, and the door holds."; others in the office hear "Someone tries the back door. It holds." He says "Locked."
- **Not kept:** it opens. A light goes along the shelves and does not stop; he says "Nothing in here but paper." Five seconds later the door is shut again.

Then they go back up the path and over the railing, and are gone the first moment nobody sees.

### Dawn

At four minutes (or once the men have gone), everyone's eyes are shut for a moment. When they open:

- the children who stood at their shelves are gone too;
- each one's folder lies open on the long table at its first page, a gap on its shelf. The table holds five, two to a cell after that;
- the men and their lights are gone, and the park's gate is open again;
- the hour is first light.

A hand on an open folder shows it, the photograph and the form ("Lautaro's folder, lying open at the first page."). Nora: "I didn't take them down. I was here all night."

A reader who was there when the men came, is still there at dawn and has had the second day has had the night (`Days` 3). It is saved to them with:

- which of the eight they brought into the archive (`Brought`);
- whether they kept the door (`KeptDoor`);
- whose hands they took (`TookHand`);
- which of them were gone from the table (`Gone`).

Part 5 reads these for the account.

## When nobody is there

As before, after ten seconds empty the place is put back. Now also: the men and their lights are gone, the lamps are lit, the gate is shut and unchained, the long table is as it was, and the back door is shut. The flashlights' light is taken out of the world before the server saves it.

## The House's read

Nothing new reads the House's read in this part. The night is the same for every reader; part 5's account is where it is read.

## Single and multiplayer

- **Shared:** the night, its clock and its hour; the men; the gate; the lamps; who is at their shelf; whether the door held; the open folders.
- **Each reader's own:** whose hand they took and whom they brought in; whether they kept the door; whether they were there when the men came and at dawn; whether the night is theirs; whether they are held until morning.
- **Alone or together.** One reader can lead some of the eight in alone; there is not time to lead them all. With two or more, one can keep the door while another leads. Each leads one child at a time, and a hand already held is not taken.
- **Spectators** are never readers here. They are not counted as there when the men came, take no one's hand, keep no door, and the men are made whatever a spectator is looking at.

## Made as

- **`LastSeen`:**
  - the night (`night`): the eight at night (`nightKids`), a child's hand (`nightHand`, `leading`, `letGo`), the men (`men`, `makeMan`, `menIn`, `menOut`), over the railing (`over`), the door (`keeping`, `tryDoor`), dawn (`dawn`), the flashlights (`beams`, `light`, `unbeam`), the lamps (`globes`), the table (`tableCell`), the shelf a child stands at (`shelfFront`, `shelfYaw`);
  - `locked`, read by `LiteraryVignettes.retreatLocked`;
  - `walk`: anyone walking a way who stands caught (on a bench, a post, or a reader in the way) for three seconds is at their next point the first moment neither they nor it can be seen. This is for every walker in the place, the day's people included;
  - `bringIn`, for a look at the place: one of the eight already standing at their shelf;
  - the fence put up a cell at a time (`raise`), shared now by the second day and the night;
  - `ensure` leaves Patricia and Luz out at night;
  - the night's hour in `time`.
- **Phases.** Stranger phases 260–262 for the three men (skins `literary_lastseen_man_a/b/c`). Gesture `FLASH` (5): the flashlight held out and swept.
- **`LastSeenBlock`:** the table's stages 3 and 4, one folder open on it and two.
- **`LastSeenRegistry`:** `FLASHLIGHT` and `STICK`, what the men hold; five subtitled sounds (`chain`, `switch`, `voices`, `rattle`, `climb`).
- **`client/LiteraryRenderers`:** the `FLASH` pose.
- **Art.** `tools/draw_last_seen_art.py` now also draws the open folder (photograph on the left leaf, the typed form on the right, each line a printed label and what was typed after it, with Nora's tick), lying the right way up for a reader at the chairs, the table models with one and two open, the flashlight and the length of wood. `tools/paint_character_skins.py` paints the three men: a hooded top and close crop with stubble; a flannel shirt, a receding line and a full beard; a jacket, a side part and a moustache. `tools/generate_last_seen_audio.py` makes the five sounds.

## Checked

`LastSeenTests` (the `last_seen` namespace), eight cases. CI gives the namespace twenty-six minutes.

- The six from parts 1 to 3, unchanged.
- `theNightAndTheDoor`, one reader who has had both days:
  - only Nora; the front door locked and saying so; the fence and notices up; Nora's first night line; Lautaro facing the avenue; the way back open;
  - taking Lautaro's hand, and from then the way back held;
  - walking him from the playground into the archive by the back door: he lets go, the reader is counted as having brought him, and he stands at his own folder facing it, and does not turn round;
  - the men chain the gate, each with his flashlight out and two with wood; their lights fall where they point; Nora tells the archive;
  - over the railing: every child still in the park gone, the one at his shelf not; the reader there; the lamps out; the reader keeping the door told so, and held to it;
  - the door tried and held;
  - dawn: Lautaro gone, his folder open on the table at its first page, a gap on his shelf; the night the reader's own with what they brought and the door kept; the way back open; the gate open and the lights gone; the hour first light; his folder shown from the table; Nora's dawn line.
- `threeReadersOneNight`, three readers and a spectator:
  - the night for all, because two of them reached it;
  - a held hand not taken by another; no second hand; crouching lets go; the hand taken again;
  - one reader leaves before taking a hand, and the way back was open for them;
  - the first is out in the park with Lautaro by the hand when the men come: the dark, the empty hand, and he is gone;
  - the one who left and the spectator were not there when the men came;
  - nobody keeps the door: it opens, a light goes along the shelves, and it is shut again;
  - dawn: the night is the first reader's only (brought nobody, kept no door, took Lautaro's hand); not the one who left, not the one who had not had the second day, not the spectator; nobody at the shelves, and the table bare.

Also: the art check (`tools/draw_last_seen_art.py --check`), the skin painter, the sound generator.

## New lines (for the owner)

All are in `LastSeenTexts`. Every one is new.

**Nora at night:**

- before the chain: "I'm not going home tonight. Stay if you're staying."
- when the gate is chained, to a reader in the office: "They've chained the gates. Bring them in here, by the back door."
- after, in turn: "Bring them in by the back door. The front is locked." / "Each one knows where their folder is. Let them go to it."
- once the men are over: "Don't open the door."
- at dawn: "I didn't take them down. I was here all night."

**The office and the gate:**

- the front door: "The front door is locked. Patricia locked it when she went home."
- the chain, to readers outside the office: "Out on the avenue, someone winds a chain round the park's gates, and padlocks it."
- a hand on one of the men: "He turns his light on you, and then back to the park."

**The men**, one at a time while they wait:

- "Gate's done."
- "They haven't moved. Not one of them."
- "Wait for the office to go dark."
- "Somebody's in there with them."
- "It isn't going to go dark."
- "Then we don't wait."

At the back door: "Locked." / "Nothing in here but paper."

**A child's hand** (shown with Lautaro; each says the child's own name, and her or his):

- "Lautaro takes your hand. His is cold, and it holds on."
- "You let go of Lautaro's hand."
- "Lautaro is holding someone else's hand."
- "You already have one of them by the hand."
- "Lautaro lets go of your hand and goes to the shelf where his folder is, and stands facing it."
- "Lautaro stands at his folder, facing it, and does not turn round."

**Coming near each of the eight at night**, once: "Lautaro stands facing the avenue. He does not blink."

**The back door:**

- "You put your weight against the back door."
- "The handle turns under your hand. You hold it, and the door holds."
- "Someone tries the back door. It holds."
- "The back door opens. A light goes along the shelves and does not stop."

**Over the railing:**

- "Your hand is empty."
- to a reader out in the park or on the avenue: "The lamps go out, all of them at once. When you can see again, there is nobody where the children were."
- to a reader in the office: "Out in the park, the lamps go out all at once. Lights are coming over the railing."

**Dawn:**

- out in the park: "Light comes up over the avenue. It is morning, and the park is empty."
- in the office, with folders on the table: "Light comes in at the louvres. Nobody stands at the shelves. On the long table, their folders lie open at the first page."
- in the office, with none: "Light comes in at the louvres. It is morning. The archive is as it was."
- an open folder: "Lautaro's folder, lying open at the first page."

**Subtitles:** "A chain is wound round the gates", "A flashlight clicks on", "Men talk low on the avenue", "Someone tries the back door", "Someone climbs the railing".

**Item names:** "Flashlight", "Length of Wood".
