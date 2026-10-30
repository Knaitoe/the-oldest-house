#!/usr/bin/env python3
"""The house of Usher (0.4.181), in vanilla-style 16x16 pixels: one `usher` block (18 kinds, 4 stages).

Its stone is grey and furred with fine fungus hanging from every joint; the fissure runs down the front through cells that
show where the crack enters and leaves, and can open into a black gap. Floors are black boards, walls sombre tapestry under
fretted ceilings; the vault is damp, the causeway worn, the tarn's ooze dark, and the night over it nearly black. Then the
long pointed windows, draperies, Roderick's easel and its painting of a lit tunnel, his guitar, books on the floor, an iron
candelabra, armorial shields and his tattered chair.

Run with python3.13 from the repository root; --check compares instead of writing. Writes textures under
assets/the_oldest_house/textures/block/usher/, block models usher_*.json and blockstates/usher.json.
"""
import json
import random
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path('src/main/resources/assets/the_oldest_house')
TEX = ASSETS / 'textures/block/usher'
MODELS = ASSETS / 'models/block'
CLEAR = (0, 0, 0, 0)


def clamp(c):
    return tuple(max(0, min(255, int(round(v)))) for v in c)


def mul(c, f):
    return clamp((c[0] * f, c[1] * f, c[2] * f))


def mix(a, b, t):
    return clamp(tuple(a[i] + (b[i] - a[i]) * t for i in range(3)))


def canvas(color=CLEAR):
    return Image.new('RGBA', (16, 16), color if len(color) == 4 else color + (255,))


def px(img, x, y, c, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), tuple(c[:3]) + (a,))


def noisy(base, seed, spread=.07):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(base, 1 + rng.uniform(-spread, spread)))
    return img


# The house's stone: coursed grey blocks, darker joints, and pale fungus hanging in threads from the joints.
STONE = (112, 110, 104)


def stone(seed=181):
    rng = random.Random(seed)
    img = noisy(STONE, seed, .09)
    for y in (0, 7, 8, 15):
        for x in range(16):
            px(img, x, y, mul(STONE, .62 if y in (7, 15) else .8))
    for x in (5, 13):
        for y in range(1, 7):
            px(img, x, y, mul(STONE, .66))
    for x in (1, 9):
        for y in range(9, 15):
            px(img, x, y, mul(STONE, .66))
    fungus = (176, 178, 160)
    for _ in range(7):
        x = rng.randrange(16)
        top = rng.choice((0, 8))
        for y in range(top + 1, top + 1 + rng.randint(2, 5)):
            if rng.random() < .85:
                px(img, x, y, mix(fungus, STONE, rng.uniform(.0, .35)))
            x += rng.choice((-1, 0, 0, 1))
    return img


# The fissure: a black crack two pixels wide, entering and leaving at the middle of the named edges.
EDGES = {'t': (7, 0), 'b': (7, 15), 'l': (0, 7), 'r': (15, 7)}


def fissure(entry, exit_, seed, wide=False):
    img = stone(seed)
    rng = random.Random(seed)
    (x0, y0), (x1, y1) = EDGES[entry], EDGES[exit_]
    steps = 22
    for i in range(steps + 1):
        t = i / steps
        x = x0 + (x1 - x0) * t + (rng.uniform(-1.2, 1.2) if 0 < i < steps else 0)
        y = y0 + (y1 - y0) * t + (rng.uniform(-1.2, 1.2) if 0 < i < steps else 0)
        if entry in 'tb' and exit_ in 'tb' or entry in 'lr' and exit_ in 'lr':
            pass
        else:
            # A corner crack bends through the middle of the cell rather than cutting across it.
            mx, my = 7.5, 7.5
            a = (1 - t) ** 2
            bq = 2 * (1 - t) * t
            c = t ** 2
            x = a * x0 + bq * mx + c * x1 + (rng.uniform(-1, 1) if 0 < i < steps else 0)
            y = a * y0 + bq * my + c * y1 + (rng.uniform(-1, 1) if 0 < i < steps else 0)
        ix, iy = int(round(x)), int(round(y))
        for dx in range(-2 if wide else 0, (3 if wide else 2)):
            px(img, ix + dx, iy, (14, 13, 12) if abs(dx) < 2 or not wide else (34, 32, 30))
            if entry in 'lr' and exit_ in 'lr' or (entry in 'lr' and exit_ in 'tb' and t < .4):
                px(img, ix, iy + dx, (14, 13, 12))
    return img


def fissure_open():
    img = stone(1899)
    for y in range(16):
        w = 3 + (y % 5 == 2)
        for x in range(8 - w, 8 + w):
            px(img, x, y, (6, 6, 7))
        px(img, 8 - w - 1, y, mul(STONE, .45))
        px(img, 8 + w, y, mul(STONE, .45))
    return img


def fissure_moon():
    """(0.4.182) The fissure opened wide on the last night, the low moon's light coming through it. The face is drawn
    full bright (the model's block_light), so its stone is painted down to sit with the dim wall round it."""
    img = stone(1900)
    for y in range(16):
        for x in range(16):
            r, g, b, a = img.getpixel((x, y))
            img.putpixel((x, y), (int(r * .42), int(g * .42), int(b * .42), a))
    for y in range(16):
        w = 4 + (y % 4 == 1)
        for x in range(8 - w, 8 + w):
            core = 1 - abs(x - 7.5) / w
            px(img, x, y, mix((96, 18, 14), (196, 58, 34), core * core))
        px(img, 8 - w - 1, y, (24, 8, 8))
        px(img, 8 + w, y, (24, 8, 8))
    return img


def floor():
    img = noisy((26, 24, 26), 1811, .12)
    for y in range(16):
        px(img, 0, y, (14, 13, 14))
        px(img, 8, y, (14, 13, 14))
    for x in range(16):
        px(img, x, 5 if x < 8 else 11, (16, 15, 17))
    for x, y in ((3, 2), (11, 9), (12, 9), (4, 13)):
        px(img, x, y, (60, 58, 64))
    return img


def tapestry():
    base = (58, 34, 36)
    img = noisy(base, 1812, .05)
    rng = random.Random(1812)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0:
                px(img, x, y, mul(base, .78))
    motif = (96, 82, 52)
    for x, y in ((7, 3), (6, 4), (8, 4), (7, 5), (5, 6), (9, 6), (7, 7), (7, 9), (6, 10), (8, 10), (7, 11)):
        px(img, x, y, mix(motif, base, rng.uniform(0, .3)))
    for x in range(16):
        px(img, x, 0, (34, 22, 24))
        px(img, x, 15, (34, 22, 24))
    return img


def fret():
    img = canvas((30, 24, 20))
    wood = (64, 48, 36)
    for i in range(16):
        for t in (0, 8):
            px(img, (i + t) % 16, i, wood)
            px(img, (15 - i + t) % 16, i, mul(wood, .8))
    for x in range(16):
        px(img, x, 0, (40, 30, 24))
        px(img, 0, x, (40, 30, 24))
    return img


def vault():
    img = stone(1813)
    img = Image.eval(img, lambda v: v)
    rng = random.Random(1813)
    out = canvas()
    for y in range(16):
        for x in range(16):
            r, g, b, a = img.getpixel((x, y))
            out.putpixel((x, y), clamp((r * .62, g * .64, b * .6)) + (255,))
    for _ in range(4):
        x = rng.randrange(16)
        for y in range(rng.randrange(6), 16):
            if rng.random() < .8:
                px(out, x, y, (44, 50, 46))
    return out


def causeway():
    base = (96, 94, 90)
    img = noisy(base, 1814, .1)
    for x in range(16):
        px(img, x, 7, mul(base, .62))
        px(img, x, 15, mul(base, .62))
    for y in range(8):
        px(img, 10, y, mul(base, .62))
    for y in range(8, 16):
        px(img, 4, y, mul(base, .62))
    rng = random.Random(1814)
    for _ in range(6):
        px(img, rng.randrange(16), rng.randrange(16), (70, 82, 62))
    return img


def mud():
    return noisy((30, 28, 22), 1815, .16)


def night():
    img = noisy((10, 10, 14), 1816, .25)
    rng = random.Random(1816)
    for _ in range(3):
        x, y = rng.randrange(16), rng.randrange(16)
        for dx in range(-2, 3):
            px(img, x + dx, y, (22, 22, 28))
    return img


def window(upper, glow=False):
    """A tall leaded window, or (0.4.182, glow) the same window on the storm night, the vapour outside faintly luminous."""
    img = canvas()
    lead = (30, 30, 32)
    glass = (24, 30, 40)
    for y in range(16):
        for x in range(2, 14):
            px(img, x, y, mix(glass, (40, 50, 64), (x + y) % 5 / 10))
    if glow:
        rng = random.Random(1839 + upper)
        for y in range(16):
            for x in range(2, 14):
                drift = (x * 3 + y * 2 + rng.randrange(3)) % 7 / 7
                px(img, x, y, mix((78, 92, 84), (138, 156, 136), drift * .7))
    for y in range(16):
        for x in (2, 13):
            px(img, x, y, lead)
        px(img, 7, y, lead)
        px(img, 8, y, lead)
    for x in range(2, 14):
        px(img, x, 15, lead)
        if not upper:
            px(img, x, 0, lead)
    if upper:
        # The pointed head of the window.
        for y in range(0, 7):
            half = int(round(6 * (y / 7) ** .6))
            for x in range(16):
                if x < 8 - half - 1 or x > 7 + half + 1:
                    img.putpixel((x, y), CLEAR)
            px(img, 7 - half - 1, y, lead)
            px(img, 8 + half + 1, y, lead)
    return img


def drapery():
    img = canvas()
    base = (40, 28, 34)
    for y in range(16):
        for x in range(16):
            fold = .78 + .3 * abs(((x + (y // 6)) % 5) - 2) / 2
            px(img, x, y, mul(base, fold))
    return img


def easel_wood():
    return noisy((92, 66, 40), 1817, .1)


def painting():
    """Roderick's picture: a long low tunnel of smooth white walls, lit from nowhere."""
    img = canvas((20, 18, 16))
    for y in range(2, 14):
        for x in range(2, 14):
            d = max(abs(x - 7.5), abs(y - 7.5) * 1.6)
            px(img, x, y, mix((236, 232, 214), (120, 116, 104), min(1, d / 7)))
    for x in range(5, 11):
        px(img, x, 7, (255, 252, 236))
        px(img, x, 8, (255, 252, 236))
    for i in range(16):
        px(img, i, 1, (110, 84, 40))
        px(img, i, 14, (110, 84, 40))
        px(img, 1, i, (110, 84, 40))
        px(img, 14, i, (110, 84, 40))
    return img


def guitar():
    img = canvas()
    body = (140, 90, 44)
    for y in range(6, 16):
        w = 4 if y in (9, 10) else 5
        for x in range(8 - w, 8 + w):
            px(img, x, y, mul(body, .9 + .1 * ((x + y) % 3)))
    for x in range(6, 10):
        for y in range(10, 13):
            if (x - 7.5) ** 2 + (y - 11.5) ** 2 < 2.5:
                px(img, x, y, (24, 18, 12))
    for y in range(0, 7):
        px(img, 7, y, (70, 46, 26))
        px(img, 8, y, (70, 46, 26))
    for y in range(1, 15):
        px(img, 7, y, (210, 206, 190)) if y % 2 else None
    return img


def books():
    img = canvas()
    rng = random.Random(1818)
    covers = [(92, 30, 30), (34, 54, 40), (60, 44, 30), (38, 40, 64), (80, 70, 50)]
    for i in range(7):
        x, y = rng.randrange(12), rng.randrange(12)
        w, h = rng.randint(3, 5), rng.randint(3, 4)
        c = covers[i % len(covers)]
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                px(img, xx, yy, mul(c, .9 + .1 * ((xx + yy) % 2)))
        if i % 3 == 0:
            for yy in range(y + 1, y + h - 1):
                px(img, x + w // 2, yy, (220, 210, 186))
    return img


def iron():
    return noisy((46, 44, 46), 1819, .12)


def wax():
    return noisy((228, 220, 196), 1820, .05)


def flame():
    img = canvas()
    for y in range(4, 12):
        w = 1 if y < 6 else 2
        for x in range(8 - w, 8 + w):
            px(img, x, y, (255, 196 if y > 7 else 236, 96 if y > 7 else 160))
    return img


def shield():
    img = canvas()
    base = (70, 60, 50)
    for y in range(1, 15):
        w = 6 if y < 9 else 6 - (y - 8)
        for x in range(8 - w, 8 + w):
            px(img, x, y, base)
    for y in range(2, 13):
        px(img, 7, y, (150, 128, 72))
        px(img, 8, y, (150, 128, 72))
    for x in range(3, 13):
        px(img, x, 6, (150, 128, 72))
    return img


def upholstery():
    base = (64, 40, 38)
    img = noisy(base, 1821, .1)
    rng = random.Random(1821)
    for _ in range(5):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, (120, 104, 80))
        px(img, x + 1, y, (120, 104, 80))
    return img


def chair_wood():
    return noisy((44, 30, 22), 1822, .1)


TEXTURES = {
    'stone': stone, 'fissure_tb': lambda: fissure('t', 'b', 1891), 'fissure_tr': lambda: fissure('t', 'r', 1892),
    'fissure_tl': lambda: fissure('t', 'l', 1893), 'fissure_lb': lambda: fissure('l', 'b', 1894),
    'fissure_rb': lambda: fissure('r', 'b', 1895), 'fissure_lr': lambda: fissure('l', 'r', 1896), 'fissure_open': fissure_open, 'fissure_moon': fissure_moon,
    'floor': floor, 'tapestry': tapestry, 'fret': fret, 'vault': vault, 'causeway': causeway, 'mud': mud, 'night': night,
    'window_lower': lambda: window(False), 'window_upper': lambda: window(True),
    'window_lower_glow': lambda: window(False, True), 'window_upper_glow': lambda: window(True, True), 'drapery': drapery,
    'easel': easel_wood, 'painting': painting, 'guitar': guitar, 'books': books, 'iron': iron, 'wax': wax, 'flame': flame,
    'shield': shield, 'upholstery': upholstery, 'chair': chair_wood,
}


def t(name):
    return f'the_oldest_house:block/usher/{name}'


def cube(all_, front=None):
    m = {'parent': 'minecraft:block/cube', 'textures': {'particle': t(all_), 'down': t(all_), 'up': t(all_), 'north': t(front or all_),
                                                        'south': t(all_), 'east': t(all_), 'west': t(all_)}}
    return m


def faces(tex, uv=None):
    return {d: ({'texture': tex, 'uv': uv} if uv else {'texture': tex}) for d in ('north', 'south', 'east', 'west', 'up', 'down')}


def element(frm, to, tex, uv=None, only=None):
    f = faces(tex, uv)
    if only:
        f = {d: v for d, v in f.items() if d in only}
    return {'from': frm, 'to': to, 'faces': f}


def model(textures, elements):
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'textures': textures, 'elements': elements}


MODEL_DEFS = {
    'usher_stone': cube('stone'), 'usher_floor': cube('floor'), 'usher_tapestry': cube('tapestry'), 'usher_fret': cube('fret'),
    'usher_vault': cube('vault'), 'usher_causeway': cube('causeway'), 'usher_mud': cube('mud'), 'usher_night': cube('night'),
    'usher_fissure_tb': cube('stone', 'fissure_tb'), 'usher_fissure_tr': cube('stone', 'fissure_tr'),
    'usher_fissure_tl': cube('stone', 'fissure_tl'), 'usher_fissure_lb': cube('stone', 'fissure_lb'),
    'usher_fissure_rb': cube('stone', 'fissure_rb'), 'usher_fissure_lr': cube('stone', 'fissure_lr'),
    'usher_fissure_open': cube('stone', 'fissure_open'),
    'usher_fissure_moon': {'parent': 'minecraft:block/block', 'textures': {'particle': t('stone'), 's': t('stone'), 'm': t('fissure_moon')},
                           'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'neoforge_data': {'block_light': 15},
                                         'faces': {d: {'texture': '#m' if d == 'north' else '#s', 'cullface': d} for d in ('north', 'south', 'east', 'west', 'up', 'down')}}]},
    'usher_window_lower': model({'g': t('window_lower'), 'particle': t('window_lower')}, [element([0, 0, 7], [16, 16, 9], '#g', only=('north', 'south'))]),
    'usher_window_upper': model({'g': t('window_upper'), 'particle': t('window_upper')}, [element([0, 0, 7], [16, 16, 9], '#g', only=('north', 'south'))]),
    'usher_window_lower_glow': model({'g': t('window_lower_glow'), 'particle': t('window_lower_glow')}, [element([0, 0, 7], [16, 16, 9], '#g', only=('north', 'south'))]),
    'usher_window_upper_glow': model({'g': t('window_upper_glow'), 'particle': t('window_upper_glow')}, [element([0, 0, 7], [16, 16, 9], '#g', only=('north', 'south'))]),
    'usher_drapery': model({'d': t('drapery'), 'particle': t('drapery')}, [element([0, 0, 14], [16, 16, 16], '#d')]),
    'usher_easel': model({'w': t('easel'), 'p': t('painting'), 'particle': t('easel')}, [
        element([3, 0, 8], [4, 16, 9], '#w'), element([12, 0, 8], [13, 16, 9], '#w'), element([7, 0, 10], [9, 14, 11], '#w'),
        element([2, 3, 7], [14, 15, 8], '#p', only=('north',)), element([2, 3, 8], [14, 15, 8.5], '#w', only=('south', 'east', 'west', 'up', 'down'))]),
    'usher_guitar': model({'g': t('guitar'), 'particle': t('guitar')}, [element([5, 0, 7], [11, 14, 9], '#g')]),
    'usher_books': model({'b': t('books'), 'particle': t('books')}, [element([1, 0, 1], [15, 1, 15], '#b', only=('up', 'north', 'south', 'east', 'west'))]),
    'usher_candelabra': model({'i': t('iron'), 'w': t('wax'), 'f': t('flame'), 'particle': t('iron')}, [
        element([6, 0, 6], [10, 1, 10], '#i'), element([7.5, 1, 7.5], [8.5, 11, 8.5], '#i'), element([3, 10, 7.5], [13, 11, 8.5], '#i'),
        element([3, 11, 7], [5, 14, 9], '#w'), element([7, 11, 7], [9, 15, 9], '#w'), element([11, 11, 7], [13, 14, 9], '#w'),
        element([3.5, 14, 8], [4.5, 16, 8], '#f', only=('north', 'south')), element([7.5, 15, 8], [8.5, 16, 8], '#f', only=('north', 'south')),
        element([11.5, 14, 8], [12.5, 16, 8], '#f', only=('north', 'south'))]),
    'usher_armorial': model({'s': t('shield'), 'particle': t('shield')}, [element([2, 2, 15], [14, 14, 16], '#s')]),
    'usher_chair': model({'w': t('chair'), 'u': t('upholstery'), 'particle': t('upholstery')}, [
        element([2, 0, 2], [4, 7, 4], '#w'), element([12, 0, 2], [14, 7, 4], '#w'), element([2, 0, 12], [4, 7, 4 + 10], '#w'),
        element([12, 0, 12], [14, 7, 14], '#w'), element([2, 7, 2], [14, 9, 14], '#u'), element([2, 9, 12], [14, 16, 14], '#u')]),
}

KIND_MODELS = {
    'stone': ['usher_stone'] * 4, 'floor': ['usher_floor'] * 4, 'tapestry': ['usher_tapestry'] * 4, 'fret': ['usher_fret'] * 4,
    'vault': ['usher_vault'] * 4, 'causeway': ['usher_causeway'] * 4, 'mud': ['usher_mud'] * 4, 'night': ['usher_night'] * 4,
    'fissure': ['usher_fissure_tb', 'usher_fissure_tr', 'usher_fissure_tl', 'usher_fissure_lb'],
    # The last stage is the fissure on the last night, the moon through it, shown to one reader at a time.
    'fissure_b': ['usher_fissure_rb', 'usher_fissure_lr', 'usher_fissure_open', 'usher_fissure_moon'],
    # Stages 2 and 3 are the storm night's glow, lower and upper, shown to one reader at a time.
    'window': ['usher_window_lower', 'usher_window_upper', 'usher_window_lower_glow', 'usher_window_upper_glow'],
    'drapery': ['usher_drapery'] * 4, 'easel': ['usher_easel'] * 4, 'guitar': ['usher_guitar'] * 4, 'books': ['usher_books'] * 4,
    'candelabra': ['usher_candelabra'] * 4, 'armorial': ['usher_armorial'] * 4, 'chair': ['usher_chair'] * 4,
}
# Models face north; the blockstate turns them to the block's facing.
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def blockstate():
    variants = {}
    for kind, models in KIND_MODELS.items():
        for facing, y in TURN.items():
            for stage in range(4):
                v = {'model': f'the_oldest_house:block/{models[stage]}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    return {'variants': variants}


def outputs():
    images = {TEX / f'{name}.png': draw() for name, draw in TEXTURES.items()}
    texts = {MODELS / f'{name}.json': json.dumps(m, indent=1) + '\n' for name, m in MODEL_DEFS.items()}
    texts[ASSETS / 'blockstates/usher.json'] = json.dumps(blockstate(), indent=1) + '\n'
    return images, texts


def main():
    check = '--check' in sys.argv
    images, texts = outputs()
    stale = []
    for path, img in images.items():
        if check:
            if not path.is_file() or Image.open(path).convert('RGBA').tobytes() != img.convert('RGBA').tobytes():
                stale.append(str(path))
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path)
    for path, text in texts.items():
        if check:
            if not path.is_file() or path.read_text() != text:
                stale.append(str(path))
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
    if stale:
        raise SystemExit('Usher art differs from tools/draw_usher_art.py: ' + ', '.join(stale))


if __name__ == '__main__':
    main()
