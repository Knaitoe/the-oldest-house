# Marjorie's art reference check (0.4.319)

[Build 1451](https://github.com/Knaitoe/the-oldest-house/actions/runs/42276801715), on `107fef1bdbeef268c467a001f86de56b531d9184` (0.4.318), failed in the quick job's asset step. The compile/package and branding steps passed. `tools/verify_character_art.py` rejected the new `ghosts_marjorie.png` because its recorded reference still described the previous painting. Every downstream job was skipped. The local native gameplay tests and model renders had passed, but the complete asset gate had not been run before the push.

The repair records the two requested Marjorie repaints in `art/inventory/ghosts_marjorie_04318.json` and teaches the existing source/JAR verifier to use that record. The historical inventory is preserved. Each entry must refer back to its original image and alpha hashes, retain the original dimensions and match the new PNG exactly. Only Marjorie's normal and dinner sheets can be superseded. Removing the four newly occupied hair UV nets must recover the original alpha hash, so the added hair cannot excuse changes to any original opacity mask.

The Ghosts gameplay, dialogue, hair geometry, scene layout and protocol remain those of 0.4.318. The version advances for the repaired candidate. The source and packaged checks retain every other painting's existing reference.

Validation: the candidate JAR builds successfully, all 78 asset commands from the quick workflow pass locally, and the separate packaged character and branding checks pass. The historical inventory remains byte-identical to 0.4.318. The earlier 82 native gameplay cases and model renders cover unchanged gameplay and textures. Full exact-commit CI remains pending.
