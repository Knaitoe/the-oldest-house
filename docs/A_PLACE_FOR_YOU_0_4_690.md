# A Place for You: the back passage (0.4.690)

The sixth slice (item 5 of `docs/A_PLACE_FOR_YOU_PLAN.md`), the proposal's N6B, somebody's actual way out:

> Kit shows the stiff back-door mechanism. The catch has been held out of reach by a cupboard and an inward-opening door. This is a
> short, physical puzzle: move the cupboard enough to reach the catch; hold the inward-opening door with a provided wedge or chair;
> release the catch and test the gate. An alternative is to ask Mara for the key, or accept Alex's help with a side window. Kit asks
> whether the reader will keep the departure quiet until the bag is outside. The player may help, refuse, tell Mara openly, or agree
> and then tell her privately. Kit reacts to what was done. If Kit completes the departure, later choices cannot silently put them back
> in the house.

Code: `PlaceForYouBack` (new), hooks in `PlaceForYou` (Kit's and Alex's marks, the evening's clock, talking at the sink and at
dinner, the right-clicks, what is carried off, Kit going), `PlaceForYouSite` (the cupboard, the wedge, the step stool, the back door,
the back gate, the path beyond it, the side window, two more places for the bag), three new kinds of the fitting block, three carried
things, the art in `tools/draw_place_for_you_art.py` and the account in `PlaceForYouTexts`.

## What happens

- **Kit by the back door.** While Mara is at the sink, Kit waits in the hall's back corner and calls the reader over now and then.
- **What Kit shows.** The back door nobody uses: the catch is stiff and somebody has stood the cupboard in front of it. A gate at the
  back; if the door opens, Kit can go without walking past the kitchen. Will the reader keep it quiet until the bag is outside? Three
  answers: keep it quiet; tell Mara, with Kit there; won't help.
- **The physical way.** The cupboard shoves along the wall a third at a time (it is heavier at the near end). On the third shove it
  clears the catch, and on the floor where it stood is a wooden wedge that was keeping it level. The catch drawn, the door opens inward
  onto the dark; with nothing holding it, it swings back shut after two seconds and the catch drops in again. The wedge pushed in under
  it, or the step stool from the corner stood against it, holds it. Then the back gate, tried, opens onto a path between the trees.
- **Mara's key.** At the sink, Mara says the reader has been at the back door. Asked about a key, she asks what for; the reader can say
  Kit wants to go out that way, or that they'd like to see where it goes. The key is in the drawer with the tea towels, with Theo's.
  It turns the lock, and the door stays open on its own, the cupboard still where it was.
- **Alex's window.** Alex knows the sitting room window opens if you lift it first. Asked to hold it for Kit, Alex goes there and lifts
  it, and Kit hands the bag out to Alex; the window goes back down after.
- **The bag out.** Once a way is open, Kit fetches the bag where nobody sees and carries it out: through the back door and the gate to
  the path, or out of the window into the side yard; then comes back in for supper. When Kit goes later, it is by that way.
- **The quiet.** Kept, Kit thanks the reader. Told openly, with Kit standing there, Mara says she put the cupboard there herself, the
  first winter, so she would hear if anyone went, and gives the key. Agreed and then told privately before the bag is out, Kit says so
  when it is: "You said you'd wait." Refused, Kit does it alone, while nobody is looking at the back of the hall.

## With more than one reader

Each reader's evening is a copy of its own; its back door, cupboard and gate are that reader's own. Nothing is shared.

## Art

Drawn at sixteen pixels by `tools/draw_place_for_you_art.py`:

- The back passage's cupboard (`press`, six stages: lower and upper halves, unshoved, a third, two thirds), two panelled doors in old
  paint worn back to the wood at the edges and round the knobs, a plinth, a moulded cornice.
- The wedge (`wedge`, on the floor and pushed in under the back door), plain wood worn smooth along one edge.
- The step stool (`stool`, in its corner and stood against the back door), two treads, the paint worn through where feet go.
- Carried: the wedge, the step stool, and the back door key on its ring with a paper tag (`pfy_carried` 6, 7 and 8).
- The back door now hangs to open inward with its catch on the cupboard's side; the yard's back fence has a gate; beyond it a path runs
  between the trees and closes over at the end.

## New lines

Every line below is new in this release.

**Cues**

- "Kit goes into the kitchen ahead of you and stands by the sink." / "The cupboard stands in front of the catch." *(new, flagged)*
- "A cupboard, stood in front of the back door." / "The cupboard, shoved along the wall, clear of the catch." *(new, flagged)*
- "It scrapes a little way along the wall." / "Another hand's width. It's heavier at the near end." *(new, flagged)*
- "It comes clear of the catch. On the floor where it stood, a wooden wedge. It was keeping the cupboard level." *(new, flagged)*
- "The catch is behind the cupboard. You can't get your hand to it." *(new, flagged)*
- "You work the catch back. The door comes open inward, onto the dark." (and "...with your free hand.") *(new, flagged)*
- "It swings back shut on its own, and the catch drops in again." *(new, flagged)*
- "Open, for now. It's already starting to swing back." / "Open, and held." *(new, flagged)*
- "You push the wedge in under it with your foot. It holds." / "You stand the step stool against it. It holds." *(new, flagged)*
- "The key turns. The lock gives, and the door stays open on its own. You leave the key in it." *(new, flagged)*
- "Behind you, the back door is already swinging shut." / "The gate opens. A path goes off between the trees." / "The back gate."
  *(new, flagged)*
- "A wooden wedge, worn smooth along one edge." / "A wooden wedge, under the door." / "A wooden wedge." *(new, flagged)*
- "A step stool, for the high shelves." / "The step stool. Two treads, paint worn through where feet go." / "The step stool, against the
  back door." *(new, flagged)*
- "Tea towels, and under them a ring of keys that aren't hers. The back door key is on its own, with a paper tag." *(new, flagged)*
- "The drawer with the tea towels, under the counter by the sink." *(new, flagged)*
- "In the sitting room, Alex lifts the window and holds it." *(new, flagged)*

**The reader**

- "I'll keep it quiet." / "I'd rather tell Mara, with you there." / "I won't help with this." *(new, flagged)*
- To Mara: "Is there a key for it?" then "Kit wants to go out that way." / "I'd like to see where it goes."; "Kit's going out the
  back."; "Never mind." *(new, flagged)*
- "Kit's found a way out the back. I'm helping." *(new, flagged)*
- To Alex: "Hold it for Kit." / "Not now." *(new, flagged)*

**Kit**

- "This is the back door. Nobody uses it." *(new, flagged)*
- "The catch is stiff, and somebody stood the cupboard in front of it. I didn't. Mara says she didn't." *(new, flagged)*
- "There's a gate at the back. If this opens, I can go without walking past the kitchen." *(new, flagged)*
- "Will you keep it quiet? Only until the bag's outside." *(new, flagged)*
- "Thanks. Push from the end. It's lighter that side." / "...All right. Then I'm standing next to you when you do." / "That's fine.
  I'll do it myself." *(new, flagged)*
- "Got a minute? Back here." / "Go on. I'm here." / "I said I'd do it myself." / "I'll get the bag." / "Push from the end." / "There." /
  "That's it. That's all it ever was." *(new, flagged)*
- With the bag out: "It's out. Thanks for keeping it." / "It's out. She knew before it was. You said you'd wait." / "It's out. She
  watched me carry it." / "It's out. I did it myself." / "It's out. You told her. That's fair. I asked a lot." *(new, flagged)*

**Mara**

- "You've been at the back door." / "What for?" / "...Did Kit ask you to say that?" / "The path. It only goes to the path." *(new,
  flagged)*
- "It's in the drawer with the tea towels. With his." / "Is Kit." / "Thank you for telling me." / "Mm." *(new, flagged)*
- With Kit there: "Kit's standing there like they've got something to say." / "I know what the back door's for. I put the cupboard
  there." / "The first winter. So I'd hear it, if anyone went." / "The key's in the drawer with the tea towels. With his. Take it,
  before I think about it." *(new, flagged)*

**Alex**

- "Kit showed you the back door." / "The sitting room window opens if you lift it first. I found that out the first night." / "If it's
  for Kit, I'll hold it." *(new, flagged)*
- "All right. Give me a minute to get there." / "Offer stands." / "I've got it." / "There. I'll shut it before she feels the
  draft." *(new, flagged)*

**Kit and Mara, told together**

- Kit: "You put the cupboard there." *(new, flagged)*

**The account** (after Mara's room or the sink, before supper again)

- "Kit showed me the back door. The catch was stiff, and somebody had stood a cupboard in front of it. Kit asked me to keep it quiet
  until the bag was outside." then "I said I would." / "I said I'd rather tell Mara, with Kit there." / "I said I wouldn't help." *(new,
  flagged)*
- "We told her together. She said she had put the cupboard there herself, the first winter, so she would hear it if anyone went. Then
  she told me where the key was." *(new, flagged)*
- "I shoved the cupboard along the wall. Under where it stood was a wedge, keeping it level." then "I drew the catch, and the door swung
  back shut on its own, so the next time I [pushed the wedge in under it / stood the step stool against it]." or "I drew the catch and
  ...", then "The gate at the back opened onto a path between the trees." *(new, flagged)*
- "Mara told me the key was in the drawer with the tea towels, with his. It turned, and the gate at the back opened onto a path between
  the trees." (or, after telling her together, "The key turned, and...") *(new, flagged)*
- "Alex lifted the sitting room window and held it, and Kit passed the bag out through it." *(new, flagged)*
- "When I next looked, the cupboard had been moved along the wall and the back door was wedged open. Kit had done it alone." *(new,
  flagged)*
- "I told Mara before the bag was out." / "I told Mara." *(new, flagged)*
- "Kit carried the bag out to the path and came back in for supper." / "Then Kit came back in for supper." / "The bag was already
  outside." *(new, flagged)*
- "Kit said I had said I would wait." / "Kit thanked me for keeping it." *(new, flagged)*

## Cases

- `PlaceForYouTests#theBackPassage` (new): Kit by the back door; shown, the quiet asked for with three answers, agreed; the catch out of
  reach while the cupboard stands; three shoves, the cupboard along the wall and the wedge where it stood; the catch drawn and the door
  swinging back with nothing holding it; the wedge picked up, the catch drawn again, the wedge in under it; the back gate; Kit carrying
  the bag out and back; the bag beyond the gate; the quiet kept; Kit's way out the back; the account's sentences.
- `PlaceForYouTests#theBackDoorKey` (new): the quiet agreed, then broken in asking Mara for the key; the key from the drawer; the door
  open with the cupboard where it was, and staying open; the gate; Kit's line; the account.
- `PlaceForYouTests#theSideWindowAndTheOpenTelling` (new): told openly with Kit by the sink; Mara's cupboard and the key; Alex's window
  going up only when Alex is there; the bag handed out; the window shut again; Kit going by the front; the account.
- `PlaceForYouTests#kitOpensTheBackAlone` (new): refused; nothing done while the reader watches the back door; done unseen.

## Results

Locally: the eleven `PlaceForYouTests` cases (the four new back-passage cases among them) and the cast and together conduct checks
for A Place for You, 13 of 13. Eye-level frames of the back passage, the cupboard across the door, the step stool in its corner and the
back door from the gate side.
