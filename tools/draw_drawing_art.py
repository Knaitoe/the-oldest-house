"""The Drawing (0.4.407): the village square on a bright summer morning.

Draws, in native Minecraft pixels from fixed seeds: three materials (the square's flagstones with grass in the joints, the painted
weatherboard of the post office and the houses, the bank's brick), and every fitting of the drawing_fitting block, its models facing
north and turned by its facing: the black box with its slot, its split corner bound with string, then wire, then a bootlace, then worn
almost bare; the three-legged stool; the typed notice pinned to the post office board, and beside it a bare pin or a folded paper; the
pile of smooth stones by the wall, from a few to a heap; a house window curtained or shuttered; the grocery's striped awning; the painted
signs over the doors (an envelope, columns, an apple, a bell); and a produce crate of apples or of beans. Also the carried things' item
pictures: the folded slip and the same slip opened, blank; the smooth stone; the glass of lemonade; and the jug. Writes the models and
block states and the blocks' and items' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/drawing/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(16):
            for x in range(16):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load()
    for y in range(max(0, y0), min(16, y1)):
        for x in range(max(0, x0), min(16, x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), shade(colour, 0, a))


def frame(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


# ------------------------------------------------------------------------------------------------ materials
def flagstone():
    # Big pale flags of uneven size, laid in two courses a block; grass comes up green in the joints.
    im = new(base='#a8a69c', seed=4070, spread=5); rnd = random.Random(4071)
    joints = '#6f7a4a'
    for y in (0, 7):
        for x in range(16):
            dot(im, x, y, joints if rnd.random() < .7 else '#7b7466')
    for x, y0, y1 in ((5, 0, 7), (12, 0, 7), (2, 7, 16), (9, 7, 16)):
        for y in range(y0, y1):
            dot(im, x, y, joints if rnd.random() < .6 else '#7b7466')
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.choice((0, 7))
        dot(im, x, y, '#8aa05a')
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#a8a69c', rnd.choice((-12, 9)))[:3])
    return im


def weatherboard():
    # White painted boards, each four pixels deep, the lap's shadow under each, the paint weathered thin in places.
    base = '#e4e0d4'
    im = new(base=base, seed=4072, spread=3); rnd = random.Random(4073)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#b9b4a6')
    for _ in range(9):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#cfc8b4')
    return im


def brick():
    base = '#8e4a3a'
    im = new(base=base, seed=4074, spread=6)
    mortar = '#c9bfae'
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, mortar)
    for row, y0 in enumerate((0, 4, 8, 12)):
        off = 0 if row % 2 == 0 else 4
        for x in range(off, 16, 8):
            for y in range(y0, y0 + 3):
                dot(im, x, y, mortar)
    return im


def materials():
    return {'flagstone': flagstone(), 'weatherboard': weatherboard(), 'brick': brick()}


# ------------------------------------------------------------------------------------------------ fittings' faces
def box_side(stage):
    # Black paint chipped down to grey wood, more of it gone each year.
    rnd = random.Random(4080 + stage)
    im = new(base='#1f1d1c' if stage < 2 else '#2c2925', seed=4080 + stage, spread=3)
    for _ in range(4 + stage * 6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#6c6458'); dot(im, x + 1, y, '#6c6458')
    frame(im, 0, 0, 16, 16, '#141312')
    return im


def box_front(stage):
    # The front with the split corner bound: string, wire, a bootlace, then the split wider and the lace frayed.
    im = box_side(stage)
    split = '#8a7a5e'
    for y in range(2, 14 if stage < 3 else 15):
        dot(im, 12 + (y % 3 == 0), y, split)
    if stage == 3:
        for y in range(4, 13):
            dot(im, 13, y, split)
    bind = {0: '#d8cfae', 1: '#9aa0a4', 2: '#5a3a2a', 3: '#4a3022'}[stage]
    for y in (4, 7, 10):
        for x in range(10, 16):
            dot(im, x, y, bind)
        if stage == 1:
            dot(im, 15, y + 1, '#c6ccd0')
    if stage >= 2:
        dot(im, 9, 7, bind); dot(im, 8, 8, bind); dot(im, 9, 9, bind)
    return im


def box_top(stage):
    im = box_side(stage)
    fill(im, 4, 7, 12, 9, '#070707')
    return im


def stool():
    im = new(base='#8a6a46', seed=4085, spread=5)
    for y in (5, 11):
        for x in range(16):
            dot(im, x, y, '#6e5236')
    return im


def cork():
    im = new(base='#a77b4e', seed=4086, spread=10)
    frame(im, 0, 0, 16, 16, '#5a3e26')
    return im


def notice():
    # The typed notice on the cork: a white sheet, a heading line and close rows of type, a pin at the top.
    im = cork()
    fill(im, 2, 1, 14, 15, '#f2efe4')
    fill(im, 4, 3, 12, 4, '#222222')
    rnd = random.Random(4087)
    for y in range(6, 14, 2):
        for x in range(3, 13):
            if rnd.random() < .78:
                dot(im, x, y, '#505050')
    dot(im, 8, 1, '#c02a22'); dot(im, 8, 0, '#8a1a14')
    return im


def account(stage):
    im = cork()
    if stage == 1:
        fill(im, 4, 3, 12, 12, '#ece6d6')
        for x in range(4, 12):
            dot(im, x, 7, '#cfc6b0')
        for y in (5, 9):
            for x in range(5, 11):
                dot(im, x, y, '#6a6a7a')
    dot(im, 8, 3, '#c02a22'); dot(im, 8, 2, '#8a1a14')
    return im


def pebbles():
    # Smooth stones from the lane: rounded greys and buffs, each with a light top and a dark underside.
    im = new(base='#7a7a72', seed=4088, spread=4); rnd = random.Random(4089)
    for _ in range(9):
        cx, cy = rnd.randrange(1, 15), rnd.randrange(1, 15)
        tone = rnd.choice(('#9a9a92', '#b2aa98', '#8c8a84', '#a6a29a'))
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1), (-1, 0), (2, 1)):
            dot(im, cx + dx, cy + dy, tone)
        dot(im, cx, cy - 1, shade(tone, 18)[:3]); dot(im, cx + 1, cy + 2, shade(tone, -22)[:3])
    return im


def window(shut):
    # A house window. Open: white frame, four panes, a curtain drawn back each side. Shut: two boarded shutters nailed across.
    im = new(base='#e4e0d4', seed=4090, spread=2)
    if not shut:
        fill(im, 2, 2, 14, 14, '#5a7086')
        fill(im, 2, 2, 5, 14, '#d8c8a8'); fill(im, 11, 2, 14, 14, '#d8c8a8')
        for x in range(2, 14):
            dot(im, x, 8, '#f2efe4')
        for y in range(2, 14):
            dot(im, 8, y, '#f2efe4')
        dot(im, 6, 4, '#8aa0b4'); dot(im, 10, 10, '#8aa0b4')
        frame(im, 1, 1, 15, 15, '#f2efe4')
    else:
        fill(im, 1, 1, 15, 15, '#4e5a48')
        for x in (7, 8):
            for y in range(1, 15):
                dot(im, x, y, '#3a4436')
        for y in (3, 8, 13):
            for x in range(1, 15):
                dot(im, x, y, '#5e6a56')
        fill(im, 0, 6, 16, 8, '#7a5a3a')
        for x in (2, 13):
            dot(im, x, 6, '#2a2a2a'); dot(im, x, 7, '#2a2a2a')
        frame(im, 0, 0, 16, 16, '#d8d4c8')
    return im


def awning():
    im = new()
    for x in range(16):
        tone = '#3e7a4a' if (x // 2) % 2 == 0 else '#efeadc'
        for y in range(16):
            dot(im, x, y, shade(tone, -6 if y > 12 else 0)[:3])
    return im


def sign(kind):
    # A painted sign board: an envelope for the post office, columns for the bank, an apple for the grocery, a bell for the hall.
    im = new(base='#2e4a3a', seed=4091 + kind, spread=2)
    frame(im, 0, 0, 16, 16, '#d8c27a')
    gold, white, red = '#e6d08a', '#f2efe4', '#b8342a'
    if kind == 0:
        fill(im, 3, 4, 13, 12, white)
        for i in range(5):
            dot(im, 3 + i, 4 + i, '#9a9a8a'); dot(im, 12 - i, 4 + i, '#9a9a8a')
    elif kind == 1:
        fill(im, 3, 3, 13, 5, gold)
        for x in (4, 7, 10):
            fill(im, x, 5, x + 2, 12, white)
        fill(im, 3, 12, 13, 13, gold)
    elif kind == 2:
        fill(im, 5, 6, 11, 12, red); fill(im, 6, 5, 10, 13, red)
        dot(im, 8, 4, '#5a3a22'); dot(im, 9, 3, '#4a8a3a'); dot(im, 10, 3, '#4a8a3a')
        dot(im, 6, 7, '#e86a5a')
    else:
        fill(im, 6, 4, 10, 5, gold); fill(im, 5, 5, 11, 10, gold); fill(im, 4, 10, 12, 11, gold)
        dot(im, 8, 11, '#8a6a2a'); dot(im, 8, 3, gold)
    return im


def crate_side():
    im = new(base='#b08a5a', seed=4095, spread=5)
    for y in (0, 5, 10, 15):
        for x in range(16):
            dot(im, x, y, '#7a5a36')
    for x in (0, 15):
        for y in range(16):
            dot(im, x, y, '#7a5a36')
    return im


def crate_top(beans):
    im = new(base='#3a2a1a', seed=4096, spread=2); rnd = random.Random(4097 + beans)
    for _ in range(22 if beans else 10):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        if beans:
            dot(im, x, y, '#5a8a3a'); dot(im, x + 1, y, '#4a7a2e')
        else:
            dot(im, x, y, '#c0382a'); dot(im, x + 1, y, '#a82a22'); dot(im, x, y + 1, '#a82a22'); dot(im, x + 1, y + 1, '#8a2018'); dot(im, x, y, '#e06a5a')
    frame(im, 0, 0, 16, 16, '#7a5a36')
    return im


# ------------------------------------------------------------------------------------------------ items
def slip_item(open_):
    im = new()
    if not open_:
        # A slip folded twice: a small square of paper, its creases showing.
        fill(im, 4, 5, 12, 11, '#efeadc'); frame(im, 4, 5, 12, 11, '#bdb6a2')
        for x in range(5, 11):
            dot(im, x, 8, '#d0c8b2')
        dot(im, 11, 5, '#ffffff')
    else:
        # The same slip unfolded flat: blank, the fold lines across it.
        fill(im, 2, 3, 14, 13, '#f2efe4'); frame(im, 2, 3, 14, 13, '#bdb6a2')
        for x in range(3, 13):
            dot(im, x, 8, '#d8d0bc')
        for y in range(4, 12):
            dot(im, 8, y, '#d8d0bc')
    return im


def stone_item():
    im = new()
    for y in range(5, 12):
        for x in range(4, 13):
            dx, dy = (x - 8) / 4.6, (y - 8.4) / 3.4
            if dx * dx + dy * dy <= 1:
                tone = '#9c988e' if dy > -.2 else '#b6b2a6'
                if dy > .55:
                    tone = '#7e7a72'
                dot(im, x, y, tone)
    dot(im, 7, 6, '#d2cec2'); dot(im, 8, 6, '#c8c4b8')
    return im


def lemonade_item():
    im = new()
    for y in range(4, 14):
        for x in range(5, 11):
            dot(im, x, y, '#f2e88a' if y > 6 else '#e8f4f6', 255 if y > 6 else 150)
    for y in range(4, 14):
        dot(im, 4, y, '#cfe4ea'); dot(im, 11, y, '#cfe4ea')
    for x in range(4, 12):
        dot(im, x, 14, '#cfe4ea')
    dot(im, 7, 9, '#fff8c0'); dot(im, 8, 11, '#fff8c0')
    return im


def jug_item():
    im = new()
    for y in range(3, 14):
        w = 3 if y < 5 else 4
        for x in range(8 - w, 8 + w):
            dot(im, x, y, '#f2e88a' if 6 <= y else '#e6f0f2', 255 if 6 <= y else 170)
    for y in range(5, 11):
        dot(im, 12, y, '#cfe4ea')
    dot(im, 11, 5, '#cfe4ea'); dot(im, 11, 10, '#cfe4ea'); dot(im, 4, 3, '#cfe4ea')
    return im


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/drawing_{name}.png'] = im
    faces = {
        'stool': stool(), 'cork': cork(), 'notice': notice(), 'account_0': account(0), 'account_1': account(1), 'pebbles': pebbles(),
        'window_open': window(False), 'window_shut': window(True), 'awning': awning(), 'crate_side': crate_side(),
        'crate_apples': crate_top(0), 'crate_beans': crate_top(1), 'weatherboard': weatherboard(), 'sign_back': new(base='#2e4a3a', seed=4099, spread=2),
    }
    for stage in range(4):
        faces[f'box_side_{stage}'] = box_side(stage); faces[f'box_front_{stage}'] = box_front(stage); faces[f'box_top_{stage}'] = box_top(stage)
        faces[f'sign_{stage}'] = sign(stage)
    for name, im in faces.items():
        out[f'block/drawing/{name}.png'] = im
    out['item/drawing_slip.png'] = slip_item(False)
    out['item/drawing_slip_open.png'] = slip_item(True)
    out['item/drawing_stone.png'] = stone_item()
    out['item/drawing_lemonade.png'] = lemonade_item()
    out['item/drawing_jug.png'] = jug_item()
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rot=None):
    faces = {}
    keys = ['north', 'south', 'east', 'west', 'up', 'down']
    if 'all' in textures:
        textures = {**{k: textures['all'] for k in keys}, **{k: v for k, v in textures.items() if k != 'all'}}
    for k in keys:
        if k in textures:
            face = {'texture': '#' + textures[k]}
            if uv and k in uv:
                face['uv'] = uv[k]
            faces[k] = face
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    for stage in range(4):
        m[f'box_{stage}'] = model([element([3, 0, 4], [13, 9, 12], {'all': 's', 'north': 'f', 'up': 't', 'down': 's'})],
                                  {'s': f'box_side_{stage}', 'f': f'box_front_{stage}', 't': f'box_top_{stage}'}, f'box_side_{stage}')
    m['stool_0'] = model([element([2, 13, 2], [14, 16, 14], {'all': 'w'}),
                          element([3, 0, 3], [5, 13, 5], {'all': 'w'}), element([11, 0, 3], [13, 13, 5], {'all': 'w'}),
                          element([7, 0, 11], [9, 13, 13], {'all': 'w'}),
                          element([4, 5, 4], [12, 6, 5], {'all': 'w'})], {'w': 'stool'}, 'stool')
    m['notice_0'] = model([element([0, 2, 15], [16, 15, 16], {'all': 'c', 'north': 'n'})], {'c': 'cork', 'n': 'notice'}, 'notice')
    for stage in range(2):
        m[f'account_{stage}'] = model([element([0, 2, 15], [16, 15, 16], {'all': 'c', 'north': 'a'})], {'c': 'cork', 'a': f'account_{stage}'}, f'account_{stage}')
    for stage in range(4):
        e = []
        layout = [(2, 2, 6, 6), (9, 3, 13, 7), (4, 9, 8, 13), (10, 10, 14, 14), (6, 5, 10, 10), (2, 11, 5, 14)]
        for i, (x0, z0, x1, z1) in enumerate(layout[:3 + stage]):
            e.append(element([x0, 0, z0], [x1, 2 + (i % 2), z1], {'all': 'p'}))
        if stage >= 2:
            e.append(element([4, 2, 4], [11, 4, 11], {'all': 'p'}))
        if stage >= 3:
            e.append(element([5, 4, 5], [10, 6, 10], {'all': 'p'}))
        m[f'stones_{stage}'] = model(e, {'p': 'pebbles'}, 'pebbles')
    for stage, face in enumerate(('window_open', 'window_shut')):
        m[f'shutter_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b', 'north': 'w'})], {'b': 'weatherboard', 'w': face}, face)
    m['awning_0'] = model([element([0, 6, 0], [16, 8, 16], {'all': 'a'}, rot={'origin': [8, 8, 16], 'axis': 'x', 'angle': -22.5}),
                           element([0, 3, 0], [16, 6, 1], {'all': 'a'})], {'a': 'awning'}, 'awning')
    for stage in range(4):
        m[f'sign_{stage}'] = model([element([0, 3, 14], [16, 13, 16], {'all': 'b', 'north': 's'})], {'b': 'sign_back', 's': f'sign_{stage}'}, f'sign_{stage}')
    for stage, top in enumerate(('crate_apples', 'crate_beans')):
        m[f'crate_{stage}'] = model([element([1, 0, 1], [15, 10, 15], {'all': 's', 'up': 't'})], {'s': 'crate_side', 't': top}, 'crate_side')
    return m


KINDS = ['box', 'stool', 'notice', 'account', 'stones', 'shutter', 'awning', 'sign', 'crate']
STAGES = {'box': 4, 'account': 2, 'stones': 4, 'shutter': 2, 'sign': 4, 'crate': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'drawing_fitting': 'Village Fitting', 'drawing_flagstone': 'Flagstones', 'drawing_weatherboard': 'Painted Weatherboard', 'drawing_brick': 'Bank Brick'}
ITEM_NAMES = {'item.the_oldest_house.drawing_slip': 'Folded Slip', 'item.the_oldest_house.drawing_stone': 'Smooth Stone',
              'item.the_oldest_house.drawing_lemonade': 'Glass of Lemonade', 'item.the_oldest_house.drawing_jug': 'Lemonade Jug'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/drawing_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/drawing_{name}'}}
        out[f'blockstates/drawing_{name}.json'] = {'variants': {'': {'model': NS + f'block/drawing_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/drawing/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/drawing/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/drawing_fitting.json'] = {'variants': variants}
    out['models/item/drawing_slip.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/drawing_slip'},
                                            'overrides': [{'predicate': {'custom_model_data': 1}, 'model': NS + 'item/drawing_slip_open'}]}
    out['models/item/drawing_slip_open.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/drawing_slip_open'}}
    for item in ('stone', 'lemonade', 'jug'):
        out[f'models/item/drawing_{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/drawing_{item}'}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/drawing/{tex.split("drawing/")[1]}.png' in images, (key, tex)
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
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **ITEM_NAMES}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the village square: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
