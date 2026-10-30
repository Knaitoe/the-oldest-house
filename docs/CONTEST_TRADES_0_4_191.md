# The contests' trades — 0.4.191

This is the success-and-failure audit's sixth build (docs/SUCCESS_AND_FAILURE_AUDIT.md). It covers the contests' trades that are not artifacts: what winning takes and what failing takes. They follow the cabin's model. Each:
- comes from the room's own story;
- never wounds;
- never touches a House original, an artifact, an owner-bound item or anything the Mother keeps.

The artifacts the contests give are build 7.

## Built

| Room | Side | What happens |
|---|---|---|
| **The small passage (Ted the Caver)** | win gives | **The cave's breath.** Under water, a reader who retraced the squeeze loses air three quarters as fast, everywhere and for good. Every fourth breath they lose comes back. |
| **The small passage (Ted the Caver)** | win takes | **The cave keeps a hand on their line.** Now and then, while they lay a trail with the trail spool, the winds pay out: "The line pays out through your fingers, back the way you came."<br>• It costs 35 to 65 percent of what is left on the spool, or, one time in six, all of it.<br>• It happens at most once a House day, about once in two hundred stretches of trail laid.<br>• After three unwindings the cave lets go: "Then it goes slack, as if something at the far end let go."<br>• Torches, chalk and every other navigation aid are never touched (the owner's note). |
| **The passing boots (elk carcasses)** | win takes | **The hides' smell.** Reading the account, the reader is told: "The smell of the hides has come out with you." Untamed, unleashed animals within ten blocks then turn and walk away from them. Water washes it off: "The water takes the smell of the hides." |
| **The second apron (diner)** | win takes | **Nausea.** A reader who tasted her meal and left gets six seconds of nausea on reading the account: "Something on the plate turns over in you." |
| **The second apron (diner)** | fail takes | **Her black frost.** Caught, the reader is frozen one tick short of a wound, slowed for five seconds, and dark ink drifts round them. Freezing never hurts them. |
| **The floorboards (Tell-Tale)** | fail takes | **Calm.** Each time the heart peaks and sends a reader out, they are unsettled once more, up to three times. When they walk back into the room, the heart starts at 20 for each time, up to 60 of 100. It is never raised to the peak by itself, and it never lowers a heart already racing. |
| **The shallows** | fail takes | **The boys' company.** A blow before the throw now also sends the boys home for that visit: "The boys stop laughing. They pick up their things and go home without you." The three bank boys leave the view of whoever struck and of the reader whose visit it was, until those readers come back. The reader's account keeps it: "Once, before that, I struck her, and the boys went home without me." |

## Not built, and why

- **Under the green lights (tunnel), "fail takes distance":** there is no next attempt. A failed uncanny room is never dealt to that reader again (`LiteraryVignettes.canDeal` refuses it), so fewer steps on a later visit cannot happen. Making uncanny failures retryable would change all six uncanny rooms, so it is left for the owner.
- **The empty chair (séance), "driving them out takes the family":** this already happens. A sitting that ends with the family driven out is shared and never resets. They go into the back room and shut the door, and they never come back for anyone.

## Tests

- **New: `ContestTradeTests.tedsCaveGivesBreathUnderWaterAndTakesTheSpoolThreeTimesAtMost`.** Two real players stand in water. The caver loses about a quarter less air than the other. The spool:
  - loses a share of what is left, not all of it;
  - does not unwind twice in a House day;
  - rarely loses all of it;
  - stops after three unwindings.

  The caver's torches are untouched.
- **New: `ContestTradeTests.theHidesSmellTurnsAnimalsAwayUntilWaterWashesItAndTheDinerLeavesNauseaOrBlackFrost`.** It checks that:
  - a cow turns away from the smell, walking away from the reader;
  - a tamed wolf is not counted;
  - water washes the smell off;
  - the meal leaves nausea;
  - the black frost freezes and slows without a wound.
- **Extended: `WitnessTests.observersCannotSpendTheHeartbeatOrBeForcedThroughItsExit`.** After the peak, the reader is unsettled once, and walking back in starts the heart at 20.
- **Extended: `IndianLakeLinkedTests.lettingGoOrStoppingStartsTheCountAgainAndABlowStillRefuses`.** After the blow, the boys have gone home and the account says so.

All four pass locally.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
