"""Original sounds for A sort of thing (0.4.642, docs/SLEEPING_KING_0_4_642.md): the King's breath, in and out, each four seconds,
heard everywhere in the country and louder the nearer him; a reader's feet as a sort of thing (soft) on the country's turf.

The proposal: "In for four seconds, out for four. Everybody in the country hears it, louder the nearer." And of feet: "as they are,
soft, none." The in-breath is a long nasal draw that rises a little and catches at the top; the out-breath a slower, lower sigh
through the mouth with the faint rattle of a sleeper. Feet as they are and none need nothing of their own.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'breath_in': ('The King breathes in', 64),
    'breath_out': ('The King breathes out', 64),
    'soft_step': ('Soft footsteps', 6),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/king/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'king.{name}'] == {'sounds': [{'name': f'the_oldest_house:king/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.king.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.king.{name}'] == subtitle, name
    print(f'Verified A sort of thing\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9642)


def envelope(t, rise, fall):
    """Swell in over `rise` seconds and fade over the last `fall`, as a breath does, with no click at either end."""
    return np.clip(t / rise, 0, 1) ** 1.6 * np.clip((t[-1] - t) / fall, 0, 1) ** 1.2


def breath_in():
    # Through the nose: air hiss with a narrow, slowly rising resonance; a small catch at the top before it turns.
    n = seconds(3.9)
    t = times(n)
    air = rng.standard_normal(n)
    hiss = band(air, 900, 5200) * .5
    rise = 600 + 500 * (t / t[-1])
    nose = np.zeros(n)
    step = seconds(.05)
    for k in range(0, n, step):
        seg = air[k:k + step]
        f = float(rise[min(k, n - 1)])
        nose[k:k + step] = band(seg, f * .85, f * 1.15)[:len(seg)]
    out = (hiss + nose * 1.4) * envelope(t, 1.6, .5)
    catch = np.exp(-((t - 3.45) / .05) ** 2) * band(rng.standard_normal(n), 300, 1400) * .6
    return out + catch


def breath_out():
    # Through the mouth: lower, softer, longer at the top and dying away; under it, every so often, the rattle of a sleeper.
    n = seconds(3.9)
    t = times(n)
    air = band(rng.standard_normal(n), 180, 2400)
    out = air * envelope(t, .35, 2.4) * .9
    rattle = np.sign(np.sin(2 * np.pi * 34 * t)) * band(rng.standard_normal(n), 90, 500)
    out += rattle * envelope(t, .3, 3.0) * .18
    return out


def soft_step():
    # A foot set down on turf by somebody hardly there: a short muffled pad, the grass barely moving.
    n = seconds(.18)
    t = times(n)
    pad = band(rng.standard_normal(n), 120, 700) * np.exp(-t / .03)
    grass = band(rng.standard_normal(n), 2500, 6000) * np.exp(-t / .05) * .15
    return (pad + grass) * np.minimum(1, t / .004)


MAKERS = {'breath_in': breath_in, 'breath_out': breath_out, 'soft_step': soft_step}


def main():
    folder = ASSETS / 'sounds/king'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'king.{name}',
               {'sounds': [{'name': f'the_oldest_house:king/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.king.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.king.{name}', subtitle)


if __name__ == '__main__':
    main()
