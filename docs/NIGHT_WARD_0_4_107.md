# Night-duty hospital materials — 0.4.107

Extends the Harrigan repair on Claude's `claude/shallows-rework` at `efb5df8`.
Harrigan's dead visits, original identity, funeral poses and custom funeral art remain.

The night-duty hospital (`HOSPITAL`, the ward with the empty incubator and the chart
read at dawn) now has its own material set. Whalestoe keeps its existing hospital art.
Fifteen new pixel sheets in `textures/block/night_ward` cover painted walls, tiled dado,
chequered floor, acoustic ceiling, enamel, brushed steel, white sheets, blue blankets,
pleated curtains, clear glass, light diffusers, call plate, incubator controls and both
halves of the enamel door. `tools/draw_night_ward_assets.py --check` reproduces them.
No additional pre-existing PNG is repainted.

The ward has native fittings using those materials: metal beds and their pillows,
chairs, stools, bedside cabinets and tables. The fitted geometry follows the existing
institute furniture meshes; door and connected-pane geometry remain native. The empty
incubator retains its original collision footprint and location and now resolves its
own enamel, glass, mattress and control face. Thin enamel rails inside the existing
glass volume make its clear case legible. Its controls face the authored approach.

`NightWardMaterials` converts authored material types inside this ward's bounded area.
New rooms receive it after construction and dressing. Saved rooms wait for native block
and entity sections, no nearby reader camera and no living resident. Lecterns, native
containers, source items and the call button are left in place. Door facing, halves,
hinge, open and powered states, and pane connections and waterlogging, are retained.
The saved checkpoint prevents another refit from restoring a removed fitting.
No night timers, calls, chart text, inventory contents or personal resolutions change.

Version 0.4.107 / protocol 81; client and server must use the same candidate. Layout 45,
46 eligible sources, 35 required resolutions across two kinds, three House endings.

## Native evidence and limits

The 0.4.106 attempt at source `1e51df2` passed ten focused/GPU jobs, including the two
older-entity Harrigan regressions in the 42-case suite. Both actual clients rendered
the same original in all three visits and produced all six Harrigan frames. The body
was visible dead in the chair and inside the real casket. The complete live job still
failed: its dedicated host did not finish world saving inside the unchanged 90-second
exit check. It is not a green baseline.

The existing bounded shutdown settling and diagnostic watch, previously restricted
to GameTest, now also covers the explicitly enabled socket expedition. It can drain
pending native chunk generation before vanilla's shutdown unload loop and report any
remaining blocker. Normal games are unaffected. No exit assertion or timeout is removed.
Harrigan's fixture now uses the normal architecture/lighting pass; final native frames
hide the proof HUD, and the casket cameras stand slightly farther back.

The additional native ward regression tests an actual loaded old ward with a camera
and a resident delaying the refit, then checks the original chart and a stored item,
door and call states, the finished night, source credit, resident UUID and saved
checkpoint after a fitting is removed. Total declared native cases: 677. The focused
ward namespace runs with the existing funeral/trailer suite as well as the full corpus.
The first ward test at `27094bf` failed because its untamed cat was rejected by native
spawn rules, leaving no resident to guard. The fixture now uses an owned native cat
and explicitly requires successful registration before checking vacancy; ownership,
health and UUID preservation are also asserted. No resident guard is relaxed.

Three new native GPU views show a staged ward cutaway and its fittings. Every possible
new fixture, door and pane state, and every incubator facing, must resolve custom ward
atlas sprites with nonempty meshes. These are material views, not an uninterrupted
manual night. Existing architecture views and the original dawn/departure story case
remain required, along with all twelve exact-source jobs, all 677 cases, every earlier
client check and both actual reconnects. Expanded candidate validation is pending.
