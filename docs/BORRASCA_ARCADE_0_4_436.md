# Borrasca: the drugstore's own games (0.4.436)

The owner, of the drugstore: "It should have its own arcade games, don't recycle".

The drugstore's two cabinets stand against its south wall, by the window, facing into the shop. They play two games made for
this town and nothing else. No Polybius game, cabinet, art, payload or score table is used. The one thing kept is the way a score
is trusted (below).

## DEEP SEAM

A mine cart running down a dark seam on three rails.

- **W / S** (or up and down) change rails. **Space** hops the cart over a gap in the rail.
- A **fallen timber** can't be hopped. The cart has to go round it on another rail.
- **Lamps** stand on the rails. Running into one keeps the cart's light up. The light shrinks as the cart goes, and the dark closes
  in round the lamp.
- The cart goes faster the longer it runs.
- Three carts. A gap or a timber costs one.
- The score is the distance run, plus 25 for each lamp.
- Now and then, deep in, something hums under the track.

## BIG CAT

Night fishing off a johnboat for the catfish on the river bottom.

- **A / D** steer the hook. Holding **space** (or up) reels in. Letting go lets the hook sink.
- **Perch** (5) and **bass** (15) come up easily.
- The **big cat** (100) keeps to the bottom and pulls back. Reeled too hard, the line's tension builds until it parts and the fish
  is lost. Let it be and the tension eases while the cat pulls down. A bar in the corner shows the tension.
- **Snapping turtles** take the worm off a bare hook.
- Six worms. Every fish landed, line parted or worm stolen uses one. The game is over when the worms run out, or at dawn
  (three minutes), when the sky has gone grey.

## Playing

- A play costs a quarter from the child's own money, the money the shops take.
- If the child is short and Kelly is beside them, she puts in the rest, as at the counters: "Here, I've got a quarter. I get next
  go."
- With no quarter between them, there is no game.

The rules are `labyrinth.BorrascaArcade`, shared by the server and the screen (`client.BorrascaArcadeScreen`). The screen sends
only the keys it held on every tick, never a score. The server plays the game again from the session's seed and keeps the score it
reaches. A log longer than the session has been open is refused. (Polybius trusts its scores the same way. This code is separate,
with its own payloads: `BorrascaArcadePayload` and `BorrascaArcadeScorePayload`.)

## The table

Each cabinet keeps a table of six, separately in each reader's town. It starts with the town's children's initials:

- **DEEP SEAM:** PAT, KRT, KEN, PRY, KEL, PAM. Patrick's score is at the top.
- **BIG CAT:** PRY, PAT, KEL, KRT, KEN, PAM.

PAT, KEN and KEL are Patrick, Kendra and Kelly. KRT, PRY and PAM are other children of the town. Every initial is a P or a K,
like the babies on the doctor's corkboard (0.4.424), and like the corkboard the table doesn't say why. The table is kept from visit
to visit, so Patrick's score is still at the top after he is gone, unless the reader beat it.

After a game the screen shows the table with the reader's three letters where their score would sit. The cue says whose
initials the reader went in under, or that they went to the top over someone's.

Grown, on the third and fourth visits, the reader finds the cabinets unplugged. Through the dust on the glass the last table still
shows, with whoever is at the top.

The account gains a line about quarters spent at the drugstore, and "I got my initials over Patrick's." for a reader who did.

## Art

`tools/draw_borrasca_town_art.py` draws the cabinets as two new shop-fitting kinds, `CABINET` and `CABINET_TOP` (lower and upper
halves; the stage is the game). The textures are original:

- **Sides:** DEEP SEAM's coal black with a slanting lamp-yellow band; BIG CAT's river green with a band of water.
- **Lower front:** a steel coin door with two lit slots and a kick plate.
- **Control panel:** a stick with a red ball and two buttons.
- **Upper half:** a small glowing picture of each game's screen, under a lit marquee. DEEP SEAM's marquee has a lamp over crossed
  picks; BIG CAT's has a catfish with its whiskers.

The screen half gives off a little light.

## Tests

- `BorrascaTests#theDrugstoreCabinetsPlayTheirOwnGames`:
  - Both cabinets stand in the drugstore, both halves, each with its game.
  - Patrick's initials are at the top of DEEP SEAM's table.
  - A click takes a quarter and opens DEEP SEAM.
  - A log longer than the session has been open is refused.
  - A game the session had time for is kept at the score of the server's own replay.
  - A good score goes in under Patrick's, and the last of the children's falls off the bottom.
  - A child with a dime gets no game and keeps the dime.
- `BorrascaTests#theCabinetGamesScoreTheSameWhenPlayedAgain`:
  - Both games, played with a changing hand on three seeds, score the same when replayed from the runs of keys a screen would
    send.
  - BIG CAT is over by dawn at the latest.
- `BorrascaVisualProof` draws a fourteenth page: the two cabinets and a frame of each game part way through.

## Run 1498 repairs

Run 1498 failed three cases in the build job's full structure suite:
- The Johnny case was repaired in 0.4.432.
- `OctoberFollowupTests#watchedOriginalMobsKeepTicking...` found a stray reader, `pine_jumper`, still logged in on the real world. The
  Borrasca wire case (`borrasca_wire`) had no `@AfterBatch` cleanup. It now has one, and so do `borrasca_registration` and
  `borrasca_arcade_rules`.
- `BarGamesTests#theBoardsOwnPlayerThrowsOnItsTurn`: the empty template keeps only its own corner's chunks ticking. The board can
  hang in the next chunk, and there the regular's first dart hung in the air for the rest of the case. The case now holds the
  corner's chunks, and seats the regular only once the board and the oche both tick entities.

## Numbers

Protocol 175 (two new payloads). Witness 86/65/63 and layout 135 unchanged. New builds only.
