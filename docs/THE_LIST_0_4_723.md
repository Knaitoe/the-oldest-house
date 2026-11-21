# The list, part 6a: what comes back, and the flyers (0.4.723)

The first part of night three (`docs/THE_LIST_PLAN.md`, part 6). Night three, as the proposal has it: the hay hook from the seed house
comes back however it is got rid of, and the town knows by now (flyers with the drawn face, a curfew). This part builds the hook
coming back and the flyers. The deputy's car, the town out with flashlights and the five hiding in the field house follow (6b, 6c). No
protocol, layout or Witness change.

## The flyers

When night three begins, flyers go up along both sides of Main Street, taped to the fronts at eye height: a photocopy of the face drawn
on the sack and, under it, CURFEW 9 PM. A flyer goes only where there is a wall to tape it to and nothing in the way. The one who went
in, and every other reader in town, is told there is one taped to the kitchen window. A hand on a flyer reads it.

They stay up through night four and come down only when the town's nights are put away.

## What comes back

On night one and night two the hay hook cannot be let go of: thrown, it is back in the hand within the second. From night three's
hunt on it can be thrown away, and it is not given back to the hand.

1. **Away.** While the reader is anywhere but their own house, it stays away. The thrown hook lies where it fell.
2. **On the bed.** The next time they are in their own house and not looking at their bed, the thrown hook is gone from wherever it lay,
   and the hook is on the bed: clean, or bloodied if it has been used.
3. **Seen.** When they look at the bed, within six blocks and with nothing in the way, they are told it is there.
4. **Taken up.** Picked up (or never let go), it is theirs as before. Put in a chest or anywhere else on them, it is back in the hand
   within the second, as it always was.

## Single and multiplayer

- **Alone**, the hook and the bed are the one who went in's.
- **With others**, only the one who went in's hook comes back; the flyers are the town's, seen by everyone in it.
- **Spectators** are told nothing.

## Not built yet

The face on the flyer is the sack's drawn face, not yet the reader's own (SELF and HAIR, part 9's polish). The curfew is printed, not
kept: nobody in town is sent home yet (6b).

## Made as

- **`TheListFlyers`** (new): `STAGE` 6 (the poster fitting's flyer), `spots` (both sides of Main Street, every twelve blocks), `post`,
  `down`, `up` (for a check).
- **`TheListBody`**: `tossed` lets the hook go from night three on and marks it the reader's (`HOOK_OF`); `comesBack` keeps it away,
  puts it on the bed and tells the reader when they look; `hookOnBed`, `hookAway` for a check. `HookAway` and `HookBed` are kept in
  the reader's record.
- **`TheListNights`**: `comesBack` (from night three's hunt to the end); night three's beginning puts the flyers up and tells everyone.
- **`TheListNight.end`** takes the flyers down.
- **Art**: `poster_6` (the flyer), drawn by `tools/draw_the_list_art.py`.

## Tests

`TheListTests.whatComesBack` (new, batch `the_list_back`): night three begun, the reader is told of the hook on the table and the flyer
on the window, and the flyers are up along Main Street. Thrown away in the street, the hook is not in their hands and stays away. In the
bedroom looking the other way, the thrown one is gone and one lies on the bed; looking at the bed, they are told. Taken up, it is theirs,
and only one. Ended, the flyers are down.

## Every new line

Grey cues on the action bar:

- To every reader in town, as night three begins: "There's a flyer taped to the kitchen window. CURFEW 9 PM, and under it somebody's
  drawing of a face in a sack."
- To the one who went in, looking at their bed: "The hay hook is on your bed."

A hand on a flyer: "A flyer. CURFEW 9 PM, and a drawing of the face in the sack. A phone number under it."
