# The waxworks 3b: the monitors (0.4.752)

The rest of part 3 of the waxworks (`docs/WAXWORKS_0_4_751.md` is the shift): the bank of six monitors in the security office, as the design asked ("FNF style central security room with camera display for various rooms, watching for irregularities").

## Sitting down

- A hand on any of the six screens, from inside the office, sits the reader down at the bank (`WaxMonitors.sit`, `WaxMonitorPayload`). From the staff corridor it is only a screen. A spectator does not sit down.
- The reader's own client opens the bank: six feeds, three over three, each labelled CAM 1 to 6 (the foyer, the Hall of Statesmen, the kitchen, the chamber, the workshop, the corridor) with the time. Keys 1 to 6 or a click bring a feed up; N, a right-click on the feed that is up, or the button under the screens marks it: "Something's moved". Escape stands up.

## The feeds

- `WaxCameras` draws one camera a frame, in turn, while the bank is open: the native level renderer from that camera's corner (the same way the bedroom's monitor is drawn), looking the way the camera looks and down into the room. Other readers are in it, and so is the reader if they are in view.
- The galleries are dark, so the picture is a night camera's: grey, brightened out of the dark, with grain that moves; scan lines are laid over it.
- When the night fails, the screens go to snow.

## Watching and marking

- The feed a reader has up is **watched**. Nothing is made in a gallery whose feed somebody is watching, and an irregular figure does not move while either the place it is in or the place it would go next is on a watched feed (the gallery's own feed while it is in its gallery, the corridor's while it is in the ring). So whoever has the office keeps a gallery still by watching it, and cannot watch them all at once.
- **"Something's moved"** on the feed that shows an unnoticed irregularity notes it, as noticing in person does: in the log, on the reader's record (`Noticed`, and `NoticedOnScreen`), and told where it was. On a feed that shows nothing out of place it notes nothing ("You look again. Nothing on that screen is out of place."), and the next mark waits two seconds, so marking every feed in turn is slow.
- A reader not on the shift marks nothing ("The log is not yours to write in. Your card is not in the rack.").
- Standing up, or walking out of the office, leaves the feed.

## More than one reader

The design's working together is now possible: one reader in the office watches feeds and marks them, another walks the rounds. Each reader at the bank has their own feed up; any of them watching a gallery keeps it still. The feeds are drawn on each reader's own client. Spectators do not sit down.

Protocol 207 (`WaxMonitorPayload` to the client, `WaxFeedPayload` back). No new block, texture or sound.

## Not yet seen

The feeds are drawn by client code that the server tests cannot reach; they have not been seen in a client frame yet. The capture is guarded so a failure to draw a feed only leaves that screen dark.

## Tests

`WaxworksTests#theMonitorsAndWhatTheyShow` (batch `waxworks_monitors`): from the corridor a screen is only a screen, from the office it sits the reader down; a spectator does not; the feed up is watched; a mark on a feed that shows nothing notes nothing and a second straight after is not taken; on the feed that shows the irregular figure it is noted, logged and kept as seen on a screen; someone not on the shift writes nothing; standing up and walking out both leave the feed. The museum case no longer expects the screen's description from inside the office.

## New text

For the owner's review: the two lines above, and the feed labels.
