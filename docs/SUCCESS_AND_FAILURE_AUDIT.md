# Success and failure in every vignette — audit and proposals (October 30)

The owner asked: "Can you ensure that each vignette has a success and failure state? Consider rewards/repercussions for all." The Goatman is the reference:
- **Success** (the vigil kept) earns a reward that lasts: the tally counter, an owner-bound artifact that keeps working.
- **Failure** (the door opened, or lost outside) resolves nothing, and its repercussion lasts: the Goatman comes home with the reader, privately and harmlessly, until a later night is got right.

This document lists, for all 55 stories in the Witness pool:
- what each has now, read from the code;
- a proposal for whatever is missing.

These are proposals for the owner to weigh. Nothing here is built except Usher (0.4.184, committed and awaiting push), which follows the Goatman's pattern and can be changed like everything else.

## How the proposals are built

So that 55 rooms stay consistent and the work stays manageable, the proposals lean on two shared pieces and one rule.

1. **A lasting repercussion: the room follows the reader** (a shared `VignetteHaunts`, generalising the Goatman's and Usher's).
   - **What it is.** Failing a room registers a private, harmless haunting in that room's own voice: a sound, a glimpse or a small wrongness. It comes every few minutes, wherever the reader is, and only they notice it.
   - **How it ends.** Only when that reader succeeds at that room on a later visit.
   - **What it never does.** It never damages or takes anything. It can grow a little stronger each House day, as Usher's scratching does.
   - **Showing it.** Each active haunting is listed in the reader's account as an open line, so the failure is visible.
2. **A lasting reward: something to keep** (a shared `VignetteKeepsakes`, for rooms with no artifact yet).
   - **What it is.** Success gives an owner-bound keepsake from the room: one item type, one sprite per room drawn in code, and a line of lore.
   - **Offering it.** Keepsakes are vignette artifacts in the Mother's bargains, as existing artifacts are.
   - **Rooms with a working artifact** keep it (the tooth, the key, the globes, the stone, the Bone Ring and so on). A few keepsakes get a small use where it fits the story; most are mementos.
3. **The rule: every room fails in its own way.**
   - **The trigger.** Failure always comes from the story's own threat, or from a wrong turn it sets up. It is never an arbitrary timer bolted on.
   - **What it costs.** It resolves nothing that visit, and the reader can try again on a later one.
   - **Rooms that already kill** keep their death and the Mother's keeping of belongings (the existing hard repercussion). A haunting is added only where a room's failure costs nothing lasting now.

Each story below shows **now** (S success, F failure, R reward, X repercussion) and **proposed**. "Exists" means already in the game. "Soft" means the failure sets the reader back but costs nothing lasting.

## Survival: the rooms with a threat

**The trailer (Goatman).** The reference.
- **Now:**
  - S: the night counted right.
  - F: the door opened, lost outside, or a death.
  - R: the tally counter.
  - X: it comes home with them until a night is got right.
- **Proposed:** nothing; complete.

**The small passage (Ted the Caver)**
- **Now:**
  - S: retraced the squeeze.
  - F: the choke blackout (soft: wakes at the last air).
  - R: none.
  - X: none.
- **Proposed:**
  - F: the pursuit catching the reader in the low chamber. They wake outside the crack and nothing is resolved.
  - X: breathing in the walls, keeping time with the cave's in-breath, wherever they sleep.
  - R: a keepsake, their own string line, cut short.

**The counted lights (Holloway)**
- **Now:**
  - S: three rooms and the service latch.
  - F: the arenas.
  - R: the Holloway shield.
  - X: death, with belongings to the Mother.
- **Proposed:** nothing; complete.

**The disappearing exits (child's room)**
- **Now:**
  - S: reached the basement through the crawl.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: still standing in the room when the last exit seals. The room keeps them until the House lets go: set back at the entrance, nothing resolved.
  - X: a toy from the ceiling plays somewhere near them.
  - R: a keepsake, one of the toys taken to the ceiling.

**The wet boards (Bly)**
- **Now:**
  - S: waited in a shut cupboard until the lake.
  - F: held and dragged by the Lady into the lake room (soft; the account remembers it).
  - R: none.
  - X: none lasting.
- **Proposed:**
  - X: wet footprints and a drip close by, until a later night in the cupboard.
  - R: a keepsake, a governess's pressed flower from the schoolroom.

**The looping field (Last Stop)**
- **Now:**
  - S: escaped the looping field and read the clipping. A capture by the men or by the herd gives each its own account.
  - F: none; every way out resolves.
  - R: none.
  - X: none.
- **Proposed:**
  - F: walking into the herd's closing eyes. They wake on the porch with no clipping.
  - X: an elk calls from just out of sight at dusk.
  - R: a keepsake, the cap from the broken post.

**The passing boots (elk carcasses)**
- **Now:**
  - S: held still under the hides and left by the service path.
  - F: the killer finds them, with real blows.
  - R: none.
  - X: damage, and death with belongings to the Mother.
- **Proposed:** R: a keepsake, a hunter's tag cut from a hide.

**The thirteenth witch (Costume Night)**
- **Now:**
  - S: reached the living grass.
  - F: her strikes.
  - R: none.
  - X: damage and death.
- **Proposed:** R: a keepsake, a contest number card.

**The flashes over water (Movie Night)**
- **Now:**
  - S: rowed to the far pier between exposures.
  - F: her strikes.
  - R: none.
  - X: damage and death.
- **Proposed:** R: a keepsake, a ticket stub for *Teeth of Summer*.

**The dare (Winter Lake)**
- **Now:**
  - S: broke the ice, waited beneath it and reached the bank.
  - F: pulled under, a heart lost (soft).
  - R: the ribbon goes back to the hand.
  - X: none lasting.
- **Proposed:**
  - X: ice cracking underfoot wherever they stand, until the dare is done.
  - R: a keepsake, one of the boys' mittens.

**The service road (Camp Blood)**
- **Now:**
  - S: escaped past the last cabins.
  - F: the slasher.
  - R: none.
  - X: death and belongings to the Mother.
- **Proposed:** R: a keepsake, a counsellor's whistle.

**Proofrock (the drowned town)**
- **Now:**
  - S: the hymn released.
  - F: her strikes.
  - R: none.
  - X: damage and death.
- **Proposed:** R: a keepsake, a page of a student essay.

**The shallows**
- **Now:**
  - S: threw her, held her feet, or watched from the bank.
  - F: a blow before the throw darkens the visit (soft).
  - R: none.
  - X: none.
- **Proposed:**
  - F: striking the boys or her: the visit darkens and nothing is resolved (as now, made the explicit failure).
  - X: her voice from water, a pool or a cauldron near them.
  - R: a keepsake, a shell from the shallows.

**Twenty seconds (backseat)**
- **Now:**
  - S: returned from the car among the pines.
  - F: none found.
  - R: none.
  - X: none.
- **Proposed:**
  - F: looking back at what rides behind them before the twenty seconds are out.
  - X: something shifts on the seat behind them when they ride or sit.
  - R: a keepsake, the car's mirror ornament.

**The third count (third-floor bathroom)**
- **Now:**
  - S: said it three times and came back out.
  - F: none found.
  - R: none.
  - X: none.
- **Proposed:**
  - F: the candle going out before they are back through the stall door.
  - X: their reflection lags a moment in glass and water, privately.
  - R: a keepsake, the candle stub.

**Under the green lights (tunnel)**
- **Now:**
  - S: passed the last green signal.
  - F: caught, a hand on the neck, and set back.
  - R: none.
  - X: none lasting.
- **Proposed:**
  - X: stooping footsteps behind them that stop when they turn.
  - R: a keepsake, a step-count chit from the signs.

**The unhinged door (buzzer)**
- **Now:**
  - S: left by the unpainted door, or kept the door closed.
  - F: caught, the buzzer stopping.
  - R: none.
  - X: none lasting.
- **Proposed:**
  - X: a buzzer at the nearest door at night.
  - R: a keepsake, the scrawled note.

**The painted sun (boarded house)**
- **Now:**
  - S: left by the unpainted door.
  - F: touched by the family and set back.
  - R: the Bone Ring.
  - X: none lasting.
- **Proposed:** X: a propped figure glimpsed in a corner, gone when looked at.

**The empty mattress (split-feed bedroom)**
- **Now:**
  - S: closed the door on the other feed.
  - F: caught.
  - R: the Smoky Mirror.
  - X: none lasting.
- **Proposed:** X: the second feed: a moment of the reader's own empty bed on any screen.

**The second apron (diner)**
- **Now:**
  - S: left after tasting the meal.
  - F: answering her.
  - R: none.
  - X: none lasting.
- **Proposed:**
  - X: "How's your meal?" heard when they eat.
  - R: a keepsake, a diner receipt.

**The cellar shift (basement)**
- **Now:**
  - S: closed the cellar after the blade broke.
  - F: taken by the fold and set upstairs.
  - R: the cleaver.
  - X: none lasting.
- **Proposed:** X: tearing paper behind a wall near them.

**Yellow dust (silo)**
- **Now:**
  - S: opened the roof and returned to the grain.
  - F: none found.
  - R: none.
  - X: none.
- **Proposed:**
  - F: sinking in the grain. They wake outside the silo and nothing is resolved.
  - X: a cough of yellow dust from their own sleeve.
  - R: a keepsake, a vial of the dust.

**The house in the third lake (lake house)**
- **Now:**
  - S: the reader's choices.
  - F: drowning blackout (soft, wakes on the raft).
  - R: the lake artifact.
  - X: none.
- **Proposed:**
  - F: blacking out in the house before the lights are lit. Nothing is resolved.
  - X: Tess's wet footprints beside theirs.

**The coffin lid (Usher)**
- **Now (0.4.184, unpushed):**
  - S: fled as the house fell, or found her in the wing.
  - F: still inside when the house falls.
  - R: a piece of the house.
  - X: the scratching follows them.
- **Proposed:** nothing; complete.

## Understanding, memory, connection, release: the rooms of choice

These rooms have no monster to fail against. Their failure has to be a wrong turn the story itself offers: turning away, being too late, doing the thing the story warns against. Several already have a choice with two endings; the proposal names which side is the failure, or adds one.

**The floorboards (Tell-Tale)**
- **Now:**
  - S: took up the board.
  - F: the heartbeat peaks and they are sent out (soft).
  - R: none.
  - X: none.
- **Proposed:**
  - X: the heartbeat under the floor wherever they stand.
  - R: a keepsake, the old man's stopped watch.

**Hide-and-clap**
- **Now:**
  - S: completed the game.
  - F: caught, which kills and drops everything.
  - R: none.
  - X: death with belongings to the Mother.
- **Proposed:** R: a keepsake, the folded blindfold.

**Mr. Harrigan's phone**
- **Now:**
  - S: buried his phone, or kept it.
  - F: none.
  - R: the kept phone works.
  - X: none.
- **Proposed:**
  - F: calling him with a name from his own phone, the thing the story warns against. The account becomes the one about who was named, and nothing is resolved.
  - X: his phone rings near them and stops when they look.
  - R: if buried, a keepsake, the funeral card.

**The model home**
- **Now:**
  - S: saw the tree.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: staying at the window when the glass gives. Nothing is resolved.
  - X: a tree's shadow on the wall that no tree casts.
  - R: a keepsake, the sales brochure.

**The preserved cave (congregation)**
- **Now:**
  - S: crossed the congregation.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: a turned pew, or looking back while crossing.
  - X: a hymn hummed under their breath that isn't theirs.
  - R: a keepsake, a hymnal page.

**The recording (Phone in the Canoe)**
- **Now:**
  - S: the clip completed, with the phone recovered from the cave.
  - F: none.
  - R: the phone.
  - X: none.
- **Proposed:**
  - F: leaving the canoe before the recording ends.
  - X: a phone vibrating in their pocket with nothing on it.

**The readers (Zampanò)**
- **Now:**
  - S: read the final page.
  - F: none.
  - R: the leaves they take.
  - X: none.
- **Proposed:**
  - F: taking the archive's originals out of the courtyard.
  - X: the Minotaur passage struck red on any page they write.
  - R: as now.

**Return to sender (Whale)**
- **Now:**
  - S: the originals returned to them.
  - F: none.
  - R: the originals and the envelope.
  - X: none.
- **Proposed:**
  - F: posting a letter not in their own hand.
  - X: a letter in their own hand they did not write, in their inventory now and then.

**The cover (barn well)**
- **Now:**
  - S: waited and climbed out.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: climbing out before the wait is over.
  - X: the lid's scrape overhead.
  - R: a keepsake, a stone with initials.

**The distant frame (plain)**
- **Now:**
  - S: held the distant frame.
  - F: none.
  - R: the photograph.
  - X: none.
- **Proposed:**
  - F: letting the silhouette come close before the frame is taken.
  - X: the crouched figure at the edge of their sight on open ground.

**The call button (hospital)**
- **Now:**
  - S: the dawn reading.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: pressing the call button before dawn.
  - X: a call bell at night.
  - R: a keepsake, a patient's bracelet.

**The Mother**
- **Now:**
  - S: gave an original, or saved the dog.
  - F: a ransom unpaid.
  - R: the returned companion.
  - X: she keeps what is not reclaimed (exists).
- **Proposed:** nothing; complete.

**The empty chair (séance)**
- **Now:**
  - S: drove the family out, heard the medium, or left when asked.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: refusing to leave when the family asks.
  - X: three knocks from a cupboard near them.
  - R: a keepsake, a sitting card.

**The pattern (wallpaper)**
- **Now:**
  - S: read the sealed journal signed with their own name.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: tearing the paper.
  - X: the pattern moving at the edge of their sight.
  - R: a keepsake, a strip of the paper.

**The closing account (Stanley)**
- **Now:**
  - S: settled, signed, or walked out with the master key.
  - F: none.
  - R: the master key.
  - X: the bath creature (a random encounter).
- **Proposed:**
  - F: still in the hotel at closing.
  - X: a room-service knock.
  - R: for settling or signing, a keepsake, the room's key tag.

**The returned knocks (Hill nursery)**
- **Now:**
  - S: each knock returned.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: knocking a different count from the pounding.
  - X: pounding on the wall nearest them.
  - R: a keepsake, the chalk.

**The little rooms (miniatures)**
- **Now:**
  - S: examined three arrangements.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: moving a figure.
  - X: a small figure standing behind theirs in the next room they enter.
  - R: a keepsake, a figure of themselves.

**The last clock (Masque)**
- **Now:**
  - S: stopped the seventh room's clock.
  - F: moving during the chime puts them back (soft).
  - R: none.
  - X: none.
- **Proposed:**
  - X: a clock chime counting where no clock is.
  - R: a keepsake, the invitation.

**The upper ledger (Winchester)**
- **Now:**
  - S: reached the upper ledger.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: taking the short stair to plaster.
  - X: hammering behind the walls at night.
  - R: a keepsake, a carpenter's nail.

**The recorded voices (Crimson Hall)**
- **Now:**
  - S: three cylinders heard, the red floor exposed.
  - F: none.
  - R: the cylinders.
  - X: none.
- **Proposed:**
  - F: stopping a cylinder before its voice ends.
  - X: a voice on any record they play.

**The broken light (elk fan)**
- **Now:**
  - S: held the platform and was thrown out.
  - F: none.
  - R: the tooth and the bulb.
  - X: none.
- **Proposed:**
  - F: climbing down before the repair is done.
  - X: a fan's tick overhead.

**The crawl beyond the roof (mapping)**
- **Now:**
  - S: kept the things, or broke them.
  - F: none.
  - R: the photograph.
  - X: none.
- **Proposed:**
  - F: not returning to the brother.
  - X: the brother's silhouette, far off.

**The food in the hollow (Holy Rabbit)**
- **Now:**
  - S: ate and followed the drag trail.
  - F: none (the cold never takes the last hearts).
  - R: the knife and the rabbit's foot.
  - X: none.
- **Proposed:**
  - F: leaving Father after committing.
  - X: Father's shaking voice in the cold.

**The listener's signature (confession)**
- **Now:**
  - S: left the mark and read the transcript.
  - F: none.
  - R: the blank journal.
  - X: none.
- **Proposed:**
  - F: leaving the booth unsigned.
  - X: the listener's breathing in their journal's silence.

**The eight loose pages (Devil's Rock)**
- **Now:**
  - S: read all eight pages.
  - F: none.
  - R: the diary.
  - X: none.
- **Proposed:**
  - F: following the path past the rock.
  - X: footsteps on gravel behind them.

**The numbered doors (Wheel)**
- **Now:**
  - S: found Mother's room.
  - F: none.
  - R: the key.
  - X: none.
- **Proposed:**
  - F: opening a door out of order.
  - X: a door number they have not opened, knocking.

**The tape and the cut (Ghosts set)**
- **Now:**
  - S: the filming days completed and their own cut read.
  - F: none.
  - R: the camcorder and the film.
  - X: none.
- **Proposed:**
  - F: missing the final day's take.
  - X: their own footage flickering on screens.

**The visitors at dusk (cabin)**
- **Now:**
  - S: offered, or refused.
  - F: refusing (a visitor walks into the lake; a room goes dark).
  - R: the jar, or the globe; the cracked globe for refusing.
  - X: the cracked globe (exists, a binary already).
- **Proposed:** nothing; complete.

**The house they remembered (family copy, old cabin)**
- **Now:**
  - S: left when asked, refused, or heard his story.
  - F: none.
  - R: none.
  - X: none.
- **Proposed:**
  - F: refusing the family's request to leave becomes the failure.
  - X: their own home's door unlocked when they return.
  - R: a keepsake, the family's photograph.

**The last finger (monkey's paw)**
- **Now:**
  - S: the last wish, the door opened onto the empty road, or the paw given back.
  - F: a corrupted wish.
  - R: the purse and the paw.
  - X: the knocking (exists).
- **Proposed:** nothing; complete.

**Borrasca**
- **Now:**
  - S: silenced the mountain.
  - F: none found.
  - R: the compass and the patch.
  - X: none.
- **Proposed:**
  - F: following the sheriff past Marker Two.
  - X: their name carved in a tree they pass.

## What this would add

- **Shared systems: two.**
  - `VignetteHaunts`: about 35 room voices, each a sound or small cue, cleared by success in that room.
  - `VignetteKeepsakes`: about 30 keepsakes, each with one sprite and one line of lore.
- **New failure triggers: about 25**, each written for its own room.
  - They mostly reuse what the room already tracks: the chime, the seal, the knock counts, the grain, the call button.
  - A few need new logic: the backseat's look back, the bathroom's candle, Harrigan's call.
- **No change to Witness:** still 55 sources, 42 required, two kinds and three endings.
  - Failure never grants credit.
  - Success already does, once.

## Suggested order, once agreed

1. The two shared systems, with the rooms whose failure already exists but costs nothing lasting:
   - **Haunting added:** the uncanny rooms, the tunnel, the buzzer, Bly, Winter Lake, the floorboards, the Masque.
   - **Keepsake added:** the deadly rooms with no reward (carcasses, Costume Night, Movie Night, Camp Blood, Proofrock, hide-and-clap).
2. **The survival rooms with no failure:** the child's room, the Last Stop, the backseat, the bathroom, the silo, the cave.
3. **The rooms of choice,** a few at a time, each with its own trigger.

Each batch is tested and validated in CI before the next.
