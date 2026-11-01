# The last swimmer (0.4.227)

The owner, 1 November (`docs/PHYSICAL_HOUSE_PROPOSALS_2026_11_01.md`, proposal 4):

> "A small municipal pool at closing time. An attendant asks you to help pull the cover across the water. Someone keeps swimming
> in the remaining uncovered lane. You hear each turn against the tiles, but only catch brief glimpses beneath the surface. As the
> opening narrows, the swimmer's lengths keep taking the same amount of time. Once the cover closes, the splashing stops. Then wet
> footsteps cross the cover toward you, pressing upward from underneath. The attendant quietly holds the exit open. That's the
> ending: an ordinary closing routine becoming something you're relieved to leave."
>
> "I'd like to play the last swimmer against Armand Hammer's Falling Out the Sky lyrics, contextually."

Slot 146, `LabyrinthPlace.LAST_SWIMMER`, one visit, indoors, built whole (`SwimmerSite`). A new understanding story: Witness 70
sources / 53 required / two kinds / three endings. Layout 72.

## The pool

- **The lobby** (x -4 to 4, z 0 to -5): lino, a bench, the reception counter on the left with the **closing checklist** (the
  source) and the **closing log** on it.
- **The office** behind the counter: a desk with the radio on it, playing; a window onto the lobby and a long window onto the hall.
- **The lockers**: a short corridor into the hall. One locker is ajar: a towel folded square and a pair of shoes, dry.
- **The hall** (x -14 to 14, z -10 to -44, eight high): tile walls with a band, an acoustic ceiling, a skylight over the middle of
  the pool with the night above it, deck tile all round.
- **The pool**: six lanes, two blocks each, 25 blocks long (z -13 to -37), shallow at the south end and deep at the north, a dark
  lane line down each lane, lane ropes between, numbered starting blocks at the shallow end, depth tiles on the deck, rails at the
  deep end.
- **The cover** on its reel along the east deck, a stand with a crank wheel at each end. **The pace clock** on the north wall, its
  sweep hand at a five-second mark. Signs: NO RUNNING, LAST SWIM. **The fire door** in the west wall at the south end, under an
  EXIT sign: the way on.
- Drawn by `tools/draw_swimmer_art.py` (55 textures; nine materials, one fitting block of 18 kinds). Three sounds by
  `tools/generate_swimmer_audio.py`: the turn against the tiles, the reel, the step under the cover.

## The closing

All of it belongs to the pool, so readers who come in together close it together; a reader on their own gets their own copy.
It starts again from the beginning when nobody has been in it for five seconds. Nothing in it can hurt.

1. **The attendant** (stranger phase 51, a polo shirt and a whistle on a lanyard) waits at the north end of the reel: "Closing up.
   Take the strap at the shallow end, would you? We pull it across together."
2. **The swimmer** (phase 52, a pale swimmer in a cap and dark goggles) swims lengths in lane four: fifteen seconds a length
   (`LastSwimmer.LENGTH_TICKS`, 300), so every turn comes round on a quarter of the pace clock. Each turn against the tiles is heard
   at that end; the splashing moves down the lane. They are seen only in glimpses: pushing off each wall, and now and then for a
   moment in the middle of a length.
3. **Hauling.** From the south deck, working the reel's crank (or the cover's strap) hauls the cover out one column across the
   water, the attendant keeping pace along the north deck. Seventeen hauls cover the pool. The attendant speaks as it goes: "That's
   it. Together." "Don't mind them. They always stay till the cover's on." "Every night." "Keep going." "All the way. It's all
   right."
4. **The lane.** Each time the cover reaches the lane they're in, at their next turn they're a lane further over. Their lengths
   never change. With one lane left, the last hauls cover it.
5. **The cover closes.** The splashing stops. The attendant says "There." The lights go off bank by bank from the far end,
   leaving the two by the fire door.
6. **The steps.** Something crosses the cover from where the swimmer was, toward the reader: one cell at a time, the cover pressed
   up under each step, a wet slap and water running off. Standing on the cover, the reader feels it come up under their feet.
   When it reaches the edge nearest them it waits there, pressing now and then.
7. **The door.** The attendant walks down the west deck to the fire door and holds it open, saying nothing.
8. **Walking out is the ending.** At the held door the attendant puts the closing log in the reader's hand and lets the door go
   behind them: the account, and the Witness line.

## The texts

- **The closing checklist** (source): the printed card (call last swim; clear the pool; cover on, two to pull, from the reel side;
  lights off; out by the fire door; NEVER put the cover on with anyone in the water), and under it, in biro, another hand: don't
  call them out, don't wait for them, cover on, lights off, out by the fire door, hold it for whoever helped. Under that, years of
  initials. Nobody has crossed out the printed line.
- **The closing log** (account): every line as far back as the page goes says 1 in the column IN AT CLOSE; tonight's line is
  filled in but for the second initials. Then the reader's own page, which changes with what they did: whether they watched the
  pace clock, whether a step came up under their feet, whether they read the checklist.
- **Witness**: "The last swimmer", understanding.

## Against "Falling Out the Sky"

0.4.233 corrects this section: the song is not a warm summer song, and the closing is now played against what it says; see
`docs/LAST_SWIMMER_SONG_0_4_233.md`. What follows is what 0.4.227 shipped.

The scene is written against the song (Armand Hammer and The Alchemist, *Haram*, 2021, with Earl Sweatshirt): unhurried, loping,
a summer song, played against a pool closing at night.

- **The radio.** The office radio is where the song belongs. It has its own sound event, `the_oldest_house:swimmer.radio`, played
  once from the radio when the closing starts, reaching the whole hall. It ships with no sound in it. A player who owns the track
  can fill it from a resource pack of their own:
  `assets/the_oldest_house/sounds.json` with `"swimmer.radio": {"replace": true, "sounds": [{"name": "the_oldest_house:swimmer/radio", "stream": true}]}`,
  and the track at `assets/the_oldest_house/sounds/swimmer/radio.ogg`. Nothing of the song is committed, and the check
  (`generate_swimmer_audio.py --check`) fails if a recording is.
- **The tempo.** The swimmer's lengths are one constant, `LastSwimmer.LENGTH_TICKS`, so they can be set to the track's time.
- **The words.** No lyric is used anywhere: they belong to the artists, and the House keeps no copyrighted text. The scene plays off
  the title's image instead. The skylight puts a square of night sky on the water, so whatever comes up comes out of a sky; the
  account remembers "a radio on in the office, something slow with a summer in it, and it kept on all through"; the radio, clicked,
  is "something slow and warm, a song for an afternoon, playing to nobody at night".

## Tests

`LastSwimmerTests` (literary namespace): built whole, indoors, one visit, an understanding story with the quota derived; the
checklist and log on the counter; water, ropes, the reel, deep at one end and shallow at the other with a lane line down each
lane; the checklist's printed line and the note under it. The pool starts closing with someone swimming in lane four, fifteen
seconds a length; hauling only works from the shallow end. Nine hauls cover nine columns with the leading edge on the ninth; at the
next turn the swimmer is a lane over. Eight more close the cover: every column covered, the swimmer gone, not yet the ending. The
steps press the cover up; the reader is ready (`held_the_door`); the far lamps are off. The attendant reaches the fire door and it
stands open. Walking to it completes the story and puts the closing log in the reader's hand.

`ArchitectureTests`: the pool keeps its deck, the checklist on the counter and the cover on its reel; its source is a clipboard; it
is built whole.
