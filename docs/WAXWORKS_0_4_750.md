# The waxworks 2: the flashlight and the figures (0.4.750)

The second part of the waxworks (`docs/WAXWORKS_AND_COLD_ROOM_0_4_749.md`, section 1; the place is `docs/WAXWORKS_0_4_749.md`). The owner asked for figures "slowly coming to life", a head at the edge of the reader's sight that seems to move "almost like it was trying to get a better look", a blink that comes only after the reader has held a figure's face for several seconds, a flashlight in an otherwise dark place, and a figure that copies the reader. This part builds those four. The shift, the monitors and the rounds come in part 3, and the House still does not deal the museum until then.

## The flashlight

- The NIGHTS locker opens on a coat and a flashlight on a hook. Each reader is lent one; a second hand on the locker finds the hook empty.
- It is lent, not given (`WaxNight.LENT` on the stack, the reader's own id). Dropped, it stays in hand. It is not dropped at a death. Out of the museum it goes back on its hook, within half a second.
- In either hand it lights where it points: a light block at level 12 in the cell in front of whatever the beam meets, up to eighteen blocks off. Put away, the light goes out. Two readers' beams on one cell share it.
- A spectator is lent nothing.

## The look

A figure turns its head toward a reader who has it at the edge of their sight (between 35 and 75 degrees off where they look, within sixteen blocks, nothing in the way) while nobody at all is looking at it (within 22 degrees). It turns two and a half degrees each tick, never more than 55 degrees from where the modeller set it, and keeps the turn. When the figures are put back on their marks, their heads stay where they got to.

This is shared. A reader who turns to look sees the head where it stopped, and so does everyone else in the museum. The figure under its sheet and the statesman who copies (below) do not take part.

## The blink

A reader who holds a figure's face in the beam, within eight blocks and nine degrees, for four seconds sees it blink once (`WaxBlinkPayload`, five ticks of eyelids). Nobody else sees it and nothing is heard. Holding on, the next one comes six seconds later. Without the light on the face nothing blinks. The record keeps which figures blinked (`Blinked`, a bit for each) and how many times (`Blinks`).

## The mirror

The third statesman in the Hall, the one across from its door, copies a reader who stands in front of him (within seven blocks, inside sixty degrees of the way he faces) and faces him (within 35 degrees). His head turns the way a reflection's would and tips as theirs does; when they crouch, he crouches; he does it six ticks after they do. Each reader's own client draws this from their own body (`WaxClient.mirror`), so two readers in front of him each see him copying themselves. Two seconds in front of him sets `Mirrored` on that reader's record.

## More than one reader

- The flashlight is one each, and the light from each reader's own beam.
- The look is shared: one head position per figure for the museum.
- The blink is the reader's own: sent only to them, kept only on their record.
- The mirror is each reader's own: drawn on their client, kept on their record.
- Spectators borrow no light, turn no head and see no blink.

Protocol 205 (the blink payload). No new block, texture or sound: the flashlight is the Last Seen flashlight's item, named "Night guard's flashlight".

## Tests

`WaxworksTests#theFlashlightTheLookTheBlinkAndTheMirror` (batch `waxworks_night`): one flashlight from the locker and no second; no light in the pocket, light where it points in hand, out when put away; a watched statesman keeps his head, one at the edge of sight turns toward the reader and keeps the turn through `ensure`; no blink without the light, a blink sent to the reader with the beam on the face for four seconds and to nobody else; the mirror counts for the reader in front of him and not behind; out of the museum the flashlight goes back and its light is out. The first case now expects the locker's new line. The sweep, cast and two-reader checks pass unchanged.

## New text

For the owner's review: the locker opening ("The NIGHTS locker opens on a coat nobody has worn in years and a flashlight on a hook. The flashlight works."), the empty hook, and the line when the flashlight is dropped ("The flashlight belongs to the museum. It goes back on its hook at the end of the night.").
