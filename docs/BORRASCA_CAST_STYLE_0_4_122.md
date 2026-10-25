# Drisking cast style — 0.4.122

The owner explicitly asks for new NPCs, including Borrasca, to match the established cast. This supersedes the earlier Borrasca exclusion for human paintings. The recent development commits add five human sheets: Kyle, Kimber, the Sheriff, Prescott and a plant worker. Other new room builders reuse existing reviewed human paintings.

All five now use `paint_character_skins.py`: its face construction, grouped skin/hair highlights, garment seams and two authored pixels per Minecraft model unit, with an exact integer export. The production mesh retains standard 64 by 64 humanoid UVs and reads the resulting 256 by 256 PNGs. The separate noise/gradient painter is replaced by an entry point into the shared painter.

The identities remain legible: Kyle's cap, striped shirt and shorts; Kimber's auburn ponytail and freckles, teal jacket and jeans; the weathered Sheriff with his moustache, campaign hat, badge and duty belt; Prescott's grey hair, green cap, red flannel and quilted vest; and the worker's coveralls, reflective vest, gloves, safety glasses and hard hat. The original machine atlases remain exact.

The common art catalog now includes all five on the actual Borrasca humanoid layer. The older 0.4.100 baseline remains immutable, protecting all 649 historical originals, including its Borrasca atlas. Native base-face coverage and pixel-grid verification now also cover the five later human sheets.

The Borrasca GPU review has eleven pages: the prior front/side/back/dim, gait, gesture, fixture, keepsake and bark-name views plus a direct comparison with five established characters on their production meshes (camp cousin, waitress, sergeant, club guest and hotel porter). Both CI paths require the comparison image. The broader character review also includes Drisking.

Local shared-painter, Drisking entry-point and full character-art checks pass. Only these five human PNGs change. There are 146 human catalog entries and 737 native cases; no actor identity, script, movement, original item or progress change. Native render inspection and the exact-source CI matrix are pending.
