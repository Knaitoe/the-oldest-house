# Borrasca: riding the bikes (0.4.435)

The owner: "Let players ride bikes? Maintaining balance and pedaling left/right should be a function."

Patrick's bike and Kendra's (0.4.431) can be ridden on a childhood visit. The first click on a bike is a look at it. A click on a bike
lying down stands it up. A click on a standing bike gets on it, and the first time the keys are shown in grey:

> **A** and **D** push the pedals in turn · **S** brake · **Shift** get off

## Pedalling is balancing

The strafe keys are the pedals. Each press of left or right is one push on that pedal. Pedalling and keeping the bike up are the same
thing:

- **A push in turn** (left after right, or right after left) drives the bike on and tips it a little toward that pedal's side.
- **Two pushes running on the same pedal** drive it on less and tip it much more toward that side.
- **Left to itself**, a moving bike leans further whichever way it already leans: quickly when it is slow, slowly when it is going well,
  with a little wobble of its own.
- **Turning** leans it into the turn.

So a reader keeps it up by pedalling in turn, watching the lean, and, when it starts to go over, pushing twice on the pedal on the
other side. Pedalling in turn without watching it goes over in about half a minute. Pushing the same pedal over and over goes over in
a second or two. Stopped, a foot goes down and nothing falls.

It goes where the rider looks, no faster than a bike can turn. Uphill slows it and downhill speeds it. The back key brakes, and water
drags at it. A glance along a wall keeps most of the way on. Riding head on into something fast puts the reader over the bars.

## Coming off

If the lean reaches all the way over, or the bike runs into something fast, the reader comes off on that side and the bike lies in the
road. The fall costs half a heart, never below two hearts. Sneaking gets off and leaves the bike on its kickstand. Either way the bike
is a block again where it stopped, and it is still the same child's: the reader's saved state keeps where each bike was left (`BikeAt0`,
`BikeAt1`). The next visit clears a bike left somewhere else and puts the bikes out as before (Patrick's lying by the store on the first
visit, Kendra's on her stand on the first two).

The bike is never saved as an entity. Anything that takes the rider off it puts the block back first: getting off, a fall, a door, a
teleport, or logging out.

## What the children say

Once a visit, Patrick or Kendra, if they are about and can see the reader, says something about it:

- Kendra, of her own bike: "You can ride it. Just bring it back." (On the second visit: "Bring it back here after. I'm waiting for
  Patrick.")
- Kendra, of Patrick's: "That's Patrick's. He won't care."
- Patrick, of his own: "Don't put it in the ditch. And pedal both sides, not just the one."
- Patrick, of Kendra's: "She lets me ride hers too. You have to keep pedaling or it tips."
- Kendra, after a fall nearby: "You have to keep going or it falls over." (and "Is it scratched?" when it was hers).

The account gains a line: "I rode Kendra's bike around town." (or Patrick's, or both), and "I came off once, and it went down in the
road." or "I came off more than once."

## What the rider sees

- `labyrinth.BorrascaBike` is the ridden bike. Everything is the server's. The client sends its strafe keys as any rider does.
- `client.BorrascaBikeRenderer` draws it from three new models by `tools/draw_borrasca_town_art.py`: `bike_frame` (no wheels,
  kickstand or pedals), `bike_wheel` and `bike_cranks`. The wheels turn with the speed. The cranks bring the pushed pedal round to the
  bottom: a half turn for a push in turn, a whole turn for the same pedal twice. The whole bike leans over about the line its wheels
  touch the road.
- `client.BorrascaBikeClient`:
  - The rider leans with the bike, as others see them.
  - In first person the view tips the same way, scaled by the reader's distortion setting (`Comfort.effects()`).
  - A needle under the crosshair shows the lean: upright in the middle, and red at either end, where it goes over.

## Tests

- `BorrascaTests#aChildGetsOnKendrasBikePedalsAndGetsOff`:
  - The first click looks, and the second gets on.
  - Stopped, it stays up.
  - The left and right strafe keys push the left and right pedals.
  - A sneak gets off, leaving the bike on its kickstand where it stopped, still Kendra's.
  - The account says the reader rode it.
- `BorrascaTests#pedallingInTurnKeepsTheBikeUpAndOnePedalPutsItDown`:
  - On a straight road, pedalling in turn and correcting with two pushes on the far pedal keeps it up for eight seconds and carries it
    more than 25 blocks.
  - Pushing one pedal over and over puts the reader off and the bike on its side, remembered once in the account.
- `BorrascaVisualProof` draws a thirteenth page: the parked bike, and the ridden one upright and leaning each way with its wheels and
  cranks turned.

## Numbers

Protocol 174 unchanged (the bike's state rides on its own synced entity data). Witness 86/65/63 and layout 135 unchanged. New builds
only.
