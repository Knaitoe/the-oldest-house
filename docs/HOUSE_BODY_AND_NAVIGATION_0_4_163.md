# The House's body, the quiet rooms before the deep interior, and the porch — 0.4.163

The owner's notes from the October 29 playtest:

> "have you implemented the check for "human" skins yet? my player skin is still displaying. I'd also remove the player character from the inventory menu while in the house so the player only sees themselves through the eyes of others or reflected in mirrors, etc."
>
> "house navigation needs to be desperately balanced. I encountered the "deep interior" - the stone passages - before even finding/finishing the personality/personhood vignettes."
>
> "expand or reset the stone pass requirement, so players aren't immediately stumbling onto it after finishing the first five vignettes."
>
> "swing textures are still disconnected." / "other texture issue" (the carved rail on the porch)

## The House's body

Before this version the House's body was a design the owner had decided on but that had not been built. It is built now, as the design describes ("The House's body" in `docs/HOUSE_READ_DESIGN.md`).

- **The check.** Each reader's own client looks at the face of their own skin: the front of the head, with the hat layer over it. If less than a quarter of it is in human skin colours, the skin isn't human. The client tells the server only yes or no; nothing else of the skin is sent.
  - The skin colours are a box in YCbCr: any lightness above near-black, and the warm chroma skin has.
  - On Minecraft's nine default skins at least 44% of the face reads as skin. Creeper green, wither and skeleton greys, zombie teal, enderman black, robot greys and blues read none.
  - Some skins will be misjudged either way, as the owner accepted: a masked human reads as not human, and a pig or a brown bear reads as human.
  - The check runs again whenever the reader's skin changes, and after every login.
- **The body is chosen from the read.**
  - **Age** comes from `AGE`: older for analog, middle for transitional, young otherwise.
  - **Clothes** come from a settled `SELF`: masc, fem, and plain clothes for androgynous, unseen or unread. Each presentation dresses each age differently.
  - **Hair** comes from `HAIR`'s routine:
    - coils for wrapped (the new cut on the shared painter);
    - braids for braided and oiled;
    - long for brushed dry;
    - close for washed and barbered;
    - short for unread.
  - **Tone and hair colour** come from the default skin Minecraft gives the reader's account: the commonest skin colour on its lower face, and the commonest colour on its crown. The self-portrait at the kitchen table is not built yet. When it is, its tone will take over.
- **The bodies are painted sheets.**
  - There are 45 sheets: three ages, three presentations and five hairstyles, all with wide arms.
  - They are painted by `tools/paint_character_skins.py` in the same pixel grid as every other person in the mod.
  - Each sheet is painted with grey skin and hair. Its lower half marks which pixels the painter took from the skin and which from the hair.
  - The painter's shading is added to its colours, so the client can recolour each body to the reader's tone and hair colour exactly as the painter would have painted it.
- **Where it shows.**
  - Inside the House's dimensions the body replaces the reader's skin everywhere a player is drawn:
    - their own first-person hands and third-person view;
    - other players' view of them;
    - the tab list;
    - mirrors and doubles.
  - The coats, glasses and veiled face of the House's version of the reader still show only in glass and copies, now over the body.
  - In the manor (its own rooms and the rooms between them) and the Overworld their own skin returns.
- **Others see it.** This is the one place a reader's read leaves the server. Every client is told which readers wear a body and which body, so friends can compare them. Readers with human skins never have any of their read sent. The README says so.
- **A server option,** `read.houseBodies`, turns the House's body off.
- **Protocol 91,** for the two new messages.

## The inventory shows no figure inside the House

Inside the House's dimensions the inventory's figure box is left empty. The reader sees themselves only as others draw them, or in glass. Any other creature drawn there, such as a horse, still is. The figure returns outside the House.

## The quiet rooms come before the deep interior

**What went wrong.** The House's depth is the number of doors a reader has come through since the hallway. At seven the ordinary halls turn to stone, and stories, trials and impossible rooms start from six and eight. The quiet rooms were dealt one at a time from 0.4.160, but only one door of a hall led to the next one. A reader who took another door went on deepening, so it was easy to reach the stone halls with quiet rooms still to come.

**What changed.**

- **Every door leads on.** While a reader has begun the quiet rooms and has one left, every door of the hall after a room leads into the next. The reader goes room, hall, room, hall; no door can miss the next room.
  - The 60%, 80% and certain chances are retired.
  - Each door is still dealt an ordinary hall, where the room's way on leads afterwards.
- **No depth among the quiet rooms.**
  - Crossings made while a reader has quiet rooms to come are kept on their way back but don't count towards depth.
  - A reader who was already deep when the quiet rooms began is held in the domestic halls until they are done: no stone halls, stories, trials, impossible rooms or staircase.
- **The count starts again after them** (the owner: "reset the stone pass requirement"). When the last quiet room is entered, every crossing on the way back so far is held. Depth counts again from the hall after it:
  - stories can begin six crossings later;
  - the stone halls start seven crossings later.
- **What doesn't change.**
  - The way back is unchanged: retreating goes back through every door, held or not.
  - Readers who never began the quiet rooms deepen exactly as before. That covers observers and readers who never came in by the front door.
  - Readers who had already finished the quiet rooms keep the depth they have.

## The porch

- **The swing.** The two seat pieces used to lie on the ground under ropes that ended in mid-air. They are now one plank seat at sitting height, each half hung by its rope, which runs up into the rope above and on to the beam.
- **The carved rail.** It was an oak piece modelled like a small table, standing at the end of the white railing and joined to nothing. It is now the railing's last section by the steps: a white end post, and a top rail with the words carved into it that reaches on into the next rail's post, as fences join. Below the top rail it stays open: that is the gap a reader on the lawn looks through at the lit window.
- **Quiet-room layout 163.** A porch a reader has had built but never entered is rebuilt with both. Porches already entered stay as they are.

## Tests and proofs

- **The quiet-room job now runs 36 cases.**
  - **New: the House's body.** It checks:
    - the face check, with the hat layer over the face counting;
    - skin tones from pale to deep reading as skin, and greens, greys, blues and black not;
    - the body following the read, with the clothes waiting for a settled presentation;
    - only an inhuman-skinned reader inside the House wearing a body;
    - every reader there being told that reader's body and no other, nothing sent again until something changes, and everyone told when the reader leaves;
    - the server option and a skin that is human again.
  - **New: depth among the quiet rooms.** It checks:
    - a reader who never began deepening as before;
    - a reader already deep being held at the domestic halls;
    - twelve crossings among the quiet rooms adding no depth;
    - every door of a hall leading to the next room and afterwards to a domestic hall;
    - the count starting again when the last room is entered, with stories six crossings later and stone seven;
    - going back undoing those crossings and keeping the older way back;
    - all of it saved.
  - **Updated: the junction case.** Every door of the hall after the first room is owed, and each is dealt an ordinary hall.
  - **Updated:** two cases now count the crossings on the way back rather than depth, which no longer grows among the quiet rooms.
- **The connected-client tailoring proof.**
  - Client B's reader wears a skin that is not human. In the coatroom phase both clients must draw B in the House's body and A in A's own skin.
  - Each client then opens its inventory inside the House and captures it (`A-inventory.png`, `B-inventory.png`). Fewer than one in fifty of the figure box's pixels may be lit.
  - A then turns to B and captures B in the House's body (`A-body.png`).
  - CI prints the mirror, body and inventory captures together for review.
- **The native art proof has eleven object pages, 22 images in all:**
  - the porch's swing and railing as they stand together;
  - ten House bodies on the production player model, each recoloured to a different account's default skin.

No layout or Witness change outside the quiet rooms: 54 / 41 / two kinds / three endings.
