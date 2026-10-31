# Owner vignette proposals, 31 October 2026

The owner sent five vignette specifications (and, separately, two shorter ideas) on 31 October. The owner's instruction for all
of them: "when you're adding a vignette, ensure you weigh it against everything we've discussed before - multiplayer
compatibility, literature references - different perspectives taken into account/integration into a player's house of leaves,
and win/lose states."

This file keeps each specification's substance (every name, number, zone, mechanic and visit), condensed from the owner's
messages, followed by how it would fit this mod. Nothing here is built yet. Each fit section is a first reading, to be settled
before building: where a spec's engineering details (separate namespaces, player attachments, structure templates, instant
death) differ from how this mod already does the same thing, the mod's way wins and the spec's intent is kept.

Standing rules every one of them meets when built: new builds only; private per-reader copies or shared scenes with personal
records (`WitnessAccount.Story`, the quota stays derived); speech on displays and the action bar, never chat; every sound
subtitled and paired with something seen; native pixel scale art drawn by a generator with `--check`; finite originals; an
account written for the reader's House of Leaves in the voice of whoever's papers they are; a real way to fail as well as to
resolve; a co-op reading for two or more readers.

## Shorter ideas (same day)

- **Polybius.** "vignette based on Polybius - real arcade games, real leaderboards per server?"
- **The companion's replacement.** "vignette that only triggers for players with a companion - the player is on a neighborhood
  street. all homes are boarded minus one. an npc replaces their companion for that vignette. follows around "meowing" or
  "barking." the player can't shake them lose. the npc has a hoard of dead cats and dogs in its messy, uncared for home."

## 1. The Left-Right Game (`vignette/asphalt-corridor`)

**Source.** The Left-Right Game (liminal highway expedition folklore). Non-Euclidean road horror: strict rule discipline,
disorientation, dread of breaking an arbitrary sequence.

**Look.** Wet weathered asphalt with faded double-yellow lines; brutalist concrete Jersey barriers, oxidized steel gantries,
rusted 1970s sedans half in fog; sodium-vapour amber lamps (#E09F3E), flickering fluorescents, low forest silhouettes (#1B2A26).
Tight fog, start 4 / end 18 blocks, colour #2A2621 (0.164, 0.149, 0.129); in a wrong-turn cul-de-sac the fog lerps over 40
ticks to bleached salt white #D8DBD6.

**Space.** A fixed 64 x 64 x 32 pocket with four modular Y-junction sectors (`sector_toll`, `sector_bluejay`,
`sector_jubilation`, `sector_sinkhole`) and a penalty sector (`sector_culdesac`). Each Y forks at 45 degrees round a central
Jersey barrier and sign gantry. Crossing Z+16 in a branch wraps the reader seamlessly (yaw and velocity kept); the next
junction's dressing is swapped behind the fog.
- Toll: brutalist tollbooth island, rusted drop-arm gate, flickering fluorescent canopy, CB radio on a steel desk.
- Bluejay Cut: pine-flanked split; the right branch has a glowing glass phone booth, reed ditch, downed power pole.
- Jubilation: 1950s pastel cul-de-sac facades with no front doors or lower windows, only dormer windows.
- Wintery Sinkhole: asphalt slabs pitched down 30 degrees, street signs jutting, Alice's idling green station wagon.
- Salt Cul-de-Sac (penalty): cracked grey salt flat, unlit lamps, three-car-high barricade of crushed sedans, Marjorie.

**Rules.** Per reader: visit (1..4), junction index, expected next turn (starts LEFT), in cul-de-sac, flare ticks. The right
fork advances the index and flips the expected turn; the wrong fork sends the reader to the cul-de-sac.
- *Road flare (visit 2).* Lit and placed at the Y-divider before taking the wrong branch, it burns 240 ticks with a crimson
  spark ring (#FF2200) and a floating segmented burn gauge, and holds off the cul-de-sac so the reader can reach the Bluejay
  booth's recording/key and backpedal.
- *Centreline gaze (visit 3, Jubilation).* Dormer windows light with CRT scanlines. Within a 40-degree yaw cone of a lit
  window with pitch above -15 degrees, gaze-lock ticks accumulate: movement slows 10% per tick to -90% and the camera locks.
  Pitching down past 35 degrees to stare at the double-yellow centreline breaks it.
- *Marjorie (the hitchhiker).* Tall, thin, wet light-absorbing trench coat, wet hair over the upper face, slack jaw, dithered
  translucent in fog. Appears 9 blocks ahead against a sedan in the cul-de-sac. Still while held in a 55-degree view cone;
  turn away (look dot < 0.25) and she lunges, which fails the visit. Backing out across the entry threshold while holding her
  gaze dissolves the cul-de-sac into a valid junction.
- *Cast.* Apollo (convoy leader) speaks only through the toll CB radio and dashboards, leaving field logs and TTY printouts.
  Bonnie (the Bluejay caller) is on the payphone line in visit 2; her words print reversed in condensation on the booth glass
  as it floods. Jubilation residents are silhouettes sliding behind upstairs curtains.

**Parity.** CB radio: green oscilloscope waveform, rotating roof beacon, TTY roll. Payphone ring: amber rotary strobe, ripple
on the glass, water rising in the booth. Marjorie's approach: condensation footprints, VHS tracking jitter, text near the
shoulder. Window hum: windows turn cyan-white with scanlines, a light bar on the wet centreline. Wagon exit: 1 Hz amber
hazards, exhaust in time with the engine.

**Visits.** 1, the Bristol Ferry tollbooth: the rule from the CB transcript; four junctions L R L R; a tutorial cul-de-sac
teaches Marjorie without penalty. 2, the Bluejay detour: junction 2's left is a locked maintenance gate; the key is in the
flooding payphone booth on the wrong (right) branch; the flare buys the trip. 3, Jubilation: junctions 5 to 8 among doorless
pastel houses, the centreline rule. 4, the Wintery Sinkhole: freezing rain and snow, buckled road, erratic signs; open Alice's
wagon tailgate to leave.

**Assets named.** `weathered_asphalt` (plain, double yellow, white dash, cracked salt), `jersey_barrier`,
`amber_gantry_beacon`, `cb_transceiver_block`, `bluejay_payphone_booth` (water level 0..4, strobe), `road_flare_block`;
items `apollo_convoy_log` (four entries by visit), `ignited_road_flare`, `brass_conduit_key`.

**Fit (first reading).** A road is new ground for the mod and the rule is clean. Multiplayer: the turn sequence and the
cul-de-sac are personal; two readers at one fork can disagree, so either each reader gets a private copy (as the uncanny rooms
do) or a pair shares one sequence and either one's wrong turn takes both (a co-op reading worth having: one holds Marjorie's
gaze while the other backs the pair out). Marjorie's lunge should fail the visit the way the mod's other failures do (out to
the hall, the visit to try again), not kill. Literature: the CB transcripts, Apollo's convoy log and Bonnie's reversed words
give three voices; the House of Leaves account is the reader's own log of turns. Win: the wagon at visit 4. Lose: a lunge.

## 2. The Dead Air Broadcast (`vignette/candle-cove`)

**Source.** Candle Cove, early-1980s public-access signal hijacking, numbers stations, studio stagecraft. Weaponized
nostalgia: a show a generation remembers that was only static.

**Look.** 1983 basement: walnut paneling #3B2219, mustard shag #A38335, faux-brick hearth #6E3C30, Zenith CRT #1F1A17. Stage:
rotting red velvet #5A121A, gilt trim #9A7B38, cardboard waves #2C4A5E, magenta spots #C8328C. Salt marsh: overcast #8C969E,
cordgrass #4A472D, brackish pools #242D2D, chain-link #3D3B37, phosphor-green beacons #32CD32. Dead air: cyan #00E5FF wash.

**Space.** Zone 1, the 1983 basement hub (14 x 14 x 6): wood paneling; shortwave receiver on a folding table; Zenith CRT in a
corner; Percy's crate beside it. Zone 2, the shifting hallway (12 x 5 x 4) north of a bulkhead door: retuning swaps its whole
volume in one tick, keeping anyone inside clear of the blocks. Zone 3, the salt marsh relay (32 x 32 x 28): brackish water,
dead cordgrass, chain-link, three 24-block guyed lattice masts (Alpha west, Beta north-east, Gamma south-east), each with a
rotatable dish at its base and a five-bulb signal light at its top. Visit 3 drops the basement walls to show plywood flats
held by iron sandbag braces inside a black studio.

**Rules.** Four channels: CH_STATIC 000.0 kHz (amber light 12, bulkhead locked, nothing hostile); CH_3_MARSH 074.5 MHz
(green 8, the hallway is a flooded storm drain to the marsh, the Skin-Taker patrols the boardwalks); CH_4_STAGE 088.1 MHz
(magenta 10, the hallway is a marionette-theatre corridor with velvet and ropes, the Skin-Taker stalks it); CH_DEAD_AIR 999.9
MHz (flickering cyan 15, every Skin-Taker freezes with a solid body that can be stood on or squeezed past; exposure counts up,
static creeps in from 120 ticks, at 160 a feedback burst snaps the channel back to CH_4_STAGE; leaving resets it).
Marsh puzzle (visit 2): each dish turns in 45-degree steps; targets Alpha 90, Beta 225, Gamma 315; a matched dish steps its
pylon through off, red strobe, amber low, amber high, green pulse, cyan lock; three cyan locks open the transmitter.

**Cast.** Percy, the laughingstock puppet: a 1.2-block water-damaged wooden marionette boy tangled in cut strings on a crate,
hinged jaw, brass gears in his torso, head tracking the reader across 60 degrees; speaks by a scrolling brass marquee in his
chest and drops printouts in his lap. The Skin-Taker: 2.6-block skeletal marionette of stitched wet burlap and ash-wood
joints, black stovepipe hat, oversized grinding porcelain teeth, shredded velvet cape; follows at 0.28, attacks within 1.5.
Horace Horrible: a silhouette on the CRT (bowler, handlebar moustache, monocle) moving against static before the visit-3 reveal.

**Parity.** Static vs dead air: a green sine on the receiver and repeaters flattens to a cyan line. Percy's clacks: the chest
marquee scrolls, his eyes pulse red. The Skin-Taker's approach: lights above it spark and drop phosphor, its porcelain jaw
glints. Antenna lock: the pylon's five bulbs fill upward, pulsing faster until solid.

**Visits.** 1, the NetNostalgia printouts: printout #1 (2001 forum users arguing whether Candle Cove aired in 1972); tune
CH_4_STAGE, the Skin-Taker blocks the corridor; a wall-breaker lever triggers dead air; duck under its arms, fetch the brass
amplifier tube from the stage-end chest, fit it to the receiver. 2, the salt marsh antennas: the tube allows CH_3_MARSH; the
storm drain to the marsh; freeze it with pole-mounted dead-air breakers while turning the three dishes. 3, the unbolted
soundstage: the walls fall; teleprompter script pages replace the printouts and describe the reader's own earlier actions as
stage directions ("ACTOR adjusts shortwave dial to 074.5 MHz. ACTOR hesitates before entering marsh."). 4, the jawbone
transmitter: the back curtain opens on the master booth over the marsh: no crew, a rusted child's highchair facing nine
stacked CRTs of static, cut strings hanging over it. The centre monitor: Percy's marquee, "YOU ARE ON THE AIR, LITTLE
PIRATE."; the room clears.

**Assets named.** `shortwave_receiver` (channel 0..3), `dead_air_wall_breaker` (eight-segment countdown ring), `zenith_crt_tv`
(static, Horace, stage feed, open threshold), `plywood_stage_flat` (paneling one side, stencilled studio plywood and brace on
the other), `marsh_parabolic_dish`, `signal_strength_pylon`; items `netnostalgia_printout`, `teleprompter_script_page`,
`brass_amplifier_tube`.

**Fit (first reading).** The channel dial is a strong verb and the visit-3 scripts are this mod's own move (the House writing
down what the reader did). Multiplayer: one receiver changes the world for everyone in it, so the dial is shared and
deliberate, with dead air freezing the Skin-Taker for all; the scripts name each reader as ACTOR and a second reader as
SECOND ACTOR from what each did. Literature: forum printouts (2001), the teleprompter's stage directions and Percy's marquee are
three hands; the account is the script page with the reader's lines. Win: the centre monitor. Lose: the Skin-Taker's touch
(out, visit again). The basement-as-stage-flat reveal should be built as real geometry the reader can walk round.

## 3. The God of the Pitt (`vignette/caver-squeeze`)

**Source.** Ted the Caver (2001 Angelfire logs) and Appalachian karst tight-caving. Compression, the arrogance of forcing a
squeeze meant to stay sealed, barometric paranoia in the dark.

**Look.** Wet ochre and slate limestone #4A4E51, calcite dripstone #D9D4C7, red cave clay #6E2D25; rusted pitons #3A3532,
fluorescent orange nylon cord #FF5500, flickering carbide lamps at 1800K.

**Space (48 x 64 x 36).** Chamber 1, Brad's staging ledge (y 48): a dome sinkhole floor with ropes, crates, carbide refills,
piton tins; a two-high stoopway into Tunnel A. Tunnel A, Floyd's Tomb (y 46 to 44): an 18-block 1 x 1 crawl (swimming pose)
under unstable pinch-ceiling slabs, with a 2-pixel fissure running alongside into an unlit parallel crawl. Chamber 2, the
Wind Tunnel and Round Stone Room (y 42): a 1.5-high vault with a perfectly round 4 x 4 x 1 flat stone wheel against a hidden
alcove and oxidized copper-sheathed grooves in the floor. Tunnel B, the Scrambled Chimney (y 42 to 24): three staggered drops
joined by 1 x 1 squeezes; its layout changes in visit 3. Chamber 3, the Pre-Echo Ledge (y 18): a 1.5-wide shelf over a void.

**Rules.** A shared draft cycle: DRAFTING_SAFE 14 s (horizontal white dust), DRAFT_DYING_WARNING 3 s (dust slows and turns,
the barometer shakes), STAGNANT_CRUSH 6 s (silence, silt falling). In a crawl (headroom 1.2 or less) during the crush,
moving faster than 0.02 a tick, swinging, or holding a lit lamp steps the slab above down one stage a tick (0..6; 2 px a
stage; pinned, 2 damage a second, no jumping); stillness, lamp off and lying down until the draft returns undo it.
Chiselling (visit 1): seam blocks have aperture 0..4; the masonry hammer held on a seam takes 3 s per step, only in the safe
draft (chiselling in the crush adds three crush stages and wakes the Seam-Crawler). Guide line (visits 2-3): orange nylon
cord strung between placed pitons; in visit 3 the line beyond Chamber 2 is cut, running into a 2-pixel crack at y 32, frayed.

**Cast.** Brad ("B"), co-explorer: mud-caked coveralls, harness, yellow hard hat with brass carbide lamp, rope coils; holds
the belay in Chamber 1 in visits 1-2 and signals the draft with his lamp (two flashes safe, amber strobe danger); gone in
visit 3, his cracked helmet strobing on Chamber 2's floor toward the chimney. The Seam-Crawler (the Hodag): only ever inside
the fissure network beside the crawls; pale abraded limbs and two retroreflective eyes in the crack; idles, stalks during the
crush (red clay puffs), and on movement or light in the crush reaches out and adds crush stages.

**Parity.** Draft: white spore streams; barometer needle green. Collapse: streams freeze into falling silt; needle red and
shaking. Brad: his lamp beam flashes or strobes; journal cards on pitons. Seam-Crawler: clay puffs from the seam, two green
points. Pre-echo: dust rings and footprints two blocks ahead; a direction arc.

**Visits.** 1 (19 May 2001), widening Floyd's Tomb: clear four seams by the barometer; log #1 notes the draft from a
fist-sized hole smelling of wet copper. 2, the wheel and the spool: string the line to Chamber 2; inspecting the wheel's track
triggers a collapse and a crawl back along the cord. 3, the shifted return: Chamber 1 silent, belay hanging; the wheel pushed
out from inside; Brad's helmet by the chimney; the chimney's passages changed; the line into a crack; the Seam-Crawler alongside.
4, the bottomless ledge: footsteps heard two seconds early; footprints ahead lead over invisible bridges to a rusted ladder out.

**Assets named.** `pinch_ceiling_slab`, `narrow_limestone_seam`, `fissure_wall_block`, `orange_guide_line`, pitons; items
`brass_aneroid_barometer` (pressure model overrides), `carbide_caver_lamp` (fuel `carbide_drip_canister`),
`heavy_masonry_hammer`, `caver_field_journal` (four Angelfire-style entries with ASCII surveys).

**Fit (first reading).** The mod already has Ted the Caver (`ted_caver`): the packed-rubble crack mined five strokes a block,
the shared in-breath clock with drifting dust that pushes crawlers, a personal string line, winding crawls with air bells and a
breath gauge, the choke, the stone set into the bowl's back wall that moves on the in-breath, the crouch-high low chamber, the
pursuit, the third and seventh later arrivals. This proposal reads best as that vignette's next form: Brad as a real partner
(and a second reader taking his place at the belay in co-op), the draft cycle's crush on the ceiling slabs, the Seam-Crawler
in the wall, the cut line and changed chimney on a later visit, and the pre-echo ledge as the final crossing. Keep the
existing Witness story (`retraced the squeeze`) and its records; make the deeper visits the new content.

## 4. The Polaroid Trail (`vignette/balloon-trail`)

**Source.** Penpal (Footsteps, Balloons, Boxes, Screens, Friends). Childhood woodland exploration soured by unseen predation:
every place you walked was being photographed from ten feet away.

**Look.** Late-autumn twilight backyard, chain-link, rusted corrugated culverts, damp red-brick foundation piers, hanging pink
insulation, velvet cinema seats, dense pines, stark white xenon flashes, bright yellow latex balloons.

**Space (64 x 64 x 24, four isolated zones).** Zone 1, the yard edge and creek culvert (64 x 16 x 24): gravel driveway edge, a
back porch (0,4,12), a shallow creek (0,2,32), a half-built pallet raft, a 3 x 3 corrugated storm pipe into an embankment
(-14,2,48). Zone 2, the crawlspace (48 x 48 x 1.5, crawling): through a pried lattice vent (12,1,0); brick piers, cast-iron
drains, insulation batts, a plastic cat bowl, stolen bedsheets (-8,0,-12), a square hole dug into the clay (0,-2,-20). Zone 3,
the neighbourhood cinema (32 x 64 x 12) by an overgrown service alley: sticky concession lobby (0,0,0), velvet rows facing a
torn screen, Row K (-8,1,-12) to Row A (-8,1,10), a projection booth window (0,8,-28). Zone 4, the pine excavation site
(64 x 64 x 24): a wooden deer blind (16,6,-8), two rectangular six-foot trenches (-4,0,12) and (4,0,12), the canopy (0,18,12).

**Rules.** Every second the reader's position, eye, yaw and pitch go into a 30-second ring. A watcher node flashes; the frame
from exactly 30 seconds ago becomes a developed photograph (dithered black and white, the reader's silhouette from the
watcher's angle) dropped at a nearby point (stump, lunchbox, seat) under a yellow balloon tethered two blocks above. To
progress, stand where the photo was taken of you and look back at the watcher (look dot above 0.94): a dust-mote beam runs
down the sightline with a shutter click and the linked mechanism opens (a vent unlatches, a key drops, a door opens).

**Cast.** Josh (memory echo): a translucent sepia (#D97706) boy in a striped shirt with a yellow flashlight and walkie-talkie,
no collision; appears at a resolved sightline, points his beam at the next objective for 3.5 s, dissolves into fireflies. The
Photographer (the watcher): tall, heavy-shouldered, dirt-caked coveralls, head lost in an overexposed white/cyan flare; never
walks, only hops between vantage nodes: flash, drop the photo, move.

**Parity.** Flash and charge: an amber red-eye LED (#FFBF00) glints from the hiding place during the 1.5 s charge; a white
flash cone and a 0.4 s cyan outline. Drop: the yellow balloon (#FFE600) with dandelion dust trailing down to the photo.
Walkie-talkie: its red LED pulses with the sound; a static ribbon with waveform bars. Cinema seat: projector motes swirl over
the row, the brass aisle light flickers amber (#D97706).

**Visits.** 1, footsteps and balloons: twilight porch; balloon tag #1 on the railing ("If found, please drop in mail or
call..."); crossing toward the raft, a flash from inside the culvert; 30 s later a balloon by the raft and photo #1 (you,
crossing the creek, seen from the pipe); align from the creek to the culvert: the side grate's latch, into Zone 2. 2, boxes:
the crawlspace; red-eye behind a pier; sightline A to Mrs Maggie's missing ceramic cat bowl of stale water; sightline B to a
nook with a mattress of stolen clothes, among them a tunic in the reader's own skin palette from visit 1; B unlatches a floor
hatch to the clay shaft. 3, screens: the cinema; the projector comes on; a silhouette alone in Row K; each flash's photo shows
it nearer, K, H, E, B; at the last it is in Row A, right behind where you stood; turning round cuts the power and opens the
fire exit. 4, friends: night pines; two trenches; flashes every 6 s from shifting canopy angles; the last photo, by trench A,
is taken from straight above (the crown of your head); stand over the trench and look straight up (pitch below -80): the beam
goes up into the branches to the exit, among fifty lit yellow balloons.

**Assets named.** `dropped_polaroid_block`, `cinema_velvet_seat` (empty, folded, occupied silhouette; aisle light),
`projector_beam_emitter`, `foundation_lattice_vent`; items `developed_polaroid_photo`, `plastic_walkie_talkie`,
`paper_balloon_tag` (four lore tags).

**Fit (first reading).** The delayed photograph is a fine verb and fits the mod's real-camera work (owned cameras, rendered
frames, the CCTV feeds). Multiplayer: each reader has their own watcher and trail, but in co-op the photographs can show the
other reader (the stolen-clothes mattress then holds both readers' palettes), and a reader can stand in another's photographed
spot. Literature: the balloon tags (a child's hand), Josh's walkie-talkie, the photographs themselves; the account is the
stack of photos in order. Win: the canopy exit. Lose: needs one: for instance the Photographer's flash catching the reader
face to face (looking at the watcher before the photo has dropped) ends the visit.

## 5. The Whistlers in the Pines (`vignette/the-whistlers`)

**Source.** Backcountry mimic folklore; a stilt-mounted alpine fire lookout. Claustrophobia, exposure, sensory deprivation,
strict gaze and shutter discipline against something that baits by sound and sight.

**Pillars.** A lit 9 x 9 glass cab is a lantern in the dark while the woods stay opaque. Looking toward a whistle outside
draws it in: pitch down to the floorboards, walk to the active window blind and drop its canvas shutter. Every cue has a
visual pair in the cab (pendulum, frost ripple, lantern colour, teleprinter tape). The Whistler never paths; it advances by
tiers on gaze failures, with stolen radio voice fragments and small distortions in the treeline.

**Look.** Creosote timber stilts #2C221E, olive-drab canvas shutters #3B4436, brass azimuth fittings #B8860B, frost-white
glass #E0F7FA, black spruce #0D1B1E, phosphor-green TTY paper #39FF14, violet-blue warning flames #6A5ACD.

**Space (48 x 48 x 64).** Level 1, the cab (y 92 to 97): 9 x 9 x 5 inside a one-block catwalk with timber rails; all four
walls five-wide glass with canvas roll shutters; the brass Osborne fire finder in the centre; south-east desk with VHF radio,
teleprinter and wood stove; a trapdoor to the stairs; a roof hatch. Level 2, switchback stairs (y 64 to 92): four icy timber
flights round the four stilts, a hanging trail lantern on each landing. Level 3, the snowbound clearing (y 64): a broken fence,
a corrugated iron diesel generator shed, a frosted propane tank, chest-high snowbanks that break sight to the treeline, black
spruce all round. Visit 4: the ground is gone to a starlit void, and eight identical lookouts stand at 30 blocks round
Tower #4 at the cab's height.

**Rules.** Whistle events pick a wall (N/S/E/W), swing the ceiling pendulum toward it and ripple its glass. Every 5 ticks, if
the reader's horizontal look is within about 50 degrees of the whistle (dot above 0.64) and that wall's shutter is open or
torn, the gaze check fails and the threat tier rises: treeline (0), shed roof (1), stair landing (2), catwalk glass (3); a
failure at 3 breaks the glass, a critical failure. Counterplay: pitch below -45, walk to the wall, pull the cord (open to
closed), then look up again. The fire finder (right-click) opens a top-down azimuth disc; turning the brass alidade to within
1.5 degrees of a flare or smoke bearing locks it and prints the objective on the teleprinter, no window needed. Visit 3: gales
tear three shutters (torn: no cord); two canvas repair tarps patch a torn frame, and must be moved from quiet walls to
whistling ones, eyes down.

**Parity.** Directional whistle (two notes, F#5 to D5): the brass wind-vane pendulum swings toward the wall and lights its
amber quadrant lamp; frost rings ripple on that glass with falling ice. Whistle on the stairs: landing lanterns swing 45 degrees
toward it and their flame turns from yellow to violet-blue. Radio (Ranger Sarah): the teleprinter's green RX lamp strobes and
tape feeds out with her words. Knocking (three): frost ferns bloom on the struck pane (stage 0..3); snow dust drops from the
hatch seams in three pulses. Generator failure: the bulb flickers and dies, the teleprinter lamp goes out, frost creeps in from
the window edges; outside, the exhaust stops and the shed lamp turns red.

**Cast.** Ranger Sarah (Tower #7, 3.2 km north-east): radio and teleprinter only; her messages degrade, and by visit 3 her tape
prints the reader's actions as they happen. The Whistler: 2.8 blocks, unnaturally slender, bark-dark skin, long limbs, sunken
light-eating sockets, fluid posture shifts; stands at the tier's anchor (treeline, shed roof at y 69, second landing at y 78,
pressed to the cab glass at y 92), facing the cab, moving between them unseen with a snow step. Mirror lookouts (visit 4): a
figure in each of the eight towers copies the reader's position, crouch and yaw relative to its own cab, except the north
tower's, which stands still at its window with binoculars fixed on Tower #4.

**Visits.** 1, orientation at dusk: Sarah's welcome on the tape; the Lookout Protocol Manual; the radio check-in; log three
flares with the fire finder; two whistles (north, west) teach the shutter. 2, the blizzard outage: midnight, power out, heater
off, glass frosting; take the diesel starter crank, down through the trapdoor and the stairs to the shed, prime and crank,
climb back; whistles on the stairs: when a lantern turns violet, crouch behind the rail or a snowbank until it is yellow again.
3, whiteout and the two tarps: three torn shutters; a six-phase whistle rotation survived by moving two tarps; three knocks on
the ceiling trapdoor to end it. 4, the forest of towers: the storm stops, the shutters roll up, the ground is void; seven
towers mirror the reader; lock the fire finder on the still figure in the north tower (bearing 000) and the trapdoor becomes
the way back to the House.

**Assets named.** `lookout_window_pane` (ripple, frost stage 0..3), `canvas_roll_shutter` (open, closed, torn, tarp patched),
`brass_wind_pendulum`, `osborne_fire_finder`, `tty_teleprinter_block`, `trail_warning_lantern` (warning mode); items
`canvas_repair_tarp`, `lookout_protocol_binder`, `diesel_starter_crank`; textures for frost ferns, shutter canvas, the fire
finder dial, TTY tape and the Whistler's bark skin.

**Fit (first reading).** Tight and readable: one room, one rule, and the fire finder gives the reader something to do with
their eyes down. Multiplayer: the cab is shared and the whistle wall is one for all; any reader's gaze failure raises the one
tier, so readers have to manage each other (one on the shutters, one on the fire finder), and in visit 2 one can stay up top
watching the lanterns' twin in the cab while the other cranks the generator. Each reader gets a mirror in visit 4. Literature:
the protocol manual, Sarah's tape (her voice, then the voice wearing hers) and the reader's own fire log give three hands; the
account is the log in the reader's hand with Sarah's last tape pasted in. Win: the north tower locked. Lose: the glass broken
at tier 3 (out, visit again).

## Across the five

- **Overlap.** The God of the Pitt is the existing Ted the Caver vignette's deeper form, not a new room. The others are new.
- **Gaze rules.** Three of the five (Marjorie's cone, Jubilation's centreline, the Whistler's shutters) turn on where the
  reader looks, as do several shipped rooms (Stacey's watched rule, the Masque, the motion-light foyer). Spread them through
  the House so the same rule does not come twice in a row.
- **Four visits each.** All five are four-visit stories. The existing multi-visit machinery (personal visit counts, a beat
  that must be done before the next visit is dealt) carries them.
- **Witness.** Each that is built adds one personal resolution to `WitnessAccount.Story`, and the quota follows from the
  pool (75%, rounded up, two kinds). The kinds: the Left-Right Game survival, the Dead Air Broadcast understanding, the
  Polaroid Trail understanding, the Whistlers survival; the God of the Pitt keeps Ted the Caver's.
- **Suggested order** (owner to confirm): the Whistlers (one building, clearest rules), the Polaroid Trail (reuses the real
  camera work), the Left-Right Game, the Dead Air Broadcast (largest), and the caver's deeper visits alongside.
