# The rest of the cast, held to the same manners (0.4.600)

The owner, 13 November:

> You should standardize your tests with Tess and run them against the dialogue heavy, branching vignettes too. Many of those NPCs also did not behave properly

and, of every story: "confirm intended and functional multiplayer compatibility for each".

0.4.560 wrote the checks (`gametest.CastConduct`) and ran them on sixteen talking scenes, 0.4.564 on two more, and 0.4.565 on sixty-six scenes with two readers in each. Three talking scenes were left to "their own cases", and those cases hold nobody to the checks: Johnny in his halls, the familiar kitchen's visitors, and the Tall Grass. This release puts the first two under them. The Tall Grass has been under the two-reader checks since 0.4.565.

New builds only. No layout, protocol or Witness change.

## What the checks found

### The kitchen's visitors

- **Made in the room.** A visitor came into being a step inside one of the kitchen's two far doors, with the sound of a door opening and no door opening. A reader looking that way saw nobody, and then somebody.
- **Gone the same way.** They walked back to that spot and were gone from it, with the sound of a door shutting.
- **Put back a step.** Getting up, a visitor was set down a block out from their chair at once. Struck while still on the way in, they were set down at the chair from the doorway.

The case's first run passed. Its reader stood where the door had left them, facing the wrong way, and saw none of it. A reader who hears a door looks at it, so the case's reader now watches: with nobody in the kitchen they stand in line with the doorway and look at the door, and while somebody is coming in or going out they look at them, on every tick.

### Johnny, as the claimant

Leaning out to a reader before he draws back (0.4.282), he walked toward them from where he stands behind the corner. A reader who had come past the corner had him walk into it: the check found his body in the wall's panelling seventeen ticks in.

His first meeting at the lamp and his holding of the heavy door went round with no fault.

### One of each

Not a fault of manners, and not one the checks could find, since no case has two readers in two places: Johnny at the kitchen table for one reader could be holding the door of his hall for another at the same moment. He is one man (docs/TRUANT_0_4_264.md: "There is one of him"). Holloway could sit in the kitchen while a reader was at his camp with him.

## What changed

### They come and go by the kitchen's own door

The kitchen's own door is the one a reader comes in by, and it is a door that opens: a reader walks in through it from the passage behind it. The two far doors never open (a reader who uses one is taken through it), and behind them is the room's outer wall, so nobody can be seen to come from there.

- **Coming.** A visitor is made in the passage, two cells back from the shut door, where no reader can see. They stand there a moment (`DOOR_WAIT`, twelve ticks). The door opens. They walk in, to a step back from the nearest free chair, and sit.
- **Going.** They get up, take a step back from the table, walk to the door, which opens for them, go through, and go a little way along the passage and to one side. The door is shut behind them. Once it has shut and nobody can see them, they are gone.
- **The door is a real door.** It opens and shuts as the block does, with the door's own sound, heard through it as any door is (`LabyrinthDoors.setDoorOpen`). The two sounds played before are gone.
- **A door the reader left open stays open.** A visitor shuts the door only if it was they who opened it. A wedged door stays wedged.
- **Never shut on a reader.** With a reader standing in the doorway, it waits.
- **Nobody comes while the passage is watched.** If every place behind the door is in somebody's sight (a reader stood in the passage, or looking down it through an open door), the kitchen's clock tries again a second later.
- **They walk from where they are.** Getting up is a step, not a move. A visitor struck on the way in turns round where they stand.
- **Watched to the last.** A reader who follows a visitor into the doorway and watches them in the passage keeps them there half a minute at most; then they are gone regardless.

### Johnny leans along what is clear

`Truant.walkClear`: a step that would put his body into a block is not taken. He goes along whichever way is clear, or stays where he is. Leaning out to a reader past the corner, he comes out along the passage and not into the wall.

### One Johnny, one Holloway

- Johnny does not come to the kitchen while a reader is in any of his own places (`Truant.withSomeone`). Holloway does not come while a reader is at his camp.
- If a reader comes into one of those places while he is sitting in the kitchen, he gets up, says his leaving line, and goes (`KitchenVisitors.elsewhere`). Johnny's is "Somewhere to be. Probably not, but still."; Holloway's is "I'll get back. There's a lot of dark to keep an eye on." Neither line is new.
- For the few seconds of his walk to the door there are still two of him in the House, in two places no one reader can see at once.

## Checks

Four new cases in `CastConductTests` (namespace `cast`, now twenty-two):

- **`johnnyAtHisLamp`**: a first meeting. The lamp's hall is built by its own builder, a reader comes in by its door and goes round him for half a minute, through the half face, the drawing back and the talk at the lamp.
- **`johnnyTheClaimant`**: a reader past twenty passes with a history with him (the one his own cases use), so that he is the claimant. It fails without `walkClear` ("stands inside a block", the wall's panelling).
- **`johnnyAtTheHeavyDoor`**: the held door.
- **`theKitchensVisitors`**: the kitchen built by its own builder and its clock run on, so that in half a minute somebody comes in, sits, is talked to through every answer the panel offers, and goes, and the next is on the way. The reader watches as above. Besides the cast checks it asks that somebody came and sat, that somebody went, that the door opened for both, and that it is shut behind whoever has gone.

Changed:

- `KitchenTests.visitorsComeInSitDownAndAnswerWhatTheyAreGiven` asks that a visitor is made in the passage and not in the room.
- `TalkBot` goes up only to people a reader can reach (a scene can keep somebody behind a shut door), and a case can ask something of its scene besides the cast checks.

### What testing found

- **The kitchen case passed while the fault was there.** Its reader was not looking. See above.
- **A visit that would not end.** The case ends each visit after seven seconds at the table. Its reader's third answer on the panel is "Stay as long as you like.", which puts the kitchen's own count back a minute, so nobody went. The case keeps its own count of their sitting now.
- **The kitchen is kept.** `FamiliarRoom.build` leaves a kitchen that already stands as its readers left it. The fixture clears every entity out of a scene it built, and so took the clock off the wall for the next case to come to the kitchen, which failed on it. The case no longer counts the kitchen as the fixture's, and shuts the door it leaves.
- **A look at a shut door sees the cell behind it.** `HouseWatchers.sees` counts a point as seen if the look stops on a block next to it. A visitor made in the cell right behind the door was "seen" by a reader looking at the door from the room. They are made a cell further back.
- **Read over before committing.** With nobody left in the kitchen, a door a visitor had opened was shut without asking whether a reader stood in the doorway. It asks now, as everywhere else. No case stands a reader in the doorway.

### What was run here

Local runs on the release's tree, before the workflow's own:

- the four new cases on the tree as it was: the claimant failed (in the wall), the other three passed, the kitchen among them;
- every case of the cast and two-reader namespaces with the Truant's, the kitchen's and the familiar room's own cases in the same server (128), after the changes;
- every case of the exploration and cast namespaces together, as the workflow's main half runs them (196);
- the Truant's, the kitchen's and the familiar room's own cases with the four new ones (26) again, after the last change above;
- the kitchen case with a visitor made in the room in front of the door instead of behind it: it fails ("appeared where a reader could see"), and the change was put back;
- the workflow's tool checks under its own Python, and the package and branding checks on a built jar.

The client's proof chain was not run: nothing a client draws has changed.

## Not covered

- **Johnny's other turns.** Only the lamp (as stranger and as claimant) and the heavy door (helping) go round under the checks. The hand in the doorway, the covered light, the shove, and his service halls, yard and copying room are walked by his own cases, which do not hold him to the checks.
- **The Tall Grass with one reader.** It is under the two-reader checks only.
- **No client has drawn it.** Nobody has watched the kitchen's door open for a visitor in a client.
- **The passage as a reader leaves it.** After a reader has arrived, the passage behind the kitchen's door is a copy of the hall they came from. The cases run with the plain passage a new kitchen has. A visitor is made only where there is room to stand, so a passage with something in the way has them made to one side, or not at all until it is clear; no case builds one.
- **Two readers in two places.** No case puts one reader in the kitchen with Johnny and another in his hall. The rule is two lines (`elsewhere`, `Truant.withSomeone`).

## For the owner

- **The door they use.** Visitors came by the far doors and now come by the kitchen's own. Coming by a far door would need that door to open, onto something built behind it. Say if they should.
- **Johnny and Holloway get up when somebody finds them at home.** The other way round was possible: leave him at the table and have his hall stand empty. This way a reader who goes looking for him always finds him.
- **The numbers are mine:** twelve ticks behind the door; half a minute at most for a reader who watches a visitor in the passage.
