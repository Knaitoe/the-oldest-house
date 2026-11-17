# The people told apart (0.4.679)

The owner, 17 November:

> Once you're finished, can you do a pass over each vignette and work to vary the npcs? Their faces, hairstyles, and even body compositions?

Most of the cast are painted by one tool, `tools/paint_character_skins.py`, from a short description of each person (a `Look`): skin, hair and its cut, clothes, eyes, age, a beard. Until now every face the tool drew had the same brows, the same set of the eyes, the same nose, mouth and jaw; the cuts came from a short list, so a scene of six often had three people in the same short hair; and every body was the same shape. In a busy scene (the hotel, the nightclub, the King's court, the contest at Costume Night) people were told apart by their clothes alone.

This pass gives each scene's people their own faces, cuts and builds. It changes how they look and nothing else: who they are, what they say, where they stand and what they do are as they were.

## Faces

A face is now made of five parts, each with a few makes, all drawn at the eight pixels of the face:

- brows: the common straight brow, arched, heavy, thin or sloped;
- the eyes: set as before, wide apart, close together, or narrowed;
- the nose: the common one, long, broad, button or hooked;
- the mouth: the common one, wide, small, full or thin;
- the jaw: the common one, round, square, narrow or hollow, drawn in the cheek's shading at the face's lower edge.

In each scene, every person gets a make that differs from every other grown person's there in at least three of the five parts, so no two faces in a room are near each other. Children differ in at least two, and keep a child's nose and jaw.

## Hair

New cuts, each drawn on the head and on the hat layer where it stands off the head: a side part, a quiff, a buzz, a mullet, curls, a fringe, a tied-back cut, a bob, pigtails, braids, and a bald crown. Where two people in one scene would wear the same cut, the second takes the next one of its own kind the scene has not used: short hair stays short, long hair long. A cut under a costume or a cap (yarn and mop wigs, hoods, a swimming cap, a bowler, a visor, a helmet) is left as it is.

## Bodies

Each person has a build: tall, short, broad, heavy, stocky, thin, lanky, slight, or the common one. A build changes the body's height (from the feet, or from the hips when sitting or lying), the torso's width and depth with the arms set out to match, and the thickness of the arms and legs. The head is never scaled, so faces stay at the size they were drawn. No two people in a scene share a build where another would do. The older of the cast are not made tall, broad or lanky; children are given a child's builds only.

The tool writes each build to `assets/the_oldest_house/cast_builds.json`, by the person's skin. On the client, `CastBuilds` reads it, and `CastBuildMixin` applies it to any figure drawn with one of those skins after its pose is set and puts the model back afterwards, so the same model draws the next figure as it was. The cast's bent arms, which hang from the model's root rather than its body, are set out with the arms. The builds are only drawn: nobody's hit box, eye height or reach changes.

## Corrected after the first look

The owner was shown the first painting of Last Seen's people and asked, "Can you see the numerous errors here?" They were these, and each is mended across every scene the pass touches:

- Eyes. The common face set one iris to the inside of its eye, which reads as eyes turned in. The pass's people look straight ahead, both irises in the middle. (The same set was in the committed game; these faces now mend it.)
- The moustache. Its line dipped in the middle and rose at the ends, which read as a grin. It now runs straight across the lip under the nose and turns down a pixel at each end.
- Stubble is an uneven nap rather than a hard stipple.
- The earth on the missing children's cheeks. It has been there since Last Seen was built (0.4.667), on the side they lay on, but as three hard pixels it read as a blotch. It is now smeared from the cheekbone to the jaw's edge, heaviest where the face lay on the ground and thinning toward the nose, and runs on round the side of the head.
- Hair is never outlined. Long highlights are broken into short strokes; a parting is a faint line in the hair, not a notch of skin; a buzz is a close nap; curls and coils are small round clumps lit on one side, in staggered rows (pale hair drawn softer), not dots.
- A bald head keeps a horseshoe of close hair, broad behind the ears and thinning into the scalp above, not a band round the head.
- A bun is a raised knot, lighter than the hair round it, with its turn and a soft shadow under it. The diner's waitresses had their own square bun drawn over it; the knot takes its place.
- Tails, pigtails and braids are drawn at last: solid tails from a tie, pigtails behind each ear, and braids as rows of chevrons. A pale tie (taken from a white collar or a cream shirt) is blended into the hair, so it never shows as a white dash.
- The scarecrow's straw at Calder's party is his costume and keeps its strands.
- The line across the forehead. The owner, after the first look: "The line is present in Santino messy short too." The older cuts lit the hair over the forehead with straight pale bars, two dashes side by side on one row, and the pass's new cuts did the same; the bob and the fringe also ended in a dark line drawn the width of the face. On every person the pass repaints, the light is now picked out a pixel at a time along the way each cut is combed, a fringe ends in ragged tips with the shadow under every other one, and the quiff's shadow is broken. Faces kept from a moment of the story (Santino, Tomás and the other missing children among them) take the same hair, since their sheets are repainted together. The Truant keeps his approved painting, bars and all.
- A face made for a moment of the story keeps the stubble, moustache and eyes it was drawn with, as it keeps the rest of its face.

## Who keeps their look

The people designed one by one with the owner keep their faces, cuts and builds exactly: the Borrasca cast, the Ghosts set's family, the Pell house, the reader's own body in the House, the lake's witch, the boy in the cell, Tess and the families. So do faces made for a moment of the story (a dancer's despair, the family changed, the dead, the possessed, the truant's leer): a person with any such sheet keeps the common face on all of them, so that their sheets match. A build given in a person's own design notes is used as written: the second worker at Borrasca and Otis at the station are heavy; the hollow's ghoul and the four Kept are thin; the goblin is slight.

One person's sheets (the same person changed, hurt, dressed for another day) all take the same make. The King's two brothers in the wood are drawn alike on purpose, "alike as two buttons", and stay alike.

## What this changes for readers

Only how people look. Everyone in a scene sees the same faces, cuts and builds; a reader's own body is untouched.

## Checks

- `python3 tools/paint_character_skins.py --check` holds every painted sheet and `cast_builds.json` to what the tool makes.
- `python3 tools/verify_character_art.py`: the 88 sheets that were approved by hash before this pass are repainted under a new record, `art/inventory/npc_variety_04679.json`. Each row names the sheet's previous approved hash and opacity, which must match the chain of earlier approvals, keeps the sheet's size, and was written only if the sheet's opacity changed nowhere but on the head's second layer, where a new cut carries its volume.
- The client's character proof draws every person in their build, sized from the common build so a tall or broad person stands out on the sheet as in the game, and checks that every build in `cast_builds.json` is found by its own sheet, stays within what a person is, never stretches the head, and is taken off the model whole after drawing.
- Every other changed sheet was checked the same way when the pass was made; the only opacity changes off the hat layer are three ponytails whose cut changed, so their tail's own patch is now clear, as it is for anyone without a ponytail.
