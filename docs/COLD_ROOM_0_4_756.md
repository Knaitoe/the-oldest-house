# The cold room, part 3: the rite (0.4.756)

The third part of the cold room (design: [WAXWORKS_AND_COLD_ROOM_0_4_749.md](WAXWORKS_AND_COLD_ROOM_0_4_749.md); earlier parts:
[COLD_ROOM_0_4_754.md](COLD_ROOM_0_4_754.md), [COLD_ROOM_0_4_755.md](COLD_ROOM_0_4_755.md)). The rite is read from the book on the
chest of drawers. The offer, the end and the account come in part 4; the House still does not deal it.

## Beginning (`ColdRite`)

- A hand on the book opens it (the book's open stage). The priest leaves the window and kneels by the bed, between its foot and the
  flask's table, facing it, and says: "Kneel there, where I can see you. Read when I read. Don't stop for anything she says."
- One rite to each copy of the house. A hand on the book after that tells where it has got to: "The book lies open at page n of twelve."

## Reading

- A reader reads while they kneel (crouch) in the back bedroom with their eyes on the open book: within 35 degrees of it and seven
  blocks. Their back is to the bed while they do.
- While anyone reads, the pages turn: eight seconds of reading a page for one reader, a half again faster for each more.
- Each page read is said, in grey, to everyone in the bedroom: "The priest reads, and you read with him:" and the page, or "You read:"
  once he has stopped.
- Spectators never read.

## Interruption

- When she speaks to a reader (part 2), that reader loses their place: "You have lost your place in the book."
- They read again once two seconds have passed and they have their eyes on the page: "You find your place again."
- During the rite she speaks every eight seconds instead of twelve.

## The cold

- It rises while nobody reads (four a second) and falls while someone does (three a second), from nothing to a hundred.
- Above a quarter, breath shows in front of every reader in the house; the game's own frost comes into the view as it climbs, always
  held short of fully frozen, where the game's freezing would hurt.
- At three quarters and above it hurts, half a heart every two seconds, but never below three and a half hearts.
- The first time it reaches each step, the reader is told: "Your breath stands in the air." / "The cold is in your hands now. The pages
  are hard to turn."

## The water

- The flask on the bedside table holds six. A hand on it during the rite shakes water over the bed: "You shake water from the flask
  over the bed. She goes quiet."
- For five seconds after, she is quiet: she does not speak, nothing moves, and the cold holds where it is.
- After the sixth: "The flask is empty."

## Holding her

- A reader kneeling at the bedside, within reach of her, holds her down: "You hold her down by the shoulders. The bed stays where it is."
- While anyone holds her the bed does not rise, and if it is up it comes down under their hands.
- A reader alone reads and sprinkles in turn, and holds her when the bed is up.

## The priest

- He kneels and reads with the readers.
- At the eleventh page his heart gives out: "The priest stops between one word and the next. He is sitting in the chair by the bed, his
  hands open in his lap, and he does not answer." He is found in the chair by the bed, head down. A hand on him: "His hands are cold.
  He does not answer."
- The readers read on alone. The twelfth page is blank: "The last page of the book is blank." Reading stops there (part 4 brings what is
  said in its place).

## The pages

All new, for the owner's review; nothing is taken from any liturgy, book or film.

1. "I name this house, its doors and its stairs, and everything in it that was not asked in."
2. "I name the cold in this room. It is not hers. Let it go back where it came from."
3. "Whatever has her by the wrists, listen. You were not asked here, and nobody here wants you."
4. "Her name is her own. You have no use for it. Give it back."
5. "Every word you say is a borrowed word. Give them back to the ones you took them from."
6. "We will still be here when you are tired."
7. "Leave the bed. Leave the drawers. Leave the cross where it hangs."
8. "You can have nobody in this house who does not give themselves to you."
9. "Go out of her by the way you came in."
10. "Go out of her. Go out of her. Go out of her."
11. "Nothing is owed you here. Nothing was ever owed you."
12. (blank)

## Kept for the account

Each reader's own: `BeganRite`, `ReadSeconds`, `HeldSeconds`, `Sprinkled`, `Pages` (the furthest they heard read).

## Checks

- `ColdRoomTests#theRiteItsPagesTheColdTheWaterAndThePriest`: the book opens and the priest kneels; kneeling with eyes on the book a page
  is read and said; a kneeling spectator reads nothing; spoken to, the reader loses their place and reads again after looking back; the
  cold rises with nobody reading, frost short of frozen, the hurt never past the last three hearts; six sprinkles, then empty; a second
  reader at the bedside holds her and the risen bed comes down; at the eleventh page the priest is in the chair; the twelfth is blank and
  reading stops there.
- A fresh build of the house starts a fresh night.
- The earlier cold room cases and the group, cast and two-reader sweeps.
