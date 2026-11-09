"""The cabin at the end of the world (0.4.406, docs/TEXTURE_PASS_CABIN_0_4_406.md): the texture pass's eleventh release.

Draws, in native Minecraft pixels from fixed seeds, what was still vanilla in the cabin by the lake: the weathered shiplap of its
outer walls (the knotty pine it already had stays inside), the fieldstone its walls stand on with a dark skirting within, its wide
worn floorboards, the tongue-and-groove ceiling between hewn beams, the cedar shakes of its roof with their eaves, the board and
batten of its gables and the grey decking of its porch. Writes the models and block states (the outer walls and their foot by
facing, the roof's edge as a slab) and the blocks' names. --check verifies everything written.
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
PINE = NS + 'block/interiors/holiday_pine'

CEDAR = '#7a6753'
STONES = ['#7d7a72', '#8b8477', '#6d6a64', '#94897a', '#76705f']
MORTAR = '#4a463f'
FLOOR = '#9a6e43'
CEILING = '#c4a273'
BEAM = '#4b3626'
SHAKE = '#6c6255'
MOSS = '#56613a'
GABLE = '#6a5947'
DECK = '#8a8274'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def shiplap(seed):
    """Weathered shiplap: four boards a block, each a shade of its own, the lip of the one above casting a dark line, butt joints
    staggered, grey where the weather has had it longest, a nail head at each end of a board."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    joints = [rnd.randrange(3, 13) for _ in range(4)]
    tone = [rnd.randint(-8, 8) for _ in range(4)]
    for y in range(16):
        board, row = y // 4, y % 4
        for x in range(16):
            k = tone[board] + rnd.randint(-3, 3) + (6 if row == 0 else 0)
            if (x * 7 + y * 3) % 11 == 0:
                k -= 6
            c = shade(CEDAR, k)
            if row == 3:
                c = shade(CEDAR, -30 + rnd.randint(-2, 2))
            elif x == joints[board]:
                c = shade(CEDAR, -22)
            elif row == 1 and x == (joints[board] + 1) % 16:
                c = shade(CEDAR, -18)
            if row < 3 and rnd.random() < .025:
                c = shade(CEDAR, 14)
            im.putpixel((x, y), c)
    return im


def fieldstone(seed):
    """Fieldstone laid in dark mortar: rounded stones of several greys and browns, each lit along its top and shadowed below."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), shade(MORTAR, 0))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(MORTAR, rnd.randint(-4, 4)))
    stones = [(2, 2, 3, 2), (8, 2, 4, 2), (13, 3, 2, 2), (4, 7, 3, 2), (11, 8, 3, 2), (1, 12, 2, 2), (7, 12, 3, 3), (13, 13, 2, 2)]
    for i, (cx, cy, rx, ry) in enumerate(stones):
        base = STONES[(i + seed) % len(STONES)]
        for y in range(cy - ry, cy + ry + 1):
            for x in range(cx - rx, cx + rx + 1):
                if ((x - cx) / (rx + .5)) ** 2 + ((y - cy) / (ry + .5)) ** 2 > 1:
                    continue
                k = rnd.randint(-4, 4) + (10 if y < cy - ry // 2 else -10 if y > cy + ry // 2 else 0)
                im.putpixel((x % 16, y % 16), shade(base, k))
    return im


def floor(seed):
    """Wide pine floorboards: three boards to a block, a dark seam between them, a board's end and its two nails, worn paler where
    feet go."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    seams, tone = (0, 5, 11), [rnd.randint(-8, 8) for _ in range(3)]
    end = rnd.randrange(3, 13)
    for x in range(16):
        board = 0 if x < 5 else 1 if x < 11 else 2
        for y in range(16):
            k = tone[board] + rnd.randint(-3, 3) + (5 if 5 <= y <= 10 else 0)
            if (y + x * 3) % 7 == 0:
                k -= 5
            c = shade(FLOOR, k)
            if x in seams:
                c = shade(FLOOR, -32)
            elif board == 1 and y == end:
                c = shade(FLOOR, -26)
            elif board == 1 and y == end + 1 and x in (7, 9):
                c = shade(FLOOR, -20)
            im.putpixel((x, y), c)
    return im


def ceiling(seed):
    """Tongue-and-groove ceiling boards, four to a block, pale pine darkened a little by years of a stove."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tone = [rnd.randint(-6, 6) for _ in range(4)]
    for x in range(16):
        for y in range(16):
            k = tone[x // 4] + rnd.randint(-3, 3) - y // 4
            c = shade(CEILING, k)
            if x % 4 == 0:
                c = shade(CEILING, -26)
            elif x % 4 == 1:
                c = shade(CEILING, k + 7)
            im.putpixel((x, y), c)
    return im


def beam(seed):
    """A hewn beam: dark timber with the scallops an adze leaves, a check running along the grain."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    check = rnd.randrange(4, 12)
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3) + (6 if (x + 2 * (y // 4)) % 5 == 0 else -3 if (x + 2 * (y // 4)) % 5 == 4 else 0)
            c = shade(BEAM, k)
            if y == check and 2 <= x <= 13:
                c = shade(BEAM, -18)
            im.putpixel((x, y), c)
    return im


def shakes(seed):
    """Cedar shakes in courses: split by hand to uneven widths, each course's butts shadowed, silvered by the weather, moss in the
    joints."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for course in range(4):
        x, edges = rnd.randrange(0, 3), set()
        while x < 16:
            edges.add(x)
            x += rnd.randint(3, 5)
        tone = {e: rnd.randint(-10, 8) for e in edges}
        for row in range(4):
            y = course * 4 + row
            last = 0
            for x in range(16):
                if x in edges:
                    last = x
                k = tone.get(last, 0) + rnd.randint(-3, 3) - (12 if row == 3 else 0)
                c = shade(SHAKE, k)
                if x in edges:
                    c = shade(SHAKE, -24)
                    if rnd.random() < .45:
                        c = shade(MOSS, rnd.randint(-6, 4))
                elif row == 3 and rnd.random() < .12:
                    c = shade(MOSS, rnd.randint(-6, 6))
                im.putpixel((x, y), c)
    return im


def shake_edge(seed):
    """The roof's edge: shake butts above, a dark fascia, then the eave boards under it."""
    rnd = random.Random(seed)
    top, eave = shakes(seed), ceiling(seed + 1)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            c = top.getpixel((x, y + 4)) if y < 8 else shade(BEAM, -6 + rnd.randint(-2, 2)) if y < 10 else eave.getpixel((x, y))
            im.putpixel((x, y), c)
    return im


def gable(seed):
    """Board and batten: wide upright boards with a narrow batten over each seam, standing proud and catching the light."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tone = [rnd.randint(-8, 8) for _ in range(2)]
    for x in range(16):
        for y in range(16):
            k = tone[x // 8] + rnd.randint(-3, 3) + (-6 if (y * 5 + x) % 9 == 0 else 0)
            c = shade(GABLE, k)
            if x in (7, 15):
                c = shade(GABLE, 12 + rnd.randint(-2, 2))
            elif x in (6, 14):
                c = shade(GABLE, -24)
            if x in (7, 15) and y in (3, 12):
                c = shade('#3a332b', 0)
            im.putpixel((x, y), c)
    return im


def deck(seed):
    """The porch decking: narrow boards gone grey, a dark gap between each, cupped a little so their middles catch the light."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tone = [rnd.randint(-8, 8) for _ in range(4)]
    for y in range(16):
        board, row = y // 4, y % 4
        for x in range(16):
            k = tone[board] + rnd.randint(-3, 3) + (5 if row == 1 else 0)
            c = shade(DECK, k)
            if row == 3:
                c = shade(DECK, -38 + rnd.randint(-2, 2))
            elif rnd.random() < .05:
                c = shade(DECK, -16)
            im.putpixel((x, y), c)
    return im


BLOCKS = [('cabin_wall', 'Cabin Wall'), ('cabin_sill', 'Fieldstone Footing'), ('cabin_floor', 'Pine Floorboards'),
          ('cabin_ceiling', 'Tongue and Groove Ceiling'), ('cabin_beam', 'Hewn Beam'), ('cabin_shakes', 'Cedar Shakes'),
          ('cabin_shake_slab', 'Cedar Shake Slab'), ('cabin_gable', 'Board and Batten'), ('cabin_porch', 'Porch Decking')]


def textures():
    return {'cabin_shiplap': shiplap(4061), 'cabin_fieldstone': fieldstone(4062), 'cabin_floor': floor(4063), 'cabin_ceiling': ceiling(4064),
            'cabin_beam': beam(4065), 'cabin_shakes': shakes(4066), 'cabin_shake_edge': shake_edge(4067), 'cabin_gable': gable(4068),
            'cabin_porch': deck(4069)}


def cube(name):
    return {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}


def skin(out, inside):
    """A wall block one thick: its outside on the side it faces and on its edges, its inside on the side within."""
    faces = {'north': '#out', 'south': '#in', 'east': '#out', 'west': '#out', 'up': '#out', 'down': '#out'}
    return {'parent': 'minecraft:block/block', 'textures': {'particle': out, 'out': out, 'in': inside},
            'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {f: {'texture': t, 'cullface': f} for f, t in faces.items()}}]}


def files():
    out = {}
    for name, outside, inside in (('cabin_wall', P + 'cabin_shiplap', PINE), ('cabin_sill', P + 'cabin_fieldstone', P + 'cabin_beam')):
        model = NS + f'block/room_shell_{name}'
        out[f'models/block/room_shell_{name}.json'] = skin(outside, inside)
        out[f'blockstates/room_shell_{name}.json'] = {'variants': {
            f'facing={f}': dict({'model': model}, **({'y': y} if y else {})) for f, y in {'north': 0, 'east': 90, 'south': 180, 'west': 270}.items()}}
    for name in ('cabin_ceiling', 'cabin_beam', 'cabin_shakes', 'cabin_gable'):
        out[f'models/block/room_shell_{name}.json'] = cube(name)
        out[f'blockstates/room_shell_{name}.json'] = {'variants': {'': {'model': NS + f'block/room_shell_{name}'}}}
    for name in ('cabin_floor', 'cabin_porch'):
        model = NS + f'block/room_shell_{name}'
        out[f'models/block/room_shell_{name}.json'] = cube(name)
        out[f'blockstates/room_shell_{name}.json'] = {'variants': {'': [{'model': model}, {'model': model, 'y': 180}]}}
    tex = {'bottom': P + 'cabin_ceiling', 'top': P + 'cabin_shakes', 'side': P + 'cabin_shake_edge'}
    slab = NS + 'block/room_shell_cabin_shake_slab'
    out['models/block/room_shell_cabin_shake_slab.json'] = {'parent': 'minecraft:block/slab', 'textures': tex}
    out['models/block/room_shell_cabin_shake_slab_top.json'] = {'parent': 'minecraft:block/slab_top', 'textures': tex}
    out['blockstates/room_shell_cabin_shake_slab.json'] = {'variants': {
        'type=bottom': {'model': slab}, 'type=top': {'model': slab + '_top'}, 'type=double': {'model': NS + 'block/room_shell_cabin_shakes'}}}
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
    print(('Verified' if check else 'Drew') + f' the cabin at the end of the world: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
