# The list 1d: the town's people (0.4.702)

The fourth part of *The list* (`docs/THE_LIST_PLAN.md`), after 1c (`docs/THE_LIST_0_4_701.md`). It puts the town's people at their places
by night, each with words of their own. Nobody moves yet; the nights that move them come from part 3. The place is still not dealt and
is not yet a Witness source.

## Who is where

The names are the book's. Everything else about them, where they are and what they say, is new.

| Who | Where | How |
| --- | --- | --- |
| Amber | Her desk chair, behind her desk under her lit window | Sitting, facing the window |
| Jenna | Inside the drive-in's stand, behind the first order window | Standing at the window |
| Janice | The sidewalk in front of the video store, at the return slot | Standing, facing the slot |
| Deek and Wes | Side by side on the home team's bench at the sideline | Sitting, facing the field |
| Mel and Lesley | The top row of the home stands, seven seats apart | Sitting, facing the field |
| Shannon | The bus bench outside the feed store | Sitting, facing the street |
| Stace | The water tower's catwalk, on the side toward town, at the rail | Standing, looking out over the town |
| The deputy | The sidewalk outside the substation, by its door | Standing, facing the street |
| The reader's parents | Their bed, each with their head on a pillow | Asleep on their backs |

## Single and multiplayer

- There is one of each in the town, shared by every reader in it.
- What each says is that reader's own. The first time a reader speaks to one of them they hear the first line; the second time, the
  second line; after that, the second line again. A second reader starts from the first line for themselves.
- Lines are said aloud: they show over the speaker's head for anyone near and are kept in the journal of whoever hears them. The speaker
  turns to the reader they answered.
- Amber and Jenna are behind glass. A hand on Amber's window or on the drive-in's order window knocks; the knock is a cue to that reader
  alone, and the one inside answers that reader through the glass.
- The parents are asleep and say nothing. A hand on either is a cue to that reader alone, and so is a hand on their bed.
- Spectators earn nothing and are not spoken to.

## Made as

- **`TheListPeople`** (new) holds the twelve on stranger phases 269 to 280, their places (`AT`), which way each faces, and their skins.
  - `ensure` puts everyone whose place is loaded where they belong, making whoever is missing; a second of anyone is let go. A reader's
    arrival calls it, and so does the town's clock every five seconds while anyone is in town, for places that have loaded since.
  - They are held where they are put, without gravity: the stands and the catwalk have nothing solid under the cell they sit or stand in.
    Stace stands two pixels up, on the catwalk's grating.
  - The parents lie at their pillows' cells, eleven pixels up (the game's own height for a sleeper), asleep; the game's rule that wakes a sleeper whose bed is gone is not theirs
    (`keepLying`).
  - `talk` gives each reader their lines; `knock` answers a hand on a window someone is behind.
- **`ActorSeat`** sits The list's people in Amber's desk chair (ten pixels up), on the bus bench, the home bench and the stands' planks
  (eight up), turned the way each seat faces.
- **`LiteraryRenderers`** draws them with their own skins and lays the parents along their bed, the head on the pillow at the headboard,
  on the comforter eight up (`lieOnTheList`).
- **`TheList`** calls `ensure` on arrival and on its clock, and sends a hand on the two windows and the parents' bed to the people.
- **Windows** (`tools/draw_the_list_art.py`). Amber's window and the drive-in's order windows are glass a reader sees through:
  - Amber's is a double-hung sash, the meeting rail across its middle and the blind rolled up under its head, so she is seen at her
    desk in her lamp's light from the yard;
  - the order window is the stand's sliding window shut, a pixel-thin pane with the aluminum where its two halves meet, so Jenna is
    seen at the drawer from the lot.

  Both keep a streak or two on the glass, as the game's own glass does.
- **`LiteraryVignettes`** passes a word with any of them to `TheListPeople.talk`.
- **Skins**, painted by `tools/paint_character_skins.py` (twelve new rows, `literary_list_*`), with their builds and their rows in the
  character catalog (`tools/character_art_catalog.py`, `character_art.json`):
  - Amber in a sweatshirt too big for her, her hair long and loose;
  - Jenna in the drive-in's shirt and apron with a name badge, her hair tied back;
  - Janice in the video store's shirt and glasses;
  - Deek in a letter jacket, his hair cut close; Wes in a flannel over a tee and a cap;
  - Mel in a knit sweater, her hair in curls; Lesley in a jean jacket, her hair long;
  - Shannon in a hoodie, braids; Stace in an old field jacket, short hair;
  - the deputy in his uniform, a moustache;
  - the reader's parents in what they sleep in.

  The variety pass gives each their own face and build within the scene.
- **Tests.** `TheListTests` gains a sixth case, `theTownsPeopleAtTheirPlaces`:
  - everyone is made at their place, one of each in the whole town, none doubled, and still there a second later;
  - the six who sit are seated at their seats' heights, facing the right way; the four who stand are not seated; Stace looks out over
    the town; the parents are asleep on their pillows;
  - each of the eight out in the town gives the first reader their first line, the second reader their own first, the first reader
    their second line and then the second again;
  - a knock on Amber's window and on the order window is a cue to that reader alone and is answered through the glass, the first line
    and then the second, a second reader's knock getting their own first;
  - a hand on either parent is a cue with nothing said aloud; a hand on their bed is a cue of its own; the other reader gets none of it.

## Found and fixed in the making

Found by the new test before this commit:

- **The parents' height.** They were laid on their pillows' cells and the game lifted them eleven pixels, where it keeps any sleeper, so
  they were not where they were put. They are laid there now.

Seen in contact sheets and eye-level frames before this commit:

- **Mel and Lesley** were placed on the stands' fifth row, not the top one.
- **Jenna's visor** drew a slice of pizza on her cap; she wears the stand's name badge instead.
- **The windows.** Amber's window was a picture of her room painted on the glass, and the order windows were painted shut; neither
  showed who was behind it.

## Every new line

Everything here is written for this part. Nothing of the book is used but the names.

**Said aloud** (the first line, then the second)
- Amber: "It's one in the morning. Mom's asleep across the hall." / "Go home. I'll see you in first period."
- Jenna: "We're closed. The fryer's off. I'm here till this drawer comes out right." / "Twelve dollars short. It's been twelve every night this week."
- Janice: "The slot's for tapes. Somebody keeps putting notes in it." / "If you want a horror tape, the wall's picked over. Everybody's renting the same three."
- Deek: "Field's ours once the lights go off. Coach doesn't know." / "Sit if you're sitting. You're blocking my view of nothing."
- Wes: "Don't tell anybody we were out here. Deek's dad would kill him." / "We're waiting on a ride that isn't coming. You can wait too."
- Mel: "From up here you can see the water tower. That's all there is to see." / "Lesley keeps seeing somebody on the field. It's a dog. I told her."
- Lesley: "Don't sit by Mel. She talks the whole time." / "Somebody keeps crossing the field down there. Back and forth. That's not you, is it?"
- Shannon: "The bus doesn't come this late. I know. I'm not waiting for the bus." / "If my brother drives by, you didn't see me."
- Stace: "Careful on the ladder. It shakes enough with one up here." / "I come up here to see who's still out. Tonight it's you."
- The deputy: "Little late for a walk." / "I'm not calling your folks. Go on home."

**A hand on things** (grey cues)
- The parents: "Your mother, asleep on her back. She breathes slowly through her mouth." / "Your father, asleep. His watch is still on."
- Their bed: "The edge of your parents' bed. The mattress dips toward the middle."
- The windows: "You tap on Amber's window with a knuckle." / "You knock on the order window."

**Elsewhere**
- Speakers' names in captions: Amber, Jenna, Janice, Deek, Wes, Mel, Lesley, Shannon, Stace, The deputy.
