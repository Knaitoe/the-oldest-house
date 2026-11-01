# The closing hall (0.4.209)

The first of the dark halls' horrors (docs/DARK_HALL_HORRORS_PROPOSAL.md). The owner, 1 November: these "should be the most
extreme yet, either psychologically or physically"; "real deaths are the right call". Earlier that day the owner had picked the
hallway closing in on the children as one of the House of Leaves sequences to build.

Slot 139, layout 65, no protocol change. One new Witness story: **62 sources / 47 required / two kinds / three endings**.

## Where it is dealt

Only in the dark band: from depth sixteen (`DarkHalls.DARK_DEPTH`), one door among several, with the usual story spacing
(`DarkHalls.horror`). It is a one-shot room; a failed visit can be dealt again.

## The hall

Built whole in the dark halls' ash (`ClosingHallSite`), with no light of its own:

- **The hall:** seven wide, six high and forty-six long, running north from the way in.
- **The children:** Chad and Daisy Navidson stand at the far end, at a child's height, holding hands. They are two shared actors
  on stranger phases 39 and 40, with their own painted skins.
- **The drawing:** Daisy's crayon drawing, the source, lies on the floor between them.
- **The squeeze:** one block high, it runs west through the hall's wall at z −3, near the way in. Its mouth is shut.
- **Past the squeeze:**
  - a small room with Karen's interview on a stool (the account);
  - the way on, in its west wall;
  - a two-wide passage running north into the dark, to a blank wall.
- **A second copy of the interview** waits just inside the way in, so a reader who comes back to read it can.

## What happens

Reading Daisy's drawing starts the closing for everyone in the hall, once the reader looks up from it:

- the way in is sealed behind them;
- low in the west wall near it, the squeeze cracks open;
- the walls and ceiling step in, a stage at a time, with the stone grinding and the growl under every step.

| Stage | Wide | High |
|---|---|---|
| 0 | 7 | 6 |
| 1 | 5 | 6 |
| 2 | 5 | 5 |
| 3 | 5 | 4 |
| 4 | 3 | 4 |
| 5 | 3 | 3 |
| 6 | 3 | 2 |
| 7 | 1 | 2 |
| 8 | 1 | 1 (the whole hall is a crawl) |
| 9 | shut | |

Alone, a step comes every four seconds and the last after six, about thirty-four seconds in all. The squeeze's cells inside the
hall stay open until the end, so the narrower the hall, the longer the squeeze.

- **The children come only while held.** Right-click one to take their hand: they keep at the reader's heel. Right-click the
  other while holding the first and they climb onto the reader's back, which slows the reader to a crawl's pace.
- **The hall closes on readers.** A reader in a closing cell is shoved into what is left and hurt. With nowhere left, at the last
  step, they are crushed: a real House death, with the Mother's custody and the portrait if it is the first.
- **It never closes on a child.** When a step would, it waits, a hand's width off them, up to five seconds ("The wall stops a
  hand's width from Daisy, and waits."). Then it closes round them and leaves a pocket, which fills when they move.
- **A child goes first.** Right-click a held child at the squeeze's mouth, or reach into the mouth itself, and they crawl through
  and run off up the passage past it. A reader who goes into the squeeze holding a child behind them loses the child, who will
  not follow.
- **Going back.** With the hall a block wide and a block high, going back to a child still in it and taking them pushes the child
  through ahead. The hall shuts on the reader.
- **After it shuts,** the squeeze fills behind it from the hall side, a cell at a time. It waits for a child in it and pushes a
  reader in it on.

## How it ends

- **Both children through, and the reader through after them:** they run off up the passage. Their voices go on, calling to each
  other, further and further away, for much longer than there is passage. Karen's interview is ready (`sent_them_through`).
- **Going back for the last child:** the reader dies in the hall. The interview is ready (`went_back`), to be read on a later
  visit at the copy by the way in.
- **A child nobody brings out:** once nobody is left in the hall, that child is left in the wall. The visit fails, and the account
  remembers which child (`LeftBehind209`).
  - For as long as the reader stays past the squeeze, the child calls the reader's name from inside the wall.
  - On every later visit to the dark halls, the first door they open has a small handprint on its inside, shown only to them,
    until they bring both children out.

The Witness line: "I went down a hall that was getting smaller and brought two children back up it. I sent them through first."
Kind: survival.

## Co-op

The hall and the children are shared. With two readers or more in the hall at the start, it closes twice as fast. Each reader can
take a hand, and nobody need carry. Every reader present at the start who comes through after both children has their own
interview; a reader present when a child is left has their own failure.

## Literature

- **The source:** Daisy's drawing. A long hall drawn in crayon and the two of them at its near end, "THE HALL IS LONGR EVRY TIME",
  her brother's neater printing under it, and the hall's lines drawn again closer and closer until the crayon goes through the
  paper.
- **The account:** Karen's interview, long afterwards, in a kitchen. It is about how long the hallway was (long enough that she
  couldn't hear them crying from the door), the walls that waited for the children, and who came out last.
  - It names the reader, and never gives them a gender.
  - It mentions an earlier visit that left a child behind.
  - It ends with the children running into the dark and coming home.
- **Elsewhere:**
  - the co-op line ("We took a hand each, and it closed twice as fast for there being two of us");
  - the miniature (a matchbox drawer pushed almost shut, two crayon stubs inside);
  - the staircase prose ("the hall that closed");
  - the co-author pool;
  - the advancement under survival.
- **Limits held:** nothing gory is shown; no harm to a child is ever seen; the reader's gender, colour and age are never named; the
  children speak on the action bar and in the air, never in chat.

## Art and sound

- `tools/draw_horror_art.py` draws the one `horror_fitting` block the five horrors will share (here: the drawing, the folder on its
  stool, the crayons, the handprint), in native pixels from fixed seeds.
- `tools/paint_character_skins.py` paints Chad and Daisy; they are in the character catalogue.
- `tools/generate_horror_audio.py` makes six subtitled cues:
  - the walls stepping in, breathing;
  - the wall holding;
  - stone cracking open;
  - a child crying;
  - a child calling far off;
  - a child's voice inside the wall.

Both art and audio are checked in CI.

## Tests

Seven `ClosingHallTests` in the literary namespace:

- dealt only in the dark band, the children at the far end;
- the drawing seals the way in and the hall closes in steps;
- the hall waits for a child and shoves a reader;
- the children go first through the squeeze, the reader after them, and the interview resolves;
- a reader who goes first loses the child, both are left in the wall, and the handprint waits;
- going back for the last child shuts the hall on the reader;
- two readers close it twice as fast.

The Witness pool every case pins now lives in one place (`gametest/WitnessPool`), so the next four horrors each change one line.
