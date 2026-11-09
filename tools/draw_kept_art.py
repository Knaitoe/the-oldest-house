"""The Kept (0.4.410): the inside of a machine.

Draws, in native Minecraft pixels from fixed seeds: eight materials (the machine's riveted casing, its floor grating, its ceiling panels
hung with cable, the same casing gone under rime, packed frost, the dining hall's flocked paper, its chequered floor, and the glare behind
the cells' doors); and every fitting of the kept_fitting block, its models facing north and turned by its facing (a machine cabinet with
its reels and small lamps, plain or sealed in rime; a cathode screen, dark or awake with lines crawling on it; cable along the floor; a
strip lamp; an icicle; the printer on its stand with its paper run out; the tray it prints into, empty or with a sheet in it; wire grown
into a thicket, with or without small lamps lit along it; a length of the long table, bare or laid for nobody; one of the hundred chairs;
the crate of tins; and a chandelier made of cable and bare bulbs). Writes the models and block states and the blocks' names. --check
verifies everything.
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
P = NS + 'block/kept/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4, size=(16, 16)):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load(); w, h = im.size
    for y in range(max(0, y0), min(h, y1)):
        for x in range(max(0, x0), min(w, x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    w, h = im.size
    if 0 <= x < w and 0 <= y < h:
        im.putpixel((x, y), shade(colour, 0, a))


def ring(im, cx, cy, r, colour):
    for k in range(32):
        a = k * 2 * math.pi / 32
        dot(im, round(cx + math.cos(a) * r), round(cy + math.sin(a) * r), colour)


# ------------------------------------------------------------------------------------------------ materials
def casing():
    im = new('#3b3f44', 41001, 3)
    for i in range(16):
        dot(im, i, 0, '#4b5056'); dot(im, 0, i, '#4b5056'); dot(im, i, 15, '#262a2e'); dot(im, 15, i, '#262a2e')
        dot(im, i, 8, '#30343a')
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13), (2, 10), (13, 10)):
        dot(im, x, y, '#62686e'); dot(im, x + 1, y + 1, '#24272b')
    return im


def grating():
    im = new('#16181b', 41002, 2)
    for i in range(16):
        for k in (0, 4, 8, 12):
            dot(im, i, k, '#4a4e54'); dot(im, k, i, '#3e4248')
            dot(im, i, k + 1, '#2e3136')
    return im


def ceiling():
    im = new('#26282c', 41003, 3)
    for x in range(16):
        for y, c in ((4, '#141518'), (5, '#1b1c1f'), (6, '#2f3136'), (11, '#141518'), (12, '#2f3136')):
            dot(im, x, y, c)
    for x in (3, 11):
        dot(im, x, 5, '#4a4c52'); dot(im, x + 4, 12, '#4a4c52')
    return im


def rime():
    im = new('#bccad3', 41004, 6); rnd = random.Random(41005)
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#7c8890'); dot(im, (x + 1) % 16, y, '#8e9aa2')
    for _ in range(20):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#e6eef3')
    return im


def frost():
    im = new('#dde6ec', 41006, 5); rnd = random.Random(41007)
    for _ in range(12):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#f6fafc')
    for _ in range(8):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#b9c6cf')
    return im


def flock():
    # A damask the machine has copied from nowhere in particular: the motif repeats, and one quarter of it is turned the wrong way.
    im = new('#5a1e24', 41008, 2)
    motif = ['..#..', '.###.', '#.#.#', '.###.', '..#..', '..#..', '.#.#.']
    for qx, qy, flip in ((0, 0, False), (8, 0, False), (0, 8, False), (8, 8, True)):
        for j, row in enumerate(motif):
            for i, c in enumerate(row):
                if c == '#':
                    dot(im, qx + 1 + (4 - i if flip else i), qy + (6 - j if flip else j), '#3d1216')
    for i in range(16):
        dot(im, i, 15, '#4a181d')
    return im


def checker():
    im = new('#d6d0c2', 41009, 4); rnd = random.Random(41010)
    for y in range(16):
        for x in range(16):
            if (x // 8 + y // 8) % 2:
                im.putpixel((x, y), shade('#2c2a28', rnd.randint(-3, 3)))
    for _ in range(10):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, shade(im.getpixel((x, y)), -12))
    return im


def glare():
    return new('#fbfbf5', 41011, 1)


def materials():
    return {'casing': casing(), 'grating': grating(), 'ceiling': ceiling(), 'rime': rime(), 'frost': frost(), 'flock': flock(), 'checker': checker(), 'glare': glare()}


# ------------------------------------------------------------------------------------------------ fittings
def cabinet_front():
    im = new('#4b524e', 41101, 3)
    for i in range(16):
        dot(im, i, 0, '#5d6560'); dot(im, 0, i, '#5d6560'); dot(im, i, 15, '#2f3431'); dot(im, 15, i, '#2f3431')
    fill(im, 2, 2, 14, 8, '#1f2421')
    for cx in (5, 10):
        ring(im, cx + .5, 5, 2.4, '#8a9088'); dot(im, cx, 5, '#c0c6bc'); dot(im, cx + 1, 5, '#c0c6bc')
    for i, c in enumerate(('#e0a040', '#70d070', '#e0a040', '#3c403c', '#70d070', '#e0a040')):
        dot(im, 3 + i * 2, 10, c)
    for y in (12, 13):
        for x in range(3, 13, 2):
            dot(im, x, y, '#2a2f2c')
    return im


def cabinet_frozen():
    im = cabinet_front(); rnd = random.Random(41102); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            k = .55 + rnd.random() * .2
            px[x, y] = (int(r * (1 - k) + 205 * k), int(g * (1 - k) + 220 * k), int(b * (1 - k) + 228 * k), 255)
    for i, x in enumerate(range(3, 15, 2)):
        if i % 2 == 0:
            dot(im, x, 10, '#e0b070')
    return im


def cabinet_side():
    im = new('#454c48', 41103, 3)
    for y in range(16):
        dot(im, 7, y, '#353b37'); dot(im, 8, y, '#565e59')
    for y in range(3, 14, 2):
        fill(im, 2, y, 6, y + 1, '#2c312e'); fill(im, 10, y, 14, y + 1, '#2c312e')
    return im


def bezel(im):
    for i in range(16):
        for k in (0, 1):
            dot(im, i, k, '#3a3d3a'); dot(im, i, 15 - k, '#2c2e2c'); dot(im, k, i, '#3a3d3a'); dot(im, 15 - k, i, '#2c2e2c')


def screen_off():
    im = new('#1a201c', 41104, 2); bezel(im)
    for k in range(3):
        dot(im, 4 + k, 4, '#2e3a32'); dot(im, 4, 4 + k, '#2e3a32')
    return im


def screen_on():
    # Four frames: lines of something like writing crawl up a cathode screen, with the scan line rolling down it.
    sheet = Image.new('RGBA', (16, 64))
    rnd = random.Random(41105)
    rows = [[rnd.random() < .6 for _ in range(10)] for _ in range(20)]
    for f in range(4):
        im = new('#0e2a1c', 41106 + f, 3); bezel(im)
        for line in range(5):
            row = rows[(line + f) % len(rows)]
            for i, on in enumerate(row):
                if on:
                    dot(im, 3 + i, 3 + line * 2, '#8af0b4')
        for x in range(2, 14):
            dot(im, x, 2 + (f * 3) % 12, '#4ec08a')
        sheet.paste(im, (0, f * 16))
    return sheet


def cable():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x0 in (1, 9):
            for i, c in enumerate(('#141517', '#2a2c30', '#3a3c40', '#1d1e20')):
                dot(im, x0 + i, y, c)
        if y % 5 == 2:
            dot(im, 2, y, '#4c4e54'); dot(im, 10, y, '#4c4e54')
    return im


def tube():
    im = new('#5a5e62', 41107, 2)
    fill(im, 1, 4, 15, 12, '#f2f4e6')
    for x in range(1, 15):
        dot(im, x, 4, '#d8dccc'); dot(im, x, 11, '#d8dccc')
    return im


def icicle():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(41108)
    for y in range(16):
        w = max(1, 4 - y // 4)
        for x in range(8 - w, 8 + w):
            dot(im, x, y, '#d4e8f0' if (x + y) % 3 else '#a8c8d8')
    return im


def printer_front():
    im = new('#c9c1a8', 41109, 3)
    fill(im, 2, 3, 14, 5, '#2a2824')
    for y in (8, 10, 12):
        for x in range(2, 14, 2):
            dot(im, x, y, '#6a6458'); dot(im, x + 1, y, '#8a8476')
    for i in range(16):
        dot(im, i, 15, '#9a937c')
    return im


def printer_side():
    im = new('#bdb59c', 41110, 3)
    for i in range(16):
        dot(im, i, 0, '#d4ccb2'); dot(im, i, 15, '#8e876f')
    fill(im, 3, 4, 13, 5, '#8e876f')
    return im


def printer_top():
    im = new('#c9c1a8', 41111, 3)
    fill(im, 3, 2, 13, 6, '#ece8dc')
    for x in range(4, 12):
        dot(im, x, 4, '#5a564c') if x % 3 else None
    fill(im, 2, 8, 14, 9, '#2a2824')
    return im


def paper():
    im = new('#ece8dc', 41112, 2)
    for y in range(2, 16, 2):
        for x in range(2, 14):
            if (x * 7 + y * 3) % 5:
                dot(im, x, y, '#5a564c')
    for y in range(16):
        dot(im, 0, y, '#d0cbbd'); dot(im, 15, y, '#d0cbbd')
        if y % 3 == 0:
            dot(im, 1, y, '#bab4a6'); dot(im, 14, y, '#bab4a6')
    return im


def tray_side():
    im = new('#5a5e62', 41113, 2)
    for i in range(16):
        dot(im, i, 0, '#7a7e84')
    return im


def tray_top(sheet):
    im = new('#4a4e52', 41114, 2)
    for i in range(16):
        dot(im, i, 0, '#6a6e72'); dot(im, 0, i, '#6a6e72'); dot(im, 15, i, '#34383c'); dot(im, i, 15, '#34383c')
    if sheet:
        fill(im, 3, 2, 13, 14, '#ece8dc')
        for y in range(4, 13, 2):
            for x in range(4, 12):
                if (x + y * 5) % 4:
                    dot(im, x, y, '#5a564c')
    return im


def wire(lit):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(41115)
    for strand in range(5):
        x = rnd.uniform(0, 16); dx = rnd.uniform(-.6, .6); c = ('#121315', '#1e2023', '#2c2e32', '#3a2a22')[strand % 4]
        for y in range(16):
            dot(im, int(x) % 16, y, c); dot(im, (int(x) + 1) % 16, y, shade(c, 10))
            x += dx + math.sin(y * .7 + strand) * .5
    for strand in range(2):
        y = rnd.uniform(0, 16)
        for x in range(16):
            dot(im, x, int(y) % 16, '#24262a'); y += math.sin(x * .5 + strand * 2) * .6
    if lit:
        for x, y, c in ((3, 4, '#e0a040'), (11, 9, '#70d070'), (6, 13, '#e0a040'), (13, 2, '#70d070')):
            dot(im, x, y, c)
    return im


def table_top(laid):
    im = new('#e6e0d2', 41116, 3)
    for i in range(16):
        dot(im, 0, i, '#cfc8b8'); dot(im, 15, i, '#cfc8b8')
    if laid:
        ring(im, 7.5, 7.5, 4.2, '#c4c0b4')
        fill(im, 5, 5, 11, 11, '#f4f2ec')
        for y in range(4, 12):
            dot(im, 2, y, '#8e9296'); dot(im, 13, y, '#8e9296')
        dot(im, 2, 4, '#a8acb0'); dot(im, 13, 4, '#a8acb0'); dot(im, 12, 4, '#a8acb0')
        ring(im, 12.5, 2.5, 1.2, '#b8c4c8')
    return im


def table_side():
    im = new('#e0dacb', 41117, 3)
    for x in range(16):
        dot(im, x, 15, '#c4bead')
        if x % 4 == 0:
            for y in range(4, 16):
                dot(im, x, y, '#d0c9b9')
    return im


def wood():
    im = new('#5a3a26', 41118, 4)
    for y in (3, 9, 14):
        for x in range(16):
            dot(im, x, y, '#4a2e1e')
    return im


def crate_side():
    im = new('#8a6a44', 41119, 4)
    for y in (0, 5, 10, 15):
        for x in range(16):
            dot(im, x, y, '#6a5034')
    for y in range(16):
        dot(im, 0, y, '#6a5034'); dot(im, 15, y, '#6a5034')
    for x, y in ((2, 2), (13, 2), (2, 12), (13, 12)):
        dot(im, x, y, '#3a3a3c')
    return im


def crate_top():
    im = new('#c4a458', 41120, 8)
    for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11), (7.5, 7.5)):
        ring(im, cx, cy, 2.6, '#7a7e84')
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                dot(im, round(cx + dx), round(cy + dy), '#a8acb2')
        dot(im, round(cx), round(cy) - 1, '#c8ccd2')
    for i in range(16):
        dot(im, i, 0, '#6a5034'); dot(im, 0, i, '#6a5034'); dot(im, i, 15, '#6a5034'); dot(im, 15, i, '#6a5034')
    return im


def bulb():
    im = new('#fff4d4', 41121, 2)
    dot(im, 1, 1, '#ffffff'); dot(im, 2, 1, '#ffffff')
    return im


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/kept_{name}.png'] = im
    faces = {
        'cabinet_front': cabinet_front(), 'cabinet_frozen': cabinet_frozen(), 'cabinet_side': cabinet_side(), 'screen_off': screen_off(),
        'screen_on': screen_on(), 'cable': cable(), 'tube': tube(), 'icicle': icicle(), 'printer_front': printer_front(),
        'printer_side': printer_side(), 'printer_top': printer_top(), 'paper': paper(), 'tray_side': tray_side(), 'tray_empty': tray_top(False),
        'tray_sheet': tray_top(True), 'wire': wire(False), 'wire_lit': wire(True), 'table_top': table_top(False), 'table_laid': table_top(True),
        'table_side': table_side(), 'wood': wood(), 'crate_side': crate_side(), 'crate_top': crate_top(), 'bulb': bulb(),
    }
    for name, im in faces.items():
        out[f'block/kept/{name}.png'] = im
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None):
    faces = {}
    keys = ['north', 'south', 'east', 'west', 'up', 'down']
    if 'all' in textures:
        textures = {**{k: textures['all'] for k in keys}, **{k: v for k, v in textures.items() if k != 'all'}}
    for k in keys:
        if k in textures:
            face = {'texture': '#' + textures[k]}
            if uv and k in uv:
                face['uv'] = uv[k]
            faces[k] = face
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, cutout=True):
    m = {'ambientocclusion': False, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}
    if cutout:
        m['render_type'] = 'minecraft:cutout'
    return m


def fitting_models():
    m = {}
    for stage, front in ((0, 'cabinet_front'), (1, 'cabinet_frozen')):
        m[f'cabinet_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f'})], {'s': 'cabinet_side', 'f': front}, front, False)
    for stage, face in ((0, 'screen_off'), (1, 'screen_on')):
        m[f'screen_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f'})], {'s': 'cabinet_side', 'f': face}, face, False)
    m['cable_0'] = model([element([1, 0, 0], [5, 2, 16], {'up': 'c', 'east': 'c', 'west': 'c'}, uv={'up': [1, 0, 5, 16]}),
                          element([9, 0, 0], [13, 2, 16], {'up': 'c', 'east': 'c', 'west': 'c'}, uv={'up': [9, 0, 13, 16]})], {'c': 'cable'}, 'cable')
    m['tube_0'] = model([element([1, 13, 6], [15, 16, 10], {'all': 't'})], {'t': 'tube'}, 'tube')
    m['icicle_0'] = model([element([6, 4, 6], [10, 16, 10], {'all': 'i'}), element([7, 1, 7], [9, 4, 9], {'all': 'i'})], {'i': 'icicle'}, 'icicle')
    m['printer_0'] = model([element([2, 6, 3], [14, 13, 13], {'all': 's', 'north': 'f', 'up': 't'}),
                            element([3, 0, 4], [4, 6, 5], {'all': 's'}), element([12, 0, 4], [13, 6, 5], {'all': 's'}),
                            element([3, 0, 11], [4, 6, 12], {'all': 's'}), element([12, 0, 11], [13, 6, 12], {'all': 's'}),
                            element([4, 0, 1.5], [12, 9, 2.5], {'north': 'p', 'south': 'p', 'up': 'p'}),
                            element([4, 0, 0], [12, .2, 4], {'up': 'p'})],
                           {'s': 'printer_side', 'f': 'printer_front', 't': 'printer_top', 'p': 'paper'}, 'printer_front')
    for stage, top in ((0, 'tray_empty'), (1, 'tray_sheet')):
        m[f'tray_{stage}'] = model([element([3, 0, 3], [13, 2, 13], {'all': 's', 'up': 't'}), element([3, 2, 11], [13, 9, 13], {'all': 's'})],
                                   {'s': 'tray_side', 't': top}, top)
    for stage, tex in ((0, 'wire'), (1, 'wire_lit')):
        m[f'wire_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'w'}), element([.01, .01, 8], [15.99, 15.99, 8], {'north': 'w', 'south': 'w'}),
                                    element([8, .01, .01], [8, 15.99, 15.99], {'east': 'w', 'west': 'w'})], {'w': tex}, tex)
    for stage, top in ((0, 'table_top'), (1, 'table_laid')):
        m[f'table_{stage}'] = model([element([0, 11, 0], [16, 13, 16], {'all': 's', 'up': 't', 'down': 'w'}), element([7, 0, 7], [9, 11, 9], {'all': 'w'}),
                                     element([5, 0, 5], [11, 1, 11], {'all': 'w'})], {'s': 'table_side', 't': top, 'w': 'wood'}, top)
    m['chair_0'] = model([element([3, 8, 3], [13, 9, 13], {'all': 'w'}),
                          element([3, 0, 3], [4, 8, 4], {'all': 'w'}), element([12, 0, 3], [13, 8, 4], {'all': 'w'}),
                          element([3, 0, 12], [4, 8, 13], {'all': 'w'}), element([12, 0, 12], [13, 8, 13], {'all': 'w'}),
                          element([3, 9, 12], [4, 19, 13], {'all': 'w'}), element([12, 9, 12], [13, 19, 13], {'all': 'w'}),
                          element([4, 15, 12.25], [12, 18, 12.75], {'all': 'w'})], {'w': 'wood'}, 'wood')
    m['crate_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'up': 't'})], {'s': 'crate_side', 't': 'crate_top'}, 'crate_side', False)
    m['chandelier_0'] = model([element([7.5, 12, 7.5], [8.5, 16, 8.5], {'all': 'c'}), element([3, 11, 3], [13, 12, 13], {'all': 'c'}),
                               element([3.5, 7, 3.5], [5.5, 11, 5.5], {'all': 'b'}), element([10.5, 7, 3.5], [12.5, 11, 5.5], {'all': 'b'}),
                               element([3.5, 7, 10.5], [5.5, 11, 12.5], {'all': 'b'}), element([10.5, 7, 10.5], [12.5, 11, 12.5], {'all': 'b'}),
                               element([7, 6, 7], [9, 11, 9], {'all': 'b'})], {'c': 'cable', 'b': 'bulb'}, 'bulb')
    return m


KINDS = ['cabinet', 'screen', 'cable', 'tube', 'icicle', 'printer', 'tray', 'wire', 'table', 'chair', 'crate', 'chandelier']
STAGES = {'cabinet': 2, 'screen': 2, 'tray': 2, 'wire': 2, 'table': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'kept_fitting': 'Machine Fitting', 'kept_casing': 'Machine Casing', 'kept_grating': 'Floor Grating', 'kept_ceiling': 'Cable Ceiling',
         'kept_rime': 'Rime', 'kept_frost': 'Packed Frost', 'kept_flock': 'Flocked Paper', 'kept_checker': 'Chequered Floor', 'kept_glare': 'Glare'}
SCREEN_META = {'animation': {'frametime': 3}}


def files():
    out = {}
    for name in materials():
        out[f'models/block/kept_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/kept_{name}'}}
        out[f'blockstates/kept_{name}.json'] = {'variants': {'': {'model': NS + f'block/kept_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/kept/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(2):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/kept/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/kept_fitting.json'] = {'variants': variants}
    out['textures/block/kept/screen_on.png.mcmeta'] = SCREEN_META
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
            assert f'block/kept/{tex.split("kept/")[1]}.png' in images, (key, tex)
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the machine: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
