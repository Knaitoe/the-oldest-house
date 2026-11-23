# The waxworks 4: the account (0.4.753)

Part 4 of the waxworks (`docs/WAXWORKS_0_4_749.md` lists the parts): the desk telephone, the police at five, six o'clock, and the night guard's log in each reader's own hand. The House now deals the waxworks, and it is a Witness source of the survival kind: pool 96, quota 33 (capped), two kinds, three endings.

## The telephone

- Before a card is punched, a hand on the desk telephone finds only the list of numbers taped beside it.
- On the shift, the first call of the night reaches a dispatcher, who takes the address and says somebody will come by toward five (`WaxShift.phone`). The caller's record keeps `Called`, `CalledTonight` and the time (`CalledAt`).
- After the first call the line only rings. After the police have been, the phone says they have been. A spectator calls nobody.

## Five o'clock

- If somebody called, at five two officers come in at the front doors, put the lights up in every gallery, walk the museum through and count twenty-three. One of them signs the log and they go (`WaxShift.police`).
- Whatever was out of place and unnoticed is put down to them: every irregularity counts as noticed and goes home unseen, as noticed ones always do. Nothing more is moved that night.
- Everyone on the shift at five has `PoliceTonight` on their record and is told. The rounds are still the readers' job: the five o'clock round still has to be walked.

## Six

- At six, with every hour's round in the log, the shift is kept. Everyone still on it is ready (`LiteraryVignettes.ready`), with their own outcome: `called_the_police` for the one who called, `kept_the_shift` for the rest. Short a round, nobody is ready; the night ends and can be tried again.
- The account is the shift log on the desk (`WaxSite.LOG`, `LiteraryRooms.ending`). Read after the notice by the time clock, it resolves `WitnessAccount.Story.WAXWORKS` for that reader and marks them completed.

## The account

The night guard's log, in the reader's hand (`WaxTexts.account`). It is made only from that reader's record and says only what they did and saw:

- the shift, and how many earlier nights ended with the screens dark;
- how many counts they wrote up; if their light found the one with no plaque, eight in the Hall where the plaques say seven, and where it stands;
- each figure that blinked at them, by name;
- the statesman who stood the way they stood, if he did;
- how many things they noted out of place, and how many of them on the screens;
- the call, and the police counting twenty-three; or that somebody else on the shift called;
- the day man's key at six, and his signature: Seen.

It never says what the twenty-fourth was. A reader who found it ends "I did not ask him about the one with no plaque. He did not say."

## Witness and the rest

- `WitnessAccount.Story.WAXWORKS`: "The night shift", survival. Line: "I kept a night shift in a wax museum with a flashlight and counted the figures every hour. The plaques said twenty-three."
- Advancement `account/waxworks` (a candle), lang keys.
- The stair's company: "the one with no plaque" (the twenty-fourth figure's own body).
- The House's forged line in the Pell journal: "I put one of them back on its mark myself."
- The together line was already there (`CoopAccounts`): "%s {was} on the night shift with me."
- `WitnessPool` pins 96 / 33 / 72.

## A fix

A figure already facing its gallery's door could be chosen to "turn to face the door" and not visibly turn. Such a figure is now gone from its plinth instead (`WaxShift.facesDoor`).

## Tests

- New: `WaxworksTests#thePhoneThePoliceAndTheLog`: two readers and a spectator; the phone before and after the punch; one call a night; the police at five settling what was out of place, for everyone on the shift and not the spectator; six with every round, each reader's outcome; each account says what its reader saw and not what they did not; a reader who saw blinks, the mirror and the twenty-fourth has them in their log; read after the notice it is theirs, and only theirs.
- `theMuseumItsGalleriesAndItsFigures` now expects the call (its reader is punched in) and the place dealt.
- `LiteraryTests#eligibleSourcesAndOneOptionalClosureLeaveTheWitnessReachable` counts the waxworks among the sources.

Local: waxworks 6/6; the group sweep, cast and together waxworks cases, the Witness pool, quota and milestone cases and the Pell forged-line case, 9/9.

## New lines for the owner's review

Every line in `WaxTexts` added here: `CALLED`, `CALLED_LATE`, `CALLED_AGAIN`, `POLICE`, `POLICE_GONE`, the figure names, and the account. The Witness line, the forged line and the stair figure's name.

## Not yet seen

The feeds (0.4.752) and the police's lights up have not been seen in a client frame; the lights going up is told, not drawn.
