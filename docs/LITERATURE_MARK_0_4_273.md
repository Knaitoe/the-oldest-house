# The first paper, marked until read — 0.4.273

The owner: "Vignettes that requires literature to be read at start need to have literature highlighted in some form to prompt
players to engage".

## Which vignettes

Every ordinary literary vignette waits on its first paper. Its account does not count until that paper has been read
(`LiteraryVignettes.Pages`: "This account answers ... Read that first"), and several hold their play on it as well: the Ghosts
Set's filming days, the child's room card, the Line, the Mapping floor plan, the elk fan, Movie Night's program and the Storeroom.
So the rule is the same for all of them (`SourceMark.waits`): a literary scene is marked unless it is one of the six uncanny rooms,
where reading is optional, or Borrasca, which keeps its own set of papers.

## What the reader sees

Until they have read it, the reader's own client draws the paper where it lies (`LiteraryRooms.source`, in the reader's own copy
where the scene is copied):
- a faint, warm outline round the object's own shape, which breathes slowly, over about three seconds;
- now and then, a mote rising off it, like dust in lamplight.

It is drawn only within 28 blocks, and with ordinary depth, so walls hide it; it is not a marker seen through the house. Nobody
else is told where another reader's paper is, and spectators are told nothing.

## When it goes

As soon as the reader has read the paper to its end (or opened a one-page paper), the mark goes out for them. It also goes when they
carry the paper off unread (they have it with them), when the story is finished, and whenever they leave the scene or change
dimension. Another reader who has not read it keeps their own mark.

## How

- `labyrinth/SourceMark`: every ten ticks, for each reader, works out where their unread first paper is (or nothing) and sends a
  change only. `network/SourceMarkPayload` carries on/off and the position. Network version 126.
- `client/SourceMarkClient`: draws the outline after translucent blocks, using the block's own shape (nothing for an empty
  shape), and the motes on the client tick.

## Tests

`SourceMarkTests#theFirstPaperIsMarkedForEachReaderUntilTheyReadIt` (literary): two readers in the Hill nursery are each marked at
its paper; reading it puts out the reader's own mark and leaves the other's; a spectator is told nothing; an uncanny room and
Borrasca are not marked. Test readers have no client to carry the mod's channel, so the case reads the server's own record of what
each reader was last sent.
