"""The service lift, the turning landings and the twin lifts (0.4.378, docs/TEXTURE_PASS_PUZZLES_0_4_378.md): the texture pass's
second interior release.

Draws, in native Minecraft pixels from fixed seeds, the three older puzzle rooms' own shells: limewashed plaster (plain, with a
hairline crack, and with a flake gone to the stone), scrubbed deal floorboards, beaded boarding for ceilings and counters, sooty
brick for the lift shaft, the stairwell's damp coursed stone and its flags with moss in the joints, old dark landing boards, the
worn tread of the landing stair, and the twin lifts' lobby brick. Writes the models and block states (the stair's in full, every
facing, half and shape), and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/puzzle_shell/'

LIME = '#d5cfc0'
DEAL = '#b9a37f'
SPRUCE = '#86684a'
SOOT_BRICK = '#5f463b'
SOOT_MORTAR = '#38312e'
CISTERN = '#6d7169'
CISTERN_JOINT = '#3d413b'
MOSS = '#4f6a36'
DARK = '#4a3425'
LOBBY = '#87503d'
LOBBY_MORTAR = '#b4ab98'

PLASTERS = 3


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def plaster(seed, k=0):
    """Limewash over plaster: soft clouding where the coats lie thicker, a faint brush arc. One a crack runs down; one has lost a
    flake, the grey of the wall showing in it with the lime's edge lit round it."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    cloud = [[rnd.randint(-2, 2) for _ in range(4)] for _ in range(4)]
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(LIME, cloud[y // 4][x // 4] + rnd.randint(-2, 2)))
    for i in range(6):
        x, y = 3 + i * 2, 4 + abs(i - 2)
        im.putpixel((x, y), shade(im.getpixel((x, y)), 4))
    if k == 1:
        x = rnd.randrange(4, 9)
        for y in range(1, 15):
            im.putpixel((x, y), shade(LIME, -26))
            if x + 1 < 16:
                im.putpixel((x + 1, y), shade(im.getpixel((x + 1, y)), 5))
            x += rnd.choice((0, 0, 1, -1)); x = max(1, min(14, x))
    elif k == 2:
        cx, cy = rnd.randrange(4, 10), rnd.randrange(6, 10)
        flake = [(cx + dx, cy + dy) for dx in range(-1, 3) for dy in range(-1, 2) if abs(dx - .5) + abs(dy) < 2.6]
        for x, y in flake:
            im.putpixel((x, y), shade(CISTERN, 8 + rnd.randint(-3, 3)))
        for x, y in flake:
            if (x, y - 1) not in flake:
                im.putpixel((x, y - 1), shade(LIME, 10))
    return im


def boards(seed, base, worn=False):
    """Floorboards, four a block running north to south, the end joints staggered, nailed in pairs at each end; scrubbed pale
    along the grain. Worn (the landing stair's tread): the middle of the tread rubbed paler where every foot has gone."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    joint = [rnd.randrange(3, 13) for _ in range(4)]
    tone = [rnd.randint(-4, 4) for _ in range(4)]
    grain = [[rnd.randint(-3, 3) for _ in range(16)] for _ in range(4)]
    for x in range(16):
        b, col = x // 4, x % 4
        for y in range(16):
            k = tone[b] + grain[b][y] // 2 + rnd.randint(-1, 1) + (-12 if col == 3 else 4 if col == 0 else 0)
            if y == joint[b]:
                k -= 14
            if worn:
                d = abs(x - 7.5) / 7.5 + abs(y - 7.5) / 9
                k += max(0, int(14 * (1 - d)))
            im.putpixel((x, y), shade(base, k))
    # A nail either side of each end joint, one to each board end, set off the line of the other.
    for b in range(4):
        for x, y in ((b * 4 + 1, joint[b] - 2), (b * 4 + 2, joint[b] + 2)):
            if 0 <= y < 16:
                im.putpixel((x, y), shade(base, -16))
    return im


def boarding(seed):
    """Beaded tongue-and-groove: narrow boards, a bead run down each joint, lit on one side and shadowed on the other."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tone = [rnd.randint(-4, 4) for _ in range(4)]
    for x in range(16):
        b, col = x // 4, x % 4
        for y in range(16):
            k = tone[b] + rnd.randint(-2, 2) + (y * 7 + b * 5) % 9 // 4
            k += -14 if col == 0 else 8 if col == 1 else 0
            im.putpixel((x, y), shade(SPRUCE, k))
    return im


def brick(seed, base, mortar, courses=4, length=8, soot=False):
    """Brick in stretcher bond: four courses a block, two bricks to a course and the courses staggered. Sooty: darker towards
    the top, where the smoke and the rope's grease went up the shaft."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    h = 16 // courses
    tones = {}
    for y in range(16):
        course = y // h
        for x in range(16):
            n = ((x + (length // 2) * (course % 2)) // length) % (16 // length)
            key = (course, n)
            if key not in tones:
                tones[key] = rnd.randint(-8, 8)
            k = tones[key] + rnd.randint(-2, 2) + (4 if y % h == 0 else 0)
            if soot:
                k -= (15 - y) // 3
            joint = y % h == h - 1 or (x + (length // 2) * (course % 2)) % length == 0
            im.putpixel((x, y), shade(mortar, rnd.randint(-3, 3) - ((15 - y) // 4 if soot else 0)) if joint else shade(base, k))
    return im


def cistern(seed):
    """The stairwell's coursed stone: two courses of uneven blocks, the joints wide and dark, wet patches where it seeps and a
    white streak of lime washed out of a joint."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    cuts = [rnd.randrange(4, 8), rnd.randrange(9, 13)]
    tones = [rnd.randint(-6, 6) for _ in range(4)]
    for y in range(16):
        course = y // 8
        for x in range(16):
            stone = course * 2 + (0 if x < cuts[course] else 1)
            k = tones[stone] + rnd.randint(-3, 3) + (5 if y % 8 == 0 else -3 if y % 8 == 6 else 0)
            im.putpixel((x, y), shade(CISTERN, k))
    for x in range(16):
        im.putpixel((x, 7), shade(CISTERN_JOINT, rnd.randint(-2, 2)))
        im.putpixel((x, 15), shade(CISTERN_JOINT, rnd.randint(-2, 2)))
    for course in range(2):
        for y in range(course * 8, course * 8 + 7):
            im.putpixel((cuts[course], y), shade(CISTERN_JOINT, rnd.randint(-2, 2)))
    for _ in range(2):
        cx, cy = rnd.randrange(2, 14), rnd.randrange(2, 14)
        for dx in range(-1, 2):
            for dy in range(-1, 3):
                x, y = cx + dx, cy + dy
                if 0 <= x < 16 and 0 <= y < 16 and abs(dx) + abs(dy - 1) < 3:
                    im.putpixel((x, y), shade(im.getpixel((x, y)), -9))
    # A little lime washed out of the lower joint, a pixel or two down the stone.
    x = cuts[1]
    for y in range(8, 8 + rnd.randrange(2, 4)):
        im.putpixel((x, y), shade('#a9a99c', rnd.randint(-6, 2)))
    return im


def flags(seed):
    """The cistern floor: flags in a grid, worn smooth, moss grown into the joints."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-6, 6) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            k = tones[(y // 8) * 2 + x // 8] + rnd.randint(-3, 3)
            im.putpixel((x, y), shade(CISTERN, k - 4))
    for i in range(16):
        for x, y in ((i, 0), (i, 8), (0, i), (8, i)):
            im.putpixel((x, y), shade(MOSS, rnd.randint(-8, 6)) if rnd.random() < .6 else shade(CISTERN_JOINT, rnd.randint(-2, 2)))
    for _ in range(3):
        x, y = rnd.choice((1, 9)), rnd.randrange(1, 15)
        im.putpixel((x, y), shade(MOSS, rnd.randint(0, 10)))
    return im


BLOCKS = [
    ('plaster', 'Limewashed Plaster'),
    ('boards', 'Scrubbed Boards'),
    ('boarding', 'Beaded Boarding'),
    ('shaft', 'Shaft Brick'),
    ('cistern', 'Cistern Stone'),
    ('cistern_floor', 'Cistern Flags'),
    ('landing', 'Landing Boards'),
    ('stair', 'Landing Stair'),
    ('lobby', 'Lobby Brick'),
]


def textures():
    out = {
        'boards': boards(3781, DEAL), 'boarding': boarding(3782), 'shaft': brick(3783, SOOT_BRICK, SOOT_MORTAR, soot=True),
        'cistern': cistern(3784), 'cistern_floor': flags(3785), 'landing': boards(3786, DARK), 'stair': boards(3786, DARK, worn=True),
        'lobby': brick(3787, LOBBY, LOBBY_MORTAR),
    }
    for k in range(PLASTERS):
        out['plaster' + (f'_{k}' if k else '')] = plaster(3780 + 10 * k, k)
    return out


def stair_states(model):
    """The stair's block states, laid out as vanilla's own stairs are: every facing, half and shape, turned and lit alike."""
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


def files():
    out = {}
    for name, _ in BLOCKS:
        model = NS + f'block/puzzle_shell_{name}'
        if name == 'stair':
            tex = {'bottom': P + 'landing', 'top': P + 'stair', 'side': P + 'landing'}
            out[f'models/block/puzzle_shell_{name}.json'] = {'parent': 'minecraft:block/stairs', 'textures': tex}
            out[f'models/block/puzzle_shell_{name}_inner.json'] = {'parent': 'minecraft:block/inner_stairs', 'textures': tex}
            out[f'models/block/puzzle_shell_{name}_outer.json'] = {'parent': 'minecraft:block/outer_stairs', 'textures': tex}
            out[f'blockstates/puzzle_shell_{name}.json'] = {'variants': stair_states(model)}
            continue
        out[f'models/block/puzzle_shell_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        variant = {'model': model}
        if name == 'plaster':
            # Each stretch of plaster picks plain, cracked or flaked by where it stands, plain the most often.
            for k in range(1, PLASTERS):
                out[f'models/block/puzzle_shell_{name}_{k}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'{name}_{k}'}}
            variant = [{'model': model, 'weight': 4}] + [{'model': model + f'_{k}'} for k in range(1, PLASTERS)]
        elif name in ('boards', 'landing'):
            # Boards run one way in a room; turned end for end so the joints do not repeat.
            variant = [{'model': model}, {'model': model, 'y': 180}]
        elif name == 'cistern_floor':
            variant = [{'model': model}, {'model': model, 'y': 90}, {'model': model, 'y': 180}, {'model': model, 'y': 270}]
        out[f'blockstates/puzzle_shell_{name}.json'] = {'variants': {'': variant}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/puzzle_shell' / f'{key}.png'
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
        key = f'block.the_oldest_house.puzzle_shell_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the puzzle rooms\' shells: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
