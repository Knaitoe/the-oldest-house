"""What each room sounds like from the hall, through its shut door (0.4.576, docs/DOOR_BLEEDS_0_4_576.md).

One sound to a room: its DoorBleed's `bleed.<name>`. Where the room has sounds of its own they are what is heard, taken from the
pack's own files and put behind wood: the top of them gone, quieter, a few seconds long. Where it has none, the sound is
synthesized here from oscillators and noise. No recordings.

Run from the repository root. Existing files are kept, so the committed sounds only change when they are deleted first.
`--check` needs nothing but the files: it looks for each sound, its entry in sounds.json and its subtitle, and reads
DoorBleed.java for the `bleed.` sounds, so a sound named there with none here (or one here that no door lets out) fails.
"""
import json
import re
import sys
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/DoorBleed.java'
REACH = 10

# Every room's sound and its subtitle. A subtitle says what is heard and that a door is in the way; it names no colour, nobody's
# age and nothing anybody feels.
ROWS = {
    'red_room': 'A low note swells behind a door',
    'ted_caver': 'A chisel taps rock behind a door',
    'zampano_courtyard': 'A pen scratches behind a door',
    'whale': 'Clocks tick out of step behind a door',
    'barn_well': 'Slow breathing behind a door',
    'plain': 'Dry wind in grass behind a door',
    'hospital': 'A monitor beeps behind a door',
    'karen_room': 'A projector runs behind a door',
    'holloway_camp': 'A fire crackles behind a door',
    'seance': 'A chair scrapes, and a low voice, behind a door',
    'wallpaper_nursery': 'Paper tears in strips behind a door',
    'hill_nursery': 'Something pounds a wall behind a door',
    'miniatures': 'A small saw works behind a door',
    'masque': 'Dance music stops for a clock behind a door',
    'usher': 'Thunder rolls behind a door',
    'winchester': 'A hammer and a saw behind a door',
    'child_room': 'A music box plays behind a door',
    'crimson_hall': 'A recording plays behind a door',
    'bly_route': 'Wet footsteps pass behind a door',
    'elk_lot': 'Pool balls clack and a truck passes, behind a door',
    'elk_fan': 'A ceiling fan turns behind a door',
    'mapping_interior': "Bells on a dancer's steps behind a door",
    'holy_rabbit': 'Wind over snow behind a door',
    'confession': 'One voice, low and steady, behind a door',
    'elk_carcasses': 'Flies behind a door',
    'costume_night': 'A megaphone squeals and a crowd claps, behind a door',
    'movie_night': 'Film music behind a door',
    'winter_lake': 'Ice groans behind a door',
    'camp_blood': 'Crickets, and a screen door, behind a door',
    'devils_rock': 'Pages turn behind a door',
    'wheel': 'Doors open one after another behind a door',
    'ghosts_set': 'A bell, then a clapperboard, behind a door',
    'end_world_cabin': 'Rain on a roof, and knocking, behind a door',
    'family_copy': 'A handle turns and is let go behind a door',
    'old_cabin': 'A chair rocks behind a door',
    'three_fingers': 'A fire spits and something scratches, behind a door',
    'nightclub': 'A bass beat thuds behind a door',
    'lake_house': 'A compressor chugs behind a door',
    'borrasca': 'Metal grinds, far off, behind a door',
    'painted_interior': 'Boards creak under a weight behind a door',
    'second_waitress': 'Plates are stacked behind a door',
    'tunnel_periphery': 'Traffic rumbles behind a door',
    'missing_sleep': 'A monitor clicks from feed to feed behind a door',
    'layered_man': 'Something wet tears behind a door',
    'motion_light': 'A buzzer rasps behind a door',
    'backseat': 'An engine idles and wipers drag, behind a door',
    'silo': 'Grain shifts behind a door',
    'dare_in_the_dark': 'Giggling, and a tap dripping, behind a door',
    'way_out': 'Birdsong catches and repeats behind a door',
    'feet_up': 'Someone counts behind a door',
    'whistlers': 'Something whistles in pines behind a door',
    'work_print': 'A tape goes in and a deck plays, behind a door',
    'polaroid_trail': 'A flash charges and a shutter clicks, behind a door',
    'left_right': 'A CB radio crackles behind a door',
    'dead_air': 'A flat tone holds behind a door',
    'pitt': 'A draft moves through stone behind a door',
    'closing_hall': 'Walls step nearer behind a door',
    'storeroom': 'A bulb buzzes and shelving drags, behind a door',
    'stillness': 'Snuffling at a floor behind a door',
    'line': 'A line creaks and snaps taut behind a door',
    'own_light': 'Two sets of footsteps, in step, behind a door',
    'polybius': 'Arcade machines chirp behind a door',
    'replacement': 'Claws click along behind a door',
    'last_swimmer': 'Someone swims a length behind a door',
    'vacant_possession': 'Packing tape rips off a roll behind a door',
    'people_lights': 'Wheels roll over boards behind a door',
    'place_for_you': 'A kettle comes to the boil behind a door',
    'second_person': 'Dryers tumble behind a door',
    'deer_road': 'Hazard lights tick behind a door',
    'state_route': 'Cars rush past behind a door',
    'marsh_supper': 'A dinner bell rings behind a door',
    'tall_grass': 'Someone calls from tall grass behind a door',
    'growing_things': 'Something rustles in shoots behind a door',
    'last_crossing': 'A pump labours and a winch ratchets, behind a door',
    'pinehook': 'Bottles clink and someone laughs, behind a door',
    'the_drawing': 'A crowd murmurs and paper rustles, behind a door',
    'corner_house': 'Cicadas, and an iron gate, behind a door',
    'black_city': 'Something pipes, far off, behind a door',
    'kept': 'Machines hum and a screen wakes, behind a door',
    'last_orders': 'Darts thud into a board behind a door',
    'bike_night': 'A card rattles in spokes behind a door',
    'pell_house': 'A hairbrush, slowly, behind a door',
    'ones_inside': 'Something moves inside a wall, behind a door',
}


def java_sounds():
    """The `bleed.` sounds DoorBleed.java gives its doors."""
    return set(re.findall(r'\.sound\("bleed\.([a-z0-9_]+)"\)', JAVA.read_text()))


def check():
    sounds = json.loads((ASSETS / 'sounds.json').read_text())
    lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
    wanted = java_sounds()
    assert wanted == set(ROWS), (sorted(wanted - set(ROWS)), sorted(set(ROWS) - wanted))
    assert len(set(ROWS.values())) == len(ROWS), 'two rooms are heard as the same words'
    for name, subtitle in ROWS.items():
        path = ASSETS / f'sounds/bleed/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'bleed.{name}'] == {'sounds': [{'name': f'the_oldest_house:bleed/{name}', 'attenuation_distance': REACH}],
                                           'subtitle': f'subtitles.the_oldest_house.bleed.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.bleed.{name}'] == subtitle, name
        assert subtitle.endswith('behind a door'), name
    stale = sorted(f.name for f in (ASSETS / 'sounds/bleed').iterdir() if f.stem not in ROWS)
    assert not stale, stale
    print(f"Verified the doors' bleeds: {len(ROWS)} sounds.")


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

# Nothing above a few thousand cycles comes through a door, so half the usual rate holds all of it.
RATE = 22050
rng = np.random.default_rng(576)


def seconds(n):
    return int(n * RATE)


def times(n):
    return np.arange(n) / RATE


def band(x, low=None, high=None, slope=0.25, rate=RATE):
    """Filter in the frequency domain with soft shoulders (as Movie Night's does)."""
    spectrum = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / rate)
    gain = np.ones_like(f)
    if high is not None:
        gain *= 1 / (1 + (f / high) ** (2 / slope))
    if low is not None:
        gain *= 1 - 1 / (1 + (f / max(1, low)) ** (2 / slope))
    return np.fft.irfft(spectrum * gain, len(x))


def envelope(n, attack, decay):
    t = times(n)
    return np.minimum(1, t / max(attack, 1e-4)) * np.exp(-t / decay)


def adsr(n, attack, release):
    t = times(n)
    total = n / RATE
    return np.minimum(1, t / max(attack, 1e-4)) * np.minimum(1, np.maximum(0, (total - t) / max(release, 1e-4)))


def place(out, sound, at):
    start = seconds(at)
    end = min(len(out), start + len(sound))
    if end > start:
        out[start:end] += sound[:end - start]


def noise(n):
    return rng.standard_normal(n)


def take(rel, start=0.0, length=None, cut=1100.0):
    """One of the pack's own sounds as it comes through a door: nothing above `cut`, at this tool's rate, its loudest moment at
    one, and a breath of fade at both ends so a piece cut out of a longer sound does not click."""
    data, rate = soundfile.read(str(ASSETS / f'sounds/{rel}.ogg'), dtype='float64', always_2d=True)
    x = band(data.mean(axis=1), 40, min(cut, RATE * .45), rate=rate)
    if rate != RATE:
        x = np.interp(np.arange(int(len(x) * RATE / rate)) * rate / RATE, np.arange(len(x)), x)
    x = x[seconds(start):seconds(start + length) if length else None]
    assert len(x) > seconds(.05), rel
    edge = min(seconds(.03), len(x) // 4)
    x[:edge] *= np.linspace(0, 1, edge)
    x[-edge:] *= np.linspace(1, 0, edge)
    return x / max(1e-9, np.max(np.abs(x)))


def scene(length, *parts):
    """Sounds laid into a stretch of quiet: each (sound, when it starts in seconds, how loud)."""
    out = np.zeros(seconds(length))
    for sound, at, gain in parts:
        place(out, sound * gain, at)
    return out


# ------------------------------------------------------------------------------------------------ rooms with no sound of their own
def red_room():
    # One held note, coming up and going down again as the light does.
    n = seconds(3.4)
    t = times(n)
    swell = np.sin(np.pi * t / 3.4) ** 2
    note = sum(np.sin(2 * np.pi * f * t + i) * w for i, (f, w) in enumerate(((82.4, 1), (123.8, .7), (164.6, .6), (247.3, .35), (329.2, .15))))
    return (note + band(noise(n), 80, 400) * .5) * swell


def pen():
    # A nib on paper: strokes of different lengths, a lift between words.
    out = np.zeros(seconds(3.3))
    at = .15
    while at < 2.9:
        m = seconds(rng.uniform(.07, .24))
        t = times(m)
        stroke = band(noise(m), 1300, 3600) * (.6 + .4 * np.sin(2 * np.pi * rng.uniform(30, 55) * t)) * adsr(m, .01, .03)
        place(out, stroke * rng.uniform(.6, 1), at)
        at += m / RATE + rng.choice([.05, .07, .09, .3])
    return band(out, 200, 2600)


def clocks():
    # Three clocks, none of them keeping the others' time.
    out = np.zeros(seconds(3.4))
    for period, pitch, gain, first in ((.5, 1150, 1, .05), (.46, 860, .8, .21), (.57, 1420, .65, .12)):
        k = 0
        while first + k * period < 3.2:
            m = seconds(.06)
            t = times(m)
            tick = band(noise(m), 700, 2600) * envelope(m, .0005, .006) + np.sin(2 * np.pi * pitch * t) * envelope(m, .0005, .012) * .6
            place(out, tick * gain * (1 if k % 2 == 0 else .7), first + k * period)
            k += 1
    return band(out, 150, 1900)


def dry_wind():
    n = seconds(3.4)
    t = times(n)
    gust = .5 + .5 * np.sin(2 * np.pi * .31 * t + 1.2) * np.sin(2 * np.pi * .11 * t + .4)
    low = band(noise(n), 220, 900) * (.55 + .45 * gust)
    grass = band(noise(n), 1800, 3600) * gust ** 2 * .35
    return (low + grass) * adsr(n, .5, .7)


def snow_wind():
    # Wind with snow in it: its body, and the note it finds in a gap somewhere, rising when it blows harder. (The room's own
    # blizzard is nearly all below what comes out of a speaker.)
    n = seconds(3.2)
    t = times(n)
    gust = .5 + .5 * np.sin(2 * np.pi * .33 * t + .5) * np.sin(2 * np.pi * .13 * t + 2)
    body = band(noise(n), 150, 700) * (.5 + .5 * gust)
    breath = np.abs(band(noise(n), None, 20))
    howl = np.sin(2 * np.pi * np.cumsum(560 + 260 * gust + 30 * np.sin(2 * np.pi * 2.1 * t)) / RATE) * (.3 + .7 * gust) * (.5 + .5 * breath / breath.max())
    return (body / np.max(np.abs(body)) + howl * .3) * adsr(n, .5, .7)


def fire():
    # Its low burning, and what spits in it.
    n = seconds(3.4)
    t = times(n)
    out = band(noise(n), 50, 380) * (.6 + .4 * np.sin(2 * np.pi * .7 * t)) * .5
    for _ in range(70):
        m = seconds(rng.uniform(.004, .02))
        snap = band(noise(max(m, 64)), 700, 2600) * envelope(max(m, 64), .0004, rng.uniform(.002, .008))
        place(out, snap * rng.exponential(.35), rng.uniform(.05, 3.2))
    return band(out, 45, 2400) * adsr(n, .2, .4)


def paper():
    # Wallpaper coming off a wall in strips: each tear quickens as it goes.
    out = np.zeros(seconds(3.4))
    for at, length in ((.2, .7), (1.25, .55), (2.2, .85)):
        m = seconds(length)
        t = times(m)
        grain = .5 + .5 * np.sign(np.sin(2 * np.pi * np.cumsum(45 + 50 * t / length) / RATE))
        place(out, band(noise(m), 800, 3600) * (.35 + .65 * grain) * adsr(m, .03, .08), at)
    return band(out, 300, 2800)


def pounding():
    # Something heavy against a wall, four times, the last from further off; and what hangs on the wall after each.
    out = np.zeros(seconds(3.4))
    for at, g in ((.2, 1), (1.05, .9), (1.9, 1), (2.75, .55)):
        m = seconds(.5)
        t = times(m)
        blow = np.sin(2 * np.pi * (84 - 26 * np.minimum(t, .12) / .12) * t) * envelope(m, .002, .11)
        blow += band(noise(m), 90, 800) * envelope(m, .001, .05) * 1.2
        place(out, blow * g, at)
        k = seconds(.25)
        place(out, band(noise(k), 200, 700) * envelope(k, .01, .06) * .12 * g, at + .08)
    return band(out, 50, 900)


def small_saw():
    # A saw no longer than a finger, and two taps of a pin hammer.
    out = np.zeros(seconds(3.3))
    for i in range(9):
        m = seconds(.11 if i % 2 == 0 else .09)
        t = times(m)
        stroke = band(noise(m), 1500, 4200) * (.5 + .5 * np.sin(2 * np.pi * 150 * t)) * adsr(m, .012, .03)
        place(out, stroke * (1 if i % 2 == 0 else .65), .15 + i * .2)
    for at in (2.3, 2.62):
        m = seconds(.08)
        t = times(m)
        place(out, (np.sin(2 * np.pi * 1750 * t) * envelope(m, .0005, .012) + band(noise(m), 900, 3000) * envelope(m, .0005, .004)) * .8, at)
    return band(out, 400, 3000)


def masque():
    # The dance, cut off between one bar and the next; and the clock it stopped for.
    music = take('literary/party', 0, 1.9, cut=800)
    music[-seconds(.02):] *= np.linspace(1, 0, seconds(.02))
    m = seconds(1.4)
    t = times(m)
    bell = sum(np.sin(2 * np.pi * f * t) * np.exp(-t / d) * w for f, d, w in ((98, 1.2, 1), (196.5, .9, .7), (247, .6, .5), (294.3, .5, .45), (393, .35, .3)))
    return scene(3.4, (music, 0, .8), (bell * adsr(m, .004, .5), 2.0, .9))


def thunder():
    n = seconds(3.4)
    t = times(n)
    shape = np.exp(-t / 1.3) + .55 * np.exp(-((t - 1.1) / .25) ** 2) + .4 * np.exp(-((t - 1.9) / .35) ** 2)
    roll = band(band(noise(n), 40, 420) * shape * np.minimum(1, t / .04), 35, 600) * adsr(n, .01, .6)
    rain = band(noise(n), 500, 1500) * adsr(n, .3, .5)
    return roll / np.max(np.abs(roll)) + rain / np.max(np.abs(rain)) * .06


def wet_steps():
    # Somebody walking who has come out of water: the foot down, the foot coming up, and what drips from them between.
    out = np.zeros(seconds(3.4))
    for at in (.3, 1.3, 2.3):
        m = seconds(.3)
        t = times(m)
        step = np.sin(2 * np.pi * 88 * t) * envelope(m, .003, .05) + band(noise(m), 450, 2100) * envelope(m, .02, .08) * .9
        k = seconds(.18)
        tk = times(k)
        suck = band(noise(k), 300, 1000) * np.exp(-((tk - .12) / .035) ** 2) * .5
        place(out, step, at)
        place(out, suck, at + .22)
    for at in (.95, 2.9):
        k = seconds(.2)
        tk = times(k)
        place(out, np.sin(2 * np.pi * (900 + 900 * np.minimum(tk, .03) / .03) * tk) * envelope(k, .001, .03) * .35, at)
    return band(out, 60, 1900)


def fan():
    # The blades' beat in the air, the motor under it, and the pull chain touching the housing once a turn.
    n = seconds(3.4)
    t = times(n)
    blade = (.5 + .5 * np.cos(2 * np.pi * 2.4 * t)) ** 3
    out = band(noise(n), 70, 520) * (.25 + .75 * blade) + np.sin(2 * np.pi * 100 * t) * .08
    for k in range(3):
        m = seconds(.03)
        place(out, band(noise(m), 900, 2200) * envelope(m, .0005, .004) * .5, .35 + k * 1.25)
    return band(out, 50, 1300) * adsr(n, .3, .4)


def dancer():
    # A dancer's steps, and the bells tied at the ankles answering each one.
    out = np.zeros(seconds(3.4))
    for at in (.2, .55, .9, 1.08, 1.6, 1.95, 2.3, 2.48, 2.9):
        m = seconds(.14)
        place(out, np.sin(2 * np.pi * 78 * times(m)) * envelope(m, .003, .03) * .6, at)
        for _ in range(7):
            k = seconds(.12)
            ring = np.sin(2 * np.pi * rng.uniform(2300, 3900) * times(k)) * envelope(k, .0005, rng.uniform(.02, .06))
            place(out, ring * rng.uniform(.15, .4), at + rng.uniform(0, .035))
    return band(out, 60, 3600)


def murmur():
    # One voice saying something long and level through a screen: its pitch, two vowels turning into each other, its breaths.
    n = seconds(3.4)
    t = times(n)
    phase = 2 * np.pi * np.cumsum(104 + 7 * np.sin(2 * np.pi * .6 * t) + 4 * np.sin(2 * np.pi * 1.7 * t + 1)) / RATE
    voice = sum(np.sin(k * phase) / k for k in range(1, 14))
    vowel = .5 + .5 * np.sin(2 * np.pi * 1.3 * t)
    voice = band(voice, 350, 750) * vowel + band(voice, 800, 1250) * (1 - vowel) * .7
    syllables = np.clip(np.sin(2 * np.pi * 3.7 * t) * .8 + .5 + .3 * np.sin(2 * np.pi * .9 * t), 0, 1)
    breaths = (np.sin(2 * np.pi * .42 * t + .8) > -.55).astype(float)
    breaths = np.convolve(breaths, np.ones(seconds(.05)) / seconds(.05), 'same')
    return band(voice * syllables * breaths, 120, 800) * adsr(n, .15, .4)


def flies():
    n = seconds(3.4)
    t = times(n)
    out = np.zeros(n)
    for _ in range(5):
        wander = np.cumsum(rng.standard_normal(n))
        wander = band(wander - wander.mean(), None, 3)
        wander /= max(1e-9, np.max(np.abs(wander)))
        phase = 2 * np.pi * np.cumsum(rng.uniform(165, 235) + 18 * wander) / RATE
        buzz = sum(np.sin(k * phase) / k for k in range(1, 9))
        near = .35 + .65 * (.5 + .5 * np.sin(2 * np.pi * rng.uniform(.2, .6) * t + rng.uniform(0, 6)))
        out += buzz * near * rng.uniform(.5, 1)
    return band(out, 120, 1700) * adsr(n, .3, .4)


def camp():
    # Night outside a cabin: the crickets, and a screen door let go to slap shut on its spring.
    out = np.zeros(seconds(3.4))
    m = seconds(.5)
    t = times(m)
    spring = np.sin(2 * np.pi * np.cumsum(520 + 260 * t / .5) / RATE) * (.5 + .5 * np.sin(2 * np.pi * 31 * t)) * adsr(m, .03, .1) * .35
    place(out, spring, .9)
    for at, g in ((1.45, 1), (1.6, .5)):
        k = seconds(.12)
        tk = times(k)
        place(out, (band(noise(k), 300, 1800) * envelope(k, .001, .02) + np.sin(2 * np.pi * 140 * tk) * envelope(k, .001, .03)) * g, at)
    return scene(3.4, (take('goatman/crickets', 0, 3.4, cut=5200), 0, .35), (band(out, 100, 2000), 0, 1))


def pages():
    out = np.zeros(seconds(3.3))
    for at in (.3, 1.7):
        m = seconds(.42)
        t = times(m)
        flutter = .4 + .6 * np.abs(band(noise(m), None, 60))
        turn = band(noise(m), 1000, 3800) * flutter / flutter.max() * np.sin(np.pi * t / .42) ** 2
        place(out, turn, at)
        k = seconds(.1)
        place(out, band(noise(k), 150, 700) * envelope(k, .002, .025) * .5, at + .4)
    return band(out, 150, 3000)


def doors():
    # A latch and a hinge, three times, each further into the house than the last.
    out = np.zeros(seconds(3.4))
    for i, at in enumerate((.15, 1.2, 2.25)):
        g = (1, .65, .4)[i]
        k = seconds(.05)
        place(out, band(noise(k), 700, 2400) * envelope(k, .0005, .008) * g, at)
        m = seconds(.6)
        t = times(m)
        creak = np.sign(np.sin(2 * np.pi * np.cumsum(290 - 110 * t / .6) / RATE)) * (.5 + .5 * np.sin(2 * np.pi * 27 * t)) * adsr(m, .05, .2)
        place(out, band(creak, 150, (1500, 1000, 650)[i]) * .5 * g, at + .07)
    return band(out, 80, 1600)


def slate():
    # The bell that says a camera is turning, and the sticks.
    out = np.zeros(seconds(3.0))
    m = seconds(.7)
    t = times(m)
    place(out, np.sign(np.sin(2 * np.pi * 415 * t)) * (.6 + .4 * np.sign(np.sin(2 * np.pi * 28 * t))) * adsr(m, .005, .03) * .35, .2)
    k = seconds(.2)
    tk = times(k)
    place(out, band(noise(k), 500, 3200) * envelope(k, .0003, .02) + np.sin(2 * np.pi * 380 * tk) * envelope(k, .0005, .035) * .7, 1.6)
    return band(out, 100, 2200)


def rocker():
    # A rocking chair on boards: forward, the runner coming down, and back.
    out = np.zeros(seconds(3.4))
    for i in range(3):
        at = .15 + i * 1.1
        for start, f0, f1, length, g in ((0, 150, 112, .34, 1), (.55, 118, 152, .28, .7)):
            m = seconds(length)
            t = times(m)
            creak = np.sign(np.sin(2 * np.pi * np.cumsum(f0 + (f1 - f0) * t / length) / RATE)) * (.45 + .55 * np.sin(2 * np.pi * 21 * t)) * adsr(m, .04, .1)
            place(out, band(creak, 90, 1200) * .5 * g, at + start)
        k = seconds(.12)
        place(out, np.sin(2 * np.pi * 68 * times(k)) * envelope(k, .003, .03) * .6, at + .34)
    return band(out, 50, 1200)


def plates():
    out = np.zeros(seconds(3.2))
    for at, g in ((.2, .7), (.62, 1), (1.3, .8), (1.72, .9), (2.5, 1)):
        m = seconds(.2)
        t = times(m)
        base = rng.uniform(1500, 2100)
        clink = sum(np.sin(2 * np.pi * base * r * t) * envelope(m, .0005, d) * w for r, d, w in ((1, .05, 1), (1.47, .035, .6), (2.09, .02, .35)))
        place(out, (clink + np.sin(2 * np.pi * 190 * t) * envelope(m, .001, .02) * .5) * g, at)
    return band(out, 120, 3000)


def feeds():
    # A monitor's hum, and the click and the smear of picture each time it changes camera.
    n = seconds(3.4)
    t = times(n)
    out = (np.sin(2 * np.pi * 60 * t) + .5 * np.sin(2 * np.pi * 120 * t) + .25 * np.sin(2 * np.pi * 180 * t)) * .25
    for at in (.5, 1.6, 2.7):
        k = seconds(.012)
        place(out, band(noise(max(k, 128)), 1200, 3200) * envelope(max(k, 128), .0003, .003), at)
        m = seconds(.07)
        place(out, band(noise(m), 400, 2400) * adsr(m, .003, .02) * .6, at + .012)
    return band(out, 45, 2300) * adsr(n, .2, .3)


def arcade():
    # A cabinet nobody is playing, going through what it plays to bring somebody over; a shot; a coin.
    n = seconds(3.4)
    out = band(noise(n), 60, 260) * .12
    for i, f in enumerate((392, 523, 659, 784, 659, 523, 392, 330, 392, 523, 784, 1047)):
        m = seconds(.1)
        place(out, np.sign(np.sin(2 * np.pi * f * times(m))) * adsr(m, .003, .02) * .22, .2 + i * .12)
    m = seconds(.5)
    place(out, np.sign(np.sin(2 * np.pi * np.cumsum(900 - 1400 * times(m)) / RATE)) * adsr(m, .005, .1) * .2, 2.1)
    k = seconds(.25)
    tk = times(k)
    place(out, (np.sin(2 * np.pi * 2100 * tk) + .6 * np.sin(2 * np.pi * 3150 * tk)) * envelope(k, .0005, .05) * .3, 2.85)
    return band(out, 80, 2000)


def claws():
    # Four feet at a trot on a hard floor, and a tag ringing on a collar.
    out = np.zeros(seconds(3.3))
    at, i = .2, 0
    while at < 2.9:
        m = seconds(.03)
        place(out, band(noise(m), 1800, 4500) * envelope(m, .0003, .004) * (1 if i % 2 == 0 else .7), at)
        if i % 4 == 0:
            k = seconds(.12)
            tk = times(k)
            place(out, (np.sin(2 * np.pi * 3400 * tk) + .7 * np.sin(2 * np.pi * 4350 * tk)) * envelope(k, .0005, .03) * .25, at + .01)
        at += .11 if i % 2 == 0 else .2
        i += 1
    return band(out, 500, 4200)


def lengths():
    # A pool hall's water, three strokes, and the turn at the wall.
    n = seconds(3.4)
    t = times(n)
    out = band(noise(n), 90, 460) * (.5 + .5 * np.sin(2 * np.pi * .45 * t)) * .18
    for at in (.15, .75, 1.35):
        m = seconds(.3)
        place(out, band(noise(m), 280, 1400) * adsr(m, .05, .15) * .6, at)
    return scene(3.4, (band(out, 60, 1300), 0, 1), (take('swimmer/turn', cut=1300), 2.0, .9))


def tape_roll():
    # Tape pulled off its roll across a box, twice, and torn off against the teeth.
    out = np.zeros(seconds(3.3))
    for at, length in ((.25, .8), (1.75, 1.0)):
        m = seconds(length)
        t = times(m)
        rip = np.sin(2 * np.pi * np.cumsum(95 + 60 * t / length) / RATE) > 0
        place(out, band(noise(m), 1300, 4200) * (.3 + .7 * rip) * adsr(m, .02, .03), at)
        k = seconds(.05)
        place(out, band(noise(k), 600, 2500) * envelope(k, .0005, .01) * .9, at + length)
    return band(out, 300, 3200)


def birdsong():
    # One phrase, sung the same way each time; the third time it catches, and goes on from where it caught.
    phrase = np.zeros(seconds(.9))
    for at, f0, f1, length in ((0, 2600, 3300, .09), (.14, 3300, 2500, .08), (.3, 2900, 2900, .05), (.38, 2900, 2900, .05), (.52, 2400, 3500, .14)):
        m = seconds(length)
        t = times(m)
        place(phrase, np.sin(2 * np.pi * np.cumsum(f0 + (f1 - f0) * t / length) / RATE) * np.sin(np.pi * t / length) ** 2, at)
    out = np.zeros(seconds(3.4))
    place(out, phrase, .15)
    place(out, phrase, 1.2)
    caught = seconds(.48)
    place(out, phrase[:caught], 2.25)
    k = seconds(.03)
    place(out, band(noise(k), 500, 4000) * .5, 2.73)
    place(out, phrase[:caught], 2.76)
    return band(out, 600, 4200)


def tall_grass():
    n = seconds(3.4)
    t = times(n)
    sway = (.5 + .5 * np.sin(2 * np.pi * .5 * t + .6)) ** 2
    rustle = band(band(noise(n), 1400, 4200) * (.2 + .8 * sway) * adsr(n, .3, .4), 500, 3000)
    return scene(3.4, (rustle / np.max(np.abs(rustle)), 0, .5), (take('grass/call', cut=1500), 1.7, .8))


# --------------------------------------------------------------------------------------------- rooms heard as their own sounds
def own(*parts, length=3.4):
    """A room's own sounds through its door: each (file, when it starts, how loud, what it is cut above, where in the file it
    begins, how much of it)."""
    def make():
        return scene(length, *((take(rel, begin, span, cut), at, gain) for rel, at, gain, cut, begin, span in
                               ((p + (1100, 0.0, None)[len(p) - 3:]) for p in parts)))
    return make


MAKERS = {
    'red_room': red_room,
    'ted_caver': own(('caver/chisel', .2, 1), ('caver/chisel', 2.0, .6, 1100, 0, 1.2)),
    'zampano_courtyard': pen,
    'whale': clocks,
    'barn_well': own(('well/breath', 0, 1, 900, 0, 3.4)),
    'plain': dry_wind,
    'hospital': own(('ward/hum', 0, .3, 700, 0, 3.0), ('ward/pulse', .3, 1, 1800), ('ward/pulse', 1.4, 1, 1800), ('ward/pulse', 2.5, 1, 1800)),
    'karen_room': own(('movie/projector', 0, 1, 2600, 0, 3.4)),
    'holloway_camp': fire,
    'seance': own(('literary/ghost_scrape', .15, 1), ('literary/ghost_low', 1.7, .8)),
    'wallpaper_nursery': paper,
    'hill_nursery': pounding,
    'miniatures': small_saw,
    'masque': masque,
    'usher': thunder,
    'winchester': own(('literary/winchester_hammer', 0, 1, 1300), ('literary/winchester_saw', 1.6, .8, 1300, 0, 1.8)),
    'child_room': own(('literary/nursery_music_box', 0, 1, 2200, 0, 3.4)),
    'crimson_hall': own(('literary/cylinder_one', 0, 1, 1000, .3, 3.4)),
    'bly_route': wet_steps,
    'elk_lot': own(('literary/lot_highway', 0, .7, 700, .6, 3.4), ('literary/lot_pool', .9, .8, 1800)),
    'elk_fan': fan,
    'mapping_interior': dancer,
    'holy_rabbit': snow_wind,
    'confession': murmur,
    'elk_carcasses': flies,
    'costume_night': own(('costume/megaphone', .1, .8, 1400), ('costume/cheer', .9, 1, 1400), length=3.5),
    'movie_night': own(('movie/calm', 0, 1, 1000, 1.0, 3.4)),
    'winter_lake': own(('winter/groan', 0, 1, 1500), ('winter/crack', 2.4, .6, 1500, 0, .9)),
    'camp_blood': camp,
    'devils_rock': pages,
    'wheel': doors,
    'ghosts_set': slate,
    'end_world_cabin': own(('literary/cabin_wind', 0, .6, 800, 0, 3.4), ('literary/cabin_knock', 1.2, 1, 900)),
    'family_copy': own(('literary/home_handle', .3, 1, 1400), ('literary/home_step', 2.2, .7, 1400), ('literary/home_step', 2.75, .5, 1400, 0, .6)),
    'old_cabin': rocker,
    'three_fingers': own(('literary/paw_hiss', .2, .8, 1500), ('literary/paw_scratch', 1.5, 1, 1500)),
    'nightclub': own(('nightclub/beat', 0, 1, 300, 0, 3.4)),
    'lake_house': own(('lake_house/compressor', 0, 1, 1200), ('lake_house/compressor', 1.0, 1, 1200), ('lake_house/hiss', 2.0, .6, 1200)),
    'borrasca': own(('borrasca/grinder', 0, 1, 900, 2.0, 3.4)),
    'painted_interior': own(('silo/groan', .2, 1, 1400), ('hall/settle', 1.6, .7, 1400, 0, 1.7)),
    'second_waitress': plates,
    'tunnel_periphery': own(('literary/tunnel_traffic', 0, 1, 500, 2.0, 3.4)),
    'missing_sleep': feeds,
    'layered_man': own(('literary/cellar_flesh_tear_2', .2, 1, 1500), ('literary/cellar_flesh_tear_5', 1.8, .8, 1500)),
    'motion_light': own(('foyer/buzzer', .3, 1, 2500), length=2.6),
    'backseat': own(('backseat/idle', 0, .6, 900, 0, 3.4), ('backseat/wiper', .5, 1, 900)),
    'silo': own(('silo/grain', .2, .8, 2500), ('silo/grain', .9, 1, 2500), ('silo/grain', 1.7, .6, 2500), ('silo/grain', 2.3, .9, 2500), length=3.1),
    'dare_in_the_dark': own(('costume/giggle', .2, 1, 1800), ('pell/drip', 2.1, .7, 1800)),
    'way_out': birdsong,
    'feet_up': own(('feet_up/count', .3, 1, 1500), ('feet_up/count', 1.1, .9, 1500), ('feet_up/count', 1.9, 1, 1500), ('feet_up/count', 2.7, .8, 1500), length=3.3),
    'whistlers': own(('lookout/gust', 0, .5, 900), ('lookout/whistle', 1.5, 1, 2400), length=3.2),
    'work_print': own(('work_print/insert', 0, .8, 2500), ('work_print/deck', 1.0, 1, 2500)),
    'polaroid_trail': own(('polaroid/charge', .2, .8, 3000), ('polaroid/click', 1.9, 1, 3000), length=3.0),
    'left_right': own(('left_right/wagon', 0, .4, 700, 0, 2.4), ('left_right/cb', .3, 1, 1800), ('left_right/cb', 2.0, .8, 1800)),
    'dead_air': own(('dead_air/dead', .2, 1, 1600), ('dead_air/dead', 1.7, 1, 1600), length=3.4),
    'pitt': own(('pitt/draft', 0, 1, 800, 0, 3.4)),
    'closing_hall': own(('horror/closing_grind', .2, 1, 700), length=3.0),
    'storeroom': own(('horror/storeroom_flicker', .3, .8, 1500), ('horror/storeroom_scrape', 1.7, 1, 1100), length=3.3),
    'stillness': own(('growl/far1', 0, .5, 500, 0, 3.2), ('horror/stillness_sniff', .6, 1, 900), length=3.2),
    'line': own(('horror/line_creak', .2, 1, 1500), ('horror/line_twang', 2.0, .9, 1500), length=3.3),
    'own_light': own(('dark_hall/steps', .3, 1, 1800), ('dark_hall/steps', .42, .55, 1100)),
    'polybius': arcade,
    'replacement': claws,
    'last_swimmer': lengths,
    'vacant_possession': tape_roll,
    'people_lights': own(('people/wheels_wood', .2, 1, 1200), ('people/wheels_wood', 1.9, .8, 1200)),
    'place_for_you': own(('pell/kettle', 0, 1, 2500, .6, 3.4)),
    'second_person': own(('town/dryers', 0, 1, 1500), length=3.0),
    'deer_road': own(('grass/hazard', .2, 1, 2200), ('grass/hazard', 1.2, 1, 2200), ('grass/hazard', 2.2, 1, 2200), length=3.1),
    'state_route': own(('route/pass', .2, 1, 1100), ('route/pass', 1.9, .8, 1100, 0, 1.5)),
    'marsh_supper': own(('marsh/bell', 0, 1, 2500), length=3.2),
    'tall_grass': tall_grass,
    'growing_things': own(('growing/shoots', .1, 1, 2200), ('growing/boards', 2.0, .6, 1200, 0, 1.4)),
    'last_crossing': own(('crossing/pump', 0, 1, 2000, 0, 3.4), ('crossing/ratchet', 2.2, .6, 2400)),
    'pinehook': own(('corner/jar', .2, .8, 2600), ('corner/jar', .75, .6, 2600), ('town/laugh', 1.4, 1, 1400)),
    'the_drawing': own(('drawing/murmur', 0, 1, 900, 0, 3.4), ('drawing/slip', 2.0, .5, 3000)),
    'corner_house': own(('corner/cicadas', 0, .3, 4500, 0, 3.4), ('corner/gate', 1.2, 1, 1800)),
    'black_city': own(('city/piping', 0, 1, 2500), length=3.4),
    'kept': own(('kept/hum', 0, 1, 1800, 0, 3.4), ('kept/wake', 1.9, .5, 2400)),
    'last_orders': own(('darts/throw', .3, .4, 2000), ('darts/board', .5, 1, 1500), ('darts/throw', 1.3, .4, 2000), ('darts/board', 1.5, .9, 1500),
                       ('darts/throw', 2.3, .4, 2000), ('darts/board', 2.5, 1, 1500), length=3.1),
    'bike_night': own(('bike/card', .2, .7, 2500), ('bike/card', .55, .9, 2500), ('bike/card', .85, 1, 2500), ('bike/card', 1.15, 1, 2500),
                      ('bike/card', 1.5, .8, 2500), ('bike/freewheel', 2.1, .6, 2500), ('bike/freewheel', 2.6, .5, 2500), length=3.3),
    'pell_house': own(('pell/brush', .2, 1, 2000), ('pell/brush', 1.9, .85, 2000, 0, 1.4)),
    'ones_inside': own(('literary/scratch', .3, 1, 1300), ('hall/settle', 2.2, .5, 800, 0, 1.0), length=3.3),
}


def finished(x):
    """Its ends faded, and as loud as the rest: what is heard most of the time brought to one level, and nothing over the top."""
    x = band(x - np.mean(x), 70, None) * adsr(len(x), .01, .08)        # nothing below what a speaker gives back
    mid = band(x, 180, 5000)                                            # where an ear takes its loudness from
    heard = mid[np.abs(mid) > np.max(np.abs(mid)) * .05]
    level = np.sqrt(np.mean(heard ** 2)) if len(heard) else 1
    return x * min(.8 / max(1e-9, np.max(np.abs(x))), .1 / max(1e-9, level))


def update(path, key, value):
    original = path.read_text()
    data = json.loads(original)
    data[key] = value
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + ('\n' if original.endswith('\n') else ''))


def main():
    global rng
    assert set(MAKERS) == set(ROWS) == java_sounds(), (set(MAKERS) ^ set(ROWS), set(ROWS) ^ java_sounds())
    folder = ASSETS / 'sounds/bleed'
    folder.mkdir(parents=True, exist_ok=True)
    made = 0
    for name, subtitle in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            rng = np.random.default_rng(zlib.crc32(name.encode()))       # each sound the same whichever others are made with it
            sound = finished(MAKERS[name]())
            assert .4 < len(sound) / RATE < 3.7, (name, len(sound) / RATE)   # a door is heard again every four seconds
            soundfile.write(str(target), sound.astype(np.float32), RATE, format='OGG', subtype='VORBIS')
            made += 1
        update(ASSETS / 'sounds.json', f'bleed.{name}',
               {'sounds': [{'name': f'the_oldest_house:bleed/{name}', 'attenuation_distance': REACH}], 'subtitle': f'subtitles.the_oldest_house.bleed.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.bleed.{name}', subtitle)
    print(f"The doors' bleeds: {len(ROWS)} subtitled sounds, {made} made now.")


if __name__ == '__main__':
    main()
