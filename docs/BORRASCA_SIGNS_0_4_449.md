# Borrasca: the town's signs (0.4.449)

The owner's notes this answers (docs/BORRASCA_PLAYTEST_2026_11_10.md):

- 5, of the historical society's wooden sign: "Give it an actual sign"
- 7, of the OPEN sign: "open sign eating glass, also unfinished"
- 8: "Give the different stores some variance/characterization in their signage"
- 23, of the two small signs on the trailhead arch: "Could benefit from custom signs"
- 36, of the corridor under the depot: "Custom signs, wall texture"
- 37-39: "No entry"

## What is in the town now

Every sign in Drisking that was one of the game's wooden signs is its own painted board. The lettering is in the picture, sixteen
pixels to the block as the game's own blocks are, in a five by seven alphabet or a three by five one.

| Sign | Where | What it is |
|---|---|---|
| Hilltop Cafe | the false front, six blocks by two | cream boards; HILLTOP in tall red letters with a shadow, CAFE under it between a cup and a slice of pie |
| Rowe's Antiques | the false front, six long | bottle green, gilt letters, a gilt line round it |
| Ridge Service | the false front, six long | white enamel between two red bands, blue letters, a red diamond at each end, rust at the bolts |
| R. Hale, M.D. | six long, low over the awning | a white board, a thin black line, plain black capitals |
| Main Street Drug | the false front, six by two | a teal band lettered MAIN STREET, DRUG in tall letters under it, a mortar to one side and the Rx to the other |
| The Tipple | the false front, six long | a dark board, THE TIPPLE in pink tube between two blue glasses. It is lit, and lights the street in front of the bar |
| Drisking General Store | the false front, nine by two | barn red boards, white block letters flaking off: the name, FEED & HARDWARE under a rule, EST. and 1961 at the ends |
| COLD DRINKS | the store's porch wall | blue tin |
| Sheriff | over the office door, five long | green and gold, SHERIFF between two stars |
| Drisking Historical Society | over its door at the back of Rowe's, five by two | cream with a double line of oxblood, DRISKING between crossed picks, OPEN SATURDAYS |
| Hardware | over the pegboard in the store, four long | yellow, black block letters |
| Lost & found | in the trail station | a piece of box card taped up, lettered in marker |
| The trailhead board | in front of the station, four by two between its posts | the county's routed board: the trail's name, a pine either side, Triple Tree 1 and Marker Two 2. Its frame is two blocks wider to carry it |
| The arch | on the beam where the street ends, seven long | the trail's name burned into a plank; TRIPLE TREE 1 MILE hangs under the beam on two chains, read from both sides |
| The fort | on its wall by the door, three by two | three scrap boards in children's paint: TRIPLE TREE, FORT, NO GROWN-UPS |
| Ridge Depot | over the front door under the hood, three long | a white enamel plate: the name, OFFICE, a bell |
| First Baptist Church | at the street, four by two between two posts under a cap | white boards, black letters, ALL WELCOME in red |
| Service exit | over the release behind the depot, three long | green and white |
| The trucks | across each tailgate | COUNTY 17 stencilled on the tan pickup; PRESCOTT in white with the firm's mark on the green one. Lettering only, no board |
| The families | on the post at each gate | PRUITT on white enamel, HOLLIS burned into plank, 14 WEBB in white on green, CORLEY stencilled on tin |
| The rooms under the depot | over each of the twelve doors | a steel plate with the room's number and a slot for a card. Rooms 6 and 7 have cards in: 263-A and 264-A |
| In the windows | inside the glass | OPEN in red tube at the cafe; OPEN in blue on a lit box at the drugstore; OPEN inked on a card on a string at Rowe's; DOCTOR IS IN at Dr. Hale's, two panes wide |
| NO ENTRY | on the wire either side of the depot's gate | red tin. The disc with the bar is on the depot's corner at the mouth of the passage to its front door, and beside the tank's ladder. Prescott's board on the service road says PRESCOTT, NO ENTRY |

What a sign says in full is read by a hand on it, the lines too small to letter included: the cafe's "Pie, coffee, plate lunch", the
trailhead's "The public walk ends at Marker Two. Stay on the marked trail. Return before dark", the fort's "knock first".

The numbers on the stakes behind the shed are still the game's own signs, fifteen of them. A test holds that they are the only
ones left on the hill.

## "No entry"

The note is under three screenshots: the passage between the depot and the wire, a lamp on a post behind wire, and the ladder up
the water tank, which ended under a closed box. It can be read as wanting signs or as saying that these lead nowhere, and both are
answered:

- The signs above.
- The tank. Its ladder goes on up to the lid. A hatch in the lid stands open over a ladder down the inside. The tank is dry: red
  silt on its floor, and a hand in it reads "Dry. Red silt a hand deep on the boards, and a red line round the staves where the
  water stood." The town's history at the society says the blasting broke into the water table, and "For two years the town's
  water ran red".
- The depot's front door and its lever already say why they do not open (0.4.447).

If the note meant a way in at the passage or at the lamp, or meant those places shut off altogether, that is asked of the owner.

## The corridor under the depot

Its walls are painted block: green to the height of a hand with a scuffed band along the floor, a dark line, cream above, stained
at the top where water has come down. Only the side that looks into the corridor is painted. The rooms off it are bare concrete as
they were, so the scratched names in them sit in the same wall.

## How it is made

- `BorrascaSigns.Sign` is the table: each sign's columns and rows, how it is fixed (on a wall; lettered straight onto what is
  behind it; inside a window; hanging free), its light, and what a hand reads. `BorrascaSignBlock` is one block of one sign:
  `piece` is which square of which picture, `facing` the way the lettering looks.
- `BorrascaSigns.hang` and `span` put a whole sign up from one corner. A block of a sign goes only where there is nothing; a sign
  that would take the place of something else stops the build and says where.
- `tools/draw_borrasca_signs.py` draws one picture per sign, the model of each block (its own sixteen-pixel square of the
  picture; a single sign's board may reach a block past its own on each side), and the states. Neon has a second picture of its
  tubes alone, drawn full bright a quarter of a pixel in front. `--check` compares the tool's table with `BorrascaSigns.Sign` and
  verifies every file; the quick gate and the build run it.
- `BorrascaWardBlock` is the corridor's wall: `course` 0 to 2 from the floor up, `facing` the painted side
  (tools/draw_borrasca_town_art.py).
- `BorrascaShopBlock`'s OPEN fitting is no longer hung anywhere.

## Tests and proof

- `BorrascaTests.theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits` goes over the whole hill: the only
  wooden signs left are the fifteen on the graves; every board has wall or wire behind it, its window before it or its beam over
  it; every sign is hung whole (NO ENTRY and the disc twice); each shop has its own sign in its window; a hand on a board is
  answered; the corridor's wall is painted where it looks into the corridor and room 6 has its plate; the tank's ladder reaches
  its lid, the hatch is open over red silt, and a hand in it is answered.
- The Borrasca client proof has two more views, `native-borrasca-signs-0.png` and `native-borrasca-signs-1.png`: every sign hung
  by the builders' own rule from the blocks the game draws, so a sign whose blocks were out of order or whose lettering ran
  backwards would show.

## Not in this batch

- Last orders' league board and booth numbers (notes 25 and 26). The board's lines change as glasses are drunk, so its lettering
  has to be drawn as the game runs. Next.
