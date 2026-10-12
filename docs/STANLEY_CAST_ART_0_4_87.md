# Stanley cast art prompt set

These six project textures were edited with the built-in ImageGen tool. Each edit used that role's earlier Stanley atlas as the target. `literary_bar_marla.png` and `literary_paw_walter.png` were style references for the bartender; subsequent roles used Marla and the revised bartender as style references. References supplied style only; the hotel retained its own wardrobe, meshes and UV layout.

## Shared prompt

> Use case: style-transfer. Asset type: production Minecraft character diffuse UV atlas, not a character illustration or render. Bring the hotel NPC into the same detailed painted pixel-art visual language as the existing NPCs. Paint distinct human eyes, brows, nose, hair strands and restrained skin shading inside the existing face islands. Add muted fabric weave, tailored seams, folds, cuffs, shoe wear and stitching comparable in detail and contrast to the references. Keep the target's 2:1 canvas, exact normalized UV island positions, proportions, occupied surfaces, role and garment colours, and transparent unused areas. The original face front is x32..63, y32..63 on the 512 × 256 target. Head, body, arms, segmented elbows, knees, fingers, apron, coat tails, cap, skirt and small accessory regions stay at their target coordinates. No padding, borders, labels, grid lines, checkerboard background or 3D preview. Avoid flat enlarged eight-pixel placeholder faces. Return a usable RGBA texture with actual transparency.

## Subjects

| Asset | Subject prompt |
|---|---|
| `stanley_porter.png` | Adult porter in muted burgundy with aged gold braid, ivory collar, worn dark shoes and a matching cap; groomed face and reserved expression. |
| `stanley_bartender.png` | Restrained middle-aged bartender with dark hair and a small moustache, charcoal period jacket, ivory shirt and worn cream apron; retain bottle, glass, cloth and finger patches. |
| `stanley_scarred_guest.png` | Tired adult male guest with subtly asymmetric brows and a healed diagonal facial scar, olive-taupe period suit, ivory collar and worn dark shoes; no open wound or blood. |
| `stanley_pianist.png` | Older male pianist with swept grey hair, lightly aged features, charcoal tailcoat, ivory shirt, muted brass buttons and worn shoes; reserved concentration. |
| `stanley_dancer.png` | Adult male dance partner with tidy brown hair, dark brown evening tailcoat, ivory shirt, worn dark shoes and a distant expression. |
| `stanley_dancer_dress.png` | Adult female partner with dressed dark hair, clear eyes and brows, muted mauve evening dress, tailored folds, understated collar, dark shoes and a composed distant expression. |

Two targeted refinements made the guest's healed cheek scar visible and filled the pianist's lower coat rectangle (logical x64..95, y30..63) with continuous opaque charcoal wool, preserving all other UV regions. The reviewed files were copied directly into the project without raster resizing or compositing. Native front, action and comparison renders are the final fit check.
