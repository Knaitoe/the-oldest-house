# Playtest notes (0.4.77)

The owner sent a set of notes with the request to merge the Whalestoe hospital branch (merged as 0.4.76, verified in run 38111159012: all twelve jobs passed). This release works through those notes on top of 0.4.76. The elk lot rework is proposed separately and is not in this release.

## The trailer supper

- **Burning.** A cooked frank left on the griddle keeps cooking on the same occupied clock. When franks finish, readers near the stove hear a crackle and are told they look done. 160 ticks later they spit and smoke ("getting too hot"). At 300 they burn: a hiss, thick smoke, "There's burnt flesh in the air" for everyone in the scene. A cousin calls it out the first time ("Something's burning! Get them off the griddle!") and complains the second time, and every real cousin turns toward the stove for five seconds. Franks cooked before 0.4.77 never burn.
- **Burnt franks.** A burnt frank keeps its char wherever it goes: in the pan, in the reader's hand ("Burnt brat", drawn charred through custom model data 1), on a plate and in a cousin's hand. It still counts for supper. The first three real cousins to bite into one say so, each differently ("Did you even turn these over?" picks up the cousin who earlier said "You always burn them on one side"). The impostor eats a burnt one without a word.
- **Eating before supper.** The refusal itself already worked; the owner asked only for text. Trying to eat a frank from the round before supper is on the plates now says "Not right now. Supper goes on the plates first." The same line also answers the right-click itself, so a full reader (who never starts eating at all) still sees it. The click that plated or took a frank is not counted as a try at eating it. The franks themselves are unchanged.
- **The Goatman past the windows.** While the impostor waits behind the closed door at night (its circuit done, not hammering, not on its way to the window), a reader inside who looks out of a side window may see it as it is, running past outside. The figure is private to that reader, unsaved, and appears and vanishes out of their sight. It never comes within three seconds of a knock, so it is never in two places at once. Each reader sees it twice a night at most, at least twenty seconds apart.
- **The knocks.** The merged hospital branch already places the night's wall, back and window knocks at their real positions around the trailer, with a range that carries across it. They run on the occupied night clock even if the body is held up, but only when the impostor is shut outside. A night that lets it in has no knocking round the walls.

## The Crimson Hall

The stair to the gallery stopped one tread short, under its own landing, and the gallery railing ran across the landing. The third wax cylinder, on the north gallery, could not be reached on foot (the vignette audit had flagged it), so the source could not be finished. New halls are built with eight treads, a landing beyond the last one and a three-block gap in the railing. Saved halls are repaired once (`crimson_gallery_0477`), keyed by base. The repair waits until the stair's chunks and entity sections are loaded, no camera is within sixteen blocks and nothing living stands on it, and it changes only cells still in their authored state.

Nothing in the House hands out a shovel, so the snow over the red floor can now also be dug with bare hands. Holding anything else gets a line saying so instead of a silent refusal.

## Proofrock

- **The direction board** (PROOFROCK / INDIAN LAKE / ← SCHOOL CHURCH →) is redrawn as pixel art at twice block density (96 × 32 over its three blocks): painted boards, a darker frame, paint worn to the wood at the edges. It replaces the painted 768 × 256 image. The back has its own face, reading the right way round from behind with the arrows reversed.
- **Under the tyres.** Car wheels claimed a full-block occlusion shape while their model covered half a block, so the road's top face was culled and the sky showed through. Wheels now hide nothing beneath them; collision is unchanged.
- **Wheel fronts.** The car wheel model faces outward: the hub shows only on the outer face, with tread on the front, back, top and bottom (a new 16 × 16 tread texture), a dark chassis behind and the sill above.
- **The cyan car** stood across School Street. It is now parked along the school-side kerb.
- **The lawn lamp** at the gravel lane came down. The lane is a back way and stays dark. Nineteen street lamps, the pier lamp and the four submerged lamps remain.

Saved towns get both changes once (`proofrock_0477`), and only after the 0.4.68 car fixtures have replaced the painted car. The car moves only if all of its fixtures are still in place and its new spot is clear; lamp parts (old or fitted) come down only where they are still authored; the lawn is mended under the base if it was holed. The pass waits for loaded chunks and entity sections, no camera within 48 blocks, and nothing (a body or an item) in any cell it changes.

## The hospital lights

The ward's ceiling diffusers are redrawn as yellowed pixel-art panels; they used to be cut from the image-generated material atlas, and that exporter now leaves them alone. Each lit panel hums (a new subtitled sound, "Fluorescent light hums"), played on the client the way a portal's ambience is. Lit panels take random ticks, so this only happens near a reader. Now and then a tube drops out for a few ticks with a subtitled buzz ("Fluorescent light flickers"), sometimes stutters once more, then holds. The light level really goes to zero while it is out, and a dark off model shows. Saved lights load lit.

## Zampano's courtyard

The four bedside tables with a lamp and a vase at the courtyard's edges are replaced by stone planters, two azaleas and two ferns. A small fountain sits between the gate and the mat: a sunk three-by-three basin, a low mossy kerb, a pedestal and a bowl that brims and drips, with the sound of water. One fish swims in the basin.

A reader can take the fish with a bare hand, once. A bucket does not take it. It is an ordinary raw cod, and the cat on the mat takes it as it takes any meat. If the fish is gone while the cat is still waiting, another is in the basin when a reader who has not had one arrives. Once the cat has come off the mat, no more fish come. A fish confers nothing.

The courtyard strays now start clear of the fountain. Saved courtyards get the planters and fountain once (`courtyard_0477`). Planters replace only tables still there with their lamp or vase. The fountain goes in only where the ground and the air above it are still as the House left them. The pass waits for loaded sections, no camera within sixteen blocks and nothing living in its cells.

## The séance

- **Driven out, they leave.** When three disturbances drive the family out, they get up and hurry one behind another through the west door into the back room. The last one in shuts it behind them, never on anyone standing in it, and the father says something about doors. They used to be given a single navigation order that often went nowhere. The door they leave shut also no longer keeps the iron door locked. Actors keep their place on the way across a reload. A sitting that ended in fear before 0.4.77, with the family still at the table, sets off now.
- **The ghost.** Once the sitting is over, a reader who leans into the shut iron door for a moment finds it isn't there for them and comes out on the other side, in either direction. The door stays shut and locked to the living, and the first lean says only "The iron is cold. Then it is hardly there." The older route (shut the other doors and shutters, then open it) still works.
- **Touch.** Right-clicking a family member gets a reaction in their own words: three lines each for the medium, the mother, the father and the daughter, in turn. They flinch for three seconds and shiver audibly. A touch resets that reader's listening but is not one of the three disturbances and counts for nothing by itself.

## The elk lot: proposed rework (not in this release)

Today the lot is a grey concrete slab, a siding box for the bar, three trucks and rows of tall grass. The elk walks off in a straight line, the accusers are three standing strangers repeating one line, and the field is a teleport loop. The proposal, for approval:

1. **A real roadside bar at night.** A two-lane highway crossing the scene with a painted shoulder and a gravel pull-off. The bar has a lit sign, a porch, an inside you can walk through (booths, a pool table, a jukebox that plays), and a back door onto the lot. The pickups are fixtures like the town's cars, parked at angles, with headlights left on.
2. **The elk as an event, not a walk.** The cow elk comes over the highway fence, slams between two pickups (a real dent, an alarm), and stops to look at the reader before going into the field through a broken fence gap. The reader can hurt it or not, which carries into the fan room as now.
3. **The men.** Three named drunks come out of the bar with flashlights: real beams (light blocks moved each tick) that sweep the lot. Each has their own lines, escalating from questions to accusations using the reader's name. They block the road back. They never touch the reader; they herd.
4. **The field.** A wide wheat-and-stubble field under a dark sky with a treeline, fence posts and an irrigation ditch. The loop is hidden by a repeating landmark (the same broken fence post, then a dropped hat) instead of a visible teleport. The herd is eyes in the stalks that close in by sound, and it cuts to black over hooves as now.
5. **After.** The clipping on the bar's noticeboard keeps today's resolution and account text: still one source, one resolution.

Saved lots would be re-carved once, only when loaded, unwatched and vacant, keeping the elk's UUID, the wound record and every reader's progress.

## Counts and compatibility

No source, resolution or ending changes: 43 sources / 33 resolutions / two kinds / three endings. Layout 40; protocol 51, because the ward fitting has a new `lit` state. Use matching 0.4.77 clients and server. Four new textures (burnt brat, board back, car tread, ward diffuser off; 491 PNGs in all) and two new subtitled sounds (ward hum and flicker). New native coverage:

- the supper test gains the refusal, a burn, a burnt frank plated and shown, and a spare frank eaten once supper is plated;
- a private window dash with its knock and assault guards;
- the Crimson gallery's on-foot reach in the architecture test, and its guarded saved repair;
- bare-hand snow;
- the town's car, lamp and wheel occlusion, fresh and saved;
- the courtyard fountain, fish and saved pass;
- the séance's touches, flight, shut door and walk through the iron door.

Validation pending.
