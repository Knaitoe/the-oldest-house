# The doctor's corkboard (0.4.455)

Note 9 of the owner's playtest of 10 November (docs/BORRASCA_PLAYTEST_2026_11_10.md), of the birth cards on Dr. Hale's
corkboard: "Try to show, don't tell. Try to design an actual ui for this".

Until now a hand on the corkboard opened four pages of typed text. The first page described the board ("Pinned in rows, edges
curling. Every card has a stork, or a rattle, or a ribbon, and every one says thank you to the doctor."), and the rest listed
each card under its family's name in capitals. The last line told the reader that the envelope was empty.

The board is a board now. A hand on it opens the board itself.

## What a reader sees

Cork in a wooden frame, and the families' cards pinned to it, three across and two down.

- Each card is a printed card on coloured stock with a small printed picture: a ribbon, a stork, a duck, a pram, a rattle, a
  bottle. On it the family has written the child's name, the day, and who it is from. Each family writes in its own hand:
  upright, leaning, thin, heavy.
- The last pin holds an envelope. On its face is the doctor's name and nothing else.
- Each piece hangs a little off true on a pin of its own colour, and throws a shadow on the cork.
- A card under the pointer lifts a little.

## Taking a piece down

A click takes a piece off its pin, and it comes up large, as it is held. Everything on it can be read there: the line of print
("It's a Girl!", "Twins!"), the child's name written large, a weight where the family gave one, the day and the year, the
family's line to the doctor, and its signature.

- A click anywhere puts it back. The left and right arrow keys (or A and D) take down the next piece along the board, place
  by place, what hangs on top before what is under it; from the board itself they take down the first or the last.
- The envelope turns over when it is clicked while it is held (or with space). Its flap stands up above the fold and its
  mouth is open under it, and there is nothing in it. Nothing on the screen says that it is empty.
- Where a piece hung, the hole its pin left shows in the cork.

Nothing is written on the screen but what is written on the cards. The board does not say what the cards have in common. The
days are there to be counted, the names to be read down, and every card has a line to the same man.

## The visits

| Visit | What hangs there |
|---|---|
| The first | five cards of the childhood year, a few weeks apart, and the envelope |
| The second | the same, and the Tates' card pinned over the envelope: the couple who were waiting on the first visit. The envelope is still underneath, the doctor's name showing below the card, and can be taken out by that edge |
| A reader come back grown | five cards of the later year, pinned over the old ones. The old cards are still under them, gone yellow, a corner of each curling; they can be taken out and read too. The envelope has yellowed with them |

Every baby's name still begins with P or K (0.4.424), in both years.

## What counts as having read it

A piece counts as looked at once a reader has had it in the hand for three quarters of a second. The reader's screen tells the
server, which checks that the piece hangs on that reader's board for their present visit and that they are standing at the board
(within eight blocks of it). Having looked at every piece that hangs on top is having read the board, recorded under the same
keys the pages had (Read_Cards, Read_Cards2, Read_CardsLater). Pieces underneath are not asked for. What a reader has looked at
is kept, so the board opens again with it.

Borrasca is each reader's own town, so the board is each reader's own.

## How it is made

- `labyrinth/BorrascaCards`: what hangs on the board on each visit (`board(visit)`), the order a hand goes along it, and the
  record of what has been looked at. `BorrascaShopTexts.cards` (the pages) is removed.
- `client/BorrascaCardsScreen`: the board and the piece in the hand. The board is drawn at two screen pixels to its pixel where
  the window allows, otherwise at one; a held card at the largest whole size that fits.
- `network/BorrascaCardsPayload` (to the reader: the visit and what they have looked at) and `BorrascaCardSeenPayload` (from
  the reader: a piece held). Protocol 180.
- `tools/draw_borrasca_cards.py` draws the screen's one sheet (`textures/gui/borrasca_cards.png`, 128 by 64): the cork, the
  frame, the six pictures at sixteen pixels, the pin and the curled corner. `--check` is in the quick gate and the workflow.

The hands are the House's existing letters (the nine-pixel hands in `house.HouseWriting`). The card stock, the pins and the
yellowing are plain colour.

## Checks

- Native: the three corkboard cases in `BorrascaTests` now open the board (not pages) and read what hangs there. New
  assertions: five cards and the envelope on the first visit; the Tates' card over the envelope on the second, and the envelope
  not asked for; the old cards under the new for a grown reader; a piece that does not hang there, a board from another visit
  and a reader across the street are all refused; the board is read only when every piece on top has been looked at.
- Client proof: three new views (`cards-0`, `cards-1`, `cards-2`), drawn by the production screen's own code at the size it
  is drawn. Before drawing, every piece of every visit's board is measured in the production fonts: each line must lie inside
  its card, hanging and in the hand, or the proof fails.

## What this is not

- The card in the hand is not a separate item and cannot be carried away.
- Other papers in Drisking's shops (the map, the price cards, the framed history) still open as pages. They are documents a
  reader reads; the corkboard was a description of a thing a reader should see.
