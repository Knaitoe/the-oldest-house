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

## Return the evidence

Include the printed commit SHA, operating system, CPU model and total RAM. Zip `build/live-proof/`, `run-live/server/logs/`, `run-live/a/logs/`, `run-live/b/logs/` and any `run-live/*/crash-reports/` directories. A successful run writes `build/live-proof/passed.txt`; failures need the logs even if no result file was written.

Success on another host would help diagnose the CI watchdog; it does not by itself make an incomplete CI run a verified release. The Python launcher currently uses POSIX process controls. A Windows launcher needs adaptation before those instructions can be used there.
