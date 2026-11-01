"""The Left-Right Game (0.4.205): seven original cues. No words, no music.

The CB: a squelch, a burble of band-limited voice-shaped noise with no words in it, the squelch closing. The payphone at the
Bluejay cut: an electromechanical bell, two rings. A lit dormer window: a CRT's high whine over a mains hum. Her lunge: a
rush of air and a low hit, no scream. A flare struck: the scrape, the catch, the spitting hiss. Alice's wagon: an engine
idling, uneven. The chain-link gate: a padlock's clack and the gate dragging back.
--check verifies the encoded files against art/left_right_audio205.json and that none of them clips.
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
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/sounds/left_right'
MANIFEST = ROOT / 'art/left_right_audio205.json'
RATE = 22050
NAMES = ('cb', 'ring', 'hum', 'lunge', 'flare', 'wagon', 'gate')


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


def cb():
    """The CB: the squelch's chirp, a burble of voice-shaped noise that never becomes words, the squelch closing."""
    rng = random.Random(501); total = 1.4; out = []; low = 0; band = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .35 * (n - low); band = low - band * .3
        syll = .5 + .5 * math.sin(2 * math.pi * 4.2 * t) * math.sin(2 * math.pi * 1.3 * t)
        talk = band * syll * (1 if .12 < t < 1.25 else 0) * (.7 + .3 * math.sin(2 * math.pi * 180 * t))
        chirp = math.sin(2 * math.pi * 1450 * t) * .35 if t < .09 or 1.28 < t < 1.36 else 0
        out.append(talk + chirp)
    return normalise(out, .5)


def ring():
    """An electromechanical payphone bell: two rings of a fast clapper on two bells, a gap between."""
    total = 2.2; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; on = t < .8 or 1.2 < t < 2.0
        strike = (t * 22) % 1; env = math.exp(-strike * 6) if on else 0
        out.append((math.sin(2 * math.pi * 1250 * t) * .6 + math.sin(2 * math.pi * 1580 * t) * .35 + math.sin(2 * math.pi * 3100 * t) * .1) * env)
    return normalise(out, .58)


def hum():
    """A lit dormer window: a set's high whine over the mains, swelling and settling."""
    total = 1.8; out = []
    for i in range(int(total * RATE)):
        t = i / RATE; env = math.sin(math.pi * min(1, t / total))
        out.append((math.sin(2 * math.pi * 60 * t) * .4 + math.sin(2 * math.pi * 120 * t) * .2 + math.sin(2 * math.pi * 7867 * t) * .12) * env)
    return normalise(out, .4)


def lunge():
    """Her lunge: a rush of air rising fast, then a low dull hit and nothing."""
    rng = random.Random(504); total = .9; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += (.1 + .7 * min(1, t / .35)) * (n - low)
        rush = low * (min(1, t / .35) if t < .38 else math.exp(-(t - .38) * 25))
        out.append(rush * .8 + click(t, .38, 55, 9, 1.0) + click(t, .38, 140, 18, .5))
    return normalise(out, .8)


def flare():
    """A road flare struck: the scrape of the cap, the catch, the spitting hiss of it burning."""
    rng = random.Random(505); total = 1.6; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .7 * (n - low)
        scrape = n * .6 * (1 if t < .12 else 0)
        catch = click(t, .14, 300, 30, .7)
        hiss = low * .45 * min(1, max(0, t - .14) / .1) * (.75 + .25 * rng.random()) * (1 if t > .14 else 0)
        spit = (rng.random() < .004) * rng.uniform(.4, .8)
        out.append(scrape + catch + hiss + spit)
    return normalise(out, .6)


def wagon():
    """Alice's wagon idling: a low uneven engine, a tick in it, exhaust breathing."""
    rng = random.Random(506); total = 2.4; out = []; phase = 0
    for i in range(int(total * RATE)):
        t = i / RATE; f = 34 + 3 * math.sin(2 * math.pi * .7 * t) + rng.uniform(-.5, .5); phase += 2 * math.pi * f / RATE
        fire = max(0, math.sin(phase)) ** 3
        out.append(fire * .7 + math.sin(phase * 2) * .15 + rng.uniform(-1, 1) * .05)
    return normalise(out, .6)


def gate():
    """The padlock's clack and the chain-link gate dragged back over the asphalt."""
    rng = random.Random(507); total = 1.3; out = []; low = 0
    for i in range(int(total * RATE)):
        t = i / RATE; n = rng.uniform(-1, 1); low += .5 * (n - low)
        drag = low * .5 * (1 if .25 < t < 1.1 else 0) * (.6 + .4 * math.sin(2 * math.pi * 23 * t))
        rattle = math.sin(2 * math.pi * 2100 * t) * .15 * (1 if .25 < t < 1.1 else 0) * (rng.random() < .3)
        out.append(click(t, .02, 1800, 90, .8) + click(t, .04, 400, 50, .6) + drag + rattle)
    return normalise(out, .62)


def encode(name, samples, folder):
    pcm = array.array('h', (int(max(-.95, min(.95, x)) * 32767) for x in samples)); path = folder / (name + '.wav')
    with wave.open(str(path), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(RATE); f.writeframes(pcm.tobytes())
    target = OUT / (name + '.ogg')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(path), '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
    return {'samples': len(samples), 'rate': RATE, 'ticks': math.ceil(len(samples) / RATE * 20), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()}


def main():
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--check', action='store_true'); args = p.parse_args()
    makers = (cb, ring, hum, lunge, flare, wagon, gate)
    if args.check:
        data = json.loads(MANIFEST.read_text())
        assert tuple(data['sounds']) == NAMES
        for name, info in data['sounds'].items():
            target = OUT / (name + '.ogg'); assert hashlib.sha256(target.read_bytes()).hexdigest() == info['sha256'], f'Changed Left-Right audio {name}'
            decoded = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(target), '-f', 'f32le', '-ac', '1', '-ar', str(RATE), '-'])
            values = array.array('f', decoded); assert values and max(abs(v) for v in values) < .96, f'Clipped Left-Right audio {name}'
        print('Verified the seven Left-Right cues.')
    else:
        OUT.mkdir(parents=True, exist_ok=True); data = {'sounds': {}}
        with tempfile.TemporaryDirectory() as d:
            temp = Path(d)
            for name, make in zip(NAMES, makers):
                data['sounds'][name] = encode(name, make(), temp)
        MANIFEST.parent.mkdir(parents=True, exist_ok=True); MANIFEST.write_text(json.dumps(data, indent=2) + '\n')
        print('Seven original mono Left-Right cues encoded: ' + ', '.join(f"{n} {data['sounds'][n]['ticks']} ticks" for n in NAMES) + '.')


if __name__ == '__main__':
    main()
