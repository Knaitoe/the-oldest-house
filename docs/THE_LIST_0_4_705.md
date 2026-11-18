# The list, part 2: the slasher's body (0.4.705)

The second part of *The list* (`docs/THE_LIST_PLAN.md`): the body every slasher on the video store's horror wall has, built and tried
on its own before any night uses it. The House still does not deal Lamesa; the body is given by hand, in the town, with
`/ghosthouse test the_list body`. Night one (part 3) will give it to the first reader through the door.

## The body

The body is a reader's own. Their record keeps it (`Body`), and it is worn only while they are in the town; outside it, nothing of it
is left on them.

- **The walk.** The reader never sprints, by key or by a double tap. They walk all the time at the pace the game gives a sprint
  (three tenths over a walk), so nobody who is still in reach gets ahead of them.
  - The game widens the view for a fast walk as it does for a sprint. Here it does not: the walk's share is taken back out of the field
    of view.
- **Doors give.** A locked house door of the town, struck three times, splinters and swings open.
  - A blow is a hand on the door or a hit with the attack key. Blows must be at least eight ticks apart; a second blow sooner does
    nothing.
  - The game's own cracks show on the door after the first and second blows. A blow is heard by everyone near, and chips of raw wood fly.
  - At the third the door gives with a splintering crash. It becomes a broken door, both halves: split down the lock side, a hole
    punched through the panel by the knob, the knob knocked crooked, standing open. From then on anyone opens and shuts it by hand.
  - The reader's own house, its front door and the House's way on are not the town's to lock, and a blow there is not a blow.
  - Blows not followed up are forgotten after ten seconds.
- **Hearts.** Every heart near enough is heard where it beats, by the slasher alone, as a sound sent only to them.
  - Anyone else's heart is heard from forty blocks; the five on the list (Deek, Mel, Lesley, Shannon, Wes) from sixty-four, louder,
    and the nearest of the five louder still.
  - A heart beats about once a second. One that has seen the body in the last ten seconds races, quicker and higher. A sleeper's heart
    is slow.
  - Other readers in the town have hearts too.
- **The get-up.** In the town the body keeps a heart.
  - A blow that would take more takes only what is above that heart.
  - A blow that would kill puts the body down where it is, face down, for five seconds. It cannot move or jump, nothing more is taken,
    and nothing knocks it about. Then it gets up where it fell.
  - It is not a House death; nothing is lost. A plain kill (an operator's) and the void still kill.
- **The mask.** A feed sack over the head, worn by default.
  - It is drawn on the head at the skin's own size: burlap, its top corners standing up, tied off with twine at the neck. The eyeholes
    are cut out, so the reader's own eyes show through them. A mark for a nose and a crooked mouth are drawn on the front in grease
    pencil.
  - Seen from inside, it is burlap close over the whole view, laid over the screen as the game lays its pumpkin. Its threads go over and
    under, the night pricks through where they cross, and two eyeholes are cut crooked, their cut threads frayed into the holes. The
    thin strip between them is half clear, as two eyes see past it. This hides what a sack hides; like the blindfold, it does not follow
    the comfort settings.
  - It can be taken off and put on like any hat.
  - With it off, anyone in town within twelve blocks who looks at the reader has seen their face. The record keeps who (`Faced`), and
    the reader is told once for each. Part 3 draws the reader's own face on the sack, and later parts give the town's memory of the face
    its use.
- **The pull.** Toward the nearest of the five within ninety-six blocks.
  - It is drawn at the edges of the reader's own screen as a dark weight beating with a heart, heaviest on the side the name is on.
  - It is heard as that name's heart, louder than the rest.
  - Standing in light (a street lamp, a porch light, a lit window) takes most of it away, and looking away from the name halves it. The
    notebook (part 5) will resist it too.
  - It never moves the reader or their camera. It is a feeling, so it follows Distortion Effects, from nothing at 0.
- **The tread.** Everyone else in town hears the slasher's steps, heavy on the caliche; the slasher hears only their own footsteps.
- **Leaving.** Out of the town by the House's way, the sack is left on the back step, and the walk and the stillness go. Coming back,
  the reader puts the sack on again (or carries it, if they were carrying it), and the walk is theirs again. A sack let go of in the
  town lies where it fell until it is picked up; it does not rot away.

## Single and multiplayer

- **Alone**, the reader tries the body on in an empty town: the people at their places by night, every heart heard, every neighbor's
  door to try.
- **With others**, the body is one reader's.
  - Only they hear the hearts and see the pull.
  - The others hear the tread, the blows and the door giving, and they see the sack.
  - Their hearts are among those the slasher hears, and theirs race once they have looked at the slasher.
  - A door that has given stays given for everyone.
- **Spectators** are never given the body, hear none of it, and their hearts are not heard.

## Made as

- **`TheListBody`** (new): the body.
  - The walk: a transient `MOVEMENT_SPEED` modifier, `list_walk`, multiplying the total by 1.3, and no sprint on the server.
  - The stillness while down: `list_still` and `list_still_jump`, both multiplying by nothing.
  - `give`/`end`; `hold` each tick for each reader with the body in the town; `strip` for anyone who has left it.
  - Hearts: `ClientboundSoundPacket` sent to the slasher only. The pull: `TheListBodyPayload` every five ticks.
  - `strike` and `give` for doors.
  - The get-up: `LivingDamageEvent.Pre` at the lowest priority, `LivingDeathEvent` at the highest, `LivingKnockBackEvent`.
  - Sacks dropped in town never rot (`ItemTossEvent`).
- **`TheListSack`** (new item, `list_sack`, "Feed Sack"): worn on the head, marked with its reader's id.
- **`TheListRegistry`**:
  - `BROKEN_DOOR` (`list_broken_door`, "Broken Door").
  - `SACK`.
  - Six sounds, each subtitled: the heart ("A heart beats"), the strike ("A door is struck"), the splinter ("A door splinters"), the
    sack ("Burlap rustles"), the tread ("Heavy footsteps"), going down ("A body hits the ground").
- **`TheList.click`**: with the body, a hand on a locked door is a blow.
- **`TheListCommands`** (new): `/ghosthouse test the_list body`, `body off` and `status`, for operators.
- **Client**:
  - `TheListBodyClient`: the sack's inside, the pull, the dimming while down, the prone pose, and the field of view.
  - `TheListSackLayer`: the sack on the head, for every player model.
  - `mixin/ListWalkMixin`: `LocalPlayer.canStartSprinting` is false with the walk.
- **Network**: `TheListBodyPayload` (S2C); the protocol is 202.
- **Art** (`tools/draw_the_list_art.py`):
  - The sack on the head (`entity/the_list/sack.png`, 64 × 32 on the head's own box layout).
  - The sack held (`item/list_sack.png`).
  - The sack from inside (`gui/list_sack_inside.png`, 128 × 64, the weave three texels a thread with a texel between).
  - The broken door's two halves, with its model and states.
  - The art check now also proves that the eyeholes are cut through, that the middle of the inside is more clear than not, and that the
    broken door is holed.
- **Sound** (`tools/generate_the_list_audio.py`): the six above, synthesized.

## Tests

`TheListTests.theSlashersBody` (new), with a second reader in town who never has the body:

- Given in the town, it is the one reader's own. The speed is a sprint's pace, and the reader never sprints.
- The sack is on.
- Hearts:
  - the slasher hears them, and the other reader hears none;
  - the pull is sent to the slasher, toward Shannon on the bench, and to nobody else.
- Shannon's heart races once she has the body in sight.
- With the sack off, she has seen the face, said once and kept.
- The tread is heard by the other reader and not by the slasher.
- A neighbor's door:
  - one blow, then a second too soon that does nothing;
  - the second blow splits it, and the third gives it, open and broken, both halves;
  - the other reader's hand on it is a door's.
- A non-lethal blow leaves a heart. A lethal one puts the body down: still, nothing more taken, where it fell. It gets up after five
  seconds, the walk back.
- Out of town nothing of it is left and the sack is on the back step; back in town the sack goes on again.
- Ended, nothing is left.
- A spectator is not given it.

## Found and fixed

- The sack's drawn face first read as a friendly smile, and then, with a fringe and a mouth stitched shut, as hair and a moustache on
  the face under it. It is now only a mark for a nose and a crooked mouth, the line lifted twice, with the eyeholes cut uneven.
- The inside at 64 × 32 read as noise once stretched across a screen, and the flat bands above and below it showed their edges. It is
  drawn at 128 × 64 with a real over-and-under weave and frayed cut edges, and laid over the whole screen.

## Every new line

Grey cues on the action bar, to the slasher alone:

- Given: "A feed sack over your head, the eyeholes cut crooked. Every heart in town is loud."
- The blows: "The door takes it. Inside, somebody turns the television down." / "The frame splits along the lock." / "It gives. The
  door swings in on what is left of the frame."
- Down and up: "You go down and stay down." / "You get up where you fell."
- The face: "%s has seen your face." (`%s` is the name the town's people go by in their captions, or "The deputy".)
- Leaving and coming back: "You leave the sack on the back step." / "The sack is on the back step. You pull it on." / "The sack is on
  the back step. You take it with you."

The item's name, "Feed Sack", and the block's, "Broken Door", are new too.
