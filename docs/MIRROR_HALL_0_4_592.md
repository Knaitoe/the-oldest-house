# The mirror hall's glass (0.4.592)

The owner, playing on 14 November:

> "reflection isn't rendered properly in reflection room"

New builds only. Protocol 192: the hall tells a reader's client how late its glass runs, and a 0.4.591 client would
draw its old figure from that. No line, layout or Witness change: the hall is the same room behind the same door,
with the same three doors at its end and the same rule for which of them is real.

## What was there

- **The hall's long wall was panes of framed, tinted glass.** Each block of it had its own frame, so the wall was a
  grid.
- **A second hall was built behind it:** three doors, a row of lamps and three seats, set out as a mirror image of
  the real ones.
- **The reader's reflection was a figure drawn in that second hall.** It wore the reader's skin, tinted, with the
  coat and glasses the House read for them, and stood where the reader had been a second or two before. It had no
  hair, nothing in its hands and nothing the reader had put on, and it moved in steps: the server sent where it
  should be five times a second.
- **So it read as a window.** A reader looked through a gridded partition into another dim room with a small figure
  in it. Nothing in the glass was the room they stood in.

## What it is now

- **The wall is one mirror,** wall to wall and floor to ceiling, with no frame and no grid. It is the same glass as
  the quiet rooms' mirrors and the trial room's: it shows the hall in front of it, as the game draws it, reflected.
- **The reader is in it as themselves:** drawn by the player's own renderer, so with their House body, their hair
  and glasses, what they hold and what they wear, walking as they walk and crouching as they crouch.
- **They wear the coat the House read for them,** as the hall's old figure did (0.4.159: "the later mirrors of the
  hazard halls show the coat as well"). It is worn in the glass only, never on the live body.
- **A beat late, then two,** as the hall always was:
  - for the first twelve seconds of a visit the reader in the glass is one second behind;
  - after that, two seconds;
  - when the gap grows it grows gradually: the reader in the glass falls behind at half pace and never jumps.
- **Near the hall's end it stands at a door,** as before: once a reader is within the last stretch, the reader in
  the glass stops copying them and stands still in front of one of the three doors. That door is the real one. The
  others still send that reader back to the entrance.
- **Each reader sees only their own reflection,** as before. Another reader in the hall is not in the glass.
- **Nothing is built behind the wall.** The second hall's doors, lamps and seats are gone.
- **A row of lamps hangs by the glass.** The hall's one row left the floor by the wall almost unlit, and a reader
  who walked up to the mirror was a black shape in it.

How it is done, for whoever changes it next:

- **The reader's own client keeps what they did,** tick by tick, and plays it back late on a body of theirs that is
  in no world. The server says only how late, and where their door is. Nothing of where a reader was is sent, and
  no other client is told anything.
- **The wall hangs from one block** (its lowest, at the entrance end), which carries the whole glass.

## Checks

- **A new case** (`HazardVarietyTests.theMirrorHallsWallIsOneGlassAndRunsLateByEachReadersOwnVisit`), with two
  readers in the hall:
  - every block of the wall is glass, and exactly one of them carries it;
  - nothing is set out behind the wall: no door, lamp or seat;
  - the lamps by the glass are there;
  - a reader new to the hall is told one second, and nothing of where they were is sent;
  - near the end they are shown standing three blocks in front of the door that is really theirs, and that door
    lets them through; the other reader, still at the entrance, is not shown a door;
  - walking back up the hall they are shown as they were again;
  - twelve seconds into the visit they are told two seconds, by their own visit and not by the hall's age;
  - coming in again, they start at one second again, though the hall itself is far older than twelve seconds.
- **The hall's three existing cases** pass: the wrong door loops only its own reader and nothing is spawned for a
  reflection; the hall's painted shell; the hazard halls' layouts. So does the rest of their suite (172 cases).
- **Two real clients on a real server,** walked through the hall by a script, with frames taken:
  - standing still, the hall reflected from wall to wall with the reader in it;
  - walking along the hall, and just after stopping: the reader in the glass is still mid-stride a second later;
  - up at the glass, along the wall, and crouched;
  - at the hall's end: the reader in the glass standing in front of a door while the reader stands elsewhere;
  - the coat, the glasses and the hair on the reader in the glass (the script gives its reader a read);
  - the other client's reader standing in the hall where the glass would have shown them, and not in it;
  - the client's own record of how late the glass ran: one second, easing to two, then the door.
- **The trial room's mirrors,** in the same run, from in front and from the side.
- **The game's own renderer:** the whole client proof chain (63 proofs), in which the hazard halls' page of the old
  figure is gone and their two other pages are as they were.
- **The workflow's package check,** lifted out and run against a package built from this tree (see the night
  ward's notes for why).
- **A compile, and the quick checks.**

Not checked:

- **Nobody has played the hall** since the change. The frames are from a script.
- **The second reader's own glass.** Frames were taken on one client only. That the other reader sees themselves
  late in theirs rests on the case above and on both clients running the same code.
- **Held and worn things in the glass.** The scripted reader's hands were empty.
- **The hidden face.** A reader the House reads as unseen has their face covered in every glass; the scripted
  reader was not one.
- **The build's own checks do not draw this glass.** The look is a script run by hand. The proof page that drew
  the old figure went with the figure. A true mirror is drawn on real clients in the build's checks only in the
  coatroom, square on.

## Caught while checking

- **A mirror vanished when one block of it left the screen.** This was the fault in the first frames of the new
  wall, and it was not new: a block's own drawing is skipped when that one block is out of view, and a glass larger
  than a block hangs from one. A reader who looked along the wall with its first block behind them saw a bare wall.
  The same was true, less visibly, of every other mirror larger than a block:
  - the wide glass over the sinks in the Third Floor Girls' room;
  - the trial room's two mirrors;
  - the quiet rooms' standing mirrors;
  - the Winchester hall stand and the buffet's glass.

  Each now answers with the whole of its glass, and is drawn while any of it is in view.
- **The glass chose itself by its first block.** A mirror is reflected when the camera is near it, and near was
  measured from the block it hangs on: at the far end of a forty-block wall, the wall was too far away to be a
  mirror. Near is now measured from the nearest point of the glass.
- **Only the first twelve seconds of the hall's life were one second late.** The delay went by the hall's own
  clock, which is kept for good. Whoever was in the hall in its first twelve seconds saw the one-second reflection;
  every reader after saw two seconds from the door. It goes by each reader's own visit now.
- **The reader at the glass was a black shape.** The frames up at the glass showed eyes and little else: the
  hall's lamps hang down its middle, seven blocks off. Hence the second row.
- **The server kept a trail of each reader** (where they stood, how they were turned, their stride) only to send
  it for the old figure. Nothing reads it now, and it is gone.
- **I redrew the wall's own block, and the art record refused it.** The block's picture is one of the paintings
  the record holds to its 0.4.100 state. The mirror covers every block of the wall from everywhere in the hall, so
  the block's own picture is never seen in a new world; I put it back as it was rather than amend the record.

## For the owner

- **In a world made before this build the hall has no reflection at all.** It keeps its old wall and the second
  hall behind it, the old figure is gone, and the old wall has no block that carries a glass. The doors work as
  they did, so the real one can still be found by trying them, but nothing shows which it is. A new world has the
  mirror. New builds only is the standing rule; one line would hang the glass on an old hall's wall the first time
  a reader walks in, and I have not written it. Say if you want it.
- **Choices that are mine:**
  - the delay counts each visit, not the hall's lifetime (see above);
  - the reflection falls behind gradually, at half pace, where it used to jump from one second to two;
  - the lamps by the glass, which make the hall brighter along that wall than it was;
  - before the hall has spoken to a reader's client (a moment on arrival) the glass shows them as they are.
- **Other readers have no reflection in this glass,** as your design has it ("each reader sees only their own
  reflection"). In the quiet rooms' and the trial room's mirrors everybody in front of the glass is in it. Say if
  the hall should show them too; they would be shown as they are, not late.
- **The trial room's two mirrors do not show the read's coat.** They were built after 0.4.159 as plain glass, and
  I have left them as they were. Say if every glass after the coatroom should show it.
- **The figure at the door stands three blocks in front of it,** facing back down the hall, as the old one did.
  From the middle of the hall it is plain which door it is at; from close beside the glass it takes a moment.
