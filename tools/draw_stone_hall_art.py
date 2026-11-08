"""The stone halls (0.4.374, docs/TEXTURE_PASS_STONE_0_4_374.md): the texture pass's second release.

Draws, in native Minecraft pixels from fixed seeds, the surfaces of the halls the House turns to below twelve doors: dressed
ashlar laid in two courses a block, the same stone weathered, cracked and damp, a damp course at the foot of the walls, flagstones
and the same flags worn smooth down the middle where people walk, and a carved band where the chiselled courses were. Writes the
models and block states, and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/hall_stone/'

STONE = '#928b7e'
MORTAR = '#625c53'
FLAG = '#7f7a72'
FLAG_JOINT = '#55514b'
SLATE = '#43464b'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def tint(c, dr, dg, db):
    r, g, b = c[:3]
    return (max(0, min(255, r + dr)), max(0, min(255, g + dg)), max(0, min(255, b + db)), 255)


def ashlar(seed, base=STONE, grain=4):
    """Two courses a block, eight pixels each, the joints staggered: the upper stone breaks at the block's edge, the lower in its
    middle. Each stone has its own tone, a lit top edge and a shadowed foot, and the fine grain of tooled limestone."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-5, 5) for _ in range(3)]
    for y in range(16):
        for x in range(16):
            course = 0 if y < 8 else 1
            stone = 0 if course == 0 else (1 if x < 8 else 2)
            k = tones[stone] + rnd.randint(-grain, grain)
            row = y % 8
            if row == 0:
                k += 7
            elif row == 6:
                k -= 6
            im.putpixel((x, y), shade(base, k))
    for x in range(16):
        im.putpixel((x, 7), shade(MORTAR, rnd.randint(-3, 3)))
        im.putpixel((x, 15), shade(MORTAR, rnd.randint(-3, 3)))
    for y in range(0, 7):
        im.putpixel((0, y), shade(MORTAR, rnd.randint(-3, 3)))
    for y in range(8, 15):
        im.putpixel((8, y), shade(MORTAR, rnd.randint(-3, 3)))
    # A pit or two in the tooled face.
    for _ in range(3):
        x, y = rnd.randrange(1, 15), rnd.randrange(16)
        if y % 8 not in (0, 7):
            im.putpixel((x, y), shade(im.getpixel((x, y)), -12))
    return im


def weathered(seed):
    """The same stone, darker, with rain marks run down from the joints."""
    im = ashlar(seed, base=shade(STONE, -8)[:3])
    rnd = random.Random(seed + 1)
    for x in (rnd.randrange(2, 7), rnd.randrange(10, 15)):
        top = rnd.randrange(0, 4)
        for y in range(top, 16):
            if y in (7, 15):
                continue
            k = -10 if (y - top) < 5 else -6
            im.putpixel((x, y), shade(im.getpixel((x, y)), k))
    return im


CRACKS = 6


def crack_line(im, points, deep=-18, lit=8):
    """A crack along a path of pixels: the split itself dark, the edge it broke from catching the light on its right."""
    for x, y in points:
        if 0 <= x < 16 and 0 <= y < 16:
            im.putpixel((x, y), shade(MORTAR, deep))
            if x + 1 < 16 and (x + 1, y) not in points:
                im.putpixel((x + 1, y), shade(im.getpixel((x + 1, y)), lit))


def wander(rnd, x, y, steps, dx=(0, 1, 1), dy=1):
    """A crack's path: a pixel at a time, never straight for long."""
    out = []
    for _ in range(steps):
        out.append((x, y))
        if dy:
            y += dy; x += rnd.choice(dx)
        else:
            x += 1; y += rnd.choice(dx)
    return out


def cracked(seed, k=0):
    """Six different cracks, so a wall never shows the same one twice in a row: down a lower stone; along an upper stone; through
    a course joint, with the corner chipped where it crossed; branching; a spalled chip out of a face; a faint hairline across both
    courses. Each is drawn over its own stone, so the tones differ too. The block state picks one by where the block stands."""
    im = ashlar(seed)
    rnd = random.Random(seed + 2)
    if k == 0:
        crack_line(im, wander(rnd, rnd.randrange(2, 6), 8, 7))
    elif k == 1:
        crack_line(im, wander(rnd, 1, rnd.randrange(2, 5), 13, dx=(0, 0, 1, -1), dy=0))
    elif k == 2:
        x = rnd.randrange(10, 13)
        path = wander(rnd, x, 2, 11, dx=(0, 0, 1, -1))
        crack_line(im, path)
        # The corner chipped off where the crack crossed the joint.
        jx = [px for px, py in path if py == 7][0]
        for cx, cy in ((jx - 1, 6), (jx - 1, 8), (jx, 8)):
            if 0 <= cx < 16:
                im.putpixel((cx, cy), shade(MORTAR, -8))
    elif k == 3:
        x = rnd.randrange(10, 13)
        trunk = [(x, y) for y in range(14, 10, -1)]
        left = wander(rnd, x - 1, 10, 3, dx=(-1, -1, 0), dy=-1)
        right = wander(rnd, x + 1, 10, 3, dx=(1, 0), dy=-1)
        crack_line(im, trunk + left + right)
    elif k == 4:
        # A spall: a chip gone out of the face, its floor in shadow and its upper lip in light.
        cx, cy = rnd.randrange(2, 6), rnd.randrange(3, 5)
        chip = [(cx + dx, cy + dy) for dx in range(3) for dy in range(2)] + [(cx + 1, cy + 2)]
        for x, y in chip:
            im.putpixel((x, y), shade(STONE, -22))
        for dx in range(3):
            im.putpixel((cx + dx, cy - 1), shade(STONE, 14))
        crack_line(im, wander(rnd, cx + 3, cy + 1, 3, dx=(0, 1), dy=1), deep=-10, lit=4)
    else:
        path, x = [], rnd.randrange(0, 3)
        for y in range(16):
            path.append((x, y)); x += rnd.choice((0, 1, 1, 1))
        crack_line(im, path, deep=-8, lit=4)
    return im


def damp(seed):
    """The stone with damp come up into it: the lower half darker and a little green, under a wavering tide line."""
    im = ashlar(seed)
    rnd = random.Random(seed + 3)
    line = [9 + rnd.choice((0, 0, 1, -1)) for _ in range(16)]
    for x in range(16):
        for y in range(16):
            if y >= line[x]:
                depth = min(5, y - line[x])
                im.putpixel((x, y), tint(im.getpixel((x, y)), -14 - depth, -9 - depth, -16 - depth))
        im.putpixel((x, line[x]), tint(im.getpixel((x, line[x])), -6, -2, -8))
    return im


def damp_course(seed):
    """The foot of the wall: the last of the ashlar above, a dark slate course laid in the joint, and a plinth stone below it,
    darkened where the damp stops."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            if y < 4:
                k = rnd.randint(-4, 4) + (7 if y == 0 else 0) - (5 if y == 3 else 0)
                c = shade(STONE, k)
            elif y < 6:
                c = shade(SLATE, rnd.randint(-3, 3) + (10 if y == 4 else 0))
            else:
                k = rnd.randint(-4, 4) - 10 - (y - 6)
                c = tint(shade(STONE, k), -4, -1, -5)
            im.putpixel((x, y), c)
    for y in range(6, 16):
        im.putpixel((4, y), shade(MORTAR, rnd.randint(-3, 3) - 6))
    for x in range(16):
        im.putpixel((x, 6), shade(im.getpixel((x, 6)), 6))
    return im


# The flags: four stones that tile, so a floor reads as laid stone, never a grid.
FLAGS = [(0, 0, 9, 6), (10, 0, 15, 6), (0, 7, 5, 15), (6, 7, 15, 15)]


def flags(seed, worn=False):
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-6, 6) for _ in FLAGS]
    for i, (x0, y0, x1, y1) in enumerate(FLAGS):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                grain = 2 if worn else 5
                k = tones[i] + rnd.randint(-grain, grain)
                if worn:
                    # Polished by feet: paler towards the middle of each stone.
                    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
                    k += 10 - int(2.2 * (abs(x - cx) + abs(y - cy)) / 2)
                if y == y0:
                    k += 4
                im.putpixel((x, y), shade(FLAG, k))
    joint = FLAG_JOINT if not worn else shade(FLAG_JOINT, 10)[:3]
    for x in range(16):
        im.putpixel((x, 6), shade(joint, rnd.randint(-2, 2)) if True else None)
        im.putpixel((x, 15), shade(joint, rnd.randint(-2, 2)))
    for y in range(0, 6):
        im.putpixel((9, y), shade(joint, rnd.randint(-2, 2)))
        im.putpixel((15, y), shade(joint, rnd.randint(-2, 2)))
    for y in range(7, 15):
        im.putpixel((5, y), shade(joint, rnd.randint(-2, 2)))
        im.putpixel((15, y), shade(joint, rnd.randint(-2, 2)))
    return im


def carved(seed):
    """A carved band: a moulding top and foot, and between them a running chevron cut into the stone, lit on its upper faces."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(STONE, rnd.randint(-3, 3)))
    for x in range(16):
        im.putpixel((x, 0), shade(STONE, 14)); im.putpixel((x, 1), shade(STONE, 4)); im.putpixel((x, 2), shade(MORTAR, 8))
        im.putpixel((x, 13), shade(STONE, 12)); im.putpixel((x, 14), shade(STONE, -2)); im.putpixel((x, 15), shade(MORTAR, 0))
    # The chevron: two strokes a period of eight, cut a pixel deep.
    for x in range(16):
        p = x % 8
        y = 4 + (p if p < 4 else 7 - p)
        im.putpixel((x, y), shade(MORTAR, -6))
        im.putpixel((x, y + 1), shade(STONE, -10))
        if y - 1 >= 3:
            im.putpixel((x, y - 1), shade(STONE, 12))
        y2 = y + 4
        if y2 < 13:
            im.putpixel((x, y2), shade(MORTAR, -6))
            if y2 - 1 >= 3:
                im.putpixel((x, y2 - 1), shade(STONE, 12))
    return im


BLOCKS = [
    ('ashlar', 'Ashlar'),
    ('ashlar_weathered', 'Weathered Ashlar'),
    ('ashlar_cracked', 'Cracked Ashlar'),
    ('ashlar_damp', 'Damp Ashlar'),
    ('damp_course', 'Damp Course'),
    ('flags', 'Flagstones'),
    ('flags_worn', 'Worn Flagstones'),
    ('carved', 'Carved Band'),
]


def textures():
    return {
        'ashlar': ashlar(374), 'ashlar_weathered': weathered(3741), 'ashlar_cracked': cracked(3742), 'ashlar_damp': damp(3743),
        **{f'ashlar_cracked_{k}': cracked(3742 + 10 * k, k) for k in range(1, CRACKS)},
        'damp_course': damp_course(3744), 'flags': flags(3745), 'flags_worn': flags(3745, worn=True), 'carved': carved(3746),
    }


def files():
    out = {}
    for name, _ in BLOCKS:
        out[f'models/block/hall_stone_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        model = NS + f'block/hall_stone_{name}'
        variant = {'model': model}
        if name == 'ashlar_cracked':
            # Each cracked block picks one of the six cracks by where it stands.
            for k in range(1, CRACKS):
                out[f'models/block/hall_stone_ashlar_cracked_{k}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'ashlar_cracked_{k}'}}
            variant = [{'model': model}] + [{'model': model + f'_{k}'} for k in range(1, CRACKS)]
        if name.startswith('flags'):
            # The flags turn with where they lie, so a long floor does not show its repeat.
            variant = [{'model': model}, {'model': model, 'y': 90}, {'model': model, 'y': 180}, {'model': model, 'y': 270}]
        out[f'blockstates/hall_stone_{name}.json'] = {'variants': {'': variant}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/hall_stone' / f'{key}.png'
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
        key = f'block.the_oldest_house.hall_stone_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the stone halls: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
