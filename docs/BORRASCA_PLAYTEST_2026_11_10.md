# Playtest notes, 10 November: Borrasca, Last orders, the Sphinx

The owner's document of 10 November ("maybe give the different pumps different last prices"), 43 screenshots with a note under or
beside each. Every note is quoted as written. Under each is what the screenshot shows and what is being done about it. The status
column is kept up to date as the work lands.

Two standing remarks run through the whole document and are treated as passes of their own, not single fixes:

- "Borrasca needs a custom texture pass"
- "Give the different stores some variance/characterization in their signage"

## The notes

| # | The owner's note | What the screenshot shows | Status |
|---|---|---|---|
| 1 | "maybe give the different pumps different last prices?" | A gas pump. The line under it reads "The dials stopped at the last sale: 9 gallons, 9.81." Both pumps say the same. | done, 0.4.444: each pump has its own last sale |
| 1 | "Borrasca needs a custom texture pass" | (the pump's flat red body and blocky dial) | done: 0.4.454, the pump is built of parts, in enamel, chrome, glass and rubber, and the two pumps' wheels stand differently; the price board shows the later prices to a grown reader (docs/BORRASCA_FITTINGS_0_4_454.md), with the pews and every seat; 0.4.460, everything that was still one of the game's own blocks standing for something else: a stove and its pipe, a griddle, a fryer, sinks, a steel toilet, buckets, steel drums, boxed effects on shelving, a crane's hook, a counter bell that rings and a jukebox (docs/BORRASCA_STAND_INS_0_4_460.md) |
| 2 | "Facing the wrong way. Give the shelves more variance?" | The drugstore's pharmacy counter: the cash register's keys face the customer. The wall of shelves behind it repeats one texture and reads as a bookcase. | done, 0.4.444: the registers' keys face their keepers; the pharmacy shelves are bottles, in two arrangements |
| 3 | "Arcade machines have gap in center?" | The side of a cabinet: a dark seam where its upper and lower halves meet. | done, 0.4.444: one side panel from the floor to the top |
| 4 | "Needs an improved texture." | The comic spinner: a grid of flat coloured squares. | done, 0.4.444: a spinner with a pole and wire pockets of comic covers |
| 5 | "Give it an actual sign" | The historical society's sign, a vanilla wooden sign. | done, 0.4.449: the society's own painted board over its door, DRISKING between crossed picks |
| 6 | "Mirror on window?" | A wall clock hung in a window opening, on the glass. | done, 0.4.444: wall behind every piece that hangs on a wall (a test now checks the whole town) |
| 7 | "open sign eating glass, also unfinished" | The OPEN sign takes the place of a window pane, and its lettering is rough. | done, 0.4.449: each shop has its own sign in its window, inside the glass: red tube at the cafe, a lit box at the drugstore, a card on a string at Rowe's, DOCTOR IS IN at Dr. Hale's |
| 8 | "Give the different stores some variance/characterization in their signage" | The same OPEN sign in another shop's window. | done, 0.4.449: every shop has its own painted board on its false front, in its own colours and at its own size, and so have the store, the Sheriff, the church and the rest (docs/BORRASCA_SIGNS_0_4_449.md) |
| 9 | "Try to show, don't tell. Try to design an actual ui for this" | The birth cards on the doctor's corkboard, described on a page of text. | done, 0.4.455: the corkboard opens as a board, with the cards pinned to it; a click takes one down to read, the envelope turns over in the hand, and nothing is written on the screen but what is on the cards (docs/BORRASCA_CORKBOARD_0_4_455.md) |
| 10 | "This ain't exactly the candy jar you promised lol" | The lollipop jar: a glass box of coloured blocks. | done, 0.4.444: a glass jar with a lid and lollipops on sticks |
| 11 | "again, custom textures would be beneficial here." | The church's pews, built of plank stairs. (I first recorded this as a waiting room's benches; the church's board shows through the window.) | done, 0.4.454: oak pews with shaped, carved ends and hymnals in the racks; and every other seat in the town that was a stair is a bench of slats on cast iron |
| 12 | "You could add inflections of personality into the citizens homes. Remember that this town is sick, that many know what the town does and why and just accept it. Some don't, but they have to bite their tongue." | A house: a bed, drawers, an armchair, a table. Nothing says who lives there. | done, 0.4.456: the street's four houses are four households, each told by what is in its room (a school picture and a crib; a cross and the family Bible; a new television and a PRESCOTT jacket; a packed suitcase, a road atlas and a frame on its face), each with a line that says what is seen and not what it means, and each the same household years on (docs/BORRASCA_HOMES_0_4_456.md) |
| 13 | "Keep prison cell door shut. Allow player to speak to npc through bars." | Earl in the cell with its door standing open. | done, 0.4.444 |
| 14 | "Try to fix wheels" | A child's bike: each wheel is eight separate blocks with gaps between them. | done, 0.4.447: each tyre is a closed ring of sixteen pieces, with eight spokes |
| 15 | "Try to fix mild visual distortion when mounting bike." | The rider's view: the handlebars fill the top of the screen and a dark bar crosses the middle of it. | done, 0.4.447: the rider sits on the saddle; their eyes had been level with the bars and the saddle's top |
| 16 | "Names should be easier to read." | The names cut in the Tree, dark on dark bark. | done, 0.4.444: paler, larger, bolder, and never darker than a lit evening |
| 17 | "The tree is old. So is the town and its secrets. There should be names all over this tree." | The trunk beside the carving: bare bark. | done, 0.4.448: names on every face of the burl and the trunks, two to a face, in three ages; Patrick's and Kendra's beside the carving, scraped off as they go |
| 18 | "Dialogue should change on second day, after Kelly/friend is taken" | The Sheriff, after she is taken: "You two keep this side of Marker Two when the wind comes up." | done: 0.4.444 for the Sheriff; 0.4.458 for the rest of the town, each of whom has their own lines for a child who came down alone, and Mrs. Odell gives a second lollipop against her rule (docs/BORRASCA_SECOND_DAY_0_4_458.md) |
| 19 | "He shouldn't still be in jail upon returning as an adult. Make him a vagrant on the street instead." | Earl in the cell, years later. | done, 0.4.444: on a flattened box at the mouth of the alley by the Tipple |
| 20 | "retexture this table and all instances of it. It's got an hd texture that isn't in line with vanilla" | A small table with a finely detailed, scratched wood texture. | done, 0.4.452: it is the reading desk, which was on a 64-pixel sheet made for another room; it has three sixteen-pixel pictures of its own now, and every reading desk in the mod changes with it (docs/READING_DESK_0_4_452.md). 0.4.453: the rest of the household furniture, which was on 32-pixel pictures, is drawn again at sixteen (docs/HOUSEHOLD_FURNITURE_0_4_453.md) |
| 21 | "try to add the faces of the actual children, especially if they're npcs from the vignette" | The LOST posters: a grey outline of a head. | done, 0.4.451: Patrick's and Kendra's own faces on their sheets, read off their skins; Dana's, Kelly's on the board (docs/BORRASCA_STORE_FRONT_0_4_451.md) |
| 22 | "Cluttered" | The noticeboard: four copies of the same corkboard tile. | done, 0.4.451, read as "make it a cluttered board": one board two blocks by two under the town's own clutter, a different picture on each visit to match its pages |
| 23 | "Could benefit from custom signs" | The trailhead arch: two small vanilla signs on its beam. | done, 0.4.449: the trail's name burned into a plank seven blocks long on the beam, and the distance on a board that hangs under it on chains |
| 24 | "Combat with the sheriff shouldn't' feel like a vanilla Minecraft fight. Punches should be sluggish with real weight. The sheriff should use his baton, then drop it after being hit a few times and start swinging his own fists. He swings with one fist then the other." | (no screenshot of the fight) | done, 0.4.457: he has his baton out, each swing raised before it comes down; the third blow that lands on him has it out of his hand and on the ground, and he throws the left fist and then the right; the reader's arm is slow and a part-drawn blow glances; a landed blow knocks him back and he reels. Not yet played by hand: the timings are the owner's to judge (docs/BORRASCA_SHERIFF_FIGHT_0_4_457.md) |
| 24 | "Sphinx talons still need three distinct nails, not the many several smaller ones." | The Sphinx's paw: a row of many small claws along three toes. | done, 0.4.447: one toe and one talon to each of the three blocks of her paw |
| 25 | "Custom booths in last order" | Last orders: booths built of planks and red wool, numbered with vanilla signs. | done, 0.4.450: panelled partitions under reeded glass and a brass rail, a post at the aisle with the booth's number on brass, a candle in red glass on each table and a picture in each booth (docs/LAST_ORDERS_BOOTHS_0_4_450.md) |
| 26 | "Custom signs for last orders" | The league table: nine vanilla signs. | done, 0.4.450: a slate four blocks by three, the weeks chalked into its squares by the game, a lamp in front of it |
| 27 | "Lol was this intentional, in last orders?" | Two regulars in a booth, sunk so that only their heads show at the table's edge. | done, 0.4.447: not intended. Their lean shared a number with Costume Night's crawl and they were drawn on all fours |
| 28 | (no note) | A noticeboard and a blank board hung across a window. | done, 0.4.447: it was the Drawing's board on the post office; it hangs on the wall beside the window. Borrasca's gas station payphone was on glass too and has moved |
| 29 | "Sheriff continues to wiggle after "death" pool blood around corpse?" | The Sheriff's body on the ground. | done, 0.4.444: the body is still, and a pool spreads under it |
| 30 | "She should spawn on this on day 4, not spawn elsewhere then walk onto it. You should have operators that are watching her go in, referencing the shiny gentleman taking care of her." | Dana lying on the belt. | done, 0.4.444 |
| 31 | (no note) | The yard: plank slabs, one of them hanging in the air. | done, 0.4.447: every board in the yard rests on the ground or on the board under it |
| 32 | "Documents are not interactable." | Papers on the desks in the depot office. | done, 0.4.444: six more documents, read where they lie |
| 33 | "re-use water fountain from personhood vignette." | A water cooler made of a white box and a blue one. | done, 0.4.447: the waiting room's cooler stands in the depot office |
| 34 | "Give new textures to the different docs." | Two documents side by side with the same face. | done, 0.4.444: ten papers, ten faces |
| 35 | "Kelly's dialogue text appears for like, half a second" | Kelly, grown, in room 6. | done, 0.4.444 |
| 36 | "Custom signs, wall texture" | The corridor under the depot: vanilla signs for the room numbers, plain walls. | done, 0.4.449: a steel plate with the number and a card slot over each door (rooms 6 and 7 have their cards in), and the corridor's walls are painted block, green below a line and cream above |
| 37-39 | "No entry" | The passage between the depot and the wire, a lantern on a fence post, and a ladder up a closed timber box. | answered both ways, 0.4.449: NO ENTRY on the wire either side of the gate, the disc at the mouth of the passage and by the tank's ladder; and the tank's ladder now reaches an open hatch, with the tank dry and red silt on its floor. The depot's door and lever say why they do not open (0.4.447). Whether the note meant something else is asked of the owner |
| 40 | "End of the world here lol." | Past the yard's fence the ground stops and there is nothing. | done, 0.4.447: the hill goes on behind the yard and ends in the cliff at the valley's head |
| - | "Borrasca shiny gentleman capture spawns on top of triple tree rather than inside" | (no screenshot) | done, 0.4.444: on the fort's floor |
| 41 | "Spots to solids" | Last orders' pool: "knaitoe (stripes) vs Booth four (spots)". | done, 0.4.444 |
| 42 | "Two dorseys" | The mail carrier, and a second one behind him. | done, 0.4.444 |
| 43 | "Have this whole front of house filled with lost papers after vignette has ended – after the orrery has either been fed or stopped." | The store front with its noticeboard and two LOST posters. | done, 0.4.451: once Dana is fed to the machine or the breaker is thrown, forty-two LOST sheets cover the front's boards and both windows' glass and the board is covered; the doorway is clear |

## Order of work

1. What is broken: Kelly's line in room 6, the two carriers, Earl's cell and Earl grown, the Sheriff's line after the taking, the
   Sheriff's body, Dana on the belt and the men watching, the depot's papers, where the Gentleman leaves a child, "solids", the
   register, the clock, the OPEN sign, the boards over the window, the hanging slab, the ground's edge, the pumps' dials, the
   cabinets' seam, the regulars' seats, the bike, the names on the Tree.
2. The texture and sign pass, shop by shop.
3. The larger pieces: the corkboard as a screen, the homes, the fight with the Sheriff, the store front after the ending, the
   Sphinx's paw.
