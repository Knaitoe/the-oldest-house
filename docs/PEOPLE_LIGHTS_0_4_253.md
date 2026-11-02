# After the People Lights Have Gone Off (0.4.253)

The owner's story proposal of 2 November (docs/AFTER_THE_PEOPLE_LIGHTS_PROPOSAL.md), after the title story of Stephen Graham Jones's
collection. None of the story's text is quoted; every line in the room is new.

The release adds one literary vignette, built whole: `PEOPLE_LIGHTS` (slot 160, one visit). The layout version is 82 (new builds
only). It adds one eligible Witness source, an **understanding** story. The quota stays derived: 72 sources, 54 required, two kinds,
three endings. There is no protocol change.

Code:
- `PeopleLights`: the evening.
- `PeopleLightsSite`: the house.
- `PeopleLightsBlock` and `PeopleLightsRegistry`: the fittings, materials, carried things and sounds.
- `PeopleLightsTexts`: Stan's offer, the account and its lines.

Assets:
- `tools/draw_people_lights_art.py`: ten materials, the fittings and the two carried things.
- `tools/generate_people_lights_audio.py`: five subtitled sounds.
- `tools/paint_character_skins.py`: Kelly, Mark and Stan.

## What the reader is

A guest for supper. Nothing in the house can hurt anyone, and nothing counts down. The evening belongs to the house's copy:
- readers who come in together spend it together;
- a reader on their own has their own copy (`SceneInstances`).

It starts again from the beginning once nobody has been in it for five seconds.

Following the owner's design, this story's dialogue is in chat: who said it, then what they said. Stage directions are grey italic
chat lines. Cues meant for one reader go to the action bar.

## The house

A narrow, tall house on a dark lot ringed with spruce. A front walk leads to the door, with orange and yellow glass beside it. A
driveway runs to the garage at the east end.

**Ground floor.**
- The foyer: large warm tiles, coats and shoes by the door, and the photograph of the two of them on the wall.
- The newel post: one block at the foot of the stair on the west side.
- The living room: oak boards, a sofa against the east wall, a lamp, the television and a coffee table.
- The kitchen: small pale tiles, the counters, the stove and fridge, a phone on the wall at sitting height, and a formica table with
  Mark's chair, a guest chair and Kelly's place at the end.
- The hall: boards, with a patterned runner down its middle. A lamp stands at the end of the hall, and a little table holds the
  house's book.
- The bedroom: carpet, the bed and two bedside tables.
- Kelly's bathroom: a grab rail, the tub, the toilet, a vanity, and the mirror hung where Stan put it, too high to see into from a
  chair.

**Upstairs.**
- The study: carpet. Mark's desk and chair face a dark oak rail over the open well above the foyer and living room. His work case
  sits on a second desk. There is a futon, boxes, a light, and the spare wheelchair at the top of the stair.
- North of the study: Mark's bathroom, and a storeroom with a ladder to the loft.

**The loft.** Boards, boxes and the old air mattress, folded flat, from the first night. At its edge is the rail Stan put up
afterwards (`RAILING`). It stands a block and a half to the hand, so nobody climbs over it.

**The garage.** Concrete a step down from the house, with a two-slab ramp from the side door in the kitchen. Its door is open south
onto the drive. The way on is a door in its back wall.

The ground floor's four surfaces each have their own chair sound (`PeopleLightsSite.floor`): the foyer's tiles, the living room's
boards, the kitchen's tiles, and the runner, which is nearly silent.

## The evening

**Arrival.** On the front walk, the reader hears Kelly and Mark through the window, about which weekend the mirror is getting done.
Inside, Kelly greets them. A reader whose House read says RUNAWAY is told the garage is always open.

**Supper.** Kelly wheels to her place at the table, and Mark comes down from the study. Right-clicking either of them opens a choice
of what to ask:
- Kelly: Stan's offer; what she still loves about the house (a HAVEN reader is asked something back); why they sleep downstairs (the
  first night in the loft, the mattress, before the rail).
- Mark: his work; the night she fell.
- Stan comes a little after anything has been asked, or after a minute.

**Stan.** He comes in by the garage and the side door without knocking. He talks to Mark about the offer over Kelly's head. A reader
who asks him to answer Kelly turns him to her: "I've measured." He starts about her mother walking in the night and stops. He leaves
his offer folded on the table and goes.

The offer is the room's source. It is a book in his hand, and its last page is a line he crossed out: "I should have put the rail up
first."

**The ordinary house.**
- **The mirror.** Kelly asks for it. Mark promises it after a call, and goes up to his desk. A reader can take it down and hang it
  lower. If nobody has after two minutes, Mark comes down with a screwdriver and does it himself. Kelly checks it when she reaches it,
  or when asked: "Hello, me."
- **Kelly's pass.** She crosses the kitchen tile, the foyer tile, the living room boards and the runner on her way to the bathroom,
  and each floor sounds as itself.
- **The floor scene.** Afterwards she is on the floor by the sofa, comfortable, and asks for a cushion. A reader may pass it (the
  sofa), or offer twice to help her back into the chair. Otherwise she gets back in herself in a while.
- **The runner.** A reader who asks first hears both sides: Kelly wants it down for the quiet, Mark wants it up to hear her. Then
  they may leave it or roll it up.
- **Mark's case.** It is his to move. Once he agrees, a reader can carry it down and set it on the kitchen table.
- **Footsteps overhead.** While Mark is at his desk and Kelly is downstairs, a reader on the ground floor hears footsteps cross the
  floor above the study.
- **The likeness.** While Kelly is downstairs, a reader under the well who looks up at the loft rail sees someone standing there,
  looking down, who looks like her. It turns its head to the nearest reader, then withdraws behind the boxes and is gone. Telling
  Kelly gets "Was it me?". The reader can answer "It looked like you" ("Those aren't the same answer") or "I don't know what it was".

**Bedtime.** Once the mirror is done and she is back in her chair, Kelly goes to bed half a minute after the likeness was seen (time
to tell her about it), or after three minutes. Mark goes back up to his desk.

**The night.** The lights go out and the chair goes quiet. Kelly, asleep, pulls herself round the hall to the newel post and back
without it (`ROUND`), to a pulling sound with breaths between.
- She stops at anyone in her way.
- A reader may step aside, stay close and watch, or turn on the living room lamp.
- Saying her name brings Mark down the stairs. Said three times, with him there for the last, she wakes. Otherwise Mark wakes her
  himself after eighty seconds.
- She doesn't remember the route. "Kitchen. All of us."
- At night Mark can be asked what he has seen before. It is not the first time: he holds up a bandaged hand.

**The kitchen table.** Kelly's chair, Mark beside her. Right-clicking either of them opens the table:
- Ask Kelly what she wants tonight: to call her father in the morning, and to not be listened to through the floor.
- Ask Mark what he has been leaving out: the thing upstairs looks like her, and he sits up there to keep her where he can hear her.
- Say you can't tell them what the house is doing.
- Ask for a little time.
- The plans, which need both of them:
  - **One more night, and call Stan in the morning.** This becomes possible once Kelly has said what she wants.
  - **Calling Stan together, now.** Kelly refuses unless she has been asked first, and Mark refuses until he has said what he has
    been leaving out.
  - **Spending the night downstairs together.** Refused until Mark has spoken. Then Kelly chooses the change: his work at the table,
    the lamp by the mirror left on, or the runner her way.

## The three plans

- **One more night.** They share the bedroom and Mark shuts the door twice.
  - The reader proposing it is asked to stay. If they do, they let the night pass from the sofa.
  - A reader who calls through the shut door wakes Kelly, and they come back out to the table.
  - In the morning, Mark's legs are numb and Kelly's foot moves under the blanket. A reader may bring the spare chair down from the
    top of the stair, unfold it by the bed, and Mark sits in it.
  - "Give me your hand." The story resolves for everyone still in the house: `one_more_night`.
- **Giving them the night.** The proposing reader may leave instead of staying. That reader alone resolves with
  `gave_them_the_night`; anyone else stays for the night and the morning.
- **Leaving together.** Kelly rings her father. She takes the photograph from the foyer and Mark takes his case. Both go out through
  the side door, down the ramp and out of the garage to Stan's headlights on the drive. As they reach the drive, the likeness stands
  at the loft rail behind the lit windows. "Tell me on the way." The outcome is `left_together`.
- **Sharing the downstairs.** Kelly's chosen change is made, and Mark's case comes down to the table. She deals. Above, at the loft
  rail, something moves quietly. The outcome is `shared_the_downstairs`.

Each plan resolves the story for every unfinished reader in the house. They are all one Witness source.

## The account

`PeopleLightsTexts.afterwards` is written in the reader's own words, and says only what they were there for:
- what they were told, and by whom;
- what they did for the household;
- what they heard and saw;
- how the night ended.

It never names a cause, and keeps "It looked like her. That isn't the same as it being her." A last page credits the story it is
after. The book is titled "The account: Stan's house" (the room's prose name, "the house Stan built", is one letter too long for a book
title).

The Witness line, the House of Leaves place name ("the house Stan built"), the co-op line and the miniature note follow the same
facts.

## Cast and art

Stranger phases:

| Phase | Who |
|---|---|
| 57 | Kelly |
| 58 | Kelly in her chair |
| 59 | Kelly on the floor |
| 60 | Kelly pulling herself along |
| 61 | Mark |
| 62 | Stan |
| 63 | The likeness |
| 64 | Kelly in bed |

The six Kelly phases share her skin. The renderer:
- draws her chair under her as a block model (`WheelchairLayer`);
- seats her at the chair's height;
- lays her flat to pull herself along with her arms;
- sleeps her in the bed.

The wheelchair faces the way its sitter would: footrests forward, big wheels behind, the seat's top at eight pixels.

## Tests

`PeopleLightsTests` (literary):
- the evening through to one more night;
- calling Stan together;
- sharing the downstairs, with a second reader at the table;
- one reader giving them the night while another stays.

`ArchitectureTests`: the house's floors, newel, rail, runner, mirror and case, and the offer's empty place before Stan.
