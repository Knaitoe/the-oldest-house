# The rest rooms (0.4.261)

The owner, with a screenshot of the quiet room: "we've got to design more of these rooms. change the notes, too. i see this far too
often."

The quiet room (slot 26) was the only room the House offered for a rest, and it was dealt among the ordinary halls at weight 3, more
often than any other single room. Its stand always held the same note, "A place to stop".

## Four more rest rooms

Each is built whole by `QuietRooms` (no generic pass touches it), is calm (nothing frightening happens in it), holds a little food in
a barrel that is stocked once and never refilled, has somewhere to sit, light, a note, and its two doors: the way back and the way on.

| Room | Slot | Shape | What is in it | The note |
|---|---|---|---|---|
| The reading room | 166 | 11 wide, 15 deep | Panelled and papered; full-height shelves down both long walls with a ladder; two beams; a cold hearth with a mantel, candles and a clock (a little before nine); two armchairs and a footstool on a rug; a standing lamp; a reading desk and chair; the food in a sideboard let into the shelves | on a stand inside the door |
| The boot room | 167 | 7 wide, 19 deep, low | Quarry tiles and glazed brick; five coats on the hooks with boots under them; two stools; a counter with a deep sink and cupboards over; the washing machine; a radiator; a runner down the middle; an umbrella; a chest of drawers by the way on; the food (and string) in a hamper | a card pinned up by the hooks |
| The garden room | 168 | 15 wide, 11 deep, tall | Tiled in a check; the far wall all frosted panes in birch frames with daylight behind; a frosted skylight between rafters; a bench of potted plants along the sill; a daybed; three cane chairs round a table; the way on in the west wall | folded on the table |
| The spare room | 169 | 9 wide, 10 deep | Papered and boarded, under the eaves on both sides; a small window with the night behind it; the bed made up; a bedside table with a candle; a blanket chest at the foot with the food in it; a wardrobe, a dresser with towels on it, a chair by the window, a rug, boxes by the door, a shaded light under the ridge | on the pillow |

No window shows outside: every pane has a solid backing (frosted glass in front of a lit wall, or a dark one), as the House's
other glazing does.

## How they are dealt

- **Among the ordinary halls** the five rest rooms are weight 1 each, and a rest room the reader has been in within the last six rooms
  (`QuietRooms.REPEAT`) is weight 0, so none comes round again soon.
- **When a rest is due**, the door is dealt the rest room that reader saw longest ago, or one they have never seen
  (`QuietRooms.rest`); the explorers' camp keeps its share as before.
- `LabyrinthPacing.quiet` covers all five, so everything that treats the quiet room as a quiet room (the Mother and strays' odds, the
  growls, wedges, the expedition rhythm, navigation aids) treats them the same way.

## The notes

Sixteen notes (`QuietNotes`), left by people who stopped to rest before the reader. The first is the quiet room's old note, word for
word; the others answer it, and sometimes each other ("I asked"; "who did you ask?"). Each reader counts their own rests: coming
into any rest room is one more (`QuietRooms.rests`), and whatever holds the note in that room opens on the note for that rest. Two
readers at one stand read their own notes. The note opens in a private reading view; it cannot be taken, and the quiet room's stand
keeps its book.

The notes never say who the reader is, never name a colour or an age, and give the reader nothing but company.

## Tests

`QuietRoomsTests` (exploration):

- `theRestRoomsAreBuiltWholeAndEachReaderReadsTheirOwnNote`: each new room is a calm gray room with no story, its two doors, room to
  stand at its way on, food in its store, every seat sittable, its note where a reader can find it, and light at the door; a reader's
  first rest opens the quiet room's stand on the first note (which cannot be taken, and the stand keeps its book); the next rest the
  next note; a second reader at the same stand reads their own first note while the first reader still reads theirs; and the pillow,
  the card and the table each open the next.
- `aRestGoesToTheRestRoomSeenLongestAgo`: five rests go to five different rooms, each then weight 0 among the halls; the sixth goes to
  the one seen longest ago; six rooms later each is weight 1 again.

`LabyrinthTests` counts the four in the gray pool from the start; `DomesticLabyrinthTests` accepts any rest room on the early doors;
`EntrancePlaytestTests.HALLS` holds them with the other halls.

Layout 86. No Witness, protocol or story change.
