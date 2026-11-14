# The night ward: curtains, the chart's station, and the beds (0.4.590)

The owner, playing on 14 November:

> "hospital curtains need improvement. the room's vignette text should be highlighted and given a unique display
> canister, too. would also be cool if the vignette had a way to engage players more somehow"

New builds only. New lines, all listed under "For the owner". No protocol, layout or Witness change: the night is
still three minutes, dawn still ends it, and the chart's final page still resolves it.

## What was there

- **The curtains were flat sheets.** Each was one pane of striped cloth drawn at thirty-two pixels to the block, with a
  band along the top and another along the foot.
- **A rod stood over every block of track,** so a bay had a row of them up to the ceiling.
- **An open bay was a gap.** Where a curtain was pulled back there was only the bare track: the cloth had gone nowhere.
- **The chart lay on a vanilla lectern,** oak, in the middle of a clinical room. Nothing marked it. The opening text
  of every literary scene is marked for a reader who has not read it (0.4.273); the six novel sites, the ward among
  them, were never given that.
- **The night asked one thing of a reader:** press the button when the monitor calls. The footsteps, the light, the
  curtains and the heartbeat all happen to a reader who stands still.

## What it is now

### The curtains

- **Cubicle curtains, drawn at sixteen pixels to the block** by `tools/draw_night_ward_assets.py`:
  - a header tape with eyelets, hung from the track;
  - an open mesh band under it, which can be seen through;
  - cloth below, in folds: the cloth is three strips in two planes a pixel apart, so the folds are real and catch the
    light differently;
  - a stitched hem that stops short of the floor, the back folds a pixel shorter than the front.
- **A curtain that is pulled back hangs bunched.** Each bay's front is now two curtains parted: the cloth of each is
  gathered in tight pleats at its own side of the opening, and the track runs on across the gap.
- **Bunched cloth is no bigger to the hand than to the eye,** and stops nobody.
- **Rods only where the track turns or ends.** A straight run of track has none.
- **The three old cloth pictures are gone.** The ward's other surfaces are as they were (see "Not covered").

### The chart

- **It stands in a carrier station** where the lectern stood: a steel cabinet, a glass chamber with the carrier in it,
  and the pipe it came down, up to the ceiling.
- **The chamber is lit,** so it is found in the dark ward.
- **It is marked for a reader who has not opened it,** with the same warm outline and motes as every other scene's
  first paper. The mark goes out for that reader when they open it, and for nobody else.
- **At dawn the last page comes down the pipe.** A carrier is heard arriving at the station, and the chart is marked
  again until that reader has read it to its last page.
- **A hand anywhere on the station opens it:** the cabinet, the chamber or the pipe.

### The night

Two things for a reader to do, both their own (another reader in the ward sees and hears none of it, as with the rest
of the night).

- **The beds call.**
  - Four times in the night a bed calls: the light over its bay comes on and a chime sounds from the bay. The first is
    fifty-five seconds in, then one every thirty seconds, each from a different bay.
  - A bed left calling chimes again every twelve seconds.
  - A hand on the calling bed answers it. Its light goes out, and a line says what is there. Nobody is in any of them;
    the line is a little different each time one is answered.
  - A hand on a bed that has not called says only that it is made.
  - Nothing is lost by leaving a bed to call. The night ends at dawn either way.
- **The curtains can be pulled back.**
  - The night still draws the open curtains shut, one bay at a time, when the reader is not looking.
  - A hand on a curtain the night has drawn runs it back along its track.
  - The night leaves that curtain alone for twenty seconds, and then draws it again when the reader is not looking and
    not standing at it.
- **The dawn chart says what was done.** When a bed has called, the last reading has a page for the beds between its
  two pages: that the reader went to each, to some, or to none.

## Checks

- **A new case** (`NovelTests.theBedsCallTheCurtainsPullBackAndTheChartIsMarked`), with two readers in the ward:
  - the station is a cabinet, a lit chamber and the pipe;
  - an open bay has bunched cloth either side, smaller than a block to the hand, and stops nobody;
  - the unopened chart is marked; opening it puts the mark out for that reader only;
  - the first bed calls, and its light is lit in that reader's view only: the ward's own light is off, and the other
    reader's night is untouched;
  - a bed that has not called says it is made; the calling bed is answered and its light goes out;
  - a second bed calls from another bay;
  - a drawn curtain is pulled back, is left alone, is not drawn while the reader stands at it, and is drawn again
    once the reader has gone from it;
  - at dawn the chart is marked again, has its page for the beds, resolves the night on its last page, and the mark
    goes out; the other reader has resolved nothing.
- **The ward's two existing cases** pass, with a bunched curtain and the two call lights added to what they expect.
- **The chart-mark case of the literary scenes** and **the six architecture cases** pass. The architecture case
  expects the station where it expected a lectern.
- **The art tool's check, the sound tool's check, the quick checks, a compile.**
- **The game's own renderer:** the ward's proof pages (six now: the bunched curtain, the call light and the station
  are on them), and eye-level frames of the ward from twelve places before and after.

Not checked:

- **Nobody has played the night** since the change. The frames are of the room at rest.
- **A lit call light has not been seen in a client frame,** nor the mark on the chamber. Both are sent to one reader's
  client and the frames have no reader in them. The case checks what is sent.
- **The chime and the carrier's arrival have not been heard in a client.** They are generated by
  `tools/generate_night_ward_audio.py` and checked by it.

## Caught while checking

- **A curtain was drawn on a reader standing at it.** It was not: the case stood its reader a block from the curtain
  it had just pulled back and waited for the night to draw it. The night never draws a curtain a reader is within a
  block and a half of, looking or not. The case now checks exactly that, then steps away.
- **Three of the bed lines described what the bed does not show.** As first written they had a blanket turned back, a
  dented pillow and a call cord. The bed's model is a made bed, and a line that says otherwise is wrong the moment a
  reader looks. The lines now say only what a hand would find.
- **A bunched curtain with nothing on its gathered side drew nothing below the track.** The ward's proof walks every
  state the curtain block can be in and requires each to draw something. Cloth gathered toward a side with no curtain
  beyond it (a curtain pulled back to the very end of its track) had no rule, so its lower half was empty. The ward as
  built has no such curtain, but the model was wrong. The cloth now hangs at the end of the run whichever way the run
  joins, and the hand's shape follows the same rule.
- **The client proof was stopped by my own clean-up.** It reads a file the server writes first; clearing the proof's
  folder removed it. Nothing in the game.
- **On CI (run 1627): the build's package check asked for the cloth pictures.** The check that the playable
  package holds the ward's pictures was a list kept by hand, and it still named the three cloth pictures this
  release removed. The build job stopped there, two minutes in, so its half of the game's cases and the client
  proofs did not run on CI for 0.4.587 to 0.4.591; every other job passed. Nothing in the game was wrong. The
  check now asks for every picture the ward has in the source, whatever they are. I ran the whole package check
  here against a built package before pushing again, which I had not done for this release.

## Not covered

- **The ward's other surfaces are still drawn at thirty-two pixels:** walls, floor, ceiling, beds, cabinets, the
  monitor. Only what this note asked for was redrawn. They are on the texture pass's list.
- **The other five novel sites' first papers are still unmarked.**

## For the owner

Every new line, all mine. Any of them can be reworded or cut.

- When a bed calls: "A bed is calling. The light over its bay has come on." and, after the first, "Another bed is
  calling."
- A hand on a bed that has not called: "The bed is made. Nobody is in it."
- A hand on a calling bed, by how many have been answered:
  1. "The call light goes out. The bed is made. Nobody is in it."
  2. "The call light goes out. The bed is made. The sheet is warm."
  3. "The call light goes out. The sheet is warm, and the pillow."
  4. "The call light goes out. The sheet is warm. The blanket is tucked in on both sides."
- A hand on a drawn curtain: "The curtain runs back along its track."
- The dawn chart's page for the beds, one of:
  - "The beds called too. / I went to each one. / Nobody was in any of them."
  - "The beds called too. / I went to some of them. / Nobody was in the ones I went to."
  - "The beds called too. / I did not go to them. / Their lights were on until dawn."
- Two subtitles: "A bed calls" and "A carrier arrives".

Choices that are mine:

- **"Display canister" I took as a pneumatic carrier station,** the kind a hospital sends charts through. If you meant
  something else (a glass case, a clipboard holder at the bed's foot), say so and it is a different model in the same
  place.
- **Four calls a night, twenty seconds before a pulled curtain is drawn again.** Each is one number.
- **Answering a bed changes nothing but the page.** It is something to do and something the chart remembers, and no
  ending turns on it. If a bed left calling should cost something, or answering all four should earn something, that
  is a rule I have not made.
- **The beds are empty.** The lines never put anyone in them.
