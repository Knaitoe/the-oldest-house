"""Original 96-second ambient composition: low reeds, sparse felt bells and slowly shifting fifths."""
from pathlib import Path
import math
import struct
import subprocess
import tempfile
import wave

ROOT = Path(__file__).resolve().parents[1]
RATE, LENGTH = 24000, 96
OUTPUT = ROOT / "src/main/resources/assets/the_oldest_house/sounds/staircase/descent.ogg"
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
chords = ((73.416, 110, 164.814), (65.406, 98, 146.832), (69.296, 103.826, 155.563), (73.416, 110, 146.832))
bells = ((5, 293.665), (13, 220), (29, 261.626), (37, 196), (53, 277.183), (61, 207.652), (77, 293.665), (85, 220))
with tempfile.TemporaryDirectory(prefix="oth-score-") as folder:
    source = Path(folder) / "score.wav"
    with wave.open(str(source), "wb") as wav:
        wav.setnchannels(1); wav.setsampwidth(2); wav.setframerate(RATE)
        chunk = bytearray()
        for sample in range(RATE * LENGTH):
            t = sample / RATE
            fade = min(1, t / 4, (LENGTH - t) / 5)
            value = 0
            for section, chord in enumerate(chords):
                age = t - section * 24
                if not 0 <= age <= 28:
                    continue
                envelope = min(1, age / 5) * min(1, (28 - age) / 5)
                for voice, pitch in enumerate(chord):
                    breath = .72 + .15 * math.sin(t * .21 + voice)
                    phase = 2 * math.pi * pitch * t + .015 * math.sin(t * .7)
                    value += .075 * envelope * breath * (math.sin(phase) + .14 * math.sin(phase * 2))
            for start, pitch in bells:
                age = t - start
                if 0 <= age < 9:
                    envelope = min(1, age / .04) * math.exp(-age * .7)
                    value += .065 * envelope * (math.sin(2 * math.pi * pitch * age) + .23 * math.sin(2 * math.pi * pitch * 2.01 * age))
            value += .022 * math.sin(2 * math.pi * 36.708 * t) * (1 + math.sin(t * .08)) / 2
            chunk.extend(struct.pack("<h", round(max(-1, min(1, value * fade)) * 26000)))
            if len(chunk) >= 48000:
                wav.writeframes(chunk); chunk.clear()
        wav.writeframes(chunk)
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(source), "-c:a", "libvorbis", "-q:a", "4", str(OUTPUT)], check=True)
print(f"{OUTPUT.relative_to(ROOT)}: {OUTPUT.stat().st_size} bytes, mono, {LENGTH} seconds")
