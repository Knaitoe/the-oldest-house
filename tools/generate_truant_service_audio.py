"""Original sounds for Johnny Truant's service halls (0.4.307): chalk on plaster, three knocks on an iron main, the main rattling as the
pressure builds, steam venting, a lift brake hauled or let go, and the car lurching on its cable.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Existing files are kept.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'chalk': ('Chalk scrapes on a wall', 10),
    'knock': ('Three knocks on a pipe', 24),
    'rattle': ('A pipe shivers', 20),
    'hiss': ('Steam vents', 18),
    'brake': ('A lift brake grinds', 14),
    'lurch': ('A lift car lurches', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/truant_service/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'truant_service.{name}'] == {'sounds': [{'name': f'the_oldest_house:truant_service/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.truant_service.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.truant_service.{name}'] == subtitle, name
    print(f'Verified Truant\'s service halls\' {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(3070)


def chalk():
    n = seconds(.9)
    out = np.zeros(n)
    for at in (0.0, .3, .55):
        k = seconds(.22)
        t = times(k)
        place(out, band(rng.standard_normal(k), 2500, 9000) * (.6 + .4 * np.sin(2 * np.pi * 40 * t)) * envelope(k, .02, .06) * .5, at)
    return out


def knock():
    n = seconds(1.1)
    out = np.zeros(n)
    for at in (0.0, .22, .44):
        k = seconds(.35)
        t = times(k)
        ring = sum(np.sin(2 * np.pi * f * t) * a for f, a in ((310, 1), (742, .5), (1290, .3)))
        place(out, ring * envelope(k, .001, .18) * .5 + band(rng.standard_normal(k), 1500, 5000) * envelope(k, .001, .01) * .4, at)
    return out


def rattle():
    n = seconds(1.5)
    t = times(n)
    buzz = np.sign(np.sin(2 * np.pi * 38 * t)) * band(rng.standard_normal(n), 200, 1500) * .4
    return buzz * np.linspace(.2, 1, n) * adsr(n, .1, .2)


def hiss():
    n = seconds(1.6)
    return band(rng.standard_normal(n), 1500, 9000) * adsr(n, .03, .5) * .7


def brake():
    n = seconds(1.0)
    t = times(n)
    grind = np.sin(2 * np.pi * np.cumsum(220 + 90 * np.sin(2 * np.pi * 6 * t)) / RATE) * .25 + band(rng.standard_normal(n), 400, 3000) * .3
    out = grind * adsr(n, .05, .3)
    k = seconds(.2)
    place(out, band(rng.standard_normal(k), 80, 700) * envelope(k, .002, .08) * .8, .75)
    return out


def lurch():
    n = seconds(1.2)
    out = np.zeros(n)
    k = seconds(.5)
    t = times(k)
    place(out, (np.sin(2 * np.pi * 55 * t) + band(rng.standard_normal(k), 100, 900)) * envelope(k, .003, .25) * .8, 0.0)
    k = seconds(.7)
    t = times(k)
    place(out, np.sin(2 * np.pi * np.cumsum(500 - 300 * t / .7) / RATE) * envelope(k, .02, .3) * .15, .25)
    return out


MAKERS = {'chalk': chalk, 'knock': knock, 'rattle': rattle, 'hiss': hiss, 'brake': brake, 'lurch': lurch}


def main():
    folder = ASSETS / 'sounds/truant_service'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'truant_service.{name}',
               {'sounds': [{'name': f'the_oldest_house:truant_service/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.truant_service.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.truant_service.{name}', subtitle)


if __name__ == '__main__':
    main()
