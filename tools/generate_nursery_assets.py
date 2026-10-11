#!/usr/bin/env python3
"""The child's room (0.4.74): vanilla-style 16x16 textures, block models and states for every piece of furniture and every
toy, the nursery wallpaper, and twelve original synthesized sounds with subtitles.

Textures follow Minecraft's own look: a small palette per material, one-pixel bevels and plank seams, scattered single-pixel
noise, nothing smooth. Nothing is sampled. Run from anywhere; it rewrites only its own files and keys.
"""
import json
import math
import random
from pathlib import Path

import numpy as np
import soundfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
ASSETS = ROOT / 'assets/the_oldest_house'
TEX = ASSETS / 'textures/block'
MODELS = ASSETS / 'models/block'
RATE = 22050


# ------------------------------------------------------------------------------------------------ textures
def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c))


def canvas(color=(0, 0, 0, 0)):
    return Image.new('RGBA', (16, 16), color)


def px(img, x, y, c, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), clamp(c) + (a,))


def planks(base, seed):
    """Painted planks, as vanilla planks: four boards, a seam under each, a few knots and grain specks."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        board = y // 4
        for x in range(16):
            f = 1.0 + rng.choice((0, 0, 0, -.04, .04))
            if y % 4 == 3:
                f = .72  # the seam
            elif y % 4 == 0:
                f += .06  # the lit top edge of each board
            if (x + board * 5) % 16 == 15 and y % 4 != 3:
                f = .82  # board ends, staggered
            px(img, x, y, mul(base, f))
    for _ in range(6):
        x, y = rng.randrange(16), rng.randrange(16)
        if y % 4 != 3:
            px(img, x, y, mul(base, .9))
    return img


def wallpaper():
    """Pale blue nursery paper with small white stars and yellow dots, tiling seamlessly."""
    rng = random.Random(474)
    base = (176, 202, 222)
    img = canvas()
    for y in range(16):
        for x in range(16):
            f = 1 + (.03 if (x + y) % 2 == 0 else -.02) + rng.choice((0, 0, 0, -.03))
            px(img, x, y, mul(base, f))
    for sx, sy in ((3, 4), (11, 12)):
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
            px(img, sx + dx, sy + dy, (244, 244, 236))
        px(img, sx, sy, (255, 255, 250))
    for dx, dy in ((12, 3), (5, 11), (8, 7)):
        px(img, dx, dy, (238, 210, 110))
    return img


def quilt():
    """Patchwork: four-pixel squares in four colours with a stitch at each corner."""
    colours = [(196, 70, 64), (236, 226, 200), (72, 112, 190), (228, 190, 76)]
    rng = random.Random(475)
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = colours[((x // 4) + (y // 4) * 2) % 4]
            f = 1 + rng.choice((0, 0, -.05, .04))
            if x % 4 == 0 or y % 4 == 0:
                f -= .08
            px(img, x, y, mul(c, f))
    for y in range(0, 16, 4):
        for x in range(0, 16, 4):
            px(img, x, y, (250, 246, 236))
    return img


def mattress():
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (232, 226, 206) if x % 4 else (150, 170, 206)
            px(img, x, y, mul(c, .96 if y % 2 else 1.0))
    return img


SWATCHES = [(196, 52, 48), (60, 104, 196), (232, 192, 56), (80, 160, 80),
            (236, 232, 224), (232, 150, 170), (148, 98, 56), (98, 62, 34),
            (36, 34, 38), (222, 178, 64), (180, 184, 190), (238, 220, 180),
            (236, 196, 160), (140, 96, 180), (232, 128, 48), (120, 120, 124)]
SWATCH = {name: i for i, name in enumerate(('red', 'blue', 'yellow', 'green', 'white', 'pink', 'fur', 'darkfur',
                                             'black', 'gold', 'silver', 'cream', 'skin', 'purple', 'orange', 'grey'))}


def swatches():
    """Sixteen four-pixel colour cells, each bevelled lighter top-left and darker bottom-right, for small toy parts."""
    rng = random.Random(476)
    img = canvas()
    for i, c in enumerate(SWATCHES):
        ox, oy = (i % 4) * 4, (i // 4) * 4
        for y in range(4):
            for x in range(4):
                f = 1.0
                if x == 0 or y == 0:
                    f = 1.1
                if x == 3 or y == 3:
                    f = .86
                if rng.random() < .15:
                    f -= .05
                px(img, ox + x, oy + y, mul(c, f))
    return img


def faces():
    """Four eight-pixel faces: the bear, the doll, the jack and the horse's head."""
    img = canvas()
    fur, dark, skin = (148, 98, 56), (98, 62, 34), (236, 196, 160)
    for y in range(8):
        for x in range(8):
            px(img, x, y, mul(fur, 1.05 if (x + y) % 3 == 0 else 1))
            px(img, 8 + x, y, skin)
            px(img, x, 8 + y, (240, 236, 228))
            px(img, 8 + x, 8 + y, (236, 232, 224))
    for x in range(2, 6):  # the bear's muzzle
        for y in range(4, 7):
            px(img, x, y, (196, 156, 110))
    for x, y in ((2, 2), (5, 2), (3, 5), (4, 5)):
        px(img, x, y, (20, 18, 20))
    for x in range(8):  # the doll's hair and face
        px(img, 8 + x, 0, (236, 200, 80))
        px(img, 8 + x, 1, (226, 188, 70))
    px(img, 8, 2, (226, 188, 70)); px(img, 15, 2, (226, 188, 70))
    px(img, 10, 3, (60, 100, 196)); px(img, 13, 3, (60, 100, 196))
    px(img, 11, 6, (196, 60, 70)); px(img, 12, 6, (196, 60, 70))
    px(img, 9, 5, (236, 160, 160)); px(img, 14, 5, (236, 160, 160))
    for x in range(8):  # the jack's hat brim, eyes, red nose and grin
        px(img, x, 8, (140, 96, 180))
    px(img, 2, 10, (20, 18, 20)); px(img, 5, 10, (20, 18, 20))
    px(img, 3, 11, (210, 40, 40)); px(img, 4, 11, (210, 40, 40))
    for x in range(1, 7):
        px(img, x, 13, (170, 40, 50))
    px(img, 1, 12, (170, 40, 50)); px(img, 6, 12, (170, 40, 50))
    for y in range(8):  # the horse: dark mane down the back of the head, an eye, a nostril
        px(img, 14, 8 + y, (70, 48, 30)); px(img, 15, 8 + y, (60, 40, 26))
    px(img, 10, 10, (20, 18, 20)); px(img, 8, 14, (120, 100, 90))
    return img


def books():
    """A low shelf front: two rows of spines in mixed colours and heights between painted boards."""
    rng = random.Random(477)
    img = canvas()
    board = (232, 228, 216)
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(board, .62))  # the shadowed back of the shelf
    for y in (0, 7, 8, 15):
        for x in range(16):
            px(img, x, y, mul(board, 1.0 if y in (0, 8) else .8))
    for row, (top, bottom) in enumerate(((1, 6), (9, 14))):
        x = 0
        while x < 16:
            w = rng.choice((1, 2, 2, 3))
            c = rng.choice(SWATCHES[:6] + [SWATCHES[13], SWATCHES[14]])
            h = rng.randint(0, 2)
            for xx in range(x, min(16, x + w)):
                for y in range(top + h, bottom + 1):
                    f = 1.0 if xx == x else .88
                    px(img, xx, y, mul(c, f))
                px(img, xx, top + h + 1, mul(c, 1.25))  # a title band
            x += w
    return img


def rug():
    """A play mat: a grey road crossing a green town, with two little houses."""
    img = canvas()
    grass, road = (104, 164, 84), (120, 120, 124)
    for y in range(16):
        for x in range(16):
            c = road if 6 <= x <= 9 or 6 <= y <= 9 else grass
            f = 1 + (.04 if (x * 7 + y * 3) % 5 == 0 else 0)
            px(img, x, y, mul(c, f))
    for i in range(0, 16, 4):
        px(img, i + 1, 8, (240, 236, 228)); px(img, i + 2, 8, (240, 236, 228))
        px(img, 8, i + 1, (240, 236, 228)); px(img, 8, i + 2, (240, 236, 228))
    for hx, hy, c in ((1, 1, (196, 70, 64)), (11, 11, (72, 112, 190))):
        for y in range(3):
            for x in range(4):
                px(img, hx + x, hy + 1 + y, c)
        for x in range(4):
            px(img, hx + x, hy, (110, 60, 40))
    return img


def track():
    """Wooden toy track running north-south: two grooves, the rest of the cell clear."""
    img = canvas()
    wood = (214, 176, 120)
    for y in range(16):
        for x in range(3, 13):
            f = .7 if x in (5, 10) else (1.06 if x in (3, 12) else 1)
            if y % 8 == 7:
                f -= .1
            px(img, x, y, mul(wood, f))
    return img


def drawing(below):
    """Crayon on paper. Above: a house with windows and no door. Below: the same house from underneath, and a ladder."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (238, 232, 212) if (x + y) % 5 else (230, 224, 204))
    red, blue, yellow, brown = (206, 60, 50), (60, 96, 200), (236, 196, 50), (110, 70, 40)
    if not below:
        for x in range(4, 12):
            px(img, x, 13, red)
        for y in range(7, 14):
            px(img, 4, y, red); px(img, 11, y, red)
        for i in range(5):
            px(img, 4 + i, 6 - i, brown); px(img, 11 - i, 6 - i, brown)
        for x, y in ((6, 9), (9, 9)):
            for dx in range(2):
                for dy in range(2):
                    px(img, x + dx, y + dy, blue)
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            px(img, 13 + dx, 1 + dy, yellow)
    else:
        for x in range(2, 14):
            px(img, x, 4, red)
        for y in range(4, 15):
            px(img, 7, y, brown); px(img, 10, y, brown)
        for y in range(6, 15, 2):
            px(img, 8, y, brown); px(img, 9, y, brown)
        for x in range(5, 13):
            px(img, x, 2, blue)
    return img


def dollhouse(inside):
    img = canvas()
    if not inside:
        for y in range(16):
            for x in range(16):
                px(img, x, y, (230, 168, 180) if (x + y) % 4 else (220, 156, 168))
        for wx, wy in ((2, 3), (10, 3), (2, 9)):
            for x in range(4):
                for y in range(4):
                    edge = x in (0, 3) or y in (0, 3)
                    px(img, wx + x, wy + y, (240, 236, 228) if edge else (90, 140, 196))
        for x in range(10, 14):  # the little front door, the one a real house would have
            for y in range(9, 16):
                px(img, x, y, (140, 86, 50) if x in (10, 13) or y == 9 else (170, 110, 64))
    else:
        for y in range(16):
            for x in range(16):
                wall = (236, 220, 190) if y < 8 else (200, 220, 236)
                px(img, x, y, wall)
        for x in range(16):
            px(img, x, 7, (150, 100, 60)); px(img, x, 15, (150, 100, 60))
        for x in range(3, 8):  # the little bed upstairs, pushed aside
            px(img, x, 5, (196, 70, 64)); px(img, x, 6, (236, 226, 200))
        px(img, 9, 6, (20, 18, 20)); px(img, 10, 6, (20, 18, 20))  # the gap in its floor
        for x in range(9, 14):
            for y in range(11, 15):
                px(img, x, y, (110, 160, 90))
    return img


def cardboard():
    rng = random.Random(478)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((176, 136, 90), 1 + rng.choice((0, 0, -.05, .04))))
    for x in range(16):
        px(img, x, 7, (200, 180, 140)); px(img, x, 8, (196, 176, 136))
    for x in range(3, 13, 2):
        px(img, x, 11, (40, 36, 40))
    return img


def mobile():
    """Cut-out stars and a moon on thin strings, the rest clear."""
    img = canvas()
    yellow, pale = (240, 210, 90), (230, 230, 210)
    for y in range(0, 6):
        px(img, 3, y, (40, 36, 40)); px(img, 12, y, (40, 36, 40))
    for cx, cy in ((3, 9),):
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (-1, -1), (1, -1), (-1, 1)):
            px(img, cx + dx, cy + dy, yellow)
        for dx, dy in ((2, 0), (-2, 0), (0, 2), (0, -2)):
            px(img, cx + dx, cy + dy, yellow)
    for dy in range(-3, 4):  # a crescent moon
        for dx in range(-3, 4):
            if dx * dx + dy * dy <= 10 and (dx - 1.6) ** 2 + (dy + .6) ** 2 > 5:
                px(img, 12 + dx, 9 + dy, pale)
    return img


def lamp(on):
    img = canvas()
    base = (250, 232, 170) if on else (200, 196, 184)
    for y in range(16):
        for x in range(16):
            f = .9 if y % 4 == 0 else 1.0
            px(img, x, y, mul(base, f))
    return img


def card():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (244, 240, 228))
    for y in (6, 9, 12):
        for x in range(2, 14 if y < 12 else 9):
            px(img, x, y, (70, 96, 170))
    for dx, dy in ((12, 2), (13, 2), (12, 3), (11, 3), (12, 4)):
        px(img, dx, dy, (230, 196, 80))
    return img


def exercise_book():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (64, 104, 180) if x > 1 else (40, 70, 130))
    for x in range(5, 13):
        for y in range(4, 9):
            px(img, x, y, (240, 236, 224))
    for x in range(6, 12):
        px(img, x, 6, (120, 120, 140))
    return img


def train_side():
    img = canvas()
    red, black, gold = (196, 52, 48), (36, 34, 38), (222, 178, 64)
    for y in range(16):
        for x in range(16):
            px(img, x, y, red if y < 12 else black)
    for x in range(16):
        px(img, x, 3, gold); px(img, x, 11, gold)
    for x in range(9, 14):
        for y in range(5, 9):
            px(img, x, y, (150, 196, 230))
    for cx in (3, 12):
        for dx in range(-2, 2):
            px(img, cx + dx, 13, (120, 120, 124)); px(img, cx + dx, 14, (90, 90, 96))
    return img


def wardrobe(open_):
    img = planks((96, 140, 196), 479)
    for y in range(16):
        px(img, 7, y, (60, 90, 140)); px(img, 8, y, (70, 104, 156))
    px(img, 6, 8, (230, 196, 80)); px(img, 9, 8, (230, 196, 80))
    for sx, sy in ((3, 3), (12, 12)):
        px(img, sx, sy, (244, 244, 236))
    return img


TEXTURES = {
    'child_wallpaper': wallpaper,
    'nursery_white_wood': lambda: planks((232, 228, 216), 480),
    'nursery_blue_wood': lambda: planks((96, 140, 196), 481),
    'nursery_red_wood': lambda: planks((190, 64, 56), 482),
    'nursery_yellow_wood': lambda: planks((226, 186, 72), 483),
    'nursery_quilt': quilt,
    'nursery_mattress': mattress,
    'nursery_swatches': swatches,
    'nursery_faces': faces,
    'nursery_books': books,
    'nursery_rug': rug,
    'nursery_track': track,
    'nursery_drawing': lambda: drawing(False),
    'nursery_drawing_below': lambda: drawing(True),
    'nursery_dollhouse': lambda: dollhouse(False),
    'nursery_dollhouse_inside': lambda: dollhouse(True),
    'nursery_cardboard': cardboard,
    'nursery_mobile': mobile,
    'nursery_lamp': lambda: lamp(True),
    'nursery_lamp_off': lambda: lamp(False),
    'nursery_card': card,
    'nursery_exercise_book': exercise_book,
    'nursery_train': train_side,
    'nursery_wardrobe': lambda: wardrobe(False),
}


# ------------------------------------------------------------------------------------------------ models
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


def sw(name):
    i = SWATCH[name]
    x, y = (i % 4) * 4, (i // 4) * 4
    return [x + .5, y + .5, x + 3.5, y + 3.5]


def box(frm, to, tex, uv=None, faces_=DIRS, overrides=None, rot=None):
    """One element. ``tex`` names a texture variable; ``uv`` is fixed for every face (swatches) or computed by the game."""
    faces = {}
    for d in faces_:
        face = {'texture': '#' + tex}
        if uv is not None:
            face['uv'] = uv
        faces[d] = face
    for d, (t, u) in (overrides or {}).items():
        faces[d] = {'texture': '#' + t, **({'uv': u} if u else {})}
    el = {'from': list(frm), 'to': list(to), 'faces': faces}
    if rot:
        el['rotation'] = rot
    return el


def color(frm, to, name, rot=None):
    return box(frm, to, 's', sw(name), rot=rot)


def model(name, elements, textures, particle=None):
    tex = {'s': 'the_oldest_house:block/nursery_swatches', **{k: f'the_oldest_house:block/{v}' for k, v in textures.items()}}
    tex['particle'] = 'the_oldest_house:block/' + (particle or textures.get('w', 'nursery_white_wood'))
    data = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False, 'textures': tex, 'elements': elements}
    (MODELS / f'nursery_{name}.json').write_text(json.dumps(data, indent=1) + '\n')


def rotated(elements, axis, angle, origin=(8, 0, 8)):
    for e in elements:
        e['rotation'] = {'origin': list(origin), 'axis': axis, 'angle': angle}
    return elements


def models():
    MODELS.mkdir(parents=True, exist_ok=True)
    W = {'w': 'nursery_white_wood', 'b': 'nursery_blue_wood', 'r': 'nursery_red_wood', 'y': 'nursery_yellow_wood'}
    # The loft-legged bed. Its legs reach down a block, so a child fits beneath the deck.
    model('bed_head', [box((0, 0, 0), (16, 3, 16), 'w'), box((1, 3, 1), (15, 6, 16), 'm'), box((1, 5.5, 7), (15, 6.5, 16), 'q'),
                       color((3, 6, 2), (13, 8, 6), 'white'), box((0, 0, 0), (16, 14, 2), 'b'),
                       box((0, -16, 0), (2, 0, 2), 'w'), box((14, -16, 0), (16, 0, 2), 'w'), color((7, 11, -0.01), (9, 13, 0), 'yellow')],
          {**W, 'm': 'nursery_mattress', 'q': 'nursery_quilt'})
    model('bed_foot', [box((0, 0, 0), (16, 3, 16), 'w'), box((1, 3, 0), (15, 6, 15), 'm'), box((1, 5.5, 0), (15, 6.5, 15), 'q'),
                       box((0.5, 2, 0), (1, 6.5, 15), 'q'), box((15, 2, 0), (15.5, 6.5, 15), 'q'), box((0, 0, 14), (16, 10, 16), 'b'),
                       box((0, -16, 14), (2, 0, 16), 'w'), box((14, -16, 14), (16, 0, 16), 'w')],
          {**W, 'm': 'nursery_mattress', 'q': 'nursery_quilt'})
    legs = lambda h, x0=1, x1=13, z0=2, z1=12, t='w': [box((x, 0, z), (x + 2, h, z + 2), t) for x in (x0, x1) for z in (z0, z1)]
    model('desk', [box((0, 10, 1), (16, 12, 15), 'y'), *legs(10), box((4, 7, 1), (12, 10, 2), 'r'), color((7.5, 8, .5), (8.5, 9, 1), 'gold')], W)
    model('chair', [box((3, 6, 3), (13, 8, 13), 'r'), *legs(6, 3, 11, 3, 11), box((3, 8, 11), (13, 15, 13), 'y')], W)
    chest = [box((1, 0, 3), (15, 8, 13), 'r'), color((4, 3, 2.9), (6, 5, 3), 'blue'), color((7, 3, 2.9), (9, 5, 3), 'yellow'), color((10, 3, 2.9), (12, 5, 3), 'green')]
    model('toy_chest_0', chest + [box((.5, 8, 2.5), (15.5, 9, 13.5), 'y')], W)
    model('toy_chest_1', chest + [box((.5, 8, 12.5), (15.5, 14, 13.5), 'y'), color((2, 7.9, 4), (14, 8, 12), 'black')], W)
    model('shelf', [box((0, 0, 6), (16, 15.5, 16), 'w', overrides={'north': ('k', None)}), box((0, 15.5, 6), (16, 16, 16.5), 'w')], {**W, 'k': 'nursery_books'})
    model('wardrobe_low_0', [box((1, 0, 4), (15, 16, 16), 'b', overrides={'north': ('d', None)})], {**W, 'd': 'nursery_wardrobe'})
    model('wardrobe_high_0', [box((1, 0, 4), (15, 14, 16), 'b', overrides={'north': ('d', None)}), box((.5, 13, 3.5), (15.5, 14, 16.5), 'w')], {**W, 'd': 'nursery_wardrobe'})
    clothes = [color((3, 4, 6), (5, 14, 7), 'pink'), color((6, 4, 6), (8, 13, 7), 'blue'), color((9, 5, 6), (11, 14, 7), 'yellow')]
    doors = lambda h: [box((1, 0, .5), (2, h, 4), 'b'), box((14, 0, .5), (15, h, 4), 'b')]
    model('wardrobe_low_1', [box((1, 0, 4), (15, 16, 16), 'b', overrides={'north': ('s', sw('black'))}), *clothes, *doors(16)], {**W})
    model('wardrobe_high_1', [box((1, 0, 4), (15, 14, 16), 'b', overrides={'north': ('s', sw('black'))}), box((.5, 13, 3.5), (15.5, 14, 16.5), 'w'),
                              color((2, 12, 5), (14, 12.5, 6), 'silver'), *doors(14)], {**W})
    roof = [color((1, 10, 4), (15, 12, 16), 'darkfur'), color((3, 12, 4), (13, 14, 16), 'darkfur'), color((5, 14, 4), (11, 15, 16), 'darkfur')]
    model('dollhouse_0', [box((1, 0, 4), (15, 10, 16), 'p', overrides={'north': ('h', None)}), *roof], {**W, 'h': 'nursery_dollhouse', 'p': 'nursery_dollhouse'}, 'nursery_dollhouse')
    model('dollhouse_1', [box((1, 0, 5), (15, 10, 16), 'p', overrides={'north': ('i', None)}), *roof, box((1, 0, 0), (2, 10, 5), 'h')],
          {**W, 'h': 'nursery_dollhouse', 'p': 'nursery_dollhouse', 'i': 'nursery_dollhouse_inside'}, 'nursery_dollhouse')
    horse = lambda: [box((2, 0, 1), (3, 2, 15), 'r'), box((13, 0, 1), (14, 2, 15), 'r'), color((5, 5, 3), (11, 10, 13), 'white'),
                     *[color((x, 2, z), (x + 1, 5, z + 1), 'white') for x in (5, 10) for z in (4, 11)], color((3, 2, 4), (13, 3, 5), 'red'), color((3, 2, 11), (13, 3, 12), 'red'),
                     box((6, 9, 1), (10, 14, 5), 's', sw('white'), overrides={'east': ('f', [8, 8, 16, 16]), 'west': ('f', [16, 8, 8, 16])}),
                     color((7, 11, 4), (9, 15, 6), 'darkfur'), color((5, 10, 6), (11, 11, 10), 'red'), color((7, 7, 13), (9, 10, 15), 'darkfur')]
    model('rocking_horse_0', horse(), {**W, 'f': 'nursery_faces'})
    model('rocking_horse_1', rotated(horse(), 'x', 22.5), {**W, 'f': 'nursery_faces'})
    model('rocking_horse_2', rotated(horse(), 'x', -22.5), {**W, 'f': 'nursery_faces'})
    model('night_light_0', [color((6, 0, 6), (10, 1, 10), 'white'), box((5, 1, 5), (11, 6, 11), 'l'), color((4.5, 6, 4.5), (11.5, 7, 11.5), 'pink')], {**W, 'l': 'nursery_lamp'}, 'nursery_lamp')
    model('night_light_1', [color((6, 0, 6), (10, 1, 10), 'white'), box((5, 1, 5), (11, 6, 11), 'l'), color((4.5, 6, 4.5), (11.5, 7, 11.5), 'pink')], {**W, 'l': 'nursery_lamp_off'}, 'nursery_lamp_off')
    model('ceiling_lamp', [box((4, 7, 4), (12, 12, 12), 'l'), color((7.5, 12, 7.5), (8.5, 16, 8.5), 'black')], {**W, 'l': 'nursery_lamp'}, 'nursery_lamp')
    model('mobile', [box((2, 14, 7.5), (14, 14.5, 8.5), 'w'), box((7.5, 14, 2), (8.5, 14.5, 14), 'w'), color((7.75, 14.5, 7.75), (8.25, 16, 8.25), 'black'),
                     box((1, 4, 8), (7, 14, 8), 'o', [0, 0, 8, 16], faces_=('north', 'south')), box((9, 4, 8), (15, 14, 8), 'o', [8, 0, 16, 16], faces_=('north', 'south')),
                     box((8, 4, 1), (8, 14, 7), 'o', [0, 0, 8, 16], faces_=('east', 'west')), box((8, 4, 9), (8, 14, 15), 'o', [8, 0, 16, 16], faces_=('east', 'west'))],
          {**W, 'o': 'nursery_mobile'})
    model('rug', [box((0, 0, 0), (16, 1, 16), 'g', overrides={d: ('s', sw('grey')) for d in ('north', 'south', 'east', 'west')})], {**W, 'g': 'nursery_rug'}, 'nursery_rug')
    model('track', [box((0, 0, 0), (16, .5, 16), 't', faces_=('up', 'down'))], {**W, 't': 'nursery_track'}, 'nursery_track')
    model('drawings_0', [box((1, 2, 15), (15, 14, 16), 'p', overrides={d: ('s', sw('cream')) for d in ('east', 'west', 'up', 'down', 'south')}), color((7.5, 12.5, 14.8), (8.5, 13.5, 15), 'red')],
          {**W, 'p': 'nursery_drawing'}, 'nursery_drawing')
    model('drawings_1', [box((1, 2, 15), (15, 14, 16), 'p', overrides={d: ('s', sw('cream')) for d in ('east', 'west', 'up', 'down', 'south')}), color((7.5, 12.5, 14.8), (8.5, 13.5, 15), 'blue')],
          {**W, 'p': 'nursery_drawing_below'}, 'nursery_drawing_below')
    model('card', [box((5, 0, 7), (11, 3, 9), 'c'), box((5, 3, 7.5), (11, 3.5, 8.5), 'c')], {**W, 'c': 'nursery_card'}, 'nursery_card')
    model('account', [box((3, 0, 4), (13, 1.5, 12), 'e', overrides={d: ('s', sw('white')) for d in ('north', 'south', 'east')})], {**W, 'e': 'nursery_exercise_book'}, 'nursery_exercise_book')
    model('boxes', [box((1, 0, 1), (15, 13, 15), 'c'), box((1, 13, 1), (15, 13.5, 15), 'c')], {**W, 'c': 'nursery_cardboard'}, 'nursery_cardboard')
    bars = [box((x, 2, z), (x + 1, 10, z + 1), 'w') for x in range(3, 14, 2) for z in (2.5, 12.5)]
    model('crib', [*[box((x, 0, z), (x + 2, 12, z + 2), 'w') for x in (0, 14) for z in (2, 12)], box((0, 10, 2), (16, 12, 4), 'w'), box((0, 10, 12), (16, 12, 14), 'w'),
                   *bars, box((2, 2, 4), (14, 4, 12), 'm'), box((0, 2, 4), (2, 12, 12), 'w'), box((14, 2, 4), (16, 12, 12), 'w')], {**W, 'm': 'nursery_mattress'})
    model('train', [color((4, 1, 1), (12, 3, 15), 'black'), *[color((x, 0, z), (x + 1, 3, z + 3), 'grey') for x in (3, 12) for z in (3, 10)],
                    box((5, 3, 1), (11, 8, 9), 't', overrides={'north': ('s', sw('black'))}), box((4, 3, 9), (12, 10, 15), 't'),
                    color((7, 8, 2), (9, 11, 4), 'black'), color((3.5, 10, 8.5), (12.5, 11, 15.5), 'gold')], {**W, 't': 'nursery_train'}, 'nursery_train')
    top = lambda: [color((7, 0, 7), (9, 2, 9), 'silver'), color((5, 2, 5), (11, 4, 11), 'red'), color((5.5, 4, 5.5), (10.5, 6, 10.5), 'yellow'), color((7.5, 6, 7.5), (8.5, 8, 8.5), 'black')]
    model('top_0', top(), W)
    model('top_1', rotated(top(), 'z', 45, (8, 3, 8)), W)
    jack = [color((4, 0, 4), (12, 8, 12), 'blue'), color((12, 4, 7.5), (14, 5, 8.5), 'silver'), color((13, 4, 6.5), (14, 6, 7.5), 'red')]
    model('jack_box_0', jack + [color((3.5, 8, 3.5), (12.5, 9, 12.5), 'red')], W)
    model('jack_box_1', jack + [color((3.5, 8, 12), (12.5, 14, 13), 'red'), color((7, 8, 7), (9, 11, 9), 'silver'),
                                box((5.5, 11, 5.5), (10.5, 15, 10.5), 's', sw('white'), overrides={'north': ('f', [0, 8, 8, 16])}), color((6.5, 15, 6.5), (9.5, 16, 9.5), 'purple')],
          {**W, 'f': 'nursery_faces'})
    model('music_box_0', [color((4, 0, 5), (12, 4, 11), 'pink'), color((3.5, 4, 4.5), (12.5, 5, 11.5), 'gold')], W)
    model('music_box_1', [color((4, 0, 5), (12, 4, 11), 'pink'), color((3.5, 4, 10.5), (12.5, 10, 11.5), 'gold'), color((5, 3.9, 6), (11, 4, 10), 'black'), color((7.5, 4, 7.5), (8.5, 7, 8.5), 'pink')], W)
    model('teddy', [color((5, 0, 5), (11, 6, 10), 'fur'), box((5.5, 6, 5), (10.5, 10, 9), 's', sw('fur'), overrides={'north': ('f', [0, 0, 8, 8])}),
                    color((5, 9, 6), (6.5, 10.5, 7.5), 'darkfur'), color((9.5, 9, 6), (11, 10.5, 7.5), 'darkfur'),
                    color((4, 2, 6), (5, 5, 8), 'fur'), color((11, 2, 6), (12, 5, 8), 'fur'), color((5, 0, 3), (7, 2, 5), 'darkfur'), color((9, 0, 3), (11, 2, 5), 'darkfur')],
          {**W, 'f': 'nursery_faces'})
    model('doll', [color((6, 0, 6), (10, 5, 10), 'pink'), color((6.5, 5, 6.5), (9.5, 7, 9.5), 'pink'),
                   box((6, 7, 6), (10, 10, 10), 's', sw('skin'), overrides={'north': ('f', [8, 0, 16, 8])}), color((5.8, 9, 6.5), (10.2, 10.5, 10.2), 'yellow'),
                   color((5.5, 4, 7), (6.5, 7, 8), 'skin'), color((9.5, 4, 7), (10.5, 7, 8), 'skin')], {**W, 'f': 'nursery_faces'})
    model('blocks_0', [color((3, 0, 3), (6, 3, 6), 'red'), color((9, 0, 4), (12, 3, 7), 'blue'), color((5, 0, 9), (8, 3, 12), 'yellow'), color((10, 0, 10), (13, 3, 13), 'green')], W)
    model('blocks_1', [color((5, 0, 5), (9, 4, 9), 'red'), color((5, 4, 5), (9, 8, 9), 'blue'), color((10, 0, 10), (13, 3, 13), 'yellow')], W)
    model('blocks_2', [color((6, 0, 6), (10, 4, 10), 'red'), color((6, 4, 6), (10, 8, 10), 'blue'), color((6, 8, 6), (10, 12, 10), 'yellow')], W)
    model('ball', [color((5, 0, 5), (11, 6, 11), 'red'), color((4.9, 2.5, 4.9), (11.1, 3.5, 11.1), 'white')], W)


KINDS = ['bed_head', 'bed_foot', 'desk', 'chair', 'toy_chest', 'shelf', 'wardrobe_low', 'wardrobe_high', 'dollhouse', 'rocking_horse', 'night_light',
         'ceiling_lamp', 'mobile', 'rug', 'track', 'drawings', 'card', 'account', 'boxes', 'crib', 'train', 'top', 'jack_box', 'music_box', 'teddy',
         'blocks', 'ball', 'doll']
STAGED = {'toy_chest': 2, 'wardrobe_low': 2, 'wardrobe_high': 2, 'dollhouse': 2, 'rocking_horse': 3, 'night_light': 2, 'drawings': 2,
          'top': 2, 'jack_box': 2, 'music_box': 2, 'blocks': 3}
CEILING = {'teddy', 'doll', 'blocks', 'ball'}
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def blockstate():
    variants = {}
    for facing, y in TURN.items():
        for kind in KINDS:
            for stage in range(4):
                if kind in CEILING and stage == 3:
                    name = f'nursery_{kind}_0' if kind in STAGED else f'nursery_{kind}'
                    variants[f'facing={facing},kind={kind},stage={stage}'] = {'model': f'the_oldest_house:block/{name}', 'x': 180, 'y': (y + 180) % 360}
                    continue
                if kind in STAGED:
                    name = f'nursery_{kind}_{min(stage, STAGED[kind] - 1)}'
                else:
                    name = f'nursery_{kind}'
                v = {'model': f'the_oldest_house:block/{name}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    (ASSETS / 'blockstates/nursery.json').write_text(json.dumps({'variants': variants}, indent=1) + '\n')
    # (The Yellow Wallpaper already owns nursery_wallpaper; the child's room has its own.)
    (ASSETS / 'blockstates/child_wallpaper.json').write_text(json.dumps({'variants': {'': {'model': 'the_oldest_house:block/child_wallpaper'}}}, indent=2) + '\n')
    (MODELS / 'child_wallpaper.json').write_text(json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': 'the_oldest_house:block/child_wallpaper'}}, indent=2) + '\n')
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


def normalize(signal, peak=0.75):
    return signal / max(1e-9, np.max(np.abs(signal))) * peak


def bell(freq, seconds, decay=4.0):
    t = t_of(seconds)
    return (np.sin(2 * np.pi * freq * t) + .45 * np.sin(2 * np.pi * freq * 2.76 * t) * np.exp(-t * 3) + .2 * np.sin(2 * np.pi * freq * 5.4 * t) * np.exp(-t * 6)) * np.exp(-t * decay)


def place(out, signal, at):
    i = round(at * RATE)
    n = min(len(signal), len(out) - i)
    out[i:i + n] += signal[:n]


def s_train():
    t = t_of(.45)
    out = np.zeros_like(t)
    for at in (0, .2):
        burst = lowpass(noise(.12, int(at * 100) + 5), .5) * np.exp(-t_of(.12) * 30)
        place(out, burst, at)
    place(out, np.sin(2 * np.pi * 1800 * t_of(.03)) * np.exp(-t_of(.03) * 120) * .4, .1)
    return normalize(out, .6)


def s_whistle():
    t = t_of(.7)
    vib = 1 + .01 * np.sin(2 * np.pi * 7 * t)
    tone = np.sin(2 * np.pi * 880 * vib * t) + .7 * np.sin(2 * np.pi * 1108 * vib * t)
    env = np.clip(t / .04, 0, 1) * np.clip((.7 - t) / .12, 0, 1)
    return normalize(tone * env + lowpass(noise(.7, 6), .3) * env * .08, .55)


def s_top():
    t = t_of(2.4)
    f = 260 + 140 * np.sin(np.pi * np.clip(t / 2.4, 0, 1))
    phase = 2 * np.pi * np.cumsum(f) / RATE
    hum = np.sin(phase) + .3 * np.sign(np.sin(phase * 2))
    return normalize(hum * np.exp(-t * .9) * (.6 + .4 * np.sin(2 * np.pi * 9 * t) ** 2), .5)


def s_chime():
    return normalize(bell(1046.5, .9, 4.5), .6)


def s_pop():
    t = t_of(.8)
    f = 200 + 500 * np.exp(-t * 8)
    boing = np.sin(2 * np.pi * np.cumsum(f * (1 + .08 * np.sin(2 * np.pi * 22 * t))) / RATE) * np.exp(-t * 4)
    pop = lowpass(noise(.8, 7), .7) * np.exp(-t * 60)
    return normalize(boing * .8 + pop, .75)


def s_music_box():
    """An original lullaby of eleven notes, then a slowing last two, on a music box comb."""
    notes = [(784, 0), (659, .45), (659, .9), (698, 1.35), (587, 1.8), (587, 2.25), (523, 2.7), (587, 3.15), (659, 3.6), (698, 4.05), (784, 4.5), (784, 5.2), (523, 6.1)]
    out = np.zeros(round(RATE * 7.4))
    for f, at in notes:
        place(out, bell(f * 1.5, 1.2, 3.5), at)
    return normalize(out, .55)


def s_squeak():
    t = t_of(.35)
    f = 1100 + 600 * np.sin(np.pi * np.clip(t / .35, 0, 1))
    tone = np.sin(2 * np.pi * np.cumsum(f) / RATE)
    env = np.sin(np.pi * np.clip(t / .35, 0, 1))
    return normalize(tone * env + lowpass(noise(.35, 8), .4) * env * .2, .5)


def s_blocks():
    out = np.zeros(round(RATE * .7))
    for i, at in enumerate((0, .09, .2, .34, .5)):
        click = lowpass(noise(.06, 20 + i), .6) * np.exp(-t_of(.06) * 70) + np.sin(2 * np.pi * (900 + 130 * i) * t_of(.06)) * np.exp(-t_of(.06) * 80) * .5
        place(out, click * (1 - i * .12), at)
    return normalize(out, .6)


def s_ball():
    t = t_of(.3)
    thud = np.sin(2 * np.pi * 180 * t) * np.exp(-t * 22) + lowpass(noise(.3, 9), .5) * np.exp(-t * 80) * .6
    return normalize(thud, .65)


def s_creak():
    t = t_of(.75)
    f = 90 + 40 * np.sin(2 * np.pi * 1.3 * t)
    saw = ((np.cumsum(f) / RATE) % 1) * 2 - 1
    grain = lowpass(noise(.75, 10), .2)
    env = np.sin(np.pi * np.clip(t / .75, 0, 1))
    return normalize((saw * .5 + grain) * env * (.5 + .5 * np.sin(2 * np.pi * 31 * t) ** 2), .5)


def s_exit():
    t = t_of(1.5)
    thump = np.sin(2 * np.pi * 55 * t) * np.exp(-t * 7)
    crumble = lowpass(noise(1.5, 11), .25) * np.clip(1 - t / 1.4, 0, 1) * (.4 + .6 * (np.random.default_rng(12).random(len(t)) < .02))
    click = np.zeros_like(t)
    place(click, np.sin(2 * np.pi * 1400 * t_of(.02)) * np.exp(-t_of(.02) * 150), 1.1)
    return normalize(thump * .9 + lowpass(crumble, .5) * .9 + click * .3, .7)


def s_whisper():
    t = t_of(1.9)
    air = lowpass(noise(1.9, 13), .35) - lowpass(noise(1.9, 14), .05)
    syllables = np.clip(np.sin(2 * np.pi * 3.1 * t) * np.sin(2 * np.pi * .7 * t + 1), 0, 1)
    formant = 1 + .5 * np.sin(2 * np.pi * 5.3 * t)
    return normalize(air * syllables * formant * np.sin(np.pi * np.clip(t / 1.9, 0, 1)), .45)


SOUNDS = {
    'nursery_train': (s_train, 'Toy train chuffs'),
    'nursery_whistle': (s_whistle, 'Toy whistle toots'),
    'nursery_top': (s_top, 'Spinning top whirs'),
    'nursery_chime': (s_chime, 'Toy chime plays'),
    'nursery_pop': (s_pop, 'Jack-in-the-box pops'),
    'nursery_music_box': (s_music_box, 'Music box plays'),
    'nursery_squeak': (s_squeak, 'Teddy squeaks'),
    'nursery_blocks': (s_blocks, 'Toy blocks clatter'),
    'nursery_ball': (s_ball, 'Rubber ball bounces'),
    'nursery_creak': (s_creak, 'Wooden toy creaks'),
    'nursery_exit': (s_exit, 'Something seals into the wall'),
    'nursery_whisper': (s_whisper, 'Something whispers above the bed'),
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
        table[f'literary.{name}'] = {'subtitle': f'subtitles.the_oldest_house.literary.{name}', 'sounds': [{'name': f'the_oldest_house:literary/{name}', 'stream': False}]}
    write_json(path, table, original)
    path = ASSETS / 'lang/en_us.json'
    original = path.read_text()
    lang = json.loads(original)
    for name, (_, subtitle) in SOUNDS.items():
        lang[f'subtitles.the_oldest_house.literary.{name}'] = subtitle
    lang['block.the_oldest_house.nursery'] = 'Nursery piece'
    lang['block.the_oldest_house.child_wallpaper'] = "Child's wallpaper"
    write_json(path, lang, original)


if __name__ == '__main__':
    TEX.mkdir(parents=True, exist_ok=True)
    for name, make in TEXTURES.items():
        make().save(TEX / f'{name}.png')
    models()
    print('nursery variants:', blockstate())
    sounds()
