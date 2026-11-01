# Texture pass, release 1: the ordinary halls (0.4.219)

The owner, 31 October: "a lot of the map could use improved, custom textures." The plan (docs/TEXTURE_PASS_PLAN.md) measured
every exported scene and found the ordinary halls, the surfaces every reader walks between stories, entirely vanilla. On 1
November the owner said to go ahead. This is its first release.

## What changes

Every domestic hall (zigzag, vaulted, service landing, pillar, offset, jogged, forked, alcove and crumbled) and the first halls'
corridors (straight, bent, cross and the quiet room) now wear their own surfaces:

| Was | Becomes |
|---|---|
| White terracotta walls | Plaster, laid by hand. Now and then a hairline crack; under the ceiling, now and then a water stain come down from above |
| White terracotta at the third course (or one under a low roof) | A picture rail of stained wood, lit along its top |
| Stripped spruce or oak, or spruce planks, at the foot of a wall | A skirting board, scuffed where boots and a broom have met it |
| Spruce plank floors | Floorboards running the length of the passage. Down the middle of a passage three, five or seven wide, they are worn pale |
| White concrete (or the vaulted hall's low terracotta neck roofs) | A plaster ceiling |
| Paired grey stained glass | A two-over-two sash window, painted, with nothing but dark behind the glass |

The first halls keep their own floors (boards or stone, each first hall has its own); only their walls, skirting and ceilings
change.

Twelve blocks, ten textures, all drawn in code by `tools/draw_hall_art.py` (CI runs it with `--check`). The east-west floorboards
are the same texture turned a quarter in their block state, so the boards always run along the hall.

## How

`HallFinish` is the last thing done to an ordinary hall as it is built (`HallVariations.buildDomestic` after its doors;
`LabyrinthHalls.build` for the first halls). It swaps a surface only from the vanilla block it was laid in, one for one. Each new
block copies the collision and sound of the block it replaces, and is as unbreakable as the halls already were. Nothing moves:
no shape, door, block entity, lamp, story volume or gameplay coordinate changes.

Every read and write goes through `BuildBlocks`, and every choice (which plaster, which way the boards run, where they are worn)
comes from coordinates relative to the hall. A paced build is therefore the same as a direct one, block for block. Floors and
ceilings are dressed before walls, so a block that is both (the vaulted hall's neck roofs, seen from the nave) is always a ceiling.

The passes that recognise a hall's walls as plain fabric (the bled-in homes, the household glimpses, the domestic hall repairs)
treat the new surfaces as fabric too (`HallFinish.fabric`).

New builds only. A world that already carved its halls keeps them as they are.

## Not in this release

- The stone halls, and the quiet and threshold halls: release 2.
- The great staircase: release 3.
- The halls' working doors stay vanilla (the plan's "left as vanilla").

## Tests

`ExplorationPassTests` (the hall shapes case) now also checks, for each domestic hall: no spruce planks left under its open cells,
no white terracotta left on its walls up to the fourth course, and at least 40 plaster faces, 20 floorboards, one worn board and a
ceiling over more than half its cells. The crumbled hall is exempt from the floorboard checks (its floor has fallen in) and the
vaulted hall from the ceiling check (its nave is fifteen blocks high).
The interior review cases (bled-in homes, household glimpses) pass unchanged. Architecture views of every hall are exported as
before, and the client renders them.
