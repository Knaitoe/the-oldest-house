# The Ones Already Inside 8: the key (0.4.608)

The plan's eighth slice (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): "N7: the key and Rae's four turns." The House still does not deal the
house.

New builds only. No layout, protocol or Witness change. One new item (`lodging_key`, "Long Key", drawn by `tools/draw_lodging_key.py`)
and one new frame state (Daniel's straps hanging open, drawn by `tools/draw_lodging_house.py`). The logic is `LodgingKey`.

## Where the key is

On its hook in the workroom until Rae goes up "to find Vera". Out of sight there (the chapter's FETCHING), she takes it: the hook is empty
from that tick, and its grey line says so. From then the house keeps where it is: with Rae, in the cupboard under the stair, with Vera, in
one reader's hand, or used.

## Seen

In Act II, while Rae stands in her place in the hall and no reader is looking at her, she takes the key out of her coat and looks at it
(about three seconds, every twenty). A reader who looks at her then, near and with nothing between, sees it:

> Rae has a key in her hand, long and plain. She looks at it, and puts it away in her coat.

and she puts it away. Each reader sees it, or does not, for themselves.

## Asked

A hand on Rae in Act II, while she has the key, from a reader who saw it or who heard her ask in the hall (and so watched her go straight
up the stair to the workroom), gets "Rae looks at you, and then at her hands." and a choice: "You have the key." (if they saw it), "You
knew the way up." (if they heard her ask), or "Say nothing." Either question gets the script's confession:

> Rae: I knew. All right? I knew he was alive.
> Rae: If I'd told Dan he'd never have come. He'd have sat in the flat and come apart. I told myself she could save them both.
> Rae: She can't. She told me that upstairs. One of them.

Then her four turns, the plan's table:

| The reader | Rae | What follows |
| --- | --- | --- |
| "Keep it. I'll stay with Dan." | "Then I'll take it up to her." She walks up the stair to Vera in the corridor and gives it to her. | The key is Vera's. What she does with it is N8's. |
| "Give it to me." | "Don't let her see it." She puts it in the reader's hand. | The reader may tell Dan, or use it. |
| "Give it to him yourself." | "All right." She walks to Daniel's room, to the frame. "I'm sorry." Daniel: "Don't." She unlocks the straps. | He walks to the parlor. She stays at the frame. |
| "Say nothing." | Nothing. | She may hide it (below). |

If another reader's turn got there first, the key has gone where they sent it and the panel closes.

## Hidden

Seen and not asked about for half a minute, Rae walks to the cupboard under the stair, opens it, puts the key in, shuts it, and walks back
to her place. A reader who opens that cupboard finds it ("On the floor of the cupboard, pushed to the back: a key, long and plain.") and
has it.

## Used

A reader with the key in either hand, in Daniel's room, puts it to the straps (a hand on the frame's jambs or back, or on him):

> The key turns hard in the first lock and easier in the second. The straps fall open.

The key is spent. The frame's back shows the straps hanging from their iron loops, buckles at the bottom, the plaster pale where they lay.
Daniel's door is opened (its bolt drawn) if it was shut, and he walks out of the frame, through his door, along the back passage, through
the hall and the parlor's door, and stands in the parlor facing it, turning one wrist in the other hand. If Rae did not let him out
herself, once he is in the parlor she walks to its door and stands in it, between him and Dan:

> Rae: Daniel. Please. Not yet. Not with him there.

A hand on the straps afterwards: "The straps hang open from their iron loops." Once he is out, he is spoken to wherever he stands, as the
others are; "I'll get you out." is no longer offered. He does not strain at the frame when Dan's voice carries. Vera does not bring Dan
down to the empty frame for the pin (the chapter waits at AFTER; the confrontation is N10's).

## Dan told

A reader who heard the confession has, with Dan, "Rae knew he was alive.":

> Dan: She knew?

and if Rae is near enough to hear him (18 blocks):

> Dan: You knew.
> Rae: I did it so you'd come.

## Lines new here (for the owner)

The confession is the script's. Everything else is new: KEY_GLANCE, RAE_WARY, the five reply labels, RAE_TO_VERA, RAE_GIVES, KEY_GIVEN,
RAE_ALL_RIGHT, RAE_SORRY, DANIEL_DONT, RAE_UNLOCKS, KEY_TURNS, STRAPS_OPEN, DANIEL_FREE, KEY_FOUND, RAE_NOT_YET, DAN_SHE_KNEW, DAN_YOU_KNEW
and RAE_SO_YOU_CAME (all in `LodgingTexts`). The plan's "Daniel may refuse her apology" is his "Don't."

## Records

Each reader's own: SawKey0608, HeardConfession0608, AskedKey0608, RaeTurn0608, ToldDanRae0608, FoundKey0608, UsedKey0608. The house's:
Key0608 (hook, rae, cupboard, vera, a reader's id, used), KeySeenAt0608, RaeConfessed0608, RaeTask0608 and RaeTaskAt0608, DanielFree0608,
DanielIn0608, FreedByRae0608, DanielSaidDont0608, DanToldRae0608.

## Left for later slices

- The key's custody when its holder leaves, disconnects or dies (slice 17: it goes back to Rae's coat, or the cupboard if she hid it).
- Vera with the key, and Rae turning on her (N8).
- The two men meeting, and Rae's attempt to keep them apart beginning physically (N10).

## Cases

Three, each its own batch: `raeTakesTheKeyIsSeenWithItAndAReaderLetsDanielOut` (the hook emptied at FETCHING, the glance seen by the reader
looking and not by the one looking away, the confession, "Give it to me.", Dan told and answered, the straps opened, Daniel walked to the
parlor, Rae in its door), `seenAndNotAskedRaeHidesTheKeyInTheCupboard` and `raeGivesDanielTheKeyHerself` (asked by "You knew the way up.",
her apology and his "Don't.", the straps opened by her, Daniel in the parlor, no doorway).
