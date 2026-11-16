# Second Person, E2: their name on the post (0.4.634)

The proposal's second ending (docs/SECOND_PERSON_PROPOSAL.md, section 5, ledger row 24), reached from the spring's form on the
mat. New builds only. No layout, protocol or Witness change: 90 sources / 68 required / two kinds / three endings. The ending
resolves Second Person like E1, with its own outcome (`their_name_on_the_post`).

## What happens

1. **Signing.** While the form lies on the mat in the spring (`S_POST`, before the resident has picked up the post), its panel has a
   fourth reply, "Sign it." A form already read can still be signed. Signing marks `Application` = `signed`, turns the form to its
   signed face (stage 2), gives the reader a moment of Darkness and moves the year to `SecondPersonSigned.S_SIGNED`. The resident is
   gone from the flat: the reader is the one who lives there now.
2. **The last day** (sub 0). One line opens it; the clock is afternoon.
3. **The knock** (sub 1). Somebody knocks at the flat door. Sam stands on the landing. A reader within four blocks of the door gets
   Sam's panel: three lines spoken as to the tenant, and two replies, "Come in." or "Not today." (`SamDoor` = `in` or `out`). A
   reader who lets the knock go (900 ticks) hears whoever it was go back down the stairs (`missed`). Sam leaves either way.
4. **The phone** (sub 2). The fourth call rings for the reader (`Ringing` = 4), by the House's read of home (HAVEN: the parent;
   otherwise the friend). The reader can listen, put it down, or leave it off the hook; a call left ringing past the third ring is
   `unanswered`. The call is saved as `Call4`.
5. **Dark** (sub 3). The street window goes black. A reader who stands at it sees, in the glass, someone standing behind them where
   the reader used to stand: the resident, at the hall (`BEHIND`, gesture reach). They ask the room twice, "...Hello?" and "Is
   somebody in my flat?". A hand on them makes them shiver (snowflakes) and they do not turn round (`TouchedBehind`). They are gone
   after ten seconds. A reader who never goes to the window reaches the next step at evening anyway.
6. **The latch** (sub 4). The front door is on the latch. Going out by it and down to the street readies the account
   (`LiteraryVignettes.ready`, outcome `their_name_on_the_post`); the book waits on the bench at the bus stop.

The account replaces the last morning's paragraph with its own, from the record only. The book on the shelf has a page for each
step.

## Art

- The form's signed face: `sp_fitting` kind `application` stage 2 (`form_signed`): the answers and a signature on the line at the
  foot, in the same ink as the boxes. Drawn by tools/draw_second_person_art.py; looked at enlarged (the entries and the signature
  read at sixteen pixels).
- No new skins: Sam and the resident are the cast already painted.

## New lines (all new in this slice)

Form panel reply: "Sign it."

Signing: "You write your name on the line at the foot. It comes out the way it always does." / "The ink is still wet when the room
changes."

Cues: "The rest of the year goes by. Then a last day of it, and the flat is yours." / "Somebody knocks at the flat door." (as
before) / "Whoever it was goes back down the stairs." / "It gets dark. The street window is black." / "In the black glass, behind
you, someone stands where you used to stand." / "The front door is on the latch." / "On the bench at the bus stop, where nobody
waits, there is a book."

Sam: "There you are." / "You look well. You look like you've slept." / "I can't stop. I just wanted to see you in it. The place." /
"Just for a minute, then. Look at you." / "Next time, then. Don't be a stranger." Replies: "Come in." / "Not today."

The resident, behind the reader: "...Hello?" / "Is somebody in my flat?" / "..."

The fourth call, HAVEN: "Hello, love. You sound different. Good different." / "Is it nice there now? Is it yours now?" / "I'll let
you go. Night night." Listened to: "You don't have to say anything. I can hear you're all right." / "Night, love."

The fourth call, otherwise: "You picked up. You actually picked up." / "You sound like you've slept, for once." / "Don't be a
stranger. You know where I am." Listened to: "I'll take that as a yes." / "Night."

The book: "Somebody is at the flat door." / "Open it." / "Or let them knock." / "The telephone. It is for you." / "Lift it, and
listen." / "Or let it ring." / "It is dark. The street window is black." / "Stand at it." / "The front door is on the latch." / "Go
out by it and down to the street."

Pages turned: "You signed the form on the mat." / "Sam came to the door and talked to you as if you lived there. You asked them
in." / "...You said not today." / "In the black street window you saw them behind you, where you used to stand."

The account: "You signed the form. For the last day of the year you lived there." / "Sam came to the door and talked to you as if
you always had, and you asked them in." (or "...and you said not today.", or "Somebody knocked, and went back down the stairs.") /
"The phone rang for you, and you listened." (or "...and you put it down.", "...and you left it off the hook.", "...and you let
it.") / "At dark, in the black window, they stood behind you where you used to stand, and you put a hand on them, and they
shivered, and did not turn round." (or "...and you could not make them hear.") / "The year was ordinary, and you had it." / "The
door was on the latch. You went out by it."

## Tests

`SecondPersonTests.theirNameOnThePost` (batch `second_signed`): a year through to the spring form, signed; Sam answered, the call
listened to, the reflection seen and touched, out by the latch; checks the outcome, the readied account and its lines. The Second
Person namespace is 6/6.
