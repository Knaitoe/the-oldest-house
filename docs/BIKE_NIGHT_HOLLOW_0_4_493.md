# Bike night 23: the Hollow (begun, 0.4.493; its plain way, 0.4.494; somebody who misses, 0.4.495)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`, "Act three"):

> **The Hollow** is the owner's platforming realm.
> - **The ground:** islands of graveyard turf with their headstones still in them; crooked fences; a bridge of coffin
>   lids; a pumpkin vine the size of a road, curling up and down; stairs of tilted stones.
> - **The riding:** riders jump and swerve from piece to piece. Missing a jump drops them onto the last island they stood
>   on (a checkpoint), with a scare.

This record grows with the Hollow. So far:

## 0.4.493: what it is made of

Nothing is built yet. This is the Hollow's materials, drawn by the graveyard's art tool
(`tools/draw_calder_graves_art.py`) and looked at on the proof's cemetery page:

- **Sod**: the block an island is made of. The cemetery's grass on top; at its sides, the grass's fringe, cut earth and
  roots hanging out of the bottom of it; underneath, dark earth and root ends.
- **Roots**, in three clumps, that hang under a piece of ground. They are in nobody's way.
- **The vine**: its own green, ribbed along its length, as a block and as a half block (for a vine that curls up and
  down by half blocks, which a bike rides without losing its way).
- **A coffin's wood**, and **a coffin's lid** as a half block: its boards, a pale cross let into it, a brass handle at
  each side.
- **A headstone laid flat** as a half block, for a step: its face up, a border cut round it, the lines of its name.
- **Old board fence** ("crooked fences"): upright, leaning to either side, and broken off short. A standing one is a
  fence where it stands, whatever it leans to, and as high to a bike as any fence; a broken one is a bump.

## What the gaps will be laid out from

Measured on November 12 by a probe case that was not kept (the table is in the proposal, under the Hollow): at top
speed on level ground a plain bike's tap of the hop carries it 2.3 blocks and a full hold 3.3; the Comet 3.4 and 4.8;
the Hopper 2.3 and 3.7, two blocks high. A bike is on the ground while any of its 0.6 of a block is.

So, as planned and not yet built: two-block gaps on the plain way (a tap clears them with room), a three-block gap
where a full hold is asked for, a gap of about five level blocks for the run "only the Comet's top speed carries", and
a ledge two blocks up for the "gap only the Hopper's jump clears".

## 0.4.494: the plain way

"Every bike has a way through." This is it: the way any bike takes, from the hill to the cemetery's far side. The five
ways the bikes' own perks open are not built.

**The hill's ground stops** on the row behind the small stones' own trees, ragged off the gate's line, and what is left
of the side railings ends in a pier on each side. From there north, for a hundred and sixty blocks, there is the
course and nothing else. **The cemetery's back railings and its back gate are on the far side of it now**, where its
ground begins again at the height of the hill's top; behind them is the lane.

In the order it is ridden (heights are where a body stands; the hill's top is 3):

| | Piece | Height | Reached over |
|---|---|---|---|
| 1 | A grave that has come away, seven blocks square, two stones still in it | 2 | two blocks of nothing, a block down |
| 2 | Another, two blocks east of the line | 2 | two, level |
| 3 | Another, two blocks west | 1 | two, a block down |
| 4 | "Crooked fences": a strip five wide and twenty-four long with board fence half across it at two places, one side and then the other, and broken boards between | 1 | two, level |
| 5 | "A bridge of coffin lids": five, each two blocks long, a block of nothing between them | 0 | **three**, a block down |
| 6 | A grave past the lids | 0 | one |
| 7 | "A pumpkin vine the size of a road, curling up and down": two blocks wide, up three by half blocks, over, and down one and a half, swinging two blocks east and back | 0 to 3 to 1.5 | two, level |
| 8 | "Stairs of tilted stones": laid headstones, three wide, half a block up every two | 1 to 5 | two, half a block down |
| 9 | The top: the highest piece | 5 | it touches the stairs |
| 10 | The last stretch: a strip three wide running down, with one gap in it | 4 to 3 | two, a block down; its own gap half a block down |
| | The far side | 3 | it touches the stretch |

**Every gap is two blocks but one, and most of them come down.** On the level a tap of the hop carries a plain bike
2.3 blocks and a held hop 3.3, and a bike is on the ground while any of its 0.6 of a block is. So on the level a tap
clears two blocks only if it is made in the last block before the brink, and a held hop clears three only from the
brink itself. The way was first laid out level, with the three wanting the hop held: the case that rides it passed
once, and the second time its rider came down a few tenths short of the lids and came off against the first of them.
That is too fine a thing to ask on the way every bike is meant to manage.

So the piece past the three is a block lower, and so are the pieces past four of the twos (a block, or half a block):
falling gives a hop time to carry. Three of the twos are level and are the harder kind. The height that loses is got
back by steps (the vine, the stairs), never by a jump. The lids' own gaps of one are ridden over at speed without a
hop.

### Cases

Two (79 in the namespace):

- on the gate's line and twelve blocks to either side, from the edge to the far side, nothing floats but the way's own
  pieces, what hangs under them and what stands on them; the hill stops at its edge with the first piece two blocks
  off and a block down; piece to piece is never more than three blocks, three only once and that one a block down,
  the lids a block apart, no more than three of the twos level; nothing is higher than
  the piece before it by more than a step, and each is in line with the last by two blocks at least over a gap (one
  where they touch: the vine swings a block at a time); from 0 to 5 high;
  each piece is made of what it is and stood on at its height; roots under the sod; stones still in the graves and
  fences across the strip; the far side's railings whole, its gate shut between two piers, gravel to it and the lane
  behind;
- **a plain bike (the Lamp) is ridden the whole of it** from behind the small stones to the far side's ground by a
  case that steers at the middle of what is ahead and round the fences, taps the hop at the brink of each two-block
  gap, and holds it from four blocks short of the three. It crosses every part, is never lower than the lowest
  piece, and ends on the far side still ridden. (On the way as it was first laid out, level, it passed once and failed
  once; see above.)

The layout case failed twice on its own rules before it passed: on the hill's ragged edge, which is the hill's and
is meant to be there; and on the vine, which is two wide and swings a block at a time, so that one row shares one
block with the next, where the case asked for two everywhere and should have asked that only across a gap.

### Not built, and not seen

- **A rider who misses a jump falls.** The proposal's "drops them onto the last island they stood on (a checkpoint),
  with a scare" is not built: they fall out of the place's room and are put back by the rule every outdoor place has,
  without their bike. That is the next thing. (Built in 0.4.495, below.)
- No perk ways, nothing that jumps out, nothing that chases, no way out by the back gate (it is shut).
- The mist in the Hollow is what it is outside the railings: none. What a rider should see out there is undecided.
- None of it has been seen in a client. Whether the gaps feel fair from a saddle, and whether the fences can be
  ridden round at speed by a person and not only by a case, are the owner's to judge.

## 0.4.495: somebody who misses

The proposal: "Missing a jump drops them onto the last island they stood on (a checkpoint), with a scare."

**Islands.** An island is the hill, the far side, or a run of the course's pieces that touch one another and is at
least five blocks long: long enough to get going on again. In the order they are ridden: the hill; the three graves;
the fences; the grave past the lids; the vine; the stairs with the top; the first half of the last stretch; its second
half; the far side. **The lids are not islands.** Each is two blocks long, and they are a bridge: somebody who goes
down from a lid goes back to the fences.

**Each island has one place** where somebody is set down on it: on its own line, eight blocks short of its far end
(the run they need to get going), or a block in from its near end where it is shorter than that. On the hill it is
on the gate's line between the small stones.

**Whose.** A reader's last island is their own, and is the hill until they have stood on another. On a bike it is the
island the bike is on the ground on. Whoever is on a Hopper's pegs has the island of the one pedalling.

**Missing.** Somebody has missed when they are two blocks under the lowest ground within six blocks of them along the
way, so nobody is taken out of a jump they might still land. They are set down at once on their island at its place,
pointing north, the way on, unhurt:

- riding, they are still on the bike, the bike stopped and upright, with whoever was on its pegs;
- on foot, they are on foot.

**The bike.** One let go of over nothing (its rider got off, or came off, in the air) is laid by its rider's island's
place, where they are about to be. It used to go back to the rack in the park, as it still does anywhere else. One
that lies in the Hollow anywhere but on its rider's island when they go down on foot (on a lid, on the next island) is
laid by them too: they could not have walked back to it. "By them" is the nearest free cell on whole ground beside
the place, or up to four rows behind it, and never where they stand.

**The scare** is theirs alone:

- the mist is over everything at once: as near as a block and a half, held for a little over a second, and gone again
  in a second. This is the one sudden change of the whole view in Bike night. It is as much of one as the reader's
  comfort setting lets through, and nothing at all with that at nothing;
- what is in the mist is heard: one new sound, subtitled "Something stirs in the mist" (39 cues);
- and a line: "You go down into the mist. Something in it has the back wheel, and lets go." On foot: "Something in it
  has your ankle, and lets go."

Anybody else sees mist where they went down and where they are.

**That there is mist under the Hollow, and something in it, is Claude's reading.** The proposal has the course "over
nothing", and has the Hollow's failure as "The screen goes to mist." Nothing is drawn down there.

### Cases

Four (83 in the namespace). All four passed the first time they were run.

- The islands are those, in that order; no lid is one; each place is a run short of its island's far end or a block in
  from its near end, has ground under it and room over it, and has somewhere by it to lay a bike; and what counts as
  the lowest ground near is the first grave by the hill's edge, the lids at the lids, the last stretch's beginning on
  the top, and the far side at the far side.
- **A rider on the Lamp** rides off the side of the first grave and is on it again at its place, on the bike, stopped,
  upright, pointing north, unhurt, told, with what is in the mist heard and the mist put over them; a second and a
  half later they have not moved. Off the second grave they are set down on the second, not the first. From the
  fences onto a lid and off the lid, they are set down on the fences.
- **Somebody on foot** who goes down off the hill's last rows, well off the gate's line, is set down on the hill on
  foot. On the Wasp, out over nothing, they get off: the Wasp lies by the third grave's place and has not gone back to
  the rack; they go down after it and are set down beside it, stand it up and get on. They leave it standing on the
  first lid and go down on foot off the lid's side: they are set down on the third grave, and the Wasp is off the lid
  and lying by them.
- **Two on the Hopper**, one on its pegs, go down off the stairs together and are set down together, both still on it,
  each told, each hearing it, each with the mist over them.

The cases move a reader on foot down by hand: a test's reader does not fall by themselves.

### Found by CI (run 1542, on 0.4.492's head)

The Bike night job failed on one case, the Wasp's: "in the same four ticks the Wasp has turned much further: 0.0
degrees against 0.0". Neither bike had turned at all. A strip case's road said it was ready when its chunks were
loaded. A chunk is promoted to ticking entities a ticket-level update after that, and this case measures four ticks
from the moment it puts its riders on: on the runner the promotion landed later than those four ticks, and the bikes
had not ticked. A strip now waits until both ends of its road tick entities (`NativeTestChunks.entityTicking`, which
was written for this after run 1370). Every other job that finished passed. `build` was cancelled by the next push,
so the cases that build every place, and the client proof, have not been run by CI on anything since 0.4.488.

### Not built, and not seen

- No perk ways, nothing that jumps out, nothing that chases, no way out by the back gate (it is shut).
- Nothing is drawn under the Hollow, and out on the course the mist is still none. What a rider sees out there is
  undecided.
- The mist over everything has not been seen in a client, and the sound has not been heard by anybody. Whether a
  block and a half for a second is a scare or a nuisance, and the two lines, are the owner's to judge.
- One place for each island, a run short of its far end, is a choice. Being set down where they left the ground would
  be kinder; at the island's near end, harsher.
- How many times somebody has gone down is counted for the night and is not yet in their record or the account.
- A reader's last island is kept with the night, not with the world: after the server stops and starts in the middle
  of the Hollow it is the hill again.

## The place

Since 0.4.492 the place stands at its own height with room seventy-two blocks over its ground and forty-eight under.
Since 0.4.494 it reaches 444 blocks north of the park's wall, where it reached 272.
