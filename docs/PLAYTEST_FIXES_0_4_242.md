# The Ghosts Set from the owner's notes (0.4.242)

The owner's notes on *A Head Full of Ghosts* (let.docx, 2 November) and the screenshots and lines that followed through the day. No
protocol, layout, Witness or source-kind change.

## The grace

"Parents asking to pray – holding hands, mary hesitant, Marjorie slipping from her seat and ducking beneath the table." On the
second day John says "Everybody take hands. Let's pray." and the scene now plays it:

- John and Sarah leave their chairs and stand beside the girls, facing the table, and each holds out a hand: Sarah down to Merry,
  John to Marjorie.
- Merry holds back (`GhostsSet.WARY`): forearms drawn up to her chest, watching her sister across the table. A few seconds in
  (`GhostsSet.GRACE_MERRY`) she gives Sarah her hand and bows her head.
- Marjorie never takes John's. She slips off her chair still seated (`GhostsSet.SLIP`: thighs tipping down, leaning back, arms
  trailing, head on one side), slower than she used to move, and only on the floor does she go onto all fours and under the table.
  John steps back from her and watches.
- When grace is over the parents walk back to their chairs and sit facing the table, not the way they walked.

`GhostsVisualProof` draws the grace from both sides of the table, close enough to see the hands (`native-ghosts-seating-prayer.png`),
and its pose pages draw all twenty poses.

## Faces

"Shifting facial expressions." The family's faces move (`GhostsCastModel.expression`, `GhostsSkinMesh.expression`). The painted brows
go under forehead skin and are drawn again a pixel at a time, lifted or lowered, the inner ends raised for worry or pulled down to
knit; one lip-coloured pixel either side of the mouth turns it up or down. Every colour is sampled from the person's own painted
sheet, so nothing is repainted for it.

- What a person is doing sets the face: worry and a pressed mouth at grace, wide fear when flinching or falling, pain in John's
  hand, Marjorie's knitted brows and small smile when she slides off her chair, crawls, stares or lies on the wall.
- The days wear on them: John, Sarah and Merry look more worried each day; from the second day Marjorie's brows sit lower.
- Between lines a face moves on its own, a look up, a pressed mouth, and now and then Marjorie smiles for a moment.
- A change of face takes about a second.

## Merry's hair

"Merry needs longer hair." Merry's sheet is painted again with the long cut (`tools/paint_character_skins.py`): hair framing her face
and over her ears. Face, clothes, size and alpha are unchanged. `art/inventory/ghosts_merry_04242.json` records her old hash and the
new one, and `tools/verify_character_art.py` holds her to it.

## The last supper

"the final meal should be spaghetti with red sauce. merry's spaghetti doesn't have sauce". On the seventh day the family's three
plates are spaghetti with red sauce and Merry's is plain, as the story has it: she never eats the sauce. They are drawn at the game's
scale by `tools/draw_ghosts_supper.py` (one 16x16 texture, `ghosts_meal_spaghetti` and `ghosts_meal_spaghetti_plain`), replacing
sandstone noodles and a square cut from a generated sheet. Marjorie says she made the sauce and that Merry has hers plain; looking
at the plates says the same. The draw check is in the quick gate. The day-two supper is unchanged.

## The seventh day

- **"cut recording dialogue from the seventh day."** The show is over and nobody on the last day talks about filming: Barry's
  closing line about keeping the tape is gone; the idle chatter is household talk; Merry and Sarah no longer bring up what the
  episode kept of them; Barry's call on arrival is about collecting the gear (and he says nothing once he has gone), his second
  line is about the cable on the stairs, and his menu offers "Stay for supper" with no camera log. The arrival header is "Day 7",
  and the cooker, tripod and monitor are described as the crew packing up. `GhostsSetTests#theLastDayHasNoTalkOfFilming` checks
  every scripted, idle, talk and arrival line of the day against a list of filming words.
- **"ensure all npcs properly leave the house too."** Barry and Father Wanderly walk out of the front door and are gone, not left
  standing in the hall; so is Merry once she runs out. They come back only when a day is staged again.
- **"don't require the player to witness the death of the family to have it concluded."** Once the last day's take has begun it
  plays out whether or not the camera is rolling or in the room, and a reader who leaves by the door during the supper still ends
  the day: what happened at the table happened.
- **"if they see it through to the end - merry running out the door, the lights in the house should cut off. the family remains."**
  When Merry is through the front door the house goes dark for that reader (`GhostsLights`): every ceiling light, lamp, the fairy
  lights and the set's lamps go out on their client, sent again every five seconds so a chunk loaded afresh is dark too. The windows
  keep the light from outside. John, Sarah and Marjorie stay at the table.

## The house

- **"Marjorie stuck on the wall."** On the third day she now comes down from the wall to the floor, and if anything holds her she is
  put on the floor rather than left hanging.
- **"Marjorie halfway out of the bed."** On the sixth day she lies where the bed pose puts her head on the pillow and her feet at the
  foot (`GhostsSet.BED_LIE_Z`).
- **"Unconnected camera tracks. Camera tracks breaking rug."** The hall's cable crossed the runner by replacing two of its cells. The
  runner now has a cell with the cable taped across it (`RUG_RUNNER_CABLE`), the cable runs on through the opening to the den, and
  the den's run turns its corner instead of stopping short.
- **"lights should span all of marjorie's room."** Her fairy lights go round all four walls under the ceiling.
- **"try to make those actually look like fruit."** The fruit bowl holds a red apple and a green apple with stems and a leaf, an
  orange and a banana (`tools/draw_barrett_home.py`).
- `ModelUvProof` (`-PuvSmoke`) has a third page, `native-ghosts-set-props.png`: the fruit bowl, both supper plates, the taped cable
  and the fairy lights.

## Words

"try to cut the ai-isms from the vignette." The family's and the crew's lines, the idle chatter, the shot notes and the episode
review are rewritten in plain speech: shorter, in the way these people talk, without the speeches about television. They are spoken
on the action bar, not in chat. The camera log names shots in words ("the hat at supper", "John's hand", "the drawer") instead of
internal keys ("Sustained shots: HatExchange, InjuredHand", which the owner noted was not right). The account opens "I filmed the
Barretts on seven days" and no longer closes on an aphorism.

## Tests

The twenty-three `GhostsSetTests` cases (one new) and the saved-seat case (`Vignette0492Tests`) pass locally; the broadcast-story case now looks
for the rewritten lead. The meal case checks both last-supper plates. Quick-gate checks pass: textures, character art (with Merry's
record), model particles, the Barrett home and supper drawings, and the painted sheets.

## Run 1397

Seventeen of eighteen jobs passed. In the full suite the keepsake case's cow, which stands outside its one-block template, was
removed before it settled ("alive false"), most likely by a neighbouring test clearing its own area. A cow that is gone is now put
down again until one has settled.

## Run 1398

- The build job's package check lists every Ghosts Set kind by hand and did not know the two supper plates; it does now.
- The live two-client proof caught Marjorie stepping up onto her bed in a single tick on day six (0.58 blocks). She lies further
  down the bed now, so her first move off it had a sideways part from inside the bed, and the step-up lifted her in one go. She
  rises straight up from where she lies first, then walks along the bed as before (`GhostsJump`).
  `GhostsSetTests#daySixWalkFromTheBedHasNoSingleTickJump` starts the day as the live proof does and fails without the change.
