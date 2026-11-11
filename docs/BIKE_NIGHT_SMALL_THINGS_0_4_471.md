# Bike night 9: the dome, the spring rider and the small things (0.4.471)

The ninth slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`), and the last of the playground's equipment. The proposal:

> **Spring rider.** A horse on a spring that rocks with the strafe keys. It is pure play.
>
> **Climbing dome.** Climbable on every bar, with a view over the park.
>
> **Also there:** a drinking fountain that works, a payphone that only takes coins, and a bench with a carved heart and two
> sets of initials.

Gus comes next, then the bullies.

**It still cannot be finished, so the House does not deal it.** Nothing is a Witness source.

## The climbing dome

It stands in the south-west corner of the playground's sand: a dome of steel bars bolted into triangles, three blocks across
and a child and a half high. Eight feet in the sand, two rings of eight joints over them, one joint at the top, fifty-six bars
in three paints, each bare along its middle where it is held.

- **Climbed on every bar.** A body at the dome's skin, inside it or out, is on a ladder there. Walking into it goes up it, as
  walking into a ladder does; jump climbs it too, and sneak holds on.
- **Stood on.** From its first ring up the skin stops a body. A reader who has climbed past that ring walks on up over the top
  and stands there. There is room on top for two.
- **A view over the park.** The first time a reader stands on its top they are told what is to be seen from there, and it goes
  in their record and their account.
- **Got into.** Under the first ring the bars are far enough apart for a child keeping low to get between them, and nothing
  stops a body there. A child standing does not fit under the ring; one sneaking does. Inside there is room to stand.
- A hand on it says what it is, and how it is climbed and got into.

I first built this piece as a square cage, three blocks each way, from my own note that called it a "frame". The proposal says
dome. The cage was never committed; this is the dome.

### How it is made

Bars at every angle are not blocks. The dome is its own block (`DomeBlock`, `calder_dome`): its ground is five blocks square
and two high, each block a cell that knows where in the dome it is and draws nothing. The dome itself is a cap of a ball, and
every rule about it is worked out from that ball:

- a body is climbing where its middle is within half a block of the ball's surface;
- the skin that stops a body is the ball's surface from the first ring up, in two-pixel squares, a third of a block thick;
- a hand finds the same surface from the sand up.

The middle cell on the sand keeps a block entity that is there only to be drawn. Its renderer (`DomeRenderer`) lays one model
of a bar from each joint to the next, turned and made as long as that bar, and a joint's plate at each joint. The models are
never blocks in the world.

Where the dome's ground would run out over the sandpit's edging, the edging stays. The bars do not reach that far.

## The spring rider

A horse on a coil spring, bolted to the sand in the playground's north-west corner.

- A hand on it sits on it. It takes one reader; another is told somebody is on it.
- **It rocks with the strafe keys** (right is the way the horse looks; the walk keys lean it as well). A lean held holds it a
  little over. Leaning with it, back and forth, takes it further each time, out to its stops. It really rocks: it is drawn as
  far over as it is, and its rider goes over with it.
- With no lean it comes back upright. Left alone it stands still.
- The spring is heard as the horse comes through upright ("Spring creaks").
- Sneak gets off, onto the sand beside it.
- A reader's record has that they sat on it and how often they rocked it out past where one lean takes it. Their account has a
  line for it. It does nothing else: "It is pure play."

It is made as the seesaw is: its own block (`SpringBlock`, `calder_spring`), a foot that keeps the rocking as its block entity
(stepped alike by the server and each client), a renderer that draws the horse rocked about the foot, and an unseen seat the
rocking carries.

## The small things

- **The drinking fountain works.** On the grass where the side path meets the sand. A hand on it presses its button: water
  runs for a few seconds, it is heard ("Fountain runs"), and the reader drinks. While it runs, another hand on it is told it is
  running. Its bowl is under a child's chin.
- **The payphone only takes coins.** On the back wall west of the shed's door, at the height a grown-up would want it. A hand
  on it lifts the handset: a dial tone ("Dial tone"), and the reader is told it wants a coin and they have none. The handset
  goes back on its hook after a few seconds ("Handset is hung up").
- **The carved bench.** The bench by the playground has a heart cut into its back across both slats, with a pair of letters'
  strokes either side of it. A hand on it reads what is cut there.

The fountain and the payphone are two more kinds of the park's fitting block, with a second stage each that a scheduled tick
puts back. The carving is one more picture.

## How it is drawn

`tools/draw_calder_play_art.py` draws the dome's three bars and its joint, and the spring rider's foot and horse, and writes
both blocks' states. `tools/draw_calder_art.py` draws the fountain (still and running), the payphone (on its hook and off it),
the carved bench, and two more pictures (the carving, and water).

Four more sounds from `tools/generate_bike_night_audio.py`.

## Tests

`BikeNightTests`, three more cases (twenty-six in all), each building the place itself in a batch of its own.

- **The fountain runs, the payphone wants a coin and one bench is carved.** Where each is. A hand on the fountain drinks, it
  runs and is heard by both readers; the second reader's hand while it runs is told so and has not drunk; it stops by itself;
  then the second drinks. The fountain's bowl is under a child's chin. A hand on the payphone lifts its handset, with a dial
  tone; again, the tone goes on; it is hung up by itself, and that is heard. A hand on the carved bench reads it. Each reader's
  account has what they did and not what the other did.
- **The dome is climbed on every bar, stood on at its top and got into keeping low.** Its cells, each knowing its place, and
  the nine timbers of edging its ground stops at; that only the middle cell keeps what draws it. Twenty-five joints, all on the
  one ball; fifty-six bars, each about a child's reach long; a bar on every joint. It stops a body from its first ring to its
  top and nowhere else. From the first ring to the top the skin is walked up, sixteen ways round: each step in is no lower and
  no more than three pixels higher. Inside there is room for a child to stand; a child standing does not get in under the first
  ring and one keeping low does. A body at its foot, part of the way up the outside, and inside against its bars is on a
  ladder; one in the middle of it or standing off from it is not. A hand on it says what it is. A reader standing on its top is
  told once what is to be seen, which is theirs alone; there is room for a second, who is told as well. The account has it.
- **The spring rider leans with its rider and goes further when it is rocked.** Its foot, upright and still. A hand sits a
  reader on its saddle and they are told once how it is rocked; the second reader is told it is taken. A lean held holds it a
  little over, never as far as rocking, and its rider goes over with it. Leaning with it takes it out to its stops; that is in
  their record, and the spring is heard by both. The walk keys lean it as well; with no lean it comes upright. Sneak gets off
  onto the sand beside it; left alone it stands still with nobody on it; then the second reader sits. The accounts say what
  each did.

The case for what a hand on the park's things says tries the dome, the carved bench, the fountain and the payphone, and holds
the new lines to the same rule as the rest: no colour named, the reader never gendered, nothing about how they feel.

The client proof has three more pages (fifteen): the dome from one side and from above; the spring rider rocked back, upright
and rocked forward; the fountain still and running, the payphone on its hook and off it, and the plain bench beside the carved
one. I looked at them.

I looked at each piece in the park in pictures made with the local aid that is not part of the build (which I taught to draw
the dome and the horse as their renderers do): the dome from across the sand, from close, from inside looking up, and from its
top; the horse from the side, the front and across the sand; the fountain from the path and from the sand; the payphone from
in front and along the wall; the bench from in front and from over it.

### What the local runs and the looks found

- **The dome's ground replaced the sandpit's edging at its corner.** Its outside row of cells runs out over the edging to the
  west and the south, and the builder put cells there in place of nine timbers. The park's own case caught it ("the edging
  turns the corner"). Cells are now put only where nothing is.
- **The fountain was too tall.** Its bowl stood at a child's eyes. A park's fountain is for children: the pedestal is four
  pixels lower, and the bowl is under their chin.
- **The carving could not be read.** It was eight pixels by two on the top slat: a smudge. It is cut across both slats of the
  back now, so the heart is five pixels by four and reads as a heart. The letters are still only strokes: there is no room at
  sixteen pixels to the block for letters. The line a hand is told says what they are.
- **A lean held counted as rocking.** The first swing of a held lean goes further than where it settles. The mark for rocking
  is now past anything one lean reaches.

## What I have not seen

- **Climbing the dome by hand.** Test players have no physics. The cases check each thing a climb is made of: where a body is
  on a ladder, where the skin stops it, that the skin can be walked up, and where a child fits. Whether going up it and over
  the top feels right, and whether getting in under the ring is found without being told, I do not know. The line a hand is
  told says how.
- **The horse rocking under a rider.** The rhythm (about fourteen ticks each way) was chosen, not played.
- **The fountain's water, the handset, and all four sounds.**
- **The park at night.** As for the rest of it.

## Numbers

No layout, protocol or Witness change. New builds only.
