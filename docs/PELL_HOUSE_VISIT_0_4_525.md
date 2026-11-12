# The Pell house 5: the first visit, by day (0.4.525)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its
street and lot (`docs/PELL_HOUSE_STREET_0_4_518.md`), its rooms (`docs/PELL_HOUSE_THINGS_0_4_522.md`), its line
(`docs/PELL_HOUSE_LINE_0_4_523.md`) and its people (`docs/PELL_HOUSE_PEOPLE_0_4_524.md`). This is its fifth slice: the first visit's
day, from the proposal's "1. The placement", as far as dinner. The House still does not deal the house.

## What happens

As the proposal has it: "Gail drops them off. June shows them round, and Callum watches from the end of the hall", with June's
rules "kindly given".

- **Gail's car** is parked at the turning circle's curb in front of June's house.
- **The handover.** When a reader comes up to Gail on the path, she turns to them: "There you are. Come and meet June." June says
  hello. Gail says June has everything ready and that she will ring in the morning, then that she has to get going.
- **Gail goes.** She walks back to her car and waits at its door until nobody can see her or the car. Then they are gone, and what
  is heard is a car door and a car pulling away.
- **The first rule.** At the step June says: "Come in out of the heat. And inside the line after dark, love. It's for the dog."
- **The tour.** June opens the front door and shows them round. At each stop she waits until somebody is with her, and until what
  was last said has had its time, before she speaks:
  - from the hall, Callum at its far end: "That's Callum. He doesn't say much. He'll come round to you."
  - at Nessa's door, which she opens: "This was Nessa's room. It's Tilly's now."
  - in the living room, at the sliding door, looking out at the pool: "The pool gate stays locked. That's the one thing in this
    house I'll ask twice."
  - out through the sliding door and across the yard, at the garage, which she opens: "You're in the garage. It's all done up. For
    your privacy."
- **Settling in.** "Dinner's at six. I'll give you a call." June goes back to her kitchen and stands at the stove.
- **Tilly** follows June in, a step behind along the way June went, until she is shown her room. Then she goes in and stays by the
  foot of the bed.
- **Callum** stays at the end of the hall and watches.

Nobody is hurt and nothing is forced. A reader can wander off, go ahead, or stay outside. June waits at each stop for whoever is
nearest. A hand on any of them still gets their own lines.

## June's rules

Each reader's record keeps which of June's four rules they have heard: the line, the pool gate, Nessa's room and the garage. A
reader hears a rule if they are within earshot when June gives it, on the tour or from a hand on her later. Her hand lines give the
same rules, so a reader who missed the tour can still hear them. Two readers can come away with different rules: the one who went
out to the garage has heard it, and the one who stayed in the living room has not.

## When the house is left

The visit is the house's, one for every reader at it. When the house has been empty for five seconds it begins again from the
start. Everybody goes back where the first visit puts them, Gail and her car are back at the curb, and the doors June opened are
shut. What each reader heard stays theirs.

## How it is made

- `PellVisit` keeps the day, once a game tick for each house with a reader at it. It moves the people and says who walked that tick,
  so `PellPeople` leaves those heads alone. People at rest settle their bodies the way their stop faces, and turn their heads to a
  reader near them as before.
- The steps are Gail's greeting, June's hello, Gail's handover, Gail's goodbye, June's first rule, then each of June's stops
  (`PellVisit.STOPS`: the way there, which way she faces, what she opens, what she says and the rule it gives), then settled.
- Each line waits for the last to have had its time (`Dialogue.ticks`) and for the next speaker not to be in the middle of
  something, so a reader's hand on June is never cut off.
- The greeting comes when a reader is within six blocks of Gail, within three of June, or inside the house. A reader who goes
  round Gail is still greeted.
- They walk on the shared footpaths (`SceneFootpaths`), with native collision. June opens the front door and the sliding door when
  she comes to them. If she stands unable to get nearer for four seconds while nobody sees her, she is put where she was going.
- Gail's car is a town car body (`TownCar`, the parked blue sedan), moved every tick while she is here and never saved. Gail goes
  only when neither she nor the car is in any reader's view (`LiteraryVignettes.watched`), so she is never seen to vanish.
- The sound is original and subtitled: `pell.car_away`, "A car door shuts and a car pulls away", made by
  `tools/generate_pell_audio.py`, with `--check` (CI runs it). The proposal's other sounds go there too as they are built.
- `PellPeople.ensure` does not make Gail again once she has gone, until the visit begins again.

## Decided here

The proposal gives the visit's order but not every word or place. These are mine, and any of them can be moved:

- **Gail's car** is parked at the turning circle in front of the house. Gail leaves while nobody is looking, since the street's
  only way out is the House's own door.
- **The first rule is given at the step,** where the line crosses the path, as they come in.
- **The stops are in this order:** the hall, Nessa's room, the pool through the glass, then the garage across the yard. June goes
  out by the sliding door and comes back the same way.
- **"Dinner's at six"** ends the day here. Dinner itself, the night and the morning are the next slice.
- **June's hand line about Tilly's room** now says "Tilly's in Nessa's room now", so that it gives the same rule as the tour.
- **Earshot is twelve blocks** from June, and a stop counts as attended when somebody is within five of it.

## The case

`PellHouseTests.theFirstVisitShowsThemRoundAndGivesJunesRules`, with two readers, checks:

- Gail's car is at the curb, and nobody is greeted before a reader comes up;
- Gail greets first, and June gives the line at the step. The reader at the step hears it and the one at the turning circle does
  not;
- June opens the front door, shows them Callum from the hall and Nessa's room (opened), and both readers hear that rule;
- Gail and her car go while both are inside;
- June shows them the pool, and both hear that rule. She goes out by the sliding door and opens the garage. The reader who went
  out there has all four rules, and the one who stayed in the living room has only the two they were there for;
- June goes back to her kitchen, Tilly is in Nessa's room, and Callum has not moved;
- a hand on June gives her first line and then the line rule, which the second reader now has;
- left, the house begins its visit again: everyone back where the first visit puts them, Gail and her car at the curb, June's
  doors shut, and each reader keeping what they heard.

The people's case (`thePeopleStandWhereTheFirstVisitPutsThem`) still holds with the visit running.

## Not yet

Dinner (June serves Tilly first), the night (June out with the tin, the singing through the wall, Callum on the line), the
morning (breakfast, Gail's call), and everything after: the second visit, the tapes, the framing, the last night, the endings, a
reader playing Tilly, the neighbours.

## Numbers

No layout change. No Witness change. New: one sound and its subtitle.
