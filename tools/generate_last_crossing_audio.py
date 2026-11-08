"""Original sounds for The Last Crossing (0.4.351): a knock carried down a speaking pipe, the station pump taking up its load, something
tapping back under the service grate, and the intake's bleed screw letting go its air and then water. From 0.4.352, Night One: the surge
coming through the intake, the second breath an infected reader hears under their own, the things that come up out of the water, the
ferry straining on its line at the first rise, and the line snapping. From 0.4.353, Day Two: the winch drum ratcheting a notch,
something answering through the pipes, and water running behind a grate in a flooded branch.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'knock': ('A knock in the speaking pipe', 24),
    'pump': ('The station pump takes the load', 32),
    'tap': ('Something taps back under the grate', 12),
    'bleed': ('Air hisses from the bleed screw', 12),
    'surge': ('The surge hits the intake', 40),
    'breath': ('A second breath, under yours', 4),
    'undertow': ('Something wet groans', 16),
    'rise': ('The ferry strains at its line', 32),
    'snap': ('The mooring line snaps', 32),
    'ratchet': ('The winch drum ratchets a notch', 16),
    'scrape': ('Something scrapes in the pipes', 24),
    'branch': ('Water runs in a branch', 10),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/crossing/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'crossing.{name}'] == {'sounds': [{'name': f'the_oldest_house:crossing/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.crossing.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.crossing.{name}'] == subtitle, name
    print(f'Verified the station\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9351)


def knock():
    # A knuckle on iron, heard down a length of pipe: a dull thud, then the pipe ringing hollow and dying away.
    n = seconds(1.2)
    out = np.zeros(n)
    k = seconds(.05)
    place(out, band(rng.standard_normal(k), 120, 1400) * envelope(k, .001, .03) * 1.0, 0)
    tk = times(seconds(1.1))
    ring = sum(np.sin(2 * np.pi * f * tk) * np.exp(-tk * d) * a for f, d, a in ((310, 5, .5), (620, 7, .25), (935, 9, .12), (1460, 12, .06)))
    place(out, ring * .6, .004)
    return out


def pump():
    # An electric pump taking up its load: the motor winding up from a growl, a knock or two of air, then a steady churning note.
    n = seconds(3.4)
    out = np.zeros(n)
    tk = times(n)
    f = 38 + 22 * np.minimum(1, tk / 1.6)
    phase = 2 * np.pi * np.cumsum(f) / RATE
    motor = np.sin(phase) * .5 + np.sin(phase * 2) * .25 + np.sign(np.sin(phase * 3)) * .08
    churn = band(rng.standard_normal(n), 200, 900) * (.3 + .2 * np.sin(2 * np.pi * 7 * tk))
    env = np.minimum(1, tk / .3) * np.minimum(1, (3.4 - tk) / .4)
    out += (motor + churn * np.minimum(1, tk / 1.6)) * env * .8
    for at in (.7, 1.1):
        k = seconds(.12)
        place(out, band(rng.standard_normal(k), 80, 600) * envelope(k, .003, .08) * .7, at)
    return out


def tap():
    # Three soft taps from under the grate, wet and unhurried, as if a fingertip were answering on the iron from below.
    n = seconds(1.6)
    out = np.zeros(n)
    for i, at in enumerate((0, .42, .9)):
        k = seconds(.08)
        hit = band(rng.standard_normal(k), 300, 2600) * envelope(k, .0008, .025)
        tk = times(seconds(.25))
        ring = np.sin(2 * np.pi * (700 - i * 30) * tk) * np.exp(-tk * 22) * .3
        place(out, hit * .7, at)
        place(out, ring, at + .002)
        k = seconds(.2)
        place(out, band(rng.standard_normal(k), 150, 700) * envelope(k, .02, .12) * .15, at + .03)
    return out


def bleed():
    # The bleed screw backed off: trapped air hissing out, sputtering, then water spitting and running.
    n = seconds(2.4)
    out = np.zeros(n)
    tk = times(n)
    hiss = band(rng.standard_normal(n), 2500, 9000) * np.exp(-tk * 1.4) * .7
    sputter = band(rng.standard_normal(n), 400, 3000) * (rng.random(n) > .985) * np.clip((tk - .8) * 2, 0, 1) * 1.4
    run = band(rng.standard_normal(n), 300, 1800) * np.clip((tk - 1.3) * 1.5, 0, 1) * .35
    env = np.minimum(1, tk / .02) * np.minimum(1, (2.4 - tk) / .3)
    out += (hiss + sputter + run) * env
    return out


def surge():
    # The surge through the intake: a deep slam on the pipe and the grate, then water gushing and roaring off into the pipes.
    n = seconds(3.6)
    out = np.zeros(n)
    k = seconds(.4)
    tk = times(k)
    slam = (band(rng.standard_normal(k), 40, 900) * envelope(k, .002, .25) * 1.2 + np.sin(2 * np.pi * 46 * tk) * np.exp(-tk * 7) * .9)
    place(out, slam, 0)
    tk = times(n)
    gush = band(rng.standard_normal(n), 150, 2400) * np.clip(tk / .25, 0, 1) * np.exp(-np.maximum(0, tk - .6) * .9) * .8
    rumble = band(rng.standard_normal(n), 30, 160) * np.exp(-tk * .7) * .7
    clank = np.zeros(n)
    for at in (.3, .9, 1.7):
        kk = seconds(.15)
        place(clank, band(rng.standard_normal(kk), 600, 3000) * envelope(kk, .001, .06) * .35, at)
    out += (gush + rumble + clank) * np.minimum(1, (3.6 - tk) / .5)
    return out


def breath():
    # A breath drawn in and let out, low and wet, a beat behind where one's own would be: close, quiet, private.
    n = seconds(2.6)
    out = np.zeros(n)
    tk = times(n)
    inhale = band(rng.standard_normal(n), 300, 2200) * np.clip(np.sin(np.pi * np.clip(tk / 1.0, 0, 1)), 0, 1) * .5
    exhale = band(rng.standard_normal(n), 120, 900) * np.clip(np.sin(np.pi * np.clip((tk - 1.25) / 1.2, 0, 1)), 0, 1) * .6
    rattle = band(rng.standard_normal(n), 80, 400) * (np.sin(2 * np.pi * 23 * tk) > .6) * np.clip(np.sin(np.pi * np.clip((tk - 1.25) / 1.2, 0, 1)), 0, 1) * .5
    out += inhale + exhale + rattle
    return out


def undertow():
    # A groan through water in the throat: a low voiced moan that bubbles and gargles and gives out.
    n = seconds(2.0)
    out = np.zeros(n)
    tk = times(n)
    f = 92 - 18 * tk + 6 * np.sin(2 * np.pi * 3.1 * tk)
    phase = 2 * np.pi * np.cumsum(f) / RATE
    voice = (np.sin(phase) * .5 + np.sin(phase * 2) * .3 + np.sin(phase * 3) * .15) * (.6 + .4 * np.sin(2 * np.pi * 11 * tk))
    gargle = band(rng.standard_normal(n), 200, 1200) * (rng.random(n) > .992) * 2.5
    env = np.minimum(1, tk / .15) * np.minimum(1, (2.0 - tk) / .6)
    out += (voice * .8 + band(gargle, 150, 1500)) * env
    return out


def rise():
    # The first rise: water slapping the ferry's side, and its timbers and line groaning as it strains against the cut.
    n = seconds(3.2)
    out = np.zeros(n)
    tk = times(n)
    for at in (0, 1.1, 2.0):
        k = seconds(.5)
        place(out, band(rng.standard_normal(k), 200, 1600) * envelope(k, .01, .3) * .6, at)
    f = 140 + 60 * np.sin(2 * np.pi * .4 * tk)
    phase = 2 * np.pi * np.cumsum(f) / RATE
    creak = np.sign(np.sin(phase)) * band(rng.standard_normal(n), 300, 2000) * (np.sin(2 * np.pi * .7 * tk) > .2) * .25
    out += creak * np.minimum(1, (3.2 - tk) / .4)
    return out


def snap():
    # The line parting: a sharp crack, the end whipping through the air, and a slap on the water.
    n = seconds(1.6)
    out = np.zeros(n)
    k = seconds(.03)
    place(out, band(rng.standard_normal(k), 1500, 9000) * envelope(k, .0003, .015) * 1.4, 0)
    k = seconds(.35)
    tk = times(k)
    whip = band(rng.standard_normal(k), 800, 6000) * np.exp(-tk * 9) * .5
    place(out, whip, .02)
    k = seconds(.6)
    place(out, band(rng.standard_normal(k), 200, 1800) * envelope(k, .005, .35) * .7, .55)
    return out


def ratchet():
    # The winch drum let round one notch: the pawl lifting, a heavy iron click as it drops into the next tooth, the drum settling.
    n = seconds(1.0)
    out = np.zeros(n)
    k = seconds(.06)
    place(out, band(rng.standard_normal(k), 900, 5000) * envelope(k, .0005, .02) * .5, 0)
    k = seconds(.04)
    place(out, band(rng.standard_normal(k), 400, 3200) * envelope(k, .0004, .015) * 1.2, .22)
    tk = times(seconds(.6))
    ring = sum(np.sin(2 * np.pi * f * tk) * np.exp(-tk * d) * a for f, d, a in ((210, 8, .45), (530, 11, .2), (1180, 16, .08)))
    place(out, ring, .222)
    k = seconds(.4)
    place(out, band(rng.standard_normal(k), 60, 300) * envelope(k, .02, .3) * .35, .25)
    return out


def scrape():
    # An answer through the pipes from somewhere under the station: something hard drawn slowly along iron, far off and hollow.
    n = seconds(2.8)
    out = np.zeros(n)
    tk = times(n)
    grit = band(rng.standard_normal(n), 600, 3800) * (.5 + .5 * np.sin(2 * np.pi * 9 * tk) ** 2)
    f = 180 + 40 * np.sin(2 * np.pi * .6 * tk)
    phase = 2 * np.pi * np.cumsum(f) / RATE
    squeal = np.sin(phase) * .25 + np.sin(phase * 2.01) * .12
    env = np.clip(tk / .6, 0, 1) * np.clip((2.8 - tk) / 1.0, 0, 1)
    hollow = sum(np.sin(2 * np.pi * fr * tk) * a for fr, a in ((95, .12), (190, .06)))
    out += (grit * .45 + squeal + hollow) * env
    return out


def branch():
    # Water running behind a grate in a service branch: a steady trickle and gurgle, quiet, close to the iron.
    n = seconds(3.0)
    out = np.zeros(n)
    tk = times(n)
    trickle = band(rng.standard_normal(n), 900, 5000) * (.4 + .3 * np.sin(2 * np.pi * 1.3 * tk))
    gurgle = band(rng.standard_normal(n), 150, 800) * (rng.random(n) > .996) * 3
    env = np.clip(tk / .5, 0, 1) * np.clip((3.0 - tk) / .5, 0, 1)
    out += (trickle * .4 + band(gurgle, 120, 900)) * env
    return out


MAKERS = {'knock': knock, 'pump': pump, 'tap': tap, 'bleed': bleed, 'surge': surge, 'breath': breath, 'undertow': undertow, 'rise': rise, 'snap': snap,
          'ratchet': ratchet, 'scrape': scrape, 'branch': branch}


def main():
    folder = ASSETS / 'sounds/crossing'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'crossing.{name}',
               {'sounds': [{'name': f'the_oldest_house:crossing/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.crossing.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.crossing.{name}', subtitle)


if __name__ == '__main__':
    main()
