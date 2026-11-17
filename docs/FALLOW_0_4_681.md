# Fallow 4: buried (0.4.681)

The fourth part of *Fallow* (`docs/FALLOW_PLAN.md`). Part 3 (0.4.674, 0.4.680) brought the afternoon to the light, the shadows and the
station falling in. This part is the cellar after: the days below, the lantern, Opal's bean, Otis's question, and digging out. Part 5 is
the road, where readers who were outside come back, and the Witness source.

Fallow is still not a Witness source.

## What happens

Times are on the afternoon's own clock, which only moves while somebody is at the station. The buried days begin when the ground is still,
two hundred ticks after the light (`FallowBuried.SETTLE`); a day below is 900 ticks (`DAY`), and night falls 660 ticks into it (`NIGHT_AT`).

1. **At the light.** A reader below stays below; only readers out in it faint (part 3's faint for those below is gone). Otis and Opal are
   below whatever has happened before. Anyone else of the five who is out in it is gone from the station: Harlan if he stayed at the doors,
   Faye if she stayed at the car, and Boyd, who would not go down. Boyd's shadow is left on the front beside where he stood, as a reader's
   is, in a plain body, an arm up if he was reaching for the rig's door.
2. **The ground still.** Those of the five who are below walk to their own places (Otis at the far end, Opal by the cot's foot, Harlan by
   the shelves, Faye by Opal). Opal kneels and sets her bean down by the cot's foot. The lantern burns. The fall of the store over the
   doors lets no daylight through: below, there is only the lantern.
3. **The lantern.** It has 2,400 ticks of oil. Turned low it burns half as fast and gives half the light. A reader's hand turns it down,
   puts it out and lights it again with a match from the shelf, while there is oil; a dry lantern stays dark. In its last 240 ticks it
   gutters, its flame jumping, and then goes out for good. Otis puts it out at nightfall to save the oil, and lights it again in the
   morning if it has oil. A reader who lights it at night hears from him once that night.
4. **The roof and the stores.** A hand on the cellar's roof finds it hot the first day, warm the second and cool from the third. Before the
   light it is planks on timbers with the yard's earth over them. The crock and the shelves go down by the day.
5. **Opal's bean.** It grows a stage for every 160 ticks the lantern stands dark, three stages at most, the last long and leaning toward
   the steps. When the light comes back and it has grown, those below are told, and Opal says so.
6. **The people below.** By day they sit, by night they doze, and every so often by day one of them says something, by the day and by
   who is below (`FallowTexts.BELOW_TALK`). A word with any of them gets their own answer, by the day.
7. **Otis's question.** From the second day, by day, Otis asks each reader below, one at a time, whether anybody is waiting on them back
   home. The reader tells him or says nothing. What he says to an answer follows the House's read of the reader's home: somebody to get
   back to, somewhere not to go back to, nowhere at all, or nothing read. To silence he says they need not say.
8. **Digging out.** From the third day (when the roof is cool) the doors can be dug at: from the steps below, shoulders to the doors, or
   from the yard, dragging at the fall. Before then they are too hot, from either side. Each reader strikes at most once a second; Otis
   comes up the steps to dig beside whoever is digging, and from the fourth day he begins alone. Twelve strokes clear a length of the doors,
   in order from the top of the steps; the first length lets a crack of daylight through, and once half are clear ash sifts down. When all
   eight are clear the doors give and go back over on their hinges.
9. **Up.** Otis comes up and looks out at it; Opal comes up with her bean in its cup and sits by the steps; Faye and Harlan, if they were
   below, come up after. A reader who was below and climbs out is told what they see.
10. **Held.** While the doors are shut over readers below, the station is kept if all of them leave the game, for 1,200 ticks rather than
    the usual hundred. Nobody is held from the House's door once the faint is over. A reader who was never at the station before the
    light is not dealt it while it lies fallen.

## Each reader keeps

`Days` (how many days below), `Lantern` (hands on it), `AskedHome`, and `ToldHome` with `HomeRead` (the read when told) or `QuietHome`,
`Dug` and `DugAbove` (strokes from each side), `DugOut` (struck at least once before the doors gave), `CameUp` (was below and came out).
Part 5 reads them.

## Single and multiplayer

- One reader below has the days to themself: the lantern, the bean, the question, and, from the fourth day, Otis digging beside them or
  alone. One reader who was out in it can come back and dig from the yard alone.
- With more, the cellar, the lantern, the bean and the doors are shared: one lantern, turned by any hand, one bean, one heap over the doors,
  struck at from both sides. Otis asks each reader below their own question, in turn, and what he says to each is theirs alone. Each
  reader's days, strokes and coming up are their own. Spectators strike nothing, are asked nothing and keep nothing.

## On the reader's screen

- The lantern low is a new stage of the crock (`crock_3`, glass `lantern_low`), half the light of the lit one.
- The bean's third stage is two crossed sheets, each tipped toward the steps (`bean_3`).
- Sitting and dozing are drawn for the station's people (`LiteraryRenderers.fallowSat`): seated, the body still and the head breathing;
  dozing, the head down and the arms folded.
- Boyd's shadow is the plain body, an arm raised.

## Made as

- `FallowBuried` (new): the light, the days, the lantern, the bean, the people, the question, digging, coming up, the reader's record.
- `FallowStrike`: the buried days from the light on; readers below stay; held only to the faint; the NPC routine stops at the light.
- `Fallow`: the buried days' hands first; a held station kept longer; a fresh reader not dealt a fallen station.
- `FallowRuin`: `recover` (a head on the steps that kept a length clear) and `shadow` for one of the five.
- `FallowShadowBlock`, `FallowShadowRenderer`: a shadow may be one of the five, in a plain body, reaching.
- `FallowBlock`: the lantern's low stage; the fall over the doors blocks all light.
- `ActorSeat`, `LiteraryRenderers`: the station's people sit and doze.
- `tools/draw_fallow_art.py`: the low lantern glass, `crock_3`, the leaning `bean_3`. `tools/generate_fallow_audio.py`: five sounds.
- `FallowTests.theBuriedDays` (new): two readers below at the light and one out in it; Boyd's shadow and the others gone; the bean set
  down, the lantern lit, no daylight; the roof by the day, the doors too hot; a hand turning the lantern down, out and lit again; night,
  the bean grown and seen in the morning; Otis asking each reader below, answered by the read and by silence; the doors dug from the steps
  and the yard, Otis beside them, a length clear letting the daylight in, the doors giving; who dug out and came up; the five up, Opal
  with her bean; the station built again with the cellar as it was. `theFourMinutes`: the one below at the light now stays below.

## Every new line

Flagged for the owner. Nothing here names a colour or an age or says how the reader feels; the cues are on the action bar, the people's
lines over their heads.

**Cues** (`FallowTexts`):
- Still: "The shaking stops. Overhead, something goes on burning."
- The roof before the light: "The cellar's roof: planks on timbers, with the yard's earth over them."
- The roof, by the day: "The timbers overhead are hot. Something up there is still burning." / "The timbers are warm, but a hand can
  stay on them now." / "The timbers are cool. Whatever was burning up there has burned itself out."
- Too hot, from below: "Heat comes down through the boards of the doors. Whatever lies on them is still burning."
- Too hot, from the yard: "Heat comes off the heap over the cellar doors. Under it, something is still burning."
- First stroke from below: "You set your shoulder to the doors and heave. Dirt sifts down, and something overhead shifts."
- First stroke from the yard: "You drag at the fall over the cellar doors. A piece of it comes away in your hands."
- A length clear: "Between two boards over the steps, a crack of daylight."
- Half: "Half the doors are clear. Ash sifts down between the boards."
- The doors give, below: "The doors give and go back over on their hinges. Ash comes down the steps on the air."
- The doors give, from the yard: "The cellar doors heave up out of the fall and go back over on their hinges."
- Coming up: "Up the steps and out. Ash is coming down out of a sky with no sun in it, and lying on everything."
- The lantern: "You turn the wick down. The light draws in to the lantern's glass." / "You turn the wick down until the flame goes
  out." / "You strike a match from the jar on the shelf and light the wick." / "The lantern is dry. There is no oil left in it." /
  "The lantern gutters. Its flame shrinks and jumps." / "The lantern goes out. There is no more oil."
- Mornings: "A second morning, by Otis's watch." / "A third morning, by Otis's watch." / "A fourth morning, by Otis's watch." /
  "Another morning, by Otis's watch."
- The bean grown: "In the lantern's light, Opal's bean is taller than it was. It has grown in the dark."
- The crock, by the day: "The crock of water, near full. The dipper hangs back on its side." / "The crock, down a hand's width. Nobody
  takes more than the dipper holds." / "The crock, half gone. A line is scratched on its side at the level each morning."
- The shelves, by the day: "Jars of peaches, beans and tomatoes. A jar of peaches is open and going round." / "Two jars are gone from the
  shelf. The empties stand back in their places, lids on." / "The shelf is thinner. The dates on the lids go back eleven summers."

**Otis:** "Let's put it out a while. Save the oil for when we need to see." / "Morning. Near enough, by my watch." / "Morning, by my
watch. For what that's worth down here." / "Let it be dark a while. We'll want that oil." / "It's cooled up there. Shoulders to the
doors: push when I push." / "I'll make a start. Spell me when you've a mind to." / "There. Up we go, slow. Mind the edges." / "Well.
There it is."

**Otis's question:** "Anybody waiting on you, back home?" Answers: "Tell him." / "Say nothing." What he says:
- Somebody to get back to: "Then you've got somewhere to get back to. Hold on to that down here."
- Somewhere not to go back to: "Some places you don't hurry back to. Nobody here's going to make you."
- Nowhere at all: "Nowhere at all? Well. You're with us now, for what that's worth."
- Nothing read: "Fair enough. You don't owe me the whole of it."
- To silence: "That's all right. You don't have to say."

**Opal's bean:** "It grew. In the dark, it grew." / "Look how tall. It doesn't even need the sun." / "It's leaning at the doors."

**Below by day:**
- The first day: Otis, "That's the store burning up there. We sit tight till it cools." Harlan, "Peaches on the second shelf. Mind the
  jars, they're all there is." Faye, "Boyd got into the rig. That rig's steel all round." Opal (Faye not below), "Is Mama still in the
  car?" and Otis, "We'll go and see, when we're out." Opal, "It's too dark for my bean."
- The second day: Harlan, "Second day. I keep wanting to go up and open the store." Faye, "My sister will have called the highway patrol
  by now." Opal (the bean grown), "I gave it some of my water. Only a little." Otis, "I've a sister up in Duluth. She'll be watching the
  road."
- The third day: Harlan, "Built this cellar for twisters. Never thought it'd see anything else." Opal, "When we get out, can I put it in
  the ground?" Otis, "First thing, sweetheart." Faye, "Whatever's up there, we go up together."

**A word with them below:** Otis, "We dig when it cools. Not before." / "Save your strength. You'll want it for the doors." and, once it
has cooled, "Push when I push." Opal, by her bean: "It's too dark down here for it." / "It's growing anyway. Look." / "It's taller than
the cup now." / "It's leaning at the doors. It wants out too." Harlan: "There's water in the crock. A dipper each, morning and night." /
"I should have checked the oil in that lantern. I always meant to." Faye: "Did you see him get into the rig? Did you see?" / "He'll be in
the rig. He'll be all right in the rig."

**Up:** Opal, "It needs the sun. There isn't any." Faye, "Boyd? Boyd!" Harlan, "That was a good store." A word with them up: Otis, "Easy
on the steps."; Opal, "It needs the sun."; Faye, "Boyd?"; Harlan, "Mind the glass."

**Subtitles:** "The cellar creaks and settles", "A lantern wick is turned", "A match is struck", "Rubble is dug at overhead", "The cellar
doors give".

## Left for part 4b

The outside in ash: the wheat to stubble and ash, craters, the poles down, the farmhouse gone, an ash sky and fog, ash falling; and its
mend. Built in 0.4.682 (`docs/FALLOW_0_4_682.md`).
