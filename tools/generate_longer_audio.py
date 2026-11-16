"""Original sounds for A Little Longer (0.4.622): Rowan humming at the bench, and a radio being tuned through static.

The proposal: "The player hears a radio being tuned and someone humming before seeing Rowan." Rowan "hums the same few notes when
concentrating", so the hum is always the same five notes, taken a little differently each time only by where it starts in the take.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'hum': ('Someone hums', 12),
    'tuning': ('A radio is tuned', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/longer/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'longer.{name}'] == {'sounds': [{'name': f'the_oldest_house:longer/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.longer.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.longer.{name}'] == subtitle, name
    print(f'Verified A Little Longer\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9622)

# The five notes, in semitones over the hummer's low note, and how long each is held (seconds). A falling-then-rising turn, unhurried.
TUNE = ((4, .55), (2, .4), (0, .7), (2, .35), (-3, 1.1))


def voiced(f0, n):
    """A closed-mouth hum at this pitch: a soft glottal buzz shaped by a nasal resonance, with a slow, slight wobble."""
    t = times(n)
    wobble = 1 + .006 * np.sin(2 * np.pi * 4.8 * t + rng.uniform(0, 6))
    phase = 2 * np.pi * np.cumsum(f0 * wobble) / RATE
    buzz = sum(np.sin(k * phase) / k ** 1.6 for k in range(1, 9))
    hum = band(buzz, 90, 900) + .35 * band(buzz, 220, 320)
    # Each note swells in and settles out, the breath taken between them.
    edge = np.minimum(1, t / .09) * np.minimum(1, (t[-1] - t) / .14)
    return hum * edge ** 1.5


def hum():
    base = 146.8  # D3: a low, easy note for anybody's voice
    out = np.zeros(seconds(sum(d for _, d in TUNE) + .8))
    at = .1
    for step, held in TUNE:
        f = base * 2 ** (step / 12)
        place(out, voiced(f, seconds(held)), at)
        at += held + .06
    breath = band(rng.standard_normal(len(out)), 600, 3000) * .015
    return out + breath


def tuning():
    # A valve set's dial turned slowly through the band: hiss rising and falling, whistles sweeping past as carriers go by, a voice
    # and a few bars of music almost found and lost, then the hiss again.
    n = seconds(4.2)
    t = times(n)
    hiss = band(rng.standard_normal(n), 300, 4200) * (.35 + .1 * np.sin(2 * np.pi * .7 * t))
    out = hiss.copy()
    for centre, width, depth in ((.7, .35, .5), (1.9, .25, .7), (3.2, .4, .55)):
        near = np.exp(-((t - centre) / width) ** 2)
        # the heterodyne whistle swings through zero beat as the dial passes the station
        f = 40 + 1600 * np.abs(t - centre) / width
        whistle = np.sin(2 * np.pi * np.cumsum(f) / RATE) * near * .25
        out += whistle
        out *= 1 - depth * near * .6
    # what was nearly a station: a band-limited murmur, like speech through a wall, at the second carrier
    murmur = band(rng.standard_normal(n), 250, 1100) * (np.sin(2 * np.pi * 3.1 * t) > 0) * np.exp(-((t - 1.9) / .22) ** 2) * .6
    music = (np.sin(2 * np.pi * 392 * t) + .5 * np.sin(2 * np.pi * 494 * t)) * np.exp(-((t - 3.2) / .3) ** 2) * .18
    crackle = (rng.random(n) > .9993) * rng.standard_normal(n) * .8
    return band(out + murmur + music, 200, 4500) + crackle


MAKERS = {'hum': hum, 'tuning': tuning}


def main():
    folder = ASSETS / 'sounds/longer'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'longer.{name}',
               {'sounds': [{'name': f'the_oldest_house:longer/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.longer.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.longer.{name}', subtitle)


if __name__ == '__main__':
    main()
