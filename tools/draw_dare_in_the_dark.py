"""Third Floor Girls' (0.4.173): the school's third-floor hall, its girls' bathroom, the wide mirror and the dare's figures.

Draws, in native Minecraft pixels from fixed seeds: seven wall, floor and ceiling materials; the hall's and bathroom's
fittings (one dare_fitting block, its models facing north and turned by its facing); the four-by-two mirror whole and
shattered; the heavy bathroom door and the stairwell door; the candle stub; the two kids' bedsheets, the woman in the
glass and the second flame. Writes the models and block states with them. --check verifies everything already written.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/dare/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def noise(im, base, seed, spread=5, box=None):
    """Per-pixel tone noise over a base colour, as vanilla's painted and stone surfaces have."""
    rnd = random.Random(seed); px = im.load(); w, h = im.size
    x0, y0, x1, y1 = box or (0, 0, w, h)
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[x, y] = shade(base, rnd.randint(-spread, spread))


def new(size=(16, 16), base=None, seed=0, spread=5):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        noise(im, base, seed, spread)
    return im


# ---------------------------------------------------------------------------------------------------- materials
def block_wall(base, mortar, seed):
    """Painted cinder block: two courses to a block, the joints offset, the paint thick in the mortar."""
    im = new(base=base, seed=seed, spread=4); d = ImageDraw.Draw(im)
    for y in (7, 15):
        d.line((0, y, 15, y), fill=shade(mortar, 0))
    for y0, x in ((0, 0), (8, 8)):
        d.line((x, y0, x, y0 + 6), fill=shade(mortar, 0))
    rnd = random.Random(seed + 1); px = im.load()
    for _ in range(9):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        if y not in (7, 15):
            px[x, y] = shade(base, -10)
    return im


def materials():
    out = {}
    out['wall'] = block_wall('#d9d1b9', '#b8ae96', 11)
    out['wall_band'] = block_wall('#5c8682', '#466a67', 12)
    trim = block_wall('#d9d1b9', '#b8ae96', 13); d = ImageDraw.Draw(trim)
    d.rectangle((0, 13, 15, 15), fill=shade('#3f5f5c', 0)); d.line((0, 12, 15, 12), fill=shade('#e9e3cf', 0))
    out['trim'] = trim
    tile = new(); px = tile.load(); rnd = random.Random(21)
    for y in range(16):
        for x in range(16):
            base = '#cfcabd' if (x // 8 + y // 8) % 2 == 0 else '#b6b2a6'
            k = rnd.randint(-4, 4)
            if rnd.random() < .07:
                k += rnd.choice((-22, 14))
            px[x, y] = shade(base, k)
    for i in range(16):
        px[i, 0] = shade(px[i, 0], -12); px[0, i] = shade(px[0, i], -12); px[i, 8] = shade(px[i, 8], -9); px[8, i] = shade(px[8, i], -9)
    out['hall_tile'] = tile
    ceil = new(base='#e4e1d7', seed=31, spread=3); px = ceil.load(); rnd = random.Random(32)
    for _ in range(26):
        px[rnd.randint(1, 15), rnd.randint(1, 15)] = shade('#e4e1d7', -38)
    for i in range(16):
        px[i, 0] = shade('#c9c6bc', 0); px[0, i] = shade('#c9c6bc', 0)
    out['ceiling'] = ceil
    hexes = new(base='#9b998f', seed=41, spread=3); px = hexes.load(); rnd = random.Random(42)
    for row in range(4):
        for col in range(4):
            ox = col * 4 + (2 if row % 2 else 0); oy = row * 4
            tone = '#e6e4dd' if rnd.random() > .12 else '#b9b7ae'
            for dy, span in ((0, (1, 2)), (1, (0, 3)), (2, (1, 2))):
                for dx in range(span[0], span[1] + 1):
                    px[(ox + dx) % 16, (oy + dy) % 16] = shade(tone, rnd.randint(-3, 3))
    out['bath_floor'] = hexes
    glaze = new(base='#c4aca8', seed=51, spread=2); px = glaze.load(); rnd = random.Random(52)
    for ty in range(4):
        for tx in range(4):
            for y in range(ty * 4, ty * 4 + 3):
                for x in range(tx * 4, tx * 4 + 3):
                    px[x, y] = shade('#e0c4c0', rnd.randint(-4, 3))
            px[tx * 4, ty * 4] = shade('#f3e2df', 0)
    out['bath_wall'] = glaze
    return out


# ---------------------------------------------------------------------------------------------------- fittings
def locker(upper):
    im = new(base='#4e6d8c', seed=61 + upper, spread=4); d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=shade('#2e4155', 0)); d.line((1, 0 if upper else 15, 14, 0 if upper else 15), fill=shade('#2e4155', 0))
    d.line((1, 1, 14, 1), fill=shade('#6c8cab', 0)); d.line((1, 1, 1, 14), fill=shade('#6c8cab', 0))
    if upper:
        for y in (3, 5, 7):
            d.line((4, y, 11, y), fill=shade('#22303f', 0)); d.line((4, y + 1, 11, y + 1), fill=shade('#7593b0', 0))
        d.rectangle((6, 11, 9, 12), fill=shade('#c7c9c4', 0)); d.point((7, 11), fill=shade('#5a5d5a', 0)); d.point((8, 12), fill=shade('#5a5d5a', 0))
    else:
        d.rectangle((11, 2, 12, 7), fill=shade('#b8bbb6', 0)); d.point((11, 7), fill=shade('#6c706b', 0)); d.rectangle((10, 4, 13, 5), fill=shade('#d4d6d1', 0))
        for y in (11, 13):
            d.line((4, y, 11, y), fill=shade('#22303f', 0))
    rnd = random.Random(63 + upper); px = im.load()
    for _ in range(4):
        x, y = rnd.randint(2, 13), rnd.randint(2, 13); px[x, y] = shade('#8a9aa8', 6)
    return im


def classroom(upper):
    im = new(base='#a6783f', seed=71 + upper, spread=6); d = ImageDraw.Draw(im)
    for x in range(1, 15, 3):
        d.line((x, 0, x, 15), fill=shade('#93662f', 0))
    d.rectangle((0, 0, 15, 15), outline=shade('#5d5f5c', 0)); d.line((1, 0, 1, 15), fill=shade('#7b7d7a', 0)); d.line((14, 0, 14, 15), fill=shade('#7b7d7a', 0))
    if upper:
        d.rectangle((5, 2, 10, 13), fill=shade('#2a313a', 0)); d.rectangle((5, 2, 10, 13), outline=shade('#8e918d', 0))
        for y in range(4, 13, 3):
            d.line((6, y, 9, y), fill=shade('#4f5965', 0))
        d.line((6, 3, 9, 6), fill=shade('#56606d', 0))
    else:
        d.rectangle((11, 3, 13, 4), fill=shade('#c9cbc6', 0)); d.point((11, 5), fill=shade('#c9cbc6', 0))
        d.line((2, 14, 13, 14), fill=shade('#7b7d7a', 0))
    return im


def trophy(upper):
    if not upper:
        im = new(base='#4c3322', seed=81, spread=5); d = ImageDraw.Draw(im)
        d.rectangle((1, 2, 7, 13), outline=shade('#2e1f14', 0)); d.rectangle((8, 2, 14, 13), outline=shade('#2e1f14', 0))
        d.point((6, 8), fill=shade('#c6a85a', 0)); d.point((9, 8), fill=shade('#c6a85a', 0)); d.line((0, 0, 15, 0), fill=shade('#6a4a33', 0))
        return im
    im = new(base='#27313a', seed=82, spread=3); d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=shade('#4c3322', 0)); d.line((1, 8, 14, 8), fill=shade('#9aa3a8', 0))
    for x, h in ((3, 4), (7, 6), (11, 3)):
        d.rectangle((x - 1, 7 - h, x + 1, 7 - h + 2), fill=shade('#d6b44a', 0)); d.line((x, 7 - h + 3, x, 6), fill=shade('#b8932f', 0)); d.rectangle((x - 1, 7, x + 1, 7), fill=shade('#5a4026', 0))
    d.rectangle((4, 10, 6, 14), fill=shade('#b9c2c8', 0)); d.polygon([(9, 10), (14, 12), (9, 14)], fill=shade('#7d2a2a', 0))
    d.line((2, 1, 5, 4), fill=shade('#5b6b78', 0))
    return im


def bulletin(i):
    im = new(base='#b08857', seed=91 + i, spread=7); d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=shade('#a7aaa6', 0))
    rnd = random.Random(93 + i)
    for colour in ('#f1efe4', '#ece27c', '#e8a9b8', '#f1efe4'):
        x, y = rnd.randint(1, 9), rnd.randint(1, 9)
        d.rectangle((x, y, x + rnd.randint(3, 5), y + rnd.randint(3, 5)), fill=shade(colour, 0))
        for yy in range(y + 1, y + 4, 2):
            d.line((x + 1, yy, x + 3, yy), fill=shade('#7d7a70', 0))
        d.point((x + 1, y), fill=shade('#c0392b', 0))
    return im


def poster():
    im = new(base='#2b3a6e', seed=101, spread=4); d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=shade('#e8e4d6', 0))
    for x, y in ((3, 3), (12, 2), (8, 5), (2, 9), (13, 10)):
        d.point((x, y), fill=shade('#f4e27a', 0))
    d.rectangle((3, 11, 12, 13), fill=shade('#e86b8f', 0)); d.line((4, 12, 11, 12), fill=shade('#fff2f6', 0))
    d.ellipse((5, 4, 10, 9), outline=shade('#f4e27a', 0))
    return im


def clock():
    im = new(); d = ImageDraw.Draw(im)
    d.ellipse((1, 1, 14, 14), fill=shade('#2a2a2a', 0)); d.ellipse((2, 2, 13, 13), fill=shade('#f3f1ea', 0))
    for a in range(12):
        x = 7.5 + math.cos(a * math.pi / 6) * 4.6; y = 7.5 + math.sin(a * math.pi / 6) * 4.6
        d.point((round(x), round(y)), fill=shade('#2a2a2a', 0))
    d.line((7, 7, 7, 4), fill=shade('#1b1b1b', 0)); d.line((7, 7, 10, 9), fill=shade('#1b1b1b', 0)); d.line((7, 8, 5, 11), fill=shade('#b02a22', 0))
    return im


def steel(seed=111):
    im = new(base='#a9aeb0', seed=seed, spread=6); px = im.load()
    for y in range(16):
        k = int(6 * math.sin(y * .9))
        for x in range(16):
            px[x, y] = shade(px[x, y], k)
    return im


def fluorescent(lit):
    im = new(base='#e9eae4' if lit else '#a9aaa4', seed=121 + lit, spread=2); d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=shade('#b9bab4' if lit else '#8a8b85', 0))
    for y in (5, 10):
        d.line((1, y, 14, y), fill=shade('#ffffff' if lit else '#8f908a', 0))
    if not lit:
        rnd = random.Random(123); px = im.load()
        for _ in range(6):
            px[rnd.randint(2, 13), rnd.randint(2, 13)] = shade('#3a3a36', 0)
    return im


FONT = {'G': ['111', '100', '101', '101', '111'], 'I': ['1', '1', '1', '1', '1'], 'R': ['110', '101', '110', '101', '101'],
        'L': ['10', '10', '10', '10', '11'], 'S': ['111', '100', '111', '001', '111']}


def plaque():
    im = new(); d = ImageDraw.Draw(im)
    d.rectangle((0, 4, 15, 11), fill=shade('#5a3a25', 0)); d.line((0, 4, 15, 4), fill=shade('#7a5238', 0))
    x = 0
    for ch in 'GIRLS':
        rows = FONT[ch]
        for y, row in enumerate(rows):
            for dx, bit in enumerate(row):
                if bit == '1':
                    d.point((x + dx, 5 + y), fill=shade('#efe9dc', 0))
        x += len(rows[0]) + 1
    return im


def porcelain(seed=131):
    im = new(base='#ecebe5', seed=seed, spread=3); px = im.load()
    for y in range(16):
        for x in range(16):
            if x + y < 6:
                px[x, y] = shade(px[x, y], 7)
    return im


def partition(seed, door=False):
    im = new(base='#c9bd94', seed=seed, spread=4); d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=shade('#a59970', 0)); d.line((1, 1, 14, 1), fill=shade('#ddd2ad', 0))
    rnd = random.Random(seed + 2)
    for _ in range(2):
        x, y = rnd.randint(2, 11), rnd.randint(3, 12); d.line((x, y, x + rnd.randint(1, 3), y + rnd.randint(-1, 1)), fill=shade('#5a4d7a', 0))
    if door:
        d.rectangle((1, 7, 2, 9), fill=shade('#c9cbc6', 0))
    return im


def stall_door(upper):
    im = partition(141 + upper, door=not upper); d = ImageDraw.Draw(im)
    if upper:
        d.line((7, 4, 9, 4), fill=shade('#9ea0a0', 0)); d.point((8, 5), fill=shade('#9ea0a0', 0))
        # A heart scratched into the paint with two sets of initials.
        for x, y in ((4, 9), (6, 9), (3, 10), (5, 10), (7, 10), (4, 11), (6, 11), (5, 12)):
            d.point((x, y), fill=shade('#8a7c52', 0))
    return im


def bathmat():
    im = new(base='#8b6c7c', seed=151, spread=6); px = im.load(); rnd = random.Random(152)
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 8.5)
            if r < 4.5 and rnd.random() < .7:
                px[x, y] = shade('#a68e98', rnd.randint(-5, 5))
            if x in (0, 15) or y in (0, 15):
                px[x, y] = shade('#6b4f5d', rnd.randint(-6, 6)) if rnd.random() < .8 else (0, 0, 0, 0)
    return im


def window():
    im = new(base='#151a24', seed=161, spread=4); d = ImageDraw.Draw(im)
    rnd = random.Random(162); px = im.load()
    for _ in range(40):
        x, y = rnd.randint(1, 14), rnd.randint(1, 14); px[x, y] = shade('#2a3140', rnd.randint(-4, 6))
    d.rectangle((0, 0, 15, 15), outline=shade('#8c9093', 0)); d.line((1, 8, 14, 8), fill=shade('#8c9093', 0))
    d.point((11, 4), fill=shade('#5d6676', 0)); d.point((12, 4), fill=shade('#4a5262', 0))
    return im


def note():
    im = new(); d = ImageDraw.Draw(im)
    d.rectangle((5, 5, 10, 10), fill=shade('#f2f0e8', 0)); d.line((5, 5, 10, 5), fill=shade('#d9d5c8', 0))
    for y in (7, 9):
        d.line((5, y, 10, y), fill=shade('#a9c2db', 0))
    d.line((6, 5, 6, 10), fill=shade('#e09a9a', 0))
    for x, y in ((7, 6), (8, 6), (9, 8), (7, 8), (8, 10)):
        d.point((x, y), fill=shade('#6a2fa0', 0))
    d.line((5, 10, 10, 5), fill=shade('#cfcabb', 0))
    return im


def sheet_floor(i):
    im = new(base='#ece8dc' if i == 0 else '#e9e1da', seed=171 + i, spread=5); px = im.load(); rnd = random.Random(173 + i)
    for _ in range(14):
        x, y = rnd.randint(0, 14), rnd.randint(0, 14); l = rnd.randint(2, 6)
        for k in range(l):
            if 0 <= x + k < 16:
                px[x + k, (y + k // 2) % 16] = shade(px[x + k, (y + k // 2) % 16], -16)
    for _ in range(7):
        x, y = rnd.randint(1, 14), rnd.randint(1, 14)
        px[x, y] = shade('#7d9cc9', 0) if i == 0 else shade('#d79aa8', 0)
    for x, y in ((5, 6), (6, 6), (9, 6), (10, 6), (5, 7), (10, 7)):
        px[x, y] = shade('#2b2622', 0)
    return im


def mirror(col, row, cracked):
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(181 + col * 2 + row * 17 + cracked * 99)
    for y in range(16):
        for x in range(16):
            gx, gy = col * 16 + x, (1 - row) * 16 + y
            t = (gx + gy * .6) / 80
            base = (int(52 + 22 * t), int(60 + 22 * t), int(66 + 22 * t))
            k = rnd.randint(-2, 2) + (8 if (gx - gy * 1.3) % 37 < 2 else 0)
            px[x, y] = shade(base, k)
    frame = shade('#9aa1a4', 0); dark = shade('#5d6366', 0)
    if col == 0:
        for y in range(16):
            px[0, y] = frame
    if col == 3:
        for y in range(16):
            px[15, y] = dark
    if row == 1:
        for x in range(16):
            px[x, 0] = frame
    if row == 0:
        for x in range(16):
            px[x, 15] = dark
    if cracked:
        d = ImageDraw.Draw(im)
        cx, cy = 32 - col * 16, 18 - (1 - row) * 16
        for a in range(9):
            ang = a * math.pi * 2 / 9 + .3; r0, r1 = 2, 40
            d.line((cx + math.cos(ang) * r0, cy + math.sin(ang) * r0, cx + math.cos(ang) * r1, cy + math.sin(ang) * r1), fill=shade('#d6dde0', 0))
        for r in (6, 13, 21):
            d.ellipse((cx - r, cy - r, cx + r, cy + r), outline=shade('#b7c0c4', 0))
        for _ in range(20):
            x, y = rnd.randint(1, 14), rnd.randint(1, 14)
            if rnd.random() < .35:
                px[x, y] = shade('#1c2024', 0)
    return im


def door(name):
    halves = {}
    for upper in (0, 1):
        if name == 'bathroom':
            im = new(base='#5b3a23', seed=191 + upper, spread=5); d = ImageDraw.Draw(im); rnd = random.Random(193 + upper); px = im.load()
            for x in range(16):
                for y in range(16):
                    if (x * 3 + y // 5 + rnd.randint(0, 2)) % 7 == 0:
                        px[x, y] = shade('#4a2e1b', rnd.randint(-3, 3))
            d.line((0, 0, 0, 15), fill=shade('#3a2414', 0)); d.line((15, 0, 15, 15), fill=shade('#3a2414', 0))
            if upper:
                d.rectangle((10, 6, 13, 13), fill=shade('#a8acaa', 0)); d.rectangle((10, 6, 13, 13), outline=shade('#7d817f', 0))
            else:
                d.rectangle((1, 11, 14, 15), fill=shade('#a8acaa', 0)); d.line((1, 11, 14, 11), fill=shade('#d0d3d1', 0))
                d.rectangle((11, 1, 13, 2), fill=shade('#c9cbc6', 0)); d.point((11, 3), fill=shade('#c9cbc6', 0))
        else:
            im = new(base='#4c6a4f', seed=195 + upper, spread=4); d = ImageDraw.Draw(im)
            d.rectangle((0, 0, 15, 15), outline=shade('#33473a', 0))
            if upper:
                d.rectangle((4, 2, 11, 12), fill=shade('#5b6e7d', 0)); d.rectangle((4, 2, 11, 12), outline=shade('#a0a5a2', 0))
                for k in range(-8, 12, 3):
                    d.line((4, 2 + k, 11, 9 + k), fill=shade('#3f4c56', 0)); d.line((11, 2 + k, 4, 9 + k), fill=shade('#3f4c56', 0))
                d.rectangle((4, 2, 11, 12), outline=shade('#a0a5a2', 0))
            else:
                d.rectangle((2, 4, 13, 5), fill=shade('#b9bcb8', 0)); d.rectangle((2, 4, 13, 5), outline=shade('#7a7e7a', 0))
                d.line((1, 14, 14, 14), fill=shade('#33473a', 0))
        halves['upper' if upper else 'lower'] = im
    return halves


def candle_item():
    im = new(); d = ImageDraw.Draw(im)
    d.rectangle((5, 8, 10, 14), fill=shade('#efe8d6', 0)); d.line((5, 8, 5, 14), fill=shade('#fffaf0', 0)); d.line((10, 8, 10, 14), fill=shade('#cbbfa5', 0))
    d.point((6, 9), fill=shade('#fffaf0', 0)); d.line((8, 8, 8, 10), fill=shade('#fffaf0', 0)); d.point((10, 11), fill=shade('#e4dac2', 0))
    d.rectangle((4, 14, 11, 15), fill=shade('#8a8b86', 0))
    d.line((7, 6, 7, 7), fill=shade('#2a2420', 0))
    d.point((7, 5), fill=shade('#ffd36a', 0)); d.point((7, 4), fill=shade('#ffefa8', 0)); d.point((7, 3), fill=shade('#ffb347', 0)); d.point((8, 5), fill=shade('#ff9a3c', 0)); d.point((6, 5), fill=shade('#ff9a3c', 0))
    return im


def flame():
    im = Image.new('RGBA', (8, 16)); px = im.load()
    for y in range(16):
        w = 3.4 * math.sin(math.pi * min(1, (16 - y) / 15)) ** .7
        for x in range(8):
            r = abs(x - 3.5)
            if r <= w:
                t = r / max(w, .01); hgt = (15 - y) / 15
                if y >= 13:
                    px[x, y] = (90, 120, 255, int(150 * (1 - t)))
                elif t < .35 and hgt < .7:
                    px[x, y] = (255, 250, 220, 255)
                elif t < .7:
                    px[x, y] = (255, 205, 90, 235)
                else:
                    px[x, y] = (255, 140, 40, 170)
    return im


# ---------------------------------------------------------------------------------------------------- figures
def sheet_entity(i):
    """A bedsheet over a kid: the UVs of client.DareRenderers.SheetModel (64 by 64)."""
    base = '#ece8dc' if i == 0 else '#ebe3dc'
    im = Image.new('RGBA', (64, 64)); noise(im, base, 201 + i, 4, (0, 0, 64, 48)); px = im.load(); rnd = random.Random(203 + i)
    for _ in range(90):
        x, y = rnd.randint(0, 50), rnd.randint(0, 47); l = rnd.randint(2, 5)
        for k in range(l):
            if y + k < 48:
                px[x, y + k] = shade(px[x, y + k], -11)
    for _ in range(26):
        x, y = rnd.randint(0, 50), rnd.randint(0, 47)
        if i == 0:
            px[x, y] = shade('#7f9fcb', 0)
        else:
            px[x, y] = shade('#d898a8', 0)
            if x + 1 < 64:
                px[x + 1, y] = shade('#9fbf8a', 0)
    # The face of the head box (texOffs 0,0, nine wide, eight high, nine deep): the front is u 9..18, v 9..17.
    for x, y in ((11, 12), (12, 12), (11, 13), (12, 13), (15, 12), (16, 12), (15, 13), (16, 13)):
        px[x, y] = shade('#1d1916', 0)
    px[12, 14] = shade(base, -18); px[15, 14] = shade(base, -18)
    # The hem (texOffs 0,32: thirteen wide, six high, ten deep): its lowest row frays.
    for x in range(0, 46):
        if rnd.random() < .5:
            px[x, 47] = shade(base, -26)
        elif rnd.random() < .3:
            px[x, 47] = (0, 0, 0, 0)
    # Sneakers at (46,0) and (46,6).
    shoe = '#e8a4b8' if i == 0 else '#8d9196'
    for y0 in (0, 6):
        for y in range(y0, y0 + 6):
            for x in range(46, 59):
                px[x, y] = shade(shoe, rnd.randint(-4, 4))
        for x in range(46, 59):
            px[x, y0 + 5] = shade('#f1efe8', 0)
    return im


def woman():
    """The woman in the glass, on the slim player layout: pale and wet, eyes open on the reader, the hair in strands."""
    im = Image.new('RGBA', (64, 64)); px = im.load(); rnd = random.Random(211)
    skin, shadow, hair, hairlit, slip, damp = '#b8bcb1', '#8f948a', '#121314', '#2c3136', '#cdccc2', '#989b92'

    def fill(x0, y0, x1, y1, colour, spread=4):
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[x, y] = shade(colour, rnd.randint(-spread, spread))
    # Head (0..32, 0..16): skin everywhere, then the face.
    fill(0, 0, 32, 16, skin)
    fill(8, 0, 24, 8, hair)  # top and bottom of the head
    for x in range(8, 16):
        px[x, 8] = shade(hair, 0)
    # The face, front at (8..16, 8..16): sunken sockets, pale eyes looking out, the mouth a little open.
    for x in (9, 10, 13, 14):
        px[x, 11] = shade(shadow, -12)
        px[x, 13] = shade(shadow, -6)
    for x, eye in ((9, 0), (10, 1), (13, 1), (14, 0)):
        px[x, 12] = shade('#1a1b1d', 0) if eye else shade('#d6d9d3', 0)
    px[11, 12] = shade(shadow, -4); px[12, 12] = shade(shadow, -4)
    px[11, 15] = shade('#2a1c1e', 0); px[12, 15] = shade('#2a1c1e', 0); px[11, 14] = shade(shadow, -10); px[12, 14] = shade(shadow, -10)
    # Back and sides of the head are hair.
    fill(0, 8, 8, 16, hair); fill(16, 8, 32, 16, hair)
    for x in range(16, 24):
        px[x, 9 + rnd.randint(0, 2)] = shade(hairlit, 0)
    # Hat layer (32..64, 0..16): wet strands over the crown and down, framing and crossing the face.
    for x in range(40, 48):
        for y in range(0, 8):
            px[x, y] = shade(hair, rnd.randint(-3, 3))
    for x in range(32, 64):
        for y in range(8, 16):
            u = x - 32
            if 8 <= u < 16:
                c = u - 8
                strand = c in (0, 1, 6, 7) or (c in (2, 5) and y < 10 + (c % 3)) or (c == 4 and y < 9)
            else:
                strand = rnd.random() < .85
            if strand:
                px[x, y] = shade(hairlit if rnd.random() < .18 else hair, rnd.randint(-3, 3))
    # Body (16..40, 16..32): the slip, darkened where it is wet.
    fill(16, 16, 40, 32, slip)
    for _ in range(40):
        x, y = rnd.randint(16, 39), rnd.randint(20, 31); px[x, y] = shade(damp, rnd.randint(-4, 4))
    for x in (21, 26):
        px[x, 20] = shade(skin, 0); px[x, 21] = shade(skin, 0)
    fill(20, 20, 28, 21, skin)
    # Slim arms (right 40..54, 16..32; left 32..46, 48..64): bare, the fingertips dark.
    fill(40, 16, 56, 32, skin); fill(32, 48, 48, 64, skin)
    for x in range(40, 54):
        px[x, 31] = shade('#3a3437', 0)
    for x in range(32, 46):
        px[x, 63] = shade('#3a3437', 0)
    for x in range(44, 51):
        px[x, 20] = shade(shadow, 0)
    # Legs (right 0..16, 16..32; left 16..32, 48..64): the slip to the knee, bare below, dirty feet.
    for x0, y0 in ((0, 16), (16, 48)):
        fill(x0, y0, x0 + 16, y0 + 10, slip); fill(x0, y0 + 10, x0 + 16, y0 + 16, skin)
        for x in range(x0, x0 + 16):
            px[x, y0 + 15] = shade('#5a524c', rnd.randint(-5, 5))
    # Jacket layer (16..40, 32..48): her hair down her back over the slip.
    for x in range(32, 40):
        for y in range(36, 44):
            if rnd.random() < .8 - (y - 36) * .07:
                px[x, y] = shade(hair, rnd.randint(-3, 3))
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


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def front_cube(front, side):
    return model([element([0, 0, 0], [16, 16, 16], {'all': 'side', 'north': 'front'})], {'front': front, 'side': side}, side)


def plate(texture, x0=1, y0=1, x1=15, y1=15, depth=1):
    return model([element([x0, y0, 16 - depth], [x1, y1, 16], {'all': 'tex', 'north': 'tex'}, uv={'north': [x0, 16 - y1, x1, 16 - y0]})], {'tex': texture}, texture)


def fitting_models():
    m = {}
    for stage in (0, 1):
        half = 'upper' if stage else 'lower'
        m[f'locker_{stage}'] = front_cube('locker_' + half, 'wall_band_side')
        m[f'classroom_door_{stage}'] = front_cube('classroom_' + half, 'wall_band_side')
        m[f'trophy_{stage}'] = front_cube('trophy_' + half, 'wall_band_side')
        m[f'bulletin_{stage}'] = plate(f'bulletin_{stage}', 0, 1, 16, 15)
        lo, hi = (2, 16) if stage == 0 else (0, 12)
        m[f'partition_{stage}'] = model([element([7, lo, 0], [9, hi, 16], {'all': 'p'})], {'p': f'partition'}, 'partition')
        m[f'stall_panel_{stage}'] = model([element([0, lo, 7], [16, hi, 9], {'all': 'p'})], {'p': 'partition'}, 'partition')
        door_tex = 'stall_door_' + half
        m[f'stall_door_{stage}'] = model([element([0, lo, 7], [16, hi, 9], {'all': 'p', 'north': 'd', 'south': 'd'})], {'p': 'partition', 'd': door_tex}, 'partition')
        ajar = {'angle': -22.5, 'axis': 'y', 'origin': [0, 8, 8]}
        m[f'stall_door_{stage + 2}'] = model([element([0, lo, 7], [16, hi, 9], {'all': 'p', 'north': 'd', 'south': 'd'}, ajar)], {'p': 'partition', 'd': door_tex}, 'partition')
        m[f'sheet_{stage}'] = model([element([1, 0, 1], [15, 2, 15], {'all': 's'}), element([3, 2, 2], [12, 4, 11], {'all': 's'}, {'angle': 22.5, 'axis': 'y', 'origin': [8, 2, 8]}),
                                     element([8, 2, 7], [14, 3.5, 14], {'all': 's'})], {'s': f'sheet_{stage}'}, f'sheet_{stage}')
    m['poster_0'] = plate('poster', 2, 0, 14, 16)
    m['clock_0'] = plate('clock', 1, 1, 15, 15)
    m['plaque_0'] = plate('plaque', 0, 4, 16, 12)
    m['window_0'] = plate('window', 0, 0, 16, 16)
    m['fountain_0'] = model([element([3, 10, 7], [13, 13, 16], {'all': 's'}), element([5, 6, 11], [11, 10, 16], {'all': 's'}), element([7, 13, 9], [8, 14, 10], {'all': 's'})], {'s': 'steel'}, 'steel')
    m['hall_light_0'] = model([element([2, 15, 2], [14, 16, 14], {'all': 'l'})], {'l': 'hall_light'}, 'hall_light')
    m['bath_light_0'] = model([element([2, 15, 2], [14, 16, 14], {'all': 'l'})], {'l': 'bath_light'}, 'bath_light')
    m['sink_0'] = model([element([1, 8, 9], [15, 14, 16], {'all': 'p', 'up': 'basin'}), element([6, 0, 13], [10, 8, 16], {'all': 'c'}), element([7, 14, 13], [9, 16, 15], {'all': 'c'}),
                         element([7.5, 15, 10.5], [8.5, 16, 13], {'all': 'c'})], {'p': 'porcelain', 'basin': 'basin', 'c': 'steel'}, 'porcelain')
    m['toilet_0'] = model([element([4, 0, 6], [12, 6, 15], {'all': 'p'}), element([3, 6, 4], [13, 9, 15], {'all': 'p', 'up': 'seat'}), element([3, 9, 13], [13, 15, 16], {'all': 'p'}),
                           element([12, 12, 14], [13.5, 13, 15], {'all': 'c'})], {'p': 'porcelain', 'seat': 'seat', 'c': 'steel'}, 'porcelain')
    m['bathmat_0'] = model([element([1, 0, 1], [15, .5, 15], {'all': 'm'})], {'m': 'bathmat'}, 'bathmat')
    m['towels_0'] = model([element([3, 3, 12], [13, 14, 16], {'all': 's', 'north': 'towels'})], {'s': 'steel', 'towels': 'towels'}, 'steel')
    m['bin_0'] = model([element([4, 0, 4], [12, 11, 12], {'all': 's', 'up': 'bin_top'})], {'s': 'steel', 'bin_top': 'bin_top'}, 'steel')
    m['note_0'] = model([element([5, 0, 5], [11, .5, 11], {'all': 'n'}, uv={'up': [5, 5, 11, 11], 'down': [5, 5, 11, 11]})], {'n': 'note'}, 'note')
    return m


def textures():
    out = {}
    mats = materials()
    for k, v in mats.items():
        out[f'block/dare/{k}.png'] = v
    side = mats['wall_band'].copy()
    out['block/dare/wall_band_side.png'] = side
    for upper in (0, 1):
        half = 'upper' if upper else 'lower'
        out[f'block/dare/locker_{half}.png'] = locker(upper)
        out[f'block/dare/classroom_{half}.png'] = classroom(upper)
        out[f'block/dare/trophy_{half}.png'] = trophy(upper)
        out[f'block/dare/stall_door_{half}.png'] = stall_door(upper)
        out[f'block/dare/bulletin_{upper}.png'] = bulletin(upper)
        out[f'block/dare/sheet_{upper}.png'] = sheet_floor(upper)
    out['block/dare/poster.png'] = poster(); out['block/dare/clock.png'] = clock(); out['block/dare/plaque.png'] = plaque()
    out['block/dare/steel.png'] = steel(); out['block/dare/hall_light.png'] = fluorescent(1); out['block/dare/bath_light.png'] = fluorescent(0)
    out['block/dare/porcelain.png'] = porcelain()
    basin = porcelain(133); d = ImageDraw.Draw(basin); d.ellipse((3, 3, 12, 12), fill=shade('#c9c8c2', 0)); d.ellipse((7, 7, 8, 8), fill=shade('#5d605e', 0)); out['block/dare/basin.png'] = basin
    seat = porcelain(135); d = ImageDraw.Draw(seat); d.ellipse((3, 2, 12, 13), outline=shade('#cfcec7', 0)); d.ellipse((5, 4, 10, 11), fill=shade('#b9c4c9', 0)); out['block/dare/seat.png'] = seat
    out['block/dare/partition.png'] = partition(139)
    out['block/dare/bathmat.png'] = bathmat(); out['block/dare/window.png'] = window(); out['block/dare/note.png'] = note()
    towels = steel(113); d = ImageDraw.Draw(towels); d.rectangle((4, 10, 11, 12), fill=shade('#2a2c2d', 0)); d.rectangle((5, 12, 10, 14), fill=shade('#e8e4d4', 0)); out['block/dare/towels.png'] = towels
    top = steel(115); d = ImageDraw.Draw(top); d.rectangle((2, 2, 13, 13), fill=shade('#3a3c3d', 0)); d.rectangle((4, 4, 8, 7), fill=shade('#e8e4d4', 0)); d.rectangle((8, 8, 11, 10), fill=shade('#d9d2b9', 0)); out['block/dare/bin_top.png'] = top
    for row in (0, 1):
        for col in range(4):
            out[f'block/dare/mirror_{col + row * 4}.png'] = mirror(col, row, False)
            out[f'block/dare/mirror_cracked_{col + row * 4}.png'] = mirror(col, row, True)
    for name in ('bathroom', 'stair'):
        for half, im in door(name).items():
            out[f'block/dare/{name}_door_{half}.png'] = im
    out['item/candle_stub.png'] = candle_item()
    out['entity/dare_sheet_a.png'] = sheet_entity(0); out['entity/dare_sheet_b.png'] = sheet_entity(1)
    out['entity/dare_woman.png'] = woman(); out['entity/dare_flame.png'] = flame()
    return out


KINDS = ['locker', 'classroom_door', 'trophy', 'bulletin', 'poster', 'clock', 'fountain', 'hall_light', 'bath_light', 'plaque',
         'sink', 'partition', 'stall_panel', 'stall_door', 'toilet', 'bathmat', 'towels', 'bin', 'window', 'note', 'sheet']
STAGES = {'locker': 2, 'classroom_door': 2, 'trophy': 2, 'partition': 2, 'stall_panel': 2, 'bulletin': 2, 'sheet': 2, 'stall_door': 4}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {}
    for name in ('wall', 'wall_band', 'hall_tile', 'ceiling', 'bath_floor', 'bath_wall', 'trim'):
        out[f'models/block/dare_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/dare_{name}.json'] = {'variants': {'': {'model': NS + f'block/dare_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/dare/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/dare/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/dare_fitting.json'] = {'variants': variants}
    mirror_variants = {}
    for part in range(8):
        for cracked in (False, True):
            name = f'mirror_{"cracked_" if cracked else ""}{part}'
            out[f'models/block/dare/{name}.json'] = model([element([0, 0, 15.5], [16, 16, 16], {'all': 'side', 'north': 'glass'})], {'glass': name, 'side': 'steel'}, name)
            for facing, y in FACING_Y.items():
                v = {'model': NS + f'block/dare/{name}'}
                if y:
                    v['y'] = y
                mirror_variants[f'facing={facing},part={part},shattered={str(cracked).lower()}'] = v
    out['blockstates/dare_mirror.json'] = {'variants': mirror_variants}
    for name in ('bathroom', 'stair'):
        door_variants = {}
        for half, vanilla in (('lower', 'bottom'), ('upper', 'top')):
            for hinge in ('left', 'right'):
                for opened in ('', '_open'):
                    out[f'models/block/dare/{name}_door_{half}_{hinge}{opened}.json'] = {'parent': f'minecraft:block/door_{vanilla}_{hinge}{opened}', 'render_type': 'minecraft:cutout',
                                                                                          'textures': {'top': P + f'{name}_door_upper', 'bottom': P + f'{name}_door_lower'}}
        for facing, y in (('east', 0), ('south', 90), ('west', 180), ('north', 270)):
            for half in ('lower', 'upper'):
                for hinge in ('left', 'right'):
                    for opened in (False, True):
                        turn = y + (90 if hinge == 'left' else -90) if opened else y
                        door_variants[f'facing={facing},half={half},hinge={hinge},open={str(opened).lower()},powered=false'] = {'model': NS + f'block/dare/{name}_door_{half}_{hinge}{"_open" if opened else ""}', 'y': turn % 360}
                        door_variants[f'facing={facing},half={half},hinge={hinge},open={str(opened).lower()},powered=true'] = {'model': NS + f'block/dare/{name}_door_{half}_{hinge}{"_open" if opened else ""}', 'y': turn % 360}
        out[f'blockstates/dare_{name}_door.json'] = {'variants': door_variants}
    out['models/item/candle_stub.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/candle_stub'}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
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
    print(('Verified' if check else 'Drew') + f' the third floor: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
