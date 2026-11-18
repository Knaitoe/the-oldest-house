# The list, part 4c-1: the fryer (0.4.714)

The first of the drive-in's set pieces (`docs/THE_LIST_PLAN.md`), after the school's boards and line (`docs/THE_LIST_0_4_711.md`,
`docs/THE_LIST_0_4_713.md`). Like them it is begun by the slasher's hand on the thing it is done with, and by hand until a night
uses it.

## The fryer

The Caprock's stand has a fryer against its front wall, under the order windows: two vats of oil with a basket hung over each, a knob
and a pilot on its front. At night it is off and the oil is cold. A hand on its knob lights it, for anyone who is not a spectator, with
or without a hunt: the burners catch under the oil, and it takes eight seconds to come up to heat, ticking. Hot, the oil shimmers and
smokes, and anyone in the stand is told so once. Another hand on the knob puts it out; the oil goes cold again.

1. **The knob, held.** The slasher with hold of one of the five can still turn the knob with the other hand; the fryer heats while they
   hold them. Held near it while it is still warming, a hand on it tells the slasher the oil is not hot yet.
2. **Into the oil.** The oil hot, the slasher with hold of one of the five within three blocks of the fryer puts a hand on it (or
   strikes it). They are forced in front of it and bent over it, and their face is pushed down into the vat. Their own words as they
   go over, a scream, the oil's roar; what they had in hand they let fall. Anyone on the list near enough runs, as in 4a. If Jenna is in
   the stand she shouts at the slasher to let go, backs away into the stand's far corner and gets down there until the hunt
   ends.
3. **Held under.** The slasher holds them there: their walk stays stilled, and the House's door stays shut to them. The one in the
   oil fights the edges of the fryer, the oil spits and smokes, and after three seconds they go still: the struggle stops, anyone in
   town within forty blocks who is not the slasher is told from which way a scream stopped, and the slasher is told they have gone
   still. They stay bent over the fryer with their face in it, one arm hanging down its side and the other lying back across its
   top. The kill is kept in the slasher's record, even if they have logged off since.
4. **Put out.** Anyone who puts the fryer out while one of the five is in it stops the count; it begins again when the oil is hot
   again. The slasher going from the town lets them up: they come out of the oil burned, and run.
5. **After.** The oil has gone dark and keeps the shape of what was in it. Anyone's hand on them is told they do not move.

## Found

As in 4a-3: the rest of the five who see them say so (Shannon's own line says where), and calm ones run; a reader who is not the
slasher, near and looking, is told once a hunt that they are bent over the fryer.

## Single and multiplayer

- **Alone**, the reader lights the fryer, catches one of the five in or by the stand, and puts them in it.
- **With others**, the knob is anyone's: another reader may light it or put it out, and putting it out only stops the count. Only the
  slasher holding one of the five puts them in, and the kill is theirs. Nobody else can pull them out; what Amber's side can do about
  that is part 8's.
- **Spectators** touch nothing, turn nothing and are told nothing.

## Retreat

As with one held (4a-2): while the slasher holds one of the five in the oil, the House's door is shut to them, and they cannot walk
away from the fryer. Once the one in it has gone still, the hold is let go: the slasher walks, and the door is open to them again.

## Ended

The hunt ended (or begun again), everyone goes back to their place, unmarked, and the fryer is laid again as it was built: off and
cold, the oil clean.

## Made as

- **`TheListFryer`** (new): where it stands in the stand (`AT`, its knob to the room, `FACING` north), the fitting's stages (`OFF`,
  `WARM`, `HOT`, and `DARK` added for the oil gone dark), `HEAT` 160 ticks to come up, `REACH` 3, `UNDER` 60 ticks, `TOLD` 8 blocks,
  `STAND` (where one forced into it stands, bent over the west vat). `hand` (a hand or a blow: the hunt's first, otherwise the knob),
  `turn` (the knob: the burners catch or go out; lit, the block's own tick is scheduled), `cameUp` (the block's tick: hot, if still
  lit, told to anyone within eight), `tick` (hot oil bubbles and smokes over both vats and is heard close by every three seconds),
  `vat`, `darken`.
- **`TheListBlock`**: the kind `FRYER`, six stages (off, warming, hot; each with the oil clean or dark), a faint light while lit,
  solid to the vats' rim; its scheduled tick is the oil coming up to heat.
- **`TheListSite`**: the drive-in's stand lays the fryer, off; `fryer` lays it again as it was built whenever a hunt begins or ends.
- **`TheListHunt`**: the state `FRYER`; gestures `FRYING` (14) and `FRIED` (15); `BURN` (8, the marks bit); `fryer(player, base)` from
  a hand or a blow on the fryer, `into`, `bend` (kept bent over it, facing into it), `frying` (each tick), `fried` (for tests); `fried`
  on the runner. The hold stays on through it, so the slasher's walk stays stilled. One dead over the fryer is found by the body a
  block up.
- **`TheList.click`** and **`TheListBody.punched`**: a hand or a blow on the fryer asks `TheListFryer.hand` first.
- **`TheListPeople`**: Jenna's `CORNER`, the stand's far corner by the back wall; `backAway` (she walks there) and `cornered` (there,
  she crouches facing the fryer), each tick of a hunt that has used the fryer (`TheListHunt.fryerUsed`), the town's own pass leaving
  her there; `place` gets up whoever the hunt had down when it puts them back.
- **Client**: the two poses (`LiteraryRenderers.CastModel.hunted`): in the oil, bent forward over the vat, the head down in it and
  twisting, the arms out to the fryer's edges and pushing, the legs scrabbling; still, bent over it, one arm hanging down its side
  and the other lying back across its top, the head down in the oil. The burn (`TheListMarks`, `burn.png`).
- **Sounds**: `the_list.fryer` (hot oil roars and spits) and `the_list.burner` (the burners catch with a soft thump), subtitled.
- **Art** (`tools/draw_the_list_art.py`): the fryer's sides, front with its knob and pilot (lit and unlit), its top in six states
  (the oil cold, shimmering or smoking, clean or dark), the baskets, the handle and the flame under it; six models; the burn over the
  five's skins, across the face: raw and wet in places, blistered pale in others, the eyes gone milky, the mouth open, down the sides
  of the head and under the chin.

## Tests

`TheListTests.theFryer` (new, batch `the_list_fryer`): the fryer laid off in the stand. Another reader lights it: told so, the
burners heard; it comes up hot on its own, and the reader by it is told and sees it bubble; put out again. Deek taken from behind on
the home bench and carried into the stand, held near the fryer. Cold, a hand on it turns the knob and he is still held; warming, a
hand on it tells the slasher the oil is not hot yet. Hot, a hand on it: into the oil, the slasher's cue, his words, bent over it
facing into it where one forced into it stands, the burn's mark, Jenna's shout, still held with no retreat and no walk; the other
reader hears the oil and him. A hand on him from the other reader: he is held down in the oil. Twenty ticks on, the oil has had him
a while and smokes; put out, and twenty ticks on the count has stopped; lit and hot again, he goes still over it, the oil gone dark,
the slasher told, the other reader told a scream stopped, the kill kept by the slasher, the hold let go, a hand on him after and he
does not move; Jenna has backed into the stand's corner and got down. The other reader, looking, finds him over the fryer. Ended:
the fryer laid off and clean, Deek back and unmarked, Jenna standing at her window.

## Seen in the frames

Eye-level frames of the stand with the fryer off and hot, Deek in it and dead over it: from the door end of the stand, close over
the fryer's corner, from behind where the slasher stands, from the west wall beside it, and from the middle of the stand.

- **The fryer** reads in the stand: steel against the front wall under the order windows, two vats with one basket hung on the
  splash and one over the oil, the knob plate and the grille over the burners. Lit, the flame shows through the grille; hot, the oil
  in the vats is lighter and broken with bubbles.
- **In the oil**, from behind he is bent over it from the hips with the head down out of sight in the west vat and the arms out to
  its edges; from the side the body lies level over it and the legs stand behind.
- **Found and fixed**: Jenna's place is the order window beside the fryer, so in the first frames she stood at his elbow while he
  was held under. She now backs away into the stand's far corner and gets down there, and the frames show her there with her hands
  to her head. Dead, his arms first stuck straight out past the fryer's edges into the air; now one hangs down its side and the
  other lies back across its top. A frame through the order window from the lot showed only the sill and the back wall, the fryer
  being below it, and was dropped.
- Particles (the bubbles and the smoke) are not drawn by the review tool; the case checks they are sent.

## Every new line

Speech, over the speaker's head as their name, forced into the oil:

- Deek: "Not the grease! Not the grease!"
- Wes: "It's on! It's on, it's on!"
- Mel: "Please. My face. Not my face."
- Lesley: "Turn it off! Somebody turn it off!"
- Shannon: "Jenna, help me! Jenna!"

Jenna, in the stand: "Let go of %s! Let go!"

Shannon, coming across one of the others over the fryer (the others keep their lines from 4a-3): "That's %s. That's %s in the fryer."

Grey cues on the action bar, to the slasher:

- "The oil isn't hot yet."
- "You force %s down over the fryer and push the face into the oil."
- "%s goes still. The oil goes on spitting."

To anyone else whose hand is on one in the oil: "%s is held down in the oil."

To whoever turns the knob: "The burners catch under the oil." and "The burners go out."

To anyone near when it comes up to heat: "The fryer's oil starts to smoke."

To a reader who is not the slasher, coming across them: "%s is bent over the fryer, face down in the oil."

Describing the fryer (a look at it): "The fryer, off. Two baskets hang over cold oil.", "The fryer's on. The oil is warming.", "The
fryer. The oil shimmers and smokes.", "The fryer, off. The oil has gone dark.", "The fryer's on. The dark oil is warming." and "The
fryer. The dark oil spits and smokes."

Subtitles: "Hot oil roars", "Burners catch".
