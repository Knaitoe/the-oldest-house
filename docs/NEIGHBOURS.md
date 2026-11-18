# Neighbours: the House beside other mods

The House shares its world with other mods without depending on any of them. The first is *A Reaping* (mod id `a_reaping`), a Hunger Games mod whose players live in districts in the Overworld and fight in an arena dimension. The design both mods follow is A Reaping's `docs/superpowers/specs/2026-11-17-house-compatibility-design.md`, and its evidence, with a file and line for every House site, is `2026-11-17-house-compatibility-research.md` beside it.

The owner, on the district: "The house should require the player to sneak out from the district. Having Hillary and the like spawn inside would likely still require the player to sneak out, while adding another layer of convolution."

## How they speak

Nothing in the House names another mod's classes, and nothing in a neighbour names the House's. They speak through:

- **Tags.** The mod that reads a tag declares it as an empty file; the other fills it from its own jar.
- **Inter-mod messages** (`InterModComms`), sent in `InterModEnqueueEvent` and read in `InterModProcessEvent`, whose payloads are `java.util.function` types only. A message to a mod that is not loaded is dropped.
- **Scoreboard tags.**

`house/Neighbours` holds the House's half. Without a neighbour every tag is empty and no message comes, and the House is as it was.

| What | Kind | Direction | Meaning |
|---|---|---|---|
| `#the_oldest_house:not_home` | block tag, declared empty here | a neighbour fills it | a bed that counts for sleep and never for home (A Reaping's issued beds) |
| `district_land` | message, `BiPredicate<ServerLevel, BlockPos>` | a neighbour sends it; any number, combined with OR | true on a district's land, worksites and margin |

A predicate that throws is taken as not holding and said once in the log. The cases install a district without a neighbour through `Neighbours.installDistrictForTesting`, and take it away in a `finally` or an `@AfterBatch` hook.

## R1. Home is a bed of the reader's own, outside every district

`opening/HomeBed`.

**A home bed** meets all four:
1. It is in the Overworld.
2. It is a bed (`#minecraft:beds`), when its chunk is loaded.
3. It is not in `#the_oldest_house:not_home`.
4. No neighbour's `district_land` holds at it.

**The reader's home** is the first of:
1. their respawn, if it is a home bed;
2. the home bed they last woke in on a morning, which the opening remembers in its player state (`OpeningPlayerState.Home`: the bed, the House's day it became home, the nights slept in it);
3. none.

An unloaded bed is trusted, as the respawn always was: it can only have been set or remembered by using one. A remembered bed the House sees gone (its chunk loaded and no bed there) is forgotten, so it is not trusted again when its chunk unloads; one on a district's land or in `#not_home` is only passed over, since those can change back.

**What counts where.**
- The opening waits for a home: a reader who only ever sleeps in a district never starts it.
- The opening's nights count only a night in a home bed or in one of the manor's. A morning in a home bed is what the House remembers.
- Sleep still counts everywhere for the House's own clock and for what ends with any night's sleep: its age, the co-author's note, lateness, the Line.
- The key tag's feeding and the front-door key's mending come only with a wake in a home bed or a manor bed.
- A door's use is counted only near home, and only when nothing else refused the click (the count listens last).
- Nothing the opening sets down, the letter, the journal and Hillary, lands on district land, and no door there is taken for their front door. When the door they use most stands off the district but its step is on it, the nearest other door is tried before the floor beside the bed.

**Every home read takes the home**, and its own no-home branch when there is none:

| Site | With a home | With none |
|---|---|---|
| The opening (`OpeningSequence`, `OpeningCommands`) | every morning step, the letter, the journal, Hillary, the manor's site, Navidson's photograph | waits at its stage |
| The Red Room (`RedRoom`) | the room they spend their time in near home, off any district's land | nothing is copied |
| The cabin (`CabinBargain`, `CabinScreen`) | "the bed you made"; the days since it became home and the nights slept in it (for a respawn the House never saw become home, the world's days and every night anywhere, together); the television shows it | "where you first woke"; the world's days and every night anywhere; the world's spawn |
| The copied homes (`LiteraryCopies`) | captured on a night in a home bed only, and its nights are home nights; the copy's bedroom, kitchen and door are the bed slept in and the table and door nearest it; their front door is the door they use most near home | nothing is copied |
| The Sphinx (`SphinxDream`) | the survey is set down only beside a home bed; the stone's sill is by the door they use most near that bed, or the nearest, never a door or a sill on district land, else beside the bed; with that bed gone, the second waking puts them at home | beside any other bed, refused (a new line, below); with the bed gone, the second waking puts them where it stood |
| The Lake House (`LakeHouseStory`) | the first morning wake in a home bed; the footprints lead to that bed | waits |
| The elk in kind (`ElkFollows`) | a penned animal within 32 of home | nothing is taken, and the debt is kept |
| Your Own Light (`OwnLight`) | the visit within 12 of home | stays pending |
| The storeroom (`Storeroom`) | the thing on the tag left on the home bed | in the pocket, two days on |
| Harrigan's hostile call | the 64 blocks round home | refused, as with no bed |
| The endings (`FinaleController`, `WitnessEnding`) | the epilogue's chest at home, read when it is delivered; the overgrowth round home | beside the reader, once they are in the Overworld; nothing grows |
| The photograph and the family's nights (`Artifacts`) | near home; asleep in it | nothing |
| The diary (`Keepsakes`) | which way and how far to home | "I don't know the way from here." |
| The guest bed and the lit window (`HouseShiftEffects`) | its colour; the window facing it | as with no bed |
| State Route's waking (`StateRoute`) | "You wake in your own bed" only in a home bed | "You wake on cold ground" |

Unchanged, because they read the manor and never a home: the HouseRead HOME trait, `HouseExperience`, `HouseLabyrinth`'s spawn rule, Karen's bed, the knocking and the belongings at home.

**New writing.** One line, for the Sphinx's survey beside a bed that is not the reader's own: "You unfold the survey beside the bed and fold it again. It is not your bed." The design left this line to the owner; it is here to be rewritten on the Text Review page.

**No migrations.** A world from before keeps its respawn as home, and remembers a home bed from the first morning woken in one.

## Cases

`HomeBedTests`: a bed under an installed district is not home and the opening waits at NONE; a bed in `#not_home` is not home; a home bed outside every district works as it always has, with the door count skipping a refused click; the home is the bed last woken in on a morning, with its day and nights; sleep in a district still ages the House; the letter never lands on district land; beside a bed on district land the Sphinx refuses the survey with the new line, and a night there is neither copied nor counted for the copied homes. The artifact, elk, copied-home, Own Light and Harrigan cases put their readers in a bed of their own.
