# The list, part 7b: Stace on foot, and fought (0.4.727)

Night four, fleshed out from 7a (`docs/THE_LIST_0_4_726.md`): Stace goes to where it ends herself, carries a shovel, and can be fought
and finished. No protocol, layout or Witness change.

## Her way there

When her plan begins she is on the water tower's catwalk, where she has stood every night. She walks round the catwalk to the gap in
its rail, climbs down the ladder rung by rung, facing it, and walks across town to where the night ends: the gin, the foot of the tower
or the playa (as in 7a). She carries a shovel from the moment her plan begins. If something stops her on foot for five seconds she is set
on at her goal. Only at her place does she close on the one who went in and put them down.

## Fought

A blow from the one who went in, once her plan has begun, on her way or at her place:

1. **Staggered.** She is knocked back a step ("She stumbles back with the shovel up, and keeps her feet.") and does not swing for three
   seconds.
2. **Down.** The third blow puts her on the ground ("She goes down in the dirt. The shovel is under her.").
3. **Finished.** A blow there with the hay hook in hand finishes her ("Stace Goodkin stops moving."). The one who went in's own record
   keeps it (`StaceDead`). The night goes on: the five who are left are still in town, and first light or the last of them ends it.

A blow from anyone else, or from the one who went in while they are down, does nothing to her. At first light she is stood up at her
place on the catwalk again, without the wound, for the town to put back.

## The account

The account says, for the one who went in, whichever was so: "On the last night Stace Goodkin came out with a shovel to finish it. She
is the one I didn't plan on." or "On the last night Stace Goodkin came out with a shovel and finished it. I didn't get up the third
time."

## Single and multiplayer

- **Alone**, she is fought by the one who went in only.
- **With others**, readers on Amber's side cannot harm her; what happens to her is seen by everyone in town and kept only in the one who
  went in's record. **Spectators** are not seen by her.

## Not built yet

The other endings (the whole list, everyone gone, the bus) come next; her body at first light is stood up rather than taken away, as for
the five on the earlier nights.

## Made as

- **`TheListStace`**: stages `CATWALK`, `LADDER`, `WALKING`, `THERE`, `DOWN`, `DEAD` (`stage`); `STAGGER` 60, `TAKES` 3, `STUCK` 100,
  `CLIMB` .25, `PACE` .9; `LADDER_TOP`, `LADDER_FOOT`; `walkTo`, `there`, `struck`, `dead`, `taken`; `reset` takes the level and stands her
  up.
- **`TheListHunt.struck`**: a blow on Stace goes to her own handling. **`TheListPeople`**: Stace holds the shovel from her plan.
- **`TheListTexts`**: `STACE_STAGGERS`, `STACE_FALLS`, `STACE_KILLED`; the account's two Stace lines.

## Tests

- `TheListTests.stacesPlan` (rewritten): she sets off from the catwalk, reaches the ladder, comes down and walks to the gin with the
  shovel before the reader is brought to her; then as in 7a.
- `TheListTests.staceFought` (new, batch `the_list_stace_fought`): she comes down; three blows stagger her twice and put her down; a
  blow with the hook finishes her, the record keeps it and the night goes on; at first light she is back at her place unmarked.

## Every new line

Grey cues on the action bar, to the one who went in: "She stumbles back with the shovel up, and keeps her feet."; "She goes down in the
dirt. The shovel is under her."; "Stace Goodkin stops moving."

The account: the two Stace lines above.
