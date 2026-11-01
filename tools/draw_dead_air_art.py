"""The Dead Air Broadcast (0.4.206): the 1983 basement, the black studio, the hallway's three tunings, the salt marsh relay and
the master booth.

Draws, in native Minecraft pixels from fixed seeds: eight materials (walnut paneling, mustard shag, faux brick, the black
studio, rotting velvet, stencilled plywood, storm-drain concrete, marsh mud); every fitting of the dead_air_fitting block, its
models facing north and turned by its facing (the shortwave receiver on four channels, the Zenith with snow, Horace, the stage
or nothing, Percy's body and head, his crate, a lamp in each channel's colour, the wall-breaker and the pole breaker thrown or
not, the bulkhead shut or open, the stage chest full or empty, the Skin-Taker's legs, body and head loose and frozen, its arm,
a dish at eight bearings, the lattice mast, a signal bulb in six states, the teleprompter, the back curtain shut or open, a
monitor with snow or Percy's marquee, the highchair, cut strings, a stage flat, a sandbag brace, cardboard waves, a magenta
spot off or on, cordgrass, the forum printouts and the folding table); and the brass tube and the dead-air breaker as items.
--check verifies everything already written.

Palette, from the owner's specification: walnut #3B2219, shag #A38335, faux brick #6E3C30, Zenith #1F1A17, velvet #5A121A,
gilt #9A7B38, waves #2C4A5E, magenta #C8328C, marsh sky #8C969E, cordgrass #4A472D, pools #242D2D, chain-link #3D3B37,
phosphor green #32CD32, dead air #00E5FF.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/dead_air/'
CHANNEL = ('#e0a040', '#c8328c', '#32cd32', '#00e5ff')
BULB = ((0x22, 0x22, 0x22), (0xd8, 0x20, 0x18), (0xc0, 0x80, 0x20), (0xff, 0xb0, 0x30), (0x32, 0xcd, 0x32), (0x00, 0xe5, 0xff))


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
def walnut():
    im = new(base='#3b2219', seed=11, spread=4); px = im.load()
    for x in (0, 4, 8, 12):
        for y in range(16):
            px[x, y] = shade(px[x, y], -14)
    for _ in range(12):
        r = random.Random(_ + 12); x, y = r.randrange(16), r.randrange(16); px[x, y] = shade(px[x, y], 10)
    return im


def shag():
    im = new(base='#a38335', seed=21, spread=14); return im


def faux_brick():
    im = new(base='#6e3c30', seed=31, spread=6); px = im.load()
    for y in range(0, 16, 4):
        for x in range(16):
            px[x, y] = (150, 140, 128, 255)
        off = 0 if (y // 4) % 2 == 0 else 4
        for x in range(off, 16, 8):
            for yy in range(y, min(16, y + 4)):
                px[x, yy] = (150, 140, 128, 255)
    return im


def studio():
    return new(base='#141414', seed=41, spread=2)


def velvet():
    im = new(base='#5a121a', seed=51, spread=6); px = im.load(); rnd = random.Random(52)
    for x in range(0, 16, 3):
        for y in range(16):
            px[x, y] = shade(px[x, y], -10)
    for _ in range(8):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade('#2a0a0c', rnd.randint(-4, 4))
    return im


def plywood():
    im = new(base='#b89a6a', seed=61, spread=5); line(im, 2, 6, 13, 6, '#3a2a1a'); line(im, 2, 9, 9, 9, '#3a2a1a')
    for x in range(2, 14, 3):
        im.load()[x, 3] = (60, 40, 30, 255)
    return im


def drain():
    im = new(base='#7a7a74', seed=71, spread=5); px = im.load()
    for y in (5, 11):
        for x in range(16):
            px[x, y] = shade(px[x, y], -14)
    for x in range(16):
        px[x, 15] = shade('#3a4a3a', 0)
    return im


def mud():
    return new(base='#3a3626', seed=81, spread=6)


def materials():
    return {'walnut': walnut(), 'shag': shag(), 'faux_brick': faux_brick(), 'studio': studio(), 'velvet': velvet(), 'plywood': plywood(), 'drain': drain(), 'mud': mud()}


# ---------------------------------------------------------------------------------------------------- fittings
def receiver(ch):
    im = new(base='#3a2e22', seed=91, spread=3); fill(im, 2, 3, 11, 8, '#1a1612'); line(im, 3, 5, 10, 5, CHANNEL[ch])
    im.load()[3 + ch * 2, 4] = (240, 240, 230, 255); fill(im, 12, 3, 15, 6, '#9a7b38'); fill(im, 12, 9, 15, 12, '#9a7b38'); return im


def snow(seed):
    im = new(); px = im.load(); rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            v = rnd.randint(40, 230); px[x, y] = (v, v, v, 255)
    return im


def crt(stage):
    im = new(base='#1f1a17', seed=101, spread=2)
    screen = snow(102) if stage in (0, 1) else new(base='#c8328c' if stage == 2 else '#050505', seed=103, spread=6)
    im.paste(screen.crop((0, 0, 11, 10)), (2, 2))
    if stage == 1:
        fill(im, 6, 3, 10, 4, '#101010'); fill(im, 7, 4, 9, 9, '#101010'); line(im, 6, 6, 10, 6, '#101010'); im.load()[8, 5] = (220, 220, 200, 255)
    fill(im, 13, 3, 15, 5, '#9a7b38'); return im


def percy(part):
    if part == 'head':
        im = new(base='#c8a878', seed=111, spread=4); fill(im, 4, 5, 6, 7, '#1a1a1a'); fill(im, 10, 5, 12, 7, '#1a1a1a'); line(im, 5, 11, 11, 11, '#5a2a1a'); line(im, 3, 2, 12, 2, '#6a4a2a')
        line(im, 7, 0, 7, 4, '#2a1a10'); return im
    im = new(base='#b8986a', seed=112, spread=4); fill(im, 3, 4, 13, 7, '#9a7b38'); line(im, 4, 5, 12, 5, '#ffd080')
    for x, y in ((5, 9), (8, 10), (11, 9)):
        fill(im, x - 1, y - 1, x + 1, y + 1, '#c09040')
    line(im, 7, 0, 7, 3, '#2a1a10'); return im


def crate():
    im = new(base='#7a5a3a', seed=121, spread=5)
    for y in (0, 7, 15):
        line(im, 0, y, 15, y, '#4a3420')
    return im


def lamp(ch):
    im = new(base='#2a2620', seed=131, spread=2); fill(im, 3, 3, 13, 13, CHANNEL[ch], 132, 6); return im


def lever(on):
    im = new(base='#3a3a36', seed=141, spread=2); frame_rect(im, 1, 1, 15, 15, '#9a7b38'); line(im, 8, 8, 8, 12 if on else 3, '#d02020'); im.load()[8, 12 if on else 3] = (255, 80, 60, 255); return im


def breaker(on):
    im = new(base='#2a2a26', seed=151, spread=2)
    import math
    for i in range(8):
        a = i / 8 * 6.283; x, y = round(8 + 5 * math.cos(a)), round(8 + 5 * math.sin(a)); im.load()[x, y] = (0, 229, 255, 255) if on else (40, 60, 70, 255)
    fill(im, 7, 4, 9, 12, '#c8c8c0'); return im


def bulkhead(opened):
    im = new(base='#4a4e52', seed=161, spread=3); frame_rect(im, 0, 0, 16, 16, '#2a2e32')
    if not opened:
        import math
        for i in range(16):
            a = i / 16 * 6.283; im.load()[round(8 + 4 * math.cos(a)), round(8 + 4 * math.sin(a))] = (180, 40, 30, 255)
        line(im, 4, 8, 12, 8, '#b02a20'); line(im, 8, 4, 8, 12, '#b02a20')
    return im


def chest(empty):
    im = new(base='#5a121a', seed=171, spread=5); frame_rect(im, 0, 0, 16, 16, '#9a7b38'); fill(im, 6, 6, 10, 9, '#9a7b38')
    if empty:
        fill(im, 2, 1, 14, 4, '#1a0a0a')
    return im


def burlap(frozen, part):
    im = new(base='#8a7450', seed=181 + (5 if part == 'head' else 0), spread=10); px = im.load(); rnd = random.Random(183)
    for x in range(0, 16, 2):
        for y in range(16):
            if (x + y) % 4 == 0:
                px[x, y] = shade(px[x, y], -16)
    for _ in range(3):
        y = rnd.randrange(16); line(im, 0, y, 15, y + rnd.randint(-1, 1), '#3a2a18')
    if part == 'head':
        fill(im, 0, 0, 16, 5, '#121212'); fill(im, 3, 10, 13, 13, '#f0ece0'); line(im, 3, 11, 12, 11, '#9a9080')
        for x in range(4, 13, 2):
            px[x, 12] = (180, 170, 150, 255)
        fill(im, 4, 6, 6, 8, '#0a0a0a'); fill(im, 10, 6, 12, 8, '#0a0a0a')
    if part == 'body':
        fill(im, 0, 0, 16, 4, '#2a0a10')
    if frozen:
        for y in range(16):
            for x in range(16):
                r, g, b, a = px[x, y]; px[x, y] = ((r + 0) // 2, (g + 229) // 2, (b + 255) // 2, a)
    return im


def dish():
    im = new(base='#d8d8d0', seed=191, spread=3); frame_rect(im, 0, 0, 16, 16, '#8a8a84'); fill(im, 7, 7, 9, 9, '#5a5a54'); return im


def mast():
    im = new(); px = im.load()
    for i in range(16):
        for (x, y) in ((i, i), (15 - i, i), (0, i), (15, i)):
            px[x, y] = (110, 104, 96, 255)
    return im


def bulb(stage):
    im = new(); r, g, b = BULB[stage]; fill(im, 4, 4, 12, 12, '#%02x%02x%02x' % (r, g, b)); frame_rect(im, 4, 4, 12, 12, '#3a3a3a'); return im


def teleprompter():
    im = new(base='#1a1a1a', seed=201, spread=2); fill(im, 2, 2, 14, 10, '#0a0a14')
    for y in (3, 5, 7):
        line(im, 3, y, 12 - (y % 3), y, '#e8e8e0')
    return im


def curtain():
    im = new(base='#5a121a', seed=211, spread=5); px = im.load()
    for x in range(0, 16, 3):
        for y in range(16):
            px[x, y] = shade(px[x, y], -14)
    fill(im, 0, 0, 16, 2, '#9a7b38'); return im


def monitor(marquee):
    im = new(base='#1f1a17', seed=221, spread=2)
    screen = snow(222) if not marquee else new(base='#0a0a0a', seed=223, spread=1)
    im.paste(screen.crop((0, 0, 12, 11)), (2, 2))
    if marquee:
        fill(im, 2, 6, 14, 9, '#9a7b38')
        for x in range(3, 13, 2):
            im.load()[x, 7] = (255, 220, 120, 255)
    return im


def highchair():
    im = new(base='#8a5a3a', seed=231, spread=12); px = im.load(); rnd = random.Random(232)
    for _ in range(20):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade('#6a3a1a', rnd.randint(-10, 10))
    return im


def strings():
    im = new(); rnd = random.Random(241)
    for x in (3, 7, 12):
        h = rnd.randint(6, 14); line(im, x, 0, x, h, '#d8d0b8')
    return im


def flat():
    im = new(base='#b89a6a', seed=251, spread=4); line(im, 0, 0, 15, 15, '#5a4a30'); line(im, 15, 0, 0, 15, '#5a4a30'); fill(im, 3, 12, 13, 14, '#3a2a1a'); return im


def brace():
    im = new(base='#8a7a5a', seed=261, spread=10); line(im, 0, 3, 15, 3, '#3a3a3a'); return im


def wave():
    im = new(); px = im.load()
    import math
    for x in range(16):
        top = round(6 + 3 * math.sin(x * .8))
        for y in range(top, 16):
            px[x, y] = shade('#2c4a5e', -4 if y > top + 1 else 18)
    return im


def spot(on):
    im = new(base='#1a1a1a', seed=271, spread=2); fill(im, 4, 4, 12, 12, '#c8328c' if on else '#3a1a2a'); return im


def grass():
    im = new(); rnd = random.Random(281)
    for x in range(0, 16, 2):
        h = rnd.randint(6, 15); line(im, x, 15, x + rnd.choice((-1, 0, 1)), 15 - h, '#4a472d')
    return im


def printouts():
    im = new(base='#ece6d4', seed=291, spread=3)
    for x in (1, 14):
        for y in range(0, 16, 2):
            im.load()[x, y] = (150, 150, 140, 255)
    for y in range(3, 14, 2):
        line(im, 3, y, 11 + (y % 3), y, '#4a4a4a')
    return im


def table():
    im = new(base='#8a8478', seed=301, spread=3); frame_rect(im, 0, 0, 16, 16, '#5a5650'); return im


def item_tube():
    im = new(); fill(im, 6, 2, 10, 13, '#c8a040'); fill(im, 7, 3, 9, 11, '#f0e0a0'); fill(im, 5, 13, 11, 15, '#5a4a30'); return im


def item_breaker():
    im = new(); fill(im, 3, 3, 13, 13, '#2a2a26'); frame_rect(im, 3, 3, 13, 13, '#00e5ff'); fill(im, 7, 4, 9, 11, '#c8c8c0'); return im


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
    for c in range(4):
        m[f'receiver_{c}'] = model([element([1, 0, 3], [15, 8, 13], {'all': 'w', 'north': 'f'})], {'w': 'walnut_dark', 'f': f'receiver_{c}'}, f'receiver_{c}')
        m[f'crt_{c}'] = model([element([1, 0, 1], [15, 14, 15], {'all': 'b', 'north': 'f'})], {'b': 'zenith', 'f': f'crt_{c}'}, f'crt_{c}')
        m[f'lamp_{c}'] = model([element([4, 12, 4], [12, 16, 12], {'all': 'l'})], {'l': f'lamp_{c}'}, f'lamp_{c}')
    m['percy_0'] = model([element([5, 0, 6], [11, 10, 10], {'all': 'b', 'north': 'f'}), element([4, 6, 6.5], [5, 13, 9.5], {'all': 'b'}), element([11, 6, 6.5], [12, 13, 9.5], {'all': 'b'}),
                          element([5, 10, 6.5], [11, 16, 9.5], {'all': 'b', 'north': 'f'})], {'b': 'percy_wood', 'f': 'percy_body'}, 'percy_body')
    m['percy_1'] = model([element([5, 0, 5], [11, 6, 11], {'all': 'w', 'north': 'f'}), element([7.9, 6, 7.9], [8.1, 16, 8.1], {'all': 's'})], {'w': 'percy_wood', 'f': 'percy_head', 's': 'strings'}, 'percy_head')
    m['crate_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'c'})], {'c': 'crate'}, 'crate', 'solid')
    m['table_0'] = model([element([0, 14, 0], [16, 16, 16], {'all': 't'}), element([1, 0, 1], [2, 14, 2], {'all': 'l'}), element([14, 0, 1], [15, 14, 2], {'all': 'l'}), element([1, 0, 14], [2, 14, 15], {'all': 'l'}), element([14, 0, 14], [15, 14, 15], {'all': 'l'})],
                         {'t': 'table', 'l': 'zenith'}, 'table')
    for s in range(2):
        m[f'lever_{s}'] = model([element([5, 3, 15], [11, 13, 16], {'north': 'l', 'south': 'l'})], {'l': 'lever_on' if s else 'lever'}, 'lever')
        m[f'breaker_{s}'] = model([element([4, 3, 12], [12, 13, 16], {'all': 'b', 'north': 'f'})], {'b': 'zenith', 'f': 'breaker_on' if s else 'breaker'}, 'breaker')
        m[f'bulkhead_{s}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b'})] if s == 0 else [element([0, 0, 0], [2, 16, 16], {'all': 'b'})], {'b': 'bulkhead' if s == 0 else 'bulkhead_open'}, 'bulkhead')
        m[f'chest_{s}'] = model([element([1, 0, 1], [15, 14, 15], {'all': 'c', 'up': 't'})], {'c': 'chest', 't': 'chest_open' if s else 'chest'}, 'chest')
        m[f'arm_{s}'] = model([element([0, 6, 5], [16, 12, 11], {'all': 'a'})], {'a': 'burlap_frozen' if s else 'burlap'}, 'burlap')
        m[f'curtain_{s}'] = model([element([0, 0, 6], [16, 16, 10], {'all': 'c'})] if s == 0 else [element([0, 0, 6], [2, 16, 10], {'all': 'c'})], {'c': 'curtain'}, 'curtain')
        m[f'monitor_{s}'] = model([element([1, 0, 1], [15, 14, 15], {'all': 'b', 'north': 'f'})], {'b': 'zenith', 'f': 'monitor_marquee' if s else 'monitor'}, 'monitor')
        m[f'spot_{s}'] = model([element([4, 10, 4], [12, 16, 12], {'all': 'l'})], {'l': 'spot_on' if s else 'spot'}, 'spot')
    for s in range(6):
        part = ('legs', 'body', 'head')[s % 3]; frozen = s >= 3
        t = 'burlap_frozen' if frozen else 'burlap'
        if part == 'legs':
            els = [element([4, 0, 6], [7, 16, 10], {'all': 'a'}), element([9, 0, 6], [12, 16, 10], {'all': 'a'})]
        elif part == 'body':
            els = [element([3, 0, 5], [13, 16, 11], {'all': 'a', 'up': 'k'}), element([2, 10, 4.5], [14, 16, 11.5], {'all': 'k'})]
        else:
            els = [element([4, 0, 4], [12, 8, 12], {'all': 'a', 'north': 'h'}), element([3, 8, 3], [13, 9, 13], {'all': 'k'}), element([5, 9, 5], [11, 16, 11], {'all': 'k'})]
        m[f'skin_{s}'] = model(els, {'a': t, 'h': ('skin_head_frozen' if frozen else 'skin_head'), 'k': 'cape'}, t)
    for s in range(2):
        m[f'dish_{s}'] = model([element([7, 0, 7], [9, 6, 9], {'all': 'p'}), element([1, 4, 7], [15, 14, 9], {'north': 'd', 'south': 'p', 'up': 'p', 'down': 'p', 'east': 'p', 'west': 'p'}, {'angle': 45, 'axis': 'y', 'origin': [8, 8, 8]} if s else None)],
                               {'p': 'mast', 'd': 'dish'}, 'dish')
    m['mast_0'] = model([element([4, 0, 4], [12, 16, 12], {'north': 'm', 'south': 'm', 'east': 'm', 'west': 'm'})], {'m': 'mast'}, 'mast')
    for s in range(6):
        m[f'bulb_{s}'] = model([element([5, 4, 5], [11, 12, 11], {'all': 'b'}), element([7, 0, 7], [9, 4, 9], {'all': 'p'})], {'b': f'bulb_{s}', 'p': 'mast'}, f'bulb_{s}')
    m['teleprompter_0'] = model([element([2, 0, 2], [14, 16, 14], {'all': 'b', 'north': 'f'})], {'b': 'zenith', 'f': 'teleprompter'}, 'teleprompter')
    m['highchair_0'] = model([element([3, 0, 3], [4, 9, 4], {'all': 'r'}), element([12, 0, 3], [13, 9, 4], {'all': 'r'}), element([3, 0, 12], [4, 9, 13], {'all': 'r'}), element([12, 0, 12], [13, 9, 13], {'all': 'r'}),
                             element([3, 9, 3], [13, 10, 13], {'all': 'r'}), element([3, 10, 12], [13, 16, 13], {'all': 'r'}), element([2, 11, 2], [14, 12, 5], {'all': 'r'})], {'r': 'highchair'}, 'highchair')
    m['strings_0'] = model([element([3, 0, 8], [13, 16, 8], {'north': 's', 'south': 's'}), element([8, 0, 3], [8, 16, 13], {'east': 's', 'west': 's'})], {'s': 'strings'}, 'strings')
    m['flat_0'] = model([element([0, 0, 13], [16, 16, 16], {'all': 'p', 'north': 'w'})], {'p': 'flat', 'w': 'walnut'}, 'flat')
    m['brace_0'] = model([element([2, 0, 2], [14, 6, 14], {'all': 'b'}), element([7, 6, 2], [9, 15, 4], {'all': 'i'}, {'angle': -45, 'axis': 'x', 'origin': [8, 6, 3]})], {'b': 'brace', 'i': 'zenith'}, 'brace')
    m['wave_0'] = model([element([0, 0, 8], [16, 10, 8], {'north': 'w', 'south': 'w'})], {'w': 'wave'}, 'wave')
    m['grass_0'] = model([element([0.8, 0, 8], [15.2, 16, 8], {'north': 'g', 'south': 'g'}, {'angle': 45, 'axis': 'y', 'origin': [8, 8, 8]}), element([8, 0, 0.8], [8, 16, 15.2], {'east': 'g', 'west': 'g'}, {'angle': 45, 'axis': 'y', 'origin': [8, 8, 8]})], {'g': 'grass'}, 'grass')
    m['printouts_0'] = model([element([3, 0, 2], [13, 3, 14], {'all': 'p'})], {'p': 'printouts'}, 'printouts')
    return m


def textures():
    out = {f'block/dead_air/{k}.png': v for k, v in materials().items()}
    t = {'walnut': walnut(), 'walnut_dark': new(base='#2a1810', seed=12, spread=3), 'zenith': new(base='#1f1a17', seed=13, spread=2), 'percy_wood': new(base='#b8986a', seed=113, spread=6),
         'percy_body': percy('body'), 'percy_head': percy('head'), 'crate': crate(), 'table': table(), 'lever': lever(False), 'lever_on': lever(True), 'breaker': breaker(False), 'breaker_on': breaker(True),
         'bulkhead': bulkhead(False), 'bulkhead_open': bulkhead(True), 'chest': chest(False), 'chest_open': chest(True), 'burlap': burlap(False, 'legs'), 'burlap_frozen': burlap(True, 'legs'),
         'skin_head': burlap(False, 'head'), 'skin_head_frozen': burlap(True, 'head'), 'cape': velvet(), 'dish': dish(), 'mast': mast(), 'teleprompter': teleprompter(), 'curtain': curtain(),
         'monitor': monitor(False), 'monitor_marquee': monitor(True), 'highchair': highchair(), 'strings': strings(), 'flat': flat(), 'brace': brace(), 'wave': wave(), 'spot': spot(False), 'spot_on': spot(True),
         'grass': grass(), 'printouts': printouts()}
    for c in range(4):
        t[f'receiver_{c}'] = receiver(c); t[f'crt_{c}'] = crt(c); t[f'lamp_{c}'] = lamp(c)
    for s in range(6):
        t[f'bulb_{s}'] = bulb(s)
    for k, v in t.items():
        out[f'block/dead_air/{k}.png'] = v
    out['item/dead_air_tube.png'] = item_tube(); out['item/dead_air_breaker.png'] = item_breaker()
    return out


KINDS = ['receiver', 'crt', 'percy', 'crate', 'lamp', 'lever', 'breaker', 'bulkhead', 'chest', 'skin', 'arm', 'dish', 'mast', 'bulb', 'teleprompter', 'curtain', 'monitor', 'highchair', 'strings',
         'flat', 'brace', 'wave', 'spot', 'grass', 'printouts', 'table']
STAGES = {'receiver': 4, 'crt': 4, 'lamp': 4, 'skin': 6, 'bulb': 6, 'percy': 2, 'lever': 2, 'breaker': 2, 'bulkhead': 2, 'chest': 2, 'arm': 2, 'curtain': 2, 'monitor': 2, 'spot': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
ITEMS = ('dead_air_tube', 'dead_air_breaker')


def files():
    out = {}
    for name in materials():
        out[f'models/block/dead_air_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/dead_air_{name}.json'] = {'variants': {'': {'model': NS + f'block/dead_air_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/dead_air/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(8):
                if kind == 'dish':
                    # A dish turns by its bearing, in eighths: a quarter turn of the block for every two, the model's own half step between.
                    v = {'model': NS + f'block/dead_air/dish_{stage % 2}'}; turn = 90 * (stage // 2)
                    if turn:
                        v['y'] = turn
                else:
                    drawn = stage if stage < STAGES.get(kind, 1) else 0
                    v = {'model': NS + f'block/dead_air/{kind}_{drawn}'}
                    if y:
                        v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/dead_air_fitting.json'] = {'variants': variants}
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
        for stage in range(2 if kind == 'dish' else STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            name = tex.split('block/dead_air/')[-1]
            assert f'block/dead_air/{name}.png' in images, (key, name)
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
    print(('Verified' if check else 'Drew') + f' the Dead Air Broadcast: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
