"""The replacement (0.4.218): a street of boarded houses, and the one that isn't.

Draws, in native Minecraft pixels from fixed seeds: six materials (asphalt; lapped siding in three paints, faded; the open house's
siding gone to stains; its carpet); every fitting of the replacement_fitting block, its models facing north and turned by its
facing (window boards and a boarded door, nailed over; the posters on the telephone pole; a street lamp lit or dead; the mailbox,
empty or with a page; the crate shut or open and its mesh sides; a row of collars on hooks with their tags; a blanket over one
shape or two; a cat curled very still and a dog curled very still, eyes shut; bowls; a litter tray; piled newspapers; bin bags;
stacked tins; a stained sofa). Writes the models and block states, and the blocks' names. --check verifies everything written.

Nothing here is graphic: the animals lie curled as if asleep, and the blankets are not lifted.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/replacement/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(size=(16, 16), base=None, seed=0, spread=4):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load()
    for y in range(max(0, y0), min(im.size[1], y1)):
        for x in range(max(0, x0), min(im.size[0], x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    if 0 <= x < im.size[0] and 0 <= y < im.size[1]:
        im.putpixel((x, y), shade(colour, 0, a))


# ------------------------------------------------------------------------------------------------ materials
def asphalt():
    im = new(base='#2a2a2c', seed=218, spread=6); rnd = random.Random(2181)
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(['#3a3a3c', '#1e1e20', '#444446']))
    for x in range(3, 9):
        dot(im, x + (x // 4), 11, '#1a1a1c')
    return im


def siding(base, seed, stained=False):
    im = new(base=base, seed=seed, spread=3)
    for y in range(3, 16, 4):
        for x in range(16):
            dot(im, x, y, shade(base, -24)[:3]); dot(im, x, y - 1, shade(base, 10)[:3])
    if stained:
        rnd = random.Random(seed + 1)
        for _ in range(26):
            x, y = rnd.randrange(16), rnd.randrange(16)
            dot(im, x, y, rnd.choice(['#5a4a2e', '#4a3e2a', '#6a5a3a']))
        for y in range(9, 16):
            dot(im, 5, y, '#4a3e2a')
            if y > 11:
                dot(im, 11, y, '#5a4a2e')
    return im


def carpet():
    im = new(base='#6a5040', seed=220, spread=6); rnd = random.Random(2201)
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#4a3628', '#5a4434', '#3a2a20']))
    return im


def materials():
    return {'asphalt': asphalt(), 'siding_a': siding('#9aa8b0', 221), 'siding_b': siding('#c8bc98', 222), 'siding_c': siding('#8c9a7c', 223),
            'siding_stained': siding('#a89e88', 224, True), 'carpet_stained': carpet()}


# ------------------------------------------------------------------------------------------------ fittings
def boards():
    im = Image.new('RGBA', (16, 16), (24, 20, 16, 255))
    for i, y in enumerate((1, 6, 11)):
        fill(im, 0, y, 16, y + 4, '#8a7050' if i % 2 == 0 else '#7a6244')
        for x in (2, 13):
            dot(im, x, y + 1, '#3a3a3a'); dot(im, x, y + 2, '#3a3a3a')
    return im


def board_door(stage):
    im = new(base='#5a4030', seed=230 + stage, spread=4)
    for y in range(1, 16, 5):
        fill(im, 1, y, 15, y + 3, '#8a7050')
        dot(im, 3, y + 1, '#3a3a3a'); dot(im, 12, y + 1, '#3a3a3a')
    for i in range(14):
        dot(im, 1 + i, 14 - i if stage == 0 else 1 + i, '#9a8060')
    return im


def poster():
    im = new(base='#d8d0b8', seed=232, spread=8)
    for x0, y0, c in [(0, 0, '#e8e4d0'), (5, 3, '#d0c8a8'), (9, 1, '#e0dac0')]:
        fill(im, x0, y0, x0 + 7, y0 + 9, c)
    fill(im, 3, 2, 13, 4, '#202020')  # LOST
    fill(im, 4, 5, 12, 10, '#b8b0a0')  # the pale photograph
    for y in (11, 13):
        fill(im, 3, y, 13, y + 1, '#404040')
    dot(im, 14, 1, '#808080'); dot(im, 1, 14, '#808080')
    return im


def lamp(lit):
    im = new(base='#3a3a3e', seed=233, spread=2)
    if lit:
        fill(im, 3, 8, 13, 15, '#f8e8b0'); fill(im, 5, 10, 11, 14, '#fff8d8')
    else:
        fill(im, 3, 8, 13, 15, '#5a5850')
    return im


def mailbox(page):
    im = new(base='#4a5a6a', seed=234, spread=3)
    fill(im, 3, 4, 13, 6, '#2a3440')
    if page:
        fill(im, 5, 2, 11, 5, '#f0ece0')
    dot(im, 13, 8, '#c03020'); dot(im, 13, 9, '#c03020')
    return im


def mesh():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(0, 16, 3):
        for j in range(16):
            dot(im, i, j, '#8a8a90'); dot(im, j, i, '#8a8a90')
    fill(im, 0, 0, 16, 1, '#5a5a60'); fill(im, 0, 15, 16, 16, '#5a5a60'); fill(im, 0, 0, 1, 16, '#5a5a60'); fill(im, 15, 0, 16, 16, '#5a5a60')
    return im


def collars():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 3, 16, 4, '#5a4030')
    cols = ['#c03030', '#3060c0', '#30a050', '#d0a020', '#8a40a0', '#202020', '#e070a0', '#40a0a0']
    for i, x in enumerate(range(0, 16, 2)):
        c = cols[i % len(cols)]
        dot(im, x, 4, '#808080')
        for y in range(5, 11):
            dot(im, x, y, c)
        dot(im, x, 11, '#d8c060'); dot(im, x + 1 if x < 15 else x, 11, '#d8c060')
    return im


def blanket():
    im = new(base='#7a7a8a', seed=236, spread=6)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#6a6a7a')
    return im


def fur(base, seed, stripes=False):
    im = new(base=base, seed=seed, spread=8)
    if stripes:
        for y in range(1, 16, 3):
            for x in range(16):
                dot(im, x, y, shade(base, -30)[:3])
    return im


def face(base, seed, dog=False):
    im = fur(base, seed)
    for x in (4, 10):  # eyes shut
        fill(im, x, 6, x + 3, 7, shade(base, -60)[:3])
    fill(im, 7, 9, 9, 10, '#3a2a2a' if not dog else '#202020')
    return im


def bowls():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x0, y0, c in [(1, 2, '#c04040'), (8, 1, '#4060c0'), (4, 9, '#b0b0b8'), (11, 9, '#c04040')]:
        fill(im, x0, y0, x0 + 5, y0 + 5, c); fill(im, x0 + 1, y0 + 1, x0 + 4, y0 + 4, shade(c, -50)[:3])
    return im


def litter():
    im = new(base='#8a9aaa', seed=238, spread=3)
    fill(im, 2, 2, 14, 14, '#b8a888')
    for x, y in [(5, 5), (9, 7), (6, 10), (11, 11)]:
        dot(im, x, y, '#7a6a4a')
    return im


def papers():
    im = new(base='#d8d4c8', seed=239, spread=10)
    for y in range(2, 16, 3):
        fill(im, 1, y, 15, y + 1, '#9a968a')
    return im


def bags():
    im = new(base='#1a1a1c', seed=240, spread=5)
    for x, y in [(3, 4), (10, 6), (6, 11)]:
        dot(im, x, y, '#3a3a40'); dot(im, x + 1, y, '#3a3a40')
    return im


def cans():
    im = new(base='#a8a8b0', seed=241, spread=8)
    for y in (2, 8, 13):
        fill(im, 0, y, 16, y + 2, '#c04030' if y == 8 else '#d8c070')
    return im


def sofa():
    im = new(base='#6a5a4a', seed=242, spread=5); rnd = random.Random(2421)
    for _ in range(12):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#4a3e30', '#5a4a3a', '#8a7a64']))
    return im


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    m['boards_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b'})], {'b': 'boards'}, 'boards')
    for s in range(2):
        m[f'board_door_{s}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b', 'north': 'd'})], {'b': 'boards', 'd': f'board_door_{s}'}, 'boards')
        m[f'lamp_{s}'] = model([element([3, 0, 3], [13, 2, 13], {'all': 'm'}), element([4, 2, 4], [12, 6, 12], {'all': 'm', 'down': 'g'})], {'m': 'lamp_metal', 'g': f'lamp_{s}'}, 'lamp_metal')
        m[f'mailbox_{s}'] = model([element([7, 0, 7], [9, 8, 9], {'all': 'p'}), element([4, 8, 3], [12, 14, 13], {'all': 'b', 'north': 'f', 'up': 'b'})], {'p': 'post', 'b': 'mailbox_side', 'f': f'mailbox_{s}'}, 'mailbox_side')
        m[f'blanket_{s}'] = model([element([1, 0, 1], [15, 1, 15], {'all': 'b'}), element([3, 1, 4], [12, 4, 12], {'all': 'b'})] + ([element([9, 1, 2], [14, 5, 8], {'all': 'b'})] if s else []), {'b': 'blanket'}, 'blanket')
    m['crate_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'm'})], {'m': 'mesh'}, 'mesh')
    m['crate_1'] = model([element([0, 0, 15], [2, 16, 16], {'all': 'm'}), element([0, 0, 0], [1, 16, 16], {'all': 'm'})], {'m': 'mesh'}, 'mesh')
    m['crate_side_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'm'})], {'m': 'mesh'}, 'mesh')
    m['poster_0'] = model([element([2, 2, 15.5], [14, 14, 16], {'north': 'p', 'south': 'p'}, {'north': [2, 2, 14, 14], 'south': [2, 2, 14, 14]})], {'p': 'poster'}, 'poster')
    m['collars_0'] = model([element([0, 6, 15.5], [16, 14, 16], {'north': 'c', 'south': 'c'}, {'north': [0, 2, 16, 12], 'south': [0, 2, 16, 12]})], {'c': 'collars'}, 'collars')
    m['cat_0'] = model([element([4, 0, 4], [12, 4, 11], {'all': 'f'}), element([9, 0, 9], [13, 4, 13], {'all': 'f', 'north': 'h'}), element([3, 0, 10], [9, 1, 12], {'all': 'f'})],
                       {'f': 'cat_fur', 'h': 'cat_face'}, 'cat_fur')
    m['dog_0'] = model([element([2, 0, 3], [14, 6, 12], {'all': 'f'}), element([10, 0, 9], [15, 5, 14], {'all': 'f', 'north': 'h'}), element([1, 0, 10], [8, 2, 13], {'all': 'f'})],
                       {'f': 'dog_fur', 'h': 'dog_face'}, 'dog_fur')
    m['bowls_0'] = model([element([1, 0, 1], [15, 2, 15], {'up': 'b', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'}, {'up': [0, 0, 16, 16]})], {'b': 'bowls', 'e': 'bowl_edge'}, 'bowls')
    m['litter_0'] = model([element([1, 0, 1], [15, 3, 15], {'all': 'e', 'up': 'l'})], {'l': 'litter', 'e': 'bowl_edge'}, 'litter')
    m['papers_0'] = model([element([1, 0, 2], [14, 4, 14], {'all': 'p'}), element([3, 4, 3], [15, 6, 13], {'all': 'p'})], {'p': 'papers'}, 'papers')
    m['bags_0'] = model([element([1, 0, 1], [9, 9, 9], {'all': 'b'}), element([7, 0, 6], [15, 7, 15], {'all': 'b'})], {'b': 'bags'}, 'bags')
    m['cans_0'] = model([element([2, 0, 2], [7, 4, 7], {'all': 'c'}), element([8, 0, 3], [13, 4, 8], {'all': 'c'}), element([4, 0, 9], [9, 4, 14], {'all': 'c'}), element([5, 4, 4], [10, 8, 9], {'all': 'c'})],
                        {'c': 'cans'}, 'cans')
    m['sofa_0'] = model([element([0, 0, 0], [16, 8, 16], {'all': 's'}), element([0, 8, 10], [16, 16, 16], {'all': 's'})], {'s': 'sofa'}, 'sofa')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/replacement/{k}.png'] = v
    out['block/replacement/boards.png'] = boards()
    for s in range(2):
        out[f'block/replacement/board_door_{s}.png'] = board_door(s)
        out[f'block/replacement/lamp_{s}.png'] = lamp(s == 0)
        out[f'block/replacement/mailbox_{s}.png'] = mailbox(s == 1)
    out['block/replacement/lamp_metal.png'] = new(base='#2a2a2e', seed=250, spread=2)
    out['block/replacement/post.png'] = new(base='#3a3a3e', seed=251, spread=2)
    out['block/replacement/mailbox_side.png'] = new(base='#4a5a6a', seed=252, spread=3)
    out['block/replacement/poster.png'] = poster()
    out['block/replacement/mesh.png'] = mesh()
    out['block/replacement/collars.png'] = collars()
    out['block/replacement/blanket.png'] = blanket()
    out['block/replacement/cat_fur.png'] = fur('#8a7a6a', 253, True); out['block/replacement/cat_face.png'] = face('#8a7a6a', 254)
    out['block/replacement/dog_fur.png'] = fur('#8a6a4a', 255); out['block/replacement/dog_face.png'] = face('#8a6a4a', 256, True)
    out['block/replacement/bowls.png'] = bowls(); out['block/replacement/bowl_edge.png'] = new(base='#9a9aa0', seed=257, spread=3)
    out['block/replacement/litter.png'] = litter(); out['block/replacement/papers.png'] = papers(); out['block/replacement/bags.png'] = bags()
    out['block/replacement/cans.png'] = cans(); out['block/replacement/sofa.png'] = sofa()
    return out


KINDS = ['boards', 'board_door', 'poster', 'lamp', 'mailbox', 'crate', 'crate_side', 'collars', 'blanket', 'cat', 'dog', 'bowls', 'litter', 'papers', 'bags', 'cans', 'sofa']
STAGES = {'board_door': 2, 'lamp': 2, 'crate': 2, 'blanket': 2, 'mailbox': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'replacement_fitting': 'Street Fitting', 'replacement_asphalt': 'Asphalt', 'replacement_siding_a': 'Siding', 'replacement_siding_b': 'Siding',
         'replacement_siding_c': 'Siding', 'replacement_siding_stained': 'Stained Siding', 'replacement_carpet_stained': 'Stained Carpet'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/replacement_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/replacement_{name}.json'] = {'variants': {'': {'model': NS + f'block/replacement_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/replacement/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(2):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/replacement/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/replacement_fitting.json'] = {'variants': variants}
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
            assert f'block/replacement/{tex.split("replacement/")[1]}.png' in images, (key, tex)
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
    for block, name in NAMES.items():
        key = f'block.the_oldest_house.{block}'
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the street: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
