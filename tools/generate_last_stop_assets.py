#!/usr/bin/env python3
"""The Last Stop (0.4.78): the elk lot's roadside bar, highway, lot, field and pickups.

Environment textures are vanilla-style 16x16 pixel art drawn here (small palettes, one-pixel bevels and seams, scattered
single-pixel noise; nothing sampled). The pickups' body panels are drawn in greys and painted by the block tint; their
glass, chrome, tyres and lights are drawn in colour on separate overlay elements. Four original synthesized sounds get
subtitles.

The bar's five designed skins (0.4.79) each have their own literary_bar_<name>.png file.
The placeholder helper only supplies a missing file; it never overwrites the shipped character art.

Run from anywhere:
    python3 tools/generate_last_stop_assets.py
"""
import json
import math
import random
import shutil
from pathlib import Path

import numpy as np
import soundfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
ASSETS = ROOT / 'assets/the_oldest_house'
TEX = ASSETS / 'textures/block/last_stop'
MODELS = ASSETS / 'models/block'
NS = 'the_oldest_house:block/last_stop/'
RATE = 22050


# ------------------------------------------------------------------------------------------------ drawing
def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


def mix(a, b, f):
    return clamp(tuple(a[i] * (1 - f) + b[i] * f for i in range(3)))


def canvas(w=16, h=16, color=(0, 0, 0, 0)):
    return Image.new('RGBA', (w, h), color)


def px(img, x, y, c, a=255):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), clamp(c) + (a,))


def noisy(base, seed, spread=(-.06, -.03, 0, 0, 0, .03, .05)):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(base, 1 + rng.choice(spread)))
    return img


def asphalt(seed=780):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            r = rng.random()
            c = (54, 54, 57) if r < .62 else (46, 46, 49) if r < .8 else (66, 65, 66) if r < .93 else (84, 82, 80)
            px(img, x, y, c)
    # A fine crack across one corner.
    for x, y in ((11, 2), (12, 3), (12, 4), (13, 5), (14, 5)):
        px(img, x, y, (34, 34, 36))
    return img


def road_line(kind):
    img = asphalt(781 if kind == 'edge' else 782)
    rng = random.Random(783)
    rows = (6, 7, 8, 9) if kind == 'edge' else (4, 5, 10, 11)
    paint = (214, 214, 206) if kind == 'edge' else (212, 166, 44)
    for y in rows:
        for x in range(16):
            worn = rng.random()
            if worn < .12:
                continue  # worn through to the asphalt
            px(img, x, y, mul(paint, .82 if worn < .3 else 1.0))
    return img


GRAVEL_STONES = [(122, 118, 112), (104, 100, 96), (138, 132, 124), (92, 88, 84), (118, 104, 88), (150, 146, 140), (80, 78, 76)]


def gravel(variant):
    rng = random.Random(790 + variant)
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = rng.choice(GRAVEL_STONES)
            if variant == 3:
                c = mul(c, 1.12)
            px(img, x, y, c)
    for _ in range(14):  # single brighter stones with a shadow under them
        x, y = rng.randrange(15), rng.randrange(15)
        px(img, x, y, (168, 164, 156))
        px(img, x + 1, y + 1, (70, 68, 66))
    if variant == 1:  # an old oil stain
        for y in range(16):
            for x in range(16):
                d = math.hypot((x - 8.5) / 1.2, y - 7.5)
                if d < 5.2 + rng.random() * 1.2:
                    px(img, x, y, mul(img.getpixel((x, y)), .42 if d < 3.5 else .6))
    if variant == 2:  # a puddle reflecting nothing much
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - 7.5, (y - 8.5) * 1.4)
                if d < 4.6:
                    px(img, x, y, (40, 46, 58) if d < 3.6 else (58, 62, 70))
        for x, y in ((6, 7), (7, 7), (9, 9)):
            px(img, x, y, (92, 100, 116))
    if variant == 3:  # packed shoulder with a tyre track
        for y in range(16):
            for x in (4, 5, 11, 12):
                px(img, x, y, mul(img.getpixel((x, y)), .78))
    return img


def boards():
    """Weathered board and batten: three boards, a batten over each joint, nail heads, grey-brown and dry."""
    rng = random.Random(800)
    base = (108, 90, 72)
    img = canvas()
    for x in range(16):
        board = x // 5
        tone = (1.0, .93, 1.05)[board % 3]
        for y in range(16):
            f = tone + rng.choice((0, 0, -.04, .03, -.07))
            if (x + y * 3 + board * 7) % 11 == 0:
                f -= .1  # grain
            px(img, x, y, mul(base, f))
    for x in (4, 9, 14):  # battens
        for y in range(16):
            px(img, x, y, mul(base, .78 if y % 8 else .6))
            px(img, x + 1 if x < 15 else x, y, mul(base, .7))
    for x in (4, 9, 14):
        for y in (2, 10):
            px(img, x, y, (60, 56, 52))
    return img


def wood(base, seed, horizontal=True, seams=4):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            u, v = (x, y) if horizontal else (y, x)
            f = 1 + rng.choice((0, 0, 0, -.04, .04))
            if v % seams == seams - 1:
                f = .74
            elif v % seams == 0:
                f += .07
            if (u + v * 5) % 13 == 0:
                f -= .06
            px(img, x, y, mul(base, f))
    return img


MAHOGANY = (104, 52, 34)
DARK = (58, 38, 28)


def counter_front():
    img = wood(MAHOGANY, 810, horizontal=False, seams=3)
    for x in range(16):
        px(img, x, 0, mul(MAHOGANY, 1.25))
        px(img, x, 12, (220, 182, 86))  # the brass foot rail
        px(img, x, 13, (164, 128, 52))
        px(img, x, 15, (30, 22, 18))  # kick plate
        px(img, x, 14, (44, 32, 24))
    for x in (0, 8):
        px(img, x, 12, (250, 220, 140))
    return img


def counter_top():
    img = wood(mul(MAHOGANY, 1.12), 811, horizontal=True, seams=8)
    for x in range(16):
        px(img, x, 0, mul(MAHOGANY, 1.4))
    for x, y in ((4, 5), (5, 5), (11, 10)):  # glass rings
        px(img, x, y, mul(MAHOGANY, .82))
    return img


def counter_back():
    """The bartender's side: two shelves of upturned glasses under the top."""
    img = wood(DARK, 812, horizontal=True, seams=16)
    for y in (6, 14):
        for x in range(16):
            px(img, x, y, mul(DARK, 1.35))
    for x0 in (1, 5, 9, 13):
        for row in (3, 11):
            for y in range(row, row + 3):
                for x in range(x0, x0 + 2):
                    px(img, x, y, (190, 214, 216) if y > row else (226, 240, 240))
    return img


def cabinet():
    img = wood(DARK, 813, horizontal=False, seams=16)
    for door in (0, 8):
        for x in range(door + 1, door + 7):
            for y in (1, 14):
                px(img, x, y, mul(DARK, .7))
        for y in range(1, 15):
            px(img, door + 1, y, mul(DARK, 1.3))
            px(img, door + 6, y, mul(DARK, .7))
        px(img, door + (5 if door == 0 else 2), 8, (210, 176, 90))
    return img


BOTTLES = [(54, 120, 62), (152, 92, 30), (196, 218, 212), (120, 40, 34), (70, 140, 150), (176, 140, 60)]


def shelf_bottles():
    """Two shelves of bottles on a dark back: greens, ambers, clear, a red label here and there."""
    rng = random.Random(820)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(DARK, .8 + rng.choice((0, .03, -.03))))
    for shelf in (7, 15):
        for x in range(16):
            px(img, x, shelf, mul(MAHOGANY, 1.2))
        x = 0
        while x < 15:
            c = rng.choice(BOTTLES)
            h = rng.choice((4, 5, 6))
            for y in range(shelf - h, shelf):
                for dx in (0, 1):
                    px(img, x + dx, y, mul(c, 1.25 if dx == 0 else .9) if y > shelf - h + 1 or dx == 0 else c)
            px(img, x, shelf - h - 1, mul(c, .7))  # the neck
            if rng.random() < .5:
                px(img, x, shelf - 3, (226, 222, 200))
                px(img, x + 1, shelf - 3, (196, 60, 48))
            x += rng.choice((2, 3))
    return img


def mirror():
    img = canvas()
    for y in range(16):
        for x in range(16):
            f = .9 + .1 * math.sin((x + y) * .5)
            px(img, x, y, mul((150, 162, 168), f))
    for i in range(16):  # frame
        for c in ((i, 0), (i, 15), (0, i), (15, i)):
            px(img, *c, mul(MAHOGANY, 1.2))
    # A painted oval and a word too small to read.
    for y in range(4, 12):
        for x in range(3, 13):
            if ((x - 7.5) / 4.8) ** 2 + ((y - 7.5) / 3.6) ** 2 < 1:
                px(img, x, y, (172, 44, 40))
    for x in range(5, 11):
        if x != 8:
            px(img, x, 7, (240, 230, 210))
            px(img, x, 8, (232, 220, 196))
    for x, y in ((2, 2), (3, 3), (12, 12), (13, 11)):
        px(img, x, y, (212, 222, 226))
    return img


def vinyl():
    """Oxblood vinyl, buttoned into diamonds."""
    img = canvas()
    base = (128, 30, 34)
    for y in range(16):
        for x in range(16):
            d = min((x - 4) % 8, (y - 4) % 8)
            f = 1.0 + (.1 if (x + y) % 8 < 2 else 0) - (.12 if (x - y) % 8 == 0 or (x + y) % 8 == 0 else 0)
            px(img, x, y, mul(base, f))
    for x, y in ((4, 4), (12, 4), (8, 8), (4, 12), (12, 12), (0, 8), (8, 0)):
        px(img, x, y, (60, 14, 16))
    return img


def formica():
    """Mint formica with the boomerangs of the year it was bought."""
    rng = random.Random(830)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((176, 214, 198), 1 + rng.choice((0, 0, .02, -.02))))
    for (x, y, c) in ((2, 3, (230, 120, 90)), (9, 5, (80, 120, 150)), (5, 11, (230, 190, 80)), (12, 12, (230, 120, 90))):
        for dx, dy in ((0, 0), (1, 0), (2, 1), (-1, 1)):
            px(img, x + dx, y + dy, c)
    return img


def felt():
    img = noisy((34, 104, 60), 840, (-.05, -.02, 0, 0, .02, .04))
    for x in range(16):
        px(img, x, 0, (26, 80, 46))
    return img


def dartboard():
    img = canvas()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = math.hypot(dx, dy)
            if r > 7.6:
                continue
            seg = int((math.atan2(dy, dx) + math.pi) / (2 * math.pi) * 10) % 2
            c = (24, 22, 20) if seg else (226, 208, 166)
            if 6.4 < r <= 7.6:
                c = (44, 40, 36)  # the number ring
            elif 5.2 < r <= 6.3 or 2.8 < r <= 3.6:
                c = (186, 40, 36) if seg else (40, 120, 60)
            elif r <= 1.0:
                c = (186, 40, 36)
            elif r <= 1.8:
                c = (40, 120, 60)
            px(img, x, y, c)
    for x, y in ((10, 5), (4, 9)):  # two darts left in it
        px(img, x, y, (230, 200, 60))
    return img


def corkboard():
    rng = random.Random(850)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((176, 134, 84), 1 + rng.choice((-.1, -.05, 0, 0, .05, .08))))
    for i in range(16):
        for c in ((i, 1), (i, 14), (0, i), (15, i)):
            px(img, *c, mul(MAHOGANY, 1.0))
    # Lost dog: a white sheet, a dark sketch of a dog, a line of tear-off tabs.
    for y in range(3, 10):
        for x in range(2, 7):
            px(img, x, y, (234, 232, 220))
    for x, y in ((3, 5), (4, 5), (5, 5), (3, 6), (5, 6), (4, 4)):
        px(img, x, y, (60, 52, 44))
    for x in (2, 4, 6):
        px(img, x, 9, (200, 198, 186))
    # A yellow band flyer, and the newspaper clipping, grey with lines of print, pinned in red.
    for y in range(9, 13):
        for x in range(8, 13):
            px(img, x, y, (222, 196, 82))
    for x in range(9, 12):
        px(img, x, 10, (150, 70, 40))
    for y in range(3, 9):
        for x in range(8, 14):
            px(img, x, y, (206, 202, 190) if (y % 2 == 0 and x not in (8, 13)) else (226, 222, 210))
    px(img, 10, 3, (200, 40, 40))
    px(img, 4, 3, (40, 80, 180))
    return img


# Three-by-five capitals, enough for the neon and the sign.
FONT = {
    'A': ['010', '101', '111', '101', '101'], 'B': ['110', '101', '110', '101', '110'], 'C': ['011', '100', '100', '100', '011'],
    'D': ['110', '101', '101', '101', '110'], 'E': ['111', '100', '110', '100', '111'], 'K': ['101', '101', '110', '101', '101'],
    'L': ['100', '100', '100', '100', '111'], 'O': ['010', '101', '101', '101', '010'], 'P': ['110', '101', '110', '100', '100'],
    'R': ['110', '101', '110', '101', '101'], 'S': ['011', '100', '010', '001', '110'], 'T': ['111', '010', '010', '010', '010'],
    ' ': ['000', '000', '000', '000', '000'],
}


def letters(img, text, x0, y0, color, scale=1, glow=None):
    x = x0
    for ch in text:
        glyph = FONT[ch]
        for gy, row in enumerate(glyph):
            for gx, bit in enumerate(row):
                if bit == '1':
                    for sy in range(scale):
                        for sx in range(scale):
                            if glow:
                                for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                                    tx, ty = x + gx * scale + sx + ox, y0 + gy * scale + sy + oy
                                    if 0 <= tx < img.width and 0 <= ty < img.height and img.getpixel((tx, ty))[3] == 0:
                                        px(img, tx, ty, glow, 150)
                            px(img, x + gx * scale + sx, y0 + gy * scale + sy, color)
        x += 4 * scale
    return x


def neon_beer():
    img = canvas()
    letters(img, 'BEER', 0, 6, (255, 92, 84), glow=(170, 30, 30))
    for x in range(2, 14):  # a blue tube underlining it
        px(img, x, 12, (110, 180, 255))
    return img


def pole_sign():
    """The road sign, 48 by 32 over six blocks: LAST STOP in red tubes, COLD BEER in amber, on dark green tin."""
    img = canvas(48, 32)
    for y in range(32):
        for x in range(48):
            edge = min(x, y, 47 - x, 31 - y)
            c = (226, 214, 180) if edge == 1 else (40, 52, 44) if edge == 0 else mul((34, 70, 50), .92 if (x * 7 + y * 3) % 11 == 0 else 1)
            px(img, x, y, c)
    for x in range(2, 46):
        px(img, x, 16, (226, 214, 180))
    # LAST STOP at twice the height: each row of the small capitals drawn two pixels tall, with a dim glow round the tubes.
    small = canvas(48, 5)
    letters(small, 'LAST STOP', 0, 0, (255, 84, 70))
    for y in range(5):
        for x in range(36):
            if small.getpixel((x, y))[3]:
                for sy in range(2):
                    px(img, 6 + x, 3 + y * 2 + sy, (255, 96, 82) if sy == 0 else (236, 66, 56))
                    if img.getpixel((7 + x, 3 + y * 2 + sy))[:3] == (34, 70, 50):
                        px(img, 7 + x, 3 + y * 2 + sy, (120, 40, 36))
    letters(img, 'COLD BEER', 7, 21, (246, 176, 58))
    for x in range(7, 41, 4):
        px(img, x, 28, (246, 176, 58))
    return img


def stalks(top=False, stubble=False):
    """Dry stalks for a cross model: close-set ochre stems with leaves and heavy heads, transparent between."""
    rng = random.Random(870 + top * 2 + stubble)
    img = canvas()
    stem = lambda: mul((176, 150, 94), .78 + rng.random() * .32)
    for x in range(0, 16):
        if rng.random() < (.4 if stubble else .22):
            continue
        if stubble:
            h = rng.choice((2, 3, 3, 4))
            for y in range(16 - h, 16):
                px(img, x, y, stem())
            px(img, x, 16 - h, (206, 186, 130))
            continue
        h = 16 if not top else rng.choice((5, 7, 9, 11))
        start = 16 - h
        lean = rng.choice((-1, 0, 0, 1))
        c = stem()
        for y in range(start, 16):
            xx = x + (lean if y < start + 4 else 0)
            px(img, xx, y, mul(c, .9 + .1 * (y % 3 == 0)))
            if y % 5 == x % 5 and y > start + 2:  # a leaf off the stem
                px(img, xx + (1 if x % 2 else -1), y - 1, mul(c, .82))
        if top or rng.random() < .35:
            for y in range(start, start + 3):
                px(img, x + lean, y, (210, 182, 112))
                px(img, x + lean + (1 if x % 2 else -1), y + 1, (188, 158, 92))
    return img


def pole():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((128, 132, 134), 1.15 if x % 4 == 1 else .85 if x % 4 == 3 else 1))
    for x in (1, 9):
        px(img, x, 3, (90, 92, 94))
        px(img, x, 11, (90, 92, 94))
    return img


# ------------------------------------------------------------------------------------------------ the pickups
def body_panel(seed=880, crease=True):
    """A grey panel the tint paints: light along the top, a body crease, a little dust low down."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            f = 1.0 - y * .008 + rng.choice((0, 0, 0, -.025, .02))
            if crease and y == 6:
                f += .1
            if crease and y == 7:
                f -= .1
            if y >= 13:
                f -= .08 * (y - 12)
            px(img, x, y, (int(232 * f),) * 3)
    for _ in range(3):  # rust freckles
        x, y = rng.randrange(16), rng.randrange(10, 16)
        px(img, x, y, (150, 112, 92))
    return img


def door_panel(dented=False):
    img = body_panel(881)
    for y in range(1, 15):
        px(img, 0, y, (120, 120, 120))
        px(img, 15, y, (120, 120, 120))
    for x in range(16):
        px(img, x, 1, (140, 140, 140))
    for x in (10, 11, 12):  # handle
        px(img, x, 5, (250, 250, 250))
        px(img, x, 6, (110, 110, 110))
    if dented:
        # Pushed in where the elk hit: a dark hollow, folds running out of it, paint scraped to the metal along the creases.
        for y in range(2, 15):
            for x in range(1, 15):
                d = math.hypot(x - 6.5, (y - 8.5) * 1.15)
                if d < 6:
                    g = img.getpixel((x, y))[0]
                    px(img, x, y, (int(g * (.58 + .07 * d)),) * 3)
        for line in (((2, 4), (6, 8), (9, 13)), ((11, 3), (7, 8), (3, 12)), ((1, 9), (6, 9), (13, 10))):
            for (x0, y0), (x1, y1) in zip(line, line[1:]):
                steps = max(abs(x1 - x0), abs(y1 - y0))
                for i in range(steps + 1):
                    x = round(x0 + (x1 - x0) * i / steps)
                    y = round(y0 + (y1 - y0) * i / steps)
                    px(img, x, y, (96, 96, 96))
                    px(img, x, y - 1, (248, 248, 248))
        for x, y in ((5, 6), (8, 10), (4, 11), (10, 7)):
            px(img, x, y, (92, 70, 58))  # bare steel already rusting
    return img


def hood_top():
    img = body_panel(883, crease=False)
    for y in range(16):
        px(img, 7, y, (250, 250, 250))
        px(img, 8, y, (196, 196, 196))
    return img


def tailgate():
    img = body_panel(884, crease=False)
    for x in range(2, 14):
        px(img, x, 3, (150, 150, 150))
        px(img, x, 10, (250, 250, 250))
    for y in range(3, 11):
        px(img, 2, y, (150, 150, 150))
        px(img, 13, y, (250, 250, 250))
    return img


def bed_liner():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (34, 34, 36) if x % 3 else (52, 52, 56))
    return img


def tyre():
    img = canvas()
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r > 7.9:
                continue
            c = (26, 26, 28) if r > 4.6 else (150, 154, 158) if r > 3.4 else (110, 114, 118) if r > 1.3 else (60, 62, 64)
            if 4.6 < r < 5.4:
                c = (40, 40, 42)
            px(img, x, y, c)
    for a in range(5):
        ang = a * 2 * math.pi / 5
        px(img, round(7.5 + 2.4 * math.cos(ang)), round(7.5 + 2.4 * math.sin(ang)), (60, 62, 66))
    return img


def tread():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (24, 24, 26) if (x + (y // 2) * 3) % 5 else (40, 40, 42))
    return img


def glass():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((46, 58, 70), 1 + .05 * ((x + y) % 3 == 0)))
    for i in range(6):  # a streak of reflected light
        px(img, 3 + i, 12 - i, (120, 140, 156))
        px(img, 4 + i, 12 - i, (90, 108, 124))
    for i in range(16):
        for c in ((i, 0), (0, i), (15, i), (i, 15)):
            px(img, *c, (22, 22, 24))
    return img


def front(part):
    """The untinted front overlay: grille in the middle, a headlight at the outer edge, the bumper under all three."""
    img = canvas()
    for x in range(16):
        for y in (12, 13):
            px(img, x, y, (196, 200, 204) if y == 12 else (140, 144, 148))
    if part == 'm':
        for y in range(4, 11):
            for x in range(16):
                px(img, x, y, (190, 194, 198) if y in (4, 10) or x % 3 == 0 else (24, 24, 26))
    else:
        cx = 4.5 if part == 'l' else 10.5
        for y in range(4, 11):
            for x in range(16):
                r = math.hypot(x - cx, y - 7)
                if r < 3.4:
                    px(img, x, y, (255, 246, 206) if r < 2.2 else (196, 200, 204))
        gx = range(9, 16) if part == 'l' else range(0, 7)
        for y in range(5, 10):
            for x in gx:
                px(img, x, y, (190, 194, 198) if y in (5, 9) or x % 3 == 0 else (24, 24, 26))
    return img


def tail(part):
    img = canvas()
    for x in range(16):
        for y in (12, 13):
            px(img, x, y, (196, 200, 204) if y == 12 else (140, 144, 148))
    if part == 'm':
        for y in range(6, 10):
            for x in range(4, 12):
                px(img, x, y, (232, 230, 220) if y in (6, 9) or x in (4, 11) else (230, 228, 214))
        for x in (5, 7, 9, 10):
            px(img, x, 7, (40, 60, 120))
            px(img, x, 8, (40, 60, 120))
    else:
        x0 = 1 if part == 'l' else 11
        for y in range(3, 10):
            for x in range(x0, x0 + 4):
                px(img, x, y, (206, 40, 36) if y < 8 else (236, 150, 40))
    return img


SWATCHES = {'chrome': (196, 200, 206), 'black': (22, 22, 24), 'white': (236, 234, 226), 'amber': (232, 152, 36),
            'red': (176, 36, 34), 'brass': (204, 166, 76), 'glass': (196, 222, 226), 'green': (44, 118, 66),
            'shade': (36, 92, 56), 'bulb': (255, 238, 186), 'wood': (104, 52, 34), 'dark': (58, 38, 28),
            'bottle': (152, 92, 30), 'clear': (200, 224, 216), 'mesh': (230, 228, 220), 'grey': (96, 98, 100)}
SWATCH = {name: i for i, name in enumerate(SWATCHES)}


def swatches():
    img = canvas()
    for name, i in SWATCH.items():
        c = SWATCHES[name]
        x0, y0 = (i % 4) * 4, (i // 4) * 4
        for y in range(4):
            for x in range(4):
                f = 1.12 if x == 0 or y == 0 else .86 if x == 3 or y == 3 else 1.0
                px(img, x0 + x, y0 + y, mul(c, f))
    return img


def register_face():
    """The register's keys and its little window."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (64, 66, 70))
    for y in range(8, 15, 2):
        for x in range(2, 14, 3):
            px(img, x, y, (226, 222, 210))
            px(img, x + 1, y, (190, 186, 176))
    for y in range(2, 6):
        for x in range(3, 13):
            px(img, x, y, (30, 40, 34) if y in (2, 5) else (120, 200, 120) if x in (5, 6, 8, 9, 10) and y == 3 else (30, 40, 34))
    return img


TEXTURES = {
    'asphalt': asphalt, 'asphalt_edge': lambda: road_line('edge'), 'asphalt_centre': lambda: road_line('centre'),
    'gravel_0': lambda: gravel(0), 'gravel_1': lambda: gravel(1), 'gravel_2': lambda: gravel(2), 'gravel_3': lambda: gravel(3),
    'boards': boards, 'counter_front': counter_front, 'counter_top': counter_top, 'counter_back': counter_back,
    'cabinet': cabinet, 'shelf_bottles': shelf_bottles, 'mirror': mirror, 'vinyl': vinyl, 'formica': formica, 'felt': felt,
    'dark_wood': lambda: wood(DARK, 814), 'mahogany': lambda: wood(MAHOGANY, 815), 'dartboard': dartboard, 'corkboard': corkboard,
    'neon_beer': neon_beer, 'pole': pole, 'stalks': stalks, 'stalks_top': lambda: stalks(top=True), 'stubble': lambda: stalks(stubble=True),
    'pu_body': body_panel, 'pu_door': door_panel, 'pu_door_dent': lambda: door_panel(True), 'pu_hood': hood_top, 'pu_tailgate': tailgate,
    'pu_liner': bed_liner, 'pu_tyre': tyre, 'pu_tread': tread, 'pu_glass': glass,
    'pu_front_l': lambda: front('l'), 'pu_front_m': lambda: front('m'), 'pu_front_r': lambda: front('r'),
    'pu_tail_l': lambda: tail('l'), 'pu_tail_m': lambda: tail('m'), 'pu_tail_r': lambda: tail('r'),
    'swatches': swatches, 'register': register_face,
}


def sign_panels():
    sign = pole_sign()
    out = {}
    for row, r in enumerate('tb'):
        for col, c in enumerate('lmr'):
            out[f'sign_{r}{c}'] = sign.crop((col * 16, row * 16, col * 16 + 16, row * 16 + 16))
    return out


# ------------------------------------------------------------------------------------------------ models
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


def sw(name):
    i = SWATCH[name]
    x, y = (i % 4) * 4, (i // 4) * 4
    return [x + .5, y + .5, x + 3.5, y + 3.5]


def box(frm, to, tex, uv=None, faces_=DIRS, overrides=None, tint=False, glow=False, rot=None):
    faces = {}
    for d in faces_:
        face = {'texture': '#' + tex}
        if uv is not None:
            face['uv'] = uv
        if tint:
            face['tintindex'] = 0
        faces[d] = face
    for d, spec in (overrides or {}).items():
        t, u = spec[0], spec[1]
        face = {'texture': '#' + t}
        if u:
            face['uv'] = u
        if len(spec) > 2 and spec[2]:
            face['tintindex'] = 0
        faces[d] = face
    el = {'from': list(frm), 'to': list(to), 'faces': faces}
    if glow:
        el['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
    if rot:
        el['rotation'] = rot
    return el


def color(frm, to, name, faces_=DIRS, glow=False):
    return box(frm, to, 's', sw(name), faces_=faces_, glow=glow)


def model(name, elements, textures, particle=None, render='cutout'):
    tex = {'s': NS + 'swatches', **{k: (v if ':' in v else NS + v) for k, v in textures.items()}}
    tex['particle'] = particle if particle and ':' in particle else NS + (particle or next(iter(textures.values()), 'swatches'))
    data = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:' + render, 'textures': tex, 'elements': elements}
    (MODELS / f'last_stop_{name}.json').write_text(json.dumps(data, indent=1) + '\n')


def cube(name, faces, particle=None):
    """A whole block, solid, culled against its neighbours."""
    tex = {d: (v if ':' in v else NS + v) for d, v in faces.items()}
    tex['particle'] = tex.get('up', next(iter(tex.values())))
    (MODELS / f'last_stop_{name}.json').write_text(json.dumps({'parent': 'minecraft:block/cube', 'textures': tex}, indent=1) + '\n')


def cross(name, texture):
    (MODELS / f'last_stop_{name}.json').write_text(json.dumps({'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout',
                                                                'textures': {'cross': NS + texture}}, indent=1) + '\n')


def all6(t):
    return {d: t for d in ('north', 'south', 'east', 'west', 'up', 'down')}


def models():
    MODELS.mkdir(parents=True, exist_ok=True)
    cube('asphalt', all6('asphalt'))
    cube('asphalt_edge', {**all6('asphalt'), 'up': 'asphalt_edge'})
    cube('asphalt_centre', {**all6('asphalt'), 'up': 'asphalt_centre'})
    for i in range(4):
        cube(f'gravel_{i}', {**all6('gravel_0'), 'up': f'gravel_{i}', 'down': 'minecraft:block/dirt'})
    cube('boards', all6('boards'))
    W = {'w': 'mahogany', 'd': 'dark_wood'}
    # The counter: tongue and groove to the patrons, a lip and a brass rail, glasses on the bartender's side.
    model('counter', [box((0, 0, 0), (16, 15, 16), 'f', overrides={'south': ('b', None), 'up': ('t', None), 'down': ('d', None)}),
                      box((0, 15, -1), (16, 16, 16), 't', overrides={'north': ('w', [0, 0, 16, 1]), 'east': ('w', [0, 0, 16, 1]), 'west': ('w', [0, 0, 16, 1])}),
                      color((0, 2, -1.5), (16, 3, -.5), 'brass')],
          {**W, 'f': 'counter_front', 'b': 'counter_back', 't': 'counter_top'}, 'counter_top')
    bottles = [color((2, 10, 6), (4, 15, 8), 'green'), color((2.5, 15, 6.5), (3.5, 16, 7.5), 'green'), color((6, 10, 7), (8, 14, 9), 'bottle'),
               color((10, 10, 5), (12, 15.5, 7), 'clear'), color((13, 10, 8), (15, 13, 10), 'red')]
    model('back_bar', [box((0, 0, 1), (16, 10, 16), 'd', overrides={'north': ('c', None), 'up': ('t', None)}), *bottles],
          {**W, 'c': 'cabinet', 't': 'counter_top'}, 'cabinet')
    model('shelf', [box((0, 0, 0), (16, 16, 16), 'd', overrides={'north': ('k', None)})], {**W, 'k': 'shelf_bottles'}, 'shelf_bottles')
    model('mirror', [box((0, 0, 14), (16, 16, 16), 'd', overrides={'north': ('m', None)})], {**W, 'm': 'mirror'}, 'mirror')
    model('taps', [color((4, 0, 6), (12, 1, 10), 'chrome'), color((6, 1, 7), (10, 9, 9), 'chrome'), color((5, 9, 7), (11, 10, 9), 'chrome'),
                   color((5.5, 10, 7.5), (6.5, 14, 8.5), 'black'), color((7.5, 10, 7.5), (8.5, 14, 8.5), 'red'), color((9.5, 10, 7.5), (10.5, 14, 8.5), 'amber'),
                   color((6.5, 6, 6), (7.5, 8, 7), 'chrome'), color((8.5, 6, 6), (9.5, 8, 7), 'chrome')], {}, 'swatches')
    model('register', [box((3, 0, 4), (13, 4, 13), 'r', overrides={'north': ('r', None)}), box((4, 4, 8), (12, 7, 13), 'r', uv=[0, 0, 16, 6]),
                       color((5, 7, 10), (11, 10, 12), 'grey'), color((6, 7.2, 9.9), (10, 9.5, 10), 'green'), color((7, 4, 3), (9, 6, 4), 'white')],
          {'r': 'register'}, 'register')
    model('glasses', [color((2, 0, 2), (14, 1, 14), 'black'), *[color((x, 1, z), (x + 2, 4, z + 2), 'clear') for x in (3, 7, 11) for z in (3, 7, 11)]], {}, 'swatches')
    model('stool', [color((5, 0, 5), (11, 1, 11), 'chrome'), color((7, 1, 7), (9, 8, 9), 'chrome'), color((4.5, 4, 4.5), (11.5, 4.5, 11.5), 'chrome'),
                    box((4, 8, 4), (12, 10, 12), 'v'), color((3.8, 8.5, 3.8), (12.2, 9, 12.2), 'chrome')], {'v': 'vinyl'}, 'vinyl')
    model('booth', [box((0, 0, 0), (16, 5, 13), 'd'), box((0, 5, 0), (16, 7, 13), 'v'), box((0, 0, 13), (16, 15, 16), 'v'), box((0, 15, 12.5), (16, 16, 16), 'w')],
          {**W, 'v': 'vinyl'}, 'vinyl')
    model('booth_table', [color((5, 0, 5), (11, 1, 11), 'chrome'), color((7, 1, 7), (9, 11, 9), 'chrome'),
                          box((0, 11, 0), (16, 12, 16), 'f', overrides={d: ('s', sw('chrome')) for d in ('north', 'south', 'east', 'west', 'down')}),
                          color((6, 12, 7), (9, 14.5, 9), 'chrome'), color((11, 12, 7), (12, 15, 8), 'red'), color((11.25, 15, 7.25), (11.75, 16, 7.75), 'white')],
          {'f': 'formica'}, 'formica')
    # The pool table in two halves along x. Stage 0 has the balls racked; stage 1 broken across the felt.
    def pool(side, stage):
        end = (0, 1.5) if side == 'l' else (14.5, 16)
        els = [box((0, 8, 0), (16, 11, 16), 'w'), *[box((x, 0, z), (x + 2, 8, z + 2), 'w') for x in ((1,) if side == 'l' else (13,)) for z in (1, 13)],
               box((0, 11, 1.5), (16, 12, 14.5), 'f', overrides={d: ('w', None) for d in ('north', 'south', 'east', 'west', 'down')}),
               box((0, 11, 0), (16, 13, 1.5), 'w'), box((0, 11, 14.5), (16, 13, 16), 'w'), box((end[0], 11, 0), (end[1], 13, 16), 'w'),
               color((end[0] if side == 'l' else end[0] - .5, 12, .5), (end[0] + 2 if side == 'l' else end[1] - .5, 12.1, 2), 'black', faces_=('up',)),
               color((end[0] if side == 'l' else end[0] - .5, 12, 14), (end[0] + 2 if side == 'l' else end[1] - .5, 12.1, 15.5), 'black', faces_=('up',)),
               color((15 if side == 'l' else 0, 12, .5), (16 if side == 'l' else 1, 12.1, 1.5), 'black', faces_=('up',)),
               color((15 if side == 'l' else 0, 12, 14.5), (16 if side == 'l' else 1, 12.1, 15.5), 'black', faces_=('up',))]
        cols = ['red', 'amber', 'black', 'green', 'white', 'brass']
        if side == 'r' and stage == 0:
            spots = [(8, 8), (9, 7.5), (9, 8.5), (10, 7), (10, 8), (10, 9), (11, 6.5), (11, 7.5), (11, 8.5), (11, 9.5)]
            els += [color((x, 12, z), (x + 1, 13, z + 1), cols[i % 5]) for i, (x, z) in enumerate(spots)]
        elif side == 'l' and stage == 0:
            els.append(color((4, 12, 7.5), (5, 13, 8.5), 'white'))
        elif stage == 1:
            spots = [(3, 4), (6, 11), (10, 3), (12, 9), (8, 7)] if side == 'l' else [(2, 6), (5, 12), (9, 4), (13, 10)]
            els += [color((x, 12, z), (x + 1, 13, z + 1), cols[(i + (side == 'r')) % 6]) for i, (x, z) in enumerate(spots)]
        model(f'pool_{side}_{stage}', els, {**W, 'f': 'felt'}, 'felt')
    for side in 'lr':
        for stage in (0, 1):
            pool(side, stage)
    model('pool_lamp', [color((7.5, 12, 7.5), (8.5, 16, 8.5), 'black'), color((3, 8, 3), (13, 12, 13), 'shade'),
                        color((3.2, 7.95, 3.2), (12.8, 8, 12.8), 'bulb', faces_=('down',), glow=True), color((6, 7, 6), (10, 8, 10), 'bulb', glow=True)], {}, 'swatches')
    model('dartboard', [box((3, 3, 15), (13, 13, 16), 'b', overrides={d: ('s', sw('black')) for d in ('south', 'east', 'west', 'up', 'down')})], {'b': 'dartboard'}, 'dartboard')
    dart = json.loads((MODELS / 'last_stop_dartboard.json').read_text())
    for stage in (1, 2, 3):
        fitted = json.loads(json.dumps(dart))
        for i in range(stage):
            x, y = [(6, 5), (10, 9), (8, 8)][i]
            fitted['elements'].append(color((x-.15, y-.15, 11), (x+.15, y+.15, 15.9), 'black'))
        (MODELS / f'last_stop_dartboard_{stage}.json').write_text(json.dumps(fitted, indent=1) + '\n')
    model('corkboard', [box((0, 1, 15), (16, 15, 16), 'c', uv=[0, 1, 16, 15], overrides={d: ('w', None) for d in ('south', 'east', 'west', 'up', 'down')})],
          {**W, 'c': 'corkboard'}, 'corkboard')
    model('neon_beer', [box((0, 3, 7.5), (16, 13, 8.5), 'n', uv=[0, 3, 16, 13], faces_=('north', 'south'), glow=True),
                        color((3, 13, 7.75), (3.5, 16, 8.25), 'black'), color((12.5, 13, 7.75), (13, 16, 8.25), 'black')], {'n': 'neon_beer'}, 'neon_beer')
    # The pole sign. Each panel's back shows its mirror twin's picture, so the sign reads the right way from the lot too.
    twin = {'l': 'r', 'm': 'm', 'r': 'l'}
    for r in 'tb':
        for c in 'lmr':
            model(f'sign_{r}{c}', [box((0, 0, 6), (16, 16, 10), 'e', overrides={'north': ('f', None), 'south': ('k', None)}, glow=True)],
                  {'f': f'sign_{r}{c}', 'k': f'sign_{r}{twin[c]}', 'e': 'pole'}, f'sign_{r}{c}')
    model('pole', [box((6, 0, 6), (10, 16, 10), 'p')], {'p': 'pole'}, 'pole')
    model('reflector', [color((7, 0, 7), (9, 12, 9), 'white'), color((6.9, 9, 6.8), (9.1, 11, 7), 'amber', glow=True), color((6.9, 9, 9), (9.1, 11, 9.2), 'amber', glow=True)], {}, 'swatches')
    cross('stalks', 'stalks')
    cross('stalks_top', 'stalks_top')
    cross('stubble', 'stubble')
    model('cap', [color((5, 0, 6), (11, 2.5, 12), 'red'), color((5.5, 2.5, 6.5), (10.5, 3, 11.5), 'red'), color((5, 0, 9), (11, 2.4, 12.01), 'mesh'),
                  color((6, 0, 3), (10, .5, 6), 'red')], {}, 'swatches')
    model('fallen_rail', [box((0, 0, 6), (16, 1.5, 10), 'p', rot={'origin': [8, 0, 8], 'axis': 'y', 'angle': 22.5})], {'p': 'minecraft:block/stripped_spruce_log'}, 'minecraft:block/stripped_spruce_log')
    # The pickups. Body panels are grey and tinted (tintindex 0); everything else is drawn as it is.
    P = {'b': 'pu_body', 'h': 'pu_hood', 'g': 'pu_glass', 'l': 'pu_liner', 'y': 'pu_tyre', 't': 'pu_tread'}

    def tinted(frm, to, tex='b', overrides=None, faces_=DIRS):
        return box(frm, to, tex, faces_=faces_, overrides=overrides, tint=True)
    under = color((1, 0, 1), (15, 2, 15), 'black')
    for part in 'lmr':
        els = [tinted((0, 2, 0), (16, 13, 16), overrides={'up': ('h', None, True)}), under, box((0, 2, -.05), (16, 13, 0), 'o', faces_=('north',))]
        if part != 'm':
            x0, x1 = (10, 15) if part == 'l' else (1, 6)
            els.append(color((x0, 6, -.12), (x1, 10, -.06), 'bulb', faces_=('north',), glow=True))
        model(f'pu_front_{part}', els, {**P, 'o': f'pu_front_{part}'}, 'pu_body')
    wheel = box((2, 0, -.6), (14, 11, 5), 't', overrides={'north': ('y', None)})
    model('pu_wheel', [tinted((0, 8, 0), (16, 13, 16), overrides={'up': ('h', None, True)}), color((0, 1, 5), (16, 8, 16), 'black'), wheel], P, 'pu_body')
    model('pu_hood', [tinted((0, 2, 0), (16, 13, 16), overrides={'up': ('h', None, True)}), under], P, 'pu_body')
    for name, tex in (('pu_door', 'pu_door'), ('pu_door_dent', 'pu_door_dent')):
        inset = .8 if name == 'pu_door_dent' else 0
        model(name, [tinted((0, 2, inset), (16, 16, 16), overrides={'north': ('d', None, True)}), under], {**P, 'd': tex}, 'pu_body')
    model('pu_bench', [tinted((0, 2, 0), (16, 15.9, 16), faces_=('north', 'south', 'east', 'west', 'down')), box((0, 15.9, 0), (16, 16, 16), 'v', faces_=('up',)), under],
          {**P, 'v': 'vinyl'}, 'pu_body')
    model('pu_cab', [box((.5, 0, .5), (15.5, 10, 15.5), 'g', faces_=('north', 'south', 'east', 'west')), tinted((0, 10, 0), (16, 12, 16), overrides={'up': ('h', None, True)}),
                     *[tinted((x, 0, z), (x + 1.5, 10, z + 1.5)) for x in (0, 14.5) for z in (0, 14.5)]], P, 'pu_glass')
    bed = box((0, 4, 3), (16, 6, 16), 'l')
    model('pu_bed_side', [tinted((0, 2, 0), (16, 13, 3)), bed, color((0, 0, 3), (16, 4, 16), 'black')], P, 'pu_body')
    model('pu_bed_wheel', [tinted((0, 8, 0), (16, 13, 3)), bed, color((0, 1, 5), (16, 4, 16), 'black'), wheel], P, 'pu_body')
    model('pu_bed', [box((0, 4, 0), (16, 6, 16), 'l'), color((1, 0, 1), (15, 4, 15), 'black')], P, 'pu_liner')
    for part in 'lmr':
        els = [tinted((0, 2, 0), (16, 13, 3), overrides={'north': ('k', None, True)}), box((0, 2, -.05), (16, 13, 0), 'o', faces_=('north',)), bed, color((1, 0, 3), (15, 4, 15), 'black')]
        if part != 'm':
            x0, x1 = (11, 15) if part == 'l' else (1, 5)
            els.append(color((x0, 6, -.12), (x1, 11, -.06), 'red', faces_=('north',), glow=True))
        model(f'pu_tail_{part}', els, {**P, 'k': 'pu_tailgate', 'o': f'pu_tail_{part}'}, 'pu_body')


KINDS = ['asphalt', 'asphalt_edge', 'asphalt_centre', 'gravel', 'boards',
         'counter', 'back_bar', 'shelf', 'mirror', 'taps', 'register', 'glasses', 'stool', 'booth', 'booth_table', 'pool_l', 'pool_r', 'pool_lamp',
         'dartboard', 'corkboard', 'neon_beer', 'sign_tl', 'sign_tm', 'sign_tr', 'sign_bl', 'sign_bm', 'sign_br', 'pole', 'reflector',
         'stalks', 'stalks_top', 'stubble', 'cap', 'fallen_rail',
         'pu_front_l', 'pu_front_m', 'pu_front_r', 'pu_wheel', 'pu_hood', 'pu_door', 'pu_door_dent', 'pu_bench', 'pu_cab', 'pu_bed_side',
         'pu_bed_wheel', 'pu_bed', 'pu_tail_l', 'pu_tail_m', 'pu_tail_r']
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def blockstate():
    variants = {}
    for facing, y in TURN.items():
        for kind in KINDS:
            for stage in range(4):
                name = f'gravel_{stage}' if kind == 'gravel' else f'{kind}_{min(stage, 1)}' if kind in ('pool_l', 'pool_r') else kind
                if kind == 'dartboard' and stage:
                    name += f'_{stage}'
                v = {'model': f'the_oldest_house:block/last_stop_{name}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    (ASSETS / 'blockstates/roadhouse.json').write_text(json.dumps({'variants': variants}, indent=1) + '\n')
    return len(variants)


# ------------------------------------------------------------------------------------------------ sounds
def t_of(seconds):
    return np.arange(round(RATE * seconds)) / RATE


def noise(seconds, seed):
    return np.random.default_rng(seed).uniform(-1, 1, round(RATE * seconds))


def lowpass(signal, alpha):
    out = np.empty_like(signal)
    acc = 0.0
    for i, v in enumerate(signal):
        acc += alpha * (v - acc)
        out[i] = acc
    return out


def normalize(signal, peak=.8):
    return signal / max(1e-9, np.max(np.abs(signal))) * peak


def place(out, signal, at):
    i = round(at * RATE)
    n = min(len(signal), len(out) - i)
    out[i:i + n] += signal[:n]


def s_alarm():
    """A car alarm's cycle: three rising whoops, then two-tone honks; it is played again while the alarm lasts."""
    t = t_of(1.6)
    out = np.zeros_like(t)
    for k in range(3):
        seg = t_of(.22)
        f = 620 + 900 * seg / .22
        phase = 2 * np.pi * np.cumsum(f) / RATE
        place(out, np.sign(np.sin(phase)) * .5 + np.sin(phase) * .5, k * .24)
    for k in range(4):
        seg = t_of(.16)
        f = 880 if k % 2 == 0 else 660
        place(out, (np.sign(np.sin(2 * np.pi * f * seg)) * .6 + np.sin(4 * np.pi * f * seg) * .2) * np.minimum(1, (.16 - seg) * 60), .76 + k * .2)
    return normalize(lowpass(out, .55), .72)


def s_highway():
    """A truck goes by in the dark: engine and tyres rising and falling, its pitch dropping as it passes."""
    secs = 5.5
    t = t_of(secs)
    env = np.exp(-((t - 2.6) / 1.15) ** 2)
    shift = 1.18 - .36 / (1 + np.exp(-(t - 2.6) * 3))
    phase = 2 * np.pi * np.cumsum(58 * shift) / RATE
    engine = np.sin(phase) * .5 + np.sin(phase * 2) * .3 + np.sin(phase * 3.02) * .15
    road = lowpass(noise(secs, 781), .08) * 3.2
    hiss = lowpass(noise(secs, 782), .45) * .4
    return normalize((engine * .6 + road + hiss * env) * env, .7)


def s_pool():
    """A break: the cue's tap, a burst of clicks thinning out, a ball rolling to the cushion."""
    secs = 1.4
    out = np.zeros(round(RATE * secs))
    rng = random.Random(783)
    click = lambda f, a: (np.sin(2 * np.pi * f * t_of(.03)) * np.exp(-t_of(.03) * 160) + noise(.03, rng.randrange(1000)) * np.exp(-t_of(.03) * 300) * .5) * a
    place(out, click(1800, .7), 0)
    at = .09
    for i in range(14):
        place(out, click(rng.uniform(2600, 4200), rng.uniform(.4, 1.0) * (1 - i / 18)), at)
        at += rng.uniform(.01, .05) * (1 + i * .25)
    roll = lowpass(noise(.6, 784), .05) * np.linspace(.6, 0, round(RATE * .6))
    place(out, roll * 1.6, .7)
    return normalize(out, .75)


def s_crash():
    """A big animal into a truck door: the thud, metal folding, a rattle and glass tinkling down."""
    secs = 2.2
    t = t_of(secs)
    out = np.zeros_like(t)
    thud = np.sin(2 * np.pi * 52 * t_of(.5)) * np.exp(-t_of(.5) * 9) + lowpass(noise(.5, 785), .1) * np.exp(-t_of(.5) * 12) * 2
    place(out, thud, 0)
    crumple = lowpass(noise(.6, 786), .6) * np.exp(-t_of(.6) * 7)
    for f, a in ((520, .5), (870, .35), (1340, .25), (2210, .15)):
        crumple += np.sin(2 * np.pi * f * t_of(.6)) * np.exp(-t_of(.6) * (6 + f / 400)) * a
    place(out, crumple, .02)
    rng = random.Random(787)
    for i in range(12):
        tink = np.sin(2 * np.pi * rng.uniform(3000, 5200) * t_of(.08)) * np.exp(-t_of(.08) * 60) * rng.uniform(.1, .3)
        place(out, tink, .25 + i * rng.uniform(.04, .1))
    return normalize(out, .85)


SOUNDS = {
    'lot_alarm': (s_alarm, 'Car alarm blares'),
    'lot_highway': (s_highway, 'Truck passes on the highway'),
    'lot_pool': (s_pool, 'Pool balls clack'),
    'lot_crash': (s_crash, 'Metal crumples'),
}


def write_json(path, data, original):
    text = json.dumps(data, indent=2, ensure_ascii=False)
    if original.endswith('\n'):
        text += '\n'
    path.write_text(text)


def sounds():
    folder = ASSETS / 'sounds/literary'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (make, _) in SOUNDS.items():
        soundfile.write(str(folder / f'{name}.ogg'), make().astype(np.float32), RATE, format='OGG', subtype='VORBIS')
    path = ASSETS / 'sounds.json'
    original = path.read_text()
    table = json.loads(original)
    for name in SOUNDS:
        entry = {'name': f'the_oldest_house:literary/{name}', 'stream': False}
        if name in ('lot_alarm', 'lot_crash', 'lot_highway'):
            entry['attenuation_distance'] = 48
        table[f'literary.{name}'] = {'subtitle': f'subtitles.the_oldest_house.literary.{name}', 'sounds': [entry]}
    write_json(path, table, original)
    path = ASSETS / 'lang/en_us.json'
    original = path.read_text()
    lang = json.loads(original)
    for name, (_, subtitle) in SOUNDS.items():
        lang[f'subtitles.the_oldest_house.literary.{name}'] = subtitle
    lang['block.the_oldest_house.roadhouse'] = 'Roadhouse piece'
    write_json(path, lang, original)


# ------------------------------------------------------------------------------------------------ the people
PLACEHOLDERS = {'marla': 'stranger_2', 'russ': 'stranger_0', 'denny': 'stranger_1', 'wade': 'stranger_3', 'hollis': 'stranger_1'}


def placeholders():
    """Each of the bar's people gets their own skin file, a copy of a stranger's to begin with. An existing file is kept."""
    entity = ASSETS / 'textures/entity'
    written = []
    for name, source in PLACEHOLDERS.items():
        target = entity / f'literary_bar_{name}.png'
        if not target.exists():
            shutil.copyfile(entity / f'literary_{source}.png', target)
            written.append(target.name)
    return written


if __name__ == '__main__':
    TEX.mkdir(parents=True, exist_ok=True)
    for name, make in TEXTURES.items():
        make().save(TEX / f'{name}.png')
    for name, img in sign_panels().items():
        img.save(TEX / f'{name}.png')
    models()
    print('roadhouse variants:', blockstate())
    sounds()
    print('placeholder skins written:', placeholders() or 'none (kept as they are)')
