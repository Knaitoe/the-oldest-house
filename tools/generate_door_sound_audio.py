"""Original sounds for sound through connected doors (0.4.229): a knock struck on a House door; that knock heard faintly through
the door it leads to; a door shutting heard as a dull thud from its other side; and four rooms as they sound through a closed door
(a clock ticking, water, music, voices), all low and muffled, as if through wood.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root.
Existing files are kept, so the committed sounds only change when they are deleted first.
"""
import json
import sys
from pathlib import Path

# The cues, their subtitles and how far they carry: the check below needs nothing but these and the files.
ROWS = {
    'knock': ('Knock on a door', 16),
    'knock_far': ('Knocking, through a door', 10),
    'thud_far': ('A door shuts, through a door', 12),
    'through_clock': ('A clock ticks behind the door', 6),
    'through_water': ('Water behind the door', 6),
    'through_music': ('Music behind the door', 6),
    'through_voices': ('Voices behind the door', 6),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/door/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'door.{name}'] == {'sounds': [{'name': f'the_oldest_house:door/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.door.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.door.{name}'] == subtitle, name
    print('Verified the doors\' seven original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(229)


def rap(muffle=None):
    # Knuckles on a panel door: a short knock of wood, its body ringing low, a click of the knuckle on top.
    m = seconds(.16)
    body = sum(np.sin(2 * np.pi * f * times(m)) * w for f, w in ((190, 1), (310, .55), (520, .25))) * envelope(m, .001, .045)
    click = band(rng.standard_normal(m), 1500, 5000) * envelope(m, .0005, .006) * .5
    out = body + click
    return band(out, 60, muffle) if muffle else out


def knock():
    out = np.zeros(seconds(.6))
    place(out, rap(), .02); place(out, rap() * .85, .2)
    return out


def knock_far():
    # The same knock from the other side of a door, and of a room: the wood's ring gone, a low thump and a little air after it.
    out = np.zeros(seconds(1.0))
    place(out, rap(520) * 1.2, .03); place(out, rap(520), .21)
    tail = band(rng.standard_normal(seconds(.5)), 80, 400) * adsr(seconds(.5), .05, .4) * .05
    place(out, tail, .22)
    return out


def thud_far():
    # A door shutting, heard from beyond it: the slab meeting the frame low and soft, the latch a dull tick.
    n = seconds(1.0)
    out = np.zeros(n)
    m = seconds(.4)
    slab = (np.sin(2 * np.pi * 62 * times(m)) + .6 * np.sin(2 * np.pi * 95 * times(m))) * envelope(m, .004, .09)
    slab += band(rng.standard_normal(m), 40, 260) * envelope(m, .002, .06) * .7
    place(out, slab, .02)
    m = seconds(.05)
    place(out, band(rng.standard_normal(m), 300, 900) * envelope(m, .001, .01) * .25, .09)
    return band(out, 30, 700)


def through_clock():
    # A wall clock in a room beyond the door: four ticks, the second of each pair softer, as heard through wood.
    out = np.zeros(seconds(2.1))
    for i in range(4):
        m = seconds(.05)
        tick = band(rng.standard_normal(m), 600, 2400) * envelope(m, .0005, .008) + np.sin(2 * np.pi * 900 * times(m)) * envelope(m, .0005, .01) * .4
        place(out, tick * (1 if i % 2 == 0 else .7), .05 + i * .5)
    return band(out, 200, 1500)


def through_water():
    # Water behind a door: a slow lap and a trickle, the brightness gone.
    n = seconds(2.4)
    out = band(rng.standard_normal(n), 80, 500) * (.6 + .4 * np.sin(2 * np.pi * .7 * times(n))) * adsr(n, .4, .8) * .5
    for at in (.4, 1.3, 1.9):
        m = seconds(.25)
        place(out, band(rng.standard_normal(m), 150, 700) * envelope(m, .01, .07) * .45, at)
    return band(out, 60, 900)


def through_music():
    # A song in a room beyond the door: only its bottom comes through, a bass line and the kick under it.
    n = seconds(2.2)
    out = np.zeros(n)
    for i, f in enumerate((55, 55, 65.4, 49)):
        m = seconds(.5)
        place(out, np.sin(2 * np.pi * f * times(m)) * adsr(m, .01, .2) * .6, i * .55)
        k = seconds(.18)
        place(out, np.sin(2 * np.pi * (70 - 30 * times(k) / .18) * times(k)) * envelope(k, .002, .05), i * .55)
    return band(out, 30, 300)


def through_voices():
    # People talking in a room beyond the door: the words lost, only the rise and fall of two or three voices.
    n = seconds(2.6)
    out = np.zeros(n)
    t = times(n)
    for f0, phase, gain in ((120, 0, .5), (200, 1.3, .4), (165, 2.4, .35)):
        syll = (np.sin(2 * np.pi * 3.1 * t + phase) > .2).astype(float)
        syll = np.convolve(syll, np.ones(800) / 800, mode='same')
        pitch = f0 * (1 + .08 * np.sin(2 * np.pi * .9 * t + phase))
        voice = np.sin(2 * np.pi * np.cumsum(pitch) / RATE) + .4 * np.sin(4 * np.pi * np.cumsum(pitch) / RATE)
        out += voice * syll * gain
    out += band(rng.standard_normal(n), 300, 900) * .03
    return band(out * adsr(n, .2, .4), 90, 700)


MAKERS = {'knock': knock, 'knock_far': knock_far, 'thud_far': thud_far, 'through_clock': through_clock, 'through_water': through_water,
          'through_music': through_music, 'through_voices': through_voices}


def main():
    folder = ASSETS / 'sounds/door'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'door.{name}',
               {'sounds': [{'name': f'the_oldest_house:door/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.door.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.door.{name}', subtitle)


if __name__ == '__main__':
    main()
