"""Original sounds for A sort of thing (0.4.642, docs/SLEEPING_KING_0_4_642.md): the King's breath, in and out, each four seconds,
heard everywhere in the country and louder the nearer him; a reader's feet as a sort of thing (soft) on the country's turf.

The proposal: "In for four seconds, out for four. Everybody in the country hears it, louder the nearer." And of feet: "as they are,
soft, none." The in-breath is a long nasal draw that rises a little and catches at the top; the out-breath a slower, lower sigh
through the mouth with the faint rattle of a sleeper. Feet as they are and none need nothing of their own.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'breath_in': ('The King breathes in', 64),
    'breath_out': ('The King breathes out', 64),
    'soft_step': ('Soft footsteps', 6),
    # 0.4.643: he stirs; and the crow whose shadow ends the brothers' battle.
    'stir': ('The King stirs in his sleep', 48),
    'crow': ('Great wings pass over', 96),
    'train': ('A train gets up steam', 48),
    'whistle': ('A train whistles', 96),
    # 0.4.646: the court's pack going up and coming down.
    'cards': ('A pack of cards flies up and falls', 64),
    # 0.4.647: the Knight coming off his horse in his armour; and his song, eight bars, sung by him or by a reader who has heard it.
    'knight_fall': ('Armour clatters to the ground', 32),
    'knight_song': ('Someone sings the Knight\'s song', 32),
    # 0.4.648: a thing at the feast introduced and filled in; and the crown coming down on a reader.
    'introduced': ('Something at the table fills in', 24),
    'crowned': ('A crown comes down', 24),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/king/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'king.{name}'] == {'sounds': [{'name': f'the_oldest_house:king/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.king.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.king.{name}'] == subtitle, name
    print(f'Verified A sort of thing\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9642)


def envelope(t, rise, fall):
    """Swell in over `rise` seconds and fade over the last `fall`, as a breath does, with no click at either end."""
    return np.clip(t / rise, 0, 1) ** 1.6 * np.clip((t[-1] - t) / fall, 0, 1) ** 1.2


def breath_in():
    # Through the nose: air hiss with a narrow, slowly rising resonance; a small catch at the top before it turns.
    n = seconds(3.9)
    t = times(n)
    air = rng.standard_normal(n)
    hiss = band(air, 900, 5200) * .5
    rise = 600 + 500 * (t / t[-1])
    nose = np.zeros(n)
    step = seconds(.05)
    for k in range(0, n, step):
        seg = air[k:k + step]
        f = float(rise[min(k, n - 1)])
        nose[k:k + step] = band(seg, f * .85, f * 1.15)[:len(seg)]
    out = (hiss + nose * 1.4) * envelope(t, 1.6, .5)
    catch = np.exp(-((t - 3.45) / .05) ** 2) * band(rng.standard_normal(n), 300, 1400) * .6
    return out + catch


def breath_out():
    # Through the mouth: lower, softer, longer at the top and dying away; under it, every so often, the rattle of a sleeper.
    n = seconds(3.9)
    t = times(n)
    air = band(rng.standard_normal(n), 180, 2400)
    out = air * envelope(t, .35, 2.4) * .9
    rattle = np.sign(np.sin(2 * np.pi * 34 * t)) * band(rng.standard_normal(n), 90, 500)
    out += rattle * envelope(t, .3, 3.0) * .18
    return out


def soft_step():
    # A foot set down on turf by somebody hardly there: a short muffled pad, the grass barely moving.
    n = seconds(.18)
    t = times(n)
    pad = band(rng.standard_normal(n), 120, 700) * np.exp(-t / .03)
    grass = band(rng.standard_normal(n), 2500, 6000) * np.exp(-t / .05) * .15
    return (pad + grass) * np.minimum(1, t / .004)


def stir():
    # A sleeper disturbed: a snort caught in the nose, then a low grumble that is almost a word, and the breath let go.
    n = seconds(2.2)
    t = times(n)
    snort = band(rng.standard_normal(n), 150, 900) * np.exp(-((t - .12) / .07) ** 2) * 1.4
    f = 95 + 25 * np.sin(2 * np.pi * 1.7 * t)
    voice = np.sin(2 * np.pi * np.cumsum(f) / RATE) + .4 * np.sin(4 * np.pi * np.cumsum(f) / RATE)
    voice *= band(rng.standard_normal(n), 60, 400) * .6 + .5
    grumble = voice * np.clip((t - .35) / .2, 0, 1) * np.clip((1.5 - t) / .5, 0, 1) * .45
    sigh = band(rng.standard_normal(n), 200, 1800) * np.clip((t - 1.3) / .2, 0, 1) * np.clip((2.2 - t) / .7, 0, 1) * .3
    return snort + grumble + sigh


def crow():
    # Wings far too big for any bird beating low overhead, a dozen strokes passing from one side to the other, and one harsh cry.
    n = seconds(4.0)
    t = times(n)
    air = band(rng.standard_normal(n), 40, 420)
    beat = np.zeros(n)
    for k in range(12):
        at = .25 + k * .3
        beat += np.exp(-((t - at) / .06) ** 2)
    wings = air * beat * np.sin(np.pi * np.clip(t / 3.9, 0, 1)) * 1.3
    cry_f = 520 - 140 * np.clip((t - 1.6) / .5, 0, 1)
    cry = np.sign(np.sin(2 * np.pi * np.cumsum(cry_f) / RATE)) * band(rng.standard_normal(n), 600, 2600)
    cry *= np.exp(-((t - 1.85) / .22) ** 2) * .5
    return wings + cry


def train():
    # 0.4.644: a small engine getting up steam and pulling away: chuffs coming faster, the carriage's wheels on the joints under them.
    n = seconds(6.0)
    t = times(n)
    hiss = band(rng.standard_normal(n), 300, 3200)
    gaps = .7 - .45 * np.clip(t / 5.5, 0, 1)
    phase = np.cumsum(1 / gaps) / RATE
    chuff = np.clip(np.sin(2 * np.pi * phase), 0, 1) ** 6
    steam = hiss * chuff * np.clip(t / .4, 0, 1)
    clack = np.zeros(n)
    for k in range(int(5.5 / .45)):
        at = .6 + k * .45 * (1 - k / 30)
        clack += np.exp(-((t - at) / .012) ** 2) * band(rng.standard_normal(n), 80, 900) * .6
    rumble = band(rng.standard_normal(n), 30, 120) * .35 * np.clip(t / 2, 0, 1)
    return steam + clack + rumble


def whistle():
    # Its whistle, two notes a little apart, held, then the fall at the end as the steam is shut off.
    n = seconds(2.4)
    t = times(n)
    f = 740 - 60 * np.clip((t - 1.9) / .4, 0, 1)
    tone = .55 * np.sin(2 * np.pi * np.cumsum(f) / RATE) + .45 * np.sin(2 * np.pi * np.cumsum(f * 1.19) / RATE)
    breath = band(rng.standard_normal(n), 900, 4000) * .18
    return (tone + breath) * envelope(t, .08, .35)


def cards():
    # 0.4.646: a whole pack thrown up at once, the riffle of fifty-odd edges in under a second, rising; then the cards coming down
    # over two seconds, each a soft flutter and a slap on grass, thinning out to the last few.
    n = seconds(3.6)
    t = times(n)
    out = np.zeros(n)
    edges = band(rng.standard_normal(n), 1800, 7000)
    for k in range(54):
        at = .05 + .8 * (k / 54) ** 1.4
        out += edges * np.exp(-((t - at) / .004) ** 2) * (.5 + .5 * k / 54)
    rush = band(rng.standard_normal(n), 500, 3000) * np.exp(-((t - .5) / .3) ** 2) * .35
    flutter = band(rng.standard_normal(n), 300, 2400)
    slap = band(rng.standard_normal(n), 120, 900)
    for k in range(40):
        at = 1.1 + 2.2 * rng.random() ** 1.7
        out += flutter * np.exp(-((t - at + .12) / .06) ** 2) * .12
        out += slap * np.exp(-np.clip(t - at, 0, None) / .02) * (t >= at) * .5 * (1 - (at - 1.1) / 2.6)
    return out + rush


def knight_fall():
    # 0.4.647: plate coming down on turf: a heavy thump, a cluster of short metallic clanks at inharmonic ratios (a helmet, a
    # greave, a gauntlet), and a few small rattles settling after.
    n = seconds(1.8)
    t = times(n)
    out = np.zeros(n)
    thud = band(rng.standard_normal(n), 40, 260)
    out += thud * np.exp(-np.clip(t - .06, 0, None) / .09) * (t >= .06) * 1.4
    for at, f0, gain in ((.05, 820, .9), (.11, 1310, .6), (.19, 610, .7), (.33, 1720, .35), (.52, 990, .25), (.81, 1460, .15), (1.12, 1180, .08)):
        bell = np.zeros(n)
        for ratio, part in ((1, 1), (2.32, .5), (3.97, .3), (5.41, .18)):
            bell += part * np.sin(2 * np.pi * f0 * ratio * np.clip(t - at, 0, None))
        out += bell * np.exp(-np.clip(t - at, 0, None) / .07) * (t >= at) * gain
    grit = band(rng.standard_normal(n), 2000, 7000) * np.exp(-((t - .12) / .08) ** 2) * .3
    return out + grit


def introduced():
    # 0.4.648: a pencil shading something in, quick strokes over a second, and as the last one lifts a small clear note, like a glass
    # touched with a nail: the thing is there.
    n = seconds(1.6)
    t = times(n)
    out = np.zeros(n)
    lead = band(rng.standard_normal(n), 1500, 6000)
    for k in range(14):
        at = .04 + k * .065 + rng.random() * .02
        out += lead * np.exp(-((t - at) / .018) ** 2) * (.25 + .3 * np.sin(np.pi * k / 14))
    ring = np.zeros(n)
    for ratio, part in ((1, 1), (2.76, .3), (5.4, .12)):
        ring += part * np.sin(2 * np.pi * 1568 * ratio * np.clip(t - .98, 0, None))
    out += ring * np.exp(-np.clip(t - .98, 0, None) / .35) * (t >= .98) * .45
    return out


def crowned():
    # 0.4.648: a crown coming down out of the air: a short rush falling in pitch, then a band of gold settling, one low ring with two
    # small clinks of its points after, and a soft weight.
    n = seconds(2.0)
    t = times(n)
    rush = band(rng.standard_normal(n), 400, 3200) * np.exp(-((t - .28) / .16) ** 2) * .4
    out = rush
    land = .5
    for at, f0, gain, decay in ((land, 660, .9, .55), (land + .07, 1990, .3, .12), (land + .16, 2350, .22, .1)):
        bell = np.zeros(n)
        for ratio, part in ((1, 1), (2.0, .35), (2.92, .2), (4.16, .1)):
            bell += part * np.sin(2 * np.pi * f0 * ratio * np.clip(t - at, 0, None))
        out = out + bell * np.exp(-np.clip(t - at, 0, None) / decay) * (t >= at) * gain
    weight = band(rng.standard_normal(n), 60, 300) * np.exp(-np.clip(t - land, 0, None) / .05) * (t >= land) * .6
    return out + weight


# The Knight's song: eight bars in a lilting six-eight, one note to a syllable or held, a falling tune that lifts once and comes home.
# (MIDI note, eighths.) Original.
SONG = [
    (62, 2), (66, 1), (69, 2), (69, 1),
    (71, 2), (69, 1), (66, 3),
    (67, 2), (64, 1), (67, 2), (71, 1),
    (69, 6),
    (74, 2), (73, 1), (71, 2), (69, 1),
    (67, 2), (66, 1), (64, 3),
    (66, 2), (69, 1), (67, 2), (64, 1),
    (62, 6),
]
EIGHTH = .25


def knight_song():
    # An old man's voice, half sung and half hummed: a buzzing source (a few harmonics, falling off), shaped by two vowel resonances,
    # a slow, slightly uneven vibrato, each note leaning in and easing off, with a breath between the phrases.
    total = sum(e for _, e in SONG) * EIGHTH + .6
    n = seconds(total)
    t = times(n)
    out = np.zeros(n)
    at = .2
    for k, (note, eighths) in enumerate(SONG):
        length = eighths * EIGHTH
        f = 440 * 2 ** ((note - 69) / 12)
        local = t - at
        inside = (local >= 0) & (local < length + .08)
        lt = np.clip(local, 0, None)
        vib = 1 + .006 * np.sin(2 * np.pi * (5.1 + .3 * np.sin(k)) * lt) * np.clip(lt / .25, 0, 1)
        phase = 2 * np.pi * np.cumsum(np.where(inside, f * vib, 0)) / RATE
        voice = np.zeros(n)
        for h in range(1, 9):
            # Vowel resonances near 600 and 1100 Hz lift the harmonics that fall near them.
            fh = f * h
            weight = (1 / h) * (1 + 1.6 * np.exp(-((fh - 600) / 220) ** 2) + 1.1 * np.exp(-((fh - 1100) / 300) ** 2))
            voice += weight * np.sin(h * phase)
        shape = np.clip(lt / .05, 0, 1) * np.clip((length + .08 - lt) / .12, 0, 1) * (1 - .25 * np.clip(lt / max(length, .1), 0, 1))
        out += voice * shape * inside
        at += length
    breath = band(rng.standard_normal(n), 300, 3000) * .015
    return out + breath


MAKERS = {'breath_in': breath_in, 'breath_out': breath_out, 'soft_step': soft_step, 'stir': stir, 'crow': crow, 'train': train, 'whistle': whistle, 'cards': cards,
          'knight_fall': knight_fall, 'knight_song': knight_song, 'introduced': introduced, 'crowned': crowned}


def main():
    folder = ASSETS / 'sounds/king'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'king.{name}',
               {'sounds': [{'name': f'the_oldest_house:king/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.king.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.king.{name}', subtitle)


if __name__ == '__main__':
    main()
