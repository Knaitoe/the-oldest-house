# Third Floor Girls' — 0.4.173

The owner's Bloody Mary vignette, from their design draft ("Third Floor Girls'", working id `dare-in-the-dark`), and one note on the bent-neck woman.

## The bent-neck woman

> "Try to ensure her head is tilted back, so her attention is towards the player"

Her head used to hang bowed at a fixed angle, so she looked at the floor. Now:

- **Her aim.** Her face follows whoever is looking at her. She turns her head up to seventy degrees from her body, and lifts or lowers it to the viewer's eyes.
- **The tip.** The tip to one side is applied under that aim, so her eyes stay on the reader whichever way she turns.
- **The proof.** The uncanny proof's stoop page now poses her for a reader in front. In the side views she is turned to them.

## Third Floor Girls'

A school's third-floor hall at night, and the girls' bathroom off it. It is a new literary scene, `dare_in_the_dark`, at slot 113. It is private: a reader takes the dare alone, in a copy of their own.

### The school

**The hall.** It runs north from the entry, 27 blocks long, between painted block walls with a darker band to shoulder height. Down one side are lockers, broken by classroom doors and a trophy case. The other side has two more classrooms, a bulletin board, a clock, a drinking fountain, a poster, and a brown plate marked GIRLS over a heavy wooden door. Fluorescent panels light the hall.

**The kids.** Two kids under bedsheets stand either side of the bathroom door. At the far end is the stairwell door, the way on. It stays shut until the dare is done.

- **The way down the hall.** When the reader goes past the kids, they say: "Not that way. In there."
- **The entry.** The school's entry behind the reader is always a way back.
- **Their bodies.** The kids fidget while they wait, shake while they giggle and, from the second count, stand perfectly still.
- **Talking to them.** Talking to them gets "Go on, then.", "Shut it all the way." or "You have to look at your eyes."

**The candle.** At the door the kids hand over a candle stub ("You hold the candle. That's the rule."). Held in either hand, it lights a small warm circle round the reader's face. It is taken back when the reader leaves the third floor.

**The note.** A folded notebook square lies on the floor by the door. It is the owner's note word for word, in a child's hand in purple gel pen. Reading it once gives a grey hint: "Shut the door, stand on the mat with the candle, and keep looking at your own eyes in the glass."

### The bathroom

The bathroom is behind its own glazed wall: hex tile, a low ceiling and a dead light fitting. The only window is frosted and black with night.

- **The sinks and glass.** Four sinks hang on the far wall under one wide mirror, four blocks across and two high. A worn mat lies before the middle two.
- **The stalls.** They stand along the opposite wall, behind the reader as they face the glass. The first one is shut and the others stand open. In the glass, the shut stall is over the reader's left shoulder.
- **The door.** Shut all the way, the heavy door latches with a click and lets no light from the hall through. Only the candle is left.
- **Outside.** On the latch the kids whisper, shuffle and laugh, and one says "Go on, then."
- **No music.** No music plays on the third floor.

### Holding the gaze

The dare is three counts, said by holding the reader's own eyes in the glass. It asks for four things at once:

1. the door latched;
2. the reader on the mat;
3. the candle in hand;
4. the reader looking into their own reflected eyes, which in a flat mirror is straight along the glass.

A little movement is forgiven: up to fourteen degrees to either side and twelve up or down, and half a second of a glance. Each of the following starts the count again:

- looking away;
- looking down at the sink or at their shoes;
- stepping off the mat;
- putting the candle down;
- opening the door.

The counts are said at about three, sixteen and twenty-nine seconds of steady looking, with the door after them, about forty seconds in all. The reader whispers "Bloody Mary…" twice, then says "Bloody Mary." Once the third is said, looking away does not undo it.

### The three counts

The reflection is the reader's own House appearance throughout: their skin, the House's clothes and hair, and the read's glasses. Only these change, and only in the glass.

| Count | In the glass | Outside |
| --- | --- | --- |
| First | One of the reflection's eyes moves, then the other, and its mouth fades away. | They giggle, and one says "That's one." |
| Second | The mouth comes back wrong: lower, wider, its corners dragged down. The first stall stands a few inches ajar, though the real one is still shut. Turning round to look is looking away, and starts the count again. | Silence. No footsteps. |
| Third | The reflection's candle is gone and its arms hang at its sides. A second flame rises behind its left ear. A woman stands right behind it, chin on its shoulder, wet hair in strands, fingers pressed into its collarbones, her head tilted back so she looks at the reader. She is only in the glass. | Silence. |

**The face.** The changes are made from the reader's own skin: the face is read off the graphics card, as the House's body check already does, and only the reader's own pixels move. It is never a stock scary face.

**Early door.** Opening the door before the third count ends that attempt ("That didn't count. You came straight back out."). The reader can shut it and try again.

### The handle

After the third count the handle will not turn: "The handle won't turn. Someone is holding it from the hall."

- **Trying again** rattles the handle but does not restart the wait.
- **The breathing.** Loud breathing comes at the back of the reader's neck.
- **Three seconds** after the first try, the door is thrown open.

The kids are gone, and two crumpled sheets lie where they stood. As the reader crosses back into the hall, the mirror shatters behind them; the camera is never turned. Then:

- the stairwell door opens;
- the account is ready on the first sheet.

### Finishing

The dare is complete only with the full three counts, the handle and the crossing.

- **The account.** Reading it, after the note, resolves the reader's Witness source. It says what they did: whether they read the note, how many times they lost the count, and whether they opened the door early.
- **The Witness line.** "I shut the door until the latch clicked and said it three times to my own eyes. The handle held from the hall. When it gave, the sheets lay empty on the floor and the glass broke behind me." The title is "The third count", of the survival kind.
- **The rule kept.** The account, like all House writing, never reads the reader's gender, colour or age.
- **Leaving early.** A reader who leaves before crossing back out, even after the door was thrown open, starts the dare from the beginning on their next visit: the sheets are picked up and the kids are there again.

## How the glass works

The coatroom's true plane mirror (`HouseReadReflection`) now takes any width of glass and a hook. The bathroom's hook (`DareReflection`) does four things:

- shows the shut stall ajar, and draws the room again when that changes;
- empties the reader's hands while their reflection is drawn at the third count;
- lays the reader's changed face over their reflected head, with the face and hat layers each just proud of their own;
- draws the woman and the flame behind the reader, reflected with everything else.

The client is told only the count, whether the reader is on the third floor, and whether the handle is held (`DareStatePayload`).

## Art and sound

**The art.** It is drawn in native pixels by `tools/draw_dare_in_the_dark.py`, which also checks itself in CI:

- 60 textures:
  - seven materials;
  - the fittings: lockers, classroom doors, trophy case, boards, poster, clock, fountain, lights, GIRLS plate, sinks, partitions, the stall door shut and ajar, toilets, mat, towels, bin, window, note and sheets;
  - the mirror, whole and cracked;
  - both doors;
  - the candle stub;
  - the two kids' sheets, the woman and the flame;
- the models and block states for all of them.

**The sounds.** There are nine subtitled sounds, reusing existing recordings: giggling, whispering, shuffling, the latch, the handle, the breathing, the door thrown open, the mirror breaking and the flame catching.

## Run 1338

Run 1338 tested 3f0f5e8.

- **The elk drag.** The elk drag case looked up the killer after the blackout. By then, on a loaded runner, he had walked the drag line out of the held chunks, and the exception stopped the literary suite. The case now holds the ridge, the drag and the cave, and a missing body fails its assertion instead.
- **The wardrobe rail.** The wardrobe case reached for the outfit rail from the middle of the room. The outfits hang in a shuffled order, so on some runs the t-shirt and jeans were just out of reach. It now steps up to the rail.
- **The canoes.** The full suite's canoe-lending case looked for the two canoes before the dock's chunks were loaded, and found none. It now waits for them, like the other lake cases, and its fixture closes after the batch.
- **The barn well's view.** Since 0.4.164 the farm has been built whole, outside the generic dressing, so the architecture case stopped exporting it. The client proof found 104 views and stopped. The case now exports the farm itself. No build job had passed since then, so this was never reported on its own.

## Tests

- **Four native dare cases** (`DareTests`, in the literary job).
  1. **The room.** The room is built with its glass, sinks, mat, shut stall, light-tight door and lit hall. The note keeps every character of the owner's words, in purple. The account follows what the reader did and never reads the reader.
  2. **The count.** The kids hand over the candle at the door. Nothing is counted with the door open. The latch, the first count, the giggling and the spoken line all come in order. Looking away loses the count. A small wander is forgiven. Looking down, stepping off the mat and putting the candle down each stop it.
  3. **The handle.** The third count holds after looking away. The handle is held, and trying again does not restart it. The door is thrown open and the kids are gone, their sheets left behind. The glass breaks only as the reader crosses. The stairwell door opens. Reading the account resolves the source. The candle is handed back on leaving.
  4. **The early door.** Opening the door before the third fails that attempt, with the kids' line, and it can be tried again. A later visit after the door gave, but before crossing, starts the dare over with the sheets picked up.
- **The three run-1338 cases** (elk drag, wardrobe rail, canoes), as above.
- **Client proofs.**
  - A new dare art proof (`-PdareSmoke`, in the silo job) shows four pages: the kids in their three phases, every fitting state and both doors, the reader's face at each count, and the third count as the glass shows it.
  - The architecture proof now expects 106 views, the third-floor bathroom's included.
  - The uncanny proof shows the bent-neck woman's lifted head.
- **The architecture case.** In place of a rug, it checks the third floor's laid hall tile and the mat before the glass.
- **The Witness count case.** It now expects 33 literary sources and 55 resolutions.

## Versions

- **Protocol 98.** The dare's state payload.
- **Layout 51.** The new slot is carved in saved worlds.
- **Witness.** 55 sources, 42 required, two kinds, three endings.
