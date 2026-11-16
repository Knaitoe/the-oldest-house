"""Original sounds for A Little Longer (0.4.622): Rowan humming at the bench, and a radio being tuned through static; 0.4.623: the
burst when the mended set is first switched on ("The radio produces a harsh burst of noise."); 0.4.624: the kettle
coming to the boil and clicking off, tea poured into a cup, and the toaster's timer ticking and the slices popping up; 0.4.625: the tune
through the kitchen radio ("A short original tune comes through clearly enough to recognize."), whole and turned down.

The proposal: "The player hears a radio being tuned and someone humming before seeing Rowan." Rowan "hums the same few notes when
concentrating", so the hum is always the same five notes, taken a little differently each time only by where it starts in the take.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'hum': ('Someone hums', 12),
    'tuning': ('A radio is tuned', 16),
    'burst': ('A radio bursts and pops', 16),
    'kettle': ('A kettle boils and clicks off', 10),
    'pour': ('Tea is poured', 8),
    'toaster': ('A toaster pops up', 10),
    'tune': ('A tune plays on the radio', 14),
    'tune_low': ('A tune plays quietly on the radio', 8),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/longer/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'longer.{name}'] == {'sounds': [{'name': f'the_oldest_house:longer/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.longer.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.longer.{name}'] == subtitle, name
    print(f'Verified A Little Longer\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9622)

# The five notes, in semitones over the hummer's low note, and how long each is held (seconds). A falling-then-rising turn, unhurried.
TUNE = ((4, .55), (2, .4), (0, .7), (2, .35), (-3, 1.1))


def voiced(f0, n):
    """A closed-mouth hum at this pitch: a soft glottal buzz shaped by a nasal resonance, with a slow, slight wobble."""
    t = times(n)
    wobble = 1 + .006 * np.sin(2 * np.pi * 4.8 * t + rng.uniform(0, 6))
    phase = 2 * np.pi * np.cumsum(f0 * wobble) / RATE
    buzz = sum(np.sin(k * phase) / k ** 1.6 for k in range(1, 9))
    hum = band(buzz, 90, 900) + .35 * band(buzz, 220, 320)
    # Each note swells in and settles out, the breath taken between them.
    edge = np.minimum(1, t / .09) * np.minimum(1, (t[-1] - t) / .14)
    return hum * edge ** 1.5


def hum():
    base = 146.8  # D3: a low, easy note for anybody's voice
    out = np.zeros(seconds(sum(d for _, d in TUNE) + .8))
    at = .1
    for step, held in TUNE:
        f = base * 2 ** (step / 12)
        place(out, voiced(f, seconds(held)), at)
        at += held + .06
    breath = band(rng.standard_normal(len(out)), 600, 3000) * .015
    return out + breath


def tuning():
    # A valve set's dial turned slowly through the band: hiss rising and falling, whistles sweeping past as carriers go by, a voice
    # and a few bars of music almost found and lost, then the hiss again.
    n = seconds(4.2)
    t = times(n)
    hiss = band(rng.standard_normal(n), 300, 4200) * (.35 + .1 * np.sin(2 * np.pi * .7 * t))
    out = hiss.copy()
    for centre, width, depth in ((.7, .35, .5), (1.9, .25, .7), (3.2, .4, .55)):
        near = np.exp(-((t - centre) / width) ** 2)
        # the heterodyne whistle swings through zero beat as the dial passes the station
        f = 40 + 1600 * np.abs(t - centre) / width
        whistle = np.sin(2 * np.pi * np.cumsum(f) / RATE) * near * .25
        out += whistle
        out *= 1 - depth * near * .6
    # what was nearly a station: a band-limited murmur, like speech through a wall, at the second carrier
    murmur = band(rng.standard_normal(n), 250, 1100) * (np.sin(2 * np.pi * 3.1 * t) > 0) * np.exp(-((t - 1.9) / .22) ** 2) * .6
    music = (np.sin(2 * np.pi * 392 * t) + .5 * np.sin(2 * np.pi * 494 * t)) * np.exp(-((t - 3.2) / .3) ** 2) * .18
    crackle = (rng.random(n) > .9993) * rng.standard_normal(n) * .8
    return band(out + murmur + music, 200, 4500) + crackle


def burst():
    # A valve set switched on with a fault: a moment of rising hum as it warms, then a harsh blast of broadband noise with a howl in it,
    # a loud pop, crackle as something arcs, and the hum dying away.
    n = seconds(2.2)
    t = times(n)
    warm = np.sin(2 * np.pi * 100 * t) * .12 * np.clip(t / .35, 0, 1) * (t < .45)
    blast_env = np.where(t < .42, 0, np.exp(-(t - .42) / .28)) * (t >= .42)
    blast = band(rng.standard_normal(n), 120, 6500) * blast_env
    howl_f = 900 - 500 * np.clip((t - .42) / .6, 0, 1)
    howl = np.sin(2 * np.pi * np.cumsum(howl_f) / RATE) * blast_env * .5
    pop = np.zeros(n)
    k = seconds(.43)
    pop[k:k + seconds(.012)] = np.hanning(seconds(.012)) * 2.2
    crackle = (rng.random(n) > .996) * rng.standard_normal(n) * 1.1 * np.where((t > .45) & (t < 1.4), 1, 0)
    hum_out = np.sin(2 * np.pi * 50 * t) * .2 * np.where(t > .5, np.exp(-(t - .5) / .5), 0)
    return warm + blast + howl + band(pop, 60, 5000) + crackle + hum_out


def kettle():
    # An electric kettle from cold: a low rumble of first bubbles growing into a rushing seethe, the pitch of the rush rising as the
    # water nears the boil, a few seconds of full rolling boil, then the thermostat's dry click and the boil collapsing into ticks.
    n = seconds(7.5)
    t = times(n)
    grow = np.clip(t / 5.2, 0, 1) ** 1.6
    after = np.where(t > 6.1, np.exp(-(t - 6.1) / .35), 1)
    noise = rng.standard_normal(n)
    rush = (band(noise, 300, 2500) * (1 - grow) + band(noise, 1000, 5000) * grow) * (.15 + .7 * grow) * after
    rumble = band(rng.standard_normal(n), 60, 260) * (.3 + .5 * grow) * after
    bubbles = np.zeros(n)
    for at in np.sort(rng.uniform(.4, 6.3, 140)):
        k = seconds(at)
        f = rng.uniform(500, 1400) * (1 + .6 * min(1, at / 5))
        m = seconds(.025)
        if k + m < n:
            bubbles[k:k + m] += np.sin(2 * np.pi * f * times(m) * (1 + 2 * times(m) / .025)) * np.hanning(m) * rng.uniform(.2, .6)
    click = np.zeros(n)
    k = seconds(6.1)
    click[k:k + seconds(.006)] = np.hanning(seconds(.006)) * 2.4
    ticks = (rng.random(n) > .9995) * rng.standard_normal(n) * .5 * np.where(t > 6.2, np.exp(-(t - 6.2) / .6), 0)
    return rush + rumble + bubbles * after + band(click, 800, 7000) + ticks


def pour():
    # Tea poured from a pot into a cup: the first splash on the cup's bottom, then a steady stream whose resonance rises as the cup
    # fills, and the last drops.
    n = seconds(2.6)
    t = times(n)
    stream = (t > .12) & (t < 2.05)
    fill = np.clip((t - .12) / 1.9, 0, 1)
    out = np.zeros(n)
    texture = np.abs(band(rng.standard_normal(n), 8, 60))
    for f0, gain in ((380, .5), (760, .3), (1140, .2)):
        # the cup's air column shortening: each resonance swept upward, set ringing by the stream's turbulence
        f = f0 * (1 + 1.1 * fill)
        out += np.sin(2 * np.pi * np.cumsum(f) / RATE) * texture * gain
    out += band(rng.standard_normal(n), 1200, 5000) * .25
    out *= stream * (.6 + .4 * np.sin(2 * np.pi * 7 * t) ** 2)
    splash = band(rng.standard_normal(n), 900, 6000) * np.where(t > .12, np.exp(-(t - .12) / .07), 0) * .9
    drops = np.zeros(n)
    for at in (2.15, 2.3, 2.48):
        k, m = seconds(at), seconds(.03)
        drops[k:k + m] = np.sin(2 * np.pi * 1500 * times(m)) * np.hanning(m) * .5
    return out + splash + drops


def toaster():
    # A pop-up toaster near its end (the lever went down some time before, with its own click): the timer ticking while the elements
    # glow, then the spring throwing the slices up with a bright clack at two seconds, and the slices settling in the slots.
    n = seconds(2.6)
    t = times(n)
    out = np.zeros(n)

    def hit(at, length, lo, hi, gain):
        k, m = seconds(at), seconds(length)
        out[k:k + m] += band(rng.standard_normal(m), lo, hi) * np.exp(-times(m) / (length / 4)) * gain
    for at in np.arange(.05, 1.9, .19):
        hit(at, .01, 2500, 7000, .25)
    hum_ = np.sin(2 * np.pi * 120 * t) * .04 * (t < 2.0)
    hit(2.0, .07, 700, 7000, 1.6)
    spring = np.sin(2 * np.pi * 420 * t) * np.where(t > 2.0, np.exp(-(t - 2.0) / .2), 0) * (t > 2.0) * .3
    hit(2.13, .04, 300, 2500, .7)
    return out + hum_ + spring

# The tune (0.4.625): an original eight-bar melody in a lilting three-time, piano over a walking bass and brushes, as a small dance band
# would play it into a valve set's speaker. Notes are semitones over C4 with lengths in beats; a rest is None.
MELODY = ((7, 1), (9, .5), (7, .5), (4, 1), (2, 2), (None, 1), (0, 1), (2, .5), (4, .5), (7, 1), (9, 2), (None, 1),
          (12, 1), (11, .5), (9, .5), (7, 1), (4, 2), (5, 1), (4, 1), (2, 1), (0, 1), (2, 3))
BASS = (-12, -5, -8, -10, -7, -3, -5, -12)  # one root a bar
BEAT = .42


def piano(f, n):
    t = times(n)
    tone = sum(np.sin(2 * np.pi * f * k * t) * (.6 ** (k - 1)) * np.exp(-t * (1.6 + .9 * k)) for k in range(1, 6))
    return tone * np.minimum(1, t / .004)


def band_tune():
    total = sum(d for _, d in MELODY) * BEAT + 1.2
    n = seconds(total)
    out = np.zeros(n)
    at = .1
    for step, dur in MELODY:
        if step is not None:
            f = 261.63 * 2 ** (step / 12)
            place(out, piano(f, seconds(dur * BEAT + .6)) * .5, at)
            place(out, piano(f * 2 ** (-5 / 12), seconds(dur * BEAT + .4)) * .16, at + .01)
        at += dur * BEAT
    bar = 3 * BEAT
    for i, root in enumerate(BASS):
        f = 261.63 * 2 ** (root / 12) / 2
        for beat in range(3):
            g = f * (1, 2 ** (7 / 12), 2 ** (4 / 12))[beat]
            k = seconds(.1 + i * bar + beat * BEAT)
            m = seconds(BEAT * .9)
            if k + m < n:
                tt = times(m)
                out[k:k + m] += np.sin(2 * np.pi * g * tt) * np.exp(-tt * 3) * .3
            # brushes on the beats, a soft swish
            hk, hm = seconds(.1 + i * bar + beat * BEAT), seconds(.09)
            if hk + hm < n:
                out[hk:hk + hm] += band(rng.standard_normal(hm), 3000, 9000) * np.exp(-times(hm) / .03) * (.12 if beat else .2)
    # into a small speaker through a valve set: the band narrowed, a little hum and crackle
    radio = band(out, 280, 3800)
    t = times(n)
    radio += np.sin(2 * np.pi * 50 * t) * .012 + band(rng.standard_normal(n), 1500, 6000) * .012
    radio += (rng.random(n) > .9996) * rng.standard_normal(n) * .25
    return radio


def tune():
    return band_tune()


def tune_low():
    # the same take turned down: the low end lost first, as a small speaker does
    return band(band_tune(), 420, 3200) * .5


MAKERS = {'hum': hum, 'tuning': tuning, 'burst': burst, 'kettle': kettle, 'pour': pour, 'toaster': toaster, 'tune': tune, 'tune_low': tune_low}


def main():
    folder = ASSETS / 'sounds/longer'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'longer.{name}',
               {'sounds': [{'name': f'the_oldest_house:longer/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.longer.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.longer.{name}', subtitle)


if __name__ == '__main__':
    main()
