# The Last Stop cast (0.4.79)

The five people in the elk bar now have their own skins, replacing the placeholders on Claude's latest 0.4.78 code (`8b76b17`). The native actors retain their phases, UUIDs, dialogue, seats, night movement and saved records.

| Person | Phase | Appearance |
| --- | --- | --- |
| Marla | 4 | Brown skin, dark pinned-up hair, burgundy blouse, charcoal waist apron and black shoes |
| Russ | 5 | Weathered face, stubble, olive trucker cap, blue denim work shirt, dark jeans and work boots |
| Denny | 6 | Short dark hair, moustache, denim vest over a charcoal shirt, bare arms and dusty jeans |
| Wade | 7 | Receding hair, salt-and-pepper beard, faded red flannel, charcoal trousers and brown boots |
| Hollis | 8 | Older pale face, grey hair and moustache, brown cardigan, cream shirt and worn dark shoes |

These are ordinary people at the bar. Their established words and movements provide the threat. Their different faces and clothes keep the three men recognizable outside in the pickup lights.

## Native mapping

The five existing `literary_bar_<name>.png` paths remain. All are 512 × 256 RGBA atlases on the renderer's 128 × 64 logical layout. Its `HumanoidModel` uses the classic head, torso, arm and leg islands; it mirrors the same leg and arm islands for the left limbs. Modern player-skin leg islands do not control these bodies. The native exports conform the generated artwork to those classic islands.

Marla uses the existing body coat mesh to extend her waist apron over the hips. That extension is hidden for the other four bar actors; the earlier stranger and literary cast retain their tailoring. The unused extension regions are transparent. No actor geometry, collision, game rule or saved-world repair is introduced.

`art/inventory/last_stop_cast_0479.json` records each skin's dimensions, phase, SHA-256 and alpha digest. The older approved cast inventory remains exact. The Last Stop generator's missing-file fallback still preserves all five shipped skins byte for byte.

## Verification

All 545 shipped PNGs decode with valid chunk CRCs. The five atlases are distinct and the placeholder helper leaves them unchanged. Native client coverage now decodes each named skin, checks the classic front faces and apron visibility, and renders front, back and dim-light views using the actual baked model. CI requires the three screenshots and the completed proof alongside every earlier native check.

The two failed jobs in 0.4.78 run `38146539916` were the full build and literary gameplay. Claude's following `8b76b17` empties the jukebox before every rebuild so its disc never drops, and makes the field case read the till receipt before its ending. Those fixes are included here. The first 0.4.79 run compiled and passed literary gameplay, but the two package checks still expected a 0.4.78 JAR. They now compare the packaged version to `gradle.properties`; the focused artifact upload follows the same declared version. Exact-source validation is pending; an earlier failure is not treated as a passed release.

Layout 40 / protocol 52. Still 43 eligible sources, 33 personal resolutions across two kinds, and three endings. Use matching client and server versions.
