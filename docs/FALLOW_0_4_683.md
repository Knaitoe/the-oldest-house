# Fallow 5a: the road (0.4.683)

The first half of the fifth part of *Fallow* (`docs/FALLOW_PLAN.md`). Part 4 (0.4.681, 0.4.682) was the cellar after the light and the ash
outside. This half is the road: the readers the light found out in it coming back, their shadows, Opal's bean in the ground, and the man
who comes down the road out of the ash. What he asks, what each reader tells him, the account and the Witness source are part 5b.

Fallow is still not a Witness source.

## What happens

1. **Held for the ones outside.** A reader the light finds out in it is sent back into the House (part 3). The station is now held for
   them: after the light, while anyone it found outside has not come back, a station with nobody at it is kept for up to five minutes
   (`FallowRoad.RETURN_HOLD`, 6000 ticks), its clock standing still while it is empty, as always. Where more than one hold applies (the
   buried days' grace for a reader below who stepped out, part 4), the longest is kept. Each afternoon has its own mark, and only a reader
   who was there at that afternoon's light is dealt it afterward; a reader from an earlier afternoon is not.
2. **Coming back.** A reader who was out in it at the light, at the station again however they came (the way back through the House's door
   leads to it), is told once that the store has no roof now. While the light's faint is still taking them out they are not yet back,
   and find nothing.
3. **Their shadow.** Any reader near the store's front who looks at a shadow on it, or puts a hand to the wall where one lies, finds it:
   their own (kept), another reader's (named, and counted), or Boyd's, an arm up toward the rig. A hand on one's own shadow a second time
   says it does not come off.
4. **The girl's bean.** Once the five are up and Opal has sat by the steps a while, she kneels and sets her bean down in its cup in the
   yard beside her, and sits by it. While she sits with it and anyone is at the station, the ground round her comes up a ring at a time,
   out to twenty blocks: shoots up through the yard's dirt and through the ash, the ash going back to soil under them, and three rings
   behind the edge the shoots grow into young wheat. Nothing comes up where a body stands, on the floors, the doors, the road or the
   rubble. A reader near enough to see it is told once; at the fourth ring Opal says so.
5. **The man on the road.** Thirty seconds after the bean is in, a man is made on the track to the far pad, in the ash, where nobody at the
   station has him in view, and walks slowly down the track to the road, straight across it between two of the fallen poles, along the
   apron's far edge north of the rig and the canopy's fall, and in through the yard's gate, where he stops west of the cellar doors and
   stands looking at them. He is the man who bought a soda that afternoon. Each reader who first sees him is told someone is coming down
   the road.
6. **Opal taken down.** When a reader first sees him, Faye (or Otis, when Faye is not up) takes Opal down the cellar steps and out of his
   sight. Her bean stays in the ground; with her gone, the green stops where it reached.
7. **His faces.** Each time a reader has seen him and he is then out of everyone's sight, his face changes: his own, then Harlan's, then
   Boyd's, then Otis's, and last the face of whoever is looking at him, drawn on each viewer's screen from their own body (the House's, or
   their own skin when its bodies are off), so that each reader sees themselves. Each reader is told whose face it is the first time they
   see each one, and keeps which they saw.
8. **Spoken to,** he does not answer yet; he looks past the reader at the cellar doors.
9. **Put back.** A fresh afternoon takes the bean up out of the yard and puts the field back as sown (part 4b's mend covers the shoots and
   the young wheat, which are stages of the field's own wheat; where they came up in the yard, the yard is left bare). A bean left in a yard
   that was not loaded when its afternoon ended is taken up during the next, before the light, while nobody is looking.

## Single and multiplayer

- The green, the man, his walk and his faces are one, for everyone. His face changes only while nobody at the station has him in view,
  spectators counted; the last face is each viewer's own.
- Coming back, the shadows, the green and the faces seen are each reader's own record. Spectators change nothing and keep nothing, but
  count as watchers when his face changes (they are left out of the House's ordinary sight check, so the road has its own).
- One reader alone: out at the light, they are sent back and the station waits for them; below at the light, they dig out with Otis (part
  4) and the man comes to them.

## On the reader's screen

- The field's wheat has two new stages, drawn at 16 px: shoots (`wheat_4`: thin blades in tufts, darker at their feet, the oldest with
  its first leaf opening) and young wheat (`wheat_5`: knee-high, soft stalks with their leaves out, small heads still in the leaf).
- His borrowed faces are the cast's own painted skins. The last is drawn on the player's own model with the viewer's skin
  (`LookoutRenderers.Double`, which now walks for him and holds nothing up).

## Made as

- `FallowRoad` (new): the hold, coming back, the shadows found, the bean set down, the green, the man on the road and his sight check,
  Opal taken down, the faces, his silence, and the bean taken up for a fresh afternoon.
- `FallowStrike`: State keeps who was outside and who is back, the shadows each reader found, the bean and the green, the man and his face;
  the light stamps each reader with the afternoon's mark; `FallowRoad.tick` after the light; a word to the man goes to `FallowRoad.talk`;
  a fresh afternoon takes the bean up.
- `Fallow`: each afternoon's mark, dealing by it after the light, the longest hold, the man's borrowed faces for the client, and a hand on
  the store's front.
- `FallowBuried.up`: when Opal sits by the steps.
- `FallowBlock`: the wheat's `SHOOT` and `GREEN` stages and their shapes. `FallowAsh.mendField`: the yard left bare.
- Client: `LiteraryRenderers` (borrowed faces, the viewer's own), `LookoutRenderers.DoubleModel` (walks for him).
- `tools/draw_fallow_art.py`: the shoots and the young wheat, with checks.
- `FallowTests.theRoad` (new); the strike case now brings its outside reader back before the afternoon can end.

## Every new line

Flagged for the owner. Nothing here names a colour or an age or says how the reader feels.

**Cues** (`FallowTexts`):
- Back: "The store has no roof now. Over it the sky is ash."
- Their shadow: "Your shadow is on the store's front, where you stood when the light came."
- A hand on it again: "It does not come off on your hand."
- Another's: "%s's shadow is on the wall, where the light found them."
- Boyd's: "%s's shadow is on the wall, an arm up toward the rig."
- The green: "Around where the girl sits, something is coming up through the ash."
- The man: "Somebody is walking down the road out of the ash, from where the pads were."
- His faces: "The man has %s's face." / "The man has your face."
- Spoken to: "He looks past you, at the cellar doors."

**Said:**
- Opal, kneeling: "It can go here." Sitting by it: "If I sit with it, it'll grow." At the fourth ring: "Look. It's all coming up."
- Faye, taking her down: "Opal. Down the steps. Now." Otis, if Faye is not up: "Down you go, sweetheart. Quiet as you can."

**A hand on them:** shoots: "Shoots, up through the ash." Young wheat: "Young wheat, its heads still soft. It came up after the light."
