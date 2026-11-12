# The Pell house 10: Gail's call (0.4.532)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its
street and lot, its rooms, its line, its people, its first visit's day, evening, bedtime and night, and its first morning
(`docs/PELL_HOUSE_MORNING_0_4_531.md`). This is its tenth slice, the rest of the proposal's "1. The placement":

> The morning: June has made breakfast, and Gail calls to see how it went.

It also starts on the proposal's "Being framed":

> **What Gail hears.** Each framed incident (the knife, June's hand, a broken thing) comes to Gail as June's version first.
> - **Answering Gail.** The older sibling answers her on the conversation panel. A calm answer and an angry one go down differently,
>   and the account remembers what was said.

and on Gail herself: "kindness under time pressure. She has always just spoken to June." Nothing is framed on the first visit ("No harm
comes to anyone. This visit is the house, read slowly"), so there is nothing yet to mark against anybody; this slice sets up the call
and the answering. Gail promised it at the step ("I'll ring in the morning and see how the first night went") and in the notice of
placement ("I will telephone every morning for the first week"). The House still does not deal the house.

## What happens

- **The telephone.** It hangs on the kitchen's east wall by the arch from the hall: a cream wall set, the receiver on its hook, the
  coiled cord looped under it, and beside it a strip of masking tape with GAIL and a number on it in pencil. A hand on it says so.
- **The ring.** A while after June has washed up (five seconds), it rings, and goes on ringing every two seconds until it is lifted.
  June says "That'll be Gail." and goes to it from the sink.
  - A reader who gets there first and lifts it hears Gail, on the conversation panel: "Hello? June? Oh, hello, love. It's Gail. Is June
    there? Put her on for me first, would you?" June comes and takes it: "I'll take that, love."
- **June's side.** June talks to Gail first, a pause after each line while Gail talks:
  - "Hello? Oh, Gail. Morning, love." (or, having taken it from a reader, "Gail? Sorry, love. Morning.")
  - "No, no. No trouble at all."
  - "Slept like logs, the pair of them." If she met a reader out in the night, or one spoke to her, or stepped in her paint, instead:
    "Slept like logs. Well, one of them was up and about in the night. I saw them back to bed."
  - "Tilly ate every bite. She's a love."
  - "And Callum's been good as gold."
  - "Hang on, I'll put them on."

  A hand on June while she is on it: "June has the receiver to her ear. She lifts a finger: a minute."
- **Holding it out.** "It's Gail, love. She wants a word." She turns to the room with the receiver held out. Readers too far off to hear
  her are told it: "June, calling from the kitchen: 'Phone, love! It's Gail.'"
- **Each reader's word.** A hand on the receiver, or on June holding it out, puts the reader on the line, one at a time; anyone else's
  hand finds that "Somebody else has the receiver."
  - Gail: "Hello, love. It's Gail." / "I've just been talking to June. She says you've settled in. How was the first night?"
  - The reader answers on the panel:
    - "It was fine."
    - "I didn't sleep much."
    - "June was out in the garden in the night." (only for a reader who saw her out)
    - "Callum was standing outside in the dark." (only for a reader who saw him on the line)
    - "I don't want to stay here."
    - or, said with nothing, "Say nothing."
  - What they say aloud is said in the kitchen, in front of June: it shows over their head for anyone near, as all panel speech does.
  - Gail answers with June's version already in her:

    | Told | Gail |
    |---|---|
    | It was fine. | "Good. That's good, love." / "June's been doing this a long time. You're in good hands." |
    | I didn't sleep much. | "No, I don't suppose you did. First nights are hard." / "It gets easier. I promise it does." (If June told her somebody was up: "June said somebody was up and about." / "First nights are hard. It gets easier, I promise it does.") |
    | June was out in the garden. | "In the garden? In the dark?" / "She'll have been seeing to something, love. She's always up at all hours, June." |
    | Callum was standing outside. | "Callum does that. June did say." / "He won't hurt anybody. Let him be, love." |
    | I don't want to stay here. | "Oh, love. I know." / "Nowhere's going to be home for a while. Give it a week. Give June a week. That's all I'm asking." |
    | (nothing) | "Hello? Are you there, love?" / "That's all right. You don't have to say anything." |

  - Then: "I'll ring again tomorrow. Put June back on for me, would you?" The reader gives the receiver back to June.
  - Given back, if another reader at the house has not had a turn, June holds it out to them: "Your turn, love. Gail wants a word." A
    reader who has had theirs: "Gail has had her word with you."
  - A reader who goes off with the receiver (out of reach of the telephone or away from the house), or keeps it a minute, has said what
    they said and no more: June has it back.
- **June's goodbye.** Once every reader at the house has had a turn, or nobody has come for it in three quarters of a minute, June has
  it back:
  - "Gail? It's me." (If nobody came: "Gail? They're a bit shy yet. I'll tell them you rang.")
  - for each thing said in front of her: "Oh, that. I don't sleep, you know me. I was only seeing to the garden." (June out in the
    garden); "He does wander. I keep an eye on him." (Callum); "It's early days, Gail. Early days." (not wanting to stay);
  - if readers came but none said anything: "Shy. It'll come.";
  - "Same time tomorrow. Bye now, love."

  She hangs up, and has a word for the room if it needs one: "Inside the line after dark, love. I did say." if somebody told Gail she
  was out in the garden, or else "Give it time, love. That's all." if somebody said they didn't want to stay. Then she goes back to the
  sink, and the morning waits.

Nobody is hurt and nothing is forced. Nothing said marks anybody on this first visit.

## What each reader keeps

Each reader's record now also keeps:

- whether they lifted the receiver before June got to it ("AnsweredThePhone");
- whether they heard June's side of the call, near enough to her ("HeardJuneOnPhone");
- whether they took the receiver ("SpokeToGail");
- what they told Gail ("ToldGail": `fine`, `no_sleep`, `june_out`, `callum`, `leave`, `nothing`, or `walked_off` for a reader who went
  off with it before answering);
- whether they were at the house when June hung up ("GailRang").

The account is not written yet (the house cannot be finished); these are kept for it.

## How it is made

- **`PellCall`** has the call. `PellMorning.after` rings it `RINGS_AFTER` (100) ticks after the washing up; `PellMorning.tick` hands the
  house to `PellCall.tick` from then on.
- **The steps** after `PellMorning.AFTER`: `RINGING` (the bell every `RING_EVERY` (40) ticks until it is lifted; June walking to it),
  `ON_PHONE` (June's side, `LISTEN` (30) ticks after each line), `OFFERED` (held out), `BACK` (her goodbye) and `DONE` (hung up). The hour
  stays at eight, as from breakfast.
- **The telephone** is a new `PellThing` kind, `PHONE`, flat on the wall (nothing bumps into it), placed by `PellSite.things` at
  `PellCall.PHONE` (-7, 1, -69) facing west. Stage 0: the receiver on its hook and the cord looped under it; stage 1: the empty hook and
  the cord out toward whoever has the receiver. Drawn by `tools/draw_pell_things.py` (`phone_plate`, `phone_handset`, `phone_cord`,
  `phone_tape`).
- **The receiver** in June's hand is a new item, `pell_receiver`, built from the telephone's own art by `tools/draw_pell_people.py` (no
  picture of its own, like the lantern and the tin). June has two new gestures for the renderer: `PHONE` (her right arm bent, the elbow
  out at her side and the hand at her cheek round the receiver, its earpiece at her ear and its mouthpiece at her mouth; her other arm
  across her waist; her head to the front) and `OFFER` (held out at arm's length).
- **The bent arm.** A cast member's arm is one straight piece, and no straight arm brings a hand to the ear. For `PHONE` the cast model
  draws the right arm in two in its place (`bent_arm`, shoulder to elbow and elbow to hand, on the arm's own skin), and the receiver is
  drawn against her head (`CastModel.receiver`) rather than out along the hand. Every other pose draws the arm as before.
- **The panel** is the conversation panel (`Talk`) every scene uses, its exchange keyed `pell_gail`, headed "Gail, on the phone". The
  replies that are said are said aloud (`Talk.reply`); "Say nothing" and giving the receiver back are not (`Talk.act`).
- **Turns.** The household keeps who has the receiver (`caller`), who has had a turn (`spoke`) and what each said (`toldGail`). One
  reader holds it at a time. June waits `OFFER_FOR` (900) ticks from when she last held it out; a reader keeps it until they give it back,
  go further than `REACH` (8) from the telephone or leave the house, or have held it `HOLD_FOR` (1200) ticks.
- **Sounds** are the game's own and the House's: the telephone bell the work print's (`work_print.ring`, "A telephone rings"), and the
  receiver lifted and put down the phone's pickup (`phone.pickup`), lower when it is put down. No new sound.
- **When the visit begins again** (`PellCall.restart`): the receiver back on its hook and nobody on the line.
- **For tests**, `PellCall.afterBreakfast` sets the house as breakfast leaves it without walking it: first light come and the line dry,
  Callum in and at the end of the hall, Tilly up in Nessa's room with her cane, the outside doors unlocked, June at the sink, washed up,
  and eight.

## Decided here

The proposal says "Gail calls to see how it went", that she has always just spoken to June, and that the older sibling answers her on
the panel. These are mine, and any of them can be moved:

- **The telephone is on the kitchen wall**, by the arch, with Gail's number taped beside it.
- **It rings once June has washed up**, and the morning waits for it.
- **June always has Gail first.** A reader who gets to it first is asked for June.
- **June's version is a little wrong**: the pair of them slept like logs whatever they did, and Callum has been good as gold all night
  on the line. If she met a reader in the night she tells Gail she saw them back to bed, which she did not.
- **Every reader at the house gets a turn**, one at a time, as both siblings would (the co-op Tilly slice will decide what Tilly's reader
  says).
- **The answers on offer** are fine, not sleeping, the two things a reader may have seen in the night (only if they saw them), not
  wanting to stay, and nothing. Gail has June's version for each.
- **June hears what is said in front of her**, and answers it to Gail and then to the room.
- **No marks yet.** Nothing on the first visit is framed, so nothing said to Gail counts against anybody; the record keeps what was said
  for the account and for later visits.

## The cases

`PellHouseTests.gailRingsAndEachReaderHasAWordWithHer`, with one reader in the kitchen (who saw June out and Callum on the line) and one
in the garage, checks:

- the telephone is on the kitchen wall, facing into the room, its receiver on the hook;
- it rings after the washing up, and June says it will be Gail;
- the reader in the kitchen lifts it first: Gail asks for June, and the reader keeps that they answered it;
- June takes it, the receiver at her ear, and the reader's panel is gone; a hand on June then gets her finger;
- June's side: sorry, the pair of them slept like logs, Callum good as gold;
- she holds it out ("She wants a word"); the reader in the garage is told; only the reader in the kitchen heard her side;
- the reader in the kitchen takes it: Gail has just been talking to June; what they saw is theirs to tell; it is out of June's hand;
- the other reader, come in, finds it busy;
- the first tells Gail June was out in the garden; Gail has June's version and asks for June back; they keep what they said, and give
  it back;
- June holds it out to the other ("Your turn"); the first has had their turn;
- the other takes it from June's hand, is offered nothing they did not see, says nothing, and Gail speaks to the silent line;
- June back on: she answers what was said in front of her, says goodbye and hangs up, the receiver out of her hand, and tells the room
  to keep inside the line;
- each reader keeps that Gail rang, that they spoke to her and what they said;
- hung up, a hand on the telephone says what it is;
- left, the house begins again with the receiver on its hook and nobody on the line, and what each reader said stays theirs.

`PellHouseTests.aReaderWhoWalksOffWithTheReceiverLeavesItUnsaid`, with one reader who spoke to June in the night, checks that June
tells Gail one of them was up; that the reader takes the receiver and goes out into the yard with it; that June has it back at once,
the panel gone and what they never said left unsaid; that with nothing said to her, June tells Gail they are shy, says goodbye and hangs
up; and that she has nothing to say to the room.

`PellHouseTests.nobodyComesToThePhoneAndJuneSpeaksForThem`, with one reader who stays in the garage, checks that they are told June is
calling; that nobody coming, June has it back and speaks for them ("They're a bit shy yet"); that she hangs up and goes back to the sink;
and that the reader keeps that Gail rang and not that they spoke to her.

## Found while building it

- **The receiver over her head.** A render of June's two gestures (local, from four sides) showed the straight-armed `PHONE` pose
  holding the receiver up in front of her face and above her head. The arm is now drawn bent for that pose, as above; a second render
  shows the hand at her cheek and the receiver from her ear to her mouth from the front, both sides and behind. `OFFER` was right as it
  was.

## Not yet

- What Gail hears after the second visit's framed incidents, and her file's marks (the second visit, "A week in").
- June on the telephone telling Gail what the older sibling never hears said, for a reader playing Tilly (the co-op Tilly slice).
- The account written from the record.
- Everything after.

## Numbers

No layout change. No Witness change. New: one `PellThing` kind (`PHONE`, two stages), one item (`pell_receiver`), four textures, two
gestures, a bent right arm in the cast model, five steps, three cases (thirteen in the Pell namespace), one test fixture.
