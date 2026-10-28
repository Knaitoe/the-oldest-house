# Every scene has a way on — 0.4.144

The owner's October 28 direction: every room needs an exit that leads forward, not only back, and "the exits should just lead back to the halls." Where each door goes was chosen to suit its scene, with custom textures where no vanilla door fit.

## The rule

Each shared scene now has one **way on**: a door into the ordinary halls.

- **Halls only.** The way on is dealt an ordinary or quiet hall, never a story, a hazard, a loop or the great staircase. A way on that was ever dealt anything else is re-dealt to a hall before it is used. By the same route, the reader finds the same hall behind it.
- **Same locks as the entry.** Whatever holds a scene's entry shut also holds its way on shut, and the door says so. That covers a committed literary beat, the closed well, an armed Holloway pursuit and the hide-and-clap game. Ordinary unfinished visits may always go on.
- **Going on is leaving.** Crossing the way on runs the scene's own departure, as walking back out of the entry does, and the door the reader came in by falls quiet behind them.
- **One crossing.** The scene and its way on count as one forward crossing. From the hall beyond, going back returns the reader to the door that led into the scene. They are never carried back into a scene from its far side.

## Private rooms

The private rooms keep to themselves and get no way on:

- the backseat, the silo and Borrasca;
- the family and old-cabin copies;
- the Red Room;
- the five quiet rooms, which chain from the junction (0.4.143);
- the empty lake, which is internal.

The hotel and the Zampanò courtyard already had dealt doors of their own, so they are unchanged.

## The doors

Rooms take a door in their own joinery, in a far or side wall. Outdoor scenes take one in a bank, a hut or a shanty, or a door that stands in its own timber frame where the ground runs out.

| Scene | Door | Where |
| --- | --- | --- |
| Hide and clap | birch | far wall |
| Model home | painted panel | far wall |
| Harrigan | dark oak | east wall beside the entry: in the study, and in the foyer once it is a funeral home; a rebuild sets it again |
| Mother of strays, Karen's room, séance, Hill nursery, Usher, Crimson Hall, Confession, Floorboards | the room's own oak or dark oak | far wall |
| Miniatures | dark oak | side wall |
| Masque | dark oak | out of the black room |
| Wallpaper nursery, Winchester, Bly, Wheel, Ghosts set, painted interior, missing sleep | painted panel | far wall (Bly: past the conservatory pond) |
| Elk fan, layered man | oak | far wall |
| Mapping | the read screen door | far wall |
| Devil's Rock, end-of-world cabin | cabin | far wall |
| Three fingers | cabin | the cottage's side wall |
| Child's room | cellar | from the brick basement |
| Nightclub, tunnel, motion light, second waitress, elk lot | service | far wall |
| Whale institute, hospital | their own ward doors | far wall |
| Barn well | weathered | the back of the tool shed |
| Ted's cave, preserved cave | cellar | in the rock |
| Holloway's camp | cellar | back wall, beside the service door, which still leads back |
| Plain, shallows | weathered | edge of the ground |
| Goatman | weathered | the far end of the woods |
| Phone in the canoe | cellar | a root cellar in the south bank, under a spruce lintel |
| Holy rabbit | weathered, spruce frame | where the trail runs out, past the account |
| Costume night | weathered, spruce frame | the far side of the park |
| Movie night | painted panel | the ticket hut's side door |
| Winter lake | cabin | the back of the shanty, out on the ice |
| Camp Blood | weathered, dark oak frame | a camp gate at the end of the road |
| Lake house | cabin | the boathouse |
| Drowned town | painted panel | the first room of the motel |
| Elk carcasses | the crew's gate | already the scene's far door, beside the account; it leads on now instead of back |

A frame's posts and lintel go only into open ground (air, snow or grass), never water or anything kept there.

## The five new doors

Five fitted native doors are drawn by `tools/draw_onward_doors.py` (16-pixel halves; `--check` verifies the textures, models and states):

- weathered grey boards with rusted strap hinges;
- a cream painted panel door with a brass knob;
- a dark Z-braced cabin door;
- a grey-green steel service door with wired glass;
- a banded cellar door with an iron ring.

Like every House door, they cannot be broken.

## Saved worlds

New scenes get their door as they are built. A scene built before 0.4.144 gets its door only when nobody can see the wall it goes into: no camera, a spectator's included, within three blocks, or within about eighty degrees of the view and 96 blocks. That happens either just before a reader comes in, or later on a 20-tick check.

The door goes in only into the scene's own wall or open air, never over a container, a fitting or anything a player has placed since. The elk scene's saved gate is registered again as a way on, facing the valley it is used from.

## Tests

Two native cases join the quiet-room job, which now runs 17:

- **Hill nursery.** In the actual scene, the junction leads in and the fitted way on leads into an ordinary hall. This holds even after the way on was dealt a story. The crossing is one crossing, going back returns to the junction door, the junction door falls quiet, and the way on survives a reload.
- **Every scene and the saved cases.**
  - Every shared scene has exactly one way on inside its own room, and the private rooms have none.
  - A saved scene gets its door only once the reader turns away from the wall.
  - The saved elk gate leads on.
  - A story dealt to a way on is replaced by a hall.
  - The well's lock holds its way on.

The architecture case now checks every way on in a full rebuild: registered as a way on, an actual door, and a clear cell in front of it. Its reachability flood now starts on the room side of dealt doors as well as entries.

No layout, protocol or Witness change: 54 / 41 / two kinds / three endings.
