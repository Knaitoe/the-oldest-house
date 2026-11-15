# The Ones Already Inside 5: Daniel through the wall (0.4.605)

The plan's fifth slice (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): "N3: Daniel through the wall, the bolt, the hatch, the restraints."
In 0.4.602 Daniel answered a hand from anywhere a hand could reach him, his shut door included. Now he is reached the ways the script
gives: "A reader at the bolted door, the food hatch, or inside once the bolt is drawn." The House still does not deal the house.

New builds only. No layout, protocol or Witness change.

## Where a reader can speak to him (`LodgingDaniel`)

- **In his room**, a hand on him, as before.
- **Through the food hatch**, from the west passage. The hatch is in his room's west wall. Shut, a hand opens it, and the reader looks
  through: "Through the hatch: a bare room, a cot, a chair turned to the back wall, and a man held standing in a doorcase there." Open,
  a hand speaks to him through it. A crouched hand shuts it. From inside his room a hand opens or shuts it as before.
- **At his door**, from the back passage, crouched and close to it, a hand speaks through the door instead of drawing the bolt. The
  first time: "Close to the door, low. Somebody on the other side stops moving." The bolt stays as it is.
- **Nowhere else.** A hand on him through his shut door, standing, does nothing.

Whatever way a reader reaches him, the exchange is the same (0.4.602): the script's three lines and five replies, and what the reader
answers is in their own record.

## When Dan's voice carries

N3 opens: "From his room, when Dan's voice carries (the hall, the back passage, the parlor), Daniel hammers on his door and shouts."
He is strapped standing in his frame, so he cannot reach his door. When Dan says something aloud in the hall, the back passage or the
parlor, Daniel strains against the straps, drives his heel back into the frame four times, and shouts the script's line, "Is that him?
Is that him out there?" A reader within eighteen blocks has heard it in their record. He does it once, then not again for a minute.

His straining pose: the wrists stay where the straps hold them; the shoulders pull; the body jerks forward off the plaster with each
blow; the head goes up; one heel kicks back.

## The restraints

A hand on the straps of his frame, from inside his room: "Each strap goes through an iron loop and is buckled, and each buckle is
locked. There is a keyhole in each." They open with the release key, which is slice 8 (N7: the key and Rae's four turns).

## Every line that is new here

The shout is the script's. These are mine:

- "Through the hatch: a bare room, a cot, a chair turned to the back wall, and a man held standing in a doorcase there."
- "Close to the door, low. Somebody on the other side stops moving."
- "Each strap goes through an iron loop and is buckled, and each buckle is locked. There is a keyhole in each."

## Decided here (any of it can move)

- He hammers on the frame, with his heel, not on the door: he is strapped standing and the door is across the room.
- Speaking through the door is a crouched hand, close to it; standing, a hand draws the bolt as before.
- Once a minute at most, so a talkative Dan does not keep him at it.
- The hammering is heard as blows on wood (the game's door-battering sound, low and quiet), from the frame.

## What was built

- `LodgingDaniel` (new): `reach` (in his room, at the open hatch from the passage, crouched at the door), `hatch`, `door`, `straps`,
  `danSpoke` and `tick` (the four blows), records `HeardHammer0605`, `ThroughHatch0605`, `ThroughDoor0605`, `FeltStraps0605`.
- `LodgingPeople`: gesture `STRAIN` (9); a hand on Daniel needs `reach`; `speakTo` for the hatch and the door; Dan's lines said aloud
  call `danSpoke`.
- `LodgingHouse.click`: the food hatch, the straps and the crouched door go to `LodgingDaniel` first.
- `LodgingTexts`: the shout and the three lines above.
- `LiteraryRenderers`: the `STRAIN` pose.

## Checks

`LodgingHouseTests.danielIsSpokenToThroughTheHatchAndTheDoorAndHammersWhenDanSpeaks` (namespace `lodging`, now six), with two readers:
a standing hand on him through his shut door says nothing; a crouched hand on the door speaks through it and leaves the bolt shot; the
hatch opens to a hand, speaks to the next, shuts to a crouched one; Dan speaking in the hall brings Daniel against the frame, and the
second reader in the back passage has heard it; he hangs again after three seconds; Dan again inside the minute does nothing; a standing
hand draws the bolt; in his room a hand speaks to him; the straps are locked and he is still held.

## Run here

- The lodging namespace whole (6).
- The release checks (in the commit).

## Not covered

- **The straining pose has not been seen in a frame.** The cast's poses are drawn only in a running world, and none was run.
- **Through the hatch, a reader sees him at an angle.** Whether the hatch's opening shows him from the west passage has not been looked
  at.
- **The blows' sound** is the game's own, not one made for this.
