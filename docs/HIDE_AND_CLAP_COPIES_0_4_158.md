# Hide-and-clap, private with copies — 0.4.158

The owner's October 29 notes: "hide and clap might work better as a private vignette too", and "Every private vignette should be given copies… if the diner and confession don't do that already, they should."

The diner and the confession already had copies (0.4.153), as did every other one-at-a-time private scene by 0.4.155 (`docs/SCENE_COPIES_0_4_153.md`). The private sites (the backseat, the silo and Borrasca), the home copies (the family copy and the old cabin) and the Red Room were already each reader's own. Hide-and-clap was the one scene left: held back from groups because it has one blindfold, and kept as one game for the whole world.

## What changes for readers

- **Private.** The game is played blindfolded and alone, one reader to a room. A group is never led into it (it was held back from groups before; now it is a private scene).
- **A room of their own.** A reader who finds the room taken gets a copy of it, exactly as at the other private scenes. They keep it until they have won the game there.
- **Everyone can play it once.** Before, the game was the world's: the first reader to reach the door reserved it, and once anyone opened the wardrobe it was won for everyone. Later readers could only examine the open wardrobe for the aftermath's Witness credit. Now each room plays its own game, and every reader can win it once, in their own room, for the game's own credit.
- **The game itself is unchanged.** The reservation on entering, retreat before the blindfold, the blindfold's commitment, the minute, the counting, the claps, the feet, the wardrobe that appears behind the reader, the crayon drawing, the reveal, and death with everything dropped (even with keepInventory) all work as before, in whichever room.

## Which room a reader gets

The rules are those of every scene with copies (`docs/SCENE_COPIES_0_4_153.md`):
- **The first reader** takes the original room, if nobody keeps it.
- **A reader in the middle of the world's game before 0.4.158** keeps the original, with their round.
- **The original once won.** If anyone has already won the world's game, the original keeps its open wardrobe for good. Nobody is given it again; new readers are given copies. A reader who already kept the original from before can still examine its wardrobe for the aftermath's credit, as before.
- **A copy whose reader has won** is passed to the next reader who needs one, put back as new: the wardrobe comes out, the blindfold goes back on the wall, and its round starts from the beginning.
- **A reader who fails** (the minute runs out) keeps their room and can try again there. The room is reset at once, as before.
- **Dealing.** The game is dealt to every reader who has not won it, and to nobody who has. For a reader who has won it, its door is only a passage.

## How it is built

- **`HideAndClap`** keeps one round per room, keyed by the room's copy (`GAMES`), with the feet (`FEET`) and the frames still to hang (`PENDING`) per room too.
  - The original's record stays the world's (`hide_and_clap` and its completion). A copy keeps its own under `hide_and_clap@<copy>`, with its own completion.
  - Rounds are restored from the save once for each save.
  - Every way in finds its room: arrival, the door, the wardrobe and the frames by where they are; the blindfold, the lock, logging in and out, death and respawn by the reader's own round.
  - Each reader's failure, and where they wake after it, stays in the world's record.
  - The room's build uses the House's recorded construction, so a copy is laid out in slices; its frames go up after the wall they hang on (`BuildBlocks.after`).
  - `freshRound` puts a passed-on copy back as new; the old wardrobe comes out once the room is loaded.
- **`SceneInstances`:**
  - the room has copies, built by its own builder with the House's furnishings;
  - a reader keeps a copy until they have the game's credit;
  - only a reader in the middle of the world's round keeps the original from before;
  - a won original is the world's;
  - a passed-on copy is put back as new.
- **`SharedVignettes`:** the game joins the one-at-a-time private scenes; nothing is held back any more.
- **`LabyrinthDealer`:** the game is dealt per reader (`HideAndClap.canDeal`).
- **`LabyrinthDoors`:** arrival starts the game in the room the reader stands in.
- **`WitnessAccount`:** the "someone reached the end before you" note is no longer given to a reader in a copy of any scene, where their story is their own.

## Tests

One native case joins the co-op cases (the quiet-room job runs 27):
- the game is a private scene with copies;
- the first reader takes the original and reserves its game, and a second waits for a copy;
- the copy has its own blindfold on the wall;
- the second reader's door opens onto the copy, and each room keeps its own round;
- the blindfold binds the second reader's round alone;
- following the copy's own claps leads to its own wardrobe;
- winning there finishes that room's game, not the world's: the winner has the credit and is let go, and the first reader's round in the original is untouched;
- the winner's door is a passage, and the others are still dealt the game;
- with the winner looking on as a spectator and no further copy allowed, the next reader is given the won copy, put back as new (the wardrobe out, the blindfold back on the wall), and plays its game from the start.

The original-room cases (the cloth's release, reservation and retreat, failure and the Mother, the room's build and the wardrobe's place) pass unchanged.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings.
