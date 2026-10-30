# The reader's journal — design (October 30)

The owner wants players to keep journals and writings of their own: "to not just draft their own house of leaves, but to create their own wall of mad writings like zampano's." They proposed five ideas and dropped the first. On the other four, the owner's answer was: "I think the journal might be worth developing as a custom sub system. I especially like the idea of a damaged book returning on death. The custom sub system fixes worrying about signing the book too."

This document is the plan for that subsystem. Nothing here is built yet.

## Agreed

- **Its own item and screen, not a vanilla book.** A writable book holds plain text only, and signing it ends the writing. The journal needs drawings, a second hand in the margins, damage and torn pages, so it has its own item, data and screen. Nothing is ever signed.
- **The journal stays the reader's own.** It is exempt from the Mother's custody. On a death it comes back damaged.
- **Rubbings only on authored objects.** They give no rewards.
- **The co-author only adds.** It is tied to real events, never destroys the reader's words, and writes only on pages written in the House.
- **The static fades by itself.** It also respects the screen-effects setting. Writing only speeds it up, and the House never says so.
- **Co-op pages are limited.** A reader can always take back their own.

## Defaults for the two open questions

Both are easy to change.

1. **Where the journal comes from.** It arrives with Will's letter, as a second parcel on the doorstep, addressed in the reader's own hand. The reader does not remember sending it. Inside the cover is their name, in the same hand. It is blank.
   - It comes before the House, so the reader can write from the start.
   - It follows the self-addressed envelope ("Addressed in your own hand").
   - A reader who joins after the opening finds it on the doorstep the first time they reach the manor.
2. **Whether the House may quote the reader's lines.** Not by default. When the owner turns it on, the reader's own House of Leaves may quote one line they wrote in the House:
   - in their own book only;
   - never for a reader who has opted out of the House's read;
   - never in anything another reader sees.

   It comes last in the build order, so it can wait for the owner.

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

## Not in scope

- The journal never counts toward anything: no Witness, no advancement, no reward, no ending.
- The House's read never reads the journal.
- No other reader ever sees a journal's text except through a torn page.

## Build order

Each step is its own version, with its own tests and proofs.

1. **The journal itself.** Item, saved data, screen, payloads, the doorstep parcel and the loss-and-return rule. Tests:
   - owner binding and inert old issues;
   - edit validation (length, rate, owner, filter);
   - saving and reloading;
   - a destroyed journal coming back.

   A client proof renders the screen: empty, written, and every damage stage.
2. **Rubbings.** The registry, the first art, once per object per reader. Tests cover each listed object and repeat tries.
3. **The co-author.** Tests:
   - each trigger;
   - the once-a-day cap;
   - only in-House pages;
   - never a page that is open;
   - text never removed;
   - a crossed-out word vanishing with the word.
4. **Torn pages and pinning.** Tests:
   - the limits and the oldest coming back;
   - taking back;
   - the filter;
   - the manor wall having no limit.
5. **Grounding.** Tests:
   - the static on a listed ending;
   - the static fading by itself;
   - a full line in an ordinary hall fading it;
   - a line in a vignette not fading it;
   - the screen-effects scale at zero drawing nothing.
6. **The post-mortem journal.** Tests:
   - exemption from custody;
   - lying where the reader fell in a hall;
   - three arrivals later from a vignette;
   - damage stages;
   - keepInventory;
   - nothing lost.
7. **Quoting in the House of Leaves**, only if the owner agrees.

Each step adds payloads or items, so each bumps the protocol. The journal adds no Witness source: 55 sources / 42 required / two kinds / three endings stay.
