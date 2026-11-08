# The dance has weight, drawn hair, the porch door, the mailbox, the laundry baskets (0.4.397)

Five of the owner's notes from November 8.

## The dance in the rain has weight

The owner: "the dancer in the personhood vignette needs weight, so the player swings when they move while dancing." This is the shelter, the quiet room with the slow dance in the rain.

While a reader holds a partner, their keys no longer walk them about the floor (`DanceWeight`, run on the reader's own movement by `DanceClient`):

- **A push to either side turns them both about the partner.** The turn builds while the key is held, up to about one full turn in two and a half seconds.
- **Let go, the swing carries on.** It dies away over a couple of seconds, as a weight does.
- **A push away leans back against the partner's hands.** The hands give a little, then draw the reader back to arm's length when the key is let go.
- **A push in closes only so far.** It never reaches the partner.
- **The view turns with the swing.** The partner stays in front.
- **The partner keeps their ground.** On the server they close on the reader slowly while the two dance, so the reader is the one who swings. Stepping back after the dance, they move at an ordinary pace, as before.

In the dance with both strangers, the reader swings about the point between them.

Only the reader's own movement changes. The partner, their sway, the reader's arms and the dance's timing are as they were.

## Drawn hair colours the reader's hair

The owner: "coloring the hair in hair vignette should color the player's hair, too."

At the kitchen table, the body, face and skin tone the reader draws were already worn on the House's body as soon as they were drawn. The hair waited until the picture went up on the fridge, and a blue or green crayon was ignored.

Now the hair works like the rest:

- The reader's own hair takes the drawn colour as it is coloured in.
- Colouring over it changes it again.
- Any crayon counts, so blue or green hair is blue or green.

As before, this applies only inside the House, and only while the House reads the reader.

## The door inside the porch's house opens

The owner: "the far door works for the kitchen vignette. the door inside the home does not work." In the porch room, what the reader does first when the whistle blows is their answer, and the doors stay shut until there is one. Going in through the screen door counted only at the moment of stepping in, during the ten seconds after the whistle. A reader who went into the house before the whistle, or was already inside when it blew, never had an answer recorded. The door inside the house stayed shut for good. The far door worked only because walking down the road records an answer of its own.

Now a reader who is in the house during those ten seconds has gone in, whenever they stepped through.

## Every quiet room's door says what it waits for

The kitchen's locked door has said what it waits for since 0.4.202. The other quiet rooms said only *The door won't open.* Now each says a few plain words more, without naming an answer:

| Room | Line |
|---|---|
| Waiting room | *The door won't open. They haven't called you yet.* |
| Bathroom | *The door won't open. Something by the glass is yours to use.* With something in hand: *What's in your hand is for the glass.* |
| Coatroom | *The door won't open. Dry things hang on the rail.* |
| Shelter | *The door won't open. Nobody has put a song on yet.* Once it plays: *The song isn't over.* |
| Porch | *The door won't open. Supper isn't on the table yet.* After the whistle: *Someone is calling you in.* |
| Kitchen | As before. |

## The porch's mailbox

The owner: "please improve the mailbox texture." The porch's mailbox shared the generic metal, oak and cloth textures and stood as a square box with a stick for a flag. It is now a galvanised rural box with its own atlas (`tools/draw_house_read.py`):

- **The box.** A rounded top in two steps, pressed with ribs and spotted with rust. The door has a rolled rim and a pull latch.
- **The flag.** It lies down on its arm along the side, since the box is empty.
- **The post.** Weathered cedar, with a cross-board under the box.

Every face is mapped to its own part of the atlas, pixel for pixel. The block's outline follows the new shape.

## The laundry baskets answer a hand

The owner asked whether the laundry corridor's baskets of folded washing should be interactable. They now answer the way the bedroom hall's painted doors and the stacks' books do. A soft sound plays and a grey line shows, one in turn per reader:

1. *Folded sheets. They are still warm.*
2. *Someone has folded these since you came in.*
3. *The top one is folded the way you fold yours.*
4. *Still warm.*

Nothing comes away, and they give no mark and no credit.

## Tests

- **`HouseReadTests.theTwoOriginalStrangersComeOnlyWhenLookedAtAndADanceEndsTheSong`** now runs the swing as well:
  - pushed round for a second and a half, the reader swings a quarter turn or more and stays in the partner's hands;
  - the view turns with the swing;
  - let go, the swing carries on, then dies away;
  - leaning away, the hands give and hold, then draw the reader back;
  - pressing in comes closer, never into the partner.
- **`HouseReadTests.theKitchenTableReadsOnlyTheFaceTheReaderDrawsAndOpensOnceThePictureIsUp`:**
  - green hair colours the reader's hair at once;
  - colouring over it changes the hair before the picture is hung;
  - the fridge keeps the last colour.
- **`ExplorationPassTests.theLaundryCorridorIsCrossedThroughWashingThatSwingsBehindWhoeverPartsIt`:** a basket, tried twice, answers and gives nothing away.
- **`HouseReadTests.porchWhistleVoiceAndAllQuietWaysOnRemainPersonalAndTheRailWritesUnseen`:** before the whistle, the locked door says supper isn't on the table yet; after it, the door says someone is calling the reader in.
- **New case `HouseReadTests.aReaderAlreadyInThePorchHouseWhenTheWhistleBlowsHasGoneIn`:**
  - a reader goes into the house before the whistle, and the door inside stays shut and says what it waits for;
  - once the whistle blows, the reader has gone in, the door opens, and the House reads home as a haven, as it does for a reader who steps in during the whistle.
  - The house-read job now counts 45 cases.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
