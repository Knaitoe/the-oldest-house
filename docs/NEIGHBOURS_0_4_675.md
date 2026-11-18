# Neighbours (0.4.675)

This candidate lets the House share a world with other mods without depending on any of them. The first neighbour is *A Reaping*
(mod id `a_reaping`), a Hunger Games mod whose players live in districts in the Overworld and fight in an arena dimension. The design
both mods follow is A Reaping's `docs/superpowers/specs/2026-11-17-house-compatibility-design.md`, and its evidence, with a file and line
for every House site, is `2026-11-17-house-compatibility-research.md` beside it. The standing account of the House's half, site by site,
is `docs/NEIGHBOURS.md`; this record says what the candidate changed and what it does not cover. It was made in four parts (the commits
"Neighbours 1" to "Neighbours 4e").

The owner, on the district: "The house should require the player to sneak out from the district. Having Hillary and the like spawn
inside would likely still require the player to sneak out, while adding another layer of convolution."

No layout, protocol or Witness change. No save is migrated: a world from before keeps its respawn as home and remembers a home bed from
the first morning woken in one.

## For a reader

**With A Reaping installed**, as its parts come:

- **The House comes only to a home.** A home is a bed of the reader's own: in the Overworld, a bed, not one of the beds a neighbour
  issues (`#the_oldest_house:not_home`), and on no district's land. A reader who only ever sleeps in their district never starts the
  House. To begin it they sneak out and sleep in a bed they set down outside the fence. The letter, the journal and Hillary come to that
  bed and never land on district land, and the manor is never sited within eight blocks of a district.
- **The House's nights.** Sleep in a district still ends the House's night and ages it, as any sleep does. Only a night in a home bed or
  one of the manor's moves the opening on, feeds the key tag or mends the front-door key.
- **The House edits nothing on a district.** The elk takes no animal and opens no gate there; Your Own Light touches no chest, candle or
  campfire there; the endings' chest and the overgrowth keep off it; the hotel tab is never collected from a container on it.
- **In the Games the House keeps out.** A world a neighbour lists in `#the_oldest_house:not_own_world` is not the reader's own world:
  the elk, the tab, the Goatman, the storeroom's return, the lamp's respawn, weapon history, the prizes, lateness, Bly's Lady, the film
  and every keepsake that works "out in your world" leave the reader alone there. Harrigan's phone has no service.
- **The House's rooms are sheltered.** A Reaping pauses its fighting and its wounds in the House's dimensions, never targets the
  House's people, and never fights Hillary while she guides.
- **A neighbour's people are left alone.** The mirror never projects them, the copied homes never take them, Harrigan's call never
  kills them and the tally counter never counts them.
- **A neighbour's weapon can be the favourite.** One in NeoForge's common weapon tags is counted by the blows it deals, passes the hall
  drawers as the reader's favourite, and can be carried to the Minotaur.
- **A neighbour's game is game.** A kind listed in `#the_oldest_house:hunted` is counted by the elk's men as "head of game", owed in kind
  and spoils as it falls.

**Without a neighbour** every tag is empty and no district holds, and the House is as it was, apart from the House's own bugs the design
lists and what fixing them changed:

- **Home.** The House remembers the home bed a reader last woke in on a morning, so a reader whose respawn moves to a manor bed keeps
  their Overworld home (before, the opening's morning steps stalled). The door count listens last and skips a click something else
  refused, and counts only doors near home. The key tag and the front-door key act only on a wake in a home or manor bed, not on a night
  wake or a wake out of bed. The elk with no home takes nothing (before, it took from round the world's spawn). The cabin counts the days
  since the bed became home. A copied home is the bed slept in and the table and door nearest it, and its front door is the door used most
  near home. The endings read the home when they reach it.
- **The manor's site.** The search saw only from the highest ground up and only from three blocks in front, while the cellar, the
  foundations, the steps and the demolition reach further. It now checks the whole lot and what the foundations would run down to under
  it, and the finale's demolition clears exactly the box it checks. With no free cell, the endings' pages are left at home instead of
  waiting for a cell forever. Your Own Light turns round a chest the reader opened themselves before any other.
- **Worlds.** Where the elk's field found a reader is kept with its world and let go when they change worlds. A reader taken out of the
  Not Yet ward while the machine mends them is finished where they are.
- **The hall drawers.** The House's own stamps (a weapon's identity, the mark of a House copy, the Mother's marks) no longer make a thing
  House-made, so an outside pickaxe the Mother has marked goes to the drawer like anything else from outside. The reader's current
  favourite weapon passes the door and the hall as their own. The favourite is only what the House has counted: the once-only seeding
  from the game's item statistics is gone, and letting a weapon fall is no use. The Minotaur also knows the original by what dealt the
  blow.
- **What is copied.** The mirror's projections and the copied homes carry none of the NeoForge data or attachments of what they copy.

## For a second mod

Nothing in the House names another mod's classes, and a neighbour must never name the House's. They speak only through tags, inter-mod
messages (`InterModComms`, sent in `InterModEnqueueEvent`, read in `InterModProcessEvent`) whose payloads are `java.util.function`
types, and scoreboard tags. The mod that reads a tag declares it as an empty file; the other fills it from its own jar. The House's
half is `house/Neighbours`.

| What | Kind | Who fills or sends | Meaning |
|---|---|---|---|
| `#the_oldest_house:not_home` | block tag, declared empty here | a neighbour | a bed that counts for sleep and never for home (an issued bed) |
| `#the_oldest_house:not_own_world` | dimension type tag, declared empty here | a neighbour | not the reader's own world (an arena): the House keeps out of it |
| `#the_oldest_house:left_alone` | entity type tag, declared empty here | a neighbour | a neighbour's own people: never projected, copied, called or counted |
| `#the_oldest_house:hunted` | entity type tag, declared empty here | a neighbour | a neighbour's game animals: game to the elk |
| `district_land` | message to `the_oldest_house`, `BiPredicate<ServerLevel, BlockPos>` | any number of neighbours, taken together (OR) | true on a district's land and worksites and at least 8 blocks round them |
| `#a_reaping:sheltered` | dimension type tag, filled here | the House | the House's dimension types: `house_interior`, `outside`, `between` |
| `#a_reaping:not_fighters` | entity type tag, filled here | the House | every living entity type the House registers |
| `house_land` | message to `a_reaping`, `BiPredicate<ServerLevel, BlockPos>` | the House | the manor's lot in the Overworld and 8 blocks round it, at any height, from the morning the House comes, standing or fallen |
| `house_made` | message to `a_reaping`, `Predicate<ItemStack>` | the House | the House's own things (`HouseKeeping.houseMade`) |
| `a_reaping.not_a_fighter` | scoreboard tag | the House, on Hillary | she is never fought while she guides (she is a vanilla wolf, and no type tag can cover her) |

The House's projections already carry scoreboard tags a neighbour can skip: `the_oldest_house.entity_projection` (the mirror) and
`LiteraryCopyProjection` (the copied homes).

What a `district_land` predicate is promised and must keep:
- It is asked on the server thread, with a position of its own that it may keep.
- It must hold at least 8 blocks round a district's land and worksites: the House probes a box only every 2 blocks, so a predicate that
  held only on a narrower strip (a road, one worksite block) could pass between two probes.
- One that throws is taken as not holding, and said once in the log.
- It may change its answer from one day to the next. A bed that becomes district land stops being home, and is home again if the land
  is given back.

Sending it, from the neighbour's mod bus:

```java
void enqueue(InterModEnqueueEvent event) {
    InterModComms.sendTo("the_oldest_house", "district_land",
            () -> (BiPredicate<ServerLevel, BlockPos>) Districts::onDistrictLand);
}
```

and reading the House's two, from `InterModProcessEvent`:

```java
event.getIMCStream("house_land"::equals).forEach(m -> houseLand = (BiPredicate<ServerLevel, BlockPos>) m.messageSupplier().get());
event.getIMCStream("house_made"::equals).forEach(m -> houseMade = (Predicate<ItemStack>) m.messageSupplier().get());
```

A message to a mod that is not loaded is dropped, so each mod works alone. The House sends `house_land` and `house_made` only to
`a_reaping`; another neighbour that needs them asks for its mod id to be added.

`LeftAloneTests` makes one of every entity type in the House's namespace and fails, by name, for a living one `not_fighters` leaves out,
so a new person or creature cannot be forgotten. A case installs a district without a neighbour through
`Neighbours.installDistrictForTesting` and takes it away in a `finally` or an `@AfterBatch` hook.

## New writing

Flagged for the owner, to be rewritten on the Text Review page. None names a colour or an age.

- The Sphinx's survey beside a bed that is not the reader's own (the design left this line to the owner): "You unfold the survey beside
  the bed and fold it again. It is not your bed."
- Harrigan's phone opened where there is no service and it is not the House: "No service. The clock still keeps the time back home."
- The Goatman's counter clicked where it keeps no count: "It clicks, but the number doesn't turn here."
- The kept bike in a world that is neither the House nor the reader's own: "Not here."
- The elk's men on a neighbour's game: "head of game" ("One head of game, %s. We counted.", "Two head of game, %s. We counted.").
- The lines that promised an effect "away from the House" or "outside" now say "out in your world", the House's own phrase: the kept
  bike, the diary, her compass, her name patch, the envelope, the counter, Harrigan's phone, his phone kept, and the two funeral lines.
  `docs/NEIGHBOURS.md` (R3) quotes each.

## What is not covered

- **Nobody has played it beside A Reaping in a client.** Every proof is a game test case on this mod alone: a district is installed by
  hand, and a dimension or entity type is put in a tag for a moment by reflection (`NeighbourTags`, the Nether standing in for the arena).
  No case loads A Reaping, and no case sends or receives a real inter-mod message; the messages are sent at every server start and
  dropped, since `a_reaping` is not loaded.
- **A Reaping's districts are not built yet,** so its `district_land` is always false today. With both mods installed, no bed is district
  land and no site is turned down for a district until they are; what works now is the tags (`not_own_world` for the arena,
  `left_alone` for its sparring partner) and what the House fills and sends for A Reaping.
- **A Reaping's half** (the design's R3 pets and drops, R4 to R6, R8, R10, R11, and its parts of R2, R5, R7 and R9) is A Reaping's, built
  there with what it belongs to.
- **Left to the owner:**
  - the Sphinx's refusal, above;
  - the copied home's heirloom carries the House copy's mark, which is one of the stamps, so it is not `house_made` and A Reaping may
    treat it as an ordinary thing;
  - the Uncanny bone ring, a woken smoky mirror and the Usher stone work in every world, so they are not own-world tests and would tell
    their holder where hidden bodies are in the Games; A Reaping's strip keeps them out once it exists;
  - in the House, Harrigan's phone still rings on through a night ring that began outside, as it always has;
  - the design's envelope is 13 rows under the origin, while the supports under the cellar and the lowest step reach 18, so a district
    under those is kept three blocks off, not eight; the jetty's ladder can reach a row under the lot and is not demolished.
- **Small edges kept:** a district predicate narrower than the probe spacing could slip between probes (the contract asks for the margin);
  past the hall, a favourite picked up by its owner with a full inventory stays on the ground untracked, as anything set down in the
  manor always has.
- **Not verified in CI.** Only selected cases were run locally. Until a workflow run passes every job on this commit, the candidate is
  pending.

## Made as

- `house/Neighbours` (new): the tags, the messages, the margin and probe, `notAFighter`, the cases' district hook.
- `opening/HomeBed` (new) and `OpeningPlayerState.Home`; every home read through them (`docs/NEIGHBOURS.md`, R1, lists each site).
- `house/HouseSpawnManager`, `HouseLayout`, `HouseShell`: the site's district margin, the lot and the run under it; `house/Containers`
  (new) and `mixin/CompoundContainerAccess` (new) for the tab.
- `house/HouseDimensions.ownWorld` and the sites that read it (R3).
- `house/HouseKeeping` (`houseMade`, `favourite`, `passes`, `same`), `labyrinth/WeaponHistory`, `labyrinth/MinotaurEntity` (R7).
- `house/HouseExteriorEntityMirror`, `labyrinth/HarriganVignette`, `labyrinth/TallyCounterItem`, `labyrinth/LiteraryCopies` (R9).
- `opening/Hillary.guiding`, `labyrinth/ElkFollows` (`#hunted`, `GAME`).
- Tag files: `data/the_oldest_house/tags/{block/not_home, dimension_type/not_own_world, entity_type/left_alone, entity_type/hunted}.json`
  (empty), `data/a_reaping/tags/dimension_type/sheltered.json` and `data/a_reaping/tags/entity_type/not_fighters.json`.
- Docs: `docs/NEIGHBOURS.md` (new), `docs/OPENING.md` (home), `docs/HALL_DRAWERS_0_4_255.md` (the stamps and the favourite).

## Cases

Five new case classes, all in the default namespace (`the_oldest_house`), which `build.gradle`'s default list runs and the workflow's
build job runs in the main half of the structure game tests (`tools/structure_groups.py`); their counts are derived like every other:

- `HomeBedTests` (7): home and not home, the opening waiting at NONE, the remembered home, sleep in a district ageing the House, the
  letter off district land, the Sphinx and the copied homes refusing a district bed.
- `NeighbourLandTests` (10): the site's margin, the lot and the ground under it, positions handed to a neighbour, `house_land`, the elk,
  Your Own Light, the hotel tab, the endings' chest, pages and overgrowth.
- `NotOwnWorldTests` (6): everything that keeps out of a world in `#not_own_world`, each with the tag taken off for a moment to show the
  House as it is in the reader's own world; Not Yet's mend cut short two ways.
- `LeftAloneTests` (7): the declared and filled tags (`not_fighters` complete), the mirror, the copied homes, Harrigan's call, the tally
  counter, Hillary's scoreboard tag, a neighbour's game.
- `HouseMadeTests` (8): the stamps, the favourite at the door and the hall, set down and picked up, a weapon let fall, another mod's
  weapon, the Minotaur.

Existing cases whose readers now sleep in a bed of their own: `ArtifactTests`, `LiteraryTests`, `OwnLightTests`,
`HarriganDialogueTests`; `HarriganDialogueTests` reads "Out in your world".

Local, selected, `-Xmx1g`: the 38 new cases together, 38/38 in 30 seconds of test time. Each part also ran the existing cases over the
code it changed; those are listed in its commit. CI pending.
