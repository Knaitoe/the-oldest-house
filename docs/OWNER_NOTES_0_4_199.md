# Owner's playtest notes, October 30 evening — 0.4.199

Fixes to things the owner saw while playing. Hallways, puzzle rooms and artifacts each have their own documents.

## The shelter dance
- **"Player arms still need proper scaling."** The first-person arms used the vanilla empty-hand basis at full size, spread past the partner's sides.
  - Each arm is now aimed: the hand rests at the partner's side a little below the chest, and the arm runs back down to a shoulder below the view, thumb side up.
  - The arms are drawn at three quarters of the model's size, and they still sway with the partner (`DanceClient.aim`).
  - The placement was checked in an offline replica of the arm transform and the hand projection. The replica's old placement matched the owner's screenshot.
- **"Make sure npc stops following player after the dance."** The step back after the dance was aimed from the reader's current position on every tick, so the stranger trailed the reader around. It is now taken once, from where the reader stood when the song was let go. The stranger stays there (`HouseReadShelter.partner`, `ManBack`/`WomanBack`).

## The Ghosts set
- **"Remove the banisters atop the basement drop so the room behind is actually accessible."** The rails round the utility strip's stairwell are gone. The floor either side of the stair leads to the washer and the shelves behind it.
- **"Ensure banisters are connected."**
  - The stair rail's top no longer runs on past the corner. It stops at the landing banister's line, turns along it to meet the first landing post, and a newel stands on the landing at the corner (`tools/generate_ghosts_stairs.py`).
  - The landing banister's top rail is white like the stair's, so the two read as one banister.
- **"Tv is cutting into shelf."** The den's television screen is wider than its stand, so the shelf beside it now stands one block along.
- **"Furniture placement is also a bit off."**
  - The family corner's television faced the wall; it now faces the chairs.
  - The side table was wedged behind the green armchair; it now stands beside it.
  - The shelf has the wall to itself.
- **Living-room textures.** The dresser's little photograph frame sampled a mostly transparent corner of the photographs texture, so it drew as a thin black wire. Its face now shows a whole photograph (`tools/draw_barrett_home.py`).
- **"The director should address the player as soon as they enter."** Barry now calls the camera operator over from the video village as they come in. On the first morning: "You're the camera? Good. Over here, in the den, by the monitors." On later mornings he says something shorter, and when the day is already finished he invites them to come and watch it. A grey line says where he is.
- **A look tool.** `GhostsLookProof`, run with `-PghostsLook`, draws these corners from the builders' own coordinates with the production block models. It saves them to `build/ghosts-look`. It is a developer's tool, not a CI proof.

## The night ward
- **"The double text should also be fixed."** A cue inside a novel site went to the action bar and also to the site's own caption, so it showed twice. Inside a site it now goes only to the caption, sent the same tick (`NovelVignettes.cue`, `CueFresh`).
- **"The monitor in the hospital needs to have a base."** The "monitor" is the red call button on the wall, standing directly over a light seam in the dado, which reads as a screen on a pole. It is part of the clinical redesign of the ward that follows: a proper call unit, and a monitor on a wheeled stand with a base.

## Third Floor Girls' (the Bloody Mary dare)
- **"The changes to the players face ... shouldn't start until the second count at least and should be much more gradual. like, difficult to discern until the face returns to normal."**
  - On the first count the reflection does not change.
  - In the second count (about 26 seconds of staring), one eye drifts a pixel aside, cross-fading over fourteen seconds, and the mouth begins to pale, no further than halfway.
  - Through the third count the other eye drifts. The mouth pales away and then comes back wrong over ten seconds: lower and wider, its far corner dragged down.
  - Each change fades in eight steps, so no single frame jumps; the difference is plain only when the face snaps back to the reader's own (`DareReflection.key`, `toward`).
  - The dare's face proof shows the six moments: before, first count, an eye drifting, the mouth paling, both eyes, and the wrong mouth.
- **"We should also try to not have bloody mary's arm poking through the player."** Her arms were posed reaching round the reader to the collarbones, so in the glass they went through the reflection's chest and out of its front.
  - She now stands further back, still over the reader's right shoulder.
  - Her inner hand comes up from behind and rests on top of the reader's shoulder, beside the neck. Her other arm hangs at her side.
  - The pose was fitted in an offline model of the two bodies, so no part of her comes out of the front of the reflection's chest. It was then checked in the dare's third-count view (`DareReflection.draw`).

## The buzzer threshold (the home-entrance opening)
- **"Can you do anything to make the voice actually sound human?"** The voice at the intercom had been summed tones. It is now a person's voice: speech from the Piper neural voice en_US-ljspeech-high, trained on the LJ Speech dataset (public domain), saved once as `art/foyer_voice_source.wav` by `tools/make_foyer_voice.py`.
  - What it says: "Hello? ... Hello. It's me. I'm just outside, at the bottom. Can you... press it? Please. It's cold out here. I can see you."
  - `tools/generate_foyer_audio.py` puts it through the building's intercom: a telephone-narrow speaker, a little overdriven, with a faint hum and the odd crackle.
  - **"Please ensure the voice in the vignette sounds human!"** The first pass drove the speaker hard and dropped whole words, so it read as a machine. Now every word carries, and only now and then does the line thin for a moment.
    - Measured against the clean voice, the speech went from 8.2 dB above the distortion and noise to 17.8 dB.
  - It plays once, as before, from the intercom, now at its natural pitch. Its subtitle reads "A voice through the intercom".
  - The other four foyer cues are unchanged, and CI never needs the voice model.

## Chalk
- **"The player should be able to erase chalk markings by right-clicking to 'rub them' out."** A right-click on a mark now rubs it out in a little chalk dust, with a soft brushing sound (`ChalkMarkBlock.useWithoutItem`, `NavigationAids.rub`).
  - It works with an empty hand, with most things in the hand, and with the chalk itself, with or without sneaking.
  - A held block still places as before.
  - As before, only the reader who drew a mark can rub it out; anyone else is told "You can only rub out your own chalk mark."
  - The companions case right-clicks a mark through the native interaction: a peer's click leaves it; the owner's click, then the owner's chalk, rub it out.

## Notes from the other room
- **"Notes addressing players who ate in the House – 'stop chewing so loudly. I could hear you all the way from the other room' something like that."** A meal finished inside the House is the eater's own fact: what they ate, and when (`HouseCorrespondence.ate`).
  - At least a room later, the next loose paper that reader opens is a note slipped under the door, "From the other room", in a household hand (`HouseRemarks`).
  - There are five notes, each written once and in order, and each needs a new meal. They come no closer than three crossings apart.
    - "Could you stop chewing so loudly? I could hear you all the way from the other room."
    - "Was that {what they ate}? You ate it with your mouth open. The walls in here are thinner than they look."
    - The crumbs swept up in the hall.
    - The glass against the wall.
    - "We haven't eaten in a long time."
  - The notes are kept and read like any other correspondence and confer nothing. A meal outside the House is nobody's business, and a peer who has not eaten hears nothing.
  - The 108 approved correspondence pieces are unchanged; these notes stand beside them.
  - New case: `CollectedCorrespondenceTests.aReaderWhoAteInTheHouseHearsAboutItFromTheOtherRoom`.

## The way up the great staircase is shorter than the way down
- **"Making the climb up the grand staircase shorter than the way down could be really cool if we can make that work."** It works because the spiral repeats exactly: every full turn is the same 192 treads, 70 blocks lower (`StaircaseAscent`).
  - A reader who climbs one whole turn, from a corner landing to the same corner a turn up, is carried up two more turns from that landing. They land on the same place on the same corner, facing the same way, with their stride and their following companions. Each turn climbed rises three, so the climb back up takes about half as long as the descent.
  - The way down is never shortened.
  - The top two turns, where the leaves and the hearths are, are always walked.
  - Nobody else sees it happen. Readers climbing together, within a few blocks of each other, are carried together. Anyone else near either landing, spectators included, keeps the climb walked.
  - A landing whose floor has worn through is passed over until the next one.
  - This matters most after the peaceful release, when the reader climbs the whole staircase home.
  - **Literature:** the completed account gains a page in Will's hand when the climb was shortened: "I counted the turns going down, and again coming up. There were fewer coming up. I counted twice."
  - New case: `StaircaseAccessTests.theClimbBackUpIsShorterThanTheWayDown`. It covers going down, a turn climbed and the next, a party carried together, a spectator keeping the climb walked, the top turns walked, and the account's page. The staircase job's count is now 12.

## Charcoal sketches
- **"You should let players interact with objects with charcoal to make the charcoal sketches in their books, too."** A right-click with charcoal on any object that gives a rubbing now takes it, the same as a click with the journal in hand (`Journals.rubbing`, `Journals.carried`).
  - It goes into the reader's own journal, wherever it is on them; on the server, their current journal is chosen before an old copy.
  - The charcoal is not used up.
  - Without a journal of one's own, charcoal sketches nothing.
  - The journal rubbing case now sketches the well's initials with charcoal, and checks that charcoal alone does nothing.

## Someone in the household slices
- **"When the domestic house slices are still inaccessible in hallways, there should be a small chance of players seeing occupants living inside."** About one visit in eight to a hall with a household slice that leads nowhere yet has someone in it (`DomesticGlimpses`).
  - A slice "leads nowhere yet" until its home has bled in. Slices with no home behind them count too.
  - They are put in while the reader is across the hall and not looking, back to the hall:
    - at the counter in a kitchen;
    - at the tub in a laundry;
    - laying the table in a dining room;
    - standing by the bed in a bedroom.
  - Where the slice belongs to one of the four homes, they are dressed as that household might be: the family, the students, the father on his own, the grandmother.
  - A soft sound of what they are doing comes from the slice. Only that reader sees or hears them.
  - They never turn round. They stay while watched. Once looked at and then looked away from, they are gone, and nobody comes back on that visit.
  - A reader who walks right up finds the lamp blinking for them, and the slice empty when it comes back.
  - Nothing is said, given or counted. Anyone left behind by a restart or a departed reader leaves too.
  - New case: `ExplorationPassTests.someoneIsSometimesSeenInAHouseholdSliceThatLeadsNowhereYet`.

## The first puzzle room: Borrowed Light
- **"Borrowed Light would be my first choice to prototype."** A dim dressing room, gray slot 120, offered only after a resolved story and a few halls (`BorrowedLight`).
  - Everything is in view before anything is touched: a stage lamp lit behind a shut shutter, a dressing mirror on a scraped track with three deeper scrapes, and a cold brass sun beside a far door with no handle.
  - Lift the catch and the beam comes out. A hand on the glass turns the mirror an eighth; a kick at its base slides it to the next stop. Lay the beam on the sun and the door gives.
  - No countdown, no damage, nothing used up. Every action answers at once. The room keeps how it was left, and once solved it stays solved, the beam still on the sun.
  - Solved, it comes back only now and then, as a pleasant room on the way.
  - Not a story: nothing counted toward the Witness.
  - Canon: [BORROWED_LIGHT_0_4_199.md](BORROWED_LIGHT_0_4_199.md). New cases: `ExplorationPassTests.borrowedLightIsSolvedByLayingTheLampsBeamOnTheSun` and `borrowedLightComesAfterAStoryAndSomeWandering`; a fifth client art view.

## The Goatman's bedside ending and its horns
- **"The Goatman ending sequence doesn't show the Goatman-kid crawl. When the Goatman jumps up to the player it should be as the Goatman, not in his kid disguise."** The crawl to the bed used to be hidden: the body turned invisible for the whole way. Now it is seen. The child it is wearing goes down on its hands and knees and crawls the aisle to the foot of the bed, low, below the sleeper's eye line (`GoatmanBedtime`, stage 5).
  - At the foot of the bed it comes up as itself, the Goatman, and jumps up in front of the sleeper before the black (stage 6).
  - Going on to the next bed it is down on the floor again as the same child. The haunting that follows remembers that child's face, as before.
  - Dawn still takes the body away, and each new night stages a fresh one, so the Goatman's form never carries into the next evening.
- **"Can we also give the Goatman curled ram horns?"** The swept-back goat horns are replaced by a ram's curl on each side (`GoatmanHorns`).
  - Each horn rises off the top corner of the skull, sweeps back over the head, down behind the ear and forward under it, the tip turned up by the cheek.
  - It is seven ridged segments, thickest at the root, set just outside the side of the head so it never cuts into the skull, and it moves with the head.
  - The horns have their own texture (`goatman_horns.png`, drawn by `tools/draw_goatman_horns.py`). The Goatman's skin is unchanged byte for byte; the old horns stay in its mesh, hidden.
  - They show wherever the Goatman is drawn: the bedside rise, the window circuit, the glimpses of the haunting, and both art proofs.
  - The bedside case now checks that the crawl is visible in the child's shape and the rise is the Goatman.

## Tests
- The shelter case walks the reader away after the dance and checks that the stranger stays put.
- The Ghosts namespace (24 cases), the novel sites (16) and the shelter case pass locally.
