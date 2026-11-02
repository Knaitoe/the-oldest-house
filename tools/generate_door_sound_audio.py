"""Original sounds for sound through connected doors (0.4.229): a knock struck on a House door; that knock heard faintly through
the door it leads to; a door shutting heard as a dull thud from its other side; and four rooms as they sound through a closed door
(a clock ticking, water, music, voices), all low and muffled, as if through wood. 0.4.239 adds two: someone moving about in
there, and a room with nothing moving in it. 0.4.246 adds the rhythms of two familiar rooms: the kitchen and the pool.

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
    # 0.4.239: a room as it is now. One person going about something ordinary; and a room with nothing moving in it.
    'through_someone': ('Someone moving about behind the door', 6),
    'through_empty': ('Nothing moving behind the door', 5),
    # 0.4.246: rooms known by their own rhythm before the door is opened. The kitchen's fridge running and cutting out over its clock,
    # then the tap dripping once; the pool's hall, its pace clock's hand clicking round, and far off a swimmer turning at the wall.
    'through_kitchen': ('A fridge hums and stops behind the door; a drip', 6),
    'through_pool': ('A pace clock and a far turn at the wall, behind the door', 6),
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
    print(f'Verified the doors\' {len(ROWS)} original cues.')


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


def through_someone():
    # One person in a room beyond the door: two soft steps on boards, a chair drawn back, a cup set down; nothing said.
    out = np.zeros(seconds(2.6))
    for at in (.1, .55):
        m = seconds(.2)
        step = np.sin(2 * np.pi * 85 * times(m)) * envelope(m, .004, .05) + band(rng.standard_normal(m), 60, 300) * envelope(m, .002, .03) * .6
        place(out, step * .8, at)
    m = seconds(.45)
    t = times(m)
    scrape = band(rng.standard_normal(m), 250, 800) * (.5 + .5 * np.sin(2 * np.pi * 9 * t)) * adsr(m, .05, .25) * .55
    place(out, scrape, 1.05)
    m = seconds(.3)
    cup = (np.sin(2 * np.pi * 820 * times(m)) * .5 + np.sin(2 * np.pi * 1310 * times(m)) * .25) * envelope(m, .001, .06)
    cup += np.sin(2 * np.pi * 120 * times(m)) * envelope(m, .002, .03) * .6
    place(out, cup * .7, 1.85)
    return band(out, 55, 1100)


def through_empty():
    # A room with nothing moving in it, heard through a door: only the room itself, a low hollow air with a standing note in it.
    n = seconds(2.8)
    t = times(n)
    air = band(rng.standard_normal(n), 35, 220) * (.7 + .3 * np.sin(2 * np.pi * .35 * t))
    hollow = (np.sin(2 * np.pi * 68 * t) + .45 * np.sin(2 * np.pi * 137 * t)) * (.5 + .5 * np.sin(2 * np.pi * .2 * t + 1.1)) * .25
    return band((air * .5 + hollow) * adsr(n, .7, 1.0), 30, 400)


def hall(x, length=.9, gain=.35):
    # A big tiled room's answer to a sound: decaying noise convolved in, by FFT so a long sound stays quick to make.
    k = seconds(length)
    kernel = rng.standard_normal(k) * np.exp(-6 * times(k) / length)
    kernel /= np.sqrt(np.sum(kernel ** 2))
    n = len(x) + k
    wet = np.fft.irfft(np.fft.rfft(x, n) * np.fft.rfft(kernel, n), n)[:len(x)]
    return x + wet * gain


def through_kitchen():
    # A kitchen beyond the door: its clock ticking under everything; the fridge's motor starting, running with a little rattle in it,
    # then cutting out with a shudder and a click; a moment's quiet; and the tap, dripping once into the sink.
    n = seconds(4.6)
    out = np.zeros(n)
    for i in range(9):
        m = seconds(.05)
        tick = band(rng.standard_normal(m), 600, 2400) * envelope(m, .0005, .008) + np.sin(2 * np.pi * 900 * times(m)) * envelope(m, .0005, .01) * .4
        place(out, tick * (.55 if i % 2 == 0 else .38), .1 + i * .5)
    m = seconds(2.6)
    t = times(m)
    hum = sum(np.sin(2 * np.pi * f * t) * w for f, w in ((58, 1), (116, .55), (174, .3), (232, .12)))
    hum *= 1 + .12 * np.sin(2 * np.pi * 1.7 * t)
    rattle = band(rng.standard_normal(m), 110, 320) * (.5 + .5 * np.sin(2 * np.pi * 24 * t)) * .35
    place(out, (hum * .28 + rattle * .6) * adsr(m, .18, .04), .3)
    for at, g in ((2.9, .7), (2.98, .45)):
        k = seconds(.12)
        place(out, (np.sin(2 * np.pi * 70 * times(k)) + band(rng.standard_normal(k), 60, 250) * .6) * envelope(k, .002, .03) * g, at)
    k = seconds(.03)
    place(out, band(rng.standard_normal(k), 900, 2400) * envelope(k, .0005, .005) * .4, 3.02)
    k = seconds(.25)
    t = times(k)
    drop = np.sin(2 * np.pi * (950 + 1400 * np.minimum(t, .03) / .03) * t) * envelope(k, .001, .035)
    drop += np.sin(2 * np.pi * 160 * t) * envelope(k, .001, .012) * .35
    place(out, drop * .8, 3.95)
    return band(out, 45, 1800)


def through_pool():
    # A swimming pool beyond the door: the big room's hollow air and the water lapping at its edge; the pace clock's sweep hand
    # clicking round, once a second; and far off, a swimmer's turn against the tiles, a dull knock of a push-off and the slap of
    # water after it, the hall answering both.
    n = seconds(4.8)
    t = times(n)
    out = band(rng.standard_normal(n), 70, 450) * (.55 + .45 * np.sin(2 * np.pi * .45 * t)) * .22
    out += band(rng.standard_normal(n), 35, 140) * .12
    for i in range(5):
        m = seconds(.04)
        click = band(rng.standard_normal(m), 1200, 3800) * envelope(m, .0003, .004) + np.sin(2 * np.pi * 1650 * times(m)) * envelope(m, .0003, .006) * .5
        place(out, click * .5, .25 + i * .95)
    m = seconds(.3)
    push = (np.sin(2 * np.pi * 64 * times(m)) + band(rng.standard_normal(m), 50, 260) * .8) * envelope(m, .003, .06)
    place(out, push * .7, 3.0)
    m = seconds(.7)
    slap = band(rng.standard_normal(m), 280, 1300) * envelope(m, .002, .05) * .55 + band(rng.standard_normal(m), 150, 700) * adsr(m, .05, .45) * .25
    place(out, slap, 3.08)
    out = hall(out, .9, .45)
    return band(out * adsr(n, .3, .5), 40, 1400)


MAKERS = {'knock': knock, 'knock_far': knock_far, 'thud_far': thud_far, 'through_clock': through_clock, 'through_water': through_water,
          'through_music': through_music, 'through_voices': through_voices, 'through_someone': through_someone, 'through_empty': through_empty,
          'through_kitchen': through_kitchen, 'through_pool': through_pool}


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
