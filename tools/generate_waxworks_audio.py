"""Original sounds for the waxworks (0.4.749, docs/WAXWORKS_0_4_749.md): the building at night, the time clock, the monitors.

The museum after closing: its ventilation, a broad low air with a radiator ticking somewhere in it; the time clock taking a card, a
clunk and the bell of its stamp; and the bank of monitors, their high whine and the faint crackle of six tubes. The rest come with the
parts that use them.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'room': ('The museum ventilation hums', 24),
    'punch': ('A time clock stamps a card', 16),
    'monitors': ('Monitors whine', 12),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/waxworks/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'waxworks.{name}'] == {'sounds': [{'name': f'the_oldest_house:waxworks/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.waxworks.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.waxworks.{name}'] == subtitle, name
    print(f'Verified the waxworks\' {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9749)


def room():
    # Four seconds of the building: the ventilation's low air, a slow swell in it; two ticks of a radiator, one near and one far.
    n = seconds(4.0)
    t = times(n)
    air = band(rng.standard_normal(n), 60, 700) * (.6 + .15 * np.sin(2 * np.pi * .3 * t))
    hum = np.sin(2 * np.pi * 60 * t) * .12 + np.sin(2 * np.pi * 120 * t) * .06
    out = air + hum
    for at, gain in ((1.3, .5), (3.1, .25)):
        burst = band(rng.standard_normal(n), 1800, 6000)
        out += burst * np.exp(-np.clip(t - at, 0, None) / .006) * (t >= at) * gain
    edge = np.clip(t / .4, 0, 1) * np.clip((t[-1] - t) / .4, 0, 1)
    return out * edge


def punch():
    # The card going in, the clunk of the stamp, the little bell after it.
    n = seconds(.8)
    t = times(n)
    out = np.zeros(n)
    for at, lo, hi, gain, decay in ((.0, 900, 4000, .4, .02), (.18, 120, 1400, 1.0, .03), (.2, 2000, 7000, .5, .01)):
        burst = band(rng.standard_normal(n), lo, hi)
        out += burst * np.exp(-np.clip(t - at, 0, None) / decay) * (t >= at) * gain
    bell = np.sin(2 * np.pi * 2350 * t) * np.exp(-np.clip(t - .22, 0, None) / .18) * (t >= .22) * .25
    return out + bell


def monitors():
    # Three seconds of six tubes: a high whine each, a little apart, and crackle.
    n = seconds(3.0)
    t = times(n)
    whine = sum(np.sin(2 * np.pi * f * t) for f in (15625, 15700, 15560)) * .04
    crackle = band(rng.standard_normal(n), 3000, 9000) * (rng.random(n) > .995) * .6
    body = band(rng.standard_normal(n), 200, 900) * .08
    edge = np.clip(t / .3, 0, 1) * np.clip((t[-1] - t) / .3, 0, 1)
    return (whine + crackle + body) * edge


MAKERS = {'room': room, 'punch': punch, 'monitors': monitors}


def main():
    folder = ASSETS / 'sounds/waxworks'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'waxworks.{name}',
               {'sounds': [{'name': f'the_oldest_house:waxworks/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.waxworks.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.waxworks.{name}', subtitle)


if __name__ == '__main__':
    main()
