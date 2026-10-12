# Holy Rabbit art (0.4.88)

Artwork uses the built-in ImageGen tool, with direct copies of its output PNGs and no scripted raster resizing or editing. The supplied healthy Father was edited into the two injury states. The existing painted Marla skin provided the style reference. The Father remains Native American across all states, in worn everyday winter clothing; no tribal identity, regalia or author likeness is specified.

| Asset in `src/main/resources/assets/the_oldest_house/textures/entity/` | Original generated output |
| --- | --- |
| `literary_father.png` | `exec-9a340b37-0528-4533-84b2-1fda0d545a70.png` |
| `literary_father_bandaged.png` | `exec-950935c1-c938-41a7-936d-c6f110f2bf31.png` |
| `literary_father_mangled.png` | `exec-8b0f94fa-c8a0-4283-87bd-bd2a29cd148f.png` |
| `literary_snow_rabbit.png` | `exec-74bf4a5c-b8fa-4237-afef-48dde161335a.png` |

All four outputs are 1774×887. The three Father skins fit the established normalized 128×64 human UVs. The injured bandage is a left-leg child of the existing humanoid model; the two native trouser legs still share their original base UVs. The asset generator preserves the painted Father files instead of replacing them with its placeholders.

Father prompt directions: match the established painted NPCs; a weathered Native American father with medium-brown complexion, dark eyes and dark hair, ordinary worn brown-olive winter coat, wool shirt, dark trousers and boots; preserve exact UV islands, face and hands across edits. For the bandaged state, change only the reserved upper-right accessory patch (logical 112–127, 0–15) to worn tied linen with restrained stains. For the final state, tear and dirty that same bandage patch around a dark red-brown injury; no detached limb or anatomical closeup. Preserve the same father and clothing elsewhere.

The generated rabbit UV-atlas attempts were rejected because they moved native UV islands. The shipped rabbit asset is instead a uniform painted fur material, wrapped by the unchanged vanilla RabbitModel. Native geometry places quiet dark eyes and muted pink nose/inner ears precisely and follows the existing head/ear transforms. Only the saved owner-marked Holy Rabbit receives this art; normal rabbits use the original renderer textures. No saved animal is replaced to acquire the skin.

Exact final rabbit-material prompt:

> A seamless game texture of fine ivory-white winter rabbit fur, covering the entire image edge to edge. 2:1 landscape bitmap. Flat evenly lit surface, tiny soft directional hairs, understated hand-painted realism, ivory with delicate warm-gray shadow strands and a few faint natural taupe fibers. This will wrap around a low-poly rabbit; keep the pattern subtle and uniform at game distance. No rabbit portrait, no face, no eyes, no ears, no body silhouette, no UV diagram, no separate pieces, no background, no border, no transparency, no lighting vignette, no large brown patches. Only a consistent fine winter-fur material, seamless on all four edges.

Native GPU views `native-holy-rabbit-cast.png` and `native-holy-rabbit-motion.png` use the registered human model, its actual injury selector and motion method, the vanilla rabbit joints and the same fitted markings layer. Release still requires their successful run and visual inspection; texture generation alone is not visual verification.
