# Staircase leaks — implemented design

Status: **implemented for 0.4.49; exact-head verification pending.** Layout 35 / protocol 34. The server’s own surface-reader menu triggers a scene after its final page has been visible for one second, the menu closes, and the reader remains still for three seconds. Ordinary carried copies and borrowed books do not trigger scenes. Room work waits for native chunk and entity readiness and runs in bounded slices. Successful chores reopen the original sheet; early exits, damage and disconnects return without forcing a reading menu.

## The idea

Reading a loose sheet on the great staircase can let the reader slip, briefly, into the scene the sheet describes. For a minute or two, the black shaft gives way to a small domestic room. The reader does one ordinary chore there, then finds themselves back on the same tread with the open sheet in their hand.

Nothing in these rooms is a monster, and nothing is quite wrong. The unease is in how ordinary they are, at the bottom of a 1,280-block shaft. Each room is plain and warm, and too quiet. The people the sheet mentions are never seen: a coat on the hook, a chair still warm, a voice from the next room.

Across many sheets, the rooms belong to one household. A reader who leaks more than once starts to recognise the house. The kitchen from one sheet has the back door from another, and the hall cupboard from a third.

## Which sheets leak

The staircase holds sixty-four loose sheets (`StaircaseNotes.TEXTS`), and each reader's original is saved per tread with its text index (`StaircaseWriting`, `staircase_pages_0427`). Five sheets leak in the first version:

| Index | Sheet | Room |
|---|---|---|
| 0 | The last plate | Kitchen at night |
| 12 | Saturday washing | Back bedroom |
| 6 | The passenger seat | Parked car |
| 9 | A small repair | Front room |
| 32 | Before bed | Kitchen, later |

The other fifty-nine sheets stay ordinary reading. A leak is a rare thing, not the point of the stairs.

## Rules

These follow the project's standing rules for personal, finite and observer-safe stories.

- **Personal.** Only the reader of their own original leaks. A copied, borrowed or replayed sheet never opens a room, and neither does a sheet read by a spectator.
- **Once per original.** Each of the reader's five leaking originals opens its room once. The result is recorded in the reader's own saved state, next to the sheet. Re-reading the sheet afterwards is just reading.
- **Offered, not forced.** The leak begins only after the reader has read the sheet to its last line. They then close the page menu without moving for three seconds. Walking away, taking damage, being in combat or having a hostile creature within sixteen blocks cancels the leak silently. The sheet stays readable, and the leak can happen on a later visit.
- **Nothing gained or lost.** No items, no Witness credit, no fire, leaf or route progress. Inventory, health, hunger, effects and companions are untouched. The rooms are scenery-only and do not count as stories.
- **Safe to leave.** Walking out of the room's doorway, or out through any edge of its floor, ends the leak at once. Disconnecting or dying ends it too.
- **The way back.** The reader always returns to the exact tread, facing, and route position they left. Their saved staircase phase is unchanged.
- **Followers.** Companions stay where they are on the stairs and keep their orders. A pet set to follow waits at the tread until the reader returns.

## How the room is shown

Each leak uses a **personal pocket room**: a small, separately built room, like the personal home copies, placed on its own island far from the shaft in the interior dimension. Only the reader is moved there, through the existing fade and seam, and moved back the same way.

- **Pocket slots.** Pocket rooms are built once per world, on demand, and reused by every reader. A reader is placed in their own instance slot so two readers never share one at the same moment.
- **Never rebuilt.** A room is never rebuilt or restocked. Player edits made during a leak are restored when the reader leaves: the room returns to its authored state from a saved template. The same applies to the chore's prop, so the next reader finds the plate still on the draining board.
- **Spectators.** Spectators see the reader stand still on the tread with a page open. Their camera never follows the reader into the room.
- **Sound.** The staircase score fades out on entry. The room has its own quiet ambience: a dripping tap, a clock, rain.
- **The shaft in the room.** One sound from the shaft always comes through: the Minotaur's call, faint, through the floorboards, like pipes knocking. It is the only sign of where the reader really is.

## The five rooms

Dimensions are interior walking space. Every room has one ordinary door, which is the way out.

### 1. The last plate — kitchen at night

A narrow kitchen, 7 by 5, lit by one bulb over the sink. Rain is on the window.

- **What's there:**
  - a sink half full of grey water, still steaming;
  - a draining board with three cups and one cracked plate;
  - a tea towel on the rail;
  - a cupboard with three empty hooks;
  - a geranium in a pot on the sill.
- **The chore:** dry the three cups with the towel and hang them on the hooks. The cracked plate has no hook. Picking it up makes the geranium the only place it fits: set it under the pot and the leak closes.
- **Implied people:** a second chair pushed back from the small table, and an apron over it.
- **Ending detail:** as the room fades, the tap keeps dripping.

### 2. Saturday washing — back bedroom

A bedroom, 6 by 6, in afternoon light. The bed is covered in warm laundry.

- **What's there:**
  - a heap of socks;
  - folded shirts;
  - a pair of trousers with something in the pocket;
  - a laundry basket.
- **The chore:**
  - Pair five socks; pairs are matched by colour on hover.
  - Take the shopping list from the trouser pocket. It reads "bread, milk, tea, soap, matches", with every item but matches crossed out.
  - Put the list on the dresser and the leak closes.
- **Implied people:** a voice from the landing reads the list aloud as you pick it up, in a caption, not audio. "We forgot the matches."
- **Ending detail:** one sock is left over. It is still there for the next reader.

### 3. The passenger seat — parked car

The inside of a small car, parked on a street at dusk. The reader is in the passenger seat and cannot move from it.

- **What's there:**
  - a paper bag of bread between the reader's knees;
  - the radio playing very low;
  - outside, a row of terraced houses, one with a stump where a tree used to be.
- **The chore:** there isn't one. The reader turns the radio down, a single interaction. The driver's seat is empty, but its belt is still fastened and the seat is warm.
  - A caption gives one line about the pear tree that used to stand at that house.
  - The bread taps the door once and the leak closes.
- **Leaving early:** pressing the dismount key leaves at once, the same as walking out of a room.
- **Implied people:** the driver, by their absence.

### 4. A small repair — front room

A sitting room, 8 by 6, with a dresser, two chairs and a mantel clock.

- **What's there:**
  - one dresser drawer that sticks halfway;
  - a candle stub on the dresser top.
- **The chore:** use the candle on the drawer. It opens silently. Inside is a button tin.
- **Repetition:** the drawer can be opened and closed as many times as the reader likes. The room stays, quietly, until they leave by the door. This is the only room with no closing beat: the reader decides when it is over.
- **Implied people:** the second chair faces the first, and there is a cup on the arm.

### 5. Before bed — kitchen, later

The same kitchen as room 1, a few hours later. It is the same geometry, re-dressed.

- **What's there:**
  - the sink is empty;
  - the cups are on their hooks;
  - the plate is under the geranium.
- **The chore:**
  - Rinse the sink once.
  - Switch off the big light, leaving only the small one over the cooker.
  - In the near-dark, the leak closes.
- **Recognition:** a reader who has already been through room 1 will recognise the cups where they hung them, unless that was another reader. The room is authored, but the arrangement is deliberately the same as room 1's ending.
- **Ending detail:** on return, the shaft's darkness is shown a little less black for ten seconds, a fog fade, before the normal black returns.

## The house

The five rooms share one household's layout, so a reader who leaks more than once can piece the house together:

- **Kitchen** (rooms 1 and 5). Its back door shows a yard with a washing line through the glass.
- **Back bedroom** (room 2). It is upstairs, and its window looks down on the same yard and washing line.
- **Front room** (room 4). It shares a wall with the kitchen. The kitchen clock can be heard through it.
- **The car** (room 3). It is parked outside the front room's window. Seen from the front room, the car is there with its passenger door ajar.

Doors that would lead between rooms are always closed and do not open. The only working door in each room is the way out.

## Multiplayer

- Two readers can leak at the same time; each has their own instance.
- If one reader is mid-leak, others on the stairs see them standing with a page open. Approaching them or hitting them does not interrupt the leak; any damage cancels it and returns them first.
- A reader in a leak is not counted as present on the stairs for the shared occupied clock. Growls and dust only reach those still in the shaft.
- A reconnecting reader is always placed back on their tread, never in a pocket room.

## Data and upgrades

- **New saved state per reader:** `staircase_leaks_0449`, with one flag per leaking index, set when its room closes, either by finishing the chore or by leaving.
- **Existing worlds and originals:**
  - A sheet already read before the release still leaks on the next full reading, if its index leaks and its flag is unset.
  - Saved originals keep their exact text, signature and components.
- **No new Witness source.** Counts stay forty-three eligible / thirty-three required / two kinds / three endings.
- **Layout and protocol:**
  - The pocket island is appended (a layout bump).
  - The client needs one new payload, to fade the score and change the return fog (a protocol bump).

## Tests to add

1. Only the reader's own full reading starts a leak. Copies, borrowed originals and spectator readings do not.
2. Moving, damage or a hostile nearby before the three seconds are up cancels the leak, and the leak stays available.
3. The reader returns to the exact tread, facing and phase, with identical inventory, health and effects.
4. Each index leaks once per reader. Another reader's leak does not use theirs.
5. Two simultaneous readers get separate instances. Edits in one do not appear in the other or persist.
6. Disconnect or death during a leak returns or respawns the reader on the stairs, never in the pocket room.
7. A following companion waits at the tread with UUID, health and orders intact.
8. Spectators never enter a pocket room.
9. The room's chore props are restored after every leak.
10. Native client check: the room renders, the score fades and the return fog fades back to black.

## Open questions

- Should a leak be possible on the way back up (the homeward phase), or only on the descent?
- Should the house ever be seen whole, perhaps as a final, sixth leak after all five rooms, with every door open and nobody home?
- Is a caption voice right for room 2, or should it stay silent?
- Five rooms out of sixty-four sheets: is that too rare to notice on one descent? An alternative is that the reader's first sheet on each flight always leaks.
