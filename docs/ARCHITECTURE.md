# Static architecture of The Oldest House

Before anything impossible happens, The Oldest House must feel like a beautiful, slightly eccentric old family house that plausibly existed for generations. The mundane architecture has to be coherent enough that the player learns it: when a door appears where there was a wall, the player should know something is genuinely wrong rather than suspect the generator.

**Scale is a deliberate departure from the design document.** The design document describes a small house, 12 blocks wide outside and 13 inside, whose interior is a solid dimension of carved rooms joined by teleport doors. The project keeps the large manor instead: its exterior stays fixed, and the impossible space grows behind it (starting with the rear hallway).

All geometry lives in `HouseLayout`. The builder (`HouseShell`, `HouseRoofs`, `HouseInteriors`), boundary transitions, mirror reconciliation, the sightline renderer, spawn checks and the structure game tests all read from it.

## Orientation

- The front faces north (-Z). +X is east.
- Plans are drawn with the front at the top, so **left is west**. Walking up to the front door, the great-room wing is on your right.
- `y = 0` is the ground-floor floor course; the upper floor course is `y = 6`; the cellar floor is `y = -5`.

## Masses

The house is several distinct masses that read as additions made over decades:

| Mass | Plan position | Storeys | Roof |
| --- | --- | --- | --- |
| Great room wing | front left, dominant | 2, upper floor jettied one block forward | tallest front gable (ridge 17), wall dormer on the west slope |
| Entrance and hall | centre, recessed 4 blocks behind the great room | 2, upper floor overhangs the porch | small low front gable (ridge 13) that runs into the cross-gable |
| Kitchen wing | front right, set back one block | 1.5, loft in the roof | lower front gable (ridge 14), one dormer on the east slope |
| Study wing | rear left, inset one block from the great room's west wall | 1.5, long gallery in the roof | perpendicular rear cross-gable (ridge 15), west gable end |
| Stair tower | rear right, behind the kitchen | tall stair hall to y 14, stone ground storey | separate hipped roof with flared eaves (ridge 18), finial |
| Service range | rear right, behind the tower | 1.5, box room in the roof | low gable (ridge 11) |

The kitchen wing, tower and service range enclose a paved service yard, open to the east behind a low wall and gate.

## Ground floor

```text
                              FRONT (north)
              ┌───bay───┐           porch
     ┌────────┘ seat    └────────┐ ┌─────┐ ┌──────────────────────┐
     │                           │ │     │ │ DINING       window  │
 ▓▓▓ │  GREAT ROOM               ├─┤ door├─┤                      │ ▓
 ▓▓▓ │▓ hearth ◄── seating group ▒ │     ▒ │                range │▓▓
 ▓▓▓ │  built-ins, beams         │ │  H  │ │ KITCHEN              │ ▓
     │                           │ │  A  │ │ counter      back door
     ├───────────────────────────┤ │  L  │ ├───────────┬──────────┘
     │                           │ │  L  ▒ │ STAIR     │  yard
     │  STUDY                    ▒ │     │ │ TOWER     │  (gate)
     │  library wall, map table, │ │     │ │ dog-leg   │
     │  lecterns, desk           │ │     │ ├───────────┴──────────┐
     │          hearth           │ │     ▒ │ SCULLERY, cellar stair
     └──────────▓▓▓▓─────────────┴─┤  ?  ├─┴──────────────────────┘
                                    threshold (future impossible door)
```

`▓` chimney stacks, `▒` openings and doors. The front door, the hall and the threshold wall share one straight axis (`x = 15`). Nothing permanent crosses it: every room opens off the sides of the hall, and the stair is in its own tower. From just inside the front door the player sees the plain wall at the far end of a 21-block hall; the day that wall has a door in it, they see it immediately.

## Upper floor

- **Principal bedroom** (over the front of the great room): the oldest, most formal room. A tester bed, its own fireplace in the great chimney, wardrobe, dressing table, portraits.
- **Literary bedroom** (over the rear of the great room): shelves along the partition, a desk, a lectern, a reading chair.
- **Upper hall**: runs the length of the house over the ground hall, with a window seat over the porch. Its rear half opens up into the cross-gable.
- **Maker loft** (in the kitchen roof): jukebox and note blocks, loom, fletching table and workbench, lit by the gable window and the dormer.
- **Long gallery** (in the cross-gable roof): open timber roof with tie beams, pictures, a window seat at the west gable.
- **Stair tower upper landing**: opens off the upper hall. The half landing below doubles as a library nook with its own window seat.
- **Box room** (in the service roof).

## Stair tower

The dog-leg rises six blocks in two flights of three steps. The first flight climbs south along the tower's west side to a half landing across the tower. The second returns north along the east side; its top step sits in the floor course so it finishes flush with the upper landing. A panelled spine with a newel post and balustrade separates the flights. The cellar stair runs down from the scullery.

## Exterior language

- Stone ground course and plinth under every wall, carried down to real terrain wherever the ground falls away.
- Plaster panels in dark oak framing: corner posts, jambs, studs, and floor beams at the jetty lines.
- Projecting sills on ground-floor windows; mullioned upper windows sit on the framing's sill rail.
- Deep verge boards on every gable end.
- No glazing on the south (rear) elevation. The impossible hallway will run south from the threshold, and it must never be visible from an ordinary window. The rear instead carries bricked-up openings framed in timber.

## Domestic purpose

The house is meant to be used before it is feared: crafting, furnaces and smokers, storage, beds, lecterns, fireplaces, a cellar workshop. Container contents (loot, books) exist only in the authoritative House dimension. The Overworld shell's containers are empty proxies.
