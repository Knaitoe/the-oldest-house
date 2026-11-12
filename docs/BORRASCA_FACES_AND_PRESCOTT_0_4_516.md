# Faces, and Prescott at his door (0.4.516)

Two notes of the owner's for Borrasca, 12 November: "It might be worth wiring ghosts set's facial expressions in, too. Thinking
specifically for the sheriff and his final fight. Maybe let Prescott be encounterable after discovering the orrery, too?"

## Faces

The Ghosts set moves a face by moving the person's own painted brows and lip (`GhostsSkinMesh.standardExpression`). Borrasca's
cast has had that since 0.4.262 for two things only: Kelly with her compass and Kelly frightened, and the two men as they take
her. Now what a face is doing is looked up by whose it is and what they are doing (`CastModel.face`), and it goes from one look
to the next over a few ticks instead of all at once, each actor's own.

**The Sheriff** has a face for each thing he does:

- easy, when he tips his hat to a child;
- hard, behind his light, and when he warns;
- set, with his hands up;
- brows down hard and mouth pulled as a blow is drawn back, and all the way as it is thrown;
- open and hurt when he reels;
- pleading, beaten and sitting in the dirt;
- slack, dead.

His brows are under the brim of his hat, so none of that could have been seen. The hat now moves: a blow that makes him reel
knocks it up at the front, and it drops back as he steadies; once he is beaten it is right back on his head and his whole face
is open; when he is dead it is off, lying crown up on the ground beside his head.

**Kelly, Patrick and Kendra** grin while a game behind the store is on (a new gesture, `GLAD`, which changes nothing else about
how they stand or run). Kelly's two older faces are as they were, and so are the two men's.

**Prescott** has two: put down and bargaining, and dead. His cap goes the way of the Sheriff's hat.

The proof has a new page, `faces`: every look as a head, square on, and the Sheriff and Prescott reeling, beaten and dead as
whole figures.

## Prescott

The depot's papers have had him there all along. The night's sign-in sheet has "10:10 J. Prescott", and every page of the night
book is "Counted" by a Prescott, father and son. He was nowhere to be found on the last night.

Now, on the last night, once the reader has seen the machine (been within nine blocks of it, looked at it from within twenty,
or cut its power), he comes out to the door of his office on the yard and stands there looking at the refinery. He comes when
nobody sees him come. Within eight blocks, in plain sight, he speaks first, once, and what he says is by what the night is:

- the machine stopped: "It's stopped. All my life I've heard that, and I never once heard it stop."
- the Sheriff dead: "You've killed a county officer. I'd go home, if I were you."
- otherwise: "You were with two sixty-three the night she came up. I had you down as gone home."

A word to him gets the next of what he has to say, in order, the last said again:

1. "None come down before eighteen. That's written in the front of the book. My father wrote it, and I've kept it."
2. "The water ran red two years and there wasn't a child born in Drisking. There's been one every year since."
3. "I don't go below. I count what comes up, and I sign for it. Somebody has to be able to say how many."
4. Only to a reader who has found her room: "Your friend had a boy and a girl. They're in town. They're well. I could tell you
   the street."
5. "Go home. You've seen it. Seeing it doesn't change what it's for."

He is no fighter. One blow from the reader puts him down against his own door: "All right. All right. There's a safe in the
office. It's open." There is no safe to open; it is something to say. Down, a word gets a plea, four of them in turn, ending
"Do it, then. There's a Prescott away at school. They'll have him home by Christmas." A blow at him once he has been down a
second kills him. His body stays where it fell for the rest of the night, face down, the blood spreading under it as under the
Sheriff's, his cap beside him. The House says only: "He doesn't get up."

Blows at him do not count against the reader as blows at the town's people do.

Nothing waits on him. The ending does not ask for him, a reader who never looks at the yard never knows he was there, and
killing him stops nothing: the last plea says why.

### The account

Only a reader who met him has any of it: "Prescott was at the door of his office on the yard, watching the refinery." Then, if
he said it: "He told me her children were in town, and that he could tell me the street." Then what the reader did: "I killed
him there. He was still offering me things." or "I put him down against his own door and left him there, offering me things."

## Multiplayer

Every reader's Drisking is their own, with its own Prescott; one reader's is never seen or struck by another. Faces are eased
on each client for each actor it draws.

## What was built

- `BorrascaNativeArt.CastModel`: `face(person, gesture)`, the easing (`FACES`), `hat(person, gesture)` and the hat's hinge and
  its place on the ground, `falls(actor)` (who lies down and bleeds: the Sheriff and Prescott), `previewFace`.
- `BorrascaVisualProof`: the `faces` page; the workflow's view list has it.
- `BorrascaStory.GLAD`; `BorrascaGames.faces` sets it on the three while a game is on.
- `BorrascaPrescott`: when he comes out, what he says, being struck, being killed, the body, and the account's sentences.
  `BorrascaStory` shows him on the last night only once he is out, ticks him, and gives a word to him on that night to
  `BorrascaPrescott.talk`. `LiteraryActor.hurt` gives the reader's blows to him. `BorrascaCast.body` keeps his body.

## Checks

- `prescottComesToHisOfficeDoorOnceTheMachineIsSeenAndAGrownReaderCanPutHimDownAndKillHim`: he is not to be seen until the
  machine has been; he comes to his door only when unwatched; he speaks first, once; his lines come in order and the children's
  only to a reader who found her room; one blow puts him down and does not kill him, nor does one in the same moment; a blow
  after that does; the body stays at his door with the blood spreading under it; the account has what was done; what he said
  is in the journal; and the breaker still stops the machine.
- Both Sheriff cases and both games cases pass with the faces in.
- The Borrasca namespace and the quick checks pass.

## Left with the owner

- **What he says.** All of it is new, and all of it is his own account of himself. Nothing he says is checked against anything;
  "a boy and a girl" and "a Prescott away at school" are new facts about the town that nothing else in Borrasca has.
- **The safe.** He offers one and there is none. If there should be one, it wants something in it.
- **Killing him changes nothing.** No ending asks for it or notices it, apart from the account. The Sheriff's death is the same.
- **He cannot be met before the last night.** The owner's note says "after discovering the orrery"; as built that is the machine
  on the last night, since it is not found before then.
- **The faces are small.** A brow is a pixel or two at sixteen pixels to the block. They read at arm's length and not across
  the street; the hat does more than the brows.
- Not seen in a client beyond the proof page.

## Numbers

No protocol, Witness or layout change. New builds only.
