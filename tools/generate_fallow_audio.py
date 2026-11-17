"""Original sounds for Fallow (0.4.672, docs/FALLOW_0_4_672.md): a gas station on a two-lane road through the wheat, late in a dry summer.

The place has only its everyday sounds in this part: the wind going over the wheat, the insects in it, the radio on the back shelf
playing a ball game a long way off, and the bell on the pump. Nothing in them is frightening; what the afternoon becomes comes later.
0.4.673 adds the afternoon's: the register ringing up a sale, a wrench working under the car on the lift, coins left on the counter.
0.4.674 adds the four minutes': the radio's two-note tone, the pads' lids grinding open, a launch, a siren a long way off, the light,
and the ground shaking over the cellar. 0.4.680 adds what the light leaves: the canopy's sheets and columns coming down over the
pumps, the store falling in on itself, and the fall of it landing on the cellar doors, heard from under them. 0.4.681 adds the buried
days': the cellar's timbers creaking as they settle, the lantern's wick turned, a match struck, a stroke at the doors from under them and
the doors giving. 0.4.682 adds the wind after the light: over a field with nothing standing in it, a low hollow wind and the dry hiss of
ash carried along the ground.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'wind': ('Wind goes over the wheat', 48),
    'insects': ('Insects buzz in the wheat', 16),
    'radio': ('A radio plays a ball game', 12),
    'bell': ('The pump\'s bell rings', 24),
    'register': ('A cash register rings up a sale', 16),
    'wrench': ('A wrench works under a car', 16),
    'coins': ('Coins are set down on a counter', 10),
    'tone': ('A long tone comes over the radio', 16),
    'lids': ('Heavy concrete grinds open', 32),
    'launch': ('A rocket roars up out of the wheat', 32),
    'siren': ('A siren wails far off', 32),
    'blast': ('A roar shakes everything', 32),
    'rumble': ('The ground shakes', 16),
    'canopy': ('Sheet metal comes down with a crash', 32),
    'collapse': ('A building falls in', 32),
    'debris': ('Rubble lands on the doors overhead', 16),
    'settle': ('The cellar creaks and settles', 16),
    'wick': ('A lantern wick is turned', 8),
    'match': ('A match is struck', 8),
    'dig': ('Rubble is dug at overhead', 16),
    'give': ('The cellar doors give', 24),
    'ash_wind': ('Wind blows over the ash', 48),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/fallow/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'fallow.{name}'] == {'sounds': [{'name': f'the_oldest_house:fallow/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.fallow.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.fallow.{name}'] == subtitle, name
    print(f'Verified Fallow\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9672)


def wind():
    # Six seconds of a dry wind over a standing field: a broad low hush that rises and falls in long gusts, and over it the field's own
    # sound, a high dry hiss where the heads rub, which swells a beat after each gust as it runs through the wheat toward the listener.
    n = seconds(6.0)
    t = times(n)
    gust = .55 + .45 * np.sin(2 * np.pi * t / 5.2 + .6) ** 2 * (.7 + .3 * np.sin(2 * np.pi * t / 1.7))
    hush = band(rng.standard_normal(n), 60, 700) * gust
    late = np.interp(t - .35, t, gust, left=gust[0])
    rustle = band(rng.standard_normal(n), 2500, 9000) * late ** 2 * (.6 + .4 * np.abs(band(rng.standard_normal(n), 2, 12)))
    edge = np.clip(t / .6, 0, 1) * np.clip((6.0 - t) / .6, 0, 1)
    return (hush * .9 + rustle * .55) * edge


def insects():
    # Three seconds of the insects in the wheat: a dry steady trill from several at once, each its own rate and pitch, swelling and
    # falling a little, and once a grasshopper's short ratchet nearer than the rest.
    n = seconds(3.0)
    t = times(n)
    out = np.zeros(n)
    for rate, pitch, depth, phase in ((31, 4200, .8, 0), (27, 5100, .7, 1.1), (36, 3600, .6, 2.3), (22, 6100, .5, .4)):
        pulse = np.clip(np.sin(2 * np.pi * rate * t + phase), 0, None) ** 3
        tone = band(rng.standard_normal(n), pitch * .85, pitch * 1.15)
        swell = .7 + .3 * np.sin(2 * np.pi * t / (2.4 + phase) + phase)
        out += tone * pulse * depth * swell
    x = t - 1.6
    ratchet = band(rng.standard_normal(n), 3000, 9000) * (np.sin(2 * np.pi * 48 * t) > .3) * np.clip(x / .02, 0, 1) * np.clip((.45 - x) / .05, 0, 1) * 1.2
    edge = np.clip(t / .3, 0, 1) * np.clip((3.0 - t) / .3, 0, 1)
    return (out + ratchet) * edge


def radio():
    # Four seconds of a ball game on a small radio across the store: a man's voice calling the play, as a band of formant-shaped noise
    # with a pitch in it, words and pauses; the crowd swelling under him once; all of it squeezed through a little speaker, with hiss.
    n = seconds(4.0)
    t = times(n)
    words = np.zeros(n)
    at = .1
    while at < 3.7:
        length = rng.uniform(.12, .32)
        x = t - at
        shape = np.clip(x / .03, 0, 1) * np.clip((length - x) / .04, 0, 1) * (x >= 0)
        pitch = rng.uniform(115, 165)
        f = pitch * (1 + .08 * np.sin(2 * np.pi * 3 * x))
        voiced = np.sign(np.sin(2 * np.pi * np.cumsum(f) / RATE)) * .4
        words += (band(voiced, 300, 900) + band(voiced, 1100, 2400) * .6 + band(rng.standard_normal(n), 2500, 3500) * .15) * shape
        at += length + rng.choice((.05, .07, .09, .3), p=(.4, .3, .2, .1))
    crowd = band(rng.standard_normal(n), 400, 2500) * np.exp(-((t - 2.6) / .7) ** 2) * .55
    hiss = band(rng.standard_normal(n), 3000, 6000) * .06
    speaker = band(words + crowd, 350, 3400)
    edge = np.clip(t / .3, 0, 1) * np.clip((4.0 - t) / .3, 0, 1)
    return (speaker + hiss) * edge


def bell():
    # The bell on top of a gas pump as the nozzle comes off its hook: a small clapper striking a dome twice, quick, the dome ringing on
    # a few bright partials and dying, and the click of the hook under it.
    n = seconds(1.4)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((.04, 1.0), (.16, .7)):
        x = t - at
        on = x >= 0
        ring = (np.sin(2 * np.pi * 2210 * x) + .55 * np.sin(2 * np.pi * 3390 * x) + .3 * np.sin(2 * np.pi * 5480 * x))
        out += ring * np.exp(-np.clip(x, 0, None) / .32) * on * level
        out += band(rng.standard_normal(n), 2000, 8000) * np.exp(-np.clip(x, 0, None) / .006) * on * level * .6
    click = band(rng.standard_normal(n), 400, 3000) * np.exp(-np.clip(t, 0, None) / .01) * .5
    return out + click


def register():
    # An old mechanical register ringing up a sale: two keys going down with a clack each, the crank's whirr, the bell struck once,
    # bright and ringing on, and the drawer shooting out on its spring to a wooden knock.
    n = seconds(1.6)
    t = times(n)
    out = np.zeros(n)
    for at in (.02, .16):
        x = t - at
        on = x >= 0
        out += band(rng.standard_normal(n), 900, 5000) * np.exp(-np.clip(x, 0, None) / .012) * on * .8
        out += np.sin(2 * np.pi * 420 * x) * np.exp(-np.clip(x, 0, None) / .02) * on * .4
    whirr = (t >= .28) & (t < .5)
    out += band(rng.standard_normal(n), 300, 1800) * whirr * (.25 + .15 * np.sin(2 * np.pi * 38 * t))
    x = t - .52
    on = x >= 0
    out += (np.sin(2 * np.pi * 2650 * x) + .5 * np.sin(2 * np.pi * 4010 * x) + .25 * np.sin(2 * np.pi * 6300 * x)) * np.exp(-np.clip(x, 0, None) / .4) * on * .9
    x = t - .6
    on = x >= 0
    out += band(rng.standard_normal(n), 200, 2200) * np.exp(-np.clip(x, 0, None) / .05) * on * .5
    x = t - .7
    on = x >= 0
    out += (band(rng.standard_normal(n), 120, 900) * .7 + np.sin(2 * np.pi * 160 * x)) * np.exp(-np.clip(x, 0, None) / .03) * on
    return out


def wrench():
    # A ratchet wrench turning a bolt up under a car: a run of quick metal clicks, a pause as the hand comes back, another run, and a
    # little knock of the socket against the frame.
    n = seconds(1.8)
    t = times(n)
    out = np.zeros(n)
    for start in (.05, .75):
        for k in range(9):
            x = t - (start + k * .055)
            on = x >= 0
            ping = np.sin(2 * np.pi * (3100 + 120 * (k % 3)) * x) * np.exp(-np.clip(x, 0, None) / .008)
            out += (band(rng.standard_normal(n), 2500, 9000) * np.exp(-np.clip(x, 0, None) / .004) + .5 * ping) * on * (.7 + .3 * (k % 2))
    x = t - 1.45
    on = x >= 0
    out += (np.sin(2 * np.pi * 740 * x) + .6 * np.sin(2 * np.pi * 1310 * x)) * np.exp(-np.clip(x, 0, None) / .06) * on * .6
    return out


def coins():
    # A few coins set down on a counter one after another: each a short bright ring on its edge and a tick as it lies flat.
    n = seconds(1.0)
    t = times(n)
    out = np.zeros(n)
    for at, f, level in ((.03, 5200, 1.0), (.17, 6100, .8), (.26, 4700, .7), (.41, 5600, .5)):
        x = t - at
        on = x >= 0
        out += (np.sin(2 * np.pi * f * x) + .5 * np.sin(2 * np.pi * f * 1.48 * x)) * np.exp(-np.clip(x, 0, None) / .07) * on * level
        out += band(rng.standard_normal(n), 3000, 10000) * np.exp(-np.clip(x, 0, None) / .003) * on * level * .6
    return out


def tone():
    # A broadcast attention tone heard out of a small radio's speaker: two steady notes sounding together, a little apart in pitch so
    # they beat, squeezed through the speaker's narrow band, with the set's hiss under it. It begins and ends clean.
    n = seconds(4.0)
    t = times(n)
    notes = np.sin(2 * np.pi * 853 * t) + np.sin(2 * np.pi * 960 * t)
    hiss = band(rng.standard_normal(n), 2000, 7000) * .05
    edge = np.clip(t / .02, 0, 1) * np.clip((4.0 - t) / .02, 0, 1)
    return band(notes * .5 + hiss, 280, 3400) * edge


def lids():
    # Two slabs of concrete dragged across their seats far off in the field: a deep grinding that catches and slips, a motor's hum under
    # it, a heavy knock as each comes to rest against its stop.
    n = seconds(3.4)
    t = times(n)
    catch = .55 + .45 * np.abs(band(rng.standard_normal(n), 3, 14)) * 6
    grind = band(rng.standard_normal(n), 40, 420) * np.clip(catch, 0, 1.4)
    hum = np.sin(2 * np.pi * 55 * t) * .35 + np.sin(2 * np.pi * 110 * t) * .12
    run = np.clip(t / .3, 0, 1) * np.clip((2.9 - t) / .25, 0, 1)
    out = (grind + hum) * run
    x = t - 2.95
    on = x >= 0
    out += (band(rng.standard_normal(n), 30, 300) * 1.4 + np.sin(2 * np.pi * 48 * x) * 1.2) * np.exp(-np.clip(x, 0, None) / .12) * on
    return out


def launch():
    # A rocket going up out of the ground a long way off: a roar that swells out of nothing to a steady tearing thunder, crackling, with
    # a low rolling under it, then going away up into the sky, the high part leaving first.
    n = seconds(8.0)
    t = times(n)
    swell = np.clip(t / 1.2, 0, 1) ** 2
    away = np.clip((8.0 - t) / 5.0, 0, 1)
    roar = band(rng.standard_normal(n), 60, 2400) * swell * away
    high = band(rng.standard_normal(n), 1500, 6000) * swell * away ** 3 * .5
    crackle = (rng.random(n) < .004) * rng.standard_normal(n) * 4
    crackle = band(crackle, 400, 5000) * swell * away ** 2
    low = band(rng.standard_normal(n), 18, 90) * 2.2 * np.clip(t / .6, 0, 1) * np.clip((8.0 - t) / 3.0, 0, 1)
    return roar + high + crackle + low


def siren():
    # A civil siren a long way off across flat country: the wail climbs, holds, falls and climbs again, thinned by the distance, with a
    # second, fainter one farther off behind it a beat out of step.
    n = seconds(8.0)
    t = times(n)

    def wail(offset, level):
        phase = ((t + offset) % 4.0) / 4.0
        f = 180 + 420 * np.clip(np.where(phase < .45, phase / .45, np.where(phase < .7, 1.0, 1 - (phase - .7) / .3)), 0, 1)
        w = np.sin(2 * np.pi * np.cumsum(f) / RATE) + .4 * np.sin(4 * np.pi * np.cumsum(f) / RATE)
        return w * level

    out = band(wail(0, 1.0) + wail(1.3, .45), 150, 1800)
    edge = np.clip(t / .8, 0, 1) * np.clip((8.0 - t) / .8, 0, 1)
    return out * edge


def blast():
    # Everything at once: a crack that is all frequencies, a pressure so low it is felt more than heard, and a roar that rolls on and on
    # and goes down slowly into a rumble.
    n = seconds(6.0)
    t = times(n)
    crack = band(rng.standard_normal(n), 200, 4500) * np.exp(-t / .05) * 1.1
    thump = np.sin(2 * np.pi * (34 - 10 * np.clip(t, 0, 1)) * t) * np.exp(-t / .9) * 2.2
    roar = band(rng.standard_normal(n), 22, 520) * np.exp(-t / 2.4) * 3.2
    edge = np.clip(t / .004, 0, 1) * np.clip((6.0 - t) / .5, 0, 1)
    return (crack + thump + roar) * edge


def rumble():
    # The ground shaking over a cellar: a deep heavy rolling, the timbers of the roof creaking with it, grit and small stones coming
    # down onto the floor in little runs.
    n = seconds(4.0)
    t = times(n)
    roll = band(rng.standard_normal(n), 20, 140) * 2.0 * (.7 + .3 * np.sin(2 * np.pi * .7 * t))
    creak = band(rng.standard_normal(n), 300, 900) * (np.abs(band(rng.standard_normal(n), 1, 4)) * 8) * .12
    grit = band((rng.random(n) < .01) * rng.standard_normal(n), 2000, 8000) * .45
    edge = np.clip(t / .4, 0, 1) * np.clip((4.0 - t) / .8, 0, 1)
    return (roll + creak + grit) * edge


def canopy():
    # The canopy over the pumps coming down: its sheets buckling with a groan, then the crash of the deck and its columns onto the
    # concrete, the sheets ringing as they settle, a last one sliding off and slapping down.
    n = seconds(4.5)
    t = times(n)
    groan = band(rng.standard_normal(n), 90, 700) * np.abs(band(rng.standard_normal(n), 2, 9)) * 7 * np.clip(t / .5, 0, 1) * (t < .9)
    x = t - .9
    on = x >= 0
    crash = band(rng.standard_normal(n), 60, 5000) * np.exp(-np.clip(x, 0, None) / .25) * 2.4 * on
    ring = sum(np.sin(2 * np.pi * f * x + p) * np.exp(-np.clip(x, 0, None) / d) * a
               for f, p, d, a in ((287, 0, 1.4, .5), (713, 1, .9, .35), (1264, 2, .6, .22), (2189, 3, .4, .12))) * on
    y = t - 3.2
    slap = band(rng.standard_normal(n), 100, 3000) * np.exp(-np.clip(y, 0, None) / .08) * 1.3 * (y >= 0)
    edge = np.clip((4.5 - t) / .4, 0, 1)
    return (groan + crash + ring + slap) * edge


def collapse():
    # The store falling in on itself: a joist cracking, then the roof going in a long sliding roar of boards, plaster and gravel with
    # surges in it as each part goes, glass running out of a frame, and the last heavy pieces thudding down after.
    n = seconds(6.0)
    t = times(n)
    crack = band(rng.standard_normal(n), 300, 6000) * np.exp(-t / .03) * 1.6
    surge = 1 + .6 * np.abs(band(rng.standard_normal(n), .8, 3)) * 10
    slide = band(rng.standard_normal(n), 70, 2600) * np.clip((t - .15) / .5, 0, 1) * np.exp(-np.clip(t - 1.2, 0, None) / 1.6) * surge * 1.4
    glass = band((rng.random(n) < .006) * rng.standard_normal(n), 3000, 9000) * 1.2 * np.clip((t - 1.0) / .3, 0, 1) * np.exp(-np.clip(t - 1.5, 0, None) / .9)
    thuds = np.zeros(n)
    for at, level in ((3.6, 1.0), (4.15, .7), (4.9, .5)):
        x = t - at
        thuds += (band(rng.standard_normal(n), 40, 400) + np.sin(2 * np.pi * 52 * x)) * np.exp(-np.clip(x, 0, None) / .12) * level * (x >= 0)
    edge = np.clip(t / .003, 0, 1) * np.clip((6.0 - t) / .5, 0, 1)
    return (crack + slide + glass + thuds * 1.6) * edge


def debris():
    # Heard from under the cellar doors: heavy pieces landing on the boards overhead, a knock that shakes them, then more, irregular,
    # and the small stuff and grit pouring down onto them after until it thins out.
    n = seconds(4.0)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((0.0, 1.0), (.35, .8), (.52, .55), (1.1, .9), (1.6, .5), (2.05, .35)):
        x = t - at
        knock = band(rng.standard_normal(n), 35, 260) * 1.4 + np.sin(2 * np.pi * 176 * x) * .5
        out += knock * np.exp(-np.clip(x, 0, None) / .09) * level * (x >= 0)
    pour = band(rng.standard_normal(n), 1200, 7000) * (np.abs(band(rng.standard_normal(n), 2, 12)) * 6) * .25
    pour *= np.clip((t - .4) / .6, 0, 1) * np.clip((4.0 - t) / 1.6, 0, 1)
    edge = np.clip(t / .003, 0, 1) * np.clip((4.0 - t) / .3, 0, 1)
    return (out + pour) * edge


def settle():
    # 0.4.681, below in the buried days: the roof's timbers taking the weight on them, a long low creak that bends as it goes, a second
    # shorter one answering, and a run of dirt sifting down out of the planks onto the floor.
    n = seconds(3.2)
    t = times(n)
    out = np.zeros(n)
    for at, length, f0, f1, level in ((.1, 1.4, 96, 74, 1.0), (1.7, .7, 128, 110, .6)):
        x = t - at
        on = (x >= 0) & (x < length)
        f = f0 + (f1 - f0) * np.clip(x / length, 0, 1)
        phase = 2 * np.pi * np.cumsum(f) / RATE
        grain = 1 + .5 * band(rng.standard_normal(n), 20, 60) * 6
        env = np.sin(np.pi * np.clip(x / length, 0, 1)) * on
        out += (np.sin(phase) + .35 * np.sin(2 * phase) + .2 * np.sin(3.1 * phase)) * grain * env * level * .5
    out += band(rng.standard_normal(n), 300, 1200) * np.abs(band(rng.standard_normal(n), 3, 14)) * 4 * .25 * np.clip((t - .3) / .2, 0, 1) * np.exp(-np.clip(t - .3, 0, None) / 1.2)
    sift = band((rng.random(n) < .02) * rng.standard_normal(n), 2500, 9000) * .5 * np.clip((t - 1.2) / .4, 0, 1) * np.clip((3.2 - t) / 1.0, 0, 1)
    edge = np.clip(t / .05, 0, 1) * np.clip((3.2 - t) / .3, 0, 1)
    return (out + sift) * edge


def wick():
    # The lantern's wick turned: four small clicks of its toothed wheel against the burner's tin, close together, and the faint tick of
    # the chimney settling after.
    n = seconds(.7)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((.02, 1.0), (.11, .8), (.19, .9), (.28, .6), (.52, .25)):
        x = t - at
        out += band(rng.standard_normal(n), 2500, 8000) * np.exp(-np.clip(x, 0, None) / .006) * level * (x >= 0)
        out += np.sin(2 * np.pi * 3100 * x) * np.exp(-np.clip(x, 0, None) / .01) * level * .3 * (x >= 0)
    return out * np.clip((.7 - t) / .1, 0, 1)


def match():
    # A match struck: its head dragged across the striker, a rasp that catches into a hiss of flame, and the flame steadying.
    n = seconds(1.4)
    t = times(n)
    rasp = band(rng.standard_normal(n), 1500, 7000) * (np.abs(band(rng.standard_normal(n), 30, 80)) * 8) * (t < .18) * np.clip(t / .02, 0, 1)
    x = t - .17
    flare = band(rng.standard_normal(n), 400, 5000) * np.exp(-np.clip(x, 0, None) / .14) * 1.6 * (x >= 0)
    burn = band(rng.standard_normal(n), 150, 1500) * .35 * np.clip(x / .1, 0, 1) * (x >= 0) * np.clip((1.4 - t) / .5, 0, 1)
    return rasp + flare + burn


def dig():
    # A stroke at the doors from under them: a shoulder or both hands driven up against the boards, a dull wooden thud, the fall on
    # them grinding as it shifts, and grit pattering down into the well after.
    n = seconds(1.6)
    t = times(n)
    thud = (band(rng.standard_normal(n), 45, 300) * 1.6 + np.sin(2 * np.pi * 92 * t) * .7) * np.exp(-t / .07)
    grind = band(rng.standard_normal(n), 120, 1800) * np.abs(band(rng.standard_normal(n), 4, 20)) * 6 * .35 * np.clip((t - .05) / .08, 0, 1) * np.exp(-np.clip(t - .1, 0, None) / .35)
    grit = band((rng.random(n) < .015) * rng.standard_normal(n), 2500, 9000) * .6 * np.clip((t - .15) / .1, 0, 1) * np.clip((1.6 - t) / .7, 0, 1)
    edge = np.clip(t / .003, 0, 1) * np.clip((1.6 - t) / .2, 0, 1)
    return (thud + grind + grit) * edge


def give():
    # The doors giving: the leaves heaved up under the last of the fall with a long groan of wood, the rubble sliding off them in a roar
    # that thins, the leaves slamming back over onto the yard, and a little more coming down after.
    n = seconds(3.6)
    t = times(n)
    f = 70 + 40 * np.clip(t / 1.1, 0, 1)
    phase = 2 * np.pi * np.cumsum(f) / RATE
    groan = (np.sin(phase) + .4 * np.sin(2.03 * phase)) * np.clip(t / .3, 0, 1) * (t < 1.2) * np.clip((1.2 - t) / .2, 0, 1) * .6
    slide = band(rng.standard_normal(n), 80, 3200) * np.clip((t - .8) / .3, 0, 1) * np.exp(-np.clip(t - 1.1, 0, None) / .7) * 1.4
    x = t - 1.55
    slam = (band(rng.standard_normal(n), 40, 900) * 1.8 + np.sin(2 * np.pi * 64 * x) * .8) * np.exp(-np.clip(x, 0, None) / .1) * (x >= 0)
    y = t - 1.95
    slam2 = (band(rng.standard_normal(n), 40, 900) * 1.2 + np.sin(2 * np.pi * 71 * y) * .6) * np.exp(-np.clip(y, 0, None) / .09) * (y >= 0)
    after = band((rng.random(n) < .01) * rng.standard_normal(n), 1500, 8000) * .5 * np.clip((t - 2.0) / .2, 0, 1) * np.clip((3.6 - t) / 1.0, 0, 1)
    edge = np.clip(t / .01, 0, 1) * np.clip((3.6 - t) / .3, 0, 1)
    return (groan + slide + slam + slam2 + after) * edge


def ash_wind():
    # Eight seconds of wind over a burnt field: nothing stands in it to rustle, so there is only a low hollow moan that swells and sinks
    # in slow gusts, a thin whistle over it at the top of each gust, and the fine dry hiss of ash grains blown along the ground, which
    # comes in grainy bursts as the gusts lift it.
    n = seconds(8.0)
    t = times(n)
    gust = .45 + .55 * np.sin(2 * np.pi * t / 7.4 + 1.1) ** 2 * (.75 + .25 * np.sin(2 * np.pi * t / 2.3))
    moan = band(rng.standard_normal(n), 35, 260) * gust
    whistle = band(rng.standard_normal(n), 900, 1300) * np.clip(gust - .7, 0, 1) * 1.6
    grains = band((rng.random(n) < .04) * rng.standard_normal(n), 3000, 10000) * np.interp(t - .5, t, gust, left=gust[0]) ** 3
    edge = np.clip(t / .8, 0, 1) * np.clip((8.0 - t) / .8, 0, 1)
    return (moan * 1.1 + whistle * .35 + grains * .5) * edge


MAKERS = {'ash_wind': ash_wind, 'wind': wind, 'insects': insects, 'radio': radio, 'bell': bell, 'register': register, 'wrench': wrench, 'coins': coins,
          'tone': tone, 'lids': lids, 'launch': launch, 'siren': siren, 'blast': blast, 'rumble': rumble, 'canopy': canopy,
          'collapse': collapse, 'debris': debris, 'settle': settle, 'wick': wick, 'match': match, 'dig': dig, 'give': give}


def main():
    folder = ASSETS / 'sounds/fallow'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'fallow.{name}',
               {'sounds': [{'name': f'the_oldest_house:fallow/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.fallow.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.fallow.{name}', subtitle)


if __name__ == '__main__':
    main()
