# The Garritys' letter (0.4.459)

The third part of docs/BORRASCA_LIFE_PROPOSAL.md. The owner, 10 November:

> Carry a letter for Dorsey. He already mentions the Garritys' mailbox going untouched. On the second visit, he could hand you a
> letter to leave under their porch door. Years later, the box is still on its post, with the family name barely legible under
> cloudy tape. You remember where it is because you've been to that porch.

All of it is optional. A reader who never speaks to Mr. Dorsey loses nothing they need.

## The house

The friend's family had no house. They have the fifth on the street now, across from the church: dark boards under grey slate, a
redbud and flower beds in the yard.

- **Its front door is its own** (not one of the game's doors): four panels, painted, a brass knob, no window and no slot. It
  does not open, on any visit. Years on the paint has come off it in long strips down to grey wood.
- **Its curtains are drawn**, at all six windows, in printed cotton. Years on the sun has had them.
- There is a room behind them (two beds, a table and three chairs, drawers), which nobody is let into.

## The mailboxes

Mr. Dorsey says "Same boxes, mostly", and there were none. Every gate on the street now has a mailbox on a post, its mouth to the
street, the family's name on a paper label under tape. The four households keep their name plates as well.

The Garritys' box is the one that changes:

| Visit | Their box | A hand on it |
|---|---|---|
| First | As any other, the flag down. | "A mailbox on its post, the flag down. GARRITY, on a label under clear tape." |
| Second | Its door hangs open and a week of mail stands out of it: two envelopes and a rolled catalogue. | "The Garritys' mailbox. Its door hangs open on a week of mail." |
| Years on | Rusted, its flag gone, the tape over the label gone cloudy. | "A mailbox still on its post, rusted, its flag gone. The tape over the label has clouded. Under it, barely, GARRITY." |

The others' boxes are seen to: years on a hand on one reads "...on a label under tape. The tape is new."

## Their door

A hand on the door is a knock, and what a knock gets:

| Visit | |
|---|---|
| First, with the friend beside the reader | She says: "That's my house. Mom's waxing the floors. Nobody goes in till they're dry." |
| First, alone | "You knock. A radio is playing inside. A woman calls that Kelly is out, and to try the trail." |
| Second | "You knock. A floorboard shifts inside. Nobody comes." |
| Years on | "You knock. Nothing in there moves. The paint has come off the door in long curls." |

## The letter

On the second visit, before the friend is taken, Mr. Dorsey says what he has always said: "Got a letter here for the Garritys.
Nobody's emptied their box all week." Asked again, he hands it over: "Their box won't take another. You know their girl, don't
you? It's the house across from the church. Push it under the porch door for me."

It is the reader's own, as the comic is, and stays in their pack between visits. Used, it is held up: **an envelope, and nothing
else** (`client.BorrascaLetterScreen`). A click, or space, turns it over.

- Its face: who it is from, printed in the corner (PRESCOTT ORE SERVICES, Ridge Depot, and the company's mark); who it is to,
  typed (Mr. & Mrs. Garrity, Drisking); a stamp under a postmark of June 1992.
- Its back: a flap, sealed.

**It is never opened**, by a child or years on. What is in it is not written anywhere in the game. Nobody says who it is from
either: Mr. Dorsey does not, and no line does. It is printed on the envelope for a reader who looks. That is the owner's own
device for the church supper (the company's name seen early on a kindness, and known again at the depot), used here.

**Under the door.** A hand on the door with the letter *in the hand* pushes it under: "You push the letter under the door. It
goes in as far as its corner." From then on a corner of the envelope shows at the door's foot, the corner with the sender printed
in it. With the letter only in the pack, a knock is a knock, and the line says the letter is still there: keeping it is the
reader's choice. A child cannot get it back out.

**Years on.** The letter is where the reader put it, gone yellow: "The corner of an envelope shows under the door, gone yellow.
It has not been moved. It is loose." A second hand draws it out ("It comes out from under the door whole, still sealed. Nobody
ever took it in."), and it is the yellowed one the reader then carries. It can be put back, and drawn out again. A reader who
never took it to the door still has it, and can push it under years late; it lies there as it is, not yellowed, and comes out
the same.

**Mr. Dorsey afterwards.** That day: "Under the door? Good. That's one I don't carry back." After the friend is taken, to a
reader still carrying it: "That letter's still theirs. Push it under the door, and then go home." Years on, once, to a reader who
has it with them: "That's one of mine. I gave that to a kid, the week the Garrity girl went. ...Keep it. There's nobody there to
want it."

**The account** says what happened and nothing else: that Mr. Dorsey gave the reader a letter for her family; that the company's
name was printed in its corner, only if the reader looked at its face; whether they pushed it under the door; and, years on, that
it was still there, that they have it now, that they put it back, or that they took it late.

## How it is made

- `labyrinth.BorrascaGarritys`: the lot, the box on each visit, the door's answers, the letter under the door, Mr. Dorsey's lines
  for it, the account's two sentences. `BorrascaDressing.house` builds the fifth house and stands a mailbox at every gate.
- `borrasca_home` gains six kinds: `box`, `box_full`, `letter`, `curtain`, `door`, `door_top`. A mailbox is in the cell over its
  fence post. The envelope's corner lies in the cell before the door and is drawn under the door's leaf; a hand cannot find it
  (it has no shape), so the door answers for it.
- The letter is `borrasca_letter`, a keepsake item under the key `Mail` (`Letter` is the note read under the depot).
- Two payloads, `BorrascaLetterPayload` (the envelope held up) and `BorrascaLetterSeenPayload` (a side looked at for a moment).
  **The network version is 181**: a client and server must both be 0.4.459.
- Thirteen new pictures and eleven new models at sixteen pixels by `tools/draw_borrasca_homes.py`, and the letter's two pictures
  in the pack.
- One tree behind the lot has moved north to make room.

New builds only. No Witness change.

## Tests

`theGarritysLetterGoesUnderTheirDoorAndIsThereYearsOn`: the house, its shut door and its twelve curtained panes; a mailbox and a
name plate at each of the four gates and a mailbox at theirs; the box on each visit; a knock on each visit, with the friend and
without; Mr. Dorsey's lines in order and the letter handed over on the second; the face looked at; the letter kept while it is in
the pack and pushed under from the hand; the corner at the door; years on, found, drawn out yellow, known again by Mr. Dorsey, and
put back; and the account at each point, for a reader who kept it and one who took it late as well.

A new page of the Borrasca client proof (`letter`): the door, the curtains and the three mailboxes in both years, the corner
under the door, the letter in the pack, and the envelope's face and back as they were and years on.

## What I could not check

I have not played it by hand. The envelope's lettering is checked to fit by the proof, and I have looked at the proof's page.
