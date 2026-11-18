# The list, part 4b-2: the line (0.4.713)

The second of the school's set pieces (`docs/THE_LIST_PLAN.md`), after the boards (`docs/THE_LIST_0_4_711.md`). The hay hook finishes
one held anywhere; a set piece is the place's own finish for one held there, begun by the slasher's hand on the thing it is done with.
Like the hunt it is begun by hand until a night uses it.

## The line

A sprinkler line is laid down the home sideline for the night's watering: aluminum pipe in lengths, coupled end to end, from a cap by
the north end zone to an elbow level with the end of the home bench, where it turns into a rubber hose and runs to the valve on its
standpipe just past the bench. Four risers stand off it to the knee, each with an impact head on top, the nozzle cocked out toward the
field; one is level with the middle of the home bench, two blocks in front of where Deek and Wes sit. The pipe, the hose and the risers
are stepped over; the valve's standpipe is not.

The valve is anyone's. A hand on it opens or shuts it, with or without a hunt, for everyone: the heads start to turn and throw water out
over the field in their arcs, ticking, and stop when it is shut. The wheel stands up on its stem while it is open.

1. **Onto the riser.** The slasher has hold of one of the five within three and a half blocks of a riser and puts a hand on it (or
   strikes it). They are forced over backward onto it, and it goes through them: the head comes out through the front. They lie
   across the line on their back, arched over the riser, the legs down on one side of the pipe and the head on the other, the hands at
   the head where it comes out of them. Their own words, a scream (anyone on the list near enough runs, as in 4a), what they had in
   hand let fall, blood run down the riser into the grass.
2. **Let go of.** The slasher's walk is given back at once: there is nothing to hold now. The hold is still theirs, so the House's door
   stays shut to them and they take nobody else, until the one on the riser is dead. A hint, once: the valve is past the end of the
   bench.
3. **Pinned.** Nobody can lift them off: anyone's hand on them, the slasher's included, is told it will not come out. They live, and
   struggle and bleed, until the water comes, or the hunt ends.
4. **The water.** Opened, the line knocks with water, and the head inside them turns as the others do. What it throws is not water:
   blood out over the grass in its arc, falling where it falls, stains on the turf where it lands. Anyone within forty blocks who is
   not a spectator is told, once. After three seconds of water they go still: the scream stops short, and anyone in town within forty
   blocks who is not the slasher is told from which way. A pool spreads either side of them. The kill is kept in the record of the one
   who put them on the riser, whoever opened the valve, even if they have logged off since. Shutting the water off before then only
   stops the count; it begins again when the water does.
5. **Dead.** They lie across the line until the hunt ends. Anyone's hand on them is told they do not move.

## Found

As in 4a-3: the rest of the five who see them say so (Shannon's own line says where), and calm ones run; a reader who is not the
slasher, near and looking, is told once a hunt: "%s lies across the sprinkler line, not moving."

## Single and multiplayer

- **Alone**, the reader puts them on the riser and must walk to the valve to finish it; nothing else finishes it but the hunt's end.
- **With others**, one held is one slasher's, and only their hand on a riser begins it. The valve is everyone's: another reader may
  open it (the kill is still the one who put them there) or shut it (which only stops the count). Nobody can lift them off; what
  Amber's side can do about that is part 8's.
- **Spectators** touch nothing, turn nothing and are told nothing.

## Retreat

As while holding anyone: the House's door is shut to the slasher from the hold until the one on the riser is dead.

## Ended

The hunt ended (or begun again), everyone goes back to their place, unmarked, and the line is laid again as it was built: the valve
shut, the risers clean, the stains taken up.

## Made as

- **`TheListLine`** (new): where the line runs in the town (`X` the home sideline, from its cap at `N` to its elbow at `S`), the four
  `RISERS` (the third level with the middle of the home bench), `HOSE` and `VALVE`; the fitting's stages `PIPE`, `CAP`, `RISER`,
  `BLOODY`, `ELBOW`, `HOSE_RUN`, `SHUT`, `OPEN`; `REACH` 3.5, `BLEEDS` 60 ticks, `HEARD` 40 blocks. `hand` (a hand or a blow on the
  line: the valve for anyone not a spectator, a riser for the hunt), `turn` (the valve), `tick` (with the valve open, each head turns
  through its arc on the level's own clock and throws water every other tick, and ticks every five seconds for anyone within 24),
  `angle` and `falls` (where a head points and where its jet comes down, the same wherever it is asked).
- **`TheListSite.line`**: the line laid as it was built (valve shut, risers clean), where its ground is loaded; with the field, and
  again whenever a hunt begins or ends.
- **`TheListHunt`**: the state `LINE` (on a riser); gestures `IMPALED` (12, on the back across the line) and `SPITTED` (13, dead
  there); `GUT` (4, the marks bit); `riser(player, rel)` from a hand or a blow on a riser, `onto`, `spit` (kept across the riser),
  `lined` (each tick), `onRiser` and `water` (for the line and for tests); `riser`, `water` and `flowed` on the runner. One dead on
  the line is found by the body at the riser's height.
- **`TheList.click`** and **`TheListBody.punched`**: a hand or a blow on the line asks `TheListLine.hand` first.
- **`TheListBlock`**: the kind `LINE`, eight stages, its shapes (the pipe, the cap, a riser and its head, the elbow and hose, the
  valve with its wheel down or up), stepped over everywhere but the valve's standpipe.
- **`LiteraryActor`**: on the riser and dead there, a box the shape of one lying across it (1.2 by 1.0).
- **Client**: the two poses (`LiteraryRenderers.CastModel.hunted`, laid on the back by `setupRotations`, the middle held up at the
  riser's height): alive, the legs down to the grass on the near side kicking, the head hung back to it on the far side and rolling,
  the hands at the belly where the head comes out; dead, the legs slack, the arms fallen out either side, the head right back. The
  wound (`TheListMarks.Wound`, `gut.png`).
- **Sounds**: `the_list.valve` (the wheel squeals in its packing, the water knocks into the dry line and rushes down it) and
  `the_list.riser` (a pipe goes in), subtitled.
- **Art** (`tools/draw_the_list_art.py`): ten textures for the line (pipe and coupling, riser and head, each clean and bloodied, hose,
  valve body, wheel and stem) and its eight models; the wound over the five's skins, low on the belly front and back, on the body
  and the coat: worn on the back, so what runs, runs sideways off the belly; in front the cloth torn open round the hole, behind a
  smaller hole with the soak heavier.

## Tests

`TheListTests.theLine` (new, batch `the_list_line`): the line laid, the valve shut, the third riser level with Deek. Deek taken from
behind on the home bench, a second reader at the valve. A riser out of his reach, or a riser under the other reader's hand, does
nothing. A hand on the riser in front of the bench: onto it, the slasher's cue, his words, set across the riser lying across the line
in a box the shape of him, the wound's mark, the riser bloodied, drops either side; still held with no retreat, but the slasher walks;
the other reader hears it and the scream. A hand on him: he will not come out. Forty ticks on: the valve's hint to the slasher, and
unwatered he lives. The other reader opens the valve and is told; twenty ticks on, the water through him told to both, the heads
throwing water and his throwing blood, stains where it lands; shut, and twenty ticks on the count has stopped; open again, he goes
still across the riser, pools either side, the slasher told, the other reader told a scream stopped, the kill kept by the slasher,
the hold let go, a hand on him after and he does not move. The other reader, come near and looking, finds him across the line. Ended:
the stains taken up, the line laid again as it was built, Deek back and unmarked.

## Seen in the frames

Eye-level frames of the line before a hunt, Deek on the riser, the water on and Deek dead there: from where the slasher stands
behind the bench, from up and down the sideline, from the field, close by the riser, and from the valve.

- **The line** reads from the valve: the pipe down the sideline in its lengths, the couplings, the risers to the knee with their brass
  heads cocked out toward the field; the wheel down on its bonnet when shut and up on its stem when open.
- **On the riser** reads from every side but behind the bench: on his back across the line, the middle held up at the riser's height,
  the legs down to the grass on the bench side, the head hung back on the field side, the riser's head standing up out of his front.
  From behind the bench he is seen end on, past his feet, and reads as a body on the line only once the camera moves.
- **Dead there**, the arms have fallen out either side and the head is right back; a pool spreads under the head and under the legs,
  and the field to the east is spotted where the jets came down.
- **Found and fixed**: the review's own slasher reached for the valve from fourteen blocks away and the town did not answer; it is
  walked to the valve now, as a reader has to be.
- Particles (the jets, what comes out of him) are not drawn by the review tool; the case checks they are sent.

## Every new line

Speech, over the speaker's head as their name, forced onto the riser:

- Deek: "Get it out. Get it out of me."
- Wes: "It's in my back. It's in my back!"
- Mel: "Don't turn it on. Please don't turn it on."
- Lesley: "I can't feel my legs."
- Shannon: "Jenna, call somebody. Call anybody."

Shannon, coming across one of the others on the line (the others keep their lines from 4a-3): "That's %s. That's %s on the sprinkler."

Grey cues on the action bar, to the slasher:

- "You force %s back onto the riser, and its head comes up through the front."
- "The line's valve stands past the end of the bench."
- "%s goes still. The sprinkler head goes on turning."

To anyone whose hand is on one on the riser: "%s is fixed on the riser. It won't come out."

To whoever turns the valve: "The wheel turns. Water knocks down the line." and "The wheel turns back. The heads run down and stop."

To everyone near, once, when the water reaches one on the riser: "The sprinkler line knocks with water. On the home sideline, %s
screams."

To a reader who is not the slasher, coming across them: "%s lies across the sprinkler line, not moving."

Describing the line (a look at it): "Aluminum pipe laid down the sideline, length to length.", "The end of the line, capped. Mud dried
round the cap.", "A riser to the knee, a sprinkler head on top. Its arm rests against the nozzle.", "A riser. Something has run down it
into the grass, and the head is clogged with it.", "The line turns here into a hose.", "A hose, flat in the grass.", "The valve. A
wheel on top to open it." and "The valve, open. The line knocks with water."

Subtitles: "A valve wheel squeals", "A pipe goes in".
