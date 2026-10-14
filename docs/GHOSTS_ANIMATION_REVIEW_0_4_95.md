# Ghosts Set texture and animation review — 0.4.95 candidate

Requested October 14: review the complete set, repair stuttering and day six's banister jump, and fix Marjorie's blinking/vomiting texture glitches. This candidate retains the Goatman windows, three circuits, louder positional impacts and original quiet score from 0.4.94, with vanilla music suppression. The native sound proof now waits for sample resolution before checking its gain.

## Confirmed causes and changes

| Area | Cause | Change |
| --- | --- | --- |
| Cast locomotion, climbing and tracking | Five-tick script calls moved native bodies in coarse steps. | The script selects targets; collision movement, accelerated falls and bounded turning run every native entity tick. |
| Pose changes | Whole jointed poses switched immediately. | Per-original pose snapshots blend over eight ticks, or five for the leap. Connected elbows/knees remain intact. |
| Blink | Thin lid cuboids sampled the hair/forehead island and appeared for three frames. | Front-only lids smoothly close over six ticks using the atlas's existing clean cheek skin; a narrow crease appears at closure. |
| Vomiting/chewing jaw | The cuboid's upper jaw face repeated the forehead and eyes when it opened. | Explicit outer skin faces preserve the painted net; upper/lower inner surfaces sample darkened reserved skin. |
| Marjorie dinner skin | Approved image's painted net has an 18-pixel UV step, larger than the normal atlas's 1774/128 step. | Fit vertex UVs to the actual dinner artwork. No PNG is resized, redrawn or replaced. |
| Day-six jump | The old beat raised Marjorie beside her bed, without clearing a guard. | The original walks off the bed, through its actual doorway to a balcony over the open foyer, and clears a native guard through six physical leap steps. Camera obstruction delays the leap. |
| Frozen lens | A new camera marker interpolated from its default position; camera payload could precede the actual actor update. | Initialize previous position/rotation, and retry handoff until the original owned client actor is in LEAP at the authoritative apex. Hold animation age while only the original head turns. Restore on exit, death, lost lease or world change. |
| Drawer/cardboard | Discrete prop phases jumped instantly between visual shapes. | Ease extension/folding/collapse over eight native ticks; keep private authoritative prop phases and collision. |
| Prayer/preview scales | Prayer hands pointed in opposing directions; Marjorie's cast preview used adult scale. | Both hands meet at the chest, seated Merry keeps folded knees, and the native proof uses the actual .86/.58 cast scales. |

The authored balcony guard uses the existing rail material/model. Old sets receive only previously empty guard cells, once, after affected native sections load and cameras/living bodies leave. Checkpointed edits/removals stay removed. No cast, container, inventory, finite supply or existing completed account is recreated.

## Examination coverage and evidence to inspect

All seven script days were traced: interviews; dinner/blinking/jaw/crawl/prayer/hand; wall climbing/help/fall/impact; basement privacy/stairs/playhouse; bedroom evaluation; drawer/bed/physical leap/freeze/head/cut; final spaghetti/eating/differentiated choking/deaths/crew and Merry departures. The plates already use authored three-dimensional spaghetti on days two and seven; Merry keeps her dry spaghetti. Retain those assets.

The native GPU review expands from six to sixteen captures: all eighteen poses from front/side/back; all eight approved cast atlases; six blink/jaw samples for each Marjorie skin; helping/falling/death/mouth effects; all four directions of every fixture; opening/collapsing props; existing cases, fitted call sheets, caption and four assembled dining chairs. The established upstairs/basement architectural captures remain required.

Five movement-heavy Ghosts cases now wait for real server ticks rather than repeatedly draining the script inside one tick. They retain or strengthen original identity, loaded body, room/start, obstruction, privacy, real collision, footage, save/reload, seven-day, poisoning/crew-exit, saved-route and no-premature-ending assertions. The declared complete corpus remains 622 cases.

The original two-client/two-reconnect expedition gains a targeted day-six fixture after its 51 prior phases. It explicitly sets the already-read filming day and initial reader positions. From there native client walking/item-use packets, production original-body motion/leap, leased camera, head turn and forced saved return are checked and captured independently for both readers. This does not establish an ordinary seven-day manual playthrough. The prior catalog review's other manual gaps remain as recorded in `VIGNETTE_EXAMINATION_0_4_93.md`.

## Validation status

Candidate pending: source review and PNG decode/CRC checks precede the exact-source CI run. Require all twelve jobs, the full 622 native cases, existing 94 architectural cutaways, new sixteen Ghosts frames, complete GPU/resource/font checks, actual two-client expedition and matching packaged JAR integrity before marking this candidate verified. A staged native fixture and an ordinary manual expedition are distinct evidence.

Earlier 0.4.94 source cf9652e passed the build/full native/GPU job and ten other jobs, while live-multiplayer failed because the proof tried to read an unresolved sound sample. That run is not a complete green verification. Production sound resources, gain and positional assertions remain required in the resolved-source rerun.
