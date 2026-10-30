#!/usr/bin/env python3
"""Zampano's filing cabinets in the courtyard archive (JOURNAL_DESIGN.md, step 2) and the leaf a reader pulls from them.

Textures are 16x16 in vanilla's block and item style:
  zampano_cabinet_wood     worn dark walnut for the sides, top and back
  zampano_cabinet_carcass  the front frame, dark in the gaps between drawers
  zampano_cabinet_front    the three drawer fronts at the rows their model elements use, each with a brass card holder
  zampano_cabinet_brass    the cup pulls
  zampano_cabinet_inside   looking down into an open drawer: folders on edge, their tabs, a leaf standing proud
  item/zampano_leaf        a loose leaf written over in a spiral
Models: the shut cabinet and one with each drawer out (its front six pixels proud, the box behind it showing its folders),
facing north; the blockstate turns them for every facing and cabinet.
Run with python3.13 from the repository root; --check compares the committed assets instead of writing them.
"""
import io
import json
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path('src/main/resources/assets/the_oldest_house')
CLEAR = (0, 0, 0, 0)
WOOD = [(66, 44, 30, 255), (74, 50, 34, 255), (58, 38, 26, 255), (82, 56, 38, 255)]
WOOD_DARK = (38, 25, 17, 255)
GAP = (22, 15, 11, 255)
BRASS = (176, 138, 62, 255); BRASS_LIGHT = (214, 178, 96, 255); BRASS_DARK = (118, 88, 40, 255)
CARD = (226, 214, 184, 255); CARD_INK = (70, 66, 70, 255)
MANILA = (196, 164, 104, 255); MANILA_DARK = (160, 128, 76, 255); LEAF = (236, 228, 206, 255); LEAF_SHADE = (206, 196, 170, 255)
INK = (52, 52, 58, 255)
# The drawers' rows in block pixels (y up): bottom, middle, top.
SLOTS = [(1, 5), (6, 10), (11, 15)]


def grain(x, y, seed=0):
    # Vertical grain with knots of darker wood, repeatable per pixel.
    n = (x * 7 + (y // 3) * 3 + seed * 5 + (x * y) % 5) % 9
    return WOOD[0] if n < 3 else WOOD[1] if n < 6 else WOOD[2] if n < 8 else WOOD[3]


def wood():
    img = Image.new('RGBA', (16, 16)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = grain(x, y)
    for x in range(16):
        px[x, 0] = WOOD_DARK; px[x, 15] = WOOD_DARK
    for y in range(16):
        px[0, y] = WOOD_DARK; px[15, y] = WOOD_DARK
    return img


def carcass():
    img = wood(); px = img.load()
    for top in (5, 10):
        v = 16 - top - 1
        for x in range(1, 15):
            px[x, v] = GAP
    for x in range(16):
        px[x, 15] = GAP
    return img


def front():
    img = Image.new('RGBA', (16, 16), CLEAR); px = img.load()
    for low, high in SLOTS:
        v0, v1 = 16 - high, 16 - low
        for v in range(v0, v1):
            for u in range(2, 14):
                px[u, v] = grain(u, v, 2)
        for u in range(2, 14):
            px[u, v0] = WOOD[3]; px[u, v1 - 1] = WOOD_DARK
        for v in range(v0, v1):
            px[2, v] = WOOD[3]; px[13, v] = WOOD_DARK
        # The brass card holder above the pull, its card inked with a mark nobody can read.
        px[6, v0 + 1] = BRASS_LIGHT; px[9, v0 + 1] = BRASS_DARK
        px[7, v0 + 1] = CARD; px[8, v0 + 1] = CARD_INK
    return img


def brass():
    img = Image.new('RGBA', (16, 16)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = BRASS_LIGHT if y < 5 else BRASS if y < 11 else BRASS_DARK
    return img


def inside():
    img = Image.new('RGBA', (16, 16)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = GAP
    # Folders stand on edge across the drawer, each a manila line with a tab, front to back.
    for row, v in enumerate(range(1, 16, 3)):
        for u in range(1, 15):
            px[u, v] = MANILA if (u + row) % 7 else MANILA_DARK
        tab = 2 + (row * 4) % 10
        for u in range(tab, tab + 3):
            px[u, v - 1] = MANILA
        px[1 + (row * 5) % 12, v + 1] = LEAF_SHADE
    # One leaf stands proud of the rest.
    for u in range(5, 11):
        px[u, 7] = LEAF
    px[7, 7] = INK
    return img


def leaf_item():
    img = Image.new('RGBA', (16, 16), CLEAR); px = img.load()
    for y in range(2, 15):
        for x in range(3, 13):
            px[x, y] = LEAF
    for y in range(2, 15):
        px[12, y] = LEAF_SHADE
    for x in range(3, 13):
        px[x, 14] = LEAF_SHADE
    # Writing wound in a square spiral from the middle.
    for x, y in ((7, 8), (8, 8), (8, 7), (7, 6), (6, 6), (5, 7), (5, 8), (5, 9), (6, 10), (7, 10), (8, 10), (9, 10),
                 (10, 9), (10, 8), (10, 7), (10, 6), (10, 5), (9, 4), (8, 4), (7, 4), (6, 4), (5, 4)):
        px[x, y] = INK
    px[4, 12] = INK; px[5, 12] = INK; px[7, 12] = INK
    # A torn corner.
    px[3, 2] = CLEAR; px[4, 2] = CLEAR; px[3, 3] = CLEAR
    return img


def face(texture, uv):
    return {'texture': texture, 'uv': uv}


def element(frm, to, faces):
    return {'from': frm, 'to': to, 'faces': faces}


def drawer_elements(index, out):
    low, high = SLOTS[index]
    z = -5 if out else 1
    v0, v1 = 16 - high, 16 - low
    cup = low + 0.6
    parts = [element([2, low, z], [14, high, z + 1], {
        'north': face('#front', [2, v0, 14, v1]), 'south': face('#wood', [2, v0, 14, v1]),
        'east': face('#wood', [0, v0, 1, v1]), 'west': face('#wood', [0, v0, 1, v1]),
        'up': face('#wood', [2, 0, 14, 1]), 'down': face('#wood', [2, 0, 14, 1])}),
        element([6, cup, z - 1], [10, cup + 1, z], {
            'north': face('#brass', [6, 6, 10, 7]), 'up': face('#brass', [6, 2, 10, 3]), 'down': face('#brass', [6, 12, 10, 13]),
            'east': face('#brass', [0, 6, 1, 7]), 'west': face('#brass', [0, 6, 1, 7])})]
    if out:
        parts.append(element([3, low, -4], [13, high - 1, 2], {
            'up': face('#inside', [3, 2, 13, 8]), 'east': face('#wood', [10, v0 + 1, 16, v1]),
            'west': face('#wood', [0, v0 + 1, 6, v1]), 'down': face('#wood', [3, 0, 13, 6])}))
    return parts


def model(open_drawer):
    elements = [element([1, 0, 2], [15, 16, 16], {
        'north': face('#carcass', [1, 0, 15, 16]), 'south': face('#wood', [1, 0, 15, 16]),
        'east': face('#wood', [2, 0, 16, 16]), 'west': face('#wood', [0, 0, 14, 16]),
        'up': face('#wood', [1, 2, 15, 16]), 'down': face('#wood', [1, 2, 15, 16])})]
    for i in range(3):
        elements += drawer_elements(i, i == open_drawer)
    tex = {name: f'the_oldest_house:block/zampano_cabinet_{name}' for name in ('wood', 'carcass', 'front', 'brass', 'inside')}
    tex['particle'] = 'the_oldest_house:block/zampano_cabinet_wood'
    return {'parent': 'minecraft:block/block', 'textures': tex, 'elements': elements}


def png(img):
    buf = io.BytesIO(); img.save(buf, 'PNG'); return buf.getvalue()


def outputs():
    out = {}
    for name, img in (('wood', wood()), ('carcass', carcass()), ('front', front()), ('brass', brass()), ('inside', inside())):
        out[f'textures/block/zampano_cabinet_{name}.png'] = png(img)
    out['textures/item/zampano_leaf.png'] = png(leaf_item())
    # Drawer property 1 is the top drawer (SLOTS index 2), 3 the foot (index 0).
    names = {0: 'zampano_cabinet', 1: 'zampano_cabinet_top_out', 2: 'zampano_cabinet_middle_out', 3: 'zampano_cabinet_foot_out'}
    slot = {0: -1, 1: 2, 2: 1, 3: 0}
    for drawer, name in names.items():
        out[f'models/block/{name}.json'] = (json.dumps(model(slot[drawer]), indent=2) + '\n').encode()
    turn = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    variants = {}
    for facing, y in turn.items():
        for cabinet in range(3):
            for drawer, name in names.items():
                v = {'model': f'the_oldest_house:block/{name}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},cabinet={cabinet},drawer={drawer}'] = v
    out['blockstates/zampano_cabinet.json'] = (json.dumps({'variants': variants}, indent=2) + '\n').encode()
    out['models/item/zampano_leaf.json'] = (json.dumps(
        {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'the_oldest_house:item/zampano_leaf'}}, indent=2) + '\n').encode()
    return out


def main():
    check = '--check' in sys.argv
    stale = []
    for rel, data in outputs().items():
        path = ASSETS / rel
        if check:
            if not path.is_file():
                stale.append(rel)
            elif rel.endswith('.png'):
                if Image.open(path).convert('RGBA').tobytes() != Image.open(io.BytesIO(data)).convert('RGBA').tobytes():
                    stale.append(rel)
            elif path.read_bytes() != data:
                stale.append(rel)
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    if stale:
        raise SystemExit('zampano cabinet assets differ from tools/draw_zampano_cabinet_art.py: ' + ', '.join(stale))


if __name__ == '__main__':
    main()
