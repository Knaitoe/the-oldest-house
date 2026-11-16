# A sort of thing: the build plan

From `docs/SLEEPING_KING_PROPOSAL.md` and its decisions of 15 November. Each part below is one release, playable or reviewable on its
own. The proposal's first slice ("the fourth square alone") is split in three so that the art, the rules and the ending can each be
looked at before the next is built on them.

| Part | Release | What |
| --- | --- | --- |
| 1 | the place | The drawing-room and its glass; the board strip (three files by eight ranks of sixteen-block squares, brooks and hedges); the wood on the fourth square with his tree; every block of the country drawn twice, filled and ruled, by one tool; the King, painted asleep, under the tree. Not dealt. |
| 2 | his breath and the five states | The shared four-second breath; the fill's edge moving a block with it; how much of a thing each reader is (the one sum, on every kind of land), sent to their client and shown on their own body (as they are; flat, no shadow; in outline) and their feet; hedges and brooks by state. Still not dealt. |
| 3 | the brothers, the rattle, both endings in small | The two brothers and what they say; the rattle on his square, taken on the breath; his stirring; saying his name, or walking off the board until a line. The House deals it from here (the understanding stories, depth twelve, after the quiet rooms); one Witness source. |
| 4 | the hill, the Queen, the bottle and the cake; the railway | |
| 5 | "Who are you?", the mushroom and the drawn bridge | |
| 6 | the court of cards and the Cat | |
| 7 | the forest, the Knight and the song | |
| 8 | the crown, the feast, the Queen's move and the last move at the tree | |
| 9 | afterwards: the keepsake, the aftermaths, the account, the Witness line, the leaf | |

The Witness pool grows by one when part 3 makes the story finishable, not before.

All nine parts are built, 0.4.641 to 0.4.649, each with its own slice doc (docs/SLEEPING_KING_0_4_641.md to docs/SLEEPING_KING_0_4_649.md).

## Part 1: the place, as built

- `LabyrinthPlace.SLEEPING_KING` ("a_sort_of_thing", one long visit, outside), its builder, the literary tables (the miniature, the
  staircase name "the ruled country", the way on, the door bleed, the scene clock), never dealt.
- `KingSite`: the plan as constants. The House's door opens into the drawing-room (fire, chimney-piece, the glass over it, chessmen
  on the hearthrug, the chess problem on the table, printed backwards). Through the glass the same room the other way round, and
  its window gives onto the board.
- `KingBlock`: one block family; every kind has a `ruled` state. `tools/draw_king_art.py` draws the filled face of each kind and
  derives its ruled twin from it (the kind's own outline and a little hatching on paper), so the two cannot drift apart; `--check`.
- Part 1 builds the whole country filled except where the proposal's deep sleep has it ruled (the seventh and eighth ranks) and the
  band (the second and sixth), so the art of all three is seen at once.
- The King: a painted actor lying in a heap under the tree, in a tall night-cap with a tassel; no other pose.
