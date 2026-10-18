# Costume Night, reworked — 0.4.103

On October 18 the owner asked for Costume Night to be reworked and approved this plan. The candidate is 0.4.103,
protocol 75 and layout 45. It changes new builds only: the mod is unreleased, and the owner judged mid-save repairs
unimportant.

46 sources / 35 resolutions / two kinds / three endings are unchanged. The Witness story `costume_night`, its outcome id
(`reached_living_grass_beyond_the_costumes`) and the "survival" kind are kept. The Witness title becomes "The thirteenth
witch", and its line "One witch held still whenever I looked. I reached the park grass and kept her in sight."

The Indian Lake setting (Proofrock, the lake and Stacey Graves, the Lake Witch) comes from Stephen Graham Jones's books.
This Halloween is the House's own, built on that setting and on the Shallows (0.4.101), where the reader was one of the
boys who threw her.

## The beach

`CostumeNight.build` builds the scene whole, like Movie Night and the Last Stop. The polish, composition and review
passes leave it alone (`ScenePolish`, `SceneCraft.GROUNDS`, `SceneReview` and `SceneHuntReview`). The nine armor stands
and their shoreline pass are retired, and so are Stacey's rock recesses here (`StaceyCover.SITES`).

- **The beach.** Sand from the entrance to the park. The judges' table, with Hank's card on it, is where readers come in.
- **The party** fills the middle of the beach:
  - a plank stage with steps and an orange and black backdrop, and the contest board beside it
  - a bonfire ringed with hay bales, with the radio on one of them
  - the tub for bobbing apples, and a candy pail on a bale by the stage
  - carved pumpkins
  - light poles at the four corners with strings of coloured bulbs between them, and one string across to the stage
- **The lake** runs along the east side of the beach. Its shallow edge is where she waits.
- **The town park.** Living grass beyond a row of low posts at the north end, with oaks, a bench and a lamp. The account
  waits on the grass.
- **The dunes.** Dark spruce on podzol along the west and on the far bank. There is no grass anywhere but the park.

## The contest

Thirteen people, each with a skin painted by `tools/paint_character_skins.py` on stranger phases 21 to 33:

- **Hank** judges from the stage. He is the boy from the Shallows, grown old.
- **Twelve entrants**, each wearing a red number on a white card, each a homemade Lake Witch. Four are children, at child
  scale. Five do "the witch crawl" on all fours from the start.

| No. | Entrant | Costume |
| --- | --- | --- |
| 1 | Tina | garbage bags and a black yarn wig |
| 2 | Danny | a bedsheet with weeds sewn on |
| 3 | Mr. Askew | a rubber mask and his wife's bathrobe |
| 4 | Kaylee | white face paint and a green yarn wig |
| 5 | Marcus | lake weed and his dad's waders |
| 6 | Shelly | a grey dress and long black hair |
| 7 | Owen | a cardboard mask with drawn teeth |
| 8 | Brooke | a mop dyed green |
| 9 | Wes | a hoodie with the hair stapled in |
| 10 | Rosa | a soaked nightgown and a black wig |
| 11 | Pip | a pillowcase with eye holes |
| 12 | Lyle | a flowered dress, beard and all |

Everyone talks when right-clicked, and every so often someone says something to a reader nearby. Spoken to twice, Hank
tells how the contest began: he and three friends started it the summer after the shallows, so that if every child in
town was her, nobody would be afraid of the real one. He gives the ribbon to whoever is most like her.

The apple tub gives each reader one apple, ever, and the candy pail one sweet. The radio answers too.

## The night

One shared clock counts while anyone is in the scene:

1. **The party.** Every twelve seconds Hank calls the next number through his megaphone. That entrant walks (or crawls) to
   the front of the stage, shows off while he comments, and goes back. The radio plays a surf tune.
2. **The ribbon**, at 45 seconds. "This year's ribbon for Best Lake Witch goes to... the one down by the water! Come on
   up, honey!" Nothing comes up. "Who... who is that?"
3. **Lights out**, 13 seconds later. The bulbs pop and go dark, the radio dies and only the bonfire is left. Every entrant
   drops to all fours for the crawl and crawls about their place, giggling ("Mr. Hank? Is this part of it?").
   - Hank: "Everybody stay where you are! Lights'll be right back!"
   - Then: "Keep your eyes on it. Whatever you do, don't you look away from it."
   - Then: "Get to the park. She never would cross the grass."

When the scene has been empty for five seconds and no camera is within 70 blocks, everything is put back for the next
reader: the lights on, everyone in their places, standing or crawling as they came, and her at the water.

## Her rule here

The thirteenth witch has no number. Water drips from her: a few drips of particles, and a drip sound for anyone within
eight blocks.

- **She moves only while no living reader there can see her.** While any reader (spectators and creative players aside)
  has her in view, she does not move at all, lights or no lights. With nobody on the beach she also stays where she is.
- **Before the lights go out** she only shifts between four places at the water's edge, slowly, when nobody is looking,
  and never strikes.
- **Once they are out** she comes for the nearest reader on the sand at twice a running pace. If she reaches them unseen,
  she lunges and strikes for two hearts. Then she goes back to the water for four seconds.
- **Where she can go.** She never sets foot on the park grass, so readers there are safe. She also cannot come within ten
  blocks of the entrance.
- **The tell is the reverse of the crowd's.** The children move while you watch them; she never does. Every time you look
  back, she is closer.

This rule is Costume Night's alone. Movie Night keeps its fireworks, and Winter Lake keeps her ordinary hunt.

## The way out

The scene is ready once all three of these hold, after the lights are out:

- the reader has seen her;
- the reader has been out on the sand;
- the reader then stands on the park grass.

A reader who waited on the grass from before the lights went out is told to go back onto the sand and cross with her in
sight. A reader who reaches the grass without having seen her is told to look back across the sand. A reader who reaches
the grass before the ribbon is told the contest isn't over.

## Words

- **The source** is Hank's judge's card, "Judge's card", signed "Hank, for the committee", in his voice:
  - twelve entrants, numbers pinned on, score each out of ten;
  - a good one holds still when you look at it, as she always did, and when you look away it's closer;
  - keep them on the sand, and if it ever comes to anything, the park grass, which she never would cross.
- **The account** is titled "The account: the ribbon". It is told from what the reader did and heard:
  - the thirteenth witch with no number, if they got close to her before the ribbon;
  - Hank's story of the contest, if he told it to them;
  - Hank looking at them as if he knew them from the water's edge, if they threw her at the Shallows;
  - the ribbon nobody came up for, and the crawl in the dark;
  - being caught once while looking at someone else, if she struck them.

  It ends: "Then the grass was under my feet. Behind me thirteen witches were crawling in the dark, and one of them was
  not playing." The shared folded-paper afterword follows.

## Assets

- **Thirteen character skins**, `literary_costume_*`. The painter gains costume features: yarn and mop wigs worn over the
  face, rubber and cardboard masks, face paint, a pillowcase, a hood, garbage bags, weeds, a soaked gown, a flowered
  dress and the number card. The art catalog gains thirteen rows, and the character art proof now has 23 views.
- **The `costume_prop` block** (48 variants), drawn by `tools/draw_costume_night_art.py`:
  - the bulbs, lit and dark, giving light 11 while lit
  - the radio, with its dial lit or dark
  - the apple tub and the candy pail
  - the contest board in two halves: the title band, a crawling witch, twelve entry cards and a blue ribbon
- **Six subtitled sounds** synthesized by `tools/generate_costume_night_audio.py`: `costume.radio`, `cheer`, `pop`,
  `megaphone`, `drip` and `giggle`.
- **The crawl.** A new actor pose on all fours (gesture 5), with a body 1.25 blocks high.

## Validation scope

- **Stacey's general cases move to Winter Lake.** Ten Stacey cases that used Costume Night as their test bed now use
  Winter Lake, which keeps her ordinary hunt; so do the two scene-review witch cases and the live expedition's door break.
  The scene-review witch strike case no longer checks grass, because grass is a refuge only on Costume Night and is
  tested there.
- **One case is retired:** the costume-stand bay case, because the stands are gone.
- **Three cases are added:**
  - **The contest and her rule.** Everyone is present in their own skin, the crawlers are low and the children small.
    Before the lights go out she never strikes, even unseen beside a reader. While watched she does not move at all,
    even with the lights out. Unseen, she comes and strikes. On the park grass nobody is hurt, and she never steps on it.
  - **The ribbon.** Hank tells his story; there is one apple and one sweet; number one is called up and goes back. Through
    the ribbon nobody is hurt. Then every bulb and the radio go dark, every entrant crawls and Hank stays standing.
  - **The way out.** The crossing ends the night, but a reader who waited on the grass has not crossed. The account tells
    only this reader's night, and reading it completes only theirs. Once the beach is empty the contest resets: lights
    on, everyone home and her at the water.
- **A native visual proof** saves three views: the props lit, the props dark, and five entrants on all fours beside her.
  It checks the crawl's height and length, and that every prop texture and costume skin is present.
