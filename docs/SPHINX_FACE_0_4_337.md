# The Sphinx: her whole face (0.4.337)

The owner, on the first crossing (docs/SPHINX_0_4_306.md): "The first sphinx vignette also needs a fully visible sphinx face."

Until now the vestibule hall showed one eye at a time over a warned door, lying face down over the wall top: "one eye, no face". That eye is gone from this crossing. When she looks now, her whole face rises out of the dark over the hall wall.

## Her face

The face is one picture, eighteen blocks across and fifteen tall (288 by 240 pixels, sixteen to a block, like her eye and her paw), drawn by `tools/sphinx_face_art.py`:

- weathered sandstone skin, still painted;
- a striped blue and gold headcloth falling to either side, with a band and a small cobra at her brow;
- kohl round each eye, running out to the temples, and painted brows;
- a long straight nose;
- lips pressed in a slight grimace (0.4.340, below).

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

## 0.4.340: out of the dark

The owner asked for her to be "a bit more ominous, its default state should be a slight grimace. It looks too friendly", then for "the
same large eyes from the original vignette" and "her mouth slightly opened by default and small, sharp teeth inside", because "the design
is really important. It's something that should instill fear when it presses down out of the dark", and last to "make the cowl thinner".

Her face now:

- **Her eyes.** As large and wide open as the one eye that looked over a warned door before 0.4.337: each about four blocks across and
  a block and a half tall, staring, with a large amber iris. The whites and irises behind the holes are drawn bigger to fill them, and
  the irises turn further.
- **Her mouth.** A slight grimace, wider than before and a little crooked: the corners dragged down and back, the lips just parted on a
  dark mouth and two rows of small, sharp teeth, the upper ones hanging from the top lip and the lower ones rising between them. Tight
  creases at each corner run down toward the jaw, and her chin is drawn up under them.
- **Her brows.** Low and nearly straight above the wide eyes, slanting down hard toward her nose, with two short furrows between them.
- **Her nose.** Broken away, as hers at Giza is: the bridge ends in a ragged edge, and below it the break is rougher and darker, with only
  the holes of the nostrils left.
- **Her stone.** Older and darker, deep in the sockets round her eyes and hollow under her cheekbones. The kohl has run in streaks down
  her cheeks from under each eye, and cracks run through the stone, one through her left eye's socket. Everything away from her eyes and
  the middle of her face sinks toward black, so she comes up out of the dark.
- **Her headcloth.** Thinner: it now runs a little over a block and three-quarters beyond her face on each side, where it once ran out to
  the edge of the picture, its paint faded and worn through to stone.

In the hall her stone takes the hall's own light, never brighter than a little above dark, while her eyes are always lit, so they are
what is seen first as she leans down (`SphinxFaceRenderer.STONE_LIGHT` and `EYE_LIGHT`). The client proof's face page adds two views as
she is in the hall's dark. The same face is used wherever she shows it, the dream included. Nothing else about the crossing changes.

## 0.4.341: her mouth

The owner: "Ensure the mouth moves when she speaks too."

Her face is stone, and her jaw moves like stone. While she speaks:

- her jaw drops in three steps (`face_mouth.png`, three frames of the box round her mouth and chin);
- her lower lip and the lower row of teeth come down with it, and her chin follows;
- the top lip, the upper teeth and the rest of her face stay where they are;
- the rhythm comes from two slow waves, one for syllables and one for phrases;
- at the end of the line her jaw eases shut to the slight grimace she rests in.

`tools/sphinx_face_art.py` draws the frames from the same face as the picture. The stone grain round her mouth is identical in every
frame, so only her jaw moves, and the picture itself is unchanged byte for byte.

The House works out how long each line takes her to say, a little slower than a person reads it (`SphinxCore.speaks`). It sends that
time to each reader who hears the line (protocol 139, `sphinx_voice`):

- **In the first crossing,** that is everyone in the hall, whoever the words were for, since they all hear her voice and see her face.
- **In the stair, shelf and field crossings,** it is the reader she speaks to. Only her eye shows there, so nothing visible changes yet.
- **In the dream,** the same clock will move her mouth as she asks her last question.

The client proof's face page shows her speaking in two of its four views.
