# Building the stories outside: keepsakes and block sets (0.4.758, 0.4.759)

The owner chose two of the three options (24 November): each story gives its **signature piece** as a keepsake, and finishing a
story **unlocks that story's block set** so a reader can build it in their own world. Nothing places a whole building for them.

## What a reader gets

When a reader finishes a story (the moment `WitnessAccount.resolved` records it, as actor or witness):

1. **The keepsake.** One block of the story's signature piece goes into their inventory (or drops at their feet if it is full),
   once per story per reader. It carries the story's name and one line in the story's own words. It places like any block, in
   any world. It is not bound to its reader: once set down it is furniture.
2. **The block set.** Every piece of the story is added to their recipe book in one go, with one grey chat line: "The pieces of
   “The back bedroom” can be cut at a stonecutter now." The pieces are made at a **stonecutter**, one from one, out of what each
   piece is made of, read from the sound of the block it was made like: wood from any planks, cloth from any wool, glass from
   glass, metal from an iron ingot, earth from dirt, and everything else (stone, brick, plaster, concrete) from cobblestone,
   blackstone or cobbled deepslate.

Why the stonecutter:
- Many pieces are cut from the same vanilla block; on a crafting table one input can only make one thing, so thousands of pieces
  would collide. The stonecutter already shows every piece a block can become and lets the reader choose.
- The stonecutter in vanilla shows its recipes to everyone. Here it shows a reader only the story pieces they have unlocked; others
  see the ordinary stonecutter. Server and client both filter, so nobody can cut what they have not earned.

## What is in a story's set

- Its **materials**: walls, floors, ceilings, trims, stairs, slabs (e.g. `cold_room_boards`, `cold_room_paper`, `cold_room_treads`).
- Its **fittings**: every kind of its furniture block, each as its own piece in its default stage (e.g. the cold room's bed head,
  bed foot, kitchen table, stove, crucifix, radiator).
- **Not** in it: blocks that make the House work rather than the room (doors back into the halls, accounts and sources while
  still unread, light-only and invisible blocks, triggers), and stage variants that only the story's own events reach (a door
  already broken, a page already burned). Pieces of a bigger thing (a bed's two halves, the plane's parts) are kept: builders place
  them like a vanilla bed or door, one half at a time.
- Blocks shared by many rooms (the halls, the domestic set) belong to no story and are not unlocked.

## Outside the House

A story piece set down outside the House is furniture. It keeps its look, collision and light; it does not run its story (no
pages turn, no one comes up the stairs). Using it does nothing (a held block is still set against it). It breaks like an ordinary
block, by hand, and drops itself in the kind it was; a block the House built stays as hard as the House. A piece cannot be set
down inside the House ("It does not belong in here.").

How: the server keeps, per level, where readers set pieces down (`PlacedPieces`); only those break or refuse use. The client lets
any piece outside the House's dimensions break, and the server puts back whatever it refuses.

## The signature piece, per story

Chosen by hand: the one object a reader would carry out of the room. Every story with blocks of its own has a set and a keepsake (0.4.759; the back bedroom and the waxworks came first, in 0.4.758):

| Story | Keepsake | Its line |
|---|---|---|
| The pattern | `nursery_wallpaper` | The pattern off the nursery wall. |
| The Pell house | `pell_thing[floor_lamp]` | The lamp from the corner of the Pells' lounge. Its shade was mended twice. |
| The list | `list_fitting[gin]` | A piece of the gin at the edge of Lamesa. There is still lint in its seams. |
| Bike night | `calder_fitting[bench_cut]` | A bench from the park in Calder, with initials cut into the seat. |
| Fallow | `fallow_fitting[pump]` | A pump from the station on the county road. The last price is still on its face. |
| A sort of thing | `king_fitting[chessmen]` | The chessmen from the King's table. One piece was always missing. |
| The ones already inside | `lodging_fitting[number]` | A number off a door in the lodging house. There were more rooms than numbers. |
| A little longer | `longer_fitting[kettle]` | The kettle from the back of Rowan's shop. |
| Second person | `sp_fitting[book]` | The book from the flat above the launderette. |
| A place for you | `pfy_fitting[kettle]` | Mara's kettle. Two cups were always set out beside it. |
| After the people lights | `people_fitting[newel]` | The newel post from the foot of the stairs Stan built. |
| The last swimmer | `swimmer_fitting[pace_clock]` | The pace clock from the end of the pool. |
| Vacant possession | `vacant_fitting[docket]` | The docket from the empty flat. Every line on it is ticked. |
| The left-right game | `left_right_fitting[payphone]` | The payphone from the side of the road where the game was played. |
| Dead air | `dead_air_fitting[crt]` | A monitor from the studio the broadcast came from. |
| Work print | `work_print_fitting[splicer]` | The splicer from the edit bay where the work print was cut. |
| The yellow balloons | `polaroid_fitting[album]` | The album from the end of the trail. Its last pages are empty. |
| Polybius | `polybius_fitting[cabinet]` | A cabinet from the arcade in the strip mall. |
| The replacement | `replacement_fitting[mailbox]` | A mailbox from the street. The name on it was painted over. |
| The median | `state_route_fitting[mile]` | A mile marker from State Route 9. |
| A deer in the road | `deer_road_fitting[sign]` | The deer crossing sign from the road. |
| The north tower | `lookout_fitting[fire_finder]` | The fire finder from the north tower's cab. |
| Yellow dust | `silo_hatch` | The hatch from the top of the silo. |
| The third count | `dare_fitting[plaque]` | The plaque off the third-floor bathroom door. |
| Feet up | `feet_up_fitting[rules_page]` | The page the rules of the game were written on. |
| The kept | `kept_fitting[chandelier]` | A chandelier from the machine's halls. There is frost on it. |
| The city on the ice | `city_fitting[logbook]` | The logbook from the camp below the pass. |
| The house on the corner | `corner_fitting[jar]` | A jar from the house on the corner. |
| The drawing | `drawing_fitting[box]` | The box the slips were drawn from in the square. |
| The supper | `marsh_fitting[bell]` | The supper bell from the Marshes' porch. |
| Growing things | `growing_fitting[spade]` | The spade from the garden by the back steps. |
| The last crossing | `crossing_fitting[nameplate]` | The nameplate off the flood station's pump. |
| The weekend at Pinehook | `pinehook_fitting[recorder]` | The recorder from the weekend at Pinehook. The tape is still in it. |
| Last orders | `bar_booth[candle]` | The candle from a booth in the bar. It was lit at last orders. |
| The name beside mine | `borrasca_fitting[marker_one]` | The first marker from the trail above Drisking. |
| The house in the third lake | `lake_house_fitting[key_bowl]` | The key bowl from the hall of the house in the third lake. |
| The upper ledger | `winchester_fitting[clock]` | The hall clock from the house that was never finished. |
| The empty chair | `seance_fitting[album]` | The family's album from the room where they sat. |
| The coffin lid | `usher[candelabra]` | A candelabra from the house of Usher. |
| The last clock | `masque[mask]` | A mask from the seventh room. |
| The closing account | `hotel_prop[typewriter]` | The typewriter from the caretaker's office. |
| The tape and the cut | `ghosts_set[camera]` | The camera the house was filmed with. |
| The last finger | `cottage[chessboard]` | The chessboard from the cottage on the mill road. |
| The thirteenth witch | `costume_prop[candy_bowl]` | The candy bowl from the contest on the beach. |
| The flashes over water | `movie_prop[projector]` | The projector from the dock. |
| The dare | `winter_prop[snowballs]` | A heap of snowballs from the near bank. |
| The looping field | `roadhouse[neon_beer]` | The beer sign from the window of the Last Stop. |
| The broken light | `elk_tape` | A strip of the tape from the fan room. |
| The food in the hollow | `rabbit_carved_trunk` | The trunk with the carving, from the hollow. |
| The small passage | `cave_marks` | The marked rock from the cave. |
| The cover | `well_cover` | The cover off the well. |
| The trailer | `trailer_supper_table` | The supper table from the trailer. |
| The readers | `zampano_cabinet` | Zampanò's cabinet of drawers. |
| The call button | `night_ward[call_dome]` | The call light from over a bed on the night ward. |
| The cellar shift | `cellar_flesh` | From the cellar wall. |
| Twenty seconds | `backseat_car` | The back seat. |
| The disappearing exits | `nursery[music_box]` | The music box from the bedroom with no way out. |
| The recorded voices | `crimson_floor` | A board from the crimson hall's floor. |
| Return to sender | `pigeonhole` | Box 7 from the post room. |
| The passing boots | `yacht_porthole` | A porthole from the yacht. |
| The service road | `camp_session[initials]` | The plank from the mess hall, with initials cut into it. |
| The window | `tract_house[family_photo]` | The family photograph from the model home. |
| The lake | `school_desk` | A desk from Indian Lake High. |
| The study | `harrigan_finish[plaque]` | The plaque from Mr. Harrigan's casket. |
| The floor | `loose_floorboard` | The loose board from the floor. |
| Not yet | `not_yet_fitting[chart]` | The chart from the foot of the cot. |
| Last seen | `last_seen_fitting[bench]` | A bench from the park. |
| The back bedroom | `cold_room_fitting[crucifix]` | Off the nail over the bed in the back room. It hangs the right way up. |
| The night shift | `waxworks_fitting[plinth]` | The stand under the twenty-fourth figure. Nobody painted a plaque for it. |

Stories built only from the House's shared blocks or from vanilla ones (the floor, the boys at the shallows, the distant frame and
others) have no set. Two-high blocks (doors, wardrobes) and blocks built of parts (signs, the seesaw, the ramp, the fan's ladder)
are left out for now: one item would place only a part of them. A few blocks that run a scene rather than furnish it (the two
roads' anchors) and Drisking's names are skipped by hand.

## Multiplayer

- Each reader's keepsake and set are their own: finishing together gives each of them one keepsake and their own recipes.
- A reader who joins a story partway or watches its end (`resolveAs`) gets both too.
- Spectators get nothing.
- A reader who loses the keepsake can cut another at a stonecutter, since the piece is in their set.

## Building it (slices)

1. **The engine (0.4.758, built)**: `StoryPieces` reads `the_oldest_house/story_pieces.json` (story, block prefixes, skips,
   keepsake, line); every matching block with no item of its own gets a `PieceItem`, each kind of a fitting is its own piece
   (carried as the stack's block state), and the stonecutter recipes are written as recipes load (`RecipeManagerPiecesMixin`),
   so no recipe files are kept. `StonecutterPiecesMixin` filters the list on both sides by the reader's recipe book;
   `PieceBreakMixin` and `PlacedPieces` make set-down pieces ordinary furniture. Item models all point at
   `item/story_piece`, drawn by `client.PieceClient` as the block the stack places; `tools/write_story_piece_models.py` writes
   them (`--check` in the quick gate). Names come from the kind (`kitchen_table` is "Kitchen table") unless the language file has
   `piece.the_oldest_house.<block>.<kind>`. Five cases in `StoryPiecesTests` (namespace `_pieces`).
2. **The manifest for every story (0.4.759, built)**: 69 stories, 595 blocks and about 2,150 pieces; exclusions as above.
3. **Review**: eye-level frames of each set's pieces in an Overworld test plot; fix pieces that look wrong as items or outside.

## Defaults the owner may want to change

(All built as listed; any can be changed.)

- Stonecutter rather than crafting table.
- One piece from one vanilla block (cheap); could instead cost more for big fittings.
- Not owner-bound.
- Unlock on finishing as either actor or witness.
