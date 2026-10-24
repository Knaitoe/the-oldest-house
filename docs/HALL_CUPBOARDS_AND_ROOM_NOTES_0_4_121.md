# Hall cupboards and room notes — 0.4.121

The impossible approach used three vanilla barrels as cupboard fronts. They remained visibly embedded barrel lids. The quiet room could also acquire loose-paper decoration in addition to its original readable book.

## Changes

- Add a shallow framed cupboard front over each of the three original hallway barrels. The fronts use vanilla spruce and stripped-spruce materials. Their models remain within four pixels of the wall; the same native barrels provide storage, opening behavior and contents.
- Keep source barrel identities, inventories and components. The in-place fitting waits for a loaded, vacant approach and leaves player changes in front alone.
- Before adding incidental paper, check its actual physical room for an existing paper or readable lectern. Separate domestic side rooms and maze nooks have their own quota.
- Reduce old incidental papers once in loaded, vacant ordinary rooms. An authored readable book takes priority; otherwise keep one existing sheet at its original coordinates. Collection state, personal text snapshots and Witness are untouched, and the cleanup never adds paper.
- Deliberate groups of original authored documents remain: bespoke vignettes, manor books, archives and staircase pages. This is not a blanket deletion of books or inventory items.

## Validation

Two native regression cases reproduce the quiet room's book plus two papers and the separate side-room case. They verify original lectern/book identity, independence across walls, stable paper coordinates, authored-scene exclusion and no restocking after saved-data reload. The existing hallway/bedroom regression now uses each of the three fronts to open its original native inventory and checks physical passage beside them.

The native client proof also draws the original barrel row and the fitted row side by side with production block models, checks all four cupboard facings, and requires `native-hall-cupboards.png` plus its completion marker before the existing 105-view architecture proof.

Resource JSON parsing, model bounds and the unchanged tracked PNG check pass locally. The workspace does not have JDK 21 or Gradle, so compilation and native execution run in the repository's existing CI. The 729-case corpus, twelve exact-source jobs and prior native architecture/GPU/client/reconnect proofs remain required before calling this candidate verified.

No protocol, layout or Witness change: protocol 88; labyrinth 46; manor 2; 52 eligible sources, 39 required, at least two kinds, three endings.
