# The God of the Pitt (0.4.207)

The owner's third proposal of 31 October (docs/OWNER_VIGNETTE_PROPOSALS_2026_10_31.md, section 3), built after the Dead Air
Broadcast. It reads, as the proposal's own fit note said, as Ted the Caver's next form: the deeper passages under that cave,
with Brad as a real partner, the draft that crushes, something in the wall, a cut line and a changed chimney, and a ledge over
nothing as the last crossing.

Slot 133, `LabyrinthPlace.PITT` (`god_of_the_pitt`), a multi-visit literary scene with four visits. It stands at its own height
(`PittCave.BASE_Y`, 200) in the outside dimension so the void under the ledge has somewhere to go, and is found there by its real
bounds, as the lookout is. Layout 63, protocol 114.

**Witness is unchanged: 61 sources / 46 required / two kinds / three endings.** The deeper passages are dealt only to a reader who
already has Ted the Caver's resolution (`retraced the squeeze`). That story stays the one Witness credit; this place adds none.
Reading the field journal at the end marks the passages finished for that reader, and they are not dealt again.

## The place

`PittCave` builds it whole (the polish, review and playtest passes skip it):

- **Brad's staging ledge.** A domed sinkhole floor of red clay under slate limestone, with ochre bands. Brad's rope hangs down
  the sinkhole's throat from the moor above. There are carbide lanterns, rope coils, piton tins and carbide canisters, and Brad's
  gear crate. His card is on a piton in the west wall (the source). Brad sits on a crate by the stoopway at the belay post, his
  lamp on his yellow hard hat. The way-on door is in the rock by the entry.
- **Floyd's Tomb.** An eighteen-block crawl, one high, on hands and knees. Its ceiling is pinch-ceiling slabs, broken free of the
  rock above. Four seams of harder stone cross it, and bolts are set in its west wall. Along its east side is a fissure two
  fingers wide, opening into an unlit parallel crawl. At its end is the fist-sized hole.
- **The Wind Tunnel and the Round Stone Room.** Past the hole is a vault a crouch high, then steps down into a round room with
  calcite flowstone on its walls. A round stone four blocks across stands on edge against the west wall in copper-sheathed
  grooves worn bright.
- **The chimneys.** The first is in the room's north-east corner: one drop on a rope to a squeeze that pinches out into a
  fissure. The second is behind the stone, opened from the third visit: three drops on Brad's ropes joined by three squeezes,
  with fissures beside them.
- **The Pre-Echo Ledge.** A landing at the second chimney's foot opens onto a void. A shelf a block and a half wide runs along its
  near edge. Bridges nobody can see (`BRIDGE`, solid and never drawn) wind across to a rusted ladder on the far wall, which climbs
  a shaft to an iron hatch in the moor.
- **The moor.** Grass, ferns, spruce and a rocky rim over the whole cave. The sinkhole's throat has Brad's rope tied off at a
  post, and the field journal lies open on a flat stone by the hatch (the account). The scene clock is at first light (new mode
  14, 23200).

One fitting block, `pitt_fitting` (24 kinds, eight stages), a climbing block `pitt_climb` (Brad's fixed ropes and the rusted
ladder, both in the climbable tag) and four materials are drawn by `tools/draw_pitt_art.py`. That is 60 textures, including the
round stone's sixteen faces cut from one drawn disc and the barometer's dial. Eight subtitled sounds come from
`tools/generate_pitt_audio.py`: the draft, the rock taking the weight, a slab stepping down, the hammer on a seam, something in
the fissure, a footstep heard early, the passage coming down, and the barometer's needle.

## Brad's tools

Brad's crate gives the reader his **heavy masonry hammer**, his **carbide caver lamp** and his **brass aneroid barometer**:

- **The barometer.** While it is carried, `PittClient` draws its dial in the corner of sight. The needle is in the green in the
  draft, swings and shakes as the draft dies, and is hard over in the red in the still air.
- **The lamp.** Held in either hand, it lights the passages (a light block at the reader's eyes). In the still air it is a reason
  for the ceiling to come down.
- **The tools stay in the passages.** A reader who leaves with them has left them in the crate.

## The draft

One draft cycle is shared by everyone in the passages, and counts only while someone is in them:

- **Fourteen seconds of draft.** White dust runs out along the crawls toward the ledge, and Brad's lamp flashes white twice.
- **Three seconds of warning.** The dust slows and turns, the needle jumps and shakes, and Brad's lamp strobes amber.
- **Six seconds of still air.** Silt falls, and the rock takes the weight with a groan.

**The crush.** In Floyd's Tomb in the still air, moving (faster than 0.02 a tick), swinging or holding a lit lamp steps the slab
over that reader down one stage a tick, as the proposal has it. The slab is drawn lower into the crawl, two pixels a stage. At
its sixth stage the reader is pinned: held where they are and hurt a point every half second. When the draft comes back the
slabs lift a stage at a time and let them go. Pinned to their last heart, they black out instead and wake on the staging ledge.
The caver vignette has always chosen a blackout over a death, and this keeps it.

**The Seam-Crawler** lives only in the fissure beside the Tomb, and on the third visit in the second chimney's cracks too. It is
two green points in the crack. In the draft it keeps out of sight. As the draft dies it comes along the fissure toward the reader,
and in the still air it reaches out at movement or light: a pale hand out of the crack, and two more stages of slab.

## The four visits

1. **The seams.** The four seams are cut with the hammer, six strokes to a stage and four stages to cut one through, and only in
   the draft. In the warning the hammer does nothing. In the still air it brings the ceiling down three stages and wakes what is
   in the fissure. At the end, the fist-sized hole, smelling of wet copper, finishes the visit.
2. **The line.** The hole is broken through. The orange line is clipped bolt by bolt, in order, from the belay to the round room
   (six bolts), and drawn for its owner alone. With the line in, kneeling at the stone's grooves brings the room down. Lamps go
   out, the dark closes in, rubble falls down the room's sides, and after a breath the collapse comes back along the passage a
   block every 24 ticks, the slabs closing behind it. A reader it catches, or one still in the round room when it starts moving,
   blacks out and wakes on the ledge with the visit still to do. A reader who was out in it and gets back onto the ledge ahead of
   it finishes the visit; Brad hauls the last of the line in. With nobody left past the stoopway, the passage settles.
3. **The shifted return.** Brad is gone, and his belay line hangs off the post, untied. The round stone has been pushed out along
   its grooves from the inside, and there is a way behind it. His cracked helmet lies on the floor with its lamp blinking amber,
   pointed at the hole. The first chimney is choked with rubble; the second is open, with his line down it and into a crack two
   fingers wide, frayed. The eyes keep pace in the cracks. Reaching the ledge at the chimney's foot finishes the visit. Out over
   the nothing there are footsteps, and the hatch is rusted shut.
4. **The bottomless ledge.** The draft is steady and nothing is in the walls. On the ledge the reader's own footprints show two
   steps ahead, with a faint arc where they turn, and the reader's next footstep is heard where the prints are before they take
   it: the pre-echo, private to each reader. The prints go out over the bridges. A fall into the void puts the reader back on the
   landing, on their knees, never having hit anything. On the last visit the hatch gives from below. Out on the moor the account
   is ready (`climbed_out`).

## Voices and the account

There are two hands, both original:

- **Brad's card**, in pencil capitals (the source). Gear in the crate; four seams, cut only while the draft blows; lie flat,
  lamp away, until it comes back; two flashes good, amber not; R.'s line, from Ted the Caver's notebook, is still tied off at the ladder up top, and nobody
  has untied it. On the back: last time it breathed in; this time he thinks it is
  holding its breath.
- **The field journal** (the account), four logs in the reader's own hand, dated as cavers dated them online in 2001, each with a
  survey drawn in letters. They are written from what the reader did:
  - the strokes of the hammer, the times pinned, the hand from the crack, the hole;
  - the bolts clipped, the stone, the collapse and the crawl back along the line, and whoever held the belay;
  - the slack belay, the stone pushed out, the helmet, the frayed line, the eyes, the ledge;
  - the prints two steps ahead, the falls, the bridges, the ladder, the hatch and the grey sky.

  It ends: "I don't know whose prints they were. They fit me. B. isn't coming up." The account title is "The account: the field
  journal".

Also added: a miniature note (a flowerpot turned upside down with a thread of orange cotton going down into it) and the House of
Leaves place name ("the passages under the cave"). With no Witness story, there are no Witness, co-op-together or advancement
lines; co-op is written into the journal itself.

## Several readers

The passages, the draft, the slabs, the seams, the collapse, the stone and the hatch are shared. Each reader's part is their own:
their tools, the bolts they clipped and the line drawn through them, their footprints and early footsteps, their pins, falls,
blackouts, visits and journal.

**The belay.** With two or more readers on the first two visits, one can take the belay from Brad, who goes up the rope for
carbide. The one on the belay hauls a pinned partner out from under the slab with the line, and gets the partner's beat on the
same visit. Walking off lets the line go. Both journals say who held it and who was hauled.

## Tests

Seven `PittTests` in the literary namespace:

- only a reader who has retraced the squeeze is dealt the deeper passages;
- the seams are cut only in the draft, and the fist-sized hole ends the first visit;
- in the still air moving brings the slab down and the draft lets it up, with the blackout at the last heart;
- the line to the round room, and the collapse behind it, end the second visit;
- on the third visit Brad is gone, the stone is out and the chimney has changed;
- the footprints cross the bridges, and the ladder climbs out to the journal;
- a second reader takes the belay and hauls a pinned partner out.

The architecture case exports the cave (the source's discovery surface is Brad's note) and checks its reachability. The eligible
literary sources case leaves the passages out of the Witness pool, alongside the old man's cabin.

The first local run found two things. A reader blacked out by the collapse woke on the ledge and counted as having got out
ahead of it; only a reader who was out in it and not caught does now. The journal case now reads its last page the way the
fixture reads every account.
