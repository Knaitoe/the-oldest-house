"""Original sounds for the Clockwork Attic (0.4.362):
- a master clock's tick and tock;
- gear teeth engaging;
- a heavy lever seating;
- a ratchet winding a train back;
- a weight coming down on its chain;
- bars drawing up into a lintel.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Existing files are kept.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'tick': ('A clock ticks', 10),
    'teeth': ('Gear teeth engage', 12),
    'lever': ('A lever seats heavily', 16),
    'ratchet': ('A ratchet winds back', 12),
    'weight': ('A weight drops', 18),
    'gate': ('Bars draw up', 14),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/clockwork/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'clockwork.{name}'] == {'sounds': [{'name': f'the_oldest_house:clockwork/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.clockwork.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.clockwork.{name}'] == subtitle, name
    print(f'Verified the clockwork\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402
from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(3700)


def click(length, low, high, decay, ring=None, ring_decay=0.05):
    """A struck click: a band of noise with a fast decay, and if asked a ringing partial over it."""
    n = seconds(length)
    out = band(rng.standard_normal(n), low, high) * envelope(n, 0.0005, decay)
    if ring:
        t = times(n)
        out = out + sum(np.sin(2 * np.pi * f * t) * a for f, a in ring) * envelope(n, 0.001, ring_decay) * 0.4
    return out


def tick():
    n = seconds(0.6)
    out = np.zeros(n)
    place(out, click(0.08, 1500, 6000, 0.008, [(2300, 1)], 0.02), 0.0)
    place(out, click(0.08, 900, 4000, 0.01, [(1700, 1)], 0.025) * 0.8, 0.3)
    return out


def teeth():
    n = seconds(0.45)
    out = np.zeros(n)
    place(out, click(0.3, 800, 7000, 0.015, [(1200, 1), (3150, 0.5)], 0.08), 0.0)
    place(out, click(0.1, 1200, 6000, 0.008) * 0.5, 0.06)
    return out


def lever():
    n = seconds(1.3)
    out = np.zeros(n)
    k = seconds(0.6)
    t = times(k)
    thud = (np.sin(2 * np.pi * 70 * t) + band(rng.standard_normal(k), 40, 400) * 0.7) * envelope(k, 0.002, 0.12)
    place(out, thud, 0.0)
    place(out, click(1.1, 600, 5000, 0.02, [(440, 1), (1180, 0.6), (2210, 0.3)], 0.4), 0.005)
    return out


def ratchet():
    n = seconds(1.3)
    out = np.zeros(n)
    at = 0.0
    gap = 0.05
    while at < 1.15:
        place(out, click(0.04, 1800, 8000, 0.006) * (0.6 + 0.4 * rng.random()), at)
        at += gap
        gap = max(0.028, gap * 0.97)
    return out * adsr(n, 0.05, 0.2)


def weight():
    n = seconds(1.5)
    out = np.zeros(n)
    for i in range(14):
        place(out, click(0.05, 2000, 9000, 0.01) * (0.3 + 0.03 * i), i * 0.04)
    k = seconds(0.9)
    t = times(k)
    thud = (np.sin(2 * np.pi * 52 * t) * 1.1 + band(rng.standard_normal(k), 30, 260)) * envelope(k, 0.003, 0.2)
    place(out, thud, 0.58)
    place(out, click(0.6, 400, 3000, 0.04, [(330, 1), (790, 0.4)], 0.25) * 0.6, 0.58)
    return out


def gate():
    n = seconds(1.3)
    out = np.zeros(n)
    at = 0.0
    while at < 1.0:
        place(out, click(0.05, 1500, 7000, 0.012) * (0.4 + 0.3 * rng.random()), at)
        at += 0.06 + 0.03 * rng.random()
    k = seconds(0.5)
    place(out, click(0.5, 500, 4000, 0.03, [(520, 1), (1390, 0.4)], 0.2), 1.0 - 0.0)
    return out


MAKERS = {'tick': tick, 'teeth': teeth, 'lever': lever, 'ratchet': ratchet, 'weight': weight, 'gate': gate}


def main():
    folder = ASSETS / 'sounds/clockwork'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'clockwork.{name}',
               {'sounds': [{'name': f'the_oldest_house:clockwork/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.clockwork.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.clockwork.{name}', subtitle)


if __name__ == '__main__':
    main()
