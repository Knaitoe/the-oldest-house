# A sort of thing 3: the brothers, his sleep and the ways out (0.4.643)

The third part of the plan (docs/SLEEPING_KING_PLAN.md), from the proposal (docs/SLEEPING_KING_PROPOSAL.md). The House now deals the place,
to a reader who has gone twelve doors deep and has not finished it, and it is a Witness source of the understanding kind ("A sort of
thing"). The pool is 91 sources, and 69 are required.

## The brothers

Tweedledum and Tweedledee stand arm in arm just inside the hedge's gap into his square. They are two new painted skins on stranger
phases 218 and 219: grown men alike as two buttons, in coats with a white collar. Each collar is lettered in red, DUM and DEE. They face
along the road, so their linked arms stay round each other, and only their heads turn to a reader within sixteen blocks. When a reader
is near, each points the way with his free arm, in opposite directions.

A hand on either of them opens the conversation panel. In three turns they say:

- what the reader is in his dream (a sort of thing);
- what would happen if he woke;
- his longer name;
- the rattle under his tree, and the battle there will be over it.

The last turn marks the reader as having heard them (`KingHeard`, their own record) and readies the battle. Later hands get short
lines, which change once the brothers are dressing or the rattle is gone.

Forty-five seconds after the battle is readied, they dress (five seconds, bolsters), then fight beside him (four seconds, the
clash of their swings). The fight stirs him twice. Every reader in the country remembers seeing it (`KingSawBattle`, written through
that reader's own tick). Then a great shadow goes over the wood (`king.crow`) and they run east and are gone for the rest of that dream.

## The rattle

The rattle lies on his square, between him and the gap. It is a new `KingBlock` kind, RATTLE: a white and red banded head, a brass
bell ring and a turned handle, tipped over, with no collision. A line's hand goes through it. A thing or a sort of thing lifts it. That
settles the battle before it is fought: the brothers stop dressing, because there is nothing left to fight about. A fresh dream puts
the rattle back.

## His sleep

He sleeps at three depths, and a fourth is awake. Every two stirs he sleeps a depth lighter, and the land shows it at once:

- **Depth one:** the filled land gives out after the fourth rank.
- **Depth two:** only his own square is filled, and it thins toward its edge.
- **Awake:** all of the land is ruled.

The breath still moves the edge at every depth.

He stirs (`king.stir`, a grumble, and he turns from one side to the other) when a reader who is a thing or more, within seven blocks of
him, does any of these:

- runs;
- lands from a jump;
- is caught moving in the first third of a second of his in-breath (once a breath);
- drops something.

Each reader's own noises stir him at most once every two seconds. The battle stirs him twice.

## The ways out

Each reader's ending is their own.

- **His whole name.** A reader who has heard the brothers, standing beside him, can put a hand on him and say the whole of his name.
  The breathing stops in the middle of a breath and he stands, turning to the nearest reader. After a second and a half the country
  goes over to lines. Three seconds later, every reader still in it comes to in the drawing-room, between the armchair and the fire,
  facing the glass:
  - the reader who said it: `said_his_whole_name`;
  - anybody else there: `there_when_he_woke`.
- **Leaving him his sleep.** A reader who has been beside him as a thing and has heard the brothers, and then walks off the board as a
  line, comes to in the drawing-room (`left_him_his_sleep`) while he goes on breathing. If somebody else later wakes him properly while
  that reader is still about, their account says so.
- **Too soon.** Six stirs wake him too soon. The country goes over to lines and goes out, and every reader in it fails the place and is
  sent back through its door. Their account is not readied.

The account lies on the armchair. Like every story, it needs the first paper (the chess problem) read first. It tells the room and the
country, the brothers if they were heard, the rattle or the battle, and the ending that was that reader's.

A country nobody has stood in for ten seconds is a fresh dream: deep asleep, the rattle back under his tree, the brothers in the wood.

## Fixes found by the new cases

- **The waking's end was lost for one reader.** The waking ends inside one reader's tick. Writing to that reader's record from there
  was undone when the tick saved its own copy afterward. The end is now kept per reader and settled from that reader's own tick, and so
  is the record of the battle.
- **The King could be missing.** He was made only when a reader arrived, when his tree's ground might not be loaded yet. The dream's
  tick now makes him whenever he is missing and the ground is loaded.

## Checked

- `KingTests.theBrothersTheRattleAndHisName`:
  - the brothers stand side by side and say his name to one reader only, and that readies the battle;
  - two stirs draw the land in at once;
  - a line's hand goes through the rattle and a thing lifts it, which settles the battle;
  - his whole name said beside him stops the breathing, and the namer comes to in the drawing-room with `said_his_whole_name`;
  - the dream starts fresh;
  - the account on the armchair resolves the story once the problem has been read.
- `KingTests.theBattleStirsHimAndSixStirsWakeHimTooSoon`:
  - the battle stirs him twice and is remembered;
  - the brothers run and are gone;
  - six stirs wake him too soon, and nothing is readied.
- `KingTests.walkingOffLeavesHimHisSleep`:
  - a reader who has not met him is only put back;
  - one who met him and heard the brothers leaves him his sleep;
  - the House does not deal the place to a reader at depth 0.
- The two earlier King cases still pass.
- `LiteraryTests` now counts the King among the sources. `WitnessPool` is 91 / 69 / 67.
- The account advancement is `a_sort_of_thing` (`tools/generate_advancements.py`).
- Art: the two skins, painted by `tools/paint_character_skins.py`, are in the catalog. The rattle's textures and model come from
  `tools/draw_king_art.py`. The two sounds come from `tools/generate_king_audio.py`.

## New lines (for the owner)

The dream's people say "Red". The House's own lines never name a colour.

The brothers:

- "That's the Red King. He's dreaming. Guess what about."
- "Why, about you. You're only a sort of thing in his dream."
- "Tread like that and he'll stop. Then where would you be?"
- "Not even there."
- "The Red King, people call him, when they don't know him. He has a longer name than that."
- "His Red Majesty, King of the Fourth Square, who sleeps under the tree. All of it, mind, or it doesn't count."
- "And that's my rattle under his tree. Spoilt."
- "It isn't spoilt. It's old."
- "Then we'll have a battle about it. As soon as we're dressed."
- "You've got it, then."
- "Then there's nothing to fight about. A pity."
- "We're dressing. You can see we're dressing."
- "Hold this bolster."
- "Don't wake him, that's all."
- "Or do. Then see where you are."

The reader's replies:

- "Nobody can guess that."
- "What about?"
- "Who is he, then?"
- "I'll keep my voice down."

Beside him:

- "His Red Majesty, King of the Fourth Square, who sleeps under the tree."
- "Let him sleep."
- "He is asleep in a heap at the foot of the tree, close enough to touch."

Cues:

- "The hand goes into him as into a drawing of him."
- "His eyes are open."
- "The hand closes on the rattle and comes away with nothing in it."
- "The rattle comes up off the grass without a sound."
- "He stirred in his sleep."
- "He turned over in his sleep, and the filled land drew in toward him."
- "He turned over again, and the filled land drew in to his own square."
- "The two brothers are tying bolsters round each other for a battle."
- "The two brothers stop dressing. There is nothing left to fight about."
- "Something went over the wood, as big as a cloud, and the two of them ran."
- "The two of them are too busy to talk."
- "The breathing stopped in the middle of a breath."
- "The breathing stopped in the middle of a breath. He is waking."
- "The country went over to lines, all of it at once, and then the lines began to go."
- "The country went over to lines, and the lines began to go."
- "He woke, and the country went out with you in it."
- "The fire, the glass and the armchair. There is a book on the armchair."
- "The last of the board let go. The drawing-room came round you, and behind the glass he is still breathing."

The account (each part shown only when it applies):

- "There was a drawing-room with a fire in it, and a glass over the fire, and the same room on the other side of the glass. Beyond it
  the country lay in squares, and an old man was asleep under a tree."
- "Two brothers in the wood took me to see him. They said he was dreaming, and that I was only a sort of thing in his dream. The two of
  them had a longer name for the King."
- "One of them had left a rattle under his tree. I went in to it between his breaths and picked it up, and after that there was
  nothing for them to fight about."
- "They dressed for a battle over a rattle and fought it beside him, and he turned over in his sleep. Something went over the wood like
  a cloud, and they ran."
- "I said the whole of his name. He opened his eyes. One of us went out."
- "Somebody else said the whole of his name. He opened his eyes, and the country went over to lines with me in it."
- "I left him his sleep. I walked until I was a line. He is still breathing behind me, or I am."
- "He woke after I had gone. I was not there to see it."

The Witness line: "I crossed a country drawn in squares to an old man asleep under a tree, and two brothers told me what I was in his
dream."

Subtitles: "The King stirs in his sleep" and "Great wings pass over".
