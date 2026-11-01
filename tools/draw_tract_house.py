#!/usr/bin/env python3
"""The Poltergeist model home as a 1980s tract house (0.4.121): vanilla-style 16x16 textures, the tract_house block's
models and states, and the house's five material blocks.

Each texture is drawn pixel by pixel from a small palette with one-pixel bevels and scattered noise, as vanilla's are;
nothing is sampled. The television's static is an animated strip. Run with --check to confirm the committed files.
"""
import argparse
import json
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
TEX = ROOT / 'textures/block/tract_house'
MODELS = ROOT / 'models/block/tract_house'
STATES = ROOT / 'blockstates'
NS = 'the_oldest_house:block/tract_house/'


def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


def canvas(w=16, h=16, color=(0, 0, 0, 0)):
    return Image.new('RGBA', (w, h), color)


def px(img, x, y, c, a=255):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), clamp(c) + (a,))


def fill(img, x0, y0, x1, y1, c, noise=0.0, rng=None):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            f = 1.0
            if noise and rng:
                f += rng.uniform(-noise, noise)
            px(img, x, y, mul(c, f))


# ------------------------------------------------------------------------------------------------ materials
def paneling():
    """Dark walnut sheet paneling: random-width vertical boards between dark V-grooves, with streaky grain."""
    rng = random.Random(1211)
    img = canvas()
    base = (104, 68, 40)
    grooves = (0, 5, 9, 13)
    for x in range(16):
        tone = 1.0 + (0.05 if x in (1, 6, 10, 14) else 0) - (0.05 if x in (4, 8, 12, 15) else 0)
        for y in range(16):
            f = tone + rng.choice((0, 0, 0, -.04, .04)) + (0.06 if (y * 3 + x * 7) % 11 == 0 else 0)
            px(img, x, y, mul(base, f))
    for x in grooves:
        for y in range(16):
            px(img, x, y, mul(base, .5))
    for _ in range(5):
        x, y = rng.randrange(16), rng.randrange(14)
        if x not in grooves:
            px(img, x, y, mul(base, .75)); px(img, x, y + 1, mul(base, .8))
    return img


def shag():
    """Harvest shag: rust, amber and brown tufts with deep shadows between them."""
    rng = random.Random(1212)
    img = canvas()
    tufts = [(150, 78, 30), (176, 104, 40), (120, 62, 28), (196, 132, 56)]
    for y in range(16):
        for x in range(16):
            c = rng.choice(tufts)
            f = 1.0 + rng.uniform(-.06, .06)
            if rng.random() < .18:
                f = .62
            px(img, x, y, mul(c, f))
    return img


def linoleum():
    """Kitchen sheet linoleum: a lattice of cream and pale gold diamonds, each outlined in a fine brown line."""
    rng = random.Random(1213)
    img = canvas()
    cream, gold, brown = (228, 214, 176), (212, 178, 104), (146, 108, 62)
    for y in range(16):
        for x in range(16):
            u, v = (x + y) % 16, (x - y) % 16
            c = cream if (u < 8) == (v < 8) else gold
            if u in (0, 8) or v in (0, 8):
                c = brown
            px(img, x, y, mul(c, 1 + rng.choice((0, 0, 0, -.03, .02))))
    return img


def popcorn():
    """Popcorn ceiling: off-white with lumpy bright and grey specks."""
    rng = random.Random(1214)
    img = canvas()
    base = (226, 222, 210)
    for y in range(16):
        for x in range(16):
            r = rng.random()
            f = 1.0 + (.06 if r < .2 else -.07 if r < .32 else 0)
            px(img, x, y, mul(base, f))
    return img


def kitchen_paper():
    """Harvest kitchen paper: cream ground, small orange and brown flowers with olive leaves on a diagonal repeat."""
    rng = random.Random(1215)
    img = canvas()
    ground, petal, centre, leaf = (232, 220, 186), (214, 120, 46), (120, 70, 34), (128, 132, 62)
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(ground, 1 + rng.choice((0, 0, 0, -.025))))
    for cx, cy in ((4, 4), (12, 12)):
        for dx, dy in ((0, -1), (0, 1), (-1, 0), (1, 0)):
            px(img, cx + dx, cy + dy, petal)
        px(img, cx, cy, centre)
        px(img, cx + 2, cy + 1, leaf); px(img, cx + 3, cy + 2, leaf)
    for cx, cy in ((12, 4), (4, 12)):
        px(img, cx, cy, mul(petal, .85)); px(img, cx + 1, cy, mul(leaf, .9))
    return img


# ------------------------------------------------------------------------------------------------ furniture
def oak(seed=1220, base=(176, 128, 74)):
    """Honey oak: long horizontal grain with darker growth lines."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        line = .86 if y % 5 == 2 else 1.0
        for x in range(16):
            f = line + rng.choice((0, 0, 0, -.04, .04)) + (0.05 if (x + y * 7) % 13 == 0 else 0)
            px(img, x, y, mul(base, f))
    return img


def plaid():
    """Sofa plaid on an eight-pixel repeat: brown ground, rust bands, a fine cream stripe crossing."""
    img = canvas()
    brown, rust, cream = (112, 72, 42), (164, 88, 42), (214, 190, 146)
    for y in range(16):
        for x in range(16):
            u, v = x % 8, y % 8
            c = brown
            if u in (2, 3) or v in (2, 3):
                c = rust
            if u in (2, 3) and v in (2, 3):
                c = mul(rust, .78)
            if u == 6 or v == 6:
                c = mul(cream, .95 if (x + y) % 2 else 1.0)
            if c == brown and (x + y) % 2 == 0:
                c = mul(brown, .9)
            px(img, x, y, c)
    return img


def corduroy():
    """Recliner corduroy: rust with vertical wales, each lit on one side."""
    rng = random.Random(1222)
    img = canvas()
    base = (150, 74, 38)
    for x in range(16):
        f = 1.08 if x % 2 == 0 else .86
        for y in range(16):
            px(img, x, y, mul(base, f + rng.uniform(-.03, .03)))
    return img


def gold(seed=1223):
    """Harvest gold enamel, faintly lit toward the top."""
    rng = random.Random(seed)
    img = canvas()
    base = (206, 162, 58)
    for y in range(16):
        for x in range(16):
            f = 1.06 - y * .008 + rng.choice((0, 0, 0, -.02))
            px(img, x, y, mul(base, f))
    return img


def fridge_front(upper):
    img = gold(1224 if upper else 1225)
    edge = (150, 112, 36)
    for i in range(16):
        px(img, 0, i, edge); px(img, 15, i, edge)
    if upper:
        for x in range(16):
            px(img, x, 15, (40, 32, 20))  # the seam between the freezer and the fridge door
            px(img, x, 0, mul((206, 162, 58), 1.12))
    else:
        for x in range(16):
            px(img, x, 15, edge)
    # A chrome bar handle down the hinge-free side.
    for y in (range(9, 15) if upper else range(1, 9)):
        px(img, 2, y, (214, 214, 206)); px(img, 3, y, (150, 150, 146))
    return img


def stove_front():
    rng = random.Random(1226)
    img = gold(1227)
    glass, frame, knob = (40, 32, 24), (190, 190, 182), (54, 46, 34)
    for y in range(5, 12):
        for x in range(3, 13):
            px(img, x, y, frame if y in (5, 11) or x in (3, 12) else mul(glass, 1 + rng.uniform(-.1, .1)))
    for x in range(4, 12):
        px(img, x, 4, (210, 210, 204))  # oven door handle
    for x in (3, 6, 9, 12):
        px(img, x, 1, knob); px(img, x, 2, mul(knob, 1.4))
    for x in range(16):
        px(img, x, 14, mul((206, 162, 58), .7)); px(img, x, 15, (40, 32, 24))
    return img


def stove_top():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (52, 46, 40) if (x + y) % 3 else (60, 54, 48))
    coil, pan = (132, 128, 122), (32, 30, 28)
    for cx, cy in ((4, 4), (12, 4), (4, 12), (12, 12)):
        for y in range(16):
            for x in range(16):
                d = ((x - cx + .5) ** 2 + (y - cy + .5) ** 2) ** .5
                if d < 3.8:
                    px(img, x, y, pan)
                if 2.1 < d < 3.3:
                    px(img, x, y, mul(coil, 1.15 if y < cy else .9))
    return img


def formica():
    """Counter top: speckled tan laminate."""
    rng = random.Random(1228)
    img = canvas()
    base = (214, 196, 158)
    for y in range(16):
        for x in range(16):
            r = rng.random()
            px(img, x, y, mul(base, .78 if r < .07 else 1.08 if r < .15 else 1.0))
    return img


def cabinet():
    """A raised-panel oak cabinet door with a brass knob."""
    img = oak(1229, (168, 118, 66))
    dark = (104, 70, 38)
    for i in range(1, 15):
        px(img, 1, i, dark); px(img, 14, i, dark); px(img, i, 1, dark); px(img, i, 14, dark)
    for i in range(3, 13):
        px(img, 3, i, mul(dark, 1.25)); px(img, i, 3, (204, 160, 104))
    px(img, 12, 8, (226, 188, 90)); px(img, 12, 9, (170, 130, 50))
    return img


def steel():
    rng = random.Random(1230)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((176, 180, 182), 1 + (.04 if y % 3 == 0 else 0) + rng.uniform(-.03, .03)))
    return img


def basin():
    img = steel()
    for y in range(3, 13):
        for x in range(3, 13):
            px(img, x, y, mul((128, 132, 134), 1 - (abs(x - 7.5) + abs(y - 7.5)) * .02))
    px(img, 8, 8, (40, 40, 42))
    return img


def tv_wood():
    return oak(1231, (96, 62, 34))


def tv_front():
    """The console's front: walnut, a speaker grille on the right, two knobs and a brand plate."""
    img = tv_wood()
    for y in range(3, 14):
        for x in range(12, 15):
            px(img, x, y, (60, 50, 40) if (x + y) % 2 else (88, 74, 58))
    px(img, 13, 1, (200, 200, 190)); px(img, 13, 2, (130, 130, 124))
    px(img, 13, 14, (200, 200, 190))
    for x in range(3, 8):
        px(img, x, 15, (206, 176, 110))
    return img


def screen_off():
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            px(img, x, y, mul((44, 52, 48), 1.15 - d * .045))
    for i in range(3):
        px(img, 3 + i, 3 + i, (96, 104, 100))
    for c in ((0, 0), (15, 0), (0, 15), (15, 15)):
        px(img, *c, (14, 16, 16))
    return img


def screen_static(frames=8):
    """Broadcast snow: grey noise with brighter bands that roll, a dark rounded corner on the tube. Animated."""
    strip = canvas(16, 16 * frames)
    for f in range(frames):
        rng = random.Random(1240 + f)
        band = (f * 5) % 16
        for y in range(16):
            for x in range(16):
                v = rng.randint(40, 230)
                if abs(y - band) <= 1:
                    v = min(255, v + 40)
                d = max(abs(x - 7.5), abs(y - 7.5))
                v = v * (1.05 - d * .03)
                if d > 7.2 and (x in (0, 15)) and (y in (0, 15)):
                    v = 12
                px(strip, x, y + f * 16, (v, v, v * 1.04))
    return strip


def lampshade():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((232, 216, 176), (1.04 if x % 3 == 0 else .97) - y * .01))
    return img


def brass():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((196, 152, 70), 1.1 if (x + y) % 5 == 0 else .95))
    return img


def tiffany():
    """A stained-glass shade: amber, green and red panes in dark leading."""
    img = canvas()
    panes = [(212, 150, 52), (96, 136, 70), (176, 62, 48), (226, 190, 96)]
    for y in range(16):
        for x in range(16):
            if x % 4 == 0 or y % 5 == 0:
                px(img, x, y, (46, 36, 28))
            else:
                px(img, x, y, mul(panes[(x // 4 + y // 5) % 4], 1.0 + (.1 if y % 5 == 1 else 0)))
    return img


def frosted():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((236, 234, 222), 1 - abs(x - 7.5) * .012))
    return img


def clown():
    """The clown doll's parts on one sheet: face (0-7, 0-7), suit (8-15, 0-7), ruff (0-7, 8-11), hair (8-15, 8-11), shoes (0-7, 12-15)."""
    img = canvas()
    skin, white, red, black, blue = (236, 228, 214), (240, 236, 228), (196, 48, 44), (30, 26, 28), (60, 90, 170)
    fill(img, 0, 0, 7, 7, skin)
    for x, y in ((2, 2), (5, 2)):
        px(img, x, y, black); px(img, x, y - 1, blue)  # eyes, painted brows
    px(img, 3, 4, red); px(img, 4, 4, red)  # nose
    for x in range(1, 7):
        px(img, x, 6, red)  # the painted grin, too wide
    px(img, 1, 5, red); px(img, 6, 5, red)
    for y in range(8):
        for x in range(8, 16):
            px(img, x, y, white if ((x - 8) // 2 + y // 2) % 2 == 0 else red)
    for y in range(8, 12):
        for x in range(8):
            px(img, x, y, white if (x + y) % 2 == 0 else (214, 210, 204))
    for y in range(8, 12):
        for x in range(8, 16):
            px(img, x, y, (218, 96, 40) if (x + y) % 3 else (180, 70, 30))
    for y in range(12, 16):
        for x in range(8):
            px(img, x, y, black if y > 13 else (60, 58, 56))
    return img


def toy_chest():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((70, 104, 170), 1 + (.04 if y % 4 == 0 else 0)))
    for x, y in ((3, 3), (10, 5), (6, 10), (12, 12)):
        px(img, x, y, (240, 220, 110)); px(img, x + 1, y, (240, 220, 110)); px(img, x, y + 1, (240, 220, 110))
    for i in range(16):
        px(img, i, 0, (44, 66, 110)); px(img, i, 15, (44, 66, 110))
    return img


def curtain():
    """Pinch-pleat curtain: rust and cream flowers on an orange ground, with folds."""
    rng = random.Random(1250)
    img = canvas()
    ground = (200, 120, 50)
    for y in range(16):
        for x in range(16):
            f = (1.08 if x % 4 == 1 else .84 if x % 4 == 3 else 1.0) + rng.uniform(-.02, .02)
            px(img, x, y, mul(ground, f))
    for x, y in ((1, 2), (9, 6), (5, 11), (13, 14)):
        px(img, x, y, (236, 210, 160)); px(img, x + 1, y + 1, (140, 60, 30))
    return img


def phone():
    """The kitchen wall phone: gold, a rotary dial with its finger holes, the cord."""
    img = gold(1251)
    for y in range(16):
        for x in range(16):
            d = ((x - 8) ** 2 + (y - 9) ** 2) ** .5
            if d < 4.5:
                px(img, x, y, (232, 220, 190) if d < 2 else (160, 120, 40))
            if 3 < d < 4.4 and (x + y) % 2 == 0:
                px(img, x, y, (60, 46, 24))
    return img


def photo():
    """A framed family snapshot: a sky, a lawn and three small figures in front of a tract house."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (130, 92, 48) if x in (0, 15) or y in (0, 15) else (150, 186, 214) if y < 7 else (96, 140, 70))
    for x in range(4, 12):
        for y in range(4, 8):
            px(img, x, y, (226, 214, 190))
    for x in range(3, 13):
        px(img, x, 3, (140, 70, 50))
    for x, h in ((5, 3), (7, 4), (10, 2)):
        for y in range(11 - h, 11):
            px(img, x, y, (60, 50, 70))
        px(img, x, 10 - h, (230, 196, 170))
    return img


def snapshot(w, h, border, draw):
    """A print w by h pixels in the top-left of the sheet: a one-pixel border and the picture inside it."""
    img = canvas()
    for y in range(h):
        for x in range(w):
            px(img, x, y, border)
    draw(img)
    return img


def photo_wedding():
    """0.4.201 (owner: "the model home could use some custom artwork for its wall photos"): the wedding portrait, eight by ten
    in a gilt frame: the bride in white beside the groom in a dark suit, against a studio's grey-blue backdrop."""
    def draw(img):
        for y in range(1, 9):
            for x in range(1, 7):
                px(img, x, y, (116, 126, 146) if y < 5 else (104, 114, 134))
        for x, y in ((2, 2), (3, 2), (2, 3)):
            px(img, x, y, (238, 234, 222))  # veil
        px(img, 3, 3, (228, 194, 166))
        for y in range(4, 9):
            for x in (2, 3):
                px(img, x, y, (242, 238, 228))
        px(img, 3, 6, (196, 78, 98)); px(img, 2, 6, (120, 160, 90))  # the bouquet
        px(img, 4, 2, (82, 58, 40)); px(img, 5, 2, (82, 58, 40))
        px(img, 4, 3, (222, 184, 156)); px(img, 5, 3, (222, 184, 156))
        for y in range(4, 9):
            for x in (4, 5):
                px(img, x, y, (42, 42, 50))
        px(img, 4, 4, (226, 226, 226))
    return snapshot(8, 10, (178, 142, 64), draw)


def photo_beach():
    """Two children at the beach in a white-bordered print: sky, a line of sea, sand, a striped umbrella and a bucket."""
    def draw(img):
        for y in range(1, 7):
            for x in range(1, 11):
                px(img, x, y, (152, 198, 230) if y < 4 else (64, 124, 172) if y == 4 else (226, 206, 152))
        px(img, 9, 1, (250, 222, 110))
        for x in (6, 7, 8):
            px(img, x, 2, (214, 66, 60) if x % 2 == 0 else (240, 236, 226))
        for y in (3, 4, 5):
            px(img, 7, y, (190, 190, 190))
        px(img, 3, 3, (232, 190, 160)); px(img, 3, 4, (210, 60, 70)); px(img, 3, 5, (232, 190, 160))
        px(img, 2, 3, (120, 80, 50))
        px(img, 5, 4, (232, 190, 160)); px(img, 5, 5, (70, 130, 200)); px(img, 5, 3, (230, 200, 120))
        px(img, 9, 5, (250, 200, 60))
    return snapshot(12, 8, (236, 232, 220), draw)


def photo_school():
    """A school picture, eight by ten: a child with long fair hair on the mottled blue backdrop, in a card mount."""
    def draw(img):
        for y in range(1, 9):
            for x in range(1, 7):
                px(img, x, y, (92, 122, 172) if (x * 3 + y * 5) % 4 else (104, 134, 184))
        for y in range(2, 7):
            for x in (2, 5):
                px(img, x, y, (222, 192, 112))
        for x in (3, 4):
            px(img, x, 2, (222, 192, 112))
            px(img, x, 3, (234, 200, 174)); px(img, x, 4, (234, 200, 174))
        px(img, 3, 3, (138, 104, 92)); px(img, 4, 3, (138, 104, 92)); px(img, 3, 5, (206, 140, 140))
        px(img, 4, 5, (234, 200, 174))
        for y in range(6, 9):
            for x in range(2, 6):
                px(img, x, y, (220, 150, 172))
    return snapshot(8, 10, (206, 196, 172), draw)


def photo_dog():
    """The family dog on the lawn in front of the siding, in a white-bordered print."""
    def draw(img):
        for y in range(1, 7):
            for x in range(1, 11):
                px(img, x, y, (206, 196, 164) if y < 5 else (96, 150, 72))
        for x in range(1, 11):
            px(img, x, 2, (186, 176, 146))
        for x in range(4, 8):
            px(img, x, 4, (204, 150, 70))
            px(img, x, 3, (214, 160, 80))
        px(img, 8, 2, (214, 160, 80)); px(img, 9, 2, (214, 160, 80)); px(img, 8, 3, (204, 150, 70))
        px(img, 8, 1, (170, 112, 52)); px(img, 10, 2, (40, 30, 30)); px(img, 9, 1, (214, 160, 80))
        px(img, 3, 3, (214, 160, 80)); px(img, 2, 2, (214, 160, 80))
        for x in (4, 7):
            px(img, x, 5, (190, 136, 60))
    return snapshot(12, 8, (236, 232, 220), draw)


TEXTURES = {
    'paneling': paneling, 'shag': shag, 'linoleum': linoleum, 'popcorn': popcorn, 'kitchen_paper': kitchen_paper,
    'oak': oak, 'plaid': plaid, 'corduroy': corduroy, 'gold': gold, 'fridge_upper': lambda: fridge_front(True),
    'fridge_lower': lambda: fridge_front(False), 'stove_front': stove_front, 'stove_top': stove_top, 'formica': formica,
    'cabinet': cabinet, 'steel': steel, 'basin': basin, 'tv_wood': tv_wood, 'tv_front': tv_front, 'screen_off': screen_off,
    'screen_static': screen_static, 'lampshade': lampshade, 'brass': brass, 'tiffany': tiffany, 'frosted': frosted,
    'clown': clown, 'toy_chest': toy_chest, 'curtain': curtain, 'phone': phone, 'photo': photo,
    'photo_wedding': photo_wedding, 'photo_beach': photo_beach, 'photo_school': photo_school, 'photo_dog': photo_dog,
}
MATERIALS = ('paneling', 'shag', 'linoleum', 'popcorn', 'kitchen_paper')


# ------------------------------------------------------------------------------------------------ models
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


def box(frm, to, tex, faces=DIRS, uv=None, over=None):
    f = {}
    for d in faces:
        face = {'texture': '#' + tex}
        if uv:
            face['uv'] = list(uv)
        f[d] = face
    for d, t in (over or {}).items():
        if isinstance(t, tuple):
            f[d] = {'texture': '#' + t[0], 'uv': list(t[1])}
        else:
            f[d] = {'texture': '#' + t}
    return {'from': list(frm), 'to': list(to), 'faces': f}


def legs(x0, z0, x1, z1, top, tex, w=1.5):
    return [box((x, 0, z), (x + w, top, z + w), tex) for x in (x0, x1 - w) for z in (z0, z1 - w)]


def chair(scale=1.0):
    """The dining chair, authored facing north: the sitter faces -z, the spindle back stands at +z."""
    s = scale
    seat = 8 * s
    els = legs(2, 2, 14, 14, seat - 1, 'oak')
    els.append(box((2, seat - 1, 2), (14, seat + 1, 14), 'oak'))
    back_top = seat + 11 * s
    els += [box((2, seat + 1, 12), (4, back_top, 14), 'oak'), box((12, seat + 1, 12), (14, back_top, 14), 'oak')]
    els.append(box((3, back_top - 2.5, 12.5), (13, back_top - .5, 13.5), 'oak'))
    for x in (5.5, 7.5, 9.5):
        els.append(box((x, seat + 1, 12.5), (x + 1, back_top - 2.5, 13.5), 'oak'))
    return els


MODEL_SPECS = {}


def spec(name, textures, particle):
    def wrap(fn):
        MODEL_SPECS[name] = (fn, textures, particle)
        return fn
    return wrap


@spec('dining_chair', {'oak': 'oak'}, 'oak')
def m_chair():
    return chair()


@spec('kids_chair', {'oak': 'oak'}, 'oak')
def m_kids_chair():
    return chair(.7)


@spec('table_west', {'oak': 'oak'}, 'oak')
def m_table_west():
    els = [box((0, 14, 1), (16, 16, 15), 'oak')]
    els += [box((1, 12, 2), (16, 14, 3), 'oak'), box((1, 12, 13), (16, 14, 14), 'oak'), box((1, 12, 3), (2, 14, 13), 'oak')]
    for z in (2, 12.5):
        els += [box((1.5, 0, z), (3, 2, z + 1.5), 'oak'), box((1.75, 2, z + .25), (2.75, 9, z + 1.25), 'oak'), box((1.5, 9, z), (3, 12, z + 1.5), 'oak')]
    return els


@spec('table_east', {'oak': 'oak'}, 'oak')
def m_table_east():
    els = [box((0, 14, 1), (16, 16, 15), 'oak')]
    els += [box((0, 12, 2), (15, 14, 3), 'oak'), box((0, 12, 13), (15, 14, 14), 'oak'), box((14, 12, 3), (15, 14, 13), 'oak')]
    for z in (2, 12.5):
        els += [box((13, 0, z), (14.5, 2, z + 1.5), 'oak'), box((13.25, 2, z + .25), (14.25, 9, z + 1.25), 'oak'), box((13, 9, z), (14.5, 12, z + 1.5), 'oak')]
    return els


def sofa(left, right):
    els = [box((0, 0, 1), (16, 3, 15), 'oak'), box((0, 3, 1), (16, 8, 13), 'plaid'), box((0, 3, 12), (16, 17, 16), 'plaid')]
    if left:
        els.append(box((0, 3, 1), (3, 12, 13), 'plaid'))
    if right:
        els.append(box((13, 3, 1), (16, 12, 13), 'plaid'))
    return els


@spec('sofa_left', {'plaid': 'plaid', 'oak': 'tv_wood'}, 'plaid')
def m_sofa_left():
    return sofa(True, False)


@spec('sofa_middle', {'plaid': 'plaid', 'oak': 'tv_wood'}, 'plaid')
def m_sofa_middle():
    return sofa(False, False)


@spec('sofa_right', {'plaid': 'plaid', 'oak': 'tv_wood'}, 'plaid')
def m_sofa_right():
    return sofa(False, True)


@spec('recliner', {'c': 'corduroy', 'oak': 'tv_wood'}, 'corduroy')
def m_recliner():
    return [box((1, 0, 2), (15, 3, 15), 'oak'), box((1, 3, 1), (15, 8, 13), 'c'), box((1, 3, 11), (15, 19, 16), 'c'),
            box((0, 3, 1), (3, 12, 14), 'c'), box((13, 3, 1), (16, 12, 14), 'c'), box((3, 15, 13), (13, 19, 15), 'c')]


@spec('coffee_table', {'oak': 'oak', 'mag': 'tv_guide'}, 'oak')
def m_coffee_table():
    els = legs(1, 3, 15, 13, 6, 'oak') + [box((1, 6, 3), (15, 8, 13), 'oak'), box((1.5, 2, 3.5), (14.5, 3, 12.5), 'oak')]
    els.append(box((3, 8, 5), (8, 8.5, 9), 'mag', uv=(1, 1, 15, 15)))
    return els


@spec('end_table_lamp', {'oak': 'oak', 'brass': 'brass', 'shade': 'lampshade'}, 'oak')
def m_end_table_lamp():
    els = legs(2, 2, 14, 14, 9, 'oak') + [box((2, 9, 2), (14, 11, 14), 'oak')]
    els += [box((6, 11, 6), (10, 12, 10), 'brass'), box((7, 12, 7), (9, 18, 9), 'brass'), box((4, 18, 4), (12, 25, 12), 'shade', faces=('north', 'south', 'east', 'west'))]
    return els


@spec('tv_off', {'w': 'tv_wood', 'f': 'tv_front', 's': 'screen_off'}, 'tv_wood')
def m_tv_off():
    return tv()


@spec('tv_static', {'w': 'tv_wood', 'f': 'tv_front', 's': 'screen_static'}, 'tv_wood')
def m_tv_static():
    return tv()


def tv():
    els = [box((1, 2, 3), (15, 15, 14), 'w', over={'north': 'f'})]
    els += [box((x, 0, z), (x + 1.5, 2, z + 1.5), 'w') for x in (2, 12.5) for z in (4, 11.5)]
    els.append(box((3, 5, 2.6), (11, 13, 3), 's', faces=('north',), uv=(0, 0, 16, 16)))
    els.append(box((2.5, 4.5, 2.8), (11.5, 13.5, 3), 'w', faces=('north',), uv=(0, 0, 1, 1)))
    els.append(box((6, 15, 7), (10, 15.5, 10), 'w'))
    return els


@spec('fridge_lower', {'g': 'gold', 'f': 'fridge_lower'}, 'gold')
def m_fridge_lower():
    return [box((1, 0, 1), (15, 16, 15), 'g', over={'north': 'f'})]


@spec('fridge_upper', {'g': 'gold', 'f': 'fridge_upper'}, 'gold')
def m_fridge_upper():
    return [box((1, 0, 1), (15, 14, 15), 'g', over={'north': 'f'}), box((1.5, 14, 1.5), (14.5, 14.5, 14.5), 'g')]


@spec('stove', {'g': 'gold', 'f': 'stove_front', 't': 'stove_top'}, 'gold')
def m_stove():
    return [box((0, 0, 1), (16, 15, 16), 'g', over={'north': 'f', 'up': 't'}), box((0, 15, 13), (16, 21, 16), 'g')]


@spec('counter', {'c': 'cabinet', 'top': 'formica', 'oak': 'oak'}, 'cabinet')
def m_counter():
    return [box((0, 2, 1), (16, 15, 16), 'oak', over={'north': 'c'}), box((0, 0, 2.5), (16, 2, 16), 'oak'),
            box((0, 15, 0), (16, 16, 16), 'top')]


@spec('counter_sink', {'c': 'cabinet', 'top': 'formica', 'oak': 'oak', 'st': 'steel', 'b': 'basin'}, 'cabinet')
def m_counter_sink():
    return [box((0, 2, 1), (16, 15, 16), 'oak', over={'north': 'c'}), box((0, 0, 2.5), (16, 2, 16), 'oak'),
            box((0, 15, 0), (16, 16, 3), 'top'), box((0, 15, 13), (16, 16, 16), 'top'), box((0, 15, 3), (2, 16, 13), 'top'),
            box((14, 15, 3), (16, 16, 13), 'top'), box((2, 14.8, 3), (14, 15.6, 13), 'b', faces=('up',), uv=(2, 2, 14, 14)),
            box((7, 16, 13.5), (9, 21, 15), 'st'), box((7.5, 19.5, 10.5), (8.5, 20.5, 13.5), 'st')]


@spec('wall_cabinet', {'c': 'cabinet', 'oak': 'oak'}, 'cabinet')
def m_wall_cabinet():
    return [box((0, 1, 8), (16, 15, 16), 'oak', over={'north': 'c'})]


@spec('ceiling_light', {'g': 'frosted', 'b': 'brass'}, 'frosted')
def m_ceiling_light():
    return [box((4, 15, 4), (12, 16, 12), 'b'), box((5, 13, 5), (11, 15, 11), 'g'), box((6.5, 12.5, 6.5), (9.5, 13, 9.5), 'b')]


@spec('pendant_light', {'t': 'tiffany', 'b': 'brass'}, 'tiffany')
def m_pendant_light():
    return [box((7.5, 11, 7.5), (8.5, 16, 8.5), 'b'), box((3, 6, 3), (13, 11, 13), 't'), box((5, 11, 5), (11, 12, 11), 't'),
            box((7, 5, 7), (9, 6, 9), 'b')]


@spec('clown_doll', {'k': 'clown'}, 'clown')
def m_clown_doll():
    return [
        box((5, 0, 3), (7, 2, 9), 'k', uv=(1, 13, 3, 15)), box((9, 0, 3), (11, 2, 9), 'k', uv=(1, 13, 3, 15)),
        box((5, 0, 8), (11, 6, 12), 'k', uv=(9, 1, 15, 7)),
        box((3.5, 3, 8.5), (5, 6, 10.5), 'k', uv=(9, 2, 11, 5)), box((11, 3, 8.5), (12.5, 6, 10.5), 'k', uv=(9, 2, 11, 5)),
        box((4.5, 6, 7.5), (11.5, 7, 12.5), 'k', uv=(0, 8, 7, 11)),
        box((5.5, 7, 7.5), (10.5, 12, 11.5), 'k', uv=(9, 1, 14, 6), over={'north': ('k', (0, 0, 8, 8))}),
        box((4, 9, 8), (5.5, 12, 11), 'k', uv=(8, 8, 11, 11)), box((10.5, 9, 8), (12, 12, 11), 'k', uv=(8, 8, 11, 11)),
    ]


@spec('toy_chest', {'t': 'toy_chest', 'oak': 'oak'}, 'toy_chest')
def m_toy_chest():
    return [box((1, 0, 3), (15, 9, 13), 't'), box((0.5, 9, 2.5), (15.5, 11, 13.5), 'oak')]


@spec('curtain_left', {'c': 'curtain', 'b': 'brass'}, 'curtain')
def m_curtain_left():
    return [box((0, 0, 14), (5, 16, 15.5), 'c'), box((0, 15.5, 14.5), (16, 16, 15), 'b')]


@spec('curtain_right', {'c': 'curtain', 'b': 'brass'}, 'curtain')
def m_curtain_right():
    return [box((11, 0, 14), (16, 16, 15.5), 'c'), box((0, 15.5, 14.5), (16, 16, 15), 'b')]


@spec('wall_phone', {'p': 'phone', 'g': 'gold'}, 'gold')
def m_wall_phone():
    return [box((5, 4, 14), (11, 13, 16), 'g', over={'north': 'p'}), box((3, 6, 13), (5, 13, 15), 'g')]


@spec('family_photo', {'p': 'photo'}, 'photo')
def m_family_photo():
    return [box((3, 5, 15), (13, 13, 16), 'p', uv=(0, 0, 16, 16))]


# The four snapshots: each picture's own size, its pixels square on the wall.
for _name, _w, _h in (('photo_wedding', 8, 10), ('photo_beach', 12, 8), ('photo_school', 8, 10), ('photo_dog', 12, 8)):
    def _make(w=_w, h=_h):
        x0, y0 = 8 - w // 2, 9 - h // 2
        return [box((x0, y0, 15), (x0 + w, y0 + h, 16), 'p', uv=(0, 0, w, h))]
    MODEL_SPECS[_name] = (_make, {'p': _name}, _name)



# ------------------------------------------------------------------------------------------------ Mapping the Interior (0.4.202)
# The owner: "duplicated pictures", "recycled cabinets", "floating, recycled clutter blocks". The boys' modular house has its
# own things now: what a mother on her own with two boys has in a small house at the edge of a dirt lot.
def tv_guide():
    """The week's TV listings, folded back on the coffee table: a red masthead, grey columns of listings, a pen ring."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (228, 224, 212) if y > 3 else (186, 44, 40))
    for y in range(5, 15, 2):
        for x in range(2, 14):
            if (x * 7 + y * 3) % 5:
                px(img, x, y, (120, 118, 112))
    for x, y in ((9, 7), (10, 7), (11, 8), (11, 9), (10, 10), (9, 10), (8, 9), (8, 8)):
        px(img, x, y, (40, 60, 150))
    return img


def map_couch():
    """The couch: worn brown upholstery, the arms rubbed pale, a crocheted afghan in zigzags over its back."""
    rng = random.Random(1260)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((110, 82, 58), 1 + rng.uniform(-.05, .05) + (.08 if y % 3 == 0 else 0)))
    for y in range(0, 6):
        for x in range(16):
            band = ((x + (y if (x // 2) % 2 else -y)) // 2) % 3
            px(img, x, y, ((196, 120, 52), (214, 196, 150), (96, 128, 72))[band])
    return img


def map_recliner():
    """His recliner, that nobody sits in: olive vinyl gone shiny, a strip of grey tape over a split in the arm."""
    rng = random.Random(1261)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((104, 108, 66), 1 + rng.uniform(-.04, .04) + (.1 if (x + y) % 7 == 0 else 0)))
    for x in range(9, 15):
        for y in (6, 7):
            px(img, x, y, (156, 156, 150) if (x + y) % 3 else (130, 130, 126))
    return img


def map_cabinet():
    """Kitchen cabinet doors in pale wood-grain laminate; one corner of the face has chipped off to the board underneath."""
    img = oak(1262, (196, 160, 112))
    dark = (138, 104, 66)
    for i in range(1, 15):
        px(img, 1, i, dark); px(img, 14, i, dark); px(img, i, 1, dark); px(img, i, 14, dark)
    for x, y in ((13, 12), (13, 13), (12, 13)):
        px(img, x, y, (176, 150, 112))
    px(img, 3, 8, (200, 200, 196)); px(img, 3, 9, (150, 150, 146))
    return img


def map_counter():
    """Almond laminate with a brown fleck, and the burn ring where a pan went down hot."""
    rng = random.Random(1263)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((222, 210, 182), .84 if rng.random() < .06 else 1.0))
    for x, y in ((5, 5), (6, 4), (7, 4), (8, 5), (8, 6), (7, 7), (6, 7), (5, 6)):
        px(img, x, y, (140, 112, 74))
    return img


def map_stove():
    """The white range: its oven window, four dials, and the clock in green: 2:49."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (232, 230, 222))
    for x in range(2, 14):
        px(img, x, 1, (40, 40, 42)); px(img, x, 2, (40, 40, 42)); px(img, x, 3, (40, 40, 42))
    digits = {'2': ['##', '.#', '##', '#.', '##'], '4': ['#.#', '#.#', '###', '..#', '..#'], '9': ['###', '#.#', '###', '..#', '###']}
    for ch, ox in (('2', 4), ('4', 8), ('9', 11)):
        pat = digits[ch]
        for j, row in enumerate(pat[:3]):
            for i, c in enumerate(row):
                if c == '#':
                    px(img, ox + i, 1 + j, (90, 230, 120))
    px(img, 7, 2, (90, 230, 120))
    for x in (3, 6, 9, 12):
        px(img, x, 5, (60, 60, 60)); px(img, x + 1, 5, (90, 90, 90))
    for y in range(7, 15):
        for x in range(3, 13):
            px(img, x, y, (70, 66, 62) if 8 <= y <= 13 and 4 <= x <= 11 else (200, 198, 190))
    return img


def map_fridge(upper):
    """The white fridge. Up top a school picture and a drawing of a dog; below, the seizure log taped on, dates and times in
    her hand, and the number for the clinic."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (236, 236, 230))
    if upper:
        for y in range(3, 10):
            for x in range(3, 8):
                px(img, x, y, (96, 126, 176) if y < 6 else (180, 140, 110))
        px(img, 5, 4, (230, 196, 170)); px(img, 5, 5, (60, 40, 30))
        for y in range(4, 12):
            for x in range(9, 14):
                px(img, x, y, (246, 244, 236))
        for x, y in ((10, 7), (11, 7), (12, 7), (10, 8), (12, 8), (11, 6)):
            px(img, x, y, (160, 100, 50))
        px(img, 11, 10, (40, 160, 60))
        px(img, 1, 14, (200, 200, 196)); px(img, 1, 15, (200, 200, 196))
    else:
        for y in range(1, 13):
            for x in range(3, 13):
                px(img, x, y, (248, 248, 240))
        for y in range(3, 12, 2):
            for x in range(4, 12):
                if (x + y) % 4:
                    px(img, x, y, (70, 70, 120))
        for x in range(3, 13):
            px(img, x, 1, (210, 60, 60))
        px(img, 3, 0, (220, 210, 150)); px(img, 12, 0, (220, 210, 150))
        px(img, 1, 1, (200, 200, 196)); px(img, 1, 2, (200, 200, 196))
    return img


def map_table():
    """The kitchen table's top: walnut-look laminate, scratched pale along its edge."""
    img = oak(1264, (132, 92, 56))
    for x in range(0, 16, 3):
        px(img, x, 15, (180, 146, 104))
    return img


def map_dresser():
    """The boys' dresser: brown veneer drawers, the knobs gone from one, stickers up the side, a scrape along the top."""
    img = oak(1265, (122, 84, 52))
    for y in (5, 10):
        for x in range(16):
            px(img, x, y, (74, 50, 30))
    for y in (2, 7, 12):
        px(img, 5, y, (210, 190, 120)); px(img, 10, y, (210, 190, 120))
    px(img, 10, 7, (74, 50, 30))
    for x, y, c in ((1, 1, (230, 60, 60)), (2, 1, (230, 60, 60)), (13, 8, (60, 120, 220)), (14, 8, (60, 120, 220)), (13, 9, (240, 210, 60)), (2, 13, (80, 190, 90))):
        px(img, x, y, c)
    return img


def map_dresser_mom():
    """Her dresser: the same veneer, a lace runner across the top, her jewellery box and a bottle of lotion on it."""
    img = oak(1266, (130, 90, 56))
    for y in (5, 10):
        for x in range(16):
            px(img, x, y, (80, 54, 32))
    for y in (2, 7, 12):
        px(img, 4, y, (226, 214, 180)); px(img, 11, y, (226, 214, 180))
    return img


def map_lace():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (236, 230, 214) if (x + y) % 3 else (214, 206, 186))
    return img


def map_toys():
    """Dino's toys on the carpet: a yellow dump truck, a plastic horse, two action figures and a red ball."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (224, 190, 48) if y < 6 else (190, 50, 44) if y < 10 else (110, 90, 70) if y < 13 else (70, 110, 170))
    for x in range(16):
        px(img, x, 5, (170, 140, 30))
    return img


def map_curtain():
    """A flowered bed sheet tacked over the window for a curtain, the tacks showing at its hem."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((214, 206, 230), 1.06 if x % 4 == 1 else .9 if x % 4 == 3 else 1.0))
    for x, y in ((2, 3), (9, 7), (5, 12), (12, 13), (13, 2)):
        px(img, x, y, (206, 110, 150)); px(img, x + 1, y, (120, 160, 90)); px(img, x, y + 1, (206, 110, 150))
    return img


def map_photo_father():
    """A snapshot gone orange with age: a young man by a pickup, a baby in a blanket in his arms. No face to make out."""
    def draw(img):
        for y in range(1, 9):
            for x in range(1, 11):
                px(img, x, y, (206, 170, 120) if y < 5 else (150, 130, 96))
        for x in range(5, 11):
            for y in range(4, 7):
                px(img, x, y, (120, 60, 46))
        px(img, 6, 7, (40, 36, 34)); px(img, 9, 7, (40, 36, 34))
        for y in range(3, 9):
            px(img, 3, y, (60, 52, 70))
        px(img, 3, 2, (176, 130, 100)); px(img, 3, 1, (40, 30, 26))
        px(img, 4, 4, (238, 232, 214)); px(img, 4, 5, (238, 232, 214))
    return snapshot(12, 10, (240, 236, 224), draw)


def map_photo_boys():
    """The two boys by the old tetherball pole in the yard, the bigger one's hand on the little one's head."""
    def draw(img):
        for y in range(1, 9):
            for x in range(1, 9):
                px(img, x, y, (170, 200, 220) if y < 5 else (176, 156, 110))
        for y in range(1, 8):
            px(img, 6, y, (150, 150, 150))
        px(img, 7, 2, (220, 200, 60))
        for y in range(4, 8):
            px(img, 2, y, (70, 90, 150))
        px(img, 2, 3, (176, 130, 100))
        for y in (6, 7):
            px(img, 4, y, (190, 60, 50))
        px(img, 4, 5, (176, 130, 100)); px(img, 3, 4, (70, 90, 150))
    return snapshot(10, 10, (240, 236, 224), draw)


TEXTURES.update({'tv_guide': tv_guide, 'map_couch': map_couch, 'map_recliner': map_recliner, 'map_cabinet': map_cabinet,
                 'map_counter': map_counter, 'map_stove': map_stove, 'map_fridge_upper': lambda: map_fridge(True),
                 'map_fridge_lower': lambda: map_fridge(False), 'map_table': map_table, 'map_dresser': map_dresser,
                 'map_dresser_mom': map_dresser_mom, 'map_lace': map_lace, 'map_toys': map_toys, 'map_curtain': map_curtain,
                 'map_photo_father': map_photo_father, 'map_photo_boys': map_photo_boys})


def _like(name, base, textures, particle):
    """A Mapping piece on another piece's frame, with its own textures."""
    MODEL_SPECS[name] = (MODEL_SPECS[base][0], textures, particle)


for _n, _b in (('map_sofa_left', 'sofa_left'), ('map_sofa_middle', 'sofa_middle'), ('map_sofa_right', 'sofa_right')):
    _like(_n, _b, {'plaid': 'map_couch', 'oak': 'tv_wood'}, 'map_couch')
_like('map_recliner', 'recliner', {'c': 'map_recliner', 'oak': 'tv_wood'}, 'map_recliner')
_like('map_counter', 'counter', {'c': 'map_cabinet', 'top': 'map_counter', 'oak': 'map_table'}, 'map_cabinet')
_like('map_counter_sink', 'counter_sink', {'c': 'map_cabinet', 'top': 'map_counter', 'oak': 'map_table', 'st': 'steel', 'b': 'basin'}, 'map_cabinet')
_like('map_wall_cabinet', 'wall_cabinet', {'c': 'map_cabinet', 'oak': 'map_table'}, 'map_cabinet')
_like('map_stove', 'stove', {'g': 'map_lace', 'f': 'map_stove', 't': 'stove_top'}, 'map_stove')
_like('map_fridge_lower', 'fridge_lower', {'g': 'map_lace', 'f': 'map_fridge_lower'}, 'map_fridge_lower')
_like('map_fridge_upper', 'fridge_upper', {'g': 'map_lace', 'f': 'map_fridge_upper'}, 'map_fridge_upper')
_like('map_table_east', 'table_east', {'oak': 'map_table'}, 'map_table')
_like('map_chair', 'dining_chair', {'oak': 'map_table'}, 'map_table')
_like('map_curtain_left', 'curtain_left', {'c': 'map_curtain', 'b': 'steel'}, 'map_curtain')
_like('map_curtain_right', 'curtain_right', {'c': 'map_curtain', 'b': 'steel'}, 'map_curtain')


@spec('map_tv', {'w': 'tv_wood', 'f': 'tv_front', 's': 'screen_off', 'st': 'steel'}, 'tv_wood')
def m_map_tv():
    """The set on its cart, rabbit ears up, the VCR under it blinking nothing."""
    rod = lambda angle: dict(box((7.8, 16, 8.3), (8.2, 26, 8.7), 'st', faces=('north', 'south', 'east', 'west')),
                             rotation={'origin': [8, 16, 8.5], 'axis': 'z', 'angle': angle})
    return tv() + [box((7, 15.5, 7.5), (9, 16.5, 9.5), 'st'), rod(22.5), rod(-22.5)]


def dresser():
    return [box((1, 0, 3), (15, 14, 15), 'oak', over={'north': 'd'}), box((0.5, 14, 2.5), (15.5, 15, 15.5), 'oak')]


@spec('map_dresser', {'oak': 'map_table', 'd': 'map_dresser', 'k': 'map_toys'}, 'map_dresser')
def m_map_dresser():
    return dresser() + [box((3, 15, 8), (6, 18, 11), 'k', uv=(0, 0, 4, 4)), box((9, 15, 6), (13, 16, 12), 'k', uv=(0, 13, 6, 16))]


@spec('map_mom_dresser', {'oak': 'map_table', 'd': 'map_dresser_mom', 'l': 'map_lace', 'j': 'tv_wood', 'g': 'brass'}, 'map_dresser_mom')
def m_map_mom_dresser():
    return dresser() + [box((2, 15, 6), (14, 15.25, 13), 'l', faces=('up', 'north')), box((4, 15.25, 8), (8, 17.5, 11), 'j'),
                        box((4.5, 17.5, 8.5), (7.5, 17.75, 10.5), 'g'), box((10.5, 15.25, 9), (11.5, 19, 10), 'l')]


@spec('map_toys', {'k': 'map_toys'}, 'map_toys')
def m_map_toys():
    """On the carpet: the dump truck with its bed tipped, the plastic horse, two action figures face down, the ball."""
    return [box((2, 0, 3), (8, 2.5, 6), 'k', uv=(0, 0, 6, 3)), box((6, 2.5, 3), (8, 4, 6), 'k', uv=(0, 0, 2, 2)),
            box((2.5, 0, 2.5), (3.5, 1, 3), 'k', uv=(0, 13, 1, 14)), box((6.5, 0, 2.5), (7.5, 1, 3), 'k', uv=(0, 13, 1, 14)),
            box((10, 0, 4), (13, 3, 5), 'k', uv=(0, 10, 3, 13)), box((12, 3, 4), (13, 4.5, 5), 'k', uv=(0, 10, 1, 11)),
            box((4, 0, 10), (8, .8, 11), 'k', uv=(0, 13, 4, 14)), box((9, 0, 11), (12.5, .8, 12), 'k', uv=(0, 6, 4, 7)),
            box((11, 0, 7), (14, 3, 10), 'k', uv=(0, 6, 3, 9))]


for _name, _w, _h in (('map_photo_father', 12, 10), ('map_photo_boys', 10, 10)):
    def _make(w=_w, h=_h):
        x0, y0 = 8 - w // 2, 9 - h // 2
        return [box((x0, y0, 15), (x0 + w, y0 + h, 16), 'p', uv=(0, 0, w, h))]
    MODEL_SPECS[_name] = (_make, {'p': _name}, _name)


KINDS = list(MODEL_SPECS)
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def model_json(name):
    fn, textures, particle = MODEL_SPECS[name]
    tex = {k: NS + v for k, v in textures.items()}
    tex['particle'] = NS + particle
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False, 'textures': tex, 'elements': fn()}


def files():
    """Every file this tool owns, as {path: bytes-or-image}."""
    out = {}
    for name, fn in TEXTURES.items():
        out[TEX / f'{name}.png'] = fn()
    out[TEX / 'screen_static.png.mcmeta'] = json.dumps({'animation': {'frametime': 2, 'interpolate': False}}, indent=2) + '\n'
    for name in KINDS:
        out[MODELS / f'{name}.json'] = json.dumps(model_json(name), indent=1) + '\n'
    variants = {}
    for facing, y in TURN.items():
        for kind in KINDS:
            v = {'model': NS + kind}
            if y:
                v['y'] = y
            variants[f'facing={facing},kind={kind}'] = v
    out[STATES / 'tract_house.json'] = json.dumps({'variants': variants}, indent=1) + '\n'
    for m in MATERIALS:
        out[STATES / f'tract_{m}.json'] = json.dumps({'variants': {'': {'model': NS + m}}}, indent=2) + '\n'
        out[MODELS / f'{m}.json'] = json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + m}}, indent=2) + '\n'
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    bad = []
    for path, data in files().items():
        if args.check:
            if not path.exists():
                bad.append(f'missing {path}')
            elif isinstance(data, Image.Image):
                with Image.open(path) as saved:
                    if saved.size != data.size or saved.convert('RGBA').tobytes() != data.tobytes():
                        bad.append(f'differs {path}')
            elif path.read_text() != data:
                bad.append(f'differs {path}')
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            if isinstance(data, Image.Image):
                data.save(path)
            else:
                path.write_text(data)
    if bad:
        print('\n'.join(bad))
        sys.exit(1)
    print(f'{"Verified" if args.check else "Drew"} {len(TEXTURES)} tract house textures, {len(KINDS)} kinds and {len(MATERIALS)} materials.')


if __name__ == '__main__':
    main()
