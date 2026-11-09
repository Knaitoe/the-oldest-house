# Borrasca: the town's routines (0.4.428)

The town's day (0.4.426) put people in Drisking's shops and on its stools. This release gives the town a clock, and two people a
routine on it: Mr. Rowe's smoke break, which is the only way a child gets into the historical society, and the mail carrier's round.
The children on bikes and a truck on the street need new models and come next.

## The town's clock

Each reader has their own clock while they are in Drisking. A turn of it takes two minutes (`BorrascaPeople.DAY`, 2400 ticks); it
keeps running from visit to visit.

## Mr. Rowe's smoke break

- From 1200 to 1800 on the clock (thirty seconds in every two minutes) Mr. Rowe steps out of his antiques shop and stands on the
  sidewalk beside his door, smoking. He goes, and comes back, only while nobody is watching his counter or the stoop, the same rule
  the cast keeps when they are moved.
- The shop's bell rings over the door when he comes back in, loud enough to hear from the back room.
- Talked to on his break: "I'm on my break, hon. Don't touch the clocks." Grown: "Five minutes. Go on back to the society if you like."

## The historical society

- While Mr. Rowe is at his counter, the society's door stays shut to a child. He says what it is: "That's the historical society, hon.
  It's for grown-ups, and it's only open Saturdays." Asked again: "Not back there. Those papers are older than your grandmother."
  Then "No."
- While he is out front, a child can open the door and go in. The map of the old workings and the photographs are in reach; the
  framed history still hangs too high to read.
- When he is back at his counter (two seconds after he comes in) he sends out any child who has been in the society a second: "Hey.
  Out of there. That room's not for kids." The child is set down on the shop floor. He shuts the society's door behind him when he
  comes back, unless somebody is standing in it.
- Grown readers go into the society whenever the shop is open, as before.
- In a co-op rework a friend will be able to keep him talking at the counter instead.

## The mail carrier

Mr. Dorsey (stranger phase 160, `literary_drisking_dorsey.png`, painted by `tools/paint_character_skins.py`) walks his round on the
childhood visits and years later: up the west side of the street past the diner, the antiques shop and the station, across, and down
the east side past the bar, the drugstore and the doctor's. He stands three seconds at each door and walks on. On the last night he
is not out.

- A child: "Nothing for you, kid. Unless you're expecting something." "Eleven Sears catalogues in this bag. Everybody wants the toy
  pages."
- The second visit: "Got a letter here for the Garritys. Nobody's emptied their box all week." "The Maddox boy's mother asks me every
  day if I've seen him. Like I'd know. I just carry the mail."
- Years later: "Twenty-two years on this route. Same boxes, mostly." "The Garritys' box is still on its post. Nobody's taken it down."

## The account

A child who read the map in the society: "While Mr. Rowe smoked out front of his shop, I went into the historical society behind it.
On a map of the old workings someone had drawn a small square over the top shaft and written DEPOT." (If the child got in some other
way while he was at his counter: "Behind Mr. Rowe's back I got into the historical society.") Caught there: "He came back in and sent
me out." Caught without reading the map: "Mr. Rowe found me in the back room of his shop and sent me out."

## Tests

`BorrascaTests.theTownKeepsItsRoutinesRoweSmokesTheSocietyOpensAndTheMailGoesRound` (batch `borrasca_routines`):

- Mr. Rowe starts at his counter and keeps a child out of the society, saying why;
- at the break, unwatched, he steps out front, says he is on his break, and the society's door opens to a child;
- the child reads the map; at the break's end he comes back in, finds the child and sends them out into the shop;
- the account keeps the society;
- Mr. Dorsey walks to the third door of his round, stands at the door he walked to, and talks to a child;
- years later a grown reader goes into the society while Mr. Rowe is in, and Mr. Dorsey is still on the route.

## Next

- **0.4.429, bikes and the truck:** children riding bikes up and down the street (a bike model of its own) and a truck that pulls up
  outside the diner while its driver eats.
- **The drugstore's arcade** (its own original games), the walk, and the lower rooms' rework follow.

## What stays

- **Witness:** 86 / 65 / 63.
- **Protocol:** 172.
- **Layout:** 135.
- **New builds only.**
