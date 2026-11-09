"""Original sounds for the arcade's prizes (0.4.411): one kazoo note (played at every pitch of its scale), the bouncy ball's boing,
a rubber squeak for the spider and the plush dog, the slap bracelet snapping round a wrist, the finger trap's weave creaking tight,
and the pop of fingers coming free.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from tools/. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'kazoo': ('Kazoo buzzes', 16),
    'boing': ('Ball bounces', 16),
    'squeak': ('Rubber squeaks', 12),
    'slap': ('Bracelet snaps on', 12),
    'creak': ('Finger trap creaks', 8),
    'pop': ('Fingers come free', 8),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/prize/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'prize.{name}'] == {'sounds': [{'name': f'the_oldest_house:prize/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.prize.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.prize.{name}'] == subtitle, name
    print(f'Verified the prizes\' {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9411)


def held(n, attack, release):
    t = times(n)
    return np.minimum(1, t / attack) * np.minimum(1, (t[-1] - t) / release)


def kazoo():
    # One hummed note through a membrane: a sawtooth at C5 with a little wobble, its buzz from clipped upper harmonics and a nasal formant.
    n = seconds(.26)
    t = times(n)
    f = 523.25 * (1 + .006 * np.sin(2 * np.pi * 6 * t))
    phase = np.cumsum(f) / RATE
    saw = 2 * (phase % 1) - 1
    buzz = np.clip(saw * 2.2, -1, 1)
    nasal = band(buzz, 900, 2600) * 1.4
    return (buzz * .35 + nasal * .6) * held(n, .015, .05)


def boing():
    # A rubber ball off a hard floor: a dull knock and a short spring pitch falling away.
    n = seconds(.32)
    t = times(n)
    knock = band(rng.standard_normal(n), 200, 1800) * np.exp(-t / .006) * .8
    spring = np.sin(2 * np.pi * np.cumsum(420 * np.exp(-t / .12) + 180) / RATE) * np.exp(-t / .09) * .7
    return knock + spring


def squeak():
    # A squeezed rubber toy: air forced through a small reed, rising then falling.
    n = seconds(.22)
    t = times(n)
    f = 1300 + 700 * np.sin(np.pi * t / t[-1])
    reed = np.sin(2 * np.pi * np.cumsum(f) / RATE) + .4 * np.sin(4 * np.pi * np.cumsum(f) / RATE)
    air = band(rng.standard_normal(n), 2000, 6000) * .15
    return (reed * .5 + air) * held(n, .02, .06)


def slap():
    # A slap bracelet: the steel band springing round and the print slapping skin.
    n = seconds(.3)
    t = times(n)
    spring = np.sin(2 * np.pi * np.cumsum(2400 - 1400 * np.minimum(1, t / .05)) / RATE) * np.exp(-t / .03) * .35
    hit = band(rng.standard_normal(n), 400, 4000) * np.exp(-np.maximum(0, t - .02) / .012) * (t > .02) * .9
    return spring + hit


def creak():
    # Woven straw drawing tight: a run of tiny dry ticks, closer together as it tightens.
    n = seconds(.4)
    out = np.zeros(n)
    at = 0.0
    gap = .05
    while at < .36:
        k = seconds(.012)
        place(out, band(rng.standard_normal(k), 1200, 5000) * np.exp(-times(k) / .003) * rng.uniform(.4, .9), at)
        at += gap
        gap = max(.012, gap * .82)
    return out


def pop():
    # A finger coming free of the weave: a soft hollow pop.
    n = seconds(.18)
    t = times(n)
    return np.sin(2 * np.pi * np.cumsum(700 - 300 * np.minimum(1, t / .05)) / RATE) * np.exp(-t / .03) * .8 + band(rng.standard_normal(n), 300, 2000) * np.exp(-t / .01) * .4


MAKERS = {'kazoo': kazoo, 'boing': boing, 'squeak': squeak, 'slap': slap, 'creak': creak, 'pop': pop}


def main():
    folder = ASSETS / 'sounds/prize'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'prize.{name}',
               {'sounds': [{'name': f'the_oldest_house:prize/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.prize.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.prize.{name}', subtitle)


if __name__ == '__main__':
    main()
