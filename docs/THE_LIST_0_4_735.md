# The list, stand-ins 1: the dawn bus (0.4.735)

The first of the stand-ins `docs/THE_LIST_PLAN.md` lists. No protocol, layout or Witness change.

Part 7c (`docs/THE_LIST_0_4_728.md`) put the one who went in, read as running, on the first bus out at first light, and showed it as a
fade. The bus is now there.

- On a night four whose one who went in is read as running (HOME RUNAWAY) and is in the game, a coach turns onto Main Street at the
  west end of town from `TheListBus.COME` on the town's clock, slows, and stands in the eastbound lane from `STANDS`, nose east, its
  door at the bench and its lamps and the sign over its screen lit. Everyone in town sees it; nobody else is told anything.
- At first light the reader at the bench gets on as before (the fade, set down in their own room). Then the bus pulls out east,
  gathering speed, and is gone past the tracks after `LEAVE` ticks, with or without anyone on it. If the night ends early (Stace, or
  none of the five left), it pulls out from wherever it has got to.
- It is a TownCar of a new kind, `BUS`: it drives, so it is no thing in the way and its lamps are always on. Told where it is every
  tick and gone two seconds after it is not; never saved. The town put away takes its bus with it.

Art: `tools/draw_list_bus.py` draws ten textures (ribbed bright metal sides with the line's stripe, the band of glass, the windscreen,
the back's louvres, the front grille, the roof, the door's two panes, the lit sign) and ten models (the coach in five fifths, end to end,
and each fifth's lamps), cutting every box on the sixteen-pixel lines as the town's cars are cut. Its wheels are the cars' wide wheel,
drawn half again as large. `--check` runs in CI and the quick checks.

Checked in `theDawnBus`: the bus stands with its door at the bench before first light, lit and no wall; at first light it is still
there, pulling out; then it is gone.
