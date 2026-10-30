# The post-mortem journal: 0.4.199

This is step 7 of [JOURNAL_DESIGN.md](JOURNAL_DESIGN.md): the post-mortem artifact. It is the owner's idea 5, the solo half. The owner said: "I especially like the idea of a damaged book returning on death."

## When a reader dies in the House

The journal is never the Mother's: she never takes it from a death or anywhere else. When its reader dies in the House, it is taken out of what they dropped before anything else can claim it (`journal/PostMortem`).

- **In an ordinary hall, or the manor.** It stays where they fell, lying open, and never despawns.
- **In a story room.** That means any room that resets, a private copy, or a scene outside. The journal is not there. It waits, and lies open by the door of the third hall they come into after: three halls from the ordinary set, with story rooms between not counted.

Either way it comes back a stage more worn: water-stained, then dog-eared, up to the torn corner. The pages written since the last death take the worst of it: on the screen those pages show one more stage of wear than the rest of the book.

## Picking it up

Picking it up gives it back. The open book shuts again in the hand, and nothing in it is lost: the words live in the journal's saved data, as always.

## Elsewhere

- **Outside the House** it drops like anything else. If it is lost there, the loss rule still holds: it comes back on the reader's next arrival in the House, a stage worse.
- **With keepInventory** it stays in the reader's hands, untouched.

## What it is not

It counts toward nothing. Witness stays 55 / 42 / two kinds / three endings.

## Build

- **Code.**
  - New: `journal/PostMortem`, which handles `LivingDropsEvent` at high priority.
  - Each hall arrival goes through `HouseExperience.arrived`.
  - The book saves `Waiting` (halls left), `Since` (pages written since the last death) and `Soaked`, and the screen is sent `Soaked`.
  - `JournalItem.inventoryTick` shuts a picked-up journal.
- **Art.**
  - `tools/draw_journal_art.py` adds `textures/item/journal_open.png`, the journal lying open.
  - `journal.json` gains an `open` override, set by the `the_oldest_house:open` item property.
- No payload change.

## Tests

`JournalTests.aJournalLiesWhereItsReaderFellOrWaitsThreeHallsAfterAStoryRoom` checks:
- the journal is not the Mother's;
- a real death in an ordinary hall leaves it lying open where the reader fell, never despawning, a stage worse, still their own copy;
- the pages written since then are soaked, with their words intact;
- picked up by the respawned reader, it is shut;
- a death in a story room leaves nothing there, then it waits through two halls and a story room and lies by the door of the third hall;
- with keepInventory it stays in hand, untouched;
- outside the House it drops as an ordinary item.
