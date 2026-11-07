# Sitting down and getting up in the Barrett home (0.4.324)

The owner asked for a better sitting and standing animation for the NPCs, particularly in the Ghosts Set, then that they use their arms as they get up, and that the standing animations vary subtly.

## What was wrong

Getting up from a seat used to be a walk at the seat's height: the standing body slid sideways off the chair in the air and then stepped down to the floor. Sitting down was the reverse: a walk up to seat height beside the chair and a glide across onto it. The pose changed in one eight-tick blend, so the legs snapped from straight to folded while the body floated.

## Now

**One move each way, carried by the client.** On the server, getting up is a single move from the seat to the floor beside it (`GhostsMotion.rise`), made in the tick the pose changes; sitting down is a single move from the spot beside the chair onto the seat, facing the table (`GhostsMotion.sit`). The errands between takes (Sarah's plate, John's mug, Barry's cable) and the day-two grace use them: Sarah and John now walk to the floor beside their chairs and sit, instead of walking up into the air. The renderer (`GhostsCastModel.Renderer`) carries the drawn body from where it was last drawn to where the server has it, and turns it to its new facing, over the time the move takes (`SIT_TICKS` 22, `RISE_TICKS` 18). No stride plays over the move.

**The body bends.** The seat in these rooms is as high as a standing hip, so the body perches. While the feet are down the knees give and the hips lower just as far as keeps the feet on the floor, with one short step as the body comes round to the seat. The legs keep their own time: sitting down, they come up only once the seat has the weight; getting up, they come down to the floor first, before the push. The torso leans about the hips and the head stays level.

**The arms work.** Getting up, the hands come off the table onto the thighs, elbows bent, then press down, the elbows straightening as the hips lift, and let go once the body is over its feet. Sitting down, the hands go back to the seat either side as it takes the weight.

**Each person differently, and each time a little differently.**

| Person | Getting up and sitting down |
| --- | --- |
| John | The deepest lean, both hands flat on his thighs and a hard push; he drops onto a seat more than he lowers onto it. |
| Sarah | More upright; one hand does the work on the table's edge or her knee, the other barely leaves her lap. |
| Merry | Little lean; her hands go either side onto the seat's edge and she slides off it, or climbs back onto it. |
| Marjorie | Slow; hands on her knees, the press late and long. |
| Barry | Pushes off the arms of his director's chair, hands wide. |

On top of that, every rise or sit has its own small differences, drawn once when it starts: how far forward the lean goes, which hand leads and how far the other trails, which foot steps, the shape of the bend's timing and a slight turn of the head.

## Scope and proof

This changes the Ghosts Set cast only; the other vignettes' seated actors are unchanged. No new actor, layout, payload or protocol change; saved family bodies are reused as before.

The Ghosts filmstrip (`gradle runClient -PghostsFilmstrip`) has a new page, `ghosts-film-seat.png`: seven rows of eight frames across the move, drawn at the height and offset where the renderer carries the body, with the seat as a bar. It was inspected at each change: feet stay down through the lean, the legs come up only at the seat, the hands press on the way up, and the five people read differently. All 37 Ghosts native cases pass locally. Exact-commit CI pending.
