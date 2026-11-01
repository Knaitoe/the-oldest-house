# Two review fixes: held companions and arcade scores (0.4.225)

The owner's code review of 1 November named two issues to prioritize.

## The replacement could strand a captured companion

> "Replacement can strand a captured pet. `leftBehind()` only finds loaded entities, then clears every saved pet ID regardless of
> whether the transfer succeeded. An unloaded captive can remain sitting and invulnerable in the crate, with its recovery record
> gone. Keep unresolved IDs until each pet's return is confirmed, and test leaving after unloading or restarting."

It was as described. A reader who left the street with a companion in the crate had it sent home by a pass that looked it up
among loaded creatures only, then cleared the whole list (`Held218`) whatever had happened. A companion whose part of the street
wasn't loaded (the reader had walked away from it, or the server had restarted), or whose move between dimensions failed, stayed
in the crate, sitting, unhurtable and held, with nothing left on the record to find it again.

Now (`Replacement.leftBehind`):
- Each companion stays on the record until its return is confirmed: it arrived at the manor (or was already there), or it was
  let out of the crate.
- One that isn't loaded is looked for again every second while the reader is away: the crate's chunk is loaded for it (a
  short-lived ticket, so it unloads again afterwards), and when it loads it is sent home.
- It is crossed off without arriving only once that chunk's creatures are loaded and it is not among them: then it is not in the
  crate, and there is nothing to strand.
- A move that fails leaves it held, unhurtable and on the record, to try again.
- The crate remembers where it is (`Pen218`), so this works after a restart.
- Opening the crate had the same weakness: it let out the companions it could find, then counted them all as freed. One that
  wasn't loaded yet is now let out as soon as it is (`Released218` holds the ones let out this visit, and the walk home checks
  those).
- "Yours has gone home ahead of you" is said once, when the first companion arrives.

**Test** (`ReplacementTests`, literary namespace): a companion held at a crate far from everyone. Its chunk is let go until the
companion is gone from the world, and the saved data is written and read back, as across a restart. The reader leaves. One pass
with the companion out of reach must keep it on the record (the old code cleared it here). Then the passes load the crate's chunk,
and the companion arrives at the manor: sitting, no longer held or unhurtable, still the reader's, and not also left in the
crate. It is crossed off only then. The existing case, where both areas stay loaded, still passes.

## The arcade believed the scores the screen reported

> "Polybius trusts client-reported scores too much. `submit()` checks a generous scoring ceiling, then awards tickets and records
> the score. It even accepts 50 points with zero reported play time. Block Party already runs on the server; the other games need
> stronger validation if their shared tables and prizes should be trustworthy."

The three working cabinets (and the black one) ran on the reader's screen, which sent a score and a play time; the server took the
score if it was under three points a tick, plus 50, of a play time the screen also reported.

Now the games' rules are shared by the screen and the server (`labyrinth/PolybiusGames`). Each game was already seeded from the
session's token, so it depends only on that seed and the keys held on each tick.
- The screen plays the game and draws it as before, but sends the keys it held on every tick, as runs
  (`PolybiusScorePayload(token, runs)`, protocol 118), and no score at all.
- The server plays the same game again from its own seed with those keys (`PolybiusGames.replay`). The score that replay reaches
  is the one recorded on the table and paid in tickets.
- A log may run no longer than the session has been open, plus two seconds (the screen opens a moment after the session). The
  server uses its own clock or the wall clock, whichever is further on, so a lagging server doesn't turn a real game away and a
  log can't claim more play than real time allowed.
- A log of nothing scores nothing. A malformed log (a run of no ticks) is refused, and so is one longer than the game runs. The screen now ends a game at its longest itself, so it never loses one to
  the session closing.
- What is only drawn (the dial's snow) uses the screen's own randomness, so it can't knock the game out of step with the replay.
- The black cabinet's play counts on the length of its key log, under the same time limit.

Someone can still write a program to play well; it has to play in real time, from the reader's own session.

**Tests** (`PolybiusTests`): a game with another session's token is refused. An empty log scores and pays nothing. A 2,000-tick
log the moment the session opens is refused. A log with a run of no ticks, or one longer than the game runs, is refused. A played
game stands at exactly the score the server's replay reaches, and is paid for that score. One place each on the table, best kept,
in order. The black cabinet's and Signal's cases send real key logs.

## Not changed

No layout or Witness change; protocol 118 (the score payload's shape). New builds only.
