# The Pell house, the owner's playtest notes: the afternoon and the evening (0.4.570)

From the owner's notes on playing the Pell house on 13 November (the earlier batches: `docs/PELL_HOUSE_NOTES_0_4_566.md` to
`docs/PELL_HOUSE_NOTES_0_4_569.md`). New builds only. No protocol, layout or Witness change.

This release also carries the fix for what run 1600 found in the clapping room (the last section).

## What the people do while the house waits

The owner: "the NPCs should also have more idle behavior in Pell house, as they do on ghosts set".

Until now June stood at the stove, Tilly at the foot of Nessa's bed and Callum at the end of the hall, each on one spot, until the
hour moved them. Now each goes about small things of their own while the house waits on its hour, as the Ghosts set's family does
between takes. They walk through the house's own doors and round its furniture, one errand at a time, a while apart.

**When.** In the afternoon, from the readers' settling in until June calls everybody to dinner at six; and in the evening, while
June watches her tape, until bedtime. A week in, the afternoon's begin once June has welcomed the readers back, and June's own wait
until her hand has been seen to. Not on the last night.

**June, in the afternoon.**

- She stirs the pot at the stove. "Nearly there."
- She runs the tap at the sink. "That tap's wanted a washer since the winter."
- She wipes down the end of the table. "Four places."

**June, in the evening.** Once in a while she gets up from her chair, goes through to the kitchen and puts the kettle on, and goes
back to her tape. "Kettle's on, if anybody wants one." The television stays on while she is up.

**Tilly, in Nessa's room.**

- She goes with her cane to the desk under the window and feels along it. "Is it still light out?"
- She sits on the edge of the bed for a while.
- She finds the height marks by the door with her fingers. "Somebody measured themselves here."
- She puts her hand to the wardrobe. "There's still clothes in here."

Her cane taps as she walks. A reader playing Tilly decides for themselves what Tilly does; none of these happen then.

**Callum, at the end of the hall.**

- He wrings the wet out of his jumper, and it drips.
- He goes along to the school photographs and looks up at them.
- He sits down against the wall for a while, and watches whoever is near.

He says nothing, as before. Water still drips from him now and then wherever he is.

**The lines.** Each line is said once a visit, half-way into the errand, and only if a reader is near enough to hear it.

**Being noticed.** Someone at an errand keeps their eyes on it. A reader who comes within three and a half blocks is noticed: June
and Tilly turn their heads to them, Callum turns round. A hand on any of them still gets the lines it always got.

**The hour.** Nothing here changes what the visit does or when. When six comes, or half past eight, whoever is out on something
goes straight back to where the day put them, and June's call to dinner and her bedtime wait until all three are back. The wait is
a few seconds at most.

**Each house is its own.** A second house (0.4.547) keeps its own errands, turns and lines.

## Being alive while standing

Standing on one spot, the Pell people used to be still but for their heads. Now, drawn over every pose but sleep and lying still:

- they breathe: the arms rise and fall a little, the head with them;
- standing, they shift their weight from one leg to the other now and then, each on a clock of their own;
- June's, Tilly's and Gail's heads drift a little, as a person's does who is not looking at anything;
- while a line of theirs is up, they nod with it, and June's hand moves if it is free.

Callum's head does not drift: he looks where he looks.

Looking up at the photographs, Callum's face is his "up" look (0.4.569). Feeling her way, and at the height marks, Tilly's is her
listening look.

## How it is built

- `labyrinth/PellIdle`: the errands (whose, the way there, what is done, for how long, with what sound, the line), each person's
  turn through theirs, and the window the house is in. `PellVisit.tick` runs it once a tick after it has walked its walkers.
- Six gestures on `PellPeople` (stirring, wiping, washing, feeling, measuring, wringing), posed in `LiteraryRenderers`. The
  actor's gesture field now holds values up to 63 (it stopped at 31).
- `LiteraryRenderers.pellLiving` is the breathing, the weight, the drift and the nod, called from `PellFaces` for every Pell
  person.
- Five sounds, each with a subtitle, made by `tools/generate_pell_audio.py` (20 cues now): a spoon round a saucepan, a tap
  running, a cloth over a table, a kettle coming to the boil, water dripping.

## Checks

- **The case.** `PellHouseTests.whileTheHouseWaitsTheyGoAboutTheirOwnThings`: all three stand where the day put them at first;
  June stirs and says her line to a reader near her; Tilly goes to the desk and feels along it; Callum wrings his jumper; the
  visit has not moved on meanwhile; at six all three are back, June calls, Tilly and Callum take their places at the table; in the
  evening June leaves her chair for the kettle with the television still on; at half past eight all are back and she sends them
  to bed.
- **The Pell house namespace**: 54 cases.
- `tools/generate_pell_audio.py --check` is in CI and the quick checks.

## The clapping room: the blindfold fell off the wall (run 1600)

Run 1600 (0.4.568) passed 25 of its 26 jobs. The build job failed one case in the full suite: in the clapping game, the copy of the
room made for a second reader had its blindfold "neither on the wall nor owed to it".

**What was wrong, in the game and not only the test.** The blindfold hangs in a frame on the wall above the bed's post. The post
was a fence. Since 0.4.220 the builder joins every fence it lays to what stands beside it, so the post grew an arm into the wall
behind it. A fence's arm collides a block and a half high: it reached into the cell above, where the frame hangs. Vanilla drops
anything hung on a wall that touches a block, the next time it looks, which is every hundred ticks. So in every clapping room
built since 0.4.220, the blindfold and its frame dropped to the floor five seconds after a reader came near, and lay there as
loose items, which are gone after five minutes. The case had only ever passed by looking at the wall sooner than five seconds
after the room loaded; in run 1600 the room's other chunks took longer than that to come in.

**What changed.**

- The bed post is a block of its own (`HouseBlocks.BED_POST`, "Bed post"): it looks as the bare fence post did, joins nothing and
  collides one block high.
- The builder no longer grows an arm that would reach something hung on a wall (`BuildBlocks.join`): the frame or painting stays,
  and that rail stops short of the wall. Every other arm is joined as before.

**Checks.**

- `CoopTests.theClappingGameIsPrivateAndEachReaderPlaysItInARoomOfTheirOwn`: in the dressed copy, the post is the bed post; both
  frames are asked what vanilla asks them (`survives`); 130 ticks later, with the wall ticking, the note and the blindfold are
  still up and nothing of them lies on the floor. Before the fix this failed every time once the wait was in.
- `LabyrinthTests.hideAndClapRoomHasOpenFloorAndABedWithItsPost`: the post joins nothing and stands no higher than its block.
- `LabyrinthTests.aFenceJoinsTheWallBesideItButNeverThroughSomethingHungThere`: a fence beside a wall joins it; the same fence
  under a frame does not, and the frame stays.
- `ArchitectureTests.nativeEverySceneHasSupportedDetailAndPreservesOriginalsOnUpgrade`: with the whole House built, nothing hung
  on a wall in any finished scene is about to fall.

## Still to come

The rest of the notes, in the next releases:

- Things for readers to do through the day, and ways to be with Tilly.
- The interior.
- The flower bed.
- Each vignette door's own bleed and sound in the halls.
