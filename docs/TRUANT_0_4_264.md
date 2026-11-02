# Johnny Truant, first slice: a useful stranger (0.4.264)

The owner's design draft is recorded word for word in docs/TRUANT_RESIDENT_DESIGN_DRAFT.md. This slice builds the beginning of it:
his places, the helpful meetings that come before twenty passes, and the half-hidden face. Nothing in it is hostile. The claimant and the
collector (the invented debt, dangerous favours, the hand in the doorway, the covered light, the shove) come in a later slice, once the
owner has met him.

## His places

There are three ordinary halls of his own, built whole, dark and cramped. They are dealt among the halls like any other.

1. **The held door.** A low service passage, three wide, with brick and pipes. Halfway along, a heavy door on a closer swings shut by
   itself a moment after it is opened, and a reader caught in the doorway is pushed back the way they came. Past it, on the right, a
   recess holds his lamp. When he is here he holds the door until everyone near it is through.
2. **The crossing.** A shaft with a one-block ledge along its wall and no rail. The drop is to a lower landing: it is survivable, it
   hurts, and a ladder leads back up. When he is here he waits at the near end. As a reader steps onto the ledge he puts a hand between
   their shoulders and walks behind them. While his hand is there, a slip off the edge is caught: he pulls them back onto the ledge.
3. **The lamp.** A low plastered corridor, two high, that turns a corner and runs on past a shallow alcove with his lamp on a crate.
   This is where his face is seen half round the corner, and where he talks.

There is one Truant. He stands in at most one of his places at a time, and everyone sees the same man there. When a reader comes into
another of his places while nobody is with him, he is there instead: he moves only while no reader is in the place he leaves. If he
is with someone elsewhere, his place has his lamp lit and his things in it, and nobody.

## The meetings (before twenty passes)

He is useful and recognisably human. He helps first and asks afterwards.

- **The help is real:** he holds the door, steadies the crossing and catches a slip, and makes room at his lamp ("Stay a minute.
  Listen before you go.").
- **Then he asks, in the conversation panel:** "What was behind you?" / "What did you find?" The reader can:
  - tell him about the last story they were in, by its place's name, and he asks one specific thing after it;
  - say they would rather not, and he accepts it;
  - ask who he is. He gives "Johnny" and no more, hesitating over it.
- **At the end he may ask for a promise:** "When you get through, tell me what was in there." The reader agrees, qualifies it
  ("maybe"), or refuses. What they said is kept as said.
- **At later meetings** he remembers what that reader actually told him and what they actually promised:
  - "You said you'd tell me." only when they did say it;
  - nothing claimed when they refused.

  A kept promise is recorded as kept.
- **The half face:** at the lamp, the first sight of him can be one eye, part of the nose and half a mouth around the jamb, with the
  rest of him behind the wall. It stays until the reader has looked at it, then slowly draws back. Looking does not freeze or remove
  him. Round the corner he is in the recess, sideways against the wall, and then at his lamp. The face is the leer: a tight smile, one
  side up. Stepping out to talk, he has his ordinary tired face.

## What is kept

**For each reader** (truant_0264):
- meetings;
- each kind of help he gave (door, crossing, catch, lamp);
- the places they told him about;
- the promise as they made it, and whether it was kept;
- how many times they saw the half face;
- the deepest they had been at each meeting.

Speech is over his head and in the panel, never in chat.

**Shared:** where he is, and his one body.

## Twenty passes

"Passes" is the deepest the reader has ever been (the House's saved maximum, which backtracking does not lower), so the relationship
does not forget its progress. At twenty the record notes it. In this slice nothing hostile follows from it.

For a later slice: an early reader standing with a veteran gets nothing lethal from him before twenty.

## Defaults for the draft's open decisions (for the owner to change)

- **The addiction:** accounts of the House only, for now. His tiredness shows in his face; no substances.
- **His most serious demand:** not in this slice.
- **Mortality:** not in this slice. Struck, he flinches and withdraws from that meeting; he cannot die.
- **His understanding of the debt:** not in this slice. The record keeps three things apart, ready for it: what he did, what was said,
  and nothing claimed yet.
- **Identity:** he gives "Johnny" and no surname. His papers are signed J.T. The House never says more.
- **Frequency:** each of his places can be dealt from the sixth crossing (where stories begin; the opening stays domestic), at weight 3, and not within six rooms of the reader's last
  meeting with him.

## Look

- **His skin:** tired skin, a worn expression, small asymmetries, ink on his hands, and a worn jacket. It is painted on the common
  painter at native pixel scale (stranger phase 73). The leer is the same face with the tight smile and the narrowed eye on the
  smiling side (phase 74).
- **His poses:** holding a door, a hand forward at shoulder height (steadying), lifting his lamp, and the lean round a corner.

## How it is built

- **The places** (`TruantPlaces`): `TRUANT_DOOR`, `TRUANT_LEDGE` and `TRUANT_LAMP`, slots 170 to 172, ordinary gray halls built whole;
  layout 87. They join the gray pool at the sixth crossing (`TruantPlaces.weight`).
- **The man** (`Truant`): one stranger on phase 73 (his face) or 74 (the leer), saved by his body's id, with the place he is in. Each
  reader's record is under `truant_0264`. What he is doing at a place is not saved: a reader arriving finds him starting over.
  - **At the door** he stands beside the doorway on its far side. He opens the door and holds it while anyone on the near side within
    about five blocks, or in the doorway, has yet to go through, and lets it go 30 ticks after the last of them. The closer shuts it 14
    ticks after anyone opens it when he is not holding it, and shoves anyone in the doorway back to the side they were last wholly on.
  - **At the crossing** he waits by the near end. He puts his hand on the first reader to step onto the ledge and walks a block behind
    them. A slip from the ledge into the shaft is caught back to the last place they stood on the ledge. At the far landing he says
    "There." and stands by them. He goes back over only when someone is waiting at the near end and nobody is with him.
  - **At the lamp** the reader's look has to rest on the edge of his face for 20 ticks, or the reader has to come within two and a
    half blocks. Then he draws back behind the corner over about two seconds, waits against the wall, and goes to his lamp.
  - **The panel** opens by itself once he has helped and asked and the reader has stood within three blocks of him for a second.
    Right-clicking him opens it too. There is one talk a meeting. A new meeting is a reader with him after more than 30 seconds apart.
  - **Struck**, he flinches (gesture 14), and holds back from that reader for the rest of the meeting. He takes no damage.
- **His words** (`TruantTexts`): the lines he says while helping, the panel's openings, his one question after a place (the same one
  for the same place), the promise, and what he says back.
- **His look**: `literary_truant` and `literary_truant_leer`, painted on the common painter at native pixel scale (catalog phases 73
  and 74). His gestures in `LiteraryRenderers.CastModel.truant` are 10 (holding a door), 11 (a hand forward), 12 (his lamp lifted, a
  lantern in his hand), 13 (the lean round a corner) and 14 (flinching).

## Tests

`TruantTests.johnnyHelpsAtHisThreePlacesAndRemembersWhatEachReaderSaid` (exploration, batch truant) plays all three places with two
readers:
- the half face, looked at, drawing back to the lamp;
- the help at the lamp and the panel, his name, a story told, the promise made as "maybe";
- one body, moving to the door only once the lamp is empty;
- the door held and let go with his question;
- a slip caught on the ledge, and "There." at the far end;
- the closer shoving a reader back while he is at the crossing;
- a later meeting that remembers the "maybe" and the place, counts the promise kept, and notes twenty passes;
- being struck, after which he will not talk and does not die;
- the other reader's record staying their own.

The domestic-pacing case keeps the opening crossings to ordinary halls, which is why his places start where stories do. It passes
alongside Truant's case. The place-wide hall cases also pass with his places in: slots, the gray maze, the dry spell, the scent, the
seven stone passes, and the shared scenes' ways on.

## Not in this slice

- the claimant and the collector;
- the dangerous favours;
- the hand in the doorway, the covered light and the shove;
- help after harm;
- his papers retelling a reader's account in the first person;
- the multiplayer gate on lethal encounters.
