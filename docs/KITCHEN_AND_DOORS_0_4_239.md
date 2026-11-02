# Doors that tell the truth, and a kitchen to come back to (0.4.239)

The owner, 2 November, four priorities. This release is the first three; the fourth (looking at the pool and the flat in play, with
their people and ordinary light) is 0.4.240. No layout, protocol, Witness or source-kind change.

## A room sounds as it is now

Sound through a closed door (0.4.229) was chosen by kind of room: every clock room ticked, every room with people in it talked. Now each
room is heard as it stands for the listener, from their own copy of a private scene or the one room everyone shares. Ordinary sounds can
be trusted, so that the ones that cannot be are felt.

- **Vacant Possession.** With nobody in it, the flat is as it was before moving day: someone in there, moving about. While it is being
  emptied the sound is fainter with every piece that goes down to the van. Once the last chair has gone, nothing: the hollow sound of
  a room with nothing moving in it. Put back, someone is still in there.
- **The last swimmer.** Someone swimming lengths until the cover is on; then nothing moving.
- **The familiar kitchen.** Its clock; or, while somebody else is in it, someone in there, moving about.
- **The séance house.** People talking until the family is driven out; empty after that.
- **The masque.** The clock in the seventh room, until this reader stops it; then nothing, for them alone.
- **Mr. Harrigan's study.** Its clock; on the last visit, when it stands as the funeral home, people talking low.

Rooms with nothing of their own to change sound as before. Two new sounds, synthesized like the others
(`tools/generate_door_sound_audio.py`): **someone moving about behind the door** (two steps, a chair drawn back, a cup set down) and
**nothing moving behind the door** (a low hollow room tone), both subtitled.

## A wedge does not shut the kitchen

A wedge in a room's entry keeps that room on the hall it was set from, and nobody comes in by that door from anywhere else while its
owner is in the game (0.4.231). That stays true of halls and puzzle rooms. The familiar kitchen is where readers find each other, so it
now lets them in another way: a reader whose way in is the wedged door, from another hall, comes in by the kitchen's side door instead
(the far door, if the side door is wedged too). The wedge still holds the entry on its hall, and that hall still leads there for its
owner. The side door is that reader's way back while they are in the kitchen: a short stretch of the hall they came from lies behind
it, and walking back out through it takes them there. A knock from their hall is heard at the side door, where they would come in.

## The kitchen in use

Everything is ordinary, shared and kept.

- **The chairs.** A hand on the seat sits down in it, facing the way it faces (at the table, the table). A hand on the back pulls it a
  block toward the reader, as before; crouching, a hand turns it. Nobody pulls a chair out from under someone sitting in it. This holds
  for every loose chair (the shut-up rooms' too).
- **The sink.** The tap fills a glass bottle with water; run over empty hands it washes them, and puts out anything burning.
- **The stove.** Whatever can be cooked goes on it, up to four at a time, and is done in ten seconds; a hand takes it off. The ring goes
  off by itself when it is done, and nothing left on it burns.
- **The fridge and the four wall cupboards** hold what is left in them (27 and 9 places), each thing where it was put, the same for
  everyone and kept with the House.
- **The table** keeps what is set on it, up to four things, where anyone can see them, until someone takes them.

And nothing in the kitchen frightens anyone: the House's growls are never heard there, and companions do not hesitate there. The window
over the sink is the kitchen's mystery, and it is enough.

## Tests

- `DoorSoundTests#aRoomSoundsThroughItsDoorAsItIsNow` (the séance, the masque per reader, Harrigan's funeral, rooms with nothing to
  change); door-sound states asserted in `VacantPossessionTests` (someone, fainter, nothing, still there), `LastSwimmerTests` (water,
  then nothing) and `FamiliarRoomTests` (someone in there, for the reader outside only).
- `FamiliarRoomTests#aWedgedKitchenStillLetsOthersInByAnotherDoor`: the wedge holding the entry on hall X; from hall Y the knock
  heard at the side door; a real crossing coming in by the side door, the wedge still in place; back out by the side door to hall Y.
- `FamiliarRoomTests#theKitchenIsForUseAndNothingFrightensAnyoneInIt`: sitting facing the table and not pulled out from under; the
  bottle filled and the fire put out; four on the stove, not done at once, done and taken off after ten seconds; the fridge keeping a
  thing in its place across a reload; the table keeping and giving.
- `TraceRoomTests` unchanged (the pull and turn still come from the back and from crouching).
