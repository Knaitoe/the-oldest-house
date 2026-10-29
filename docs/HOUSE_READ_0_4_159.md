# The House's read of the reader — 0.4.159

The owner's note (October 29): "why don't you try to knock out the read of the reader? Again, I'd suggest reviewing the personhood rooms as they exist to ensure they are properly structured and accurately portray their intended scenes".

This lifts the owner's earlier deferral of category assignment. The five quiet rooms were reviewed against the owner's designs in `docs/HOUSE_READ_DESIGN.md` and repaired, and the House now turns what each reader does in them into a read that later rooms, the account and the House of Leaves are written with.

## The rooms, reviewed and repaired

**The waiting room** read *what the reader picks up to pass the time*, but its three ways of doing that were not equally easy to reach. The paper and glasses sat six blocks from the door, while the television and cooler stood thirteen blocks away at the back.
- The three ways now stand about seven blocks from the door:
  - the low table to the left, with the folded local paper, the magazines, the reading glasses and a new **ashtray** (the owner's design had one);
  - the cart ahead, with the television on its rolling cart and the water cooler beside it, the way to the counter running between them;
  - the phone on the arm of the chair nearest the wall outlet, to the right.
- The window onto the parking lot moved from a frosted pane on the far side to two **clear windows beside the phone chair**, on the `DIGITAL` side, as the owner placed it. The reception glass stays frosted.
- The armchairs face into the room.

**The bathroom** was already faithful: four routines at equal distance, shuffled per reader, the light switch opening the bedroom door. It is unchanged.

**The coatroom** lacked the owner's **wooden bench**; it now has one by the coats. The three coats stay on three pegs at equal distance, shuffled per reader.

**The shelter** did not portray the scene.
- The strangers stood mid-deck by two fence posts, and two docks opened right beside them, so walking to a dock passed a stranger.
- It is rebuilt:
  - the young man leans on the **west railing** and the young woman on the **east railing**, mirror images facing across the floor, leaning back with their elbows on the wood, the two lit alike and moving the same small amount, each with a bottle on the rail beside them;
  - the **jukebox stands against the back wall** on the land side, with the way back to its left and the way on to its right, so leaving passes neither rail nor the dock;
  - one **dock runs out ahead**, past both of them: steps down from the lake rail to a short pier, a dry lantern by the steps and a covered rowboat tied alongside;
  - "the middle of the floorboards" is the strip between the two rails, the same distance from both.
- When the waiting room has read the reader as older, the strangers are **around forty** (`TRANSITIONAL`) **or older** (`ANALOG`), with eight new skins painted on the shared character grid (`tools/paint_house_read_cast.py`). The account then calls them the man and the woman.

**The porch** recorded its acts but had no window, and missed three things the design reads.
- The **200 ticks after the whistle** are now its window.
- Hiding under the porch counts on **either side** of the steps (only the west side counted before).
- **Looking in at the lit window from the lawn** and not going in counts, when the reader's line of sight actually lands on one of the front windows.
- **The way out** each reader takes (the front hall, the side gate or the road) is kept.
- Not built: the design's "side porch out of view of the door". Under the porch is the way to hide.

**Saved worlds.** A reader who has entered a room never sees it again. A waiting room, coatroom or shelter built ahead of time but never entered is built again in the new layout when it is next prepared.

## How the House reads

Each room hands in **signals** as they happen, and `HouseRead` turns them into one category per trait with a strength:
- **First signal:** the first deliberate act inside the room's window sets the trait, tentatively. Later poking around is kept as an observation only.
- **Second signal:** one that agrees settles it. One that disagrees makes it uncertain, and the next decides: settled if that value has been seen twice, otherwise tentative.
- **A second signal never stands alone.** Sitting or pacing on their own, for example, read nothing.
- A room may revise its own first instinct, as the coatroom does when the glass is turned after dressing.

| Room | Window | First signal | Second signal |
|---|---|---|---|
| Waiting room (`AGE`) | until the door clicks | the paper, magazine or glasses (`ANALOG`); the television knob or the cooler (`TRANSITIONAL`); the phone (`DIGITAL`) | as the door clicks: the phone still in hand (`DIGITAL`); the paper, magazine or glasses still held (`ANALOG`); within reach of the cart (`TRANSITIONAL`); sitting (`ANALOG`); or flipping through more channels (`TRANSITIONAL`) |
| Bathroom (`HAIR`) | until the light goes out | the first routine item, by its set | a second item from the same set; the hot tap, only for the set whose washcloth hangs on the faucet |
| Coatroom (`SELF`) | until the curtain | the first garment, by its set; a coat and boots from the wool and trench sides together read `ANDROGYNOUS` | two seconds at the glass in the coat settles it; turning the glass or covering it **before dressing** settles `UNSEEN` (the coats after are only observed); **after dressing** it makes `UNSEEN` the first guess |
| Shelter (`DRAWN_TO`) | while the song plays | each stranger's share of the reader's gaze (their face within eight degrees of the view centre, nothing between) and time within five blocks: 65% or more for one, otherwise both if both had real attention; or walking past both onto the dock (`NO_ONE`) | staying beside one stranger, in the middle between them, or on the dock until the song ends |
| Porch (`HOME`) | 200 ticks after the whistle | through the screen door (`HAVEN`); under the porch, or looking in at the lit window and not going in (`DANGER`); down the gravel to the road (`RUNAWAY`) | sitting at the warm table (`HAVEN`); still hidden when the kitchen light goes off (`DANGER`); past the mailbox into the trees without turning back to the house (`RUNAWAY`) |

`HouseRead` also keeps each room's observations (`Seen`), the literal acts the account and the book recite. Categories are never recited.

## What the read changes

**The account opens with it.**
- The House recites the reader's night back to them, in plain type, before *AN ACCOUNT* (`HouseReadProse`).
- It is one breath of prose, one clause per room, in room order:
  - with more than two clauses, a second sentence begins at the hall;
  - skipped rooms drop out;
  - every one of the 31 combinations of rooms reads correctly, as does every branch of every room.
- The owner's example night reads exactly as the owner wrote it.
- It closes with one line chosen by `HOME`:
  - `HAVEN`: "Somewhere in the House, a plate is being kept warm for you."
  - `DANGER`: "The House has learned how you listen to a hallway."
  - `RUNAWAY`: "The House has noticed how quickly you find the road."
- A reader the House could not read opens with today's page.
- It is always "you", never he or she.

**The House of Leaves is written with it.**
- *II. The hands* takes the first routine item and the coat.
- *III. What happened* takes who the reader stood beside under the eaves, the middle of the floorboards, or the lantern alone.
- *IV. The door* takes what the reader did when the whistle blew and the way they left.
- *V. Below* changes with `HOME`:
  - the letter writer, who "do[es] not know where that is for you", knows: the porch light and the plate kept warm, which door to use, or the road;
  - the other three narrators each change one sentence.
- With no read, the book is exactly today's. The narrator following `AGE` is still the owner's to decide and is not built.

**The House's version of the reader.**
- A private message (`HouseReadLookPayload`) carries only a reader's own look to their own client:
  - the coat they took, once their presentation is settled;
  - reading glasses for an older read;
  - an obscured face for a reader the glass did not agree with (`UNSEEN`).
- These are worn over the reader's own skin, never painted on it, and only in glass and copies:
  - the coatroom glass adds the glasses and the obscured face;
  - the later mirrors of the hazard halls show the coat as well;
  - the double in the novel sites and the reader's echo on the great staircase wear them.
- The live model never does, and another reader's copy never does.
- The native art proof draws the new layers on standard and slim skins.

**Strangers who speak** (the six earlier consumers) now have a read to follow:
- the Last Stop men, the Stanley's staff and the Ghosts crew address by settled presentation;
- the child's bedtime card, the 217 sink and the lake house vanity follow the routine;
- the lake house partner follows who the reader is drawn to.

**The deal.** A reader the porch settled as one who takes the road is dealt roads and outdoor rooms twice as often.

## Privacy and the server option

- The read lives only in the world save (`house_read/<reader>`). It is never sent to another reader's client, and nothing writes it to the playtest log.
- The README now says so.
- Two server options sit under `read` in the server config:
  - `readReaders` turns all inference off;
  - `readDrawnTo` turns off only who a reader is drawn to.
- A trait turned off is never inferred, and a saved read of it is ignored until it is turned back on.

## Not built yet

- Settled `DANGER` authority figures who linger rather than chase, and the warm or tense mood a tentative `HOME` gives domestic rooms.
- The larger `AGE` changes: the Wheel starting at the reader's stage of life, Harrigan's handset, a landline in later halls.
- The silk wrap on a double asleep: no sleeping double exists yet.
- The owner's two open proposals: Tom and Kate as grown names, and the narrator following `AGE`.
- From the October 28 review: caching each reader's active room per tick, and regrouping friends after the chain.

## Tests

The quiet-room job runs 31 cases: four new ones, and the six room cases extended.
- **The read** (one case):
  - strengths, uncertainty and its resolution, a second never standing alone, the first-instinct revision, invalid values, another reader untouched, both server options, and reload;
  - the owner's night verbatim, all 31 combinations and every branch of every room in the opening, each checked for sentence shape, order, no gendered words and page fit;
  - two readers' accounts opening differently, and an unread one opening as before;
  - the House of Leaves unchanged with no read, and written with the read and `HOME` across 16 seeds and every narrator;
  - the look reaching only its reader;
  - the deal weight doubling only for a settled `RUNAWAY`, only outdoors.
- **The shelter:** an older reader's strangers are older; staying beside the woman, looking at her, through the song settles `WOMEN`, she smiles, and the account says so.
- **The porch:**
  - three readers each in their own yard: in, under the porch, and down the road;
  - each read tentatively, the hidden one and the runaway then settled;
  - each account and book recites its own yard, and the runaway's deal weight doubles.
- **The coatroom:** the glass turned before dressing settles `UNSEEN` and the coat after is only observed, while the look shows the coat without the face; a mixed outfit reads `ANDROGYNOUS`, and turning the glass afterwards makes `UNSEEN` the first guess.
- **The existing cases:**
  - the waiting room's new positions, at equal distance, with the ashtray and window;
  - glasses kept on to the door settling `ANALOG`;
  - the bathroom's first two items settling the routine, and the untouched light reading nothing;
  - the coat and glass settling presentation, with the look wearing that coat;
  - the shelter's mirrored rails and dock ahead;
  - the porch watcher who looks in at the lit window through the rail and never goes in reading `DANGER`.
- **The writing job** adds the opening's longest pages, and every new leaf and ending, to the native font check. All 2,457 pages fit the 14-line book page.

Local runs: the quiet-room job's 31 cases pass, as do the writing job and its native client font check, and the quiet-room art proof's 16 images, including the House's version. Full CI is pending.

Protocol 90 (a new private message). No layout or Witness pool change: 54 / 41 / two kinds / three endings.
