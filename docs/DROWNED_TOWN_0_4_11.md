# Indian Lake: Drowned Town — 0.4.11

A multi-visit vignette whose verb is **breath**. The first playable Indian Lake site follows the updated design notes: Stacey Graves is a custom Lake Witch with jointed shoulders, elbows, wrists, hips and knees, wet hair, a torn skirt and an authored UV atlas.

## Playing the sequence

1. Find a leaking lake door in the labyrinth. It leads to a night shore, with a submerged street grid, school, church, houses and a visible steeple. A shore lectern and submerged signs give physical orientation clues.
2. Dive into the school on the left. Three desk barrels each contain one waterlogged essay. The school door and abandoned house doors hold native air pockets; the old well has a real upward bubble column.
3. Use the furnace on the shore to dry all three distinct essays. It uses normal Minecraft smelting, fuel, output slots and readable written-book components. Finite fuel, two portable doors and a little food are beside it. The essays describe water, grass, breath, returning rooms and slasher rules in original student writing.
4. Stacey approaches over bare dirt. Her shore pathfinder and physical movement both reject water and **grass blocks**. She routes around refuges. A visible 0.9-second raised-arm warning precedes a three-heart strike. Returning to water or grass cancels an imminent hit. Normal weapons can put her down for the current visit; a later visit restores the hunt.
5. Leave through the entry door. On the next arrival, the school’s last desk holds the church key. An incomplete first visit resumes; simply visiting or carrying someone else’s key cannot advance it. New arrivals do not restage an occupied room.
6. Use the key at the submerged church’s iron door. Keep the artifact. The preserved congregation sits inside; the preacher remains below and keeps singing. Swim to the roof hatch. Opening it lets the hymn reach the shore and finishes the vignette.

Minecraft’s own air supply, water breathing, Respiration and turtle shells apply. Portable doors and soul sand may be placed and recovered in the lake away from authored puzzle props and the church. The rest of the labyrinth’s block protection remains in force. The water is actual source water, and the night is enclosed scenery; entering does not move the shared world clock.

## What persists

- Visit, three-bit drying progress, finite desks, church key placement, unlocked gate, opened roof, placed air tools, defeated hunter for the current visit and completion survive saving. Arrivals preserve furnace contents, dropped items and placed doors.
- A genuine hunt is recorded per explorer, providing the future shallows’ prerequisite. The future shallows completion API records its participants; reopening a dried essay afterward adds a page with the boys’ names, including that explorer’s username.
- Opening the roof saves the escaped hymn. The future cave’s next-arrival API empties its congregation and marks the dead as standing on shore. The shared congregation model supports both poses, and the town can stage their shore positions when that consequence has occurred.
- The actual roof opening records a personal **understanding** resolution for the Witness ending. A later explorer can crouch and inspect the open hatch to record the aftermath. A traded essay or key supplies no Witness credit.
- A church key carried in inventory or either hand prevents Minecraft drowned from targeting the bearer outside the House and clears an existing pursuit. The benefit ends when the key is put away. Stacey’s hunt retains its own rules.

This release implements Drowned Town. The cave, shallows, Camp Blood and remaining Indian Lake sites retain their updated design and saved connection points for later passes. They are not separately dealt rooms yet.

## Test controls

| Command | Use |
| --- | --- |
| `/oldesthouse door drowned_town` | Place a test door to the lake; the House and carved labyrinth must exist. |
| `/oldesthouse vignette drowned_town status` | Show visits, distinct dried essays, gate, roof and completion. |
| `/oldesthouse vignette drowned_town reset` | Reset this room and its finite supplies to the first sequence. |

Source assets are reproducible with `python3 tools/generate_indian_lake_assets.py`. All three lake sound files are original mono Vorbis; the hymn has no lyrics. Native server tests cover all three normal furnace recipes, actual return-door progression, the later key, the roof’s protected interaction, personal credit, native drowned targeting, state reload and actual Lake Witch movement around refuges.

The builder upgrades version 13 by adding only slot 27. Existing vignette inventories and completed endings remain at their prior coordinates. Client and server should both use 0.4.11 (network protocol 13).
