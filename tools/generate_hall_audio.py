"""Original, quiet mono room landmarks. No samples or borrowed story motifs; requires ffmpeg."""
from pathlib import Path
import math
import random
import struct
import subprocess
import tempfile
import wave

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/assets/the_oldest_house/sounds/hall"
RATE = 24000
OUTPUT.mkdir(parents=True, exist_ok=True)
for name, seconds, strikes in (
    ("pipes", 1.7, ((.04, 164, 1, 14), (.28, 149, .6, 12), (.79, 164, .32, 14))),
    ("stone", 2.4, ((.04, 82, 1, 6), (.35, 83, .2, 5), (.72, 79, .13, 5))),
    ("settle", 1.8, ((.04, 126, .7, 8), (.59, 114, .3, 12))),
):
    rng = random.Random(448 + len(name))
    samples = []
    filtered = 0
    for index in range(round(seconds * RATE)):
        t = index / RATE
        filtered = .86 * filtered + .14 * rng.uniform(-1, 1)
        value = 0
        for start, pitch, gain, decay in strikes:
            age = t - start
            if age < 0:
                continue
            attack = min(1, age / .008)
            envelope = gain * attack * math.exp(-age * decay)
            value += envelope * (math.sin(2 * math.pi * pitch * age) + .19 * math.sin(2 * math.pi * pitch * 2.13 * age) + .13 * filtered)
            if name == "settle":
                value += .08 * filtered * attack * math.exp(-age * 4)
        samples.append(value)
    peak = max(abs(x) for x in samples)
    with tempfile.TemporaryDirectory(prefix="oth-hall-") as folder:
        source = Path(folder) / "source.wav"
        with wave.open(str(source), "wb") as wav:
            wav.setnchannels(1); wav.setsampwidth(2); wav.setframerate(RATE)
            wav.writeframes(b"".join(struct.pack("<h", round(x / peak * 18000)) for x in samples))
        target = OUTPUT / f"{name}.ogg"
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(source), "-c:a", "libvorbis", "-q:a", "4", str(target)], check=True)
        print(f"{target.relative_to(ROOT)}: {target.stat().st_size} bytes, mono")
