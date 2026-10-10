"""CI proof: a loopback dedicated server, two real clients, then the same B profile reconnects."""
from pathlib import Path
import os
import signal
import subprocess
import time

os.environ.setdefault("ALSOFT_DRIVERS", "null")

ROOT = Path(__file__).resolve().parents[1]
os.chdir(ROOT)
report = ROOT / "build/live-proof"
report.mkdir(parents=True, exist_ok=True)
server = ROOT / "run-live/server"
server.mkdir(parents=True, exist_ok=True)
(server / "eula.txt").write_text("eula=true\n")
(server / "server.properties").write_text('server-ip=127.0.0.1\nserver-port=25578\nonline-mode=false\nview-distance=6\nsimulation-distance=3\nspawn-protection=0\nspawn-monsters=false\nmax-players=3\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\nsync-chunk-writes=false\n')
for role in ("a", "b"):
    folder = ROOT / "run-live" / role
    folder.mkdir(parents=True, exist_ok=True)
    (folder / "options.txt").write_text("pauseOnLostFocus:false\nrenderDistance:6\nsimulationDistance:5\nmaxFps:30\nenableVsync:false\nguiScale:2\ntutorialStep:none\n")

processes = []
logs = []
def launch(task, name):
    log = open(report / f"{name}.log", "w")
    logs.append(log)
    p = subprocess.Popen(["gradle", task, "--no-daemon", "--max-workers=1", "-Dorg.gradle.jvmargs=-Xmx256m"], stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
    processes.append(p)
    return p

def wait_for(check, seconds, context):
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        if check():
            return
        for p in processes:
            if p.poll() is not None and p.returncode != 0:
                raise RuntimeError(f"Native process failed during {context}: {p.returncode}")
        if (report / "failed.txt").exists():
            raise RuntimeError((report / "failed.txt").read_text())
        if host.poll() is not None and not (report / "passed.txt").exists():
            raise RuntimeError(f"Dedicated server stopped before proof completion during {context}")
        time.sleep(2)
    raise RuntimeError(f"Timed out waiting for {context}")

try:
    subprocess.run(["gradle", "classes", "prepareProofServerRun", "prepareProofARun", "prepareProofBRun", "--no-daemon", "--max-workers=2"], check=True)
    host = launch("runProofServer", "server")
    wait_for(lambda: "Done (" in (server / "logs/latest.log").read_text(errors="replace") if (server / "logs/latest.log").exists() else False, 180, "dedicated server startup")
    first = launch("runProofA", "client-a")
    # Each JVM extracts LWJGL natives during first window startup. Let A reach
    # its real socket login before B starts extracting the same native libraries.
    # The expedition itself still requires both live clients together.
    wait_for(lambda: "joined the game" in (server / "logs/latest.log").read_text(errors="replace"), 180, "first native client window and socket login")
    second = launch("runProofB", "client-b-first")
    wait_for(lambda: (report / "restart-b.txt").exists() and second.poll() is not None, 360, "both clients' first expedition and native logout")
    reconnected = launch("runProofB", "client-b-reconnected")
    wait_for(lambda: (report / "restart-b-leak.txt").exists() and reconnected.poll() is not None, 240, "personal burn, two note rooms and logout inside a leak")
    recovered = launch("runProofB", "client-b-leak-reconnected")
    wait_for(lambda: (report / "passed.txt").exists() and (report / "B-reconnected.png").exists(), 420, "same-profile recovery, native shared hunts and rendered proof")
    for p in processes:
        p.wait(timeout=90)
        assert p.returncode == 0, p.returncode
    for shot in ("A-first.png", "B-first.png", "A-embers.png", "B-embers-reconnected.png", "B-reconnected.png", "A-leak.png", "B-leak.png", "B-leak-first.png", "A-stacy-door.png", "B-stacy-door.png", "A-leaf-cover.png", "B-leaf-cover.png", "A-slasher-leaves.png", "B-slasher-leaves.png", "A-private-elk.png", "B-private-elk.png", "A-private-leaves.png", "B-private-leaves.png", "A-porthole.png", "B-porthole.png"):
        assert (report / shot).stat().st_size > 10000, shot
    for role in ("A", "B"):
        for view in ("actual-exposure", "camera-view", "developed-frame", "ending-hidden", "ending-personal", "ending-collected"):
            assert (report / f"{role}-{view}.png").stat().st_size > 500, (role, view)
    assert (report / "camera-maps.txt").is_file()
    for role in ("A", "B"):
        for view in ("trailer-arrival", "trailer-camp", "trailer-fear", "trailer-voice"):
            assert (report / f"{role}-{view}.png").stat().st_size > 10000, (role, view)
    assert (report / "trailer-arrival.txt").is_file()
    assert (report / "trailer-camp.txt").is_file()
    assert (report / "trailer-night.txt").is_file()
    for role in ("A", "B"):
        assert (report / f"{role}-trailer-sounds.txt").stat().st_size>100
    # Require clean native movement validation during the actual crossing
    # and return, even when a later correction would recover a bad packet.
    native_log = (server / "logs/latest.log").read_text(errors="replace")
    crossing = native_log.split("LIVE EXPEDITION phase 2", 1)[1].split("LIVE EXPEDITION phase 5", 1)[0]
    assert "moved too quickly!" not in crossing and "moved wrongly!" not in crossing, crossing
    leaks = native_log.split("LIVE EXPEDITION phase 9",1)[1]
    assert "moved too quickly!" not in leaks and "moved wrongly!" not in leaks, leaks
    print((report / "passed.txt").read_text(), flush=True)
finally:
    for p in processes:
        if p.poll() is None:
            os.killpg(p.pid, signal.SIGTERM)
    for log in logs:
        log.close()
    for name in ("server", "client-a", "client-b-first", "client-b-reconnected", "client-b-leak-reconnected"):
        log = report / f"{name}.log"
        if log.exists():
            selected = [line for line in log.read_text(errors="replace").splitlines() if any(word in line for word in ("LIVE EXPEDITION", "LIVE PORTHOLE", "Exception", "Caused by", "ERROR", "FAILED"))]
            print(f"{name}:\n" + "\n".join(selected[-25:]), flush=True)
