# The Sphinx: survey pins and what every crossing shares (0.4.334)

From the owner's draft "The Sphinx" (4 November). The owner asked for the rest of the Sphinx design to be finished next, with every
encounter working in multiplayer. This release builds what all four crossings and the dream share, and adds the co-op survey pins to
the first crossing (docs/SPHINX_0_4_306.md). The stair, shelf and field crossings, the dream and the Dream Stela fragment follow in later
releases and use these records.

## What the House remembers

The draft's rule: "Keep the physical result of a crossing shared. Keep each reader's answer and conduct separate."

`SphinxCore` keeps everything under `sphinx_0306`:

- one shared room per crossing: `Room` for the vestibule, and later `Stair`, `Shelf` and `Field`;
- one record per reader per riddle: `early`, `stair`, `shelf` and `field`.

A reader's record holds:

| Key | Meaning |
| --- | --- |
| `Heard` | She asked them the question. |
| `First` | Their first deliberate answer to that riddle: 1 right, 2 wrong. A pin counts, as below. |
| `Solved`, `SolvedAt` | A correct answer, by pin or latch, a later retry included, and when. The latest one carves their stone. |
| `Walked` | How many times they chose to leave with the question heard and unanswered while she was not provoked. Disconnecting or being swept back does not count. |
| `Forced` | They pulled a stop pin themselves. |
| `Retreated`, `Swept` | They were in the room when she came, or were swept back by her paw. |
| `Pinned` | Which choice their survey pin marked when the attempt was locked, plus one. |

`SphinxCore` also works out what the dream needs from those records:

- which riddles a reader has resolved: answered, deliberately walked away from, or forced;
- their accuracy: correct first answers divided by riddles deliberately answered;
- their conduct:
  - **hostile** if they ever pulled a pin;
  - **dismissive** after two deliberate walkaways;
  - **patient** otherwise, whatever their answers were;
- the riddle they solved most recently.

## Survey pins

The draft: "let each reader set a small survey pin at their pick while the latches are still shut. The first committed crossing locks
the submitted answers for that attempt, then moves the room forward for everybody."

- **Setting a pin.** Beside each door's latch there is now a brass survey socket. Right-click it to set your pin there. Right-click another socket to move the pin; right-click the same socket again to take it back out. The socket shows everyone how many pins stand in it (none, one, or more). A pin is not an answer, and nothing is said aloud.
- **Locking the attempt.** The first latch lifted locks the attempt:
  - each pinned reader's pick becomes their answer, and they are told so;
  - the reader at the latch answers with the latch if they set no pin;
  - a reader who pinned the right door is credited even when a friend pulled a wrong latch;
  - every pin then comes out for the next attempt.
- **A latch in motion.** It consumes the attempt once. A second latch lifted while the first is still moving (four ticks) does nothing. Two readers on the same tick cannot double-trigger her or open a sealed way.
- **Unanswered.** A reader who leaves the room or goes offline with a pin set takes it with them, unanswered. A reader who never sets a pin stays unanswered unless they lift a latch.
- **Solo.** Alone, the first latch is the first answer, as before.

## Tests

`SphinxTests#surveyPinsLockEachReadersAnswerAtTheFirstLatch` (exploration) plays this with three readers:

- the pins are set and shown;
- a pin is moved;
- a reader leaves with their pin and takes it with them, unanswered;
- two latches on the same tick make one attempt;
- the puller's wrong pin and the friend's right pin are each credited as their own;
- the sockets empty;
- a later solo latch opens the door.

The first crossing's case still passes unchanged.

## Assets

`tools/draw_sphinx_fittings.py` adds the socket and a surveyor's pin with a cloth tag: three socket states and two textures.
