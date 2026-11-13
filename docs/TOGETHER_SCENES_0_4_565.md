# Two readers in every story (0.4.565)

The owner, 13 November: "confirm intended and functional multiplayer compatibility for each".

0.4.564 took two readers into the eighteen talking scenes. This release takes two readers into every other story in the Witness pool
that the House builds in its own halls: sixty-six more. With the private stories' own cases, every one of the 88 now has a case that
takes more than one reader in. The whole list, with how each story holds its readers and what proves it, is
`docs/MULTIPLAYER_AUDIT.md`.

New builds only. No layout, protocol or Witness change.

## The cases

`TogetherSceneTests` (namespace `together`, 66 cases, one batch each):

- The scene is built as the House builds it (`LabyrinthBuilder.buildOne`: the builder, the onward door and the House's finishing
  passes), not by a test's own hand.
- Both readers come in through the House's door. The second waits in the hall until their door is ready; the door decides where they
  go (`SceneInstances.enter`): into the first reader's scene where the story is one room for everyone or written for a party, into a
  copy of their own where it is one reader's.
- Each scene's own welcome runs for each reader exactly as the door runs it. The door's arrival now calls one method for all of them
  (`LabyrinthDoors.scenesArrived`), which the cases call too; nothing it does changed.
- The second reader must be where the story says.
- Where the story has a paper, each reader reads their own, and one reader's reading is never the other's.
- Both readers then go round whoever they find for a quarter of a minute, speaking to them and answering what they are asked, and the
  people are held to the standard cast checks (`CastConduct`) for each reader.

## What the cases found, and what changed

The first run: 57 of 66. Five faults were in the game:

- **The cabin at the end of the world** (`CabinBargain.say`). The four visitors spoke in chat, and over each one's head stood only
  their own name, said again at every line of theirs; the journal kept the name as what they had said. Their lines are captions now,
  with the name before them, and they turn to the reader; the journal keeps the words. While a visitor's body is not there, their
  words come in the action bar, quoted, as the House's other unseen voices do.
- **Whom a person turns to** (`LiteraryActor.address`). A person picked the nearest reader to turn to from every player about,
  including readers who could not see them. At the cabin, one reader's visitors turned to the other reader, who stood nearer and could
  not see them, and away from their own. Now only readers the person is there for.
- **The closing hall** (`ClosingHall.children`). A child lifted onto a reader's back arrived there at once from up to three blocks
  away; they climb up over a few ticks now. A child by the hand stood behind wherever the reader was looking, so a reader looking about
  swung them round in bursts; they keep to the reader's body now, as a held hand does.
- **Movie Night's canoe** (`LiteraryActor.seatFront`). A teenager in the canoe, spoken to from behind, tried to turn right round and
  was held facing the bow, since a boat lets nobody aboard turn past its sides. Someone in a boat is seated now, facing the bow, and
  turns their head as far as a head turns, as anyone seated does.

The checks themselves, written for one reader on foot, now measure what readers see (`CastConduct`):

- **A part a scene has several of** (`several`): the Masque's fourteen dancers to a reader, the two parents in the split feed, the four
  of the painted family. One more than the scene has is still two bodies for one part.
- **Somebody brought out on purpose** (`emerges`): the woman on the deer road, who climbs out of the doe.
- **A cut**: somebody already there when a scene puts a reader somewhere did not appear to them (the Masque puts a reader who moved at
  the chime back a room, where the red figure stands).
- **Steps**: a stride counts only after a tick standing still, and not near a reader who has just been put down somewhere (a test's
  reader is put beside each person in turn, and whoever keeps with them hurries after them).
- **The swimmer** swims in her lane; **a child carried on a back** is held off the ground.
- **Camp Blood**: the readers walk up to everyone, the man included, and he kills them. Only there is a reader's death not a fault.

- **Second Person** (`SecondPerson.stage`): the resident, stalled by a reader standing in the way, said "..." again three seconds
  later, as a second reader moved about. The line is kept to once in twenty seconds now, however the readers move.
- **Movie Night's lines to the film** (`MovieNightCast.speak`): Lynn called "Bobby, don't. Not in the water." on her frame of the
  film with both readers thirty blocks off. A line to the film goes unsaid with nobody in earshot, as an idle line already did.
- **Last orders** (`LastOrders`): two readers along the bar each had "Go on. Steadies the hand." a second apart. A glass pushed while
  the last offer still hangs in the air gets a word of its own ("And one for you.", "One each. Go on.").

## And the checks, after the fixes

The run after the first fixes found five more; the three above were the game's. Two were the checks':

- **Somebody who says something new looks where that goes** (`CastConduct.supersede`). Hank answered one reader at Costume Night and, a
  tick later, called the next number to the other; he was held to the first.
- **A reader who turns round hurries whoever keeps with them** (`CastConduct.turnedWithin`): the closing hall's child held by the hand
  swings round with a test's reader who looks from one person to the next.
- **Bly's Lady** glides her lane to the pond and out over its water before it takes her, as a ghost does; she is held off the ground on
  purpose (`floating`).

## Run 1597 (0.4.564)

Every job passed but two: the build job was still running, and **live-multiplayer** passed its whole expedition ("LIVE EXPEDITION CHECK
PASSED") and then failed the log check that no reader was told they moved too quickly: one of the two readers was, as they came into
the trailer for the Goatman night.

The second reader through takes a cousin's place, and the trailer puts them where that cousin stood, at the camp (`RoleSpots.stand`),
in the same tick as the door put them through. The server was behind ("Can't keep up! ... 95 ticks behind"), handled that reader's
answer to both moves and their first step in one go, and measured the step from where the door had put them, 66 blocks back, not from
the camp: "OTHProofA moved too quickly! 10.0,0.0,-66.0", and a jolt back to the camp.

The game measures a reader's steps from where they stood when the tick began. A move the game makes did not move that mark; only the
next tick did. The House's door already set it after its own move (`HouseInternalTeleport.shift`), and the House's other moves did not.
Now every move does (`TeleportBaselineMixin`, on `ServerGamePacketListenerImpl.teleport`): a reader the game puts somewhere takes their
next step from there, however many moves a tick holds and however soon the step comes. Vanilla's movement checks are otherwise as they
were.

`SharedDoorTests.aReaderPutInASecondPlaceStepsOnFromThere` puts a reader through a door and then on to a second place in one tick, and
has their game answer and step before the next tick. Without the fix it fails exactly as the proof did ("moved too quickly! 8.1,0.0,
-11.0", sent back); with it the step is taken from the second place.

## Local runs

- `together` and `cast`, 102 of 102; before the last fixes, 102 of 107 with the lake house's 5 (the faults above), and 99 of 102
  before that.
- The two shared-door cases, the new one first without the fix (it fails), then with it (both pass).
- The quick checks and the structure halves check.
