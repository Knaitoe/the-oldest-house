"""Traces and the door wedge (0.4.231): the shut-up rooms under dust, a cloakroom, and three rooms with doors that shut themselves.

Draws, in native Minecraft pixels from fixed seeds: four materials (grey unpolished boards, faded sprigged paper, dark panelling,
stained plaster) and every fitting of the trace_fitting block, its models facing north and turned by its facing: dust clean, walked
through (a pair of shoe prints pointing the way the reader went), a second set of bare prints, both, and scored where a chair was
dragged; the loose chair; a small table; furniture under a dust sheet; a coat hook with the wedge on it; a door closer's arm. And the
wedge itself, as an item. 0.4.234: grit over a stone floor (polished andesite), the same five stages, for the grit gallery in the stone
depths. Writes the models, the block states and the names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/trace/'


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


# ------------------------------------------------------------------ materials
def boards():
    im = new(base='#6f6a62', seed=2311, spread=5)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#4c4842')
    rnd = random.Random(2312)
    for row, y in enumerate((1, 5, 9, 13)):
        dot(im, (row * 5 + 3) % 16, y, '#4c4842'); dot(im, (row * 5 + 11) % 16, y + 1, '#4c4842')
        for _ in range(3):
            dot(im, rnd.randrange(16), y + rnd.randrange(2), '#7d776e')
    return im


def paper():
    im = new(base='#a59e8a', seed=2313, spread=3)
    # A small sprig, gone grey, on a half-drop.
    for cx, cy in ((3, 3), (11, 3), (7, 11), (15, 11)):
        for dx, dy, c in ((0, 0, '#7f8270'), (-1, 1, '#868a76'), (1, 1, '#868a76'), (0, 2, '#7c7464')):
            dot(im, (cx + dx) % 16, (cy + dy) % 16, c)
    for x in range(16):
        dot(im, x, 15, '#968f7c')
    return im


def panel():
    im = new(base='#4a3a2c', seed=2315, spread=3)
    for y in range(16):
        dot(im, 0, y, '#3a2d22'); dot(im, 15, y, '#5a4838')
    for x in range(16):
        dot(im, x, 0, '#5a4838'); dot(im, x, 15, '#3a2d22')
    for y in range(3, 13):
        dot(im, 3, y, '#3a2d22'); dot(im, 12, y, '#5a4838')
    for x in range(3, 13):
        dot(im, x, 3, '#3a2d22'); dot(im, x, 12, '#5a4838')
    return im


def plaster():
    im = new(base='#c2bcae', seed=2317, spread=3)
    rnd = random.Random(2318)
    for _ in range(9):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#ab9f84'); dot(im, (x + 1) % 16, y, '#b3a88e')
    return im


def materials():
    return {'boards': boards(), 'paper': paper(), 'panel': panel(), 'plaster': plaster()}


# ------------------------------------------------------------------ fittings
DUST = '#a49e93'
BARE = '#6f6a62'
GRIT = '#a3a199'
STONE = '#7e807f'


def dust(seed=2321):
    im = new(base=DUST, seed=seed, spread=4)
    rnd = random.Random(seed + 1)
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#b4aea2')
    return im


def grit(seed=2361):
    """Grit shed from a stone vault: grey, coarser than dust, with darker grains and a few pale chips."""
    im = new(base=GRIT, seed=seed, spread=5)
    rnd = random.Random(seed + 1)
    for _ in range(12):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#8e8c86')
    for _ in range(8):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#b7b5ad')
    return im


def shoe(im, x, y, colour=BARE):
    """A shoe print pointing north (toward the top): sole four rows, a gap, a heel."""
    for dy, (x0, x1) in enumerate(((1, 3), (0, 3), (0, 3), (0, 3))):
        fill(im, x + x0, y + dy, x + x1, y + dy + 1, colour)
    fill(im, x, y + 5, x + 3, y + 7, colour)


def bare(im, x, y, colour='#7a756d'):
    """A bare footprint pointing north: a narrow sole and a heel, four toes above."""
    for dx in range(4):
        dot(im, x + dx, y, colour)
    fill(im, x, y + 2, x + 3, y + 5, colour)
    fill(im, x + 1, y + 5, x + 3, y + 7, colour)


def prints(floor=None, under=BARE):
    im = floor() if floor else dust(); shoe(im, 3, 8, under); shoe(im, 9, 1, under); return im


def other(floor=None, toes='#7a756d'):
    im = floor(2371) if floor else dust(2331); bare(im, 4, 9, toes); bare(im, 9, 2, toes); return im


def both(floor=None, under=BARE, toes='#7a756d'):
    im = floor(2373) if floor else dust(2333); shoe(im, 1, 8, under); shoe(im, 5, 1, under); bare(im, 10, 9, toes); bare(im, 12, 2, toes); return im


def dragged(floor=None, scored=('#8a847a', '#7c766d')):
    im = floor(2375) if floor else dust(2335)
    for x in (3, 4, 11, 12):
        for y in range(16):
            dot(im, x, y, scored[0] if x in (3, 11) else scored[1])
    return im


def wood(base='#7a5a3a', seed=2341):
    im = new(base=base, seed=seed, spread=4)
    for y in (4, 9, 13):
        for x in range(16):
            if (x + y) % 5:
                dot(im, x, y, shade(base, -14)[:3])
    return im


def sheet():
    # 0.4.240: the cloth hangs in uneven folds to a greyed, dusty hem; no line across the top, so a tall piece's two blocks meet
    # without a seam.
    im = new(base='#d8d2c4', seed=2343, spread=3)
    for x, lean in ((2, 0), (6, 1), (11, 0), (14, 1)):
        for y in range(16):
            fx = x + (lean if y > 9 else 0)
            dot(im, fx, y, '#c4bdae'); dot(im, fx + 1, y, '#e2ddd1')
    for x in range(16):
        dot(im, x, 15, '#bab3a4'); dot(im, x, 14, '#c9c2b4' if x % 3 else '#bdb6a7')
    return im


def sheet_top():
    # The top of a covered piece: the cloth gathered over it in creases running corner to corner.
    im = new(base='#ddd8cb', seed=2347, spread=3)
    for i in range(16):
        dot(im, i, i, '#c8c1b2'); dot(im, (i + 5) % 16, i, '#e6e1d6')
        dot(im, 15 - i, (i + 3) % 16, '#cbc4b5')
    return im


def brass():
    im = new(base='#a88a3c', seed=2345, spread=6)
    return im


def metal():
    return new(base='#55585a', seed=2347, spread=4)


def wedge_item():
    """The wedge in profile: a long low triangle of worn wood, the thin edge to the left, its worn underside paler."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x in range(1, 15):
        top = 11 - (x * 6) // 14
        for y in range(top, 12):
            c = '#8a6440' if y > top else '#a07650'
            dot(im, x, y, shade(c, ((x * 7 + y * 3) % 5) - 2)[:3])
        dot(im, x, 12, '#5c4128')
    for y in range(5, 12):
        dot(im, 15, y, '#6e4f31')
    for x in range(2, 14, 4):
        dot(im, x, 11, '#b08a60')
    return im


def wedge_side():
    """The wedge hanging on its hook, seen side on: the same profile, turned point down."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(1, 15):
        w = 1 + (y * 5) // 14
        for x in range(8 - w, 8 + w):
            dot(im, x, 15 - y, shade('#8a6440', ((x + y) % 3) - 1)[:3])
    dot(im, 7, 0, '#3a2d22'); dot(im, 8, 0, '#3a2d22')
    return im


def textures():
    out = {f'block/trace/{k}.png': v for k, v in materials().items()}
    t = {'dust_0': dust(), 'dust_1': prints(), 'dust_2': other(), 'dust_3': both(), 'dust_4': dragged(),
         'grit_0': grit(), 'grit_1': prints(grit, STONE), 'grit_2': other(grit, '#888a89'), 'grit_3': both(grit, STONE, '#888a89'),
         'grit_4': dragged(grit, ('#8b8d8c', '#7e807f')),
         'chair': wood(), 'table': wood('#6a4a30', 2349), 'table_top': wood('#74543a', 2351), 'sheet': sheet(), 'sheet_top': sheet_top(),
         'brass': brass(), 'metal': metal(), 'wedge_side': wedge_side()}
    out.update({f'block/trace/{k}.png': v for k, v in t.items()})
    out['item/door_wedge.png'] = wedge_item()
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


def fitting_models():
    m = {}
    # Dust lies a quarter of a pixel thick over the boards; its prints point north and turn with the block.
    for stage in range(5):
        m[f'dust_{stage}'] = model([element([0, 0, 0], [16, 0.25, 16], {'all': 'd'})], {'d': f'dust_{stage}'}, f'dust_{stage}')
        m[f'grit_{stage}'] = model([element([0, 0, 0], [16, 0.25, 16], {'all': 'd'})], {'d': f'grit_{stage}'}, f'grit_{stage}')
    # The loose chair: four legs, a seat, a back of two posts and a rail, the sitter facing north.
    legs = [element([x, 0, z], [x + 2, 8, z + 2], {'all': 'w'}) for x in (2, 12) for z in (2, 12)]
    # The back's posts rise above the block: their sides are mapped onto the texture's top eleven rows, not off it (0.4.240).
    def post(x):
        return element([x, 10, 12], [x + 2, 21, 14], {'all': 'w'}, {'north': [14 - x, 0, 16 - x, 11], 'south': [x, 0, x + 2, 11], 'east': [2, 0, 4, 11], 'west': [12, 0, 14, 11]})
    m['chair_0'] = model(legs + [element([2, 8, 2], [14, 10, 14], {'all': 'w'}),
                                post(2), post(12),
                                element([4, 15, 12.5], [12, 20, 13.5], {'all': 'w'}, {'north': [4, 1, 12, 6], 'south': [4, 1, 12, 6], 'east': [12.5, 1, 13.5, 6], 'west': [12.5, 1, 13.5, 6]})],
                         {'w': 'chair'}, 'chair')
    m['table_0'] = model([element([x, 0, z], [x + 2, 13, z + 2], {'all': 't'}) for x in (1.5, 12.5) for z in (1.5, 12.5)]
                         + [element([1, 13, 1], [15, 15, 15], {'all': 't', 'up': 'u'})], {'t': 'table', 'u': 'table_top'}, 'table')
    # Furniture under a dust sheet (0.4.240): one piece (stage 0), or a tall piece's lower block (1) and upper block (2), which meet
    # as one cloth; the hem flares a quarter pixel at the floor. Faces that reach past the block are mapped onto the texture.
    hem = element([-0.25, 0, -0.25], [16.25, 1, 16.25], {'north': 's', 'south': 's', 'east': 's', 'west': 's'},
                  {f: [0, 15, 16, 16] for f in ('north', 'south', 'east', 'west')})
    sides = {'north': 's', 'south': 's', 'east': 's', 'west': 's'}
    m['sheet_0'] = model([element([0, 0, 0], [16, 13, 16], {**sides, 'up': 't'}), element([1, 13, 1], [15, 14, 15], {**sides, 'up': 't'}), hem],
                         {'s': 'sheet', 't': 'sheet_top'}, 'sheet')
    m['sheet_1'] = model([element([0, 0, 0], [16, 16, 16], sides), hem], {'s': 'sheet'}, 'sheet')
    m['sheet_2'] = model([element([0, 0, 0], [16, 13, 16], {**sides, 'up': 't'}, {f: [0, 0, 16, 13] for f in sides}),
                          element([1, 13, 1], [15, 14, 15], {**sides, 'up': 't'})], {'s': 'sheet', 't': 'sheet_top'}, 'sheet')
    # The hook: a brass plate on the wall behind, its arm, and the wedge hanging from it point down.
    m['hook_0'] = model([element([6, 12, 15], [10, 15, 16], {'all': 'b'}), element([7.5, 12.5, 13], [8.5, 13.5, 15], {'all': 'b'}),
                         element([4, 4, 13.5], [12, 14, 14.5], {'north': 'e', 'south': 'e'})], {'b': 'brass', 'e': 'wedge_side'}, 'brass')
    # The closer: a housing on the wall over the door and its folded arm.
    m['closer_0'] = model([element([2, 11, 13], [9, 14, 16], {'all': 'm'}), element([9, 12, 13.5], [14, 13, 14.5], {'all': 'm'})], {'m': 'metal'}, 'metal')
    return m


KINDS = ['dust', 'chair', 'table', 'sheet', 'hook', 'closer', 'grit']
STAGES = {'dust': 5, 'grit': 5, 'sheet': 3}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 5
NAMES = {'block.the_oldest_house.trace_fitting': 'Furniture', 'block.the_oldest_house.trace_boards': 'Grey Boards',
         'block.the_oldest_house.trace_paper': 'Faded Paper', 'block.the_oldest_house.trace_panel': 'Dark Panelling',
         'block.the_oldest_house.trace_plaster': 'Stained Plaster', 'item.the_oldest_house.door_wedge': 'Door Wedge'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/trace_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/trace_{name}.json'] = {'variants': {'': {'model': NS + f'block/trace_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/trace/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/trace/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/trace_fitting.json'] = {'variants': variants}
    out['models/item/door_wedge.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/door_wedge'}}
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
            assert f'block/trace/{tex.split("trace/")[1]}.png' in images, (key, tex)
    # Prints are drawn on the dust itself, so dust read by its own pixels shows them: clean dust has no bare board showing.
    def pixels(im):
        px = im.load(); return [px[x, y][:3] for y in range(16) for x in range(16)]
    assert rgb(BARE) not in pixels(images['block/trace/dust_0.png'])
    assert rgb(BARE) in pixels(images['block/trace/dust_1.png'])
    # And grit the same over its stone.
    assert rgb(STONE) not in pixels(images['block/trace/grit_0.png'])
    assert rgb(STONE) in pixels(images['block/trace/grit_1.png'])
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
    print(('Verified' if check else 'Drew') + f' the trace rooms: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
