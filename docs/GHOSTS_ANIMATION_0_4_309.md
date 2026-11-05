# The Ghosts Set: a living family (0.4.309)

The owner: "please focus on improving the animations for the ghosts set vignette."

## What was wrong

Looking at the cast as a viewer, before any code:
- The family at dinner sat in one identical pose, arms straight out onto the table.
- Almost every pose was a frozen hold, so the living family looked as still as the dead one on the seventh day.
- That flattened the story's central image. Marjorie's stillness at the table is what is wrong in the room, and it can only read as wrong
  if everyone around her is visibly alive.
- Lines appeared over still mouths.
- The fall was a held star, the landing never settled, and the wall was a still.

## What changed (all in the one shared cast model, so the scene and the native proofs agree)

- **Each sits as themselves.**
  - John sits back, a hand on his thigh and his other forearm along the table.
  - Sarah leans in, her hands drawn together toward the middle.
  - Merry has her hands in her lap, shoulders up, chin down.
  - Marjorie sits bolt upright, both arms straight out and flat, exactly alike.
- **The living layer.** Every living pose breathes at the person's own rate: Merry fastest, John slowest, faster under strain (crouching,
  the wall, helping, flinching, choking, wary). The chest and shoulders rise and the head goes with them. The head drifts and now and then
  glances aside, except where the pose has the head doing something (the hand, the stare, grace, eating, choking, slumping, the leap) or
  the person is walking.
- **Marjorie at dinner is the exception.** From the second day, seated or staring, she does not breathe, drift or glance, and blinks about
  a third as often. The dead do none of it either.
- **Talking.** When a new line goes up over someone, they talk for about as long as the line takes to say:
  - the jaw in syllables;
  - a small nod;
  - John, Sarah, Barry and the priest move a hand while standing or seated, and keep still while walking.
- **Motion where the story moves:**
  - the fall flails arms and legs;
  - the impact lands hard and settles over about a third of a second;
  - on the wall the grip shifts hand to hand, with a tremor;
  - a flinch trembles and eases;
  - helping hands reach.
- **Timing by weight.** A flinch, a fall or a landing blends in three ticks. Sitting, slumping, grace, wariness and death take fourteen.
  Everything else keeps eight; the leap keeps five.

## Owner follow-ups

- **Mouths that show who is speaking.**
  - The cast speak in the action bar with their name tags hidden, so nothing on the bodies said who was talking.
  - Each person now carries a synced speaking timer (`LiteraryActor.speaks`), set whenever one of their lines goes up: the scripted takes,
    talking to someone, the errands, and the lines between takes. It is sized to the line.
  - While it runs the jaw drops on each syllable, with pauses between phrases, wide enough to see across the table, with a nod and a slight
    turn.
  - Marjorie's day-two dinner lines are the exception: they stay a voice from somewhere, while her mouth hangs slowly open.
- **Day one at the table, the parents eat and drink.** John and Sarah each, on their own slow cycle (about every twenty-six and thirty
  seconds, never in step, never mid-line), lift a hand to the mouth, tip the head back a little for a sip, then lower it while they chew.
- **Faces that stay.**
  - A change of feeling arrives over two and a half seconds, not one.
  - The small passing looks between lines ease in, hold for several seconds, and ease out, and come about half as often, so each one reads
    as a reaction.

## The filmstrips, and what they changed

`GhostsFilmstrip` (`gradle runClient -PghostsFilmstrip`) renders motion as images, using the very model the scene uses. Each row is one person
in one pose, each column the same pose a few ticks later; it writes build/font-smoke/ghosts-film-*.png. Pages:
- strain: fall, landing, wall, flinch, helping;
- story beats: crawl, the slide off the chair, leap, choking;
- speaking faces against a silent one;
- the day-one table;
- the four walks;
- standing idle, with Marjorie at dinner beside John.

The first strips showed:
- what read: the fall, the landing, the sips, choking, and faintly the mouths;
- what did not: the wall (a tremor on a held pose), the slide (static), standing (dead straight), the flinch and helping (too subtle),
  the crawl (rigid), the mouths (too small).

Changed:
- **The wall** is a hand-over-hand climb: arms trade high and low, the opposite knee drives up, the head follows the reaching hand,
  slow and deliberate.
- **The slide off the chair** sinks and tips back over about two seconds, boneless, the head lolling.
- **Standing** shifts the weight from leg to leg with a knee easing, the arms a little bent and swaying.
- **The flinch** jolts back with the arms coming up before the tremble.
- **Helping** hands grab upward in turn.
- **Marjorie's crawl** cocks her head over and bobs with each placed hand.
- **Mouths** open wide enough on each syllable to tell from the strip who is talking.
- **Each walks as themselves**, scaling the shared gait:
  - John heavier, leaning in, arms swinging less;
  - Sarah brisk, arms close;
  - Merry quick, arms swinging wide;
  - Marjorie wrong: arms all but still, head level, leaning back as if carried.

The second strips show each of these.

## Verification

- The native visual proof was rendered and the seating view inspected.
- Day one in a real client: John's posture, his head moving between frames, and his hand coming up both for a sip and as a gesture while
  his line was on screen were seen.
- The mouths are seen front-on in the speaking filmstrip, open on syllables against a shut silent face; not yet front-on in the scene.
- All 27 Ghosts cases pass locally.
- Pending CI.
