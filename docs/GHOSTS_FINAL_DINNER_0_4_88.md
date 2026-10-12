# Ghost Set: final dinner, departure and recorded reviews

The October 12 requests are implemented on the home/decor batch: Merry leaves after her family dies; all six speakers vary their conversations across the seven days; every day has different summaries selected from what was actually recorded; Marjorie eats and taunts Merry with a messy mouth; the family deaths include leaking blood and spittle.

## Personal scene sequence

The seventh take lasts 1,560 occupied ticks. Marjorie, John and Sarah eat first, choke at separate clocks (630/660/675), and become still at 780/810/825. Their original native actors remain alive as story entities, in death poses; they cannot answer the player. These poses grant no game death or extra Witness authority. Merry begins leaving at clock 860, after all three are still. She walks around the actual chairs and table, through the ordinary front door at z=-14, and into the arrival passage outside the family home. Native collision remains authoritative: player edits can obstruct her path, and no tick teleports her through a wall. Only an existing native door is opened; no edited block is replaced.

Merry's path cursor and completed departure are saved on the reader's own scene record. Interrupted returns resume it, completed returns leave her outside, and an older already-finished seventh take receives the departure without losing its existing completion. Every family/crew UUID stays the same. Another reader can still be on an earlier day with their own living cast. The seven-day, actual-door-return, original source and own final account requirements remain unchanged.

## Camera-dependent summaries

`GhostsConversations` contains separate subject-specific raw and edited review passages for all seven days. `GhostsSet.edit` selects only tags with at least twenty actual recorded framing ticks in that day's private camera log. Native camera ownership, use, centred aim and real occlusion remain the source of those tags. A shot that was missed or seen too briefly does not receive a summary. An empty day explicitly says no sustained usable shot is available.

The first day contrasts the two sisters' interviews, John and Sarah. Supper can feature the hat, crawl, rusty fittings or injured hand. Upstairs can retain handholds or the threat. The private conversation can be recorded or withheld, with the cardboard house a separate shot. The evaluation can show the patient, her mother or other reactions. The exorcism can retain the drawer mechanism or the held leap. The final meal can show Marjorie's messy mouth and teasing, the sauce-free plate, convulsions, leaking mouths or Merry's departure. Each choice produces that day's own prose and different production omissions. The longest centred family member still affects the lead. The log and edited cut remain distinct native books, with existing finite original snapshots preserved.

All six speakers have two original adaptation conversations per day, saved independently by speaker/day/reader. Dead family members do not speak; surviving Merry's lines change after the deaths and after leaving. Barry's existing native choice menu, the privacy choice and previous filmed-face feedback remain.

## Native animation and art

The existing painted cast atlases are retained without raster alteration. New connected eating, choking and still-death poses move the original elbows, head, jaw and torso. A native render layer adds small sauce smears at Marjorie's mouth, dark wet mouth marks for the dying family, stretching blood/saliva strands and drops falling with quadratic displacement. The attachment follows the actual head, while the streams remain vertical as the head falls forward. These are coloured native meshes using the Minecraft white-concrete material, not a replacement NPC skin or spawned collectible items. Fourteen jointed poses are now supported.

A fourth required GPU asset view, `native-ghosts-final-dinner.png`, renders the actual layer on all three poisoned family members at several animation ages. This proof is queued through normal CI; no local GPU inspection is claimed.

## Source relationship

All dialogue here is original adaptation prose, not purported quotations. The poisoning, Marjorie's decision to eat the sauce and Merry's plain plate were checked against the final-chapter summary at https://www.supersummary.com/a-head-full-of-ghosts/part-3-chapters-23-26-summary/ and the author's premise at https://www.paultremblay.net/head-full-of-ghosts . The visible immediate front-door departure and the camera crew's final pickup are the owner's requested game staging, rather than claims that the novel documents such footage. Ambiguity about possession, illness, memory and editorial manipulation remains.

## Verification

Mod 0.4.88 / protocol 65 / layout 43. 577 declared native cases, 721 decoded PNGs, 91 architecture views. 44 sources, 33 required resolutions across two kinds, three endings.

Compile and PNG/CRC checks pass. All twenty focused Ghost cases pass in 33 seconds (15.87 seconds of native case execution). The three new cases compare different captured subjects in both reviews on every day, verify all six speakers' day-specific private conversations, and run the original eating/choking/death family through Merry's physical front-door departure and a SavedData interruption/reload. Full exact-source CI and native GPU inspection remain pending.
