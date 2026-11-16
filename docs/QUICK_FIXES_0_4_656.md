# Quick fixes: the King's side hedges, faces that drew nothing, the elk's eyes, the girl from the wardrobe (0.4.656)

The owner's notes after 0.4.650, in the order they came. No Witness, layout or protocol change.

## Through either side hedge, the King's asleep ending

The owner: "Walking through either side hedge skips the Sleeping King's asleep ending. ... The passing test teleports the player
beyond the hedge and calls the handler directly, bypassing normal server routing."

- **Why.** The King's scene ended at its side hedges (its room's bounds, `IndianLakeRooms.bounds`). A reader who walked through
  one as a line was in no scene at all, so his rule never looked at them: they were never put back and never left him his sleep.
  The case for it put the reader past the hedge and called the rule itself, so it never saw that.
- **Now.** The scene holds a reader six blocks past either side hedge (`KingSite.OFF_BOARD`), as it already held them past the
  far one, and the outdoor bounds put a reader back only beyond that (`OutdoorBounds`). Off the board the King's own rule acts,
  as it does past the far hedge: one who met him, heard the brothers and was in check leaves him his sleep; anybody else is put
  back.
- **The case.** `KingTests.walkingOffLeavesHimHisSleep` now goes through the server's own routing. The reader is put just past
  the east hedge and then the west one, and the scene's own quarter-second tick finds them there and puts them back; then,
  having met him, heard the brothers and been in check, they walk from the last square through the east hedge as a line, and
  come to in the drawing-room with the account ready. Nothing calls the rule directly.

## Faces that drew nothing

The owner: "The Last Crossing's frayed coil loses two faces in 0.4.650. I confirmed that both east-facing strips sample fully
transparent pixels in frayed.png. The local model check passed because it checks texture bounds and flicker."

- **Why.** 0.4.651's check for faces that draw nothing passed over any face less than a pixel across (`flicker.SLIVER`), so the
  half-pixel sides of a card, a rope, a note or a frayed end were never looked at. At a twentieth of a pixel it finds ten models
  in the pack with sides that drew nothing.
- **Now.** `flicker.SLIVER` is a twentieth of a pixel, and `flicker --check` finds no face in the pack that draws nothing. Each
  of the ten is fixed in the tool that draws it:

| Model | Faces that drew nothing | Now |
|---|---|---|
| The Last Crossing's coil (`crossing/coil_3`) | three sides of the frayed end | each side shows strands, the cut row where they part (`draw_last_crossing_art.py`) |
| Third Floor Girls' note (`dare/note_0`) | its four edges | its own edge rows and columns, turned to run along them (`draw_dare_in_the_dark.py`) |
| People Lights' cards (`people/cards_0`) | the stack's four sides | the cards' own edges (`draw_people_lights_art.py`) |
| Pinehook's release line (`pinehook/release_0`) | the line's two sides | the rope's full row (`draw_pinehook_art.py`) |
| Borrasca's shop tags (`borrasca_shop/doc_tags_0`) | the sheet's four sides | none: the tags are cut out of their sheet, whose edge runs through clear picture, so they lie flat without sides (`draw_borrasca_town_art.py`) |
| The barn's workbench (`farm_prop_workbench`) | two faces of the vise's jaw | a span off the picture slides back onto it whole and keeps its size; until now each end was clamped alone, which squeezed the sides of everything on the bench and the potting bench (`draw_barn_well.py`) |
| The Sphinx's survey plate (`sphinx/survey_0` to `_2`) | its four edges | the plate shows its picture one pixel to one, eight by ten of brass with its rim round every edge; until now the whole picture was squeezed onto its face and its edges fell on the clear margin (`draw_sphinx_fittings.py`) |
| The work print's section (`work_print/section_0`) | its top and bottom | its picture's own edge rows (`draw_work_print_art.py`) |

## The elk's eyes

The owner: "The deer, too. You see how its glowing eyes are on the front of its head while it still has its normal black eyes
on the side?"

- The glow (`LiteraryRenderers.ElkEyes`) was a pair of slits on the front of its head, where an elk has no eyes. It now lies over
  the eyes painted on each side of the head, a pixel and a half square and two pixels back from its front, standing a tenth of
  a pixel off the side so it is drawn over them. From in front, what shows of it is the edge of each eye.
- The painted eyes are mirror-symmetric now: the right eye sat half a pixel nearer the front than the left, so no one pair of
  mirrored boxes lay on both (`tools/paint_character_skins.py`, `elk_skin`). The four elk sheets change only at that eye; their
  size and opacity are kept (`art/inventory/elk_eyes_04656.json`).
- Only an elk whose eyes are lit for its reader is drawn with the glow: the herd in the field beyond the Last Stop, and the elk
  on the stair (0.4.655).

## The girl from the wardrobe

The owner: "Can you improve the texture for the ghost girl from hide and clap?"

She was painted by `tools/generate_clap_assets.py` with a grain finer than her model's pixels: a face of two slits and a line,
arms and legs all one tone, and a nightdress whose cloth was later swapped for a tile of the cast's material sheet
(`tools/generate_cast_skins.py`) that read as sand. She is painted now by her own tool, `tools/draw_clap_girl.py`, at the
cast's grid: two pixels to a unit of her model, doubled to her sheet, so every pixel painted on her is a 2 by 2 square of it.

- **Her face.** Narrow, framed by long dark hair that falls straight past it on both sides, with bangs cut straight across her
  forehead; dark eyes with a little shadow under them; a small mouth a little open. Nothing on it is drawn as a wound.
- **Her hair.** Lighter strands through the dark. From the side it hides her cheek; behind her it falls past her shoulders onto
  the back of her nightdress, its ends uneven.
- **Her nightdress.** Cotton gone dull with keeping, gathered under a smocked yoke with a little lace at the neck and buttons down
  the back, soft folds to the hem, the hem and the skirt above it dirty from the floor of a wardrobe; short puffed sleeves with
  lace at the cuff.
- **Her arms and legs.** Pale, cooler and a shade darker than the cloth so they do not read as more of it, a little shadow at the
  joints, dirt at the fingertips, the shins and the feet; her soles dark.
- **Her neck.** Her head stood a pixel above her shoulders and her hair showed through the gap under her chin. Her model gains a
  neck, its last piece, so no other piece's place on her sheet moves (`tools/generate_clap_assets.py`, `ClapGhostModel`).
- `tools/generate_cast_skins.py` no longer paints her.

Her sheet supersedes only its own 0.4.100 hash (`art/inventory/clap_girl_04656.json`). `tools/verify_character_art.py` checks
that it is on the cast's grid, that her opacity is exactly her model's nets, each filled to its corners, and that without her
neck's net it is the old sheet's.

## Checked

- The King's cases, 14/14, the walk case among them; and the walk case against the code before this change, where it fails.
- `flicker --check` and the pack scan at a twentieth of a pixel: ten models with faces that drew nothing before, none after.
- `verify_character_art.py` and `paint_character_skins.py --check`.
- Review frames of the ten models, each from four sides and from a little above (the flat ones from low, so their sides
  show), the survey plate at all three of its stages, and the potting bench with them: every side draws its picture.
- Review frames, in a client and a world: the girl whole from in front, three-quarters, the side and behind, and her head from
  in front, three-quarters and the side; the elk whole, and its head from in front and from both sides, by day and by night;
  the stair's creatures in a row by day, and the girl, the elk and the cousin by night.
- Build, package, branding and quick checks; architecture, writing, cast and the client proof.

## Not covered

- The girl and the elk were seen on open ground, not in the wardrobe's room, the field or on the stair.

## New lines

None.
