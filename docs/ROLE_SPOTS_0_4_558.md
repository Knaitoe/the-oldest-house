# Where a part begins (0.4.558)

The owner, 13 November: "Did you ensure players are placed in their appropriate spots? Player 2 in Goatman spawns at a cousin's
location, replacing said cousin for that vignette and custom instructions for their role? Etc"

Every story where a reader can take somebody's part was checked for where that reader begins and what they are told.

| Story | The part | Before | Now |
|---|---|---|---|
| The Goatman | a cousin's place (0.4.150, 0.4.418), and the gas run when it is that cousin's | at the head of the trail behind the latecomer, walking the trail to the camp | on that cousin's own spot at the camp, the cousin gone; told their chair, their bed, the generator if the gas run is theirs, and that somebody is still coming up the trail |
| The lake house | Tess's place, or Theo's (0.4.156) | at the boathouse door beside the diver | where Tess waits: on the dock, or on the raft by the compressor on the helmet visit, with her flashlight, and told where they are |
| The Shallows | her feet in Lonnie's place, or the bank (0.4.157) | at the carry place, in Lonnie's stance | unchanged |
| The Ghosts Set | a second camera (0.4.404) | by the crew, with Barry's word | unchanged |
| The Pell house | Tilly (0.4.545) | wherever they take up the cane | unchanged: the part is taken where the reader stands |
| The séance, the yard's stories, Last orders, Polybius, Pinehook, The Last Crossing | no cast member's part: every reader is a guest or one of the crew | | unchanged |

## The Goatman

- A reader who takes a cousin's place (anyone in before supper while a place is open, 0.4.418) begins where that cousin is: at
  the cousin's place by the fire before supper, or at their seat once they are called in (`GoatmanVignette.place`), facing the way
  the cousin would. They stand on the spot when it is clear and has ground under it, else on the nearest of the ways up to it,
  else beside it (`RoleSpots.stand`). The cousin goes as they come, so there is one child where there was one.
- The trail is done for them: they count as arrived and greeted (`GoatmanVignette.STOOD_AT`), so the trail is the latecomer's
  alone and it never pulls them back to its head.
- They are told their place (`GoatmanVignette.keptPlace`): with the others since the afternoon, their chair at the table, the bed
  they sleep on, the generator and the can in the shed when the gas run is theirs, and that somebody is still coming up the trail
  while anyone is.
- At dawn the evening keeps who came up the trail and who was at the camp already (`Trail0558`, `Camp0558`, `AtCamp0558`), and
  each one's account says where they began: the stand-in at the camp all afternoon when the latecomer came up the trail late, the
  latecomer finding the others there in cousins' places. Older records keep their old line.
- A friend who comes into a second trailer takes a cousin's place there in the same way.

## The lake house

- The reader in Tess's place begins each visit where she would be (`LakeHousePairs.tessSpot`): on the dock where she waits for
  the diver, or on the raft by the compressor she keeps going on the helmet visit. They face the boathouse door as she does, have her
  flashlight (they no longer pass the nail by the door), and are told in grey where they are. A reconnect partway through a visit
  leaves them where they are.
- Tess herself still slips away, unseen when she can be, as she has since 0.4.156.

## What testing found

The Goatman case's first run found the stand-in back at the head of the trail. The game had placed them correctly: the
trailer's own tick takes in any reader standing in the woods, and it had taken both of the case's readers in before the case
began, the second onto the cousin's spot. The case's own first step then moved them back to the trail head. That step's check
failed, but a failed step does not stop the sequence, and the report showed the later step's failure. The case now keeps its
readers out of the woods until they come through, as a reader arriving by the door does.

## The cases

- `GoatmanTests.aSecondReaderBeginsInTheCousinsPlaceAtTheCamp`: two readers come through together. The first is the latecomer
  at the head of the trail. The second is at the camp on the cousin's spot, that cousin is gone, and they are told their place and
  that somebody is still coming up the trail. The trail does not pull them back. Each account says where they began.
- `GoatmanTests.aSeventeenthChildIsGivenATrailerOfTheirOwn` (extended): a friend who comes into the second trailer takes a cousin's
  place at its camp, and the first trailer's evening is untouched.
- `LakeHouseCompletionTests.aSecondReaderGoesDownInTessPlaceAndAThirdHasALakeOfTheirOwn` (extended): the partner begins on the
  dock facing the boathouse with her flashlight; on the helmet visit, on the raft by the compressor; and a reconnect does not move
  them.

## Decided here (any of it can move)

- The stand-in counts as arrived and greeted, so the cousins greet the latecomer only.
- Tess's NPC waits just off the dock over the water; the reader in her place stands on the dock beside it.
- Her flashlight is given at the start, since the reader no longer passes the door where it hangs.

## Numbers

No layout change. No Witness change. New builds only.
