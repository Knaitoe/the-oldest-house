# Owner notes: 0.4.201

Notes from the owner on October 31: the bathroom mirror, the quiet rooms and their last card, then a round of fixes across the rooms.

## Everyone wears the House's body inside the House

> "the skin layering over human minecraft skins is more than a little faulty. if you can't fix that confidently, just give all players a house skin inside the house."

Until now only a reader whose own skin did not look human wore the House's painted body (0.4.163). A reader with a human skin kept it, and every client recoloured it to the tone drawn at the kitchen table (0.4.195). That client also refitted its arms to the body drawn there and painted the drawn face over its own (0.4.196). The recolouring moved every pixel near the skin's commonest lower-face colour to the drawn tone and left the rest alone. A skin's shading, lips, blush and hat-layer pixels fall outside any such match, so they kept the old tone. The owner's screenshot shows the result: a face patched in two tones under a hair style.

No rule for recolouring an arbitrary skin can be trusted, so that path is gone. Every reader inside the House now wears the painted body:

- **Who and where.** Every reader, beyond the manor and the rooms between (`HouseBodies.inside`), while the server option is on. Their own skin returns in the manor and outside, as before.
- **Which body.** Unchanged from 0.4.163 and 0.4.196. The read picks the age, the clothes (from a settled presentation, or the body drawn at the kitchen table) and the hair. The tone, hair colour and face drawn at the kitchen table colour it. Until the reader has drawn them, the body uses the tone and hair colour of the default skin Minecraft gives their account.
- **What is gone.** The client's check of its own face (the `house_body_report` packet and the face test), the `OWN` stand-in body and the recolouring, arm refitting and face painting of real skins. No reader's own skin is read or changed now. Protocol 109.
- **The hair styles** from the bathroom tools are drawn on top of the body as before. They take their colour from its crown, which is the hair colour the body was painted in.
- **The server option** `houseBodies` keeps its name; its comment now describes the body worn by everyone. Off, every reader keeps their own skin everywhere.

Tests: `HouseReadReaderTests.everyReaderWearsTheBodyTheHouseReadInsideTheHouseOnly` replaces the inhuman-skin case. Two readers, one read and one unread, both wear bodies inside the House and both are sent to every client. A reader who leaves keeps their own skin and everyone is told. The server option turns both off. A drawn tone colours the body. The kitchen case checks the drawn body and face on the worn body. The tailoring proof requires both connected readers to be drawn in the House's body. The kitchen proof's skin page shows painted bodies in an account's colours and in drawn tones with each face.

## The bathroom's opening line

> "you can just have the text read 'your hands knew what to do' rather than 'your hands still knew' - the still is redundant"

"Some nights you came home late and worn through, and your hands knew what to do before bed."

## The supper call is text

> "just remove the voice lines for now from the house-entry - the wash your hands before you come in - and add text instead. it still sounds inhuman"

The back steps' call in for supper was a synthesized voice. The voice and its sound are removed. When it would have played, 26 ticks after the whistle, the reader sees the words on the action bar, quoted, grey and italic: "Wash your hands before you sit down, alright? It's getting cold." Only that reader sees them. `tools/generate_house_read_audio.py` no longer makes the voice; its manifest lists seven cues. The television's murmur through the kitchen wall is still a synthesized voice, mixed down until no words carry. It is unchanged.

## The last card

> "Every house is haunted. Old arguments and worse habits keep these walls standing. A word of caution before the door closes: This House Has Ghosts in It"

The owner's words replace "Every house is a haunted house. / But a warning, before you proceed: / this house has ghosts in it." The card keeps its three pages: the first two sentences, then the caution, then the title. The story test still forbids age words in the House's lines; "Old arguments" is allowed by name, since it ages the arguments and not anyone in the house.

## The journal goes on the hotbar

> "try to place the journal in the first open slot of the player's hotbar if you can, rather than the first open space in their inventory. so they're quicker to recognize its addition"

A journal handed over (the first one for a reader who missed the letter, or a lost one coming back) goes in the first empty hotbar slot other than the one in hand. If the slot in hand is the only empty one, it goes there. With a full hotbar it goes in the pack, as before, and at the reader's feet only when everything is full. `JournalTests.theJournalIsItsOwnersAndAnOldCopyGoesBlank` checks the slot past the hand, the hand's slot when it is the only open one, and the pack after a full hotbar.

## Tearing a page shows how

> "did you finish your work on the journal? I'm not seeing the ability to tear a page free"

Tearing has worked since 0.4.199: hold right-click on a page with writing on it for about a second, the corner lifts and the page comes away; a right-click on the stub takes it back. Nothing on the screen said so. Now a written page's bottom outer corner curls up a little while the pointer is over the page, and on the corner itself a tooltip says "Hold right-click to tear it out". The inside cover and blank pages show neither, since they cannot be torn. The journal's visual proof is unchanged (the hint is off in proof mode).

## Not in the phone's shot

> "can you try to ensure the player can't see themselves in lake_canoe during the camera fall sequence?"

When the phone goes over the side in Phone in the Canoe, the view becomes the falling phone's. Until now the game drew the reader as it draws anyone the camera can see, so their body, the name over it and the first-person hand all showed in the shot. While the view is the phone's, `LakePhoneClient` now cancels the player renderer for the reader's own body, which also drops the name, and cancels the hand. Other readers in the scene are drawn as before. The body's shadow on the canoe is not suppressed; at night it barely shows.

## The lake house culvert moves

> "for the lake house, the movement through the hand-push corridor is dreadfully slow and awkward"

Under the culvert's roof a stroke could start only every sixteen ticks, each added .13 to the canoe's speed, and the canoe lost a further 6% a tick on top of the water's own drag, so it crawled about a block a second: the eighteen-block channel took about nineteen seconds of holding forward. Each stroke also set the hull .085 off its wall, so in the two-wide channel it knocked across into the other.

`LakeHouseCanoe` now:

- **strokes faster hand over hand.** A stroke's arm lasts ten ticks; the other hand may follow after eight, the same hand once its stroke is done. Holding forward still alternates hands by itself; left or right still uses that hand, and a one-armed reader still uses theirs.
- **glides between strokes.** Each stroke adds .15 (to at most .3), and the culvert's extra drag is 3% a tick. Hand over hand it averages about 2.6 blocks a second (one hand about 2.1): the channel in about seven seconds.
- **keeps to the middle.** A stroke sets the hull only .03 off its wall, and under the roof the canoe loses most of its drift across the channel each tick and eases toward the point between the two walls.

The lake house namespace's sixteen cases pass, the stroke case included (an immediate second stroke is still refused; the same hand pushes again after a stroke; a back stroke runs back out).

## The tunnel: a stoplight in the middle, and no fresh start

> "stoplight spawned in middle of vignette? The vignette also doesnt reset upon re-entry"

**The stoplight.** The tunnel's stoplight and bin belong against its far wall, at z -144. Two saved-world refits were still running on new worlds: `UncannyEnvironmentRepairs` and `UncannyPlaytestRepairs` compared each block with older plans of the rooms and wrote in their target plan wherever a block still matched. For the tunnel that target was the 0.4.132 plan, when the tunnel ended at z -73 with its stoplight at (0, 1, -72) and bin at (2, 0, -71). In the long tunnel those cells are open corridor, which matched the older plans' air, so the refits put the old stoplight and bin down in the middle of it. Both refits are removed, with the tunnel's own lengthening refit in `TunnelPassage` and the four old room plans (`UncannyRooms0121`, `0122`, `0129`, `0132`) that only they used, as the standing rule asks of any migration that is touched. Their tests go with them; the fresh-build checks in those cases stay, including the case that exports the eleven room views. They never ran in the test server, which is why CI never saw them.

**Coming back.** A reader caught by the woman was marked failed and left inside the tunnel; coming back in found it as they had left it, the woman standing where she had caught them and the signals mid-cycle. Now, coming back into the tunnel after more than three seconds away starts it over: the signals from the beginning, the same woman folded at the far end, and the catch forgotten. A passage already made (past the last green, or read) is kept. Being caught leaves the reader in the tunnel, so it starts over only once they have left and come back. `UncannyTests.comingBackIntoTheTunnelStartsItOverButKeepsAPassageMade` covers staying, coming back and a passage kept; the uncanny and foyer namespaces pass (59) and the uncanny environment case passes.

## No gap at the door

> "Try to clear the gap between vignette boundaries and doors."

Four of the uncanny rooms (the tunnel, the split-feed bedroom, the basement and the buzzer foyer) stand their front wall at z = -1, a block inside the doorway plane. Only the doorway column was joined to the hall's door, so the cells beside it were a one-block slot between the hall's wall and the room's, with no floor: a reader in the doorway looked sideways into the void. `UncannyRooms.closeSlot` now fills that slot with the room's own front wall and floor, block for block, and leaves only the doorway open. The diner keeps its forecourt as it was.

`ArchitectureTests` now checks every built scene whose entry door stands at (0, 0, 1): no cell of the threshold row is a hole beside a solid floor, and the doorway itself always has a floor. Before the fix it listed the four rooms; with it the architecture namespace passes (5).

## Proofrock's car windows

> "Fix the car windows for drowned_town."

The cabin of each parked car was four blocks of one model that drew glass only on its north face, so every car had one strip of windshield turned a different way per block and solid sides elsewhere. The cabin model is redrawn (`tools/generate_playtest_fixes_assets.py cabin`): a roof, one corner pillar, and tinted glass you can see through running from that pillar along the outer front and side to the block's edge, plus a seat inside. Each of the four cabin blocks is turned so the pillar is the car's own corner (`ProofrockTown.cabinCorner`), so the panes meet as one windshield, rear window and side windows. The cabin no longer culls the faces of the body under it. New textures `car_glass`, `car_trim` and `car_seat`; the fixture block keeps its 256 states.

## Stacey comes up the sand at Costume Night

> "Stacey graves still isn't wanting to cross to land."

Her steps up from the water work (0.4.164). What kept her in the lake was the route: after the lights go out she goes for the nearest reader on the sand, and a reader standing where she may not go (within ten blocks of the entrance, up on the stage, among the props) left her with no route at all, so she waited where she was. She now heads for the open ground nearest that reader, out to twelve blocks, and waits at the edge of what is hers. The Costume Night case adds a reader by the entrance and checks that she comes out of the lake and up the sand to the edge nearest them, and reaches nobody; the literary namespace passes (88).

## The diner: the restroom, the griddle, her words and her change

> "please fix the bathroom in second waitress vignette. no see-through door in a diner lol... no "bathroom" texture that bleeds into the diner decal. it should be appropriately grungy"
> "also messed up textures in second waitress"
> "the second waitress should eventually transform after following the player"

- **The restroom** no longer borrows the diner's east wall, where its tiles showed on the diner side and covered a window. The diner keeps its own wall, dado and windows; a solid steel door in it (`uncanny_restroom_door`, painted, scuffed, a kick plate, a push plate and a small plaque, no glass) opens into an annex whose outside is the diner's own finish under the diner's roof and whose inside is a tiled lining three blocks by five. The tiles are yellowed and grimy, with dirty grout, a crack, a chipped tile and a rust run; the floor's small tiles have gone dark at the joints, with a stain in one corner (`tools/draw_vignette_details.py`). The fittings keep their kinds and controls at new places (`DinerDetails.TOILET`, `BASIN`, `SOAP`, `DRYER`).
- **The griddle**'s black cooking plate shared the steel top's surface and the two flickered through each other in streaks. The plate now sits half a pixel down inside a steel rim.
- **Her words** went to chat; they are on the action bar now, as the rule for speech asks.
- **She changes after following.** Once the plate is served, a reader who gets up is followed. Twenty seconds of being followed, counted across every time they get up (`UncannyDiner.FOLLOWED_UNTIL_SHE_CHANGES`), and she changes: her mouth splits, she says "Sit down.", and, as when the plate is eaten or the night runs on, she comes for them fourteen seconds later. The front door and the staff door are still ways out. `ServiceFollowupTests.herOriginalBodyChangesAfterFollowingAReaderAwayFromTheTable` checks that she follows, still a waitress, and then changes without anything eaten, the same body and no catch yet. The service, uncanny and uncanny environment namespaces pass (66).

## Cardboard boxes touch

> "try to have the various instances of cardboard boxes actually touching rather than floating in space"

The moving boxes (the Wheel's first flat) were drawn a pixel inside their cell on every side and three short of the top, so boxes side by side had gaps and a box stacked on another floated over it. Each now fills its cell; the open box's walls run to the cell's edge. The Barrett house's pile of boxes has its lower box full width with the upper sitting on its lid. Their collision follows.

## Torn pages go back to the pack

The build job of the 0.4.201 run failed `tornPagesPinAndOnlySixAreLeftOutInTheHouseBeyondTheManor`: torn pages were handed over by the same code as the journal, so the hotbar change above put the ninth page into the hand's slot, where the test then put another page. The owner asked for the journal on the hotbar; a torn page now goes where any picked-up item would, as before. The case passes on its own; its message now says what failed.

## The Ghosts set: banister, photographs, clutter

> "the banisters in ghosts set are still disconnected... did you touch that at all?"
> "textures are also still messed up." / "family photos on windows? a lot of the furniture looks more like clutter than actual living space."

- **The banister.** The 0.4.199 change joined the top of the stair rail to the landing rail, but the rail pieces stood in the column beside the treads, over the open foyer: every post hung in the air, with its foot bar pointing away from the stair. The banister now stands in the treads' own open-edge column (`GhostsSetRooms.STAIR_RAIL_X`). Each piece has a baluster on the tread's low half and one on its high half, and the handrail rises a block a block, so the pieces meet. At the top it levels at the landing banister's height, runs onto the landing, stops at a newel and turns into the landing banister, which now starts at that newel (four pieces, x 0 to 3). Collision follows the mesh (`tools/generate_ghosts_stairs.py`). The proof's staircase view was redrawn and checked.
- **The photographs on the dresser and on the wall.** The Barrett models' generator dropped a box's explicit texture coordinates whenever a face took another texture, so the dresser's framed photograph sampled the photo sheet's transparent margin and showed only its black edges, and the wall photographs sat a row off. Overridden faces now keep their coordinates (`tools/draw_barrett_home.py`); the dresser, photographs and sofa seat models change.
- **Photographs on a window.** The family corner's photographs hung on the side window's blind; they are on plain wall now. `GhostsSetTests` checks that no photograph, poster or calendar in the house hangs over a window.
- **Clutter.** The laundry baskets, clothes heaps and boxes left in the hall, on the landing and in the bedrooms are gone. The utility room keeps its basket, Marjorie's room its clothes on the floor, and the den the crew's gear. The ghosts namespace passes (22).

## The vault can be reached

> "redesign the architecture of usher home to be more grandiose, as intended. the player cannot currently reach the basement."

The stair to the vault ended under the hall floor: the floor over the landing began right at the last step, so a reader stepping off it hit their head and stayed on the step. The opening in the hall floor now runs one cell past the last step (its rail moves with it). The Usher case checks the headroom over every step, the three cells a reader passes through stepping onto the landing, and the landing to the vault door. The grander rebuild is the next piece of work.

## Not changed yet

- The coatroom's closing line, "That's how you'll look here.", waits on the owner's choice among the suggestions given in conversation.
- The Wheel's mother dropping fire charges: nothing in the mod gives, drops or names a fire charge, and her actor cannot be hurt, so where they came from is not yet known.
