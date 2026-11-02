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
# ------------------------------------------------------------------------------------------------ 0.4.202: the family's own things
# The owner: "far too many repeated pictures... not indicative of a family of four", "the tv should be showing something",
# "blinds should be single columns facing opposite directions", "bathroom is host to a family of four. there is clutter. a cup
# with toothbrushes. punny artwork. a tubshower with curtains. dirty towels and clothes bins", "very little character for a
# young girl's bedroom", "very little character to marjorie's bedroom too", "hallway should end with a closet".
JOHN_HAIR, SARAH_HAIR, MARJ_HAIR, MERRY_HAIR = (58, 40, 30), (150, 70, 38), (44, 30, 24), (176, 128, 70)
SKIN, SKIN_D = (226, 186, 156), (196, 156, 126)


def pic(w, h, border, draw):
    img = canvas()
    for y in range(h):
        for x in range(w):
            px(img, x, y, border)
    draw(img)
    return img


def figure(img, x, top, hair, shirt, tall=5, long_hair=False, beard=False):
    """A person at photo scale: hair, a face, a shirt, from `top` down."""
    px(img, x, top, hair); px(img, x, top + 1, SKIN)
    if beard:
        px(img, x, top + 2, hair)
    if long_hair:
        px(img, x - 1, top + 1, hair); px(img, x + 1, top + 1, hair)
    for y in range(top + (3 if beard else 2), top + tall):
        px(img, x, y, shirt)


def blinds_cell(head):
    """One column of a double window with its blind drawn. The trim runs down the outer edge only, so the two columns (one turned
    to face the other way) meet as one window; the lower column has the sill, the upper the blind's head rail."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            if y % 2 == 1:
                px(img, x, y, (248, 236, 200) if x > 2 else (236, 224, 190))
            else:
                px(img, x, y, mul((228, 226, 220), 1 - .02 * (x % 3 == 0)))
    for y in range(16):
        px(img, 0, y, (238, 236, 230)); px(img, 1, y, (212, 210, 204))
    if head:
        for x in range(16):
            px(img, x, 0, (238, 236, 230)); px(img, x, 1, (214, 210, 200)); px(img, x, 2, (190, 186, 178))
    else:
        for x in range(16):
            px(img, x, 15, (238, 236, 230)); px(img, x, 14, (204, 202, 196))
    for y in range(3 if head else 0, 16 if head else 13):
        px(img, 4, y, (196, 192, 184))
    if not head:
        px(img, 4, 12, (150, 146, 140))
    return img


def tv_cartoon():
    """The living room set is on: a Saturday cartoon, a round yellow critter hopping over a hill under a sun, eight frames."""
    frames = 8
    img = Image.new('RGBA', (16, 16 * frames), (0, 0, 0, 0))
    for f in range(frames):
        oy = 16 * f
        for y in range(16):
            for x in range(16):
                sky = (110, 178, 236) if y < 9 else (96, 176, 84)
                if 9 <= y <= 10 and abs(x - 8) > 6 - (y - 9) * 2:
                    sky = (110, 178, 236)
                img.putpixel((x, oy + y), sky + (255,))
        sx = 12 + (1 if f % 4 < 2 else 0)
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            img.putpixel((sx + dx, oy + 2 + dy), (252, 226, 90, 255))
        hop = (0, 2, 3, 2, 0, 0, 1, 0)[f]
        cx = 3 + f
        for dx in range(3):
            for dy in range(3):
                img.putpixel((min(15, cx + dx), oy + 8 - hop + dy), (250, 200, 60, 255))
        img.putpixel((min(15, cx + 2), oy + 8 - hop), (30, 30, 30, 255))
        for x in range(16):
            img.putpixel((x, oy), (12, 12, 14, 255)); img.putpixel((x, oy + 15), (12, 12, 14, 255))
    return img


def photo_wedding():
    def draw(img):
        for y in range(1, 9):
            for x in range(1, 7):
                px(img, x, y, (132, 138, 152))
        figure(img, 2, 2, SARAH_HAIR, (244, 240, 230), tall=7, long_hair=True)
        px(img, 2, 5, (210, 90, 110)); px(img, 1, 5, (120, 160, 90))
        figure(img, 4, 1, JOHN_HAIR, (40, 40, 48), tall=8)
        px(img, 4, 3, (230, 230, 230))
    return pic(8, 10, (184, 150, 70), draw)


def school(hair, shirt, pigtails):
    def draw(img):
        for y in range(1, 9):
            for x in range(1, 7):
                px(img, x, y, (92, 122, 172) if (x * 3 + y * 5) % 4 else (104, 134, 184))
        for x in (2, 3, 4, 5):
            px(img, x, 2, hair)
        for x in (3, 4):
            px(img, x, 3, SKIN); px(img, x, 4, SKIN)
        px(img, 3, 3, (60, 46, 40)); px(img, 4, 3, (60, 46, 40)); px(img, 3, 5, (200, 120, 120)); px(img, 4, 5, SKIN)
        if pigtails:
            px(img, 1, 4, hair); px(img, 6, 4, hair); px(img, 2, 3, hair); px(img, 5, 3, hair)
        else:
            for y in range(3, 7):
                px(img, 2, y, hair); px(img, 5, y, hair)
        for y in range(6, 9):
            for x in range(2, 6):
                px(img, x, y, shirt)
    return draw


def photo_marjorie():
    return pic(8, 10, (214, 206, 186), school(MARJ_HAIR, (70, 110, 80), False))


def photo_merry():
    return pic(8, 10, (214, 206, 186), school(MERRY_HAIR, (200, 120, 170), True))


def photo_beach():
    """The four of them on the beach at the end of a summer: sea, sand, the girls in front, John and Sarah behind."""
    def draw(img):
        for y in range(1, 9):
            for x in range(1, 11):
                px(img, x, y, (156, 200, 232) if y < 4 else (64, 124, 172) if y == 4 else (228, 210, 156))
        figure(img, 3, 2, JOHN_HAIR, (90, 120, 160), tall=6, beard=True)
        figure(img, 7, 2, SARAH_HAIR, (220, 90, 70), tall=6, long_hair=True)
        figure(img, 4, 5, MARJ_HAIR, (70, 110, 80), tall=3)
        figure(img, 6, 6, MERRY_HAIR, (240, 200, 70), tall=2)
        px(img, 9, 7, (230, 60, 60))
    return pic(12, 10, (240, 236, 226), draw)


def photo_baby():
    """Sarah on the old couch with Merry new in a blanket, and Marjorie, six, leaning in to look."""
    def draw(img):
        for y in range(1, 9):
            for x in range(1, 9):
                px(img, x, y, (206, 190, 160) if y < 6 else (110, 80, 120))
        figure(img, 3, 2, SARAH_HAIR, (120, 150, 190), tall=5, long_hair=True)
        px(img, 4, 4, (246, 240, 226)); px(img, 5, 4, (246, 240, 226)); px(img, 5, 3, SKIN)
        figure(img, 7, 4, MARJ_HAIR, (230, 120, 150), tall=3)
    return pic(10, 10, (240, 236, 226), draw)


def photo_sisters():
    """Marjorie giving Merry a piggyback across the yard, both of them laughing."""
    def draw(img):
        for y in range(1, 7):
            for x in range(1, 9):
                px(img, x, y, (150, 196, 226) if y < 4 else (96, 160, 80))
        figure(img, 4, 2, MARJ_HAIR, (70, 110, 80), tall=5, long_hair=True)
        figure(img, 5, 1, MERRY_HAIR, (240, 200, 70), tall=3)
        px(img, 6, 3, SKIN)
    return pic(10, 8, (240, 236, 226), draw)


def portrait():
    """The studio portrait over the front door, two blocks square: John and Sarah standing, Marjorie and Merry in front."""
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    def p(x, y, c):
        img.putpixel((x, y), clamp(c) + (255,))
    for y in range(32):
        for x in range(32):
            edge = x in (0, 1, 30, 31) or y in (0, 1, 30, 31)
            p(x, y, (120, 82, 44) if edge else mix((96, 106, 128), (70, 76, 96), y / 32))
    def person(cx, top, hair, shirt, h, beard=False, long_hair=False):
        for dx in (-1, 0, 1):
            p(cx + dx, top, hair); p(cx + dx, top + 1, hair if dx else SKIN)
            for y in range(top + 2, top + 4):
                p(cx + dx, y, SKIN)
        p(cx - 1, top + 2, (50, 40, 36)); p(cx + 1, top + 2, (50, 40, 36))
        if beard:
            for dx in (-1, 0, 1):
                p(cx + dx, top + 4, hair)
        if long_hair:
            for y in range(top + 1, top + 6):
                p(cx - 2, y, hair); p(cx + 2, y, hair)
        for y in range(top + 4 + (1 if beard else 0), top + h):
            for dx in (-2, -1, 0, 1, 2):
                p(cx + dx, y, shirt)
    person(11, 5, JOHN_HAIR, (50, 64, 96), 26, beard=True)
    person(20, 6, SARAH_HAIR, (120, 60, 70), 25, long_hair=True)
    person(13, 15, MARJ_HAIR, (60, 100, 76), 15, long_hair=True)
    person(19, 18, MERRY_HAIR, (210, 130, 170), 12)
    return img


def portrait_part(i):
    def make():
        full = portrait()
        x0, y0 = (i % 2) * 16, (i // 2) * 16
        return full.crop((x0, y0, x0 + 16, y0 + 16))
    return make


def crucifix():
    img = canvas()
    for y in range(1, 15):
        for x in (7, 8):
            px(img, x, y, (110, 74, 42) if x == 7 else (92, 60, 34))
    for x in range(3, 13):
        for y in (4, 5):
            px(img, x, y, (110, 74, 42) if y == 4 else (92, 60, 34))
    for x, y in ((7, 4), (8, 4), (7, 5), (8, 5), (7, 6), (8, 6), (7, 7), (8, 8), (7, 9), (5, 4), (10, 4)):
        px(img, x, y, (206, 170, 90))
    return img


def merry_drawing():
    """Merry's crayon drawing taped up: a house, a sun, four of them holding hands, a cat."""
    img = canvas()
    for y in range(1, 15):
        for x in range(1, 15):
            px(img, x, y, (248, 246, 236))
    for x in range(10, 14):
        px(img, x, 2, (250, 210, 40))
    px(img, 12, 1, (250, 210, 40)); px(img, 12, 3, (250, 210, 40))
    for x in range(2, 7):
        px(img, x, 4, (200, 50, 50))
    for y in range(5, 9):
        px(img, 2, y, (200, 50, 50)); px(img, 6, y, (200, 50, 50))
    for i, (x, c) in enumerate(((3, (60, 60, 200)), (6, (200, 60, 120)), (9, (60, 160, 60)), (12, (230, 140, 40)))):
        px(img, x, 9 + (i // 2), (60, 40, 30)); px(img, x, 11, c); px(img, x, 12, c)
    for x in range(3, 13):
        px(img, x, 11, mul(get(img, x, 11), .9)) if get(img, x, 11) == (248, 246, 236) else None
    for x in range(1, 15):
        px(img, x, 14, (60, 170, 60))
    px(img, 1, 1, (232, 226, 190)); px(img, 14, 1, (232, 226, 190))
    return img


FONT = {'A': ['.#.', '#.#', '###', '#.#', '#.#'], 'E': ['###', '#..', '##.', '#..', '###'], 'H': ['#.#', '#.#', '###', '#.#', '#.#'],
        'L': ['#..', '#..', '#..', '#..', '###'], 'O': ['.#.', '#.#', '#.#', '#.#', '.#.'], 'P': ['##.', '#.#', '##.', '#..', '#..'],
        'R': ['##.', '#.#', '##.', '#.#', '#.#'], 'S': ['.##', '#..', '.#.', '..#', '##.'], 'W': ['#.#', '#.#', '#.#', '###', '#.#']}


def lettering(img, x, y, word, c):
    for ch in word:
        for j, row in enumerate(FONT[ch]):
            for i, cell in enumerate(row):
                if cell == '#':
                    img.putpixel((x + i, y + j), clamp(c) + (255,))
        x += 4


def print_soap():
    """SOAP OPERA, in the family bathroom: block letters over bubbles in a white frame. The house is on television now."""
    img = Image.new('RGBA', (32, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(32):
            edge = x in (0, 31) or y in (0, 15)
            img.putpixel((x, y), ((236, 234, 228) if edge else (186, 220, 232)) + (255,))
    lettering(img, 9, 2, 'SOAP', (40, 60, 110))
    lettering(img, 6, 8, 'OPERA', (40, 60, 110))
    for x, y in ((3, 3), (4, 6), (27, 4), (28, 9), (3, 11), (26, 12)):
        img.putpixel((x, y), (255, 255, 255, 255))
        img.putpixel((x + 1, y), (220, 240, 248, 255))
    return img


def print_whale():
    """WHALE HELLO, in the girls' bathroom, a blue whale blowing its spout."""
    img = Image.new('RGBA', (32, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(32):
            edge = x in (0, 31) or y in (0, 15)
            img.putpixel((x, y), ((200, 150, 100) if edge else (244, 240, 226)) + (255,))
    lettering(img, 3, 2, 'WHALE', (40, 80, 140))
    lettering(img, 3, 8, 'HELLO', (40, 80, 140))
    for x in range(23, 30):
        for y in range(8, 12):
            img.putpixel((x, y), (70, 120, 180, 255))
    img.putpixel((22, 9, ), (70, 120, 180, 255)); img.putpixel((21, 8), (70, 120, 180, 255))
    img.putpixel((27, 9), (20, 20, 30, 255))
    for y in (5, 6, 7):
        img.putpixel((25, y), (150, 200, 240, 255))
    img.putpixel((24, 4), (150, 200, 240, 255)); img.putpixel((26, 4), (150, 200, 240, 255))
    return img


def half(fn, right):
    def make():
        im = fn()
        return im.crop((16, 0, 32, 16) if right else (0, 0, 16, 16))
    return make


def curtain_fish():
    """The shower curtain: little fish and bubbles on sea green, the folds shaded."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((124, 196, 186), 1.08 if x % 4 == 1 else .86 if x % 4 == 3 else 1.0))
    for x, y in ((2, 3), (9, 7), (5, 12), (12, 1), (13, 13)):
        px(img, x, y, (246, 160, 70)); px(img, x + 1, y, (246, 160, 70)); px(img, x - 1, y, (230, 120, 50))
    for x, y in ((7, 2), (3, 8), (11, 10)):
        px(img, x, y, (236, 248, 248))
    return img


def tile_floor():
    """Small white hexagonal tile with grey grout, a few gone yellow."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            grout = (y % 4 == 0) or ((x + (2 if (y // 4) % 2 else 0)) % 4 == 0)
            px(img, x, y, (170, 170, 166) if grout else ((236, 232, 214) if (x * 7 + y * 3) % 11 == 0 else (244, 244, 240)))
    return img


def wall_tile():
    """Wainscot tile to waist height: white subway tile, grey grout, a navy band and a cap at the top; paint above."""
    img = paint()
    for y in range(6, 16):
        for x in range(16):
            grout = y % 3 == 0 or (x + (4 if (y // 3) % 2 else 0)) % 8 == 0
            px(img, x, y, (176, 178, 176) if grout else (240, 242, 240))
    for x in range(16):
        px(img, x, 5, (52, 66, 110)); px(img, x, 4, (236, 236, 232))
    return img


def toothbrushes():
    """The family's four toothbrushes in a cup, blue, pink, green and purple; a squeezed tube; the soap pump."""
    img = canvas()
    for x, c in ((2, (60, 110, 210)), (5, (230, 110, 160)), (8, (70, 170, 90)), (11, (150, 90, 200))):
        for y in range(16):
            px(img, x, y, c)
    for x in range(16):
        px(img, x, 15, (240, 240, 236))
    return img


def towels():
    """Bath towels on the bar: a navy one, a striped one, a pink one and a small one with a duck on it."""
    img = canvas()
    cols = [(46, 60, 110), (226, 190, 90), (230, 150, 170), (120, 190, 200)]
    for y in range(16):
        for x in range(16):
            c = cols[x // 4]
            if x // 4 == 1 and y % 4 < 2:
                c = (240, 236, 220)
            px(img, x, y, mul(c, .9 if y % 3 == 2 else 1.0))
    px(img, 13, 10, (250, 220, 60)); px(img, 14, 10, (250, 220, 60)); px(img, 14, 9, (250, 220, 60))
    return img


def towel_heap():
    """A wet towel dropped on the floor with a sock and somebody's jeans."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((46, 60, 110), .9 if (x + y) % 5 == 0 else 1.0))
    for y in range(9, 16):
        for x in range(0, 6):
            px(img, x, y, (70, 96, 150))
    for x, y in ((11, 3), (12, 3), (12, 4), (13, 4)):
        px(img, x, y, (240, 240, 236))
    return img


def sorter():
    """The laundry sorter: three canvas bags on a frame, lights, darks and colours, all of them full."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = ((226, 222, 210), (70, 70, 80), (190, 110, 90))[min(2, x // 6)]
            if x in (5, 11) or y in (0, 1):
                c = (180, 180, 184)
            px(img, x, y, mul(c, .92 if y % 4 == 3 else 1.0))
    return img


def bath_mat():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((130, 170, 200), .9 if (x + y) % 2 else 1.0))
    for x in range(16):
        px(img, x, 0, (240, 240, 236)); px(img, x, 15, (240, 240, 236))
    return img


def coloring():
    """Merry's colouring book open on the coffee table, a horse half coloured purple, crayons beside it; the remote; a mug."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (248, 246, 238))
    for x, y in ((4, 5), (5, 5), (6, 5), (6, 4), (7, 4), (4, 6), (6, 6), (3, 4)):
        px(img, x, y, (150, 90, 200))
    for y in range(16):
        px(img, 8, y, (210, 206, 196))
    for x, y in ((10, 6), (11, 6), (12, 7), (11, 8), (10, 8)):
        px(img, x, y, (60, 60, 60))
    return img


def lamp_shade():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((244, 226, 186), 1.06 if x % 3 == 0 else 1.0))
    return img


def toys():
    """Merry's things on the rug: a grey stuffed rabbit, letter blocks, a plastic pony with a purple mane."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (170, 170, 176) if y < 6 else (220, 60, 60) if y < 9 else (60, 120, 210) if y < 12 else (240, 200, 220))
    for x in range(16):
        px(img, x, 12, (150, 90, 200))
    return img


def dust_sheet():
    """A dust sheet thrown over the family's den sofa and TV when the crew moved in, gaffer tape holding a corner."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((226, 222, 210), 1.05 if (x + y // 3) % 5 == 0 else .94 if (x - y) % 7 == 0 else 1.0))
    for x in range(5, 11):
        px(img, x, 13, (90, 90, 96)); px(img, x, 14, (70, 70, 76))
    return img


def schedule():
    """The crew's whiteboard: seven days in marker, crossed off as they go, a call time circled in red."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (244, 246, 246) if 0 < x < 15 and 0 < y < 13 else (190, 192, 196))
    for i in range(7):
        y = 2 + i * 1 + (i // 4)
        for x in range(2, 6 + (i % 3)):
            px(img, x, y if y < 12 else 11, (40, 60, 140))
    for x in range(9, 13):
        px(img, x, 3, (200, 50, 50)); px(img, x, 6, (200, 50, 50))
    px(img, 9, 4, (200, 50, 50)); px(img, 9, 5, (200, 50, 50)); px(img, 12, 4, (200, 50, 50)); px(img, 12, 5, (200, 50, 50))
    for x in range(2, 14):
        px(img, x, 14, (150, 150, 156))
    return img


def cork():
    """The kitchen cork board: the chore chart with stars, a permission slip, the parish bulletin, a spelling test with an A."""
    rng = random.Random(5170)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((186, 140, 92), 1 + rng.uniform(-.08, .08)) if 0 < x < 15 and 0 < y < 15 else (120, 84, 50))
    for y in range(2, 8):
        for x in range(2, 8):
            px(img, x, y, (250, 250, 244))
    for y in (3, 5, 7):
        for x in range(3, 7):
            px(img, x, y, (120, 140, 190))
    px(img, 6, 3, (240, 200, 40)); px(img, 6, 5, (240, 200, 40))
    for y in range(3, 9):
        for x in range(9, 14):
            px(img, x, y, (236, 220, 170))
    for y in range(10, 14):
        for x in range(3, 9):
            px(img, x, y, (250, 250, 244))
    px(img, 7, 11, (220, 40, 40)); px(img, 8, 11, (220, 40, 40))
    for y in range(10, 14):
        for x in range(10, 14):
            px(img, x, y, (190, 220, 240))
    return img


def backpacks():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (40, 40, 46) if x < 8 else (170, 110, 200))
    for x in range(16):
        px(img, x, 2, (70, 70, 76) if x < 8 else (210, 150, 230))
    px(img, 4, 8, (200, 60, 60)); px(img, 12, 7, (250, 230, 90)); px(img, 11, 9, (100, 200, 120))
    return img


def fruit():
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (220, 60, 50) if (x // 4 + y // 4) % 3 == 0 else (240, 200, 50) if (x // 4 + y // 4) % 3 == 1 else (240, 140, 40)
            px(img, x, y, c)
    return img


def fruit_bowl():
    """0.4.242 (owner: "try to make those actually look like fruit"): a red apple, a green apple with its leaf, an orange with its
    dimpled skin, a banana going brown at the ends, and the stems."""
    img = canvas()
    def fill(x0, y0, x1, y1, c):
        for y in range(y0, y1):
            for x in range(x0, x1):
                px(img, x, y, c)
    fill(0, 0, 4, 4, (196, 40, 36)); fill(2, 2, 4, 4, (150, 28, 26)); px(img, 1, 1, (236, 110, 96)); px(img, 3, 1, (176, 34, 30))
    fill(4, 0, 8, 4, (130, 186, 60)); fill(6, 2, 8, 4, (94, 140, 40)); px(img, 5, 1, (190, 226, 120)); px(img, 7, 0, (110, 166, 50))
    fill(8, 0, 12, 4, (236, 140, 30))
    for x, y in ((8, 1), (10, 0), (11, 2), (9, 3), (10, 2)):
        px(img, x, y, (210, 116, 20))
    px(img, 9, 1, (250, 190, 90))
    fill(0, 4, 8, 6, (238, 206, 70)); fill(0, 5, 8, 6, (210, 176, 50)); px(img, 0, 4, (110, 80, 40)); px(img, 0, 5, (110, 80, 40)); px(img, 7, 4, (90, 70, 40)); px(img, 7, 5, (90, 70, 40))
    fill(0, 6, 8, 8, (240, 212, 80)); fill(1, 7, 7, 8, (226, 194, 62)); px(img, 0, 6, (110, 80, 40)); px(img, 7, 6, (90, 70, 40)); px(img, 3, 6, (196, 150, 60))
    fill(12, 0, 13, 2, (90, 60, 30)); fill(13, 0, 15, 1, (80, 140, 50))
    return img


def stuffed():
    """Merry's stuffed animals: a brown bear, a white lamb, a green frog."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (150, 100, 60) if x < 6 else (240, 240, 232) if x < 11 else (100, 180, 90))
    px(img, 2, 4, (30, 20, 20)); px(img, 4, 4, (30, 20, 20)); px(img, 8, 4, (30, 20, 20)); px(img, 13, 3, (30, 20, 20))
    return img


def stars():
    """Glow-in-the-dark stars stuck on Merry's ceiling."""
    img = canvas()
    for x, y in ((2, 3), (10, 2), (6, 8), (13, 11), (3, 13), (9, 13)):
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
            if 0 <= x + dx < 16 and 0 <= y + dy < 16:
                px(img, x + dx, y + dy, (210, 250, 200))
    return img


def drawings():
    """Three of Merry's drawings taped in a row: the cardboard house, her sister, a monster under a bed with big feet."""
    img = canvas()
    for y in range(2, 14):
        for x in range(16):
            if x % 5 != 4:
                px(img, x, y, (248, 246, 236))
    for x in range(1, 4):
        px(img, x, 6, (170, 120, 60)); px(img, x, 9, (170, 120, 60))
    px(img, 2, 5, (200, 50, 50))
    px(img, 7, 5, MARJ_HAIR); px(img, 7, 6, SKIN); px(img, 7, 7, (70, 110, 80)); px(img, 7, 8, (70, 110, 80))
    for x in range(10, 14):
        px(img, x, 10, (40, 40, 40))
    px(img, 11, 7, (40, 40, 40)); px(img, 12, 8, (200, 30, 30))
    return img


def fairy():
    """Marjorie's string of fairy lights, warm white bulbs on a thin green wire."""
    img = canvas()
    for x in range(16):
        y = 3 + int(1.5 * math.sin(x / 2.5))
        px(img, x, y, (60, 90, 50))
        if x % 3 == 1:
            px(img, x, y + 1, (255, 236, 170)); px(img, x, y + 2, (255, 220, 140))
    return img


def band_poster():
    """A band poster, half the faces scratched out in pen; a torn corner."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (24, 24, 30))
    for x in range(2, 14):
        px(img, x, 2, (220, 40, 60)); px(img, x, 3, (220, 40, 60))
    for i, x in enumerate((3, 6, 9, 12)):
        px(img, x, 7, (220, 200, 180)); px(img, x, 8, (60, 60, 70)); px(img, x, 9, (60, 60, 70))
        if i % 2:
            px(img, x, 7, (10, 10, 10)); px(img, x - 1, 6, (10, 10, 10)); px(img, x + 1, 8, (10, 10, 10))
    for x in range(2, 14):
        px(img, x, 12, (200, 200, 200)) if x % 2 else None
    px(img, 15, 15, (0, 0, 0)); px(img, 14, 15, (0, 0, 0)); px(img, 15, 14, (0, 0, 0))
    return img


def pinboard():
    """Marjorie's pinboard: photo-booth strips with her friends, two concert ticket stubs, a pressed flower."""
    rng = random.Random(5171)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((176, 132, 90), 1 + rng.uniform(-.08, .08)) if 0 < x < 15 and 0 < y < 15 else (40, 30, 26))
    for y in range(2, 13):
        for x in (3, 4):
            px(img, x, y, (240, 240, 236) if y % 3 else (60, 60, 64))
    for y in range(3, 6):
        for x in range(7, 13):
            px(img, x, y, (230, 80, 90))
    for y in range(8, 11):
        for x in range(8, 13):
            px(img, x, y, (90, 180, 220))
    px(img, 11, 12, (200, 60, 140)); px(img, 11, 13, (70, 130, 70))
    return img


def guitar():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (170, 96, 42) if y > 5 else (60, 40, 26))
    for x, y in ((7, 10), (8, 10), (7, 11), (8, 11)):
        px(img, x, y, (30, 20, 16))
    return img


def boombox():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (54, 56, 62))
    for cx in (4, 11):
        for y in range(5, 12):
            for x in range(cx - 3, cx + 4):
                if (x - cx) ** 2 + (y - 8.5) ** 2 < 9:
                    px(img, x, y, (24, 24, 28) if (x + y) % 2 else (40, 40, 44))
    for x in range(6, 10):
        px(img, x, 2, (180, 220, 240))
    return img


def linen():
    """Folded towels and sheets on the closet shelves, the board games on top, the vacuum's hose."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            shelf = y in (5, 10, 15)
            c = (140, 104, 66) if shelf else ((240, 236, 226) if (x // 4) % 2 else (150, 186, 214)) if y < 5 else \
                ((230, 150, 170) if x < 8 else (240, 230, 200)) if y < 10 else ((200, 60, 60) if x < 5 else (60, 120, 200) if x < 10 else (240, 210, 80))
            px(img, x, y, mul(c, .9 if (y % 5 == 4 and not shelf) else 1.0))
    return img


def nightstand_top():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (108, 70, 44))
    return img


def bible():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (40, 30, 26))
    for x in range(16):
        px(img, x, 0, (200, 170, 90)); px(img, x, 15, (240, 236, 224))
    px(img, 8, 6, (200, 170, 90)); px(img, 8, 7, (200, 170, 90)); px(img, 7, 7, (200, 170, 90)); px(img, 9, 7, (200, 170, 90)); px(img, 8, 8, (200, 170, 90))
    return img


def paperback():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (200, 70, 90))
    for x in range(3, 13):
        px(img, x, 4, (250, 240, 220)); px(img, x, 6, (250, 240, 220))
    return img


def plaster_crack():
    """0.4.202 (owner: "crack looks out of place with the rest"): the crack day three opens, in the house's own paint: a thin dark
    line down the wall with a few flakes of paint off, the plaster greyer where they came away."""
    img = paint()
    path = [(9, 0), (9, 1), (8, 2), (8, 3), (7, 4), (7, 5), (8, 6), (8, 7), (9, 8), (9, 9), (8, 10), (7, 11), (7, 12), (6, 13), (6, 14), (5, 15)]
    for x, y in path:
        px(img, x, y, (92, 84, 74))
        px(img, x + 1, y, (176, 168, 156))
    for x, y in ((10, 4), (11, 5), (5, 9), (6, 9), (4, 12)):
        px(img, x, y, (184, 178, 168))
    px(img, 10, 3, (120, 112, 102)); px(img, 6, 8, (120, 112, 102))
    return img


def case_shell():
    """0.4.202 (owner: "the camera case texture also doesn't carry the same style as the rest of the set"): the crew's hard case
    as the house draws its things: matte black plastic, a moulded rib, two latches and a handle."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (44, 46, 50) if (x + y) % 7 else (50, 52, 56))
    for x in range(16):
        px(img, x, 7, (30, 30, 34)); px(img, x, 8, (64, 66, 70))
    for x in (3, 4, 11, 12):
        for y in (6, 7, 8, 9):
            px(img, x, y, (170, 172, 176) if y in (7, 8) else (120, 122, 126))
    bevel(img, 0, 0, 15, 15, 1.2, .7)
    return img


def case_label():
    """The case's lid: black, with the production's paper label taped on and written over in marker."""
    img = case_shell()
    for y in range(3, 7):
        for x in range(3, 13):
            px(img, x, y, (232, 222, 190))
    for x in (4, 5, 7, 8, 9, 11):
        px(img, x, 4, (40, 40, 60))
    for x in (4, 6, 7, 10):
        px(img, x, 5, (40, 40, 60))
    return img


TEXTURES.update({
    'blinds_low': lambda: blinds_cell(False), 'blinds_head': lambda: blinds_cell(True), 'tv_cartoon': tv_cartoon,
    'photo_wedding': photo_wedding, 'photo_marjorie': photo_marjorie, 'photo_merry': photo_merry, 'photo_beach': photo_beach,
    'photo_baby': photo_baby, 'photo_sisters': photo_sisters, 'portrait_tl': portrait_part(0), 'portrait_tr': portrait_part(1),
    'portrait_bl': portrait_part(2), 'portrait_br': portrait_part(3), 'crucifix': crucifix, 'merry_drawing': merry_drawing,
    'print_soap_l': half(print_soap, False), 'print_soap_r': half(print_soap, True), 'print_whale_l': half(print_whale, False),
    'print_whale_r': half(print_whale, True), 'curtain_fish': curtain_fish, 'tile_floor': tile_floor, 'wall_tile': wall_tile,
    'toothbrushes': toothbrushes, 'towels': towels, 'towel_heap': towel_heap, 'sorter': sorter, 'bath_mat': bath_mat,
    'coloring': coloring, 'lamp_shade': lamp_shade, 'toys': toys, 'dust_sheet': dust_sheet, 'schedule': schedule, 'cork': cork,
    'backpacks': backpacks, 'fruit': fruit, 'fruit_bowl': fruit_bowl, 'stuffed': stuffed, 'stars': stars, 'drawings': drawings, 'fairy': fairy,
    'band_poster': band_poster, 'pinboard': pinboard, 'guitar': guitar, 'boombox': boombox, 'linen': linen,
    'nightstand_top': nightstand_top, 'bible': bible, 'paperback': paperback, 'plaster_crack': plaster_crack,
    'case_shell': case_shell, 'case_label': case_label})
GLOW.update({'tv_cartoon': 9, 'fairy': 10, 'stars': 6, 'lamp_shade': 12, 'blinds_low': 6, 'blinds_head': 6})


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
    # 0.4.201: an overriding texture keeps the box's explicit UVs; dropping them sampled the photos' transparent margin, so
    # the dresser's framed photograph showed only its black edges.
    for d, t in (over or {}).items():
        f[d] = {'texture': '#' + t}
        if uv:
            f[d]['uv'] = list(uv)
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


@spec('tv_stand', {'w': 'walnut', 't': 'tv_cartoon', 'k': 'black'}, 'walnut')
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


@spec('dresser', {'w': 'walnut', 'd': 'drawers', 'm': 'ceramic', 'k': 'black'}, 'walnut')
def m_dresser():
    # 0.4.202 (owner: "far too many repeated pictures"): no framed photograph on every dresser; a little dish for whatever is in
    # the pockets, and a wooden box.
    return [box((0, 1, 4), (16, 13, 16), 'w', over={'north': 'd'}), box((1, 0, 5), (15, 1, 15), 'k'),
            box((3, 13, 9), (7, 13.8, 13), 'm'), box((9, 13, 8), (13, 14.5, 12), 'w')]


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
    # 0.4.201: the lower box fills the cell, so a row of these meets, and the upper box sits on its lid.
    return [box((0, 0, 0), (16, 8, 16), 'c', over={'north': 'l'}), box((2, 8, 3), (14, 15, 13), 'c', over={'north': 'l'})]


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




# ------------------------------------------------------------------------------------------------ 0.4.202 models
def mirrored(tex, over_tex):
    """A cube whose room face shows the texture as drawn and whose other face shows it mirrored: turned to face the other way,
    the same block is the window's other column."""
    e = box((0, 0, 0), (16, 16, 16), tex, over={'north': over_tex, 'south': over_tex})
    e['faces']['north']['uv'] = [0, 0, 16, 16]
    e['faces']['south']['uv'] = [16, 0, 0, 16]
    return e


@spec('window', {'b': 'blinds_low', 'p': 'paint', 'w': 'white'}, 'blinds_low')
def m_window():
    return [mirrored('p', 'b'), box((0, 0, -1.5), (16, 1, 0), 'w'), box((0, 0, 16), (16, 1, 17.5), 'w')]


@spec('window_head', {'b': 'blinds_head', 'p': 'paint', 'w': 'white'}, 'blinds_head')
def m_window_head():
    return [mirrored('p', 'b'), box((0, 14, -1), (16, 16, 0), 'w'), box((0, 14, 16), (16, 16, 17), 'w')]


def framed(name, w, h, y0=None):
    def make():
        x0 = 8 - w // 2
        y = (9 - h // 2) if y0 is None else y0
        return [box((x0, y, 15), (x0 + w, y + h, 16), 'p', faces=('north', 'east', 'west', 'up', 'down'), uv=(0, 0, w, h))]
    MODEL_SPECS[name] = (make, {'p': name}, name)


for _n, _w, _h in (('photo_wedding', 8, 10), ('photo_marjorie', 8, 10), ('photo_merry', 8, 10), ('photo_beach', 12, 10),
                   ('photo_baby', 10, 10), ('photo_sisters', 10, 8)):
    framed(_n, _w, _h)


@spec('portrait', {'tl': 'portrait_tl', 'tr': 'portrait_tr', 'bl': 'portrait_bl', 'br': 'portrait_br', 'k': 'walnut'}, 'portrait_tl')
def m_portrait():
    """Two blocks square, hung centred over the door: the canvas's left half on the +x side, as a north face reads it."""
    def part(x0, y0, t):
        e = box((x0, y0, 15), (x0 + 16, y0 + 16, 16), 'k', over={'north': t})
        e['faces']['north']['uv'] = [0, 0, 16, 16]
        return e
    return [part(8, 16, 'tl'), part(-8, 16, 'tr'), part(8, 0, 'bl'), part(-8, 0, 'br')]


@spec('crucifix', {'c': 'crucifix'}, 'crucifix')
def m_crucifix():
    return [box((3, 1, 15.4), (13, 15, 16), 'c', faces=('north',), uv=(3, 1, 13, 15))]


@spec('merry_drawing', {'d': 'merry_drawing'}, 'merry_drawing')
def m_merry_drawing():
    return [box((1, 1, 15.6), (15, 15, 16), 'd', faces=('north',), uv=(1, 1, 15, 15))]


def two_wide(name, y0):
    def make():
        a = box((8, y0, 15.4), (24, y0 + 16, 16), 'l', faces=('north',)); a['faces']['north']['uv'] = [0, 0, 16, 16]
        b = box((-8, y0, 15.4), (8, y0 + 16, 16), 'r', faces=('north',)); b['faces']['north']['uv'] = [0, 0, 16, 16]
        return [a, b]
    MODEL_SPECS[name] = (make, {'l': name + '_l', 'r': name + '_r'}, name + '_l')


two_wide('print_soap', 0)
two_wide('print_whale', 0)


@spec('tub_shower', {'p': 'porcelain', 't': 'tub_top', 'c': 'chrome', 'f': 'curtain_fish', 'b': 'toothbrushes', 'y': 'fruit'}, 'porcelain')
def m_tub_shower():
    """The tub-shower: the tub against the wall on its +x side, the curtain on the open side drawn most of the way, the rod, the
    shower head over the taps, shampoo on the rim and the girls' rubber duck."""
    tub = [box((0, 0, 0), (16, 2, 32), 'p', over={'up': 't'}), box((0, 2, 0), (1.5, 9, 32), 'p'), box((14.5, 2, 0), (16, 9, 32), 'p'),
           box((1.5, 2, 0), (14.5, 9, 1.5), 'p'), box((1.5, 2, 30.5), (14.5, 9, 32), 'p'), box((7, 9, 30), (9, 12, 32), 'c'),
           box((7.5, 11, 28), (8.5, 12, 30), 'c')]
    curtain = [box((.2, 9, 9), (.8, 31, 32), 'f', faces=('east', 'west')), box((0, 9, 5), (1.6, 31, 9), 'f'),
               box((0, 31, 0), (.6, 31.6, 32), 'c'), box((7.5, 26, 30.5), (8.5, 27, 32), 'c'), box((7, 24.5, 29), (9, 26, 30.5), 'c')]
    rim = [box((14.6, 9, 3), (15.6, 12.5, 4.5), 'b', uv=(2, 2, 3, 6)), box((14.6, 9, 5), (15.6, 11.5, 6.5), 'b', uv=(5, 2, 6, 6)),
           box((14.6, 9, 7), (15.6, 12, 8.5), 'b', uv=(11, 2, 12, 6)), box((12, 9, 27.5), (13.5, 10.5, 29), 'y', uv=(4, 4, 6, 6)),
           box((12.2, 10.5, 28.6), (13.2, 11.5, 29.4), 'y', uv=(4, 4, 6, 6))]
    return tub + curtain + rim


def vanity_base():
    return [box((1, 0, 5), (15, 12, 16), 'w', over={'north': 'v'}), box((0, 12, 4), (16, 13, 16), 'm', over={'up': 's'}),
            box((2, 17, 15.5), (14, 29, 16), 'r'), box((7, 13, 13), (9, 15.5, 14.5), 'c'), box((7.5, 15, 11), (8.5, 15.5, 14), 'c')]


@spec('vanity_family', {'w': 'white', 'v': 'vanity_front', 'm': 'marble', 's': 'sink_top', 'r': 'mirror', 'c': 'chrome', 'b': 'toothbrushes', 'p': 'porcelain'}, 'white')
def m_vanity_family():
    """The family's vanity: four toothbrushes in the cup, the soap pump, a squeezed tube, a hairbrush left on the edge."""
    brushes = [box((11 + i * .7, 15, 11.2), (11.4 + i * .7, 19, 11.6), 'b', uv=(2 + i * 3, 0, 3 + i * 3, 16)) for i in range(4)]
    return vanity_base() + [box((10.5, 13, 10.5), (14, 15.5, 12.5), 'p')] + brushes + [
        box((2.5, 13, 11), (4.5, 16, 13), 'p'), box((3, 16, 11.5), (4, 17, 12.5), 'c'), box((2, 13, 6), (6, 13.6, 7.5), 'b', uv=(2, 0, 3, 4)),
        box((10, 13, 5.5), (14, 14, 7), 'b', uv=(8, 0, 9, 4))]


@spec('vanity_kids', {'w': 'white', 'v': 'vanity_front', 'm': 'marble', 's': 'sink_top', 'r': 'mirror', 'c': 'chrome', 'b': 'toothbrushes', 'p': 'porcelain', 'y': 'fruit'}, 'white')
def m_vanity_kids():
    """The girls' vanity: two toothbrushes, the detangling spray, hair ties round the tap, the duck that lives on the edge."""
    brushes = [box((11 + i * .9, 15, 11.2), (11.4 + i * .9, 19, 11.6), 'b', uv=(5 + i * 6, 0, 6 + i * 6, 16)) for i in range(2)]
    return vanity_base() + [box((10.5, 13, 10.5), (13.5, 15.5, 12.5), 'p')] + brushes + [
        box((2.5, 13, 10.5), (4, 17, 12), 'b', uv=(5, 2, 6, 6)), box((2.7, 17, 10.7), (3.8, 18, 11.8), 'p'),
        box((6.6, 13.2, 12.6), (9.4, 13.8, 13.4), 'b', uv=(11, 0, 12, 2)), box((12, 13, 5.5), (13.5, 14.5, 7), 'y', uv=(4, 4, 6, 6))]


@spec('step_stool', {'w': 'white', 'b': 'bath_mat'}, 'white')
def m_step_stool():
    return [box((3, 0, 4), (13, 4, 12), 'w'), box((3.5, 4, 4.5), (12.5, 4.5, 11.5), 'b')]


@spec('hamper', {'s': 'sorter', 'c': 'chrome', 'l': 'clothes'}, 'sorter')
def m_hamper():
    return [box((1, 0, 4), (15, 12, 13), 's'), box((1.5, 12, 4.5), (14.5, 13.5, 12.5), 'l'), box((4, 13.5, 6), (8, 15, 10), 'l'),
            box((.5, 12, 3.5), (15.5, 12.5, 4), 'c'), box((.5, 12, 13), (15.5, 12.5, 13.5), 'c')]


@spec('towel_bar', {'t': 'towels', 'c': 'chrome'}, 'towels')
def m_towel_bar():
    hang = [box((1.5 + i * 3.4, 1 + (i % 2), 14.6), (4.5 + i * 3.4, 11, 15.4), 't', uv=(i * 4, 0, i * 4 + 4, 10)) for i in range(4)]
    return [box((0.5, 10.5, 15), (15.5, 11.5, 16), 'c'), box((0.5, 10, 15.5), (1.5, 12, 16), 'c'), box((14.5, 10, 15.5), (15.5, 12, 16), 'c')] + hang


@spec('dirty_towels', {'t': 'towel_heap'}, 'towel_heap')
def m_dirty_towels():
    return [box((1, 0, 2), (13, 1.5, 13), 't'), box((3, 1.5, 4), (10, 3, 10), 't', uv=(2, 2, 9, 8)), box((9, 0, 11), (15, 1, 15), 't', uv=(0, 9, 6, 13))]


@spec('bath_mat', {'b': 'bath_mat'}, 'bath_mat')
def m_bath_mat():
    return [box((1, 0, 2), (15, .5, 14), 'b')]


spec('storey_tile', {'up': 'tile_floor', 'down': 'ceiling_plaster', 'side': 'paint'}, 'tile_floor')(lambda: [])


@spec('bath_wall', {'t': 'wall_tile', 'p': 'paint'}, 'wall_tile')
def m_bath_wall():
    return [box((0, 0, 0), (16, 16, 16), 'p', over={'north': 't'})]


@spec('coffee_table', {'w': 'walnut', 'c': 'coloring', 'k': 'black', 'm': 'ceramic', 'y': 'fruit'}, 'walnut')
def m_coffee_table():
    """The coffee table in front of the TV: Merry's colouring book open on it with the crayons, the remote, John's mug."""
    legs = [box((x, 0, z), (x + 1.5, 6, z + 1.5), 'w') for x in (1, 13.5) for z in (3, 11.5)]
    return legs + [box((0, 6, 2), (16, 8, 14), 'w'), box((2, 8, 4), (10, 8.3, 10), 'c', faces=('up',)),
                   box((2.5, 8, 10.5), (5, 8.6, 11), 'y', uv=(0, 0, 2, 1)), box((5.5, 8, 10.5), (8, 8.6, 11), 'y', uv=(8, 8, 10, 9)),
                   box((11, 8, 5), (12.5, 8.8, 9), 'k'), box((11.5, 8, 10), (14, 11, 12.5), 'm')]


@spec('floor_lamp', {'c': 'chrome', 'l': 'lamp_shade', 'k': 'black'}, 'lamp_shade')
def m_floor_lamp():
    return [box((5, 0, 5), (11, 1, 11), 'k'), box((7.5, 1, 7.5), (8.5, 24, 8.5), 'c'),
            box((4, 24, 4), (12, 30, 12), 'l', faces=('north', 'south', 'east', 'west', 'down'))]


@spec('toys', {'t': 'toys'}, 'toys')
def m_toys():
    return [box((2, 0, 3), (6, 4, 6), 't', uv=(0, 0, 4, 4)), box((2.5, 4, 3.5), (5.5, 7, 5.5), 't', uv=(0, 0, 3, 3)),
            box((2.6, 7, 4), (3.4, 9.5, 4.6), 't', uv=(0, 0, 1, 2)), box((4.6, 7, 4), (5.4, 9.5, 4.6), 't', uv=(0, 0, 1, 2)),
            box((8, 0, 4), (10, 2, 6), 't', uv=(0, 6, 2, 8)), box((10.5, 0, 5), (12.5, 2, 7), 't', uv=(0, 9, 2, 11)),
            box((9, 2, 4.5), (11, 4, 6.5), 't', uv=(4, 6, 6, 8)), box((6, 0, 10), (12, 4, 12), 't', uv=(0, 12, 6, 15)),
            box((10, 4, 10), (12, 7, 12), 't', uv=(0, 12, 2, 15))]


@spec('covered', {'d': 'dust_sheet'}, 'dust_sheet')
def m_covered():
    return [box((0, 0, 1), (16, 9, 16), 'd'), box((0, 9, 9), (16, 15, 16), 'd'), box((1, 15, 11), (15, 16.5, 15), 'd')]


@spec('schedule_board', {'s': 'schedule', 'c': 'chrome'}, 'schedule')
def m_schedule_board():
    return [box((1, 2, 15), (15, 15, 16), 'c', over={'north': 's'}), box((2, 2, 14), (14, 3, 15), 'c')]


@spec('cork_board', {'c': 'cork'}, 'cork')
def m_cork_board():
    return [box((1, 1, 15.2), (15, 15, 16), 'c')]


@spec('backpacks', {'b': 'backpacks', 'o': 'oak'}, 'backpacks')
def m_backpacks():
    return [box((0, 11, 15), (16, 12.5, 16), 'o'), box((1, 2, 12), (7, 11, 15.5), 'b', uv=(0, 2, 6, 11)), box((9, 1, 12.5), (15, 10, 15.5), 'b', uv=(9, 2, 15, 11))]


@spec('fruit_bowl', {'m': 'ceramic', 'f': 'fruit_bowl'}, 'fruit_bowl')
def m_fruit_bowl():
    """0.4.242: two apples with stems (and a leaf), an orange, and a banana laid across them, each from its own part of fruit_bowl."""
    banana = box((6, 5, 7), (11.5, 6.2, 8.2), 'f', uv=(0, 4, 5.5, 5.2), rot={'origin': [8.75, 5.6, 7.6], 'axis': 'y', 'angle': 22.5})
    for d in ('up', 'down'):
        banana['faces'][d]['uv'] = [0, 6, 5.5, 7.2]
    return [box((4, 0, 4), (12, 2, 12), 'm'),
            box((4.5, 2, 4.5), (7.5, 5, 7.5), 'f', uv=(0, 0, 3, 3)), box((5.75, 5, 5.75), (6.25, 5.8, 6.25), 'f', uv=(12, 0, 12.5, 1.6)),
            box((8.5, 2, 4.6), (11.5, 5, 7.6), 'f', uv=(4, 0, 7, 3)), box((9.75, 5, 5.85), (10.25, 5.6, 6.35), 'f', uv=(12, 0, 12.5, 1.2)),
            box((10.25, 5.3, 5.85), (11.25, 5.4, 6.6), 'f', uv=(13, 0, 15, 1)),
            box((5, 2, 8.4), (8.2, 5.2, 11.6), 'f', uv=(8, 0, 11.2, 3.2)),
            banana]


@spec('stuffed', {'s': 'stuffed'}, 'stuffed')
def m_stuffed():
    return [box((1, 0, 5), (6, 5, 10), 's', uv=(0, 6, 5, 11)), box((2, 5, 6), (5, 8, 9), 's', uv=(1, 2, 4, 5)),
            box((7, 0, 4), (11, 4, 9), 's', uv=(6, 6, 10, 11)), box((7.5, 4, 4.5), (10.5, 6.5, 7), 's', uv=(7, 2, 10, 5)),
            box((11, 0, 9), (15, 3, 13), 's', uv=(11, 6, 15, 10)), box((11.5, 3, 9.5), (14.5, 5, 12), 's', uv=(11, 2, 14, 4))]


@spec('ceiling_stars', {'s': 'stars'}, 'stars')
def m_ceiling_stars():
    return [box((0, 15.8, 0), (16, 16, 16), 's', faces=('down',))]


@spec('drawings', {'d': 'drawings'}, 'drawings')
def m_drawings():
    return [box((0, 2, 15.6), (16, 14, 16), 'd', faces=('north',), uv=(0, 2, 16, 14))]


@spec('fairy_lights', {'f': 'fairy'}, 'fairy')
def m_fairy_lights():
    return [box((0, 9, 15.6), (16, 16, 16), 'f', faces=('north',), uv=(0, 0, 16, 7))]


@spec('poster_c', {'p': 'band_poster'}, 'band_poster')
def m_poster_c():
    return [box((2, 1, 15.6), (14, 15, 16), 'p')]


@spec('pinboard', {'p': 'pinboard'}, 'pinboard')
def m_pinboard():
    return [box((1, 1, 15.2), (15, 15, 16), 'p')]


@spec('guitar', {'g': 'guitar', 'k': 'black'}, 'guitar')
def m_guitar():
    rot = {'origin': [8, 0, 14], 'axis': 'x', 'angle': 22.5}
    return [dict(box((4, 0, 11.5), (12, 9, 14), 'g', uv=(4, 6, 12, 15)), rotation=rot), dict(box((7, 9, 12), (9, 24, 13), 'g', uv=(7, 0, 9, 6)), rotation=rot),
            dict(box((6.5, 24, 11.8), (9.5, 27, 13.2), 'k'), rotation=rot)]


@spec('boombox', {'b': 'boombox', 'k': 'black', 'c': 'chrome', 'p': 'paperback'}, 'boombox')
def m_boombox():
    return [box((2, 0, 6), (14, 7, 10), 'k', over={'north': 'b'}), box((4, 7, 7.5), (12, 8, 8.5), 'c'),
            box((1, 0, 11), (4.5, 3, 14.5), 'p'), box((1.2, 3, 11.2), (4.3, 3.6, 14.3), 'k')]


@spec('linen_shelves', {'s': 'shelf_side', 'l': 'linen'}, 'linen')
def m_linen_shelves():
    return [box((0, 0, 7), (16, 16, 16), 's', over={'north': 'l'})]


def nightstand():
    return [box((1, 0, 3), (15, 11, 14), 'w', over={'north': 'd'}), box((0.5, 11, 2.5), (15.5, 12, 14.5), 'w', over={'up': 'n'})]


@spec('nightstand_john', {'w': 'walnut', 'd': 'drawers', 'n': 'nightstand_top', 'b': 'bible', 'c': 'chrome', 'k': 'black'}, 'walnut')
def m_nightstand_john():
    """John's side: the Bible he reads now, a rosary on it, the alarm clock set for the job interviews."""
    return nightstand() + [box((3, 12, 5), (9, 13.5, 13), 'b'), box((4, 13.5, 7), (8, 13.7, 11), 'k'),
                           box((10.5, 12, 8), (14, 15, 10), 'k', over={'north': 'c'})]


@spec('nightstand_sarah', {'w': 'walnut', 'd': 'drawers', 'n': 'nightstand_top', 'p': 'paperback', 'k': 'black', 'g': 'white'}, 'walnut')
def m_nightstand_sarah():
    """Sarah's side: a paperback face down where she stopped, her reading glasses folded on it, a glass of water."""
    return nightstand() + [box((3, 12, 5), (9, 12.8, 12), 'p'), box((4, 12.8, 7), (8, 13.2, 8), 'k'), box((11, 12, 8), (13.5, 16, 10.5), 'g')]


# ------------------------------------------------------------------------------------------------ 0.4.220: the owner's walk-through
# "duplicate clothes", "more repeated clutter", "a kitchen with no appliances", "barry should be facing the monitor. something
# should be on the monitor", "add a unique rug to mary's room": a second coat rail with its own coats, a sideboard instead of
# two dressers, a kitchen with its stove, hood, wall cabinets, microwave, toaster, coffee maker and dishwasher, the crew's field
# monitor showing the dinner table, and a rug of each room's own.
def coats_b():
    """The second rail: John's tan trench coat with its belt, Merry's yellow raincoat with the hood up on the hook, a striped scarf."""
    img = canvas()
    rect(img, 0, 0, 5, 15, (176, 146, 102), .05, 5301)
    for x in range(6):
        px(img, x, 8, (132, 104, 70))
    px(img, 2, 8, (210, 190, 140)); px(img, 1, 0, (140, 112, 76)); px(img, 4, 0, (140, 112, 76))
    for y in (3, 6, 11):
        px(img, 1, y, (110, 86, 58))
    rect(img, 6, 1, 10, 13, (236, 196, 56), .05, 5302)
    rect(img, 7, 1, 9, 3, (214, 174, 42)); px(img, 8, 2, (60, 52, 40))
    for y in (6, 9):
        px(img, 8, y, (250, 250, 250))
    for x in range(6, 11):
        px(img, x, 13, (196, 156, 36))
    for y in range(0, 16):
        c = (178, 44, 52) if (y // 2) % 2 == 0 else (232, 224, 206)
        px(img, 12, y, c); px(img, 13, y, mul(c, .9))
    for y in (14, 15):
        px(img, 11, y, (232, 224, 206)); px(img, 14, y, (178, 44, 52))
    return img


def sideboard_drawers():
    """The sideboard's left half: two drawers over a cupboard, brass pulls, the walnut's grain across them."""
    img = walnut()
    rect(img, 0, 0, 15, 15, (98, 68, 46), .03, 5303)
    for (y0, y1) in ((1, 4), (6, 14)):
        rect(img, 1, y0, 14, y1, (112, 78, 54), .03, 5304 + y0); bevel(img, 1, y0, 14, y1)
    rect(img, 6, 2, 9, 2, (200, 168, 92)); rect(img, 12, 9, 12, 11, (200, 168, 92))
    return img


def sideboard_doors():
    """The right half: two cupboard doors with raised panels and brass knobs."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (98, 68, 46), .03, 5306)
    for x0 in (1, 8):
        rect(img, x0, 1, x0 + 6, 14, (112, 78, 54), .03, 5307 + x0); bevel(img, x0, 1, x0 + 6, 14)
        rect(img, x0 + 2, 3, x0 + 4, 12, (106, 74, 50)); bevel(img, x0 + 2, 3, x0 + 4, 12, .86, 1.12)
    px(img, 6, 8, (204, 172, 96)); px(img, 9, 8, (204, 172, 96))
    return img


def range_top():
    """The stove's top: four black coil burners on white enamel, the big ones at the front, a chrome trim round the edge."""
    img = ground((238, 236, 230), 5310, .012)
    for x in range(16):
        px(img, x, 0, (180, 184, 190)); px(img, x, 15, (180, 184, 190))
    for y in range(16):
        px(img, 0, y, (180, 184, 190)); px(img, 15, y, (180, 184, 190))
    for cx, cy, r in ((4.5, 4.5, 2.6), (11.5, 4.5, 2.2), (4.5, 11, 3.0), (11.5, 11, 2.6)):
        for y in range(16):
            for x in range(16):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** .5
                if d <= r:
                    ring = abs(d - r * .66) < .55 or d > r - .6
                    px(img, x, y, (30, 30, 32) if ring else (74, 74, 78))
    return img


def range_front():
    """The oven door: a dark window with the rack's shadow in it, the bar handle, the drawer beneath."""
    img = ground((236, 234, 228), 5311, .012)
    rect(img, 1, 2, 14, 2, (186, 190, 196)); rect(img, 1, 1, 14, 1, (220, 224, 230))
    rect(img, 3, 4, 12, 9, (28, 28, 32)); bevel(img, 3, 4, 12, 9, 1.3, .9)
    for x in range(4, 12):
        px(img, x, 7, (52, 50, 48))
    rect(img, 1, 12, 14, 12, (200, 198, 192)); rect(img, 6, 13, 9, 13, (170, 174, 180))
    return img


def range_back():
    """The stove's backguard: four knobs and the little clock between them."""
    img = ground((236, 234, 228), 5312, .012)
    for x in (2, 4, 11, 13):
        px(img, x, 7, (40, 40, 42)); px(img, x, 8, (70, 70, 74))
    rect(img, 6, 6, 9, 9, (30, 32, 30)); px(img, 7, 7, (120, 220, 120)); px(img, 8, 8, (120, 220, 120))
    return img


def hood():
    """The range hood: brushed steel, a seam where the vent goes up into the cabinet."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mix((196, 200, 206), (150, 156, 164), (y % 4) / 6))
    for x in range(16):
        px(img, x, 15, (120, 126, 134))
    return img


def hood_light():
    """The hood from below: the grease filter and the little bulb that is always on."""
    img = ground((90, 94, 100), 5313, .04)
    for y in range(1, 15, 2):
        for x in range(1, 15):
            px(img, x, y, (60, 64, 70))
    rect(img, 6, 6, 9, 9, (255, 236, 180))
    return img


def cabinet_front():
    """An upper cabinet: two white shaker doors, a brass knob on each where they meet."""
    img = ground((238, 236, 228), 5314, .012)
    for x0 in (0, 8):
        rect(img, x0, 0, x0 + 7, 15, (236, 234, 226)); bevel(img, x0, 0, x0 + 7, 15, 1.04, .82)
        rect(img, x0 + 2, 2, x0 + 5, 13, (230, 228, 220)); bevel(img, x0 + 2, 2, x0 + 5, 13, .9, 1.04)
    px(img, 6, 12, (196, 164, 90)); px(img, 9, 12, (196, 164, 90))
    return img


def microwave_front():
    """The microwave: its dark door, a plate's shadow inside, the keypad and the green clock."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (210, 210, 206), .02, 5315)
    rect(img, 1, 1, 10, 14, (24, 24, 28)); bevel(img, 1, 1, 10, 14, 1.4, .9)
    for x in range(3, 9):
        px(img, x, 11, (44, 44, 48))
    rect(img, 12, 2, 14, 3, (24, 30, 24)); px(img, 12, 2, (110, 220, 110)); px(img, 14, 2, (110, 220, 110))
    for y in range(5, 13, 2):
        for x in (12, 14):
            px(img, x, y, (150, 150, 150))
    return img


def toaster():
    """A chrome toaster: the two slots on top, the lever, a crumb or two."""
    img = chrome()
    for y in (4, 11):
        rect(img, 3, y, 12, y + 1, (40, 40, 42))
    return img


def coffee_front():
    """The coffee maker: the black body, its warming plate, the glass carafe a third full."""
    img = canvas()
    rect(img, 0, 0, 15, 15, (34, 34, 36), .04, 5316)
    rect(img, 2, 6, 13, 14, (170, 190, 196)); bevel(img, 2, 6, 13, 14, 1.2, .8)
    rect(img, 3, 11, 12, 13, (84, 48, 28))
    rect(img, 1, 15, 14, 15, (60, 60, 64)); px(img, 12, 2, (220, 50, 40))
    return img


def dishwasher_front():
    """The dishwasher: a stainless door with its handle bar and the control strip along the top."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mix((200, 204, 208), (170, 174, 180), ((x * 3 + y) % 7) / 7))
    rect(img, 0, 0, 15, 2, (60, 62, 66)); px(img, 3, 1, (110, 220, 110)); px(img, 12, 1, (200, 200, 200)); px(img, 13, 1, (200, 200, 200))
    rect(img, 2, 4, 13, 4, (236, 238, 242)); rect(img, 2, 5, 13, 5, (130, 134, 140))
    return img


def feed():
    """The crew's field monitor: the camera's live picture of the dinner table from the corner, the family at their plates,
    the safe-area lines, a red REC light and the running timecode."""
    rng = random.Random(5317)
    img = canvas()
    for y in range(16):
        for x in range(16):
            v = rng.randrange(6)
            px(img, x, y, (54 + v, 46 + v, 40 + v))
    rect(img, 3, 7, 12, 10, (126, 96, 66)); rect(img, 4, 8, 11, 9, (150, 116, 80))
    for x, hair, shirt in ((4, (60, 40, 30), (70, 90, 130)), (7, (150, 70, 38), (180, 90, 90)), (10, (44, 30, 24), (60, 60, 64))):
        px(img, x, 4, hair); px(img, x, 5, (214, 176, 148)); px(img, x, 6, shirt); px(img, x + 1, 6, shirt)
    px(img, 6, 11, (176, 128, 70)); px(img, 6, 12, (214, 176, 148)); px(img, 6, 13, (120, 78, 160))
    for x in range(1, 15):
        if x % 3:
            px(img, x, 1, (220, 220, 220), 140); px(img, x, 14, (220, 220, 220), 140)
    px(img, 1, 1, (230, 40, 40)); px(img, 2, 1, (230, 40, 40))
    for x in range(9, 15):
        px(img, x, 15, (230, 230, 230) if x % 2 else (40, 40, 40))
    return img


def rug_runner():
    """The hall runner, one half of its width: burgundy wool, a cream and gold border down its outer edge, lozenges down its middle."""
    img = ground((112, 30, 36), 5318, .05)
    for y in range(16):
        px(img, 0, y, (226, 210, 170)); px(img, 1, y, (196, 150, 70)); px(img, 2, y, (226, 210, 170))
        px(img, 3, y, (90, 22, 28))
    for y in range(16):
        k = abs((y % 8) - 4)
        for x in range(16):
            if abs(x - 11) == k:
                px(img, x, y, (206, 168, 92))
            elif abs(x - 11) < k:
                px(img, x, y, (62, 70, 102))
    return img


def rug_runner_narrow():
    """The landing's runner, one block wide: a border down both edges, lozenges between."""
    img = ground((60, 70, 100), 5319, .05)
    for y in range(16):
        for x, c in ((0, (210, 196, 160)), (1, (150, 46, 44)), (14, (150, 46, 44)), (15, (210, 196, 160))):
            px(img, x, y, c)
    for y in range(16):
        k = abs((y % 8) - 4)
        for x in range(3, 13):
            if abs(x - 7.5) <= k - .5 and abs(x - 7.5) > k - 1.5:
                px(img, x, y, (206, 180, 120))
    return img


def rug_living():
    """The living room's rug: a faded rust and cream lattice on navy, worn paler where the sofa's feet go."""
    img = ground((44, 54, 84), 5320, .05)
    for y in range(16):
        for x in range(16):
            u, v = (x + y) % 8, (x - y) % 8
            if u == 0 or v == 0:
                px(img, x, y, (206, 190, 156))
            elif u == 4 and v == 4:
                px(img, x, y, (170, 84, 52))
            elif (u in (3, 5)) and (v in (3, 5)):
                px(img, x, y, (140, 70, 46))
    return img


def merry_rug():
    """Merry's own rug, two blocks by two: a round night sky, navy with a fringe, a fat crescent moon, ringed Saturn, red Mars and
    stars, to go with the planets on her wall and the glow stars on her ceiling. Returns the whole 32-pixel picture."""
    rng = random.Random(5321)
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    def put(x, y, c, a=255):
        if 0 <= x < 32 and 0 <= y < 32:
            img.putpixel((x, y), clamp(c) + (a,))
    for y in range(32):
        for x in range(32):
            d = ((x - 15.5) ** 2 + (y - 15.5) ** 2) ** .5
            if d <= 15.6:
                fringe = d > 14.4
                put(x, y, (226, 214, 180) if fringe and (x + y) % 2 else (196, 182, 146) if fringe else mul((34, 42, 92), 1 + rng.uniform(-.06, .06)))
    for y in range(32):
        for x in range(32):
            d1 = ((x - 11) ** 2 + (y - 10) ** 2) ** .5; d2 = ((x - 13.5) ** 2 + (y - 8.5) ** 2) ** .5
            if d1 <= 4.6 and d2 > 4.2:
                put(x, y, (246, 216, 96))
    for y in range(32):
        for x in range(32):
            if ((x - 21) ** 2 + (y - 19) ** 2) ** .5 <= 2.6:
                put(x, y, (226, 150, 70) if (y - 19) % 2 else (240, 176, 96))
    for x in range(16, 27):
        if abs(x - 21) > 1:
            put(x, 19 + (1 if x < 21 else 0) - (1 if x > 23 else 0), (220, 200, 150))
    for y in range(32):
        for x in range(32):
            if ((x - 9) ** 2 + (y - 23) ** 2) ** .5 <= 1.6:
                put(x, y, (200, 70, 54))
    for x, y in ((20, 7), (25, 11), (7, 16), (15, 26), (24, 25), (17, 13), (5, 11), (12, 19), (27, 16), (19, 23)):
        put(x, y, (250, 246, 220))
    for x, y in ((20, 7), (7, 16), (24, 25)):
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            put(x + dx, y + dy, (236, 222, 150))
    return img


def merry_rug_part(i):
    """One quarter of Merry's rug: 0 north-west, 1 north-east, 2 south-east, 3 south-west."""
    x, y = ((0, 0), (16, 0), (16, 16), (0, 16))[i]
    return merry_rug().crop((x, y, x + 16, y + 16))


def rug_shag():
    """Marjorie's shag rug: deep plum pile, tufts lighter and darker where it has been walked on and sat on."""
    rng = random.Random(5322)
    img = ground((92, 46, 96), 5322, .08)
    for _ in range(70):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, mul((92, 46, 96), rng.choice((.7, .8, 1.25, 1.35))))
    return img


TEXTURES.update({f.__name__: f for f in (coats_b, sideboard_drawers, sideboard_doors, range_top, range_front, range_back, hood,
                                         hood_light, cabinet_front, microwave_front, toaster, coffee_front, dishwasher_front, feed,
                                         rug_runner, rug_runner_narrow, rug_living, rug_shag)})
TEXTURES.update({f'merry_rug_{i}': (lambda i=i: merry_rug_part(i)) for i in range(4)})
GLOW.update({'feed': 8, 'hood_light': 10})


@spec('coat_hooks_b', {'o': 'oak', 'c': 'coats_b'}, 'coats_b')
def m_coat_hooks_b():
    return [box((0, 13, 14.5), (16, 14.5, 16), 'o'), box((1, 1, 13), (5.5, 13, 15.5), 'c', uv=(0, 0, 6, 16)),
            box((6, 3, 12.5), (10.5, 13, 15.5), 'c', uv=(6, 1, 11, 14)), box((11.5, 0, 14), (14.5, 13, 15.5), 'c', uv=(11, 0, 15, 16))]


@spec('sideboard_l', {'w': 'walnut', 'f': 'sideboard_drawers', 'k': 'black', 'c': 'ceramic', 'l': 'lamp_shade'}, 'walnut')
def m_sideboard_l():
    """The sideboard's left half, against the wall: two drawers over a cupboard; on top, the table lamp."""
    return [box((0, 1, 3), (16, 13, 16), 'w', over={'north': 'f'}), box((1, 0, 4), (15, 1, 15), 'k'),
            box((5, 13, 8), (9, 14, 12), 'c'), box((6.5, 14, 9.5), (7.5, 20, 10.5), 'c'),
            box((3.5, 20, 6.5), (10.5, 25, 13.5), 'l', faces=('north', 'south', 'east', 'west', 'down'))]


@spec('sideboard_r', {'w': 'walnut', 'f': 'sideboard_doors', 'k': 'black', 'c': 'ceramic', 'g': 'leaves', 'm': 'white'}, 'walnut')
def m_sideboard_r():
    """The right half: two cupboard doors; on top, a vase of flowers from the garden and the good placemats in a stack."""
    return [box((0, 1, 3), (16, 13, 16), 'w', over={'north': 'f'}), box((1, 0, 4), (15, 1, 15), 'k'),
            box((9.5, 13, 8), (12.5, 19, 11), 'c'), box((8, 18, 9.5), (14, 24, 9.5), 'g', faces=('north', 'south')),
            box((2, 13, 6), (7, 14.2, 12), 'm')]


@spec('range', {'t': 'range_top', 'f': 'range_front', 'b': 'range_back', 'w': 'white', 'k': 'black'}, 'range_front')
def m_range():
    return [box((0, 1, 1), (16, 15, 16), 'w', over={'north': 'f', 'up': 't'}), box((1, 0, 2), (15, 1, 15), 'k'),
            box((0, 15, 14), (16, 19, 16), 'w', over={'north': 'b'})]


@spec('range_hood', {'h': 'hood', 'l': 'hood_light'}, 'hood')
def m_range_hood():
    return [box((0, -2, 7), (16, 3, 16), 'h', over={'down': 'l'}), box((4, 3, 10), (12, 16, 16), 'h')]


@spec('wall_cabinet', {'w': 'white', 'f': 'cabinet_front'}, 'cabinet_front')
def m_wall_cabinet():
    return [box((0, -4, 9), (16, 14, 16), 'w', over={'north': 'f'}, uv=(0, 0, 16, 16))]


@spec('microwave', {'f': 'microwave_front', 'w': 'white', 'k': 'black'}, 'microwave_front')
def m_microwave():
    return [box((2, .5, 5), (15, 8.5, 14), 'w', over={'north': 'f'}, uv=(0, 0, 16, 16)), box((3, 0, 6), (14, .5, 13), 'k')]


@spec('toaster', {'t': 'toaster', 'k': 'black', 'c': 'ceramic'}, 'toaster')
def m_toaster():
    return [box((3, 0, 7), (10, 6, 12), 't'), box((10, 3, 9), (10.6, 4, 10), 'k'),
            box((11.5, 0, 6), (14.5, 4, 9), 'c')]


@spec('coffee_maker', {'f': 'coffee_front', 'k': 'black'}, 'coffee_front')
def m_coffee_maker():
    return [box((4, 0, 6), (12, 11, 14), 'k', over={'north': 'f'}, uv=(0, 0, 16, 16))]


@spec('dishwasher', {'f': 'dishwasher_front', 'w': 'white', 't': 'the_oldest_house:block/ghosts/formica'}, 'dishwasher_front')
def m_dishwasher():
    return [box((0, 0, 1), (16, 14, 16), 'w', over={'north': 'f'}, uv=(0, 0, 16, 16)), box((0, 14, 0), (16, 16, 16), 't')]


@spec('field_monitor', {'s': 'feed', 'k': 'black'}, 'feed')
def m_field_monitor():
    """The crew's field monitor on its stand, turned to the director's chairs, showing the camera's live picture of the table."""
    return [box((5, 0, 8), (11, 1, 13), 'k'), box((7.5, 1, 10), (8.5, 4, 11), 'k'),
            box((1, 4, 9), (15, 14, 11), 'k', over={'north': 's'}, uv=(0, 0, 16, 16))]


def rug(name, tex):
    spec(name, {'r': tex}, tex)(lambda: [box((0, 0, 0), (16, .5, 16), 'r', faces=('up', 'north', 'south', 'east', 'west'), uv=(0, 0, 16, 16))])


rug('rug_runner', 'rug_runner')
rug('rug_runner_narrow', 'rug_runner_narrow')
rug('rug_living', 'rug_living')
rug('rug_shag', 'rug_shag')


@spec('rug_runner_cable', {'r': 'rug_runner', 'c': 'cable'}, 'rug_runner')
def m_rug_runner_cable():
    """0.4.242 (owner: "camera tracks breaking rug"): the runner, and the crew's taped cable lying across it."""
    return [box((0, 0, 0), (16, .5, 16), 'r', faces=('up', 'north', 'south', 'east', 'west'), uv=(0, 0, 16, 16)),
            box((0, .5, 0), (16, .75, 16), 'c', faces=('up',))]
# Merry's rug: one kind, its quarter chosen by the block's facing (see files()); each quarter is drawn unturned.
for _i, _q in enumerate(('nw', 'ne', 'se', 'sw')):
    spec(f'merry_rug_{_q}', {'r': f'merry_rug_{_i}'}, f'merry_rug_{_i}')(lambda: [box((0, 0, 0), (16, .5, 16), 'r', faces=('up', 'north', 'south', 'east', 'west'), uv=(0, 0, 16, 16))])


@spec('case_closed', {'s': 'case_shell', 'l': 'case_label', 'k': 'black'}, 'case_shell')
def m_case_closed():
    return [box((1, 0, 1), (15, 10, 15), 's', over={'up': 'l'}), box((6, 10, 6), (10, 11.5, 7), 'k'), box((6, 10, 9), (10, 11.5, 10), 'k'),
            box((6, 11.5, 6), (10, 12.5, 10), 'k')]


@spec('case_open', {'s': 'case_shell', 'l': 'case_label', 'k': 'black', 'f': 'black'}, 'case_shell')
def m_case_open():
    return [box((1, 0, 1), (15, 8, 15), 's', over={'up': 'f'}), box((1, 8, 14), (15, 21, 15.5), 's', over={'north': 'l'})]


MERRY_QUARTERS = {'north': 'merry_rug_nw', 'east': 'merry_rug_ne', 'south': 'merry_rug_se', 'west': 'merry_rug_sw'}
KINDS = [k for k in MODEL_SPECS if k not in ('case_closed', 'case_open') and k not in MERRY_QUARTERS.values()]
MODELLED = KINDS + list(MERRY_QUARTERS.values())
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
CUBES = {'paint', 'concrete', 'drywall'}


def model_json(name):
    fn, textures, particle = MODEL_SPECS[name]
    tex = {k: v if ':' in v else NS + v for k, v in textures.items()}
    tex['particle'] = NS + particle
    if name in CUBES:
        return {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + textures['t']}}
    if name in ('storey', 'storey_tile'):
        up, down, side = (t if ':' in t else NS + t for t in (textures['up'], textures['down'], textures['side']))
        return {'parent': 'minecraft:block/cube', 'textures': {'up': up, 'down': down, 'north': side, 'south': side, 'east': side, 'west': side, 'particle': side}}
    m = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': name in ('window', 'garage_door'),
         'textures': tex, 'elements': fn()}
    return m


def files():
    out = {}
    for name, fn in TEXTURES.items():
        out[TEX / f'{name}.png'] = fn()
    for name in MODELLED:
        out[MODELS / f'{name}.json'] = json.dumps(model_json(name), indent=1) + '\n'
    out[TEX / 'tv_cartoon.png.mcmeta'] = json.dumps({'animation': {'frametime': 4, 'interpolate': False}}, indent=2) + '\n'
    # 0.4.202: the crew's case and the day-three crack, in this house's style (their blocks belong to the set's older art).
    for name, target in (('case_closed', 'ghosts_case_closed'), ('case_open', 'ghosts_case_open')):
        out[MODELS.parent / f'{target}.json'] = json.dumps(model_json(name), indent=1) + '\n'
    out[MODELS.parent / 'ghosts_plaster.json'] = json.dumps({'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
        'textures': {'p': NS + 'paint', 'c': NS + 'plaster_crack', 'particle': NS + 'paint'},
        'elements': [box((0, 0, 0), (16, 16, 16), 'p', over={'north': 'c'})]}, indent=1) + '\n'
    variants = {}
    for facing, y in TURN.items():
        for kind in KINDS:
            v = {'model': NS + kind}
            if y:
                v['y'] = y
            variants[f'facing={facing},kind={kind}'] = v
        variants[f'facing={facing},kind=merry_rug'] = {'model': NS + MERRY_QUARTERS[facing]}
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
