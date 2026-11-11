# Bike night 17: the people on the pavements (0.4.485)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`), in three places:

> - **Voices:** people call from windows and sidewalks, in captions: "Get off the road!", "Go, kid, go!", "Leave 'em
>   alone, Tully!"
> - trick-or-treaters in costume on the sidewalks with pillowcases; reusing Costume Night's painted costume skins where
>   they fit.
> - **The theater:** [...] A line of costumed teenagers waits outside. As the readers pass, the line cheers and parts
>   for them, then closes on the bullies.
> - **The burger place:** [...] Teenagers sit on the hoods of their cars in the lot. They cheer the readers and boo the
>   bullies.

All four are here. What is left of the town is its set pieces (the laundromat, the grocery, the arcade, the school
field's tracks, its sounds).

## Who

Fourteen, and two parked cars.

**Costume Night's skins do not fit.** Each of those is an entrant in a contest with a number pinned to their chest, and
all thirteen are dressed as the same drowned girl. So the fourteen have skins of their own, painted by the same painter
(`tools/paint_character_skins.py`, `literary_calder_*`):

- **four children out for sweets**, each with a pillowcase: a bedsheet ghost with two eyeholes, a skeleton in a printed
  hood with a painted skull, a pumpkin's face on an orange shirt, a vampire with a red collar and two teeth;
- **six teenagers in the theater's line**: a rubber monster mask, a painted skull, a cardboard robot with its works on
  its chest, a mummy, a cat in eyebrow pencil, a scarecrow with a stitched mouth;
- **four who are not dressed up**, two to a hood: a letter jacket and a denim one on the sedan, a striped sweater and a
  ball cap over a hood on the pickup.

The pillowcase is a thing of its own in a hand (`treat_sack`, drawn by `tools/draw_calder_play_art.py`): ticking
stripes, the neck gathered in the fist, the weight in the bottom. Nobody is given one.

The two cars are a blue sedan and a white pickup with nothing in its bed, parked nose out in the burger place's lot.
They are the cars' own models in two more paints (`tools/draw_calder_cars.py`), and stand with their lights off.

## What they do

- **The ghost and the skeleton go round Elm Court** from door to door: up the west pavement to the first house's walk,
  on to the second's, across the turning circle to the house opposite, and back down the east side. On each step they
  stand side by side, and one of them says "Trick or treat!" They take it in turns.
- **The pumpkin and the vampire go along Main Street's north pavement** and back. They stop to look in the
  laundromat's window, and the video shop's, and have something to say about each.
- **The line stands down the middle of the pavement in front of the theater**, each looking at the back of the next.
- **The four on the hoods sit back on their hands** with their legs out along them, looking over Park Street.

**Any of them a reader rides by cheers.** Arms go up (a trick-or-treater's one free arm), and one of them calls: "Go,
kid, go!" Somebody standing still on a bike is not riding by, and neither is somebody on foot.

**Any of them one of the three rides by boos**, hands cupped, and one of them says what they think: "Leave 'em alone,
Tully!"

**The line parts for a reader riding along the pavement.** Every other one of them steps to the theater's wall and the
rest to the kerb, and the way down the middle is clear. With the rider gone by they are back in their places. They do
not move for somebody in the road, and they do not move for the three: **one of the three who rides into the line is
stopped**, told "No cutting, Ray-Ray.", and walked out into the road, two seconds in all. He answers when they let him
go. The line will not have the same one again for fifteen seconds.

A hand on any of them gets a word.

Everything they say is over their heads, as Gus's is and the drivers'; none of it is ever in chat. None of it says
anything of what a reader is.

## When

They are out from when the gate is opened, and so now are the cars, which in 0.4.484 waited for the chase: a reader
who goes out into the town ahead of the three finds it awake.

The night over, none of them is anywhere, and the next night has them where they began. Somebody out of sight (their
part of the town asleep) is where they were when it wakes; nobody is made twice.

## Numbers

| | |
|---|---|
| A child's size, and a teenager's | 0.56 and 0.8 of a grown body |
| A pair's pace | 0.085 blocks a tick |
| Standing at a door or a window | 4.5 seconds |
| A place they cannot get to is given up after | 25 seconds |
| Cheering reaches | 9 blocks (10 from the line, 14 from the hoods) |
| Booing reaches | 9 blocks |
| Arms stay up | under 2 seconds |
| Between two cheers from the same people | 8 seconds |
| Between two boos | 10 seconds |
| The line parts for a rider within | 7 blocks of either end, on the pavement |
| The line holds one of the three for | 2 seconds |
| And will not hold him again for | 15 seconds |

## Decisions for the owner

- **Own skins instead of Costume Night's.** The proposal's "where they fit" is read as nowhere, for the reason above.
  If the two towns' Halloweens are meant to share faces, the ten in costume can go back to those skins in one line.
- **The four on the hoods have no car of their own driving.** The lot's cars are parked for the night.
- **Nobody calls from a window.** The proposal's "windows and sidewalks" is pavements and drivers here; a lit window
  with a voice in it would be a set piece of the next slice if wanted.

## Cases

Four, in `BikeNightTests` (61 in all):

- the fourteen: one of each, in their own skin and size, a pillowcase in four hands, the line on the pavement and
  two over each parked car's hood; each stop of the court's round is on a house's step; how every line of theirs
  reads; a hand on one; nobody before the gate is opened, and nobody after the night is over;
- both pairs once round: each stop stood at and looked at, what is said there said by one of them, in turns; the second
  never far behind; Main Street's pair never in the road;
- a rider standing still is not cheered; one riding by in the road is, and the line stays where it is; one riding along
  the pavement goes straight through with nobody nearer than half a block, and the line is back behind them; the four on
  the hoods cheer a rider on Park Street;
- the line boos Tully in the road and does not stop him there; Ray-Ray, put into the line, is stopped, told, answers,
  and is off the pavement when he is let go; the line has not moved.
