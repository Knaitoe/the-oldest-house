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
    rate, duration = 22050, 3.6
    rng, slow, air = random.Random(462), 0.0, 0.0
    samples = bytearray()
    for i in range(round(rate * duration)):
        t, noise = i / rate, rng.uniform(-1, 1)
        slow = .93 * slow + .07 * noise
        air = .42 * air + .58 * (noise - slow)
        inhale = math.sin(math.pi * (t - .25) / 1.15) ** 2 if .25 < t < 1.4 else 0
        exhale = math.sin(math.pi * (t - 1.65) / 1.55) ** 2 if 1.65 < t < 3.2 else 0
        envelope = .22 * inhale + .29 * exhale
        value = envelope * (air * .7 + slow * 2.2)
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
