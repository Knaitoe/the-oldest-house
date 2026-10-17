"""Original Movie Night sounds (0.4.102): the score of *Teeth of Summer*, the projector, and the lake around it.

Synthesized here from oscillators and noise; no recordings and no borrowed melodies. The film's cues are pressed through
a small old speaker (narrow band, hiss, crackle and a little wobble), because that is how the lake hears them. Run from
the repository root. Existing files are kept, so the committed sounds only change when they are deleted first.
"""
import json
from pathlib import Path

import numpy as np
import soundfile

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
RATE = 44100
rng = np.random.default_rng(102)


def seconds(n):
    return int(n * RATE)


def times(n):
    return np.arange(n) / RATE


def band(x, low=None, high=None, slope=0.25):
    """Filter in the frequency domain with soft shoulders, so long cues stay fast to build."""
    spectrum = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / RATE)
    gain = np.ones_like(f)
    if high is not None:
        gain *= 1 / (1 + (f / high) ** (2 / slope))
    if low is not None:
        gain *= 1 - 1 / (1 + (f / max(1, low)) ** (2 / slope))
    return np.fft.irfft(spectrum * gain, len(x))


def envelope(n, attack, decay):
    t = times(n)
    return np.minimum(1, t / max(attack, 1e-4)) * np.exp(-t / decay)


def adsr(n, attack, release):
    t = times(n)
    total = n / RATE
    return np.minimum(1, t / max(attack, 1e-4)) * np.minimum(1, np.maximum(0, (total - t) / max(release, 1e-4)))


def place(out, sound, at):
    start = seconds(at)
    end = min(len(out), start + len(sound))
    if end > start:
        out[start:end] += sound[:end - start]


def note(name):
    names = {'C': -9, 'C#': -8, 'D': -7, 'D#': -6, 'E': -5, 'F': -4, 'F#': -3, 'G': -2, 'G#': -1, 'A': 0, 'A#': 1, 'B': 2}
    pitch, octave = name[:-1], int(name[-1])
    return 440 * 2 ** ((names[pitch] + (octave - 4) * 12) / 12)


def vibraphone(freq, length):
    n = seconds(length)
    t = times(n)
    tremolo = 1 + .25 * np.sin(2 * np.pi * 5.5 * t)
    tone = np.sin(2 * np.pi * freq * t) + .25 * np.sin(2 * np.pi * freq * 4 * t) * np.exp(-t / .08)
    return tone * tremolo * envelope(n, .004, .7)


def organ(freqs, length):
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for f in freqs:
        out += np.sin(2 * np.pi * f * t) + .4 * np.sin(2 * np.pi * f * 2 * t) + .2 * np.sin(2 * np.pi * f * 3 * t)
    return out / len(freqs) * adsr(n, .05, .2)


def bass(freq, length):
    n = seconds(length)
    t = times(n)
    tone = np.sin(2 * np.pi * freq * t) + .3 * np.sin(2 * np.pi * freq * 2 * t)
    return tone * envelope(n, .006, .35)


def strings(freqs, length, tremolo=0.0, attack=.4, release=.6):
    """A section of detuned saws, softened; tremolo bows the whole chord."""
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for f in freqs:
        for detune in (-.004, 0, .005):
            phase = (f * (1 + detune) * t + rng.random()) % 1
            out += 2 * phase - 1
    out = band(out, None, 2400)
    if tremolo:
        out *= .55 + .45 * np.abs(np.sin(np.pi * tremolo * t))
    return out / (3 * len(freqs)) * adsr(n, attack, release)


def timpani(freq, length=1.6):
    n = seconds(length)
    t = times(n)
    bend = freq * (1 + .15 * np.exp(-t / .05))
    tone = np.sin(2 * np.pi * np.cumsum(bend) / RATE)
    hit = band(rng.standard_normal(n), 60, 800) * envelope(n, .001, .03)
    return (tone * envelope(n, .002, .5) + hit * .6)


def old_speaker(x, hiss=.012, crackle=.004):
    """The lake's speakers: a narrow band, a little wobble, hiss and dust in the groove."""
    n = len(x)
    t = times(n)
    wobble = 1 + .002 * np.sin(2 * np.pi * .7 * t)
    idx = np.clip(np.cumsum(wobble) - 1, 0, n - 1)
    x = np.interp(idx, np.arange(n), x)
    x = band(x, 280, 3600)
    x = np.tanh(x * 1.6) / 1.6
    x += band(rng.standard_normal(n), 1500, 9000) * hiss
    pops = (rng.random(n) < crackle / 40) * rng.standard_normal(n) * .6
    return x + band(pops, 800, 6000)


# ------------------------------------------------------------------------------------------------ the film's cues
def calm():
    # A bright beach tune for a hotel lobby, four bars round C, A minor, F and G.
    beat = 60 / 112
    out = np.zeros(seconds(beat * 16 + 1))
    chords = [('C3', ['E4', 'G4', 'C5']), ('A2', ['E4', 'A4', 'C5']), ('F2', ['F4', 'A4', 'C5']), ('G2', ['D4', 'G4', 'B4'])]
    melody = ['E5', 'G5', 'A5', 'G5', 'E5', 'D5', 'C5', None, 'A4', 'C5', 'D5', 'E5', 'D5', 'C5', 'B4', 'D5']
    for bar, (root, chord) in enumerate(chords):
        start = bar * 4 * beat
        place(out, bass(note(root), beat * 1.5) * .5, start)
        place(out, bass(note(root) * 1.5, beat * .9) * .35, start + beat * 1.5)
        place(out, bass(note(root), beat * 1.5) * .45, start + beat * 2)
        for k in (0, 2):
            place(out, organ([note(c) for c in chord], beat * 1.4) * .22, start + beat * (k + .5))
    for i, m in enumerate(melody):
        if m:
            place(out, vibraphone(note(m), beat * 1.6) * .35, i * beat)
    for i in range(32):
        n = seconds(.04)
        place(out, band(rng.standard_normal(n), 5000, 11000) * envelope(n, .001, .015) * .08, i * beat / 2)
    return old_speaker(out)


def dread():
    # Low strings creep in under a single pulse that keeps coming faster; it stops before it arrives.
    length = 11.5
    out = np.zeros(seconds(length))
    place(out, strings([note('D2'), note('D#2'), note('A2')], length - .3, tremolo=7, attack=3.5, release=.08) * .6, 0)
    at, gap = .4, 1.25
    while at < length - .6:
        n = seconds(.5)
        t = times(n)
        pulse = (np.sin(2 * np.pi * note('D1') * t) + .5 * np.sin(2 * np.pi * note('D2') * t)) * envelope(n, .01, .22)
        place(out, pulse * .9, at)
        place(out, strings([note('A1')], .35, attack=.02, release=.2) * .5, at)
        at += gap
        gap = max(.16, gap * .82)
    rise = np.linspace(0, 1, seconds(length)) ** 3
    out[:len(rise)] *= .6 + .6 * rise
    out[-seconds(.25):] *= np.linspace(1, 0, seconds(.25))
    return old_speaker(out)


def sting():
    # One shriek: a brass cluster, a timpani blow and the strings jumping an octave.
    out = np.zeros(seconds(2.6))
    place(out, timpani(note('D2')) * 1.2, 0)
    n = seconds(1.8)
    t = times(n)
    brass = np.zeros(n)
    for f in (note('D3'), note('D#3'), note('A3'), note('G#4')):
        brass += np.sign(np.sin(2 * np.pi * f * t)) * .3 + np.sin(2 * np.pi * f * 2 * t) * .2
    place(out, band(brass, None, 3000) * envelope(n, .01, .5) * .5, 0)
    place(out, strings([note('D#6'), note('E6')], 1.4, tremolo=16, attack=.01, release=.5) * .7, .02)
    return old_speaker(out)


def finale():
    # Dawn over the water: warm strings rising in D major, and the last chord bends to minor.
    out = np.zeros(seconds(10.5))
    place(out, strings([note('D3'), note('A3'), note('F#4')], 3.4, attack=1.2, release=.8) * .55, 0)
    place(out, strings([note('B2'), note('F#3'), note('D4')], 3.2, attack=.8, release=.8) * .55, 2.8)
    place(out, strings([note('G2'), note('D3'), note('B3'), note('G4')], 3.0, attack=.8, release=.6) * .6, 5.4)
    place(out, strings([note('D3'), note('A3'), note('F4')], 2.4, tremolo=6, attack=.4, release=1.2) * .5, 8.0)
    for i, m in enumerate(['F#5', 'A5', 'B5', 'A5', 'D6', 'B5']):
        place(out, vibraphone(note(m), 1.4) * .25, .6 + i * 1.2)
    place(out, timpani(note('D2'), 2.0) * .35, 8.0)
    return old_speaker(out)


def bell():
    # The buoy on screen: its bell tolls with the swell, slower, then not at all; the water goes on.
    out = np.zeros(seconds(8.0))
    for at in (0.0, 1.1, 2.4, 3.9, 5.6):
        n = seconds(2.4)
        t = times(n)
        f = 620 * (1 + .002 * rng.standard_normal())
        tone = sum(a * np.sin(2 * np.pi * f * k * t) * np.exp(-t / d) for a, k, d in ((1, 1, .9), (.6, 2.76, .4), (.35, 5.4, .15), (.3, .5, 1.4)))
        place(out, tone * envelope(n, .002, 3) * .4, at)
    waves = band(rng.standard_normal(len(out)), 120, 900) * (.5 + .5 * np.sin(2 * np.pi * .25 * times(len(out))) ** 2)
    out += waves * .25
    return old_speaker(out)


# ------------------------------------------------------------------------------------------------ the lake's own sounds
def projector():
    # The motor's hum and the shutter's clatter, twenty-four times a second; it repeats seamlessly.
    length = 4.0
    n = seconds(length)
    t = times(n)
    out = (np.sin(2 * np.pi * 120 * t) * .25 + np.sin(2 * np.pi * 240 * t) * .12) * (1 + .1 * np.sin(2 * np.pi * 3 * t))
    for k in range(int(length * 24)):
        m = seconds(.012)
        click = band(rng.standard_normal(m), 1500, 7000) * envelope(m, .0005, .003)
        place(out, click * .5, k / 24)
    out += band(rng.standard_normal(n), 300, 2000) * .06
    return out


def reel():
    # The end of the film: its tail flaps against the gate, slower and slower, then the motor alone.
    out = np.zeros(seconds(6.0))
    at, gap = 0.0, .07
    while at < 4.6:
        m = seconds(.03)
        flap = band(rng.standard_normal(m), 600, 5000) * envelope(m, .001, .008)
        place(out, flap * .8, at)
        at += gap
        gap *= 1.045
    n = len(out)
    out += np.sin(2 * np.pi * 120 * times(n)) * .12 * np.linspace(1, .3, n)
    return out


def cut():
    # The speakers drop out: a pop, a crackle of bad contact, a moment of hum, then nothing.
    out = np.zeros(seconds(1.3))
    n = seconds(.08)
    place(out, band(rng.standard_normal(n), 80, 4000) * envelope(n, .0005, .02) * 1.4, 0)
    for at in rng.uniform(.03, .35, 14):
        m = seconds(.01)
        place(out, band(rng.standard_normal(m), 1000, 8000) * envelope(m, .0005, .003) * .6, at)
    m = seconds(.9)
    place(out, np.sin(2 * np.pi * 60 * times(m)) * .3 * np.linspace(1, 0, m) ** 2, .1)
    return out


def bump():
    # Something heavy passes under the canoe and knocks the hull; the water slaps back along the sides.
    out = np.zeros(seconds(1.8))
    n = seconds(.6)
    t = times(n)
    knock = np.sin(2 * np.pi * np.cumsum(70 * (1 + .6 * np.exp(-t / .03))) / RATE) * envelope(n, .002, .14)
    place(out, knock * 1.2, 0)
    place(out, band(rng.standard_normal(n), 120, 900) * envelope(n, .003, .06) * .8, 0)
    for at in (.18, .42, .7, 1.0):
        m = seconds(.3)
        place(out, band(rng.standard_normal(m), 300, 2600) * envelope(m, .02, .08) * .35, at)
    return out


SOUNDS = {
    'calm': (calm, 'Film music: a bright beach tune', 32),
    'dread': (dread, 'Film music: low strings creep closer', 32),
    'sting': (sting, 'Film music: a shrieking sting', 32),
    'finale': (finale, 'Film music: strings swell at dawn', 32),
    'bell': (bell, 'On the screen: a buoy bell tolls', 32),
    'projector': (projector, 'Projector whirs', 16),
    'reel': (reel, "The reel's tail flaps", 16),
    'cut': (cut, 'The speakers cut out', 24),
    'bump': (bump, 'Something knocks the hull', 16),
}


def normalized(x, peak=0.8):
    x = x - np.mean(x)
    return x / max(1e-6, np.max(np.abs(x))) * peak


def update(path, key, value):
    original = path.read_text()
    data = json.loads(original)
    data[key] = value
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + ('\n' if original.endswith('\n') else ''))


def main():
    folder = ASSETS / 'sounds/movie'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (make, subtitle, reach) in SOUNDS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(make()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'movie.{name}',
               {'sounds': [{'name': f'the_oldest_house:movie/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.movie.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.movie.{name}', subtitle)
    print(f'Movie Night: {len(SOUNDS)} subtitled sounds.')


if __name__ == '__main__':
    main()
