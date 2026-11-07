# State Route 9: what the road leaves (0.4.336)

The owner's notes on State Route 9 (docs/STATE_ROUTE_0_4_333.md):

> "What if getting hit renders the player's corpse, injured and torn by the vehicles? It frames it for a few seconds then spawns them in their bed."
>
> "Ensure you're using the house skin. Items should be dropped as per normal death, so the mother of strays can collect."

Being struck squarely by a car or a semi is now a real death. It no longer blacks out and sets the reader back on the shoulder. A pickup's knock and the wind off a trailer are unchanged.

## The death

1. **The strike.** Brakes, then a heavy impact (a new subtitled cue, "A heavy impact on the road").
2. **The body.** It is carried along the lane, three and a half blocks by a car or seven by a semi, and left at the lane's edge.
3. **The death itself.** It is an ordinary death, by the `route_vehicle` damage type ("… was struck on the 9"). Whatever the reader carried falls where the body lies, so the Mother of Strays collects it as she would after any death in the House. The key alone goes back into the mailbox, as before. keepInventory, and every other rule about deaths, apply as they would to any other death.
4. **The reader's own last sight.** There is no death screen. Their camera stands off over the body and comes slowly down round it for four and a half seconds. The view then fades to black, they respawn without pressing anything, and the fade lifts.
5. **Waking.**
   - **With a bed.** They wake lying in their own bed, for a moment, even by day, then get up: "You wake in your own bed. Your clothes are wet through, and there is grit in your hair."
   - **Without a bed.** Wherever the world puts them: "You wake on cold ground, wet through, with grit in your hair."

## The body on the tar

The body wears the reader's House body (the skin the House gives them inside it; the player's own skin where House bodies are turned off). It lies on its back, twisted:

- the head is turned away;
- one arm is flung over;
- the other is bent out the wrong way;
- a leg is folded at the hip.

It is torn and bloodied by the road:

- hairline and face;
- road rash down one side;
- a soaked, torn shirt;
- a dragged back;
- a scraped arm;
- a bloodied knee;
- a torn leg.

There is also:

- a smear up the lane from where it was hit;
- a pool under it;
- glass and one shoe back at the point of impact.

The body stays on the road for a minute, for everyone on it to see. The road keeps up to six at once.

## Together

Everyone on the road within thirty-six blocks of the strike sees it. Each of them keeps whose body it was and how many they saw, under `RouteSawWreck` and `RouteSawWrecks`.

## Accounts

- **Struck.**
  - Struck once: "I misjudged it. I heard brakes, and then I was looking down at myself on the tar, at what the road had done to me, and one shoe a long way off."
  - Struck more than once, each time: "I heard brakes, and then I was looking down at what the road had left of me on the tar."
  - Then, by where they woke: "Then I woke in my own bed (or on cold ground), wet through, with grit in my hair, and had to come all the way back." `RouteAbed` records waking in a bed.
- **Witnessed.**
  - One body: "I saw what the road left of {name}, out on the tar where the traffic kept going round it."
  - More than one: the count, with the last name.

## Tests

`StateRouteTests#theRoadTheTrafficAndWhatItDoesToABody` now ends with a car strike while a second reader stands on the shoulder. It checks:

- a real death;
- the body at the lane's edge, carried along the lane;
- the dying reader lying there;
- the bread they carried fallen round it;
- the witness's record;
- both accounts' new lines.

The pickup and the wind leave the reader alive and unstruck, as before.

## Network

Protocol 138: `state_route_wreck` (the reader's own shot of their body).
