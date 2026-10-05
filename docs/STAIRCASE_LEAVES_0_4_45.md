# Staircase leaves — 0.4.45

The great staircase is lit only by the explorer's own House of Leaves, and the story is found on the way down.

## How the descent works now

- **The camp shelf** gives the binding: a written book holding only its title leaf, plus a flint and steel. The title leaf names the reader and says the five leaves are loose on the stairs, one to each flight.
- **One leaf lies on each of the first five flights.** Each is a paper sheet on a level, full-block tread, three blocks to the inner or outer side of the walk. It is placed at a different point along each flight.
  - Leaf *k* is within reach once *k* fires burn.
  - It lies beyond the reach of the fire before it, so a flight's leaf cannot be found early.
  - It always lies above its own hearth's landing.
- **The sheet is shared scenery, but its words are personal.** Opening it shows the reader's own leaf on a native lectern page, and **Take** binds it into the carried original.
  - A leaf comes loose only in order, and only while the reader carries their living original.
  - Each reader binds each flight's leaf once.
  - Spectators bind nothing.
- **Hearth *k* lights only from the reader's own original holding leaf *k*.** Paper, native copies, other readers' books and an empty binding are refused with a plain message. Each burn takes the bound leaf, keeps the book's other components, and lets the reader down to the next flight. The fifth leaf takes the binding with it.
- **A quiet page-turn**, heard only by that reader, sounds near the leaf they still need when they come within fourteen blocks.
- **Losing the original:** the shelf binds the same story again from the saved record. It holds exactly the found, unburned leaves; nothing burned is refilled. The left-behind copy goes cold. A flint and steel is added only if the reader has none.
- **The record decides, never a copy.** The record holds the saved account, the original's identity and two counters (found, burned). Duplicates only ever show the same state, and cannot spend a leaf twice.

## Varied accounts

`StaircaseProse` writes each account once, from the native record `StaircaseAccount` gathers.

- **Facts used:**
  - native statistics: distances walked, run, swum, rowed, ridden, flown, fallen and climbed; deaths; creatures killed; bread made; enchantments; animals bred; fish caught; cake eaten; flowers potted; music played; bells rung; trades; nights in a bed; hours played;
  - confirmed Overworld work: the last block placed or broken, named from its registry id;
  - this explorer's House records: the companion they cared for, what the Mother still keeps, a story left unfinished (by its prose name, never an internal id), nights in the manor, letters read and depth reached.
- **Narrators:** there are four, each with its own hand and layout.
  - A witness, in Will's hand, with numbered, titled leaves.
  - Letters, in Pelafina's hand.
  - A survey, in Zampanò's hand, with bracketed notes.
  - A ledger, in plain type, with two entries per leaf.
- **How the leaves are chosen:**
  - Facts are weighted by how much of this player's life they hold, with a seeded tilt, and kept to distinct kinds.
  - One of three arcs orders them.
  - Each fact has three phrasings.
  - The fifth leaf always closes on a House fact, or on a quiet leaf.
- **Missing facts:** an explorer with little recorded history receives quiet leaves, not invented history or disclaimers.
- **Wording:** counts use native-sized numbers with separators, "once" and "twice", and singular nouns.

Existing 0.4.44 accounts keep their written chapters and gain a title leaf. They hold all their remaining chapters, so no search is needed. Tutorial originals from 0.4.33 to 0.4.43 become the reader's account and still hold the leaves they had. Fires lit before this edition (by paper, or an old deep visit) count as burned leaves, so nobody is trapped.

## Entrance repairs

- **Camp supports:** two deepslate beams run under the camp to the south shaft wall on corbels, with a cross-beam at its north end. Chains tie the four roof corners to the shaft cap. Existing worlds receive these once (`staircase_supports_0445`), only into empty cells, while the camp is vacant.
- **Old platform removed:** the same pass lifts only the planks and wool of the open 0.4.23 platform that was left floating north of the walkway. New worlds no longer lay it; their camp fire stands inside the camp.
- **Arrival copy:** staircase arrivals copy only the entry hall's own width (three blocks either side of the door). The walkway's south rail and the first tread are no longer overwritten by strips of the source room. The walkway keeps its three-wide path and its rails. The hall's own side walls are restored after each copy, so a wide source room never opens the hall onto the shaft.
- **Leaving the stairs:** a confirmed return now always lifts the staircase darkness and resets the phase. It spends only the waypoint actually used.

## Verification

Require all 359 declared native gameplay tests. That includes eight focused staircase cases (supports, the confined arrival copy, leaf placement against the real fire edges, and the existing-world support pass) and fifteen multiplayer cases:

- two readers finding and burning their own leaves at shared hearths;
- refusal of paper, copies, borrowed books and spectators;
- order and carried-binding rules, with the dark edge holding the next leaf;
- re-binding a lost original;
- upgrades from 0.4.33–0.4.44 originals.

The native client width check also consumes every leaf the writer can produce at its widest, through all four narrators.

Layout 33 and protocol 32 remain unchanged. No network message is added. Forty-three eligible Witness sources require thirty-three personal resolutions across at least two kinds; the three endings remain. Burning adds no Witness source.

Verified source `eef5d85fde7288ad295f53a2c25e28235d5b8033`, run 37303487827: all 359 declared native gameplay tests (the complete suite), the eight focused staircase and fifteen multiplayer cases, the literary, hotel, hallway and seam suites, the package checks and the native client width check (every widest leaf through all four narrators within 114 px / 14 lines) passed.
