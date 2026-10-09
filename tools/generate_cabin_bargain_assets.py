#!/usr/bin/env python3
"""The cabin bargain (0.4.51): original synthesized cues, the two snow globes, the given arm and the stump's bandage.

No samples, recordings or words. Every cue is mono and gets an English subtitle. Run from the repository root:
    python3 tools/generate_cabin_bargain_assets.py
"""
from pathlib import Path
import json, math, random
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
RATE = 22050


# ---------------------------------------------------------------------------------------------------- audio
def seconds(s):
    return np.arange(int(RATE * s)) / RATE


def band(signal, low, high):
    """Brick-wall band filter in the frequency domain, with soft shoulders."""
    spectrum = np.fft.rfft(signal)
    f = np.fft.rfftfreq(len(signal), 1 / RATE)
    gain = 1 / (1 + (f / high) ** 6) if high else np.ones_like(f)
    if low:
        gain = gain / (1 + (low / np.maximum(f, 1e-3)) ** 4)
    return np.fft.irfft(spectrum * gain, n=len(signal))


def env(t, start, attack, decay):
    age = t - start
    return np.where(age < 0, 0, np.minimum(1, age / max(attack, 1e-4)) * np.exp(-decay * np.maximum(age, 0)))


def write(name, signal, peak=.8):
    fade = np.minimum(1, np.arange(len(signal)) / (RATE * .01)) * np.minimum(1, np.arange(len(signal))[::-1] / (RATE * .06))
    signal = signal * fade
    signal = signal / max(1e-6, np.max(np.abs(signal))) * peak
    out = A / 'sounds/literary' / (name + '.ogg')
    out.parent.mkdir(parents=True, exist_ok=True)
    import soundfile
    soundfile.write(str(out), signal.astype(np.float32), RATE, format='OGG', subtype='VORBIS')
    return len(signal) / RATE


def knock(rng):
    """Three knuckle raps on a solid wooden door, the third a little harder."""
    t = seconds(1.5)
    out = np.zeros_like(t)
    for start, strength in ((.05, .8), (.38, .85), (.71, 1.0)):
        burst = band(rng.uniform(-1, 1, len(t)), 120, 1400) * env(t, start, .002, 60)
        body = np.sin(2 * np.pi * 118 * (t - start)) * env(t, start, .003, 28) + .5 * np.sin(2 * np.pi * 231 * (t - start)) * env(t, start, .002, 45)
        out += strength * (burst * .6 + body)
    return out


def heart(rng):
    """A heartbeat that stumbles: lub-dub, lub-dub, a beat that does not come, then a heavy, wet catch."""
    t = seconds(2.6)
    out = np.zeros_like(t)
    for start, strength, pitch, decay in ((.05, 1, 58, 22), (.24, .65, 72, 30), (.75, 1, 58, 22), (.94, .65, 72, 30), (2.0, 1.3, 50, 14), (2.17, .5, 64, 26)):
        a = t - start
        phase = 2 * np.pi * (pitch * a - 24 * a * a)
        out += strength * env(t, start, .008, decay) * (np.sin(phase) + .25 * np.sin(2.07 * phase))
    squelch = band(rng.uniform(-1, 1, len(t)), 200, 1100) * env(t, 2.0, .02, 7) * (.5 + .5 * np.sin(2 * np.pi * 19 * t))
    return out + .45 * squelch


def cord(rng):
    """A cord pulled tight above the elbow: fibres creaking under friction, then the knot cinched."""
    t = seconds(1.6)
    stick = (np.sin(2 * np.pi * (31 + 22 * t) * t) > .55).astype(float)
    creak = band(rng.uniform(-1, 1, len(t)) * stick, 500, 3400) * np.clip(t / .9, 0, 1) * (t < 1.15)
    groan = np.sin(2 * np.pi * (180 + 90 * t) * t) * .35 * np.exp(-((t - .7) / .45) ** 2)
    snap = band(rng.uniform(-1, 1, len(t)), 300, 2600) * env(t, 1.18, .002, 40) * 1.4
    thump = np.sin(2 * np.pi * 95 * (t - 1.18)) * env(t, 1.18, .003, 30)
    return creak * .8 + groan + snap + thump


def chop(rng):
    """The blow: a hatchet through flesh and bone into the boards. Wet impact, a crack, a heavy thud, then a wet slide."""
    t = seconds(1.9)
    impact = band(rng.uniform(-1, 1, len(t)), 900, 7000) * env(t, .02, .0005, 90)
    meat = band(rng.uniform(-1, 1, len(t)), 150, 900) * env(t, .02, .002, 18) * (1 + .6 * np.sin(2 * np.pi * 33 * t))
    clicks = np.zeros_like(t)
    for k in range(7):
        start = .04 + k * .011 + rng.uniform(0, .004)
        clicks += band(rng.uniform(-1, 1, len(t)), 1800, 6000) * env(t, start, .0003, 260) * rng.uniform(.5, 1)
    thud = np.sin(2 * np.pi * (68 - 18 * t) * (t - .06)) * env(t, .06, .002, 9) * 1.3
    slide = band(rng.uniform(-1, 1, len(t)), 250, 1500) * np.exp(-((t - .95) / .35) ** 2) * (.5 + .5 * np.sin(2 * np.pi * (11 + 5 * t) * t)) * .55
    return impact * .9 + meat * .8 + clicks * 1.1 + thud + slide


def wet(rng):
    """After: wet cloth pressed down, a slow gurgle and drips onto the floorboards."""
    t = seconds(2.8)
    press = band(rng.uniform(-1, 1, len(t)), 180, 1300) * (.5 + .5 * np.sin(2 * np.pi * (3.2 + .8 * np.sin(t * 2)) * t)) ** 3 * np.exp(-t / 1.4)
    gurgle = np.zeros_like(t)
    for k in range(9):
        start = rng.uniform(.1, 2.2)
        f = rng.uniform(260, 520)
        a = t - start
        gurgle += np.sin(2 * np.pi * (f * a + 900 * a * a)) * env(t, start, .004, 35) * .4
    drips = np.zeros_like(t)
    for start in (.6, 1.15, 1.6, 1.95, 2.3, 2.6):
        a = t - start
        drips += np.sin(2 * np.pi * (1100 * a - 2600 * a * a)) * env(t, start, .001, 70) * .5
    return press + gurgle + drips


def ring(rng):
    """The ringing after a shock: a thin high tone that beats against itself and fades."""
    t = seconds(3.4)
    tone = np.sin(2 * np.pi * 3950 * t) + .7 * np.sin(2 * np.pi * 3957 * t) + .15 * np.sin(2 * np.pi * 7900 * t)
    return tone * np.minimum(1, t / .3) * np.exp(-t / 1.6) * .6


def wind(rng):
    """Storm around a small wooden building: gusting wind and rain drumming on a roof."""
    t = seconds(5.0)
    gust = .55 + .45 * np.sin(2 * np.pi * .23 * t + 1) * np.sin(2 * np.pi * .11 * t + 2)
    howl = band(rng.uniform(-1, 1, len(t)), 90, 520) * gust
    whistle = np.sin(2 * np.pi * (410 + 60 * np.sin(2 * np.pi * .3 * t)) * t) * .06 * gust ** 3
    rain = band(rng.uniform(-1, 1, len(t)), 1500, 6500) * .35
    patter = np.zeros_like(t)
    for start in rng.uniform(0, 5, 160):
        patter += band(rng.uniform(-1, 1, len(t)), 600, 3000) * env(t, start, .0005, 220) * .25
    return howl + whistle + rain + patter


def globe(rng):
    """A snow globe shaken: water sloshing in glass, flakes ticking, a small glass ring."""
    t = seconds(1.4)
    slosh = band(rng.uniform(-1, 1, len(t)), 200, 1700) * (np.sin(2 * np.pi * 5.5 * t) ** 2) * np.exp(-t / .5)
    tink = sum(np.sin(2 * np.pi * f * (t - s)) * env(t, s, .001, 18) for f, s in ((2637, .05), (3136, .32), (2793, .58)))
    return slosh + tink * .3


def cracked(rng):
    """The cracked globe: a thinner slosh and a dull, broken ring; a drip runs out."""
    t = seconds(1.4)
    slosh = band(rng.uniform(-1, 1, len(t)), 300, 1200) * (np.sin(2 * np.pi * 6.5 * t) ** 2) * np.exp(-t / .3) * .6
    buzz = np.sin(2 * np.pi * 1310 * t) * env(t, .05, .001, 30) * (1 + .8 * np.sign(np.sin(2 * np.pi * 47 * t)))
    a = t - .9
    drip = np.sin(2 * np.pi * (950 * a - 2000 * a * a)) * env(t, .9, .001, 60) * .5
    return slosh + buzz * .25 + drip


CUES = {
    'cabin_knock': (knock, 'Someone knocks at the cabin door'),
    'cabin_heart': (heart, 'Your heartbeat stumbles'),
    'cabin_cord': (cord, 'A cord pulls tight'),
    'cabin_chop': (chop, 'A blade through bone'),
    'cabin_wet': (wet, 'Something wet drips on the boards'),
    'cabin_ring': (ring, 'Your ears ring'),
    'cabin_wind': (wind, 'Rain and wind batter the cabin'),
    'globe_shake': (globe, 'Snow swirls in a glass globe'),
    'globe_cracked': (cracked, 'Water leaks from a cracked globe'),
}


# ---------------------------------------------------------------------------------------------------- art
def save(path, im):
    p = A / path
    p.parent.mkdir(parents=True, exist_ok=True)
    im.save(p, optimize=True)


def textures():
    rng = random.Random(451)
    # Glass with snow caught in the water: mostly clear, pale edge, white flakes. Cracked: a dark break and a dry upper third.
    for name, broken in (('cabin_globe_glass', False), ('cabin_globe_glass_cracked', True)):
        im = Image.new('RGBA', (16, 16), (196, 222, 236, 70))
        d = ImageDraw.Draw(im)
        d.rectangle((0, 0, 15, 15), outline=(230, 244, 250, 130))
        d.point((2, 2), fill=(255, 255, 255, 200)); d.point((3, 2), fill=(255, 255, 255, 160)); d.point((2, 3), fill=(255, 255, 255, 160))
        for _ in range(14):
            x, y = rng.randrange(1, 15), rng.randrange(1 if not broken else 6, 15)
            im.putpixel((x, y), (250, 252, 255, 230))
        if broken:
            for y in range(1, 5):
                for x in range(1, 15):
                    im.putpixel((x, y), (214, 226, 232, 40))
            d.line((10, 0, 9, 3, 11, 5, 8, 9, 9, 12), fill=(44, 52, 58, 220))
            d.line((9, 3, 6, 4), fill=(60, 70, 76, 200))
        save(f'textures/item/{name}.png', im)
    base = Image.new('RGBA', (16, 16), (74, 46, 30, 255))
    d = ImageDraw.Draw(base)
    for y in range(16):
        for x in range(16):
            n = rng.randrange(-6, 7) + int(math.sin(x * .6 + y * .1) * 4)
            base.putpixel((x, y), (max(0, 74 + n), max(0, 46 + n), max(0, 30 + n // 2), 255))
    d.line((0, 3, 15, 3), fill=(112, 82, 46, 255)); d.line((0, 12, 15, 12), fill=(48, 29, 19, 255))
    save('textures/item/cabin_globe_base.png', base)
    # The little world inside: snow ground, a dark cabin with a lit window, a lake strip and a jetty.
    scene = Image.new('RGBA', (16, 16), (236, 240, 244, 255))
    d = ImageDraw.Draw(scene)
    d.rectangle((0, 0, 15, 4), fill=(66, 92, 116, 255)); d.rectangle((6, 0, 7, 4), fill=(110, 84, 56, 255))
    d.rectangle((3, 7, 11, 14), fill=(84, 56, 38, 255)); d.rectangle((2, 6, 12, 7), fill=(52, 36, 28, 255))
    d.rectangle((6, 9, 8, 11), fill=(236, 196, 104, 255)); d.rectangle((9, 12, 10, 14), fill=(40, 28, 22, 255))
    save('textures/item/cabin_globe_scene.png', scene)
    # The given arm: skin with a sleeve hem near the top, a hand at the bottom, the cut end wrapped in stained linen.
    arm = Image.new('RGBA', (16, 16), (196, 160, 128, 255))
    d = ImageDraw.Draw(arm)
    for y in range(16):
        for x in range(16):
            n = rng.randrange(-5, 6)
            arm.putpixel((x, y), (184 + n, 150 + n, 120 + n, 255))
    d.rectangle((0, 0, 15, 3), fill=(222, 214, 196, 255))
    for x in range(0, 16, 3):
        d.point((x, 1), fill=(180, 170, 150, 255))
    d.rectangle((0, 2, 15, 3), fill=(120, 34, 30, 255)); d.point((4, 1), fill=(150, 46, 38, 255)); d.point((11, 0), fill=(150, 46, 38, 255))
    d.line((0, 12, 15, 12), fill=(160, 124, 96, 255)); d.line((4, 13, 4, 15), fill=(150, 116, 90, 255)); d.line((8, 13, 8, 15), fill=(150, 116, 90, 255)); d.line((12, 13, 12, 15), fill=(150, 116, 90, 255))
    save('textures/item/cabin_given_arm.png', arm)
    # The stump's bandage (32x16): wide box at row 0, slim box at row 8; darker and red at the end.
    bandage = Image.new('RGBA', (32, 16), (0, 0, 0, 0))
    for row, w in ((0, 4), (8, 3)):
        width, height = 2 * (4 + w), 4 + 2
        for y in range(height):
            for x in range(width):
                n = rng.randrange(-8, 6)
                cross = 10 if (x + y) % 3 == 0 else 0
                bandage.putpixel((x, row + y), (226 + n - cross, 219 + n - cross, 204 + n - cross, 255))
        # Bottom face (the cut end): soaked through.
        for y in range(4):
            for x in range(4 + w, 4 + 2 * w):
                stain = 1 - abs(y - 1.5) / 3
                bandage.putpixel((x, row + y), (int(140 + 30 * (1 - stain)), int(40 + 40 * (1 - stain)), int(36 + 30 * (1 - stain)), 255))
        # Lower edge of the sides: a brown seep.
        for x in range(width):
            if rng.random() < .7:
                bandage.putpixel((x, row + height - 1), (152, 82, 66, 255))
    save('textures/entity/arm_stump_bandage.png', bandage)


def models():
    tex = {'glass': 'the_oldest_house:item/cabin_globe_glass', 'base': 'the_oldest_house:item/cabin_globe_base',
           'scene': 'the_oldest_house:item/cabin_globe_scene'}
    display = {'gui': {'rotation': [25, -35, 0], 'translation': [0, 0, 0], 'scale': [.9, .9, .9]},
               'ground': {'translation': [0, 2, 0], 'scale': [.5, .5, .5]},
               'fixed': {'translation': [0, 0, 0], 'scale': [.9, .9, .9]},
               'thirdperson_righthand': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [.55, .55, .55]},
               'firstperson_righthand': {'rotation': [0, -35, 10], 'translation': [0, 3, 0], 'scale': [.7, .7, .7]},
               'firstperson_lefthand': {'rotation': [0, -35, 10], 'translation': [0, 3, 0], 'scale': [.7, .7, .7]}}

    def cube(a, b, t, uv=(0, 0, 16, 16)):
        return {'from': a, 'to': b, 'faces': {f: {'texture': '#' + t, 'uv': list(uv)} for f in ('north', 'south', 'east', 'west', 'up', 'down')}}

    def globe(cracked):
        t = dict(tex, particle=tex['base'])
        if cracked:
            t['glass'] = 'the_oldest_house:item/cabin_globe_glass_cracked'
        water = 9.5 if cracked else 13
        els = [cube([3, 0, 3], [13, 3, 13], 'base'), cube([2.5, 0, 2.5], [13.5, 1, 13.5], 'base'),
               cube([5.5, 3, 5.5], [10.5, 3.2, 10.5], 'scene'),
               cube([6.5, 3.2, 7], [9.5, 5.2, 9.5], 'scene', (3, 7, 11, 14)), cube([6.2, 5.2, 6.8], [9.8, 5.8, 9.7], 'scene', (2, 6, 12, 7)),
               cube([4, 3, 4], [12, water, 12], 'glass'), cube([5, water, 5], [11, 14, 11], 'glass')]
        return {'textures': t, 'elements': els, 'display': display, 'render_type': 'minecraft:translucent'}

    def write(path, obj):
        p = A / path
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(obj, indent=2) + '\n')

    write('models/item/cabin_snow_globe.json', globe(False))
    write('models/item/cabin_cracked_snow_globe.json', globe(True))
    arm = {'textures': {'skin': 'the_oldest_house:item/cabin_given_arm', 'particle': 'the_oldest_house:item/cabin_given_arm'},
           'elements': [cube([6.5, 2, 6.5], [9.5, 13, 9.5], 'skin', (0, 2, 16, 12)), cube([6.2, 13, 6.2], [9.8, 15, 9.8], 'skin', (0, 0, 16, 4)),
                        cube([6.4, 0, 6.6], [9.6, 2.2, 9.4], 'skin', (0, 12, 16, 16)), cube([9.3, .8, 7], [10.4, 2.4, 8.4], 'skin', (0, 12, 16, 16))],
           'display': display}
    write('models/item/cabin_given_arm.json', arm)


def registry(lengths):
    sounds = json.loads((A / 'sounds.json').read_text())
    lang = json.loads((A / 'lang/en_us.json').read_text())
    for name, (_, subtitle) in CUES.items():
        key = 'literary.' + name
        sounds[key] = {'subtitle': 'subtitles.the_oldest_house.' + key, 'sounds': [{'name': 'the_oldest_house:literary/' + name, 'stream': False}]}
        lang['subtitles.the_oldest_house.' + key] = subtitle
    lang.update({'item.the_oldest_house.cabin_snow_globe': 'Snow globe',
                 'item.the_oldest_house.cabin_cracked_snow_globe': 'Cracked snow globe',
                 'item.the_oldest_house.cabin_given_arm': 'A given arm'})
    (A / 'sounds.json').write_text(json.dumps(sounds, indent=2) + '\n')
    (A / 'lang/en_us.json').write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    (ROOT / 'art').mkdir(exist_ok=True)
    (ROOT / 'art/cabin_bargain_audio.json').write_text(json.dumps(lengths, indent=2) + '\n')


def main():
    lengths = {}
    for i, (name, (make, _)) in enumerate(CUES.items()):
        lengths[name] = round(write(name, make(np.random.default_rng(4510 + i))), 3)
    textures()
    models()
    registry(lengths)
    print(json.dumps(lengths))


if __name__ == '__main__':
    main()
