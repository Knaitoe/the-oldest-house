# The child's room, rebuilt (0.4.74)

The owner asked for the child's room vignette to be looked at again: textures remade in vanilla Minecraft style, every piece of furniture unique and right for a child's bedroom, toys a reader can actually play with, a reworked basement, and a sequence that triggers as intended. Built on 0.4.73 (`38f71cd`).

## What was wrong

- **The basement was a void.** The old builder cleared a strip under the bed and left the layer beneath the floor open and unbounded; there was no room down there, only a gap.
- **The sequence rarely fired.** The windows were three of nine glass columns, so "sealing" one left glass beside it. An exit was "watched" inside a very wide cone (about 75 degrees either side), so with anyone in the room almost nothing ever went. Looking at a sealed exit gave no sign that it counted.
- **The room was not a child's room.** SceneCraft and the polish had dressed it like any other literary room: a kitchenette, generic carpets, pendants, and a figure hanging from the ceiling.

## The room

`ChildRoom` builds it, and the generic polish, composition and review passes leave it alone (`ScenePolish`, `SceneCraft`, `SceneReview`). The box now runs down to y −5 for the basement.

- **The bedroom.** Birch panelling to a stripped-birch rail, a pale patterned wallpaper (`child_wallpaper`) above it, a smooth quartz ceiling and birch floor. Two windows onto a night that is only dark, and two birch doors of the room's own (one "the bathroom"). The House door the reader came in by is not one of the room's exits and never goes.
- **Furniture, all one block (`nursery`, 28 kinds, facing and stage):** a loft bed on legs with a quilt (a child fits underneath), a little white desk and chair with the bedtime card on it, a red toy chest, two low bookshelves with picture books, a two-high blue wardrobe, a dollhouse, a rocking horse, a night light, three ceiling lamps, a mobile over the bed, a rug and a loop of wooden train track. In the basement: stacked outgrown boxes, a crib taken apart and put back wrong, a crayon drawing of the house from underneath.
- **Toys:** a wind-up train, a spinning top, a jack-in-the-box, a music box, a teddy bear, a stack of blocks, a rubber ball and a doll.
- **Below:** under the bed there is a gap in the boards with a ladder, then a crawlspace a child can only crawl through (coarse earth, the underside of the floor, one dead end with an unlit candle), then a brick basement where a child can stand: a lit boiler, a hanging lantern, the boxes and crib, and the exercise book with the last account on a workbench. A ladder climbs to an iron hatch in the bedroom floor beside the door the reader came in by; it opens only from below and shuts itself once nobody is in it.

All 24 new textures are drawn at 16 × 16 in vanilla's palette and shading, and every model uses cutout rendering.

## Play

Right-clicking any piece does something, and a peer sees it. The train runs a lap of its track; the top spins and falls over (click it to stand it up); the jack-in-the-box plays a note per turn and pops on the eighth (click to push him back in); the music box opens and plays for eight seconds; the bear squeaks and turns to you; the doll creaks and turns to you; the blocks stack higher twice, then fall; the ball rolls up to four blocks the way you push it; the rocking horse rocks; the mobile turns; the night light switches. The toy chest, dollhouse and wardrobe open and close and have their say, as do the shelves, bed, desk, drawings, boxes and crib. Some of them say more than they seem to (the dollhouse's little bed has been pushed aside over a gap in the floor). The room's own doors do not open. None of it is progress, and the client's following off-hand click sets nothing down on a toy. Twelve original sounds, all subtitled.

## The sequence

1. The bedtime card on the desk is the source. Once any reader present has read it, the room goes on its own shared clock: every six occupied seconds the next exit tries to go, in order: the left window, the right window, the west door, the bathroom door.
2. An exit goes only while no camera (spectators included) has it in view, within 60 degrees of where it faces with a clear line, and only when no living body is in it. It becomes panelling, rail and paper to match the wall, with a soft sound. Each time, one floor toy (the bear, the doll, the blocks, the ball) is found stuck to the ceiling above the bed, upside down, and the dolls turn toward the exit that went.
3. Each reader must personally look at every sealed exit (a second of looking, from within fourteen blocks) and is told what they see. Once all four are seen: "Every way out of the room is wall now." A peer's looking, a spectator's or an exit sealed before a reader read the card counts for nothing until that reader looks themselves.
4. Below the bed, through the crawlspace, down into the basement: "You can stand up here." The last account is ready for that reader only, and reading it resolves the room. A reader who arrived to an already-shut room gets the aftermath outcome (`examined_the_sealed_thresholds_and_found_the_remaining_crawl`); otherwise `examined_the_lost_exits_and_crawled_below_the_bed`. Crawling without having seen every exit earns nothing.
5. Once the room is shut, something above the bed whispers now and then to each reader, privately.

## Saved worlds

`child_room_0474` rebuilds an existing child's room once, keyed by its base, only when its chunks and entity sections are loaded, no camera is within sixteen blocks of it and nothing living is inside. The old ceiling figure and any old literary actors are removed. Readers who finished keep everything. For everyone else the room's sequence starts over (their card stays read): the old exit, crawl and readiness flags are cleared. New rooms are built this way and mark the checkpoint. Nothing finite is lost; the room held no supplies.

## Counts and compatibility

CHILD_ROOM stays one eligible source with the same personal resolution and account text. Still 43 sources / 33 resolutions / two kinds / three endings. Layout 40 (no layout bump); protocol 48 for the new blocks. Native coverage replaces the old aftermath case with four literary cases: the full sequence with a reader, a peer and a spectator (unseen sealing, the spectator holding an exit, toys to the ceiling, personal looking, the crawl, the basement, the reading, the hatch); a late reader who must see every exit before their crawl counts; every toy's play and that play confers nothing; and the guarded once-only rebuild with finished and unfinished readers across reload. Validation pending.
