# Faces and lines (0.4.328)

Three things from the owner's messages of 7 November:

- "I don't see any mark on his hand."
- "The line Barry is saying is also poorly written", and, once Speak Up was done, "did you parse ghosts set for obvious ai dialogue?"
- "Can you try to add more variance to the facial hair of the npcs… many of them share that little chin strip."

## John's hand

0.4.326 covered the mark on John's left forearm, so the injury was meant to show only on the right. But the mark was painted on the arm net, low on the forearm, and from any ordinary distance his right hand looked whole. The injury is now geometry on his right hand, drawn over the arm by the cast model:

- **Day 2, from the moment he hurts it.** An open wound on the back and outside of the hand, with blood running toward the wrist.
- **Day 3 onwards.** A wrap of gauze from the knuckles to halfway up the forearm, with two folds and a stain come through on the outside and back.

The left hand stays plain. The skin files are unchanged. The filmstrip has a new "bandage" page showing John on day 2 (side and front), day 4 standing and sitting, and day 6 flinching.

## The Ghosts Set's lines

Barry's day-one line ("If Merry looks into the lens, keep rolling. We can always ask her again.") read as written rather than said. The rest of the set was checked for the same thing: lines that explain their own subtext, lines built on a neat turn ("I said I wasn't scared downstairs. That was downstairs."), and adults who speak in balanced sentences. Fifty-six were rewritten to be shorter and plainer, and to sound like the person saying them. Some examples:

| Who | Was | Now |
| --- | --- | --- |
| Barry, day 1 | If Merry looks into the lens, keep rolling. We can always ask her again. | She'll look right at the camera. Kids always do. Just stay on her. |
| Merry, day 1 | If you take that book, bring it back. She writes in it. | That's our book. You have to give it back. |
| John, day 1 | I'm home for now. They keep asking when I'm going back, like I wouldn't go tomorrow if I could. | I'm between jobs right now. Everybody asks. I'd go back tomorrow. |
| John, day 2 | Don't come any closer with that. I can see my own hand, thanks. | Get that out of my face. It's a cut. It's fine. |
| Wanderly, day 2 | I wasn't at that meal. I won't pretend a recording puts me there. | I wasn't there that night. I've only seen the tape. |
| Sarah, day 4 | The bills still turn up. I know you think about that when I say we're doing this for her. | Yes, they're paying us. That's not why we said yes. |
| Sarah, day 4 | I'll put the food away. Apparently supper can wait as long as you need it to. | Supper's going cold. Again. |
| Merry, day 5 | When I say something boring, nobody asks me to repeat it. | Can I go now? I have homework. |
| Marjorie | You keep the camera on the scary voice. When I ask for help, you look away. | Why do you only film me when I'm being scary? |

Four closing sentences in the things a reader can pick up were cut or trimmed where they explained what had just been shown (the cookbook's "She has never liked the sauce.", the hymn book's "Nobody has written in it since."). No line changed what happens or who says it, and nothing in the set's sequence moved.

## Facial hair

Every bearded man in the cast was painted from one of three styles, and all three drew the same thin line round the jaw. The cast now has thirteen styles, each with its own shape on the face:

- **Stubble**: an uneven stipple over the jaw and lip.
- **Shadow**: an even five o'clock shadow, darkest at the chin.
- **Patchy**: a beard that has not come in, on the chin and the corners of the jaw.
- **Goatee**: round the mouth and down to the chin.
- **Short**: close-trimmed and boxed, with sideburns, a filled jaw and clean cheeks.
- **Full**: up the cheeks to the sideburns and over the lip, the mouth a dark gap in it.
- **Chops**: side whiskers widening to the jaw, joined across the lip, with a shaved chin.
- **Walrus**: a heavy moustache drooping over the mouth.
- **Pencil**: a thin trimmed line under the nose.
- **Handlebar**: full under the nose, swept out and up at both ends.
- **Horseshoe**: over the lip and straight down past the corners of the mouth.
- **Chinstrap**: the old jaw line, kept for one man.
- **Moustache**: the original moustache.

The short, full and chops styles carry round under the ears onto the sides of the head, and a full beard covers the underside of the chin.

Where people share a room, they now wear different beards:

- **The Last Stop**: Russ short, Denny a goatee, Wade a horseshoe, Hollis chops.
- **The cottage on the mill road**: Walter chops, the sergeant a handlebar, the man from the mill a pencil moustache.
- **The hotel**: the porter a pencil moustache, the bartender a handlebar, the pianist his old moustache.

Elsewhere:

- **Stubble**: the man in the basement, Johnny Truant and a road worker.
- **Shadow**: the window cleaner, the séance father and the grey-haired road worker.
- **Patchy**: Mark and the nightclub dancer.
- **Goatee**: a passenger.
- **Short**: Dell and Brad.
- **Full**: Lyle in his witch costume, the Holy Rabbit's father and the old man in the cabin.
- **Walrus**: Ray the projectionist and the finale's witness.
- **Horseshoe**: Hank and a stranger.
- **Chinstrap**: Holloway.

The Drisking sheets are byte for byte as they were: the sheriff keeps the original moustache and the second worker the chinstrap. The manor's family portraits, approved in 0.4.223, are unchanged too. The old man's portrait keeps the chinstrap, and Will's keeps the old light jaw line, now named "jawline" and used nowhere else. Thirty-six skin sheets were painted again. Only beard pixels changed, and every sheet keeps its size and opacity.

## Validation

- `tools/paint_character_skins.py --check` and `tools/verify_character_art.py` pass. The 24 repainted sheets that had an approved hash are recorded in `art/inventory/beards_04328.json` with their previous and new hashes. The verifier chains that record after the earlier repaints, so the package check's 0.4.65 cast list follows it too. The other 12 had no recorded hash and are listed in the same file.
- The 40 Ghosts cases.
- The quick asset gate.
