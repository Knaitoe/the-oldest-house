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

## The buzzer threshold (the home-entrance opening)
- **"Can you do anything to make the voice actually sound human?"** The voice at the intercom had been summed tones. It is now a person's voice: speech from the Piper neural voice en_US-ljspeech-high, trained on the LJ Speech dataset (public domain), saved once as `art/foyer_voice_source.wav` by `tools/make_foyer_voice.py`.
  - What it says: "Hello? ... Hello. It's me. I'm just outside, at the bottom. Can you... press it? Please. It's cold out here. I can see you."
  - `tools/generate_foyer_audio.py` puts it through the building's intercom: a narrow, ringing speaker, driven too hard, humming, crackling and breaking up, so a word or two carries and the rest does not.
  - It plays once, as before, from the intercom, now at its natural pitch. Its subtitle reads "A voice through the intercom".
  - The other four foyer cues are unchanged, and CI never needs the voice model.

## Chalk
- **"The player should be able to erase chalk markings by right-clicking to 'rub them' out."** A right-click on a mark now rubs it out in a little chalk dust, with a soft brushing sound (`ChalkMarkBlock.useWithoutItem`, `NavigationAids.rub`).
  - It works with an empty hand, with most things in the hand, and with the chalk itself, with or without sneaking.
  - A held block still places as before.
  - As before, only the reader who drew a mark can rub it out; anyone else is told "You can only rub out your own chalk mark."
  - The companions case right-clicks a mark through the native interaction: a peer's click leaves it; the owner's click, then the owner's chalk, rub it out.

## Tests
- The shelter case walks the reader away after the dance and checks that the stranger stays put.
- The Ghosts namespace (24 cases), the novel sites (16) and the shelter case pass locally.
