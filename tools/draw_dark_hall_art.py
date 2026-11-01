"""The dark halls (0.4.208): the house the book describes, smooth ash-grey, without fixtures.

Draws, in native Minecraft pixels from fixed seeds, three materials: the ash wall (smooth, faintly mottled, a hairline seam now
and then), its floor (darker, scuffed) and its ceiling (darkest). --check verifies everything already written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/dark_hall/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mottled(base, seed, spread, seams):
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, rnd.randint(-spread, spread))
    for _ in range(seams):
        x0, y0 = rnd.randrange(16), rnd.randrange(16); length = rnd.randint(3, 7); horizontal = rnd.random() < .5
        for i in range(length):
            x, y = (x0 + i, y0) if horizontal else (x0, y0 + i)
            if 0 <= x < 16 and 0 <= y < 16:
                px[x, y] = shade(px[x, y], -9)
    return im


def materials():
    return {'ash': mottled('#6e6c68', 811, 3, 2), 'floor': mottled('#4c4a47', 812, 5, 4), 'ceiling': mottled('#3a3937', 813, 2, 1)}


def files():
    out = {}
    for name in materials():
        out[f'models/block/dark_hall_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/dark_hall_{name}.json'] = {'variants': {'': {'model': NS + f'block/dark_hall_{name}'}}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images = {f'block/dark_hall/{k}.png': v for k, v in materials().items()}
    data = files()
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
    print(('Verified' if check else 'Drew') + f' the dark halls: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
