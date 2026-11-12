# The cemetery's fold (0.4.521)

The owner, playing Bike night on November 12: "there should also be a transition in the cemetery to a new zone, so this isn't
visible - the cemetery should seem to suddenly expand, the player shouldn't see it looming in the horizon or be able to revisit it
come sunset".

## What it was

The Hollow began where the cemetery's last row ended (docs/BIKE_NIGHT_HOLLOW_0_4_493.md). The hill's back gave out onto the
floating ground, and that ground stood over the hill, so anybody in the town could see it from the streets. After the night was
over, anybody could ride back up the hill and over it again.

## What it is now

- **The town's cemetery ends at its back railings again.** They run all the way across, with a pier at each end and the back gate
  shut in them. Behind them are a lane, its wall with the door on in it, and a wood (`CalderGraves.back`).
- **The Hollow is 1,056 blocks further north,** past a cemetery of its own, the expanse (`CalderGraves.expanse`). No client draws
  that far (thirty-two chunks is 512 blocks). The expanse is the town's cemetery going on where the town's stops. It has the same
  ground and banks, rows every three or four blocks with the gate's line clear, the town's trees, and an angel and an obelisk the
  town's cemetery has none of. It runs another thirty-two rows past where the town's back railings would be, to the hill's edge.
- **The fold (`BikeNightFold`).** While the chase is on (the cemetery's front gate has opened for it), somebody who goes north past
  the fold, a few rows short of the back railings, is carried 1,024 blocks north. They can be on foot or on their bike, with whoever
  is on its pegs, its way and its lean kept. The mist closes over them for a second and a half while it is done, and they hear it
  close. They come out of it in the expanse.
- **The expanse's mist is thinner than the town's last rows'** where the fold lets them out, so there are rows on every side where
  the railings were. It thickens toward the hill's edge, as the town's last rows did (`BikeNightFold.mist`).
- **Back.** Going south past the fold's far side, somebody is carried back to the town's cemetery, a few rows short of its back
  railings.
- **Out.** Through the Hollow's back gate onto its lane, somebody is carried to the town's own lane, just behind its cemetery's back
  gate. That gate opens with the Hollow's: the two back gates are one gate (`BikeNightFold.gates`). The door on is in the town lane's
  wall. "I rode out by the back gate onto a lane, and down the hill the lights of the town were on again" stays true.
- **Gus** goes the same ways after his friend: in after them, out of the back gate after them, and back after them. If his friend
  is already out in the lane, he goes in and rides the Hollow to take his cap down off its gate, as before. He keeps his distance
  from where he is going, so the fold takes him a few blocks sooner than it takes a reader, on either side. Going out, it takes
  him from his distance short of the Hollow's back gate once his cap is down, and puts him in the town's lane where a reader
  comes out, past the town's back line, so it has nothing to take him back in for.
- **Nobody else goes through.** Before the chase, or once the Hollow has had somebody or let them out, they come to the back
  railings and nothing else. There is no seeing the Hollow, and no going back to it, from the town.
- **The ground on both sides is held** while somebody is near the fold in the chase, so whoever is carried lands on ground that is
  in.

## Building it

- The place's box now reaches the lane past the Hollow. Its builder, a scene copy's builder, the report-only vignette audit and the
  architecture views hold and look at only the two built parts (`BikeNightFold.built`), never the thousand empty rows between.
- The architecture test exports the far side as a view of its own, `bike_night_hollow`; the scene's own view is the town.
- The Hollow's ways, the specters, the trail, the leaps and the islands are where they were, moved 1,056 blocks north.

## Decided here

- The fold carries 1,024 blocks, and the Hollow begins 32 rows further on, so there is a cemetery to come out into.
- The fold is four and a half blocks short of the back railings. In the town's last rows a rider sees seven blocks in the mist, so
  the railings can show just before it closes; when it lifts they are gone. That is the expansion.
- The expanse is as wide as the town's cemetery, ragged and without railings at its sides. The place's box allows little more, and
  in the mist nobody sees that far to either side.
- Gus is carried out a little sooner and a little further than a reader: from his keeping distance short of the Hollow's back gate
  to just past the town's. Carried by the fold's own width he would land inside the town's railings, where the fold would take
  him straight back in. His cap's post is inside that distance, so he is not taken until his cap is down.

## Not done, for the owner

- Looking south from the expanse shows the expanse's own rows, not the town. Somebody who rides back south through the fold sees
  the mist close over them again on the way.
- A spectator is never carried; one following a reader goes by the spectator's own means.

## Checked

- Three new cases in the Bike night namespace:
  - the town's cemetery is shut at its back with the gate in it, the lane and the wood are behind it, and the far side is further
    off than anybody draws. The expanse has rows past where the railings stand, no railings, and its own two memorials;
  - before the chase, somebody past the fold is not carried and nothing past it is held;
  - in the chase, a rider on the Wasp is carried with it into the expanse, hears the fold and has the mist over them for its surge.
    The mist there is thinner than the town's last rows' and thickens by the hill's edge. Somebody on foot is carried over and back,
    and once the Hollow has had them they are not let through again.
- Eight existing cases changed for the new ground: the cemetery's railings (the back corners are the back railings' end piers),
  the dark way's mist, the plain way (ridden from the expanse), Gus on the plain way, the back gate (the riders come out in the
  town's lane), the ending's wall and door, a seventh reader's town, and the whole night.
- The whole Bike night namespace: 108 of 109. The one that failed was the whole night: its riders stopped in the town's lane
  and Gus, coming after them by way of his cap, was taken out before he had it down (his cap's post is inside the distance the
  fold takes him from). He is now taken only once it is down. That case, Gus on the plain way and the cap's case again: 3 of 3.
- The quick checks.
- The architecture namespace, 6 of 6, with the far side's own view (`bike_night_hollow`).
