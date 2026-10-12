# Nightclub (0.4.88)

The owner's instruction is to author custom environment textures reminiscent of vanilla Minecraft, retain the mod's painted NPC style, provide appropriate club attire and make decor interactive. A later instruction adds the reversible despair morph when music pauses. This is an indefinitely visitable recurring vignette, not a timed completion challenge.

Layout 43 appends `NIGHTCLUB` at slot 95 without moving any older place. Ordinary dealer pacing includes the room; `/oldesthouse test vignette nightclub` uses the established operator route. No Witness source or resolution is added: 44 / 33 / two kinds / three endings remain. Protocol 60 requires the client presentation for the new registry and synced mood.

The 33-by-50 room uses custom dark brick, worn timber and stepped speaker material. A low entrance partition screens the first view, a dim native green EXIT sign marks the supported return door, and the open floor leads to a raised mixer booth. Eighteen persistent dancers and one DJ have individual gestures and shared beat timing. The original loop is synthesized at 120 BPM for sixteen bars. Muting it stops the sound and morphs the original dancers into despair over about half a second; their feet remain in motion. Resuming it immediately restores their earlier faces and gestures. The native humanoid joints carry connected wrists, elbows and knees; the despair torso rotates around its hips while shoulders and head follow it.

The actual mixer selects normal, soft or silent music; touching a playing speaker makes a positional pulse. Three glasses belong to the physical counters and are finite. Pickup removes the actual block, native drinking produces the empty carried glass, and returning it places an empty prop and consumes the item. The narrow Adventure interaction cannot alter walls or manufacture water. Actual bench and stool seats support native mounting and dismounting. There is no forced departure or resolution lock.

Artwork uses built-in ImageGen output copied directly, with no scripted raster editing or resizing. Earlier painterly environment attempts were rejected. The three supplied vanilla textures were visual references for new custom pixel assets, not shipped replacements for the new wall/floor/speaker IDs. NPC edits retain everyday clothing, identity, complexion and the established painted style.

| Shipped asset | Original generated output |
| --- | --- |
| `textures/block/nightclub_wall.png` | `exec-f8b0282d-76d4-4553-86af-487b52577f90.png` |
| `textures/block/nightclub_floor.png` | `exec-5f1776c6-c5af-449e-8dd1-cc7994f1ab21.png` |
| `textures/block/nightclub_speaker.png` | `exec-8f5266c0-1385-4e6c-bf87-bd79f12b4a51.png` |
| `textures/entity/nightclub_dancer_male.png` | `exec-0858d735-dd24-40b7-b17f-02e624dcb88a.png` |
| `textures/entity/nightclub_dancer_male_despair.png` | `exec-1ef3a42c-489a-42e8-b2b3-7fc519f59a1c.png` |
| `textures/entity/nightclub_dancer_female.png` | `exec-db956ab8-fc04-4822-bc2b-36e14747e2f9.png` |
| `textures/entity/nightclub_dancer_female_despair.png` | `exec-7073cffb-baa5-4c6d-9178-c30565f02826.png` |
| `textures/entity/nightclub_dancer_vest.png` | `exec-bab89933-f5d5-4339-b77b-b6fdd921ee71.png` |
| `textures/entity/nightclub_dancer_vest_despair.png` | `exec-ae718fac-541d-48b4-a17c-74e05e544c0f.png` |
| `textures/entity/nightclub_dancer_copper.png` | `exec-70a2ac5b-ccd9-4558-9e5f-b32b5a4cea7c.png` |
| `textures/entity/nightclub_dancer_copper_despair.png` | `exec-443008de-d033-4cde-a152-e534a6c2201d.png` |
| `textures/entity/nightclub_dancer_mustard.png` | `exec-f659a5b1-6886-4bbc-9b17-c7c977c2ad2c.png` |
| `textures/entity/nightclub_dancer_mustard_despair.png` | `exec-1c88ea4b-f3b3-4b55-aaf0-76045b5ad120.png` |
| `textures/entity/nightclub_dancer_silver.png` | `exec-955a75c5-d9a2-4665-b83b-8e5344221b1f.png` |
| `textures/entity/nightclub_dancer_silver_despair.png` | `exec-dc1fcb1a-d48b-47e6-9aca-ca21a36c2d51.png` |
| `textures/entity/nightclub_dancer_brick.png` | `exec-47efa412-c5f9-436e-997e-38a0b0895960.png` |
| `textures/entity/nightclub_dancer_brick_despair.png` | `exec-0aa9fbc9-ed22-4240-8368-2ea28cbdec23.png` |
| `textures/entity/nightclub_dancer_braids.png` | `exec-05820bfc-b4d4-40bb-85f8-ac0d220e92b8.png` |
| `textures/entity/nightclub_dancer_braids_despair.png` | `exec-cfafb2ba-9d0c-4a8c-811b-078194ce37fb.png` |
| `textures/entity/nightclub_dj.png` | `exec-9b18ea91-b257-42b0-8f88-aeb779650377.png` |
| `textures/entity/nightclub_dj_despair.png` | `exec-af4f2f8a-9a3d-40bb-abe1-858555d2a74f.png` |

Environment prompt directions: flat seamless game tiles, logical 16-by-16 coarse square pixels, crisp stepped details, restrained five- or six-color charcoal/brown palettes, no photo lighting, blur, painterly noise or realistic texture. The cast has eight distinct dancer looks and a dedicated DJ, with paired normal/grief atlases. Male attire: charcoal graphic T-shirt, black jeans, boots and a silver chain. Female attire: muted plum tank, charcoal short skirt/tights, boots and simple jewelry. Final despair edits retain the same person and clothing with lowered lids, unfocused eyes, restrained under-eye shadows and relaxed lips. The early gaping-mouth/raised-brow variants were rejected as caricature at the owner's instruction. Body animation carries the grief through a bowed torso, dropped shoulders and a subtle head tremble; the deformation is reduced to ten percent vertical and 3.5 percent horizontal. The original male normal and restrained grief outputs use 80-by-40 normalized UV layers; the other eight designs use the supplied 128-by-64 layout. The vest and brick-red male materials were corrected to flat chest shading and the red tank retained its club-appropriate cut; their grief variants derive from those corrected files. Native GPU inspection of all eighteen cast atlases is still required.

`tools/generate_nightclub_assets.py` emits fourteen native block meshes, two item models, 132 oriented states, language/sound references and the original OGG loop. It preserves the supplied PNGs. `NightclubVisualProof` renders the registered materials, both UV choices, club/existing cast comparison, joint poses, progressive despair and return, full/empty glass models and every prop state into four native screenshots. CI runs it in the existing rendering job. Source batches continue while full CI runs.

Local validation: Java compilation passes; both targeted native GameTests pass in 21 seconds after the nine-look cast expansion; every custom club texture/model reference resolves; all 714 shipped PNGs decode and have valid CRCs. Exact-source full gameplay, architecture, GPU and multiplayer checks remain pending. These local results do not certify a released JAR.
