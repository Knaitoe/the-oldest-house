"""Original Costume Night sounds (0.4.103): the party's radio, the crowd, the bulbs going, Hank's megaphone, her drip and
the children giggling in the dark.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings and no borrowed melodies. Run
from the repository root. Existing files are kept, so the committed sounds only change when they are deleted first.
"""
import numpy as np
import soundfile

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, note, bass, old_speaker, normalized, update

rng = np.random.default_rng(103)


def pluck(freq, length, bright=.996):
    """A plucked string (Karplus-Strong), for the radio's surf guitar."""
    n = seconds(length)
    period = max(2, int(RATE / freq))
    buf = rng.uniform(-1, 1, period)
    out = np.zeros(n)
    for i in range(n):
        out[i] = buf[i % period]
        buf[i % period] = bright * .5 * (buf[i % period] + buf[(i + 1) % period])
    return out * envelope(n, .002, length * .7)


def spring(x, delay=.09, feedback=.45, taps=6):
    """A cheap spring reverb: a few decaying echoes, a little darker each time."""
    out = x.copy()
    d = seconds(delay)
    echo = x.copy()
    for k in range(1, taps):
        echo = band(echo, None, 5000 - k * 500) * feedback
        shifted = np.zeros_like(out)
        shifted[d * k:] = echo[:len(out) - d * k]
        out += shifted
    return out


def radio():
    # A surf tune from the town station, twelve seconds, looping: a tremolo-picked riff over a walking bass and a snare.
    beat = 60 / 150
    length = 12.0
    out = np.zeros(seconds(length + .5))
    riff = ['E3', 'G3', 'A3', 'B3', 'A3', 'G3', 'E3', 'D3']
    bars = [('E2', 0), ('A2', 5), ('B2', 7), ('E2', 0)]
    t = 0.0
    bar = 0
    while t < length - .1:
        root, shift = bars[bar % 4]
        for i, r in enumerate(riff):
            f = note(r) * 2 ** (shift / 12)
            for k in range(3):
                place(out, pluck(f, beat * .5) * .32, t + i * beat * .5 + k * beat / 6)
        for i in range(4):
            place(out, bass(note(root) * (1.5 if i % 2 else 1), beat * .9) * .5, t + i * beat)
            if i % 2:
                m = seconds(.12)
                place(out, band(rng.standard_normal(m), 900, 6000) * envelope(m, .001, .05) * .35, t + i * beat)
            m = seconds(.15)
            kick = np.sin(2 * np.pi * np.cumsum(60 + 80 * np.exp(-times(m) / .02)) / RATE) * envelope(m, .001, .08)
            place(out, kick * .45, t + i * beat)
        t += beat * 4
        bar += 1
    out = spring(out)[:seconds(length)]
    return old_speaker(out, hiss=.02, crackle=.006)


def cheer():
    # Thirty people laughing and clapping on a beach, rising and falling away.
    length = 2.6
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for _ in range(30):
        start = rng.uniform(0, .5)
        m = seconds(rng.uniform(.8, 1.8))
        f = rng.uniform(170, 420)
        tt = times(m)
        voice = np.sin(2 * np.pi * (f + 25 * np.sin(2 * np.pi * rng.uniform(4, 7) * tt)) * tt) * (np.sin(2 * np.pi * rng.uniform(5, 9) * tt) > 0)
        place(out, band(voice, 150, 2500) * envelope(m, .05, .5) * .12, start)
    for _ in range(90):
        m = seconds(.03)
        place(out, band(rng.standard_normal(m), 800, 6000) * envelope(m, .001, .01) * .5, rng.uniform(0, 1.8))
    return out * adsr(n, .05, .8)


def pop():
    # The bulbs blow one after another down the string, the radio dies, and the hum of the power goes.
    length = 1.8
    n = seconds(length)
    out = np.zeros(n)
    at = 0.0
    for k in range(9):
        m = seconds(.05)
        place(out, band(rng.standard_normal(m), 1200, 9000) * envelope(m, .0005, .012) * (1 - k * .06), at)
        at += .05 + rng.uniform(0, .06)
    t = times(n)
    hum = (np.sin(2 * np.pi * 60 * t) * .3 + np.sin(2 * np.pi * 120 * t) * .15) * np.exp(-t / .35)
    return out + hum


def megaphone():
    # Hank keys the megaphone: a click, a squeal of feedback that he ducks, and the open hiss.
    length = .9
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    m = seconds(.02)
    place(out, band(rng.standard_normal(m), 600, 4000) * envelope(m, .0005, .006), 0)
    squeal = np.sin(2 * np.pi * (2400 + 300 * t) * t) * np.minimum(1, t / .08) * np.exp(-np.maximum(0, t - .25) / .08)
    out += squeal * .35
    out += band(rng.standard_normal(n), 800, 3500) * .05
    return np.tanh(out * 2) / 2


def drip():
    # Water running off her: a few slow drops onto wet sand.
    length = 1.6
    out = np.zeros(seconds(length))
    for at in (0.0, .45, .8, 1.3):
        m = seconds(.12)
        tt = times(m)
        f = rng.uniform(700, 1100)
        drop = np.sin(2 * np.pi * (f + 900 * np.exp(-tt / .015)) * tt) * envelope(m, .001, .03)
        place(out, drop * .6, at)
    return out


def giggle():
    # Children in the dark: short breathy "hee" bursts, rising, from here and there.
    length = 1.8
    n = seconds(length)
    out = np.zeros(n)
    for voice in range(3):
        at = rng.uniform(0, .3)
        f = rng.uniform(520, 700)
        for k in range(rng.integers(3, 6)):
            m = seconds(.11)
            tt = times(m)
            tone = np.sin(2 * np.pi * (f * (1 + .1 * tt / .11)) * tt) * .5 + band(rng.standard_normal(m), 2000, 7000) * .5
            place(out, tone * envelope(m, .01, .05) * .35, at)
            at += .15 + rng.uniform(0, .05)
    return out


SOUNDS = {
    'radio': (radio, 'A surf song on a little radio', 24),
    'cheer': (cheer, 'The crowd laughs and claps', 40),
    'pop': (pop, 'The bulbs pop and go dark', 48),
    'megaphone': (megaphone, "Hank's megaphone squeals", 48),
    'drip': (drip, 'Water drips onto sand', 8),
    'giggle': (giggle, 'Children giggle in the dark', 24),
}


def main():
    folder = ASSETS / 'sounds/costume'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (make, subtitle, reach) in SOUNDS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(make()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'costume.{name}',
               {'sounds': [{'name': f'the_oldest_house:costume/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.costume.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.costume.{name}', subtitle)


if __name__ == '__main__':
    main()
