# Aftermath rooms (0.4.244)

The owner asked for finished scenes to come back as quiet aftermath rooms:

> Let some finished scenes return as quiet aftermath rooms. The pool is covered and closed. Vacant Possession is empty—or its window
> remains lit because you returned the chair. No new objective or Witness credit; players recognize the consequences of what they did.

Three scenes come back in this release: the Last Swimmer, Vacant Possession and the Masque. Neither adds a protocol, layout, Witness, source-kind or
quota change. Witness still derives from `WitnessAccount.Story`. An aftermath is not a story and earns nothing.

## How a finished scene comes back

`Aftermath` (labyrinth) is the registry. A scene is listed in `Aftermath.PLACES` and dresses its own aftermath in its own controller.

- **Dealing.** `Aftermath.canDeal` lets the dealer offer a finished scene back to the reader who finished it:
  - at most `RETURNS` (two) times;
  - never while it is among their last eight crossings.

  `LabyrinthDealer.vignettesAvailable` adds these scenes. While a scene can be dealt, `finishedFor` is false, so its door leads into the
  room instead of being a plain passage. Once it has been visited, it is a passage again until the reader's crossings carry it out of
  their last eight.
- **Counting.** `Aftermath.counted` counts each return once a visit. It also marks the visit's beat, so the reader's next arrival counts
  as a new visit, and a third return is never mistaken for the second.
- **A deliberate scent** still seeks unfinished stories only. Both scent filters in the dealer skip a scene the reader would find only as
  its aftermath.
- **Solitary.** An aftermath belongs to the reader who finished the scene. GroupCrossings never builds a group around a crossing into one:
  - `before` returns early when the leader is returning;
  - `crossed` records no lead for a returning reader;
  - `canJoin` refuses a reader who finished the scene.

  Friends who have not finished it keep their own doors.
- **Mixed rooms.** A scene shows its aftermath only when everyone in it has finished it (`Aftermath.alone`). A reader who has not
  finished it gets the scene as it always was, and anyone finished who is with them walks through it unaffected, as before.
- **Credit.** None. The scenes' endings already check `Completed`, and the aftermath touches neither the reader's record nor the Witness
  account. The tests check that both are unchanged.

## The pool

A reader who closed the pool comes back to it as it was left. The cover is on from wall to wall, every lamp is off but the two by the
fire door, and nobody is there: no swimmer and no attendant. The radio is off and the pace clock's sweep hand has stopped where it was.

- `LastSwimmer.tick` gathers the finished readers in each pool. A pool with only finished readers and no closing under way waits a second
  (`AFTER_WAIT`), so that an unfinished reader coming in with them starts the closing instead. Then it is dressed by
  `LastSwimmer.aftermath`, once its people's sections are loaded.
- A closing always starts from new: `start` takes the cover off and lights the lamps.
- Each finished reader there gets the quiet line once a visit. Then the cover, clock, radio and open locker each answer with their own
  lines (`SwimmerTexts.AFTER_*`), and nothing hauls the cover.
- Through its door (`LastSwimmer.heard(base,data,listener)`): nothing moving for a reader who closed it, with no closing under way; water
  for anyone else.

## The flat

The flat comes back as each reader left it.

- **For everyone in it**, the flat is as the last chair left it when carried down:
  - everything on the list gone, with what each piece hid there to see;
  - the bulbs off;
  - nobody there.
- **For each reader who put the chair back**, in their own view only:
  - the chair is where it stood and the bulbs are on;
  - the one who lived here sits in it, facing the street, no more there than when they left. This is a private figure
    (`LiteraryActor.bind(PLACE, reader)`), drawn at the faintness the last piece left it.

  The chair and bulbs are block views sent to that reader alone, again every five seconds so that a section loaded afresh agrees. Their
  figure goes with them when they leave. Their view of the lit window stays until the flat is put back, so looking back from the street
  they still see the light on.

Two readers who chose differently can stand in the same flat, and each finds their own ending there.

- `VacantPossession.step` decides at the flat's first tick (`Flat.after`, from `Aftermath.alone`); `aftermath` lifts everything and puts
  the bulbs out. `keptViews` gives each kept reader their view and figure, and takes away the figures of readers who have gone.
- An unfinished reader who comes into an aftermath flat starts moving day afresh. The flat is put back, the tenant greets them, and
  every private figure is cleared.
- Each finished reader gets the quiet line once a visit (`VacantTexts.AFTER_EMPTY` or `AFTER_KEPT`, by their own outcome). The docket
  board answers that the docket is gone.
- Through its door (`VacantPossession.heard(base,data,listener)`): someone faintly (`KEPT_HEARD`) for a reader who put the chair back;
  nothing for one who carried it down.

## The Masque

The Masque is shared, but everything in it already belongs to each reader: their dancers, the figure that scolds them, the chimes, and
the clock they stopped. So a reader who stopped the clock and finished comes back to the seven rooms as they left them:
- their dancers stand still and turn to them;
- the clock is stopped for them alone, and no chime puts them back a room;
- the party is silent;
- the door has no clock behind it for them.

Its aftermath needs no solitude, since another reader in the rooms still dances their own evening. The quiet line
(`LiteraryVignettes.MASQUE_AFTER`) is counted on the literary tick's own record with `Aftermath.count`, because that tick saves the copy
it read afterwards.

## Joining the registry

For a later scene to join:

1. Add it to `Aftermath.PLACES`.
2. Dress the aftermath in its controller only when `Aftermath.alone` is true, unless everything that differs is already private to each
   reader.
3. Show outcome differences to each reader privately, with actors bound to them and block views sent to them alone. Never change the room
   for everyone.
4. Call `Aftermath.counted` (or `Aftermath.count` on a record the caller saves) with a quiet line.
5. Answer its door sound per listener.

## Tests

- `LastSwimmerTests#aFinishedPoolComesBackCoveredAndClosed`:
  - the cover on, two lamps lit, nobody there and no closing;
  - the record and Witness count unchanged, and the return counted once;
  - nothing hauls the cover;
  - the door sound per listener;
  - offered back at most twice, and never within the last eight crossings.
- `VacantPossessionTests#aFinishedFlatComesBackEmptyOrLitAsItWasLeft`:
  - the chair-carrier alone: the flat empty and dark, the door silent for them, the return counted, nothing earned;
  - a reader who put the chair back joins: the flat unchanged for everyone, their own figure in the chair at the kept faintness and seen
    by them alone, the door sound per listener, neither able to join the other's crossing;
  - their figure leaves with them;
  - an unfinished reader then starts moving day afresh, and the finished reader keeps their ending.
- `LiteraryTests#aFinishedMasqueComesBackStillAsItWasLeft`:
  - offered back to the stopper;
  - their dancers still and their own, nothing earned, and no clock through the door;
  - each return its own visit, and no third offer.
- The existing pool, flat, Masque, door-sound and co-op cases pass locally.
- This is pending CI.
