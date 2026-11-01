"""The Whistlers in the Pines (0.4.202): the fire lookout, its fittings, the two things a reader carries, and the Whistler.

Draws, in native Minecraft pixels from fixed seeds: five materials (creosoted timber, worn floorboards, painted panel walls,
corrugated iron, roofing); every fitting of the lookout_fitting block, its models facing north and turned by its facing
(the window in its five kinds with four stages of frost, the cord, the Osborne fire finder, the brass pendulum, the amber
quadrant lamps, the bulb, the teleprinter, the radio, the stove, the desk, the protocol binder, the fire log, the cot, the
crank on its hook, the folded tarps, the generator, the shed lamp, the propane tank, the landing lanterns and the hatch);
the starter crank and the repair tarp as items; and the Whistler's own sheet (64 by 64, its long-limbed net). Writes the
models and block states with them. --check verifies everything already written.

Palette, from the owner's specification: creosote timber #2C221E, olive-drab canvas #3B4436, brass #B8860B, frost-white
glass #E0F7FA, black spruce #0D1B1E, phosphor-green tape #39FF14, violet-blue warning flame #6A5ACD.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/lookout/'
CREOSOTE, CANVAS, BRASS, FROST, SPRUCE, TAPE, VIOLET = '#2c221e', '#3b4436', '#b8860b', '#e0f7fa', '#0d1b1e', '#39ff14', '#6a5acd'


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
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[x, y] = shade(colour, rnd.randint(-spread, spread) if spread else 0, a)


def line(im, x0, y0, x1, y1, colour, a=255):
    px = im.load(); n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        px[round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n)] = shade(colour, 0, a)


# ---------------------------------------------------------------------------------------------------- materials
def timber(seed=11):
    """Creosoted timber: near black, a dull sheen, the grain running up the stilt and a check split down it."""
    im = new(base=CREOSOTE, seed=seed, spread=4); px = im.load(); rnd = random.Random(seed + 1)
    for x in range(16):
        if x % 4 == 1:
            for y in range(16):
                px[x, y] = shade(CREOSOTE, -6 + rnd.randint(-2, 2))
        if x % 5 == 3:
            for y in range(16):
                if rnd.random() < .5:
                    px[x, y] = shade(CREOSOTE, 9)
    for y in range(3, 12):
        px[11, y] = shade(CREOSOTE, -14)
    return im


def boards():
    """The cab floor: grey-brown boards worn pale down the middle where the watch walks."""
    im = new(base='#6d5c4b', seed=21, spread=5); px = im.load(); rnd = random.Random(22)
    for r in range(4):
        y1 = r * 4 + 3
        for x in range(16):
            px[x, y1] = shade('#3f342a', rnd.randint(-3, 3))
        end = (r * 7 + 2) % 16
        for y in range(r * 4, y1):
            px[end, y] = shade('#3f342a', 2)
        for _ in range(6):
            x, y = rnd.randint(0, 15), rnd.randint(r * 4, y1 - 1)
            px[x, y] = shade('#6d5c4b', rnd.choice((-12, 10)))
    return im


def panel():
    """The cab's lower walls: tongue-and-groove boards painted the service's olive, chipped."""
    im = new(base='#525c45', seed=31, spread=3); px = im.load(); rnd = random.Random(32)
    for x in range(0, 16, 4):
        for y in range(16):
            px[x, y] = shade('#3a4231', rnd.randint(-2, 2))
            px[x + 1, y] = shade('#5f6a51', 0)
    for _ in range(5):
        x, y = rnd.randint(1, 15), rnd.randint(0, 15)
        px[x, y] = shade('#7a6750', rnd.randint(-4, 4))
    return im


def iron():
    """Corrugated iron: ridges every two pixels, rust running down from the fixings."""
    im = new(base='#8a8f93', seed=41, spread=3); px = im.load(); rnd = random.Random(42)
    for x in range(16):
        k = (12, 2, -10, -2)[x % 4]
        for y in range(16):
            px[x, y] = shade(px[x, y], k)
    for x in (2, 10):
        for y in range(1, 1 + rnd.randint(4, 9)):
            px[x, y] = shade('#8a5a3a', rnd.randint(-8, 8))
    return im


def roof():
    """Tarred roofing in overlapping courses."""
    im = new(base='#3a3632', seed=51, spread=4); px = im.load(); rnd = random.Random(52)
    for r in range(4):
        for x in range(16):
            px[x, r * 4 + 3] = shade('#24211e', rnd.randint(-2, 2))
        for x in range((r * 3) % 4, 16, 4):
            for y in range(r * 4, r * 4 + 3):
                px[x, y] = shade('#2c2925', 0)
    return im


def materials():
    return {'timber': timber(), 'boards': boards(), 'panel': panel(), 'iron': iron(), 'roof': roof()}


# ---------------------------------------------------------------------------------------------------- the window
def glass(frost):
    """The pane: nearly clear, a pale sheen; frost ferns grow in from the edges, more with each stage."""
    im = new(); px = im.load(); rnd = random.Random(100 + frost)
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(FROST, 0, 46)
    for x, y in ((3, 3), (4, 4), (11, 2), (12, 3)):
        px[x, y] = shade('#ffffff', 0, 110)
    if frost:
        reach = (0, 3, 5, 8)[frost]
        for y in range(16):
            for x in range(16):
                edge = min(x, y, 15 - x, 15 - y)
                if edge < reach and rnd.random() < (reach - edge) / reach * .9:
                    px[x, y] = shade(FROST, rnd.randint(-6, 8), 150 + rnd.randint(0, 80))
        for _ in range(frost * 3):
            # A fern: a stalk in from an edge with short fronds off it.
            side = rnd.randrange(4); t = rnd.randint(2, 13); length = reach + rnd.randint(1, 3)
            for i in range(length):
                x, y = ((t, i), (15 - i, t), (t, 15 - i), (i, t))[side]
                if 0 <= x < 16 and 0 <= y < 16:
                    px[x, y] = shade('#ffffff', 0, 220)
                    if i % 2 and side in (0, 2) and 0 < x < 15:
                        px[x - 1, y] = shade(FROST, 0, 190); px[x + 1, y] = shade(FROST, 0, 190)
                    if i % 2 and side in (1, 3) and 0 < y < 15:
                        px[x, y - 1] = shade(FROST, 0, 190); px[x, y + 1] = shade(FROST, 0, 190)
    return im


def canvas():
    """The blind down: olive-drab canvas, a weave in the cloth and a batten across the foot."""
    im = new(base=CANVAS, seed=111, spread=3); px = im.load()
    for y in range(16):
        for x in range(16):
            if (x + y) % 3 == 0:
                px[x, y] = shade(px[x, y], -6)
    fill(im, 0, 14, 16, 16, '#5a4a36', seed=112, spread=3)
    return im


def canvas_torn():
    """What the gale left: ragged strips hanging from the roller, the rest open."""
    im = new(); px = im.load(); rnd = random.Random(121)
    fill(im, 0, 0, 16, 2, '#5a4a36', seed=122, spread=3)
    for x in range(16):
        hang = rnd.choice((0, 1, 2, 3, 5, 8)) if x % 5 else 0
        for y in range(2, 2 + hang):
            px[x, y] = shade(CANVAS, rnd.randint(-8, 4))
        if hang:
            px[x, 1 + hang] = shade(CANVAS, -18)
    return im


def tarp():
    """A repair tarp lashed over the frame: waxed canvas, browner than the blinds, cord across it."""
    im = new(base='#5a5238', seed=131, spread=4); px = im.load()
    for y in range(16):
        for x in range(16):
            if (x * 3 + y) % 7 == 0:
                px[x, y] = shade(px[x, y], 8)
    line(im, 0, 1, 15, 14, '#c7b88a'); line(im, 0, 14, 15, 1, '#c7b88a')
    for x, y in ((0, 0), (15, 0), (0, 15), (15, 15)):
        px[x, y] = shade(BRASS, 0)
    return im


def broken():
    """The glass gone: shards left in the corners of the frame."""
    im = new(); px = im.load(); rnd = random.Random(141)
    for y in range(16):
        for x in range(16):
            corner = min(x + y, 15 - x + y, x + 15 - y, 30 - x - y)
            if corner < 5 and rnd.random() < .8:
                px[x, y] = shade(FROST, rnd.randint(-10, 10), 130)
    for x, y in ((1, 4), (2, 3), (14, 2), (13, 3), (3, 13), (12, 14)):
        px[x, y] = shade('#ffffff', 0, 200)
    return im


# ---------------------------------------------------------------------------------------------------- fittings
def brass(seed=151):
    im = new(base=BRASS, seed=seed, spread=7); px = im.load()
    for x in range(0, 16, 5):
        for y in range(16):
            px[x, y] = shade(BRASS, 22)
    return im


def finder_top():
    """The Osborne fire finder from above: the brass ring with its degrees, north marked, the alidade across it."""
    im = new(base='#3a2e24', seed=161, spread=3); px = im.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.6 <= d <= 7.4:
                px[x, y] = shade(BRASS, 10 if (round(math.degrees(math.atan2(y - 7.5, x - 7.5))) // 15) % 2 else -6)
            elif d < 5.6:
                px[x, y] = shade('#d9d2b6', -abs(x - y) % 5)
    px[7, 0] = px[8, 0] = shade('#a02a20', 0)
    for i in range(2, 14):
        px[i, 7] = shade('#2a2a2a', 0)
    px[7, 7] = px[8, 8] = shade(BRASS, 30)
    return im


def wood(base, seam, seed):
    im = new(base=base, seed=seed, spread=4); px = im.load(); rnd = random.Random(seed + 1)
    for y in range(0, 16, 5):
        for x in range(16):
            px[x, y] = shade(seam, rnd.randint(-2, 2))
    return im


def bulb(lit):
    im = new(base='#f4eccc' if lit else '#8b8676', seed=171 + lit, spread=4 if lit else 2)
    if lit:
        fill(im, 5, 5, 11, 11, '#fffbe8')
    return im


def amber(lit):
    im = new(base='#ffb23a' if lit else '#6b4a22', seed=181 + lit, spread=6)
    if lit:
        fill(im, 6, 6, 10, 10, '#ffe9a8')
    return im


def tty_front(state):
    """The teleprinter: grey enamel, the keyboard, the green tape coming out of its slot, and its RX lamp."""
    im = new(base='#7c8580', seed=191, spread=3); px = im.load()
    fill(im, 2, 9, 14, 14, '#2f3432', seed=192, spread=3)
    for y in (10, 12):
        for x in range(3, 13, 2):
            px[x, y] = shade('#d8d4c4', 0)
    fill(im, 3, 2, 13, 4, '#1d201f')
    if state != 2:
        for x in range(5, 11):
            px[x, 3] = shade(TAPE, -60 if state == 0 else 0)
        for y in range(4, 8):
            px[7, y] = px[8, y] = shade(TAPE, -40 if state == 0 else -10)
    px[13, 6] = shade(TAPE if state == 1 else '#2a3a2a', 0)
    return im


def radio_front():
    im = new(base='#4b4f4a', seed=201, spread=3); px = im.load()
    fill(im, 2, 3, 9, 9, '#1c1d1b')
    for x in range(3, 8):
        px[x, 5] = shade('#a3ffa0', -40)
    for x, y in ((11, 4), (13, 4), (12, 8)):
        px[x, y] = shade('#c8c4b4', 0); px[x, y + 1] = shade('#7a776c', 0)
    return im


def stove(lit, part):
    """Cast iron, its door's draught holes glowing when it is lit."""
    im = new(base='#2a2a2c', seed=211 + part, spread=4); px = im.load()
    if part == 0:
        fill(im, 3, 4, 13, 12, '#1a1a1c')
        for x in range(5, 12, 2):
            px[x, 10] = shade('#ff7a28' if lit else '#3a3030', 0)
        if lit:
            px[7, 8] = px[8, 8] = shade('#ffb04a', 0)
    return im


def binder(cover):
    if cover:
        im = new(base='#4f5a3a', seed=221, spread=3)
        fill(im, 4, 5, 12, 9, '#d9d2b6'); line(im, 5, 7, 10, 7, '#3a3a3a')
        return im
    im = new(base='#e4dcc4', seed=222, spread=2); px = im.load()
    for y in range(0, 16, 2):
        for x in range(16):
            px[x, y] = shade('#cfc6aa', 0)
    return im


def log(cover):
    if cover:
        im = new(base='#57625c', seed=231, spread=3)
        fill(im, 0, 0, 16, 2, '#2f3532'); fill(im, 4, 6, 12, 9, '#cfc7ae')
        return im
    im = new(base='#e9e2cc', seed=232, spread=2); px = im.load()
    for y in range(2, 16, 3):
        for x in range(2, 15):
            px[x, y] = shade('#4a5a7a', 30)
    return im


def cot(top):
    im = new(base='#57603f' if top else '#4a4a3e', seed=241 + top, spread=4)
    if top:
        fill(im, 0, 0, 16, 5, '#8a8676', seed=243, spread=3)
    return im


def hook():
    im = new(); px = im.load()
    fill(im, 6, 1, 10, 3, '#3a3a3a')
    line(im, 8, 3, 8, 7, '#4a4a4a')
    line(im, 4, 8, 12, 8, '#6a6a6a'); line(im, 4, 8, 4, 13, '#6a6a6a'); line(im, 12, 8, 12, 11, '#6a6a6a')
    fill(im, 3, 13, 6, 15, '#6b4b2e')
    return im


def tarp_folded(top):
    im = new(base='#5a5238', seed=251 + top, spread=4); px = im.load()
    if not top:
        for y in range(1, 16, 2):
            for x in range(16):
                px[x, y] = shade('#47412c', 0)
    return im


def generator(part):
    """A small diesel set: green paint, the flywheel and the primer on its side, a grille on the radiator."""
    im = new(base='#4d5a3b', seed=261 + part, spread=4); px = im.load()
    if part == 0:
        fill(im, 2, 2, 14, 8, '#2a2f24')
        for y in range(3, 8):
            for x in range(3, 13, 2):
                px[x, y] = shade('#6b735e', 0)
        fill(im, 5, 10, 11, 14, '#1e1f1c'); px[8, 12] = shade(BRASS, 0)
    if part == 1:
        for y in range(16):
            for x in range(16):
                if 3 <= math.hypot(x - 8, y - 8) <= 6:
                    px[x, y] = shade('#2e2e2c', 0)
    return im


def shed_lamp(red):
    im = new(base='#ff5040' if red else '#fff6dc', seed=271 + red, spread=5)
    fill(im, 0, 0, 16, 2, '#3a3a3a')
    return im


def propane(part):
    im = new(base='#d9dcda', seed=281 + part, spread=3); px = im.load()
    rnd = random.Random(283 + part)
    for _ in range(30):
        px[rnd.randrange(16), rnd.randrange(16)] = shade(FROST, rnd.randint(-4, 8))
    if part == 1:
        fill(im, 6, 0, 10, 3, BRASS)
    return im


def lantern(stage):
    """A trail lantern: an iron cage round a glass chimney; its flame yellow, cold and blue, or out."""
    im = new(); px = im.load()
    flame = ('#ffd36a', VIOLET, '#3a3a3a')[stage]
    fill(im, 4, 3, 12, 13, '#d8e4e4', a=90)
    fill(im, 6, 6, 10, 11, flame, a=255 if stage < 2 else 160)
    if stage < 2:
        fill(im, 7, 7, 9, 10, '#fff6c8' if stage == 0 else '#b8b0ff')
    for x in (3, 12):
        line(im, x, 2, x, 13, '#3a3a3a')
    line(im, 3, 2, 12, 2, '#3a3a3a'); line(im, 3, 13, 12, 13, '#3a3a3a')
    return im


def hatch():
    im = boards(); px = im.load()
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px[x, y] = shade('#2a2a2a', 0)
    line(im, 3, 7, 12, 7, '#3a3a3a'); line(im, 3, 8, 12, 8, '#5a5a5a')
    return im


def cord():
    im = new(); px = im.load()
    for y in range(0, 12):
        px[7, y] = shade('#c2b48a', -(y % 2) * 12)
    fill(im, 6, 12, 10, 15, '#6b4b2e')
    return im


# ---------------------------------------------------------------------------------------------------- items and the Whistler
def crank_item():
    im = new(); px = im.load()
    line(im, 3, 12, 10, 5, '#5a5a5a'); line(im, 4, 12, 11, 5, '#7a7a7a')
    line(im, 10, 5, 13, 8, '#6a6a6a'); fill(im, 12, 8, 15, 11, '#6b4b2e')
    fill(im, 1, 12, 4, 15, '#3a3a3a')
    return im


def tarp_item():
    im = new(); fill(im, 2, 4, 14, 13, '#5a5238', seed=301, spread=4); px = im.load()
    for y in (6, 9, 12):
        for x in range(2, 14):
            px[x, y] = shade('#47412c', 0)
    line(im, 2, 8, 13, 8, '#c7b88a')
    return im


def whistler():
    """The Whistler's sheet on its own net: bark-dark skin, ridged like the spruce it stands in; sockets that take the light; fingers darker and longer at the ends of the arms."""
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(311)
    base = '#2a241f'
    def region(x0, y0, x1, y1):
        for y in range(y0, y1):
            for x in range(x0, x1):
                k = rnd.randint(-5, 5) + (-7 if (x + y * 3) % 7 == 0 else 0) + (6 if (x * 5 + y) % 11 == 0 else 0)
                px[x, y] = shade(base, k)
    region(0, 0, 32, 16)      # head
    region(32, 0, 50, 17)     # body
    region(0, 16, 24, 39)     # arms
    region(24, 17, 48, 38)    # legs
    # Bark ridges running down the limbs and the body.
    for x in list(range(1, 24, 3)) + list(range(25, 48, 3)) + list(range(33, 50, 4)):
        for y in range(0, 39):
            if px[x, y][3] and rnd.random() < .6:
                px[x, y] = shade(base, -12)
    # The face: two sockets deep enough to take the light, a seam of a mouth well below them.
    for x in (9, 10, 13, 14):
        for y in (11, 12):
            px[x, y] = (6, 5, 5, 255)
    for x in (8, 11, 12, 15):
        px[x, 11] = shade(base, -16)
    line(im, 10, 14, 13, 14, '#141110')
    # The hands: the last three pixels of each arm darker, the fingers split.
    for x0 in (0, 12):
        for y in range(36, 39):
            for x in range(x0, x0 + 12):
                px[x, y] = shade('#191512', rnd.randint(-3, 3)) if (x - x0) % 3 != 2 else (0, 0, 0, 0) if y == 38 else shade('#191512', 0)
    return im


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


def frame():
    """A muntin round each cell of the window, so the five-wide window reads as panes in a frame."""
    t = {'all': 'f'}
    return [element([0, 0, 7], [16, 1, 9], t), element([0, 15, 7], [16, 16, 9], t), element([0, 1, 7], [1, 15, 9], t), element([15, 1, 7], [16, 15, 9], t)]


def window_models():
    m = {}
    for stage in range(4):
        m[f'pane_{stage}'] = model(frame() + [element([1, 1, 7.9], [15, 15, 8.1], {'north': 'g', 'south': 'g'})], {'f': 'timber', 'g': f'glass_{stage}'}, f'glass_{stage}', 'translucent')
        m[f'torn_{stage}'] = model(frame() + [element([1, 1, 7.9], [15, 15, 8.1], {'north': 'g', 'south': 'g'}), element([0, 0, 9.2], [16, 16, 9.3], {'north': 'c', 'south': 'c'})],
                                   {'f': 'timber', 'g': f'glass_{stage}', 'c': 'canvas_torn'}, f'glass_{stage}', 'translucent')
    m['shutter_0'] = model(frame() + [element([1, 1, 7.9], [15, 15, 8.1], {'north': 'g'}), element([0, 0, 9], [16, 16, 10], {'all': 'c'})], {'f': 'timber', 'g': 'glass_0', 'c': 'canvas'}, 'canvas', 'translucent')
    m['tarp_0'] = model(frame() + [element([1, 1, 7.9], [15, 15, 8.1], {'north': 'g'}), element([0, 0, 9], [16, 16, 10], {'all': 't'})], {'f': 'timber', 'g': 'glass_0', 't': 'tarp'}, 'tarp', 'translucent')
    m['broken_0'] = model(frame() + [element([1, 1, 7.95], [15, 15, 8.05], {'north': 'g', 'south': 'g'})], {'f': 'timber', 'g': 'broken'}, 'broken', 'translucent')
    return m


def fitting_models():
    m = window_models()
    m['cord_0'] = model([element([6, 0, 14.5], [10, 16, 14.5], {'north': 'c', 'south': 'c'})], {'c': 'cord'}, 'cord')
    m['fire_finder_0'] = model([element([5, 0, 5], [11, 11, 11], {'all': 'w'}), element([2, 11, 2], [14, 13, 14], {'all': 'b', 'up': 't'}), element([7.5, 13, 2], [8.5, 15, 3], {'all': 'b'}), element([7.5, 13, 13], [8.5, 15, 14], {'all': 'b'}),
                                element([7.5, 13, 3], [8.5, 13.5, 13], {'all': 'b'})], {'w': 'finder_stand', 'b': 'brass', 't': 'finder_top'}, 'finder_top')
    for stage in range(2):
        rot = {'angle': 22.5, 'axis': 'x', 'origin': [8, 16, 8]} if stage else None
        m[f'pendulum_{stage}'] = model([element([7.5, 6, 7.5], [8.5, 16, 8.5], {'all': 'b'}, rot), element([6, 2, 6], [10, 6, 10], {'all': 'b'}, rot)], {'b': 'brass'}, 'brass')
        m[f'quadrant_lamp_{stage}'] = model([element([5, 4, 14], [11, 11, 16], {'all': 'm', 'north': 'l'})], {'m': 'brass', 'l': 'amber_lit' if stage else 'amber_out'}, 'brass')
        m[f'cab_lamp_{stage}'] = model([element([7.5, 12, 7.5], [8.5, 16, 8.5], {'all': 'm'}), element([6, 7, 6], [10, 12, 10], {'all': 'l'})], {'m': 'brass', 'l': 'bulb_lit' if stage == 0 else 'bulb_out'}, 'brass')
        m[f'stove_{stage}'] = model([element([2, 0, 2], [14, 13, 14], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'stove_side', 'f': 'stove_lit' if stage == 0 else 'stove_cold', 't': 'stove_top'}, 'stove_side')
        m[f'cot_{stage}'] = model([element([0, 4, 0], [16, 7, 16], {'all': 's', 'up': 't'}), element([0, 0, 0], [2, 4, 2], {'all': 's'}), element([14, 0, 0], [16, 4, 2], {'all': 's'}),
                                   element([0, 0, 14], [2, 4, 16], {'all': 's'}), element([14, 0, 14], [16, 4, 16], {'all': 's'})], {'s': 'cot_side', 't': 'cot_top' if stage == 0 else 'cot_side'}, 'cot_top')
        m[f'crank_hook_{stage}'] = model([element([3, 2, 15], [13, 14, 15.01], {'north': 'h' if stage == 0 else 'e', 'south': 'h' if stage == 0 else 'e'})], {'h': 'hook', 'e': 'hook_empty'}, 'hook')
        m[f'generator_{stage}'] = model([element([0, 0, 1], [16, 14, 15], {'all': 's', 'north': 'f', 'east': 'w', 'west': 'w', 'up': 's'})], {'s': 'generator_side', 'f': 'generator_front', 'w': 'generator_wheel'}, 'generator_side')
        m[f'shed_lamp_{stage}'] = model([element([5, 3, 10], [11, 11, 16], {'all': 'l'})], {'l': 'shed_lamp_red' if stage else 'shed_lamp_lit'}, 'shed_lamp_lit')
        m[f'propane_{stage}'] = model([element([0, 1, 2], [16, 14, 14], {'all': 'p'}), element([2, 0, 3], [4, 1, 13], {'all': 'c'})] + ([element([6, 14, 6], [10, 16, 10], {'all': 'v'})] if stage else []),
                                      {'p': 'propane', 'c': 'timber', 'v': 'propane_valve'}, 'propane')
    for stage in range(3):
        m[f'teleprinter_{stage}'] = model([element([1, 0, 1], [15, 14, 15], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'tty_side', 'f': f'tty_front_{stage}', 't': 'tty_side'}, 'tty_side')
        m[f'lantern_{stage}'] = model([element([5, 1, 5], [11, 12, 11], {'all': 'l', 'up': 'c', 'down': 'c'}, None, {'north': [3, 2, 13, 14], 'south': [3, 2, 13, 14], 'east': [3, 2, 13, 14], 'west': [3, 2, 13, 14]}),
                                       element([7.5, 12, 7.5], [8.5, 16, 8.5], {'all': 'c'})], {'l': f'lantern_{stage}', 'c': 'iron_dark'}, f'lantern_{stage}')
    m['radio_0'] = model([element([2, 0, 4], [14, 8, 12], {'all': 's', 'north': 'f'}), element([12, 8, 9], [12.5, 15, 9.5], {'all': 's'})], {'s': 'radio_side', 'f': 'radio_front'}, 'radio_front')
    m['desk_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'up': 't', 'north': 'f'})], {'s': 'desk_side', 't': 'desk_top', 'f': 'desk_front'}, 'desk_top')
    m['binder_0'] = model([element([3, 0, 2], [13, 4, 14], {'all': 'p', 'up': 'c', 'down': 'c', 'west': 'c'})], {'p': 'binder_pages', 'c': 'binder_cover'}, 'binder_cover')
    m['fire_log_0'] = model([element([3, 0, 3], [13, 2, 13], {'all': 'p', 'up': 'c', 'down': 'c'})], {'p': 'log_pages', 'c': 'log_cover'}, 'log_cover')
    m['tarp_pile_0'] = model([element([2, 0, 2], [14, 4, 14], {'all': 's', 'up': 't'})], {'s': 'tarp_folded_side', 't': 'tarp_folded_top'}, 'tarp_folded_top')
    m['hatch_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'h'})], {'h': 'hatch'}, 'hatch')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/lookout/{k}.png'] = v
    for stage in range(4):
        out[f'block/lookout/glass_{stage}.png'] = glass(stage)
    out['block/lookout/canvas.png'] = canvas(); out['block/lookout/canvas_torn.png'] = canvas_torn(); out['block/lookout/tarp.png'] = tarp(); out['block/lookout/broken.png'] = broken()
    out['block/lookout/brass.png'] = brass(); out['block/lookout/finder_top.png'] = finder_top(); out['block/lookout/finder_stand.png'] = wood('#4a3626', '#2f2218', 162)
    out['block/lookout/bulb_lit.png'] = bulb(1); out['block/lookout/bulb_out.png'] = bulb(0)
    out['block/lookout/amber_lit.png'] = amber(1); out['block/lookout/amber_out.png'] = amber(0)
    for stage in range(3):
        out[f'block/lookout/tty_front_{stage}.png'] = tty_front(stage)
        out[f'block/lookout/lantern_{stage}.png'] = lantern(stage)
    out['block/lookout/tty_side.png'] = new(base='#7c8580', seed=193, spread=3)
    out['block/lookout/radio_front.png'] = radio_front(); out['block/lookout/radio_side.png'] = new(base='#4b4f4a', seed=202, spread=3)
    out['block/lookout/stove_lit.png'] = stove(1, 0); out['block/lookout/stove_cold.png'] = stove(0, 0); out['block/lookout/stove_side.png'] = stove(0, 1); out['block/lookout/stove_top.png'] = stove(0, 2)
    out['block/lookout/desk_side.png'] = wood('#6b4f36', '#4a3626', 214); out['block/lookout/desk_top.png'] = wood('#775a3f', '#4a3626', 215)
    out['block/lookout/desk_front.png'] = wood('#6b4f36', '#4a3626', 216)
    out['block/lookout/binder_cover.png'] = binder(1); out['block/lookout/binder_pages.png'] = binder(0)
    out['block/lookout/log_cover.png'] = log(1); out['block/lookout/log_pages.png'] = log(0)
    out['block/lookout/cot_top.png'] = cot(1); out['block/lookout/cot_side.png'] = cot(0)
    out['block/lookout/hook.png'] = hook()
    empty = hook(); px = empty.load()
    for y in range(8, 16):
        for x in range(16):
            px[x, y] = (0, 0, 0, 0)
    out['block/lookout/hook_empty.png'] = empty
    out['block/lookout/tarp_folded_top.png'] = tarp_folded(1); out['block/lookout/tarp_folded_side.png'] = tarp_folded(0)
    out['block/lookout/generator_front.png'] = generator(0); out['block/lookout/generator_wheel.png'] = generator(1); out['block/lookout/generator_side.png'] = generator(2)
    out['block/lookout/shed_lamp_lit.png'] = shed_lamp(0); out['block/lookout/shed_lamp_red.png'] = shed_lamp(1)
    out['block/lookout/propane.png'] = propane(0); out['block/lookout/propane_valve.png'] = brass(287)
    out['block/lookout/iron_dark.png'] = new(base='#2f2f30', seed=291, spread=3)
    out['block/lookout/hatch.png'] = hatch(); out['block/lookout/cord.png'] = cord()
    out['item/lookout_crank.png'] = crank_item(); out['item/lookout_tarp.png'] = tarp_item()
    out['entity/lookout_whistler.png'] = whistler()
    return out


KINDS = ['pane', 'shutter', 'torn', 'tarp', 'broken', 'cord', 'fire_finder', 'pendulum', 'quadrant_lamp', 'cab_lamp', 'teleprinter', 'radio', 'stove', 'desk', 'binder', 'fire_log', 'cot',
         'crank_hook', 'tarp_pile', 'generator', 'shed_lamp', 'propane', 'lantern', 'hatch']
STAGES = {'pane': 4, 'torn': 4, 'teleprinter': 3, 'lantern': 3, 'pendulum': 2, 'quadrant_lamp': 2, 'cab_lamp': 2, 'stove': 2, 'cot': 2, 'crank_hook': 2, 'generator': 2, 'shed_lamp': 2, 'propane': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {}
    for name in materials():
        out[f'models/block/lookout_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/lookout_{name}.json'] = {'variants': {'': {'model': NS + f'block/lookout_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/lookout/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/lookout/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/lookout_fitting.json'] = {'variants': variants}
    for item in ('lookout_crank', 'lookout_tarp'):
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
    print(('Verified' if check else 'Drew') + f' the lookout: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
