"""The Resonant Pipes (0.4.360): the drums, the brass pipework, its valves and sections, the reed boxes and the latch, in vanilla's manner.

16x16 textures from small palettes with per-pixel noise: an iron drum with one, two or three painted bands for its note; brass pipe, dull
or trembling with lifted dust; a red valve wheel; a short section on its bracket; a box of brass reeds behind a grille, still, singing
(dust lifting) or buzzing (blurred); an iron catch, down or lifted. Writes models, block states and the block's name. --check verifies.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import shade, dot, frame, element
from draw_sphinx_fittings import noisy

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/resonance/'


def brass(trembling):
    im = noisy(['#a8823c', '#9c7834', '#b48c44'], 3600)
    for x in range(16):
        dot(im, x, 0, '#6e5224'); dot(im, x, 15, '#6e5224'); dot(im, x, 5, '#d0a85c')
    if trembling:
        rnd = random.Random(3601)
        for _ in range(22):
            dot(im, rnd.randrange(16), rnd.randrange(1, 15), '#f0e6c8', 170)
    return im


def iron():
    return noisy(['#4c4c50', '#444448', '#56565a'], 3602)


def drum_face(bands):
    im = noisy(['#4c4c50', '#444448', '#56565a'], 3603 + bands)
    frame(im, 0, 0, 16, 16, '#2e2e32')
    for i in range(bands):
        y = 4 + i * 3
        for x in range(2, 14):
            dot(im, x, y, '#b03a2e')
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        dot(im, x, y, '#8a8a8e')
    return im


def wheel():
    return noisy(['#9c2a22', '#8a221c', '#aa3428'], 3606)


def reeds(stage):
    im = noisy(['#5a3e24', '#523820', '#62442a'], 3607)
    for x in range(2, 14):
        for y in range(3, 13):
            dot(im, x, y, '#1e160e')
    rnd = random.Random(3608 + stage)
    for x in range(3, 13, 2):
        top = 4 + (x % 3)
        for y in range(top, 12):
            wobble = (rnd.randrange(3) - 1) if stage == 2 else 0
            dot(im, max(2, min(13, x + wobble)), y, '#c8a050')
    if stage == 1:
        for _ in range(12):
            dot(im, rnd.randrange(2, 14), rnd.randrange(1, 5), '#f4ecd8', 190)
    for x in range(2, 14):
        dot(im, x, 7, '#3a2a18', 200)
    frame(im, 0, 0, 16, 16, '#3a2814')
    return im


def textures():
    t = {'brass_0': brass(False), 'brass_1': brass(True), 'iron': iron(), 'wheel': wheel()}
    t.update({f'drum_{n}': drum_face(n + 1) for n in range(3)})
    t.update({f'reeds_{n}': reeds(n) for n in range(3)})
    return {f'block/resonance/{k}.png': v for k, v in t.items()}


def model(elements, textures, particle):
    return {'ambientocclusion': True, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def pipe_along_z(tex):
    return [element([5, 5, 0], [11, 11, 16], {'north': tex, 'south': tex, 'east': tex, 'west': tex, 'up': tex, 'down': tex})]


def fitting_models():
    m = {}
    for s in range(3):
        m[f'drum_{s}'] = model([element([2, 0, 2], [14, 16, 14], {'north': 'f', 'south': 'f', 'east': 'i', 'west': 'i', 'up': 'i', 'down': 'i'}),
                                element([1, 2, 1], [15, 3, 15], {'all': 'i'}), element([1, 13, 1], [15, 14, 15], {'all': 'i'})],
                               {'f': f'drum_{s}', 'i': 'iron'}, 'iron')
    for s in range(2):
        m[f'pipe_{s}'] = model(pipe_along_z('b'), {'b': f'brass_{s}'}, f'brass_{s}')
        m[f'riser_{s}'] = model([element([5, 0, 5], [11, 16, 11], {'all': 'b'})], {'b': f'brass_{s}'}, f'brass_{s}')
        # A valve: a fatter body on the run, a spindle and a wheel; the handle bar along the run when open, across when shut.
        bar = element([7, 13, 3], [9, 14, 13], {'all': 'w'}) if s == 1 else element([3, 13, 7], [13, 14, 9], {'all': 'w'})
        m[f'valve_{s}'] = model(pipe_along_z('b') + [element([4, 4, 5], [12, 12, 11], {'all': 'i'}), element([7, 12, 7], [9, 13, 9], {'all': 'i'}), bar],
                                {'b': 'brass_0', 'i': 'iron', 'w': 'wheel'}, 'brass_0')
        # A short section: fitted, a run with two flanges; out, only the flanges, and the section hung on a bracket below.
        flanges = [element([4, 4, 0], [12, 12, 2], {'all': 'i'}), element([4, 4, 14], [12, 12, 16], {'all': 'i'})]
        loose = [element([5, 0, 4], [11, 4, 12], {'all': 'b'}), element([7, 4, 7], [9, 5, 9], {'all': 'i'})]
        m[f'section_{s}'] = model(flanges + (pipe_along_z('b') if s == 1 else loose), {'b': 'brass_0', 'i': 'iron'}, 'brass_0')
    for s in range(3):
        m[f'reed_{s}'] = model([element([2, 1, 11], [14, 15, 16], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r', 'down': 'r'},
                                        uv={'north': [0, 0, 16, 16]})], {'r': f'reeds_{s}'}, f'reeds_{s}')
    for s in range(2):
        catch = element([3, 10, 13], [13, 12, 15], {'all': 'i'}) if s == 0 else element([3, 10, 13], [5, 15, 15], {'all': 'i'})
        m[f'latch_{s}'] = model([element([2, 8, 15], [14, 14, 16], {'all': 'i'}), catch], {'i': 'iron'}, 'iron')
    return m


KINDS = ['drum', 'pipe', 'riser', 'valve', 'section', 'reed', 'latch']
STAGES = {'drum': 3, 'pipe': 2, 'riser': 2, 'valve': 2, 'section': 2, 'reed': 3, 'latch': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {f'models/block/resonance/{k}.json': v for k, v in fitting_models().items()}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(3):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/resonance/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/resonance_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    for key, obj in fitting_models().items():
        for tex in obj['textures'].values():
            assert f'block/resonance/{tex.split("resonance/")[1]}.png' in images, (key, tex)
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text()); key = 'block.the_oldest_house.resonance_fitting'
    if check:
        assert lang.get(key) == 'Resonant Pipework', key
    else:
        lang[key] = 'Resonant Pipework'; lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the resonant pipework: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
