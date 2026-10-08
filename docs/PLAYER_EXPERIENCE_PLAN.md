# The player experience plan

Status: **partly integrated in the 0.4.56 candidate.** The original proposal followed 0.4.49; its release assignments have been rebased after 0.4.55. Implemented source work is recorded below and in [the integration record](PLAYER_EXPERIENCE_INTEGRATION_0_4_56.md). Exact-head validation is required before release. Where it proposes changing an established rule, the change is listed under [Decisions for the owner](#decisions-for-the-owner) and nothing changes until it is decided.

## Why this plan exists

The House has a great deal of content:

- 79 places and 43 personal Witness stories;
- three endings;
- 108 letters and 64 stair sheets;
- five note scenes and a 2,560-block staircase;
- companions and full multiplayer.

Over the last several releases, new content has arrived faster than the existing content has been played, polished and connected. Each code review finds 10–15 defects, mostly where a new system meets old saves or a second player. The native test suite is near 400 cases. AGENTS.md now carries more than forty paragraphs of preservation rules.

None of that is wrong, but the cheapest large improvement to the experience now is **not more rooms**. It is:

- making the existing journey legible, fair and well paced;
- watching real people play it.

This plan sets out how.

## Where the game stands

These facts come from the code (configuration defaults in `HouseConfig`, `OpeningConfig`, `LabyrinthPacing`, `LabyrinthDealer` and `WitnessAccount`). The time estimates are mine and are not measured. Measuring them is the first job of the playtest log in [workstream A](#a-stabilize-and-watch-people-play).

### The journey

1. **Settling in.** The opening becomes eligible once a player has played for at least three days and slept at least two nights. Navidson's letter arrives the next morning; Hillary and the House follow.
2. **The manor.** The room between rooms can first appear on the House's third morning. It has a 50% chance, plus 25% for each morning it does not appear, and needs two mornings after the rugs change. Two subtle shifts must then happen before the impossible hallway can open, again at 50% plus 25% a morning.
3. **The labyrinth.** No story is offered in the first six crossings (`STORY_DEPTH = 6`).
   - **Story chance:** 22% per fresh route at depths 6–9, then 38%, plus 12% per dry deal. A story is guaranteed after six dry deals, and stories are at least two visits apart.
   - **Hazards:** 24%, 36% and 48% by depth.
4. **The staircase.** It is offered from depth 20 (`STAIRCASE_DEPTH`) and descends through five book-fed hearths.
5. **The endings.**
   - **Witness:** needs 33 of the 43 personal resolutions, across at least two kinds. The pool has five kinds: survival (14 stories), understanding (12), memory (8), connection (6) and release (3).
   - **Killed and Wounded:** the other two endings come from the fight at the cell.

### What that probably means in play

- **Before the labyrinth:** a new player probably spends a week or more of in-game days (several real hours) in the Overworld and manor before the labyrinth opens.
- **To the Witness ending:** reaching it takes at least one story-bearing crossing per resolution. With pacing, several stories needing more than one visit, and retreats, that is probably 100–150 crossings. My guess is 15–30 hours.

That can be right for a long-form horror mod, but only if the player always knows they are making progress.

### What the player can see of their progress

- **The account (0.4.56):** the personal book still arrives at the first resolution. It now includes a prose count, unfinished personal stories, broad progress milestones derived from the actual quota, and a hint when only one kind of ending has been heard. Carried accounts update on a new beginning, a resolution and login; another owner's book is unchanged.
- **The map:** the saved personal route map (`LabyrinthDealer.arriveAt`) has no in-game form.
- **Unfinished stories (0.4.56):** a dormant door names the room and explains the walk-away-and-return action.

## Principles

Every proposal below follows these, and they are meant to guide later work too.

1. **Never stuck, always told.** Confusion about the fiction is horror; confusion about the controls is a bug. Every refusal tells the player:
   - something they can understand;
   - what might change it;
   - in the House's own voice where possible.
2. **Fear through restraint.** Scares are rationed so each lands. The strongest tools are things that change unseen, sound and darkness, and they are spent deliberately.
3. **The House remembers you.** It already records a great deal of honest, personal fact. It should use more of it in the world, so that hours of play feel personal.
4. **Shared world, personal story.** Multiplayer moments make players need and watch each other, but personal credit stays personal and every story keeps a solo path.
5. **Existing rules stand.** These still apply:
   - personal, finite and honest progress;
   - spectators earn nothing;
   - in-place upgrades wait for vacancy and never refill or rebuild;
   - native identities are preserved;
   - the Witness quota is derived from `WitnessAccount.Story`.

## Workstreams

Each workstream gives the problem, the evidence, the proposals, how we'll know it worked, and a rough size (S: about a day, M: a few days, L: a week or more).

### A. Stabilize, and watch people play

**Problem.** Defects keep pace with features, and tuning is done by intuition: no one outside the project has been watched playing.

**Proposals.**

1. **Content freeze for two releases.** Reserve two future releases for repairs, legibility, pacing and accessibility. The original 0.4.50–0.4.51 assignments have already been used; a content freeze remains a proposal, not an established rule. Repairs, legibility, pacing and accessibility only. (S to agree; it saves effort.)
2. **A definition of "releasable."** Every CI job green on the exact head, including:
   - the live two-client proof, which failed on every run for its first day and has passed since d09c1fd;
   - the full suite, with the intermittent shutdown stall resolved or made non-fatal in CI.

   (M; the shutdown stall is diagnosed to generation claims left open at shutdown.)
3. **A playtest log**, opt-in and local only, written by the server. It is specified in [Appendix B](#appendix-b-playtest-event-log). It answers how long each step really takes, where people retreat or die, and where they get stuck. (M)
4. **Playtest rounds.** Two or three people who have never seen the mod each play a solo and a duo session, recorded, using the [playtest kit](#appendix-c-playtest-kit). Run one round per release in this plan. (S per round)

**Done when:** one full release goes out with every job green, and at least one playtest round has been reviewed against the log.

### B. Never stuck, always told

**Problem.** About 140 status messages exist, but many refusals are silent `return false`s, and some messages give no way forward. Recent examples:

- a hearth that did nothing when clicked without a flame (fixed in 0.4.49's review);
- a lost lighter that could never be replaced (recovery deferred; an empty carried inventory does not establish that the finite original is lost);
- "The door sticks." when the House is still carving the room behind it;
- "The door is quiet." for a dormant story.

**Proposals.**

1. **Friction audit.** Inventory every gate and refusal: door crossings, interactions, story steps, staircase phases, note scenes and finale steps. Give each one:
   - a reason;
   - a way forward;
   - a voice that fits the fiction.

   [Appendix A](#appendix-a-friction-inventory-first-pass) starts the inventory. (M)
2. **Message style.** Messages go on the action bar, use about 80 characters at most, and are written in the second person, present tense. Diegetic first ("Something is still settling behind it"), with an actionable clause where the player can act ("Tom carries a lighter"). Never name internal systems.
3. **A soft-lock catalogue.** Every item a story or the staircase needs must have a recovery path or a substitute:
   - lighter: Tom's one-time handoff flag is repaired. Replacement is deferred until original custody and remaining uses can be preserved;
   - House of Leaves: the shelf rebinds it;
   - keys and pickaxes: verify each.

   Each recovery path gets a test. (M)
4. **Repeated-refusal help.** If a player is refused at the same gate three times within a minute, the follow-up message adds one more concrete hint. Tom's radio is the natural voice when the player carries it. (S)

**Done when:** every gate in Appendix A has an agreed message, the soft-lock catalogue has a recovery test for each item, and playtest logs show no repeated-refusal loops longer than a minute.

### C. Progress you can feel

**Problem.** The Witness ending is a long road, and the player cannot see where they are on it.

**Proposals.**

1. **The account shows its gaps.**
   - **A count, in prose:** "I have heard eleven rooms to their end. The account is not whole." No number of total stories is shown, which keeps the mystery.
   - **Begun stories:** stories the player has begun but not finished appear as short, unfinished entries ("A room with a wardrobe. I left before the game ended."). They do not tell the player where to go.
   - **Kinds:** when only one kind has been resolved, a line hints that the account lacks a different kind of ending ("Everything here is about surviving."). Survival is the largest kind, so a player can resolve many stories of that kind and still not qualify.
   - **Kept current:** the account is already given at the first resolution and rewritten after each. The new entries use the same mechanism. (M)
**0.4.56 implementation:** the account now carries the count, unfinished entries and kind hint above. Broad milestones follow the derived quota without revealing the full pool. Only personal beginnings and actual resolutions contribute; spectators and borrowed books confer none.

2. **The survey.** An in-game map of the player's own saved route: a held item or a book of hand-drawn plates showing rooms visited, doors taken, stories finished and dormant doors. It is personal and drawn from data the game already keeps. (L)
3. **Dormant doors that say something.** "The door is quiet" becomes specific without spoiling: "The wardrobe room is quiet. It will open again if you come back to it later." (S)
4. **The quota (decision).** At 33 of 43 the road is long. See [Decisions](#decisions-for-the-owner). Whatever is chosen, the account should make the remaining distance feel finite.

**Done when:** a playtester can say roughly how far through the stories they are, without a command; and nobody in a playtest asks "what am I supposed to do?" more than once per session.

### D. Give each session a shape

**Problem.** The dealer is a fair probability model, but a session needs a deliberate rhythm: quiet, unease, story, breather. Long ordinary stretches between stories risk feeling empty. Conversely, a hazard straight after a story leaves no room to breathe.

**Proposals.**

1. **Breathers.** After a story or hazard resolves, the next fresh deal prefers a quiet room or a short hall. The note scenes, the quiet chair and the hall sounds already belong here. (S)
2. **Small strangenesses in ordinary halls.** Build a library of cheap, unseen changes, each like the quiet chair:
   - a painting turned to the wall;
   - a door now ajar;
   - a rug's corner folded;
   - a light that was on is off.

   They happen roughly once in three ordinary halls. Each is vacancy-gated, waits until no camera is within 48 blocks, honours player edits and never refills. (M)
3. **Callbacks.** After a player resolves a story, a quiet echo of it can appear in a later ordinary hall, for that player only. These are scenery and never items: the cracked plate on a sideboard, a single sock on a radiator. The House remembering you is the strongest horror this game has. (M)
4. **Session escalation.** Within one expedition the House grows more active (more cues, more strangenesses); returning to the manor resets it. This gives each trip an arc and makes the manor a real refuge. (M)
5. **Time to the labyrinth.** Shorten the opening's minimum wait so a new player reaches the hallway in one or two evenings of play, not a week. See [Decisions](#decisions-for-the-owner). (S to tune)

**Done when:** logs show a story or strangeness at least every ten minutes of labyrinth play, and no hazard straight after a story; playtesters describe expeditions as having a beginning, middle and end.

### E. Fewer, better scares; more choices

**Problem.** Scares are only as strong as the choices around them. Several of the House's best tools are fair and honest, and they are under-explained:

- retreat is always open until a story commits;
- light is finite (torches, the lighter's 64 uses, the staircase's darkness);
- hiding works where it is offered.

**Proposals.**

1. **Make retreat a known choice.** The first time a player is in a story room, a single line tells them they can still leave. After that, nothing. (S)
2. **A scare budget.** Count active scares per expedition (growls, lunges, darkness events) and space them. Prefer sound first, sight second. (M, after the log exists)
3. **Light as a resource that matters.** Light matters in the labyrinth's dark stretches and the staircase: a few places where carrying light is the decision, not decoration. (M)

**Done when:** playtesters can say what they chose to do in their most frightening moment, and say retreat was available.

### F. Multiplayer moments

**Problem.** The engineering keeps progress personal and fair, and that is right. But the best co-op horror comes from players knowing different things, and from separation and reunion.

**Proposals.**

1. **Asymmetric sightings.** One player sees what another does not. The note scenes already do this: the reader's still body stays on the stair. Extend it to a few places where only the person in front sees a change. (M)
2. **Optional two-person beats.** One holds a door while the other reads; one stands at the lever while the other crosses. These are never required for credit; every story keeps its solo path. (M)
3. **Separation and reunion.** Shared routes already share halls. Make losing each other a feature:
   - chalk and the trail spool are the tools;
   - footsteps through walls (the sound bridge) are the clue;
   - a reunion line when two players meet again after time apart. (M)

**Done when:** in a duo playtest, both players can describe a moment that only worked because the other was there.

### G. The manor as home

**Problem.** The manor is where the story begins, but between expeditions there is little reason to linger, and little sign of the journey.

**Proposals.**

1. **A keeping shelf.** A display block in the manor holds the player's own story yields (items already marked by `VignetteYields`). Placing them is the player's choice; nothing is copied or consumed. (M)
2. **The manor answers.** Small, honest changes in the manor reflect personal progress: a letter left on the hall table after a resolution, a companion waiting at the door after a long expedition. These stay vacancy-gated and personal where the manor is shared. (M)
3. **Rest that matters.** Returning to the manor resets session escalation ([D4](#d-give-each-session-a-shape)). A first return after a death or a story could carry a short, quiet line. (S)

**Done when:** playtesters return to the manor between expeditions without being told to.

### H. Accessibility and comfort

**Problem.** So much is told through sound and darkness, which excludes some players.

**Findings.**

- **Subtitles:** 10 of the 56 sound events have none: `hotel.piano`, `hotel.orchestra`, `hotel.bell`, `hotel.hose`, `staircase.descent`, `leak.drip`, `leak.clock`, `leak.rain`, `leak.radio` and `leak.bag`.
- **Camera shake:** it already follows the screen-effects setting.

**Proposals.**

1. **Subtitles for every cue.** Each subtitle is written to describe what is heard, not what it means ("Distant piano", not "The ghost plays"). A CI check fails when a sound event has none. (S; **done**: the ten missing subtitles are written and the package check enforces it. The blind stretch's bells now show their true direction in subtitles, like their positional audio.)
2. **A darkness check.** Review the darkest scenes (the staircase, the blind stretch, the Goatman vigil) at Minecraft's default brightness. Then decide whether to offer a client "low vision" option that raises the black fog's minimum slightly without lighting the scene. (S, plus a decision)
3. **Flashes and fades.** List every flash, fade and flicker (screen fades, caption flicker, the glitched "don't"), and make sure each respects the screen-effects setting. (S)
4. **Readable writing.** The handwriting fonts are part of the fiction, but long letters are hard to read for some players. A client option could render books in a plain font, keeping the colours and layout. (M)

**Done when:** every sound event has a subtitle (CI-enforced), and the darkness and flash reviews are recorded.

## Roadmap

| Release | Theme | Contents |
|---|---|---|
| 0.4.56 candidate | **Stability, clarity and personal progress** | Integrate the reviewed save-preservation repairs, all subtitles, first door/hearth messages, corrected opt-in logging and measurements, and the account's count, unfinished entries, milestones and kind hint. |
| Next clarity pass | **Finish the friction audit** | Complete item-custody/recovery decisions, remaining silent gates and retreat explanation; review the first fresh solo/duo playtest round. Decide quota/opening changes from measured play. |
| Later rhythm pass | **Give expeditions a shape** | Breathers, small unseen changes, callbacks and session escalation; tune from reliable logs and a second playtest round. |
| Later shared/home pass | **Together and home** | Optional co-op moments, keeping shelf/manor responses, survey map and reading/accessibility options, preserving solo paths and established scene darkness. |

Future release numbers depend on playtest findings. A content freeze and changes to quota, opening wait or darkness remain owner decisions.

## Measures

The playtest log yields these, per player and per session:

- Time from join to the letter, to entering the manor, to the hallway, to the first labyrinth crossing, and to the first story.
- Stories started and resolved per hour of labyrinth play. Time between story or strangeness beats.
- Retreats, deaths and their places. How many expeditions end in each.
- Repeated refusals: the same gate three or more times within 60 seconds.
- Sessions that end inside the labyrinth versus at the manor.
- Questionnaire scores ([Appendix C](#appendix-c-playtest-kit)).

### Targets (initial; revise after round 1)

| Measure | Target |
|---|---|
| First story | within 25 minutes of first entering the labyrinth |
| Beats | no ten-minute stretch of labyrinth play without a story, strangeness or note scene |
| Stuck | no repeated-refusal loop longer than one minute |
| Progress | every playtester can say roughly how far through the stories they are |

## Decisions for the owner

These change established rules or the game's intent. Nothing changes until they are decided.

1. **The Witness quota.** Today it is 75% of the pool, rounded up: 33 of 43.
   - (a) Keep it.
   - (b) Lower it to 60% (26 of 43).
   - (c) Keep 75%, but count each of the five kinds separately with a lower per-kind floor.

   I recommend deciding after playtest round 1 shows real hours per resolution.
2. **The opening wait.** Today it is three days since joining and two nights slept, then several mornings in the manor. Option: one day and one night, with the manor steps unchanged.
3. **The content freeze.** Two releases with no new stories, as proposed. Agree, shorten or lengthen.
4. **A darkness floor.** Whether to offer a client low-vision option that softens the black fog.
5. **Optional co-op beats.** Agree that two-person beats are never required for credit. This plan assumes they are not.

## Constraints every change must keep

- **Progress:**
  - personal, finite and honest;
  - spectators and copies confer nothing;
  - saved originals are immutable;
  - completed endings and Witness evidence are preserved.
- **World changes:**
  - in-place changes are vacancy- and camera-gated, honour player edits, and never refill or rebuild;
  - native actor and companion identities, health and orders are preserved.
- **The pool:** the Witness quota stays derived from `WitnessAccount.Story` unless decision 1 changes the rule itself, which AGENTS.md must then record.
- **Every release:** the full native suite, the focused suites and the client proofs, verified on the exact head.

## Appendix A: friction inventory (first pass)

This lists what the first pass found. The full audit in workstream B completes it.

### Door refusals (`LabyrinthDoors`)

| Situation | Today | Proposed |
|---|---|---|
| The labyrinth core is still being carved | "The door sticks." | **Done:** "The door sticks. Something is still settling behind it." At the same physical door, a third try within a real-time minute says "It will give. Try another door, or come back to this one in a minute." |
| The destination place is still being carved | "The door sticks." (unless rerouted) | **Done:** the same messages; rerouting is unchanged. |
| A dormant story door | "The door is quiet. Leave its approach and find it again." | **Done:** names the room without spoiling it, and says what wakes it: "The wardrobe is quiet behind this door. Walk a little way off, then come back." |
| A locked door during hide-and-clap | "The door is locked." | **Done:** "The door won't open until the game is over." |
| Somebody's room (a permanently locked door) | "The door is locked." | **Done:** "The door is locked. Someone lives here." |

### Stories and items

| Situation | Today | To verify |
|---|---|---|
| Caver crack | "The crack needs a pickaxe. There is one in the camp barrel." | That any pickaxe works, and that the barrel's single pickaxe taken by one player does not block another. |
| Hotel service door | "The service door needs the master key from the maze." | That the key has a recovery path if lost. |
| Drowned Town church | "The church door needs its key. The school kept it." — shown even when the player holds the key but the essays are not yet dried. | **Done in 0.4.56 source:** before the second visit and the three dried essays, it now says "The church door won't move yet. The school's essays come first." **Found:** the key is placed on the school desk once (`KeyPlaced`). A key lost before the church is unlocked has no recovery path. The church is shared state, so the fix (re-placing the key when the desk is empty and the church is still sealed) needs an owner decision. |
| Harrigan's phone | "No answer." once a day | That the player understands the daily limit. |
| Staircase hearth without a flame | (fixed) "The hearth needs a flame. Tom carries a lighter." | — |
| Lost lighter | Tom gives one finite original; the handoff dialogue flag is repaired. | Recovery is deferred: the original may be stored, dropped or held by a peer. Repeated interactions must not mint replacements. |

### Silent refusals to find

Interaction handlers that `return false` without a message when the player has clearly tried something. Candidates:

- staircase leaf binding;
- note-scene chores;
- finale lectern steps;
- companion commands;
- chalk on unsupported surfaces.

## Appendix B: playtest event log

**Status: implemented in the 0.4.56 candidate** (`house/PlaytestLog.java`, `tools/summarize_playtest_log.py`).

**Purpose.** Measure the journey without guessing.

**Switching it on.** In the server config file `the_oldest_house-house-server.toml`, section `[playtest]`, set `playtestLog = true`. It is off by default. The server then appends one JSON object per line to `<world>/the_oldest_house/playtest-log.jsonl`, on its own thread. Nothing is ever sent anywhere, and nothing in play depends on the log. `playtestLogHashIds` (on by default) replaces player UUIDs with a short one-way hash, so a log can be shared.

**Reading it.** `python3 tools/summarize_playtest_log.py <world>/the_oldest_house/playtest-log.jsonl` prints per-player and per-connection measures in wall-clock minutes. Offline intervals and manor breaks are excluded from labyrinth hours. Interrupted sessions count only through the last observed event and are labelled incomplete. Legacy logs without occupancy observations are labelled estimates:

- when each opening stage, the first labyrinth arrival, the first story room and the first resolution happened;
- arrivals, story rooms entered and stories resolved per hour of labyrinth play;
- the longest quiet stretch, including leading/trailing and zero-beat stretches, excluding presence inside an unfinished story or personal note scene;
- retreats from unfinished stories, by place;
- deaths, by place and cause;
- refusals by physical gate and connection, counting each separate episode of three or more attempts within 60 seconds;
- note scenes finished or left;
- where sessions ended.

**Every event records** wall-clock time, game time, the player (hashed by default), an opaque connection identity, actual dimension, place, depth, spectator status, expedition occupancy, activity and the event name. Location changes are sampled once a second and a heartbeat records continuing presence every 30 seconds. Nothing depends on the logger.

Additional fields:

| Event | Fields |
|---|---|
| `session_start` / `session_end` | Explicit connection boundaries; logging enabled mid-session starts a new observed segment. |
| `location` | Dimension/occupancy/activity changes and continuing-presence heartbeat. |
| `opening_stage` | stage, written when it changes |
| `arrive` | place, kind, story (true for a Witness story room), depth |
| `story_leave` | place, resolved, depth: leaving a story room by its entry door; unresolved means a retreat |
| `story_resolve` | story, kind, outcome, count, kinds, ready (Witness requirement met) |
| `death` | place, cause, depth |
| `refused` | gate and actual site (dimension plus coordinates where known); includes construction/dormant/held/resident doors, hearth refusals and the church |
| `fire_lit` | index |
| `note_scene_enter` / `note_scene_leave` | index; on leaving: finished, recovered, seconds |
| `phase` | finale phase, written when it changes (this includes the endings: `locked_out`, `escaped`, `witnessed`) |

**Not yet logged:** manor mornings and shifts, leaf binding, newly perceived strangenesses, and refusals at other gates. Add each as the friction audit reaches it, through `PlaytestLog.refused(player, gate)`.

## Appendix C: playtest kit

### Who and how

- Before the session, set `playtestLog = true` on the test server ([Appendix B](#appendix-b-playtest-event-log)). Afterwards, run the summarizer and keep its output with the recording and questionnaires.
- Two or three people who have not seen the mod. One plays solo, then two play together.
- Record the screen and voice. Thinking aloud is welcome, but not required.
- **Before:** explain only how to install the mod, and that it is a slow horror story about a house.
- **During:** the observer does not help unless asked twice, and notes the time and the moment.

### Observer notes (one line each)

- When did they first look confused, and about what?
- When did they first look frightened, and what caused it?
- Did they retreat? Why?
- Did they read the letters and sheets, or skip them?
- Did they ever say "what am I supposed to do?"
- Where did they stop the session?

### Questionnaire (after each session; 1–5 unless stated)

1. How often did you know what you could do next?
2. How often did you feel the game was unfair or broken?
3. How frightening was it at its most frightening? Describe that moment.
4. Did you feel you were making progress? How far through do you think you are?
5. Which room or moment will you remember? (free text)
6. Was there a stretch that felt empty or repetitive? Where? (free text)
7. Did you read the letters and sheets? Did they matter to you?
8. (Duo) Was there a moment that only worked because the other player was there?
9. Would you play another session? Why or why not?
10. Anything else? (free text)
