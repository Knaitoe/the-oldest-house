#!/usr/bin/env python3
"""Original mono inhale/exhale, with a quiet loop seam and no sampled recording."""
import math
import random
import struct
import subprocess
import tempfile
import wave
from pathlib import Path

TARGET = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house/sounds/well/breath.ogg'


def generate():
    rate, duration = 22050, 4.8
    rng, slow, air = random.Random(464), 0.0, 0.0
    samples = bytearray()
    for i in range(round(rate * duration)):
        t, noise = i / rate, rng.uniform(-1, 1)
        slow = .975 * slow + .025 * noise
        air = .82 * air + .18 * (noise - slow)
        inhale = math.sin(math.pi * (t - .3) / 1.6) ** 1.4 if .3 < t < 1.9 else 0
        exhale = math.sin(math.pi * (t - 2.2) / 2.2) ** 1.8 if 2.2 < t < 4.4 else 0
        envelope = .24 * inhale + .34 * exhale
        throat = math.sin(2 * math.pi * (112 + 4 * math.sin(t * 2)) * t) * .06
        value = envelope * (air * 1.1 + slow * 2.7 + throat)
        samples.extend(struct.pack('<h', round(max(-.8, min(.8, value)) * 32767)))
    TARGET.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='well-breath-') as temporary:
        wav = Path(temporary) / 'breath.wav'
        with wave.open(str(wav), 'wb') as recording:
            recording.setnchannels(1)
            recording.setsampwidth(2)
            recording.setframerate(rate)
            recording.writeframes(samples)
        subprocess.run(['ffmpeg', '-loglevel', 'error', '-y', '-i', str(wav), '-c:a', 'libvorbis', '-q:a', '5', str(TARGET)], check=True)


if __name__ == '__main__':
    generate()
