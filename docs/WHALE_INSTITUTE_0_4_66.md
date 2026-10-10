# The Three Attic Whalestoe Institute — 0.4.66

Based on the 0.4.65 trailer and cast head `e6ffbf9`. The owner asked for the Whale room to be redesigned, from map to function. They wanted sending and receiving letters to feel seamless, and time to feel meaningless: a reply is already in the player's hand when they post, and the mailbox finally holds their own sent letters. The design below was approved before implementation.

No new eligible source: 43 sources / 33 resolutions / two kinds / three endings. Layout 39 / protocol 40.

## The building

The institute fills the existing WHALE slot (34), in the same footprint and with the same entry door.

- **Reception and post room.** Benches face the door. Two boards read *VISITING HOURS — none at present* and *Patients may write as often as they like.* The outgoing slot is set into the back wall beside the old mail plaque. Twelve numbered pigeonholes line the east wall under a *POST* sign: 1 to 6 above, 7 to 12 below. Each pigeonhole is a real block with its number on a brass plate. The clocks in their fixed frames disagree.
- **Corridor.** Eight rooms: odd numbers to the east, even to the west. Rooms 2, 5 and 8 have iron doors and do not open. Four more clocks keep four different hours.
  - Room 7's number plate has been taken off: only two screw holes remain.
  - Recessed frosted windows look out of the side rooms.
- **The other rooms.** A pair of made beds, a chair turned to the wall, the baths, and stacked chairs.
- **Her room (7).** A bed, a walnut desk with paper in it, her opening letter on a lectern, and a calendar on the wall.
- **Dayroom.** Chairs face windows painted shut. A tea table and a nurses' counter. A two-wide stair, solid beneath, rises along the north wall.
- **Three attics** under one dark-oak roof:
  - the west attic holds the stairwell;
  - the middle attic, through an open door, has a chair at a painted-over window and the undated letter;
  - the east attic stays locked behind iron.

The institute's own builder authors all of this. The generic architecture, shell and dressing passes no longer touch WHALE. The polish pass still lights it and trims defects.

## The letters

Everything here is private to each writer, stored under `Whale0466` in that reader's novel record.

- **Paper.** The desk in her room gives a blank book and quill, one at a time, up to four per reader. Any other book and quill the player owns works too.
- **Posting.** The slot accepts only a signed letter in the player's own hand. An unsigned letter is refused with a message and stays in hand. The slot takes three letters, then is stopped up.
- **The reply.** The moment a letter goes, the hand that posted it already holds her answer. Each answer:
  - is dated earlier than the last (9 March, 2 March, undated);
  - quotes the first sentence of the letter it has not yet received;
  - is headed from a room she does not live in (2, 11, 4).
- **The calendar.** After each letter, the calendar in her room is crossed out again. Only that writer sees it change: a private sign packet, refreshed every five seconds while they are inside.
- **The cipher.** The five lines on the third page of every answer begin S, E, V, E, N. Nothing says so.
- **The box.** After the third letter, pigeonhole 7 holds that writer's three posted letters, exactly as written: same components, same pages, same signature. It gives them once. As it does, the writer alone hears the numberless door open down the corridor. Every other box, and box 7 for anyone who has not written three letters, is empty.
- **The ending.** Reading one of those returned letters inside her room resolves WHALE (`returned_to_sender`). Reading it anywhere else does nothing.
  - The reading opens as a native lectern menu that nothing can be taken from, so the letter stays in the player's inventory.
  - A one-page letter is already at its last page, so opening it resolves it.
- **The other letters.** Her opening letter and the middle attic's undated letter are scenery. They foreshadow; neither explains.

New chest deliveries in the ordinary world have stopped. Letters already delivered stay where they are. The attic knock is retired.

## The envelope

The resolution gives one finite, owner-bound **self-addressed envelope**. Only the reader it was addressed to can use it.

- **Inside the House: return to sender.** Once per in-game day it returns its sender to the manor's domestic hall, carrying following companions, and spends the way back as if every door had been walked back through. It refuses:
  - inside any vignette, a literary copy, a novel room or the great staircase;
  - during a committed finale phase, a door transition, a locked literary retreat or a staircase note scene;
  - in the outside or between dimensions.
- **Outside: one sealed stack.** It seals the stack held in the other hand. It refuses containers, bundles and other envelopes. Sneak-use opens it.
  - If its keeper dies away from the House while it is sealed, it stays out of the death drops.
  - On respawn the keeper gets it back, opened, with its contents beside it, ready to seal again.

## Saved worlds and multiplayer

- **The carve.** Layout 39 carves the institute again in existing worlds (layouts 21 to 38). It waits until no one is inside it or has a camera within 32 blocks, and until its chunks and entity sections are loaded. Residents are frozen through the sliced carve and restored afterwards. Fixed clocks are cleared first, so no frame drops an item.
- **What survives.** Every reader's novel record, posted books, Witness evidence and completed ending is kept.
  - Accounts that resolved the old attic ending keep its title and words.
  - Saved prose originals are unchanged; new prose calls the place "the institute post room".
- **Multiplayer.** A peer gains nothing from another writer's post, box or reading, and box 7 is empty for them until they have written their own three letters.

## Verification

Native coverage replaces the old chest/knock case with `postedLettersAreAnsweredBeforeTheyArriveAndComeHomeToHerBox`. It checks:

- **The building:** real numbered boxes, her un-numbered door, and paper given one sheet at a time.
- **Posting:** an unsigned letter is refused; three posts each put a quoting, wrongly headed SEVEN reply in hand; a fourth is refused.
- **The calendar:** it advances after each letter.
- **The box:** a wrong box is empty, and box 7 is empty for a peer. Box 7 returns exact originals once.
- **Reading:** a returned letter changes nothing in the post room, and resolves in her room with nothing takeable. A peer is not credited.
- **Saving:** the record and the finite envelope survive a native reload.
- **The envelope:** it refuses inside the institute and recalls from the manor once a day. Outside, it seals one stack, is held out of death drops and comes back opened on respawn.

`layoutThirtyThreeAppendsOnlyNewHallsAndKeepsExistingDoorsUsable` also checks the institute is carved again in place, with box 7 and her desk, and that an earlier reader's letters and resolution survive. The window check uses the institute's own interior. The writing proof covers every reply with a sixteen-letter name and the longest quotation. The package check covers the 48 pigeonhole block states, twelve plates, the envelope model and the three new classes.

Verified together with 0.4.67 at source 3ffcff14d87b9f4c48381e2af22e9639f0b2b327, run 38059068194: all twelve jobs and 484 native cases passed.
