# Fallow 3b: what the light leaves (0.4.680)

This is the second half of the third part of *Fallow* (`docs/FALLOW_PLAN.md`). Part 3a (0.4.674) brought the afternoon to the light.
This half is what the light leaves of the station: each reader's shadow on its front, and the station falling in over the people below.
Part 4 is the cellar after (buried); part 5 is the road, where readers who were outside come back and find their shadow, and the Witness
source.

Fallow is still not a Witness source.

## What happens

Times are in ticks after the light (`FallowRuin`), on the afternoon's own clock, which only moves while somebody is at the station.

1. **The plate glass** (at the light). Every pane of the store's front and its west side is blown in, out of its frame. Each keeps a few
   teeth of glass along the edge it hangs from (the lower pane's at its foot, the upper's at its head) and is open in the middle. Glass
   breaks, and a spray of it goes into the store.
2. **The shadows** (at the light). A reader out in front of the station, between it and the road, has their shadow thrown straight back
   onto the store's front by the light and left there.
   - It lies on the solid run of wall nearest where they stood: either side of the front door, the corners, the jambs of the garage
     bay. The glass is gone, and nothing is thrown onto an empty frame. Two readers never share a run; the second takes the next
     nearest.
   - It is the body they wore when the light came (the House's body, hair and coat and all, or their own skin where the House's bodies
     are off), in the stance it caught them in: standing, mid-stride by how fast they were going, or crouched.
   - It stays when the station comes down: the front is the one wall that stands.
   - A reader beside the station, behind it, in the yard or in the store throws their shadow onto nothing. A reader below throws none.
     Spectators throw none.
   - Each reader's record keeps the run of wall theirs is on (`ShadowX`), for part 5.
3. **The station comes down.** While anyone is below to hear it, a part at a time:
   - **The canopy** (40). Its deck comes off its four columns onto the concrete in buckled sheets; a sheet catches on the pump island
     and leans there. Each column breaks off at its foot and lies beside it, the west pair to the west and the east pair to the east.
   - **The store's roof** (60). It goes into the store: the ceiling and the roof over it, a lip of ceiling left hanging here and there
     at the walls, the strip lights down with it. The floor is deep in it, all but the way in from the House's door. The garage's
     roof goes the same way, over the car on its lift.
   - **The walls** (90). The west and back walls break off raggedly a course or two above the plinth; the west side lies out in the
     yard where it fell, its boards still lapped. The House's door in the back wall and the wall either side of it stand. The front
     stands, toward the light, with its sign and the shadows on it.
   - **The cellar doors** (120). The fall of the store comes across the yard and onto the doors, and lies heaped round them.

   With nobody below, all of it comes down at once forty ticks after the light, after the readers out in it have gone.

   Below, each part is heard muffled from overhead, with a line for the canopy, the store and the doors; the ground shakes on through
   it, and the dust comes down as before. Spectators about the station hear it as it is.
4. **Nothing is put where a body stands.** Rubble is laid only on empty ground and never on anybody, reader or one of the people. A
   head come up the cellar steps into the open doors keeps that cell clear.
5. **A fresh afternoon.** When the station has stood empty five seconds, the afternoon is over as before, and the station is built
   again from its own plans before anything else is set back: glass, canopy, roof, walls, the cellar doors clear and shut, and nobody's
   shadow on the front. No chance enters the plans, so it is the station it was. If any of its ground was not loaded when the afternoon
   was made fresh, it is built again as soon as it is all loaded and nobody stands on any of it, before the light.

## On the reader's screen (`FallowShadowRenderer`)

The shadow's block holds nothing a body meets and draws nothing itself. Its record (whose shadow, the body they wore, the way they faced,
how far into a step and whether crouched) goes to every client, which draws the body pressed flat against the wall's face, a hair in front
of it, the way the light threw it.

- It is one flat soot, the body's outline and none of its colours: the client reads the skin once and keeps only which of its pixels are
  there, so hair and a coat's hem keep their edges and a face, a shirt or a pattern shows nothing.
- Round it is a scorch two pixels wide, the inner pixel darker and the outer nearer the siding's own colour, even all the way round
  whatever the stance.
- It is lit as the wall round it is, so it darkens with the wall at night and never glows.
- A stride is caught as a photograph catches one, the legs about sixty degrees apart; a crouch is drawn crouched.
- The body is turned to whichever of face-on, back-on or side-on to the wall it was nearest. Thrown at a slant, a body this square runs its
  head, shoulders and arms together into one block and stops reading as a person.
- From a reader's own skin when the House's bodies are off, slim or wide arms as theirs are. A skin the client has not yet read is drawn
  darkened for that moment instead.

## Single and multiplayer

- One reader alone sees all of it from whichever side of the doors they were on when the light came: from below, the station coming down
  a part at a time; from out front, the glass and their own shadow before the faint.
- With more, each reader out front has their own shadow on their own run of the front, and each reader below hears the fall. What a
  reader did and where they stood stays theirs.
- The station's fall is one fall, shared by everyone there.

## Found and fixed

- The station's name board read backwards from the forecourt (`'S AN RL HA`): part 1 laid its four boards from west to east, but from the
  road east is on the left. They are laid the other way, and the name is lettered across all four as one sign, evenly spaced, rather than
  two letters centred on each board. The part 1 case's check of the board follows.

## Made as

- `FallowRuin` (new): the parts and when they come; the shadows; `mend` and `leftover`, the station built again.
- `FallowShadowBlock` (new): the block and its record. `FallowShadowRenderer` (new): the shadow drawn from the body.
- `FallowStrike`: the ruin's clock after the light; each reader's shadow at the light; a fresh afternoon builds the station first.
- `FallowSite.mend`: the station from its plans (the store's build no longer takes a chance).
- `Fallow`: a fallen station left over is built again while nobody is on it.
- `FallowBlock`: the blown panes and eight kinds of rubble, with their shapes. `FallowTexts`: what a hand on them finds; the three cues.
- `tools/draw_fallow_art.py`: the name board lettered as one sign; the panes' teeth, the rubble's models and textures (sheets, joists, heaps, the leaning sheet, the columns,
  the wall's boards, the fall over the doors); the shadow's empty model. `tools/generate_fallow_audio.py`: three sounds.
- `FallowTests.theFourMinutes` now goes on past the light: the reader who held the doors goes out front and leaves their shadow beside
  where they stood, and nobody else's is on the front; the one below hears the three parts; the glass is blown, the canopy down with a
  column beside its foot, the store's roof in the store, the fall across the cellar doors; the front and the House's door stand, with the
  shadow on it; once both are out and the afternoon is over, the station is whole again and the shadow is gone.

## Every new line

Flagged for the owner. Nothing here names a colour or an age or says how the reader feels; the cues are on the action bar.

**Cues** (`FallowTexts`), heard from below:
- "Out toward the road, sheet metal comes down on concrete and goes on ringing."
- "Nearer, a roof gives, and everything under it goes with it."
- "Something heavy lands across the doors overhead. The light at their cracks is gone."

**A hand on the ruin:**
- A blown window: "The window's frame. The glass has gone out of it, all but a few teeth along the edges."
- A heap: "A heap of what the roof was: joists, tar paper, a strip light still in its housing."
- Over the cellar doors: "The store, down across the cellar doors. It does not shift."
- A joist: "A roof joist, fallen across the rest, its nails still in it."
- A canopy sheet: "A sheet of the canopy's deck, buckled flat on the concrete."
- A sheet on the island: "A sheet of the canopy, caught on the island and leaning there."
- A column: "A column of the canopy, broken off at its foot. The pencil marks run along it now, a line and a date beside each."
- The yard wall: "A run of the store's wall, its boards still lapped, lying where it fell."
- Anything else: "Siding, ceiling tile, a shelf's bracket, and dust over all of it."

**Subtitles:** "Sheet metal comes down with a crash", "A building falls in", "Rubble lands on the doors overhead".

**Block name:** "Shadow".
