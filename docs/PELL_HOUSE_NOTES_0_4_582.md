# The Pell house with more than one reader (0.4.582)

The owner, 14 November: "With the idling activities for npcs and player(s) in the Pell house, did you account for multiplayer
instances too?"

They were built for it and not tested for it. What June, Tilly and Callum do while the house waits (0.4.570), and June's
jobs, Tilly's question and the book (0.4.571), each had cases with one reader in one house. This release adds cases with two
readers, with a reader as Tilly, and with a second house, and makes one change. New builds only. No protocol, layout or Witness
change, and no line changed.

## What the house does

- **Each house is its own.** A second pair has a house of their own (0.4.547). The errands, whose turn it is, which lines have
  been said, June's ticks on the chart, the plates, the bowl and the book are all kept on that house's own record. Plates and
  the bowl carry the house they came from, and go back if they are carried out of it.
- **Two readers in one house.**
  - June, Tilly and Callum notice whoever is nearest, and an errand's line is said once a visit if any reader is in earshot.
  - The plates: see below. Either reader lays. June's "Four. Good. Always four, love." is kept by whoever heard it.
  - There is one bowl. Whoever picks it up has it; the other finds its place bare.
  - Tilly asks each reader, on that reader's first hand on her, and each is offered only what they have put a hand on
    themselves.
  - One reader has the book at a time; the other is told "Somebody else has the book." How far each has read is their own.
  - Each account has only what its reader did.
- **A reader as Tilly.** The house sends Tilly on none of its errands. Nobody asks for the book and nobody answers it; the
  other reader reads it aloud. The reader as Tilly can do June's jobs.

## What changed

**The cupboard shares the plates.** The 0.4.571 notes said two readers can share the plates. They could only if the first
dropped some: the first hand on the cupboard took down all four. Now, with another reader at the house who has taken none
down this visit, a reader's first handful is half of what the table still wants (two of four), and the rest is left in the
cupboard for the other. A reader who comes back takes whatever is left, so one reader can still lay the whole table if the
other does not. One reader alone takes four, as before.

In the code: `PellPeople.Household.tookPlates` (who has taken plates down this visit, cleared when the visit begins again) and
`PellJobs.cupboard`.

## Checks

- **A new case,** `PellHouseTests.twoReadersInOneHouseShareJunesJobsAndEachKeepsTheirOwn`: the first reader takes two plates
  and no more while they have them; the second takes the other two; each lays two, and the job is ticked; both heard June.
  The first has the bowl; the second's hand on the kennel puts nothing down; filled and set down, it is ticked, and only the
  reader who filled it keeps that. Tilly asks the first reader and offers the desk and not the marks; then asks the second
  and offers the marks and not the desk; what each has told her is their own. The first has the book and the second is told
  somebody has it; the first reads the chapter; the second then reads from the ticket, their own place. Each account has its
  own reader's doings, and the bowl is in one of them only.
- **A new case,** `PellHouseTests.withAReaderAsTillyTheBookIsReadAloudAndJunesJobsAreStillTheirs`: June and Callum go about
  their errands and Tilly is sent on none; the other reader reads the chapter aloud with none of Tilly's lines said by the
  house, and it is ticked; the reader as Tilly takes half the plates and lays a place.
- **The second-house case** (`aThirdAndFourthReaderHaveAHouseOfTheirOwnAndEachAccountNamesItsOwnPair`) now also checks that
  the second house's plates are its own and lay nothing in the first; that its bowl is picked up while the first house's
  stands full of leaves; and that its June goes on her errand while nobody in the first house is sent on one.
- **The Pell house namespace** (65 cases) and the cast and two-reader namespaces (102), run together: 167 passed.
- The quick checks, and the declared case count (1,569).

## For the owner

No line was written or changed. Three things to know, the first a choice of mine:

- **The plates split in half** when two readers are in the house. If the first reader should still take all four, it is one
  line to take back.
- **Tilly asks each reader in the same words.** "Is it nice? Tell me what it looks like." is said to the first reader's hand
  and again to the second's. If the second asking should be different, it needs a line from you.
- **While one of the Pell people is saying something, another reader's hand on them gets nothing,** not even a line that says
  so. That has always been so for one reader; with two it is easier to meet.

## Still to come

The rest of the notes from the 14 November playtest: the yard's stock stairs, wall light, long table and floating lamp.
