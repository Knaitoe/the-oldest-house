"""The city on the ice (0.4.409): an expedition's camp, a pass, and a city of dark stone.

Draws, in native Minecraft pixels from fixed seeds: six materials (the city's dark stone, the same stone carved in fives, packed snow,
blue ice, the tents' canvas, and the huts' weathered boards); every fitting of the city_fitting block, its models facing north and turned
by its facing (the radio set with its dials lit, the operator's log, the message pad blank or written on, a hurricane lamp dark or lit, a
supply crate, the oil stove, an empty dog harness, rock cores in their box, the dissecting table with its straps cut, a tent's canvas
opened in cuts, the aeroplane's propeller, and three-toed prints in the grit); what fills the tunnel, an animated dark sheen with eyes
opening and closing in it; and the twelve carved panels along the ramp, wordless, each a 64-pixel picture cut across four blocks by three.

0.4.579: the aeroplane, a high-winged cabin machine on skis, drawn once in its own pixels and cut into the cells it stands in, each a
part of the city_plane block (its models, and the table of where each part stands and what shape it has, which CityPlaneBlock.java
carries between two marked lines); and the fixed rope up the pass, with its stakes, the city_rope block.

Writes the models and block states and the blocks' names. --check verifies everything.
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
    """A tent's canvas: grey-green duck, the weave showing, a seam down one edge where two widths are sewn, and the weather
    on it."""
    im = new('#8a917d', 40907, 2); px = im.load()
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                px[x, y] = shade(px[x, y], -6)
    # The seam: the fold's shadow, and the stitches beside it.
    for y in range(16):
        dot(im, 0, y, '#6c7361')
        if y % 3 != 2:
            dot(im, 1, y, '#a2a894')
    # Where the cloth has hung wet and dried: paler under the top, darker at the foot.
    for x in range(2, 16):
        if x % 5:
            px[x, 2] = shade(px[x, 2], 9)
        px[x, 15] = shade(px[x, 15], -9)
    rnd = random.Random(40970)
    for _ in range(5):
        x, y = rnd.randrange(2, 16), rnd.randrange(4, 14)
        px[x, y] = shade(px[x, y], -11)
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
    """The same canvas opened in two long straight cuts: nothing where the blade went, the cut edge curled pale along one lip
    and the dark of the inside along the other. Each is one straight line from end to end: a cut that stood wider along its
    middle read, in the frames, as a jagged tear."""
    im = canvas(); px = im.load()
    for x, y0, y1 in ((4, 1, 14), (11, 3, 16)):
        for y in range(y0, y1):
            px[x - 1, y] = shade(px[x - 1, y], 18)
            px[x, y] = (0, 0, 0, 0)
            px[x + 1, y] = shade('#2d3129', 0)
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


# ------------------------------------------------------------------------------------------------ the aeroplane
def plane_dope(base='#b3b9be', seam='#989fa6', stitch='#a3aab0', light='#c3c8cc', seed=40940):
    """The aeroplane's doped fabric: the stitching over each rib a faint line with the thread's knots beside it, a seam down
    every sixteenth pixel, and the weather's streaks."""
    im = new(base, seed, 3); rnd = random.Random(seed + 1)
    for y in (3, 11):
        for x in range(16):
            dot(im, x, y, stitch)
            if x % 2 == 0:
                dot(im, x, y + 1, light)
    for y in range(16):
        dot(im, 0, y, seam)
    for _ in range(5):
        x, y = rnd.randrange(1, 16), rnd.randrange(13)
        for k in range(1 + rnd.randrange(3)):
            dot(im, x, y + k, shade(base, -12))
    return im


def plane_dope_tip():
    """The same fabric painted to be seen on snow, at the wing's tips and on the rudder; the paint worn."""
    im = plane_dope('#a9532f', '#8c4124', '#99492a', '#bb6740', 40942); rnd = random.Random(40943)
    for _ in range(7):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#b3b9be')
    return im


def plane_dope_under():
    """The fabric's inside, where a cut has let it hang: bare cloth, in the wing's shadow."""
    im = new('#7a8087', 40944, 3)
    for y in range(0, 16, 2):
        for x in range(y // 2 % 2, 16, 2):
            dot(im, x, y, '#6c7279')
    return im


def plane_slit():
    """What shows through a cut: the dark of the inside, and a stringer crossing it."""
    im = new('#14161a', 40945, 2)
    for x in range(16):
        dot(im, x, 7, '#3a372f')
    for y in range(16):
        dot(im, 5, y, '#2b2e33')
    return im


def plane_cowl():
    """The nose's metal panels: a line where two meet, a rivet every fourth pixel along it."""
    im = new('#8b9197', 40946, 3)
    for k in range(16):
        dot(im, k, 0, '#6f757b'); dot(im, 0, k, '#6f757b')
    for k in range(2, 16, 4):
        dot(im, k, 1, '#aab0b5'); dot(im, 1, k, '#aab0b5')
    return im


def plane_glass():
    im = new('#18212a', 40947, 2)
    for k in range(4):
        dot(im, 3 + k, 9 - k, '#42566a'); dot(im, 10 + k, 13 - k, '#2c3a48')
    return im


def plane_strut():
    im = new('#33383c', 40948, 3)
    for k in range(16):
        dot(im, k, 0, '#4a5156'); dot(im, 0, k, '#4a5156')
    return im


def plane_ski():
    """A ski's ash board: the grain along it, a darker line each side where the sole is shod."""
    im = new('#a58053', 40949, 4)
    for x in range(16):
        dot(im, x, 5, '#8d6b43'); dot(im, x, 10, '#93704a')
        dot(im, x, 0, '#6e5335'); dot(im, x, 15, '#6e5335')
    return im


def plane_engine():
    """A cylinder's cooling fins."""
    im = new('#37393c', 40950, 2)
    for y in range(0, 16, 2):
        for x in range(16):
            dot(im, x, y, '#54585c')
    return im


def r3(v):
    v = round(v, 3)
    return int(v) if v == int(v) else v


def solid(lo, hi, tex, rot=None, skip=(), **over):
    """A box of one texture. Each face is a piece of the picture of the face's own size, taken from where the box stands, so
    nothing is stretched and a picture runs on from one box to the next; a face longer than the picture is the whole of it.
    A face named in skip is left out. A face named in over takes another texture, or a texture and the piece of it to show."""
    w, h, d = hi[0] - lo[0], hi[1] - lo[1], hi[2] - lo[2]

    def at(start, size):
        return (0, 16) if size > 16 else (max(0, min(16 - size, start % 16)), max(0, min(16 - size, start % 16)) + size)
    (u0, u1), (v0, v1), (t0, t1) = at(lo[0], w), at(16 - hi[1], h), at(lo[2], d)
    uv = {'north': [u0, v0, u1, v1], 'south': [u0, v0, u1, v1], 'east': [t0, v0, t1, v1], 'west': [t0, v0, t1, v1],
          'up': [u0, t0, u1, t1], 'down': [u0, t0, u1, t1]}
    faces = {k: tex for k in uv if k not in skip}
    for k, value in over.items():
        if k in skip:
            continue
        if isinstance(value, tuple):
            faces[k], uv[k] = value
        else:
            faces[k] = value
    return element([r3(v) for v in lo], [r3(v) for v in hi], faces, uv={k: [r3(c) for c in uv[k]] for k in faces}, rot=rot)


def swept(lo, hi, rot):
    """The upright box a turned box lies within."""
    if not rot:
        return list(lo), list(hi)
    o = rot['origin']; a = math.radians(rot['angle']); cs, sn = math.cos(a), math.sin(a)
    pts = []
    for x in (lo[0], hi[0]):
        for y in (lo[1], hi[1]):
            for z in (lo[2], hi[2]):
                px, py, pz = x - o[0], y - o[1], z - o[2]
                if rot['axis'] == 'x':
                    py, pz = py * cs - pz * sn, py * sn + pz * cs
                elif rot['axis'] == 'y':
                    px, pz = px * cs + pz * sn, -px * sn + pz * cs
                else:
                    px, py = px * cs - py * sn, px * sn + py * cs
                pts.append((px + o[0], py + o[1], pz + o[2]))
    return [min(p[n] for p in pts) for n in range(3)], [max(p[n] for p in pts) for n in range(3)]


class Plane:
    """The aeroplane, drawn in its own pixels (X from the rudder post to the nose, Y up from the ice, Z across from its
    centreline) and cut into the cells it stands in. A cell is a block of the city_plane block; its model is what of the
    aeroplane is inside it. Each thing drawn may be given a name: everything of one name in a cell is found by a hand as one
    box, and stops a body as one box if it stops one at all."""

    def __init__(self):
        self.cells = {}

    def cell(self, key):
        return self.cells.setdefault(key, {'elements': [], 'found': {}, 'fabric': False})

    def find(self, key, name, lo, hi, stops):
        if name is None:
            return
        lo = [max(0, min(16, v)) for v in lo]; hi = [max(0, min(16, v)) for v in hi]
        if min(h - l for l, h in zip(lo, hi)) <= 0:
            return
        found = self.cell(key)['found']
        if name in found:
            assert found[name][2] == stops, name
            lo = [min(a, b) for a, b in zip(lo, found[name][0])]; hi = [max(a, b) for a, b in zip(hi, found[name][1])]
        found[name] = (lo, hi, stops)

    def box(self, lo, hi, tex, name=None, stops=True, fabric=False, **over):
        """A box, cut at every cell boundary it crosses. The faces a cut makes are inside the thing and are left out."""
        spans = []
        for a, b, shift in ((lo[0], hi[0], 0), (lo[1], hi[1], 0), (lo[2], hi[2], 8)):
            a, b = a + shift, b + shift
            assert b > a, (lo, hi)
            edges = [a] + [16 * k for k in range(math.floor(a / 16) + 1, math.ceil(b / 16))] + [b]
            spans.append([(edges[i], edges[i + 1]) for i in range(len(edges) - 1) if edges[i + 1] - edges[i] > 1e-6])
        ends = ((lo[0], hi[0]), (lo[1], hi[1]), (lo[2] + 8, hi[2] + 8))
        names = (('west', 'east'), ('down', 'up'), ('north', 'south'))
        for x in spans[0]:
            for y in spans[1]:
                for z in spans[2]:
                    key = tuple(math.floor(v[0] / 16 + 1e-9) for v in (x, y, z))
                    l = [v[0] - 16 * k for v, k in zip((x, y, z), key)]; h = [v[1] - 16 * k for v, k in zip((x, y, z), key)]
                    skip = set()
                    for span, end, (low, high) in zip((x, y, z), ends, names):
                        if abs(span[0] - end[0]) > 1e-6:
                            skip.add(low)
                        if abs(span[1] - end[1]) > 1e-6:
                            skip.add(high)
                    c = self.cell(key)
                    c['elements'].append(solid(l, h, tex, skip=skip, **over))
                    c['fabric'] |= fabric
                    self.find(key, name, l, h, stops)

    def both(self, lo, hi, tex, **kw):
        """A box and the same on the other side of the centreline."""
        self.box(lo, hi, tex, **kw)
        self.box([lo[0], lo[1], -hi[2]], [hi[0], hi[1], -lo[2]], tex, **kw)

    def place(self, lo, hi, tex, rot, name):
        """A turned box, whole, in the cell its middle is in: the game turns a box one way only, about one axis, by 22.5
        or 45 degrees, and draws nothing further than a cell beyond its own."""
        m = [(lo[n] + hi[n]) / 2 for n in range(3)]
        key = tuple(math.floor(v / 16) for v in (m[0], m[1], m[2] + 8))
        off = (16 * key[0], 16 * key[1], 16 * key[2] - 8)
        l = [lo[n] - off[n] for n in range(3)]; h = [hi[n] - off[n] for n in range(3)]
        assert min(l) >= -16 and max(h) <= 32, (lo, hi)
        if rot:
            rot = {'origin': [r3(rot['origin'][n] - off[n]) for n in range(3)], 'axis': rot['axis'], 'angle': rot['angle']}
        self.cell(key)['elements'].append(solid(l, h, tex, rot=rot))
        self.find(key, name, *swept(l, h, rot), False)

    def bar(self, a, b, thick, tex, name, pieces=1):
        """A bar from a to b, which lie in a plane square to one axis, at 22.5 or 45 degrees in it. In pieces, so that each
        lies near enough to the cell it is drawn in."""
        diff = [b[i] - a[i] for i in range(3)]
        still = [i for i in range(3) if abs(diff[i]) < 1e-6]
        assert len(still) == 1, (a, b)
        axis = 'xyz'[still[0]]
        # the box lies along the first of the other two axes before it is turned
        i, j = [k for k in range(3) if k != still[0]]
        if axis == 'x':
            i, j = 2, 1                     # along z, turning toward y
        length = math.hypot(diff[i], diff[j])
        if diff[i] < 0:
            a, b, diff = b, a, [-v for v in diff]
        angle = math.degrees(math.atan2(diff[j], diff[i]))
        snapped = min((-45, -22.5, 0, 22.5, 45), key=lambda t: abs(t - angle))
        assert abs(snapped - angle) < 1.6, (a, b, angle)
        turn = {'x': -snapped, 'y': -snapped, 'z': snapped}[axis]
        for k in range(pieces):
            m = [a[n] + diff[n] * (k + .5) / pieces for n in range(3)]
            half = length / pieces / 2
            lo = [m[n] - thick / 2 for n in range(3)]; hi = [m[n] + thick / 2 for n in range(3)]
            lo[i], hi[i] = m[i] - half, m[i] + half
            self.place(lo, hi, tex, {'origin': m, 'axis': axis, 'angle': turn} if turn else None, name)

    def turned(self, lo, hi, tex, axis, angle, origin, name):
        """A box turned about a point."""
        self.place(lo, hi, tex, {'origin': origin, 'axis': axis, 'angle': angle}, name)


# The aeroplane's length in its own pixels: thirteen cells from the rudder post to the nose, and the propeller in the cell
# past that.
PLANE_LENGTH = 208
# Along the body: where, the belly's height, the back's height, half the breadth.
STATIONS = ((4, 6, 12, 1), (40, 8, 23, 4), (80, 11, 31, 6), (116, 14, 37, 7.5), (160, 19, 41, 7.5), (172, 20, 37, 7.5), (184, 21, 37, 7),
            (200, 23, 37, 5.5), (208, 26, 36, 4))
# Across the wing from its trailing edge to its leading edge: from, to, the underside's height, the thickness.
WING = ((116, 120, 38.5, 2), (120, 126, 38.5, 3.5), (126, 136, 39, 5), (136, 146, 40, 6), (146, 151, 41, 5), (151, 156, 42, 3))
SPAN, TIP = 122, 100
# Where the fabric is cut: the wing's cuts run from front to back (where across, from, to, and how far a flap of it hangs
# under the wing, nothing for none); the body's run from back to belly (where along, which side).
WING_CUTS = ((-88, 122, 152, 9), (-61, 126, 150, 0), (-37, 120, 146, 5), (26, 124, 152, 12), (52, 128, 148, 0), (79, 120, 150, 7), (107, 126, 146, 4))
BODY_CUTS = ((30, 1), (47, -1), (58, 1), (72, -1), (88, 1), (97, 1), (104, -1))
# The engine's shaft, and so the propeller's: its height.
SHAFT = 31


def station(x):
    """The body's belly, back and half breadth at a place along it, to the half pixel."""
    for (x0, b0, t0, w0), (x1, b1, t1, w1) in zip(STATIONS, STATIONS[1:]):
        if x0 <= x <= x1:
            k = (x - x0) / (x1 - x0)
            return tuple(round(2 * (a + (b - a) * k)) / 2 for a, b in ((b0, b1), (t0, t1), (w0, w1)))
    raise ValueError(x)


def wing_at(x):
    for x0, x1, under, thick in WING:
        if x0 <= x < x1:
            return under, thick
    raise ValueError(x)


def plane():
    """The aeroplane: a high-winged cabin machine on skis, its tail down on the ice, the fabric of its body and its wing
    opened in long straight cuts."""
    p = Plane()
    # The body, in slices four pixels long, each a little higher and broader than the last; its corners taken off.
    for x0 in range(4, PLANE_LENGTH, 4):
        belly, back, half = station(x0 + 2)
        skin = 'cowl' if x0 >= 172 else 'dope'
        cloth = x0 < 172
        p.box([x0, belly, -half], [x0 + 4, back, half], skin, name='body', fabric=cloth)
        if half >= 3:
            p.box([x0, back, -(half - 2)], [x0 + 4, back + 1, half - 2], skin, name='body', fabric=cloth)
            p.box([x0, belly - 1, -(half - 2)], [x0 + 4, belly, half - 2], skin, name='body', fabric=cloth)
    # The cabin's windows, three a side, and the windscreen over the pilot's seat.
    for x0 in (120, 133, 146):
        _, back, half = station(x0 + 5)
        p.both([x0, back - 8, half], [x0 + 10, back - 2.5, half + .15], 'glass')
    for x0 in range(160, 172, 4):
        _, back, half = station(x0 + 2)
        p.box([x0, back + 1, -(half - 2.5)], [x0 + 4, back + 1.15, half - 2.5], 'glass')
        p.both([x0, back - 6, half], [x0 + 4, back - 1, half + .15], 'glass')
    # The wing, across the cabin's roof: thickest a third of the way back, its tips rounded and painted to be seen on snow.
    for x0, x1, under, thick in WING:
        p.box([x0, under, -TIP], [x1, under + thick, TIP], 'dope', name='wing', fabric=True)
        for side in (-1, 1):
            lo, hi = sorted((side * TIP, side * SPAN))
            p.box([x0, under, lo], [x1, under + thick, hi], 'dope_tip', name='wing', fabric=True)
    for x0, x1, under, thick in WING[1:-1]:
        for side in (-1, 1):
            lo, hi = sorted((side * SPAN, side * (SPAN + 4)))
            p.box([x0, under + .5, lo], [x1, under + thick - .5, hi], 'dope_tip', name='wing', fabric=True)
    for x0, x1, under, thick in WING[2:-2]:
        for side in (-1, 1):
            lo, hi = sorted((side * (SPAN + 4), side * (SPAN + 6)))
            p.box([x0, under + 1, lo], [x1, under + thick - 1, hi], 'dope_tip', name='wing', fabric=True)
    # The wing's cuts: the dark of the inside through the top and through the underside, and what hangs down from some.
    for z, x0, x1, hang in WING_CUTS:
        for a, b, under, thick in WING:
            lo, hi = max(a, x0), min(b, x1)
            if hi <= lo:
                continue
            p.box([lo, under + thick, z - .75], [hi, under + thick + .12, z + .75], 'slit')
            p.box([lo, under - .12, z - .75], [hi, under, z + .75], 'slit')
        if hang:
            under, _ = wing_at(x0 + 8)
            p.box([x0 + 4, under - hang, z + .75], [x0 + 16, under, z + 1.25], 'dope_under', name='flap', stops=False)
            p.box([x0 + 16, under - hang * .6, z - 1.25], [x0 + 24, under, z - .75], 'dope_under', name='flap', stops=False)
    # The body's cuts, from the back down to the belly.
    for x, side in BODY_CUTS:
        belly, back, half = station(x)
        lo, hi = sorted((side * half, side * (half + .12)))
        p.box([x - .75, belly + 1.5, lo], [x + .75, back - 1, hi], 'slit')
        top = back + (1 if half >= 3 else 0); reach = max(half - 2, .5)
        p.box([x - .75, top, -reach if side < 0 else 0], [x + .75, top + .12, 0 if side < 0 else reach], 'slit')
    # The struts from the body's foot out to the wing, two a side.
    for x, out in ((150, 56), (126, 60)):
        belly, back, half = station(x)
        under, _ = wing_at(x)
        rise = (out - half) * math.tan(math.radians(22.5))
        for side in (-1, 1):
            p.bar([x, under + .5 - rise, side * half], [x, under + .5, side * out], 1.5, 'strut', 'strut', pieces=2)
    # The tail: the tailplane through the body, the fin and the rudder over it, a small ski under it.
    p.box([6, 13.25, -42], [32, 15.25, 42], 'dope', name='tail', fabric=True)
    p.both([10, 13.25, 42], [28, 15.25, 46], 'dope', name='tail', fabric=True)
    for x0, x1, top, skin in ((0, 6, 40, 'dope_tip'), (6, 12, 43, 'dope_tip'), (12, 18, 43, 'dope'), (18, 24, 37, 'dope'), (24, 30, 29, 'dope')):
        _, back, _ = station(max(4, x0 + 3))
        p.box([x0, back if x0 else 9, -.75], [x1, top, .75], skin, name='fin', fabric=True)
    p.box([2, 0, -2.5], [22, 1.5, 2.5], 'ski', name='ski')
    p.box([10, 1.5, -1], [12, 7, 1], 'strut', name='tail post', stops=False)
    # The skis: long boards turned up at the toe, a post on each, a leg from each post to the body and a bar between them.
    for side in (-1, 1):
        lo, hi = sorted((side * 18.5, side * 23.5))
        p.box([138, 0, lo], [190, 1.5, hi], 'ski', name='ski')
        p.box([190, 1, lo], [194, 2.5, hi], 'ski', name='ski')
        p.box([194, 2, lo], [197, 3.5, hi], 'ski', name='ski')
        lo, hi = sorted((side * 19.5, side * 22.5))
        p.box([162, 1.5, lo], [166, 8, hi], 'strut', name='post')
        belly, _, half = station(164)
        p.bar([164, 7, side * 21], [164, 7 + (21 - half - 1), side * (half + 1)], 2, 'strut', 'leg')
    p.box([163, 5.75, -19.5], [165, 7.25, 19.5], 'strut', name='spreader', stops=False)
    # The engine: its cylinders stand out round the nose, and its exhaust runs back along one side.
    axis = SHAFT - .5
    p.box([196, axis + 4.5, -2], [202, axis + 11.5, 2], 'engine', name='engine', stops=False)
    p.box([196, axis - 11.5, -2], [202, axis - 4.5, 2], 'engine', name='engine', stops=False)
    p.both([196, axis - 2, 4.5], [202, axis + 2, 11.5], 'engine', name='engine', stops=False)
    for angle in (-45, 45):
        p.turned([196, axis + 4.5, -2], [202, axis + 11.5, 2], 'engine', 'x', angle, (199, axis, 0), 'engine')
        p.turned([196, axis - 11.5, -2], [202, axis - 4.5, 2], 'engine', 'x', angle, (199, axis, 0), 'engine')
    p.box([176, 23.5, -9.25], [198, 25.5, -7.25], 'engine', name='exhaust', stops=False)
    return p


PLANE_TEXTURES = {'dope': 'plane_dope', 'dope_tip': 'plane_dope_tip', 'dope_under': 'plane_dope_under', 'slit': 'plane_slit', 'cowl': 'plane_cowl',
                  'glass': 'plane_glass', 'strut': 'plane_strut', 'ski': 'plane_ski', 'engine': 'plane_engine'}


def plane_cells():
    """The aeroplane's cells in a fixed order: (cell, its model, the boxes that stop a body in it, the boxes a hand finds,
    fabric or not). A box is six numbers, in sixteenths of the cell."""
    p = plane()
    out = []
    for key in sorted(p.cells):
        c = p.cells[key]
        assert c['found'], key
        used = sorted({f['texture'][1:] for e in c['elements'] for f in e['faces'].values()})
        particle = 'plane_dope' if c['fabric'] else PLANE_TEXTURES[used[0]]
        for e in c['elements']:
            assert min(e['from']) >= -16 and max(e['to']) <= 32, (key, e)

        def boxes(only_stops):
            return [r3(v) for name in sorted(c['found']) for lo, hi, stops in [c['found'][name]] if stops or not only_stops for v in lo + hi]
        out.append((key, model(c['elements'], {k: PLANE_TEXTURES[k] for k in used}, particle), boxes(True), boxes(False), c['fabric']))
    return out


def plane_java():
    """The table CityPlaneBlock.java carries between its two marked lines: where each part stands and what shape it has."""
    cells = plane_cells()

    def rows(indent, values, width=150):
        lines, line = [], indent
        for i, v in enumerate(values):
            piece = '{' + ','.join(str(n) for n in v) + '}' + (',' if i + 1 < len(values) else '')
            if len(line) + len(piece) > width and line.strip():
                lines.append(line); line = indent
            line += piece
        return lines + [line]
    pad = ' ' * 8
    lines = ['    /** How many parts the aeroplane is in: one to a cell it stands in. */',
             '    public static final int PARTS=' + str(len(cells)) + ';',
             '    /** Where each part stands from the cell of the rudder post\'s foot: toward the nose, up, across. */',
             '    static final int[][] CELLS={', *rows(pad, [key for key, *_ in cells]), '    };',
             '    /** What of each part stops a body: boxes of six numbers (x0,y0,z0,x1,y1,z1) in sixteenths of the cell; none stops nobody. */',
             '    static final double[][] SOLID={', *rows(pad, [stops for _, _, stops, _, _ in cells]), '    };',
             '    /** What of each part a hand finds: boxes as above. */',
             '    static final double[][] REACH={', *rows(pad, [reach for _, _, _, reach, _ in cells]), '    };',
             '    /** Which parts are covered in fabric: one mark a part. */',
             '    static final String FABRIC="' + ''.join('1' if fabric else '0' for *_, fabric in cells) + '";',
             '    /** The cell the propeller stands in, past the nose, from the same cell as the parts. */',
             '    static final int[] PROPELLER={' + ','.join(str(v) for v in (PLANE_LENGTH // 16, SHAFT // 16, 0)) + '};']
    return '\n'.join(lines) + '\n'


# ------------------------------------------------------------------------------------------------ the propeller
def prop():
    """The propeller's laminated wood: the glue lines down the blade, and brass over its tip."""
    im = new('#8a6038', 40926, 4)
    for x in (2, 5, 7, 10, 13):
        for y in range(16):
            dot(im, x, y, '#70491f')
    for y in range(16):
        dot(im, 8, y, '#9c7044')
    fill(im, 0, 0, 16, 3, '#b0924a')
    for x in range(16):
        dot(im, x, 2, '#8a7034')
    dot(im, 7, 1, '#d8c27a'); dot(im, 9, 1, '#d8c27a')
    return im


def prop_hub():
    """The propeller's boss: steel, and in the picture's first corner its face, four pixels square, the shaft's nut in the
    middle of it."""
    im = new('#3b4045', 40927, 3)
    for k in range(4, 16, 4):
        for x in range(16):
            dot(im, x, k, '#2c3035')
    fill(im, 0, 0, 4, 4, '#2a2e32')
    fill(im, 1, 1, 3, 3, '#8f969d')
    dot(im, 1, 1, '#b4bac0')
    return im


def prop_model():
    """A two-bladed propeller on its boss, drawn facing north: the boss runs back to the nose it is on, the blades stand a
    little off upright as they were left."""
    hub = SHAFT % 16
    spin = {'origin': [8, hub, 13], 'axis': 'z', 'angle': 22.5}
    def boss(lo, hi, face):
        """A box of the boss's steel; its two ends take the piece of the picture named, its sides plain steel of their size."""
        w, h, d = hi[0] - lo[0], hi[1] - lo[1], hi[2] - lo[2]
        return element(lo, hi, {'all': 'h'}, uv={'north': face, 'south': face, 'east': [5, 5, 5 + d, 5 + h], 'west': [5, 5, 5 + d, 5 + h],
                                               'up': [5, 9, 5 + w, 9 + d], 'down': [5, 9, 5 + w, 9 + d]})
    elements = [boss([6.5, hub - 1.5, 13.75], [9.5, hub + 1.5, 16], [8, 5, 11, 8]),
                boss([6, hub - 2, 12.25], [10, hub + 2, 13.75], [0, 0, 4, 4]),
                boss([7, hub - 1, 10.75], [9, hub + 1, 12.25], [1, 1, 3, 3])]
    # Each blade in two lengths, the outer one narrower; the second blade is the first turned half round the shaft, so the
    # picture on it is turned too and the brass is at its tip.
    for sign in (1, -1):
        for near, far, half, rows in ((2, 9.5, 1.5, (8.5, 16)), (9.5, 17, 1, (0, 7.5))):
            lo = [8 - half, hub + near, 12.5] if sign > 0 else [8 - half, hub - far, 12.5]
            hi = [8 + half, hub + far, 13.5] if sign > 0 else [8 + half, hub - near, 13.5]
            u0, u1 = 8 - half, 8 + half
            v0, v1 = rows
            face = [u0, v0, u1, v1] if sign > 0 else [u1, v1, u0, v0]
            edge = [7, v0, 8, v1] if sign > 0 else [8, v1, 7, v0]
            end = [u0, 0, u1, 1]
            elements.append(element(lo, hi, {'all': 'p'}, uv={'north': face, 'south': face, 'east': edge, 'west': edge, 'up': end, 'down': end}, rot=spin))
    return model([{**e, 'from': [r3(v) for v in e['from']], 'to': [r3(v) for v in e['to']]} for e in elements], {'p': 'prop', 'h': 'prop_hub'}, 'prop')


# ------------------------------------------------------------------------------------------------ the fixed rope
def rope():
    """A hemp rope: the lay of its strands a slanting mark every third pixel."""
    im = new('#c2b083', 40960, 3)
    for y in range(16):
        for x in range(16):
            if (x + y) % 3 == 0:
                dot(im, x, y, '#9b875a')
            elif (x + y) % 3 == 1 and y % 2 == 0:
                dot(im, x, y, '#d3c39a')
    return im


def stake():
    """A stake's split wood: the grain down it, weathered grey, the top bruised where it was driven."""
    im = new('#8e7b5f', 40961, 4)
    for x in (1, 4, 6, 9, 12, 14):
        for y in range(16):
            if (x * 3 + y) % 7:
                dot(im, x, y, '#6f5e45')
    for x in range(16):
        dot(im, x, 0, '#5c4d38'); dot(im, x, 1, '#a39075')
    return im


# The rope stands this many pixels out from the side of its cell that the rock is on, and this high over the ground at the
# edge of a row; on a row of steps it climbs a whole block across the cell.
ROPE_OUT, ROPE_HIGH = 3, 18
ROPE_SLOPES = ('flat', 'rise', 'fall')          # level; rising the way the block faces; falling the way it faces
ROPE_STAKES = ('none', 'both', 'ahead', 'behind')  # no stake; a stake the rope runs past; one it runs on from only ahead, or only behind


def rope_high(slope, z):
    """The rope's height at a place across its cell, drawn facing north: 0 is the edge ahead, 16 the edge behind."""
    return ROPE_HIGH if slope == 'flat' else ROPE_HIGH - z if slope == 'rise' else ROPE_HIGH - (16 - z)


def rope_run(slope, z0, z1):
    """The rope from one place across the cell to another: a level bar, or bars turned to the steps' slope."""
    x0, x1 = ROPE_OUT - .5, ROPE_OUT + .5
    out = []
    if slope == 'flat':
        uv = {'east': [z0, 4, z1, 5], 'west': [z0, 7, z1, 8], 'up': [0, z0, 1, z1], 'down': [3, z0, 4, z1], 'north': [0, 0, 1, 1], 'south': [0, 0, 1, 1]}
        return [element([x0, ROPE_HIGH - .5, z0], [x1, ROPE_HIGH + .5, z1], {'all': 'r'}, uv=uv)]
    pieces = 1 if z1 - z0 <= 8 else 2
    for k in range(pieces):
        a, b = z0 + (z1 - z0) * k / pieces, z0 + (z1 - z0) * (k + 1) / pieces
        mz = (a + b) / 2; my = rope_high(slope, mz); half = (b - a) / 2 * math.sqrt(2)
        long = r3(2 * half)
        uv = {'east': [0, 4, long, 5], 'west': [0, 7, long, 8], 'up': [0, 0, 1, long], 'down': [3, 0, 4, long], 'north': [0, 0, 1, 1], 'south': [0, 0, 1, 1]}
        out.append(element([x0, r3(my - .5), r3(mz - half)], [x1, r3(my + .5), r3(mz + half)], {'all': 'r'}, uv=uv,
                           rot={'origin': [ROPE_OUT, r3(my), r3(mz)], 'axis': 'x', 'angle': 45 if slope == 'rise' else -45}))
    return out


def rope_stake_at(slope):
    """Where across its cell a stake stands: in the middle of a level row, on the upper tread of a row of steps."""
    return 8 if slope == 'flat' else 4 if slope == 'rise' else 12


def rope_model(slope, staked):
    """One cell of the fixed rope, drawn facing north with the rock to its west."""
    if staked == 'none':
        return model(rope_run(slope, 0, 16), {'r': 'rope'}, 'rope')
    z = rope_stake_at(slope); high = rope_high(slope, z)
    def post(y0, y1, v0):
        """A length of the stake: each side a strip of the grain two pixels wide, from the row named down."""
        v1 = v0 + (y1 - y0)
        return element([ROPE_OUT - 1, y0, z - 1], [ROPE_OUT + 1, y1, z + 1], {'all': 's'},
                       uv={'north': [0, v0, 2, v1], 'south': [4, v0, 6, v1], 'east': [8, v0, 10, v1], 'west': [12, v0, 14, v1], 'up': [0, 0, 2, 2], 'down': [0, 14, 2, 16]})
    top = high + 2
    knot = {k: [0, 0, 3, 3] for k in ('north', 'south', 'east', 'west', 'up', 'down')}
    # A stake taller than the picture is in two lengths, so that its bruised top is drawn once, at its top.
    elements = [post(0, top, 0)] if top <= 16 else [post(top - 16, top, 0), post(0, top - 16, 32 - top)]
    elements.append(element([ROPE_OUT - 1.5, high - 1.5, z - 1.5], [ROPE_OUT + 1.5, high + 1.5, z + 1.5], {'all': 'r'}, uv=knot))
    if staked in ('both', 'ahead'):
        elements += rope_run(slope, 0, z - 1.5)
    if staked in ('both', 'behind'):
        elements += rope_run(slope, z + 1.5, 16)
    if staked != 'both':
        # The end of the rope hangs from its hitch, on the side the rope does not run on from.
        tail = z + 1.5 if staked == 'ahead' else z - 2.5
        elements.append(element([ROPE_OUT - .5, high - 6, tail], [ROPE_OUT + .5, high - .5, tail + 1], {'all': 'r'},
                                uv={'north': [0, 0, 1, 5.5], 'south': [1, 0, 2, 5.5], 'east': [2, 0, 3, 5.5], 'west': [3, 0, 4, 5.5], 'up': [0, 0, 1, 1], 'down': [0, 0, 1, 1]}))
    return model(elements, {'r': 'rope', 's': 'stake'}, 'stake')


def rope_models():
    return {f'rope_{slope}_{staked}': rope_model(slope, staked) for slope in ROPE_SLOPES for staked in ROPE_STAKES}


# ------------------------------------------------------------------------------------------------ assembling
def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/city_{name}.png'] = im
    faces = {
        'radio_front': radio_front(), 'radio_side': radio_side(), 'logbook': logbook(), 'paper_edge': paper_edge(), 'pad_blank': pad(False),
        'pad_written': pad(True), 'lamp_off': lamp_glass(False), 'lamp_on': lamp_glass(True), 'lamp_metal': lamp_metal(), 'crate': crate(),
        'stove': stove(), 'harness': harness(), 'cores_top': cores_top(), 'cores_side': cores_side(), 'table_top': table_top(),
        'table_side': table_side(), 'fabric': fabric(), 'prop': prop(), 'prop_hub': prop_hub(), 'rope': rope(), 'stake': stake(),
        'prints': prints(), 'basalt': basalt(), 'mass': mass_frames(),
        'plane_dope': plane_dope(), 'plane_dope_tip': plane_dope_tip(), 'plane_dope_under': plane_dope_under(), 'plane_slit': plane_slit(),
        'plane_cowl': plane_cowl(), 'plane_glass': plane_glass(), 'plane_strut': plane_strut(), 'plane_ski': plane_ski(), 'plane_engine': plane_engine(),
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
    m['prop_0'] = prop_model()
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
         'city_fives': 'Stone Carved in Fives', 'city_snow': 'Packed Snow', 'city_ice': 'Blue Ice', 'city_canvas': 'Tent Canvas', 'city_boards': 'Weathered Boards',
         'city_plane': 'Aeroplane', 'city_rope': 'Fixed Rope'}
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/CityPlaneBlock.java'
JAVA_FROM = '    // ---- drawn by tools/draw_city_art.py: nothing between this line and the next like it is written by hand ----\n'
JAVA_TO = '    // ---- the end of what the tool draws ----\n'
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
    # The aeroplane: a model to a part, drawn as it stands, its nose to the east.
    parts = plane_cells()
    for n, (_, obj, *_rest) in enumerate(parts):
        out[f'models/block/city/plane_{n}.json'] = obj
    out['blockstates/city_plane.json'] = {'variants': {f'part={n}': {'model': NS + f'block/city/plane_{n}'} for n in range(len(parts))}}
    # The fixed rope: a model to a slope and a stake, turned by its facing.
    ropes = {}
    for key, obj in rope_models().items():
        out[f'models/block/city/{key}.json'] = obj
    for facing, y in FACING_Y.items():
        for slope in ROPE_SLOPES:
            for staked in ROPE_STAKES:
                v = {'model': NS + f'block/city/rope_{slope}_{staked}'}
                if y:
                    v['y'] = y
                ropes[f'facing={facing},slope={slope},stake={staked}'] = v
    out['blockstates/city_rope.json'] = {'variants': ropes}
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
    models = {key[len('models/block/city/'):-len('.json')]: obj for key, obj in data.items() if key.startswith('models/block/city/')}
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
    # The rope is drawn to the two numbers its block tells the game.
    assert f'public static final int OUT={ROPE_OUT},HIGH={ROPE_HIGH};' in (JAVA.parent / 'CityRopeBlock.java').read_text(), 'the tool and CityRopeBlock.java draw the rope to different numbers'
    # The aeroplane's table, in the class that places it.
    java = JAVA.read_text(); a = java.index(JAVA_FROM) + len(JAVA_FROM); b = java.index(JAVA_TO); table = plane_java()
    if check:
        assert java[a:b] == table, 'CityPlaneBlock.java does not carry the table this tool draws'
    elif java[a:b] != table:
        JAVA.write_text(java[:a] + table + java[b:])
    print(('Verified' if check else 'Drew') + f' the camp, the pass and the city on the ice: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
