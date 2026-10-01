"""Make an original mono lub-dub, with low wood resonance; requires ffmpeg only."""
from pathlib import Path
import math
import random
import struct
import subprocess
import tempfile
import wave

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/assets/the_oldest_house/sounds/vignette/heartbeat.ogg"
RATE = 24000
random_source = random.Random(410)
samples = []
noise = 0.0
for index in range(round(RATE * 0.46)):
    time = index / RATE
    noise = 0.87 * noise + 0.13 * random_source.uniform(-1, 1)
    value = 0.0
    for start, strength, pitch, decay in ((0.018, 1.0, 64, 24), (0.195, 0.65, 78, 32)):
        age = time - start
        if age < 0:
            continue
        attack = min(1.0, age / 0.007)
        envelope = attack * math.exp(-decay * age)
        phase = 2 * math.pi * (pitch * age - 28 * age * age)
        value += strength * envelope * (math.sin(phase) + 0.23 * math.sin(2.08 * phase) + noise * 0.25)
        # A quiet, damped wooden response, rather than a digital click.
        value += strength * 0.12 * math.sin(2 * math.pi * 143 * age) * math.exp(-18 * age) * attack
    samples.append(value)
peak = max(abs(value) for value in samples)
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
with tempfile.TemporaryDirectory(prefix="oldest-house-heart-") as folder:
    source = Path(folder) / "heartbeat.wav"
    with wave.open(str(source), "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(RATE)
        wav.writeframes(b"".join(struct.pack("<h", round(value / peak * 24500)) for value in samples))
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(source),
                    "-c:a", "libvorbis", "-q:a", "5", str(OUTPUT)], check=True)
print(f"Wrote {OUTPUT.relative_to(ROOT)} ({OUTPUT.stat().st_size} bytes)")
