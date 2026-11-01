# The Dead Air Broadcast (0.4.206)

The owner's second proposal of 31 October (docs/OWNER_VIGNETTE_PROPOSALS_2026_10_31.md, section 2), built after the Left-Right
Game. A shortwave receiver in a 1983 basement tunes the hallway beyond the bulkhead. What the dial is set to is what the
hallway is, and in two of those things a tall puppet made of sacks is waiting.

Slot 132, `LabyrinthPlace.DEAD_AIR`, a multi-visit literary scene with four visits (three in the morning on the scene clock).
Layout 62, protocol 113. Witness: a new understanding source, **61 sources / 46 required / two kinds / three endings**.

## The place

`DeadAirSet` builds it whole (the polish, review and playtest passes skip it):

- **The basement.** Shag carpet, walnut paneling, a cold faux-brick hearth, a plaid couch and four ceiling lamps. On a
  folding table stand the receiver and the forum printouts (the source). The Zenith set shows snow. Percy, a marionette boy
  with cut strings and a brass marquee across his chest, sits on a crate and turns his head to whoever is nearest. In the
  north wall is a steel bulkhead with a wheel.
- **The hallway.** Five wide and twelve long beyond the bulkhead. It is retuned whole by `DeadAirSet.tune`; the cells a
  reader stands in are never filled.
- **The stage's end room.** Off the hallway's east side when it is tuned to the stage: rotting velvet, painted cardboard
  waves, a spotlight, a wall-breaker and the chest.
- **The salt marsh relay.** Past the hallway's far end when it is a drain: brackish pools, mud, cordgrass, spruce
  boardwalks, three lattice masts (Alpha, Beta, Gamma) with a dish at each foot, a breaker on a pole beside each, five signal
  bulbs at each top, and the transmitter shack with an iron door.
- **The studio.** The basement is a set standing in a black studio with light rigs and braces. There is a teleprompter, a
  breaker, and a stair up the east side to a back curtain.
- **The booth.** Over the hallway, behind the curtain: nine monitors in a three-by-three bank, a rusted highchair turned to
  face them with cut strings hanging over it, and a rail looking out across the marsh. The centre monitor is the account.

One fitting block, `dead_air_fitting` (26 kinds, eight stages), and eight materials (walnut, shag, faux brick, studio black,
velvet, plywood, drain concrete, mud) are drawn by `tools/draw_dead_air_art.py`. Seven subtitled sounds come from
`tools/generate_dead_air_audio.py`: static, the dial, dead air, feedback, Percy's clack, the tall one's creak and the breaker.
`DeadAirClient` (from `DeadAirPayload`) tints the screen with the channel's colour and draws the cyan of dead air, with the
static creeping back in for its last two seconds.

## The dial

The receiver steps through what it can reach:

- **Static.** The hallway is paneled and its far wall shut, and the bulkhead's wheel will not turn.
- **088.1, the stage.** Magenta light. The hallway is a marionette theatre's corridor of velvet and plywood, with strings
  overhead and waves along the walls, and its east side opens into the end room.
- **074.5, the marsh.** Green light. The hallway is a flooded storm drain with water in its gutters, and its far end is open
  onto the marsh. The dial only reaches the marsh once someone in the set has fitted the tube.

The dial is one for everyone in the set. It will not leave a channel while anyone is still out in what that channel made
(in the end room, out on the marsh, or at the drain's far end), so nobody is left inside a wall.

## The tall one and dead air

The Skin-Taker is a burlap figure sewn up with twine, in a top hat. It stands in the hallway on the stage and walks the
boardwalks on the marsh. Every four ticks it steps toward the nearest reader in its part of the set. In the hallway its arms
reach from wall to wall at chest height. Its touch puts that reader out of the set, to come back and do the visit again.

**Dead air.** A wall-breaker (in the hallway, in the end room, in the studio) or a breaker on its pole kills the air.
Everything goes the blue of an empty screen and the Skin-Taker stops where it is, with a body that can be stood on and arms
that are solid. In the hallway a reader goes under its arms on hands and knees. After eight seconds a howl of feedback snaps
the set back to what it was tuned to, and the tall one moves again.

## The four visits

1. **The tube.** Tune to the stage, kill the air, crawl under its arms, and open the chest at the stage's end for a brass
   amplifier tube wrapped in a playbill. Fitting it into the receiver finishes the visit.
2. **The marsh.** Tune to 074.5 and go out through the drain. Percy's second printout has the night man's bearings: Alpha
   due east (90), Beta 225, Gamma 315. Each dish turns an eighth at a time. While a dish is on its bearing its mast's bulbs
   step up to a cyan lock, and off it they go dark. The breakers on their poles hold the tall one still while the dishes are
   turned. Three cyan locks send something out on channel four and open the shack, which finishes the visit.
3. **The stage.** The basement's side walls are down to flats on braces, with the black studio round them, and the tall one
   waits in the studio. The teleprompter has the reader's own actions written as stage directions: the dial, the
   wall-breaker, the crawl, the tube, the dishes, the breakers, and how many times they were touched. Reading it finishes
   the visit.
4. **The booth.** The back curtain is open. Up the stair there is nobody: a highchair facing nine monitors of snow. The
   centre monitor clears to Percy's marquee, YOU ARE ON THE AIR, LITTLE PIRATE, readies the account (`on_the_air`) and gives
   the reward. The other monitors go to snow, then black.

## Voices and the account

There are three hands, all original:

- **The forum printouts** (2001, NetNostalgia, "little pirate theatre?? ch 4, early 80s"): people trying to remember a
  children's programme nobody else remembers. The first is the source on the table. Percy has the second and third in his lap
  on the first two visits: the relay on the salt marsh, and the night man's bearings and the highchair.
- **Percy's marquee**, which scrolls when he is touched, says what to do next, in capitals.
- **The teleprompter**, whose script is the reader's own visits as stage directions.

The account, read at the centre monitor, is the last page of the script: the booth, the highchair, the marquee, END OF
BROADCAST, and in the margin, in pencil, in the reader's own hand: there was never anybody in the booth.

**Witness line:** "I tuned the set to a channel that was never on the air and walked into the show. At the top of the stair
there was nobody in the booth, only a highchair facing nine screens of snow."

**Reward: the dead-air breaker** (`dead_air_breaker`), owner-bound. Thrown, it is dead air for its owner alone: for eight
seconds nothing in the House that hunts by sight can find them (the same check the Work print costume uses), then two minutes
before it throws again.

## Several readers

The set, the dial, the air, the tall one, the masts and the shack are shared, one for everyone in the scene. Each reader's
part is their own: the tube is fitted per reader (though anyone's fitted tube lets the dial reach the marsh), and so are the
dishes they turned, the crawl, the script, the visits and the account. Dead air stops the tall one for everyone. Its touch
puts out only the one it touches. The set goes back to rest after five empty seconds.

Co-op lines (CoopAccounts), a co-author line, a miniature note, the House of Leaves place name ("the basement studio") and an
advancement under understanding are added.

## Tests

Six `DeadAirTests` in the literary namespace:

- the printouts, and the dial starting at static with the bulkhead locked;
- dead air stopping the tall one, the crawl under its arms, the tube from the chest, and the feedback after eight seconds;
- the tall one's touch putting the reader out with the visit still to do;
- the three dishes locking the masts and opening the shack;
- the walls down and the teleprompter holding the reader's own lines;
- the centre monitor putting the reader on the air, and the script resolving it.

The architecture case exports the set (the source's discovery surface is the printouts) and checks its reachability. The
Witness counts are 61 / 46.

The first local run found the receiver retuning the hallway without saving the dial's new channel, so the tall one was
never placed and the marsh never reached. It now saves the set after every retune.
