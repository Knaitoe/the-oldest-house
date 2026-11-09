"""The bar's games (0.4.414, docs/BAR_GAMES_PROPOSAL.md): the pool table's cloth, rails, wood and pockets, the balls as the table
and the screen draw them, and the table's item picture; the dartboard's face and sides, and the dart's and board's item pictures
(0.4.415). Writes the table's model and block states and its name. --check verifies
everything already written. Run from tools/.

Palette: cloth #1f6e3e, rails and apron mahogany #5a3218 / #3a1e0e, sights #e8d8b0, pockets #0c0806 with a leather lip #3a2414.
The table's model is drawn as its foot facing north (the head one block on, beyond z=0); the head is the same model turned round.
Pixel positions in the cloth and rail pictures are the model's own coordinates, so the pockets sit where the rails meet.
"""
from pathlib import Path
import argparse
import json
import math
from PIL import Image

from draw_polybius_art import shade, new, fill, dot, element

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
B = NS + 'block/bar/'
CLOTH, RAIL, RAIL_DARK, SIGHT, POCKET, LIP = '#1f6e3e', '#5a3218', '#3a1e0e', '#e8d8b0', '#0c0806', '#3a2414'


def quarter(im, cx, cy, r, colour, lip=None):
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            d = math.hypot(x + .5 - cx, y + .5 - cy)
            if d < r:
                dot(im, x, y, colour)
            elif lip and d < r + 1:
                dot(im, x, y, lip)


def cloth():
    """The cloth, with the dark of the pockets at its corners: the far corners (z 14.5) and the middle ones (z 0)."""
    im = new(base=CLOTH, seed=401, spread=3)
    for cx, cy in [(1.5, 14.5), (14.5, 14.5), (1.5, 0), (14.5, 0)]:
        quarter(im, cx, cy, 2.2, POCKET)
    return im


def rail():
    """The rails' tops: mahogany, a cushion edge, three sights along each long side, and the pockets cut into the ends."""
    im = new(base=RAIL, seed=402, spread=4)
    for z in (4, 8, 12):
        dot(im, 0, z, SIGHT); dot(im, 15, z, SIGHT)
    for z in range(16):
        dot(im, 1, z, RAIL_DARK); dot(im, 14, z, RAIL_DARK)
    for x in range(16):
        dot(im, x, 14, RAIL_DARK)
    for cx, cy, r in [(1, 15, 2.2), (15, 15, 2.2), (.75, 0, 1.6), (15.25, 0, 1.6)]:
        quarter(im, cx, cy, r, POCKET, LIP)
    return im


def wood():
    im = new(base=RAIL_DARK, seed=403, spread=3)
    for y in (3, 9, 14):
        for x in range(16):
            dot(im, x, y, shade(RAIL_DARK, -10))
    return im


def leg():
    im = new(base=RAIL_DARK, seed=404, spread=2)
    for y in range(16):
        dot(im, 0, y, shade(RAIL_DARK, 12)); dot(im, 15, y, shade(RAIL_DARK, -14))
    fill(im, 0, 0, 16, 2, shade(RAIL, 6))
    return im


def disc(size=16, colour='#ffffff', band=None, edge=True):
    im = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    c = size / 2
    for y in range(size):
        for x in range(size):
            d = math.hypot(x + .5 - c, y + .5 - c)
            if d > c:
                continue
            if band and not (band[0] <= y < band[1]):
                continue
            a = 255 if d < c - 1 or not edge else int(255 * (c - d))
            dot(im, x, y, colour, max(0, min(255, a)))
    return im


def shine():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + .5 - 5.5, y + .5 - 5)
            if d < 3.2 and math.hypot(x + .5 - 8, y + .5 - 8) < 7.5:
                dot(im, x, y, '#ffffff', int(140 * (1 - d / 3.2)))
            dd = math.hypot(x + .5 - 8, y + .5 - 8)
            if 6.2 < dd < 8 and y > 8:
                dot(im, x, y, '#000000', 50)
    return im


def pocket_gui():
    im = disc(colour=POCKET)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + .5 - 8, y + .5 - 8)
            if 6.5 < d < 8:
                dot(im, x, y, LIP)
    return im


def item_icon():
    """The table from above, as an item: cloth, rails, pockets."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 1, 4, 15, 12, RAIL)
    fill(im, 2, 5, 14, 11, CLOTH)
    for x, y in [(1, 4), (14, 4), (1, 11), (14, 11), (7, 4), (8, 4), (7, 11), (8, 11)]:
        dot(im, x, y, POCKET)
    for x, y, c in [(10, 7, '#e8c020'), (11, 8, '#c8301e'), (10, 9, '#2850b8'), (5, 8, '#f4f0e4')]:
        dot(im, x, y, c)
    for x in (2, 13):
        for y in (12, 13):
            dot(im, x, y, RAIL_DARK)
    return im


# ------------------------------------------------------------------------------------------------------------ darts (0.4.415)
ORDER = [20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5]
BLACK, CREAM, RED, GREEN, WIRE, RING = '#1a1816', '#e6d9b8', '#b8281c', '#1c6e38', '#b8bcc0', '#141210'
DIGITS = {
    '0': ['111', '101', '101', '101', '111'], '1': ['010', '110', '010', '010', '111'], '2': ['111', '001', '111', '100', '111'],
    '3': ['111', '001', '011', '001', '111'], '4': ['101', '101', '111', '001', '001'], '5': ['111', '100', '111', '001', '111'],
    '6': ['111', '100', '111', '101', '111'], '7': ['111', '001', '010', '010', '010'], '8': ['111', '101', '111', '101', '111'],
    '9': ['111', '101', '111', '001', '111'],
}


def dartboard_face():
    """The board's face at 128 pixels to the block: the double's outer wire 56 pixels out (7 of the block's 16), the number ring
    to the edge. Proportions are a standard board's; 20 at the top, 6 at the right, as seen by someone facing it."""
    size, c, scoring = 128, 64, 56
    im = Image.new('RGBA', (size, size), (0, 0, 0, 255))
    wires = [6.35, 15.9, 99, 107, 162, 170]
    for y in range(size):
        for x in range(size):
            u, v = x + .5 - c, c - (y + .5)
            r = math.hypot(u, v) / scoring * 170
            if r > 170:
                dot(im, x, y, RING if math.hypot(u, v) < 63 else shade(RING, 10))
                continue
            if r < 6.35:
                colour = RED
            elif r < 15.9:
                colour = GREEN
            else:
                a = math.degrees(math.atan2(u, v)) % 360
                i = int((a + 9) // 18) % 20
                ring = 99 <= r < 107 or r >= 162
                colour = (RED if i % 2 == 0 else GREEN) if ring else (BLACK if i % 2 == 0 else CREAM)
                edge = abs(((a + 9) % 18)) < 18 * .02 or abs(((a + 9) % 18) - 18) < 18 * .02
                if edge and r > 15.9:
                    colour = WIRE
            if any(abs(r - w) < .9 for w in wires):
                colour = WIRE
            dot(im, x, y, colour)
    for i, n in enumerate(ORDER):
        a = math.radians(i * 18)
        cx, cy = c + math.sin(a) * 59.5, c - math.cos(a) * 59.5
        text = str(n)
        w = len(text) * 4 - 1
        for k, ch in enumerate(text):
            for row, bits in enumerate(DIGITS[ch]):
                for col, bit in enumerate(bits):
                    if bit == '1':
                        dot(im, int(cx - w / 2 + k * 4 + col), int(cy - 2.5 + row), '#ece6d6')
    return im


def dartboard_side():
    im = new(base=RING, seed=405, spread=2)
    for x in range(16):
        dot(im, x, 0, shade(RING, 14)); dot(im, x, 15, shade(RING, -6))
    return im


def dart_icon():
    """A dart lying corner to corner: point bottom left, flights top right."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for k in range(2):
        dot(im, 2 + k, 13 - k, '#d8d8d8')
    for k in range(2, 6):
        dot(im, 2 + k, 13 - k, '#6e7074'); dot(im, 3 + k, 13 - k, '#55575a')
    for k in range(6, 9):
        dot(im, 2 + k, 13 - k, '#161616')
    for x, y in [(11, 4), (12, 4), (12, 3), (13, 3), (11, 2), (10, 3), (13, 5), (12, 5), (11, 5), (12, 6), (13, 2)]:
        dot(im, x, y, '#c8301e')
    dot(im, 12, 4, '#8a1e14')
    return im


def dartboard_icon():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            u, v = x + .5 - 8, 8 - (y + .5)
            r = math.hypot(u, v)
            if r > 7.6:
                continue
            if r > 6.4:
                colour = RING
            elif r < 1:
                colour = RED
            elif r < 1.9:
                colour = GREEN
            else:
                i = int(((math.degrees(math.atan2(u, v)) % 360) + 9) // 18) % 20
                ring = 3.6 <= r < 4.4 or r >= 5.6
                colour = (RED if i % 2 == 0 else GREEN) if ring else (BLACK if i % 2 == 0 else CREAM)
            dot(im, x, y, colour)
    return im


def dartboard_model():
    """The board hung on the south side of its block, its face to the north."""
    side = {'texture': '#side'}
    return {'ambientocclusion': False,
            'textures': {'face': B + 'dartboard_face', 'side': B + 'dartboard_side', 'particle': B + 'dartboard_side'},
            'elements': [{'from': [0, 0, 14], 'to': [16, 16, 16], 'faces': {
                'north': {'uv': [0, 0, 16, 16], 'texture': '#face'},
                'south': dict(side, uv=[0, 0, 16, 16], cullface='south'),
                'east': dict(side, uv=[14, 0, 16, 16]), 'west': dict(side, uv=[0, 0, 2, 16]),
                'up': dict(side, uv=[0, 14, 16, 16]), 'down': dict(side, uv=[0, 0, 16, 2])}}]}


def textures():
    return {
        'block/bar/pool_cloth.png': cloth(), 'block/bar/pool_rail.png': rail(), 'block/bar/pool_wood.png': wood(),
        'block/bar/pool_leg.png': leg(), 'block/bar/ball.png': Image.new('RGBA', (8, 8), (255, 255, 255, 255)),
        'gui/pool_ball.png': disc(), 'gui/pool_band.png': disc(band=(5, 11)), 'gui/pool_shine.png': shine(),
        'gui/pool_pocket.png': pocket_gui(), 'item/pool_table.png': item_icon(),
        'block/bar/dartboard_face.png': dartboard_face(), 'block/bar/dartboard_side.png': dartboard_side(),
        'item/dart.png': dart_icon(), 'item/dartboard.png': dartboard_icon(),
    }


def table_model():
    """The foot of the table, facing north: the far rail at z 16, open at z 0 where the head joins it."""
    els = [
        element([0.5, 7, 0], [15.5, 11, 15.5], {'all': 'wood'}),
        element([1.5, 11, 0], [14.5, 12, 14.5], {'up': 'cloth', 'north': 'cloth', 'east': 'wood', 'west': 'wood', 'south': 'wood'}),
        element([0, 11, 0], [1.5, 13, 16], {'up': 'rail', 'east': 'wood', 'west': 'wood', 'south': 'wood', 'north': 'wood', 'down': 'wood'}),
        element([14.5, 11, 0], [16, 13, 16], {'up': 'rail', 'east': 'wood', 'west': 'wood', 'south': 'wood', 'north': 'wood', 'down': 'wood'}),
        element([1.5, 11, 14.5], [14.5, 13, 16], {'up': 'rail', 'north': 'wood', 'south': 'wood', 'down': 'wood'}),
        element([1, 0, 12.5], [3.5, 7, 15], {'all': 'leg'}),
        element([12.5, 0, 12.5], [15, 7, 15], {'all': 'leg'}),
    ]
    tex = {'cloth': B + 'pool_cloth', 'rail': B + 'pool_rail', 'wood': B + 'pool_wood', 'leg': B + 'pool_leg', 'particle': B + 'pool_wood'}
    return {'ambientocclusion': False, 'textures': tex, 'elements': els}


FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {'models/block/bar/pool_table.json': table_model(),
           'models/item/pool_table.json': {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/pool_table'}}}
    variants = {}
    for facing, y in FACING_Y.items():
        for part, turn in (('foot', 0), ('head', 180)):
            v = {'model': B + 'pool_table'}
            if (y + turn) % 360:
                v['y'] = (y + turn) % 360
            variants[f'facing={facing},part={part}'] = v
    out['blockstates/pool_table.json'] = {'variants': variants}
    out['models/block/bar/dartboard.json'] = dartboard_model()
    out['blockstates/dartboard.json'] = {'variants': {f'facing={f}': ({'model': B + 'dartboard', 'y': y} if y else {'model': B + 'dartboard'})
                                                      for f, y in FACING_Y.items()}}
    for item in ('dart', 'dartboard'):
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/' + item}}
    return out


NAMES = {'block.the_oldest_house.pool_table': 'Pool Table', 'block.the_oldest_house.dartboard': 'Dartboard',
         'item.the_oldest_house.dart': 'Dart', 'entity.the_oldest_house.dart': 'Dart'}


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for key, name in NAMES.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the bar: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
