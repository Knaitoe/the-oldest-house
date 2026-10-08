# The elk carcasses in two stages (0.4.50)

Status: **implemented for 0.4.50; exact-head verification pending.** Layout 36 / protocol 34 (unchanged). The owner asked for this rework: the earlier scene was a single field of spruce over a five-by-seven grid of identical hide blocks. It is replaced by two stages, after the passage in *My Heart Is a Chainsaw* where the girls escape the yacht, are pursued, and hide in a pile of elk carcasses that also hides the construction crew's bodies.

No Witness source is added or removed. The story keeps its id (`elk_carcasses`), kind (survival), account text and saved outcome, so the pool stays forty-three eligible sources / thirty-three required / two kinds / three endings.

## Stage one: the yacht

The reader comes through the House door into a guest cabin on the lower deck of a large motor yacht. It is moored stern-to under a granite bluff, the morning after its party. The arrival vestibule is enclosed in the yacht's aft deckhouse, so from outside the yacht looks like a yacht, not a white box on the water.

The yacht has three decks.

- **Lower deck:** the cabin the reader wakes in, a corridor with four guest cabins, a companionway up to the wheelhouse, and the crew's quarters forward.
- **Main deck:** the aft deck where the party was, the saloon and wheelhouse behind tinted glass, side decks, and a foredeck with a sunpad and a hatch down to the crew's quarters.
- **Sun deck:** loungers and a flybridge under a bimini.

Thirteen murdered guests lie where they fell: in bunks, on cabin floors, slumped at the saloon table and the helm, on the aft deck, on a lounger. They are drawn by the prop renderer as actual bodies, in four poses (on the back, face down, slumped in a chair, curled), with four party-clothes skins.

The deckhand's note (the source) is on a reading stand in the waking cabin. The reader has to read it, as before.

**The forced escape.** The reader's own killer waits in the crew's quarters until the reader has actually seen three of the dead and been aboard for ten seconds, or has been aboard for a minute. If the reader is below, he comes aft through the crew door. If the reader is on deck, he climbs out of the foredeck hatch a block at a time. Either way the message is "He is still aboard."

He runs at the reader and strikes when close, for four points of damage. Blows do not stop him, and hitting him does not end the visit. There are two ways up from the lower deck (the companionway and a ladder from the waking cabin to the aft deck), so a reader is never cornered below.

The way off the yacht is over the side: every rail can be jumped, and the sun deck drops into eight blocks of water. He will not swim after the reader. He goes to the rail nearest them and watches. A boarding ladder lets a reader climb back aboard, and the hunt resumes. The House door in the cabin always allows retreat, as in every unfinished visit.

## Stage two: the stream woods and the cave

The second stage is laid out from the owner's sketch, and is about four times the area of the old scene. Across the lake, a stream runs from the south-west to the north-east between two wedges of woods:

- the larger wedge is west of the stream;
- a smaller one is east of it;
- the crew's half-cut construction site is in the open ground east of the woods.

The cave is not in the middle of the map. It is on the outer, north-western edge of the larger wood, at the foot of a rocky ridge, under a knoll. A fallen spruce lies half across its mouth, with ferns and a berry bush at its foot, so it has to be found. Two things lead to it for a reader who looks:

- the crew's sign ("Elk to the cut below the ridge") on their site trailer;
- a trail of dragged mud and stains that leaves the site's flatbed, fords the stream on stepping stones and crosses the wood to the mouth.

**On landing.** When the reader is ashore, the killer goes into the lake out of their sight (they hear the splash at the yacht). He comes up out of the water at whichever of three beaches is farthest from them.

**The hunt.**

- He walks a search of the valley and both woods, pausing to look round, and every other leg drifts toward the part of the valley the reader is in.
- He sees a reader in front of him in the open within 28 blocks. A crouching reader is seen only within 12 blocks, or 5 in undergrowth, and leaves block his sight.
- When he sees the reader he runs at them, faster than walking and slower than sprinting.
- When he loses them, he goes to where he last saw them before he goes back to searching.

**The cave.**

- An actual chamber of stone, andesite, tuff and cobble under the knoll, with a drag-mud floor, dripstone and a crack of light from the surface.
- The pile of carcasses is heaped against its back wall. The carcasses are the jointed elk model lying on its side, skinned, or folded on its belly, never a block of hide.
- The road crew are in the pile: two along its back, one half out of it, one beside it. Their hard hats lie on the floor, and one is on the trail.
- Under the front of the pile is a hollow, one block high and three wide. Its gap faces the chamber floor. A reader who crouches in front of the gap gets down and crawls in.

**The search.** A reader who lies still in the hollow for three seconds brings him into the cave. If he is more than seventy blocks away he is set down on the trail out of sight. Then he:

1. walks in;
2. passes the gap to the west;
3. passes it again to the east;
4. crouches at the pile for three seconds;
5. walks out and away up the valley.

**Giving yourself away.**

- If the reader moves or leaves the hollow while he is near, he hears it ("He heard you."). He crouches at the gap and reaches in under the hides every second and a half until they get out and run.
- If he saw them go into the hollow, the same happens.
- Hiding works again once they have broken his line of sight.

**The way out.** If the reader held still and actually saw his boots through the gap, he does not come back: "The boots do not come back. The crew's path leads on to their gate." If they held still but never watched him go, he will be back ("You never saw him go. He will be back."), and the next search can satisfy it.

The account is ready when the reader has held still through a search, seen the boots, and reached the crew's gate at the valley head. A footpath leaves the cave along the foot of the ridge to that gate. The ending ledger is on a barrel beside the gate. Reading it after the note resolves the source with the saved outcome `held_still_beneath_the_hides_and_left_by_service_path`.

The gate's door is a second House return door (`service`), like Holloway's, so a reader is not made to swim back to the yacht.

## Rules kept

- **Personal.** Every reader has their own killer, a private literary actor visible only to them. Two readers on one yacht are each hunted by their own. His blows land only on his reader. Nothing in either stage changes another reader's state or the shared scene. Spectators and non-participants are not hunted and earn nothing.
- **Honest progress.**
  - Seen bodies are counted from the reader's own line of sight.
  - "Still" is measured from the reader's actual position between samples.
  - The boots must be in the reader's own view, through the gap.
  - Earned facts (`EscapedYacht`, `PassedSearch`, `BootsSeen`) stay with the reader across visits. A new crossing always starts aboard again.
- **No soft-locks.**
  - The cabin door allows retreat at any time.
  - The gate door is a second way back.
  - The site trench has a step at each end.
  - The stream rises from a spring inside the scene.
  - A walker caught on scenery is set on at his target.
- **No generic scenery.** SceneCraft no longer plants its mixed woods over this scene, and the arrival is never given earth heaped into the lake. The generic polish still runs once after construction, and its wooded border sits behind the scene's own thicket.

## Saved worlds

A saved world at layout 32 to 35 has the earlier scene, and the builder carves the two stages in its place (`ElkUpgrade`).

**What is carved again.** This is a rebuild the owner authorised, not a repair, so the rebuild changes scenery and nothing that belongs to anyone:

- it is queued last;
- it is carved in bounded slices like any construction;
- until it stands, the old scene is not dealt and nothing runs in it.

**When.** Only once nobody is in the scene, no camera is within 32 blocks of it, and its chunks and entity sections are loaded. Afterwards the generic polish runs once on the new ground.

**What is kept.**

- **Readers' records.** Saved originals (including the earlier note's text for readers who took it), personal facts, outcomes and Witness evidence are personal records and are never touched.
- **The scene's shared killer** keeps his identity. He is moved below decks, and the next reader inherits him as their own instead of a new one being made.
- **Pets, animals and dropped items** that the new ground buried, or left over water or air, are set down on the north beach with their identity, owners and orders.

## Assets

`tools/generate_elk_yacht_assets.py` draws everything as original pixel art, at vanilla's 16-pixel density for blocks, so the scene sits with the grass and stone around it.

**Yacht materials:**

- gelcoat hull, a boot stripe with a gold pinstripe, navy waterline, antifouling;
- teak decking with black caulk;
- cherry saloon panelling and a cream saloon carpet;
- deck cushions and bimini canvas;
- tinted saloon glass, chrome-ringed portholes and a stainless rail.

**Crew and cave materials:**

- corrugated site-trailer siding;
- an orange safety-fence mesh;
- dragged mud;
- elk fur, which now replaces the hide prop's panelled texture.

**Props:**

- four blood decals (pool, smear, spatter, bootprints);
- a life ring;
- a dropped hard hat.

**The killer's new atlas and model:**

- a waxed canvas coat with coat tails that trail his stride;
- a charcoal hood over a stitched elk-hide mask;
- oiled logging boots with red laces and steel toes;
- a belt and sheath, and a felling axe.

**The killer's motion.** Each state has its own movement:

- **Watching:** weight shifting, slow head sweeps.
- **Searching:** a heavy gait, the axe carried low, the head turning side to side.
- **Running:** leaning forward, a long stride, the axe raised.
- **At the pile:** crouched, head cocked, one hand reaching.
- **Striking:** an overhead chop.

The Camp Blood killer shares the model and wears the same coat.

**Bodies and carcasses.** The carcass, guest and crew atlases are new: four of each body. The carcasses and bodies render through the literary prop block entity renderer, out to 48 blocks. Their block models are empty, so nothing blocky shows under them.

## Verification contract

Native GameTests (`the_oldest_house_literary`):

1. `elkReaderWakesAboardEscapesHidesUnderTheCarcassesAndLeavesByTheGate`:
   - the reader arrives in the cabin and reads the note;
   - three of the dead are actually seen before he shows himself;
   - he comes for them, and does not follow them into the lake;
   - landing starts the second stage;
   - in the hollow the reader is down and hidden;
   - a real navigated search passes the gap at least twice, and the boots are seen;
   - the gate makes the account ready, and reading the ledger resolves the source.
2. `elkKillersArePrivateAndMovingUnderThePileGivesTheReaderAway`:
   - two readers have two killers;
   - moving while he is at the pile gives the reader away, and he reaches in;
   - the other reader's crossing and killer are untouched.
3. `elkRebuildKeepsTheSceneKillerPetsAndDroppedThings`:
   - only layouts 32 to 35 rebuild;
   - an occupied scene is not taken down;
   - the shared killer, a buried sitting pet and a dropped book keep their identity and are set down safely;
   - the next reader inherits the killer.

Elsewhere:

- `sevenForwardPassesOfferOnlyStoneOrdinaryHallsAndRetainDrawnMaps` now also proves a layout-33 world carves the two stages in place and deals them only once they stand.
- The architecture test checks:
  - the yacht cabin and portholes, and the hull afloat with no lake water inside;
  - the gate's real door and the ending ledger;
  - the one-high hollow under carcasses;
  - at least three crew bodies and thirty carcasses in the cave;
  - the chamber roofed by rock;
  - the cave off-centre on the western wood's outer edge;
  - a stream running the valley.
- Two new architecture views: `elk_yacht` (the yacht in section) and `elk_cave` (the chamber sliced at the pile). This makes seventy-one views. The view renderer now draws carcasses and bodies.
- `ElkVisualProof` (native client):
  - renders the killer's five motions, four carcasses and eight bodies;
  - fails if any lying body or carcass sinks through or floats above its floor.
- Package checks:
  - the new classes, atlases, rail blockstate and porthole model;
  - every literary prop variant's textures;
  - 384 prop variants.
