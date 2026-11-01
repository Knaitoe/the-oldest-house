# Borrasca: the rooms under the depot (0.4.237)

The owner, 1 November, on the review's note that Borrasca's trailhead warning described more than the vignette held: "the vignette
should contain the warned content rather than the warning being removed." And on the cast: the events had to have played out for
many generations of children, with the Sheriff and the Prescotts the same throughout.

The note at the trailhead (unchanged) reads: abduction, coercion, sexual violence and death; these remain off-screen; no assault is
shown or heard. This release puts each of them in the vignette, found after the fact, never shown happening and never described. It
is the third of three Borrasca releases (0.4.235, the trail in any order; 0.4.236, the Shiny Gentleman). No protocol, layout, Witness
or source-kind change, and nothing here is required to finish: the ending is still the machine stopped and her name carried out.

## Where

All of it is inside the wire, so it is reached only on the last night, when the gate is open.

- **The hatch.** In the depot's back room, a trapdoor in the floor and a ladder down through the rock.
- **The corridor.** Concrete, lit by three lanterns, with six rooms down each side. Every room is three blocks square and holds a cot
  and a bucket. Every door is iron and every bolt is on the corridor side: a lever by each door opens it. A tag plate over each door
  gives the room's number.
- **Room 6, hers.** The door stands open and the room is empty. Scratched into the wall: NELL, and beside it the reader's own name,
  the way the two of them cut their names into the tree. The plate over the door reads 263-A, TRANSFER, D-417.
- **Room 7, the newest.** Bolted, by the ladder. The plate reads 264-A, the next tag on the property receipt (Mara, the missing
  poster on the store's board). Inside, a folded blanket on the cot, still warm; the trucks went up the ridge road tonight.
- **Older names.** Low on the walls of five other rooms, one name each, scratched in other years: DANA (room 2), and others.
- **The desk at the end.** Two papers:
  - *Night book, lower rooms.* A book of nights kept by room number, never by name: times in and out, fees to the county
    maintenance account, "Approved: Sheriff's office" on every page, "Counted:" by E. Prescott in 1964, W. Prescott in 1979 and 1993,
    and J. Prescott tonight. A room "closed, to the yard". "Rm 2. Letter withheld." Her tag, "received", "will not eat", "asks for her
    friend by name", "transfer, D-417, night", and nothing after. The newest tag received.
  - *A letter, not sent,* in a child's hand: Dana, from room 2, to her mother. She was told she could write if she was good; her
    mother had been told she went to an aunt's. There are other girls; they knock on the walls at night, and the girl in four
    stopped knocking. Men come down at night; Mr. Prescott writes in a book; the Sheriff comes too. "I'm not allowed to say the rest
    and I don't want to." Stamped WITHHELD.
- **Behind the shed.** Fifteen low mounds in three rows, a stake at the head of each with a tag number (the tags before hers), and no
  names.

What each thing is for is left to the papers and the reader. The book never says what the visits were; the letter stops where it
stops; the mounds are only numbers. Nell is not found: the book says she was transferred, and nothing after.

## The account

The reader's account keeps what they found, in their own words: the rooms with the bolts on the outside; her name beside theirs on
the wall and the room empty; the newest room still warm; the book of nights, approved by the Sheriff's office and counted by a
Prescott, father and son, with "transfer, and nothing after" beside her number; a girl's letter home that was never sent; numbers in
the ground behind the shed. The Witness line adds, if the book was read, that it had the Sheriff's office on every page.

## Building

`BorrascaRooms.lowerRooms` and `graves`; the carving block (`borrasca_bark`) gains a `wall` variant, plain light concrete, on which
its block entity draws scratched names (the friend's and the reader's, or one older name) in place of carved ones. The asset tool
writes the new block state and model.

## Tests

`BorrascaTests#theRoomsUnderTheDepotHoldWhatTheNoteWarnedOf` (borrasca namespace): the hatch, ladder and corridor; twelve iron doors
bolted from the corridor, hers open; a lever opens a door; the night book and the letter read at the desk, the book carrying the
Sheriff's approvals, three Prescotts, her tag and transfer, the yard and the newest tag, the letter saying what it can and no more;
her name scratched beside the reader's in room 6; the newest room; a grave's number and no name; the account and Witness line; and
nothing of it required to finish. `BorrascaVisualProof` fits both new papers to the native lectern page.
