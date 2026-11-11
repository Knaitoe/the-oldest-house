# Bike night 6: the seesaw (0.4.468)

The sixth slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). The owner's note:

> Lots of fun potential for unique interaction here - a seesaw, slides, swings, monkey bars, etc

The fourth piece of equipment on the playground, and the first that is for two. The merry-go-round, the spring rider and the
dome come next, then Gus.

**It still cannot be finished, so the House does not deal it.** Nothing is a Witness source.

## The seesaw

It stands on the east side of the playground's sand, its board running north and south.

- **The stand.** Two posts on feet with an axle between them. It stops a body.
- **The board.** Nearly five blocks long, with a seat and a handle at each end and half a tyre under each. It stands with one
  end down on its tyre and the other up over a child's head. It really tips: it is drawn where it is, at whatever angle it is
  at, and a rider on it is carried up and down with their end.
- **Sitting.** A hand on an end of the board sits on it, whichever way it is tipped. Each end takes one reader. A hand on an end
  somebody is on is told so.
- **Weight.** A rider's weight brings their end down. Two riders weigh the same.
- **Pushing off.** A rider whose end is down pushes off the sand with jump. One who is up in the air has nothing to push off.
- **Alone,** a push takes a rider about half way up, and their weight brings them back down with a bump ("Seesaw bumps down").
- **Two,** a push carries the board all the way over: the one who pushed is up in the air and the other is down, and it is their
  turn. The axle is heard at each push ("Seesaw creaks").
- **Getting off** puts a reader on the sand beside their end. If the other rider was up in the air, their own weight brings them
  down, hard. Nobody is hurt: the sand is soft.
- Left alone, it stays as it was left.
- A reader's record has that they sat on it, how many times they pushed off, and who was on the other end when they did. Their
  account tells of it, and names who they rode with.

**The board stops nobody.** It moves, and the game has no moving thing a body can walk into; a reader walks through it. The
stand in the middle is solid.

### How it is made

The seesaw is its own block (`SeesawBlock`, `calder_seesaw`): the stand, and a place at each end that draws nothing, stops
nobody and is what a hand finds. The stand keeps the board (`SeesawBlock.Board`, the block's entity): how far over it is and how
fast it is going. Each tick a rider's weight turns it their way, it keeps most of its speed, and it stops on a bumper; under a
lone rider it bounces a little if it came down hard. The server and each client step it by the same rule, and the server sends
where it is whenever something happens to it (a push, a bump, a rider coming or going) and a few times a second while it moves.

The board is drawn by the stand's renderer (`SeesawRenderer`), turned about the axle: three models that are never blocks in the
world, the middle of the board and an end each way.

A rider sits on an unseen seat (`SeesawSeat`). The board puts each seat where its end is, on the client and the server alike,
after it has moved; so a rider is drawn where the board is and not where it was a tick ago.

Jump reaches the server by the message the swings already use for letting go. Which it means is the server's to say, from what
the reader is sitting on. No protocol change.

## How it is drawn

`tools/draw_calder_play_art.py` draws three more models and one more picture (the board's top and underside), and the seesaw's
block states. The board's limit is worked out from the same numbers in the game as in the tool: the axle's height, and how far
out and how far under the board a bumper is.

Two more sounds from `tools/generate_bike_night_audio.py`.

## Tests

`BikeNightTests`, one more case (twenty-two in all), building the place itself in a batch of its own.

- **The seesaw goes over when whoever is down pushes off, and one alone comes back down.** The stand and its board; the two
  ends' places, which a hand finds and which stop nobody; that it stands with its first end down, its bumper on the sand, the low
  seat one a child sits down onto and the high one over their head. A hand on the low end sits on it; the seat is where the
  board's end is, and the reader on it. Nobody pushes who is not on it. Alone: a push takes the reader up, in the air there is
  nothing to push off, they come back down, it never goes over, the axle and the bumper are heard, and they are told once how it
  is done. A second reader takes the high end and a third is told it is taken. Two on it: it stays as it was; the one in the air
  cannot push; the one who is down pushes and it goes all the way over; the second pushes in their turn and it comes back. The
  one who is down gets off, is put on the sand beside their end, and the one in the air comes down with a bump. Their records say
  who pushed how often and with whom. The third reader has it alone. Left alone it stays as it was left, with no seat left on it.
  The three accounts say what each did.

The case for what a hand on the park's things says tries the seesaw's stand.

The client proof has an eleventh page: the seesaw as it stands, one end down, and another caught level, half way over, both
drawn by the stand's renderer. I looked at it.

I looked at the seesaw in the park in seven pictures made with the local aid that is not part of the build (which I taught to
draw the board as the stand's renderer does, as it stands): from each side of it, along it from beyond each end, along the board
from over the low seat, from the swings and from the monkey bars. The low end's tyre is on the sand, the high end is over a
child's head, and the seats and handles lean with the board. Nothing needed changing.

### What the local runs found

- **A reader's seat was counted before anybody had sat.** The board keeps the game time each end's seat last said it was there,
  and I had started that at the least number there is; the time since then overflowed, and both ends read as ridden on a seesaw
  nobody had touched. The case's first look at the board caught it. It starts at a time before any game time now.
- **The bumper stopped a hair over the sand.** I had worked the board's limit out as if the bumper hung straight down from the
  board however far over it was. It is worked out exactly now, and the case checks that the bumper's bottom is on the sand.
- **An end is free a moment after its rider has gone, not at once.** The case sat a third reader on an end in the tick the first
  left it, and was refused. That is as it should be (the seat is gone on its next tick); the case waits for the end to be free.

## What I have not seen

- **The board moving.** The proof's page is two still pictures of it. Nobody has watched it tip. Whether the speeds are right
  (a little under a second from one end to the other between two riders), I do not know.
- **A rider on it.** Where a reader sits is worked out from the game's own numbers for a sitting body. Nobody has looked at one
  on the seat, or at two facing each other.
- **A client stepping it.** The server and a client run the same rule and the server corrects the client. That has not been run
  with a real client, and whether a rider's view judders when the server's word arrives, I do not know.
- **The jump key.** The client's sender has one more condition. The case calls what the server does when the message arrives.
- **Its sounds, and the night.** As for the rest of the park.

## Numbers

No layout, protocol or Witness change. New builds only.
