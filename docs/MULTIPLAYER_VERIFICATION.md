# Running the real two-client verification

The useful comparison is the same automated expedition on another machine. It launches a dedicated server and two actual Minecraft clients, then reconnects the same second profile twice. It checks native movement, separate personal records, collected originals, shared actors, private effects and rendered views. The proof starts in the hallway and staircase; its current watchdog failure occurs before hotel gameplay.

## Linux with an existing graphical desktop

Have Java 21, Gradle 9.2.1, Python 3 and Git available. Use a fresh checkout so the disposable proof directories cannot reuse an earlier test world:

```bash
git clone --branch feat/stanley-hotel-0.4.86 https://github.com/Knaitoe/the-oldest-house.git the-oldest-house-multiplayer-check
cd the-oldest-house-multiplayer-check
git rev-parse HEAD
python3 tools/run_live_expedition.py
```

The clients control themselves. Keep both windows running and let the automation perform its logouts and reconnects. The dedicated proof server listens only on `127.0.0.1:25578`; no public server or additional Minecraft accounts are needed for this isolated test.

To reproduce CI's software rendering on Linux instead, have Xvfb and Mesa installed and replace the final command with:

```bash
LP_NUM_THREADS=1 LIBGL_ALWAYS_SOFTWARE=1 xvfb-run -a python3 tools/run_live_expedition.py
```

The thread limit applies to the software renderer, not the server or its watchdog. The server remains at a three GiB maximum heap and the same view/simulation distances. Every gameplay assertion, screenshot and both reconnects remain required. The full 558-case GameTest job separately has a sixty-minute process budget because the previous forty-minute run was still advancing when it was stopped.

## What the proof holds still

Two places in the expedition keep an actor where it is so that both clients' cameras can have it. Neither is in the game.

- **The private elk hunters** (phase 19) are held at one spot while both clients receive and pick them.
- **The Goatman at the trailer's two west panes** (phases 35 and 36, since run 1605). Each client must have its own shape and
  two of its children's faces through the front and the middle glass. That is three of the four times it comes to those panes
  in its two laps, so one pane straight after another, and it crosses between them at twice a reader's walk. A client with
  slow frames had no start on it and found the second pane already left. Its own wait at a west pane is now kept until the
  cameras are ready for it to go: at the front pane until both clients stand at the middle one's places, at the middle pane
  while either still stands there. No leg of its laps is skipped, the body is not moved, its voice keeps its own clock, and a
  wait is given up after fifteen seconds. The clients also take their places at the glass before dark, once they have the
  frame of the quiet.

When a phase times out, the message names which clients had acknowledged it. Each client's
`<role>-trailer-night-status.txt` says what it has of the windows so far.

## The clients' heap

The two proof clients run with a 2 GB heap, the figure the game's own launcher gives a player. Until run 1606 they had
1,100 MB, set when the pack was a fraction of its size. In that run both clients ran out of it at the same moment, taking in
the manor's chunks at the start of the manor view (phase 38): the interior is 254 sections high, and every chunk a client
holds there costs it all 254. The host keeps its 3 GB.

Measured on a full local run with the collector's log on (`-Xlog:gc`): once it is in the world a client keeps between
1,050 and 1,165 MB after a collection, from its first minute to its last, and the manor view is the top of that. So the
old figure was already the whole of what a client holds. The host keeps between 1,150 and 1,300 MB of its 3 GB.

`-PproofClientHeap=<size>` sets another figure for a run, for a machine with less to give.

## The Goatman's beds

`tools/run_goatman_bed_proof.py` puts two clients in the trailer's west and east beds and takes each one's frame from the
pillow. The host passes it only with both in their beds at once.

Until run 1606 each client left three seconds after its own frame. In that run one client had its frame about five seconds
after the other, so the first had gone before the second confirmed, and the host waited out its limit with nobody there. A
client now stays in its bed until the host has written its verdict. The host's failure message names which clients had
confirmed and which were still connected.

The tool starts its three processes from one exported command each. Gradle 9 lists a run's argument file among its JVM
arguments and Gradle 8 does not; the export now adds it when it is missing, so the check runs under either.

## Return the evidence

Include the printed commit SHA, operating system, CPU model and total RAM. Zip `build/live-proof/`, `run-live/server/logs/`, `run-live/a/logs/`, `run-live/b/logs/` and any `run-live/*/crash-reports/` directories. A successful run writes `build/live-proof/passed.txt`; failures need the logs even if no result file was written.

Success on another host would help diagnose the CI watchdog; it does not by itself make an incomplete CI run a verified release. The Python launcher currently uses POSIX process controls. A Windows launcher needs adaptation before those instructions can be used there.
