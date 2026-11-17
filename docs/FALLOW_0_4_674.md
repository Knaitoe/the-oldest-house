# Fallow 3a: the four minutes (0.4.674)

This is the first half of the third part of *Fallow* (`docs/FALLOW_PLAN.md`). Part 1 (0.4.672) built the station; part 2 (0.4.673) made
the afternoon. This half is the alarm and the four minutes up to the light. The second half (3b) adds what the light leaves: the shadows
on the wall and the station falling in. Part 4 is the cellar after (buried); part 5 is the road and the Witness source. Built to the
defaults the owner approved: a four-minute clock; the House's faint, not a death; no refuge but the cellar.

Fallow is still not a Witness source.

## What happens

Everything below runs on the afternoon's own clock (`Fallow.Site.tick`), which only moves while somebody is at the station. Times are in
ticks from the tone (`FallowStrike`).

1. **The tone** (fifteen seconds after the man on the road is gone, `AFTER_MAN` 300).
   - The ball game on the radio stops for a long two-note tone, which keeps on until the light.
   - The television in the corner shows its blank card (`FallowBlock.CARD`).
   - Readers in the store or the garage are told the game stopped; six seconds on, that a voice under the tone reads three instructions.
   - Boyd laughs it off as a test. Harlan says it is not.
   - Harlan comes off his afternoon (`Fallow.STRIKE`), Opal's bean stops being company, and nobody comes in to buy anything.
2. **The lids** (300).
   - Out in the wheat, on both pads, the concrete lid grinds off its seat to the east, a column every 13 ticks, three columns in all.
   - Under each seat is a shaft three across and four deep, walled and floored in concrete, with a concrete rim at each end of the
     mouth. The lid lies on the gravel past its seat. (New in this part's build of the pads; the hatch box moved west, clear of the
     lid's run.)
   - Every reader hears the grinding from each pad's way. Outside, they are told something heavy is grinding open in the wheat; under
     a roof, that the floor hums and the jars ring.
   - Otis says he has driven past the pads for twenty years and never seen anybody at them. Opal says the ground is buzzing.
   - **From here, nobody leaves by the House's door** until the light has had them (`Fallow.locked`, in
     `LiteraryVignettes.retreatLocked`).
3. **The launches** (360 and 390).
   - Something climbs out of each shaft on a column of fire, slowly and then faster, and leans away north as it goes. Every reader hears
     the roar from the pad's way and is told of it; the ground shakes a little at each.
   - The client draws them (below). The pads themselves stay hidden in the haze.
   - Harlan: "That's ours going up. Theirs will be coming for the holes."
   - A long way off to the east, sirens start, and come round again every eighteen seconds until the light.
4. **Everybody in the cellar** (430). Harlan says it. Then each goes to what they go to, running (`RUN` 0.2 a tick):
   - **Harlan** runs out of the garage, along the front and round to the cellar doors, opens them and holds them, calling every seven
     and a half seconds. A reader who lets the doors down finds them thrown back open.
     - A reader who puts a hand on the open doors near him (within six blocks) **takes the doors** from him. He tells them to hold them
       till everybody is down, and goes down himself. The reader's record keeps `HeldDoors`.
     - If nobody takes them, he stays at the doors, and at the end shuts them over the people below and stays out.
   - **Otis** goes to Opal. She takes her bean up off the forecourt in both hands and stands; he lifts her and carries her (she rides
     at his chest, `LiteraryActor.getPassengerAttachmentPoint`, his arms under her, `Fallow.CARRY`) out along the front, round to the
     steps and down, and sets her by the cot with her bean. He goes to the far end of the room.
   - **Faye** runs to the garage and works the lift's lever to bring the car down. It does not come down.
     - A reader who speaks to her takes her by the arm: she turns from the car, asks where Opal is, and comes after that reader
       wherever they go (`LEAD_PACE` 0.22). If she is caught on something where nobody sees, she is at the reader's back.
     - Once that reader is at the top of the cellar steps or below, she goes down to Opal. The reader's record keeps `BroughtFaye`.
     - If the reader leaves the station, she goes back to the lever.
   - **Boyd** goes to the rig's door and stays there, pulling at it and shouting for Faye, then for the keys, and will not be talked into
     a hole in the ground. He cannot be brought down.
5. **The wait.** Below, once Otis has set Opal down, a line every fifteen seconds between Otis, Opal and (if he came down) Harlan.
   Ten seconds before the light Opal asks if she can count; Otis tells her to go on; she counts to seven, a number a second.
6. **The doors** (forty ticks before the light, `SHUT`).
   - They are pulled shut: from below by Harlan if a reader took them from him, otherwise by Otis, or by Harlan from outside if he is
     still holding them. Readers below and readers near the doors are told which.
   - A reader still outside can open them again and go down. They come down again behind the last one in, a second and a half after
     nobody is on the top steps.
7. **The light** (4800 ticks, four minutes, after the lids).
   - Each reader's record keeps where it found them (`AtLight`: `cellar` if below, `outside` otherwise), where they stood and faced,
     and one more strike (`Strikes`). A reader who walks in after the light keeps nothing of it.
   - The television goes dark.
   - **A reader out in it** is whited out (the screen is the light) and two seconds later taken out by the House under a fade, back
     the way they came (`LabyrinthDoors.sendBack`). That is the faint; nothing is lost.
   - **A reader below** sees it come in round the doors at every crack, the ground shakes, the dust comes down out of the roof and does
     not stop, and ten seconds later the dark takes them too, the same way. (Part 4 replaces this with what the cellar is after.)
   - Spectators see all of it and earn nothing.

When the station has stood empty five seconds, the afternoon is over as before; a fresh one puts the television's picture back, slides
the lids back onto their seats, lets the cellar doors down, and sets everybody back at their places, nobody carried or carrying.

## On the reader's screen (`FallowStrikeClient`)

The server sends each reader at the station how far along the strike is every second (`FallowStrikePayload`), and at each launch and
the light. From it the client draws:

- **What goes up.** A long body of eight sides, fifteen blocks with a three-block nose, two dark bands where its stages join, lit on the
  side to the late sun in the west. It starts below the mouth and climbs out of it, slowly and then faster (2.2 τ² blocks), leaning
  north as it climbs. Under it, a flickering cone of fire and a glare round the nozzle, light added to whatever is behind.
- **The smoke.** A column of soft round puffs, thick in the middle and nothing at the edge, every 1.6 blocks up the way it went. Each
  spreads and drifts east from when it was left, and thins over about four minutes. A cloud rolls out across the pad from the mouth.
- **Over the haze.** All of it is drawn with the fog held off, so it stands up out of the haze that hides the pads, and tinted toward
  the haze's own colour the farther off it is. Its body writes depth; the smoke and fire do not.
- **The ground.** A small shake at each launch for six seconds; a hard one at the light outside, and a long fading one below.
- **The light.** Outside, the whole screen goes to the light in three ticks and stays until the House's fade takes over. Below, a pale
  wash over the dark that goes in a second and a half.
- The shaking and the light follow the screen-effects setting (`Comfort.effects`): at nothing, the light is still shown at a third.

## Single and multiplayer

- One reader alone can do everything: take the doors from Harlan, then go to the garage for Faye and bring her down.
- With two, one can take the doors while the other brings Faye; each keeps only what they did themselves.
- With more, the rest go down or stay out as they choose. Only one reader can lead Faye at a time.
- The clock, the people and the light are the station's, shared by everyone there. Each reader's record (`AtLight`, `HeldDoors`,
  `BroughtFaye`, `Strikes`) is their own.
- Everything said to a reader in particular (`sayTo`) is said to them; what anybody says aloud is heard by everyone near.

## Made as

- `FallowStrike` (new): the clock, the four people's four minutes, the doors, the light and the faint, the hand on the doors and the
  words during it, the lock.
- `Fallow`: the strike's state on each afternoon; a speed per person; nobody stops to listen once the tone is on; customers and chores
  stop; `stand` lets go of anybody carried; `afresh` puts the station back; the radio's ball game stops for the tone.
- `FallowSite`: the shafts under the lids; the hatch box moved; `lid` and `lidAt` (one function for the builder and the slide);
  `shaft`; the cellar's well a cell longer, with walkable steps and a landing.
- `SceneFootpaths`: a body that has found no way on waits a second before searching again.
- `Fallow.walk` / `FallowStrike.steps`: the well's steps walked straight, tread by tread. `FallowStrike.underDoors`: nobody under
  the doors when they come down.
- `FallowTexts`: the four minutes' lines (below). `FallowRegistry`: six sounds.
- `FallowStrikePayload`, `FallowStrikeClient`, `FallowRenderTypes` (solid body, soft smoke, added fire).
- `LiteraryActor`: Otis's arms hold Opal. `LiteraryRenderers.fallow`: the carrying pose.
- Sounds (`tools/generate_fallow_audio.py`, all subtitled): the tone (two notes beating through a small speaker), the lids (grinding,
  a motor's hum, a knock at the stop), the launch (a swell to tearing thunder that goes away upward), the siren (a far wail and a
  fainter one behind it), the light (a crack, a pressure felt more than heard, a long rolling roar) and the rumble over the cellar.
- `FallowTests.theFourMinutes`: the tone and the card; the lids slid, the shaft and its rim; the lock; Harlan holding the doors and one
  reader taking them; the other leading Faye down; Otis carrying Opal and her bean; Boyd at the rig; Opal counting; the doors shut
  before the light; each reader's place at it; the television dark; both readers taken out.

## Found and fixed in the making

- The hand on the doors kept nothing at first: the click's own record was saved after the strike's, over it. The strike now writes into
  the record the click was given.
- Faye would have gone for her milk in the middle of the four minutes if the afternoon's clock reached her time; customers stop at the
  tone.
- Nobody could walk down to the cellar. Its first step was a full block below the yard (part 1 lowered every step so the last would be on
  the floor, under the room's roof): a reader could drop down it and had to jump to climb out, and the people's footpaths found no way
  down at all. The well is a cell longer now, under eight lengths of door instead of six: three steps, each half a step at a time from
  the yard's own level, and at the foot a landing on the cellar's floor in the gap through its wall, still under the doors, so a body
  comes off the last step with open air over its head and walks on into the room.
- While someone could not get where they were going, the footpath search ran again every tick, the whole search each time, and the
  test server fell to two or three ticks a second. Every scene's walkers share that search (`SceneFootpaths`): one that has found no
  way on, or is stuck, now waits a second before it searches again.
- Then Faye stopped on the doors' curb. Stepping over the curb at the head of the steps, she arrived at the top step still standing on
  it; the search planned from there, could not step down a whole step from the curb's height, and walked her along the curb instead.
  The people now walk from the top of the steps down the well straight, a tread at a time, each foot on the highest thing under it.
- The doors came down at the last on whoever was on the upper steps. They wait until nobody, reader or one of the people, stands
  under them, and come down at the light at the latest.
- Spectators could not see the launches or the light: the client asked the station's own check for where the reader is, which leaves
  spectators out. The strike has its own, which lets them watch.
- Looking down the well, the far end showed the yard's earth: the edge of the cellar's roof, whose sides are the yard's ground. A
  fieldstone lintel lies over the gap now, which from below is a stone header over the way in.

## Every new line

Flagged for the owner. Nothing here names a colour or an age or says how the reader feels; speech is spoken by the people, cues are on
the action bar.

**Cues** (`FallowTexts`):
- "The ball game stops. A long tone comes out of the radio, two notes at once, and does not stop."
- "Under the tone a man's voice reads, slow: go below ground now. Stay away from windows. Do not use the telephone."
- The radio, to a hand while the tone is on: "The radio on the shelf. The tone comes out of it, steady, and the voice under it reading
  the same three things over."
- "Out in the wheat, both ways, something heavy grinds and keeps grinding." / under a roof: "The floor hums. The jars on the back shelf
  touch each other and ring."
- "A roar comes over the wheat. Out of the haze something climbs on a column of fire, slowly, then faster, then up and gone." / "Another,
  from the other side of the road."
- "A long way off to the east a siren starts up, and another past it, fainter."
- "You take the cellar doors from Harlan. He goes down the steps." / "Harlan throws the doors back open."
- "The cellar doors come down and lie shut." / "Harlan reaches up and pulls the doors down over the steps, and the dark comes in." /
  "Otis reaches up and pulls the doors down over the steps, and the dark comes in." / "The doors come down again behind you."
- "You take Faye by the arm."
- "Light comes in round the doors, at every crack at once." / "Dust comes down out of the roof and does not stop."

**Spoken:**
- Boyd: "It's a test. They do that." Harlan: "That's not the test. The test tells you it's a test."
- Otis: "Twenty years I've driven past those pads. Never once saw a soul out at them." Opal: "Mama, the ground's buzzing."
- Harlan: "That's ours going up. Theirs will be coming for the holes." / "Everybody in the cellar. Now."
- Harlan calling at the doors, in turn: "Down the steps! Mind your heads at the bottom!" / "Faye! Leave it!" / "Boyd! There's room!" /
  "Come on. Come on." (Faye's and Boyd's give way to the last once they are past calling.)
- Harlan to a reader: "Get down there. I've got the doors." / taking them: "You hold them, then. Don't let them shut till everybody's
  down." / at the end, still out: "Keep them shut. Don't open them for anything."
- Faye: "How do you get it down? How does it come down?" / "Everything we have is in that car." / "Where's Opal? Where is she?" /
  below: "There you are. There you are."
- Boyd at the rig: "Faye! Faye, come on!" / "Otis! Where's your keys? It's not even locked!" / "I'm not getting in a hole in the
  ground." / "This thing'll do sixty with the box on. We can be fifty miles off." / "Go on, then. Go on down."
- Otis: "Come here, sweetheart. Bring your bean." / "Hold on to me." / "There. You sit right there with your bean." / to a reader while
  he goes for her: "Go on down. I've got her."
- Opal: "I've got it. I've got it."
- Below, in turn: Otis "How long, do you figure?" / Harlan "Not long." / Opal "It needs the sun. My bean." / Otis "It'll keep. Beans
  are tough." / Opal "Is Boyd coming?" / Otis "He's coming." / Harlan "Keep back from the steps." / Opal "Can I count?" / Otis "Go on and
  count." / Opal "One." through "Seven."

**Subtitles:** "A long tone comes over the radio", "Heavy concrete grinds open", "A rocket roars up out of the wheat", "A siren wails far
off", "A roar shakes everything", "The ground shakes".
