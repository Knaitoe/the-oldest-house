# A Deer in the Road, the first slice (0.4.310)

The owner's design "A Deer in the Road: An Understanding of the Bent-Neck Woman" (Drive), built as one ONE_SHOT literary vignette.
`LabyrinthPlace.DEER_ROAD`, slot 187, outside, built whole, shared (one wreck for the party), layout 96. Dealt only to a reader who
has resolved the tunnel (Under the green lights); a reader she catches is not dealt it again. Witness: one new understanding source,
75 / 57 / 52.

## The road (`DeerRoadSite`)
A county road at midnight in rain (client-only rain and close fog, `DeerRoadClient`; scene clock 9). It bends west into the trees at
the south end; the service door (the entry, a steel door in a pump house's end wall) stands on the outside of the bend. East of the
road: gravel shoulder, the drainage ditch running high as far as the culvert (grate in its west bank, marker post beside it), damp
past it to the hinged drainage gate in its headwall (the scene's way on, `OnwardPlacements`). Brush on the east bank hides where the
doe comes out. Behind where the car stops, the shoulder is hatched and the county's notice stands on its post (the first paper).

## The shared state (`DeerRoadScene`, on an anchor under the asphalt) and the story (`DeerRoad`)
- **The drive.** The first reader through the door is put behind the wheel of the idling sedan and it moves; a second takes the
  passenger seat; others arrive in the headlight spill. No dismount while moving.
- **The beat.** When the doe steps out the driver's first input decides: back = brake (lands square, stops short), strafe = swerve
  across the line or onto the shoulder. The strike happens either way; a straight strike springs the driver's door, a swerve the
  passenger's. The car's real collision cells (`DeerCarCells`, clipped from `DeerCarMesh`) are placed where it stops.
- **The hush.** Hazards tick, the fan winds down, the belly moves in little starts.
- **First birth.** The woman (LiteraryActor `ROAD_WOMAN`, the tunnel's bent body and nightgown, shared) comes out from under the doe:
  hands first on the centre line, shoulders, head on its neck, the brace, the rise (`DeerRoadPoses`; the road surface hides what has
  not come out yet). She makes for the driver's door with the tunnel's stride and dies against it; struck four times while moving,
  she goes down where she is. The body stays; against the door it jams it (shut or open).
- **Second birth.** After the silence the rear belt winds tight a tooth at a time, the bench pushes up and splits at the seam, and she
  comes out of the back seat (opening the rear door if shut) with the first body still there. She hunts the nearest reader she can
  see (a crouched reader in the ditch is hidden past a few steps), paths round the car and its open doors, catches on touch.
- **The third, suggested once.** After a second death, the first reader past the culvert hears two scrapes and sees fingers through
  the grate.
- **The next vehicle.** Comes up from the bend. Without a triangle it goes by fast. With the triangle (from the boot, set on the road
  behind the car) it slows; flagged from the hatched shoulder it stops alongside for ten seconds with its lights on her, and she
  stands in them: the clear moment for the service door.
- **Off the road.** Service door (entry) or drainage gate (way on). A reader who saw the first birth resolves the story and carries
  the account out; leaving earlier says so and resolves nothing. Caught: blackout and the failed return.
- **Reset.** With nobody on the road and no open visit pinning it (offline members hold it for a day), it is set back fresh.

## What each reader keeps
Per reader, from where they stood: the hand, the rise, the death and where, blows, the triangle, the reel, the bench birth seen or
only heard (and from the ditch), watching the first body instead, both bodies in one look, the culvert, the stopped vehicle,
flagged, the exit. `DeerRoadTexts.account` writes only those facts; the owner's closing line is for a reader who saw both bodies.
Witness line, co-op line (`CoopAccounts`), miniature note, House of Leaves place name and advancement added.

## Art and sound
`tools/draw_deer_road_art.py` (checked in CI): car materials, the doe, the next vehicle, the roadside fittings, the gate, the
triangle. Sounds are vanilla and existing House cues (yard engine and traffic, the Backseat's impact and latch); a generated cue set
is a follow-up.

## Tests
`DeerRoadTests` (literary): the whole road with a braking driver through both births, four blows, the culvert and the ditch exit with
the account checked; and a reader caught by the second woman. Architecture, advancement, co-op and Witness-pool cases updated.

## Not yet verified
No live client pass has been made on this slice: the car, doe and the woman's emergence poses are drawn but unreviewed on screen.
Next: a client playtest of the drive and both births, generated audio, the co-op passenger beat, the tuning of the hold.

## First CI repair
Run 42117835973 on f39821b compiled and packaged successfully, then stopped in `generate_advancements.py --check` with
`KeyError: 'deer_road'`: the checked-in advancement had not been added to the generator's icon table. Register the warning
triangle there and regenerate the advancement text from the Witness account so the resources and their generator agree.
The art generator also asserted a different advancement description; it now leaves advancement text to the canonical generator.
The complete asset gate and journey-measurement unit tests pass locally.
Full exact-commit CI and the live client review remain pending.
