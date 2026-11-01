# The Whistlers in the Pines (0.4.202)

The owner's fifth proposal (`docs/OWNER_VIGNETTE_PROPOSALS_2026_10_31.md`, section 5), built first: one building, the
clearest rules. Slot 128, `whistlers`, literary, multi-visit, outdoors. Witness: "The north tower", a survival story; the pool
grows to 57 sources and the quota to 43.

## The place

A fire lookout on a snowy clearing ringed with black spruce (`LookoutTower`): a timber frame six storeys high with a
switchback stair and four landings, a glazed cab on top with a catwalk round it, a corrugated generator shed and a propane
tank at its foot, chest-high snowbanks between the shed and the stair, the reader's own trail door in the treeline behind it.
In the cab: the fire finder (Osborne-style alidade on its ring) in the middle, the desk with the protocol binder (the source)
and the fire log (the account) by the west wall, the radio and teleprinter, the stove, the crank on its hook by the floor
hatch, a pendulum under the roof and a lamp over each window. Every window has a canvas blind on a roller with its cord hung
low on the wall. 59 textures, 56 models and states (`tools/draw_lookout_art.py`); ten wordless cues
(`tools/generate_lookout_audio.py`).

## The rule

Something in the woods whistles from one side of the cab. The pendulum swings toward that wall, its lamp lights and frost
rings on its glass. A reader in the cab who looks toward that wall while its blind is up (or torn, or the glass gone) draws it
in a tier: from the treeline to the shed roof, to the stair frame, to the catwalk glass; a look at the glass breaks it and the
night is over (everyone comes to at the foot of the tower). Looking down is always safe, and every cord can be found with eyes on
the boards. The tier eases back by one after 45 seconds without a look. The Whistler never paths: it is where its tier puts it,
and moves only while nobody is watching where it is or where it goes.

## The nights

1. **Orientation.** Seven's tape welcomes the reader; the reader checks in on the radio, takes the alidade and logs three
   flares by bearing (047, 158, 291, held within 1.5° for half a second). Two whistles, north and west, teach the blinds.
2. **The blizzard outage.** No power, no heat, frost creeping in. The crank from its hook, down the stair to the shed, prime
   and crank the diesel (four turns close together), and back up to a warm cab. On the stair the landing lanterns burn cold when
   it whistles there, and a reader outside the cab must be down behind the rail until they burn again.
3. **The whiteout.** The gale has torn three blinds and there are two tarps. Six whistles round the walls are survived by
   moving the tarps to whichever torn frame it whistles from, eyes down; three knocks on the roof hatch end it. The tape starts
   printing what the reader does.
4. **The forest of towers.** The reader comes to in tower four with nothing under its stilts and eight lookouts round it at cab
   height. In seven of them a figure in the reader's own body does what the reader does; the north one stands still with
   binoculars. Locking the fire finder on it (bearing 000) opens the floor hatch, and the hatch lets the reader down into tower
   four's own cab, where the fire log waits: the account, `locked_the_north_tower`.

## Multiplayer

The tower is one for everyone in it (a party shares a copy; unrelated readers have their own). The whistle wall, the tier,
the blinds, the power and the tarps belong to the tower, and anyone's look raises the one tier. A night's events credit the
readers whose night it is (by their own visit); others keep them company. The crank and tarps stay in the tower: nobody carries
them out. Each reader's figures in the forest are their own.

## Literature

The binder is typed by the service and never names what is outside. Seven's tape is the second hand; on the third night it
prints what the reader does as they do it. The fire log is the reader's own, with Seven's last tape pasted in. The Witness
line, co-op lines, the miniature note and the House of Leaves fact follow. House writing never names the reader's gender, a
colour of theirs or an age.

## Tests

`LookoutTests` (literary): the first night's check-in, three flares logged at their bearings with eyes down through both
whistles; each look raising the tier until the fourth breaks the glass, and a drawn blind making the look safe; the crank and
four turns starting the diesel and the warm cab ending the second night; the torn blinds, a tarp lashed, six whistles survived
and three knocks; and the fourth night's lock on the north tower, the hatch and the fire log. The architecture case exports the
clearing, the cab and the forest of towers, and holds the binder as the source.
