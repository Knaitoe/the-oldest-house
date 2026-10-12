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

## Return the evidence

Include the printed commit SHA, operating system, CPU model and total RAM. Zip `build/live-proof/`, `run-live/server/logs/`, `run-live/a/logs/`, `run-live/b/logs/` and any `run-live/*/crash-reports/` directories. A successful run writes `build/live-proof/passed.txt`; failures need the logs even if no result file was written.

Success on another host would help diagnose the CI watchdog; it does not by itself make an incomplete CI run a verified release. The Python launcher currently uses POSIX process controls. A Windows launcher needs adaptation before those instructions can be used there.
