"""The horrors of the dark halls (0.4.209 onward): original cues, no words, no music. Synthesized from fixed seeds.

The closing hall (0.4.209):
  closing.grind  the walls stepping in: stone grinding on stone, and under it something slow breathing out
  closing.hold   the hall holding a hand's width from a child: a strained, held creak that does not resolve
  closing.crack  the squeeze cracking open low in a wall: a sharp split and grit falling
  closing.cry    a child crying close by: small hiccupping breaths with a voice in them, no words
  closing.call   a child calling far off: two falling notes, a long hall's echo behind
  closing.wall   a child's voice from inside a wall: the same call, muffled through stone

--check verifies the encoded files against art/horror_audio.json and that none of them clips.
"""
import argparse
import array
import hashlib
import json
import math
from pathlib import Path
import random
import subprocess
import tempfile
import wave

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/horror'
MANIFEST = ROOT / 'art/horror_audio.json'
RATE = 22050


def normalise(samples, peak):
    top = max(abs(x) for x in samples) or 1
    out = [x / top * peak for x in samples]
    fade = int(.01 * RATE)
    for i in range(min(fade, len(out) // 2)):
        out[i] *= i / fade; out[-i - 1] *= i / fade
    return out


def lowpass(samples, k):
    out = []; y = 0
    for x in samples:
        y += k * (x - y); out.append(y)
    return out


def tail(samples, delay, gain, repeats):
    out = list(samples) + [0.0] * int(delay * RATE * repeats)
    d = int(delay * RATE)
    for k in range(1, repeats + 1):
        g = gain ** k
        for i, x in enumerate(samples):
            out[i + d * k] += x * g
    return out


def grind():
    """Stone on stone: coarse filtered noise with a slow judder, and a long low exhale under it."""
    rng = random.Random(9101); total = 2.6; out = []; low = 0; band = 0
    for i in range(int(total * RATE)):
        t = i / RATE; env = min(1, t / .15) * max(0, 1 - max(0, t - 1.6) / 1.0)
        n = rng.uniform(-1, 1); band += .25 * (n - band); low += .02 * (n - low)
        judder = .55 + .45 * abs(math.sin(2 * math.pi * 7.5 * t + math.sin(t * 3)))
        breath = math.sin(2 * math.pi * 46 * t) * .35 * math.sin(math.pi * min(1, t / total)) + low * 2.2
        out.append((band * judder * .9 + breath * .6) * env)
    return normalise(out, .7)


def hold():
    """A held creak: a slow-beating low tone with grit catching in it, rising a little in pitch and not resolving."""
    rng = random.Random(9102); total = 1.8; out = []; band = 0
    for i in range(int(total * RATE)):
        t = i / RATE; env = math.sin(math.pi * t / total) ** .7
        f = 70 + 12 * t / total
        n = rng.uniform(-1, 1); band += .15 * (n - band)
        catch = band * (1 if (int(t * 23) % 5 == 0) else .25)
        out.append((math.sin(2 * math.pi * f * t) * .5 + math.sin(2 * math.pi * (f * 1.012) * t) * .4 + catch * .7) * env)
    return normalise(out, .55)


def crack():
    """A sharp split, then grit trickling down."""
    rng = random.Random(9103); total = 1.4; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        split = rng.uniform(-1, 1) * math.exp(-t * 45) * 1.2 + math.sin(2 * math.pi * 180 * t) * math.exp(-t * 30) * .6
        grit = (rng.uniform(-1, 1) * .25 if rng.random() < .08 else 0) * math.exp(-max(0, t - .1) * 2.5) if t > .08 else 0
        out.append(split + grit)
    return normalise(tail(out, .11, .3, 3), .75)


def voice(t, f, vib=5.5, depth=.025):
    """A small, breathy voice: a few harmonics with vibrato, no words."""
    ph = 2 * math.pi * f * (t + depth / (2 * math.pi * vib) * math.sin(2 * math.pi * vib * t))
    return math.sin(ph) * .6 + math.sin(2 * ph) * .25 + math.sin(3 * ph) * .1


def cry():
    """Hiccupping breaths with a high voice in them: four catches, the last one long."""
    rng = random.Random(9104); out = []
    catches = ((.0, .28, 560), (.42, .22, 600), (.78, .26, 540), (1.18, .7, 620))
    total = 2.2
    for i in range(int(total * RATE)):
        t = i / RATE; s = 0
        for at, length, f in catches:
            if at <= t < at + length:
                u = (t - at) / length; env = math.sin(math.pi * u) ** .8
                s += voice(t - at, f * (1 - .12 * u)) * env * .7 + rng.uniform(-1, 1) * .12 * env
        # The intake of breath between catches.
        s += rng.uniform(-1, 1) * .05 * (1 if any(at + length <= t < at + length + .1 for at, length, _ in catches) else 0)
        out.append(s)
    return normalise(lowpass(out, .5), .5)


def call(muffled=False):
    """Two falling notes, the way a child calls a name down a hall, with the hall behind it."""
    out = []; total = 1.3
    for i in range(int(total * RATE)):
        t = i / RATE; s = 0
        if t < .38:
            u = t / .38; s = voice(t, 700 * (1 + .03 * u)) * math.sin(math.pi * u) ** .6
        elif .46 <= t < 1.05:
            u = (t - .46) / .59; s = voice(t - .46, 560 * (1 - .1 * u)) * math.sin(math.pi * u) ** .5
        out.append(s)
    if muffled:
        out = lowpass(lowpass(out, .06), .1)
        return normalise(tail(out, .06, .35, 3), .55)
    return normalise(tail(out, .29, .45, 5), .5)


NAMES = ('closing.grind', 'closing.hold', 'closing.crack', 'closing.cry', 'closing.call', 'closing.wall')
MAKERS = {'closing.grind': grind, 'closing.hold': hold, 'closing.crack': crack, 'closing.cry': cry, 'closing.call': call, 'closing.wall': lambda: call(True)}


def file_of(name):
    return name.replace('.', '_')


def encode(name, samples, folder):
    pcm = array.array('h', (int(max(-.95, min(.95, x)) * 32767) for x in samples)); path = folder / (file_of(name) + '.wav')
    with wave.open(str(path), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(RATE); f.writeframes(pcm.tobytes())
    target = OUT / (file_of(name) + '.ogg')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
    return {'samples': len(samples), 'rate': RATE, 'ticks': math.ceil(len(samples) / RATE * 20), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()}


def main():
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--check', action='store_true'); args = p.parse_args()
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES, tuple(data['sounds'])
        for name, info in data['sounds'].items():
            target = OUT / (file_of(name) + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed horror audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped horror audio {name}'
        print(f'Verified the {len(NAMES)} horror cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name in NAMES:
                data['sounds'][name] = encode(name, MAKERS[name](), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print(f'{len(NAMES)} original mono horror cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
