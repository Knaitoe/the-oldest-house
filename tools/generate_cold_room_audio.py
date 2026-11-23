"""Original sounds for the cold room (0.4.754, docs/COLD_ROOM_0_4_754.md): the house at night and its stairs.

The rented house in winter: wind at the glass, low and gusting, a radiator's tick inside it; and the stair boards giving under a foot.
The rest come with the parts that use them.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'room': ('Wind presses at the windows', 24),
    'boards': ('Stair boards creak', 12),
    'bed': ('A bed frame knocks', 20),
    'drawer': ('A drawer slides out', 12),
    'voice': ('Someone speaks low', 10),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/cold_room/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'cold_room.{name}'] == {'sounds': [{'name': f'the_oldest_house:cold_room/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.cold_room.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.cold_room.{name}'] == subtitle, name
    print(f'Verified the cold room\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9754)


def room():
    # Five seconds of the house: wind at the glass in two gusts, a thin whistle in the frame at the top of each; one radiator tick.
    n = seconds(5.0)
    t = times(n)
    gust = .45 + .35 * np.clip(np.sin(2 * np.pi * .22 * t), 0, None) + .2 * np.clip(np.sin(2 * np.pi * .41 * t + 1.3), 0, None)
    wind = band(rng.standard_normal(n), 80, 900) * gust
    whistle = np.sin(2 * np.pi * (720 + 40 * np.sin(2 * np.pi * .7 * t)) * t) * .05 * np.clip(gust - .6, 0, None) * 3
    out = wind + whistle
    burst = band(rng.standard_normal(n), 1800, 6000)
    out += burst * np.exp(-np.clip(t - 3.4, 0, None) / .006) * (t >= 3.4) * .35
    edge = np.clip(t / .5, 0, 1) * np.clip((t[-1] - t) / .5, 0, 1)
    return out * edge


def boards():
    # A board giving under weight: a slow rising creak, the wood's grain in it, and the knock of it settling.
    n = seconds(.9)
    t = times(n)
    f = 180 + 140 * np.clip(t / .6, 0, 1)
    creak = np.sin(2 * np.pi * np.cumsum(f) / RATE) * (1 + .6 * np.sign(np.sin(2 * np.pi * 37 * t))) * .25
    creak *= np.clip(t / .08, 0, 1) * np.clip((.65 - t) / .1, 0, 1)
    grain = band(rng.standard_normal(n), 400, 2400) * .12 * (t < .65)
    knock = band(rng.standard_normal(n), 90, 600) * np.exp(-np.clip(t - .68, 0, None) / .03) * (t >= .68) * .8
    return creak + grain + knock


def bed():
    # 0.4.755: an iron frame lifted and let down: three knocks of its legs on the boards, closer together, and the springs' ring.
    n = seconds(1.4)
    t = times(n)
    out = np.zeros(n)
    for at, size in ((.0, 1.0), (.42, .8), (.7, .9)):
        knock = band(rng.standard_normal(n), 60, 500) * np.exp(-np.clip(t - at, 0, None) / .05) * (t >= at)
        ring = np.sin(2 * np.pi * 410 * (t - at)) * np.exp(-np.clip(t - at, 0, None) / .25) * (t >= at) * .12
        out += (knock + ring) * size
    return out


def drawer():
    # 0.4.755: wood on wood, a drawer pulled out its whole length, and the knock of it stopping on its runners.
    n = seconds(1.1)
    t = times(n)
    slide = band(rng.standard_normal(n), 250, 1800) * (.3 + .2 * np.sin(2 * np.pi * 23 * t)) * np.clip(t / .05, 0, 1) * (t < .8)
    stop = band(rng.standard_normal(n), 80, 700) * np.exp(-np.clip(t - .8, 0, None) / .03) * (t >= .8) * .9
    return slide + stop


def voice():
    # 0.4.755: under the words, a breath drawn too long and let out through a throat: no words in it, low and close.
    n = seconds(2.2)
    t = times(n)
    breath = band(rng.standard_normal(n), 120, 1400) * np.clip(np.sin(np.pi * t / 2.2), 0, None)
    throat = np.sin(2 * np.pi * (62 + 6 * np.sin(2 * np.pi * 3.1 * t)) * t) * .35 * np.clip((t - .6) / .4, 0, 1) * np.clip((2.1 - t) / .4, 0, 1)
    rasp = band(rng.standard_normal(n), 60, 300) * np.abs(np.sin(2 * np.pi * 11 * t)) * .4 * (t > .6)
    return breath * .6 + throat + rasp


MAKERS = {'room': room, 'boards': boards, 'bed': bed, 'drawer': drawer, 'voice': voice}


def main():
    folder = ASSETS / 'sounds/cold_room'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'cold_room.{name}',
               {'sounds': [{'name': f'the_oldest_house:cold_room/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.cold_room.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.cold_room.{name}', subtitle)


if __name__ == '__main__':
    main()
