# A Place for You: invite the past (0.4.688)

The fourth slice (item 3 of `docs/A_PLACE_FOR_YOU_PLAN.md`), the proposal's E3:

> The reader makes room for Theo's picture and explicitly completes the invitation. The old evening is restored. Theo enters as an
> ordinary person. Mara recognizes him. The household reorganizes around his being there. The present evening's memories are
> discarded. Mara no longer knows why the reader is standing in the hall. A care gesture prepared for them now belongs to someone
> else. Someone who already left is outside the rewrite. Someone still in the household may lose the present wish they had
> expressed. That cost is made explicit before the invitation. One small detail of Theo is wrong in a way Mara notices. She can choose
> to overlook it.

Code: `PlaceForYouInvite` (new), hooks in `PlaceForYou` (the chapter `INVITE`, Theo as a fifth person, the marks, the photograph and the
fridge), `PlaceForYouDoor` (the invitation, and no last word while his photograph is up), `PlaceForYouSite` (the photograph on the
chest and on the fridge), `HouseReadKitchenClient` (the fridge door showing a photograph), Theo's painted skin and the account in
`PlaceForYouTexts`.

## What happens

- **Making room.** Once somebody has knocked and the voice has asked at the door, Theo's photograph on the chest of drawers can be
  taken, but only once the reader's own picture is down off the fridge. Carried to the fridge, it goes up under the magnet where the
  reader's picture was. Taken down again, the fridge door is bare; it can also go back on the chest. Carried out of the yard, it goes
  back on the chest by itself.
- **The cost first.** The invitation is offered only after Ren has said what letting him in would cost (slice 2's line: nobody
  would remember tonight). While his photograph is up, the voice does not stop asking.
- **The invitation.** At the front door, open or shut, the reader can say "Come in, Theo." The door opens and the gate opens.
- **He comes in.** Theo is made at whichever end of the road the reader is not looking at, out among the trees, and walks: along the
  road, in at the gate, up the path, straight in without knocking. He drops his keys under the hall glass. Mara, waiting in the hall,
  says "There you are." If Kit is still on the road, they pass at the gate and say goodnight.
- **The old evening.** Each of these happens only where the reader isn't looking: Alex and Ren, who were never in the old evening,
  are not in the house; the reader's chair and place card go from the table; the gap in the household's pictures is filled; the
  reader's routine is laid out in the bathroom again. Kit, already gone, stays gone.
- **Mara doesn't know the reader.** Back at the table, she asks the reader if she can help with something. Asked, she doesn't think
  they've met; or Theo is her brother, and late; or mind the gate.
- **The care.** Where the reader can hear it, Mara tells Theo his things are out in the bathroom. They are the reader's.
- **The wrong thing.** In the kitchen, Theo dries a cup and puts it away in the right cupboard. Mara says so. He always put them in the
  wrong one. She lets it go and tells him to sit down.
- **The end.** The past let in (`invited_the_past`); the account waits in the mailbox. A reader who walks out before it is over is
  let go, and the account says so.

Nothing hurts, nothing locks, and Theo is never shown to be anything but a tired man home late.

## With more than one reader

Each reader's evening is a copy of its own, and so is its Theo. Nothing new is shared.

## Art

- Theo's painted skin (`literary_place_theo`, stranger phase 238): Mara's eyes, the work shirt and collar of his photograph, a day's
  shadow. Painted by `tools/paint_character_skins.py`; catalogued in `tools/character_art_catalog.py`.
- The fridge door can show a photograph instead of a drawing: Theo's own photograph (the chest's texture), with a white border and the
  magnet over it, drawn by `HouseReadKitchenClient`.
- The carried photograph (`pfy_carried`, custom model data 5) is the chest's standing photograph.
- Theo's photograph itself (`block/place/theo`) drawn again with more in it: the wall and a doorframe's edge behind him, his face turned a
  little, one ear, a side parting, stubble along the jaw, the open collar of the work shirt and the print gone pale at the corners.

## New lines

Every line below is new in this release.

**Cues**

- "A man half turned from the camera, in a work shirt." *(the photograph, now in the Talk panel; slice 1's cue)*
- "Your picture is still up on the fridge." *(new, flagged)*
- "You take Theo's photograph off the chest of drawers." *(new, flagged)*
- "You put Theo's photograph up on the fridge, under the magnet, where your picture was." *(new, flagged)*
- "You stand it back on the chest of drawers." *(new, flagged)*
- "Theo's photograph." *(new, flagged)*
- "Theo's photograph, under the magnet, where your picture was." *(new, flagged)*
- "You take it down. The fridge door is bare." *(new, flagged)*
- "Theo's photograph, under the magnet." *(new, flagged)*
- "Nobody answers. Then, a long way down the road, footsteps." *(new, flagged)*
- "He comes straight in without knocking, and drops his keys under the hall glass." *(new, flagged)*
- "Laid out as it was for you. She put it out for him." *(new, flagged)*
- "Theo dries a cup and puts it away in the cupboard over the counter." *(new, flagged)*
- The ending: "Theo is in his place at the table. There isn't one for you." *(new, flagged)*

**The reader**

- "Take it." / "Leave it." / "Take it down." *(new, flagged)*
- "Come in, Theo." *(new, flagged)*
- To Mara: "It's me." / "Who's he?" / "I'll go." *(new, flagged)*

**The household**

- Kit, passing him at the gate: "Night." Theo: "Night." *(new, flagged)*
- Mara: "There you are." *(new, flagged)*
- Theo: "Here I am. I lost track of the time." *(new, flagged)*
- Mara: "Sorry. Can I help you with something?" *(new, flagged)*
- Mara: "I'm sorry. I don't think we've met." / "That's Theo. My brother. He's late. He's always late." / "Mind the gate. It sticks."
  *(new, flagged)*
- Mara: "Your things are out in the bathroom. I put them out." Theo: "You didn't have to." *(new, flagged)*
- Mara: "That's the right cupboard." Theo: "Is it?" Mara: "You always put them in the wrong one." Theo: "Did I?" Mara: "Never mind.
  Sit down, it's getting cold." *(new, flagged)*
- Spoken to afterwards: Theo, "Evening."; Mara, "Mind the gate on your way out. It sticks."; Kit, "Night." *(new, flagged)*

**The account** (for `invited_the_past`, after the confrontation and the door)

- "I had taken my picture down off the fridge. I put Theo's photograph up where it had been, and told him to come in." *(new, flagged)*
- "He came along the road and in at the gate, and straight in without knocking, and dropped his keys under the hall glass." *(new,
  flagged)*
- "Kit passed him at the gate. They said goodnight to each other." *(new, flagged)*
- "Mara said, \"There you are.\"" *(new, flagged)*
- "After that Alex and Ren weren't in the house." (or Alex, or Ren, alone) "Ren had said they were staying near her." *(new, flagged)*
- "Mara asked me if she could help me with something." then "I told her it was me. She said she didn't think we'd met." / "I asked who
  he was. Theo, she said, her brother, and late, the way he always was." / "I said I'd go. She told me to mind the gate." *(new,
  flagged)*
- "She told him his things were out in the bathroom." then "It was my [routine], the one she had laid out for me." or "She had put
  them out for me." *(new, flagged)*
- "He dried a cup and put it away in the right cupboard. Mara said he always put them in the wrong one. Then she told him to sit down
  before it got cold." *(new, flagged)*
- "Kit had already gone." *(new, flagged)*
- "When I left, Theo was in his place at the table." or "I went out while they were still talking. Nobody asked where I was going."
  *(new, flagged)*

## Cases

`PlaceForYouTests#invitingThePast` (new): the knock and the door opened; the cost asked of Ren; no taking the photograph while the
reader's picture is up; the picture down, the photograph carried and put up on the fridge; "Come in, Theo." at the open door; Theo
made out of sight at a road's end; in without knocking; Alex and Ren gone while nobody looks; Mara not knowing the reader; the care;
the cup in the right cupboard; `invited_the_past`; no place at the table for the reader; the routine laid out for him; and the
account's sentences, without the opened house's or the kept evening's.

## Results

Locally: the six `PlaceForYouTests` cases, `invitingThePast` among them, with A Place for You's cast and two-reader conduct cases,
8 of 8. Eye-level frames of the photograph gone from the chest and of the household's pictures with the gap filled. The release chain
on this commit is in the commit's message.
