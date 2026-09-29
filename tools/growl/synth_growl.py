#!/usr/bin/env python3
"""
Synthesizes the Growl: the Minotaur's voice, heard long before it is seen.

Every file is mono (Minecraft only positions mono sounds) and original: a
large, slow throat built from a gliding low fundamental and its harmonics,
roughened by an irregular rattle, shaped by fixed "throat" resonances, with
breath noise, then heard through stone: low-passed and given a long, dark
room tail. Nothing is sampled.

    pip install numpy soundfile
    python3 tools/growl/synth_growl.py

writes src/main/resources/assets/the_oldest_house/sounds/growl/*.ogg. A
recorded or studio-made growl can replace any file under the same name.
"""
import os

import numpy as np
import soundfile as sf

SR = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "..",
                   "src", "main", "resources", "assets", "the_oldest_house", "sounds", "growl")


def smooth_noise(rng, n, rate_hz):
    """Slowly varying noise in about [-1, 1], changing at roughly rate_hz."""
    points = max(4, int(n / SR * rate_hz) + 2)
    knots = rng.uniform(-1.0, 1.0, points)
    x = np.linspace(0, points - 1, n)
    i = np.floor(x).astype(int)
    f = x - i
    f = f * f * (3 - 2 * f)
    i2 = np.minimum(i + 1, points - 1)
    return knots[i] * (1 - f) + knots[i2] * f


def spectral_filter(signal, gain_of_hz):
    spectrum = np.fft.rfft(signal)
    freqs = np.fft.rfftfreq(len(signal), 1.0 / SR)
    return np.fft.irfft(spectrum * gain_of_hz(freqs), len(signal))


def lowpass(cutoff, order=2.0):
    return lambda f: 1.0 / (1.0 + (f / cutoff) ** (2 * order))


def throat(f):
    """Fixed resonances of a very large throat and chest, over a low shelf."""
    bumps = (1.0 * np.exp(-((f - 140) / 70) ** 2)
             + 0.7 * np.exp(-((f - 380) / 120) ** 2)
             + 0.35 * np.exp(-((f - 850) / 220) ** 2)
             + 0.12 * np.exp(-((f - 1900) / 500) ** 2))
    return 0.25 + bumps


def envelope(n, attack, release, rng):
    t = np.arange(n) / SR
    dur = n / SR
    a = np.clip(t / attack, 0, 1)
    a = a * a * (3 - 2 * a)
    r = np.clip((dur - t) / release, 0, 1) ** 1.6
    swell = 1.0 + 0.25 * smooth_noise(rng, n, 1.2)
    return a * r * swell


def voice(dur, f0_start, f0_end, seed, rattle_hz, rattle_depth, breath):
    rng = np.random.default_rng(seed)
    n = int(dur * SR)
    t = np.arange(n) / SR
    glide = f0_start * (f0_end / f0_start) ** (t / dur)
    wobble = 1.0 + 0.035 * np.sin(2 * np.pi * 0.37 * t + rng.uniform(0, 6)) + 0.02 * smooth_noise(rng, n, 6)
    f0 = glide * wobble
    phase = 2 * np.pi * np.cumsum(f0) / SR

    tone = np.zeros(n)
    for k in range(1, 40):
        amp = 1.0 / k ** 1.05
        tone += amp * np.sin(k * phase + rng.uniform(0, 2 * np.pi))
    # A subharmonic underneath: the fry of something too big for its voice.
    tone += 0.55 * np.sin(0.5 * phase + rng.uniform(0, 2 * np.pi))

    # The rattle: an irregular flutter, faster in the middle of the breath.
    rate = rattle_hz * (1.0 + 0.3 * smooth_noise(rng, n, 2.0))
    rattle_phase = 2 * np.pi * np.cumsum(rate) / SR
    rattle = 1.0 - rattle_depth * (0.5 + 0.5 * np.sin(rattle_phase)) ** 2 * (0.6 + 0.4 * smooth_noise(rng, n, 9))
    tone *= rattle

    noise = rng.normal(0, 1, n)
    noise = spectral_filter(noise, lambda f: 1.0 / np.maximum(f, 20.0) ** 0.9)
    noise /= np.max(np.abs(noise)) + 1e-9
    body = tone / (np.max(np.abs(tone)) + 1e-9) + breath * noise * rattle
    body = spectral_filter(body, throat)
    return body


def room(signal, seconds, darkness_hz, wet, seed):
    """A long stone tail: convolution with decaying, darkened noise."""
    rng = np.random.default_rng(seed)
    m = int(seconds * SR)
    t = np.arange(m) / SR
    ir = rng.normal(0, 1, m) * np.exp(-6.9 * t / seconds)
    ir = spectral_filter(ir, lowpass(darkness_hz, 1.5))
    ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
    size = len(signal) + m
    tail = np.fft.irfft(np.fft.rfft(signal, size) * np.fft.rfft(ir, size), size)
    dry = np.concatenate([signal, np.zeros(m)])
    return (1 - wet) * dry + wet * tail * 3.0


def finish(signal, peak=0.89):
    signal = signal - np.mean(signal)
    fade = int(0.08 * SR)
    signal[-fade:] *= np.linspace(1, 0, fade)
    signal[:64] *= np.linspace(0, 1, 64)
    return (signal / (np.max(np.abs(signal)) + 1e-9) * peak).astype(np.float32)


def make(name, seconds, f0, seed, cutoff, tail, wet, rattle_hz=26.0, rattle_depth=0.75, breath=0.35,
         attack=1.1, release=1.8, sub=0.0):
    rng = np.random.default_rng(seed + 1000)
    body = voice(seconds, f0[0], f0[1], seed, rattle_hz, rattle_depth, breath)
    body *= envelope(len(body), attack, release, rng)
    if sub > 0:
        t = np.arange(len(body)) / SR
        rumble = np.sin(2 * np.pi * (27 + 3 * smooth_noise(rng, len(body), 0.5)) * t)
        body += sub * rumble * envelope(len(body), attack * 1.4, release * 1.2, rng) * np.max(np.abs(body))
    heard = spectral_filter(body, lowpass(cutoff, 2.0))
    heard = room(heard, tail, cutoff * 0.8, wet, seed + 7)
    out = finish(heard)
    path = os.path.join(OUT, name + ".ogg")
    sf.write(path, out, SR, format="OGG", subtype="VORBIS")
    print(f"{name}: {len(out) / SR:.1f} s, peak {np.max(np.abs(out)):.2f}, rms {np.sqrt(np.mean(out ** 2)):.3f}")


def main():
    os.makedirs(OUT, exist_ok=True)
    # Far: long, deep, heard through a great deal of stone. Enough of the
    # throat's midrange survives the walls to be heard on small speakers.
    make("far1", 5.0, (78, 56), seed=11, cutoff=750, tail=3.2, wet=0.6)
    make("far2", 4.2, (88, 63), seed=23, cutoff=680, tail=3.0, wet=0.62, rattle_hz=22.0)
    # Below: directly underneath, felt as much as heard, but still heard.
    make("below1", 4.6, (64, 46), seed=37, cutoff=520, tail=1.8, wet=0.45, sub=0.3, breath=0.3)
    # Near: shorter, rougher, less wall between.
    make("near1", 3.4, (96, 66), seed=53, cutoff=2400, tail=1.6, wet=0.35, rattle_hz=31.0, rattle_depth=0.85,
         breath=0.45, attack=0.45, release=1.2)

if __name__ == "__main__":
    main()
