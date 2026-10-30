# The drawn self — 0.4.196

The owner, in the second document of October 30: "The child's drawing would be a good opportunity to let the player select a feminine or masculine body, and maybe what kind of face they want – happy, scared, sad, etc. customization through a child's eyes."

## At the kitchen table
- **The drawing starts unfinished.** It now has a child's big head, the shirt, arms and hands, and nothing more: no face, and nothing below the shirt. A pencil lies down the right margin of the page.
- **The pencil.** Clicking the pencil takes it in place of whatever crayon is in hand, and clicking the margin again puts it back. In the hand it is a yellow pencil with an eraser: a crayon stack with custom model data 1, drawn by `read_pencil`.
- **The face.** With the pencil, a click on the face draws a face. Each further click rubs it out and draws the next, in this order: smiling, sad (with a tear), scared, cross, plain (a straight mouth). The pencil marks sit inside the head's six-by-three middle (`HouseReadKitchen.FACE_MARKS`; the tear is `TEAR`, in blue).
- **The body.** A click on the shirt, or below it, draws trousers. The next click rubs them out for a dress (with stick legs), and so on (`BODIES`, `figure(body)`).
- **Other clicks.** The pencil on the hair or hands only goes over the lines; in the margin it draws a line there.
- **Colouring.** The crayons colour the parts as before, including the trousers or the skirt once drawn.
- **A reminder.** If a reader colours the picture but gives it no face, the voice from the next room says, after fifteen seconds: "Don't forget to give yourself a face. The pencil's right there."
- **Records.** Each stroke is recorded at once in the room's observations (`Mood`, `Body`, `FirstMood`, `FirstBody`). Putting the picture up records the face and body it went up with. The pencil goes back with the picture, and when the reader leaves.

## What it changes
- **Every reader, inside the House, wears what they drew.** `HouseBodies.toned` now sends a human-skinned reader's drawn tone, body and face. Each client remakes that reader's own skin:
  - **Tone.** The skin is recoloured as in 0.4.195.
  - **Body.** A dress gives slim (Alex-width) arms and trousers give wide (Steve-width) arms. The arms are refitted pixel by pixel (`HouseBodyClient.arms`): each arm and sleeve keeps its six faces, and the faces across the arm keep their outer columns, losing or doubling one in the middle. No columns are left empty.
  - **Face.** The lower face is cleared to the reader's own skin colour. The drawn mouth goes on in the colour of their own mouth, with brows for a cross face and a tear for a sad one.
  - **Faces left alone.** A face that is mostly not skin below the eyes (a beard, a mask), or that has anything on the hat layer over the mouth, keeps its own lower face.
- **Inhuman skins.** For a reader with an inhuman skin, the drawn body chooses the House body's clothes directly (dress: the feminine sheet; trousers: the masculine sheet), ahead of the House's own read of their presentation. The drawn face goes onto the painted face the same way.
- **The House's read is untouched.** The drawing sets no `SELF` signal, so the coatroom's read and every address line are as before. The body is the reader's choice of what to wear in the House, not a reading of who they are.
- **Outside, and in the manor, every reader wears their own skin**, as before.

## Literature
- **The account's opening.** `HouseReadProse.kitchen` says what the pencil drew, for example: "drew yourself sad, with a tear, in a dress, at the kitchen table, face and hands alike, and put the picture up on the fridge". It still names no colour. `HouseReadProse.drawn` gives the face and body phrase.
- **The House of Leaves.** The hands leaf (`StaircaseProse.hands`) adds it: "You drew yourself once at a kitchen table, smiling, in trousers, and pressed hard on the hands." `StaircaseProse.Read` gains `drawn`, filled by `StaircaseAccount.read` once the picture is up.
- **The writing proof.** The widest night now carries the widest drawn line, and the leaves corpus carries every face, body and hands combination.

## Rendering
The page (`block/house_read/sheet.png`) is now blank paper. `HouseReadKitchenClient` draws the figure's pencil lines, a dress's legs, the crayon (the same slanted strokes and gaps as the old masks) and the face pixel by pixel from `HouseReadKitchen.figure`. These are the same pixels the server reads a click by. The four `sheet_*` mask PNGs are retired. `tools/house_read_kitchen_art.py` draws the blank page and the pencil sprite, and writes the crayon model's override.

## Tests and proofs
- `HouseReadTests.theKitchenTableReadsOnlyTheFaceTheReaderDrawsAndOpensOnceThePictureIsUp` takes the pencil and draws two faces and both bodies. It checks:
  - the records;
  - the House body's presentation and face;
  - the toned entry for a human skin;
  - the pencil's return;
  - the new prose and leaves phrase.
- `HouseReadKitchenProof` adds a third page, `kitchen_drawn`, which shows:
  - every face on the sheet, both bodies, and the pencil in the margin and taken;
  - the picture on the fridge, and the pencil in the hand;
  - a wide and a slim default skin toned, refitted to the other arm width, and given drawn faces.
- The build job now expects three kitchen proof images.

## What stays
- **Protocol 102, layout 51.** The protocol is bumped because `HouseBodies.Body` gains `face`.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
