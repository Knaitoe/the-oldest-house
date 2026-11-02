# Scenes looked at in play, and the owner's notes of 2 November (0.4.240)

The owner, 2 November: "Review the scenes with actors and normal lighting present. For the pool, watch from the strap while the
attendant crosses to the exit. For Vacant Possession, carry furniture down the stairs and watch the final chair decision. Those views
will expose obstructed movement, missed gestures and awkward sightlines that the cutaways conceal." Then, through the day: a more
haunting title splash; screenshots of the dust hall, the nightclub, the tunnel and the plain. No protocol, layout, Witness or
source-kind change.

## The pool and the flat, in play

`SceneReviewSocketProof` and `SceneReviewSocketClient` (`tools/run_live_expedition.py --scenes-only`, gradle property
`sceneReviewSocketSmoke`): two real clients on a dedicated server, in the production pool and flat, with their actors and the
world's light. Staging only: both scenes are built at their places and the readers set down in them; the flat's first seven pieces
go down to the van on the server, so the run starts with the dresser. After that the clients haul the cover with real clicks,
carry the dresser down the real stair with real movement, go back up to the tenant and carry the last chair down.

What the views showed, and what changed:

- **The flat's stair ended a tread short.** It had four treads for a five-block drop, so a reader walking down stepped off the last
  one into the hall wall's corner. It now has five, z -1 to -5 (`VacantSite`). The cutaways never showed it; carrying the dresser
  down found it at once.
- **The tenant's window could not be seen from the street.** The living room's window is straight over the van: from the pavement
  only by looking up, from across the street not at all, so the closing beat ("You look up at the window. It's dark.") happened out
  of sight. The living room has a second window past the van's end (x 8, z -11), lit while the tenant is home and dark after.
- **The front door's open leaf narrows the hall's doorway** to 13 sixteenths of a block. A reader carrying straight down the middle
  of the hall can catch it; a step to the side clears it. Left as it is: it is a door. (The client walker now corrects sideways
  drift before it reaches the door.)
- **The stair runs between walls**, so from the hall the carry shows only as the reader comes out at its foot. That is the house;
  the hall view watches the stair's foot.
- **The pool** reads as intended from both places: from the strap the attendant is small at the far end, crossing to the fire door
  under the remaining bank of lights; from the west deck they pass close. Nothing obstructs the walk.

## The title splash

`HouseSplash` replaces the vanilla splash (`TitleSplashMixin`): the mod's name, where and at the angle the splash goes, but in ash
rather than yellow and set the way the book sets its pages. "house" in its blue; "has" with "had" struck through in red above it,
and now and then, for a moment, the correction fails and the line reads "had"; the letters of "ghosts" drift and fade each on its
own; "in it" goes away letter by letter, smaller and fainter; the whole line again backwards and faint beneath, as if read through
the page; the light it is drawn in fails for a quarter of a second here and there. `TitleSplashProof` (`-PsplashSmoke`) saves a held
and a slipped frame.

## Faces that took their pixels from beside their texture

The owner, on the dust hall: "the blocks here might need to have their pixel maps adjusted." A model face with no `uv` of its own
takes its coordinates from where its element sits in the block. An element reaching above the block (a chair's back) or below it
gets coordinates off its texture, and the face shows whatever picture sits next to it in the atlas: the yellow tip on the dust
hall's chair, the green and blue "screen" on the plain's note stand. 125 of the mod's models had such faces. `ModelUvRangeMixin`
moves automatic coordinates back onto their own texture, one pixel to one pixel, wherever they fall off it (a span wider than the
texture is fitted to it); nothing with explicit coordinates changes. `ModelUvProof` (`-PuvSmoke`) draws the dust hall's chair and
covered furniture, the plain's stand, a dining chair and the trailer's chair.

## Particles

"missing particles for some textures...": forty block models (thirty-nine of the Ghosts Set's fittings and the frozen block) named
no particle texture, so hitting or breaking one showed the missing-texture checkerboard. Each now names one of its own textures
(the frozen block, ice). `tools/check_model_particles.py`, in the quick gate, resolves every block model through its parents,
vanilla's included, and fails if any names none.

## The dust hall

The sheet over a tall piece of furniture was two separate covered blocks, each with its own lid. Sheets now have three stages
(`TraceRooms.sheetStage`): one piece (0), a tall piece's lower block (1, full height, no lid) and its upper block (2), meeting as one
cloth; uneven folds to a dusty hem, a creased top (`sheet_top`), and a hem that flares a quarter pixel at the floor. The chair's back
posts and rail are mapped onto its texture in the model too (`tools/draw_trace_fittings.py`).

## The plain

- **The note stand** had two legs on a diagonal, which read as one leg short; it has four (`tools/generate_finishing_0488_models.py`,
  `VignetteDetailBlock` shape).
- **No clutter.** The "survey rest" by the door (two smooth sandstone blocks with a satchel, tools and a rope coil) is gone
  (`VignetteArchitecture.plain`).
- **Trees that belong there.** The border's spruces and its wall of spruce needles are replaced, on the plain and any dry ground
  (sandstone, red sand), by sparse flat-topped acacias, dead brush and a stepped dune ridge at the far edge (`ScenePolish.acacia`).
  A lake's sand beach keeps its pine woods.

## The bent-necked woman

"bent necked woman doesnt properly reset once players exit the vignette." She was put back only when her reader came in again, and
never once they had passed: a reader who left mid-chase, or came back after getting through, found her standing wherever the chase
had left her. Now, whenever her reader is not in the tunnel, she goes back to her place at the far end, folded and facing down it
(`TunnelStride.home`, her place kept on her body); and whenever a reader comes back after time away she is folded there again, a
passage made or not (`UncannyStories.reenter`; only the reader's progress is kept once they are through).

## The nightclub

- **One EXIT sign.** A finishing pass hung a second sign above the one the club is built with; it no longer does.
- **The club's music on its own.** Inside the club the game's own music, weather and ambience (the cave moods the dark brings on) are
  not heard (`NightclubScore.sounds`). A reader can still switch the music off at the desk; after forty-five seconds of silence the
  DJ reaches across and the bass comes back (`Nightclub.resume`).
- **Dismay that spreads.** Each dancer notices the silence in their own time, one to six seconds in (the DJ at once), then loses
  the dance over six more: the beat runs down out of their bodies, their heads go to the booth at the back, then their arms drop and
  they droop. When the bass comes back they find it again, quicker, each a beat apart (`NightclubActor.notice`).
- **Faces between.** A third face for every dancer and the DJ, between dancing and dismay: brows lifted, eyes gone to one side, the
  mouth drawn in small (`nightclub_*_unsure`, painted by `tools/paint_character_skins.py`). Dancing below a fifth of the way, unsure
  to three fifths, dismayed after (`NightclubRenderers.face`).

## Run 1395

- The build job's writing check asked Borrasca for four papers that 0.4.235 removed; it now takes the trail sign, the trailhead
  note, the map, the receipt (with and without the compass), the night book and the letter.
- The handling case read the workshop's spare kit in the same tick as its refit, which places its blocks in slices; on a slow
  runner the kit came a few ticks later. It now waits for it.

## Tests

`UncannyBranchTests#theBentNeckedWomanGoesHomeWhenHerReaderLeavesAndWhenTheyComeBack`;
`NightclubTests#...` (the cast case) checks that dismay spreads dancer by dancer and comes back over a moment;
`VignetteHandlingTests` waits for the refit. Nightclub 2/2, uncanny 59/59, the 0.4.88 namespace 12/12, handling 3/3 and the Vacant
Possession cases pass locally.
