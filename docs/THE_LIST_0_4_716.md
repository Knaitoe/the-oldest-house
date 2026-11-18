# The list, part 4d-1: the pumpjack (0.4.716)

The first of the pasture's set pieces (`docs/THE_LIST_PLAN.md`), after the drive-in's (`docs/THE_LIST_0_4_714.md`,
`docs/THE_LIST_0_4_715.md`). Like the others it is begun by the slasher's hand on the thing it is done with, and by hand until a night
uses it.

## The pumpjacks

Three pumpjacks stand on their caliche pads in the pasture south of town (`docs/THE_LIST_0_4_701.md`), each nodding over its well. Each
has a band brake on its gearbox: a drum on the gearbox's cap and a lever beside it. A hand on the gearbox hauls the brake on, for anyone
who is not a spectator, with or without a hunt: the ratchet clacks, the band bites, and the jack slows and stops where it is, its
beam held, the lever pulled up. Another hand knocks it off, and the jack runs on from where it stood. Everyone near sees and hears the
same.

Each counterweight swings through a slot in the gearbox's skid, down to the ground and up again, once a stroke.

1. **The weight, coming round.** The slasher with hold of one of the five near a pumpjack's gearbox puts a hand on it (or strikes it).
   If the weight is down in its slot, or will be in less than a third of a stroke, they are told the weight is coming round, and
   nothing happens yet. Braked, the weight hanging in the slot leaves no room under it; braked anywhere else, the head can go in.
2. **Under it.** With the weight clear, they are forced down on their knees beside the gearbox, bowed right over, and their head is
   pushed down onto the ground in the near weight's slot, the cheek to the ground. Their own words, a scream; what they had in hand they
   let fall. Anyone on the list near enough runs, as in 4a.
3. **Held there.** The slasher holds them down: their walk stays stilled and the House's door stays shut to them. The one under the
   weight fights to lift the head as they fight a grip, but it takes them twice as long; longer still while the slasher looks at them.
   Torn loose, they get up and run. The slasher going from the town lets them up the same way.
4. **The brake.** Anyone else's hand on the gearbox hauls the brake on, and the weight does not come; it hangs where it is until the
   brake is off again. The slasher's hand, on the gearbox or on the one held, knocks it off.
5. **The weight comes down.** Running, the near weight comes round and down onto the head, and the jack stops there with a crack: the
   motor strains against it, grinds the weight a little further on into it, and labours for a few seconds more before it only stands.
   They go still. The slasher is told; anyone in town within forty blocks who is not the slasher is told from which way a scream
   stopped. The kill is kept in the slasher's record, even if they have logged off since. Blood runs out from under the head into a pool.
6. **After.** The jack stays stopped on them. A hand on its gearbox finds the brake makes no difference; a hand on them is told they do
   not move.

## Found

As in 4a-3: the rest of the five who see them say so (Shannon's own line says where), and calm ones run; a reader who is not the
slasher, near and looking, is told once a hunt that they are kneeling at a pumpjack with the head under its weight.

## Single and multiplayer

- **Alone**, the reader catches one of the five, brings them out to the pasture, waits for the weight to clear, and forces them down
  under it. The brake is theirs as much as anyone's.
- **With others**, the brake is anyone's: another reader may haul it on with one of the five under the weight, and as long as it stays
  on the weight does not come. Only the slasher's hand knocks it off again while they hold someone there, and the kill is theirs. Nobody
  else can lift the one held; what Amber's side can do about that is part 8's. The jack's stroke, its brake and its stall are the same
  for everyone who can see it.
- **Spectators** touch nothing, brake nothing and are told nothing.

## Retreat

As with one held (4a-2): while the slasher holds one of the five under the weight, the House's door is shut to them and they cannot walk
away from the jack. Once the one under it has gone still, the hold is let go: the slasher walks, and the door is open to them again.

## Ended

The hunt ended (or begun again), everyone goes back to their place, unmarked, and every jack is let go: its brake off, running on from
where it stood.

## Made as

- **`TheListMachineBlock.Turning`**: a pumpjack's own tick, which is the level's while it runs (plus an `offset`) and held at `stop`
  while it stands; `brake` (held where it is, the lever up), `stall` (held at a tick of its own, the motor straining or not), `run` (let
  go from where it stood). Sent to everyone with the block (its update tag), never saved, so a server started again finds every well
  running; the lever's swing is eased over four ticks on the client. `Machine.BRAKE` (never placed) is the lever's model.
- **`TheListMachines`**: `crankAt(tick, at)`, `tick(entity, time, partial)`; `knocks` at a well's own tick, and never while it stands.
  The pasture's knock is asked only of wells whose ground is loaded.
- **`TheListJack`** (new): each well's frame, turned as its renderer turns it (`frame`), the gearbox (`gearbox`, `of`), where the head
  is laid in the slot (`head`), where one forced down kneels and faces (`kneel`, `kneelYaw`), where their blood lands (`under`). `JAM`
  and `CLEAR`, the crank's angles at which the near weight first meets a head laid in its slot and last covers it, worked out once from
  the weights' boxes; `LEAD`, a third of a stroke's warning; `GRIND` (seven degrees over `GRIND_TICKS` 30); `inSlot`, `clearOf`.
  `hand` (a hand or a blow on a gearbox: the hunt's first, otherwise the brake), `brake`, `stall` and the town's `tick` (the grind, the
  motor heard every two seconds for `STRAIN` 110 ticks, then still), `reset`, `setAngle` (for tests).
- **`TheListHunt`**: the state `JACK`; gestures `PUMPED` (18) and `PINNED` (19); `FLAT` (32, the marks bit); `jack(player, base, well)`,
  `pumped`, `bowed`, `jacking` (each tick: the well's own tick watched across `JAM`, the struggle at twice the grip's strength),
  `pinned` (where the weight comes down). A hand on one under the weight: the slasher's knocks the brake off, anyone else's finds them
  held down. One dead at a jack is found by the back, a little up.
- **`TheList.click`**, **`TheListBody.punched`**: a hand or a blow on a pumpjack's gearbox asks `TheListJack.hand`. The town's tick runs
  `TheListJack.tick`. The gearbox's shape stands as high as its cap.
- **Client**: `TheListMachineRenderer` draws a pumpjack at its own tick, its lever on its pin atop the gearbox, and, stalled against the
  motor, a shudder through it. The two poses (`LiteraryRenderers.CastModel.hunted`): on the knees bowed right over, the head on its side
  on the ground and twisting, the arms out flat either side pushing; still, sunk flat, the arms gone slack along the ground. The flattened
  head (`TheListMarks`, `flat.png`).
- **Sounds**: `the_list.brake` (the lever's ratchet, the band biting on the drum and squealing as the gears slow, the weights settling)
  and `the_list.strain` (the motor's hum pulled down under load, the belt slipping, the teeth grinding), subtitled.
- **Art** (`tools/draw_the_list_art.py`): the gearbox's skid rails stop either side of where the weights swing lowest, a slot through
  to the ground; the brake's drum, cheeks and pin on the cap (`pumpjack_7`); the lever (`pumpjack_brake`: a galvanized bar, a worn grip,
  the latch rod); `flat()` over the five's skins: the right side of the head crushed, dark and split and wet through the middle, the hair
  over it soaked, the edges of the face, top and back where they meet it caught, blood from the right eye, the nose and the mouth, and the
  lower half of the left side, lying in it.

## Also

- **The line's spray** (4b-2): with no fresh open cell along its head's arc (the arc over the stands, or all of it red already), it
  tries the arc turned to either side, fifteen degrees at a time, so every spray still leaves new blood. The line case missed it once
  in a full run.

## Tests

`TheListTests.theJack` (new, batch `the_list_jack`): the north well running, its brake off. Another reader hauls the brake on, told so and
hearing it: the well stands where it is for five ticks; knocked off, it runs on from there. Deek taken from behind on the home bench and
brought out to the well, held by its gearbox. With the weight in its slot, a hand on the gearbox tells the slasher it is coming round and
he is still held. Clear of it: forced down, the slasher's cue, his words, on the knees beside the gearbox bowed toward it, still held
with no retreat and no walk, the other reader hearing him. A hand from the other reader on him: held down. The other reader hauls the
brake on and, set a moment from the head, the weight does not come. The slasher's hand on him knocks it off, and his hand on the gearbox
after does nothing more. The weight comes down: still, the side of the head marked, the well stopped where it met him, the slasher told,
the other reader told a scream stopped and hearing the motor, the kill kept by the slasher, the hold let go, a pool under the head.
Ground on into him, and then a hand on the gearbox finds the brake makes no difference; a hand on him and he does not move. The other
reader, looking, finds him at the pumpjack. Ended: the well running, Deek back and unmarked.

## Seen in the frames

Eye-level frames of the north well running, braked, Deek forced down under it, the weight a moment off and dead there: from the west,
close over the gearbox, from the south and north along the pad, from across the pasture, and at the lever.

- **The well** reads on its caliche pad, the beam nodding, the weights swinging down through the skid's slot and up again. Braked,
  the lever stands up on the gearbox's cap and the beam holds where it stopped.
- **Under the weight**, Deek kneels beside the gearbox bowed right over, the side of the head on the ground in the slot, the arms out
  flat either side.
- **Dead**, the near weight rests down on the head, the motor's housing beside it, and the pool spreads out from under it.
- **Found and fixed**: the frame tool drew each well at its own tick from its place in the town, but a well's stroke is offset by
  where it stands in the world, so the first frames showed the weight a block above the head. The tool now carries the same offset;
  the game was right. The lever's frame stands too close and looks into the gearbox; the lever reads from the south and close views.
- Particles (the blood off the head) are not drawn by the review tool; the case checks the pool under the head.

## Every new line

Speech, over the speaker's head as their name, forced down under the weight:

- Deek: "Not under that thing. Come on, not under that."
- Wes: "It's still going! Turn it off! It's still going!"
- Mel: "There's a brake. On the box. Somebody pull the brake."
- Lesley: "My face is in the dirt! Get off me!"
- Shannon: "Jenna! Jenna, I'm out at the wells!"

Shannon, coming across one of the others at a pumpjack (the others keep their lines from 4a-3): "That's %s. That's %s at the pumpjack."

Grey cues on the action bar, to the slasher:

- "You force %s down on the knees beside the gearbox and hold the head where the weight comes round."
- "Not yet. The weight is coming round."
- "The weight hangs in the slot. There's no room under it."
- "The weight comes down on the head and stops there. %s goes still."

To anyone else whose hand is on one under the weight: "%s is held down by the gearbox, the head under the weight."

To anyone whose hand is on the gearbox with one dead under it: "The well has stopped on %s. The brake makes no difference now."

To whoever hauls the brake on or knocks it off: "You haul the brake on. The jack slows and stops." and "You knock the brake off. The
jack starts up again."

To a reader who is not the slasher, coming across them: "%s kneels at a pumpjack, the head under its weight."

Subtitles: "A brake bites", "A motor strains".
