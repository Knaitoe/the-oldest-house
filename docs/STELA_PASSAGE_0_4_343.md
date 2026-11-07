# The stone against loose fill (0.4.343)

From the owner's draft "The Sphinx" (4 November), "A Stone From Between the Paws":

> *"Press it against loose fill covering a buried passage. Sand, gravel, or authored rubble draws aside long enough to crawl into the open space beyond. A shallow test can clear the mouth of a buried entrance. A deeper test can get you through a collapsed gravel run to the ledge you already know is there. The loose material settles after you pass."*

The stone is the piece of the Dream Stela a reader takes from inside their own door after the dream (0.4.342). This release gives it its use.

## Pressing it

The reader holds their stone and uses it on loose fill:

- **What counts as fill:** sand, red sand, gravel, and the dream's drifted sand.
- **Which way it looks:** straight in, the way the reader faces, along the height of the block pressed.

It looks for open space a body can crawl into: nothing to collide with, and no water or lava. Three things can happen:

- **It finds open space within its reach.** The fill between is held aside as a passage one block high.
- **The open space is farther in than it reaches.** The near edge shifts and settles, and nothing moves: *"The near edge shifts and settles. Whatever is behind it is farther in than the stone reaches."*
- **Something harder lies behind the fill.** It gives a little and stops: *"The fill gives a little and stops against something harder."*

Reach is the record the dream fixed: three, five or eight blocks of fill.

## The passage

The held fill becomes **parted fill** (`StelaFillBlock`):

- It is drawn in the fill's own texture, so a gravel run stays gravel.
- It is neither air nor replaceable, so the loose fill above it stays up instead of coming down into the passage.
- Nobody can break it or push it, and it drops nothing.

A reader inside the passage crawls, and so does one crouching at either end and facing in. Out in the open again, they stand. The crawl uses the same lease as the yard's culvert, now allowed in any world.

The passage is everyone's while it is open.

## How it opens

As the reader's dream went:

| Manner | Opening |
|---|---|
| **Patient** | The fill parts quietly and holds longest: thirty seconds and two more for each block. There is time to look through, turn back, or bring someone else through. |
| **Dismissive** | The stone must be held against the fill. After a second a narrow crack opens. It is solid to a body but clear to the eye down the passage's length, so the reader sees what is beyond before committing. Held three and a half seconds, it opens; let go before then, and the crack closes. It holds twenty seconds and a second and a half for each block. |
| **Hostile** | The fill opens at once with a hard crack. The crack is heard by anything nearby: hostile creatures within 24 blocks come toward the mouth, and any that can see the reader turns on them. It holds least time: ten seconds and a second for each block, never less than the warning plus three seconds. |

## Closing

Every passage sheds grit at both ends before it closes, with a sound: soft for a patient stone, loud for a hostile one. The warning comes early enough to crawl the passage's whole length, three quarters of a second a block and two seconds more.

- **It never closes on anyone inside.** While a living body is in the passage, the closing waits and the grit goes on falling.
- **The exact fill goes back.** Each block's original state was saved when it was held aside. Only blocks still held aside are restored.
- **One passage to a stone at a time.** Pressed again while it holds one open, the stone parts nothing else.
- **Only its owner can press it.**
- **It does not work in the House.**
- **It respects protected ground.** It parts only fill the reader could break where they stand: spawn protection, game mode, and any mod's protection all count.

Open passages are saved with the world. A passage open across a restart settles as it should once its blocks are loaded, and a dismissive crack whose reader has gone closes again.

## Assets

`tools/draw_sphinx_dream.py` writes the parted fill's blockstate and its twelve models. There is one model for each of the four fills in each of three phases:

- **Crack:** a slit four pixels wide and twelve high down the passage's length.
- **Open:** a skin of loose fill on the passage's floor.
- **Shedding:** the open floor, with grit hanging from the roof of the passage.

The models use vanilla's own sand, red sand and gravel textures and the dream's drift. No new sounds; no protocol change.

## Tests

`StelaPassageTests#theStoneHoldsLooseFillAsideLongEnoughToCrawlThrough` (exploration) runs four readers at buried passages:

- **The patient stone:** it parts four blocks of sand and gravel, and the sand over them holds. It holds the patient length. The reader crawls in. Brought due while they are inside, the passage waits, shedding. Once they are out it settles, every block back as it was, and the reader stands.
- **Refusals:** the stone parts nothing else while it holds a passage; nothing parts where the pocket is farther in than it reaches, or where stone lies behind; and a stone pressed by someone other than its owner moves nothing.
- **The hostile stone:** it opens at once, holds less time than the patient one, and brings a nearby husk toward it.
- **The dismissive stone:** it opens nothing until held. Held, a crack comes first, solid to a body; let go, it closes; held on, it opens.
- **Saving:** open passages are saved and read back the same.
