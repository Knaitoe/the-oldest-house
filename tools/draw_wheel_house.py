#!/usr/bin/env python3
"""The Wheel as a life (0.4.121): vanilla-style 16x16 textures, the life_stage block's models and states, and the
house's material blocks.

The numbered rooms are stages of the daughter's life: the nursery and the childhood room are built at twice the size a
grown reader expects (the reader is child-sized in them), the teenage room, the first flat and the hospital room at
ordinary size, and Mother's room at the hub. Each texture is drawn pixel by pixel from a small palette with one-pixel
bevels and scattered noise, as vanilla's are; nothing is sampled. Lamps, stars and screens glow by their own light.
Run with --check to confirm the committed files.
"""
import argparse
import json
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
TEX = ROOT / 'textures/block/life_stage'
MODELS = ROOT / 'models/block/life_stage'
STATES = ROOT / 'blockstates'
NS = 'the_oldest_house:block/life_stage/'


def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


def mix(a, b, t):
    return clamp(tuple(a[i] + (b[i] - a[i]) * t for i in range(3)))


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


def ground(c, seed, noise=.03, img=None):
    rng = random.Random(seed)
    img = img or canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(c, 1 + rng.uniform(-noise, noise)))
    return img


def bevel(img, x0, y0, x1, y1, light=1.12, dark=.78):
    """Lights the top and left edge of a panel and shades its bottom and right, as vanilla's panels are."""
    for x in range(x0, x1 + 1):
        r, g, b, a = img.getpixel((x, y0)); px(img, x, y0, mul((r, g, b), light), a)
        r, g, b, a = img.getpixel((x, y1)); px(img, x, y1, mul((r, g, b), dark), a)
    for y in range(y0 + 1, y1):
        r, g, b, a = img.getpixel((x0, y)); px(img, x0, y, mul((r, g, b), light), a)
        r, g, b, a = img.getpixel((x1, y)); px(img, x1, y, mul((r, g, b), dark), a)


GLYPHS = {
    '1': ['.#.', '##.', '.#.', '.#.', '###'], '2': ['##.', '..#', '.#.', '#..', '###'], '3': ['##.', '..#', '.#.', '..#', '##.'],
    '4': ['#.#', '#.#', '###', '..#', '..#'], '5': ['###', '#..', '##.', '..#', '##.'],
    'A': ['.#.', '#.#', '###', '#.#', '#.#'], 'B': ['##.', '#.#', '##.', '#.#', '##.'], 'C': ['.##', '#..', '#..', '#..', '.##'],
    'K': ['#.#', '##.', '#..', '##.', '#.#'], 'E': ['###', '#..', '##.', '#..', '###'], 'P': ['##.', '#.#', '##.', '#..', '#..'],
    'O': ['.#.', '#.#', '#.#', '#.#', '.#.'], 'U': ['#.#', '#.#', '#.#', '#.#', '.#.'], 'T': ['###', '.#.', '.#.', '.#.', '.#.'],
    'I': ['###', '.#.', '.#.', '.#.', '###'], 'H': ['#.#', '#.#', '###', '#.#', '#.#'], 'N': ['#.#', '###', '###', '#.#', '#.#'],
    '6': ['.##', '#..', '##.', '#.#', '.#.'],
}


def glyph(img, ch, x, y, c, scale=1):
    for j, row in enumerate(GLYPHS[ch]):
        for i, cell in enumerate(row):
            if cell == '#':
                for dy in range(scale):
                    for dx in range(scale):
                        px(img, x + i * scale + dx, y + j * scale + dy, c)


# ------------------------------------------------------------------------------------------------ materials
def nursery_paper():
    """Nursery paper: powder blue, a fine white stripe, small yellow ducklings on a half-drop repeat."""
    img = ground((176, 206, 228), 2101, .02)
    for y in range(16):
        px(img, 0, y, (214, 230, 240)); px(img, 8, y, (196, 220, 236))
    for cx, cy in ((3, 3), (11, 11)):
        duck = (240, 206, 84)
        for dx, dy in ((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1), (3, -1), (2, -1), (3, -2)):
            px(img, cx + dx, cy + dy, duck)
        px(img, cx + 4, cy - 1, (232, 140, 60))  # the beak
        px(img, cx + 3, cy - 2, mul(duck, .9))
        px(img, cx + 1, cy + 2, mul(duck, .8)); px(img, cx + 2, cy + 2, mul(duck, .8))
    return img


def nursery_carpet():
    """Cream nursery wool, soft and faintly mottled."""
    rng = random.Random(2102)
    img = canvas()
    base = (232, 222, 200)
    for y in range(16):
        for x in range(16):
            f = 1 + rng.uniform(-.035, .035) - (.05 if rng.random() < .1 else 0)
            px(img, x, y, mul(base, f))
    return img


def star_ceiling():
    """The childhood room's ceiling: deep blue paint, a little uneven, for the stars to glow against."""
    rng = random.Random(2103)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((40, 46, 86), 1 + rng.uniform(-.06, .06)))
    return img


def stars():
    """Glow-in-the-dark stickers: two five-point stars, a crescent moon and a scatter of dots, pale green-yellow."""
    img = canvas()
    glow, edge = (214, 255, 150), (170, 220, 110)
    star = ['..#..', '..#..', '#####', '.###.', '##.##']
    for sx, sy in ((2, 2), (9, 9)):
        for j, row in enumerate(star):
            for i, cell in enumerate(row):
                if cell == '#':
                    px(img, sx + i, sy + j, glow if (i, j) != (2, 2) else edge)
    for x, y in ((12, 1), (13, 2), (13, 3), (12, 4)):
        px(img, x, y, glow)
    for x, y in ((6, 12), (1, 9), (14, 13), (8, 5), (4, 14)):
        px(img, x, y, edge)
    return img


def child_paper():
    """The childhood room's paper: buttercup yellow with white polka dots and a faint diagonal sheen."""
    img = ground((240, 214, 120), 2104, .02)
    for cx, cy in ((3, 3), (11, 3), (7, 11), (15, 11)):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            px(img, (cx + dx) % 16, (cy + dy) % 16, (252, 246, 226))
    for i in range(16):
        r, g, b, _ = img.getpixel((i, (i * 3) % 16)); px(img, i, (i * 3) % 16, mul((r, g, b), 1.03))
    return img


def teen_paint():
    """The teenage room's walls: dusty violet emulsion with old tape marks and a blob of putty."""
    img = ground((126, 98, 140), 2105, .025)
    for x in range(2, 6):
        px(img, x, 3, (150, 126, 160))  # a strip of tape left behind
    for y in range(9, 13):
        px(img, 12, y, (148, 124, 158))
    px(img, 9, 6, (214, 210, 196)); px(img, 10, 6, (190, 186, 172)); px(img, 9, 7, (180, 176, 162))
    return img


def teen_carpet():
    """Charcoal low-pile carpet, flecked."""
    rng = random.Random(2106)
    img = canvas()
    for y in range(16):
        for x in range(16):
            r = rng.random()
            c = (74, 72, 76) if r > .12 else (104, 100, 110) if r > .05 else (52, 50, 54)
            px(img, x, y, mul(c, 1 + rng.uniform(-.04, .04)))
    return img


def apartment_wall():
    """A rented room's walls: magnolia over old plaster, scuffed low, two nail holes and a paler square where a picture hung."""
    img = ground((226, 214, 190), 2107, .02)
    for x in range(5, 12):
        for y in range(3, 9):
            r, g, b, _ = img.getpixel((x, y)); px(img, x, y, mul((r, g, b), 1.04))
    px(img, 8, 2, (90, 80, 70)); px(img, 3, 11, (110, 100, 88))
    rng = random.Random(2117)
    for _ in range(6):
        x, y = rng.randrange(16), rng.randrange(12, 16)
        px(img, x, y, (190, 178, 158))
    return img


def apartment_boards():
    """Bare pine boards, sanded long ago, with dark gaps between them and a few nail heads."""
    rng = random.Random(2108)
    img = canvas()
    base = (190, 150, 102)
    for y in range(16):
        board = y // 4
        tone = (1.0, .94, 1.04, .97)[board]
        for x in range(16):
            f = tone + rng.choice((0, 0, -.03, .03)) + (.05 if (x * 5 + y * 3) % 17 == 0 else 0)
            px(img, x, y, mul(base, f))
        if y % 4 == 3:
            for x in range(16):
                px(img, x, y, mul(base, .55))
    for x, y in ((2, 1), (13, 1), (6, 5), (10, 9), (1, 13), (14, 13)):
        px(img, x, y, (70, 60, 50))
    for y in range(4):
        px(img, (5 + y * 4) % 16, y * 4 + 1, mul(base, .7))
    return img


def hospital_tile():
    """Hospital wall tile: pale green glaze in four-pixel squares with grey grout, each square lit at one corner."""
    rng = random.Random(2109)
    img = canvas()
    glaze, grout = (178, 210, 186), (150, 160, 152)
    for y in range(16):
        for x in range(16):
            if x % 4 == 0 or y % 4 == 0:
                px(img, x, y, grout)
            else:
                f = 1.0 + (.06 if (x % 4, y % 4) == (1, 1) else 0) + rng.uniform(-.02, .02)
                px(img, x, y, mul(glaze, f))
    return img


def hospital_lino():
    """Speckled ward linoleum: putty grey with darker and lighter flecks."""
    rng = random.Random(2110)
    img = canvas()
    for y in range(16):
        for x in range(16):
            r = rng.random()
            c = (176, 172, 160) if r > .16 else (140, 136, 126) if r > .07 else (204, 200, 190)
            px(img, x, y, c)
    return img


def mother_paper():
    """Mother's paper: faded rose and cream stripes, a small leafy sprig in each cream band."""
    img = canvas()
    rose, cream, sprig = (202, 152, 150), (234, 220, 204), (128, 140, 104)
    rng = random.Random(2111)
    for y in range(16):
        for x in range(16):
            c = rose if x % 8 < 3 else cream
            if x % 8 == 3:
                c = mix(rose, cream, .5)
            px(img, x, y, mul(c, 1 + rng.uniform(-.02, .02)))
    for cx, cy in ((5, 3), (13, 11)):
        for dx, dy in ((0, 0), (0, 1), (0, 2), (-1, 1), (1, 0), (1, 2)):
            px(img, cx + dx, cy + dy, sprig)
        px(img, cx, cy - 1, (196, 120, 118))
    return img


def mother_carpet():
    """Mother's carpet: dusty rose, worn paler where she walked."""
    rng = random.Random(2112)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((170, 120, 120), 1 + rng.uniform(-.04, .04) + (.04 if 5 < x < 11 else 0)))
    return img


def hall_paper():
    """The hall's paper: cream with a fine brown double stripe every eight pixels, as in any family's hall."""
    img = ground((228, 214, 184), 2113, .015)
    for y in range(16):
        for x in (2, 4, 10, 12):
            px(img, x, y, (176, 148, 110))
        px(img, 3, y, (212, 196, 164)); px(img, 11, y, (212, 196, 164))
    return img


def hall_runner():
    """The hall runner: red ground, a navy border on two sides, a cream lozenge in the middle."""
    img = canvas()
    red, navy, cream = (150, 44, 40), (40, 46, 84), (220, 200, 160)
    rng = random.Random(2114)
    for y in range(16):
        for x in range(16):
            c = red
            if x in (0, 15):
                c = navy
            elif x in (1, 14):
                c = cream
            elif abs(x - 7.5) + abs(y % 8 - 3.5) < 3.2:
                c = navy if abs(x - 7.5) + abs(y % 8 - 3.5) > 2 else cream
            px(img, x, y, mul(c, 1 + rng.uniform(-.04, .04)))
    return img


# ------------------------------------------------------------------------------------------------ the nursery and the childhood room
def white_paint(seed=2201):
    """Nursery furniture: white gloss over wood, the grain just showing through."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            f = 1.0 - (.04 if (x * 7 + y) % 9 == 0 else 0) + rng.uniform(-.015, .015)
            px(img, x, y, mul((236, 234, 226), f))
    return img


def duck_sheet():
    """The crib sheet: pale blue cotton printed with small yellow ducks."""
    img = ground((196, 220, 238), 2202, .02)
    for cx, cy in ((3, 4), (11, 12), (12, 3), (4, 12)):
        for dx, dy in ((0, 0), (1, 0), (1, -1)):
            px(img, cx + dx, cy + dy, (238, 204, 80))
        px(img, cx + 2, cy - 1, (226, 132, 58))
    return img


def letter_block(ch, paint):
    """A wooden alphabet block, painted cream, a bevelled border and a big coloured letter on every face."""
    rng = random.Random(2203 + ord(ch))
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((232, 216, 180), 1 + rng.uniform(-.025, .025)))
    for i in range(16):
        for c in ((i, 0), (i, 15), (0, i), (15, i)):
            px(img, *c, mul(paint, .9))
        for c in ((i, 1), (1, i)):
            if 1 <= c[0] <= 14 and 1 <= c[1] <= 14:
                px(img, *c, mul(paint, 1.15))
    glyph(img, ch, 3, 3, paint, 2)
    glyph(img, ch, 4, 4, mul(paint, .7), 2)
    glyph(img, ch, 3, 3, paint, 2)
    return img


def drawers_front():
    """A giant chest's drawer front: white panel, a shadowed reveal round it, two brass knobs."""
    img = white_paint(2206)
    for x in range(16):
        px(img, x, 0, (150, 148, 140)); px(img, x, 15, (170, 168, 160))
    for y in range(16):
        px(img, 0, y, (190, 188, 180)); px(img, 15, y, (170, 168, 160))
    bevel(img, 2, 2, 13, 13, 1.04, .86)
    for kx in (4, 11):
        px(img, kx, 7, (220, 180, 90)); px(img, kx + 1, 7, (180, 140, 60)); px(img, kx, 8, (170, 130, 56)); px(img, kx + 1, 8, (140, 104, 40))
    return img


def lamp_shade():
    """A pleated lamp shade in buttermilk, brighter toward its lower rim where the bulb shows."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((244, 226, 170), (1.05 if x % 2 == 0 else .95) * (0.95 + y * .006)))
    return img


def ceramic():
    """A ceramic lamp base, glazed blue with a white band."""
    rng = random.Random(2208)
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (90, 130, 186) if y not in (6, 7) else (230, 232, 236)
            px(img, x, y, mul(c, 1 + rng.uniform(-.03, .03) + (.1 if x in (4, 5) else 0)))
    return img


def baby_book():
    """The baby book lying open: on the left a small photograph of a baby asleep, on the right lines of blue ink."""
    img = canvas()
    paper = (240, 234, 216)
    for y in range(16):
        for x in range(16):
            px(img, x, y, paper if 0 < x < 15 else (150, 186, 214))
    for y in range(16):
        px(img, 7, y, (206, 198, 178)); px(img, 8, y, (220, 212, 192))
    for y in range(3, 9):
        for x in range(2, 6):
            px(img, x, y, (214, 202, 186) if y < 5 else (224, 196, 180))
    for x in range(2, 6):
        px(img, x, 2, (60, 52, 44)); px(img, x, 9, (60, 52, 44))
    px(img, 3, 6, (190, 150, 140)); px(img, 4, 6, (190, 150, 140))  # a sleeping face
    for y in (3, 5, 7, 9, 11, 13):
        for x in range(10, 14 - (y == 13) * 2):
            px(img, x, y, (60, 80, 150) if (x + y) % 3 else (90, 110, 170))
    for x in range(2, 6):
        px(img, x, 12, (60, 80, 150))
    return img


def cloth_blue():
    """Pale blue book cloth."""
    rng = random.Random(2210)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((150, 186, 214), 1 + rng.uniform(-.04, .04) + (.04 if (x + y) % 2 else 0)))
    return img


def quilt():
    """A child's patchwork quilt: four-pixel squares of pink gingham, yellow, mint and a small floral, stitched."""
    img = canvas()
    patches = [((236, 170, 186), 'gingham'), ((244, 220, 120), 'plain'), ((170, 222, 196), 'dots'), ((180, 196, 236), 'floral')]
    for py in range(4):
        for pxi in range(4):
            c, kind = patches[(pxi + py * 3) % 4]
            for j in range(4):
                for i in range(4):
                    x, y = pxi * 4 + i, py * 4 + j
                    f = 1.0
                    if kind == 'gingham' and (i % 2 == 0) != (j % 2 == 0):
                        f = .88
                    if kind == 'dots' and (i, j) in ((1, 1), (3, 3)):
                        f = 1.15
                    if kind == 'floral' and (i, j) in ((1, 2), (2, 1)):
                        c2 = (236, 140, 150); px(img, x, y, c2); continue
                    px(img, x, y, mul(c, f))
    for i in range(16):
        for k in (0, 4, 8, 12):
            if i % 2 == 0:
                px(img, k, i, (250, 246, 236)); px(img, i, k, (250, 246, 236))
    return img


def headboard():
    """The child's headboard: pale blue painted boards with a white star cut out of the middle."""
    rng = random.Random(2212)
    img = canvas()
    for y in range(16):
        for x in range(16):
            f = 1 + rng.uniform(-.02, .02) - (.08 if x % 5 == 4 else 0)
            px(img, x, y, mul((150, 190, 222), f))
    star = ['..#..', '.###.', '#####', '.###.', '.#.#.']
    for j, row in enumerate(star):
        for i, cell in enumerate(row):
            if cell == '#':
                px(img, 6 + i, 5 + j, (246, 246, 240))
    return img


def pillow():
    """A white cotton pillow, a lace edge on two sides."""
    img = ground((246, 244, 238), 2213, .02)
    for i in range(16):
        if i % 2 == 0:
            px(img, i, 0, (226, 222, 214)); px(img, 0, i, (226, 222, 214))
    return img


def crayon(color):
    """A crayon's wax and its paper wrapper: the colour itself, a darker band and a black stripe printed on the wrapper."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = color
            if 3 <= x <= 12:
                c = mul(color, .86)
                if x in (5, 10):
                    c = (34, 30, 30)
            px(img, x, y, mul(c, 1.1 if y < 5 else .92 if y > 10 else 1))
    return img


def drawing():
    """A child's crayon drawing: a house, a very tall mother, a small girl up to her pocket, stars above."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (244, 240, 226))
    red, blue, green, yellow, brown = (210, 56, 50), (60, 90, 190), (70, 150, 70), (236, 200, 50), (120, 80, 50)
    for x in range(1, 7):
        px(img, x, 13, green)
    for y in range(8, 13):
        px(img, 1, y, red); px(img, 6, y, red)
    for i in range(4):
        px(img, 1 + i, 8 - i, red); px(img, 6 - i, 8 - i, red)
    px(img, 3, 11, brown); px(img, 3, 12, brown); px(img, 4, 11, brown); px(img, 4, 12, brown)
    # Mother: a circle head and a long dress, the tallest thing on the page.
    for x, y in ((10, 2), (11, 2), (9, 3), (12, 3), (10, 4), (11, 4)):
        px(img, x, y, brown)
    for y in range(5, 14):
        px(img, 10, y, blue); px(img, 11, y, blue)
    for y in range(8, 14):
        px(img, 9, y, blue); px(img, 12, y, blue)
    px(img, 8, 7, brown); px(img, 13, 7, brown)
    # The girl, up to Mother's pocket.
    px(img, 14, 9, brown); px(img, 14, 10, red); px(img, 14, 11, red); px(img, 14, 12, red); px(img, 13, 13, brown); px(img, 15, 13, brown)
    for x, y in ((2, 2), (6, 1), (4, 4), (14, 1)):
        px(img, x, y, yellow)
    return img


def moon_light():
    """The night light: a smiling crescent moon in warm yellow plastic."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = (x - 7.5) ** 2 + (y - 7.5) ** 2
            d2 = (x - 10.5) ** 2 + (y - 6) ** 2
            if d < 49 and d2 > 25:
                px(img, x, y, mul((252, 230, 140), 1.05 - d / 200))
            else:
                px(img, x, y, (236, 226, 196))
    px(img, 5, 7, (180, 140, 60)); px(img, 6, 10, (190, 150, 70)); px(img, 7, 11, (190, 150, 70))
    return img


# ------------------------------------------------------------------------------------------------ the teenage room
def poster_band():
    """A gig poster: black ground, a jagged white band name and four figures in silhouette against a red flare."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (24, 22, 26) if y > 3 else (30, 28, 32))
    for x in range(16):
        for y in range(6, 12):
            d = abs(x - 7.5) + abs(y - 9)
            if d < 6:
                px(img, x, y, mix((200, 40, 40), (24, 22, 26), d / 6))
    for x in (3, 6, 9, 12):
        px(img, x, 9, (8, 8, 10)); px(img, x, 10, (8, 8, 10)); px(img, x, 11, (8, 8, 10)); px(img, x, 12, (8, 8, 10)); px(img, x, 8, (8, 8, 10))
    for x in range(2, 14):
        if x % 3 != 1:
            px(img, x, 2, (236, 236, 236))
        if x % 2 == 0:
            px(img, x, 3, (200, 200, 200))
    for x in range(1, 15):
        px(img, x, 14, (190, 190, 190) if x % 4 else (24, 22, 26))
    for i in range(16):
        px(img, i, 0, (220, 214, 200)); px(img, i, 15, (220, 214, 200)); px(img, 0, i, (220, 214, 200)); px(img, 15, i, (220, 214, 200))
    return img


def poster_moon():
    """A space poster: deep blue, the moon's grey limb along the bottom, the Earth small and blue above it."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (18, 24, 56))
    for x in range(16):
        for y in range(16):
            if (x - 7.5) ** 2 + (y - 22) ** 2 < 100:
                px(img, x, y, mul((170, 170, 160), 1 - ((x * 3 + y * 5) % 7) * .02))
    for x, y in ((10, 4), (11, 4), (10, 5), (11, 5), (12, 5), (11, 3)):
        px(img, x, y, (80, 140, 220))
    px(img, 10, 4, (220, 230, 240))
    for x, y in ((2, 2), (5, 7), (14, 9), (3, 10), (13, 1)):
        px(img, x, y, (230, 230, 240))
    for i in range(16):
        px(img, i, 0, (220, 214, 200)); px(img, i, 15, (220, 214, 200)); px(img, 0, i, (220, 214, 200)); px(img, 15, i, (220, 214, 200))
    return img


def stereo():
    """A black portable stereo's face: two round speaker grilles either side of a cassette door with a window."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (40, 40, 44))
    for cx in (3.5, 12.5):
        for y in range(16):
            for x in range(16):
                d = (x - cx) ** 2 + (y - 9.5) ** 2
                if d < 9:
                    px(img, x, y, (24, 24, 26) if (x + y) % 2 else (56, 56, 60))
                elif d < 12:
                    px(img, x, y, (120, 120, 126))
    for x in range(6, 10):
        for y in range(7, 12):
            px(img, x, y, (70, 70, 76))
    for x in range(7, 9):
        px(img, x, 9, (150, 120, 90))
    for x in range(2, 14, 2):
        px(img, x, 3, (190, 190, 196))
    px(img, 13, 3, (220, 60, 50))
    return img


def plastic_black():
    rng = random.Random(2304)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((44, 44, 48), 1 + rng.uniform(-.05, .05)))
    return img


def lava():
    """A lava lamp's glass: orange liquid with magenta blobs rising."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (236, 120, 40))
    for cx, cy, r in ((7, 3, 2.4), (9, 9, 3.2), (6, 13, 2.0)):
        for y in range(16):
            for x in range(16):
                if (x - cx) ** 2 + (y - cy) ** 2 < r * r:
                    px(img, x, y, (226, 50, 140))
    return img


def silver():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((196, 198, 204), 1.1 if (x + y) % 6 == 0 else .96))
    return img


def duvet_purple():
    """A teenager's duvet: deep purple with a black zigzag, rumpled."""
    rng = random.Random(2306)
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (86, 40, 110)
            if (y + abs((x % 6) - 3)) % 8 == 0:
                c = (26, 20, 30)
            px(img, x, y, mul(c, 1 + rng.uniform(-.05, .05) + (.08 if (x * 3 + y * 5) % 11 == 0 else 0)))
    return img


def sheet_grey():
    rng = random.Random(2307)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((168, 168, 174), 1 + rng.uniform(-.04, .04) + (.06 if (x + 2 * y) % 7 == 0 else 0)))
    return img


def iron():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((40, 38, 42), 1.25 if x % 4 == 1 else 1))
    return img


def diary():
    """The diary's cover: lilac leatherette, a gold clasp hanging open where it was forced."""
    rng = random.Random(2309)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((170, 140, 196), 1 + rng.uniform(-.04, .04)))
    for y in range(16):
        px(img, 1, y, (130, 100, 156))
    for x in range(11, 16):
        px(img, x, 7, (220, 186, 90)); px(img, x, 8, (180, 146, 60))
    px(img, 15, 9, (200, 166, 70)); px(img, 14, 10, (200, 166, 70))
    glyph(img, 'K', 5, 5, (230, 210, 240))
    return img


def clothes():
    """A heap of clothes: denim, a black band shirt, red flannel."""
    rng = random.Random(2310)
    img = canvas()
    for y in range(16):
        for x in range(16):
            band = (x + y // 2) % 12
            c = (60, 86, 140) if band < 4 else (30, 30, 34) if band < 8 else (170, 50, 46)
            if band >= 8 and (x % 3 == 0 or y % 3 == 0):
                c = (110, 30, 30)
            px(img, x, y, mul(c, 1 + rng.uniform(-.08, .08)))
    return img


def keep_out():
    """A hand-lettered door sign: yellow card, KEEP OUT in thick black marker, a skull doodle."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (238, 210, 70))
    for i, ch in enumerate('KEEP'):
        glyph(img, ch, 1 + i * 4 - (1 if i == 3 else 0), 2, (24, 20, 20))
    for i, ch in enumerate('OUT'):
        glyph(img, ch, 3 + i * 4, 9, (24, 20, 20))
    for i in range(16):
        px(img, i, 0, (200, 176, 50)); px(img, i, 15, (200, 176, 50))
    return img


# ------------------------------------------------------------------------------------------------ the first flat
def cardboard(seed=2401, tape=True):
    """A moving box's side: brown corrugated card, a tape strip over the top edge."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((182, 140, 92), 1 + rng.uniform(-.03, .03) + (.03 if x % 3 == 0 else 0)))
    if tape:
        for x in range(16):
            for y in range(0, 3):
                px(img, x, y, (200, 176, 130) if y < 2 else (160, 130, 90))
    return img


def cardboard_top():
    """The box's taped top: two flaps meeting under a tape strip."""
    img = cardboard(2402, False)
    for y in range(16):
        for x in range(6, 10):
            px(img, x, y, (206, 182, 136))
        px(img, 7, y, (190, 166, 120))
    return img


def box_kitchen():
    """A box side marked KITCHEN in black marker, the word crossed through and BEDROOM written under it."""
    img = cardboard(2403)
    for i, ch in enumerate('KITCHEN'):
        if ch in GLYPHS:
            glyph(img, ch, 1 + i * 2, 5, (30, 26, 24))
    for x in range(0, 16):
        px(img, x, 7, (30, 26, 24))
    return img


def box_open():
    """Inside an open box: a jumble of books, a mug, the corner of a framed photo."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (120, 90, 58))
    for x0, y0, x1, y1, c in ((1, 1, 6, 9, (150, 40, 40)), (7, 2, 10, 12, (40, 80, 120)), (11, 1, 14, 6, (220, 200, 160)), (2, 10, 7, 14, (200, 200, 196)), (10, 8, 14, 14, (110, 70, 40))):
        fill(img, x0, y0, x1, y1, c)
        bevel(img, x0, y0, x1, y1)
    return img


def ticking():
    """Mattress ticking: white with narrow blue stripes."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (70, 96, 150) if x % 4 == 1 else (236, 236, 230))
    return img


def sleeping_bag():
    rng = random.Random(2406)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((70, 110, 70), 1 + rng.uniform(-.04, .04) - (.15 if y % 5 == 0 else 0)))
    return img


def hot_plate():
    """A single-ring hot plate: white enamel and a black coil."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (232, 230, 224))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 2 < d < 6.3 and int(d * 1.3) % 2 == 0:
                px(img, x, y, (34, 32, 32))
    px(img, 13, 13, (200, 40, 30))
    return img


def bulb():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((255, 236, 180), 1.05 - abs(x - 7.5) * .02))
    return img


def letter():
    """Mother's letter on its envelope: blue ink on lined paper, a stamp on the envelope's corner."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (232, 224, 204) if x < 10 or y > 5 else (240, 236, 222))
    for y in (2, 4, 6, 8, 10, 12):
        for x in range(1, 9):
            if (x * 7 + y) % 5:
                px(img, x, y, (60, 76, 150))
    for x in range(11, 15):
        for y in range(1, 5):
            px(img, x, y, (190, 60, 60) if (x + y) % 2 else (170, 50, 50))
    return img


# ------------------------------------------------------------------------------------------------ the hospital room
def hosp_sheet():
    img = ground((240, 242, 240), 2501, .015)
    for x in range(16):
        px(img, x, 5, (218, 222, 222))
    return img


def chrome():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((206, 210, 214), 1.15 if x % 4 == 0 else .9 if x % 4 == 2 else 1))
    return img


def cellular():
    """A cellular hospital blanket: pale blue, a grid of open weave."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (150, 186, 214) if x % 3 and y % 3 else (122, 158, 190))
    return img


def bed_panel():
    """The hospital bed's head panel: cream laminate with a darker edge band."""
    img = ground((222, 214, 196), 2504, .015)
    for i in range(16):
        px(img, i, 0, (170, 160, 140)); px(img, i, 15, (170, 160, 140)); px(img, 0, i, (180, 170, 150)); px(img, 15, i, (180, 170, 150))
    return img


def iv_bag():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (220, 232, 236) if y < 4 else (196, 220, 228))
    for y in range(6, 12):
        px(img, 7, y, (90, 110, 120))
    return img


def monitor_screen():
    """The monitor's screen: black, a green trace with one beat on it and the numbers beside."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (10, 20, 14))
    trace = [9, 9, 9, 9, 9, 8, 3, 13, 9, 9, 9, 9, 9, 9, 9, 9]
    for x, y in enumerate(trace):
        px(img, x, y, (90, 240, 120))
        if x and abs(trace[x - 1] - y) > 1:
            for yy in range(min(y, trace[x - 1]), max(y, trace[x - 1])):
                px(img, x, yy, (90, 240, 120))
    for x in range(11, 15):
        px(img, x, 2, (240, 220, 90))
    return img


def beige_plastic():
    return ground((210, 202, 182), 2508, .02)


def vinyl():
    rng = random.Random(2509)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((60, 128, 130), 1 + rng.uniform(-.03, .03) + (.1 if (x + y) % 7 == 0 else 0)))
    return img


def carnations():
    """A bunch of carnations: pink and red heads with frilled edges."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (232, 120, 150) if (x // 4 + y // 4) % 2 else (200, 40, 60)
            px(img, x, y, mul(c, 1.1 if (x + y) % 3 == 0 else .92))
    return img


def stems():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (90, 140, 80) if x % 3 == 0 else (190, 220, 230))
    return img


def chart():
    """The chart on the bed's foot: a brown clipboard, a white form ruled in boxes, entries in blue."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (130, 96, 60))
    for y in range(2, 15):
        for x in range(2, 14):
            px(img, x, y, (244, 242, 236))
    for x in range(5, 11):
        px(img, x, 1, (170, 170, 176)); px(img, x, 2, (140, 140, 146))
    for y in (5, 8, 11):
        for x in range(2, 14):
            px(img, x, y, (170, 190, 210))
    for y in (4, 7, 10, 13):
        for x in range(3, 12):
            if (x * 3 + y) % 4:
                px(img, x, y, (50, 70, 150))
    return img


# ------------------------------------------------------------------------------------------------ Mother's room and the hall
def candlewick():
    """A candlewick bedspread: cream cotton with raised tufts in rows of small diamonds."""
    img = ground((232, 222, 200), 2601, .02)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0 and (x - y) % 8 == 0 or (x + y) % 8 == 4 and (x - y) % 4 == 0:
                px(img, x, y, (248, 242, 226))
                px(img, x + 1, y + 1, (206, 194, 170))
    return img


def walnut(seed=2602):
    """Walnut: dark brown with long grain."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            f = (.86 if y % 5 == 1 else 1.0) + rng.choice((0, 0, -.04, .04))
            px(img, x, y, mul((96, 64, 42), f))
    return img


def mirror():
    """An old mirror's glass: grey-blue, a pale reflection streak, foxing at the edge."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = mix((150, 170, 180), (200, 214, 220), max(0, 1 - abs(x - y - 2) / 4))
            px(img, x, y, c)
    for x, y in ((0, 3), (1, 13), (14, 1), (15, 10), (0, 9)):
        px(img, x, y, (120, 110, 90))
    return img


def maple():
    rng = random.Random(2604)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((200, 150, 96), (.9 if (x + y * 3) % 7 == 0 else 1.0) + rng.uniform(-.03, .03)))
    return img


def brass_plaque(ch):
    """A brass house-number plaque: a polished oval plate with a black numeral."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (200, 160, 76) if (x - 7.5) ** 2 / 64 + (y - 7.5) ** 2 / 64 < 1 else (150, 112, 46))
    for x in range(16):
        for y in range(16):
            if (x - 7.5) ** 2 / 64 + (y - 7.5) ** 2 / 64 < .62:
                px(img, x, y, (226, 190, 100) if x + y < 14 else (196, 156, 70))
    glyph(img, ch, 5, 3, (36, 28, 18), 2)
    return img


def mobile():
    """The mobile's felt shapes, cut out, one to a quarter: a yellow duck, a blue star, a white moon, a pink cloud."""
    img = canvas()
    duck = ['........', '....##..', '...####.', '#..###..', '######..', '.#####..', '..###...', '........']
    star = ['...#....', '...#....', '#######.', '.#####..', '..###...', '.##.##..', '.#...#..', '........']
    moon = ['..###...', '.##.....', '##......', '##......', '##......', '.##.....', '..###...', '........']
    cloud = ['........', '..##....', '.####.#.', '########', '########', '.######.', '........', '........']
    shapes = ((0, 0, duck, (240, 206, 84)), (8, 0, star, (110, 150, 214)), (0, 8, moon, (240, 238, 226)), (8, 8, cloud, (232, 168, 186)))
    for qx, qy, rows, c in shapes:
        for y, row in enumerate(rows):
            for x, cell in enumerate(row):
                if cell == '#':
                    below = y + 1 < 8 and rows[y + 1][x] == '#'
                    px(img, qx + x, qy + y, mul(c, 1.0 if below else .82))
    px(img, 5, 2, (40, 40, 40)); px(img, 7, 3, (232, 140, 60)); px(img, 6, 3, (232, 140, 60))  # the duck's eye and beak
    return img


def notebook():
    """The daughter's notebook: a red marbled cover with a label, HOUSE NUMBERS written on it."""
    rng = random.Random(2606)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((150, 46, 44), 1 + rng.uniform(-.1, .1)))
    for x in range(3, 13):
        for y in range(5, 10):
            px(img, x, y, (236, 228, 204))
    for x in range(4, 12):
        if x % 3:
            px(img, x, 7, (40, 40, 60))
    for y in range(16):
        px(img, 0, y, (60, 30, 30)); px(img, 1, y, (90, 36, 34))
    return img


TEXTURES = {
    'nursery_paper': nursery_paper, 'nursery_carpet': nursery_carpet, 'star_ceiling': star_ceiling, 'stars': stars,
    'child_paper': child_paper, 'teen_paint': teen_paint, 'teen_carpet': teen_carpet, 'apartment_wall': apartment_wall,
    'apartment_boards': apartment_boards, 'hospital_tile': hospital_tile, 'hospital_lino': hospital_lino,
    'mother_paper': mother_paper, 'mother_carpet': mother_carpet, 'hall_paper': hall_paper, 'hall_runner': hall_runner,
    'white_paint': white_paint, 'duck_sheet': duck_sheet, 'toy_a': lambda: letter_block('A', (196, 50, 46)),
    'toy_b': lambda: letter_block('B', (50, 90, 180)), 'toy_c': lambda: letter_block('C', (60, 150, 70)),
    'drawers_front': drawers_front, 'lamp_shade': lamp_shade, 'ceramic': ceramic, 'baby_book': baby_book, 'cloth_blue': cloth_blue,
    'quilt': quilt, 'headboard': headboard, 'pillow': pillow, 'crayon_red': lambda: crayon((214, 52, 48)),
    'crayon_blue': lambda: crayon((58, 96, 200)), 'drawing': drawing, 'moon_light': moon_light,
    'poster_band': poster_band, 'poster_moon': poster_moon, 'stereo': stereo, 'plastic_black': plastic_black, 'lava': lava,
    'silver': silver, 'duvet_purple': duvet_purple, 'sheet_grey': sheet_grey, 'iron': iron, 'diary': diary, 'clothes': clothes,
    'keep_out': keep_out, 'cardboard': cardboard, 'cardboard_top': cardboard_top, 'box_kitchen': box_kitchen, 'box_open': box_open,
    'ticking': ticking, 'sleeping_bag': sleeping_bag, 'hot_plate': hot_plate, 'bulb': bulb, 'letter': letter,
    'hosp_sheet': hosp_sheet, 'chrome': chrome, 'cellular': cellular, 'bed_panel': bed_panel, 'iv_bag': iv_bag,
    'monitor_screen': monitor_screen, 'beige_plastic': beige_plastic, 'vinyl': vinyl, 'carnations': carnations, 'stems': stems,
    'chart': chart, 'candlewick': candlewick, 'walnut': walnut, 'mirror': mirror, 'maple': maple, 'notebook': notebook,
    'mobile': mobile, **{f'number_{n}': (lambda n=n: brass_plaque(str(n))) for n in range(1, 7)},
}
# Plain material blocks; the star ceiling is a material with its own model (stars glowing below the paint).
MATERIALS = ('nursery_paper', 'nursery_carpet', 'child_paper', 'teen_paint', 'teen_carpet', 'apartment_wall', 'apartment_boards',
             'hospital_tile', 'hospital_lino', 'mother_paper', 'mother_carpet', 'hall_paper', 'hall_runner')


# ------------------------------------------------------------------------------------------------ models
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


def box(frm, to, tex, faces=DIRS, uv=None, over=None, glow=0):
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
    el = {'from': list(frm), 'to': list(to), 'faces': f}
    if glow:
        el['neoforge_data'] = {'block_light': glow, 'sky_light': 0}
    return el


def legs(x0, z0, x1, z1, top, tex, w=1.5):
    return [box((x, 0, z), (x + w, top, z + w), tex) for x in (x0, x1 - w) for z in (z0, z1 - w)]


MODEL_SPECS = {}


def spec(name, textures, particle):
    def wrap(fn):
        MODEL_SPECS[name] = (fn, textures, particle)
        return fn
    return wrap


# Every model is authored facing north: its front toward -z, its back (a wall, a headboard) toward +z. Beds are two cells:
# the head cell's headboard at +z, the foot cell north of it.

def slats_x(x0, x1, z0, z1, y0, y1, step, w, tex):
    """Vertical slats along x between two rails, for crib and cot sides that face north or south."""
    return [box((x, y0, z0), (x + w, y1, z1), tex) for x in range(x0, x1, step)]


def slats_z(z0, z1, x0, x1, y0, y1, step, w, tex):
    return [box((x0, y0, z), (x1, y1, z + w), tex) for z in range(z0, z1, step)]


# ---------------------------------------------------------------- the nursery: a crib and its mobile at twice the size
@spec('crib_big_edge', {'w': 'white_paint', 's': 'duck_sheet'}, 'white_paint')
def m_crib_big_edge():
    """A middle cell of the big crib's long side: the mattress, and the slatted side along the back edge."""
    els = [box((0, 8, 0), (16, 12, 13), 'w', over={'up': 's'}), box((0, 0, 13), (16, 7, 16), 'w'), box((0, 7, 13), (16, 9, 16), 'w')]
    els += slats_x(1, 16, 13.5, 15.5, 9, 16, 4, 2, 'w')
    return els


@spec('crib_big_corner', {'w': 'white_paint', 's': 'duck_sheet'}, 'white_paint')
def m_crib_big_corner():
    """A corner cell: the mattress, the long side along the back and the end along the west, and the post they meet at."""
    els = [box((3, 8, 0), (16, 12, 13), 'w', over={'up': 's'}), box((0, 0, 0), (4, 16, 4), 'w'), box((4, 0, 13), (16, 7, 16), 'w'),
           box((4, 7, 13), (16, 9, 16), 'w'), box((0, 0, 4), (3, 7, 16), 'w'), box((0, 7, 4), (3, 9, 16), 'w'), box((0, 0, 12), (4, 16, 16), 'w')]
    els += slats_x(5, 16, 13.5, 15.5, 9, 16, 4, 2, 'w') + slats_z(5, 12, .5, 2.5, 9, 16, 4, 2, 'w')
    return els


@spec('crib_big_rail', {'w': 'white_paint'}, 'white_paint')
def m_crib_big_rail():
    return slats_x(1, 16, 13.5, 15.5, 0, 12, 4, 2, 'w') + [box((0, 12, 13), (16, 14.5, 16), 'w')]


@spec('crib_big_rail_corner', {'w': 'white_paint'}, 'white_paint')
def m_crib_big_rail_corner():
    els = slats_x(5, 16, 13.5, 15.5, 0, 12, 4, 2, 'w') + slats_z(5, 12, .5, 2.5, 0, 12, 4, 2, 'w')
    els += [box((4, 12, 13), (16, 14.5, 16), 'w'), box((0, 12, 4), (3, 14.5, 12), 'w'), box((0, 0, 12), (4, 15, 16), 'w'),
            box((.5, 15, 12.5), (3.5, 17, 15.5), 'w'), box((0, 0, 0), (4, 15, 4), 'w'), box((.5, 15, .5), (3.5, 17, 3.5), 'w')]
    return els


@spec('crib_end', {'w': 'white_paint', 's': 'duck_sheet'}, 'white_paint')
def m_crib_end():
    """Half an ordinary crib: two of these, back to back, are one crib a grown woman leans over."""
    els = [box((1, 4, 0), (15, 7, 14), 'w', over={'up': 's'}), box((0, 0, 14), (2, 15, 16), 'w'), box((14, 0, 14), (16, 15, 16), 'w'),
           box((.5, 15, 14.5), (1.5, 16, 15.5), 'w'), box((14.5, 15, 14.5), (15.5, 16, 15.5), 'w')]
    for x0 in (.25, 14.25):
        els += [box((x0, 4, 0), (x0 + 1.5, 5, 14), 'w'), box((x0, 13, 0), (x0 + 1.5, 14.5, 14), 'w')]
        els += slats_z(1, 14, x0 + .25, x0 + 1.25, 5, 13, 3, 1, 'w')
    els += [box((2, 4, 14.25), (14, 5, 15.75), 'w'), box((2, 13, 14.25), (14, 14.5, 15.75), 'w')]
    els += slats_x(3, 14, 14.5, 15.5, 5, 13, 3, 1, 'w')
    return els


def mobile_shapes(arm, hang):
    """Four felt shapes on threads from the ends of a cross; arm is the half-span, hang the thread's length."""
    els = [box((7.5, 16 - hang * .5, 7.5), (8.5, 16, 8.5), 'w')]
    y = 16 - hang * .5
    els += [box((8 - arm, y - .75, 7.6), (8 + arm, y, 8.4), 'w'), box((7.6, y - .75, 8 - arm), (8.4, y, 8 + arm), 'w')]
    quads = ((0, 0, 8, 8), (8, 0, 16, 8), (0, 8, 8, 16), (8, 8, 16, 16))
    size = 2 + arm / 4
    for (x, z), uv in zip(((8 - arm, 8), (8 + arm, 8), (8, 8 - arm), (8, 8 + arm)), quads):
        top = y - .75
        els.append(box((x - .2, top - hang * .5, z - .2), (x + .2, top, z + .2), 'w'))
        bottom = top - hang * .5
        flat_x = abs(z - 8) > .1
        if flat_x:
            els.append(box((x - size, bottom - size * 2, z - .1), (x + size, bottom, z + .1), 'm', faces=('north', 'south'), uv=uv))
        else:
            els.append(box((x - .1, bottom - size * 2, z - size), (x + .1, bottom, z + size), 'm', faces=('east', 'west'), uv=uv))
    return els


@spec('mobile', {'w': 'white_paint', 'm': 'mobile'}, 'mobile')
def m_mobile():
    return mobile_shapes(5, 10)


@spec('mobile_big', {'w': 'white_paint', 'm': 'mobile'}, 'mobile')
def m_mobile_big():
    """The nursery's mobile, twice the size: its shapes hang into the cells around it, no lower than a model may reach."""
    return mobile_shapes(14, 18)


@spec('mobile_string', {'w': 'white_paint'}, 'white_paint')
def m_mobile_string():
    return [box((7.5, 0, 7.5), (8.5, 16, 8.5), 'w')]


@spec('drawers', {'w': 'white_paint', 'd': 'drawers_front'}, 'white_paint')
def m_drawers():
    return [box((0, 0, 1), (16, 15, 16), 'w', over={'north': 'd'}), box((-.25, 15, .5), (16.25, 16, 16), 'w')]


def letter_toy(tex):
    return [box((1.5, 0, 1.5), (14.5, 13, 14.5), tex)]


@spec('toy_a', {'t': 'toy_a'}, 'toy_a')
def m_toy_a():
    return letter_toy('t')


@spec('toy_b', {'t': 'toy_b'}, 'toy_b')
def m_toy_b():
    return letter_toy('t')


@spec('toy_c', {'t': 'toy_c'}, 'toy_c')
def m_toy_c():
    return letter_toy('t')


@spec('lamp', {'c': 'ceramic', 'w': 'white_paint', 'l': 'lamp_shade'}, 'lamp_shade')
def m_lamp():
    return [box((5, 0, 5), (11, 6, 11), 'c'), box((7.5, 6, 7.5), (8.5, 9, 8.5), 'w'),
            box((3, 9, 3), (13, 16, 13), 'l', faces=('north', 'south', 'east', 'west', 'up'), glow=15), box((3.5, 9, 3.5), (12.5, 9.2, 12.5), 'l', faces=('down',), glow=15)]


@spec('baby_book', {'b': 'baby_book', 'c': 'cloth_blue'}, 'cloth_blue')
def m_baby_book():
    return [box((2, 0, 4), (14, 1, 12), 'c', over={'up': 'b'})]


@spec('chair_small', {'m': 'maple'}, 'maple')
def m_chair_small():
    els = legs(3, 3, 13, 13, 6, 'm', w=1.5) + [box((2.5, 6, 2.5), (13.5, 7.5, 13.5), 'm')]
    els += [box((3, 7.5, 11.5), (4.5, 15, 13), 'm'), box((11.5, 7.5, 11.5), (13, 15, 13), 'm'), box((3, 12, 11.75), (13, 14.5, 12.75), 'm'),
            box((3, 9, 11.75), (13, 10, 12.75), 'm')]
    return els


@spec('chair_big_lower', {'m': 'maple'}, 'maple')
def m_chair_big_lower():
    """The same chair at twice the size: legs, rails and the seat, whose top is the cell's top."""
    els = legs(1, 1, 15, 15, 13, 'm', w=3) + [box((1, 13, 1), (15, 16, 15), 'm')]
    els += [box((2.5, 5, 2.5), (13.5, 6, 3.5), 'm'), box((2.5, 5, 12.5), (13.5, 6, 13.5), 'm')]
    return els


@spec('chair_big_upper', {'m': 'maple'}, 'maple')
def m_chair_big_upper():
    return [box((1, 0, 12), (4, 15, 15), 'm'), box((12, 0, 12), (15, 15, 15), 'm'), box((4, 9, 12.5), (12, 13, 14.5), 'm'),
            box((4, 3, 12.5), (12, 5, 14.5), 'm'), box((.5, 15, 11.5), (15.5, 16, 15.5), 'm')]


# ---------------------------------------------------------------- the childhood room: a bed at twice the size, crayons, stars
@spec('bigbed_head', {'h': 'headboard', 'q': 'quilt', 'p': 'pillow'}, 'quilt')
def m_bigbed_head():
    return [box((0, 0, 0), (16, 6, 14), 'h'), box((0, 6, 0), (16, 13, 14), 'p'), box((0, 13, 0), (16, 16, 7), 'q'),
            box((1, 13, 7), (15, 17, 13.5), 'p'), box((0, 0, 14), (16, 16, 16), 'h')]


@spec('bigbed_headboard', {'h': 'headboard'}, 'headboard')
def m_bigbed_headboard():
    return [box((0, 0, 14), (16, 14, 16), 'h'), box((-.5, 14, 13.5), (16.5, 15.5, 16.5), 'h')]


@spec('bigbed_mid', {'h': 'headboard', 'q': 'quilt', 'p': 'pillow'}, 'quilt')
def m_bigbed_mid():
    return [box((0, 0, 0), (16, 6, 16), 'h'), box((0, 6, 0), (16, 13, 16), 'p'), box((0, 13, 0), (16, 16, 16), 'q')]


@spec('bigbed_foot', {'h': 'headboard', 'q': 'quilt', 'p': 'pillow'}, 'quilt')
def m_bigbed_foot():
    return [box((0, 0, 3), (16, 6, 16), 'h'), box((0, 6, 3), (16, 13, 16), 'p'), box((0, 13, 3), (16, 16, 16), 'q'),
            box((0, 0, 0), (16, 22, 3), 'h')]


def crayon_model():
    return [box((0, 0, 5.5), (12, 5, 10.5), 'c'), box((12, .75, 6.25), (14.5, 4.25, 9.75), 'c', uv=(0, 0, 3, 16)),
            box((14.5, 1.75, 7.25), (16, 3.25, 8.75), 'c', uv=(0, 0, 2, 16))]


@spec('crayon_red', {'c': 'crayon_red'}, 'crayon_red')
def m_crayon_red():
    return crayon_model()


@spec('crayon_blue', {'c': 'crayon_blue'}, 'crayon_blue')
def m_crayon_blue():
    return crayon_model()


@spec('drawing', {'d': 'drawing'}, 'drawing')
def m_drawing():
    return [box((1, 2, 15.5), (15, 14, 16), 'd', uv=(0, 0, 16, 16))]


@spec('night_light', {'n': 'moon_light'}, 'moon_light')
def m_night_light():
    return [box((5, 2, 14.5), (11, 9, 16), 'n', uv=(0, 0, 16, 16), glow=12)]


# ---------------------------------------------------------------- the teenage room
@spec('poster_band', {'p': 'poster_band'}, 'poster_band')
def m_poster_band():
    return [box((1, 0, 15.6), (15, 16, 16), 'p', uv=(0, 0, 16, 16))]


@spec('poster_moon', {'p': 'poster_moon'}, 'poster_moon')
def m_poster_moon():
    return [box((1, 0, 15.6), (15, 16, 16), 'p', uv=(0, 0, 16, 16))]


@spec('stereo', {'b': 'plastic_black', 's': 'stereo', 'i': 'silver'}, 'plastic_black')
def m_stereo():
    return [box((1, 0, 3), (15, 9, 15), 'b'), box((2, 9, 5), (14, 15, 11), 'b', over={'north': 's'}),
            box((4, 15, 7.5), (5, 17, 8.5), 'i'), box((11, 15, 7.5), (12, 17, 8.5), 'i'), box((4, 17, 7.5), (12, 18, 8.5), 'i')]


@spec('lava_lamp', {'b': 'plastic_black', 'l': 'lava', 'i': 'silver'}, 'lava')
def m_lava_lamp():
    return [box((2, 0, 2), (14, 9, 14), 'b'), box((6, 9, 6), (10, 11, 10), 'i'), box((6.5, 11, 6.5), (9.5, 17, 9.5), 'l', glow=10),
            box((7, 17, 7), (9, 18.5, 9), 'i')]


@spec('diary', {'w': 'white_paint', 'd': 'diary'}, 'diary')
def m_diary():
    return [box((2, 0, 3), (14, 10, 15), 'w'), box((4, 10, 5), (11, 11.5, 11), 'd')]


@spec('clothes', {'c': 'clothes'}, 'clothes')
def m_clothes():
    return [box((1, 0, 2), (15, 2.5, 14), 'c'), box((3, 2.5, 4), (12, 4.5, 11), 'c'), box((5, 4.5, 5), (10, 5.5, 9), 'c')]


@spec('keep_out', {'k': 'keep_out'}, 'keep_out')
def m_keep_out():
    return [box((3.5, 5, 15.6), (12.5, 12, 16), 'k', uv=(0, 0, 16, 16))]


def iron_end(z0, top):
    els = [box((0, 0, z0), (1.5, top, z0 + 1.5), 'i'), box((14.5, 0, z0), (16, top, z0 + 1.5), 'i'), box((1.5, top - 2, z0), (14.5, top - .5, z0 + 1.5), 'i'),
           box((1.5, 4, z0), (14.5, 5, z0 + 1.5), 'i')]
    els += slats_x(3, 14, z0 + .25, z0 + 1.25, 5, top - 2, 3, 1, 'i')
    return els


@spec('teen_bed_head', {'i': 'iron', 'g': 'sheet_grey', 'd': 'duvet_purple', 'p': 'pillow'}, 'duvet_purple')
def m_teen_bed_head():
    return iron_end(14.5, 15) + [box((1, 4, 0), (15, 8, 14.5), 'g'), box((.5, 8, 0), (15.5, 9.5, 10), 'd'), box((.5, 5, 0), (1, 8, 10), 'd'),
                                 box((15, 5, 0), (15.5, 8, 10), 'd'), box((2, 8, 10), (14, 10.5, 14), 'p')]


@spec('teen_bed_foot', {'i': 'iron', 'g': 'sheet_grey', 'd': 'duvet_purple'}, 'duvet_purple')
def m_teen_bed_foot():
    return iron_end(0, 11) + [box((1, 4, 1.5), (15, 8, 16), 'g'), box((.5, 8, 1.5), (15.5, 9.5, 16), 'd'), box((.5, 5, 1.5), (1, 8, 16), 'd'),
                              box((15, 5, 1.5), (15.5, 8, 16), 'd'), box((3, 9.5, 6), (10, 10.5, 12), 'd')]


# ---------------------------------------------------------------- the first flat
@spec('box', {'c': 'cardboard', 't': 'cardboard_top'}, 'cardboard')
def m_box():
    return [box((1, 0, 1), (15, 13, 15), 'c', over={'up': 't'})]


@spec('box_kitchen', {'c': 'cardboard', 't': 'cardboard_top', 'k': 'box_kitchen'}, 'cardboard')
def m_box_kitchen():
    return [box((1, 0, 1), (15, 13, 15), 'c', over={'up': 't', 'north': 'k'})]


@spec('box_open', {'c': 'cardboard', 'o': 'box_open'}, 'cardboard')
def m_box_open():
    return [box((1, 0, 1), (15, 12, 2), 'c'), box((1, 0, 14), (15, 12, 15), 'c'), box((1, 0, 2), (2, 12, 14), 'c'), box((14, 0, 2), (15, 12, 14), 'c'),
            box((2, 9, 2), (14, 10, 14), 'c', over={'up': 'o'}), box((1, 12, -2.5), (15, 12.5, 1), 'c'), box((1, 12, 15), (15, 12.5, 18.5), 'c')]


@spec('mattress_head', {'t': 'ticking', 's': 'sleeping_bag', 'p': 'pillow'}, 'ticking')
def m_mattress_head():
    return [box((0, 0, 0), (16, 4, 16), 't'), box((1, 4, 0), (15, 6, 10.5), 's'), box((2, 4, 11), (14, 6.5, 15), 'p')]


@spec('mattress_foot', {'t': 'ticking', 's': 'sleeping_bag'}, 'ticking')
def m_mattress_foot():
    return [box((0, 0, 0), (16, 4, 16), 't'), box((1, 4, 1.5), (15, 6, 16), 's'), box((2, 4, .5), (14, 5.5, 1.5), 's')]


@spec('hot_plate', {'c': 'cardboard', 't': 'cardboard_top', 'h': 'hot_plate', 'w': 'white_paint'}, 'cardboard')
def m_hot_plate():
    return [box((1, 0, 1), (15, 12, 15), 'c', over={'up': 't'}), box((3, 12, 4), (13, 14, 12), 'w', over={'up': 'h'})]


@spec('bulb', {'b': 'plastic_black', 'l': 'bulb'}, 'bulb')
def m_bulb():
    return [box((7.75, 8, 7.75), (8.25, 16, 8.25), 'b'), box((7, 6.5, 7), (9, 8, 9), 'b'), box((6.5, 3, 6.5), (9.5, 6.5, 9.5), 'l', glow=15)]


@spec('letter', {'l': 'letter'}, 'letter')
def m_letter():
    return [box((3, 0, 4.5), (13, .5, 11.5), 'l', faces=('up', 'down', 'north', 'south', 'east', 'west'), uv=(0, 0, 16, 16))]


# ---------------------------------------------------------------- the hospital room
def hosp_base(z0, z1):
    els = [box((1, 3, z0), (15, 5, z1), 'c'), box((1, 5, z0), (15, 9, z1), 's')]
    els += [box((x, 0, z), (x + 2, 3, z + 2), 'c') for x in (1.5, 12.5) for z in ((z0 + .5, z1 - 2.5))]
    return els


@spec('hosp_bed_head', {'c': 'chrome', 's': 'hosp_sheet', 'b': 'cellular', 'p': 'pillow', 'h': 'bed_panel'}, 'cellular')
def m_hosp_bed_head():
    return hosp_base(0, 14) + [box((.5, 9, 0), (15.5, 10, 9), 'b'), box((2, 9, 9), (14, 11.5, 13.5), 'p'), box((0, 3, 14), (16, 16, 15.5), 'h'),
                               box((0, 9, 2), (.75, 12, 12), 'c'), box((15.25, 9, 2), (16, 12, 12), 'c')]


@spec('hosp_bed_foot', {'c': 'chrome', 's': 'hosp_sheet', 'b': 'cellular', 'h': 'bed_panel', 'k': 'chart'}, 'cellular')
def m_hosp_bed_foot():
    return hosp_base(1.5, 16) + [box((.5, 9, 1.5), (15.5, 10, 16), 'b'), box((0, 3, 0), (16, 12, 1.5), 'h'),
                                 box((4.5, 5, -.6), (11.5, 11.5, 0), 'k', uv=(0, 0, 16, 16)), box((0, 9, 4), (.75, 12, 14), 'c'), box((15.25, 9, 4), (16, 12, 14), 'c')]


@spec('iv_lower', {'c': 'chrome'}, 'chrome')
def m_iv_lower():
    return [box((3, 0, 7.25), (13, 1, 8.75), 'c'), box((7.25, 0, 3), (8.75, 1, 13), 'c'), box((7.5, 1, 7.5), (8.5, 16, 8.5), 'c')]


@spec('iv_upper', {'c': 'chrome', 'v': 'iv_bag'}, 'iv_bag')
def m_iv_upper():
    return [box((7.5, 0, 7.5), (8.5, 13, 8.5), 'c'), box((4, 12, 7.6), (12, 12.6, 8.4), 'c'), box((3.5, 5, 7), (6.5, 11.5, 9), 'v', uv=(0, 0, 16, 16)),
            box((4.75, 0, 7.75), (5.25, 5, 8.25), 'v', uv=(7, 14, 8, 16))]


@spec('monitor', {'c': 'chrome', 'p': 'beige_plastic', 's': 'monitor_screen'}, 'beige_plastic')
def m_monitor():
    return [box((4, 0, 7.25), (12, 1, 8.75), 'c'), box((7.25, 0, 4), (8.75, 1, 12), 'c'), box((7.5, 1, 7.5), (8.5, 8, 8.5), 'c'),
            box((3, 8, 5), (13, 15, 12), 'p'), box((4, 9, 4.8), (12, 14, 5), 's', faces=('north',), uv=(0, 0, 16, 16), glow=9)]


@spec('visitor_chair', {'c': 'chrome', 'v': 'vinyl'}, 'vinyl')
def m_visitor_chair():
    return [box((2.5, 0, 2.5), (3.5, 6, 3.5), 'c'), box((12.5, 0, 2.5), (13.5, 6, 3.5), 'c'), box((2.5, 0, 12.5), (3.5, 6, 13.5), 'c'),
            box((12.5, 0, 12.5), (13.5, 6, 13.5), 'c'), box((2, 6, 2), (14, 9, 14), 'v'), box((2, 9, 12), (14, 19, 14), 'v')]


@spec('carnations', {'p': 'beige_plastic', 'g': 'ceramic', 'f': 'carnations', 's': 'stems'}, 'carnations')
def m_carnations():
    return [box((2, 0, 3), (14, 11, 15), 'p'), box((6, 11, 7), (10, 16, 11), 'g'), box((7, 16, 8), (9, 20, 10), 's'),
            box((5, 20, 6), (11, 23, 12), 'f')]


# ---------------------------------------------------------------- Mother's room and the hall
@spec('mother_bed_head', {'w': 'walnut', 'c': 'candlewick', 'p': 'pillow'}, 'candlewick')
def m_mother_bed_head():
    return [box((0, 0, 14), (16, 19, 16), 'w'), box((0, 2, 0), (16, 6, 14), 'w'), box((.5, 6, 0), (15.5, 9, 14), 'c'),
            box((-.25, 4, 0), (16.25, 10, 9.5), 'c'), box((1.5, 9, 10), (7.5, 12, 13.5), 'p'), box((8.5, 9, 10), (14.5, 12, 13.5), 'p')]


@spec('mother_bed_foot', {'w': 'walnut', 'c': 'candlewick'}, 'candlewick')
def m_mother_bed_foot():
    return [box((0, 0, 0), (16, 12, 2), 'w'), box((0, 2, 2), (16, 6, 16), 'w'), box((-.25, 4, 2), (16.25, 10, 16), 'c')]


@spec('wardrobe_lower', {'w': 'walnut'}, 'walnut')
def m_wardrobe_lower():
    return [box((0, 0, 2), (16, 16, 16), 'w'), box((-.5, 0, 1.5), (16.5, 1.5, 16), 'w'), box((7.75, 1.5, 1.6), (8.25, 16, 2), 'w')]


@spec('wardrobe_upper', {'w': 'walnut', 'm': 'mirror'}, 'walnut')
def m_wardrobe_upper():
    return [box((0, 0, 2), (16, 14, 16), 'w'), box((-.5, 14, 1.5), (16.5, 16, 16), 'w'), box((3, 1, 1.75), (13, 13, 2), 'm', faces=('north',), uv=(0, 0, 16, 16))]


@spec('notebook', {'m': 'maple', 'n': 'notebook', 'w': 'white_paint'}, 'maple')
def m_notebook():
    return legs(2, 4, 14, 14, 10, 'm') + [box((1, 10, 3), (15, 12, 15), 'm'), box((4, 12, 6), (11, 13, 11), 'w', over={'up': 'n'})]


def plaque(n):
    return [box((4.5, 3, 15.5), (11.5, 11, 16), f'n', uv=(0, 0, 16, 16))]


for _n in range(1, 7):
    spec(f'number_{_n}', {'n': f'number_{_n}'}, f'number_{_n}')(lambda: plaque(0))


KINDS = list(MODEL_SPECS)
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def model_json(name):
    fn, textures, particle = MODEL_SPECS[name]
    tex = {k: NS + v for k, v in textures.items()}
    tex['particle'] = NS + particle
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False, 'textures': tex, 'elements': fn()}


def star_ceiling_model():
    """The childhood room's ceiling: blue paint, and on its underside the stickers, which glow."""
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
            'textures': {'all': NS + 'star_ceiling', 'stars': NS + 'stars', 'particle': NS + 'star_ceiling'},
            'elements': [box((0, 0, 0), (16, 16, 16), 'all'),
                         box((0, -.02, 0), (16, 0, 16), 'stars', faces=('down',), uv=(0, 0, 16, 16), glow=10)]}


def files():
    """Every file this tool owns, as {path: bytes-or-image}."""
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
    out[STATES / 'life_stage.json'] = json.dumps({'variants': variants}, indent=1) + '\n'
    for m in MATERIALS:
        out[STATES / f'life_{m}.json'] = json.dumps({'variants': {'': {'model': NS + m}}}, indent=2) + '\n'
        out[MODELS / f'{m}.json'] = json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + m}}, indent=2) + '\n'
    out[STATES / 'life_star_ceiling.json'] = json.dumps({'variants': {'': {'model': NS + 'star_ceiling'}}}, indent=2) + '\n'
    out[MODELS / 'star_ceiling.json'] = json.dumps(star_ceiling_model(), indent=1) + '\n'
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
    print(f'{"Verified" if args.check else "Drew"} {len(TEXTURES)} life-stage textures, {len(KINDS)} kinds and {len(MATERIALS) + 1} materials.')


if __name__ == '__main__':
    main()
