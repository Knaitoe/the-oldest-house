"""Original sounds for the player's phone (0.4.105): the ringback heard while calling Harrigan, the line picking up for
his voicemail, the voicemail tone, a message going and a message arriving, and his own old phone ringing in a pocket.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root.
Existing files are kept, so the committed sounds only change when they are deleted first.
"""
import numpy as np
import soundfile

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update

rng = np.random.default_rng(105)


def tone(freqs, length, gain=1.0):
    n = seconds(length)
    t = times(n)
    return sum(np.sin(2 * np.pi * f * t) for f in freqs) * gain / len(freqs)


def ringback():
    # The ringing heard on a call: two seconds of the paired tone through a small earpiece, then the gap is the loop's.
    length = 2.0
    n = seconds(length)
    out = tone((440, 480), length) * adsr(n, .02, .05)
    out += band(rng.standard_normal(n), 300, 3000) * .02
    return band(out, 300, 3400)


def pickup():
    # The line opens: a click, then a dry hiss, and under it something like soil settling.
    length = 1.4
    n = seconds(length)
    out = np.zeros(n)
    m = seconds(.03)
    place(out, band(rng.standard_normal(m), 800, 6000) * envelope(m, .0005, .008), .02)
    hiss = band(rng.standard_normal(n), 2000, 7000) * adsr(n, .05, .4) * .08
    soil = band(rng.standard_normal(n), 60, 400) * (.5 + .5 * np.sin(2 * np.pi * .9 * times(n))) * adsr(n, .3, .5) * .25
    return band(out + hiss + soil, None, 3400)


def beep():
    # The voicemail tone.
    length = .55
    n = seconds(length)
    return tone((1000,), length) * adsr(n, .01, .03) * .8


def sent():
    # A message going: a short rising sweep.
    length = .3
    n = seconds(length)
    t = times(n)
    f = 600 + 900 * t / length
    return np.sin(2 * np.pi * np.cumsum(f) / RATE) * envelope(n, .01, .12)


def received():
    # A message arriving: two soft notes.
    length = .6
    out = np.zeros(seconds(length))
    for at, f in ((0, 880), (.14, 1320)):
        m = seconds(.4)
        place(out, tone((f, f * 2), .4, .7) * envelope(m, .005, .12), at)
    return out


def his_ring():
    # His own phone, an old one, ringing in a pocket: a bell trill, muffled by cloth.
    length = 2.0
    n = seconds(length)
    t = times(n)
    trill = (np.sin(2 * np.pi * 20 * t) > 0).astype(float)
    bell = tone((1100, 1650), length) * trill * adsr(n, .01, .1)
    return band(bell, None, 1800)


SOUNDS = {
    'ringback': (ringback, 'Phone rings', 8),
    'pickup': (pickup, 'Line picks up', 8),
    'beep': (beep, 'Voicemail tone', 8),
    'sent': (sent, 'Message sent', 8),
    'received': (received, 'Message received', 8),
    'his_ring': (his_ring, 'His phone rings', 16),
}


def main():
    folder = ASSETS / 'sounds/phone'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (make, subtitle, reach) in SOUNDS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(make()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'phone.{name}',
               {'sounds': [{'name': f'the_oldest_house:phone/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.phone.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.phone.{name}', subtitle)


if __name__ == '__main__':
    main()
