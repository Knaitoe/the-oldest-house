# The Ones Already Inside 6: the injury, and the failed bodies (0.4.606)

The plan's sixth slice (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): "N4 and the failed bodies: the intrusion, the glass, the cut on both;
the pin; how failed bodies move, batter and take." The script's N4: "The first failed body comes through from the passages while Vera
is between rooms. It pulls at the door's frame, then batters the latch. It goes for whoever is nearest. Dan, if he is near, puts
himself between it and a reader; glass breaks (the hall stand's, or the parlor's door light); his hand is cut. In his room at the same
moment Daniel's hand opens in the same place." The House still does not deal the house.

New builds only. No layout, protocol or Witness change.

## What happens (`LodgingStory`, `LodgingInjury`)

After the rooms (0.4.604) the three are settled in the workroom. Twenty seconds of the house's clock later:

1. **Between rooms.** Vera steps out of the workroom into the corridor and stands there, between the workroom and her own room. Dan and
   Rae come back down the stair to the hall; Dan stands in front of the bench.
2. **At the jib door.** The failed body in the west passage pulls at the papered door at the end of the back passage, three seconds,
   the door knocking in its frame. Readers within twenty-four blocks are told, once: "At the end of the back passage the papered door
   shifts in its frame, stops, and shifts again."
3. **The latch.** It batters the door with both fists, six blows over three seconds, and the latch goes: the door bursts open.
4. **Loose.** It comes through into the back passage and walks, slowly, its arms out, toward the nearest reader on the ground floor
   (the hall or the back passage), by the mouth of the hall and never through a wall.
5. **Dan in its way.** Dan waits by the hall stand. When it comes within five blocks of him there, he goes to it and puts himself in
   its way. It throws him back against the stand: the glass breaks, and his hand is cut across the back. In his room at the same
   moment Daniel's hand opens in the same place.
   - A reader who can see Dan: "The glass in the hall stand breaks. Dan's hand comes away from it cut across the back."
   - A reader with Daniel (in his room, at the open hatch, or crouched at his door): "The back of his hand opens across the knuckles,
     where nothing has touched it." and, in the panel, Daniel: "What did you do? What did they do to him?"
   - A few seconds later Dan, over his head: "It's nothing. It's a cut."
   - Dan holds his cut hand to his chest for ten seconds.
6. **It goes back** through the jib door into the passage, and pulls the door to behind it once nobody is standing in the doorway.

### Kept off

Five blows on it before it reaches Dan drive it back through the door, and nothing is cut. That stands. Each blow stops it where it is
and puts it back half a step ("It stops where it is put, and comes on again."). While a reader is fighting it, Dan holds back by the
stand and lets them.

Then the rule comes later, deliberately, with both men there. Thirty seconds after it has gone back with nothing cut, Vera comes down
from the corridor, through the hall and the back passage to Daniel's door, draws his bolt, opens it and leaves it open, and stands by
his frame. Dan comes after her once she is in the hall, and stands at the frame's other side holding his hand out. She pricks it with a
pin and says the script's line: "Watch his hand. Not this one. His." Daniel's hand bleeds where Dan's was pricked. Readers within ten
blocks of her have it in the panel.

### How it takes

Reaching a reader, it closes a hand on them ("A hand closes on your arm and does not let go. It walks you back the way it came.") and
walks them back toward the jib door, a step behind it. Two blows from somebody else, or three from the one held, break the grip ("Its
hand comes off you."). A reader it gets as far as the door it lets go in the passage, in the dark behind the paper ("In the dark behind
the paper it lets go of you, and goes on without you."), and goes back. Nothing it does hurts the reader. What becomes of a reader
taken into the passages is slice 12.

## How they look

- **Its walk.** Arms out at the height of a face, the fingers a little apart, the head pushed forward; battering, both fists coming
  down on the door in turn with the shoulders behind each; walking somebody back, one arm out behind and low, holding.
- **Dan hurt.** Both elbows bent: the cut hand drawn in to his chest, the other cupped under it, his head bent over them.
- **Vera with the pin.** The pin raised to the hand he holds out, her other hand at his wrist.
- **The cut.** Dan's and Daniel's sheets have each a cut and a pricked version (`literary_lodging_dan_cut`, `_prick`,
  `literary_lodging_daniel_cut`, `_prick`): across the back of the right hand, the line along the knuckles darker where it is deepest
  and two drops run toward the fingers; or one bead of blood and the start of a run. Painted by `tools/paint_character_skins.py` (the
  new `wound` look), listed in `character_art.json` on the two men's own phases, and picked by the marks they carry
  (`LodgingPeople.dressed`).
- **The glass.** The hall stand's upper half has a third stage, its glass broken: a star of cracks from low on the left where his hand
  went in, a piece gone out of it showing the board behind, and blood at its lower edge. Drawn by `tools/draw_lodging_house.py`
  (`mirror_broken`).

## What the frames showed (and what changed)

Each new pose was drawn on the cast model with its own sheet in a local review screen, before and after these changes:

- Dan's first hurt pose put two straight arms forward; seen from the front it read as hands up. Both elbows bend now.
- The cut showed on both hands: the cast's left arm mirrored the right arm's pixels. The lodging house's people now draw the left arm
  from its own pixels, bent or straight (the Pell house's people already did), so a wound on one hand stays on one hand.
- Daniel's straps were at shoulder height, a pixel outside his shoulders, so the only way to his wrists folded each forearm back over
  the upper arm, and the arms read as stumps. The straps are on the frame's low half now, at his hanging wrists, and he hangs
  straight-armed in them; straining, the wrists stay and the body comes forward off the plaster. The frame's art moved with them
  (`tools/draw_lodging_house.py`: `frame_back_2/3`, `recess_daniel_low/high`).
- Daniel's shout went out with nobody near enough to hear it (the cast checks); he hears Dan whoever is about, but strains and shouts
  only with a reader within eighteen blocks of him.

## Every line that is new here

Daniel's, Dan's and Vera's lines above are the script's. These are mine:

- "At the end of the back passage the papered door shifts in its frame, stops, and shifts again."
- "The glass in the hall stand breaks. Dan's hand comes away from it cut across the back."
- "The back of his hand opens across the knuckles, where nothing has touched it."
- "A hand closes on your arm and does not let go. It walks you back the way it came."
- "Its hand comes off you."
- "In the dark behind the paper it lets go of you, and goes on without you."
- "It stops where it is put, and comes on again."

## Decided here (any of it can move)

- Twenty seconds after the three are settled, Vera steps out into the corridor, and Dan and Rae come down; nothing is said.
- The glass is the hall stand's, not the parlor's door light. Dan waits in front of the stand, so it is the one he goes back against.
- It goes for readers only on the ground floor's hall and back passage; a reader in another room is safe from it this time.
- Five blows keep it off; two from somebody else, or three from the one held, break a grip. A blow never hurts it.
- The pin comes thirty seconds after it has gone back with nothing cut, and Vera leaves Daniel's door open after it.
- After the pin, Vera and Dan stay at the frame; the next slice moves them.
- A reader taken is let go in the passage, unhurt; slice 12 has more for them.

## What was built

- `LodgingInjury` (new): the incident's phases (`PULL`, `BATTER`, `LOOSE`, `BACK`, `OVER`), its walk, grip and drag, Dan's going to it,
  the glass, the blows, the pin; records `HeardJib0606`, `StruckFailed0606`, `SawDanCut0606`, `SawDanielCut0606`, `KeptOff0606`,
  `Taken0606`, `Held0606`, `SawPin0606`.
- `LodgingStory`: stages `DOWN`, `INJURY`, `AFTER`, `PIN`, `PINNED`; Vera's place between rooms, Dan's in the hall, and the two at the
  frame; their walks (a door on the way opened as it is reached, Daniel's bolt drawn first), clear of the stair's newel post.
- `LodgingPeople`: gestures `BATTER`, `REACH`, `DRAG`, `HURT`, `PIN`; `dressed` for the cut and the prick; a blow on one of them goes to
  `struck`.
- `LiteraryActor`: a blow on the lodging house's people goes to `LodgingPeople.struck`.
- `LiteraryRenderers`: the five poses; the cut and pricked sheets.
- `LodgingBlock`: the hall stand draws three stages.

## Checks

- `LodgingHouseTests.theFailedBodyComesThroughTheGlassBreaksAndBothMenAreCut` (namespace `lodging`): from settled in the workroom,
  Vera goes out and Dan and Rae come down, the workroom door shut behind them; it pulls at the jib door (the reader in the hall is
  told), batters it while it holds, and the door goes; it reaches Dan, the glass breaks, both men are cut and drawn so; the reader in
  the hall saw Dan's and not Daniel's, the reader with Daniel saw his and not Dan's and has his line; it goes back and pulls the door
  to, Dan back in front of the bench; with the cut shown, no pin.
- `LodgingHouseTests.aFailedBodyTakesAReaderIsDrivenOffAndThePinShowsTheRule`: it takes hold of the reader nearest the door and walks
  them back; one blow from the other reader does not break the grip and the second does; three more drive it off, nothing cut; the
  door is not pulled to on the reader standing in it; a minute on Vera walks down, draws Daniel's bolt, Dan comes after her, the pin
  marks both hands and Daniel is drawn pricked; the reader in the room saw it and has her line, the one in the hall did not.

## Run here

- The lodging namespace whole (8).
- The release checks (in the commit).

## Not covered

- **The poses were seen in review frames of the model alone and of Daniel in his frame, not in play** with the house around them.
- **A reader taken into the passages** is let go there and nothing more yet.
- **Its sounds** are the game's own (a door knocking, battering, breaking; glass).
- **Three or more readers** at it at once.

## For the owner

- The seven grey lines above.
- The pin comes thirty seconds after nothing was cut; Vera leaves Daniel's door open.
