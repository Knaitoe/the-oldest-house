# Rooms known by their rhythm (0.4.246)

The owner, 2 November:

> Give familiar rooms distinctive sounds. Now that doorway audio follows room state, add recognizable rhythms: the kitchen's fridge
> motor followed by a drip, the pool's pace clock and distant turn against the tiles. Eventually players should recognize a
> particular room before opening its door.

Through a closed door, a room has sounded like its kind since 0.4.229 (a clock, water, music, voices), and like its present state
since 0.4.239 (someone moving about, or nothing). Two familiar rooms now have a rhythm of their own, heard in the same way: faintly,
for the reader standing at a closed door that leads there, through the wood.

- **The kitchen** (the familiar room): its clock ticking under everything. Then the fridge's motor starts and runs with a little
  rattle in it, cuts out with a shudder and a click, and after a moment's quiet the tap drips once into the sink. While somebody
  else is in the kitchen, it still sounds of someone moving about (0.4.239).
- **The pool** (the Last Swimmer): the tiled hall's hollow air and the water lapping at its edge. The pace clock's sweep hand clicks
  round once a second, and far off a swimmer turns against the wall: the dull knock of the push-off, the slap of water after it, and
  the hall answering both. Once the cover is on, it sounds of nothing (0.4.239).

  To the reader who closed it, it stays silent as its aftermath (0.4.244).

The first time a reader hears each, a line names it: "Through the door: a fridge running, then cutting out. A clock. The tap,
dripping once." and "Through the door: water lapping in a big tiled room, a clock's hand clicking round, and far off, someone turning
at the wall." After that the sound alone has to do it. Each rhythm is about four and a half seconds long and comes round every five
seconds for as long as the reader stands at the door.

## Code and assets

- `DoorSounds.Carry`: `KITCHEN` (`door.through_kitchen`) and `POOL` (`door.through_pool`), every 100 ticks. `CARRIES` gives them to
  `FAMILIAR_KITCHEN` and `LAST_SWIMMER`.
- `FamiliarRoom.heard` answers `KITCHEN` instead of `CLOCK`. `LastSwimmer.heard` answers `POOL` instead of `WATER` while the pool is
  uncovered.
- `tools/generate_door_sound_audio.py`: `through_kitchen` and `through_pool`, synthesized like the other door sounds (oscillators and
  noise, no recordings). The pool's hall is a short FFT reverb. Both are subtitled ("A fridge hums and stops behind the door; a drip";
  "A pace clock and a far turn at the wall, behind the door"), and the script's check covers them.

## Tests

The door-sound, kitchen and pool cases now expect the two rhythms where they expected the clock and the water:
- `DoorSoundTests` (the pool heard through a real door, and the pool's carry);
- `FamiliarRoomTests#theKitchenIsOneRoomForEveryoneAndKeepsWhatIsLeftInIt`;
- `LastSwimmerTests` (someone swimming, and the aftermath's stranger).

All pass locally. This is pending CI. No protocol, layout, Witness or source-kind change.
