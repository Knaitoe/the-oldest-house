# The Stock Gate and the Windbreak (0.4.345)

Two more of the yard's encounters from the owner's draft "The House Has a Yard" (4 November): its middle puzzle and its deep hazard. Both are gray passages outside, not Witness stories. Layout 106 appends slots 205 and 206; protocol 141 adds the new fittings' block states. Nothing standing is rebuilt.

## The Stock Gate

From the draft:

> *"From the loading platform you can see how the swing gates join the chutes. At ground level the high plank sides hide the junctions. The gate positions and the movement of the animals give the crew evidence."*
>
> *"A wrong gate lets the herd into your lane. The loading step gives you somewhere to get out of the way. A friend can call the route from above. Alone, you can climb back up, watch where the animals went, and set the gates again."*

`YARD_STOCK`, `YardStock`. It is the survey family's deep puzzle, after Survey Stakes (the reader learned to read the land from a vantage there), offered by the puzzle rooms' own pacing from the strange depths.

**The yard.**

- **The loading platform.** The back door opens under a platform on posts. A stair at its east side climbs to the deck, which is railed all round so nobody steps from it onto the plank sides.
- **The lane.** It runs on under the platform, out into the yard and straight to the far gate. It is one chute wide between high plank sides, and it is always open.
- **The side chutes.** Three join the lane, two from the west and one from the east, each round a bend to a swing gate. A swing gate is a leaf of tube rails on oak stiles, three blocks high, its top rail painted. It hangs on a post standing a little above the sides, so from the deck the plan reads: which arm each painted rail crosses.
  - The first two gates, K and L, each choose between the pen's race and an empty crush. Both start open to the pen.
  - The third gate, N, leads only to a crush or an empty loading race, whichever way it stands.
- **The herd.** Four cattle stand in the pen, made once with the yard and never replaced.
- **The loading steps.** Four recesses with a stone step stand along the lane's east side.

**The herd.**

- **The drive.** While anyone is in the yard or on the deck, the herd is driven out of the pen. A chain rattles at the pen gate, they low, and dust lifts first.
- **Which way they go.** They take whichever way the gates leave open from the pen: straight on where they can, and where a chute meets the lane, away from the nearest reader in it (north with nobody there).
- **The way back.** They go to the far end and mill. Once the way behind them is clear of readers, they walk back to the pen. There is a cooldown before the next drive.
- **Readers in the way.** The cattle shove a reader in their way along the lane, and wait behind someone who can't be moved. A reader on a loading step is out of their way.
- **A gate swung behind them.** If a gate is swung across the way back after they came through, they stand where they are until it opens again.
- **Both pen gates shut.** The herd crowds the gate and goes nowhere.

**The gates.** A gate is swung by hand from the chute, within reach. It never swings across a body, or across where the arms meet while cattle are there.

**Solved.** Reaching the far gate settles the yard for the House's pacing. The usual way is to:

1. look from the deck, or watch where the herd went;
2. go round each side chute to its gate;
3. swing K and L across the pen's race;
4. walk out.

**Reset.** Once the yard has been empty and unwatched for ten seconds, K and L go back open to the pen and the herd stands in the pen again, for the next crew. N keeps the position it was built with.

Nothing here hurts or is used up.

## The Windbreak

From the draft:

> *"Gusts move across the grass before they hit. A loose sheet of barn roofing scrapes between gusts, then tears free and strikes the fence at head height. Stone walls, tractor ruts, and the lee of the poplars offer cover."*
>
> *"Time a crossing between gusts or take the sheltered edge. In a deeper version, one useful piece of shelter starts to come apart in view, giving you time to choose the next one."*

`YARD_WINDBREAK`, `YardWind`. It is a physical hazard, dealt (weight 3) from the deep halls, as the draft's deep hazard. Because it is deep only, the deeper version is the version.

**The field.** An open field is crossed south to north, under a wind from the west.

- **The shelter line.** Up the west side, with short gaps between to time:
  - close-set poplars, their crowns one screen from the ground up, their boughs leaning with the wind;
  - a lean-to of barn roofing, its back boards to the wind;
  - a chest-high stone wall.
- **The ruts.** Along the east side, a tractor's two ruts, wet in the bottom.
- **The wire.** A post-and-wire fence runs along the east edge.
- **The middle.** The straight way up the middle is open to every gust.

**Shelter.** A reader is sheltered when something solid stands within four blocks upwind of their eyes, or when they crouch in a rut. The field's own hedge doesn't count: the wind comes over it.

**The gusts.** One comes every eleven seconds.

- **Before it hits.** For two seconds it shows on the grass, a front of blown grass crossing the field from west to east. The boughs lean, and the wind's own subtitled sound rises.
- **The gust.** For a second and a half, a reader out of shelter is pushed east toward the wire.

**The lean-to coming apart.**

- **Second gust.** The loose sheet at the lean-to's east edge lifts. Between gusts it scrapes.
- **Third gust.** It tears free and crosses the field at head height into the wire.
  - It hurts whoever it meets in its row, once each: two hearts, never past three left.
  - It comes from the lean-to, so readers sheltering under the lean-to are behind it.
  - It lies by the wire where it struck.
- **Fourth gust.** The lean-to's top back boards go.
- **Fifth gust.** The rest go, down to the bottom board, and the lean-to shelters nobody. The readers have seen it coming apart for two gusts, and the ruts and the stone wall are still there.

**Reset.** Once the field has been empty and unwatched for ten seconds, the lean-to is whole again, the sheet is back on its roof and the gusts are counted from none.

## Assets

`tools/draw_yard_fittings.py` draws two new fittings:

- **The swing gate's leaf**, in three heights: galvanised tube rails between oak stiles, a brace in the middle, the top rail painted.
- **The corrugated roofing sheet**, rusted at the nail lines of its laps, in three states: on a roof, its edge lifted, and fallen flat.

No new sounds: the chain, iron and cattle sounds are vanilla's; the wind is the yard's own.

## Tests

`YardStockTests` (exploration):

- **The whole puzzle.** The herd is driven into the lane and up it away from the reader, and goes back to the pen once the way is clear. K is swung across the race, and the next drive takes the other chute by the pen's second gate. L is swung, and the herd crowds the gate and stays penned. The far gate is reached. The same four cattle stay throughout, and the gates and the solved yard save without story credit.
- **A reader in the way.** A reader up the lane is shoved along it by the cattle; a reader on a loading step is out of their way; nobody is hurt.

`YardWindTests` (exploration):

- **The gust.** The poplars and the stone wall shelter their lee, and the middle of the field is open. The grass shows the gust before it hits and the boughs lean, and nobody is pushed yet. In the gust, the reader in the open is pushed east, while readers behind the poplars, behind the wall and crouched in a rut don't move.
- **The lean-to.** The third gust tears the sheet off. It takes two hearts from the reader in its row and never takes anyone past three. The reader under the lean-to is untouched, and the sheet lies by the wire. The fourth and fifth gusts strip the boards until the lean-to shelters nobody. The reset puts everything back.

Both places are exported for the native architecture views.
