"""The horrors of the dark halls (0.4.209 onward): the one fitting block the five rooms share, in native Minecraft pixels.

Drawn in code from fixed seeds: no source pictures. The rooms themselves are built in the dark halls' ash
(tools/draw_dark_hall_art.py); this draws only what stands in them.

The closing hall (0.4.209): Daisy's crayon drawing on the floor, Karen's interview in a folder on a stool, the children's
crayons worn to stubs, and a small handprint (only ever shown to one reader, on the inside of a door).

The storeroom (0.4.210): grey steel shelving, bare or holding the black ink, the needles in their sterile packs, or a thing with a
paper tag; the bulb on its pull chain (out, lit, and its chain swung one way and the other); the list in the reader's hand;
cardboard boxes; and the footnote page.

The stillness (0.4.211): a surveyor's log page nailed to the wall; a count scratched into the floor dust in rows of five; and a
cloven print pressed into the dust where something stood.

--check verifies everything already written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/horror/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4, size=(16, 16)):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        px = im.load(); rnd = random.Random(seed)
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def put(im, x, y, c, a=255):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.load()[x, y] = shade(c, 0, a)


def line(im, x0, y0, x1, y1, c, a=255):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        put(im, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), c, a)


# ------------------------------------------------------------------------------------------------ the closing hall
def drawing():
    """Drawing paper on the floor: two long crayon lines meeting at the far edge, two small figures, the lines drawn again closer."""
    im = new('#e8e0cc', 901, 3)
    for x in range(16):
        put(im, x, 0, '#cfc6b0'); put(im, x, 15, '#cfc6b0')
    for y in range(16):
        put(im, 0, y, '#cfc6b0'); put(im, 15, y, '#cfc6b0')
    # The fold, both ways.
    for i in range(1, 15):
        put(im, 8, i, '#d9d0ba'); put(im, i, 8, '#d9d0ba')
    # The hallway: two long lines meeting near the top.
    line(im, 2, 14, 7, 2, '#2b2b3a'); line(im, 13, 14, 8, 2, '#2b2b3a')
    # Drawn again, closer, and again, until it goes through.
    line(im, 4, 14, 7, 5, '#3a3a52'); line(im, 11, 14, 8, 5, '#3a3a52')
    line(im, 6, 14, 7, 8, '#4a2a2a'); line(im, 9, 14, 8, 8, '#4a2a2a')
    put(im, 7, 13, '#1a1410'); put(im, 8, 13, '#1a1410'); put(im, 7, 12, '#1a1410')
    # Two small people at the near end, holding hands.
    for x, top, c in ((5, 11, '#b03a2e'), (10, 12, '#d17ba0')):
        put(im, x, top, '#e2b48e'); put(im, x, top + 1, c); put(im, x, top + 2, c); put(im, x - 1, top + 3, '#2b2b3a'); put(im, x + 1, top + 3, '#2b2b3a')
    line(im, 6, 12, 9, 13, '#e2b48e')
    # A tear.
    put(im, 14, 4, '#a8a08c'); put(im, 13, 5, '#a8a08c')
    return im


def folder():
    """A manila folder, typed pages showing at its edge."""
    im = new('#c9a868', 902, 4)
    for x in range(16):
        put(im, x, 0, '#9e8048'); put(im, x, 15, '#9e8048')
    for y in range(2, 14):
        put(im, 14, y, '#efeadc'); put(im, 15, y, '#e4ddca')
    for y in (4, 6, 8, 10):
        line(im, 3, y, 11, y, '#7c6438', 150)
    put(im, 2, 2, '#6f5228'); put(im, 3, 2, '#6f5228')
    return im


def pages():
    """The edge of a stack of typed pages."""
    im = new('#ece6d6', 903, 3)
    for y in range(0, 16, 2):
        line(im, 0, y, 15, y, '#d4cdb8')
    return im


def stool_seat():
    return new('#5e4a36', 904, 5)


def stool_leg():
    im = new('#4a3a2a', 905, 4)
    for y in range(16):
        put(im, 0, y, '#3a2c1e')
    return im


def crayons():
    """Crayon stubs on the floor, their papers peeled off, the dark ones gone."""
    im = new(None)
    rnd = random.Random(906)
    colours = ('#d8442e', '#e8c63a', '#3a8a4a', '#d17ba0', '#e88a2e', '#5aa0d8', '#e8e0cc')
    spots = [(2, 3), (6, 2), (11, 4), (4, 9), (9, 8), (13, 11), (7, 13)]
    for (x, y), c in zip(spots, colours):
        horizontal = rnd.random() < .5
        for i in range(3):
            put(im, x + (i if horizontal else 0), y + (0 if horizontal else i), c)
        put(im, x + (3 if horizontal else 0), y + (0 if horizontal else 3), '#2a2420')
    # Peeled paper curls.
    for x, y in ((3, 6), (12, 7), (10, 13)):
        put(im, x, y, '#efe8d0'); put(im, x + 1, y, '#d6cdb0')
    return im


def handprint():
    """A small hand, pressed flat: palm and five short fingers, grey-black, as if from soot or ash."""
    im = new(None)
    c = '#1c1a18'
    for y in range(8, 14):
        for x in range(5, 11):
            if (x, y) not in ((5, 13), (10, 13)):
                put(im, x, y, c, 210)
    for x, top in ((5, 5), (6, 3), (8, 2), (10, 4)):
        for y in range(top, 8):
            put(im, x if x != 10 else x - 0, y, c, 190)
    for y in range(4, 8):
        put(im, 7, y - 1, c, 190)
    # The thumb, out to the side.
    put(im, 11, 9, c, 180); put(im, 12, 8, c, 160); put(im, 12, 7, c, 140)
    # Smeared at the bottom where it was dragged.
    for x in range(6, 10):
        put(im, x, 14, c, 90)
    return im


# ------------------------------------------------------------------------------------------------ the storeroom
def steel(seed=921):
    """Grey steel shelving seen from the front: two shelf boards with a dark gap between, uprights at the edges, a few rust flecks."""
    im = new('#7a7d80', seed, 3)
    for y in (0, 7, 8, 15):
        for x in range(16):
            put(im, x, y, '#5f6265' if y in (7, 15) else '#9a9da0')
    for y in range(16):
        put(im, 0, y, '#55585b'); put(im, 15, y, '#55585b')
    for y in range(1, 7):
        for x in range(1, 15):
            put(im, x, y, '#2a2826'); put(im, x, y + 8, '#2a2826')
    rnd = random.Random(seed + 1)
    for _ in range(4):
        put(im, rnd.randrange(1, 15), rnd.choice((0, 8)), '#7a4a2a')
    return im


def stocked(kind):
    """The shelf front with something on its lower board: boxes, the ink, the needles, or a thing with a tag."""
    im = steel()
    rnd = random.Random({'boxes': 931, 'ink': 932, 'needles': 933, 'own': 934}[kind])
    if kind == 'boxes':
        for x0, w, c in ((1, 5, '#a8865a'), (7, 4, '#9a7a50'), (12, 3, '#b0905e')):
            for y in range(10, 15):
                for x in range(x0, x0 + w):
                    put(im, x, y, shade(c, rnd.randint(-6, 6))[:3] and c)
            line(im, x0, 10, x0 + w - 1, 10, shade(c, 20)[:3] and '#c8a878')
        for x0, c in ((2, '#8a6a40'), (9, '#9a7a50')):
            for y in range(2, 7):
                for x in range(x0, x0 + 4):
                    put(im, x, y, c)
    elif kind == 'ink':
        for y in range(9, 15):
            for x in range(5, 11):
                put(im, x, y, '#14141c' if 6 <= x <= 9 or y > 10 else '#20202a')
        put(im, 7, 9, '#3a3a50'); put(im, 8, 9, '#3a3a50'); put(im, 6, 11, '#e8e0cc'); put(im, 6, 12, '#e8e0cc'); put(im, 7, 12, '#e8e0cc')
        for x0 in (1, 12):
            for y in range(12, 15):
                put(im, x0, y, '#1a1a22'); put(im, x0 + 1, y, '#1a1a22')
    elif kind == 'needles':
        for x0 in (2, 6, 10):
            for y in range(10, 15):
                for x in range(x0, x0 + 3):
                    put(im, x, y, '#e6eaee')
            line(im, x0 + 1, 11, x0 + 1, 13, '#a8b0b8')
            put(im, x0, 10, '#4a8acc'); put(im, x0 + 2, 10, '#4a8acc')
    elif kind == 'own':
        for y in range(10, 15):
            for x in range(6, 11):
                put(im, x, y, '#6a5040')
        put(im, 8, 9, '#c8c0a8')
        for y in range(11, 14):
            for x in range(11, 14):
                put(im, x, y, '#efe6c8')
        put(im, 12, 12, '#2a2620'); line(im, 10, 11, 11, 11, '#8a8070')
    return im


def shelf_side():
    """The end of a run of shelving: an upright and the edges of the boards."""
    im = new('#6e7174', 941, 3)
    for y in (0, 7, 8, 15):
        for x in range(16):
            put(im, x, y, '#5a5d60')
    return im


def bulb(lit):
    im = new(None)
    glass = '#fff4c8' if lit else '#9a968a'
    for y in range(9, 15):
        for x in range(5, 11):
            if (x - 7.5) ** 2 + (y - 11.5) ** 2 <= 9:
                put(im, x, y, glass)
    if lit:
        put(im, 7, 11, '#ffffff'); put(im, 8, 11, '#ffffff')
    for y in range(6, 9):
        for x in range(6, 10):
            put(im, x, y, '#8a7a5a')
    return im


def chain():
    im = new(None)
    for y in range(16):
        put(im, 7 + (y % 2), y, '#8a8a8a')
    return im


def listing():
    """A torn notebook page, a list in a careful hand, the last line pressed through."""
    im = new('#ede6d2', 951, 3)
    for y in (3, 6, 9, 12):
        line(im, 2, y, 13, y, '#c4ccd8', 150)
    for y, w in ((2, 6), (5, 9), (8, 8), (11, 10)):
        line(im, 3, y, 3 + w, y, '#2a2e48')
    line(im, 3, 14, 12, 14, '#14182a')
    for x in range(16):
        if x % 3 == 0:
            put(im, x, 0, '#d8d0bc')
    return im


def footnote():
    """A page that is mostly footnote: small type, a superscript, the lines running off the edge."""
    im = new('#eae4d4', 961, 3)
    put(im, 2, 2, '#1a1a1a')
    for y in range(4, 15, 2):
        line(im, 1, y, 15 if y > 8 else 13, y, '#3a3632', 200)
    line(im, 9, 12, 15, 13, '#2a2e48')
    return im


# ------------------------------------------------------------------------------------------------ the stillness
def log_page():
    """A page from a survey log, squared paper, columns of figures and then a looser hand; a nail through the top."""
    im = new('#e2dccb', 971, 3)
    for x in range(1, 16, 3):
        line(im, x, 1, x, 15, '#cfd6cf', 110)
    for y in range(1, 16, 3):
        line(im, 1, y, 15, y, '#cfd6cf', 110)
    for y in (3, 5):
        for x in (2, 6, 10):
            line(im, x, y, x + 2, y, '#3b3a44')
    for y, w in ((8, 10), (10, 8), (12, 11)):
        line(im, 2, y, 2 + w, y, '#2c2a30', 220)
    line(im, 2, 14, 5, 14, '#1c1a1e')
    put(im, 8, 0, '#55504a'); put(im, 7, 0, '#6d675e'); put(im, 8, 1, '#3e3a35')
    return im


def tally():
    """Scratches in grey dust: rows of four strokes and a fifth across, more rows than fit."""
    im = new(None)
    c = '#d8d4cc'
    for row, y in enumerate((2, 6, 10, 14)):
        for group in range(3 if row < 3 else 1):
            x0 = 1 + group * 5
            for k in range(4):
                line(im, x0 + k, y - 1, x0 + k, y + 1, c, 200)
            line(im, x0 - 1, y + 1, x0 + 4, y - 1, c, 230)
    return im


def hoof():
    """One cloven print pressed into dust: two blunt crescents side by side, darker at the toe."""
    im = new(None)
    for cx in (5, 10):
        for y in range(3, 13):
            for x in range(cx - 2, cx + 3):
                dx = (x - cx) / 2.6
                dy = (y - 8) / 5.2
                if dx * dx + dy * dy <= 1:
                    put(im, x, y, '#141210', 120 + (12 - y) * 8)
    return im


def textures():
    return {f'block/horror/{k}.png': v for k, v in {
        'drawing': drawing(), 'folder': folder(), 'pages': pages(), 'stool_seat': stool_seat(), 'stool_leg': stool_leg(),
        'crayons': crayons(), 'handprint': handprint(),
        'shelf': steel(), 'shelf_ink': stocked('ink'), 'shelf_needles': stocked('needles'), 'shelf_own': stocked('own'), 'shelf_boxes': stocked('boxes'),
        'shelf_side': shelf_side(), 'bulb_lit': bulb(True), 'bulb_out': bulb(False), 'chain': chain(), 'list': listing(), 'footnote': footnote(),
        'log': log_page(), 'tally': tally(), 'hoof': hoof(),
    }.items()}


def element(lo, hi, textures, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures.get('all'))
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


KINDS = ('drawing', 'transcript', 'crayons', 'handprint', 'shelf', 'shelf_ink', 'shelf_needles', 'shelf_own', 'bulb', 'list', 'boxes', 'footnote', 'log', 'tally', 'hoof')
STAGES = {'bulb': 4}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def fitting_models():
    m = {}
    m['drawing'] = model([element([2, 0, 2], [14, .25, 14], {'up': 'd', 'down': 'd'})], {'d': 'drawing'}, 'drawing')
    legs = [element([x, 0, z], [x + 1.5, 7, z + 1.5], {'all': 'l'}) for x, z in ((3.5, 3.5), (11, 3.5), (3.5, 11), (11, 11))]
    m['transcript'] = model(legs + [
        element([3, 7, 3], [13, 8.5, 13], {'all': 's'}),
        element([4, 8.5, 4.5], [12, 9.25, 11.5], {'up': 'f', 'down': 'f', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}),
    ], {'l': 'stool_leg', 's': 'stool_seat', 'f': 'folder', 'p': 'pages'}, 'folder')
    m['crayons'] = model([element([1, 0, 1], [15, .5, 15], {'up': 'c'})], {'c': 'crayons'}, 'crayons')
    # Drawn against the north side of its block, low, at a child's height; turned by facing.
    m['handprint'] = model([element([3, 2, 0], [13, 12, .1], {'north': 'h', 'south': 'h'}, uv={'north': [3, 2, 13, 12], 'south': [13, 2, 3, 12]})], {'h': 'handprint'}, 'handprint', 'translucent')
    # Steel shelving: a full block whose front (north as drawn) carries the stock; the other faces are uprights and boards.
    for name, front in (('shelf', 'shelf'), ('shelf_ink', 'shelf_ink'), ('shelf_needles', 'shelf_needles'), ('shelf_own', 'shelf_own'), ('boxes', 'shelf_boxes')):
        m[name] = model([element([0, 0, 0], [16, 16, 16], {'north': 'f', 'south': 'f', 'east': 's', 'west': 's', 'up': 's', 'down': 's'})], {'f': front, 's': 'shelf_side'}, 'shelf', 'solid')
    # The bulb: out, lit, and lit with its chain swung one way and then the other.
    for stage in range(4):
        lit = stage > 0
        tilt = {0: 0, 1: 0, 2: 22.5, 3: -22.5}[stage]
        parts = [element([7.5, 8, 7.5], [8.5, 16, 8.5], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}),
                 element([5, 2, 5], [11, 8, 11], {'all': 'b'}), element([6, 8, 6], [10, 10, 10], {'all': 'b'})]
        if tilt:
            for p in parts:
                p['rotation'] = {'origin': [8, 16, 8], 'axis': 'z', 'angle': tilt}
        m[f'bulb_{stage}'] = model(parts, {'c': 'chain', 'b': 'bulb_lit' if lit else 'bulb_out'}, 'bulb_lit' if lit else 'bulb_out')
    m['list'] = model([element([3, 4, 15.9], [13, 14, 16], {'north': 'l'}, uv={'north': [3, 2, 13, 12]})], {'l': 'list'}, 'list')
    m['footnote'] = model([element([2, 0, 2], [14, .25, 14], {'up': 'n', 'down': 'n'})], {'n': 'footnote'}, 'footnote')
    # The stillness: the log page against its wall (the south side as drawn, like the list), the count and the print on the floor.
    m['log'] = model([element([3, 3, 15.9], [13, 15, 16], {'north': 'g'}, uv={'north': [3, 1, 13, 13]})], {'g': 'log'}, 'log')
    m['tally'] = model([element([1, 0, 1], [15, .1, 15], {'up': 't'})], {'t': 'tally'}, 'tally', 'translucent')
    m['hoof'] = model([element([3, 0, 2], [13, .05, 14], {'up': 'o'})], {'o': 'hoof'}, 'hoof', 'translucent')
    return m


def files():
    out = {}
    for key, obj in fitting_models().items():
        out[f'models/block/horror/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = f'{kind}_{stage if stage < STAGES.get(kind, 1) else 0}' if kind in STAGES else kind
                v = {'model': NS + f'block/horror/{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/horror_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, obj in fitting_models().items():
        for tex in obj['textures'].values():
            assert f'block/horror/{tex.split("block/horror/")[-1]}.png' in images, (key, tex)
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert (f'{kind}_{stage}' if kind in STAGES else kind) in fitting_models(), (kind, stage)
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
    print(('Verified' if check else 'Drew') + f' the horrors of the dark halls: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
