# Traces, and the door wedge (0.4.231)

The owner, 1 November (`docs/PHYSICAL_HOUSE_PROPOSALS_2026_11_01.md`, proposal 6): "Dust keeps footprints; a chair stays where it
was dragged; a wedge holds a door where it was left. Readers can know a room by what they did in it." The wedge "holds an unstable
route open: a wedged door is not shut behind the reader, and the hall beyond it stays what it was. Rarely, and only after many
dependable returns: a second set of prints beside the reader's own, or a second chair drawn up to their table. Most traces stay
exactly as left, so the exceptions carry weight." And, for the lighter puzzles: "a door wedge becomes a way to hold an unstable route
open. Learning something useful should itself feel rewarding."

`labyrinth/TraceRooms`, `TraceBlock`, `Wedges`, `TraceRegistry`. Three new gray places (slots 150-152), layout 75; no vignette,
source kind, Witness or protocol change.

## The shut-up rooms (DUST_HALL, slot 150)

An ordinary hall, built whole: grey boards under dust, panelling to the dado and faded sprigged paper above, furniture under dust
sheets along the walls, a small table, one loose chair, cobwebs in the corners. A side door and a far door lead on. Dealt a little more
often than a plain hall (weight 12) from the third crossing until the halls turn to stone.

- **Dust keeps footprints.** Every floor cell is dust (`TraceBlock` DUST). A reader walking through it leaves prints pointing the way
  they went (stage 1, turned by the block's facing); walking it again in another direction turns them. Nothing tidies them away.
  Each reader's own path (the last 48 cells) is remembered.
- **A chair stays where it was dragged.** Right-click the loose chair and it is pulled a block toward the reader, scraping; the dust
  it stood in is scored where it was dragged (stage 4). Crouching, a right-click turns it. It goes only onto bare floor or dust with
  headroom, never into anyone, and stays wherever it is left. A chair left drawn up to the table, facing it, is remembered.

## The wedge

`door_wedge`, one to a reader, ever: from the hook in the cloakroom or in the sprung rooms ("There is only the one wedge on the hook,
and it is the one you took"). Nothing replaces a lost one.

With the wedge in either hand, a right-click on a door it can hold puts it in under the door (opening the door first if it is
shut; with the wedge in hand every other door is used as ever); a right-click on a wedged door works it out again, into the hand
of whoever pulls it. The wedge lies at the door's foot (a display of the item), so it stays where it was left for everyone to see.

- **Doors on closers** (the cloakroom's and the sprung rooms'): a door left open swings shut thirty ticks later unless something holds
  it: the wedge, a chair in the cell either side of it, or someone standing in it.
- **The House's own doors**: only the door a reader came in by, from inside an ordinary hall, a quiet room or a puzzle room (never a
  story, never a room copied for each reader). Wedged:
  - it is not shut behind anyone, coming or going, so the stretch of hall copied in behind it stays in view;
  - the hall behind it stays what it was: for the one who set it, the door they came through still leads to this room however they
    come back to that hall (the House deals a hall's doors by the way it was reached, so another way there can deal the same door
    elsewhere), and the House's growls (`GrowlChanges`), which deal an ordinary door somewhere new when nobody is looking, never touch
    it;
  - nobody comes in by that door from anywhere else while it stands open on the hall it shows: their door "gives an inch and stops
    against something on the other side." From the hall it shows, it opens as ever. If the one who set it is not in the game, the
    wedge is knocked loose instead, and lies by the door;
  - anything that shuts the door hard (a story's exit closing on it) knocks it loose the same way.

## The cloakroom (PROPPED_DOOR, early, slot 151)

The fifth puzzle family, `PuzzleRooms.Family.HOLD`, offered by the shared puzzle rhythm like the others (an early room from the third
crossing and never in the stone; the deep room only once the early one is solved, from the strange depths); nothing counts down,
hurts or is used up; a solved room stays solved, for everyone; no Witness.

A cloakroom: the hook with the wedge, a hat shelf, a bench, a pot for umbrellas. Its inner door is on a closer, and behind it a long
narrow passage turns once and runs to the far door, iron, with no handle on this side: "The door has no handle on this side. It gives
only while the way behind you stands open." Nobody can reach it before the inner door swings shut behind them. Wedged, the door stays
open, and at the far door: "The far door gives. Behind you, the way you came is still standing open." (A second reader standing in
the doorway does as well.) The wedge is the reader's to keep, and to take into the halls.

## The sprung rooms (SPRUNG_ROOMS, deep, slot 152)

Three rooms under dust in a line, each door between them on a closer, the far door in the last: it gives only while both doors
behind stand open, all the way back. One wedge holds one door. The other is held by the room's loose chair, dragged block by block
into its doorway (or by a second reader). "The far door gives. Both doors behind you are standing open, all the way back."

## Dependable returns, and the rare exception

Arriving somewhere a reader left a trace and finding it as they left it (at least half their prints still there, their chair where
they left it, or their wedge still in its door) is a dependable return. After six of them, each later departure has one chance in four
that the House adds one thing, once to a place for each reader, and only with nobody else there and nobody looking at where it goes:
a second chair drawn up across the table, if the reader left one drawn up to it; otherwise a second set of prints, bare and narrower,
beside the reader's own along their path (dust stage 2, or 3 where both lie). Most traces stay exactly as left.

## Art

`tools/draw_trace_fittings.py`: four materials, the fitting block's dust (clean, prints, the second prints, both, dragged), the chair,
the table, the dust sheet, the hook with the wedge, the closer's arm, and the wedge as an item (17 textures). Its check holds that
clean dust shows no bare board and walked dust does.

## Tests

`TraceRoomTests` (exploration namespace): the shut-up rooms built whole as an ordinary hall with no story, not in the first crossings nor in the stone;
a walk up the hall printing every cell north and no other; the reader's path remembered; the chair pulled a block, scoring the dust,
and turned crouching; pulled up to the table and remembered; six dependable returns and, leaving with nobody looking, a second chair
across the table, once only. The cloakroom: the far door shut with no handle; one wedge from the hook; the inner door swinging shut
by itself; nothing at the far door with the way behind shut; wedged, the door held and the far door giving, solved for good; the wedge
pulled back out; built again as left. The sprung rooms: the wedge holding one door, the other swinging shut, nothing at the far door;
the chair pulled a block, then standing in the second doorway, both held, the far door giving. A House door: a door further in
refused; the entry wedged and held open; the route held for its owner only; reached another way, the junction's west door still
leads to the shut-up rooms; from the east door the entry stops, from the west it opens; its owner gone from the game, knocked loose
and lying by the door. `LabyrinthTests`: twenty-one gray places at the start, the shut-up rooms and the cloakroom among them.
