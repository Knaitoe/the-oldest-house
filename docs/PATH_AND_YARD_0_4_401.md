# Doors follow the path, and the yard is a place (0.4.401)

The owner, after 0.4.400:

- "pretty sure it's sending me to where it expects me to be based on depth, not through the actual path of the labyrinth"
- "it's also feeding me yard maps when i'm not in the yard"

## What was wrong

**The yard was a scatter of rooms.** Every yard place was a gray room dealt by depth alone: the turn-around, ditch and clothesline, the survey stakes, the flood cut, the stock gate, the windbreak, the ditch in flood, the storm field, Truant's sheds and the Sphinx's field. Any ordinary door of any hall could open onto one, from the story depth on, and often did. So a storm field could open off a plaster corridor, and the next door led back into the halls.

The owner's draft ("The House Has a Yard", 4 November) describes one place:

- "You open a back door onto wet grass. There's a bulb over the porch, a turnaround beside the garage"
- "The driveway, gravel turnaround, drainage ditch, washing line, and overgrown tree line by the state route all belong to the same domestic space."
- "A reader should be able to learn that layout and use it."

**A friend's door was only held for half a minute.** Since 0.4.400, whoever followed through a door within half a minute came out where the first reader went. After that, the door was dealt by the follower's own route and depth, even with their friend still on the other side of it. A slower friend came out somewhere the House chose for their depth, not where the door had just led.

## What changes

### The yard is entered by its back door (`YardStretch`)

- **From the House's halls**, only a back door onto the yard is dealt. This is the turn-around, or one of its once-only variants that start on the same porch (the long drive and the porch turn).
  - The turn-around carries the yard's whole share again (`YardStretch.BACK_DOOR`, weight 6). That was its weight before the ditch and the clothesline split it.
  - No other yard place, and none of the yard's stories, opens off a hall.
- **Inside the yard**, the ways on lead further into the yard. That covers its connectors, its puzzles and hazards at their own pacing, Truant's sheds and the Sphinx's field.
  - The drive does not keep coming back round to the porch. The long drive is the one authored time it does.
  - No rest rooms or interior oddities are dealt there.
- **After three yard places in a row** (`YardStretch.LONGEST`), the way on leads back inside. The same happens sooner when nothing more of the yard is due.
- **The yard's stories** are The Supper, In the Tall Grass, Growing Things and The Last Crossing. They open only off the yard, and off the yard they come first.
  - A reader whose next story waits out there, and nothing else, is dealt the back door in its place.
  - A scent goes on with them, to find the story in the yard.
  - That back door does not count as a story dealt, so the House's story pacing carries on into the yard.

### A door leads where your friend went while they are beyond it

A dealt door is held behind the first reader through it:

- for half a minute, as before;
- **and for as long as that reader is still beyond it**, meaning their way back still runs through this door.

Whoever follows through it in that time comes out in the same room, however long they took to get to the door. Half a minute after the first reader comes back through, it is each reader's own door again.

Stories keep their own rules. A friend who finished the story, or whose story there went another way, keeps their own door and is told why.

A fold never takes a reader alone behind a friend who went on through the same door. Their friend's crossing takes them on (`GroupCrossings.ledThrough`).

## Tests

- **New exploration case `ExplorationPassTests.theYardOpensOnlyByItsBackDoorAndLeadsOnIntoItselfForAStretch`:**
  - from a stone hall, no yard place but the back door and no yard story, over 800 dealt doors, with the back door among them;
  - out of the back door, every ordinary way on leads further into the yard and never straight back round to the porch;
  - after three yard places, every way on leads back inside;
  - the yard's stories only off the yard, and first there;
  - a story waiting only in the yard deals the back door.
- **`CoopTests.aFriendWhoFollowsThroughAHallDoorComesOutInTheSameHall` extended:**
  - two minutes on, while the first reader is still beyond the door, it still leads where they went;
  - once they have come back through it and the half minute is long past, a reader's door is their own again.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
- **New builds only.** Doors a reader has already been dealt keep what they lead to.
