# The shallows together — 0.4.157

The owner's October 28 note: "The shallows also needs to be designed for multiple players. The second joining player should be apart from the first, helping to throw Stacey. The rest can be participants. These extra participants do not exclude the existing npcs."

Until now each reader in the shallows had a recollection of their own: their own Stacey and their own Lonnie at her feet, in carry places seven blocks apart, with the boys on the bank speaking to each of them privately. With friends on the bank it is now one throw.

## Who does what

- **The first reader** has the recollection, as before: Stacey on the bank and Lonnie holding her feet. They take her wrists.
- **The second reader** to come while it is under way (before she is thrown) joins it and takes her feet, in Lonnie's place. Lonnie lets go and stands at the water's edge beside them, out of her swing, and counts. He says: "<name>, you get her feet. I'll count."
- **Anyone after that** stands on the bank with Bud, Hank and Clay and watches. Bud calls them over: "<name>! Come watch. She's going in." The three boys stay where they are and keep their lines. Lonnie stays too, counting.
- **Nobody else gets a recollection of their own** while one is under way, whether or not they have been through the drowned town's hunt. Readers who come after she has been thrown, or after someone struck her, start their own as before, if the shallows can be dealt to them.
- **Everyone with her** sees Stacey and Lonnie, hears every line and sound, and sees the splash. Nobody else is sent any of it.

## Throwing her together

- **Taking hold.** The thrower takes her wrists as before (empty hands, a click on her). Lonnie then tells the partner: "<name>, her feet. Then we swing her on three." The partner takes her feet the same way: empty hands, a click on her. They stand at the east end of her, facing the thrower. Her feet wait until someone has her arms: "Wait till <thrower> has her arms."
- **Swinging.** The lake is on the thrower's left and the partner's right, so each swings toward the water with that key and back with the other. The hint shows each of them their own keys.
  - A stroke goes only when both swing the same way within eight ticks of each other.
  - One pair of hands alone does nothing.
  - Swinging different ways gets Lonnie's "Together. Out on my count, back after."
  - The count, the boys' jeers and her pleading are as before: out on one, back, out on two, back, out on three, and both let go mid-swing.
- **Letting go.** If either of them sneaks, lets go or steps away, she is put down and both grips are gone. The count starts again: "<name> lets go of her feet." If the partner leaves the bank, Lonnie takes her feet again and the thrower can finish with him.
- **A blow.** A blow from anyone with her still darkens the visit, as a blow from the thrower always did.
- **Her landing** plays out as before for everyone there: she lies still, gets up wrong and runs across the water into the woods.

## What each of them keeps

When she breaks on the water:
- **The thrower** keeps the shallows as before (the throw, the essays, the Witness credit).
- **The partner** threw her too: they keep the Witness credit as one who acted.
- **Readers on the bank** keep it as witnesses.
- **The school pages remember everyone who was there,** hunted or not, so the dried essays name them all. The shallows are not dealt to any of them again, and the later lake nights (the costume contest's old Hank, Ray at the movie) know them as boys from the shallows.

Each Witness line says how they were part of it:
- **Her wrists, with a partner:** "I held her wrists while <partner> held her feet, and we swung her out over the water on three. The lake would not take her. The school pages remembered my name among the boys on the bank."
- **Her wrists, with Lonnie:** the line as it was.
- **Her feet:** "I held her feet while <thrower> held her wrists, and we swung her out over the water on three. The lake would not take her. The school pages remembered my name among the boys on the bank."
- **The bank:** "I stood on the bank with the boys while <thrower> and <partner, or Lonnie> swung her out over the water on three. Nobody stopped them, and I didn't either. The lake would not take her. The school pages remembered my name among the boys on the bank."

## Coming and going

- **Membership is saved.** A reader who joined stays part of the recollection while its reader is there and it lasts. A reconnect, or Stacey's body being briefly unloaded, does not hand them a recollection of their own.
- **Leaving.** A reader who leaves the bank leaves the recollection they joined. If its reader leaves, the recollection goes with them, and those who joined are let go.
- **On the client,** only the readers holding her swing her, so a reader watching from the bank keeps walking and their arms stay still. The partner's arms carry her toward the water on their right.

## How it is built

- **Records.** Each reader's shallows record saves who they are with (`With0157`) and their part (`Role0157`: feet or bank). The thrower's record saves who holds her feet (`Feet0157`). At the landing, each reader's record keeps how they were part of it (`Throw0157`: role, thrower, partner) for their Witness line.
- **`Shallows`** handles:
  - the joining (`hostHere`, `join`) and taking her feet (`liftFeet`);
  - the paired stroke in `swing` (each side's last swing, within `TOGETHER` ticks);
  - Lonnie's place (`lonnie`, at `edge`);
  - sending lines, reactions and sounds to everyone with her (`group`);
  - each participant's credit at the landing;
  - each role's Witness line (`witness`).
- **Visibility.** `LakeWitchEntity` and `LakeCongregantEntity` send the recollection's Stacey and Lonnie to the readers who joined it (`sharesMemory`).
- **Progress.** `IndianLakeProgress.joinedShallows` puts a participant in the school pages without the hunt; the thrower's own `completedShallows` still needs it.

## Tests

- **The 0.4.93 originals case** now checks the new rule:
  - a hunted reader who arrives while another's recollection is under way joins it, at her feet;
  - one girl and one Lonnie are sent to both and to nobody else, with one carry place;
  - after the girl and a boy are unloaded and the saved data reloads, repeated arrivals keep them absent and the one who joined stays with it;
  - the returned original is still shared and liftable, without a throw.
- **One new case** (a hunted thrower, an unhunted partner, an unhunted reader on the bank and a stranger):
  - one recollection; the roles are feet and then bank; everyone with her, and only them, sees her and Lonnie;
  - her feet wait for her arms; Lonnie moves to the water's edge; the bank cannot take hold;
  - the partner takes her feet facing the thrower; sneaking at her feet puts her down and clears both grips;
  - one pair of hands alone is no stroke, and different directions are no stroke; three strokes out together release her, with both grips gone;
  - all three keep the shallows (the partner as one who acted, the bank as a witness), and the stranger none; every one of them is in the school pages and is not dealt the shallows again;
  - each Witness line names the others; a carried essay names everyone.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings.
