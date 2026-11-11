# How long words stay up: one rule, and a setting (0.4.473)

The owner, 11 November, with a picture of Patrick's caption in Drisking ("I can ride all the way to the fort. Kendra says
pushing on the steep part doesn't count."):

> slow dialogue a bit across vignettes, please.

> if you could make a universal handler and then make it configurable via config or settings that would be incredible

## What was wrong

A person's line already had a sensible rule of its own (0.4.398): it stays up longer the longer it is. Hardly anything let it
apply.

- **Seventeen scenes took a line down on a timer of their own.** Most of them used one fixed time for every line, between
  three and a half and six seconds, however long the line was. Drisking took every line down after five. Patrick's line in the
  picture is eighty-nine characters: five seconds is not long enough to read it.
- **In Drisking the next person could start after those same five seconds**, over a line the reader was still reading.
- **The action bar gives every line three seconds**, whatever its length. What a reader notices is written there, and several
  scenes repeat a person's line there.

## The rule

`labyrinth/Dialogue` decides how long words stay up, and nothing else does.

- **A person's line** stays up for two seconds to find it and three twentieths of a second for each character, never under five
  seconds and never over twelve. (That is the rule a line already had; it is now the only one.) Patrick's line stays twelve
  seconds. "Hello." stays five.
- **The person keeps their own line up** for that long and takes it down themselves. A scene no longer times a line: where it
  needs to know, it asks the person whether they are still saying it (`LiteraryActor.saying`). Seventeen scenes that had a
  timer now ask.
- **A person who is cut off** (interrupted, put away, dead) loses their line at once (`LiteraryActor.hush`). Nothing uses it
  yet that did not already clear the line another way; it is there so that the next scene written has the right word for it.
- **A reader's own words** over their head (a reply chosen aloud) stay up by the same rule.
- **The action bar.** The House's own words there stay up by the same rule, but never under the game's three seconds. The
  game's own lines ("Press Shift to dismount") are left alone.
- **Drisking.** Nobody in town starts a line of their own until the line the reader was last given has had its time.

## The setting

`readingTimePercent`, in the world's settings. 100 is the usual time. 150 is half as long again; 50 is half. It goes from 25
to 400, and nothing stays up under a second and a half whatever it is set to.

- **In the game:** Mods, This House Has Ghosts in It, Config. The mod had no settings screen before; this is the game's own
  one for mods, and it shows every setting the mod has, each with the note that stands beside it in its file. The world's
  settings can be changed there from inside a single-player world.
- **In the file:** `serverconfig/the_oldest_house-house-server.toml` in the world's folder.

It is the world's setting, so everybody in a world reads at one pace. A client is sent it with the rest of the world's
settings, which is how its action bar keeps the same time.

## What this does not change

- **When a scene says its lines.** A scene's lines come on its own story's clock (a knock, a chime, a count), and those beats
  are the scene's. Only how long each line then stays is changed, and in Drisking how soon the next may follow.
- **The conversation panel** (Talk). It waits for the reader.
- **Words drawn on signs standing in the air** in seven older scenes (the finale, Holloway's camp, the arcade cabinet, the
  hotel, the séance, the second-person rooms and the manor's own people). Each still keeps its own time. They are the next
  thing to bring under the rule.

## Tests

- `LiteraryTests`, one more case: **a line stays up as long as it takes to read, by one rule, and the setting scales it.** The
  rule's numbers for a brief, a middling and a long line; the setting at 150, 50 and 25; the action bar's time; a person's long
  line still up after six and a half seconds and gone when it has been read; the world's setting at 200 keeping a brief line up
  past its usual time; and a person cut off losing their line at once.
- `BorrascaTests`, one more case: **a line in Drisking stays up as long as it takes to read.** Patrick's line from the owner's
  picture: still up and still being read six seconds on, where it used to be taken down at five; gone when it has been read,
  and only then may the town speak again.
- The Masque's case that waits for the red figure's line to go waits by the rule's own number.

## What I have not seen

- **Any of it in the game.** How long twelve seconds of Patrick feels, whether three captions stacked is too many now that
  each stays longer, and whether the action bar's longer time is welcome everywhere, are the owner's to judge. The setting is
  there for exactly that.
- **The settings screen.** It is the game's own; I have checked that the client starts with it registered and that every
  setting has a name, not looked at it.

## Numbers

No layout, protocol or Witness change. New builds only.
