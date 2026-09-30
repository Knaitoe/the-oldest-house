#!/usr/bin/env python3
"""Authored, muffled door cues. No samples, words, or external recordings."""
from pathlib import Path
import subprocess
import tempfile
import wave
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/leaks'
RATE = 22050

def muffle(signal, cutoff=1100):
    spectrum = np.fft.rfft(signal)
    frequencies = np.fft.rfftfreq(len(signal), 1 / RATE)
    return np.fft.irfft(spectrum / (1 + (frequencies / cutoff) ** 6), n=len(signal))

def write(name, signal):
    envelope = np.minimum(1, np.arange(len(signal)) / (RATE * .08))
    envelope *= np.minimum(1, np.arange(len(signal))[::-1] / (RATE * .16))
    signal = signal * envelope
    signal = signal / max(1, np.max(np.abs(signal))) * .75
    OUT.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as directory:
        wav = Path(directory) / (name + '.wav')
        with wave.open(str(wav), 'wb') as stream:
            stream.setnchannels(1); stream.setsampwidth(2); stream.setframerate(RATE)
            stream.writeframes((signal * 32767).astype('<i2').tobytes())
        subprocess.run(['ffmpeg', '-nostdin', '-hide_banner', '-loglevel', 'error', '-y', '-i', str(wav),
                        '-c:a', 'libvorbis', '-q:a', '4', str(OUT / (name + '.ogg'))], check=True)

def main():
    rng = np.random.default_rng(747)
    t = np.arange(int(RATE * 2.8)) / RATE
    murmur = np.zeros_like(t)
    # Varying formants and syllable envelopes suggest voices through a wall without speaking words.
    for center, duration, fundamental in zip([.25, .62, 1.08, 1.5, 1.94, 2.35], [.18, .24, .29, .16, .26, .3], [127, 191, 146, 213, 133, 176]):
        syllable = np.exp(-((t - center) / duration) ** 4)
        pitch = 2 * np.pi * (fundamental * t + 2 * np.sin(2 * np.pi * 2.1 * t))
        voice = sum(np.sin(pitch * harmonic) / harmonic for harmonic in range(1, 7))
        murmur += syllable * voice * .12
    write('television', muffle(murmur + rng.normal(0, .018, len(t)), 900))
    t = np.arange(int(RATE * 2.6)) / RATE
    pulse = ((t < .8) | ((t > 1.35) & (t < 2.15))).astype(float)
    pulse *= (.65 + .35 * np.sin(2 * np.pi * 14 * t))
    write('phone', muffle((np.sin(2 * np.pi * 440 * t) + .8 * np.sin(2 * np.pi * 480 * t)) * pulse * .28, 1050))
    t = np.arange(int(RATE * 4.0)) / RATE
    music = np.zeros_like(t)
    for i, frequency in enumerate([220, 261.63, 329.63, 293.66, 246.94, 207.65, 220, 164.81]):
        local = t - i * .46
        env = np.where(local >= 0, np.exp(-np.maximum(local, 0) * 2.8), 0)
        tone = np.sin(2 * np.pi * frequency * local) + .23 * np.sin(2 * np.pi * frequency * 2.003 * local)
        music += tone * env * .21
    music += .035 * np.sin(2 * np.pi * 110 * t)
    write('hotel_music', muffle(music, 1000))
    for file in OUT.glob('*.ogg'):
        print(file.relative_to(ROOT), file.stat().st_size)

if __name__ == '__main__': main()
