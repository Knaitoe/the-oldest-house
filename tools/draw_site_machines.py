#!/usr/bin/env python3
"""The road crew's machines at the elk carcass site: a skid steer, bucket down, and the flatbed they hauled the elk on.

Each machine is authored whole, in pixels, facing north (its front toward -z), and then cut into one native block model
per occupied cell of the site_machine block, so textures run on unbroken from cell to cell. Paint, steel, rubber, mud and
planks are vanilla-style 16x16 tiles drawn here; the fitted faces (wheels, the cab's glass, the doors, the grille and the
lights) are drawn at one texel per pixel on one decal sheet per machine. Every cell's collision box is the box round its
own pieces; SiteMachineBlock carries the same boxes and --check confirms them.

    python3 tools/draw_site_machines.py           # draw and write
    python3 tools/draw_site_machines.py --check   # confirm the shipped files and the Java kinds match
    python3 tools/draw_site_machines.py --java    # print the Kind constants for SiteMachineBlock
"""
import argparse
import json
import math
import random
import re
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/SiteMachineBlock.java'
NS = 'the_oldest_house:block/site_machine/'


# ------------------------------------------------------------------------------------------------ drawing
def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c[:3])


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


def mix(a, b, f):
    return clamp(tuple(a[i] * (1 - f) + b[i] * f for i in range(3)))


def put(img, x, y, c):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), clamp(c) + (255,))


def mottled(base, seed, spread=.05, size=16):
    """A vanilla-style flat tone: one base colour, single-pixel variation and the odd darker pixel."""
    rnd = random.Random(seed)
    img = Image.new('RGBA', (size, size))
    for y in range(size):
        for x in range(size):
            f = 1 + rnd.choice((-spread, -spread / 2, 0, 0, 0, spread / 2, spread * .8))
            put(img, x, y, mul(base, f))
    return img


def paint_tile(base, seed, primer=(150, 148, 138), mud=0.0, mud_colour=(96, 74, 50), scratches=3):
    """Worn machine paint: the odd scratch to primer, a darker ding, and mud thrown up from below when asked."""
    rnd = random.Random(seed)
    img = mottled(base, seed, .045)
    for _ in range(scratches):  # scratches run with the work, mostly level
        x, y, n = rnd.randrange(16), rnd.randrange(16), rnd.randint(2, 5)
        for i in range(n):
            put(img, (x + i) % 16, y + (1 if i == n // 2 and rnd.random() < .5 else 0), primer)
    for _ in range(2):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        put(img, x, y, mul(base, .72))
        put(img, x + 1, y, mul(base, .84))
    if mud:
        for x in range(16):
            top = 16 - int(16 * mud * (.55 + .45 * rnd.random()))
            for y in range(max(0, top), 16):
                c = mud_colour if rnd.random() < .82 else mul(mud_colour, .78)
                put(img, x, y, mix(c, (70, 54, 38), rnd.random() * .3))
            for _ in range(rnd.randint(0, 2)):  # spatter above the line
                put(img, x, max(0, top - rnd.randint(1, 4)), mul(mud_colour, 1.05))
    return img


def tread_tile(seed):
    """Tyre tread seen from the front: rubber with chevron lugs and caked dirt in the grooves."""
    rnd = random.Random(seed)
    img = mottled((40, 39, 38), seed, .06)
    for y in range(16):
        for x in range(16):
            d = (y + abs(x - 7.5) * .6) % 5
            if d < 1.6:
                put(img, x, y, (24, 24, 25))
                if rnd.random() < .18:
                    put(img, x, y, (92, 74, 54))
            elif d < 2.2:
                put(img, x, y, (58, 57, 55))
    return img


def frame_tile(seed, base=(46, 44, 42)):
    """Black steel with grime and two bolt heads."""
    img = mottled(base, seed, .08)
    rnd = random.Random(seed)
    for x, y in ((3, 4), (12, 11)):
        put(img, x, y, (84, 82, 78))
        put(img, x + 1, y + 1, (28, 27, 26))
    for _ in range(5):
        put(img, rnd.randrange(16), rnd.randrange(16), (74, 62, 48))
    return img


def worn_steel(seed, base=(126, 124, 118), dirt=(98, 80, 58), streak=(170, 168, 160)):
    """Bucket steel worn bright by what it has scraped, dirt in the low corners."""
    rnd = random.Random(seed)
    img = mottled(base, seed, .05)
    for _ in range(6):
        x, y, n = rnd.randrange(16), rnd.randrange(16), rnd.randint(3, 8)
        for i in range(n):
            put(img, x, (y + i) % 16, streak if i % 3 else mul(streak, .9))
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(9, 16)
        put(img, x, y, mix(dirt, (70, 54, 40), rnd.random() * .4))
    return img


def mud_tile(seed):
    rnd = random.Random(seed)
    img = mottled((86, 64, 42), seed, .07)
    for _ in range(9):  # clods and wet shine
        x, y = rnd.randrange(16), rnd.randrange(16)
        put(img, x, y, (62, 46, 30))
        put(img, x + 1, y, (70, 52, 34))
    for _ in range(5):
        put(img, rnd.randrange(16), rnd.randrange(16), (118, 96, 70))
    for _ in range(2):  # a dead stalk or two in it
        x, y = rnd.randrange(14), rnd.randrange(16)
        put(img, x, y, (128, 112, 70))
        put(img, x + 1, y, (128, 112, 70))
    return img


def roof_tile(seed):
    img = mottled((34, 34, 36), seed, .08)
    for x in range(0, 16, 4):  # moulded ribs
        for y in range(16):
            put(img, x, y, (26, 26, 28))
    return img


def plate_tile(seed):
    """Diamond tread plate for steps and the toolbox lid."""
    img = mottled((120, 122, 122), seed, .04)
    for y in range(0, 16, 4):
        for x in range(0, 16, 4):
            ox = 2 if (y // 4) % 2 else 0
            put(img, x + ox, y + 1, (158, 160, 160))
            put(img, x + ox + 1, y + 2, (82, 84, 84))
    return img


def chrome_tile(seed):
    img = mottled((168, 170, 168), seed, .04)
    for x in range(16):
        put(img, x, 3, (222, 224, 220))
        put(img, x, 4, (204, 206, 202))
        put(img, x, 11, (110, 112, 112))
    return img


def galv_tile(seed):
    """Galvanised steel for the stakes, rails and the rack's frame: grey spangle."""
    rnd = random.Random(seed)
    img = mottled((146, 150, 150), seed, .05)
    for _ in range(10):
        x, y = rnd.randrange(15), rnd.randrange(15)
        put(img, x, y, (168, 172, 172))
        put(img, x + 1, y + 1, (126, 130, 130))
    return img


def deck_tile(seed, stained=None):
    """Weathered deck planks running fore and aft, gaps between them, nail heads at the cross members."""
    rnd = random.Random(seed)
    img = Image.new('RGBA', (16, 16))
    tones = [(126, 106, 80), (116, 98, 74), (132, 112, 86), (110, 92, 68)]
    for x in range(16):
        plank = x // 4
        for y in range(16):
            c = tones[(plank + seed) % 4]
            if x % 4 == 3:
                c = (64, 54, 42)  # the gap
            elif rnd.random() < .12:
                c = mul(c, .86)
            elif rnd.random() < .06:
                c = mul(c, 1.1)
            put(img, x, y, c)
    for x in range(0, 16, 4):  # grain streaks
        y, n = rnd.randrange(16), rnd.randint(3, 7)
        for i in range(n):
            put(img, x + 1, (y + i) % 16, (98, 82, 62))
    for x in range(0, 16, 4):
        put(img, x + 1, 7, (70, 66, 60))
        put(img, x + 1, 8, (150, 146, 136))
    if stained == 'pool':
        # Where a carcass lay: dried blood soaked dark into the grain, ragged at its edge, pooled in the gaps.
        for y in range(16):
            for x in range(16):
                d = math.hypot((x - 6.5) / 6, (y - 9) / 5) + .3 * math.sin(x * 1.7 + y * .9)
                if d < 1 and x % 4 != 3:
                    put(img, x, y, mix((86, 26, 22), (58, 20, 18), rnd.random() * .5))
                elif d < 1.3 and x % 4 != 3 and rnd.random() < .45:
                    put(img, x, y, mix(img.getpixel((x, y))[:3], (90, 34, 28), .55))
                elif x % 4 == 3 and d < 1.6:
                    put(img, x, y, (44, 18, 16))
    elif stained == 'smear':
        # Where one was dragged off the back: long smears with the planks, thinning out.
        for x0, width, start in ((4, 3, 0), (9, 2, 3)):
            for y in range(start, 16):
                for x in range(x0, x0 + width):
                    if x % 4 != 3 and rnd.random() < .85 - y * .025:
                        put(img, x, y, mix((92, 30, 24), (64, 22, 18), rnd.random() * .5))
        for _ in range(4):
            put(img, rnd.randrange(16), rnd.randrange(16), (74, 24, 20))
    return img


def rail_tile(seed, paint):
    """The deck's side rail, a four-pixel band (the tile's top rows): a painted channel, stake pockets and a reflector."""
    img = paint_tile(paint, seed)
    for x in range(16):
        put(img, x, 0, mul(paint, 1.14))
        put(img, x, 3, mul(paint, .6))
    for x0 in (2, 10):
        for y in (1, 2):
            put(img, x0, y, (30, 28, 26))
            put(img, x0 + 1, y, (30, 28, 26))
    put(img, 6, 1, (236, 150, 60))
    put(img, 6, 2, (176, 90, 30))
    put(img, 14, 2, (120, 96, 72))  # a road-salt bloom
    return img


def parts_tile():
    """Small parts by region: chrome, red lens, white lens, amber lens."""
    img = Image.new('RGBA', (16, 16))
    regions = [((0, 0), (178, 180, 178), (226, 228, 224)), ((8, 0), (172, 30, 26), (236, 96, 82)),
               ((0, 8), (226, 222, 196), (255, 252, 232)), ((8, 8), (214, 120, 26), (252, 186, 82))]
    for (ox, oy), base, hi in regions:
        for y in range(8):
            for x in range(8):
                c = base
                if x == 0 or y == 0:
                    c = mul(base, .7)
                elif x == 1 and y < 4 or y == 1 and x < 4:
                    c = hi
                put(img, ox + x, oy + y, c)
    return img


# ------------------------------------------------------------------------------------------------ the decal sheets
def wheel_face(img, ox, oy, d, rim, hub, lugs=6):
    """A tyre seen side on: worn rubber with tread blocks at the shoulder, a pressed steel rim and the lug nuts."""
    r = d / 2
    rnd = random.Random(ox * 31 + oy)
    for y in range(d):
        for x in range(d):
            dx, dy = x + .5 - r, y + .5 - r
            dist = math.hypot(dx, dy)
            ang = math.atan2(dy, dx)
            if dist > r - 1.2:
                c = (30, 30, 31) if int((ang + math.pi) / (2 * math.pi) * 20) % 2 else (46, 45, 44)
            elif dist > r * .62:
                c = (42, 41, 40) if rnd.random() < .85 else (54, 53, 51)
                if rnd.random() < .05:
                    c = (92, 74, 54)  # dried mud
            elif dist > r * .56:
                c = mul(rim, .62)
            elif dist > r * .26:
                c = rim
                if dx < -1 and dy < -1:
                    c = mul(rim, 1.2)
                if dx > 1 and dy > 1:
                    c = mul(rim, .8)
            else:
                c = hub
            put(img, ox + x, oy + y, c)
    for i in range(lugs):
        a = i / lugs * 2 * math.pi
        x, y = round(r - .5 + math.cos(a) * r * .4), round(r - .5 + math.sin(a) * r * .4)
        put(img, ox + x, oy + y, (196, 196, 190))
    put(img, ox + int(r) - 1, oy + int(r) - 1, mul(hub, .6))


def glass(img, box, frame, tint=(40, 54, 60), seed=0, bars=(), grid=0):
    """Tinted safety glass in a frame, with one diagonal reflection and, for the cage, bars or a grid."""
    x0, y0, x1, y1 = box
    rnd = random.Random(seed)
    for y in range(y0, y1):
        for x in range(x0, x1):
            c = mul(tint, 1 + rnd.choice((-.05, 0, 0, .04)))
            if (x - x0) - (y - y0) * .7 in range(3, 6) or (x - x0) - (y - y0) * .7 in range(8, 9):
                c = mix(c, (150, 170, 176), .4)
            put(img, x, y, c)
    for x in range(x0, x1):
        put(img, x, y0, frame)
        put(img, x, y1 - 1, frame)
    for y in range(y0, y1):
        put(img, x0, y, frame)
        put(img, x1 - 1, y, frame)
    for b in bars:
        for y in range(y0, y1):
            put(img, x0 + b, y, frame)
    if grid:
        for y in range(y0, y1):
            for x in range(x0, x1):
                if (x - x0) % grid == 0 or (y - y0) % grid == 0:
                    put(img, x, y, frame)


def skid_sheet():
    """64x64. Wheel 14x14 at (0,0); cab door 18x15 at (16,0); cab rear 18x15 at (36,0); cab sides 15x15 at (0,16)/(16,16);
    engine door 20x8 at (0,32); chassis rear 22x13 at (24,32)."""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 255))
    yellow, black = (222, 166, 38), (32, 32, 33)
    wheel_face(img, 0, 0, 14, (70, 70, 72), (52, 52, 54), lugs=8)
    # The cab's front door: a black frame round tinted glass, a hinge, a handle and a warning label (no words).
    glass(img, (16, 0, 34, 15), black, seed=1)
    for x in range(30, 33):
        put(img, x, 9, (150, 150, 146))
    for y in range(3, 6):
        for x in range(18, 21):
            put(img, x, y, (232, 196, 40) if (x + y) % 2 else (26, 26, 26))
    for y in (2, 12):
        put(img, 17, y, (110, 110, 106))
    # The rear window: smaller glass in a heavy surround.
    glass(img, (36, 0, 54, 15), black, seed=2, grid=0)
    for y in range(0, 15):
        for x in (36, 37, 52, 53):
            put(img, x, y, black)
    for x in range(36, 54):
        for y in (0, 1, 12, 13, 14):
            put(img, x, y, black)
    # The cage sides: bars over dark glass, the seat's headrest a shadow behind them.
    for ox in (0, 16):
        glass(img, (ox, 16, ox + 15, 31), (78, 78, 76), tint=(30, 36, 40), seed=3 + ox, grid=3)
        for y in range(21, 26):
            for x in range(ox + 6, ox + 10):
                if (x - ox) % 3 and (y - 16) % 3:
                    put(img, x, y, (20, 20, 22))
    # The engine door: yellow, louvred vents and the two tail lamps.
    for y in range(32, 40):
        for x in range(0, 20):
            put(img, x, y, mul(yellow, 1 + random.Random(x * 7 + y).choice((-.04, 0, .03))))
    for y in range(34, 38):
        for x in range(4, 16):
            put(img, x, y, (30, 30, 30) if y % 2 == 0 else (64, 60, 52))
    for x0 in (1, 17):
        for y in range(34, 37):
            for x in range(x0, x0 + 2):
                put(img, x, y, (200, 34, 28) if y < 36 else (150, 26, 22))
    for x in range(0, 20):
        put(img, x, 32, mul(yellow, 1.12))
        put(img, x, 39, mul(yellow, .7))
    # The chassis rear: black counterweight with a step, a hitch and mud thrown up the back.
    rnd = random.Random(9)
    for y in range(32, 45):
        for x in range(24, 46):
            c = mul(black, 1 + rnd.choice((-.08, 0, .06)))
            if y > 40 and rnd.random() < .4:
                c = (88, 68, 46)
            put(img, x, y, c)
    for x in range(26, 44):
        put(img, x, 33, (70, 70, 68))
    for y in range(37, 41):
        for x in range(33, 37):
            put(img, x, y, (100, 100, 96) if y == 37 else (60, 60, 58))
    return img


def truck_sheet():
    """128x128. Wheel 16x16 at (0,0); grille 32x17 at (16,0); windshield 42x12 at (48,0); rear window 42x12 at (48,16);
    doors 20x16 at (0,32)/(24,32); side windows 18x12 at (48,32)/(72,32); tail 48x4 at (0,52); rack 44x24 at (0,64)."""
    img = Image.new('RGBA', (128, 128), (0, 0, 0, 255))
    orange, black = (220, 110, 34), (30, 30, 31)
    wheel_face(img, 0, 0, 16, (164, 166, 162), (110, 112, 110), lugs=8)
    # The grille: dark slats in a chrome surround, square headlamps either side with amber markers below.
    for y in range(17):
        for x in range(32):
            put(img, 16 + x, y, mul(orange, 1 + random.Random(x + y * 40).choice((-.04, 0, .03))))
    for y in range(4, 14):
        for x in range(9, 23):
            c = (170, 172, 170) if y in (4, 13) or x in (9, 22) else ((22, 22, 24) if y % 2 else (60, 60, 62))
            put(img, 16 + x, y, c)
    for lx in (1, 24):
        for y in range(5, 10):
            for x in range(lx, lx + 7):
                c = (226, 222, 196)
                if x in (lx, lx + 6) or y in (5, 9):
                    c = (150, 152, 150)
                elif x == lx + 1 and y == 6:
                    c = (255, 252, 236)
                put(img, 16 + x, y, c)
        for x in range(lx + 1, lx + 6):
            put(img, 16 + x, 11, (214, 120, 26))
            put(img, 16 + x, 12, (170, 86, 22))
    for x in range(32):
        put(img, 16 + x, 0, mul(orange, 1.15))
        put(img, 16 + x, 16, (40, 40, 40))
    # The windshield: two panes either side of a pillar, a wiper resting at the bottom, a sun strip at the top.
    glass(img, (48, 0, 90, 12), black, tint=(46, 64, 72), seed=11, bars=(21,))
    for x in range(49, 89):
        put(img, x, 1, (36, 70, 86))
    for x in range(54, 66):
        put(img, x, 10, (18, 18, 18))
    for x in range(72, 84):
        put(img, x, 10, (18, 18, 18))
    # The back glass: dark, a sliding centre pane.
    glass(img, (48, 16, 90, 28), black, tint=(34, 44, 50), seed=12, bars=(14, 28))
    # Doors: a panel line, the handle forward of the hinge-less edge and the crew's stencilled number (no words).
    for ox, flip in ((0, False), (24, True)):
        rnd = random.Random(20 + ox)
        for y in range(16):
            for x in range(20):
                c = mul(orange, 1 + rnd.choice((-.04, 0, 0, .03)))
                if y > 12 and rnd.random() < .5:
                    c = mix(c, (104, 82, 58), .6)
                put(img, ox + x, 32 + y, c)
        for y in range(16):
            put(img, ox + (19 if flip else 0), 32 + y, mul(orange, .62))
            put(img, ox + (0 if flip else 19), 32 + y, mul(orange, .78))
        for x in range(20):
            put(img, ox + x, 32, mul(orange, .66))
        hx = ox + (3 if flip else 13)
        for x in range(hx, hx + 4):
            put(img, x, 36, (180, 180, 176))
            put(img, x, 37, (90, 90, 88))
        # The department's round seal, white ring and green field, without its name.
        cx, cy = ox + (11 if flip else 8), 42.5
        for y in range(39, 47):
            for x in range(cx - 4, cx + 4):
                d = math.hypot(x + .5 - cx, y + .5 - cy)
                if d < 3.2:
                    put(img, x, y, (58, 92, 60) if d < 2.2 else (228, 226, 216))
    # Side windows, mirrored left and right: glass with the vent pane forward.
    for ox, flip in ((48, False), (72, True)):
        glass(img, (ox, 32, ox + 18, 44), black, tint=(42, 58, 66), seed=30 + ox, bars=((12,) if not flip else (5,)))
    # The tail: red lamps, an amber turn lamp and reflectors along a black-painted sill.
    for y in range(52, 56):
        for x in range(48):
            put(img, x, y, (44, 42, 40))
    for x0 in (1, 41):
        for y in range(52, 56):
            for x in range(x0, x0 + 6):
                put(img, x, y, (196, 32, 26) if x < x0 + 4 else (214, 120, 26))
    for x in range(14, 34, 4):
        put(img, x, 53, (220, 60, 40))
    # The headache rack: a galvanised frame round expanded mesh you can see the cab through.
    for y in range(64, 88):
        for x in range(44):
            edge = x < 2 or x > 41 or y < 66 or y > 85
            mesh = (x + y) % 4 == 0 or (x - y) % 4 == 0
            if edge:
                put(img, x, y, (150, 154, 154) if (x + y) % 3 else (128, 132, 132))
            elif mesh:
                put(img, x, y, (132, 136, 136))
            else:
                img.putpixel((x, y), (0, 0, 0, 0))
    return img


# ------------------------------------------------------------------------------------------------ geometry
FACES = ('north', 'south', 'west', 'east', 'up', 'down')


class Box:
    """An axis-aligned piece in machine pixels. faces maps a face to a tile name, ('sheet', tx, ty) or None to skip it."""

    def __init__(self, lo, hi, default, **faces):
        self.lo, self.hi = tuple(float(v) for v in lo), tuple(float(v) for v in hi)
        self.faces = {f: faces.get(f, default) for f in FACES}


def wheel(cx, cy, cz, r, x0, x1, outer, sheet_at, tread='tread', side='rubber'):
    """A tyre built of three stepped boxes, its outer face drawn from the sheet's wheel square."""
    out = []
    for hz, hy in ((r, r * .44), (r * .75, r * .75), (r * .44, r)):
        faces = {'north': tread, 'south': tread, 'up': tread, 'down': tread, 'west': side, 'east': side}
        faces[outer] = ('sheet', sheet_at[0], sheet_at[1], (cz - r, cy - r, cz + r, cy + r))
        out.append(Box((x0, cy - hy, cz - hz), (x1, cy + hy, cz + hz), tread, **faces))
    return out


def skid():
    s = []
    for cz in (24, 40):
        s += wheel(0, 7, cz, 7, 0, 5, 'west', (0, 0))
        s += wheel(0, 7, cz, 7, 27, 32, 'east', (0, 0))
    s.append(Box((5, 2, 17), (27, 8, 47), 'frame', south=('sheet', 24, 32, (5, 2, 27, 15))))
    s.append(Box((5, 8, 17), (27, 15, 47), 'paint_low', up='plate', south=('sheet', 24, 32, (5, 2, 27, 15))))
    s.append(Box((7, 15, 20), (25, 30, 35), 'paint', north=('sheet', 16, 0), south=('sheet', 36, 0),
                 west=('sheet', 0, 16), east=('sheet', 16, 16), up='roof'))
    s.append(Box((6.5, 30, 19.5), (25.5, 31.5, 35.5), 'roof'))
    s.append(Box((6, 15, 35), (26, 23, 47), 'paint', south=('sheet', 0, 32)))
    for x0 in (5, 24):
        s.append(Box((x0, 15, 37), (x0 + 3, 27, 45), 'paint'))
    for x0 in (1.5, 27):
        s.append(Box((x0, 22.5, 13), (x0 + 3.5, 26.5, 44), 'paint'))
        s.append(Box((x0, 4, 13), (x0 + 3.5, 22.5, 17), 'paint'))
        s.append(Box((x0 - .5, 23.5, 41), (x0 + 4, 25.5, 43), 'parts_chrome'))  # the arm's pivot pin
    s.append(Box((5, 17, 13.5), (27, 20, 16.5), 'paint'))
    for x0 in (9, 21):
        s.append(Box((x0, 11, 12.5), (x0 + 2, 18, 14), 'parts_chrome'))
    for x0 in (7.5, 22):
        s.append(Box((x0, 28, 19), (x0 + 2.5, 30, 20), 'parts_white'))
    s.append(Box((21, 23, 42), (22.5, 28, 43.5), 'frame'))  # exhaust stack
    # The bucket, down on the ground with a load of mud still in it.
    s.append(Box((0, 0, 1.5), (32, 1.5, 13), 'bucket_in', down='frame'))
    s.append(Box((0, 0, 0), (32, 1, 1.5), 'edge'))
    s.append(Box((0, 1.5, 12), (32, 11, 14), 'bucket_out', north='bucket_in'))
    s.append(Box((1.5, 1.5, 10.5), (30.5, 3.5, 12), 'bucket_in'))
    s.append(Box((0, 11, 8.5), (32, 12.5, 14), 'bucket_out', down='bucket_in'))
    for x0, inner in ((0, 'east'), (30.5, 'west')):
        s.append(Box((x0, 1.5, 7), (x0 + 1.5, 11, 12), 'bucket_out', **{inner: 'bucket_in'}))
        s.append(Box((x0, 1.5, 2), (x0 + 1.5, 6.5, 7), 'bucket_out', **{inner: 'bucket_in'}))
    s.append(Box((1.5, 1.5, 3), (30.5, 4, 10.5), 'mud'))
    s.append(Box((8, 4, 6), (16, 5.5, 10), 'mud'))
    s.append(Box((19, 4, 7), (24, 5, 10), 'mud'))
    s.append(Box((3, 4, 14), (29, 10, 15), 'frame'))  # quick-attach plate
    return s


def truck():
    s = []
    for cz, x_spans in ((18, ((1, 7), (41, 47))), (104, ((0, 8), (40, 48)))):
        s += wheel(0, 8, cz, 8, x_spans[0][0], x_spans[0][1], 'west', (0, 0))
        s += wheel(0, 8, cz, 8, x_spans[1][0], x_spans[1][1], 'east', (0, 0))
    s.append(Box((3, 6, 1), (45, 11, 3.5), 'galv'))
    s.append(Box((8, 13, 3), (40, 30, 26), 'paint', north=('sheet', 16, 0)))
    for x0 in (2, 40):
        s.append(Box((x0, 16, 6), (x0 + 6, 22, 28), 'paint', down='frame'))
    for x0 in (2.5, 42.5):
        s.append(Box((x0, 19, 5.5), (x0 + 3, 21, 6), 'parts_amber'))
    s.append(Box((12, 8, 3), (36, 13, 26), 'frame'))
    s.append(Box((2, 14, 26), (46, 30, 46), 'paint', west=('sheet', 0, 32), east=('sheet', 24, 32)))
    for x0 in (.5, 46):
        s.append(Box((x0, 9, 30), (x0 + 1.5, 11, 38), 'plate'))
    s.append(Box((3, 30, 27), (45, 42, 45), 'paint', north=('sheet', 48, 0), south=('sheet', 48, 16),
                 west=('sheet', 48, 32), east=('sheet', 72, 32)))
    s.append(Box((2.5, 42, 26.5), (45.5, 43.5, 45.5), 'paint'))
    s.append(Box((21, 43.5, 35), (27, 46.5, 39), 'parts_amber'))
    for x0, x1 in ((0, 3), (45, 48)):
        s.append(Box((x0, 33, 28), (x1, 34, 29), 'frame'))
        s.append(Box((x0 if x0 == 0 else 46.5, 30, 27.5), (1.5 if x0 == 0 else 48, 37, 30), 'frame'))
    s.append(Box((2, 16, 46), (46, 40, 47.5), 'galv', north=('sheet', 0, 64), south=('sheet', 0, 64)))
    s.append(Box((6, 7, 48), (11, 13, 62), 'chrome'))  # fuel tank
    s.append(Box((37, 6, 50), (42, 12, 64), 'plate'))  # toolbox
    s.append(Box((10, 8, 26), (38, 12, 126), 'frame'))
    # The deck cell by cell, so the stains lie where the carcasses do (the middle of rows four and six) and run off the back.
    stains = {(1, 4): 'deck_pool', (1, 5): 'deck_smear', (1, 6): 'deck_pool', (1, 7): 'deck_smear', (0, 6): 'deck_smear'}
    for row in range(3, 8):
        for col in range(3):
            faces = {'west': 'rail' if col == 0 else None, 'east': 'rail' if col == 2 else None, 'north': 'rail' if row == 3 else None,
                     'south': ('sheet', 0, 52, (0, 12, 48, 16)) if row == 7 else None, 'down': 'frame'}
            s.append(Box((col * 16, 12, row * 16), (col * 16 + 16, 16, row * 16 + 16), stains.get((col, row), 'deck'), **faces))
    for z in (50, 66, 82, 98, 114):
        for x0 in (0, 46):
            s.append(Box((x0, 16, z), (x0 + 2, 27, z + 2), 'galv'))
    for x0 in (0, 46.5):
        s.append(Box((x0, 25, 49), (x0 + 1.5, 27, 127), 'galv'))
    s.append(Box((6, 7, 125), (42, 10, 128), 'frame'))
    for x0 in (1, 40):
        s.append(Box((x0, 2, 113), (x0 + 7, 12, 114), 'frame'))  # mud flaps
    return s


MACHINES = {'skid': (skid, (2, 3, 2)), 'truck': (truck, (3, 8, 3))}


def face_uv(face, lo, hi, full_lo, full_hi, spec, sheet_size):
    """The face's uv in sixteenths. Tiles follow the native auto-uv of the cell. Sheet faces map the whole face at one
    texel per pixel from (tx, ty); an optional drawing rectangle (in the face's own plane, low corner then high) stands
    in for the face's extent, so pieces built in steps (the tyres) or stacked (the skid steer's back) share one drawing."""
    if spec[0] != 'sheet':
        (x1, y1, z1), (x2, y2, z2) = lo, hi
        return {'north': [16 - x2, 16 - y2, 16 - x1, 16 - y1], 'south': [x1, 16 - y2, x2, 16 - y1], 'west': [z1, 16 - y2, z2, 16 - y1],
                'east': [16 - z2, 16 - y2, 16 - z1, 16 - y1], 'up': [x1, z1, x2, z2], 'down': [x1, 16 - z2, x2, 16 - z1]}[face]
    tx, ty = spec[1], spec[2]
    full_lo, full_hi = list(full_lo), list(full_hi)
    if len(spec) > 3:
        a0, b0, a1, b1 = spec[3]
        across = 2 if face in ('west', 'east') else 0
        full_lo[across], full_lo[1], full_hi[across], full_hi[1] = a0, b0, a1, b1

    def axes(a, b):
        (ax, ay, az), (bx, by, bz) = a, b
        return {'north': (-bx, -by, -ax, -ay), 'south': (ax, -by, bx, -ay), 'west': (az, -by, bz, -ay), 'east': (-bz, -by, -az, -ay),
                'up': (ax, az, bx, bz), 'down': (ax, -bz, bx, -az)}[face]
    u0, v0, u1, v1 = axes(lo, hi)
    fu0, fv0, _, _ = axes(full_lo, full_hi)
    k = 16 / sheet_size
    return [round((tx + u0 - fu0) * k, 4), round((ty + v0 - fv0) * k, 4), round((tx + u1 - fu0) * k, 4), round((ty + v1 - fv0) * k, 4)]


def cells(name):
    build, (cols, rows, levels) = MACHINES[name]
    pieces = {}
    for box in build():
        for cx in range(cols):
            for cy in range(levels):
                for cz in range(rows):
                    o = (cx * 16, cy * 16, cz * 16)
                    lo = tuple(max(box.lo[i], o[i]) for i in range(3))
                    hi = tuple(min(box.hi[i], o[i] + 16) for i in range(3))
                    if any(hi[i] - lo[i] <= 1e-6 for i in range(3)):
                        continue
                    pieces.setdefault((cx, cz, cy), []).append((box, lo, hi, o))
    assert all(0 <= b.lo[i] and b.hi[i] <= (cols, levels, rows)[i] * 16 for b in build() for i in range(3)), name
    return pieces


def model_for(name, entries, sheet_size):
    textures, elements = {}, []
    cut = {'west': (0, 'lo'), 'east': (0, 'hi'), 'down': (1, 'lo'), 'up': (1, 'hi'), 'north': (2, 'lo'), 'south': (2, 'hi')}
    for box, lo, hi, o in entries:
        local_lo = [round(lo[i] - o[i], 4) for i in range(3)]
        local_hi = [round(hi[i] - o[i], 4) for i in range(3)]
        faces = {}
        for f in FACES:
            spec = box.faces[f]
            if spec is None:
                continue
            axis, side = cut[f]
            # A face on a cut is inside the machine: the next cell carries the rest of the piece.
            if (side == 'lo' and lo[axis] > box.lo[axis] + 1e-6) or (side == 'hi' and hi[axis] < box.hi[axis] - 1e-6):
                continue
            if isinstance(spec, tuple):
                key, ref = 'sheet', f'{NS}{name}_sheet'
            elif spec.startswith('parts_'):
                key, ref = 'parts', NS + 'parts'
            else:
                key, ref = spec, f'{NS}{name}_{spec}' if spec in OWN[name] else NS + spec
            textures[key] = ref
            if isinstance(spec, tuple):
                uv = face_uv(f, lo, hi, box.lo, box.hi, spec, sheet_size)
            elif spec.startswith('parts_'):
                ox, oy = {'parts_chrome': (0, 0), 'parts_red': (8, 0), 'parts_white': (0, 8), 'parts_amber': (8, 8)}[spec]
                uv = [ox + 2, oy + 2, ox + 6, oy + 6]
            else:
                uv = face_uv(f, local_lo, local_hi, None, None, (spec,), 16)
            faces[f] = {'texture': '#' + key, 'uv': uv}
        if faces:
            elements.append({'from': local_lo, 'to': local_hi, 'faces': faces})
    main = next((k for k in ('paint', 'deck', 'frame', 'bucket_out', 'galv') if k in textures), next(iter(textures)))
    textures['particle'] = textures[main]
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False,
            'textures': textures, 'elements': elements}


def collision(entries):
    lo = [min(e[1][i] - e[3][i] for e in entries) for i in range(3)]
    hi = [max(e[2][i] - e[3][i] for e in entries) for i in range(3)]
    return [math.floor(lo[0]), math.floor(lo[1]), math.floor(lo[2]), math.ceil(hi[0]), math.ceil(hi[1]), math.ceil(hi[2])]


OWN = {'skid': {'paint', 'paint_low', 'bucket_out'}, 'truck': {'paint', 'rail'}}


def tiles():
    yellow, orange = (222, 166, 38), (220, 110, 34)
    return {
        'skid_paint': paint_tile(yellow, 401), 'skid_paint_low': paint_tile(yellow, 402, mud=.75),
        'skid_bucket_out': paint_tile(mul(yellow, .94), 403, mud=.3),
        'truck_paint': paint_tile(orange, 404, primer=(150, 122, 98), scratches=1),
        'frame': frame_tile(406), 'tread': tread_tile(407), 'rubber': mottled((38, 38, 38), 408, .07),
        'bucket_in': worn_steel(409), 'edge': worn_steel(410, base=(160, 160, 154), dirt=(120, 110, 96), streak=(196, 196, 190)),
        'mud': mud_tile(411), 'roof': roof_tile(412), 'plate': plate_tile(413), 'chrome': chrome_tile(414), 'galv': galv_tile(415),
        'deck': deck_tile(416), 'deck_pool': deck_tile(417, 'pool'), 'deck_smear': deck_tile(419, 'smear'), 'truck_rail': rail_tile(418, orange),
        'parts': parts_tile(),
    }


def outputs():
    files = {}
    t = tiles()
    for k, img in t.items():
        files[f'textures/block/site_machine/{k}.png'] = img
    files['textures/block/site_machine/skid_sheet.png'] = skid_sheet()
    files['textures/block/site_machine/truck_sheet.png'] = truck_sheet()
    kinds, variants = [], {}
    for name in MACHINES:
        sheet = 64 if name == 'skid' else 128
        for (cx, cz, cy), entries in sorted(cells(name).items(), key=lambda kv: (kv[0][2], kv[0][1], kv[0][0])):
            kind = f'{name}_{cx}_{cz}_{cy}'
            m = model_for(name, entries, sheet)
            files[f'models/block/site_machine/{kind}.json'] = m
            kinds.append((kind, collision(entries)))
            for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
                v = {'model': f'the_oldest_house:block/site_machine/{kind}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind}'] = v
    files['blockstates/site_machine.json'] = {'variants': variants}
    return files, kinds


def java_kinds(kinds):
    return ',\n        '.join(f'{k.upper()}({",".join(str(v) for v in box)})' for k, box in kinds)


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--check', action='store_true')
    p.add_argument('--java', action='store_true')
    args = p.parse_args()
    files, kinds = outputs()
    if args.java:
        print(java_kinds(kinds) + ';')
        return
    for rel, obj in files.items():
        path = A / rel
        if args.check:
            if isinstance(obj, Image.Image):
                with Image.open(path) as actual:
                    assert actual.convert('RGBA').tobytes() == obj.convert('RGBA').tobytes(), rel
            else:
                assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            if isinstance(obj, Image.Image):
                obj.save(path, optimize=True)
            else:
                path.write_text(json.dumps(obj, indent=1) + '\n')
    shipped = re.findall(r'\b((?:SKID|TRUCK)_\d_\d_\d)\(([^)]*)\)', JAVA.read_text()) if JAVA.exists() else []
    expected = [(k.upper(), ','.join(str(v) for v in box)) for k, box in kinds]
    if args.check or shipped:
        assert shipped == expected, 'SiteMachineBlock.Kind does not match the drawn cells; run with --java'
    print(('Verified' if args.check else 'Drew') + f' the site machines: {len(kinds)} cells, {len(files)} files.')


if __name__ == '__main__':
    main()
