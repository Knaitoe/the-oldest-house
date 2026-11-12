"""Original sounds for the Pell house (docs/PELL_HOUSE_PROPOSAL.md, "Sound"). So far: Gail's car leaving the curb once she has
handed the siblings over (docs/PELL_HOUSE_VISIT_0_4_525.md), heard from inside the house.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first. --check
verifies the pack against the list here (CI runs that).
"""
import json
import sys
from pathlib import Path

ROWS = {
    'car_away': ('A car door shuts and a car pulls away', 48),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/pell/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'pell.{name}'] == {'sounds': [{'name': f'the_oldest_house:pell/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.pell.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.pell.{name}'] == subtitle, name
    print(f'Verified the Pell house\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9525)


def car_away():
    # A car door shut from outside, heavy and padded with a latch's click; a beat; the starter's whine and the engine catching into a
    # low idle; then it pulls away, the note rising through one gear and its tyres hissing on the road, and is gone up the street.
    n = seconds(6.4)
    out = np.zeros(n)
    k = seconds(.45)
    tk = times(k)
    thud = np.sin(2 * np.pi * 62 * tk) * np.exp(-tk / .09) + band(rng.standard_normal(k), 80, 700) * np.exp(-tk / .05) * .8
    place(out, thud * .9, .1)
    k = seconds(.04)
    tk = times(k)
    place(out, band(rng.standard_normal(k), 1800, 7000) * np.exp(-tk / .006) * .5, .13)
    # The starter: a fast wavering whine for under a second.
    k = seconds(.75)
    tk = times(k)
    whine = np.sin(2 * np.pi * np.cumsum(180 + 40 * np.sin(2 * np.pi * 11 * tk)) / RATE)
    place(out, band(whine, 120, 1200) * envelope(k, .02, .5) * .35, 1.0)
    # The engine, from catching to gone: firing pulses at a rate that idles, then climbs as it pulls away, and fades with distance.
    start = 1.65
    k = seconds(6.4 - start)
    tk = times(k)
    rate = np.where(tk < 1.1, 26 + 4 * np.minimum(1, tk / .2), 30 + 34 * np.minimum(1, (tk - 1.1) / 2.4))
    phase = np.cumsum(rate) / RATE
    firing = (np.sin(2 * np.pi * phase) + .5 * np.sin(4 * np.pi * phase) + .3 * np.sin(6 * np.pi * phase))
    rumble = band(firing + band(rng.standard_normal(k), 40, 300) * .4, 30, 900)
    fade = np.where(tk < 1.4, np.minimum(1, tk / .08), np.exp(-(tk - 1.4) / 1.3))
    place(out, rumble * fade * .55, start)
    # Tyres on the road as it goes: a hiss that swells and falls away with the engine.
    tyres = band(rng.standard_normal(k), 400, 3500) * np.where(tk < 1.2, 0, np.sin(np.pi * np.clip((tk - 1.2) / 3.4, 0, 1)) ** 1.5) * .18
    place(out, tyres, start)
    return out


MAKERS = {'car_away': car_away}


def main():
    folder = ASSETS / 'sounds/pell'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'pell.{name}',
               {'sounds': [{'name': f'the_oldest_house:pell/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.pell.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.pell.{name}', subtitle)


if __name__ == '__main__':
    main()
