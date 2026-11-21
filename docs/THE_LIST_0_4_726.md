# The list, part 7a: Stace's plan (0.4.726)

The first part of night four (`docs/THE_LIST_PLAN.md`, part 7): Stace's plan, where the night ends, and the ending where she stops the
one who went in. No protocol, layout or Witness change.

## Her plan

Half a minute into night four's hunt, Stace has worked out where it ends, by how many of the five are alive:

| Alive | Where it ends | The one who went in is told |
|---|---|---|
| three or more | in front of the gin's press | "Over by the tracks, the gin's lights come on all at once." |
| one or two | at the foot of the water tower | "A flashlight at the foot of the water tower swings once toward you and holds." |
| none | out on the playa | "Out on the playa a pair of headlights comes on and stays on." |

From that moment the House's door is shut to the one who went in until first light.

## Stopped

She waits at her place. When the one who went in is within twelve blocks of it and on their feet, she turns to them and closes in.
Within reach she puts them down ("Stay there."), as from any blow; the body gets up after the usual five seconds. Two seconds after
they are up she does it again. The third time she finishes it ("That's the last of it."): the night is over, the body is the reader's
own again, and their outcome is `was_stopped`. Nobody else in town is stopped.

## Single and multiplayer

- **Alone**, Stace is the one who went in's last obstacle.
- **With others**, she is the town's, seen by everyone; only the one who went in is put down or stopped, and only they are told where it
  ends. Readers on Amber's side keep `was_in_town_at_the_end`.
- **Spectators** are neither seen nor put down.

## Not built yet

She is set at her place when her plan begins rather than walking there, she has no weapon of her own in hand, and she cannot yet be
finished with the hook; the other endings (the whole list, everyone gone, the bus) come in 7b.

## Made as

- **`TheListStace`** (new): `GIN`, `TOWER`, `PLAYA`; `PLAN_AFTER` 600, `GAP` 40, `FINISH` 3, `REACH` 2.5, `COMES` 12; `on`, `where`, `blows`,
  `holds`, `at`, `choose`, `tick`, `begin`, `finish`, `ground`, `reset`; for checks `planNow`. Kept per town, never saved.
- **`TheListNights`**: Stace's tick on night four (she may end the night); her plan put away at first light; `outcome` gives
  `was_stopped`. **`TheListNight.locked`**: shut from her plan. **`TheListPeople`**: Stace, once her plan has begun, is wherever she has
  got to. **`TheListTexts`**: `STACE_PLAN`, `STACE_HIT`, `STACE_FINISHED`, `STACE_DOWN`, `STACE_FINISH`.

## Tests

`TheListTests.stacesPlan` (new, batch `the_list_stace`): night four begun, the door is open; her plan begun with all five alive ends at
the gin, she is there, the reader is told and the door is shut. Brought to her, they are put down, alive; up, put down again; the third
time the night is over, they are ready with `was_stopped`, and her plan is put away.

## Every new line

Stace, over her head: "Stay there." and "That's the last of it."

Grey cues on the action bar, to the one who went in: the three places above; "Something heavy catches you behind the knees."; "She
doesn't stop until you stop moving."
