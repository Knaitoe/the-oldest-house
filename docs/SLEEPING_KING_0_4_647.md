# A sort of thing 7: the forest, the Knight and the song (0.4.647)

The seventh part of the plan (docs/SLEEPING_KING_PLAN.md), from the proposal (docs/SLEEPING_KING_PROPOSAL.md). No Witness, layout or
protocol change here.

## The forest

The seventh square is ruled while he sleeps deep, so trees, path and Knight are drawings, and a reader there is a line
(`KingSite.forest`). Small trees stand over the whole square, closer set than the wood on the fourth. They are kept off the road, the
Knight's lane a pace east of it, the hedge gaps, the peg and the turn's path, so the way through is always clear. Every tree breathes
with the land, as the wood's do.

The road turns at the far edge of the square. A side path leaves it eastward along the last two rows before the brook (`TURN_Z`). It
goes through the hedge, which is opened there, into the east file, and in among a stand of trees. The path's end (`TURN_END`) lies among
them, under boughs low enough to close over it.

## The Knight

The Knight waits on his horse a little east of the road beside the square's peg (`KingSite.KNIGHT_WAIT`). He is one of the dream's
people, the same for everybody in that copy of the country (`KingForest`, stranger phase 227).

- **A drawing until he is looked at.** Nobody looking at him, he and his horse are pencil outlines and nothing filled. Somebody looking
  at him, he fills in over a third of a second, and thins as slowly when they stop (`KingKnightRenderer`).
- **He looks back.** Filled in, he looks at the nearest of those looking at him: turning in the saddle when he is riding, turning
  altogether when he stands. That look counts as the dream's notice (`KingBreath.lookedAtByTheDream`). So a line he looks at is a sort
  of thing.
- **He rides with them.** He keeps a pace east of the road and a little behind whoever is furthest along the square, never back over the
  brook behind it.
- **He falls off.** With a reader on the square and nobody looking at him for three seconds, he thins and comes off his horse into the
  path. Everybody near hears his armour hit the ground. He lies there as a drawing, with the horse grazing beside him, until somebody
  looks at him for two seconds. Then he gets up again. Each reader keeps whether they saw him fall (`KingKnightFell`) and whether they
  were among those who got him up (`KingKnightRaised`).
- **His song.** Asked, he sings it, eight bars, with the words on the panel. A reader who has heard it has it (`KingHeardSong`).
  Crouching still for a little over half a second, they sing it themselves, heard by everybody near, no oftener than every twenty
  seconds (`KingSang`). Sung beside the King, it settles him by four stirs, once a dream (`KingSleep.settleBySong`). The filled land
  goes out again from him as far as his depth comes back, and the reader keeps that they did it (`KingSangToHim`). This is the
  proposal's "the Knight's song, sung beside him, settles him by four stirs, once."
- **His cake.** He keeps a cake in a box on his saddle for a reader who has none, and gives it when asked (`KingKnightCake`).
- **The last peg.** The seventh square's peg counts a reader only as a thing. A line he looks at is a sort of thing, and a bite of cake
  does the rest: looked at and fed, they are a thing there, and counted.
- **The turn in the road.** When the reader furthest along comes to the last brook with him near, he asks once to be watched as far as
  the turn. Two seconds later he rides out along the turn's path. If somebody watched him the whole way, he goes round it filled in
  (`KingKnightTurn` 1, "watched"). If nobody did, he is a drawing before he gets there (2, "went on"). Either way he is gone for the
  rest of that dream, and a fresh dream has him back at the peg.

Talking to him (a hand on him) opens the panel: his greeting, then "Would you sing something?", "Have you anything to eat?" if the
reader has no cake, and "Ride on." Fallen, he only asks to be looked at.

## With more than one reader

The Knight, his falls, the turn and the settling are the dream's, shared by everybody in that copy of the country. Any reader looking
at him keeps him filled in, and two readers can take it in turns, each looking back to keep him. Whether a reader saw him fall, got him
up, heard or sang his song, had his cake, was counted on the peg, and was asked at the turn (and how it went) are each reader's own
record, written from their own tick. Only readers within twenty-four blocks when he asks are asked. A reader alone is enough for all of
it: he is the one who looks.

## The people

- The Knight: an old soldier with a long grey moustache, his helmet's visor up, in dented, banded plate. A short pale surcoat over mail
  is belted and checked at the hem like a board, with gloves and dark sabatons. Painted by the shared painter at the cast's scale
  (`literary_king_knight`).
- His horse: a pale, dappled old horse with a dark mane and tail, a bridle strap, a saddle on a piped cloth and a girth. A small wooden
  box with a brass catch is strapped to the saddle's off side. It walks with a slow four-beat when he rides, stands with an ear turning
  when he waits, and grazes with its head down while he lies in the path. Drawn by `tools/draw_king_horse.py`.
- On the horse he sits up in the saddle with both hands forward on the reins and his legs close down its sides, rising a little with
  its walk. Off it he lies on his back
  beside it, one arm flung out, a knee up, his head turned toward it.

## Sound

Both are original and synthesized by `tools/generate_king_audio.py`:
- `king.knight_fall`: plate coming down on turf. A thump, a cluster of short metallic clanks, and a few small rattles settling.
- `king.knight_song`: the song in a lilting six-eight, sung half under the breath in an old man's voice.

## Checked

- `KingTests.theKnightTheForestAndTheTurn` checks each of these:
  - He is made at the peg, a drawing on his horse.
  - Looked at, he fills in and looks back.
  - On the peg, a line he looks at is a sort of thing and not counted. A bite of cake makes them a thing, and counted.
  - Left unwatched, he falls off as a drawing, and the reader saw it.
  - Looked at long enough, he gets up, and the reader got him up.
  - He sings, and the reader has the song. He gives a cake to a reader who has none.
  - Stirred twice, the King sleeps a depth lighter. Crouching still beside him, the reader sings, and the song settles him back.
  - At the last brook he asks. Watched, he rides out round the turn, and the account says so.
  - In a fresh dream he is back at the peg. Asked and let go, he rides out anyway, a drawing before the turn, and that account says so.
- The earlier nine cases still pass.
- Look review: the Knight mounted, filled and as a drawing; fallen beside the grazing horse; the horse from each side.

## New lines (for the owner)

Spoken (captions over the speaker; "the Knight" is the House's name):
- The Knight, greeting: "Good day. I ride with anybody going this way, as far as the last brook. I stay on better if I'm looked at."
- The Knight, again: "Still going on? So am I, as long as you keep an eye on me."
- The Knight, fallen: "Nobody was looking, and I came off. Look at me a while and I'll get up."
- The Knight, getting up: "There. Up again. It's being looked at that does it."
- The Knight, at the last brook: "Would you watch me as far as the turn? I go better watched." (the proposal's)
- The Knight, asked to ride on: "On we go, then. Keep me in the corner of your eye."
- The Knight, giving a cake: "I keep a cake in the box on my saddle, in case. Here. A bite, and you'll be more than you are."

His song (original):
- "My horse is only drawn, and I am only drawn as well;"
- "look at me and I'm filled in. Look off, and off I fell."
- "I fell off at the second brook, and fell off at the third,"
- "and somebody came back and looked, and never said a word."

The reader's answers:
- "Would you sing something?" / "Have you anything to eat?" / "Ride on."

Cues:
- The first time a reader looks at him: "A rider on a drawn horse, beside the road. He fills in as he is looked at."
- He falls: "Nobody was looking at the Knight. He thinned to a drawing and came off his horse into the path."
- Watched round the turn: "He went round the turn filled in, and out of sight."
- Let go before it: "He was a drawing before he got to the turn. Then there was nothing at the turn but the road."
- A reader sings: "The Knight's song, under the breath, all four lines of it."
- The song settles him: "He settled in his sleep, and breathed easier."
- The song settles him a depth: "He settled in his sleep, and the filled land went out again from him."

Subtitles: "Armour clatters to the ground"; "Someone sings the Knight's song".

Account sentences:
- "A knight rode with me through the forest on the seventh square. He was only drawn in while somebody looked at him."
- "When nobody looked at him he came off his horse and lay in the path as a drawing, until I went back and looked at him, and he got
  up." (or, if the reader did not get him up, the sentence ends at "as a drawing.")
- "He sang me a song about a drawn horse, and I sang it afterwards, under my breath." (or "He sang me a song about a drawn horse.")
- "I sang it beside the King as well, and he settled in his sleep."
- "He gave me a cake out of a box on his saddle."
- "On the forest's peg I was looked at and fed, and it counted me."
- "At the last brook he asked me to watch him as far as the turn in the road. I watched, and he went round it filled in."
- "At the last brook he asked me to watch him as far as the turn in the road. I did not watch him all the way, and he was a drawing
  before he got there."
