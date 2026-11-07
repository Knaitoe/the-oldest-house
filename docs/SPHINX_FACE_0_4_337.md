# The Sphinx: her whole face (0.4.337)

The owner, on the first crossing (docs/SPHINX_0_4_306.md): "The first sphinx vignette also needs a fully visible sphinx face."

Until now the vestibule hall showed one eye at a time over a warned door, lying face down over the wall top: "one eye, no face". That eye is gone from this crossing. When she looks now, her whole face rises out of the dark over the hall wall.

## Her face

The face is one picture, eighteen blocks across and fifteen tall (288 by 240 pixels, sixteen to a block, like her eye and her paw), drawn by `tools/sphinx_face_art.py`:

- weathered sandstone skin, still painted;
- a striped blue and gold headcloth falling to either side, with a band and a small cobra at her brow;
- kohl round each eye, running out to the temples, and painted brows;
- a long straight nose;
- closed lips with the corners just lifted.

Beyond her outline the picture is empty, and its lower edge falls away into the dark, so no block or frame shows.

She leans out over the wall to look down. Her chin is just above the wall top, over the doors, and the crown of her headcloth leans out over the back of the hall, about seventeen degrees forward of upright.

## Her eyes

Her eyes are holes in the picture, each about three blocks across. Behind each one the renderer draws three layers:

- **The white.** Yellowed, with the shadow of the lid along the top.
- **The iris.** Amber, with a black pupil. It turns smoothly toward whoever is nearest. Readers in the hall come first; she looks toward someone further off only when the hall is empty.
- **The lid.** It comes down when she blinks, about every seven seconds, both eyes together.

The server keeps a coarse version of the same look on the block. It records west, middle or east, and right under her, the middle, or the back of the hall. The tests follow that.

## When she looks

- **A wrong latch.** Her face rises over the wall, sliding up and forward out of the dark as it brightens over a second and a half. It stays while that door's look lasts, seven seconds as before.
- **A second warned door.** Its look keeps her there. Her face sinks when the last look is over.
- **Pulling a pin.** Her face rises as her paw comes over the wall top, and stays until she withdraws.

Her voice and breath come from over the door concerned, as before.

## The hall

So that her whole face can be seen, the hall is six blocks deep (z -8 to -13) instead of two. The doors, their latches, pins, sockets and flaps, and the chambers behind them all stand four blocks further north. The dark above goes up to 22, because the room now stands one block into its slot. Everything else about the crossing is unchanged:

- the question, the stand, the flaps;
- the survey pins, the latch in motion;
- the sweep to the corridor sill;
- the seal, and the records.

New builds only.

## Multiplayer

There is one face for the party. It rises and sinks for everyone, and its eyes follow the same reader on every client: whoever in the hall is nearest the middle of the hall below her.

## Tests

`SphinxTests#theSphinxsFirstCrossingIsTestedAnsweredAndForced` now checks:

- her face is in place and hidden at first;
- it rises with the warning;
- with a clear line of sight to six points on the face (crown, chin, both sides and both eyes), from six places at the back and middle of the hall;
- her eyes are in sight from right at the door;
- a second warning keeps her looking;
- her eyes follow a reader west, east and down to the doors, even with a friend nearer in the vestibule;
- her face sinks when both looks are over, rises again with the pin, and sinks when she withdraws.

The survey pins case passes unchanged.

The native art proof (`-PdareSmoke`, the silo-native job) adds a sixth view, `native-dare-sphinx-face.png`, showing her face three ways:

- face on;
- from the hall below, with her eyes turned down and a blink;
- half risen out of the dark.

## Assets

- `tools/sphinx_face_art.py`: the face, the whites, the iris and the lid, under `textures/entity/sphinx/` (`tools/draw_sphinx_fittings.py` writes and checks them).
- `sphinx_face`: an invisible block with a block entity, drawn by `SphinxFaceRenderer`.
