"""The lingering static after the House's worst rooms (journal/Grounding.java): a faint ringing in the ears, original and
synthesized, a seamless six-second mono loop. Requires ffmpeg.

A high pure tone that wavers a little in loudness and pitch, over a thin hiss. The tone and its wavers complete whole cycles in
the loop, so it repeats without a seam; the hiss is noise and has none to show. Writes sounds/journal/static_ring.ogg.
"""
from pathlib import Path
import math
import random
import struct
import subprocess
import tempfile
import wave

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/assets/the_oldest_house/sounds/journal"
RATE = 32000
SECONDS = 6


def ring():
    rng = random.Random(4199)
    n = RATE * SECONDS
    samples = []
    hiss = 0.0
    noise = [rng.uniform(-1, 1) for _ in range(n)]
    for i in range(n):
        t = i / RATE
        # 6.4 kHz with a slow waver of loudness (0.5 Hz) and a breath of pitch (1/3 Hz): whole cycles in six seconds.
        drift = 3.0 * math.sin(2 * math.pi * t / 3) / (2 * math.pi / 3)
        tone = math.sin(2 * math.pi * (6400 * t + drift))
        level = .78 + .22 * math.sin(2 * math.pi * .5 * t)
        # A thin high hiss, smoothed.
        hiss = .55 * hiss + .45 * noise[i]
        samples.append(level * tone * .9 + .08 * hiss)
    peak = max(abs(x) for x in samples)
    return [x / peak * .5 for x in samples]


def write(name, samples):
    OUTPUT.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="oth-journal-") as folder:
        source = Path(folder) / "source.wav"
        with wave.open(str(source), "wb") as wav:
            wav.setnchannels(1); wav.setsampwidth(2); wav.setframerate(RATE)
            wav.writeframes(b"".join(struct.pack("<h", max(-32767, min(32767, round(x * 32767)))) for x in samples))
        target = OUTPUT / f"{name}.ogg"
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(source), "-c:a", "libvorbis", "-q:a", "4", str(target)], check=True)


if __name__ == "__main__":
    write("static_ring", ring())
