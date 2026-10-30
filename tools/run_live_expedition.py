"""CI proof: a loopback dedicated server, two real clients, then the same B profile reconnects."""
from pathlib import Path
import os
import signal
import subprocess
import sys
import time

os.environ.setdefault("ALSOFT_DRIVERS", "null")

ROOT = Path(__file__).resolve().parents[1]
os.chdir(ROOT)
borrasca_only = "--borrasca-only" in sys.argv[1:]
uncanny_only = "--uncanny-only" in sys.argv[1:]
backseat_only = "--backseat-only" in sys.argv[1:]
proof_flags = ["-PbackseatSocketSmoke"] if backseat_only else (["-PuncannySocketSmoke"] if uncanny_only else (["-PborrascaSocketSmoke"] if borrasca_only else []))
proof_root = ROOT / ("run-backseat-live" if backseat_only else ("run-uncanny-live" if uncanny_only else ("run-borrasca-live" if borrasca_only else "run-live")))
report = ROOT / ("build/backseat-live-proof" if backseat_only else ("build/uncanny-live-proof" if uncanny_only else ("build/borrasca-live-proof" if borrasca_only else "build/live-proof")))
if backseat_only and ((proof_root / "server/world/level.dat").exists() or any((report / name).exists() for name in ("passed.txt", "failed.txt", "restart-b.txt"))):
    raise SystemExit("Backseat proof requires a fresh isolated world and report. Preserve the previous run-backseat-live and build/backseat-live-proof folders elsewhere before rerunning.")
report.mkdir(parents=True, exist_ok=True)
server = proof_root / "server"
server.mkdir(parents=True, exist_ok=True)
(server / "eula.txt").write_text("eula=true\n")
# The uncanny preflight's long tunnel is sprinted through natural terrain: at view distance 3, as the other live proofs
# run, the chunks it promotes to ticking around the readers fit a small lease taken before the sprint (run 1345).
view_distance = 3 if uncanny_only else 6
(server / "server.properties").write_text(f'server-ip=127.0.0.1\nserver-port=25578\nonline-mode=false\nview-distance={view_distance}\nsimulation-distance=3\nspawn-protection=0\nspawn-monsters=false\nmax-players=3\nlevel-type=minecraft:flat\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}},{{"block":"minecraft:dirt","height":2}},{{"block":"minecraft:grass_block","height":1}}],"biome":"minecraft:plains"}}\ngenerate-structures=false\nsync-chunk-writes=false\n')
for role in ("a", "b"):
    folder = proof_root / role
    folder.mkdir(parents=True, exist_ok=True)
    (folder / "options.txt").write_text("pauseOnLostFocus:false\nrenderDistance:6\nsimulationDistance:5\nmaxFps:30\nenableVsync:false\nguiScale:2\ntutorialStep:none\n")

processes = []
logs = []
def launch(task, name):
    log = open(report / f"{name}.log", "w")
    logs.append(log)
    p = subprocess.Popen(["gradle", task, "--no-daemon", "--max-workers=1", "-Dorg.gradle.jvmargs=-Xmx256m"] + proof_flags, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
    processes.append(p)
    return p

def capture_native_threads(label="login"):
    """Observe native JVMs during a stall; never retry or bypass the failed action."""
    try:
        listing = subprocess.run(["jcmd", "-l"], capture_output=True, text=True, timeout=5)
        (report / f"{label}-jvms.txt").write_text(listing.stdout + listing.stderr)
        for line in listing.stdout.splitlines():
            fields = line.split(maxsplit=1)
            if len(fields) != 2 or not fields[0].isdigit():
                continue
            if not any(name in fields[1] for name in ("devlaunch", "BootstrapLauncher", "modlauncher")):
                continue
            pid = fields[0]
            dump = subprocess.run(["jcmd", pid, "Thread.print", "-l"], capture_output=True, text=True, timeout=8)
            (report / f"{label}-threads-{pid}.txt").write_text(dump.stdout + dump.stderr)
    except (OSError, subprocess.TimeoutExpired) as exc:
        (report / f"{label}-diagnostics-error.txt").write_text(str(exc))

def wait_for(check, seconds, context):
    deadline = time.monotonic() + seconds
    connecting_at = None
    captured_login = False
    stalls = 0
    stalled_at = None
    while time.monotonic() < deadline:
        if check():
            return
        if context == "first native client window and socket login" and not captured_login:
            client_log = report / "client-a.log"
            if client_log.exists() and "Connecting to 127.0.0.1, 25578" in client_log.read_text(errors="replace"):
                if connecting_at is None:
                    connecting_at = time.monotonic()
                elif time.monotonic() - connecting_at >= 4:
                    captured_login = True
                    capture_native_threads()
        # A native read can block the host long enough to lose both clients.
        # Sample while it is blocked, rather than diagnosing the later offline timeout.
        # The uncanny and Borrasca checks start at their own first phase; each quiet
        # stretch of the log is sampled once, up to three.
        native_log = server / "logs/latest.log"
        if stalls < 3 and context != "dedicated server startup" and native_log.exists():
            quiet_since = native_log.stat().st_mtime
            text = native_log.read_text(errors="replace")
            started = ("phase 2" in text
                       or backseat_only and "BACKSEAT SOCKET phase 1" in text
                       or uncanny_only and "UNCANNY SOCKET phase 1" in text
                       or borrasca_only and "LIVE EXPEDITION phase 54" in text)
            if started and quiet_since != stalled_at and time.time() - quiet_since >= 10:
                stalls += 1
                stalled_at = quiet_since
                capture_native_threads("stalled-host" if stalls == 1 else f"stalled-host-{stalls}")
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
    subprocess.run(["gradle", "classes", "prepareProofServerRun", "prepareProofARun", "prepareProofBRun", "--no-daemon", "--max-workers=2"] + proof_flags, check=True)
    host = launch("runProofServer", "server")
    wait_for(lambda: "Done (" in (server / "logs/latest.log").read_text(errors="replace") if (server / "logs/latest.log").exists() else False, 180, "dedicated server startup")
    first = launch("runProofA", "client-a")
    # Each JVM extracts LWJGL natives during first window startup. Let A reach
    # its real socket login before B starts extracting the same native libraries.
    # The expedition itself still requires both live clients together.
    wait_for(lambda: "joined the game" in (server / "logs/latest.log").read_text(errors="replace"), 180, "first native client window and socket login")
    second = launch("runProofB", "client-b-first")
    if backseat_only:
        wait_for(lambda: (report / "restart-b.txt").exists() and second.poll() is not None, 360, "two physical Backseat entries, private weather/audio, notebook, native locks and actual logout")
        reconnected = launch("runProofB", "client-b-reconnected")
        wait_for(lambda: (report / "passed.txt").exists(), 360, "same-profile original locked seat, native mirror reveals, independent tree impacts and confirmed House returns")
        for p in processes:
            p.wait(timeout=90)
            assert p.returncode == 0, p.returncode
        for role in ("A", "B"):
            for view in ("rainy-approach", "standing-before-lock", "seated", "locked", "locked-reconnected", "reveal", "side-window-early", "side-window-late", "acceleration", "house-return", "mirror-before", "mirror-raised"):
                assert (report / f"{role}-{view}.png").stat().st_size > 100, (role, view)
            assert (report / f"{role}-mirror-pixels.txt").stat().st_size > 40, role
            assert (report / f"{role}-speed-camera.txt").stat().st_size > 40, role
            assert (report / f"{role}-window-motion.txt").stat().st_size > 80, role
        for name in ("A-notebook.png", "A-notebook-kept.png", "B-notebook-unread.png", "B-locked-before-reconnect.png", "B-peer-still-locked.png", "A-peer-still-home.png", "reconnect-originals.txt", "peer-independent.txt"):
            assert (report / name).stat().st_size > 40, name
        native_log = (server / "logs/latest.log").read_text(errors="replace")
        assert "Seeding the house interior in the background." not in native_log
        assert not any(warning in native_log for warning in ("moved too quickly!", "moved wrongly!", "floating too long!")), native_log[-5000:]
        print((report / "passed.txt").read_text(), flush=True)
        raise SystemExit(0)
    if uncanny_only:
        wait_for(lambda: (report / "restart-b.txt").exists() and second.poll() is not None, 360, "both actual clients' native bedroom actions and four CCTV feeds")
        reconnected = launch("runProofB", "client-b-reconnected")
        wait_for(lambda: (report / "passed.txt").exists(), 360, "same-profile CCTV recovery, native door/account and actual health fuel")
        for p in processes:
            p.wait(timeout=90)
            assert p.returncode == 0, p.returncode
        for role in ("A", "B"):
            assert (report / f"{role}-bedroom-native-route.txt").stat().st_size > 50, role
            for view in ("short-passage", "boarded-periphery", "diner-translucent", "diner-solid", "giant-folded", "tunnel-native-escape", "bedroom-source", "bedroom-first-cut", "bedroom-second-cut", "bedroom-direct", "bedroom-cctv", "bedroom-cctv-reconnected", "bedroom-door-closed", "bedroom-account", "cellar-growth", "foyer-lit", "foyer-dark", "ring-and-mirror"):
                assert (report / f"{role}-{view}.png").stat().st_size > 500, (role, view)
            for prefix in ("", "reconnected-"):
                for channel in range(4):
                    assert (report / f"{role}-cctv-{prefix}{channel}.png").stat().st_size > 500, (role, prefix, channel)
                assert (report / f"{role}-cctv-{prefix}pixels.txt").stat().st_size > 30, (role, prefix)
        assert (report / "A-full-account.png").stat().st_size > 500, "native Full account button"
        native_log = (server / "logs/latest.log").read_text(errors="replace")
        assert "Seeding the house interior in the background." not in native_log, "The isolated uncanny fixture started an unrelated manor terrain copy."
        assert not any(warning in native_log for warning in ("moved too quickly!", "moved wrongly!", "floating too long!")), native_log[-5000:]
        print((report / "passed.txt").read_text(), flush=True)
        raise SystemExit(0)
    if borrasca_only:
        wait_for(lambda: (report / "passed.txt").exists(), 300, "targeted production Borrasca source, ladder, original record, low pipe and native return")
        for p in processes:
            p.wait(timeout=90)
            assert p.returncode == 0, p.returncode
        assert (report / "borrasca-socket-passed.txt").is_file()
        for role in ("A", "B"):
            for view in ("source-and-sound", "native-ladder", "original-register", "native-pipe", "native-return"):
                assert (report / f"{role}-borrasca-{view}.png").stat().st_size > 500, (role, view)
        native_log = (server / "logs/latest.log").read_text(errors="replace")
        assert not any(warning in native_log for warning in ("moved too quickly!", "moved wrongly!", "floating too long!")), native_log[-5000:]
        print((report / "passed.txt").read_text(), flush=True)
        raise SystemExit(0)
    wait_for(lambda: (report / "restart-b.txt").exists() and second.poll() is not None, 360, "both clients' first expedition and native logout")
    reconnected = launch("runProofB", "client-b-reconnected")
    wait_for(lambda: (report / "restart-b-leak.txt").exists() and reconnected.poll() is not None, 240, "personal burn, two note rooms and logout inside a leak")
    recovered = launch("runProofB", "client-b-leak-reconnected")
    wait_for(lambda: (report / "passed.txt").exists() and (report / "B-reconnected.png").exists(), 600, "same-profile recovery, native shared hunts, actual servings and canoe breath/input/rendered proof")
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
        for view in ("trailer-arrival", "trailer-camp", "trailer-confusion", "trailer-fear", "trailer-terror", "trailer-voice"):
            assert (report / f"{role}-{view}.png").stat().st_size > 10000, (role, view)
    assert (report / "trailer-arrival.txt").is_file()
    assert (report / "trailer-camp.txt").is_file()
    assert (report / "trailer-night.txt").is_file()
    for role in ("A", "B"):
        assert (report / f"{role}-trailer-window-goatman.png").stat().st_size > 500
        children = list(report.glob(f"{role}-trailer-window-child*.png"))
        assert len(children) >= 2, (role, children)
        assert all(frame.stat().st_size > 500 for frame in children)
        assert (report / f"{role}-trailer-window-variety.txt").stat().st_size > 100
    assert (report / "mob-sight-passed.txt").is_file()
    assert (report / "camp-bed-passed.txt").is_file()
    assert (report / "canoe-passed.txt").is_file()
    assert (report / "ghosts-native-jump-passed.txt").is_file()
    assert (report / "borrasca-socket-passed.txt").is_file()
    assert (report / "harrigan-states-passed.txt").is_file()
    for role in ("A", "B"):
        for view in ("harrigan-alive", "harrigan-dead", "harrigan-casket"):
            assert (report / f"{role}-{view}.png").stat().st_size > 10000, (role, view)
            assert (report / f"{role}-{view}.txt").stat().st_size > 100, (role, view)

    for role in ("A", "B"):
        for view in ("source-and-sound", "native-ladder", "original-register", "native-pipe", "native-return"):
            assert (report / f"{role}-borrasca-{view}.png").stat().st_size > 500, (role, view)
    for role in ("A", "B"):
        for view in ("ghosts-native-approach", "ghosts-held-banister-jump", "ghosts-restored-camera"):
            assert (report / f"{role}-{view}.png").stat().st_size > 500, (role, view)
            assert (report / f"{role}-{view}.txt").stat().st_size > 100, (role, view)
    assert (report / "tess-native-route.txt").is_file()
    assert (report / "A-tess-swimming-stair.png").stat().st_size > 500
    for role in ("A", "B"):
        for view in ("supper-empty", "supper-served", "supper-picked-up", "supper-old-original-repaired", "canoe-under-roof", "canoe-left-hand", "canoe-right-hand", "canoe-seated-open-water", "canoe-dismounted-dive"):
            assert (report / f"{role}-{view}.png").stat().st_size > 500, (role, view)
        for step in (44, 46):
            assert (report / f"{role}-supper-pixels-{step}.txt").is_file()
    for role in ("A", "B"):
        for view in ("camp-bed-hidden", "camp-bed-left", "paw-clutch"):
            assert (report / f"{role}-{view}.png").stat().st_size > 500, (role, view)
    for role in ("A", "B"):
        for view in ("outside-mob-sight", "window-mob-sight"):
            assert (report / f"{role}-{view}.png").stat().st_size > 500, (role, view)
            assert (report / f"{role}-{view}.txt").stat().st_size > 100, (role, view)
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
            selected = [line for line in log.read_text(errors="replace").splitlines() if any(word in line for word in ("LIVE EXPEDITION", "LIVE PORTHOLE", "LIVE BORRASCA", "UNCANNY SOCKET", "UNCANNY TUNNEL", "UNCANNY CLIENT:", "BACKSEAT", "Exception", "Caused by", "ERROR", "FAILED"))]
            print(f"{name}:\n" + "\n".join(selected[-25:]), flush=True)
