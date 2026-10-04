# A labyrinth that holds still (0.4.41)

Playtest report: the hallways loop far too often and need to be more static, so
that exploring them means something.

## Why it looped

- Each labyrinth place exists once physically. Ordinary corridors have one
  entry and one dealt exit, so the early labyrinth is a few rooms reused.
- Every arrival re-dealt the doors of the room just entered, so the same door
  led somewhere new each time.
- The dealer was free to send a player straight back into the room they had
  just left: straight hall to bent hall to straight hall. With two equally
  weighted corridors that happened about half the time.
- Nothing could be mapped, and the same two rooms kept alternating.

## What changed

- **A remembered map.** Each player now keeps a map. A spot is identified by its
  route: the doors on the player's way back, in order, plus the place itself.
  - Arriving somewhere by a route taken before restores what lay behind each of
    its doors.
  - Going back and forward again, or returning after a restart, finds the same
    halls. The map is saved (`Nodes` in each player's dealer record, newest
    1,024 spots).
- **Shared routes look the same.** A new spot is dealt with a random seeded from
  its route and the manor. Explorers who take the same doors from the same
  hallway find the same ordinary halls. Personal story eligibility, pacing and
  pets still differ per player, so stories and deep anomalies can still diverge.
- **Only closed doors change.** A remembered door is dealt afresh only when:
  - its destination can no longer be found by that player (a finished story, a
    place no longer offered, or the finale no longer offered);
  - the player carries Hillary's scent;
  - the player is rescuing a pet from the Mother's den.
- **No ping-pong.** An ordinary deal never leads straight back into the hall the
  explorer just came from, as long as at least two other halls are available.

Unchanged: per-player deals and the return stack, story pacing and depth gates,
vignette rediscovery, the finale offer, layout 32 / protocol 32. Statistical
dealing by `dealPlace` is unchanged; only actual arrivals use the map.

## Verification

`LabyrinthMapTests` covers:
- the same route restores the same doors after unrelated re-deals;
- a second explorer on the same route sees the same halls;
- different routes are told apart;
- the map survives a save and load;
- 200 arrivals never deal a hall straight back to the one just left.
