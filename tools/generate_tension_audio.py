"""Original sounds for the Tension Room (0.4.306): a hand-wheel turning against weight (its pawl clicking), the cable taking up (a
creak of hemp over the sheave), and the locking pin drawing free (a dry metal slide and a spring's thunk).

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root. Existing files
are kept, so the committed sounds only change when they are deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'wheel': ('A wheel turns against weight', 12),
    'take': ('Cable takes up', 14),
    'pin': ('A pin draws free', 12),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/tension/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'tension.{name}'] == {'sounds': [{'name': f'the_oldest_house:tension/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.tension.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.tension.{name}'] == subtitle, name
    print(f'Verified the cable gear\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(306)


def wheel():
    # A wooden hand-wheel straining round: a low groan of the axle under load and the pawl clicking over four teeth.
    n = seconds(1.0)
    t = times(n)
    groan = np.sin(2 * np.pi * np.cumsum(95 + 25 * np.sin(2 * np.pi * 1.7 * t)) / RATE) * .3
    groan += band(rng.standard_normal(n), 80, 600) * .15
    out = groan * adsr(n, .08, .3)
    for i in range(4):
        k = seconds(.04)
        place(out, band(rng.standard_normal(k), 1500, 6000) * envelope(k, .001, .015) * .7, .12 + i * .2)
    return out


def take():
    # The cable taking up: hemp creaking over the sheave, a long stretched rasp, and a faint thrum as it comes taut.
    n = seconds(1.2)
    t = times(n)
    creak = np.sin(2 * np.pi * np.cumsum(320 + 140 * np.sin(2 * np.pi * 5 * t)) / RATE) * .12
    rasp = band(rng.standard_normal(n), 600, 3000) * .25 * (.6 + .4 * np.sin(2 * np.pi * 11 * t))
    out = (creak + rasp) * adsr(n, .1, .4)
    k = seconds(.5)
    tk = times(k)
    place(out, np.sin(2 * np.pi * 70 * tk) * envelope(k, .01, .3) * .35, .65)
    return out


def pin():
    # A brass pin drawn out of its seat: a dry slide, then the platform's springs letting go with a deep thunk.
    n = seconds(1.0)
    out = np.zeros(n)
    k = seconds(.35)
    tk = times(k)
    place(out, band(rng.standard_normal(k), 2000, 8000) * envelope(k, .02, .1) * .3, 0.0)
    place(out, np.sin(2 * np.pi * np.cumsum(1800 - 900 * tk / .35) / RATE) * envelope(k, .02, .1) * .08, 0.0)
    k = seconds(.4)
    tk = times(k)
    place(out, (np.sin(2 * np.pi * 75 * tk) + .5 * np.sin(2 * np.pi * 150 * tk)) * envelope(k, .003, .2) * .7, .38)
    place(out, band(rng.standard_normal(k), 200, 1200) * envelope(k, .002, .08) * .3, .38)
    return out


MAKERS = {'wheel': wheel, 'take': take, 'pin': pin}


def main():
    folder = ASSETS / 'sounds/tension'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'tension.{name}',
               {'sounds': [{'name': f'the_oldest_house:tension/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.tension.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.tension.{name}', subtitle)


if __name__ == '__main__':
    main()
