# The House on the Corner (0.4.408)

The next series' Enríquez proposal ([DESIGN_PROPOSALS_NEXT.md](DESIGN_PROPOSALS_NEXT.md), section 6), built. The owner, on the next series: "Continue with textures and then vignettes ... Don't wait for my approval, just continue." Its open decisions are taken as follows:

- **The story:** "Adela's House", as the proposal recommends.
- **The jars:** kept as close as the proposal itself describes and no closer. There are teeth in one jar, fingernails in another, and in the third something pale nobody can make out. They are small pixel art seen through glass.

The source is in copyright, so only its shape is used:

- no names, no text and no title from it;
- every line here is original.

## The place

A city street at siesta, under the outside sky. Afternoon all day, except on later visits.

- **The street:** the reader comes out of the House's door in the blank side wall of a house. The street runs north between pavements, a road of old stone setts between kerbs.
  - Low rendered houses stand on both sides, with balconies over their doors and their shutters down for the afternoon.
  - Jacarandas in flower stand on the pavements, with their petals in the gutters.
  - There is a kiosk on the west pavement, its papers in the rack and back at five.
  - A dog sleeps in the shade.
- **The cross street:** at the end, the street meets a cross street. Across it, a door in the row of houses faces up the main street. That door is the way on.
- **The house on the corner:** on the far right-hand corner, behind a tall iron fence and a garden nobody keeps.
  - **The front room:** clean and lit, though the power has been off for years. It has patterned tiles, papered walls, a rug, two armchairs and a sideboard.
  - **The boards:** on the wall to the left are boards with glass jars on them.
  - **The corridor:** a door at the back of the room opens onto a corridor three wide. It runs for more than thirty blocks, long past where the house ended outside. A blank apartment block stands over it behind the house, so from the street nothing gives it away.
  - **The last room:** the corridor ends at a last door. Behind it is a room smaller than the door.
- **The kerb:** on the cross street's kerb, facing the gate.
  - A school exercise book lies there; it is the source.
  - Beside it is a pebble. When a reader's account is ready, they alone see a folded paper under it.

## The people

Four people, on stranger phases 131 to 134, with painted skins.

| Who | What they do |
|---|---|
| Romina | A girl with one arm, who loves the house. Every time she is asked about the other arm she tells a different story. Her left arm is not drawn. |
| Nacho | Her brother, a child. "Don't believe her about her arm." |
| Luli | Their friend, a child. "Don't touch the jars. I mean it." |
| Nacho, older | Her brother, grown, on the kerb on visits after the last room. |

A reader can talk to any of them through the House's conversation panel (right-click).

## The days

The street runs one day at a time for everyone on it, on one clock. When nobody has been there for ten seconds, it starts over.

The day the street has is the furthest any unfinished reader there has reached, so friends go on together. A reader's day counts only if they were there for its beat.

1. **The dare.**
   - The children wait on the kerb until a reader comes over (or 30 seconds pass). Romina says: "You're new. Do you want to see it?"
   - The gate opens, and they take the reader through the garden into the front room.
   - Nacho says the electric has been cut since before they were born. Luli says not to touch anything.
   - The door at the back will not open ("It always opens"), and they go home.
   - **Counts:** for a reader in the front room when they leave.
2. **The corridor.**
   - The back door opens. Romina goes to the mouth of the corridor and holds out her hand.
   - **Her hand:** a reader who takes it (right-click) walks with her; she waits for them and draws them gently after her. Letting go is always possible: right-click again, crouch, or walk away.
   - **The others:** Nacho and Luli stay in the front room and call after her.
   - **The last door:** at the end she puts her hand on it and says "Not today", then walks back.
   - **Counts:** for a reader deep in the corridor when she reaches the door.
3. **The last room.**
   - Nacho will not go in, and he and Luli wait at the gate.
   - Romina walks to the last door again, lets go of the hand she was holding ("Wait here"), goes in, and the door shuts.
   - Out in the garden, the grass grows over the path.
   - **Opening the door:** a reader who opens the last door finds the room empty and smaller than the door. It opens for them alone.
   - **Waiting:** a reader who waits outside for twenty seconds sees the front door of the house open onto the street. Nobody comes out.
   - Either way is the end of it for that reader, and their account is ready.
4. **After, on every later visit.**
   - The street is dark for that reader, by a per-reader scene clock, and the house's windows are lit.
   - Nacho, older, sits on the kerb. He tells them what he has been hearing: her stories in the wall behind his bed, the table set for four, the lights coming on at three in the morning, something set down on a shelf.
   - There is one jar more on the boards each time.

## Her arm

Each time she is asked, she tells the next of six stories about it:

1. a dog took it;
2. she gave it to her greedy brother;
3. a train took it in a tunnel;
4. she traded it at a fair;
5. she put it in the river and it went to the sea;
6. she keeps it somewhere safe.

**The cabin's arm.** If the reader lost an arm in the House (BodyLoss), she says "We match" and tells the true story once: a fever, when she was very small. It is the only time she tells it. This uses only the House's real record, and it explains nothing about the house.

## Resolution and the account

**Resolution:** opening the last door, or waiting outside until the front door opens. The account is ready under the pebble and is read after the exercise book. There is one source (`the_house_on_the_corner`, memory).

The account is the reader's memory of her. It says:

- each story about the arm that this reader heard, in order;
- the true story, if she told it to them;
- the front room, and the jars if they looked at them;
- the corridor, if they walked it;
- whether they held her hand to the last door, held it for a while, or didn't take it;
- that she went in;
- what they did: opened the door, or waited outside;
- the garden grown over the path;
- whether they know which story was true.

## Multiplayer

- **Shared:** the street, the house and the children.
- **Her hand:** she walks with one reader at a time, the one holding her hand; the others see that reader take it and see her go in.
- **Each reader's own:** the last room (the door opens for them alone), the choice between opening the door and waiting, the stories they heard and the account.

## Made

- `CornerSite` builds the street whole: the street, cross street, rows, kiosk, jacarandas, lot, house, corridor and block.
- `Corner` runs the days.
- `CornerTexts` holds the exercise book, the stories and the account.
- `CornerRegistry` registers:
  - five materials: render, pavement slabs, setts, patterned tiles and wallpaper;
  - one fitting block (`CornerBlock`) with ten kinds: gate, jar, lamp, window, kiosk, dog, notebook, note, petals and blossom;
  - five sounds: gate, jar, shut, cicadas and hum.
- `CornerPayload` and `CornerClient` carry the per-reader night (scene clock mode 19).
- The art is drawn in code by `tools/draw_corner_art.py`: 29 textures, models and states.
- The sounds are synthesized by `tools/generate_corner_audio.py`.
- The skins are painted by `tools/paint_character_skins.py` and catalogued by `tools/character_art_catalog.py`.
- New builds only.

## Tests

`CornerTests`, three cases in the literary namespace:

- **the dare:** the street, the house and the corridor are built; her stories; the children take the reader in; a jar; the back door will not open; the day counts;
- **the last room:** a reader who lost an arm hears "we match", holds her hand to the last door and opens it, and the account resolves the story;
- **two readers and after:** one holds her hand and opens the door, the other waits outside until the front door opens, and each account is their own; then the street at night, her brother older, one jar more and the windows lit.

## What stays

- **Witness:** 83 sources / 63 resolutions / two kinds / three endings (one new memory source).
- **Layout:** 132 (slot 234).
- **Protocol:** 165.
