# The Night Operator (0.4.252)

The owner's third puzzle proposal of 2 November (docs/PUZZLE_PROPOSALS_2026_11_02.md):

> An operator at an old telephone exchange asks you to help someone find their way out of another wing. You patch into different rooms
> and identify them through background sounds: running water, a refrigerator, someone walking on a bare floor. Opening the correct
> connection lets the caller continue. Their footsteps become progressively nearer. When they finally say they're outside, footsteps
> cross your exchange toward its door. Nobody is visible. The operator removes their headset: "That's us finished."

The release adds one puzzle room in a new family, LINE: the night operator (`NIGHT_OPERATOR`, slot 159, early, with no deep room). The
layout version is 81 (new builds only). The room follows `PuzzleRooms`' pacing:
- it is offered only after a story, and rarely;
- nothing counts down, hurts or is used up, and solved stays solved for everyone;
- no Witness credit.

There is no protocol, Witness or source-kind change. Code: `NightOperator`, `ExchangeBlock`.

## The switchroom

A small room at night:
- dark boards with a rug, a dark oak dado and plaster above;
- one hanging lamp;
- the shift notes pinned on a cork board, a radiator and a chair by the east wall;
- the far door in the north wall, iron, with no handle on this side.

**The board.** A cord switchboard runs along the west wall:
- nine jacks, each a black panel in an oak frame with a lamp cap over a brass socket;
- a field of small lamps above them;
- below them a shelf of plugs and keys.

At its near end, by the entry, is the incoming line's panel with a larger lamp, and the headset hook with one spare headset. The
operator sits on a stool at the board, in a headset, facing it.

## Listening and patching

**The jacks.** Each jack reaches a room somewhere in the House with something going on in it. Which jack reaches which room is fixed for
each world. The rooms are:
- water running;
- a fridge;
- someone walking on bare boards;
- a clock;
- rain on a window;
- a radio between stations;
- a kettle coming to the boil;
- nothing at all;
- and one of the nine is the exchange's own line: a switchboard, and someone breathing near it.

**Listening.** A hand on a jack listens to it. That reader alone hears the room for a few seconds, thin and close as a telephone makes
it, and the jack's lamp lights. The action bar names the line by its number.

**Patching.** A crouching hand on a jack puts the caller through to it. The cord's plug goes into that jack and comes out of any other.

## The call

**The operator.** When someone first comes in, the incoming line's lamp lights and the board buzzes. The operator explains, once,
over three lines shown over their head:
- "You'll be the relief. Good. I've someone on the line I can't place."
- "They're lost in the other wing. They'll tell you what they can hear behind the next door."
- "Hand on a jack and you'll hear that room. Crouch and press it to put them through."

After that they leave the board to the reader. Nobody can be put through until they have finished.

**The caller.** The caller speaks on the action bar, in quotation marks, as speech always does. They describe the door in front of
them, one at a time:
1. water running behind it, a tap, they think;
2. something humming behind it, like a fridge;
3. someone walking about behind it, on bare boards ("Put me through anyway");
4. only a clock behind it;
5. light under a door, and a switchboard behind it: the exchange's own line.

**The wrong room.** Put through to the wrong room, the caller says what they can hear in there, and "That's not the one." For a room
they have already come through, they say "I've been in there already." Either way they wait at the same door.

**The right room.** They say "Right. Going through." Then their footsteps come, four of them, from beyond the room's entry door. Each
time they stop nearer: twenty-two blocks away, then fifteen, nine and four. Then they describe the next door.

**The end.**
1. Put through to the exchange's own line, the caller says: "Thank you. I'm outside now. I can hear you."
2. Footsteps cross the switchroom from the entry door to the far door, past whoever is there. Nobody is.
3. The far door opens, and a moment later it shuts.
4. The operator takes off their headset and hangs it on the hook beside the spare. The incoming line's lamp goes out and the cord
   comes out of the board.
5. The operator says: "That's us finished."
6. The far door's latch is let go and it stands open. That is the room solved.

Built again afterwards, the room stays finished: the door open, two headsets on the hook, the operator at the board without theirs.

**The shift notes.** Each hand on the cork board reads the next of the notes:
- "SHIFT NOTES. Nights: one on the board at all times."
- "Callers from the other wing: put them through a room at a time. They know the way better than we do."
- "Don't put anyone through to a room you haven't listened to."
- "A line with nothing on it after midnight is not a fault. Leave it."
- "If they say they're outside, let them by."

## Assets

- **The board.** `tools/draw_exchange_fittings.py` (checked in CI) draws the `exchange_fitting` block:
  - the jack: dark, lit, or lit with a plug and its cord hanging;
  - the incoming line: dark or lit;
  - the lamp field;
  - the shelf of plugs and keys;
  - the headset hook, with one headset or two;
  - the cork board.

  The lit lamps give a little light.
- **The operator.** Two painted skins on the stranger phases 55 (`literary_night_operator`) and 56
  (`literary_night_operator_bare`): a cardigan, reading glasses and pinned-back hair, in a switchboard headset and without it. The
  headset is a new `headset` mark in tools/paint_character_skins.py.
- **The sounds.** `tools/generate_operator_audio.py` (checked in CI) synthesizes ten subtitled sounds, with no recordings: the nine
  lines, band-limited like a telephone line with its hiss and crackle, and the board's buzzer. The caller's footsteps are vanilla wood
  steps.

## Tests

`NightOperatorTests` (exploration), `theCallerIsPutThroughRoomByRoomAndCrossesTheExchangeUnseen`:
- the far door iron and shut;
- nine unlit jacks, the incoming line lit, one headset;
- each jack reaching its own room;
- nobody put through while the operator explains;
- listening lights a jack without putting anyone through;
- the wrong room leaves the caller where they are;
- the five right rooms in order bring them through, the cord in one jack at a time;
- footsteps cross the room and the far door opens, with nobody there but the operator, then shuts;
- solved, with the operator's headset on the hook, the door open and the line dark;
- the board quiet afterwards;
- built again, it stays finished, with the operator back at the board without the headset.
