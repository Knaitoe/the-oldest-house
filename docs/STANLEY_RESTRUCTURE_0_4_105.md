# The hotel, restructured (0.4.105)

The owner asked for the Stanley to be comprehensively restructured: maps well designed and congruent, and the narrative
playing. They approved this plan: one real building over three floors, its doors joined to the places they lead to, one
story of the closing account, every existing mechanic kept, and three endings at the desk. New builds only; saved hotels
are not migrated (the mod is unreleased).

## The building

`HotelRooms` builds the hotel whole. Polish, composition, review, playtest-repair and architecture passes skip the hotel
and its grounds (`ScenePolish`, `SceneCraft`, `SceneReview`/`SceneHuntReview`, `ScenePlaytestRepairs`,
`VignetteArchitecture`). Partitions between differently finished rooms are two thick, so each room shows its own walls.
Floors and ceilings between the storeys are one `CEILING` piece: boards on top, plaster underneath.

Ground floor (floor y -1, walk 0..4, ceiling 5):

- **Lobby** x -10..10, z -1..-16. Entry at (0,0,1) through the front wall. The desk runs down the west side (x -7,
  z -3..-7, facing east): the bell at `DESK` (-7,0,-5), the register at `REGISTER` (-7,0,-4), the key rack behind, the
  porter behind it, the office door behind him at (-11,0,-6). The "Closing night" card on an easel at `RULES` (-3,0,-2).
  The grand stair rises up the east side (x 8..10, treads z -6..-11, y 0..5) through the ceiling to the landing.
  Archways to the bar (east), the dining room (west) and the ballroom (north).
- **Bar** x 13..27, z -1..-16: gold paper, counter at x 21, back bar and dark mirror at x 27, stools, three tables.
- **Office** x -27..-13, z -1..-8: panelled, desk with the manager's notes (`OFFICE_NOTES`), filing cabinets,
  switchboard.
- **Dining room** x -27..-13, z -11..-33: the table for two at `TABLE` (-21,0,-21) with its two chairs, five cleared
  tables, the fire at `FIRE` (-20,0,-33) under a stone breast and mantel, and the orchestra's empty dais in the far
  corner (music on the stands, the sound plays there). 217's patched floor is straight over the table.
- **Ballroom** x -10..10, z -19..-40, two storeys high (walk 0..9): parquet, gold walls, pilasters, the bandstand across
  the far end with the piano and the pianist, the 1921 photograph at `PHOTO` (-10,2,-28) on the west wall (a guest's head
  goes in `FRONT_ROW`), two tall dark mirrors opposite. The dance floor is x -9..9, z -19..-35.
- **Kitchen** x -27..-13, z -36..-50 (tiles, ranges, the steel island, a chest), its swing door from the dining room;
  **stores** x -10..10, z -43..-50; **laundry** x 13..27, z -43..-50.
- **Service corridor** x -27..27, z -53..-55. North of it: the **caretaker's rooms** x -27..-15 (the log `LOG` on his
  desk, the typewriter `TYPEWRITER`), the **staff entrance** x -12..-8 behind the iron `SERVICE_DOOR` (-10,0,-56) with
  its time clock and the exit door `STAFF_EXIT` (-10,0,-67), the **back porch** x -3..3 with the back door `BACK_DOOR`
  (0,0,-67), the stair well down to the boiler (x 9..11), and the **workshop** x 14..27.

Basement: the **boiler room** x -10..11, z -60..-66 (walk -4..-2), down the walled service stair. `BOILER` (0,-4,-65),
`GAUGE` (0,-3,-64), the vent `VALVE` (3,-3,-66), the restart `SWITCH` (5,-3,-66), the plant notice (-4,-3,-66).

Upstairs (carpet at 6 over boards at 5, walk to 8, ceiling 9):

- **Landing** x 8..10, z -8..-14, at the top of the stair; the well through the ground floor's ceiling is z -8..-10, so
  a guest's head clears it from the third tread up.
- **Guest corridor**: west along z -12..-14 to x -15, then north along x -15..-13 to the end door `NUMBERS` (-14,6,-45).
  It is built exactly as the numbered corridor is: three wide, carpet on the boards, wainscot to the knee, paper above,
  the same ceiling, spruce doors set in its walls and a brass plate one step before each. Its doors are numbered 211..222
  in the order a guest passes them (`LOCKED`); they stay shut ("Someone lives here"), and nothing is behind them.
- **Room 217** x -27..-18, z -16..-26, door `ROOM_DOOR` (-16,6,-21) on the corridor's west wall, its plate
  `PLAQUE_217` (-15,7,-20) (it reads 237 once looked away from), the hose rack `HOSE_RACK` across from it. Faded rose
  paper, the bed at `BED`/`BED_HEAD`, the drawers `DRAWER`, the candle, the housekeeping card `HOUSEKEEPING` on the
  desk, two windows, the pale `PATCH` (-21,5,-21) in its floor over the table for two, and a tiled bathroom with the tub.
- **Linen room** x -22..-18, z -33..-38, and **the caretaker's family's rooms** x -27..-18, z -41..-50: two small beds
  made, the children's trunks at their feet, a note left on the table (`FAMILY_NOTE`).

Windows (`WINDOWS`) stand in the outer walls of the rooms that have them. The room side is night with snow banked against
the glass; the outside is lit panes behind red curtains. The shared world keeps every window at stage 0; each guest is
sent their own snow height (`HotelVignette.views`, on arrival and every 100 ticks): at the sill on the first night,
halfway up the lower sash on the second, above the latch on the third, nearly to the top after.

## Joined doors

- **Back door to the grounds.** The rear wall around the back door (x -7..7) is siding, the porch's two windows and a
  cornice row, and the porch inside it is clapboarded, so the copy the door makes into the grounds' vestibule is the
  hotel's own back wall and porch. The grounds build the rest of that wall to match at z 1: siding, the ground floor's
  lit windows where the hotel has them, the upper floor shuttered, cornice, red roof and chimneys, a canopy and lamps
  over the back door, and the staff door (bolted from inside). Coming back through the grounds' door returns the guest
  to the porch side of the back door, as always.
- **The guest corridor and the numbered corridor.** The corridor's end door is `hotel/numbers` to the numbered
  corridor; the rooms there begin again at 201, as the card says. The numbered corridor's carpet now lies on the same
  boards as the hotel's (its doors sit flush; before, the carpet was the floor and the doors stood a step up), so 217's
  side of the two corridors is the same block for block.
- **217.** In the numbered corridor, the door a reader sees numbered 217 opens into the hotel's own guest corridor in
  front of 217's door, standing where they stood in front of the other (`HotelVignette.destinationEntry` returns an
  unregistered door at `ROOM_DOOR`; `LabyrinthDoors.arrive` copies nothing over it).
- **Against the numbers.** A guest who has never stayed and walks back against the numbers to the numbered corridor's
  door finds its way back now leads out into the hotel's guest corridor: `HotelVignette.route` puts the corridor's end
  door on the top of their return stack, and the ordinary return takes them there. Their way back from the hotel is then
  where they first came from.
- **Staff entrance.** `hotel/service` is a dealt door out of the hotel.

## The account

Everything is free and goes on the account: dinner at the table for two (sit, it is served, eat it) and each drink at the
bar. Opening any chest still settles one place into lost property at the desk, where it can be collected; rest in 217
still sorts the pockets the first night and puts them in the drawers after; torches lit in 217 are still handed back.

**Nights.** Coming in from outside starts a stay; the first time a stay reaches the hotel itself is another night
(`Visits`). Crossing to the grounds or the numbered corridor and back is the same night; a stay ends once the guest has
been gone from all three for 100 ticks. The arrival line changes with the night. The hose is further from its rack each
night (as before), and the snow higher.

**The third night** (`NIGHTS`) the account is due, and the desk's account says so and names the three ways out of it.
Each is the story's ending, once in the Witness account, with its own line (`HotelTexts.witness`):

- **Settle** (`read_the_settled_account`). Ring the bell: the porter takes what is owing from the guest's pack, one thing
  for each uncollected place, last pocket first, and keeps it (`Paid`). With nothing owing, the settled account is theirs
  to read; reading it through closes the account. It needs something to have been charged.
- **Sign the second place** (`signed_the_second_place`). The register's second line takes the name of a guest who is in
  the photograph; reading it through closes the account. The staff greet them as caretaker; the caretaker's log goes on
  in their name and the typewriter's page is theirs.
- **Walk out** (`walked_out_before_closing`). With the master key from the maze, through the iron service door and out by
  the staff entrance before the account is presented: the hotel keeps what it held (lost property and drawers are
  emptied).

Once closed the house charges nothing more; the porter, the bartender and the pianist speak to the ending.

## Kept

Property custody and its exact components, the drawers, returned torches, the boiler (found, pressure by days, the
blast, the restart), the photograph and the dancers, the pet cemetery and its fifty-two plots, the maze key, the hose and
its phases, 217 turning 237, the six cast members and their gestures, the death and lost-pet histories.

## Assets

`tools/generate_stanley_hotel_assets.py`: one `stanley` block, 40 kinds by 4 stages (640 states), 62 textures drawn in
code in vanilla-style 16x16 pixels; the hotel plates gain fixed numbers (`HotelRoomPlaqueBlockEntity`). The roof is red
standing-seam metal. Protocol 79.

## Tests

`HotelTests`: settling on the last night (and the same night across the grounds; the shared table and windows staying as
built while each guest's view changes); signing only in the photograph on the last night, the log going on in their
name; walking out with the key on the last night and not before, nor without the key; custody, the boiler, the bell's
real hit, locked rooms, the dancers on the ballroom floor; and a walk from the front door to every room, up the grand
stair and down to the boiler, the grounds' wall matching the rear wall, and 217's side of the corridor matching the
numbered corridor's. `OctoberFollowupTests`: the numbered corridor's 217 lets its reader out in front of 217's own door.
The architecture test exports the hotel's three floors and its grounds.

## Runs

- **Run 39197354581 (82961a5):** nine of twelve jobs passed. Two faults. The tub's two models named their top texture by
  a key they did not define, so every client check stopped loading models (build, trailer-client). The well through the
  lobby ceiling began over the fourth tread, so a guest's head struck the ceiling stepping up from the third and the walk
  never reached the landing (build, hotel-gameplay); the well now begins one tread earlier.
- **Run 39202668820 (ee85083):** eleven of twelve jobs passed, the hotel job and every client check included. The build
  job's full suite failed one case outside the hotel: the cancelled canoe toss (see the small fixes' runs).
