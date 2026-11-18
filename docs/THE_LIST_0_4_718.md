# The list, part 4e-1: the gin's auger (0.4.718)

The first of the gin's set pieces (`docs/THE_LIST_PLAN.md`), after the pasture's pumpjack and stock tank (`docs/THE_LIST_0_4_716.md`,
`docs/THE_LIST_0_4_717.md`). The plan keeps the gin for the fourth night: the five are only taken there then. Until the nights are built
(parts 5 to 7), a hunt is begun by hand, and now on a night of the operator's choosing.

## The gin's line

The gin by the tracks (`docs/THE_LIST_0_4_699.md`) has its augers in an open trough down the middle of the building, from the module
feeder at the yard end toward the lint cleaner. The trough now lies along the run, as its blocks are laid (before this, each block's
trough was turned across it).

1. **The switch.** The feeder's lower half carries the switch. Anyone in town who is not a spectator throws it with a hand or a blow:
   off, the augers run down and stop; on, the gin shudders and they turn again. Before the fourth night it only clicks: nothing in the
   gin moves.
2. **Running.** On the fourth night, while the switch is on and nothing is jammed, the line runs: heard every four seconds from the middle
   of the run, and seen as lint lifting off the trough, while anyone is within forty blocks.

## In the auger

1. **In.** The slasher with hold of one of the five near the trough puts a hand on the auger (or strikes it). Before the fourth night
   the slasher is told the augers are not turning and the screw takes nobody; nothing happens. On the fourth, the slasher forces them
   down into the trough feet first and lets go: the screw has them now. Their words, a scream; anyone on the list near enough runs, as
   in 4a. What they had in hand they let fall. The slasher walks free, and the House's door is open to them again.
2. **Drawn down.** While the line runs, the screw draws them down into it, the legs first, over five seconds, blood coming up off it and
   running out onto the floor beside the trough. They cannot get out of it while it runs.
3. **The switch thrown.** With the line stopped, the screw stops drawing them, and they fight their way out of it as they fight a grip
   (4a-2), at twice its pace. Out, they run. Thrown on again before they are out, the screw takes them where it left off.
4. **Drawn all the way.** Drawn down to the waist, they die there, slumped forward over the trough with the arms hanging over its
   sides. The screw jams on them and the line stops for good; the switch turns nothing after. The slasher is told; anyone in town within
   forty blocks who is not the slasher is told from which way a scream stopped. The kill is kept in the slasher's record, even if they
   have logged off since.
5. **After.** A hand on the auger there finds them down in the trough to the waist and the screw not turning; a hand on them is told
   they do not move.

## Found

As in 4a-3: the rest of the five who see them say so (Shannon's own line says where), and calm ones run; a reader who is not the
slasher, near and looking, is told once a hunt that they are down in the auger's trough to the waist.

## Single and multiplayer

- **Alone**, nobody else reaches the switch: the reader puts one of the five in and the screw does the rest. A reader may throw the
  switch themselves, and on again.
- **With others**, the switch is anyone's: a reader who gets to it in time stops the line and the one in the screw fights free; the
  slasher, walking free once the screw has them, may stand at the switch or throw it on again. A hand on the one in the trough, the
  slasher's or anyone's, finds them held in the screw (Amber's side, part 8, adds what they can do besides). The line's sound, lint and
  blood are the same for everyone near.
- **Spectators** touch nothing, throw nothing and are told nothing.

## Retreat

Once the screw has them, the slasher has let go: their walk and the House's door are their own again, as after any hold let go.

## Ended

The hunt ended (or begun again), everyone goes back to their place, unmarked; the switch is back on and the screw cleared.

## Made as

- **`TheListGin`** (new): `MID`, `RUN_S`, `RUN_N` (the auger run), `SWITCH` (the feeder's lower half), `NIGHT` 4, `REACH` 2.6,
  `DEPTH` .85, `DRAW` 100; `auger` and `feeder` (which blocks of the town they are), `running` (the fourth night, switched on, not
  jammed), `off`, `jammed`, `in` (where one in the trough is, by how far drawn), `beside` (where blood lands), `hand` (the switch for
  anyone; an auger, the hunt's first), `toggle`, `tick` (the hum and the lint), `reset` and `clearAll`. Off and jammed are kept per town,
  never saved.
- **`TheListHunt`**: the state `AUGER`; gestures `AUGERED` (23) and `MANGLED` (24); `Hunt.night` and `start(level, base, night)`;
  `night(base)`, `auger(player, base, cell)`, `augered`, `sink`, `augering` (each tick: drawn while the line runs, the blood and the
  struggle, fighting free at twice a grip's pace while it is stopped), `mangled` (the screw jams); `drawn`, `strengthIn` and `night`
  for tests. One mangled is found by the back, slumped over the trough.
- **`TheListCommands`**: `hunt <night>` begins the hunt on one of the four nights; `hunt` alone is still the first.
- **`TheListBlock`**: the auger's shape turns with its facing. **`TheListSite.gin`**: the auger blocks face along the run.
- **`TheList.click`**, **`TheListBody.punched`**: a hand or a blow on the gin asks `TheListGin.hand`.
- **Client**: two poses (`LiteraryRenderers.CastModel.hunted`): stood in the trough leaning back from the screw, the arms out to its
  sides bracing and shoving, the head thrown about; still, slumped forward over it with the arms hanging over its sides. The body is
  drawn down through the trough's floor as the screw draws it, so the legs are gone into it.
- **Sound**: the existing `the_list.auger` (the line running, and the screw taking them), `the_list.strain` (the switch, and the motor
  labouring when it jams) and the vanilla lever click for the switch. No new sound or art.

## Tests

`TheListTests.theAuger` (new, batch `the_list_auger`): the run and the switch; the trough lies along the run. The first night: the gin is
still, the switch only clicks, and the slasher's hand on the auger with hold of Deek is told not tonight and changes nothing. The fourth
night: the gin runs; the slasher's hand puts Deek in, feet first: the slasher's cue, his words, the slasher let go of him and free to
leave, the other reader hearing him go in. The other reader's hand on him, and on the auger: held in the screw. While it runs he is drawn
down and does not fight his way out. The other reader throws the switch: the line stops, and he starts to fight up out of it; the
slasher throws it back on. Drawn all the way, he dies there: slumped in the trough, the screw jammed, the slasher told, the kill kept, the
other reader told a scream stopped. The switch then turns nothing; a hand on the auger finds him still; the other reader, looking, finds
him in the auger. Ended: Deek back, the gin put back.

## Seen in the frames

Eye-level frames of Deek just put in, drawn halfway, and dead, each from west, east, north, south and close. The trough now reads as one
run along the gin's floor. Just in, he stands in it with the arms out to its sides; halfway, he is down in it to the waist and still
bracing; dead, he lies slumped forward over its side with the arms hung over it and blood pooled on the floor beside it. Nothing needed
changing.

## Every new line

Speech, over the speaker's head as their name, put into the auger:

- Deek: "My leg! It's got my leg!"
- Wes: "Turn it off! Somebody turn it off!"
- Mel: "The switch! By the big door, the switch!"
- Lesley: "Pull me out! Grab my arms and pull!"
- Shannon: "Jenna! Jenna, I'm in the gin!"

Shannon, coming across one of the others in the auger (the others keep their lines from 4a-3): "That's %s. That's %s in the gin."

Grey cues on the action bar, to the slasher:

- "You force %s down into the auger's trough, feet first, and let go. The screw has them."
- "The screw jams on what it has taken and the gin stops. %s goes still."
- Before the fourth night: "The augers aren't turning tonight. The screw won't take anyone."

To anyone whose hand is on one in the auger, or on the auger there: "%s is down in the auger's trough, the screw turning on the legs."

To anyone whose hand is on the auger where one died: "%s is down in the trough to the waist. The screw doesn't turn."

To anyone throwing the switch:

- "You throw the switch. The augers run down and stop."
- "You throw the switch. The gin shudders and the augers turn."
- Jammed: "You throw the switch. Nothing turns; the screw is jammed."
- Before the fourth night: "The switch clicks. Nothing in the gin moves tonight."

To a reader who is not the slasher, coming across them: "%s is down in the auger's trough to the waist."
