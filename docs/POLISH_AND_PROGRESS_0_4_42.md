# Scene polish and the way deeper (0.4.42)

Playtest asks:
- a thorough polish of every vignette: no floating objects, proper borders,
  appropriate creatures, professional detail;
- exploring deeper must never be left to chance, even with the remembered map;
- the great staircase must not turn up in the early labyrinth.

## The audit

`VignetteAudit` is report-only. It runs inside the architecture GameTest after
every scene is built, and CI publishes it as "Vignette audit" annotations on the
build job. For each scene it reports:
- blocks that cannot survive where they stand;
- split doors;
- detached block clusters;
- hanging entities with no wall;
- floating or buried creatures;
- dark reachable floor, using block light propagated through the actual blocks;
- detail per reachable floor cell;
- outdoor edges that fall away into the void.

## ScenePolish

`ScenePolish` runs once per scene, after construction (checkpoint
`scene_polish_0442`).
- **Existing worlds:** it runs in place, one vacant scene at a time, after its
  chunks have loaded by ticket.
- **Explicit rebuilds:** these polish the scene again.
- **Barn and den:** the barn farm and the Mother's den are polished again after
  the finishing upgrades re-dress them.

It changes only defective blocks or empty space, never story volumes, doorways or
block entities:
- **Lanterns:** a hanging lantern always hangs from something. Its chain runs up
  to the ceiling, or outdoors it becomes a lamp on a post. Lamps over water or
  the void are removed.
- **Wall fittings:** signs, ladders, bells and levers turn to face a real wall.
  A wall sign set into the wall line steps out onto the restored wall face, with
  its words kept. A free-standing sign gets a post.
- **Other unsupported blocks:** these are cleared, as vanilla would clear them on
  the next update. Paths under solid blocks become dirt.
- **Trees:** trees left standing over water that was dug in later are removed.
  Authored leaves are made persistent so they no longer decay.
- **Creatures:** buried creatures step out into open space. Captives held in
  story volumes stay put.
- **Outdoor borders:** outdoor scenes gain a twelve-block border of the same
  ground. It has scattered spruces and ferns, and its outer edge is undergrowth
  too thick to see through, so no edge opens onto the void.
  - The arrival vestibule is left as the door copies it.
  - Ground that already stands is left alone.
- **Lighting:** interiors are lit with chain-hung lanterns until most reachable
  floor can be read. The darkness allowed is 20% by default, 45% in uneasy
  places and 60% in the mazes. The light sink, blind stretch, flooded passage,
  caves and hide-and-clap stay dark by design.

## SceneDressing

`SceneDressing` furnishes rooms that measure as bare (fewer than 0.10 detail
objects per reachable floor cell), using a theme per scene:
- a rug two cells clear of walls and furniture;
- furniture with a small object on top, against straight runs of wall;
- frames, clocks and coats on the walls;
- shelves in the studies;
- cobwebs in high corners of the older places;
- weathered stone along the maze paths.

Nothing narrows a passage below two blocks.

Flat outdoor ground (under 6% covered) gets what grows on it:
- grass and ferns;
- needles and mushrooms on podzol;
- drifts on snow;
- an occasional boulder or fallen trunk on open, natural ground.

## Creatures

Vanilla monsters no longer join inside the manor's rooms (in either copy of the
house) or inside the impossible hallway. Explicitly allowed creatures and the
House's own creatures are unaffected.

## The way deeper

Every spot keeps at least one *way on*: a dealt door, for this player, to a ready
place that has dealt doors of its own.
- Loops do not count, because they lead on only slowly.
- The great staircase counts once it is offered.

How this is kept:
- **On arrival:** if a remembered or newly dealt spot has no way on, one ordinary
  door is turned into a way on, and the map remembers it.
  - A story behind a corridor's only door is kept.
  - A loop behind a corridor's only door is replaced.
- **On use:** a dealt door that cannot take the player anywhere opens onto an
  ordinary way on instead, when nothing else in the place leads on. This covers a
  quiet source after leaving a story, a story closed to this player and a place
  still being built.

Dormancy and rediscovery are unchanged wherever another way on exists.

## The great staircase

The great staircase is offered only at depth 20 or more (`STAIRCASE_DEPTH`),
including after its discovery. Previously a discovered staircase could reappear
from depth 6. First eligibility is unchanged: depth 12 and two stories. The way
back is now remembered 32 doors deep (it was 16), so depth 20 can be reached and
counted. The correspondence gates (0/6/8/12/16/16) are unaffected.

## The deep tier is stone

Three stone halls are appended at slots 69–71 (`StoneHalls`). This is layout
33; existing worlds build the new slots in place and leave every older slot
untouched.
- **Stone gallery:** a long vaulted gallery. Chiselled pilasters carry ribs
  across the vault, cloister benches sit in the bays, and a side way branches
  off. It has two ways on.
- **Stone crossing:** four pillars with chiselled bases and capitals, around a
  dry fountain, on a floor laid in rings. It has three ways on.
- **Stone descent:** a stair that steps down four levels, each with a rising
  step so the climb back needs no jump, into a lower hall. It has one way on.

All three are laid in aged masonry with cracked and mossy courses, and every
lamp hangs from the vault.

From depth 12 they are dealt as the ordinary ways on, with their own build tier.
The domestic straight, bent and cross halls fade out at depth 12 and are gone by
depth 14; the junction becomes rare. `LiteraryRooms.isLiterary` now covers slots
45–68 only.

Protocol 32 is unchanged.
