# The Pell house 17: midnight in the pool (0.4.539)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its first
two visits whole, and its third, the last night, begins: the line can be broken and Callum moved off it with food
(`docs/PELL_HOUSE_LAST_NIGHT_0_4_538.md`). This is its seventeenth slice: midnight in the pool, and getting Tilly out. From the
proposal's "3. The last night":

> - **At midnight** June takes Tilly into the water. To a reader playing Tilly, this is warm and calm. June talks softly and calls her
>   Nessa.

and from "The last night, in play":

> - **Tilly in the water.**
>   - **The calls:** June calls her Nessa, softly, again and again.
>   - **A character Tilly:** holds out for a fixed time, shorter if she answered to "Nessa" on the earlier visits.
>   - **No drowning:** if breath runs out, June lets her up into the air and asks again, and a dazed reader is tempted to answer.
>     Nobody drowns. The failure is the name, not the water.
> - **Getting her out.**
>   - **Getting to her:** the older sibling has to get into the pool and take Tilly's hand.
>   - **The way out:** guide her out of the water and across the yard to the gap, by sound, since she can't see it.
>   - **Guiding by sound:** ... A character Tilly follows the reader's hand.
>   - **June:** stays in the water calling the name. She does not chase.

The freezer comes next, then the endings (Gail's car at dawn, the account, the keepsake). A reader playing Tilly comes with co-op. The
House still does not deal the house.

## Midnight

- **June comes out of her room** in her robe when the last night's hour comes to midnight, goes to Nessa's room and stands by the bed.
- **She wakes Tilly by Nessa's name:** "Nessa. Up you get, love. Come with Mum." Tilly, getting up: "I'm Tilly." June: "Course you are.
  Give me your hand." Tilly's cane stays by the bed; she is led.
- **Down to the water.** June leads her by the hand along the hall to the sliding door, unlocks it and leaves it open, out to the pool's
  gate, unlocks its padlock and leaves it open, and round to the chair, where the robe comes off and is dropped over it. Along the
  shallow end's edge, and in.
- **In the water** June stands behind Tilly in the middle of the shallow end with her hands on the girl's shoulders, both facing the deep
  end. The shallow end is deeper than either of them is tall, so June stands with the water at her waist and Tilly with it at her chest,
  as if on a step.
- **The splashing wakes** anybody asleep on the camp bed: "Out the back, water splashes, and June is saying a name over and over." They
  keep that it woke them.

## The calls

- **June calls her Nessa,** softly, again and again: "Nessa.", "Nessa, love. It's Mum.", "Say yes for Mum, Nessa.", and so on. A reader on
  the lot too far off to hear her is told what she called.
- **Tilly answers with her own name,** fainter the longer it goes on: "I'm Tilly." and "My name's Tilly.", then "Tilly.", then "Tilly...",
  and near the end nothing.
- **Under.** Every so often June presses her under for a few seconds (bubbles at the surface), and lets her up into the air with a splash
  and a cough, and asks again: "Nessa?" It wakes anybody asleep again.
- **Holding out.** Three minutes in June's hands, and Tilly answers: "Yes, Mum." June: "There's my girl. There she is." Every reader awake
  near enough to hear her (as near as a reader's journal keeps what is said) hears her, and keeps it. June takes her out of the water and back in, putting her robe on again at the chair on the way,
  lays her down in Nessa's bed and sits on its edge beside her for the rest of the night.

## A hand

- **Taking her hand.** A reader in reach of her in the water (in it, or right at its edge) puts a hand on her: "You take Tilly's hand under
  the water. She holds on." Tilly: "Is that you?" If she was under, she comes up with the hand. June's hands come away; she stays where she
  is, turns to watch the girl go and goes on calling the name ("Nessa, come back to Mum.", "Where are you going, Nessa?"). She does not
  chase, and Tilly no longer answers her. On land, any hand that reaches her takes hers ("You take Tilly's hand. She holds on.").
  A hand that has had hers before tonight takes it again ("You take her hand again. She holds on."), and she does not ask; to a hand
  new to her, she asks.
- **Following.** Tilly follows the hand: through the water at a slow wade, keeping to the shallow end; up out of it just in front of a
  reader standing at its edge, if there is room to stand there; and across the yard at a careful walk, a step behind them.
- **Never over the paint.** She will not walk into the pool, nor onto the line where it is unbroken: her bare foot finds the paint and she
  stops short ("Tilly stops short. Her bare foot has found the paint, and she will not step on it."). With the hand further along she goes
  along the paint toward it. Only a cell scrubbed out where the line is broken (two side by side) takes her weight.
- **Let go.** A reader who goes more than a few steps off lets go of her ("Her hand slips out of yours. She stands where she is."), and she
  stands until a hand takes hers again. Let go in the water, in a few seconds June wades to her and takes her back into her hands, and
  the first thing she says, as soon as she can, is "There you are. Come here." The holding out goes on where it left off.
- **One hand at a time.** A second reader's hand finds she has somebody's already.

## Out

- **Through the gap.** On a cell of the line scrubbed out where it is broken, or anywhere outside the line, with the hand still hers,
  Tilly is out: "Tilly steps through the gap with you. She is outside the line." The reader who brought her keeps it; anybody else near
  keeps that they saw it.
- **June stops calling.** Readers on the lot are told so. She stands in the water with her arms down.
- What comes after (dawn, Gail's car, the account) is the endings slice.

## What each reader keeps

- `WokenByThePool`, `SawRite` (saw her in June's hands in the water), `HeardTillyAnswer`;
- `TookTillysHand`, `GotTillyOut`, `SawTillyOut`.

## How it is made

- **`labyrinth.PellRite`**: the rite, from midnight on the last night (`tick`, from `PellNight`'s night once the house is locked up):
  June's walk to Nessa's room, the waking, the walk down with Tilly a step behind on June's trail, into the water, the calls and Tilly's
  answers, under and up, the holding out and the answer, back to bed; a hand on Tilly (`hand`) and on June (`june`), from `PellPeople.talk`;
  Tilly after a reader's hand in the water and on land (`led`), the line under her feet (`onPaint`, `outside`), out; `restart` with the
  visit; and, for tests, `inWater` and `nearlyAnswers`.
- `PellPeople`: gestures `HOLD`, `PRESS`, `WADE` (June in the water, in her swimming things), `LEADS` and `LEADS_SWIM` (June leading Tilly by
  the hand, in her robe and in her swimming things) and `LED` (Tilly with her hand held out in someone's); the rite's state on the house.
- `PellNight`: on the last night, the night hands over to `PellRite`.
- Client: `LiteraryRenderers.pell` draws the six gestures.

## Decided here (any of it can move)

- Midnight on the last night is when June goes; she wakes Tilly by the bed, and Tilly gives her own name.
- June leaves the sliding door and the gate open behind her, going down and coming back.
- June stands Tilly with the water at her chest and herself with it at her waist, as if on a step, the shallow end being deeper than
  either is tall.
- Three minutes in June's hands to the answer. Nothing on the earlier visits has a character Tilly answer to Nessa, so nothing shortens it.
- Under every sixteen seconds for two and a half; June asks again as soon as she has her up.
- A reader takes her hand within two and a half blocks of her in the water; a hand more than five blocks off lets her go; June takes her
  back after five seconds if she is let go in the water, never on land.
- Once a hand has taken hers, Tilly no longer answers June, whoever has her hand after; only June's own hands bring the holding out back.
- Out means through the line: on a scrubbed-out cell where it is broken, or beyond it. Behind the shed the line runs along the back fence,
  so a girl brought through the thin stretch stands in the gap with her back to the fence.
- Answered, June takes her back to Nessa's bed and sits beside her for the rest of the night; what that means for the morning is the
  endings slice.

## Checked

- `PellHouseTests.atMidnightJuneTakesTillyIntoThePoolAndCallsHerNessaUntilSheAnswers`: two readers, one asleep on the camp bed and one at
  the pool's fence; June out of her room at midnight in her robe; the waking, by Nessa's name and Tilly's; Tilly up without her cane and
  June leading her; the sliding door and the gate unlocked and left open, the robe over the chair; June in the shallow end holding her and
  Tilly in front of her; the reader asleep woken and told; the reader at the fence seeing it; June's calls and Tilly's own name back; under,
  June pressing, Tilly below the water; up, and June asking again; the woken reader come out to the deck; the answer, heard by both
  readers; back to Nessa's bed, Tilly lying in it and June in her robe sat beside her, the robe off the chair.
- `PellHouseTests.aHandInTheWaterTakesTillyAndSheFollowsItThroughTheGapButNeverOverThePaint`: two readers, a gap scrubbed on the line's
  west side; the first hand takes hers (she holds on, asks, June's hands come away), the second finds she has somebody's; the first goes
  off and lets go of her, and June wades to her, takes her back and says so as soon as she can; the second takes her hand (new to her, she
  asks again), leads her through the water, she is lifted out at the edge and follows across the deck; let go there, she stands, and June
  does not come for her; the same hand again (she does not ask); she stops short of the whole line with the hand beyond it, goes along
  the paint to the gap and through it; out, kept by the reader who brought her, June stopped calling and the other reader told.
- The slice 16 case's last check now finds June gone for Tilly at midnight instead of in her room.
