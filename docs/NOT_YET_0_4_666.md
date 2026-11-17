# Not Yet 4: the drawer (0.4.666)

The fourth and last part of *Not Yet* (`docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`). Part 1 (`docs/NOT_YET_0_4_663.md`) built the
ward, part 2 (`docs/NOT_YET_0_4_664.md`) made it refuse a reader's death, and part 3 (`docs/NOT_YET_0_4_665.md`) gave it the trial.
This part gives a reader who stayed down what they came for: a made cot, an account in the drawer under it, and a place in the Witness
pool. The pool goes from 91 sources to 92. The quota stays 69 (three quarters of 92, rounded up, is 69), across at least two kinds.

## What happens

### The made cot

A reader who stays down to the end of a trial is ready. The quiet comes, the far door opens, as in part 3, and their own cot is
**made**: blanket drawn tight and squared, the way the long room's three lie. It stays made while they are up and about in the ward,
for the rest of the visit. Only their cot is made; another reader's cot is theirs to stay down on.

### The drawer

Clicking their own made cot (head or foot) opens the drawer under it. The first time a visit, the drawer slides out on its own with a
little wooden scrape, the screens say **NOT YOU. NOT NOW.**, and the narration says *The drawer under your cot slides out a little way
on its own. There is a book in it.* The book is the account.

The account answers the chart at the ward's foot, the first paper. A reader who has read the chart and reads the account to its last
page has resolved *Not Yet*: Witness credit, survival kind, outcome `stayed_down`. A reader who has not read the chart is told to read
it first, as everywhere else.

### What the account says

The account is built from what happened to that reader, and only that. In order:

1. **Arrival.** Through the machine to a ward that was waiting for someone to keep; or, for a reader who let the Kept's four go higher
   up, a ward that knew it and said there was only them now. Cots in rows under lamps, a window onto a field where the sun does not
   move.
2. **The fall.** The floor gave on the way in, a fall nobody lives through, a seam. If it mended them again, how many times in all.
3. **The rule.** Which of the long room's three told it (only if one did): it wants you up.
4. **Lying down.** It asked them to lie down and said it would keep them. If they got up before, how many times, and that each next
   time was longer.
5. **What it did.** The coils glowing; then the name it called: the other readers' from the floor at the far end, or the lost
   companion whistled for past the arch, or the reader's own name by the far door.
6. **Its lies.** One sentence for each line it said on the screens, with whether it was so: the door at the end was open (it was not,
   or that time it was); another reader got up and was at the door (they had, or had not); when another really got up, it said so, and
   that was true.
7. **Someone else.** The other reader it hurt where they lay, and told them it would stop if they got up; or, alone, the man from the
   long room on the screen, who said to stay down. If it hurt them for someone else's staying, who it told.
8. **The cold,** if it came, and the blow, if it was dealt: it killed them, and would not let that count either.
9. **Staying down.** The quiet, the lamp lifting, the door. Who else was lying in the ward with them.
10. **Home and the counter.** If the House read their home, what the ward told them on the way in (someone once kept them warm and fed;
    they knew what it was to be kept; they had left every place that ever kept them). The counter over the arch, counting their years,
    in the kind the House chose for them (drums, falling cards or bars of light). It kept counting after they left.
11. **The cot made,** and the book in the drawer.
12. **The machine's page,** in its own capitals: the reader's name in ink, MENDED, GOT UP, STAYED DOWN, NOT YET.

### The long room

To a reader who stayed down, each of the long room's three says one thing more, and says it again after:

- **Ines:** *It went quiet. I heard it from here. I didn't know it could.*
- **Bram:** *The door's open for you. Don't stand in it long. It'll take that as asking.*
- **Odile,** by the House's read of how to address the reader: *Go on, then.* / *Go on, son.* / *Go on, pet.* and *It won't look at you
  again.*

### On the stair

The one from the long room who told the reader the rule stands on the great stair among the reader's company, as the stories' other
people do. A reader who never heard the rule from any of them has Ines.

## Single and multiplayer

- **Alone.** The account is the solo trial: the lost companion by name (or the reader's own name), the open door that was not, the man
  on the screen.
- **Together.** Each reader's account is their own, written from their own trial. Where the machine used them on each other, each
  account names the other: who it called, who it said had got up, who it hurt and who it told could stop it, who really got up, and who
  was lying in the ward at the end. The together line on the Witness page says whether another reader stayed down before them (the far
  door had opened once already) or was still lying under a lamp when the door opened for them. These two lines replace part 1's
  single line (*was in the ward with me, in another cot*), which was registered after them and would have taken their place.
- **Several who stay down** each get their own made cot and their own drawer. A reader's drawer opens only for them.
- **Spectators** get no cot, no drawer and no credit.

## Made as

- **`NotYetTrial.finish`**: a reader who stays down to the end is made ready (`LiteraryVignettes.ready`, `stayed_down`), their cot set
  to the `MADE` stage with a chiseled-bookshelf scrape, and the facts the account needs saved on their record: `HomeRead` (the House's
  read of home), `CounterKind`, `TookFour` (the Kept), and `Beside` (the readers lying down with them, gathered every second of the
  trial). The trial also keeps `Called`, `CalledOthers`, `CalledLost`, `Used`, `HurtFor`, `ScreenMan`, `Cold` and, on each lie, `About`.
- **`NotYet.cotClick`** and **`NotYet.drawer`**: a ready reader's own cot opens the account; anything else goes to part 3's lie-down.
  `LiteraryVignettes` sends Not Yet's cot clicks there first, and the ending-book position for the place is the reader's own cot.
- **`NotYet.reader`** keeps a stayed-down reader's cot made while they are up.
- **`NotYet.talk`**: the three's lines to one who stayed down.
- **`NotYetTexts.account`**, **`DRAWER`**, **`stayed`**.
- **Witness:** `WitnessAccount.Story.NOT_YET` (survival), the account advancement (`account/not_yet`, a white bed, generated by
  `tools/generate_advancements.py`), `CoopAccounts` together lines, and a figure in `StairCompany`.
- **`WitnessPool`** is 92 / 69 / 68.

No new block, texture, sound or protocol: the made cot is the `MADE` stage the cot block has had since part 1.

## Checked

`NotYetTests`, eight cases:

- **`stayingDownToTheEndOpensTheFarDoor`** now also checks that the cot is made and the account ready after staying down; that the cot
  stays made once they are up, and no other cot is; that clicking it opens the drawer, slides it out and says NOT YOU; that the account
  names Moss, says the door was not open, shows the man from the long room and the machine's page (MENDED: TWO), and says nothing about
  getting up or anyone beside them; that reading the chart and then the account to its end resolves the Witness source; and that each
  of the long room's three says their line to one who stayed down.
- **`twoLieDownAndTheMachineUsesEachOnTheOther`** now also checks each reader's account: the one still lying down names the other it
  called, the lie that they had got up (they had not), the hurt, and the true line when they did; the one hurt names who it told could
  stop it.
- `LiteraryTests` counts *Not Yet* among the literary sources and checks its Witness story and kind. `StaircaseAccessTests`,
  `WitnessTests` and `AdvancementTests` follow the pool.

## New lines (for the owner)

Every line below is new in this part.

| Line | When |
| --- | --- |
| The drawer under your cot slides out a little way on its own. There is a book in it. | The first time a visit a ready reader opens their cot's drawer. |
| It went quiet. I heard it from here. I didn't know it could. | Ines, to a reader who stayed down. |
| The door's open for you. Don't stand in it long. It'll take that as asking. | Bram, to a reader who stayed down. |
| Go on, then. It won't look at you again. / Go on, son. … / Go on, pet. … | Odile, to a reader who stayed down, by the House's read of address. |
| I came down through the machine to a ward. It said it had been waiting for someone to keep. | Account, arrival. |
| I came down through the machine to a ward. Further up I had let four of its people go, and it knew. It said there was only me now. | Account, arrival, a reader who let the Kept's four go. |
| There were cots in rows under lamps, and a window onto a field where the sun did not move. | Account, arrival. |
| The floor gave under me on the way in, and I fell further than anyone lives through. I woke on a cot with a seam on me I had not had. | Account, the fall. |
| It did that N times in all. Every time there was another seam, and the screens said NOT YET. / The screens said NOT YET. | Account, the fall. |
| Past the ward, in a long room, three people lay with their eyes open. NAME told me the only rule there is: it wants you up. | Account, the rule. |
| It asked me to lie down, and said it would keep me. I lay down on the cot it had given me. | Account, lying down, never got up. |
| I got up once / twice / N times before I stayed down. Every time, it said that everyone gets up, and the next time was longer. | Account, lying down, got up before. |
| While I lay there it heated the cot under me until the coils glowed, and … | Account, what it did. |
| … it called NAMES from the far end of the ward, low down, as if from the floor. | Account, the name, with other readers. |
| … something past the arch whistled and called NAME, and waited for it / them to come. | Account, the name, a lost companion. |
| … it said my own name by the far door, and then nearer. | Account, the name, nothing lost. |
| The screens said the door at the end was open and nothing was keeping me. It was not. / That time it was. | Account, the lie, alone. |
| The screens said NAME had got up and was standing at the door. NAME had. / NAME had not. | Account, the lie, with another reader. |
| When NAME got up, it told me so. That was true. | Account, the true line. |
| It hurt NAME where they lay, and told me it would stop if I got up. I did not get up. | Account, someone else, together. |
| It showed me the man from the long room on the screen over my cot, with something being done to him, and said it would stop if I got up. He said to stay down. | Account, someone else, alone. |
| It hurt me where I lay, and told NAME that they could stop it. | Account, the one hurt. |
| Then it let the cold in through the grille, and said I could get up and get warm. | Account, the cold. |
| Then it killed me, and would not let that count either. | Account, the blow. |
| I stayed down. Everything went quiet at once. The lamp lifted away from my face, and at the far end of the long room a door slid open. | Account, staying down. |
| NAME was / NAMES were lying in the ward with me. | Account, staying down, together. |
| When I first came into the ward it had told me that someone once kept me warm and fed, and that it could do it for longer. | Account, home read as a haven. |
| When I first came into the ward it had told me that I knew what it was to be kept, and that it did it better. | Account, home read as danger. |
| When I first came into the ward it had told me that I had left every place that ever kept me, and would not leave this one. | Account, a runaway. |
| On the way out I passed under the counter over the arch. It was counting my years, on drums that turned / cards that fell one over another / bars of light. It kept counting after I left. | Account, the counter. |
| My cot was made when I looked back, tight, the way the others were. This was in the drawer under it. | Account. |
| NAME. IN INK. MENDED: N. GOT UP: N. STAYED DOWN. NOT YET. | Account, the machine's page. |
| %s had stayed down before me. The far door had been opened once already, and had shut again. | Witness page, together, another stayed down first. |
| When the far door slid open for me, %s {was} still lying under a lamp in the ward. | Witness page, together, another still lying. |
| Deep in the machine there is a ward where it mends what it breaks. It would not let me die. I lay down and stayed down, whatever it did, until it looked away. | Witness line (and the advancement's first sentence). |
| the one from the long room who told the rule, standing | The figure on the stair (not shown as text in play). |

Advancement: **Not yet**, *Deep in the machine there is a ward where it mends what it breaks.*

## Not Yet, all four parts

| Part | Version | What it added |
| --- | --- | --- |
| 1 | 0.4.663 | The ward, the long room, the three, the chart. |
| 2 | 0.4.664 | Refused deaths, seams, the counter, the home line. |
| 3 | 0.4.665 | The trial: the invitation, the five, the blow, getting up, staying down, the far door. |
| 4 | 0.4.666 | The made cot, the drawer's account, the three's last lines, the stair, the Witness source. |
