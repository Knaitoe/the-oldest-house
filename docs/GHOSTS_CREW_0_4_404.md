# The Ghosts set for several readers (0.4.404)

The owner, after 0.4.403:

> "a head full of ghosts should allow multiple players too. only one version of the cast is rendered for the primary player - the rest see nothing."

## What was wrong

Each reader filmed a family of their own on the Barretts' set.

- The cast, the props and every beat of a day were that reader's.
- Their bodies were hidden from every other reader.
- Two readers on the set at once were in two different productions laid over the same house. Neither could see what the other was filming.

## What changes (`GhostsCrew`)

The set shoots one production at a time.

### The lead and the crew

- **The lead.** The first reader filming on the set leads. The family, Barry and Father Wanderly, the cardboard house and the desk drawer are theirs. The lead keeps the set while they stay on it.
- **The crew.** Every other reader filming there is a second camera on the lead's take.
  - They see the lead's cast. Their own cast stays out of sight while they crew, and nobody else sees it either.
  - They follow the lead's day, clock and beats. That covers the dinner, the pickup upstairs, the leap, the supper on the last day and the jump out after the sixth day.
  - They hear the take: its lines, Barry's calls, its sounds and particles. The lines go into their own record of what was said.
  - They can talk to the lead's cast. Asking Barry to roll rolls the lead's take.

### What each camera keeps

- A second camera's own lens holds its own shots.
  - What it kept in frame goes into that reader's own camera log.
  - Who it kept in the middle of the frame shapes that reader's own cut.
  - Its own stills go to the monitor.
- On the sixth day, a second camera's held shot of Marjorie mid-leap is taken from where that camera stands.

### Days, endings and coming and going

- **Days filmed together.** A day filmed to the end counts as filmed for every camera on it.
- **The last day.** A reader who is on the set when the last day ends has seen the filming through, whichever days they missed. They still leave their account in the booth.
- **A lead who walks out.** The longest-present second camera takes over the same bodies, where they stand, and the take goes on.
  - The bodies are traded, not replaced, so nobody's cast is lost.
  - If the lead walked out of an unfinished take, they start from their first unfilmed day when they come back.
  - If the day was already done, they go on to their next day as usual.
  - A lead who has finished the filming keeps their own cast. The next lead's family is staged afresh.
- **A second camera who walks out.** When they come back alone, they start from their first unfilmed day.
  - From then on, a reader who has been a second camera goes on to their first unfilmed day after each day.
  - A reader who never crewed goes on to the next day as before.
- **Coming onto a set already rolling.** The reader is told who is filming and which day it is, and Barry asks them to stay out of the shot.

### The account

A reader who was ever a second camera has this line in their account: "Some of those days another camera was rolling beside mine. What follows is only what my lens held."

## Under the hood

- **Who leads.** `GhostsCrew.refresh` runs once per scene pass, before the readers' own ticks.
  - It picks the lead by when each reader came onto the set (`OnSet0404`).
  - It keeps the lead in the place's shared state (`CrewLead0404`).
- **Following the take.** A second camera's reader tick copies the lead's production keys (`GhostsCrew.follow`) and draws their own view.
- **Relays.** During the lead's tick, `GhostsSet.say`, `sound`, `particles`, `heard` and `capture` reach every second camera.
  - The errands, the hidden plate and the lights going out on the last day reach them too.
- **Who sees which cast.** The view payload carries `GhostCast`, the reader whose cast this reader sees.
  - On the server: `LiteraryActor.broadcastToPlayer` and `LiteraryVignettes.talk`.
  - On the client: `GhostsTakeClient.cast` and `sees`, used by the cast renderer, the pick rule and the frozen-frame camera.

## Tests

New Ghosts case `GhostsSetTests.aSecondReaderOnTheSetIsASecondCameraOnTheLeadsTakeAndSeesTheLeadsCast`. It checks:

- who leads;
- that a second camera calls the lead's take;
- that the lead's cast is seen by both readers, and the second camera's own cast by neither;
- that the second camera follows the lead's day and clock, holds its own shots and hears the lines;
- that the day counts as filmed for both;
- that the next camera takes over the same bodies, unmoved, when the lead walks out.

Four earlier cases were written for the old rule that each reader films a private family. They now choose the lead (`GhostsSetTests.lead`) and check the shared take; everything else they checked stays:

- the whole-family case: one cast, the lead's, tracked by both readers, and no second body staged;
- the held jump: the second camera is held on the lead's day six and has it filmed;
- the poisoning: the second camera is on the lead's last day;
- the sisters' dialogue: the friend sees the same Merry, and the dialogue cursor stays personal.

The live two-client day-six proof now has A lead and B as a second camera, and both lenses hold on the one Marjorie.

The literary job counts its cases from the source.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
- **New builds only.**
