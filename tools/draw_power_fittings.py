"""The Fuse Board (0.4.254): a basement's electrics, and the basement.

Draws, in native Minecraft pixels from fixed seeds: four materials (whitewashed brick, painted concrete with its oil stains, a concrete
soffit, steel plate) and every part of the power_fitting block, its models facing north (front to the room, back to the wall) and turned
by its facing: a fuse holder for each circuit with its label plate (a work light, the shutter, the door latch, the hoist), empty and
with its fuse; the spare clip with the board's pilot lamp, empty and holding a fuse; a cloth-covered cable for each circuit in its
own colour (a horizontal run, a vertical run, a junction box); the caged work light, cold and burning; the roller shutter's slats and
its bottom slat with the line of light under it; the shutter's housing, its motor lamp dark and lit; the guide rail; the manual catch,
engaged and pinned back; the latch box, dark and lit; the wedge, lying by the wall and kicked under the door; the hoist's motor,
dark and lit; its jib and the jib's end with its pulley; the crate; and the floor hatch, shut and open. Writes the models, the block
states and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/power/'


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
def brick():
    # Whitewashed brick, the wash thin over the courses and gone grey in the mortar.
    base, mortar = '#d8d4c8', '#a8a49a'
    im = new(base=base, seed=2540, spread=4); rnd = random.Random(2541)
    for row in range(4):
        y = row * 4 + 3
        for x in range(16):
            dot(im, x, y, mortar)
        off = 0 if row % 2 == 0 else 4
        for x in range(off, 16, 8):
            for dy in range(3):
                dot(im, x, row * 4 + dy, mortar)
    for _ in range(9):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if y % 4 != 3:
            dot(im, x, y, '#b8907a')
    return im


def floor():
    base = '#6a6e6a'
    im = new(base=base, seed=2542, spread=5); rnd = random.Random(2543)
    for _ in range(3):
        cx, cy = rnd.randrange(3, 13), rnd.randrange(3, 13)
        for dx in range(-2, 3):
            for dy in range(-1, 2):
                if abs(dx) + abs(dy) < 3:
                    dot(im, cx + dx, cy + dy, shade(base, -18)[:3])
    for x in range(16):
        dot(im, x, 15, shade(base, -10)[:3])
    return im


def materials():
    plate = new(base='#7a7e82', seed=2544, spread=3)
    frame(plate, 0, 0, 16, 16, '#5a5e62')
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        dot(plate, x, y, '#a8acb0')
    return {'brick': brick(), 'floor': floor(), 'ceiling': new(base='#8a8a86', seed=2545, spread=3), 'plate': plate}


# ------------------------------------------------------------------------------------------------ fittings
CIRCUITS = {'light': '#e8e0c8', 'shutter': '#2a2a2c', 'latch': '#9a2e28', 'hoist': '#4e6a62'}


def slate():
    im = new(base='#3c4044', seed=2550, spread=3)
    frame(im, 0, 0, 16, 16, '#2a2c30')
    return im


def label(circuit):
    # An enamel plate with a mark for what the circuit feeds, inked by hand.
    im = new(base='#e4dcc4', seed=2551, spread=2)
    ink = '#2a2420'
    if circuit == 'light':
        fill(im, 6, 3, 10, 9, ink); fill(im, 7, 9, 9, 12, ink); dot(im, 7, 5, '#e4dcc4'); dot(im, 8, 5, '#e4dcc4')
    elif circuit == 'shutter':
        for y in (3, 6, 9, 12):
            fill(im, 3, y, 13, y + 2, ink)
    elif circuit == 'latch':
        frame(im, 4, 3, 12, 14, ink); fill(im, 9, 8, 11, 10, ink)
    else:
        fill(im, 7, 2, 9, 9, ink); fill(im, 5, 9, 11, 11, ink); dot(im, 5, 12, ink); dot(im, 10, 12, ink); fill(im, 6, 12, 10, 14, ink)
    return im


def porcelain():
    im = new(base='#ece8de', seed=2552, spread=2)
    frame(im, 0, 0, 16, 16, '#c8c2b4')
    return im


def fuse():
    im = new(base='#d8c8a0', seed=2553, spread=2)
    fill(im, 0, 0, 3, 16, '#b87838'); fill(im, 13, 0, 16, 16, '#b87838')
    return im


def cable(colour, seed):
    im = new(base=colour, seed=seed, spread=3)
    for i in range(0, 16, 3):
        for j in range(16):
            if (i + j) % 6 == 0:
                dot(im, i, j, shade(colour, -14)[:3])
    return im


def junction():
    im = new(base='#5a5e5a', seed=2554, spread=3)
    frame(im, 0, 0, 16, 16, '#3a3e3a')
    for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):
        dot(im, x, y, '#9a9e9a')
    return im


def cage(lit):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    glass = '#fff4c8' if lit else '#9a9688'
    fill(im, 2, 2, 14, 14, glass)
    if lit:
        fill(im, 5, 5, 11, 11, '#ffffff')
    for i in range(0, 16, 4):
        for j in range(16):
            dot(im, i, j, '#2a2a2c'); dot(im, j, i, '#2a2a2c')
    frame(im, 0, 0, 16, 16, '#2a2a2c')
    return im


def slats(bottom):
    base = '#8a9094'
    im = new(base=base, seed=2555 + bottom, spread=2)
    for y in range(0, 16, 3):
        for x in range(16):
            dot(im, x, y, '#5a6064'); dot(im, x, y + 1, '#a4aaae')
    if bottom:
        fill(im, 0, 12, 16, 16, '#4a5054'); fill(im, 6, 13, 10, 15, '#2a2e30')
    return im


def light_line():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 0, 16, 16, '#fff0b0')
    return im


def housing(lit):
    im = new(base='#6a6e72', seed=2557, spread=3)
    frame(im, 0, 0, 16, 16, '#4a4e52')
    fill(im, 7, 7, 9, 9, '#ff4a2a' if lit else '#4a1a14')
    return im


def guide(catch=None):
    im = new(base='#d8d4c8', seed=2558, spread=3)
    fill(im, 5, 0, 11, 16, '#5a5e62'); fill(im, 7, 0, 9, 16, '#2a2e30')
    if catch == 'engaged':
        fill(im, 3, 6, 13, 9, '#9a3a28'); fill(im, 3, 6, 5, 10, '#6a2a1c')
    elif catch == 'pinned':
        fill(im, 3, 2, 6, 10, '#9a3a28'); fill(im, 2, 1, 7, 3, '#c8c8c8')
    return im


def box_face(lit, colour='#5a5e62'):
    im = new(base=colour, seed=2559, spread=3)
    frame(im, 0, 0, 16, 16, shade(colour, -20)[:3])
    fill(im, 6, 2, 10, 6, '#ffb82a' if lit else '#4a3a14')
    return im


def wood(base='#8a6a42', seed=2560):
    im = new(base=base, seed=seed, spread=5)
    for y in (4, 9, 13):
        for x in range(16):
            dot(im, x, y, shade(base, -24)[:3])
    return im


def crate():
    im = wood('#9a7a4a', 2561)
    frame(im, 0, 0, 16, 16, '#5a5e62')
    for i in range(16):
        dot(im, i, i, '#6a4e2a')
    return im


def hatch():
    im = wood('#7a6040', 2562)
    frame(im, 0, 0, 16, 16, '#4a4e52')
    fill(im, 6, 6, 10, 10, '#5a5e62'); fill(im, 7, 7, 9, 9, '#7a6040')
    return im


def textures():
    out = {f'block/power_{k}.png': v for k, v in materials().items()}
    t = {
        'slate': slate(), 'porcelain': porcelain(), 'fuse': fuse(), 'junction': junction(),
        'lamp_off': cage(False), 'lamp_on': cage(True), 'bracket': new(base='#3a3e42', seed=2563, spread=2),
        'slats': slats(0), 'slats_bottom': slats(1), 'light_line': light_line(),
        'housing_off': housing(False), 'housing_on': housing(True), 'steel': new(base='#6a6e72', seed=2564, spread=3),
        'guide': guide(), 'catch_engaged': guide('engaged'), 'catch_pinned': guide('pinned'),
        'latch_off': box_face(False), 'latch_on': box_face(True), 'motor_off': box_face(False, '#4e6a62'), 'motor_on': box_face(True, '#4e6a62'),
        'wood': wood(), 'crate': crate(), 'hatch': hatch(), 'pilot': new(base='#c83a2a', seed=2565, spread=6),
    }
    for i, (c, colour) in enumerate(CIRCUITS.items()):
        t[f'label_{c}'] = label(c)
        t[f'cable_{c}'] = cable(colour, 2570 + i)
    out.update({f'block/power/{k}.png': v for k, v in t.items()})
    return out


def element(lo, hi, textures, uv=None, rotation=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
        if rotation and f in rotation:
            faces[f]['rotation'] = rotation[f]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    for c in CIRCUITS:
        for stage in range(2):
            els = [element([4, 2, 15], [12, 14, 16], {'all': 's'}), element([5, 4, 13], [11, 11, 15], {'all': 'p'}),
                   element([5, 12, 14.5], [11, 14, 15], {'north': 'l'}, uv={'north': [0, 0, 16, 16]})]
            if stage:
                els.append(element([5.5, 6.5, 12], [10.5, 8.5, 13], {'all': 'f'}, uv={'north': [0, 0, 16, 16]}))
            m[f'holder_{c}_{stage}'] = model(els, {'s': 'slate', 'p': 'porcelain', 'l': f'label_{c}', 'f': 'fuse'}, 'slate')
        run = model([element([0, 7, 15], [16, 9, 16], {'all': 'c'}), element([2, 6.5, 14.5], [3, 9.5, 16], {'all': 'b'}), element([12, 6.5, 14.5], [13, 9.5, 16], {'all': 'b'})],
                    {'c': f'cable_{c}', 'b': 'bracket'}, f'cable_{c}')
        rise = model([element([7, 0, 15], [9, 16, 16], {'all': 'c'}), element([6.5, 2, 14.5], [9.5, 3, 16], {'all': 'b'}), element([6.5, 12, 14.5], [9.5, 13, 16], {'all': 'b'})],
                     {'c': f'cable_{c}', 'b': 'bracket'}, f'cable_{c}')
        box = model([element([4, 4, 13], [12, 12, 16], {'all': 'j'}), element([7, 0, 15], [9, 4, 16], {'all': 'c'}), element([7, 12, 15], [9, 16, 16], {'all': 'c'}),
                     element([0, 7, 15], [4, 9, 16], {'all': 'c'}), element([12, 7, 15], [16, 9, 16], {'all': 'c'})],
                    {'j': 'junction', 'c': f'cable_{c}'}, 'junction')
        m[f'cable_{c}_0'], m[f'cable_{c}_1'], m[f'cable_{c}_2'] = run, rise, box
    for stage in range(2):
        els = [element([4, 3, 15], [12, 13, 16], {'all': 's'}), element([5, 5, 14], [11, 6, 15], {'all': 'b'}), element([5, 9, 14], [11, 10, 15], {'all': 'b'}),
               element([7, 11, 13.5], [9, 13, 15], {'all': 'g'})]
        if stage:
            els.append(element([5.5, 6, 13.5], [10.5, 9, 14.5], {'all': 'f'}, uv={'north': [0, 0, 16, 16]}))
        m[f'clip_{stage}'] = model(els, {'s': 'slate', 'b': 'bracket', 'g': 'pilot', 'f': 'fuse'}, 'slate')
    for stage, lamp in enumerate(('lamp_off', 'lamp_on')):
        m[f'work_lamp_{stage}'] = model([element([7, 6, 15], [9, 10, 16], {'all': 'b'}), element([7.5, 7.5, 10], [8.5, 8.5, 15], {'all': 'b'}),
                                         element([5, 5, 6], [11, 11, 10], {'all': 'l'}, uv={f: [0, 0, 16, 16] for f in ('north', 'south', 'east', 'west', 'up', 'down')})],
                                        {'b': 'bracket', 'l': lamp}, lamp)
    m['shutter_0'] = model([element([0, 0, 6], [16, 16, 10], {'north': 's', 'south': 's', 'up': 'e', 'down': 'e', 'east': 'e', 'west': 'e'})], {'s': 'slats', 'e': 'steel'}, 'slats')
    m['shutter_1'] = model([element([0, 2, 6], [16, 16, 10], {'north': 's', 'south': 's', 'up': 'e', 'down': 'e', 'east': 'e', 'west': 'e'}, uv={'north': [0, 0, 16, 14], 'south': [0, 0, 16, 14]}),
                            element([1, 0, 9], [15, 1, 9.5], {'north': 'l', 'up': 'l'})],
                           {'s': 'slats_bottom', 'e': 'steel', 'l': 'light_line'}, 'slats_bottom')
    for stage, face in enumerate(('housing_off', 'housing_on')):
        m[f'shutter_box_{stage}'] = model([element([0, 0, 2], [16, 16, 14], {'all': 'e', 'north': 'h', 'south': 'e'})], {'e': 'steel', 'h': face}, 'steel')
    def wall_cube(front):
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'f', 'south': 'f', 'east': 'b', 'west': 'b', 'up': 'p', 'down': 'p'})],
                     {'f': front, 'b': 'guide', 'p': 'steel'}, 'guide')
    m['guide_0'] = wall_cube('guide')
    m['catch_0'], m['catch_1'] = wall_cube('catch_engaged'), wall_cube('catch_pinned')
    for stage, face in enumerate(('latch_off', 'latch_on')):
        m[f'latch_{stage}'] = model([element([4, 3, 12], [12, 13, 16], {'all': 'e', 'north': 'f'}, uv={'north': [0, 0, 16, 16]})], {'e': 'steel', 'f': face}, 'steel')
    m['wedge_0'] = model([element([3, 0, 9], [13, 3, 15], {'all': 'w'})], {'w': 'wood'}, 'wood')
    m['wedge_1'] = model([element([10, 0, 6], [16, 2, 16], {'all': 'w'}), element([10, 2, 11], [16, 3, 16], {'all': 'w'})], {'w': 'wood'}, 'wood')
    for stage, face in enumerate(('motor_off', 'motor_on')):
        m[f'hoist_{stage}'] = model([element([2, 2, 6], [14, 14, 16], {'all': 'e', 'north': 'f'}, uv={'north': [0, 0, 16, 16]}), element([6, 6, 3], [10, 10, 6], {'all': 'e'})],
                                    {'e': 'steel', 'f': face}, 'steel')
    m['jib_0'] = model([element([6, 10, 0], [10, 14, 16], {'all': 'e'})], {'e': 'steel'}, 'steel')
    m['jib_1'] = model([element([6, 10, 8], [10, 14, 16], {'all': 'e'}), element([7, 5, 5], [9, 11, 11], {'all': 'e'}), element([7.5, 0, 7.5], [8.5, 5, 8.5], {'all': 'b'})],
                       {'e': 'steel', 'b': 'bracket'}, 'steel')
    m['crate_0'] = model([element([1, 0, 1], [15, 14, 15], {'all': 'c'}, uv={'up': [1, 1, 15, 15], 'down': [1, 1, 15, 15]}), element([7, 14, 7], [9, 16, 9], {'all': 'e'})],
                         {'c': 'crate', 'e': 'steel'}, 'crate')
    m['hatch_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'p', 'up': 'h'})], {'p': 'steel', 'h': 'hatch'}, 'hatch')
    m['hatch_1'] = model([element([0, 0, 0], [16, 16, 2], {'all': 'p'}), element([0, 0, 14], [16, 16, 16], {'all': 'p'}), element([0, 0, 2], [2, 16, 14], {'all': 'p'}),
                          element([14, 0, 2], [16, 16, 14], {'all': 'p'}), element([0, 16, 0], [16, 30, 2], {'all': 'h'}, uv={'north': [0, 1, 16, 15], 'south': [0, 1, 16, 15]})],
                         {'p': 'steel', 'h': 'hatch'}, 'hatch')
    return m


KINDS = ['holder_light', 'holder_shutter', 'holder_latch', 'holder_hoist', 'clip', 'cable_light', 'cable_shutter', 'cable_latch', 'cable_hoist',
         'work_lamp', 'shutter', 'shutter_box', 'guide', 'catch', 'latch', 'wedge', 'hoist', 'jib', 'crate', 'hatch']
STAGES = {k: 2 for k in ('holder_light', 'holder_shutter', 'holder_latch', 'holder_hoist', 'clip', 'work_lamp', 'shutter', 'shutter_box', 'catch', 'latch', 'wedge', 'hoist', 'jib', 'hatch')}
STAGES.update({k: 3 for k in ('cable_light', 'cable_shutter', 'cable_latch', 'cable_hoist')})
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 3
NAMES = {'power_fitting': 'Basement Electrics', 'power_floor': 'Painted Concrete', 'power_brick': 'Whitewashed Brick', 'power_ceiling': 'Concrete Soffit',
         'power_plate': 'Steel Plate'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/power_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/power_{name}'}}
        out[f'blockstates/power_{name}.json'] = {'variants': {'': {'model': NS + f'block/power_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/power/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/power/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/power_fitting.json'] = {'variants': variants}
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
            assert f'block/power/{tex.split("power/")[1]}.png' in images, (key, tex)
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
    for key, name in {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the basement electrics: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
