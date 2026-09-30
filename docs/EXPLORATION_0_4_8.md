# Exploration and companions — 0.4.8

The descent begins with ordinary, warm-lit hallways. Straight, bent and crossing layouts have several separate doors on a single stretch. Connected gray mazes remain available farther in, but unfamiliar topology is an encounter, rather than the default room type.

## Dealing and pacing

The dealer makes one anomaly roll per arrival and assigns at most one onward door to an impossible place. The entry/return door is separate. Route depth below 3 has no anomaly roll; depths 3–5 use 3%, 6–8 use 5%, 9–17 use 8%, and 18+ use 10%. The actual chance of walking into one is lower because the player chooses a door. A recently visited anomaly suppresses this roll until three subsequent visits have passed. Larger 7×7, 9×9 and 11×11 folded mazes still unlock at depths 3, 6 and 9.

Ordinary hallways do not routinely deal themselves. The existing eight-room personal history, vignette dry-spell guarantee, multi-visit story progress, private door leaks and Hillary's scent remain in use. Anomalies and special rooms use the saved repeat penalty.

After an anomaly or hazardous stretch, a 75% roll can offer a quieter destination. Deep exploration also offers a pause, with forced rest offers paused for four visits after a quiet room. Weighted quiet destinations can still appear during that interval at their reduced repeat weight. A quiet room has chairs, a water cauldron, finite food, a readable explorer note and two doors. The explorer camp retains its bedrolls, fire, food and finite recovery. The Growl does not play during these quiet stops. Players can sit with an empty-hand right-click on a stair chair or carpet; Shift dismounts. Quiet places keep their warm light.

## Companion controls

After greeting Hillary, empty-hand right-click opens a non-pausing command wheel. It also works on your tamed cats and wolves. Mouse selection, WASD or arrow keys choose a command; Esc closes it.

| Command | Behavior |
| --- | --- |
| Follow | Accompanies the owner, including real manor and labyrinth transfers when nearby and standing. |
| Stay | Sits and stays behind; never transfers with the player. |
| Track deeper | Leads along a physical route toward an onward door, preferring a scented vignette. |
| Find the way out | Traces physical corridors toward the entry/return door, then toward the manor exit. |

A compass held out to a companion remains a shortcut for Find the way out. Holding a vignette yield out to Hillary still supplies a scent. The player walks and opens each door; guidance does not finish an expedition automatically.

At a new deep place, the companion stops, looks back and whines or hisses. The pause grows from one to two seconds, then ends. Quiet stops and domestic space do not impose it. Acknowledged, untamed Hillary can be commanded too. Commands persist with the entity. Native ownership, health and identity survive transfers. Server packets check ownership, six-block interaction reach and a valid command.

Nearby standing pets follow across a threshold within twelve blocks. Leashed or mounted animals remain behind. The wheel can command ordinary owned cats and wolves as well as rescued strays.

## Physical navigation

Chalk is a damageable item with 64 marks. Right-click a surface to draw; floor marks are directional arrows and wall/ceiling marks are crosses. Shift-right-click erases your own mark. Right-clicking a shut labyrinth door while holding chalk draws at your feet.

A trail spool has 192 blocks of string. Right-click to tie or untie it; while active, walking on a supported floor lays connected string. It remains active when you put the spool away to open doors. Gaps, jumps and threshold transfers begin a new segment. It stops when exhausted or when you leave the labyrinth. Neither marks nor string obstruct movement.

Recipes: bone meal plus a clay ball makes chalk. A shaped recipe with string around a central stick makes a spool. Tom's junction barrel and the camp receive one finite supply upgrade in existing worlds, in empty slots; ordinary revisits do not refill them. The camp also supplies bones and raw cod for rescues.

Bent/crossing halls have old explorer arrows and chipped masonry landmarks. At depth 6+, the House can very occasionally turn an unattended floor arrow or sever a string segment. Checks occur every twenty seconds at 4%, with a shared two-minute cooldown. Only nearby-room marks at least twelve blocks from their owner can change, and any player's view protects them. Quiet rooms are excluded. These changes affect actual world blocks and survive saving.

## Rare encounters

At depth 5+, Mother may briefly leave her den for an eligible, unseen corner: an arrival roll of 1 in 100 in a quiet stop, or 1 in 300 elsewhere, followed by a twenty-minute shared cooldown. She uses the existing den entity. Active claims, the Pekingese scene, salving, banishment or an occupied den prevent the visit. She returns after forty-five seconds once unwitnessed; entering the den also requests her return. Authored scenes are excluded.

At depth 4+, an untamed cat or wolf may be found lost in an eligible room: 1 in 120 arrivals in quiet stops or 1 in 240 elsewhere, with an eight-minute shared cooldown. Each physical room can produce one rescue animal, and at most three loaded untamed strays wait in the House. They appear only out of view. Native bone/fish taming works; they remain vulnerable pets, with the same command wheel and threshold support as Hillary. They are not Mother's kept-pet props.

## Playtest commands and upgrade

Use `/oldesthouse door straight_hall`, `bent_hall`, `cross_hall` or `quiet_room` to place a direct test door after the manor exists. `/oldesthouse labyrinth status` reports exploration state. Operator supplies: `/give @s the_oldest_house:chalk` and `/give @s the_oldest_house:trail_spool`.

Build version 13 extends existing version-12 stacks with four new places. Existing active vignettes, cache contents, dropped items and authored Mother/clap assets are retained. Network protocol is 10; client and server must use the same mod version.

Validation covers physical paths to all hallway doors, sampled anomaly frequency, saved encounter spacing, command authorization and persistence, physical marker collision/visibility, corner routes, and native rescued-cat transfer. The dedicated-server suite also retains the existing opening, Mother, clap-and-seek and structural checks. Client wheel presentation, real-time pet walking and the subjective rarity/pacing still require an in-game playtest.
