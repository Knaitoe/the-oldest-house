# Prose review — This House Has Ghosts in It

Reviewed 18 November 2026. Source snapshot **0.4.714**, commit `654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5`.

This document records proposed edits to player-facing writing. The review concentrates on the opening, household notes, character-defining rooms, correspondence, selected vignette dialogue and papers, accounts, and subtitles. It is a broad editorial sample. Source extraction covered the downloaded text modules and English language file, but the complete mod has additional dialogue in scene controllers and art. Those have not all received a line-by-line edit here. Timing, book pagination, and the experience of reading during play have not been playtested for this review.

## Assessment

The mod’s strongest prose gives an ordinary object a history and then changes one thing about it. The measuring notes, the cracked cup in Rowan’s flat, Opal’s bean, and the letters about a dead partner all do this well. Concrete work also distinguishes people. Harlan thinks about opening his store, Rowan worries about a missing screw, and Casey keeps trying to make evidence fit the weekend he planned.

The most useful revision would reduce the moments where the writing explains its own effect. Several passages conclude with a polished judgment after the scene has already made the feeling legible. Other passages give a narrator knowledge they have no visible way to obtain. The effect becomes more noticeable when multiple letter writers and NPCs use it.

Preserve the range of intensity. The bodily horror in the cellar and diner has a place beside the quieter domestic writing. The editorial question is whether each detail sharpens the image, establishes a speaker, or changes what the reader understands. Shortening every line into fragments would flatten that range.

Start with the copy edits and clarity changes below. The remaining suggestions are choices of voice, emphasis, and revelation, with alternative wording supplied for review.

## Suggested changes

All source links point to the reviewed commit. The quoted originals are exact excerpts. A proposed replacement applies to that excerpt unless the entry specifies a wider paragraph edit.

### 01. Correct the verb in the bedroom-camera note

**Copy edit** · [UncannyTexts.java — log(): case MISSING_SLEEP->](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/UncannyTexts.java#L54)

**Original**

> I never laid down last night.

**Suggested wording or cut**

> I never lay down last night.

The narrator means that they did not lie down. “Lay” is the simple past form needed here. The surrounding movement between last night and the present can stay.

### 02. Use the intended physical verb

**Copy edit** · [UncannyTexts.java — log(): case MISSING_SLEEP->](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/UncannyTexts.java#L54)

**Original**

> his arms are jetted straight out from his shoulders

**Suggested wording or cut**

> his arms jut straight out from his shoulders

“Jetted” suggests propulsion or liquid. “Jut” makes the marionette posture immediately legible.

### 03. Normalize the injured man’s capitalization

**Copy edit** · [CellarDialogue.java — cut()](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/CellarDialogue.java#L54)

**Original**

> I can't see. it still hurts. please.

**Suggested wording or cut**

> I can't see. It still hurts. Please.

The surrounding spoken lines use ordinary capitalization. The small letters here look accidental in a displayed line. The short sentences already convey his distress.

### 04. Clarify the occupied car in the State Route advancement

**Clarity** · [en_us.json — advancements.the_oldest_house.account.state_route.description](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/assets/the_oldest_house/lang/en_us.json#L1196)

**Original**

> I crossed four lanes of a state route on foot in the rain for a key in a mailbox, and came back between cars with nobody in them but one.

**Suggested wording or cut**

> I crossed four lanes in the rain for a key in a mailbox. Coming back, I found every car empty but one.

The ending of the current sentence can mean one person among otherwise empty cars. StateRouteTexts.account says that one station wagon has every seat occupied. The suggested wording keeps that distinction and shortens an already dense advancement.

### 05. Give the extra name its own emphasis

**Clarity** · [en_us.json — advancements.the_oldest_house.account.last_crossing.description](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/assets/the_oldest_house/lang/en_us.json#L1274)

**Original**

> I worked three days and three nights at a flood station with a crew, for a keeper who called the roll every morning and one name more at the end of it.

**Suggested wording or cut**

> I worked three days and nights at a flood station. Each morning the keeper called our names, then one more.

The extra name is the disturbing detail. The current chain of clauses delays it and makes “called the roll ... and one name more” harder to parse.

### 06. Make the digging subtitle readable at a glance

**Clarity** · [en_us.json — subtitles.the_oldest_house.fallow.dig](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/assets/the_oldest_house/lang/en_us.json#L2135)

**Original**

> Rubble is dug at overhead

**Suggested wording or cut**

> Digging through rubble overhead

“Is dug at” is an awkward passive construction for a brief sound caption. Keep “overhead” because it carries useful spatial information.

### 07. Keep the victim attached to the sentence

**Clarity** · [TheListTexts.java — INTO_FRYER](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/TheListTexts.java#L186)

**Original**

> You force %s down over the fryer and push the face into the oil.

**Suggested wording or cut**

> You force %s down over the fryer and push their face into the oil.

After the named person, “the face” sounds like an unattached prop. “Their face” keeps the action physically clear and preserves the violence. Singular “their” also works across the named cast.

Retain the single %s substitution.

### 08. Resolve the sprinkler’s ambiguous anatomy

**Clarity** · [TheListTexts.java — ONTO_RISER](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/TheListTexts.java#L163)

**Original**

> You force %s back onto the riser, and its head comes up through the front.

**Suggested wording or cut**

> You force %s back onto the riser. The sprinkler head pushes through their body.

“Its head” and “the front” force the reader to resolve both the object and the body part during a fast action. The release record describes the head emerging through the front of the victim.

Retain the single %s substitution.

### 09. Describe the dwindling food directly

**Clarity** · [FallowTexts.java — SHELF_DAYS](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/FallowTexts.java#L320)

**Original**

> The shelf is thinner. The dates on the lids go back eleven summers.

**Suggested wording or cut**

> Fewer jars on the shelf. The dates on the lids go back eleven summers.

The shelf itself appears to shrink in the current wording. That could suggest a supernatural event in this mod. The scene is showing supplies being used up.

### 10. Untangle Vera’s conditional sentence

**Clarity** · [LodgingTexts.java — VERA_SENT_ASK](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/LodgingTexts.java#L257)

**Original**

> What's in your bed I won't touch if you won't have it touched. So it'll be you.

**Suggested wording or cut**

> If you won't let me touch what's in your bed, you'll have to do it yourself.

The current front-loaded object and repeated “touch” require a reread. The next line already specifies that the player must come to the frame and hold him by the shoulders. The revision keeps Vera’s coercive bargain.

### 11. Attach the repair to the crack

**Clarity** · [LongerTexts.java — CUPS](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/LongerTexts.java#L31)

**Original**

> One has a crack down from the rim that has been glued.

**Suggested wording or cut**

> One has a glued crack running down from the rim.

The current relative clause can describe the rim as glued. This is a small edit, but the damaged cup becomes an important emotional object later.

### 12. Let the waiting-room objects carry the age association

**Editorial option** · [HouseReadTexts.java — WAITING](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/HouseReadTexts.java#L17)

**Original**

> Folks who remember when the afternoon used to be long just find a chair with good springs, fold the paper back to the local page, and let the clock do its own work. Folks who came up with a wire in their pocket don't know what to do with their knees when a door is shut—they’re up and down the rug before the minute hand even clears the twelve, looking for a piece of glass that’ll glow back at them.

**Suggested wording or cut**

> Someone has folded the paper back to the local page. The chair beside it has good springs. A phone has been left on the other chair's armrest. The clock keeps the same time for both.

The current note gives the two groups a moral and behavioral contrast before the player chooses anything. It also explains what the character-defining interaction is reading. Keep the first sentence about time moving at the speed of wallpaper, then let the objects invite the choice.

This is a proposed rewrite of authored prose. Preserve the established age-reading behavior and the player’s ability to choose either object.

### 13. Remove the certainty imposed on the drawing choice

**Editorial option** · [HouseReadTexts.java — KITCHEN](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/HouseReadTexts.java#L26)

**Original**

> Nobody has to say a word. The hands already know what color they've always been.

**Suggested wording or cut**

> The back of the sheet is covered with little trial marks.

The current ending assumes that the child already knows which crayon must represent them. The preceding paragraph’s trial marks offer a more open image and suit a scene where the player is defining a character. This replacement retains the activity without deciding what it means for them.

### 14. Include the shelter’s solitary possibility

**Editorial option** · [HouseReadTexts.java — SHELTER](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/HouseReadTexts.java#L19)

**Original**

> some stand right in the middle of the floorboards and hope neither one of them goes home before the rain stops.

**Suggested wording or cut**

> some stand right in the middle of the floorboards and hope neither one of them goes home before the rain stops. Someone else goes out to the lantern, where the rain is easier to hear.

The note currently describes attraction to the boy, the girl, or both. HouseRead also supports NO_ONE, and HouseReadProse explicitly records going to the lantern. A final image would give that route equal presence without assigning loneliness or rejection to it.

The additional sentence is optional. Avoid turning the whole note into a list of controls or named personality states.

### 15. Reduce the waitress note’s certainty about the monster

**Editorial option** · [UncannyTexts.java — log(): case SECOND_WAITRESS->](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/UncannyTexts.java#L38)

**Original**

> People think she’s the ghost of the Miller girl who died in the ditch four years back. She isn't. That girl is deader than dead; something snatched her soul out of the air and ate it on the way up, and now whatever is left is wearing her uniform.

**Suggested wording or cut**

> People think she’s the Miller girl who died in the ditch four years back. I’ve seen the photographs. In those, her smile stops where it should.

The current passage gives the writer certain knowledge of a soul being intercepted and eaten. It explains the entity before the player can encounter her. The photograph comparison lets the later salt-shaker and splitting-mouth details establish what has changed.

Treat this as a replacement for the explanation and the following photograph/smile discussion through “unrecognizable.” The full paragraph should be edited together. Preserve the physicalization timings and the note’s warnings.

### 16. End the motion-light image on observable behavior

**Editorial option** · [UncannyTexts.java — log(): case MOTION_LIGHT->](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/UncannyTexts.java#L74)

**Original**

> waiting out the sensor's timer so you can't see what they’re doing to their face.

**Suggested wording or cut**

> waiting out the sensor's timer.

The writer can observe somebody holding still. The final explanation assigns a private purpose and an unseen facial action to that stillness. Ending at the timer leaves the reader with the person beyond the glass and the imminent loss of light.

### 17. Make the floating movement internally consistent

**Editorial option** · [UncannyTexts.java — log(): case MISSING_SLEEP->](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/UncannyTexts.java#L54)

**Original**

> His feet don't touch the carpet—they glide an inch above the clutter, tiptoeing his jerking body toward my parents' room.

**Suggested wording or cut**

> His feet don't touch the carpet. They glide an inch above the clutter while his body jerks toward my parents' room.

“Glide,” “tiptoeing,” and “jerking” compete for the same motion. Keeping the smooth feet and jerking body preserves the unnatural contrast while removing the implied foot contact.

### 18. Give Johnny a natural refusal response

**Editorial option** · [TruantTexts.java — DECLINED](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/TruantTexts.java#L33)

**Original**

> All right. You don't owe me it.

**Suggested wording or cut**

> All right. You don't owe me an answer.

“Owe me it” is conspicuous beside the easy, practical hospitality of his bread and coffee lines. A clear early reassurance also gives his later claim on the player’s story something exact to betray.

### 19. Keep the worker’s warning in his physical vocabulary

**Editorial option** · [CellarDialogue.java — touched()](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/CellarDialogue.java#L28)

**Original**

> Don't - don't let him touch you, he'll consume you too.

**Suggested wording or cut**

> Don't—don't let him touch you. You'll go in with Marcus.

Elsewhere the worker speaks about steps, nails, the hip, and Marcus. “Consume” sounds more like an explanation of the monster. Naming Marcus makes the warning specific to this room and gives the stammer a consistent mark.

If this line can occur before the player has heard Marcus’s name, “He’ll pull you into him” is the clearer final sentence.

### 20. Make Wendell’s exhaustion easier to hear

**Editorial option** · [KeptTexts.java — WALKING](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/KeptTexts.java#L58)

**Original**

> I'm walking as fast as I'm let.

**Suggested wording or cut**

> I'm going as fast as it'll let me.

The unusual passive “as I’m let” draws attention to the wording. The revision preserves the implication that the machine controls even his pace.

### 21. Let Casey confess in the same defensive voice he has used all night

**Editorial option** · [PinehookTexts.java — CASEY_CONFESS](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/PinehookTexts.java#L213)

**Original**

> I set all of it up: the knocking, the breaker, Jude in the costume. Not the shed or the knife. Somebody else did that, and I helped them by keeping my mouth shut.

**Suggested wording or cut**

> The knocks were mine. The power, Jude, all of that. The knife wasn't. I knew that, and I kept telling you it was fine.

Casey spends the scene minimizing concrete evidence. His current conclusion summarizes his moral responsibility with unusual composure. The revision makes him admit the specific lie he kept repeating. Keep the preceding “Everybody listen. The mask was mine.”

### 22. Name what the reader can see Daniel holding

**Editorial option** · [LodgingTexts.java — ATTEMPT_SEEN](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/LodgingTexts.java#L143)

**Original**

> Daniel stands over Dan with it raised, and does not bring it down.

**Suggested wording or cut**

> Daniel stands over Dan with the heavy object raised. He does not bring it down.

An action-bar cue can appear long after the exchange that supplied the object. “It” relies on the player remembering that referent during a confrontation.

Prefer the actual held item’s display name if the prose system already exposes it. “The heavy object” is a wording-only fallback. Review the rendered line in context before choosing.

### 23. Give the cylinder instructions a household speaker

**Editorial option** · [LiteraryTexts.java — source()](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/LiteraryTexts.java#L36)

**Original**

> Three women left cylinders in different rooms. Play all three on the hall's jukebox. Let each finish before replacing it.

**Suggested wording or cut**

> I found a cylinder in each woman's room. There are three. I've left the player in the hall. Listen to the end of each before you change it.

The current housekeeper’s document shifts into the language of a puzzle objective. The suggested wording retains the number, the hall location, and the need to finish each recording, while giving the writer a reason to have left the note.

Keep the following upstairs/balcony clue. If “jukebox” is needed to identify the visible object, retain that noun in place of “player.”

### 24. Let the shared-downstairs ending finish with the couple

**Editorial option** · [PeopleLightsTexts.java — ended(): case "shared_the_downstairs"->](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/PeopleLightsTexts.java#L38)

**Original**

> Neither looks up when you go. That's the point.

**Suggested wording or cut**

> Neither looks up when you go.

The table, work case, cards, and their attention to each other already show the change. “That’s the point” steps outside the scene to interpret it for the player.

### 25. Keep one F-chain letter close to the shift worker’s immediate thoughts

**Editorial option** · [letters.json — F02.text](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/data/the_oldest_house/correspondence/letters.json#L245)

**Original**

> I cannot name anything wrong with today. I would still pay someone to tell me it was over.

**Suggested wording or cut**

> Nothing to put in the book. I keep checking the clock.

The original is an effective aphorism. Across the correspondence, many different writers arrive at equally polished closing insights. Using plainer language here would help distinguish the occurrence-book worker from the more literary senders.

This is a voice-variation option, not a correctness fix. Keep the surrounding clock/radio detail and the chain’s progression toward the sign-out trap.

### 26. Clarify what the party host fears making worse

**Editorial option** · [letters.json — H03.text](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/data/the_oldest_house/correspondence/letters.json#L476)

**Original**

> I wanted to put more on his plate but couldn't think how to do it without making him worse.

**Suggested wording or cut**

> I wanted to put more on his plate, but he'd have apologised for that too.

“Making him worse” leaves the object of the worry vague. The suggested sentence keeps the host’s concern attached to Martin’s embarrassment and repeated apologies.

### 27. Consider removing one explanation from “Kindness”

**Editorial option** · [letters.json — B03.text](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/data/the_oldest_house/correspondence/letters.json#L410)

**Original**

> It is difficult to forgive someone for seeing you need help.

**Suggested wording or cut**

> Delete this sentence and let the next sentence follow the washing scene.

The complaint that the same water is too hot and too cold already gives the reader the speaker’s wounded pride. The later apology and “I was tired. She stayed” carry the emotional turn. Removing the abstract explanation would let those details do more.

A discretionary cut. The sentence is clear and may be worth keeping if the mother’s habit of explaining herself is intentional.

### 28. Keep the staircase’s interpretation proportionate to the recorded act

**Editorial option** · [StaircaseProse.java — KINDS](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/StaircaseProse.java#L112)

**Original**

> You ate {n|slice|slices} of cake. Once, there was reason to celebrate.

**Suggested wording or cut**

> You ate {n|slice|slices} of cake. The crumbs are gone. The count remains.

Eating cake does not establish that the player celebrated. This alternative lets the House keep an intimate, invasive record without supplying an occasion that the player never chose.

If the House’s presumptuous interpretation is deliberate, keep the current line as characterization. Review the same choice in “Care is in your record” and “Small kindnesses count too” before making a global rule. Preserve the plural token exactly.

## Passages to protect

- **Will and Karen’s measuring notes.** “He said houses don’t settle bigger” and Karen holding Daisy’s hand while they count give the impossible measurements a domestic cost. The opening’s invitation, photograph, and growing discrepancy already establish unease. Additional explicit menace would be unnecessary. [HouseNotes](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/house/HouseNotes.java#L23) · [NavidsonLetter](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/opening/NavidsonLetter.java#L45)
- **Rowan’s competence and embarrassment.** The missing screw, the bad toast, and “I wanted a minute where it was just a broken radio” let the relationship develop through shared work. Keep the joke about being good at wiring and the practical detail of the cup. [LongerTexts](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/LongerTexts.java#L122)
- **Fallow’s differing responses to catastrophe.** “I keep wanting to go up and open the store,” Faye’s repetition about the steel rig, and Opal sharing her water with the bean give each person a separate way to cope. The repetitions here change meaning as the situation worsens. [FallowTexts](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/FallowTexts.java#L358)
- **Dan and Daniel’s competing claims to a life.** Dan carrying the neighbor’s dog and Daniel mending the chair establish personhood through lived time. Preserve the uncertainty in who deserves what. [LodgingTexts](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/LodgingTexts.java#L80)
- **The grief correspondence.** “Four minutes” ends on a continued habit, and “Cherries” ends on an ordinary action the writer can finally take. Both trust the reader. [E04](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/data/the_oldest_house/correspondence/letters.json#L652) · [E06](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/data/the_oldest_house/correspondence/letters.json#L1048)
- **“Garden report.”** Its paragraph initials spell **LET ME OUT**. Preserve those initials, their order, and the paragraph boundaries during any copy edit. Its follow-up, “Correction,” complicates the apparent rescue request. [B04](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/data/the_oldest_house/correspondence/letters.json#L619) · [B05](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/resources/data/the_oldest_house/correspondence/letters.json#L817)
- **The transition from Johnny’s help to his claim on the story.** Bread, coffee, and a held door make the later “Half of it’s mine” accusation personal. Maintain those concrete favors and their callbacks. [TruantTexts](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/TruantTexts.java#L20)
- **The List’s small-town objects.** Sandburs in the blanket, bugs striking the lantern, and the seed house bulb going out do more to establish place than an additional paragraph describing an ominous town would. [TheListTexts](https://github.com/Knaitoe/the-oldest-house/blob/654e6bdec59837ffdcc6db2ca8e599eaa7a8ecf5/src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/TheListTexts.java#L313)

## Guidance for the next editorial pass

**Keep speech attached to a person’s immediate concern.** Casey’s denial, Harlan’s work habits, Rowan’s repair talk, and Vera’s household authority offer distinct models. Review a run of one character’s lines before changing an isolated sentence. Familiar phrases such as “All right,” “Go on,” and “For what that’s worth” are ordinary speech. Concentration and placement determine whether they begin to erase individual voices.

**Let evidence earn an explanation.** The waitress’s changing weight and grip can establish a threat before a note explains its cosmology. In a story whose moral choice depends on consequences, such as the lodging house, the player still needs clear information. Preserve that information when editing the delivery.

**Give found documents a reason to exist.** A maintenance instruction can be direct. A receipt, private diary, invitation, or letter needs a writer with a purpose appropriate to that object. The cylinder note is one place where the wording can better connect the required action to its author.

**Allow different kinds of sentences.** The mod uses fragments effectively for inspections and panic. Longer sentences are valuable in confessions, letters, and a person trying to explain themselves. Review the repeated progression of observation, reversal, and final aphorism across neighboring documents. Cutting a few explanatory endings will create more variation than shortening everything.

**Preserve player authorship where the scene offers it.** The quiet-room notes can make a behavior legible without telling the player what people of an age, appearance, or disposition always do. A hostile narrator may deliberately misread the player. Keep that intention specific to the speaker and situation.

**Read conditional accounts as complete pages.** The source often assembles several strings according to personal and shared events. An edit to one fragment must still join correctly with every adjacent branch. Preserve names, substitutions, counts, and the distinction between the player’s act and something another participant did.

## Coverage and implementation notes

Close reading and targeted sampling included NavidsonLetter, HouseNotes, MarginaliaTexts, SpeakUp, HouseReadTexts and HouseReadProse, TruantTexts, CellarDialogue, UncannyTexts, LongerTexts, FallowTexts, TheListTexts, PinehookTexts, LodgingTexts, KeptTexts, CornerTexts, LastOrdersTexts, StillnessTexts, DeadAirTexts, StateRouteTexts, LiteraryTexts, StaircaseProse, and the authored correspondence. Additional sampling covered HotelTexts, LastCrossingTexts, PellTexts, PeopleLightsTexts, PlaceForYouTexts, BorrascaTexts, KingTexts, and the English advancements and subtitles. The recommendations concern the cited passages. They do not certify every line in those modules.

The review uses the current source strings for its proposals. Release records for The List and Fallow supplied additional scene context. Imported novel quotations were outside the editing scope. Some source comments identify prose as the owner’s approved text, so the proposed alternatives should be treated as editorial choices for the owner to accept or reject.

If these edits are adopted, check the source string, any generator that holds the same wording, and tests that assert it. Preserve `%s`, `{reader}`, `{possession}`, `{companion}`, and plural tokens exactly where they are used. Respect the repository’s House-writing rules for reader address, colors, and ages when composing new prose. Check book pages in the authored font and action-bar cues at the normal reading speed. No conclusion about layout or in-game pacing is claimed by this source review.

The review is stored on `docs/player-prose-review-0714` to make the suggestions available together. The proposed replacements are documented for consideration.
