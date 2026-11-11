# Bike night: what the three wear (0.4.480)

The owner, on seeing the three who come to the park (0.4.479, `docs/BIKE_NIGHT_BULLIES_0_4_479.md`):

> give the bullies more contemporary clothing please. skull with fire eyes on a t-shirt, hoodie, etc.

They had been painted in a cut-off denim jacket, plaid, and a sweatshirt. This release repaints the three and changes nothing they do.

## The three

- **Tully.** A black T-shirt with a skull on the chest, bone on black, a flame in each eye socket: orange, with a yellow heart, its tip up over the brow. Short sleeves. Blue jeans out at both knees, the threads still across. White sneakers. The second sheet, Tully in Gus's cap, is the same Tully under the same red cap.
- **Ray-Ray.** A maroon hooded sweatshirt: the hood's opening at the neck, two pale cords, the pouch across the front, the hem; on his back the hood lies on his shoulders. Dark track pants with a pale stripe down the outside of each leg. Pale sneakers.
- **Duff.** A green football jersey with a pale band round the collar, stripes over the shoulders and two round each sleeve, and the number 66 large on the front and the back. Under it a long-sleeved shirt to the wrist. Khaki trousers and dark sneakers.

Their faces, hair, sizes and everything else are as they were.

## How

The shared painter (`tools/paint_character_skins.py`) paints every human sheet from one row. A row gains six things, each nothing unless it is asked for, so every other sheet paints exactly as it did (the painter's `--check` holds all 319):

| | |
|---|---|
| `print` | What is printed on the chest. `skull_fire` is the skull, eleven pixels across. |
| `jersey` | A jersey's number, in the entry cards' figures at twice their size, front and back. |
| `under` | The colour of a long sleeve worn under a short one. |
| `stripe` | The colour of a stripe down the outside of each leg. |
| `rips` | The knees out: skin through the cloth, and the threads. |
| `shoes` | Sneakers in a colour, where a sheet does not have the plain dark shoes. |

And two styles of top: `hoodie` and `jersey`.

## The review sheets were mirror images

The character review (`client/CharacterArtVisualProof`) draws each body front and back. It drew them straight into the screen's own space, whose y runs down, which is the mirror of the world's: every sheet was a mirror image of what the game shows. On a body the same on both sides nobody could see it. Duff's 66 read backwards on the sheet, and so did the letter on Gus's cap, while both read correctly in the game. The review now turns each body over across x, and its sheets show what the game shows. The game's own drawing was never wrong and is not changed.

## What did not change

No actor, phase, size, gesture, line or rule. No block, item, sound or packet: protocol and layout are as they were. Witness is unchanged.

## Checked

- The painter's own check, all 319 sheets; the quick gate.
- The character review (41 pages) and Bike night's own (17), drawn by a real client: the three on the model, front and back.
