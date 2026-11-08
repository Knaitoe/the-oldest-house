# Texture pass, release 3: the great staircase (0.4.376)

The plan (docs/TEXTURE_PASS_PLAN.md) measured the great staircase as the largest single surface in the House: some 72,000 visible
blocks, most of them the well's deepslate tiles, its chiselled deepslate pilasters and the iron bars along every landing and flight.
The owner allowed architecture changes to the staircase, but not to its height. This release changes neither its height nor its
shape. It swaps block for block, as the halls' releases did, and lets the descent read as going down.

## What changes

| Was | Becomes |
|---|---|
| Deepslate tiles in the well's four walls and its two caps | The well's own stone: long blocks of dark stone in two courses, the joints staggered and sunk, the faces finely tooled. It comes in three bands, each darker and colder than the one above: the first third of the way down, the second, and everything below that. In the lowest band the edges are worn round and a wet sheen runs down from the joints |
| Chiselled deepslate, the pilaster every twelve blocks | A fluted pier in the same band's stone: three flutes between plain margins |
| Iron bars along the landings and the flights | A wrought-iron balustrade: a handrail, a bottom rail and balusters between them, round a post at each block. It is iron bars underneath, so it joins its neighbours, stops at a gap and keeps a body back exactly as the bars did |

The treads and landings (the staircase stone and the banded tuff, deepslate and blackstone bricks) stay as they are, as do the entry
hall, the camp and its walkway rail, the cell's bars, the arena and the escape below. The book-lit darkness and every dark check are
unchanged: nothing here gives light.

Seven blocks, seven textures and the balustrade's two models, drawn in code by `tools/draw_stair_well_art.py` (CI and the quick gate
run it with `--check`).

## How

The staircase is built from one plan (`FinaleArchitecture.plan`). Its shaft walls and caps now take `StairWell.stone(y)` and
`StairWell.pier(y)` for the band their height falls in, and its rails `StairWell.BALUSTRADE`. Nothing else in the plan changes.

What reads those blocks back reads the new ones too:
- the staircase's wear (the sparse breaks in the landings and rails) counts the balustrade as an authored rail, and trims the rails
  either side of a break as it trimmed the bars;
- the falling masonry along the well counts the well's own stone as masonry.

New builds only. A world that already built its staircase keeps it as it is.

## Tests

- **The architecture case** (the old shaft upgraded to the current plan) now also checks that the well's walls and piers are its own
  stone at three depths, in the band for each depth, with three different bands down the well. Its guard-rail count, one per depth
  band, counts the balustrade.
- The staircase entrance cases pass unchanged: the camp walkway keeps its iron bars.
- The finale, collapse and staircase wear cases pass unchanged: the wear still trims the rail beside a break, and two readers
  across a clock boundary still share one falling stone.
- The client check renders the three staircase views (upper, middle and lower) as before.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
