"""Original sounds for the Pell house (docs/PELL_HOUSE_PROPOSAL.md, "Sound"). So far: Gail's car leaving the curb once she has
handed the siblings over (docs/PELL_HOUSE_VISIT_0_4_525.md), heard from inside the house; and (docs/PELL_HOUSE_BEDTIME_0_4_527.md)
the home tape June plays in the evening, a birthday sung in her kitchen, through the television; the hairbrush at bedtime; and the
lock turning on each door when she locks up. From docs/PELL_HOUSE_NIGHT_0_4_530.md: her brush on the line at night, and the tin's
lid. From docs/PELL_HOUSE_HAND_0_4_535.md: a door slammed in the laundry, and June's cry. From docs/PELL_HOUSE_TAPES_0_4_536.md: the
shed's video deck taking a tape, and the woman on the tapes talking, through the television's small speaker (three takes, so her
lines are not one clip over and over). From docs/PELL_HOUSE_LAST_NIGHT_0_4_538.md: the scrubbing brush on the line, one stroke out
and back at a time, played as it is worked. From docs/PELL_HOUSE_DAWN_0_4_541.md: Gail's car coming up the street at first light and
pulling in at the curb. From docs/PELL_HOUSE_FAILURE_0_4_543.md: a cane tapping behind a door that will not open (three takes). From
docs/PELL_HOUSE_SENSES_0_4_546.md, what only a reader playing Tilly hears: a woman singing, wordless and low, through the wall at the head
of Nessa's bed; the chest freezer's hum in the laundry; and tape hiss from the shed. The hum and the hiss are four seconds each, sent
again as each ends, so they fade in and out at their ends. From docs/PELL_HOUSE_NOTES_0_4_570.md, the people's own small things while
the house waits on its hour: a wooden spoon round a saucepan, the kitchen tap run, a cloth over the table, the kettle coming to the boil,
and water dripping off Callum onto the floor.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first. --check
verifies the pack against the list here (CI runs that).
"""
import json
import sys
from pathlib import Path

ROWS = {
    'car_away': ('A car door shuts and a car pulls away', 48),
    'tape': ('A home tape plays: a birthday', 16),
    'brush': ('A hairbrush, slowly', 8),
    'lock': ('A lock turns', 12),
    'paint': ('A paintbrush, in strokes', 14),
    'tin': ("A paint tin's lid", 10),
    'hand': ('A door slams, and a cry', 24),
    'tape_play': ('A video deck takes a tape', 12),
    'tape_voice': ("A woman's voice, on a tape", 12),
    'scrub': ('A scrubbing brush on paint', 10),
    'car_come': ('A car pulls in at the curb', 48),
    'cane': ('A cane, tapping', 16),
    'singing': ('Singing, through the wall', 12),
    'hum': ('A low hum', 16),
    'hiss': ('Tape hiss', 18),
    'stir': ('A spoon round a saucepan', 10),
    'tap': ('A tap runs', 12),
    'wipe': ('A cloth over a table', 8),
    'kettle': ('A kettle comes to the boil', 12),
    'drip': ('Water dripping', 8),
}
# 0.4.536: sounds with more than one take; the game picks one each time.
TAKES = {'tape_voice': 3, 'cane': 3}


def files(name):
    n = TAKES.get(name, 1)
    return [name] if n == 1 else [f'{name}_{i}' for i in range(1, n + 1)]


def entry(name, reach):
    return {'sounds': [{'name': f'the_oldest_house:pell/{f}', 'attenuation_distance': reach} for f in files(name)], 'subtitle': f'subtitles.the_oldest_house.pell.{name}'}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        for f in files(name):
            path = ASSET_ROOT / f'sounds/pell/{f}.ogg'
            assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'pell.{name}'] == entry(name, reach), name
        assert lang[f'subtitles.the_oldest_house.pell.{name}'] == subtitle, name
    print(f'Verified the Pell house\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9525)


def car_away():
    # A car door shut from outside, heavy and padded with a latch's click; a beat; the starter's whine and the engine catching into a
    # low idle; then it pulls away, the note rising through one gear and its tyres hissing on the road, and is gone up the street.
    n = seconds(6.4)
    out = np.zeros(n)
    k = seconds(.45)
    tk = times(k)
    thud = np.sin(2 * np.pi * 62 * tk) * np.exp(-tk / .09) + band(rng.standard_normal(k), 80, 700) * np.exp(-tk / .05) * .8
    place(out, thud * .9, .1)
    k = seconds(.04)
    tk = times(k)
    place(out, band(rng.standard_normal(k), 1800, 7000) * np.exp(-tk / .006) * .5, .13)
    # The starter: a fast wavering whine for under a second.
    k = seconds(.75)
    tk = times(k)
    whine = np.sin(2 * np.pi * np.cumsum(180 + 40 * np.sin(2 * np.pi * 11 * tk)) / RATE)
    place(out, band(whine, 120, 1200) * envelope(k, .02, .5) * .35, 1.0)
    # The engine, from catching to gone: firing pulses at a rate that idles, then climbs as it pulls away, and fades with distance.
    start = 1.65
    k = seconds(6.4 - start)
    tk = times(k)
    rate = np.where(tk < 1.1, 26 + 4 * np.minimum(1, tk / .2), 30 + 34 * np.minimum(1, (tk - 1.1) / 2.4))
    phase = np.cumsum(rate) / RATE
    firing = (np.sin(2 * np.pi * phase) + .5 * np.sin(4 * np.pi * phase) + .3 * np.sin(6 * np.pi * phase))
    rumble = band(firing + band(rng.standard_normal(k), 40, 300) * .4, 30, 900)
    fade = np.where(tk < 1.4, np.minimum(1, tk / .08), np.exp(-(tk - 1.4) / 1.3))
    place(out, rumble * fade * .55, start)
    # Tyres on the road as it goes: a hiss that swells and falls away with the engine.
    tyres = band(rng.standard_normal(k), 400, 3500) * np.where(tk < 1.2, 0, np.sin(np.pi * np.clip((tk - 1.2) / 3.4, 0, 1)) ** 1.5) * .18
    place(out, tyres, start)
    return out


def car_come():
    # 0.4.541: Gail's car at first light, from up the street: the engine low and steady, louder as it comes, its note falling as it
    # slows; the tyres on the road, and a soft squeal of brakes as it stops; a last moment of idle and the engine off; then a car door
    # opened, and shut.
    n = seconds(6.6)
    out = np.zeros(n)
    k = seconds(4.4)
    tk = times(k)
    rate = np.where(tk < 2.8, 46 - 6 * np.minimum(1, tk / 2.8), 40 - 14 * np.minimum(1, (tk - 2.8) / 1.2))
    phase = np.cumsum(rate) / RATE
    firing = (np.sin(2 * np.pi * phase) + .5 * np.sin(4 * np.pi * phase) + .3 * np.sin(6 * np.pi * phase))
    rumble = band(firing + band(rng.standard_normal(k), 40, 300) * .4, 30, 900)
    near = np.minimum(1, (tk / 2.8) ** 1.6) * np.where(tk < 4.0, 1, np.exp(-(tk - 4.0) / .12))
    place(out, rumble * near * .55, 0)
    tyres = band(rng.standard_normal(k), 400, 3500) * np.minimum(1, (tk / 2.8) ** 2) * np.where(tk < 3.5, 1, np.exp(-(tk - 3.5) / .2)) * .2
    place(out, tyres, 0)
    k = seconds(.5)
    tk = times(k)
    place(out, np.sin(2 * np.pi * np.cumsum(2300 - 300 * tk) / RATE) * envelope(k, .05, .35) * .05, 3.3)
    # The door: its latch, and a beat later the door shut, heavy and padded.
    k = seconds(.04)
    tk = times(k)
    place(out, band(rng.standard_normal(k), 1800, 7000) * np.exp(-tk / .006) * .45, 5.0)
    k = seconds(.45)
    tk = times(k)
    thud = np.sin(2 * np.pi * 62 * tk) * np.exp(-tk / .09) + band(rng.standard_normal(k), 80, 700) * np.exp(-tk / .05) * .8
    place(out, thud * .9, 5.9)
    return out


# The birthday song's last two lines (the tune is a traditional one), as sung in a kitchen and heard again through a television's
# speaker: each syllable its note (in semitones from the tune's home note), its length in beats and its vowel's two formants.
SONG = [(7, .75, (750, 1300)), (7, .25, (300, 2200)), (19, 1, (500, 1400)), (16, 1, (450, 1900)), (12, 1, (300, 2200)),
        (11, 1, (550, 1800)), (9, 2, (750, 1250)),
        (17, .75, (750, 1300)), (17, .25, (300, 2200)), (16, 1, (500, 1400)), (12, 1, (450, 1900)), (14, 1, (350, 900)),
        (12, 2.6, (320, 850))]
BEAT = .43


def sung(note_hz, n, formants, breath, seed):
    """One sung syllable: a voice's harmonics with a slow vibrato, shaped by the vowel's two formants, a breath of noise under it."""
    r = np.random.default_rng(seed)
    t = times(n)
    f = note_hz * (1 + .006 * np.sin(2 * np.pi * (5.2 + r.random()) * t))
    phase = np.cumsum(f) / RATE
    src = sum(np.sin(2 * np.pi * k * phase + r.random() * 6) / k for k in range(1, 14))
    voiced = sum(band(src, lo * .8, lo * 1.25) * g for lo, g in zip(formants, (1, .6)))
    voiced += band(r.standard_normal(n), formants[0], formants[1] * 1.5) * breath
    shape = np.minimum(1, t / .04) * np.minimum(1, (t[-1] - t) / .07 + .02)
    return voiced * shape


def tape():
    # The birthday on the tape: a kitchen of grown-ups and one child singing the song's last two lines a little ragged, a cheer, the
    # clapping, the child's laugh; through a television's small speaker, the tape wavering and hissing under it.
    n = seconds(8.4)
    out = np.zeros(n)
    home = 196.0
    at = .15
    for i, (step, beats, formants) in enumerate(SONG):
        length = beats * BEAT
        for v, (octave, gain, late) in enumerate(((1, .5, 0), (1, .35, .03), (1, .3, -.02), (2, .45, .05))):
            k = seconds(length * .96)
            note = home * octave * 2 ** (step / 12) * (1 + .004 * (v - 1.5))
            place(out, sung(note, k, formants, .15 if octave == 2 else .08, 9500 + i * 7 + v) * gain, max(0, at + late))
        # The consonant at each syllable's start: a breath of noise.
        k = seconds(.03)
        place(out, band(rng.standard_normal(k), 2000, 6000) * np.exp(-times(k) / .01) * .18, at)
        at += length
    # A cheer from the child, then clapping from four pairs of hands, and her laugh.
    k = seconds(.35)
    tk = times(k)
    cheer = sum(np.sin(2 * np.pi * h * np.cumsum(520 + 380 * tk / .35) / RATE) / h for h in range(1, 6))
    place(out, band(cheer, 400, 3000) * np.sin(np.pi * tk / .35) * .5, at + .05)
    for c in range(26):
        k = seconds(.05)
        place(out, band(rng.standard_normal(k), 900, 4500) * np.exp(-times(k) / .012) * (.3 + .2 * rng.random()), at + .2 + rng.random() * 1.6)
    for s in range(5):
        k = seconds(.11)
        tk = times(k)
        place(out, sung(470 - s * 14, k, (800, 1400), .3, 9700 + s) * np.sin(np.pi * tk / .11) * .55, at + 1.0 + s * .14)
    # The television: its small speaker, the tape's wow, its hiss and a low hum.
    out = band(out, 260, 3400)
    t = times(n)
    warped = np.clip(t + .004 * np.sin(2 * np.pi * .6 * t) / (2 * np.pi * .6), 0, t[-1])
    out = np.interp(warped, t, out)
    out += band(rng.standard_normal(n), 3000, 9000) * .025 + np.sin(2 * np.pi * 60 * t) * .01
    return out


def brush():
    # A hairbrush drawn slowly through hair, three times: each stroke a soft rising and falling hiss with the bristles' small catches.
    n = seconds(1.9)
    out = np.zeros(n)
    for i in range(3):
        k = seconds(.48)
        tk = times(k)
        hiss = band(rng.standard_normal(k), 2400, 9000) * np.sin(np.pi * tk / .48) ** 2
        catches = band((rng.random(k) > .997).astype(float), 1500, 7000) * 2.5 * np.sin(np.pi * tk / .48)
        place(out, (hiss * .5 + catches) * (1 - .15 * i), .05 + i * .6)
    return out


def lock():
    # A key into a lock and turned: the key's scrape going in, the two clicks of the turn, and the bolt going home.
    n = seconds(.8)
    out = np.zeros(n)
    k = seconds(.13)
    place(out, band(rng.standard_normal(k), 1500, 6000) * np.sin(np.pi * times(k) / .13) * .18, .02)
    for at in (.27, .35):
        k = seconds(.08)
        tk = times(k)
        click = sum(np.sin(2 * np.pi * f * tk) * np.exp(-tk / .012) for f in (2300, 3700, 5200)) * .3
        place(out, click + band(rng.standard_normal(k), 2000, 8000) * np.exp(-tk / .004) * .5, at)
    k = seconds(.12)
    tk = times(k)
    place(out, np.sin(2 * np.pi * 180 * tk) * np.exp(-tk / .03) * .7 + band(rng.standard_normal(k), 300, 1500) * np.exp(-tk / .02) * .4, .44)
    return out


def paint():
    # A wide brush loaded with paint, drawn along the ground and back: each stroke starts with the soft wet slap of the bristles going
    # down, then drags, a coarse hiss with the paint's sticky catches in it, and lifts off thinner than it went down.
    n = seconds(1.5)
    out = np.zeros(n)
    for i, (at, length) in enumerate(((.03, .55), (.72, .5))):
        k = seconds(length)
        tk = times(k)
        drag = np.sin(np.pi * np.minimum(tk / length, 1)) ** 1.4 * np.exp(-tk / (length * 1.6))
        hiss = band(rng.standard_normal(k), 900, 5200) * drag * .55
        wet = band(rng.standard_normal(k), 180, 900) * drag * .5
        catches = band((rng.random(k) > .996).astype(float), 600, 3500) * 2.2 * drag
        slap = np.zeros(k)
        m = seconds(.06)
        tm = times(m)
        slap[:m] = band(rng.standard_normal(m), 150, 1400) * np.exp(-tm / .015) * .9
        place(out, (hiss + wet + catches + slap) * (1 - .18 * i), at)
    return out


def tin():
    # A paint tin's lid: the scrape of something levered under its rim, then the lid lifting with a dull tinny ring; or pressed back
    # down, the same ring shorter. One sound for both, pitched by the game.
    n = seconds(.7)
    out = np.zeros(n)
    k = seconds(.16)
    tk = times(k)
    place(out, band(rng.standard_normal(k), 1200, 6000) * np.sin(np.pi * tk / .16) * .25, .02)
    k = seconds(.45)
    tk = times(k)
    ring = sum(a * np.sin(2 * np.pi * f * tk) * np.exp(-tk / d) for f, a, d in ((1180, .5, .09), (2730, .35, .06), (4410, .2, .035), (690, .4, .05)))
    place(out, ring + band(rng.standard_normal(k), 300, 2500) * np.exp(-tk / .01) * .6, .17)
    return out


def cried(n, seed):
    """A short cry: a grown voice's harmonics jumping up and falling away through an open vowel, rough at its height, breath under it."""
    r = np.random.default_rng(seed)
    t = times(n)
    f = (290 + 250 * np.minimum(1, t / .05) * np.exp(-np.maximum(0, t - .05) / .2)) * (1 + .018 * np.sin(2 * np.pi * 27 * t))
    phase = np.cumsum(f) / RATE
    src = sum(np.sin(2 * np.pi * k * phase + r.random() * 6) / k for k in range(1, 16))
    voiced = band(src, 650, 1050) + band(src, 1100, 1550) * .6 + band(src, 2400, 3200) * .22
    voiced += band(r.standard_normal(n), 800, 4000) * .16
    shape = np.minimum(1, t / .012) * np.exp(-np.maximum(0, t - .14) / .09)
    return voiced * shape


def hand():
    # A door in a small room slammed hard on something in the way: a dull, short thump where it should have banged shut, the frame's
    # crack and the leaf's rattle against it; under a breath later a woman's cry, sharp and cut off; then breath sucked in through the
    # teeth, and let out.
    n = seconds(1.9)
    out = np.zeros(n)
    k = seconds(.32)
    tk = times(k)
    thump = np.sin(2 * np.pi * 68 * tk) * np.exp(-tk / .05) + band(rng.standard_normal(k), 90, 900) * np.exp(-tk / .03) * .9
    place(out, thump, .03)
    k = seconds(.025)
    place(out, band(rng.standard_normal(k), 1500, 6000) * np.exp(-times(k) / .005) * .4, .035)
    for i in range(3):
        k = seconds(.06)
        tk = times(k)
        place(out, band(rng.standard_normal(k), 400, 2600) * np.exp(-tk / .015) * (.32 - .09 * i), .12 + i * .07)
    place(out, cried(seconds(.5), 9535) * .8, .31)
    for at, length, lo, gain in ((1.05, .26, 2600, .2), (1.45, .34, 700, .12)):
        k = seconds(length)
        tk = times(k)
        place(out, band(rng.standard_normal(k), lo, lo * 3) * np.sin(np.pi * tk / length) ** 2 * gain, at)
    return out


def tape_play():
    # The shed's deck taking a tape: the cassette pushed home, the carriage dropping with a clunk, the motor coming up, the heads
    # meeting the tape with a short scrape, and the hiss of blank tape before the picture comes.
    n = seconds(1.6)
    out = np.zeros(n)
    k = seconds(.08)
    place(out, band(rng.standard_normal(k), 500, 3000) * np.exp(-times(k) / .02) * .5, .02)
    k = seconds(.22)
    tk = times(k)
    place(out, (np.sin(2 * np.pi * 95 * tk) * np.exp(-tk / .04) + band(rng.standard_normal(k), 120, 1400) * np.exp(-tk / .03)) * .8, .2)
    k = seconds(1.1)
    tk = times(k)
    whir = np.sin(2 * np.pi * np.cumsum(70 + 90 * np.minimum(1, tk / .4)) / RATE) * .25 + band(rng.standard_normal(k), 200, 900) * .12
    place(out, whir * np.minimum(1, tk / .1) * np.exp(-np.maximum(0, tk - .7) / .25), .42)
    k = seconds(.12)
    place(out, band(rng.standard_normal(k), 1500, 5000) * np.sin(np.pi * times(k) / .12) * .3, .62)
    k = seconds(.9)
    tk = times(k)
    place(out, band(rng.standard_normal(k), 3000, 9000) * np.minimum(1, tk / .3) * .08, .68)
    return out


def tape_voice(take):
    # The woman on the tapes, talking: an older voice, unhurried, a phrase falling at its end, a breath, another phrase; words that
    # are not quite words through a small television speaker, with the tape's wow, its hiss and a low hum. Each take its own phrases.
    r = np.random.default_rng(9536 + take)
    n = seconds(4.6)
    out = np.zeros(n)
    vowels = [(750, 1250), (500, 1500), (350, 2100), (450, 900), (600, 1750), (300, 850)]
    at = .2
    for phrase in range(2 + take % 2):
        syllables = 5 + int(r.integers(0, 5))
        base = 205 - 8 * phrase + r.random() * 10
        for i in range(syllables):
            length = .11 + r.random() * .12
            fall = 1 - .18 * (i / max(1, syllables - 1)) ** 2
            k = seconds(length)
            t = times(k)
            pitch = base * fall * (1 + .04 * np.sin(2 * np.pi * (2 + r.random()) * t))
            phase = np.cumsum(pitch) / RATE
            src = sum(np.sin(2 * np.pi * h * phase + r.random() * 6) / h for h in range(1, 14))
            f1, f2 = vowels[int(r.integers(0, len(vowels)))]
            voiced = band(src, f1 * .8, f1 * 1.25) + band(src, f2 * .8, f2 * 1.2) * .5 + band(r.standard_normal(k), f1, f2 * 1.4) * .05
            shape = np.minimum(1, t / .02) * np.minimum(1, (t[-1] - t) / .04 + .02)
            place(out, voiced * shape * (.8 - .25 * (i == syllables - 1)), at)
            if r.random() < .55:
                c = seconds(.04)
                place(out, band(r.standard_normal(c), 2200, 6500) * np.exp(-times(c) / .012) * .2, at + length - .01)
            at += length * (.92 + r.random() * .1)
        at += .28 + r.random() * .2
        k = seconds(.25)
        place(out, band(r.standard_normal(k), 500, 2000) * np.sin(np.pi * times(k) / .25) ** 2 * .06, at - .26)
        if at > 4.0:
            break
    out = band(out, 280, 3300)
    t = times(n)
    warped = np.clip(t + .003 * np.sin(2 * np.pi * .7 * t) / (2 * np.pi * .7), 0, t[-1])
    out = np.interp(warped, t, out)
    out += band(rng.standard_normal(n), 3000, 9000) * .03 + np.sin(2 * np.pi * 60 * t) * .012
    return out


def scrub():
    # A stiff brush worked hard out and back over painted concrete, wet: each way a coarse rasp, harder in the middle of the push,
    # with the bristles' small catches on the grit, and under it the slosh of the water it was dipped in; the second way a little
    # shorter and softer than the first. Played once every stroke, so it is short.
    n = seconds(.4)
    out = np.zeros(n)
    for i, (at, length) in enumerate(((.0, .2), (.2, .17))):
        k = seconds(length)
        tk = times(k)
        push = np.sin(np.pi * np.minimum(tk / length, 1)) ** 1.2
        rasp = band(rng.standard_normal(k), 1800, 7500) * push * .7
        grit = band((rng.random(k) > .985).astype(float), 1200, 6000) * 1.8 * push
        water = band(rng.standard_normal(k), 120, 700) * push * .35
        place(out, (rasp + grit + water) * (1 - .25 * i), at)
    return out


def cane(take):
    # 0.4.543: a cane's rubber tip set down on a floor, behind a door: a short soft knock with the tip's small click in it, heard through
    # the wood, so the top of it is gone. Three takes (a little higher or lower, a little harder or softer), so a run of taps along a
    # wall is not one clip over and over. Each is its own seed, so adding them changed none of the sounds before them.
    r = np.random.default_rng(54300 + take)
    n = seconds(.3)
    out = np.zeros(n)
    k = seconds(.12)
    tk = times(k)
    knock = np.sin(2 * np.pi * (230 + 35 * take) * tk) * np.exp(-tk / .022) + band(r.standard_normal(k), 150, 900) * np.exp(-tk / .014) * .6
    place(out, knock * (.85 + .05 * take), .02)
    k = seconds(.02)
    tk = times(k)
    place(out, band(r.standard_normal(k), 900, 2500) * np.exp(-tk / .004) * .3, .02)
    return band(out, 90, 1800)


def singing():
    # 0.4.546: a woman's voice through a wall, singing with her mouth closed, low and slow: eight notes of a tune of its own that falls
    # and comes back up, held at the end, a breath, and four more that do not finish it. Each note glides into the next; the voice
    # wavers a little; the wall takes everything above the vowel's first ring, and the room on her side of it rings faintly after.
    r = np.random.default_rng(54600)
    tune = [(261.6, .9), (293.7, .6), (261.6, .6), (220.0, 1.2), (196.0, .8), (220.0, .6), (261.6, .9), (246.9, 1.7),
            (None, .55), (220.0, .7), (196.0, .7), (174.6, .8), (196.0, 1.6)]
    n = seconds(sum(length for _, length in tune) + .9)
    pitch = np.zeros(n)
    level = np.zeros(n)
    at = .25
    last = tune[0][0]
    for freq, length in tune:
        start, k = seconds(at), seconds(length)
        if freq is not None:
            t = times(k)
            glide = np.minimum(1, t / .07)
            pitch[start:start + k] = last + (freq - last) * glide
            level[start:start + k] = np.minimum(1, t / .09) * np.minimum(1, (length - t) / .16 + .15)
            last = freq
        else:
            pitch[start:start + k] = last
        at += length
    pitch[pitch == 0] = last
    t = times(n)
    pitch *= 1 + .012 * np.sin(2 * np.pi * 5.1 * t + .4 * np.sin(2 * np.pi * .3 * t))
    phase = np.cumsum(pitch) / RATE
    source = sum(np.sin(2 * np.pi * h * phase + r.random() * 6) / h ** 1.3 for h in range(1, 16))
    # The hum of a closed mouth: the first ring of an "oo", a little of the second, and breath.
    voiced = band(source, 230, 420) + band(source, 700, 950) * .25 + band(r.standard_normal(n), 300, 900) * .03
    voiced *= np.convolve(level, np.ones(seconds(.03)) / seconds(.03), mode='same')
    room = np.zeros(n)
    for delay, gain in ((.031, .35), (.047, .25), (.083, .18), (.121, .1)):
        d = seconds(delay)
        room[d:] += voiced[:-d] * gain
    through = band(voiced + room, 110, 620)
    return through + band(r.standard_normal(n), 60, 300) * .004


def hum():
    # 0.4.546: the chest freezer's compressor in the laundry, as a child hears it from the hall: mains hum at twice the supply, its
    # fundamental under it, a faint third, a slow beat between the motor and the wall, and the casing's rattle low in it. Four seconds,
    # whole cycles, faded at its ends so it can be played again as it ends.
    r = np.random.default_rng(54601)
    n = seconds(4.0)
    t = times(n)
    out = np.sin(2 * np.pi * 100 * t) * .55 + np.sin(2 * np.pi * 100.5 * t) * .35 + np.sin(2 * np.pi * 50 * t) * .3 + np.sin(2 * np.pi * 150 * t) * .08
    out += band(r.standard_normal(n), 180, 520) * .05 * (1 + .5 * np.sin(2 * np.pi * 1.5 * t))
    fade = np.minimum(1, np.minimum(t, t[-1] - t) / .05)
    return out * fade


def hiss():
    # 0.4.546: tape hiss through the shed's thin wall: the high wash of a tape running with nothing on it, its level wandering a little
    # with the tape, a dropout now and then, and under it the deck's motor. Four seconds, faded at its ends.
    r = np.random.default_rng(54602)
    n = seconds(4.0)
    t = times(n)
    wash = band(r.standard_normal(n), 2500, 7000) * (1 + .15 * np.sin(2 * np.pi * .7 * t) + .08 * np.sin(2 * np.pi * 3.1 * t))
    for at in (.9, 2.6):
        k = seconds(.08)
        start = seconds(at)
        wash[start:start + k] *= .35 + .65 * np.abs(np.linspace(-1, 1, k))
    motor = np.sin(2 * np.pi * 60 * t) * .04 + band(r.standard_normal(n), 40, 160) * .03
    fade = np.minimum(1, np.minimum(t, t[-1] - t) / .05)
    return (wash + motor) * fade


def stir():
    # 0.4.570: a wooden spoon drawn twice round the bottom of a saucepan of something thick: each round a dull scrape that swells and
    # fades as the spoon goes round, the pan's metal faintly in it, and the slow burp of the stew; then the spoon knocked twice on the rim.
    r = np.random.default_rng(57001)
    n = seconds(1.6)
    out = np.zeros(n)
    for i in range(2):
        k = seconds(.5)
        tk = times(k)
        swell = np.sin(np.pi * tk / .5) ** 1.5
        scrape = band(r.standard_normal(k), 500, 2600) * swell * .45
        pan = np.sin(2 * np.pi * (410 + 30 * i) * tk) * swell * .08
        place(out, (scrape + pan) * (1 - .12 * i), .02 + i * .52)
    k = seconds(.18)
    tk = times(k)
    place(out, band(r.standard_normal(k), 80, 400) * np.exp(-tk / .05) * .35, .55)
    for at in (1.18, 1.36):
        k = seconds(.2)
        tk = times(k)
        knock = sum(a * np.sin(2 * np.pi * f * tk) * np.exp(-tk / d) for f, a, d in ((620, .5, .05), (1490, .3, .03), (2770, .15, .02)))
        place(out, knock + band(r.standard_normal(k), 300, 1800) * np.exp(-tk / .006) * .4, at)
    return out


def tap():
    # 0.4.570: the kitchen tap turned on: the washer's short squeal, then water into a steel sink, hard at first and settling, with the
    # gurgle of the drain under it, then the squeal of it turned off and the last drips. About three seconds.
    r = np.random.default_rng(57002)
    n = seconds(3.1)
    out = np.zeros(n)
    t = times(seconds(.12))
    place(out, np.sin(2 * np.pi * (1650 + 900 * t / .12) * t) * np.sin(np.pi * t / .12) * .12, .02)
    k = seconds(2.6)
    tk = times(k)
    level = np.minimum(1, tk / .08) * np.minimum(1, (2.6 - tk) / .1) * (1 - .25 * np.minimum(1, tk / .8))
    stream = band(r.standard_normal(k), 350, 6000) * (.8 + .2 * np.sin(2 * np.pi * 6.3 * tk + r.random()))
    gurgle = band(r.standard_normal(k), 90, 400) * (.5 + .5 * np.sin(2 * np.pi * 2.1 * tk)) * .5
    place(out, (stream * .55 + gurgle) * level, .12)
    t = times(seconds(.1))
    place(out, np.sin(2 * np.pi * (2300 - 800 * t / .1) * t) * np.sin(np.pi * t / .1) * .1, 2.72)
    for at in (2.86, 3.0):
        k = seconds(.06)
        tk = times(k)
        place(out, np.sin(2 * np.pi * 1180 * tk) * np.exp(-tk / .012) * .25, at)
    return out


def wipe():
    # 0.4.570: a damp cloth wiped across a laminate tabletop and back: two soft swishes, the second shorter, with the cloth's faint drag.
    r = np.random.default_rng(57003)
    n = seconds(.85)
    out = np.zeros(n)
    for i, (at, length) in enumerate(((.02, .38), (.44, .32))):
        k = seconds(length)
        tk = times(k)
        push = np.sin(np.pi * tk / length) ** 2
        swish = band(r.standard_normal(k), 900, 6500) * push * .5
        drag = band(r.standard_normal(k), 200, 900) * push * .25
        place(out, (swish + drag) * (1 - .2 * i), at)
    return out


def kettle():
    # 0.4.570: an electric kettle coming to the boil, heard from across the room: the low roar of the element rising, the hiss over it
    # growing as it nears the boil, small ticks of the metal heating, the bubbling at the top, and the switch clicking off.
    r = np.random.default_rng(57004)
    n = seconds(4.2)
    out = np.zeros(n)
    k = seconds(3.9)
    tk = times(k)
    rise = np.minimum(1, tk / 3.4) ** 1.5
    roar = band(r.standard_normal(k), 60, 320) * (.25 + .6 * rise)
    hiss = band(r.standard_normal(k), 2200, 8000) * rise ** 2 * .45
    bubbles = band((r.random(k) > .992).astype(float), 300, 1600) * 1.6 * np.maximum(0, rise - .6) / .4
    ticks = band((r.random(k) > .9995).astype(float), 1500, 6000) * 2.0 * (1 - rise)
    fade = np.minimum(1, tk / .2) * np.minimum(1, (3.9 - tk) / .08)
    place(out, (roar + hiss + bubbles + ticks) * fade, .02)
    k = seconds(.08)
    tk = times(k)
    place(out, sum(np.sin(2 * np.pi * f * tk) * np.exp(-tk / .01) for f in (1900, 3300)) * .3 + band(r.standard_normal(k), 1500, 6000) * np.exp(-tk / .004) * .4, 3.95)
    return out


def drip():
    # 0.4.570: water dripping off wet clothes onto a hard floor: three drops a little apart, each a small bright plink with the tiny
    # splash of it, the third the faintest.
    r = np.random.default_rng(57005)
    n = seconds(1.1)
    out = np.zeros(n)
    for i, (at, f) in enumerate(((.03, 1320), (.41, 1170), (.86, 1250))):
        k = seconds(.12)
        tk = times(k)
        plink = np.sin(2 * np.pi * f * tk * (1 + .25 * np.exp(-tk / .01))) * np.exp(-tk / .025)
        splash = band(r.standard_normal(k), 2000, 7000) * np.exp(-tk / .008) * .3
        place(out, (plink * .6 + splash) * (1 - .25 * i), at)
    return out


MAKERS = {'car_away': car_away, 'tape': tape, 'brush': brush, 'lock': lock, 'paint': paint, 'tin': tin, 'hand': hand, 'tape_play': tape_play,
          'tape_voice_1': lambda: tape_voice(1), 'tape_voice_2': lambda: tape_voice(2), 'tape_voice_3': lambda: tape_voice(3), 'scrub': scrub,
          'car_come': car_come, 'cane_1': lambda: cane(1), 'cane_2': lambda: cane(2), 'cane_3': lambda: cane(3), 'singing': singing, 'hum': hum,
          'hiss': hiss, 'stir': stir, 'tap': tap, 'wipe': wipe, 'kettle': kettle, 'drip': drip}


def main():
    folder = ASSETS / 'sounds/pell'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        for f in files(name):
            target = folder / f'{f}.ogg'
            if not target.exists():
                soundfile.write(str(target), normalized(MAKERS[f]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'pell.{name}', entry(name, reach))
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.pell.{name}', subtitle)


if __name__ == '__main__':
    main()
