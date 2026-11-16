# A sort of thing 2: his breath, and how much of a thing a reader is (0.4.642)

The second part of the plan (docs/SLEEPING_KING_PLAN.md), from the proposal (docs/SLEEPING_KING_PROPOSAL.md). Still not dealt.

## His breath

One clock for the country (`KingBreath`), in for four seconds and out for four, counted only while a reader is there. Every reader
hears each breath, from his direction and louder the nearer him (`king.breath_in`, `king.breath_out`, made in 0.4.641). On the
in-breath the edge of the filled land moves a block out: toward the house on the near side of the wood and toward the far ranks
beyond it, so the fill grows both ways. On the out-breath it moves back. Every column whose drawing differs between the two
(`KingSite.breathing`) goes over to its twin and back (`KingRegistry.twin`).

## How much of a thing a reader is

One sum, from the proposal's table. This part builds:

- **The land underfoot.** Filled land makes a thing (3), the band where the fill gives out makes a sort of thing (2), and ruled land
  makes a line (1). The house is filled.
- **Being looked at** by another reader in the country who is at least a sort of thing, with nothing between them. This is one step
  up, and no further than a thing.
- **His in-breath, beside him** (within seven blocks). This is one step up, and the only way past a thing so far. It makes a reader
  too much of a thing (4).
- **Off the country altogether**, which only a line can reach through its hedges, a reader is nowhere (0). The dream puts them back
  on the last square that counted them, unhurt.

Pegs, being named, what is swallowed and the crown come in later parts.

What each state does:

- **Too much of a thing** is rooted: no walking and no jumping.
- **A thing** is bound. The hedges are walls (`KingHedgeBlock`). A brook can be crossed only at its bridge, because an unseen verge
  half again as high as a jump lies over its two rows (`KingVergeBlock`).
- **A sort of thing** goes through hedges and brooks slowly. Its feet are soft (`king.soft_step`), it is drawn flat, pressed to a
  card's depth, and it casts no shadow.
- **A line** goes through both as if they were not there. Its feet make no sound. It is drawn in outline (every edge of the body in
  pencil, nothing filled), it casts no shadow, and it has no hands in front of its eyes.

Each reader's own amount is sent every quarter second to every reader in the country (`KingStatePayload`), held two seconds on the
client (`KingClient`), and used both for what the hedges and verge let through and for how each body is drawn
(`KingShadowMixin` for the shadow).

## Checked

- `KingTests.hisBreathAndHowMuchOfAThingAReaderIs`:
  - filled land makes a thing, which the hedge and the verge hold, and the verge is not over the bridge;
  - ruled land makes a line, which goes through both;
  - a reader who is a thing looking at the line makes it a sort of thing;
  - beside him on the in-breath a reader is too much of a thing and rooted, and free on the out-breath;
  - every breathing column shows the in-breath's drawing, and the out-breath's after;
  - a reader off the country is put back.
- The first case still passes.
- The King's cases are now in the complete suite's default list too (build.gradle). 0.4.641 left them only in their own CI job, which `tools/structure_groups.py --check` caught.
- Look review: the three looks drawn from the production code's flat press and pencil edges (a thing, a sort of thing from the
  front, three-quarter and side, and the line) on a local review sheet. The line is drawn without the skin's outer layers, which
  would otherwise cage the figure. This sheet draws the model directly. The in-game hook that hides those layers while the outline
  renders, and the shadow mixin, have not been seen in a live world.

## New lines (for the owner)

- Put back from nowhere: "The ground comes back underfoot, on a square stood on before."
