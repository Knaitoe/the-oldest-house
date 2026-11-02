# The Wheel, Movie Night and Devil's Rock (0.4.241)

The owner's notes of 2 November, after 0.4.240. No protocol, layout, Witness or source-kind change.

## The Wheel

- **"don't have this exit door in mother's room. the vignette requires player to take the other door."** The scene's way on into
  the halls stood in the north wall of Mother's room, so a reader could leave from there without going through her door, the one
  with no number, which is how the Wheel ends. The way on now stands in the hall inside the front door, in its east wall
  (`OnwardPlacements`: (5,0,-5), approached from the west), where the reader comes out after Mother.
- **"weird fence posts on wheel entrance?"** Four oak fence posts stood loose against the hall's side walls, a block up, on
  nothing. They are gone.
- **"blocks in wheel have space between them."** The nursery's letter blocks are thirteen sixteenths high, so the one stacked on
  another floated three sixteenths above it. A stacked block (`LifeStageBlock.Kind.TOY_STACKED`, drawn by
  `tools/draw_wheel_house.py`) is drawn three sixteenths down into its cell and rests on the one beneath.

## Movie Night

"for movie night, something should implore the player to check the first vignette so they can complete it after running to the
grass." The program ("Tonight on the lake") waits on its stand by the door the reader came in at, and the account at the ticket hut
answers it; a reader who took a canoe straight out and came round to the pier and its grass found an account that would not
finish, a lake away from what it wanted.

- Ray calls after a reader who takes a canoe out without a program: take one off the stand by the door, you'll want it on the far
  side.
- Dot, at the hut, sends a reader who has come round without one back for it, and says the path round the shore is quicker than it
  looks; talking to her says it again.
- Everywhere, the line that says the last account is here now names the first paper when the reader has not read it
  (`LiteraryVignettes.readyLine`).
- The audience's words go to the action bar, not chat (speech is never chat).

## Devil's Rock

**"try to remove obviously ai-textures from devils rock"**, with screenshots of the bear on Kate's floor, the framed MISSING
poster and the cassette. These three props were cut from a generated sheet (`tools/generate_review_assets.py`): 64x64 crops of a
painted picture, and every face of every box showed the whole picture squeezed onto it. They are drawn again here, pixel by
pixel at the game's scale, by `tools/draw_devils_rock_props.py`, and each face takes its own part of its texture:

- the bear sits, with a face only on its front, a tan muzzle and belly, paw pads and ears;
- the poster is a photocopied MISSING notice, the boy's photograph in grey, lines of type, tape at the corners, on a board;
- the cassette lies flat, its label, window and two reels;
- the two wood textures those props shared with the other generated props (the notice and map boards, the chess board, the diary
  stack, the flashlight) are plain stained boards and weathered boards now.

The four replaced crops were among the historical PNGs `tools/verify_character_art.py` holds to their 0.4.100 hashes;
`art/inventory/devils_rock_props_0241.json` records each one's old hash and its new drawing, and the check holds them to that.

**"add more family details to the house, though. in devil's rock. give it the same lived-in nature as the barret home"**, and then:
"Don't reuse the barrets household pieces. I meant to look to it for design inspiration." Nothing here is the Barretts'. The
Sandersons have their own pieces (`tools/devils_rock_household.py`, `VignetteDetailBlock` kinds), most of them about the weeks
after Tommy went:

- by the door, the coat pegs with Elizabeth's parka, Kate's hoodie and his denim jacket still on its peg, and a hall table with
  the mail nobody has opened;
- in the kitchen, the fridge (in place of two iron blocks) with his flyer under a magnet on the freezer door and a school
  schedule below, the neighbours' foil-covered casseroles on the counter, the calendar with the days crossed off and one ringed,
  and Elizabeth's cork board: the park map with its pins, his flyers, the detective's card;
- in the living room, a coffee table with the newspapers, a mug and the tissues, a basket of laundry by her chair, a plant nobody
  waters, the three of them at a lake one summer, and his school photograph;
- in Tommy's room, his poster, his skateboard against the wall under it, his jeans and hoodie on the floor;
- in Kate's room, her paint jars and palette on the desk and her flowers taped to the wall;
- in Elizabeth's room, a basket of laundry, and on the drawers a snapshot of Kate and Tommy pulling faces.

The loose pages' landing cells, the gap between Elizabeth's chair and the end table, and the cells inside every doorway are left
clear. `ModelUvProof` (`-PuvSmoke`) draws a second page, `native-devils-rock-props.png`, of every redrawn prop and household
piece; `tools/draw_devils_rock_props.py --check` is in the quick gate.

## Tests

The Devil's Rock diary case, the Wheel case and the Movie Night audience case (`VignetteAnimationTests`) pass locally; the
quick-gate checks (textures, character art with the new record, model particles, the Wheel and Devil's Rock drawings) pass.
