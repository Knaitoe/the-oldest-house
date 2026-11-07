# Speak Up (0.4.327)

The owner offered two "finding your voice" rooms on 7 November, Clearing the Throat and Speak Up, and said to choose or tweak. This is Speak Up, with two things borrowed from Clearing the Throat: the voice that holds back every scene needing a spoken answer until the reader has one, and "say nothing" as a full answer for a reader the House reads as unseen.

The owner's text, kept as the brief:

> The player enters an ordinary dining room. A few people sit around the table, leaving one place open. They acknowledge the player enough to offer a seat, but carry on talking across them. One person asks the player something simple: "Would you like something to drink?" Before the player can respond, another answers: "They're fine." It happens again: someone offers food, or asks whether the player is staying. Someone else supplies the answer. Their assumptions could contradict one another, making it clear these people know very little about their guest. Then the person opposite finally looks directly at the player. "You can answer." That is where the conversation panel presents its first choices. Each answer gets a concrete response: water is poured, the glass is taken away, or someone moves their chair to clear the doorway. The unsettling part comes afterward. Conversation resumes, but everyone gradually turns toward the empty chair beside the player. Someone asks it the same question. The empty place gives the answer the player just chose. I'd place it before the earliest character scene that requires a spoken answer.

## Where it sits

`SPEAK_UP`, slot 200, an ordinary gray room in the interior (layout 101), built whole by its own builder (`SpeakUp`). It is not a Witness story: no source, no account, no credit, and the quota is unchanged.

- **When it comes.** After the quiet rooms, a reader meets stories in the ordinary way, and the ones that do not ask them to speak can come first (the owner: "the player can encounter unvoiced vignettes after the quiet rooms. But speak up is encountered/unlocks voiced vignettes"). The dining room is one of the things a story door can open on, at the stories' own pace: one story door in three, and the next one for certain once two other stories have been dealt. It is never dealt as an ordinary hall, never from the shared hall cache, and never again once the reader has a voice.
- **What it holds back.** From the first quiet room a reader enters until they have spoken at the table, they are not dealt a scene that asks for a spoken answer: the Ghosts Set, Second Person, Confession, A Place for You, and Johnny Truant's places. A door already dealt to one of those is dealt again.
- **Who is not held back.** A reader who has never begun the quiet rooms, such as an older record or a test fixture, is neither held back nor sent to Speak Up.

## The room

An ordinary dining room, eleven blocks by thirteen:

- a table two blocks wide and three long, with three cane chairs down the west side, two on the east side and one at the head;
- a sideboard on the west wall with a serving counter beside it and a jug of water on the counter, a light over the table, and a window in the east wall with night behind it;
- the way in from the south, and the way on in the north wall, right behind the chair at the head of the table.

There are four people at the table, each reader's own private cast. Nobody else sees them, so a second reader in the room has a table of their own.

- **Joan**, at the south end of the west side, nearest the way in, who answers for the reader first.
- **Pat**, opposite her, who contradicts her.
- **Ruth**, opposite the reader's place, who is the one who finally looks at them.
- **Dell**, at the head of the table, his chair in front of the way on.

The reader's place is the middle chair on the west side, with a glass upside down on the table in front of it. The chair beside it, at the north end, is empty, with supper set for someone.

## The sequence

The timings are game ticks and run only while the reader is in the room.

1. **Arrival.** Ruth: "There's room. Sit down." The others talk among themselves above their heads. The table's talk is in their voices, never in chat, and the lines addressed to the reader are in the panel.
2. **Drink.** Pat asks: "Would you like something to drink?" Before anything can be chosen, Joan answers: "They're fine."
3. **Staying.** Dell asks: "Are you staying for supper?" Joan says "They can't stay," and Pat says "Of course they're staying."
4. **Bread.** Pat offers the bread. Joan says "They don't eat bread." Dell says "Since when?"
5. **"You can answer."** Ruth turns to the reader and says it. The panel opens with three replies, or four for a reader the House reads as unseen:
   - **"Some water, please."** Ruth: "Of course." She pours, and the reader is handed a glass of water, which they keep.
   - **"I'm fine, thank you."** Ruth: "All right." The glass is taken away.
   - **"I'd like to leave."** Dell: "Oh. Sorry." He drags his chair aside and stands clear, and the way on is open.
   - **"Say nothing."** (unseen only) Ruth: "That's an answer too."

   Whatever is chosen, the reader has a voice from that moment, and it is saved.
6. **The empty chair.** Talk resumes for a moment ("Where were we." "Supper."). Then one by one they turn their heads to the empty chair beside the reader. Joan asks it: "Would you like something to drink?" The empty chair answers with the reader's chosen line, word for word. The words show over the chair, and the panel opens again with them as one of the reader's own replies, which they did not choose. If the reader said nothing, the chair says nothing, and Ruth says "That's an answer too" to it.
7. **The way on.** If Dell has not already moved his chair, he does it now without a word. The reader may leave.

A reader who touches any of them while the question is open has it put again. Once it is over, they go on eating.

## Other readers

If someone else is in the room, they see their own table and answer their own question. One reader's answer gives nobody else a voice, and each empty chair repeats only its own reader. The room's blocks are shared, so a chair one reader's Dell has moved stays moved. The owner asked that a reader's chosen lines be shown to other players; that is a change to the panel as a whole, and it comes next, outside this room.

## Leaving early

Walking out before "You can answer" leaves the reader without a voice, so a later story door offers Speak Up again. The scene starts over each time the reader enters; the glass and Dell's chair are put back when nobody else is in the room. A reader who has spoken finds the table already eating.

## Not in this slice

- The HOME-DANGER whisper variant from Clearing the Throat.
- Showing a reader's chosen lines to other players.

## Validation

- Two new exploration cases. The first checks the dealing. A reader who never began the quiet rooms is not held back. Partway through, they are held. After the last room the unvoiced stories are available and the spoken ones are not. There is no story door, and so no table, before the story depth. At the story depth, an unvoiced story can come first and so can the table, and the table always comes within three story doors. Once the reader has a voice, the table is never dealt again.
- The second seats two readers at their own tables. It checks that each sees their own five, that the table answers for them and offers nothing to say until Ruth's line, and that both are offered the three replies. One asks to leave (the chair moves and the way on is clear); the other asks for water and is handed a glass. Each empty chair repeats its own reader's words, which never reach the other reader, and Pat turns to the chair.
- The slot, generic-pass, test-door, quiet-room chain, tiered-carve and yard-dealing cases pass with the new place.
- The architecture views export the dining room.
