"""Original Winter Lake sounds (0.4.104): her knock under the ice, the ice cracking, groaning and breaking, the hand taking
the ribbon at the hole, the boys running off through the snow, and the wind across the lake.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Lake ice has its own voice, a
falling, ringing "pew" as a crack runs through it, and the cracks here are built on that. Run from the repository root.
Existing files are kept, so the committed sounds only change when they are deleted first.
"""
import numpy as np
import soundfile

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update

rng = np.random.default_rng(104)


def ring(start, end, length, decay):
    """A falling ice ring: a sine swept down in pitch, with a sibling an octave and a fifth above."""
    n = seconds(length)
    t = times(n)
    f = end + (start - end) * np.exp(-t / (length * .3))
    phase = 2 * np.pi * np.cumsum(f) / RATE
    return (np.sin(phase) + .35 * np.sin(phase * 3)) * np.exp(-t / decay)


def echo(x, delay, feedback, taps=5):
    out = x.copy()
    d = seconds(delay)
    for k in range(1, taps):
        if d * k >= len(out):
            break
        out[d * k:] += band(x, None, 4000 - k * 500)[:len(out) - d * k] * feedback ** k
    return out


def knock():
    # Something knocks once against the underside of the ice: a muffled thud, and the sheet humming with it.
    length = 1.4
    n = seconds(length)
    t = times(n)
    thud = np.sin(2 * np.pi * (70 + 60 * np.exp(-t / .03)) * t) * envelope(n, .002, .12)
    out = thud + band(rng.standard_normal(n), 80, 600) * envelope(n, .001, .03) * .6
    out += ring(900, 520, length, .35) * .12
    return echo(band(out, None, 1800), .16, .4)


def crack():
    # A crack running out across the ice: a sharp report, then the long falling ring of the sheet.
    length = 2.4
    n = seconds(length)
    out = band(rng.standard_normal(n), 300, 9000) * envelope(n, .0005, .02)
    for start, end, at, gain in ((2600, 420, 0, .5), (1900, 360, .05, .35), (3200, 600, .11, .25)):
        place(out, ring(start, end, length - at - .01, .6) * gain, at)
    return echo(out, .21, .35)


def groan():
    # The ice settling over the dark water: a long, low moan that bends in pitch.
    length = 2.8
    n = seconds(length)
    t = times(n)
    f = 85 + 40 * np.sin(2 * np.pi * .35 * t) + 25 * np.sin(2 * np.pi * 1.3 * t)
    tone = np.sin(2 * np.pi * np.cumsum(f) / RATE) + .4 * np.sin(4 * np.pi * np.cumsum(f) / RATE)
    out = tone * adsr(n, .6, 1.1) + band(rng.standard_normal(n), 60, 400) * adsr(n, .4, 1) * .25
    return band(out, None, 900)


def breaking():
    # The ice gives: a crack, plates breaking, and the splash of someone going in.
    length = 2.6
    n = seconds(length)
    out = crack()[:n] * .7
    for k in range(7):
        m = seconds(.08)
        place(out, band(rng.standard_normal(m), 600, 7000) * envelope(m, .001, .025) * .5, .08 + k * .06 + rng.uniform(0, .03))
    m = seconds(1.6)
    splash = band(rng.standard_normal(m), 150, 3500) * envelope(m, .01, .4)
    place(out, splash * .9, .25)
    return out


def hand():
    # At the hole: water lapping, a few bubbles, and something drawn down under the surface.
    length = 2.4
    n = seconds(length)
    t = times(n)
    lap = band(rng.standard_normal(n), 200, 1600) * (.5 + .5 * np.sin(2 * np.pi * 1.6 * t)) ** 2 * adsr(n, .2, .8) * .5
    out = lap
    for at in (.3, .55, .7, 1.2, 1.5):
        m = seconds(.06)
        tt = times(m)
        bubble = np.sin(2 * np.pi * (400 + 1800 * tt / .06) * tt) * envelope(m, .002, .02)
        place(out, bubble * .35, at)
    m = seconds(.9)
    gulp = band(rng.standard_normal(m), 80, 500) * envelope(m, .05, .3)
    place(out, gulp * .8, 1.3)
    return out


def run():
    # The boys running off through the snow along the bank, the crunching footsteps going away.
    length = 3.2
    n = seconds(length)
    out = np.zeros(n)
    for boy in range(4):
        at = rng.uniform(0, .2)
        gap = rng.uniform(.17, .22)
        while at < length - .2:
            m = seconds(.09)
            step = band(rng.standard_normal(m), 400, 5000) * envelope(m, .004, .03)
            place(out, step * .5 * max(0, 1 - at / length) ** 1.5, at)
            at += gap
    return out


def wind():
    # Wind across the open ice, ten seconds, rising and falling; it loops.
    length = 10.0
    n = seconds(length)
    t = times(n)
    noise = rng.standard_normal(n)
    low = band(noise, 120, 700) * (.6 + .4 * np.sin(2 * np.pi * t / length))
    high = band(noise, 900, 3000) * (.3 + .3 * np.sin(2 * np.pi * 2 * t / length + 1)) * .4
    out = low + high
    fade = seconds(.5)
    out[:fade] *= np.linspace(0, 1, fade)
    out[-fade:] *= np.linspace(1, 0, fade)
    return out


SOUNDS = {
    'knock': (knock, 'Something knocks under the ice', 24),
    'crack': (crack, 'The ice cracks', 40),
    'groan': (groan, 'The ice groans', 24),
    'break': (breaking, 'The ice breaks', 40),
    'hand': (hand, 'Water laps in the hole', 16),
    'run': (run, 'Boys run through the snow', 48),
    'wind': (wind, 'Wind across the ice', 64),
}


def main():
    folder = ASSETS / 'sounds/winter'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (make, subtitle, reach) in SOUNDS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(make()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'winter.{name}',
               {'sounds': [{'name': f'the_oldest_house:winter/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.winter.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.winter.{name}', subtitle)


if __name__ == '__main__':
    main()
