# The list, part 3a: night one, up to the water tower (0.4.706)

The first half of the third part of *The list* (`docs/THE_LIST_PLAN.md`). Part 3 was one row in the plan: the party at the stock tank,
the seed house, the auger, coming to under the tower with the sack on, the walk home, Amber at her window, and the town dealt from
there. It is built in two halves:

- **3a (this release):** the party, the ride, the seed house and the auger, and coming to under the water tower with the body.
- **3b (0.4.707):** the walk home, Amber at her window, first light, what the other readers do with the rest of the night, and the
  House dealing Lamesa.

The House still does not deal the town. Night one is begun by hand, in the town, with `/ghosthouse test the_list night`.

## The night

The night has its own clock: ten o'clock when it begins, two in the morning at the gin, three when the reader comes to. It runs at a
third of a tick per tick, only while someone is in town, and every reader in town sees that hour in their own sky. Outside the town
nothing of it shows.

1. **The party.** Out past the back fence, in the pasture by the stock tank, the five on the list are at a party: Deek, Wes, Mel,
   Lesley and Shannon.
   - Wes's brother's white pickup is parked with its lights on, and a blue sedan beside it.
   - A radio plays a tape as loud as it goes. It is heard across the pasture, and fainter from the edge of town.
   - There is an ice chest, empty cans, a hissing camp lantern, a blanket on the grass and a torn cardboard case.
   - Mel has the camcorder out.
   - Every reader in town is told once that the party is on.
2. **The five talk.** Each of the five has two lines for any reader. The first comes once; after that it is always the second.
   Deek picks one reader when the night begins: the first through the door. When that reader comes near, he asks them to help him get
   a case of beer out of the seed house at the gin. Talked to again, he says to get in the truck.
3. **The ride.** The view goes black for everyone at the tank. When it comes back they are at the gin's yard, set down before the seed
   house's door, and it is two in the morning.
   - The party is taken up and the pickup is parked in the yard. The sedan stays at the tank.
   - The five stand round the door.
   - Anyone more than twenty-four blocks from the tank does not ride.
4. **The seed house.** Deek rolls the heavy door open along its rail and tells the one he picked to go on in: the case is in the back,
   past the auger. The door stays open while anyone else is inside; Deek says one at a time in there.
5. **The door shuts.** Once the one he picked is three blocks inside and alone:
   - The door rolls shut and a bolt drops.
   - The five say their lines outside it, in order. Mel can't get anything on the camera; Lesley asks if that is the auger; Deek
     didn't touch it; Shannon says to open it; Wes says it is stuck.
   - The lamp over the door goes out, and the bulb inside, with a lever's click. It is dark inside.
   - The auger turns and seed pours. Dust falls from the dark, the reader is drawn slowly toward the trench where the auger runs, and the
     view shakes: hard for the one inside, less for anyone near the seed house.
   - Then the view goes black: for the one inside, for everyone who rode, and for anyone within forty blocks.
6. **Coming to.** It is three in the morning.
   - The one who went in lies face down under the water tower with the sack over their head and the slasher's body (part 2) theirs.
     After seven and a half seconds they get up, with the body's walk.
   - Everyone else is where they stood. The five and the pickup are gone. The seed house is shut and dark.
   - Each reader is told where they are: the one who went in that they have come to under the tower; anyone else that nobody is left.

From here, part 3b takes the reader home.

## Single and multiplayer

- **Alone**, the one reader is the one Deek picks. They are at the party, ride, go in, and come to.
- **With others**, the night is the town's, one at a time.
  - Deek picks the first reader through the door. Only one night runs in a town; a second reader beginning it is not given a second
    night.
  - Everyone in town hears the radio and sees the party. Every reader gets the five's two lines; only the one Deek picked is asked,
    and only their word starts the ride.
  - Everyone within twenty-four blocks of the tank rides. Nobody else is moved.
  - At the gin anyone may go into the seed house, but the door shuts only on the one Deek picked, alone.
  - Only the one Deek picked is locked in. Every other reader can leave by the House's door throughout.
  - Everyone near feels the auger, hears it and goes black at the cut. Only the one who went in comes to under the tower, with the body.
  - The records are each reader's own: the one who went in keeps that they came to (`CameTo1`), and anyone who rode keeps that they saw
    the door shut (`SawShut1`).
- **Spectators** see the sky and hear what anyone near would hear. They ride nowhere, are never picked and earn nothing.

## Retreat

Night one can be left by the House's door until the seed house door shuts. From then until the cut, the one inside cannot leave; once
they come to, the door is theirs again. Every other reader can leave at any time.

## Made as

- **`TheListNight`** (new): the night.
  - Saved in the town's shared record (`Night`: the slasher, the phase, the time in it, the hour, whether Deek has asked, who rode).
  - Phases: party, ride, seed house, shut, after.
  - `begin` (the first reader in town) and `arrive` (anyone who comes into town while it is on); `end` (an operator's) puts the town
    back as it was, the seed house's door, lamp and bulb with it.
  - `tick` from the town's own tick; the hour is sent to every reader in town every second, and at once on a change.
  - `locked`: the one inside, from the door shutting until the cut. `LiteraryVignettes.retreatLocked` asks it.
- **`TheListPeople`**: where each of the five is now (`spot`): at the tank for the party and the ride, at the gin for the seed house,
  gone home after it; everyone else at their own place. One look over the whole town finds each of them wherever they are, so nobody is
  made twice, and anyone the night has sent home is let go. Mel holds the camcorder from the party to the seed house.
- **`TheListBody.comeTo`**: the body given lying face down, the sack on its head, for as long as asked; the get-up cue is the coming to.
- **`TownCar`**: a parked car can have its lights on (synced), drawn at full brightness.
- **`TheListBlock`**: a `party` kind in eight stages: the radio, the ice chest, the empty cans, the lantern (light fourteen), the blanket,
  the cardboard case, the lamp over the door out, and the bulb out. The cans, the blanket and the two dark lamps take no collision.
- **`TheListCommands`**: `/ghosthouse test the_list night` (begins it, or moves it on), `night party|seed|shut|after` and `night off`.
- **Client**: `TheListClient` keeps the night's hour (`TheListNightPayload`, protocol 203) and shakes the view by the payload's amount,
  scaled by Distortion Effects. `NovelSceneClient` takes the town's hour before any other scene's.
- **Art** (`tools/draw_the_list_art.py`): the eight party models and their eleven textures at sixteen pixels.
- **Sound** (`tools/generate_the_list_audio.py`): five synthesized sounds, each subtitled.

## Tests

`TheListTests.nightOneTheSeedHouse` (new, batch `the_list_seed`), with a second reader who rides along:

- The night begins round the first reader, and only one night runs. It is ten o'clock; the party is heard by both readers; the hour is
  sent, running; the radio and lantern are set out and the lantern is lit.
- The five stand at the tank; the pickup is there with its lights on, and the sedan. The radio is heard; Mel has the camera.
- The second reader talks to Wes and to Deek: the party's own lines, and no ride.
- The one Deek picked comes near and is asked; talked to again, they ride. The view goes black for both readers, and the House's door is
  still theirs.
- At the gin: both set down before the door, two in the morning, the party taken up, the pickup in the yard, Deek at the door, the door
  open, the send-in line.
- With the second reader inside too, the door stays open. With the second reader out, it shuts: the one inside is locked in, the other
  is not; the door rolls shut, the lights go out and it is dark inside; the auger is heard.
- After: the one who went in is under the tower, face down with the body and the sack; the second reader has no body and stands where
  they stood; it is three in the morning; each record keeps its night; the House's door is theirs again; the seed house stays shut and
  dark; the five and the pickup are gone.
- The coming-to and nobody-left cues; the body gets up with its walk.
- Ended, the seed house is put back.

`TheListTests.theTownsPeopleAtTheirPlaces`, from run 1729: every list case shares the one town, and in the full suite a Deek left
elsewhere in the town by an earlier case was counted beside the one at his place. The town's own pass now finds them wherever they are
and keeps one, the case runs that pass before counting, and every list case ends any night an earlier case left going.

## Seen in the frames

Twelve frames at eye level, of the party, the open seed house and the shut one, were looked at before this was called done.

- The party, the pickup and the sedan, the five and their things read as they should.
- The bulb inside the seed house hung at the roof, nine blocks up, and from the floor it was a speck. It now hangs on a chain, three
  blocks down from the roof, where the one inside can see it go out.

## Every new line

Grey cues on the action bar:

- To everyone in town while the party is on: "Out past the back fence, a radio. Headlights in the pasture."
- To the one who went in, coming to: "You come to under the water tower, face down in the caliche."
- To anyone else in town after the cut: "Nobody is left. The trucks are gone. The seed house door is shut, the lamp over it out."

Speech, over the speaker's head as their name:

- Deek: "You came. Get something out of the ice chest." / "We're waiting on one more thing. Then we'll go."
- Wes: "That's my brother's tape. Don't touch the radio." / "Deek's got a plan. Deek always has a plan."
- Mel: "Say something for the camera. No, something good." / "The battery's at half. I'm saving it for later."
- Lesley: "Don't lean on the truck. It's his dad's." / "Somebody saw coyotes out here last week. Mel wants to film one."
- Shannon: "Sit if you're staying. There are stickers in the blanket." / "Ask Deek what we're doing after. He won't tell me."
- Deek to the one he picked: "Wes's brother left a case of beer in the seed house at the gin. That door takes two. You and me?" /
  "Get in the truck."
- Deek at the gin: "It's in the back, past the auger. Go on. I've got the door." / "One at a time in there."
- Outside the shut door, in order: Deek, "Count to a hundred. Then knock." Wes, "Lights!" Mel, "Hold still. I can't get anything."
  Lesley, "Deek. Is that the auger?" Deek, "I didn't touch it." Shannon, "Open it. Open it!" Wes, "It's stuck. Deek, it's stuck."

What a hand on the party's things says:

- The radio: "A radio with a tape in it, turned up as far as it goes."
- The ice chest: "An ice chest. Ice water and two cans floating in it."
- The cans: "Empty cans, most of them stepped on."
- The lantern: "A camp lantern, hissing. Bugs knock against the glass."
- The blanket: "A blanket spread on the grass. Sandburs in the weave."
- The case: "A cardboard case, torn open. Empty."
- The lamp over the seed house door, dark: "The lamp over the door. Out."
- The bulb inside, dark: "The bulb. Out."

Subtitles: "A radio plays", "A heavy door rolls on its rail", "A bolt drops", "An auger turns", "Seed pours".
