"""Original sounds for Not Yet (0.4.663, docs/NOT_YET_0_4_663.md): the ward's hum, and one turn of the counter; 0.4.664
(docs/NOT_YET_0_4_664.md) adds the hatch dropping open and the arms at work on the table.

The proposal: "the counter's tick, the mending, the hatch, a name called, and the silence when it looks away." This part has the place
only: its hum, which the machine's other rooms share the timbre of (mains at fifty and its harmonics, a fan's broadband air, the faint
whine of screens), and one turn of a counter's window, a small mechanical click with the drum's rattle settling after it. The rest come
with the parts that use them.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'hum': ('The ward hums', 24),
    'tick': ('A counter turns over', 16),
    'hatch': ('A hatch drops open', 24),
    'mend': ('Fine arms work', 16),
    'heat': ('A cot frame ticks with heat', 12),
    'call': ('Someone calls a name', 32),
    'groan': ('A groan, from a screen', 10),
    'quiet': ('Everything goes quiet', 16),
    'slide': ('A steel door slides open', 24),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/not_yet/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'not_yet.{name}'] == {'sounds': [{'name': f'the_oldest_house:not_yet/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.not_yet.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.not_yet.{name}'] == subtitle, name
    print(f'Verified Not Yet\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9663)


def hum():
    # Three and a half seconds of the ward: mains at fifty with its second and third, a little uneven; the fan's air under it; a thin
    # screen whine high up that comes and goes; faded in and out so it can be laid end to end.
    n = seconds(3.5)
    t = times(n)
    wobble = 1 + .004 * np.sin(2 * np.pi * .7 * t)
    mains = (np.sin(2 * np.pi * 50 * wobble * t) * .5 + np.sin(2 * np.pi * 100 * wobble * t) * .7
             + np.sin(2 * np.pi * 150 * wobble * t) * .25)
    fan = band(rng.standard_normal(n), 120, 1400) * .35
    whine = np.sin(2 * np.pi * 15600 * t) * (.03 + .02 * np.sin(2 * np.pi * .4 * t))
    edge = np.clip(t / .4, 0, 1) * np.clip((t[-1] - t) / .4, 0, 1)
    return (mains * .5 + fan + whine) * edge


def tick():
    # One window of the counter turning over: the pawl's click, the drum's flat clack a few hundredths later, a short rattle settling.
    n = seconds(.45)
    t = times(n)
    out = np.zeros(n)
    for at, lo, hi, gain, decay in ((.0, 2200, 7000, 1.0, .004), (.035, 500, 2600, .8, .012), (.07, 900, 3600, .25, .03)):
        burst = band(rng.standard_normal(n), lo, hi)
        out += burst * np.exp(-np.clip(t - at, 0, None) / decay) * (t >= at) * gain
    ring = np.sin(2 * np.pi * 1830 * t) * np.exp(-t / .05) * .12
    return out + ring


def hatch():
    # 0.4.664: the plates dropping open under a reader: the latch letting go, fifteen plates swinging down on their hinges almost
    # together, a hollow ring off the shaft's casing, and the last plate knocking against the wall it hangs on.
    n = seconds(1.6)
    t = times(n)
    out = np.zeros(n)
    latch = band(rng.standard_normal(n), 1800, 6000) * np.exp(-t / .006) * .9
    out += latch
    for k in range(9):
        at = .05 + k * .018 + rng.uniform(0, .01)
        clank = band(rng.standard_normal(n), 300, 2400) * np.exp(-np.clip(t - at, 0, None) / .03) * (t >= at)
        out += clank * (.7 - k * .05)
    ring = (np.sin(2 * np.pi * 142 * t) * .5 + np.sin(2 * np.pi * 377 * t) * .3 + np.sin(2 * np.pi * 611 * t) * .15)
    out += ring * np.exp(-np.clip(t - .06, 0, None) / .45) * (t >= .06) * .45
    knock = band(rng.standard_normal(n), 200, 1200) * np.exp(-np.clip(t - .9, 0, None) / .02) * (t >= .9) * .5
    return out + knock


def mend():
    # 0.4.664: one second of the arms at work over the table: two small servos whining up and down out of step, the tick of fine joints,
    # and twice a soft drawn sound, thread pulled through.
    n = seconds(1.0)
    t = times(n)
    whine = np.zeros(n)
    for f0, rate, phase in ((1900, 3.1, 0.0), (2600, 4.3, 1.3)):
        f = f0 * (1 + .08 * np.sin(2 * np.pi * rate * t + phase))
        whine += np.sin(2 * np.pi * np.cumsum(f) / RATE) * (.12 + .06 * np.sin(2 * np.pi * rate * t + phase))
    ticks = np.zeros(n)
    for at in np.arange(.03, 1.0, .11) + rng.uniform(-.01, .01, 9):
        ticks += band(rng.standard_normal(n), 2500, 8000) * np.exp(-np.clip(t - at, 0, None) / .003) * (t >= at) * .4
    pull = np.zeros(n)
    for at in (.25, .7):
        env = np.clip((t - at) / .05, 0, 1) * np.clip((at + .22 - t) / .12, 0, 1)
        pull += band(rng.standard_normal(n), 900, 3800) * env * .35
    edge = np.clip(t / .03, 0, 1) * np.clip((t[-1] - t) / .05, 0, 1)
    return (whine + ticks + pull) * edge


def shaped(source, formants):
    # A source through vowel resonances, in the frequency domain: each formant a soft peak of its own width and weight.
    spectrum = np.fft.rfft(source)
    f = np.fft.rfftfreq(len(source), 1 / RATE)
    gain = np.zeros_like(f)
    for centre, width, weight in formants:
        gain += weight * np.exp(-((f - centre) / width) ** 2)
    return np.fft.irfft(spectrum * gain, len(source))


def voice(t, pitch, breath=.25):
    # A glottal source: a pulse train at the given pitch (an array, so it can glide and waver), with breath under it.
    phase = np.cumsum(pitch) / RATE
    pulses = (phase % 1.0)
    source = (1 - 2 * pulses) ** 3
    return source + rng.standard_normal(len(t)) * breath


def room(x, length=.9, wet=.35):
    # A long hard room: a decaying noise tail convolved with the dry sound, by FFT.
    n = seconds(length)
    tail = rng.standard_normal(n) * np.exp(-times(n) / (length / 4))
    tail = band(tail, 200, 5000)
    out = np.fft.irfft(np.fft.rfft(x, len(x) + n) * np.fft.rfft(tail, len(x) + n), len(x) + n)[:len(x)]
    return x * (1 - wet) + out / max(1e-6, np.max(np.abs(out))) * np.max(np.abs(x)) * wet


def heat():
    # 0.4.665: a steel cot frame warming: irregular ticks as the tube expands, a faint creak drawn out under them, the hiss of coils.
    n = seconds(1.6)
    t = times(n)
    out = np.zeros(n)
    for at in np.sort(rng.uniform(.02, 1.5, 11)):
        out += band(rng.standard_normal(n), 2000, 7000) * np.exp(-np.clip(t - at, 0, None) / .0025) * (t >= at) * rng.uniform(.3, .8)
    creak_pitch = 240 + 60 * np.sin(2 * np.pi * .7 * t)
    creak = band(voice(t, creak_pitch, .6), 180, 900) * np.clip(np.sin(np.pi * t / 1.6), 0, 1) * .25
    hiss = band(rng.standard_normal(n), 3000, 9000) * .05
    return out + creak + hiss


def call():
    # 0.4.665: a name called from far down the ward: two syllables of a voice with no words in it, ah and then oh, falling in pitch, in a
    # long hard room.
    n = seconds(1.5)
    t = times(n)
    pitch = 220 * (1 - .18 * np.clip((t - .1) / 1.0, 0, 1)) * (1 + .01 * np.sin(2 * np.pi * 5.5 * t))
    src = voice(t, pitch, .3)
    ah = shaped(src, ((800, 120, 1.0), (1150, 140, .55), (2900, 260, .2)))
    oh = shaped(src, ((450, 100, 1.0), (800, 120, .45), (2830, 260, .12)))
    first = np.clip((t - .05) / .06, 0, 1) * np.clip((.55 - t) / .08, 0, 1)
    second = np.clip((t - .62) / .05, 0, 1) * np.clip((1.3 - t) / .35, 0, 1)
    dry = ah * first + oh * second
    return room(band(dry, 150, 6000), 1.1, .5)


def groan():
    # 0.4.665: a man's groan through a small speaker: a low wavering uh, its pitch sagging, cut thin, with the screen's hiss over it.
    n = seconds(2.0)
    t = times(n)
    pitch = 108 * (1 - .12 * t / 2.0) * (1 + .03 * np.sin(2 * np.pi * 3.1 * t) + .02 * rng.standard_normal(n).cumsum() / np.sqrt(np.arange(1, n + 1)))
    uh = shaped(voice(t, pitch, .45), ((620, 110, 1.0), (1200, 150, .45), (2550, 240, .15)))
    env = np.clip(t / .25, 0, 1) * np.clip((1.9 - t) / .6, 0, 1)
    speaker = band(uh * env, 320, 3200, .15)
    crush = np.round(speaker / max(1e-6, np.max(np.abs(speaker))) * 24) / 24
    hiss = band(rng.standard_normal(n), 2500, 8000) * .04
    return crush * .9 + hiss


def quiet():
    # 0.4.665: it looks away. The ward's hum cut off at once (a low drop), then nothing but the high ring in the ears, fading.
    n = seconds(3.2)
    t = times(n)
    drop = (np.sin(2 * np.pi * 50 * t) * .6 + np.sin(2 * np.pi * 100 * t) * .4) * np.exp(-t / .05)
    thump = band(rng.standard_normal(n), 30, 180) * np.exp(-t / .08) * .8
    ring = np.sin(2 * np.pi * 6200 * t) * .05 * np.clip((t - .15) / .4, 0, 1) * np.exp(-np.clip(t - .55, 0, None) / 1.2)
    return drop + thump + ring


def slide():
    # 0.4.665: a steel leaf sliding in its frame: a low rumble on rollers, a rattle in the track, and the clunk as it stops open.
    n = seconds(1.9)
    t = times(n)
    moving = np.clip(t / .15, 0, 1) * np.clip((1.45 - t) / .1, 0, 1)
    rumble = band(rng.standard_normal(n), 40, 260) * moving * .9
    rattle = band(rng.standard_normal(n), 800, 3000) * moving * (.25 + .2 * np.sin(2 * np.pi * 17 * t) ** 2)
    clunk = band(rng.standard_normal(n), 80, 900) * np.exp(-np.clip(t - 1.47, 0, None) / .04) * (t >= 1.47) * 1.2
    ring = np.sin(2 * np.pi * 96 * t) * np.exp(-np.clip(t - 1.47, 0, None) / .3) * (t >= 1.47) * .35
    return rumble + rattle + clunk + ring


MAKERS = {'hum': hum, 'tick': tick, 'hatch': hatch, 'mend': mend, 'heat': heat, 'call': call, 'groan': groan, 'quiet': quiet, 'slide': slide}


def main():
    folder = ASSETS / 'sounds/not_yet'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'not_yet.{name}',
               {'sounds': [{'name': f'the_oldest_house:not_yet/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.not_yet.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.not_yet.{name}', subtitle)


if __name__ == '__main__':
    main()
