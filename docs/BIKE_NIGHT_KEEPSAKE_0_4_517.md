# Bike night 34: the keepsake (0.4.517)

The last thing in the proposal's endings that was not built (`docs/BIKE_NIGHT_PROPOSAL.md`): "**The keepsake:** Claude suggests
the bike itself, owner-bound, rideable in the Overworld and the House with its perk. A smaller alternative is the Rattler's spoke
card. The owner's call." The owner, 12 November: "start by finishing each unfinished vignette". Built as the bike itself; the
owner may still prefer the card, and what was decided here is listed at the end.

Gus's page says of the bikes in the rack: "there are always some in the rack that nobody comes back for."

## What is there

Whoever **rides** out by the back gate and then leaves keeps the bike that was under them at the gate. It is handed over with
their account, once. Somebody who came out on the pegs of another's bike, or who walked out, has no bike to keep: theirs was
taken, or was never theirs. Somebody the three caught, or the Hollow had, has nothing, as before.

It is one thing in the hand, named for what it is ("The Hopper"), drawn as the standing bike itself, small, and seen from its
side in a slot or a frame. Under its name: "Yours now. Away from the House it is under you whenever you want it, and it goes as
it went that night."

**Used**, it is under its reader and they are on it, pointing the way they were looking. It rides exactly as it did in Calder
Falls, with the one thing its make does: the Wasp turns inside anything, the Hare climbs, the Comet is fastest, the Hopper jumps
and carries a friend on its pegs, the Lamp throws its light, the Rattler has its card. The keys are shown once, as they are in
the park, to a reader who has not seen them.

**Getting off it, or coming off it,** it is in the hand again. It is never left standing or lying anywhere, so it cannot be
lost, taken, or left behind in a world.

**Where.** In the reader's own world and in the manor, as the other keepsakes that do something are used (`Keepsakes`). Not in
the House's halls and not in any of its scenes: there, "Not in here." A reader who rides it somewhere it may not go is off it.
Where there is no room for it: "There is no room for it here."

**Whose.** Its reader's alone. In anyone else's hand: "It isn't yours."

## The account

A reader who kept one has a last sentence: "I kept the bike. Nobody was coming back for it."

## Multiplayer

Each rider who rides out keeps their own. A kept Hopper carries another reader on its pegs anywhere it can be ridden. Nothing of
it is shared, and a kept bike is seen by everybody as any ridden bike is.

## What was built

- `KeptBikeItem` (`kept_bike`): `of(make)`, `make(stack)`, `refuse(reader)`, `ride(reader, stack)`, and the item's use.
- `NightBike`: `keep()` / `kept()`. A kept bike is not stood anywhere when its rider is off it (`parkAs`), tells the night
  nothing, and puts its rider off where it may not go. `NightBike.Make.of(id)`.
- `NightBikes.keys`: the keys' line, shared by the park's bikes and a kept one.
- `BikeNight.leave`: the bike, with the account. `BikeNightTexts.account`: the sentence. `Keepsakes.line`: its line.
- `tools/draw_bike_night_art.py`: the item's models, one for each make by the stack's model data, each the standing bike's own
  model with a hand's, a slot's and a frame's view of it; the tool checks that its makes are in the game's order.

## The case

`whoeverRidesOutKeepsTheirBikeAndRidesItInTheirOwnWorldAndNowhereInTheHouse`, 103 in the namespace. Three who came through the
gate: on the Hopper, on its pegs, on foot. The rider has one bike, the Hopper, theirs alone, with its line, and their account
ends with the sentence; the other two have finished and have none, and leaving again gives no second. In another's hand it is
not got on, and they are told. Used in its reader's own world it is under them, a friend steps onto its pegs, and it is ridden
up a road. Off it, nothing stands or lies anywhere on the road and it is in the hand. In the House it is refused.

## Left with the owner

- **The bike or the card.** This is the proposal's larger choice. If the card is wanted instead, or as well, it is a small
  thing to add.
- **Not in the halls.** The proposal says "rideable in the Overworld and the House". As built it goes where the other
  keepsakes work, the reader's own world and the manor. Riding it through the halls would need each kind of hall looked at
  (the trials, the puzzle rooms, the staircase), and a bike makes some of them easy.
- **It folds into the hand.** A bike that is in the hand whenever it is not being ridden is a convenience, and it is not how
  the park's bikes behave. The other way is a bike stood in the world as a block, which can then be lost, buried or taken.
- **Carried riders keep nothing.** Somebody whose bike the three took and who came out on a Hopper's pegs gets the account
  and no bike. (Their own turns up later, chained to a school fence, as before.)
- **The sentence** in the account, and the line under the item's name.
- **In the hand it has not been seen.** Its view in a slot is in the proof (page `kept`: each of the six, large, and at a
  slot's size), where it had to be made smaller to stand inside the slot. Held in first and third person, lying on the
  ground and hung in a frame, it is placed by numbers nobody has looked at in a client.

## Numbers

No protocol, Witness or layout change. New builds only. One new item.
