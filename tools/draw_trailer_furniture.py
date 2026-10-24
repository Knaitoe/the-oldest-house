"""Draw the trailer's dinette chairs and bunk frames: three pixel materials and their native models."""
from pathlib import Path
import argparse
import json
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
PREFIX = 'the_oldest_house:block/trailer/'

def paintings():
    out = {}
    def tile(name, color):
        im = Image.new('RGBA', (32, 32), color)
        out[name] = im
        return ImageDraw.Draw(im)
    # Warm knotty pine for the chairs.
    d = tile('chair_wood', '#b9824c')
    for y in (3, 9, 14, 20, 26, 30):
        d.line((0, y, 31, y), fill='#a5703f')
    for x, y in ((7, 6), (22, 17), (13, 27)):
        d.ellipse((x - 2, y - 1, x + 2, y + 1), fill='#7f4f2a')
    d.line((0, 0, 31, 0), fill='#d29a62')
    # A worn plaid cushion: rust, brown and cream.
    d = tile('chair_cushion', '#9a3f2b')
    for x in range(0, 32, 8):
        d.rectangle((x, 0, x + 2, 31), fill='#6e2c1f')
    for y in range(0, 32, 8):
        d.rectangle((0, y, 31, y + 2), fill='#6e2c1f')
    for n in range(4, 32, 8):
        d.line((n, 0, n, 31), fill='#d8c39a'); d.line((0, n, 31, n), fill='#d8c39a')
    for x, y in ((11, 13), (27, 29), (19, 5)):
        d.point((x, y), fill='#c6735a')
    # Darker stained bunk timber, nail heads at the joints.
    d = tile('bunk_wood', '#6b4429')
    for y in (2, 8, 15, 21, 28):
        d.line((0, y, 31, y), fill='#5a3820')
    for x, y in ((3, 3), (28, 3), (3, 28), (28, 28)):
        d.point((x, y), fill='#a9a39a')
    d.line((0, 31, 31, 31), fill='#47301c')
    return out

def box(lo, hi, texture, skip=()):
    faces = {f: {'texture': '#' + texture} for f in ('north', 'south', 'east', 'west', 'up', 'down') if f not in skip}
    return {'from': lo, 'to': hi, 'faces': faces}

def models():
    def model(elements, materials):
        return {'ambientocclusion': True, 'textures': {**{m: PREFIX + m for m in materials}, 'particle': PREFIX + materials[0]}, 'elements': elements}
    result = {}
    # The chair's back is on the east side, as a stair's raised step is in the native east-facing model.
    chair = [box([x, 0, z], [x + 1.5, 6, z + 1.5], 'wood', ('up',)) for x in (2, 12.5) for z in (2, 12.5)]
    chair += [box([1.5, 6, 1.5], [14.5, 7, 14.5], 'wood'), box([2, 7, 2], [13, 8, 14], 'cushion', ('down',)),
              box([13, 7, 2], [14.5, 19, 3.5], 'wood'), box([13, 7, 12.5], [14.5, 19, 14], 'wood'),
              box([13.2, 11, 3.5], [14.2, 18, 12.5], 'cushion'), box([13, 18, 2], [14.5, 19.5, 14], 'wood')]
    result['models/block/trailer_chair.json'] = model(chair, ['wood', 'cushion'])
    rails = [box([0, 13, 0], [16, 16, 1], 'wood'), box([0, 13, 15], [16, 16, 16], 'wood')]
    foot = [box([0, 0, 0], [2, 16, 2], 'wood'), box([0, 0, 14], [2, 16, 16], 'wood')] + rails
    foot += [box([.5, y, 2], [1.5, y + 1, 14], 'wood') for y in (3, 7, 11)]
    head = [box([14, 0, 0], [16, 16, 2], 'wood'), box([14, 0, 14], [16, 16, 16], 'wood'), box([14.5, 10, 2], [16, 16, 14], 'wood')] + rails
    result['models/block/trailer_bunk_foot.json'] = model(foot, ['wood'])
    result['models/block/trailer_bunk_head.json'] = model(head, ['wood'])
    for m in result.values():
        m['textures'] = {k: (PREFIX + ('chair_' if 'cushion' in m['textures'] else 'bunk_') + k if k != 'particle' else v) for k, v in m['textures'].items()}
        m['textures']['particle'] = m['textures']['wood']
    variants = {f'facing={facing}': ({'model': 'the_oldest_house:block/trailer_chair', 'y': y} if y else {'model': 'the_oldest_house:block/trailer_chair'})
                for facing, y in (('east', 0), ('south', 90), ('west', 180), ('north', 270))}
    result['blockstates/trailer_chair.json'] = {'variants': variants}
    result['blockstates/trailer_bunk_frame.json'] = {'variants': {'part=foot': {'model': 'the_oldest_house:block/trailer_bunk_foot'},
                                                                  'part=head': {'model': 'the_oldest_house:block/trailer_bunk_head'}}}
    return result

def main():
    p = argparse.ArgumentParser(); p.add_argument('--check', action='store_true'); check = p.parse_args().check
    for name, im in paintings().items():
        path = A / f'textures/block/trailer/{name}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for name, obj in models().items():
        path = A / name
        if check:
            assert json.loads(path.read_text()) == obj, name
        else:
            path.write_text(json.dumps(obj, indent=2) + '\n')
    print(('Verified' if check else 'Painted') + ' the trailer chairs and bunk frames.')

if __name__ == '__main__':
    main()
