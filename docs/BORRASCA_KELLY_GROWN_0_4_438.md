# Borrasca: Kelly, grown, in room 6 (0.4.438)

The owner: "Why not have Kelly etc aged up if the player sees them as adults? Wouldn't that fit the rule and the lore?" This is the
second part of the lower rooms' rework (0.4.437 rewrote the records; 0.4.439 is the feeding scene).

The earlier proposal never found her. Now she is in room 6 on the last night, taken at twelve and brought down at eighteen. She is the
same saved actor as the child the reader knew. In the source the taken girl is found alive and will not leave. Kelly does the same:
she knows the reader, stays, and sends them to stop the machine. The ending is unchanged: the machine stopped and her name carried out.

## Room 6

- The door still stands open. The plate over it now reads only RM 6 and 263-A (no TRANSFER, no D-417).
- `BorrascaRooms.KELLY` (-15,6,-179) is her place by the cot, under the two names scratched into the wall.
- `BorrascaCast` sends the friend's actor there for visit 4 (`CastKimber`, visit 4). A missing friend's next place is now the later
  of visit 3 and the current visit, so the last night no longer parks her in the store.
- `BorrascaStory.visitFour` keeps her in `GROWN` (phase 11):
  - visible, with no name shown;
  - put back on her spot if she is more than a block and a half off it;
  - turned to the door, and to the reader once they are in the room.
- When a grown Kelly is loaded on the last night, the actor's join hook sets that phase rather than hiding her.

## Grown

- `LiteraryActor.appearance` treats the friend as a child only while she is not grown: she stands at full size (1.8 blocks).
- `client.BorrascaNativeArt` draws `GROWN` with a new skin, `borrasca_kelly_grown`: her auburn hair, green eyes and freckles, in a
  washed-out shirt and trousers. It is painted by the shared painter (`tools/paint_character_skins.py`) as person 8. The cast
  preview treats only persons 0, 1, 6 and 7 as children, and the character catalog and its check list her.

## What she says

Coming into the room, the cue reads: "Room 6. Scratched into the wall: KELLY, and beside it your name, the way you both cut them into
the tree. A woman is standing under them. She looks up." She speaks first. After that, each word with her is the next line, and the
last one repeats:

1. "Don't touch the other doors. They count them in the morning."
2. "You still have your name on the tree? I did ours on every wall they gave me."
3. "I'm not coming up. Two of mine are up there already. They have other people's names now."
4. "The machine on the ridge is the only thing up there that's theirs. Stop it."
5. "Take my name up with you. Not me. Go on."

## The records and the account

- The night book's 2004 page now ends "Rm 6. Asks for nobody now." instead of a transfer.
- The account's line for room 6:
  - Found: "In one of them her name was scratched into the wall beside mine. She was there, grown, and would not come up."
  - Asked (her fourth line or later): "She asked me to stop the machine, and to take her name up instead of her."
- The night book's line now reads "Beside her number there were two births, both placed in town."

## Tests

`BorrascaTests#theRoomsUnderTheDepotHoldWhatTheNoteWarnedOf` adds two checks:

- In room 6 Kelly is grown, visible, full size and by the cot, and `KellyFound` is set.
- Four words with her set `KellyAsked`. The account says she was there, grown, would not come up, and asked for her name to be taken
  up.

The night book check now reads "Asks for nobody now".

`BorrascaVisualProof` draws her grown on its four cast pages (front, side, back and dim), beside the child she was.

## Numbers

Protocol 175, Witness 86/65/63 and layout 135 are unchanged. One new skin. New builds only.
