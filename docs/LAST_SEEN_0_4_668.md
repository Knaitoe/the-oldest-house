# Last Seen 2: the archive and the first one (0.4.668)

This is the second of the five parts of *Last Seen* (`docs/LAST_SEEN_PLAN.md`). It is built to the recommended decisions in `docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`, which the owner approved on 17 November.

Part 1 (`docs/LAST_SEEN_0_4_667.md`) built the place and the three who work in the office. Part 2 gives the reader something to do. The archive's folders become things to carry. Nora gives her rule and a chore. Then the first of the ten is at the fountain, and a reader who finds her folder by her face and brings it to her has matched her.

The House deals the place from here, from depth eight. It is not a Witness source until part 5.

## The morning

One morning runs for everyone in the park. It starts when the first reader is there and the office's entities are loaded:

- The day's pile is four folders on the long table: Tomás, Micaela, Brian and Daiana.
- Each of those four leaves a gap on its own shelf.
- Every other folder is in its bay.
- The front door is shut, and the three are in their chairs.

The morning has three stages:

1. **The archive's work.** Nora's first line is her rule, and her second is the chore. A reader takes the pile's folders one at a time and puts each back where its name says.
2. **The call.** The call comes when the pile is shelved, with none of its folders still off the shelves, or after three minutes if the pile is left. The first of them is then at the fountain.
   - She is made only where no reader can see her. If any reader is looking, she is not there yet.
   - Luz gets up from her chair, walks to the front door and opens it.
   - Luz turns to the room: "There's a girl at the fountain." Then she says "Nora."
   - Nora gets up from her desk and walks round through the partition, past the counter's end and out of the front door. She stops at the head of the path, at the edge of the paved front, and comes no further. She turns to face the park.
3. **The girl at the fountain.** The girl is Rocío, the first of the ten (stranger phase 243). She stands just south of the fountain's rim at child scale, facing the office, and does not blink.

The morning is held in memory, as the Corner's street is, and is never saved. When nobody has been in the place for ten seconds, it is put back as it was:

- The girl is gone, and Luz and Nora are back in their chairs.
- The front door is shut and the pile is back on the table.
- Every folder is in its bay.

The next reader starts a new morning. What each reader shelved, read and matched is saved to them.

While anyone walks (Luz to the door, Nora to the path), a reader who speaks to them stops them to answer, then they go on, as the Drawing's villagers do.

## The folders

There are ten folders, one for each of the ten (`last_seen_folder`, one to a stack). Each carries its child, its park, its morning and an issue number. Its icon is a manila folder with that child's photograph clipped to its front, drawn from their own skin at eight pixels, so the face shows in the hand and in the pack.

- **Held open.** A folder opens on the reader's own screen: the manila card, the child's photograph clipped to it and the typed form. The photograph is the head of the child's own skin, with its hat layer over it. The form has a file number, NAME, LAST SEEN and ON, and Nora's pencilled note at the foot: "Folder stays open." A folder opens like this when it comes off a shelf or the pile, and again whenever it is used in the hand.
- **The shelves are filed by letter.** The alphabet runs along the thirty-five bays in walking order, so each letter has one bay or two. Each child's folder is filed in the first bay of their initial.
  - A bay with nothing of the ten in it gives its letter.
  - A bay that holds one of the ten gives that folder, into an empty hand. A held folder offered to the wrong bay is told whose names are there. At its own bay it goes back, spine out, and its gap fills.
- **One at a time.** A reader may have one folder off the shelves at a time. Two readers may each hold one.
- **A folder out is out.** A bay whose folder is with someone else shows its gap and says so.
- **A folder goes back when its reader goes.** A reader who leaves the place, logs off, dies, or no longer has the folder has it put back on its shelf, and is told so. This happens even if nobody is left in the place.
- **A copy is nobody's.** Every time a folder comes off its shelf it gets a new issue number, and every time it goes back the number changes again. Any copy that is not the folder out this morning is removed from inventories within two seconds.
- **The girl's hand.**
  - Touched with an empty hand, she is cold, and frost breathes off her.
  - Shown another child's folder, she looks at the face on it, and then away.
  - Shown her own, she takes it into her hand and looks down at it for two seconds. Then she looks up at the one who brought it and says the one thing, "I was waiting where they told me to." Three more seconds and she gives it back to that reader, or puts it back on its shelf if they have gone.
  - Nora's lines change: "Go on. I'll stay here.", then "You've seen her face. Find hers.", then, once the reader has matched her, "Put it back where it goes. It stays open."

## The House's read

Where the House has read a reader, the office uses it, and never names what it read:

- **AGE.** Patricia's first line to a reader the House reads as young (DIGITAL) is "Are you here with someone?"
- **HOME.** Nora's third line to a reader the House reads as having run from a home, or been in danger in one (RUNAWAY or DANGER), is said once: "Most of the ones in here weren't taken. They went. Nobody ever asks what from."

## Single and multiplayer

- **Shared:**
  - the morning's clock, the pile, the folders on the shelves;
  - the front door;
  - Luz's call, Nora's walk and the girl.
- **Each reader's own:**
  - what they heard from each of the three;
  - which folders they shelved (`Shelved`), read (`Read`) and matched (`Matched`, with `Days` at least one);
  - whether they saw her (`SawFirst`), and whether they saw another reader match her (`SawMatched`).
- **Alone or together.** One reader can do the whole morning alone. With two or more:
  - each holds their own folder;
  - each brings her folder to her for themselves (it goes back on its shelf, or is handed back, between them);
  - each is counted for themselves.
- **Spectators** take nothing from the pile or the shelves, are never told she is there, and never count as having seen her. She is made unseen by readers only.

## Made as

- `LastSeen`:
  - the morning (`Day`: stage, pile, folders out, issues, each bay's own fill, the girl, the walks);
  - the folders (take, reshelve, release, the sweep);
  - the bays' letters;
  - the call, the walks and the girl;
  - the talk;
  - `dealable` (depth eight).
- `LastSeenFolder`: the item. Used in the hand, it is held open, and its name is the child's ("Rocío's folder").
- `LastSeenFolderPayload` and `client/LastSeenFolderScreen`: the folder held open. Protocol 198.
- `LiteraryActor`: the ten at child scale (0.68), as the Corner's children are.
- `LiteraryVignettes`: the deal gate is now `LastSeen.dealable`, and the morning is cleared with the other scenes.
- **Art:** `tools/draw_last_seen_art.py` now also draws:
  - the folder's icons, eleven of them: one plain, with a typed label, and one for each of the ten with their photograph clipped on;
  - the item model, with custom-model-data overrides;
  - the folder held open (`gui/last_seen_folder.png`): a manila card with its tab, a typed label across the top, the form sheet with its printed rules, a ring where a cup once stood, and below the card the paper clip the screen draws over the photograph.

  106 textures, 125 models and states, `--check`. The check asserts that the ten folders show ten different faces, and that the card and its clip are drawn.
- **Sound:** the folder sliding off a shelf (made in part 1) is used now, whenever a folder comes off a shelf, goes back, or is taken by her.

## Checked

`LastSeenTests` (the `last_seen` namespace), four cases:

- `theOfficeTheArchiveAndThePark` (part 1, updated):
  - a bay gives its letter;
  - the pile gives its first folder;
  - the House deals the place only from depth eight.
- `theHousesDoorsStayLockedAndASpectatorOpensNothing` (part 1, unchanged).
- `theArchiveAndTheFirstOne`, one reader:
  - Nora's rule and the chore.
  - The pile, one folder at a time into an empty hand, each held open. A second try while one is held gives nothing. Each pile folder's gap shows on its own shelf.
  - The wrong bay keeps nothing and says whose names are there. The right bay takes the folder back and fills its gap, and it is counted.
  - The table goes bare and says so.
  - The girl comes only after the pile is shelved, at child scale, at the rim, facing the office.
  - Luz opens the front door and calls Nora. Nora stands at the head of the path and no further.
  - Nora's two lines while the reader looks.
  - The reader is told how the girl stands. Her hand is cold.
  - A bay that holds one of the ten gives that folder, held open, one at a time. Another child's folder she looks at and gives no more.
  - Her own she takes into her hand, says the one thing after two seconds, and gives back to the reader. The match is counted for them, and Nora's last line follows.
  - A reader who leaves with the folder has it put back on its shelf.
- `twoReadersEachMatchHer`, two readers:
  - A spectator's hand takes nothing.
  - Each reader holds one folder at once, and each counts their own shelving.
  - A reader who leaves has their folder put back, and is told. A copy of a folder is removed once the folder goes back.
  - Patricia's line to a reader read as young, and her usual line to the other.
  - Nora's line to a reader read as having left a home.
  - Each reader brings the girl's folder to her for themselves. The one watching is counted as having seen it.

Also: `LiteraryTests` (the place is dealt only from depth eight and is still no source), and the art check.

## New lines (for the owner)

All are in `LastSeenTexts`. Every one is new except where marked changed.

**Nora:**

- Her first line, now her rule: "Every one of them has a folder. A folder stays open."
- Her second, the chore: "The day's folders are on the table. They go back where their names say."
- Her old first line, now her third and every other after: "We keep them all. Nobody has ever asked us to throw one out." (unchanged)
- **Changed:** her old second line, "The folders don't leave the archive. You can sit at the table with one, if you want.", is now "You can sit at the table with one, if you want. Nobody minds." The folders leave the archive now.
- To a reader read as having left a home, once: "Most of the ones in here weren't taken. They went. Nobody ever asks what from."
- At the head of the path: "Go on. I'll stay here." Then: "You've seen her face. Find hers."
- Once the reader has matched her: "Put it back where it goes. It stays open."

**Patricia**, to a reader read as young, first: "Are you here with someone?"

**Luz**, at the front door: "There's a girl at the fountain." Then: "Nora."

**The folders:**

- Their name: "[name]'s folder"
- Held open: "MISSING PERSONS: MINORS", "FILE [number]", "NAME", "LAST SEEN", "ON", and Nora's pencil: "Folder stays open."
- A bay: "Folders, spine out. The names on these spines all begin with [letter]."
- The wrong bay: "The names on these spines all begin with [letter]."
- Put back: "It goes back with the other [letter] names, spine out."
- Taken from a bay: "[name]'s folder, with the other [letter] names."
- From the pile: "A folder from the day's pile: [name]."
- One at a time: "One folder off the shelves at a time."
- Put back when the reader goes: "The folder is back on its shelf."
- The table bare: "The day's pile is done. The table is bare."

**The first of them:**

- Near her: "She stands at the fountain's rim, facing the office. She does not blink."
- Her hand: "Her hand is as cold as the fountain's stone."
- Another child's folder: "She looks at the face on it, and then away."
- Her own folder, the one thing she says: "I was waiting where they told me to."
- Given back: "She gives the folder back."

**The item's name** (`item.the_oldest_house.last_seen_folder`): "Folder".
