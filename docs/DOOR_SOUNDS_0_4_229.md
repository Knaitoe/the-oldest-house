# Sound through connected doors (0.4.229)

The owner, 1 November (`docs/PHYSICAL_HOUSE_PROPOSALS_2026_11_01.md`, proposal 4 of the build order): sound follows the House's
actual connections, so readers can find each other, and know a room, by listening.

`labyrinth/DoorSounds`. No vignette, source kind or Witness change; no layout change. Protocol unchanged (sounds and action-bar
lines only).

## Where a door leads

Every House door has a far side, and it is always the knocker's or the listener's own (`DoorSounds.far`):

- a dealt door leads to the place it is dealt to for them, and comes out at that place's own door (their kept copy, for a place
  with copies);
- a door with a fixed place leads there;
- from inside a room, its door leads back to the door they came in by (the top of their own way back);
- a door the House has not dealt yet leads nowhere, and carries nothing; a resident's locked door leads to nobody's side.

## Knocking

Striking a closed House door (an ordinary attack on it: the House never lets its doors break, so the strike did nothing before)
knocks on it: a knock where it is struck (`door.knock`), and the same knock, muffled, at the far side (`door.knock_far`), however
impossible the place. Anyone standing near that far door is told: "Someone knocks, on the other side of the door." One knock at a
time from the same hand (`KNOCK_GAP`, six ticks).

Knocking on a resident's locked door ("Someone lives here") is answered once: a little later, from behind the same door, two slow
knocks back.

## A door shutting

When a House door shuts, it is heard on its far side, for whoever last used it, as a dull thud (`door.thud_far`). A crossing
carries nothing back and forth: when a reader goes through, the door behind them and the one in front both move within a tick or
two of each other, and neither is heard through the other; nor is the thud played to the reader who has just come out there.

## Listening at a door

Standing at a closed House door (within `LISTEN`, about a block and three quarters), a reader hears faintly, through the wood, the
room it leads to for them, if it is a room that carries. Only they hear it. The first time they hear each kind, they are told what it
is:

| Kind | Rooms | Line |
|---|---|---|
| A clock | Winchester, the Masque, Harrigan, the confession, Usher, the Whale, Karen's room, the waiting room, the kitchen | "Through the door, faintly: a clock ticking." |
| Water | the pool, the lake house, the flooded passage, the barn well, the Shallows, the drowned town, the cistern, the bathroom | "Through the door: water, moving." |
| Music | the nightclub, the arcade, Movie Night, the Last Stop, Dead Air | "Through the door: music, only the low part of it." |
| Voices | the hotel, the Goatman's trailer, the séance, the Ghosts set, Costume Night, Vacant Possession | "Through the door: people talking. You can't make out the words." |

So two readers can find each other by knocking and listening, and one alone can recognise a room by its clock through another
entrance, or learn that two distant doors lead to the same place. The familiar room (0.4.232) will carry its own fridge, clock and
tap through every door that leads to it.

## Sounds

Seven original sounds by `tools/generate_door_sound_audio.py` (synthesized; `--check` in CI): the knock, the knock through a door,
a door shutting through a door, and the clock, water, music and voices as heard through one. Each has its own subtitle.

## Tests

`DoorSoundTests` (default namespace): a door undealt leads nowhere; dealt, it comes out at the place's own door for that reader and
somewhere else for another; from inside, back to the reader's own door and nobody else's; a resident's door leads nowhere. Then, in
loaded ground: a knock heard at the door and at the pool's door where it leads; one knock at a time; the water heard through the
closed door by the reader standing at it alone, and not again at once; a door shutting heard at its far side; a resident knocking
back from behind a locked door, once only.
