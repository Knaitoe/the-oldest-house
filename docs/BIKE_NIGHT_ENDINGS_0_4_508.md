# Bike night 25: the endings (out the back gate, 0.4.508)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`, "Endings"): "**Success: out the back gate.** The condition: the reader
rides out of the far gate." "**The account** (survival kind) remembers: the bike; the best thing they did on the
playground; how many sand balls hit; which streets they took; whether they carried anyone; which way they went through
the Hollow, high or low."

Until now the night could not be finished: the lane behind the back gate was a strip of road with nothing at either
end, and the House never dealt it. This is the part that lets it be finished and dealt. What the endings also promise
and this does not build is at the end.

## The way on

A brick wall runs along the lane's far verge and round its two ends, capped as the cemetery's piers are. It stands
three rows back from the lane, eight blocks from the back gate, so that somebody who comes through the gate at speed
has room to turn or stop. In it, east of the gate's line, is a door: the night's way on (`OnwardPlacements`), which
the House deals from as it does from any way on.

## Leaving

`BikeNight.leave`. Whoever is through the back gate and then leaves, by the lane's door or by the door they came in
by, has finished the night: once. It is theirs with the Witness, and their account goes out with them. How they came
through the gate is the outcome: riding, on the pegs of somebody else's bike, or on foot. They are one source.

Whoever leaves before the back gate has finished nothing and may come back. Whoever was caught, in the town or in the
Hollow, has nothing to finish.

## The account

The account was already written slice by slice from the reader's own record, as far as "Tully said to get us." It
goes on now:

- **which streets they took**, by their signs, in the order they came to them (looked at once a second for whoever
  is on a bike; the two halves of Mill Street are one street, and the court and its circle are Elm Court);
- **whether they carried anyone**, or were carried;
- that the three fell back at the cemetery gate, and that past the last of the stones the hill gave out;
- **which way they went through the Hollow**: each way other than the plain one that they had under them, in the
  words of somebody who rode it, or that they kept to what was straight ahead;
- how many times they went down into the mist, if they did;
- how they came out by the back gate, and that the town's lights were on again;
- and which way they left.

It tells of nothing that did not happen.

## The source

`WitnessAccount.Story.BIKE_NIGHT`, of the survival kind: "I got out of a park on a bike with three coming after me,
through a town and up into its graveyard. Where the hill gave out under the stones I rode what was left of it, and
came out by the back gate." The quota is derived from the pool with it in (`gametest/WitnessPool`).

It has no first paper. Every other literary place hands over a paper to read first and an account that answers it;
the night hands over only the reader's own account. So it is a source that is not counted among the literary places
with first papers.

## Dealing

`BikeNight.dealable`: the night is dealt to somebody who has not had it. Not to somebody who has finished it. And,
as the proposal has it for both failures, "the vignette is never dealt to them again": not to somebody the three
caught in the town, and not to somebody the Hollow has had.

## The case

One (96 in the namespace), three readers.

- The wall is whole and capped, stands back from the lane, and goes round the lane's ends; the door stands in it off
  the gate's line, and the House knows it as the night's way on; the night is a source of the survival kind.
- Nobody has finished it, and it may be dealt to any of them.
- **The streets**: a rider a second on Main Street and a second on Mill Street has them kept, in that order.
- **Somebody who leaves before the back gate** has finished nothing, has no account, and may be dealt it again.
- **Somebody the Hollow has had** has no account, nothing with the Witness, and is never dealt it again. Nor is
  somebody caught in the town.
- **A rider through the back gate** has not finished until they leave. Through the door in the wall they have:
  with the Witness, not dealt it again, one account, which tells of their bike, their streets, the way they went
  through and the way out, and of nothing that did not happen. Leaving again changes nothing.
- The account of somebody who took the dark way and the high way, went down twice, came out on somebody's pegs and
  left by the park says each of those.

And the cases elsewhere that pin the pool: the registry's, and the Witness's quota, order, reload, older accounts and
operator cases. They pass.

## Not built, not seen, and left with the owner

- **The keepsake.** The proposal leaves it to the owner: the bike itself, or the Rattler's spoke card. Nothing is
  kept yet. The reader's bike stays where they got off it.
- **What follows at the gates**: the three's bikes lying by the front one, the cap hanging on the back one, Gus
  there. (Built in 0.4.509: `docs/BIKE_NIGHT_AFTER_0_4_509.md`.)
- **Dawn** for somebody the Hollow has had: it is still night outside the gate. (Built in 0.4.510:
  `docs/BIKE_NIGHT_STAYS_0_4_510.md`.)
- **The headstone** with a caught reader's name at the end of the old row in every copy of the town, and their bike
  rusting against it (built in 0.4.510); and **the bike chained to a school fence** in a later hall for somebody
  caught in the town.
- Somebody who was caught and then walks out by the door they came in by simply leaves. Nothing is said to them.
- Nobody has played the night from the park to the lane. Each part has its own cases; the whole has none.
- The door is the House's own way-on door, in brick. Whether the lane wants a door of its own is the owner's to see.

## What the first full local gate found

- The new case passed alone and failed among the others (`and it is the night's way on: null`). A test fixture builds
  a place without its way on. The builder fits that, or else a pass that waits until no camera is turned towards the
  door, and in a full run somebody usually is. The case now fits the door itself before it asks for it.
- `tools/generate_advancements.py --check` failed (`KeyError: 'bike_night'`): a Witness source needs its advancement.
  It has one now, under the treat sack.
