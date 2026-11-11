# The project as a whole, 10 November

The owner asked for a step back. This is what the project is by the numbers, what is working, and what I think is going wrong,
in the order I would deal with it. Every figure was counted from the branch on the day.

## What it is now

| | |
|---|---|
| Java | 1,670 files, 243,000 lines, 20 MB. The `labyrinth` package alone is 839 files and 11 MB, in one flat folder. |
| Places | 234 entries in `LabyrinthPlace`. About 86 Witness stories. |
| Tests | 1,263 native game tests in 43 suites; 22 CI jobs; a full run takes about 55 minutes. |
| Art and sound | 4,380 textures, 524 sounds, 6,032 model and state files, drawn by 273 tools. The jar is over 32 MB. |
| Writing | 505 documents, about 800,000 words. The README is 256 KB. |
| Pace | 662 commits since 19 October; 496 of them in the last fourteen days. The branch is 612 commits ahead of `main`. |

## What is working

- **The art is reproducible.** Nearly every texture is drawn by a tool that can also check itself, so nothing is painted by hand
  and lost. A redraw is a code change.
- **A fault gets a test before it gets a fix.** The pool table, the Ghosts camera, the refinery timeout: each was reproduced first.
- **Each reader has their own record.** Per-reader state, per-reader sites and owner-bound objects are why multiplayer mostly holds.
- **The rules for the writing hold.** Speech is not chat, the House does not say how the reader feels, the account follows what
  was done.

## What I think is wrong, in order

### 1. The tests pass and the game is still broken where you look

Today's notes are forty-five things found in one sitting in the vignette that has had the most work. All 1,263 tests were green.
They fall into three kinds, and the tests are blind to all three:

- **What it looks like.** A clock on a window, a sign in place of a pane, a slab in the air, the ground ending past the fence,
  a ladder to nowhere, vanilla signs and plank benches. The tests check that a block is there, not that it looks right.
- **What happens in a real world.** The fixtures build rooms directly and hold every chunk loaded. The game builds from a recorded
  plan and unloads what is far away. The pool table vanished from the bar for that reason; the second Dorsey was made because the
  first had walked out of the loaded area.
- **What only the client shows.** Kelly's line vanishing, the Sheriff's body breathing, the bike's handlebars across the view.

What I would change:

1. **Scene rules as tests, run over every built scene.** Nothing hangs on glass or air (added today for Drisking). Nothing floats.
   Every ladder ends at an opening. No vanilla sign where a custom one is the rule. No edge of the world in sight of walkable ground.
2. **Build scenes in tests the way the game does**, from the recorded plan, by default.
3. **One pass per vignette at a real view distance**, with nothing held loaded.
4. **Eye-level frames along each vignette's route**, rendered and looked at by me before I say it is done. I did this for the
   Ghosts set and Winchester and it found what you would have found.

### 2. Breadth is outrunning finish

There are 234 places. Borrasca, after some twenty-five releases, still gave you forty-five notes. Most vignettes have had a small
fraction of that attention, so the same kinds of fault are almost certainly in all of them.

I would write down what "finished" means for a vignette (its own signs and furniture, people's lines for every state of the
story, an aftermath, a clean eye-level pass) and finish a short list to that standard before starting another. The Pell house
and bike night are good designs and should wait their turn.

### 3. The Witness ending gets longer every time a story is added

It needs three-quarters of the stories: about 65 of about 86 today, many of them several visits long. Each new vignette adds to
it. You have said it should stay the hardest ending and stay at three-quarters, and I have kept that. I would still put a ceiling
on the number, or let whole families of stories count once, before the game is given to anyone else.

### 4. "Verified" no longer means much at this pace

The rule is that a commit is verified only when every job passes on that commit. A run takes 55 minutes and a push cancels the
run before it, so at thirty-five commits a day most versions never get a run of their own. Either pushes go in fewer, larger
batches, or we say plainly that only named checkpoints are verified and tag them.

### 5. There is no stable copy

Everything is on one branch, 612 commits ahead of `main`, under a pull request named for 0.4.125. There is nothing to hand to a
friend that is known to work. I would merge to `main` whenever a checkpoint is green and tag it. That is your call.

### 6. The code is getting hard to change safely

- One folder of 839 files. Lines up to 7,296 characters long; 12,900 lines over 200.
- **2,564 different state keys written as bare strings.** A typing mistake in one makes a new key and no error. A count over
  Borrasca's files found keys that are read and never written. A test that checks every key read is written somewhere is cheap.
- 480 event subscribers and 203 tick handlers. Each runs every tick for every player. Nobody has measured it with four players.

None of this needs a rewrite. I would move a vignette's files into its own folder when they are next touched, add the key
check, and take one profile.

### 7. The documents cannot be read any more

800,000 words is more than anyone will read, including me. The README is a quarter of a megabyte with the newest change on top
of every older one. I would keep the README to one page for a player, keep one changelog, and move the per-release notes into a
history folder.

### 8. Things to decide before anyone else plays it

- **The novel's own text is in the jar** (the poems and the letters, word for word), and several vignettes are named for living
  authors' works. That is fine privately. It is a real question for a public release.
- **Saves.** "New builds only" means every update can break a world in progress.
- **One player.** Several vignettes were built for company; playing them alone is still on the list.

## What I would do next

1. Finish today's notes for Borrasca, with the scene rules added as tests as I go.
2. The window, the comic and the arcade, as you asked.
3. The four changes under point 1, starting with the eye-level pass, on Borrasca first and then the next vignette you choose.
4. Tag a checkpoint when the run is green.

## One day on, 11 November

You asked again for the step back. The review above still stands. This is what has moved since, counted from the branch today.

| | 10 November | 11 November |
|---|---|---|
| Java | 1,670 files, 243,000 lines | 1,702 files, 247,000 lines |
| Tests | 1,263 | 1,271 |
| Textures | 4,380 | 4,622 |
| Documents | 505; the README 256 KB | 527; the README 275 KB |
| Commits | 662; 612 ahead of `main` | 684; 634 ahead |
| A full CI run | about 55 minutes | 63 minutes (run 1516): 227 runner-minutes over 22 jobs, 59 of them the `build` job |
| Witness | about 65 of 86 | 65 of 86: no story was added |

By point:

1. **The tests pass and the game is still broken where you look: partly acted on.** All forty-five notes are closed
   (docs/BORRASCA_PLAYTEST_2026_11_10.md). Two scene rules now run as tests over the whole of Drisking: nothing hangs on glass or
   air (0.4.444), and none of the game's own blocks stands in for something else (0.4.460). Each record since 0.4.457 ends with
   what I could not check. Not done: building scenes in tests the way the game does, a pass at a real view distance, and the
   eye-level frames. I said I would look at those frames before calling a thing done, and for the letter, the supper and the dog
   I looked at pages of their models instead. I have since looked: twenty-two frames from a reader's eye height, on the street,
   at the truck's tailgate (and from a child's height), across the church's lawn as found and as left, at the Corleys' gate and
   porch, at the Garritys' door, and by the first marker. The truck is clear of the street with the name readable, the tables
   show in its bed, the lawn's tables and people are where they should be, the dog is under his tree and on the porch, and the
   letter's corner shows at the foot of the door. I found nothing to fix. People and the dog are drawn as plain columns in these
   frames, so they say nothing about how those look or move.
2. **Breadth is outrunning finish: held.** Nothing new has been started. Every commit since has been Drisking: the forty-five
   notes, then your notes for more life (the window and comic, the arcade, the letter, the supper, the dog). Three vignettes are
   part built and parked: A Place for You (its first evening), Second Person (its winter) and The Ones Already Inside (built, not
   dealt to anyone). Bike night and the Pell house are next by your order.
3. **The Witness ending: unchanged, and yours to decide.**
4. **"Verified": I changed how I push.** I hold a push until the run in progress has finished, so each run verifies a batch: run
   1515 verified 0.4.451 to 0.4.454, and run 1516 verified 0.4.455 to 0.4.458. Nothing is tagged yet.
5. **No stable copy: unchanged, and it costs more than I said.** A new working session starts from a branch cut from `main`,
   which is 0.4.117 of 24 October. It is handed that tree's notes for agents as its instructions: 179 KB of release history,
   with counts and rules that stopped being true weeks ago. The branch's own notes are 46 lines and current. Bringing `main` up
   to a green checkpoint would fix that as well as giving you something to hand to a friend.
6. **The code: unchanged.** The letter, the supper and the dog added about thirty more state keys written as bare strings. The
   key check and the profile with four players are not done.
7. **The documents: worse** by twenty-two files and 19 KB of README.
8. **Before anyone else plays it: unchanged.**

What I would do next, in order:

1. Bike night, as you have ordered, built from the start to the standard in point 2: its own signs and furniture, lines for every
   state of the story, an aftermath, and an eye-level pass before I call it done.
2. The games in Drisking (hide and seek, tag), which are all that is left of your notes for more life.
3. The state-key check in the quick job, in a wait on CI. It is about an hour's work.

Still yours to decide, so that they are not lost: a ceiling on the Witness count; merging to `main` and tagging; whether the
README and the per-release notes are cut down; the novel's text and the borrowed names before any public release.
