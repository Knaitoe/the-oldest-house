"""Mapping the Interior (0.4.396, docs/TEXTURE_PASS_MAPPING_0_4_396.md): the texture pass's tenth release.

Draws, in native Minecraft pixels from fixed seeds, what was still vanilla in the brothers' house: the panelling of its partitions
and inside its outer walls (the siding stays outside), the popcorn ceiling, the kitchen's linoleum, the three rooms' carpets, the
vinyl skirting round its base, the asphalt shingles of its roof and the plywood under its eaves, and below it the tunnel's
trodden floor and the cistern's old brick. Writes the models and block states (the outer walls by facing, the roof's edge as a
slab) and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/room_shell/'

WALNUT = '#6e4b2e'
POPCORN = '#e2dccb'
LINO = '#d9cfa9'
LINO_LINE = '#9c8f6c'
SKIRT = '#d6d1c2'
CARPETS = {'mapping_carpet_front': '#87582f', 'mapping_carpet_boys': '#3d5079', 'mapping_carpet_bed': '#8d8577'}
SHINGLE = '#4a4b4f'
SOFFIT = '#b59b72'
EARTH = '#6b5440'
BRICK = '#6f4f3e'
MORTAR = '#3e3a33'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def panel(seed):
    """Prefinished wall panelling: grooves at uneven spacing, a printed walnut grain running down, a knot or two."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    grooves = {0, 3, 7, 12}
    tone = [rnd.randint(-6, 6) for _ in range(16)]
    knots = [(rnd.randrange(1, 15), rnd.randrange(2, 14)) for _ in range(2)]
    for x in range(16):
        streak = rnd.random() < .35
        for y in range(16):
            k = tone[x] // 2 + rnd.randint(-2, 2) + (-7 if streak and (y + x) % 5 < 3 else 0)
            if any(abs(x - kx) + abs(y - ky) <= 1 for kx, ky in knots):
                k -= 14
            if x in grooves:
                k = -34 if x else -28
            im.putpixel((x, y), shade(WALNUT, k))
    return im


def popcorn(seed):
    """The popcorn ceiling: a close stipple of lumps, the light catching their tops."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            k = rnd.randint(-2, 2) + (9 if r < .18 else -10 if r > .8 else 0)
            im.putpixel((x, y), shade(POPCORN, k))
    return im


def lino(seed):
    """Kitchen linoleum printed as tiles, every other one a shade darker: a hairline grid, a dark nick where four meet, worn pale near the middle."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3) + (4 if 4 <= x <= 11 and 4 <= y <= 11 else 0) - (7 if (x // 8 + y // 8) % 2 else 0)
            c = shade(LINO, k)
            if x % 8 == 0 or y % 8 == 0:
                c = shade(LINO_LINE, rnd.randint(-3, 3))
            if abs(x % 8 - 0) + abs(y % 8 - 0) == 1 or (x % 8 == 7 and y % 8 == 0) or (x % 8 == 0 and y % 8 == 7):
                c = shade(LINO_LINE, -18)
            im.putpixel((x, y), c)
    return im


def skirt(seed):
    """Vinyl skirting round the house's base: ribbed panels standing upright, mud splashed up from the yard."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        rib = x % 4
        for y in range(16):
            k = rnd.randint(-2, 2) + (8 if rib == 0 else -10 if rib == 3 else 0)
            c = shade(SKIRT, k)
            if y >= 12 and rnd.random() < (y - 11) * .18:
                c = shade('#6a5a44', rnd.randint(-8, 6))
            im.putpixel((x, y), c)
    return im


def carpet(seed, base):
    """Wall-to-wall carpet: a deep pile of tufts, each a little lighter at its tip."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(base, rnd.randint(-8, 4)))
    for _ in range(40):
        x, y = rnd.randrange(16), rnd.randrange(15)
        im.putpixel((x, y), shade(base, 12))
        im.putpixel((x, y + 1), shade(base, -14))
    return im


def shingles(seed):
    """Asphalt shingles in courses of four: tabs of uneven width, the courses staggered, gritty, a shadow under each lip."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for course in range(4):
        cuts = sorted(rnd.sample(range(1, 16), 3))
        tone = rnd.randint(-6, 6)
        for y in range(course * 4, course * 4 + 4):
            for x in range(16):
                r = rnd.random()
                k = tone + rnd.randint(-3, 3) + (-16 if r < .07 else 10 if r > .95 else 0)
                if y % 4 == 3:
                    k -= 16
                if x in cuts and y % 4 != 0:
                    k -= 20
                im.putpixel((x, y), shade(SHINGLE, k))
    return im


def soffit(seed):
    """The plywood under the eaves, with a strip of vent holes."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-4, 4) + ((x + y // 3) % 7 == 0) * -6
            if 6 <= y <= 9 and x % 2 == 0 and y % 2 == 1:
                k = -40
            im.putpixel((x, y), shade(SOFFIT, k))
    return im


def shingle_edge(seed):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            c = shade(SHINGLE, rnd.randint(-4, 4)) if y < 8 else shade('#b8b6ad', -14 if y == 15 else rnd.randint(-3, 3)) if y < 10 else shade(SOFFIT, rnd.randint(-4, 4))
            im.putpixel((x, y), c)
    return im


def tunnel(seed):
    """The tunnel's floor, trodden hard: packed earth with two long smears where knees have gone, a stone here and there."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            k = rnd.randint(-5, 5) + (-12 if x in (4, 5, 10, 11) else 0) + (14 if r > .96 else -10 if r < .05 else 0)
            im.putpixel((x, y), shade(EARTH, k))
    return im


def cistern(seed):
    """The cistern's old brick, small and wet, the mortar dark and a green bloom along the bottom course."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        row = y // 4
        for x in range(16):
            off = (x + 4 * (row % 2)) % 8
            k = rnd.randint(-6, 6)
            c = shade(BRICK, k)
            if y % 4 == 3 or off == 0:
                c = shade(MORTAR, rnd.randint(-3, 3))
            if y >= 12 and rnd.random() < .3:
                c = shade('#4f5e34', rnd.randint(-6, 6))
            im.putpixel((x, y), c)
    return im


BLOCKS = [('mapping_wall', 'Siding'), ('mapping_panel', 'Wood Panelling'), ('mapping_ceiling', 'Popcorn Ceiling'),
          ('mapping_lino', 'Kitchen Linoleum'), ('mapping_skirt', 'Vinyl Skirting'), ('mapping_carpet_front', 'Front Room Carpet'),
          ('mapping_carpet_boys', 'Boys\' Room Carpet'), ('mapping_carpet_bed', 'Bedroom Carpet'),
          ('mapping_shingles', 'Asphalt Shingles'), ('mapping_shingle_slab', 'Asphalt Shingle Slab'),
          ('mapping_tunnel', 'Tunnel Floor'), ('mapping_cistern', 'Cistern Brick')]


def textures():
    out = {'mapping_panel': panel(3831), 'mapping_ceiling': popcorn(3832), 'mapping_lino': lino(3833), 'mapping_skirt': skirt(3834),
           'mapping_shingles': shingles(3838), 'mapping_soffit': soffit(3839), 'mapping_shingle_edge': shingle_edge(3840),
           'mapping_tunnel': tunnel(3841), 'mapping_cistern': cistern(3842)}
    for i, (name, base) in enumerate(CARPETS.items()):
        out[name] = carpet(3835 + i, base)
    return out


def cube(name):
    return {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}


def files():
    out = {}
    # The outer wall: the house's siding on the side it faces, the panelling within.
    wall = NS + 'block/room_shell_mapping_wall'
    faces = {'north': '#out', 'south': '#in', 'east': '#out', 'west': '#out', 'up': '#out', 'down': '#out'}
    out['models/block/room_shell_mapping_wall.json'] = {
        'parent': 'minecraft:block/block',
        'textures': {'particle': NS + 'block/manufactured_siding', 'out': NS + 'block/manufactured_siding', 'in': P + 'mapping_panel'},
        'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {f: {'texture': t, 'cullface': f} for f, t in faces.items()}}]}
    out['blockstates/room_shell_mapping_wall.json'] = {'variants': {
        f'facing={f}': dict({'model': wall}, **({'y': y} if y else {})) for f, y in {'north': 0, 'east': 90, 'south': 180, 'west': 270}.items()}}
    for name in ('mapping_panel', 'mapping_ceiling', 'mapping_skirt', 'mapping_shingles', 'mapping_cistern'):
        out[f'models/block/room_shell_{name}.json'] = cube(name)
        out[f'blockstates/room_shell_{name}.json'] = {'variants': {'': {'model': NS + f'block/room_shell_{name}'}}}
    for name in ['mapping_lino', 'mapping_tunnel'] + list(CARPETS):
        model = NS + f'block/room_shell_{name}'
        out[f'models/block/room_shell_{name}.json'] = cube(name)
        out[f'blockstates/room_shell_{name}.json'] = {'variants': {'': [{'model': model}, {'model': model, 'y': 90},
                                                                          {'model': model, 'y': 180}, {'model': model, 'y': 270}]}}
    tex = {'bottom': P + 'mapping_soffit', 'top': P + 'mapping_shingles', 'side': P + 'mapping_shingle_edge'}
    slab = NS + 'block/room_shell_mapping_shingle_slab'
    out['models/block/room_shell_mapping_shingle_slab.json'] = {'parent': 'minecraft:block/slab', 'textures': tex}
    out['models/block/room_shell_mapping_shingle_slab_top.json'] = {'parent': 'minecraft:block/slab_top', 'textures': tex}
    out['blockstates/room_shell_mapping_shingle_slab.json'] = {'variants': {
        'type=bottom': {'model': slab}, 'type=top': {'model': slab + '_top'}, 'type=double': {'model': NS + 'block/room_shell_mapping_shingles'}}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/room_shell' / f'{key}.png'
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
        key = f'block.the_oldest_house.room_shell_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the brothers\' house: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
