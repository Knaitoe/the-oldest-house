# The charge, the cell, the laps and the fires (0.4.84)

This builds on the owner's latest public source (`8ade65a`, Build #845). The owner asked for four things: a more devastating Minotaur charge; a death that leaves the reader in the cell; the Goatman visibly tearing round the camper with knocks that can be heard; and staircase fires that a reader who gave an arm at the cabin can light.

## The charge

- **Before the charge.** While it winds up, the Minotaur paws the floor with one hoof (a new model pose), kicking up grit and dust in the floor's own block particles. The hoof strikes stone with a heavy stamp.
- **The bellow.** At the moment it charges it bellows (a new sound), with steam from its nostrils.
- **The run.** As it runs it throws up grit behind both hooves, and every few strides the hoof stamps. If it runs into a wall it bursts against it in dust.
- **A blocked charge.** A raised shield still stops the charge and staggers the Minotaur, which is the opening to strike. But the reader behind the shield:
  - takes exactly **two hearts**, through the shield and any armour (a new `the_oldest_house:minotaur_charge` damage type: "was trampled by The Minotaur");
  - is thrown back off their feet;
  - has their shield knocked down, with a short cooldown;
  - feels the room shake through the staircase's existing screen-shake lease.
- **The shield wears out.** Each block takes a share of what the shield has left, so its durability bar drops with each blow. The count is kept on the shield (`MinotaurBlocks`), so the **third block breaks that shield outright**, whatever durability or enchantments it had. A reader who brings a second shield gets three more blocks.
- **An unblocked charge** still kills, as before.

## Death: the cell

- **Waking in the cell.** A reader killed in the fight wakes **inside the cell**, behind shut bars, with nothing; their belongings go into the Mother's sealed custody as before. Their phase is the new `IMPRISONED`. While anyone is kept there:
  - nobody else can open the cell ("There is already someone on the other side.");
  - no new prisoner appears;
  - the peaceful Witness release waits.
- **The Minotaur leaves.** It does not vanish. It waits outside the bars until the one it killed is standing on the other side, looks at them for a few seconds, bellows, and then lumbers out through the arena's mouth and into the dark of the stairs.
  - A creature that was still the caged child at the reader's death changes only where no other living reader can see it; otherwise it is removed out of sight, as before.
  - A creature unloaded at the time of the death is released as soon as it loads.
- **The one door.** The cell has one door, an iron door set into its back wall, there only while someone is kept. Using it takes the reader out of the House by the front door (or to the world spawn if that is built over), and **not let back in**: they are `LOCKED_OUT`, as a defeat always was.
- **The door resets.** Once nobody is left inside, the door goes back to wall, so the cell is exactly as every other reader finds it. The next reader to come down finds a new prisoner in it.
- **On the stairs.** The released Minotaur stays on the staircase. It climbs a stretch of stairs at a lumbering pace, rests, growls far off, and moves on. It harms nobody and moves without being seen only if it is wedged. At most three roam at once; an older one goes, unseen, when a fourth is released.
- **Unchanged.**
  - A new fight waits until any loose Minotaur has left the arena.
  - Dying in the cell wakes the reader there again.
  - A collapse that ends the House turns anyone still kept into `LOCKED_OUT`.
  - Deaths in the collapse, the escape or the peaceful release keep their old outcome, and the wounded Minotaur stays alive in its cell.

## The Goatman tears round the camper

The 0.4.77 run past the window happened only if a reader happened to stare out of one particular window in a narrow lull, so in play it was almost never seen. It is now a lap:

- **The lap.** While the impostor waits unseen behind the closed door, every reader inside the trailer gets their own private, unsaved figure of the Goatman as it is. It runs a whole lap just off the walls at high speed (0.8 blocks a tick), past every side window, wherever the reader is looking.
- **The knocks.** It slaps or scrapes each of the five side windows as it passes and hammers the back wall. Its footfalls and blows are private positional sounds for its reader, loud enough to hear from anywhere inside.
- **Where it starts and ends.** It starts at a corner the reader cannot see, and is gone at its corner only once it is out of their sight there.
- **When it runs.** It runs at most three times a night, 15 seconds apart. Never within three seconds of a knock at the door, and never during the door assault: it cannot be in two places at once.
- **Sound packets.** The Goatman's other private knock packets now use the registered sound holders.

## Lighting the fires

Each hearth still burns only the next leaf of the reader's own House of Leaves, with a lighter, in order. The hands are now forgiving:

- **The flame.** It is any lighter the reader carries, held in either hand or in their pack.
- **Two hands.** A reader with both hands holds their House of Leaves in either hand.
- **One hand.** A reader who gave an arm at the cabin may hold either the book or the lighter in their one hand and keep the other in the pack.
- **Refusals.** The refusal messages name what is missing ("Hold your House of Leaves to the fire. The lighter can stay in your pack.").

## Assets

- **Sounds.** Two original subtitled sounds, `finale.minotaur_bellow` ("The Minotaur bellows") and `finale.minotaur_hoof` ("A hoof strikes stone"), synthesized by `tools/generate_minotaur_charge_assets.py`.
- **Damage type.** The same tool writes the damage type, adds it to the `bypasses_armor` and `bypasses_shield` tags, and adds the death message.

## Versions and tests

- **Versions.** Mod 0.4.84, protocol 56 for the new sound registry entries; use matching clients and servers. Layout stays 42: the cell door is placed and removed in play, not by a rebuild. Still 44 sources / 33 resolutions / two kinds / three endings.
- **New native cases.**
  - `aBlockedChargeStillCostsTwoHeartsAndTheThirdBlockBreaksTheShield`: three real charges into a raised shield, each costing exactly two hearts and a throw backwards. The shield's count and wear rise, and it breaks on the third.
  - `aReaderKilledInTheFightWakesInTheCellWatchesItGoAndLeavesByItsOneDoor`: the defeat shortcut's real death, the creature out and waiting, no new prisoner and no second opener while kept, waking in the cell with nothing, bars shut and door present, the Minotaur turning away once seen, the door out to `LOCKED_OUT`, the door back to wall, and a new prisoner for the next reader.
- **Updated cases.**
  - The defeat shortcut expects `IMPRISONED`.
  - The window-run case now checks private laps for two readers wherever they look: never after a knock or during the assault, starting at a corner and striking all five windows and the back wall.
  - The one-armed hearth case adds a leaf burned with the lighter held and the book packed, and a two-handed reader holding the book in the main hand.

Exact-source validation is pending.
