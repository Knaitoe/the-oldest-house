"""A Deer in the Road (0.4.310): the sedan, the doe, the next vehicle and the roadside, in vanilla's manner.

16x16 textures from small palettes with per-pixel noise. The car: faded paint, chrome, a grille, tan vinyl and its foam, carpet,
tyres and hubs, the dash, the rear belt's webbing, and lamps (low beam, amber, tail, dome). The doe: her coat, her pale belly, her
face, the wound, the eyeshine. The next vehicle's paint and dark glass. The roadside: the hatched shoulder, the ditch's mud, the
county's notice and the crossing sign, the marker post, the culvert grate (with fingers through it), the triangle and its case, the
pump-house lamp, and the drainage gate's two halves. Models and block states for the fittings, materials, anchor, cells and gate.
Advancement text is generated from the Witness account by generate_advancements.py.
--check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import shade, fill, dot, frame, line, element
from draw_sphinx_fittings import noisy

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/deer_road/'


def flat(c):
    im = Image.new('RGBA', (16, 16)); fill(im, 0, 0, 16, 16, c); return im


def paint():
    im = noisy(['#5e3a36', '#56332f', '#64403b', '#5a3632'], 3101, [4, 3, 2, 2])
    for x in range(16):
        dot(im, x, 0, '#76504a')
    return im


def under():
    return noisy(['#1e1c1a', '#262421', '#1a1816'], 3102)


def chrome():
    im = noisy(['#b8bcc0', '#a8acb0', '#c8ccd0'], 3103)
    for x in range(16):
        dot(im, x, 1, '#e4e8ec'); dot(im, x, 14, '#7c8084')
    return im


def grille():
    im = flat('#2a2a2c')
    for y in range(1, 16, 3):
        for x in range(16):
            dot(im, x, y, '#8c9094')
    return im


def vinyl():
    im = noisy(['#9a8466', '#927c5e', '#a28c6e'], 3104)
    for y in (5, 11):
        for x in range(16):
            dot(im, x, y, '#7a6650')
    return im


def foam():
    im = noisy(['#d8c070', '#ccb462', '#e2ca7c', '#c0a856'], 3105, [4, 3, 2, 1])
    rnd = random.Random(3106)
    for _ in range(12):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#a89048')
    return im


def carpet():
    return noisy(['#3a3430', '#36302c', '#3e3834'], 3107)


def tyre():
    im = noisy(['#1c1c1c', '#222222', '#181818'], 3108)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, '#2c2c2c')
    return im


def hub():
    im = flat('#7a7e82'); frame(im, 2, 2, 14, 14, '#9ea2a6'); fill(im, 6, 6, 10, 10, '#5a5e62'); return im


def dash():
    return noisy(['#2c2826', '#28241f', '#302c29'], 3109)


def belt():
    im = noisy(['#4a4440', '#443e3a', '#504a46'], 3110)
    for y in range(16):
        dot(im, 0, y, '#2e2a27'); dot(im, 15, y, '#2e2a27')
    return im


def lamp(core, edge):
    im = flat(edge); fill(im, 2, 2, 14, 14, core); dot(im, 4, 4, '#ffffff'); return im


def fur():
    im = noisy(['#8a6a4a', '#806244', '#94744f', '#7a5c40'], 3111, [4, 3, 2, 2])
    rnd = random.Random(3112)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#6a4e36')
    return im


def fur_belly():
    return noisy(['#d8ccb8', '#cec2ae', '#e2d6c2'], 3113)


def doe_face():
    im = fur()
    fill(im, 6, 11, 10, 15, '#2a201a'); dot(im, 3, 6, '#141010'); dot(im, 12, 6, '#141010'); fill(im, 5, 9, 11, 10, '#a0846a')
    return im


def wound():
    im = noisy(['#4a1414', '#3a0e0e', '#561a18'], 3114)
    for x in range(16):
        dot(im, x, 8, '#1a0606')
    return im


def eyeshine():
    return flat('#e8ecd0')


def van():
    im = noisy(['#c8c8c0', '#bebeb6', '#d0d0c8'], 3115)
    for x in range(16):
        dot(im, x, 15, '#9a9a92')
    return im


def glass_dark():
    return noisy(['#1a2228', '#202a30', '#161c22'], 3116)


def triangle_case():
    im = noisy(['#3c3c3e', '#363638', '#424244'], 3117); line(im, 2, 13, 8, 3, '#c04a3a'); line(im, 8, 3, 13, 13, '#c04a3a'); line(im, 2, 13, 13, 13, '#c04a3a'); return im


def hatch():
    im = noisy(['#46464a', '#3e3e42', '#4c4c50'], 3118)
    for k in range(-16, 16, 6):
        for i in range(3):
            line(im, k + i, 15, k + i + 15, 0, '#bcbcb4')
    return im


def mud():
    return noisy(['#3a2e24', '#342820', '#40342a', '#2e241c'], 3119)


def notice():
    im = flat('#d6d2c2'); frame(im, 0, 0, 16, 16, '#4a4842')
    fill(im, 2, 2, 14, 4, '#2a2a28')
    rnd = random.Random(3120)
    for y in range(6, 14, 2):
        x = 2
        while x < 13:
            w = rnd.randrange(1, 4)
            for i in range(w):
                if x + i < 14:
                    dot(im, x + i, y, '#5a5850')
            x += w + 1
    return im


def sign():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        r = 7 - abs(y - 7.5) + .5
        for x in range(16):
            if abs(x - 7.5) <= r:
                im.putpixel((x, y), shade('#d8b23a', 0))
            if abs(abs(x - 7.5) - r) < 1 and abs(x - 7.5) <= r:
                im.putpixel((x, y), shade('#1e1c18', 0))
    for x, y in ((5, 7), (6, 7), (7, 7), (8, 7), (9, 6), (10, 5), (5, 8), (5, 9), (8, 8), (8, 9), (10, 4), (11, 5)):
        dot(im, x, y, '#1e1c18')
    return im


def post():
    im = noisy(['#d8d8d2', '#cecec8'], 3121); fill(im, 0, 3, 16, 5, '#1e1e1e'); fill(im, 0, 1, 16, 3, '#e8b030'); return im


def pole():
    return noisy(['#7a7c7e', '#6e7072', '#848688'], 3122)


def grate(hand):
    im = Image.new('RGBA', (16, 16), (12, 12, 14, 255))
    for x in range(1, 16, 3):
        fill(im, x, 0, x + 1, 16, '#5a5650')
    for y in (0, 15):
        fill(im, 0, y, 16, y + 1, '#4a4640')
    if hand:
        for x, top in ((3, 6), (6, 5), (9, 5), (12, 7)):
            fill(im, x - 1, top, x + 1, top + 4, '#c8bba4')
            dot(im, x - 1, top, '#a89a84')
    return im


def triangle():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(3, 15):
        half = (y - 3) * 7 / 11
        for x in range(16):
            d = abs(x - 7.5)
            if d <= half:
                inner = d <= half - 2 and y < 13
                im.putpixel((x, y), shade('#1e1c1a' if inner else '#c43a2c', 0))
    fill(im, 2, 14, 14, 16, '#3a3a3a')
    return im


def lamp_cage():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); fill(im, 5, 5, 11, 12, '#f4e2a0'); frame(im, 4, 4, 12, 13, '#3a3a36'); fill(im, 7, 0, 9, 4, '#3a3a36'); return im


def gate(half):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x in range(0, 16, 3):
        fill(im, x, 0, x + 2, 16, '#58544c')
    fill(im, 0, 0, 16, 2, '#4a463e'); fill(im, 0, 14, 16, 16, '#4a463e')
    if half == 'lower':
        fill(im, 12, 1, 15, 4, '#8c8676'); dot(im, 13, 2, '#bab4a2')
    return im


def textures():
    t = {'paint': paint(), 'under': under(), 'chrome': chrome(), 'grille': grille(), 'vinyl': vinyl(), 'foam': foam(), 'carpet': carpet(),
         'tyre': tyre(), 'hub': hub(), 'dash': dash(), 'belt': belt(), 'headlamp': lamp('#fff6d8', '#d8ccb0'), 'amber': lamp('#f0a030', '#a06418'),
         'tail': lamp('#c42a20', '#781812'), 'dome': lamp('#f8ecc8', '#c8bc98'), 'triangle_case': triangle_case(), 'fur': fur(),
         'fur_belly': fur_belly(), 'doe_face': doe_face(), 'wound': wound(), 'eyeshine': eyeshine(), 'van': van(), 'glass_dark': glass_dark(),
         'notice': notice(), 'sign': sign(), 'post': post(), 'pole': pole(), 'grate': grate(False), 'grate_hand': grate(True),
         'triangle': triangle(), 'lamp': lamp_cage(), 'gate_lower': gate('lower'), 'gate_upper': gate('upper')}
    out = {f'block/deer_road/{k}.png': v for k, v in t.items()}
    out['block/deer_road_hatch.png'] = hatch()
    out['block/deer_road_mud.png'] = mud()
    out['item/deer_road_triangle.png'] = triangle()
    return out


def model(elements, tex, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in tex.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    pole_el = element([7, 0, 7], [9, 16, 9], {'all': 'p'})
    m['notice_0'] = model([pole_el, element([2, 4, 6.5], [14, 14, 7.5], {'north': 'n', 'south': 'n', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'})], {'p': 'pole', 'n': 'notice'}, 'notice')
    m['sign_0'] = model([element([7, 0, 7], [9, 4, 9], {'all': 'p'}), element([0, 2, 6.5], [16, 16, 7.5], {'north': 's', 'south': 's'})], {'p': 'pole', 's': 'sign'}, 'pole')
    m['marker_0'] = model([element([6, 0, 6], [10, 14, 10], {'north': 'm', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm'}, uv={f: [0, 2, 4, 16] for f in ('north', 'south', 'east', 'west', 'up')})], {'m': 'post'}, 'post')
    for st, tex in ((0, 'grate'), (1, 'grate_hand')):
        m[f'grate_{st}'] = model([element([0, 0, 0], [16, 16, 2], {'north': 'g', 'south': 'g'}), element([0, 0, 14], [16, 16, 16], {'north': 'g', 'south': 'g'})], {'g': tex}, tex)
    m['triangle_0'] = model([element([2, 0, 7.5], [14, 12, 8.5], {'north': 't', 'south': 't'}), element([3, 0, 5], [13, 1, 11], {'up': 'c', 'down': 'c'})], {'t': 'triangle', 'c': 'triangle_case'}, 'triangle')
    m['lamp_0'] = model([element([5, 6, 12], [11, 13, 16], {'all': 'l'})], {'l': 'lamp'}, 'lamp')
    m['carcass_0'] = {'parent': 'minecraft:block/block', 'textures': {'particle': P + 'fur'}, 'elements': []}
    return m


KINDS = ['notice', 'sign', 'marker', 'grate', 'triangle', 'carcass', 'lamp']
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'deer_road_fitting': 'Roadside Fittings', 'deer_road_hatch': 'Hatched Shoulder', 'deer_road_mud': 'Ditch Mud', 'deer_road_anchor': 'Road',
         'deer_road_car': 'Car', 'deer_road_gate': 'Drainage Gate'}


def door_files(out):
    for half, vanilla in (('lower', 'bottom'), ('upper', 'top')):
        for hinge in ('left', 'right'):
            for opened in ('', '_open'):
                out[f'models/block/deer_road_gate_{half}_{hinge}{opened}.json'] = {'parent': f'minecraft:block/door_{vanilla}_{hinge}{opened}', 'render_type': 'minecraft:cutout',
                                                                                  'textures': {'top': P + 'gate_upper', 'bottom': P + 'gate_lower'}}
    variants = {}
    for facing, y in {'east': 0, 'south': 90, 'west': 180, 'north': 270}.items():
        for half in ('lower', 'upper'):
            for hinge in ('left', 'right'):
                for opened in (False, True):
                    turn = y + (90 if hinge == 'left' else -90) if opened else y
                    variants[f'facing={facing},half={half},hinge={hinge},open={str(opened).lower()}'] = {'model': NS + f'block/deer_road_gate_{half}_{hinge}{"_open" if opened else ""}', 'y': turn % 360}
    out['blockstates/deer_road_gate.json'] = {'variants': variants}


def files():
    out = {}
    for name in ('hatch', 'mud'):
        out[f'models/block/deer_road_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/deer_road_{name}'}}
        out[f'blockstates/deer_road_{name}.json'] = {'variants': {'': {'model': NS + f'block/deer_road_{name}'}}}
    out['models/block/deer_road_anchor.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': 'minecraft:block/stone'}}
    out['blockstates/deer_road_anchor.json'] = {'variants': {'': {'model': NS + 'block/deer_road_anchor'}}}
    out['models/block/deer_road_car.json'] = {'parent': 'minecraft:block/block', 'textures': {'particle': P + 'paint'}, 'elements': []}
    out['blockstates/deer_road_car.json'] = {'variants': {'': {'model': NS + 'block/deer_road_car'}}}
    out.update({f'models/block/deer_road/{k}.json': v for k, v in fitting_models().items()})
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(2):
                drawn = stage if kind == 'grate' else 0
                v = {'model': NS + f'block/deer_road/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/deer_road_fitting.json'] = {'variants': variants}
    out['models/item/deer_road_triangle.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/deer_road_triangle'}}
    door_files(out)
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    names = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    names['item.the_oldest_house.deer_road_triangle'] = 'Warning Triangle'
    names['entity.the_oldest_house.deer_road_seat'] = 'Seat'
    for key, name in names.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the county road: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
