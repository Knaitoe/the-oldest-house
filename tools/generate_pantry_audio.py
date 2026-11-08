"""Original sounds for the Cold Pantry (0.4.363):
- ice scraping as a block is lifted out of the box or put back;
- a ventilation shutter swinging on its hinge;
- water dripping as the room warms;
- frost cracking off the pipes;
- the cold store's seal letting go.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Existing files are kept.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'ice': ('Ice scrapes', 10),
    'shutter': ('A shutter swings', 12),
    'drip': ('Water drips', 8),
    'frost': ('Frost cracks', 10),
    'seal': ('A seal gives', 12),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/pantry/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'pantry.{name}'] == {'sounds': [{'name': f'the_oldest_house:pantry/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.pantry.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.pantry.{name}'] == subtitle, name
    print(f'Verified the cold pantry\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402
from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(3800)


def ice():
    n = seconds(0.9)
    t = times(n)
    grind = band(rng.standard_normal(n), 1800, 9000) * (0.5 + 0.5 * np.abs(np.sin(2 * np.pi * 17 * t)))
    knock = np.zeros(n)
    k = seconds(0.15)
    place(knock, band(rng.standard_normal(k), 300, 2500) * envelope(k, 0.001, 0.03), 0.7)
    return grind * adsr(n, 0.05, 0.3) * 0.7 + knock


def shutter():
    n = seconds(1.0)
    out = np.zeros(n)
    k = seconds(0.55)
    t = times(k)
    freq = 420 + 160 * t / 0.55
    creak = np.sign(np.sin(2 * np.pi * np.cumsum(freq) / RATE)) * band(rng.standard_normal(k), 300, 3000) * 0.4
    place(out, band(creak, 250, 2500) * adsr(k, 0.08, 0.2), 0.0)
    k = seconds(0.4)
    place(out, band(rng.standard_normal(k), 120, 1600) * envelope(k, 0.002, 0.06), 0.55)
    return out


def drip():
    n = seconds(0.7)
    out = np.zeros(n)
    for at, f in ((0.0, 1400), (0.35, 1150)):
        k = seconds(0.25)
        t = times(k)
        sweep = np.sin(2 * np.pi * (f + 900 * t) * t) * envelope(k, 0.001, 0.04)
        place(out, sweep, at)
    return out


def frost():
    n = seconds(0.8)
    out = np.zeros(n)
    for at in (0.0, 0.05, 0.12, 0.2, 0.33, 0.41):
        k = seconds(0.04)
        place(out, band(rng.standard_normal(k), 2500, 11000) * envelope(k, 0.0003, 0.006) * (0.4 + 0.6 * rng.random()), at)
    return out


def seal():
    n = seconds(1.1)
    out = np.zeros(n)
    k = seconds(0.12)
    place(out, band(rng.standard_normal(k), 200, 2000) * envelope(k, 0.001, 0.03), 0.0)
    k = seconds(0.9)
    sigh = band(rng.standard_normal(k), 400, 2600) * adsr(k, 0.15, 0.5) * 0.4
    place(out, sigh, 0.08)
    return out


MAKERS = {'ice': ice, 'shutter': shutter, 'drip': drip, 'frost': frost, 'seal': seal}


def main():
    folder = ASSETS / 'sounds/pantry'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'pantry.{name}',
               {'sounds': [{'name': f'the_oldest_house:pantry/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.pantry.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.pantry.{name}', subtitle)


if __name__ == '__main__':
    main()
