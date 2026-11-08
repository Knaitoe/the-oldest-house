"""Original sounds for Last Weekend at Pinehook (0.4.364):
- knuckles rapping on a window pane;
- the breaker going, and the cabin's hum with it;
- something heavy going over in the shed;
- a camera's flash charging and going off;
- a tape going into the deck.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Existing files are kept.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'knock': ('Knuckles rap on glass', 16),
    'breaker': ('The power cuts out', 16),
    'crash': ('Something crashes in the shed', 24),
    'flash': ('A camera flashes', 10),
    'tape': ('A tape slots into the deck', 8),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/pinehook/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'pinehook.{name}'] == {'sounds': [{'name': f'the_oldest_house:pinehook/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.pinehook.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.pinehook.{name}'] == subtitle, name
    print(f'Verified Pinehook\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402
from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(6400)


def knock():
    n = seconds(0.9)
    out = np.zeros(n)
    for at in (0.0, 0.18, 0.36):
        k = seconds(0.09)
        t = times(k)
        tap = band(rng.standard_normal(k), 900, 5200) * envelope(k, 0.0005, 0.012)
        ring = np.sin(2 * np.pi * 2350 * t) * envelope(k, 0.0005, 0.03) * 0.35
        place(out, tap + ring, at)
    return out


def breaker():
    n = seconds(1.6)
    out = np.zeros(n)
    k = seconds(0.12)
    place(out, band(rng.standard_normal(k), 150, 2400) * envelope(k, 0.001, 0.025), 0.0)
    k = seconds(1.4)
    t = times(k)
    freq = 60 * (1 - 0.6 * t / 1.4)
    hum = np.sin(2 * np.pi * np.cumsum(freq) / RATE) * np.linspace(0.5, 0.0, k) ** 1.5
    place(out, hum * 0.6, 0.05)
    return out


def crash():
    n = seconds(1.3)
    out = np.zeros(n)
    k = seconds(0.5)
    place(out, band(rng.standard_normal(k), 60, 900) * envelope(k, 0.002, 0.12), 0.0)
    for at in (0.08, 0.2, 0.31, 0.5):
        k = seconds(0.25)
        place(out, band(rng.standard_normal(k), 300, 4000) * envelope(k, 0.001, 0.05) * (0.3 + 0.5 * rng.random()), at)
    return out


def flash():
    n = seconds(1.0)
    out = np.zeros(n)
    k = seconds(0.7)
    t = times(k)
    whine = np.sin(2 * np.pi * (2000 + 3500 * t / 0.7) * t) * adsr(k, 0.05, 0.1) * 0.25
    place(out, whine, 0.0)
    k = seconds(0.08)
    place(out, band(rng.standard_normal(k), 1500, 9000) * envelope(k, 0.0005, 0.015), 0.72)
    return out


def tape():
    n = seconds(1.1)
    out = np.zeros(n)
    k = seconds(0.1)
    place(out, band(rng.standard_normal(k), 400, 3000) * envelope(k, 0.001, 0.02), 0.0)
    k = seconds(0.12)
    place(out, band(rng.standard_normal(k), 200, 1800) * envelope(k, 0.001, 0.03), 0.25)
    k = seconds(0.7)
    t = times(k)
    whirr = band(rng.standard_normal(k), 600, 2400) * (0.6 + 0.4 * np.sin(2 * np.pi * 30 * t)) * adsr(k, 0.1, 0.2) * 0.3
    place(out, whirr, 0.38)
    return out


MAKERS = {'knock': knock, 'breaker': breaker, 'crash': crash, 'flash': flash, 'tape': tape}


def main():
    folder = ASSETS / 'sounds/pinehook'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'pinehook.{name}',
               {'sounds': [{'name': f'the_oldest_house:pinehook/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.pinehook.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.pinehook.{name}', subtitle)


if __name__ == '__main__':
    main()
