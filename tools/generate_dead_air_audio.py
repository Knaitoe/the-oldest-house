"""The Dead Air Broadcast (0.4.206): seven original cues. No words, no music.

Snow on an empty channel. The receiver's dial: detents clicking, a whistle sliding across the band. Dead air: a flat tone,
pure and too steady. The feedback burst: a rising howl that snaps off. Percy's jaw: a wooden clack, twice. The strings and
the Skin-Taker's joints: a slow creak of wood and twine. A breaker thrown: a heavy thunk and a relay's buzz.
--check verifies the encoded files against art/dead_air_audio206.json and that none of them clips.
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
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/dead_air'
MANIFEST = ROOT / 'art/dead_air_audio206.json'
RATE = 22050
NAMES = ('static', 'tune', 'dead', 'feedback', 'clack', 'creak', 'breaker')


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


def static():
    """An empty channel: white noise with a set's whine in it, breathing slightly."""
    rng = random.Random(601); total = 2.0; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .75 * (n - low)
        out.append(low * (.8 + .2 * math.sin(2 * math.pi * .9 * t)) + math.sin(2 * math.pi * 15734 / 4 * t) * .02)
    return normalise(out, .42)


def tune():
    """The dial: detents clicking under the fingers while a heterodyne whistle slides down the band."""
    total = 1.1; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; f = 2400 - 1800 * t / total; phase += 2 * math.pi * f / RATE
        det = sum(click(t, k * .11, 1800, 120, .5) for k in range(10))
        out.append(math.sin(phase) * .25 * math.sin(math.pi * t / total) + det)
    return normalise(out, .55)


def dead():
    """Dead air: a flat test tone, pure and too steady, a little louder than it should be."""
    total = 1.6; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; out.append(math.sin(2 * math.pi * 1000 * t) * min(1, t / .02) * min(1, (total - t) / .05))
    return normalise(out, .32)


def feedback():
    """The feedback burst: a howl rising and tightening, then snapping off."""
    total = 1.0; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; f = 600 + 2200 * (t / .85) ** 2; phase += 2 * math.pi * f / RATE
        env = min(1, t / .6) if t < .85 else max(0, 1 - (t - .85) / .02)
        out.append((math.sin(phase) + .4 * math.sin(phase * 2.01)) * env)
    return normalise(out, .6)


def clack():
    """Percy's wooden jaw, twice."""
    total = .5; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; out.append(click(t, .02, 900, 90, 1.0) + click(t, .02, 2400, 140, .4) + click(t, .22, 820, 90, .8) + click(t, .22, 2100, 140, .3))
    return normalise(out, .6)


def creak():
    """Wood and twine taking a weight: a slow, grainy creak."""
    rng = random.Random(606); total = 1.2; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; f = 140 + 60 * math.sin(2 * math.pi * .8 * t); phase += 2 * math.pi * f / RATE
        grain = 1 if (int(t * 70) % 3) else .3
        out.append(math.sin(phase) * grain * math.sin(math.pi * t / total) * .6 + rng.uniform(-1, 1) * .05)
    return normalise(out, .5)


def breaker():
    """A breaker thrown: the handle's heavy thunk, and a relay buzzing as it holds."""
    total = 1.1; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; buzz = (1 if (t * 120) % 1 < .5 else -1) * .15 * (1 if .12 < t < .95 else 0)
        out.append(click(t, .02, 70, 12, 1.0) + click(t, .02, 600, 60, .4) + buzz)
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
    makers = (static, tune, dead, feedback, clack, creak, breaker)
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES
        for name, info in data['sounds'].items():
            target = OUT / (name + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed Dead Air audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped Dead Air audio {name}'
        print('Verified the seven Dead Air cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name, make in zip(NAMES, makers):
                data['sounds'][name] = encode(name, make(), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print('Seven original mono Dead Air cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
