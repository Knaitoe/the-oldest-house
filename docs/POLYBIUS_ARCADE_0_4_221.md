# Polybius: tickets, prizes, the regulars and Block Party (0.4.221)

The owner, 1 November, on the arcade (docs/POLYBIUS_0_4_217.md): "Add tickets/ticket exchange. Vary textures on vendor stand.
Add npcs playing unused games. Add a brawler that supports 4 players. Using Minecraft characters would be cool."

The black cabinet, the men, the three plays and the walk, and the account are unchanged. Everything here is around them.

## Tickets

Every game pays out in prize tickets (`polybius_tickets`, an item drawn as a folded strip):
- **The three working cabinets:** a game the server believes pays one ticket, and one more for every hundred points, up to thirty
  (`Polybius.tickets`). A game that never scored pays nothing.
- **Block Party:** each fighter is paid when the game ends or when they leave it: one ticket, and one more for every 250 points,
  up to forty. A fighter who knocked nothing down is paid nothing.
- The black cabinet pays nothing.

## The prize counter

The shelf behind the counter is eight cells, four wide and two high, and each holds one prize, two rows of it, with its price on a
card (the shelf block's stage). Pointing at a cell and using it buys that prize, if the reader has the tickets: the tickets are
counted out and the prize is theirs. Without enough, they're told the price and what they have.

| Prize | Tickets |
|---|---|
| Rubber Spider | 5 |
| Finger Trap | 5 |
| Bouncy Ball | 10 |
| Glow Star | 10 |
| Slap Bracelet | 15 |
| Kazoo | 20 |
| Mood Ring | 25 |
| Plush Dog with One Eye | 40 |

Each prize is its own item with its own picture. The counter itself says where the prices are.

## The regulars

Three of the cabinets that were taped up are being played. Three regulars, the same three for everyone (painted like the rest of
the cast, stranger phases 47 to 49: a denim jacket, a red tee, a knit sweater and glasses), stand at their own cabinets with their
hands on the stick and the buttons, working them. Their cabinets run their own games' attract screens (a lighthouse's beam over
the sea, a dragonfly over lily pads, crates falling into a cart). A reader can't play those cabinets: the regular is on it, and
says so without looking up. Speaking to a regular gets one of their lines; none of them will play the black cabinet.

Two cabinets are still taped up.

## Block Party

A four-player cabinet, two blocks wide on the west wall, with four sticks. Up to four readers stand at it at once. Each plays as
their own Minecraft skin, down one street at night, against zombies, skeletons and creepers drawn from the game's own pictures of
them:
- **Joining:** using the cabinet takes a free stick. The first to join opens five seconds for the others (it starts at once if all
  four are taken). Anyone may drop into an empty seat while it runs. A fifth reader is told all four sticks are taken.
- **Playing:** the movement keys walk along the sidewalk, and the space bar punches. Zombies walk up and hit; skeletons keep their
  distance and shoot; creepers come close and, if nobody knocks them back, go off and hurt everyone near. Waves come from the right,
  now and then from the left, and grow with the wave and with the number of players. Two punches knock a zombie down.
- **Lives:** twenty health and three lives each; a fighter who loses a life comes back after two seconds, tinted for a moment while nothing can
  hurt them.
- **The end:** when every seated fighter is out of lives, it is GAME OVER on every screen; each is paid in tickets, and the cabinet
  goes back to its attract mode.
- **Leaving:** closing the screen, or walking more than five blocks from the cabinet, gives up the seat (and pays what it earned).
  A reader at Block Party cannot play another cabinet at the same time, and the other way round.

The game runs on the server (`PolybiusBrawl`), twenty steps a second, one game per arcade. Each player's screen
(`client/PolybiusBrawlScreen`) sends only which keys are held, and draws what the server sends back each step: each fighter as a
paper doll from their own skin (face, shirt, sleeves, trousers, the hat layer), the zombies and skeletons from their own
textures, the creepers from theirs. Names over the fighters, a bar along the top with each seat's name, score, health and lives.
Nothing flashes: a hurt fighter is tinted red while it lasts instead of blinking, and a lit creeper whitens steadily as its fuse
burns down.

## Not changed

The black cabinet, the men, the walk and the account; the three games and their tables; the Witness pool. Protocol 117 (two new
payloads). New builds only.

## Tests

Two new cases in `PolybiusTests` (literary namespace):
- **Block Party:** the cabinet stands two wide; four readers take the four sticks and a fifth is turned away; a reader at it cannot
  play another cabinet; held keys move the fighter on the server; two punches knock a zombie down and score it; closing the screen
  gives up the seat and the fifth reader drops in; when everyone is out the seats empty, the one who scored is paid in tickets and
  the others are not.
- **Tickets and prizes:** three unowned regulars stand at their own cabinets, each of which is one they play; each shelf cell holds
  its own prize; the ticket rule; twelve tickets don't buy the plush dog and ten of them buy the bouncy ball.

The native character review (`character_art.json` and `tools/character_art_catalog.py`) lists the three regulars, and Brad from
0.4.220, whose absence failed that release's quick gate.
