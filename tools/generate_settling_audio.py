"""Original sounds for the Settling Floor (0.4.361):
- floorboards creaking under weight;
- a section settling a step, a groan and a soft thump into the fill;
- the far wall's plaster cracking as its panel shifts, with a breath of air after;
- the cabinet scraping along the boards.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Existing files are kept.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'creak': ('Floorboards creak', 10),
    'give': ('Floorboards settle', 14),
    'seam': ('Plaster cracks', 18),
    'scrape': ('A cabinet scrapes', 12),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/settling/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'settling.{name}'] == {'sounds': [{'name': f'the_oldest_house:settling/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.settling.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.settling.{name}'] == subtitle, name
    print(f'Verified the settling floor\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402
from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(3610)


def creak_tone(length, f0, f1, rasp=0.6):
    """A wooden creak: a slowly gliding fundamental, stick-slip pulses on it and the boards' hollow body."""
    n = seconds(length)
    t = times(n)
    freq = f0 + (f1 - f0) * t / length + 6 * np.sin(2 * np.pi * 3.1 * t)
    phase = 2 * np.pi * np.cumsum(freq) / RATE
    pulses = np.sign(np.sin(phase)) * (0.5 + 0.5 * np.abs(np.sin(phase * 0.5)))
    jitter = band(rng.standard_normal(n), 300, 2400) * rasp
    body = band(pulses + jitter * np.abs(np.sin(phase)), 120, 1800)
    return body * adsr(n, length * 0.25, length * 0.35)


def creak():
    return creak_tone(1.0, 170, 230)


def give():
    n = seconds(1.5)
    out = np.zeros(n)
    place(out, creak_tone(0.7, 140, 95, 0.8) * 0.8, 0.0)
    k = seconds(0.6)
    t = times(k)
    thump = (np.sin(2 * np.pi * 58 * t) * 0.9 + band(rng.standard_normal(k), 40, 300) * 0.6) * envelope(k, 0.004, 0.12)
    place(out, thump, 0.62)
    k = seconds(0.8)
    grit = band(rng.standard_normal(k), 900, 5000) * envelope(k, 0.01, 0.25) * 0.15
    place(out, grit, 0.66)
    return out


def seam():
    n = seconds(1.8)
    out = np.zeros(n)
    for at, size in ((0.0, 1.0), (0.07, 0.7), (0.12, 0.5), (0.31, 0.8), (0.36, 0.4), (0.55, 0.3)):
        k = seconds(0.05)
        place(out, band(rng.standard_normal(k), 1200, 9000) * envelope(k, 0.0005, 0.012) * size, at)
    k = seconds(1.3)
    breath = band(rng.standard_normal(k), 400, 2500) * adsr(k, 0.4, 0.7) * 0.25
    place(out, breath, 0.45)
    return out


def scrape():
    n = seconds(0.9)
    t = times(n)
    judder = 0.6 + 0.4 * np.abs(np.sin(2 * np.pi * 23 * t + 2 * np.sin(2 * np.pi * 5 * t)))
    rasp = band(rng.standard_normal(n), 180, 1600) * judder
    rumble = band(rng.standard_normal(n), 30, 160) * 0.8
    return (rasp + rumble) * adsr(n, 0.06, 0.25)


MAKERS = {'creak': creak, 'give': give, 'seam': seam, 'scrape': scrape}


def main():
    folder = ASSETS / 'sounds/settling'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'settling.{name}',
               {'sounds': [{'name': f'the_oldest_house:settling/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.settling.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.settling.{name}', subtitle)


if __name__ == '__main__':
    main()
