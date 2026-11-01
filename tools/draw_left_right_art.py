"""The Left-Right Game (0.4.205): the road past the Bristol Ferry toll, the Bluejay cut, Jubilation, the sinkhole and the salt.

Draws, in native Minecraft pixels from fixed seeds: ten materials (wet asphalt, its double yellow and white edge lines, cracked
salt, brutalist concrete, four pastel stuccos, roof shingle); every fitting of the left_right_fitting block, its models facing
north and turned by its facing (the teletype, the CB quiet or live, the convoy log, the flare crate, a Jersey barrier, the
gantry's beam and its signs in four sectors, a sodium lamp lit or out, a fluorescent tube on or off, the raised drop-arm, a rusted
sedan in four paints, the payphone with the water at four heights, the key hook full or empty, the chain-link gate shut or open,
a dormer window dark, lit or with someone behind its curtain, a bent street sign, the wagon with its hazards off or on, its
tailgate shut or down, Marjorie's lower and upper halves, a burning flare, a downed power pole, reeds and crushed cars); and the
road flare and the conduit key as items. Writes the models and block states with them. --check verifies everything already
written.

Palette, from the owner's specification: sodium amber #E09F3E, forest #1B2A26, road fog #2A2621, salt #D8DBD6, flare crimson
#FF2200, Alice's wagon green.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/left_right/'
AMBER, CRIMSON, SALT, YELLOW = '#e09f3e', '#ff2200', '#d8dbd6', '#d9b23a'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(size=(16, 16), base=None, seed=0, spread=5):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, seed=0, spread=0, a=255):
    rnd = random.Random(seed); px = im.load()
    for y in range(max(0, y0), min(im.size[1], y1)):
        for x in range(max(0, x0), min(im.size[0], x1)):
            px[x, y] = shade(colour, rnd.randint(-spread, spread) if spread else 0, a)


def line(im, x0, y0, x1, y1, colour, a=255):
    px = im.load(); n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        x, y = round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n)
        if 0 <= x < im.size[0] and 0 <= y < im.size[1]:
            px[x, y] = shade(colour, 0, a)


def frame_rect(im, x0, y0, x1, y1, colour):
    line(im, x0, y0, x1 - 1, y0, colour); line(im, x0, y1 - 1, x1 - 1, y1 - 1, colour)
    line(im, x0, y0, x0, y1 - 1, colour); line(im, x1 - 1, y0, x1 - 1, y1 - 1, colour)


# ---------------------------------------------------------------------------------------------------- materials
def asphalt(seed=11):
    """Wet asphalt: dark, grained, with a few lighter aggregate stones and a sheen of water in the low places."""
    im = new(base='#2e2e30', seed=seed, spread=6); px = im.load(); rnd = random.Random(seed + 1)
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade('#55555a', rnd.randint(-8, 8))
    for _ in range(3):
        x, y = rnd.randrange(14), rnd.randrange(16)
        for dx in range(rnd.randint(2, 4)):
            if x + dx < 16:
                px[x + dx, y] = shade('#3e4248', 4)
    return im


def asphalt_line():
    im = asphalt(13); fill(im, 4, 0, 7, 16, YELLOW, 14, 10); fill(im, 9, 0, 12, 16, YELLOW, 15, 10)
    px = im.load(); rnd = random.Random(16)
    for _ in range(10):
        x, y = rnd.choice((4, 5, 6, 9, 10, 11)), rnd.randrange(16); px[x, y] = shade('#2e2e30', rnd.randint(-4, 4))
    return im


def asphalt_edge():
    im = asphalt(17); fill(im, 6, 0, 10, 16, '#d6d6d0', 18, 8); px = im.load(); rnd = random.Random(19)
    for _ in range(8):
        x, y = rnd.randint(6, 9), rnd.randrange(16); px[x, y] = shade('#2e2e30', rnd.randint(-4, 4))
    return im


def salt():
    """The salt flat: bleached white, a net of grey cracks."""
    im = new(base=SALT, seed=21, spread=4); rnd = random.Random(22)
    x, y = 0, rnd.randrange(16)
    for _ in range(3):
        x0, y0 = rnd.randrange(16), rnd.randrange(16)
        for _ in range(9):
            x1, y1 = x0 + rnd.randint(-3, 3), y0 + rnd.randint(-3, 3); line(im, x0, y0, x1, y1, '#a9aca6'); x0, y0 = x1, y1
    return im


def concrete():
    """Brutalist concrete: board-formed lines, water stains running down."""
    im = new(base='#8d8a84', seed=31, spread=4); px = im.load(); rnd = random.Random(32)
    for y in (3, 8, 13):
        for x in range(16):
            px[x, y] = shade(px[x, y], -14)
    for x in (2, 11):
        for y in range(rnd.randint(5, 16)):
            px[x, y] = shade(px[x, y], -18)
    return im


def stucco(base, seed):
    im = new(base=base, seed=seed, spread=5); px = im.load(); rnd = random.Random(seed + 1)
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade(px[x, y], rnd.choice((-12, 10)))
    return im


def shingle():
    im = new(base='#4a4448', seed=51, spread=4); px = im.load()
    for y in range(0, 16, 4):
        for x in range(16):
            px[x, y] = shade(px[x, y], -16)
        off = 0 if (y // 4) % 2 == 0 else 2
        for x in range(off, 16, 4):
            for yy in range(y, min(16, y + 4)):
                px[x, yy] = shade(px[x, yy], -12)
    return im


def materials():
    return {'asphalt': asphalt(), 'asphalt_line': asphalt_line(), 'asphalt_edge': asphalt_edge(), 'salt': salt(), 'concrete': concrete(),
            'pastel_pink': stucco('#e8b4b8', 41), 'pastel_mint': stucco('#b6dcc4', 43), 'pastel_blue': stucco('#b4c8e4', 45),
            'pastel_butter': stucco('#efe0a6', 47), 'shingle': shingle()}


# ---------------------------------------------------------------------------------------------------- fittings
def tty():
    im = new(base='#5c5a52', seed=61, spread=3); fill(im, 2, 9, 14, 15, '#3a3834'); px = im.load()
    for x in range(3, 13, 2):
        for y in (10, 12):
            px[x, y] = (210, 205, 190, 255)
    fill(im, 4, 1, 12, 7, '#ece6d4', 62, 4)
    for y in (2, 4):
        line(im, 5, y, 10, y, '#4a4a4a')
    return im


def radio(live):
    im = new(base='#3a3a36', seed=71, spread=3); fill(im, 2, 4, 9, 11, '#0c140c')
    if live:
        for x in range(2, 9):
            import math
            y = 7 + round(2 * math.sin(x * 1.3)); im.load()[x, y] = (60, 255, 90, 255)
    else:
        line(im, 2, 7, 8, 7, '#2a5a2a')
    for y in (5, 9):
        im.load()[12, y] = (180, 180, 170, 255)
    fill(im, 11, 12, 14, 14, '#8a1a1a' if live else '#3a1010')
    return im


def logbook():
    im = new(base='#6a4a2a', seed=81, spread=4); frame_rect(im, 1, 1, 15, 15, '#3a2412'); fill(im, 5, 6, 11, 9, '#d8cfb0'); return im


def crate():
    im = new(base='#8a6a42', seed=91, spread=5); px = im.load()
    for y in (0, 5, 10, 15):
        line(im, 0, y, 15, y, '#5a4224')
    fill(im, 3, 6, 13, 9, CRIMSON, 92, 10); line(im, 3, 7, 12, 7, '#ffd0a0')
    return im


def jersey():
    im = new(base='#9a978f', seed=101, spread=4); px = im.load()
    for x in range(16):
        if (x // 2) % 2 == 0:
            for y in (13, 14):
                px[x, y] = shade('#d9b23a', -8)
    for y in range(0, 16, 5):
        px[3, y] = shade(px[3, y], -20)
    return im


def gantry():
    im = new(base='#5a6066', seed=111, spread=4); px = im.load(); rnd = random.Random(112)
    for x in range(0, 16, 3):
        line(im, x, 0, x + 3, 15, '#454a50')
    for _ in range(10):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade('#8a4a2a', rnd.randint(-10, 10))
    return im


SIGN_COLOURS = ('#1d5a34', '#5a3a1d', '#d9a8b8', '#3a4a56')


def sign(right, sector):
    base = SIGN_COLOURS[sector]
    im = new(base=base, seed=121 + sector + (10 if right else 0), spread=3); frame_rect(im, 0, 0, 16, 16, '#e8e8e0' if sector != 2 else '#ffffff')
    ink = '#f2f2ea' if sector != 2 else '#4a3a44'
    if right:
        line(im, 3, 8, 12, 8, ink); line(im, 9, 5, 12, 8, ink); line(im, 9, 11, 12, 8, ink)
    else:
        line(im, 3, 8, 12, 8, ink); line(im, 6, 5, 3, 8, ink); line(im, 6, 11, 3, 8, ink)
    if sector == 3:
        fill(im, 0, 0, 16, 3, '#eef2f4', 123, 4); fill(im, 2, 3, 6, 5, '#eef2f4')
    return im


def lamp(lit):
    im = new(base='#3c3c3a', seed=131, spread=3); fill(im, 2, 9, 14, 15, AMBER if lit else '#4a3a28', 132, 6 if lit else 2); return im


def tube(lit):
    im = new(base='#b8b8b0', seed=141, spread=2); fill(im, 1, 5, 15, 11, '#f2fbff' if lit else '#7a8086', 142, 3); return im


def arm():
    im = new(base='#d8d4cc', seed=151, spread=3)
    for y in range(0, 16, 4):
        fill(im, 0, y, 16, y + 2, '#b8231c', 152, 8)
    return im


SEDAN = ('#7a3a2a', '#3a5a6a', '#8a7a4a', '#4a4a3a')


def sedan(k, part):
    im = new(base=SEDAN[k], seed=161 + k, spread=8); px = im.load(); rnd = random.Random(165 + k)
    for _ in range(20):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade('#8a4a22', rnd.randint(-14, 10))
    if part == 'side':
        fill(im, 2, 3, 14, 7, '#1a2024', 166 + k, 4); line(im, 0, 12, 15, 12, '#2a2a2a')
    return im


def payphone():
    im = new(base='#8a8a86', seed=171, spread=3); fill(im, 4, 2, 12, 7, '#2a2a2a'); fill(im, 5, 9, 11, 14, '#3a3a3a'); px = im.load()
    for x in range(5, 11, 2):
        for y in range(10, 14, 2):
            px[x, y] = (200, 200, 190, 255)
    line(im, 3, 3, 3, 13, '#1a1a1a')
    return im


def water():
    im = new(base='#3a6a8a', seed=181, spread=6)
    for y in range(16):
        for x in range(16):
            r, g, b, a = im.getpixel((x, y)); im.putpixel((x, y), (r, g, b, 150))
    return im


def key_hook(full):
    im = new(); fill(im, 6, 1, 10, 4, '#3a3a3a')
    if full:
        fill(im, 7, 4, 9, 13, '#c8a040'); fill(im, 6, 11, 10, 14, '#c8a040'); im.load()[9, 9] = (160, 120, 40, 255)
    return im


def gate():
    im = new(); px = im.load()
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0 or (x - y) % 4 == 0:
                px[x, y] = (150, 156, 160, 255)
    line(im, 0, 0, 0, 15, '#6a6e72'); line(im, 15, 0, 15, 15, '#6a6e72'); line(im, 0, 0, 15, 0, '#6a6e72')
    fill(im, 6, 7, 10, 10, '#b89a40')
    return im


def dormer(stage):
    im = new(base='#ece8e0', seed=191, spread=2); frame_rect(im, 3, 3, 13, 14, '#c8c2b8')
    if stage == 1:
        fill(im, 4, 4, 12, 13, '#d8f4ff', 192, 6)
        for y in range(4, 13, 2):
            line(im, 4, y, 11, y, '#9adcf0')
    elif stage == 2:
        fill(im, 4, 4, 12, 13, '#d8c890', 193, 4); fill(im, 6, 6, 10, 13, '#3a3028'); fill(im, 7, 5, 9, 7, '#3a3028')
    else:
        fill(im, 4, 4, 12, 13, '#1a1c22', 194, 2)
    line(im, 8, 4, 8, 12, '#c8c2b8'); line(im, 4, 8, 11, 8, '#c8c2b8')
    return im


def sign_bent():
    im = new(base='#e8e2c8', seed=201, spread=3); frame_rect(im, 0, 0, 16, 16, '#3a3a3a'); line(im, 3, 3, 12, 12, '#3a3a3a'); line(im, 12, 3, 3, 12, '#3a3a3a')
    fill(im, 0, 0, 16, 3, '#f2f4f6', 202, 3); return im


def wagon(on):
    im = new(base='#3a6a3a', seed=211, spread=5); fill(im, 1, 2, 15, 6, '#1a2420', 212, 3); line(im, 0, 10, 15, 10, '#c8c0a0')
    for x in (1, 14):
        fill(im, x - 1, 12, x + 1, 15, AMBER if on else '#5a3a1a')
    return im


def tailgate():
    im = new(base='#3a6a3a', seed=221, spread=5); fill(im, 2, 1, 14, 7, '#1a2420', 222, 3); fill(im, 6, 9, 10, 11, '#c8c0a0'); return im


def marjorie(part):
    """Her coat, wet through and darker than the dark; her hair down over the top of her face; her jaw hanging."""
    if part == 'low':
        im = new(base='#121418', seed=231, spread=4); px = im.load()
        line(im, 7, 0, 7, 11, '#08090c'); fill(im, 3, 12, 7, 16, '#d8d4cc', 232, 4); fill(im, 9, 12, 13, 16, '#d8d4cc', 233, 4)
        for y in range(0, 12, 3):
            px[5, y] = (40, 46, 56, 255); px[10, y + 1] = (40, 46, 56, 255)
        return im
    if part == 'coat':
        im = new(base='#121418', seed=234, spread=4); line(im, 7, 0, 7, 15, '#08090c'); return im
    if part == 'hair':
        return new(base='#0a0a0c', seed=236, spread=3)
    im = new(base='#0a0a0c', seed=235, spread=3)                     # the face: hair to the nose, then the jaw hanging open
    fill(im, 3, 9, 13, 16, '#c8c2b8', 237, 5); fill(im, 6, 12, 10, 16, '#1a0c0c')
    for x in (4, 6, 9, 11):
        line(im, x, 0, x, 10 + (x % 3), '#14141a')
    return im


def flare():
    im = new(); fill(im, 6, 4, 10, 16, CRIMSON, 241, 10); fill(im, 6, 1, 10, 4, '#ffe0a0'); im.load()[8, 0] = (255, 255, 220, 255); return im


def pole():
    im = new(base='#5a4a36', seed=251, spread=6)
    for x in range(0, 16, 4):
        line(im, x, 0, x, 15, '#4a3a28')
    return im


def reeds():
    im = new(); rnd = random.Random(261)
    for x in range(1, 16, 2):
        h = rnd.randint(7, 15); line(im, x, 15, x + rnd.choice((-1, 0, 1)), 15 - h, '#6a6a3a')
        if rnd.random() < .5:
            fill(im, x, 15 - h, x + 1, 15 - h + 3, '#5a3a24')
    return im


def barricade():
    im = new(base='#5a4a3e', seed=271, spread=12); px = im.load(); rnd = random.Random(272)
    for _ in range(6):
        x0, y0 = rnd.randrange(16), rnd.randrange(16); line(im, x0, y0, x0 + rnd.randint(-6, 6), y0 + rnd.randint(-3, 3), rnd.choice(('#7a3a2a', '#3a5a6a', '#1a1a1a', '#8a8a86')))
    return im


def steel():
    return new(base='#6a6e72', seed=281, spread=3)


def item_flare():
    im = new(); fill(im, 7, 4, 10, 15, CRIMSON, 291, 8); fill(im, 7, 2, 10, 4, '#ffe0a0'); im.load()[8, 1] = (255, 255, 220, 255); line(im, 7, 9, 9, 9, '#ffffff'); return im


def item_key():
    im = new(); fill(im, 5, 2, 10, 7, '#c8a040'); fill(im, 6, 3, 9, 6, (0, 0, 0, 0)); fill(im, 7, 7, 9, 14, '#c8a040'); fill(im, 9, 11, 11, 12, '#c8a040'); fill(im, 9, 13, 11, 14, '#c8a040'); return im


# ---------------------------------------------------------------------------------------------------- models
def element(lo, hi, textures, rotation=None, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures.get('all'))
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rotation:
        e['rotation'] = rotation
    return e


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def cube(tex, top=None, front=None):
    t = {'all': 'a'}; texs = {'a': tex}
    if top:
        t['up'] = 'u'; texs['u'] = top
    if front:
        t['north'] = 'f'; texs['f'] = front
    return model([element([0, 0, 0], [16, 16, 16], t)], texs, tex, 'solid')


def fitting_models():
    m = {}
    m['tty_0'] = model([element([3, 0, 3], [13, 8, 13], {'all': 's', 'up': 't'}), element([5, 8, 9], [11, 12, 10], {'all': 'p'})], {'s': 'steel', 't': 'tty', 'p': 'paper'}, 'tty')
    for s in range(2):
        m[f'radio_{s}'] = model([element([2, 0, 4], [14, 7, 12], {'all': 's', 'north': 'f'})], {'s': 'steel', 'f': 'radio_live' if s else 'radio'}, 'radio')
        m[f'lamp_{s}'] = model([element([4, 8, 4], [12, 16, 12], {'all': 'l'})], {'l': 'lamp' if s == 0 else 'lamp_off'}, 'lamp')
        m[f'fluorescent_{s}'] = model([element([2, 13, 6], [14, 16, 10], {'all': 't'})], {'t': 'tube' if s == 0 else 'tube_off'}, 'tube')
        m[f'key_hook_{s}'] = model([element([5, 3, 15], [11, 13, 16], {'north': 'k', 'south': 'k'})], {'k': 'key_hook' if s == 0 else 'key_hook_empty'}, 'key_hook')
        m[f'gate_{s}'] = model([element([0, 0, 7.5], [16, 16, 8.5], {'north': 'g', 'south': 'g'})] if s == 0 else [element([0, 0, 7], [2, 16, 9], {'all': 'p'})], {'g': 'gate', 'p': 'steel'}, 'gate')
        m[f'wagon_{s}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'w', 'up': 'r'})], {'w': 'wagon_on' if s else 'wagon', 'r': 'wagon_roof'}, 'wagon', 'solid')
        m[f'tailgate_{s}'] = model([element([0, 0, 12], [16, 14, 16], {'all': 't'})] if s == 0 else [element([0, 0, 0], [16, 3, 16], {'all': 't'})], {'t': 'tailgate'}, 'tailgate')
    m['marjorie_0'] = model([element([4, 0, 5], [12, 16, 11], {'all': 'c', 'north': 'l', 'south': 'l'})], {'c': 'marjorie_coat', 'l': 'marjorie_low'}, 'marjorie_coat')
    m['marjorie_1'] = model([element([4, 0, 5], [12, 9, 11], {'all': 'c'}), element([5, 9, 5.5], [11, 15, 10.5], {'all': 'h', 'north': 'f'}), element([3, 6, 6], [4, 9, 10], {'all': 'c'}), element([12, 6, 6], [13, 9, 10], {'all': 'c'})],
                            {'c': 'marjorie_coat', 'h': 'marjorie_hair', 'f': 'marjorie_face'}, 'marjorie_coat')
    m['logbook_0'] = model([element([3, 0, 3], [13, 2, 13], {'all': 'l'})], {'l': 'logbook'}, 'logbook')
    m['crate_0'] = model([element([1, 0, 2], [15, 10, 14], {'all': 'c'})], {'c': 'crate'}, 'crate')
    m['jersey_0'] = model([element([0, 0, 3], [16, 6, 13], {'all': 'j'}), element([0, 6, 5], [16, 13, 11], {'all': 'j'})], {'j': 'jersey'}, 'jersey')
    m['gantry_0'] = model([element([0, 4, 4], [16, 12, 12], {'all': 'g'})], {'g': 'gantry'}, 'gantry')
    for k in range(4):
        m[f'sign_l_{k}'] = model([element([0, 0, 14], [16, 16, 16], {'all': 'b', 'north': 's'})], {'s': f'sign_l_{k}', 'b': 'steel'}, f'sign_l_{k}')
        m[f'sign_r_{k}'] = model([element([0, 0, 14], [16, 16, 16], {'all': 'b', 'north': 's'})], {'s': f'sign_r_{k}', 'b': 'steel'}, f'sign_r_{k}')
        m[f'sedan_{k}'] = model([element([0, 0, 0], [16, 14, 16], {'all': 's', 'up': 't'})], {'s': f'sedan_{k}', 't': f'sedan_top_{k}'}, f'sedan_{k}', 'solid')
        els = [element([3, 0, 11], [13, 16, 16], {'all': 'b', 'north': 'p'})]
        if k:
            els.append(element([0, 0, 0], [16, 4 * k, 16], {'all': 'w'}))
        m[f'payphone_{k}'] = model(els, {'p': 'payphone', 'b': 'steel', 'w': 'water'}, 'payphone', 'translucent')
    for k in range(3):
        m[f'dormer_{k}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'r', 'north': 'w'})], {'w': f'dormer_{k}', 'r': 'dormer_frame'}, f'dormer_{k}', 'solid')
    m['arm_0'] = model([element([7, 0, 7], [9, 16, 9], {'all': 'a'})], {'a': 'arm'}, 'arm')
    m['sign_bent_0'] = model([element([7, 0, 7], [9, 12, 9], {'all': 'p'}), element([2, 8, 7.5], [14, 16, 8.5], {'north': 's', 'south': 's'}, {'angle': 22.5, 'axis': 'z', 'origin': [8, 8, 8]})],
                             {'p': 'steel', 's': 'sign_bent'}, 'sign_bent')
    m['flare_0'] = model([element([7, 0, 7], [9, 6, 9], {'all': 'f'}, None, {f: [6, 1, 10, 16] for f in ('north', 'south', 'east', 'west')})], {'f': 'flare'}, 'flare')
    m['pole_0'] = model([element([0, 0, 6], [16, 4, 10], {'all': 'p'})], {'p': 'pole'}, 'pole')
    m['reeds_0'] = model([element([0.8, 0, 8], [15.2, 16, 8], {'north': 'r', 'south': 'r'}, {'angle': 45, 'axis': 'y', 'origin': [8, 8, 8]}),
                          element([8, 0, 0.8], [8, 16, 15.2], {'east': 'r', 'west': 'r'}, {'angle': 45, 'axis': 'y', 'origin': [8, 8, 8]})], {'r': 'reeds'}, 'reeds')
    m['barricade_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b'})], {'b': 'barricade'}, 'barricade', 'solid')
    return m


def textures():
    out = {f'block/left_right/{k}.png': v for k, v in materials().items()}
    t = {'tty': tty(), 'paper': new(base='#ece6d4', seed=63, spread=3), 'radio': radio(False), 'radio_live': radio(True), 'logbook': logbook(), 'crate': crate(),
         'jersey': jersey(), 'gantry': gantry(), 'lamp': lamp(True), 'lamp_off': lamp(False), 'tube': tube(True), 'tube_off': tube(False), 'arm': arm(),
         'payphone': payphone(), 'water': water(), 'key_hook': key_hook(True), 'key_hook_empty': key_hook(False), 'gate': gate(),
         'dormer_frame': new(base='#ece8e0', seed=195, spread=2), 'sign_bent': sign_bent(), 'wagon': wagon(False), 'wagon_on': wagon(True),
         'wagon_roof': new(base='#2f5a2f', seed=213, spread=4), 'tailgate': tailgate(), 'marjorie_low': marjorie('low'), 'marjorie_coat': marjorie('coat'),
         'marjorie_hair': marjorie('hair'), 'marjorie_face': marjorie('face'), 'flare': flare(), 'pole': pole(), 'reeds': reeds(), 'barricade': barricade(), 'steel': steel()}
    for k in range(4):
        t[f'sign_l_{k}'] = sign(False, k); t[f'sign_r_{k}'] = sign(True, k); t[f'sedan_{k}'] = sedan(k, 'side'); t[f'sedan_top_{k}'] = sedan(k, 'top')
    for k in range(3):
        t[f'dormer_{k}'] = dormer(k)
    for k, v in t.items():
        out[f'block/left_right/{k}.png'] = v
    out['item/left_right_flare.png'] = item_flare(); out['item/left_right_key.png'] = item_key()
    return out


KINDS = ['tty', 'radio', 'logbook', 'crate', 'jersey', 'gantry', 'sign_l', 'sign_r', 'lamp', 'fluorescent', 'arm', 'sedan', 'payphone', 'key_hook', 'gate',
         'dormer', 'sign_bent', 'wagon', 'tailgate', 'marjorie', 'flare', 'pole', 'reeds', 'barricade']
STAGES = {'sign_l': 4, 'sign_r': 4, 'sedan': 4, 'payphone': 4, 'dormer': 3, 'radio': 2, 'lamp': 2, 'fluorescent': 2, 'key_hook': 2, 'gate': 2, 'wagon': 2, 'tailgate': 2, 'marjorie': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
ITEMS = ('left_right_flare', 'left_right_key')


def files():
    out = {}
    for name in materials():
        out[f'models/block/left_right_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/left_right_{name}.json'] = {'variants': {'': {'model': NS + f'block/left_right_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/left_right/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/left_right/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/left_right_fitting.json'] = {'variants': variants}
    for item in ITEMS:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            name = tex.split('block/left_right/')[-1]
            assert f'block/left_right/{name}.png' in images, (key, name)
    for rel, im in images.items():
        path = A / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    print(('Verified' if check else 'Drew') + f' the Left-Right Game: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
