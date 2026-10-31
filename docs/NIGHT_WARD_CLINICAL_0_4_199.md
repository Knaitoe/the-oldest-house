# The night ward as a clinical ward: 0.4.199

The owner asked for the night ward to be redesigned so it reads as a hospital: "The monitor in the hospital needs to have a base", then "Hospital curtains would add a lot of flavor too".

The old ward was the institute's material set over a room that the generic scene passes had furnished. It had a dozen chained lanterns, beds standing about in the open, glass partitions in mid-air and a counter in the middle. The "monitor" the owner meant was the red call button. It stood alone on one wall block over a light seam in the dado, and read as a screen on a pole.

`NightWard` now builds the ward whole. The scene passes leave it alone:
- polish and dressing (`ScenePolish`, `SceneDressing`);
- shells (`SceneShells.shapes`);
- `VignetteArchitecture`;
- playtest repairs.

## The room

- **The ward.** The same 17 by 22 box, now with a suspended ceiling three blocks up (`NightWard.CEILING_Y`), ward height rather than a hall's.
  - Tiled dado, painted walls and the chequered floor.
  - Wired-glass windows set back into reveals: two on the west wall, one on the east.
- **The bays.** Five beds, three along the west wall and two along the east, each in a curtained bay:
  - the bed, head to the wall, under a bed-head service panel with gas outlets, sockets, a nurse-call button and a reading lamp;
  - a locker beside the head;
  - a skirting night light;
  - a visitor's chair or a drip stand.
- **The curtains.** Pleated cloth two blocks high, hung on drop rods from a ceiling track.
  - They close each bay's two sides and half its front. The other half stands open toward the aisle, with only the track across it.
  - Curtains join the walls and each other like panes.
  - They are cloth: anyone walks through them, with a soft rustle, so nobody is ever shut in or stranded by one.
- **The nurses' station** by the door: a counter facing the aisle, a desk under the window with the files, a filing cabinet, a stool and a wall clock.
- **Opposite the station:** a basin, the medicine trolley and two waiting chairs under the window.
- **Further in:** another basin under the west window between the bays, a linen trolley, a drip stand and a trolley by the far wall.
- **The middle.** The incubator keeps the middle under its own lit panel, with the chair beside it and the chart on its stand.
  - A monitor stands beside it on a wheeled stand with a five-point base and a green trace on its screen.
  - A short partition runs from the east wall between the two east bays. It ends in a service column, and the call button sits on that column's panel: a proper call unit, no longer a lone block.
- **The light.** Ceiling panels in a grid, as on a ward at night:
  - lit only over the station and the incubator;
  - the rest dark;
  - night lights low along the walls;
  - a faint glow from the monitor;
  - the purple close fog as before.
- **The clocks.** There are three, one by the station, one opposite it and one on the far wall, as the miniature's note says.

## What does not change

- **The anchors:**
  - the incubator at (0,0,-14) and the clear ground round it;
  - the chair at (3,0,-12), facing it;
  - the button (`NovelRooms.BUTTON`) on its panel at (6,1,-14);
  - the chart (`NovelRooms.WARD_NOTE`) and its book;
  - the entry door and the way on at (4,0,-23).
- The night timers, the calls, the dawn chart, the source and the resolution.
- Witness stays 55 / 42 / two kinds / three endings.
- **Saved wards** keep the earlier room, refitted by `NightWardMaterials` as before; the new ward is for new builds.

## Build

- **Code.**
  - New: `labyrinth/NightWard` (the builder) and `labyrinth/NightWardCurtainBlock` (`night_ward_curtain`, pane-like, parts lower, upper and rail, no collision).
  - `NightWardBlock` gains ten fittings: counter, sink, column, unlit panel, bed-head unit, monitor stand, monitor, night light, trolley and drip stand. Light comes from the lit panel (15), night light (6) and monitor (3).
  - `NovelRooms.hospital` hands the ward to `NightWard`.
- **Art.** `tools/draw_night_ward_assets.py` draws the new materials (column, unlit panel, bed-head unit, monitor screen, night light, trolley, drip bag, rubber, counter, worktop, ceramic, and three curtain cloths), their models and the curtain's multipart blockstate. CI runs it in check mode. No existing PNG changes.
- **Protocol.** No new payloads.

## Tests and proofs

- `NightWardTests` (the saved-ward refit) and `NovelTests.wardDepartureResetsNightAndOnlyPresentDawnReadingCounts` (the night, the call, the dawn chart) pass on the new ward.
- The architecture export takes the suspended ceiling off (`maxY` is the ceiling less one) so the cutaway shows the bays.
- **`NightWardVisualProof`** checks that every state of the ward's blocks resolves its own night-ward sprites, the curtain's included. It renders:
  - a staged bay: bed, panel, locker, half-drawn curtains, the monitor on its stand and the call column;
  - fixture sheets for every fitting and curtain part.

  CI requires all five views.
