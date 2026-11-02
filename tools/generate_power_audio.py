"""Original sounds for the Fuse Board (0.4.254): the hum of a circuit given power, a motor running (the shutter rolling up, the hoist
lifting), and the shutter's motor straining against its catch and dropping back.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root. Existing files
are kept, so the committed sounds only change when they are deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'hum': ('Mains hum', 8),
    'motor': ('A motor runs', 16),
    'strain': ('A motor strains, and stops', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/power/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'power.{name}'] == {'sounds': [{'name': f'the_oldest_house:power/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.power.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.power.{name}'] == subtitle, name
    print(f'Verified the basement\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(254)


def hum():
    # Mains hum: the fundamental and its odd harmonics, a little buzz on them, fading in and out.
    n = seconds(1.6)
    t = times(n)
    out = sum(np.sin(2 * np.pi * f * t) * w for f, w in ((50, .5), (100, .35), (150, .25), (250, .12), (350, .06)))
    out += band(rng.standard_normal(n), 400, 2000) * .04 * (1 + np.sign(np.sin(2 * np.pi * 100 * t)))
    return out * adsr(n, .25, .5)


def motor():
    # A motor running up: a whine rising to speed, gears chattering, a steady roll, and the clunk at the top.
    n = seconds(2.4)
    t = times(n)
    speed = np.clip(t / .5, 0, 1)
    whine = np.sin(2 * np.pi * np.cumsum(180 + 220 * speed) / RATE) * .3
    gears = band(rng.standard_normal(n), 600, 2400) * (.5 + .5 * np.sign(np.sin(2 * np.pi * (14 + 10 * speed) * t))) * .18
    rumble = band(rng.standard_normal(n), 60, 300) * .35
    out = (whine + gears + rumble) * adsr(n, .1, .2)
    k = seconds(.2)
    place(out, band(rng.standard_normal(k), 80, 800) * envelope(k, .002, .05) * .8, 2.15)
    return out


def strain():
    # Straining: the motor whines up, labours against something that won't give, a rattle in the rails, then cuts out and drops back.
    n = seconds(1.8)
    t = times(n)
    load = np.clip(t / .6, 0, 1) * (t < 1.1)
    whine = np.sin(2 * np.pi * np.cumsum(140 + 90 * load - 40 * np.clip((t - .6) / .5, 0, 1)) / RATE) * .35 * (t < 1.15)
    rattle = band(rng.standard_normal(n), 300, 1800) * (.5 + .5 * np.sign(np.sin(2 * np.pi * 22 * t))) * .25 * load
    out = (whine + rattle) * adsr(n, .05, .1)
    k = seconds(.25)
    place(out, band(rng.standard_normal(k), 60, 700) * envelope(k, .002, .06) * .9, 1.2)
    place(out, band(rng.standard_normal(k), 200, 1500) * envelope(k, .002, .04) * .4, 1.3)
    return out


MAKERS = {'hum': hum, 'motor': motor, 'strain': strain}


def main():
    folder = ASSETS / 'sounds/power'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'power.{name}',
               {'sounds': [{'name': f'the_oldest_house:power/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.power.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.power.{name}', subtitle)


if __name__ == '__main__':
    main()
