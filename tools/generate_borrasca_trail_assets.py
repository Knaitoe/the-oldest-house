"""Native meshes and authored 16-pixel material tiles, matching vanilla's pixel scale.

0.4.121: the sixteen trail tiles are redrawn with shading and detail, and the town's fittings (siding, steel, rust,
paper, the noticeboard, filing drawers, the belt, truck paint and the machine frame) get their own 16-pixel block
textures in textures/block/borrasca/ in place of the old eight-pixel cast atlas squares.
"""
import argparse
import json
import math
import random
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
ATLAS = 'the_oldest_house:entity/borrasca_trail_atlas'
CAST_ATLAS = 'the_oldest_house:entity/borrasca_cast_pixels'
FACES = ('north', 'south', 'east', 'west', 'up', 'down')
GLYPHS = {
    '1': ('010','110','010','010','010','010','111'),
    '2': ('111','001','001','111','100','100','111'),
    '3': ('111','001','001','111','001','001','111'),
    '4': ('101','101','101','111','001','001','001'),
    'K': ('101','110','100','110','101'), 'I': ('111','010','010','010','111'),
    'M': ('101','111','111','101','101'), 'B': ('110','101','110','101','110'),
    'E': ('111','100','110','100','111'), 'R': ('110','101','110','110','101'),
}


def rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c[:3]) + (255,)


def mix(a, b, t):
    return clamp(tuple(a[i] + (b[i] - a[i]) * t for i in range(3)))


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


class Tile:
    def __init__(self, base, seed):
        self.im = Image.new('RGBA', (16, 16), rgb(base) if isinstance(base, str) else base)
        self.p = self.im.load()
        self.r = random.Random(seed)

    def set(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.p[x, y] = rgb(c) if isinstance(c, str) else c

    def get(self, x, y):
        return self.p[x % 16, y % 16]

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def outline(self, x0, y0, x1, y1, c):
        for x in range(x0, x1 + 1):
            self.set(x, y0, c); self.set(x, y1, c)
        for y in range(y0, y1 + 1):
            self.set(x0, y, c); self.set(x1, y, c)

    def jitter(self, amount, x0=0, y0=0, x1=15, y1=15):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                f = 1 + self.r.uniform(-amount, amount)
                self.p[x, y] = mul(self.p[x, y], f)

    def speck(self, n, colours, x0=0, y0=0, x1=15, y1=15):
        for _ in range(n):
            self.set(self.r.randint(x0, x1), self.r.randint(y0, y1), self.r.choice(colours))


# ---- The trail atlas: sixteen tiles in a fixed order the models rely on ----------------------------------------------

def bark(t):
    plates = [(0, 2), (3, 6), (7, 9), (10, 12), (13, 15)]
    deep, dark, mid, light = rgb('#2a2119'), rgb('#3f3124'), rgb('#55432f'), rgb('#6e5940')
    for x0, x1 in plates:
        y = t.r.randint(-6, 0)
        while y < 16:
            h = t.r.randint(4, 8)
            for yy in range(max(0, y), min(16, y + h)):
                for x in range(x0, x1 + 1):
                    c = mid if x not in (x0, x1) else dark
                    if x == x0 + 1 and yy < y + h - 1:
                        c = light
                    t.set(x, yy, c)
            if 0 <= y + h - 1 < 16:
                for x in range(x0, x1 + 1):
                    t.set(x, y + h - 1, dark)
            y += h
    for yy in range(16):
        for x in (3 - 1, 7 - 1, 10 - 1, 13 - 1):
            t.set(x, yy, deep if t.r.random() < .8 else dark)
    t.speck(5, ['#6f7a5a', '#7c8762'])
    t.jitter(.05)


def paintings():
    tiles = []

    def tile(base, seed):
        t = Tile(base, seed)
        tiles.append(t)
        return t

    # 0 Bark of the Triple Tree.
    bark(tile('#4a3a2a', 10))
    # 1 The same bark with her name gouged out: pale fresh wood in flat chisel strokes, shadowed below.
    t = tile('#4a3a2a', 10); bark(t)
    pale, light, shadow = rgb('#c4a776'), rgb('#e0c897'), rgb('#86693f')
    for row, (x0, x1) in zip(range(4, 11), [(4, 11), (3, 12), (2, 13), (2, 13), (3, 12), (3, 13), (5, 11)]):
        for x in range(x0 + t.r.randint(0, 1), x1 - t.r.randint(0, 1) + 1):
            t.set(x, row, light if row in (4, 7) else pale)
        t.set(x1 + 1, row, shadow)
    for x in range(3, 13):
        t.set(x, 11, shadow)
    for x, y in ((5, 5), (8, 6), (10, 5), (6, 8), (9, 9), (11, 8)):
        t.set(x, y, '#a88a5c')
    # 2 Aged boards: grey, split and nailed, with moss where the rain sits.
    t = tile('#7b7464', 20)
    for y0 in (0, 4, 8, 12):
        t.rect(0, y0, 15, y0, '#8f8877')
        t.rect(0, y0 + 3, 15, y0 + 3, '#3f3a31')
        for x in range(16):
            if t.r.random() < .3:
                t.set(x, y0 + 1 + t.r.randint(0, 1), '#6a6354')
        t.set(t.r.randint(1, 3), y0 + 1, '#2f2b25'); t.set(t.r.randint(12, 14), y0 + 1, '#2f2b25')
    t.rect(1, 13, 4, 14, '#5b6b3c'); t.set(2, 12, '#6c7d46'); t.set(5, 14, '#55643a')
    for x in range(7, 12):
        t.set(x, 6, '#4d473c')
    t.jitter(.04)
    # 3 The trail: packed red clay with pebbles, a root and needles.
    t = tile('#8b583c', 30); t.jitter(.07)
    for _ in range(9):
        x, y = t.r.randint(0, 14), t.r.randint(0, 14)
        t.set(x, y, '#a49c8c'); t.set(x + 1, y, '#8c8576'); t.set(x, y + 1, '#6b6457')
    for x in range(16):
        y = 10 + round(1.5 * math.sin(x * .7))
        t.set(x, y, '#5a4330'); t.set(x, y - 1, '#6f5540')
    t.speck(10, ['#74472f', '#6d5a2a', '#9a6a48'])
    # 4 Hazard tape: yellow with black diagonals, a sheen on top and a fold.
    t = tile('#d8b93a', 40)
    for y in range(16):
        for x in range(16):
            if (x + y) % 8 < 4:
                t.set(x, y, '#2b2b26')
    for x in range(16):
        t.set(x, 0, mix(t.get(x, 0), rgb('#fff2a0'), .5)); t.set(x, 15, mul(t.get(x, 15), .7))
    for y in range(16):
        t.set(9, y, mul(t.get(9, y), .82))
    # 5 Galvanized steel: brushed, with a seam.
    t = tile('#9aa19c', 50)
    for x in range(16):
        f = 1 + .06 * ((x * 7) % 5 - 2) / 2
        for y in range(16):
            t.set(x, y, mul(t.get(x, y), f))
    t.rect(0, 7, 15, 7, '#6f7772'); t.rect(0, 8, 15, 8, '#b9c0ba'); t.jitter(.03)
    # 6 Polished hood panel: bevelled, riveted, brushed.
    t = tile('#a9b0aa', 60); t.jitter(.025)
    t.rect(0, 0, 15, 0, '#d3d8cf'); t.rect(0, 0, 0, 15, '#c4cac1'); t.rect(0, 15, 15, 15, '#6a726c'); t.rect(15, 0, 15, 15, '#788079')
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        t.set(x, y, '#7c847e'); t.set(x - 1, y - 1, '#e2e6dc')
    t.rect(1, 8, 14, 8, '#8f968f')
    # 7 Her compass: brass case, cream dial, red and blue needle, red tape round the rim.
    t = tile('#5c4a2c', 70)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 7.6:
                t.set(x, y, '#e2c27a' if d > 6.4 and x + y < 15 else '#b48d46' if d > 5.4 else '#7c5f2b' if d > 4.9 else '#e3dcc0')
    for i in range(-3, 4):
        t.set(7 + i // 2, 7 - i, '#a43c32' if i > 0 else '#4d5f6f')
    t.set(7, 7, '#2e2a22'); t.set(8, 7, '#2e2a22')
    for x in range(10, 16):
        t.set(x, 15 - (x - 10), '#9a3b33'); t.set(x, 14 - (x - 10), '#b2463c')
    # 8 Kimber's pack: teal canvas, tan straps and buckles, a zip.
    t = tile('#3f6f68', 80)
    for y in range(16):
        for x in range(16):
            if (x + y) % 2:
                t.set(x, y, '#457770')
    t.jitter(.04)
    for x0 in (3, 11):
        t.rect(x0, 0, x0 + 1, 15, '#8e7d58'); t.rect(x0, 0, x0, 15, '#a6946a'); t.rect(x0, 9, x0 + 1, 10, '#cfc6a0')
    t.rect(0, 2, 15, 2, '#2c4b46'); t.rect(7, 1, 8, 3, '#b9b49a')
    # 9 Name patch: maroon cloth, cream stitching round the edge.
    t = tile('#6e3238', 90)
    t.rect(2, 2, 13, 13, '#7a3a40')
    for i in range(1, 15, 2):
        for c in ((i, 1), (i, 14), (1, i), (14, i)):
            t.set(*c, '#d1b888')
    t.jitter(.05)
    # 10 Flashlight grip: ribbed black rubber.
    t = tile('#2e3633', 100)
    for y in range(0, 16, 3):
        t.rect(0, y, 15, y, '#435049'); t.rect(0, y + 1, 15, y + 1, '#222927')
    # 11 Padlock: brass body, keyhole, a worn rim.
    t = tile('#8a6b33', 110)
    t.rect(1, 1, 14, 14, '#b08a42'); t.rect(1, 1, 14, 1, '#d8b56c'); t.rect(1, 1, 1, 14, '#c9a35a'); t.rect(1, 14, 14, 14, '#6e5428')
    t.rect(7, 5, 8, 8, '#2c2a22'); t.rect(7, 8, 8, 11, '#2c2a22'); t.rect(6, 5, 9, 6, '#2c2a22'); t.jitter(.04)
    # 12 Enamel plate behind the marker digits and letters.
    t = tile('#d6cfb2', 120)
    t.outline(0, 0, 15, 15, '#5f5a48'); t.rect(1, 1, 14, 1, '#ebe5cb'); t.speck(4, ['#8a8574', '#a8a28c'], 1, 1, 14, 14)
    # 13 County paint: white, a blue stripe, the gold star.
    t = tile('#dcdccf', 130); t.jitter(.02)
    t.rect(0, 7, 15, 9, '#36597a'); t.rect(0, 6, 15, 6, '#5a7d99')
    for x, y in ((7, 2), (6, 3), (7, 3), (8, 3), (5, 4), (9, 4), (7, 4), (6, 5), (8, 5)):
        t.set(x, y, '#c4a23f')
    t.rect(0, 15, 15, 15, '#a9a99c')
    # 14 The ridge lamp, lit: a lens in rings round a white-hot centre.
    t = tile('#6b5e3b', 140)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            t.set(x, y, '#fff6cf' if d < 2.2 else '#f6e39a' if d < 4 else '#e9c866' if d < 5.6 else '#c9a245' if d < 7 else '#6b5e3b')
    # 15 Breaker panel: grey box, four switches, one red, a warning label.
    t = tile('#7c847e', 150); t.jitter(.03)
    t.outline(0, 0, 15, 15, '#4f5651')
    for i, (x, y) in enumerate(((3, 3), (8, 3), (3, 8), (8, 8))):
        t.rect(x, y, x + 3, y + 3, '#454b47'); t.rect(x + 1, y + (0 if i else 2), x + 2, y + (1 if i else 3), '#c94a3b' if i == 0 else '#d6d9d1')
    t.rect(3, 13, 12, 14, '#d8c13c'); t.rect(5, 13, 6, 14, '#2b2b26'); t.rect(9, 13, 10, 14, '#2b2b26')
    atlas = Image.new('RGBA', (64, 64))
    for i, t in enumerate(tiles):
        atlas.paste(t.im, ((i % 4) * 16, (i // 4) * 16))
    assert len(tiles) == 16
    return atlas


# ---- The town's materials: ordinary block textures, one file each -----------------------------------------------------

def corrugated(t, palette):
    for x in range(16):
        c = palette[x % 4]
        for y in range(16):
            t.set(x, y, c)
    t.jitter(.03)


def materials():
    out = {}
    # Board-and-batten siding, stained brown and weathered.
    t = Tile('#6b5a43', 200)
    for x in range(16):
        for y in range(16):
            if t.r.random() < .08:
                t.set(x, y, '#5d4d39')
    for x0 in (0, 8):
        t.rect(x0 + 6, 0, x0 + 7, 15, '#7d6a50'); t.rect(x0 + 6, 0, x0 + 6, 15, '#8f7b5e'); t.rect(x0 + 8 if x0 + 8 < 16 else 0, 0, x0 + 8 if x0 + 8 < 16 else 0, 15, '#4a3d2e')
        t.set(x0 + 6, 2, '#33302b'); t.set(x0 + 6, 13, '#33302b')
    for x in (2, 4, 10, 12):
        for y in range(16):
            if t.r.random() < .3:
                t.set(x, y, '#5a4a37')
    t.jitter(.04); out['timber'] = t.im
    # Galvanized corrugated steel, a bolt row top and bottom, a few spangles.
    t = Tile('#9da4a3', 210)
    corrugated(t, [rgb('#bcc2c0'), rgb('#a0a7a6'), rgb('#7f8685'), rgb('#9aa1a0')])
    for x in range(1, 16, 4):
        t.set(x, 1, '#5e6463'); t.set(x, 14, '#5e6463')
    t.speck(6, ['#c9cecb', '#8d9493']); out['steel'] = t.im
    # The same sheet rusted: streaked orange-brown, a little galvanizing left.
    t = Tile('#8a5a3c', 220)
    corrugated(t, [rgb('#a8714a'), rgb('#94603f'), rgb('#6e4630'), rgb('#8a593b')])
    for _ in range(5):
        x, y = t.r.randint(0, 15), t.r.randint(0, 9)
        for yy in range(y, min(16, y + t.r.randint(3, 7))):
            t.set(x, yy, mul(t.get(x, yy), .78))
    for x, y in ((9, 3), (10, 3), (10, 4), (11, 4), (10, 5), (3, 10), (4, 10), (4, 11)):
        t.set(x, y, '#9a9f9b' if x % 4 != 2 else '#80857f')
    for x in range(1, 16, 4):
        t.set(x, 1, '#4a3324'); t.set(x, 14, '#4a3324')
    out['rust'] = t.im
    # A typed sheet on a desk: header bar, lines, a red receiving stamp.
    t = Tile('#e6dfc8', 230)
    t.outline(0, 0, 15, 15, '#c4bba3'); t.rect(2, 2, 9, 2, '#56544c')
    for y in (4, 6, 8, 10):
        t.rect(2, y, 2 + t.r.randint(8, 11), y, '#8c8a80')
    for x, y in ((11, 11), (12, 11), (13, 11), (11, 12), (13, 12), (11, 13), (12, 13), (13, 13)):
        t.set(x, y, '#9c3b32')
    t.jitter(.02); out['paper'] = t.im
    # The noticeboard: cork, a white sheet, a yellow flyer, and a MISSING poster with a small grey photograph.
    t = Tile('#a87c4f', 240)
    t.speck(60, ['#8f6640', '#c09060', '#9a7048'])
    t.rect(1, 1, 7, 8, '#e8e2cf'); t.rect(1, 8, 7, 8, '#c8c1aa')
    for y in (3, 5, 7):
        t.rect(2, y, 6, y, '#8f8b7e')
    t.set(4, 1, '#b8352c')
    t.rect(9, 2, 14, 7, '#e3cf63'); t.rect(10, 3, 13, 3, '#5e5532'); t.rect(10, 5, 12, 5, '#8d7f45'); t.set(11, 2, '#2d5f8a')
    t.rect(4, 9, 11, 15, '#efeadb'); t.rect(4, 9, 11, 10, '#1f1d1a'); t.rect(6, 11, 9, 13, '#8a8a84'); t.rect(7, 11, 8, 12, '#6a6a65')
    t.rect(5, 14, 10, 14, '#77746a'); t.set(7, 9, '#efeadb'); t.set(5, 9, '#efeadb'); t.set(10, 9, '#efeadb')
    out['notice'] = t.im
    # Dark stained frame wood for the board's edge.
    t = Tile('#3e2f22', 250)
    for y in range(16):
        for x in range(16):
            if (x * 3 + y * 7) % 11 == 0 or t.r.random() < .12:
                t.set(x, y, '#4c3a2a')
    t.rect(0, 0, 15, 0, '#5a4532'); out['notice_frame'] = t.im
    # A spruce board with initials cut into it.
    t = Tile('#7a5c3c', 260)
    for y in range(16):
        for x in range(16):
            if (y + (x // 5)) % 4 == 0:
                t.set(x, y, '#6c5034')
    for x, y in ((3, 5), (3, 6), (3, 7), (3, 8), (4, 6), (5, 5), (5, 8), (4, 7), (8, 5), (8, 6), (8, 7), (8, 8), (9, 5), (10, 6), (9, 7), (10, 8), (12, 6), (12, 7)):
        t.set(x, y, '#d1b285'); t.set(x, y + 1, '#4f3a25')
    t.jitter(.04); out['carving'] = t.im
    # A steel filing cabinet: three drawers, label holders, pulls; and its plain side.
    t = Tile('#7b8270', 270); t.jitter(.025)
    for y0 in (1, 6, 11):
        t.rect(1, y0, 14, y0 + 3, '#858c79'); t.rect(1, y0, 14, y0, '#9aa18c'); t.rect(1, y0 + 3, 14, y0 + 3, '#5b6253')
        t.rect(6, y0 + 1, 9, y0 + 1, '#cfc7a4'); t.rect(6, y0 + 2, 9, y0 + 2, '#3d413a')
    t.outline(0, 0, 15, 15, '#4c5246'); out['drawers'] = t.im
    t = Tile('#7b8270', 271); t.jitter(.025)
    t.rect(0, 0, 0, 15, '#9aa18c'); t.rect(15, 0, 15, 15, '#4c5246'); t.rect(0, 0, 15, 0, '#8f9682'); t.rect(0, 15, 15, 15, '#4c5246')
    out['drawers_side'] = t.im
    # The belt: black rubber ribbed across, worn grey down the middle; its frame with rollers and bolts.
    t = Tile('#2b2b2a', 280)
    for y in range(0, 16, 3):
        t.rect(0, y, 15, y, '#3c3c3a'); t.rect(0, y + 1, 15, y + 1, '#1f1f1e')
    for y in range(16):
        for x in (6, 7, 8, 9):
            t.set(x, y, mix(t.get(x, y), rgb('#5a5a56'), .35))
    out['belt'] = t.im
    t = Tile('#6d7470', 281)
    t.rect(0, 0, 15, 1, '#8b928d'); t.rect(0, 14, 15, 15, '#4e5451')
    for x0 in (1, 9):
        t.rect(x0, 4, x0 + 5, 10, '#3a3f3c'); t.rect(x0 + 1, 5, x0 + 4, 9, '#565c58'); t.set(x0 + 2, 7, '#a1a8a3'); t.set(x0 + 3, 7, '#a1a8a3')
    t.set(0, 12, '#2f3331'); t.set(15, 12, '#2f3331'); out['belt_frame'] = t.im
    # Truck paint: dark green with a door seam and a chrome strip.
    t = Tile('#4f6b46', 290); t.jitter(.03)
    t.rect(0, 5, 15, 5, '#a9b0a8'); t.rect(0, 6, 15, 6, '#3a5034'); t.rect(7, 0, 7, 15, '#2f402b'); t.rect(0, 0, 15, 0, '#6a8660')
    out['truck'] = t.im
    # The machine's frame: painted industrial green, plates and rivets, a warning band at its foot, grime.
    t = Tile('#56635a', 300); t.jitter(.04)
    t.outline(0, 0, 15, 15, '#3c463f'); t.rect(1, 1, 14, 1, '#6c7a70')
    for x, y in ((2, 2), (13, 2), (2, 9), (13, 9)):
        t.set(x, y, '#879489'); t.set(x + 1, y + 1, '#2f3832')
    for x in range(16):
        for y in (12, 13, 14):
            t.set(x, y, '#d1b23a' if (x + y) % 6 < 3 else '#2a2a24')
    t.speck(8, ['#3f3a2c', '#4a4535'], 0, 3, 15, 11); out['machine'] = t.im
    return out


def cast_painting():
    """Eight logical pixels across each head, like a vanilla player head."""
    tiles=[]
    faces=(('#ad8461','#4b392b','#63746e'),('#c99c76','#623f2c','#547561'),
           ('#b58e6c','#3a3b2d','#526d79'),('#af8a68','#686653','#646c56'))
    for person,(skin,hair,eyes) in enumerate(faces):
        im=Image.new('RGBA',(8,8),skin);d=ImageDraw.Draw(im)
        d.rectangle((0,0,7,1),fill=hair);d.point((0,2),fill=hair);d.point((7,2),fill=hair)
        if person==1:d.rectangle((0,2,0,6),fill=hair);d.rectangle((7,2,7,6),fill=hair)
        if person==2:d.rectangle((0,0,7,1),fill='#4b5648');d.line((0,2,7,2),fill='#85794b')
        d.point((1,4),fill='#d8c4a1');d.point((6,4),fill='#d8c4a1');d.point((2,4),fill=eyes);d.point((5,4),fill=eyes)
        d.line((3,5,4,5),fill='#9c7052');d.line((3,6,4,6),fill='#885c46')
        if person==2:d.line((2,6,5,6),fill=hair)
        tiles.append(im)
    for i,base in enumerate(('#6d6651','#6a7376','#506574','#716449')):
        im=Image.new('RGBA',(8,8),base);d=ImageDraw.Draw(im)
        d.line((0,0,7,0),fill='#424c46');d.line((3,1,3,7),fill='#52594b')
        d.point((4,2),fill='#a9a38a');d.point((4,5),fill='#a9a38a')
        if i==2:d.rectangle((5,2,6,3),fill='#b49b55');d.line((0,7,7,7),fill='#393f34')
        if i==1:d.line((0,5,7,5),fill='#8b5149')
        tiles.append(im)
    for base,line in (('#4b5552','#3a4543'),('#5b5849','#474a3e'),('#bd9270','#c79e79'),('#3d3b32','#4f4b3e')):
        im=Image.new('RGBA',(8,8),base);ImageDraw.Draw(im).line((0,7,7,7),fill=line);tiles.append(im)
    # Retained prop UVs use the same four-by-four islands; author their materials at native scale too.
    for base,line in (('#66533b','#493e2f'),('#8f9a90','#64746a'),('#85583d','#a36f45'),('#d0c8a9','#96977f')):
        im=Image.new('RGBA',(8,8),base);d=ImageDraw.Draw(im)
        for y in (1,5):d.line((1,y,6,y),fill=line)
        tiles.append(im)
    atlas=Image.new('RGBA',(32,32))
    for i,im in enumerate(tiles):atlas.paste(im,(i%4*8,i//4*8))
    return atlas


def uv(tile):
    x, y = tile % 4 * 4, tile // 4 * 4
    return [x + .03, y + .03, x + 3.97, y + 3.97]


def cube(box, tile, rotation=None):
    result = {'from': list(box[:3]), 'to': list(box[3:]),
              'faces': {f: {'texture': '#atlas', 'uv': uv(tile)} for f in FACES}}
    if rotation:
        result['rotation'] = rotation
    return result


def model(elements):
    return {'textures': {'atlas': ATLAS, 'particle': ATLAS}, 'elements': elements}


def letters(word, x, y, z, size, tile, mirror=True):
    result = []
    start=x; width=(len(word)*4-1)*size
    for char in word:
        rows = GLYPHS[char]
        for row, line in enumerate(rows):
            for col, pixel in enumerate(line):
                if pixel == '1':
                    a = start+width-(x-start+col*size)-size if mirror else x+col*size
                    b = y + (len(rows) - row - 1) * size
                    result.append(cube((a,b,z,a+size*.88,b+size*.88,z+.08),tile))
        x += 4 * size
    return result

def tex(name):
    return 'the_oldest_house:block/borrasca/' + name


def faces(texture, only=None, uvs=None):
    return {f: ({'texture': texture, 'uv': uvs[f]} if uvs and f in uvs else {'texture': texture}) for f in (only or FACES)}


def town_models():
    """The town's fittings on their own block textures; every model is authored facing north."""
    out = {}
    for name in ('timber', 'steel', 'rust', 'truck'):
        out[name] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': tex(name)}}
    out['drawers'] = {'parent': 'minecraft:block/cube', 'textures': {'north': tex('drawers'), 'south': tex('drawers_side'), 'east': tex('drawers_side'),
                      'west': tex('drawers_side'), 'up': tex('drawers_side'), 'down': tex('drawers_side'), 'particle': tex('drawers_side')}}
    out['paper'] = {'textures': {'paper': tex('paper'), 'particle': tex('paper')},
                    'elements': [{'from': [1, .1, 1], 'to': [15, .45, 15], 'faces': faces('#paper', uvs={'up': [1, 1, 15, 15], 'down': [1, 1, 15, 15]})}]}
    out['notice'] = {'textures': {'frame': tex('notice_frame'), 'board': tex('notice'), 'particle': tex('notice')},
                     'elements': [{'from': [0, 0, 14], 'to': [16, 16, 16], 'faces': faces('#frame')},
                                  {'from': [1, 1, 13.9], 'to': [15, 15, 14], 'faces': faces('#board', ('north',), {'north': [0, 0, 16, 16]})}]}
    out['carving'] = {'textures': {'wood': tex('carving'), 'particle': tex('carving')},
                      'elements': [{'from': [0, 0, 14], 'to': [16, 16, 16], 'faces': faces('#wood', uvs={'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16]})}]}
    legs = [[1, 1], [13, 1], [1, 13], [13, 13]]
    out['belt'] = {'textures': {'belt': tex('belt'), 'frame': tex('belt_frame'), 'particle': tex('belt_frame')},
                   'elements': [{'from': [0, 5, 0], 'to': [16, 8, 16], 'faces': {**faces('#frame', ('north', 'south', 'east', 'west', 'down')), 'up': {'texture': '#belt'}}},
                                {'from': [1, 8, 0], 'to': [15, 9, 16], 'faces': faces('#belt', ('up', 'north', 'south'))},
                                {'from': [0, 8, 0], 'to': [1, 10, 16], 'faces': faces('#frame')},
                                {'from': [15, 8, 0], 'to': [16, 10, 16], 'faces': faces('#frame')},
                                *[{'from': [x, 0, z], 'to': [x + 2, 5, z + 2], 'faces': faces('#frame')} for x, z in legs]]}
    out['machine'] = {'textures': {'frame': tex('machine'), 'particle': tex('machine')},
                      'elements': [{'from': [0, 0, 0], 'to': [16, 4, 16], 'faces': faces('#frame')},
                                   {'from': [0, 4, 0], 'to': [3, 16, 16], 'faces': faces('#frame')},
                                   {'from': [13, 4, 0], 'to': [16, 16, 16], 'faces': faces('#frame')},
                                   {'from': [3, 7, 6], 'to': [5, 11, 10], 'faces': faces('#frame')},
                                   {'from': [11, 7, 6], 'to': [13, 11, 10], 'faces': faces('#frame')}]}
    return out


def outputs():
    out = {}
    kinds = {
        'trail': (3, [(0,0,0,16,16,16)]),
        'aged_wood': (2, [(0,0,0,16,16,16)]),
        'tape': (4, [(0,9,7,16,11,9),(1,6,7,2,10,9),(14,8,7,15,13,9)]),
        'polished': (6, [(0,0,0,16,16,16)]),
        'pack': (8, [(3,0,3,13,10,13),(5,10,5,11,12,7),(4,3,2,12,7,3)]),
        'patch': (9, [(1,0,1,15,1,15)]),
        'lock': (11, [(5,2,6,11,9,10),(5,9,7,6,13,9),(10,9,7,11,13,9),(6,12,7,10,13,9)]),
        'lamp': (14, [(0,2,3,16,14,13)]),
        'lamp_off': (5, [(0,2,3,16,14,13)]),
        'breaker': (15, [(0,0,0,16,16,16)]),
        'county': (13, [(0,0,0,16,16,16)]),
        'tracks': (3, [(1,.01,1,15,.15,15)]),
    }
    for name, (tile, boxes) in kinds.items():
        out[f'models/block/borrasca_trail_{name}.json'] = model([cube(b,tile) for b in boxes])
    # A see-through mesh made from actual narrow wires, rather than an opaque pane texture.
    wire = [cube((0,0,7.5,.6,16,8.5),5),cube((15.4,0,7.5,16,16,8.5),5)]
    for y in (1,6,11):
        for angle in (-45,45):
            wire.append(cube((1,y,7.75,15,y+.35,8.25),5,{'origin':[8,y+.175,8],'axis':'z','angle':angle,'rescale':False}))
    out['models/block/borrasca_trail_fence.json'] = model(wire)
    for name, number in zip(('one','two','three','four'),'1234'):
        # Base model faces north. The state rotations keep visible digits and native selection aligned.
        digit = letters(number,5.5,2.7,13.8,1.45,12)
        out[f'models/block/borrasca_trail_marker_{name}.json'] = model([cube((0,0,14,16,16,16),2),*digit])
    old = ('timber','steel','rust','paper','notice','carving','drawers','belt','truck')
    for name, value in town_models().items():
        out[f'models/block/borrasca_{name}.json'] = value
    new = tuple(kinds) + ('fence','marker_one','marker_two','marker_three','marker_four')
    states = {}
    for name in (*old,*new):
        for facing, angle in (('north',0),('east',90),('south',180),('west',270)):
            entry = {'model': 'the_oldest_house:block/borrasca_' + (name if name in old else 'trail_'+name)}
            if angle:
                entry['y'] = angle
            states[f'kind={name},facing={facing}'] = entry
    out['blockstates/borrasca_fitting.json'] = {'variants': states}
    for stage in range(5):
        bark = cube((0,0,0,16,16,16),0)
        if stage >= 3:
            bark['faces']['south']['uv'] = uv(1)
        out[f'models/block/borrasca_bark_{stage}.json'] = model([bark])
    out['blockstates/borrasca_bark.json'] = {'variants': {f'stage={i}': {'model':f'the_oldest_house:block/borrasca_bark_{i}'} for i in range(5)}}
    display = {'gui': {'rotation':[0,180,0],'translation':[0,0,0],'scale':[1,1,1]},
               'ground': {'rotation':[90,0,0],'translation':[0,1,0],'scale':[.5,.5,.5]},
               'thirdperson_righthand': {'rotation':[0,0,0],'translation':[0,2,0],'scale':[.5,.5,.5]},
               'firstperson_righthand': {'rotation':[0,-30,0],'translation':[0,2,0],'scale':[.8,.8,.8]}}
    compass = model([cube((2,2,7,14,14,9),7),cube((6,14,7,10,16,9),8)])
    patch = model([cube((0,3,7.5,16,13,8.5),9),*letters('KIMBER',1.1,6,7.35,.6,12,mirror=False)])
    flashlight = model([cube((6,0,6,10,12,10),5),cube((5.5,2,5.5,10.5,10,10.5),10),cube((4,10,4,12,16,12),14)])
    for name, asset in (('compass',compass),('name_patch',patch),('flashlight',flashlight)):
        asset['display'] = display
        out[f'models/item/borrasca_{name}.json'] = asset
    atlas_file = ROOT / 'src/main/resources/assets/minecraft/atlases/blocks.json'
    atlas = json.loads(atlas_file.read_text())
    source = {'type':'minecraft:single','resource':ATLAS,'sprite':ATLAS}
    if source not in atlas['sources']:
        atlas['sources'].append(source)
    source = {'type':'minecraft:single','resource':CAST_ATLAS,'sprite':CAST_ATLAS}
    if source not in atlas['sources']:
        atlas['sources'].append(source)
    out['../minecraft/atlases/blocks.json'] = atlas
    lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
    lang.update({'block.the_oldest_house.borrasca_bark':'Carved pine bark',
                 'item.the_oldest_house.borrasca_compass':"Kimber's compass",
                 'item.the_oldest_house.borrasca_name_patch':"Kimber's name patch",
                 'item.the_oldest_house.borrasca_flashlight':'County flashlight',
                 'subtitles.the_oldest_house.borrasca.triple_chime':'Metallic cry from the ridge',
                 'subtitles.the_oldest_house.borrasca.carve':'Knife scrapes wood',
                 'subtitles.the_oldest_house.borrasca.breaker':'Breaker drops',
                 'subtitles.the_oldest_house.borrasca.chain':'Chain rattles'})
    out['lang/en_us.json'] = lang
    sounds = json.loads((ASSETS / 'sounds.json').read_text())
    for name in ('triple_chime','carve','breaker','chain'):
        sounds['borrasca.'+name] = {'subtitle':'subtitles.the_oldest_house.borrasca.'+name,
            'sounds':[{'name':'the_oldest_house:borrasca/'+name, 'attenuation_distance':256 if name=='triple_chime' else 24}]}
    out['sounds.json'] = sounds
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    changed = []
    images = [(f'textures/entity/{name}.png', image) for name, image in (('borrasca_trail_atlas',paintings()),('borrasca_cast_pixels',cast_painting()))]
    images += [(f'textures/block/borrasca/{name}.png', image) for name, image in materials().items()]
    for name,image in images:
        path = ASSETS / name
        path.parent.mkdir(parents=True, exist_ok=True)
        if args.check:
            with Image.open(path) as actual:
                assert actual.size == image.size and actual.convert('RGBA').tobytes() == image.tobytes(), 'Native pixels differ: '+name
        else:
            image.save(path,optimize=True)
    for name, value in outputs().items():
        path = ASSETS / name
        content = json.dumps(value,indent=2,ensure_ascii=False)+'\n'
        if not path.exists() or path.read_text() != content:
            changed.append(str(path.relative_to(ROOT)))
            if not args.check:
                path.parent.mkdir(parents=True,exist_ok=True)
                path.write_text(content)
    if args.check and changed:
        raise SystemExit('Borrasca generated assets differ: '+', '.join(changed))
    print('Borrasca native meshes, block states, item models, atlas and subtitles:',len(outputs()),'definitions;',len(changed),'changed')


if __name__ == '__main__':
    main()
