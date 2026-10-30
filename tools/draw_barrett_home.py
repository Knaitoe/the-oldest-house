#!/usr/bin/env python3
"""The Barretts' house (0.4.121): vanilla-style 16x16 textures and the barrett_home block's models and states.

The family's own things make it a home somebody lives in (painted walls, blinds, the fridge with its magnets and a
drawing, school portraits in the hall, mail on the console, coats and sneakers by the door, laundry, the girls' posters
and homework, John's job listings) and the crew's gear is spread through it (taped cable runs, softboxes, director's
chairs, the video-village cart, apple boxes, sandbags, craft services and the call sheets on a folding table in the
garage). Every texture is drawn pixel by pixel from a small palette with one-pixel bevels and scattered noise, as
vanilla's are; nothing is sampled. Run with --check to confirm the committed files.
"""
import argparse
import json
import math
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
TEX = ROOT / 'textures/block/barrett'
MODELS = ROOT / 'models/block/barrett'
STATES = ROOT / 'blockstates'
NS = 'the_oldest_house:block/barrett/'


def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


def mix(a, b, t):
    return clamp(tuple(a[i] * (1 - t) + b[i] * t for i in range(3)))


def canvas():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def px(img, x, y, c, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), clamp(c) + (a,))


def get(img, x, y):
    return img.getpixel((x, y))[:3]


def ground(c, seed, noise=.04):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(c, 1 + rng.uniform(-noise, noise)))
    return img


def rect(img, x0, y0, x1, y1, c, noise=0, seed=0):
    rng = random.Random(seed)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px(img, x, y, mul(c, 1 + rng.uniform(-noise, noise)))


def bevel(img, x0, y0, x1, y1, light=1.14, dark=.76):
    for x in range(x0, x1 + 1):
        px(img, x, y0, mul(get(img, x, y0), light), img.getpixel((x, y0))[3])
        px(img, x, y1, mul(get(img, x, y1), dark), img.getpixel((x, y1))[3])
    for y in range(y0 + 1, y1):
        px(img, x0, y, mul(get(img, x0, y), light), img.getpixel((x0, y))[3])
        px(img, x1, y, mul(get(img, x1, y), dark), img.getpixel((x1, y))[3])


def grain(base, seed, along_x=True, knots=0):
    rng = random.Random(seed)
    img = ground(base, seed, .03)
    for i in range(16):
        if rng.random() < .4:
            for j in range(16):
                if rng.random() < .8:
                    x, y = (j, i) if along_x else (i, j)
                    px(img, x, y, mul(get(img, x, y), .88))
    for _ in range(knots):
        x, y = rng.randrange(2, 14), rng.randrange(2, 14)
        px(img, x, y, mul(base, .7))
    return img


# ------------------------------------------------------------------------------------------------ materials
def paint():
    """Warm greige eggshell paint on drywall: almost flat, the roller's faint vertical drag."""
    rng = random.Random(5101)
    img = canvas()
    for x in range(16):
        f = 1 + rng.uniform(-.012, .012)
        for y in range(16):
            px(img, x, y, mul((206, 198, 184), f + rng.uniform(-.015, .015)))
    return img


def concrete():
    """The garage slab: grey concrete, pitted, with an old oil stain where a car stood."""
    rng = random.Random(5102)
    img = ground((150, 148, 142), 5102, .05)
    for _ in range(10):
        px(img, rng.randrange(16), rng.randrange(16), (120, 118, 112))
    for y in range(16):
        for x in range(16):
            d = ((x - 10.5) ** 2 + (y - 9.5) ** 2) ** .5
            if d < 4.2:
                px(img, x, y, mul(get(img, x, y), .78 + d * .04))
    return img


def drywall():
    """Unpainted garage drywall: grey-white board, a taped seam down the middle, screw dimples."""
    img = ground((214, 212, 204), 5103, .025)
    for y in range(16):
        px(img, 7, y, (224, 221, 210)); px(img, 8, y, (226, 223, 212))
    for y in (3, 11):
        for x in (2, 13):
            px(img, x, y, (186, 184, 176))
    return img


def blinds():
    """A window with the blinds drawn: white slats, a warm line of daylight between each, the cord down one side."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px(img, x, y, (232, 230, 224))
            elif y % 2 == 1:
                px(img, x, y, (248, 236, 200) if 2 < x < 13 else (236, 224, 190))
            else:
                px(img, x, y, mul((228, 226, 220), 1 - .02 * (x % 3 == 0)))
    for y in range(1, 15):
        px(img, 12, y, (196, 192, 184))
    px(img, 12, 14, (150, 146, 140))
    bevel(img, 0, 0, 15, 15, 1.04, .82)
    return img


def garage_door():
    """The inside of the sectional garage door: white panels with grooves, the top row of small dark windows."""
    img = ground((228, 228, 222), 5104, .02)
    for y in (3, 7, 11, 15):
        for x in range(16):
            px(img, x, y, (186, 186, 180))
    for x0 in (2, 7, 12):
        for x in range(x0, x0 + 3):
            for y in (1, 2):
                px(img, x, y, (64, 74, 84))
    for y in range(8, 11):
        px(img, 7, y, (150, 150, 146)); px(img, 8, y, (150, 150, 146))
    return img


def ceiling_plaster():
    """The underside of the floor above: a white plastered ceiling, faintly stippled."""
    return ground((238, 236, 230), 5105, .018)


def ceiling_light():
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 7.6:
                px(img, x, y, mix((255, 250, 232), (226, 220, 204), min(1, d / 7.6)))
    return img


# ------------------------------------------------------------------------------------------------ woods and finishes
def oak():
    return grain((198, 162, 114), 5110, True, 1)


def walnut():
    return grain((104, 72, 50), 5111, True, 1)


def white():
    img = ground((236, 234, 228), 5112, .015)
    return img


def black():
    return ground((34, 34, 36), 5113, .05)


def chrome():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mix((214, 218, 224), (120, 126, 134), ((x + y) % 8) / 8))
    return img


def porcelain():
    img = ground((240, 240, 236), 5114, .012)
    for i in range(3):
        px(img, 3 + i, 3, (255, 255, 255)); px(img, 3, 3 + i, (255, 255, 255))
    return img


def marble():
    rng = random.Random(5115)
    img = ground((234, 232, 228), 5115, .015)
    x = rng.randrange(16)
    for y in range(16):
        x = (x + rng.choice((-1, 0, 1))) % 16
        px(img, x, y, (196, 194, 192))
    return img


# ------------------------------------------------------------------------------------------------ the family's things
def mail():
    """The console's top: envelopes fanned out, a catalog with a bright cover, a bill with a red window."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (232, 228, 218), .02, 5120)
    rect(img, 1, 2, 7, 6, (246, 244, 238)); rect(img, 3, 3, 6, 3, (180, 176, 170))
    rect(img, 5, 6, 12, 10, (238, 236, 230)); rect(img, 9, 7, 11, 8, (196, 64, 52))
    rect(img, 2, 9, 8, 15, (64, 132, 170), .04, 5121); rect(img, 3, 10, 7, 11, (238, 210, 90)); rect(img, 3, 13, 6, 13, (230, 230, 230))
    return img


def coats():
    """Hung by the door, left to right: Dad's navy coat, Sarah's red raincoat, Merry's purple school backpack."""
    img = canvas()
    rect(img, 0, 0, 5, 15, (44, 58, 92), .05, 5122); px(img, 2, 0, (30, 40, 64)); px(img, 3, 0, (30, 40, 64))
    for y in (4, 8, 12):
        px(img, 3, y, (200, 200, 190))
    rect(img, 6, 0, 10, 15, (178, 44, 40), .05, 5123)
    for y in range(2, 15):
        px(img, 8, y, (150, 34, 32))
    rect(img, 11, 3, 15, 15, (120, 78, 160), .05, 5124); rect(img, 12, 8, 14, 11, (98, 60, 136)); px(img, 13, 9, (236, 210, 80))
    return img


def shoes():
    """The shoe rack's front: sneakers, Sarah's flats and Merry's small pink trainers on two shelves."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (198, 162, 114), .03, 5125)
    rect(img, 0, 7, 15, 8, (168, 134, 92))
    for x0, c in ((1, (240, 240, 236)), (5, (240, 240, 236)), (10, (40, 40, 44))):
        rect(img, x0, 3, x0 + 3, 6, c); px(img, x0, 6, mul(c, .7))
    for x0, c in ((2, (232, 140, 170)), (8, (232, 140, 170)), (12, (90, 120, 160))):
        rect(img, x0, 11, x0 + 2, 14, c)
    return img


def photos():
    """School portraits in the hall: two girls against the mottled blue school backdrop, and the family at the beach."""
    img = canvas()
    for x0, x1, hair in ((0, 4, (62, 40, 30)), (11, 15, (196, 150, 90))):
        rect(img, x0, 1, x1, 10, (30, 26, 24))
        rect(img, x0 + 1, 2, x1 - 1, 9, (104, 132, 176), .08, 5126 + x0)
        rect(img, x0 + 1, 3, x1 - 1, 5, hair); px(img, x0 + 2, 5, (230, 196, 170)); px(img, x0 + 2, 6, (230, 196, 170))
        rect(img, x0 + 1, 7, x1 - 1, 9, (180, 60, 60) if x0 else (60, 90, 70))
    rect(img, 5, 5, 10, 13, (220, 216, 206))
    rect(img, 6, 6, 9, 9, (120, 180, 214)); rect(img, 6, 10, 9, 12, (226, 206, 150))
    for x in (6, 7, 8, 9):
        px(img, x, 9, (60, 50, 44))
    return img


def calendar():
    """The family calendar: a picture of a lighthouse above a month grid crowded with appointments in two hands."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (244, 242, 236))
    rect(img, 1, 1, 14, 6, (120, 170, 210)); rect(img, 1, 5, 14, 6, (90, 140, 90)); rect(img, 9, 2, 10, 5, (236, 236, 230)); px(img, 9, 2, (200, 60, 50))
    for y in range(8, 15, 2):
        for x in range(1, 15):
            px(img, x, y, (206, 204, 198))
    for x in range(1, 15, 2):
        for y in range(8, 15):
            px(img, x, y, (206, 204, 198))
    for x, y, c in ((2, 9, (40, 60, 160)), (6, 11, (200, 50, 50)), (10, 9, (40, 60, 160)), (12, 13, (200, 50, 50)), (4, 13, (40, 140, 60))):
        px(img, x, y, c)
    return img


def fridge_front():
    """The fridge door: white enamel, a long steel handle and the family's magnets."""
    img = ground((238, 238, 234), 5127, .012)
    for y in range(2, 14):
        px(img, 13, y, (180, 184, 190)); px(img, 14, y, (150, 154, 160))
    for (x, y), c in zip(((3, 3), (7, 5), (4, 9), (9, 11), (6, 13)), ((220, 60, 60), (60, 140, 220), (240, 200, 40), (90, 180, 90), (230, 120, 40))):
        px(img, x, y, c); px(img, x + 1, y, mul(c, .8))
    rect(img, 2, 6, 6, 7, (250, 250, 250)); px(img, 2, 6, (200, 50, 50))
    return img


def fridge_top():
    """The freezer door: a crayon drawing of the family (four figures and a sun), the school lunch menu, magnets."""
    img = ground((238, 238, 234), 5128, .012)
    for y in range(3, 13):
        px(img, 13, y, (180, 184, 190)); px(img, 14, y, (150, 154, 160))
    rect(img, 1, 2, 8, 9, (252, 250, 244))
    px(img, 7, 3, (240, 200, 40)); px(img, 6, 3, (240, 200, 40)); px(img, 7, 4, (240, 200, 40))
    for x0, c in ((2, (40, 60, 160)), (3, (200, 50, 50)), (5, (120, 70, 160)), (6, (230, 120, 170))):
        px(img, x0, 6, c); px(img, x0, 7, c); px(img, x0, 8, mul(c, .8))
    rect(img, 9, 8, 12, 14, (244, 242, 228))
    for y in (9, 10, 11, 12, 13):
        px(img, 10, y, (150, 150, 150)); px(img, 11, y, (180, 180, 180))
    px(img, 4, 1, (220, 60, 60)); px(img, 10, 7, (60, 140, 220))
    return img


def fridge_side():
    return ground((226, 226, 222), 5129, .015)


def fabric():
    """The sofa's woven slate-blue fabric, heathered."""
    rng = random.Random(5130)
    img = canvas()
    for y in range(16):
        for x in range(16):
            base = (98, 112, 126) if (x + y) % 2 else (90, 104, 118)
            px(img, x, y, mul(base, 1 + rng.uniform(-.06, .06)))
    return img


def cushion():
    """Seat cushions from above: two, with a piped seam and a throw blanket's corner."""
    img = fabric()
    for y in range(16):
        px(img, 7, y, (70, 82, 94)); px(img, 8, y, (110, 124, 138))
    for x in range(16):
        px(img, x, 0, (70, 82, 94)); px(img, x, 15, (70, 82, 94))
    for y in range(9, 16):
        for x in range(10, 16):
            if x - 10 > 15 - y - 1:
                px(img, x, y, (196, 168, 120) if (x + y) % 3 else (176, 148, 102))
    return img


def tv_screen():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (22, 24, 28))
    for i in range(5):
        px(img, 3 + i, 2 + i, (46, 50, 58))
    for x in range(16):
        px(img, x, 0, (12, 12, 14)); px(img, x, 15, (12, 12, 14))
    return img


def games():
    """The family shelves: board-game boxes on their sides, a row of paperbacks, the baby albums."""
    rng = random.Random(5131)
    img = canvas()
    rect(img, 0, 0, 15, 15, (104, 72, 50), .03, 5131)
    for y in (5, 10, 15):
        rect(img, 0, y, 15, y, (80, 54, 38))
    for y0, y1, colours in ((1, 4, ((200, 60, 50), (240, 200, 60), (50, 120, 190), (230, 230, 220))), (6, 9, None), (11, 14, ((236, 220, 196), (196, 210, 230), (230, 196, 210)))):
        x = 1
        while x < 15:
            w = rng.choice((1, 1, 2)) if colours is None else 3
            c = rng.choice(((180, 60, 50), (60, 90, 140), (220, 200, 140), (70, 120, 80), (40, 40, 44))) if colours is None else colours[(x // 3) % len(colours)]
            for xx in range(x, min(15, x + w)):
                for yy in range(y0 + (0 if colours else rng.randrange(0, 2)), y1 + 1):
                    px(img, xx, yy, mul(c, 1 + rng.uniform(-.05, .05)))
            x += w + (1 if colours else 0)
    return img


def shelf_side():
    return walnut()


def pot():
    img = ground((236, 232, 222), 5132, .02)
    for x in range(16):
        px(img, x, 0, (210, 206, 196)); px(img, x, 15, (190, 186, 176))
    return img


def leaves():
    """Fiddle-leaf fig leaves on a cut-out: broad, glossy, darker at the midrib."""
    rng = random.Random(5133)
    img = canvas()
    for cx, cy, r in ((4, 4, 3.4), (11, 3, 3.0), (7, 9, 3.8), (13, 10, 2.8), (3, 12, 2.6)):
        for y in range(16):
            for x in range(16):
                d = ((x - cx) ** 2 / 1.0 + (y - cy) ** 2 / 1.6) ** .5
                if d < r:
                    c = (58, 120, 56) if (x + y) % 3 else (70, 138, 64)
                    if abs(x - cx) < .6:
                        c = (44, 96, 44)
                    px(img, x, y, mul(c, 1 + rng.uniform(-.06, .06)))
    for y in range(12, 16):
        px(img, 8, y, (96, 70, 44))
    return img


def basket():
    """A white plastic laundry basket's side, with its grid of slots."""
    img = ground((236, 236, 232), 5134, .015)
    for y in range(2, 14):
        for x in range(1, 15):
            if y % 3 == 0 or x % 3 == 0:
                continue
            px(img, x, y, (190, 190, 186))
    return img


def laundry():
    """Folded laundry from above: towels, a striped tee, jeans."""
    img = canvas()
    rect(img, 0, 0, 7, 7, (120, 170, 200), .03, 5135); rect(img, 0, 3, 7, 3, (100, 150, 180))
    rect(img, 8, 0, 15, 7, (240, 240, 236), .02, 5136)
    for y in (1, 3, 5):
        rect(img, 8, y, 15, y, (200, 60, 60))
    rect(img, 0, 8, 7, 15, (60, 84, 130), .05, 5137); rect(img, 3, 8, 4, 15, (50, 70, 110))
    rect(img, 8, 8, 15, 15, (236, 200, 210), .03, 5138)
    return img


def clothes():
    """A pile on a bedroom floor, from above: jeans, a grey hoodie, socks, on a cut-out."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            if (x - 7) ** 2 + (y - 8) ** 2 < 40:
                px(img, x, y, (128, 130, 136) if (x * 3 + y) % 7 else (110, 112, 118))
    for y in range(2, 9):
        for x in range(9, 14):
            px(img, x, y, (60, 84, 130))
    px(img, 3, 12, (240, 240, 236)); px(img, 4, 12, (240, 240, 236)); px(img, 12, 13, (220, 80, 90))
    return img


def listings():
    """John's desk: the classifieds with jobs circled in red, a printed résumé, a coffee ring."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (232, 228, 214), .02, 5139)
    for y in range(1, 16, 2):
        for x in range(1, 8):
            px(img, x, y, (150, 148, 140))
    for (cx, cy) in ((3, 4), (5, 11)):
        for a in range(0, 360, 45):
            px(img, round(cx + 2 * math.cos(math.radians(a))), round(cy + 1.4 * math.sin(math.radians(a))), (200, 40, 40))
    rect(img, 9, 1, 15, 15, (248, 248, 246))
    for y in (3, 5, 6, 8, 9, 11, 12):
        for x in range(10, 15):
            px(img, x, y, (120, 120, 120))
    for a in range(0, 360, 40):
        px(img, round(12 + 2.2 * math.cos(math.radians(a))), round(13 + 2.2 * math.sin(math.radians(a))), (150, 110, 70))
    return img


def laptop():
    img = ground((176, 180, 186), 5140, .02)
    rect(img, 1, 1, 14, 9, (60, 62, 68))
    for y in (2, 4, 6, 8):
        for x in range(2, 14, 2):
            px(img, x, y, (90, 94, 100))
    rect(img, 5, 11, 10, 14, (150, 154, 160))
    return img


def laptop_screen():
    """The laptop's screen: a job site's list of openings, one row highlighted."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (40, 42, 48))
    rect(img, 1, 1, 14, 14, (236, 240, 246))
    rect(img, 1, 1, 14, 2, (40, 90, 160))
    for y in (4, 6, 8, 10, 12):
        rect(img, 2, y, 12, y, (140, 150, 170))
    rect(img, 2, 8, 13, 8, (240, 210, 80))
    return img


def homework():
    """Merry's homework: a lined exercise book in a child's big letters, a gold star, crayons alongside."""
    img = canvas()
    rect(img, 0, 0, 10, 15, (248, 248, 244))
    for y in range(2, 16, 2):
        for x in range(0, 11):
            px(img, x, y, (170, 196, 226))
    for y, row in ((3, (1, 2, 4, 5, 6, 8)), (5, (1, 3, 4, 6, 7)), (7, (2, 3, 5, 8, 9)), (9, (1, 2, 3, 6))):
        for x in row:
            px(img, x, y, (70, 70, 90))
    px(img, 8, 12, (240, 196, 40)); px(img, 7, 12, (240, 196, 40)); px(img, 8, 11, (240, 196, 40))
    for i, c in enumerate(((220, 50, 50), (50, 110, 220), (60, 170, 70), (240, 200, 40), (160, 80, 180))):
        rect(img, 12, 1 + i * 3, 15, 2 + i * 3, c)
    return img


def crayons():
    img = canvas()
    rect(img, 0, 0, 15, 15, (210, 70, 70))
    for x, c in enumerate(((220, 50, 50), (50, 110, 220), (60, 170, 70), (240, 200, 40), (160, 80, 180), (240, 140, 40))):
        rect(img, 1 + x * 2 + (x > 2) * 2, 0, 2 + x * 2 + (x > 2) * 2, 3, c)
    return img


def poster_a():
    """Marjorie's poster: a black gig poster, a white jagged band name over a pale skull, a date line."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (20, 20, 22))
    for x in range(2, 14):
        px(img, x, 2 + (x % 3 == 0), (236, 236, 230))
        if x % 2:
            px(img, x, 3 + (x % 4 == 1), (236, 236, 230))
    rect(img, 5, 6, 10, 10, (200, 196, 186)); px(img, 6, 8, (20, 20, 22)); px(img, 9, 8, (20, 20, 22)); rect(img, 7, 10, 8, 11, (200, 196, 186))
    for x in range(3, 13):
        px(img, x, 13, (180, 40, 40))
    return img


def poster_b():
    """Merry's poster: the planets in a row on deep blue, the sun at the edge."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (24, 34, 80))
    for x, y in ((2, 2), (11, 3), (6, 13), (14, 12), (9, 1)):
        px(img, x, y, (240, 240, 230))
    for y in range(16):
        for x in range(3):
            if (x + 1) ** 2 + (y - 8) ** 2 < 30:
                px(img, x, y, (250, 200, 60))
    for cx, r, c in ((5, 0, (180, 160, 140)), (7, 1, (230, 190, 120)), (10, 1, (70, 130, 210)), (12, 0, (200, 90, 60)), (14, 1, (220, 180, 130))):
        for y in range(8 - r, 9 + r):
            for x in range(cx - r, cx + r + 1):
                px(img, x, y, c)
    return img


def drawers():
    img = walnut()
    for y in (5, 10):
        for x in range(16):
            px(img, x, y, (70, 48, 34))
    for y in (2, 7, 12):
        px(img, 7, y, (200, 190, 170)); px(img, 8, y, (200, 190, 170))
    return img


def duvet():
    """A cream duvet with quilting stitches, a fold of the sheet showing at one end."""
    img = ground((232, 228, 220), 5141, .02)
    for y in range(16):
        for x in range(16):
            if (x + y) % 8 == 0 or (x - y) % 8 == 0:
                px(img, x, y, (214, 210, 200))
    return img


def pillow():
    img = ground((246, 246, 242), 5142, .015)
    bevel(img, 0, 0, 15, 15, 1.02, .9)
    return img


def headboard():
    """An upholstered headboard in grey linen with buttoned tufts."""
    img = ground((150, 150, 146), 5143, .04)
    for y in (3, 8, 13):
        for x in range(2 + (y % 2) * 2, 16, 4):
            px(img, x, y, (110, 110, 106))
    return img


def bowl():
    img = porcelain()
    for y in range(16):
        for x in range(16):
            if (x - 7.5) ** 2 / 25 + (y - 8) ** 2 / 36 < 1:
                px(img, x, y, (200, 214, 222) if (x - 7.5) ** 2 / 9 + (y - 8) ** 2 / 16 < 1 else (226, 228, 228))
    return img


def tub_top():
    img = porcelain()
    rect(img, 2, 2, 13, 13, (214, 226, 232))
    return img


def vanity_front():
    img = white()
    for x in range(16):
        px(img, x, 0, (210, 208, 202))
    for x in (7, 8):
        for y in range(1, 16):
            px(img, x, y, (208, 206, 200))
    px(img, 5, 6, (170, 174, 180)); px(img, 10, 6, (170, 174, 180))
    return img


def sink_top():
    img = marble()
    for y in range(16):
        for x in range(16):
            if (x - 7.5) ** 2 / 30 + (y - 7) ** 2 / 20 < 1:
                px(img, x, y, (244, 244, 240) if (x - 7.5) ** 2 / 20 + (y - 7) ** 2 / 12 < 1 else (220, 220, 216))
    px(img, 7, 7, (120, 124, 130))
    return img


def mirror():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mix((196, 210, 218), (150, 166, 178), y / 15))
    for i in range(4):
        px(img, 3 + i, 2 + i, (232, 240, 244)); px(img, 4 + i, 2 + i, (220, 232, 238))
    bevel(img, 0, 0, 15, 15, 1.0, .8)
    return img


def appliance():
    return ground((240, 240, 238), 5144, .01)


def washer():
    """A front-loading washer: the control strip with its dial, the round glass door, a sock against the glass."""
    img = appliance()
    rect(img, 0, 0, 15, 3, (226, 226, 224))
    px(img, 12, 1, (120, 124, 130)); px(img, 13, 1, (120, 124, 130)); px(img, 2, 1, (60, 160, 220)); px(img, 4, 1, (60, 60, 60))
    for y in range(4, 16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 9.5) ** 2) ** .5
            if 4.6 < d < 6.0:
                px(img, x, y, (150, 154, 160))
            elif d <= 4.6:
                px(img, x, y, (60, 70, 82) if (x + y) % 4 else (80, 92, 104))
    px(img, 6, 10, (230, 120, 140)); px(img, 7, 10, (230, 120, 140))
    return img


def dryer():
    img = washer()
    rect(img, 0, 0, 15, 3, (226, 226, 224))
    px(img, 7, 1, (120, 124, 130)); px(img, 8, 1, (120, 124, 130)); px(img, 13, 2, (230, 80, 60))
    for y in range(4, 16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 9.5) ** 2) ** .5
            if d <= 4.6:
                px(img, x, y, (40, 44, 50))
    return img


def cardboard():
    img = ground((186, 146, 98), 5145, .04)
    for x in range(16):
        px(img, x, 7, (206, 196, 170)); px(img, x, 8, (196, 186, 160))
    return img


def box_label():
    """A storage box's side, XMAS in marker on masking tape."""
    img = cardboard()
    rect(img, 3, 10, 12, 13, (232, 222, 190))
    for x, y in ((4, 11), (4, 12), (5, 11), (5, 12), (7, 11), (7, 12), (8, 11), (10, 11), (10, 12), (11, 12)):
        px(img, x, y, (30, 30, 34))
    return img


def heater():
    img = ground((228, 226, 220), 5146, .015)
    for x in range(16):
        px(img, x, 15 - (x % 2), (200, 198, 192))
    return img


def heater_label():
    img = heater()
    rect(img, 4, 4, 11, 10, (250, 230, 120)); rect(img, 5, 5, 10, 5, (200, 40, 40))
    for y in (7, 8, 9):
        for x in range(5, 11):
            px(img, x, y, (90, 90, 90))
    rect(img, 6, 12, 9, 13, (60, 60, 64))
    return img


def shelf_metal():
    img = ground((132, 136, 142), 5147, .04)
    for x in (0, 15):
        for y in range(16):
            px(img, x, y, (96, 100, 106))
    return img


def paint_cans():
    """Garage shelving: paint cans with drips, a coil of hose, the Christmas lights in a tub."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (60, 62, 66))
    for y in (5, 10, 15):
        rect(img, 0, y, 15, y, (150, 154, 160))
    for x0, c in ((1, (230, 230, 226)), (5, (180, 200, 220)), (9, (220, 210, 170))):
        rect(img, x0, 1, x0 + 2, 4, (170, 174, 180)); rect(img, x0, 2, x0 + 2, 3, c)
    rect(img, 1, 6, 6, 9, (50, 120, 60)); rect(img, 2, 7, 5, 8, (40, 100, 50))
    rect(img, 8, 7, 14, 9, (220, 220, 214), .02, 5148)
    for x in (9, 11, 13):
        px(img, x, 7, (220, 50, 50)); px(img, x + 1, 8, (60, 160, 60))
    rect(img, 1, 11, 14, 14, (186, 146, 98))
    return img


# ------------------------------------------------------------------------------------------------ the crew's gear
def cable():
    """A black cable run taped down with strips of silver gaffer tape, on a cut-out."""
    img = canvas()
    for x in range(16):
        px(img, x, 7, (28, 28, 30)); px(img, x, 8, (40, 40, 44))
    for x0 in (2, 10):
        for x in range(x0, x0 + 3):
            for y in range(5, 11):
                px(img, x, y, (176, 178, 180) if y not in (5, 10) else (150, 152, 156))
    return img


def stand():
    img = ground((44, 44, 46), 5149, .05)
    for y in range(0, 16, 4):
        px(img, 7, y, (90, 90, 94))
    return img


def softbox():
    """The softbox's front diffuser: bright white silk in a black border."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            px(img, x, y, (30, 30, 32) if d > 6.5 else mix((255, 255, 250), (232, 232, 226), d / 6.5))
    return img


def softbox_back():
    img = ground((30, 30, 32), 5150, .04)
    for i in range(16):
        px(img, i, i, (50, 50, 54)); px(img, 15 - i, i, (50, 50, 54))
    return img


def canvas_cloth():
    return ground((30, 30, 34), 5151, .05)


def canvas_back():
    """A director's chair back: black canvas with the show's name stencilled in white."""
    img = canvas_cloth()
    for x in range(2, 14):
        if x % 3 != 1:
            px(img, x, 7, (230, 230, 226)); px(img, x, 8, (210, 210, 206))
    return img


def cart():
    img = ground((70, 72, 76), 5152, .04)
    for x in range(16):
        px(img, x, 0, (120, 124, 130)); px(img, x, 15, (120, 124, 130))
    return img


def monitor():
    """A video-village monitor: grainy grey footage of the dinner table, a timecode, a red tally light."""
    rng = random.Random(5153)
    img = canvas()
    rect(img, 0, 0, 15, 15, (24, 24, 26))
    for y in range(2, 13):
        for x in range(1, 15):
            v = 60 + rng.randrange(30) + (40 if 5 <= y <= 9 and 4 <= x <= 11 else 0)
            px(img, x, y, (v, v, v + 6))
    rect(img, 5, 8, 10, 9, (150, 150, 156)); px(img, 6, 5, (40, 40, 44)); px(img, 9, 5, (40, 40, 44))
    for x in range(2, 9):
        px(img, x, 13, (230, 230, 230) if x % 2 else (24, 24, 26))
    px(img, 14, 1, (230, 40, 40))
    return img


def monitor_b():
    """The second monitor: the bedroom upstairs on the other camera, darker, a figure at the bed."""
    rng = random.Random(5154)
    img = canvas()
    rect(img, 0, 0, 15, 15, (24, 24, 26))
    for y in range(2, 13):
        for x in range(1, 15):
            v = 34 + rng.randrange(24)
            px(img, x, y, (v, v + 4, v + 10))
    rect(img, 3, 9, 11, 11, (90, 94, 104)); rect(img, 8, 5, 9, 9, (120, 124, 132))
    for x in range(2, 9):
        px(img, x, 13, (230, 230, 230) if x % 2 else (24, 24, 26))
    return img


def apple_box():
    img = grain((214, 184, 140), 5155, True)
    bevel(img, 0, 0, 15, 15)
    rect(img, 5, 6, 10, 8, (60, 48, 36))
    return img


def sandbag():
    img = ground((214, 110, 40), 5156, .06)
    for x in range(16):
        px(img, x, 7, (30, 30, 30)); px(img, x, 8, (30, 30, 30))
    px(img, 2, 2, (30, 30, 30)); px(img, 13, 13, (30, 30, 30))
    return img


def folding():
    img = ground((176, 172, 162), 5157, .02)
    bevel(img, 0, 0, 15, 15, 1.05, .8)
    return img


def urn():
    img = chrome()
    rect(img, 6, 10, 9, 12, (40, 40, 44))
    return img


def donuts():
    """Craft services: an open box of doughnuts, two gone."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (240, 180, 196))
    for i, (cx, cy) in enumerate(((3.5, 3.5), (8, 3.5), (12.5, 3.5), (3.5, 10), (8, 10), (12.5, 10))):
        if i in (2, 4):
            continue
        c = ((150, 90, 50), (240, 160, 190), (110, 70, 50), (236, 220, 180))[i % 4]
        for y in range(16):
            for x in range(16):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** .5
                if 1 < d < 3:
                    px(img, x, y, c)
    return img


def papers():
    """Call sheets and sides on the folding table, a coffee cup, a pen."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (176, 172, 162))
    rect(img, 1, 1, 9, 14, (250, 250, 246)); rect(img, 3, 0, 11, 12, (244, 244, 238))
    for y in range(2, 12, 2):
        rect(img, 4, y, 10, y, (110, 110, 110))
    rect(img, 4, 2, 7, 2, (30, 30, 30))
    for a in range(0, 360, 45):
        px(img, round(13 + 1.6 * math.cos(math.radians(a))), round(12 + 1.6 * math.sin(math.radians(a))), (240, 240, 236))
    px(img, 13, 12, (90, 60, 40))
    for i in range(5):
        px(img, 11 + i, 4 + i // 2, (30, 60, 160))
    return img


def mesh():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (30, 30, 32) if (x + y) % 2 else (46, 46, 50))
    return img


def ceramic():
    img = ground((60, 90, 110), 5158, .04)
    return img


TEXTURES = {f.__name__: f for f in (
    paint, concrete, drywall, blinds, garage_door, ceiling_plaster, ceiling_light, oak, walnut, white, black, chrome, porcelain, marble,
    mail, coats, shoes, photos, calendar, fridge_front, fridge_top, fridge_side, fabric, cushion, tv_screen, games, shelf_side,
    pot, leaves, basket, laundry, clothes, listings, laptop, laptop_screen, homework, crayons, poster_a, poster_b, drawers,
    duvet, pillow, headboard, bowl, tub_top, vanity_front, sink_top, mirror, appliance, washer, dryer, cardboard, box_label,
    heater, heater_label, shelf_metal, paint_cans, cable, stand, softbox, softbox_back, canvas_cloth, canvas_back, cart,
    monitor, monitor_b, apple_box, sandbag, folding, urn, donuts, papers, mesh, ceramic)}


# ------------------------------------------------------------------------------------------------ models
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')
GLOW = {'blinds': 6, 'ceiling_light': 15, 'softbox': 15, 'monitor': 8, 'monitor_b': 8, 'laptop_screen': 8, 'tv_screen': 0}


def default_uv(frm, to, d):
    (x1, y1, z1), (x2, y2, z2) = frm, to
    return {'down': [x1, 16 - z2, x2, 16 - z1], 'up': [x1, z1, x2, z2], 'north': [16 - x2, 16 - y2, 16 - x1, 16 - y1],
            'south': [x1, 16 - y2, x2, 16 - y1], 'west': [z1, 16 - y2, z2, 16 - y1], 'east': [16 - z2, 16 - y2, 16 - z1, 16 - y1]}[d]


def fit(a, b):
    """Bring one UV span inside the sprite: shifted by whole tiles when it fits, else stretched over the whole sprite."""
    lo, hi = min(a, b), max(a, b)
    if 0 <= lo and hi <= 16:
        return a, b
    if hi - lo <= 16:
        shift = math.floor(lo / 16) * 16
        if hi - shift > 16:
            shift = hi - 16
        return a - shift, b - shift
    return (0, 16) if a <= b else (16, 0)


def box(frm, to, tex, faces=DIRS, uv=None, over=None, rot=None):
    f = {}
    for d in faces:
        face = {'texture': '#' + tex}
        if uv:
            face['uv'] = list(uv)
        f[d] = face
    for d, t in (over or {}).items():
        f[d] = {'texture': '#' + t}
    # Faces of elements that reach past the block get explicit coordinates inside their own sprite.
    if any(c < 0 or c > 16 for c in list(frm) + list(to)):
        for d, face in f.items():
            if 'uv' not in face:
                u0, v0, u1, v1 = default_uv(frm, to, d)
                (u0, u1), (v0, v1) = fit(u0, u1), fit(v0, v1)
                face['uv'] = [round(u0, 3), round(v0, 3), round(u1, 3), round(v1, 3)]
    for face in f.values():
        name = face['texture'][1:]
        if GLOW.get(name):
            face['neoforge_data'] = {'block_light': GLOW[name], 'sky_light': 0}
    e = {'from': list(frm), 'to': list(to), 'faces': f}
    if rot:
        e['rotation'] = rot
    return e


MODEL_SPECS = {}


def spec(name, textures, particle):
    def wrap(fn):
        MODEL_SPECS[name] = (fn, textures, particle)
        return fn
    return wrap


def cube(name, tex):
    spec(name, {'t': tex}, tex)(lambda: [box((0, 0, 0), (16, 16, 16), 't')])


cube('paint', 'paint')
cube('concrete', 'concrete')
cube('drywall', 'drywall')


# Authored facing north: the front toward -z, anything on a wall against +z.
@spec('window', {'b': 'blinds', 'p': 'paint'}, 'blinds')
def m_window():
    return [box((0, 0, 0), (16, 16, 16), 'p', over={'north': 'b', 'south': 'b'})]


@spec('garage_door', {'g': 'garage_door', 'p': 'drywall'}, 'garage_door')
def m_garage_door():
    return [box((0, 0, 0), (16, 16, 16), 'p', over={'north': 'g'})]


# One block between the storeys: the house's wood floor on top, the plastered ceiling underneath.
spec('storey', {'up': 'the_oldest_house:block/ghosts/floor', 'down': 'ceiling_plaster', 'side': 'paint'}, 'paint')(lambda: [])


@spec('ceiling_light', {'l': 'ceiling_light', 'w': 'white'}, 'ceiling_light')
def m_ceiling_light():
    return [box((4, 15, 4), (12, 16, 12), 'w', over={'down': 'l'}), box((5, 14.5, 5), (11, 15, 11), 'l')]


@spec('console', {'o': 'oak', 'm': 'mail', 'c': 'ceramic', 'k': 'black'}, 'oak')
def m_console():
    legs = [box((x, 0, z), (x + 1.5, 11, z + 1.5), 'k') for x in (1, 13.5) for z in (5, 13)]
    return legs + [box((0, 11, 4), (16, 12.5, 15.5), 'o'), box((1, 3, 5), (15, 4, 14.5), 'o'),
                   box((1.5, 12.5, 6), (9.5, 12.8, 13), 'm', faces=('up',), uv=(0, 0, 16, 16)),
                   box((11, 12.5, 7.5), (14, 14, 10.5), 'c'), box((3, 4, 6), (8, 6, 13), 'm', faces=('up', 'north'))]


@spec('coat_hooks', {'o': 'oak', 'c': 'coats', 'k': 'black'}, 'coats')
def m_coat_hooks():
    return [box((0, 13, 14.5), (16, 14.5, 16), 'o'), box((1, 2, 13), (5.5, 13, 15.5), 'c', uv=(0, 0, 6, 16)),
            box((6, 3, 13), (10.5, 13, 15.5), 'c', uv=(6, 0, 11, 16)), box((11, 5, 11.5), (15, 12, 15.5), 'c', uv=(11, 3, 16, 16))]


@spec('shoe_rack', {'o': 'oak', 's': 'shoes'}, 'shoes')
def m_shoe_rack():
    return [box((0, 0, 9), (16, 8, 16), 'o', over={'north': 's'})]


@spec('photos', {'p': 'photos', 'k': 'black'}, 'photos')
def m_photos():
    return [box((0, 2, 15.5), (16, 14, 16), 'k', over={'north': 'p'}, uv=(0, 1, 16, 13))]


@spec('calendar', {'c': 'calendar'}, 'calendar')
def m_calendar():
    return [box((4, 2, 15.6), (12, 14, 16), 'c', uv=(2, 1, 14, 15))]


@spec('fridge', {'f': 'fridge_front', 's': 'fridge_side'}, 'fridge_side')
def m_fridge():
    return [box((1, 0, 2), (15, 16, 16), 's', over={'north': 'f'})]


@spec('fridge_top', {'f': 'fridge_top', 's': 'fridge_side'}, 'fridge_side')
def m_fridge_top():
    return [box((1, 0, 2), (15, 14, 16), 's', over={'north': 'f'}), box((1, 0, 1.5), (15, .5, 2), 's')]


def sofa(arm_low):
    arm_x = (0, 2.5) if arm_low else (13.5, 16)
    return [box((0, 0, 3), (16, 4, 16), 'f'), box((0, 4, 3), (16, 7, 13), 'f', over={'up': 'u'}, uv=(0, 0, 16, 16)),
            box((0, 4, 13), (16, 15, 16), 'f'), box((arm_x[0], 4, 3), (arm_x[1], 10, 13), 'f')]


@spec('sofa_a', {'f': 'fabric', 'u': 'cushion', 'k': 'black'}, 'fabric')
def m_sofa_a():
    return sofa(True)


@spec('sofa_b', {'f': 'fabric', 'u': 'cushion', 'k': 'black'}, 'fabric')
def m_sofa_b():
    return sofa(False)


@spec('tv_stand', {'w': 'walnut', 't': 'tv_screen', 'k': 'black'}, 'walnut')
def m_tv_stand():
    return [box((0, 0, 5), (16, 5, 15), 'w'), box((5, 5, 9), (11, 5.5, 11), 'k'), box((7, 5.5, 9.5), (9, 7, 10.5), 'k'),
            box((-2, 6.5, 9.5), (18, 17, 10.5), 'k', over={'north': 't'}), box((2, 1, 4.9), (7, 3, 5), 'k')]


@spec('bookshelf', {'s': 'shelf_side', 'g': 'games'}, 'shelf_side')
def m_bookshelf():
    return [box((0, 0, 4), (16, 16, 16), 's', over={'north': 'g'})]


@spec('plant', {'p': 'pot', 'l': 'leaves'}, 'leaves')
def m_plant():
    leaf = []
    for angle in (45, -45):
        leaf.append(box((0.5, 6, 8), (15.5, 22, 8), 'l', faces=('north', 'south'), rot={'origin': [8, 8, 8], 'axis': 'y', 'angle': angle}))
    return [box((4.5, 0, 4.5), (11.5, 7, 11.5), 'p')] + leaf


@spec('laundry', {'b': 'basket', 'l': 'laundry'}, 'basket')
def m_laundry():
    return [box((2, 0, 3), (14, 8, 13), 'b'), box((3, 8, 4), (13, 9.5, 12), 'l', over={'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'})]


@spec('clothes', {'c': 'clothes'}, 'clothes')
def m_clothes():
    return [box((0, 0, 0), (16, .6, 16), 'c', faces=('up',))]


@spec('desk', {'o': 'oak', 'w': 'white', 'l': 'listings', 'p': 'laptop', 's': 'laptop_screen'}, 'oak')
def m_desk():
    legs = [box((x, 0, z), (x + 1.5, 11, z + 1.5), 'w') for x in (.5, 14) for z in (2.5, 13)]
    return legs + [box((0, 11, 2), (16, 12, 15), 'o'), box((1, 12, 3), (9, 12.3, 12), 'l', faces=('up',)),
                   box((10, 12, 4), (15, 12.5, 9.5), 'p', faces=('up', 'north', 'east', 'west', 'south')), box((10, 12.5, 9.5), (15, 16, 10), 'p', over={'north': 's'})]


@spec('desk_kid', {'w': 'white', 'h': 'homework', 'c': 'crayons'}, 'white')
def m_desk_kid():
    legs = [box((x, 0, z), (x + 1.5, 10, z + 1.5), 'w') for x in (.5, 14) for z in (3, 13)]
    return legs + [box((0, 10, 3), (16, 11, 15), 'w'), box((10, 4, 3.5), (15, 10, 14.5), 'w'),
                   box((1, 11, 4), (13, 11.3, 13), 'h', faces=('up',)), box((12, 11, 11), (14, 14, 13), 'c')]


@spec('poster_a', {'p': 'poster_a'}, 'poster_a')
def m_poster_a():
    return [box((2, 1, 15.6), (14, 15, 16), 'p')]


@spec('poster_b', {'p': 'poster_b'}, 'poster_b')
def m_poster_b():
    return [box((1, 2, 15.6), (15, 14, 16), 'p')]


@spec('dresser', {'w': 'walnut', 'd': 'drawers', 'p': 'photos', 'k': 'black'}, 'walnut')
def m_dresser():
    return [box((0, 1, 4), (16, 13, 16), 'w', over={'north': 'd'}), box((1, 0, 5), (15, 1, 15), 'k'),
            box((3, 13, 11), (7, 17, 12), 'k', over={'north': 'p'}, uv=(5, 5, 11, 13)), box((9, 13, 8), (13, 14.5, 12), 'w')]


@spec('double_bed', {'w': 'walnut', 'd': 'duvet', 'p': 'pillow', 'h': 'headboard'}, 'duvet')
def m_double_bed():
    return [box((0, 2, 0), (32, 6, 31), 'w'), box((.5, 6, .5), (31.5, 10, 26), 'd'),
            box((.5, 6, 26), (31.5, 9, 31), 'd', over={'up': 'p'}),
            box((2, 9, 26.5), (14.5, 12, 30.5), 'p'), box((17.5, 9, 26.5), (30, 12, 30.5), 'p'),
            box((0, 0, 31), (32, 22, 32), 'h'), box((1, 0, 1), (3, 2, 3), 'w'), box((29, 0, 1), (31, 2, 3), 'w')]


spec('bed_fill', {}, 'duvet')(lambda: [])


@spec('toilet', {'p': 'porcelain', 'b': 'bowl'}, 'porcelain')
def m_toilet():
    return [box((5, 0, 4), (11, 6, 12), 'p'), box((4, 6, 2), (12, 9, 12), 'p', over={'up': 'b'}), box((3, 8, 12), (13, 16, 16), 'p')]


@spec('tub', {'p': 'porcelain', 't': 'tub_top', 'c': 'chrome'}, 'porcelain')
def m_tub():
    return [box((0, 0, 0), (16, 2, 32), 'p', over={'up': 't'}), box((0, 2, 0), (1.5, 9, 32), 'p'), box((14.5, 2, 0), (16, 9, 32), 'p'),
            box((1.5, 2, 0), (14.5, 9, 1.5), 'p'), box((1.5, 2, 30.5), (14.5, 9, 32), 'p'), box((7, 9, 30), (9, 12, 32), 'c'),
            box((7.5, 11, 28), (8.5, 12, 30), 'c')]


@spec('vanity', {'w': 'white', 'v': 'vanity_front', 'm': 'marble', 's': 'sink_top', 'r': 'mirror', 'c': 'chrome'}, 'white')
def m_vanity():
    return [box((1, 0, 5), (15, 12, 16), 'w', over={'north': 'v'}), box((0, 12, 4), (16, 13, 16), 'm', over={'up': 's'}),
            box((2, 17, 15.5), (14, 29, 16), 'r'), box((7, 13, 13), (9, 15.5, 14.5), 'c'), box((7.5, 15, 11), (8.5, 15.5, 14), 'c')]


@spec('washer', {'a': 'appliance', 'f': 'washer'}, 'appliance')
def m_washer():
    return [box((1, 0, 2), (15, 15, 16), 'a', over={'north': 'f'})]


@spec('dryer', {'a': 'appliance', 'f': 'dryer'}, 'appliance')
def m_dryer():
    return [box((1, 0, 2), (15, 15, 16), 'a', over={'north': 'f'})]


@spec('boxes', {'c': 'cardboard', 'l': 'box_label'}, 'cardboard')
def m_boxes():
    return [box((1, 0, 2), (15, 8, 15), 'c', over={'north': 'l'}), box((3, 8, 4), (13, 14, 13), 'c', over={'north': 'l'})]


@spec('water_heater', {'h': 'heater', 'l': 'heater_label', 'c': 'chrome'}, 'heater')
def m_water_heater():
    return [box((3, 0, 3), (13, 16, 13), 'h', over={'north': 'l'}), box((5, 16, 7), (6, 20, 8), 'c'), box((10, 16, 7), (11, 20, 8), 'c')]


@spec('shelves', {'m': 'shelf_metal', 'p': 'paint_cans'}, 'shelf_metal')
def m_shelves():
    return [box((0, 0, 8), (16, 16, 16), 'm', over={'north': 'p'})]


@spec('cable', {'c': 'cable'}, 'cable')
def m_cable():
    return [box((0, 0, 0), (16, .25, 16), 'c', faces=('up',))]


@spec('cable_corner', {'c': 'cable'}, 'cable')
def m_cable_corner():
    north = box((5, .01, 0), (11, .26, 11), 'c', faces=('up',), uv=(0, 5, 11, 11))
    north['faces']['up']['rotation'] = 90
    return [box((5, 0, 5), (16, .25, 11), 'c', faces=('up',), uv=(5, 5, 16, 11)), north]


@spec('softbox', {'s': 'softbox', 'b': 'softbox_back', 'k': 'stand'}, 'softbox_back')
def m_softbox():
    legs = [box((7.5, 0, 7.5), (8.5, 7, 15), 'k', rot={'origin': [8, 0, 8], 'axis': 'y', 'angle': a}) for a in (0, 45, -45)]
    return legs + [box((7.5, 0, 7.5), (8.5, 22, 8.5), 'k'), box((1, 17, 6), (15, 31, 10), 'b', over={'north': 's'}),
                   box((3, 19, 10), (13, 29, 12), 'b')]


@spec('directors_chair', {'o': 'oak', 'c': 'canvas_cloth', 'b': 'canvas_back'}, 'canvas_cloth')
def m_directors_chair():
    posts = [box((x, 0, z), (x + 1.5, 16, z + 1.5), 'o') for x in (2, 12.5) for z in (12,)]
    legs = [box((x, 0, 3), (x + 1.5, 9, 4.5), 'o') for x in (2, 12.5)]
    return posts + legs + [box((2, 8, 3), (14, 9, 13.5), 'c'), box((3.5, 11, 12.5), (12.5, 16, 13.2), 'b'),
                           box((2, 11, 3), (3.5, 12, 13.5), 'o'), box((12.5, 11, 3), (14, 12, 13.5), 'o')]


@spec('village_cart', {'c': 'cart', 'm': 'monitor', 'n': 'monitor_b', 'k': 'black'}, 'cart')
def m_village_cart():
    wheels = [box((x, 0, z), (x + 2, 2, z + 2), 'k') for x in (1, 13) for z in (3, 11)]
    return wheels + [box((1, 2, 3), (15, 3, 13), 'c'), box((1, 9, 3), (15, 10, 13), 'c'), box((1, 3, 12), (15, 9, 13), 'c'),
                     box((.5, 10, 8), (8, 16, 9), 'k', over={'north': 'm'}), box((8, 10, 8), (15.5, 16, 9), 'k', over={'north': 'n'})]


@spec('apple_boxes', {'a': 'apple_box'}, 'apple_box')
def m_apple_boxes():
    return [box((1, 0, 3), (15, 5, 13), 'a'), box((3, 5, 2), (13, 10, 14), 'a')]


@spec('sandbags', {'s': 'sandbag'}, 'sandbag')
def m_sandbags():
    return [box((2, 0, 4), (14, 4, 12), 's'), box((4, 4, 3), (12, 7, 13), 's')]


@spec('craft_table', {'f': 'folding', 'u': 'urn', 'd': 'donuts', 'w': 'white', 'k': 'black'}, 'folding')
def m_craft_table():
    legs = [box((x, 0, z), (x + 1, 11, z + 1), 'k') for x in (1, 14) for z in (3, 12)]
    return legs + [box((0, 11, 2), (16, 12, 14), 'f'), box((1.5, 12, 5), (5.5, 19, 9), 'u'), box((7.5, 12, 4), (15, 13.5, 11), 'w', over={'up': 'd'}),
                   box((6, 12, 10.5), (7, 14, 11.5), 'w')]


@spec('folding_table', {'f': 'folding', 'p': 'papers', 'k': 'black'}, 'folding')
def m_folding_table():
    legs = [box((x, 0, z), (x + 1, 15, z + 1), 'k') for x in (1, 14) for z in (1.5, 13.5)]
    return legs + [box((0, 15, 0), (16, 16, 16), 'f', over={'up': 'p'})]


@spec('office_chair', {'m': 'mesh', 'k': 'black', 'c': 'chrome'}, 'mesh')
def m_office_chair():
    return [box((3, 0, 7.5), (13, 1, 8.5), 'k'), box((7.5, 0, 3), (8.5, 1, 13), 'k'), box((7.4, 1, 7.4), (8.6, 6, 8.6), 'c'),
            box((3, 6, 3), (13, 8, 13), 'm'), box((3, 9, 12.5), (13, 19, 13.5), 'm'), box((7.5, 7, 13), (8.5, 10, 14), 'k')]


KINDS = list(MODEL_SPECS)
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
CUBES = {'paint', 'concrete', 'drywall'}


def model_json(name):
    fn, textures, particle = MODEL_SPECS[name]
    tex = {k: NS + v for k, v in textures.items()}
    tex['particle'] = NS + particle
    if name in CUBES:
        return {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + textures['t']}}
    if name == 'storey':
        up, down, side = (t if ':' in t else NS + t for t in (textures['up'], textures['down'], textures['side']))
        return {'parent': 'minecraft:block/cube', 'textures': {'up': up, 'down': down, 'north': side, 'south': side, 'east': side, 'west': side, 'particle': side}}
    m = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': name in ('window', 'garage_door'),
         'textures': tex, 'elements': fn()}
    return m


def files():
    out = {}
    for name, fn in TEXTURES.items():
        out[TEX / f'{name}.png'] = fn()
    for name in KINDS:
        out[MODELS / f'{name}.json'] = json.dumps(model_json(name), indent=1) + '\n'
    variants = {}
    for facing, y in TURN.items():
        for kind in KINDS:
            v = {'model': NS + kind}
            if y:
                v['y'] = y
            variants[f'facing={facing},kind={kind}'] = v
    out[STATES / 'barrett_home.json'] = json.dumps({'variants': variants}, indent=1) + '\n'
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
    print(f'{"Verified" if args.check else "Drew"} {len(TEXTURES)} Barrett home textures and {len(KINDS)} kinds.')


if __name__ == '__main__':
    main()
