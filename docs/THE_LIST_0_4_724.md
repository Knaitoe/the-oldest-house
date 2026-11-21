# The list, part 6b: the deputy out on his round (0.4.724)

The second part of night three (`docs/THE_LIST_PLAN.md`, part 6). The deputy's car on the caliche roads and the town out with
flashlights, in skeleton: the deputy walks the town with a flashlight, sees the one who went in, calls out and fires. No protocol,
layout or Witness change.

## His round

On night three and night four, while the hunt is on, the deputy leaves his place on the sidewalk by the substation with a flashlight
lit in his hand. He walks east and west along Main Street's south sidewalk, down Avenue K's west shoulder to Seventh, along Seventh and
back, over and over. If something stops him for five seconds he is set on at the next point of his round.

The flashlight's beam is a real light, four blocks ahead of him at chest height, that moves as he does. A reader standing in it is
standing in light: the pull toward the nearest name is held off, as under a street lamp.

## Seen

1. **Called out.** If the one who went in is within twenty blocks, within sixty degrees of where he is facing and nothing is in the way,
   he stops, turns the light on them and calls out. They are told the flashlight has found them.
2. **Fired.** Still seen a second and a half later, he fires. The body goes down where it stands, as from any blow, and gets up after
   the usual five seconds. It is not a House death and nothing is lost. He does not fire again for twenty seconds.
3. **Lost.** Out of his sight for three seconds, he goes back to his round from where he stopped.

## The curfew

Once a night, another reader in town close in front of him in the street is told there is a curfew and to go home. Nobody is made to.

## First light

At first light his round is over: he is back on the sidewalk by the substation, the beam is out and his way is forgotten.

## Single and multiplayer

- **Alone**, he is the one who went in's to avoid.
- **With others**, he is one man in the town, seen by everyone; only the one who went in is fired on, and only the others are told to go
  home.
- **Spectators** are neither seen nor told.

## Not built yet

The deputy's car and the townspeople out with flashlights beside him need their own painted figures and are left for later; the
deputy's round stands for the town's search in this skeleton.

## Made as

- **`TheListDeputy`** (new): `ROUND` (his round's points), `WALK` .55, `SEES` 20, `FACING` 60, `BEAM` 4, `AIM` 30, `AGAIN` 400, `LOST`
  60, `STUCK` 100; `on`, `holds`, `tick`, `walk`, `nearest`, `sees`, `beam`, `reset`; for checks `aiming`, `fired`, `beamAt`, `step`.
  Where he is on his round is kept per town and never saved.
- **`TheListPeople`**: the deputy on his round is wherever he has got to, and holds the flashlight.
- **`TheListNights`**: the round each tick of night three's and four's hunts; put away at first light. **`TheListNight.end`** too.
- **`TheListTexts`**: the deputy's lines and cues. No new sound, block or art (the flashlight is Borrasca's).

## Tests

`TheListTests.theDeputy` (new, batch `the_list_deputy`): night three begun, the deputy is out, walks off from his place with the
flashlight in his hand, and its beam is a light ahead of him. Another reader just in front of him is told there is a curfew. The one who
went in, put in front of him, is seen and told; he fires and the body goes down, alive. Out of his sight, he walks on. At first light his
round is over, he is back at his place and the beam is out.

## Every new line

The deputy, over his head:

- Seeing the one who went in: "Hey. You. Stay right where you are."
- After firing: "Stay down."
- To another reader in the street: "There's a curfew. Go home and stay there."

Grey cues on the action bar, to the one who went in: "A flashlight finds you from down the street." and, when he fires, "Something hits
you in the chest, and then you hear it."
