# Reworks, 0.4.121

The fifth batch of the owner's notes: ten reworks, each planned and approved before it was built ("Batch 5 plans sound
great. Run with them."). They are built in four parts. Nothing here changes the Witness pool: 52 sources / 39
resolutions / two kinds / three endings.

## Part one: Miniatures, Confession, Karen's room, the phone in the canoe

### Miniatures

- **Cards.** A touch on a model stand (the three at (-8|0|8, 1, -18)) shows that model's card in the chat, in grey
  italics. It names the reader and the room (`StaircaseProse.place`, or `UncannyTexts.title`), then says only what the
  House recorded about them there, in the third person and with no pronoun guessed:
  - how often they came and how long they stayed the last time (literary rooms' `Visit` and `Present`);
  - how many rooms back it was (`LabyrinthData.recentVisit`);
  - whether they read what was left out (`Read_Source`);
  - what they did, from their own recorded outcome (`Outcome`, or the Witness record for other rooms), through a table of
    third-person phrases (`MiniatureNotes.PHRASES`), with unlisted outcomes read as their own words;
  - how it ended: the last account read, left where it lay, or left before it was finished.
  A model of a room still ahead says the reader has not been there. The room crossed alone (the model with the second
  figure) adds that no one came in with them. Reading a card sets a bit in `CardsRead`; it grants nothing.
- **The mother.** After twenty seconds in the room (`MOTHER_AFTER`, the visit's own ticks), the server names the room's
  four upper corners to that reader's client, inside the models payload. The client (`MiniatureMotherClient`) may then
  put her in one corner whose whole body already sits at the edge of the screen (74–97% of the half-width or height) with
  a clear line to it: face down on the ceiling, gown hanging, arms reaching past her head, face lifted to the room, lit by
  the corner's own light. A faint scuttle plays. She is gone the moment her head or middle comes inside 58% of the screen,
  or after a second and a half, and waits five to twelve minutes, longer each time, before the next. She is drawn only on
  that client; no entity exists.
- **Her skin** is painted on the shared character grid (`tools/paint_character_skins.py`, `literary_miniature_mother`,
  mark `possessed`: sunken sockets with the whites showing, a bloodless open mouth, lank strands over the face, bare feet
  under a slept-in nightgown). It is reviewed on the gowned cast model (catalog role `LADY`). The native literary-effects
  proof draws her ceiling pose from below.

### Confession

- After each chapter the listener dictates, he asks the person holding the pen one question (`ConfessionAnswers.QUESTIONS`,
  one per chapter), on his name tag and in the reader's chat.
- The reader answers by typing in chat. The line never reaches public chat: the event is cancelled, the reader alone sees
  their own words ("You, quietly: …") and the fragment he repeats while he writes it down (the answer's longest word and
  the two after it). Anything said to him before he asks, during a chapter, is held back the same way, and he says not
  yet. Between chapters, ordinary chat is the reader's own again.
- Thirty seconds of silence before him (counted only while the reader stands in his sight) goes down as
  "(did not answer)".
- The journal takes each question and the reply. The sealed journal is the reader's earlier writing, then his eight
  questions with their answers, one to a page, then the count and the signature. The last account ends on their last
  answer, or on the line saying they gave none. The resolution and its gates are unchanged.

### Karen's room

- The reader's own client projects onto the white wall: their newest F2 screenshots, read from this computer's
  `screenshots` folder, and, among them, small pictures of their screen that the client took now and then without saying
  so (every five to ten minutes of play with no screen open, 160 by 90, the newest 24 kept in
  `the_oldest_house/karen` in the game folder). Up to ten of each, newest first, interleaved; each is cut to 16:9 from its
  middle, scaled to 256 by 144 on a background thread, and shown for six seconds with a projector click. The House's own
  photographs take a turn after every third picture; the projector's button moves the slides on.
- Nothing about these pictures leaves the computer: no image, file name or count is sent to the server. The server only
  names the wall, and now does so even before any photograph is ready.
- Client settings (`the_oldest_house-client.toml`): `projectScreenshots` and `secretSnapshots`, both on by default.
- The native literary-effects proof checks the reading order, the cut and the refusal of non-pictures in a scratch folder.

### The phone in the canoe

- **Filming** shows the phone's viewfinder: blinking REC, a running timecode, frame corners, battery, ISO and quality
  marks and a night grain. Four waterlogged light blocks put moonlight on the water over the drowned steeple.
- **The fall.** As the phone goes over the side the picture tumbles for 24 ticks, with a splash and the sound of going
  under. It then lies looking up through the water for the rest of the seven seconds: a teal dark, a moon broken into
  drifting bands, the odd bubble rising past the lens, the muffled lake, and a viewfinder that says the lens is wet.
- **Coming to.** The fade to black beats like a heart (four beats, slowing). The reader comes to on the jetty: the dark
  lifts over four and a half seconds, they gasp out of the water, drip for twelve seconds, and are told they are soaked
  through. The server marks the finish by sending the end tick with phase 0.
- **Playback.** Recovered from the cave canoe, the phone plays its clip back on its cracked screen
  (`PhonePlaybackScreen`). Each stored frame is drawn for two and a half seconds as a small, grainy night picture of what
  the lens faced, with a crossfade and the frame's own time and description: the sky with the moon, the drowned steeple
  and its belfry, the canoe's boards and ribs, the dark bank, the turn as it fell, and the moon from under the water,
  which it holds with "END OF CLIP". The pictures are drawn from the frame's description, not read from the world.
  Crouching opens the frame record as a book, as before.
- The six pictures were rendered locally from a port of the painter and revised before they were built: the steeple
  became a tower and belfry instead of a tree shape, the boards lost brick-like joints, the bank gained a sky glow
  behind its trees, and the underwater moon grew.
- The native phone proof chain adds three held playback frames and the viewfinder over the water.

## Tests

- `miniatureCardsTellTheReaderWhatTheyDidInEachRoom`: the card's wording from recorded facts, an unfinished room, a room
  still ahead, the stands, the mother's twenty seconds and the four corners in open air under the ceiling.
- `theActualCarriedQuillResumesAcrossVisitsAndTheSealedJournalIsReadPersonally` now answers each question with a real
  chat event (cancelled from public chat), leaves the fourth in silence, checks ordinary chat between chapters and the
  sealed journal's pages.
- `theListenerRepeatsAFragmentOfTheReadersOwnWords`: the fragment, one question per chapter, an unanswered journal.
- The recovered phone case: a plain use plays back without writing the record; crouching writes and opens it.
