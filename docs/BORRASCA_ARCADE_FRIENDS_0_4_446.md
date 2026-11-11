# Borrasca: Patrick and Kendra at the cabinets (0.4.446)

The second part of [BORRASCA_LIFE_PROPOSAL.md](BORRASCA_LIFE_PROPOSAL.md), the one the owner paired with the window
([BORRASCA_COMIC_0_4_445.md](BORRASCA_COMIC_0_4_445.md)). The owner, 10 November:

> Give Patrick and Kendra a turn at the arcade. Patrick watches your first run of Deep Seam and asks for a rematch if you beat him.
> Kendra knows a trick he keeps getting wrong. On the next visit, she reacts differently depending on whose initials are at the
> top. Patrick's score would remind you of an afternoon you actually spent with him.
>
> The arcade rematch is the one I'd pair with the window. It gives Patrick and Kendra more room to be funny and competitive while
> the four of you are still together.

Nothing here is required. The four visits, the ending and the Witness count are unchanged.

## In play

**They come in.** On the first visit, the first quarter the reader puts in DEEP SEAM brings Patrick and Kendra into the drugstore
while the screen has the reader's eyes. They get to the sidewalk by its door only when nobody can see them go, and walk in through
the door: Patrick to the reader's shoulder, Kendra behind him. Kelly, if she is on her way up the street and near enough, comes in
after them and goes to the comic spinner. The door shuts behind them.

What they say can be read without leaving the cabinet: the captions sit across the top of the game's field.

- Patrick, while the reader's first game is still going: "That's my game. Hop everything. That's all there is to it."
- Kendra: "Don't listen to him."
- Kelly, at the spinner: "That's where Patrick gets them. Thirty-one's the one I've got." (It is the one she stands in the reader's
  window at the fort.)

**Under Patrick's score** (PAT, 730, at the top of the table), he says so, a different way each time:

1. "Seven thirty. P, A, T. That's me. It's been up there since June."
2. "You can hop them. The stick on this one's sticky, that's all."
3. "Closer. Not close. Closer."

After the first of these Kendra gives the trick: "He hops the timbers. You can't hop a timber, you go round it. He got seven thirty
the one day none came down his rail." It is true of the reader's own game: a gap in the rail can be hopped, and a fallen timber
only gone round. Then she takes the other cabinet: "I'm not watching him do it again. I'll be at the fish."

**Over Patrick's score**, he wants the cabinet: "No. No way. Move over, I'm getting it back." If the reader is standing where he
means to stand he waits a few seconds, says "Move!", and comes anyway. A hand on the cabinet while he is on his way gets "My go."

**Patrick's game is a real one.** The server plays it to its end out of a seed before he touches the stick, and it then takes as
long to happen as it took. He hops everything on his rail, timbers too, and goes after lamps; he loses all three carts to timbers,
in under a minute and a quarter. Each time, Kendra, without looking round from her own game: "That was a timber." / "Timber. I can
hear it from here." / "Go round them, Pat." The cabinet thumps as each cart goes. A reader who puts a hand on the cabinet while he
plays watches his game over his shoulder: the same game, out of the same seed, on their own screen, with nothing to pay and
nothing to steer.

When he has lost: "The stick's sticky. That one doesn't count." Kelly, from the comics: "It's always the stick."

**Telling him.** A word to Patrick then is a choice: *Tell him to go round the timbers*, or *Say nothing*.

- Told: "Round them. ...I knew that. I was about to." He has one more quarter. This game he changes rails for timbers, and as the
  cart gets faster his nerve goes and now and then he only watches something come. It is chosen to be a good game: the shortest of
  two dozen that goes over whatever is at the top, or failing that his best. Kendra calls these too: "That one was a gap. That
  one's on you." / "You looked right at it." If it goes over the reader's score: "P. A. T. Read it.", and Kendra: "You told him.
  You did this to us." If not: "...It's a dumb game anyway.", and Kendra: "It is not."
- Not told: "Best of three. Saturday."

A reader who beats him again after his quarters are gone: "That was my last quarter. You wait till Saturday." A reader already at
the top who goes higher: "Now you're just showing off."

**Kendra fishes.** BIG CAT, as she plays it: she keeps her hook up out of the perch until there is something worth a worm, keeps
off the turtles, and lets the big one run. When the big cat takes the hook: "Big one. You let them run. Patrick reels till it
snaps." Her game can be watched the same way. At its end her score goes on BIG CAT's table and she says whose it went over ("Over
K, R, T. He'll hate that." / "Over Kelly. Don't tell her."), or that it was better or worse than her last. A word to her while she
plays: "Shh. Fishing." To Patrick while he plays: "Not now!"

**They leave** when the reader has been out of the shop for five seconds and nobody is at a cabinet: out by the door, shutting it,
and on up the street after Kelly, the walk to the Tree picked up from where they are. A reader who goes far off (forty-eight blocks)
finds their games finished without them, the scores on the tables. A reader who leaves the hill in the middle of it, or logs out,
comes back to find it over the same way.

**The next time**, with Patrick gone, the first thing Kendra has to say that is not about the comic is how it came out:

| Whose initials are at the top of DEEP SEAM | Kendra |
|---|---|
| The reader's, and Patrick was never told | "Your letters are still over his on the Deep Seam. He was going back Saturday with his paper money." |
| The reader's, though he was told | "You told him how and you still got back over him. He said the stick was sticky." |
| Patrick's again | "He got the top back the same afternoon. He told everybody he worked the timbers out by himself." |
| Patrick's, never beaten | "Nobody ever got over him on the Deep Seam. He'd want me to say so." |

Years later the unplugged cabinet still shows that table through the dust, as before.

**The account** keeps the afternoon: that the two came in to watch; that Patrick wanted the cabinet back and hopped every timber
while Kendra told him so; that the reader told him, and whether he had the top back before they left; that Kendra fished and Kelly
went through the comics.

## A table that can be beaten

Patrick's 2140 could only be passed by a nearly perfect five-minute game, so almost no reader would ever have seen any of this.
DEEP SEAM's table now starts PAT 730, KRT 610, KEN 480, PRY 390, KEL 270, PAM 150. Hopping everything is worth about 350; 730 is
about a hundred seconds of going round the timbers with half a dozen lamps picked up on the way. BIG CAT's table is as it was. New
builds only; a reader who already has a table keeps it.

## How it is made

- **`BorrascaArcade.Hand`**: the keys a hand that is not the reader's holds on a tick. `HOPS` (Patrick), `ROUND` (Patrick once
  told; `NERVE` .3 is how often, at top speed, he freezes) and `FISHES` (Kendra). A hand's keys depend only on the game in front of
  it and on numbers drawn from the game's seed, so a seed is a whole game: `BorrascaArcade.play(game, seed, way)` returns its
  score, its length and when it went wrong or right (`Played`). `BorrascaArcade.seed(from, game, way, fits, nearest)` tries
  twenty-four seeds for a game that fits. In a script that ports the two games' rules, over three hundred seeds, the hopping hand
  scores about 350 in about fifty seconds and the hand that goes round about 1200 in about two minutes; the game test checks the
  same relation over twenty-four seeds in the game itself.
- **`BorrascaArcadeFriends`** is the scene. `ArcadeScene` NONE, ON, OVER; `PatMode`, `KenMode`, `KelMode` COMING, WATCHING, GOING,
  PLAYING, LEAVING, LEFT; `ArcadeTop` HIS, READERS, HIS_AGAIN. `started`, `taken` and `finished` are called from
  `BorrascaArcade.click` and `submit`; `tick` from `BorrascaStory.reader`; `kelly` from `BorrascaStory.pact` before it walks her on;
  `talk` and `later` from `talkTownChild`; `arrived` from `BorrascaStory.arrive` and `rejoined`, which settles an afternoon left
  unfinished. While the scene is on, `tagAlong` and `greetChildren` leave the two alone. A game of
  theirs is `Seed`, `Way`, `Start` (game time), `Length`, `Score`, `At`, `What` under their prefix; `PatGames` counts his quarters
  (two).
- **Getting in.** `BorrascaCast.recall` to the sidewalk (5,0,-98, in the world relative to the base) when unwatched, then on foot
  through the door (7,0,-98) to (8,0,-98) and to their places: Patrick (8,0,-94), Kendra (9,0,-96), Kelly (10,0,-96); the cabinets'
  fronts are (9,0,-94) and (11,0,-94). The scene opens the door for them and shuts only a door it opened, never on the reader.
- **Watching.** `BorrascaArcadeWatchPayload(game, seed, way, elapsed, name, table)` opens `client.BorrascaArcadeScreen` with a
  `Hand`: the game is run on in silence to where it has got to and then tick for tick. Nothing is sent back.
- **Words on the screen.** `SpeechCaptions.render(g, delta, lowest)` draws the same captions with their lowest frame at a given
  height; the cabinet's screen calls it so that the people at the reader's shoulder can be read across the top of the field.
- `DeepSeam.struck` records what a cart was lost to. Protocol 179 (one payload). No layout change.

## Checks

- `BorrascaTests.patrickAndKendraTakeTheirTurnsAtTheCabinets` plays the whole afternoon: the two in through the door to their
  places, the door shut; Patrick's advice and Kendra's answer; Kelly at the comics; the gloat, the trick, Kendra at BIG CAT with a
  real game out of its seed, watched without a quarter; the reader over Patrick, "Move!", "My go.", his hopping game lost to three
  timbers and called each time; the choice, the told game over nine hundred, PAT back at the top; all three out by the door and on
  up the street; the account; and Kendra's line the next time, with each of its four forms.
- `BorrascaTests.theCabinetGamesScoreTheSameWhenPlayedAgain` now also plays twenty-four seeds with each hand twice: the same game
  both times, the hopping hand losing all three carts to timbers, going round worth more than twice hopping.
- `BorrascaTests.theDrugstoreCabinetsPlayTheirOwnGames`: seven hundred goes in second, under Patrick's seven thirty.
- The client proof's arcade view draws both games as these hands play them.

## Not in this version

- The reader's own game is not answered while it is going, beyond Patrick's one piece of advice: the server only learns how a game
  went when it ends.
- Kelly does not take a turn. She says "I get next go" when she puts in a quarter the reader is short, as before, and does not
  collect.
