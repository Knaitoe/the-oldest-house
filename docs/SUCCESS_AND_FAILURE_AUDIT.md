# Success and failure in every vignette: trades (October 30, second draft)

The owner asked: "Can you ensure that each vignette has a success and failure state? Consider rewards/repercussions for all." On the first draft the owner added:
- "Look to the end of world cabin. Winning sometimes takes, too. Failing sometimes gives."
- "Not every fail state should haunt the reader, unless appropriately suited for it, like the Goatman."
- "The Goatman isn't just ooh spooky figure, either. He's mechanically intrusive. He steals food and knocks at doors."

The first draft gave every room the same pair: a haunting for failure and a keepsake for success. This draft replaces that. Every outcome is a trade, as at the cabin. A haunting appears only in the few rooms whose story is about something that follows the reader home, and there it intrudes on play, as the Goatman does.

This document lists, for all 55 stories in the Witness pool:
- what each has now, read from the code;
- what a win and a fail would give and take.

These are proposals for the owner to weigh. Nothing here is built except Usher (0.4.184, pushed). The section on Usher below recommends changing it.

## The model

**1. Outcomes are trades.** At the cabin both answers cost and both give:
- **Offering everything** costs hearts and an arm. It gives the whole globe, which won't let its keeper die once a day.
- **Refusing** closes one of the reader's own rooms. It gives the cracked globe, which catches its keeper once.

Several rooms already work like this. The proposals extend it rather than invent a new system:
- **Winter Lake:** the dare is won by giving the ribbon back to the hand.
- **The Stanley:** settling the account pays the bill.
- **The Smoky Mirror:** it shows the true view for blood.
- **The Shallows:** throwing her costs the reader's strength.
- **The monkey's paw:** every wish gives and takes at once.

**2. Haunting is rare, and when it happens it intrudes.** The Goatman is the measure. Coming home with a reader who let it in, it does three things:
- it eats one piece of food from their pack each day ("Someone has been at your food");
- some nights it tries a door they are standing near;
- it is glimpsed at the edge of their sight and is gone when they turn.

It never harms them. Only that reader notices it, and only a later night got right ends it. A haunt in this draft must meet the same bar:
- **The story is about something that follows.** It is not merely frightening.
- **It changes something in play.** It takes, opens, moves or occupies something; a sound alone is not enough.
- **It never harms.** It is private, and it ends only with a later success in that room.

Five rooms meet that bar beyond the Goatman (below). Usher does not, and the scratching it has now is only a sound.

**3. Failure comes from the story.** It is the story's own threat, or a wrong turn the story sets up, never a timer bolted on.
- It resolves nothing that visit and gives no Witness credit.
- The reader can try again later.
- Rooms that kill keep their death and the Mother's keeping of belongings.

**4. Limits on what a room may take:**
- Never a House original, an artifact, an owner-bound item or another reader's things.
- What a room keeps, it keeps where the reader can see it (on the ceiling, on the bank, in the chamber), and a later win gives it back.
- Never a heart below the room's existing floor, and never anything in the Mother's custody.

**Blank sides are deliberate.** Not every room needs all four sides (win gives, win takes, fail gives, fail takes). A blank side means the story doesn't ask for one.

## What a room can give or take

| | Gives | Takes |
|---|---|---|
| **Win** | An artifact or keepsake. A lasting small gift (the caver's breath). | A price paid to win (the arm, the ribbon, the bill). Something left behind (the string line). A mark carried out (the hides' smell). The room itself, closed to that reader (the cabin's closed room). |
| **Fail** | A lesser or broken version of the reward (the cracked globe, a blurred photograph). Something the story hands the loser (the rabbit alive). An account of the failure. | Something held, kept in the room until a later win. A setback the next attempt starts from (the giant starts closer). A death, where the room already kills. A haunt, only where suited. |

## The haunts: six rooms

Each intrudes the way the Goatman does and never harms. Each ends only with a later success in that room.

| Room | Why it follows | What it does |
|---|---|---|
| **The trailer (Goatman)** | Exists; the reference. | Eats from their pack, tries doors, is glimpsed. |
| **The wet boards (Bly)** | The Lady of the lake follows the one she held: the story's own ending is her coming back for Dani. | At night near water, the reader is walked a few steps toward it that they did not take, and stopped at the edge. Wet footprints lead to their bed. Never into the water. |
| **The looping field (Last Stop)** | The elk follows the hunters home for years in *The Only Good Indians*. | Once a day a gate or door of an animal pen near them is found open, a hoof print beside it. Animals may wander; none is hurt. |
| **Twenty seconds (backseat)** | The thing in the back seat rides home. | The seat behind them is taken: no passenger fits in a boat or cart they ride, and once a ride the boat lurches as if weight shifted. |
| **The returned knocks (Hill nursery)** | HELP … COME HOME on the wall: the house wants them back. | Now and then, sleeping in their own bed, they wake in the manor instead. |
| **Mr. Harrigan's phone** (kept) | The dead man answers. Here winning takes: the reader who keeps the phone keeps him. | Once a day his phone turns up in their hand, playing his voicemail greeting until they put it away. |

## Usher: a recommendation

**What 0.4.184 has.** Failing the flight (still inside as the house falls) starts a private scratching under the reader's feet, every three to seven minutes, louder each day. It ends when they get out in time on a later night.

**Why it should go.** It fails both tests above:
- Poe's house sinks into the tarn and nothing follows the narrator out.
- The scratching only sounds; it changes nothing.

**Recommended trade:**
- **Fled (closed lid):**
  - Gives the stone (as now).
  - Takes the house. The Usher door no longer deals to that reader, as the cabin closes a room. They saw it go into the tarn.
- **Found her in the wing (open lid):**
  - Gives the stone.
  - Takes nothing. The open lid already cost the storm night.
- **Buried (fail):**
  - **Takes:** the tarn keeps what was in their hands. It lies on the bank by the causeway on the night they get out in time.
  - **Gives:** a leaf from the vault register with their own name in the burial column. It is a keepsake of the failure that can be offered to the Mother.

The reading, the shared night and the flight stay as built; only `UsherHaunt` would be removed. **I won't change it until you say.**

## Room by room

Each room shows **now** (S success, F failure, R reward, X repercussion) and the proposed trade. "New" marks a failure that doesn't exist yet. "Soft" means the failure sets the reader back but costs nothing lasting.

### Survival: the rooms with a threat

**The trailer (Goatman).**
- **Now:**
  - S: the night counted right; R: the tally counter.
  - F: the door opened, or lost outside; X: it comes home with them.
- **Proposed:** nothing; complete.

**The small passage (Ted the Caver).**
- **Now:**
  - S: retraced the squeeze.
  - F: the choke blackout (soft).
- **Win gives:** the cave's breath: a few more seconds of air underwater, for good.
- **Win takes:** their string line and every torch they set. Ted's crew left theirs.
- **Fail (new):** the pursuit catches them in the low chamber. They wake outside the crack.
- **Fail takes:** what was in their hands. It lies in the low chamber, theirs again on the visit they get out.

**The counted lights (Holloway).**
- **Now:**
  - S: three rooms and the latch; R: the shield.
  - F: death; X: belongings to the Mother.
- **Proposed:** nothing; winning already costs the arenas.

**The disappearing exits (child's room).**
- **Now:** S: through the crawl to the basement. There is no failure.
- **Win gives:** one of the toys taken to the ceiling.
- **Fail (new):** still in the room when the last exit seals. The room lets go at the entrance.
- **Fail takes:** what was in their hand goes up to the ceiling above the bed, with the toys. A later win brings it down.

**The wet boards (Bly).**
- **Now:**
  - S: waited in the cupboard.
  - F: held and dragged into the lake room (soft).
- **Win gives:** Flora's doll from the cupboard.
- **Fail:** the Lady follows (haunt, above).

**The looping field (Last Stop).**
- **Now:** S: escaped the field and read the clipping; captures have their own accounts. There is no failure.
- **Win gives:** the cap from the broken post.
- **Win takes:** the tab comes due: an emerald a drink from their pack, as far as it goes.
- **Fail (new):** walking into the herd's closing eyes. They wake on the porch with no clipping, and the elk follows (haunt, above).

**The passing boots (elk carcasses).**
- **Now:**
  - S: held still and left by the service path.
  - F: the killer's blows, and death.
- **Win gives:** a hunter's tag cut from a hide.
- **Win takes:** the hides' smell. Animals shy from them until they wash in water.

**The thirteenth witch (Costume Night).**
- **Now:**
  - S: reached the grass.
  - F: her strikes, and death.
- **Win gives:** their number card from Hank's table.

**The flashes over water (Movie Night).**
- **Now:**
  - S: rowed to the far pier.
  - F: her strikes, and death.
- **Win gives:** a ticket stub for *Teeth of Summer*.

**The dare (Winter Lake).**
- **Now:**
  - S: reached the bank after the hand took the ribbon (winning takes the ribbon).
  - F: pulled under, a heart (soft).
- **Win gives:** Bud's note, signed by all four boys.

**The service road (Camp Blood).**
- **Now:**
  - S: past the last cabins.
  - F: the slasher, and death.
- **Win gives:** a counsellor's whistle.

**Proofrock.**
- **Now:**
  - S: the hymn released.
  - F: her strikes, and death.
- **Win gives:** a page of a student essay.

**The shallows.**
- **Now:**
  - S: threw her (costs exhaustion), held her feet, or watched.
  - F: a blow before the throw darkens the visit.
- **Proposed:**
  - It is already a trade.
  - The blow becomes the explicit failure.
  - **Fail takes:** the boys' company: they go home, and the account says so.

**Twenty seconds (backseat).**
- **Now:** S: back from the car among the pines. There is no failure.
- **Win gives:** the mirror ornament.
- **Fail (new):** looking back before the twenty seconds are out. It rides home with them (haunt, above).

**The third count (third-floor bathroom).**
- **Now:** S: said it three times and came out. There is no failure.
- **Win gives:** the candle stub.
- **Fail (new):** the candle goes out before they are back through the stall door.
- **Fail takes:** whatever light they held. It stays in the stall until a later win.

**Under the green lights (tunnel).**
- **Now:**
  - S: past the last green signal.
  - F: caught and set back.
- **Fail takes:** distance. The signs count fewer steps left on that reader's next attempt; a win resets it.

**The unhinged door (buzzer).**
- **Now:**
  - S: left by the unpainted door, or kept it closed.
  - F: caught.
- **Win gives:** the scrawled note.

**The painted sun (boarded house).**
- **Now:**
  - S: left by the unpainted door; R: the Bone Ring.
  - F: touched and set back.
- **Proposed:** nothing; complete.

**The empty mattress (split-feed bedroom).**
- **Now:**
  - S: closed the door on the other feed; R: the Smoky Mirror, which costs blood to use.
  - F: caught.
- **Proposed:** nothing; complete.

**The second apron (diner).**
- **Now:**
  - S: left after tasting the meal.
  - F: answering her.
- **Win gives:** the receipt.
- **Win takes:** a moment of nausea from what was on the plate.
- **Fail takes:** she clears their plate: one piece of food from their pack.

**The cellar shift (basement).**
- **Now:**
  - S: closed the cellar; R: the cleaver.
  - F: taken by the fold.
- **Proposed:** nothing; complete.

**Yellow dust (silo).**
- **Now:** S: opened the roof and returned to the grain. There is no failure.
- **Win gives:** a vial of the dust.
- **Fail (new):** sinking in the grain. They wake outside the silo.
- **Fail takes:** a cough of yellow dust for a minute, leaving them hungry.

**The house in the third lake (lake house).**
- **Now:**
  - S: the reader's three choices, each with its own finite keepsake.
  - F: drowning blackout (soft).
- **Fail (new):** blacking out in the house before the lights are lit. Nothing is resolved.

**The coffin lid (Usher).** See the recommendation above.

### Understanding, memory, connection, release: the rooms of choice

These rooms have no monster. A failure here is a wrong turn the story itself offers.

**The floorboards (Tell-Tale).**
- **Now:**
  - S: took up the board.
  - F: the heartbeat peaks, and they are sent out (soft).
- **Win gives:** the old man's watch, wrapped in cotton.
- **Fail takes:** calm. The heartbeat starts louder on their next visit.

**Hide-and-clap.**
- **Now:**
  - S: completed the game.
  - F: caught, death and everything dropped.
- **Win gives:** the folded blindfold.

**Mr. Harrigan's phone.**
- **Now:**
  - S: buried his phone, or kept it.
  - R: the kept phone works.
  - There is no failure.
- **Kept:**
  - Gives the working phone.
  - Takes the reader's peace: he rings (haunt, above).
- **Buried:**
  - Gives the funeral card.
  - Takes the phone and its calls for good.
- **Fail (new):** leaving the funeral with the phone neither in the casket nor on them. It rings in the empty chapel on their next visit, and the choice comes again.

**The model home.**
- **Now:** S: saw the tree. There is no failure.
- **Win gives:** the sales brochure.
- **Fail (new):** staying at the window when the glass gives.

**The preserved cave (congregation).**
- **Now:** S: crossed the congregation. There is no failure.
- **Win gives:** a hymnal page.
- **Fail (new):** looking back while crossing.

**The recording (Phone in the Canoe).**
- **Now:** S: the clip completed; R: the phone, from the cave. There is no failure.
- **Fail (new):** leaving the canoe before the recording ends.

**The readers (Zampanò).**
- **Now:** S: read the final page; R: the leaves. There is no failure.
- **Fail (new):** carrying the archive's originals out of the courtyard.
- **Fail takes:** the originals crumble back to their shelves.

**Return to sender (Whale).**
- **Now:**
  - S: the originals returned; R: the originals and the envelope (winning takes the letters posted).
  - There is no failure.
- **Fail (new):** posting a letter not in their own hand. The reply comes in someone else's hand and the count starts again.

**The cover (barn well).**
- **Now:** S: waited and climbed out. There is no failure.
- **Win gives:** a stone with the initials.
- **Fail (new):** climbing out before the wait is over.

**The distant frame (plain).**
- **Now:** S: held the frame; R: the photograph. There is no failure.
- **Fail (new):** letting the silhouette come close before the frame is taken.
- **Fail gives:** a photograph of a blur too close to make out (a lesser reward).

**The call button (hospital).**
- **Now:** S: the dawn reading. There is no failure.
- **Win gives:** a patient's bracelet.
- **Fail (new):** pressing the call button before dawn.

**The Mother.**
- **Now:**
  - S: gave an original, or saved the dog (winning takes the original).
  - F: a ransom unpaid; X: she keeps what is not reclaimed.
- **Proposed:** nothing; complete.

**The empty chair (séance).**
- **Now:** S: drove the family out, heard the medium, or left when asked. There is no failure.
- **Drove them out:**
  - Gives the sitting record.
  - Takes the family: they don't return for that reader.
- **Fail (new):** staying past the family's third request. They are put out through the iron door.

**The pattern (wallpaper).**
- **Now:** S: read the sealed journal signed with their own name. There is no failure.
- **Win gives:** a strip of the paper.
- **Fail (new):** tearing the paper before reading the journal. The room is bare until their next visit.

**The closing account (Stanley).**
- **Now:**
  - S: settled (pays the bill), signed, or walked out with the master key.
  - There is no failure.
- **Win gives:** for settling or signing, the room's key tag.
- **Fail (new):** still in the hotel at closing, with nothing settled.
- **Fail takes:** the bill stays open into the next stay.

**The returned knocks (Hill nursery).**
- **Now:** S: each knock returned. There is no failure.
- **Win gives:** the chalk.
- **Fail (new):** knocking back a different count from the pounding. The house calls them home (haunt, above).

**The little rooms (miniatures).**
- **Now:** S: examined three arrangements. There is no failure.
- **Win gives:** a figure of themselves.
- **Fail (new):** moving a figure.
- **Fail gives:** the figure they moved. Their own stays in the arrangement.

**The last clock (Masque).**
- **Now:**
  - S: stopped the clock.
  - F: moving during the chime sends them back (soft).
- **Win gives:** the invitation.

**The upper ledger (Winchester).**
- **Now:** S: reached the upper ledger. There is no failure.
- **Win gives:** a carpenter's nail.
- **Fail (new):** taking the short stair to plaster.

**The recorded voices (Crimson Hall).**
- **Now:** S: three cylinders heard; R: the cylinders. There is no failure.
- **Fail (new):** stopping a cylinder before its voice ends.

**The broken light (elk fan).**
- **Now:**
  - S: held the platform and was thrown out (winning takes).
  - R: the tooth and the bulb.
  - There is no failure.
- **Fail (new):** climbing down before the repair is done.

**The crawl beyond the roof (mapping).**
- **Now:**
  - S: kept the things, or broke them (already a trade).
  - R: the photograph.
  - There is no failure.
- **Fail (new):** not going back to the brother.

**The food in the hollow (Holy Rabbit).**
- **Now:**
  - S: ate and followed the drag trail (winning takes Slaney).
  - R: the knife and the foot.
  - There is no failure.
- **Fail (new):** leaving Father after committing.
- **Fail gives:** Slaney, alive, as their own rabbit. The food stops.

**The listener's signature (confession).**
- **Now:** S: left the mark and read the transcript; R: the blank journal. There is no failure.
- **Fail (new):** leaving the booth unsigned.

**The eight loose pages (Devil's Rock).**
- **Now:** S: read all eight; R: the diary. There is no failure.
- **Fail (new):** following the path past the rock.

**The numbered doors (Wheel).**
- **Now:** S: found Mother's room; R: the key. There is no failure.
- **Fail (new):** opening a door out of order. The numbers change.

**The tape and the cut (Ghosts set).**
- **Now:** S: seven days and their own cut; R: the camcorder and the film. There is no failure.
- **Fail (new):** missing the final day's take.
- **Fail gives:** the production's cut, without their footage.

**The visitors at dusk (cabin).** The reference for trades; complete.

**The house they remembered (family copy, old cabin).**
- **Now:** S: left when asked, refused, or heard his story. There is no failure.
- **Win gives:** the family's photograph.
- **Fail (new):** carrying anything out of the copy of their home. It turns to dust at the door.

**The last finger (monkey's paw).** The purest trade: every wish gives and takes. It is complete. Its knocking belongs to the cottage, not to the reader's home.

**Borrasca.**
- **Now:** S: silenced the mountain; R: the compass and the patch. There is no failure.
- **Fail (new):** following the sheriff past Marker Two. They are back at the trailhead.

## What changes from the first draft

- **Haunts:**
  - **First draft:** about 35, mostly sounds.
  - **Now:** five new ones, all intrusive.
  - **Usher's** would be removed.
  - **No shared haunting system.** Each haunt is written for its room, as the Goatman's is.
- **Keepsakes:**
  - **First draft:** about 30.
  - **Now:** about 30, but only where a win has no other reward. Several rooms gain a cost on winning instead of, or as well as, a gift.
- **Failures:**
  - **New ones:** about 30, each from its own story.
  - **What they reuse:** mostly what the room already tracks (the seals, the knock counts, the grain, the candle, the call button).
  - **New logic:** a few need it: the backseat's look back, the cave's glance behind, the miniatures' moved figure.
- **Witness:** no change. Still 55 sources, 42 required, two kinds and three endings. Failure never grants credit.

## Suggested order, once agreed

1. **Usher's trade,** replacing its haunt (small, and it sets the pattern).
2. **The five haunts,** with their rooms' failures: Bly, Last Stop, backseat, Hill nursery, Harrigan.
3. **The survival rooms' trades:** the caver, child's room, bathroom, silo, tunnel, diner, carcasses.
4. **The rooms of choice,** a few at a time.
5. **Keepsakes for the rooms with nothing,** in one batch with their sprites.

Each batch is tested and validated in CI before the next.
