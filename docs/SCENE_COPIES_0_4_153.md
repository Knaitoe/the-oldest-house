# Copies of private scenes — 0.4.153

The owner's October 28 direction, after asking what isolated instances would cost: build them for every private scene that can be occupied, with the bounds suggested:
- reuse a copy once nobody's story in it is unfinished;
- build the next copy ahead;
- cap the number of copies.

Since 0.4.151 a private scene that stands once was one reader's at a time, and a second reader's door opened onto an ordinary hall. Now the second reader gets a copy of the scene of their own.

## Which scenes

This release gives copies to the five private scenes that run through the literary scenes' controller:
- the confession;
- the Holy Rabbit;
- the Wheel house;
- the diner (the second waitress);
- the split-feed bedroom (missing sleep).

Harrigan, the Whale institute and the phone in the canoe run on systems of their own, and come next. Until then they keep the 0.4.151 rule, one reader at a time. The lake house's pairs will use the same copies.

## How a reader finds their copy

- **A reader keeps the copy they first enter** until they finish its story (until it can no longer be dealt to them). They come back to it on every later visit, even when the original stands empty, so anything they changed there is still there. Then the copy is free for the next reader.
- **Readers already partway through before 0.4.153** keep the original, which holds what they did there.
- **A reader with no copy** takes the first that stands free: the original first, then copies in order. A copy is free when nobody is in it (spectators hold nothing) and nobody keeps it for an unfinished story.
- **When nothing stands free,** the next copy is built, and the door says so and stays shut: "Someone is already in <the story's title>. Another is being made ready for you. Try the door again in a moment."
- **When every copy the server allows exists and none is free,** the 0.4.151 rule applies: the door opens onto an ordinary hall, "Someone is already in <title>. It will wait for you."
- **Building ahead.** When a reader takes the last free copy and another player is on the server, the next copy is built at once, so the next reader rarely waits. A player alone on a server never causes a copy to be built.
- **An operator's test door** still opens onto the original.

## The cap

The server setting `sceneCopies` (in the House's configuration, beside the encounter settings) is the most copies of each scene the House may build. It defaults to 8 and may be set from 0 to 64. At 0, no copies are built and every private scene is one reader at a time.

## Where copies stand and how they are built

- **Place.** Copies stand a million blocks west of the House, in the same dimension and at the same height as their original. Each further copy of a scene is another 65,536 blocks west. Both distances are wider than the whole slot grid, so no copy can overlap another scene, and readers in different copies are far out of each other's sight.
- **Construction.** One copy is built at a time, in bounded slices, with chunk tickets held only while it is built. Every stage resumes from a saved cursor after a restart. The copy is built the way the original was:
  - an interior copy's slot is filled solid first, a few columns a tick;
  - the scene's own builder carves it;
  - the original's finishing passes (polish, composition, review and dressing) dress it. They run against the House origin shifted by exactly the copy's offset, so each pass's once-only checkpoint is the copy's own and never touches the original's.
- **Doors.** The copy's doors are registered under the original's door ids with the copy's number appended (`confession/entry@2`). Everything that reads a door's scene from its id still reads it, and exact comparisons (return doors, the diner's till exit, the way on) compare the id without the number. Each copy has its own entry, its own return doors and its own fitted way on into the halls.
- **Recognised as the House.** The slot grid finds copies from position alone (their offset is fixed), so a reader in a copy is inside the House rather than out through a wall. The same holds for block protection, the scene's sky and view on the client, the outdoor scenes' physical edges, and the controller's current scene.
- **Each reader's scene is where they are.** The literary controller gives every reader the location of the copy they stand in, or the one they keep. The scenes' cast, papers, lights, the bedroom's camera feed and the diner's waitress follow it. A scene's shared cast (the confession's listener) is its own in each copy.

## What it costs

Measured in the native tests, a copy of the confession room was built in about five seconds of background work, without a stall. The larger outdoor Holy Rabbit takes longer, in the same bounded slices. On disk a copy is a few megabytes at most. A copy costs memory only while someone is in it, about what one player exploring a new area costs. Empty copies are not loaded.

## Tests

One native case joins the co-op cases (seven; the quiet-room job runs 24):
- the first reader comes into the original confession room though a spectator stands in it, and keeps it;
- with another reader on the server, the next copy is built ahead; while it is built, the second reader's door stays shut;
- once it stands, the second reader comes into a copy of their own, a million blocks west. The House knows it for the confession, each reader's scene is where they are, and the copy has its own entry and way on, built like the original;
- the reader in the copy is not thrown out through a wall;
- walking out through the copy's own entry takes them back to the hall they came from;
- they come back to the copy they keep, though the original stands empty;
- once the first reader has finished the story, a new reader takes the original; the spectator keeps nothing, and nobody is hurt.

The 0.4.151 private-scene case still passes for Harrigan, which has no copies yet.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings.
