# Owner notes: 0.4.201

Four notes from the owner on October 31, after a look at the bathroom mirror, the quiet rooms and their last card.

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

## Not in the phone's shot

> "can you try to ensure the player can't see themselves in lake_canoe during the camera fall sequence?"

When the phone goes over the side in Phone in the Canoe, the view becomes the falling phone's. Until now the game drew the reader as it draws anyone the camera can see, so their body, the name over it and the first-person hand all showed in the shot. While the view is the phone's, `LakePhoneClient` now cancels the player renderer for the reader's own body, which also drops the name, and cancels the hand. Other readers in the scene are drawn as before. The body's shadow on the canoe is not suppressed; at night it barely shows.

## Not changed yet

The coatroom's closing line, "That's how you'll look here.", waits on the owner's choice among the suggestions given in conversation.
