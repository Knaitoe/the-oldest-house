# Second Person, the first slice — 0.4.274

The proposal is docs/SECOND_PERSON_PROPOSAL.md. The owner approved it: "Your proposition is great. The only notes are that violence is
acceptable to show, it's a horror mod - use your judgement for what fits." This page records what the first slice builds and what
it leaves for later, following the proposal's own plan:

> 1. **The neutral story first.** ... no read at all ...
> 2. **First slice:** summer and autumn, the book, the cupboard, the unseen verbs and the HAVEN phone arc, ending on E1.

The proposal's open questions are taken as the draft has them: the reader stays unseen except through reflections; the House's
pages (winter onwards, a later slice) are found afterwards; E2 stays. None of these is reached in this slice except the first.

## In one paragraph

A flat above a closed launderette, across a terraced street from the door the reader came in by. Somebody moves in: the House's
guess at the reader, wearing the reader's own House body, with the routine object they chose in the bathroom, the coat they chose
in the coatroom and the face they drew at the kitchen table. Nobody in the flat can see the reader, answer them or step aside for
them. The reader can do what ghosts do: move things, open and shut doors, turn lamps on and off, make a room cold, knock, lift a
ringing phone, write a word in breath on a cold pane, and stand where glass might show them. A children's adventure book on the
shelf keeps up with the year and lists, by number, what the reader can do now. The airing cupboard, warm, with its ticking tank,
is how the year moves on: going in and shutting the door turns the season. This slice has late summer and autumn, then a short
last morning in which the door has been left on the latch.

## What this slice builds

- **The street and the flat**, whole, in the outside dimension, a copy for each reader (SceneInstances). The terrace opposite has
  the door the reader came in by; the road; a pavement with a lamp post and a bus stop where nobody waits; the launderette, shut;
  the side door and stair; the landing, with Iris's door opposite; the flat: hall (post mat, coat hooks, the hall glass),
  sitting room (armchair, sofa, the phone, the shelf and the book, the television, a lamp, the street window), kitchen (table for
  two, fridge, kettle, sink and tap), bathroom (bath, basin, the mirror cabinet), bedroom, and the airing cupboard at the end of
  the hall. Pixel-art materials by tools/draw_second_person_art.py, checked in CI.
- **The resident**: one saved private actor wearing the reader's House body, coat and hair (as the reader's doubles already do),
  walking the flat on footpaths. They talk to themselves above their head and never to the reader. They pause, not reset, when the
  reader leaves, and stop in a doorway the reader is standing in rather than walk through them.
- **Iris Dunne** across the landing, painted, in autumn: knocks most evenings with the end of something she cooked, and is the
  only one who senses the reader.
- **Sam**, from the resident's work, painted, the autumn guest (the neutral version of the DRAWN_TO evening).
- **The voice on the phone**: a parent who is kind and starting to forget (settled HOME = HAVEN), or an old friend (everyone else,
  including inference off). Voices only, through the receiver, in the conversation panel.
- **The unseen verbs**: doors; lamps; the movable things (the routine object, the coat, the drawing, the photograph from home),
  carried one at a time and set down where the reader chooses; cold; touch; knocking (on Iris's wall and on doors); the phone
  (lift, put down, leave off the hook, or leave ringing); breath on cold or steamed glass; standing where a reflection or a
  camera catches the reader; the cupboard.
- **The book** on the sitting-room shelf: its opening pages are the vignette's first paper (and so are outlined until read,
  0.4.273); after that it opens at its latest page, which says where things stand and lists by number what the reader can do now.
  Pages the reader "turns" (acts they take) are kept, in order, for the account and the keepsake.
- **Decisions 1 to 11** of the proposal's ledger (summer: putting away or hiding, the hall glass, the first night, the first
  call, leaving and coming back; autumn: the routine before the guest, the evening, the photograph, the second call, the end of
  the evening, Iris's wall), and **E1, the door on the latch**.
- **The account and the keepsake**, written from the record.

## Left for later slices

Winter (Iris's death, the sleepwalk, the care scene, being seen, the tin, the House's pages), spring (the agent, the application,
the question on the glass), E2, E3, E4 and the aftermath; the DANGER and RUNAWAY phone arcs; the AGE situations and the DRAWN_TO
people (Dev, Hannah, both, Marcus). Readers whose read would choose those get the neutral version, which is a whole story. Stranger
phases 75 to 81 are kept for this vignette's people.

## The script, neutral and HAVEN

### The book's opening (the first paper)

> 1. You are standing in a hall that isn't yours. The boxes are not yours. In a minute somebody will come up the stairs with the
> last of them, and they will not see you. They will never see you, not straight on.
>
> You can do what people in houses like this have always done: move a thing from where it was put, open a door, shut one, turn a
> lamp on, stand close enough to make the air go cold, knock on a wall. You can lift a phone that is ringing, and listen.
> Breathe on cold glass and it will hold a word. Stand where glass faces you, and you may be seen in it.
>
> When you want the time to pass, the airing cupboard at the end of the hall is warm. Go in and shut the door.
>
> This book will keep up with you. Turn to its latest page when you need to know what you can do.

### Late summer

- **Arrival.** The reader comes up into a flat of boxes. Within twenty seconds (or once the book is read) the resident comes up the
  stairs with the last box, puts it down and says "Right." Then, quieter: "Right. That's everything." They sit on the box a moment.
- **Unpacking (1).** Through the afternoon they unpack: the photograph from home onto the shelf, the kettle on, the drawing (the
  reader's own, copied) out of a box and onto the fridge, the coat on a hook, the routine object onto the bathroom shelf. Before
  they get to a thing, the reader can carry it there themselves (put away), put it somewhere else, or hide it (the airing
  cupboard, under the bed). Put away: "I didn't put that there." A pause: "I must have." Hidden: they hunt for it ("It was here.
  It was in this box."), then give up: "It'll turn up."
- **The hall glass (2).** They hang it facing the room. The reader can turn it to the wall; they find it so, and leave it:
  "...Fine." (A resident read as UNSEEN hangs it facing the wall themselves: "Not yet.")
- **The bath and the first call (4).** In the evening they run a bath. The phone rings.
  - HAVEN: "Hello? Love? It's only me. I wanted to know you got in all right. Did you say Tuesday? For the bed, I mean. Did you
    say Tuesday?" Lifted, the parent talks on fondly into the silence: "This line's terrible. I'll let you go. Ring me when
    you're settled. Did you say Tuesday?" Put down: the resident, dripping in the doorway, looks at the phone a long time.
    Left ringing: they run out dripping and catch it: "Mum? Dad? No, I'm here, I was in the bath. Tuesday, yes." Off the hook:
    the parent cannot get through, and is worried next season.
  - Neutral, an old friend: "It's me. Just checking you're alive. The new place. Is it awful? It's awful, isn't it." Lifted,
    the friend talks on and gives up laughing: "I can hear you breathing, you know. Ring me." Left: the resident catches it and
    laughs. Off the hook: nobody gets through, and the friend asks about it in autumn.
- **The first night (3).** They go to bed and cannot sleep. The reader can stand in the bedroom doorway ("...hello?"), run the
  kitchen tap (they get up, find it running, turn it off, stand there), sit in the armchair (nothing happens, which is its own
  answer), or shut the bedroom door on them, a kindness: they sleep. Left alone, they sleep at last, near dawn.
- **Glass (any time it is cold or steamed).** The bathroom mirror after the bath, the street window after dark: breath writes one
  word. Summer offers HELLO, SORRY, STAY, GO. They see it, and say so in their own way; the word stays until they wipe it.

### Autumn

- **While you were in the cupboard...** The book opens the season with what happened: they have a job (the rota is on the fridge),
  a rug, the radio on for company. Things the reader put where they live are still where the reader put them.
- **Iris (11).** She knocks most evenings with a covered plate, talks about her husband's chair, and once, at the door, says to
  the room behind them: "This flat's never been empty in my time. You're all right. Just don't frighten them." Knock on her wall
  and she knocks back. Later she tells the resident: "You've got company. I don't mind it. I like a bit of company."
- **Getting ready (5).** Sam is coming for supper. They do their routine first, with the object the reader chose, wherever it
  now lives; if it was hidden they cannot find it ("I had it. I had it this morning."). The reader can set it out where they will
  see it: "Thanks," to nobody. Then they stop.
- **The evening (6).** Sam comes. The reader can help (light the lamp, straighten the cloth on the table, shut the window), interfere
  (cold beside Sam, a door, a knock: "Your flat's haunted," Sam laughs), be seen (stand in the dark window behind the resident's
  chair, where Sam sees someone standing; the evening ends there), or leave them alone (go into the bedroom and shut the door).
- **The photograph (7).** They take a picture of the room to send to someone. The reader is in the frame or not. If they are,
  the resident looks at it a long while; after a good evening they keep it, after a bad one they delete it.
- **The second call (8).** HAVEN: the parent asks the new address, twice, and is embarrassed: "I've written it down somewhere.
  Say it again." Neutral: the friend wants to visit. Lift, put down, leave off the hook, or leave ringing, as before; if the
  phone was left off the hook in summer, the caller says so: "I tried and tried."
- **The end of the evening (10).** Sam asks if they can stay. The reader can make the flat cold, make it warm (lamp lit, window
  shut), or do nothing. The resident decides, from the evening: Sam stays, or goes home.

### The last morning, and E1: the door on the latch

Going into the cupboard after autumn: "While you were in the cupboard, the year went on without you: the frost, the short days,
then a letter about the tenancy." It is morning. The resident has decided (from the year so far) to stay or to go, and the front
door is on the latch, for the reader whether they know it or not. The book's last page offers one last thing first: lay the
routine object out, turn the glass round, or write a word. Then the reader goes out by the front door and down to the street.
From the pavement they see the decision play out: a light in the window and the resident at it, or the resident coming out of the
side door with one bag and walking to the bus stop and away. On the bus-stop bench, where nobody waits, is the book: the
keepsake, and the account. Reading it is the ending.

## Branching, not a score

The record holds what happened, not a meter: what they found moved and what they said; what was hidden; the glass's position;
the words written and whether they were seen; the calls (lifted, put down, off the hook, left); the first night; Iris's knock; the
routine set out; the evening (helped, interfered, seen, left alone); the photograph (in it, kept or deleted); Sam staying; the
decision; the last act. The resident's choices read that history; nothing is a count to reach.

## The account

Written from the record, never from the read's names, in the second person like the book. It opens with the first thing they
found moved, and then names: the routine object and where it lived; who rang and what the reader did with the phone; Sam, and how
the evening ended; Iris; the photograph, if the reader was in it; the words on the glass; the last morning and what they decided;
and the one last thing the reader did. It never says what any of it proves about the reader. One understanding source (Witness
74 / 56 / two kinds / three endings).

## Rules kept

- New builds only. Speech over heads and in the panel, never chat. The book and account never gender the reader and never name a
  colour or an age.
- The read is a snapshot taken on first arrival; the vignette never changes it. With inference off, nothing is read and the
  neutral version plays, and the routine object is a washcloth and the drawing a blank sheet.
- Nothing hurts the reader; there is no fail state. Leaving resumes the year where it was.

## As built

The pieces: `SecondPersonSite` (the street and the flat), `SecondPersonBlock` and `SecondPersonRegistry` (one fitting block, twelve
materials, the carried item), `SecondPerson` (the year, per reader), `SecondPersonStory` (every line, the book's latest page, the
decision and the account), `SecondPersonTexts` (the book's opening and the account book), `client/SecondPersonRenderers` (the
resident) and `client/SecondPersonClient` with `SecondPersonPayload` (the flat's hour). Slot 173, layout 88, network 127. Iris is
stranger phase 75 and Sam 76, painted by tools/paint_character_skins.py.

**The clock.** Each step has its own count of ticks, and it counts only while the reader is somewhere in the street or the flat.
Leaving pauses everyone where they are; coming back picks up from there. The hour in the sky comes from the step: an afternoon for
the unpacking, evening for the bath, midnight for the first night, dawn after it; early evening to night through autumn; a bright
morning at the end.

**The resident.** Their body is drawn from the reader's own House body, with the coat, glasses and hair the quiet rooms gave them.
Only that reader ever sees them. They walk the flat on footpaths when the reader can see them, and go straight to their place when
the reader cannot. They open the door to the room they are going into as they reach it. If the reader shut a door on them, they
say "Did I shut that?" once. They stop rather than walk through a reader standing in their way. Touching or striking them makes
them flinch, and does nothing else.

**The things.** The photograph, the drawing, the coat and the routine object are unpacked in that order. Before the resident gets
to a box, the reader can open it and take what is in it, one thing at a time. Setting a thing down on top of something puts it
there: in its own place (the bookcase, the bathroom shelf, the hook, the fridge door) it counts as put away. Anywhere else in the
flat it counts as moved, and the resident leaves it where it is. In the airing cupboard, or pushed under the bed, it is hidden, and
they hunt for it and give up ("It'll turn up."). A thing carried out of the street goes back to being lost. In autumn the resident
needs the routine object before Sam comes: if it is lost they cannot find it, and the reader can set it out for them ("Thanks.",
to nobody).

**The phone.** The first call comes in the bath and the second after the photograph. Lifting a ringing receiver opens the
conversation panel with the caller's voice: a parent, kind and starting to forget, for a home the House read as a haven; an old
friend for everyone else. The reader can only put the receiver down, leave it off the hook, or listen. A call nobody lifts is
caught by the resident. A receiver lifted and then stepped away from counts, after half a minute, as having been listened to.

**Glass.** The bathroom mirror steams once the bath has run. The street window holds breath after dark and on the frosty last
morning. Breathing on either offers four words: HELLO, SORRY, STAY and GO. A word stays until the resident sees it, says something
about it, and wipes it away. The hall glass can be turned to the wall and back. If the reader stands in front of it while the
resident passes, they stop and look into it a long time.

**Autumn.** Iris brings stew, talks about Ted's chair, and says past the resident into the room: "You're all right. Just don't
frighten them." Knocking on the passage wall, which is hers, gets a knock back. Once the evening is over she comes to the door to
say "You've got company. I don't mind it." Sam's evening is about a minute long and has four ways through, and the first one that
happens counts:
- helped: the sitting-room lamp lit, or the tablecloth straightened;
- interfered: standing close to Sam, a knock, or the kitchen door opened or shut;
- seen: standing in the kitchen window behind the resident's chair, after which Sam leaves;
- left alone: in the bedroom with its door shut.

The proposal's "shut the window" is not built; the lamp and the cloth stand in for it.

**The picture.** The reader is in it if they are in the sitting room and in front of the camera. After a seen or interfered evening
it is deleted, otherwise kept.

**Sam asks to stay.** Cold, or a bad evening, sends Sam home. Lighting the lamp then, or a good or quiet evening, lets Sam stay.

**The decision.** It is read off the record, never a count the reader is told to reach:
- for staying: Sam stayed, the picture kept, the routine set out, Iris answered, the bedroom door shut or the doorway stood in on
  the first night, a good or quiet evening, HELLO or STAY read;
- for going: the tap run on the first night, a seen or interfered evening, the picture deleted, GO read, a call put down.

They stay if the first list holds at least as much as the second.

**The cupboard.** It turns the season only once the season has done what it does: summer once the first night is over, autumn once
the evening is over and Iris has had her word, if the reader knocked. Before then: "It is warm in here, and the tank ticks. Not
yet."

**The end.** On the last morning the front door is on the latch, and the first of three acts is kept as the last thing: laying the
routine out, turning the glass, or writing on the window. Out on the street, a reader whose resident stays sees them at the lit
window. Otherwise the resident comes out of the side door with one bag, walks to the bus stop and goes. Then the book is on the
bench: the account, to keep.

**Tests.** `SecondPersonTests`, in the literary namespace:
- a summer: the book read, the photograph put away, the coat under the bed, the glass turned, the first call left off the hook,
  their door shut on the first night, and the cupboard into autumn, where the book opens at its latest page;
- an autumn evening: seen in the window, in the deleted picture, the second call let ring, the cupboard to the last morning, the
  glass turned round, the decision to go, and the account read.

`ArchitectureTests` checks the flat as built.
