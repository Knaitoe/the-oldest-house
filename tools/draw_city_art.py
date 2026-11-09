"""The city on the ice (0.4.409): an expedition's camp, a pass, and a city of dark stone.

Draws, in native Minecraft pixels from fixed seeds: six materials (the city's dark stone, the same stone carved in fives, packed snow,
blue ice, the tents' and the aeroplane's canvas, and the huts' weathered boards); every fitting of the city_fitting block, its models
facing north and turned by its facing (the radio set with its dials lit, the operator's log, the message pad blank or written on, a
hurricane lamp dark or lit, a supply crate, the oil stove, an empty dog harness, rock cores in their box, the dissecting table with its
straps cut, the aeroplane's fabric opened in cuts, its propeller, and three-toed prints in the grit); what fills the tunnel, an animated
dark sheen with eyes opening and closing in it; and the twelve carved panels along the ramp, wordless, each a 64-pixel picture cut across
four blocks by three. Writes the models and block states and the blocks' names. --check verifies everything.
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
P = NS + 'block/city/'


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


# ------------------------------------------------------------------------------------------------ materials
def basalt():
    im = new('#2c2b31', 40901, 4); rnd = random.Random(40902)
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#3a3940')
    for _ in range(8):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#1f1e23')
    for x in range(16):
        dot(im, x, 15, '#232227'); dot(im, x, 7, '#26252b')
    for y in range(16):
        dot(im, 0 if y < 8 else 8, y, '#232227')
    return im


def fives():
    im = basalt()
    # A five-armed star cut into the face, its arms ending in drilled points.
    cx, cy = 7.5, 7.5
    for k in range(5):
        a = -math.pi / 2 + k * 2 * math.pi / 5
        for r in range(1, 7):
            dot(im, round(cx + math.cos(a) * r), round(cy + math.sin(a) * r), '#17161a')
        dot(im, round(cx + math.cos(a) * 6.5), round(cy + math.sin(a) * 6.5), '#0f0e12')
        dot(im, round(cx + math.cos(a) * 3 + 1), round(cy + math.sin(a) * 3), '#45444c')
    dot(im, 7, 7, '#45444c'); dot(im, 8, 8, '#17161a')
    return im


def snow():
    im = new('#eef2f6', 40903, 3); rnd = random.Random(40904)
    for _ in range(12):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#dce4ec')
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#ffffff')
    return im


def ice():
    im = new('#8fb8d8', 40905, 4); rnd = random.Random(40906)
    for _ in range(4):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for i in range(rnd.randint(3, 7)):
            dot(im, (x + i) % 16, (y + i // 2) % 16, '#c6def0')
    for _ in range(6):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#6f9cc2')
    return im


def canvas():
    im = new('#d8cfb4', 40907, 3)
    for y in range(0, 16, 2):
        for x in range(16):
            if (x + y) % 4 == 0:
                dot(im, x, y, '#cbc1a4')
    for x in range(16):
        dot(im, x, 5, '#c4b998')
    return im


def boards():
    im = new('#7d6247', 40908, 5)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#5a4430')
    rnd = random.Random(40909)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#8e7458')
    for y in (1, 5, 9, 13):
        dot(im, 3 + y % 7, y, '#4b3826')
    return im


def materials():
    return {'basalt': basalt(), 'fives': fives(), 'snow': snow(), 'ice': ice(), 'canvas': canvas(), 'boards': boards()}


# ------------------------------------------------------------------------------------------------ fittings
def radio_front():
    im = new('#3c4436', 40910, 3)
    fill(im, 1, 1, 15, 15, '#2f362a')
    for i, x in enumerate((3, 7, 11)):
        fill(im, x, 3, x + 3, 6, '#e0c070' if i != 1 else '#c8e090')
        dot(im, x + 1, 4, '#3a2a10')
    for x in (4, 8, 12):
        fill(im, x - 1, 9, x + 1, 11, '#1c1c1c'); dot(im, x - 1, 9, '#6a6a6a')
    for x in range(2, 14):
        dot(im, x, 13, '#20261c')
    return im


def radio_side():
    im = new('#3c4436', 40911, 3)
    for y in range(2, 14, 3):
        for x in range(3, 13):
            dot(im, x, y, '#2a3024')
    return im


def logbook():
    im = new('#3d2e24', 40912, 3)
    fill(im, 1, 1, 15, 15, '#4d3a2c')
    for x in range(3, 13):
        dot(im, x, 4, '#c8b68e')
    fill(im, 7, 1, 9, 15, '#2d2119')
    return im


def paper_edge():
    im = new('#e8e0cc', 40913, 2)
    for y in range(0, 16, 2):
        for x in range(16):
            dot(im, x, y, '#d6ccb4')
    return im


def pad(written):
    im = new('#efe8d6', 40914, 2)
    fill(im, 0, 0, 16, 2, '#7a3a2a')
    for y in range(3, 16, 2):
        for x in range(1, 15):
            dot(im, x, y, '#d8d0bc')
    if written:
        rnd = random.Random(40915)
        for y in range(4, 15, 2):
            x = 2
            while x < 14:
                n = rnd.randint(1, 4)
                for i in range(n):
                    if x + i < 14:
                        dot(im, x + i, y, '#2a2a3a')
                x += n + 1
    return im


def lamp_glass(lit):
    im = new('#f4d890' if lit else '#6a6858', 40916 + lit, 4)
    if lit:
        fill(im, 5, 4, 11, 12, '#fff2c0'); fill(im, 7, 6, 9, 11, '#ffffff')
    else:
        fill(im, 6, 8, 10, 12, '#3a3830')
    return im


def lamp_metal():
    im = new('#3a3a3a', 40918, 4)
    for y in range(16):
        dot(im, 0, y, '#2a2a2a'); dot(im, 15, y, '#4a4a4a')
    return im


def crate():
    im = new('#8a6a46', 40919, 5)
    for i in range(16):
        dot(im, i, 0, '#5a4228'); dot(im, i, 15, '#5a4228'); dot(im, 0, i, '#5a4228'); dot(im, 15, i, '#5a4228')
        dot(im, i, i, '#6b5032'); dot(im, i, 15 - i, '#6b5032')
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        dot(im, x, y, '#2a2a2a')
    return im


def stove():
    im = new('#4a4a4e', 40920, 3)
    fill(im, 3, 6, 13, 13, '#2a2a2e')
    fill(im, 5, 8, 11, 11, '#d0602a'); fill(im, 6, 9, 10, 10, '#f0a040')
    for x in range(2, 14, 2):
        dot(im, x, 2, '#5e5e64')
    return im


def harness():
    im = new(None)
    for x in range(1, 15):
        dot(im, x, 7, '#7a2a1e'); dot(im, x, 8, '#5e2016')
    for y in range(2, 14):
        dot(im, 4, y, '#7a2a1e'); dot(im, 11, y, '#7a2a1e')
    for x, y in ((4, 7), (11, 7), (4, 3), (11, 12)):
        fill(im, x - 1, y - 1, x + 2, y + 2, '#a0a0a8'); dot(im, x, y, '#5a5a62')
    return im


def cores_top():
    im = new('#7a5e3e', 40921, 4)
    for x in range(1, 15):
        for y in (3, 7, 11):
            dot(im, x, y, '#4a3826')
    rnd = random.Random(40922)
    for y in (1, 5, 9, 13):
        for x in range(1, 15):
            dot(im, x, y, shade('#6a6a70', rnd.randint(-14, 14)))
            dot(im, x, y + 1, shade('#5a5a62', rnd.randint(-10, 10)))
    return im


def cores_side():
    im = crate()
    fill(im, 3, 5, 13, 9, '#e8e0cc')
    for x in range(4, 9):
        dot(im, x, 7, '#2a2a3a')
    return im


def table_top():
    im = new('#8a8a90', 40923, 3)
    for y in (4, 11):
        for x in range(16):
            dot(im, x, y, '#5e3a24'); dot(im, x, y + 1, '#4a2c1a')
        dot(im, 7, y, '#8a8a90'); dot(im, 8, y, '#8a8a90'); dot(im, 7, y + 1, '#8a8a90'); dot(im, 8, y + 1, '#8a8a90')
    for x, y in ((3, 7), (12, 8), (6, 13)):
        dot(im, x, y, '#4a3a3a')
    return im


def table_side():
    im = new('#6a6a70', 40924, 3)
    for y in range(16):
        dot(im, 1, y, '#4a4a50'); dot(im, 14, y, '#4a4a50')
    fill(im, 0, 0, 16, 3, '#8a8a90')
    return im


def fabric():
    im = canvas()
    rnd = random.Random(40925)
    for k in range(3):
        x = 2 + k * 5
        for y in range(1, 15):
            if rnd.random() < .85:
                dot(im, x + (y % 3 == 0), y, (0, 0, 0, 0))
                dot(im, x + 1 + (y % 3 == 0), y, '#a89c7e')
    return im


def prop():
    im = new('#7a5232', 40926, 5)
    for y in range(16):
        dot(im, 7, y, '#5a3a20')
    fill(im, 6, 6, 10, 10, '#3a3a3a')
    return im


def prints():
    im = new(None)
    for ox, oy in ((3, 3), (9, 9)):
        for dx, dy in ((0, 0), (-2, -2), (0, -3), (2, -2), (0, 2)):
            dot(im, ox + 1 + dx, oy + 3 + dy, '#2a2620', 200)
        dot(im, ox + 1, oy + 4, '#2a2620', 200)
    return im


def mass_frames():
    """Eight frames: a dark sheen sliding over the surface, and eyes that open, stare and close in it, never twice in the same place."""
    frames = new(None, size=(16, 128))
    rnd = random.Random(40927)
    eyes = [(rnd.randrange(2, 13), rnd.randrange(2, 13)) for _ in range(4)]
    for f in range(8):
        px = frames.load()
        for y in range(16):
            for x in range(16):
                sheen = math.sin((x + y + f * 2) * .55) * 12 + math.sin((x - y * 2 + f) * .3) * 8
                tint = (int(14 + sheen * .4), int(12 + max(0, sheen) * .9), int(20 + max(0, -sheen) * .9))
                px[x, y + f * 16] = (max(0, tint[0]), max(0, tint[1]), max(0, tint[2]), 255)
        for i, (ex, ey) in enumerate(eyes):
            phase = (f + i * 2) % 8
            if phase in (2, 3, 4):
                open_ = 1 if phase != 3 else 2
                for dx in range(-1, 2):
                    for dy in range(-open_ + 1, open_):
                        dot(frames, ex + dx, ey + dy + f * 16, '#d8d4a0')
                dot(frames, ex, ey + f * 16, '#101010')
            elif phase in (1, 5):
                for dx in range(-1, 2):
                    dot(frames, ex + dx, ey + f * 16, '#6a6650')
    return frames


# ------------------------------------------------------------------------------------------------ the twelve panels
STONE, LINE, RAISE, DEEP, OCHRE = '#2c2b31', '#141317', '#4c4a54', '#0a090c', '#7a5a3a'


class Carving:
    """A 64 x 48 relief on a 64-pixel sheet: incised lines, raised shapes and old pigment, inside a carved frame."""

    def __init__(self, seed):
        self.im = new(STONE, seed, 5, size=(64, 64))
        self.rnd = random.Random(seed + 1)
        for x in range(64):
            for y in (0, 1, 46, 47):
                dot(self.im, x, y, RAISE if y in (0, 47) else LINE)
        for y in range(48):
            for x in (0, 1, 62, 63):
                dot(self.im, x, y, RAISE if x in (0, 63) else LINE)
        for k in range(5):
            dot(self.im, 24 + k * 4, 0, DEEP)
        fill(self.im, 0, 48, 64, 64, STONE)

    def px(self, x, y, c=LINE, a=255):
        if 2 <= x < 62 and 2 <= y < 46:
            dot(self.im, int(x), int(y), c, a)

    def line(self, x0, y0, x1, y1, c=LINE):
        n = int(max(abs(x1 - x0), abs(y1 - y0))) + 1
        for i in range(n):
            t = i / max(1, n - 1)
            self.px(round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t), c)

    def blob(self, cx, cy, rx, ry, c=RAISE, edge=LINE, eyes=0):
        for y in range(int(cy - ry) - 1, int(cy + ry) + 2):
            for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
                d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
                wobble = .12 * math.sin(x * 1.7 + y * 2.3)
                if d <= 1 + wobble:
                    self.px(x, y, edge if d > .7 + wobble else c)
        for _ in range(eyes):
            ex, ey = cx + self.rnd.uniform(-rx * .6, rx * .6), cy + self.rnd.uniform(-ry * .5, ry * .5)
            self.px(round(ex), round(ey), '#b0a878'); self.px(round(ex) + 1, round(ey), DEEP)

    def figure(self, cx, cy, wings=True, c=RAISE):
        """One of the builders: a ribbed barrel of a body, a five-armed head, fan wings folded at its sides."""
        for y in range(-4, 5):
            w = 2 if abs(y) < 3 else 1
            for x in range(-w, w + 1):
                self.px(cx + x, cy + y, LINE if abs(x) == w else c)
        for y in (-2, 0, 2):
            self.px(cx, cy + y, LINE)
        for k in range(5):
            a = -math.pi / 2 + k * 2 * math.pi / 5
            self.px(round(cx + math.cos(a) * 2), round(cy - 6 + math.sin(a) * 2), c)
        self.px(cx, cy - 6, LINE)
        for k in range(3):
            self.px(cx - 2 + k * 2, cy + 6, LINE)
        if wings:
            for i in range(4):
                self.line(cx - 3, cy - 2, cx - 6 - i, cy - 4 + i * 2, LINE)
                self.line(cx + 3, cy - 2, cx + 6 + i, cy - 4 + i * 2, LINE)

    def terraces(self, x0, base, width, steps=5, step=3, c=RAISE):
        for s in range(steps):
            inset = s * (width // (steps * 2 + 1))
            for x in range(x0 + inset, x0 + width - inset):
                for y in range(base - (s + 1) * step, base - s * step):
                    self.px(x, y, LINE if y == base - (s + 1) * step or x in (x0 + inset, x0 + width - inset - 1) else c)

    def stars(self, n, top=3, bottom=16):
        for _ in range(n):
            self.px(self.rnd.randrange(3, 61), self.rnd.randrange(top, bottom), RAISE)

    def waves(self, y0, y1):
        for y in range(y0, y1, 3):
            for x in range(3, 61):
                if math.sin(x * .6 + y) > .3:
                    self.px(x, y, LINE)

    def pigment(self, x0, y0, x1, y1, amount=.25):
        for y in range(y0, y1):
            for x in range(x0, x1):
                if self.rnd.random() < amount and 2 <= x < 62 and 2 <= y < 46:
                    r, g, b, _ = self.im.getpixel((x, y))
                    if (r, g, b) != rgb(LINE)[:3]:
                        dot(self.im, x, y, OCHRE, 255)

    def hack(self, x0, y0, x1, y1):
        """Stone cut away after the carving was made: deep, rough, chisel marks across it."""
        for y in range(y0, y1):
            for x in range(x0, x1):
                self.px(x, y, DEEP if self.rnd.random() < .7 else LINE)
        for i in range(10):
            x = self.rnd.randrange(x0, x1 - 3); y = self.rnd.randrange(y0, y1 - 2)
            self.line(x, y, x + 3, y + 2, RAISE)


def panel(i):
    c = Carving(41000 + i * 7)
    if i == 0:
        c.stars(40)
        for k, (x, y) in enumerate(((14, 16), (32, 26), (50, 18))):
            c.figure(x, y)
            c.line(x - 4, y - 10, x - 1, y - 7, LINE); c.line(x + 4, y - 10, x + 1, y - 7, LINE)
        c.pigment(3, 3, 61, 16, .06)
    elif i == 1:
        c.waves(32, 45)
        c.terraces(18, 34, 28, 3, 2)
        for x, y in ((12, 24), (52, 22)):
            c.figure(x, y)
        c.stars(10, 3, 9)
    elif i == 2:
        c.terraces(6, 44, 34)
        c.terraces(38, 44, 22, 5, 2)
        for x, y in ((10, 20), (44, 16)):
            c.figure(x, y, False)
            c.line(x + 3, y + 1, x + 8, y + 6, LINE)
        c.pigment(6, 29, 40, 44, .12)
    elif i == 3:
        for x0, w in ((4, 14), (20, 18), (40, 12), (52, 9)):
            c.terraces(x0, 44, w, 5, 3 if w > 12 else 2)
        c.blob(46, 9, 5, 5, RAISE, LINE)
        for k in range(8):
            a = k * math.pi / 4
            c.line(46 + math.cos(a) * 7, 9 + math.sin(a) * 7, 46 + math.cos(a) * 9, 9 + math.sin(a) * 9, LINE)
        for x in (18, 37):
            c.line(x, 44, x, 26, LINE)
            for y in range(28, 42, 3):
                c.line(x, y, x - 3, y - 2, LINE); c.line(x, y, x + 3, y - 2, LINE)
        c.pigment(40, 3, 54, 16, .3)
    elif i == 4:
        for y in range(30, 44):
            w = 10 - abs(y - 37) // 2
            c.line(32 - w, y, 32 + w, y, LINE if y in (30, 43) else RAISE)
        c.blob(32, 22, 7, 8, '#1c1b20', LINE)
        for x, d in ((14, 1), (50, -1)):
            c.figure(x, 24, False)
            c.line(x + 2 * d, 20, 32 - 6 * d, 16, LINE)
    elif i == 5:
        c.terraces(4, 44, 56, 5, 7)
        for k in range(5):
            x, y = 10 + k * 10, 38 - k * 6
            c.blob(x, y, 3, 3, '#1c1b20', LINE)
            c.px(x, y - 4, RAISE); c.px(x + 1, y - 4, RAISE); c.px(x, y - 5, RAISE); c.px(x + 1, y - 5, RAISE)
        c.figure(56, 10, False)
    elif i == 6:
        for k, eyes in enumerate((0, 3, 9)):
            c.blob(12 + k * 20, 26, 7, 9, '#1c1b20', LINE, eyes)
        c.line(32 + 20 - 4, 30, 32 + 20 + 2, 31, '#b0a878')
        for k in range(6):
            c.line(48 + k, 36, 50 + k, 40, LINE)
    elif i == 7:
        c.terraces(4, 44, 22, 5, 2)
        c.blob(44, 24, 12, 14, '#1c1b20', LINE, 12)
        for x, y in ((8, 14), (20, 10)):
            c.figure(x, y)
            c.line(x + 3, y + 1, 34, 22 + (x // 10), '#b0a878')
        c.line(4, 38, 26, 30, DEEP)
    elif i == 8:
        c.line(3, 26, 60, 26, LINE)
        c.terraces(8, 26, 20, 3, 2)
        c.terraces(34, 26, 22, 3, 2)
        for k in range(5):
            c.blob(10 + k * 11, 38, 3, 2, '#1c1b20', LINE, 1)
        for x in (18, 44):
            c.figure(x, 13)
        for _ in range(30):
            x, y = c.rnd.randrange(3, 60), c.rnd.randrange(28, 45)
            c.px(x, y, LINE)
    elif i == 9:
        for x in range(3, 61):
            edge = 12 + int(4 * math.sin(x * .7)) + (x % 5 == 0) * 3
            for y in range(3, edge):
                c.px(x, y, '#8a94a4' if (x + y) % 3 else '#a8b4c4')
        c.terraces(6, 44, 50, 5, 3)
        for x in (14, 30, 46):
            c.figure(x, 36, False)
            c.px(x, 29, '#b0a878')
    elif i == 10:
        for r, y0 in ((22, 6), (16, 14), (10, 22), (5, 30)):
            c.line(32 - r, y0, 32 + r, y0, LINE); c.line(32 + r, y0, 32 + r, y0 + 8, LINE)
            c.line(32 + r, y0 + 8, 32 - r + 4, y0 + 8, LINE)
        c.waves(39, 45)
        for x, y in ((20, 11), (40, 19), (30, 27)):
            c.figure(x, y, False)
    else:
        for y in range(6, 34):
            w = 18 if y > 12 else int(math.sqrt(max(0, 18 * 18 - (12 - y) ** 2 * 14)))
            c.px(32 - w, y, LINE); c.px(32 + w, y, LINE)
        c.blob(32, 22, 16, 11, '#1c1b20', LINE, 14)
        c.hack(10, 34, 54, 45)
    return c.im


# ------------------------------------------------------------------------------------------------ assembling
def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/city_{name}.png'] = im
    faces = {
        'radio_front': radio_front(), 'radio_side': radio_side(), 'logbook': logbook(), 'paper_edge': paper_edge(), 'pad_blank': pad(False),
        'pad_written': pad(True), 'lamp_off': lamp_glass(False), 'lamp_on': lamp_glass(True), 'lamp_metal': lamp_metal(), 'crate': crate(),
        'stove': stove(), 'harness': harness(), 'cores_top': cores_top(), 'cores_side': cores_side(), 'table_top': table_top(),
        'table_side': table_side(), 'fabric': fabric(), 'prop': prop(), 'prints': prints(), 'basalt': basalt(), 'mass': mass_frames(),
    }
    for i in range(12):
        faces[f'mural_{i}'] = panel(i)
    for name, im in faces.items():
        out[f'block/city/{name}.png'] = im
    return out


def element(lo, hi, textures, uv=None, rot=None):
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
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle, cutout=True):
    m = {'ambientocclusion': False, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}
    if cutout:
        m['render_type'] = 'minecraft:cutout'
    return m


def fitting_models():
    m = {}
    m['radio_0'] = model([element([2, 0, 5], [14, 9, 13], {'all': 's', 'north': 'f'}), element([11, 9, 9], [12, 15, 10], {'all': 's'})], {'s': 'radio_side', 'f': 'radio_front'}, 'radio_front')
    m['logbook_0'] = model([element([4, 0, 4], [12, 2, 12], {'all': 'e', 'up': 'b'})], {'e': 'paper_edge', 'b': 'logbook'}, 'logbook')
    for stage, face in ((0, 'pad_blank'), (1, 'pad_written')):
        m[f'pad_{stage}'] = model([element([4, 0, 5], [12, 1, 11], {'all': 'e', 'up': 'p'})], {'e': 'paper_edge', 'p': face}, face)
    for stage, glass in ((0, 'lamp_off'), (1, 'lamp_on')):
        m[f'lamp_{stage}'] = model([element([7.5, 13, 7.5], [8.5, 16, 8.5], {'all': 'm'}), element([5, 11, 5], [11, 12, 11], {'all': 'm'}),
                                    element([6, 5, 6], [10, 11, 10], {'all': 'g'}), element([5, 3, 5], [11, 5, 11], {'all': 'm'})], {'m': 'lamp_metal', 'g': glass}, glass)
    m['crate_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'c'})], {'c': 'crate'}, 'crate', False)
    m['stove_0'] = model([element([3, 0, 3], [13, 12, 13], {'all': 'm', 'north': 's'}), element([7, 12, 7], [9, 16, 9], {'all': 'm'})], {'m': 'lamp_metal', 's': 'stove'}, 'stove')
    m['harness_0'] = model([element([1, .1, 1], [15, .3, 15], {'up': 'h', 'down': 'h'})], {'h': 'harness'}, 'harness')
    m['cores_0'] = model([element([1, 0, 3], [15, 6, 13], {'all': 's', 'up': 't'})], {'s': 'cores_side', 't': 'cores_top'}, 'cores_top')
    m['table_0'] = model([element([0, 10, 0], [16, 12, 16], {'all': 's', 'up': 't'}), element([1, 0, 1], [3, 10, 3], {'all': 's'}),
                          element([13, 0, 1], [15, 10, 3], {'all': 's'}), element([1, 0, 13], [3, 10, 15], {'all': 's'}), element([13, 0, 13], [15, 10, 15], {'all': 's'})],
                         {'s': 'table_side', 't': 'table_top'}, 'table_top')
    m['fabric_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'f'})], {'f': 'fabric'}, 'fabric')
    m['prop_0'] = model([element([7, 0, 7], [9, 16, 9], {'all': 'p'}), element([0, 7, 7], [16, 9, 9], {'all': 'p'}), element([6, 6, 6], [10, 10, 10], {'all': 'p'})], {'p': 'prop'}, 'prop')
    m['prints_0'] = model([element([0, .1, 0], [16, .2, 16], {'up': 'p', 'down': 'p'})], {'p': 'prints'}, 'prints')
    return m


def mural_models():
    m = {}
    for i in range(12):
        for part in range(12):
            col, row = part % 4, part // 4
            uv = [col * 4, row * 4, col * 4 + 4, row * 4 + 4]
            m[f'mural_{i}_{part}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b', 'north': 'm'}, uv={'north': uv})], {'b': 'basalt', 'm': f'mural_{i}'}, 'basalt', False)
    return m


KINDS = ['radio', 'logbook', 'pad', 'lamp', 'crate', 'stove', 'harness', 'cores', 'table', 'fabric', 'prop', 'prints']
STAGES = {'pad': 2, 'lamp': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'city_fitting': 'Camp Fitting', 'city_mural': 'Carved Panel', 'city_mass': 'Something in the Tunnel', 'city_basalt': 'Dark Stone',
         'city_fives': 'Stone Carved in Fives', 'city_snow': 'Packed Snow', 'city_ice': 'Blue Ice', 'city_canvas': 'Tent Canvas', 'city_boards': 'Weathered Boards'}
MASS_META = {'animation': {'frametime': 4, 'interpolate': True}}


def files():
    out = {}
    for name in materials():
        out[f'models/block/city_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/city_{name}'}}
        out[f'blockstates/city_{name}.json'] = {'variants': {'': {'model': NS + f'block/city_{name}'}}}
    for key, obj in {**fitting_models(), **mural_models()}.items():
        out[f'models/block/city/{key}.json'] = obj
    out['models/block/city_mass.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + 'mass'}}
    out['blockstates/city_mass.json'] = {'variants': {'': {'model': NS + 'block/city_mass'}}}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(2):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/city/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/city_fitting.json'] = {'variants': variants}
    murals = {}
    for facing, y in FACING_Y.items():
        for i in range(12):
            for part in range(12):
                v = {'model': NS + f'block/city/mural_{i}_{part}'}
                if y:
                    v['y'] = y
                murals[f'facing={facing},panel={i},part={part}'] = v
    out['blockstates/city_mural.json'] = {'variants': murals}
    out['textures/block/city/mass.png.mcmeta'] = MASS_META
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = {**fitting_models(), **mural_models()}
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/city/{tex.split("city/")[1]}.png' in images, (key, tex)
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
    print(('Verified' if check else 'Drew') + f' the camp, the pass and the city on the ice: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
