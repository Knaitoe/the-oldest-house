"""Original sounds for the night ward's long night (0.4.330): the incubator monitor keeping time with a heart, soft-soled footsteps on
the ward floor, and curtain rings running along a ceiling track.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root. Existing files
are kept, so the committed sounds only change when they are deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'pulse': ('The monitor beeps', 12),
    'step': ('Footsteps', 16),
    'curtain': ('A curtain is drawn', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/ward/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'ward.{name}'] == {'sounds': [{'name': f'the_oldest_house:ward/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.ward.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.ward.{name}'] == subtitle, name
    print(f'Verified the night ward\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(4330)


def pulse():
    # One clean beep, a little lower and softer than the alarm, with the faint ring of a small plastic speaker.
    n = seconds(.32)
    out = np.zeros(n)
    k = seconds(.11)
    tk = times(k)
    tone = np.sin(2 * np.pi * 980 * tk) + .18 * np.sin(2 * np.pi * 1960 * tk) + .06 * np.sin(2 * np.pi * 2940 * tk)
    gate = np.clip(np.minimum(tk / .004, (.11 - tk) / .012), 0, 1)
    place(out, tone * gate * .5, 0.0)
    k = seconds(.2)
    place(out, np.sin(2 * np.pi * 980 * times(k)) * np.exp(-times(k) * 30) * .04, .11)
    return out


def step():
    # A soft heel on vinyl, then the sole rolling and a small squeak as it leaves the floor.
    n = seconds(.42)
    out = np.zeros(n)
    k = seconds(.09)
    tk = times(k)
    place(out, (np.sin(2 * np.pi * 85 * tk) * .8 + band(rng.standard_normal(k), 120, 900) * .5) * envelope(k, .003, .04), 0.0)
    k = seconds(.16)
    place(out, band(rng.standard_normal(k), 900, 4200) * envelope(k, .03, .07) * .12, .05)
    k = seconds(.06)
    tk = times(k)
    chirp = np.sin(2 * np.pi * np.cumsum(2300 + 900 * tk / .06) / RATE)
    place(out, chirp * envelope(k, .01, .03) * .045, .22)
    return out


def curtain():
    # A run of small rings sliding along a metal track, the cloth swishing with them, and the last ring knocking the end stop.
    n = seconds(1.3)
    t = times(n)
    out = band(rng.standard_normal(n), 500, 5000) * np.sin(np.pi * np.clip(t / 1.05, 0, 1)) ** 1.5 * .18
    at = .04
    while at < 1.0:
        k = seconds(.035)
        tk = times(k)
        f = rng.uniform(3200, 4800)
        place(out, (np.sin(2 * np.pi * f * tk) * .35 + band(rng.standard_normal(k), 2500, 8000) * .5) * envelope(k, .001, .012) * .35, at)
        at += rng.uniform(.035, .08)
    k = seconds(.18)
    tk = times(k)
    place(out, (np.sin(2 * np.pi * 1600 * tk) * .4 + band(rng.standard_normal(k), 1000, 6000) * .4) * envelope(k, .001, .05) * .55, 1.02)
    return out


MAKERS = {'pulse': pulse, 'step': step, 'curtain': curtain}


def main():
    folder = ASSETS / 'sounds/ward'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'ward.{name}',
               {'sounds': [{'name': f'the_oldest_house:ward/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.ward.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.ward.{name}', subtitle)


if __name__ == '__main__':
    main()
