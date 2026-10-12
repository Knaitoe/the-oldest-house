# Ghost Set home, meals and decor

The original home now uses sixteen Minecraft-style material tiles, eight coarse equipment tiles, seven meal tiles and an empty plate and a crew call-sheet surface. Four original ImageGen files are imported unchanged. The painted cast stays in the mod's established NPC style; the room and equipment use chunky native pixel materials. JSON face UVs address the atlas tiles directly, with no bitmap cropping or resampling. The native camcorder mesh uses the same equipment materials.

The four actual dinner plates remain shared physical objects. A client baked-model wrapper uses only its reader's leased filming day and refreshes the affected native chunk cells on a day change or cleanup. It cannot replace anyone's inventory, change the shared plate state or change a peer's day. The meals are stew, spaghetti, sandwiches, soup, roast, potatoes, then spaghetti again; Merry's final spaghetti remains plain.

The original call-sheet lectern and its exact block entity, book, native reader and finite collection are retained. Only that source's book rendering becomes a flat sheet on the actual 22.5-degree sloping top. Native Minecraft text supplies the title and schedule labels. No general lectern renderer or unrelated book changes.

Three crew cases are real empty nine-slot containers. Their shared lids open for real menu users and remain open while another user is still inside. Native inventory transfer, container validity, saving and drops preserve their contents; they generate no stock or rewards. Case contents are independent of filming progress. Lamps physically switch between lit and unlit states. The sisters' supported books and drawing open finite personal originals; the monitor opens the day's own cut, or the unedited log while crouching. Other domestic fittings give specific handling feedback. Touching decor cannot manufacture footage or resolve the scene.

The upstairs ceiling meets the existing partitions, lamps meet that ceiling, books and the borrowed hat have full-height support, and a worn living-room rug joins the armchairs. The saved dressing uses small vacant slices and changes only expected authored cells. It waits for living bodies and actual spectator/borrowed cameras, retains block entities and inventories, and never reruns after its saved checkpoint. Existing cast UUIDs, call-sheet custody, seven-day progress and earlier resolutions remain.

Validation: Java compilation and 721 PNG decodes pass. All 140 Ghost Set fixture states and eight native case/lid states resolve to source models. All seventeen focused native Ghost Set cases pass in 32 seconds, including native two-person case use, finite papers, light switches, divergent meal days, saved dressing and the held-jump return. Native GPU inspection and full exact-source CI remain pending. The mandatory client proof now includes a third page showing four native call-sheet orientations, both case lids and the actual rolling-font caption.

Mod 0.4.88 / protocol 64 / layout 43. 574 declared cases. 44 sources, 33 required resolutions across two kinds, three endings.

## Original artwork provenance

### materials_atlas.png

SHA-256: `c90a4cbfa55592ba006c95ee3a3b1847380c11e52ff759f8cf58baeecfad7794`.

Exact ImageGen prompt:

```text
Create an ORIGINAL Minecraft-style game material texture atlas. Square canvas, exactly 4 columns by 4 rows of edge-to-edge equal square tiles, no gutters, frames, labels or perspective. Every tile must look like a vanilla Minecraft 16x16 texture enlarged with nearest-neighbour: strictly square chunky pixels, a small palette, restrained 1-pixel grain and hand-placed seams, NO smooth photographic texture, painting, gradients, bevel rendering, tiny realistic noise or lighting cast across the sheet. Tiles must have perfectly straight borders at precisely 25%, 50%, 75% of canvas width and height. The 16 cells in row-major order: row 1 (1) faded cream wallpaper with a tiny muted sage floral pixel repeat, (2) medium warm oak floor planks with staggered joints, (3) off-white slightly aged ceiling plaster, (4) four sage-green kitchen tiles with thin darker grout. Row 2 (5) dark walnut vertical wall panelling, (6) warm brown oak furniture wood, (7) worn muted sage laminate worktop, (8) dull slate-grey equipment metal with simple pixel seams and corner bolt dots. Row 3 (9) pale cream enamel, (10) brown corrugated cardboard with a little tape wear, (11) blue denim cloth, (12) muted brick-red cloth. Row 4 (13) muted green fabric, (14) blue-grey bed linen, (15) off-white ceramic with subtle pixel edge variation, (16) damaged cream plaster with a few small brown chips and two irregular crack lines. Domestic 1990s family home, ordinary and worn. Each tile seamlessly repeating where relevant. All pixels opaque. Finished usable material atlas only.
```

### meals_atlas.png

SHA-256: `d3c5132e83526d952bd3d1a290115a160f146a95d3c3e4342529a6db0d9a080c`.

Exact ImageGen prompt:

```text
Create an ORIGINAL Minecraft-style meal texture atlas for native game plates. Exactly 4 columns by 2 rows of equal square tiles on an edge-to-edge 2:1 rectangular canvas, no gaps, frames, labels or perspective. Eight top-down orthographic plate sprites, every plate centred identically in its own tile and occupying the middle 80%. Each is designed as a vanilla Minecraft 16x16 pixel-art tile enlarged with nearest-neighbour: visible square pixels, limited palettes, no anti-aliasing, no smooth realistic painting, no photo detail, no 3D highlights. White/off-white simple pixelated ceramic plates or shallow bowls with light grey rim, transparent background outside the plate, no utensils. Row-major cells: first row (1) brown vegetable stew with little carrot and potato squares, (2) yellow spaghetti with a small muted tomato-red sauce mound, (3) two brown bread sandwich triangles with a little green filling, (4) a shallow bowl of pale yellow soup with a few herb pixels. Second row (5) small roast chicken/brown roast pieces with orange carrot and green peas, (6) chunky pale potatoes with green peas, (7) plain yellow spaghetti with absolutely no red sauce, (8) empty white plate. Keep foods recognisable through coarse native pixel art and restrained domestic colours. Finished usable 2:1 atlas only.
```

### equipment_atlas.png

SHA-256: `1c47a7fadfeccf2582f72cf2ae7bcb3ce983dac415d4fd6460dd6a4291893f34`.

Exact ImageGen prompt:

```text
Create an ORIGINAL Minecraft-style game decor texture atlas. Exactly 4 columns by 2 rows of equal square tiles on an edge-to-edge 2:1 rectangular canvas with perfectly straight tile borders at quarter-width and half-height. No labels, gutters, borders or perspective. Every tile should look like an original vanilla Minecraft 16x16 pixel-art texture enlarged with nearest-neighbour: square chunky pixels, very small palettes, no smooth photography, no painterly gradients, no tiny realistic detail. Row-major cells: first row (1) muted purple cloth with scattered small silver sequin dots, (2) dark charcoal video-camera controls with a small dull green screen and square red recording indicator, (3) dark grey equipment case with a worn off-white tape label carrying only a few simple dark pixel strokes, (4) a child's cream paper drawing of three simple tangled green vines with small pixel leaves. Second row (5) worn reddish-brown book spine with simple cream bands, (6) worn blue book spine with simple cream bands, (7) plain dull white paper with three small dark writing rows, (8) pale ceramic/enamel with a few chips. 1990s domestic documentary crew setting. Entire atlas opaque. Finished usable texture atlas only.
```

### call_sheet.png

SHA-256: `a482ca37db771015114cdbeb1c05f4a14324c301530f523c468bf2745bf69e67`.

Exact ImageGen prompt:

```text
Create an ORIGINAL Minecraft-style paper texture to be used for a documentary call sheet on a lectern. One square, edge-to-edge opaque game texture designed as a strict vanilla Minecraft 32x32 pixel-art tile enlarged with nearest-neighbour: large square visible pixels, limited off-white/cream palette, no photorealistic paper fibers, no shading gradient, no anti-aliasing. The whole canvas is a flat sheet of slightly worn off-white paper. Thin muted grey pixel ruling: three short rows near the top and a simple two-column schedule grid across the middle. A small grey paperclip at the upper left can be two simple angular pixel strokes. No readable text, no lettering, no logos, no margins outside the paper, no extra objects or perspective; legible text is drawn separately by the game. Quiet documentary production prop. Finished usable square paper texture only.
```

