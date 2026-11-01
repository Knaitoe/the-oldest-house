"""The horrors of the dark halls (0.4.209 onward): the one fitting block the five rooms share, in native Minecraft pixels.

Drawn in code from fixed seeds: no source pictures. The rooms themselves are built in the dark halls' ash
(tools/draw_dark_hall_art.py); this draws only what stands in them.

The closing hall (0.4.209): Daisy's crayon drawing on the floor, Karen's interview in a folder on a stool, the children's
crayons worn to stubs, and a small handprint (only ever shown to one reader, on the inside of a door).

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


def textures():
    return {f'block/horror/{k}.png': v for k, v in {
        'drawing': drawing(), 'folder': folder(), 'pages': pages(), 'stool_seat': stool_seat(), 'stool_leg': stool_leg(),
        'crayons': crayons(), 'handprint': handprint(),
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


KINDS = ('drawing', 'transcript', 'crayons', 'handprint')
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
    return m


def files():
    out = {}
    for key, obj in fitting_models().items():
        out[f'models/block/horror/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            v = {'model': NS + f'block/horror/{kind}'}
            if y:
                v['y'] = y
            variants[f'facing={facing},kind={kind}'] = v
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
        assert kind in fitting_models(), kind
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
