# Painted family texture follow-up

The owner requests that the painted family match the established NPC style while remaining ghoulish and frightening; the faces may be asymmetric. This pass repaints the father, mother and child in both their initial and changed forms, using the same deterministic cast painter and native pixel density as the other literary humans.

The family keeps its olive suit, faded rose dress and blue shirt. Bloodless skin, hollow cheeks and uneven lids make the initial faces uneasy. The changed father has one collapsed socket and a crooked, slack jaw; the changed mother has a lower eye and a mouth pulled to one side; the changed child has a small hollow stare and an off-centre open mouth. Hair and clothes continue to identify the same people. Deliberate flat pixel clusters supply these features; no gradients, resampled portraits or random grain are added.

Only the six `literary_family_{father,mother,child}{,_changed}.png` sheets change. Each remains 512 by 256 pixels with its exact previous alpha. Existing production models, proportions, role/phase selection, saved actors, visits and transformation timing remain unchanged.

The historical 0.4.100 inventory stays intact. `art/inventory/painted_family_04123.json` records the six explicitly authorized replacements and their original review hashes. The verifier checks that exact set, matching atlas dimensions and alpha, all other catalogued paintings, and all 649 protected historical PNGs. `paint_character_skins.py --check` still checks the entire current painter output.

Local painter, character-art and diff checks pass. Fresh native production-model captures and exact-source CI are required before describing the update as fully verified. The existing character catalog includes all six family variants on their actual literary meshes; the native literary views also cover the changed child behind the lens. These are asset/render checks, not a complete ordinary playthrough of the family visits.
