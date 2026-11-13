# The cast checks, a second round (0.4.561)

The owner, 13 November: "You should standardize your tests with Tess and run them against the dialogue heavy, branching vignettes
too. Many of those NPCs also did not behave properly." 0.4.560 wrote the checks (docs/CULVERT_0_4_560.md) and ran them over sixteen
talking scenes. This round adds two more and fixes what the checks found when they were run again and again.

New builds only. No layout, protocol or Witness change.

## Two more scenes

`CastConductTests` now has eighteen cases. The two new ones:

- **The Ghosts set**: the crew and the family, on a filming day. They kept every check from the first run.
- **The Confession**: the visitor at the table.

## What the checks found

- **The Confession's visitor nagged.** Until the reader laid the clean book down, he said "There is a clean book on the low shelf.
  Put it between us." every five seconds. He now says it once, reminds the reader twice more, a long while apart and in fewer words
  ("The book on the low shelf. Between us." half a minute later, "The low shelf." a minute after that), and then only waits. Each
  visit starts over (`LiteraryVignettes.BOOK_ASKS`).
- **A Place for You asked the same favour word for word.** Each time the reader came back to one of the four, they asked the same
  thing in the same words ("Help me make the tea? I can never find anything."). The second time it comes in other words ("The tea,
  then? I still can't find the pot."), and the two take turns after that. "In a minute" gets "Whenever you like" the first time and
  "It'll keep" after.
- **Ren arrived at the counter in plain sight.** Once the reader had agreed to make the tea, Ren went to the counter, five blocks in
  a tick. The house puts someone straight onto their mark only when the reader can see neither them nor the mark, but it looked at the
  mark at head height only, and the reader could see the lower half of it. It now looks from the legs up to the head, as the checks
  do; otherwise Ren walks there.
- **The Drawing's late villager answered over his shoulder.** Spoken to while walking into the square, he answered and walked on,
  his head turned as far as a head turns and still short of the reader. A villager walking somewhere now stops to answer, for up to
  three seconds a leg, then goes on (`LiteraryActor.listening`, `Drawing.walk`). This is the Drawing's own: nothing else stops for
  being spoken to, so a pursuer clicked in the middle of a chase keeps coming.

A turn fault now says how far off the reader was and which way the speaker meant to look, and the speaker's own report says what
they aimed at before their seat held them back, so a fault can be read without running the case again.

## Local runs

- The cast namespace, eighteen of eighteen, twice over, with no fault from the checks in any scene. The faults above came and went
  between runs (each run's test reader reaches people at slightly different moments): the Drawing's and Ren's were each seen in one
  run of three before their fixes, and neither in the two runs after.
- The scenes' own cases: A Place for You's three, the Drawing's three and the Confession's three, nine of nine.
- The quick checks and the structure halves check.

## Run 1595 (0.4.560)

Two jobs failed on 0.4.560, both from its own changes:

- **second-person-gameplay**: the autumn evening ended as "interfered" while the case's reader stood in the window to be seen. The
  evening counted the kitchen door as the reader's doing whenever it stood otherwise than at the start of the evening; the supper table
  is in the kitchen, and since 0.4.560 Sam walks in to it through the kitchen door rather than appearing in his chair (he may be put
  somewhere only where the reader sees neither him nor the place). Now the reader's own hand on that door during the evening is what
  counts (`EveDoorBanged`, set in `SecondPerson.click`).
- **goatman-gameplay** (the wake cases): Harrigan dead in his chair turned his head to the reader who clicked him. The cast-wide turn
  left out the dead, but his body is alive to the game. Harrigan dead or in his casket no longer turns to anyone (`addresses`).

- **live-multiplayer** stopped at the trailer supper (phase 44): "the actual child's ray missed the native table". The proof puts
  both readers at their plates and then lets them into the evening; since 0.4.558 the second reader in takes a cousin's place and
  begins where that cousin is, at the camp, so the second client aimed at its plate from beside the fire and its ray met nothing. The
  proof now puts both back at their plates once they are in (`SupperSocketProof.begin`). Earlier runs since 0.4.558 had stopped
  before the supper and never reached it.

The phases after the supper seat the first reader in their canoe in the culvert's mouth before Tess has come, and wait for her to be
in it. Before 0.4.560 she was put straight into the canoe; now she has to get there. A new lake house case,
`tessSwimsOutToACanoeAlreadyUnderTheRoof`, seats a reader there the same way: she comes down the dock, steps down from its end,
swims the lake and the open trough and climbs in from beside the canoe, and the cast checks hold the whole way.

Locally: the Second Person cases four of four, the wake namespace with them, and the new lake house case.
