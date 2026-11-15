# The Ones Already Inside 4: Rae's request, the rooms, the bodies in the beds (0.4.604)

The plan's fourth slice (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): "N1 and N2: Rae's request and the rooms, the doubles in the beds
(the reader's House body), the first eyes opening." In 0.4.602 the four were where the script's first scene has them and answered a
hand, and nothing came of what was said. Now the first two scenes play through. The House still does not deal the house.

New builds only. No layout, protocol or Witness change.

## What happens

The house keeps one chapter for everybody in it (`LodgingStory`), with its own clock, which counts only while somebody is reading in
the house. Everybody leaving holds the chapter where it is.

1. **Rae asks.** As in 0.4.602, Rae makes her request of each reader for themselves. When every reader in the house has answered her,
   or a minute of the house's time after she first asked anybody, she says "I'll go up and find her. She won't have heard the door."
2. **She goes up.** She walks up the stair and along the corridor to the workroom (she knows the way: the script's contradiction a
   reader can notice), opens its door, goes in and shuts it behind her. Dan stays on the bench.
3. **A moment out of sight.** Three seconds.
4. **Vera comes down,** Rae behind her, the workroom door opened and shut again, and stands in the hall.
5. **Vera's four lines** (N2), over her head for anybody near, a few seconds apart. With the first, every reader in the house is
   given a guest room: their name is on a card in the holder at its door, and in its bed, asleep under the blanket, is a body with
   their face. Her last line, "Let's have a look at you. Not here.", is to Dan, and he gets up.
6. **Upstairs.** Vera takes Dan up to the workroom; Rae goes with them, a little behind. Passing a guest room whose door stands open
   with a body in its bed, Dan says "That's not how it was. That's not how it was for me." once.
7. **Settled.** The three of them in the workroom: Vera at her work, Dan standing with his hand in his pocket, Rae by the door.

A reader who comes in after the rooms are given out has one given them as they arrive, while there is one left. There are six guest
rooms; they are given in an order that spreads readers out along the corridor.

Rae and Vera walk on their own feet: up and down the treads a step at a time, never through a wall or a door, opening a door as they
come to it and shutting it once nobody is in the way.

## The bodies in the beds (`LodgingDoubles`)

- **Whose it is.** Each is the reader's body as the House has it, their skin and the House's hair, wide or slim as their own, a little
  smaller than they are (.94). It lies on its back in the bed, the head on the pillow, the blanket up to the chest and the shape of
  its feet under it. Only the head shows.
- **Asleep.** It breathes. Its eyes are shut under lids drawn from its own cheek, and under the lids the eyes move, still for a while
  and then a few quick moves, the same both sides.
- **Never made in sight.** A body is laid in its bed only when nobody (spectators included) can see the bed, and only when the room is
  loaded. Each reader has one, and only one.
- **Its eyes open.** When its reader has stood beside the bed for three seconds, it opens its eyes. It does not look at the reader. It
  turns its head on the pillow toward Vera, wherever she is in the house, and keeps it there.
- **A hand on it** tells the reader what they see: their own, asleep or awake, or another reader's, by name.
- **A hand on one's own** opens the panel with the script's four replies: "Who is that?" / "What is this?" / "I'm not staying in
  here." / "Leave it." Vera answers the first two with her three lines ("That's yours. It isn't finished." ...) and the third with
  "Nobody makes you. The room is yours either way." "Leave it." closes the panel. A reply asked is not offered again.
- **A blow** does nothing to it. If Vera is near enough to hear and can see the reader, she says "Don't. It isn't anybody's yet.
  You'll only spoil the work." in the panel; otherwise the reader is told nothing marks where the blow landed.
- **Everybody sees them.** The bodies are not private: a reader looking into another's room sees that reader's body.

## The card holders

The guest rooms' card holders (0.4.550) were a bare plate. They are a small brass frame now, with lips to hold a card, and the card
(stage 1) sits in it. The name on the card is lettered onto it in a dark ink, sized to fit the card. Drawn by
`tools/draw_lodging_things.py` (`card_holder`, `name_card`).

## The stair

Rae and Vera walking the stair found that a body going from the third tread to the fourth put its head into the gallery floor: the
stairwell was open over only two blocks of the gallery. It is open over three now (the gallery floor stops a block further from the
landing), with the rail run on round the new edge. A reader's head had the same trouble.

## Every line that is new here

The script's lines (N1's end, N2, Dan's line at the door, Vera's answers and the four replies) the owner has had since 0.4.549. These
are new in this release and are mine:

- Rae: "I'll go up and find her. She won't have heard the door."
- In the house's grey, at a body:
  - Its own reader's, asleep: "Asleep under the blanket is a body with your face. The skin is too smooth. Its eyes move under the
    lids."
  - Its own reader's, awake: "Its eyes are open. It is not looking at you."
  - When it wakes: "It opens its eyes. It does not look at you. It turns its head on the pillow and looks toward the rest of the
    house."
  - Another reader's: "Asleep under the blanket, a body with (name)'s face. Its eyes move under the lids." / "Its eyes are open. It
    has (name)'s face, and it is not looking at anybody."
  - Struck, out of Vera's hearing: "Nothing marks where the blow landed. It goes on breathing."
- In the house's grey, a hand on Rae once she has gone: "Rae keeps close to Dan." On Vera while she is busy: "Vera does not stop for
  it."

None of it says what the reader is like, how they feel or what they are called, except a name on a card and in "(name)'s face".

## Decided here (any of it can move)

- Rae goes when everybody has answered or after a minute, whichever comes first.
- Vera speaks from the hall, not the gallery (the script has her come down the stair; the hall is where everybody is).
- The rooms are given out all at once, on Vera's first line. A late reader is given one as they arrive.
- Three seconds beside the bed to wake a body; it then looks toward Vera for good.
- Dan's line at a door is said once, whoever's room it is.
- Nobody touches the bodies' rooms yet: no thing of the reader's own stands on the washstand (later, from the personhood read).
- After the rooms, Rae and Vera answer a hand with a grey line rather than speech. The next slice (N3) has Daniel.

## What was built

- `LodgingStory`: the chapter (stages `ASKING`, `RAE_UP`, `FETCHING`, `COMING_DOWN`, `ADDRESS`, `UPSTAIRS`, `SETTLED`), saved with the
  house; where each of the four stands at each stage; Rae's and Vera's walks (up, down, and Dan's with them), each step's height
  taken from the floor under the toe, middle and heel of the foot, and a body held where it is rather than stepped into a block; the
  workroom door; the rooms, names and cards given out; Dan's line.
- `LodgingDoubles`: the bodies; the cards; the wake; the panel at one's own body; the blow.
- `LiteraryActor`: role `LODGING_DOUBLE` (49): no address, not pushed, a blow and a hand go to `LodgingDoubles`, a low body (.6 by
  .45) so it lies in the bed and is not walked into.
- `client/LodgingRenderers` (new): the body in bed (the player model, head only, laid down by the bed's facing), the blanket and sheet
  over it with the chest rising, the lids and the moving eyes under them, and the House's hair on its own reader's screen.
- `LiteraryRenderers.CastRenderer`: hands the role to it, and draws it for every viewer.
- `LodgingPeople`: the four take their spots from the chapter; heads stay still while walking; Vera turns to Dan for her last line;
  Rae and Vera's hand lines once they are busy.
- `LodgingTexts`: the lines above.
- `LodgingSite`: the stairwell and its rail.

## Checks

- `LodgingHouseTests.theRoomsAreGivenOutAndEachReaderFindsTheirOwnBodyAsleepInTheBed` (namespace `lodging`, now five), with two
  readers: Rae waits for both answers, walks up by the stair and shuts the workroom door; Vera comes down; each reader has a room,
  the card at its door has their name, and a body is in its bed, owned by them, asleep, one each, and not made while either reader
  could see the bed; a blow does nothing; standing beside the bed wakes it, and it turns its head; the awake line; Dan sees an open
  room and says his line once; the three end in the workroom; a hand on one's own body opens the panel and Vera answers.
- The 0.4.602 people case holds the chapter, so Rae is still in the hall when it looks for her; its rail check follows the new edge.

## Run here

- The lodging, cast, two-reader and architecture namespaces whole (118 cases, all passed).
- The local client frame tool, three frames of a body in the guest bed: asleep from above, asleep from the side, awake with its head
  turned. Two things it found and that are fixed: the head was drawn at a child's size, so it sat a hand down the bed from the
  pillow; and the lids stood off the face, so at a low angle a line of eye showed under each one.
- The release checks (below in the commit).

## Not covered

- **Nothing has been watched in play.** The frames are of one bed, by itself. Nobody has seen Rae or Vera walk the stair in a client.
- **The bodies with readers' own skins.** The frames used the default skin and no House hair.
- **Leaving and coming back mid-chapter.** The chapter holds while the house is empty; no case leaves and returns.
- **A seventh reader.** Six rooms; the seventh is given none. The plan's second house for a seventh reader is slice 18.
- **Two readers waking their bodies at once,** and a reader in another's room when it wakes.

## For the owner

- **The bodies' faces.** They are the reader's own skin face, with the House's hair, drawn at .94. Say if they should look more
  wrong ("too smooth, the hands without lines" is under the blanket for now).
- **Rae's one new line,** and the grey lines at the bodies, above.
- **Vera in the hall** rather than on the stair for her four lines.
