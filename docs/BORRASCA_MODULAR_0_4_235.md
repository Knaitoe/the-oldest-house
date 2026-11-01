# Borrasca, played in any order (0.4.235)

The owner, 1 November, after a review of the vignette: "I think the vignette needs to be more modular, rather than assuming the
player isn't going to go explore the entire map as soon as they enter. I also don't think the shiny gentleman has been introduced as
a monster." Then: combine that with the review's notes and suggestions and begin all of them, with the trigger warning's content
present in the vignette rather than the warning removed.

On the cast: "Let's drop Kimber. Remember, the vignette was supposed to occupy a separate cast in the same environment? The sheriff
could fit, because he was in power for a while but the events of borrasca had to have played out for many, many casts of kids." And:
"The Prescott family is well established too. They could still exist, no? You should consider this for the narrative build."

This release is the first of three. It makes every beat happen where the reader is rather than where the script expected them, seals
the refinery until the last night, replaces the friend, and fixes the review's smaller points. The Shiny Gentleman seen in the
distance when the noise echoes on a child visit is 0.4.236; the warned content, off-screen, is 0.4.237. Network protocol, layout,
Witness and source kind are unchanged: Borrasca is still one understanding story, resolved by the same ending.

## A separate cast in the same place

The friend is now **Nell Garrity**, twelve, one of the later Drisking children. The trail, the three trees, the fort and the
refinery are the ones every generation of children has had; the fort has been built and rebuilt "for as long as anyone in Drisking
can remember". Two things last across the generations, and they are the vignette's adults:

- **The Sheriff**, the same man, in office long enough to have stood at the second marker for children before Nell and to stand there
  again when the reader comes back grown.
- **The Prescotts**, a family business "since the old mine": the hardware store's owner at the trailhead, and the signatures on the
  county's papers, going back further than the store.

The traces of earlier children are in the place, not explained: older names worn soft in the bark, and on the second visit, lower down
the trunk, a name someone has scraped away; an older brown poster under the
board's current one, a tag number on the property receipt with the next tag already written out for someone else, Nell's mother
saying the fort was there when she was little and the rules were the same. Internal identifiers (`CastKimber`, `BORRASCA_KIMBER`, the
skin file) keep their old names; only what the reader sees changed. Nell's name patch and compass are redrawn for her name.

## Wherever the reader goes

**Visit one, the pact.** Nell walks to the Tree at her own pace whether the reader is beside her, behind her, or already there; a
reader who runs ahead hears "Hey! Wait for me!" once. She tells the legend whenever the reader is at the Tree with her, in the fort or
at its foot, and it now introduces him as a monster: "They call him the Shiny Gentleman. He lives past the second marker." "You hear
him before you see him. Three times, like somebody hitting a pipe a long way off." Dana Pruitt went up to look at him and "moved
away". "So if you hear him, you come down." She climbs down and carves once the reader is near enough to see it, calling them down
if they are not. The reader's own cut is a click at the carving, as before; the noise answers it from the ridge.

**Past the second marker as a child.** A child who crosses Marker Two on either child visit hears the three strokes once, the dust
lifting with them, and Nell has a word for them afterwards ("You went past the second marker? Don't. That's where he is."). 0.4.236
adds what they see.

**Visit two, the taking.** A note by the door (the trailhead content note) must be read first, as before; Nell waits at the first
marker. She sets off once the reader comes near. The two shelters happen where she is when the noise comes: she ducks behind the
timber and says so; the reader is not made to crouch, though a reader who does is remembered for it. At the clearing she waits for
the reader wherever they have got to, calling for them, and is taken there: two men come down the ridge road from wherever the reader
is not looking, if there is such a place, walk the whole way down to her with their steps heard on the gravel, take her arm and
lead her up toward the wire. The light on the ridge stays on from the moment she sees someone until she is gone. She calls the reader's name once. Her compass falls where it always did; the heavy tracks lead uphill. Afterwards the three of
them are seen walking up while anyone watches and are gone once nobody is.

"Behind" now means further back along the whole route, so a reader waiting up ahead is never waited for.

**Visit three, the blockade.** The gouge is seen by looking at the bark from close (or a click). The Sheriff gives himself away as
soon as he has the reader in sight, from either side of the tape. The wire is reached by any route he does not see: the gully is one
way among several, and the visit is done once the reader has seen the gouge and stood at the wire. His beam holds on whoever it found
until they are out of it; at forty it becomes the escort, which walks behind the reader, never pushes or moves them, waits when they
stop ("Keep walking.") and lets them go on the near side of the tape ("Stay this side.").

**Visit four, the names.** As before. The pack is at the sorting table only from visit three (it was there before she was taken).

## The refinery is sealed until the last night

The service path used to cut the compound's west fence, so a child could walk into the yard on the first visit. It now runs outside
the fence to an enclosed stone tunnel under the yard, with an iron back door at the compound end and an iron gate at the woods end,
both opened only by the release lever on the last night. The fence is whole on every visit. The socket proof's route (down the
service steps, crouched through the tunnel, out past the gate) is the same route.

## Speech is never chat

Everyone in the vignette speaks over their own head and on the reader's action bar ("Nell: …", "Sheriff: …", "Prescott: …", "A
man: …"), and the line clears after five seconds. The House's own word (the note by the door) is grey italic, also on the action bar.

## Smaller points from the review

- Workers are seen only on visit three and during the taking; Prescott is not at the store on the last night. Nobody stands idle as
  an audience.
- Name tags are drawn only for people who are visible.
- Two new gestures: Nell being led (arm out behind her, head turned back) and a taker's hand reaching for her.
- Each visit has its hour on the client: late afternoon, dusk, morning, night.
- The account is written from what the reader did and heard: the shelter if they took it, the denial if they heard it, the restored
  name or the cloth.
- The source page no longer says how anyone vanished; it says children have built the fort "for as long as anyone in Drisking can
  remember".

## Tests

`BorrascaTests` (borrasca namespace, also run by the literary job): the refinery sealed from the woods until the last night (fence
whole, tunnel shut, gate and door opened by the release); a reader who runs to the Tree first still makes the pact there; a reader
waiting at the clearing still loses her there, the shelters happening without the reader crouching and two men leading her away; the
gouge seen by looking, the denial at sight, and an unseen route to the wire completing the visit without the gully; the escort that
waits for a still reader and ends at the tape; the service route walked north then west through the tunnel. Existing cases updated
for Nell. The diagnostics case now uses site indices no other case builds at, since an earlier batch's chunks may still be resident.
