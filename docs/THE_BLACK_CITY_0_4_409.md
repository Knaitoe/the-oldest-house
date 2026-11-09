# The City on the Ice (0.4.409)

This is the next series' Lovecraft proposal ([DESIGN_PROPOSALS_NEXT.md](DESIGN_PROPOSALS_NEXT.md), section 2a), built as "The Black City". The owner, on the next series: "Continue with textures and then vignettes ... Don't wait for my approval, just continue." Its open decisions are taken as follows:

- **The Mountains, not Innsmouth**, as the proposal recommends.
- **What fills the tunnel** is drawn in code as a placeholder the owner can replace. It is a block texture, `textures/block/city/mass.png`, an animated dark sheen with eyes opening and closing in it.
- **The look back** shows only mist, and the mist moves wrongly.

The source is treated as still protected, so only its shape is used:

- no names, no text and no title from it;
- every line here is original;
- in the game the place is called "the city on the ice".

## The place

The place lies outside, 128 blocks across and 192 deep, under a low sun that never quite sets. Its base stands high in the outside dimension (`CitySite.BASE_Y`, 100) so the pit has room under it.

- **The camp.** The reader comes out of the House's door in the back wall of the radio hut. The radio hisses on a crate, with the operator's log on one side of it and a message pad on the other.
  - **Outside, in a wind that blows the snow flat:**
    - three torn tents;
    - an empty dog line, its harnesses on the snow with the buckles still done up;
    - the geology tent with its rock cores;
    - a dissecting table under a tarp, its straps cut;
    - an aeroplane whose fabric has been opened in long cuts.
  - **The way on** is the mess hut's door.
- **The pass.** Cut steps climb north between walls of rock, with fixed-rope stakes along both edges. The peaks either side are stepped, too regular to be peaks.
  - Three nooks in the east wall are out of the wind.
  - From the crest, the city lies below and the far peaks stand beyond it.
- **The city.** A stair goes down into a plaza of dark stone.
  - Low walls carved in fives run in rings round the middle.
  - Five stepped towers stand around it, each with a doorway too tall for anyone who lived here to have been small.
  - In the middle is a square pit.
- **The ramp.** It turns down the walls of the pit, a turn and a half, half a block lower every few steps, with a curb and a low wall along its inner edge.
  - Twelve carved panels are set into the wall along it, four blocks wide and three high.
  - At the foot is a landing, and the shaft in the middle drops into deep water.
- **The tunnel.** It runs north from the landing, lit by hurricane lamps, to a cavern: a warm shore by dark water, under a low roof.
  - The prints of birds nobody sees run both ways in the grit.

## The panels

Each panel is a wordless 64-pixel carving. A reader reads one by standing in front of it and looking at it for three seconds. They are read in order, and each reader's reading is their own. A later panel looked at first "goes on from something further up the ramp".

What each panel shows:

1. Shapes coming down out of the stars, on wings like fans.
2. A warm sea, and land rising.
3. Terraces of five steps.
4. The city finished under a large sun.
5. Something without a shape poured out of a vat.
6. The shapeless things carrying stones.
7. The shapeless things with eyes, then mouths.
8. The rising.
9. The shapeless things driven down under the city.
10. The ice coming.
11. The builders going down to a sea under the city by a turning ramp.
12. A tunnel full from wall to wall, the stone under it cut out.

## The wind and the cold

Both are shared by everyone in the city.

- **The gusts.** On the upper pass and the crest, gusts blow half of every five seconds and push readers back down. Crouching halves the push.
- **The cold.** It builds out in the wind, and the frost overlay and the vanilla slowness come with it. It is held short of freezing, so it never hurts.
- **The nooks** in the rock keep both off.

## The tunnel

The tunnel is shared too.

1. **The trigger.** A reader who has read all twelve panels stands on the shore. A reader who has not hears something pipe far off over the water, and nothing comes.
2. **The piping.** Something pipes over the water. Everyone in the cavern, the tunnel or the pit is in what follows. It counts only for readers who had read all twelve when it began or when they came into it.
3. **The chase.** What fills the tunnel comes up out of the water.
   - **Speed and body:** it fills the tunnel from wall to wall at a fixed pace, slower than a walk, with a body sixteen blocks long behind its front.
   - **Behind it:** the lamps go out as it passes.
   - **Ahead of it:** in the tunnel, its push moves the air up toward the readers.
   - **The route:** it comes up the tunnel and then the ramp, to the rim.
4. **Escaping.** A reader who gets out of the pit ahead of it has survived.
5. **Being taken.** A reader it reaches is taken into the dark. They wake on the ice by the hut, unharmed, with every carving still read. A reader can be taken more than once.
6. **Afterwards.** When nobody is left in it, it goes back down and the tunnel empties. The lamps are lit again once the tunnel has been empty for fifteen seconds.

The mass is a non-colliding block that fills empty cells, slice by slice along the chase. It never stops anyone, and never replaces anything that is there.

## The look back, and the account

**Resolution.** A reader who has read all twelve panels and outrun what fills the tunnel goes back over the pass. On the crest, looking back at the far peaks for three seconds, they see the mist on them moving against the wind. The mist is cloud particles sent to that reader alone. Seeing it is the end of it.

- **The account** waits on the message pad by the radio. It is read after the radio log, and there is one source (`the_black_city`, understanding).
- **The camp at dawn.** After that, the camp is at dawn for that reader, on a per-reader scene clock.

The account says:

- the empty camp and the climb;
- the city;
- the carvings that reader read, in order;
- the tunnel, and what came up it;
- the times they were taken, if any;
- the mist moving against the wind;
- that nobody was calling on the radio.

## Multiplayer

- **Shared:** the wind, the cold, the chase and the lamps going out.
- **Each reader's own:** the panels read, being taken and waking, the look back, the dawn and the account.

## Made

- **Code:**
  - `CitySite` builds the place whole: terrain, hut, camp, pass, towers, rings, pit and ramp, tunnel, cavern and panels. It also lays out the ramp and the chase slices.
  - `BlackCity` runs the wind, the panels, the chase, the dark, the look back and the account.
  - `CityTexts` holds the radio log, the panel lines and the account.
- **`CityRegistry` registers:**
  - six materials (dark stone, stone carved in fives, snow, ice, canvas, boards);
  - `CityBlock` (twelve kinds: radio, logbook, pad, lamp, crate, stove, harness, cores, table, fabric, prop, prints);
  - `CityMural` (twelve panels of twelve parts);
  - the mass block;
  - five sounds: wind, flap, piping, birds, mass.
- **Network:** `CityPayload` and `CityClient` carry the per-reader dawn (scene clock mode 20), plus the camp's blowing snow, the clear air past the crest and the dark under the city.
- **Art and sound:** `tools/draw_city_art.py` draws 39 textures and 175 models and states, the twelve panels included. `tools/generate_city_audio.py` synthesizes the sounds.
- New builds only.

## Tests

`CityTests` has three cases in the literary namespace:

- **the camp and the carvings:**
  - the place is built whole, with the source, the twelve panels in order down the ramp, the clear tunnel and the lit lamps;
  - the wind pushes and the cold builds short of freezing, and a nook keeps both off;
  - a later panel says nothing yet, then the first and the twelfth are read;
- **the tunnel:**
  - a reader without all twelve finds nothing at the shore;
  - one with all twelve brings the piping and the chase;
  - one reader gets out ahead and survives;
  - one who stays is taken, wakes by the hut with every carving read, and the shore's lamps go out;
  - afterwards the tunnel empties;
- **the look back:** the reader who outran it looks back and has the account, and the one who didn't does not.

## What stays

- **Witness:** 84 sources / 63 resolutions / two kinds / three endings (one new understanding source).
- **Layout:** 133 (slot 235).
- **Protocol:** 166.
