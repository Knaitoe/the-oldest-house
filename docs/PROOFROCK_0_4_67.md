# Proofrock and Indian Lake High — 0.4.67

Based on the 0.4.66 Whalestoe head (`5cedea9`). The owner asked for Drowned Town to become an actual town:

- large;
- designed around the places the witch can hide;
- with a school big enough for her to chase a reader through it while they gather the essays.

New textures could be made as needed. The plan below was approved before implementation.

There is no new eligible source: 43 sources / 33 resolutions / two kinds / three endings. The two-visit story, the church roof resolution and every rule of the hunt are unchanged. Layout 40 / protocol 41. Verified source 3ffcff14d87b9f4c48381e2af22e9639f0b2b327, run 38059068194: all twelve jobs and 484 native cases passed.

## The town

Proofrock fills the DROWNED_TOWN slot. The room grows from the old 58 × 64 shore to 128 × 128: x −64 to 64, z −128 to 0, y −13 to 8. The entry door stays where it was, at the south edge, opening onto the forest road. North is towards the lake.

- **Main Street** runs north from the forest road to the beach. It has asphalt with a broken centre line, stone kerbs, lamps on arms over the road, and parked cars at the kerb.
- **West side of Main Street:**
  - the general store, post office (with numbered pigeonholes) and laundromat;
  - the west alley behind them, closed off from the back yards by a board fence;
  - Pine Lane's six houses, each with a fenced grass lawn, a stone front walk, and a shed and woodpile out back.
- **East side of Main Street:**
  - the hardware store, the sheriff's office (with a barred cell) and the diner;
  - then, across Lake Street, the cinema (seats facing a dead screen), the bait shop and the town hall;
  - an east alley runs behind both rows.
- **School Street and Lake Street** cross Main Street. Indian Lake High stands on School Street, west of Main Street.
- **East of the shops:**
  - the garage, with pumps and an abandoned flatbed;
  - the motel: six rooms facing the lot, the office at the north end;
  - the town green, with a bandstand, benches and a memorial, and two houses facing it;
  - Beach Road behind them.
- **The lakefront:**
  - the beach, where the furnace and its supply barrel stand on bare sand;
  - the pier and the boathouse;
  - picnic tables and a cold fire pit.
- **The lake** fills the whole north shore and an eastern bay. It is shallow at the edge and shelves down to −12.
- **The old town under the lake:**
  - its street and lamps still stand, running from under the pier to the church door;
  - two wells still breathe through soul sand;
  - the church's steeple rises out of the water over the pulpit.
- **The edges** are spruce woods, with a cold campfire ring in the eastern trees.

Every shop has a signboard over its false front, painted across three panels (`town_fixture`), display windows, an awning, a back room behind a partition and a back door onto its alley. Each house has a bed, a table, a sofa and a lamp that is sometimes on.

## Where she hides

The town is built around the existing hunt. Stacey Graves still travels on the surface, hides behind real cover, comes from behind, strikes once and withdraws.

- **One level.** Every street, yard and floor she can cross is a single level, so a hunt can follow a reader anywhere on land. Her floors have no carpets or slabs.
- **Light and dark.** Lamps keep Main Street, the side streets and the school's east and south corridors lit. The alleys, back yards, back rooms, sheds and the school's north corridor are dark.
- **Cover.** Corners, cars, dumpsters, sheds, woodpiles and board fences break the lines of sight. Her first appearance and her fallback cover are dark spots she can always reach, starting in the east alley, far from the entry.
- **Her gaps.** A few low gaps take her body and not a reader's:
  - under the flatbed;
  - under the picnic tables;
  - under the gym's folded bleachers.
- **No high ground.** The picnic tables have no benches. A table top two blocks up is out of a reader's jump, so it never becomes somewhere to stand above her.
- **Refuges.** Living grass is a refuge, as it always was. That means the fenced lawns, the town green and the school courtyard. Grass grows nowhere else in town. Staying under the lake still hides a reader.

The witch's search is generalised to the larger town. Her ground test uses the room's own bounds. Her shore and pursuit routes use a bounded A\* search instead of breadth-first search, with budgets of 12,000 cells for the shore and 6,000 for an ambush.

## Indian Lake High

The school is single-storey and large enough for a chase. It runs from Main Street (x −6) to x −62, and from School Street (z −45) to the beach (z −88).

- **Entrance.** The front doors open from Main Street into the lobby, which has trophy cases and a crest. An open arch leads from the lobby into the ring corridor.
- **The ring corridor** goes all the way round a glazed grass courtyard. Each side of the courtyard has a door. Lockers line the corridor's outer walls between the doors.
- **Classrooms.** Every classroom has two ways out, has its own chalkboard and is set with rows of student desks. They are:
  - film studies (S1), with the projector and posters;
  - a second south classroom (S2);
  - an east classroom off the lobby (E1);
  - a north classroom by the beach (E2).
- **Offices.** The front office has a counter you can walk round. The principal's room holds the desk the key is put in.
- **The cafeteria** has a loading door onto the beach.
- **The gym** has a seven-block ceiling, high windows and folded bleachers along both sides. The bleachers are broken for the doors and leave a one-block gap underneath.
- **The library** has five long stacks with one cross aisle. It has doors to the ring, the cafeteria, the gym and the woods.

Inside, the gym, the library and the ring give a pursuit room to turn. The courtyard is the safe place at the school's centre.

### The essays and the key

- The three damp essays are in teachers' desks on different sides of the ring:
  - film studies;
  - the library;
  - the north classroom.
- On the second visit the church key is put into the principal's desk.
- Each teacher's desk (`school_desk`) is a real nine-slot drawer. What is put in it stays, and nothing refills it.
- The key goes into the principal's first empty slot. If a reader has filled the drawer, the key is left on top of it.

## Textures

`tools/generate_proofrock_assets.py` paints the new pieces as native 16-pixel textures. One block, `town_fixture`, has 46 kinds in four facings (184 states). The new textures are:

- twelve three-panel signboards: general store, post office, laundromat, hardware, sheriff, diner, cinema, bait, town hall, motel, garage and the school;
- lockers (front, top and side);
- a three-panel chalkboard;
- the trophy case and the school crest;
- the bleachers;
- a *LOST* poster;
- the student desk, in wood and metal;
- the teacher's desk, front and top.

Posters have no collision. Student desks are a 14 × 12 × 14 box that does not hide what is behind it.

## Saved worlds

Worlds saved at layouts 13 to 39 carve Drowned Town again as Proofrock, under the same guards as the Goatman, Whale and elk re-carves:

- the carve waits until the town is vacant, unwatched and loaded;
- residents are protected throughout;
- displaced residents settle afterwards;
- the witch and the congregation are placed by the scene itself.

**Kept:**

- **The town's shared state:** visits, dried essays, the key's placement, the unlocked church, the opened roof and completion.
- **Every reader's Witness record.**
- **Every finite container's contents.** Before any block is removed, the furnace, the supply barrel, the three essay desks and the key desk are emptied into saved custody (`Carry0467`). After the carve, each item goes back into its new counterpart:
  - in the same slot where possible;
  - otherwise in the first empty slot;
  - otherwise set down beside it.
- **Nothing is restocked.**
- **Air tools a reader left in the lake.** Each placed air door or soul sand is returned with the supplies, and the old placement record is cleared.
- **The finite canoe, the shore body and the hunter.** They keep their UUIDs and are moved to Proofrock's places for them.

**Lost:** anything a player built inside the old town.

A carve interrupted by a restart never takes custody twice.

The old shore's own passes no longer touch Drowned Town: exterior dressing, settlement decoration, scene composition and Stacy's cover pockets. Proofrock's builder authors everything, and the polish pass still lights and trims it.

## Tests

The native cases build the real town; there are no mocks.

- **DrownedTownTests:**
  - Main Street is paved;
  - lake water stands at the shore and in the bay;
  - the school door is a real door;
  - the essays are in their desks;
  - the church, its door, the furnace approach, the roof hatch and the air-tool ground are at their new positions.
- **IndianLakeLinkedTests:** an actual layout-14 world upgrades into Proofrock. The school's depleted desk keeps its exact remaining contents, there is exactly one key, and custody is released.
- **LakeAmbushTests:** an old town with a filled desk, furnace fuel and a reader's air door rebuilds. The desk contents, the fuel and the refunded door all arrive. The same canoe, shore body and hunter stand in their new places, and later visits do not replace them.
- **PhoneCanoeTests:** the linked cave still finds a teacher's desk after the upgrade.
- **ArchitectureTests:**
  - Main Street and all four sides of the school's ring are passable;
  - the courtyard is grass;
  - the front doors are real;
  - the bleachers leave a gap only the witch fits under.
- **CI package checks:** the 184 fixture states, the teacher's desk states, every signboard panel and school texture, and the new classes.
