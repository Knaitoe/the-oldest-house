# Every artifact does something: the first batch — 0.4.192

The owner: "most of the items you provided the player with don't really have an effect on the gameplay, either inside or outside of the house." The plan, an effect for every artifact, is in docs/SUCCESS_AND_FAILURE_AUDIT.md under **Every artifact does something**. This build gives the first nine of them their effects (`Keepsakes`).

Each works only for the reader it was left for: it checks the keepsake's owner mark, so a borrowed copy does nothing. Holding or using one gives no Witness credit, and the item itself is never changed.

| Keepsake | Effect |
|---|---|
| **The elk's tooth** (elk fan) | In the off hand, the keeper resists knockback by 0.6, the Minotaur's throw included. Out of the hand, it is gone. |
| **The bulb** (elk fan) | Used, it hangs a light (level 12) where the keeper stands, for two minutes, once every five minutes. It works in their own world and in the manor. In the halls it will not light, since their darkness is the House's, and it does nothing inside a room's scene, where the repair uses it. |
| **Father's knife** (Holy Rabbit) | A cow, mooshroom, pig, sheep, chicken or rabbit killed with it in the main hand drops one more cooked portion. It gives nothing extra while the Last Stop's elk follows them, since she spoils what they kill. |
| **The rabbit's foot** (Holy Rabbit) | Carried, it gives luck. |
| **The brother's photograph** (mapping) | Carried, the keeper's own tamed companions within 16 blocks take half damage. |
| **The wax cylinders** (Crimson Hall) | Put into an empty jukebox by their keeper, no monster spawns naturally within 48 blocks while it plays. Taking it out ends that. |
| **The diary's last page** (Devil's Rock) | Read at night in their own world, it says, in pencil, how many blocks it is and which way to their bed, or "You are home." By day it says nothing. |
| **The leaf of the vault register** (Usher, failed) | Carried in their own world, the undead do not take notice of them beyond four blocks, unless one of them has been struck by the keeper. |
| **The cap from the broken post** (Last Stop) | Worn in their own world, untamed passive animals within twelve blocks come after them as if led. |

## Tests

New `KeepsakeTests` with two cases on real players and mobs.
- **The tooth, foot, knife and photograph:**
  - the tooth weighs down its keeper and nobody else;
  - the knife's kill leaves one cooked portion, and another reader's kill leaves none;
  - the keeper's wolf takes half damage, and another reader's wolf takes all of it.
- **The bulb, cylinders, leaf and cap:**
  - the bulb's light comes on and goes out, and it waits before lighting again;
  - the ward holds while the cylinder plays, and ends when it is taken out;
  - with the leaf, the dead pass over the keeper until struck;
  - the cap leads a cow.

Both pass locally.

## Next

The plain photograph, Zampanò's leaves, Borrasca's compass and patch, and the Wheel's key are batch B. Build 7's new artifacts follow.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
