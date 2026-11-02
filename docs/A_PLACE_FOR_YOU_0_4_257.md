# A Place for You, the first slice (0.4.257)

The owner's choose-your-own-adventure proposal (docs/A_PLACE_FOR_YOU_PROPOSAL.md), built to its own plan for a first playable slice:

> The first playable slice should contain the arrival, one ordinary shared activity, the routine, Kit's bag and the confrontation.
> Complete one route through opening the house. That slice should establish whether Mara, Kit and a peer are people the player
> wants to spend time with.

The slice is one continuous evening in Mara's house. It ends with Open the house (E1), or with Take the road (E4) once the
household's conflict has been acknowledged. Either one resolves the story.

Built later, by the proposal's order:
- Keep a place (E2) and Invite the past (E3);
- Theo at the door;
- the three investigation routes (Mara's room, the back passage, the older pictures);
- romance and closeness routes;
- the aftermath visits.

The house is built whole, with its upstairs, so later slices add story rather than rooms. Code: `PlaceForYou` (the evening),
`PlaceForYouSite` (the house), `PlaceForYouTexts` (the writing), `PlaceForYouBlock` (its props) and `Talk` (the conversation
panel, shared by later vignettes).

## What this release changes

- **The place.** `LabyrinthPlace.PLACE_FOR_YOU` takes slot 165 (layout 85, new builds only). It stands in the outside
  dimension, built whole, and is private: each reader gets their own copy and keeps it until they finish.
- **Witness.** It is one new eligible understanding source, so the pool is 73 sources, 55 required, two kinds, three endings.
- **Protocol 121.** The conversation panel's two payloads raise it.

## The house

A short road runs east to west across the south of the scene; the reader arrives on it. Opposite stands a picket fence with a
gate, a front path, a porch and the front door. A side porch with a screen door opens from the kitchen on the east.

- **Ground floor.**
  - The front vestibule: coat hooks, the hall glass and a bench.
  - The sitting room to the west: two armchairs, a sofa, the radio and the household's pictures on the sideboard.
  - The kitchen and dining room to the east: the table with its places, the stove, the sink, the fridge and the screen door.
  - The hall running north: the stair; Kit's door; the bathroom; and the back passage, with its stiff back door and the
    cupboard in front of its catch.
- **Upstairs.** Mara's room, the spare room, a landing with older pictures and a storage room. Furnished for later slices; in
  this slice their doors are shut and a hand on them gets a line.

The house has its own materials and props, drawn by `tools/draw_place_for_you_art.py` (checked in CI): clapboard, two
wallpapers, floorboards, kitchen linoleum, porch boards, bathroom tile, shingles; the bag in three states, the place card,
plates, the kettle and cups, the radio, Theo's photograph, the household picture, the cupboard door Kit's door becomes, and the
routine items lying on a counter.

## The cast

- **Mara** keeps the house. New skin.
- **Kit** is an adult trying to leave it. New skin.
- **Alex and Ren**, the two strangers from the shelter, wear the shelter strangers' own faces at the reader's age. Alex is the
  man, Ren the woman. The House writes about both of them by name.
- **Theo** is Mara's dead brother. In this slice he is only a photograph, a chair and the way Mara talks around them.

People speak above their heads (their nameplates). Every exchange that asks something of the reader opens the conversation
panel; that is where the writing lives.

## The conversation panel (`Talk`)

- **The panel.** A private, non-pausing panel shows who is speaking, the exchange so far (the reader's own replies included)
  and the replies on offer.
- **Replies state their intention.** For example: "I don't think I've been here before."
- **Nothing advances while a reply is open.** Escape closes the panel, and the conversation waits. Using the person again
  reopens the exchange where it was.
- **A reply is honoured once.** The server keeps one open exchange per reader with a token. A stale or repeated click does
  nothing.
- **Payloads.** `TalkPayload` (to the client: open or update) and `TalkReplyPayload` (to the server: token and reply).

## What the House remembers, and when it may say it

On the reader's first arrival the evening takes a snapshot of what the quiet rooms saw (`Snap`). It never reads them again and
never changes them. Every callback is a literal act, never a category.

- **What the snapshot holds.**
  - the routine item the reader used in the bathroom (or picked up);
  - what they did to the hall glass;
  - where they stood under the eaves;
  - how they first answered the porch whistle;
  - their kitchen drawing, if they hung it;
  - the settled waiting-room, presentation and attraction reads;
  - the home read.
- **When it is empty.** Turning off inference (`HouseConfig`) empties the snapshot. So does a reader the quiet rooms never saw.
  An empty snapshot gets the fully written neutral evening: the reader is an expected guest.
- **What an answer settles.** The reader's answer tonight decides how the household treats them. A refusal is never taken as
  proof the House was right.

## The evening

Each chapter is saved in the reader's record. Leaving mid-evening and coming back finds the house as it was left:
- the people at their marks;
- the bag where it is;
- the door as it became.

### The approach

Where the evening first becomes readable depends on how the reader answered the porch. Every approach reaches the same house.
- **Went straight in (haven).** The screen door stands open on the side porch, with a plate kept warm on the table inside.
- **Hid or watched (danger).** Kit is on the side porch, making sure the gate stays open.
- **Took the road (runaway).** Alex is on the road and asks for a moment before the reader walks on.
- **Otherwise.** The front path, and nobody blocks the door.

### The welcome

Mara meets the reader inside and says one small, permitted thing she knows, in order of what the snapshot holds:
- the routine item: "I left it where your hand went last time. Unless you'd rather I moved it.";
- the turned or covered glass;
- the lantern in the rain;
- the drawing on the fridge;
- nothing.

The welcome offers three replies:
- **"It's good to be back."** The household treats the reader as family.
- **"I don't think I've been here before."** The reader is a guest. Mara apologises, and she keeps saying the wrong thing now
  and then.
- **"Thank you. I'll stay for supper."** The reader takes hospitality without the relationship.

A settled waiting-room read changes only what Mara offers, never what the reader can do:
- the room that is still theirs;
- being the one who always knows what to do;
- being looked after.

### The place card (the source)

The reader's place at the table is set, with a place card folded once: their name in Mara's hand. Reading it is the story's
source.

### The first chair

Who the reader sits beside, or talks to first, is the first uninterrupted conversation. It closes off no one.
- **Mara** asks an ordinary favour and talks around the empty place at the table: Theo's chair.
- **Kit** asks whether the reader is any good with catches.
- **Alex** remembers the shelter if the reader stood by him or danced with him; otherwise he asks if they have met.
- **Ren** asks something practical, then quietly whether the reader is all right staying.

### Something ordinary together

The reader chooses one, each with a small imperfection written in:
- **Clearing the plates with Mara.** Three plates from the table to the sink. She has reheated the meal once already.
- **Mending Kit's bag clasp.** Three tries at the clasp while Kit holds the bag. Kit laughs at their own explanation.
- **Making tea with Ren.** The kettle on the stove, then two cups. Ren forgets where the cups are kept.
- **Sitting by the radio with Alex.** The sofa, and half a minute of the radio and talk.

### The prepared bathroom

Mara, or whoever the reader chose to spend time with, says the bathroom is ready. On its counter lies the reader's own routine
item, or a plain washcloth.

They ask how the reader wants it:
- **Help.** The helper does it with them.
- **Company.** The helper sits on the edge of the tub while the reader does it.
- **Privacy.** The helper leaves and shuts the door.

The answer is remembered. The reader uses the item, and it goes back in the cabinet.

Then it is laid out again, ready for the same moment. If the helper is still there, they say they did not move it; they can see
it too.

### The bag in the passage

Kit carries a packed bag to the gate and comes back in. During supper talk, Mara goes out and carries the bag back to a kitchen
chair. Kit, Mara and the reader have the exchange at the bag:
- "Mara, put it where Kit left it."
- "Kit, where were you planning to go?"
- "Could this wait until morning?"
- "I shouldn't be deciding whether somebody else leaves."

The reader can also pick the bag up (only within the house and its yard) and do one of these:
- put it back at the gate;
- hand it to Kit;
- hand it to Mara;
- help her unpack it on the table.

When Kit's leaving is put off (a delay asked for, or the bag unpacked), the house starts taking Kit's place, while the reader
can see it happen:
- Kit's door becomes a cupboard door;
- Kit's chair is gone from the table.

After the bag, Mara stands at the sink. Talking to her there is the direct conversation the confrontation needs:
- the empty place at the table, and Theo;
- "I don't want another conversation in an empty kitchen."

The reader can listen without promising, promise her company, or ask what Kit wants. A promise is kept verbatim in the record.

### Dinner again

The table is reset. Plates the reader cleared are back, exactly as they were. Before the confrontation, the house shows what the
evening's commitments are doing, with something the reader can stop with their hands:
- **Mara cannot stop setting the table.** She asks the reader to take the serving spoon from her hand; taken, it is set down
  on the counter.
- **The reader helped Kit.** The front door swings shut ahead of whoever walks to it, and has to be held.
- **The reader asked for privacy.** In the empty bathroom the routine goes on by itself, and a hand on the item stops it.
- **Kit's door became a cupboard.** Kit can be heard behind it until the reader opens it.

Then the confrontation, at the table:
- **"I'd like to say this to everyone."** Speak openly at the table.
- **"Mara, can we talk in the other room?"** Mara and the reader step into the sitting room.
- **"Kit, go now. I'll keep them talking."** Help Kit finish leaving.
- **"This is more than I can take on. I'm going to go."** Take the road.

### Open the house (E1)

- **The exits.** The reader makes the way out usable: a hand on the front door sets the hall chair against it.
- **The picture.** The reader takes their picture down from the fridge (their own drawing, or a pencil figure with no face).
  That is the place the household fixed for them.

Then each person decides for themselves:
- **Kit** leaves with the bag, out of the front door and through the gate, leaving both open, and is gone from the road once
  nobody is watching.
- **Mara** chooses one of three:
  - to stay for now;
  - to ask the reader to sit with her before she decides (only for a reader who promised her company and kept it);
  - to walk to the gate with Kit (only for a reader who heard her out without using it against her).
- **Alex** names where he is going and asks whether the reader wants to walk a while. No is a whole answer.
- **Ren** stays with Mara if Mara stays, and otherwise goes.

There are two written versions: a reader who accepted belonging helps the family change; a guest helps as a guest. If everybody
leaves, the last person turns off the kitchen light. The account is ready in the mailbox by the gate.

### Take the road (E4)

Once the bag has been seen, walking out of the gate onto the road asks once whether the reader means it. If they do:
- what was promised is answered;
- Kit may already be outside;
- the house stays exactly as it was left;
- the account is ready in the mailbox by the gate.

Walking out before then just leaves, and the evening waits.

## The account

It records acts, never what they prove:
- the first familiar thing the reader let the house claim, or refused;
- the boundary they set in the bathroom;
- any promise, and whether it was kept;
- what they learned about Theo and about Kit;
- what they did at the end;
- who actually stayed.

It never names a colour, an age or the reader's gender. Its title is "The account: Mara's house". The Witness line, House of
Leaves place name, co-op line and miniature note follow the same facts.

The keepsake is the place card, folded once, which the reader may keep.

## Tests

`PlaceForYouTests` (literary), every exchange answered through the panel's own replies:
- **A guest opens the house.** A reader the quiet rooms never saw: the neutral welcome (nothing remembered, the spare room
  offered); the card read; the plates cleared with Mara; the bathroom kept private and the routine laid out again; the bag seen
  from the porch, brought back in while the reader is in the kitchen, the exchange, the bag carried and handed back to Kit; the
  promise to sit with Mara; the spoon; the door that swings shut and is held; the routine stopped by hand; saying it to everyone;
  Kit leaving through the gate; the chair against the door and the picture down; sitting with Mara until she decides; Alex walked
  to the road; the account read from the mailbox, saying exactly those things.
- **A reader the House knows takes the road.** Settled age and home reads, a routine item, a turned coatroom glass, the eaves
  shared and a drawing hung: welcomed back as family with the item named and the analog offer; the hall glass turned; Ren
  remembering the eaves; tea; the reader's own item in the bathroom, done together; the bag; "wait until morning"; Kit's door a
  cupboard once looked at and Kit's chair gone; Kit's cupboard opened at dinner; the road taken; the road account.
- **Inference off.** Nothing kept, the neutral welcome, a faceless figure on the fridge; a stale reply changes nothing; talking
  again picks an open exchange up where it was, and once it is gone Kit asks the one thing left.

`ArchitectureTests` checks that the house is built whole.
