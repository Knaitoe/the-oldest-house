# The Pell house 9: the first morning (0.4.531)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its
street and lot, its rooms, its line, its people, its first visit's day, its first evening, its bedtime and its first night
(`docs/PELL_HOUSE_NIGHT_0_4_530.md`). This is its ninth slice: the night's passing and the first morning, from the proposal's
"1. The placement":

> The morning: June has made breakfast, and Gail calls to see how it went.

This slice is the breakfast. Gail's call is the next. The night slice also left one thing for the morning: in solo play, nobody plays
Tilly, so nobody hears the singing through the wall, and Tilly mentions it at breakfast instead. The House still does not deal the
house.

## What happens

- **Sleep.** From the time June sends them out to the garage until she calls them to breakfast, a hand on the camp bed lies the reader
  down on it, and the dark comes in. Before then, and after, a hand on it says what it is, as before.
  - While every reader at the house has been lying down a moment (a second), the night hurries on. The hour goes on fast, and the
    house's people are where they were going at once.
  - A reader can get up whenever they like, as from any bed.
  - The house wakes them twice. At eleven, as June lets herself out by the back door, the lock wakes anyone asleep in the garage:
    "Across the yard, a key turns in a lock." At seven, June calling them to breakfast wakes them.
- **The small hours.** June's rounds are as they were (0.4.530). When she is back in her room the hour no longer waits at midnight: it
  goes on toward six, three times as fast as by day.
- **First light** (five). The stretch June painted in the night has dried: a hand on it finds it gone over again, not long ago. A smear
  made after she went in has dried as it was: "A shoe went through it while it was wet and dragged it. It has dried like that."
  - Callum comes in off the line the first moment nobody sees him or where he is going, and lies down on his mattress in the circle.
    From first light on he does not go out again.
- **Six.** June gets up. She opens her door, steps into the hall and shuts it behind her (waiting, as she does, for nobody to be in its
  way). Then she goes round the outside doors from inside and unlocks each, a lock heard each time: the back door, the sliding door,
  the front door. While she does, the hour holds at half past six.
- **Breakfast laid.** At the table's near end she lays breakfast, as she laid dinner:
  - a place for Tilly by the hall door, her toast cut into fingers, the crusts off;
  - one for Callum by the stove, with nothing on it;
  - one for each reader at the house, up to two: toast and a fried egg;
  - any left over, laid for nobody.

  Then she goes to the stove, and the hour goes on to seven.
- **Seven.** "Breakfast!" Anyone asleep in the garage wakes to it. Whoever is too far off to hear her is told it: "June, calling from
  the kitchen: 'Breakfast!'"
  - Tilly gets up, her cane back in her hand, and comes along the hall to her place.
  - Callum comes from his room to his. If he is still out on the line (somebody has been watching him all morning), June says "Callum.
    Breakfast." once, and he is in his chair the first moment nobody sees him or it.
  - With no place left at the table for one of them (readers in every chair), they come to the kitchen door and stand there.
- **What Tilly heard.** Once Tilly has come and a reader has come into the kitchen (or nobody has, after half a minute):
  - Tilly: "Somebody was singing last night."
  - June: "Was there, love?"
  - Tilly: "In the wall by my bed. The same bit, over and over."
  - June: "That'll be the pipes, love. Eat your egg."
- **Eating.** A hand on the table at a reader's own place eats their breakfast: "Toast and a fried egg, the yolk still soft. You eat it
  all." Tilly eats hers in her own time. At Callum's place a hand finds his plate with nothing on it.
- **After.** When Tilly has eaten, what she heard has been said, and every reader's breakfast is eaten (or a minute has gone by), June
  says "Leave those. I'll do them." Tilly goes back to Nessa's room and Callum to the end of the hall. June clears the table, takes the
  plates to the sink, and is there a while. The hour goes on to eight, as by day, and the morning waits there for Gail's call.
- **Talking to June** in the morning, from when she gets up until breakfast is cleared, a hand on her gets, in turn:
  - "Morning, love. Sleep all right?"
  - "There's more toast if you want it."
  - "Callum's had his."

Nobody is hurt and nothing is forced.

## What each reader keeps

Each reader's record now also keeps:

- whether they slept in the garage ("SleptInGarage");
- whether the back door's lock woke them at eleven ("WokenByTheLock");
- whether they heard Tilly say somebody was singing ("HeardAboutSinging"), within earshot of her;
- whether they ate their breakfast ("AteBreakfast");
- whether they were at the house when breakfast was done ("FirstMorning").

## How it is made

- **`PellMorning`** has the night's passing and the morning. `PellNight.tick` hands it the house from `SMALL_HOURS` on.
- **The steps** after `SMALL_HOURS`: `RISING` (June up, unlocking her way to the kitchen, breakfast laid at the end of it), `LAID`
  (at the stove until seven), `CALLED` (breakfast), and `AFTER` (cleared; the morning waits).
- **The hour** (`PellVisit.clock`):
  - from `SMALL_HOURS` it goes on at `NIGHT_PACE` to `UP` (24000, six), passing `DAWN` (23000, five);
  - while she goes round the doors (`RISING`), to `HALF_SIX` (24500);
  - once breakfast is laid (`LAID`), to `SEVEN` (25000);
  - from the call (`CALLED`), at one to `EIGHT` (26000).

  The client's sky takes an hour past 24000 as the next day's morning.
- **The hurried night.** `PellMorning.sleepers` runs every tick before the clock. The night hurries when every reader at the house who
  is alive and not a spectator has been asleep on the camp bed for `SETTLE` (20) ticks.
  - The hour goes on at `HURRY` (400 day-ticks a tick).
  - Walkers are put at the end of their way at once and do what they were going to do there (sit, perch, lie down or stand).
  - June paints each cell without the strokes' wait.

  A reader who gets up ends it at once.
- **Sleeping.** The camp bed is a bed to the game (`PellThing.isBed`, `getBedDirection`; `setBedOccupied` does nothing, since it has no
  such state). A hand on it calls the game's own `startSleeping` on its head cell, so the reader lies along it as on any bed, and sends
  the House's fade. It does not go through the game's own bed checks (night, monsters, spawn point), and it does not join the game's
  count of sleepers, so the game never skips the House's night and wakes everyone. `PellMorning.keepSleeping` (a
  `CanContinueSleepingEvent`) keeps a reader on the camp bed whatever the House dimension's own hour.
- **Waking** (`PellMorning.wake`) stands the reader up beside the bed as the game does, takes the dark away and tells them what woke
  them.
- **Seeing while asleep.** A reader asleep sees nothing: `PellNight.seen` passes them by, Callum does not copy them, nobody turns their
  head to them, and they never count as watching (`LiteraryVignettes.watched` already passed over sleepers).
- **Breakfast** is a plate state of its own (`PellVisit.BREAKFAST`), laid as an item display like dinner's, its item
  `pell_breakfast`. Eating it gives five food (dinner gave six).
- **Unlocking.** June's locks are the household's (`Household.locked`); she takes each off as she comes to its door, with the lock's
  sound. The doors stay shut.
- **Callum coming in** (`PellMorning.comeIn`) waits, like his going out, for no camera to see him or where he is going: his mattress
  before breakfast is called, his chair at breakfast, the end of the hall after it.
- **When the visit begins again** (`PellMorning.restart`): anyone still on the camp bed is woken, and the night and the morning are
  undone with the rest of the visit.
- **For tests**, `PellMorning.smallHours` sets the house as June's rounds leave it without walking them: locked up, the stretch painted
  and wet, June in her room, Tilly asleep, Callum on the line, midnight.
- **New art.** Breakfast, drawn by `tools/draw_pell_people.py` on June's plate: a fried egg, its white frilled brown where it caught the
  pan and the yolk standing up, and two slices of toast, one half over the other, butter melted into the top one.

## Found while building it

- **A sleeper would float over the camp bed.** The game lays a sleeper at a vanilla bed's height (nine pixels), and the camp bed's
  canvas is at seven, so a reader asleep on it would lie two pixels above it. A reader asleep on the camp bed is drawn two pixels lower
  (`PellClient.lower`), the way the park's swing already tilts a reader's body (`SwingClient`). This is worked out from the two
  heights; no render of a sleeping reader was made for it.
- **Waking would flash the world for a frame.** A House fade begun while another was showing started from clear, so the short fade
  that ends the held dark on waking would have shown the world for a frame before going black and fading out. A fade begun while
  another is showing now starts from however dark that one has made the screen (`HouseFadeState.begin`). This is for every House fade,
  and only ever removes such a flash.

## Decided here

The proposal gives "June has made breakfast" and nothing of the night's passing. These are mine, and any of them can be moved:

- **The camp bed is slept in**, and sleep hurries the night only while every reader at the house is asleep. With a reader up, the
  night goes at its own pace and a sleeper sees nothing of it.
- **The house wakes a sleeper twice**: the back door's lock at eleven, so a sleeping reader can still see June go out with the tin; and
  breakfast at seven.
- **First light dries the paint.** A smear left after June went in dries as it is, and stays until the visit begins again.
- **Callum comes in when nobody sees**, as he went out. Watched all morning, he stays out through breakfast's call, and June calls him
  once, as if he were always out there in the mornings.
- **June unlocks every outside door** on her way to the kitchen, from inside, and leaves them shut.
- **Tilly mentions the singing at breakfast**, as the night slice proposed for solo play. Nobody yet hears it through the wall; that is
  the co-op Tilly slice.
- **Breakfast is toast and a fried egg**, Tilly's toast cut into fingers with the crusts off.
- **The morning waits at eight** for Gail's call (the next slice).

## Changed

- The night no longer waits at midnight: the small hours go on toward the morning. The night case's last check is now that the hour
  goes on through the small hours.

## The cases

`PellHouseTests.theNightPassesWhileTheySleepAndJuneCallsThemToBreakfast`, with two readers in the garage, checks:

- locked up, a hand on the camp bed lies a reader down on it, and they keep that they slept in the garage;
- with the other reader up, the night does not hurry;
- the other lies down too (from the bed's foot), and with both asleep it does;
- at eleven June goes out, and the back door's lock wakes them both; each is told what woke them and keeps it;
- awake, the night goes at its own pace; they lie down again and sleep through to breakfast;
- June's call wakes them, heard from the garage, at seven;
- the stretch June painted has dried, Callum is in off the line, and June has unlocked the three outside doors;
- breakfast at Tilly's place, Callum's plate with nothing on it, a breakfast for each reader, four plates on the table;
- Tilly and Callum get up and come to the table, Tilly with her cane;
- the readers come in and sit at the places laid for them;
- Tilly says somebody was singing, and both readers heard her; June asks, Tilly says in the wall by her bed, June says the pipes;
- each reader eats their breakfast, a breakfast's worth, keeps that they ate, and is told what it was;
- June says to leave the plates; Tilly has eaten hers; Tilly and Callum get down; each reader keeps the first morning;
- the table is cleared, Tilly goes back to Nessa's room, and the hour has gone no further than eight;
- left, the house begins again, the afternoon and the table bare, and what each reader did stays theirs.

`PellHouseTests.watchedCallumStaysOutUntilNobodySeesAndJuneCallsHimIn`, with one reader asleep in the garage and one awake by the drive,
checks:

- with a reader awake, the night does not hurry;
- the reader awake steps in the wet paint and smears it;
- at first light the stretch dries, and the smear dries as it was;
- Callum, watched, stays out; the reader asleep sleeps on;
- June gets up, unlocks her way to the kitchen, lays breakfast, and has shut her door behind her; Callum, still watched, is still out;
- June's call wakes the reader asleep, who hears it from the garage; Callum stays out;
- once breakfast is called, the camp bed is only looked at;
- June calls Callum in; while watched he stays out, and the moment nobody sees, he is in his chair at his place;
- a hand on June in the morning gets her morning lines;
- a hand on the smear finds it dried as it was;
- left, the house begins again: the line as the first visit has it, Callum at the end of the hall, and what each reader did kept.

## Not yet

- Gail's call (the next slice).
- Tilly's singing through the wall, for a reader playing Tilly (the co-op Tilly slice).
- Everything after.

## Numbers

No layout change. No Witness change. New: one item (breakfast), one plate state, four steps, one test fixture.
