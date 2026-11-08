"""Holloway's camp and the preserved cave's chapel (0.4.381, docs/TEXTURE_PASS_CAMP_0_4_381.md): the texture pass's fifth interior release.

Draws, in native Minecraft pixels from fixed seeds, what was still vanilla in Holloway's hunt and the drowned congregation's chapel.
The camp's three rooms: rough ashlar (plain, or cracked, or mossed in the joints, picked by position), irregular flags, hewn timber,
plank boards, packed grit and slate. The chapel: pews worn smooth where people sat, and the dark boards of the causeway. Writes the
models and block states (the pew's in full) and the blocks' names. --check verifies everything written.
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

STONE = '#7d7c76'
JOINT = '#4f4e4a'
MOSS = '#556b34'
FLAG = '#86847c'
TIMBER = '#6e5a44'
BOARD = '#5c4a36'
GRIT = '#6f6a5f'
SLATE = '#3e4146'
PEW = '#4a3626'
PEW_SEAT = '#5e4530'
CAUSEWAY = '#3e3024'

ASHLARS = 3


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def ashlar(seed, k=0):
    """The hunt's rough ashlar: big blocks in two courses, the joints wide and dark. The second pattern has a crack run down the top
    course; the third has moss grown into the joints."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    cuts = [rnd.randrange(4, 9), rnd.randrange(8, 13)]
    tones = [rnd.randint(-7, 7) for _ in range(4)]
    for y in range(16):
        course = y // 8
        for x in range(16):
            stone = course * 2 + (0 if x < cuts[course] else 1)
            k2 = tones[stone] + rnd.randint(-4, 4) + (5 if y % 8 == 0 else -4 if y % 8 == 6 else 0)
            im.putpixel((x, y), shade(STONE, k2))
    for x in range(16):
        for y in (7, 15):
            im.putpixel((x, y), shade(JOINT, rnd.randint(-3, 3)))
    for course in range(2):
        for y in range(course * 8, course * 8 + 7):
            im.putpixel((cuts[course], y), shade(JOINT, rnd.randint(-3, 3)))
    if k == 1:
        x = rnd.randrange(3, 12)
        for y in range(0, 7):
            im.putpixel((x, y), shade(STONE, -34))
            x = max(1, min(14, x + rnd.choice((0, 1, -1))))
    if k == 2:
        for x in range(16):
            for y in (7, 15):
                if rnd.random() < .6:
                    im.putpixel((x, y), shade(MOSS, rnd.randint(-8, 8)))
            if rnd.random() < .25:
                im.putpixel((x, 6), shade(MOSS, rnd.randint(-6, 4)))
    return im


def flags(seed):
    """Irregular flags: three rows of uneven height, each cut at its own places, worn smooth."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    rows = [0, rnd.randrange(4, 7), rnd.randrange(9, 12), 16]
    cuts = [sorted(rnd.sample(range(3, 14), 2)) for _ in range(3)]
    tone = {}
    for y in range(16):
        r = next(i for i in range(3) if rows[i] <= y < rows[i + 1])
        for x in range(16):
            key = (r, sum(x >= c for c in cuts[r]))
            if key not in tone:
                tone[key] = rnd.randint(-8, 8)
            joint = y == rows[r] or x in cuts[r] or x == 0
            im.putpixel((x, y), shade(JOINT, rnd.randint(-3, 4)) if joint else shade(FLAG, tone[key] + rnd.randint(-3, 3)))
    return im


def timber(seed):
    """Hewn timber: three planks stood upright, of uneven widths, the adze's marks across them."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tone = [rnd.randint(-6, 6) for _ in range(3)]
    for x in range(16):
        b = 0 if x < 5 else 1 if x < 11 else 2
        for y in range(16):
            k = tone[b] + rnd.randint(-3, 3) + ((y + b * 3) % 5 == 0) * -5
            if x in (0, 5, 11):
                k = -24
            im.putpixel((x, y), shade(TIMBER, k))
    return im


def boards(seed, base):
    """Boards, four a block, the end joints staggered."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    joint = [rnd.randrange(2, 14) for _ in range(4)]
    tone = [rnd.randint(-5, 5) for _ in range(4)]
    for x in range(16):
        b, col = x // 4, x % 4
        for y in range(16):
            k = tone[b] + rnd.randint(-2, 2) + (-14 if col == 3 else 4 if col == 0 else 0) - (16 if y == joint[b] else 0)
            im.putpixel((x, y), shade(base, k))
    return im


def grit(seed):
    """Packed grit: fine, with a dark stone or a pale one here and there."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            im.putpixel((x, y), shade(GRIT, rnd.randint(-5, 5) + (-14 if r < .06 else 10 if r > .95 else 0)))
    return im


def slate(seed):
    """Slate laid in courses overhead, each course staggered, the lower edges dark."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        row = y // 4
        for x in range(16):
            off = (x + 2 * (row % 2)) % 8
            k = rnd.randint(-3, 3) + (-14 if y % 4 == 3 or off == 0 else 4 if y % 4 == 0 else 0)
            im.putpixel((x, y), shade(SLATE, k))
    return im


def pew_seat(seed):
    """The pew's seat, worn smooth and pale where people sat, the grain running along it."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-2, 2) + ((x + y // 5) % 7 == 0) * -4 + (8 if 4 <= y <= 11 else 0) - (12 if y in (0, 15) else 0)
            im.putpixel((x, y), shade(PEW_SEAT, k))
    return im


def pew_side(seed):
    """The pew's ends and back: dark boards framed at either edge."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-2, 2) + (-14 if x in (0, 15) else 6 if x in (1, 14) else 0) + ((y + x // 3) % 6 == 0) * -3
            im.putpixel((x, y), shade(PEW, k))
    return im

BLOCKS = [
    ('camp_ashlar', 'Rough Ashlar'),
    ('camp_flags', 'Hunt Flags'),
    ('camp_timber', 'Hewn Timber'),
    ('camp_boards', 'Camp Boards'),
    ('camp_grit', 'Packed Grit'),
    ('camp_slate', 'Slate Ceiling'),
    ('pew', 'Chapel Pew'),
    ('causeway', 'Causeway Boards'),
]


def textures():
    out = {'camp_flags': flags(3813), 'camp_timber': timber(3814), 'camp_boards': boards(3815, BOARD), 'camp_grit': grit(3816),
           'camp_slate': slate(3817), 'pew_seat': pew_seat(3818), 'pew_side': pew_side(3819), 'causeway': boards(3820, CAUSEWAY)}
    for k in range(ASHLARS):
        out['camp_ashlar' + (f'_{k}' if k else '')] = ashlar(3812 + 10 * k, k)
    return out


def stair_states(model):
    """The pew's block states, laid out as vanilla's own stairs are: every facing, half and shape, turned and lit alike."""
    rot = {'east': 0, 'south': 90, 'west': 180, 'north': 270}
    out = {}
    for facing, y0 in rot.items():
        for half in ('bottom', 'top'):
            for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
                kind = '' if shape == 'straight' else '_inner' if shape.startswith('inner') else '_outer'
                y = y0
                if half == 'bottom' and shape.endswith('left'):
                    y = y0 - 90
                if half == 'top' and shape.endswith('right'):
                    y = y0 + 90
                v = {'model': model + kind}
                if half == 'top':
                    v['x'] = 180
                y %= 360
                if y:
                    v['y'] = y
                if half == 'top' or y:
                    v['uvlock'] = True
                out[f'facing={facing},half={half},shape={shape}'] = v
    return out


def cube(name):
    return {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}


def files():
    out = {}
    for name, _ in BLOCKS:
        model = NS + f'block/room_shell_{name}'
        variant = {'model': model}
        if name == 'pew':
            tex = {'bottom': P + 'pew_side', 'top': P + 'pew_seat', 'side': P + 'pew_side'}
            out[f'models/block/room_shell_{name}.json'] = {'parent': 'minecraft:block/stairs', 'textures': tex}
            out[f'models/block/room_shell_{name}_inner.json'] = {'parent': 'minecraft:block/inner_stairs', 'textures': tex}
            out[f'models/block/room_shell_{name}_outer.json'] = {'parent': 'minecraft:block/outer_stairs', 'textures': tex}
            out[f'blockstates/room_shell_{name}.json'] = {'variants': stair_states(model)}
            continue
        out[f'models/block/room_shell_{name}.json'] = cube(name)
        if name == 'camp_ashlar':
            # Each block picks plain, cracked or mossed by where it stands, plain the most often.
            for k in range(1, ASHLARS):
                out[f'models/block/room_shell_{name}_{k}.json'] = cube(f'{name}_{k}')
            variant = [{'model': model, 'weight': 4}] + [{'model': model + f'_{k}'} for k in range(1, ASHLARS)]
        elif name == 'camp_flags':
            variant = [{'model': model}, {'model': model, 'y': 90}, {'model': model, 'y': 180}, {'model': model, 'y': 270}]
        elif name in ('camp_boards', 'causeway'):
            variant = [{'model': model}, {'model': model, 'y': 180}]
        out[f'blockstates/room_shell_{name}.json'] = {'variants': {'': variant}}
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
    print(('Verified' if check else 'Drew') + f' Holloway\'s camp and the chapel: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
