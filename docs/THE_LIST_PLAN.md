# The list: build plan

The vignette after Stephen Graham Jones's *I Was a Teenage Slasher* (`docs/THE_LIST_PROPOSAL.md`), built to the owner's decisions of
17 November: a high school cast; the reader makes real, visceral slasher kills; asymmetric multiplayer; four nights; longer set-piece
finishes; the book's own names, and only its names. Everything the people say and do is written new.

It is built in parts, like *Fallow* and *Last Seen*. Each part is a release with its own doc listing every new line. The House does not
deal it until night one exists (part 3). It becomes a Witness source in the last part, of the memory kind.

## Defaults for what the proposal leaves open

My calls, built this way unless the owner says otherwise.

- **The House's door** is the back door of the reader's own house in Lamesa. Every night begins in their kitchen, and the night is
  left the same way.
- **One town to a slasher.** The first reader through the door in a group comes back wrong, and the town is theirs for the four
  nights: who has died stays dead, the bodies are gone by the next night and the town knows who found them, the flyers stay up.
  Readers who come with them are on Amber's side in that town (part 8). A reader on their own gets a town of their own and is the
  slasher.
- **Retreat.** Night one can be left by the House's door until the seed house door shuts. On the later nights the door stays open
  except while the reader has hold of someone (from the catch to the end of the finish, or until the name breaks free) and, on night
  four, from the moment Stace starts her plan.
- **The get-up.** Down for five seconds, then up where they fell. In the town the slasher's body never drops below a heart; a blow
  that would kill puts them down instead. Not a House death; nothing is lost.
- **The pull** is drawn at the edges of the screen and heard as the nearest name's heart. It never moves the camera or the reader,
  and it follows the visual-comfort settings.
- **Who can be finished:** the five on the list, readers on Amber's side (out for the night), and Stace on night four. Jenna,
  Janice, Amber and the adults can be shoved aside or knocked down, never finished.
- **No kill is required.** Every night can be walked through without one, and two endings (everyone gone, the bus) have none.
- **The night's clock** runs on the town's scene clock from ten at night to first light, about twenty real minutes, and stops while
  nobody is in the town.
- **Content.** The House's content warning already covers violence; the finishes are gory at sixteen pixels and stop when they are
  done.

## The place

`LabyrinthPlace.THE_LIST` ("the_list", recurring), the next free slot after *Fallow*, outdoors under the outside sky, about 192 × 192,
on its own scene clock. Every position is relative to the base, the ground just inside the House's door; north is -z.

- **The reader's house** at the base: a ranch house with a carport, a swamp cooler on the roof, chain-link round the yard, a porch
  light. The kitchen, the hall, the reader's room with the bed the hook keeps coming back to.
- **The neighborhood** round it: caliche streets, more ranch houses with sprinklers, dogs behind fences, a cotton field at the end of
  every lot. **Amber's house** across two streets, with the den, the VCR and the shelf of rented tapes.
- **Main Street** to the north: the drive-in burger stand with its car stalls and speaker posts, the video store and its horror wall,
  the pharmacy, the sheriff's substation, the feed store with the bus bench outside.
- **The cotton gin and seed house** to the east by the tracks, and **the water tower** beside them with its ladder and catwalk.
- **The high school's field** to the west: the field house, the bleachers, the press box, the field's lights.
- **The pasture** to the south: pumpjacks, the stock tank with tailgates round it, a cattle guard, a windmill.
- **The playa** beyond the gin, flat and shining.
- **The edge:** cotton to a haze that hides the scene's edge, as *Fallow*'s wheat does.

## The people

All on stranger phases after *Fallow*'s, painted on the cast's grid by `tools/paint_character_skins.py`: Amber Big Plume Dennison;
the five on the list, Deek Masterson, Mel Boanerges, Lesley Cantor, Shannon Larkweather and Wes Stripling; Stace Goodkin; Jenna
Gonzales at the drive-in; Janice Dickerson at the video store. The adults (a deputy, the names' parents) are unnamed. The reader is
the narrator's part and has their own House body; nobody in the town carries the narrator's name.

## The parts

| Part | What it adds |
| --- | --- |
| 1a | **The middle of town.** The ground (caliche, cotton, mesquite, the playa), the streets and lots; Main Street (the drive-in, the video store, the pharmacy, the substation, the feed store and the bus bench); the gin, the seed house and the water tower; the reader's house. A hand on anything answers. Its own art, ambient sound and door bleed. Not dealt. |
| 1b | **The rest of the town and its people.** The school's field, the pasture, the neighborhood and Amber's house; everyone at their places by night, each with their own line. Not dealt. |
| 2 | **The slasher's body.** The walk that never loses ground, doors that give, hearts through walls, the get-up, the mask on and off, the pull. Tried on its own before any night uses it. |
| 3a | **Night one, to the water tower** (0.4.706). The party at the stock tank, the ride, the seed house, the auger, coming to under the tower with the sack on. Not dealt. |
| 3b | **Night one, home** (0.4.707, built: `docs/THE_LIST_0_4_707.md`). The walk home, Amber at her window, first light, the other readers' night. Dealt from here. |
| 4a-1 | **The hunt** (0.4.708, built: `docs/THE_LIST_0_4_708.md`). Names as people: they notice the slasher, run, hide, shut and bolt doors behind them, yell for help and fight back once; found again, they cower. Hearts that give them away. Begun by hand until a night uses it. |
| 4a-2 | **The catch** (0.4.709, built: `docs/THE_LIST_0_4_709.md`). The staged hold and breaking free, the hay hook and its finish, blood thrown, the body left where it falls. |
| 4a-3 | **Wounds, stains and bodies found** (0.4.710, built: `docs/THE_LIST_0_4_710.md`). Wounds drawn on the five and on the slasher's body, blood left on what it lands on, the dead found by the rest of the five and by other readers. |
| 4b | **The school's set pieces**, where the hunt already is. 4b-1, **the boards** (0.4.711, built: `docs/THE_LIST_0_4_711.md`): one held on the home stands forced face down over a row, the head through the gap under the next, and stamped on. 4b-2, **the line** (0.4.713, built: `docs/THE_LIST_0_4_713.md`): one held forced back onto a sprinkler riser down the home sideline, and the water turned on. |
| 4c | **The drive-in's set pieces.** 4c-1, **the fryer** (0.4.714, built: `docs/THE_LIST_0_4_714.md`): one held in the Caprock's stand forced down over the hot fryer, the face pushed into the oil and held there. 4c-2, **the car door** (0.4.715, built: `docs/THE_LIST_0_4_715.md`): one held forced down on the knees at a car in the drive-in's stalls with the head in at its open door, and the door slammed on it. |
| 4d–4e | **The other set pieces, by place:** the pasture (**the pumpjack**, 0.4.716, built: `docs/THE_LIST_0_4_716.md`: one held forced down on the knees by a pumpjack's gearbox with the head in the counterweight's slot, and the weight brought round onto it unless someone hauls the brake on; and **the stock tank**, 0.4.717, built: `docs/THE_LIST_0_4_717.md`: one held pressed over its rim with the head under the water, pushed back under each time it comes up, until the third time it stays there), and last the gin: **the auger**, 0.4.718, built: `docs/THE_LIST_0_4_718.md`: one forced into the trough feet first and drawn down by the screw while the line runs, freed only if someone throws the switch; and **the press**, 0.4.719, built: `docs/THE_LIST_0_4_719.md`: one shoved into the bale press's chamber with the ram coming down, let out only if someone else hauls the release. The five are only taken there on night four. Each is a staged scene of its own, built for its place. |
| 5 | **Night two.** 5a, the den and the field (0.4.720, built: `docs/THE_LIST_0_4_720.md`). 5b, the field house's door, the heart through a wall and the tower (0.4.722, built: `docs/THE_LIST_0_4_722.md`). The skeleton of parts 5 to 9 (0.4.721, built: `docs/THE_LIST_0_4_721.md`): the first name and night two's hunt, nights three and four as hunts, the end, the account and the Witness story; what each part adds goes into it. Amber's den, the tapes, the tests on the field and the tower, the notebook's rules (some of them wrong), the first name going out. |
| 6 | **Night three.** 6a, the flyers and what comes back (0.4.723, built: `docs/THE_LIST_0_4_723.md`); 6b, the deputy out on his round (0.4.724, built: `docs/THE_LIST_0_4_724.md`); 6c, the field house (0.4.725, built: `docs/THE_LIST_0_4_725.md`). The hook that comes back, the flyers and the curfew, the deputy's car, the town out with flashlights, the field house. |
| 7 | **Night four.** Stace's plan, built on the rules she believes; the night ending at the gin, the tower or the playa; the endings. |
| 8 | **Amber's side.** Other readers in the den from night two: warning names, driving them out, hiding them, barricading, keeping a heart quiet, knocking the slasher down, testing the rules for real. |
| 9 | **What is kept.** The account, the Witness line, the keepsake, the House of Leaves fact, the house afterwards and the together lines. |

## The beats, as the House keeps them

- **One clock to a town.** The night runs once for everyone in it.
- **Each reader's own:** whom they finished and how, whom they got out, which rules they tested, whether they took the mask off and
  who saw their face, where they were at first light, and their account. Spectators see everything, count as watchers and earn
  nothing.
- **The town's own:** who is dead and where they were found, who has left town, what the town knows, and Stace's rules.

## The House's read of the reader

As the proposal says: SELF and HAIR draw the face on the sack and on the flyers; DRAWN_TO picks whose heart is loudest; HOME leaves
the porch light on (HAVEN), brings the town to the reader's house first on night three (DANGER) or makes the dawn bus an ending
(RUNAWAY); AGE sets how far away the account is written from. Nothing names what it read.

## Art and sound

- **Drawn in code** by `tools/draw_the_list_art.py`, at Minecraft's own pixel scale: caliche, cotton, mesquite, chain-link, siding and
  roofing, the swamp coolers, the drive-in's stalls, sign and speaker posts, the video store's shelves and its boxes (every title
  original), the pharmacy and substation fittings, the gin's machines, the seed house's bins, the water tower, pumpjacks, the
  windmill, the stock tank, the field's lights, bleachers and press box, 1980s pickups and a sedan, the feed sack mask, the hay
  hook, blood, stains and wounds.
- **Sounds** by `tools/generate_the_list_audio.py`, every one subtitled: night insects, the pumpjacks, sprinklers, dogs, the
  drive-in's speakers, the gin, hearts, the pull, the slasher's tread, doors splintering, strikes, screams, the VCR.

## Checks

`TheListTests` from part 1a, in its own CI job once the nights begin; the architecture export of the town; eye-level frames of every
part before it is committed.
