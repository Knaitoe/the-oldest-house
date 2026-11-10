# Borrasca: the comic in the window (0.4.445)

The first part of [BORRASCA_LIFE_PROPOSAL.md](BORRASCA_LIFE_PROPOSAL.md). The owner, 10 November:

> You should start dev with Kelly's fort window. The player already pick a window in her pretend house. Let her tuck a borrowed
> comic into that sill for you. Years later, when you find her in Room 6, she remembers which window you chose and asks if you ever
> finished it. That gives the two of you an ordinary thing to talk about before she asks you to carry her name back up.

Nothing here is required. The four visits, the ending and the Witness count are unchanged.

## In play

**The fort, first visit.** Kelly still says "Pick a window", and still answers west or east. The window the reader stood at is now
kept; a reader who stands at neither is given the west one. When her line about the window has had its time she walks across the
rug to that window with a comic in her hand, stands it in the sill against the glass, and says:

> Patrick lent me this. I'm only halfway. It stays in your window, so we both know where it is.

("this window" for a reader who chose none.) She walks back to her place and adds:

> Read it if I'm not here. Just don't tell me how it ends.

She does this whether or not the reader stays up there to watch, and a reader standing in her way only slows her. The noise from
the ridge waits until she is back.

**The comic** is *Kit & Caboose* No. 31, "The Light in the Crater". It stands on the ledge the wall leaves inside the window,
leaning on the glass, its cover to the room and its back cover to anyone looking in from outside. A hand on it takes it down. It is
the reader's own, like her compass: nobody else can pick it up, and there is only ever one.

Used from the hand it opens: a cover, six pages of story and a back cover, a page at a time. A click on the right of the page, the
mouse wheel, the arrow keys or the space bar turn it on; the left of the page or the other mouse button turns it back. It opens
again at the page it was closed on. The story stops on "CONTINUED IN No. 32! WHO KNOCKS FROM THE OTHER SIDE?" The back cover has a
subscription coupon somebody began to fill in with a pencil: PAT MADDOX, RT 2 BOX, and no more.

Having had all six pages of the story open, each for a moment, is having finished it. The game never says so.

**Patrick**, spoken to on the first visit by a reader carrying it, says once before anything else:

> That's my Kit & Caboose. Kelly's had it a week. If you get to the end before she does, don't tell her.

**Kendra**, on the second visit, with Patrick gone:

> That's Patrick's. He lends them out before he's read them himself. Keep it flat for him.

**Left in the sill**, it stays there through every later visit. Once the reader is grown the window has bleached it, and a comic
taken down then is the bleached one, in the hand and on every page.

**Room 6.** After her first words ("Don't touch the other doors. They count them in the morning.") the next thing Kelly says is:

> The west window. You picked it. I stood Patrick's comic in the sill for you. Did you ever finish it?

("You picked it" only when the reader did.) The reader's next word to her is their answer, and it is whatever is true:

| What the reader did | The reader | Kelly |
|---|---|---|
| Left it in the sill | "You tell her it is still in the sill." | "Then it's where I said it would be. Leave it. Somebody else will pick that window." |
| Took it, never finished it, does not have it here | "You tell her you took it down from the sill and never got to the last page." | "Then neither of us knows. Good. Don't find out for me." |
| Finished it, does not have it here | "You tell her where it stops: a knock from under the crater, and the rest in No. 32." | "A knock. I gave it a better one than that. I had the time." |
| Has it in their pack | "It is in your pack. You hold it out." | "You kept it flat." (Taken down grown: "It sat there the whole time. Look what the window did to it.") |

With the comic held out, the reader chooses: **Leave it with her**, or **Keep it**. Left, it goes from the reader's pack into her
hand, and she says "I was on the third page. I know the panel." Kept: "Then you finish it. One of us should.", or, from a reader
who has finished it, "Then you know where it stops. Don't say." Closing the choice keeps it.

Then her own lines go on as before, to the name.

**The account** keeps it. After the window: "She stood a comic of Patrick's in its sill for me. She had only got halfway through
it." After finding her: "She remembered which window was mine, and asked if I had ever finished the comic.", and then what was
true: "It was still in the sill." / "I never had." / "I told her where it stops." / "I had it with me. I kept it." / "I had it with
me. I left it with her, and she found her page."

## How it is made

- **State** (the reader's own record): `FortWindow` ("west" or "east"); `ComicStep` and `ComicTicks` (her walk); `ComicTucked`;
  `ComicBack`; `ComicTaken` and `ComicTakenVisit`; `ComicSeen` (the pages had open, as bits), `ComicAt` (the page it was closed
  on), `ComicFinished`; `ComicPatrick`, `ComicKendra`; `ComicAsked` (0, asked, answered), `ComicAnswer`, `ComicGiven`.
- **`BorrascaComic`** holds all of it. `tuck` is her walk: wait 60 ticks, to the turn on the rug, to the window, reach (gesture
  4), stand it and speak, wait 70, back by the turn, speak, done. No step waits past 100 ticks. `BorrascaStory.pact` runs it ahead
  of everything else in the fort while `FortWindow` is set and `ComicBack` is not, and then sets the fort's clock to
  `FORT_AFTER_COMIC` (260 of 320), so the ridge follows in three seconds.
- **The sill** is `BorrascaShopBlock.Kind.COMIC` in the cell inside the pane (authored -4,7,-96 facing east, or 4,7,-96 facing
  west), stage 0 bright and stage 1 bleached. Its model is drawn past its own block, on the seven-pixel ledge between the cell and
  the glass, leaning 22.5 degrees; its shape is there too, and it has no collision. `BorrascaComic.keep` sets or clears the block
  from the record on every story tick, so it survives every visit and is never there twice.
- **The item** is `borrasca_comic`, a `BorrascaKeepsakeItem` issued through `BorrascaBelongings` (token and owner in its custom
  data, one original). Taken on visit 3 or 4 it carries custom model data 1: the bleached item picture and the bleached pages.
- **The pages** are drawn by `tools/draw_borrasca_comic.py`: `textures/gui/borrasca_comic.png` and `borrasca_comic_aged.png`, each
  512 by 384, eight pages of 128 by 192 in four columns and two rows, with the tool's own four-by-five lettering. The sill's tile
  (`block/borrasca_shop/comic.png`, `comic_aged.png`) and its model are drawn by `tools/draw_borrasca_town_art.py`. Nothing is
  copied from any real comic; the title and everything in it are original.
- **The screen** is `client.BorrascaComicScreen`, opened by `BorrascaComicPayload(aged, page)`. It draws the page at a whole
  number of screen pixels to the pixel when the window allows. A page open for 15 ticks is sent once as
  `BorrascaComicPagePayload(page)`; `BorrascaComic.page` records it only for a reader who is carrying their own comic.
- Protocol 178 (two payloads). Layout unchanged. New builds only: a reader whose fort scene was played before this version has no
  `FortWindow`, so there is no comic and Kelly does not ask.

## Checks

- `BorrascaTests.aReaderWhoRunsToTheTreeFirstStillMakesThePactThere` now waits for her at the west window with the comic in the
  sill, its cover to the room, out of her hand, her words over her head and the ridge not yet heard; then for her back at her
  place; and the account's three sentences in order.
- `BorrascaTests.theComicIsKnownInTownAndAnsweredForInRoomSix`, two readers. One takes it down as a child (one comic, their own,
  the sill empty, no second one), has the pages open (five pages are not the end; a reader not carrying theirs records nothing),
  is known by it to Patrick once and to Kendra once, and years later leaves it in Kelly's hand. The other leaves it: it is in the
  west sill, bleached, on the last night; each of the four answers is given for the state it belongs to; taken down grown it is
  the bleached one; and her own lines then run to the end.
- The client proof's eighteenth view, `native-borrasca-comic.png`: the sill comic at both stages from the room and from outside,
  behind a pane on a plank wall; the item, bright and bleached; and all eight pages, bright and bleached, through the screen's own
  drawing.
