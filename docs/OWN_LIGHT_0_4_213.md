# Your own light (0.4.213)

The last of the dark halls' five horrors (docs/DARK_HALL_HORRORS_PROPOSAL.md). It reaches into the reader's home, so it was built
last. The owner, 1 November: these "should be the most extreme yet, either psychologically or physically".

Slot 143, layout 69, no protocol change. One new Witness story, of the understanding kind: **66 sources / 50 required / two kinds
/ three endings**.

## Where it is dealt

Only in the dark band, from depth sixteen (`DarkHalls.DARK_DEPTH`): one door among several, with the usual story spacing
(`DarkHalls.horror`). It is a one-shot room; a failed visit can be dealt again.

## The fork

Built whole in the dark halls' ash (`OwnLightSite`), with no light of its own:

- **The lobby:** nine wide. Words are scratched into its west wall (the source).
- **The branches:** two, side by side, three wide and forty long. The wall between them is broken by low gaps every six blocks, so
  each branch can be seen from the other.
- **The far room:** the branches meet again here. Its far wall has the way on and sixteen places for torches. The stub of a torch
  burnt down to nothing lies on its floor (the account).

## What happens

**Level with the reader.** When the reader goes down one branch, someone walks the other, level with them.
- **What the reader sees:** the reader's own body, in their own skin (the House's body if they have one), with their walk and a torch in hand.
- **Who sees it:** the reader alone. It is a private figure, and a peer sees an empty branch.
- **Matching:**
  - It stops when they stop, crouches when they crouch, and turns its head when they turn theirs.
  - For the first stretch it is a beat late (half a second).
  - Past the first gaps it is a beat early: it moves before they do.
- **Torches:** every torch or lantern the reader sets down, it sets one across the wall from it. (The House lets lights be set
  down only in its ordinary halls; this is the one story room where a reader may set their own, `OwnLight.allowsPlacing`.)

**Halfway, it stops matching.** It comes through the gap behind the reader into their branch and walks back the way they came.
- Its own torches go out.
- It takes the reader's torches down one at a time as it passes them, each with the House's low rumble.
- It goes out through the way in, which is heard to shut, far back.

## How it ends

**Going on without following it.** In the far room, the reader's torches are set into the far wall in the order they were placed,
burning (`let_it_take_them`). Anything it had not yet taken down is taken then.

**Following it back** (six blocks or more back toward the way in, after it has turned):
- every torch in the place goes out at once;
- the House rumbles and the way in slams;
- the reader comes to at the fork with no light but what is in their hands.

Nothing placed comes back, and the far room holds nothing for a reader who followed. The visit can be tried again.

**Either way, it goes home first.** The next time the reader is at their bed (their respawn point, within twelve blocks):
- it has been slept in;
- the nearest chest has every slot turned the other way round, with nothing taken;
- candles and campfires left burning nearby are out;
- a note lies on the pillow, in the reader's own handwriting:
  - "Thank you for the light." if they followed;
  - "Not yet." if they did not.

The Witness line: "I let it take them." Kind: understanding.

## Co-op

Each reader's double is their own. A peer sees an empty branch, and the torches it sets across the wall are only those of the reader
it walks beside.

## Literature

There are no papers in it.

**The source:** words scratched into the lobby wall with a key or a coin: BRING YOUR OWN LIGHT, and under it, smaller, "it will
bring its own".

**The account:** "The account: the light", in the reader's own words:
- the figure with their walk;
- the torches set across from theirs, then early;
- the turn back halfway;
- their torches going out behind them, or the dark all at once if they followed;
- the far wall;
- the note from the pillow, pasted in, and what they make of it.

**Elsewhere:**
- the co-op line;
- the miniature (two matchboxes side by side with a playing card on edge down the middle, a spent match in one and a whole one in the other, both
  pointing the same way);
- the staircase prose ("the fork where my light went on without me");
- the co-author pool;
- the advancement under understanding.

**Limits held:**
- the reader's gender, colour and age are never named;
- lines are on the action bar, never in chat.

## A beat late (0.4.216)

The proposal's last cost, left out of 0.4.213, is now built: until the reader next sleeps, other readers' clients now and then show
them a beat behind their own body, the way the double moved in the hall.

**When:**
- It begins when the double goes home, whether the reader followed it or went on (`Late213`, set with `Home213`).
- It ends when the reader sleeps through a night. Getting into bed and out again before morning does not end it.

**What peers see** (`client/LateBodyClient`):
- Now and then (a spell of about four seconds, roughly every twenty-five, at a moment of its own for each reader), the reader's
  body is drawn where they were eight ticks before, turned the way they were turned.
- Each spell eases in and out.
- A teleport or a door is never drawn late: the body stays where it is.

**What does not change:**
- The reader's own client draws them as they are.
- Nothing moves on the server: positions, hits and collisions are the real ones.
- Nothing about it is said to anyone.

Every client is told who is late by `LateBodiesPayload`, sent when the set changes and at login (`OwnLightLate`). Protocol 115.

## Art and sound

`tools/draw_horror_art.py` adds two pieces to the shared `horror_fitting` block: the scratched words and the burnt stub.

The double reuses the lookout's double renderer, which draws the reader's own skin. It now walks with its legs and holds what it
carries.

The rumble, the footsteps and the slam are the dark halls' own sounds, so there are no new sounds.

## Tests

Five `OwnLightTests` in the literary namespace:
- dealt only in the dark band, with twin branches, gaps and the words on the wall;
- the double walks level a beat late, then early, and sets a torch across from the reader's;
- halfway it turns back, takes their torches down, goes out through the way in, and the far wall has them;
- following it back, every light goes and they come to at the fork;
- at home, the bed slept in, a chest turned round, the lights out, and a note on the pillow in the reader's own hand.
