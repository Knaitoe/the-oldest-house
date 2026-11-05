# Johnny Truant's service halls (0.4.307)

The owner's "Johnny Truant · Encounter Pairs" asks for three places in each part of the labyrinth where a reader can be glad to see
him and later dread him using the same object. The early domestic pairs are already built: bread, thermos and held door
(0.4.264-0.4.301). This release adds the **middle stone and service halls'** three. The deep "missing sentence" pair and the three
outside pairs (toolshed, barn lantern, stock gate) are still to come.

## Dealing

- `TRUANT_CHALK` (slot 183), `TRUANT_KNOCK` (184) and `TRUANT_LIFT` (185) join `TruantPlaces.PLACES`, so they share the one Johnny, his record, the five-crossing weighting and the short gap between his halls.
- `TruantPlaces.dealt(depth)` offers them only from the stone halls (`STONE_DEPTH`). The domestic three are offered from the story depth, as before.
- Layout 94. `LabyrinthPlaces.MAX_COLUMNS` goes to 24 because slot 185 needs a twenty-fourth column at the lowest manor heights. Earlier slots never move.

## The turns

`Truant.Turn` gains ARROW, KNOCK and BRAKE, each place's hostile turn. They are chosen exactly as his others (`Truant.choose`):
- only for a reader past twenty passes with a history with him;
- never while anyone under twenty passes is in the place (the turn becomes a claim);
- each harm recorded (`Harm`, `HarmAt` and a count), so later meetings can answer for it, and he can still help afterwards.

## The chalk arrow

A service junction:
- a corridor north;
- a cross passage;
- the west branch: the pipes run down it, an iron gutter drains into it, and it steps down a yard in;
- the east branch: dry, no pipes, narrowing to one wide and two high, then the floor ends over a sump (a four-block drop, with a ladder and a nook back to the passage).

**Help:** as a reader comes to the junction he chalks an arrow on the north wall toward the west branch: "That way. Where the pipes go. The other one drops." `HELP_CHALK`.

**Turned (ARROW):** he rubs it out and draws it toward the drop: "Not that way, not tonight. This way. Quick." The pipes, the gutter and the step still give the true way away. A fall into the sump is kept against him (`Arrowed`).

## The pipe knock

A corridor north, a blind elbow west, a branch north to the way on. A cast-iron steam main runs the whole way and repeats an eight-second cycle:
- 1.5 s shivering: the whole pipe blurs, rattles and smokes, visible all the way back up the corridor;
- 1.5 s venting across the corner: steam that scalds anyone in it, 1.5 hearts every half second;
- then quiet.

**Help:** from round the corner, out of sight, he knocks three on the main just after it goes quiet. A reader can knock back on the pipe and he answers when it is clear. `HELP_KNOCK`.

**Turned (KNOCK):** the same three knocks just as the shiver starts. A scald in that meeting is kept against him (`Scalded`). A reader who waits at the elbow and watches one cycle sees the lie.

## The lift brake

A stair climbs from the door to a landing eight blocks up. A three-by-three shaft drops to the passage on, beside a ledge cut into the shaft wall at the sill.

**Help:** he holds the brake. Once everyone in the place is aboard he says "Everyone in? Going down." and lets the car down a block at a time, carrying them. Empty, it comes back up for whoever is on the landing. `HELP_BRAKE`.

**Turned (BRAKE):** as the one he wants crosses the sill he lets go: "Mind the gap."
- the car lurches (half a second, enough to step back or onto the ledge), then drops;
- whoever is aboard rides it down unhurt;
- the one at the sill is not carried and falls after it, which is kept against him (`Dropped`);
- the brake on the car itself, thrown by anyone aboard, stops it where it is.

## Assets

- `tools/draw_truant_service_fittings.py` draws the `truant_fitting` block in vanilla's manner: chalk arrow (bare, left, right with a smudge), steam main (still, shivering, venting), landing brake and car brake (off, on), car platform.
- `tools/generate_truant_service_audio.py` makes six subtitled cues: chalk, knock, rattle, hiss, brake, lurch.
- Both are checked in CI.

## Tests

`TruantServiceTests` (exploration):
- the arrow drawn for help, kept as a claim with an early reader present, turned when alone, and the sump fall recorded;
- the knock in the quiet, the shiver on the corridor's pipe, the scald as the hall's own in a helpful meeting, and as his when turned;
- the lift held and let down with two readers, returning for the landing, let go at the sill with the friend riding it, the target's fall recorded, and the car's brake stopping a second drop.

Test players do not simulate their own gravity, so the target's fall down the empty shaft is applied as vanilla fall damage. All 105 exploration cases pass locally.
