"""The dark halls (0.4.208): four original cues. No words, no music.

The House's rumble when it takes a light: low, slow, felt more than heard, and nothing like the Minotaur's growl (no voice in it,
no breath; stone settling). Footsteps behind: three steps on a hard floor in a long hall, with a tail of room. A door slamming
late, far behind. A thing landing far below: small, faint and much too late.
--check verifies the encoded files against art/dark_hall_audio208.json and that none of them clips.
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
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/dark_hall'
MANIFEST = ROOT / 'art/dark_hall_audio208.json'
RATE = 22050
NAMES = ('rumble', 'steps', 'slam', 'land')


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


def tail(samples, delay, gain, repeats):
    out = list(samples) + [0.0] * int(delay * RATE * repeats)
    d = int(delay * RATE)
    for k in range(1, repeats + 1):
        g = gain ** k
        for i, x in enumerate(samples):
            out[i + d * k] += x * g
    return out


def rumble():
    """Stone settling: a very low swell with no voice in it, slow beating between two sub tones, grit at its peak."""
    rng = random.Random(801); total = 3.4; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; env = math.sin(math.pi * t / total) ** 1.5; n = rng.uniform(-1, 1); low += .01 * (n - low)
        out.append((math.sin(2 * math.pi * 31 * t) + math.sin(2 * math.pi * 33.5 * t)) * .5 * env + low * 3 * env)
    return normalise(out, .66)


def steps():
    """Three footsteps on a hard floor, a little uneven, each with the long hall's tail behind it."""
    rng = random.Random(802); total = 1.6; dry = []
    hits = (.05, .58, 1.09)
    for i in range(int(total * RATE)):
        t = i / RATE
        dry.append(sum(click(t, at, 140, 45, 1.0) + click(t, at, 900, 120, .25) + (rng.uniform(-1, 1) * .3 * math.exp(-(t - at) * 60) if t >= at else 0) for at in hits))
    return normalise(tail(dry, .19, .35, 3), .5)


def slam():
    """A door slamming far behind: the hit, the frame's rattle, the hall carrying it."""
    rng = random.Random(803); total = .9; dry = []
    for i in range(int(total * RATE)):
        t = i / RATE; rattle = rng.uniform(-1, 1) * .25 * math.exp(-(t - .06) * 25) if t > .06 else 0
        dry.append(click(t, .01, 75, 14, 1.0) + click(t, .01, 330, 40, .4) + rattle)
    return normalise(tail(dry, .27, .4, 4), .55)


def land():
    """A small thing landing much too far below: faint, dull, and gone."""
    total = .6; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; out.append(click(t, .02, 520, 70, 1.0) + click(t, .07, 610, 90, .4) + click(t, .02, 190, 40, .5))
    return normalise(out, .3)


def encode(name, samples, folder):
    pcm = array.array('h', (int(max(-.95, min(.95, x)) * 32767) for x in samples)); path = folder / (name + '.wav')
    with wave.open(str(path), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(RATE); f.writeframes(pcm.tobytes())
    target = OUT / (name + '.ogg')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
    return {'samples': len(samples), 'rate': RATE, 'ticks': math.ceil(len(samples) / RATE * 20), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()}


def main():
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--check', action='store_true'); args = p.parse_args()
    makers = (rumble, steps, slam, land)
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES
        for name, info in data['sounds'].items():
            target = OUT / (name + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed dark hall audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped dark hall audio {name}'
        print('Verified the four dark hall cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name, make in zip(NAMES, makers):
                data['sounds'][name] = encode(name, make(), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print('Four original mono dark hall cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
