"""Original sounds for In the Tall Grass (0.4.349): a boy calling out of the grass, the wagon's hazard relay ticking, someone shouting
across the road, a car coming along the road and pulling onto the gravel, and a car pulling away.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. The voices are shaped buzz through vowel-like
resonances, so they read as someone calling without carrying words: the words are on the screen. Mono, so Minecraft places each one
where it happens (and the field places them where it wants them heard). Run from the repository root. Existing files are kept, so the
committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'boy': ('A boy calls from the grass', 40),
    'hazard': ('Hazard lights tick', 24),
    'call': ('Someone shouts', 48),
    'car': ('A car slows and pulls onto gravel', 64),
    'away': ('A car pulls away', 64),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/grass/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'grass.{name}'] == {'sounds': [{'name': f'the_oldest_house:grass/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.grass.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.grass.{name}'] == subtitle, name
    print(f'Verified the tall grass\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9349)


def voice(pitch, syllables, total, breath=.04):
    """A calling voice: a glottal buzz with a little vibrato, through two vowel resonances that move per syllable, a breath under it."""
    n = seconds(total)
    out = np.zeros(n)
    for at, length, loud, rise, (f1, f2) in syllables:
        k = seconds(length)
        tk = times(k)
        f = pitch * (1 + rise * tk / length) * (1 + .025 * np.sin(2 * np.pi * 5.2 * tk))
        phase = 2 * np.pi * np.cumsum(f) / RATE
        buzz = sum(np.sin(phase * h) / h ** 1.2 for h in range(1, 14))
        shaped = band(buzz, f1 * .7, f1 * 1.4) * 1.0 + band(buzz, f2 * .8, f2 * 1.25) * .55 + band(buzz, 120, 600) * .25
        shaped += band(rng.standard_normal(k), 900, 4000) * breath
        env = np.sin(np.pi * np.minimum(1, tk / length)) ** .6
        place(out, shaped * env * loud, at)
    return out


def boy():
    # "Hel-lo? Is some-one there?": a high young voice, the first two syllables up into a question, a gap, then four quicker, falling
    # away at the end, as if he had turned his head. The grass takes the top off it.
    s = [(0.00, .22, .9, .05, (700, 1900)), (0.24, .42, 1.0, .35, (550, 1000)),
         (1.05, .14, .7, .0, (450, 2200)), (1.21, .16, .75, .02, (650, 1700)), (1.39, .18, .8, -.05, (600, 1100)), (1.60, .40, .7, .2, (700, 1800))]
    out = voice(340, s, 2.4)
    return band(out, 180, 3200)


def hazard():
    # The relay behind the dash: a dry click on, a softer click off half a second later.
    n = seconds(.75)
    out = np.zeros(n)
    for at, strength in ((0.0, 1.0), (0.5, .55)):
        k = seconds(.03)
        place(out, band(rng.standard_normal(k), 1200, 7000) * envelope(k, .0003, .004) * strength, at)
        k = seconds(.012)
        place(out, np.sin(2 * np.pi * 2400 * times(k)) * envelope(k, .0002, .003) * strength * .5, at)
    return out


def call():
    # An adult calling out across open ground, two syllables, the second held, no words.
    s = [(0.0, .26, .9, .08, (650, 1500)), (0.3, .62, 1.0, -.12, (750, 1200))]
    return band(voice(170, s, 1.2, breath=.06), 120, 3800)


def engine(n, start_rpm, end_rpm, loud):
    tk = times(n)
    rpm = start_rpm + (end_rpm - start_rpm) * tk / (n / RATE)
    f = rpm / 60 * 2
    phase = 2 * np.pi * np.cumsum(f) / RATE
    body = np.sin(phase) * .6 + np.sin(phase * 2) * .3 + np.sign(np.sin(phase * .5)) * .15
    rumble = band(rng.standard_normal(n), 40, 260) * .5
    return (body + rumble) * loud


def car():
    # A car coming along the road, tires hissing on the asphalt, slowing, the engine dropping, then onto gravel: tires crunching to a
    # stop, the engine left idling.
    n = seconds(5.0)
    out = np.zeros(n)
    k = seconds(3.4)
    tk = times(k)
    approach = engine(k, 2400, 900, 1.0) * np.minimum(1, tk / 1.8) ** 2
    hiss = band(rng.standard_normal(k), 600, 5000) * .35 * np.minimum(1, tk / 1.8) ** 2 * np.maximum(0, 1 - (tk - 2.0) / 1.4)
    place(out, approach + hiss, 0)
    k = seconds(1.3)
    tk = times(k)
    crunch = band(rng.standard_normal(k), 800, 6000) * (rng.random(k) > .6) * np.maximum(0, 1 - tk / 1.3) * .9
    place(out, crunch, 3.0)
    k = seconds(1.8)
    place(out, engine(k, 800, 780, .55) * np.minimum(1, times(k) / .3), 3.2)
    return out


def away():
    # The engine pulling away: a gear's worth of revs climbing, gravel spat under the tires, then the hiss on the asphalt going off.
    n = seconds(4.2)
    out = np.zeros(n)
    k = seconds(3.8)
    tk = times(k)
    place(out, engine(k, 900, 2600, 1.0) * np.maximum(0, 1 - tk / 3.8) ** 1.5, 0)
    k = seconds(.7)
    place(out, band(rng.standard_normal(k), 900, 6000) * (rng.random(k) > .5) * envelope(k, .02, .3) * .8, .1)
    k = seconds(3.0)
    place(out, band(rng.standard_normal(k), 600, 5000) * .3 * np.maximum(0, 1 - times(k) / 3.0), .9)
    return out


MAKERS = {'boy': boy, 'hazard': hazard, 'call': call, 'car': car, 'away': away}


def main():
    folder = ASSETS / 'sounds/grass'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'grass.{name}',
               {'sounds': [{'name': f'the_oldest_house:grass/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.grass.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.grass.{name}', subtitle)


if __name__ == '__main__':
    main()
