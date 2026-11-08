"""Original sounds for the Resonant Pipes (0.4.360): three steady notes hummed into the pipework (low, middle and high, each two seconds
so they can be laid end to end), the reeds buzzing against a wrong note, and the catch on the door lifting.
Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Existing files are kept.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'low': ('A low note hums in the pipes', 16),
    'middle': ('A middle note hums in the pipes', 16),
    'high': ('A high note hums in the pipes', 16),
    'buzz': ('Reeds buzz', 12),
    'latch': ('A catch lifts', 14),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/resonance/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'resonance.{name}'] == {'sounds': [{'name': f'the_oldest_house:resonance/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.resonance.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.resonance.{name}'] == subtitle, name
    print(f'Verified the resonant pipes\' {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402
from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(3600)


def hum(freq):
    """A pipe-organ note: the fundamental, soft octaves and a little breath, eased in and out so notes overlap without clicks."""
    n = seconds(2.0)
    t = times(n)
    tone = sum(np.sin(2 * np.pi * freq * k * t) * a for k, a in ((1, 1), (2, .35), (3, .12), (4, .05)))
    breath = band(rng.standard_normal(n), freq, freq * 6) * .05
    return (tone * .5 + breath) * adsr(n, .25, .35)


def buzz():
    n = seconds(1.2)
    t = times(n)
    rattle = np.sign(np.sin(2 * np.pi * 52 * t)) * band(rng.standard_normal(n), 400, 3500) * .35
    beat = (np.sin(2 * np.pi * 220 * t) + np.sin(2 * np.pi * 233 * t)) * .2
    return (rattle + beat) * adsr(n, .05, .3)


def latch():
    n = seconds(1.0)
    out = np.zeros(n)
    k = seconds(.08)
    place(out, band(rng.standard_normal(k), 1500, 7000) * envelope(k, .001, .03) * .8, 0.0)
    k = seconds(.9)
    t = times(k)
    ring = sum(np.sin(2 * np.pi * f * t) * a for f, a in ((660, 1), (990, .4), (1320, .2)))
    place(out, ring * envelope(k, .002, .35) * .3, .05)
    return out


MAKERS = {'low': lambda: hum(110), 'middle': lambda: hum(165), 'high': lambda: hum(247), 'buzz': buzz, 'latch': latch}


def main():
    folder = ASSETS / 'sounds/resonance'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'resonance.{name}',
               {'sounds': [{'name': f'the_oldest_house:resonance/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.resonance.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.resonance.{name}', subtitle)


if __name__ == '__main__':
    main()
