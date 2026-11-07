# Said aloud (0.4.329)

The owner asked, while Speak Up was being built, that a reader's chosen lines be shown to other players. Until now a reply chosen in the conversation panel lived only in that reader's panel and journal; anyone standing beside them saw nothing.

## What changes

When a reader chooses something to say in the panel, the words show over that reader's head, above their name, for every other player within sixteen blocks. They are drawn the way the House's own people speak: in the world, never in chat. They fade in, stay long enough to read (three seconds, longer for more words, up to nine), and fade out. Long lines wrap.

The speaker is not sent their own words. They have them in the panel already, and from their own eyes nothing would show.

## Said and done

Not every reply is something said. Many are things the reader does: taking a cup, walking on, putting a receiver down. Each reply is now one or the other. A reply is said unless the scene marks it as done (`Talk.act`), and a done reply shows nothing over anyone's head. These are marked as done:

- **Speak Up**: "Say nothing." (the unseen reader's fourth answer).
- **The Ghosts Set**: "Ready for today's filming", "Stay for supper", "Review the edited episode" and "Review the unedited camera log". "I'll leave it for now" is still said to Barry.
- **Second Person**: the phone's "Put the receiver down.", "Leave it off the hook." and "Listen."; the word breathed onto the glass, which is written and not said; and "Nothing." at the glass.
- **Johnny Truant**: "Take some bread.", "Take the coffee." and "Tell him about…", which hands him the place rather than voicing it. In his hospitality, "Take the cup.", "Call out to warn them.", "Get in his way.", "Set the cup down and go." and "Stay a moment."
- **A Place for You**: "Take the spoon.", "Leave it." and, on the road, "Keep walking." and "Go back."

Everything else in the panel is said, including the answers to Johnny's questions and the Confession's answers.

The empty chair in Speak Up does not use this. Its words show over the chair, as before, and the reader's own answer shows over the reader when they give it. One reader's answer still gives nobody else a voice.

## How it works

- `Talk.Reply` carries whether it is said. `Talk.reply` makes a said reply and `Talk.act` a done one; the old two-argument constructor still makes a said one.
- When a said reply is chosen, `Spoken.say` sends `SpokenPayload` (the speaker's entity, the words, how long they stay) to every other player in the same level within `Spoken.EARSHOT`. Spectators say nothing aloud.
- `SpokenClient` keeps the words for that player and draws them in `RenderPlayerEvent.Post`, facing the camera, three-quarters of a block above where the name tag sits.
- Protocol 137, for the new payload.

## John's hand between takes

The owner asked whether the bandage holds for the rest of the days. It does: from day 3 to day 7 John wears it whenever he is drawn, because the reader's day reaches the client every tick while they are in the set. Day 2 had a gap. The open cut showed only while John held his hand pose, so once that day's take was over and he stood or sat normally, his hand looked whole again until the next day. The server now says when John is hurt (after supper's cut on day 2, or once day 2 is done, and every day after), and the cast draws the cut whenever that is so.

## Validation

- The Speak Up two-reader case now also checks four things:
  - when one reader asks to leave, the other hears "I'd like to leave.", and the first reader hears the other's "Some water, please.";
  - the speaker is not sent their own words;
  - the empty chairs' echoes are not counted as either reader's speech;
  - a done reply ("Take the glass.") is not heard.
- A new Ghosts case checks when John's hand is hurt: whole on day 1 and on day 2 before supper, cut at supper and after day 2's take, and bandaged on days 3 to 7.
- The sixteen cases that choose panel replies pass, from A Place for You, Second Person, Johnny Truant, Speak Up, the Ghosts Set's dialogue, the journal and the literary rooms.
- No client proof checks the drawing yet. The words over a head have not been captured in a screenshot.
