# Borrasca 0.4.440: the names, and the Shiny Gentleman coming nearer

The owner, after playing: "kendra tells the player to leave their name so other kids know they were there. in the original story,
wasn't it to keep the shiny gentleman from taking them?"; "the shiny gentleman's visage is supposed to flash in the distance, to loom
over the player when they're a child - their active imagination. that was in there, and now it's not triggering"; "have the klangs get
closer while the player is past the mile marker and grow more frequent. the gentleman grows closer to a child player with each until
it's directly above them. it picks them up and eats them after that, sending the player to the triple tree again"; and "I'd allow it on
visit one, too".

## The names

The children cut their names into the Triple Tree so that he cannot take them. Kelly says so at the fort ("If you don't, he takes you.
We didn't.") and after the carving ("Our names are on it now. That means he can't have us."); Kendra, on the second visit, says she
used to believe it.

## Seen from far off

Under the pines a clear place past the second marker with his head in sight is rare, and when the noise found none it showed nothing.
Now, failing one, he stands up the ridge anyway (`BorrascaStory.ridgeSpot`): the way the noise comes from, past the second marker, tall
enough (up to 3.6 times his height) that his hat and face stand over the tops of the pines there, in sight if any such place is and
otherwise where the child is looking. The stand's fade-in, which hid any stand longer than the old seventy ticks, is gone: he is simply
there with the stroke.

## Past the second marker

A child who stays past the second marker (visit one, and visit two before Kelly leads them up or after the compass is found) hears the
machine strike again and again, from where he stands, louder and sooner each time: 64, 44, 30, 20, 12 and 6 blocks off, the strokes
150, 115, 85, 65, 50 and 40 ticks apart, the first a few seconds after crossing. Each time he is nearer and taller. Then he stands over
them (`OVER`, 2.5 blocks, at his tallest), and a second and a half later he has them: they are lifted off the ground and held for three
seconds while, in their own view only, he folds down over them, his hands come up under them, and his face comes down into their sight,
the smooth oval splitting open on steel teeth and the dark of his throat (`BorrascaGentlemanModel.renderTaking`), until the screen is
black. Nothing of the child is drawn. They come to under the Triple Tree with their back to the trunk, unhurt; the cue names their own
name in the bark if they carved it. Going back below the marker stops the strokes, and he falls back a step for every five seconds spent
there. A grown reader never meets him.

The account remembers it: "Once I went on past the second marker. Each time the noise came it was nearer, and so was it, until it stood
right over me and bent down. Then I was under the Triple Tree with my back to the trunk."

Saved per reader: `Approach`, `ApproachArmed`, `ApproachWait`, `ApproachAway`, `GrabTicks`, `Swallowed`; each new visit clears all but
`Swallowed`. The view payload carries `grab` and `swallowed`; protocol 177.

## Tests

`BorrascaTests.pastTheSecondMarkerHeComesNearerAndTakesTheChild`: six strokes, each nearer and no later than the last, then over the
child at his tallest; the grab is told to the reader's view; after it they are at the Tree, alive, eaten once, and the account says so;
below the marker he falls back; grown, never. The pines case now steps back below the marker before it waits for him to go.
