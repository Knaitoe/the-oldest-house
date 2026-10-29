# The owner's additional notes — 0.4.164

The owner sent a document of playtest notes with screenshots (October 29), then asked: "Ensure your changes to the cousins and goatman's flee work with multiplayer too". This version answers each note.

## Movie Night: the floating life preserver

> "floating artifact." (the owner then: "it's the life preserver that's floating.")

- **The cause.** The life ring hung on its lake side a hand's width off a thin oak fence post at the dock's edge. A fence post is four pixels wide in the middle of its block, so the ring hung in the air beside it, out over the water.
- **The fix.** The post is now a solid stripped-oak piling two blocks high, and the ring hangs flush on its lake side.
- **Saved lakes.** A lake built before this version has its post replaced by the piling the next time a reader arrives.

## Lecterns on tables: each scene's own reading stand

> "podiums like this need new textures suited to their vignettes."

Six scenes left their paper on a vanilla lectern standing on a table. Each now reads from a stand drawn for that scene by `tools/draw_reading_stands.py`, at Minecraft's own sixteen-pixel scale.

| Scene | The stand | Its paper |
| --- | --- | --- |
| Movie Night | a little pine easel with the printed program for *Teeth of Summer*, and a stack of programs in front | Ray's "Tonight on the lake" |
| Costume Night | a clipboard on a stand with the judge's scorecard: twelve entrants and the thirteenth circled, the judge's pencil beside it | Hank's judge's card |
| Winter Lake | a note folded small and opened out on the stump, held down by a stone, with snow in its creases | Bud's note passed in class |
| Winchester | a mahogany book rest with brass corners, the ledger open on it, a red ribbon hanging from its edge | the construction ledger |
| The Ghosts set | a black script stand; the client still draws the day's call sheet on its desk, where a lectern's top was | the call sheet |
| Borrasca | the county's green leaflet box at the trailhead, a trail leaflet in its pocket (three pines from one root, the dotted walk to Marker Two) | the Triple Tree trail leaflet |

How it works:

- Each stand is still a native lectern underneath (`ReadingStandBlock`), with the lectern's own block entity holding the scene's original book. Every reading, archive and test of the source is unchanged.
- The vanilla open book is no longer drawn over a stand.
- The stands face the way a reader comes to them.
- Scenes built before this version keep their lecterns until they are next built.
- Scenes that already had a themed reading object since 0.4.55 keep it.

## Stacey getting from the water onto land

> "Stacey seems to have problems traversing from water to land. Maybe from land to water too. This is evident in movie night and the costume contest."

- **The cause.** Afloat, she is not standing on the ground, so Minecraft's step-up never applies. At a bank, a dock edge or a beach shelf she pushed into the face and stopped.
- **The fix.** When the ground ahead of her is higher, she now lifts up to it (at most six tenths of a block a tick) before moving forward. From land into the water she was already able to drop.

## The barn well: the farm rebuilt as one build

> "Improve clutter in barn_well shed." / "Fix fencing in barn_well barn" / "Give barn custom texture? Fix the mess it is right now."

**What went wrong.** The farm was the old room with seven later passes laid over it, each patching the one before. The result was:

- the pens' rails stood in the barn's own wall line, so the barn's walls hung over a fence at their foot;
- a beam floated beside the well;
- spruces were planted at the barn's eaves and inside the field;
- roofs stacked on roofs.

**What changed.** `BarnWell` now builds the whole farm in one pass, and none of the old passes touch it. Those passes were polish, composition, architecture, shells, support repairs, playtest repairs and the old farm layout.

- **The barn.**
  - New custom textures (`tools/draw_barn_well.py`): red board-and-batten siding, white corner boards and header, four-pane windows set high, and two X-braced doors slid back along the wall beside a four-wide doorway.
  - It stands on a stone footing and has a dark-oak gambrel roof with a board's overhang at each end.
  - High in the south gable are a hay door and its jambs, with a hoist beam run out from the ridge and a chain.
- **The fencing.**
  - The two pens are inside the long walls, never on them.
  - Each pen is closed by a rail and a gate on the aisle side and a rail across its front.
  - The rails join their neighbours, the walls and the loft's posts, and stop a body.
- **Inside the barn.**
  - A loft over the front half, on a beam and two posts, with hay stacked under the eaves and its low rail.
  - A trough in each pen and the east pen's water.
  - A harness and a pitchfork on the back wall, and lanterns hung from the ridge and the loft.
  - By the door: the feed barrel (in the same place), hay bales, milk cans and seed sacks.
  - A barrow left on the apron outside.
- **The shed** keeps its place, and the onward door in its back wall.
  - It has grey weathered board walls and a half-pitch slab roof.
  - Things a working shed keeps: a workbench with its vise and a jar under a pegboard of garden tools, a potting bench with seedlings, seed sacks, milk cans, a harness by the back door, a pitchfork, a composter, and the barrow under the window.
  - The way through from the door to the back door is kept clear.
- **The well head.**
  - Two posts carry a small gabled roof.
  - The windlass spans between them three blocks above the mouth, so a reader climbs clear of the shaft under it.
  - Its rope runs down to a bucket resting on the curb.
  - The shaft, its ladder, the cover, the initials, the paper and the ribbon barrel are exactly where the story keeps them.
- **The grounds.**
  - The field is fenced, with its gate, irrigation channel and a scarecrow.
  - The chicken yard has continuous rails and a gate.
  - There is one old oak in the yard.
  - The paths run from the gate to the barn, the shed and the well.
  - The wood on the bank around the farm is as before.
  - Nothing stands on a path block, which is a pixel low.
- **The stock is unchanged:** the seven animals in their pens, the shared dog and cat, the finite feed barrel and the ribbon.
- **Saved worlds.** New farms only. An older farm keeps its old layout and its old passes.

## Goatman

> "Npcs squished together in goatman"

- Cousins coming in through the door now queue for it instead of all taking the same line.
- At the fire they have two more places to stand.

> "The buns should begin on the table, not spawn in after the player has plated a frank. Interacting with a plated frank with the bun should put a hot dog in a bun on the plate, not deliver it back to the player's inventory."

- The buns are on the table from the start of the evening.
- Using a bun on a plated frank makes a hot dog in a bun on that plate.
- A double click cannot take two buns.

> "the following-kid spawns in before the player and waits for them to move before following. This should not spawn in until the player has already started to walk. Ensure you add sound effects for their footsteps..."

- The girl on the trail now appears only once the reader has started walking, out of sight behind them.
- Her footsteps follow behind them, private to that reader.

> "Cousins should BEGIN outside... the table should also be cleared on re-entry."

- A reader who comes in through the door with nobody else already in the evening begins a fresh one.
- In a fresh evening every cousin starts outside, by the fire or in the yard, and the table is cleared.
- Anything of the last evening that was not loaded is cleared as soon as it is.
- A second reader who arrives joins the first reader's evening instead of starting another.

> "When the goatman flees, disguised as the child upon the cousin's return – the sequence needs to be better timed. The goatman should also have a custom run animation for that sequence. Maybe head down, crouched low, hands almost touching the ground? Like an animal squabbling away."

- The thing reaches the door a moment (twelve ticks) before the cousin coming back. It is out past them on the porch instead of meeting them in the doorway.
- If the door was shut and opened late, the two still cross together.
- The thing runs out in a new animal scurry: head down, hips low, body pitched forward and hands almost on the ground, its limbs reaching by inverse kinematics. The scurry lasts only for the dash out to the fire.

> "The cousins' stomach sleeping animation has their head tilted too far up. The back-sleeping animation has their hands raised unnaturally."

- A cousin asleep on their stomach now lies with the head turned on the pillow.
- A cousin asleep on their back has their arms lowered along the sides.

> "When the player lays down to 'sleep'... the screen should fade as it normally does. Then it should re-open to a clear screen – no chat log – when the goatman first stands to laugh."

- Lying down darkens the screen as sleep always does.
- When the thing first gets up to laugh, the dark lifts over a moment onto a clear screen. Neither the chat nor the bed's chat box and button are drawn.

## Multiplayer

The owner: "Ensure your changes to the cousins and goatman's flee work with multiplayer too".

- **The scurry** is saved on the creature and synced to every client, so every reader sees the same body run low. That includes the path where a reader went for gas in the runner's place.
- **A fresh evening** begins only when no reader of the evening is already in the camp. A second reader through the door joins.
- **The trail girl and her footsteps** are each reader's own.
- **The bun guard** is per reader.
- **The bed screen** clears on each sleeping reader's own client.

## The CI failure on 0.4.163

Run 1329 failed only in the Borrasca two-client proof: both clients timed out when the server stalled for 36 seconds at its phase 54. The stall dump showed the cause:

- Once a second, a check read the floor of a scene that was not loaded (`SceneExteriors`). That loaded the scene's chunk on the server thread.
- The check now waits until the scene's chunks and their entities are already loaded, and never loads them itself.

## Tests and proofs

- **Goatman.**
  - New: a door arrival with nobody inside begins a fresh evening outside, and the next reader joins it.
  - The exchange case checks the new timing: through the door twelve ticks before the cousin, or together after a late-opened door.
  - The gas case checks the scurry on the reader-runner path.
  - The bun and trail cases are updated.
- **The farm.**
  - New: the farm built whole is never laid over. Every older pass is run again, and the farm must stand exactly as built, the original paper and the ribbon barrel included.
  - New: the barn's pens, the shed and the well stand as one build. It checks:
    - the walls down to their footing;
    - the rails joined;
    - the windlass clear of the climb;
    - every farm thing on a solid top or flush on a solid face, and every lantern held.
  - Updated: the stock and pets case and the rails case, for the new pens.
  - Updated: the staircase's composed-roofs case no longer includes the barn.
- **The native art proof** in the quiet-room client run adds three pages:
  - the six stands on their scenes' own tables, from the reader's side;
  - the same six from behind;
  - the dock's piling with the life ring.
- **The window-entry proof** adds two scurry views.
- **CI** checks both new art tools.
- **Locally** these pass:
  - the Goatman and multiplayer suites, 138 cases;
  - the literary, Ghosts and Borrasca suites, 105 cases;
  - the farm and well cases.

No protocol, layout or Witness change: 54 / 41 / two kinds / three endings.
