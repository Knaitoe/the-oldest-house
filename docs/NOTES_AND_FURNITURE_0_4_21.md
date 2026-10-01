# Notes and furniture — 0.4.21

The corridors keep pieces of homes, and the writing now keeps the people who lived in them. Seventeen installments form four small serials, with additional household scraps between thresholds. These are original scenery and writing, not new Witness resolutions: the pool remains ten, the requirement eight across at least two kinds, and the endings three.

## Writing

Right-click the paper or notebook on a cupboard, desk or table to read through Minecraft's native book interface. Take Book collects that exact copy once per reader, surface and depth band. No chat announces the interpretation, identifies a writer as a monster or diagnoses the reader.

| Thread | Opening | Later discoveries |
| --- | --- | --- |
| Housekeeping | Washing a cup, mending a chair, folding sheets | “You are useless” appears once, then across three pages, then seven times on every page of an eight-page notebook in an original scratched bitmap hand. |
| The letters | A note about milk, supper and the spare key | “Do not come for me” changes from a warning to a request for one unopened room. Kitchens, the kettle and the empty chair recur. |
| The inventory | Spare linen for a guest | The list acquires an actual carried item's name, the reader's name and a nearby named owned animal, personal Witness count or visited-place count. Its final entry denies anything is missing. |
| The poems | A verse on a grocery list | Five original poems use domestic objects, repetition, white space, direct address and crossed-out lines. |

The first chapter is ordinary. Chapters two, three and four require six, eight and twelve crossings; the fifth poem requires sixteen. Further early scraps contain things such as a sticking drawer, soup, unlabelled photographs and a radiator that clicks. Reading multiple early scraps cannot skip these thresholds. Progress advances only on reaching a chapter's final native page or taking its complete book. Later chapters still appear in order if a reader starts deep in the labyrinth.

Each surface binds a saved book to the individual reader and current depth band. Reopening it keeps its text and page count. Returning at a deeper band can reveal a later installment; an earlier collected copy is never rewritten. Inventory details are snapshots from when the page was first encountered. Readers have independent native menus, book contents, page positions, progress and finite collection flags. Sharing or taking a page does not advance someone else's serial. Spectators cannot create discoveries.

Personal references use only the Minecraft profile and native in-game facts. User biography, account information and external browsing do not supply the writing. The voice belongs to a hostile fictional document, and mundane or compassionate scraps continue to coexist with it.

## Furniture

Twelve native model variants add cane chairs, kitchen stools, worn green armchairs, floral armchairs, blue woven sofa modules, footstools, walnut desks, Formica tables, bedside tables, chests of drawers, radiators and washing machines. Fourteen original pixel materials include cane weaving, floral cloth, repaired upholstery, laminate scratches, drawer fronts, yellowed enamel, a laundry window and ordinary paper. Furniture collision follows its shape and leaves space beneath tables. Seat variants use the existing native player sitting system with their own height and facing.

The furniture is distributed across the manor, landing, quiet room, cut-off kitchen/laundry/bedroom/dining fragments and maze recesses. Native beds and containers remain functional. Decorative drawer fronts do not introduce new storage rewards. Ordinary and esoteric pacing remains six/eight/twelve/sixteen crossings.

Layout 19 applies a decoration upgrade to existing version-18 rooms. It does not rerun the older fragment construction, rebuild vignettes, replace container entities, replenish caches or reset actors, run randomness, Witness evidence or dialogue. New paper uses bare tops only; specific furniture replacements require the expected original block and chair orientation. Saved per-place checkpoints prevent removed paper from being replenished. Earlier structural upgrades retain their original scope.

## Review

- `/oldesthouse writing notes` gives all seventeen preview books using the current player's in-game details, without writing reading progress.
- `/oldesthouse writing furniture` gives twelve placeable furniture variants. The operator samples use the generic furniture icon; placed shapes and materials follow their components.
- `/oldesthouse door junction` reaches the landing with the four initial writing threads. Deeper normal crossings reveal later writing.

Native GameTests cover ordered depth gates, actual page buttons and finite Take Book behavior, exact book persistence across reload, independent simultaneous readers, personal snapshots, spectators, seating, clear door approaches and the version-18 upgrade with player storage edits and existing evidence. Client book layout, furniture materials and atmosphere still require an in-game visual playtest.

Regenerate native assets with `python3 tools/generate_household_assets.py`.
