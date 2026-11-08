"""Original sounds for The Supper (0.4.348): the Marsh dinner bell rung in its yoke, the kitchen's swing door going through and back,
and boots wiped on the coir mat at the top of the porch steps.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'bell': ('A dinner bell rings', 48),
    'swing': ('A swing door flaps through and back', 16),
    'wipe': ('Boots scrape on a doormat', 12),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/marsh/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'marsh.{name}'] == {'sounds': [{'name': f'the_oldest_house:marsh/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.marsh.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.marsh.{name}'] == subtitle, name
    print(f'Verified the Marsh supper\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9348)


def bell():
    # A cast farm bell swung three times in its yoke: inharmonic partials that ring and beat, each strike over the last one's tail,
    # the clapper's knock at the front of each.
    n = seconds(3.2)
    out = np.zeros(n)
    partials = [(512, 1.0, 1.4), (1063, .55, .9), (1421, .35, .7), (1987, .22, .45), (2762, .12, .3), (257, .3, 2.0)]
    for at, strength in ((0.0, 1.0), (0.62, .85), (1.24, .7)):
        k = seconds(2.0)
        tk = times(k)
        ring = np.zeros(k)
        for f, a, d in partials:
            ring += a * np.sin(2 * np.pi * f * tk * (1 + .002 * np.sin(2 * np.pi * 3.1 * tk))) * np.exp(-tk / d)
        knock = band(rng.standard_normal(seconds(.02)), 800, 5000) * envelope(seconds(.02), .0005, .006)
        ring[:len(knock)] += knock * 1.5
        place(out, ring * strength * np.minimum(1, tk / .002), at)
    return out


def swing():
    # A light kitchen door on a double-acting hinge: pushed through with a short wooden creak, flapping back past centre twice,
    # each pass softer, and the hinge's spring sighing.
    n = seconds(1.4)
    out = np.zeros(n)
    for at, strength in ((0.0, 1.0), (0.38, .55), (0.66, .3)):
        k = seconds(.24)
        tk = times(k)
        creak_f = 380 + 260 * tk / .24
        creak = np.sign(np.sin(2 * np.pi * np.cumsum(creak_f) / RATE)) * band(rng.standard_normal(k), 300, 2400) * .35
        air = band(rng.standard_normal(k), 200, 1200) * np.sin(np.pi * tk / .24) * .5
        place(out, (creak + air) * envelope(k, .01, .12) * strength, at)
        k = seconds(.05)
        place(out, band(rng.standard_normal(k), 120, 900) * envelope(k, .001, .015) * strength * .8, at + .2)
    return out


def wipe():
    # Two boots dragged back across stiff coir: a dry bristled scrape, a pause, the other foot, then a last short scuff.
    n = seconds(1.3)
    out = np.zeros(n)
    for at, length, strength in ((0.0, .32, 1.0), (0.45, .30, .9), (0.92, .14, .6)):
        k = seconds(length)
        tk = times(k)
        bristle = band(rng.standard_normal(k), 1800, 9000) * (.6 + .4 * (rng.random(k) > .7))
        thump = band(rng.standard_normal(k), 80, 500) * .4
        place(out, (bristle + thump) * np.sin(np.pi * tk / length) ** .7 * strength, at)
    return out


MAKERS = {'bell': bell, 'swing': swing, 'wipe': wipe}


def main():
    folder = ASSETS / 'sounds/marsh'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'marsh.{name}',
               {'sounds': [{'name': f'the_oldest_house:marsh/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.marsh.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.marsh.{name}', subtitle)


if __name__ == '__main__':
    main()
