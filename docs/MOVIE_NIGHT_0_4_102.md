# Movie Night, reworked — 0.4.102

On October 17 the owner asked for Movie Night to be reworked as a freer adaptation. They asked for a film actually shown
on the screen; a large shark that turns up rarely in the water, standing for the fear the film plants, and that never
harms anyone; and for the fireworks, the speakers and the pier to take part. The candidate is 0.4.102, protocol 74 and
layout 45. It changes new builds only: the mod is unreleased, and the owner judged mid-save repairs unimportant.
46 sources / 35 resolutions / two kinds / three endings are unchanged. The Witness story, its outcome id
(`rowed_to_the_far_pier_between_firework_exposures`) and the "survival" kind are kept.

## The scene

Movie Night is now built whole by `MovieNight.build`, like the Last Stop and the cottage. The polish, composition and
review passes leave it alone (`ScenePolish`, `SceneCraft.GROUNDS`, `SceneReview`). Stacey's three rock recesses in
the lake (`StaceyCover`) are kept.

- **The near beach.** Sand, blankets facing the screen, two deck chairs, a cooler, a lamp by the gate, and the
  projectionist's desk with the program on its lectern.
- **The dock.** It runs out over the water to the projector at its end. Four canoes are moored alongside.
- **The screen.** A black wall twenty blocks wide with a dark oak frame and bracing, on a raft moored in the middle of
  the lake.
- **The barge.** It carries three firework mortars, out to the east.
- **The far pier.** Spruce, over open water, with a lantern and a speaker at each end. A boardwalk leads from it to a
  ticket hut on the far bank, where the account waits.
- **The surroundings.** Mixed oak, birch and spruce woods line the banks, the lake has shallow shelves at its edges,
  and speakers on poles stand along the beach, beside the screen and on the pier.

## The film: *Teeth of Summer*

An original beach-horror picture, 130 seconds long, looping. Thirteen shots:

- countdown leader
- title over a sunset sea
- a night swim seen from below
- a fin crossing to the buoy
- the next morning's beach with a sandal left on the sand
- the lifeguard clearing the water
- the hotel owner who won't close his beach
- the boat at night with its searchlight, and the bell that stops
- the shark rising under the boat
- red flares and swimmers
- a lantern on still water
- dawn, a boy asking "Mom? Is it gone?" and THE END?
- the run-out of white light

The dialogue is burned in at the foot of the frame. Each client builds one 160 x 90 frame per tick (`MovieFilm`) from an
atlas drawn in code by `tools/draw_movie_night_art.py`: eight backdrops, the sprites, the countdown digits and three
silhouettes. Frames are keyed to the level's clock, so every reader sees the same moment. Each frame goes through an old
print: grain, dust, scratches, gate weave, flicker and a vignette. The beam is drawn as pale additive light from the lens
to the film's corners, and writes no depth, so the water and the film still draw through it.

**The shadows.** Anyone in the beam throws a shadow on the film, larger the nearer they are to the lens: a rower's head
and shoulders, a standing figure, or something on all fours.

## The night

- **Fireworks.** A rocket goes up from the barge every three to five seconds while anyone is here. Each burst lights the
  whole lake like lightning, for every reader. The light comes at the end of the rocket's own native lifetime, even if
  its section has stopped ticking.
- **Stacey.** She can only move in the dark: while a burst lights the lake she does not move at all. In the dark she
  stalks at half her usual pace. Being looked at turns her back only when she is caught in the projector's beam; in
  the dark, seeing her changes nothing.
- **Where she can go.** She keeps to the open water and the planks out on it (the dock, the raft and the barge). The far
  pier, the banks and the beach are not hers.
- **Whom she hunts.** A reader in a canoe, in the water but not under it, or out on the dock, raft or barge. Readers on
  dry land or on the pier are not hunted.
- **Exposures.** Seeing her in a burst counts, but only if the reader is out past the beach and she is in clear view.
  This is the old rule.
- **The speakers.** They play the film's score (beach tune, creeping strings, the buoy's bell, the sting, the dawn swell)
  from the two speakers nearest each reader, to that reader alone. The reel's flapping comes from the projector. When she
  is within twelve blocks in the dark, that reader's speakers cut out with a pop. They come back on the next cue once she
  is sixteen blocks off, or while a burst lights the lake.
- **The shark.** On about one visit in three, once, a shark that only that reader is ever sent comes for their canoe out
  past the screen. It circles with its fin cutting the surface, then goes under the hull: the canoe lifts and rolls,
  something knocks, and the water slaps back. Then it sinks away. It has no body to collide with, takes no damage, deals
  none and is never saved. There are no sharks in a lake.
- **The canoes.** There are four, one to a reader, at the dock. An empty-handed reader takes any canoe nobody here is
  using. A canoe goes back to its own place when its reader is finished or gone, and is replaced only after an actual
  discard or kill.
- **The pier.** Coming alongside the far pier puts the reader up on its boards. The scene is ready once the reader is on
  the pier or the bank behind it, has rowed thirty blocks and has seen her in two bursts. A reader who lands early is
  told to look for her in the next burst.

## Words

- **The program** (source, "Tonight on the lake", by the projectionist). It names the film, sends the reader to the
  canoes and tells them what the bursts and the speakers mean. It also warns them out of the beam and promises the pier.
- **The account** ("after the show"). It is told from what the reader did and saw: their own head on the screen,
  something on all fours crossing the beam, the speakers going quiet, the fin, being caught once in the dark. It ends
  with the film going on for nobody, and keeps the shared folded-paper afterword.
- **The Witness line:** "Each burst showed her nearer the canoe and held her where it found her. I rowed in the dark
  between them until the pier was under my hands."

## Assets

- **Art, drawn in code** by `tools/draw_movie_night_art.py`:
  - the film atlas, `textures/movie/teeth_of_summer.png`
  - the shark's skin, `textures/entity/lake_shark.png`
  - four prop textures
  - the speaker, projector and mortar models and blockstates
- **Nine sounds, synthesized** by `tools/generate_movie_night_audio.py`, each subtitled: `movie.calm`, `dread`, `sting`,
  `finale`, `bell`, `projector`, `reel`, `cut` and `bump`. The film's cues are pressed through a small old speaker.
- **New native pieces:**
  - the `movie_prop` block (speaker, projector, mortar)
  - the `movie_screen` block and its block entity, which carries the renderer
  - the private `lake_shark` entity, with its model and renderer
  - one payload (a burst's flash, the canoe's rock)

## Validation scope

Three Movie Night cases are rewritten and three added, so 656 are declared:

- Four canoes; the rower's route round the raft to the pier; two exposures; landing; the empty canoe returning to its
  place; a discard replaced once; the account.
- Bursts freeze her; exposures count exactly the bursts that found her in clear view; a reader on the beach is not out
  under them; reload.
- The beam: the reader's own shadow; she is turned back only in the beam; the speakers go quiet near her and come back;
  her shadow crosses the film the reader is watching.
- The shark: one per visit, sent only to its reader, no collision or harm, never saved, gone after its dive.
- The pier: she never steps on it; the pier alone is not the end without two exposures; the account tells only the
  reader's own night.
- An uncertain loaded absence cannot replace the actual saved canoe.

A native visual proof saves seven views. Five are frames of the film. The other two are the shark with its fin clear of
the water, and the props with the projector's shadows.
