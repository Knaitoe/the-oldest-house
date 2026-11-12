"""Original sounds for the Pell house (docs/PELL_HOUSE_PROPOSAL.md, "Sound"). So far: Gail's car leaving the curb once she has
handed the siblings over (docs/PELL_HOUSE_VISIT_0_4_525.md), heard from inside the house; and (docs/PELL_HOUSE_BEDTIME_0_4_527.md)
the home tape June plays in the evening, a birthday sung in her kitchen, through the television; the hairbrush at bedtime; and the
lock turning on each door when she locks up.

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
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/pell/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'pell.{name}'] == {'sounds': [{'name': f'the_oldest_house:pell/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.pell.{name}'}, name
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


MAKERS = {'car_away': car_away, 'tape': tape, 'brush': brush, 'lock': lock}


def main():
    folder = ASSETS / 'sounds/pell'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'pell.{name}',
               {'sounds': [{'name': f'the_oldest_house:pell/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.pell.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.pell.{name}', subtitle)


if __name__ == '__main__':
    main()
