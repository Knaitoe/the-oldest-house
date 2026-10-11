#!/usr/bin/env python3
"""Three Fingers (0.4.81): the cottage on the mill road, the withered paw and the night's sounds.

Environment textures are vanilla-style 16x16 pixel art drawn here (small palettes, one-pixel bevels and seams, scattered
single-pixel noise; nothing sampled): whitewash, faded wallpaper, wainscot, kitchen flags, thatch, the range and kettle,
the dresser and its plates, the mantel and clock, a sampler, the soldier's pocketbook, the family Bible, the lamp on the
road, a milestone and the doorstep. Six original synthesized sounds get subtitles.

The withered paw's art and the five people's skins are drawn by tools/draw_three_fingers_art.py (or replaced by the owner).
The placeholder helpers here only supply a missing file and never overwrite one that exists.

Run from anywhere:
    python3 tools/generate_paw_assets.py
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
TEX = ASSETS / 'textures/block/cottage'
MODELS = ASSETS / 'models/block'
NS = 'the_oldest_house:block/cottage/'
RATE = 22050


# ------------------------------------------------------------------------------------------------ drawing
def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c[:3]))


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


def whitewash():
    """Lime render over rough stone: off-white, uneven, a hairline crack and a damp corner."""
    img = noisy((214, 208, 192), 811, (-.05, -.02, 0, 0, .02, .03))
    rng = random.Random(812)
    for _ in range(10):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, (196, 190, 174))
    for x, y in ((3, 4), (4, 5), (4, 6), (5, 7), (5, 8), (6, 8)):
        px(img, x, y, (170, 164, 150))
    for y in range(12, 16):
        for x in range(0, 5 - (y - 12)):
            px(img, x, y, mul(img.getpixel((x, y)), .9))
    return img


def wallpaper():
    """Faded cream paper with a thin rose stripe and small green sprigs between."""
    img = noisy((206, 194, 160), 821, (-.03, 0, 0, .02))
    for y in range(16):
        for x in (0, 8):
            px(img, x, y, (178, 132, 120))
    rng = random.Random(822)
    for cx, cy in ((4, 3), (12, 11), (4, 11), (12, 3)):
        px(img, cx, cy, (176, 98, 96))
        for dx, dy in ((-1, 1), (1, 1), (0, 2)):
            px(img, cx + dx, cy + dy, (104, 128, 84))
    for _ in range(6):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, (190, 176, 140))
    return img


def wainscot():
    """Dark stained boarding with a moulded rail along the top."""
    img = canvas()
    rng = random.Random(831)
    for y in range(16):
        for x in range(16):
            c = (86, 58, 38) if (x // 4) % 2 == 0 else (80, 54, 35)
            if x % 4 == 0:
                c = (60, 40, 26)
            px(img, x, y, mul(c, 1 + rng.choice((-.05, 0, 0, .04))))
    for x in range(16):
        px(img, x, 0, (118, 84, 56))
        px(img, x, 1, (98, 68, 44))
        px(img, x, 2, (56, 38, 24))
    return img


def flagstone(top=True):
    """Worn grey kitchen flags, irregular joints filled with dirt."""
    img = noisy((128, 124, 116) if top else (110, 106, 100), 841 if top else 842)
    joints = [(0, y) for y in range(16)] + [(x, 7) for x in range(16)] + [(9, y) for y in range(7)] + [(5, y) for y in range(8, 16)]
    for x, y in joints:
        px(img, x, y, (74, 68, 60))
    rng = random.Random(843)
    for _ in range(8):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, (146, 142, 132))
    return img


def thatch(top=False):
    """Bundled straw laid in courses, darker at the butts, a few loose ends."""
    img = canvas()
    rng = random.Random(851 if top else 852)
    straws = [(158, 128, 72), (142, 112, 62), (172, 142, 84), (124, 98, 54)]
    for y in range(16):
        for x in range(16):
            c = rng.choice(straws)
            if not top and y % 5 == 4:
                c = mul(c, .7)
            px(img, x, y, c)
    for _ in range(18):
        x, y = rng.randrange(16), rng.randrange(15)
        px(img, x, y, (190, 160, 100))
        px(img, x, y + 1, (100, 78, 42))
    return img


def iron(seed):
    return noisy((42, 40, 40), seed, (-.08, -.04, 0, 0, .05, .1))


def range_front():
    """Black-leaded iron: the oven door with its handle, the firebox grate glowing low, a brass rail."""
    img = iron(861)
    for x in range(1, 15):
        px(img, x, 1, (168, 132, 64))
    for y in range(3, 13):
        for x in (2, 13):
            px(img, x, y, (24, 22, 22))
    for x in range(2, 14):
        px(img, x, 3, (24, 22, 22))
        px(img, x, 12, (24, 22, 22))
    for x in range(3, 8):
        for y in range(5, 11):
            px(img, x, y, (54, 52, 50))
    for y in range(6, 10):
        px(img, 7, y, (150, 150, 146))
    for x in range(9, 13):
        for y in range(8, 11):
            px(img, x, y, (226, 120, 40) if (x + y) % 2 == 0 else (150, 54, 20))
    for x in range(9, 13):
        px(img, x, 7, (20, 18, 18))
    return img


def range_top():
    img = iron(862)
    for cx, cy in ((4, 5), (11, 5), (8, 11)):
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - cx, y - cy)
                if 2.2 < d < 3.2:
                    px(img, x, y, (70, 68, 66))
                elif d <= 2.2:
                    px(img, x, y, (30, 28, 28))
    return img


def kettle():
    """A copper kettle, dented and dark at the base."""
    img = canvas()
    rng = random.Random(871)
    for y in range(16):
        for x in range(16):
            c = (176, 100, 54) if y < 10 else (130, 70, 38)
            if x in (0, 15) or y in (0, 15):
                c = mul(c, .7)
            px(img, x, y, mul(c, 1 + rng.choice((-.06, 0, 0, .06))))
    for x in range(4, 12):
        px(img, x, 2, (222, 156, 98))
    return img


def dresser_front():
    """The lower cupboard: two panelled doors with knobs under a row of drawers."""
    img = canvas()
    rng = random.Random(881)
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((112, 76, 46), 1 + rng.choice((-.05, 0, 0, .04))))
    for x in range(16):
        px(img, x, 4, (70, 46, 28))
        px(img, x, 0, (140, 98, 62))
    for y in range(5, 16):
        px(img, 8, y, (70, 46, 28))
    for x0 in (1, 9):
        for x in range(x0, x0 + 6):
            px(img, x, 6, (92, 62, 38))
            px(img, x, 14, (92, 62, 38))
        for y in range(6, 15):
            px(img, x0, y, (92, 62, 38))
            px(img, x0 + 5, y, (92, 62, 38))
    for x, y in ((4, 2), (11, 2), (6, 10), (10, 10)):
        px(img, x, y, (190, 150, 80))
    return img


def plain_wood(seed, base=(112, 76, 46)):
    img = canvas()
    rng = random.Random(seed)
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(base, 1 + rng.choice((-.06, -.02, 0, 0, .03))))
    for y in (5, 11):
        for x in range(16):
            px(img, x, y, mul(base, .8))
    return img


def dresser_plates():
    """The plate rack: blue-and-white plates stood on two shelves, a jug hung from a hook."""
    img = canvas()
    back = (96, 64, 40)
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(back, 1 + ((x * 7 + y * 3) % 5 - 2) * .02))
    for y in (6, 14):
        for x in range(16):
            px(img, x, y, (132, 92, 58))
    for row, base in ((0, 5), (8, 13)):
        for cx in (3, 8, 13):
            for y in range(base - 4, base + 1):
                for x in range(cx - 2, cx + 3):
                    d = math.hypot(x - cx, (y - (base - 2)) * 1.1)
                    if d <= 2.6:
                        px(img, x, y, (226, 228, 232) if d > 1.2 else (70, 96, 160))
    return img


def mantel():
    """A dark oak shelf with a turned edge."""
    img = plain_wood(891, (78, 52, 32))
    for x in range(16):
        px(img, x, 0, (110, 76, 48))
        px(img, x, 15, (50, 32, 20))
    return img


def clock():
    """A small mantel clock: dark case, cream face, hands near the hour."""
    img = canvas(16, 16, (60, 38, 24, 255))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 6:
                px(img, x, y, (226, 214, 180))
            if 5.4 <= d < 6.2:
                px(img, x, y, (168, 130, 64))
    for y in range(4, 8):
        px(img, 7, y, (30, 26, 24))
    for x in range(7, 11):
        px(img, x, 8, (30, 26, 24))
    return img


def sampler():
    """An embroidered sampler in a narrow frame: a border, a cottage, letters too small to read and a date."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (210, 198, 166))
    for i in range(16):
        for c in ((i, 0), (i, 15), (0, i), (15, i)):
            px(img, *c, (84, 56, 34))
    rng = random.Random(901)
    for x in range(2, 14):
        if x % 2 == 0:
            px(img, x, 2, (150, 60, 58))
            px(img, x, 13, (150, 60, 58))
    for y in range(4, 8):
        for x in range(5, 11):
            px(img, x, y, (170, 110, 80) if y > 5 else (96, 70, 50))
    px(img, 8, 6, (60, 70, 120))
    for x in range(3, 13):
        if rng.random() < .7:
            px(img, x, 10, (70, 84, 60))
    return img


def pocketbook():
    """A battered leather pocketbook, its strap undone, a page edge showing."""
    img = canvas()
    rng = random.Random(911)
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((92, 58, 34), 1 + rng.choice((-.08, -.03, 0, .04))))
    for x in range(16):
        px(img, x, 14, (220, 208, 176))
    for y in range(16):
        px(img, 11, y, (60, 36, 20))
    return img


def bible():
    """A heavy black Bible with gilt edges and a worn gilt cross."""
    img = canvas()
    rng = random.Random(921)
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((30, 28, 30), 1 + rng.choice((-.1, 0, 0, .12))))
    for i in range(4, 12):
        px(img, 8, i, (190, 156, 72))
    for i in range(6, 11):
        px(img, i, 6, (190, 156, 72))
    for i in range(16):
        px(img, i, 0, (60, 54, 56))
        px(img, i, 15, (60, 54, 56))
    return img


def bible_side():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (206, 168, 80) if 2 <= y <= 13 else (30, 28, 30))
    for x in range(0, 16, 3):
        for y in range(2, 14):
            px(img, x, y, (176, 140, 62))
    return img


def lamp(lit=True):
    """The lamp on the road: iron frame, glass panes, a flame burning or guttering."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            frame = x in (0, 15) or y in (0, 15) or x == 8
            if frame:
                px(img, x, y, (34, 32, 32))
            else:
                d = math.hypot(x - 7.5, (y - 9) * .8)
                if lit:
                    px(img, x, y, (255, 226, 140) if d < 2.5 else (238, 196, 110) if d < 4.5 else (196, 160, 96))
                else:
                    px(img, x, y, (150, 110, 60) if d < 1.5 else (70, 62, 54))
    return img


def milestone():
    """A weathered milestone, a carved arrow and a figure, lichen at the foot."""
    img = noisy((140, 136, 126), 941)
    for x, y in ((7, 3), (6, 4), (7, 4), (8, 4), (7, 5), (7, 6), (7, 7)):
        px(img, x, y, (84, 80, 74))
    for x, y in ((6, 10), (7, 9), (7, 10), (7, 11), (7, 12), (6, 12), (8, 12)):
        px(img, x, y, (84, 80, 74))
    rng = random.Random(942)
    for _ in range(9):
        x, y = rng.randrange(16), rng.randrange(13, 16)
        px(img, x, y, (110, 128, 72))
    return img


def doorstep():
    img = noisy((122, 118, 110), 951)
    for x in range(3, 13):
        px(img, x, 7, mul((122, 118, 110), .82))
        px(img, x, 8, mul((122, 118, 110), .86))
    return img


TEXTURES = {
    'whitewash': whitewash, 'wallpaper': wallpaper, 'wainscot': wainscot,
    'flagstone': lambda: flagstone(True), 'flagstone_side': lambda: flagstone(False),
    'thatch': lambda: thatch(False), 'thatch_top': lambda: thatch(True),
    'range_front': range_front, 'range_top': range_top, 'range_side': lambda: iron(863),
    'kettle': kettle, 'dresser_front': dresser_front, 'dresser_wood': lambda: plain_wood(882), 'dresser_plates': dresser_plates,
    'mantel': mantel, 'clock': clock, 'sampler': sampler, 'pocketbook': pocketbook, 'bible': bible, 'bible_side': bible_side,
    'lamp_lit': lambda: lamp(True), 'lamp_dim': lambda: lamp(False), 'milestone': milestone, 'doorstep': doorstep,
}


# ------------------------------------------------------------------------------------------------ models
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


def box(frm, to, tex, faces_=DIRS, overrides=None, glow=False):
    faces = {d: {'texture': '#' + tex} for d in faces_}
    for d, t in (overrides or {}).items():
        faces[d] = {'texture': '#' + t}
    el = {'from': list(frm), 'to': list(to), 'faces': faces}
    if glow:
        el['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
    return el


def model(name, elements, textures, particle=None, render='cutout'):
    tex = {k: (v if ':' in v else NS + v) for k, v in textures.items()}
    tex['particle'] = NS + (particle or next(iter(textures.values())))
    data = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:' + render, 'textures': tex, 'elements': elements}
    (MODELS / f'cottage_{name}.json').write_text(json.dumps(data, indent=1) + '\n')


def cube(name, faces):
    tex = {d: (v if ':' in v else NS + v) for d, v in faces.items()}
    tex['particle'] = tex.get('north', next(iter(tex.values())))
    (MODELS / f'cottage_{name}.json').write_text(json.dumps({'parent': 'minecraft:block/cube', 'textures': tex}, indent=1) + '\n')


def all6(t):
    return {d: t for d in DIRS}


def models():
    MODELS.mkdir(parents=True, exist_ok=True)
    cube('wallpaper', all6('wallpaper'))
    cube('wainscot', {**all6('wainscot'), 'up': 'dresser_wood', 'down': 'dresser_wood'})
    cube('flagstone', {**all6('flagstone_side'), 'up': 'flagstone'})
    cube('whitewash', all6('whitewash'))
    cube('thatch', {**all6('thatch'), 'up': 'thatch_top'})
    model('range', [box((0, 0, 0), (16, 16, 16), 's', overrides={'north': 'f', 'up': 't'})], {'f': 'range_front', 't': 'range_top', 's': 'range_side'}, 'range_side')
    model('kettle', [box((4, 0, 4), (12, 6, 12), 'k'), box((6, 6, 6), (10, 7, 10), 'k'), box((11, 2, 7), (14, 4, 9), 'k'), box((7, 7, 7.5), (9, 9, 8.5), 'k')], {'k': 'kettle'})
    model('dresser', [box((0, 0, 2), (16, 16, 16), 'w', overrides={'north': 'f'})], {'f': 'dresser_front', 'w': 'dresser_wood'}, 'dresser_wood')
    model('dresser_top', [box((0, 0, 14), (16, 14, 16), 'p', overrides={'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}),
                          box((0, 6, 9), (16, 7, 14), 'w'), box((0, 0, 9), (16, 1, 14), 'w'), box((0, 13, 9), (16, 14, 16), 'w'),
                          box((0, 0, 9), (1, 14, 14), 'w'), box((15, 0, 9), (16, 14, 14), 'w')], {'p': 'dresser_plates', 'w': 'dresser_wood'}, 'dresser_wood')
    model('mantel', [box((0, 10, 6), (16, 14, 16), 'm'), box((6, 14, 10), (10, 16, 13), 'c', overrides={'east': 'm', 'west': 'm', 'up': 'm', 'south': 'm', 'down': 'm'})],
          {'m': 'mantel', 'c': 'clock'}, 'mantel')
    model('sampler', [box((2, 3, 15), (14, 13, 16), 's', overrides={'south': 'f', 'east': 'f', 'west': 'f', 'up': 'f', 'down': 'f'})], {'s': 'sampler', 'f': 'mantel'}, 'sampler')
    model('pocketbook', [box((4, 0, 5), (12, 2, 11), 'p')], {'p': 'pocketbook'})
    model('bible', [box((3, 0, 3), (13, 4, 13), 's', overrides={'up': 'b', 'down': 'b'})], {'b': 'bible', 's': 'bible_side'}, 'bible')
    model('lamp_head_0', [box((4, 1, 4), (12, 10, 12), 'l', glow=True), box((3, 10, 3), (13, 12, 13), 'r'), box((7, 0, 7), (9, 1, 9), 'r')], {'l': 'lamp_lit', 'r': 'range_side'}, 'lamp_lit')
    model('lamp_head_1', [box((4, 1, 4), (12, 10, 12), 'l'), box((3, 10, 3), (13, 12, 13), 'r'), box((7, 0, 7), (9, 1, 9), 'r')], {'l': 'lamp_dim', 'r': 'range_side'}, 'lamp_dim')
    model('milestone', [box((3, 0, 4), (13, 10, 12), 'm'), box((4, 10, 5), (12, 12, 11), 'm')], {'m': 'milestone'})
    model('doorstep', [box((0, 0, 0), (16, 4, 16), 'd')], {'d': 'doorstep'})


KINDS = ['wallpaper', 'wainscot', 'flagstone', 'whitewash', 'thatch', 'range', 'kettle', 'dresser', 'dresser_top', 'mantel', 'sampler',
         'pocketbook', 'bible', 'lamp_head', 'milestone', 'doorstep']
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def blockstate():
    variants = {}
    for facing, y in TURN.items():
        for kind in KINDS:
            for stage in range(2):
                name = f'{kind}_{stage}' if kind == 'lamp_head' else kind
                v = {'model': f'the_oldest_house:block/cottage_{name}'}
                if y and kind not in ('wallpaper', 'whitewash', 'flagstone', 'thatch'):
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    (ASSETS / 'blockstates/cottage.json').write_text(json.dumps({'variants': variants}, indent=1) + '\n')
    return len(variants)


# ------------------------------------------------------------------------------------------------ the paw (placeholder art)
def paw_placeholder(fingers):
    """A crude dried paw: the owner's art replaces it. Raised fingers show how many wishes are left."""
    img = canvas()
    skin, dark = (120, 92, 64), (72, 52, 36)
    for y in range(9, 15):
        for x in range(4, 12):
            if (x - 7.5) ** 2 / 16 + (y - 11.5) ** 2 / 9 <= 1:
                px(img, x, y, skin if (x + y) % 3 else dark)
    for y in range(13, 16):
        for x in range(6, 10):
            px(img, x, y, dark)
    for i, x in enumerate((5, 7, 9)):
        up = i < fingers
        for y in range(3 if up else 8, 10):
            px(img, x, y, skin if y % 2 else dark)
        if not up:
            px(img, x + 1, 9, dark)
    return img


def paw():
    item = ASSETS / 'textures/item'
    models_dir = ASSETS / 'models/item'
    written = []
    for fingers, suffix in ((3, ''), (2, '_2'), (1, '_1'), (0, '_0')):
        target = item / f'withered_paw{suffix}.png'
        if not target.exists():
            paw_placeholder(fingers).save(target)
            written.append(target.name)
        if suffix:
            (models_dir / f'withered_paw{suffix}.json').write_text(json.dumps(
                {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'the_oldest_house:item/withered_paw{suffix}'}}, indent=1) + '\n')
    overrides = [{'predicate': {'custom_model_data': gone}, 'model': f'the_oldest_house:item/withered_paw_{3 - gone}'} for gone in (1, 2, 3)]
    (models_dir / 'withered_paw.json').write_text(json.dumps(
        {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'the_oldest_house:item/withered_paw'}, 'overrides': overrides}, indent=1) + '\n')
    return written


# ------------------------------------------------------------------------------------------------ the people (placeholder skins)
PLACEHOLDERS = {'walter': 'family_father', 'ruth': 'family_mother', 'will': 'stranger_1', 'sergeant': 'old_man', 'messenger': 'stranger_2'}


def placeholders():
    """Each of the cottage's people gets their own skin file, a copy of an existing skin to begin with. An existing file is kept."""
    entity = ASSETS / 'textures/entity'
    written = []
    for name, source in PLACEHOLDERS.items():
        target = entity / f'literary_paw_{name}.png'
        if not target.exists():
            shutil.copyfile(entity / f'literary_{source}.png', target)
            written.append(target.name)
    return written


# ------------------------------------------------------------------------------------------------ sounds
def t_of(seconds):
    return np.arange(round(RATE * seconds)) / RATE


def noise(seconds, seed):
    return np.random.default_rng(seed).uniform(-1, 1, round(RATE * seconds))


def lowpass(signal, alpha):
    out = np.zeros_like(signal)
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


def blow(seed, weight=1.0):
    """One blow on a heavy wooden door: a low thud with the door's ring and a little rattle."""
    t = t_of(.32)
    body = np.sin(2 * np.pi * 92 * t) * np.exp(-t * 18) + np.sin(2 * np.pi * 184 * t) * np.exp(-t * 26) * .5
    knock = lowpass(noise(.32, seed), .2) * np.exp(-t * 40) * 1.6
    rattle = noise(.32, seed + 1) * np.exp(-t * 60) * .15
    return (body + knock + rattle) * weight


def s_knock():
    """Two knocks at the cottage door."""
    out = np.zeros(round(RATE * .9))
    place(out, blow(961), 0)
    place(out, blow(962, .9), .3)
    return normalize(out, .8)


def s_hammer():
    """Fists on the door, fast and uneven, the frame rattling."""
    out = np.zeros(round(RATE * 1.2))
    rng = random.Random(963)
    at = 0.0
    for i in range(7):
        place(out, blow(964 + i, rng.uniform(.8, 1.2)), at)
        at += rng.uniform(.11, .19)
    rattle = lowpass(noise(1.2, 975), .6) * .08
    return normalize(out + rattle, .9)


def s_wind():
    """A gust against the windows: rising and falling roar, a thin whistle, rain flung on the glass."""
    secs = 3.6
    t = t_of(secs)
    swell = np.sin(np.pi * t / secs) ** 2
    roar = lowpass(noise(secs, 981), .05) * 4
    whistle = np.sin(2 * np.pi * np.cumsum(620 + 180 * np.sin(2 * np.pi * .7 * t)) / RATE) * .08 * swell
    rain = np.zeros_like(t)
    rng = random.Random(982)
    for _ in range(90):
        i = rng.randrange(len(t) - 200)
        rain[i:i + 200] += noise(200 / RATE, rng.randrange(10000)) * np.exp(-np.arange(200) / 18) * rng.uniform(.05, .2)
    return normalize((roar + whistle) * swell + rain * (.4 + swell), .7)


def s_twist():
    """Dry leather and small bones turning: a creak with a crackle in it."""
    secs = .7
    t = t_of(secs)
    creak = np.sign(np.sin(2 * np.pi * np.cumsum(60 + 90 * t) / RATE)) * .2 * np.exp(-((t - .3) / .2) ** 2)
    out = lowpass(creak, .3)
    rng = random.Random(991)
    for _ in range(14):
        place(out, noise(.01, rng.randrange(10000)) * rng.uniform(.3, .8) * np.exp(-np.arange(round(RATE * .01)) / 30), rng.uniform(.05, .6))
    return normalize(out, .75)


def s_scratch():
    """Claws low on a door: four drawn strokes, then quiet."""
    secs = 1.4
    out = np.zeros(round(RATE * secs))
    for k in range(4):
        n = round(RATE * .22)
        env = np.sin(np.pi * np.arange(n) / n) ** 1.5
        stroke = noise(.22, 1001 + k)
        stroke = stroke - lowpass(stroke, .2)
        place(out, stroke * env * .7, .05 + k * .3)
    return normalize(out, .7)


def s_hiss():
    """Something thrown on the coals: a hiss, then the fire spitting."""
    secs = 1.0
    t = t_of(secs)
    h = noise(secs, 1011)
    hiss = (h - lowpass(h, .25)) * np.exp(-t * 3.5)
    out = hiss.copy()
    rng = random.Random(1012)
    for _ in range(9):
        pop = noise(.02, rng.randrange(10000)) * np.exp(-np.arange(round(RATE * .02)) / 40) * rng.uniform(.4, .9)
        place(out, pop, rng.uniform(.15, .9))
    return normalize(out, .7)


SOUNDS = {
    'paw_knock': (s_knock, 'Someone knocks at the door'),
    'paw_hammer': (s_hammer, 'Fists hammer at the door'),
    'paw_wind': (s_wind, 'Wind batters the cottage'),
    'paw_twist': (s_twist, 'The paw twists'),
    'paw_scratch': (s_scratch, 'Something scratches at the door'),
    'paw_hiss': (s_hiss, 'The fire spits'),
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
        entry = {'name': f'the_oldest_house:literary/{name}', 'stream': False, 'attenuation_distance': 24}
        table[f'literary.{name}'] = {'subtitle': f'subtitles.the_oldest_house.literary.{name}', 'sounds': [entry]}
    write_json(path, table, original)
    path = ASSETS / 'lang/en_us.json'
    original = path.read_text()
    lang = json.loads(original)
    for name, (_, subtitle) in SOUNDS.items():
        lang[f'subtitles.the_oldest_house.literary.{name}'] = subtitle
    lang['block.the_oldest_house.cottage'] = 'Cottage piece'
    lang['item.the_oldest_house.withered_paw'] = 'Withered paw'
    write_json(path, lang, original)


if __name__ == '__main__':
    TEX.mkdir(parents=True, exist_ok=True)
    for name, make in TEXTURES.items():
        make().save(TEX / f'{name}.png')
    models()
    print('cottage variants:', blockstate())
    sounds()
    print('placeholder paw art written:', paw() or 'none (kept as it is)')
    print('placeholder skins written:', placeholders() or 'none (kept as they are)')
