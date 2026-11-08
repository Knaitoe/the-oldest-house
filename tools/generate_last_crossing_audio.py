"""Original sounds for The Last Crossing (0.4.351): a knock carried down a speaking pipe, the station pump taking up its load, something
tapping back under the service grate, and the intake's bleed screw letting go its air and then water.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'knock': ('A knock in the speaking pipe', 24),
    'pump': ('The station pump takes the load', 32),
    'tap': ('Something taps back under the grate', 12),
    'bleed': ('Air hisses from the bleed screw', 12),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/crossing/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'crossing.{name}'] == {'sounds': [{'name': f'the_oldest_house:crossing/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.crossing.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.crossing.{name}'] == subtitle, name
    print(f'Verified the station\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9351)


def knock():
    # A knuckle on iron, heard down a length of pipe: a dull thud, then the pipe ringing hollow and dying away.
    n = seconds(1.2)
    out = np.zeros(n)
    k = seconds(.05)
    place(out, band(rng.standard_normal(k), 120, 1400) * envelope(k, .001, .03) * 1.0, 0)
    tk = times(seconds(1.1))
    ring = sum(np.sin(2 * np.pi * f * tk) * np.exp(-tk * d) * a for f, d, a in ((310, 5, .5), (620, 7, .25), (935, 9, .12), (1460, 12, .06)))
    place(out, ring * .6, .004)
    return out


def pump():
    # An electric pump taking up its load: the motor winding up from a growl, a knock or two of air, then a steady churning note.
    n = seconds(3.4)
    out = np.zeros(n)
    tk = times(n)
    f = 38 + 22 * np.minimum(1, tk / 1.6)
    phase = 2 * np.pi * np.cumsum(f) / RATE
    motor = np.sin(phase) * .5 + np.sin(phase * 2) * .25 + np.sign(np.sin(phase * 3)) * .08
    churn = band(rng.standard_normal(n), 200, 900) * (.3 + .2 * np.sin(2 * np.pi * 7 * tk))
    env = np.minimum(1, tk / .3) * np.minimum(1, (3.4 - tk) / .4)
    out += (motor + churn * np.minimum(1, tk / 1.6)) * env * .8
    for at in (.7, 1.1):
        k = seconds(.12)
        place(out, band(rng.standard_normal(k), 80, 600) * envelope(k, .003, .08) * .7, at)
    return out


def tap():
    # Three soft taps from under the grate, wet and unhurried, as if a fingertip were answering on the iron from below.
    n = seconds(1.6)
    out = np.zeros(n)
    for i, at in enumerate((0, .42, .9)):
        k = seconds(.08)
        hit = band(rng.standard_normal(k), 300, 2600) * envelope(k, .0008, .025)
        tk = times(seconds(.25))
        ring = np.sin(2 * np.pi * (700 - i * 30) * tk) * np.exp(-tk * 22) * .3
        place(out, hit * .7, at)
        place(out, ring, at + .002)
        k = seconds(.2)
        place(out, band(rng.standard_normal(k), 150, 700) * envelope(k, .02, .12) * .15, at + .03)
    return out


def bleed():
    # The bleed screw backed off: trapped air hissing out, sputtering, then water spitting and running.
    n = seconds(2.4)
    out = np.zeros(n)
    tk = times(n)
    hiss = band(rng.standard_normal(n), 2500, 9000) * np.exp(-tk * 1.4) * .7
    sputter = band(rng.standard_normal(n), 400, 3000) * (rng.random(n) > .985) * np.clip((tk - .8) * 2, 0, 1) * 1.4
    run = band(rng.standard_normal(n), 300, 1800) * np.clip((tk - 1.3) * 1.5, 0, 1) * .35
    env = np.minimum(1, tk / .02) * np.minimum(1, (2.4 - tk) / .3)
    out += (hiss + sputter + run) * env
    return out


MAKERS = {'knock': knock, 'pump': pump, 'tap': tap, 'bleed': bleed}


def main():
    folder = ASSETS / 'sounds/crossing'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'crossing.{name}',
               {'sounds': [{'name': f'the_oldest_house:crossing/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.crossing.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.crossing.{name}', subtitle)


if __name__ == '__main__':
    main()
