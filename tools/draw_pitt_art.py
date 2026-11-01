"""The God of the Pitt (0.4.207): the deeper passages under Ted the Caver's cave.

Draws, in native Minecraft pixels from fixed seeds: four materials (wet slate limestone, ochre limestone, red cave clay, calcite
flowstone); every fitting of the pitt_fitting block, its models facing north and turned by its facing (a pinch-ceiling slab at
seven drops, a seam at five apertures, a fissure plain, with eyes and with a hand reached out, a bolt bare and with the orange
line, Brad's gear crate, Brad seated at the belay and his head with the lamp off, white and amber, the belay post held and slack,
his card on a piton, the fist-sized hole, the round stone's sixteen faces, the copper groove, his cracked helmet dark and lit,
his line on the floor and into a crack, carbide canisters, piton tins, a rope coil, a carbide lantern, the field journal on its
stone, the vault's half ceiling and packed rubble; the bridges have a model with nothing in it); the fixed rope and the rusted
ladder; Brad's hammer, lamp and barometer as items; and the barometer's dial for the screen. --check verifies everything already
written.

Palette, from the owner's specification: limestone #4A4E51, calcite #D9D4C7, cave clay #6E2D25, rusted pitons #3A3532, orange
nylon cord #FF5500, carbide light at 1800K (#FF9A3C).
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
P = NS + 'block/pitt/'
CORD = '#ff5500'
RUST = '#3a3532'


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
def limestone(seed=11):
    im = new(base='#4a4e51', seed=seed, spread=5); px = im.load(); rnd = random.Random(seed + 1)
    for y in (4, 11):
        for x in range(16):
            if rnd.random() < .8:
                px[x, y] = shade(px[x, y], -12)
    for _ in range(10):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade(px[x, y], 18)
    return im


def ochre():
    im = new(base='#7a6440', seed=21, spread=6); px = im.load()
    for y in (3, 9, 14):
        for x in range(16):
            px[x, y] = shade(px[x, y], -14)
    return im


def clay():
    im = new(base='#6e2d25', seed=31, spread=6); px = im.load(); rnd = random.Random(32)
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade('#4e1d18', rnd.randint(-4, 4))
    return im


def calcite():
    im = new(base='#d9d4c7', seed=41, spread=4); px = im.load()
    for x in (2, 6, 7, 11, 14):
        for y in range(16):
            if (x * 3 + y) % 5:
                px[x, y] = shade(px[x, y], -14)
    return im


def materials():
    return {'limestone': limestone(), 'ochre': ochre(), 'clay': clay(), 'calcite': calcite()}


# ---------------------------------------------------------------------------------------------------- fittings
def slab_bottom():
    im = limestone(111); px = im.load()
    for x, y in ((2, 3), (3, 4), (4, 4), (5, 5), (6, 7), (7, 8), (9, 8), (10, 9), (12, 11), (13, 12)):
        px[x, y] = shade('#1a1c1e', 0)
    frame_rect(im, 0, 0, 16, 16, '#2e3133'); return im


def seam(a):
    im = limestone(121); px = im.load()
    fill(im, 0, 6, 16, 10, '#8f8a80', seed=122, spread=4)
    if a > 0:
        r = 1 + a * 1.6
        for y in range(16):
            for x in range(16):
                if (x - 7.5) ** 2 + (y - 8) ** 2 <= r * r:
                    px[x, y] = shade('#0d0d0e', 0)
    return im


def seam_cut():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); fill(im, 0, 0, 16, 16, '#5a5650', seed=123, spread=6); return im


def fissure(stage):
    im = limestone(131); px = im.load()
    for y in range(16):
        x = 7 + (1 if (y // 3) % 2 else 0)
        px[x, y] = shade('#050505', 0); px[x + 1, y] = shade('#0a0a0b', 0)
    if stage >= 1:
        px[7, 6] = (90, 255, 120, 255); px[8, 8] = (90, 255, 120, 255)
    return im


def hand():
    im = new(base='#c9c0b4', seed=141, spread=6)
    for y in (3, 8, 12):
        line(im, 2, y, 13, y + 1, '#a8402e')
    return im


def bolt(clipped):
    im = new(); fill(im, 6, 6, 10, 10, RUST); frame_rect(im, 5, 5, 11, 11, '#5a524a')
    if clipped:
        line(im, 0, 8, 15, 8, CORD); line(im, 0, 9, 15, 9, CORD)
    return im


def crate():
    im = new(base='#7a5a34', seed=151, spread=5); frame_rect(im, 0, 0, 16, 16, '#4a3420'); line(im, 0, 0, 15, 15, '#4a3420')
    fill(im, 5, 5, 8, 11, '#e8e0c8'); fill(im, 9, 9, 11, 11, '#e8e0c8'); return im


def coveralls():
    im = new(base='#8a7450', seed=161, spread=6); px = im.load(); rnd = random.Random(162)
    for _ in range(26):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade('#5a3a24', rnd.randint(-6, 6))
    line(im, 3, 0, 6, 15, '#202020'); line(im, 12, 0, 9, 15, '#202020'); return im


def face(lamp):
    im = new(base='#b88a6a', seed=171, spread=3); px = im.load()
    px[5, 8] = px[10, 8] = (30, 20, 16, 255); fill(im, 6, 11, 10, 12, '#6a4434')
    fill(im, 4, 13, 12, 16, '#5a3a2a', seed=172, spread=6)
    return im


def hardhat():
    im = new(base='#e0b020', seed=181, spread=6); frame_rect(im, 0, 12, 16, 16, '#a07810'); return im


def lamp_tex(stage):
    c = {0: '#5a4a30', 1: '#fff6d8', 2: '#ff9a3c'}[stage]
    im = new(base='#b08a40', seed=191, spread=3); fill(im, 4, 4, 12, 12, c); frame_rect(im, 3, 3, 13, 13, '#7a5a20'); return im


def post():
    im = new(base='#5a4a3a', seed=201, spread=5); line(im, 7, 0, 7, 15, '#3a2a1e'); return im


def rope_tex(slack=False):
    im = new(); px = im.load()
    for y in range(16):
        for x in (6, 7, 8, 9):
            px[x, y] = shade('#d8b878' if (x + y) % 2 else '#b89858', 0)
    return im


def cord():
    im = new(); px = im.load()
    for y in range(16):
        for x in (7, 8):
            px[x, y] = shade(CORD if (x + y) % 3 else '#c04000', 0)
    return im


def note():
    im = new(base='#e8e2d0', seed=211, spread=3)
    for y in (4, 6, 8, 10, 12):
        line(im, 3, y, 12 - (y % 4), y, '#5a5a6a')
    fill(im, 7, 0, 9, 3, RUST); return im


def hole():
    im = limestone(221); px = im.load()
    for y in range(16):
        for x in range(16):
            d = (x - 7.5) ** 2 + (y - 8) ** 2
            if d <= 6:
                px[x, y] = shade('#050505', 0)
            elif d <= 12 and (x + y) % 2:
                px[x, y] = shade('#3a5a4a', 0)
    return im


def wheel_sheet():
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(231)
    for y in range(64):
        for x in range(64):
            d = math.hypot(x - 31.5, y - 31.5)
            if d <= 31.5:
                c = shade('#6a6e70', rnd.randint(-5, 5))
                if 29 <= d <= 31.5:
                    c = shade('#4a4e50', rnd.randint(-3, 3))
                elif int(d) % 6 == 0:
                    c = shade(c, -10)
                px[x, y] = c
    for x in range(28, 36):
        for y in range(28, 36):
            px[x, y] = shade('#2a2c2e', 0)
    return im


def wheel_tile(sheet, u, row):
    return sheet.crop((u * 16, row * 16, u * 16 + 16, row * 16 + 16))


def groove():
    im = clay(); px = im.load()
    for x in range(16):
        for y in (6, 9):
            px[x, y] = shade('#5a7a5a', 0)
        for y in (7, 8):
            px[x, y] = shade('#b87a40' if (x // 2) % 2 else '#d89a5a', 0)
    return im


def helmet(lit):
    im = hardhat(); px = im.load(); line(im, 2, 2, 9, 10, '#3a2a10')
    fill(im, 6, 5, 10, 9, '#ff9a3c' if lit else '#5a4a30'); return im


def canister():
    im = new(base='#8a8e90', seed=241, spread=5); fill(im, 6, 0, 10, 3, '#e8e8e0'); line(im, 2, 9, 5, 7, '#5a5e60'); return im


def tins():
    im = new(base='#3a5a3a', seed=251, spread=5)
    for x in range(2, 15, 3):
        line(im, x, 1, x, 6, RUST)
    return im


def coil():
    im = new(); px = im.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 3 <= d <= 7.5 and int(d * 2) % 2 == 0:
                px[x, y] = shade(CORD, -10 if (x + y) % 3 else 0)
            elif 3 <= d <= 7.5:
                px[x, y] = shade('#c04000', 0)
    return im


def lantern():
    im = new(base='#b08a40', seed=261, spread=4); fill(im, 5, 5, 11, 12, '#ffcf7a'); fill(im, 7, 7, 9, 11, '#ff9a3c'); frame_rect(im, 4, 4, 12, 13, '#6a4a20'); return im


def journal():
    im = new(base='#e0d8c0', seed=271, spread=4); line(im, 8, 0, 8, 15, '#7a6a50')
    for y in range(3, 14, 2):
        line(im, 1, y, 6, y, '#4a4a5a'); line(im, 10, y, 14, y, '#4a4a5a')
    fill(im, 2, 11, 7, 14, '#8a7a60', seed=272, spread=6); return im


def journal_cover():
    return new(base='#3a4a2a', seed=281, spread=4)


def rubble():
    im = new(base='#5a5650', seed=291, spread=10); px = im.load(); rnd = random.Random(292)
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(16); px[x, y] = shade('#6e2d25', rnd.randint(-6, 6))
    return im


def ladder():
    im = new(); fill(im, 1, 0, 3, 16, '#6a3a20', seed=301, spread=8); fill(im, 13, 0, 15, 16, '#6a3a20', seed=302, spread=8)
    for y in (1, 5, 9, 13):
        fill(im, 3, y, 13, y + 2, '#7a4a28', seed=303 + y, spread=8)
    return im


def item_hammer():
    im = new(); fill(im, 7, 4, 9, 15, '#6a4a2a'); fill(im, 3, 2, 13, 6, '#5a5e60'); fill(im, 3, 2, 5, 6, '#3a3e40'); return im


def item_lamp():
    im = new(); fill(im, 5, 6, 11, 14, '#b08a40'); fill(im, 6, 3, 10, 6, '#8a6a30'); fill(im, 10, 7, 14, 10, '#ff9a3c'); return im


def item_barometer():
    im = new(); px = im.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 7.5:
                px[x, y] = shade('#b08a40', 0) if d > 5.5 else shade('#f0e8d0', 0)
    line(im, 8, 8, 4, 5, '#1a1410'); return im


def dial():
    im = Image.new('RGBA', (48, 48), (0, 0, 0, 0)); px = im.load()
    for y in range(48):
        for x in range(48):
            d = math.hypot(x - 23.5, y - 27.5)
            if d <= 21:
                px[x, y] = shade('#b08a40', -8 if d > 20 else 0) if d > 17 else shade('#f0e8d0', 0)
                if 13 <= d <= 16:
                    ang = math.degrees(math.atan2(x - 23.5, -(y - 27.5)))
                    if -70 <= ang <= -20:
                        px[x, y] = shade('#3a7a30', 0)
                    elif 20 <= ang <= 70:
                        px[x, y] = shade('#c03020', 0)
                    elif -20 < ang < 20:
                        px[x, y] = shade('#c08a20', 0)
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


def fitting_models():
    m = {}
    for s in range(7):
        m[f'slab_{s}'] = model([element([0, -2 * s, 0], [16, 16, 16], {'all': 'l', 'down': 'b'})], {'l': 'limestone_dark', 'b': 'slab_bottom'}, 'slab_bottom')
    for a in range(5):
        if a < 4:
            m[f'seam_{a}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'l', 'north': 's', 'south': 's'})], {'l': 'limestone_dark', 's': f'seam_{a}'}, f'seam_{a}', 'solid')
        else:
            m['seam_4'] = model([element([0, 0, 0], [2, 16, 16], {'all': 'c'}), element([14, 0, 0], [16, 16, 16], {'all': 'c'}), element([2, 14, 0], [14, 16, 16], {'all': 'c'}),
                                 element([2, 0, 0], [14, 1, 16], {'all': 'c'})], {'c': 'seam_cut'}, 'seam_cut')
    for s in range(3):
        els = [element([0, 0, 0], [16, 16, 16], {'all': 'l', 'north': 'f'})]
        if s == 2:
            els.append(element([5, 3, -6], [11, 6, 0], {'all': 'h'}))
            els.append(element([6, 2, -8], [10, 4, -6], {'all': 'h'}))
        m[f'fissure_{s}'] = model(els, {'l': 'limestone_dark', 'f': f'fissure_{min(s, 1)}', 'h': 'hand'}, 'fissure_0')
    for s in range(2):
        m[f'anchor_{s}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'l'}), element([6, 6, -3], [10, 10, 0], {'all': 'b'})] +
                                 ([element([7, 7.5, -4], [9, 8.5, -1], {'all': 'c'})] if s else []), {'l': 'limestone_dark', 'b': 'bolt_1' if s else 'bolt_0', 'c': 'cord'}, 'limestone_dark')
    m['crate_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'c'})], {'c': 'crate'}, 'crate', 'solid')
    # 0.4.220: Brad himself is a painted actor; this is the kit box he sits on, a rope coiled on its lid.
    m['brad_0'] = model([element([2, 0, 3], [14, 8, 13], {'all': 'k'}), element([2, 3, 2.5], [14, 4, 13.5], {'all': 'r'}),
                         element([4, 8, 5], [12, 9, 11], {'all': 'r'})], {'k': 'crate', 'r': 'cord'}, 'crate')
    for s in range(3):
        m[f'brad_head_{s}'] = model([element([4.5, 0, 5], [11.5, 7, 11], {'all': 'f', 'north': 'f'}), element([4, 6, 4.5], [12, 9, 11.5], {'all': 'y'}),
                                     element([7, 6.5, 3.5], [9, 8.5, 4.5], {'all': 'p'})], {'f': 'face', 'y': 'hardhat', 'p': f'lamp_{s}'}, 'face')
    m['belay_0'] = model([element([6, 0, 6], [10, 13, 10], {'all': 'w'}), element([5, 9, 5], [11, 11, 11], {'all': 'r'}), element([7.5, 10, 0], [8.5, 11, 6], {'all': 'c'})], {'w': 'post', 'r': 'rope', 'c': 'cord'}, 'post')
    m['belay_1'] = model([element([6, 0, 6], [10, 13, 10], {'all': 'w'}), element([5, 9, 5], [11, 11, 11], {'all': 'r'}), element([7.5, 0, 4.5], [8.5, 10, 5.5], {'all': 'c'})], {'w': 'post', 'r': 'rope', 'c': 'cord'}, 'post')
    m['note_0'] = model([element([4, 3, 15], [12, 13, 16], {'all': 'n'})], {'n': 'note'}, 'note')
    m['hole_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'l', 'north': 'h', 'south': 'h'})], {'l': 'limestone_dark', 'h': 'hole'}, 'hole', 'solid')
    for s in range(8):
        m[f'wheel_{s}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'e', 'north': 'w', 'south': 'w'})], {'e': 'wheel_edge', 'w': f'wheel_top_{s}'}, f'wheel_top_{s}')
        m[f'wheel_low_{s}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'e', 'north': 'w', 'south': 'w'})], {'e': 'wheel_edge', 'w': f'wheel_low_{s}'}, f'wheel_low_{s}')
    m['groove_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'c', 'up': 'g'})], {'c': 'clay', 'g': 'groove'}, 'groove', 'solid')
    for s in range(2):
        m[f'helmet_{s}'] = model([element([4, 0, 4], [12, 5, 12], {'all': 'h'}), element([3, 0, 3], [13, 1, 13], {'all': 'h'})], {'h': 'helmet_1' if s else 'helmet_0'}, 'helmet_0')
    m['line_0'] = model([element([7, 0, 0], [9, 1, 16], {'all': 'c'})], {'c': 'cord'}, 'cord')
    m['cut_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'l', 'north': 'f'}), element([7, 2, -6], [9, 3, 0], {'all': 'c'}), element([6, 2, -7], [10, 3, -6], {'all': 'c'})],
                       {'l': 'limestone_dark', 'f': 'fissure_0', 'c': 'cord'}, 'fissure_0')
    m['canister_0'] = model([element([4, 0, 4], [12, 10, 12], {'all': 'c'})], {'c': 'canister'}, 'canister')
    m['tins_0'] = model([element([2, 0, 3], [7, 6, 13], {'all': 't'}), element([8, 0, 3], [14, 5, 13], {'all': 't'})], {'t': 'tins'}, 'tins')
    m['coil_0'] = model([element([2, 0, 2], [14, 5, 14], {'all': 'c'})], {'c': 'coil'}, 'coil')
    m['lantern_0'] = model([element([5, 0, 5], [11, 9, 11], {'all': 'l'}), element([7, 9, 7], [9, 11, 9], {'all': 'l'})], {'l': 'lantern'}, 'lantern')
    m['bridge_0'] = {'textures': {'particle': P + 'limestone_dark'}, 'elements': []}
    m['journal_0'] = model([element([1, 0, 2], [15, 4, 14], {'all': 'l'}), element([3, 4, 4], [13, 6, 12], {'all': 'b', 'up': 'p'})], {'l': 'limestone_dark', 'b': 'journal_cover', 'p': 'journal'}, 'journal')
    m['ceiling_0'] = model([element([0, 8, 0], [16, 16, 16], {'all': 'l'})], {'l': 'limestone_dark'}, 'limestone_dark', 'solid')
    m['rubble_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'r'})], {'r': 'rubble'}, 'rubble', 'solid')
    return m


def climb_models():
    return {'rope': model([element([6.5, 0, 6.5], [9.5, 16, 9.5], {'all': 'r'})], {'r': 'rope'}, 'rope'),
            'ladder': model([element([0, 0, 15], [16, 16, 15.6], {'north': 'l', 'south': 'l'})], {'l': 'ladder'}, 'ladder')}


def textures():
    out = {f'block/pitt/{k}.png': v for k, v in materials().items()}
    sheet = wheel_sheet()
    t = {'limestone_dark': limestone(13), 'slab_bottom': slab_bottom(), 'seam_cut': seam_cut(), 'hand': hand(), 'bolt_0': bolt(False), 'bolt_1': bolt(True), 'crate': crate(),
         'coveralls': coveralls(), 'face': face(0), 'hardhat': hardhat(), 'post': post(), 'rope': rope_tex(), 'cord': cord(), 'note': note(), 'hole': hole(),
         'wheel_edge': new(base='#5a5e60', seed=232, spread=4), 'clay': clay(), 'groove': groove(), 'helmet_0': helmet(False), 'helmet_1': helmet(True), 'canister': canister(),
         'tins': tins(), 'coil': coil(), 'lantern': lantern(), 'journal': journal(), 'journal_cover': journal_cover(), 'rubble': rubble(), 'ladder': ladder()}
    for a in range(4):
        t[f'seam_{a}'] = seam(a)
    for s in range(2):
        t[f'fissure_{s}'] = fissure(s)
    for s in range(3):
        t[f'lamp_{s}'] = lamp_tex(s)
    for s in range(8):
        u = s % 4
        t[f'wheel_low_{s}'] = wheel_tile(sheet, u, 3 - s // 4)
        t[f'wheel_top_{s}'] = wheel_tile(sheet, u, 1 - s // 4)
    for k, v in t.items():
        out[f'block/pitt/{k}.png'] = v
    out['item/pitt_masonry_hammer.png'] = item_hammer(); out['item/pitt_carbide_lamp.png'] = item_lamp(); out['item/pitt_aneroid_barometer.png'] = item_barometer()
    out['gui/pitt_barometer.png'] = dial()
    return out


KINDS = ['slab', 'seam', 'fissure', 'anchor', 'crate', 'brad', 'brad_head', 'belay', 'note', 'hole', 'wheel', 'wheel_low', 'groove', 'helmet', 'line', 'cut', 'canister', 'tins', 'coil',
         'lantern', 'bridge', 'journal', 'ceiling', 'rubble']
STAGES = {'slab': 7, 'seam': 5, 'fissure': 3, 'brad_head': 3, 'anchor': 2, 'belay': 2, 'helmet': 2, 'wheel': 8, 'wheel_low': 8}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
ITEMS = ('pitt_masonry_hammer', 'pitt_carbide_lamp', 'pitt_aneroid_barometer')


def files():
    out = {}
    for name in materials():
        out[f'models/block/pitt_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/pitt_{name}.json'] = {'variants': {'': {'model': NS + f'block/pitt_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/pitt/{key}.json'] = obj
    for key, obj in climb_models().items():
        out[f'models/block/pitt/climb_{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(8):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/pitt/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/pitt_fitting.json'] = {'variants': variants}
    climb = {}
    for kind in ('rope', 'ladder'):
        for facing, y in FACING_Y.items():
            v = {'model': NS + f'block/pitt/climb_{kind}'}
            if y:
                v['y'] = y
            climb[f'facing={facing},kind={kind}'] = v
    out['blockstates/pitt_climb.json'] = {'variants': climb}
    for item in ITEMS:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = {**fitting_models(), **{f'climb_{k}': v for k, v in climb_models().items()}}
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            name = tex.split('block/pitt/')[-1]
            assert f'block/pitt/{name}.png' in images, (key, name)
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
    print(('Verified' if check else 'Drew') + f' the God of the Pitt: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
