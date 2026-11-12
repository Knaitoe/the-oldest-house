# The games behind the store (0.4.515)

The second half of part 5 of docs/BORRASCA_LIFE_PROPOSAL.md, and the last of the owner's life notes. The owner, 10 November: "I'd
also add more active play - hide and seek, tag". And of the whole list: "Most of these should be optional." The proposal: "Tag and
hide and seek with all four, in the trees behind the store, started by any of them and dropped whenever the reader walks off."

Kelly's own game on the street is as it was: she is tagged at the bend, hides behind one of the street's two oaks, and is found.

## What is there

On the first visit, when the reader has found Kelly behind her oak, Patrick calls from where he stands in the street: "That
doesn't count. There's one tree on this whole street." Kendra is already going: "Behind the store! Kelly got found, so Kelly's
it." Kelly: "One game. Then the Tree." The three run down the alley between the store and the fence, into the trees behind the
store. A reader who goes after them plays three games, one after another.

1. **Kelly counts.** She stands with her face to the big oak and counts to twenty, fast. Patrick and Kendra run and hide; the
   reader has the same nine seconds. Then she walks the trees, a set way round that passes every hiding place, and finds whoever
   she can see: Patrick ("Patrick. The woodpile. You always pick the woodpile."), Kendra ("Kendra's behind the skinny oak!"), and
   the reader, if the reader is within nine blocks in front of her with nothing between, or near enough to touch. A reader stood
   in the open when she turns round is told: "You have to hide. That's the whole game." One she never sees is given up: "I give
   up! Come out! Where were you?" and Patrick: "Nobody beats Kelly. Nobody."
2. **The reader counts.** Kendra: "New kid's it! Count at the big oak. No looking." When the reader stands at the big oak the
   three run to hide, each to the other of their two places, and Kelly calls "Ready!" from hers. Each is found by a reader within
   a couple of blocks who can see them. After twenty seconds each gives one hint to a reader who comes near without finding them;
   Kelly's are by the compass ("South. That's all you get."). After two minutes whoever is still hidden calls "Olly olly oxen
   free!" and they come out.
3. **Tag.** Kendra: "Tag! New kid's still it!" The three scatter, each to a different part of two rings of clear ground, one
   round the woodpile and one round the low tree. Each stands until the reader comes within five blocks, then bolts along the
   ring to the next turning that is furthest from the reader and not past them, and keeps going while the reader is close
   behind. Each run they are a little slower. A reader within reach of one has tagged them, and they go and stand out. When all
   three are out, or after a hundred seconds, Kelly calls it: "Okay. That's everybody. The Tree. I promised." or "Time! Nobody's
   it. I have to show you the Tree before supper."

Then the three go back out by the alley, to where the street had them, and Kelly's walk to the Tree begins as it always has,
with Patrick and Kendra coming behind as far as the first marker.

## Nothing waits on it

- A reader who goes on up the street when they are called, or does not come within forty seconds, is not called back: the three
  come out again and the walk begins.
- A reader who leaves the trees and the alley for four seconds in the middle of a game ends it. Kelly, if she is near enough to
  be heard: "Okay. The Tree, then. Wait for me!"
- A reader who leaves Drisking in the middle of a game finds it over when they come back, with the three where the street has
  them.
- A reader who did not find Kelly on the street, because they let her game run out or walked on ahead, is not called at all. Nor
  is one whose Patrick and Kendra are at the drugstore's cabinets.
- A word to any of the three during a game gets no answer of the town's: they are playing.

## The ground

The trees behind the store were a strip of grass nine blocks wide with the big oak in it, a low leafy tree near the back fence,
and bare trunks along the valley's wall. New builds have two more oaks there, one north of the big oak and one east of it, and a
cord of split oak stacked two high along the back of the store.

Six hiding places, each out of sight of the oak where the count is made: behind the north oak, behind the east oak, behind the
low tree, behind the woodpile, round the corner of the store in the alley, and behind a trunk north of the alley (Kelly: "That's
out of bounds and you know it."). Kelly has the low tree and the north oak, Patrick the woodpile and the corner, Kendra the east
oak and the far trunk; which of their two each takes first is drawn when the games are called.

Nobody finds a way round anything. Every step any of the three takes is along one of twenty-seven straight lines between
twenty-five places (`BorrascaGames.LINES`, `POINTS`), each line clear from end to end over level ground; they go to the end of
the line they are on, or back along it when that is shorter, and then by the shortest way along the others. A step that takes
far longer than it should is finished for them when nobody is looking, and after five more seconds whoever is.

## The account

A reader who played at least the first game through has it: "After that all four of us played in the trees behind the store."
Then, if Kelly gave them up: "Kelly counted, and she never found me." And if all three were tagged: "I tagged all three of them."

## Multiplayer

Every reader's Drisking is their own, and so are their three. Nothing here is shared.

## What was built

- `BorrascaGames`: the ground (`plant`), the places and lines, the walking, and the three games; `on`, `arrived`, `told`.
- `BorrascaRooms.build` plants the two oaks and stacks the wood. `BorrascaStory.pact` gives Kelly to the games after her own game
  and before her walk; the walk up behind her, the town's greetings, a word to any of the three, and the drugstore's cabinets
  leave the three alone while a game is on; `arrive` ends one left in the middle. `BorrascaTexts.account` has the sentences.

## Checks

- `theFourPlayInTheTreesBehindTheStoreKellyCountsThenTheReaderCountsThenTag`: the oaks and the woodpile are there; every line is
  clear for a body their size with ground under it; every hiding place is out of sight of the counting oak at the reader's eye
  height and at Kelly's; the reader hides down the alley and is given up while Patrick and Kendra are found; the reader counts
  and finds all three in their other places; Patrick bolts from a reader four blocks off; all three are tagged; they go back out
  by the alley; the account has all three sentences; Kelly's walk begins and the two follow.
- `theGamesBehindTheStoreAreDroppedForAReaderWhoNeverComesOrWalksOffAndNothingWaitsOnThem`: two readers, each in their own
  Drisking. One goes on up the street when called; one walks off while Kelly is counting. For each the three come back out, no
  game is in the account, and the walk begins. Games left in the middle are over on the way back in.
- `patrickAndKendraWalkUpAsFarAsTheFirstMarker` also checks that a reader who never found Kelly is never called.
- The Borrasca namespace, 51 cases, and the quick checks pass.

## Left with the owner

- **Who starts it.** The proposal says "started by any of them". As built, Patrick calls the first game, Kendra the reader's
  turn and tag, and Kelly ends it; the reader starts nothing, and the offer is made once, after Kelly is found. A reader cannot
  ask for a game.
- **When.** Only on the first visit, and only before the walk to the Tree. On the second visit Patrick is gone and his poster
  says his bicycle was found behind the store; nothing was added there.
- **Only a reader who found Kelly is asked.** One who let her game run out is taken as not wanting more of it.
- **Hiding down the alley wins.** Kelly looks nine blocks; a reader fourteen blocks down the alley is never seen. So does
  keeping behind her.
- **The reader's count is not heard or seen.** The House does not speak for the reader; standing at the oak is the count, and
  nothing stops a reader from watching where the three go.
- **A walking reader** catches each runner only after they have tired, and may run out of the hundred seconds.
- **The lines hold five seconds each** (Dialogue), so the calling takes about fifteen seconds and Kelly's count is said faster
  than that, each number taking the last one's place.
- **Vanilla wood.** The two oaks and the woodpile are the game's own oak, like the other trees of the town.
- **The valley's wall.** The trees stand in a strip nine blocks wide between the back of the store and the valley's sheer grey
  wall. Nothing was done to the wall.
- Not seen in a client: the three running, hiding and being found were watched only through the cases. The ground was looked at
  in eye-level frames: from the big oak none of the three can be seen in any of their places.

## Numbers

No protocol, Witness or layout change. New builds only.
