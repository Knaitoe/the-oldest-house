# Original artwork — 0.4.30

Twenty inventory icons were separately generated with built-in imagegen, each with a transparent background. [The asset manifest](inventory/assets_0430.json) records each description, shared brief, source hash, native texture path and final hash. Integration crops transparent margin, centers and resizes with nearest-neighbor sampling to a 32×32 RGBA icon. All twenty native item models reference their own texture. Household furniture and note-surface block items retain their existing native block models.

The caged prisoner uses one generated four-material atlas: folded undyed cotton, faded blue hospital cloth, gray trouser fabric and worn brown shoe leather. [materials.png](prisoner/materials.png) is assembled onto exact Minecraft skin UVs by `tools/generate_prisoner_skin.py`; the cloth-covered face has no portrait. The existing child/creature UUID and gameplay remain unchanged.

The mail plaque is generated worn timber bearing “OUTGOING MAIL” and “POST BOOKS HERE,” fitted to the native plank-and-post block model. The great-room rug uses a generated square woven brown/ochre pattern. Native textures live under `src/main/resources/assets/the_oldest_house/textures/`.

The wardrobe side retains its original pixels: its malformed trailing PNG chunk is removed by a lossless re-encode. `tools/verify_textures.py` validates every shipped PNG chunk and fully decodes all textures. Native client proofs render the real prisoner skin and all twenty registered item models.
