# The long night (0.4.330)

Two of the owner's notes from 7 November:

- On the night ward: "Any way to make this more engaging? To creep up the horror while also better conveying the intention of the vignette?"
- On the Sphinx: opening one eye left the other stuck, and both should follow the reader; each finger should have one nail.

## The night ward

The ward is about calling for help that doesn't come. The chart says "Someone should come", and the dawn page ends "I had mistaken an answer for someone coming." Until now, each press of the call button got only a line ("No footsteps follow.") and the night was the same from start to finish. Now the button is answered, but not by anyone, and the ward closes in as the night goes on. All of it is the reader's own: the sounds are sent only to them and the changed blocks are shown only to them, so another reader in the ward has their own night.

**Footsteps answer every press.** When the monitor calls and the reader presses the button, soft-soled footsteps start to walk. They come a little further in each quarter of the night:

1. **First quarter.** Up the corridor outside, to the door. "The footsteps stop outside the door. Nobody comes in."
2. **Second quarter.** In through the door, to the nurses' station. "The footsteps stop by the station. Nobody is there."
3. **Third quarter.** Up the aisle and into a bay, behind its drawn curtain, which stirs. "The footsteps stop behind a curtain. It moves a little."
4. **Last quarter.** From the far end of the ward to a step behind the reader. The incubator's light goes out for a moment. "The footsteps stop behind you."

Late in the night, a call left ringing for eight seconds is answered anyway. Footsteps run the length of the ward and stop behind the reader, and the light goes out for longer. This happens once a night.

**The ward closes in.**

- At the end of the first quarter the station light flickers and goes out. The incubator's is the only light left.
- After that, the bays' open curtains are drawn one at a time, a few minutes apart, each with the sound of curtain rings running along the track. A curtain is never drawn while the reader is looking at its bay.
- From halfway through the night, the monitor on the empty incubator starts to beep like a heart monitor. It gets faster and louder towards dawn.

**Dawn** shows the ward as it really is and forgets the night. Leaving does the same, and the night starts again on the next visit, as before. The resolution, the chart, both pages, the timing and the Witness record are all unchanged.

Three new subtitled sounds come from `tools/generate_night_ward_audio.py`: "The monitor beeps", "Footsteps" and "A curtain is drawn". The light's flicker uses the ward's existing flicker sound.

## The Sphinx's eyes

When the reader goes for a second door's latch after being warned, the Sphinx's eye over that door opens. Before, the room kept one open eye at a time. A second warning opened a second eye but only the second one was tracked, so the first stayed open, staring at nothing, for good. Now each door keeps its own eye: two or three can be open at once, each follows the reader, each drops grit from the top of the wall over its own door, and each closes when its own time is up.

## The Sphinx's nails

The resting hand's claw on each toe was two pieces, each with its own drawn nail, so every toe showed two nails stacked. They are now mapped as one nail: the band where the nail meets the toe is at the top, and the lower piece continues the same nail to its tip.

## Validation

- A new ward case, `theNightWardAnswersEachCallCloserAndClosesIn`, checks the following:
  - the first press is answered at the door from outside, and nobody comes in;
  - halfway through, the first three bays are drawn while the reader looks away, and the station light is out for that reader only, with the real ward unchanged;
  - a press in the third quarter walks to the drawn bay nearest the reader and stops behind its curtain;
  - a call left ringing in the last quarter brings the footsteps running to just behind the reader, and the incubator light goes out;
  - dawn forgets all of it, and none of it is a resolution.
- The ward departure case now also checks that leaving forgets an answer that is still on its way.
- The Sphinx's first-crossing case now presses a second warned latch and checks that both eyes are open, that each follows the reader, and that both close.
- `tools/draw_sphinx_fittings.py --check` and `tools/generate_night_ward_audio.py --check` pass.
