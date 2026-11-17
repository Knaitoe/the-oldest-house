# Painted boards (0.4.677)

The owner, 17 November, of the Last Crossing:

> there are several signs in last crossing that could benefit from custom textures. i was hoping you'd notice that on your own

The station's four signs were painted in 0.4.675. This release goes through every other scene for the same thing: the game's own wooden sign, with words typed on it in the game's font, standing in for a sign the place would really have.

## What was found

Every builder that still set a wooden sign with words on it:

| Scene | Sign | Now |
|---|---|---|
| Camp Blood | SERVICE ROAD, No camp traffic, where the sand road ends | a stained board on two posts |
| The elk lot | SERVICE ROAD, Crew only, Sign out here, on the gate post | the county's board on two posts beside the gate, SIGN OUT HERE added in marker |
| The elk lot | SECOND SUMMER, Indian Lake, on the yacht's side | her name painted on her side, with a gold rule |
| Pinehook | PINEHOOK, on a post at the apron | the name burned into a split log, on two posts |
| Pinehook | PRIVATE, NO ENTRY, beside the boathouse door | a tin plate screwed over the door, clear of both windows; the pine whose lower boughs hung over it is no longer planted, and no other pine moves |
| Movie Night | TICKETS, Movie night, on the lake, on the hut's corner post | a board with bulbs round it across the top of the hut's open front, post to post |
| Borrasca | the number on each of the fifteen graves' stakes | a tin tag nailed to the stake, the number written on it |

Camp Blood had two more, set by the pass that dresses the camp once it is built (`SceneCraft`): the gate's sign on its post (CAMP, Cabins 1 - 6, Lights out at ten) and a CABIN plate on each cabin's porch. Both are now painted and built with the camp itself, and the dressing pass no longer sets either:

| Scene | Sign | Now |
|---|---|---|
| Camp Blood | CAMP, Cabins 1 - 6, Lights out at ten, on a post at the gate | a stained board routed with CAMP between two pines, on two posts east of the road, across from the flagpole |
| Camp Blood | CABIN 1 to 6, over each porch | a short stained plank by each cabin's door at eye height, its number routed into it |

Her calendar at the Whalestoe institute was also a wooden sign in its builder, but the ward's fitting-out already takes it down and puts the notebook in its place as each institute is built, so no reader ever sees it. It is left as it was.

The longer notices are still the game's own: Holloway's three notes at his camp, the seven labels over the archive's drawers, and the road crew's notice at the elk lot. They hold too many words for a picture at sixteen pixels to the block; the next release letters them over painted plates in the game's own hands.

## How they are made

As the station's are. `tools/draw_board_signs.py` draws each picture at sixteen pixels to the block, lettered in the picture, and hands it to `tools/draw_borrasca_signs.py`'s pieces, models and states, pointed at its own folder (`board_sign`). A sign more than a block wide is a grid of `board_sign` blocks, each showing its own square (`BoardSigns`, `BoardSignBlock`, `BoardRegistry`). A board standing free, between posts, shows the picture from both sides. Nothing walks into a board, and a hand on one reads it.

## What a hand on each reads

Every line here is new.

- CAMP, routed into two planks between the posts. Under it: CABINS 1-6. LIGHTS OUT AT TEN. **(new)**
- A 1 routed into a plank by the door, a nail at each end. (And so for 2 to 6.) **(new)**
- SERVICE ROAD. NO CAMP TRAFFIC. Moss has got into the bottom of the letters. **(new)**
- SERVICE ROAD. CREW ONLY. Under it, in marker: SIGN OUT HERE. **(new)**
- SECOND SUMMER, painted on her side. INDIAN LAKE under it, smaller. The paint is flaking. **(new)**
- PINEHOOK, burned into a split log between two posts. **(new)**
- PRIVATE. NO ENTRY. A tin plate screwed over the door, rust run down from the screws. **(new)**
- TICKETS. MOVIE NIGHT. A row of bulbs round the board; most of them still work. **(new)**
- 211-A, written on a tin tag nailed to the stake. (And so for each grave's own number.) **(new)**

## Checks

- `python3 tools/draw_board_signs.py --check` in CI holds the pictures, models, states and name to what the tool draws, and the tool's list to `BoardSigns.Sign`.
- The all-scenes architecture case now looks through every finished scene for the game's own signs with words on them. It names any it finds; the long notices above are the only ones let stand, and the case logs them. It also counts every painted board's blocks, so each stands whole and once, and a board on a wall has the wall behind it.
- The Borrasca case holds each grave's tag to its number, and now finds no wooden sign anywhere on the hill.

## Seen

Eye-level frames from the export of each finished scene, as a reader standing there would see them: Camp Blood's gate board from the road and from inside the gate, cabin 1's plate, and the board at the road's end; the elk lot's gate board from the road and from the slope above; the yacht's name from the shore; Pinehook's name board and the boathouse plate; the ticket hut's board. The first frames came from an export that left out the camp's dressing pass, and showed the gate board and plates missing, because they had been put in that pass. They are built with the camp now, and the export dresses each scene as play does. A pine's boughs hung across the PRIVATE plate; that is fixed above. Both were looked at again. The architecture case then found the old CABIN signs, which the dressing pass still set over the new plates; it sets them no longer.
