# Borrasca: the feeding (0.4.439)

The third part of the lower rooms' rework (0.4.437 rewrote the records; 0.4.438 put Kelly, grown, in room 6). On the last night the
reader can watch the machine fed. The owner, November 9: "gore is appropriate... It should horrify the reader not just on an
intellectual level but a visceral, reactionary one too." The one fed is a grown woman. Nothing of a child is shown this way, and
nothing sexual is shown.

## Dana, 141-A

- She is in room 2, on her cot, looking at the ceiling. The cue: "Room 2. A woman lies on the cot, looking at the ceiling. She doesn't
  look round." The night book's last page: "Rm 2. No more deliveries. Up before the shift."
- She is a town cast actor (`BORRASCA_KYLE` on phase `DANA`, saved as `CastDana`), on her own skin, `borrasca_dana` (person 9).
- The owner: "She doesn't need to be in the nude, but her clothing should be in scraps" and "bare torso too, using the navel
  shadowing". Her skin has a bare torso with a shadowed navel, strips of a band across the chest, rags at the hips, felt slippers and a
  tagged band on one wrist.
- A second skin, `borrasca_dana_aside`, has her eyes gone to one side. About one moment in nine she shows it while she lies on the belt
  (the owner: "eyes occasionally looking to one side").
- A third, `borrasca_dana_scream` (person 11), has her eyes wide, the brows pulled up and her mouth stretched open. She wears it at the
  blade.

## The men, the walk and the belt

`labyrinth.BorrascaFeeding`, called every five ticks on the last visit:

1. A reader out in the yard near the depot's side door hears two men there: "Two's done. They want her up before the shift." / "She
   walk?" / "She walks. She doesn't do anything else."
2. They walk her out of the side door, one ahead and one with a hand on her arm, through the gap in the bars to the head of the belt.
3. "Lie down." She lies on her back on the belt, feet to the machine, her arms hanging over its sides and swaying a little.
4. The belt carries her north to the housing. One man stands by the belt: "Don't watch it go in. Watch the belt."

## At the blade

The owner: "when she hits the blade it shouldn't be quick. text should be garbled and wobbly like marjorie's and her awwwoo turning
into an AAHHHHH and then -. the game should track the splitting/tearing of her body so it doesn't tear all at once, it should be a
really gruesome affair".

- **Slow.** From the slot's mouth the belt draws her in at 0.0028 blocks a tick. Her body is two blocks long, so it takes most of a
  minute.
- **Bone.** It catches at her ankles, knees, hips, ribs, shoulders and jaw (`BONES`). At each one the motor labours and she jerks
  against it for one and a half to three seconds. Then it gives with a crack, the belt jumps, and bone and blood spray out of the slot.
  At the first catch a man says: "It catches on the bones. Give it a second."
- **Tracked, not all at once.** How much of her the machine has taken is saved and sent to clients as
  `LiteraryActor.torn` (thousandths of a block from her feet, `Torn0439`).
  - The cast model has every part again in one-pixel slices on the same skin islands (`BorrascaNativeArt.castLayer`).
  - `CastModel.cut` hides every slice past the slot, so her body goes a pixel at a time, legs first, then hips, belly, chest and head.
  - The two slices nearest the cut are drawn soaked in blood.
  - Where the body ends there is a torn end: ragged skin round meat and fat, a broken bone or the spine standing out of it, and two
    strips of meat hanging off the rim. They are drawn from a tile painted on an unused island of her own skin, (56,16) to (64,32).
  - Above the cut the skin splits open up the belly and the thighs, longer the more the machine has taken.
- **Alive at first.** While it has her legs and belly she is alive (`TORN_LIVE`):
  - she writhes, rolling about her own length and arching off the belt;
  - her head is thrown back and goes from side to side;
  - both arms claw up off the belt.
  
  Once it reaches her chest (`SILENT`, 1.16 blocks) she goes still (`TORN_DEAD`), her head fallen to one side and her arms hanging.
- **Her voice.** As the machine reaches each depth she makes the next sound: "awwwoo", "awwwwooo", "awwwooOOO", "AWWOOAAAH",
  "AAHHHHH", "AAAHHHHHHH", "AAAAHHHHHHHHHH", "AAHHH-HH", "-". Each is said again while it goes on, so it stays up, and nothing comes
  after the last. Her lines have no speaker's name.
- **Garbled, as Marjorie's.** `SpeechCaptions.shaking` draws her caption a letter at a time, each bobbing and tipping on its own as
  Marjorie's voice is drawn in the Ghosts set. The harder she screams, the harder it shakes. Past a point the letters jump about and
  some leave a dark red double behind them.
- **While she is still on the belt.** What the knees, hips and shoulders broke rides out of the far end of the housing on the outflow
  belt while the rest of her is still going in. Wet sounds, dark-red dust and pieces come from the slot the whole time. The belt soaks
  back from the slot toward her head.
- **After.** When her head has gone in, the three strokes sound. Then her hair and a slipper come out, then the band from her wrist,
  onto the pile at the end of the belt. A man says: "Hose the belt down after." Red dust drifts off the top of the housing for a while.
- Right-clicking the band: "A band off a wrist, cut through. The card on it is stamped 141-A." The pile: "Pulp, with bone through it.
  It is still warm." and "Hair, long, going grey, and a felt slipper."

## The red, and him

The owner: "Can you overlay the machinery with the shiny gentleman 'eating' her? ... Just a flash for about two seconds, not the whole
feeding sequence overlayed. Ensure the players screen is pulsing/flashing a dull angry red as she's ate by the orrery".

`client.BorrascaFeedingClient` reads only what each client already has (Dana's synced `torn` depth). The server sends nothing new.

- **The red.** While the belt is still taking her, the screen throbs a dull red: a slow double heartbeat, darker at the edges, harder at
  each lurch when a bone gives. Five seconds after the belt stops moving her (the breaker, or the end) it fades out. Like every flash and
  shake, it scales with the reader's Distortion Effects setting, down to nothing.
- **Him.** The owner: "the monster is the child's imagination seeing him as an adult is that trauma made real". The figure in the
  pines on the child visits is what a child makes of the machine on the hill. On the last night the grown reader sees the same figure,
  real. When the machine reaches her chest (`GENTLEMAN_AT`, 1.02 blocks) with the slot in this reader's sight, for two seconds the machine
  is not there and he is, drawn by the machine's own renderer in its place (`BorrascaOrrery.devour`):
  - he is four and a half times his height, so his coat stands on the housing's walls where the machine's cone stood;
  - he is folded down at the waist over the south wall;
  - his neck is drawn out down to the slot, and his faceless oval is split across and open around her, lined with her torn flesh and her
    bone for teeth;
  - his arms come down to her shoulders and the fingers hook over.

  Then he is gone and the machine is turning again. The server records that the reader saw him at the machine (`GentlemanAtMachine`).
  The account says: "For a second the machine wasn't there. Something tall and shining was, bent over the belt, its face split open
  around her." If the reader saw him in the pines as a child, it says "The tall thing from the pines was" instead.

## The machine, rebuilt

The owner: "Well the orrery needs to be rebuilt too". In the story the women are fed to "a large silver, conically shaped machine", and
its whirling "lit up the valley".

`client.BorrascaOrrery` is drawn by the machine block's renderer and can be seen from 256 blocks:

- a sixteen-sided silver cone stands on the housing's walls and rises ten blocks, in bands of plain plate, rivets and vents that glow
  while it runs, to a tip bent over like a hat (where the child got his hat from);
- three arms turn round it at three heights and speeds, each carrying a polished ball; the middle one turns the other way and carries
  a smaller ball turning round its own;
- two tilted hoops turn round it, slower;
- a streak of light goes round the cone;
- when the breaker is pulled, everything slows to a stop with the drum;
- the belt runs into a mouth at its foot: a lip over the slot with steel teeth hanging from it, and dark behind.

None of it is solid. The housing's walls are the only blocks, and the arms and hoops turn above head height. Its materials are on the
right half of the Shiny Gentleman's sheet (`tools/draw_shiny_gentleman.py`, now 128 by 64). His half is unchanged, and so is he.

## The breaker

The owner: "The player should be able to shut the belt off to save her; the account remembers that, but Dana does not get off the
belt. She does not respond to the player and just stares ahead, arms swaying slightly off the edge of the belt and eyes occasionally
looking to one side."

- **Before the slot (SAVED).** The belt stops with her whole on it. "Breaker's gone." / "Leave her. She won't get off." She stays
  there for the rest of the night. A word or a hand gets nothing from her.
- **At the blade (LATE).** It stops where it is, with that much of her taken. The depth is kept (`DanaLateAt`). Still alive, she makes
  one more sound ("aww... ooo") and goes still a few seconds later.
- **Before they fetch her.** She stays on her cot in room 2.

## The account

- Room 2 before: "In room 2 a woman lay on the cot looking at the ceiling. She didn't look at me."
- Saved: "Two men walked a woman out of the depot and laid her on the belt. I pulled the breaker before it took her in. She stayed
  where they had put her, looking up, and never once looked at me." With a hand held out: "I held out my hand. She didn't take it."
- Late: "...I pulled the breaker when it already had her feet." (or "her legs.", or "half of her."), then "She went quiet a little
  after the belt stopped."
- Fed: "Two men walked a woman out of the depot and laid her on the belt, and it took her into the machine feet first. It took a long
  time. She screamed until it had her chest. Pieces of her were coming out of the far end before it had her head." Then the pile,
  with 141-A if the band was read.
- Room 7 is made up for 264-A: "Room 7. The cot is made up with a clean blanket. A coat from town hangs on the hook."

## The note at the trailhead

The note now has a page of its own for this:

- "CONTENT NOTE: Abduction, coercion, sexual violence and death. Sexual violence stays off-screen. No assault is shown or heard."
- "GRAPHIC GORE: On the last visit a woman is fed alive into a machine. It is slow, and it is shown: her body torn apart, her
  screaming, and what is left of her."

## Art

- `tools/paint_character_skins.py` paints her three skins. The catalog and its check list `dana`, `dana_aside` and `dana_scream`.
- `tools/draw_borrasca_town_art.py` draws three new shop kinds. None of them collides:
  - `REMAINS` in two stages: pulp and bone, and hair with a felt slipper;
  - `WRISTBAND`: the cut band and its card;
  - `SMEAR` in two stages: the belt's rubber wet, and soaked.
- One new subtitled sound, `machine_wet`.

## Tests

- `BorrascaTests#theMachineTakesHerSlowlyAndTearsHerASliceAtATime`:
  - she lies on her cot, whole, and the room 2 cue is set;
  - near the side door, the men go for her;
  - from the slot:
    - the torn depth only ever grows, in sixty or more steps over at least 600 ticks;
    - she is alive and then still;
    - her voice goes awwwoo, then AAHHHHH, then -;
    - pieces come out while she is still on the belt;
  - the pile holds pulp, hair and slipper, and the band; the band reads 141-A;
  - the account keeps it, and her cot is empty after.
- `BorrascaTests#theBreakerStopsTheBeltButSheDoesNotGetOff`:
  - the trailhead note warns of the gore;
  - one reader's breaker stops the belt with her whole:
    - she stays where she lies, and a hand gets nothing;
    - the account remembers both;
  - another reader's stops it with her legs taken:
    - the depth stays;
    - she goes still;
    - the account says her legs.
- `BorrascaVisualProof` page 14 shows:
  - her laid on the belt, and at the knees, belly and chest;
  - her three faces;
  - the shaking caption;
  - the pile.
- Page 15 shows the two seconds as the machine's renderer draws them, under the red. Page 16 shows the machine running, from three sides,
  one of them slowing.

## Numbers

Protocol 176: the cast actor syncs `torn`. Witness 86/65/63 and layout 135 unchanged. New builds only.
