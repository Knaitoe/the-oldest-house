"""The Settling Floor (0.4.361): the gallery's floorboards settling a step at a time, its skirting, the hidden panel and the cabinet.

16x16 textures in vanilla's manner, from small palettes with per-pixel noise and fixed seeds:
- worn gallery boards at four depths of settling, their joints opening and a crack spreading as they go down;
- the boards' cut ends;
- the skirting along the plastered walls, tight, then with a dark gap opened under it;
- the far wall's panel, lower and upper halves, with daylight showing round its edges once it has shifted;
- a free-standing cabinet's front, sides and top.

The panel's plain state uses the draft rooms' plaster itself, the same texture as the walls round it, not a near likeness. Writes
the settling_fitting block's models and states and its name. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import shade, dot, fill, frame, element
from draw_sphinx_fittings import noisy
from draw_draft_fittings import plaster

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/settling/'
WALL = NS + 'block/draft/plaster'
DROP = 3  # pixels each step of settling takes off the boards' height

BOARD = ['#8c6a44', '#866440', '#926e48', '#80603c']
JOINT = '#4a3622'
DARK = '#1c140c'


def boards(stage):
    im = noisy(BOARD, 3610 + stage)
    rnd = random.Random(3620 + stage)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, JOINT)
        # The joints open as the boards go down: a second dark row, then a third.
        if stage >= 2:
            for x in range(16):
                if rnd.random() < .3 * (stage - 1):
                    dot(im, x, y - 1, DARK if stage == 3 else JOINT)
    # Butt joints, staggered board to board.
    for row, x in ((0, 5), (1, 11), (2, 2), (3, 9)):
        for y in range(row * 4, row * 4 + 3):
            dot(im, x, y, JOINT)
    # A crack spreading from the middle as it settles.
    path = [(8, 5), (7, 6), (7, 8), (8, 9), (9, 10), (9, 12), (10, 13)]
    for x, y in path[:stage * 2 + (1 if stage else 0)]:
        dot(im, x, y, DARK)
    # Grit worked up through the joints.
    for _ in range(stage * 4):
        dot(im, rnd.randrange(16), rnd.choice((2, 6, 10, 14)), '#b09a78', 200)
    return im


def board_ends():
    im = noisy(BOARD, 3615)
    for y in range(16):
        for x in (3, 7, 11, 15):
            dot(im, x, y, JOINT)
    for x in range(16):
        dot(im, x, 0, shade(BOARD[0], 18))
    return im


def skirting(gap):
    im = plaster()
    top = 10
    for x in range(16):
        dot(im, x, top, '#5a3c22'); dot(im, x, top + 1, '#7a5432')
    fill(im, 0, top + 2, 16, 16, '#6c4a2c')
    for x in range(16):
        dot(im, x, 15, '#4a3220')
        if x % 7 == 3:
            dot(im, x, 13, '#5e4028')
    if gap:
        # The board has lifted away from the floor: a dark slit under it, and the fill showing in it.
        fill(im, 0, 14, 16, 16, DARK)
        for x in range(1, 16, 3):
            dot(im, x, 15, '#3a2c1e')
    return im


def panel(top):
    """The panel shifted in its frame: daylight showing round its edges, brightest where the gap is widest."""
    im = plaster()
    light = '#f4ecd0'
    for y in range(16):
        dot(im, 0, y, light); dot(im, 15, y, light)
        if y % 3 == 0:
            dot(im, 1, y, light, 150); dot(im, 14, y, light, 150)
    edge = 0 if top else 15
    for x in range(16):
        dot(im, x, edge, light)
    # Dust lifting off the edges in the air drawn through.
    rnd = random.Random(3630 + top)
    for _ in range(8):
        x = rnd.choice((1, 2, 13, 14)); y = rnd.randrange(16)
        dot(im, x, y, '#e8e4d8', 170)
    return im


def cabinet_front():
    im = noisy(['#5a3a20', '#54361e', '#603e24'], 3640)
    frame(im, 0, 0, 16, 16, '#3a2414')
    for x in (7, 8):
        for y in range(1, 15):
            dot(im, x, y, '#3a2414')
    for x0 in (2, 10):
        frame(im, x0, 2, x0 + 4, 14, '#4a2e1a')
    for x in (6, 9):
        dot(im, x, 8, '#c8a050'); dot(im, x, 9, '#9c7a38')
    return im


def cabinet_side():
    im = noisy(['#5a3a20', '#54361e', '#603e24'], 3641)
    frame(im, 0, 0, 16, 16, '#3a2414')
    frame(im, 2, 2, 14, 14, '#4a2e1a')
    return im


def cabinet_top():
    im = noisy(['#664428', '#5e3e24', '#6a482c'], 3642)
    frame(im, 0, 0, 16, 16, '#3a2414')
    for _ in range(10):
        dot(im, random.Random(3643).randrange(1, 15), random.Random(_).randrange(1, 15), '#8a7a66', 160)
    return im


def textures():
    t = {f'boards_{s}': boards(s) for s in range(4)}
    t.update({'board_ends': board_ends(), 'skirting_0': skirting(False), 'skirting_1': skirting(True),
              'panel_lit': panel(False), 'panel_top_lit': panel(True),
              'cabinet_front': cabinet_front(), 'cabinet_side': cabinet_side(), 'cabinet_top': cabinet_top()})
    return {f'block/settling/{k}.png': v for k, v in t.items()}


def ref(name):
    return name if ':' in name else P + name


def model(elements, textures, particle, render='cutout', ao=True):
    return {'ambientocclusion': ao, 'render_type': 'minecraft:' + render, 'textures': {**{k: ref(v) for k, v in textures.items()}, 'particle': ref(particle)}, 'elements': elements}


def culled(el, faces):
    for f in faces:
        if f in el['faces']:
            el['faces'][f]['cullface'] = f
    return el


def fitting_models():
    m = {}
    for s in range(4):
        h = 16 - DROP * s
        side = [0, DROP * s, 16, 16]
        el = element([0, 0, 0], [16, h, 16], {'up': 'b', 'down': 'e', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'},
                     uv={'north': side, 'south': side, 'east': side, 'west': side})
        m[f'board_{s}'] = model([culled(el, ('down', 'north', 'south', 'east', 'west') + (('up',) if s == 0 else ()))], {'b': f'boards_{s}', 'e': 'board_ends'}, f'boards_{s}', 'solid')
        # The cabinet stands on whatever the boards under it have come down to.
        d = -DROP * s
        m[f'cabinet_{s}'] = model([element([1, d, 1], [15, 16 + d, 15], {'north': 'f', 'south': 's', 'east': 's', 'west': 's', 'up': 't', 'down': 's'}),
                                   element([0, 15 + d, 0], [16, 16 + d, 16], {'all': 't'})], {'f': 'cabinet_front', 's': 'cabinet_side', 't': 'cabinet_top'}, 'cabinet_side')
    for s in range(2):
        m[f'skirting_{s}'] = model([culled(element([0, 0, 0], [16, 16, 16], {'all': 'p', 'north': 'k'}), ('down', 'south', 'east', 'west', 'up'))],
                                   {'p': WALL, 'k': f'skirting_{s}'}, WALL, 'solid')
    # The panel: plain, the wall's own plaster; shifted, daylight round its edges; open, swung back against the passage's side.
    for kind, lit in (('panel', 'panel_lit'), ('panel_top', 'panel_top_lit')):
        m[f'{kind}_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'p'})], {'p': WALL}, WALL, 'solid')
        m[f'{kind}_1'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'p', 'north': 'l'})], {'p': WALL, 'l': lit}, WALL, 'solid')
        m[f'{kind}_2'] = model([element([0, 0, 0], [2, 16, 16], {'all': 'p'})], {'p': WALL}, WALL)
    return m


KINDS = ['board', 'skirting', 'panel', 'panel_top', 'cabinet']
STAGES = {'board': 4, 'skirting': 2, 'panel': 3, 'panel_top': 3, 'cabinet': 4}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {f'models/block/settling/{k}.json': v for k, v in fitting_models().items()}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES[kind] else STAGES[kind] - 1
                v = {'model': NS + f'block/settling/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/settling_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES[kind]):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            if tex == WALL:
                continue
            assert f'block/settling/{tex.split("settling/")[1]}.png' in images, (key, tex)
    # The hidden panel, before it shifts, is the wall's own plaster.
    assert models['panel_0']['textures']['p'] == WALL and models['panel_top_0']['textures']['p'] == WALL
    assert (A / 'textures/block/draft/plaster.png').exists()
    for rel, im in images.items():
        assert im.size == (16, 16)
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text()); key = 'block.the_oldest_house.settling_fitting'
    if check:
        assert lang.get(key) == 'Gallery Fittings', key
    else:
        lang[key] = 'Gallery Fittings'; lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the settling floor: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
