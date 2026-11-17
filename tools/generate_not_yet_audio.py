"""Original sounds for Not Yet (0.4.663, docs/NOT_YET_0_4_663.md): the ward's hum, and one turn of the counter.

The proposal: "the counter's tick, the mending, the hatch, a name called, and the silence when it looks away." This part has the place
only: its hum, which the machine's other rooms share the timbre of (mains at fifty and its harmonics, a fan's broadband air, the faint
whine of screens), and one turn of a counter's window, a small mechanical click with the drum's rattle settling after it. The rest come
with the parts that use them.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'hum': ('The ward hums', 24),
    'tick': ('A counter turns over', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/not_yet/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'not_yet.{name}'] == {'sounds': [{'name': f'the_oldest_house:not_yet/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.not_yet.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.not_yet.{name}'] == subtitle, name
    print(f'Verified Not Yet\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9663)


def hum():
    # Three and a half seconds of the ward: mains at fifty with its second and third, a little uneven; the fan's air under it; a thin
    # screen whine high up that comes and goes; faded in and out so it can be laid end to end.
    n = seconds(3.5)
    t = times(n)
    wobble = 1 + .004 * np.sin(2 * np.pi * .7 * t)
    mains = (np.sin(2 * np.pi * 50 * wobble * t) * .5 + np.sin(2 * np.pi * 100 * wobble * t) * .7
             + np.sin(2 * np.pi * 150 * wobble * t) * .25)
    fan = band(rng.standard_normal(n), 120, 1400) * .35
    whine = np.sin(2 * np.pi * 15600 * t) * (.03 + .02 * np.sin(2 * np.pi * .4 * t))
    edge = np.clip(t / .4, 0, 1) * np.clip((t[-1] - t) / .4, 0, 1)
    return (mains * .5 + fan + whine) * edge


def tick():
    # One window of the counter turning over: the pawl's click, the drum's flat clack a few hundredths later, a short rattle settling.
    n = seconds(.45)
    t = times(n)
    out = np.zeros(n)
    for at, lo, hi, gain, decay in ((.0, 2200, 7000, 1.0, .004), (.035, 500, 2600, .8, .012), (.07, 900, 3600, .25, .03)):
        burst = band(rng.standard_normal(n), lo, hi)
        out += burst * np.exp(-np.clip(t - at, 0, None) / decay) * (t >= at) * gain
    ring = np.sin(2 * np.pi * 1830 * t) * np.exp(-t / .05) * .12
    return out + ring


MAKERS = {'hum': hum, 'tick': tick}


def main():
    folder = ASSETS / 'sounds/not_yet'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'not_yet.{name}',
               {'sounds': [{'name': f'the_oldest_house:not_yet/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.not_yet.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.not_yet.{name}', subtitle)


if __name__ == '__main__':
    main()
