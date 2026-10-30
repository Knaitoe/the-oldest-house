# Roderick, the storm and the fall — 0.4.182

Stage B of the Usher pass. Stage A ([USHER_0_4_181.md](USHER_0_4_181.md)) built the house. This stage adds its people and its last night. Everything here is each reader's own, and none of it changes a block for anyone else.

## Roderick

- **Where he is.** He sits in his chair in the studio, facing the doors. He is a private actor bound to the reader, on stranger phase 38 (`UsherStory.RODERICK`), and he turns his head toward the reader.
- **His skin.** `literary_usher_roderick.png` is painted by `tools/paint_character_skins.py`:
  - a cadaverous face;
  - hair that floats rather than falls;
  - a frock coat with a pale stock at the throat.

  It is added to the character art catalogue and drawn in the native character review.
- **His seat.** The renderer seats an actor in his chair, as it does at the bar's stools.
- **Talking to him.** Right-click him and he speaks; his lines follow the night:
  - **Before the lid:** his letter, the light and the music that hurt him, the tarn that holds the house upside down, the stones, and his sister to be kept below a fortnight.
  - **After the lid, the same visit:** he will hear it, either way.
  - **The storm night, closed lid:** "You hear it too." He asks to be read to, from the old romance on the floor by his chair.
  - **The storm night, open lid:** she has been walking the wing all night.

## The storm night

- **When.** Every visit after the lid is chosen is the storm night, for that reader.
- **The glow.** The vapour about the house glows behind the windows: each window's glow stage, lower and upper, is sent to that reader's view, coming and going.
- **The rest of the weather.**
  - the casements rattle;
  - a low sound moves through the house;
  - a faint exhalation drifts over the tarn.
- **No lightning.** Poe's storm night has none: the light comes off the house.

## The reading (closed lid)

1. **Starting it.** A hand on the strewn books in the studio begins the romance. Before the lid is chosen they are only damp old books.
2. **The passages.** There are three, each original to this room, given to the reader in grey italic.
3. **The answers.** Each passage ends on the sound the vault answers with, sixty ticks later and from below:
   - splitting wood;
   - a long grating cry;
   - a ringing of metal.

   Roderick speaks after each.
4. **The door.** After the third: "We have put her living in the tomb. I tell you she is standing outside the door."
   - The studio doors stand open in the reader's view.
   - A private Madeline stands on the threshold, reeling.
5. **The fall.** She goes to him, and the two go down together.
   - The fissure on the front opens wide, with the moon's light through it, for that reader. This uses a new full-bright stage of the fissure, drawn with NeoForge's model block light.
   - The windows glow, and the house groans.
6. **The flight.** Getting back over the causeway to the bank readies the account: *fled_as_the_house_fell*.
   - The account tells the reading, the three answers, the doors and the crack.
   - It is paged.
7. **After the fall.** Roderick is not in his chair again for that reader.

## Leaving and the open lid

- **Leaving mid-reading.** A reader who leaves forgets the reading and its Madeline; the next visit begins the night again.
- **The open lid.** It keeps its ending: she is found standing in the wing (`found_her_outside_the_open_coffin`), with the storm about the house.

## What stays

- **The shared parts.** The lid choice, the coffin's woman, the scratching and the vault's cracks are as before.
- **The co-op vault account.** Its lines are unchanged. The new outcome counts as a closed lid, as `returned_to_the_closed_lid` did.
- **Witness.** Still one understanding source: 55 / 42 / two kinds / three endings.
- **Layout and protocol unchanged:** layout 51, protocol 101.
- **New builds only.**

## Tests

- **`LiteraryTests.theStormNightsReadingIsAnsweredFromTheVaultAndTheReaderFleesAsTheHouseFalls`:**
  - Roderick sits in his own chair, the reader's own, and answers when spoken to;
  - the books do nothing before the lid;
  - after a closed lid and a return, the books open the romance;
  - each of three passages is answered;
  - she stands at the studio doors, the reader's own, and the shared woman is untouched;
  - she and Roderick go down together, and the house's own fissure blocks are unchanged;
  - reaching the bank readies *fled_as_the_house_fell*, whose account tells the reading and the flight;
  - he is not in his chair again.
- **`nativeCoffinChoiceRequiresAReturnAndKeepsTheSameWoman`:** the closed lid's return now brings the crack and the storm night; the ending waits for the reading.
- **The native character review** draws Roderick front and back.
