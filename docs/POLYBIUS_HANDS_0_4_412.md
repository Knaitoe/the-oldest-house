# Block Party: what a fighter holds (0.4.412)

The owner's note on Block Party (docs/POLYBIUS_PARTY_0_4_224.md) asked for equipped items to be held and moved properly. Until now
every item was drawn as a small icon a few pixels in front of the doll, turned only by the way the fighter faced: the sword floated
beside the body, the bow never drew, and a punching arm and a sword swing were drawn separately.

## What changed

- **The arm holds the item.** The front arm turns at the shoulder, from hanging through straight ahead to straight up, and is drawn
  over the body and head. The item picture is placed by the pixel a hand would close on (the sword's hilt, the bow's grip, the
  bottle's neck), so it moves with the arm.
- **Knight:** the sword rests low and forward. A swing raises it behind the head, with the blade trailing, and brings it down
  through the front. A finisher holds the arm out and spins the blade right round. The shield is drawn after the doll, in front.
- **Archer:** the bow is held low at the side between shots. When it shoots, the arm comes up straight and the string draws back
  through the vanilla pulling pictures, then springs home.
- **Alchemist:** the bottle is lifted back over the shoulder and thrown forward. Once it has left the hand the hand is empty, and
  the bottle in the air is the thrown one.
- **TNT:** carried in the hand held overhead.
- **Brawler:** the punch is thrown straight out, as before.
- Turned to the left, the arm and what it holds are mirrored with the doll.

## Made

- `PolybiusBrawlScreen` only. `doll` takes an arm angle and what to draw in the hand. `arm` gives the angle for each class and
  stage of its swing. `held` draws the item. `inHand` places a picture by its grip and turns it.
- The game, its timings, payloads and protocol are unchanged (168).
- New builds only.

## What stays

- **Witness:** 85 sources / 64 resolutions / two kinds / three endings.
- **Layout:** 134.
