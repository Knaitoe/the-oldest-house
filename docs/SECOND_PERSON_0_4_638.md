# Second Person: the other homes (0.4.638)

The proposal's DANGER and RUNAWAY variants of the House's read of home (docs/SECOND_PERSON_PROPOSAL.md, chapters 1 to 4). New builds
only. No layout, protocol or Witness change: 90 sources / 68 required / two kinds / three endings. A settled read changes the voices;
a home the House did not read (or read as something else) keeps the old friend, unchanged.

## What changes

`SecondPersonStory.home(own)` reads the snapshot's home: HAVEN, DANGER, RUNAWAY or nothing. Every place the phone arc used to ask only
"haven or not" now asks which:

- **The caller.** HAVEN: Mum or Dad, then the ward. DANGER: someone from before, every time. RUNAWAY: a wrong number the first time,
  then Lou, a sister from the family they left. Otherwise the old friend.
- **The four calls, what they say into a silence, and what the resident says catching the phone** all have their own lines per home.
  Nothing violent is said by someone from before; it is that they have the number, and then the address.
- **The winter ticket.** DANGER: a card from a friend who knows, with a train ticket in it. RUNAWAY: a letter from Lou with a ticket
  home for the day. Their own lines when it is found and when they come back.
- **The sleepwalk to the latch.** HAVEN opens the door for somebody who is not coming; DANGER tries the lock twice and the door stays
  shut; RUNAWAY opens it and says they are going home.
- **Two cups.** The resident's line about the phone follows the home.
- **The account.** The caller's line, the sleepwalk left alone, the ticket on the mat, and letting it ring (DANGER: they lifted it and
  put it down) follow the home.

## New lines (all new in this slice)

Callers: "Someone from before, on the line" / "A wrong number, on the line" / "Lou, on the line".

Someone from before: "I know you're in." / "I know it's you. I can hear the street." / "You took the phone off the hook. That's fine."
/ "Flat two. Above the launderette. I've written it down." / "I've got a pen." / "It's me. You know it's me." / "I came round. You
didn't come to the door." / "I left you a note. Did you read it?" / "I'll come again." / "Is this still their number?" / "Tell them
nobody's looking any more." / "Tell them that. Please." / "Say something." / "You can't not say anything forever." / "I can wait." /
"I'm very good at waiting." / "Goodnight." / "I can hear you." / "You can't stay in there forever." / "...Is somebody there?" /
"Never mind. Never mind."

The wrong number and Lou: "Hello? Is that the chip shop?" / "Sorry. Sorry, wrong number." / "Sorry, love." / "It's Lou. Don't hang
up. It rang and rang, last time." / "It's Lou. Don't hang up." / "Mum gave me the number. She doesn't know I've got it." / "I just want
to know you're all right." / "It's Lou again. It's nearly the new year." / "I came up. I stood on your landing. I didn't knock." /
"I'll write. You don't have to write back." / "Is that you? It's Lou." / "You don't have to say anything." / "I'm glad you've got
somewhere." / "Hello? Chip shop?" / "...Wrong number. Sorry." / "I can hear you there." / "That's all right. That's all right." / "I'll
ring next month." / "Are you there? I think you're there." / "Happy new year. From all of us. Well. From me." / "That's all right.
I'll take that." / "Night, then."

The resident: "Hello? ...Nobody. It was nobody." / "How did you get this number?" / "Hello? ...No. Wrong number. Ha. Wrong number." /
"Lou. How did you— No. Don't. I'm fine." / "Nobody needs to know where." / "I slept. I slept the whole week." / "Home. Just for the
day." / "Nobody made me stay. Nobody even asked me to." / "Locked. It's locked. It's locked." / "I'm going home. I'm going home now." /
"I changed the number. Nobody rings now, and I like it." / "I wrote to Lou. I haven't posted it. I will."

The post: "a card from a friend who knows, with a train ticket in it: Come and stay till it's over. Nobody needs to know where." / "a
letter from Lou with a train ticket in it: Come home for New Year. Just for the day. Nobody will make you stay."

The account: "The one who rang was someone from before, who should not have had the number." / "The first time it was a wrong number.
After that it was a sister, Lou, from the family they had left." / "You let them be. They checked the lock twice, and woke sitting
against the door, cold." / "You let them be. They went out onto the landing, towards the stairs, and woke on the top step, cold." /
"They came back saying they had slept the whole week." / "They came back saying nobody had made them stay." / "(The first time / The
second time), you let it ring, and they lifted it themselves, and put it down."

## Tests

`SecondPersonTests.theOtherHomes` (batch `second_homes`): for each home and none, every call, silence and caught line belongs to that
home only; the callers, someone from before knowing they are in, the wrong number then Lou, and the tickets. The Second Person
namespace is 10/10.
