# The reader's journal — design (October 30)

The owner wants players to keep journals and writings of their own: "to not just draft their own house of leaves, but to create their own wall of mad writings like zampano's." They proposed five ideas and dropped the first. On the other four, the owner's answer was: "I think the journal might be worth developing as a custom sub system. I especially like the idea of a damaged book returning on death. The custom sub system fixes worrying about signing the book too."

This document is the plan for that subsystem. Step 1, the journal and its page renderer, is built in 0.4.177 ([JOURNAL_0_4_177.md](JOURNAL_0_4_177.md)). Step 2, Zampanò's courtyard on the same renderer, is built in 0.4.178 ([ZAMPANO_DRAWERS_0_4_178.md](ZAMPANO_DRAWERS_0_4_178.md)). Step 3, pencil rubbings, is built in 0.4.179 ([JOURNAL_RUBBINGS_0_4_179.md](JOURNAL_RUBBINGS_0_4_179.md)).

## Agreed

- **Its own item and screen, not a vanilla book.** A writable book holds plain text only, and signing it ends the writing. The journal needs drawings, a second hand in the margins, damage and torn pages, so it has its own item, data and screen. Nothing is ever signed.
- **The journal stays the reader's own.** It is exempt from the Mother's custody. On a death it comes back damaged.
- **Rubbings only on authored objects.** They give no rewards.
- **The co-author only adds.** It is tied to real events, never destroys the reader's words, and writes only on pages written in the House.
- **The static fades by itself.** It also respects the screen-effects setting. Writing only speeds it up, and the House never says so.
- **Co-op pages are limited.** A reader can always take back their own.

## Defaults for the two open questions

The first is easy to change; the owner has answered the second.

1. **Where the journal comes from.** It arrives with Will's letter, as a second parcel on the doorstep, addressed in the reader's own hand. The reader does not remember sending it. Inside the cover is their name, in the same hand. It is blank.
   - It comes before the House, so the reader can write from the start.
   - It follows the self-addressed envelope ("Addressed in your own hand").
   - A reader who joins after the opening finds it on the doorstep the first time they reach the manor.
2. **Whether the House may quote the reader's lines.** The owner: "yes, the house should quote the player in some carefully chosen instances." See [Where the House quotes the reader](#where-the-house-quotes-the-reader).

## The journal

### The item

- **One journal per reader,** bound to its owner like the envelope.
- **Only the owner opens it.** To anyone else it is a closed notebook with a name inside the cover. The only sharing is deliberate: a torn page.
- **The item is a handle; the words live in the world's saved data.** The stack carries only its owner, its issue number and its damage stage. Three reasons:
  - large text in item data goes out to every client in inventory packets;
  - a stack can be copied in creative;
  - losing the item must never lose the words.
- **A lost journal comes back.** If it is destroyed, despawns or falls out of the world, it waits for its owner on their next arrival in the House, one damage stage worse. As with the paw, the old stack goes inert when the new one is issued.

### Pages

- **Size.** 96 pages, shown two at a time as a spread.
- **Each page holds:**
  - **the reader's text:** up to 14 lines, edited in place;
  - **marks**, each with a position on the page: rubbings, margin writing and stains;
  - **where it was begun:** the House day and the place's prose name (`StaircaseProse.place`), written faintly at its foot.
- **What the record is for.** The co-author writes only on pages begun in the House. The post-mortem knows which pages a death soaked.
- **Text is never removed by anything but the reader.** A crossing-out is a mark drawn over a word. If the reader deletes the word, its crossing-out goes with it.

### The screen

- **The spread.** Graphite text on paper, with a caret and line editing. Turning pages has a sound. Writing makes a soft graphite-on-paper sound that follows the typing.
- **The reader's text** is in the vanilla font in graphite grey. A pencil hand, drawn in code like the other hands, can come later.
- **Margin writing** uses its own hand, distinct from every existing font and drawn in code.
- **Rubbings** are small charcoal textures laid on the page. Typing flows around them.
- **Damage** shows on the cover and the paper, in four stages:
  1. water rings;
  2. dog-eared corners;
  3. swollen, rippled pages;
  4. a torn corner and a tide line across every page.

  Text stays legible at every stage.
- **No instructions on screen.** There is only the book.

### One page renderer: the journal and Zampanò's courtyard

The owner's follow-up: "You might be able to use the same journal system to help in zampano's courtyard. It should allow for the sort of spiraling and cascading text featured in the actual house of leaves book. The ergodic style?"

The journal's page is built as a general page renderer, not a book of plain lines. A page is a list of placed pieces:

- **Flowing text:** the reader's ordinary writing, wrapped in lines.
- **Placed text runs.** Each run has a font, a position, a scale, a colour, struck or not, and one of these shapes:
  - **a line at any angle**, including upside down;
  - **mirrored**, readable only in a mirror;
  - **a spiral** winding in or out from a point;
  - **a cascade**: one word or a few letters to a line, falling down the page as on the book's stair pages;
  - **columns** running side by side, or one column running sideways up the margin;
  - **a boxed window** of text inside the page, as in the labyrinth chapter.
- **Images:** rubbings and stains.
- **Marks:** crossings-out and margin hands.

The word "house" keeps the mod's existing blue (`HouseText.INK_BLUE`), as it already does in books. "Minotaur" is red here, as in every book since 0.4.175: these pages raise `HouseText`'s literature count while they draw.

**Two uses of the one renderer:**

1. **Zampanò's courtyard.** The filing drawers open onto the same screen showing authored pages in these shapes:
   - the drawers are textured and can be opened;
   - pages are pulled from them as from the book;
   - a page's text spirals, cascades, runs in columns and boxes, or turns upside down, so the reader turns the page, tilts it or reads it backwards.

   The text is written for the courtyard in Zampanò's manner, alongside the novel finds the mod already ships. It quotes nothing new from the book.
2. **The reader's own pages.** After typing a line, the reader can:
   - drag it into the margin;
   - turn it at an angle or upside down;
   - set it as a spiral or a cascade;
   - mirror it.

   That is the owner's "own wall of mad writings": a page can become as tangled as Zampanò's. Placed lines stay the reader's own text. The co-author still only adds.

### Network

| Payload | Direction | Carries |
|---|---|---|
| `JournalOpenPayload` | S2C | The whole journal on open: text, marks, headers, damage stage. 96 short pages fit in one payload. |
| `JournalEditPayload` | C2S | A page index and its new text. |
| `JournalTearPayload` | C2S | A page index. |

The server checks each edit:
- the length and the line count;
- a rate limit, 10 ticks per page as with the phone;
- that the editor is the owner and holds the journal;
- the server's text filter, as for signs and books.

## Rubbings and margin scraps

The owner's idea 2.

- **What makes a rubbing.** The reader holds the journal and right-clicks an authored object. The rubbable objects are a registry of block kinds and actors: a carved rail, a strange mark, a plate, a dead phone, and so on. Each gives a charcoal rubbing, a one-line observation, or both, on the open page or the next free one, with room to write beside it.
- **Every other right-click opens the journal.** That includes the air and any block that is not rubbable.
- **Once per object per reader.** A second try only turns to that page.
- **What it is not.** It is not a reward, a Witness step or an advancement, and it never changes a scene.
- **The art.** The first rubbings are drawn by `tools/draw_journal_art.py`, about twelve, from objects already in the rooms. More follow as rooms are reviewed.
- **The observation lines** follow the House's writing rules: they never gender the reader and never name a colour or an age.

## The uninvited co-author

The owner's idea 3.

- **Its triggers:**
  - waking from sleep;
  - the caver's blackout and other blackouts;
  - the end of a high-dread vignette (a listed set).
- **How often.** A small chance each time: one in six, at most once per House day per reader.
- **Where it writes.** Only on pages begun in the House. It never writes on a page the reader has open.
- **What it writes,** using the owner's three forms:
  - **a blank page:** "Nothing to say? I was right behind you the whole time.";
  - **a crossed-out name:** a thick line through a character's name the reader wrote. Names are matched against the cast the reader has met. The word stays underneath.
  - **a direction:** under a line with "left" or "right" in it: "No. Right." or "No. Left.", whichever contradicts the line.
- **What the reader can do.** The margin writing is ink. The reader cannot erase it, only tear out its page.

## Writing as grounding

The owner's idea 4.

- **The static.** After a listed intense vignette ends, the reader carries a lingering static:
  - a faint tinnitus tone;
  - a slight camera shake;
  - a dark periphery.

  All of it is scaled by Minecraft's screen-effects setting, down to none.
- **It never blocks play.** On its own it fades over about five occupied minutes.
- **Writing settles it.** One full line of at least 24 characters, written in an ordinary hallway, fades it within three seconds under the graphite sound. An ordinary hallway means no vignette, no hazard and no hunter near.
- **Nothing explains this.** There is no bar and no hint. The sound and the quiet that follows are the whole cue.

## The post-mortem artifact

The owner's idea 5.

### Solo

The journal is exempt from the Mother's custody.

- **On a death in an ordinary hall,** it stays where the reader fell, lying open, and does not despawn.
- **On a death in a vignette** (rooms that reset, or a private copy), it waits three arrivals later: in the third hall the reader enters after respawning, it lies by the door.
- **Damage.** Either way it is one stage worse: water-damaged and dog-eared, as the owner described. The pages written since the last death take the worst of it.
- **Picking it up** gives it back. Nothing in it is lost.
- **Outside the House** it drops like anything else, and the loss rule above still holds.
- **With keepInventory** it stays in the reader's hands and takes no damage.

### Co-op, and the reader's own wall

- **Tearing.** From the screen, a reader can tear out a page that has writing on it. The page becomes a torn page item, "Torn from <name>'s journal". The journal keeps the stub.
- **Pinning.** A torn page pins to a wall or a table as a page that anyone can read:
  - at a glance, a few tiny lines show;
  - right-click opens it in full;
  - dropped in a shared hallway, it stays where it fell.
- **The reader's own wall.** In the manor there is no limit on pinned pages, so a reader can build their own Zampanò wall of pages.
- **Limits in the House:**
  - six pinned or dropped pages per reader at a time;
  - a seventh returns the oldest to its writer's journal.
- **Taking a page back.** The writer can take back their own page at any time, and it goes back into its stub.
- **Moderation.** Torn pages pass the server's text filter when torn. The server can clear a reader's pages with one command.

## Where the House quotes the reader

The owner: "yes, the house should quote the player in some carefully chosen instances."

The House quotes a reader's own words rarely, exactly, and only to that reader. Three moments:

1. **In the reader's own House of Leaves.** When a chapter tells of a place where the reader wrote something, it may quote one line written there.
   - For example: *In the hall with the lamp you wrote: "it is longer than it was."*
   - At most one quote a chapter.
2. **The co-author, once.** On a later blank page, the other hand copies out one of the reader's own lines, word for word, in its hand, and adds nothing.
3. **At an ending.** The last page the House writes quotes the first line the reader ever wrote in the House.

**What is quoted:**
- The words are the reader's own, unaltered, from pages begun in the House.
- A line is at least a few words long and has passed the text filter.
- The House never quotes:
  - a line that names another player;
  - a line that has been torn out;
  - a line the reader has since deleted.

**Who sees it:**
- Only the reader. A quote is never put on a pinned page or in any account another reader can read.
- A reader who has opted out of the House's read is never quoted. The read itself still never looks at the journal.

## Not in scope

- The journal never counts toward anything: no Witness, no advancement, no reward, no ending.
- The House's read never reads the journal.
- No other reader ever sees a journal's text except through a torn page.

## Build order

Each step is its own version, with its own tests and proofs.

1. **The journal itself and the page renderer.**
   - Item, saved data, screen, payloads, the doorstep parcel and the loss-and-return rule.
   - The renderer with every shape: lines at any angle, mirrored, spiral, cascade, columns, boxed window.
   - The reader's own placed lines.

   Tests:
   - owner binding and inert old issues;
   - edit validation (length, rate, owner, filter, placed-line bounds);
   - saving and reloading;
   - a destroyed journal coming back.

   A client proof renders the screen: empty, written, each shape, and every damage stage.
2. **Zampanò's courtyard on the same renderer.** Textured filing drawers that open, pages pulled from them, and authored ergodic pages in every shape. Tests:
   - the drawers open and close;
   - each reader's pulled pages are their own;
   - nothing is taken from the courtyard's finite originals.

   A client proof renders each courtyard page.
3. **Rubbings.** The registry, the first art, once per object per reader. Tests cover each listed object and repeat tries.
4. **The co-author.** Tests:
   - each trigger;
   - the once-a-day cap;
   - only in-House pages;
   - never a page that is open;
   - text never removed;
   - a crossed-out word vanishing with the word.
5. **Torn pages and pinning.** Tests:
   - the limits and the oldest coming back;
   - taking back;
   - the filter;
   - the manor wall having no limit.
6. **Grounding.** Tests:
   - the static on a listed ending;
   - the static fading by itself;
   - a full line in an ordinary hall fading it;
   - a line in a vignette not fading it;
   - the screen-effects scale at zero drawing nothing.
7. **The post-mortem journal.** Tests:
   - exemption from custody;
   - lying where the reader fell in a hall;
   - three arrivals later from a vignette;
   - damage stages;
   - keepInventory;
   - nothing lost.
8. **The House quoting the reader** at the three moments above. Tests:
   - each moment;
   - only in-House, unaltered lines that passed the filter;
   - never a deleted or torn line, or one naming another player;
   - never for a reader who opted out of the read;
   - never seen by another reader.

Each step adds payloads or items, so each bumps the protocol. The journal adds no Witness source: 55 sources / 42 required / two kinds / three endings stay.
