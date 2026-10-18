# Small fixes from the vignette review — 0.4.105

On October 18 the owner asked for the smaller batch of fixes from the gameplay and storytelling review of every vignette.
The candidate is 0.4.105 on verified 0.4.104 (fd6a73f). Protocol 76 and layout 45 are unchanged; no saved state changes
shape. 46 sources / 35 resolutions / two kinds / three endings are unchanged.

## Floorboards: the board says what it needs

The loose board comes up only under an axe. The room supplies none and nothing mentioned one, and every attempt on the
board with anything else was refused *and* added 35 of the room's 100 noise points, so a player digging where the
heartbeat is would be sent back out after three tries with no explanation.

- Right-clicking, hitting or breaking the board with anything but an axe shows one line on the action bar: "It won't come
  up by hand. An axe would pry it up." Right-clicking still knocks, as before.
- Attempts on the board itself add no noise. Breaking anything else in the room is still loud.

## Account titles

The literary account books were titled with internal ids ("The account: mapping interior", "devils rock", "elk carcasses").
They now use the House's prose place names (`StaircaseProse.place`): "The account: the crawlspace house", "the diary room",
and so on. A written book title holds 32 characters, so the few names that would not fit are shortened ("the nursery", "the
workshop", "the fan room", "the crawlspace", "the ridge", "the cabin", "the copied home", "the cottage", "the lake house").
Three Fingers, Lake House and Borrasca had no prose names; they gain "the cottage on the mill road", "the house under the
lake" and "Drisking", which also reach any staircase prose that names them. The House's experience page no longer names a
room the reader turned back from by its id.

## Elk Fan: the account and the Witness line agree

The Witness line said the light was repaired and that "on later returns the shape became tape, then a tooth". What happens
is the other way round: the reader stands under the blades until they are thrown out with a tooth in their hand, and the
room is taped off when they come back. The line now reads "I fixed the light and stood under the turning blades until they
threw me out. A tooth was in my hand. When I went back, tape held the shape of the deer." The account no longer names the
mechanic ("Five seconds was longer than I thought" becomes "I stayed up there longer than I meant to").

## Masque: the chime costs something

The steward's paper says to stand still at the chime, but moving only made a red figure speak.

- A second after the first stroke (20 ticks into the chime), a reader who is still moving is put back a room, at the near
  door of the room before the one they were in, facing the clock. The red figure there says "The clock is speaking.", and the
  action bar says "You moved while the clock spoke. You are a room further back."
- The rooms past it count again only once they are crossed again (stopping the clock still needs all seven).
- It happens at most once a chime, and not at all once the clock has stopped.
- The party music now starts in every world: it was checked on one tick that the five-tick scene clock could miss forever.

## Hide-and-Clap: the stakes before the blindfold

Failing the game kills the player and drops everything they carry, even with keepInventory on; the code does this on
purpose, so the drops stay ordinary recoverable items. That is unchanged. What changes is that a reader is told before the
blindfold goes on: entering the room now adds a chat line, "If the minute runs out you die, and drop everything you carry,
even with keepInventory on." The note stays in the child's voice.

## Validation scope

- **Floorboards:** both pry cases now first try the board by hand: it stays, the event is refused and the noise does not
  change; breaking a wall afterwards is still loud.
- **Titles:** the Elk Fan case checks every account title is a prose name within 32 characters, and the fan's own title.
- **Masque (new case):** standing still through a chime keeps the reader's place and rooms; moving puts them back at the near
  door of the room before, forgets the rooms past it, and a second move in the same chime does not put them back again.
