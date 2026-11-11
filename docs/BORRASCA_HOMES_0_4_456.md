# Drisking's four households (0.4.456)

Note 12 of the owner's playtest of 10 November (docs/BORRASCA_PLAYTEST_2026_11_10.md): "You could add inflections of personality
into the citizens homes. Remember that this town is sick, that many know what the town does and why and just accept it. Some
don't, but they have to bite their tongue."

The four houses on the street had the same room: a rug, a table and a stool, an armchair, a chest of drawers, a bed. Nothing
in any of them said who lived there.

They are four households now. They are the four families whose cards hang on the doctor's corkboard, so each has a crib. What
else is in each room is what that family does about what the town does.

## The four

| House | What they do about it | What is in the room |
|---|---|---|
| The Pruitts | They lost a daughter on the pine trail; her sheet is on the store's board, sun-faded. They took what the town gave them afterwards. | Her school picture in a gilt frame over the drawers, the one clean thing on that wall. The crib at the foot of the bed. The armchair turned to the window on the street. |
| The Hollises | They have their faith, and ask it nothing. | A wooden cross over the bed, a card from the church pushed in behind it. The family Bible open on the desk at its page of births: one name has a small cross drawn beside it. Plain chairs. Bare boards. |
| The Webbs | They are paid by it. | A new television in a walnut cabinet, the sofa and a footstool set before it. A canvas work jacket on the hooks by the door, PRESCOTT stitched over the pocket, no dust on it. |
| The Corleys | They know, and say nothing. | A suitcase by the bed, packed. A road atlas open on the table, a way out of the county marked in pencil, rubbed out, and marked again. A picture frame on its face on the drawers. Two cribs. |

Each room also has its own bed, its own chairs and its own floor: the Pruitts' rug, the Hollises' bare boards, the Webbs' bright
carpet before the television, the Corleys' rug.

## Show, not tell

A hand on one of these things gives one line, and the line says what is there to be seen. No line says what it means, and no
line names the town's business. Some of them:

- "A school picture of a girl, in a gilt frame. The glass has been wiped. Nothing else on that wall has."
- "The family Bible, open at the births. Beside one name somebody has drawn a small cross, and a date. The newest line is
  Katelyn's, the ink still dark."
- "A canvas work jacket on the hook by the door. PRESCOTT is stitched over the pocket. There is no dust of any kind on it."
- "A road atlas open on the table. A way out of the county is marked in pencil, rubbed out, and marked again."
- "A picture frame lying on its face on the drawers."

The picture frame cannot be turned over.

## Years on

To a reader who comes back grown, each room is the same household later. Each thing changes where it stands.

| House | Years on |
|---|---|
| The Pruitts | A snapshot of a younger girl is tucked into the corner of the gilt frame. The crib is down from the attic again. |
| The Hollises | One more line under Katelyn's in the Bible, in the same hand. |
| The Webbs | A flat television stands on top of the old cabinet. A second, smaller jacket hangs on the hooks, and a cap. |
| The Corleys | Dust on the suitcase. The atlas is shut, under a stack of bills. The frame is still on its face. |

## What is drawn

Nine things, at sixteen pixels to the block, by `tools/draw_borrasca_homes.py` (twenty textures, sixteen models):

- a crib with slatted sides, a sheet and a checked blanket, a toy duck in it (two years' blankets);
- a framed school photograph (and the later one with the snapshot);
- a wooden cross with its card;
- an open Bible (and the later page);
- a television in a cabinet on four legs, with an aerial (and the later one, a flat set standing on it);
- a row of brass hooks with a work jacket (and the later one, with a second jacket and a cap);
- a suitcase with straps, clasps and a tag (and the later one, dusty);
- an open road atlas with a pencil across it (and the later one, shut under two envelopes);
- a picture frame on its face.

## How it is made

- `labyrinth/BorrascaHomeBlock` (`borrasca_home`): a kind, a facing and a stage. A stage is a year: 0 on the childhood visits,
  1 years on. Things on the floor are solid; things on a wall hang on the wall behind them; things on a table lie in the cell
  above it.
- `labyrinth/BorrascaHomes`: each household's room (`furnish`), the list of what tells them apart (`things`), the lines
  (`look`) and the years (`years`, called where the station's price board is set). A room is laid out from its back wall and
  its north end, so the two houses across the street are the mirror of the two on this side.
- `BorrascaDressing.house` no longer furnishes a room itself.

No layout, protocol or Witness change.

## Checks

- Native: a new Borrasca case. Every thing stands where its household keeps it, of its kind, facing its way; what lies on a
  table has a table under it and what hangs has a wall behind it; the four rooms hold four different sets of things; a reader
  can walk in at each door; every thing has a line, and no two houses share one; years on, each thing that ages has aged and its
  line has changed.
- Client proof: a new view (`homes`), every thing in both its years through the production models.
- I looked at each room in first person, in the childhood year and years on, before committing.

## What this is not

- The houses' people are not in them. The Webbs are at the diner counter by day; nobody else on the street has a face yet.
- The other families on the corkboard (the Maddoxes, the Tates) have no house on the street.
- The rooms do not change after the ending.
