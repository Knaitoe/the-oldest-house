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
