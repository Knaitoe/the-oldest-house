# The pool and the kitchen, after review (0.4.238)

Three findings from the owner's source review, 1 November: two are bugs, one is a timing concern. None was reproduced in a live
playthrough; each now has a native test. No protocol, layout, Witness or source-kind change.

## The pool is put back only when nobody is in it

The pool (the last swimmer, 0.4.227) used to count only readers who had not finished. A reader who walked out through the fire door
and stopped beside it to read the account no longer counted, so five seconds later the attendant vanished, the cover rolled back, the
lights came up and the fire door shut around them.

Now every living player in the hall who is not a spectator holds the pool, finished or not. While only finished readers are there the
pool stays exactly as it is (the attendant at the open door, the cover on, the lamps as they were). It is put back five seconds after
the last person has left the hall.

## The kitchen's window after a reconnect

The yard below the familiar kitchen's window (0.4.232) is laid out on each reader's own client, so it is lost whenever the client is
sent the yard afresh. A reader who reconnected while standing in the kitchen saw the plain yard: room tracking keeps where a reader
was across a reconnect (so a reconnect is never a fresh arrival or a dependable return), which meant the view was never laid out
again. The cleanup helpers were not called on logout either.

Now the view is laid out again when a reader logs back in inside the kitchen and whenever the client is sent a chunk holding part of
the yard (after a reconnect, a respawn or the chunk coming back into view), a moment after the chunk so it lands on top. A reconnect
is still not a visit. Logging out forgets what was waiting to be shown and what was shown.

## The pool's beats come with the cover

The radio's later beats, the lights wavering in the song's second spoken part (about 131 seconds in) and the splashing stopping when
the last verse begins (about 148 seconds), both need the cover still being pulled. Its seventeen hauls can be done in about ten
seconds, so a reader who did as the attendant asked, promptly, missed both.

Each beat now comes once, with the song or with the cover, whichever is first, and only while a lane is still open:

- **The lights waver** at the second spoken part or once a third of the pool is covered (six hauls), for as long as that part lasts
  (17 seconds) or until the splashing stops.
- **The splashing stops** at the last verse or once two thirds are covered (twelve hauls), with the same five seconds of nothing,
  remembered for every reader in the hall.

A reader who takes their time still meets them with the song, as before.

## Tests

- `LastSwimmerTests#aPromptCoverStillMeetsTheBeatsAndALingeringReaderKeepsThePool` (literary namespace): six prompt hauls waver the
  lights before the radio's second spoken part; twelve stop the splashing before the last verse and steady the lights; the cover goes
  on; the reader walks out, then lingers by the door for longer than the reset and the pool stays as it is; once they leave the hall
  it is put back.
- `FamiliarRoomTests#theWindowIsShownAgainAfterAReconnect` (exploration namespace): the yard laid out at the window; logging out
  forgets it; logging back in inside the kitchen lays it out again without counting a visit; a yard chunk sent again lays it out
  again; reconnecting elsewhere lays out nothing.
