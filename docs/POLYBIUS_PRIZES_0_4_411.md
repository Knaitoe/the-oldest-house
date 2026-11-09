# The prizes (0.4.411)

The owner, on the arcade's prizes (docs/POLYBIUS_ARCADE_0_4_221.md): "add animations/interactions for the arcade prize items? Massive disappointment that I couldn't snap the bracelet onto my wrist. I'm assuming other items are similarly missing interactions."

They were: until now all eight prizes were plain items. Each prize now has a use and an animation of its own.

## What each prize does

| Prize | Use | How it looks |
|---|---|---|
| **Slap Bracelet** | Hold use: the bracelet is held up straight, then slapped round the wrist of the hand holding it. | The band straightens in the hand, is raised and comes down with a slap. It then shows on that wrist for every viewer, in mirrors and on the reader's own arm in first person. |
| **Mood Ring** | Hold use: the ring slips onto a finger of the hand holding it. | A gold band with a stone on that hand. The stone changes with its wearer (below). |
| **Kazoo** | Hold use: the reader plays it. | Held at the mouth like a horn. It plays a note every fifth of a second; level is the middle of a two-octave scale, looking up plays higher and looking down lower. A reader whose mouth the machine in the Kept took gets no sound from it ("No sound comes."). |
| **Finger Trap** | Hold use: both hands go into it. | Both arms come forward. Each second of pulling tightens it and stretches the weave, up to three times; it creaks as it goes. Crouching pushes the fingers together and it loosens; when it is loose, they come free with a pop. Letting go while it is tight gets a line saying how. |
| **Bouncy Ball** | Use: throw it. | It bounces off every face and anyone it hits, losing a little each time, with a boing that falls with its speed. When it has almost nothing left it lies where it stopped, as the ball, for anyone to pick up. It hurts nobody. |
| **Glow Star** | Use on any face of a block: it sticks there. | Flat on the wall, floor or ceiling, drawn at full brightness, so it glows in the dark. |
| **Rubber Spider** | Use on a floor or a table: it is set down. | It sits flat. When anyone comes within arm's reach, it jumps and squeaks, at most once in five seconds. |
| **Plush Dog with One Eye** | Use on a floor, a table or a bed: it is sat down. Use anywhere else: hug it. | Sat upright, facing the reader who set it down. Hugged, it is held close in both arms and squeaks softly now and then. Three seconds of a hug halve whatever lingering static the reader has from one of the worst rooms; nothing says so. |

The set-down prizes (star, spider, dog) come back to whoever uses a hand on them, or drop as themselves when hit or when what holds them is taken away. They are entities, not blocks, so the House's rule against building does not stop them and no room is changed by them.

## Wearing and taking off

- A bracelet or ring goes on the arm of the hand that held it. Each wrist takes one bracelet and each hand one ring.
- Crouching and using an empty hand takes one off into that hand: a ring before a bracelet, the main arm's before the other's.
- Worn prizes are saved with the world (`prize_wear_0411`) and stay on through death, doors and logins, like the machine's loss of a mouth.

## The mood ring's stone

| Stone | When |
|---|---|
| Violet | Whole (80% health or more), somewhere other than the House |
| Blue | Whole, inside the House |
| Green | A little hurt (40 to 80%) |
| Amber | Badly hurt (under 40%) |
| Black | Being hurt right now, or on no finger at all |

The ring's picture in the inventory and in the hand shows the same colour for whoever holds it.

## Made

- **Code:**
  - `PolybiusPrize` is the item: one class with a kind for each prize, and its uses, sounds and animation timing.
  - `PrizeWear` keeps what each player wears, and `PrizeWearPayload` and `PrizeTakeOffPayload` carry it.
  - `PrizeProp` is a set-down spider, dog or star, and `PrizeBall` the ball in flight.
  - `PrizeClient` holds the client side:
    - the worn layer on every player and the first-person arm;
    - the prop renderer;
    - the item properties (the bracelet straight, the trap's stretch, the ring's mood);
    - the two-handed and slap animations;
    - the take-off input.
  - `Grounding.ease` halves the lingering static for the dog.
- **Art:** `tools/draw_polybius_art.py` now also draws:
  - the straight bracelet;
  - three stretches of the trap;
  - the ring's four lit stones, with the base ring's stone made black;
  - the worn texture (`textures/entity/prize_wear.png`).

  The three items' models carry overrides.
- **Sounds:** `tools/generate_prize_audio.py` synthesizes six subtitled sounds: the kazoo's note, the boing, a rubber squeak, the slap, the trap's creak and the pop.
- **Protocol:** 168.
- New builds only.

## Tests

One new case in `PolybiusTests` (literary namespace, its own batch):

- the bracelet snaps round the right wrist, and a wrist takes only one;
- the ring goes on, and its stone reads blue, then amber, then black, and black off a finger;
- crouching with an empty hand takes the ring off, then the bracelet;
- the kazoo plays the middle, top and bottom of its scale;
- the trap tightens and lets go;
- a hug halves the static;
- the star sticks to a wall and the spider sits on the floor;
- the spider jumps when someone comes near;
- the ball bounces more than once and lies as the ball;
- the star and the spider come back to hand.

## What stays

- **Witness:** 85 sources / 64 resolutions / two kinds / three endings.
- **Layout:** 134.
