# Movie Night, reworked — 0.4.102

On October 17 the owner asked for Movie Night to be reworked as a freer adaptation. They asked for a film actually shown
on the screen; a large shark that turns up rarely in the water, standing for the fear the film plants, and that never
harms anyone; and for the fireworks, the speakers and the pier to take part. The candidate is 0.4.102, protocol 74 and
layout 45. It changes new builds only: the mod is unreleased, and the owner judged mid-save repairs unimportant.
46 sources / 35 resolutions / two kinds / three endings are unchanged. The Witness story, its outcome id
(`rowed_to_the_far_pier_between_firework_exposures`) and the "survival" kind are kept.

The owner then asked for people and custom textures on the lake. The scene now has an audience that talks, reacts to
the film and goes missing one at a time (see "The audience"). Its boards, screen, hut, props and canoes are all drawn in
code (see "Assets").

## The scene

Movie Night is now built whole by `MovieNight.build`, like the Last Stop and the cottage. The polish, composition and
review passes leave it alone (`ScenePolish`, `SceneCraft.GROUNDS`, `SceneReview`). Stacey's three rock recesses in
the lake (`StaceyCover`) are kept.

- **The near beach.** Sand, the film's poster on its board, a lamp by the gate, and the projectionist's desk with the
  program on its lectern. The Pruitts' three lawn chairs face the screen behind a plaid blanket, with the boy's popcorn
  and a cooler; two more blankets lie empty.
- **The dock.** Grey boards run out over the water to the projector at its end, with film cans beside it and a life ring
  on a post. Four painted canoes (red, blue, yellow, white) are moored alongside.
- **The screen.** A black canvas wall twenty blocks wide in a peeling white frame, braced, on a tarred raft moored in the
  middle of the lake. A teal canoe is tied to a buoy beside it, out of the beam.
- **The barge.** Grey boards, with three firework mortars, out to the east.
- **The far pier.** Oiled cedar, over open water, with a lantern and a speaker at each end. A cedar boardwalk leads from
  it to the ticket hut on the far bank: seafoam clapboard, a counter, a second poster, and the account in front of the
  counter.
- **Reading the boards.** She can stand on the grey dock and barge boards and the black raft. The far pier's warm cedar
  is never hers, so the safe planks look different from the others.
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
- **Where she can go.** She keeps to the open water and the grey or black boards out on it (the dock, the raft and the
  barge). The far pier's cedar, the banks and the beach are not hers.
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

## The audience

Seven people, each in a skin painted by `tools/paint_character_skins.py` (stranger phases 14 to 20):

- **Ray**, the projectionist, stands on the dock by the projector.
- **Dot** sells tickets in the hut on the far bank, behind her counter.
- **The Pruitts** sit in lawn chairs on the sand: Frank, his wife June, and their boy Jamie (a child, at child scale).
- **Bobby and Lynn**, two teenagers, sit together in the teal canoe tied up by the screen. Nobody else can get in.

They talk when a reader right-clicks them. Every few seconds one of them says something to whoever is near. At set
moments in the film they react to it: Frank's "Here it comes" at the fin, Jamie's "Mom!" at the rise. A line spoken to a
reader also goes to that reader's chat.

**Going missing.** One shared clock counts while anyone is in the scene. At 30 seconds the boy is due to go, at 55 the
two in the canoe, at 80 Frank and at 110 June. Each is taken only in the dark, never in a burst's light, and only when
no living reader or spectator in the level can see them (110 blocks, the usual view cone, nothing in the way). They are
not there the next time anyone looks. Each disappearance leaves:

- a soft splash from the water's edge nearest them;
- for Jamie, his popcorn spilled on the blanket;
- for Frank, his chair knocked over;
- for the teenagers, their canoe empty, drifting slowly off down the lake.

What the others say changes with each one gone. June tells the reader the boy went to the restroom. Later she says
Frank went to look for him, and asks the reader to sit with her. Dot stays on the safe bank. She greets each reader who
comes round once; if anyone has gone missing, she asks whether they saw the Pruitts on the way across. Each reader
present when someone vanishes has that person missing from their own night (`Missing0502`). Readers who arrive later
just find the chair empty.

When the scene has been empty for five seconds and no camera is within 70 blocks, everyone is back in place for the
next reader: the chair stood up, the popcorn back in its box, the canoe moored with Bobby and Lynn in it.

## Words

- **The program** (source, "Tonight on the lake", by Ray, the projectionist). It names the film, sends the reader to the
  canoes and tells them what the bursts and the speakers mean. It also warns them out of the beam and promises the pier.
- **The account** ("after the show"). It opens with Ray, the Pruitts and the two in the canoe, then tells what the reader
  did and saw: their own head on the screen, something on all fours crossing the beam, the speakers going quiet, each
  person who went missing while they were there, the fin, and being caught once in the dark. It also includes what Dot
  said, if she spoke to them. The film goes on "for nobody" only once June is gone too. It keeps the shared folded-paper
  afterword.
- **The Witness line:** "Each burst showed her nearer the canoe and held her where it found her. I rowed in the dark
  between them until the pier was under my hands."

## Assets

- **Art, drawn in code** by `tools/draw_movie_night_art.py`:
  - the film atlas, `textures/movie/teeth_of_summer.png`
  - the shark's skin, `textures/entity/lake_shark.png`
  - four prop textures
  - the speaker, projector and mortar models and blockstates
  - six board textures on the `movie_material` block: grey dock boards, the tarred raft, the pier's cedar, the screen's
    seamed black canvas (the screen block uses it too), the peeling white frame and the hut's seafoam clapboard
  - eleven more `movie_prop` kinds (56 variants), each with its own model: a folding lawn chair (green and white webbing,
    aluminium tubes, a leaning back) and the same chair knocked over, a plaid blanket, a red cooler, a striped popcorn box
    and the box spilled with kernels, the poster on its board (two halves: the title, a swimmer, the jaws rising), a life
    ring, a stack of film cans and a mooring buoy
  - five canoe paints (red, blue, yellow, white, teal): the lake house canoe's fresh paint recoloured on the same 128 x 64
    layout
- **Nine sounds, synthesized** by `tools/generate_movie_night_audio.py`, each subtitled: `movie.calm`, `dread`, `sting`,
  `finale`, `bell`, `projector`, `reel`, `cut` and `bump`. The film's cues are pressed through a small old speaker.
- **New native pieces:**
  - the `movie_prop` block (speaker, projector, mortar)
  - the `movie_screen` block and its block entity, which carries the renderer
  - the private `lake_shark` entity, with its model and renderer
  - one payload (a burst's flash, the canoe's rock)
  - `MovieNightCast`, the audience's shared clock, words and disappearances
  - `LakeCanoeEntity` carries a synced paint. `LakeCanoeRenderer` draws paint 0 as the vanilla boat (the cave's and the
    phone's canoes are unchanged) and any other paint on the lake house canoe's hull, which is now generic over boats.
- **Character skins:** seven `literary_movie_*` sheets on the 128 x 64 layout. The review catalog gains seven rows, and
  the character art proof gains one view (21).

## Validation scope

Three Movie Night cases are rewritten and four added, so 657 are declared:

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
- The audience: everyone is present in their own skin, with the Pruitts in their chairs and the boy at child scale. The
  teenagers ride their own painted canoe and nobody else can get in; the dock canoes are painted; Dot stands behind her
  counter; Frank answers. The boy stays while a reader watches him. Once nobody is looking, in the dark, he goes, the
  popcorn spills, and both readers have him missing. The teenagers go next, and their canoe drifts. The account names
  only who went. When everyone has left, the night resets: everyone is back, the popcorn is in its box and the canoe is
  at its mooring.
- The dock canoe counts in the existing cases count only the four reader canoes (the teenagers' is separate). The
  footing case uses the new boards: grey dock and black raft are hers, cedar is not.

A native visual proof saves nine views. Five are frames of the film. The others are:

- the shark with its fin clear of the water;
- the props with the projector's shadows;
- the audience's things with the six boards;
- the five painted canoes on their hull, which is checked to be a long canoe, with every paint present.
