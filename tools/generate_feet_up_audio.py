"""Feet Up (0.4.200): three original cues for the children's game. No words.

The voices below, raised: two muffled voices behind the parlour door that rise over each other and stop. The count: a
knuckle on wood, kept soft, for each touch. The board: a loose board that creaks, tears and goes.
--check verifies the encoded files against art/feet_up_audio200.json and that none of them clips.
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
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/feet_up'
MANIFEST = ROOT / 'art/feet_up_audio200.json'
RATE = 22050
NAMES = ('shout', 'count', 'crack')


def voice(rng, pitch, syllables, start, total):
    """One voice: a buzzing tone with a slow vibrato, shaped into syllables, then low-passed twice as a door would."""
    out = [0.0] * int(total * RATE)
    for k, (at, length, loud) in enumerate(syllables):
        t0 = start + at
        for i in range(int(length * RATE)):
            t = i / RATE; q = t / length; n = int((t0 + t) * RATE)
            if n >= len(out):
                break
            f = pitch * (1 + .06 * math.sin(2 * math.pi * 5.5 * (t0 + t)) + .12 * q * (1 if k % 2 else -.5))
            phase = 2 * math.pi * f * t
            buzz = sum(math.sin(phase * h) / h for h in range(1, 7))
            formant = math.sin(phase * 4.2) * .35 + math.sin(phase * 6.8) * .18
            env = math.sin(math.pi * q) ** .7 * loud
            out[n] += (buzz * .5 + formant) * env
    low = 0; low2 = 0
    for i, x in enumerate(out):
        low += .11 * (x - low); low2 += .16 * (low - low2); out[i] = low2
    return out


def shout():
    rng = random.Random(200); total = 1.9
    a = voice(rng, 128, ((0, .22, .5), (.26, .18, .7), (.5, .34, 1.0), (.92, .2, .8)), .05, total)
    b = voice(rng, 196, ((0, .16, .4), (.2, .26, .75), (.55, .3, 1.0), (.95, .36, .9)), .32, total)
    out = []
    for i in range(len(a)):
        t = i / RATE
        swell = min(1, t / .5) * (1 if t < 1.45 else max(0, 1 - (t - 1.45) / .12))
        out.append((a[i] * .65 + b[i] * .55) * swell * .9 + rng.uniform(-.004, .004) * swell)
    # The door takes a hand at the end: one dull thud, then nothing.
    for i in range(int(.12 * RATE)):
        n = int(1.5 * RATE) + i
        if n < len(out):
            out[n] += math.sin(2 * math.pi * 62 * i / RATE) * math.exp(-i / RATE * 28) * .55
    return normalise(out, .62)


def count():
    rng = random.Random(201); total = .42; out = []
    low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; noise = rng.uniform(-1, 1); low = .72 * low + .28 * noise
        knock = math.sin(2 * math.pi * 172 * t) * math.exp(-t * 34) * .9 + low * .35 * math.exp(-t * 60)
        breath = (noise - low) * .05 * math.exp(-((t - .16) / .07) ** 2)
        out.append(knock + breath)
    return normalise(out, .4)


def crack():
    rng = random.Random(202); total = 1.05; out = []
    low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; noise = rng.uniform(-1, 1); low = .84 * low + .16 * noise
        creak = math.sin(2 * math.pi * (88 * t + 160 * t * t)) * .28 * math.sin(math.pi * min(1, t / .42)) ** 1.5 if t < .42 else 0
        tear = 0
        for when, gain in ((.4, .9), (.46, .7), (.55, .5), (.6, .35)):
            if t >= when:
                tear += (noise * .45 + low * .55) * gain * math.exp(-(t - when) * 38)
        thud = math.sin(2 * math.pi * 54 * (t - .78)) * math.exp(-(t - .78) * 22) * .6 if t >= .78 else 0
        out.append(creak + tear + thud)
    return normalise(out, .7)


def normalise(samples, peak):
    top = max(abs(x) for x in samples) or 1
    out = [x / top * peak for x in samples]
    fade = int(.012 * RATE)
    for i in range(fade):
        out[i] *= i / fade; out[-i - 1] *= i / fade
    return out


def encode(name, samples, folder):
    pcm = array.array('h', (int(max(-.95, min(.95, x)) * 32767) for x in samples)); path = folder / (name + '.wav')
    with wave.open(str(path), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(RATE); f.writeframes(pcm.tobytes())
    target = OUT / (name + '.ogg')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
    return {'samples': len(samples), 'rate': RATE, 'ticks': math.ceil(len(samples) / RATE * 20), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()}


def main():
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--check', action='store_true'); args = p.parse_args()
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES
        for name, info in data['sounds'].items():
            target = OUT / (name + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed Feet Up audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped Feet Up audio {name}'
        print('Verified the three Feet Up cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name, make in zip(NAMES, (shout, count, crack)):
                data['sounds'][name] = encode(name, make(), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print('Three original mono Feet Up cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
