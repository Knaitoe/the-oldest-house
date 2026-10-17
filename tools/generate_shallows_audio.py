"""Original Shallows sounds (0.4.101): the swing, her break on the water, her bones as she rises, the crossing.

Synthesized here from noise and short resonances; no recordings. Run from the repository root. Existing files are kept,
so the committed sounds only change when they are deleted first.
"""
import json
from pathlib import Path

import numpy as np
import soundfile

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
RATE = 44100
rng = np.random.default_rng(101)


def seconds(n):
    return int(n * RATE)


def lowpass(x, cutoff):
    # One-pole smoothing, run twice for a gentler slope.
    a = np.exp(-2 * np.pi * cutoff / RATE)
    for _ in range(2):
        y = np.empty_like(x)
        acc = 0.0
        for i, v in enumerate(x):
            acc = (1 - a) * v + a * acc
            y[i] = acc
        x = y
    return x


def bandpass(x, low, high):
    return lowpass(x, high) - lowpass(x, low)


def envelope(n, attack, decay):
    t = np.arange(n) / RATE
    return np.minimum(1, t / max(attack, 1e-4)) * np.exp(-t / decay)


def place(out, sound, at):
    start = seconds(at)
    end = min(len(out), start + len(sound))
    out[start:end] += sound[:end - start]


def crack(length=0.05, pitch=1.0):
    # A dry snap: a broadband click with two short resonances, like a green stick or a joint.
    n = seconds(length)
    t = np.arange(n) / RATE
    click = rng.standard_normal(n) * envelope(n, 0.0004, 0.006)
    ring = (np.sin(2 * np.pi * 1450 * pitch * t) * 0.6 + np.sin(2 * np.pi * 2730 * pitch * t) * 0.4) * envelope(n, 0.0008, 0.012)
    return bandpass(click, 400, 7000) * 1.4 + ring * 0.5


def swing():
    # Cloth and air: a band of noise sweeping up then down as the body passes the bottom of the arc.
    n = seconds(0.5)
    t = np.arange(n) / RATE
    noise = rng.standard_normal(n)
    sweep = np.sin(np.pi * t / t[-1])
    low = bandpass(noise, 250, 900) * (0.4 + 0.6 * sweep)
    high = bandpass(noise, 900, 3200) * sweep ** 2
    rustle = bandpass(rng.standard_normal(n), 2000, 6000) * (rng.random(n) < 0.02) * 2.5
    return (low * 0.7 + high * 0.5 + rustle * 0.3) * sweep


def break_on_water():
    # A flat, wet slap with no plunge after it, and the cracks of a body landing on something hard.
    out = np.zeros(seconds(1.1))
    n = seconds(0.35)
    slap = bandpass(rng.standard_normal(n), 300, 3800) * envelope(n, 0.001, 0.045)
    thud = lowpass(rng.standard_normal(n), 140) * envelope(n, 0.002, 0.09) * 6
    place(out, slap * 1.2 + thud, 0)
    for at, pitch, gain in ((0.012, 1.0, 1.0), (0.055, 0.8, 0.9), (0.11, 1.15, 0.7), (0.21, 0.7, 0.8), (0.36, 0.95, 0.45)):
        place(out, crack(0.06, pitch) * gain, at)
    # A few drops falling back, small, because nothing went under.
    for at in rng.uniform(0.15, 0.8, 9):
        m = seconds(0.03)
        drop = np.sin(2 * np.pi * rng.uniform(900, 1700) * np.arange(m) / RATE) * envelope(m, 0.001, 0.008)
        place(out, drop * 0.12, at)
    return out


def rise():
    # Joints settling the wrong way: grinding creaks between slow, separate clicks.
    out = np.zeros(seconds(1.6))
    for at in (0.0, 0.32, 0.55, 0.9, 1.25):
        n = seconds(0.22)
        t = np.arange(n) / RATE
        base = rng.uniform(70, 120)
        phase = 2 * np.pi * np.cumsum(base * (1 + 0.25 * rng.standard_normal(n).cumsum() / np.sqrt(n))) / RATE
        creak = np.sign(np.sin(phase)) * envelope(n, 0.02, 0.07)
        place(out, bandpass(creak, 200, 2500) * 0.35, at)
        place(out, crack(0.05, rng.uniform(0.6, 1.1)) * 0.8, at + rng.uniform(0.05, 0.18))
    return out


def scurry():
    # Quick wet taps, two hands and two feet at a time, spaced unevenly; meant to repeat while she runs.
    out = np.zeros(seconds(0.24))
    for at in (0.0, 0.035, 0.11, 0.15):
        n = seconds(0.06)
        tap = bandpass(rng.standard_normal(n), 1200, 5200) * envelope(n, 0.0008, 0.012)
        splash = bandpass(rng.standard_normal(n), 2500, 8000) * envelope(n, 0.004, 0.02) * 0.4
        place(out, tap + splash, at + rng.uniform(0, 0.01))
    return out


SOUNDS = {
    'swing': (swing, 'Cloth swings through the air'),
    'break': (break_on_water, 'Bones snap against the water'),
    'rise': (rise, 'Bones grind back into place'),
    'scurry': (scurry, 'Something skitters across the water'),
}


def normalized(x, peak=0.8):
    x = x - np.mean(x)
    return x / max(1e-6, np.max(np.abs(x))) * peak


def update(path, key, value):
    original = path.read_text()
    data = json.loads(original)
    data[key] = value
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + ('\n' if original.endswith('\n') else ''))


def main():
    folder = ASSETS / 'sounds/shallows'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (make, subtitle) in SOUNDS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(make()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'shallows.{name}',
               {'sounds': [{'name': f'the_oldest_house:shallows/{name}'}], 'subtitle': f'subtitles.the_oldest_house.shallows.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.shallows.{name}', subtitle)
    print(f'Shallows: {len(SOUNDS)} subtitled sounds.')


if __name__ == '__main__':
    main()
