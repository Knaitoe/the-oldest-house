# Fallow: build plan

The second of the three vignettes the owner approved on 17 November (`docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`, section 2), built
to the proposal's recommended decisions:

- **The clock:** four minutes from the lids opening to the light (three if playtests find it drags).
- **A reader outside when the light comes:** the House's faint, not a death. They wake at the House's door with everything they carried,
  and their shadow stays on the station's wall. The shadow is the cost.
- **The girl's gift:** shown. The stubble round her grows back while she sits on the cellar steps.
- **A refuge for readers who don't make the cellar:** not at first.

It is built in five parts, like *Not Yet* and *Last Seen*, after *Last Seen*. Each part is a release with its own doc listing every new
line. The House does not deal it until part 2. It becomes a Witness source in part 5, of survival.

## The place

`LabyrinthPlace.FALLOW` ("fallow", recurring, the next free slot after *Last Seen*), outdoors under the outside sky, about 96 × 128, on its
own scene clock (a late summer afternoon, then the light, then ash days). Every position is relative to the base, the ground just inside
the House's door; north is -z.

- **The station** on a two-lane road running east and west.
  - Out front: two pumps on an island under a canopy, a sign on a pole.
  - The store: a counter and register, coolers, a rack of road maps, a radio on a shelf, a television on a bracket, a coffee machine.
    The House's door is the store's back door, so a reader comes in behind the counter.
  - Round the side: a garage bay with a car up on the lift.
  - Behind: the keeper's trailer, and the storm cellar's slanted doors in the yard.
- **The storm cellar:** stone steps down, shelves of jars and tins, a lantern on a hook, a cot, a water crock.
- **The wheat:** flat to a haze that hides the scene's edge. Across the road, a farmhouse behind a windbreak; telephone poles along the
  road.
- **The pads:** two fenced squares of gravel out in the wheat, each with a flat concrete lid, a pole and a light.

## The people

All on stranger phases after *Last Seen*'s, painted on the cast's grid. Every name is original.

- **The keeper**, who runs the station.
- **A woman and her daughter**, travelling, with car trouble. The girl, at child scale, carries a bean plant in a paper cup.
- **The woman's man**, short-tempered and in a hurry.
- **A big man**, a long-haul driver whose rig sits at the pumps.
- **The stranger**, who on the first visit buys a soda, asks the girl her name (she doesn't tell him), asks the time, and walks off into
  the wheat toward the pads. His face changes only when nobody is looking, by the House's usual rule.

## The parts

| Part | What it adds |
| --- | --- |
| 1 | **The station.** The store, the garage, the trailer, the cellar, the wheat, the road, the farmhouse and the pads. Everyone at their places, each with their own line. A hand on anything answers. Its own art, ambient sound and door bleed. Not dealt. |
| 2 | **The afternoon.** Minding the store: the keeper's chore at the register while he works under the car. The girl's plant. The stranger buys his soda and walks into the wheat. The House deals it from here. |
| 3 | **The four minutes.** The tone on the radio, the blank card on the television, the man laughing. The lids slide open, the ground shakes and the missiles go up. The clock. Getting people down: the woman by the hand, the doors held, the girl carried. The light: a shadow on the wall for each reader outside (painted from their skin, in their pose), the faint; for readers in the cellar, the light in the cracks, the shaking, the station falling in. |
| 4 | **Buried.** The lantern's finite oil, days by its light, the warm ceiling, the girl's plant growing in the dark, digging out a stroke at a time (faster together, the big man helping). Outside: ash, stubble, craters, the poles down, the shadows on the wall. |
| 5 | **The road.** The girl's gift. Readers who were outside come back down the road and find their shadow. The stranger with each face in turn, the offer of a ride, and the choice on the cellar steps. The account in the keeper's ledger, singed, under the counter. The Witness source, the stair, the together lines. |

## The beats, as the House keeps them

- **One clock.** The station's afternoon, and the four minutes, run once for everyone there. Where each reader is when the light comes
  decides their path: the cellar (part 4, then 5) or outside (straight to 5).
- **Each reader's own:** where they were at the light, their shadow, who they brought down, whether they dug, what they told the stranger,
  and their account. Spectators earn nothing and leave no shadow.
- **Retreat.** Any visit can be walked out of by the House's door until the lids open. From the lids to the light, a reader in the place
  has committed; leaving is the same as being outside when the light comes.
- **Nobody dies.** A reader outside at the light faints and wakes at the House's door. The woman's man and anyone not brought down are
  shadows on the wall afterwards; nobody is shown being hurt.

## The House's read of the reader

Where the House has read a reader in the quiet rooms, *Fallow* uses it, never naming what it read:

- **AGE.** The keeper asks a reader the House reads as older to mind the till, and one it reads as young to keep the girl company while
  her mother sees to the car.
- **HOME.** In the buried days the big man asks whether anybody is waiting on the reader at home. What he says next follows the House's
  read (somebody to get back to; somewhere not to go back to; nowhere at all).
- **SELF.** The stranger's words to the reader go through the House's address, as every line naming a reader does.
- **TONE and HAIR.** The reader's shadow is cut from their own skin's silhouette, hair and all; the face the stranger wears last is the
  reader's own, painted from their skin.

## Art and sound

- **Drawn in code** by `tools/draw_fallow_art.py`, at Minecraft's own pixel scale: pumps, canopy, the store's fittings, the register,
  the radio, the television, the lift, the trailer, the cellar doors and shelves, wheat, stubble and ash, the pads' lids and fences, the
  farmhouse and poles.
- **The strike:** missiles as entities with smoke trails; the flash as a screen effect (Borrasca's flash is the start).
- **The shadows:** a wall decal painted from a reader's skin silhouette in their pose at the light, the way the first-death portraits are
  painted.
- **People** painted on the cast's grid by `tools/paint_character_skins.py`.
- **Sound** by `tools/generate_fallow_audio.py`, every one subtitled: the broadcast tone, the launch, the blast, the cellar settling,
  digging and the wind.

## Writing

No colour or age is named. The text never says how the reader feels. The reader's gender comes only through the House's read. Speech is
over heads and in the conversation panel, never in chat. Every new line is flagged in its part's doc.
