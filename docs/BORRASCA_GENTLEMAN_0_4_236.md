# Borrasca: the Shiny Gentleman (0.4.236)

The owner, 1 November: "Remember that in early visits, when the player is a child, the shiny gentleman should appear as a tall
frightening monster in the distance when the noise echoes out."

The second of three Borrasca releases (0.4.235 made the trail play in any order; 0.4.237 puts the warned content in the vignette).
Network protocol 120; no layout, Witness or source-kind change.

## When the noise comes

On the two child visits, every time the noise comes off the ridge (the three strokes Nell's legend describes: when the reader cuts
their name beside hers, when a child goes past the second marker, when she sees someone at the clearing), the reader may see him.
He stands among the pines past the second marker, between 28 and 70 blocks off, somewhere the reader has a clear line of sight to
from where they are, preferring what is already in front of them. If there is nowhere like that, there is only the noise.

He stands for three and a half seconds, as long as the strokes last, and is gone when they stop. He does not move or come nearer.
He is the reader's alone: nobody else sees him, and he is not a creature in the world, only something drawn on the reader's screen
where he stands. He never appears on the adult visits; the grown reader finds men and machinery where he stood.

A reader who looks straight at him while he is there remembers it, and the account says so: something tall stood in the pines past
the second marker and shone, and when the noise stopped it was gone.

## What he looks like

Four and a half blocks tall and too thin. A long coat, narrow under shoulders that are too wide, falling to a ragged hem; a tall hat
bent over at the crown, grown into his head; no face, only a smooth polished oval with one bright band across it where the light
catches; a neck like a stalk; arms that hang apart from the coat, bend out at the elbow and end past his knees in long hooked
fingers; legs a pixel wide. He shines like wet metal, lit hard on one side and dark on the other, with streaks of light sliding down
him and a few glints at his edges. He is drawn faintly lit, so he shows at dusk.

`tools/draw_shiny_gentleman.py` draws him in native Minecraft pixels (16 to a block) from a fixed seed; `--check` verifies the PNG.

## Tests

`BorrascaTests#theShinyGentlemanStandsInThePinesWhenTheNoiseComes` (borrasca namespace): a child past the second marker hears the
noise and he stands past it, 28 to 70 blocks off, in clear sight, in the view sent to that reader; looking at him is remembered and
the account says so; he is gone when the strokes end; on the adult visits he never comes. `BorrascaVisualProof` draws him on a
twelfth native page.
