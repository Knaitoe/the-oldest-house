# Bike night: over the bar (0.4.475)

The owner, 11 November:

> for the bike night scene, it would be cute if there was a way for the player to travel all the way around the swing. it
> should be slightly difficult to manage - i'm not sure what the best way to hide/enable it would be

The owner left how to hide it and how to do it open. This is what I chose; any of it can change.

## How it is hidden

Nothing in the game says how it is done, and no hint is given on the swing. The park has one word on it, and it is a rumour:
a hand on the swings' frame finds, scratched into a post,

> T.R. WENT OVER THE BAR

and under that, in other scratches,

> DID NOT

## How it is done

By rhythm, with the swing already as high as it goes.

- **A kick.** Leaning with a swing takes it to its widest arc, however sloppily (0.4.465). A *kick* is the lean thrown the
  other way (or taken up) exactly as the swing turns at an end of its arc: within three ticks of the turn, either side, which
  is a window of about a third of a second. A kick forward belongs to the back end of the arc, a kick backward to the front.
- **Each kick in time takes the arc a step past its widest**, and the chains ring a little higher each time. The arc takes
  the step up as the swing comes down, so the seat never jumps. After two the swing is going past level at each end.
- **Every end must get its kick.** The first end of the arc that goes by without one loses everything the kicks had gained,
  and the arc sinks back to its usual widest. So it is five in a row, about six and a half seconds of keeping exact time.
- **The fifth sends it over.** The next time the swing comes through the bottom it keeps going: up the far side, over the
  top (slowest there, at two fifths of its speed at the bottom), down behind, and through the bottom again, in a little under
  two seconds. Then it is a swing again, at the arc it went over from.
- **Again.** Kicks kept up send it over again from the other end, the other way round. Kicks let go, it sinks back.
- **With a friend.** A push that meets the swing as it turns (0.4.474) is a kick too. So somebody behind the swing can take
  the back end of the arc, and the rider has only the front end to time. Hands at one end alone take it a step past its
  widest for a moment and never over: the other end still needs its kick.
- **Only the park's swings.** The rope swings (the porch's, Drisking's yard) never go over.

"Slightly difficult" is the window (three ticks either side) and the count (five). Both are single numbers
(`HouseReadSwing.KICK`, `OVER_KICKS`).

## What happens round it

- **Told once.** The first time in a night: "All the way over the bar." Anybody within thirty-two blocks is told who.
- **The post.** A reader who has been over, putting a hand on the frame again, picks up a stone and scratches under DID NOT:
  DID TOO. Once. After that it is there for them whenever they come back, and for anybody else for the rest of that night
  ("in fresh scratches").
- **The account.** "I went all the way over the bar on one of the swings. Nobody saw." Or who saw; "and then did it again";
  and that they scratched it into the post. The one who saw has it too.
- **Letting go.** Jumping off a swing kicked past its widest arc, or one going over the bar, throws nobody harder than the
  widest arc would (otherwise it would throw a child over the railings). A turn somebody lets go of does not count.
- **Nobody is hurt.** A reader coming down off any swing is unhurt wherever they land, not only on the sand: the top of the
  turn is seven and a half blocks up.
- **Hands.** Nobody's hands reach a swing while it is going over.
- **The rider's own view is not turned.** They are carried round the circle looking the way they were looking. As others
  see them they go over with the seat, as they already tilt with it on a high arc.

## How it works

- The swing's place is still worked out from the time it began, on the client and the server alike, with two more synced
  numbers on the seat: which pass through the bottom begins a turn (`SWING_TURN`, counted in half swings; an even one is going
  forward), and how long the swing has spent in turns that are over (`SWING_LAG`). During a turn the angle is
  `HouseReadSwing.over(part)`, a whole turn that leaves the bottom at the speed of the arc it was sent from; the to-and-fro's
  own clock (`SeatEntity.swung`) stands still for it, and goes on from the bottom afterwards.
- `SeatEntity.kick` is the rule; `pumping` loses the kicks at an end that goes by without one, and tells the park when a turn
  is done (`BikeNight.wentOver`).
- No new art and no new sound: the seat and chains are drawn turned about the bar as they always are, and the chains' own
  sound is pitched up for a kick and down for the top of the turn.

## Tests

`BikeNightTests`, one more case and two changed (29 in the namespace):

- **A swing kicked exactly as it turns, end after end, goes all the way over the bar.** The turn is a whole turn, leaves the
  bottom at the right speed and is slowest over the top; the post's words; leaning late, end after end, never passes the
  widest arc; five kicks in time step the arc up and send the seat over the bar and all the way round; it comes down a swing
  again; with the lean let go the arc sinks back; both records and what both are told; two kicks and then a missed end loses
  it; a rider timing only the front end with somebody pushing at the back goes over; letting go mid-turn throws no harder
  than the widest arc and does not count; nobody is hurt coming down; the scratch added once and found after by both; the
  accounts.
- The pumped-swing case's rider no longer leans to the tick (which would now take them over): they let the lean go for a few
  ticks round each turn, and still reach the widest arc and no more.
- The push case holds that hands at one end take the swing a step past its widest at most and never over.

## What I have not seen

- **Whether it is the right amount of difficult.** The window and the count were chosen, not played. A bot gets it every
  time; I do not know how often a person does without meaning to, or how long it takes one who is trying.
- **The ride itself**: being carried over the top with the view not turned.
- Two real clients.
- What I did look at is one new page of the client proof: the frame from the side with a swing at its widest arc, one
  kicked past it, and one over the top.

## Numbers

No layout, protocol or Witness change. Two more pieces of a seat's synced data. New builds only.
