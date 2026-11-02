# Following friends through a door (0.4.270)

The sixth of the owner's quality-of-life notes of 2 November (docs/OWNER_QOL_NOTES_2026_11_02.md):

> Clearer group crossings. Co-op already has a follow window and compatibility checks. Add a subtle indication that friends can follow
> through this door, plus feedback when private scenes or different story progress will separate them. That would prevent
> considerable multiplayer confusion.

Code: `labyrinth/GroupCrossings`. How groups are dealt is unchanged (docs/COOP_FOUNDATION_0_4_146.md): the first through
a dealt door into a shared vignette holds it for thirty seconds, and whoever follows through it in that time comes into the same
scene if they can.

## At a held door

While a friend's crossing holds a door:
- **Motes.** A reader within six blocks who can follow sees a few pale motes drift at the door. Only that reader sees them.
- **Who went through.** Coming within four blocks, they are told once, on the action bar: "Ana went through here a moment ago.
  The way is still open behind them."
- **Who cannot follow.** A reader within four blocks who cannot follow sees no motes and is told why instead:
  - "You have already been where Ana went. This door leads you elsewhere." when they have finished it, or would find only its
    aftermath;
  - "Ana went into a story yours has already parted from. This door leads you elsewhere." when they belong to another thread of
    it.

Nothing is shown to the one who went through, to anyone only watching, or to anyone in another place.

## When the door separates people

- **Coming through after.** A reader who comes through a held door and cannot follow keeps their own door, as before. Once the
  crossing's fade has passed, they are told why, in the words above.
- **The group splits.** The first through has friends at the door, some cannot share what lies behind it, and nothing is left that
  all of them could go into. Then the group splits, as before, and now:
  - the first through is told, after the crossing: "Not everyone at the door could go where it leads. The others will find their
    own way." For a scene that is one reader's at a time: "What is behind this door is for one at a time. The others will find
    their own way.";
  - each friend at the door who cannot follow is told at once: "Ana went where you cannot follow. Your door will lead you
    elsewhere." For a scene that is one reader's at a time: "Ana went where only one can go. Your door will lead you elsewhere.";
  - a friend who could have followed is told nothing, and sees the motes.

When the House can find a scene everyone at the door can share, it still deals that instead, and nobody is separated.

## Tests

`CoopTests.friendsAtAHeldDoorAreToldWhetherTheyCanFollowAndWhyTheySplit` (batch coop_cues270), at a real hall door, checks:
- **At the held door.** After one reader's crossing into the nursery:
  - a friend beside the door is told the way is still open;
  - a friend who finished the nursery is told they have already been there;
  - nobody fourteen blocks off, nor the one who went, is told anything.
- **Coming through after.** The finished friend, coming through after, is owed the line that says why.
- **The split.** With nothing left that all could share, the first through keeps the nursery and is owed "find their own way".
  The friend who cannot follow is owed the line naming who went; the friend who could follow is owed nothing.

The existing follow case (`friendsThroughOneDoorShareTheSceneAndAFriendWhoFinishedItGoesOnAlone`) passes with the cues in place.
