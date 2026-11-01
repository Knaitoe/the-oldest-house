"""The Polaroid Trail (0.4.204): seven original cues. No words, no music.

The flash charging: a thin whine rising out of hearing. The flash: a hard pop with a bright tail. The shutter: a click
and the print pushed out with a short motor buzz. A balloon's rubber: a squeak and the string's tap. Josh's walkie: a
squelch, a burst of static, the squelch again. The projector: a clattering motor with the gate ticking. The power going:
a hum dropping away and a relay's thunk into silence.
--check verifies the encoded files against art/polaroid_trail_audio204.json and that none of them clips.
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
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/polaroid'
MANIFEST = ROOT / 'art/polaroid_trail_audio204.json'
RATE = 22050
NAMES = ('charge', 'flash', 'click', 'balloon', 'walkie', 'projector', 'power')


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


def charge():
    """The flash's capacitor charging: a thin whine sweeping up from 2 kHz to near the top of hearing, swelling as it goes."""
    total = 1.5; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; f = 2000 + 7000 * (t / total) ** 1.6
        phase += 2 * math.pi * f / RATE
        out.append(math.sin(phase) * (.25 + .75 * t / total) * .6 + math.sin(phase * 2) * .08)
    return normalise(out, .4)


def flash():
    """The flash: a hard pop, a crack of noise, and a short bright ringing tail."""
    rng = random.Random(402); total = .7; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .8 * (n - low)
        out.append(low * math.exp(-t * 30) + click(t, 0, 180, 25, 1.0) + click(t, 0, 4200, 18, .25))
    return normalise(out, .75)


def click_():
    """The shutter: a crisp click, then the print pushed out by a little motor."""
    total = .9; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        motor = (math.sin(2 * math.pi * 230 * t) * .5 + math.sin(2 * math.pi * 460 * t) * .2) * (1 if .18 < t < .72 else 0) * (1 + .3 * math.sin(2 * math.pi * 38 * t))
        out.append(click(t, .01, 2600, 120, .9) + click(t, .03, 700, 80, .6) + motor * .45 + click(t, .72, 300, 60, .4))
    return normalise(out, .65)


def balloon():
    """A balloon touched: the rubber's squeak, the string tapping against it."""
    total = .8; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; f = 520 + 380 * math.sin(math.pi * min(1, t / .35))
        phase += 2 * math.pi * f / RATE
        squeak = math.sin(phase) * (1 if t < .35 else 0) * math.sin(math.pi * min(1, t / .35)) * .6
        out.append(squeak + click(t, .45, 900, 70, .3) + click(t, .58, 1100, 80, .2))
    return normalise(out, .55)


def walkie():
    """Josh's walkie-talkie: the squelch's chirp, a burst of band-limited static, the squelch again."""
    rng = random.Random(405); total = 1.2; out = []; low = 0; high = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .45 * (n - low); high = low - high * .2
        open_ = .12 < t < 1.0
        chirp = math.sin(2 * math.pi * 1700 * t) * .4 if t < .1 or 1.02 < t < 1.12 else 0
        out.append(chirp + high * (.7 if open_ else 0) * (.8 + .2 * math.sin(2 * math.pi * 7 * t)))
    return normalise(out, .55)


def projector():
    """A film projector running: a clattering motor and the gate's claw ticking over at twenty-four a second."""
    rng = random.Random(406); total = 2.4; out = []
    for i in range(int(total * RATE)):
        t = i / RATE
        motor = math.sin(2 * math.pi * 75 * t) * .3 + math.sin(2 * math.pi * 150 * t) * .15
        tick = math.exp(-((t * 24) % 1) * 30) * (1 + rng.uniform(-.25, .25))
        out.append(motor + tick * (math.sin(2 * math.pi * 1900 * t) * .3 + rng.uniform(-1, 1) * .12))
    return normalise(out, .55)


def power():
    """The power going: the building's hum sinking and dying, a relay's heavy thunk, then nothing."""
    total = 1.6; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; f = 60 * max(.15, 1 - t / 1.0)
        phase += 2 * math.pi * f / RATE
        hum = (math.sin(phase) * .5 + math.sin(phase * 2) * .25) * max(0, 1 - t / 1.0)
        out.append(hum + click(t, 1.0, 90, 14, 1.0) + click(t, 1.0, 1400, 60, .3))
    return normalise(out, .7)


def encode(name, samples, folder):
    pcm = array.array('h', (int(max(-.95, min(.95, x)) * 32767) for x in samples)); path = folder / (name + '.wav')
    with wave.open(str(path), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(RATE); f.writeframes(pcm.tobytes())
    target = OUT / (name + '.ogg')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
    return {'samples': len(samples), 'rate': RATE, 'ticks': math.ceil(len(samples) / RATE * 20), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()}


def main():
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--check', action='store_true'); args = p.parse_args()
    makers = (charge, flash, click_, balloon, walkie, projector, power)
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES
        for name, info in data['sounds'].items():
            target = OUT / (name + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed Polaroid Trail audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped Polaroid Trail audio {name}'
        print('Verified the seven Polaroid Trail cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name, make in zip(NAMES, makers):
                data['sounds'][name] = encode(name, make(), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print('Seven original mono Polaroid Trail cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
