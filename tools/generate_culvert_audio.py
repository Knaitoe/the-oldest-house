"""Original sound for the lake house's culvert (0.4.560, docs/CULVERT_0_4_560.md): a wet palm pushing off the concrete, the push
that moves a canoe along under the drive. Three takes, so a run of strokes never repeats one, each with the tunnel's short
answer in it.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places it at the
canoe. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

NAME = 'hand_push'
SUBTITLE = 'Hand pushes off concrete'
REACH = 16
TAKES = 3
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def entry():
    return {'sounds': [{'name': f'the_oldest_house:lake_house/{NAME}_{k + 1}', 'attenuation_distance': REACH} for k in range(TAKES)],
            'subtitle': f'subtitles.the_oldest_house.lake_house.{NAME}'}


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for k in range(TAKES):
        path = ASSET_ROOT / f'sounds/lake_house/{NAME}_{k + 1}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
    assert sounds[f'lake_house.{NAME}'] == entry(), NAME
    assert lang[f'subtitles.the_oldest_house.lake_house.{NAME}'] == SUBTITLE, NAME
    print(f'Verified the culvert\'s hand push: {TAKES} takes.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9560)


def push(k):
    # A wet palm flat against concrete and shoved off it: the slap of skin, a dull knock in the wall, water flicked off the
    # fingers, and the tube answering it back a few times, closer together each time.
    n = seconds(.55)
    t = times(n)
    slap = band(rng.standard_normal(n), 300 + 60 * k, 2600) * np.exp(-t / (.018 + .004 * k))
    knock = (np.sin(2 * np.pi * (96 + 11 * k) * t) * .7 + band(rng.standard_normal(n), 60, 260) * .5) * np.exp(-t / .045)
    drip = band(rng.standard_normal(n), 2400, 6000) * np.exp(-np.maximum(0, t - .03) / .012) * (t > .03) * .25
    dry = slap + knock * .8 + drip
    out = dry.copy()
    for delay, gain in ((.031, .38), (.057, .24), (.079, .15), (.097, .09)):
        place(out, band(dry, 150, 1800) * gain, delay + .004 * k)
    return out


def main():
    folder = ASSETS / 'sounds/lake_house'
    folder.mkdir(parents=True, exist_ok=True)
    for k in range(TAKES):
        target = folder / f'{NAME}_{k + 1}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(push(k), .7).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
    update(ASSETS / 'sounds.json', f'lake_house.{NAME}', entry())
    update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.lake_house.{NAME}', SUBTITLE)


if __name__ == '__main__':
    main()
