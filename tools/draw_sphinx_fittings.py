"""The Sphinx (0.4.306): her first crossing's fittings and the vestibule they stand in, in vanilla Minecraft's own manner.

Every texture is 16x16 from a small palette with per-pixel noise, the way vanilla's are drawn: quarry tiles four to a block like vanilla
bricks, a marked tile with a brass stud, limewash plaster with the faint mottling of vanilla's calcite, spruce ceiling boards in vanilla
spruce plank tones, and a plaster seam cracked in a door's shape. The fittings: the umbrella stand, a letter flap (shut, and open on a
real hole), a door latch (down and lifted), the stop pin (hidden, exposed, pulled), a grille, and her fingers, sandstone-coloured skin
painted like stone. Writes models, block states and names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import shade, fill, dot, frame, line, element

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/sphinx/'


def noisy(palette, seed, weights=None):
    """Vanilla-style noise: each pixel picks from a few close tones rather than a smooth gradient."""
    im = Image.new('RGBA', (16, 16)); rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(rnd.choices(palette, weights)[0], 0))
    return im


# ------------------------------------------------------------------------------------------------ materials
TILE = ['#9c5a3c', '#a4613f', '#94553a', '#8c5034']
GROUT = '#5e3a2a'


def tile(seed=3061):
    im = noisy(TILE, seed, [4, 3, 3, 1])
    for i in range(16):
        dot(im, i, 7, GROUT); dot(im, i, 15, GROUT); dot(im, 7, i, GROUT); dot(im, 15, i, GROUT)
    return im


def marked():
    im = tile(3062)
    # A brass stud at the crossing of the joints, catching the light on its top-left like vanilla's gold.
    fill(im, 6, 6, 10, 10, '#c9a43c'); dot(im, 6, 6, '#f2dc6e'); dot(im, 7, 6, '#f2dc6e'); dot(im, 6, 7, '#f2dc6e')
    dot(im, 9, 9, '#7a5e1c'); dot(im, 8, 9, '#8e6e22'); dot(im, 9, 8, '#8e6e22')
    return im


PLASTER = ['#e2ddd2', '#dbd6ca', '#d4cfc2', '#e8e4da']


def plaster(seed=3063):
    return noisy(PLASTER, seed, [5, 4, 2, 2])


def ceiling():
    # Vanilla spruce planks: four boards, the same dark seams and end grain.
    im = noisy(['#735432', '#6b4d2e', '#7a5a36', '#664a2c'], 3064, [4, 3, 2, 2])
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#4f3a22')
    for x, y in ((4, 1), (11, 5), (2, 9), (13, 13)):
        dot(im, x, y, '#4f3a22')
    return im


def seam():
    im = plaster(3065)
    for a, b in (((3, 0), (4, 6)), ((4, 6), (2, 11)), ((2, 11), (4, 16)), ((12, 0), (11, 5)), ((11, 5), (13, 10)), ((13, 10), (12, 16)), ((4, 6), (8, 7)), ((8, 7), (11, 5))):
        line(im, a[0], a[1], b[0], b[1], '#8c867a')
    return im


def materials():
    return {'tile': tile(), 'marked': marked(), 'plaster': plaster(), 'ceiling': ceiling(), 'seam': seam()}


# ------------------------------------------------------------------------------------------------ fittings
def iron():
    return noisy(['#5a5a5a', '#4e4e4e', '#646464'], 3070)


def brass():
    im = noisy(['#c9a43c', '#b8932e', '#d6b24a'], 3071)
    return im


def pot():
    # Glazed earthenware with brass bands, like vanilla's flower pot in tone.
    im = noisy(['#7a4632', '#84503a', '#6e3e2c'], 3072)
    for y in (1, 13):
        for x in range(16):
            dot(im, x, y, '#c9a43c'); dot(im, x, y + 1, '#8e6e22')
    return im


def umbrella():
    im = noisy(['#2e3a4a', '#283444', '#34404e'], 3073)
    for y in range(16):
        dot(im, 7, y, '#1c2430')
    return im


def flap():
    im = brass(); frame(im, 0, 0, 16, 16, '#7a5e1c')
    fill(im, 3, 6, 13, 8, '#3a2e14')
    for x in (2, 13):
        dot(im, x, 2, '#f2dc6e')
    return im


def latch(lifted):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 5, 3, 11, 13, '#4e4e4e'); frame(im, 5, 3, 11, 13, '#2e2e2e')
    if lifted:
        line(im, 7, 9, 12, 4, '#7a7a7a'); line(im, 8, 9, 13, 4, '#5a5a5a')
    else:
        fill(im, 7, 7, 15, 9, '#7a7a7a'); dot(im, 14, 8, '#5a5a5a')
    # A chalk tick by the latch, the way someone marked it before.
    line(im, 1, 11, 2, 12, '#e8e8e0'); line(im, 2, 12, 4, 8, '#e8e8e0')
    return im


def grille():
    im = noisy(['#3e3e3e', '#464646'], 3074)
    for y in range(2, 14, 3):
        fill(im, 2, y, 14, y + 2, '#121212')
    frame(im, 0, 0, 16, 16, '#2a2a2a')
    return im


def skin():
    # Her skin: sandstone ochres with a painted band of faded blue across it, the colour left on old stone.
    im = noisy(['#cfa86a', '#c49c5e', '#d8b476', '#b88f52'], 3075, [4, 3, 2, 2])
    for x in range(16):
        dot(im, x, 9, '#5a7a96'); dot(im, x, 10, '#4e6c86')
    for x in range(0, 16, 4):
        dot(im, x, 5, '#a07a44')
    return im


def nail():
    im = noisy(['#e6d4b0', '#dcc8a2'], 3076)
    frame(im, 0, 0, 16, 16, '#a88c5e')
    return im


def textures():
    out = {f'block/sphinx_{k}.png': v for k, v in materials().items()}
    t = {'iron': iron(), 'brass': brass(), 'pot': pot(), 'umbrella': umbrella(), 'flap': flap(), 'latch_0': latch(False), 'latch_1': latch(True),
         'grille': grille(), 'skin': skin(), 'nail': nail(), 'plaster': plaster(3066)}
    out.update({f'block/sphinx/{k}.png': v for k, v in t.items()})
    return out


def model(elements, textures, particle):
    return {'ambientocclusion': True, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    m['stand_0'] = model([element([4, 0, 4], [12, 9, 12], {'all': 'p', 'up': 'i'}), element([6, 9, 5], [8, 15, 7], {'all': 'u'}), element([8, 9, 8], [10, 14, 10], {'all': 'u'}),
                          element([9.5, 9, 5.5], [10.5, 16, 6.5], {'all': 'i'})], {'p': 'pot', 'u': 'umbrella', 'i': 'iron'}, 'pot')
    m['flap_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'w', 'south': 'w'}), element([3, 5, -0.5], [13, 11, 0], {'north': 'f'}, uv={'north': [0, 0, 16, 16]})],
                        {'w': 'plaster', 'f': 'flap'}, 'plaster')
    # Open, the plaster round a real hole, and the flap swung up above it.
    m['flap_1'] = model([element([0, 0, 0], [16, 4, 16], {'all': 'w'}), element([0, 12, 0], [16, 16, 16], {'all': 'w'}), element([0, 4, 0], [3, 12, 16], {'all': 'w'}),
                         element([13, 4, 0], [16, 12, 16], {'all': 'w'}), element([3, 11, -1.5], [13, 12, 0], {'all': 'f'}, uv={f: [0, 0, 16, 2] for f in ('north', 'south', 'east', 'west', 'up', 'down')})],
                        {'w': 'plaster', 'f': 'flap'}, 'plaster')
    for s in range(2):
        m[f'latch_{s}'] = model([element([0, 0, 15.5], [16, 16, 16], {'north': 'l'}, uv={'north': [0, 0, 16, 16]})], {'l': f'latch_{s}'}, 'iron')
    for s, depth in ((0, 15.6), (1, 10), (2, 15.6)):
        els = [element([5, 5, 15.5], [11, 11, 16], {'all': 'i'})]
        if s != 2:
            els.append(element([6.5, 6.5, depth], [9.5, 9.5, 15.5], {'all': 'b'}))
        m[f'pin_{s}'] = model(els, {'i': 'iron', 'b': 'brass'}, 'iron')
    m['grille_0'] = model([element([1, 1, 15], [15, 15, 16], {'all': 'i', 'north': 'g'}, uv={'north': [0, 0, 16, 16]})], {'i': 'iron', 'g': 'grille'}, 'grille')
    els = []
    for i, x in enumerate((0.5, 4.5, 8.5, 12.5)):
        low = 4 if i in (1, 2) else 6
        els.append(element([x, low, 10], [x + 3, 16, 16], {'all': 's'}))
        els.append(element([x + .5, low, 9.4], [x + 2.5, low + 2, 10], {'north': 'n'}))
    m['fingers_0'] = model(els, {'s': 'skin', 'n': 'nail'}, 'skin')
    return m


KINDS = ['stand', 'flap', 'latch', 'pin', 'grille', 'fingers']
STAGES = {'stand': 1, 'flap': 2, 'latch': 2, 'pin': 3, 'grille': 1, 'fingers': 1}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 3
NAMES = {'sphinx_fitting': 'Vestibule Fittings', 'sphinx_tile': 'Quarry Tiles', 'sphinx_marked': 'Marked Tile', 'sphinx_plaster': 'Limewash Plaster',
         'sphinx_ceiling': 'Spruce Ceiling', 'sphinx_seam': 'Cracked Plaster'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/sphinx_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/sphinx_{name}'}}
        out[f'blockstates/sphinx_{name}.json'] = {'variants': {'': {'model': NS + f'block/sphinx_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/sphinx/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/sphinx/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/sphinx_fitting.json'] = {'variants': variants}
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
            assert f'block/sphinx/{tex.split("sphinx/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
        assert im.size == (16, 16), rel
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
    for key, name in {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the Sphinx\'s vestibule: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
