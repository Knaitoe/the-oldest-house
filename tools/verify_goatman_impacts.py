"""Decode every registered Goatman impact from source or the actual playable JAR."""
import argparse
import array
import hashlib
import json
import math
from pathlib import Path
import subprocess
import tempfile
import zipfile


def verify(read):
    sounds = json.loads(read("sounds.json"))
    with tempfile.TemporaryDirectory(prefix="goatman-impact-check-") as temporary:
        for kind in ("knock", "claw", "hammer"):
            takes = sounds[f"goatman.{kind}"]["sounds"]
            assert len(takes) == 4, f"{kind}: four separately recorded contacts must be registered"
            fingerprints = set()
            for take in takes:
                assert take["attenuation_distance"] == 48, f"{kind}: lost positional range"
                name = take["name"].split(":", 1)[1]
                data = read(f"sounds/{name}.ogg")
                assert data.startswith(b"OggS"), f"{name}: invalid Ogg header"
                path = Path(temporary) / (name.rsplit("/", 1)[-1] + ".ogg")
                path.write_bytes(data)
                info = json.loads(subprocess.check_output([
                    "ffprobe", "-v", "error", "-show_streams", "-of", "json", str(path)
                ]))["streams"][0]
                assert info["channels"] == 1 and int(info["sample_rate"]) == 44100, f"{name}: native positional mono master required"
                assert .25 <= float(info["duration"]) <= .7, f"{name}: stacked prerecorded contacts or a truncated contact"
                decoded = subprocess.check_output([
                    "ffmpeg", "-nostdin", "-v", "error", "-i", str(path), "-f", "f32le", "-ac", "1", "pipe:1"
                ])
                samples = array.array("f", decoded)
                assert samples and all(math.isfinite(v) for v in samples), f"{name}: invalid decoded audio"
                peak = max(map(abs, samples))
                rms = math.sqrt(sum(v * v for v in samples) / len(samples))
                assert .5 < peak < .99 and rms > .02, f"{name}: clipped or inaudible contact ({peak=}, {rms=})"
                fingerprints.add(hashlib.sha256(decoded).digest())
                print(f"{name}: mono, {float(info['duration']):.2f}s, peak {peak:.3f}, RMS {rms:.3f}")
            assert len(fingerprints) == 4, f"{kind}: the four takes decode to duplicate audio"
    print("All twelve registered positional contacts decode, vary and remain below clipping.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", nargs="?", const="auto", help="Check a playable JAR; without a path, select build/libs' sole playable JAR")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    if args.jar:
        if args.jar == "auto":
            jars = [p for p in (root / "build/libs").glob("*.jar") if not p.name.endswith(("-sources.jar", "-javadoc.jar"))]
            assert len(jars) == 1, f"Expected one exact playable JAR: {jars}"
            path = jars[0]
        else:
            path = Path(args.jar)
        with zipfile.ZipFile(path) as package:
            assert package.testzip() is None, "Playable JAR CRC failure"
            verify(lambda name: package.read("assets/the_oldest_house/" + name))
    else:
        verify(lambda name: (root / "src/main/resources/assets/the_oldest_house" / name).read_bytes())


if __name__ == "__main__":
    main()
