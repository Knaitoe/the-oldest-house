# Second Person, E3: nobody home (0.4.635)

The proposal's third ending (docs/SECOND_PERSON_PROPOSAL.md, section 5, ledger row 26: "Frighten them out"). New builds only. No
layout, protocol or Witness change: 90 sources / 68 required / two kinds / three endings. The ending resolves Second Person like E1 and
E2, with its own outcome (`nobody_home`).

## What happens

1. **Frightened out.** At any cupboard turn before the last morning (summer, autumn, winter or spring, and never once the form is
   signed), the year is read off the record: `SecondPersonStory.fright` (things hidden, the air made cold round somebody, a hand on
   them, the tap on the first night, the evening interfered with or the reader seen by Sam, the cold at the stay, GO read on the glass,
   a call put down, going or a frightening hand when seen) against `gentle`. At six or more, and three or more over gentle, the season
   does not go by: the flat goes into its own season, `SecondPersonGone.GONE` (5, compared only by equality), and the season they left
   in is kept (`LeftIn`). The House's pages are not turned.
2. **The night** (sub 0). It is the middle of the night, the lamp off, the flat door shut. They are sitting on the edge of the bed,
   dressed. A reader in the bedroom hears "Don't. Please don't."
3. **The bag** (sub 1). They take one bag from the hall (the bag gesture already drawn for the winter ticket and the last morning)
   and say to the flat that they are taking nothing else.
4. **The door** (sub 2, 3). The flat door opens; "I'm sorry. Whoever you are." They go out onto the landing, the door shuts, and
   their steps go down the stairs and out of the street door.
5. **Nobody home** (sub 4). The book's last page: "They've gone. The flat is yours. Nobody can see you." Nothing in the flat moves
   after that unless the reader moves it. A little later the flat door is found unlocked, the book is on the bench at the bus stop and
   the account is ready (`LiteraryVignettes.ready`, outcome `nobody_home`). The reader can stay as long as they like.

The account keeps every paragraph of the year up to the season they left in (the winter only if they left in the winter or spring,
the spring only if they reached it), then takes the last morning's place with its own paragraph, from the record only.

## New lines (all new in this slice)

Cues: "You come out of the cupboard into the middle of the night. The season has not gone by. They have stopped sleeping." / "They
are sitting on the edge of the bed, dressed, with one bag." / "The door shuts. Their steps go down the stairs, then the street door,
then nothing." / "They've gone. The flat is yours. Nobody can see you." / "They did not lock the door behind them."

The resident: "Don't. Please don't." / "I'm not taking anything else. You can have it." / "I'm sorry. Whoever you are."

The book: the sitting cue and the last page, as its page for each step; "Wait." / "Go out by the front door and down to the
street."

Pages turned: "You went into the cupboard, and the season did not go by." / "They left in the night with one bag."

The account: "They left in the night, in (the summer / the autumn / the winter / the spring), with one bag. The rest of their things
are where they stood." / "Their (routine thing) is (where it was last put)." / "You hid one of their things." (or "You hid their
things.") / "You made the air go cold round them (once / twice / three times / again and again)." / "On the first night you ran the
tap." / "You wrote GO on the glass." / "When they saw you in the glass, you went." / "When they saw you in the glass, you put a hand
on them, and they backed away to the wall." / "Nothing in the flat moved after that, unless you moved it. The flat was yours. Nobody
could see you."

## Tests

`SecondPersonTests.nobodyHome` (batch `second_gone`): an autumn of hidden things, cold and the tap, then the cupboard; the season
does not go by, they sit up, take the bag, go; the outcome, the empty flat and the account's lines; no winter and no last morning in
it. The Second Person namespace is 7/7.
