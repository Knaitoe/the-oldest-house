# The fight with the Sheriff (0.4.457)

Note 24 of the owner's playtest of 10 November (docs/BORRASCA_PLAYTEST_2026_11_10.md): "Combat with the sheriff shouldn't'
feel like a vanilla Minecraft fight. Punches should be sluggish with real weight. The sheriff should use his baton, then drop it
after being hit a few times and start swinging his own fists. He swings with one fist then the other."

What the fight is for has not changed (0.4.427). A grown reader who strikes the Sheriff has a fight. Beaten, he goes down in the
dirt with his hands up and talks. A blow after that kills him, and his body stays where it falls.

How it went has changed. He used to walk up and tap the reader with his flashlight every second or so, and the reader could
hit him as fast as the button could be pressed.

## His baton

When he is struck he has his baton out, not the flashlight.

A swing has a beginning. He raises the baton over his right shoulder and holds it there for three quarters of a second, turning
with the reader. For the last quarter second he is committed: he stops turning and steps in behind it. Then it comes down.

- It lands on a reader who is still within its reach and in front of him. It takes two hearts, knocks them back, and leaves their
  legs slow for a moment.
- A reader who has stepped out of it is missed. He is half a second longer getting his balance.
- After a swing he rests his arm for a second and a half.

## His fists

The third blow that lands on him has the baton out of his hand. It lies on the ground where it fell for the rest of that visit
(for good, if he dies there). He says what he said before at the third blow, and puts his fists up.

He throws the left and then the right. Each is drawn back first, for half a second, and each is a hook: it takes a heart and
pushes the reader across as well as back. The right follows the left whether or not the left found anything. Then he rests.

## The reader's arms

While the fight is on, a blow has to be brought all the way back: a little over one blow a second, whatever is in the hand. The
game's own attack indicator shows the arm coming back.

A blow thrown before the arm is back glances. It does nothing to him and he does not flinch. (It does rouse him, if he was not
already fighting.)

## A blow that lands on him

- He is knocked back a step and reels from it for three quarters of a second: his head goes back and his arms go out. If he had
  only begun to raise his baton or draw a fist back, that swing is broken.
- He cannot be kept reeling. Having reeled, he is steady for a second and a half and takes the next blow standing. A swing he is
  already committed to comes down whatever lands on him. So a reader who stands in front of him and trades blows is hit.
- On the reader's own screen a landed blow jolts the view for a moment, down and to one side, then the other side the next
  time. The jolt follows Distortion Effects, and is nothing at none.

## Leaving it

If the reader runs far enough he lets them go, as before. On another visit he has his baton again and it is no longer on the
ground. The reader's arms are their own again as soon as the fight is over, however it ends (beaten, dead, run from, the town
left, the game left).

## What is drawn

- Eight poses on the cast's own model: his hands up; the baton raised, and brought down; each fist drawn back, and thrown;
  reeling. The model eases from one to the next, so a raise is seen to be a raise.
- The baton: black, with a side handle. In his hand, and lying on the ground (`borrasca_shop` kind `baton`, which says what it is
  to a hand). Drawn by `tools/draw_borrasca_town_art.py`.

## How it is made

- `labyrinth/BorrascaSheriffFight`: the whole fight. The story's clock runs each fifth tick, so every length is a multiple of
  five ticks. `BorrascaStory.retribution` hands the fight to it; `sheriffStruck` tells it of a landed blow.
- `LiteraryActor.hurt`: a blow on the Sheriff that was not brought all the way back is refused. The game has already made such a
  blow smaller, so it is told from a whole one by its size against the reader's own attack damage.
- The reader's slow arm is a transient modifier on attack speed that the fight owns. A check on the reader's own tick takes it
  off anyone who still has it and is no longer in a fight.
- `client/BorrascaFightClient`: the jolt.

No layout, protocol or Witness change.

## Checks

- Native: a new Borrasca case. A part-drawn blow glances and only rouses him. He has his baton out and the reader's attack speed
  is held down. The baton is raised before anything lands, then lands on a reader who stood there and slows their legs. A reader
  moved out of reach is missed. The first whole blow reels him. After two the baton is still in his hand; the third has it out and
  lying on the ground. Then the left fist and the right, each drawn back before it is thrown, in that order, and one lands.
  Beaten, he goes down as before and the reader's arms are their own again. On another visit the baton is off the ground.
- The three existing Sheriff cases pass unchanged.
- Client proof: a new view (`fight`), the eight poses from two sides, the baton in the hand and on the ground.

## What I could not check

I have not played this fight by hand. The timings are tested, and the poses are seen in the proof, but how it feels in the hand
is the owner's to judge: how long the raise should be, how hard the baton should hit, how slow the reader's arm should be. Each
is one number at the top of `BorrascaSheriffFight`.
