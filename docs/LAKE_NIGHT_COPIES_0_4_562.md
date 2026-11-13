# A lake each: the lake nights by copy (0.4.562)

The owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full".

Movie Night, Costume Night and Winter Lake have each been one lake for everyone: one record of the night for the whole world, with
its canoes, its cast, its clock and its Stacey in it. Any number of readers could be out on one lake at once, and whatever one of them
did to the night (the boys running from the hole, the lights going out at the contest, a canoe claimed) was done for all of them.
0.4.557 named them as the next to be kept by copy. This release does it.

New builds only. No layout, protocol or Witness change.

## Full

| Story | Places | Why |
|---|---|---|
| Movie Night | 4 | one to each canoe at the dock |
| Costume Night | 6 | a crowd for Hank's contest, and no more |
| Winter Lake | 4 | one to each of the four boys at the fire |

A lake's places are held by the readers at it now. None of the three keeps a roll, so a reader who walks out keeps no place.

- The next reader through the door when a lake is full goes to the next lake, built whole a million blocks west by the scene's own
  builder. Their door waits while it is made ready; it is made ready ahead when a lake is down to its last place.
- **Each lake has its own night** (`LiteraryVignettes.shared(data, place, copy)`): the original's in the scene's record as it always
  was, copy k's kept beside it. It holds the canoes and who has claimed them, the cast and where each of them is, the clock, who has
  gone missing, whether the lights are out or the boys have run, and her.
- **Each lake has its own Stacey.** She hunts, hides from and is seen by the readers at her own lake alone. A reader at another lake is
  nobody to her (`LakeWitchEntity.here`).
- Each lake's clock runs only while somebody is at it, and is put back for the next readers when it has been empty a while, as before,
  each on its own.
- A snowball landing on the ice is heard at the lake it lands on. What she heard of each reader at Winter Lake is forgotten for that
  lake's readers when its night starts over or she breaks through, and kept for the readers at the others.
- A reader's own part is theirs wherever they are, as before: their exposures, what they rowed, what they have read and been told.
- The accounts already count company within one copy (0.4.547).

## The case

`LiteraryTests.aFifthReaderIsGivenALakeOfTheirOwnAtMovieNight`: four readers at Movie Night are full to a fifth and never to one of
the four. The fifth's door waits while a second lake is made ready, a million blocks west, and they go in by its own door. The second
lake has four canoes of its own at its dock, none of them the first lake's, its own Ray and Dot, and its own Stacey; the first lake keeps
its own of each. The fifth takes the second lake's first canoe, and the claim is kept at the second lake, not the first.

## What testing found

- **The Winter Lake hearing case started before the boys were back.** Run after another case at the same lake, the boys and she are
  that lake's saved people, and come back only once their ground has loaded again. In one local run of the lake cases the hearing case
  looked for them a fixed fifteen ticks in, 0.3 seconds after the one-page case before it had ended, and Bud was not there yet. Nothing
  about the copies is involved: the original lake is copy 0 throughout. The case is now one sequence that waits until all four boys
  and she are loaded, then keeps its own timing from there.
- **A wait that crashed the test server.** The first try waited with `SceneReady.when` and set up the case's every-tick step inside
  the wait. `onEachTick` schedules one step for every tick left, and adding hundreds of them while the test's timers are being walked
  broke the walk (a NullPointerException in the timer map's iterator). A case sets up its every-tick steps when it starts.

## Decided here (any of it can move)

- Movie Night four, one to each canoe; Winter Lake four, one to each of the boys; Costume Night six.
- The film, the fireworks and the score were already kept by where the lake stands, and need nothing more.

## Not yet

- **Camp Blood and the hotel**: each still keeps its story in one record for the whole world.
- **The Sphinx**: her halls are the House's own, and stay shared on purpose.
