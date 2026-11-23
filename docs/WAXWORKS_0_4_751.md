# The waxworks 3: the shift (0.4.751)

The third part of the waxworks (`docs/WAXWORKS_AND_COLD_ROOM_0_4_749.md`, section 1). The night guard's shift, from eight minutes to midnight to six, its rounds, and the figures that move while nobody is looking. The monitors' live feeds come next, in part 3b; the account and the House's dealing in part 4.

## The clock

- The **time clock** in the staff corridor takes each reader's card once. Punching in puts that reader on the shift; the first card starts the night. A second card is refused, and a spectator punches nothing.
- **One clock** for the museum (`WaxShift.Night`), two minutes of play an hour: 11:52 to midnight is 320 ticks, each hour 2,400, six o'clock at 14,720 ticks (about twelve minutes). It runs only while someone on the shift is in the museum.
- The **office clock** shows it: its hands at 11:52, then on each hour from twelve to six. Each hour is said once to whoever is on the shift.

## The rounds

- Each hour wants a **round**: every gallery counted.
- A gallery is **counted** when the beam of someone on the shift has been on each figure standing in it this hour (within ten and a half blocks and fourteen degrees, the chest or the eyes in sight; the booth's glass, the ropes and the plaques do not block it). The count goes in the log, and the reader is told: "The Hall of Statesmen, counted: seven."
- The **twenty-fourth** adds to the Hall's count when the beam finds him ("counted: eight"), whether before or after the Hall is counted. A round does not wait for him; finding him is kept on the reader's record (`FoundTwentyFourth`).
- With every gallery counted, the hour's round is in the log and everyone on the shift is told. The **log** on the desk says how many of the six rounds are written up, and how many things have been noted out of place.

## Irregularities

- Every half hour from about the fourth minute of the night, in a gallery nobody is in, on a figure nobody could see, one figure is **turned** on its mark to face the gallery's door, or (one time in three) is **gone** from it. Never the twenty-fourth, the copying statesman or the one under the sheet.
- Unnoticed for half an hour, it **comes nearer**, always unseen: to the ring outside its gallery, then to the ring by the office door, then into the office.
- Someone on the shift who **looks at it** for a second and a half (within twelve blocks and twelve degrees, with the light on, or within four blocks without it; in the office, without the light) notices it. It goes in the log and on that reader's record (`Noticed`), they are told where it was ("That one is out in the corridor. It goes in the log."), and the next time nobody sees it, it is back on its own mark. Someone not on the shift notices nothing.

## Failure and six o'clock

- **Three in the office** end the night: the screens go dark, everyone's flashlight goes out and is gone, and everyone on the shift is told and put out of the museum (`LabyrinthDoors.sendBack`), unfinished (`Failed`). Nothing harms anyone. A reader in the museum who never punched in is not failed.
- **At six**, with all six rounds in the log, the shift is **kept**: a key turns in the front door and the day man calls down the corridor, and everyone who punched in and is still there has it on their record (`Kept`, with `Rounds` and the night's `NoticedTogether`). Short a round, the shift simply ends (`Short`).
- Once the museum has been empty for ten seconds the night is forgotten: the figures go back, the screens come on, the clock goes back to eight minutes to.

## The look, now each reader's own

The design gave the look to each reader, and part 2 had shared it. From this part a figure's head turns for the reader who has it at the edge of their sight (while nobody looks straight at it), drawn by that reader's own client (`WaxLookPayload`); the body in the world never turns its head, and another reader's figures stay as they were set. A figure the shift moves starts again from where it was set. The flashlight's beam now reaches nine and a half blocks, as the design asked.

## More than one reader

- Shared: the clock, the rounds and counts, the irregularities, the log, failure and six o'clock.
- Each reader's own: their card, what their beam lit and counted, what they noticed, their record of the night, the look, the blink and the mirror.
- Spectators punch no card, count nothing and notice nothing.

Protocol 206 (the look payload). No new block, texture or sound: the clock and screens use their existing stages, the key in the door a vanilla sound.

## Tests

`WaxworksTests#theShiftItsRoundsAndWhatMoves` (batch `waxworks_shift`): no night until a card is punched; one card each, a spectator none; the clock runs while someone is on the shift; the beam on each figure of the foyer and the Hall counts them, three and eight with the twenty-fourth; every gallery counted puts the round in the log and the desk says so; the reader not on the shift has counted nothing; an irregularity is made while everyone is in the office, never on the three that cannot move, and is turned or gone; unnoticed it comes into the ring; someone not on the shift looking at it notices nothing, the night guard with the light does, and it goes in the log and on their record; unseen it goes back to its own mark; three in the office end the night, dark screens, the light taken, the shift unfinished, the other reader not failed; emptied, the night is forgotten; a second night kept at six for whoever punched in and for nobody else. The flashlight case now checks the look per reader (sent to that reader's client, not to the other, and the body in the world unturned). The museum case expects the punch and the log's new lines.

## New text

For the owner's review: the punch ("The clock takes the card and stamps it 11:52. The night shift has begun."), a second card, the stopped clock, the hours, each gallery's count, each round, what is noticed in each place, the log, failure, six o'clock kept and short.
