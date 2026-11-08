# The House's read: age (0.4.372)

The second of the read's leftovers (`docs/HOUSE_READ_0_4_159.md`, "Not built yet"): the larger `AGE` changes. The targets are in `docs/PERSONALITY_TAILORING_TARGETS_0_4_140.md` and `docs/HOUSE_READ_DESIGN.md`:
- the Wheel starts at the reader's stage of life;
- Harrigan's spare is a handset for an older reader;
- a landline rings in later halls for an older reader.

The shelter strangers' age already followed `AGE` (0.4.159).

- **Witness:** unchanged, 81 / 61 / two kinds / three endings.
- **Layout:** 122. **Protocol:** 163, unchanged; the phone screen's state gains one key.
- **New assets:**
  - one original sound, a bell telephone's ring (`tools/generate_home_audio.py`);
  - one item texture, the handset (`tools/draw_age_art.py`).

## What reads what

| Read | The Wheel | Harrigan's spare | The halls |
|---|---|---|---|
| Settled `DIGITAL` | Opens on her teenage room, with her phone charging on the floor by the bed | The phone, as before | Nothing |
| Settled `TRANSITIONAL` | Opens on her own daughter's nursery, with a small television on the floor in the corner | The phone, as before | Nothing |
| Settled `ANALOG` | Opens on the hospital room, with yesterday's paper folded to the crossword by the visitor's chair | A handset with buttons and a plainer screen | A telephone rings |
| Tentative `ANALOG` | The nursery, as before | The phone, as before | A telephone rings |
| No read, an uncertain read, or the trait switched off | The nursery, as before | The phone, as before | Nothing |

## The Wheel

`WheelHouse.startRoom` decides which room the front door opens on. Each room's one era object is in `WheelHouse.ERAS`.

- **The front door opens on the reader's own stage of her life.** They come in at their own height. The front door goes there every time, until they have found Mother.
- **Nothing about the wheel is skipped.**
  - The hospital room's door still goes round to the nursery.
  - Mother's room still opens only for a reader who has seen all six rooms and gone back through a door.
  - The first two rooms still make a child of anyone going forward into them.
  - Starting late is a way into the whole life, never a way past it.
- **The era object belongs to that reader alone.**
  - It is sent only to their screen, over a cell that is really empty, and resent while they stay in the room.
  - The room's own furnishings stay hers, so a friend in the room sees her room as it is.
  - Right-clicking the object reads one line:
    - *"Her phone, on the floor by the bed, charging. It lights: Mum, three missed calls. It goes dark again."*
    - *"A small television on the floor in the corner, with the sound turned right down. Whatever was on when the baby finally slept is still on."*
    - *"Yesterday's paper by the visitor's chair, folded to the crossword. Most of the squares are filled in, in two different hands."*

## Harrigan's spare

A reader settled as older gets an old phone with buttons when they scratch the ticket. The choice is made at that moment and kept (`HarriganVignette.handsetFor`, `Handset0372`):
- **The item:** `custom_model_data` 1 draws a candybar handset with a stub aerial, a small screen and twelve keys.
- **The screen:** every colour becomes one of four shades of a liquid-crystal panel.
- **What it does:** the clock, the contact, calls, texts and voicemail work exactly as before.
- **What it says:** *"You take the spare phone, an old one with buttons."*
- **Harrigan's own phone** and the burial or keepsake choice are untouched.

## A telephone down the hall

`HomeRead.hallCue` decides when the telephone rings. The ordinary halls' one-minute cue comes from the landmark nearest each listener.
- **Who hears it:** a reader the House reads as older (tentative is enough), at a depth of eight or more.
- **When:** one cue in four is a telephone instead, starting with the second.
- **What they hear:** it rings four times, six seconds apart, then stops. There is nothing to answer.
- **Who doesn't:** anyone else in the hall hears the pipes. The quiet room and the explorers' camp never ring.

## Tests

One new case in the tailoring namespace, `aSettledAgeOpensTheWheelAtTheReadersStageGivesTheHandsetAndRingsDownLaterHalls`. It checks:
- which reads move the Wheel, make the handset and hear the telephone;
- that each era object stands in its own room, off the line between the doors, and that its line names no colour and no age;
- that the screen is told it is the handset for that reader only;
- for each settled age:
  - the front door opens on the right room at the reader's own height;
  - the object is not in the world but is sent to that reader, and not to a friend;
  - the reader can look at it;
- that the hospital room's door still goes round to the nursery;
- that with no read the front door opens on the nursery, as a child;
- that nothing rings in the early halls;
- that deep in, the second cue rings four times to that reader and stops, and the friend hears nothing.

The house-read job now counts 44 cases.
