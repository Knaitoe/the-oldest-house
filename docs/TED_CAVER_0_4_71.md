# The small passage, reworked — 0.4.71

Based on 0.4.69 head `f44ede1` (children and streetlights). The owner asked for a review of the Ted the Caver vignette's design and implementation, then approved implementing the review's suggestions. During the work the owner added two rules:

- the reader must be able to mine the cave's proper blocks despite the House's rule that nothing is broken;
- a reader may take back only the torches they placed themselves.

There is no new eligible source: 43 sources / 33 resolutions / two kinds / three endings. The resolution is unchanged: `ted_caver`, "retraced the squeeze", personal and earned once. Protocol 45. Layout 40 is unchanged, because saved caves are reshaped in place rather than carved again. 0.4.70 (the Whalestoe hospital) is a parallel branch that does not yet compile. This work is based on its common ancestor, and the two should merge without touching the same game code. Validation pending.

## What the review found

- **The hidden passage was not hidden.** The smooth stone stood inside the open bowl. A reader could walk round it to the passage behind and the low chamber. Only the story flag needed the stone.
- **The long dig had gone.** Since 0.4.51, one ordinary pickaxe break opened the crack, in under a second with an iron pickaxe.
- **The climax had almost no pressure.** The "low chamber" was five blocks tall. The stay lasted four seconds. The pursuit was a sound and a tiny tug every eight seconds. The two strings in the barrel did nothing.
- **Machine subtitles.** Vanilla sounds made the stone show "Piston retracts" and the scraping show "Block broken".
- **Smaller issues:** a pickaxe hint that pointed at an empty barrel, an epilogue that played 12 and 24 seconds after leaving, and per-tick reading of the whole saved record for every online player.

## The cave now

- **The crack.** It is five blocks of packed rubble deep (`cave_rubble`). Each block takes several seconds with an iron pickaxe and drops nothing.
  - **How it is worked.** Normal pickaxe mining works on the front block only. The first block can be mined from the mouth. The rest must be mined while crawling inside the opened squeeze, one block at a time.
  - **Strokes.** The timed right-click strokes remain: five strokes, 25 ticks apart, take out each block. That is 25 in total, where there were 24 before.
  - **Shared progress.** The work is shared and saved, and the opened run stays open for later readers.
  - **The hint.** Without a pickaxe, the reader is told so. The camp barrel is mentioned only while it still holds one.
- **The House's no-breaking rule.** It still protects everything else. Inside the cave a reader may break only two things: the front rubble block, with a pickaxe, and a torch they placed. A torch placed by someone else is refused with "That torch isn't yours to take." Torches placed before this version have no recorded owner, and anyone may take them.
- **The stone.** It is set into the bowl's back wall. Nothing behind it can be reached on foot or crouching until it moves. The passage behind it is two blocks high and leads to the low chamber.
- **The low chamber.** One block of air sits under a tuff-slab ceiling, so a reader must crouch to enter and stays crouched.

## The breath

- **One clock.** The cave breathes on a single clock shared by everyone inside, and the clock pauses while the cave is empty. Each cycle is five seconds out (toward the camp), one still, five seconds in, one still.
  - **Heard:** each breath is audible at the squeeze mouth and at the stone.
  - **Seen:** smoke-fine dust drifts out of the mouth and along the squeeze. Before the stone moves, the dust also drifts from the stone's seams, which is how a reader finds the loose stone. Afterwards it drifts from the passage behind the stone.
- **The draught.** A crawling reader feels it. The server sends the current push with the crawl pose, and the client adds it to its own movement.
  - **Before the long wait:** it is gentle both ways (out +0.008, in -0.006 per tick).
  - **On the way out:** the out-breath helps (+0.012) and the in-breath drags hard (-0.022). A reader who has tied a line is held: the drag drops to -0.006.
  - **Never stuck:** crawling always makes net progress over a breath, so no one is stranded.
- **The stone puzzle.** Reading the marks still comes first. After that, the stone gives only on the in-breath.
  - **On the out-breath:** "The stone is pressed hard into its seat. Air hisses round its edges."
  - **In the still moment:** "The stone will not move. The air around it has gone still."
  - **Hint:** the notebook's fifth page says air comes from behind the stone "out and then back in".
  - **Later readers:** once the stone has moved, a later reader examines the rolled stone themselves, at any breath.

## The line

- **Tying.** A reader holding string can tie a line off at the chain beside the ladder, or at the ladder itself. This spends one of their own strings: the barrel has two, and any string works. Each reader ties their own line, once.
- **Paying out.** The line records the reader's path past the shaft and is drawn as a pale cord along the floor. Only that reader sees it.
- **At the climax.** The cord draws tight with a creak and is drawn often for five seconds, showing the way back. It holds against the in-breath's drag.
- **After the escape.** The line stays tied: "It is wrong to leave the line there."

## The low chamber and the return

- **The wait.** It lasts 25 occupied seconds and counts only after the reader's own stone. Cues escalate:
  - grit sifts from the ceiling;
  - stone scrapes in the passage behind;
  - it scrapes closer, and a tied line twitches.
- **The pursuit.** Then the pursuit begins:
  - **with a tied line:** "Air moves past your face. Then the line draws tight toward the way you came."
  - **without one:** "Air moves past your face. Nothing holds you to the way back."
- **The return.** It is the same physical retracing of the squeeze and climb above the rope, with scraping behind the reader and the draught working against them.
- **The epilogue.** It comes on later trips through the House: a chisel heard behind a wall on the third and seventh later arrival, and the last undated page after the seventh.

## Sounds

Six original synthesized sounds, each with a subtitle (`tools/generate_caver_assets.py`):

| Sound | Subtitle |
|---|---|
| `caver.exhale` | Air breathes out of the stone |
| `caver.inhale` | Air draws back into the stone |
| `caver.scrape` | Stone scrapes behind you |
| `caver.line_taut` | Line pulls taut |
| `caver.stone_roll` | Heavy stone rolls inward |
| `caver.chisel` | A chisel taps behind a wall |

The only remaining vanilla sound is the stone break of the reader's own stroke.

## Saved caves

`CaverRedesign` reshapes an older cave once, in place, under checkpoint `caver_0471`.

**What changes:**

- the bowl closes behind the stone;
- the passage and chamber get their lower ceilings, and a faint light moves down into the chamber;
- an unopened crack becomes the packed run, keeping its saved strokes (every five strokes is one block already out);
- an already opened crack stays open, with its work counted complete.

**The guards:**

- it waits for loaded chunks and entity sections, and for no camera, including spectators, anywhere in the deep cave;
- nothing is changed while new rock or a slab would close through a living body;
- the whole change is applied at once, never half way.

**What is kept:**

- **Torches:** a torch standing where rock returns goes back into the camp barrel, or beside it if the barrel is full, and its owner record is cleared.
- **Unchanged:** the notebook, barrel contents, ladder, every reader's record and Witness evidence. Nothing is restocked.

**New worlds** build the new shape directly and mark the checkpoint.

## Notebook

The nine pages are unchanged except two lines:

- **page two:** rubble is taken out "a block at a time", and the line is tied off at the ladder;
- **page five:** air comes from behind the stone "out and then back in".

The notebook is a living record rebuilt from the reader's progress, so an owned copy picks up these lines at its next update.

## Tests

The caver namespace now has 13 native cases (`CaverTests` and `CaverInteractionTests`).

- **Walking route.** A reachability search over real block collision, with no teleporting, proves three things:
  - with the stone in its seat, no route from the bowl reaches the passage or chamber;
  - once it rolls aside, both are reachable;
  - the chamber has crouching room and no standing room, and the passage is exactly two high.
- **Rubble.** Timed strokes take out each of the five blocks in turn, cannot be spammed, and survive reload and rebuild without restocking. Normal mining works on the front block only, records the miner, and opens the squeeze after all five blocks.
- **Torches.** Each torch belongs to its placer. Neither of two players can take the other's.
- **The breath.** It does not advance while the cave is empty, and one clock serves everyone inside. The stone stays put on the out-breath and in the still moment, rolls on the in-breath, and credits only the hand that moved it. A later reader's own examination counts at any breath.
- **The line.** One reader's string ties only their line, and it pays out only behind the reader who walks. The draught values (gentle, dragging, held, and none after escape) are checked, and the line survives reload.
- **The full escape.** Tie the line, crawl in, read the marks, move the stone on the in-breath, wait the real 25 seconds, check the held drag, crawl back and climb. Only the explorer is credited. Six later arrivals add nothing, and the seventh adds the last page.
- **Saved-cave reshaping.** An actual older cave, which could be walked round its stone, waits for a reader in the deep cave and for a body in the chamber, then reshapes all at once. A torch goes back to the barrel. Twelve saved strokes keep two blocks out. The stone becomes the only way on. The checkpoint survives reload, and an already opened crack stays open.
