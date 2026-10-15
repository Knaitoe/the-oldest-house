"""Deterministic original non-vocal sound design. Never samples speech, screams or existing music.

The muffled grinder is a filtered copy of the SAME take, with identical duration/phase.
Only new audio files are written. Existing assets are never opened for modification.
"""
import array
import hashlib
import math
from pathlib import Path
import random
import subprocess
import tempfile
import wave

RATE = 22050
DEST = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house/sounds/borrasca'


def compose(name, seconds, sample):
    rng = random.Random(497 + sum(map(ord, name)))
    values = array.array('f')
    looping = name in ('grinder', 'town_wind')
    blend = int(.20 * RATE) if looping else 0
    count = int(seconds * RATE) + blend
    low = 0.0
    for i in range(count):
        t = i / RATE
        noise = rng.uniform(-1, 1)
        low += .025 * (noise - low)
        edge = 1 if looping else min(1, t / .10, (seconds - t) / .10)
        values.append(sample(t, noise, low) * edge)
    if looping:
        for i in range(blend):
            weight = i / blend
            values[i] = values[count - blend + i] * (1 - weight) + values[i] * weight
        del values[-blend:]
    peak = max(map(abs, values)) or 1
    scale = .78 / peak
    pcm = array.array('h', (int(max(-1, min(1, v * scale)) * 32767) for v in values))
    with tempfile.TemporaryDirectory(prefix='borrasca-audio-') as temporary:
        wav = Path(temporary) / (name + '.wav')
        with wave.open(str(wav), 'wb') as f:
            f.setnchannels(1)
            f.setsampwidth(2)
            f.setframerate(RATE)
            f.writeframes(pcm.tobytes())
        target = DEST / (name + '.ogg')
        subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(wav), '-c:a', 'libvorbis', '-q:a', '5', str(target)], check=True)
        if name == 'grinder':
            subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(wav), '-af', 'lowpass=f=780:p=2', '-c:a', 'libvorbis', '-q:a', '5', str(DEST / 'grinder_muffled.ogg')], check=True)
        rms = math.sqrt(sum(v*v for v in values) / len(values)) * scale
        print(name, 'mono', RATE, 'Hz', seconds, 's', 'peak', round(.78, 3), 'RMS', round(rms, 4), 'sha256', hashlib.sha256(target.read_bytes()).hexdigest())


if __name__ == '__main__':
    DEST.mkdir(parents=True, exist_ok=True)
    tau = math.tau
    compose('grinder', 32, lambda t, n, low: (.15 * math.sin(tau*55*t) + .08*math.sin(tau*110*t) + .035*math.sin(tau*219*t) + .08*(n-low)*( .3+.7*max(0,math.sin(tau*.75*t))**4) + .19*low) * (.85+.1*math.sin(tau*.125*t)) + .06*math.sin(tau*146.75*t)*(max(0,math.sin(tau*.25*t))**10))
    compose('motor_stop', 4, lambda t, n, low: (max(0,1-t/4)**2)*(.24*math.sin(tau*(55*t-5*t*t))+.04*n+.08*low))
    compose('tree_creak', 2.4, lambda t, n, low: math.sin(math.pi*t/2.4)**2*(.15*math.sin(tau*(104*t-12*t*t))+.025*n+.11*low))
    compose('town_wind', 32, lambda t, n, low: .09*low*(.65+.25*math.sin(tau*.0625*t))+.001*n)
