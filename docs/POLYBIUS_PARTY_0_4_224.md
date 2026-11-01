# Polybius: Block Party's classes, combos and items; the pizza counter (0.4.224)

The owner, 1 November, on Block Party (docs/POLYBIUS_ARCADE_0_4_221.md): "Very cool design though! I'd love to make it more
mechanically complex. Let players pick a class, add combos, items." And: "Add a pizza and soda booth/vendor, too! Describe the pizza
as overly greasy, staffed by a teenager with a poor attitude."

The black cabinet, the men, the walk, the account, the tickets and the prize shelf are unchanged.

## Block Party

### Classes

While the cabinet counts down to the start, left and right pick who each fighter plays. Each stick starts on a different class.

| Class | Health | Pace | Attack | Finisher | Special (wait) |
|---|---|---|---|---|---|
| Brawler | 20 | quick | a punch | an uppercut that stuns | a dash punch through everything in the way (4 s) |
| Knight | 26 | slow | a sword with a longer reach | a spin that cuts both ways | the shield up for two seconds (5 s) |
| Archer | 16 | quickest | an arrow, or a kick up close | an arrow that goes through everything | three arrows at once (4.5 s) |
| Alchemist | 18 | middling | a thrown potion that splashes and slows | a bigger splash | a healing splash for every fighter near (8 s) |

A fighter waiting to come back in (after a lost life, or just dropped in) can pick again with left and right; the screen shows the
count and the class.

The Knight's shield stops what comes at it from the front: a zombie's or spider's swing, an enderman's blow, a skeleton's arrow, a
creeper's blast.

### Combos

Hits landed one after another keep a chain going for a second and a half. The chain shows over the fighter (x2, x3...). Every third
hit of a chain is the class's finisher, and the screen says FINISH. Something knocked down while a chain is going scores more, up to
three times its worth at a chain of ten. Being hurt breaks the chain.

### Specials

Their own key: K or Shift. A bar under each seat's health fills as the wait runs down.

### Items

What goes down sometimes leaves something in the street (about one in eight); walking over it takes it.

| Item | What it does |
|---|---|
| Steak | six health back |
| Golden apple | all health back, and three seconds when nothing can hurt |
| Potion of Swiftness | half again as fast, for ten seconds |
| Potion of Strength | hits land harder, for ten seconds |
| TNT | the next attack throws it instead (up to three carried); it goes up where it lands |
| Totem of Undying | a life |

What is in effect shows under each seat's name along the top. An item left too long fades, and is gone after twenty seconds.

### Spiders and endermen

From the fourth wave spiders come too: low, quick, and coming in a bound when they're close. Every fifth wave ("SOMETHING TALL")
ends with an enderman: forty health, a heavy blow, and every four seconds it steps from one fighter's side to another's. It always
leaves a golden apple.

All of it is drawn from the game's own pictures: the classes' sword, bow and potions in hand, arrows and TNT in the air, potion
splashes and blasts as widening rings, the spider and the enderman from their own textures, eyes and all. Nothing flashes.

The 0.4.223 standoff, where a zombie or creeper stops a fist's length beside its fighter, had its sides crossed: one coming from the
right made for the fighter's left and walked through them. It keeps to its own side now (fixed in 0.4.223 itself, before it was
pushed).

## The pizza counter

Along the east wall, where a taped-up cabinet stood: a counter with a warmer of slices under a lamp and a stack of napkins, a back
counter with a soda fountain, pizza boxes (a chef with a moustache on every lid) and a conveyor oven, and a menu board above with a
slice and a cup and their prices. One taped-up cabinet is left.

Behind it, a teenager in a paper visor and an apron with a slice on it, the same one for everyone (painted like the rest of the cast,
stranger phase 50), slumped on the counter on one arm. They don't look up. Speaking to them gets one of their lines ("It's pepperoni.
It's always pepperoni. The other kind ran out in, like, spring."; "I'm on till close. There isn't a close. That's a joke. Kind of.").

- **A slice of pizza**, four tickets, off the warmer: a meal (six hunger), "Shining with grease. The paper plate has gone see-through."
  Eating it: "The cheese slides off in one sheet." / "Grease runs down to your wrist." / "It folds under its own weight and drips."
- **A fountain soda**, three tickets, off the fountain: drunk, not eaten, always drinkable, and a little quicker on your feet for
  fifteen seconds. "Mostly ice. Flat."

Without the tickets: "That's four tickets. You've got two." They go back to the magazine.

## The portraits' eyes

The owner: "Do you not see the eyes of the portraits." Every portrait had one eye with a dark pupil and the other a pale smear. A
bust's head is ten pixels wide, and a face painted at sixteen pixels was squeezed into it by skipping columns; it skipped one
iris and kept the other. The cast's painter also turns the right iris in a little, so even kept it sat in a different place.
The bust now keeps a fixed, mirror-symmetric set of the face's columns and rows (`PortraitPainter.COLS2/ROWS2`, and the same in
`tools/draw_manor_portraits.py`): both eyes whole (white, iris, white) under their brows, the nose between them, the cheeks. The
manor's five family portraits are repainted with it and look straight out of the frame (level irises); their hashes are
recorded again in `art/inventory/manor_portraits_04223.json`. Portraits taken in the House are painted the same way, and one
from a vanilla-size skin no longer doubles a column right of centre (its eyes are centred).

## Not changed

The black cabinet, the men, the walk and the account; the three games and their tables; the prize shelf; the Witness pool. No new
payloads; Block Party's state carries the new fields. New builds only.

## Tests

Two new cases in `PolybiusTests` (literary namespace):
- **Classes, chains, specials and items:** each stick starts on its own class; left and right before the start pick another, with
  its own health; three punches in a row are a chain and the third is the uppercut (5 + 5 + 9); the brawler's special dashes
  forward; a steak heals, a totem is a life, and carried TNT goes with the next attack in place of an arrow and goes up where it
  lands.
- **The pizza counter:** one teenager, the same for everyone; the warmer and the fountain on the counter; no tickets, no slice;
  four tickets buy a slice and three a soda; the slice is a meal.

The native character review lists the teenager.
