# The Ones Already Inside 18: the accounts and the Witness (0.4.618)

The plan's eighteenth slice (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): "The accounts and the Witness: each reader's account from their
own record; the source dealt; the co-op lines; a second house for a seventh reader."

New builds only. No layout or protocol change. The logic is `LodgingEnd`. No new models, poses or textures.

## When the house has ended

The house has ended once one of the four ends has played out to its last image:

- E1, Daniel returned: the return's coda is over (the dog named);
- E2, Dan completed: the completion's coda is over (his hand over his mouth);
- E3, the house severed: both men are on the bench;
- E4, neither Daniel: Rae has come in to the laid table (or anything after it: a reader stopped Vera, or broke the support).

At that moment the house writes down who had come into it (`EndedParty0618`, from `Readers0617`). Every one of them, in the house then
or coming back to it later, finds their account on the bench. A reader who came in after the end has none.

## The account

Read on the bench, having read the terms on the hall table, it resolves the story once, of the **understanding** kind, with the
house's end as its outcome (`returned_daniel`, `completed_dan`, `severed_the_house`, `neither_daniel`). The House then never deals the
lodging house to that reader again; until then it does (`LodgingHouse.dealable`).

The account is written from that reader's own record, in the order things happened: the terms, coming in late, upstairs, the
double in their bed, Daniel in his doorway, the cut on both, the two accounts, the weapon, Rae's confession and the key, Vera's offer,
the cellar and the third way, the passages and the body made from theirs, then whichever end they saw and their part in it. It says
nothing of what that reader never did, saw or heard, and ends at the door.

## The Witness

One new source of the understanding kind: **89 sources / 67 required / 65 with a first paper**; two kinds; three endings
(`gametest/WitnessPool`). The Witness line is written by the house's end.

## Co-op lines and the seventh reader

Already in place and checked here: the co-op line ("%s was in the lodging house with me.", `CoopAccounts`); six readers to a house,
the seventh given a house of their own (`SceneInstances.PARTIES`, `LodgingHouse.PARTY`). New: a house that has ended takes nobody
new, so a later reader is given another house rather than walking into the aftermath of somebody else's ending.

## Records

House: `Ended0618` (1 returned, 2 completed, 3 severed, 4 neither), `EndedParty0618`. Each reader's own: the literary `Ready`,
`Outcome` and `Completed`.

## New lines for the owner

Every line here is new.

Account:
- "In the bed in your room something slept with your face." / "…, and its eyes opened."
- "At the back of the house a man stood strapped in one of those doorways. He had Dan's face."
- "When Dan was cut, the same cut opened on Daniel."
- "Each of them told you the life was his."
- "You put something heavy in Daniel's hand."
- "Rae told you she had known all along that he was alive."
- "You turned the key in his straps yourself."
- "Vera asked for help keeping one of them quiet while she mended the other." / "…, and when you pressed her she said the other
  would not come through it."
- "Down there you learned that all of it could be let go at once."
- "Afterwards he kept a hand over his own mouth, and Rae kept trying to take it away."
- "The terms were still on the hall table when you went out."

Witness, by end:
- Returned: "In a lodging house one man had been made out of another. The made one went back into the doorway he came from, and the
  other came out and asked the name of an old dog."
- Completed: "In a lodging house one man had been made out of another. The first was held in a doorway until there was no room for
  him, and the made one kept a hand over his mouth."
- Severed: "In a lodging house one man had been made out of another. The house let go of both, and they sat in sight of the front
  door and could not help each other."
- Neither: "In a lodging house one man had been made out of another. They died of the same blows, and two places were laid for
  whoever came next."
- Story text: "I stayed in a lodging house where one man had been made out of another, and both of them were still in it. The house
  came to one of its ends while I was there."

## Decided here (any of it can move)

- The kind is understanding: what the readers do there is learn what the two men are and choose what the house comes to.
- Every reader of the party gets an account whatever part they had, as at the Pell house; withdrawing before the end and coming back
  still finds it.
- The advancement's icon is the release key.

## Not yet

- Personhood fragments: slice 19. Aftermath: slice 20.
