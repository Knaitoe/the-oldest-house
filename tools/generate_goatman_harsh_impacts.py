"""Harsh trailer impacts for the Goatman vigil (0.4.97 follow-up, October 15 2026).

The 0.4.94 masters were the cabin knock and two older cues pushed louder: over 90% of the
knock's energy sat below 150 Hz and nothing reached 2 kHz, so it read as a soft, padded thump.
These replacements are synthesized from scratch, with no outside samples, as blows on a thin
tin camper: a hard knuckle or fist transient, the aluminium skin's short boom, an inharmonic
sheet-metal ring, the loose pane or door chattering in its frame afterwards, and driven
saturation for grit. The claw is a hit followed by nails dragged across the glass with a
stick-slip screech.

Each event now contains ONE contact. Rhythm belongs to the actual striking body;
playing four prerecorded raps for every swing made the fast assault beats pile up
into an unrelated rattle. Four separately synthesized takes per event vary the
contact, frame vibration and loose glass rather than merely shifting one pitch.
  sounds/goatman/knock_vigil*.ogg   one dry knuckle crack and short frame chatter
  sounds/goatman/claw_vigil*.ogg    a palm slap and one brief nail skid on glass
  sounds/goatman/hammer_vigil*.ogg  one heavy body blow and a settling tin frame

All three stay mono (positional), and decode without clipping.
"""
from pathlib import Path
import subprocess
import tempfile
import wave

import numpy as np
from scipy import signal

ROOT = Path(__file__).resolve().parents[1]
FOLDER = ROOT / "src/main/resources/assets/the_oldest_house/sounds/goatman"
RATE = 44100
PEAK = .9


def silence(seconds):
    return np.zeros(int(RATE * seconds))


def place(out, piece, at):
    start = int(RATE * at)
    end = min(len(out), start + len(piece))
    out[start:end] += piece[:end - start]


def band(x, low, high, order=4):
    sos = signal.butter(order, [low, high], btype="bandpass", fs=RATE, output="sos")
    return signal.sosfilt(sos, x)


def resonator(x, centre, q):
    b, a = signal.iirpeak(centre, q, fs=RATE)
    return signal.lfilter(b, a, x)


def fade(t, seconds):
    """A smooth close over the last quarter, so no element is ever cut off mid-ring."""
    edge = np.clip((seconds - t) / (.25 * seconds), 0, 1)
    return .5 - .5 * np.cos(np.pi * edge)


def decay(seconds, tau):
    t = np.arange(int(RATE * seconds)) / RATE
    return t, np.exp(-t / tau) * fade(t, seconds)


def knuckle(rng, gain, bright=1.0):
    """The bone-on-metal crack: a few milliseconds of broadband noise, mostly 1.2-9 kHz."""
    t, env = decay(.03, .0028)
    noise = rng.standard_normal(len(t))
    crack = band(noise, 1200, 9000 if bright >= 1 else 6000) * env
    tick = np.sin(2 * np.pi * rng.uniform(2700, 3600) * t) * np.exp(-t / .0016)
    return gain * (1.6 * crack / max(1e-9, np.abs(crack).max()) + .55 * tick)


def boom(rng, gain, f0, tau=.07, length=.35):
    """The tin skin flexing: a damped low tone that sags in pitch, with a short mid body."""
    t, env = decay(length, tau)
    pitch = f0 * (1 - .18 * (1 - np.exp(-t / .035)))
    low = np.sin(2 * np.pi * np.cumsum(pitch) / RATE) * env
    mid_f = f0 * rng.uniform(2.1, 2.7)
    mid = np.sin(2 * np.pi * mid_f * t + rng.uniform(0, 6)) * np.exp(-t / (tau * .55)) * fade(t, length)
    return gain * (low + .55 * mid)


def ring(rng, gain, base, length=.55, tau=.22):
    """Sheet-metal ring: inharmonic plate modes with close detuned pairs, so it beats and sounds tinny."""
    t, _ = decay(length, tau)
    out = np.zeros_like(t)
    for k, ratio in enumerate((1, 1.59, 2.14, 2.65, 3.16, 3.85, 4.52, 5.61, 6.9)):
        f = base * ratio * rng.uniform(.97, 1.03)
        life = tau / (1 + .55 * k)
        for detune in (1, 1.0 + rng.uniform(.004, .011)):
            out += np.sin(2 * np.pi * f * detune * t + rng.uniform(0, 6.28)) * np.exp(-t / life) / np.sqrt(k + 1)
    out *= fade(t, length)
    return gain * out / max(1e-9, np.abs(out).max())


def rattle(rng, gain, clicks=9, spacing=.03, length=.4, low=1800, high=7000):
    """The loose pane or door chattering in its frame after the blow."""
    out = np.zeros(int(RATE * length))
    at, level = rng.uniform(.012, .02), 1.0
    for _ in range(clicks):
        if at >= length - .01:
            break
        t, env = decay(.006, .0011)
        click = band(rng.standard_normal(len(t)), low, high, order=2) * env
        place(out, level * click / max(1e-9, np.abs(click).max()), at)
        at += spacing * rng.uniform(.6, 1.45)
        level *= rng.uniform(.68, .86)
    return gain * out


def drive(x, amount):
    """Saturate for grit, then restore the level the drive took away."""
    return np.tanh(amount * x) / np.tanh(amount)


def hit(rng, velocity, *, f0, ring_base, ring_gain=.55, rattle_gain=.5, length=.6):
    out = silence(length)
    place(out, knuckle(rng, 1.0 * velocity), 0)
    place(out, boom(rng, .9 * velocity, f0), .0015)
    place(out, ring(rng, ring_gain * velocity, ring_base, length=min(.55, length)), .001)
    place(out, rattle(rng, rattle_gain * velocity, length=min(.4, length - .01)), 0)
    return out


def scrape(rng, seconds):
    """Nails dragged across the glass: an irregular stick-slip pulse train through moving resonances."""
    n = int(RATE * seconds)
    t = np.arange(n) / RATE
    pulses = np.zeros(n)
    at = 0.0
    while at < seconds:
        index = int(at * RATE)
        pulses[index] = rng.uniform(.4, 1.0)
        # 180-320 slips a second, the rate wandering as the hand presses and lifts.
        rate = 240 + 70 * np.sin(2 * np.pi * 1.7 * at + 1.3) + rng.uniform(-50, 50)
        at += 1 / max(120, rate)
    grit = pulses + .12 * rng.standard_normal(n)
    sweep = 1 + .12 * np.sin(2 * np.pi * .9 * t) - .08 * t / seconds
    out = np.zeros(n)
    # Moving resonances, processed in short blocks so their centres can glide.
    block = 512
    for start in range(0, n, block):
        piece = grit[start:start + block]
        s = sweep[min(start, n - 1)]
        mix = .0
        for centre, q, weight in ((2350, 7, 1.0), (3450, 9, .9), (5100, 6, .6), (7200, 4, .35)):
            mix = mix + weight * resonator(piece, min(RATE / 2 - 200, centre * s), q)
        out[start:start + len(piece)] = mix
    # A thin squeal riding on top, catching and letting go about forty times a second.
    squeal_f = 3100 * sweep * (1 + .03 * np.sin(2 * np.pi * 23 * t))
    catches = np.repeat((rng.random(n // 1000 + 1) > .35).astype(float), 1000)[:n]
    catches = signal.sosfiltfilt(signal.butter(2, 90, btype="lowpass", fs=RATE, output="sos"), .3 + .7 * catches)
    squeal = np.sin(2 * np.pi * np.cumsum(squeal_f) / RATE) * catches
    out = out / max(1e-9, np.abs(out).max()) + .35 * squeal / max(1e-9, np.abs(squeal).max())
    # A ragged contact: pressed hard, slipping, catching.
    contact = .55 + .45 * np.abs(np.sin(2 * np.pi * 3.3 * t + rng.uniform(0, 3))) ** .5
    attack = np.minimum(1, t / .025)
    release = np.minimum(1, (seconds - t) / .07)
    return out * contact * attack * release


def knock(rng):
    out = silence(.36)
    # Bone against the hard frame, with a warm short body rather than a sustained electronic clang.
    place(out, knuckle(rng, rng.uniform(.85, 1.05)), 0)
    place(out, boom(rng, 1.15, rng.uniform(155, 205), tau=.027, length=.22), .001)
    place(out, boom(rng, .48, rng.uniform(370, 480), tau=.012, length=.12), .001)
    place(out, ring(rng, .17, rng.uniform(720, 870), length=.24, tau=.038), .002)
    place(out, rattle(rng, .3, clicks=5, spacing=.018, length=.18), .012)
    return drive(out, 1.35)


def hammer(rng):
    out = silence(.68)
    place(out, knuckle(rng, 1.5), 0)
    place(out, knuckle(rng, .8), .004)
    place(out, boom(rng, 1.1, rng.uniform(67, 82), tau=.06, length=.4), .002)
    place(out, boom(rng, .75, rng.uniform(165, 195), tau=.035, length=.22), .001)
    place(out, ring(rng, .4, rng.uniform(360, 440), length=.64, tau=.12), .001)
    place(out, rattle(rng, .75, clicks=12, spacing=.024, length=.48), .006)
    # Things inside shaking: a short high jingle off the shelves.
    place(out, rattle(rng, .5, clicks=10, spacing=.018, length=.35, low=3500, high=9500), .03)
    return drive(out, 1.8)


def claw(rng):
    out = silence(.64)
    place(out, hit(rng, 1.0, f0=rng.uniform(145, 175), ring_base=rng.uniform(620, 780), ring_gain=.23, rattle_gain=.3, length=.4), 0)
    # A hand sliding off the pane, with intermittent contact instead of a long synthetic squeal.
    place(out, .45 * scrape(rng, rng.uniform(.27, .36)), .08)
    return drive(out, 1.5)


def normalise(x):
    x = x - np.mean(x)
    x = x * fade(np.arange(len(x)) / RATE, len(x) / RATE) ** .25
    return x * (PEAK / max(1e-9, np.abs(x).max()))


def encode(samples, target):
    with tempfile.TemporaryDirectory(prefix="oth-goatman-impacts-") as temporary:
        wav = Path(temporary) / "impact.wav"
        with wave.open(str(wav), "wb") as output:
            output.setnchannels(1); output.setsampwidth(2); output.setframerate(RATE)
            output.writeframes((np.clip(samples, -1, 1) * 32767).astype("<i2").tobytes())
        subprocess.run(["ffmpeg", "-nostdin", "-v", "error", "-y", "-i", str(wav), "-ac", "1", "-ar", str(RATE),
                        "-c:a", "libvorbis", "-q:a", "6", str(target)], check=True)


def main(folder=FOLDER):
    folder = Path(folder)
    folder.mkdir(parents=True, exist_ok=True)
    for name, make, seed in (("knock", knock, 4497), ("claw", claw, 3297), ("hammer", hammer, 6197)):
        for take in range(4):
            suffix = "" if take == 0 else f"_{take}"
            encode(normalise(make(np.random.default_rng(seed + take * 7919))), folder / f"{name}_vigil{suffix}.ogg")
    print(f"Twelve short mono Goatman impact takes generated in {folder}.")


if __name__ == "__main__":
    import sys
    main(sys.argv[1] if len(sys.argv) > 1 else FOLDER)
