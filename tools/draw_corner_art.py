"""The House on the Corner (0.4.408): a city street at siesta, and the empty house on the corner.

Draws, in native Minecraft pixels from fixed seeds: five materials (the houses' painted render, the pavement slabs, the road's old stone
setts, the house's patterned floor tiles, and the corridor's faded paper), and every fitting of the corner_fitting block, its models
facing north and turned by its facing: one cell of the tall iron gate, shut or swung open, lower and upper; a board on the front room's
wall, bare or with a glass jar on it of small teeth, of clipped fingernails, or of something pale that can't be made out; a wall lamp
with a frosted shade, dark or lit; a tall window behind its shutters, dark or lit through the slats; the kiosk's newspaper rack; the dog
asleep; a school exercise book left on the kerb; a pebble on the kerb, alone or holding down a folded paper; jacaranda petals fallen on
the pavement; and the jacarandas' flowering crowns. Writes the models and block states and the blocks' names. --check verifies
everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/corner/'


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


# ------------------------------------------------------------------------------------------------ materials
def stucco():
    im = new('#e6d9bf', 40801, 5); rnd = random.Random(40802)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#d8c9ab')
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#f0e6d0')
    # A hairline crack wandering down from the top.
    x = 11
    for y in range(0, 7):
        dot(im, x, y, '#bfae8e'); x += rnd.choice((-1, 0, 0, 1))
    return im


def paving():
    im = new('#b9b5ab', 40803, 4)
    for i in range(16):
        dot(im, i, 0, '#8f8b82'); dot(im, i, 8, '#8f8b82'); dot(im, 0, i, '#8f8b82'); dot(im, 8, i, '#8f8b82')
    rnd = random.Random(40804)
    for _ in range(8):
        dot(im, rnd.randrange(1, 16), rnd.randrange(1, 16), '#a8a49a')
    dot(im, 5, 11, '#cbc7bd'); dot(im, 12, 4, '#cbc7bd')
    return im


def setts():
    im = new('#6f6a63', 40805, 6); rnd = random.Random(40806)
    for row in range(4):
        y0 = row * 4; off = 2 if row % 2 else 0
        for i in range(16):
            dot(im, i, y0, '#3e3a35')
        for x in range(-off, 16, 4):
            for y in range(y0, y0 + 4):
                dot(im, x + off, y, '#3e3a35')
        for x in range(off, 16, 4):
            dot(im, (x + 1) % 16, y0 + 1, '#86807a')
    for _ in range(6):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#5c5852')
    return im


def tiles():
    im = new('#e8dcc4', 40807, 2)
    for qx in (0, 8):
        for qy in (0, 8):
            fill(im, qx, qy, qx + 8, qy + 1, '#b9a68a'); fill(im, qx, qy, qx + 1, qy + 8, '#b9a68a')
            # A small four-petal figure in each tile's middle, and corners that meet as diamonds across the joints.
            cx, cy = qx + 4, qy + 4
            for dx, dy in ((0, -2), (0, 2), (-2, 0), (2, 0)):
                dot(im, cx + dx, cy + dy, '#a8573e')
            for dx, dy in ((0, -1), (0, 1), (-1, 0), (1, 0)):
                dot(im, cx + dx, cy + dy, '#3e6a68')
            dot(im, cx, cy, '#2d4c4a')
            for dx, dy in ((1, 1), (7, 1), (1, 7), (7, 7)):
                dot(im, qx + dx, qy + dy, '#a8573e')
    return im


def wallpaper():
    im = new('#d9cfb6', 40809, 3); rnd = random.Random(40810)
    for y in range(16):
        dot(im, 3, y, '#c8bc9e'); dot(im, 11, y, '#c8bc9e')
    for x, y in ((7, 2), (7, 10), (15, 6), (15, 14)):
        dot(im, x, y, '#8c9a76'); dot(im, x - 1, y + 1, '#8c9a76'); dot(im, x + 1, y + 1, '#8c9a76'); dot(im, x, y + 1, '#a8705c')
    for _ in range(6):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#cfc3a6')
    return im


def materials():
    return {'stucco': stucco(), 'paving': paving(), 'setts': setts(), 'tiles': tiles(), 'wallpaper': wallpaper()}


# ------------------------------------------------------------------------------------------------ fittings
IRON = '#2a2826'


def gate(upper):
    im = new()
    for x in range(1, 16, 3):
        for y in range(16):
            dot(im, x, y, IRON)
    for x in range(16):
        dot(im, x, 14 if not upper else 2, IRON)
        if not upper:
            dot(im, x, 2, IRON)
    if upper:
        for x in range(1, 16, 3):
            for y in range(0, 2):
                dot(im, x, y, (0, 0, 0, 0)) if y == 0 else dot(im, x, y, '#3a3734')
            dot(im, x, 0, IRON)
    for y in range(16):
        dot(im, 0, y, IRON); dot(im, 15, y, IRON)
    return im


def board():
    im = new('#7a5a3c', 40811, 5)
    for x in range(16):
        dot(im, x, 0, '#5c4129'); dot(im, x, 15, '#5c4129')
    return im


def glass():
    im = new()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y == 15:
                dot(im, x, y, '#cfe0dc', 170)
    for y in range(2, 13):
        dot(im, 3, y, '#f4fbfa', 200)
    dot(im, 4, 3, '#f4fbfa', 200)
    return im


def lid():
    im = new('#8f8a80', 40812, 6)
    for x in range(16):
        dot(im, x, 15, '#6c675e')
    return im


def teeth():
    im = new('#cfc5ad', 40813, 3); rnd = random.Random(40814)
    for _ in range(40):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#f2ecdc'); dot(im, x, min(15, y + 1), '#ddd3bb')
    return im


def nails():
    im = new('#c7b19e', 40815, 3); rnd = random.Random(40816)
    for _ in range(26):
        x, y = rnd.randrange(15), rnd.randrange(16)
        dot(im, x, y, '#e8d6c4'); dot(im, x + 1, y, '#e8d6c4')
    return im


def murk():
    im = new('#9b9a8a', 40817, 8)
    # Something folded on itself: a paler shape, its edges lost in the cloud.
    for y in range(5, 12):
        for x in range(4 + (y % 3 == 0), 12 - (y % 4 == 0)):
            dot(im, x, y, '#cfcab6')
    for x in range(5, 11):
        dot(im, x, 8, '#b5af99')
    return im


def lamp(lit):
    im = new('#f6efd8' if lit else '#bdb7a6', 40818, 2)
    for x in range(16):
        dot(im, x, 0, '#a08a52'); dot(im, x, 15, '#a08a52')
    return im


def brass():
    return new('#a08a52', 40819, 6)


def shutter(lit):
    im = new('#3f5a44', 40820, 3)
    for y in range(1, 16, 3):
        for x in range(1, 15):
            dot(im, x, y, '#2c4030')
            if lit:
                dot(im, x, y + 1, '#f3d48a')
    for y in range(16):
        dot(im, 0, y, '#263a2a'); dot(im, 15, y, '#263a2a'); dot(im, 7, y, '#263a2a')
    for x in range(16):
        dot(im, x, 0, '#263a2a'); dot(im, x, 15, '#263a2a')
    return im


def rack():
    im = new('#3a5a3e', 40821, 3); rnd = random.Random(40822)
    for row, y0 in enumerate((2, 7, 12)):
        for x0 in (1, 6, 11):
            c = rnd.choice(('#ece6d4', '#e0d8c2', '#d9cfb6'))
            fill(im, x0, y0, x0 + 4, y0 + 4, c)
            for x in range(x0, x0 + 4):
                dot(im, x, y0 + 1, '#5a5650')
            dot(im, x0 + 1, y0 + 3, '#8a8478')
    return im


def kiosk_side():
    return new('#3a5a3e', 40823, 3)


def fur():
    im = new('#b28a5e', 40824, 6); rnd = random.Random(40825)
    for _ in range(12):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#9a754c')
    return im


def dog_face():
    im = fur()
    fill(im, 2, 3, 5, 9, '#7a5a3a'); fill(im, 11, 3, 14, 9, '#7a5a3a')
    dot(im, 6, 9, '#3a2a1e'); dot(im, 9, 9, '#3a2a1e')  # eyes shut, a line each
    dot(im, 7, 9, '#3a2a1e'); dot(im, 8, 9, '#3a2a1e')
    fill(im, 6, 12, 10, 14, '#2a201a')
    return im


def notebook():
    im = new('#4f6a8a', 40826, 3)
    fill(im, 3, 4, 13, 9, '#ece6d4')
    for x in range(4, 12):
        dot(im, x, 6, '#8a8478')
    for x in range(4, 9):
        dot(im, x, 7, '#8a8478')
    for y in range(16):
        dot(im, 0, y, '#3a5070')
    return im


def paper_edge():
    im = new('#ece6d4', 40827, 2)
    for x in range(0, 16, 2):
        dot(im, x, 8, '#cfc8b4')
    return im


def pebble():
    im = new('#9a958c', 40828, 6)
    dot(im, 4, 4, '#b8b3a8'); dot(im, 5, 4, '#b8b3a8')
    return im


def folded():
    im = new('#f0eadb', 40829, 2)
    for i in range(16):
        dot(im, i, i, '#d5cdb8')
    for x in range(3, 13):
        dot(im, x, 5, '#9a958a')
    for x in range(3, 10):
        dot(im, x, 7, '#9a958a')
    return im


def petals():
    im = new(); rnd = random.Random(40830)
    for _ in range(26):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(('#8a6ac8', '#9f86d8', '#7458b8')))
    return im


def blossom():
    im = new(); rnd = random.Random(40831); px = im.load()
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            if r < .78:
                px[x, y] = shade(rnd.choice(('#8a6ac8', '#9f86d8', '#7458b8', '#6e4fb0')), rnd.randint(-6, 6))
            elif r < .86:
                px[x, y] = shade('#4c6a3a', rnd.randint(-6, 6))
    return im


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/corner_{name}.png'] = im
    faces = {
        'gate_lower': gate(False), 'gate_upper': gate(True), 'board': board(), 'glass': glass(), 'lid': lid(), 'teeth': teeth(), 'nails': nails(),
        'murk': murk(), 'lamp_off': lamp(False), 'lamp_on': lamp(True), 'brass': brass(), 'shutter_shut': shutter(False), 'shutter_lit': shutter(True),
        'stucco': stucco(), 'rack': rack(), 'kiosk_side': kiosk_side(), 'fur': fur(), 'dog_face': dog_face(), 'notebook': notebook(),
        'paper_edge': paper_edge(), 'pebble': pebble(), 'folded': folded(), 'petals': petals(), 'blossom': blossom(),
    }
    for name, im in faces.items():
        out[f'block/corner/{name}.png'] = im
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
        tex = 'gate_upper' if stage >= 2 else 'gate_lower'
        if stage % 2 == 0:
            m[f'gate_{stage}'] = model([element([0, 0, 7.5], [16, 16, 8.5], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g'})], {'g': tex}, tex)
        else:
            m[f'gate_{stage}'] = model([element([0.5, 0, 0], [1.5, 16, 16], {'east': 'g', 'west': 'g', 'north': 'g', 'south': 'g'})], {'g': tex}, tex)
    shelf = [element([0, 3, 8], [16, 4, 16], {'all': 'b'}), element([2, 1, 15], [3, 3, 16], {'all': 'b'}), element([13, 1, 15], [14, 3, 16], {'all': 'b'})]
    m['jar_0'] = model(shelf, {'b': 'board'}, 'board')
    for stage, contents in ((1, 'teeth'), (2, 'nails'), (3, 'murk')):
        m[f'jar_{stage}'] = model(shelf + [
            element([5.5, 4.2, 9.5], [10.5, 10, 14.5], {'all': 'c'}),
            element([5, 4, 9], [11, 12, 15], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g'}),
            element([5, 12, 9], [11, 13, 15], {'all': 'l'})], {'b': 'board', 'c': contents, 'g': 'glass', 'l': 'lid'}, 'glass')
    for stage, shade_ in ((0, 'lamp_off'), (1, 'lamp_on')):
        m[f'lamp_{stage}'] = model([element([7.5, 9, 14], [8.5, 10, 16], {'all': 'm'}), element([7, 12, 12], [9, 13, 16], {'all': 'm'}),
                                    element([5, 6, 10], [11, 12, 14], {'all': 's'})], {'m': 'brass', 's': shade_}, shade_)
    for stage, face in ((0, 'shutter_shut'), (1, 'shutter_lit')):
        m[f'window_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'r', 'north': 'w'})], {'r': 'stucco', 'w': face}, face)
    m['kiosk_0'] = model([element([1, 0, 3], [15, 14, 13], {'all': 's', 'north': 'r'})], {'s': 'kiosk_side', 'r': 'rack'}, 'rack')
    m['dog_0'] = model([element([3, 0, 5], [13, 5, 12], {'all': 'f'}), element([12, 0, 6], [16, 4, 11], {'all': 'f', 'east': 'd'}),
                        element([0, 0, 8], [3, 1, 9], {'all': 'f'})], {'f': 'fur', 'd': 'dog_face'}, 'fur')
    m['notebook_0'] = model([element([4, 0, 4], [12, 1, 12], {'all': 'e', 'up': 'n'})], {'e': 'paper_edge', 'n': 'notebook'}, 'notebook')
    m['note_0'] = model([element([6, 0, 6], [10, 2, 10], {'all': 'p'})], {'p': 'pebble'}, 'pebble')
    m['note_1'] = model([element([4, 0, 4], [12, 0.5, 12], {'all': 'e', 'up': 'f'}), element([6, 0.5, 6], [10, 2.5, 10], {'all': 'p'})],
                        {'e': 'paper_edge', 'f': 'folded', 'p': 'pebble'}, 'folded')
    m['petals_0'] = model([element([0, 0.1, 0], [16, 0.2, 16], {'up': 'p', 'down': 'p'})], {'p': 'petals'}, 'petals')
    m['blossom_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b'})], {'b': 'blossom'}, 'blossom')
    return m


KINDS = ['gate', 'jar', 'lamp', 'window', 'kiosk', 'dog', 'notebook', 'note', 'petals', 'blossom']
STAGES = {'gate': 4, 'jar': 4, 'lamp': 2, 'window': 2, 'note': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'corner_fitting': 'Street Fitting', 'corner_stucco': 'Painted Render', 'corner_paving': 'Pavement Slabs', 'corner_setts': 'Stone Setts',
         'corner_tiles': 'Patterned Tiles', 'corner_wallpaper': 'Faded Wallpaper'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/corner_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/corner_{name}'}}
        out[f'blockstates/corner_{name}.json'] = {'variants': {'': {'model': NS + f'block/corner_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/corner/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/corner/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/corner_fitting.json'] = {'variants': variants}
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
            assert f'block/corner/{tex.split("corner/")[1]}.png' in images, (key, tex)
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
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the street and the house on the corner: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
