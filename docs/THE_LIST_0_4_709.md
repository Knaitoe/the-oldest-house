# The list, part 4a-2: the catch and the hay hook (0.4.709)

The second half of part 4a of *The list* (`docs/THE_LIST_PLAN.md`), after the hunt in 4a-1 (`docs/THE_LIST_0_4_708.md`). The plan's row
for 4a named the staged hold and breaking free, the hay hook and its finish, blood, wounds and stains, and bodies left and found. This
release builds the hold, breaking free, the hook's finish, the blood thrown off it and the body left where it falls. Wounds drawn on the
skin, stains on what the blood lands on, and the body found by others are 4a-3.

No night uses any of it yet; nights two to four (parts 5 to 7) will. `/ghosthouse test the_list hunt` now gives the operator the hay hook
with the body.

## The catch

A hand on one of the five, from the slasher, does what their state allows:

| They are | A hand on them |
| --- | --- |
| At their place, and do not see the slasher (behind them, or to the side) | Taken hold of. |
| At their place, and see the slasher | They get up and run, as in 4a-1. |
| Running | "%s twists out of your reach." Nothing else. |
| Hiding | Found, as in 4a-1: the first time they fight back. |
| Cornered (found again) | Taken hold of. |
| Held, with the hay hook in the slasher's main hand | The hook (below). |
| Dead | "%s doesn't move." |

A blow does the same as a hand for one cornered (taken hold of) or one held (the hook). Nobody is taken hold of while the slasher holds
someone else.

- **Creeping.** A slasher who is crouched is heard by the five only from a block away (it was six), and the body's tread is not heard at
  all. So a calm name can be come up on from behind.
- **Held.** They come up off their seat or the floor to stand in front of the slasher, 0.8 blocks off, the way the slasher faced when they
  took hold, and stay there whichever way the slasher turns. They shove at the slasher's chest with both hands in turn, the head wrenched
  away, a foot back to push off. They say their words, scream, and struggle (a subtitled sound, again every two seconds). Anyone else on
  the list near enough to hear the scream runs.
- **The slasher, holding,** cannot walk or jump, and cannot retreat (`LiteraryVignettes.retreatLocked`). They are told "You have hold of
  %s."
- **Breaking free.** Each has a strength: Deek 60, Mel 80, Wes 90, Lesley 100, Shannon 110. It counts up one a tick while the slasher
  looks at them (within 50 degrees) and two a tick while the slasher looks away. When it is reached they tear loose:
  - the slasher is shoved back a little, hears it, and is told "%s tears loose.";
  - they say their words (Lesley says nothing), scream, and run: one caught at their place runs their first way; one caught cornered runs
    back the way they came, their second way backward, to their first hiding place;
  - for a second they will not turn and fight the slasher who was holding them, though they still can once after that, as in 4a-1.
- **Let go another way.** If the slasher is put down, leaves the town, dies or logs off, they are let go of the same way, without the
  shove.

## The hay hook

Given with the body. It is the slasher's own, kept in their record: thrown down, it is back in their hands within a second; when the body
is taken back, so is the hook. Held as a tool is held. Its picture is a T-handled hook, worn wood across a dark iron shank that bends over
to a filed point; after it has been used, the same with blood down the bend and off the point.

With the hook in the main hand, a hand or a blow on the one held begins the finish, about five and a half seconds:

1. **The hook comes up** (one second). The slasher's arm swings; they see it, the head going back and the hands up in front of the face.
   Whatever they hold falls (Mel's camcorder), and lies where it lands, nobody's to pick up, until the hunt ends. Until the hook goes in,
   if the slasher is put down or leaves, they are let go of.
2. **In.** Under the collarbone. The slasher is told "The hook goes in under the collarbone." A heavy, wet sound and a caught breath
   (both subtitled), the scream cut short a quarter of a second later, and blood thrown out of them toward the slasher's face, up and
   out, falling where it falls. Anyone else in town within forty blocks who is not the slasher is told "Somewhere to the %s, a scream
   stops short."
3. **On the knees** (two and a half seconds). Drawn in close on it, a little nearer than they were held, down on their knees, held up
   straight by it, the arms hanging and the head fallen back, breathing a little. Blood drips down the front of them. At six tenths of a
   second the slasher is told "%s's knees give."
4. **Out.** The hook torn free (a subtitled sound), blood again, and the hook in hand bloodied, as it stays in the record. They slump
   forward over their knees. The slasher is told "You tear it free, and %s folds over."
5. **Fallen.** After most of a second they fall forward, face down, the head toward the slasher's feet, the arms loose and the head turned
   on the ground. The slasher is let go of and may walk and leave. A second later they are dead.

## The body left

- They lie where they fell for as long as the hunt lasts. They are not sent back to their place, have no heart in the slasher's ears, do
  not draw the slasher's look, and are not counted among those who see the bare face.
- A hand on them, anyone's, is told "%s doesn't move."
- The slasher's record keeps who they killed (`Kills`, a bit for each).
- Ended (`hunt off`), they are stood up and back at their place like the rest. Part 5 keeps the dead dead from one night to the next.

## Blood

Its own particle (`the_list:list_blood`), four drawings of a drop at sixteen pixels: a bead, a fatter bead, a drawn-out string, a splash
breaking up. Each is thrown, falls under more than ordinary weight, flattens where it lands and stays there a moment before it fades. They
are sent to everyone near, so a second reader nearby sees the same blood.

## Single and multiplayer

- **Alone**, the reader has the body and the hook, and everything above is theirs.
- **With others**, only a reader with the body takes hold of anyone, and only one at a time each. Two slashers may each hold one.
  - Everyone near hears the scream, the struggle, the hook going in and coming out, and sees the blood. Everyone in town within forty
    blocks who is not the slasher is told from which way a scream stopped short.
  - Another reader's hand on one of the five who is running, hiding, cornered or held does nothing; on the dead, they are told they do
    not move. A calm name still talks to them as before.
  - The lock on retreat is the holder's alone.
  - Each reader's cues are their own.
- **Spectators** touch nothing, are told nothing, and are never held from.

## Retreat

Locked for the slasher from taking hold until the one held falls, or until they are let go of. Nobody else's retreat changes.

## Made as

- **`TheListHunt`**: the states `HELD`, `FINISH` and `DEAD` join `CALM`, `RUN`, `HIDE` and `COWER`; gestures `STRUGGLING`, `RECOIL`,
  `KNEELING`, `SLUMPED` and `FALLEN`.
  - `hand` (a hand on one of the five, from `TheListPeople.talk`), `take`, `held`, `loose`, `finish`, `finishing`, `spray`, `drip`.
  - `STRENGTH`, `LOOK_AWAY` (50 degrees), `HELD_AT` (0.8 blocks) and `KNELT_AT` (0.65); the finish's ticks `RAISE` 20, `KNEES` 32, `OUT`
    70, `FOLD` 86, `DONE` 110; `HEARS_CROUCHED` 1.
  - `BACK`: each one's way back, their second way run backward to their first hiding place (a door on it opened as they come to it).
  - `holding(player)`, `gone(entity)`, `dead`, `sees`, `dropped`; `drop` and `sweep` (what was let fall, tagged `the_list_dropped`,
    taken up at the start and end of a hunt).
  - A runner just torn loose has `since` set to minus a second, and fights back on being caught up with only from nought.
  - Who used the hook (`killer`) is kept past the fall, when the holder lets go, so the death is recorded as theirs.
- **`TheListBody`**: the hook (`giveHook`, `hookInHand`, `bloodied`, `bloody`, the record's `Hook`: 1 clean, 2 used; given back every second
  if missing; a thrown hook does not leave), `killed` and `kills`, `holding` (speed and jump to nothing while holding). The dead are
  skipped by hearts, the look and the pull; a crouched reader's tread is silent.
- **`TheListPeople`**: `talk` asks the hunt first; Mel holds the camcorder through a hunt until she lets it fall. **`LiteraryVignettes.retreatLocked`** asks `TheListHunt.holding`.
- **`TheListRegistry`**: the item `list_hay_hook`, the particle `list_blood`, four sounds.
- **Client**: `TheListBloodParticles` (the drop); `LiteraryRenderers.hunted` draws the five new gestures and lays the fallen face down.
- **Art** (`tools/draw_the_list_art.py`): the hook clean and bloodied, its two item models (the bloodied one by custom model data 1), the
  four drops and the particle's description.
- **Sound** (`tools/generate_the_list_audio.py`): a struggle (shoes scuffing and skidding, cloth wrenched, breath forced out in short bursts;
  loops), the hook going in (a dull heavy punch through cloth, a short wet give, the iron grating once), coming out (a wet, sucking tear
  that rises and lets go, the cloth ripping), and a gasp (a voiced catch, cut off).

## Tests

`TheListTests.theCatch` (new, batch `the_list_catch`), with a second reader:

- The body and the hook are given, and the hook is in hand.
- Shannon, on the bus bench, does not see the slasher crouched behind her. A hand takes hold of her: held, up off the bench, struggling,
  within a block in front of the slasher; the slasher told so; her words; retreat locked and the body standing still; the second reader
  hears the scream and the struggle and is held to nothing.
- Looked at for two seconds, she is still held. Looked away from, she tears loose: her words, the slasher told, let go (walking, free to
  leave), running her first way; a hand on her running gets only the twist; half a second later she has not fought.
- Mel is found under the north end of the stands and fights back, hides at the south end, and found again cowers. A hand takes hold of her
  with her words; a second, with the hook in hand, begins the finish and she recoils.
- After the hook comes up: kneeling; the cue and the sound; blood sent to the slasher; the second reader told a scream stopped short;
  retreat still locked.
- The camcorder falls out of her hand as the hook comes up, lies on the ground, and is not put back in her hand.
- Dead: the knees and the out cues and the sound; fallen face down, on the ground, not seated; let go; the hook bloodied in hand and in
  the record; Mel in the slasher's record. Both readers' hands get that she does not move. For two seconds no heart beats where she lies,
  and she stays there.
- Ended, she is back at her place, seated and still, the camcorder is taken up, and with the body taken back no hook is left.

## Seen in the frames

Seventeen frames at eye level: Shannon held at the bus bench from the slasher's own eyes, the side and behind; Mel held under the stands,
then at the hook's raise, on her knees, slumped and fallen, each from the slasher's eyes and from the side, front or behind.

- Held, Mel holds the camcorder up at the slasher while she shoves, which goes with her words. But with both hands up at the hook's raise
  it read as filming. She lets it fall there now.
- On her knees she is held upright with the head back; slumped, folded over the knees; fallen, she lies flat on the ground, face down,
  her head toward where the slasher stood.

## Every new line

Speech, over the speaker's head as their name. Taken hold of:

- Deek: "Everybody knows I came out here!"
- Wes: "Deek! Deek, it's got me!"
- Mel: "There's tape of you. There's tape!"
- Lesley: "My mom's waiting up. Please."
- Shannon: "Somebody! Please, somebody!"

Tearing loose:

- Deek: "Too slow, freak."
- Wes: "I'm out, I'm out!"
- Mel: "Run! Everybody run!"
- Shannon: "Jenna!"
- Lesley: none.

Grey cues on the action bar, to the slasher:

- "%s twists out of your reach." (a name)
- "You have hold of %s."
- "%s tears loose."
- "The hook goes in under the collarbone."
- "%s's knees give."
- "You tear it free, and %s folds over."

To anyone's hand on the dead: "%s doesn't move."

To every reader in town within forty blocks but the slasher: "Somewhere to the %s, a scream stops short." (a compass direction)

Subtitles: "Someone struggles", "A hook goes in", "A hook tears free", "Someone gasps".

Item name: "Hay Hook".

For operators, changed: "The hunt is on: the five are at their places and run when they see you, and the hay hook is in your hands.
/ghosthouse test the_list hunt off ends it."
