# The quiet rooms' notes, the cardboard house and a film-day shortcut — 0.4.172

Two sets of the owner's requests.

- **The Ghosts set** (from the backlog): "add a command to trigger specific film days for testing; improve Marjorie's crawl into and destruction of the cardboard box." The third part, condensing the house into a smaller, more lived-in one, is not in this version.
- **The quiet rooms**: the owner's October 29 playtest notes, one by one below.

## The quiet rooms

### Walking out through the doors

> "Door's behind the counter now. Players should still be able to walk through doors probably, before the vignette ending dialogue plays."

- **The bathroom.** The vanity ran across the doorway to the bedroom, so the room could only end on its timer. It now stops short of the door and turns the corner by the tub instead, keeping all eight places for the routine tools.
- **No timers.** The waiting room, bathroom and kitchen each ended a few seconds after their door opened, wherever the reader stood. Every quiet room now ends only as the reader walks through its open door, and the closing line comes then.
- **Clicking a door** still leaves, as before.
- **Older saves.** Rooms not yet entered, built with an older layout, are rebuilt (layout 172).

### Lines

- **The bathroom's closing line.** "You slept the way people sleep in a house that is listening" was flagged as an AI-ism. It is now: "The light went out. You found the bed in the dark and slept."
- **The coatroom's closing line.** "The glass will remember." is cut. It is now: "That's how you'll look here."
- **The wardrobe** ("Another ai-ism…", "The text should be describing the clothing rather than the player").
  - Each garment's first-wear line used to be a guess about the reader, such as "You keep a thing unfinished rather than throw it away."
  - It now describes the garment, for example the long cardigan: "Hand-knitted to the knee, changing yarn below the hip."
  - The pasts in the tooltips are unchanged.
- **The waiting room and shelter** close on "They called your name, and you went through." and "The song ended. The rain kept on, and you went home in it."
- **The last quiet room's title card** now plays off the house the reader has just been in:

  > Every house is a haunted house.
  >
  > But a warning, before you proceed:
  >
  > this house has ghosts in it.

### The afro

> "Let's make the fro a little less intense. Or add an option for a shorter fro."

The picked-out afro now sits closer to the head and is rounded at the crown. It is three pixels high rather than four and a half, and a pixel and a bit out at the sides rather than two. Each of the sixteen bathroom tools gives one style, so a shorter afro replaces the large one rather than joining it.

### Everyday clothes

> "please make sure to add a basic t-shirt and jeans, crop top and shorts, and a tank top and shorts for players wanting simple attire. The clothing should be able to be swapped out like the hair tools."

Three outfits hang on wooden hangers along a brass rail on the coatroom's back wall, west of the curtain:

| Outfit | What it looks like | Its past |
| --- | --- | --- |
| T-shirt and jeans | A plain cotton tee washed thin at the collar, and straight-leg denim worn through at one knee. | The shirt is from a concert nobody else remembers going to. The jeans were broken in by somebody taller. |
| Crop top and shorts | A cropped cotton top that stops short of the waist, and cut-off denim frayed at the hem. | Packed for a summer that was called off, and never unpacked. |
| Tank top and shorts | A ribbed tank top cut wide at the arms, and loose drawstring shorts to above the knee. | Worn for every move, carrying boxes up other people's stairs. |

- **Wearing one.** An outfit is worn in place of a coat, top and bottoms together. Taking another outfit or a coat swaps it, and whatever was worn goes back on its hanger or hook, just as the coats already did.
- **On the reader.** The top follows the body and arms and the bottoms each leg, so they move with the reader. The boots go over them.
- **The read.** Each outfit belongs to one of the three sets the House reads presentation from, like the coats.
- **The account.** It says the outfit came "off the rail in the hall".

### The shelter's two strangers

The owner called them the dancing NPCs:

> "Fix the posture of the dancing npcs? Pretty sure the sequence is broken too boot. It needs a decisive way for the player to end it. The npcs approaching the player – assuming that's the intention? – isn't very reliable. I looked at the boy for a fraction of the time I did the girl and he was halfway across while she continued to eke her way forward. The girl – at the risk of sounding sexist – should look more traditionally feminine for easier gender recognition."
>
> "Unless the intent is for the player to exit. If so, that should be presented more clearly."

**Posture.** The strangers lean back on the rail only while they are at the rail. Before, they kept the lean while walking across the floor. Now:

- off the rail, they stand and walk normally;
- close to the reader, they hold out a hand;
- when dancing, they put both hands up to the partner's shoulders and sway from foot to foot;
- after a dance, they step back.

**The approach.** A stranger now comes across the floor only while the reader is looking at them. The one not looked at stays leaning on their rail. Before, both walked in whenever the reader stood in the middle. They stop an arm's length away and hold out a hand: "The girl holds out a hand. Take it to dance."

**Ending it.** There are three decisive ways to end the song:

| The reader | The read | The account |
| --- | --- | --- |
| Uses a stranger (takes the hand) | That stranger, settled | "danced with the girl under the eaves" |
| Stands between both strangers once both hold out a hand | Both | "danced with the two strangers under the eaves" |
| Uses the lantern out on the dock | No one | as before |

- **A dance.** It lasts eight seconds. Then the song is let go and the partner lets go and steps back. Standing between both strangers for five seconds starts a dance with both of them.
- **A decisive act is the answer.** Whatever glances came before it, the read starts again from that act, settled.
- **Without a decisive act**, the song still ends after its 45 seconds and reads where the reader stayed. If the House has no answer by then, the song plays again, as before.

**Leaving.** When the room is settled:

- the jukebox clicks off;
- a lamp over the way on, dark until then, comes on;
- that door swings open;
- the reader is told: "The jukebox clicks off. The door under the lamp is open."

Before, nothing changed when the room had its answer.

**The girl.** She now wears a wine-red dress with a cream sash and a long skirt, with dark tights. Her long hair falls down her back and over her shoulders, with a gold clip. She has lashes at the outer corners of her eyes and a rose mouth.

Repainting her sheets (`tools/paint_house_read_cast.py`) showed that the half-smile pixel had been drawn off the face, in an unused corner of the sheet. Both strangers' half-smiles are now on their faces.

### The rope swing

> "The rope swing should be interactive. Players should be able to swing. 😊 Can you remove the sparkle effects too?"

- **Sitting.** Using the porch swing seats the reader in the middle of the plank, facing the house.
- **Swinging.** The swing builds up over a few seconds to a gentle swing of about thirty degrees along its arc. The seat and its ropes are drawn swinging with the reader, and the still ones are hidden.
- **Getting off.** The still swing comes back.
- **In sync.** Every position comes from the moment the reader sat down, so the client and the server agree.
- **Sparkles.** The glow particles drifting over the yard are gone.

### The room the reader can't enter

> "I haven't explored enough to know if this room being inaccessible was intentional on your part. I do think that teasing the player with its proximity while not allowing them to enter, builds up great anticipation and a sense of unease, though!"

It is intentional. It is one of the homes that bleed in later (0.4.170): the room stays sealed until the halls have been passed through enough times, then opens all at once. Nothing changes here.

## The Ghosts set

### A film-day shortcut

`/ghosthouse test|vignette ghosts_set day <1-7> [pickup]` (operators only) starts a chosen filming day for the reader in the set, as a fresh arrival on that day.

- **`day 4 pickup`** starts at the upstairs pickup, with both sisters already up the stairs.
- **Earlier days.** They are not filmed by it, so the account cannot be finished from a shortcut.
- **`ghosts_set status`** shows the day and the clock.

### Marjorie and the cardboard house

On day four's pickup:

1. Marjorie looks the box over: "That's my house. You won't fit, Marjorie." / "There's room. There's always room in here."
2. She crawls in through its doorway.
3. She thrashes until it bulges, with scrapes.
4. She splits it open, rising with her arms up, and sinks back down among the card.
5. Merry reaches for her, then shrinks back.

The playhouse is now hinged cardboard panels with a gabled roof, drawn by `tools/draw_ghosts_playhouse.py`:

- Merry's crayon on the outside: a sun, vines, windows, flowers, a heart over the door, the two sisters holding hands and red shingles;
- plain board with a box's printed arrows and tape inside.

It bulges as she thrashes, then tears open: the front first, then the sides and back fall flat, and the roof halves are thrown off. A phase already reached is drawn finished and never replayed.

## Run 1337

Run 1337 tested 5e69a1a. Fifteen of seventeen jobs passed.

- **The Holy Rabbit father.** A Holy Rabbit case used the father's saved id a tick before his body was in the level on a loaded runner. The talk call then threw an exception that stopped the whole native suite.
  - Both father cases now wait for the body itself.
  - The talk call ignores a missing actor.
- **The elk shove.** The elk shove case's shore site, far from the scene's base, was not entity-ticking yet. The case now holds the site first.
- **The boat attacks.** The boat-attack case waits for two swings rather than a fixed time. Each swing is still held to the saved delay.

## Tests

- **Quiet-room cases (40, one added).**
  - The waiting, bathroom and kitchen cases wait without the room ending, then walk through the open door.
  - The wardrobe case wears the t-shirt and jeans from the rail and checks the account's "off the rail" and that no line guesses at the reader.
  - The porch case swings, checks the arc and the return of the still swing, and checks that no glow particles are sent.
  - The shelter case: the boy holds out a hand without leaving his rail; looked at, the girl comes across while the boy stays; taking her hand is a dance that settles the read, lets the song go, lights the lamp and opens the door; she steps back; the account says so.
  - The new case: the dock lantern and the dance with both.
- **The Ghosts case** covers the shortcut and the box in native time.
- **Client proofs.**
  - The wardrobe proof shows all twenty-one garments hanging and the twelve coats and three outfits worn.
  - The shelter cast page shows the strangers on the rail, holding out a hand and dancing.
  - The hair page shows the new afro.

## Versions

- **Protocol 97.** The outfits add three fitting states and three items; the seat entity syncs its swing.
- **Layout.** No labyrinth layout or Witness change: 54 / 41 / two kinds / three endings.
- **Rebuilds.** Quiet rooms not yet entered are rebuilt (layout 172).
