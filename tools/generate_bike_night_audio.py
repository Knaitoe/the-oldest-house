"""Original sounds for Bike night's bikes (0.4.463): a bike's tyres letting go of the road for a hop, and both of them coming back
down; the playing card pegged into the Rattler's spokes, and the roar it makes when it takes the bike; the Lamp's dynamo against its
tyre. And the park's (0.4.464): the failing lamp striking and buzzing as it comes back on, and the chain on the gate when a hand
tries it. And the playground's (0.4.465): a swing's hangers as it comes through the bottom of its arc, and a ball of sand packed in
a hand, thrown, and bursting on what it meets; (0.4.466) a body going down the slide; (0.4.467) a hand coming down on a rung
of the monkey bars; (0.4.468) the seesaw's axle as somebody pushes off, and its bumper coming down on the sand; and (0.4.469) the
merry-go-round's bearing as it comes round. And the park's small things (0.4.471): the drinking fountain running, the payphone's
dial tone and its handset going back on the hook, and the spring rider's spring. And the hunt's (0.4.482): a body landing in
dry leaves, and the three laughing as they ride off. And the cars' (0.4.484): an engine going by, a second of it, which the game
plays over and over as a car drives; and four horns, one for each car. And the town's own (0.4.486): the laundromat's dryers, three
seconds of them, played over and over; the neon's one bad letter; a row of the grocery's lights switched off at the box; a full
house laughing in the theater, heard from the street; and eight seconds of a tune on a car radio, which goes round. And (0.4.488) a chain of grocery carts being
wheeled, a second of it, played over while it moves.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one at the
bike. The game plays the card and the dynamo over and over while the bike moves, higher and louder the faster it goes, so each is a
short piece that begins and ends quietly. Run from tools/. Existing files are kept, so the committed sounds only change when deleted
first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'bike/hop': ('bike.hop', 'Bike hops', 12),
    'bike/land': ('bike.land', 'Bike lands', 14),
    'bike/card': ('bike.card', 'Card rattles in spokes', 14),
    'bike/turbo': ('bike.turbo', 'Card roars in spokes', 20),
    'bike/dynamo': ('bike.dynamo', 'Dynamo whines', 8),
    'park/lamp_buzz': ('park.lamp_buzz', 'Lamp buzzes', 12),
    'park/chain': ('park.chain', 'Chain rattles', 12),
    'park/swing_creak': ('park.swing_creak', 'Swing creaks', 12),
    'park/sand_scoop': ('park.sand_scoop', 'Sand is packed', 8),
    'park/sand_throw': ('park.sand_throw', 'Sand ball is thrown', 10),
    'park/sand_burst': ('park.sand_burst', 'Sand ball bursts', 14),
    'park/slide': ('park.slide', 'Someone slides', 14),
    'park/bars': ('park.bars', 'Rung rings', 10),
    'park/seesaw_creak': ('park.seesaw_creak', 'Seesaw creaks', 10),
    'park/seesaw_thud': ('park.seesaw_thud', 'Seesaw bumps down', 12),
    'park/roundabout_squeak': ('park.roundabout_squeak', 'Merry-go-round squeaks', 12),
    'park/fountain': ('park.fountain', 'Fountain runs', 10),
    'park/phone_tone': ('park.phone_tone', 'Dial tone', 8),
    'park/phone_hook': ('park.phone_hook', 'Handset is hung up', 8),
    'park/spring': ('park.spring', 'Spring creaks', 10),
    # 0.4.479, the three who come to the park.
    'bike/freewheel': ('bike.freewheel', 'Freewheel ticks', 12),
    'park/car_pass': ('park.car_pass', 'Car passes', 56),
    'park/padlock': ('park.padlock', 'Padlock comes open', 16),
    'park/cough': ('park.cough', 'Someone coughs', 16),
    'park/sand_kick': ('park.sand_kick', 'Sand is kicked', 12),
    # 0.4.482, the hunt.
    'town/leaves': ('town.leaves', 'Someone lands in leaves', 16),
    'town/laugh': ('town.laugh', 'Laughter', 28),
    # 0.4.484, the cars: an engine going by, and each car's own horn.
    'town/engine': ('town.engine', 'A car goes by', 32),
    'town/horn_wagon': ('town.horn_wagon', "A station wagon's horn", 48),
    'town/horn_pickup': ('town.horn_pickup', "A pickup's horn", 48),
    'town/horn_sedan': ('town.horn_sedan', "A sedan's horn", 48),
    'town/horn_muscle': ('town.horn_muscle', "A fast car's horn", 48),
    # 0.4.486: the town's own.
    # (Each is played louder than one, which is how the game is told to send it further than sixteen blocks; what it reaches is
    # this many blocks times that. So the ones that go on and on are made quiet in themselves: LEVELS.)
    'town/dryers': ('town.dryers', 'Dryers tumble', 16),
    'town/neon': ('town.neon', 'Neon buzzes', 14),
    'town/breaker': ('town.breaker', 'Lights are switched off', 16),
    'town/laughter': ('town.laughter', 'An audience laughs', 16),
    'town/radio': ('town.radio', 'A car radio plays', 16),
    # 0.4.488: the stock boy's chain of carts.
    'town/carts': ('town.carts', 'Carts rattle', 16),
}
# How loud each is in itself, where it is not as loud as a sound can be.
LEVELS = {'town/dryers': .4, 'town/radio': .32, 'town/laughter': .5, 'town/neon': .55, 'town/carts': .5}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for path_name, (event, subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/{path_name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[event] == {'sounds': [{'name': f'the_oldest_house:{path_name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.{event}'}, event
        assert lang[f'subtitles.the_oldest_house.{event}'] == subtitle, event
    print(f'Verified Bike night\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9463)


def click(length=.012, low=1500, high=6500, decay=.002):
    m = seconds(length)
    return band(rng.standard_normal(m), low, high) * np.exp(-times(m) / decay)


def hop():
    # Both tyres let go of the road: grit thrown off them, the frame's own low spring upward, and the chain slapping its stay.
    n = seconds(.2)
    t = times(n)
    grit = band(rng.standard_normal(n), 500, 3500) * np.exp(-t / .018) * .6
    spring = np.sin(2 * np.pi * np.cumsum(120 + 150 * np.clip(t / .08, 0, 1)) / RATE) * np.exp(-t / .045) * .5
    out = grit + spring
    i = seconds(.05)
    c = click(.02, 1800, 5000, .004) * .35
    out[i:i + len(c)] += c
    return out


def land():
    # The back wheel and then the front: two low thumps, the tyres biting, and the chain and a loose mudguard after them.
    n = seconds(.34)
    out = np.zeros(n)
    for at, level in ((0, 1.0), (.045, .8)):
        i = seconds(at)
        m = n - i
        t = times(m)
        thump = np.sin(2 * np.pi * np.cumsum(110 - 45 * np.clip(t / .05, 0, 1)) / RATE) * np.exp(-t / .05) * .8
        bite = band(rng.standard_normal(m), 300, 2200) * np.exp(-t / .025) * .45
        out[i:] += (thump + bite) * level
    for at, level in ((.07, .4), (.11, .25), (.16, .15)):
        i = seconds(at)
        c = click(.03, 2000, 6000, .006) * level
        out[i:i + len(c)] += c
    return out


def card(length=.42, rate0=46, rate1=52, level=.7):
    # A playing card flicked by every spoke that passes: a run of dry clicks, never quite even, each with the card's own papery ring.
    n = seconds(length)
    out = np.zeros(n)
    at = 0.0
    k = 0
    while at < length - .02:
        rate = rate0 + (rate1 - rate0) * at / length
        i = seconds(at)
        c = click(.014, 1400, 6000, .0022) * (level * (.75 + .25 * rng.random()))
        m = seconds(.014)
        tt = times(m)
        ring = np.sin(2 * np.pi * (880 + 60 * rng.random()) * tt) * np.exp(-tt / .004) * .25 * level
        end = min(n, i + m)
        out[i:end] += (c + ring)[:end - i]
        at += (1 / rate) * (.9 + .2 * rng.random())
        k += 1
    edge = seconds(.03)
    out[:edge] *= np.linspace(0, 1, edge)
    out[-edge:] *= np.linspace(1, 0, edge)
    return out


def turbo():
    # The card taking the bike: the clicks run together into a roar that climbs, with the air going past, and falls away.
    length = 1.2
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    at = 0.0
    while at < length - .02:
        rate = 45 + 110 * np.clip(at / .5, 0, 1)
        i = seconds(at)
        c = click(.012, 1200, 7000, .002) * (.5 + .3 * rng.random())
        end = min(n, i + len(c))
        out[i:end] += c[:end - i]
        at += (1 / rate) * (.92 + .16 * rng.random())
    drone = np.sin(2 * np.pi * np.cumsum(90 + 150 * np.clip(t / .5, 0, 1)) / RATE)
    drone = np.sign(drone) * np.abs(drone) ** .5 * .22
    rush = band(rng.standard_normal(n), 700, 4000) * np.clip(t / .4, 0, 1) * .3
    shape = np.clip(t / .05, 0, 1) * np.clip((length - t) / .35, 0, 1)
    return (out + drone + rush) * shape


def dynamo():
    # A bottle dynamo's little wheel against the tyre: a thin whine with a rough edge, and the rubber's hiss under it.
    length = .5
    n = seconds(length)
    t = times(n)
    wobble = 1 + .012 * np.sin(2 * np.pi * 9 * t)
    phase = 2 * np.pi * np.cumsum(430 * wobble) / RATE
    whine = np.sin(phase) * .5 + np.sin(2 * phase) * .22 + np.sin(3 * phase) * .12 + np.sin(5 * phase) * .05
    hiss = band(rng.standard_normal(n), 2500, 7000) * .12
    edge = np.clip(t / .06, 0, 1) * np.clip((length - t) / .06, 0, 1)
    return (whine + hiss) * edge


def lamp_buzz():
    # A discharge lamp that will not stay lit: the starter's tick, the tube catching with a rasp, and the ballast's hum settling under it.
    length = .9
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((0, .7), (.07, .5), (.16, .8)):
        i = seconds(at)
        c = click(.01, 2500, 7500, .0015) * level
        out[i:i + len(c)] += c
    catch = np.clip((t - .16) / .05, 0, 1)
    hum = (np.sin(2 * np.pi * 120 * t) * .5 + np.sin(2 * np.pi * 240 * t) * .25 + np.sin(2 * np.pi * 360 * t) * .14 + np.sin(2 * np.pi * 600 * t) * .06)
    rasp = band(rng.standard_normal(n), 1800, 6000) * (.5 + .5 * np.sign(np.sin(2 * np.pi * 120 * t))) * np.exp(-np.clip(t - .16, 0, None) / .12) * .5
    settle = .35 + .65 * np.exp(-np.clip(t - .16, 0, None) / .25)
    out += (hum * settle * .6 + rasp) * catch * np.clip((length - t) / .2, 0, 1)
    return out


def chain():
    # A hand on the gate: the leaves shift an inch against the chain, the links run and knock on the iron, and the padlock swings back against a bar.
    length = .8
    n = seconds(length)
    out = np.zeros(n)
    knocks = ((0, 1.0, 2100), (.05, .6, 2900), (.09, .75, 2400), (.15, .5, 3300), (.21, .65, 2600), (.3, .35, 3100), (.47, .55, 1500), (.62, .25, 1500))
    for at, level, pitch in knocks:
        i = seconds(at)
        m = n - i
        t = times(m)
        ring = (np.sin(2 * np.pi * pitch * t) + .6 * np.sin(2 * np.pi * pitch * 1.52 * t) + .35 * np.sin(2 * np.pi * pitch * 2.71 * t)) * np.exp(-t / .035)
        hit = band(rng.standard_normal(m), 1200, 7000) * np.exp(-t / .006)
        out[i:] += (ring * .5 + hit * .8) * level
    t = times(n)
    out += np.sin(2 * np.pi * 180 * t) * np.exp(-t / .09) * .3                   # the leaf itself, low, as it takes up the slack
    return out


def swing_creak():
    # A swing's hanger turning in its eye under a weight: a dry squeak that climbs and falls back, and the links settling after it.
    length = .42
    n = seconds(length)
    t = times(n)
    pitch = 880 + 420 * np.sin(np.pi * np.clip(t / .26, 0, 1)) ** 2
    phase = 2 * np.pi * np.cumsum(pitch) / RATE
    squeak = (np.sin(phase) + .45 * np.sin(2 * phase) + .2 * np.sin(3.02 * phase)) * np.sin(np.pi * np.clip(t / .3, 0, 1)) ** 1.5
    grit = band(rng.standard_normal(n), 2500, 7000) * np.sin(np.pi * np.clip(t / .3, 0, 1)) * .25
    out = squeak * (.6 + .4 * np.sign(np.sin(2 * np.pi * 37 * t))) * .5 + grit
    for at, level, ring in ((.3, .35, 2700), (.34, .22, 3400), (.38, .15, 2300)):
        i = seconds(at)
        m = n - i
        u = times(m)
        out[i:] += np.sin(2 * np.pi * ring * u) * np.exp(-u / .02) * level
    return out


def sand_scoop():
    # A hand goes into damp sand and comes up full, and the other hand pats it round: two short gritty swells.
    length = .36
    n = seconds(length)
    t = times(n)
    grit = band(rng.standard_normal(n), 900, 5200)
    scoop = np.sin(np.pi * np.clip(t / .17, 0, 1)) ** 2
    pat = np.exp(-np.clip(t - .22, 0, None) / .025) * (t >= .22)
    low = np.sin(2 * np.pi * 140 * t) * np.exp(-np.clip(t - .22, 0, None) / .03) * (t >= .22)
    return grit * (scoop * .7 + pat * .9) + low * .35


def sand_throw():
    # An arm coming over, and a small heavy thing leaving the hand: a soft rush that is gone at once.
    length = .2
    n = seconds(length)
    t = times(n)
    rush = band(rng.standard_normal(n), 350, 2600) * np.sin(np.pi * np.clip(t / length, 0, 1)) ** 2
    sift = band(rng.standard_normal(n), 3000, 8000) * np.exp(-np.clip(t - .1, 0, None) / .03) * (t >= .1) * .3
    return rush + sift


def sand_burst():
    # A ball of sand meeting something: a dull knock with no ring in it, and the sand going everywhere after.
    length = .5
    n = seconds(length)
    t = times(n)
    knock = np.sin(2 * np.pi * (120 - 60 * np.clip(t / .05, 0, 1)) * t) * np.exp(-t / .03)
    spray = band(rng.standard_normal(n), 1800, 8000) * np.exp(-t / .07)
    out = knock * .9 + spray * .7
    for _ in range(14):
        i = seconds(.06 + rng.random() * .36)
        c = click(.006, 2500, 8000, .0012) * (.1 + rng.random() * .25) * (1 - i / n)
        out[i:i + len(c)] += c[:max(0, n - i)]
    return out


def slide():
    # Cloth on sheet steel, faster all the way down: a hiss that climbs, the sheet drumming under it, and the lip at the end.
    length = 1.25
    n = seconds(length)
    t = times(n)
    go = np.clip(t / 1.05, 0, 1)
    hiss = band(rng.standard_normal(n), 1500, 7000) * (.15 + .85 * go ** 1.5) * np.clip((length - t) / .12, 0, 1)
    drum = band(rng.standard_normal(n), 90, 420) * (.2 + .8 * go) * np.clip((length - t) / .12, 0, 1)
    ring = np.sin(2 * np.pi * (210 + 60 * go) * t) * .12 * go
    out = hiss * .6 + drum * .9 + ring
    i = seconds(1.07)
    c = click(.02, 300, 2500, .006) * .8
    out[i:i + len(c)] += c[:max(0, n - i)]
    return out


def bars():
    # A hand coming down on a steel rung: the slap of the palm, the pipe ringing short and dull after it, and the frame's hum under.
    length = .34
    n = seconds(length)
    t = times(n)
    slap = band(rng.standard_normal(n), 700, 3800) * np.exp(-t / .012) * .9
    ring = (np.sin(2 * np.pi * 640 * t) + .5 * np.sin(2 * np.pi * 1730 * t) + .25 * np.sin(2 * np.pi * 2960 * t)) * np.exp(-t / .07) * .35
    hum = np.sin(2 * np.pi * 172 * t) * np.exp(-t / .12) * .22
    return (slap + ring + hum) * np.clip((length - t) / .04, 0, 1)


def seesaw_creak():
    # A seesaw's axle turning dry under a load as somebody pushes off: a low groan that climbs, with grit in it.
    length = .5
    n = seconds(length)
    t = times(n)
    pitch = 150 + 130 * np.clip(t / .4, 0, 1) ** 1.5
    phase = 2 * np.pi * np.cumsum(pitch) / RATE
    shape = np.sin(np.pi * np.clip(t / length, 0, 1)) ** .8
    groan = (np.sin(phase) + .5 * np.sin(2.01 * phase) + .3 * np.sin(3.03 * phase)) * shape
    grit = band(rng.standard_normal(n), 900, 4200) * shape * (.5 + .5 * np.sign(np.sin(2 * np.pi * 23 * t))) * .22
    return groan * .5 + grit


def seesaw_thud():
    # A half tyre under the end of a board coming down on packed sand: a dull bump, the board's knock after it, and sand.
    length = .4
    n = seconds(length)
    t = times(n)
    bump = np.sin(2 * np.pi * (95 - 40 * np.clip(t / .08, 0, 1)) * t) * np.exp(-t / .05)
    knock = band(rng.standard_normal(n), 250, 1100) * np.exp(-t / .03) * .7
    sand = band(rng.standard_normal(n), 2500, 7000) * np.exp(-np.clip(t - .02, 0, None) / .06) * (t > .02) * .12
    return (bump + knock + sand) * np.clip((length - t) / .05, 0, 1)


def roundabout_squeak():
    # A merry-go-round's dry bearing coming round: two chirps, the second lower, over the deck's rumble.
    length = .46
    n = seconds(length)
    t = times(n)
    out = band(rng.standard_normal(n), 60, 260) * np.sin(np.pi * np.clip(t / length, 0, 1)) * .35
    for at, pitch, level in ((.02, 1500, .5), (.21, 1150, .4)):
        i = seconds(at)
        m = min(n - i, seconds(.15))
        u = times(m)
        sweep = pitch * (1 + .25 * np.sin(np.pi * u / .15))
        phase = 2 * np.pi * np.cumsum(sweep) / RATE
        out[i:i + m] += (np.sin(phase) + .4 * np.sin(2 * phase)) * np.sin(np.pi * u / .15) ** 2 * level
    return out


def fountain():
    # Water thrown from a spout into a steel bowl: a hiss that comes on, its patter in the bowl, and the drain's gurgle as it stops.
    length = 2.4
    n = seconds(length)
    t = times(n)
    on = np.clip(t / .15, 0, 1) * np.clip((length - .35 - t) / .25, 0, 1)
    hiss = band(rng.standard_normal(n), 2500, 8000) * on * .5
    patter = band(rng.standard_normal(n), 500, 2200) * on * (.6 + .4 * np.sign(np.sin(2 * np.pi * 31 * t + 3 * np.sin(2 * np.pi * 3.3 * t)))) * .35
    gurgle = np.sin(2 * np.pi * (180 + 90 * np.sin(2 * np.pi * 9 * t)) * t) * np.clip((t - (length - .6)) / .1, 0, 1) * np.clip((length - t) / .3, 0, 1) * .3
    return hiss + patter + gurgle


def phone_tone():
    # A handset lifted off its hook (a click), and the dial tone in it: two steady notes together, small, from the earpiece.
    length = 3.2
    n = seconds(length)
    t = times(n)
    out = (np.sin(2 * np.pi * 350 * t) + np.sin(2 * np.pi * 440 * t)) * .22 * np.clip((t - .12) / .03, 0, 1) * np.clip((length - t) / .08, 0, 1)
    c = click(.03, 400, 3000, .008) * .8
    out[:len(c)] += c
    return out


def phone_hook():
    # A handset set back on its hook: plastic on steel, twice, and the cradle's spring.
    length = .3
    n = seconds(length)
    t = times(n)
    out = np.sin(2 * np.pi * 900 * t) * np.exp(-t / .03) * .15
    for at, level in ((0, .9), (.07, .5)):
        i = seconds(at)
        c = click(.04, 300, 2600, .01) * level
        out[i:i + len(c)] += c[:n - i]
    return out


def spring():
    # A heavy coil spring flexing under a weight and coming back: a low twang that bends down and up, and the coils' rub.
    length = .5
    n = seconds(length)
    t = times(n)
    pitch = 120 - 35 * np.sin(np.pi * np.clip(t / length, 0, 1))
    phase = 2 * np.pi * np.cumsum(pitch) / RATE
    shape = np.sin(np.pi * np.clip(t / length, 0, 1)) ** .7
    twang = (np.sin(phase) + .5 * np.sin(2.02 * phase) + .35 * np.sin(3.97 * phase)) * shape * .45
    rub = band(rng.standard_normal(n), 1200, 4200) * shape * (.5 + .5 * np.sign(np.sin(2 * np.pi * 41 * t))) * .14
    return twang + rub


def freewheel():
    # A freewheel's pawls under somebody coasting: a run of small clicks, quick and even, that begins and ends quietly.
    length = .5
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for k in range(int(length * 34)):
        i = seconds(k / 34 + .004)
        c = click(.006, 2600, 9000, .0012) * (.5 + .12 * rng.random())
        out[i:i + len(c)] += c[:n - i]
    return out * np.clip(t / .05, 0, 1) * np.clip((length - t) / .08, 0, 1)


def car_pass():
    # A car going by on the street: its engine low and steady, its tyres on the road, coming up, level, and going, a little lower
    # as it goes away.
    length = 4.2
    n = seconds(length)
    t = times(n)
    near = np.exp(-((t - 1.9) / .95) ** 2)
    pitch = 62 * (1.04 - .08 / (1 + np.exp(-(t - 1.9) / .25)))
    phase = 2 * np.pi * np.cumsum(pitch) / RATE
    engine = (np.sin(phase) + .55 * np.sin(2 * phase) + .3 * np.sin(3.01 * phase) + .18 * np.sin(4.5 * phase)) * (.22 + .3 * near)
    tyres = band(rng.standard_normal(n), 300, 2400) * near * .5
    wind = band(rng.standard_normal(n), 90, 500) * near ** .6 * .35
    return (engine + tyres + wind) * np.clip(t / .5, 0, 1) * np.clip((length - t) / .9, 0, 1)


def padlock():
    # A key turned in a padlock, the shackle springing, and the chain pulled through the bars: links running on iron.
    length = 1.5
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((.05, .5), (.2, 1.0)):
        i = seconds(at)
        c = click(.03, 900, 5200, .006) * level
        out[i:i + len(c)] += c[:n - i]
    run = np.clip((t - .38) / .1, 0, 1) * np.clip((length - t) / .35, 0, 1)
    links = band(rng.standard_normal(n), 1800, 7500) * (np.sin(2 * np.pi * 19 * t) > .2) * run * .5
    ring = np.sin(2 * np.pi * 2350 * t) * np.exp(-np.clip(t - .2, 0, None) / .12) * (t > .2) * .2
    return out + links + ring


def cough():
    # Somebody coughing sand out: three short barks, each a breath of noise with a throat under it, the last the smallest.
    length = .95
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for at, level, tone in ((.02, 1.0, 310), (.3, .85, 285), (.6, .6, 260)):
        shape = np.clip((t - at) / .012, 0, 1) * np.exp(-np.clip(t - at, 0, None) / .07) * (t >= at)
        out += (band(rng.standard_normal(n), 500, 3600) * .7 + np.sin(2 * np.pi * tone * t) * .35 + np.sin(2 * np.pi * tone * 2.1 * t) * .15) * shape * level
    return out


def sand_kick():
    # A shoe through dry sand: the scuff, and the spray landing after it.
    length = .55
    n = seconds(length)
    t = times(n)
    scuff = band(rng.standard_normal(n), 400, 2600) * np.clip(t / .02, 0, 1) * np.exp(-t / .07) * .8
    spray = band(rng.standard_normal(n), 2500, 9000) * np.clip((t - .08) / .05, 0, 1) * np.exp(-np.clip(t - .08, 0, None) / .16) * .45
    return scuff + spray


def leaves():
    # Somebody landing in a heap of dry leaves: the body's thump, and the leaves crackling as they settle after it.
    length = .9
    n = seconds(length)
    t = times(n)
    thump = np.sin(2 * np.pi * 72 * t) * np.exp(-t / .05) * .7
    grains = np.zeros(n)
    for _ in range(90):
        at = rng.uniform(0, .6)
        m = seconds(.012)
        i = int(at * RATE)
        if i + m < n:
            grains[i:i + m] += band(rng.standard_normal(m), 2500, 9000) * np.exp(-times(m) / .003) * rng.uniform(.2, .8) * np.exp(-at / .25)
    hiss = band(rng.standard_normal(n), 1800, 7000) * np.clip(t / .01, 0, 1) * np.exp(-t / .18) * .5
    return thump + grains + hiss


def laugh():
    # Three children laughing as they ride off: short voiced barks, each a little lower than the last, from three throats and not together.
    length = 1.7
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for voice, (pitch, start) in enumerate(((330, .0), (270, .07), (390, .13))):
        at = start
        for k in range(7):
            f0 = pitch * (1 - .035 * k) * rng.uniform(.97, 1.03)
            shape = np.clip((t - at) / .01, 0, 1) * np.exp(-np.clip(t - at, 0, None) / .045) * (t >= at) * (t < at + .19)
            tone = np.sin(2 * np.pi * f0 * t) * .5 + np.sin(2 * np.pi * f0 * 2 * t) * .3 + np.sin(2 * np.pi * f0 * 3 * t) * .15
            breath = band(rng.standard_normal(n), 900, 3200) * .25
            out += (tone + breath) * shape * (1 - .08 * k) * (.8 if voice else 1)
            at += .17 + rng.uniform(-.015, .02)
    return out


def engine():
    # A car's engine as it goes by, a second of it: low and even, with its tyres on the road, beginning and ending quietly so that
    # one beat of it runs into the next.
    length = 1.1
    n = seconds(length)
    t = times(n)
    phase = 2 * np.pi * 58 * t
    tone = np.sin(phase) + .5 * np.sin(2 * phase) + .28 * np.sin(3.02 * phase) + .15 * np.sin(4.5 * phase)
    burble = 1 + .18 * np.sin(2 * np.pi * 14.5 * t)
    tyres = band(rng.standard_normal(n), 300, 2200) * .35
    return (tone * .5 * burble + tyres) * np.clip(t / .3, 0, 1) * np.clip((length - t) / .4, 0, 1)


def horn(low, high, length, rasp=0.0, twice=False):
    # A car's horn: two reeds a third or so apart, each with its overtones; some rasp for an old one; one long press, or two short.
    n = seconds(length)
    t = times(n)

    def press(start, held):
        shape = np.clip((t - start) / .015, 0, 1) * np.clip((start + held - t) / .04, 0, 1) * (t >= start) * (t <= start + held)
        out = np.zeros(n)
        for f, level in ((low, 1.0), (high, .85)):
            ph = 2 * np.pi * f * t
            out += (np.sin(ph) + .5 * np.sin(2 * ph) + .33 * np.sin(3 * ph) + .2 * np.sin(4 * ph) + rasp * np.sign(np.sin(ph))) * level
        return out * shape
    return (press(.02, .2) + press(.32, length - .38) if twice else press(.02, length - .06)) * .5


def dryers():
    # Dryers turning behind glass, three seconds of them: the motors' hum, the drums' rumble, and what is in them falling once a
    # turn, two machines a little out of step. It begins and ends quietly, so that one playing of it runs into the next.
    length = 3.0
    n = seconds(length)
    t = times(n)
    hum = np.sin(2 * np.pi * 100 * t) * .3 + np.sin(2 * np.pi * 200 * t) * .1
    rumble = band(rng.standard_normal(n), 60, 260) * (.6 + .4 * np.sin(2 * np.pi * .9 * t)) * .8
    out = hum + rumble
    for period, first in ((.92, .1), (1.07, .45)):
        at = first
        while at < length - .2:
            i = seconds(at)
            m = seconds(.14)
            thump = band(rng.standard_normal(m), 90, 500) * np.exp(-times(m) / .04) * .9
            out[i:i + m] += thump
            if rng.random() < .5:                           # a zip or a button on the drum
                c = click(.012, 2500, 6000, .002) * .3
                j = i + seconds(.05)
                out[j:j + len(c)] += c
            at += period
    return out * np.clip(t / .35, 0, 1) * np.clip((length - t) / .35, 0, 1)


def neon():
    # A tube that will not stay lit: a short hard buzz at twice the mains with a crackle on it, and a tick as it lets go.
    length = .5
    n = seconds(length)
    t = times(n)
    gate = .5 + .5 * np.sign(np.sin(2 * np.pi * 120 * t))
    hum = np.sign(np.sin(2 * np.pi * 120 * t)) * .22 + np.sin(2 * np.pi * 240 * t) * .3 + np.sin(2 * np.pi * 480 * t) * .14
    crackle = band(rng.standard_normal(n), 3000, 9000) * gate * .35
    out = (hum + crackle) * np.clip(t / .008, 0, 1) * np.clip((length - .06 - t) / .05, 0, 1)
    c = click(.01, 2500, 7500, .0015) * .6
    i = seconds(length - .05)
    out[i:i + len(c)] += c
    return out


def breaker():
    # A row of lights switched off at the box: the handle going over with a clack, and the ballasts dying under it.
    length = .9
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((0, 1.0), (.035, .5)):
        c = click(.03, 400, 3500, .006) * level
        i = seconds(at)
        out[i:i + len(c)] += c
    f = 120 * (1 - .12 * np.clip(t / length, 0, 1))
    phase = 2 * np.pi * np.cumsum(f) / RATE
    out += (np.sin(phase) * .3 + np.sin(2 * phase) * .14) * np.exp(-t / .22) * np.clip(t / .02, 0, 1)
    return out


def laughter():
    # A full house laughing at the same thing, heard through the theater's doors: a great many voices, none of them clear, a swell
    # and a falling away.
    length = 2.6
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for voice in range(26):
        pitch = rng.uniform(150, 420)
        at = rng.uniform(.0, .5)
        for k in range(int(rng.integers(4, 8))):
            f0 = pitch * (1 - .03 * k) * rng.uniform(.97, 1.03)
            shape = np.clip((t - at) / .012, 0, 1) * np.exp(-np.clip(t - at, 0, None) / .05) * (t >= at) * (t < at + .2)
            out += (np.sin(2 * np.pi * f0 * t) * .5 + np.sin(2 * np.pi * f0 * 2 * t) * .3 + np.sin(2 * np.pi * f0 * 3 * t) * .12) * shape * (1 - .07 * k)
            at += rng.uniform(.15, .24)
    room = band(rng.standard_normal(n), 300, 1500) * .5 * np.clip(t / .4, 0, 1) * np.exp(-np.clip(t - .7, 0, None) / .8)
    return band(out + room * np.max(np.abs(out)) * .25, 140, 1100) * np.clip(t / .3, 0, 1) * np.clip((length - t) / .5, 0, 1)


def radio():
    # A car radio a street away, eight seconds of a tune nobody could name: a bass on the beat, an organ picking out four chords
    # over it, a tick on the off-beats, all of it through a three-inch speaker. It ends as it began, so it goes round.
    beat, bars = .5, 4
    length = beat * 4 * bars
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    chords = ((110.0, (220.0, 261.63, 329.63)), (87.31, (174.61, 220.0, 261.63)), (130.81, (261.63, 329.63, 392.0)), (98.0, (196.0, 246.94, 293.66)))
    order = (0, 1, 2, 1, 0, 2, 1, 2)

    def note(f, at, held, level, organ):
        shape = np.clip((t - at) / .008, 0, 1) * (np.exp(-np.clip(t - at, 0, None) / (held * .9)) if not organ else np.clip((at + held - t) / .03, 0, 1)) * (t >= at) * (t < at + held)
        ph = 2 * np.pi * f * t
        return (np.sin(ph) + (.5 * np.sin(2 * ph) + .3 * np.sin(3 * ph) + .12 * np.sin(4 * ph) if organ else .3 * np.sin(2 * ph))) * shape * level
    for bar in range(bars):
        root, tones = chords[bar]
        for b in range(4):
            at = (bar * 4 + b) * beat
            out += note(root, at, beat * .9, .8 if b in (0, 2) else .5, False)
            for half in range(2):
                out += note(tones[order[b * 2 + half]] * 2, at + half * beat / 2, beat * .42, .32, True)
            c = click(.02, 5000, 9000, .004) * .25
            i = seconds(at + beat / 2)
            out[i:i + len(c)] += c
    out = np.tanh(band(out, 380, 3000) * 2.2)
    return out * np.clip(t / .12, 0, 1) * np.clip((length - t) / .12, 0, 1)


def carts():
    # A chain of grocery carts being wheeled: wire baskets shaking against one another, and castors on tarmac. A second of it,
    # beginning and ending quietly so that one playing of it runs into the next.
    length = 1.0
    n = seconds(length)
    t = times(n)
    out = band(rng.standard_normal(n), 180, 900) * .25 * (.7 + .3 * np.sin(2 * np.pi * 7 * t))
    for _ in range(46):
        c = click(rng.uniform(.006, .02), rng.uniform(2200, 3500), rng.uniform(6000, 9000), rng.uniform(.0015, .004)) * rng.uniform(.25, .9)
        i = int(rng.uniform(0, n - len(c)))
        out[i:i + len(c)] += c
    return out * np.clip(t / .15, 0, 1) * np.clip((length - t) / .15, 0, 1)


MAKERS = {'town/carts': carts, 'town/dryers': dryers, 'town/neon': neon, 'town/breaker': breaker, 'town/laughter': laughter, 'town/radio': radio,
          'town/engine': engine, 'town/horn_wagon': lambda: horn(349, 440, .7), 'town/horn_pickup': lambda: horn(294, 311, .8, .25, True),
          'town/horn_sedan': lambda: horn(392, 494, .65), 'town/horn_muscle': lambda: horn(466, 587, .95, .15),
          'town/leaves': leaves, 'town/laugh': laugh, 'bike/freewheel': freewheel, 'park/car_pass': car_pass, 'park/padlock': padlock, 'park/cough': cough, 'park/sand_kick': sand_kick, 'bike/hop': hop, 'bike/land': land, 'bike/card': card, 'bike/turbo': turbo, 'bike/dynamo': dynamo, 'park/lamp_buzz': lamp_buzz, 'park/chain': chain,
          'park/swing_creak': swing_creak, 'park/sand_scoop': sand_scoop, 'park/sand_throw': sand_throw, 'park/sand_burst': sand_burst, 'park/slide': slide, 'park/bars': bars, 'park/seesaw_creak': seesaw_creak, 'park/seesaw_thud': seesaw_thud, 'park/roundabout_squeak': roundabout_squeak, 'park/fountain': fountain, 'park/phone_tone': phone_tone, 'park/phone_hook': phone_hook,
          'park/spring': spring}


def main():
    for path_name, (event, subtitle, reach) in ROWS.items():
        target = ASSETS / f'sounds/{path_name}.ogg'
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[path_name](), LEVELS.get(path_name, .8)).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', event,
               {'sounds': [{'name': f'the_oldest_house:{path_name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.{event}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.{event}', subtitle)


if __name__ == '__main__':
    main()
