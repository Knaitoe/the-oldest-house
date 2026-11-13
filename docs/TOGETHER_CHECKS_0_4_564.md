# Two readers in every talking scene (0.4.564)

The owner, 13 November: "remember to continue other vignettes and then confirm intended and functional multiplayer compatibility
for each", and of the cast checks, "You should standardize your tests with Tess and run them against the dialogue heavy, branching
vignettes too."

The multiplayer audit begins here, with the standard cast checks (0.4.560) run on two readers at once.

New builds only. No layout, protocol or Witness change. No change to any scene: these are checks.

## The checks with two readers

`TogetherConductTests` (namespace `together`, 18 cases) takes the eighteen talking scenes of `CastConductTests` and brings in two
readers. Each comes in as a player would, through the House's door (`SceneInstances.enter`), where the scene has copies:

| How the second reader comes in | Scenes |
|---|---|
| Into the same scene, written for a party | Pinehook, The Last Crossing, The Supper, Growing Things, Last orders, Bike night, the Ghosts set |
| In with the first, two to a house | The Pell house |
| Into the one room the House has for everyone | The House on the Corner, The Kept, The Drawing, Three Fingers, Speak Up |
| Into a copy of their own, a million blocks west, while their door waits | A Place for You, Second Person, People Lights, Mr. Harrigan's study, the Confession |

Each case checks that the second reader went where the story says they should. Then both readers go round everyone they find at
once, for the same half a minute, each speaking to people and answering whatever the conversation panel asks. The cast is held to the
same checks as with one reader: one body to a part, nobody appearing, vanishing, jumping or standing in a block where a reader can
see, every line said in earshot, and a turn of the head to whoever it is said to. Readers together are watched as one scene; a reader
in a copy has their copy's people watched for them.

## What the checks needed, with two readers

Three rules were written for one reader, and are now written for any number:

- **The people a reader meets are the ones the game shows them** (`LiteraryActor.broadcastToPlayer`). A second camera on the Ghosts
  set meets the lead's family, which is how the crew works. One reader's own people are kept from the others, so they neither
  appear, vanish nor jump for those others, and never count as speaking to them.
- **An answer turns to the reader who asked.** Before, every line was checked against the nearest reader. When two readers speak
  to the same person on the same tick, the answer may go to either.
- **A line nobody asked for may go to any reader in reach and in sight.** A line said with no reader within twelve blocks in sight
  is said to nobody, and has nobody to turn to. That is the person's own rule (`LiteraryActor.ADDRESS_RANGE`). A reader brought near
  afterwards is not who the line was said to.

## What testing found

Nothing in the scenes. With the rules above, all eighteen pass with two readers, and the eighteen one-reader cases still pass. The
first runs failed on four things, each a fault in the test:

- **The Ghosts set's second camera found nobody to talk to.** The test reader looked only for people with no owner or with
  themselves as owner. The family belongs to the lead, and the game shows them to the lead's cameras.
- **The Pell house gave the second reader a house of their own.** The test had put the first reader inside without the door. The
  door records a house as its reader's, and a partner finds them that way.
- **Three Fingers: Walter turned to the reader he was speaking to.** The check expected the nearer one.
- **The Pell house and the Drawing: two readers acting at once.** In the Pell house, both readers spoke to the same seated person
  on the same tick, and she answered the second. In the Drawing, a villager greeted the square with neither reader within twelve
  blocks, and a test reader was brought near before the check looked.

## Local runs

- `together` 18 of 18 and `cast` 18 of 18, twice.
- The three lake house cases that hold Tess to the same checks.
- The quick checks and the structure halves check. `together` runs in the structure suite's rest half (`tools/structure_groups.py`).

## Run 1596 (0.4.562)

Every job passed but one. **live-multiplayer** stopped at the culvert (phase 49): both readers pushed hand over hand under the
roof, left and right, and both canoes then glided to a stop at the north mouth and stayed there for the rest of the two minutes.

The hands and the paddles disagreed about where the walls end, a fault in 0.4.560's culvert. The paddles were taken away while the
middle of the hull was between the walls (`LakeHouseCanoe.pushable`), but a stroke from the front bench needs the walls where that
bench's hands reach them, a third of a block ahead of the middle (`wallWithinReach`). For the last third of a block of either trough,
going out, the paddles were gone and no stroke could land. A canoe that glided through that stretch went on, and one that came to a
stop in it could go no further. Both of the proof's canoes stopped there; a reader could have too.

Now both ask the same question: the walls are looked for where the front bench's hands reach them, and the paddles come back the
moment those hands are past the walls' ends, at either mouth. The trough case (`theBackBenchPushesTooAndTheMouthsTakeTheBow`) puts a
canoe where both stopped, and then walks one along the whole culvert and past both mouths, both ways and across its width, holding
that nowhere are the paddles taken where the front bench cannot push. The case fails without the fix, at the place the proof's
canoes stopped. The proof was the first run to take a canoe out of the north mouth since 0.4.560: run 1595 stopped before the canoes.

Locally: the lake house namespace, 21 of 21.

## Next in the audit

Every story in the Witness pool (88), how it holds more than one reader, and what proves it: docs/MULTIPLAYER_AUDIT.md.
