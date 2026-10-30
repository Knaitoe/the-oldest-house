# Success and failure in every vignette: contests, choices and experiences (October 30, third draft)

The owner asked: "Can you ensure that each vignette has a success and failure state? Consider rewards/repercussions for all." On the drafts the owner added:
- "Look to the end of world cabin. Winning sometimes takes, too. Failing sometimes gives."
- "Not every fail state should haunt the reader, unless appropriately suited for it, like the Goatman."
- "The Goatman isn't just ooh spooky figure, either. He's mechanically intrusive. He steals food and knocks at doors."
- "The goatman just echoed his vignette."
- On the Last Stop: "The elk entity in that story punished its protagonist for harming animals. That might be better?"
- "Harrigan's phone also already has an intended victory loop. It doesn't need more, does it?"
- "What would the failure of backseat be? I'm not sure there's a way to win or fail that vignette. That and a few others can exist as experiences. Maybe offer players a singular artifact or enact some subtle change or haunting that only plays in the house."

The earlier drafts forced a failure onto every room. This draft sorts the 55 stories in the Witness pool into three kinds, each with its own proposal:

| Kind | What it is | What the proposals do |
|---|---|---|
| **Contests** (25) | The room has a real way to win and a real way to lose. | Make both outcomes trades, as at the cabin. A haunt follows the reader home only where the story is about something that follows. |
| **Choices** (6) | Several endings, none of them a loss. | Mostly complete already: the choices are the trade. |
| **Experiences** (24) | The reader goes through it; it is neither won nor lost. | Leave it as it is. Completing it gives one singular artifact, or a subtle change or haunting that plays only in the House. |

These are proposals for the owner to weigh. Nothing here is built except Usher (0.4.184, pushed), which the Usher section below recommends changing.

## Contests: outcomes as trades

**1. Every outcome is a trade.** At the cabin both answers cost and both give:
- **Offering everything** costs hearts and an arm. It gives the whole globe, which won't let its keeper die once a day.
- **Refusing** closes one of the reader's own rooms. It gives the cracked globe, which catches its keeper once.

Several rooms already work like this:
- **Winter Lake:** the dare is won by giving the ribbon back to the hand.
- **The Smoky Mirror:** it shows the true view for blood.
- **The Shallows:** throwing her costs the reader's strength.
- **The monkey's paw:** every wish gives and takes at once.

**2. A haunt is the room again, smaller, and only where the story follows.** The Goatman's haunt echoes his night. At the trailer he took a place at supper and knocked at the door to be let in. Home with a reader who let him in, he eats from their pack and tries the door they stand by. A haunt here follows that idea, not that behaviour:
- **Only where the story follows.** The story is about something that comes after the reader, not merely something frightening.
- **With its own room's play.** No haunt borrows the Goatman's food and doors.
- **Never wounding.** It may cost the reader things, but it never wounds them. It is private, and a later success in that room ends it.

**3. Failure comes from the story.** It is the room's own threat or wrong turn, never a timer bolted on.
- It resolves nothing that visit and gives no Witness credit.
- The reader can try again later.
- Rooms that kill keep their death and the Mother's keeping of belongings.

**4. Limits on what a room may take:**
- Never a House original, an artifact, an owner-bound item or another reader's things.
- Never a heart below the room's existing floor, and never anything in the Mother's custody.
- What a room keeps, it keeps where the reader can see it, and a later win gives it back.

**Blank sides are deliberate.** A room need not fill all four sides (win gives, win takes, fail gives, fail takes) when the story doesn't ask for them.

### The haunts that follow home: three rooms

| Room | What the room does | What follows the reader |
|---|---|---|
| **The trailer (Goatman)** | He shares the cousins' supper and knocks to be let in. | Exists: he eats from their pack, tries the door they stand by, and is glimpsed. |
| **The wet boards (Bly)** | The Lady holds the reader, carries them a pace ahead of her in the dark, and puts them in the lake. The story ends with her coming back for Dani. | At night near water the dark closes in. They are held a moment and carried a few steps toward the water, then set down at its edge, never in it. |
| **The looping field (Last Stop)** | Struck, the elk is wounded and the men say "You don't hurt them." In *The Only Good Indians* she comes back, years later, for the men who killed her and her calf. | She punishes what they do to animals (under the room below). It ends on a later night when they let her pass without raising a hand, with no animal killed since. |

### Usher: a recommendation

**What 0.4.184 has.** A reader still inside as the house falls is followed by a private scratching under their feet, every three to seven minutes, louder each day. It ends when they get out in time on a later night.

**Why it should go:**
- **Nothing follows in Poe.** The house sinks into the tarn and nothing follows the narrator out.
- **It repeats nothing the reader did.** The scratching echoes a sound from the room, but none of the night's play: the lid, the reading, the doors, the flight.

**Recommended trade:**
- **Fled (closed lid):**
  - Gives the stone (as now).
  - Takes the house: the Usher door no longer deals to that reader, as the cabin closes a room. They saw it go into the tarn.
- **Found her in the wing (open lid):** gives the stone and takes nothing. The open lid already cost the storm night.
- **Buried (fail):**
  - **Takes:** the tarn keeps what was in their hands. It lies on the bank by the causeway on the night they get out in time.
  - **Gives:** a leaf from the vault register with their own name in the burial column. It can be offered to the Mother.

The reading, the shared night and the flight stay as built; only `UsherHaunt` would go. **I won't change it until you say.**

### The contests, room by room

Each shows **now** (S success, F failure, R reward, X repercussion) and the proposed trade. "Soft" means the failure sets the reader back but costs nothing lasting.

**The trailer (Goatman).**
- **Now:**
  - S: the night counted right; R: the tally counter.
  - F: the door opened, or lost outside; X: he comes home with them.
- **Proposed:** nothing; complete.

**The small passage (Ted the Caver).**
- **Now:**
  - S: retraced the squeeze.
  - F: the choke blackout (soft).
- **Win gives:** the cave's breath: a few more seconds of air underwater, for good.
- **Win takes:** the cave keeps a hand on their line. The owner: "Taking every torch for Ted is far too much … have their spool winds occasionally unwind: a text blurb and a reduction or complete deduction of the spool's durability."
  - **The trail spool:** afterwards, now and then while they lay a trail with it in the halls, the winds unwind. A grey line comes: "The line pays out through your fingers, back the way you came."
  - **The cost:** the spool loses durability, usually a share of what is left, rarely all of it.
  - **The limit:** three unwindings, then the cave lets go.
  - **Never taken:** their torches, chalk and other navigation stay untouched.

**The counted lights (Holloway).**
- **Now:**
  - S: three rooms and the latch; R: the shield.
  - F: death; X: belongings to the Mother.
- **Proposed:** nothing; winning already costs the arenas.

**The wet boards (Bly).**
- **Now:**
  - S: waited in the cupboard.
  - F: held and dragged into the lake room (soft).
- **Win gives:** Flora's doll from the cupboard.
- **Fail:** the Lady follows (haunt, above).

**The looping field (Last Stop).** After *The Only Good Indians*, where the elk woman punishes the men for what they did to animals.
- **Now:**
  - S: escaped the field and read the clipping. A capture by the men or by the herd is its own ending.
  - Striking the elk wounds her and changes the men's words ("You hurt it, … You don't hurt them."). It costs nothing else.
  - There is no failure.
- **For every reader, the men's accusations read their real record.** Native statistics count the animals they have killed in their world.
  - A long record is named back to them: "Forty cows, …. Thirty pigs."
  - A reader who has killed none leaves the accusation faltering: "Never even shot a deer? Then why'd it look at you?"
- **Win gives:** the cap from the broken post.
- **Win takes:** the tab comes due: an emerald a drink from their pack, as far as it goes.
- **Fail (new):** striking her. The night resolves nothing and there is no clipping. She follows them home and answers what they do to animals:
  - **Waste:** the meat and hide of any animal they kill spoil as they drop. The men in the novel left meat on the hill.
  - **In kind:** for each animal they kill, one of their penned animals is gone in the morning, the gate open and hoof prints beside it. It is never a named or tamed companion. This is the harsh option and can be left out.
  - **The calf:** killing a young animal brings her that night. Their next sleep wakes them in the looping field, to walk out again.
  - **How it ends:** on a later night at the Last Stop they stand where she can see them and let her pass without raising a hand, with no animal killed since she began following (from the same statistics). That is how the novel's cycle ends: Denorah faces her and does not shoot.

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
  - It is already a trade, and the blow is its failure.
  - **Fail takes:** the boys' company. They go home, and the account says so.

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
  - S: closed the door on the other feed; R: the Smoky Mirror, which costs blood.
  - F: caught.
- **Proposed:** nothing; complete.

**The second apron (diner).**
- **Now:**
  - S: left after tasting the meal.
  - F: answering her.
- **Win gives:** the receipt.
- **Win takes:** a moment of nausea from what was on the plate.
- **Fail takes:** they leave in her black frost: frozen and slow for a few seconds, never hurt.

**The cellar shift (basement).**
- **Now:**
  - S: closed the cellar.
  - F: taken by the fold.
  - The cleaver is the room's own tool, used inside it; there is no reward.
- **Win gives:** the worker's broken blade.

**The coffin lid (Usher).** See the recommendation above.

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

**The last clock (Masque).**
- **Now:**
  - S: stopped the clock.
  - F: moving during the chime sends them back (soft).
- **Win gives:** the invitation.

**The Mother.**
- **Now:**
  - S: gave an original, or saved the dog (winning takes the original).
  - F: a ransom unpaid; X: she keeps what is not reclaimed.
- **Proposed:** nothing; complete.

**The last finger (monkey's paw).** The purest trade: every wish gives and takes. It is complete.

**The visitors at dusk (cabin).** The reference for trades. It is complete.

## Choices: several endings, none a loss

The choices are the trade. Each one gives and takes something different.

**Mr. Harrigan's phone.** The owner: "You can give Harrigan his phone or not … That should reflect in how the phone is handled. Maybe you can receive friendly affirming texts if you didn't bury him with it, sort of freeing him. If you gave him the phone, you let him carry the obligation and that's when the rot starts."
- **Now:**
  - **Buried** (he has his phone): his line stays open. One name a day by text or voicemail: a hostile species dies near the caller's Overworld bed at the next dawn, and a harmless or named creature brings him instead.
  - **Kept:** his line goes nowhere. A call or text to his number rings in the reader's own pocket.
- **Kept: he is freed.**
  - Now and then a kind text from his number comes into the reader's thread, though his phone is in their pocket.
  - The texts are short and in his voice, the man who sent the boy books and scratch tickets: "Read something today." "You did right by me."
  - He never takes a name.
- **Buried: he carries the obligation, and the rot starts.** The line takes names as now, and each name he carries rots a little more:
  - **First names:** his voicemail greeting slows and roughens.
  - **Then:** his replies in the thread arrive with letters missing, and the casket's smell is on the phone (flies by the reader at dawn after a name).
  - **At the last:** he no longer waits to be asked. Some nights the phone rings on its own, his breathing on the line, until they give a name or let it ring out.
  - The account follows where the rot has reached.

**The empty chair (séance).**
- **Now:** drove the family out, heard the medium, or left when asked.
- **Proposed:** driving them out also takes the family, who don't return for that reader. It gives the sitting record.

**The closing account (Stanley).**
- **Now:** settled (which pays the bill), signed, or walked out with the master key.
- **Proposed:** settling or signing gives the room's key tag, to match the key.

**The crawl beyond the roof (mapping).**
- **Now:** kept the things, or broke them. R: the photograph.
- **Proposed:** nothing; complete.

**The house in the third lake (lake house).**
- **Now:** three personal choices, each with its own finite keepsake.
- **Proposed:** nothing; complete.

**The house they remembered (family copy, old cabin).**
- **Now:** left when asked, refused, or heard his story.
- **Proposed:** leaving when asked gives the family's photograph.

## Experiences: gone through, not won

These rooms aren't won or lost. A reader goes through them, and they resolve on completion as now. Each gets one thing on completion, and only one:
- **A singular artifact** that belongs to that room alone.
- **Or a subtle change or haunting that plays only in the House**, in the manor or the halls. It never reaches the reader's own world, never blocks a route and is seen only by that reader.

Twelve already have their artifact and need nothing:

| Room | Its artifact |
|---|---|
| The recording (Phone in the Canoe) | the phone |
| The readers (Zampanò) | the leaves |
| Return to sender (Whale) | the originals and the envelope |
| The distant frame (plain) | the photograph |
| The recorded voices (Crimson Hall) | the cylinders |
| The broken light (elk fan) | the tooth and the bulb |
| The food in the hollow (Holy Rabbit) | the knife and the foot |
| The listener's signature (confession) | the blank journal |
| The eight loose pages (Devil's Rock) | the diary |
| The numbered doors (Wheel) | the key |
| The tape and the cut (Ghosts set) | the camcorder and the film |
| Borrasca | the compass and the patch |

Twelve have nothing yet. Each proposal echoes its own room:

| Room | Proposed |
|---|---|
| **Twenty seconds (backseat)** | **In the House:** now and then, in a manor mirror, something sits behind their reflection's shoulder, as it rose in the car's mirror. Gone when they turn. |
| **The model home** | **In the House:** one manor window shows a tree that isn't there from outside. |
| **The preserved cave (congregation)** | **In the House:** now and then a hall holds a single pew, turned to face the way they came; it is gone the next time. |
| **The cover (barn well)** | **Artifact:** a stone with the initials from the well wall. |
| **The call button (hospital)** | **Artifact:** a patient's bracelet from the night ward. |
| **The pattern (wallpaper)** | **In the House:** over the House days that follow, one manor wall takes the yellow pattern, a strip at a time, and the pattern moves at the edge of sight. |
| **The returned knocks (Hill nursery)** | **In the House:** pounding comes now and then on the manor wall nearest them. Knocking back the same count on that wall answers it, as in the nursery. |
| **The little rooms (miniatures)** | **In the House:** a small figure of them stands on a manor shelf, moved each day to where they last stood. |
| **The upper ledger (Winchester)** | **In the House:** a door in a manor wall that opens onto plaster. |
| **The third count (third-floor bathroom)** | **Artifact:** the stub of the dare's candle. |
| **Yellow dust (silo)** | **Artifact:** a vial of the dust. |
| **The disappearing exits (child's room)** | **In the House:** one of the toys from the ceiling sits on the manor stairs, a step higher each day. |

## The artifacts and what each is for

The owner asked for the intention of each artifact. Every artifact, existing or proposed, shares these rules:
- **Bound to its reader.** It works, or means anything, only for the reader it was left for.
- **Finite.** At most one per outcome, and never restocked.
- **Offerable to the Mother** as a vignette artifact. Even a keepsake has weight: it is something to give up.
- **No Witness credit.** Holding or using one never counts toward the Witness.

Each serves one of three intentions:
1. **A tool: the room's lesson, made usable.** What the reader learned in order to get through the room keeps working for them afterwards.
2. **A keepsake: the room's story, to carry and reread.** Pages, records, photographs.
3. **A price: what winning took, or what failing left.** The arm given at the cabin, the ribbon handed back, the cracked globe.

### Artifacts in the game now

| Room | Artifact | What it does | Intention |
|---|---|---|---|
| **The trailer (Goatman)** | the tally counter | Tool. In the House it counts what is really there, shows what hides or pretends, and clicks when something comes up behind its keeper. Outside, it keeps count while they sleep and wakes them when something comes close. | The night was won by counting, not by seeing. The reward is the counting itself, turned against every impostor and ambush to come. |
| **The counted lights (Holloway)** | the shield | Tool: a real shield with 336 uses. | The hunt's survivor keeps what got them through, for the threats deeper in, such as the Minotaur's charge. |
| **The painted sun (boarded house)** | the Bone Ring | Tool. In the off hand, it outlines nearby people through walls and shows their intentions. Other players read as Unknown. | The painted family pretended to be people. The ring tells who is really in a room and what they mean to do. |
| **The empty mattress (split-feed bedroom)** | the Smoky Mirror | Tool and price: two hearts buy twelve seconds of true sight. | The room was about a feed that couldn't be trusted. The mirror is the true one, and, as in the room, looking costs something. |
| **The coffin lid (Usher)** | a piece of the house | Tool. Held to the ear, it makes the nearest hidden living thing within 32 blocks knock back, once a minute. | Roderick heard everything the house hid. The reader keeps that hearing, for what hides in the House and in their world. |
| **The visitors at dusk (cabin)** | the whole globe or the cracked globe; the grasshopper jar | Tool and price. The whole globe won't let its keeper die, once a day. The cracked globe catches its keeper once, while its water lasts. | The cabin's trade made into protection. Giving everything (an arm, hearts) buys more of it than refusing does. |
| **The last finger (monkey's paw)** | the paw; the purse | Tool and price. The paw wishes, a finger at a time, and every wish comes true wrong. The purse is the first wish's compensation. | A gain always comes with its cost in the same hand. |
| **Return to sender (Whale)** | the originals; the self-addressed envelope | Tool. In the halls the envelope returns its sender to the manor, once a day. Outside, it holds one sealed stack that waits for them if they die away from the House. | Letters that came home bring their writer home: a way back, and one thing kept safe. |
| **The house in the third lake (lake house)** | the flashlight, the house light or the gold helmet (one per choice) | Tool, working only outside the House. The flashlight's beam never falters, even under water. The light, hung in their world, marks where they come home. The helmet is worn. | Tess's gifts, one for each way the reader answered her. Each is about coming back up out of dark water. |
| **Mr. Harrigan's phone** | his phone | Tool. Kept, it frees him; buried, it starts the rot (Choices, above). | The line to the dead, and what the reader chose to do with it. |
| **The tape and the cut (Ghosts set)** | the camcorder; the film | Tool and keepsake. The camcorder records only what its holder really sees. The film plays the reader's own cut in a jukebox. | The reader was the camera operator. They keep the camera and their own cut of the story. |
| **The recording (Phone in the Canoe)** | the phone | Tool: it records clips. It is recovered from the cave. | The clip is the memory, and the phone is how to make more. |
| **The readers (Zampanò)** | his leaves | Keepsake: each leaf can be lifted, read and turned over. | The archive is theirs to reread. |
| **The recorded voices (Crimson Hall)** | three wax cylinders | Keepsake: they play in a jukebox. | The voices go home with them, to be heard again. |
| **The distant frame (plain)** | the photograph | Keepsake: the frame, with the vulture. | Proof of the frame they held still for. |
| **The crawl beyond the roof (mapping)** | the photograph | Keepsake. | What the crawl gave back of the brother. |
| **The broken light (elk fan)** | the tooth; the bulb | Keepsake. The bulb is the repair's. | The repair finished, and the elk's mark carried out. |
| **The food in the hollow (Holy Rabbit)** | Father's knife; the rabbit's foot | Keepsake. | What Father left at the end of the trail, and what the rabbit gave. |
| **The listener's signature (confession)** | the blank journal | Keepsake and tool: a journal to write in. | The listener signed for the reader. The reader gets pages for their own words. |
| **The eight loose pages (Devil's Rock)** | the diary | Keepsake: its eight pages, to reread. | The whole account in one binding. |
| **The numbered doors (Wheel)** | the key | Keepsake: the key to Mother's room. | The last door opened, kept. |
| **Borrasca** | the compass; the patch | Keepsake: the same originals used on the trail and at the sorting table. | Carried evidence of the names restored. |
| **The dare (Winter Lake)** | the ribbon | Price: it goes back to the hand to win. | The dare is won by letting it go. |
| **The closing account (Stanley)** | the master key | Price: walking out with it leaves the hotel what it holds of the guest's. | Leaving on the reader's own terms, at a cost. |
| **The Mother** | the returned companion | Their own animal, back. | The thing they nearly lost. |

### Artifacts proposed here

Most proposed keepsakes are mementos on purpose. Their weight is that the Mother will take them, and keeping them plain leaves the rooms with tools special. A few get a small use where the story offers one.

| Room | Artifact | When | What it does and why |
|---|---|---|---|
| **The small passage (Ted the Caver)** | the cave's breath (not an item) | win | A few more seconds of air underwater, for good. The cave taught them to hold their breath. |
| **The small passage (Ted the Caver)** | the spool's unwinding (not an item) | win takes | Three times, while they lay a trail, the spool's winds unwind and it loses some or all of its durability. The cave keeps a hand on their line, then lets go. |
| **The wet boards (Bly)** | Flora's doll | win | Keepsake of the child they waited with. |
| **The looping field (Last Stop)** | the cap from the broken post | win | Proof they walked out of the loop. Worn as a cap. |
| **The passing boots (elk carcasses)** | a hunter's tag | win | Cut from the hide that hid them. |
| **The thirteenth witch (Costume Night)** | their number card | win | They were an entrant, not the thirteenth. |
| **The flashes over water (Movie Night)** | a ticket stub for *Teeth of Summer* | win | Admit one: they rowed out and back. |
| **The dare (Winter Lake)** | Bud's note, signed by all four | win | The boys' word that the dare was done. |
| **The service road (Camp Blood)** | a counsellor's whistle | win | Blown, it brings the reader's own companions to them, as counsellors call campers in. |
| **Proofrock** | a page of a student essay | win | The town's story in a student's hand, to read. |
| **The unhinged door (buzzer)** | the scrawled note | win | The warning that got them out, to reread. |
| **The cellar shift (basement)** | the worker's broken blade | win | The shift finished. |
| **The second apron (diner)** | the receipt | win | The meal, paid for. |
| **The coffin lid (Usher)** | a leaf of the vault register with their name | fail | The house wrote them down among its dead. A keepsake of the failure that the Mother will take. |
| **The floorboards (Tell-Tale)** | the old man's watch, wrapped in cotton | win | The sound that gave the narrator away, quiet in their pocket. It tells the time as a clock does. |
| **Hide-and-clap** | the folded blindfold | win | The game's cloth, folded away. |
| **The last clock (Masque)** | the invitation | win | Readable: the party they left alive. |
| **The empty chair (séance)** | the sitting record | drove them out | The record of the night the reader ended the sittings. |
| **The closing account (Stanley)** | the room's key tag | settled or signed | To match the master key: a guest who paid or signed keeps the tag. |
| **The house they remembered (family copy)** | the family's photograph | left when asked | The house they remembered, as it was. |
| **The cover (barn well)** | a stone with the initials from the well wall | experience | The wait, kept. |
| **The call button (hospital)** | a patient's bracelet | experience | The night on the ward. |
| **The third count (third-floor bathroom)** | the stub of the dare's candle | experience | The dare done. |
| **Yellow dust (silo)** | a vial of the dust | experience | The roof, opened. |

The eight House-only changes for experiences are not artifacts. Their intention is that the House remembers the room: each echoes it, seen only by that reader, in the manor or the halls.

## What changes from the earlier drafts

- **Kinds:** rooms are now sorted into contests, choices and experiences, rather than every room being given a failure.
- **New failures:** only the Last Stop's (striking the elk). Every other contest already has one.
- **Haunts that follow home:**
  - **Kept:** the Goatman's.
  - **New:** Bly's and the Last Stop's.
  - **Removed:** Usher's would go.
- **Harrigan:** the choice decides the phone: kept frees him (kind texts), buried lets the rot start.
- **House-only changes:** eight, all in experience rooms.
- **New artifacts and keepsakes:** about twenty, at most one per outcome:
  - four for experiences;
  - Usher's register leaf, given on failing;
  - the rest as gifts on winning a contest or choice.
- **Witness:** no change. Still 55 sources, 42 required, two kinds and three endings. Failure never grants credit.

## Suggested order, once agreed

1. **Usher's trade,** replacing its haunt. It is small and sets the pattern.
2. **The Last Stop rework:** the failure, the men's statistics and the elk's punishment.
3. **Harrigan's two phones:** the kept phone's kind texts, the buried phone's rot.
4. **Bly's haunt.**
5. **The eight House-only changes,** which share one private-view mechanism.
6. **The contests' trades,** a few rooms at a time.
7. **The keepsakes and artifacts,** in one batch with their sprites.

Each batch is tested and validated in CI before the next.
