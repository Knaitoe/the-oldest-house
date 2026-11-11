# Bike night 10: Gus (0.4.476)

The tenth slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). What the proposal says of him:

> **Alone,** a reader has one friend along, **Gus**, a character on his own bike, who talks, rides the seesaw with them and
> keeps up in the chase.

> **Seesaw.** It takes two: two readers, or one and Gus.

> **The tone.** Gus talks the whole time about nothing. The readers are left alone long enough to play.

This is Gus at the playground. His bike and the chase come with the chase; the cap comes off with the bullies.

## Who he is

A boy the readers' size, in a windbreaker, glasses and a ball cap with a C on it. The sheet by the door is his plan for the
night. He is painted by the shared painter (`tools/paint_character_skins.py`), twice: in his cap, and without it for when it
has been taken.

The owner, 11 November, on being told the plan for the rest of the night: "if you don't have gus designed as a child, i'd
probably assume him/the rest of the cast of playground kids and bullies as such." He is one: the readers' size, with a
child's face. The bullies, when they come, are children too: older kids, a little bigger than the readers.

## One thing I read differently from the proposal

The proposal has him along when a reader is alone. I have put him in every night, one of him, whoever comes:

- a night's readers do not all arrive at once, so "alone" cannot be decided once and kept;
- the plan by the door is his, and a friend who wrote the plan and then is not there is a loose end;
- his is the cap the bullies are to take.

What "alone" still decides is what he does: the seesaw and the swings below. If he should be only a lone reader's friend
after all, it is one condition in `BikeNightGus`.

## What he does

- **He waits** on the sand by the side path. When somebody first comes near he says hello, with a wave, to each as they come.
- **He talks about nothing.** A line every little while (eight to nineteen seconds after the last has gone), twenty of them,
  in a different order each night, never over a line of his own that is still up. Only while somebody is near enough to hear.
- **He tells the story, badly.** After a few lines he starts on why nobody cuts through the graveyard on Halloween, in four
  parts, and does not get to the end of it ("my brother tells it better"). It is never told properly. A reader near enough
  to hear the last part has it in their account.
- **He says something about what he sees**, once each in a night unless it happens again: the bike a reader took (each of
  the six its own line, and the Rattler, which he said was his); a landing off the swings that is the furthest tonight; a
  swing going past its widest, and over the bar; the slide; the bars crossed, and a rung missed; somebody thrown off the
  seesaw or the merry-go-round; the top of the dome; the horse; the fountain; the payphone; the scratches on the post, and a
  reader's own under them; and a ball of sand that meets him (three lines, and then he has said all he has to say about
  it). It waits until the reader is back in earshot.
- **He keeps near whoever is playing on the sand**: when they get more than seven and a half blocks from him he comes
  over and stops three and a half short. Beside a swing, never in front of it. With nobody playing he goes back to his
  place.
- **The seesaw.** A reader on one end with nobody on the other for two seconds gets him on the other. He pushes off the
  sand when his end comes down: seven times in ten as it lands, which is a bounce, and otherwise a moment after. Left on it
  alone for two and a half seconds, he gets off. With two readers on it, it is theirs. He is never thrown off it; a reader
  he bounces hard enough is.
- **The swings.** A reader on a swing with nobody else in the park gets him behind it (round the end of the frame, not
  through where the swings swing), pushing as it starts away from him, as a reader's push does (0.4.474). Most of his
  pushes meet the swing as it turns, which makes each a kick (0.4.475): with Gus at the back a lone reader has only the
  front end of the arc to time to go over the bar. One push in seven comes too late to be a kick. With another reader in
  the park the pushing is theirs to do.
- **A hand on him** gets an answer: to pick a bike, if the reader has not; then six things in turn. Not while he is in the
  middle of saying something.
- **He saw.** A reader who goes over the bar with no other reader near has Gus for a witness, and their account says so.

Everything he says is a caption over his head, by the one rule for how long words stay up (0.4.473), and never chat.

## The records

`GusMet`; `GusStory`; a seesaw ridden with him has him in `SeesawWith`; a swing he pushed has him in `SwingPushedBy`;
`OverSeenBy` when he was the one who saw. The account: "Gus was there, in his cap, talking the whole time about nothing. He
told me why nobody cuts through the graveyard on Halloween. He told it badly."; "I went up and down on the seesaw with Gus
on the other end."; "I sat on one of the swings, and Gus pushed me."; "… Gus saw."

## Where it lives

- `labyrinth/BikeNightGus`: his body, what he is doing, what he says and when. Stranger phases 166 (in his cap) and 167
  (without).
- `BikeNightTexts`: every line of his (`gusLines()` is all of them).
- `SeesawSeat.sit(LiteraryActor, …)`, and a push off the sand by whoever is on a seat.
- `LiteraryRenderers`: his six gestures (a wave, pointing, both arms up, hands out to push, hands on the seesaw's handle, a
  shrug).
- `BikeNightGus.keptHome` is for tests: the playground's own cases have it to themselves.

## Something to know

Drisking already has a Gus: a grown man in Borrasca's town (stranger phase 152). Calder Falls's Gus is this one. They never
meet, but they share a name; either can be renamed.

## Not built here, and owed

- **The Wasp on the slide.** The proposal: "Riding down it on the Wasp is possible, and Gus is impressed." The slide
  (0.4.466) carries a seated reader and nothing else; a bike cannot get onto it. That was missed when the slide was built.
  It is the next thing, with Gus's line for it. (Done in 0.4.477, `docs/BIKE_NIGHT_SLIDE_BIKE_0_4_477.md`: a bike could
  already do it; the game now notices.)
- His bike, his riding, and the cap: with the bullies and the chase.

## Tests

`BikeNightTests`, three more cases (32 in the namespace):

- **Gus is at the playground, talking the whole time about nothing.** How he talks (every line short enough to read, none
  unkind, none saying how the reader feels); one of him, the readers' size, at his place; nothing said with nobody near;
  hello, by name and with a wave, and nothing of it sent as a message; a few lines about nothing, no two the same; the story
  in its four parts in order; on about nothing; never a line over one still up; a hand on him, with and without a bike, and
  not while he is speaking; the bike a reader took; a ball of sand; he comes over to a reader playing and stops short; he
  goes home when nobody is; the account; the night over and the next night's Gus.
- **Gus takes the other end of the seesaw for a reader on their own.** He comes over and gets on; the board has his weight;
  it goes over and back with each pushing off; the record and the account; left on it, he gets off beside his end; with two
  readers on it he leaves it to them.
- **Gus pushes a reader who is on a swing alone, and helps them over the bar.** He goes round the frame and not through
  it; his pushes take the swing past its gentle arc; the reader is told who and has it in their record; with him at the
  back, a reader timing only the front end goes over; he says so; the account names him as who saw; with another reader in
  the park he leaves the pushing to them.

The playground's older cases keep Gus at his place (`keptHome`), so that a lone reader on the seesaw or a swing is still
alone on it there.

## What the local runs found

- **He stopped half way to a reader.** He thinks again about where to stand every second and a half; by the second
  thought he was within the distance he lets a reader get to, and so stood where he was, six and a half blocks short. Once
  he has set off after somebody he now keeps on until he is beside them. His own case found it.
- Three things were my cases' and not the game's: a case's reader is made the park's size a few ticks after arriving, and
  cannot sit on anything in the tick they arrive; and Gus says hello before he says how the seesaw is done, so the case
  waits for the second line.

## What I have not seen

- **Him, in the game.** His sheets I have looked at enlarged (the cap, the glasses, the windbreaker), and on the character
  proof's page; not him standing on the sand, walking, on the seesaw or with his hands out.
- On that proof page the C on his cap reads backwards. The page draws every figure mirrored (the one-armed girl from the
  house on the corner has her arm on the wrong side there too), so in the game it reads the right way round, as it does on
  the sheet. The page's mirroring is older than this and is not put right here.
- **How much he talks.** The gaps were chosen. Whether a line every ten to twenty seconds is company or too much is the
  owner's to say; it is two numbers (`BETWEEN`, `BETWEEN_MORE`).
- His walking round the frame, by eye.

## Numbers

No layout, protocol or Witness change. Two new skins (stranger phases 166 and 167). New builds only.
