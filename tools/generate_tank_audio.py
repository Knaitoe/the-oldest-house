"""Original sounds for the Header Tank (0.4.256): a valve's wheel turning, water running down a pipe into the pit, a stroke of the hand
pump, and the platform's boards knocking against the pit wall as it rides up or down.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root. Existing files
are kept, so the committed sounds only change when they are deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'valve': ('A valve wheel turns', 12),
    'run': ('Water runs in a pipe', 16),
    'pump': ('A hand pump gulps', 12),
    'knock': ('Boards knock on a wall', 12),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/tank/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'tank.{name}'] == {'sounds': [{'name': f'the_oldest_house:tank/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.tank.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.tank.{name}'] == subtitle, name
    print(f'Verified the waterworks\' {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(256)


def valve():
    # A stiff wheel turning: a dry metal squeal that catches and slips, and a clunk as it seats.
    n = seconds(1.0)
    t = times(n)
    catch = (np.sin(2 * np.pi * 7 * t) > 0).astype(float) * .6 + .4
    squeal = np.sin(2 * np.pi * np.cumsum(900 + 300 * np.sin(2 * np.pi * 1.3 * t)) / RATE) * .25 * catch
    grit = band(rng.standard_normal(n), 1200, 5000) * .12 * catch
    out = (squeal + grit) * adsr(n, .05, .25)
    k = seconds(.15)
    place(out, band(rng.standard_normal(k), 100, 900) * envelope(k, .002, .05) * .7, .82)
    return out


def run():
    # Water running down a pipe and pouring into the pit: a hollow rush, bubbles, the splash below.
    n = seconds(1.4)
    t = times(n)
    rush = band(rng.standard_normal(n), 300, 1400) * .45
    hollow = np.sin(2 * np.pi * 210 * t) * .06 * (1 + .5 * np.sin(2 * np.pi * 3 * t))
    out = (rush + hollow) * adsr(n, .15, .35)
    for _ in range(14):
        k = seconds(.05)
        f = rng.uniform(500, 1400)
        tk = times(k)
        place(out, np.sin(2 * np.pi * f * (1 + 3 * tk) * tk) * envelope(k, .002, .03) * .18, rng.uniform(.05, 1.2))
    out += band(rng.standard_normal(n), 2000, 7000) * .08 * adsr(n, .2, .3)
    return out


def pump():
    # A hand pump stroke: the handle's squeak, the leather drawing, a gulp of water up the pipe.
    n = seconds(1.1)
    out = np.zeros(n)
    k = seconds(.3)
    tk = times(k)
    place(out, np.sin(2 * np.pi * np.cumsum(600 + 500 * tk / .3) / RATE) * envelope(k, .02, .1) * .2, 0.0)
    k = seconds(.45)
    place(out, band(rng.standard_normal(k), 150, 900) * envelope(k, .05, .2) * .5, .2)
    k = seconds(.35)
    tk = times(k)
    place(out, np.sin(2 * np.pi * (180 - 90 * tk / .35) * tk) * envelope(k, .01, .2) * .6, .55)
    place(out, band(rng.standard_normal(k), 400, 1600) * envelope(k, .01, .15) * .25, .6)
    return out


def knock():
    # The platform's boards knocking on the brick as it rides: two hollow wooden knocks and a creak.
    n = seconds(.8)
    out = np.zeros(n)
    for at, f in ((0.0, 160), (.18, 140)):
        k = seconds(.18)
        tk = times(k)
        place(out, (np.sin(2 * np.pi * f * tk) + .4 * np.sin(2 * np.pi * 2.7 * f * tk)) * envelope(k, .002, .06) * .6, at)
        place(out, band(rng.standard_normal(k), 300, 2500) * envelope(k, .001, .03) * .3, at)
    k = seconds(.4)
    tk = times(k)
    place(out, np.sin(2 * np.pi * np.cumsum(240 + 60 * np.sin(2 * np.pi * 9 * tk)) / RATE) * envelope(k, .05, .2) * .12, .35)
    return out


MAKERS = {'valve': valve, 'run': run, 'pump': pump, 'knock': knock}


def main():
    folder = ASSETS / 'sounds/tank'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'tank.{name}',
               {'sounds': [{'name': f'the_oldest_house:tank/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.tank.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.tank.{name}', subtitle)


if __name__ == '__main__':
    main()
