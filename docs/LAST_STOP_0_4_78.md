# The Last Stop (0.4.78)

The owner approved the elk lot rework proposed with 0.4.77 and added three things: a jukebox a reader can play, people inside the bar who talk, and drinks on a tab. The lot is rebuilt whole in its existing slot as a roadside bar at night. It is still one source with the same resolution and account: 43 sources / 33 resolutions / two kinds / three endings.

## The place

The reader steps out onto a gravel apron in a gap in the right-of-way fence. In front of them is a two-lane highway: edge lines, a double yellow centre line, shoulders and reflector posts. Both ends of the road disappear into the woods. Across it are:

- **The bar:** board and batten under a false front, with a porch, a lantern, a bench and a pole sign by the road ("LAST STOP" over "COLD BEER", readable from both sides). A neon BEER hangs in the window.
- **The lot:** gravel with oil stains and a puddle, lit by a sodium lamp beside the bar's side door, with a dumpster on the back corner. Three pickups (rust red, faded blue and tan) point at the field with their headlights left on.
- **The field fence:** post and rail, broken through at one gap with its rails down, and a dry ditch behind it, bridged by planks at the gap.

Inside the bar:

- the counter, with a brass foot rail, taps, glasses and the register;
- the back bar: cabinets, two shelves of bottles and a mirror;
- six stools, two red vinyl booths along the west wall, a pool table under a green lamp, a dartboard, a jukebox, and the noticeboard by the door.

The till receipt by the register is the source; the clipping on the noticeboard is the ending.

Past the fence is a wide field of dry stalks and stubble, with a trodden lane up the middle, an old line of fence posts (one of them broken), a dead tree, and hedgerows and woods down both sides. The field repeats every 34 blocks. A reader carried back one repeat at its far end stands somewhere identical to where they were. Past the fence the client's night haze closes to about a dozen blocks, so the repeat is never in view.

Nothing generic is laid over the lot: polish, composition, review and playtest passes all skip it.

## The bar is open

- **People.** Marla tends bar. Russ and Hollis sit at the counter, Denny plays pool and Wade has a booth. Each has their own lines, given in turn to each reader who speaks to them, and now and then one of them says something to the room. Their skins are placeholders, see below.
- **Drinks on a tab.** Speaking to Marla opens her menu: a draft beer ($2.50), a whiskey ($4.00), a cola ($1.00), and the reader's own tab. Each drink is a real glass (a potion with its own colour and name, no effects listed) in the reader's inventory. A whiskey goes to the head for a few seconds, and so does a beer from the third drink on.
  - Every reader has their own tab and stops at six drinks ("That's enough for tonight, sugar").
  - Marla takes an empty glass back.
  - The clipping ends with the open tab, if there was one.
- **The jukebox.** It starts the night playing. Each press changes the record (stal, mall, chirp, wait), then silence, then round again. The vanilla interaction is cancelled: nothing goes in or comes out, and a disc in the reader's hand stays there.
- **The pool table and darts.** The pool table breaks and racks, with a sound. Denny says something if he is at the table. The darts land three to a throw.

## The night

One shared clock counts only while someone is in the scene.

- **When it comes.** After a minute of the bar, or twenty seconds once a reader is outside, the cow elk comes over the highway fence from the dark, gallops across the road, and slams into the blue pickup between the parked trucks. The door really dents (it becomes a dented door). A crash sounds, the alarm sounds for fifteen seconds and the headlights flash. Marla asks what that was.
- **The elk looks.** It is knocked back out of the gap and stands in the lot looking at whoever is there. It goes after it has been seen for two seconds, when a reader comes within five blocks, or after twenty seconds. It walks through the broken gap, over the ditch and up the lane, then waits in the field, turning to watch whoever is in the stalks. It can be struck as before, and the wound is the striker's own, carried into the fan room.
- **The men.** Russ, Denny and Wade go out of the side door with flashlights. A flashlight is real light: a light block where its beam lands, moved as it swings, with motes along the beam. Two beams can share one spot. The men search around the trucks, then turn on the reader by name in stages:
  - questions;
  - "You were out here";
  - accusations;
  - the field.

  By the last stage one of them stands on the road back, one by the bar and one at the gap. They keep their distance (a man near a reader steps back and around them) and never touch anyone. Mobs can be walked through, so the way back is never closed. They have extra lines for a reader who hurt the elk.
- **Reset.** When the scene has been empty for five seconds and no camera is within seventy blocks of it, the night starts over. The cast goes back to their places, the elk out of sight, the door mended, the side door shut and the flashlights out. Pieces that are not loaded at that moment are put back as they load.

## The field

Each reader's part is their own.

- **Counting.** Field time counts only once the reader has personally seen the elk (in the lot or in the field).
- **The herd.** Every four seconds the hooves come, and two pairs of eyes in the stalks, private to that reader, move in closer with every pass.
- **The loop.** At the far end of the field the reader is carried back one repeat, without a cut.
- **The cap.** After the first time round, a dropped cap lies by the broken post. It is shown only to that reader (a block sent to their client alone; the world is unchanged), so a reader learns they have been there before.
- **The ending.** After two loops and twenty seconds in the field, the next time the reader reaches the end it cuts to black over the hooves. They wake on the bar's porch, Ready, with the clipping waiting on the noticeboard. The resolution is unchanged: `escaped_the_looping_field_and_read_own_clipping`.
- **Without the elk.** A reader who has not seen the elk is simply turned round at the field's end, and nothing counts.

## Saved lots

`elk_lot_0478` re-carves a saved lot once, keyed by its base. It waits until:

- every chunk and entity section of the scene is loaded;
- no camera is within sixteen blocks;
- nothing living is in it but the elk and the scene's own cast.

The elk keeps its body, its wound and its UUID, and is put out of sight for the first night. The old accusers and herd eyes go. The old source lectern gives up its book before it is removed, so nothing drops. Every reader's record is kept exactly as it was.

Until a saved lot is re-carved it keeps working as before, with its old source and ending in their old places.

## Assets

- **One block.** One `roadhouse` block (49 kinds, facing, stage; 784 states) carries the road, lot gravel, siding, the bar's furniture, the sign, the field's stalks and stubble, the cap and every pickup part.
- **Textures.** 49 vanilla-style 16x16 textures drawn by `tools/generate_last_stop_assets.py`, which draws them in code with nothing sampled. The pickups' body panels are grey and painted by a client block tint from their stage; their glass, chrome, tyres and lights are separate untinted overlays. The neon, the sign, the headlights, the tail lights and the pool lamp are drawn full-bright.
- **Sounds.** Four original subtitled sounds: a car alarm, a truck passing on the highway, pool balls, and metal crumpling.
- **Placeholder skins.** The bar's five people wear placeholder skins: `textures/entity/literary_bar_marla.png`, `_russ`, `_denny`, `_wade` and `_hollis`. Each began as a copy of an existing stranger skin so the cast renders. The generator never overwrites them once they exist, so the owner's designs can simply replace them.

## Counts and compatibility

No source, resolution or ending changes: 43 sources / 33 resolutions / two kinds / three endings. Layout 40. Protocol 52, because the roadhouse block is new; use matching 0.4.78 clients and server.

New native coverage (four literary cases):

- the bar: the five people, the tab and its limit, a whiskey's effect, an empty glass, the jukebox and the pool table, and the till receipt read;
- the night: the crash, the dent and alarm, the elk seen, the men out with lit flashlights, saying the reader's name, standing on the road and never closer than arm's length, and the reset;
- the field: its exact repeat, the loop counted only for a reader who saw the elk, the wound kept by its striker, and the cut to the porch with the tab in the clipping and credit for its own reader only;
- the saved re-carve: its body and camera guards, the kept elk and records, no dropped book, and a checkpoint that survives reload.

The architecture test checks the register as the lot's source surface. The staircase composition test no longer counts the bar, which is no longer composed.

Validation pending.
