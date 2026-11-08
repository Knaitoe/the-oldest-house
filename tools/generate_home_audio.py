"""Original sounds for the House's read of a reader's home (0.4.371): the authority figures who linger under a settled DANGER read.
- a floorboard in the hall taking someone's weight;
- one slow step on boards, played along the hall a step at a time so it moves;
- a door handle turned partway, held, and let go, the latch tongue clicking back.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Existing files are kept.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'home_floorboard': ('A floorboard creaks in the hall', 14),
    'home_step': ('Slow footsteps in the hall', 16),
    'home_handle': ('A door handle turns and is let go', 10),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def entry(name):
    return {'sounds': [{'name': f'the_oldest_house:literary/{name}', 'attenuation_distance': ROWS[name][1]}], 'subtitle': f'subtitles.the_oldest_house.literary.{name}'}


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, _) in ROWS.items():
        path = ASSET_ROOT / f'sounds/literary/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'literary.{name}'] == entry(name), name
        assert lang[f'subtitles.the_oldest_house.literary.{name}'] == subtitle, name
    print(f"Verified the home read's {len(ROWS)} original cues.")


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402
from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(3710)


def creak_tone(length, f0, f1, rasp=0.5):
    """A board under weight: a slow gliding fundamental with stick-slip pulses and the hollow of the joists beneath."""
    n = seconds(length)
    t = times(n)
    freq = f0 + (f1 - f0) * t / length + 4 * np.sin(2 * np.pi * 2.3 * t)
    phase = 2 * np.pi * np.cumsum(freq) / RATE
    pulses = np.sign(np.sin(phase)) * (0.45 + 0.55 * np.abs(np.sin(phase * 0.5)))
    jitter = band(rng.standard_normal(n), 250, 2000) * rasp
    body = band(pulses + jitter * np.abs(np.sin(phase)), 90, 1400)
    return body * adsr(n, length * 0.3, length * 0.4)


def home_floorboard():
    n = seconds(1.4)
    out = np.zeros(n)
    place(out, creak_tone(0.9, 120, 165) * 0.9, 0.0)
    place(out, creak_tone(0.4, 170, 150, 0.3) * 0.35, 0.85)
    return out


def home_step():
    """A heel and then the ball of a foot on boards, heavy and unhurried, with a small give in the board after."""
    n = seconds(0.7)
    out = np.zeros(n)
    for at, size, low in ((0.0, 1.0, 60), (0.09, 0.55, 90)):
        k = seconds(0.18)
        t = times(k)
        thud = (np.sin(2 * np.pi * low * t) * 0.8 + band(rng.standard_normal(k), 50, 500) * 0.7) * envelope(k, 0.002, 0.05)
        place(out, thud * size, at)
    place(out, creak_tone(0.35, 150, 130, 0.25) * 0.18, 0.2)
    return out


def home_handle():
    """The handle turned partway against its spring, a pause, then let go: the spring's scrape and the latch tongue snapping home."""
    n = seconds(1.6)
    out = np.zeros(n)
    k = seconds(0.35)
    t = times(k)
    turn = band(rng.standard_normal(k), 1200, 5000) * (0.4 + 0.6 * np.abs(np.sin(2 * np.pi * 31 * t))) * adsr(k, 0.05, 0.12) * 0.4
    place(out, turn, 0.0)
    k = seconds(0.06)
    click = band(rng.standard_normal(k), 1500, 8000) * envelope(k, 0.0005, 0.01)
    place(out, click * 0.5, 0.33)
    k = seconds(0.25)
    t = times(k)
    back = band(rng.standard_normal(k), 900, 4500) * (0.5 + 0.5 * np.abs(np.sin(2 * np.pi * 44 * t))) * adsr(k, 0.02, 0.1) * 0.35
    place(out, back, 1.05)
    k = seconds(0.12)
    t = times(k)
    tongue = (band(rng.standard_normal(k), 1800, 9000) * 0.8 + np.sin(2 * np.pi * 2600 * t) * 0.3) * envelope(k, 0.0005, 0.02)
    place(out, tongue, 1.3)
    return out


MAKERS = {'home_floorboard': home_floorboard, 'home_step': home_step, 'home_handle': home_handle}


def main():
    folder = ASSETS / 'sounds/literary'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, _) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'literary.{name}', entry(name))
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.literary.{name}', subtitle)


if __name__ == '__main__':
    main()
