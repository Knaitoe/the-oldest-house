# A sort of thing 8: the feast, the crown and check (0.4.648)

The eighth part of the plan (docs/SLEEPING_KING_PLAN.md), from the proposal (docs/SLEEPING_KING_PROPOSAL.md, "8. The crown" and "The
last move"). No Witness, layout or protocol change here. This release also has the dream go on every server tick (KingBreath.tickAll),
so the King's people move as smoothly in play as in the cases, and every line timed by the dream's clock comes when it should. It also
steadies one two-client proof that failed in run 1682 (see the end).

## The feast

The eighth square has a long table laid down the middle file, two blocks wide from z -160 to -166 (`KingSite.feast`). A cloth hangs
down its long sides. On it are a leg of mutton on a pewter dish, a pudding with sauce and a sprig of holly, and two pairs of candles on
brass stands. Three high-backed chairs stand down each side and one at the foot. The peg for the eighth square is at its head
(`KingSite.FEAST_HEAD`).

All of it starts as a drawing. Every block is in its ruled twin, made from its filled picture by the same rule as the country's blocks,
so the two cannot drift apart (`tools/draw_king_art.py`, `FEAST_RULED`). Six of the dream's people sit in the side chairs, pencilled
in, drawn as their originals: the Guard, the Knight and the King of Hearts on the west side, the herald, the Queen and the Queen of
Hearts on the east. The chair at the foot is empty (`KingFeast`, stranger phases 228-233).

- **Introductions.** A reader on the eighth square who keeps something of the feast in the middle of their sight for a second
  introduces it (`KingFeast.LOOK`, game ticks). This works for a guest, the table, the mutton, the pudding, the candles or the empty
  chair. Whatever is introduced fills in. A guest fills in over about a third of a second and says what they say. A thing gets a line
  to every reader at the feast, and a small clear note sounds where it is (`king.introduced`). From then on it is the dream's, filled
  in for everybody in that copy of the country. The candles, filled in, give real light (11).
- **The guests look back.** Filled in, a guest turns their head to the nearest reader on the square, as far as a neck goes from
  facing the table (75°). That look is the dream's notice (`KingBreath.lookedAtByTheDream`), so a line a guest looks at is a sort of
  thing. Drawn guests only face across the table.
- **A hand on things.** A drawn thing is only drawn. An introduced one is somebody now: the mutton and pudding are not to be cut, a
  chair is not to be sat on, and the table holds what it holds. A hand on a filled guest opens their panel. A hand on a drawn one says
  they are only a drawing.

## The crown

Counted on the peg at the head of the table, a reader who is at least a sort of thing is crowned (`KingFeast.crown`). Counted means
standing on it while a guest or another reader looks at them. A line has no head for a crown.

- **The crown is real.** It comes down out of the air onto their head (`king.crowned`), and whatever they wore there goes into their
  pack, or at their feet if the pack is full. It is a crown in the round, gold, with eight points and three stones (`KingCrownItem`,
  `models/item/king_crown.json`). It is their own: another reader's crown on a head is only a crown.
- **It is notice.** A reader wearing their own crown is never less than a sort of thing anywhere in the country (`KingBreath`).
- **The Queen's move.** Off the peg, eight ruled lines run out across the ground from the wearer's feet, the eight ways a queen moves
  (`client.KingQueenLines`). One of them is turned toward the King, drawn harder, and ends a little short of where he lies, by his
  tree. The rest thin out as they go. Each has a tick across it every two blocks, like a rule's marks. Only the wearer sees their own
  lines, and they are told of them once. Along them, and anywhere, no hedge or brook stops a queen (`KingHedgeBlock`,
  `KingVergeBlock`).

## Check, and the last move

- **Check.** Crowned beside him, a reader is too much of a thing, and they are told so once. From the second in-breath after they
  come beside him, each in-breath stirs him once (`KingSleep`, `KingCheckFrom`). However lightly he sleeps by then, they have at
  least one whole breath to choose in. A step away and back gives them that breath again.
- **Standing until he wakes.** If the stirs wake him while a reader is in check beside him, that is the last move and not too soon.
  That reader stood there until he woke (`stood_until_he_woke`). Everybody else in the country at the waking was there when he woke,
  and their account says somebody stood beside him crowned.
- **Taking the crown off.** With the crown off after check, a reader is only a thing again and free to walk on the out-breath. Away
  from him they thin by the square (`KingBreath.away`: a thing within one square, a sort of thing at two, a line from three). To leave
  him his sleep, a reader has to stop being a queen (`KingSleep.walkedOff` needs check and no crown on the head).
- **The last song.** The Knight's song, sung beside him by a reader in check who has taken the crown off, settles him all the way
  (`KingSleep.lastSong`), once a dream. His stirs go back to none, and the filled land goes out from him to the edges of the country,
  for the only time (`KingSleep.FULL`).

## With more than one reader

The feast and its introductions are the dream's, one per copy of the country: whatever one reader introduces is filled in for
everybody, and a guest looks at the nearest reader. Being crowned, the lines, check, taking the crown off, the last song and the
ending are each reader's own, written from their own tick. Each crown is its wearer's. Two readers can both be crowned and both be in
check. If the stirs wake him, each one in check stood there until he woke. A reader alone has the guests to look at them on the peg.

## Art and sound

- `tools/draw_king_art.py` draws the cloth with its hem (for the drape), the same cloth without it (for the board's top), the roast,
  pewter, pudding and sauce, holly, candle, flame and brass, each with its ruled twin, and the mahogany, velvet and ivory's ruled twins
  (95 textures in all now). The feast's five kinds each have a drawn model (stage 0) and a filled one (stage 1): the table piece with
  its drape and one leg, the three dishes on it, and the high-backed chair with its velvet panel and brass knobs. The chair's back
  comes to a sitter's ears, so from behind there is a head over every chair. The mutton is a fat end, a shank and a knuckle of bone.
  The crown's gold and stones are two small item textures, and its model is drawn round the head as the game draws whatever is worn
  there.
- `client.LiteraryRenderers` draws each guest in their original's skin, sat up to the table (two pixels forward on the seat) with
  their hands flat on the cloth and the long coat hidden. Filled in, they are each at table in their own way: the Guard lifts a hand
  to his mouth as at a glass, the Knight nods forward and catches himself, the herald raises a hand as if to read something out, the
  Kings and Queens keep their hands together, and whoever speaks talks with a hand. Nobody cuts anything: everything on the table has
  been introduced. Drawn, they are still, in pencil (`KingClient.Edges`, the same outline as a reader who is a line), and the fill
  comes over the pencil as they are introduced.
- `tools/generate_king_audio.py`: `king.introduced` (a pencil shading something in, then a small clear note) and `king.crowned` (a
  short rush down, a band of gold settling with two small clinks, a soft weight).

## Checked

- `KingTests.theFeastTheCrownAndCheck` checks each of these:
  - The feast is laid as a drawing, its guests pencilled.
  - A second's look introduces the King of Hearts, who fills in with his chair, and the reader's record has it.
  - A look introduces the table, which fills in.
  - On the peg with him looking, the reader is crowned. The crown is on their head and their own, the old hat is in their pack, and
    another reader is not crowned.
  - Off the peg, the reader is told of the lines and is a sort of thing at least. Beside him they are in check and too much of a thing,
    and a hedge has no collision for them.
  - An in-breath stirs him. The crown off, carried, the reader is uncrowned.
  - The song sung beside him settles him all the way.
  - Walking off the board leaves him his sleep, and the account has the feast, the crown and the song.
  - A second reader, crowned and in check when six stirs wake him, stood there until he woke, and the account says so.
- `KingTests.walkingOffLeavesHimHisSleep` now also checks that a reader who met him and heard the brothers but was never in check is
  only put back.
- The earlier ten cases still pass.
- Look review, 26 frames from a headless client: the feast's pieces drawn and filled, the table laid (drawn, filling in and filled,
  from above and at eye height), each guest at table, the Queen drawn, the crown worn from four sides and as an item, and the lines
  from above and at eye height. The frames found five things, all changed and looked at again:
  - The guests' hands stopped short of the table and their forearms went through its edge. They sit forward now, hands on the cloth.
  - From behind, the chair backs hid everything of a sitter but the top of the head. The backs are three pixels lower.
  - Drawn, the cloth and the roast were a scatter of ink marks. The cloth's weave is fainter and the roast is scored in two long cuts,
    so their drawings are clean paper with a line for the hem and two for the cuts.
  - The cloth's hem ran across the middle of the table's top. The board has the cloth without its hem, and the hem is along the
    drape's bottom edge.
  - The Knight was cutting at the mutton, against the feast's own rule. He nods forward and catches himself instead.

## New lines (for the owner)

The owner (16 November): "you can gender the reader based on the presentation flag from the personhood vignettes". The Queen's
line at the crowning and the cue about the lines follow the reader's settled presentation read (`HouseRead.address`): a queen, a king,
either, or only crowned for anybody the House has not read. Whichever they are, they move as a queen does. `AGENTS.md` has the rule.

Spoken by the guests when introduced (captions over the speaker):
- The Queen: "There you are. Here you're what you're counted as. Stand at the head of the table."
- The Guard: "No tickets at a table. Everybody here is somebody now."
- The Knight: "I came round the long way. Somebody looked, so here I am."
- The herald: "Introduced! Everything at this table has been introduced to everything else."
- The King of Hearts: "We're all somebodies at this table. You looked at us, and that does it."
- The Queen of Hearts: "Nothing that's been introduced is cut. Remember that."

A hand on a filled guest (their panel):
- The Queen: "At the head of the table. Stand there, and be counted."
- The Guard: "I'd look at you through my glasses, but you've been looked at enough."
- The Knight: "Keep an eye on me. I sit up better watched."
- The herald: "Every name at this table has been read out. Yours was read first."
- The King of Hearts: "Sit down? There's nowhere to sit that hasn't been introduced."
- The Queen of Hearts: "Cut nothing, and nobody. It's very rude."

The Queen at the crowning, by the reader's presentation read:
- Read as a woman: "There. Now you're a queen, and a queen goes as far as the board goes, any way at all. Look at your feet."
- Read as a man: "There. Now you're a king, and a king new-made goes as a queen goes: as far as the board goes, any way at all. Look at
  your feet."
- Read as both: "There. Now you're crowned, king or queen as you please, and you go as far as the board goes, any way at all. Look at
  your feet."
- Unread, uncertain or unseen: "There. Now you're crowned, and the crowned go as far as the board goes, any way at all. Look at your
  feet."

Cues:
- First on the square: "A long table on the eighth square, laid for a feast, and only drawn. Somebody is drawn at every place but one."
- A drawn guest touched: "Only a drawing of somebody in a chair. Looked at a while, they fill in."
- A drawn thing touched: "Only drawn: an outline on paper. Looked at a while, it fills in."
- The mutton or pudding, introduced, touched: "It has been introduced, and is somebody now. It is not to be cut."
- A chair, introduced, touched: "It has been introduced, and is somebody now. It is not to be sat on."
- The table, introduced, touched: "The table has been introduced, and is somebody now. Nothing is to be put on it or taken off it."
- The table introduced: "The cloth fills in, and the long table under it, laid for company."
- The mutton introduced: "A leg of mutton fills in on its dish, and bows a little."
- The pudding introduced: "A pudding fills in on its plate. It has been introduced now, and nobody will cut it."
- The candles introduced: "The candles fill in, and are lit. The light from them is real."
- The empty chair introduced: "The chair at the foot of the table fills in. Nobody is in it."
- The crown: "Something came down out of the air and settled on the head: a crown, real, and heavy."
- The lines: "Eight ruled lines run out from the feet across the country. Along them no hedge or brook stops a queen. One of them ends by
  his tree." (For a reader read as a man, "stops a king crowned here"; for anybody else, "stops anybody crowned".)
- Check: "Crowned, beside him: too much of a thing on every breath, and his in-breaths will stir him. Check."
- The crown off after check: "The crown is off. Only a thing again, and free to walk on the out-breath."
- The last song: "He settled all the way, and the filled land went out from him to the edges of the country."
- The King's name panel, before the crown: "A pawn has no move here."

Subtitles: "Something at the table fills in"; "A crown comes down".

Account sentences:
- "On the eighth square a feast was laid, all of it drawn, and whatever I looked at was introduced, and filled in, and was somebody
  after that." (with " By the end the table was full of somebodies." if they introduced eight or more), or "On the eighth square a feast
  was laid, all of it drawn. I did not look at any of it long enough to fill it in."
- "At the head of the table I was counted, and a crown came down on me. After that no hedge or brook stopped me."
- "Beside him, crowned, I was too much of a thing, and every in-breath stirred him."
- "I took the crown off and sang him the Knight's song, and the country filled in behind me to its edges."
- Ending, stood until he woke: "I stood there crowned until his breathing woke him. He opened his eyes. One of us went out."
- Ending, there when another stood: "Somebody stood beside him crowned until his breathing woke him. He opened his eyes, and the
  country went over to lines with me in it."
- Ending, left him his sleep after check and without the song: "I took the crown off and left him his sleep." (then the existing
  "I walked until I was a line..." as before).

## Also in this release

Run 1682 failed one job, the Backseat's two-client proof: the client that had just reconnected drew a frame only every few ticks while
its world compiled, the ride it then started lasts under four seconds, and the frames the proof takes during the ride were missed.
The driver in that proof now waits to turn to the mirror (which starts the ride) until its client has drawn sixteen frames in forty
ticks, or twenty seconds have gone (`client.BackseatSocketClient`). The proof's status line also says how many frames the client has
drawn. Nothing in the Backseat itself changed.
