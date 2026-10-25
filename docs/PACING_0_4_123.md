# Goatman pacing and the Confession, 0.4.123

The owner, October 25. Goatman: the sequence still drags. There should be a moment of idle chatter while the player and
the cousins eat, then a knock at the door, "let me in", which the player may open or not; then the rapid knocking and the
circuit round the windows start, with the Goatman's words playing as it circles and tied to its knocks at the door.
Confession: "What name did you come here under?" and then it stalls; it does not progress.

Built on 062f6a3e, the common base of the 0.4.121 and 0.4.122 lines, so it merges into either. No layout, protocol or
Witness change: 52 sources / 39 resolutions / two kinds / three endings.

## Goatman

New rounds carry `Brisk04123` (set in `GoatmanVignette.newRun`); rounds saved before it keep the evening they began. The
0.4.118 and 0.4.121 rules all stand: the cousin goes for gas the moment the last frank is plated, words come before
blows, two laps of six surfaces, three front-door blows, a lone knock every twenty seconds after the words, the sash
crawl and the cousins drawing back, and lines checked against the moment. Both endings keep their rules: the cousin must
be let in, the front door and the bathroom window must stay shut through the night, and opening the door at night takes
the opener.

### The evening (gathering clock)

- 120 the cooler hint, 220 a line about the long story, cooking talk from 420 while the player cooks.
- The first frank on a plate calls everyone in ("Food's coming. Everybody inside."), opening the door; cousins jog to
  their seats. The clock still waits at 599 until every place is plated; supper then begins at once
  (`SupperAt04123`) and the cousin leaves for gas, running, out of sight at the trail's bend. The one who doesn't
  belong comes in to take his chair once he is out past the porch, so the two never meet in one seat.
- Counted from supper: +30 and +80 talk while they eat (the second is the bathroom-window hint); **+130** a cousin pulls
  the door to (once every cousin at the table is in and nobody stands in the doorway, and only before he is heard);
  +170 "Save his place. He'll be back." while the impostor sits in it; **+210** he is heard at the bend ("Wait up! Don't
  lock it!") and runs to the porch in about seven seconds.
- At the shut door he says his line and knocks twice, every five seconds (0.4.121). The reader chooses:
  - **Open**: the existing doorway exchange; the disguised original runs out past him as he comes in, and the questions
    start ("Who just ran out, then?"). Two seconds later the woods go quiet and the porch light goes out; two seconds
    after that night falls and the door is locked.
  - **Leave it**: after his three lines (fifteen seconds) his voice stops mid-word ("Let me—") and he is gone; the
    quiet comes at once and night two seconds later. The impostor is still at the table; it leaves its seat for the
    porch step the first moment no camera (spectators included) is turned toward it or the step, and its empty seat is
    noticed.
- A safety net brings the quiet 70 seconds after supper if he never got away to come back.
- No separate evening lap, and no private tearing lap on a brisk round: the one circuit is the night's, with its words.

### The night (vigil clock)

- The thing speaks at once: the first word at tick 20, then one every 130 ticks (`GoatmanStalking.BRISK_DEMANDS`), all
  seven in order across both laps. Words still come first, but each is followed straight away by its own run of knocks
  on the door: three raps for "let me in", one blow to each word of "let. me. in", short runs after that and a fast run
  under the last (about thirty blows in all). Each word holds the centre of the screen 70 ticks (the last 110), so the
  cousins' own lines fit between them; an interrupted cousin line no longer pushes the next one back.
- After the words: the first lone knock three seconds after the last, then one every twenty seconds.
- After the last lap: the three front-step blows, then straight to the bathroom window (no 1900-tick wait). Found shut,
  morning comes three seconds later; open, it gets in as before, and the attempt keeps its full ten seconds.
- The shortest whole vigil is 900 ticks. A brisk night is about a minute; supper to the knock about twenty seconds.

## Confession

The 0.4.121 listener dictated one sentence, asked his question, and once it was answered (or thirty seconds passed) he
stopped for the rest of the visit with the question still on his name tag; the next chapter waited for a return, and
nothing said so. Eight sentences took eight visits, which read as a stall. Now his tag shows the fragment he writes
down, he rests three seconds (ordinary chat is the reader's own again) and dictates the next chapter in the same sitting.
Leaving still resumes where he stopped, and a visit already closed under the older rule keeps waiting for the return. The
journal, the sealing, the seam in the wall and the resolution on the next return are unchanged.

## Tests

No new cases. Rewritten for the brisk round: the doorway exchange (night within 83 ticks of the crossing, the first word
within 23 ticks of nightfall, all five panes struck by the original on the night circuit while it speaks), the confusion
case (the actual knock, a native door use, the exchange's questions through reload, the return reaction under the
night's words), the fear night (stages by demand, not old clock values), the obstruction case (brisk vigil and window
timings), the finished camper (exchange, door locked at nightfall, all panes and both forms at night, dawn and
Survival), the plating cases (brisk supper gate) and the live two-client proof's trailer setup (an explicit, slightly
longer quiet so both clients capture the confused cousins). The Confession quill case answers all eight questions in
one sitting, leaving and returning once between chapters. Legacy-fixture cases (`Fixture.evening`/`night`) still
exercise the older rounds.

Validation pending: the native build cannot run in this session (the NeoForge maven is not reachable), so all twelve
exact-source CI jobs remain required.
