"""Draw the trailer's dinette chairs and bunk frames: three pixel materials and their native models."""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
PREFIX = 'the_oldest_house:block/trailer/'

def shade(c, k):
    c = c.lstrip('#'); r, g, b = (int(c[i:i + 2], 16) for i in (0, 2, 4))
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), 255)

def grain(im, base, seed, streaks=7, knots=1, seams=()):
    """Plank grain like the native planks: tone noise, long wavy streaks, oval knots and optional plank seams."""
    rnd = random.Random(seed); px = im.load()
    for y in range(32):
        row = rnd.randint(-6, 6)
        for x in range(32):
            px[x, y] = shade(base, row + rnd.randint(-4, 4))
    for _ in range(streaks):
        y0, amp, period, phase, k = rnd.randint(0, 31), rnd.choice((0, 1, 1, 2)), rnd.uniform(6, 14), rnd.uniform(0, 6), rnd.choice((-18, -13, -9, 9))
        x0 = rnd.randint(0, 31)
        for i in range(rnd.randint(10, 32)):
            x = (x0 + i) % 32; y = (y0 + round(math.sin((x + phase) / period * math.pi) * amp)) % 32
            px[x, y] = shade(base, k + rnd.randint(-2, 2))
    for _ in range(knots):
        kx, ky = rnd.randint(4, 27), rnd.randint(4, 27)
        for dx in range(-2, 3):
            for dy in range(-1, 2):
                if abs(dx) + abs(dy) <= 2:
                    px[kx + dx, ky + dy] = shade(base, -30 if dy == 0 and abs(dx) <= 1 else -14)
    for y in seams:
        for x in range(32):
            px[x, y] = shade(base, -34); px[x, (y + 1) % 32] = shade(base, 10)

def paintings():
    out = {}
    # Warm knotty pine for the chairs.
    im = Image.new('RGBA', (32, 32)); grain(im, '#b9824c', 31, knots=2); out['chair_wood'] = im
    # A worn tartan cushion: rust ground, brown bands, a thin mustard and cream overcheck, two tufting buttons.
    im = Image.new('RGBA', (32, 32), '#9a3f2b'); out['chair_cushion'] = im; d = ImageDraw.Draw(im); rnd = random.Random(32); px = im.load()
    for n in range(0, 32, 10):
        d.rectangle((n, 0, n + 3, 31), fill='#7a3022'); d.rectangle((0, n, 31, n + 3), fill='#7a3022')
        d.rectangle((n, n, n + 3, n + 3), fill='#5e2419')
    for n in range(6, 32, 10):
        d.line((n, 0, n, 31), fill='#a8843e'); d.line((0, n + 2, 31, n + 2), fill='#b49a74')
    for y in range(32):
        for x in range(32):
            r, g, b, a = px[x, y]; k = rnd.randint(-6, 6); px[x, y] = (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)
    for x, y in ((9, 9), (23, 23)):
        d.ellipse((x - 1, y - 1, x + 1, y + 1), fill='#4e1d14'); d.point((x, y - 1), fill='#b8664e')
    # Darker stained bunk timber, plank seams and nail heads at the joints.
    im = Image.new('RGBA', (32, 32)); grain(im, '#6b4429', 33, knots=1, seams=(15,)); out['bunk_wood'] = im; d = ImageDraw.Draw(im)
    for x, y in ((3, 3), (28, 3), (3, 28), (28, 28), (3, 12), (28, 19)):
        d.point((x, y), fill='#b0aaa0'); d.point((x + 1, y + 1), fill='#3a2616')
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
