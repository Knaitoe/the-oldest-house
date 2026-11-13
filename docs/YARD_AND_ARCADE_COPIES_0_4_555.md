# A farm for the sixth: the yard's stories and the arcade by copy (0.4.555)

The owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full".

Three of the yard's stories (The Supper, In the Tall Grass, Growing Things) and the Polybius arcade have always been one place for
everyone: one supper, one road, one Saturday, one arcade, with any number of readers in it. Each already kept its story by where it
stands (a supper, a field, a day or a Block Party game for each base), so a second whole copy of the place has a story of its own
without anything in the stories changing. This release gives them copies and a number of places.

## Full

| Story | Places | Why |
|---|---|---|
| The Supper | 5 | one to each chair laid for a guest at the Marshes' table (`MarshSupperSite.READER_SEATS`) |
| In the Tall Grass | 6 | a carload, for the House's wagon on the shoulder |
| Growing Things | 4 | a pair of hands at each end of the kitchen table, and two more; a small house |
| Polybius | 4 | Block Party's four seats (`PolybiusBrawl.SEATS`) |

A place's places are held by the readers in it now. None of these stories keeps a roll, so a reader who walks out keeps no place: the
supper, the field, the day and the arcade go on without them, as they always have.

- The next reader through the door goes into the next copy, built whole a million blocks west by the place's own builder, with a
  household, a guest, a boy's voice or three regulars of its own. Their door waits while it is made ready; it is made ready ahead when a
  copy is down to its last place. Friends go into the same copy while it has room.
- **The Supper.** Corinne's keys never leave the farm: whoever carries them out finds them gone, and they are back by the butcher's block
  of the farm they were carried from, the one their carrier keeps (`MarshSupper.keysBack`). Before, they went back to the first farm's
  kitchen whatever farm they came from.
- **Polybius.** One reader plays a cabinet at a time, in each arcade: a reader in the second arcade plays its Mothlight while somebody
  plays the first's. The tables are the whole server's, as they always were; a score made in one arcade now goes up on every other arcade
  whose tables are loaded as well (`Polybius.boardsElsewhere`), not only on the next arrival there.
- **In the Tall Grass.** The telegraph poles that never come nearer stand over the road the reader is on (`TallGrassClient`, through
  `SceneInstances.originAt`).
- **Growing Things.** Merry, on the Ghosts Set years later, looks at a reader she remembers from her own set's family: her key is the
  Ghosts Set's own (`LiteraryVignettes.actorId`), which 0.4.553 keeps by set. Before, a reader filming on a second set would never have been
  looked at.
- The accounts already count company within one copy (0.4.547), so a reader's line says who was at their own table, on their own road,
  at their own Saturday or in their own arcade.

## The cases

- `MarshSupperTests.aSixthGuestIsGivenAFarmOfTheirOwn` (literary): five readers at the farm are full to a sixth and never to one of the
  five. The sixth's door waits while a second farm is made ready; one of the five goes back to theirs. The second farm stands a million
  blocks west and the sixth goes in by its own door. A household and a guest of its own are at its table (its own Ida, not the first's);
  sat down, the sixth's supper begins while the first farm's still waits for its own guests. Her keys lie by the second farm's butcher's
  block only; the sixth takes them and carries them out into the House, and they are back by the second farm's butcher's block, with none
  in the first farm's kitchen. The same case checks the road's six, the Barretts' four and the arcade's four.
- `PolybiusTests.aFifthPlayerIsGivenAnArcadeOfTheirOwn` (literary): four readers in the arcade are full to a fifth. The fifth's door waits
  while a second arcade is made ready, a million blocks west, and they go in by its own door. One of the four plays the first arcade's
  Mothlight and nobody else in the first may play it at once; the fifth plays the second arcade's Mothlight all the same. Each arcade has
  its own three regulars, and the fifth's two men stand by the black cabinet in theirs.

## Decided here (any of it can move)

- The Supper five, In the Tall Grass six, Growing Things four, Polybius four.
- A reader who walks out keeps no place in any of the four, since none of them keeps a roll.
- The arcade's tables stay the whole server's, shown in every arcade.

## Not yet

- **The shared scenes with a world of their own**: the séance, Camp Blood, the lake nights (Movie Night, Costume Night, Winter Lake) and
  the hotel each keep their story in one record for the whole world (the scene's shared record, `LiteraryVignettes.shared`, for most; the
  séance's and the hotel's own records), so a copy cannot have a night of its own until that record is kept by copy, as the Goatman's
  evening was in 0.4.552. (This list first named Last orders too; its world record turned out to hold only the league board and the
  window, and it has copies since 0.4.556, `docs/LAST_ORDERS_COPIES_0_4_556.md`.)
- **The Sphinx**: her halls are the House's own (gray halls, not a story's place), and stay shared on purpose.

## Numbers

No layout change. No Witness change. New builds only.
