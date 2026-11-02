"""The Folding Rooms (0.4.250): a former dining room, its pantry and morning room, and partitions of hinged panels on floor and
ceiling tracks.

Draws, in native Minecraft pixels from fixed seeds: eight materials (herringbone parquet; the floor track, plain and worn bright
where a partition has most often stood; parquet less worn where furniture stood for years; dark panelling to the dado; paper above
it; the plaster ceiling and its track) and every fitting of the fold_fitting block, its models facing north and turned by its
facing: a partition panel, lower, upper and upper with its brass pulls; the hanger rod, plain and with its carriage at the ceiling;
the partition folded back on its hinges, lower and upper; the sideboard; a dining table; a dining chair; the serving hatch, shutter
down and up. Writes the models and block states, and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/fold/'


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


OAK = '#8e6234'
OAK_DARK = '#5e3c1e'
BRASS = '#b48c3c'
BRASS_DARK = '#6e5222'
WALNUT = '#5a3a24'


def parquet(seed=2500, worn=0):
    """Herringbone: blocks of three short boards, laid alternately across and along, each board a little different."""
    rnd = random.Random(seed)
    im = new(base=OAK, seed=seed, spread=3)
    for by in range(0, 16, 4):
        for bx in range(0, 16, 4):
            across = ((bx + by) // 4) % 2 == 0
            tone = rnd.randint(-10, 10) + worn
            for i in range(4):
                for j in range(4):
                    x, y = bx + i, by + j
                    k = tone + rnd.randint(-3, 3)
                    if (across and j == 3) or (not across and i == 3):
                        dot(im, x, y, shade(OAK_DARK, k // 2))
                    else:
                        dot(im, x, y, shade(OAK, k))
    return im


def track(worn=False):
    im = parquet(2510, 6 if worn else 0)
    fill(im, 6, 0, 7, 16, BRASS_DARK); fill(im, 9, 0, 10, 16, BRASS_DARK)
    fill(im, 7, 0, 9, 16, '#d6b05e' if worn else BRASS)
    if worn:
        for y in range(1, 16, 3):
            dot(im, 7, y, '#f0d488'); dot(im, 8, (y + 1) % 16, '#e8c878')
    else:
        for y in range(2, 16, 5):
            dot(im, 8, y, BRASS_DARK)
    return im


def marks():
    """Parquet with four squares less worn than the boards round them: where the feet of something heavy stood for years."""
    im = parquet(2520)
    for (x, y) in ((1, 1), (12, 1), (1, 12), (12, 12)):
        fill(im, x, y, x + 3, y + 3, '#a8763e')
        frame(im, x, y, x + 3, y + 3, '#b4824a')
    return im


def panelling():
    im = new(base='#4e3420', seed=2530, spread=3)
    frame(im, 0, 0, 16, 16, '#3a2414')
    fill(im, 3, 3, 13, 13, '#5c3e26'); frame(im, 3, 3, 13, 13, '#2e1c0e')
    for x in range(4, 12):
        dot(im, x, 4, '#6e4c30')
    return im


def paper():
    im = new(base='#6e7a62', seed=2540, spread=2)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#62705a')
    for y in range(2, 16, 6):
        for x in range(1, 16, 4):
            dot(im, x + 1, y, '#808c72')
    return im


def ceiling():
    return new(base='#dcd8cc', seed=2550, spread=2)


def ceiling_track():
    im = ceiling()
    fill(im, 6, 0, 10, 16, '#7a6a48'); fill(im, 7, 0, 9, 16, '#2a2418')
    return im


def materials():
    return {'parquet': parquet(), 'track': track(), 'track_worn': track(True), 'marks': marks(), 'panelling': panelling(),
            'paper': paper(), 'ceiling': ceiling(), 'ceiling_track': ceiling_track()}


def panel_face(upper=False):
    """One leaf of the partition: a fielded panel in a frame, and the hinge seam down each side where it meets the next."""
    im = new(base=OAK, seed=2600 + (1 if upper else 0), spread=3)
    fill(im, 0, 0, 1, 16, OAK_DARK); fill(im, 15, 0, 16, 16, OAK_DARK)
    for y in (3, 8, 13):
        dot(im, 0, y, BRASS); dot(im, 15, y, BRASS)
    fill(im, 3, 2 if upper else 1, 13, 14 if upper else 15, '#9a6c3c')
    frame(im, 3, 2 if upper else 1, 13, 14 if upper else 15, '#6a4422')
    if upper:
        fill(im, 1, 0, 15, 1, '#7a5228')
    return im


def edge():
    im = new(base='#7a5228', seed=2610, spread=2)
    for y in range(0, 16, 3):
        for x in range(16):
            dot(im, x, y, '#6a4422')
    return im


def folded():
    """The leaves folded flat against each other: their edges in a stack, light and dark by turns."""
    im = new(base=OAK, seed=2620, spread=2)
    for x in range(16):
        k = 8 if (x // 2) % 2 == 0 else -14
        for y in range(16):
            dot(im, x, y, shade(OAK, k))
    fill(im, 0, 0, 16, 1, OAK_DARK); fill(im, 0, 15, 16, 16, OAK_DARK)
    return im


def sideboard_front():
    im = new(base=WALNUT, seed=2630, spread=3)
    frame(im, 0, 0, 16, 16, '#3a2414')
    fill(im, 1, 1, 15, 4, '#664430'); frame(im, 1, 1, 15, 4, '#3a2414')
    for x0 in (1, 8):
        frame(im, x0, 5, x0 + 7, 15, '#3a2414'); fill(im, x0 + 2, 7, x0 + 5, 13, '#624030')
    dot(im, 7, 2, BRASS); dot(im, 8, 2, BRASS); dot(im, 6, 10, BRASS); dot(im, 9, 10, BRASS)
    return im


def sideboard_top():
    im = new(base=WALNUT, seed=2632, spread=2)
    fill(im, 0, 5, 16, 11, '#e6e0cc'); frame(im, 0, 5, 16, 11, '#d0c8b0')
    return im


def cloth():
    im = new(base='#ece8da', seed=2640, spread=2)
    for x in range(0, 16, 8):
        for y in range(16):
            dot(im, x, y, '#dcd6c4')
    return im


def wood():
    return new(base='#4a2e1a', seed=2642, spread=3)


def seat():
    im = new(base='#6e2a2a', seed=2644, spread=3)
    frame(im, 0, 0, 16, 16, '#4a1a1a')
    return im


def hatch(opened):
    im = panelling()
    fill(im, 2, 3, 14, 13, '#3a2414')
    if opened:
        fill(im, 3, 4, 13, 12, '#141210')
        fill(im, 3, 4, 13, 5, '#5c3e26')
    else:
        for y in range(4, 12):
            fill(im, 3, y, 13, y + 1, '#6e4c30' if y % 2 == 0 else '#5c3e26')
        dot(im, 8, 10, BRASS)
    return im


def textures():
    out = {f'block/fold/{k}.png': v for k, v in materials().items()}
    t = {'panel': panel_face(), 'panel_upper': panel_face(True), 'edge': edge(), 'brass': new(base=BRASS, seed=2650, spread=6),
         'iron': new(base='#3a3a38', seed=2652, spread=3), 'folded': folded(), 'sideboard_front': sideboard_front(),
         'sideboard_side': new(base=WALNUT, seed=2634, spread=3), 'sideboard_top': sideboard_top(), 'cloth': cloth(), 'wood': wood(),
         'seat': seat(), 'hatch_0': hatch(False), 'hatch_1': hatch(True)}
    out.update({f'block/fold/{k}.png': v for k, v in t.items()})
    return out


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


def panel_model(face, pulls):
    els = [element([0, 0, 5], [16, 16, 11], {'all': 'e', 'north': 'p', 'south': 'p'})]
    if pulls:
        els.append(element([7, 4, 4], [9, 10, 5], {'all': 'b'}))
        els.append(element([7, 4, 11], [9, 10, 12], {'all': 'b'}))
    return model(els, {'e': 'edge', 'p': face, 'b': 'brass'}, face)


def fitting_models():
    m = {}
    m['panel_0'] = panel_model('panel', False)
    m['panel_1'] = panel_model('panel_upper', False)
    m['panel_2'] = panel_model('panel_upper', True)
    m['hanger_0'] = model([element([7.5, 0, 7.5], [8.5, 16, 8.5], {'all': 'b'})], {'b': 'brass'}, 'brass')
    m['hanger_1'] = model([element([7.5, 0, 7.5], [8.5, 13, 8.5], {'all': 'b'}), element([5, 13, 6], [11, 16, 10], {'all': 'i'})], {'b': 'brass', 'i': 'iron'}, 'iron')
    m['folded_0'] = model([element([1, 0, 2], [15, 16, 14], {'all': 'f', 'up': 'e', 'down': 'e'})], {'f': 'folded', 'e': 'edge'}, 'folded')
    m['folded_1'] = model([element([1, 0, 2], [15, 16, 14], {'all': 'f', 'up': 'e', 'down': 'e'})], {'f': 'folded', 'e': 'edge'}, 'folded')
    m['sideboard_0'] = model([element([1, 0, 3], [15, 12, 15], {'all': 's', 'north': 'f'}), element([0, 12, 2], [16, 13, 16], {'all': 's', 'up': 't'})],
                             {'s': 'sideboard_side', 'f': 'sideboard_front', 't': 'sideboard_top'}, 'sideboard_front')
    legs = [element([x, 0, z], [x + 2, 12, z + 2], {'all': 'w'}) for x in (1, 13) for z in (1, 13)]
    m['table_0'] = model([element([0, 12, 0], [16, 14, 16], {'all': 'c'})] + legs, {'c': 'cloth', 'w': 'wood'}, 'cloth')
    chair = [element([x, 0, z], [x + 1.5, 7, z + 1.5], {'all': 'w'}) for x in (3, 11.5) for z in (3, 11.5)]
    chair += [element([3, 7, 3], [13, 9, 13], {'all': 'w', 'up': 's'}), element([3, 9, 12], [4.5, 19, 13.5], {'all': 'w'}),
              element([11.5, 9, 12], [13, 19, 13.5], {'all': 'w'}), element([4.5, 14, 12.25], [11.5, 18, 13.25], {'all': 'w'})]
    m['chair_0'] = model(chair, {'w': 'wood', 's': 'seat'}, 'wood')
    m['hatch_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'p', 'north': 'h'})], {'p': 'panelling', 'h': 'hatch_0'}, 'panelling', 'solid')
    m['hatch_1'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'p', 'north': 'h'})], {'p': 'panelling', 'h': 'hatch_1'}, 'panelling', 'solid')
    return m


KINDS = ['panel', 'hanger', 'folded', 'sideboard', 'table', 'chair', 'hatch']
STAGES = {'panel': 3, 'hanger': 2, 'folded': 2, 'hatch': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 3
NAMES = {'fold_fitting': 'Dining Room Fitting', 'fold_parquet': 'Herringbone Parquet', 'fold_track': 'Partition Track',
         'fold_track_worn': 'Worn Partition Track', 'fold_marks': 'Worn Parquet', 'fold_panelling': 'Dark Panelling',
         'fold_paper': 'Dining Room Paper', 'fold_ceiling': 'Plaster Ceiling', 'fold_ceiling_track': 'Ceiling Track'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/fold_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/fold_{name}.json'] = {'variants': {'': {'model': NS + f'block/fold_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/fold/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/fold/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/fold_fitting.json'] = {'variants': variants}
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
            assert f'block/fold/{tex.split("fold/")[1]}.png' in images, (key, tex)
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
    print(('Verified' if check else 'Drew') + f' the folding rooms: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
