# The list, stand-ins 2: the deputy's patrol car (0.4.736)

The second of the stand-ins `docs/THE_LIST_PLAN.md` lists. No protocol, layout or Witness change.

The deputy (`docs/THE_LIST_0_4_724.md`) walked the town's whole search on foot. He now has a car on the caliche roads.

- On night three and night four, while the hunt is on, a patrol car (`TownCar.CRUISER`: a two-tone sedan with a spotlight on its pillar
  and a light bar on its roof) drives one loop at a walking man's pace and a half: east along South Seventh from the school's lot, down
  the highway's southbound side to South Ninth, west along Ninth to where the caliche ends, back east on its far side, up the highway
  and west along Seventh again. It turns through each corner over the last blocks before it. Its headlights throw a real light on the
  road five blocks ahead.
- If it sees the one who went in (within eighteen blocks, within forty degrees of its nose, nothing in the way) it stops, a bell from
  the car, and holds its spotlight on them: a real light where they stand, moved with them while it can see them. The hunt reads them
  as standing in light for as long as it holds. They are told a spotlight swings off the road and stays on them; any other
  participant within sixteen blocks of the car hears the radio say they are out by the road. The deputy leaves his round and goes
  there for twenty seconds.
- After five seconds it lets them go, drives on and does not stop for them again for fifteen. Nobody else is spotted, and a
  spectator never.
- It is a puppet as the town's other cars are: no wall, lamps always on, told where it is every tick and gone two seconds after it is
  not, never saved. First light, the night's end and the town put away take it, its headlights and its spotlight with them.

Art: `tools/draw_calder_cars.py` draws the cruiser from the sedan's body with its own cabin and roof, the two-tone side, the spotlight
and the light bar's chrome base, and two lit halves of the bar (`bar_red`, `bar_blue`). `--check` already runs in CI and the quick
checks.

Checked in `theCruiser`: the car drives, lit and no wall; a reader who did not go in is not spotted; the one who went in ahead of it is
spotted, told, stood in its light (the light block, and the hunt's read) while the other is not, the reader by the car hears the radio
and the deputy is called; the car stays stopped while it holds, then lets them go, drives on and puts its spotlight out; first light
takes it.
