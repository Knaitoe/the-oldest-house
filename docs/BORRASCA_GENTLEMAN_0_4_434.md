# Borrasca: the Shiny Gentleman out by the far marker (0.4.434)

The owner: "Ensure the shiny gentleman still appears as an actual monster out by the far marker too".

Two things stood in the way.

- **He was a picture, not a body.** Since 0.4.236 he had been one flat image, turned to face the reader wherever they stood.
- **The longer hill put him out of reach.** He could only stand 28 to 70 blocks from the child. 0.4.419 drew the hill out, and the
  second marker is now about 125 blocks past the Tree. A child at the Tree hearing the noise was too far away for him to be anywhere,
  so they heard it and saw nothing. He still came for a child past the marker, and on the ridge behind the men at the taking.

## His body

He is built from boxes now, in native pixels like the rest of the cast, and painted on one atlas by
`tools/draw_shiny_gentleman.py`. The tool keeps its old name and texture path; it paints the faces of the boxes instead of a flat
figure.

- A tall hat bent over at the crown, grown into his head under a narrow brim.
- A long smooth oval with no face, and one bright band across it where the light catches.
- A neck like a stalk.
- Shoulders too wide over a narrow coat that falls to a ragged hem.
- Legs a pixel wide.
- Arms that hang out from the coat, bend out again at the elbow and reach past his knees in four long hooked fingers.

He shines like wet metal, lit hard from the viewer's left and dark on the right, with streaks of light down the coat and a few glints.

He is not still. He sways a little, his head lies over to one side and rocks slowly, and his fingers curl and open. He fades in with
the first stroke and out as the last one dies, as before. He is still the reader's alone: nobody else sees him, and he is drawn on
their screen where the server says he stands. `client.BorrascaGentlemanModel` builds and poses him;
`BorrascaAtmosphere.drawGentleman` draws him.

## Out by the far marker

- **Farther.** When the noise comes he may now stand up to 200 blocks from the child (`GENT_FAR`), among the pines past the second
  marker as before. He still needs room to stand and his head in clear sight, and he still prefers what the child is already facing.
- **Taller with distance.** Within 40 blocks he is his own height, four and a half blocks. Farther off he is taller, one of his own
  heights for every 40 blocks, up to 2.6 times (`GENT_SCALE_PER`, `GENT_SCALE_MAX`). From the foot of the Tree he stands out on the
  ridge above the pines; looking straight at him is remembered, at whatever height he stands.
- **Behind the men at the taking** he stays his own height.

The server keeps how tall he stands (`GentScale`) and sends it to the reader with where he stands (`BorrascaViewPayload.gentlemanScale`).

## Tests

- `BorrascaTests#fromTheTreeHeStandsOutPastTheSecondMarker`: a child a few steps from the trunk of the Tree, looking up the ridge, hears
  the noise (pressed against the bark, the trunk fills the view and he finds nowhere to stand in it; if no place will do, the failure
  says what each column was turned down for).
  He stands past the second marker more than 70 blocks off, taller for the distance, with his head in clear sight, and the reader's
  view is told where and how tall.
- `BorrascaTests#theShinyGentlemanStandsInThePinesWhenTheNoiseComes` now checks against his height wherever he stands.
- `BorrascaVisualProof` draws him on its twelfth page, once straight on and once turned.

## Numbers

Protocol 174 (the view now carries his height). Witness 86/65/63 and layout 135 unchanged. New builds only.
