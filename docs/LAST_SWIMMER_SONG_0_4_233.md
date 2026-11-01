# The last swimmer, against the song (0.4.233)

The owner, 1 November: "I'd like to play the last swimmer against Armand Hammer's Falling Out the Sky lyrics, contextually." And
then, asking whether the lyrics had been reviewed: they had not. 0.4.227 was written against reviews' description of the song's
mood and its title, and called it "something slow and warm, a song for an afternoon". That was wrong. This release plays the
closing against what the song actually says.

No lyric is used anywhere in the mod, its texts or its documents: the words belong to the artists. What follows is a paraphrase,
made from a lyrics page and an interpretation read for this purpose (readdork.com, songtell.com, Wikipedia's *Haram* page).

## The song

Armand Hammer and The Alchemist, with Earl Sweatshirt: "Falling Out the Sky", *Haram* (2021), track 9, 3:40. Three verses and two
spoken interludes, no hook.

1. **Earl Sweatshirt.** Numb, private: a parent's body he never cried for; a ride that only goes forward, with no reverse; a night
   sky of supernovas and dead stars; still rooting for his own. The title's image closes the verse.
2. **A spoken interlude.** Everyone is a star; the trouble is that some were never dipped and poured back out.
3. **billy woods.** A summer away: waking somewhere new, a job painting houses by the shift, months passing; then heading home
   with something hidden in the car, and a trooper waiting on the road.
4. **A spoken interlude.** A daydream you steer until it takes over, and things come apart in front of you.
5. **ELUCID.** Childhood summers, then a pool where someone drowned the summer before he learned to swim in it, and, diving under,
   voices in the deep end he couldn't make out; then a graveyard.

Its motifs are water, falling, stars and dead light, bodies, time that only runs one way, and the past leaking into the present.

## The closing, played against it

- **The radio's clock.** The office radio starts when the closing does (its sound event, `swimmer.radio`, still ships empty; a
  player who owns the track can drop it in from a resource pack, as in 0.4.227). From then a clock runs through the song's parts:
  `LastSwimmer.SONG`, the ticks at which each begins (0, 940, 1280, 2620, 2960) and its end (4400, 3:40). The starts are estimated
  from the verses' lengths, not timed against the recording; they are one array, to be tuned to the track.
- **The daydream that comes apart.** While the radio is in its second spoken part and the cover is still being pulled, the hall's
  lights waver, one bank and then another, and steady: "All down the hall the lights waver, one bank and then another, and steady."
- **The last verse.** When it begins, the splashing stops and nothing comes up, for five seconds; then it goes on further down the
  lane "as if it hadn't". The swimmer's lengths go on all the while (they never change). The account remembers it for anyone who
  was in the hall.
- **The deep end.** Under the water at the deep end, while the pool is still open, a reader hears voices they can't make out, for
  them alone, now and then: "Under the water at the deep end: voices. You can't make out what they're saying." The account
  remembers it: "I went under at the deep end, once. There were voices down there. I couldn't make out a word."
- **The summer before.** The note under the checklist ends "They haven't since last summer." The closing log goes back to the start
  of last summer, and on its first line the fourth column, IN AT CLOSE, was filled in, scored through and filled in again: 1. Who
  went under is never said.
- **Dead stars.** The skylight lays seven stars on the water under it, a handle and a bowl (`LastSwimmer.STARS`). The cover puts
  them out as it reaches them, and when the last goes: "The cover has taken the last of the sky off the water." The account says
  so.
- **No reverse.** The reel's crank, worked crouching as if to wind it back: "The crank only turns the one way."
- **The radio itself**, clicked: "A radio, turned low. Voices over a slow, warped loop, more talk than tune. Someone on it is
  talking about the stars." Once it has played out: "The radio has gone to a hiss between stations."

Nothing changes in how the story is played or resolved: the same cover, the same steps, the same held door and Witness line.

## Tests

`LastSwimmerTests#theClosingIsPlayedAgainstTheSongOnTheRadio` (literary namespace): the radio's clock through five parts and
3:40; the stars going out with the cover; the note's "since last summer"; the crank refusing to turn back; the radio reaching its
second spoken part; the last verse holding the splashing and the swimmer unseen, remembered for the reader in the hall; under the
water at the deep end, the voices, and the account remembering them. The 0.4.227 case is unchanged.
