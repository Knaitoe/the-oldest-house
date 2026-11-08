# A slower first stretch (0.4.393)

R4 of [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md), built when the owner asked for "the rest of the rules" on November 8: "continue with development now please. for the rest of the rules, then back to the textures, then to the vignettes."

## The rule

- **The domestic halls go on longer.** They are dealt to depth eight, where before they stopped at six.
- **The stone halls begin at nine**, where before they began at seven.
- **Nothing else moves.**
  - Stories still begin at depth six, and trials and impossible rooms keep their own depths.
  - The dark halls still begin at sixteen.
  - The staircase is still at twenty-four.
- This only adds walking in the domestic halls. Every hall that belonged to the stone stretch (the galleries, the grit gallery, the cistern walk and the library stacks) is now dealt from nine, and every hall and puzzle room that belonged to the domestic stretch now goes on to eight.

## Code

- `LabyrinthPacing.STONE_DEPTH` is 9 (it was 7). Everything that turns at the stone reads it: the ordinary halls' weights, `grayAvailable`, the early puzzle rooms, Truant's places, and the builder's `requiredDepth`, so the stone halls are still built ahead of anyone reaching them.

## Tests

- `StoneHallTests.theNinthPassStartsTheStoneTier` (renamed from the seventh): the stone halls enter at nine passes; at eight the way on is still domestic.
- `ExplorationPassTests.nineForwardPassesOfferOnlyStoneOrdinaryHallsAndRetainDrawnMaps` (renamed from seven): domestic halls before nine, stone from nine, stone built ahead.
- `EntrancePlaytestTests.untilTheQuietRoomsAreDoneTheHouseStaysDomesticThenDeepensACrossingAtATime`: after the quiet rooms, stories at six and the stone three crossings later, with no stone hall offered before it.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130. No new slot.
