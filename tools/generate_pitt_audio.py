"""The God of the Pitt (0.4.207): eight original cues. No words, no music.

The draft: air pulled steadily through stone, a low hollow breath. The rock taking the weight: a long sub-bass groan. A slab
stepping down: a short dry grind and a settle. The hammer on a seam: a bright ring and its chips. Something in the fissure: a
wet scrape and a puff. A footstep on stone, heard early: one boot, close, with no room around it. The passage coming down: a
rumble and falling stone. The barometer's needle: a small brass tick and a tremble.
--check verifies the encoded files against art/pitt_audio207.json and that none of them clips.
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
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/pitt'
MANIFEST = ROOT / 'art/pitt_audio207.json'
RATE = 22050
NAMES = ('draft', 'groan', 'grind', 'chisel', 'scrape', 'step', 'rumble', 'tick')


def normalise(samples, peak):
    top = max(abs(x) for x in samples) or 1
    out = [x / top * peak for x in samples]
    fade = int(.008 * RATE)
    for i in range(fade):
        out[i] *= i / fade; out[-i - 1] *= i / fade
    return out


def click(t, at, freq, decay, amp=1.0):
    if t < at:
        return 0.0
    d = t - at
    return math.sin(2 * math.pi * freq * d) * math.exp(-d * decay) * amp


def draft():
    """Air drawn through a long passage: band-limited noise swelling and easing, with a hollow resonance in it."""
    rng = random.Random(701); total = 3.6; out = []; low = 0; lower = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .08 * (n - low); lower += .02 * (low - lower)
        swell = .55 + .45 * math.sin(math.pi * t / total) ** 2
        out.append((low - lower) * swell + math.sin(2 * math.pi * 86 * t) * .03 * swell)
    return normalise(out, .45)


def groan():
    """Stone under load: a sub-bass groan bending downward, with grit in it."""
    rng = random.Random(702); total = 2.4; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; f = 52 - 14 * t / total + 3 * math.sin(2 * math.pi * 1.3 * t); phase += 2 * math.pi * f / RATE
        env = min(1, t / .4) * min(1, (total - t) / .6)
        out.append((math.sin(phase) + .35 * math.sin(phase * 2.03) + rng.uniform(-1, 1) * .08) * env)
    return normalise(out, .62)


def grind():
    """A slab stepping down: a dry grinding scrape, then a settle."""
    rng = random.Random(703); total = .55; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .3 * (n - low)
        env = (1 if t < .32 else max(0, 1 - (t - .32) / .08)) * min(1, t / .02)
        out.append(low * env * (1 if int(t * 160) % 2 else .5) + click(t, .38, 90, 30, .7))
    return normalise(out, .6)


def chisel():
    """The hammer on a seam: a bright metallic ring over stone, and chips falling."""
    rng = random.Random(704); total = .7; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        chips = sum(click(t, .08 + k * .07, 2600 + k * 300, 220, .25) for k in range(4))
        out.append(click(t, .01, 3200, 28, 1.0) + click(t, .01, 5100, 40, .4) + click(t, .01, 180, 60, .6) + chips + (rng.uniform(-1, 1) * .2 if t < .03 else 0))
    return normalise(out, .62)


def scrape():
    """Something pale in the fissure: a wet scrape along stone, and a soft puff of clay."""
    rng = random.Random(705); total = 1.1; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .15 * (n - low)
        wet = 1 + .6 * math.sin(2 * math.pi * 23 * t)
        env = math.sin(math.pi * min(1, t / .8)) if t < .8 else 0
        puff = rng.uniform(-1, 1) * .5 * math.exp(-(t - .85) * 30) if t > .85 else 0
        out.append(low * wet * env + puff)
    return normalise(out, .5)


def step():
    """One boot on stone, close, with nothing around it to make an echo."""
    rng = random.Random(706); total = .35; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; grit = rng.uniform(-1, 1) * math.exp(-t * 40) * .5
        out.append(click(t, .005, 110, 35, 1.0) + click(t, .005, 420, 70, .4) + grit)
    return normalise(out, .55)


def rumble():
    """The passage coming down: a long low rumble and stones falling through it."""
    rng = random.Random(707); total = 3.0; out = []; low = 0; lower = 0
    falls = [(rng.uniform(.2, 2.6), rng.uniform(300, 900)) for _ in range(14)]
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .03 * (n - low); lower += .01 * (low - lower)
        env = min(1, t / .3) * min(1, (total - t) / .8)
        stones = sum(click(t, at, f, 50, .35) for at, f in falls)
        out.append(lower * 4 * env + stones)
    return normalise(out, .7)


def tick():
    """The barometer's needle jumping: a small brass tick, then a trembling buzz."""
    total = .6; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; tremble = math.sin(2 * math.pi * 1900 * t) * .15 * math.exp(-(t - .05) * 9) if t > .05 else 0
        out.append(click(t, .01, 4200, 160, 1.0) + click(t, .01, 1500, 90, .5) + tremble * (1 + .5 * math.sin(2 * math.pi * 31 * t)))
    return normalise(out, .5)


def encode(name, samples, folder):
    pcm = array.array('h', (int(max(-.95, min(.95, x)) * 32767) for x in samples)); path = folder / (name + '.wav')
    with wave.open(str(path), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(RATE); f.writeframes(pcm.tobytes())
    target = OUT / (name + '.ogg')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
    return {'samples': len(samples), 'rate': RATE, 'ticks': math.ceil(len(samples) / RATE * 20), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()}


def main():
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--check', action='store_true'); args = p.parse_args()
    makers = (draft, groan, grind, chisel, scrape, step, rumble, tick)
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES
        for name, info in data['sounds'].items():
            target = OUT / (name + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed God of the Pitt audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped God of the Pitt audio {name}'
        print('Verified the eight God of the Pitt cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name, make in zip(NAMES, makers):
                data['sounds'][name] = encode(name, make(), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print('Eight original mono God of the Pitt cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
