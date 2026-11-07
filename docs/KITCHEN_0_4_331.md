# Cooking, and people at the table (0.4.331)

From the owner's notes of 7 November:

> With the kitchen existing as a safe space for the player, maybe we could add more to that. Cooking is such a core part of domestic life and can impart so much of a person's history and culture. How can we impress that upon the house? It should feel organic and straightforward – I'm thinking developing a cooking system that offers slight buffs in the house, OR maybe NPCs that accept food and respond to specific dishes in specific ways. This wouldn't gauge the ethnic nature of the food but the broad strokes of it – texture, seasoning, cook?
>
> Add NPCs that visit the kitchen? Truant? Holloway? Maybe unnamed NPCs – other travelers, or NPCs from the lengthier vignettes? There are some NPCs that claim to leave their homes or houses.

This release does both. The kitchen is the familiar kitchen (0.4.232), the one room everyone shares and where nothing frightens anyone. Nothing here is a story, so there is no Witness credit and no account.

## The stove makes dishes

The stove still cooks one kind of thing on its own, four at a time, and never burns it. Put anything else in with it and the ring becomes a pan with a dish in it. The pan takes up to six things, one at a time:

- food (meat, fish, potatoes, carrots, eggs, bread, fruit, mushrooms, kelp and so on);
- a seasoning: dried kelp, sugar, honey, cocoa beans or blaze powder;
- a bottle of water or a bucket of milk.

The bottle or bucket comes back to the hand that held it. A dish can also be started from scratch with anything that is not ordinary roast food.

A dish is three broad strokes and nothing more:

- **Texture.** Crisp if it was fried dry, soft if water went in, creamy if milk did.
- **Seasoning.** What most of the things in it taste of:
  - salty: dried kelp or kelp;
  - sweet: sugar, honey, apples, beetroot, pumpkin or melon;
  - sharp: berries;
  - earthy: mushrooms;
  - bitter: cocoa;
  - fiery: blaze powder.

  With no seasoning it is plain. With three or more kinds it is muddled. A tie goes to the stronger taste.
- **Cook.** How long it was on the ring. A pan with water or milk in it takes longer.
  - **Underdone** if it comes off early.
  - **Cooked through** once it smells done (ten seconds dry, fifteen with water or milk).
  - **Well done** if it is left a while after that.
  - **Burnt** past that point, when the ring clicks off by itself. The kitchen says when it smells done, when it starts to catch, and when it has burnt.

A hand takes the pan off at any time. What comes off is a dish named by its strokes, such as "Salty stew", "Sweet fry" or "Creamy soup". Under the name it says how well it was cooked and who made it. It comes on a plate or in a bowl, or a burnt plate or bowl. How much it fills depends on what went in and how well it was cooked. A soft or creamy dish leaves its bowl behind.

## Eating one in the House

A dish that is cooked through or well done gives a slight lift inside the House (well done lasts a little less):

| Stroke | Lift | Line |
| --- | --- | --- |
| Soft | Regeneration, six seconds | "Warm all the way down." |
| Creamy | Absorption, thirty seconds | "Filling." |
| Salty | A little more saturation | "It stays with you." |
| Sweet | Haste, a minute | "Your hands feel quick." |
| Sharp | Speed, twenty seconds | "Awake, all at once." |
| Earthy | Resistance, twenty seconds | "Heavy, in a good way." |
| Bitter | Clears poison and hunger | "It clears your head." |
| Fiery | Fire resistance, a minute | "Warm right through." |

Nothing in the table lets a reader see in the dark, breathe under water or jump further, so none of it undoes the House's dark rooms, its dives or its climbs. An underdone dish gives no lift ("Not quite done in the middle."), and if raw meat or fish went into it, it does what raw meat does. A burnt one "tastes of the pan." Outside the House a dish is just food.

## People at the table

While someone is in the kitchen, every few minutes of that time, someone else comes in by one of its far doors, sits at the table for a while, and goes. One comes at a time, and it is the same visitor for everybody there.

- **Johnny**, from his places. He likes things crisp, sharp or salty, and doesn't mind them burnt. He can't stand sweet, or creamy ("It's like eating a pillow").
- **Holloway**, only while he lives. He likes salt, crisp, and cooked hard. Soft food is slop to him, and he won't take food from a reader who has been in his barrel.
- **The tenant from Vacant Possession**, only once a reader in the kitchen has carried the last of their things down to the van. They have no kitchen of their own at the moment. They like things soft and plain or sweet, and cooked through; sharp or fiery food is hard on them.
- **Someone passing through**, another person lost in the House, looking for the door from their own hallway. Anything hot and soft, salty or earthy suits them. Bitter or muddled food does not, nor anything underdone.

A visitor can be talked to: what they'd eat (which tells a reader how to cook for them), where they have come from, or "Stay as long as you like." The reader's side shows over their head like any other spoken reply (0.4.329).

Held out, food is taken. Any food will do, read in the same broad strokes:

- roast meat and bread are crisp and plain;
- stews are soft;
- raw meat is underdone;
- fruit and sweets are sweet.

The visitor eats it and says what they make of it:

1. If it was underdone or burnt and they mind, they say so.
2. Otherwise, if it is something they can't stand, they say that.
3. Otherwise they say how much they liked it.

The first time a reader cooks them a dish they love, they say something about themselves they had not said. A bowl comes back to the hand that held it out. Two meals a visit is all anyone eats.

Each visitor remembers each reader: what they were last given and whether they loved it. Next time, they bring it up ("Last time you gave me something crisp and salty, well done").

Fed, a visitor stays a minute more. Unfed, they stay a couple of minutes and say they are hungry now and then. Then they get up, say a word and go out by the door they came in by. Struck, they get up and go at once. If everyone leaves the kitchen, so do they.

## The kitchen's book

On the counter by the door lies the kitchen's book, "What was eaten here", for anyone to read and nobody to take. It keeps the last forty meals given to visitors, newest first: who ate what, who made it, who gave it and what they said. For example:

> Holloway ate something crisp and salty, well done, made by Sam, from Sam and said: "That's a proper camp meal."

This is how the kitchen keeps what its readers cook.

## Art

- `tools/draw_kitchen_dishes.py`: five sixteen-pixel dishes and their item model.
- `literary_kitchen_holloway`: Holloway on the cast's layout, painted from his own look by `tools/paint_character_skins.py` and catalogued on stranger phase 101. Johnny, the tenant and the traveller wear Johnny's, the tenant's and a stranger's existing skins.
- The kitchen's chairs now seat an actor: the seat is ten pixels up.

## Validation

- `KitchenTests#aPanOfMoreThanOneThingIsADishByItsStrokes` checks:
  - the strokes on their own (underdone chicken with sugar, a muddled pan, a burnt creamy one, a tie);
  - a real pan: beef alone cooks the plain way, and salt and water put in with it make a pan;
  - the bottle comes back, and a stick doesn't go in;
  - taken off when it smelled done, the pan gives a soft, salty stew, cooked through, named and filling, in a bowl, made by the cook;
  - the lift from eating it;
  - underdone chicken gives no lift;
  - a pan left on burns and the ring clicks off, nothing more goes in, and it comes off burnt.
- `KitchenTests#visitorsComeInSitDownAndAnswerWhatTheyAreGiven` checks:
  - who can come, the tenant only once a reader here has moved them out;
  - Holloway comes in and sits at the table;
  - a crisp, salty, well-done dish is what he loves, with his story the first time;
  - a stew is slop to him, and its bowl comes back;
  - a third meal is refused;
  - the book records the meals and can be read, and the host is remembered;
  - fed, he leaves by his door, and he isn't next;
  - dead, he doesn't come;
  - Johnny, struck, goes.
- `FamiliarRoomTests#theKitchenIsForUseAndNothingFrightensAnyoneInIt` still passes: four of one thing on the stove, done in ten seconds, never burnt.
- `tools/draw_kitchen_dishes.py --check`, `tools/paint_character_skins.py --check` and `tools/verify_character_art.py` pass.
