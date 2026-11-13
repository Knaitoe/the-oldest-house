# The Pell house, the owner's playtest notes (0.4.566)

The owner played the Pell house on 13 November and sent notes as they went. This release answers the first of them. New builds only;
no layout, protocol or Witness change.

## The door handles sit in the corner

The inner doors' knob was painted onto the lower door's texture, in its bottom corner, and on the flat face it read as a smudge at
the floor. The knob is now solid: a brass rose and knob on each face of the closed lower door, at hand height, from the door's
own model (`tools/draw_pell_house.py`, `door_knob`); the paint is gone from the texture. The garage, screen and shed doors share
the model's texture keys and are redrawn with it.

## The floor lamp's shade

The shade was one box with the lamp's whole picture stretched round it. It is three bands now, narrowing to the top, each mapped to
its own rows of the shade's picture, with the bulb's glow under it when the lamp is on (`tools/draw_pell_things.py`, `floor_lamp`).

## June's bandage on both hands

The cast's body was the classic one, whose left arm is drawn from the right arm's pixels, mirrored: the tea towel round June's
right hand was round both. The Pell house's people now draw their left arms from their own pixels (`CastModel.ownLeftArm`); the
painter already paints both. Only the Pell house's people do, so no other painted person changes.

## No game music in the house

Minecraft's own music no longer plays in the Pell house: what was playing stops as a reader comes in, and none starts while they
are there (`PellClient`). The house's own sounds are unchanged.

## When to leave

A visit is a reader's once Gail has rung and June has hung up, or once the last night has ended. After that the day waits for
nothing, and the next visit comes only by going out through the House's door and coming back; a reader who stayed on, waiting for
another day, waited for nothing. Now, a quarter of a minute after a reader has had all of a visit, June sends whoever is near her
out ("Go on, love, out you go. Get some sun on you. I'll be here."; a week in, the washing); half a minute after that the House
says once, in grey: "Nothing more will happen today. Go back out the way you came; when you come again, it will be a week on."
(later still, after the second visit; after the last, that the house has nothing more for them); and once more, in passing,
two and a half minutes on. June never talks over herself to say it (`PellHouse.leaving`).

## The hairbrush

At bedtime the brush was the item's flat picture, as big as Tilly's head, held up above her like a paddle, and June's arm only
rocked up and down. The brush is a small solid one now (handle, neck, paddle and a cushion of bristles, from its own sheet:
`tools/draw_pell_people.py`, `hairbrush_model`), held with the paddle running on past June's fist and bent down onto the hair. Each
stroke lays it on Tilly's crown and draws it slowly down the back of her head to the nape, then lifts it off and brings it back over
the top. Tilly's head gives to each stroke and comes forward again. Both keep to the level's time, so they keep together, and the
brush's sound comes with each stroke (`PellPeople.BRUSH_STROKE`).

## The telephone's cord

Off its hook, the wall set showed a stub of cord hanging into the air, and the receiver at June's ear had none. The cord is now
drawn whole each frame, from the foot of the wall set to the foot of the receiver's mouthpiece, wherever June has it: at her ear,
or held out to someone. It hangs down out of both ends and sags between them, less as she stands further off, coiled a turn to each
pixel in the telephone's own cord texture (`client/PellPhoneCord`). The wall set keeps only the cord's root.

## Answered, without a change

- **Callum's plate** is laid and stays empty at dinner on purpose: June passes it over without a word, and the tapes say why later
  (`docs/PELL_HOUSE_DINNER_0_4_526.md`). It can be changed if it reads as a fault.

## Still to come

The other notes are the next releases: the shed kept June's, the scrubbing brush showing its progress, taking hold of Callum's
wrist, Tilly's hair growing, the sleepers' pose, faces, what the people and the readers do through the day, the interior and the
flower bed.

## Tests

The Pell house namespace; a new case, `PellHouseTests.aReaderWhoHasHadTheirVisitIsSentOut`, has a reader finish a visit and stay:
June sends them out, the House tells them once, and again later in passing, and nothing is said before the visit is theirs.
