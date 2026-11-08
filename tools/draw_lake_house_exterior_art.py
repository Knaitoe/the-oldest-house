"""The lake house's outside (0.4.379, docs/TEXTURE_PASS_LAKE_HOUSE_0_4_379.md): the texture pass's third interior release.

The lake house's rooms already had their own surfaces (lake_house_floor, _wall, _cedar, _pool_stone). Draws, in native Minecraft
pixels from fixed seeds, what was still vanilla round them: the drowned house's brick with silt in its joints and a film of
algae (two patterns, picked by position), its silted foundation stone, the boathouse's weathered board-and-batten, the dock's
bleached boards, and the culvert's board-formed concrete and the same stained where the water stands. Writes the models and block
states, and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/lake_house/'

BRICK = '#6d4b3e'
SILT = '#857b62'
ALGAE = '#4f5e36'
STONE = '#6a6c66'
SIDING = '#7b7166'
DOCK = '#8e897d'
CONCRETE = '#9b9a93'
STAIN = '#2e3a2c'

BRICKS = 2


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(a, b, t):
    a, b = (rgb(a) if isinstance(a, str) else a[:3]), (rgb(b) if isinstance(b, str) else b[:3])
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def drowned_brick(seed, k=0):
    """Brick in stretcher bond, four courses a block, gone dark under the lake: silt lying in the joints, a green film of algae over
    the top of each brick where it settles. The second pattern is further gone, the film thicker and a brick's face spalled away."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = {}
    film = 0.35 if k == 0 else 0.6
    for y in range(16):
        course = y // 4
        for x in range(16):
            key = (course, ((x + 4 * (course % 2)) // 8) % 2)
            if key not in tones:
                tones[key] = rnd.randint(-8, 8)
            joint = y % 4 == 3 or (x + 4 * (course % 2)) % 8 == 0
            if joint:
                im.putpixel((x, y), shade(SILT, rnd.randint(-6, 4)))
                continue
            c = shade(BRICK, tones[key] + rnd.randint(-3, 3))
            if y % 4 == 0 and rnd.random() < film + .3:
                c = mix(c, ALGAE, .55 + rnd.random() * .2)
            elif rnd.random() < film * .25:
                c = mix(c, ALGAE, .35)
            im.putpixel((x, y), c)
    if k == 1:
        cx, cy = rnd.randrange(2, 10), rnd.randrange(0, 3) * 4 + 1
        for dx in range(4):
            for dy in range(2):
                im.putpixel((cx + dx, cy + dy), shade(BRICK, -22 + rnd.randint(-3, 3)))
    return im


def silt_stone(seed):
    """The foundation: two courses of grey stone under a blanket of silt, thickest along the top of each course, the joints filled."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    cuts = [rnd.randrange(5, 11), rnd.randrange(3, 13)]
    for y in range(16):
        course, row = y // 8, y % 8
        for x in range(16):
            c = shade(STONE, rnd.randint(-4, 4) + (-3 if x < cuts[course] else 3))
            c = mix(c, SILT, max(0, .55 - row * .08) + rnd.random() * .15)
            if row == 7 or x == cuts[course]:
                c = shade(SILT, -14 + rnd.randint(-3, 3))
            im.putpixel((x, y), c)
    return im


def siding(seed):
    """Board-and-batten gone silver: wide boards with their grain run up them, a narrow batten over each joint, lit down one
    edge and shadowed down the other, a nail head or two."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    grain = [rnd.randint(-5, 5) for _ in range(16)]
    for x in range(16):
        col = x % 8
        for y in range(16):
            if col == 0:
                k = 9 + rnd.randint(-2, 2)
            elif col == 1:
                k = -4 + rnd.randint(-2, 2)
            elif col == 2:
                k = -14 + rnd.randint(-2, 2)
            else:
                k = grain[x] + rnd.randint(-2, 2) + (y * 3 + x) % 7 // 3
            im.putpixel((x, y), shade(SIDING, k))
    for x, y in ((0, 3), (8, 11), (1, 12), (9, 4)):
        im.putpixel((x, y), shade(SIDING, -22))
    return im


def dock(seed):
    """Dock boards bleached grey by the lake, four a block with a dark gap between them, the grain open and a nail at each end."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tone = [rnd.randint(-5, 5) for _ in range(4)]
    for x in range(16):
        b, col = x // 4, x % 4
        for y in range(16):
            k = tone[b] + rnd.randint(-3, 3) + ((y + b * 5) % 5 == 0) * -5
            if col == 3:
                k = -34 + rnd.randint(-3, 3)
            elif col == 0:
                k += 5
            im.putpixel((x, y), shade(DOCK, k))
    for b in range(4):
        for y in (1, 14):
            im.putpixel((b * 4 + 1, y), shade(DOCK, -24))
    return im


def culvert(seed, stained=False):
    """Board-formed concrete: the faint lines of the shuttering boards across it every four pixels, two tie holes. Stained: dark
    and green where the water stands, darkest at the foot, a slime run down from the top."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3) + (-6 if y % 4 == 3 else 2 if y % 4 == 0 else 0)
            c = shade(CONCRETE, k)
            if stained:
                c = mix(c, STAIN, min(.85, .25 + y * .04 + rnd.random() * .1))
            im.putpixel((x, y), c)
    for x, y in ((4, 5), (12, 9)):
        im.putpixel((x, y), shade(CONCRETE, -40))
        im.putpixel((x + 1, y), shade(CONCRETE, -22))
    if stained:
        for x in (rnd.randrange(1, 7), rnd.randrange(9, 15)):
            for y in range(0, rnd.randrange(5, 10)):
                im.putpixel((x, y), mix(im.getpixel((x, y)), ALGAE, .6))
    return im


BLOCKS = [
    ('drowned_brick', 'Drowned Brick'),
    ('silt_stone', 'Silted Stone'),
    ('siding', 'Weathered Siding'),
    ('dock', 'Dock Boards'),
    ('culvert', 'Culvert Concrete'),
    ('culvert_stained', 'Stained Culvert Concrete'),
]


def textures():
    out = {'silt_stone': silt_stone(3791), 'siding': siding(3792), 'dock': dock(3793), 'culvert': culvert(3794),
           'culvert_stained': culvert(3794, stained=True)}
    for k in range(BRICKS):
        out['drowned_brick' + (f'_{k}' if k else '')] = drowned_brick(3790 + 10 * k, k)
    return out


def files():
    out = {}
    for name, _ in BLOCKS:
        model = NS + f'block/lake_house_{name}'
        out[f'models/block/lake_house_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        variant = {'model': model}
        if name == 'drowned_brick':
            # Picked by where it stands: most of the brick has a light film, some is further gone.
            out[f'models/block/lake_house_{name}_1.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'{name}_1'}}
            variant = [{'model': model, 'weight': 2}, {'model': model + '_1'}]
        elif name == 'dock':
            variant = [{'model': model}, {'model': model, 'y': 180}]
        out[f'blockstates/lake_house_{name}.json'] = {'variants': {'': variant}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/lake_house' / f'{key}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), key
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for name, title in BLOCKS:
        key = f'block.the_oldest_house.lake_house_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the lake house\'s outside: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
