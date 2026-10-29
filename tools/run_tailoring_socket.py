"""Two real native windows, owner-only props, B reconnect and original card-page input/render."""
from pathlib import Path
import os
import signal
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
os.chdir(ROOT)
os.environ.setdefault("ALSOFT_DRIVERS", "null")
world = ROOT / "run-tailoring-live"
report = ROOT / "build/tailoring-live-proof"
if (world / "server/world/level.dat").exists() or any((report / name).exists() for name in ("passed.txt", "failed.txt", "restart-b.txt")):
    raise SystemExit("Use a fresh isolated tailoring proof world; preserve the existing world and report before rerunning.")
report.mkdir(parents=True, exist_ok=True)
(world / "server").mkdir(parents=True, exist_ok=True)
(world / "server/eula.txt").write_text("eula=true\n")
(world / "server/server.properties").write_text('server-ip=127.0.0.1\nserver-port=25578\nonline-mode=false\nview-distance=5\nsimulation-distance=3\nspawn-protection=0\nspawn-monsters=false\nmax-players=3\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\nsync-chunk-writes=false\n')
for role in ("a", "b"):
    folder = world / role
    folder.mkdir(parents=True, exist_ok=True)
    (folder / "options.txt").write_text("pauseOnLostFocus:false\nrenderDistance:5\nsimulationDistance:3\nmaxFps:30\nenableVsync:false\nguiScale:2\ntutorialStep:none\n")
processes = []
logs = []
flags = ["-PtailoringSocketSmoke"]


def launch(task, name):
    log = open(report / (name + ".log"), "w")
    logs.append(log)
    process = subprocess.Popen(["gradle", task, "--no-daemon", "--max-workers=1", "-Dorg.gradle.jvmargs=-Xmx256m"] + flags, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
    processes.append(process)
    return process


def text(path):
    return path.read_text(errors="replace") if path.exists() else ""


def wait_for(check, seconds, label):
    until = time.monotonic() + seconds
    while time.monotonic() < until:
        if check():
            return
        if (report / "failed.txt").exists():
            raise RuntimeError(text(report / "failed.txt"))
        for process in processes:
            if process.poll() is not None and process.returncode:
                raise RuntimeError(f"Native process failed during {label}: {process.returncode}")
        if host.poll() is not None:
            raise RuntimeError(f"Native host stopped during {label}")
        time.sleep(1)
    listing = subprocess.run(["jcmd", "-l"], capture_output=True, text=True, timeout=5)
    (report / "timed-out-jvms.txt").write_text(listing.stdout + listing.stderr)
    for line in listing.stdout.splitlines():
        parts = line.split(maxsplit=1)
        if len(parts) == 2 and parts[0].isdigit() and any(name in parts[1] for name in ("devlaunch", "BootstrapLauncher", "modlauncher")):
            dump = subprocess.run(["jcmd", parts[0], "Thread.print", "-l"], capture_output=True, text=True, timeout=8)
            (report / ("timed-out-threads-" + parts[0] + ".txt")).write_text(dump.stdout + dump.stderr)
    raise RuntimeError("Timed out: " + label)


try:
    subprocess.run(["gradle", "classes", "prepareProofServerRun", "prepareProofARun", "prepareProofBRun", "--no-daemon", "--max-workers=2"] + flags, check=True)
    host = launch("runProofServer", "server")
    wait_for(lambda: "Done (" in text(world / "server/logs/latest.log"), 180, "dedicated native host startup")
    wait_for(lambda: "TAILORING SOCKET fixture ready" in text(world / "server/logs/latest.log"), 300, "the isolated scenes built before any client connects")
    first = launch("runProofA", "client-a")
    wait_for(lambda: "OTHProofA joined the game" in text(world / "server/logs/latest.log"), 180, "the first actual client login")
    second = launch("runProofB", "client-b-first")
    wait_for(lambda: (report / "restart-b.txt").exists() and second.poll() is not None, 300, "both original sink renders and actual B logout")
    reconnected = launch("runProofB", "client-b-reconnected")
    wait_for(lambda: (report / "passed.txt").exists(), 300, "the same-profile reconnect, both original vanity renders and real card pages")
    for process in processes:
        process.wait(timeout=90)
        assert process.returncode == 0, process.returncode
    for role in ("A", "B"):
        for view in ("sink", "sink-reconnected", "vanity", "card", "mirror"):
            assert (report / f"{role}-{view}.png").stat().st_size > 1000, (role, view)
        assert len((report / f"{role}-sink-original.txt").read_text()) == 36
    assert (report / "B-before-reconnect.png").stat().st_size > 1000
    assert (report / "reconnect-originals.txt").stat().st_size > 100
    assert "All dimensions are saved" in text(world / "server/logs/latest.log")
    print((report / "passed.txt").read_text(), flush=True)
finally:
    for process in processes:
        if process.poll() is None:
            os.killpg(process.pid, signal.SIGTERM)
    for log in logs:
        log.close()
    for name in ("server", "client-a", "client-b-first", "client-b-reconnected"):
        lines = [line for line in text(report / (name + ".log")).splitlines() if any(word in line for word in ("TAILORING", "Exception", "Caused by", "ERROR", "FAILED"))]
        print(name + ":\n" + "\n".join(lines[-20:]), flush=True)
