# The Ones Already Inside 7: the two accounts, and the weapon (0.4.607)

The plan's seventh slice (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): "N5 and N6: Daniel's account and Dan's; giving Daniel a weapon." Act
II begins when the glass has gone one way or the other (the injury is over, `LodgingStory.AFTER`). The House still does not deal the
house.

New builds only. No layout, protocol or Witness change.

## N5: Daniel's account

A reader who has spoken to Daniel before has his account once in Act II, in the panel, through any of the ways to him (his room, the
hatch, crouched at his door):

> Daniel: She said a week. I'd been working nights for a year. She said I could sleep a week and something would go out and do my days,
> and when the week was done it would come back, and I'd go out, and nobody would know.
> Daniel: It didn't come back. I asked her where it was. She said it was getting on well.
> Daniel: The food comes through the hatch. I mended the chair. I mended it again.
> Daniel: I want the key. I want my name. I want whatever's left.

Then, while his account is told, a reader has these replies (each once):

- "He didn't know." (only once they have heard Dan's side): he looks at them and says nothing to it.
- "If you hurt him, you hurt yourself. I watched it." (only once they have seen the rule: either cut, or the pin): "Then he'd better not
  make me."
- "Give it to him." (only while they hold a sword, an axe, a trident, a mace or shears, and he has nothing): he takes it in the hand the
  strap leaves free and holds it down against the frame. It is the reader's, and it is his now; it never drops. A reader who has not
  seen the rule is told "Good."; one who has may still warn him afterwards.
- "Wait." closes the panel.

## N6: Dan's account

In Act II Dan has his own account for each reader, once:

> Dan: There's a woman two doors down from me with a dog that's too old for the stairs. I carry it up for her, morning and night. Six
> years. Rae can tell you. She thinks I'm the nicest man in the street.
> Dan: That happened. I don't care what else happened. That happened.

A reader who promised to stand with him (Rae's question, 0.4.604) hears too: "When it comes to it, what does that mean? Standing with
me."

A reader who knows Daniel (has spoken to him, or saw his hand open) can tell Dan, until the two have stood at the frame together (the
pin): "He's still here, in the room at the back. She's still using him." Dan: "No. Vera told me he'd gone home. She said he had his own
life and didn't want this one." and "Show me." The house remembers that Dan has asked to be shown (`DanKnows0607`) for a later slice.

## How it looks

- **Daniel armed.** What he is given is drawn in his right hand, strapped at the wrist, turned to hang down along his leg against the
  frame, not held out at the room (`LiteraryRenderers.heldDown`). Seen in a review frame, front and side, with an axe.

## Every line that is new here

The accounts and Daniel's and Dan's answers are the script's. These are mine:

- The reply labels "Give it to him." and "He's still here, in the room at the back. She's still using him."
- "He looks at you, and says nothing to that."
- "He takes it in the hand the strap leaves free, and holds it down against the frame."
- "He has it still, held down against the frame." (grey, to a reader who comes to him while he has it)

## Decided here (any of it can move)

- Act II starts when the injury is over, whichever way it went.
- A weapon is a sword, an axe, a trident, a mace or shears; one, from the reader's main hand, and only if he has none.
- "Good." only to a reader who has not seen the rule; the warning stays open to one who has.
- Telling Dan is open until the pin, which shows him Daniel anyway.

## What was built

- `LodgingPeople`: Dan's account and the "still here" reply; Daniel's account and his four replies; `weapon`, `armed`; records
  `DanielTold0607`, `DanTold0607`, `HeardDanSide0607`, `ToldDanDrained0607`, `SaidDidntKnow0607`, `SaidWait0607`, `GaveWeapon0607`,
  `WarnedDaniel0607`; the house's `DanielArmed0607`, `DanKnows0607`.
- `LodgingTexts`: the two accounts, the replies and answers.
- `LiteraryRenderers`: the lodging house's people may hold things; strapped, a held thing hangs down along the leg.

## Checks

- `LodgingHouseTests.theTwoMenGiveTheirAccountsAndDanielCanBeArmed` (namespace `lodging`, nine): no account before the glass; in Act II
  Daniel's account to a reader who has met him; Dan's account and the "still here" reply; "He didn't know." once Dan's side is heard;
  the weapon given goes into his hand, out of the reader's, and the house has it; "Good." to a reader who has not seen the rule.

## Run here

- The lodging and cast namespaces whole (32).
- Review frames of Daniel holding an axe, front and side.

## Not covered

- **What Daniel does with the weapon** is later slices' (the confrontation, N10 and N11).
- **Dan being shown** Daniel at the frame, when asked: later.

## For the owner

- The four new lines above.
