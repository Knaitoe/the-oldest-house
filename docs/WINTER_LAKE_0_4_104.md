# Winter Lake, reworked — 0.4.104

On October 18 the owner approved this plan for Winter Lake. The candidate is 0.4.104, protocol 76 and layout 45. It
changes new builds only: the mod is unreleased, and the owner judged mid-save repairs unimportant.

46 sources / 35 resolutions / two kinds / three endings are unchanged. The Witness story `winter_lake`, its outcome id
(`broke_ice_waited_beneath_it_and_reached_the_bank`) and the "survival" kind are kept. The Witness title becomes "The dare",
and its line "I carried her ribbon out to the hole where she went down and gave it back. Then I walked to the bank as
quietly as I could."

The Indian Lake setting comes from Stephen Graham Jones's books. This winter is the House's own, and it puts the three lake
scenes in order:

1. **The Shallows** (summer): the boys throw her in.
2. **Winter Lake** (that winter): the lake freezes and the boys go back out.
3. **Costume Night** (the next summer and decades on): Hank starts the contest.

As in the Shallows, the reader is one of the boys.

## The lake

`WinterLake.build` builds the scene whole, like Costume Night. The polish, composition and review passes leave it alone
(`ScenePolish`, `SceneCraft.GROUNDS` and `SceneReview`), and so does `StaceyCover`.

- **The near bank**, where readers come in: snow, the boys' fire with logs to sit on, a sled with the account beside it, a
  stump with Bud's note on it, a lantern, and the boys' snow fort with two heaps of snowballs behind it.
- **The ice** at y -1 over open water five blocks deep. Two kinds, drawn by `tools/draw_winter_lake_art.py`:
  - **black ice**, clear and dark: you can see down through it, it rings underfoot (glass footsteps) and it is slippery;
  - **snow-covered ice**, opaque and quiet (snow footsteps). It drifts along the shore and lies in long patches.
- **The hole** where she went down, which never froze: three blocks of open water at (0, -46), black ice all round it,
  frozen cattails standing in a ring. A faint light sits deep under it for anyone in the water looking for the way up.
- **A fishing shanty** out on the ice, with a plank floor she cannot hear through.
- **The far bank and the sides**: spruce with snow on them.

The scene's clock holds it at midnight (see Movie Night's 0.4.103 note: scene clocks now reach the sky).

## The boys

Bud, Hank, Clay and Lonnie, the same four as the Shallows, at the same age, in coats, knit caps, scarves and mittens
(painted by `tools/paint_character_skins.py`, the `winter` mark, on stranger phases 34 to 37; boy scale 0.8; no long
coat). They are shared, and speak to each reader privately.

- At first they pelt each other with snowballs out onto the ice. After 45 occupied seconds Hank stops it: "Quit it. Quit
  it! You'll wake her up."
- Talking to Hank three times: "You came. Bud said you wouldn't." (or, for a reader who threw her at the Shallows, "You
  came. You were there that day too. You had her by the wrists."); then that her ribbon came off in his hand and he can't
  sleep; then the dare, and he gives the reader her ribbon: "Somebody has to take it back out to her. Out to the hole, where
  she went down. Here. Walk soft."
- Bud, Clay and Lonnie each have their own lines: sound carries on the ice, a foot thick, don't yell, walk on the snow.

## Her rule here: she hears

One shared body, always under the ice, hanging from its underside, drawn upside down with her face up through the ice. A
reader sees her only through clear ice.

- **Noise.** Running is loud (3), walking is heard (1), crouching and standing still are silent. Black ice rings louder
  (x1.5) than snow-covered ice (x0.4). Off the ice, on the shanty's floor or in the water, she hears nothing.
- **She goes to sound.** To the loudest recent noise at 0.14 blocks a tick; otherwise she drifts slowly.
- **Snowballs.** A snowball landing on the ice is the loudest thing on the lake; she goes to where it landed. Readers take
  snowballs from the boys' heaps (eight at most in their pockets).
- **Under a noisy reader** she builds up suspicion while they keep making noise above her, and loses it while they are quiet:
  a knock, two knocks, a crack in the ice, the crack spreading, and then she breaks through.
- **Pulled under.** The ice under the reader and round it opens; they are pulled down into the water and take one heart.
  She lets go and drifts off for eight seconds. The hole they made has its own light, and they can swim up through it and
  climb out. If their air runs out, they drown as normal.

This rule is Winter Lake's alone. Her ordinary hunt now remains only at Movie Night, under its fireworks rule.

## The way out

- At the hole, holding her ribbon, the reader uses it: a hand comes up out of the black water and takes it.
- The first time that happens in a night, the boys run, as they did at the Shallows: "She took it. She took it!", "Run!",
  snow kicked over the fire, and Hank calling back "I'm sorry. I'm sorry!" Then they are gone into the trees.
- The scene is ready once the reader who gave the ribbon back is on the near bank again. The account waits by the sled.

Only the reader Hank gave a ribbon to can give it back. When the lake has been empty for five seconds and no camera is
within 70 blocks, the night starts over: the boys by the fire, the fire lit, every crack and hole mended, her under the ice.

## Words

- **The source** is Bud's note passed in class, one page, signed "Bud": tonight at the lake, Hank still has her ribbon,
  whoever walks it out to the hole where she went down, that ends it; walk soft, Dad says sound carries a mile on ice.
- **The account** is titled "The account: the dare". It is told from what the reader did: the Shallows link if they threw
  her, the dare, a snowball that drew her, seeing her through the black ice, her knocking under their feet, falling through
  and coming up, the hand taking the ribbon, the boys running and the walk back. It ends: "The fire was out when I reached
  the bank. Behind me the ice ticked and settled, and somewhere under it she had her ribbon back." The shared folded-paper
  afterword follows.

## Assets

- **Four character skins**, `literary_winter_*`; the art catalog gains four rows (136, still 23 views).
- **The `winter_prop` block** (six kinds): black ice, snow-covered ice, cracked, split, frozen cattails and snowballs.
- **Her ribbon**, `winter_ribbon`, a blue hair ribbon, owner-bound.
- **Seven subtitled sounds** synthesized by `tools/generate_winter_lake_audio.py`: `winter.knock`, `crack`, `groan`, `break`,
  `hand`, `run` and `wind`.
- **Her under-ice pose**: `LakeWitchEntity` gains a synced under-ice flag; the renderer turns her over and lets a little
  moonlight reach her.

## Validation scope

- **Stacey's general cases stay where they are.** Winter Lake's controller drives only the body it adopts, and only where
  the scene has been built; a Stacey made the ordinary way keeps her ordinary hunt. So the general Stacey cases, the two
  scene-review witch cases and the live door break still run in bare Winter Lake frames with their own floors. The
  recess-cover case moves to Movie Night, the one scene that keeps its recesses.
- **One case is retired:** the old ice-break case.
- **Three cases are added:**
  - **Her hearing.** She is under the ice and the boys are on the bank; the hole is open. A snowball onto the ice is heard
    where it landed and draws her there. Crouched over her, a reader makes no sound; running over her, she knocks, cracks
    the ice and comes through, and the reader is pulled under. Nothing happens to the reader on the bank. Climbing out counts
    as coming up.
  - **The ribbon.** Hank gives it on the third word, to that reader only. On the bank it is only kept; nobody else can give
    it back. At the hole it is taken: her hand comes up, then she is back under the ice. The boys run and the fire goes out.
  - **The way out.** Out on the ice the dare is not over; back on the bank it is. A reader who walked out and back without
    it has not done it. The account tells only this reader's night, and reading it completes only theirs. Once the lake is
    empty the night resets: the ice mended, the boys and the fire back, her under the ice.
- **A native visual proof** saves three views: the lake's blocks and her ribbon, the four boys, and her under black ice.
