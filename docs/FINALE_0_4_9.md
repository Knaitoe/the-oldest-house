# Finale — 0.4.9

The journey can now reach either ending. Opening the scratched cell commits the explorer; finding the great staircase does not.

## Finding and preparing

The great staircase becomes eligible at twelve return crossings after the explorer has personally visited two vignettes. A deeper arrival has an 18% chance of offering it through one side door. After discovery it remains eligible on later expeditions. No completion checklist forces it, and earlier halls retain their ordinary pacing.

This is a physical descent of 127 blocks through a tall dark shaft, separate from the earlier spiral loop. The player can return through its entry until opening the cell. Chalk, string, portable light and first-person exploration work here. The companion wheel can lead along the stair treads in either direction.

Holloway's barrel contains a finite shield and two survey pages. The old man examines the weapon in hand and identifies the original most-used weapon, including whether the Mother has it. The lectern holds an original short play with red deletions. These pages are new authored text, not quotations from the novel.

Weapons acquire a persistent physical identity as they are used. Existing saves seed history from available item-use statistics; ties are deterministic. An item explicitly marked as a House copy cannot acquire an original identity. The Mother's actual archived weapon retains its identity. An explorer with no weapon history receives a stone sword at commitment.

## The encounter

The Minotaur appears only after the cell opens. Its nameless boss bar stays full. A warned, straight charge can be stopped by a raised shield facing the creature. The block wears the shield and opens roughly three seconds to strike. Other attacks do not reduce health. One hit from the recorded original weapon starts the wounded crawl; ranged weapons carry provenance on their projectiles.

The model has articulated shoulders, arms, forearms, hands, jaw, curling horns, legs and split hooves, with distinct windup, charge, stun and crawl poses. Its voice uses the existing Growl sound assets.

## Defeat

Death after commitment, including suicide, returns the player outside the real manor's front door. Its doors never admit that player again. All inventory and cursor contents, including ordinary resources, go permanently to sealed entries in the Mother's collection. Nearby accompanying pets are also kept. Other vignette completion records are unchanged. Knocking receives an answer.

The exclusion is a world record keyed by player UUID. Player cloning, reconnecting and restarting do not remove it. Ordinary clap-and-seek failure continues to drop recoverable items and respawn inside the manor.

## Wound and escape

The creature crawls toward its cell as the collapse starts. Tom's radio transmission cuts out; he does not accompany the escape. The player reaches branching paths over genuine dropoffs at the dark bottom. Falling off is fatal and has the same permanent consequences as other committed deaths.

Put a collected page or paper in the offhand and use flint and steel in the main hand to burn it. The note is consumed and lights nearby blocks for six seconds. Chalk, string and portable lights remain usable.

An owned cat or dog previously kept by the Mother can return as a guide if the explorer left her shelves alone or offered something she wanted. The actual collection record supplies the pet's name, collar and identity data; its carried equipment is not duplicated. It waits when the explorer falls behind and returns to ownership at the exit. A dead guide does not respawn repeatedly.

Without that rescue, the return advances the shared Overworld clock sixty days. Crops around the recorded bed mature; grass and vines appear only in vacant supported cells. This clock change affects the shared world. The player's built blocks are preserved.

The House's actual manor site becomes an empty lot. Demolition is saved and processed in bounded batches. All residents are evacuated before the House becomes inactive. There is no new portal in the player's home wall.

After one further game day, a mundane chest appears near the recorded Overworld bed, containing pages in another hand and a page referencing an unvisited vignette. Later chest loot has a 7% chance of a further note; wandering traders may stock one.

## Persistence and testing

Encounter owner, selected weapon, phase, guide and route position, temporary note light, ending, demolition cursor and epilogue delivery are saved. Reloading a charge restores its warning instead of an unseen lethal dash. Actor recovery waits for entity tracking before replacing a missing creature.

Use a disposable world: both endings are permanent in normal play. Operator fixtures:

- `/oldesthouse finale prepare` builds the authored region in bounded batches after the House exists.
- `/oldesthouse finale go` enters the top once prepared.
- `/oldesthouse finale cell` moves to the preparation chamber.
- `/oldesthouse finale start` opens the cell and commits the explorer.
- `/oldesthouse finale status` prints the player's saved finale record.
- `/oldesthouse reset` clears finale test progression along with the House reset.

Automated checks cover original/copy archive identity, sealed ordinary resources, kindness versus shelf taking, optional personal discovery, saved terminal outcomes, inactive collapsed origins, physical stair and escape routes, actual creature wound behavior, and safe charge reload. Existing vignette and companion regression checks continue to run.

## Assets

`src/main/resources/assets/the_oldest_house/textures/entity/finale_materials.png` is the built-in image-generation output, used unchanged as a two-column, four-row diffuse material atlas. Native UV layout is 256 × 512; the source PNG is 887 × 1774. Material cells are hide/bone, fur/black, elderly skin/wool, white hair/boot leather. Existing Mother, Pekingese and clap-and-seek textures are unchanged.

Generation prompt: "Create a portrait 1:2 texture material atlas for a Minecraft horror mod's custom articulated Minotaur and elderly witness models. Eight rectangular swatches arranged exactly in 2 equal columns and 4 equal rows, every swatch filling its cell edge to edge, no gutters, no visible dividing lines, no text, no frame. Flat unlit diffuse albedo material only, fine pixel-scale painterly organic detail, muted almost monochrome oppressive atmosphere. Top row LEFT weathered desaturated gray-taupe leathery bull hide with subtle scars; RIGHT old yellow-gray bone horn with faint longitudinal grain. Second row LEFT near-black coarse matted animal fur, barely visible brown and gray variation; RIGHT almost solid black mouth and eye material. Third row LEFT pale ashen elderly human skin with extremely subtle mottling and fine wrinkle-like grain, no facial features; RIGHT worn dark charcoal wool coat cloth. Bottom row LEFT fine sparse white-gray hair texture; RIGHT almost black worn boot leather. Each cell is a seamless repeating material swatch. No faces, no body parts, no animals, no objects, no highlights or directional shadows. This is a UV texture atlas, not a character portrait."
