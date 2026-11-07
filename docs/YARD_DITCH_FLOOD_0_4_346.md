# The Ditch in Flood (0.4.346)

The yard's drainage ditch, in flood, from the owner's draft "The House Has a Yard" (4 November). It is a gray passage outside, not a Witness story. Layout 107 appends slot 207. There are no new block states, so the protocol stays 141. Nothing standing is rebuilt.

From the draft:

> *"Follow the shallow flow to a culvert with a low, visible lip. Leave a place to get down into the crawl and a clear channel to the other side. [...] The quiet version earns its place as a routine connector. The flash-flood version belongs to the hazard pool and has its own warning and escape routes."*

`YARD_DITCH_FLOOD`, `YardFlash`. It is a physical hazard, dealt (weight 3) from the strange depths. The quiet ditch (`YARD_DITCH`, 0.4.313) stays as it was, a routine connector.

## The ditch

It is the same ditch: `YardConnectors` builds it.

- **The trench.** Boot-deep water over stone slabs between high hedges.
- **The culvert.** Its crawl is really one block high, behind a concrete lip at each end.
- **The gravel run.** It leads on beyond the culvert to the way out.

**The cuts.** Two cuts climb the east bank, each two rows wide. From the water there is a half-step up, then a slab, then a ledge above the water. A post stands on each ledge with a bulb on it, so the way up shows from the trench.

## The flood

The flood runs only while someone is in the ditch.

- **The quiet ditch.** The first sixteen seconds are the ordinary ditch.
- **Rain coming on.** For four seconds:
  - thunder far off;
  - rain falling into the channel;
  - sticks and leaves coming down the water and ticking against the culvert's lip.
- **The water rising.** For three seconds the water climbs from the boots to the knees all along the trench, and the creek grows louder. It does not carry anyone yet.
- **The surge.** For four seconds:
  - The water runs through the pipe and out over the gravel beyond.
  - Whoever is in the channel is carried north with it, out the far end onto the gravel, breath kept and unhurt.
  - A reader pressed against the culvert's mouth is taken into the crawl and carried through it.
- **Going down.** The water drains, and the ditch is quiet for sixteen seconds before the rain comes on again.

**The ledges stay dry.** Whatever ran over a cut's first step, into the doorway, past the culvert's sides or along the gravel goes with the water when its phase is over.

**Reset.** Once the ditch has been empty and unwatched for ten seconds, it is the quiet ditch again and its clock starts over, so the next reader has the whole warning ahead of them.

Nothing here hurts or is used up.

## Assets

None new. The thunder, rain and gravel are vanilla sounds; the creek is the yard's own sound.

## Tests

`YardFlashTests` (exploration), with three readers in the ditch:

1. **The climb.** The first reader walks out of the water up a cut's half-steps onto its ledge.
2. **The warning.** The rain comes on with the water still boot-deep. Then the water rises to the knees without pushing anyone.
3. **The surge.** It carries the second reader down the trench, into the culvert's crawl, through the pipe and out onto the gravel, with full health and breath. The third reader, on a ledge, is still dry and has not moved.
4. **The drain.** The water goes down and leaves nothing standing anywhere in the ditch.
5. **The reset.** A surge is reset to the quiet ditch and leaves the ditch dry.

The ditch earns no story credit. The quiet ditch's culvert case and the clothesline case, which share its builder, still pass.

The place is exported for the native architecture views.
