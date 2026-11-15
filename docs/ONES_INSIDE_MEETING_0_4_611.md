# The Ones Already Inside 11: the two men in one room (0.4.611)

The plan's eleventh slice (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): "N10 and N11: the confrontation's skeleton with its substitutions;
the first attempt." The House still does not deal the house.

New builds only. No layout, protocol or Witness change. The logic is `LodgingMeeting`; three new poses (RAISE, GRAB, PALMS).

## Putting them in one room

- **Daniel free.** Once he is in the parlor (and Rae, if she went to its door, has stood there) for ten seconds of the house's clock,
  Rae goes in, then Dan. Daniel steps back from the door toward the windows; Dan stands just inside it, Rae by the door between them.
- **Daniel held.** A reader whose Dan has asked to be shown (N6) has "Come and see him." with Dan. Dan: "Show me." He walks to Daniel's
  room, Rae after him, and stands before the frame. Vera's pin waits while they are there.
- **One of them dead.** Not yet possible; nothing comes back to make the scene (a later slice).

## The script

Every seventy ticks, aloud to anybody within earshot:

> Daniel: She said a week.
> Dan: She told me you were gone.
> Daniel: I wasn't.
> Rae: He didn't know.
> Daniel: Then why did you?

Then, to readers near: "Daniel is looking at Rae. Nobody else is saying anything." A hand on Rae, Dan or Daniel gives that reader's
replies. Each telling is said once in the house; a promise belongs to the reader who made it.

| The reader | Only for | They answer |
| --- | --- | --- |
| "She knew he was alive. She told me." | a reader who heard Rae's confession (N7) | Dan: "You knew." Rae: "I did it so you'd come." |
| "Vera says only one of you comes through it." | a reader who heard Vera's truth (N8) | Dan: "Which one?" Daniel: "You know which." |
| "There's a third way. In the cellar. It undoes all of it." | a reader who saw the dead post's body (N9) | Dan: "All of it. Me as well?" Nobody answers him. |
| "I said I'd stay with you. I'm staying." | a reader who promised Rae to stay with Dan | Dan: "All right." |
| "I said I'd get you out. I meant it." | a reader who promised Daniel | Daniel: "Then don't let her near me." |
| "Let her answer." | anybody, once | Rae: "Because if he'd known, he'd never have come." "And I couldn't lose him twice." |

## The first attempt

Twenty-five seconds after the last line, or six after Rae answers, one of them acts, in the plan's order:

1. **Vera**, if what she said was told aloud: "Excuse me." She comes down the stair and through the back passage to the frame. Landed: "Bring him to me."
2. **Daniel**, armed, free, and with nobody having promised him aloud: "Hold still." He goes at Dan with it raised. Landed: he stands over Dan with it raised and does not bring it down.
3. **Dan**, told (what Rae did, or the cellar) and stood by: "Give me a minute. Nobody decides this for me." (the script's line), his hands up.
4. **Rae**, if nobody told what she did: "Come on. She's upstairs. She can see to it." She takes Dan by the arm toward the stair. Landed at its foot: "Vera! We're coming up."
5. Otherwise **Dan**, cornered: "Vera! Vera, he's down here!" He takes hold of Daniel by both arms. Landed: "Vera!"

Every attempt but Dan's minute begins aloud and takes time; a hand on the one acting before it is done offers "Stop.", and they stop
where they are: Rae "Let go. Let go of me.", Daniel "He's wearing my hands.", Dan "Get off me. Get off.", Vera "You're making this worse
for him." What follows a landed or stopped attempt is N12's.

## Lines new here (for the owner)

The five lines of the scene and Dan's minute are the script's; "You knew." and "I did it so you'd come." are 0.4.608's. Everything else
is new: MEET_ASKED, R_COME_SEE, DAN_SHOW_ME, the six reply labels, DAN_WHICH, DANIEL_YOU_KNOW, DAN_ME_AS_WELL, NOBODY_ANSWERS,
DAN_ALL_RIGHT, DANIEL_NOT_NEAR, RAE_WHY, R_STOP and the attempts' other lines (ATTEMPT_START, ATTEMPT_STOPPED, ATTEMPT_LANDED,
ATTEMPT_SEEN), all in `LodgingTexts`.

## Records

The house's: Meeting0611 (stage), MeetWhere0611, MeetAt0611, MeetLine0611, MeetToldRae0611, MeetToldVera0611, MeetToldCellar0611,
MeetSupported0611, MeetBacked0611, MeetRaeAnswered0611, Attempt0611, AttemptAt0611, AttemptStopped0611, AttemptLanded0611. Each reader's:
BroughtDan0611, MeetSpoke0611, KeptDan0611, KeptDaniel0611, StoppedIt0611.

## Cases

Three, each its own batch: `theTwoMenMeetInTheParlorAndDanAsksForAMinute` (the order of the attempts over every case; Rae and Dan into the
parlor and the door shut behind them; the replies only for who heard; the cellar told once; a promise kept; "Let her answer."; Dan's
minute), `aReaderTakesDanToTheFrameAndStopsRaeTakingHimUp` (brought to the frame, Vera's pin waiting, Rae taking him and stopped where
she was) and `toldWhatVeraSaidSheGoesForTheFrame`.
