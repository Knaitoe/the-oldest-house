# Stanley Hotel: first interaction and animation pass

Candidate 0.4.86 builds on the 0.4.85 camera-crew vignette. Layout 42; protocol 58 because the existing hotel actors now synchronize bounded interaction gestures. The hotel remains one recurring Witness source: 44 sources, 33 required personal resolutions across two kinds, three endings.

The existing large connected hotel is retained: reception, dining room, bar, ballroom, Room 217, caretaker quarters, office, linen room, workshop, boiler basement and the snowbound maze/cemetery. This first pass develops its materials, cast motion and furnishings. It does not add another hotel or change the existing guest's personal closing requirements.

## Usable furnishings

| Item or surface | Native action |
|---|---|
| Reception bell | Point at the raised bell and use it to ring; the original porter acknowledges it. |
| Reception desk | Use the desk to read the personal account; sneak-use to retrieve exact belongings from lost property. |
| Both pianos | Use to play the original four-second cue, with a personal cooldown. |
| Bartender | Speak to receive a native drink, with a synchronized pour. Drinking in the bar enters the tab. |
| Dining chair and meal | Sit to be served; use the actual meal to eat. |
| Room 217 bed | Use the actual bed to rest; later rests store the guest's main pockets in private drawers. |
| Drawers and linen chests | Open native inventories; retained belongings, components and quantities remain recoverable. |
| Typewriter, caretaker log and housekeeping paper | Read and collect the existing finite original manuscripts. |
| Gauge and boiler | Examine the actual pressure/cold state. A missing player-removed gauge remains missing. |
| Vent and restart controls | Operate the existing heating mechanics; inspection alone does not count as maintenance. |
| Hose rack | Inspect whether the hose is still wound or has left the landing. |
| Party photograph | Examine the personal print; the native player profile still fills the front-row place. |
| Maze key and service door | Find the finite key and unlock the actual service door. |
| Cemetery stones | Read the ledger of actual named lost companions. |

The existing native interaction shapes now turn with the meshes. Selection includes the raised reception bell, tall typewriter paper, piano keyboard, boiler and large photograph. Actual ray tests cover all four bell orientations. Canceled interactions and native spectators are excluded; bells and piano cues cannot stack under held use, incur a meal charge or confer story credit.

## Cast and motion

Six new 512 × 256 atlases fit the existing 128 × 64 logical UV layout. Separate elbow and knee joints replace rigid limbs, and the pianist has six small finger joints. The porter bows on acknowledgment; the bartender pours from a modeled bottle into a glass, then returns to polishing; the pianist works both hands; guests incline toward the listener; coat tails and the dance dress follow restrained steps.

Three dance pairs take real native collision-respecting steps on the parquet, turn toward their partners and retain their actor UUIDs. Existing dancers are assigned their partner slot on the same bodies. The pianist's original body walks the short distance to the keyboard if it was left too far away in a saved hotel. Interaction gestures have bounded saved start/end times and expire normally; they do not replace inventory, account, actor or progression state.

`tools/generate_stanley_assets.py` produces the six fitted skins and seventeen original materials: burgundy patterned carpet, floral paper, walnut paneling, coffer plaster, varnished wood, aged brass/steel/rubber, paper, piano keys, food, the floor patch, the sepia party image and four pressure faces. Existing native fixture and keepsake meshes use these new sibling texture resources. The previous assets remain available; saved geometry and original block entities are not rebuilt.

## Validation

All nine focused native hotel cases pass, including the existing full hotel arc/custody/heating/history checks and the new real-hit/gesture/item and paired-dance cases. All 691 native PNGs decode with valid chunk CRCs; the eight writing grids pass structural checks. The complete suite now declares 558 native cases. The candidate still requires all twelve exact-source CI jobs, 90 architecture views, all prior client/socket/reconnect checks, and the three new native Stanley cast/action/furnishing views. `-PstanleySmoke` renders those registered assets separately so this first pass can be reviewed while the complete suite runs. It is not yet a verified release.
