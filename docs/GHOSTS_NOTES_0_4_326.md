# Ghosts Set notes (0.4.326)

From the owner's notes of 7 November, the items about the Barrett home.

## John's hand

"Both of the dad's hands are injured, which isn't accurate to events." The painted skin marks the injury on the arm net that both of his arms share, so the bandaged mark showed on both forearms whenever he held or flinched at his hand. The cast model now covers the mark on his left forearm with plain skin from the same atlas, so only his right hand is hurt. The skin files are unchanged. The filmstrip has a "hand" page showing him from the front, both sides and flinching.

## The beds

"Improperly placed leg for bed." Each half of a bed had two legs on a diagonal, so one stood under the middle of the bed. The head half now has both its legs at the head and the foot half both at the foot. Nothing else in the mesh moved.

## Marjorie's desk and the drawer

"Drawer should not begin opened. Should also be rotated and placed against wall." The writing desk stood loose in the middle of Marjorie's room. It now stands against her east wall facing the bed, with her pinboard over it, her chair pulled out beside it and the boombox moved to the corner. The laptop on it turns with it. Its drawer follows the desk's facing and, when shut, sits in line with the edge of the desk top, with only the pull proud of it. It still slides open on the sixth day's take, toward the bed. The camera's capture point for the drawer moved with it, and so did the check that it can be seen from her doorway.

## The piles of books

"Give different text and textures to the piles of books in merry's room." Every pile in the house was the same denim-and-red slab, and each opened the same storybook, the kitchen's included. Each pile is now its own thing, drawn at native pixel scale as a stack of separate books, each with its cover, spine and page edges:

- **Merry's picture books**, on her chest of drawers: big, thin and bright. They hold the stories she copied in, as before.
- **Three library books**, on her floor: the school library's hardbacks in plastic jackets, with spine labels and a slip sticking out of the top one. They are overdue and stamped with Marjorie's name. The top one is about two sisters who run away to a lighthouse, and its corner is turned down at the page where they pack. On the back of the slip Merry has written M + M.
- **Marjorie's paperbacks**, on her chest of drawers: cracked spines, a horror novel with its cover torn off, and a list inside its back cover with some lines ticked.
- **Sarah's cookbooks**, on the kitchen counter: two swollen cookbooks and a ring binder. The binder falls open at the spaghetti sauce card, which ends "a spoonful of sugar to take the edge off". Beside it Merry has drawn a face with its tongue out.
- **The old box of books** in the basement keeps the old mesh and has its own text: a church-sale box, and a hymn book with another family's name in it.

Taken together with 0.4.325's open sugar canister and Merry's plan, the cookbook card and the library book say the same thing a different way, without anyone saying it.

## Barry in the panel

The panel showed "Barry" over "Barry: If she looks at you, just let her. We can ask it again." Lines from the person the exchange is with are no longer prefixed with their name; anyone else who speaks in it still is. His line is now "If Merry looks into the lens, keep rolling. We can always ask her again."

## Not done: "Fix texture here"

The note marked an object with a blue base and post and two cream planes crossing over it, on a dark wooden top. No model in the Barrett home matches it: not the lamps, the plants, the hat, the receiver, the light stands or the books. Its colours are closest to the quiet rooms' denim and lampshade textures. It is left until the owner says where it was.

## Validation

- A new Ghosts case checks five things on a new build: each of the five piles is its own kind with its own book; Marjorie's desk faces the bed with its back to a full wall; the laptop faces the same way; the reader's drawer starts shut; and the two piles in Merry's room open different books.
- All 40 Ghosts cases pass locally.
- The new lore view "bedrooms" shows the beds, the five piles and the shut drawer, and is listed in CI.
- `tools/draw_ghosts_bedrooms.py --check` joins the quick asset checks.
- Protocol 136, for the new block states.
