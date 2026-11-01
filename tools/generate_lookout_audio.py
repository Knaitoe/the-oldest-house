"""The Whistlers in the Pines (0.4.202): ten original cues for the fire lookout. No words.

The whistle: two falling notes, F#5 to D5, breathy, like someone calling a dog in the dark. Three knocks on wood. A canvas
blind rolling down and catching. The glass cracking, and breaking. The teleprinter's chatter. The radio's squelch and hiss.
The diesel catching and settling. The crank's ratchet. A gust of the gale against the cab.
--check verifies the encoded files against art/lookout_audio202.json and that none of them clips.
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
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/lookout'
MANIFEST = ROOT / 'art/lookout_audio202.json'
RATE = 22050
NAMES = ('whistle', 'knock', 'shutter', 'crack', 'shatter', 'tty', 'radio', 'generator', 'crank', 'gust')
FS5, D5 = 739.99, 587.33


def normalise(samples, peak):
    top = max(abs(x) for x in samples) or 1
    out = [x / top * peak for x in samples]
    fade = int(.012 * RATE)
    for i in range(fade):
        out[i] *= i / fade; out[-i - 1] *= i / fade
    return out


def noise(rng, n):
    return [rng.uniform(-1, 1) for _ in range(n)]


def whistle():
    """Two notes, the second falling to it: a pure tone with breath in it, a little vibrato, a slide between."""
    rng = random.Random(202); total = 1.35; out = []; phase = 0; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE
        if t < .5:
            f = FS5; env = math.sin(math.pi * min(1, t / .08)) ** .5 if t < .08 else 1 - max(0, t - .4) / .1 * .7
        elif t < .58:
            q = (t - .5) / .08; f = FS5 + (D5 - FS5) * q; env = .3 + .7 * q
        else:
            f = D5; env = max(0, 1 - (t - .58) / .77) ** 1.4
        f *= 1 + .006 * math.sin(2 * math.pi * 5.2 * t)
        phase += 2 * math.pi * f / RATE
        n = rng.uniform(-1, 1); low += .3 * (n - low)
        out.append((math.sin(phase) * .8 + math.sin(phase * 2) * .06 + low * .25) * env)
    return normalise(out, .7)


def knock():
    """Three knocks on wood, a beat apart, the last a little harder."""
    rng = random.Random(203); total = 1.2; out = [0.0] * int(total * RATE)
    for k, (at, loud) in enumerate(((.0, .8), (.32, .85), (.64, 1.0))):
        low = 0
        for i in range(int(.25 * RATE)):
            n = int((at + .02) * RATE) + i
            if n >= len(out):
                break
            t = i / RATE; low = .7 * low + .3 * rng.uniform(-1, 1)
            out[n] += (math.sin(2 * math.pi * 140 * t) * math.exp(-t * 30) + low * .5 * math.exp(-t * 55)) * loud
    return normalise(out, .75)


def shutter():
    """Canvas running off a roller, a ratchet of the spring, the batten knocking home."""
    rng = random.Random(204); total = .9; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low = .9 * low + .1 * n
        rush = (n - low) * .35 * math.sin(math.pi * min(1, t / .7)) if t < .7 else 0
        tick = math.sin(2 * math.pi * 900 * t) * .25 * math.exp(-((t * 18) % 1) * 12) if t < .65 else 0
        thud = math.sin(2 * math.pi * 90 * (t - .7)) * math.exp(-(t - .7) * 25) * .8 if t >= .7 else 0
        out.append(rush + tick + thud)
    return normalise(out, .6)


def crack():
    """The glass taking a strain: a high tick, a thin running crack."""
    rng = random.Random(205); total = .6; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1)
        tick = math.sin(2 * math.pi * 3200 * t) * math.exp(-t * 60) * .6
        run = n * .3 * math.exp(-t * 9) * (1 if (int(t * 400) % 3) else .2)
        out.append(tick + run)
    return normalise(out, .55)


def shatter():
    """The glass going in: a burst, then the pieces."""
    rng = random.Random(206); total = 1.6; out = []
    pieces = [(rng.uniform(.08, 1.3), rng.uniform(1800, 5200), rng.uniform(.2, .6)) for _ in range(40)]
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1)
        burst = n * math.exp(-t * 8) * .9
        tinkle = sum(math.sin(2 * math.pi * f * (t - at)) * math.exp(-(t - at) * 40) * g for at, f, g in pieces if t >= at)
        out.append(burst + tinkle * .35)
    return normalise(out, .8)


def tty():
    """The teleprinter: a run of keys striking, a carriage moving, a bell at the end of the line."""
    rng = random.Random(207); total = 1.4; out = [0.0] * int(total * RATE)
    at = .02
    while at < 1.1:
        for i in range(int(.03 * RATE)):
            n = int(at * RATE) + i
            if n < len(out):
                out[n] += rng.uniform(-1, 1) * math.exp(-i / RATE * 220) * .7 + math.sin(2 * math.pi * 620 * i / RATE) * math.exp(-i / RATE * 160) * .3
        at += rng.uniform(.045, .08)
    for i in range(int(.25 * RATE)):
        n = int(1.12 * RATE) + i
        if n < len(out):
            t = i / RATE; out[n] += (math.sin(2 * math.pi * 2100 * t) + .4 * math.sin(2 * math.pi * 3150 * t)) * math.exp(-t * 9) * .35
    return normalise(out, .55)


def radio():
    """The set opening: a squelch, hiss with something almost in it, the squelch closing."""
    rng = random.Random(208); total = 1.3; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low = .6 * low + .4 * n
        hiss = (n - low * .5) * .4 * (1 if .08 < t < 1.15 else 0)
        ghost = math.sin(2 * math.pi * 240 * t) * math.sin(2 * math.pi * 3.1 * t) ** 2 * .08 * (1 if .3 < t < .9 else 0)
        squelch = math.sin(2 * math.pi * 1400 * t) * .4 * (math.exp(-t * 40) + (math.exp(-(t - 1.15) * 40) if t >= 1.15 else 0))
        out.append(hiss + ghost + squelch)
    return normalise(out, .55)


def generator():
    """The diesel catching: coughs, then the beat settling into a run."""
    rng = random.Random(209); total = 2.2; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        rate = 4 + min(1, t / 1.2) * 14
        beat = (t * rate) % 1
        pulse = math.exp(-beat * 9) * (.6 if t < .5 and int(t * rate) % 2 else 1)
        out.append((math.sin(2 * math.pi * 48 * t) * .7 + rng.uniform(-1, 1) * .35) * pulse * min(1, t / .1))
    return normalise(out, .7)


def crank():
    """One heave of the crank: the ratchet's pawl clicking over, a compression grunt from the engine."""
    rng = random.Random(210); total = .8; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        click = math.sin(2 * math.pi * 1800 * t) * math.exp(-((t * 14) % 1) * 30) * .4 if t < .55 else 0
        grunt = (math.sin(2 * math.pi * 70 * t) + rng.uniform(-1, 1) * .3) * math.sin(math.pi * min(1, max(0, t - .2) / .5)) * .5 if .2 < t < .7 else 0
        out.append(click + grunt)
    return normalise(out, .6)


def gust():
    """A gust against the cab: wind rising, a moan in the stay wires, falling away."""
    rng = random.Random(211); total = 2.6; out = []; low = low2 = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .04 * (n - low); low2 += .2 * (low - low2)
        swell = math.sin(math.pi * t / total) ** 1.5
        moan = math.sin(2 * math.pi * (180 + 40 * math.sin(t * 1.7)) * t) * .12 * swell
        out.append((low2 * 6 + moan) * swell)
    return normalise(out, .65)


def encode(name, samples, folder):
    pcm = array.array('h', (int(max(-.95, min(.95, x)) * 32767) for x in samples)); path = folder / (name + '.wav')
    with wave.open(str(path), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(RATE); f.writeframes(pcm.tobytes())
    target = OUT / (name + '.ogg')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
    return {'samples': len(samples), 'rate': RATE, 'ticks': math.ceil(len(samples) / RATE * 20), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()}


def main():
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--check', action='store_true'); args = p.parse_args()
    makers = (whistle, knock, shutter, crack, shatter, tty, radio, generator, crank, gust)
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES
        for name, info in data['sounds'].items():
            target = OUT / (name + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed lookout audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped lookout audio {name}'
        print('Verified the ten lookout cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name, make in zip(NAMES, makers):
                data['sounds'][name] = encode(name, make(), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print('Ten original mono lookout cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
