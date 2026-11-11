# Bike night 11: a bike on the slide (0.4.477)

A piece of the playground the proposal has and the slide (0.4.466) did not (`docs/BIKE_NIGHT_PROPOSAL.md`):

> **Slide.** A long metal slide. A reader goes down it seated and shoots off the end with speed. Riding down it on the Wasp
> is possible, and Gus is impressed.

I missed the second sentence when I built the slide, and found it reading the proposal again for Gus.

## What was already there

More than I expected. A bike rolls up any step a block high or less, and the chute's collision is a flight of low treads; so
a bike already rode up the chute from its level end. Up on the deck, between the guards, there is room to stop; a stopped
bike turns where it stands toward where its rider looks; and it rolls down again. The new case does exactly that on the
Wasp, and it passed the first time it ran. What was missing was the game knowing it had happened.

## What is new

- **It is noticed.** A reader who has been up on the slide's deck on a bike, and then comes off the end of the chute still
  on it, without leaving the slide's line between and within twenty seconds, has ridden it down. They are told ("Down the
  slide on a bike. The chute rings under the tyres all the way to the sand."), and it is in their record with the bike.
- **Gus is impressed.** "You rode down the slide. On a bike. I'm telling everyone. I'm telling my mom, and she thinks I'm
  at the library." Once a night, with both arms up.
- **The account.** "I rode the Wasp up the slide, turned it round at the top, and rode it down again."
- **Part way does not count.** A bike ridden a little way up the chute and down again has not been on the deck.

## Any bike, not only the Wasp

The proposal names the Wasp. Any of the six can do it, because nothing about the slide stops the others, and I have not
added something to stop them. The Wasp is the natural one: it turns round on the deck in half the time of most (ten degrees
a tick against six, and the Comet's four), and a glance off a guard costs it almost nothing. If it should be the Wasp's
alone, the least artificial way is to make the deck too tight for a longer bike to be turned on; say so and I will.

## Tests

`BikeNightTests`, one more case (33 in the namespace): **a bike is ridden up the slide, turned round on its deck and ridden
down again.** Up the chute pedalling and braked to a stop on the deck, between its guards; turned round where it stands;
down, and off the end still riding; told, recorded and in the account; Gus says so; and a second go that turns back part way
up is not counted.

## What I have not seen

- It done by hand: how hard it is to stop on a deck one block long without going off the back over the ladder. The case
  brakes as soon as the bike is near the top; a person has to judge it.
- Gus with both arms up.

## Numbers

No layout, protocol or Witness change. No art, no sound. New builds only.
