# The advancement panel — 0.4.165

The owner: "can you work on an advancement panel next?" This version builds [ADVANCEMENTS_DESIGN.md](ADVANCEMENTS_DESIGN.md) with the owner's answers:

- two tabs;
- "all of the endings should be hidden.";
- story descriptions: "your call." They are in the account's own "I";
- the five kinds hidden until earned.

The owner also asked, "When you're free", for the waiting room's wait to go back up: "15 seconds whizzes by with the amount of detail you've added." It is 40 seconds again (below).

## The two tabs

Minecraft's advancements menu (`L`) has two new tabs.

1. **This House Has Ghosts in It.** The journey, 25 advancements. Its background is the manor's damask wallpaper.
   - The way in: the letter, the manor, the room between rooms, the impossible hallway, the far door.
   - Depth: six doors deep, something below (the growl), stone, twenty doors deep.
   - The great staircase: the explorers' camp, your own book, the first fire, five fires, the cell.
   - The three endings: the Witness, the wound, locked out.
   - What the reader keeps and loses along the way: a stray, a companion through a door, the Mother of Strays, something bought back from her, chalk, the spool, the House's letters, the first-death portrait.
2. **An Account.** 66 advancements, on the account's paper.
   - The account itself, then the progress: the first story heard to the end, a second kind of ending, five rooms, twenty rooms, enough for the Witness, every room.
   - The five kinds of ending (`kind_survival`, `kind_understanding`, `kind_memory`, `kind_connection`, `kind_release`). Each kind has its stories under it.
   - One advancement for each of the 54 stories in the Witness pool. Its title is the story's account title. Its description is the first sentence of that story's account, in the reader's "I". So the tab reads as an index of the reader's own account.

The titles and descriptions are exactly the design's tables.

## What is hidden

Hidden advancements show nothing until they are earned.

- **Every ending:** the Witness, the wound and locked out.
- **The five kinds** and **every story.**
- **Every room** (the last step of the account's progress).
- **The deep and the dark:** the growl, the camp, your own book, both fire steps, the cell, the Mother, buying something back from her, and the portrait.

Everything else shows as a grey outline once its parent is earned, as Minecraft does.

## The rules

- **Personal.** Each reader earns their own. In a shared scene, a witness earns a story's advancement exactly when the Witness record gives them that story, in whatever role it credits. Another reader's act gives a peer nothing.
- **Nothing in chat.** No advancement announces to chat, so nobody learns from chat what another reader finished. The reader still gets the toast.
- **Nothing paid.** No experience, items, loot, recipes or functions. The House's finite supplies stay finite.
- **The quiet rooms and the House's read are never advancements.** Nothing a reader does in the five quiet rooms, and nothing the House reads of them, appears in the menu.
- **Spectators** earn nothing.

## How it is awarded

- **From the save.** Every advancement except chalk and the spool has an `impossible` criterion. `HouseAdvancements.sync` awards it from a record the House already keeps for its own reasons (`HouseFacts`). It runs:
  - when the reader logs in;
  - every two seconds while they play;
  - at once when a story resolves for them.
- **Existing worlds.** A tester's menu fills with their saved history the first time they log in, and only once. Syncing again awards nothing twice.
- **What each journey step reads:**

| Advancement | The saved fact |
| --- | --- |
| letter | the opening has delivered the letter (or the reader has already been inside) |
| manor | the reader has seen the manor (or the opening records them entering) |
| hallway | the reader walks inside the impossible hallway, or has ever reached a place past its far door |
| junction | the reader has reached the junction |
| six doors, twenty doors | the deepest the reader has been (the House's saved maximum), or where they stand now |
| growl | the growl has been heard by this reader |
| stone | the reader has reached a stone hall |
| camp | the finale record has discovered the camp at the staircase's head |
| your own book | the reader's House of Leaves has a bound leaf |
| first fire, five fires | the reader's lit hearths |
| cell | the reader was warned on the cell floor, or has opened the cell or reached any of its outcomes |
| the three endings | the finale's own outcome: witnessed, escaped, or locked out |
| the Mother | she is keeping something of the reader's |
| correspondence | the reader has read a House letter to its end |
| a likeness | the reader's first-death portrait is painted (an operator preview does not count) |

- **Moments the save never kept** are awarded where they happen, and the advancement is then their record:
  - the room between rooms, when a reader comes out of it;
  - a stray, when a reader feeds a House stray meat or tames one with a bone or fish;
  - not alone, when a companion follows the reader through a House door;
  - paid for, when the reader trades with the Mother for what she holds, recovers a pet from her, or pays her with a life.
- **Chalk and the spool** use Minecraft's own `inventory_changed` trigger.
- **The account** is earned with the first story, or when the reader carries their own account book.
- **Enough** is earned when `WitnessAccount.ready` is true. It is the Witness's own rule (75% of the pool rounded up, across two kinds), so it follows the pool as vignettes are added.
- **No new protocol.** Advancements travel in Minecraft's own packets.

## One table, generated

`tools/generate_advancements.py` writes the 91 advancement files under `data/the_oldest_house/advancement/house/` and `.../account/` and their 182 titles and descriptions into the language file.

- The story rows are read from `WitnessAccount.Story`, so a story's title always matches the account and a new vignette in the Witness pool gets its advancement the next time the tool runs.
- `--check` fails if:
  - a story has no advancement;
  - any advancement announces to chat or pays a reward;
  - an ending is not hidden;
  - an icon is missing;
  - a generated file or language line is out of date.
- CI runs the check.

## The waiting room's wait

- The occupied wait in the waiting room is 40 seconds again (`HouseReadMoments.WAIT`, 800 ticks). It was 15 seconds from 0.4.161.
- The test that waits for the call now names the forty-second wait.

## Tests

`AdvancementTests`, three new cases:

- **The tabs.** The journey tab has its 25 and one root (the letter). The account has its progress, the five kinds and one advancement for every story, under the account root. No advancement announces to chat or pays. Every story sits under its kind and is hidden. The kinds and every ending are hidden.
- **A story.** Resolving one story awards that story, its kind, the first story and the account, and not a second kind. A peer gets nothing. More stories bring the second kind and five rooms. The quota is `WitnessAccount.ready`'s own. All 54 bring every room and every story's own advancement.
- **A saved history.** A reader who has only been through the quiet rooms gets nothing in the account tab. Stories saved before the menu are awarded at the first sync. Syncing again does not award them a second time.

All three pass locally. The full native suite counts them with every declared case.

No protocol, layout or Witness change: 54 / 41 / two kinds / three endings.
