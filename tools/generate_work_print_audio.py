"""Work print (0.4.203): nine original cues for the video shop. No words, no music.

A tape pushed into a deck: the flap, the clunk of the carriage taking it. The deck's motor and heads: a hum with a slow
wow under it and the hiss of tape. The eject: a motor whine and the carriage's snap. The shop's phone: an old two-bell ring,
twice. The snow on an empty channel. The splicer: the blade coming down, the tape pulled off its roll. The flatbed: a
heavier motor, the sprockets ticking over. The record button: a short beep, the relay. The camera: the shutter's whirr.
--check verifies the encoded files against art/work_print_audio203.json and that none of them clips.
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
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/work_print'
MANIFEST = ROOT / 'art/work_print_audio203.json'
RATE = 22050
NAMES = ('insert', 'deck', 'eject', 'ring', 'snow', 'splice', 'flatbed', 'rec', 'camera')


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


def insert():
    """The flap pushed in, a scrape of plastic, then the carriage pulling the tape down with a clunk."""
    rng = random.Random(301); total = .9; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .25 * (n - low)
        scrape = low * .5 * (1 if .05 < t < .28 else 0) * math.sin(math.pi * min(1, max(0, t - .05) / .23))
        motor = math.sin(2 * math.pi * (110 + 40 * t) * t) * .25 if .3 < t < .62 else 0
        out.append(click(t, .02, 900, 60, .7) + scrape + motor + click(t, .62, 160, 28, 1.0) + click(t, .63, 2400, 120, .3))
    return normalise(out, .7)


def deck():
    """The deck playing: a motor hum with a slow wow, the heads' whine, tape hiss."""
    rng = random.Random(302); total = 2.4; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .6 * (n - low)
        wow = 1 + .01 * math.sin(2 * math.pi * .7 * t)
        out.append(math.sin(2 * math.pi * 60 * wow * t) * .35 + math.sin(2 * math.pi * 120 * wow * t) * .15 + math.sin(2 * math.pi * 3150 * t) * .03 + low * .12)
    return normalise(out, .55)


def eject():
    """The eject: a motor winding up, the carriage rising, the snap of the flap."""
    total = .8; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        motor = math.sin(2 * math.pi * (180 + 260 * t) * t) * .3 * math.sin(math.pi * min(1, t / .5)) if t < .5 else 0
        out.append(motor + click(t, .5, 140, 30, 1.0) + click(t, .55, 1800, 90, .5))
    return normalise(out, .7)


def ring():
    """An old desk phone: two bells struck fast by a clapper, two seconds of it, a little silence after."""
    total = 2.6; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        on = t < 1.9
        strike = (t * 20) % 1
        env = math.exp(-strike * 5) if on else math.exp(-(t - 1.9) * 7) * math.exp(-strike * 2)
        bell = math.sin(2 * math.pi * 1100 * t) * .6 + math.sin(2 * math.pi * 1400 * t) * .4 + math.sin(2 * math.pi * 2750 * t) * .15
        out.append(bell * env)
    return normalise(out, .62)


def snow():
    """An empty channel: white noise with the set's whine in it."""
    rng = random.Random(305); total = 2.0; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .7 * (n - low)
        out.append(low * .8 + math.sin(2 * math.pi * 15734 / 4 * t) * .02)
    return normalise(out, .45)


def splice():
    """The splicer: the blade's chop, then a strip of tape torn off its roll and pressed down."""
    rng = random.Random(306); total = 1.0; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .5 * (n - low)
        tear = low * .5 * (1 if .35 < t < .6 else 0) * (.6 + .4 * math.sin(t * 300))
        out.append(click(t, .05, 2200, 80, .8) + click(t, .06, 300, 40, .9) + tear + click(t, .75, 200, 50, .4))
    return normalise(out, .65)


def flatbed():
    """The flatbed running: a heavier motor, the sprockets ticking over at twenty-four a second."""
    rng = random.Random(307); total = 2.4; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        motor = math.sin(2 * math.pi * 50 * t) * .4 + math.sin(2 * math.pi * 100 * t) * .2
        tick = math.exp(-((t * 24) % 1) * 40) * .35 * (1 + rng.uniform(-.2, .2))
        out.append((motor + tick * math.sin(2 * math.pi * 1300 * t)) * min(1, t / .3))
    return normalise(out, .6)


def rec():
    """The record button: a relay's click and a short beep."""
    total = .45; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        beep = math.sin(2 * math.pi * 1760 * t) * .5 if .08 < t < .26 else 0
        out.append(click(t, .01, 600, 70, .8) + beep)
    return normalise(out, .55)


def camera():
    """The camera's run: a shutter's whirr, quick, slowing as it is let go."""
    rng = random.Random(309); total = 1.4; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; rate = 24 * (1 if t < .9 else max(.2, 1 - (t - .9) * 1.6))
        phase += rate / RATE
        flutter = math.exp(-(phase % 1) * 18)
        out.append(flutter * math.sin(2 * math.pi * 900 * t) * .5 + rng.uniform(-1, 1) * .08 * flutter + math.sin(2 * math.pi * 140 * t) * .15)
    return normalise(out, .6)


def encode(name, samples, folder):
    pcm = array.array('h', (int(max(-.95, min(.95, x)) * 32767) for x in samples)); path = folder / (name + '.wav')
    with wave.open(str(path), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(RATE); f.writeframes(pcm.tobytes())
    target = OUT / (name + '.ogg')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
    return {'samples': len(samples), 'rate': RATE, 'ticks': math.ceil(len(samples) / RATE * 20), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()}


def main():
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--check', action='store_true'); args = p.parse_args()
    makers = (insert, deck, eject, ring, snow, splice, flatbed, rec, camera)
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES
        for name, info in data['sounds'].items():
            target = OUT / (name + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed video shop audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped video shop audio {name}'
        print('Verified the nine video shop cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name, make in zip(NAMES, makers):
                data['sounds'][name] = encode(name, make(), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print('Nine original mono video shop cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
